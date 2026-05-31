// Copyright (c) 2026 Glavo
// Portions Copyright 2022-2023 The Android Open Source Project
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.shape;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

/// Matches and interpolates rounded polygon outlines.
///
/// The geometry and matching model is a Java port of the AndroidX `graphics-shapes` `RoundedPolygon`
/// and `Morph` algorithms used by Material 3 Expressive loading indicators. The class is internal
/// because it intentionally exposes only the subset required by M3FX controls.
@NotNullByDefault
public final class M3ShapeMorph {
    /// A small distance tolerance used when comparing points.
    private static final double DISTANCE_EPSILON = 1e-4;

    /// A small angular tolerance used when comparing outline progress.
    private static final double ANGLE_EPSILON = 1e-6;

    /// One full turn in radians.
    private static final double TWO_PI = Math.PI * 2.0;

    /// Material loading indicator indeterminate polygon sequence.
    private static final Sequence LOADING_INDICATOR_INDETERMINATE = sequence(
            true,
            softBurst(),
            cookie9(),
            pentagon(),
            pill(),
            sunny(),
            cookie4(),
            oval()
    );

    /// Material loading indicator determinate polygon sequence.
    private static final Sequence LOADING_INDICATOR_DETERMINATE = sequence(
            false,
            circle(10).transformed(M3ShapeMorph::rotate18).normalized(),
            softBurst()
    );

    /// Matched cubic curve pairs used by this morph.
    private final CubicPair @Unmodifiable [] matches;

    /// Creates a morph between two rounded polygons.
    ///
    /// @param start the starting polygon
    /// @param end the ending polygon
    private M3ShapeMorph(RoundedPolygon start, RoundedPolygon end) {
        this.matches = match(start, end).toArray(CubicPair[]::new);
    }

    /// Returns the Material loading indicator indeterminate morph sequence.
    ///
    /// @return the indeterminate loading indicator morph sequence
    public static Sequence loadingIndicatorIndeterminate() {
        return LOADING_INDICATOR_INDETERMINATE;
    }

    /// Returns the Material loading indicator determinate morph sequence.
    ///
    /// @return the determinate loading indicator morph sequence
    public static Sequence loadingIndicatorDeterminate() {
        return LOADING_INDICATOR_DETERMINATE;
    }

    /// Returns the number of cubic curves in this morph.
    ///
    /// @return the matched cubic curve count
    public int curveCount() {
        return matches.length;
    }

    /// Writes this morph at the requested progress to a JavaFX path.
    ///
    /// @param path the path to update
    /// @param progress the morph progress from `0.0` to `1.0`
    /// @param centerX the target center x-coordinate
    /// @param centerY the target center y-coordinate
    /// @param size the active indicator size in pixels
    /// @param sequenceScale the scale factor used to keep rotating shapes inside the active size
    /// @param extraScale the transient animation scale applied to the active shape
    /// @param rotationTurns the clockwise rotation in turns
    public void writeTo(
            Path path,
            double progress,
            double centerX,
            double centerY,
            double size,
            double sequenceScale,
            double extraScale,
            double rotationTurns
    ) {
        ensurePathElements(path, matches.length);
        double scale = size * sequenceScale * extraScale;
        double[] bounds = morphedBounds(progress, scale);
        double offsetX = centerX - (bounds[0] + bounds[2]) / 2.0;
        double offsetY = centerY - (bounds[1] + bounds[3]) / 2.0;
        double rotation = rotationTurns * TWO_PI;

        MoveTo moveTo = (MoveTo) path.getElements().get(0);
        Cubic first = interpolate(matches[0].start(), matches[0].end(), progress);
        Point firstPoint = transform(first.anchor0(), scale, offsetX, offsetY, centerX, centerY, rotation);
        moveTo.setX(firstPoint.x());
        moveTo.setY(firstPoint.y());

        for (int i = 0; i < matches.length; i++) {
            Cubic cubic = interpolate(matches[i].start(), matches[i].end(), progress);
            CubicCurveTo element = (CubicCurveTo) path.getElements().get(i + 1);
            Point control0 = transform(cubic.control0(), scale, offsetX, offsetY, centerX, centerY, rotation);
            Point control1 = transform(cubic.control1(), scale, offsetX, offsetY, centerX, centerY, rotation);
            Point anchor1 = transform(cubic.anchor1(), scale, offsetX, offsetY, centerX, centerY, rotation);
            element.setControlX1(control0.x());
            element.setControlY1(control0.y());
            element.setControlX2(control1.x());
            element.setControlY2(control1.y());
            element.setX(anchor1.x());
            element.setY(anchor1.y());
        }
    }

    /// Calculates the unrotated bounds of this morph after scaling.
    ///
    /// @param progress the morph progress
    /// @param scale the output scale
    /// @return the bounds as left, top, right, and bottom values
    private double @Unmodifiable [] morphedBounds(double progress, double scale) {
        double[] bounds = new double[4];
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (CubicPair match : matches) {
            Cubic cubic = interpolate(match.start(), match.end(), progress).scaled(scale);
            cubic.calculateBounds(bounds, false);
            minX = Math.min(minX, bounds[0]);
            minY = Math.min(minY, bounds[1]);
            maxX = Math.max(maxX, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
        }
        return new double[]{minX, minY, maxX, maxY};
    }

    /// Ensures that the path contains one move, the requested cubic count, and one close element.
    ///
    /// @param path the path to update
    /// @param curveCount the required cubic curve count
    private static void ensurePathElements(Path path, int curveCount) {
        int required = curveCount + 2;
        if (path.getElements().size() == required
                && path.getElements().get(0) instanceof MoveTo
                && path.getElements().get(required - 1) instanceof ClosePath) {
            return;
        }

        path.getElements().clear();
        path.getElements().add(new MoveTo());
        for (int i = 0; i < curveCount; i++) {
            path.getElements().add(new CubicCurveTo());
        }
        path.getElements().add(new ClosePath());
    }

    /// Transforms one normalized morph point into the JavaFX path coordinate space.
    ///
    /// @param point the normalized point
    /// @param scale the normalized-to-pixel scale
    /// @param offsetX the unrotated x-offset
    /// @param offsetY the unrotated y-offset
    /// @param centerX the rotation center x-coordinate
    /// @param centerY the rotation center y-coordinate
    /// @param rotation the rotation in radians
    /// @return the transformed point
    private static Point transform(
            Point point,
            double scale,
            double offsetX,
            double offsetY,
            double centerX,
            double centerY,
            double rotation
    ) {
        double x = point.x() * scale + offsetX;
        double y = point.y() * scale + offsetY;
        if (rotation == 0.0) {
            return new Point(x, y);
        }

        double dx = x - centerX;
        double dy = y - centerY;
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);
        return new Point(centerX + dx * cos - dy * sin, centerY + dx * sin + dy * cos);
    }

    /// Creates a morph sequence from rounded polygons.
    ///
    /// @param circular whether the final polygon should morph back to the first polygon
    /// @param polygons the source polygons
    /// @return the morph sequence
    private static Sequence sequence(boolean circular, RoundedPolygon... polygons) {
        if (polygons.length < 2) {
            throw new IllegalArgumentException("At least two polygons are required");
        }

        RoundedPolygon[] normalized = new RoundedPolygon[polygons.length];
        for (int i = 0; i < polygons.length; i++) {
            normalized[i] = polygons[i].normalized();
        }

        int morphCount = circular ? normalized.length : normalized.length - 1;
        M3ShapeMorph[] morphs = new M3ShapeMorph[morphCount];
        for (int i = 0; i < morphCount; i++) {
            morphs[i] = new M3ShapeMorph(normalized[i], normalized[(i + 1) % normalized.length]);
        }
        return new Sequence(morphs, calculateScaleFactor(normalized));
    }

    /// Calculates the sequence scale factor used by Compose Material loading indicators.
    ///
    /// @param polygons the normalized source polygons
    /// @return the scale factor that keeps every polygon inside the active size while rotating
    private static double calculateScaleFactor(RoundedPolygon @Unmodifiable [] polygons) {
        double scaleFactor = 1.0;
        double[] bounds = new double[4];
        double[] maxBounds = new double[4];
        for (RoundedPolygon polygon : polygons) {
            polygon.calculateBounds(bounds, true);
            polygon.calculateMaxBounds(maxBounds);
            double scaleX = (bounds[2] - bounds[0]) / (maxBounds[2] - maxBounds[0]);
            double scaleY = (bounds[3] - bounds[1]) / (maxBounds[3] - maxBounds[1]);
            scaleFactor = Math.min(scaleFactor, Math.max(scaleX, scaleY));
        }
        return scaleFactor;
    }

    /// Matches cubic curves from two rounded polygons using the AndroidX morph mapping algorithm.
    ///
    /// @param start the start polygon
    /// @param end the end polygon
    /// @return matched start and end cubic pairs
    private static List<CubicPair> match(RoundedPolygon start, RoundedPolygon end) {
        MeasuredPolygon startMeasured = MeasuredPolygon.measure(new AngleMeasurer(start.centerX, start.centerY), start);
        MeasuredPolygon endMeasured = MeasuredPolygon.measure(new AngleMeasurer(end.centerX, end.centerY), end);
        DoubleMapper mapper = featureMapper(startMeasured.features, endMeasured.features);
        double endCutPoint = mapper.map(0.0);
        MeasuredPolygon shiftedEnd = endMeasured.cutAndShift(endCutPoint);
        List<CubicPair> result = new ArrayList<>();

        int startIndex = 0;
        int endIndex = 0;
        @Nullable MeasuredCubic startCubic = startMeasured.getOrNull(startIndex++);
        @Nullable MeasuredCubic endCubic = shiftedEnd.getOrNull(endIndex++);
        while (startCubic != null && endCubic != null) {
            double startEnd = startIndex == startMeasured.size() ? 1.0 : startCubic.endProgress;
            double mappedEnd = endIndex == shiftedEnd.size()
                    ? 1.0
                    : mapper.mapBack(positiveModulo(endCubic.endProgress + endCutPoint, 1.0));
            double targetProgress = Math.min(startEnd, mappedEnd);

            CutStep startStep = startEnd > targetProgress + ANGLE_EPSILON
                    ? startCubic.cutAtProgress(targetProgress)
                    : new CutStep(startCubic, startMeasured.getOrNull(startIndex++));
            CutStep endStep = mappedEnd > targetProgress + ANGLE_EPSILON
                    ? endCubic.cutAtProgress(positiveModulo(mapper.map(targetProgress) - endCutPoint, 1.0))
                    : new CutStep(endCubic, shiftedEnd.getOrNull(endIndex++));

            result.add(new CubicPair(startStep.segment.cubic, endStep.segment.cubic));
            startCubic = startStep.next;
            endCubic = endStep.next;
        }

        if (startCubic != null || endCubic != null) {
            throw new IllegalStateException("Expected both polygons to be fully matched");
        }
        return result;
    }

    /// Creates a feature mapper between two measured feature lists.
    ///
    /// @param first the first measured feature list
    /// @param second the second measured feature list
    /// @return the bidirectional progress mapper
    private static DoubleMapper featureMapper(List<ProgressableFeature> first, List<ProgressableFeature> second) {
        List<ProgressableFeature> filteredFirst = filteredCorners(first);
        List<ProgressableFeature> filteredSecond = filteredCorners(second);
        List<ProgressableFeature> mapperFirst;
        List<ProgressableFeature> mapperSecond;
        if (filteredFirst.size() > filteredSecond.size()) {
            mapperFirst = doMapping(filteredSecond, filteredFirst);
            mapperSecond = filteredSecond;
        } else {
            mapperFirst = filteredFirst;
            mapperSecond = doMapping(filteredFirst, filteredSecond);
        }

        List<ProgressPair> pairs = new ArrayList<>(Math.min(mapperFirst.size(), mapperSecond.size()));
        for (int i = 0; i < mapperFirst.size() && i < mapperSecond.size(); i++) {
            pairs.add(new ProgressPair(mapperFirst.get(i).progress(), mapperSecond.get(i).progress()));
        }
        return new DoubleMapper(pairs);
    }

    /// Filters a measured feature list down to rounded polygon corners.
    ///
    /// @param features the source features
    /// @return the corner-only feature list
    private static List<ProgressableFeature> filteredCorners(List<ProgressableFeature> features) {
        List<ProgressableFeature> result = new ArrayList<>();
        for (ProgressableFeature feature : features) {
            if (feature.feature() instanceof Feature.Corner) {
                result.add(feature);
            }
        }
        return result;
    }

    /// Maps a smaller feature set to the best ordered subset of a larger feature set.
    ///
    /// @param smaller the smaller measured feature list
    /// @param larger the larger measured feature list
    /// @return selected features from the larger list
    private static List<ProgressableFeature> doMapping(List<ProgressableFeature> smaller, List<ProgressableFeature> larger) {
        int firstIndex = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < larger.size(); i++) {
            double distance = featureDistanceSquared(smaller.get(0).feature(), larger.get(i).feature());
            if (distance < bestDistance) {
                bestDistance = distance;
                firstIndex = i;
            }
        }

        int smallerSize = smaller.size();
        int largerSize = larger.size();
        List<ProgressableFeature> result = new ArrayList<>(smallerSize);
        result.add(larger.get(firstIndex));
        int lastPicked = firstIndex;
        for (int i = 1; i < smallerSize; i++) {
            int last = firstIndex - (smallerSize - i);
            if (last <= lastPicked) {
                last += largerSize;
            }

            int best = lastPicked + 1;
            bestDistance = Double.MAX_VALUE;
            for (int candidate = lastPicked + 1; candidate <= last; candidate++) {
                double distance = featureDistanceSquared(
                        smaller.get(i).feature(),
                        larger.get(candidate % largerSize).feature()
                );
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
            result.add(larger.get(best % largerSize));
            lastPicked = best;
        }
        return result;
    }

    /// Returns the squared distance between two features for morph matching.
    ///
    /// @param first the first feature
    /// @param second the second feature
    /// @return the squared feature distance
    private static double featureDistanceSquared(Feature first, Feature second) {
        if (first instanceof Feature.Corner firstCorner
                && second instanceof Feature.Corner secondCorner
                && firstCorner.convex != secondCorner.convex) {
            return Double.MAX_VALUE;
        }

        Cubic firstStart = first.cubics.get(0);
        Cubic firstEnd = first.cubics.get(first.cubics.size() - 1);
        Cubic secondStart = second.cubics.get(0);
        Cubic secondEnd = second.cubics.get(second.cubics.size() - 1);
        double firstX = (firstStart.anchor0X() + firstEnd.anchor1X()) / 2.0;
        double firstY = (firstStart.anchor0Y() + firstEnd.anchor1Y()) / 2.0;
        double secondX = (secondStart.anchor0X() + secondEnd.anchor1X()) / 2.0;
        double secondY = (secondStart.anchor0Y() + secondEnd.anchor1Y()) / 2.0;
        return distanceSquared(firstX - secondX, firstY - secondY);
    }

    /// Creates the Material `circle` shape.
    ///
    /// @param vertices the underlying vertex count
    /// @return the rounded polygon
    private static RoundedPolygon circle(int vertices) {
        double theta = Math.PI / vertices;
        double polygonRadius = 1.0 / Math.cos(theta);
        return RoundedPolygon.regular(vertices, polygonRadius, 0.0, 0.0, new CornerRounding(1.0, 0.0), null);
    }

    /// Creates the Material `oval` shape used by loading indicators.
    ///
    /// @return the rounded polygon
    private static RoundedPolygon oval() {
        return circle(8).transformed((x, y) -> new Point(x, y * 0.7)).transformed(M3ShapeMorph::rotateNegative45);
    }

    /// Creates the Material `pill` shape used by loading indicators.
    ///
    /// @return the rounded polygon
    private static RoundedPolygon pill() {
        return RoundedPolygon.pill(1.25, 1.0).transformed(M3ShapeMorph::rotateNegative45);
    }

    /// Creates the Material `pentagon` shape used by loading indicators.
    ///
    /// @return the rounded polygon
    private static RoundedPolygon pentagon() {
        return RoundedPolygon.regular(5, 1.0, 0.0, 0.0, new CornerRounding(0.3, 0.0), null)
                .transformed(M3ShapeMorph::rotateNegative18);
    }

    /// Creates the Material `sunny` shape used by loading indicators.
    ///
    /// @return the rounded polygon
    private static RoundedPolygon sunny() {
        return RoundedPolygon.star(8, 1.0, 0.8, new CornerRounding(0.15, 0.0), null);
    }

    /// Creates the Material `cookie 4-sided` shape used by loading indicators.
    ///
    /// @return the rounded polygon
    private static RoundedPolygon cookie4() {
        return RoundedPolygon.star(4, 1.0, 0.5, new CornerRounding(0.3, 0.0), null)
                .transformed(M3ShapeMorph::rotateNegative45);
    }

    /// Creates the Material `cookie 9-sided` shape used by loading indicators.
    ///
    /// @return the rounded polygon
    private static RoundedPolygon cookie9() {
        return RoundedPolygon.star(9, 1.0, 0.8, new CornerRounding(0.5, 0.0), null)
                .transformed(M3ShapeMorph::rotateNegative90);
    }

    /// Creates the Material `soft burst` shape used by loading indicators.
    ///
    /// @return the rounded polygon
    private static RoundedPolygon softBurst() {
        CornerRounding rounding = new CornerRounding(0.1, 0.0);
        return RoundedPolygon.star(10, 1.0, 0.65, rounding, rounding)
                .transformed(M3ShapeMorph::rotate18);
    }

    /// Rotates a point by 18 degrees.
    ///
    /// @param x the source x-coordinate
    /// @param y the source y-coordinate
    /// @return the rotated point
    private static Point rotate18(double x, double y) {
        return rotate(x, y, Math.toRadians(18.0));
    }

    /// Rotates a point by -18 degrees.
    ///
    /// @param x the source x-coordinate
    /// @param y the source y-coordinate
    /// @return the rotated point
    private static Point rotateNegative18(double x, double y) {
        return rotate(x, y, Math.toRadians(-18.0));
    }

    /// Rotates a point by -45 degrees.
    ///
    /// @param x the source x-coordinate
    /// @param y the source y-coordinate
    /// @return the rotated point
    private static Point rotateNegative45(double x, double y) {
        return rotate(x, y, Math.toRadians(-45.0));
    }

    /// Rotates a point by -90 degrees.
    ///
    /// @param x the source x-coordinate
    /// @param y the source y-coordinate
    /// @return the rotated point
    private static Point rotateNegative90(double x, double y) {
        return rotate(x, y, Math.toRadians(-90.0));
    }

    /// Rotates a point around the origin.
    ///
    /// @param x the source x-coordinate
    /// @param y the source y-coordinate
    /// @param angle the rotation angle in radians
    /// @return the rotated point
    private static Point rotate(double x, double y, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Point(x * cos - y * sin, x * sin + y * cos);
    }

    /// Returns a linearly interpolated cubic.
    ///
    /// @param start the start cubic
    /// @param end the end cubic
    /// @param fraction the interpolation fraction
    /// @return the interpolated cubic
    private static Cubic interpolate(Cubic start, Cubic end, double fraction) {
        return new Cubic(
                interpolate(start.anchor0X(), end.anchor0X(), fraction),
                interpolate(start.anchor0Y(), end.anchor0Y(), fraction),
                interpolate(start.control0X(), end.control0X(), fraction),
                interpolate(start.control0Y(), end.control0Y(), fraction),
                interpolate(start.control1X(), end.control1X(), fraction),
                interpolate(start.control1Y(), end.control1Y(), fraction),
                interpolate(start.anchor1X(), end.anchor1X(), fraction),
                interpolate(start.anchor1Y(), end.anchor1Y(), fraction)
        );
    }

    /// Returns a linearly interpolated point.
    ///
    /// @param start the start point
    /// @param end the end point
    /// @param fraction the interpolation fraction
    /// @return the interpolated point
    private static Point interpolate(Point start, Point end, double fraction) {
        return new Point(interpolate(start.x(), end.x(), fraction), interpolate(start.y(), end.y(), fraction));
    }

    /// Returns a linearly interpolated scalar.
    ///
    /// @param start the start value
    /// @param end the end value
    /// @param fraction the interpolation fraction
    /// @return the interpolated value
    private static double interpolate(double start, double end, double fraction) {
        return (1.0 - fraction) * start + fraction * end;
    }

    /// Returns a positive modulo result.
    ///
    /// @param value the input value
    /// @param modulus the modulus
    /// @return the positive modulo result
    private static double positiveModulo(double value, double modulus) {
        return (value % modulus + modulus) % modulus;
    }

    /// Returns the Euclidean distance.
    ///
    /// @param x the x distance
    /// @param y the y distance
    /// @return the distance
    private static double distance(double x, double y) {
        return Math.sqrt(distanceSquared(x, y));
    }

    /// Returns the squared Euclidean distance.
    ///
    /// @param x the x distance
    /// @param y the y distance
    /// @return the squared distance
    private static double distanceSquared(double x, double y) {
        return x * x + y * y;
    }

    /// Returns an angle in the range `[0, 2pi)`.
    ///
    /// @param x the x coordinate
    /// @param y the y coordinate
    /// @return the positive angle
    private static double angle(double x, double y) {
        return positiveModulo(Math.atan2(y, x), TWO_PI);
    }

    /// Returns a unit vector for the requested direction.
    ///
    /// @param x the source x-coordinate
    /// @param y the source y-coordinate
    /// @return the direction vector
    private static Point directionVector(double x, double y) {
        double distance = distance(x, y);
        if (distance <= 0.0) {
            throw new IllegalArgumentException("Required distance greater than zero");
        }
        return new Point(x / distance, y / distance);
    }

    /// Returns a cartesian point for a radial coordinate.
    ///
    /// @param radius the radius
    /// @param angle the angle in radians
    /// @param centerX the center x-coordinate
    /// @param centerY the center y-coordinate
    /// @return the cartesian point
    private static Point radialToCartesian(double radius, double angle, double centerX, double centerY) {
        return new Point(Math.cos(angle) * radius + centerX, Math.sin(angle) * radius + centerY);
    }

    /// Finds the input that approximately minimizes a scalar function.
    ///
    /// @param from the lower bound
    /// @param to the upper bound
    /// @param tolerance the stop tolerance
    /// @param function the function to minimize
    /// @return the minimizing input
    private static double findMinimum(double from, double to, double tolerance, FindMinimumFunction function) {
        double a = from;
        double b = to;
        while (b - a > tolerance) {
            double first = (2.0 * a + b) / 3.0;
            double second = (2.0 * b + a) / 3.0;
            if (function.apply(first) < function.apply(second)) {
                b = second;
            } else {
                a = first;
            }
        }
        return (a + b) / 2.0;
    }

    /// A single-input scalar function used by ternary search.
    @FunctionalInterface
    @NotNullByDefault
    private interface FindMinimumFunction {
        /// Applies the function to a value.
        ///
        /// @param value the input value
        /// @return the output value
        double apply(double value);
    }

    /// A point transformation function.
    @FunctionalInterface
    @NotNullByDefault
    private interface PointTransformer {
        /// Transforms the requested point.
        ///
        /// @param x the source x-coordinate
        /// @param y the source y-coordinate
        /// @return the transformed point
        Point transform(double x, double y);
    }

    /// A sequence of matched polygon morphs.
    @NotNullByDefault
    public static final class Sequence {
        /// The matched morphs in this sequence.
        private final M3ShapeMorph @Unmodifiable [] morphs;

        /// The sequence-wide scale factor.
        private final double scaleFactor;

        /// Creates a morph sequence.
        ///
        /// @param morphs the matched morphs
        /// @param scaleFactor the sequence-wide scale factor
        private Sequence(M3ShapeMorph @Unmodifiable [] morphs, double scaleFactor) {
            this.morphs = morphs;
            this.scaleFactor = scaleFactor;
        }

        /// Returns the number of morph segments in this sequence.
        ///
        /// @return the morph segment count
        public int size() {
            return morphs.length;
        }

        /// Returns the sequence-wide scale factor.
        ///
        /// @return the scale factor
        public double scaleFactor() {
            return scaleFactor;
        }

        /// Returns a morph by index.
        ///
        /// @param index the morph index
        /// @return the morph at the requested index
        public M3ShapeMorph morphAt(int index) {
            return morphs[Math.floorMod(index, morphs.length)];
        }
    }

    /// A matched cubic pair.
    ///
    /// @param start the start cubic
    /// @param end the end cubic
    @NotNullByDefault
    private record CubicPair(Cubic start, Cubic end) {
    }

    /// A pair of progress values used by progress mappers.
    ///
    /// @param source the source progress
    /// @param target the target progress
    @NotNullByDefault
    private record ProgressPair(double source, double target) {
    }

    /// A measured feature and its outline progress.
    ///
    /// @param progress the feature progress
    /// @param feature the measured feature
    @NotNullByDefault
    private record ProgressableFeature(double progress, Feature feature) {
    }

    /// A two-dimensional point.
    ///
    /// @param x the x coordinate
    /// @param y the y coordinate
    @NotNullByDefault
    private record Point(double x, double y) {
        /// Returns this point added to another point.
        ///
        /// @param other the other point
        /// @return the sum point
        private Point add(Point other) {
            return new Point(x + other.x, y + other.y);
        }

        /// Returns this point subtracted by another point.
        ///
        /// @param other the other point
        /// @return the difference point
        private Point subtract(Point other) {
            return new Point(x - other.x, y - other.y);
        }

        /// Returns this point multiplied by a scalar.
        ///
        /// @param scalar the scalar
        /// @return the scaled point
        private Point multiply(double scalar) {
            return new Point(x * scalar, y * scalar);
        }

        /// Returns this point divided by a scalar.
        ///
        /// @param scalar the scalar
        /// @return the scaled point
        private Point divide(double scalar) {
            return new Point(x / scalar, y / scalar);
        }

        /// Returns this point rotated by 90 degrees.
        ///
        /// @return the rotated point
        private Point rotate90() {
            return new Point(-y, x);
        }

        /// Returns this point's dot product with another point.
        ///
        /// @param other the other point
        /// @return the dot product
        private double dot(Point other) {
            return x * other.x + y * other.y;
        }

        /// Returns this point's dot product with raw coordinates.
        ///
        /// @param otherX the other x-coordinate
        /// @param otherY the other y-coordinate
        /// @return the dot product
        private double dot(double otherX, double otherY) {
            return x * otherX + y * otherY;
        }

        /// Returns whether another vector is clockwise from this vector.
        ///
        /// @param other the other vector
        /// @return whether the other vector is clockwise
        private boolean clockwise(Point other) {
            return x * other.y - y * other.x > 0.0;
        }

        /// Returns this point's direction vector.
        ///
        /// @return the unit direction vector
        private Point direction() {
            return directionVector(x, y);
        }

        /// Returns this point transformed by a transformer.
        ///
        /// @param transformer the transformer
        /// @return the transformed point
        private Point transformed(PointTransformer transformer) {
            return transformer.transform(x, y);
        }
    }

    /// Corner rounding parameters.
    ///
    /// @param radius the corner radius
    /// @param smoothing the smoothing amount
    @NotNullByDefault
    private record CornerRounding(double radius, double smoothing) {
    }

    /// A cubic Bezier segment.
    @NotNullByDefault
    private static final class Cubic {
        /// The start anchor x-coordinate.
        private final double anchor0X;

        /// The start anchor y-coordinate.
        private final double anchor0Y;

        /// The first control point x-coordinate.
        private final double control0X;

        /// The first control point y-coordinate.
        private final double control0Y;

        /// The second control point x-coordinate.
        private final double control1X;

        /// The second control point y-coordinate.
        private final double control1Y;

        /// The end anchor x-coordinate.
        private final double anchor1X;

        /// The end anchor y-coordinate.
        private final double anchor1Y;

        /// Creates a cubic Bezier segment.
        private Cubic(
                double anchor0X,
                double anchor0Y,
                double control0X,
                double control0Y,
                double control1X,
                double control1Y,
                double anchor1X,
                double anchor1Y
        ) {
            this.anchor0X = anchor0X;
            this.anchor0Y = anchor0Y;
            this.control0X = control0X;
            this.control0Y = control0Y;
            this.control1X = control1X;
            this.control1Y = control1Y;
            this.anchor1X = anchor1X;
            this.anchor1Y = anchor1Y;
        }

        /// Creates a cubic Bezier segment from point values.
        ///
        /// @param anchor0 the start anchor
        /// @param control0 the first control point
        /// @param control1 the second control point
        /// @param anchor1 the end anchor
        private Cubic(Point anchor0, Point control0, Point control1, Point anchor1) {
            this(anchor0.x(), anchor0.y(), control0.x(), control0.y(), control1.x(), control1.y(), anchor1.x(), anchor1.y());
        }

        /// Returns the start anchor x-coordinate.
        private double anchor0X() {
            return anchor0X;
        }

        /// Returns the start anchor y-coordinate.
        private double anchor0Y() {
            return anchor0Y;
        }

        /// Returns the first control point x-coordinate.
        private double control0X() {
            return control0X;
        }

        /// Returns the first control point y-coordinate.
        private double control0Y() {
            return control0Y;
        }

        /// Returns the second control point x-coordinate.
        private double control1X() {
            return control1X;
        }

        /// Returns the second control point y-coordinate.
        private double control1Y() {
            return control1Y;
        }

        /// Returns the end anchor x-coordinate.
        private double anchor1X() {
            return anchor1X;
        }

        /// Returns the end anchor y-coordinate.
        private double anchor1Y() {
            return anchor1Y;
        }

        /// Returns the start anchor point.
        ///
        /// @return the start anchor
        private Point anchor0() {
            return new Point(anchor0X, anchor0Y);
        }

        /// Returns the first control point.
        ///
        /// @return the first control point
        private Point control0() {
            return new Point(control0X, control0Y);
        }

        /// Returns the second control point.
        ///
        /// @return the second control point
        private Point control1() {
            return new Point(control1X, control1Y);
        }

        /// Returns the end anchor point.
        ///
        /// @return the end anchor
        private Point anchor1() {
            return new Point(anchor1X, anchor1Y);
        }

        /// Returns whether this segment has zero visible length.
        ///
        /// @return whether the anchors are effectively equal
        private boolean zeroLength() {
            return Math.abs(anchor0X - anchor1X) < DISTANCE_EPSILON
                    && Math.abs(anchor0Y - anchor1Y) < DISTANCE_EPSILON;
        }

        /// Returns a point on this curve.
        ///
        /// @param t the curve parameter
        /// @return the curve point
        private Point pointOnCurve(double t) {
            double u = 1.0 - t;
            return new Point(
                    anchor0X * (u * u * u)
                            + control0X * (3.0 * t * u * u)
                            + control1X * (3.0 * t * t * u)
                            + anchor1X * (t * t * t),
                    anchor0Y * (u * u * u)
                            + control0Y * (3.0 * t * u * u)
                            + control1Y * (3.0 * t * t * u)
                            + anchor1Y * (t * t * t)
            );
        }

        /// Calculates the bounds of this curve.
        ///
        /// @param bounds the destination bounds
        /// @param approximate whether to use control-point bounds instead of exact curve extrema
        private void calculateBounds(double[] bounds, boolean approximate) {
            if (zeroLength()) {
                bounds[0] = anchor0X;
                bounds[1] = anchor0Y;
                bounds[2] = anchor0X;
                bounds[3] = anchor0Y;
                return;
            }

            double minX = Math.min(anchor0X, anchor1X);
            double minY = Math.min(anchor0Y, anchor1Y);
            double maxX = Math.max(anchor0X, anchor1X);
            double maxY = Math.max(anchor0Y, anchor1Y);

            if (approximate) {
                bounds[0] = Math.min(minX, Math.min(control0X, control1X));
                bounds[1] = Math.min(minY, Math.min(control0Y, control1Y));
                bounds[2] = Math.max(maxX, Math.max(control0X, control1X));
                bounds[3] = Math.max(maxY, Math.max(control0Y, control1Y));
                return;
            }

            double xa = -anchor0X + 3.0 * control0X - 3.0 * control1X + anchor1X;
            double xb = 2.0 * anchor0X - 4.0 * control0X + 2.0 * control1X;
            double xc = -anchor0X + control0X;
            double[] xRange = addExtrema(xa, xb, xc, minX, maxX, true);
            minX = xRange[0];
            maxX = xRange[1];

            double ya = -anchor0Y + 3.0 * control0Y - 3.0 * control1Y + anchor1Y;
            double yb = 2.0 * anchor0Y - 4.0 * control0Y + 2.0 * control1Y;
            double yc = -anchor0Y + control0Y;
            double[] yRange = addExtrema(ya, yb, yc, minY, maxY, false);
            minY = yRange[0];
            maxY = yRange[1];

            bounds[0] = minX;
            bounds[1] = minY;
            bounds[2] = maxX;
            bounds[3] = maxY;
        }

        /// Adds cubic extrema to an axis range.
        ///
        /// @param a the quadratic coefficient
        /// @param b the linear coefficient
        /// @param c the constant coefficient
        /// @param min the current minimum
        /// @param max the current maximum
        /// @param xAxis whether to sample x-values
        /// @return the updated range as minimum and maximum
        private double @Unmodifiable [] addExtrema(double a, double b, double c, double min, double max, boolean xAxis) {
            if (Math.abs(a) < DISTANCE_EPSILON) {
                if (b != 0.0) {
                    double t = c / -b;
                    if (t >= 0.0 && t <= 1.0) {
                        double value = xAxis ? pointOnCurve(t).x() : pointOnCurve(t).y();
                        min = Math.min(min, value);
                        max = Math.max(max, value);
                    }
                }
            } else {
                double discriminant = b * b - 4.0 * a * c;
                if (discriminant >= 0.0) {
                    double sqrt = Math.sqrt(discriminant);
                    double t1 = (-b + sqrt) / (2.0 * a);
                    double t2 = (-b - sqrt) / (2.0 * a);
                    if (t1 >= 0.0 && t1 <= 1.0) {
                        double value = xAxis ? pointOnCurve(t1).x() : pointOnCurve(t1).y();
                        min = Math.min(min, value);
                        max = Math.max(max, value);
                    }
                    if (t2 >= 0.0 && t2 <= 1.0) {
                        double value = xAxis ? pointOnCurve(t2).x() : pointOnCurve(t2).y();
                        min = Math.min(min, value);
                        max = Math.max(max, value);
                    }
                }
            }
            return new double[]{min, max};
        }

        /// Splits this curve at the requested parameter.
        ///
        /// @param t the split parameter
        /// @return the two split curves
        private CubicSplit split(double t) {
            double u = 1.0 - t;
            Point point = pointOnCurve(t);
            return new CubicSplit(
                    new Cubic(
                            anchor0X, anchor0Y,
                            anchor0X * u + control0X * t, anchor0Y * u + control0Y * t,
                            anchor0X * (u * u) + control0X * (2.0 * u * t) + control1X * (t * t),
                            anchor0Y * (u * u) + control0Y * (2.0 * u * t) + control1Y * (t * t),
                            point.x(), point.y()
                    ),
                    new Cubic(
                            point.x(), point.y(),
                            control0X * (u * u) + control1X * (2.0 * u * t) + anchor1X * (t * t),
                            control0Y * (u * u) + control1Y * (2.0 * u * t) + anchor1Y * (t * t),
                            control1X * u + anchor1X * t, control1Y * u + anchor1Y * t,
                            anchor1X, anchor1Y
                    )
            );
        }

        /// Returns this curve with reversed anchors and controls.
        ///
        /// @return the reversed cubic
        private Cubic reverse() {
            return new Cubic(anchor1X, anchor1Y, control1X, control1Y, control0X, control0Y, anchor0X, anchor0Y);
        }

        /// Returns this curve scaled by a scalar.
        ///
        /// @param scale the scale
        /// @return the scaled cubic
        private Cubic scaled(double scale) {
            return new Cubic(
                    anchor0X * scale,
                    anchor0Y * scale,
                    control0X * scale,
                    control0Y * scale,
                    control1X * scale,
                    control1Y * scale,
                    anchor1X * scale,
                    anchor1Y * scale
            );
        }

        /// Returns this curve transformed by a transformer.
        ///
        /// @param transformer the transformer
        /// @return the transformed cubic
        private Cubic transformed(PointTransformer transformer) {
            return new Cubic(
                    anchor0().transformed(transformer),
                    control0().transformed(transformer),
                    control1().transformed(transformer),
                    anchor1().transformed(transformer)
            );
        }

        /// Creates a straight cubic line.
        ///
        /// @param x0 the start x-coordinate
        /// @param y0 the start y-coordinate
        /// @param x1 the end x-coordinate
        /// @param y1 the end y-coordinate
        /// @return the cubic line
        private static Cubic straightLine(double x0, double y0, double x1, double y1) {
            return new Cubic(
                    x0, y0,
                    interpolate(x0, x1, 1.0 / 3.0),
                    interpolate(y0, y1, 1.0 / 3.0),
                    interpolate(x0, x1, 2.0 / 3.0),
                    interpolate(y0, y1, 2.0 / 3.0),
                    x1, y1
            );
        }

        /// Creates a cubic approximation of a circular arc.
        ///
        /// @param centerX the circle center x-coordinate
        /// @param centerY the circle center y-coordinate
        /// @param x0 the start x-coordinate
        /// @param y0 the start y-coordinate
        /// @param x1 the end x-coordinate
        /// @param y1 the end y-coordinate
        /// @return the cubic arc
        private static Cubic circularArc(double centerX, double centerY, double x0, double y0, double x1, double y1) {
            Point p0d = directionVector(x0 - centerX, y0 - centerY);
            Point p1d = directionVector(x1 - centerX, y1 - centerY);
            Point rotatedP0 = p0d.rotate90();
            Point rotatedP1 = p1d.rotate90();
            boolean clockwise = rotatedP0.dot(x1 - centerX, y1 - centerY) >= 0.0;
            double cosAngle = p0d.dot(p1d);
            if (cosAngle > 0.999) {
                return straightLine(x0, y0, x1, y1);
            }

            double k = distance(x0 - centerX, y0 - centerY) * 4.0 / 3.0
                    * (Math.sqrt(2.0 * (1.0 - cosAngle)) - Math.sqrt(1.0 - cosAngle * cosAngle))
                    / (1.0 - cosAngle)
                    * (clockwise ? 1.0 : -1.0);
            return new Cubic(
                    x0, y0,
                    x0 + rotatedP0.x() * k, y0 + rotatedP0.y() * k,
                    x1 - rotatedP1.x() * k, y1 - rotatedP1.y() * k,
                    x1, y1
            );
        }
    }

    /// A pair of cubics from a split operation.
    ///
    /// @param first the first cubic
    /// @param second the second cubic
    @NotNullByDefault
    private record CubicSplit(Cubic first, Cubic second) {
    }

    /// A rounded polygon feature.
    @NotNullByDefault
    private abstract static class Feature {
        /// The cubic curves that make up this feature.
        protected final List<Cubic> cubics;

        /// Creates a feature.
        ///
        /// @param cubics the feature cubics
        private Feature(List<Cubic> cubics) {
            this.cubics = List.copyOf(cubics);
        }

        /// Returns this feature transformed by a point transformer.
        ///
        /// @param transformer the point transformer
        /// @return the transformed feature
        abstract Feature transformed(PointTransformer transformer);

        /// A polygon edge feature.
        @NotNullByDefault
        private static final class Edge extends Feature {
            /// Creates an edge.
            ///
            /// @param cubics the edge cubics
            private Edge(List<Cubic> cubics) {
                super(cubics);
            }

            /// Returns this edge transformed by a point transformer.
            ///
            /// @param transformer the point transformer
            /// @return the transformed edge
            @Override
            Feature transformed(PointTransformer transformer) {
                List<Cubic> transformed = new ArrayList<>(cubics.size());
                for (Cubic cubic : cubics) {
                    transformed.add(cubic.transformed(transformer));
                }
                return new Edge(transformed);
            }
        }

        /// A polygon corner feature.
        @NotNullByDefault
        private static final class Corner extends Feature {
            /// The source vertex for this corner.
            private final Point vertex;

            /// The center used by this corner rounding.
            private final Point roundedCenter;

            /// Whether this corner is convex.
            private final boolean convex;

            /// Creates a corner.
            ///
            /// @param cubics the corner cubics
            /// @param vertex the source vertex
            /// @param roundedCenter the rounded center
            /// @param convex whether the corner is convex
            private Corner(List<Cubic> cubics, Point vertex, Point roundedCenter, boolean convex) {
                super(cubics);
                this.vertex = vertex;
                this.roundedCenter = roundedCenter;
                this.convex = convex;
            }

            /// Returns this corner transformed by a point transformer.
            ///
            /// @param transformer the point transformer
            /// @return the transformed corner
            @Override
            Feature transformed(PointTransformer transformer) {
                List<Cubic> transformed = new ArrayList<>(cubics.size());
                for (Cubic cubic : cubics) {
                    transformed.add(cubic.transformed(transformer));
                }
                return new Corner(
                        transformed,
                        vertex.transformed(transformer),
                        roundedCenter.transformed(transformer),
                        convex
                );
            }
        }
    }

    /// A rounded polygon.
    @NotNullByDefault
    private static final class RoundedPolygon {
        /// The semantic features that make up this polygon.
        private final List<Feature> features;

        /// The flattened cubic outline.
        private final List<Cubic> cubics;

        /// The polygon center x-coordinate.
        private final double centerX;

        /// The polygon center y-coordinate.
        private final double centerY;

        /// Creates a rounded polygon from features.
        ///
        /// @param features the polygon features
        /// @param centerX the polygon center x-coordinate
        /// @param centerY the polygon center y-coordinate
        private RoundedPolygon(List<Feature> features, double centerX, double centerY) {
            this.features = List.copyOf(features);
            this.centerX = centerX;
            this.centerY = centerY;
            this.cubics = flatten(features);
            validate();
        }

        /// Creates a regular rounded polygon.
        ///
        /// @param vertices the vertex count
        /// @param radius the vertex radius
        /// @param centerX the center x-coordinate
        /// @param centerY the center y-coordinate
        /// @param rounding the default corner rounding
        /// @param perVertexRounding the optional per-vertex rounding list
        /// @return the rounded polygon
        private static RoundedPolygon regular(
                int vertices,
                double radius,
                double centerX,
                double centerY,
                CornerRounding rounding,
                @Nullable List<CornerRounding> perVertexRounding
        ) {
            if (vertices < 3) {
                throw new IllegalArgumentException("Polygons must have at least three vertices");
            }

            double[] points = new double[vertices * 2];
            int index = 0;
            for (int i = 0; i < vertices; i++) {
                Point vertex = radialToCartesian(radius, Math.PI / vertices * 2.0 * i, centerX, centerY);
                points[index++] = vertex.x();
                points[index++] = vertex.y();
            }
            return fromVertices(points, rounding, perVertexRounding, centerX, centerY);
        }

        /// Creates a rounded star polygon.
        ///
        /// @param verticesPerRadius the number of outer and inner vertices
        /// @param radius the outer radius
        /// @param innerRadius the inner radius
        /// @param rounding the outer rounding
        /// @param innerRounding the optional inner rounding
        /// @return the rounded polygon
        private static RoundedPolygon star(
                int verticesPerRadius,
                double radius,
                double innerRadius,
                CornerRounding rounding,
                @Nullable CornerRounding innerRounding
        ) {
            if (radius <= 0.0 || innerRadius <= 0.0 || innerRadius >= radius) {
                throw new IllegalArgumentException("Star radii must be positive and ordered");
            }

            @Nullable List<CornerRounding> perVertexRounding = null;
            if (innerRounding != null) {
                perVertexRounding = new ArrayList<>(verticesPerRadius * 2);
                for (int i = 0; i < verticesPerRadius; i++) {
                    perVertexRounding.add(rounding);
                    perVertexRounding.add(innerRounding);
                }
            }

            double[] points = new double[verticesPerRadius * 4];
            int index = 0;
            for (int i = 0; i < verticesPerRadius * 2; i++) {
                double vertexRadius = i % 2 == 0 ? radius : innerRadius;
                Point vertex = radialToCartesian(vertexRadius, Math.PI / verticesPerRadius * i, 0.0, 0.0);
                points[index++] = vertex.x();
                points[index++] = vertex.y();
            }
            return fromVertices(points, rounding, perVertexRounding, 0.0, 0.0);
        }

        /// Creates a pill polygon.
        ///
        /// @param width the pill width
        /// @param height the pill height
        /// @return the rounded polygon
        private static RoundedPolygon pill(double width, double height) {
            if (width <= 0.0 || height <= 0.0) {
                throw new IllegalArgumentException("Pill dimensions must be positive");
            }

            double halfWidth = width / 2.0;
            double halfHeight = height / 2.0;
            return fromVertices(
                    new double[]{
                            halfWidth, halfHeight,
                            -halfWidth, halfHeight,
                            -halfWidth, -halfHeight,
                            halfWidth, -halfHeight
                    },
                    new CornerRounding(Math.min(halfWidth, halfHeight), 0.0),
                    null,
                    0.0,
                    0.0
            );
        }

        /// Creates a rounded polygon from raw vertices.
        ///
        /// @param vertices the raw xy vertex array
        /// @param rounding the default corner rounding
        /// @param perVertexRounding the optional per-vertex rounding list
        /// @param centerX the center x-coordinate
        /// @param centerY the center y-coordinate
        /// @return the rounded polygon
        private static RoundedPolygon fromVertices(
                double[] vertices,
                CornerRounding rounding,
                @Nullable List<CornerRounding> perVertexRounding,
                double centerX,
                double centerY
        ) {
            if (vertices.length < 6 || vertices.length % 2 != 0) {
                throw new IllegalArgumentException("The vertices array must contain at least three points");
            }
            if (perVertexRounding != null && perVertexRounding.size() * 2 != vertices.length) {
                throw new IllegalArgumentException("Per-vertex rounding size must match the vertex count");
            }

            int count = vertices.length / 2;
            List<RoundedCorner> roundedCorners = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                CornerRounding vertexRounding = perVertexRounding == null ? rounding : perVertexRounding.get(i);
                int previous = ((i + count - 1) % count) * 2;
                int next = ((i + 1) % count) * 2;
                roundedCorners.add(new RoundedCorner(
                        new Point(vertices[previous], vertices[previous + 1]),
                        new Point(vertices[i * 2], vertices[i * 2 + 1]),
                        new Point(vertices[next], vertices[next + 1]),
                        vertexRounding
                ));
            }

            List<CutAdjust> cutAdjusts = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                double expectedRoundCut = roundedCorners.get(i).expectedRoundCut
                        + roundedCorners.get((i + 1) % count).expectedRoundCut;
                double expectedCut = roundedCorners.get(i).expectedCut()
                        + roundedCorners.get((i + 1) % count).expectedCut();
                double x = vertices[i * 2];
                double y = vertices[i * 2 + 1];
                double nextX = vertices[((i + 1) % count) * 2];
                double nextY = vertices[((i + 1) % count) * 2 + 1];
                double sideSize = distance(x - nextX, y - nextY);
                if (expectedRoundCut > sideSize) {
                    cutAdjusts.add(new CutAdjust(sideSize / expectedRoundCut, 0.0));
                } else if (expectedCut > sideSize) {
                    cutAdjusts.add(new CutAdjust(1.0, (sideSize - expectedRoundCut) / (expectedCut - expectedRoundCut)));
                } else {
                    cutAdjusts.add(new CutAdjust(1.0, 1.0));
                }
            }

            List<List<Cubic>> corners = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                CutAdjust previous = cutAdjusts.get((i + count - 1) % count);
                CutAdjust next = cutAdjusts.get(i);
                RoundedCorner corner = roundedCorners.get(i);
                double allowedCut0 = corner.expectedRoundCut * previous.roundCutRatio
                        + (corner.expectedCut() - corner.expectedRoundCut) * previous.cutRatio;
                double allowedCut1 = corner.expectedRoundCut * next.roundCutRatio
                        + (corner.expectedCut() - corner.expectedRoundCut) * next.cutRatio;
                corners.add(corner.getCubics(allowedCut0, allowedCut1));
            }

            List<Feature> features = new ArrayList<>(count * 2);
            for (int i = 0; i < count; i++) {
                int previous = (i + count - 1) % count;
                int next = (i + 1) % count;
                Point currentVertex = new Point(vertices[i * 2], vertices[i * 2 + 1]);
                Point previousVertex = new Point(vertices[previous * 2], vertices[previous * 2 + 1]);
                Point nextVertex = new Point(vertices[next * 2], vertices[next * 2 + 1]);
                boolean convex = currentVertex.subtract(previousVertex).clockwise(nextVertex.subtract(currentVertex));
                List<Cubic> corner = corners.get(i);
                features.add(new Feature.Corner(corner, currentVertex, roundedCorners.get(i).center, convex));
                features.add(new Feature.Edge(List.of(Cubic.straightLine(
                        corner.get(corner.size() - 1).anchor1X(),
                        corner.get(corner.size() - 1).anchor1Y(),
                        corners.get((i + 1) % count).get(0).anchor0X(),
                        corners.get((i + 1) % count).get(0).anchor0Y()
                ))));
            }

            return new RoundedPolygon(features, centerX, centerY);
        }

        /// Returns a flattened cubic outline for features.
        ///
        /// @param features the source features
        /// @return the flattened outline
        private static List<Cubic> flatten(List<Feature> features) {
            List<Cubic> result = new ArrayList<>();
            @Nullable Cubic firstCubic = null;
            @Nullable Cubic lastCubic = null;
            @Nullable List<Cubic> firstFeatureSplitStart = null;
            @Nullable List<Cubic> firstFeatureSplitEnd = null;

            if (!features.isEmpty() && features.get(0).cubics.size() == 3) {
                Cubic center = features.get(0).cubics.get(1);
                CubicSplit split = center.split(0.5);
                firstFeatureSplitStart = List.of(features.get(0).cubics.get(0), split.first());
                firstFeatureSplitEnd = List.of(split.second(), features.get(0).cubics.get(2));
            }

            for (int i = 0; i <= features.size(); i++) {
                @Nullable List<Cubic> featureCubics;
                if (i == 0 && firstFeatureSplitEnd != null) {
                    featureCubics = firstFeatureSplitEnd;
                } else if (i == features.size()) {
                    featureCubics = firstFeatureSplitStart;
                } else {
                    featureCubics = features.get(i).cubics;
                }
                if (featureCubics == null) {
                    break;
                }

                for (Cubic cubic : featureCubics) {
                    if (!cubic.zeroLength()) {
                        if (lastCubic != null) {
                            result.add(lastCubic);
                        }
                        lastCubic = cubic;
                        if (firstCubic == null) {
                            firstCubic = cubic;
                        }
                    } else if (lastCubic != null) {
                        lastCubic = new Cubic(
                                lastCubic.anchor0X(), lastCubic.anchor0Y(),
                                lastCubic.control0X(), lastCubic.control0Y(),
                                lastCubic.control1X(), lastCubic.control1Y(),
                                cubic.anchor1X(), cubic.anchor1Y()
                        );
                    }
                }
            }

            if (lastCubic != null && firstCubic != null) {
                result.add(new Cubic(
                        lastCubic.anchor0X(), lastCubic.anchor0Y(),
                        lastCubic.control0X(), lastCubic.control0Y(),
                        lastCubic.control1X(), lastCubic.control1Y(),
                        firstCubic.anchor0X(), firstCubic.anchor0Y()
                ));
            }
            return List.copyOf(result);
        }

        /// Validates that the polygon outline is contiguous.
        private void validate() {
            Cubic previous = cubics.get(cubics.size() - 1);
            for (Cubic cubic : cubics) {
                if (Math.abs(cubic.anchor0X() - previous.anchor1X()) > DISTANCE_EPSILON
                        || Math.abs(cubic.anchor0Y() - previous.anchor1Y()) > DISTANCE_EPSILON) {
                    throw new IllegalArgumentException("RoundedPolygon must be contiguous");
                }
                previous = cubic;
            }
        }

        /// Returns this polygon transformed by a point transformer.
        ///
        /// @param transformer the point transformer
        /// @return the transformed polygon
        private RoundedPolygon transformed(PointTransformer transformer) {
            List<Feature> transformed = new ArrayList<>(features.size());
            for (Feature feature : features) {
                transformed.add(feature.transformed(transformer));
            }
            Point center = new Point(centerX, centerY).transformed(transformer);
            return new RoundedPolygon(transformed, center.x(), center.y());
        }

        /// Returns this polygon normalized into a unit square.
        ///
        /// @return the normalized polygon
        private RoundedPolygon normalized() {
            double[] bounds = new double[4];
            calculateBounds(bounds, true);
            double width = bounds[2] - bounds[0];
            double height = bounds[3] - bounds[1];
            double side = Math.max(width, height);
            double offsetX = (side - width) / 2.0 - bounds[0];
            double offsetY = (side - height) / 2.0 - bounds[1];
            return transformed((x, y) -> new Point((x + offsetX) / side, (y + offsetY) / side));
        }

        /// Calculates axis-aligned bounds.
        ///
        /// @param bounds the destination bounds
        /// @param approximate whether to use approximate cubic bounds
        private void calculateBounds(double[] bounds, boolean approximate) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            for (Cubic cubic : cubics) {
                cubic.calculateBounds(bounds, approximate);
                minX = Math.min(minX, bounds[0]);
                minY = Math.min(minY, bounds[1]);
                maxX = Math.max(maxX, bounds[2]);
                maxY = Math.max(maxY, bounds[3]);
            }
            bounds[0] = minX;
            bounds[1] = minY;
            bounds[2] = maxX;
            bounds[3] = maxY;
        }

        /// Calculates max bounds that can hold this polygon under rotation.
        ///
        /// @param bounds the destination bounds
        private void calculateMaxBounds(double[] bounds) {
            double maxDistanceSquared = 0.0;
            for (Cubic cubic : cubics) {
                double anchorDistance = distanceSquared(cubic.anchor0X() - centerX, cubic.anchor0Y() - centerY);
                Point middle = cubic.pointOnCurve(0.5);
                double middleDistance = distanceSquared(middle.x() - centerX, middle.y() - centerY);
                maxDistanceSquared = Math.max(maxDistanceSquared, Math.max(anchorDistance, middleDistance));
            }
            double distance = Math.sqrt(maxDistanceSquared);
            bounds[0] = centerX - distance;
            bounds[1] = centerY - distance;
            bounds[2] = centerX + distance;
            bounds[3] = centerY + distance;
        }
    }

    /// Per-side cut adjustment values.
    ///
    /// @param roundCutRatio the round-cut ratio
    /// @param cutRatio the smoothing cut ratio
    @NotNullByDefault
    private record CutAdjust(double roundCutRatio, double cutRatio) {
    }

    /// A rounded corner helper used while building polygons.
    @NotNullByDefault
    private static final class RoundedCorner {
        /// The previous vertex.
        private final Point previous;

        /// The corner vertex.
        private final Point corner;

        /// The next vertex.
        private final Point next;

        /// The rounding parameters.
        private final CornerRounding rounding;

        /// The direction from the corner to the previous vertex.
        private final Point direction0;

        /// The direction from the corner to the next vertex.
        private final Point direction1;

        /// The requested corner radius.
        private final double cornerRadius;

        /// The requested smoothing amount.
        private final double smoothing;

        /// The expected cut needed for the circular rounding section.
        private final double expectedRoundCut;

        /// The center of the generated rounded corner.
        private Point center = new Point(0.0, 0.0);

        /// Creates a rounded corner helper.
        ///
        /// @param previous the previous vertex
        /// @param corner the corner vertex
        /// @param next the next vertex
        /// @param rounding the rounding parameters
        private RoundedCorner(Point previous, Point corner, Point next, CornerRounding rounding) {
            this.previous = previous;
            this.corner = corner;
            this.next = next;
            this.rounding = rounding;
            this.direction0 = previous.subtract(corner).direction();
            this.direction1 = next.subtract(corner).direction();
            this.cornerRadius = rounding.radius();
            this.smoothing = rounding.smoothing();

            double cosAngle = direction0.dot(direction1);
            double sinAngle = Math.sqrt(1.0 - cosAngle * cosAngle);
            this.expectedRoundCut = sinAngle > 1e-3 ? cornerRadius * (cosAngle + 1.0) / sinAngle : 0.0;
        }

        /// Returns the full expected cut including smoothing.
        ///
        /// @return the expected cut
        private double expectedCut() {
            return (1.0 + smoothing) * expectedRoundCut;
        }

        /// Returns the cubic curves that draw this rounded corner.
        ///
        /// @param allowedCut0 the allowed cut from the previous side
        /// @param allowedCut1 the allowed cut from the next side
        /// @return the rounded corner cubics
        private List<Cubic> getCubics(double allowedCut0, double allowedCut1) {
            double allowedCut = Math.min(allowedCut0, allowedCut1);
            if (expectedRoundCut < DISTANCE_EPSILON
                    || allowedCut < DISTANCE_EPSILON
                    || cornerRadius < DISTANCE_EPSILON) {
                center = corner;
                return List.of(Cubic.straightLine(corner.x(), corner.y(), corner.x(), corner.y()));
            }

            double actualRoundCut = Math.min(allowedCut, expectedRoundCut);
            double actualSmoothing0 = actualSmoothing(allowedCut0);
            double actualSmoothing1 = actualSmoothing(allowedCut1);
            double actualRadius = cornerRadius * actualRoundCut / expectedRoundCut;
            double centerDistance = Math.sqrt(actualRadius * actualRadius + actualRoundCut * actualRoundCut);
            center = corner.add(direction0.add(direction1).divide(2.0).direction().multiply(centerDistance));

            Point circleIntersection0 = corner.add(direction0.multiply(actualRoundCut));
            Point circleIntersection1 = corner.add(direction1.multiply(actualRoundCut));
            Cubic flanking0 = computeFlankingCurve(
                    actualRoundCut,
                    actualSmoothing0,
                    previous,
                    circleIntersection0,
                    circleIntersection1,
                    actualRadius
            );
            Cubic flanking1 = computeFlankingCurve(
                    actualRoundCut,
                    actualSmoothing1,
                    next,
                    circleIntersection1,
                    circleIntersection0,
                    actualRadius
            ).reverse();
            return List.of(
                    flanking0,
                    Cubic.circularArc(center.x(), center.y(), flanking0.anchor1X(), flanking0.anchor1Y(),
                            flanking1.anchor0X(), flanking1.anchor0Y()),
                    flanking1
            );
        }

        /// Returns the actual smoothing value for an allowed cut.
        ///
        /// @param allowedCut the allowed cut
        /// @return the actual smoothing value
        private double actualSmoothing(double allowedCut) {
            if (allowedCut > expectedCut()) {
                return smoothing;
            }
            if (allowedCut > expectedRoundCut) {
                return smoothing * (allowedCut - expectedRoundCut) / (expectedCut() - expectedRoundCut);
            }
            return 0.0;
        }

        /// Computes a flanking curve between a side and a circular segment.
        ///
        /// @param actualRoundCut the active round cut
        /// @param actualSmoothing the active smoothing amount
        /// @param sideStart the side start point
        /// @param circleIntersection the first circle intersection
        /// @param otherCircleIntersection the opposite circle intersection
        /// @param actualRadius the active corner radius
        /// @return the flanking cubic
        private Cubic computeFlankingCurve(
                double actualRoundCut,
                double actualSmoothing,
                Point sideStart,
                Point circleIntersection,
                Point otherCircleIntersection,
                double actualRadius
        ) {
            Point sideDirection = sideStart.subtract(corner).direction();
            Point curveStart = corner.add(sideDirection.multiply(actualRoundCut * (1.0 + actualSmoothing)));
            Point interpolatedCircle = interpolate(
                    circleIntersection,
                    circleIntersection.add(otherCircleIntersection).divide(2.0),
                    actualSmoothing
            );
            Point curveEnd = center.add(interpolatedCircle.subtract(center).direction().multiply(actualRadius));
            Point circleTangent = curveEnd.subtract(center).rotate90();
            Point anchorEnd = lineIntersection(sideStart, sideDirection, curveEnd, circleTangent);
            if (anchorEnd == null) {
                anchorEnd = circleIntersection;
            }
            Point anchorStart = curveStart.add(anchorEnd.multiply(2.0)).divide(3.0);
            return new Cubic(curveStart, anchorStart, anchorEnd, curveEnd);
        }

        /// Returns the intersection point of two infinite lines.
        ///
        /// @param point0 the first line point
        /// @param direction0 the first line direction
        /// @param point1 the second line point
        /// @param direction1 the second line direction
        /// @return the intersection point, or `null` if the lines are effectively parallel
        private @Nullable Point lineIntersection(Point point0, Point direction0, Point point1, Point direction1) {
            Point rotated = direction1.rotate90();
            double denominator = direction0.dot(rotated);
            if (Math.abs(denominator) < DISTANCE_EPSILON) {
                return null;
            }
            double numerator = point1.subtract(point0).dot(rotated);
            if (Math.abs(denominator) < DISTANCE_EPSILON * Math.abs(numerator)) {
                return null;
            }
            return point0.add(direction0.multiply(numerator / denominator));
        }
    }

    /// A measured polygon represented by outline progress.
    @NotNullByDefault
    private static final class MeasuredPolygon {
        /// The cubic measurer used by this polygon.
        private final Measurer measurer;

        /// The measured cubics.
        private final List<MeasuredCubic> cubics;

        /// The measured corner features.
        private final List<ProgressableFeature> features;

        /// Creates a measured polygon.
        ///
        /// @param measurer the cubic measurer
        /// @param features the progressable features
        /// @param sourceCubics the source cubics
        /// @param outlineProgress the outline progress boundaries
        private MeasuredPolygon(
                Measurer measurer,
                List<ProgressableFeature> features,
                List<Cubic> sourceCubics,
                List<Double> outlineProgress
        ) {
            this.measurer = measurer;
            this.features = List.copyOf(features);
            List<MeasuredCubic> measured = new ArrayList<>();
            double startProgress = 0.0;
            for (int i = 0; i < sourceCubics.size(); i++) {
                if (outlineProgress.get(i + 1) - outlineProgress.get(i) > DISTANCE_EPSILON) {
                    measured.add(new MeasuredCubic(
                            measurer,
                            sourceCubics.get(i),
                            startProgress,
                            outlineProgress.get(i + 1)
                    ));
                    startProgress = outlineProgress.get(i + 1);
                }
            }
            measured.get(measured.size() - 1).endProgress = 1.0;
            this.cubics = List.copyOf(measured);
        }

        /// Returns the number of measured cubics.
        ///
        /// @return the measured cubic count
        private int size() {
            return cubics.size();
        }

        /// Returns a measured cubic or `null` when the index is outside the polygon.
        ///
        /// @param index the measured cubic index
        /// @return the measured cubic, or `null`
        private @Nullable MeasuredCubic getOrNull(int index) {
            return index >= 0 && index < cubics.size() ? cubics.get(index) : null;
        }

        /// Cuts this polygon at a progress value and shifts the result to start there.
        ///
        /// @param cuttingPoint the cutting point
        /// @return the cut and shifted polygon
        private MeasuredPolygon cutAndShift(double cuttingPoint) {
            if (cuttingPoint < DISTANCE_EPSILON) {
                return this;
            }

            int targetIndex = -1;
            for (int i = 0; i < cubics.size(); i++) {
                MeasuredCubic cubic = cubics.get(i);
                if (cuttingPoint >= cubic.startProgress && cuttingPoint <= cubic.endProgress) {
                    targetIndex = i;
                    break;
                }
            }
            if (targetIndex < 0) {
                throw new IllegalArgumentException("Cutting point did not intersect any cubic");
            }

            CutStep split = cubics.get(targetIndex).cutAtProgress(cuttingPoint);
            List<Cubic> shiftedCubics = new ArrayList<>(cubics.size() + 1);
            shiftedCubics.add(split.next.cubic);
            for (int i = 1; i < cubics.size(); i++) {
                shiftedCubics.add(cubics.get((i + targetIndex) % cubics.size()).cubic);
            }
            shiftedCubics.add(split.segment.cubic);

            List<Double> shiftedProgress = new ArrayList<>(cubics.size() + 2);
            for (int i = 0; i < cubics.size() + 2; i++) {
                if (i == 0) {
                    shiftedProgress.add(0.0);
                } else if (i == cubics.size() + 1) {
                    shiftedProgress.add(1.0);
                } else {
                    int cubicIndex = (targetIndex + i - 1) % cubics.size();
                    shiftedProgress.add(positiveModulo(cubics.get(cubicIndex).endProgress - cuttingPoint, 1.0));
                }
            }

            List<ProgressableFeature> shiftedFeatures = new ArrayList<>(features.size());
            for (ProgressableFeature feature : features) {
                shiftedFeatures.add(new ProgressableFeature(
                        positiveModulo(feature.progress() - cuttingPoint, 1.0),
                        feature.feature()
                ));
            }
            return new MeasuredPolygon(measurer, shiftedFeatures, shiftedCubics, shiftedProgress);
        }

        /// Measures a rounded polygon.
        ///
        /// @param measurer the cubic measurer
        /// @param polygon the polygon to measure
        /// @return the measured polygon
        private static MeasuredPolygon measure(Measurer measurer, RoundedPolygon polygon) {
            List<Cubic> cubics = new ArrayList<>();
            List<FeatureCubicIndex> featureToCubic = new ArrayList<>();
            for (Feature feature : polygon.features) {
                for (int i = 0; i < feature.cubics.size(); i++) {
                    if (feature instanceof Feature.Corner && i == feature.cubics.size() / 2) {
                        featureToCubic.add(new FeatureCubicIndex(feature, cubics.size()));
                    }
                    cubics.add(feature.cubics.get(i));
                }
            }

            List<Double> measures = new ArrayList<>(cubics.size() + 1);
            measures.add(0.0);
            double total = 0.0;
            for (Cubic cubic : cubics) {
                total += measurer.measure(cubic);
                if (total < 0.0) {
                    throw new IllegalStateException("Measured cubic is expected to be non-negative");
                }
                measures.add(total);
            }

            List<Double> outlineProgress = new ArrayList<>(measures.size());
            for (double measure : measures) {
                outlineProgress.add(measure / total);
            }

            List<ProgressableFeature> features = new ArrayList<>(featureToCubic.size());
            for (FeatureCubicIndex entry : featureToCubic) {
                int index = entry.cubicIndex();
                features.add(new ProgressableFeature(
                        (outlineProgress.get(index) + outlineProgress.get(index + 1)) / 2.0,
                        entry.feature()
                ));
            }

            return new MeasuredPolygon(measurer, features, cubics, outlineProgress);
        }
    }

    /// A feature and representative cubic index.
    ///
    /// @param feature the feature
    /// @param cubicIndex the cubic index
    @NotNullByDefault
    private record FeatureCubicIndex(Feature feature, int cubicIndex) {
    }

    /// A measured cubic curve.
    @NotNullByDefault
    private static final class MeasuredCubic {
        /// The measurer used to split this cubic.
        private final Measurer measurer;

        /// The measured cubic.
        private final Cubic cubic;

        /// The size reported by the measurer for this cubic.
        private final double measuredSize;

        /// The start outline progress.
        private final double startProgress;

        /// The end outline progress.
        private double endProgress;

        /// Creates a measured cubic.
        ///
        /// @param cubic the cubic
        /// @param startProgress the start outline progress
        /// @param endProgress the end outline progress
        private MeasuredCubic(Measurer measurer, Cubic cubic, double startProgress, double endProgress) {
            this.measurer = measurer;
            this.cubic = cubic;
            this.measuredSize = measurer.measure(cubic);
            this.startProgress = startProgress;
            this.endProgress = endProgress;
        }

        /// Cuts this cubic at the requested outline progress.
        ///
        /// @param cutProgress the cut outline progress
        /// @return the split segment and next segment
        private CutStep cutAtProgress(double cutProgress) {
            double bounded = Math.max(startProgress, Math.min(endProgress, cutProgress));
            double outlineProgressSize = endProgress - startProgress;
            double progressFromStart = bounded - startProgress;
            double relativeProgress = progressFromStart / outlineProgressSize;
            double t = measurer.findCutPoint(cubic, relativeProgress * measuredSize);
            CubicSplit split = cubic.split(t);
            return new CutStep(
                    new MeasuredCubic(measurer, split.first(), startProgress, bounded),
                    new MeasuredCubic(measurer, split.second(), bounded, endProgress)
            );
        }
    }

    /// A cut segment and the next measured cubic.
    ///
    /// @param segment the emitted segment
    /// @param next the next segment, or `null`
    @NotNullByDefault
    private record CutStep(MeasuredCubic segment, @Nullable MeasuredCubic next) {
    }

    /// Measures cubic curves.
    @NotNullByDefault
    private interface Measurer {
        /// Measures a cubic.
        ///
        /// @param cubic the cubic
        /// @return the measured size
        double measure(Cubic cubic);

        /// Finds the cubic parameter that reaches the requested measured distance.
        ///
        /// @param cubic the cubic
        /// @param measure the target measured distance
        /// @return the cubic parameter
        double findCutPoint(Cubic cubic, double measure);
    }

    /// Measures cubic curves by their angle around a center point.
    @NotNullByDefault
    private static final class AngleMeasurer implements Measurer {
        /// The center x-coordinate.
        private final double centerX;

        /// The center y-coordinate.
        private final double centerY;

        /// Creates an angle measurer.
        ///
        /// @param centerX the center x-coordinate
        /// @param centerY the center y-coordinate
        private AngleMeasurer(double centerX, double centerY) {
            this.centerX = centerX;
            this.centerY = centerY;
        }

        /// Measures a cubic by the angle between its anchors.
        ///
        /// @param cubic the cubic
        /// @return the measured angle
        @Override
        public double measure(Cubic cubic) {
            double value = positiveModulo(
                    angle(cubic.anchor1X() - centerX, cubic.anchor1Y() - centerY)
                            - angle(cubic.anchor0X() - centerX, cubic.anchor0Y() - centerY),
                    TWO_PI
            );
            return value > TWO_PI - DISTANCE_EPSILON ? 0.0 : value;
        }

        /// Finds the cubic parameter that reaches a target angular distance.
        ///
        /// @param cubic the cubic
        /// @param measure the target angular distance
        /// @return the cubic parameter
        @Override
        public double findCutPoint(Cubic cubic, double measure) {
            double angle0 = angle(cubic.anchor0X() - centerX, cubic.anchor0Y() - centerY);
            return findMinimum(0.0, 1.0, 1e-5, t -> {
                Point point = cubic.pointOnCurve(t);
                double pointAngle = angle(point.x() - centerX, point.y() - centerY);
                return Math.abs(positiveModulo(pointAngle - angle0, TWO_PI) - measure);
            });
        }
    }

    /// Maps progress from one outline to another and back.
    @NotNullByDefault
    private static final class DoubleMapper {
        /// The source progress values.
        private final double @Unmodifiable [] sourceValues;

        /// The target progress values.
        private final double @Unmodifiable [] targetValues;

        /// Creates a double mapper.
        ///
        /// @param mappings the progress mappings
        private DoubleMapper(List<ProgressPair> mappings) {
            if (mappings.size() < 2) {
                mappings = List.of(new ProgressPair(0.0, 0.0), new ProgressPair(0.5, 0.5));
            }
            sourceValues = new double[mappings.size()];
            targetValues = new double[mappings.size()];
            for (int i = 0; i < mappings.size(); i++) {
                sourceValues[i] = mappings.get(i).source();
                targetValues[i] = mappings.get(i).target();
            }
        }

        /// Maps source progress to target progress.
        ///
        /// @param value the source progress
        /// @return the target progress
        private double map(double value) {
            return linearMap(sourceValues, targetValues, value);
        }

        /// Maps target progress back to source progress.
        ///
        /// @param value the target progress
        /// @return the source progress
        private double mapBack(double value) {
            return linearMap(targetValues, sourceValues, value);
        }

        /// Linearly maps a progress value between cyclic progress sets.
        ///
        /// @param xValues the source values
        /// @param yValues the target values
        /// @param x the source progress
        /// @return the mapped progress
        private static double linearMap(double[] xValues, double[] yValues, double x) {
            double progress = positiveModulo(x, 1.0);
            int segmentStart = 0;
            for (int i = 0; i < xValues.length; i++) {
                if (progressInRange(progress, xValues[i], xValues[(i + 1) % xValues.length])) {
                    segmentStart = i;
                    break;
                }
            }

            int segmentEnd = (segmentStart + 1) % xValues.length;
            double segmentSizeX = positiveModulo(xValues[segmentEnd] - xValues[segmentStart], 1.0);
            double segmentSizeY = positiveModulo(yValues[segmentEnd] - yValues[segmentStart], 1.0);
            double position = segmentSizeX < 0.001
                    ? 0.5
                    : positiveModulo(progress - xValues[segmentStart], 1.0) / segmentSizeX;
            return positiveModulo(yValues[segmentStart] + segmentSizeY * position, 1.0);
        }

        /// Returns whether a progress value is inside a cyclic range.
        ///
        /// @param progress the progress value
        /// @param from the range start
        /// @param to the range end
        /// @return whether the progress is in range
        private static boolean progressInRange(double progress, double from, double to) {
            return to >= from ? progress >= from && progress <= to : progress >= from || progress <= to;
        }
    }
}
