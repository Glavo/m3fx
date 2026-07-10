// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
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

    /// The animated phase used by indeterminate loading.
    private double indeterminatePhase;

    /// The reusable indeterminate morph segment transition.
    private final MorphSegmentTransition indeterminateAnimation = new MorphSegmentTransition();

    /// The independent global rotation animation value.
    private double globalRotation;

    /// The reusable independent global rotation transition.
    private final GlobalRotationTransition globalRotationAnimation = new GlobalRotationTransition();

    /// The active morph segment index.
    private int currentMorphIndex;

    /// The target rotation at the beginning of the active morph segment.
    private double morphRotationTarget = QUARTER_ROTATION;

    /// Updates indeterminate animation state when global or node-local motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(getSkinnable(), this::updateAnimationState);

    /// Updates indeterminate animation state when the scene enters or leaves a real window.
    private final InvalidationListener windowInvalidation = observable -> updateAnimationState();

    /// Updates indeterminate animation state when the control enters or leaves a scene.
    private final ChangeListener<@Nullable Scene> sceneInvalidation = (observable, oldScene, newScene) -> {
        if (oldScene != null) {
            oldScene.windowProperty().removeListener(windowInvalidation);
        }
        if (newScene != null) {
            newScene.windowProperty().addListener(windowInvalidation);
        }
        updateAnimationState();
    };

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

        indeterminateAnimation.setOnFinished(event -> finishIndeterminateMorphSegment());

        control.variantProperty().addListener(layoutInvalidation);
        control.containerSizeProperty().addListener(layoutInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        control.sceneProperty().addListener(sceneInvalidation);
        @Nullable Scene scene = control.getScene();
        if (scene != null) {
            scene.windowProperty().addListener(windowInvalidation);
        }
        updateAnimationState();
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        indeterminateAnimation.stop();
        globalRotationAnimation.stop();
        loadingIndicator.sceneProperty().removeListener(sceneInvalidation);
        @Nullable Scene scene = loadingIndicator.getScene();
        if (scene != null) {
            scene.windowProperty().removeListener(windowInvalidation);
        }
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
        double phase = indeterminatePhase;

        boolean contained = loadingIndicator.getVariant() == M3LoadingIndicatorVariant.CONTAINED;
        container.setVisible(contained);
        if (contained) {
            container.resizeRelocate(x, y, width, height);
        }
        rebuildIndicatorPath(centerX, centerY, indicatorSize, phase);
        indicator.setLayoutX(0.0);
        indicator.setLayoutY(0.0);
        indicator.setOpacity(loadingIndicator.isDisabled() ? 0.38 : 1.0);
    }

    /// Updates the loading animation for the current attachment and motion settings.
    private void updateAnimationState() {
        if (shouldPauseActivityAnimations()) {
            indeterminateAnimation.stop();
            globalRotationAnimation.stop();
            resetIndeterminateAnimationState();
            getSkinnable().requestLayout();
        } else if (M3Animation.shouldReduceMotion(getSkinnable())) {
            indeterminateAnimation.stop();
            resetIndeterminateAnimationState();
            startBasicIndeterminateAnimation();
        } else if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING
                || globalRotationAnimation.getStatus() != Animation.Status.RUNNING) {
            startIndeterminateAnimation();
        }
    }

    /// Starts the indeterminate morph and global rotation loops.
    private void startIndeterminateAnimation() {
        indeterminateAnimation.stop();
        globalRotationAnimation.stop();
        resetIndeterminateAnimationState();
        configureIndeterminateMorphSegment();
        configureGlobalRotationAnimation();
        indeterminateAnimation.playFromStart();
        globalRotationAnimation.playFromStart();
    }

    /// Starts the reduced indeterminate loop used when full motion is disabled.
    private void startBasicIndeterminateAnimation() {
        globalRotationAnimation.stop();
        configureGlobalRotationAnimation();
        globalRotationAnimation.playFromStart();
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
        Duration activeDuration = activeMorphDuration(morphInterval, spec);
        indeterminateAnimation.configure(morphInterval, activeDuration, spec.interpolator(), currentMorphIndex);
    }

    /// Configures the independent global rotation loop.
    private void configureGlobalRotationAnimation() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        globalRotationAnimation.configure(behavior.loadingIndicatorGlobalRotationDuration());
    }

    /// Advances to the next indeterminate morph segment and keeps the loop running.
    private void finishIndeterminateMorphSegment() {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        if (M3Animation.shouldReduceMotion(loadingIndicator)
                || shouldPauseActivityAnimations()) {
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
    private void rebuildIndicatorPath(
            double centerX,
            double centerY,
            double indicatorSize,
            double phase
    ) {
        if (indicatorSize <= 0.0) {
            indicator.getElements().clear();
            return;
        }

        if (M3Animation.shouldReduceMotion(getSkinnable())) {
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
        return scene == null || scene.getWindow() == null;
    }

    /// Returns the active morph duration used before the shape settles for the rest of the interval.
    private static Duration activeMorphDuration(Duration morphInterval, M3MotionSpec spec) {
        double intervalMillis = morphInterval.toMillis();
        if (intervalMillis <= 0.0) {
            return morphInterval;
        }

        double activeMillis = Math.min(spec.duration().toMillis(), intervalMillis * MORPH_ACTIVE_FRACTION);
        return Duration.millis(Math.max(1.0, Math.min(intervalMillis, activeMillis)));
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

        /// Creates a linearly timed segment transition whose inner morph uses the Material easing curve.
        private MorphSegmentTransition() {
            setInterpolator(M3Motion.LINEAR);
        }

        /// Configures the next segment without allocating key frames or writable properties.
        ///
        /// @param interval the complete morph interval
        /// @param activeDuration the part of the interval used for geometric interpolation
        /// @param morphInterpolator the Material easing curve for the active part
        /// @param startPhase the absolute sequence phase at the beginning of the segment
        private void configure(
                Duration interval,
                Duration activeDuration,
                Interpolator morphInterpolator,
                double startPhase
        ) {
            stop();
            setCycleDuration(interval);
            this.morphInterpolator = morphInterpolator;
            this.startPhase = startPhase;
            double intervalMillis = interval.toMillis();
            activeFraction = intervalMillis <= 0.0
                    ? 1.0
                    : clamp(activeDuration.toMillis() / intervalMillis);
        }

        /// Updates the primitive morph phase and requests layout only when the visible shape changes.
        @Override
        protected void interpolate(double fraction) {
            double activeProgress = activeFraction <= 0.0
                    ? 1.0
                    : clamp(fraction / activeFraction);
            double easedProgress = activeProgress >= 1.0
                    ? 1.0
                    : morphInterpolator.interpolate(0.0, 1.0, activeProgress);
            double newPhase = startPhase + easedProgress;
            if (Double.compare(indeterminatePhase, newPhase) != 0) {
                indeterminatePhase = newPhase;
                getSkinnable().requestLayout();
            }
        }
    }

    /// Reuses one indefinite transition for the independent linear rotation channel.
    @NotNullByDefault
    private final class GlobalRotationTransition extends Transition {
        /// Creates a linearly timed indefinite rotation transition.
        private GlobalRotationTransition() {
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

        /// Updates the primitive global rotation and requests the next path layout.
        @Override
        protected void interpolate(double fraction) {
            if (Double.compare(globalRotation, fraction) != 0) {
                globalRotation = fraction;
                getSkinnable().requestLayout();
            }
        }
    }
}
