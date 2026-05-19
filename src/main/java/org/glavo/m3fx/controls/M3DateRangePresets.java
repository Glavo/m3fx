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

/// Provides reusable [M3DateRangePreset] factories for common date range choices.
@NotNullByDefault
public final class M3DateRangePresets {
    /// Prevents utility class instantiation.
    private M3DateRangePresets() {
    }

    /// Returns a custom preset with inclusive range endpoints.
    public static M3DateRangePreset range(String text, LocalDate startDate, LocalDate endDate) {
        return new M3DateRangePreset(text, startDate, endDate);
    }

    /// Returns a one-day preset for the supplied date.
    public static M3DateRangePreset singleDay(String text, LocalDate date) {
        LocalDate validatedDate = Objects.requireNonNull(date, "date");
        return new M3DateRangePreset(text, validatedDate, validatedDate);
    }

    /// Returns a one-day preset for the supplied date labeled `Today`.
    public static M3DateRangePreset today(LocalDate date) {
        return singleDay("Today", date);
    }

    /// Returns a one-day preset for the day after the supplied date.
    public static M3DateRangePreset tomorrow(LocalDate date) {
        return singleDay("Tomorrow", Objects.requireNonNull(date, "date").plusDays(1));
    }

    /// Returns a preset starting at the supplied date and spanning the requested number of days.
    public static M3DateRangePreset nextDays(LocalDate startDate, int dayCount) {
        LocalDate start = Objects.requireNonNull(startDate, "startDate");
        int count = positiveDayCount(dayCount);
        return new M3DateRangePreset("Next " + count + " days", start, start.plusDays(count - 1L));
    }

    /// Returns a preset ending at the supplied date and spanning the requested number of days.
    public static M3DateRangePreset previousDays(LocalDate endDate, int dayCount) {
        LocalDate end = Objects.requireNonNull(endDate, "endDate");
        int count = positiveDayCount(dayCount);
        return new M3DateRangePreset("Previous " + count + " days", end.minusDays(count - 1L), end);
    }

    /// Returns the week containing the supplied date.
    public static M3DateRangePreset thisWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate start = weekStart(date, firstDayOfWeek);
        return new M3DateRangePreset("This week", start, start.plusDays(6));
    }

    /// Returns the week after the week containing the supplied date.
    public static M3DateRangePreset nextWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate start = weekStart(date, firstDayOfWeek).plusWeeks(1);
        return new M3DateRangePreset("Next week", start, start.plusDays(6));
    }

    /// Returns the month containing the supplied date.
    public static M3DateRangePreset thisMonth(LocalDate date) {
        YearMonth month = YearMonth.from(Objects.requireNonNull(date, "date"));
        return new M3DateRangePreset("This month", month.atDay(1), month.atEndOfMonth());
    }

    /// Returns the month after the month containing the supplied date.
    public static M3DateRangePreset nextMonth(LocalDate date) {
        YearMonth month = YearMonth.from(Objects.requireNonNull(date, "date")).plusMonths(1);
        return new M3DateRangePreset("Next month", month.atDay(1), month.atEndOfMonth());
    }

    /// Returns the default date range dialog preset list.
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
