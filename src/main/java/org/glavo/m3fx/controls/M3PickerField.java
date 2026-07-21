// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/// A base control for Material Design 3 text fields that edit values through a picker popup.
///
/// [M3DatePickerField] and [M3TimePickerField] combine editable text with a non-modal picker popup. The raw editor
/// text and committed value are deliberately separate: changing [#textProperty()] does not change [#valueProperty()]
/// until [#commitEditorText()] succeeds, while changing the value immediately rewrites the text with the current
/// [#formatterProperty()]. An empty committed string clears the value.
///
/// The popup has no independent owner property; [#showPicker()] uses the window containing this control and has no
/// effect until that window can show popups. It is auto-hiding, closes after a picker selection, and is also closed
/// when this control becomes unreachable. [#showingProperty()] is read-only and remains `true` until a requested hide
/// has completed. Showing and hiding are non-blocking.
///
/// ```java
/// M3DatePickerField field = new M3DatePickerField();
/// field.setLabelText("Start date");
/// field.setValue(java.time.LocalDate.of(2026, 7, 17));
/// field.valueProperty().addListener((observable, oldDate, newDate) ->
///         System.out.println("Selected date: " + newDate));
/// ```
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview),
/// [Material Design time pickers](https://m3.material.io/components/time-pickers/overview), and
/// [Material Design text fields](https://m3.material.io/components/text-fields/overview).
///
/// @param <T> the value type edited by the field
/// @param <P> the popup picker control type
@NotNullByDefault
public abstract sealed class M3PickerField<T, P extends Control> extends Control
        permits M3DatePickerField, M3TimePickerField {
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

    /// The editable text field used by the picker field.
    private final M3TextField editor = new M3TextField();

    /// The Material text input layout wrapping the editor and open button.
    private final M3TextInputLayout inputLayout = new M3TextInputLayout(editor);

    /// The concrete popup picker control.
    private final P picker;

    /// The picker value property used to synchronize popup selections.
    private final ObjectProperty<@Nullable T> pickerValue;

    /// The trailing button that opens the popup picker.
    private final M3IconButton openButton;

    /// The node currently designated as the popup body.
    private Node popupContent;

    /// The concrete picker popup style class.
    private final String popupStyleClass;

    /// The lazily created and subsequently reused popup presentation.
    private @Nullable PickerPopup pickerPopup;

    /// Whether value listeners are currently synchronizing the field and picker.
    private boolean synchronizingValue;

    /// Whether the editor text is currently being rewritten from a selected value.
    private boolean updatingEditorText;

    /// Creates a picker field around the supplied popup picker.
    ///
    /// @param picker                   the concrete popup picker control
    /// @param pickerValue              the picker value property synchronized with this field value
    /// @param formatter                the formatter used to convert between editor text and picker values
    /// @param styleClass               the concrete picker field style class
    /// @param popupStyleClass          the concrete picker popup style class
    /// @param pickerIconGraphic        the graphic displayed by the trailing open button
    /// @param openButtonAccessibleText the accessible text for the trailing open button
    /// @param invalidTextErrorText     the error text shown when editor text cannot be parsed
    /// @param rangeErrorText           the error text shown when editor text parses outside the selectable range
    /// @throws NullPointerException if `picker`, `pickerValue`, `formatter`, `styleClass`, `popupStyleClass`,
    ///         `pickerIconGraphic`, `openButtonAccessibleText`, `invalidTextErrorText`, or `rangeErrorText` is `null`
    M3PickerField(
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
        this.popupContent = this.picker;
        this.popupStyleClass = Objects.requireNonNull(popupStyleClass, "popupStyleClass");
        this.openButton = new M3IconButton(Objects.requireNonNull(pickerIconGraphic, "pickerIconGraphic"));
        this.formatter.set(Objects.requireNonNull(formatter, "formatter"));
        this.invalidTextErrorText.set(Objects.requireNonNull(invalidTextErrorText, "invalidTextErrorText"));
        this.rangeErrorText.set(Objects.requireNonNull(rangeErrorText, "rangeErrorText"));
        editor.textProperty().bindBidirectional(text);
        editor.variantProperty().bindBidirectional(variant);
        inputLayout.characterCounterVisibleProperty().bindBidirectional(characterCounterVisible);
        inputLayout.characterLimitEnforcedProperty().bindBidirectional(characterLimitEnforced);
        inputLayout.characterLimitProperty().bindBidirectional(characterLimit);
        inputLayout.labelTextProperty().bindBidirectional(labelText);
        inputLayout.supportingTextProperty().bindBidirectional(supportingText);
        inputLayout.errorTextProperty().bindBidirectional(errorText);
        initialize(
                Objects.requireNonNull(styleClass, "styleClass"),
                Objects.requireNonNull(openButtonAccessibleText, "openButtonAccessibleText")
        );
    }

    /// The selected and committed picker value.
    ///
    /// A `null` value represents an empty field. Direct non-null assignments are normalized to the precision
    /// supported by the concrete picker and must fall within its selectable range. A binding source must already
    /// provide normalized values in that range. Changing the value rewrites [#textProperty()] using the current
    /// formatter and synchronizes the popup picker.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable T> value =
            new SimpleObjectProperty<>(this, "value") {
                /// Normalizes and validates values assigned through the property.
                @Override
                public void set(@Nullable T newValue) {
                    @Nullable T normalizedValue = newValue == null ? null : normalizeValue(newValue);
                    validateValue(normalizedValue);
                    super.set(normalizedValue);
                }

                /// Synchronizes the editor and popup picker after the selected value changes.
                @Override
                protected void invalidated() {
                    @Nullable T selectedValue = get();
                    if (isBound() && selectedValue != null) {
                        T normalizedValue = normalizeValue(selectedValue);
                        if (!Objects.equals(normalizedValue, selectedValue)
                                || isPickerValueDisabled(selectedValue)) {
                            return;
                        }
                    }
                    handleValueChanged(selectedValue);
                }
            };

    /// Returns the selected value, or `null` when the field is empty.
    ///
    /// @return the selected value, or `null` when the field is empty
    public final @Nullable T getValue() {
        return value.get();
    }

    /// Sets the selected value, or clears the field when `null` is supplied.
    ///
    /// @param value the selected value, or `null` to clear the field
    /// @throws IllegalArgumentException if `value` is outside the concrete picker's selectable range
    public final void setValue(@Nullable T value) {
        this.value.set(value);
    }

    /// Returns the observable, bindable committed-value property.
    ///
    /// The property is `null` by default. Direct non-null assignments are normalized and validated by the concrete
    /// picker. A binding source must supply values that are already normalized and within the current selectable
    /// range. Valid changes rewrite editor text and synchronize the popup picker.
    ///
    /// @return the committed-value property
    public final ObjectProperty<@Nullable T> valueProperty() {
        return value;
    }

    /// The non-null raw editor text.
    ///
    /// Text may be incomplete or invalid while the user edits it. Assigning text does not update [#valueProperty()]
    /// until [#commitEditorText()] succeeds.
    ///
    /// @defaultValue `""`
    private final StringProperty text = new SimpleStringProperty(this, "text", "") {
        /// Keeps editor text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "text"));
        }
    };

    /// Returns the current raw editor text.
    ///
    /// This text may be temporarily invalid while the user is editing. Call [#commitEditorText()] to parse it into
    /// [#valueProperty()].
    ///
    /// @return the current raw editor text
    public final String getText() {
        return text.get();
    }

    /// Sets the raw editor text.
    ///
    /// The value is not parsed until [#commitEditorText()] is called or the editor action commits it.
    ///
    /// @param text the raw editor text
    /// @throws NullPointerException if `text` is `null`
    public final void setText(String text) {
        this.text.set(text);
    }

    /// Returns the observable, bindable raw editor-text property.
    ///
    /// The property is the empty string by default and rejects `null`. Assignments update the embedded editor but do
    /// not change [#valueProperty()] until the text is committed successfully.
    ///
    /// @return the raw editor-text property
    public final StringProperty textProperty() {
        return text;
    }

    /// The visual variant of the editable text field.
    ///
    /// The property rejects `null`.
    ///
    /// @defaultValue [M3TextInputVariant#FILLED]
    private final ObjectProperty<M3TextInputVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3TextInputVariant.FILLED) {
                /// Keeps the text input variant non-null.
                @Override
                public void set(M3TextInputVariant newValue) {
                    super.set(Objects.requireNonNull(newValue, "variant"));
                }
            };

    /// Returns the text input variant used by the embedded editor.
    ///
    /// @return the text input variant used by the embedded editor
    public final M3TextInputVariant getVariant() {
        return variant.get();
    }

    /// Sets the text input variant used by the embedded editor.
    ///
    /// @param variant the text input variant used by the embedded editor
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3TextInputVariant variant) {
        this.variant.set(variant);
    }

    /// Returns the observable, bindable text-input variant property.
    ///
    /// The property is [M3TextInputVariant#FILLED] by default and rejects `null`. It is bidirectionally synchronized
    /// with the embedded editor's variant property.
    ///
    /// @return the text-input variant property
    public final ObjectProperty<M3TextInputVariant> variantProperty() {
        return variant;
    }

    /// Whether the character counter is shown below the editor.
    ///
    /// This property does not enforce [#characterLimitProperty()]; enforcement is controlled independently by
    /// [#characterLimitEnforcedProperty()].
    ///
    /// @defaultValue `false`
    private final BooleanProperty characterCounterVisible =
            new SimpleBooleanProperty(this, "characterCounterVisible");

    /// Returns whether the character counter is visible below the editor.
    ///
    /// @return `true` when the character counter is visible
    public final boolean isCharacterCounterVisible() {
        return characterCounterVisible.get();
    }

    /// Sets whether the character counter is visible below the editor.
    ///
    /// @param characterCounterVisible whether the character counter is visible
    public final void setCharacterCounterVisible(boolean characterCounterVisible) {
        this.characterCounterVisible.set(characterCounterVisible);
    }

    /// Returns the observable, bindable character-counter visibility property.
    ///
    /// The property is `false` by default and is bidirectionally synchronized with the wrapped input layout. It does
    /// not enforce the configured character limit.
    ///
    /// @return the character-counter visibility property
    public final BooleanProperty characterCounterVisibleProperty() {
        return characterCounterVisible;
    }

    /// Whether input longer than [#characterLimitProperty()] is rejected by the editor.
    ///
    /// This property has no effect while the character limit is `-1`.
    ///
    /// @defaultValue `false`
    private final BooleanProperty characterLimitEnforced =
            new SimpleBooleanProperty(this, "characterLimitEnforced");

    /// Returns whether editor text is truncated to the configured character limit.
    ///
    /// @return `true` when editor text is truncated to the configured character limit
    public final boolean isCharacterLimitEnforced() {
        return characterLimitEnforced.get();
    }

    /// Sets whether editor text is truncated to the configured character limit.
    ///
    /// @param characterLimitEnforced whether editor text is truncated to the configured character limit
    public final void setCharacterLimitEnforced(boolean characterLimitEnforced) {
        this.characterLimitEnforced.set(characterLimitEnforced);
    }

    /// Returns the observable, bindable character-limit enforcement property.
    ///
    /// The property is `false` by default and is bidirectionally synchronized with the wrapped input layout. It has
    /// no effect while [#getCharacterLimit()] is `-1`.
    ///
    /// @return the character-limit enforcement property
    public final BooleanProperty characterLimitEnforcedProperty() {
        return characterLimitEnforced;
    }

    /// The maximum editor-text length, or `-1` for no limit.
    ///
    /// Values less than `-1` are rejected. The limit is measured using the same character-count semantics as
    /// [M3TextInputLayout]. It may be displayed or enforced independently.
    ///
    /// @defaultValue `-1`
    private final IntegerProperty characterLimit = new SimpleIntegerProperty(this, "characterLimit", -1) {
        /// Accepts `-1` for no limit or a non-negative character count.
        @Override
        public void set(int newValue) {
            if (newValue < -1) {
                throw new IllegalArgumentException("characterLimit must be -1 or non-negative");
            }
            super.set(newValue);
        }
    };

    /// Returns the active character limit, or `-1` when no limit is active.
    ///
    /// @return the active character limit, or `-1` when no limit is active
    public final int getCharacterLimit() {
        return characterLimit.get();
    }

    /// Sets the active character limit, or `-1` to disable the limit.
    ///
    /// @param characterLimit the active character limit, or `-1` to disable the limit
    /// @throws IllegalArgumentException if `characterLimit` is less than `-1`
    public final void setCharacterLimit(int characterLimit) {
        this.characterLimit.set(characterLimit);
    }

    /// Returns the observable, bindable character-limit property.
    ///
    /// The property is `-1` by default and accepts `-1` or a non-negative character count. It is bidirectionally
    /// synchronized with the wrapped input layout.
    ///
    /// @return the character-limit property
    public final IntegerProperty characterLimitProperty() {
        return characterLimit;
    }

    /// The non-null formatter used to parse and display values.
    ///
    /// Changing the formatter immediately rewrites the editor when a value is selected. Concrete picker fields
    /// supply their documented initial formatter.
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

    /// Returns the formatter used for editor text.
    ///
    /// @return the formatter used for editor text
    public final DateTimeFormatter getFormatter() {
        return formatter.get();
    }

    /// Sets the formatter used for editor text.
    ///
    /// @param formatter the formatter used for editor text
    /// @throws NullPointerException if `formatter` is `null`
    public final void setFormatter(DateTimeFormatter formatter) {
        this.formatter.set(formatter);
    }

    /// Returns the observable, bindable non-null formatter property.
    ///
    /// The concrete picker field supplies the initial formatter. The property rejects `null`; changes immediately
    /// reformat editor text when a value is selected.
    ///
    /// @return the formatter property
    public final ObjectProperty<DateTimeFormatter> formatterProperty() {
        return formatter;
    }

    /// The non-null floating label text.
    ///
    /// An empty string suppresses the label.
    ///
    /// @defaultValue `""`
    private final StringProperty labelText = new SimpleStringProperty(this, "labelText", "") {
        /// Keeps label text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "labelText"));
        }
    };

    /// Returns the label text displayed by the wrapped input layout.
    ///
    /// @return the label text displayed by the wrapped input layout
    public final String getLabelText() {
        return labelText.get();
    }

    /// Sets the label text displayed by the wrapped input layout.
    ///
    /// @param labelText the label text displayed by the wrapped input layout
    /// @throws NullPointerException if `labelText` is `null`
    public final void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    /// Returns the observable, bindable non-null floating-label property.
    ///
    /// The property is the empty string by default, which suppresses the label, and rejects `null`. It is
    /// bidirectionally synchronized with the wrapped input layout.
    ///
    /// @return the floating-label property
    public final StringProperty labelTextProperty() {
        return labelText;
    }

    /// The non-null supporting text displayed when no error message is active.
    ///
    /// An empty string suppresses supporting text.
    ///
    /// @defaultValue `""`
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "") {
        /// Keeps supporting text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "supportingText"));
        }
    };

    /// Returns the supporting text shown when no error is active.
    ///
    /// @return the supporting text shown when no error is active
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the supporting text shown when no error is active.
    ///
    /// @param supportingText the supporting text shown when no error is active
    /// @throws NullPointerException if `supportingText` is `null`
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(supportingText);
    }

    /// Returns the observable, bindable non-null supporting-text property.
    ///
    /// The property is the empty string by default, which suppresses supporting text, and rejects `null`. It is
    /// bidirectionally synchronized with the wrapped input layout.
    ///
    /// @return the supporting-text property
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// The non-null error message currently displayed by the field.
    ///
    /// An empty string clears the error presentation. A failed [#commitEditorText()] replaces this value with either
    /// [#getInvalidTextErrorText()] or [#getRangeErrorText()]; subsequent user edits clear such generated messages.
    ///
    /// @defaultValue `""`
    private final StringProperty errorText = new SimpleStringProperty(this, "errorText", "") {
        /// Keeps error text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "errorText"));
        }
    };

    /// Returns the current error text shown by the wrapped input layout.
    ///
    /// @return the current error text shown by the wrapped input layout
    public final String getErrorText() {
        return errorText.get();
    }

    /// Sets the current error text shown by the wrapped input layout.
    ///
    /// @param errorText the current error text shown by the wrapped input layout
    /// @throws NullPointerException if `errorText` is `null`
    public final void setErrorText(String errorText) {
        this.errorText.set(errorText);
    }

    /// Returns the observable, bindable non-null current-error-text property.
    ///
    /// The property is the empty string by default, which clears error presentation, and rejects `null`. It is
    /// bidirectionally synchronized with the wrapped input layout and may be replaced by a failed text commit.
    ///
    /// @return the current-error-text property
    public final StringProperty errorTextProperty() {
        return errorText;
    }

    /// The non-null message used when editor text cannot be parsed.
    ///
    /// Concrete picker fields supply their documented initial message.
    private final StringProperty invalidTextErrorText =
            new SimpleStringProperty(this, "invalidTextErrorText") {
                /// Keeps parse error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "invalidTextErrorText"));
                }
            };

    /// Returns the parse error message used when editor text is invalid.
    ///
    /// @return the parse error message used when editor text is invalid
    public final String getInvalidTextErrorText() {
        return invalidTextErrorText.get();
    }

    /// Sets the parse error message used when editor text is invalid.
    ///
    /// @param invalidTextErrorText the parse error message used when editor text is invalid
    /// @throws NullPointerException if `invalidTextErrorText` is `null`
    public final void setInvalidTextErrorText(String invalidTextErrorText) {
        this.invalidTextErrorText.set(invalidTextErrorText);
    }

    /// Returns the observable, bindable non-null parse-error-message property.
    ///
    /// The concrete picker field supplies the initial message. The property rejects `null`; its current value is
    /// copied to [#errorTextProperty()] when editor text cannot be parsed.
    ///
    /// @return the parse-error-message property
    public final StringProperty invalidTextErrorTextProperty() {
        return invalidTextErrorText;
    }

    /// The non-null message used when parsed text is outside the selectable range.
    ///
    /// Concrete picker fields supply their documented initial message.
    private final StringProperty rangeErrorText =
            new SimpleStringProperty(this, "rangeErrorText") {
                /// Keeps range error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "rangeErrorText"));
                }
            };

    /// Returns the range error message used when editor text is outside the selectable range.
    ///
    /// @return the range error message used when editor text is outside the selectable range
    public final String getRangeErrorText() {
        return rangeErrorText.get();
    }

    /// Sets the range error message used when editor text is outside the selectable range.
    ///
    /// @param rangeErrorText the range error message used when editor text is outside the selectable range
    /// @throws NullPointerException if `rangeErrorText` is `null`
    public final void setRangeErrorText(String rangeErrorText) {
        this.rangeErrorText.set(rangeErrorText);
    }

    /// Returns the observable, bindable non-null range-error-message property.
    ///
    /// The concrete picker field supplies the initial message. The property rejects `null`; its current value is
    /// copied to [#errorTextProperty()] when parsed editor text is outside the selectable range.
    ///
    /// @return the range-error-message property
    public final StringProperty rangeErrorTextProperty() {
        return rangeErrorText;
    }

    /// Whether the picker popup is visible or completing its hide transition.
    ///
    /// This is derived read-only state. It becomes `true` only after the popup is shown successfully and returns to
    /// `false` when the popup has actually hidden.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// Returns whether the picker popup is currently showing.
    ///
    /// @return `true` when the picker popup is showing
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the observable, read-only picker-popup showing property.
    ///
    /// The property is `false` by default and cannot be written directly. It remains `true` through a requested hide
    /// transition and becomes `false` only after the popup has hidden.
    ///
    /// @return the read-only picker-popup showing property
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Returns the current editor text character count.
    ///
    /// @return the current editor text character count
    public final int getCharacterCount() {
        return inputLayout.getCharacterCount();
    }

    /// Returns whether the current editor text exceeds the active character limit.
    ///
    /// @return `true` when the current editor text exceeds the active character limit
    public final boolean isCharacterLimitExceeded() {
        return inputLayout.isCharacterLimitExceeded();
    }

    /// Returns the embedded editable text field.
    ///
    /// @return the embedded editable text field
    final M3TextField getEditor() {
        return editor;
    }

    /// Returns the Material text input layout used by this picker field.
    ///
    /// @return the Material text input layout used by this picker field
    final M3TextInputLayout getInputLayout() {
        return inputLayout;
    }

    /// Returns the popup picker owned by this field.
    ///
    /// The same control instance is returned on every call. Applications may configure its range and presentation,
    /// but must not add it to another parent because this field owns its popup presentation.
    ///
    /// @return the concrete popup picker control
    public final P getPicker() {
        return picker;
    }

    /// Parses and commits the current editor text.
    ///
    /// Leading and trailing whitespace is ignored. Empty text clears the selected value. Valid text is normalized,
    /// checked against the concrete picker's selectable range, assigned to [valueProperty], and reformatted for
    /// display. Invalid text leaves the previous value unchanged and displays the configured parse or range error.
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

    /// Shows the picker popup when this field has a reachable owner window.
    ///
    /// The call is non-blocking. It has no effect if the popup is already showing, this field is not effectively
    /// reachable, or popup placement cannot be resolved. Focus remains managed by the current field and picker
    /// interaction rather than being requested unconditionally by this method.
    public final void showPicker() {
        if (!M3Accessible.canReach(this) || isShowing() || !M3PopupWindows.canShow(this)) {
            return;
        }

        PickerPopup popup = pickerPopup();
        boolean popupShown = false;
        popup.contextSynchronizer.start();
        try {
            preparePopupForShow(popup);
            @Nullable M3PopupPositioning.Placement placement =
                    M3PopupPositioning.menuBelowOrAbove(inputLayout, popup.root, POPUP_OFFSET_Y);
            if (placement == null) {
                return;
            }

            popup.transitionOffsetY =
                    placement.opensAbove() ? POPUP_TRANSITION_OFFSET_Y : -POPUP_TRANSITION_OFFSET_Y;
            preparePopupForShowAnimation(popup);
            if (!M3PopupWindows.show(popup.window, this, placement.x(), placement.y())) {
                return;
            }
            popupShown = true;
        } finally {
            if (!popupShown) {
                resetPopupAnimationState(popup);
                popup.contextSynchronizer.stop();
            }
        }
        popup.focusNotifier.start();
        popup.reachabilityObserver.install();
        showing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
        popup.focusNotifier.refresh();
        playShowAnimation(popup);
    }

    /// Requests that the picker popup hide.
    ///
    /// The call is non-blocking and idempotent. [showingProperty] becomes `false` after the popup has actually
    /// hidden.
    public final void hidePicker() {
        hidePicker(false);
    }

    /// Shows or hides the picker popup according to its current window state.
    ///
    /// This method has the same owner and reachability restrictions as [showPicker].
    public final void togglePicker() {
        if (isShowing()) {
            hidePicker();
        } else {
            showPicker();
        }
    }

    /// Returns accessibility attributes for the embedded editor and popup picker.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case FOCUS_NODE -> focusNode();
            case ITEM_AT_INDEX -> accessibleItem(parameters);
            case SELECTED_ITEMS -> selectedItems();
            case SUBMENU -> picker;
            case TEXT -> editor.getText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes editor and popup accessibility actions.
    ///
    /// @param action     the accessibility action to execute
    /// @param parameters optional action-specific parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_MENU, EXPAND -> showPicker();
            case COLLAPSE -> hidePicker(true);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            case SET_SELECTED_ITEMS, INCREMENT, DECREMENT, BLOCK_INCREMENT, BLOCK_DECREMENT ->
                    forwardPickerAccessibleAction(action, parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Parses one non-empty editor text value.
    ///
    /// @param text      the non-empty editor text to parse
    /// @param formatter the formatter used by this field
    /// @return the parsed value
    abstract T parseValue(String text, DateTimeFormatter formatter);

    /// Formats one selected value for editor display.
    ///
    /// @param value     the selected value to format
    /// @param formatter the formatter used by this field
    /// @return the editor text representation of the value
    abstract String formatValue(T value, DateTimeFormatter formatter);

    /// Normalizes one selected value to the precision used by the picker.
    ///
    /// @param value the selected value to normalize
    /// @return the normalized value
    abstract T normalizeValue(T value);

    /// Returns whether one normalized value is outside the concrete picker's selectable range.
    ///
    /// @param value the normalized value to test
    /// @return `true` when the value is outside the selectable range
    abstract boolean isPickerValueDisabled(T value);

    /// Applies a field value to the concrete picker without changing field-specific state.
    ///
    /// @param value the field value to apply to the picker, or `null` to clear picker selection
    abstract void setPickerValue(@Nullable T value);

    /// Replaces the node hosted by the popup surface.
    ///
    /// @param content the popup content node
    final void setPopupContent(Node content) {
        popupContent = Objects.requireNonNull(content, "content");
        @Nullable PickerPopup popup = pickerPopup;
        if (popup != null) {
            popup.root.getChildren().setAll(content);
        }
    }

    /// Restores the popup surface to host only the concrete picker.
    final void resetPopupContent() {
        setPopupContent(picker);
    }

    /// Adds style classes and installs field-level handlers.
    private void initialize(String styleClass, String openButtonAccessibleText) {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        M3ControlStyles.add(this, styleClass);
        M3ControlStyles.add(openButton, OPEN_BUTTON_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.COMBO_BOX);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this,
                this::focusAccessibleNode,
                this::showAccessibleItem,
                this::handlesAccessibleShowTarget);

        inputLayout.setTrailing(openButton);
        inputLayout.disableProperty().bind(disabledProperty());
        inputLayout.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        picker.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        openButton.setAccessibleText(openButtonAccessibleText);
        openButton.setOnAction(event -> togglePicker());

        pickerValue.addListener((observable, oldValue, newValue) -> handlePickerValueChanged(newValue));
        editor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        editor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        editor.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused && !isShowing()) {
                commitEditorText();
            }
        });
        editor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingEditorText) {
                clearGeneratedErrorText();
            }
        });
    }

    /// Hides the popup if its owner field can no longer be reached from its scene.
    private void hidePopupIfOwnerUnreachable() {
        if (isShowing() && !M3Accessible.canReach(this)) {
            hidePicker(false);
        }
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
                if (isShowing()) {
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
        if (event.getCode() == KeyCode.ESCAPE && isShowing()) {
            hidePicker(true);
            event.consume();
        }
    }

    /// Hides the popup picker and optionally restores editor focus.
    private void hidePicker(boolean focusEditor) {
        @Nullable PickerPopup popup = pickerPopup;
        if (popup == null || !popup.window.isShowing()) {
            return;
        }

        popup.focusEditorOnHidden |= focusEditor;
        if (getScene() == null) {
            popup.window.hide();
            return;
        }
        if (popup.hiding && popup.animation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        popup.hiding = true;
        popup.animation.configure(
                spec,
                0.0,
                POPUP_TRANSITION_SCALE,
                POPUP_TRANSITION_SCALE,
                popup.root.getTranslateX(),
                popup.transitionOffsetY
        );
        M3Animation.playFromStart(this, popup.animation);
    }

    /// Handles a selected value coming from the popup picker.
    private void handlePickerValueChanged(@Nullable T newValue) {
        if (synchronizingValue) {
            return;
        }

        setValue(newValue);
        if (isShowing()) {
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

    /// Returns the field selection as an immutable accessibility list.
    private List<T> selectedItems() {
        @Nullable T selectedValue = getValue();
        return selectedValue == null ? List.of() : List.of(selectedValue);
    }

    /// Returns the indexed accessibility child used by skins and assistive clients.
    ///
    /// @param parameters the accessibility index parameters
    /// @return the indexed child, or `null` when the parameters do not address a child
    private @Nullable Node accessibleItem(Object... parameters) {
        return parameters.length == 1 && parameters[0] instanceof Integer index && index == 0
                ? inputLayout
                : null;
    }

    /// Returns the current keyboard focus node for accessibility clients.
    private Node focusNode() {
        if (!isShowing()) {
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
        @Nullable PickerPopup popup = pickerPopup;
        if (popup == null) {
            return null;
        }
        @Nullable Scene popupScene = popup.root.getScene();
        @Nullable Node focusOwner = popupScene == null ? null : popupScene.getFocusOwner();
        if (focusOwner != null && M3Accessible.containsNode(popup.root, focusOwner)
                && M3Accessible.canReach(focusOwner)) {
            return focusOwner;
        }
        return null;
    }

    /// Returns whether this field can reveal the supplied non-node accessibility target.
    boolean handlesAccessibleShowTarget(@Nullable Object parameter) {
        return false;
    }

    /// Returns whether a show-item request names a target this field can reasonably forward.
    private boolean canAttemptAccessibleShow(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return true;
        }
        for (Object parameter : parameters) {
            if (canAttemptAccessibleShowParameter(parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one show-item parameter can be routed to the popup picker.
    private boolean canAttemptAccessibleShowParameter(@Nullable Object parameter) {
        if (parameter instanceof Number) {
            return true;
        }
        if (parameter instanceof Node node) {
            return M3Accessible.isEffectivelyReachable(node);
        }
        if (handlesAccessibleShowTarget(parameter)) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (canAttemptAccessibleShowParameter(value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (canAttemptAccessibleShowParameter(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Shows the popup, forwards a show-item request to the picker, and focuses the requested item.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when the popup stayed visible and a picker target accepted focus
    final boolean showAccessibleItem(Object... parameters) {
        if (!M3Accessible.canReach(this) || !canAttemptAccessibleShow(parameters)) {
            return false;
        }
        boolean preservePopupFocus = isShowing() && parameters.length == 0 && popupFocusOwner() != null;
        showPicker();
        if (!isShowing()) {
            return false;
        }
        if (!preservePopupFocus) {
            forwardPickerAccessibleAction(AccessibleAction.SHOW_ITEM, parameters);
        }
        return focusPicker();
    }

    /// Forwards value-oriented accessibility actions to the concrete popup picker.
    private void forwardPickerAccessibleAction(AccessibleAction action, Object... parameters) {
        picker.executeAccessibleAction(action, parameters);
    }

    /// Focuses the preferred node inside the popup picker.
    private boolean focusPicker() {
        if (!isShowing()) {
            return false;
        }

        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            refreshPopupFocusNotifier();
            return true;
        }
        return false;
    }

    /// Requests focus for the current editor, open button, or popup focus target.
    ///
    /// @return `true` when the current target accepted focus
    final boolean focusAccessibleNode() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            refreshPopupFocusNotifier();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients and owner containers about the exposed focus target.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
    }

    /// Refreshes popup accessibility focus state after an explicit focus change.
    private void refreshPopupFocusNotifier() {
        @Nullable PickerPopup popup = pickerPopup;
        if (popup != null) {
            popup.focusNotifier.refresh();
        }
    }

    /// Clears generated parse or range errors after user edits.
    private void clearGeneratedErrorText() {
        String errorText = inputLayout.getErrorText();
        if (errorText.equals(getInvalidTextErrorText()) || errorText.equals(getRangeErrorText())) {
            inputLayout.setErrorText("");
        }
    }

    /// Synchronizes owner popup context and minimum-width state into the popup-hosted picker.
    private void preparePopupForShow(PickerPopup popup) {
        double fieldWidth = Math.max(0.0, inputLayout.getWidth());
        M3Css.setMinWidthIfUnbound(popup.root, Math.max(fieldWidth, popup.root.minWidth(-1.0)));
        popup.root.applyCss();
    }

    /// Applies initial visual state before the popup is shown.
    private void preparePopupForShowAnimation(PickerPopup popup) {
        popup.animation.stop();
        popup.hiding = false;
        popup.root.setOpacity(0.0);
        popup.root.setScaleX(POPUP_TRANSITION_SCALE);
        popup.root.setScaleY(POPUP_TRANSITION_SCALE);
        popup.root.setTranslateY(popup.transitionOffsetY);
    }

    /// Plays the popup picker enter animation.
    private void playShowAnimation(PickerPopup popup) {
        popup.animation.stop();
        popup.hiding = false;
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        popup.animation.configure(spec, 1.0, 1.0, 1.0, popup.root.getTranslateX(), 0.0);
        M3Animation.playFromStart(this, popup.animation);
    }

    /// Resets transient popup picker animation transforms.
    private void resetPopupAnimationState(PickerPopup popup) {
        popup.animation.stop();
        popup.hiding = false;
        popup.root.setOpacity(1.0);
        popup.root.setScaleX(1.0);
        popup.root.setScaleY(1.0);
        popup.root.setTranslateY(0.0);
    }

    /// Handles popup hidden cleanup and optional focus return.
    private void handlePopupHidden(PickerPopup popup) {
        popup.focusNotifier.stop();
        popup.reachabilityObserver.uninstall();
        popup.contextSynchronizer.stop();
        showing.set(false);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
        popup.focusNotifier.refresh();
        resetPopupAnimationState(popup);
        boolean restoreFocus = popup.focusEditorOnHidden;
        popup.focusEditorOnHidden = false;
        if (restoreFocus && M3Accessible.canReach(editor)) {
            M3Accessible.showItem(this, editor);
        }
    }

    /// Returns the lazily created popup presentation.
    private PickerPopup pickerPopup() {
        @Nullable PickerPopup popup = pickerPopup;
        if (popup == null) {
            popup = new PickerPopup();
            pickerPopup = popup;
        }
        return popup;
    }

    /// Owns the popup window and collaborators needed only while picker presentation is available.
    @NotNullByDefault
    private final class PickerPopup {
        /// The detached popup root that inherits owner styles and hosts the selected popup content.
        private final StackPane root = new StackPane();

        /// The native popup window used for picker presentation.
        private final Popup window = new Popup();

        /// Keeps the detached root synchronized with the owner scene and theme context while visible.
        private final M3PopupContextSynchronizer contextSynchronizer;

        /// Animates popup entry and exit without allocating a transition for each display.
        private final M3NodeTransition animation;

        /// Reports popup focus changes through the picker field's accessibility node.
        private final M3AccessibleFocusNotifier focusNotifier;

        /// Closes the popup when the picker field becomes unreachable.
        private final M3ReachabilityObserver reachabilityObserver;

        /// Whether focus should return to the editor after the popup hides.
        private boolean focusEditorOnHidden;

        /// Whether the current transition is closing the popup.
        private boolean hiding;

        /// The vertical offset used by the current popup hide transition.
        private double transitionOffsetY = -POPUP_TRANSITION_OFFSET_Y;

        /// Creates and connects one reusable picker popup presentation.
        private PickerPopup() {
            M3ControlStyles.add(root, POPUP_STYLE_CLASS);
            M3ControlStyles.add(root, popupStyleClass);
            root.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
            root.getChildren().setAll(popupContent);
            root.addEventHandler(KeyEvent.KEY_PRESSED, M3PickerField.this::handlePickerKeyPressed);

            window.setAutoHide(true);
            window.getContent().add(root);
            window.setOnHidden(event -> handlePopupHidden(this));

            contextSynchronizer = new M3PopupContextSynchronizer(
                    M3PickerField.this,
                    root,
                    M3Stylesheets.controlStylesheet("picker-field.css")
            );
            animation = new M3NodeTransition(root);
            animation.setOnFinished(event -> {
                if (hiding) {
                    window.hide();
                }
            });
            focusNotifier = new M3AccessibleFocusNotifier(
                    M3PickerField.this,
                    root,
                    M3PickerField.this::focusNode,
                    M3PickerField.this::notifyFocusNodeChanged
            );
            reachabilityObserver = new M3ReachabilityObserver(
                    M3PickerField.this,
                    M3PickerField.this::hidePopupIfOwnerUnreachable
            );
        }
    }

    /// Returns the user-agent stylesheet for M3FX picker fields.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("picker-field.css");
    }
}
