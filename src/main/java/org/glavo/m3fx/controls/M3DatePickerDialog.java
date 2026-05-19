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

/// A Material Design 3 dialog preset for selecting one date.
@NotNullByDefault
public class M3DatePickerDialog extends M3Dialog<LocalDate> {
    /// The default title and header text for date picker dialogs.
    public static final String DEFAULT_TITLE = "Select date";

    /// The date picker displayed as dialog content.
    private final M3DatePicker picker = new M3DatePicker();

    /// Creates an empty date picker dialog.
    public M3DatePickerDialog() {
        initialize();
    }

    /// Creates a date picker dialog initialized with the supplied selected date.
    public M3DatePickerDialog(@Nullable LocalDate value) {
        initialize();
        setValue(value);
    }

    /// Returns the date picker displayed by this dialog.
    public final M3DatePicker getPicker() {
        return picker;
    }

    /// Returns the selected date, or `null` when no date is selected.
    public final @Nullable LocalDate getValue() {
        return picker.getValue();
    }

    /// Sets the selected date, or clears selection when `null` is supplied.
    public final void setValue(@Nullable LocalDate value) {
        picker.setValue(value);
    }

    /// Returns the selected date property.
    public final ObjectProperty<@Nullable LocalDate> valueProperty() {
        return picker.valueProperty();
    }

    /// Clears the selected date.
    public final void clearValue() {
        picker.clearValue();
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

    /// Selects a date if it is inside the configured range.
    public final void selectDate(LocalDate date) {
        picker.selectDate(Objects.requireNonNull(date, "date"));
    }

    /// Selects today's date when it is inside the configured range.
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

    /// Shows the supplied month without changing the selected date.
    public final void showMonth(YearMonth month) {
        picker.showMonth(month);
    }

    /// Shows the month containing today's date without changing the selected date.
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
        picker.valueProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        updateOkButtonState();
    }

    /// Converts a dialog button into the selected date result.
    private @Nullable LocalDate convertResult(@Nullable ButtonType buttonType) {
        return buttonType == ButtonType.OK ? getValue() : null;
    }

    /// Enables the OK button only when a selected date exists.
    private void updateOkButtonState() {
        @Nullable Node okButton = getM3DialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(getValue() == null);
        }
    }
}
