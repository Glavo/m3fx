// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3ProgressIndicator].
@NotNullByDefault
public class M3ProgressIndicatorSkin extends SkinBase<M3ProgressIndicator> {
    /// The duration of one indeterminate circular sweep.
    private static final Duration INDETERMINATE_DURATION = Duration.millis(1100.0);

    /// The default stroke width for circular progress visuals.
    private static final double STROKE_WIDTH = 4.0;

    /// The visible arc length used by indeterminate progress.
    private static final double INDETERMINATE_ARC_LENGTH = -96.0;

    /// The track circle.
    private final Circle track = new Circle();

    /// The progress arc.
    private final Arc indicator = new Arc();

    /// The animated rotation used by indeterminate progress.
    private final DoubleProperty indeterminateRotation = new SimpleDoubleProperty(this, "indeterminateRotation");

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Requests layout after progress or token changes.
    private final InvalidationListener layoutInvalidation = observable -> {
        updateIndeterminateAnimation();
        getSkinnable().requestLayout();
    };

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

        indeterminateRotation.addListener(observable -> control.requestLayout());
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminateRotation, 0.0, Interpolator.LINEAR)
                ),
                new KeyFrame(
                        INDETERMINATE_DURATION,
                        new KeyValue(indeterminateRotation, 360.0, Interpolator.LINEAR)
                )
        );

        control.progressProperty().addListener(layoutInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        updateIndeterminateAnimation();
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3ProgressIndicator progressIndicator = getSkinnable();
        indeterminateAnimation.stop();
        progressIndicator.progressProperty().removeListener(layoutInvalidation);
        progressIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        super.dispose();
    }

    /// Lays out the circular track and indicator arc.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double size = Math.min(Math.min(width, height), getSkinnable().getIndicatorSize());
        double radius = Math.max(0.0, (size - STROKE_WIDTH) / 2.0);
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        track.setCenterX(centerX);
        track.setCenterY(centerY);
        track.setRadius(radius);
        track.setStrokeWidth(STROKE_WIDTH);

        indicator.setCenterX(centerX);
        indicator.setCenterY(centerY);
        indicator.setRadiusX(radius);
        indicator.setRadiusY(radius);
        indicator.setStrokeWidth(STROKE_WIDTH);
        updateIndicatorArc();
    }

    /// Updates the visible arc for determinate or indeterminate progress.
    private void updateIndicatorArc() {
        double progress = getSkinnable().getProgress();
        if (progress == ProgressIndicator.INDETERMINATE_PROGRESS) {
            indicator.setStartAngle(90.0 - indeterminateRotation.get());
            indicator.setLength(INDETERMINATE_ARC_LENGTH);
            return;
        }

        indicator.setStartAngle(90.0);
        indicator.setLength(-360.0 * clamp(progress));
    }

    /// Starts or stops the indeterminate animation for the current progress value.
    private void updateIndeterminateAnimation() {
        if (getSkinnable().getProgress() == ProgressIndicator.INDETERMINATE_PROGRESS) {
            if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
                indeterminateAnimation.play();
            }
        } else {
            indeterminateAnimation.stop();
            indeterminateRotation.set(0.0);
        }
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
