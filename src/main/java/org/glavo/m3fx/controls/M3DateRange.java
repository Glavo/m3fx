// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.util.Objects;

/// An inclusive local-date range.
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
