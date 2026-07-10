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
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ProgressBar].
@NotNullByDefault
public class M3ProgressBarSkin extends SkinBase<M3ProgressBar> {
    /// The minimum width used by an indeterminate segment.
    private static final double MIN_INDETERMINATE_SEGMENT_WIDTH = 24.0;

    /// The first visible phase used when indeterminate progress starts.
    private static final double INDETERMINATE_START_POSITION = 0.18;

    /// The preferred distance between sampled points in a linear expressive wave.
    private static final double LINEAR_WAVE_SAMPLE_LENGTH = 4.0;

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

    /// The resolved progress bar width from the latest layout.
    private double resolvedWidth;

    /// The resolved progress bar visual height from the latest layout.
    private double resolvedHeight;

    /// The resolved track thickness from the latest layout.
    private double resolvedThickness;

    /// The resolved track corner radius from the latest layout.
    private double resolvedRadius;

    /// The resolved expressive wave amplitude from the latest layout.
    private double resolvedAmplitude;

    /// Whether the progress bar has received valid layout geometry.
    private boolean geometryReady;

    /// The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition.
    private final M3DoubleTransition determinateAnimation = new M3DoubleTransition(displayedProgress);

    /// The animated position of the indeterminate segment.
    private final DoubleProperty indeterminatePosition =
            new SimpleDoubleProperty(this, "indeterminatePosition", INDETERMINATE_START_POSITION);

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Updates internal progress geometry after animation ticks without invalidating parent layout.
    private final InvalidationListener animationInvalidation =
            observable -> updateAnimatedVisuals();

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
        double radius = Math.min(progressBar.getTrackShape(), thickness / 2.0);

        resolvedWidth = width;
        resolvedHeight = visualHeight;
        resolvedThickness = thickness;
        resolvedRadius = radius;
        resolvedAmplitude = amplitude;
        geometryReady = true;

        container.resizeRelocate(x, trackY, width, visualHeight);
        clip.setWidth(width);
        clip.setHeight(visualHeight);
        clip.setArcWidth(radius * 2.0);
        clip.setArcHeight(radius * 2.0);
        updateAnimatedVisuals();
    }

    /// Updates the animated progress visuals using geometry resolved by the latest layout pass.
    private void updateAnimatedVisuals() {
        if (!geometryReady) {
            return;
        }
        layoutBar(resolvedWidth, resolvedHeight, resolvedThickness, resolvedRadius, resolvedAmplitude);
    }

    /// Lays out the determinate or indeterminate bar region.
    private void layoutBar(double width, double height, double thickness, double radius, double amplitude) {
        double progress = getSkinnable().getProgress();
        if (amplitude > 0.0) {
            layoutWavyBar(width, height, thickness, radius, amplitude, progress);
            return;
        }

        waveBar.setVisible(false);
        double centerY = height / 2.0;
        double effectiveGap = Math.max(0.0, getSkinnable().getTrackGap()) + Math.max(0.0, thickness) / 2.0;
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            stop.setVisible(false);
            double segmentWidth = Math.max(MIN_INDETERMINATE_SEGMENT_WIDTH, width * 0.32);
            double segmentX = indeterminateSegmentX(width, segmentWidth);
            double segmentEnd = segmentX + segmentWidth;
            layoutTrackSegment(track, 0.0, Math.min(width, segmentX - effectiveGap),
                    centerY, thickness, radius);
            layoutTrackSegment(secondaryTrack, Math.max(0.0, segmentEnd + effectiveGap),
                    width - Math.max(0.0, segmentEnd + effectiveGap), centerY, thickness, radius);
            bar.setVisible(true);
            layoutRectangle(bar, segmentX, centerY - thickness / 2.0, segmentWidth, thickness, radius);
            return;
        }

        secondaryTrack.setVisible(false);
        double progressWidth = width * displayedProgress.get();
        bar.setVisible(progressWidth > 0.0);
        layoutRectangle(bar, 0.0, centerY - thickness / 2.0, progressWidth, thickness,
                Math.min(radius, progressWidth / 2.0));
        double stopDiameter = Math.min(thickness, getSkinnable().getStopSize());
        double remaining = width - progressWidth;
        double visibleStopDiameter = Math.max(0.0, Math.min(stopDiameter, remaining - effectiveGap));
        layoutDeterminateTrackAndStop(width, centerY, thickness, radius, effectiveGap, progressWidth,
                visibleStopDiameter);
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
        double effectiveGap = Math.max(0.0, getSkinnable().getTrackGap()) + Math.max(0.0, thickness) / 2.0;

        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            stop.setVisible(false);
            double segmentWidth = Math.max(MIN_INDETERMINATE_SEGMENT_WIDTH, width * 0.32);
            double segmentX = indeterminateSegmentX(width, segmentWidth);
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
        double remaining = width - progressWidth;
        double visibleStopDiameter = Math.max(0.0, Math.min(stopDiameter, remaining - effectiveGap));
        layoutDeterminateTrackAndStop(width, centerY, thickness, radius, effectiveGap, progressWidth,
                visibleStopDiameter);
    }

    /// Lays out the inactive determinate track and the optional stop indicator.
    private void layoutDeterminateTrackAndStop(
            double width,
            double centerY,
            double thickness,
            double radius,
            double effectiveGap,
            double progressWidth,
            double visibleStopDiameter
    ) {
        double stopLeft = Math.max(0.0, width - visibleStopDiameter);
        double trackStart = Math.min(stopLeft, progressWidth + effectiveGap);
        double trackWidth = Math.max(0.0, stopLeft - effectiveGap - trackStart);
        layoutTrackSegment(track, trackStart, trackWidth, centerY, thickness, radius);
        layoutStopIndicator(visibleStopDiameter, width - visibleStopDiameter / 2.0, centerY);
    }

    /// Lays out the linear stop indicator and hides it when the visible size is empty.
    private void layoutStopIndicator(double visibleDiameter, double centerX, double centerY) {
        stop.setVisible(visibleDiameter > 0.0);
        stop.setRadius(visibleDiameter / 2.0);
        stop.setCenterX(centerX);
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
        if (endX <= startX) {
            path.setVisible(false);
            return;
        }

        path.setVisible(true);
        double safeWavelength = Math.max(1.0, wavelength);
        int steps = Math.max(2, (int) Math.ceil((endX - startX) / LINEAR_WAVE_SAMPLE_LENGTH));
        ObservableList<PathElement> elements = path.getElements();
        ensureSampledPathElements(elements, steps + 1);
        for (int i = 0; i <= steps; i++) {
            double fraction = (double) i / (double) steps;
            double x = startX + (endX - startX) * fraction;
            double angle = (x / safeWavelength + phase) * Math.PI * 2.0;
            double y = centerY + Math.sin(angle) * amplitude;
            setSampledPathPoint(elements.get(i), x, y);
        }
    }

    /// Ensures that a sampled path contains one `MoveTo` followed by reusable `LineTo` elements.
    private static void ensureSampledPathElements(ObservableList<PathElement> elements, int count) {
        if (count < 1) {
            elements.clear();
            return;
        }

        if (elements.isEmpty()) {
            elements.add(new MoveTo());
        } else if (!(elements.get(0) instanceof MoveTo)) {
            elements.set(0, new MoveTo());
        }

        for (int i = 1; i < count; i++) {
            if (i >= elements.size()) {
                elements.add(new LineTo());
            } else if (!(elements.get(i) instanceof LineTo)) {
                elements.set(i, new LineTo());
            }
        }

        if (elements.size() > count) {
            elements.remove(count, elements.size());
        }
    }

    /// Updates the coordinates of a reusable sampled path element.
    private static void setSampledPathPoint(PathElement element, double x, double y) {
        if (element instanceof MoveTo moveTo) {
            moveTo.setX(x);
            moveTo.setY(y);
            return;
        }
        if (element instanceof LineTo lineTo) {
            lineTo.setX(x);
            lineTo.setY(y);
            return;
        }
        throw new IllegalArgumentException("Unsupported sampled path element: " + element);
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

    /// Returns the logical x coordinate of the indeterminate segment.
    private double indeterminateSegmentX(double width, double segmentWidth) {
        return -segmentWidth + (width + segmentWidth) * indeterminatePosition.get();
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            if (shouldPauseActivityAnimations()) {
                indeterminateAnimation.stop();
                indeterminatePosition.set(INDETERMINATE_START_POSITION);
            } else {
                startIndeterminateAnimation();
            }
            updateAnimatedVisuals();
        } else {
            indeterminateAnimation.stop();
            indeterminatePosition.set(INDETERMINATE_START_POSITION);
            animateDisplayedProgress(
                    clamp(progress),
                    animateDeterminateProgress && !shouldPauseActivityAnimations()
            );
        }
    }

    /// Returns whether pulse-driven progress animations should pause for the current window lifecycle state.
    private boolean shouldPauseActivityAnimations() {
        @Nullable Scene scene = getSkinnable().getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window == null || !window.isShowing();
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
        determinateAnimation.configure(spec, targetProgress);
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
