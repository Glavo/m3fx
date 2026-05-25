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

    /// Shape states used by the indeterminate morphing loop.
    private static final ShapeSpec[] INDETERMINATE_SHAPES = {
            new ShapeSpec(0, 0.00, 0.00),
            new ShapeSpec(3, 0.16, 0.08),
            new ShapeSpec(4, 0.14, 0.21),
            new ShapeSpec(5, 0.13, 0.34),
            new ShapeSpec(6, 0.11, 0.47),
            new ShapeSpec(7, 0.10, 0.59),
            new ShapeSpec(5, 0.18, 0.74)
    };

    /// The determinate starting shape.
    private static final ShapeSpec DETERMINATE_START_SHAPE = new ShapeSpec(0, 0.0, 0.0);

    /// The determinate completed shape.
    private static final ShapeSpec DETERMINATE_END_SHAPE = new ShapeSpec(7, 0.16, 0.25);

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

        double rotation = indeterminate ? phase / INDETERMINATE_SHAPE_COUNT : -phase * 0.5;
        MorphState morph = indeterminate ? indeterminateMorphAt(phase) : determinateMorphAt(phase);

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            double angle = Math.PI * 2.0 * i / SAMPLE_COUNT;
            double rotatedAngle = angle + Math.PI * 2.0 * rotation;
            double shapeRadius = radiusFor(morph, angle) * radius;
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

    /// Returns the interpolated indeterminate morph state for the current phase.
    private static MorphState indeterminateMorphAt(double phase) {
        double normalized = positiveModulo(phase, INDETERMINATE_SHAPE_COUNT);
        int index = (int) Math.floor(normalized);
        double fraction = normalized - index;
        ShapeSpec current = INDETERMINATE_SHAPES[index];
        ShapeSpec next = INDETERMINATE_SHAPES[(index + 1) % INDETERMINATE_SHAPE_COUNT];
        return new MorphState(current, next, smoothStep(fraction));
    }

    /// Returns the interpolated determinate morph state for the current progress.
    private static MorphState determinateMorphAt(double progress) {
        return new MorphState(DETERMINATE_START_SHAPE, DETERMINATE_END_SHAPE, smoothStep(clamp(progress)));
    }

    /// Returns a sampled radius multiplier for a morph state.
    private static double radiusFor(MorphState morph, double angle) {
        return interpolate(
                radiusFor(morph.start(), angle),
                radiusFor(morph.end(), angle),
                morph.fraction()
        );
    }

    /// Returns a sampled radius multiplier for a shape.
    private static double radiusFor(ShapeSpec shape, double angle) {
        if (shape.lobes() == 0 || shape.amplitude() <= 0.0) {
            return 1.0;
        }

        double wave = Math.cos(shape.lobes() * (angle + Math.PI * 2.0 * shape.phaseOffset()));
        return 1.0 - shape.amplitude() * 0.5 + shape.amplitude() * 0.5 * wave;
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

    /// Describes the current morph between two sampled radial shapes.
    ///
    /// @param start the source shape
    /// @param end the target shape
    /// @param fraction the interpolation fraction from `0.0` to `1.0`
    private record MorphState(ShapeSpec start, ShapeSpec end, double fraction) {
        /// Creates a morph state.
        private MorphState {
        }
    }

    /// Describes a sampled radial shape.
    ///
    /// @param lobes the number of radial lobes, or `0` for a circle
    /// @param amplitude the radial variation amount
    /// @param phaseOffset the angular phase offset, in turns
    private record ShapeSpec(int lobes, double amplitude, double phaseOffset) {
        /// Creates a shape specification.
        private ShapeSpec {
        }
    }
}
