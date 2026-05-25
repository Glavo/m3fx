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
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3LoadingIndicator].
@NotNullByDefault
public class M3LoadingIndicatorSkin extends SkinBase<M3LoadingIndicator> {
    /// The number of visible loading indicator shapes.
    private static final int SHAPE_COUNT = 4;

    /// SVG paths normalized to a 24 by 24 viewport.
    private static final String[] SHAPE_PATHS = {
            "M12 2A10 10 0 1 1 12 22A10 10 0 1 1 12 2",
            "M5 3H19A2 2 0 0 1 21 5V19A2 2 0 0 1 19 21H5A2 2 0 0 1 3 19V5A2 2 0 0 1 5 3",
            "M12 2L22 19A2 2 0 0 1 20.25 22H3.75A2 2 0 0 1 2 19L12 2",
            "M12 2L22 12L12 22L2 12Z"
    };

    /// The minimum visual scale for inactive shapes.
    private static final double INACTIVE_SCALE = 0.58;

    /// The minimum visual opacity for inactive shapes.
    private static final double INACTIVE_OPACITY = 0.36;

    /// The loading shapes.
    private final SVGPath[] shapes = new SVGPath[SHAPE_COUNT];

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
        for (int i = 0; i < SHAPE_COUNT; i++) {
            SVGPath shape = new SVGPath();
            shape.getStyleClass().add("m3-loading-indicator-shape");
            shape.setContent(SHAPE_PATHS[i]);
            shape.setManaged(false);
            shapes[i] = shape;
        }
        getChildren().addAll(shapes);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePhase.addListener(animationInvalidation);
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);

        control.progressProperty().addListener(progressInvalidation);
        control.indicatorSizeProperty().addListener(layoutInvalidation);
        control.shapeSizeProperty().addListener(layoutInvalidation);
        control.shapeSpacingProperty().addListener(layoutInvalidation);
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
        loadingIndicator.indicatorSizeProperty().removeListener(layoutInvalidation);
        loadingIndicator.shapeSizeProperty().removeListener(layoutInvalidation);
        loadingIndicator.shapeSpacingProperty().removeListener(layoutInvalidation);
        super.dispose();
    }

    /// Lays out the four loading shapes inside the control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3LoadingIndicator loadingIndicator = getSkinnable();
        double shapeSize = Math.min(loadingIndicator.getShapeSize(), Math.min(width, height));
        double spacing = loadingIndicator.getShapeSpacing();
        double contentWidth = shapeSize * SHAPE_COUNT + spacing * (SHAPE_COUNT - 1);
        double startX = x + (width - contentWidth) / 2.0;
        double centerY = y + height / 2.0;

        for (int i = 0; i < SHAPE_COUNT; i++) {
            SVGPath shape = shapes[i];
            double scale = scaleForShape(i);
            double opacity = opacityForShape(i);
            double baseX = startX + i * (shapeSize + spacing);
            shape.resizeRelocate(baseX, centerY - shapeSize / 2.0, shapeSize, shapeSize);
            shape.setScaleX(scaleForSvg(shape, shapeSize) * scale);
            shape.setScaleY(scaleForSvg(shape, shapeSize) * scale);
            shape.setOpacity(opacity);
        }
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
                        new KeyValue(indeterminatePhase, SHAPE_COUNT, M3Motion.LINEAR)
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

    /// Returns the visual scale for a shape.
    private double scaleForShape(int index) {
        if (getSkinnable().isIndeterminate()) {
            double distance = cyclicDistance(indeterminatePhase.get(), index);
            double activity = Math.max(0.0, 1.0 - distance);
            return INACTIVE_SCALE + (1.0 - INACTIVE_SCALE) * activity;
        }

        double shapeProgress = clamp(displayedProgress.get() * SHAPE_COUNT - index);
        return INACTIVE_SCALE + (1.0 - INACTIVE_SCALE) * shapeProgress;
    }

    /// Returns the visual opacity for a shape.
    private double opacityForShape(int index) {
        if (getSkinnable().isIndeterminate()) {
            double distance = cyclicDistance(indeterminatePhase.get(), index);
            double activity = Math.max(0.0, 1.0 - distance);
            return INACTIVE_OPACITY + (1.0 - INACTIVE_OPACITY) * activity;
        }

        double shapeProgress = clamp(displayedProgress.get() * SHAPE_COUNT - index);
        return INACTIVE_OPACITY + (1.0 - INACTIVE_OPACITY) * shapeProgress;
    }

    /// Returns a normalized cyclic distance between an animation phase and shape index.
    private static double cyclicDistance(double phase, int index) {
        double wrapped = phase % SHAPE_COUNT;
        double distance = Math.abs(wrapped - index);
        return Math.min(distance, SHAPE_COUNT - distance);
    }

    /// Returns the initial displayed progress value for a public progress value.
    private static double initialDisplayedProgress(double progress) {
        return progress == M3LoadingIndicator.INDETERMINATE_PROGRESS ? 0.0 : clamp(progress);
    }

    /// Returns the SVG scale needed to fit a path into the requested shape size.
    private static double scaleForSvg(SVGPath shape, double shapeSize) {
        double width = shape.prefWidth(-1.0);
        double height = shape.prefHeight(-1.0);
        double sourceSize = Math.max(width, height);
        return sourceSize <= 0.0 ? 1.0 : shapeSize / sourceSize;
    }

    /// Clamps a progress value to the visible range.
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
