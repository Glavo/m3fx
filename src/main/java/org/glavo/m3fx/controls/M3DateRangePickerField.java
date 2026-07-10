// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.glavo.m3fx.skins.M3DateRangePickerFieldSkin;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 date range picker field with editable start and end date inputs.
///
/// `M3DateRangePickerField` pairs two text inputs with an [M3DateRangePicker] popup so users can type or choose
/// an inclusive range. It exposes nullable range state, parsing and formatting behavior, popup visibility, and
/// optional preset actions for inline form use.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DateRangePickerField extends javafx.scene.control.Control {
    /// The base style class for M3FX date range picker fields.
    public static final String STYLE_CLASS = "m3-date-range-picker-field";

    /// The style class applied to the internal two-field container.
    public static final String CONTAINER_STYLE_CLASS = "m3-date-range-picker-field-container";

    /// The style class applied to date range picker field popup surfaces.
    public static final String POPUP_STYLE_CLASS = "m3-date-range-picker-field-popup";

    /// The style class applied to popup content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-range-picker-field-preset-content";

    /// The style class applied to the popup preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-date-range-picker-field-preset-list";

    /// The style class applied to each popup preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-range-picker-field-preset-button";

    /// The style class applied to the trailing picker open buttons.
    public static final String OPEN_BUTTON_STYLE_CLASS = M3PickerField.OPEN_BUTTON_STYLE_CLASS;

    /// The vertical gap between the field and popup picker.
    private static final double POPUP_OFFSET_Y = 8.0;

    /// The initial popup picker scale used for enter and exit motion.
    private static final double POPUP_TRANSITION_SCALE = 0.96;

    /// The initial popup picker offset used for enter and exit motion.
    private static final double POPUP_TRANSITION_OFFSET_Y = 6.0;

    // Internal storage for [startDateProperty].
    private final ObjectProperty<@Nullable LocalDate> startDate =
            new SimpleObjectProperty<>(this, "startDate") {
                /// Validates direct property writes before applying them.
                @Override
                public void set(@Nullable LocalDate newValue) {
                    if (!applyingRange) {
                        validateDate(newValue);
                        validateDateRange(newValue, getEndDate());
                    }
                    super.set(newValue);
                }

                /// Synchronizes editors and popup selection after the start date changes.
                @Override
                protected void invalidated() {
                    if (!applyingRange && get() == null && getEndDate() != null) {
                        endDate.set(null);
                    }
                    if (!applyingRange) {
                        handleFieldRangeChanged();
                    }
                }
            };

    // Internal storage for [endDateProperty].
    private final ObjectProperty<@Nullable LocalDate> endDate =
            new SimpleObjectProperty<>(this, "endDate") {
                /// Validates direct property writes before applying them.
                @Override
                public void set(@Nullable LocalDate newValue) {
                    if (!applyingRange) {
                        if (newValue != null && getStartDate() == null) {
                            throw new IllegalArgumentException("startDate must be selected before endDate");
                        }
                        validateDate(newValue);
                        validateDateRange(getStartDate(), newValue);
                    }
                    super.set(newValue);
                }

                /// Synchronizes editors and popup selection after the end date changes.
                @Override
                protected void invalidated() {
                    if (!applyingRange) {
                        handleFieldRangeChanged();
                    }
                }
            };

    // Internal storage for [formatterProperty].
    private final ObjectProperty<DateTimeFormatter> formatter =
            new SimpleObjectProperty<>(this, "formatter") {
                /// Keeps formatter values non-null.
                @Override
                public void set(DateTimeFormatter newValue) {
                    super.set(Objects.requireNonNull(newValue, "formatter"));
                }

                /// Rewrites editor text when the formatter changes.
                @Override
                protected void invalidated() {
                    updateEditorsFromRange();
                }
            };

    // Internal storage for [invalidTextErrorTextProperty].
    private final StringProperty invalidTextErrorText =
            new SimpleStringProperty(this, "invalidTextErrorText") {
                /// Keeps parse error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "invalidTextErrorText"));
                }
            };

    // Internal storage for [rangeErrorTextProperty].
    private final StringProperty rangeErrorText =
            new SimpleStringProperty(this, "rangeErrorText") {
                /// Keeps range error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "rangeErrorText"));
                }
            };

    /// The editable text field used for the start date.
    private final M3TextField startEditor = new M3TextField();

    /// The editable text field used for the end date.
    private final M3TextField endEditor = new M3TextField();

    /// The Material text input layout wrapping the start editor.
    private final M3TextInputLayout startInputLayout = new M3TextInputLayout(startEditor);

    /// The Material text input layout wrapping the end editor.
    private final M3TextInputLayout endInputLayout = new M3TextInputLayout(endEditor);

    /// The popup date range picker.
    private final M3DateRangePicker picker = new M3DateRangePicker();

    /// The mutable preset list rendered before the popup picker.
    private final ObservableList<M3DateRangePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The wrapper used when the popup renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3DateRangePreset> presetsListener = change -> updatePresetContent();

    /// The trailing button that opens the popup from the start editor.
    private final M3IconButton startOpenButton = createOpenButton("Open start date range picker");

    /// The trailing button that opens the popup from the end editor.
    private final M3IconButton endOpenButton = createOpenButton("Open end date range picker");

    /// The popup root that inherits scene styles and hosts the picker.
    private final StackPane popupContent = new StackPane();

    /// The popup window used for picker display.
    private final Popup popup = new Popup();

    /// Keeps the detached date-range picker popup synchronized with the owner scene and theme context while visible.
    private final M3PopupContextSynchronizer popupContextSynchronizer =
            new M3PopupContextSynchronizer(this, popupContent, M3Stylesheets.controlStylesheet("picker-field.css"));

    // Internal storage for [showingProperty].
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The picker popup enter animation.
    private final M3NodeTransition showAnimation = new M3NodeTransition(popupContent);

    /// The picker popup exit animation.
    private final M3NodeTransition hideAnimation = new M3NodeTransition(popupContent);

    /// Observes runtime motion settings while this field is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Reports popup picker focus changes through this field's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, popupContent, this::focusNode, this::notifyFocusNodeChanged);

    /// Closes the popup when this field or one of its ancestors becomes unreachable.
    private final M3ReachabilityObserver reachabilityObserver =
            new M3ReachabilityObserver(this, this::hidePopupIfOwnerUnreachable);

    /// Whether both field endpoints are currently being assigned through [setRange].
    private boolean applyingRange;

    /// Whether field state is currently being pushed into the popup picker.
    private boolean synchronizingPicker;

    /// Whether popup picker state is currently being copied into the field.
    private boolean synchronizingFromPicker;

    /// Whether picker range synchronization has been queued for the next FX pulse.
    private boolean pickerSyncScheduled;

    /// The generation used to ignore stale queued picker range synchronization.
    private int pickerSyncGeneration;

    /// Whether editor text is currently being rewritten from selected dates.
    private boolean updatingEditorText;

    /// The editor that opened the current popup session.
    private @Nullable M3TextField popupOwnerEditor;

    /// The editor that should receive focus after the popup hides.
    private @Nullable M3TextField focusEditorOnHidden;

    /// The vertical offset used by the current popup hide animation.
    private double popupTransitionOffsetY = -POPUP_TRANSITION_OFFSET_Y;

    /// Creates an empty date range picker field.
    public M3DateRangePickerField() {
        this(null, null);
    }

    /// Creates a date range picker field initialized with the supplied selected range.
    ///
    /// @param startDate the first selected date, or `null` for no selected range
    /// @param endDate the last selected date, or `null` for an incomplete range
    public M3DateRangePickerField(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        formatter.set(DateTimeFormatter.ISO_LOCAL_DATE);
        invalidTextErrorText.set("Enter a valid date");
        rangeErrorText.set("Date range is outside the selectable range");
        initialize();
        setRange(startDate, endDate);
    }

    /// Returns the selected start date, or `null` when the range is empty.
    ///
    /// @return the selected start date, or `null` when the range is empty
    public @Nullable LocalDate getStartDate() {
        return startDate.get();
    }

    /// Sets the selected start date, or clears the range start when `null` is supplied.
    ///
    /// @param startDate the selected start date, or `null` to clear the range
    public void setStartDate(@Nullable LocalDate startDate) {
        this.startDate.set(startDate);
    }

    /// Returns the start date property.
    ///
    /// @return the start date property
    public ObjectProperty<@Nullable LocalDate> startDateProperty() {
        return startDate;
    }

    /// Returns the selected end date, or `null` while only a start date is selected.
    ///
    /// @return the selected end date, or `null` while only a start date is selected
    public @Nullable LocalDate getEndDate() {
        return endDate.get();
    }

    /// Sets the selected end date, or clears the range end when `null` is supplied.
    ///
    /// @param endDate the selected end date, or `null` to clear the range end
    public void setEndDate(@Nullable LocalDate endDate) {
        this.endDate.set(endDate);
    }

    /// Returns the end date property.
    ///
    /// @return the end date property
    public ObjectProperty<@Nullable LocalDate> endDateProperty() {
        return endDate;
    }

    /// Sets both range endpoints atomically after validating ordering and selectable bounds.
    ///
    /// @param startDate the first selected date, or `null` to clear the range
    /// @param endDate the last selected date, or `null` for an incomplete range
    public void setRange(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        validateDate(startDate);
        validateDate(endDate);
        validateDateRange(startDate, endDate);
        applyingRange = true;
        try {
            this.startDate.set(startDate);
            this.endDate.set(endDate);
        } finally {
            applyingRange = false;
        }
        handleFieldRangeChanged();
    }

    /// Sets both range endpoints from the supplied inclusive range.
    ///
    /// @param range the inclusive date range to select
    public void setRange(M3DateRange range) {
        M3DateRange validatedRange = Objects.requireNonNull(range, "range");
        setRange(validatedRange.startDate(), validatedRange.endDate());
    }

    /// Clears both selected range endpoints.
    public void clearRange() {
        setRange(null, null);
    }

    /// Returns whether both range endpoints are selected.
    ///
    /// @return `true` when both range endpoints are selected
    public boolean isRangeComplete() {
        return getStartDate() != null && getEndDate() != null;
    }

    /// Returns the selected range, or `null` when the range is incomplete.
    ///
    /// @return the selected range, or `null` when the range is incomplete
    public @Nullable M3DateRange getRange() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        return start == null || end == null ? null : new M3DateRange(start, end);
    }

    /// Applies a date range preset, updates the editors, and closes the popup when it is showing.
    ///
    /// @param preset the date range preset to apply
    private void applyPreset(M3DateRangePreset preset) {
        M3DateRange range = Objects.requireNonNull(preset, "preset").range();
        setRange(range);
        picker.showMonth(YearMonth.from(range.startDate()));
        if (popup.isShowing()) {
            hidePicker(true);
        }
    }

    /// Returns the current raw start-date editor text.
    ///
    /// This text may be temporarily invalid while the user is editing. Call [commitEditorText] to parse it into
    /// [startDateProperty].
    ///
    /// @return the current raw start-date editor text
    public String getStartText() {
        return startEditor.getText();
    }

    /// Sets the raw start-date editor text.
    ///
    /// The value is not parsed until [commitEditorText] is called or the editor action commits it.
    ///
    /// @param startText the raw start-date editor text
    public void setStartText(String startText) {
        startEditor.setText(Objects.requireNonNull(startText, "startText"));
    }

    /// Returns the raw start-date editor text property.
    ///
    /// @return the raw start-date editor text property
    public StringProperty startTextProperty() {
        return startEditor.textProperty();
    }

    /// Returns the current raw end-date editor text.
    ///
    /// This text may be temporarily invalid while the user is editing. Call [commitEditorText] to parse it into
    /// [endDateProperty].
    ///
    /// @return the current raw end-date editor text
    public String getEndText() {
        return endEditor.getText();
    }

    /// Sets the raw end-date editor text.
    ///
    /// The value is not parsed until [commitEditorText] is called or the editor action commits it.
    ///
    /// @param endText the raw end-date editor text
    public void setEndText(String endText) {
        endEditor.setText(Objects.requireNonNull(endText, "endText"));
    }

    /// Returns the raw end-date editor text property.
    ///
    /// @return the raw end-date editor text property
    public StringProperty endTextProperty() {
        return endEditor.textProperty();
    }

    /// Returns the text input variant used by the start-date editor.
    ///
    /// @return the text input variant used by the start-date editor
    public M3TextInputVariant getStartVariant() {
        return startEditor.getVariant();
    }

    /// Sets the text input variant used by the start-date editor.
    ///
    /// @param variant the text input variant used by the start-date editor
    public void setStartVariant(M3TextInputVariant variant) {
        startEditor.setVariant(variant);
    }

    /// Returns the start-date editor variant property.
    ///
    /// @return the start-date editor variant property
    public ObjectProperty<M3TextInputVariant> startVariantProperty() {
        return startEditor.variantProperty();
    }

    /// Returns the text input variant used by the end-date editor.
    ///
    /// @return the text input variant used by the end-date editor
    public M3TextInputVariant getEndVariant() {
        return endEditor.getVariant();
    }

    /// Sets the text input variant used by the end-date editor.
    ///
    /// @param variant the text input variant used by the end-date editor
    public void setEndVariant(M3TextInputVariant variant) {
        endEditor.setVariant(variant);
    }

    /// Returns the end-date editor variant property.
    ///
    /// @return the end-date editor variant property
    public ObjectProperty<M3TextInputVariant> endVariantProperty() {
        return endEditor.variantProperty();
    }

    /// Returns the current error text shown for the start-date editor.
    ///
    /// @return the current error text shown for the start-date editor
    public String getStartErrorText() {
        return startInputLayout.getErrorText();
    }

    /// Sets the current error text shown for the start-date editor.
    ///
    /// @param errorText the current error text shown for the start-date editor
    public void setStartErrorText(String errorText) {
        startInputLayout.setErrorText(errorText);
    }

    /// Returns the start-date editor error text property.
    ///
    /// @return the start-date editor error text property
    public StringProperty startErrorTextProperty() {
        return startInputLayout.errorTextProperty();
    }

    /// Returns the current error text shown for the end-date editor.
    ///
    /// @return the current error text shown for the end-date editor
    public String getEndErrorText() {
        return endInputLayout.getErrorText();
    }

    /// Sets the current error text shown for the end-date editor.
    ///
    /// @param errorText the current error text shown for the end-date editor
    public void setEndErrorText(String errorText) {
        endInputLayout.setErrorText(errorText);
    }

    /// Returns the end-date editor error text property.
    ///
    /// @return the end-date editor error text property
    public StringProperty endErrorTextProperty() {
        return endInputLayout.errorTextProperty();
    }

    /// Returns the editable text field shown for the start date.
    ///
    /// @return the editable text field shown for the start date
    M3TextField getStartEditor() {
        return startEditor;
    }

    /// Returns the editable text field shown for the end date.
    ///
    /// @return the editable text field shown for the end date
    M3TextField getEndEditor() {
        return endEditor;
    }

    /// Returns the Material text input layout used by the start date editor.
    ///
    /// @return the Material text input layout used by the start date editor
    M3TextInputLayout getStartInputLayout() {
        return startInputLayout;
    }

    /// Returns the Material text input layout used by the end date editor.
    ///
    /// @return the Material text input layout used by the end date editor
    M3TextInputLayout getEndInputLayout() {
        return endInputLayout;
    }

    /// Returns the popup date range picker control.
    ///
    /// @return the popup date range picker control
    public M3DateRangePicker getPicker() {
        return picker;
    }

    /// Returns the mutable date range preset list rendered in the popup.
    ///
    /// @return the mutable date range preset list rendered in the popup
    public ObservableList<M3DateRangePreset> getPresets() {
        return presets;
    }

    /// Returns the formatter used for editor text.
    ///
    /// @return the formatter used for editor text
    public DateTimeFormatter getFormatter() {
        return formatter.get();
    }

    /// Sets the formatter used for editor text.
    ///
    /// @param formatter the formatter used for editor text
    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter.set(formatter);
    }

    /// Returns the editor text formatter property.
    ///
    /// @return the editor text formatter property
    public ObjectProperty<DateTimeFormatter> formatterProperty() {
        return formatter;
    }

    /// Returns the parse error message used when editor text is invalid.
    ///
    /// @return the parse error message used when editor text is invalid
    public String getInvalidTextErrorText() {
        return invalidTextErrorText.get();
    }

    /// Sets the parse error message used when editor text is invalid.
    ///
    /// @param invalidTextErrorText the parse error message used when editor text is invalid
    public void setInvalidTextErrorText(String invalidTextErrorText) {
        this.invalidTextErrorText.set(invalidTextErrorText);
    }

    /// Returns the parse error message property.
    ///
    /// @return the parse error message property
    public StringProperty invalidTextErrorTextProperty() {
        return invalidTextErrorText;
    }

    /// Returns the range error message used when editor text is outside the selectable range.
    ///
    /// @return the range error message used when editor text is outside the selectable range
    public String getRangeErrorText() {
        return rangeErrorText.get();
    }

    /// Sets the range error message used when editor text is outside the selectable range.
    ///
    /// @param rangeErrorText the range error message used when editor text is outside the selectable range
    public void setRangeErrorText(String rangeErrorText) {
        this.rangeErrorText.set(rangeErrorText);
    }

    /// Returns the range error message property.
    ///
    /// @return the range error message property
    public StringProperty rangeErrorTextProperty() {
        return rangeErrorText;
    }

    /// Returns the label text displayed by the start date input layout.
    ///
    /// @return the label text displayed by the start date input layout
    public String getStartLabelText() {
        return startInputLayout.getLabelText();
    }

    /// Sets the label text displayed by the start date input layout.
    ///
    /// @param startLabelText the label text displayed by the start date input layout
    public void setStartLabelText(String startLabelText) {
        startInputLayout.setLabelText(startLabelText);
    }

    /// Returns the start label text property.
    ///
    /// @return the start label text property
    public StringProperty startLabelTextProperty() {
        return startInputLayout.labelTextProperty();
    }

    /// Returns the label text displayed by the end date input layout.
    ///
    /// @return the label text displayed by the end date input layout
    public String getEndLabelText() {
        return endInputLayout.getLabelText();
    }

    /// Sets the label text displayed by the end date input layout.
    ///
    /// @param endLabelText the label text displayed by the end date input layout
    public void setEndLabelText(String endLabelText) {
        endInputLayout.setLabelText(endLabelText);
    }

    /// Returns the end label text property.
    ///
    /// @return the end label text property
    public StringProperty endLabelTextProperty() {
        return endInputLayout.labelTextProperty();
    }

    /// Returns the supporting text displayed by the start date input layout.
    ///
    /// @return the supporting text displayed by the start date input layout
    public String getStartSupportingText() {
        return startInputLayout.getSupportingText();
    }

    /// Sets the supporting text displayed by the start date input layout.
    ///
    /// @param supportingText the supporting text displayed by the start date input layout
    public void setStartSupportingText(String supportingText) {
        startInputLayout.setSupportingText(supportingText);
    }

    /// Returns the start supporting text property.
    ///
    /// @return the start supporting text property
    public StringProperty startSupportingTextProperty() {
        return startInputLayout.supportingTextProperty();
    }

    /// Returns the supporting text displayed by the end date input layout.
    ///
    /// @return the supporting text displayed by the end date input layout
    public String getEndSupportingText() {
        return endInputLayout.getSupportingText();
    }

    /// Sets the supporting text displayed by the end date input layout.
    ///
    /// @param supportingText the supporting text displayed by the end date input layout
    public void setEndSupportingText(String supportingText) {
        endInputLayout.setSupportingText(supportingText);
    }

    /// Returns the end supporting text property.
    ///
    /// @return the end supporting text property
    public StringProperty endSupportingTextProperty() {
        return endInputLayout.supportingTextProperty();
    }

    /// Returns whether the picker popup is currently showing.
    ///
    /// @return `true` when the picker popup is currently showing
    public boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only popup showing property.
    ///
    /// @return the read-only popup showing property
    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Parses both editor texts, updates selected endpoints, and returns whether the text is valid.
    ///
    /// @return `true` when both editor texts can be committed as a valid range
    public boolean commitEditorText() {
        clearGeneratedErrorText();
        @Nullable LocalDate parsedStart = parseEditorDate(startEditor, startInputLayout);
        @Nullable LocalDate parsedEnd = parseEditorDate(endEditor, endInputLayout);
        if (!startInputLayout.getErrorText().isEmpty() || !endInputLayout.getErrorText().isEmpty()) {
            return false;
        }
        if (parsedStart == null && parsedEnd != null) {
            startInputLayout.setErrorText(getInvalidTextErrorText());
            return false;
        }
        if (parsedStart != null && parsedEnd != null && parsedStart.isAfter(parsedEnd)) {
            updateRangeErrors();
            return false;
        }
        if (parsedStart != null && picker.isDateDisabled(parsedStart)
                || parsedEnd != null && picker.isDateDisabled(parsedEnd)) {
            updateRangeErrors();
            return false;
        }

        setRange(parsedStart, parsedEnd);
        clearGeneratedErrorText();
        return true;
    }

    /// Shows the picker popup when this field is attached to a window.
    public void showPicker() {
        if (!M3Accessible.canReach(this) || popup.isShowing()) {
            return;
        }

        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null) {
            return;
        }

        if (popupOwnerEditor == null) {
            popupOwnerEditor = currentEditor();
        }
        popupContextSynchronizer.start();
        preparePopupForShow();
        @Nullable M3PopupPositioning.Placement placement =
                M3PopupPositioning.menuBelowOrAbove(this, popupContent, POPUP_OFFSET_Y);
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
    public void hidePicker() {
        hidePicker(false);
    }

    /// Toggles the picker popup.
    public void togglePicker() {
        if (popup.isShowing()) {
            hidePicker();
        } else {
            showPicker();
        }
    }

    /// Returns the user-agent stylesheet for M3FX picker fields.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("picker-field.css");
    }

    /// Returns accessibility attributes for the embedded editors and popup picker.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case FOCUS_NODE -> focusNode();
            case ITEM_AT_INDEX -> accessibleItem(parameters);
            case SELECTED_ITEMS -> selectedItems();
            case SUBMENU -> picker;
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes editor and popup accessibility actions.
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
                    forwardPickerAccessibleAction(action, true, parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 date range picker field skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DateRangePickerFieldSkin(this);
    }

    /// Adds base style classes and installs event handling.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem,
                this::handlesAccessibleShowTarget);
        startInputLayout.setLabelText("Start date");
        endInputLayout.setLabelText("End date");
        startInputLayout.setTrailing(startOpenButton);
        endInputLayout.setTrailing(endOpenButton);
        startInputLayout.disableProperty().bind(disabledProperty());
        endInputLayout.disableProperty().bind(disabledProperty());
        startInputLayout.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        endInputLayout.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        picker.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        popupContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());

        M3ControlStyles.add(popupContent, M3PickerField.POPUP_STYLE_CLASS);
        M3ControlStyles.add(popupContent, POPUP_STYLE_CLASS);
        popupContent.getChildren().setAll(picker);
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetContent.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(this));
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetList.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(this));
        M3PresetNavigation.install(presetList, this, this::focusPickerContent);
        popup.setAutoHide(true);
        popup.getContent().add(popupContent);
        hideAnimation.setOnFinished(event -> popup.hide());
        popup.setOnHidden(event -> handlePopupHidden());

        startOpenButton.setOnAction(event -> {
            popupOwnerEditor = startEditor;
            togglePicker();
        });
        endOpenButton.setOnAction(event -> {
            popupOwnerEditor = endEditor;
            togglePicker();
        });
        startEditor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        endEditor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        startEditor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        endEditor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        popupContent.addEventHandler(KeyEvent.KEY_PRESSED, this::handlePickerKeyPressed);
        startEditor.focusedProperty().addListener((observable, oldValue, focused) -> handleEditorFocusChanged());
        endEditor.focusedProperty().addListener((observable, oldValue, focused) -> handleEditorFocusChanged());
        startEditor.textProperty().addListener((observable, oldValue, newValue) -> handleEditorTextChanged());
        endEditor.textProperty().addListener((observable, oldValue, newValue) -> handleEditorTextChanged());
        picker.startDateProperty().addListener(observable -> schedulePickerRangeSync());
        picker.endDateProperty().addListener(observable -> schedulePickerRangeSync());
        picker.minDateProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        picker.maxDateProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        presets.addListener(presetsListener);
        popupFocusNotifier.start();
        reachabilityObserver.install();
    }

    /// Hides the popup if its owner field can no longer be reached from its scene.
    private void hidePopupIfOwnerUnreachable() {
        if (popup.isShowing() && !M3Accessible.canReach(this)) {
            hidePicker(false);
        }
    }

    /// Creates one popup open button.
    private static M3IconButton createOpenButton(String accessibleText) {
        M3IconButton button = new M3IconButton(new M3InternalIcon(
                M3InternalIcon.Glyph.CALENDAR,
                M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
        ));
        M3ControlStyles.add(button, OPEN_BUTTON_STYLE_CLASS);
        button.setAccessibleText(accessibleText);
        return button;
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
                popupOwnerEditor = event.getSource() == endEditor ? endEditor : startEditor;
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

    /// Rebuilds popup content from the current preset list.
    private void updatePresetContent() {
        popupContent.getChildren().clear();
        presetContent.getChildren().clear();
        presetList.getChildren().clear();

        if (presets.isEmpty()) {
            popupContent.getChildren().setAll(picker);
            return;
        }

        for (M3DateRangePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, picker);
        popupContent.getChildren().setAll(presetContent);
    }

    /// Creates one popup preset action button.
    private M3Button createPresetButton(M3DateRangePreset preset) {
        M3Button button = new M3Button(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        M3Css.setMaxWidthIfUnbound(button, Double.MAX_VALUE);
        button.setDisable(isPresetDisabled(preset));
        button.setOnAction(event -> {
            applyPreset(preset);
            event.consume();
        });
        return button;
    }

    /// Returns whether a preset cannot be selected with the current date bounds.
    private boolean isPresetDisabled(M3DateRangePreset preset) {
        M3DateRange range = preset.range();
        return picker.isDateDisabled(range.startDate()) || picker.isDateDisabled(range.endDate());
    }

    /// Commits editor text after focus leaves both range editors.
    private void handleEditorFocusChanged() {
        if (!startEditor.isFocused() && !endEditor.isFocused() && !popup.isShowing()) {
            commitEditorText();
        }
        notifyFocusNodeChanged();
    }

    /// Clears generated error text after user edits.
    private void handleEditorTextChanged() {
        if (!updatingEditorText) {
            clearGeneratedErrorText();
        }
    }

    /// Schedules synchronization after the picker finishes its atomic range mutation.
    private void schedulePickerRangeSync() {
        if (synchronizingPicker || pickerSyncScheduled) {
            return;
        }

        int generation = ++pickerSyncGeneration;
        pickerSyncScheduled = true;
        try {
            Platform.runLater(() -> {
                if (generation != pickerSyncGeneration) {
                    return;
                }
                pickerSyncScheduled = false;
                if (!synchronizingPicker) {
                    syncRangeFromPicker();
                }
            });
        } catch (IllegalStateException ignored) {
            pickerSyncScheduled = false;
            syncRangeFromPicker();
        }
    }

    /// Synchronizes field state from the popup picker.
    private void syncRangeFromPicker() {
        pickerSyncGeneration++;
        pickerSyncScheduled = false;
        boolean completeRange = picker.isRangeComplete();
        synchronizingFromPicker = true;
        try {
            setRange(picker.getStartDate(), picker.getEndDate());
        } finally {
            synchronizingFromPicker = false;
        }

        if (completeRange && popup.isShowing()) {
            hidePicker(true);
        }
    }

    /// Synchronizes dependent state after either field endpoint changes.
    private void handleFieldRangeChanged() {
        updateEditorsFromRange();
        clearGeneratedErrorText();

        if (!synchronizingFromPicker) {
            pickerSyncGeneration++;
            pickerSyncScheduled = false;
            synchronizingPicker = true;
            try {
                picker.setRange(getStartDate(), getEndDate());
            } finally {
                synchronizingPicker = false;
            }
        }

        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        M3Accessible.notifyFocusNodeChanged(this);
    }

    /// Updates editor text from the current selected range.
    private void updateEditorsFromRange() {
        updatingEditorText = true;
        try {
            startEditor.setText(formatNullableDate(getStartDate()));
            endEditor.setText(formatNullableDate(getEndDate()));
        } finally {
            updatingEditorText = false;
        }
    }

    /// Formats one nullable date value.
    private String formatNullableDate(@Nullable LocalDate date) {
        return date == null ? "" : getFormatter().format(date);
    }

    /// Parses one editor text value and updates parse error state.
    private @Nullable LocalDate parseEditorDate(M3TextField editor, M3TextInputLayout inputLayout) {
        String text = editor.getText() == null ? "" : editor.getText().trim();
        if (text.isEmpty()) {
            inputLayout.setErrorText("");
            return null;
        }

        try {
            inputLayout.setErrorText("");
            return LocalDate.from(getFormatter().parse(text));
        } catch (DateTimeException | IllegalArgumentException e) {
            inputLayout.setErrorText(getInvalidTextErrorText());
            return null;
        }
    }

    /// Validates that one date is selectable.
    private void validateDate(@Nullable LocalDate date) {
        if (date != null && picker.isDateDisabled(date)) {
            throw new IllegalArgumentException("date is outside the selectable range");
        }
    }

    /// Validates optional inclusive selected endpoints.
    private static void validateDateRange(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        if (startDate == null && endDate != null) {
            throw new IllegalArgumentException("startDate must be selected before endDate");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }

    /// Refreshes preset state and clears the selected range when picker bounds exclude it.
    private void handleSelectableBoundsChanged() {
        updatePresetContent();
        clearRangeIfOutOfBounds();
    }

    /// Clears selected endpoints when current bounds exclude either endpoint.
    private void clearRangeIfOutOfBounds() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start != null && picker.isDateDisabled(start) || end != null && picker.isDateDisabled(end)) {
            clearRange();
        }
    }

    /// Applies range error text to the editors that currently contain dates.
    private void updateRangeErrors() {
        startInputLayout.setErrorText(getRangeErrorText());
        if (!endEditor.getText().isBlank()) {
            endInputLayout.setErrorText(getRangeErrorText());
        }
    }

    /// Clears generated parse or range errors.
    private void clearGeneratedErrorText() {
        clearGeneratedErrorText(startInputLayout);
        clearGeneratedErrorText(endInputLayout);
    }

    /// Clears generated parse or range errors from one layout.
    private void clearGeneratedErrorText(M3TextInputLayout inputLayout) {
        String errorText = inputLayout.getErrorText();
        if (errorText.equals(getInvalidTextErrorText()) || errorText.equals(getRangeErrorText())) {
            inputLayout.setErrorText("");
        }
    }

    /// Returns the field selection as an immutable accessibility list.
    private List<LocalDate> selectedItems() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start == null) {
            return List.of();
        }
        return end == null ? List.of(start) : List.of(start, end);
    }

    /// Returns one indexed editor layout for accessibility clients and skins.
    ///
    /// @param parameters the accessibility index parameters
    /// @return the indexed editor layout, or `null` when the parameters do not address a child
    private @Nullable Node accessibleItem(Object... parameters) {
        if (parameters.length != 1 || !(parameters[0] instanceof Integer index)) {
            return null;
        }
        return switch (index) {
            case 0 -> startInputLayout;
            case 1 -> endInputLayout;
            default -> null;
        };
    }

    /// Returns combined editor text for accessibility clients.
    private String accessibleText() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start == null) {
            return "";
        }
        return end == null ? start.toString() : start + "/" + end;
    }

    /// Returns the current keyboard focus node for accessibility clients.
    private Node focusNode() {
        if (popup.isShowing()) {
            @Nullable Node popupFocusOwner = popupFocusOwner();
            if (popupFocusOwner != null) {
                return popupFocusOwner;
            }

            @Nullable Object focusNode = picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
            return focusNode instanceof Node node && M3Accessible.canReach(node) ? node : picker;
        }
        return fieldFocusNode();
    }

    /// Returns the current focus target inside the closed range field.
    private Node fieldFocusNode() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner != null) {
            if (M3Accessible.containsNode(startEditor, focusOwner)) {
                return startEditor;
            }
            if (M3Accessible.containsNode(startOpenButton, focusOwner)) {
                return startOpenButton;
            }
            if (M3Accessible.containsNode(endEditor, focusOwner)) {
                return endEditor;
            }
            if (M3Accessible.containsNode(endOpenButton, focusOwner)) {
                return endOpenButton;
            }
        }
        return startEditor;
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

    /// Returns whether this field can reveal the supplied accessibility date target.
    private boolean handlesAccessibleShowTarget(@Nullable Object parameter) {
        return parameter instanceof LocalDate date && !picker.isDateDisabled(date);
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
        return showPickerAndForwardAccessibleAction(AccessibleAction.SHOW_ITEM, parameters);
    }

    /// Shows the popup when possible, forwards an accessibility action to the picker, and focuses its item.
    private boolean showPickerAndForwardAccessibleAction(AccessibleAction action, Object... parameters) {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (action == AccessibleAction.SHOW_ITEM && !canAttemptAccessibleShow(parameters)) {
            return false;
        }
        boolean preservePopupFocus = popup.isShowing() && parameters.length == 0 && popupFocusOwner() != null;
        showPicker();
        if (!popup.isShowing()) {
            return false;
        }
        if (!preservePopupFocus) {
            forwardPickerAccessibleAction(action, false, parameters);
        }
        return focusPicker();
    }

    /// Forwards an accessibility action to the popup range picker and optionally syncs edited endpoints.
    private void forwardPickerAccessibleAction(
            AccessibleAction action,
            boolean syncRange,
            Object... parameters
    ) {
        picker.executeAccessibleAction(action, parameters);
        if (syncRange && !synchronizingPicker) {
            syncRangeFromPicker();
        }
    }

    /// Focuses the preferred node inside the popup picker.
    private boolean focusPicker() {
        if (!popup.isShowing()) {
            return false;
        }

        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
            return true;
        }
        return false;
    }

    /// Focuses the picker content directly instead of preserving an already focused preset action.
    private boolean focusPickerContent() {
        if (!popup.isShowing()) {
            return false;
        }

        @Nullable Object focusNode = picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        boolean focused;
        if (focusNode instanceof Node node && M3Accessible.canReach(node)) {
            focused = M3Accessible.showItem(this, node);
        } else {
            focused = M3Accessible.showItem(this, picker);
        }
        if (focused) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
            return true;
        }
        return false;
    }

    /// Requests focus for the field's current editor or popup focus target.
    ///
    /// @return `true` when the current target accepted focus
    final boolean focusAccessibleNode() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
            return true;
        }
        return false;
    }

    /// Returns the editor currently focused by the user, or the start editor by default.
    private M3TextField currentEditor() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner != null
                && (M3Accessible.containsNode(endEditor, focusOwner)
                || M3Accessible.containsNode(endOpenButton, focusOwner))) {
            return endEditor;
        }
        return startEditor;
    }

    /// Notifies accessibility clients and owner containers about the exposed focus target.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
    }

    /// Synchronizes owner popup context and minimum-width state into the popup-hosted picker.
    private void preparePopupForShow() {
        popupContextSynchronizer.sync();
        double fieldWidth = Math.max(0.0, getWidth());
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
        showAnimation.configure(spec, 1.0, 1.0, 1.0, popupContent.getTranslateX(), 0.0);
        M3Animation.playFromStart(this, showAnimation);
    }

    /// Hides the popup picker and optionally restores editor focus.
    private void hidePicker(boolean focusEditor) {
        if (!popup.isShowing()) {
            return;
        }

        focusEditorOnHidden = focusEditor ? (popupOwnerEditor == null ? currentEditor() : popupOwnerEditor) : null;
        showAnimation.stop();
        if (hideAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hideAnimation.configure(
                spec,
                0.0,
                POPUP_TRANSITION_SCALE,
                POPUP_TRANSITION_SCALE,
                popupContent.getTranslateX(),
                popupTransitionOffsetY
        );
        M3Animation.playFromStart(this, hideAnimation);
    }

    /// Handles popup hidden cleanup and optional focus return.
    private void handlePopupHidden() {
        popupContextSynchronizer.stop();
        showing.set(false);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        resetPopupAnimationState();
        @Nullable M3TextField editor = focusEditorOnHidden;
        focusEditorOnHidden = null;
        popupOwnerEditor = null;
        if (M3Accessible.canReach(editor)) {
            M3Accessible.showItem(this, editor);
        }
        notifyFocusNodeChanged();
        popupFocusNotifier.refresh();
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
        M3Animation.finishRunningAnimationsIfDisabled(this, showAnimation, hideAnimation);
    }

}
