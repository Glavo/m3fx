// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

    // The animated phase used by indeterminate loading.
    private final DoubleProperty indeterminatePhase = new SimpleDoubleProperty(this, "indeterminatePhase");

    /// The indeterminate animation timeline.
    private final Timeline indeterminateAnimation = new Timeline();

    // The independent global rotation animation value.
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

        indeterminatePhase.addListener(animationInvalidation);
        globalRotation.addListener(animationInvalidation);
        globalRotationAnimation.setCycleCount(Animation.INDEFINITE);
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
        indeterminatePhase.removeListener(animationInvalidation);
        globalRotation.removeListener(animationInvalidation);
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
        double phase = indeterminatePhase.get();

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
        indeterminatePhase.set(0.0);
        globalRotation.set(0.0);
    }

    /// Configures the active indeterminate morph segment.
    private void configureIndeterminateMorphSegment() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        M3MotionSpec spec = M3Animation.defaultSpatial(getSkinnable());
        Duration morphInterval = behavior.loadingIndicatorMorphInterval();
        Duration activeDuration = activeMorphDuration(morphInterval, spec);
        if (activeDuration.equals(morphInterval)) {
            replaceKeyFrames(indeterminateAnimation,
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(indeterminatePhase, currentMorphIndex, M3Motion.LINEAR)
                    ),
                    new KeyFrame(
                            morphInterval,
                            new KeyValue(
                                    indeterminatePhase,
                                    currentMorphIndex + 1.0,
                                    spec.interpolator()
                            )
                    )
            );
        } else {
            replaceKeyFrames(indeterminateAnimation,
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(indeterminatePhase, currentMorphIndex, M3Motion.LINEAR)
                    ),
                    new KeyFrame(
                            activeDuration,
                            new KeyValue(
                                    indeterminatePhase,
                                    currentMorphIndex + 1.0,
                                    spec.interpolator()
                            )
                    ),
                    new KeyFrame(
                            morphInterval,
                            new KeyValue(indeterminatePhase, currentMorphIndex + 1.0, M3Motion.LINEAR)
                    )
            );
        }
    }

    /// Configures the independent global rotation loop.
    private void configureGlobalRotationAnimation() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        replaceKeyFrames(globalRotationAnimation,
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
        if (M3Animation.shouldReduceMotion(loadingIndicator)
                || shouldPauseActivityAnimations()) {
            return;
        }

        indeterminateAnimation.stop();
        currentMorphIndex = (currentMorphIndex + 1) % INDETERMINATE_SHAPE_COUNT;
        morphRotationTarget = positiveUnitModulo(morphRotationTarget + QUARTER_ROTATION);
        indeterminatePhase.set(currentMorphIndex);
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
                    globalRotation.get(),
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
        return clamp(segmentProgress) * QUARTER_ROTATION + morphRotationTarget + globalRotation.get();
    }

    /// Returns whether activity animations should pause for the current scene attachment state.
    private boolean shouldPauseActivityAnimations() {
        @Nullable Scene scene = getSkinnable().getScene();
        return scene == null || scene.getWindow() == null;
    }

    /// Replaces timeline key frames without mutating a running timeline.
    private static void replaceKeyFrames(Timeline timeline, KeyFrame... keyFrames) {
        timeline.stop();
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(keyFrames);
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
}
