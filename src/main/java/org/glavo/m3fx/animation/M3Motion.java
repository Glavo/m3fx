// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Interpolator;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;

/// Material Design 3 motion easing and duration constants for JavaFX animations.
///
/// Duration constants are immutable [Duration] values. Easing constants map normalized input progress to
/// normalized output progress and can be supplied directly to JavaFX transitions. Component code should normally
/// consume semantic [M3MotionSpec] roles from the active theme so Standard and Expressive profiles can differ.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3Motion {
    /// A linear animation curve.
    public static final Interpolator LINEAR = Interpolator.LINEAR;

    /// The Material Design 3 standard easing curve.
    public static final Interpolator STANDARD = new CubicInterpolator(0.2, 0.0, 0.0, 1.0);

    /// The Material Design 3 standard accelerate easing curve.
    public static final Interpolator STANDARD_ACCELERATE = new CubicInterpolator(0.3, 0.0, 1.0, 1.0);

    /// The Material Design 3 standard decelerate easing curve.
    public static final Interpolator STANDARD_DECELERATE = new CubicInterpolator(0.0, 0.0, 0.0, 1.0);

    /// The Material Design 3 emphasized easing curve for larger transitions.
    public static final Interpolator EMPHASIZED = new ThreePointCubicInterpolator(
            new Offset(0.05, 0.0),
            new Offset(0.133333, 0.06),
            new Offset(0.166666, 0.4),
            new Offset(0.208333, 0.82),
            new Offset(0.25, 1.0)
    );

    /// The Material Design 3 emphasized accelerate easing curve.
    public static final Interpolator EMPHASIZED_ACCELERATE = new CubicInterpolator(0.3, 0.0, 0.8, 0.15);

    /// The Material Design 3 emphasized decelerate easing curve.
    public static final Interpolator EMPHASIZED_DECELERATE = new CubicInterpolator(0.05, 0.7, 0.1, 1.0);

    /// The short1 duration token.
    public static final Duration SHORT1 = Duration.millis(50.0);

    /// The short2 duration token.
    public static final Duration SHORT2 = Duration.millis(100.0);

    /// The short3 duration token.
    public static final Duration SHORT3 = Duration.millis(150.0);

    /// The short4 duration token.
    public static final Duration SHORT4 = Duration.millis(200.0);

    /// The medium1 duration token.
    public static final Duration MEDIUM1 = Duration.millis(250.0);

    /// The medium2 duration token.
    public static final Duration MEDIUM2 = Duration.millis(300.0);

    /// The medium3 duration token.
    public static final Duration MEDIUM3 = Duration.millis(350.0);

    /// The medium4 duration token.
    public static final Duration MEDIUM4 = Duration.millis(400.0);

    /// The long1 duration token.
    public static final Duration LONG1 = Duration.millis(450.0);

    /// The long2 duration token.
    public static final Duration LONG2 = Duration.millis(500.0);

    /// The long3 duration token.
    public static final Duration LONG3 = Duration.millis(550.0);

    /// The long4 duration token.
    public static final Duration LONG4 = Duration.millis(600.0);

    /// The extraLong1 duration token.
    public static final Duration EXTRA_LONG1 = Duration.millis(700.0);

    /// The extraLong2 duration token.
    public static final Duration EXTRA_LONG2 = Duration.millis(800.0);

    /// The extraLong3 duration token.
    public static final Duration EXTRA_LONG3 = Duration.millis(900.0);

    /// The extraLong4 duration token.
    public static final Duration EXTRA_LONG4 = Duration.millis(1000.0);

    /// Prevents instantiation.
    private M3Motion() {
    }

    /// Evaluates a cubic Bezier easing curve for the supplied x progress.
    private static double cubicBezier(double x1, double y1, double x2, double y2, double x) {
        double low = 0.0;
        double high = 1.0;
        double t = x;
        for (int i = 0; i < 24; i++) {
            t = (low + high) / 2.0;
            double estimate = cubicCoordinate(x1, x2, t);
            if (estimate < x) {
                low = t;
            } else {
                high = t;
            }
        }
        return cubicCoordinate(y1, y2, t);
    }

    /// Evaluates one coordinate of a cubic Bezier curve whose endpoints are zero and one.
    private static double cubicCoordinate(double firstControl, double secondControl, double t) {
        double inverse = 1.0 - t;
        return 3.0 * firstControl * inverse * inverse * t
                + 3.0 * secondControl * inverse * t * t
                + t * t * t;
    }

    /// A two-dimensional control point.
    ///
    /// @param x the x coordinate
    /// @param y the y coordinate
    @NotNullByDefault
    private record Offset(double x, double y) {
    }

    /// A JavaFX interpolator backed by one cubic Bezier curve.
    @NotNullByDefault
    private static final class CubicInterpolator extends Interpolator {
        /// The x coordinate of the first control point.
        private final double x1;

        /// The y coordinate of the first control point.
        private final double y1;

        /// The x coordinate of the second control point.
        private final double x2;

        /// The y coordinate of the second control point.
        private final double y2;

        /// Creates a cubic Bezier interpolator.
        private CubicInterpolator(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /// Computes the eased value for the supplied progress.
        @Override
        protected double curve(double t) {
            return cubicBezier(x1, y1, x2, y2, t);
        }

        /// Returns a debug representation of this curve.
        @Override
        public String toString() {
            return "CubicInterpolator[x1=" + x1
                    + ", y1=" + y1
                    + ", x2=" + x2
                    + ", y2=" + y2 + "]";
        }
    }

    /// A JavaFX interpolator backed by two cubic Bezier curves with a shared midpoint.
    @NotNullByDefault
    private static final class ThreePointCubicInterpolator extends Interpolator {
        /// The first control point of the first cubic curve.
        private final Offset firstStartControl;

        /// The second control point of the first cubic curve.
        private final Offset firstEndControl;

        /// The midpoint shared by both cubic curves.
        private final Offset midpoint;

        /// The first control point of the second cubic curve.
        private final Offset secondStartControl;

        /// The second control point of the second cubic curve.
        private final Offset secondEndControl;

        /// Creates a three-point cubic interpolator.
        private ThreePointCubicInterpolator(
                Offset firstStartControl,
                Offset firstEndControl,
                Offset midpoint,
                Offset secondStartControl,
                Offset secondEndControl
        ) {
            this.firstStartControl = firstStartControl;
            this.firstEndControl = firstEndControl;
            this.midpoint = midpoint;
            this.secondStartControl = secondStartControl;
            this.secondEndControl = secondEndControl;
        }

        /// Computes the eased value for the supplied progress.
        @Override
        protected double curve(double t) {
            if (t < midpoint.x()) {
                double scaleX = midpoint.x();
                double scaleY = midpoint.y();
                double scaledT = t / scaleX;
                return cubicBezier(
                        firstStartControl.x() / scaleX,
                        firstStartControl.y() / scaleY,
                        firstEndControl.x() / scaleX,
                        firstEndControl.y() / scaleY,
                        scaledT
                ) * scaleY;
            }

            double scaleX = 1.0 - midpoint.x();
            double scaleY = 1.0 - midpoint.y();
            double scaledT = (t - midpoint.x()) / scaleX;
            return cubicBezier(
                    (secondStartControl.x() - midpoint.x()) / scaleX,
                    (secondStartControl.y() - midpoint.y()) / scaleY,
                    (secondEndControl.x() - midpoint.x()) / scaleX,
                    (secondEndControl.y() - midpoint.y()) / scaleY,
                    scaledT
            ) * scaleY + midpoint.y();
        }

        /// Returns a debug representation of this curve.
        @Override
        public String toString() {
            return "ThreePointCubicInterpolator[firstStartControl=" + firstStartControl
                    + ", firstEndControl=" + firstEndControl
                    + ", midpoint=" + midpoint
                    + ", secondStartControl=" + secondStartControl
                    + ", secondEndControl=" + secondEndControl + "]";
        }
    }
}
