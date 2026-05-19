// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/// Provides reusable [M3TimePreset] factories for common time choices.
@NotNullByDefault
public final class M3TimePresets {
    /// Prevents utility class instantiation.
    private M3TimePresets() {
    }

    /// Returns a custom preset for one time.
    public static M3TimePreset time(String text, LocalTime time) {
        return new M3TimePreset(text, normalizeTime(time));
    }

    /// Returns a preset for the supplied time labeled `Now`.
    public static M3TimePreset now(LocalTime time) {
        return time("Now", time);
    }

    /// Returns a preset offset by the requested number of minutes from the supplied time.
    public static M3TimePreset minutesFrom(LocalTime time, int minuteOffset) {
        LocalTime anchorTime = normalizeTime(time);
        if (minuteOffset == 0) {
            return now(anchorTime);
        }

        int absoluteOffset = Math.abs(minuteOffset);
        String text = minuteOffset > 0 ? "In " + absoluteOffset + " min" : absoluteOffset + " min ago";
        return time(text, anchorTime.plusMinutes(minuteOffset));
    }

    /// Returns a preset for midnight.
    public static M3TimePreset midnight() {
        return time("Midnight", LocalTime.MIDNIGHT);
    }

    /// Returns a preset for 09:00.
    public static M3TimePreset morning() {
        return time("Morning", LocalTime.of(9, 0));
    }

    /// Returns a preset for 12:00.
    public static M3TimePreset noon() {
        return time("Noon", LocalTime.NOON);
    }

    /// Returns a preset for 15:00.
    public static M3TimePreset afternoon() {
        return time("Afternoon", LocalTime.of(15, 0));
    }

    /// Returns a preset for 18:00.
    public static M3TimePreset evening() {
        return time("Evening", LocalTime.of(18, 0));
    }

    /// Returns the default time preset list.
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
