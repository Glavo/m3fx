// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Describes the color space and two axes edited by an [M3ColorArea].
///
/// Both axes must be distinct, non-alpha channels of `colorSpace`. The remaining channel is the fixed channel:
/// direct area adjustments preserve its current value. The x-axis increases from the logical start edge to the
/// logical end edge and therefore mirrors with node orientation. The y-axis increases from the bottom edge to the
/// top edge.
///
/// @param colorSpace the non-null color space used for all three color channels
/// @param xChannel   the non-null channel assigned to the logical horizontal axis
/// @param yChannel   the non-null channel assigned to the vertical axis
@NotNullByDefault
public record M3ColorPlane(
        M3ColorSpace colorSpace,
        M3ColorChannel xChannel,
        M3ColorChannel yChannel
) {
    /// The conventional HSB saturation and brightness plane.
    public static final M3ColorPlane HSB_SATURATION_BRIGHTNESS =
            new M3ColorPlane(M3ColorSpace.HSB, M3ColorChannel.SATURATION, M3ColorChannel.BRIGHTNESS);

    /// The conventional HSL saturation and lightness plane.
    public static final M3ColorPlane HSL_SATURATION_LIGHTNESS =
            new M3ColorPlane(M3ColorSpace.HSL, M3ColorChannel.SATURATION, M3ColorChannel.LIGHTNESS);

    /// An RGB red and green plane that retains the current blue channel.
    public static final M3ColorPlane RGB_RED_GREEN =
            new M3ColorPlane(M3ColorSpace.RGB, M3ColorChannel.RED, M3ColorChannel.GREEN);

    /// Creates a color plane after validating its axes.
    ///
    /// @throws NullPointerException if a component is `null`
    /// @throws IllegalArgumentException if a channel is alpha, the channels are equal, or a channel does not belong
    ///                                  to `colorSpace`
    public M3ColorPlane {
        Objects.requireNonNull(colorSpace, "colorSpace");
        Objects.requireNonNull(xChannel, "xChannel");
        Objects.requireNonNull(yChannel, "yChannel");
        if (xChannel == M3ColorChannel.ALPHA || yChannel == M3ColorChannel.ALPHA) {
            throw new IllegalArgumentException("color area channels must not be alpha");
        }
        if (xChannel == yChannel) {
            throw new IllegalArgumentException("color area channels must be distinct");
        }
        if (!colorSpace.supports(xChannel) || !colorSpace.supports(yChannel)) {
            throw new IllegalArgumentException("color area channels must belong to " + colorSpace);
        }
    }

    /// Returns the channel not assigned to either axis.
    ///
    /// @return the non-null remaining channel in `colorSpace`
    public M3ColorChannel fixedChannel() {
        for (M3ColorChannel channel : colorSpace.getChannels()) {
            if (channel != xChannel && channel != yChannel) {
                return channel;
            }
        }
        throw new IllegalStateException("color plane has no fixed channel");
    }
}
