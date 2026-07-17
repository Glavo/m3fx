// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.util.Objects;

/// A labeled inclusive date range preset for an [M3DateRangePicker].
///
/// Presets are immutable value objects. The text is presentation content supplied by the application; M3FX does
/// not localize it. Both components are required.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
///
/// @param text the text shown for the preset action
/// @param range the inclusive range selected by the preset
@NotNullByDefault
public record M3DateRangePreset(String text, M3DateRange range) {
    /// Creates a validated date range preset.
    ///
    /// @throws NullPointerException if `text` or `range` is `null`
    public M3DateRangePreset {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(range, "range");
    }

    /// Creates a date range preset from two inclusive endpoints.
    ///
    /// @param text the text shown for the preset action
    /// @param startDate the first date included in the range
    /// @param endDate the last date included in the range
    /// @throws NullPointerException if any argument is `null`
    /// @throws IllegalArgumentException if `endDate` is before `startDate`
    public M3DateRangePreset(String text, LocalDate startDate, LocalDate endDate) {
        this(text, new M3DateRange(startDate, endDate));
    }

    /// Returns the text shown for the preset action.
    ///
    /// @return the text shown for the preset action
    @Override
    public String text() {
        return text;
    }

    /// Returns the inclusive range selected by the preset.
    ///
    /// @return the inclusive range selected by the preset
    @Override
    public M3DateRange range() {
        return range;
    }

}
