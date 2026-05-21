// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/// A Material Design 3 dialog preset for selecting an inclusive date range.
///
/// The dialog installs an [M3DateRangePicker] as its content, wires OK and cancel actions, and keeps the
/// selected [M3DateRange] as the dialog result when the user accepts the range.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public class M3DateRangePickerDialog extends M3Dialog<M3DateRange> {
    /// The default title and header text for date range picker dialogs.
    public static final String DEFAULT_TITLE = "Select date range";

    /// The style class applied to dialog content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-range-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-date-range-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-range-picker-dialog-preset-button";

    /// The date range picker displayed as dialog content.
    private final M3DateRangePicker picker = new M3DateRangePicker();

    /// The mutable preset list rendered before the picker.
    private final ObservableList<M3DateRangePreset> presets = FXCollections.observableArrayList();

    /// The wrapper used when the dialog renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3DateRangePreset> presetsListener = change -> updatePresetContent();

    /// Creates an empty date range picker dialog.
    public M3DateRangePickerDialog() {
        initialize();
    }

    /// Creates a date range picker dialog initialized with the supplied range.
    public M3DateRangePickerDialog(M3DateRange range) {
        this(range.startDate(), range.endDate());
    }

    /// Creates a date range picker dialog initialized with the supplied endpoints.
    public M3DateRangePickerDialog(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        initialize();
        setRange(startDate, endDate);
    }

    /// Returns the date range picker displayed by this dialog.
    public final M3DateRangePicker getPicker() {
        return picker;
    }

    /// Returns the mutable date range preset list.
    public final ObservableList<M3DateRangePreset> getPresets() {
        return presets;
    }

    /// Adds one date range preset.
    public final void addPreset(M3DateRangePreset preset) {
        presets.add(Objects.requireNonNull(preset, "preset"));
    }

    /// Adds date range presets after validating the preset array.
    public final void addPresets(M3DateRangePreset... presets) {
        validatePresets(presets);
        this.presets.addAll(presets);
    }

    /// Replaces all date range presets.
    public final void setPresets(M3DateRangePreset... presets) {
        validatePresets(presets);
        this.presets.setAll(presets);
    }

    /// Replaces all date range presets with the default common range set.
    public final void setCommonPresets(LocalDate anchorDate) {
        presets.setAll(M3DateRangePresets.common(anchorDate, getFirstDayOfWeek()));
    }

    /// Removes all date range presets.
    public final void clearPresets() {
        presets.clear();
    }

    /// Returns the first selected date, or `null` when no range start is selected.
    public final @Nullable LocalDate getStartDate() {
        return picker.getStartDate();
    }

    /// Sets the first selected date, or clears the range start when `null` is supplied.
    public final void setStartDate(@Nullable LocalDate startDate) {
        picker.setStartDate(startDate);
    }

    /// Returns the range start property.
    public final ObjectProperty<@Nullable LocalDate> startDateProperty() {
        return picker.startDateProperty();
    }

    /// Returns the last selected date, or `null` while only a range start is selected.
    public final @Nullable LocalDate getEndDate() {
        return picker.getEndDate();
    }

    /// Sets the last selected date, or clears the range end when `null` is supplied.
    public final void setEndDate(@Nullable LocalDate endDate) {
        picker.setEndDate(endDate);
    }

    /// Returns the range end property.
    public final ObjectProperty<@Nullable LocalDate> endDateProperty() {
        return picker.endDateProperty();
    }

    /// Sets both range endpoints atomically after validating ordering and selectable bounds.
    public final void setRange(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        picker.setRange(startDate, endDate);
    }

    /// Clears both range endpoints.
    public final void clearRange() {
        picker.clearRange();
    }

    /// Returns whether both range endpoints are selected.
    public final boolean isRangeComplete() {
        return picker.isRangeComplete();
    }

    /// Returns the selected range, or `null` when the range is incomplete.
    public final @Nullable M3DateRange getRange() {
        return picker.getRange();
    }

    /// Returns whether the supplied date is inside the selected inclusive range.
    public final boolean isDateInSelectedRange(LocalDate date) {
        return picker.isDateInSelectedRange(date);
    }

    /// Returns the month currently displayed by the picker.
    public final YearMonth getDisplayedMonth() {
        return picker.getDisplayedMonth();
    }

    /// Sets the month displayed by the picker.
    public final void setDisplayedMonth(YearMonth displayedMonth) {
        picker.setDisplayedMonth(displayedMonth);
    }

    /// Returns the displayed month property from the picker.
    public final ObjectProperty<YearMonth> displayedMonthProperty() {
        return picker.displayedMonthProperty();
    }

    /// Returns the weekday shown in the first picker column.
    public final DayOfWeek getFirstDayOfWeek() {
        return picker.getFirstDayOfWeek();
    }

    /// Sets the weekday shown in the first picker column.
    public final void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        picker.setFirstDayOfWeek(firstDayOfWeek);
    }

    /// Returns the first day of week property from the picker.
    public final ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return picker.firstDayOfWeekProperty();
    }

    /// Returns the earliest selectable date, or `null` when there is no lower bound.
    public final @Nullable LocalDate getMinDate() {
        return picker.getMinDate();
    }

    /// Sets the earliest selectable date, or clears the lower bound when `null` is supplied.
    public final void setMinDate(@Nullable LocalDate minDate) {
        picker.setMinDate(minDate);
    }

    /// Returns the minimum date property from the picker.
    public final ObjectProperty<@Nullable LocalDate> minDateProperty() {
        return picker.minDateProperty();
    }

    /// Returns the latest selectable date, or `null` when there is no upper bound.
    public final @Nullable LocalDate getMaxDate() {
        return picker.getMaxDate();
    }

    /// Sets the latest selectable date, or clears the upper bound when `null` is supplied.
    public final void setMaxDate(@Nullable LocalDate maxDate) {
        picker.setMaxDate(maxDate);
    }

    /// Returns the maximum date property from the picker.
    public final ObjectProperty<@Nullable LocalDate> maxDateProperty() {
        return picker.maxDateProperty();
    }

    /// Returns whether adjacent-month days are visible in leading and trailing picker cells.
    public final boolean isShowAdjacentMonthDays() {
        return picker.isShowAdjacentMonthDays();
    }

    /// Sets whether adjacent-month days are visible in leading and trailing picker cells.
    public final void setShowAdjacentMonthDays(boolean showAdjacentMonthDays) {
        picker.setShowAdjacentMonthDays(showAdjacentMonthDays);
    }

    /// Returns the adjacent-month visibility property from the picker.
    public final BooleanProperty showAdjacentMonthDaysProperty() {
        return picker.showAdjacentMonthDaysProperty();
    }

    /// Selects a date as the next range endpoint.
    public final void selectDate(LocalDate date) {
        picker.selectDate(Objects.requireNonNull(date, "date"));
    }

    /// Selects today's date as the next range endpoint when it is inside the configured range.
    public final void selectToday() {
        picker.selectToday();
    }

    /// Applies a date range preset and leaves the dialog open for confirmation.
    public final void applyPreset(M3DateRangePreset preset) {
        picker.applyPreset(Objects.requireNonNull(preset, "preset"));
        updateOkButtonState();
    }

    /// Shows the month before the current displayed month.
    public final void showPreviousMonth() {
        picker.showPreviousMonth();
    }

    /// Shows the month after the current displayed month.
    public final void showNextMonth() {
        picker.showNextMonth();
    }

    /// Shows the supplied month without changing the selected range.
    public final void showMonth(YearMonth month) {
        picker.showMonth(month);
    }

    /// Shows the month containing today's date without changing the selected range.
    public final void showToday() {
        picker.showToday();
    }

    /// Returns whether the supplied date is outside the configured selectable range.
    public final boolean isDateDisabled(LocalDate date) {
        return picker.isDateDisabled(date);
    }

    /// Configures dialog content, buttons, result conversion, and button state.
    @SuppressWarnings("DataFlowIssue")
    private void initialize() {
        setTitle(DEFAULT_TITLE);
        M3DialogPane pane = getM3DialogPane();
        pane.setHeaderText(DEFAULT_TITLE);
        pane.setContent(picker);
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.setAlignment(Pos.TOP_LEFT);
        pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(this::convertResult);
        picker.startDateProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.endDateProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        presets.addListener(presetsListener);
        updateOkButtonState();
    }

    /// Rebuilds dialog content from the current preset list.
    private void updatePresetContent() {
        M3DialogPane pane = getM3DialogPane();
        presetContent.getChildren().clear();
        presetList.getChildren().clear();
        pane.setContent(null);

        if (presets.isEmpty()) {
            pane.setContent(picker);
            return;
        }

        for (M3DateRangePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, picker);
        pane.setContent(presetContent);
    }

    /// Creates one preset action button.
    private M3Button createPresetButton(M3DateRangePreset preset) {
        M3Button button = M3Button.withVariant(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> applyPreset(preset));
        return button;
    }

    /// Converts a dialog button into the selected date range result.
    private @Nullable M3DateRange convertResult(@Nullable ButtonType buttonType) {
        return buttonType == ButtonType.OK ? getRange() : null;
    }

    /// Enables the OK button only when both range endpoints are selected.
    private void updateOkButtonState() {
        @Nullable Node okButton = getM3DialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(!isRangeComplete());
        }
    }

    /// Validates a date range preset array.
    private static void validatePresets(M3DateRangePreset... presets) {
        Objects.requireNonNull(presets, "presets");
        for (M3DateRangePreset preset : presets) {
            Objects.requireNonNull(preset, "preset");
        }
    }
}
