// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Interpolator;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;

/// Provides the named easing curves and duration values defined by the Material Design 3 motion system.
///
/// The easing constants are JavaFX [Interpolator] instances and can be assigned directly to a transition or key
/// value. They map an input fraction in the usual JavaFX interpolation range from `0.0` through `1.0` to an eased
/// output fraction. The duration constants are immutable [Duration] values expressed in milliseconds.
///
/// These constants describe the primitive Material motion scale. Controls and applications that need a semantic
/// choice such as a fast effects transition or a default spatial transition should use [M3MotionScheme] instead;
/// doing so allows the active Standard or Expressive profile to select the appropriate duration and easing pair.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3Motion {
    /// A linear animation curve with no acceleration or deceleration.
    public static final Interpolator LINEAR = Interpolator.LINEAR;

    /// The Material Design 3 standard easing curve for transitions that remain on screen.
    public static final Interpolator STANDARD = new CubicInterpolator(0.2, 0.0, 0.0, 1.0);

    /// The Material Design 3 standard accelerate easing curve, which ends faster than it starts.
    public static final Interpolator STANDARD_ACCELERATE = new CubicInterpolator(0.3, 0.0, 1.0, 1.0);

    /// The Material Design 3 standard decelerate easing curve, which begins faster than it ends.
    public static final Interpolator STANDARD_DECELERATE = new CubicInterpolator(0.0, 0.0, 0.0, 1.0);

    /// The Material Design 3 emphasized easing curve for prominent or spatial transitions.
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

    /// The finite fallback curve for Standard spatial springs.
    public static final Interpolator STANDARD_SPATIAL = new CubicInterpolator(0.27, 1.06, 0.18, 1.0);

    /// The finite fallback curve for the Expressive fast spatial spring.
    public static final Interpolator EXPRESSIVE_FAST_SPATIAL = new CubicInterpolator(0.42, 1.67, 0.21, 0.90);

    /// The finite fallback curve for the Expressive default spatial spring.
    public static final Interpolator EXPRESSIVE_DEFAULT_SPATIAL = new CubicInterpolator(0.38, 1.21, 0.22, 1.0);

    /// The finite fallback curve for the Expressive slow spatial spring.
    public static final Interpolator EXPRESSIVE_SLOW_SPATIAL = new CubicInterpolator(0.39, 1.29, 0.35, 0.98);

    /// The finite fallback curve for fast effects springs.
    public static final Interpolator FAST_EFFECTS = new CubicInterpolator(0.31, 0.94, 0.34, 1.0);

    /// The finite fallback curve for default effects springs.
    public static final Interpolator DEFAULT_EFFECTS = new CubicInterpolator(0.34, 0.80, 0.34, 1.0);

    /// The finite fallback curve for slow effects springs.
    public static final Interpolator SLOW_EFFECTS = new CubicInterpolator(0.34, 0.88, 0.34, 1.0);

    /// The short1 duration token, equal to 50 milliseconds.
    public static final Duration SHORT1 = Duration.millis(50.0);

    /// The short2 duration token, equal to 100 milliseconds.
    public static final Duration SHORT2 = Duration.millis(100.0);

    /// The short3 duration token, equal to 150 milliseconds.
    public static final Duration SHORT3 = Duration.millis(150.0);

    /// The short4 duration token, equal to 200 milliseconds.
    public static final Duration SHORT4 = Duration.millis(200.0);

    /// The medium1 duration token, equal to 250 milliseconds.
    public static final Duration MEDIUM1 = Duration.millis(250.0);

    /// The medium2 duration token, equal to 300 milliseconds.
    public static final Duration MEDIUM2 = Duration.millis(300.0);

    /// The medium3 duration token, equal to 350 milliseconds.
    public static final Duration MEDIUM3 = Duration.millis(350.0);

    /// The medium4 duration token, equal to 400 milliseconds.
    public static final Duration MEDIUM4 = Duration.millis(400.0);

    /// The long1 duration token, equal to 450 milliseconds.
    public static final Duration LONG1 = Duration.millis(450.0);

    /// The long2 duration token, equal to 500 milliseconds.
    public static final Duration LONG2 = Duration.millis(500.0);

    /// The long3 duration token, equal to 550 milliseconds.
    public static final Duration LONG3 = Duration.millis(550.0);

    /// The long4 duration token, equal to 600 milliseconds.
    public static final Duration LONG4 = Duration.millis(600.0);

    /// The extraLong1 duration token, equal to 700 milliseconds.
    public static final Duration EXTRA_LONG1 = Duration.millis(700.0);

    /// The extraLong2 duration token, equal to 800 milliseconds.
    public static final Duration EXTRA_LONG2 = Duration.millis(800.0);

    /// The extraLong3 duration token, equal to 900 milliseconds.
    public static final Duration EXTRA_LONG3 = Duration.millis(900.0);

    /// The extraLong4 duration token, equal to 1000 milliseconds.
    public static final Duration EXTRA_LONG4 = Duration.millis(1000.0);

    /// Prevents instantiation.
    private M3Motion() {
    }

    /// Evaluates a cubic Bezier easing curve with precomputed polynomial coefficients.
    private static double cubicBezier(
            double xA,
            double xB,
            double xC,
            double yA,
            double yB,
            double yC,
            double x
    ) {
        double low = 0.0;
        double high = 1.0;
        double t = x;
        for (int i = 0; i < 24; i++) {
            t = (low + high) / 2.0;
            double estimate = cubicCoordinate(xA, xB, xC, t);
            if (estimate < x) {
                low = t;
            } else {
                high = t;
            }
        }
        return cubicCoordinate(yA, yB, yC, t);
    }

    /// Evaluates one precomputed coordinate polynomial whose endpoints are zero and one.
    private static double cubicCoordinate(double a, double b, double c, double t) {
        return ((a * t + b) * t + c) * t;
    }

    /// Returns the cubic coefficient for one pair of control coordinates.
    private static double cubicA(double firstControl, double secondControl) {
        return 1.0 - 3.0 * secondControl + 3.0 * firstControl;
    }

    /// Returns the quadratic coefficient for one pair of control coordinates.
    private static double cubicB(double firstControl, double secondControl) {
        return 3.0 * secondControl - 6.0 * firstControl;
    }

    /// Returns the linear coefficient for the first control coordinate.
    private static double cubicC(double firstControl) {
        return 3.0 * firstControl;
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
        /// The cubic coefficient of the x coordinate polynomial.
        private final double xA;

        /// The quadratic coefficient of the x coordinate polynomial.
        private final double xB;

        /// The linear coefficient of the x coordinate polynomial.
        private final double xC;

        /// The cubic coefficient of the y coordinate polynomial.
        private final double yA;

        /// The quadratic coefficient of the y coordinate polynomial.
        private final double yB;

        /// The linear coefficient of the y coordinate polynomial.
        private final double yC;

        /// Creates a cubic Bezier interpolator.
        private CubicInterpolator(double x1, double y1, double x2, double y2) {
            xA = cubicA(x1, x2);
            xB = cubicB(x1, x2);
            xC = cubicC(x1);
            yA = cubicA(y1, y2);
            yB = cubicB(y1, y2);
            yC = cubicC(y1);
        }

        /// Computes the eased value for the supplied progress.
        @Override
        protected double curve(double t) {
            return cubicBezier(xA, xB, xC, yA, yB, yC, t);
        }

        /// Returns a debug representation of this curve.
        @Override
        public String toString() {
            return "CubicInterpolator[xA=" + xA
                    + ", xB=" + xB
                    + ", xC=" + xC
                    + ", yA=" + yA
                    + ", yB=" + yB
                    + ", yC=" + yC + "]";
        }
    }

    /// A JavaFX interpolator backed by two cubic Bezier curves with a shared midpoint.
    @NotNullByDefault
    private static final class ThreePointCubicInterpolator extends Interpolator {
        /// The shared midpoint x coordinate.
        private final double midpointX;

        /// The shared midpoint y coordinate.
        private final double midpointY;

        /// The cubic coefficient of the first segment's x polynomial.
        private final double firstXA;

        /// The quadratic coefficient of the first segment's x polynomial.
        private final double firstXB;

        /// The linear coefficient of the first segment's x polynomial.
        private final double firstXC;

        /// The cubic coefficient of the first segment's y polynomial.
        private final double firstYA;

        /// The quadratic coefficient of the first segment's y polynomial.
        private final double firstYB;

        /// The linear coefficient of the first segment's y polynomial.
        private final double firstYC;

        /// The cubic coefficient of the second segment's x polynomial.
        private final double secondXA;

        /// The quadratic coefficient of the second segment's x polynomial.
        private final double secondXB;

        /// The linear coefficient of the second segment's x polynomial.
        private final double secondXC;

        /// The cubic coefficient of the second segment's y polynomial.
        private final double secondYA;

        /// The quadratic coefficient of the second segment's y polynomial.
        private final double secondYB;

        /// The linear coefficient of the second segment's y polynomial.
        private final double secondYC;

        /// Creates a three-point cubic interpolator.
        private ThreePointCubicInterpolator(
                Offset firstStartControl,
                Offset firstEndControl,
                Offset midpoint,
                Offset secondStartControl,
                Offset secondEndControl
        ) {
            midpointX = midpoint.x();
            midpointY = midpoint.y();

            double firstX1 = firstStartControl.x() / midpointX;
            double firstX2 = firstEndControl.x() / midpointX;
            double firstY1 = firstStartControl.y() / midpointY;
            double firstY2 = firstEndControl.y() / midpointY;
            firstXA = cubicA(firstX1, firstX2);
            firstXB = cubicB(firstX1, firstX2);
            firstXC = cubicC(firstX1);
            firstYA = cubicA(firstY1, firstY2);
            firstYB = cubicB(firstY1, firstY2);
            firstYC = cubicC(firstY1);

            double secondScaleX = 1.0 - midpointX;
            double secondScaleY = 1.0 - midpointY;
            double secondX1 = (secondStartControl.x() - midpointX) / secondScaleX;
            double secondX2 = (secondEndControl.x() - midpointX) / secondScaleX;
            double secondY1 = (secondStartControl.y() - midpointY) / secondScaleY;
            double secondY2 = (secondEndControl.y() - midpointY) / secondScaleY;
            secondXA = cubicA(secondX1, secondX2);
            secondXB = cubicB(secondX1, secondX2);
            secondXC = cubicC(secondX1);
            secondYA = cubicA(secondY1, secondY2);
            secondYB = cubicB(secondY1, secondY2);
            secondYC = cubicC(secondY1);
        }

        /// Computes the eased value for the supplied progress.
        @Override
        protected double curve(double t) {
            if (t < midpointX) {
                double scaledT = t / midpointX;
                return cubicBezier(
                        firstXA,
                        firstXB,
                        firstXC,
                        firstYA,
                        firstYB,
                        firstYC,
                        scaledT
                ) * midpointY;
            }

            double scaleX = 1.0 - midpointX;
            double scaleY = 1.0 - midpointY;
            double scaledT = (t - midpointX) / scaleX;
            return cubicBezier(
                    secondXA,
                    secondXB,
                    secondXC,
                    secondYA,
                    secondYB,
                    secondYC,
                    scaledT
            ) * scaleY + midpointY;
        }

        /// Returns a debug representation of this curve.
        @Override
        public String toString() {
            return "ThreePointCubicInterpolator[midpointX=" + midpointX
                    + ", midpointY=" + midpointY
                    + ", firstXA=" + firstXA
                    + ", firstXB=" + firstXB
                    + ", firstXC=" + firstXC
                    + ", firstYA=" + firstYA
                    + ", firstYB=" + firstYB
                    + ", firstYC=" + firstYC
                    + ", secondXA=" + secondXA
                    + ", secondXB=" + secondXB
                    + ", secondXC=" + secondXC
                    + ", secondYA=" + secondYA
                    + ", secondYB=" + secondYB
                    + ", secondYC=" + secondYC + "]";
        }
    }
}
