// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.internal.animation.M3DoubleTransition;

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
///
/// The skin renders determinate and indeterminate circular progress using standard arcs or expressive wavy paths.
/// It preserves the configured gap between active and inactive geometry, animates determinate value changes through
/// the active motion profile, and pauses indeterminate activity while the control has no showing window.
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
    private static final int CIRCULAR_INDICATOR_SAMPLE_STEPS = 72;

    /// The minimum whole-wave count used to preserve the expressive circular silhouette.
    private static final int MIN_CIRCULAR_WAVE_COUNT = 5;

    /// The maximum whole-wave count that retains at least four samples per wave.
    private static final int MAX_CIRCULAR_WAVE_COUNT = CIRCULAR_INDICATOR_SAMPLE_STEPS / 4;

    /// The time required for one wave crest to advance by one wavelength.
    private static final double WAVE_PHASE_CYCLE_MILLIS = 1000.0;

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
    private final M3DoubleTransition determinateAnimation = new M3DoubleTransition(
            displayedProgress,
            M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
            0.0,
            1.0
    );

    /// The animated active arc start fraction.
    private double indeterminateStartFraction;

    /// The animated active arc sweep fraction.
    private double indeterminateSweepFraction = INDETERMINATE_MIN_SWEEP_FRACTION;

    /// The current normalized expressive wave phase, measured in complete wavelengths.
    private double wavePhaseCycles;

    /// The reusable activity transition for indeterminate geometry and determinate wave propagation.
    private final ActivityTransition activityAnimation = new ActivityTransition();

    /// Whether the current inherited motion settings require reduced-motion rendering.
    private boolean reducedMotion;

    /// Whether progress animation state is currently being recomputed.
    private boolean updatingProgressAnimation;

    /// Updates internal progress geometry after animation ticks without invalidating parent layout.
    private final InvalidationListener animationInvalidation =
            observable -> updateAnimatedVisuals();

    /// Updates animations when the public progress value changes.
    private final InvalidationListener progressInvalidation = observable -> updateProgressAnimation(true);

    /// Observes motion settings while determinate or indeterminate progress is active.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(getSkinnable(), () -> updateProgressAnimation(false), false);

    /// Requests layout after size-related token changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Reconfigures activity when expressive wave mode changes and requests fresh geometry.
    private final InvalidationListener waveModeInvalidation = observable -> {
        getSkinnable().requestLayout();
        updateProgressAnimation(false);
    };

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
        control.waveIndicatorSizeProperty().addListener(layoutInvalidation);
        control.waveAmplitudeProperty().addListener(waveModeInvalidation);
        control.wavelengthProperty().addListener(layoutInvalidation);
        control.trackGapProperty().addListener(layoutInvalidation);
        determinateAnimation.setOnFinished(event -> updateProgressAnimation(false));
        updateProgressAnimation(false);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3ProgressIndicator progressIndicator = getSkinnable();
        determinateAnimation.stop();
        determinateAnimation.setOnFinished(null);
        activityAnimation.stop();
        displayedProgress.removeListener(animationInvalidation);
        progressIndicator.progressProperty().removeListener(progressInvalidation);
        motionSettingsObserver.dispose();
        progressIndicator.trackThicknessProperty().removeListener(layoutInvalidation);
        progressIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        progressIndicator.waveIndicatorSizeProperty().removeListener(layoutInvalidation);
        progressIndicator.waveAmplitudeProperty().removeListener(waveModeInvalidation);
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
                    wavePhaseCycles
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
                wavePhaseCycles
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
        if (updatingProgressAnimation) {
            return;
        }
        updatingProgressAnimation = true;
        try {
            M3ProgressIndicator progressIndicator = getSkinnable();
            reducedMotion = !M3Animation.areAnimationsEnabled(progressIndicator);
            double progress = progressIndicator.getProgress();
            boolean activityPaused = shouldPauseActivityAnimations();
            if (progress == M3ProgressIndicator.INDETERMINATE_PROGRESS) {
                determinateAnimation.stop();
                if (activityPaused) {
                    activityAnimation.stop();
                    resetIndeterminateGeometry();
                } else {
                    startActivityAnimation();
                }
                updateAnimatedVisuals();
                motionSettingsObserver.start();
            } else {
                resetIndeterminateGeometry();
                animateDisplayedProgress(
                        clamp(progress),
                        animateDeterminateProgress && !activityPaused
                );
                boolean waveVisible = isDeterminateWaveVisible(progress);
                if (waveVisible && !reducedMotion && !activityPaused) {
                    startActivityAnimation();
                } else {
                    activityAnimation.stop();
                }
                if (determinateAnimation.getStatus() == Animation.Status.RUNNING || waveVisible) {
                    motionSettingsObserver.start();
                } else {
                    motionSettingsObserver.stop();
                }
            }
        } finally {
            updatingProgressAnimation = false;
        }
    }

    /// Returns whether pulse-driven progress animations should pause for the current window lifecycle state.
    private boolean shouldPauseActivityAnimations() {
        @Nullable Scene scene = getSkinnable().getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window == null || !window.isShowing();
    }

    /// Starts the shared linear activity loop when it is not already running with the current duration.
    private void startActivityAnimation() {
        Duration duration = M3Animation.motionBehavior(getSkinnable()).circularProgressIndeterminateCycleDuration();
        if (activityAnimation.getStatus() == Animation.Status.RUNNING
                && activityAnimation.getCycleDuration().equals(duration)) {
            return;
        }
        activityAnimation.configure(duration);
        activityAnimation.playFromStart();
    }

    /// Returns whether determinate expressive wave geometry is currently visible.
    private boolean isDeterminateWaveVisible(double targetProgress) {
        if (getSkinnable().getWaveAmplitude() <= 0.0) {
            return false;
        }
        return amplitudeForProgress(clamp(targetProgress)) > 0.0
                || amplitudeForProgress(displayedProgress.get()) > 0.0;
    }

    /// Animates the displayed determinate progress value.
    private void animateDisplayedProgress(double targetProgress, boolean animate) {
        if (!animate || reducedMotion) {
            determinateAnimation.stop();
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

    /// Returns the four emphasized-decelerate quarter-turn pulses used by AndroidX circular indeterminate progress.
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
        double easedFraction;
        if (localFraction <= 0.0) {
            easedFraction = 0.0;
        } else if (localFraction >= 1.0) {
            easedFraction = 1.0;
        } else {
            easedFraction = M3Motion.EMPHASIZED_DECELERATE.interpolate(0.0, 1.0, localFraction);
        }
        return (pulseIndex + easedFraction) * CIRCULAR_ADDITIONAL_ROTATION_DEGREES;
    }

    /// Resets circular indeterminate geometry to the seamless cycle origin.
    private void resetIndeterminateGeometry() {
        indeterminateStartFraction = 0.0;
        indeterminateSweepFraction = reducedMotion
                ? BASIC_INDETERMINATE_SWEEP_FRACTION
                : INDETERMINATE_MIN_SWEEP_FRACTION;
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
            double phaseCycles
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
                phaseCycles
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
            double phaseCycles
    ) {
        int waveCount = circularWaveCount(radius, wavelength);
        int resolvedSteps = Math.max(4, steps);
        ObservableList<PathElement> elements = path.getElements();
        ensureSampledPathElements(elements, resolvedSteps + 1);
        for (int i = 0; i <= resolvedSteps; i++) {
            double fraction = (double) i / (double) resolvedSteps;
            double progress = start + (end - start) * fraction;
            double angle = Math.toRadians(90.0 - 360.0 * progress);
            double waveRadius = circularWaveRadius(
                    radius,
                    amplitude,
                    waveCount,
                    progress,
                    phaseCycles
            );
            double x = centerX + Math.cos(angle) * waveRadius;
            double y = centerY - Math.sin(angle) * waveRadius;
            setSampledPathPoint(elements.get(i), x, y);
        }
    }

    /// Returns a whole-wave count that closes seamlessly around the circular indicator.
    ///
    /// @param radius the center-line radius
    /// @param wavelength the requested arc length between adjacent crests
    /// @return the bounded number of complete waves around the circle
    static int circularWaveCount(double radius, double wavelength) {
        double circumference = Math.max(0.0, Math.PI * 2.0 * radius);
        int waveCount = (int) Math.floor(circumference / Math.max(1.0, wavelength));
        return Math.max(MIN_CIRCULAR_WAVE_COUNT, Math.min(MAX_CIRCULAR_WAVE_COUNT, waveCount));
    }

    /// Returns the radial coordinate for one point of a circular traveling wave.
    ///
    /// @param radius the unmodulated center-line radius
    /// @param amplitude the maximum radial displacement
    /// @param waveCount the complete number of waves around the circle
    /// @param progress the clockwise position around the full circle
    /// @param phaseCycles the propagated offset measured in wavelengths
    /// @return the modulated radius at `progress`
    static double circularWaveRadius(
            double radius,
            double amplitude,
            int waveCount,
            double progress,
            double phaseCycles
    ) {
        double angle = (progress * waveCount - phaseCycles) * Math.PI * 2.0;
        return radius + Math.sin(angle) * amplitude;
    }

    /// Advances a normalized wave phase from consecutive fractions of a repeating activity cycle.
    ///
    /// @param phaseCycles the current phase measured in wavelengths
    /// @param previousCycleFraction the preceding activity-cycle fraction
    /// @param currentCycleFraction the current activity-cycle fraction
    /// @param cycleDurationMillis the complete activity-cycle duration in milliseconds
    /// @return the advanced phase normalized to `[0, 1)`
    static double propagatedWavePhase(
            double phaseCycles,
            double previousCycleFraction,
            double currentCycleFraction,
            double cycleDurationMillis
    ) {
        double previous = clamp(previousCycleFraction);
        double current = clamp(currentCycleFraction);
        double elapsedFraction = current - previous;
        if (elapsedFraction < 0.0) {
            elapsedFraction += 1.0;
        }
        double elapsedMillis = elapsedFraction * Math.max(0.0, cycleDurationMillis);
        double propagated = phaseCycles + elapsedMillis / WAVE_PHASE_CYCLE_MILLIS;
        return propagated - Math.floor(propagated);
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

    /// Reuses one transition for indeterminate geometry and expressive wave propagation.
    @NotNullByDefault
    private final class ActivityTransition extends Transition {
        /// The configured activity cycle duration cached for allocation-free phase propagation.
        private double cycleDurationMillis;

        /// The cycle fraction observed during the preceding pulse.
        private double previousCycleFraction;

        /// Creates a linearly timed indefinite transition.
        private ActivityTransition() {
            setInterpolator(M3Motion.LINEAR);
            setCycleCount(Animation.INDEFINITE);
        }

        /// Updates the loop duration without allocating key frames or writable properties.
        ///
        /// @param duration the complete indeterminate cycle duration
        private void configure(Duration duration) {
            stop();
            setCycleDuration(duration);
            cycleDurationMillis = Math.max(0.0, duration.toMillis());
            previousCycleFraction = 0.0;
        }

        /// Updates primitive phase and active geometry without allocating per-pulse objects.
        @Override
        protected void interpolate(double fraction) {
            boolean indeterminate = getSkinnable().getProgress() == M3ProgressIndicator.INDETERMINATE_PROGRESS;
            if (!indeterminate && !reducedMotion && getSkinnable().getWaveAmplitude() > 0.0) {
                wavePhaseCycles = propagatedWavePhase(
                        wavePhaseCycles,
                        previousCycleFraction,
                        fraction,
                        cycleDurationMillis
                );
            }
            previousCycleFraction = fraction;
            if (indeterminate) {
                updateIndeterminateGeometry(fraction);
            }
            updateAnimatedVisuals();
        }
    }
}
