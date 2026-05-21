// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.util.Objects;

/// An immutable inclusive local-date range used by M3FX date range pickers.
///
/// The range always contains both endpoints and rejects values where the start date is after the end date.
/// [M3DateRangePicker] and [M3DateRangePickerField] use this record as their selected-value type.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
///
/// @param startDate the first date in the range
/// @param endDate the last date in the range
@NotNullByDefault
public record M3DateRange(LocalDate startDate, LocalDate endDate) {
    /// Creates a validated inclusive date range.
    public M3DateRange {
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }

    /// Returns the first date in the range.
    @Override
    public LocalDate startDate() {
        return startDate;
    }

    /// Returns the last date in the range.
    @Override
    public LocalDate endDate() {
        return endDate;
    }

    /// Returns whether the supplied date is inside this inclusive range.
    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
