// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.stage.Window;
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
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3LoadingIndicator].
@NotNullByDefault
public class M3LoadingIndicatorSkin extends SkinBase<M3LoadingIndicator> {
    /// The Material loading indicator indeterminate morph sequence.
    private static final M3ShapeMorph.Sequence INDETERMINATE_SEQUENCE =
            M3ShapeMorph.loadingIndicatorIndeterminate();

    /// The number of default indeterminate shape states.
    private static final int INDETERMINATE_SHAPE_COUNT = INDETERMINATE_SEQUENCE.size();

    /// The rotation added by each morph segment.
    private static final double QUARTER_ROTATION = 0.25;

    /// The maximum active-shape scale added while an indeterminate morph segment is in flight.
    private static final double MORPH_SCALE_AMPLITUDE = 0.12;

    /// The part of each morph interval used by the shape interpolation before the target shape settles.
    private static final double MORPH_ACTIVE_FRACTION = 0.72;

    /// The single active loading shape.
    private final Path indicator = new Path();

    /// The optional contained loading indicator container.
    private final Region container = new Region();

    /// The reusable shape morph scratch storage.
    private final M3ShapeMorph.Scratch shapeScratch = new M3ShapeMorph.Scratch();

    /// The resolved indicator center x-coordinate from the latest layout.
    private double indicatorCenterX;

    /// The resolved indicator center y-coordinate from the latest layout.
    private double indicatorCenterY;

    /// The resolved active indicator size from the latest layout.
    private double resolvedIndicatorSize;

    /// Whether the indicator has received valid layout geometry.
    private boolean indicatorGeometryReady;

    /// The animated phase used by indeterminate loading.
    private double indeterminatePhase;

    /// The reusable indeterminate morph segment transition.
    private final MorphSegmentTransition indeterminateAnimation = new MorphSegmentTransition();

    /// The independent global rotation animation value.
    private double globalRotation;

    /// The reusable fixed-shape rotation transition used by reduced motion.
    private final BasicRotationTransition basicRotationAnimation = new BasicRotationTransition();

    /// The active morph segment index.
    private int currentMorphIndex;

    /// The target rotation at the beginning of the active morph segment.
    private double morphRotationTarget = QUARTER_ROTATION;

    /// Whether the current inherited motion settings require reduced-motion rendering.
    private boolean reducedMotion;

    /// Updates indeterminate animation state when global or node-local motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(getSkinnable(), this::updateAnimationState);

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
        getChildren().setAll(container, indicator);

        indeterminateAnimation.setOnFinished(event -> finishIndeterminateMorphSegment());

        control.variantProperty().addListener(layoutInvalidation);
        control.containerSizeProperty().addListener(layoutInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        updateAnimationState();
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        indeterminateAnimation.stop();
        indeterminateAnimation.setOnFinished(null);
        basicRotationAnimation.stop();
        motionSettingsObserver.dispose();
        loadingIndicator.variantProperty().removeListener(layoutInvalidation);
        loadingIndicator.containerSizeProperty().removeListener(layoutInvalidation);
        loadingIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        getChildren().removeAll(container, indicator);
        super.dispose();
    }

    /// Lays out the active loading shape inside the control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        double indicatorSize = Math.min(loadingIndicator.getIndicatorSize(), Math.min(width, height));
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        indicatorCenterX = centerX;
        indicatorCenterY = centerY;
        resolvedIndicatorSize = indicatorSize;
        indicatorGeometryReady = true;

        boolean contained = loadingIndicator.getVariant() == M3LoadingIndicatorVariant.CONTAINED;
        container.setVisible(contained);
        if (contained) {
            container.resizeRelocate(x, y, width, height);
        }
        updateIndicatorPath();
        indicator.setLayoutX(0.0);
        indicator.setLayoutY(0.0);
    }

    /// Updates the loading animation for the current attachment and motion settings.
    private void updateAnimationState() {
        reducedMotion = M3Animation.shouldReduceMotion(getSkinnable());
        if (shouldPauseActivityAnimations()) {
            indeterminateAnimation.stop();
            basicRotationAnimation.stop();
            resetIndeterminateAnimationState();
            updateIndicatorPath();
        } else if (reducedMotion) {
            indeterminateAnimation.stop();
            resetIndeterminateAnimationState();
            startBasicIndeterminateAnimation();
        } else if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
            startIndeterminateAnimation();
        }
    }

    /// Starts the indeterminate morph and global rotation loops.
    private void startIndeterminateAnimation() {
        indeterminateAnimation.stop();
        basicRotationAnimation.stop();
        resetIndeterminateAnimationState();
        configureIndeterminateMorphSegment();
        indeterminateAnimation.playFromStart();
    }

    /// Starts the reduced indeterminate loop used when full motion is disabled.
    private void startBasicIndeterminateAnimation() {
        basicRotationAnimation.stop();
        configureBasicRotationAnimation();
        basicRotationAnimation.playFromStart();
    }

    /// Resets indeterminate animation state to the first Compose morph segment.
    private void resetIndeterminateAnimationState() {
        currentMorphIndex = 0;
        morphRotationTarget = QUARTER_ROTATION;
        indeterminatePhase = 0.0;
        globalRotation = 0.0;
    }

    /// Configures the active indeterminate morph segment.
    private void configureIndeterminateMorphSegment() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        M3MotionSpec spec = M3Animation.defaultSpatial(getSkinnable());
        Duration morphInterval = behavior.loadingIndicatorMorphInterval();
        indeterminateAnimation.configure(
                morphInterval,
                activeMorphDurationMillis(morphInterval, spec),
                behavior.loadingIndicatorGlobalRotationDuration(),
                spec.interpolator(),
                currentMorphIndex
        );
    }

    /// Configures the fixed-shape reduced-motion rotation loop.
    private void configureBasicRotationAnimation() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        basicRotationAnimation.configure(behavior.loadingIndicatorGlobalRotationDuration());
    }

    /// Advances to the next indeterminate morph segment and keeps the loop running.
    private void finishIndeterminateMorphSegment() {
        if (reducedMotion || shouldPauseActivityAnimations()) {
            return;
        }

        indeterminateAnimation.stop();
        currentMorphIndex = (currentMorphIndex + 1) % INDETERMINATE_SHAPE_COUNT;
        morphRotationTarget = positiveUnitModulo(morphRotationTarget + QUARTER_ROTATION);
        indeterminatePhase = currentMorphIndex;
        configureIndeterminateMorphSegment();
        indeterminateAnimation.playFromStart();
    }

    /// Rebuilds the active shape path for the current animation state.
    private void updateIndicatorPath() {
        if (!indicatorGeometryReady) {
            return;
        }

        double indicatorSize = resolvedIndicatorSize;
        if (indicatorSize <= 0.0) {
            indicator.getElements().clear();
            return;
        }

        double centerX = indicatorCenterX;
        double centerY = indicatorCenterY;
        double phase = indeterminatePhase;

        if (reducedMotion) {
            INDETERMINATE_SEQUENCE.morphAt(0).writeTo(
                    indicator,
                    0.0,
                    centerX,
                    centerY,
                    indicatorSize,
                    INDETERMINATE_SEQUENCE.scaleFactor(),
                    1.0,
                    globalRotation,
                    shapeScratch
            );
            return;
        }

        double segmentProgress = phase - currentMorphIndex;
        double progress = clamp(segmentProgress);
        INDETERMINATE_SEQUENCE.morphAt(currentMorphIndex).writeTo(
                indicator,
                progress,
                centerX,
                centerY,
                indicatorSize,
                INDETERMINATE_SEQUENCE.scaleFactor(),
                shapeScaleFor(segmentProgress),
                indeterminateRotationFor(segmentProgress),
                shapeScratch
        );
    }

    /// Returns the active shape scale for the current animation state.
    private static double shapeScaleFor(double phase) {
        double fraction = clamp(phase);
        double envelope = Math.sin(Math.PI * fraction);
        return 1.0 + MORPH_SCALE_AMPLITUDE * envelope * envelope;
    }

    /// Returns the official-style indeterminate rotation phase for a morph segment.
    private double indeterminateRotationFor(double segmentProgress) {
        return clamp(segmentProgress) * QUARTER_ROTATION + morphRotationTarget + globalRotation;
    }

    /// Returns whether activity animations should pause for the current scene attachment state.
    private boolean shouldPauseActivityAnimations() {
        @Nullable Scene scene = getSkinnable().getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window == null || !window.isShowing();
    }

    /// Returns the active morph duration used before the shape settles for the rest of the interval.
    private static double activeMorphDurationMillis(Duration morphInterval, M3MotionSpec spec) {
        double intervalMillis = morphInterval.toMillis();
        if (intervalMillis <= 0.0) {
            return intervalMillis;
        }

        double activeMillis = Math.min(spec.duration().toMillis(), intervalMillis * MORPH_ACTIVE_FRACTION);
        return Math.max(1.0, Math.min(intervalMillis, activeMillis));
    }

    /// Returns a positive modulo result in the unit interval.
    private static double positiveUnitModulo(double value) {
        double result = value % 1.0;
        return result < 0.0 ? result + 1.0 : result;
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Reuses one transition object for every indeterminate morph segment.
    @NotNullByDefault
    private final class MorphSegmentTransition extends Transition {
        /// The easing curve applied during the active part of the segment.
        private Interpolator morphInterpolator = M3Motion.LINEAR;

        /// The absolute sequence phase at the beginning of the segment.
        private double startPhase;

        /// The normalized part of the cycle occupied by active morphing.
        private double activeFraction = 1.0;

        /// The global rotation at the beginning of the segment.
        private double startRotation;

        /// The global rotation added over the complete segment.
        private double rotationDelta;

        /// Creates a linearly timed segment transition whose inner morph uses the Material easing curve.
        private MorphSegmentTransition() {
            setInterpolator(M3Motion.LINEAR);
        }

        /// Configures the next segment without allocating key frames or writable properties.
        ///
        /// @param interval the complete morph interval
        /// @param activeDurationMillis the part of the interval used for geometric interpolation, in milliseconds
        /// @param rotationDuration the duration of one complete independent rotation
        /// @param morphInterpolator the Material easing curve for the active part
        /// @param startPhase the absolute sequence phase at the beginning of the segment
        private void configure(
                Duration interval,
                double activeDurationMillis,
                Duration rotationDuration,
                Interpolator morphInterpolator,
                double startPhase
        ) {
            stop();
            setCycleDuration(interval);
            this.morphInterpolator = morphInterpolator;
            this.startPhase = startPhase;
            this.startRotation = globalRotation;
            double intervalMillis = interval.toMillis();
            activeFraction = intervalMillis <= 0.0
                    ? 1.0
                    : clamp(activeDurationMillis / intervalMillis);
            double rotationMillis = rotationDuration.toMillis();
            rotationDelta = rotationMillis <= 0.0 ? 0.0 : intervalMillis / rotationMillis;
        }

        /// Updates the primitive morph phase and rewrites the path only when the visible shape changes.
        @Override
        protected void interpolate(double fraction) {
            double activeProgress = activeFraction <= 0.0
                    ? 1.0
                    : clamp(fraction / activeFraction);
            double easedProgress = activeProgress >= 1.0
                    ? 1.0
                    : morphInterpolator.interpolate(0.0, 1.0, activeProgress);
            double newPhase = startPhase + easedProgress;
            double newRotation = positiveUnitModulo(startRotation + rotationDelta * fraction);
            if (Double.compare(indeterminatePhase, newPhase) != 0
                    || Double.compare(globalRotation, newRotation) != 0) {
                indeterminatePhase = newPhase;
                globalRotation = newRotation;
                updateIndicatorPath();
            }
        }
    }

    /// Reuses one indefinite transition for reduced-motion fixed-shape rotation.
    @NotNullByDefault
    private final class BasicRotationTransition extends Transition {
        /// Creates a linearly timed indefinite reduced-motion rotation transition.
        private BasicRotationTransition() {
            setInterpolator(M3Motion.LINEAR);
            setCycleCount(Animation.INDEFINITE);
        }

        /// Updates the rotation cycle duration without allocating key frames or writable properties.
        ///
        /// @param duration the complete rotation duration
        private void configure(Duration duration) {
            stop();
            setCycleDuration(duration);
        }

        /// Updates the primitive global rotation and rewrites the path directly.
        @Override
        protected void interpolate(double fraction) {
            if (Double.compare(globalRotation, fraction) != 0) {
                globalRotation = fraction;
                updateIndicatorPath();
            }
        }
    }
}
