// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/// A Material Design 3 dialog preset for selecting an inclusive date range.
@NotNullByDefault
public class M3DateRangePickerDialog extends M3Dialog<M3DateRange> {
    /// The default title and header text for date range picker dialogs.
    public static final String DEFAULT_TITLE = "Select date range";

    /// The date range picker displayed as dialog content.
    private final M3DateRangePicker picker = new M3DateRangePicker();

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
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        return start == null || end == null ? null : new M3DateRange(start, end);
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
        pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(this::convertResult);
        picker.startDateProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.endDateProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        updateOkButtonState();
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
}
