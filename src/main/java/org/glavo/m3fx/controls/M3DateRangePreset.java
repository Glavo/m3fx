// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.util.Objects;

/// A labeled inclusive date range that can be applied to an [M3DateRangePicker].
///
/// @param text the text shown for the preset action
/// @param range the inclusive range selected by the preset
@NotNullByDefault
public record M3DateRangePreset(String text, M3DateRange range) {
    /// Creates a validated date range preset.
    public M3DateRangePreset {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(range, "range");
    }

    /// Creates a date range preset from two inclusive endpoints.
    public M3DateRangePreset(String text, LocalDate startDate, LocalDate endDate) {
        this(text, new M3DateRange(startDate, endDate));
    }

    /// Returns the text shown for the preset action.
    @Override
    public String text() {
        return text;
    }

    /// Returns the inclusive range selected by the preset.
    @Override
    public M3DateRange range() {
        return range;
    }

    /// Applies this preset to the supplied date range picker.
    public void applyTo(M3DateRangePicker picker) {
        Objects.requireNonNull(picker, "picker").applyPreset(this);
    }
}
