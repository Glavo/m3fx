// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Formats color-channel values for accessibility clients.
@NotNullByDefault
final class M3ColorAccessibility {
    /// Prevents utility class instantiation.
    private M3ColorAccessibility() {
    }

    /// Returns a localized-independent description of one channel value.
    ///
    /// @param channel the described channel
    /// @param value   the channel value in its declared range
    /// @return the channel name followed by a value and unit
    static String channelValue(M3ColorChannel channel, double value) {
        if (channel == M3ColorChannel.HUE) {
            return "Hue " + Math.round(value) + " degrees";
        }
        return channelName(channel) + " " + Math.round(value * 100.0) + " percent";
    }

    /// Returns the English accessibility name of a color channel.
    ///
    /// @param channel the channel to name
    /// @return the channel name
    private static String channelName(M3ColorChannel channel) {
        return switch (channel) {
            case RED -> "Red";
            case GREEN -> "Green";
            case BLUE -> "Blue";
            case HUE -> "Hue";
            case SATURATION -> "Saturation";
            case LIGHTNESS -> "Lightness";
            case BRIGHTNESS -> "Brightness";
            case ALPHA -> "Alpha";
        };
    }
}
