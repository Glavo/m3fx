// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.scene.Node;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3SpringParameters;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Reusable finite transition for a node's opacity, scale, and translation channels.
@NotNullByDefault
public final class M3NodeTransition extends M3FiniteTransition {
    /// The fraction interval used to estimate velocity for duration-based fallback curves.
    private static final double VELOCITY_SAMPLE_FRACTION = 1.0e-4;

    /// The opacity delta below which a physical spring is visually settled.
    private static final double OPACITY_VISIBILITY_THRESHOLD = 1.0e-2;

    /// The scale delta below which a physical spring changes rendered geometry by less than a subpixel.
    private static final double SCALE_VISIBILITY_THRESHOLD = 5.0e-4;

    /// The translation delta below which a physical spring changes position by less than half a logical pixel.
    private static final double TRANSLATION_VISIBILITY_THRESHOLD = 5.0e-1;

    /// The shortest non-zero spring run accepted by a JavaFX transition, in seconds.
    private static final double MIN_SPRING_DURATION_SECONDS = 1.0e-3;

    /// The node whose visual channels are animated.
    private final Node node;

    /// The motion specification configured for the current or most recent run.
    private @Nullable M3MotionSpec motionSpec;

    /// The physical parameters used by the current spring run, or `null` for duration-based interpolation.
    private @Nullable M3SpringParameters springParameters;

    /// The duration of the longest channel in the current run, in seconds.
    private double runDurationSeconds;

    /// The opacity at the beginning of the current transition.
    private double startOpacity;

    /// The opacity at the end of the current transition.
    private double targetOpacity;

    /// The opacity velocity at the beginning of the current transition, in units per second.
    private double initialOpacityVelocity;

    /// The time at which the opacity channel settles, in seconds.
    private double opacityDurationSeconds;

    /// The horizontal scale at the beginning of the current transition.
    private double startScaleX;

    /// The horizontal scale at the end of the current transition.
    private double targetScaleX;

    /// The horizontal scale velocity at the beginning of the current transition, in units per second.
    private double initialScaleXVelocity;

    /// The time at which the horizontal scale channel settles, in seconds.
    private double scaleXDurationSeconds;

    /// The vertical scale at the beginning of the current transition.
    private double startScaleY;

    /// The vertical scale at the end of the current transition.
    private double targetScaleY;

    /// The vertical scale velocity at the beginning of the current transition, in units per second.
    private double initialScaleYVelocity;

    /// The time at which the vertical scale channel settles, in seconds.
    private double scaleYDurationSeconds;

    /// The horizontal translation at the beginning of the current transition.
    private double startTranslateX;

    /// The horizontal translation at the end of the current transition.
    private double targetTranslateX;

    /// The horizontal translation velocity at the beginning of the current transition, in logical pixels per second.
    private double initialTranslateXVelocity;

    /// The time at which the horizontal translation channel settles, in seconds.
    private double translateXDurationSeconds;

    /// The vertical translation at the beginning of the current transition.
    private double startTranslateY;

    /// The vertical translation at the end of the current transition.
    private double targetTranslateY;

    /// The vertical translation velocity at the beginning of the current transition, in logical pixels per second.
    private double initialTranslateYVelocity;

    /// The time at which the vertical translation channel settles, in seconds.
    private double translateYDurationSeconds;

    /// Creates a transition for a node.
    ///
    /// @param node the node whose visual channels are animated
    /// @throws NullPointerException if `node` is `null`
    public M3NodeTransition(Node node) {
        this.node = Objects.requireNonNull(node, "node");
    }

    /// Reconfigures all supported visual channels from their current values.
    ///
    /// @param spec             the duration and easing specification
    /// @param targetOpacity    the target opacity
    /// @param targetScaleX     the target horizontal scale
    /// @param targetScaleY     the target vertical scale
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
        M3MotionSpec checkedSpec = Objects.requireNonNull(spec, "spec");
        double opacityVelocity = currentVelocity(
                startOpacity, targetOpacity, initialOpacityVelocity, opacityDurationSeconds
        );
        double scaleXVelocity = currentVelocity(
                startScaleX, targetScaleX, initialScaleXVelocity, scaleXDurationSeconds
        );
        double scaleYVelocity = currentVelocity(
                startScaleY, targetScaleY, initialScaleYVelocity, scaleYDurationSeconds
        );
        double translateXVelocity = currentVelocity(
                startTranslateX, targetTranslateX, initialTranslateXVelocity, translateXDurationSeconds
        );
        double translateYVelocity = currentVelocity(
                startTranslateY, targetTranslateY, initialTranslateYVelocity, translateYDurationSeconds
        );

        stop();
        motionSpec = checkedSpec;
        springParameters = checkedSpec.springParameters();
        startOpacity = node.getOpacity();
        this.targetOpacity = targetOpacity;
        initialOpacityVelocity = opacityVelocity;
        startScaleX = node.getScaleX();
        this.targetScaleX = targetScaleX;
        initialScaleXVelocity = scaleXVelocity;
        startScaleY = node.getScaleY();
        this.targetScaleY = targetScaleY;
        initialScaleYVelocity = scaleYVelocity;
        startTranslateX = node.getTranslateX();
        this.targetTranslateX = targetTranslateX;
        initialTranslateXVelocity = translateXVelocity;
        startTranslateY = node.getTranslateY();
        this.targetTranslateY = targetTranslateY;
        initialTranslateYVelocity = translateYVelocity;

        configureDurations(checkedSpec);
        setCycleDuration(Duration.seconds(runDurationSeconds));
        setInterpolator(Interpolator.LINEAR);
    }

    /// Applies spring or fallback interpolation to channels that change during the current transition.
    @Override
    protected void interpolate(double fraction) {
        @Nullable M3MotionSpec spec = motionSpec;
        if (spec == null) {
            return;
        }

        if (isChanging(startOpacity, targetOpacity, initialOpacityVelocity)) {
            node.setOpacity(clampOpacity(interpolate(
                    spec,
                    startOpacity,
                    targetOpacity,
                    initialOpacityVelocity,
                    opacityDurationSeconds,
                    fraction
            )));
        }
        if (isChanging(startScaleX, targetScaleX, initialScaleXVelocity)) {
            node.setScaleX(interpolate(
                    spec,
                    startScaleX,
                    targetScaleX,
                    initialScaleXVelocity,
                    scaleXDurationSeconds,
                    fraction
            ));
        }
        if (isChanging(startScaleY, targetScaleY, initialScaleYVelocity)) {
            node.setScaleY(interpolate(
                    spec,
                    startScaleY,
                    targetScaleY,
                    initialScaleYVelocity,
                    scaleYDurationSeconds,
                    fraction
            ));
        }
        if (isChanging(startTranslateX, targetTranslateX, initialTranslateXVelocity)) {
            node.setTranslateX(interpolate(
                    spec,
                    startTranslateX,
                    targetTranslateX,
                    initialTranslateXVelocity,
                    translateXDurationSeconds,
                    fraction
            ));
        }
        if (isChanging(startTranslateY, targetTranslateY, initialTranslateYVelocity)) {
            node.setTranslateY(interpolate(
                    spec,
                    startTranslateY,
                    targetTranslateY,
                    initialTranslateYVelocity,
                    translateYDurationSeconds,
                    fraction
            ));
        }
    }

    /// Computes the actual spring settling time of every channel and the enclosing transition.
    private void configureDurations(M3MotionSpec spec) {
        double fallbackDurationSeconds = spec.duration().toSeconds();
        @Nullable M3SpringParameters spring = springParameters;
        if (spring == null) {
            runDurationSeconds = fallbackDurationSeconds;
            opacityDurationSeconds = fallbackDurationSeconds;
            scaleXDurationSeconds = fallbackDurationSeconds;
            scaleYDurationSeconds = fallbackDurationSeconds;
            translateXDurationSeconds = fallbackDurationSeconds;
            translateYDurationSeconds = fallbackDurationSeconds;
            return;
        }

        opacityDurationSeconds = estimateChannelDuration(
                startOpacity,
                targetOpacity,
                initialOpacityVelocity,
                OPACITY_VISIBILITY_THRESHOLD,
                spring,
                fallbackDurationSeconds
        );
        scaleXDurationSeconds = estimateChannelDuration(
                startScaleX,
                targetScaleX,
                initialScaleXVelocity,
                SCALE_VISIBILITY_THRESHOLD,
                spring,
                fallbackDurationSeconds
        );
        scaleYDurationSeconds = estimateChannelDuration(
                startScaleY,
                targetScaleY,
                initialScaleYVelocity,
                SCALE_VISIBILITY_THRESHOLD,
                spring,
                fallbackDurationSeconds
        );
        translateXDurationSeconds = estimateChannelDuration(
                startTranslateX,
                targetTranslateX,
                initialTranslateXVelocity,
                TRANSLATION_VISIBILITY_THRESHOLD,
                spring,
                fallbackDurationSeconds
        );
        translateYDurationSeconds = estimateChannelDuration(
                startTranslateY,
                targetTranslateY,
                initialTranslateYVelocity,
                TRANSLATION_VISIBILITY_THRESHOLD,
                spring,
                fallbackDurationSeconds
        );
        runDurationSeconds = Math.max(
                Math.max(opacityDurationSeconds, Math.max(scaleXDurationSeconds, scaleYDurationSeconds)),
                Math.max(translateXDurationSeconds, translateYDurationSeconds)
        );
    }

    /// Returns the current physical velocity of one channel before this transition is retargeted.
    private double currentVelocity(
            double start,
            double target,
            double initialVelocity,
            double channelDurationSeconds
    ) {
        @Nullable M3MotionSpec spec = motionSpec;
        if (spec == null || getStatus() == Animation.Status.STOPPED) {
            return 0.0;
        }

        double durationSeconds = springParameters == null ? spec.duration().toSeconds() : channelDurationSeconds;
        if (durationSeconds <= 0.0) {
            return 0.0;
        }
        double elapsedSeconds = Math.max(0.0, getCurrentTime().toSeconds());
        if (elapsedSeconds >= durationSeconds) {
            return 0.0;
        }
        @Nullable M3SpringParameters spring = springParameters;
        if (spring != null) {
            return M3SpringSolver.velocity(start, target, initialVelocity, elapsedSeconds, spring);
        }

        double fraction = elapsedSeconds / durationSeconds;
        double lowerFraction = Math.max(0.0, fraction - VELOCITY_SAMPLE_FRACTION);
        double upperFraction = Math.min(1.0, fraction + VELOCITY_SAMPLE_FRACTION);
        if (Double.compare(lowerFraction, upperFraction) == 0) {
            return 0.0;
        }
        double lowerValue = spec.interpolator().interpolate(start, target, lowerFraction);
        double upperValue = spec.interpolator().interpolate(start, target, upperFraction);
        return (upperValue - lowerValue) / ((upperFraction - lowerFraction) * durationSeconds);
    }

    /// Interpolates one channel using the configured physical spring or duration-based fallback.
    private double interpolate(
            M3MotionSpec spec,
            double start,
            double target,
            double initialVelocity,
            double channelDurationSeconds,
            double fraction
    ) {
        if (fraction >= 1.0 || channelDurationSeconds <= 0.0) {
            return target;
        }
        @Nullable M3SpringParameters spring = spec.springParameters();
        if (spring == null) {
            return spec.interpolator().interpolate(start, target, fraction);
        }
        double elapsedSeconds = Math.max(0.0, fraction) * runDurationSeconds;
        if (elapsedSeconds >= channelDurationSeconds) {
            return target;
        }
        return M3SpringSolver.value(
                start,
                target,
                initialVelocity,
                elapsedSeconds,
                spring
        );
    }

    /// Returns one spring channel's finite settling duration.
    private static double estimateChannelDuration(
            double start,
            double target,
            double initialVelocity,
            double visibilityThreshold,
            M3SpringParameters spring,
            double fallbackDurationSeconds
    ) {
        if (Double.compare(start, target) == 0 && Double.compare(initialVelocity, 0.0) == 0) {
            return 0.0;
        }
        double durationSeconds = M3SpringSolver.estimateDurationSeconds(
                start - target,
                initialVelocity,
                visibilityThreshold,
                spring
        );
        if (!Double.isFinite(durationSeconds)) {
            return fallbackDurationSeconds;
        }
        return durationSeconds <= 0.0 ? 0.0 : Math.max(MIN_SPRING_DURATION_SECONDS, durationSeconds);
    }

    /// Returns whether a channel has displacement or retained spring velocity to animate.
    private boolean isChanging(double start, double target, double initialVelocity) {
        return Double.compare(start, target) != 0
                || springParameters != null && Double.compare(initialVelocity, 0.0) != 0;
    }

    /// Restricts opacity to the range accepted by JavaFX rendering.
    private static double clampOpacity(double opacity) {
        return Math.max(0.0, Math.min(1.0, opacity));
    }
}
