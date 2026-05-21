// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3DateRangePickerFieldSkin;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.DayOfWeek;
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

    /// The selected start date, or `null` when the range is empty.
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

    /// The selected end date, or `null` while only a start date is selected.
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

    /// The formatter used to convert between editor text and picker dates.
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

    /// The error message shown when either editor text cannot be parsed.
    private final StringProperty invalidTextErrorText =
            new SimpleStringProperty(this, "invalidTextErrorText") {
                /// Keeps parse error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "invalidTextErrorText"));
                }
            };

    /// The error message shown when editor text parses outside the selectable range.
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
    private final ObservableList<M3DateRangePreset> presets = FXCollections.observableArrayList();

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

    /// Whether the popup picker is currently showing.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The picker popup enter animation.
    private final Timeline showAnimation = new Timeline();

    /// The picker popup exit animation.
    private final Timeline hideAnimation = new Timeline();

    /// Whether both field endpoints are currently being assigned through [setRange].
    private boolean applyingRange;

    /// Whether field state is currently being pushed into the popup picker.
    private boolean synchronizingPicker;

    /// Whether popup picker state is currently being copied into the field.
    private boolean synchronizingFromPicker;

    /// Whether picker range synchronization has been queued for the next FX pulse.
    private boolean pickerSyncScheduled;

    /// Whether editor text is currently being rewritten from selected dates.
    private boolean updatingEditorText;

    /// Whether focus should return to the start editor after the popup hides.
    private boolean focusStartEditorOnHidden;

    /// The vertical offset used by the current popup hide animation.
    private double popupTransitionOffsetY = -POPUP_TRANSITION_OFFSET_Y;

    /// Creates an empty date range picker field.
    public M3DateRangePickerField() {
        this(null, null);
    }

    /// Creates a date range picker field initialized with the supplied selected range.
    public M3DateRangePickerField(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        formatter.set(DateTimeFormatter.ISO_LOCAL_DATE);
        invalidTextErrorText.set("Enter a valid date");
        rangeErrorText.set("Date range is outside the selectable range");
        initialize();
        setRange(startDate, endDate);
    }

    /// Returns the selected start date, or `null` when the range is empty.
    public @Nullable LocalDate getStartDate() {
        return startDate.get();
    }

    /// Sets the selected start date, or clears the range start when `null` is supplied.
    public void setStartDate(@Nullable LocalDate startDate) {
        this.startDate.set(startDate);
    }

    /// Returns the start date property.
    public ObjectProperty<@Nullable LocalDate> startDateProperty() {
        return startDate;
    }

    /// Returns the selected end date, or `null` while only a start date is selected.
    public @Nullable LocalDate getEndDate() {
        return endDate.get();
    }

    /// Sets the selected end date, or clears the range end when `null` is supplied.
    public void setEndDate(@Nullable LocalDate endDate) {
        this.endDate.set(endDate);
    }

    /// Returns the end date property.
    public ObjectProperty<@Nullable LocalDate> endDateProperty() {
        return endDate;
    }

    /// Sets both range endpoints atomically after validating ordering and selectable bounds.
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
    public void setRange(M3DateRange range) {
        M3DateRange validatedRange = Objects.requireNonNull(range, "range");
        setRange(validatedRange.startDate(), validatedRange.endDate());
    }

    /// Clears both selected range endpoints.
    public void clearRange() {
        setRange(null, null);
    }

    /// Returns whether both range endpoints are selected.
    public boolean isRangeComplete() {
        return getStartDate() != null && getEndDate() != null;
    }

    /// Returns the selected range, or `null` when the range is incomplete.
    public @Nullable M3DateRange getRange() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        return start == null || end == null ? null : new M3DateRange(start, end);
    }

    /// Applies a date range preset, updates the editors, and closes the popup when it is showing.
    public void applyPreset(M3DateRangePreset preset) {
        M3DateRange range = Objects.requireNonNull(preset, "preset").range();
        setRange(range);
        picker.showMonth(YearMonth.from(range.startDate()));
        if (popup.isShowing()) {
            hidePicker(true);
        }
    }

    /// Returns the editable text field shown for the start date.
    public M3TextField getStartEditor() {
        return startEditor;
    }

    /// Returns the editable text field shown for the end date.
    public M3TextField getEndEditor() {
        return endEditor;
    }

    /// Returns the Material text input layout used by the start date editor.
    public M3TextInputLayout getStartInputLayout() {
        return startInputLayout;
    }

    /// Returns the Material text input layout used by the end date editor.
    public M3TextInputLayout getEndInputLayout() {
        return endInputLayout;
    }

    /// Returns the popup date range picker control.
    public M3DateRangePicker getPicker() {
        return picker;
    }

    /// Returns the mutable date range preset list rendered in the popup.
    public ObservableList<M3DateRangePreset> getPresets() {
        return presets;
    }

    /// Adds one date range preset to the popup.
    public void addPreset(M3DateRangePreset preset) {
        presets.add(Objects.requireNonNull(preset, "preset"));
    }

    /// Adds date range presets after validating the preset array.
    public void addPresets(M3DateRangePreset... presets) {
        validatePresets(presets);
        this.presets.addAll(presets);
    }

    /// Replaces all date range presets.
    public void setPresets(M3DateRangePreset... presets) {
        validatePresets(presets);
        this.presets.setAll(presets);
    }

    /// Replaces all date range presets with the default common range set.
    public void setCommonPresets(LocalDate anchorDate) {
        presets.setAll(M3DateRangePresets.common(anchorDate, getFirstDayOfWeek()));
    }

    /// Removes all date range presets from the popup.
    public void clearPresets() {
        presets.clear();
    }

    /// Returns the formatter used for editor text.
    public DateTimeFormatter getFormatter() {
        return formatter.get();
    }

    /// Sets the formatter used for editor text.
    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter.set(formatter);
    }

    /// Returns the editor text formatter property.
    public ObjectProperty<DateTimeFormatter> formatterProperty() {
        return formatter;
    }

    /// Returns the parse error message used when editor text is invalid.
    public String getInvalidTextErrorText() {
        return invalidTextErrorText.get();
    }

    /// Sets the parse error message used when editor text is invalid.
    public void setInvalidTextErrorText(String invalidTextErrorText) {
        this.invalidTextErrorText.set(invalidTextErrorText);
    }

    /// Returns the parse error message property.
    public StringProperty invalidTextErrorTextProperty() {
        return invalidTextErrorText;
    }

    /// Returns the range error message used when editor text is outside the selectable range.
    public String getRangeErrorText() {
        return rangeErrorText.get();
    }

    /// Sets the range error message used when editor text is outside the selectable range.
    public void setRangeErrorText(String rangeErrorText) {
        this.rangeErrorText.set(rangeErrorText);
    }

    /// Returns the range error message property.
    public StringProperty rangeErrorTextProperty() {
        return rangeErrorText;
    }

    /// Returns the label text displayed by the start date input layout.
    public String getStartLabelText() {
        return startInputLayout.getLabelText();
    }

    /// Sets the label text displayed by the start date input layout.
    public void setStartLabelText(String startLabelText) {
        startInputLayout.setLabelText(startLabelText);
    }

    /// Returns the start label text property.
    public StringProperty startLabelTextProperty() {
        return startInputLayout.labelTextProperty();
    }

    /// Returns the label text displayed by the end date input layout.
    public String getEndLabelText() {
        return endInputLayout.getLabelText();
    }

    /// Sets the label text displayed by the end date input layout.
    public void setEndLabelText(String endLabelText) {
        endInputLayout.setLabelText(endLabelText);
    }

    /// Returns the end label text property.
    public StringProperty endLabelTextProperty() {
        return endInputLayout.labelTextProperty();
    }

    /// Returns the supporting text displayed by the start date input layout.
    public String getStartSupportingText() {
        return startInputLayout.getSupportingText();
    }

    /// Sets the supporting text displayed by the start date input layout.
    public void setStartSupportingText(String supportingText) {
        startInputLayout.setSupportingText(supportingText);
    }

    /// Returns the start supporting text property.
    public StringProperty startSupportingTextProperty() {
        return startInputLayout.supportingTextProperty();
    }

    /// Returns the supporting text displayed by the end date input layout.
    public String getEndSupportingText() {
        return endInputLayout.getSupportingText();
    }

    /// Sets the supporting text displayed by the end date input layout.
    public void setEndSupportingText(String supportingText) {
        endInputLayout.setSupportingText(supportingText);
    }

    /// Returns the end supporting text property.
    public StringProperty endSupportingTextProperty() {
        return endInputLayout.supportingTextProperty();
    }

    /// Returns whether the picker popup is currently showing.
    public boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only popup showing property.
    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Returns the month currently displayed by the popup calendar grid.
    public YearMonth getDisplayedMonth() {
        return picker.getDisplayedMonth();
    }

    /// Sets the month displayed by the popup calendar grid.
    public void setDisplayedMonth(YearMonth displayedMonth) {
        picker.setDisplayedMonth(displayedMonth);
    }

    /// Returns the displayed month property from the popup picker.
    public ObjectProperty<YearMonth> displayedMonthProperty() {
        return picker.displayedMonthProperty();
    }

    /// Returns the weekday shown in the first popup calendar column.
    public DayOfWeek getFirstDayOfWeek() {
        return picker.getFirstDayOfWeek();
    }

    /// Sets the weekday shown in the first popup calendar column.
    public void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        picker.setFirstDayOfWeek(firstDayOfWeek);
    }

    /// Returns the first day of week property from the popup picker.
    public ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return picker.firstDayOfWeekProperty();
    }

    /// Returns the earliest selectable date, or `null` when there is no lower bound.
    public @Nullable LocalDate getMinDate() {
        return picker.getMinDate();
    }

    /// Sets the earliest selectable date, or clears the lower bound when `null` is supplied.
    public void setMinDate(@Nullable LocalDate minDate) {
        picker.setMinDate(minDate);
        clearRangeIfOutOfBounds();
    }

    /// Returns the minimum date property from the popup picker.
    public ObjectProperty<@Nullable LocalDate> minDateProperty() {
        return picker.minDateProperty();
    }

    /// Returns the latest selectable date, or `null` when there is no upper bound.
    public @Nullable LocalDate getMaxDate() {
        return picker.getMaxDate();
    }

    /// Sets the latest selectable date, or clears the upper bound when `null` is supplied.
    public void setMaxDate(@Nullable LocalDate maxDate) {
        picker.setMaxDate(maxDate);
        clearRangeIfOutOfBounds();
    }

    /// Returns the maximum date property from the popup picker.
    public ObjectProperty<@Nullable LocalDate> maxDateProperty() {
        return picker.maxDateProperty();
    }

    /// Returns whether adjacent-month days are visible in popup leading and trailing grid cells.
    public boolean isShowAdjacentMonthDays() {
        return picker.isShowAdjacentMonthDays();
    }

    /// Sets whether adjacent-month days are visible in popup leading and trailing grid cells.
    public void setShowAdjacentMonthDays(boolean showAdjacentMonthDays) {
        picker.setShowAdjacentMonthDays(showAdjacentMonthDays);
    }

    /// Returns the adjacent-month visibility property from the popup picker.
    public BooleanProperty showAdjacentMonthDaysProperty() {
        return picker.showAdjacentMonthDaysProperty();
    }

    /// Parses both editor texts, updates selected endpoints, and returns whether the text is valid.
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
            setRangeErrors();
            return false;
        }
        if (parsedStart != null && picker.isDateDisabled(parsedStart)
                || parsedEnd != null && picker.isDateDisabled(parsedEnd)) {
            setRangeErrors();
            return false;
        }

        setRange(parsedStart, parsedEnd);
        clearGeneratedErrorText();
        return true;
    }

    /// Shows the picker popup when this field is attached to a window.
    public void showPicker() {
        if (isDisabled() || popup.isShowing()) {
            return;
        }

        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null) {
            return;
        }

        preparePopupForShow(scene);
        @Nullable M3PopupPositioning.Placement placement =
                M3PopupPositioning.menuBelowOrAbove(this, popupContent, POPUP_OFFSET_Y);
        if (placement == null) {
            return;
        }

        popupTransitionOffsetY = placement.opensAbove() ? POPUP_TRANSITION_OFFSET_Y : -POPUP_TRANSITION_OFFSET_Y;
        preparePopupForShowAnimation();
        popup.show(this, placement.x(), placement.y());
        showing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
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
        switch (action) {
            case REQUEST_FOCUS -> startEditor.requestFocus();
            case SHOW_MENU, EXPAND -> showPicker();
            case COLLAPSE -> hidePicker(true);
            case SHOW_ITEM -> showPickerAndForwardAccessibleAction(action, parameters);
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
        startInputLayout.setLabelText("Start date");
        endInputLayout.setLabelText("End date");
        startInputLayout.setTrailing(startOpenButton);
        endInputLayout.setTrailing(endOpenButton);
        startInputLayout.disableProperty().bind(disabledProperty());
        endInputLayout.disableProperty().bind(disabledProperty());

        popupContent.getStyleClass().add(M3PickerField.POPUP_STYLE_CLASS);
        popupContent.getStyleClass().add(POPUP_STYLE_CLASS);
        popupContent.getChildren().setAll(picker);
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.setAlignment(Pos.TOP_LEFT);
        popup.setAutoHide(true);
        popup.getContent().add(popupContent);
        popup.setOnHidden(event -> handlePopupHidden());

        startOpenButton.setOnAction(event -> togglePicker());
        endOpenButton.setOnAction(event -> togglePicker());
        startEditor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        endEditor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        startEditor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        endEditor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        picker.addEventHandler(KeyEvent.KEY_PRESSED, this::handlePickerKeyPressed);
        startEditor.focusedProperty().addListener((observable, oldValue, focused) -> handleEditorFocusChanged());
        endEditor.focusedProperty().addListener((observable, oldValue, focused) -> handleEditorFocusChanged());
        startEditor.textProperty().addListener((observable, oldValue, newValue) -> handleEditorTextChanged());
        endEditor.textProperty().addListener((observable, oldValue, newValue) -> handleEditorTextChanged());
        picker.startDateProperty().addListener(observable -> schedulePickerRangeSync());
        picker.endDateProperty().addListener(observable -> schedulePickerRangeSync());
        picker.minDateProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
        picker.maxDateProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
        presets.addListener(presetsListener);
    }

    /// Creates one popup open button.
    private static M3IconButton createOpenButton(String accessibleText) {
        M3IconButton button = M3IconButton.withIcon("v", M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT);
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
        M3Button button = M3Button.withVariant(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        button.setMaxWidth(Double.MAX_VALUE);
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

        pickerSyncScheduled = true;
        try {
            Platform.runLater(() -> {
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
            synchronizingPicker = true;
            try {
                picker.setRange(getStartDate(), getEndDate());
            } finally {
                synchronizingPicker = false;
            }
        }

        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
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

    /// Clears selected endpoints when current bounds exclude either endpoint.
    private void clearRangeIfOutOfBounds() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start != null && picker.isDateDisabled(start) || end != null && picker.isDateDisabled(end)) {
            clearRange();
        }
    }

    /// Applies range error text to the editors that currently contain dates.
    private void setRangeErrors() {
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
            @Nullable Object focusNode = picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
            return focusNode instanceof Node node ? node : picker;
        }
        return endEditor.isFocused() ? endEditor : startEditor;
    }

    /// Shows the popup when possible, forwards an accessibility action to the picker, and focuses its item.
    private void showPickerAndForwardAccessibleAction(AccessibleAction action, Object... parameters) {
        showPicker();
        forwardPickerAccessibleAction(action, false, parameters);
        focusPicker();
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
    private void focusPicker() {
        if (popup.isShowing()) {
            focusNode().requestFocus();
        }
    }

    /// Copies scene styles and theme declarations into the popup-hosted picker.
    private void preparePopupForShow(Scene scene) {
        popupContent.getStylesheets().setAll(scene.getStylesheets());
        String fieldStylesheet = M3Stylesheets.controlStylesheet("picker-field.css");
        if (!popupContent.getStylesheets().contains(fieldStylesheet)) {
            popupContent.getStylesheets().add(fieldStylesheet);
        }

        Parent root = scene.getRoot();
        M3ThemeManager.copyThemeContext(root, popupContent);
        M3Animation.copyResolvedMotionSettings(this, popupContent);
        double fieldWidth = Math.max(0.0, getWidth());
        popupContent.setMinWidth(Math.max(fieldWidth, popupContent.minWidth(-1.0)));
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

    /// Hides the popup picker and optionally restores editor focus.
    private void hidePicker(boolean focusStartEditor) {
        if (!popup.isShowing()) {
            return;
        }

        focusStartEditorOnHidden = focusStartEditor;
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

    /// Handles popup hidden cleanup and optional focus return.
    private void handlePopupHidden() {
        showing.set(false);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        resetPopupAnimationState();
        if (focusStartEditorOnHidden) {
            focusStartEditorOnHidden = false;
            startEditor.requestFocus();
        }
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

    /// Validates a date range preset array.
    private static void validatePresets(M3DateRangePreset... presets) {
        Objects.requireNonNull(presets, "presets");
        for (M3DateRangePreset preset : presets) {
            Objects.requireNonNull(preset, "preset");
        }
    }
}
