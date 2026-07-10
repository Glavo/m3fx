// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;

/// Reusable finite transition for a node's opacity, scale, and translation channels.
@NotNullByDefault
public final class M3NodeTransition extends M3FiniteTransition {
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

    /// The horizontal translation at the beginning of the current transition.
    private double startTranslateX;

    /// The horizontal translation at the end of the current transition.
    private double targetTranslateX;

    /// The vertical translation at the beginning of the current transition.
    private double startTranslateY;

    /// The vertical translation at the end of the current transition.
    private double targetTranslateY;

    /// Creates a transition for a node.
    ///
    /// @param node the node whose visual channels are animated
    public M3NodeTransition(Node node) {
        this.node = node;
    }

    /// Reconfigures all supported visual channels from their current values.
    ///
    /// @param spec the duration and easing specification
    /// @param targetOpacity the target opacity
    /// @param targetScaleX the target horizontal scale
    /// @param targetScaleY the target vertical scale
    /// @param targetTranslateX the target horizontal translation
    /// @param targetTranslateY the target vertical translation
    public void configure(
            M3MotionSpec spec,
            double targetOpacity,
            double targetScaleX,
            double targetScaleY,
            double targetTranslateX,
            double targetTranslateY
    ) {
        stop();
        setCycleDuration(spec.duration());
        setInterpolator(spec.interpolator());
        startOpacity = node.getOpacity();
        this.targetOpacity = targetOpacity;
        startScaleX = node.getScaleX();
        this.targetScaleX = targetScaleX;
        startScaleY = node.getScaleY();
        this.targetScaleY = targetScaleY;
        startTranslateX = node.getTranslateX();
        this.targetTranslateX = targetTranslateX;
        startTranslateY = node.getTranslateY();
        this.targetTranslateY = targetTranslateY;
    }

    /// Applies the eased values for channels that change during the current transition.
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
        if (Double.compare(startTranslateX, targetTranslateX) != 0) {
            node.setTranslateX(interpolate(startTranslateX, targetTranslateX, fraction));
        }
        if (Double.compare(startTranslateY, targetTranslateY) != 0) {
            node.setTranslateY(interpolate(startTranslateY, targetTranslateY, fraction));
        }
    }

    /// Interpolates linearly between two scalar values.
    private static double interpolate(double start, double end, double fraction) {
        return start + (end - start) * fraction;
    }
}
