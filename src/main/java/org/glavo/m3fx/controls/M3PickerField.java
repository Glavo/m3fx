// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/// A base control for Material Design 3 text fields that edit values through a picker popup.
///
/// Subclasses provide value parsing, formatting, range checks, and the concrete picker control while this base class
/// owns the shared text field, popup placement, popup motion, accessibility state, and validation handoff.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview),
/// [Material Design time pickers](https://m3.material.io/components/time-pickers/overview), and
/// [Material Design text fields](https://m3.material.io/components/text-fields/overview).
///
/// @param <T> the value type edited by the field
/// @param <P> the popup picker control type
@NotNullByDefault
public abstract class M3PickerField<T, P extends Control> extends Control {
    /// The base style class for M3FX picker fields.
    public static final String STYLE_CLASS = "m3-picker-field";

    /// The base style class applied to picker popup surfaces.
    public static final String POPUP_STYLE_CLASS = "m3-picker-field-popup";

    /// The style class applied to the trailing picker open button.
    public static final String OPEN_BUTTON_STYLE_CLASS = "m3-picker-field-open-button";

    /// The vertical gap between the field and popup picker.
    private static final double POPUP_OFFSET_Y = 8.0;

    /// The initial popup picker scale used for enter and exit motion.
    private static final double POPUP_TRANSITION_SCALE = 0.96;

    /// The initial popup picker offset used for enter and exit motion.
    private static final double POPUP_TRANSITION_OFFSET_Y = 6.0;

    // The selected value, or `null` when the field is empty.
    private final ObjectProperty<@Nullable T> value =
            new SimpleObjectProperty<>(this, "value") {
                /// Normalizes and validates values assigned through the property.
                @Override
                public void set(@Nullable T newValue) {
                    @Nullable T normalizedValue = normalizeNullableValue(newValue);
                    validateValue(normalizedValue);
                    super.set(normalizedValue);
                }

                /// Synchronizes the editor and popup picker after the selected value changes.
                @Override
                protected void invalidated() {
                    handleValueChanged(get());
                }
            };

    // The formatter used to convert between editor text and picker values.
    private final ObjectProperty<DateTimeFormatter> formatter =
            new SimpleObjectProperty<>(this, "formatter") {
                /// Keeps formatter values non-null.
                @Override
                public void set(DateTimeFormatter newValue) {
                    super.set(Objects.requireNonNull(newValue, "formatter"));
                }

                /// Rewrites the editor text when a selected value already exists.
                @Override
                protected void invalidated() {
                    updateEditorFromValue();
                }
            };

    // The error message shown when editor text cannot be parsed.
    private final StringProperty invalidTextErrorText =
            new SimpleStringProperty(this, "invalidTextErrorText") {
                /// Keeps parse error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "invalidTextErrorText"));
                }
            };

    // The error message shown when editor text parses outside the selectable range.
    private final StringProperty rangeErrorText =
            new SimpleStringProperty(this, "rangeErrorText") {
                /// Keeps range error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "rangeErrorText"));
                }
            };

    /// The editable text field used by the picker field.
    private final M3TextField editor = new M3TextField();

    /// The Material text input layout wrapping the editor and open button.
    private final M3TextInputLayout inputLayout = new M3TextInputLayout(editor);

    /// The concrete popup picker control.
    private final P picker;

    // The picker value property used to synchronize popup selections.
    private final ObjectProperty<@Nullable T> pickerValue;

    /// The trailing button that opens the popup picker.
    private final M3IconButton openButton;

    /// The popup root that inherits scene styles and hosts the picker.
    private final StackPane popupContent = new StackPane();

    /// The popup window used for picker display.
    private final Popup popup = new Popup();

    /// Keeps the detached picker popup synchronized with the owner scene and theme context while visible.
    private final M3PopupContextSynchronizer popupContextSynchronizer =
            new M3PopupContextSynchronizer(this, popupContent, M3Stylesheets.controlStylesheet("picker-field.css"));

    // Whether the popup picker is currently showing.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The picker popup enter animation.
    private final Timeline showAnimation = new Timeline();

    /// The picker popup exit animation.
    private final Timeline hideAnimation = new Timeline();

    /// Observes runtime motion settings while this field is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Reports popup picker focus changes through this field's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, popupContent, this::focusNode, this::notifyFocusNodeChanged);

    /// Whether value listeners are currently synchronizing the field and picker.
    private boolean synchronizingValue;

    /// Whether the editor text is currently being rewritten from a selected value.
    private boolean updatingEditorText;

    /// Whether focus should return to the editor after the popup hides.
    private boolean focusEditorOnHidden;

    /// The vertical offset used by the current popup hide animation.
    private double popupTransitionOffsetY = -POPUP_TRANSITION_OFFSET_Y;

    /// Creates a picker field around the supplied popup picker.
    ///
    /// @param picker the concrete popup picker control
    /// @param pickerValue the picker value property synchronized with this field value
    /// @param formatter the formatter used to convert between editor text and picker values
    /// @param styleClass the concrete picker field style class
    /// @param popupStyleClass the concrete picker popup style class
    /// @param pickerIconGraphic the graphic displayed by the trailing open button
    /// @param openButtonAccessibleText the accessible text for the trailing open button
    /// @param invalidTextErrorText the error text shown when editor text cannot be parsed
    /// @param rangeErrorText the error text shown when editor text parses outside the selectable range
    protected M3PickerField(
            P picker,
            ObjectProperty<@Nullable T> pickerValue,
            DateTimeFormatter formatter,
            String styleClass,
            String popupStyleClass,
            Node pickerIconGraphic,
            String openButtonAccessibleText,
            String invalidTextErrorText,
            String rangeErrorText
    ) {
        this.picker = Objects.requireNonNull(picker, "picker");
        this.pickerValue = Objects.requireNonNull(pickerValue, "pickerValue");
        this.openButton = new M3IconButton(Objects.requireNonNull(pickerIconGraphic, "pickerIconGraphic"));
        this.formatter.set(Objects.requireNonNull(formatter, "formatter"));
        this.invalidTextErrorText.set(Objects.requireNonNull(invalidTextErrorText, "invalidTextErrorText"));
        this.rangeErrorText.set(Objects.requireNonNull(rangeErrorText, "rangeErrorText"));
        initialize(
                Objects.requireNonNull(styleClass, "styleClass"),
                Objects.requireNonNull(popupStyleClass, "popupStyleClass"),
                Objects.requireNonNull(openButtonAccessibleText, "openButtonAccessibleText")
        );
    }

    /// Returns the selected value, or `null` when the field is empty.
    ///
    /// @return the selected value, or `null` when the field is empty
    public final @Nullable T getValue() {
        return value.get();
    }

    /// Sets the selected value, or clears the field when `null` is supplied.
    ///
    /// @param value the selected value, or `null` to clear the field
    public final void setValue(@Nullable T value) {
        this.value.set(value);
    }

    /// Returns the selected value property.
    ///
    /// @return the selected value property
    public final ObjectProperty<@Nullable T> valueProperty() {
        return value;
    }

    /// Returns the editable text field shown inside the Material input layout.
    ///
    /// @return the editable text field shown inside the Material input layout
    public final M3TextField getEditor() {
        return editor;
    }

    /// Returns the Material text input layout used by this picker field.
    ///
    /// @return the Material text input layout used by this picker field
    public final M3TextInputLayout getInputLayout() {
        return inputLayout;
    }

    /// Returns the concrete popup picker control.
    ///
    /// @return the concrete popup picker control
    public final P getPicker() {
        return picker;
    }

    /// Returns the formatter used for editor text.
    ///
    /// @return the formatter used for editor text
    public final DateTimeFormatter getFormatter() {
        return formatter.get();
    }

    /// Sets the formatter used for editor text.
    ///
    /// @param formatter the formatter used for editor text
    public final void setFormatter(DateTimeFormatter formatter) {
        this.formatter.set(formatter);
    }

    /// Returns the editor text formatter property.
    ///
    /// @return the editor text formatter property
    public final ObjectProperty<DateTimeFormatter> formatterProperty() {
        return formatter;
    }

    /// Returns the label text displayed by the wrapped input layout.
    ///
    /// @return the label text displayed by the wrapped input layout
    public final String getLabelText() {
        return inputLayout.getLabelText();
    }

    /// Sets the label text displayed by the wrapped input layout.
    ///
    /// @param labelText the label text displayed by the wrapped input layout
    public final void setLabelText(String labelText) {
        inputLayout.setLabelText(labelText);
    }

    /// Returns the label text property from the wrapped input layout.
    ///
    /// @return the label text property from the wrapped input layout
    public final StringProperty labelTextProperty() {
        return inputLayout.labelTextProperty();
    }

    /// Returns the supporting text shown when no error is active.
    ///
    /// @return the supporting text shown when no error is active
    public final String getSupportingText() {
        return inputLayout.getSupportingText();
    }

    /// Sets the supporting text shown when no error is active.
    ///
    /// @param supportingText the supporting text shown when no error is active
    public final void setSupportingText(String supportingText) {
        inputLayout.setSupportingText(supportingText);
    }

    /// Returns the supporting text property from the wrapped input layout.
    ///
    /// @return the supporting text property from the wrapped input layout
    public final StringProperty supportingTextProperty() {
        return inputLayout.supportingTextProperty();
    }

    /// Returns the current error text shown by the wrapped input layout.
    ///
    /// @return the current error text shown by the wrapped input layout
    public final String getErrorText() {
        return inputLayout.getErrorText();
    }

    /// Sets the current error text shown by the wrapped input layout.
    ///
    /// @param errorText the current error text shown by the wrapped input layout
    public final void setErrorText(String errorText) {
        inputLayout.setErrorText(errorText);
    }

    /// Returns the error text property from the wrapped input layout.
    ///
    /// @return the error text property from the wrapped input layout
    public final StringProperty errorTextProperty() {
        return inputLayout.errorTextProperty();
    }

    /// Returns the parse error message used when editor text is invalid.
    ///
    /// @return the parse error message used when editor text is invalid
    public final String getInvalidTextErrorText() {
        return invalidTextErrorText.get();
    }

    /// Sets the parse error message used when editor text is invalid.
    ///
    /// @param invalidTextErrorText the parse error message used when editor text is invalid
    public final void setInvalidTextErrorText(String invalidTextErrorText) {
        this.invalidTextErrorText.set(invalidTextErrorText);
    }

    /// Returns the parse error message property.
    ///
    /// @return the parse error message property
    public final StringProperty invalidTextErrorTextProperty() {
        return invalidTextErrorText;
    }

    /// Returns the range error message used when editor text is outside the selectable range.
    ///
    /// @return the range error message used when editor text is outside the selectable range
    public final String getRangeErrorText() {
        return rangeErrorText.get();
    }

    /// Sets the range error message used when editor text is outside the selectable range.
    ///
    /// @param rangeErrorText the range error message used when editor text is outside the selectable range
    public final void setRangeErrorText(String rangeErrorText) {
        this.rangeErrorText.set(rangeErrorText);
    }

    /// Returns the range error message property.
    ///
    /// @return the range error message property
    public final StringProperty rangeErrorTextProperty() {
        return rangeErrorText;
    }

    /// Returns whether the picker popup is currently showing.
    ///
    /// @return `true` when the picker popup is showing
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only popup showing property.
    ///
    /// @return the read-only popup showing property
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Parses the current editor text, updates the selected value, and returns whether the text is valid.
    ///
    /// @return `true` when the editor text was committed as a valid value
    public final boolean commitEditorText() {
        String text = editor.getText() == null ? "" : editor.getText().trim();
        if (text.isEmpty()) {
            setValue(null);
            inputLayout.setErrorText("");
            return true;
        }

        try {
            T parsedValue = normalizeValue(parseValue(text, getFormatter()));
            if (isPickerValueDisabled(parsedValue)) {
                inputLayout.setErrorText(getRangeErrorText());
                return false;
            }

            setValue(parsedValue);
            inputLayout.setErrorText("");
            return true;
        } catch (DateTimeException | IllegalArgumentException e) {
            inputLayout.setErrorText(getInvalidTextErrorText());
            return false;
        }
    }

    /// Shows the picker popup when this field is attached to a window.
    public final void showPicker() {
        if (!M3Accessible.canReach(this) || popup.isShowing()) {
            return;
        }

        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null) {
            return;
        }

        popupContextSynchronizer.start();
        preparePopupForShow();
        @Nullable M3PopupPositioning.Placement placement =
                M3PopupPositioning.menuBelowOrAbove(inputLayout, popupContent, POPUP_OFFSET_Y);
        if (placement == null) {
            popupContextSynchronizer.stop();
            return;
        }

        popupTransitionOffsetY = placement.opensAbove() ? POPUP_TRANSITION_OFFSET_Y : -POPUP_TRANSITION_OFFSET_Y;
        preparePopupForShowAnimation();
        popup.show(this, placement.x(), placement.y());
        showing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
        popupFocusNotifier.refresh();
        playShowAnimation();
    }

    /// Hides the picker popup.
    public final void hidePicker() {
        hidePicker(false);
    }

    /// Toggles the picker popup.
    public final void togglePicker() {
        if (popup.isShowing()) {
            hidePicker();
        } else {
            showPicker();
        }
    }

    /// Returns accessibility attributes for the embedded editor and popup picker.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case FOCUS_NODE -> focusNode();
            case SELECTED_ITEMS -> selectedItems();
            case SUBMENU -> picker;
            case TEXT -> editor.getText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes editor and popup accessibility actions.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_MENU, EXPAND -> showPicker();
            case COLLAPSE -> hidePicker(true);
            case SHOW_ITEM -> showPickerAndForwardAccessibleAction(action, parameters);
            case SET_SELECTED_ITEMS, INCREMENT, DECREMENT, BLOCK_INCREMENT, BLOCK_DECREMENT ->
                    forwardPickerAccessibleAction(action, parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Parses one non-empty editor text value.
    ///
    /// @param text the non-empty editor text to parse
    /// @param formatter the formatter used by this field
    /// @return the parsed value
    protected abstract T parseValue(String text, DateTimeFormatter formatter);

    /// Formats one selected value for editor display.
    ///
    /// @param value the selected value to format
    /// @param formatter the formatter used by this field
    /// @return the editor text representation of the value
    protected abstract String formatValue(T value, DateTimeFormatter formatter);

    /// Normalizes one selected value to the precision used by the picker.
    ///
    /// @param value the selected value to normalize
    /// @return the normalized value
    protected abstract T normalizeValue(T value);

    /// Returns whether one normalized value is outside the concrete picker's selectable range.
    ///
    /// @param value the normalized value to test
    /// @return `true` when the value is outside the selectable range
    protected abstract boolean isPickerValueDisabled(T value);

    /// Applies a field value to the concrete picker without changing field-specific state.
    ///
    /// @param value the field value to apply to the picker, or `null` to clear picker selection
    protected abstract void setPickerValue(@Nullable T value);

    /// Replaces the node hosted by the popup surface.
    ///
    /// @param content the popup content node
    protected final void setPopupContent(Node content) {
        popupContent.getChildren().setAll(Objects.requireNonNull(content, "content"));
    }

    /// Restores the popup surface to host only the concrete picker.
    protected final void resetPopupContent() {
        setPopupContent(picker);
    }

    /// Adds style classes, installs handlers, and prepares the popup.
    private void initialize(String styleClass, String popupStyleClass, String openButtonAccessibleText) {
        M3ControlStyles.add(this, STYLE_CLASS);
        M3ControlStyles.add(this, styleClass);
        M3ControlStyles.add(popupContent, POPUP_STYLE_CLASS);
        M3ControlStyles.add(popupContent, popupStyleClass);
        M3ControlStyles.add(openButton, OPEN_BUTTON_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.COMBO_BOX);

        inputLayout.setTrailing(openButton);
        inputLayout.disableProperty().bind(disabledProperty());
        inputLayout.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        picker.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        popupContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        openButton.setAccessibleText(openButtonAccessibleText);
        openButton.setOnAction(event -> togglePicker());

        popupContent.getChildren().setAll(picker);
        popup.setAutoHide(true);
        popup.getContent().add(popupContent);
        popup.setOnHidden(event -> handlePopupHidden());

        pickerValue.addListener((observable, oldValue, newValue) -> handlePickerValueChanged(newValue));
        editor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        editor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        editor.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused && !popup.isShowing()) {
                commitEditorText();
            }
        });
        editor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingEditorText) {
                clearGeneratedErrorText();
            }
        });
        popupContent.addEventHandler(KeyEvent.KEY_PRESSED, this::handlePickerKeyPressed);
        popupFocusNotifier.start();
    }

    /// Handles editor action commits.
    private void handleEditorAction(ActionEvent event) {
        if (commitEditorText()) {
            hidePicker();
        }
        event.consume();
    }

    /// Handles editor keyboard opening and dismissal.
    private void handleEditorKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case DOWN, F4 -> {
                showPicker();
                focusPicker();
                event.consume();
            }
            case ESCAPE -> {
                if (popup.isShowing()) {
                    hidePicker(true);
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Handles picker keyboard dismissal.
    private void handlePickerKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && popup.isShowing()) {
            hidePicker(true);
            event.consume();
        }
    }

    /// Hides the popup picker and optionally restores editor focus.
    private void hidePicker(boolean focusEditor) {
        if (!popup.isShowing()) {
            return;
        }

        focusEditorOnHidden = focusEditor;
        showAnimation.stop();
        if (hideAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hideAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                event -> popup.hide(),
                new KeyValue(popupContent.opacityProperty(), 0.0, spec.interpolator()),
                new KeyValue(popupContent.scaleXProperty(), POPUP_TRANSITION_SCALE, spec.interpolator()),
                new KeyValue(popupContent.scaleYProperty(), POPUP_TRANSITION_SCALE, spec.interpolator()),
                new KeyValue(popupContent.translateYProperty(), popupTransitionOffsetY, spec.interpolator())
        ));
        M3Animation.playFromStart(this, hideAnimation);
    }

    /// Handles a selected value coming from the popup picker.
    private void handlePickerValueChanged(@Nullable T newValue) {
        if (synchronizingValue) {
            return;
        }

        setValue(newValue);
        if (popup.isShowing()) {
            hidePicker(true);
        }
    }

    /// Synchronizes dependent state after the selected value changes.
    private void handleValueChanged(@Nullable T newValue) {
        synchronizingValue = true;
        try {
            setPickerValue(newValue);
            updateEditorFromValue();
            clearGeneratedErrorText();
        } finally {
            synchronizingValue = false;
        }

        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        notifyFocusNodeChanged();
    }

    /// Updates editor text from the currently selected value.
    private void updateEditorFromValue() {
        updatingEditorText = true;
        try {
            @Nullable T selectedValue = getValue();
            editor.setText(selectedValue == null ? "" : formatValue(selectedValue, getFormatter()));
        } finally {
            updatingEditorText = false;
        }
    }

    /// Validates one normalized value.
    private void validateValue(@Nullable T value) {
        if (value != null && isPickerValueDisabled(value)) {
            throw new IllegalArgumentException("value is outside the selectable range");
        }
    }

    /// Normalizes a nullable selected value.
    private @Nullable T normalizeNullableValue(@Nullable T value) {
        return value == null ? null : normalizeValue(value);
    }

    /// Returns the field selection as an immutable accessibility list.
    private List<T> selectedItems() {
        @Nullable T selectedValue = getValue();
        return selectedValue == null ? List.of() : List.of(selectedValue);
    }

    /// Returns the current keyboard focus node for accessibility clients.
    private Node focusNode() {
        if (!popup.isShowing()) {
            return fieldFocusNode();
        }

        @Nullable Node popupFocusOwner = popupFocusOwner();
        if (popupFocusOwner != null) {
            return popupFocusOwner;
        }

        @Nullable Object focusNode = picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node && M3Accessible.canReach(node) ? node : picker;
    }

    /// Returns the current focus target inside the closed field.
    private Node fieldFocusNode() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner != null) {
            if (M3Accessible.containsNode(editor, focusOwner)) {
                return editor;
            }
            if (M3Accessible.containsNode(openButton, focusOwner)) {
                return openButton;
            }
        }
        return editor;
    }

    /// Returns the current focus owner inside popup content when it belongs to the popup scene.
    private @Nullable Node popupFocusOwner() {
        @Nullable Scene popupScene = popupContent.getScene();
        @Nullable Node focusOwner = popupScene == null ? null : popupScene.getFocusOwner();
        if (focusOwner != null && M3Accessible.containsNode(popupContent, focusOwner)
                && M3Accessible.canReach(focusOwner)) {
            return focusOwner;
        }
        return null;
    }

    /// Shows the popup when possible, forwards an accessibility action to the picker, and focuses its item.
    private void showPickerAndForwardAccessibleAction(AccessibleAction action, Object... parameters) {
        if (!M3Accessible.canReach(this)) {
            return;
        }
        boolean preservePopupFocus = popup.isShowing() && parameters.length == 0 && popupFocusOwner() != null;
        showPicker();
        if (!popup.isShowing()) {
            return;
        }
        if (!preservePopupFocus) {
            forwardPickerAccessibleAction(action, parameters);
        }
        focusPicker();
    }

    /// Forwards value-oriented accessibility actions to the concrete popup picker.
    private void forwardPickerAccessibleAction(AccessibleAction action, Object... parameters) {
        picker.executeAccessibleAction(action, parameters);
    }

    /// Focuses the preferred node inside the popup picker.
    private void focusPicker() {
        if (!popup.isShowing()) {
            return;
        }

        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
        }
    }

    /// Requests focus for the current editor, open button, or popup focus target.
    private void focusAccessibleNode() {
        if (!M3Accessible.canReach(this)) {
            return;
        }
        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
        }
    }

    /// Notifies accessibility clients and owner containers about the exposed focus target.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
    }

    /// Clears generated parse or range errors after user edits.
    private void clearGeneratedErrorText() {
        String errorText = inputLayout.getErrorText();
        if (errorText.equals(getInvalidTextErrorText()) || errorText.equals(getRangeErrorText())) {
            inputLayout.setErrorText("");
        }
    }

    /// Copies owner motion, CSS, and minimum-width state into the popup-hosted picker.
    private void preparePopupForShow() {
        popupContextSynchronizer.sync();
        M3Animation.copyResolvedMotionSettings(this, popupContent);
        double fieldWidth = Math.max(0.0, inputLayout.getWidth());
        M3Css.setMinWidthIfUnbound(popupContent, Math.max(fieldWidth, popupContent.minWidth(-1.0)));
        popupContent.applyCss();
    }

    /// Applies initial visual state before the popup is shown.
    private void preparePopupForShowAnimation() {
        hideAnimation.stop();
        popupContent.setOpacity(0.0);
        popupContent.setScaleX(POPUP_TRANSITION_SCALE);
        popupContent.setScaleY(POPUP_TRANSITION_SCALE);
        popupContent.setTranslateY(popupTransitionOffsetY);
    }

    /// Plays the popup picker enter animation.
    private void playShowAnimation() {
        showAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        showAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(popupContent.opacityProperty(), 1.0, spec.interpolator()),
                new KeyValue(popupContent.scaleXProperty(), 1.0, spec.interpolator()),
                new KeyValue(popupContent.scaleYProperty(), 1.0, spec.interpolator()),
                new KeyValue(popupContent.translateYProperty(), 0.0, spec.interpolator())
        ));
        M3Animation.playFromStart(this, showAnimation);
    }

    /// Resets transient popup picker animation transforms.
    private void resetPopupAnimationState() {
        showAnimation.stop();
        hideAnimation.stop();
        popupContent.setOpacity(1.0);
        popupContent.setScaleX(1.0);
        popupContent.setScaleY(1.0);
        popupContent.setTranslateY(0.0);
    }

    /// Applies changed runtime motion settings to active picker popup animations.
    private void refreshMotionSettings() {
        if (popup.isShowing()) {
            M3Animation.copyResolvedMotionSettings(this, popupContent);
        }
        M3Animation.finishRunningAnimationsIfDisabled(this, showAnimation, hideAnimation);
    }

    /// Handles popup hidden cleanup and optional focus return.
    private void handlePopupHidden() {
        popupContextSynchronizer.stop();
        showing.set(false);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
        popupFocusNotifier.refresh();
        resetPopupAnimationState();
        if (focusEditorOnHidden) {
            focusEditorOnHidden = false;
            if (M3Accessible.canReach(editor)) {
                M3Accessible.showItem(this, editor);
            }
        }
    }

    /// Returns the user-agent stylesheet for M3FX picker fields.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("picker-field.css");
    }
}
