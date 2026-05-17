// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3ProgressIndicator].
@NotNullByDefault
public class M3ProgressIndicatorSkin extends SkinBase<M3ProgressIndicator> {
    /// The duration used when determinate progress values change.
    private static final Duration DETERMINATE_DURATION = M3Motion.MEDIUM1;

    /// The duration of one indeterminate circular sweep.
    private static final Duration INDETERMINATE_DURATION = Duration.millis(1332.0);

    /// The shortest visible sweep used by indeterminate progress.
    private static final double INDETERMINATE_MIN_SWEEP = 42.0;

    /// The longest visible sweep used by indeterminate progress.
    private static final double INDETERMINATE_MAX_SWEEP = 96.0;

    /// The first visible phase used when indeterminate progress starts.
    private static final double INDETERMINATE_START_PHASE = 0.25;

    /// The track circle.
    private final Circle track = new Circle();

    /// The progress arc.
    private final Arc indicator = new Arc();

    /// The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition timeline.
    private final Timeline determinateAnimation = new Timeline();

    /// The animated phase used by indeterminate progress.
    private final DoubleProperty indeterminatePhase =
            new SimpleDoubleProperty(this, "indeterminatePhase", INDETERMINATE_START_PHASE);

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Requests layout after animation ticks.
    private final InvalidationListener animationInvalidation =
            observable -> getSkinnable().requestLayout();

    /// Updates animations when the public progress value changes.
    private final InvalidationListener progressInvalidation = observable -> updateProgressAnimation(true);

    /// Requests layout after size-related token changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a progress indicator skin.
    public M3ProgressIndicatorSkin(M3ProgressIndicator control) {
        super(control);
        track.getStyleClass().add("track");
        indicator.getStyleClass().add("indicator");
        track.setManaged(false);
        indicator.setManaged(false);
        track.setFill(Color.TRANSPARENT);
        indicator.setFill(Color.TRANSPARENT);
        track.setStrokeLineCap(StrokeLineCap.ROUND);
        indicator.setStrokeLineCap(StrokeLineCap.ROUND);
        indicator.setType(ArcType.OPEN);
        getChildren().addAll(track, indicator);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePhase.addListener(animationInvalidation);
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminatePhase, INDETERMINATE_START_PHASE, M3Motion.LINEAR)
                ),
                new KeyFrame(
                        INDETERMINATE_DURATION,
                        new KeyValue(indeterminatePhase, 1.0, M3Motion.LINEAR)
                )
        );

        control.progressProperty().addListener(progressInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        updateProgressAnimation(false);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3ProgressIndicator progressIndicator = getSkinnable();
        determinateAnimation.stop();
        indeterminateAnimation.stop();
        displayedProgress.removeListener(animationInvalidation);
        indeterminatePhase.removeListener(animationInvalidation);
        progressIndicator.progressProperty().removeListener(progressInvalidation);
        progressIndicator.trackThicknessProperty().removeListener(layoutInvalidation);
        progressIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        super.dispose();
    }

    /// Lays out the circular track and indicator arc.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double size = Math.min(width, height);
        double strokeWidth = Math.min(getSkinnable().getTrackThickness(), size);
        double radius = Math.max(0.0, (size - strokeWidth) / 2.0);
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        track.setCenterX(centerX);
        track.setCenterY(centerY);
        track.setRadius(radius);
        track.setStrokeWidth(strokeWidth);

        indicator.setCenterX(centerX);
        indicator.setCenterY(centerY);
        indicator.setRadiusX(radius);
        indicator.setRadiusY(radius);
        indicator.setStrokeWidth(strokeWidth);
        updateIndicatorArc();
    }

    /// Updates the visible arc for determinate or indeterminate progress.
    private void updateIndicatorArc() {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            track.setVisible(false);
            indicator.setStartAngle(indeterminateStartAngle(indeterminatePhase.get()));
            indicator.setLength(-indeterminateSweep(indeterminatePhase.get()));
            return;
        }

        track.setVisible(true);
        indicator.setStartAngle(90.0);
        indicator.setLength(-360.0 * displayedProgress.get());
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
                indeterminateAnimation.play();
            }
        } else {
            indeterminateAnimation.stop();
            indeterminatePhase.set(INDETERMINATE_START_PHASE);
            animateDisplayedProgress(clamp(progress), animateDeterminateProgress);
        }
    }

    /// Animates the displayed determinate progress value.
    private void animateDisplayedProgress(double targetProgress, boolean animate) {
        determinateAnimation.stop();
        if (!animate) {
            displayedProgress.set(targetProgress);
            return;
        }

        determinateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        DETERMINATE_DURATION,
                        new KeyValue(displayedProgress, targetProgress, M3Motion.STANDARD)
                )
        );
        determinateAnimation.playFromStart();
    }

    /// Returns the initial displayed progress value for a public progress value.
    private static double initialDisplayedProgress(double progress) {
        return progress == M3ProgressIndicator.INDETERMINATE_PROGRESS ? 0.0 : clamp(progress);
    }

    /// Returns the animated start angle for an indeterminate progress phase.
    private static double indeterminateStartAngle(double phase) {
        return 90.0 - 360.0 * phase;
    }

    /// Returns the animated sweep for an indeterminate progress phase.
    private static double indeterminateSweep(double phase) {
        double wave = 0.5 - Math.cos(phase * Math.PI * 2.0) / 2.0;
        return INDETERMINATE_MIN_SWEEP
                + (INDETERMINATE_MAX_SWEEP - INDETERMINATE_MIN_SWEEP) * wave;
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
