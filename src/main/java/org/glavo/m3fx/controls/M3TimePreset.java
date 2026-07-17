// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalTime;
import java.util.Objects;

/// A labeled time preset for an [M3TimePicker].
///
/// Presets are immutable value objects. The text is presentation content supplied by the application; M3FX does
/// not localize it. Both components are required.
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
///
/// @param text the text shown for the preset action
/// @param time the time selected by the preset
@NotNullByDefault
public record M3TimePreset(String text, LocalTime time) {
    /// Creates a validated time preset.
    ///
    /// @throws NullPointerException if `text` or `time` is `null`
    public M3TimePreset {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(time, "time");
    }

    /// Returns the text shown for the preset action.
    ///
    /// @return the non-null action text
    @Override
    public String text() {
        return text;
    }

    /// Returns the time selected by the preset.
    ///
    /// @return the non-null preset time
    @Override
    public LocalTime time() {
        return time;
    }

}
