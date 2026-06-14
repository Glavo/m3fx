// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalTime;
import java.util.Objects;

/// A labeled time that can be applied to an [M3TimePicker].
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
///
/// @param text the text shown for the preset action
/// @param time the time selected by the preset
@NotNullByDefault
public record M3TimePreset(String text, LocalTime time) {
    /// Creates a validated time preset.
    public M3TimePreset {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(time, "time");
    }

    /// Returns the text shown for the preset action.
    @Override
    public String text() {
        return text;
    }

    /// Returns the time selected by the preset.
    @Override
    public LocalTime time() {
        return time;
    }

    /// Applies this preset to the supplied time picker.
    public void applyTo(M3TimePicker picker) {
        Objects.requireNonNull(picker, "picker").applyPreset(this);
    }
}
