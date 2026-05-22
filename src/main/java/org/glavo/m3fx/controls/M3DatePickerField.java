// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.skins.M3PickerFieldSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/// A Material Design 3 date picker field that combines an editable text field with a popup calendar.
///
/// `M3DatePickerField` lets users type a date or choose one from an embedded [M3DatePicker]. It exposes
/// nullable selected-date state, parsing and formatting behavior, popup visibility, and optional preset actions
/// so date entry can be used inline in forms.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DatePickerField extends M3PickerField<LocalDate, M3DatePicker> {
    /// The style class applied to date picker field controls.
    public static final String STYLE_CLASS = "m3-date-picker-field";

    /// The style class applied to date picker field popup surfaces.
    public static final String POPUP_STYLE_CLASS = "m3-date-picker-field-popup";

    /// The style class applied to popup content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-picker-field-preset-content";

    /// The style class applied to the popup preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-date-picker-field-preset-list";

    /// The style class applied to each popup preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-picker-field-preset-button";

    /// The mutable preset list rendered before the popup picker.
    private final ObservableList<M3DatePreset> presets = FXCollections.observableArrayList();

    /// The wrapper used when the popup renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3DatePreset> presetsListener = change -> updatePresetContent();

    /// Creates an empty date picker field.
    public M3DatePickerField() {
        this(new M3DatePicker());
    }

    /// Creates a date picker field initialized with the supplied value.
    ///
    /// @param value the initially selected date
    public M3DatePickerField(LocalDate value) {
        this(new M3DatePicker());
        setValue(value);
    }

    /// Creates a date picker field around a fresh popup date picker.
    private M3DatePickerField(M3DatePicker picker) {
        super(
                picker,
                picker.valueProperty(),
                DateTimeFormatter.ISO_LOCAL_DATE,
                STYLE_CLASS,
                POPUP_STYLE_CLASS,
                "v",
                "Open date picker",
                "Enter a valid date",
                "Date is outside the selectable range"
        );
        initializePresetContent();
    }

    /// Returns the mutable date preset list rendered in the popup.
    ///
    /// @return the mutable date preset list rendered in the popup
    public ObservableList<M3DatePreset> getPresets() {
        return presets;
    }

    /// Adds one date preset to the popup.
    ///
    /// @param preset the date preset to add
    public void addPreset(M3DatePreset preset) {
        presets.add(Objects.requireNonNull(preset, "preset"));
    }

    /// Adds date presets after validating the preset array.
    ///
    /// @param presets the date presets to add
    public void addPresets(M3DatePreset... presets) {
        validatePresets(presets);
        this.presets.addAll(presets);
    }

    /// Replaces all date presets.
    ///
    /// @param presets the replacement date presets
    public void setPresets(M3DatePreset... presets) {
        validatePresets(presets);
        this.presets.setAll(presets);
    }

    /// Replaces all date presets with the default common date set.
    ///
    /// @param anchorDate the date used to compute relative common presets
    public void setCommonPresets(LocalDate anchorDate) {
        presets.setAll(M3DatePresets.common(anchorDate));
    }

    /// Removes all date presets from the popup.
    public void clearPresets() {
        presets.clear();
    }

    /// Returns the month currently displayed by the popup calendar grid.
    ///
    /// @return the month currently displayed by the popup calendar grid
    public YearMonth getDisplayedMonth() {
        return getPicker().getDisplayedMonth();
    }

    /// Sets the month displayed by the popup calendar grid.
    ///
    /// @param displayedMonth the month displayed by the popup calendar grid
    public void setDisplayedMonth(YearMonth displayedMonth) {
        getPicker().setDisplayedMonth(displayedMonth);
    }

    /// Returns the displayed month property from the popup date picker.
    ///
    /// @return the displayed month property from the popup date picker
    public ObjectProperty<YearMonth> displayedMonthProperty() {
        return getPicker().displayedMonthProperty();
    }

    /// Returns the weekday shown in the first popup calendar column.
    ///
    /// @return the weekday shown in the first popup calendar column
    public DayOfWeek getFirstDayOfWeek() {
        return getPicker().getFirstDayOfWeek();
    }

    /// Sets the weekday shown in the first popup calendar column.
    ///
    /// @param firstDayOfWeek the weekday shown in the first popup calendar column
    public void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        getPicker().setFirstDayOfWeek(firstDayOfWeek);
    }

    /// Returns the first day of week property from the popup date picker.
    ///
    /// @return the first day of week property from the popup date picker
    public ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return getPicker().firstDayOfWeekProperty();
    }

    /// Returns the earliest selectable date, or `null` when there is no lower bound.
    ///
    /// @return the earliest selectable date, or `null` when there is no lower bound
    public @Nullable LocalDate getMinDate() {
        return getPicker().getMinDate();
    }

    /// Sets the earliest selectable date, or clears the lower bound when `null` is supplied.
    ///
    /// @param minDate the earliest selectable date, or `null` to clear the lower bound
    public void setMinDate(@Nullable LocalDate minDate) {
        getPicker().setMinDate(minDate);
    }

    /// Returns the minimum date property from the popup date picker.
    ///
    /// @return the minimum date property from the popup date picker
    public ObjectProperty<@Nullable LocalDate> minDateProperty() {
        return getPicker().minDateProperty();
    }

    /// Returns the latest selectable date, or `null` when there is no upper bound.
    ///
    /// @return the latest selectable date, or `null` when there is no upper bound
    public @Nullable LocalDate getMaxDate() {
        return getPicker().getMaxDate();
    }

    /// Sets the latest selectable date, or clears the upper bound when `null` is supplied.
    ///
    /// @param maxDate the latest selectable date, or `null` to clear the upper bound
    public void setMaxDate(@Nullable LocalDate maxDate) {
        getPicker().setMaxDate(maxDate);
    }

    /// Returns the maximum date property from the popup date picker.
    ///
    /// @return the maximum date property from the popup date picker
    public ObjectProperty<@Nullable LocalDate> maxDateProperty() {
        return getPicker().maxDateProperty();
    }

    /// Returns whether adjacent-month days are visible in popup leading and trailing grid cells.
    ///
    /// @return `true` when adjacent-month days are visible
    public boolean isShowAdjacentMonthDays() {
        return getPicker().isShowAdjacentMonthDays();
    }

    /// Sets whether adjacent-month days are visible in popup leading and trailing grid cells.
    ///
    /// @param showAdjacentMonthDays whether adjacent-month days should be visible
    public void setShowAdjacentMonthDays(boolean showAdjacentMonthDays) {
        getPicker().setShowAdjacentMonthDays(showAdjacentMonthDays);
    }

    /// Returns the adjacent-month visibility property from the popup date picker.
    ///
    /// @return the adjacent-month visibility property from the popup date picker
    public BooleanProperty showAdjacentMonthDaysProperty() {
        return getPicker().showAdjacentMonthDaysProperty();
    }

    /// Selects a date if it is inside the configured range.
    ///
    /// @param date the date to select
    public void selectDate(LocalDate date) {
        setValue(Objects.requireNonNull(date, "date"));
    }

    /// Selects today's date when it is inside the configured range.
    public void selectToday() {
        selectDate(LocalDate.now());
    }

    /// Applies a date preset, updates the editor, and closes the popup when it is showing.
    ///
    /// @param preset the date preset to apply
    public void applyPreset(M3DatePreset preset) {
        LocalDate date = Objects.requireNonNull(preset, "preset").date();
        setValue(date);
        getPicker().showMonth(YearMonth.from(date));
        if (isShowing()) {
            hidePicker();
        }
    }

    /// Clears the selected date.
    public void clearValue() {
        setValue(null);
    }

    /// Shows the month before the current displayed month.
    public void showPreviousMonth() {
        getPicker().showPreviousMonth();
    }

    /// Shows the month after the current displayed month.
    public void showNextMonth() {
        getPicker().showNextMonth();
    }

    /// Shows the supplied month without changing the selected date.
    ///
    /// @param month the month to display
    public void showMonth(YearMonth month) {
        getPicker().showMonth(month);
    }

    /// Shows the month containing today's date without changing the selected date.
    public void showToday() {
        getPicker().showToday();
    }

    /// Returns whether the supplied date is outside the configured selectable range.
    ///
    /// @param date the date to test
    /// @return `true` when the date is outside the configured selectable range
    public boolean isDateDisabled(LocalDate date) {
        return getPicker().isDateDisabled(date);
    }

    /// Creates the default Material Design 3 picker field skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3PickerFieldSkin<>(this);
    }

    /// Parses one editor date string.
    @Override
    protected LocalDate parseValue(String text, DateTimeFormatter formatter) {
        return LocalDate.from(formatter.parse(text));
    }

    /// Formats one date value for editor display.
    @Override
    protected String formatValue(LocalDate value, DateTimeFormatter formatter) {
        return formatter.format(value);
    }

    /// Dates already use the popup picker's precision.
    @Override
    protected LocalDate normalizeValue(LocalDate value) {
        return Objects.requireNonNull(value, "value");
    }

    /// Returns whether a date is outside the popup picker's selectable range.
    @Override
    protected boolean isPickerValueDisabled(LocalDate value) {
        return getPicker().isDateDisabled(value);
    }

    /// Applies a value to the popup date picker.
    @Override
    protected void setPickerValue(@Nullable LocalDate value) {
        getPicker().setValue(value);
    }

    /// Configures popup preset containers and listeners.
    private void initializePresetContent() {
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetList.setAlignment(Pos.TOP_LEFT);
        getPicker().minDateProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
        getPicker().maxDateProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
        presets.addListener(presetsListener);
    }

    /// Rebuilds popup content from the current preset list.
    private void updatePresetContent() {
        presetContent.getChildren().clear();
        presetList.getChildren().clear();

        if (presets.isEmpty()) {
            resetPopupContent();
            return;
        }

        for (M3DatePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, getPicker());
        setPopupContent(presetContent);
    }

    /// Creates one popup preset action button.
    private M3Button createPresetButton(M3DatePreset preset) {
        M3Button button = M3Button.withVariant(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setDisable(getPicker().isDateDisabled(preset.date()));
        button.setOnAction(event -> {
            applyPreset(preset);
            event.consume();
        });
        return button;
    }

    /// Validates a date preset array.
    private static void validatePresets(M3DatePreset... presets) {
        Objects.requireNonNull(presets, "presets");
        for (M3DatePreset preset : presets) {
            Objects.requireNonNull(preset, "preset");
        }
    }
}
