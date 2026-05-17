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
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3ProgressBar].
@NotNullByDefault
public class M3ProgressBarSkin extends SkinBase<M3ProgressBar> {
    /// The duration used when determinate progress values change.
    private static final Duration DETERMINATE_DURATION = M3Motion.MEDIUM1;

    /// The duration of one indeterminate track sweep.
    private static final Duration INDETERMINATE_DURATION = Duration.millis(1400.0);

    /// The minimum width used by an indeterminate segment.
    private static final double MIN_INDETERMINATE_SEGMENT_WIDTH = 24.0;

    /// The first visible phase used when indeterminate progress starts.
    private static final double INDETERMINATE_START_POSITION = 0.18;

    /// The clipped visual container.
    private final Pane container = new Pane();

    /// The clip that keeps the bar inside the track bounds.
    private final Rectangle clip = new Rectangle();

    /// The track rectangle.
    private final Rectangle track = new Rectangle();

    /// The progress bar rectangle.
    private final Rectangle bar = new Rectangle();

    /// The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition timeline.
    private final Timeline determinateAnimation = new Timeline();

    /// The animated position of the indeterminate segment.
    private final DoubleProperty indeterminatePosition =
            new SimpleDoubleProperty(this, "indeterminatePosition", INDETERMINATE_START_POSITION);

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Requests layout after animation ticks.
    private final InvalidationListener animationInvalidation =
            observable -> getSkinnable().requestLayout();

    /// Updates animations when the public progress value changes.
    private final InvalidationListener progressInvalidation = observable -> updateProgressAnimation(true);

    /// Requests layout after size-related token changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a progress bar skin.
    public M3ProgressBarSkin(M3ProgressBar control) {
        super(control);
        container.getStyleClass().add("m3-progress-bar-container");
        container.setManaged(false);
        container.setClip(clip);
        track.getStyleClass().add("track");
        bar.getStyleClass().add("bar");
        track.setManaged(false);
        bar.setManaged(false);
        container.getChildren().addAll(track, bar);
        getChildren().add(container);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePosition.addListener(animationInvalidation);
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminatePosition, INDETERMINATE_START_POSITION, M3Motion.LINEAR)
                ),
                new KeyFrame(
                        INDETERMINATE_DURATION,
                        new KeyValue(indeterminatePosition, 1.0, M3Motion.LINEAR)
                )
        );

        control.progressProperty().addListener(progressInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.trackShapeProperty().addListener(layoutInvalidation);
        updateProgressAnimation(false);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3ProgressBar progressBar = getSkinnable();
        determinateAnimation.stop();
        indeterminateAnimation.stop();
        displayedProgress.removeListener(animationInvalidation);
        indeterminatePosition.removeListener(animationInvalidation);
        progressBar.progressProperty().removeListener(progressInvalidation);
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
        double radius = resolvedTrackRadius(progressBar, thickness);

        container.resizeRelocate(x, trackY, width, thickness);
        clip.setWidth(width);
        clip.setHeight(thickness);
        clip.setArcWidth(radius * 2.0);
        clip.setArcHeight(radius * 2.0);
        layoutRectangle(track, 0.0, 0.0, width, thickness, radius);
        layoutBar(0.0, 0.0, width, thickness, radius);
    }

    /// Lays out the determinate or indeterminate bar region.
    private void layoutBar(double x, double y, double width, double height, double radius) {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            double segmentWidth = Math.max(MIN_INDETERMINATE_SEGMENT_WIDTH, width * 0.32);
            double segmentX = x - segmentWidth + (width + segmentWidth) * indeterminatePosition.get();
            bar.setVisible(true);
            layoutRectangle(bar, segmentX, y, segmentWidth, height, radius);
            return;
        }

        double progressWidth = width * displayedProgress.get();
        bar.setVisible(progressWidth > 0.0);
        layoutRectangle(bar, x, y, progressWidth, height, Math.min(radius, progressWidth / 2.0));
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
                indeterminateAnimation.play();
            }
        } else {
            indeterminateAnimation.stop();
            indeterminatePosition.set(INDETERMINATE_START_POSITION);
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
        return progress == M3ProgressBar.INDETERMINATE_PROGRESS ? 0.0 : clamp(progress);
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Returns a track radius that can be rendered cleanly for the current thickness.
    private static double resolvedTrackRadius(M3ProgressBar progressBar, double thickness) {
        return Math.min(progressBar.getTrackShape(), thickness / 2.0);
    }

    /// Lays out a progress rectangle with a clean resolved corner radius.
    private static void layoutRectangle(
            Rectangle rectangle,
            double x,
            double y,
            double width,
            double height,
            double radius
    ) {
        rectangle.setX(x);
        rectangle.setY(y);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
        rectangle.setArcWidth(radius * 2.0);
        rectangle.setArcHeight(radius * 2.0);
    }
}
