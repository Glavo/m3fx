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
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3ProgressIndicator].
@NotNullByDefault
public class M3ProgressIndicatorSkin extends SkinBase<M3ProgressIndicator> {
    /// The AndroidX Material 3 circular indeterminate cycle duration used to normalize keyframe timings.
    private static final double CIRCULAR_INDETERMINATE_REFERENCE_DURATION_MILLIS = 6000.0;

    /// The global rotation completed during one AndroidX circular indeterminate cycle.
    private static final double CIRCULAR_GLOBAL_ROTATION_DEGREES = 1080.0;

    /// The delay between additional quarter-turn pulses.
    private static final double CIRCULAR_ADDITIONAL_ROTATION_DELAY_MILLIS = 1500.0;

    /// The duration of each additional quarter-turn pulse.
    private static final double CIRCULAR_ADDITIONAL_ROTATION_DURATION_MILLIS = 300.0;

    /// The additional rotation contributed by each pulse.
    private static final double CIRCULAR_ADDITIONAL_ROTATION_DEGREES = 90.0;

    /// The shortest active sweep fraction used by full circular motion.
    private static final double INDETERMINATE_MIN_SWEEP_FRACTION = 0.10;

    /// The longest active sweep fraction used by full circular motion.
    private static final double INDETERMINATE_MAX_SWEEP_FRACTION = 0.87;

    /// The fixed sweep fraction used by reduced indeterminate progress.
    private static final double BASIC_INDETERMINATE_SWEEP_FRACTION = 0.20;

    /// The fixed sample count for circular inactive track paths.
    private static final int CIRCULAR_TRACK_SAMPLE_STEPS = 72;

    /// The fixed sample count for circular active indicator paths.
    private static final int CIRCULAR_INDICATOR_SAMPLE_STEPS = 32;

    /// The inactive track arc with gaps around the active indicator.
    private final Arc track = new Arc();

    /// The progress arc.
    private final Arc indicator = new Arc();

    /// The expressive circular track path with a gap around the active indicator.
    private final Path waveTrack = new Path();

    /// The expressive wavy active indicator path.
    private final Path waveIndicator = new Path();

    /// The resolved indicator center x-coordinate from the latest layout.
    private double resolvedCenterX;

    /// The resolved indicator center y-coordinate from the latest layout.
    private double resolvedCenterY;

    /// The resolved indicator radius from the latest layout.
    private double resolvedRadius;

    /// The resolved indicator stroke width from the latest layout.
    private double resolvedStrokeWidth;

    /// Whether the latest layout selected expressive wavy geometry.
    private boolean resolvedWavy;

    /// Whether the progress indicator has received valid layout geometry.
    private boolean geometryReady;

    /// The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition.
    private final M3DoubleTransition determinateAnimation = new M3DoubleTransition(displayedProgress);

    /// The animated active arc start fraction.
    private double indeterminateStartFraction;

    /// The animated active arc sweep fraction.
    private double indeterminateSweepFraction = INDETERMINATE_MIN_SWEEP_FRACTION;

    /// The current normalized indeterminate cycle fraction used as the expressive wave phase.
    private double indeterminateCycleFraction;

    /// The reusable indeterminate phase transition.
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

    /// Creates a progress indicator skin.
    ///
    /// @param control the skinned progress indicator
    public M3ProgressIndicatorSkin(M3ProgressIndicator control) {
        super(control);
        track.getStyleClass().add("track");
        indicator.getStyleClass().add("indicator");
        waveTrack.getStyleClass().add("m3-progress-indicator-track-wave");
        waveIndicator.getStyleClass().add("m3-progress-indicator-wave");
        track.setManaged(false);
        indicator.setManaged(false);
        waveTrack.setManaged(false);
        waveIndicator.setManaged(false);
        track.setFill(Color.TRANSPARENT);
        indicator.setFill(Color.TRANSPARENT);
        waveTrack.setFill(null);
        waveIndicator.setFill(null);
        track.setStrokeLineCap(StrokeLineCap.ROUND);
        indicator.setStrokeLineCap(StrokeLineCap.ROUND);
        waveTrack.setStrokeLineCap(StrokeLineCap.ROUND);
        waveIndicator.setStrokeLineCap(StrokeLineCap.ROUND);
        track.setType(ArcType.OPEN);
        indicator.setType(ArcType.OPEN);
        getChildren().setAll(track, indicator, waveTrack, waveIndicator);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);

        control.progressProperty().addListener(progressInvalidation);
        control.trackThicknessProperty().addListener(layoutInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        control.waveAmplitudeProperty().addListener(layoutInvalidation);
        control.wavelengthProperty().addListener(layoutInvalidation);
        control.trackGapProperty().addListener(layoutInvalidation);
        updateProgressAnimation(false);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3ProgressIndicator progressIndicator = getSkinnable();
        determinateAnimation.stop();
        indeterminateAnimation.stop();
        displayedProgress.removeListener(animationInvalidation);
        progressIndicator.progressProperty().removeListener(progressInvalidation);
        motionSettingsObserver.dispose();
        progressIndicator.trackThicknessProperty().removeListener(layoutInvalidation);
        progressIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        progressIndicator.waveAmplitudeProperty().removeListener(layoutInvalidation);
        progressIndicator.wavelengthProperty().removeListener(layoutInvalidation);
        progressIndicator.trackGapProperty().removeListener(layoutInvalidation);
        getChildren().removeAll(track, indicator, waveTrack, waveIndicator);
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

        resolvedCenterX = centerX;
        resolvedCenterY = centerY;
        resolvedRadius = radius;
        resolvedStrokeWidth = strokeWidth;
        resolvedWavy = getSkinnable().getWaveAmplitude() > 0.0;
        geometryReady = true;

        if (resolvedWavy) {
            layoutWavyIndicator(centerX, centerY, radius, strokeWidth);
            return;
        }

        waveTrack.setVisible(false);
        waveIndicator.setVisible(false);
        indicator.setVisible(true);
        track.setCenterX(centerX);
        track.setCenterY(centerY);
        track.setRadiusX(radius);
        track.setRadiusY(radius);
        track.setStrokeWidth(strokeWidth);

        indicator.setCenterX(centerX);
        indicator.setCenterY(centerY);
        indicator.setRadiusX(radius);
        indicator.setRadiusY(radius);
        indicator.setStrokeWidth(strokeWidth);
        updateAnimatedVisuals();
    }

    /// Updates the animated progress visuals using geometry resolved by the latest layout pass.
    private void updateAnimatedVisuals() {
        if (!geometryReady) {
            return;
        }
        if (resolvedWavy) {
            layoutWavyIndicator(resolvedCenterX, resolvedCenterY, resolvedRadius, resolvedStrokeWidth);
        } else {
            updateIndicatorArc();
        }
    }

    /// Lays out expressive wavy circular progress paths.
    private void layoutWavyIndicator(double centerX, double centerY, double radius, double strokeWidth) {
        M3ProgressIndicator progressIndicator = getSkinnable();
        track.setVisible(false);
        indicator.setVisible(false);
        waveTrack.setStrokeWidth(strokeWidth);
        waveIndicator.setStrokeWidth(strokeWidth);

        double progress = progressIndicator.getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            double sweepFraction = indeterminateSweepFraction;
            double start = indeterminateStartFraction;
            layoutCircularTrackPath(waveTrack, centerX, centerY, radius, strokeWidth, start, start + sweepFraction);
            layoutCircularWavePath(
                    waveIndicator,
                    centerX,
                    centerY,
                    radius,
                    progressIndicator.getWaveAmplitude(),
                    progressIndicator.getWavelength(),
                    start,
                    start + sweepFraction,
                    indeterminateCycleFraction
            );
            return;
        }

        double displayed = displayedProgress.get();
        layoutCircularTrackPath(waveTrack, centerX, centerY, radius, strokeWidth, 0.0, displayed);
        layoutCircularWavePath(
                waveIndicator,
                centerX,
                centerY,
                radius,
                amplitudeForProgress(displayed) * progressIndicator.getWaveAmplitude(),
                progressIndicator.getWavelength(),
                0.0,
                displayed,
                0.0
        );
    }

    /// Updates the visible arc for determinate or indeterminate progress.
    private void updateIndicatorArc() {
        double progress = getSkinnable().getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            double start = indeterminateStartFraction;
            indicator.setStartAngle(90.0 - 360.0 * start);
            indicator.setLength(-360.0 * indeterminateSweepFraction);
            updateTrackArc(start, start + indeterminateSweepFraction);
            return;
        }

        double displayed = displayedProgress.get();
        indicator.setStartAngle(90.0);
        indicator.setLength(-360.0 * displayed);
        updateTrackArc(0.0, displayed);
    }

    /// Updates the inactive track arc outside the active indicator and its two visual gaps.
    private void updateTrackArc(double activeStart, double activeEnd) {
        double gapFraction = circularGapFraction(
                resolvedRadius,
                getSkinnable().getTrackGap(),
                resolvedStrokeWidth
        );
        double start = activeEnd + gapFraction;
        double end = activeStart + 1.0 - gapFraction;
        boolean visible = resolvedRadius > 0.0 && end > start;
        track.setVisible(visible);
        if (visible) {
            track.setStartAngle(90.0 - 360.0 * start);
            track.setLength(-360.0 * (end - start));
        }
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        M3ProgressIndicator progressIndicator = getSkinnable();
        reducedMotion = M3Animation.shouldReduceMotion(progressIndicator);
        double progress = progressIndicator.getProgress();
        if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            if (shouldPauseActivityAnimations()) {
                indeterminateAnimation.stop();
                resetIndeterminateGeometry();
            } else {
                startIndeterminateAnimation();
            }
            updateAnimatedVisuals();
        } else {
            indeterminateAnimation.stop();
            resetIndeterminateGeometry();
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

    /// Starts the indeterminate linear phase loop.
    private void startIndeterminateAnimation() {
        indeterminateAnimation.stop();
        indeterminateAnimation.configure(
                M3Animation.motionBehavior(getSkinnable()).circularProgressIndeterminateCycleDuration()
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
        return progress == M3ProgressIndicator.INDETERMINATE_PROGRESS ? 0.0 : clamp(progress);
    }

    /// Updates circular indeterminate geometry from the AndroidX global, additional, and sweep animations.
    private void updateIndeterminateGeometry(double cycleFraction) {
        indeterminateCycleFraction = cycleFraction;
        if (reducedMotion) {
            indeterminateStartFraction = cycleFraction;
            indeterminateSweepFraction = BASIC_INDETERMINATE_SWEEP_FRACTION;
            return;
        }

        double globalRotation = CIRCULAR_GLOBAL_ROTATION_DEGREES * cycleFraction;
        double additionalRotation = additionalRotationDegrees(cycleFraction);
        indeterminateStartFraction = (globalRotation + additionalRotation) / 360.0;

        indeterminateSweepFraction = indeterminateSweepFraction(cycleFraction);
    }

    /// Returns the AndroidX circular sweep for one normalized indeterminate cycle fraction.
    static double indeterminateSweepFraction(double cycleFraction) {
        double normalizedFraction = clamp(cycleFraction);
        if (normalizedFraction < 0.5) {
            double localFraction = normalizedFraction * 2.0;
            return INDETERMINATE_MIN_SWEEP_FRACTION
                    + (INDETERMINATE_MAX_SWEEP_FRACTION - INDETERMINATE_MIN_SWEEP_FRACTION)
                    * localFraction;
        }

        double localFraction = (normalizedFraction - 0.5) * 2.0;
        return M3Motion.STANDARD.interpolate(
                INDETERMINATE_MAX_SWEEP_FRACTION,
                INDETERMINATE_MIN_SWEEP_FRACTION,
                localFraction
        );
    }

    /// Returns the four linear quarter-turn pulses used by AndroidX circular indeterminate progress.
    static double additionalRotationDegrees(double cycleFraction) {
        double elapsedMillis = clamp(cycleFraction) * CIRCULAR_INDETERMINATE_REFERENCE_DURATION_MILLIS;
        int pulseIndex = Math.min(
                3,
                (int) (elapsedMillis / CIRCULAR_ADDITIONAL_ROTATION_DELAY_MILLIS)
        );
        double pulseStartMillis = pulseIndex * CIRCULAR_ADDITIONAL_ROTATION_DELAY_MILLIS;
        double localFraction = clamp(
                (elapsedMillis - pulseStartMillis) / CIRCULAR_ADDITIONAL_ROTATION_DURATION_MILLIS
        );
        return (pulseIndex + localFraction) * CIRCULAR_ADDITIONAL_ROTATION_DEGREES;
    }

    /// Resets circular indeterminate geometry to the seamless cycle origin.
    private void resetIndeterminateGeometry() {
        indeterminateStartFraction = 0.0;
        indeterminateSweepFraction = reducedMotion
                ? BASIC_INDETERMINATE_SWEEP_FRACTION
                : INDETERMINATE_MIN_SWEEP_FRACTION;
        indeterminateCycleFraction = 0.0;
    }

    /// Returns the resolved active wave amplitude for a determinate progress value.
    private static double amplitudeForProgress(double progress) {
        return progress <= 0.1 || progress >= 0.95 ? 0.0 : 1.0;
    }

    /// Lays out the circular track path outside the active indicator and gap.
    private void layoutCircularTrackPath(
            Path path,
            double centerX,
            double centerY,
            double radius,
            double strokeWidth,
            double activeStart,
            double activeEnd
    ) {
        double gapFraction = circularGapFraction(radius, getSkinnable().getTrackGap(), strokeWidth);
        double start = activeEnd + gapFraction;
        double end = activeStart + 1.0 - gapFraction;
        if (radius <= 0.0 || end <= start) {
            path.setVisible(false);
            return;
        }

        path.setVisible(true);
        writeCircularSegment(
                path,
                CIRCULAR_TRACK_SAMPLE_STEPS,
                centerX,
                centerY,
                radius,
                0.0,
                1.0,
                start,
                end,
                0.0
        );
    }

    /// Lays out a circular wavy active indicator path.
    private static void layoutCircularWavePath(
            Path path,
            double centerX,
            double centerY,
            double radius,
            double amplitude,
            double wavelength,
            double start,
            double end,
            double phase
    ) {
        if (radius <= 0.0 || end <= start) {
            path.setVisible(false);
            return;
        }

        path.setVisible(true);
        writeCircularSegment(
                path,
                CIRCULAR_INDICATOR_SAMPLE_STEPS,
                centerX,
                centerY,
                radius,
                amplitude,
                wavelength,
                start,
                end,
                phase
        );
    }

    /// Writes a sampled circular segment to a reusable path.
    private static void writeCircularSegment(
            Path path,
            int steps,
            double centerX,
            double centerY,
            double radius,
            double amplitude,
            double wavelength,
            double start,
            double end,
            double phase
    ) {
        double circumference = Math.max(1.0, Math.PI * 2.0 * radius);
        double waves = Math.max(1.0, circumference / Math.max(1.0, wavelength));
        int resolvedSteps = Math.max(4, steps);
        ObservableList<PathElement> elements = path.getElements();
        ensureSampledPathElements(elements, resolvedSteps + 1);
        for (int i = 0; i <= resolvedSteps; i++) {
            double fraction = (double) i / (double) resolvedSteps;
            double progress = start + (end - start) * fraction;
            double angle = Math.toRadians(90.0 - 360.0 * progress);
            double waveRadius = radius + Math.sin((progress * waves + phase) * Math.PI * 2.0) * amplitude;
            double x = centerX + Math.cos(angle) * waveRadius;
            double y = centerY - Math.sin(angle) * waveRadius;
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

    /// Converts a circular gap in pixels to a normalized circumference fraction.
    private static double circularGapFraction(double radius, double gap, double strokeWidth) {
        if (radius <= 0.0 || gap <= 0.0) {
            return 0.0;
        }
        double capCompensatedGap = gap + Math.max(0.0, strokeWidth);
        return Math.min(0.20, capCompensatedGap / (Math.PI * 2.0 * radius));
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Reuses one transition for the indeterminate circular phase loop.
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

        /// Updates the primitive phase and active geometry for one animation pulse.
        @Override
        protected void interpolate(double fraction) {
            updateIndeterminateGeometry(fraction);
            updateAnimatedVisuals();
        }
    }
}
