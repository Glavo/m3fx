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
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3ProgressBar].
@NotNullByDefault
public class M3ProgressBarSkin extends SkinBase<M3ProgressBar> {
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

    /// The second track rectangle used when a moving wavy segment splits the track.
    private final Rectangle secondaryTrack = new Rectangle();

    /// The progress bar rectangle.
    private final Rectangle bar = new Rectangle();

    /// The wavy active progress path used by expressive progress bars.
    private final Path waveBar = new Path();

    /// The stop indicator rendered at the end of an expressive progress bar track.
    private final Circle stop = new Circle();

    // The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition timeline.
    private final Timeline determinateAnimation = new Timeline();

    // The animated position of the indeterminate segment.
    private final DoubleProperty indeterminatePosition =
            new SimpleDoubleProperty(this, "indeterminatePosition", INDETERMINATE_START_POSITION);

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Requests layout after animation ticks.
    private final InvalidationListener animationInvalidation =
            observable -> getSkinnable().requestLayout();

    /// Updates animations when the public progress value changes.
    private final InvalidationListener progressInvalidation = observable -> updateProgressAnimation(true);

    /// Updates indeterminate animation state when global or node-local motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(getSkinnable(), () -> updateProgressAnimation(false));

    /// Requests layout after size-related token changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a progress bar skin.
    ///
    /// @param control the skinned progress bar
    public M3ProgressBarSkin(M3ProgressBar control) {
        super(control);
        container.getStyleClass().add("m3-progress-bar-container");
        container.setManaged(false);
        container.setClip(clip);
        track.getStyleClass().add("track");
        secondaryTrack.getStyleClass().addAll("track", "m3-progress-bar-secondary-track");
        bar.getStyleClass().add("bar");
        waveBar.getStyleClass().add("m3-progress-bar-wave");
        stop.getStyleClass().add("m3-progress-stop");
        track.setManaged(false);
        secondaryTrack.setManaged(false);
        bar.setManaged(false);
        waveBar.setManaged(false);
        stop.setManaged(false);
        waveBar.setFill(null);
        waveBar.setStrokeLineCap(StrokeLineCap.ROUND);
        container.getChildren().addAll(track, secondaryTrack, bar, waveBar, stop);
        getChildren().add(container);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePosition.addListener(animationInvalidation);
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);

        control.progressProperty().addListener(progressInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.trackShapeProperty().addListener(layoutInvalidation);
        control.waveAmplitudeProperty().addListener(layoutInvalidation);
        control.wavelengthProperty().addListener(layoutInvalidation);
        control.trackGapProperty().addListener(layoutInvalidation);
        control.stopSizeProperty().addListener(layoutInvalidation);
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
        motionSettingsObserver.dispose();
        progressBar.trackThicknessProperty().removeListener(layoutInvalidation);
        progressBar.trackShapeProperty().removeListener(layoutInvalidation);
        progressBar.waveAmplitudeProperty().removeListener(layoutInvalidation);
        progressBar.wavelengthProperty().removeListener(layoutInvalidation);
        progressBar.trackGapProperty().removeListener(layoutInvalidation);
        progressBar.stopSizeProperty().removeListener(layoutInvalidation);
        super.dispose();
    }

    /// Lays out the track and progress segment.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3ProgressBar progressBar = getSkinnable();
        double thickness = Math.min(progressBar.getTrackThickness(), height);
        double amplitude = Math.min(progressBar.getWaveAmplitude(), Math.max(0.0, (height - thickness) / 2.0));
        double visualHeight = Math.min(height, thickness + amplitude * 2.0);
        double trackY = y + (height - visualHeight) / 2.0;
        double radius = resolvedTrackRadius(progressBar, thickness);

        container.resizeRelocate(x, trackY, width, visualHeight);
        clip.setWidth(width);
        clip.setHeight(visualHeight);
        clip.setArcWidth(radius * 2.0);
        clip.setArcHeight(radius * 2.0);
        layoutBar(width, visualHeight, thickness, radius, amplitude);
    }

    /// Lays out the determinate or indeterminate bar region.
    private void layoutBar(double width, double height, double thickness, double radius, double amplitude) {
        double progress = getSkinnable().getProgress();
        if (amplitude > 0.0) {
            layoutWavyBar(width, height, thickness, radius, amplitude, progress);
            return;
        }

        waveBar.setVisible(false);
        stop.setVisible(false);
        secondaryTrack.setVisible(false);
        track.setVisible(true);
        layoutRectangle(track, 0.0, 0.0, width, thickness, radius);
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            double segmentWidth = Math.max(MIN_INDETERMINATE_SEGMENT_WIDTH, width * 0.32);
            double segmentX = -segmentWidth + (width + segmentWidth) * indeterminatePosition.get();
            bar.setVisible(true);
            layoutRectangle(bar, segmentX, 0.0, segmentWidth, thickness, radius);
            return;
        }

        double progressWidth = width * displayedProgress.get();
        bar.setVisible(progressWidth > 0.0);
        layoutRectangle(bar, 0.0, 0.0, progressWidth, thickness, Math.min(radius, progressWidth / 2.0));
    }

    /// Lays out the expressive wavy active path, separated track, and stop indicator.
    private void layoutWavyBar(
            double width,
            double height,
            double thickness,
            double radius,
            double amplitude,
            double progress
    ) {
        double centerY = height / 2.0;
        bar.setVisible(false);
        waveBar.setVisible(true);
        waveBar.setStrokeWidth(thickness);
        double effectiveGap = effectiveTrackGap(getSkinnable().getTrackGap(), thickness);

        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            stop.setVisible(false);
            double segmentWidth = Math.max(MIN_INDETERMINATE_SEGMENT_WIDTH, width * 0.32);
            double segmentX = -segmentWidth + (width + segmentWidth) * indeterminatePosition.get();
            double segmentEnd = segmentX + segmentWidth;
            layoutTrackSegment(track, 0.0, Math.min(width, segmentX - effectiveGap),
                    centerY, thickness, radius);
            layoutTrackSegment(secondaryTrack, Math.max(0.0, segmentEnd + effectiveGap),
                    width - Math.max(0.0, segmentEnd + effectiveGap), centerY, thickness, radius);
            layoutWavePath(waveBar, segmentX, segmentEnd, centerY, amplitude,
                    getSkinnable().getWavelength(), indeterminatePosition.get());
            return;
        }

        secondaryTrack.setVisible(false);
        double displayed = displayedProgress.get();
        double progressWidth = width * displayed;
        double activeAmplitude = amplitudeForProgress(displayed) * amplitude;
        layoutWavePath(waveBar, 0.0, progressWidth, centerY, activeAmplitude, getSkinnable().getWavelength(), 0.0);

        double stopDiameter = Math.min(thickness, getSkinnable().getStopSize());
        double stopLeft = Math.max(0.0, width - stopDiameter);
        double trackStart = Math.min(stopLeft, progressWidth + effectiveGap);
        double trackWidth = Math.max(0.0, stopLeft - effectiveGap - trackStart);
        layoutTrackSegment(track, trackStart, trackWidth, centerY, thickness, radius);

        double remaining = width - progressWidth;
        double visibleStopDiameter = Math.max(0.0, Math.min(stopDiameter, remaining - effectiveGap));
        stop.setVisible(visibleStopDiameter > 0.0);
        stop.setRadius(visibleStopDiameter / 2.0);
        stop.setCenterX(width - visibleStopDiameter / 2.0);
        stop.setCenterY(centerY);
    }

    /// Returns the resolved active wave amplitude for a determinate progress value.
    private static double amplitudeForProgress(double progress) {
        return progress <= 0.1 || progress >= 0.95 ? 0.0 : 1.0;
    }

    /// Lays out a sampled sine-wave progress path.
    private static void layoutWavePath(
            Path path,
            double startX,
            double endX,
            double centerY,
            double amplitude,
            double wavelength,
            double phase
    ) {
        path.getElements().clear();
        if (endX <= startX) {
            path.setVisible(false);
            return;
        }

        path.setVisible(true);
        double safeWavelength = Math.max(1.0, wavelength);
        int steps = Math.max(2, (int) Math.ceil((endX - startX) / 4.0));
        for (int i = 0; i <= steps; i++) {
            double fraction = (double) i / (double) steps;
            double x = startX + (endX - startX) * fraction;
            double angle = (x / safeWavelength + phase) * Math.PI * 2.0;
            double y = centerY + Math.sin(angle) * amplitude;
            if (i == 0) {
                path.getElements().add(new MoveTo(x, y));
            } else {
                path.getElements().add(new LineTo(x, y));
            }
        }
    }

    /// Returns the centerline gap that preserves the requested visible gap around round caps.
    private static double effectiveTrackGap(double gap, double thickness) {
        return Math.max(0.0, gap) + Math.max(0.0, thickness) / 2.0;
    }

    /// Lays out a track segment and hides it when its visible width is empty.
    private static void layoutTrackSegment(
            Rectangle rectangle,
            double x,
            double width,
            double centerY,
            double thickness,
            double radius
    ) {
        double visibleWidth = Math.max(0.0, width);
        rectangle.setVisible(visibleWidth > 0.0);
        layoutRectangle(rectangle, x, centerY - thickness / 2.0, visibleWidth, thickness,
                Math.min(radius, visibleWidth / 2.0));
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            startIndeterminateAnimation();
            getSkinnable().requestLayout();
        } else {
            indeterminateAnimation.stop();
            indeterminatePosition.set(INDETERMINATE_START_POSITION);
            animateDisplayedProgress(clamp(progress), animateDeterminateProgress);
        }
    }

    /// Starts the indeterminate linear segment loop.
    private void startIndeterminateAnimation() {
        indeterminateAnimation.stop();
        configureIndeterminateAnimation();
        indeterminateAnimation.playFromStart();
    }

    /// Configures the indeterminate sweep with the current owner behavior timing.
    private void configureIndeterminateAnimation() {
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminatePosition, INDETERMINATE_START_POSITION, M3Motion.LINEAR)
                ),
                new KeyFrame(
                        M3Animation.motionBehavior(getSkinnable()).linearProgressIndeterminateCycleDuration(),
                        new KeyValue(indeterminatePosition, 1.0, M3Motion.LINEAR)
                )
        );
    }

    /// Animates the displayed determinate progress value.
    private void animateDisplayedProgress(double targetProgress, boolean animate) {
        determinateAnimation.stop();
        if (!animate || M3Animation.shouldReduceMotion(getSkinnable())) {
            displayedProgress.set(targetProgress);
            return;
        }

        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        determinateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        spec.duration(),
                        new KeyValue(displayedProgress, targetProgress, spec.interpolator())
                )
        );
        M3Animation.playFromStart(getSkinnable(), determinateAnimation);
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
