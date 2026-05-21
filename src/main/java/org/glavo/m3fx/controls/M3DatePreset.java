// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.util.Objects;

/// A labeled date that can be applied to an [M3DatePicker].
///
/// @param text the text shown for the preset action
/// @param date the date selected by the preset
@NotNullByDefault
public record M3DatePreset(String text, LocalDate date) {
    /// Creates a validated date preset.
    public M3DatePreset {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(date, "date");
    }

    /// Returns the text shown for the preset action.
    ///
    /// @return the text shown for the preset action
    @Override
    public String text() {
        return text;
    }

    /// Returns the date selected by the preset.
    ///
    /// @return the date selected by the preset
    @Override
    public LocalDate date() {
        return date;
    }

    /// Applies this preset to the supplied date picker.
    ///
    /// @param picker the date picker that should receive this preset
    public void applyTo(M3DatePicker picker) {
        Objects.requireNonNull(picker, "picker").applyPreset(this);
    }
}
