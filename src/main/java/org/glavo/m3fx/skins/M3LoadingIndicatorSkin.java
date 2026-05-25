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
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3LoadingIndicator].
@NotNullByDefault
public class M3LoadingIndicatorSkin extends SkinBase<M3LoadingIndicator> {
    /// The number of sampled points used for the generated active shape.
    private static final int SAMPLE_COUNT = 96;

    /// The number of default indeterminate shape states.
    private static final int INDETERMINATE_SHAPE_COUNT = 7;

    /// The highest radial harmonic used by the generated shape sequence.
    private static final int HARMONIC_COUNT = 8;

    /// Shape coefficient states used by the indeterminate morphing loop.
    private static final double[][] INDETERMINATE_SHAPES = {
            coefficients(0, 0.00, 0.00),
            coefficients(3, 0.16, 0.08),
            coefficients(4, 0.14, 0.21),
            coefficients(5, 0.13, 0.34),
            coefficients(6, 0.11, 0.47),
            coefficients(7, 0.10, 0.59),
            coefficients(5, 0.18, 0.74)
    };

    /// The determinate starting shape.
    private static final double[] DETERMINATE_START_SHAPE = coefficients(0, 0.0, 0.0);

    /// The determinate completed shape.
    private static final double[] DETERMINATE_END_SHAPE = coefficients(7, 0.16, 0.25);

    /// The single active loading shape.
    private final Path indicator = new Path();

    /// The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition timeline.
    private final Timeline determinateAnimation = new Timeline();

    /// The animated phase used by indeterminate loading.
    private final DoubleProperty indeterminatePhase = new SimpleDoubleProperty(this, "indeterminatePhase");

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// Requests layout after animation ticks.
    private final InvalidationListener animationInvalidation =
            observable -> getSkinnable().requestLayout();

    /// Updates animations when the public progress value changes.
    private final InvalidationListener progressInvalidation = observable -> updateProgressAnimation(true);

    /// Requests layout after size-related token changes.
    private final InvalidationListener layoutInvalidation = observable -> getSkinnable().requestLayout();

    /// Creates a loading indicator skin.
    ///
    /// @param control the skinned loading indicator
    public M3LoadingIndicatorSkin(M3LoadingIndicator control) {
        super(control);
        indicator.getStyleClass().add("m3-loading-indicator-indicator");
        indicator.setManaged(false);
        getChildren().add(indicator);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePhase.addListener(animationInvalidation);
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);

        control.progressProperty().addListener(progressInvalidation);
        control.containerSizeProperty().addListener(layoutInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        updateProgressAnimation(false);
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        determinateAnimation.stop();
        indeterminateAnimation.stop();
        displayedProgress.removeListener(animationInvalidation);
        indeterminatePhase.removeListener(animationInvalidation);
        loadingIndicator.progressProperty().removeListener(progressInvalidation);
        loadingIndicator.containerSizeProperty().removeListener(layoutInvalidation);
        loadingIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        super.dispose();
    }

    /// Lays out the active loading shape inside the control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        double indicatorSize = Math.min(loadingIndicator.getIndicatorSize(), Math.min(width, height));
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double phase = loadingIndicator.isIndeterminate()
                ? indeterminatePhase.get()
                : displayedProgress.get();

        rebuildIndicatorPath(centerX, centerY, indicatorSize / 2.0, phase, loadingIndicator.isIndeterminate());
        indicator.resizeRelocate(x, y, width, height);
        indicator.setOpacity(loadingIndicator.isDisabled() ? 0.38 : 1.0);
    }

    /// Updates determinate or indeterminate animation state for the current progress value.
    private void updateProgressAnimation(boolean animateDeterminateProgress) {
        double progress = getSkinnable().getProgress();
        if (progress == M3LoadingIndicator.INDETERMINATE_PROGRESS) {
            determinateAnimation.stop();
            displayedProgress.set(0.0);
            if (!M3Animation.areAnimationsEnabled(getSkinnable())) {
                indeterminateAnimation.stop();
                indeterminatePhase.set(0.0);
            } else if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
                configureIndeterminateAnimation();
                indeterminateAnimation.play();
            }
        } else {
            indeterminateAnimation.stop();
            indeterminatePhase.set(0.0);
            animateDisplayedProgress(clamp(progress), animateDeterminateProgress);
        }
    }

    /// Configures the indeterminate phase loop with the current owner behavior timing.
    private void configureIndeterminateAnimation() {
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminatePhase, 0.0, M3Motion.LINEAR)
                ),
                new KeyFrame(
                        M3Animation.motionBehavior(getSkinnable()).circularProgressIndeterminateCycleDuration(),
                        new KeyValue(indeterminatePhase, INDETERMINATE_SHAPE_COUNT, M3Motion.LINEAR)
                )
        );
    }

    /// Animates the displayed determinate progress value.
    private void animateDisplayedProgress(double targetProgress, boolean animate) {
        determinateAnimation.stop();
        if (!animate || !M3Animation.areAnimationsEnabled(getSkinnable())) {
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

    /// Rebuilds the active shape path for the current animation state.
    private void rebuildIndicatorPath(
            double centerX,
            double centerY,
            double radius,
            double phase,
            boolean indeterminate
    ) {
        indicator.getElements().clear();
        if (radius <= 0.0) {
            return;
        }

        double rotationPhase = indeterminate ? easedIndeterminatePhase(phase) : phase;
        double rotation = indeterminate ? rotationPhase / INDETERMINATE_SHAPE_COUNT : -phase * 0.5;
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            double angle = Math.PI * 2.0 * i / SAMPLE_COUNT;
            double rotatedAngle = angle + Math.PI * 2.0 * rotation;
            double shapeRadius = radiusFor(phase, angle, indeterminate) * radius;
            double pointX = centerX + Math.cos(rotatedAngle) * shapeRadius;
            double pointY = centerY + Math.sin(rotatedAngle) * shapeRadius;

            if (i == 0) {
                indicator.getElements().add(new MoveTo(pointX, pointY));
            } else {
                indicator.getElements().add(new LineTo(pointX, pointY));
            }
        }
        indicator.getElements().add(new ClosePath());
    }

    /// Returns a sampled radius multiplier for the current animation state.
    private static double radiusFor(double phase, double angle, boolean indeterminate) {
        return indeterminate
                ? indeterminateRadiusFor(phase, angle)
                : determinateRadiusFor(phase, angle);
    }

    /// Returns a Catmull-Rom interpolated indeterminate radius multiplier.
    private static double indeterminateRadiusFor(double phase, double angle) {
        double normalized = positiveModulo(phase, INDETERMINATE_SHAPE_COUNT);
        int index = (int) Math.floor(normalized);
        double fraction = smoothStep(normalized - index);
        double radius = catmullCoefficient(INDETERMINATE_SHAPES, index, fraction, 0);
        for (int harmonic = 1; harmonic <= HARMONIC_COUNT; harmonic++) {
            double harmonicAngle = harmonic * angle;
            radius += catmullCoefficient(INDETERMINATE_SHAPES, index, fraction, harmonic)
                    * Math.cos(harmonicAngle);
            radius += catmullCoefficient(INDETERMINATE_SHAPES, index, fraction, HARMONIC_COUNT + harmonic)
                    * Math.sin(harmonicAngle);
        }
        return clampRadius(radius);
    }

    /// Returns an eased indeterminate phase that slows at each shape state.
    private static double easedIndeterminatePhase(double phase) {
        double normalized = positiveModulo(phase, INDETERMINATE_SHAPE_COUNT);
        int index = (int) Math.floor(normalized);
        return index + smoothStep(normalized - index);
    }

    /// Returns a linearly interpolated determinate radius multiplier.
    private static double determinateRadiusFor(double progress, double angle) {
        double fraction = smoothStep(clamp(progress));
        double radius = interpolate(DETERMINATE_START_SHAPE[0], DETERMINATE_END_SHAPE[0], fraction);
        for (int harmonic = 1; harmonic <= HARMONIC_COUNT; harmonic++) {
            double harmonicAngle = harmonic * angle;
            radius += interpolate(DETERMINATE_START_SHAPE[harmonic], DETERMINATE_END_SHAPE[harmonic], fraction)
                    * Math.cos(harmonicAngle);
            radius += interpolate(
                    DETERMINATE_START_SHAPE[HARMONIC_COUNT + harmonic],
                    DETERMINATE_END_SHAPE[HARMONIC_COUNT + harmonic],
                    fraction
            ) * Math.sin(harmonicAngle);
        }
        return clampRadius(radius);
    }

    /// Returns a smooth interpolation fraction with zero velocity at both ends.
    private static double smoothStep(double value) {
        double clamped = clamp(value);
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    /// Returns the initial displayed progress value for a public progress value.
    private static double initialDisplayedProgress(double progress) {
        return progress == M3LoadingIndicator.INDETERMINATE_PROGRESS ? 0.0 : clamp(progress);
    }

    /// Returns a positive modulo result.
    private static double positiveModulo(double value, double modulus) {
        double result = value % modulus;
        return result < 0.0 ? result + modulus : result;
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Interpolates between two scalar values.
    private static double interpolate(double start, double end, double fraction) {
        return start + (end - start) * fraction;
    }

    /// Returns one Catmull-Rom interpolated coefficient for the closed indeterminate sequence.
    private static double catmullCoefficient(double[][] sequence, int index, double fraction, int coefficientIndex) {
        int count = sequence.length;
        double previous = sequence[(index + count - 1) % count][coefficientIndex];
        double current = sequence[index][coefficientIndex];
        double next = sequence[(index + 1) % count][coefficientIndex];
        double following = sequence[(index + 2) % count][coefficientIndex];
        double t2 = fraction * fraction;
        double t3 = t2 * fraction;

        return 0.5 * ((2.0 * current)
                + (-previous + next) * fraction
                + (2.0 * previous - 5.0 * current + 4.0 * next - following) * t2
                + (-previous + 3.0 * current - 3.0 * next + following) * t3);
    }

    /// Returns a bounded radius multiplier to keep generated paths stable.
    private static double clampRadius(double radius) {
        return Math.max(0.70, Math.min(1.18, radius));
    }

    /// Creates harmonic coefficients for a radial shape.
    private static double[] coefficients(int lobes, double amplitude, double phaseOffset) {
        double[] coefficients = new double[HARMONIC_COUNT * 2 + 1];
        coefficients[0] = 1.0;
        if (lobes > 0 && lobes <= HARMONIC_COUNT && amplitude > 0.0) {
            double harmonicPhase = lobes * Math.PI * 2.0 * phaseOffset;
            coefficients[0] -= amplitude * 0.5;
            coefficients[lobes] = amplitude * 0.5 * Math.cos(harmonicPhase);
            coefficients[HARMONIC_COUNT + lobes] = -amplitude * 0.5 * Math.sin(harmonicPhase);
        }
        return coefficients;
    }
}
