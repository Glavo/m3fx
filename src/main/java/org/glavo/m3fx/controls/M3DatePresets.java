// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/// Provides reusable [M3DatePreset] factories for common date choices.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DatePresets {
    /// Prevents utility class instantiation.
    private M3DatePresets() {
    }

    /// Returns a custom preset for one date.
    ///
    /// @param text the preset action text
    /// @param date the date selected by the preset
    /// @return a custom date preset
    public static M3DatePreset date(String text, LocalDate date) {
        return new M3DatePreset(text, date);
    }

    /// Returns a preset for the supplied date labeled `Today`.
    ///
    /// @param date the date selected by the preset
    /// @return a `Today` preset for the supplied date
    public static M3DatePreset today(LocalDate date) {
        return date("Today", date);
    }

    /// Returns a preset for the day after the supplied date.
    ///
    /// @param date the anchor date
    /// @return a `Tomorrow` preset for the day after the supplied date
    public static M3DatePreset tomorrow(LocalDate date) {
        return date("Tomorrow", Objects.requireNonNull(date, "date").plusDays(1));
    }

    /// Returns a preset for the day before the supplied date.
    ///
    /// @param date the anchor date
    /// @return a `Yesterday` preset for the day before the supplied date
    public static M3DatePreset yesterday(LocalDate date) {
        return date("Yesterday", Objects.requireNonNull(date, "date").minusDays(1));
    }

    /// Returns a preset offset by the requested number of days from the supplied date.
    ///
    /// @param date the anchor date
    /// @param dayOffset the number of days to add to the anchor date
    /// @return a preset for the offset date
    public static M3DatePreset daysFrom(LocalDate date, int dayOffset) {
        LocalDate anchorDate = Objects.requireNonNull(date, "date");
        if (dayOffset == 0) {
            return today(anchorDate);
        }
        if (dayOffset == 1) {
            return tomorrow(anchorDate);
        }
        if (dayOffset == -1) {
            return yesterday(anchorDate);
        }

        int absoluteOffset = Math.abs(dayOffset);
        String text = dayOffset > 0 ? "In " + absoluteOffset + " days" : absoluteOffset + " days ago";
        return date(text, anchorDate.plusDays(dayOffset));
    }

    /// Returns a preset for the first day of the month containing the supplied date.
    ///
    /// @param date the anchor date
    /// @return a preset for the first day of the anchor date's month
    public static M3DatePreset thisMonthStart(LocalDate date) {
        YearMonth month = YearMonth.from(Objects.requireNonNull(date, "date"));
        return date("Start of month", month.atDay(1));
    }

    /// Returns a preset for the first day of the month after the month containing the supplied date.
    ///
    /// @param date the anchor date
    /// @return a preset for the first day of the month after the anchor date's month
    public static M3DatePreset nextMonthStart(LocalDate date) {
        YearMonth month = YearMonth.from(Objects.requireNonNull(date, "date")).plusMonths(1);
        return date("Next month", month.atDay(1));
    }

    /// Returns the default single-date preset list.
    ///
    /// @param date the anchor date used to compute relative presets
    /// @return the immutable default single-date preset list
    public static @Unmodifiable List<M3DatePreset> common(LocalDate date) {
        LocalDate anchorDate = Objects.requireNonNull(date, "date");
        return List.of(
                today(anchorDate),
                tomorrow(anchorDate),
                daysFrom(anchorDate, 7),
                thisMonthStart(anchorDate),
                nextMonthStart(anchorDate)
        );
    }
}
