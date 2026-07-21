// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.scene.Node;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.animation.M3ScalarChannel;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Reusable finite transition for a node's opacity, scale, and translation channels.
///
/// The transition coordinates five reusable scalar channels from one JavaFX pulse receiver. Each channel retains
/// velocity independently when an active run is retargeted and may therefore have a different physical settling
/// duration. Only channels with displacement or retained spring velocity are written during a run, allowing
/// independent transitions to animate disjoint properties of the same node. The enclosing transition runs until
/// the longest channel settles and allocates no per-pulse objects.
@NotNullByDefault
public final class M3NodeTransition extends M3FiniteTransition {
    /// The opacity delta below which a physical spring is visually settled.
    private static final double OPACITY_VISIBILITY_THRESHOLD = 1.0e-2;

    /// The scale delta below which a physical spring changes rendered geometry by less than a subpixel.
    private static final double SCALE_VISIBILITY_THRESHOLD = 5.0e-4;

    /// The translation delta below which a physical spring changes position by less than half a logical pixel.
    private static final double TRANSLATION_VISIBILITY_THRESHOLD = 5.0e-1;

    /// The node whose visual channels are animated.
    private final Node node;

    /// The scalar channel that animates opacity.
    private final M3ScalarChannel opacityChannel = new M3ScalarChannel(OPACITY_VISIBILITY_THRESHOLD);

    /// The scalar channel that animates horizontal scale.
    private final M3ScalarChannel scaleXChannel = new M3ScalarChannel(SCALE_VISIBILITY_THRESHOLD);

    /// The scalar channel that animates vertical scale.
    private final M3ScalarChannel scaleYChannel = new M3ScalarChannel(SCALE_VISIBILITY_THRESHOLD);

    /// The scalar channel that animates horizontal translation.
    private final M3ScalarChannel translateXChannel = new M3ScalarChannel(TRANSLATION_VISIBILITY_THRESHOLD);

    /// The scalar channel that animates vertical translation.
    private final M3ScalarChannel translateYChannel = new M3ScalarChannel(TRANSLATION_VISIBILITY_THRESHOLD);

    /// The duration of the longest configured channel, in seconds.
    private double runDurationSeconds;

    /// Whether the current run owns the node's opacity channel.
    private boolean animateOpacity;

    /// Whether the current run owns the node's horizontal scale channel.
    private boolean animateScaleX;

    /// Whether the current run owns the node's vertical scale channel.
    private boolean animateScaleY;

    /// Whether the current run owns the node's horizontal translation channel.
    private boolean animateTranslateX;

    /// Whether the current run owns the node's vertical translation channel.
    private boolean animateTranslateY;

    /// Whether this transition has been configured at least once.
    private boolean configured;

    /// Creates a transition for a node.
    ///
    /// @param node the node whose visual channels are animated
    /// @throws NullPointerException if `node` is `null`
    public M3NodeTransition(Node node) {
        this.node = Objects.requireNonNull(node, "node");
    }

    /// Reconfigures all supported visual channels from their current values.
    ///
    /// A running transition contributes each channel's current velocity to the corresponding new channel. A stopped
    /// transition starts every channel with zero velocity. Opacity output is restricted to the closed unit interval;
    /// scale and translation values are not clamped.
    ///
    /// @param spec             the motion specification for the next run
    /// @param targetOpacity    the target opacity before output clamping
    /// @param targetScaleX     the target horizontal scale
    /// @param targetScaleY     the target vertical scale
    /// @param targetTranslateX the target horizontal translation
    /// @param targetTranslateY the target vertical translation
    /// @throws NullPointerException     if `spec` is `null`
    /// @throws IllegalArgumentException if a target or current node channel is non-finite
    public void configure(
            M3MotionSpec spec,
            double targetOpacity,
            double targetScaleX,
            double targetScaleY,
            double targetTranslateX,
            double targetTranslateY
    ) {
        M3MotionSpec checkedSpec = Objects.requireNonNull(spec, "spec");
        if (!Double.isFinite(targetOpacity)
                || !Double.isFinite(targetScaleX)
                || !Double.isFinite(targetScaleY)
                || !Double.isFinite(targetTranslateX)
                || !Double.isFinite(targetTranslateY)) {
            throw new IllegalArgumentException("target node channels must be finite");
        }

        double currentOpacity = node.getOpacity();
        double currentScaleX = node.getScaleX();
        double currentScaleY = node.getScaleY();
        double currentTranslateX = node.getTranslateX();
        double currentTranslateY = node.getTranslateY();
        if (!Double.isFinite(currentOpacity)
                || !Double.isFinite(currentScaleX)
                || !Double.isFinite(currentScaleY)
                || !Double.isFinite(currentTranslateX)
                || !Double.isFinite(currentTranslateY)) {
            throw new IllegalArgumentException("current node channels must be finite");
        }

        double previousElapsedSeconds = getStatus() == Animation.Status.STOPPED
                ? Double.POSITIVE_INFINITY
                : Math.max(0.0, getCurrentTime().toSeconds());
        stop();

        opacityChannel.configure(currentOpacity, targetOpacity, checkedSpec, previousElapsedSeconds);
        scaleXChannel.configure(currentScaleX, targetScaleX, checkedSpec, previousElapsedSeconds);
        scaleYChannel.configure(currentScaleY, targetScaleY, checkedSpec, previousElapsedSeconds);
        translateXChannel.configure(currentTranslateX, targetTranslateX, checkedSpec, previousElapsedSeconds);
        translateYChannel.configure(currentTranslateY, targetTranslateY, checkedSpec, previousElapsedSeconds);

        animateOpacity = ownsChannel(currentOpacity, targetOpacity, opacityChannel);
        animateScaleX = ownsChannel(currentScaleX, targetScaleX, scaleXChannel);
        animateScaleY = ownsChannel(currentScaleY, targetScaleY, scaleYChannel);
        animateTranslateX = ownsChannel(currentTranslateX, targetTranslateX, translateXChannel);
        animateTranslateY = ownsChannel(currentTranslateY, targetTranslateY, translateYChannel);

        runDurationSeconds = Math.max(
                Math.max(
                        opacityChannel.getDurationSeconds(),
                        Math.max(scaleXChannel.getDurationSeconds(), scaleYChannel.getDurationSeconds())
                ),
                Math.max(translateXChannel.getDurationSeconds(), translateYChannel.getDurationSeconds())
        );
        configured = true;
        setCycleDuration(Duration.seconds(runDurationSeconds));
        setInterpolator(Interpolator.LINEAR);
    }

    /// Applies the scalar channels owned by the current run for the current pulse.
    @Override
    protected void interpolate(double fraction) {
        if (!configured) {
            return;
        }

        double elapsedSeconds = fraction >= 1.0 || runDurationSeconds <= 0.0
                ? Double.POSITIVE_INFINITY
                : Math.max(0.0, fraction) * runDurationSeconds;

        if (animateOpacity) {
            double opacity = clampOpacity(opacityChannel.valueAt(elapsedSeconds));
            if (Double.compare(node.getOpacity(), opacity) != 0) {
                node.setOpacity(opacity);
            }
        }

        if (animateScaleX) {
            double scaleX = scaleXChannel.valueAt(elapsedSeconds);
            if (Double.compare(node.getScaleX(), scaleX) != 0) {
                node.setScaleX(scaleX);
            }
        }

        if (animateScaleY) {
            double scaleY = scaleYChannel.valueAt(elapsedSeconds);
            if (Double.compare(node.getScaleY(), scaleY) != 0) {
                node.setScaleY(scaleY);
            }
        }

        if (animateTranslateX) {
            double translateX = translateXChannel.valueAt(elapsedSeconds);
            if (Double.compare(node.getTranslateX(), translateX) != 0) {
                node.setTranslateX(translateX);
            }
        }

        if (animateTranslateY) {
            double translateY = translateYChannel.valueAt(elapsedSeconds);
            if (Double.compare(node.getTranslateY(), translateY) != 0) {
                node.setTranslateY(translateY);
            }
        }
    }

    /// Restricts opacity to the range accepted by JavaFX rendering.
    private static double clampOpacity(double opacity) {
        return Math.max(0.0, Math.min(1.0, opacity));
    }

    /// Returns whether a configured scalar channel must write its node property during the current run.
    private static boolean ownsChannel(double currentValue, double targetValue, M3ScalarChannel channel) {
        return Double.compare(currentValue, targetValue) != 0 || channel.getDurationSeconds() > 0.0;
    }
}
