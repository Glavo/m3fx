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
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3ProgressBar].
@NotNullByDefault
public class M3ProgressBarSkin extends SkinBase<M3ProgressBar> {
    /// The duration of one indeterminate track sweep.
    private static final Duration INDETERMINATE_DURATION = Duration.millis(1400.0);

    /// The minimum width used by an indeterminate segment.
    private static final double MIN_INDETERMINATE_SEGMENT_WIDTH = 24.0;

    /// The track region.
    private final Region track = new Region();

    /// The progress bar region.
    private final Region bar = new Region();

    /// The animated position of the indeterminate segment.
    private final DoubleProperty indeterminatePosition = new SimpleDoubleProperty(this, "indeterminatePosition");

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Requests layout after progress or token changes.
    private final InvalidationListener layoutInvalidation = observable -> {
        updateIndeterminateAnimation();
        getSkinnable().requestLayout();
    };

    /// Creates a progress bar skin.
    public M3ProgressBarSkin(M3ProgressBar control) {
        super(control);
        track.getStyleClass().add("track");
        bar.getStyleClass().add("bar");
        track.setManaged(false);
        bar.setManaged(false);
        getChildren().addAll(track, bar);

        indeterminatePosition.addListener(observable -> control.requestLayout());
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminatePosition, 0.0, Interpolator.LINEAR)
                ),
                new KeyFrame(
                        INDETERMINATE_DURATION,
                        new KeyValue(indeterminatePosition, 1.0, Interpolator.LINEAR)
                )
        );

        control.progressProperty().addListener(layoutInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.trackShapeProperty().addListener(layoutInvalidation);
        updateIndeterminateAnimation();
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3ProgressBar progressBar = getSkinnable();
        indeterminateAnimation.stop();
        progressBar.progressProperty().removeListener(layoutInvalidation);
        progressBar.trackThicknessProperty().removeListener(layoutInvalidation);
        progressBar.trackShapeProperty().removeListener(layoutInvalidation);
        super.dispose();
    }

    /// Lays out the track and progress segment.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3ProgressBar progressBar = getSkinnable();
        double thickness = Math.min(progressBar.getTrackThickness(), height);
        double trackY = y + (height - thickness) / 2.0;
        String shape = formatPixels(progressBar.getTrackShape());
        String style = "-fx-background-radius: " + shape + "; -fx-pref-height: " + formatPixels(thickness) + ";";

        track.setStyle(style);
        bar.setStyle(style);
        track.resizeRelocate(x, trackY, width, thickness);
        layoutBar(x, trackY, width, thickness);
    }

    /// Lays out the determinate or indeterminate bar region.
    private void layoutBar(double x, double y, double width, double height) {
        double progress = getSkinnable().getProgress();
        if (progress == ProgressIndicator.INDETERMINATE_PROGRESS) {
            double segmentWidth = Math.max(MIN_INDETERMINATE_SEGMENT_WIDTH, width * 0.32);
            double segmentX = x - segmentWidth + (width + segmentWidth) * indeterminatePosition.get();
            bar.resizeRelocate(segmentX, y, segmentWidth, height);
            return;
        }

        double progressWidth = width * clamp(progress);
        bar.resizeRelocate(x, y, progressWidth, height);
    }

    /// Starts or stops the indeterminate animation for the current progress value.
    private void updateIndeterminateAnimation() {
        if (getSkinnable().getProgress() == ProgressIndicator.INDETERMINATE_PROGRESS) {
            if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
                indeterminateAnimation.play();
            }
        } else {
            indeterminateAnimation.stop();
            indeterminatePosition.set(0.0);
        }
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }
}
