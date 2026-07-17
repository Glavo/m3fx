// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/// Provides reusable [M3TimePreset] values for common time choices.
///
/// These methods use English labels and minute precision. Seconds and nanoseconds in supplied anchor values are
/// discarded. Applications that require localized labels can create [M3TimePreset] values directly.
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
@NotNullByDefault
public final class M3TimePresets {
    /// Prevents utility class instantiation.
    private M3TimePresets() {
    }

    /// Returns a preset for the supplied time labeled `Now`.
    ///
    /// @param time the time selected by the preset
    /// @return a `Now` preset for the supplied time with seconds and nanos cleared
    /// @throws NullPointerException if `time` is `null`
    public static M3TimePreset now(LocalTime time) {
        return new M3TimePreset("Now", normalizeTime(time));
    }

    /// Returns a preset offset by the requested number of minutes from the supplied time.
    ///
    /// @param time the anchor time
    /// @param minuteOffset the number of minutes to add to the anchor time
    /// @return a preset for the offset time with seconds and nanos cleared
    /// @throws NullPointerException if `time` is `null`
    public static M3TimePreset minutesFrom(LocalTime time, int minuteOffset) {
        LocalTime anchorTime = normalizeTime(time);
        if (minuteOffset == 0) {
            return now(anchorTime);
        }

        int absoluteOffset = Math.abs(minuteOffset);
        String text = minuteOffset > 0 ? "In " + absoluteOffset + " min" : absoluteOffset + " min ago";
        return new M3TimePreset(text, normalizeTime(anchorTime.plusMinutes(minuteOffset)));
    }

    /// Returns a preset for midnight.
    ///
    /// @return a `Midnight` preset selecting `LocalTime.MIDNIGHT`
    public static M3TimePreset midnight() {
        return new M3TimePreset("Midnight", LocalTime.MIDNIGHT);
    }

    /// Returns a preset for 09:00.
    ///
    /// @return a `Morning` preset selecting 09:00
    public static M3TimePreset morning() {
        return new M3TimePreset("Morning", LocalTime.of(9, 0));
    }

    /// Returns a preset for 12:00.
    ///
    /// @return a `Noon` preset selecting `LocalTime.NOON`
    public static M3TimePreset noon() {
        return new M3TimePreset("Noon", LocalTime.NOON);
    }

    /// Returns a preset for 15:00.
    ///
    /// @return an `Afternoon` preset selecting 15:00
    public static M3TimePreset afternoon() {
        return new M3TimePreset("Afternoon", LocalTime.of(15, 0));
    }

    /// Returns a preset for 18:00.
    ///
    /// @return an `Evening` preset selecting 18:00
    public static M3TimePreset evening() {
        return new M3TimePreset("Evening", LocalTime.of(18, 0));
    }

    /// Returns the default time preset list.
    ///
    /// @param time the anchor time used to compute relative presets
    /// @return the immutable default time preset list
    /// @throws NullPointerException if `time` is `null`
    public static @Unmodifiable List<M3TimePreset> common(LocalTime time) {
        LocalTime anchorTime = normalizeTime(time);
        return List.of(
                now(anchorTime),
                minutesFrom(anchorTime, 15),
                morning(),
                noon(),
                evening()
        );
    }

    /// Clears seconds and nanos because presets use hour and minute precision.
    private static LocalTime normalizeTime(LocalTime time) {
        return Objects.requireNonNull(time, "time").withSecond(0).withNano(0);
    }
}
