// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Node;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// Reusable finite transition for an internal node's opacity and scale channels.
@NotNullByDefault
final class M3NodeTransition extends M3FiniteTransition {
    /// The node whose visual channels are animated.
    private final Node node;

    /// The opacity at the beginning of the current transition.
    private double startOpacity;

    /// The opacity at the end of the current transition.
    private double targetOpacity;

    /// The horizontal scale at the beginning of the current transition.
    private double startScaleX;

    /// The horizontal scale at the end of the current transition.
    private double targetScaleX;

    /// The vertical scale at the beginning of the current transition.
    private double startScaleY;

    /// The vertical scale at the end of the current transition.
    private double targetScaleY;

    /// Creates a transition for an internal skin node.
    M3NodeTransition(Node node) {
        this.node = node;
    }

    /// Reconfigures all supported visual channels from their current values.
    void configure(M3MotionSpec spec, double targetOpacity, double targetScaleX, double targetScaleY) {
        stop();
        setCycleDuration(spec.duration());
        setInterpolator(spec.interpolator());
        startOpacity = node.getOpacity();
        this.targetOpacity = targetOpacity;
        startScaleX = node.getScaleX();
        this.targetScaleX = targetScaleX;
        startScaleY = node.getScaleY();
        this.targetScaleY = targetScaleY;
    }

    /// Applies the eased opacity and scale values for channels that change during the current transition.
    @Override
    protected void interpolate(double fraction) {
        if (Double.compare(startOpacity, targetOpacity) != 0) {
            node.setOpacity(interpolate(startOpacity, targetOpacity, fraction));
        }
        if (Double.compare(startScaleX, targetScaleX) != 0) {
            node.setScaleX(interpolate(startScaleX, targetScaleX, fraction));
        }
        if (Double.compare(startScaleY, targetScaleY) != 0) {
            node.setScaleY(interpolate(startScaleY, targetScaleY, fraction));
        }
    }

    /// Interpolates linearly between two scalar values.
    private static double interpolate(double start, double end, double fraction) {
        return start + (end - start) * fraction;
    }
}
