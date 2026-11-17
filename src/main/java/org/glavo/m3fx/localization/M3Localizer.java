// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.localization;

import javafx.scene.Node;
import org.glavo.m3fx.internal.M3LocalizerObserver;
import org.jetbrains.annotations.NotNullByDefault;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;

/// Supplies default text and regional formatting for M3FX controls.
///
/// The base implementation supplies English messages and uses the specified locale for calendar names and date
/// formatting. Subclasses may override individual methods or delegate them to an application's translation system.
/// Each method returns final text, not a message pattern. Results must be non-null and stable for the same inputs
/// throughout the lifetime of this object. Methods are called synchronously and must not perform blocking I/O.
///
/// Install an instance on a node with [#install(Node, M3Localizer)]. That node and its descendants use the instance
/// until a nearer installation overrides it. Replacing the installed instance updates existing localized controls;
/// mutating external resources used by an instance does not notify controls. Explicit control text and formatter
/// settings take precedence over localized defaults. Localization does not change node orientation.
///
/// Without an installation, controls use English messages and the default format locale captured when the fallback
/// localizer is first needed. Installing a localizer on a detached subtree is supported. Access to live nodes follows
/// the JavaFX Application Thread rules. See [Material Design](https://m3.material.io/) for component terminology.
@NotNullByDefault
public class M3Localizer {
    /// The immutable locale used by the default regional formatting methods.
    private final Locale locale;

    /// The reusable formatter for complete accessible calendar dates.
    private final DateTimeFormatter dateFormatter;

    /// Creates an English localizer with regional formatting for the supplied locale.
    ///
    /// The locale does not translate messages automatically. Override message methods to supply translations.
    ///
    /// @param locale the locale for calendar names and date formatting
    /// @throws NullPointerException if `locale` is `null`
    public M3Localizer(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale");
        dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
    }

    /// Returns the locale supplied at construction.
    ///
    /// @return the regional formatting locale
    public final Locale getLocale() {
        return locale;
    }

    /// Returns the effective localizer for a node, including inherited and fallback values.
    ///
    /// @param node the node whose defaults are being resolved
    /// @return the nearest installed localizer, or the English fallback
    /// @throws NullPointerException if `node` is `null`
    public static M3Localizer getEffectiveLocalizer(Node node) {
        return M3LocalizerObserver.resolve(Objects.requireNonNull(node, "node"));
    }

    /// Installs or replaces a node's localizer for itself and its descendants.
    ///
    /// Installing the same instance again has no effect. Existing controls retain application-supplied text and
    /// formatting overrides. To change translations, install a new instance rather than mutating an existing one.
    ///
    /// @param node the root of the localized subtree
    /// @param localizer the replacement localizer
    /// @throws NullPointerException if either argument is `null`
    public static void install(Node node, M3Localizer localizer) {
        M3LocalizerObserver.install(Objects.requireNonNull(node, "node"),
                Objects.requireNonNull(localizer, "localizer"));
    }

    /// Removes a node's installation and restores inheritance from its parent.
    ///
    /// Descendant installations are not removed. Calling this method without a direct installation has no effect.
    ///
    /// @param node the root whose direct installation is removed
    /// @throws NullPointerException if `node` is `null`
    public static void uninstall(Node node) {
        M3LocalizerObserver.install(Objects.requireNonNull(node, "node"), null);
    }

    /// Returns the clear-text action description.
    /// @return `"Clear text"`
    public String getClearActionText() { return "Clear text"; }

    /// Returns the dismiss action description.
    /// @return `"Dismiss"`
    public String getDismissText() { return "Dismiss"; }

    /// Returns the cancellation action label.
    /// @return `"Cancel"`
    public String getCancelText() { return "Cancel"; }

    /// Returns the confirmation action label.
    /// @return `"OK"`
    public String getConfirmText() { return "OK"; }

    /// Returns the single-date dialog title.
    /// @return `"Select date"`
    public String getSelectDateText() { return "Select date"; }

    /// Returns the date-range dialog title.
    /// @return `"Select date range"`
    public String getSelectDateRangeText() { return "Select date range"; }

    /// Returns the time dialog title.
    /// @return `"Select time"`
    public String getSelectTimeText() { return "Select time"; }

    /// Returns the date-picker opening action description.
    /// @return `"Open date picker"`
    public String getOpenDatePickerText() { return "Open date picker"; }

    /// Returns the time-picker opening action description.
    /// @return `"Open time picker"`
    public String getOpenTimePickerText() { return "Open time picker"; }

    /// Returns the date-range field label.
    /// @return `"Date range"`
    public String getDateRangeText() { return "Date range"; }

    /// Returns the start-date picker action description.
    /// @return `"Open start date range picker"`
    public String getOpenStartDateRangePickerText() { return "Open start date range picker"; }

    /// Returns the end-date picker action description.
    /// @return `"Open end date range picker"`
    public String getOpenEndDateRangePickerText() { return "Open end date range picker"; }

    /// Returns the invalid-date validation message.
    /// @return `"Enter a valid date"`
    public String getInvalidDateText() { return "Enter a valid date"; }

    /// Returns the invalid-time validation message.
    /// @return `"Enter a valid time"`
    public String getInvalidTimeText() { return "Enter a valid time"; }

    /// Returns the unavailable-date validation message.
    /// @return `"Date is outside the selectable range"`
    public String getDateOutOfRangeText() { return "Date is outside the selectable range"; }

    /// Returns the unavailable-time validation message.
    /// @return `"Time is outside the selectable range"`
    public String getTimeOutOfRangeText() { return "Time is outside the selectable range"; }

    /// Returns the start-date editor description.
    /// @return `"Start date"`
    public String getStartDateText() { return "Start date"; }

    /// Returns the end-date editor description.
    /// @return `"End date"`
    public String getEndDateText() { return "End date"; }

    /// Returns the invalid-date-range validation message.
    /// @return `"Enter a valid date"`
    public String getInvalidDateRangeText() { return "Enter a valid date"; }

    /// Returns the unavailable-date-range validation message.
    /// @return `"Date range is outside the selectable range"`
    public String getDateRangeOutOfRangeText() { return "Date range is outside the selectable range"; }

    /// Returns the previous-month action description.
    /// @return `"Previous month"`
    public String getPreviousMonthText() { return "Previous month"; }

    /// Returns the next-month action description.
    /// @return `"Next month"`
    public String getNextMonthText() { return "Next month"; }

    /// Returns the previous-year action description.
    /// @return `"Previous year"`
    public String getPreviousYearText() { return "Previous year"; }

    /// Returns the next-year action description.
    /// @return `"Next year"`
    public String getNextYearText() { return "Next year"; }

    /// Returns the month-menu action description.
    /// @return `"Choose month"`
    public String getChooseMonthText() { return "Choose month"; }

    /// Returns the year-menu action description.
    /// @return `"Choose year"`
    public String getChooseYearText() { return "Choose year"; }

    /// Returns the hour editor description.
    /// @return `"Hour"`
    public String getHourText() { return "Hour"; }

    /// Returns the minute editor description.
    /// @return `"Minute"`
    public String getMinuteText() { return "Minute"; }

    /// Returns the keyboard-input mode action description.
    /// @return `"Use keyboard input"`
    public String getUseKeyboardInputText() { return "Use keyboard input"; }

    /// Returns the clock-dial mode action description.
    /// @return `"Use clock dial"`
    public String getUseClockDialText() { return "Use clock dial"; }

    /// Returns the clock-dial description.
    /// @return `"Clock dial"`
    public String getClockDialText() { return "Clock dial"; }

    /// Returns the morning period label.
    /// @return `"AM"`
    public String getAmText() { return "AM"; }

    /// Returns the afternoon period label.
    /// @return `"PM"`
    public String getPmText() { return "PM"; }

    /// Returns the navigation overflow action description.
    /// @return `"More items"`
    public String getMoreItemsText() { return "More items"; }

    /// Returns the back action description.
    /// @return `"Back"`
    public String getBackText() { return "Back"; }

    /// Returns the lower range-thumb description.
    /// @return `"Minimum value"`
    public String getMinimumValueText() { return "Minimum value"; }

    /// Returns the upper range-thumb description.
    /// @return `"Maximum value"`
    public String getMaximumValueText() { return "Maximum value"; }

    /// Returns the bottom-sheet resize action description.
    /// @return `"Resize bottom sheet"`
    public String getResizeBottomSheetText() { return "Resize bottom sheet"; }

    /// Returns the decrement action description.
    /// @return `"Decrement"`
    public String getDecrementText() { return "Decrement"; }

    /// Returns the increment action description.
    /// @return `"Increment"`
    public String getIncrementText() { return "Increment"; }

    /// Returns the invalid-number validation message.
    /// @return `"Enter a valid number"`
    public String getInvalidNumberText() { return "Enter a valid number"; }

    /// Returns the number-range validation message.
    /// @return `"Enter a value in the allowed range and step"`
    public String getNumberOutOfRangeText() { return "Enter a value in the allowed range and step"; }

    /// Returns the validation-summary title.
    /// @return `"Fix the following fields"`
    public String getValidationSummaryTitleText() { return "Fix the following fields"; }

    /// Returns the empty validation-summary message.
    /// @return `"No validation issues"`
    public String getNoValidationIssuesText() { return "No validation issues"; }

    /// Returns the fallback label for an unnamed invalid input.
    /// @return `"Field"`
    public String getFieldText() { return "Field"; }

    /// Returns the fallback description of an invalid input without an error message.
    /// @return `"Invalid value"`
    public String getInvalidValueText() { return "Invalid value"; }

    /// Returns the description of progress whose total is not known.
    /// @return `"Indeterminate"`
    public String getIndeterminateProgressText() { return "Indeterminate"; }

    /// Formats a determinate progress value for assistive technology.
    /// @param progress the normalized progress in the range zero through one
    /// @return the rounded whole percentage followed by a percent sign
    public String formatProgress(double progress) { return Math.round(progress * 100.0) + "%"; }

    /// Returns the first weekday for this localizer's region.
    /// @return the first calendar column's weekday
    public DayOfWeek getFirstDayOfWeek() { return WeekFields.of(locale).getFirstDayOfWeek(); }

    /// Formats a month name, preserving the requested grammatical context and width.
    /// @param month the month
    /// @param style the full, short, or narrow standalone or contextual style
    /// @return the localized month name
    /// @throws NullPointerException if either argument is `null`
    public String formatMonth(Month month, TextStyle style) {
        return Objects.requireNonNull(month, "month").getDisplayName(Objects.requireNonNull(style, "style"), locale);
    }

    /// Formats a weekday name, preserving the requested grammatical context and width.
    /// @param day the weekday
    /// @param style the full, short, or narrow standalone or contextual style
    /// @return the localized weekday name
    /// @throws NullPointerException if either argument is `null`
    public String formatWeekday(DayOfWeek day, TextStyle style) {
        return Objects.requireNonNull(day, "day").getDisplayName(Objects.requireNonNull(style, "style"), locale);
    }

    /// Formats a complete date for assistive technology.
    /// @param date the date to describe
    /// @return the date in this locale's full date format
    /// @throws NullPointerException if `date` is `null`
    public String formatDate(LocalDate date) {
        return dateFormatter.format(Objects.requireNonNull(date, "date"));
    }

    /// Formats a calendar year without digit grouping.
    /// @param year the ISO calendar year
    /// @return the year in decimal notation
    public String formatYear(int year) { return Integer.toString(year); }

    /// Formats the day number shown in a calendar cell.
    /// @param date the cell's date
    /// @return the day of month in decimal notation
    /// @throws NullPointerException if `date` is `null`
    public String formatDayOfMonth(LocalDate date) {
        return Integer.toString(Objects.requireNonNull(date, "date").getDayOfMonth());
    }

    /// Formats an hour label without converting between twelve- and twenty-four-hour clocks.
    ///
    /// Overrides must return one or two decimal digit characters recognized by [Character#digit(char, int)],
    /// representing the supplied value. The same text is used in editable time inputs; units and punctuation are
    /// not accepted there. Twelve-hour dial labels omit a leading numeric zero.
    /// @param hour the display hour selected by the control's clock convention
    /// @return the display hour with at least two decimal digits
    public String formatHour(int hour) { return hour >= 0 && hour < 10 ? "0" + hour : Integer.toString(hour); }

    /// Formats a minute label.
    ///
    /// Overrides must return one or two decimal digit characters recognized by [Character#digit(char, int)],
    /// representing the supplied value, because the text is also used in editable time inputs.
    /// @param minute the minute of hour
    /// @return the minute with at least two decimal digits
    public String formatMinute(int minute) { return minute >= 0 && minute < 10 ? "0" + minute : Integer.toString(minute); }
}
