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
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3LoadingIndicatorVariant;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.shape.M3ShapeMorph;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3LoadingIndicator].
@NotNullByDefault
public class M3LoadingIndicatorSkin extends SkinBase<M3LoadingIndicator> {
    /// The Material loading indicator indeterminate morph sequence.
    private static final M3ShapeMorph.Sequence INDETERMINATE_SEQUENCE =
            M3ShapeMorph.loadingIndicatorIndeterminate();

    /// The Material loading indicator determinate morph sequence.
    private static final M3ShapeMorph.Sequence DETERMINATE_SEQUENCE =
            M3ShapeMorph.loadingIndicatorDeterminate();

    /// The number of default indeterminate shape states.
    private static final int INDETERMINATE_SHAPE_COUNT = INDETERMINATE_SEQUENCE.size();

    /// The rotation added by each morph segment.
    private static final double QUARTER_ROTATION = 0.25;

    /// The AndroidX loading indicator spring damping ratio.
    private static final double MORPH_SPRING_DAMPING_RATIO = 0.6;

    /// The AndroidX loading indicator spring stiffness.
    private static final double MORPH_SPRING_STIFFNESS = 200.0;

    /// The AndroidX loading indicator morph spring visibility threshold.
    private static final double MORPH_SPRING_VISIBILITY_THRESHOLD = 0.1;

    /// The maximum active-shape scale added while an indeterminate morph segment is in flight.
    private static final double MORPH_SCALE_AMPLITUDE = 0.12;

    /// The single active loading shape.
    private final Path indicator = new Path();

    /// The optional contained loading indicator container.
    private final Region container = new Region();

    /// The progress value currently displayed by determinate progress.
    private final DoubleProperty displayedProgress = new SimpleDoubleProperty(this, "displayedProgress");

    /// The determinate progress transition timeline.
    private final Timeline determinateAnimation = new Timeline();

    /// The animated phase used by indeterminate loading.
    private final DoubleProperty indeterminatePhase = new SimpleDoubleProperty(this, "indeterminatePhase");

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    /// The independent global rotation animation value.
    private final DoubleProperty globalRotation = new SimpleDoubleProperty(this, "globalRotation");

    /// The independent global rotation animation timeline.
    private final Timeline globalRotationAnimation = new Timeline();

    /// The active morph segment index.
    private int currentMorphIndex;

    /// The target rotation at the beginning of the active morph segment.
    private double morphRotationTarget = QUARTER_ROTATION;

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

    /// Creates a loading indicator skin.
    ///
    /// @param control the skinned loading indicator
    public M3LoadingIndicatorSkin(M3LoadingIndicator control) {
        super(control);
        container.getStyleClass().add("m3-loading-indicator-container");
        container.setManaged(false);
        indicator.getStyleClass().add("m3-loading-indicator-indicator");
        indicator.setManaged(false);
        getChildren().addAll(container, indicator);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePhase.addListener(animationInvalidation);
        globalRotation.addListener(animationInvalidation);
        globalRotationAnimation.setCycleCount(Animation.INDEFINITE);
        indeterminateAnimation.setOnFinished(event -> finishIndeterminateMorphSegment());

        control.progressProperty().addListener(progressInvalidation);
        control.variantProperty().addListener(layoutInvalidation);
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
        globalRotationAnimation.stop();
        displayedProgress.removeListener(animationInvalidation);
        indeterminatePhase.removeListener(animationInvalidation);
        globalRotation.removeListener(animationInvalidation);
        loadingIndicator.progressProperty().removeListener(progressInvalidation);
        motionSettingsObserver.dispose();
        loadingIndicator.variantProperty().removeListener(layoutInvalidation);
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

        boolean contained = loadingIndicator.getVariant() == M3LoadingIndicatorVariant.CONTAINED;
        container.setVisible(contained);
        if (contained) {
            container.resizeRelocate(x, y, width, height);
        }
        rebuildIndicatorPath(
                centerX,
                centerY,
                indicatorSize,
                phase,
                loadingIndicator.isIndeterminate()
        );
        indicator.setLayoutX(0.0);
        indicator.setLayoutY(0.0);
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
                globalRotationAnimation.stop();
                resetIndeterminateAnimationState();
            } else if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING
                    || globalRotationAnimation.getStatus() != Animation.Status.RUNNING) {
                startIndeterminateAnimation();
            }
        } else {
            indeterminateAnimation.stop();
            globalRotationAnimation.stop();
            resetIndeterminateAnimationState();
            animateDisplayedProgress(clamp(progress), animateDeterminateProgress);
        }
    }

    /// Starts the indeterminate morph and global rotation loops.
    private void startIndeterminateAnimation() {
        resetIndeterminateAnimationState();
        configureIndeterminateMorphSegment();
        configureGlobalRotationAnimation();
        indeterminateAnimation.playFromStart();
        globalRotationAnimation.playFromStart();
    }

    /// Resets indeterminate animation state to the first Compose morph segment.
    private void resetIndeterminateAnimationState() {
        currentMorphIndex = 0;
        morphRotationTarget = QUARTER_ROTATION;
        indeterminatePhase.set(0.0);
        globalRotation.set(0.0);
    }

    /// Configures the active indeterminate morph segment.
    private void configureIndeterminateMorphSegment() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        Duration morphInterval = behavior.loadingIndicatorMorphInterval();
        indeterminateAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(indeterminatePhase, currentMorphIndex, M3Motion.LINEAR)
                ),
                new KeyFrame(
                        morphInterval,
                        new KeyValue(
                                indeterminatePhase,
                                currentMorphIndex + 1.0,
                                new LoadingIndicatorSpringInterpolator(morphInterval.toSeconds())
                        )
                )
        );
    }

    /// Configures the independent global rotation loop.
    private void configureGlobalRotationAnimation() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        globalRotationAnimation.getKeyFrames().setAll(
                new KeyFrame(Duration.ZERO, new KeyValue(globalRotation, 0.0, M3Motion.LINEAR)),
                new KeyFrame(
                        behavior.loadingIndicatorGlobalRotationDuration(),
                        new KeyValue(globalRotation, 1.0, M3Motion.LINEAR)
                )
        );
    }

    /// Advances to the next indeterminate morph segment and keeps the loop running.
    private void finishIndeterminateMorphSegment() {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        if (!loadingIndicator.isIndeterminate() || !M3Animation.areAnimationsEnabled(loadingIndicator)) {
            return;
        }

        currentMorphIndex = (currentMorphIndex + 1) % INDETERMINATE_SHAPE_COUNT;
        morphRotationTarget = positiveModulo(morphRotationTarget + QUARTER_ROTATION, 1.0);
        indeterminatePhase.set(currentMorphIndex);
        configureIndeterminateMorphSegment();
        indeterminateAnimation.playFromStart();
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
            double indicatorSize,
            double phase,
            boolean indeterminate
    ) {
        if (indicatorSize <= 0.0) {
            indicator.getElements().clear();
            return;
        }

        if (indeterminate) {
            double segmentProgress = phase - currentMorphIndex;
            double progress = clamp(segmentProgress);
            INDETERMINATE_SEQUENCE.morphAt(currentMorphIndex).writeTo(
                    indicator,
                    progress,
                    centerX,
                    centerY,
                    indicatorSize,
                    INDETERMINATE_SEQUENCE.scaleFactor(),
                    shapeScaleFor(segmentProgress, true),
                    indeterminateRotationFor(segmentProgress)
            );
        } else {
            double progress = smoothStep(clamp(phase));
            DETERMINATE_SEQUENCE.morphAt(0).writeTo(
                    indicator,
                    progress,
                    centerX,
                    centerY,
                    indicatorSize,
                    DETERMINATE_SEQUENCE.scaleFactor(),
                    1.0,
                    -phase * 0.5
            );
        }
    }

    /// Returns the active shape scale for the current animation state.
    private static double shapeScaleFor(double phase, boolean indeterminate) {
        if (!indeterminate) {
            return 1.0;
        }

        double fraction = clamp(phase);
        double envelope = Math.sin(Math.PI * fraction);
        return 1.0 + MORPH_SCALE_AMPLITUDE * envelope * envelope;
    }

    /// Returns the official-style indeterminate rotation phase for a morph segment.
    private double indeterminateRotationFor(double segmentProgress) {
        return segmentProgress * QUARTER_ROTATION + morphRotationTarget + globalRotation.get();
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

    /// Interpolates the AndroidX loading indicator morph spring over one segment interval.
    @NotNullByDefault
    private static final class LoadingIndicatorSpringInterpolator extends Interpolator {
        /// The duration of one morph segment in seconds.
        private final double durationSeconds;

        /// The AndroidX spring finish time inside one morph segment, in seconds.
        private final double springFinishTimeSeconds;

        /// Creates a spring interpolator for one loading indicator morph segment.
        ///
        /// @param durationSeconds the segment duration in seconds
        private LoadingIndicatorSpringInterpolator(double durationSeconds) {
            this.durationSeconds = Math.max(0.0, durationSeconds);
            this.springFinishTimeSeconds = findSpringFinishTime(this.durationSeconds);
        }

        /// Computes the spring progress for the supplied normalized time.
        @Override
        protected double curve(double t) {
            if (durationSeconds == 0.0) {
                return 1.0;
            }

            double elapsedSeconds = clamp(t) * durationSeconds;
            if (elapsedSeconds >= springFinishTimeSeconds) {
                return 1.0;
            }
            return springResponse(elapsedSeconds);
        }

        /// Returns the AndroidX spring finish time for one morph segment.
        ///
        /// @param durationSeconds the segment duration in seconds
        /// @return the spring finish time in seconds
        private static double findSpringFinishTime(double durationSeconds) {
            if (durationSeconds <= 0.0) {
                return 0.0;
            }

            double naturalFrequency = Math.sqrt(MORPH_SPRING_STIFFNESS);
            double decayRate = MORPH_SPRING_DAMPING_RATIO * naturalFrequency;
            double undamped = Math.sqrt(1.0 - MORPH_SPRING_DAMPING_RATIO * MORPH_SPRING_DAMPING_RATIO);
            double envelopeAmplitude = 1.0 / undamped;
            if (envelopeAmplitude <= MORPH_SPRING_VISIBILITY_THRESHOLD) {
                return 0.0;
            }

            double finishSeconds = Math.log(envelopeAmplitude / MORPH_SPRING_VISIBILITY_THRESHOLD) / decayRate;
            return Math.min(durationSeconds, finishSeconds);
        }

        /// Returns the unit step response for the AndroidX spring parameters.
        ///
        /// @param elapsedSeconds the elapsed time in seconds
        /// @return the spring response
        private static double springResponse(double elapsedSeconds) {
            double naturalFrequency = Math.sqrt(MORPH_SPRING_STIFFNESS);
            double dampedFrequency = naturalFrequency * Math.sqrt(1.0 - MORPH_SPRING_DAMPING_RATIO
                    * MORPH_SPRING_DAMPING_RATIO);
            double decay = Math.exp(-MORPH_SPRING_DAMPING_RATIO * naturalFrequency * elapsedSeconds);
            double phase = dampedFrequency * elapsedSeconds;
            double sineScale = MORPH_SPRING_DAMPING_RATIO
                    / Math.sqrt(1.0 - MORPH_SPRING_DAMPING_RATIO * MORPH_SPRING_DAMPING_RATIO);
            return 1.0 - decay * (Math.cos(phase) + sineScale * Math.sin(phase));
        }
    }
}
