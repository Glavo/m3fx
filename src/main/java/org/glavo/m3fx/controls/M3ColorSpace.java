// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/// Identifies a color space represented by [M3Color].
///
/// Each color space defines three ordered color channels. [M3ColorChannel#ALPHA] is available in every color space
/// in addition to those channels, but is not included in [#getChannels()].
@NotNullByDefault
public enum M3ColorSpace {
    /// The red, green, and blue color space.
    RGB(List.of(M3ColorChannel.RED, M3ColorChannel.GREEN, M3ColorChannel.BLUE)),

    /// The hue, saturation, and lightness color space.
    HSL(List.of(M3ColorChannel.HUE, M3ColorChannel.SATURATION, M3ColorChannel.LIGHTNESS)),

    /// The hue, saturation, and brightness color space.
    HSB(List.of(M3ColorChannel.HUE, M3ColorChannel.SATURATION, M3ColorChannel.BRIGHTNESS));

    /// The immutable ordered color channels of this space.
    private final @Unmodifiable List<M3ColorChannel> channels;

    /// Creates a color-space descriptor.
    ///
    /// @param channels the three ordered color channels
    M3ColorSpace(@Unmodifiable List<M3ColorChannel> channels) {
        this.channels = channels;
    }

    /// Returns the ordered color channels of this space.
    ///
    /// The returned list contains exactly three elements and is unmodifiable. It does not include
    /// [M3ColorChannel#ALPHA].
    ///
    /// @return an unmodifiable list of the three color channels, in component order
    public @Unmodifiable List<M3ColorChannel> getChannels() {
        return channels;
    }

    /// Returns whether values in this color space expose a channel.
    ///
    /// This method returns `true` for [M3ColorChannel#ALPHA] and for each element of [#getChannels()].
    ///
    /// @param channel the non-null channel to test
    /// @return `true` if `channel` is available in this color space
    /// @throws NullPointerException if `channel` is `null`
    public boolean supports(M3ColorChannel channel) {
        Objects.requireNonNull(channel, "channel");
        return channel == M3ColorChannel.ALPHA || channels.contains(channel);
    }
}
