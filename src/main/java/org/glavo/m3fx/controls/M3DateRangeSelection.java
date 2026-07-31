// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;

/// An immutable snapshot of empty, in-progress, or complete date-range selection.
///
/// An empty selection has two `null` endpoints. An in-progress selection has a non-null start and a `null` end. A
/// complete selection contains both endpoints in ascending order and can be converted to [M3DateRange].
///
/// This value lets observers consume one atomic selection notification even though range picker controls also
/// expose independently bindable endpoint properties.
///
/// @param startDate the selected start, or `null` for an empty selection
/// @param endDate the selected end, or `null` for an empty or in-progress selection
@NotNullByDefault
public record M3DateRangeSelection(
        @Nullable LocalDate startDate,
        @Nullable LocalDate endDate
) {
    /// The shared empty selection.
    public static final M3DateRangeSelection EMPTY = new M3DateRangeSelection(null, null);

    /// Creates a validated date-range selection snapshot.
    ///
    /// @throws IllegalArgumentException if `endDate` is non-null while `startDate` is `null`, or if the start is
    ///                                  after the end
    public M3DateRangeSelection {
        if (startDate == null && endDate != null) {
            throw new IllegalArgumentException("startDate must be selected before endDate");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }

    /// Returns a selection snapshot, reusing [#EMPTY] for two absent endpoints.
    ///
    /// @param startDate the selected start, or `null` for an empty selection
    /// @param endDate the selected end, or `null` for an empty or in-progress selection
    /// @return the validated immutable selection
    /// @throws IllegalArgumentException if `endDate` is non-null while `startDate` is `null`, or if the start is
    ///                                  after the end
    static M3DateRangeSelection of(
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate
    ) {
        return startDate == null && endDate == null
                ? EMPTY
                : new M3DateRangeSelection(startDate, endDate);
    }

    /// Returns the selected start, or `null` for an empty selection.
    ///
    /// @return the selected start, or `null`
    @Override
    public @Nullable LocalDate startDate() {
        return startDate;
    }

    /// Returns the selected end, or `null` for an empty or in-progress selection.
    ///
    /// @return the selected end, or `null`
    @Override
    public @Nullable LocalDate endDate() {
        return endDate;
    }

    /// Returns whether this selection has no start or end.
    ///
    /// @return `true` when both endpoints are absent
    public boolean isEmpty() {
        return startDate == null;
    }

    /// Returns whether this selection has both endpoints.
    ///
    /// @return `true` when both endpoints are present
    public boolean isComplete() {
        return endDate != null;
    }

    /// Returns this complete selection as an inclusive range.
    ///
    /// @return the inclusive range, or `null` while this selection is empty or in progress
    public @Nullable M3DateRange toRange() {
        return endDate == null ? null : new M3DateRange(Objects.requireNonNull(startDate), endDate);
    }
}
