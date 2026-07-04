// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/// Provides reusable [M3DateRangePreset] values for common date range choices.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DateRangePresets {
    /// Prevents utility class instantiation.
    private M3DateRangePresets() {
    }

    /// Returns a one-day preset for the supplied date labeled `Today`.
    ///
    /// @param date the date selected by the preset
    /// @return a one-day preset for the supplied date labeled `Today`
    public static M3DateRangePreset today(LocalDate date) {
        LocalDate validatedDate = Objects.requireNonNull(date, "date");
        return new M3DateRangePreset("Today", validatedDate, validatedDate);
    }

    /// Returns a one-day preset for the day after the supplied date.
    ///
    /// @param date the date used to derive tomorrow
    /// @return a one-day preset for the day after the supplied date
    public static M3DateRangePreset tomorrow(LocalDate date) {
        LocalDate tomorrow = Objects.requireNonNull(date, "date").plusDays(1);
        return new M3DateRangePreset("Tomorrow", tomorrow, tomorrow);
    }

    /// Returns a preset starting at the supplied date and spanning the requested number of days.
    ///
    /// @param startDate the first date included in the preset range
    /// @param dayCount the number of days included in the preset range
    /// @return a preset starting at the supplied date and spanning the requested number of days
    public static M3DateRangePreset nextDays(LocalDate startDate, int dayCount) {
        LocalDate start = Objects.requireNonNull(startDate, "startDate");
        int count = positiveDayCount(dayCount);
        return new M3DateRangePreset("Next " + count + " days", start, start.plusDays(count - 1L));
    }

    /// Returns a preset ending at the supplied date and spanning the requested number of days.
    ///
    /// @param endDate the last date included in the preset range
    /// @param dayCount the number of days included in the preset range
    /// @return a preset ending at the supplied date and spanning the requested number of days
    public static M3DateRangePreset previousDays(LocalDate endDate, int dayCount) {
        LocalDate end = Objects.requireNonNull(endDate, "endDate");
        int count = positiveDayCount(dayCount);
        return new M3DateRangePreset("Previous " + count + " days", end.minusDays(count - 1L), end);
    }

    /// Returns the week containing the supplied date.
    ///
    /// @param date a date inside the returned week
    /// @param firstDayOfWeek the first day used to calculate the week boundary
    /// @return the week containing the supplied date
    public static M3DateRangePreset thisWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate start = weekStart(date, firstDayOfWeek);
        return new M3DateRangePreset("This week", start, start.plusDays(6));
    }

    /// Returns the week after the week containing the supplied date.
    ///
    /// @param date a date inside the reference week
    /// @param firstDayOfWeek the first day used to calculate week boundaries
    /// @return the week after the week containing the supplied date
    public static M3DateRangePreset nextWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate start = weekStart(date, firstDayOfWeek).plusWeeks(1);
        return new M3DateRangePreset("Next week", start, start.plusDays(6));
    }

    /// Returns the month containing the supplied date.
    ///
    /// @param date a date inside the returned month
    /// @return the month containing the supplied date
    public static M3DateRangePreset thisMonth(LocalDate date) {
        YearMonth month = YearMonth.from(Objects.requireNonNull(date, "date"));
        return new M3DateRangePreset("This month", month.atDay(1), month.atEndOfMonth());
    }

    /// Returns the month after the month containing the supplied date.
    ///
    /// @param date a date inside the reference month
    /// @return the month after the month containing the supplied date
    public static M3DateRangePreset nextMonth(LocalDate date) {
        YearMonth month = YearMonth.from(Objects.requireNonNull(date, "date")).plusMonths(1);
        return new M3DateRangePreset("Next month", month.atDay(1), month.atEndOfMonth());
    }

    /// Returns the default date range dialog preset list.
    ///
    /// @param date the date used to derive relative common presets
    /// @param firstDayOfWeek the first day used to calculate week presets
    /// @return the default date range dialog preset list
    public static @Unmodifiable List<M3DateRangePreset> common(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate validatedDate = Objects.requireNonNull(date, "date");
        DayOfWeek validatedFirstDayOfWeek = Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek");
        return List.of(
                today(validatedDate),
                tomorrow(validatedDate),
                nextDays(validatedDate, 7),
                thisWeek(validatedDate, validatedFirstDayOfWeek),
                nextWeek(validatedDate, validatedFirstDayOfWeek),
                thisMonth(validatedDate)
        );
    }

    /// Returns the first date in the week containing the supplied date.
    private static LocalDate weekStart(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate validatedDate = Objects.requireNonNull(date, "date");
        DayOfWeek validatedFirstDayOfWeek = Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek");
        int daysFromWeekStart = Math.floorMod(
                validatedDate.getDayOfWeek().getValue() - validatedFirstDayOfWeek.getValue(),
                7
        );
        return validatedDate.minusDays(daysFromWeekStart);
    }

    /// Returns a validated positive day count.
    private static int positiveDayCount(int dayCount) {
        if (dayCount <= 0) {
            throw new IllegalArgumentException("dayCount must be positive");
        }
        return dayCount;
    }
}
