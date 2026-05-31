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
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3LoadingIndicatorVariant;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3LoadingIndicator].
@NotNullByDefault
public class M3LoadingIndicatorSkin extends SkinBase<M3LoadingIndicator> {
    /// The number of sampled points used for the generated active shape.
    private static final int SAMPLE_COUNT = 96;

    /// The number of default indeterminate shape states.
    private static final int INDETERMINATE_SHAPE_COUNT = 7;

    /// The spring-like interpolator used for each morph segment.
    private static final Interpolator MORPH_INTERPOLATOR = Interpolator.SPLINE(0.20, 0.00, 0.00, 1.00);

    /// Shape states used by the indeterminate morphing loop.
    ///
    /// The order follows the Material 3 Expressive default polygon sequence: soft burst, cookie 9-sided,
    /// pentagon, pill, sunny, cookie 4-sided, and oval.
    private static final double[][][] INDETERMINATE_SHAPES = {
            sampledRoundedStar(10, 0.65, 1.0 / 20.0, 2),
            sampledRoundedStar(9, 0.80, -0.25, 4),
            sampledRoundedPolygon(5, -1.0 / 20.0, 4),
            sampledSuperellipse(1.25, 1.0, 4.5, -0.125),
            sampledRoundedStar(8, 0.80, 0.0, 3),
            sampledRoundedStar(4, 0.50, -0.125, 4),
            sampledEllipse(1.0, 0.70, -0.125)
    };

    /// The determinate starting shape.
    private static final double[][] DETERMINATE_START_SHAPE =
            sampledEllipse(1.0, 1.0, 0.0);

    /// The determinate completed shape.
    private static final double[][] DETERMINATE_END_SHAPE =
            sampledRoundedStar(10, 0.65, 1.0 / 20.0, 2);

    /// The single active loading shape.
    private final Path indicator = new Path();

    /// The optional contained loading indicator container.
    private final Region container = new Region();

    /// The reusable first path point.
    private final MoveTo firstPoint = new MoveTo();

    /// The reusable remaining path points.
    private final LineTo[] remainingPoints = new LineTo[SAMPLE_COUNT - 1];

    /// The reusable x-coordinate samples for the current frame.
    private final double[] sampledX = new double[SAMPLE_COUNT];

    /// The reusable y-coordinate samples for the current frame.
    private final double[] sampledY = new double[SAMPLE_COUNT];

    /// The minimum x-coordinate of the current sampled polygon bounds.
    private double sampledMinX;

    /// The minimum y-coordinate of the current sampled polygon bounds.
    private double sampledMinY;

    /// The maximum x-coordinate of the current sampled polygon bounds.
    private double sampledMaxX;

    /// The maximum y-coordinate of the current sampled polygon bounds.
    private double sampledMaxY;

    /// The x-coordinate of the current sampled polygon centroid.
    private double sampledCentroidX;

    /// The y-coordinate of the current sampled polygon centroid.
    private double sampledCentroidY;

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
        indicator.getElements().add(firstPoint);
        for (int i = 0; i < remainingPoints.length; i++) {
            LineTo point = new LineTo();
            remainingPoints[i] = point;
            indicator.getElements().add(point);
        }
        indicator.getElements().add(new ClosePath());
        getChildren().addAll(container, indicator);

        displayedProgress.set(initialDisplayedProgress(control.getProgress()));
        displayedProgress.addListener(animationInvalidation);
        indeterminatePhase.addListener(animationInvalidation);
        globalRotation.addListener(animationInvalidation);
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);
        globalRotationAnimation.setCycleCount(Animation.INDEFINITE);

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
                indicatorSize / 2.0,
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
                indeterminatePhase.set(0.0);
                globalRotation.set(0.0);
            } else if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
                configureIndeterminateAnimation();
                indeterminateAnimation.playFromStart();
                globalRotationAnimation.playFromStart();
            }
        } else {
            indeterminateAnimation.stop();
            globalRotationAnimation.stop();
            indeterminatePhase.set(0.0);
            globalRotation.set(0.0);
            animateDisplayedProgress(clamp(progress), animateDeterminateProgress);
        }
    }

    /// Configures the indeterminate morph and global rotation loops.
    private void configureIndeterminateAnimation() {
        M3MotionBehavior behavior = M3Animation.motionBehavior(getSkinnable());
        Duration morphInterval = behavior.loadingIndicatorMorphInterval();
        indeterminateAnimation.getKeyFrames().clear();
        indeterminateAnimation.getKeyFrames().add(new KeyFrame(
                Duration.ZERO,
                new KeyValue(indeterminatePhase, 0.0, MORPH_INTERPOLATOR)
        ));
        for (int i = 1; i <= INDETERMINATE_SHAPE_COUNT; i++) {
            indeterminateAnimation.getKeyFrames().add(new KeyFrame(
                    morphInterval.multiply(i),
                    new KeyValue(indeterminatePhase, i, MORPH_INTERPOLATOR)
            ));
        }

        globalRotationAnimation.getKeyFrames().setAll(
                new KeyFrame(Duration.ZERO, new KeyValue(globalRotation, 0.0, M3Motion.LINEAR)),
                new KeyFrame(
                        behavior.loadingIndicatorGlobalRotationDuration(),
                        new KeyValue(globalRotation, 1.0, M3Motion.LINEAR)
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
        if (radius <= 0.0) {
            return;
        }

        double rotation = indeterminate ? indeterminateRotationFor(phase) : -phase * 0.5;
        double rotationRadians = Math.PI * 2.0 * rotation;
        double rotationCos = Math.cos(rotationRadians);
        double rotationSin = Math.sin(rotationRadians);
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            double shapeX = shapeXFor(phase, i, indeterminate) * radius;
            double shapeY = shapeYFor(phase, i, indeterminate) * radius;
            sampledX[i] = centerX + rotationCos * shapeX - rotationSin * shapeY;
            sampledY[i] = centerY + rotationSin * shapeX + rotationCos * shapeY;
        }

        updatePolygonBounds();
        updatePolygonCentroid();
        double offsetX = centerX - sampledCentroidX;
        double offsetY = centerY - sampledCentroidY;
        firstPoint.setX(sampledX[0] + offsetX);
        firstPoint.setY(sampledY[0] + offsetY);
        for (int i = 1; i < SAMPLE_COUNT; i++) {
            LineTo point = remainingPoints[i - 1];
            point.setX(sampledX[i] + offsetX);
            point.setY(sampledY[i] + offsetY);
        }
    }

    /// Updates the centroid of the currently sampled polygon.
    private void updatePolygonCentroid() {
        double signedArea = 0.0;
        double centroidX = 0.0;
        double centroidY = 0.0;
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            int next = (i + 1) % SAMPLE_COUNT;
            double cross = sampledX[i] * sampledY[next] - sampledX[next] * sampledY[i];
            signedArea += cross;
            centroidX += (sampledX[i] + sampledX[next]) * cross;
            centroidY += (sampledY[i] + sampledY[next]) * cross;
        }

        if (Math.abs(signedArea) < 0.000001) {
            sampledCentroidX = (sampledMinX + sampledMaxX) / 2.0;
            sampledCentroidY = (sampledMinY + sampledMaxY) / 2.0;
            return;
        }
        sampledCentroidX = centroidX / (3.0 * signedArea);
        sampledCentroidY = centroidY / (3.0 * signedArea);
    }

    /// Updates the visual bounds of the currently sampled polygon.
    private void updatePolygonBounds() {
        sampledMinX = sampledX[0];
        sampledMinY = sampledY[0];
        sampledMaxX = sampledX[0];
        sampledMaxY = sampledY[0];

        for (int i = 1; i < SAMPLE_COUNT; i++) {
            sampledMinX = Math.min(sampledMinX, sampledX[i]);
            sampledMinY = Math.min(sampledMinY, sampledY[i]);
            sampledMaxX = Math.max(sampledMaxX, sampledX[i]);
            sampledMaxY = Math.max(sampledMaxY, sampledY[i]);
        }
    }

    /// Returns a morph interpolated x-coordinate for one sampled point.
    private static double shapeXFor(double phase, int sampleIndex, boolean indeterminate) {
        return indeterminate
                ? indeterminatePointFor(phase, sampleIndex, 0)
                : determinatePointFor(phase, sampleIndex, 0);
    }

    /// Returns a morph interpolated y-coordinate for one sampled point.
    private static double shapeYFor(double phase, int sampleIndex, boolean indeterminate) {
        return indeterminate
                ? indeterminatePointFor(phase, sampleIndex, 1)
                : determinatePointFor(phase, sampleIndex, 1);
    }

    /// Returns an interpolated indeterminate point coordinate.
    private static double indeterminatePointFor(double phase, int sampleIndex, int axis) {
        double normalized = positiveModulo(phase, INDETERMINATE_SHAPE_COUNT);
        int index = (int) Math.floor(normalized);
        double fraction = normalized - index;
        double[][] current = INDETERMINATE_SHAPES[index];
        double[][] next = INDETERMINATE_SHAPES[(index + 1) % INDETERMINATE_SHAPE_COUNT];
        return interpolate(current[axis][sampleIndex], next[axis][sampleIndex], fraction);
    }

    /// Returns the official-style indeterminate rotation phase for a morph segment.
    private double indeterminateRotationFor(double phase) {
        double normalized = positiveModulo(phase, INDETERMINATE_SHAPE_COUNT);
        return (normalized + 1.0) * 0.25 + globalRotation.get();
    }

    /// Returns an interpolated determinate point coordinate.
    private static double determinatePointFor(double progress, int sampleIndex, int axis) {
        double fraction = smoothStep(clamp(progress));
        return interpolate(DETERMINATE_START_SHAPE[axis][sampleIndex], DETERMINATE_END_SHAPE[axis][sampleIndex], fraction);
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

    /// Creates a smoothed sampled star shape.
    private static double[][] sampledRoundedStar(
            int lobes,
            double innerRadius,
            double rotationTurns,
            int smoothingPasses
    ) {
        double[][] points = new double[lobes * 2][2];
        for (int i = 0; i < points.length; i++) {
            double angle = Math.PI * 2.0 * i / points.length + rotationTurns * Math.PI * 2.0;
            double radius = i % 2 == 0 ? 1.0 : innerRadius;
            points[i][0] = Math.cos(angle) * radius;
            points[i][1] = Math.sin(angle) * radius;
        }
        return sampledSmoothedClosedPolyline(points, smoothingPasses);
    }

    /// Creates a smoothed sampled regular polygon shape.
    private static double[][] sampledRoundedPolygon(int vertices, double rotationTurns, int smoothingPasses) {
        double[][] points = new double[vertices][2];
        for (int i = 0; i < vertices; i++) {
            double angle = Math.PI * 2.0 * i / vertices + rotationTurns * Math.PI * 2.0;
            points[i][0] = Math.cos(angle);
            points[i][1] = Math.sin(angle);
        }
        return sampledSmoothedClosedPolyline(points, smoothingPasses);
    }

    /// Creates a sampled ellipse shape.
    private static double[][] sampledEllipse(double scaleX, double scaleY, double rotationTurns) {
        return sampledSuperellipse(scaleX, scaleY, 2.0, rotationTurns);
    }

    /// Creates a sampled superellipse shape.
    private static double[][] sampledSuperellipse(
            double scaleX,
            double scaleY,
            double exponent,
            double rotationTurns
    ) {
        double[][] shape = new double[2][SAMPLE_COUNT];
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            double angle = Math.PI * 2.0 * i / SAMPLE_COUNT;
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            double radius = Math.pow(
                    Math.pow(Math.abs(cosine) / scaleX, exponent)
                            + Math.pow(Math.abs(sine) / scaleY, exponent),
                    -1.0 / exponent
            );
            shape[0][i] = cosine * radius;
            shape[1][i] = sine * radius;
        }
        rotateShape(shape, rotationTurns);
        normalizeShape(shape);
        return shape;
    }

    /// Creates a sampled shape from a Chaikin-smoothed closed polyline.
    private static double[][] sampledSmoothedClosedPolyline(double[][] points, int smoothingPasses) {
        double[][] smoothedPoints = points;
        for (int i = 0; i < smoothingPasses; i++) {
            smoothedPoints = chaikinSmooth(smoothedPoints);
        }
        double[][] shape = resampleRadialClosedPolyline(smoothedPoints);
        normalizeShape(shape);
        return shape;
    }

    /// Returns one Chaikin smoothing pass for a closed polyline.
    private static double[][] chaikinSmooth(double[][] points) {
        double[][] smoothed = new double[points.length * 2][2];
        for (int i = 0; i < points.length; i++) {
            double[] current = points[i];
            double[] next = points[(i + 1) % points.length];
            smoothed[i * 2][0] = current[0] * 0.75 + next[0] * 0.25;
            smoothed[i * 2][1] = current[1] * 0.75 + next[1] * 0.25;
            smoothed[i * 2 + 1][0] = current[0] * 0.25 + next[0] * 0.75;
            smoothed[i * 2 + 1][1] = current[1] * 0.25 + next[1] * 0.75;
        }
        return smoothed;
    }

    /// Resamples a star-shaped closed polyline by fixed center-ray angles.
    private static double[][] resampleRadialClosedPolyline(double[][] points) {
        double[][] shape = new double[2][SAMPLE_COUNT];
        for (int sampleIndex = 0; sampleIndex < SAMPLE_COUNT; sampleIndex++) {
            double angle = Math.PI * 2.0 * sampleIndex / SAMPLE_COUNT;
            double directionX = Math.cos(angle);
            double directionY = Math.sin(angle);
            double radius = radialIntersection(points, directionX, directionY);
            shape[0][sampleIndex] = directionX * radius;
            shape[1][sampleIndex] = directionY * radius;
        }
        return shape;
    }

    /// Returns the positive ray intersection radius for a closed polyline.
    private static double radialIntersection(double[][] points, double directionX, double directionY) {
        double bestRadius = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i++) {
            double[] current = points[i];
            double[] next = points[(i + 1) % points.length];
            double edgeX = next[0] - current[0];
            double edgeY = next[1] - current[1];
            double denominator = cross(directionX, directionY, edgeX, edgeY);
            if (Math.abs(denominator) < 0.000001) {
                continue;
            }

            double rayRadius = cross(current[0], current[1], edgeX, edgeY) / denominator;
            double edgeFraction = cross(current[0], current[1], directionX, directionY) / denominator;
            if (rayRadius >= 0.0 && edgeFraction >= -0.000001 && edgeFraction <= 1.000001) {
                bestRadius = Math.min(bestRadius, rayRadius);
            }
        }

        if (Double.isFinite(bestRadius)) {
            return bestRadius;
        }

        double fallbackRadius = 0.0;
        for (double[] point : points) {
            fallbackRadius = Math.max(fallbackRadius, point[0] * directionX + point[1] * directionY);
        }
        return Math.max(0.0, fallbackRadius);
    }

    /// Returns the two-dimensional cross product for two vectors.
    private static double cross(double firstX, double firstY, double secondX, double secondY) {
        return firstX * secondY - firstY * secondX;
    }

    /// Rotates a sampled shape in place.
    private static void rotateShape(double[][] shape, double rotationTurns) {
        double radians = rotationTurns * Math.PI * 2.0;
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            double x = shape[0][i];
            double y = shape[1][i];
            shape[0][i] = cosine * x - sine * y;
            shape[1][i] = sine * x + cosine * y;
        }
    }

    /// Normalizes a sampled shape so its visual bounds are centered and fit a unit radius box.
    private static void normalizeShape(double[][] shape) {
        double minX = shape[0][0];
        double maxX = shape[0][0];
        double minY = shape[1][0];
        double maxY = shape[1][0];
        for (int i = 1; i < SAMPLE_COUNT; i++) {
            minX = Math.min(minX, shape[0][i]);
            maxX = Math.max(maxX, shape[0][i]);
            minY = Math.min(minY, shape[1][i]);
            maxY = Math.max(maxY, shape[1][i]);
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double scale = 2.0 / Math.max(maxX - minX, maxY - minY);
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            shape[0][i] = (shape[0][i] - centerX) * scale;
            shape[1][i] = (shape[1][i] - centerY) * scale;
        }
    }
}
