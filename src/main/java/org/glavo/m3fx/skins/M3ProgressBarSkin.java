// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.Transition;
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
    /// The AndroidX Material 3 linear indeterminate cycle duration used to normalize keyframe timings.
    private static final double LINEAR_INDETERMINATE_REFERENCE_DURATION_MILLIS = 1750.0;

    /// The first active segment head duration.
    private static final double FIRST_HEAD_DURATION_MILLIS = 1000.0;

    /// The first active segment tail delay.
    private static final double FIRST_TAIL_DELAY_MILLIS = 250.0;

    /// The first active segment tail duration.
    private static final double FIRST_TAIL_DURATION_MILLIS = 1000.0;

    /// The second active segment head delay.
    private static final double SECOND_HEAD_DELAY_MILLIS = 650.0;

    /// The second active segment head duration.
    private static final double SECOND_HEAD_DURATION_MILLIS = 850.0;

    /// The second active segment tail delay.
    private static final double SECOND_TAIL_DELAY_MILLIS = 900.0;

    /// The second active segment tail duration.
    private static final double SECOND_TAIL_DURATION_MILLIS = 850.0;

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

    /// The third track rectangle needed while both indeterminate segments are visible.
    private final Rectangle tertiaryTrack = new Rectangle();

    /// The progress bar rectangle.
    private final Rectangle bar = new Rectangle();

    /// The second progress rectangle used by indeterminate linear progress.
    private final Rectangle secondaryBar = new Rectangle();

    /// The wavy active progress path used by expressive progress bars.
    private final Path waveBar = new Path();

    /// The second wavy active path used by expressive indeterminate progress.
    private final Path secondaryWaveBar = new Path();

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

    /// The first indeterminate segment start fraction.
    private double firstIndeterminateStart;

    /// The first indeterminate segment end fraction.
    private double firstIndeterminateEnd;

    /// The second indeterminate segment start fraction.
    private double secondIndeterminateStart;

    /// The second indeterminate segment end fraction.
    private double secondIndeterminateEnd;

    /// The current normalized indeterminate cycle fraction used as the expressive wave phase.
    private double indeterminateCycleFraction;

    /// The reusable indeterminate segment transition.
    private final IndeterminateTransition indeterminateAnimation = new IndeterminateTransition();

    /// Whether the current inherited motion settings require reduced-motion rendering.
    private boolean reducedMotion;

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
        secondaryTrack.getStyleClass().add("m3-progress-bar-secondary-track");
        tertiaryTrack.getStyleClass().add("m3-progress-bar-tertiary-track");
        bar.getStyleClass().add("bar");
        secondaryBar.getStyleClass().add("m3-progress-bar-secondary-bar");
        waveBar.getStyleClass().add("m3-progress-bar-wave");
        secondaryWaveBar.getStyleClass().add("m3-progress-bar-secondary-wave");
        stop.getStyleClass().add("m3-progress-stop");
        track.setManaged(false);
        secondaryTrack.setManaged(false);
        tertiaryTrack.setManaged(false);
        bar.setManaged(false);
        secondaryBar.setManaged(false);
        waveBar.setManaged(false);
        secondaryWaveBar.setManaged(false);
        stop.setManaged(false);
        waveBar.setFill(null);
        secondaryWaveBar.setFill(null);
        waveBar.setStrokeLineCap(StrokeLineCap.ROUND);
        secondaryWaveBar.setStrokeLineCap(StrokeLineCap.ROUND);
        container.getChildren().addAll(
                track,
                secondaryTrack,
                tertiaryTrack,
                bar,
                secondaryBar,
                waveBar,
                secondaryWaveBar,
                stop
        );
        getChildren().setAll(container);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);

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
        progressBar.progressProperty().removeListener(progressInvalidation);
        motionSettingsObserver.dispose();
        progressBar.trackThicknessProperty().removeListener(layoutInvalidation);
        progressBar.trackShapeProperty().removeListener(layoutInvalidation);
        progressBar.waveAmplitudeProperty().removeListener(layoutInvalidation);
        progressBar.wavelengthProperty().removeListener(layoutInvalidation);
        progressBar.trackGapProperty().removeListener(layoutInvalidation);
        progressBar.stopSizeProperty().removeListener(layoutInvalidation);
        getChildren().remove(container);
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
        secondaryWaveBar.setVisible(false);
        double centerY = height / 2.0;
        double effectiveGap = Math.max(0.0, getSkinnable().getTrackGap()) + Math.max(0.0, thickness) / 2.0;
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            stop.setVisible(false);
            layoutIndeterminateTracks(width, centerY, thickness, radius, effectiveGap);
            layoutIndeterminateRectangle(
                    bar,
                    width,
                    centerY,
                    thickness,
                    radius,
                    firstIndeterminateStart,
                    firstIndeterminateEnd
            );
            layoutIndeterminateRectangle(
                    secondaryBar,
                    width,
                    centerY,
                    thickness,
                    radius,
                    secondIndeterminateStart,
                    secondIndeterminateEnd
            );
            return;
        }

        secondaryTrack.setVisible(false);
        tertiaryTrack.setVisible(false);
        secondaryBar.setVisible(false);
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
        secondaryBar.setVisible(false);
        waveBar.setVisible(true);
        waveBar.setStrokeWidth(thickness);
        secondaryWaveBar.setStrokeWidth(thickness);
        double effectiveGap = Math.max(0.0, getSkinnable().getTrackGap()) + Math.max(0.0, thickness) / 2.0;

        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            stop.setVisible(false);
            layoutIndeterminateTracks(width, centerY, thickness, radius, effectiveGap);
            layoutIndeterminateWave(
                    waveBar,
                    width,
                    centerY,
                    amplitude,
                    firstIndeterminateStart,
                    firstIndeterminateEnd
            );
            layoutIndeterminateWave(
                    secondaryWaveBar,
                    width,
                    centerY,
                    amplitude,
                    secondIndeterminateStart,
                    secondIndeterminateEnd
            );
            return;
        }

        secondaryTrack.setVisible(false);
        tertiaryTrack.setVisible(false);
        secondaryWaveBar.setVisible(false);
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
        layoutWavePath(path, startX, endX, centerY, amplitude, wavelength, phase, 0);
    }

    /// Lays out a sampled sine-wave path with an optional stable sample count.
    private static void layoutWavePath(
            Path path,
            double startX,
            double endX,
            double centerY,
            double amplitude,
            double wavelength,
            double phase,
            int fixedSteps
    ) {
        int steps = fixedSteps > 0
                ? fixedSteps
                : Math.max(2, (int) Math.ceil((endX - startX) / LINEAR_WAVE_SAMPLE_LENGTH));
        ObservableList<PathElement> elements = path.getElements();
        ensureSampledPathElements(elements, steps + 1);
        if (endX <= startX) {
            path.setVisible(false);
            for (int i = 0; i <= steps; i++) {
                setSampledPathPoint(elements.get(i), startX, centerY);
            }
            return;
        }

        path.setVisible(true);
        double safeWavelength = Math.max(1.0, wavelength);
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

    /// Lays out the inactive track around up to two visible indeterminate active segments.
    private void layoutIndeterminateTracks(
            double width,
            double centerY,
            double thickness,
            double radius,
            double gap
    ) {
        double firstStart = clamp(firstIndeterminateStart) * width;
        double firstEnd = clamp(firstIndeterminateEnd) * width;
        double secondStart = clamp(secondIndeterminateStart) * width;
        double secondEnd = clamp(secondIndeterminateEnd) * width;
        boolean firstVisible = firstEnd > firstStart;
        boolean secondVisible = secondEnd > secondStart;

        if (!firstVisible && !secondVisible) {
            layoutTrackSegment(track, 0.0, width, centerY, thickness, radius);
            secondaryTrack.setVisible(false);
            tertiaryTrack.setVisible(false);
            return;
        }
        if (!firstVisible) {
            firstStart = secondStart;
            firstEnd = secondEnd;
            secondVisible = false;
        } else if (secondVisible && secondStart < firstStart) {
            double swappedStart = firstStart;
            double swappedEnd = firstEnd;
            firstStart = secondStart;
            firstEnd = secondEnd;
            secondStart = swappedStart;
            secondEnd = swappedEnd;
        }

        if (secondVisible && secondStart <= firstEnd + gap * 2.0) {
            firstEnd = Math.max(firstEnd, secondEnd);
            secondVisible = false;
        }

        layoutTrackSegment(
                track,
                0.0,
                Math.max(0.0, firstStart - gap),
                centerY,
                thickness,
                radius
        );
        if (!secondVisible) {
            double trailingStart = Math.min(width, firstEnd + gap);
            layoutTrackSegment(
                    secondaryTrack,
                    trailingStart,
                    width - trailingStart,
                    centerY,
                    thickness,
                    radius
            );
            tertiaryTrack.setVisible(false);
            return;
        }

        double middleStart = Math.min(width, firstEnd + gap);
        double middleEnd = Math.max(middleStart, secondStart - gap);
        layoutTrackSegment(
                secondaryTrack,
                middleStart,
                middleEnd - middleStart,
                centerY,
                thickness,
                radius
        );
        double trailingStart = Math.min(width, secondEnd + gap);
        layoutTrackSegment(
                tertiaryTrack,
                trailingStart,
                width - trailingStart,
                centerY,
                thickness,
                radius
        );
    }

    /// Lays out one indeterminate active rectangle from normalized endpoints.
    private static void layoutIndeterminateRectangle(
            Rectangle rectangle,
            double width,
            double centerY,
            double thickness,
            double radius,
            double start,
            double end
    ) {
        double startX = clamp(start) * width;
        double endX = clamp(end) * width;
        double segmentWidth = Math.max(0.0, endX - startX);
        rectangle.setVisible(segmentWidth > 0.0);
        layoutRectangle(
                rectangle,
                startX,
                centerY - thickness / 2.0,
                segmentWidth,
                thickness,
                Math.min(radius, segmentWidth / 2.0)
        );
    }

    /// Lays out one expressive indeterminate active wave from normalized endpoints.
    private void layoutIndeterminateWave(
            Path path,
            double width,
            double centerY,
            double amplitude,
            double start,
            double end
    ) {
        layoutWavePath(
                path,
                clamp(start) * width,
                clamp(end) * width,
                centerY,
                amplitude,
                getSkinnable().getWavelength(),
                indeterminateCycleFraction,
                Math.max(2, (int) Math.ceil(width / LINEAR_WAVE_SAMPLE_LENGTH))
        );
    }

    /// Resolves one delayed AndroidX keyframe interval using the Material emphasized accelerate easing.
    private static double timedProgress(double cycleFraction, double delayMillis, double durationMillis) {
        double elapsedMillis = clamp(cycleFraction) * LINEAR_INDETERMINATE_REFERENCE_DURATION_MILLIS;
        double intervalFraction = clamp((elapsedMillis - delayMillis) / durationMillis);
        return M3Motion.EMPHASIZED_ACCELERATE.interpolate(0.0, 1.0, intervalFraction);
    }

    /// Updates both indeterminate segments for one normalized AndroidX cycle fraction.
    private void updateIndeterminateSegments(double fraction) {
        indeterminateCycleFraction = fraction;
        if (reducedMotion) {
            firstIndeterminateStart = Math.max(0.0, fraction * 1.32 - 0.32);
            firstIndeterminateEnd = Math.min(1.0, firstIndeterminateStart + 0.32);
            secondIndeterminateStart = 0.0;
            secondIndeterminateEnd = 0.0;
            return;
        }

        firstIndeterminateStart = timedProgress(
                fraction,
                FIRST_TAIL_DELAY_MILLIS,
                FIRST_TAIL_DURATION_MILLIS
        );
        firstIndeterminateEnd = timedProgress(fraction, 0.0, FIRST_HEAD_DURATION_MILLIS);
        secondIndeterminateStart = timedProgress(
                fraction,
                SECOND_TAIL_DELAY_MILLIS,
                SECOND_TAIL_DURATION_MILLIS
        );
        secondIndeterminateEnd = timedProgress(
                fraction,
                SECOND_HEAD_DELAY_MILLIS,
                SECOND_HEAD_DURATION_MILLIS
        );
        if (firstIndeterminateEnd <= firstIndeterminateStart
                && secondIndeterminateEnd > secondIndeterminateStart) {
            firstIndeterminateStart = secondIndeterminateStart;
            firstIndeterminateEnd = secondIndeterminateEnd;
            secondIndeterminateStart = 0.0;
            secondIndeterminateEnd = 0.0;
        }
    }

    /// Resets both indeterminate segments to a stable visible phase while animation is paused.
    private void resetIndeterminateSegments() {
        updateIndeterminateSegments(reducedMotion ? 0.0 : 0.40);
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        M3ProgressBar progressBar = getSkinnable();
        reducedMotion = M3Animation.shouldReduceMotion(progressBar);
        double progress = progressBar.getProgress();
        if (progress == M3ProgressBar.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            if (shouldPauseActivityAnimations()) {
                indeterminateAnimation.stop();
                resetIndeterminateSegments();
            } else {
                startIndeterminateAnimation();
            }
            updateAnimatedVisuals();
        } else {
            indeterminateAnimation.stop();
            resetIndeterminateSegments();
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
        indeterminateAnimation.configure(
                M3Animation.motionBehavior(getSkinnable()).linearProgressIndeterminateCycleDuration()
        );
        indeterminateAnimation.playFromStart();
    }

    /// Animates the displayed determinate progress value.
    private void animateDisplayedProgress(double targetProgress, boolean animate) {
        determinateAnimation.stop();
        if (!animate || reducedMotion) {
            displayedProgress.set(targetProgress);
            return;
        }

        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        determinateAnimation.configure(spec, targetProgress);
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

    /// Reuses one transition for the indeterminate linear segment loop.
    @NotNullByDefault
    private final class IndeterminateTransition extends Transition {
        /// Creates a linearly timed indefinite transition.
        private IndeterminateTransition() {
            setInterpolator(M3Motion.LINEAR);
            setCycleCount(Animation.INDEFINITE);
        }

        /// Updates the loop duration without allocating key frames or writable properties.
        ///
        /// @param duration the complete indeterminate cycle duration
        private void configure(Duration duration) {
            stop();
            setCycleDuration(duration);
        }

        /// Updates the primitive segment position and active geometry for one animation pulse.
        @Override
        protected void interpolate(double fraction) {
            double shiftedFraction = fraction + 0.40;
            if (shiftedFraction >= 1.0) {
                shiftedFraction -= 1.0;
            }
            updateIndeterminateSegments(shiftedFraction);
            updateAnimatedVisuals();
        }
    }
}
