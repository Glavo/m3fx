// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.function.IntToDoubleFunction;

/// Provides reusable vector converters for common immutable JavaFX value types.
///
/// These converters use the natural component order of each represented type. Geometric components use a
/// half-pixel spring visibility threshold. Color channels use an 8-bit component threshold and are clamped to the
/// JavaFX color range when reconstructed, so a spring may overshoot internally without producing an invalid
/// [Color].
@NotNullByDefault
public final class M3VectorConverters {
    /// Converts [Color] values in red, green, blue, and opacity order.
    public static final M3VectorConverter<Color> COLOR = new M3VectorConverter<>() {
        /// Returns four color components.
        @Override
        public int getComponentCount() {
            return 4;
        }

        /// Returns the requested color component.
        @Override
        public double getComponent(Color value, int index) {
            return switch (index) {
                case 0 -> value.getRed();
                case 1 -> value.getGreen();
                case 2 -> value.getBlue();
                case 3 -> value.getOpacity();
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        /// Creates a color while containing possible spring overshoot.
        @Override
        public Color createValue(IntToDoubleFunction components) {
            return new Color(
                    clampUnit(components.applyAsDouble(0)),
                    clampUnit(components.applyAsDouble(1)),
                    clampUnit(components.applyAsDouble(2)),
                    clampUnit(components.applyAsDouble(3))
            );
        }

        /// Returns one 8-bit color-component threshold.
        @Override
        public double getVisibilityThreshold(int index) {
            checkIndex(index, 4);
            return 1.0 / 255.0;
        }
    };

    /// Converts [Point2D] values in x and y order.
    public static final M3VectorConverter<Point2D> POINT_2D = new M3VectorConverter<>() {
        /// Returns two point components.
        @Override
        public int getComponentCount() {
            return 2;
        }

        /// Returns the requested point component.
        @Override
        public double getComponent(Point2D value, int index) {
            return switch (index) {
                case 0 -> value.getX();
                case 1 -> value.getY();
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        /// Creates a point from its current components.
        @Override
        public Point2D createValue(IntToDoubleFunction components) {
            return new Point2D(components.applyAsDouble(0), components.applyAsDouble(1));
        }

        /// Returns the geometric visibility threshold.
        @Override
        public double getVisibilityThreshold(int index) {
            checkIndex(index, 2);
            return 0.5;
        }
    };

    /// Converts [Point3D] values in x, y, and z order.
    public static final M3VectorConverter<Point3D> POINT_3D = new M3VectorConverter<>() {
        /// Returns three point components.
        @Override
        public int getComponentCount() {
            return 3;
        }

        /// Returns the requested point component.
        @Override
        public double getComponent(Point3D value, int index) {
            return switch (index) {
                case 0 -> value.getX();
                case 1 -> value.getY();
                case 2 -> value.getZ();
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        /// Creates a point from its current components.
        @Override
        public Point3D createValue(IntToDoubleFunction components) {
            return new Point3D(
                    components.applyAsDouble(0),
                    components.applyAsDouble(1),
                    components.applyAsDouble(2)
            );
        }

        /// Returns the geometric visibility threshold.
        @Override
        public double getVisibilityThreshold(int index) {
            checkIndex(index, 3);
            return 0.5;
        }
    };

    /// Converts [Dimension2D] values in width and height order.
    public static final M3VectorConverter<Dimension2D> DIMENSION_2D = new M3VectorConverter<>() {
        /// Returns two dimension components.
        @Override
        public int getComponentCount() {
            return 2;
        }

        /// Returns the requested dimension component.
        @Override
        public double getComponent(Dimension2D value, int index) {
            return switch (index) {
                case 0 -> value.getWidth();
                case 1 -> value.getHeight();
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        /// Creates a dimension while containing possible negative spring overshoot.
        @Override
        public Dimension2D createValue(IntToDoubleFunction components) {
            return new Dimension2D(
                    Math.max(0.0, components.applyAsDouble(0)),
                    Math.max(0.0, components.applyAsDouble(1))
            );
        }

        /// Returns the geometric visibility threshold.
        @Override
        public double getVisibilityThreshold(int index) {
            checkIndex(index, 2);
            return 0.5;
        }
    };

    /// Converts [Rectangle2D] values in minimum-x, minimum-y, width, and height order.
    public static final M3VectorConverter<Rectangle2D> RECTANGLE_2D = new M3VectorConverter<>() {
        /// Returns four rectangle components.
        @Override
        public int getComponentCount() {
            return 4;
        }

        /// Returns the requested rectangle component.
        @Override
        public double getComponent(Rectangle2D value, int index) {
            return switch (index) {
                case 0 -> value.getMinX();
                case 1 -> value.getMinY();
                case 2 -> value.getWidth();
                case 3 -> value.getHeight();
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        /// Creates a rectangle while containing possible negative size overshoot.
        @Override
        public Rectangle2D createValue(IntToDoubleFunction components) {
            return new Rectangle2D(
                    components.applyAsDouble(0),
                    components.applyAsDouble(1),
                    Math.max(0.0, components.applyAsDouble(2)),
                    Math.max(0.0, components.applyAsDouble(3))
            );
        }

        /// Returns the geometric visibility threshold.
        @Override
        public double getVisibilityThreshold(int index) {
            checkIndex(index, 4);
            return 0.5;
        }
    };

    /// Converts [Insets] values in top, right, bottom, and left order.
    public static final M3VectorConverter<Insets> INSETS = new M3VectorConverter<>() {
        /// Returns four inset components.
        @Override
        public int getComponentCount() {
            return 4;
        }

        /// Returns the requested inset component.
        @Override
        public double getComponent(Insets value, int index) {
            return switch (index) {
                case 0 -> value.getTop();
                case 1 -> value.getRight();
                case 2 -> value.getBottom();
                case 3 -> value.getLeft();
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        /// Creates insets from their current components.
        @Override
        public Insets createValue(IntToDoubleFunction components) {
            return new Insets(
                    components.applyAsDouble(0),
                    components.applyAsDouble(1),
                    components.applyAsDouble(2),
                    components.applyAsDouble(3)
            );
        }

        /// Returns the geometric visibility threshold.
        @Override
        public double getVisibilityThreshold(int index) {
            checkIndex(index, 4);
            return 0.5;
        }
    };

    /// Prevents instantiation.
    private M3VectorConverters() {
    }

    /// Returns a value limited to the inclusive unit interval.
    private static double clampUnit(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Verifies one component index.
    private static void checkIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
    }
}
