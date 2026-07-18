// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.jetbrains.annotations.NotNullByDefault;

/// A reusable asymmetric rounded rectangle used as a JavaFX region shape.
///
/// The path elements are retained for the lifetime of the shape. Updating the bounds or corner radii therefore
/// changes only primitive path coordinates and does not allocate pulse-local geometry objects.
@NotNullByDefault
final class M3RoundedRectangleShape extends Path {
    /// The path starting point.
    private final MoveTo start = new MoveTo();

    /// The top edge.
    private final LineTo topEdge = new LineTo();

    /// The rounded top-right corner.
    private final ArcTo topRightArc = new ArcTo();

    /// The right edge.
    private final LineTo rightEdge = new LineTo();

    /// The rounded bottom-right corner.
    private final ArcTo bottomRightArc = new ArcTo();

    /// The bottom edge.
    private final LineTo bottomEdge = new LineTo();

    /// The rounded bottom-left corner.
    private final ArcTo bottomLeftArc = new ArcTo();

    /// The left edge.
    private final LineTo leftEdge = new LineTo();

    /// The rounded top-left corner.
    private final ArcTo topLeftArc = new ArcTo();

    /// The width represented by the current path.
    private double width = Double.NaN;

    /// The height represented by the current path.
    private double height = Double.NaN;

    /// The current top-left horizontal radius.
    private double topLeftHorizontalRadius = Double.NaN;

    /// The current top-left vertical radius.
    private double topLeftVerticalRadius = Double.NaN;

    /// The current top-right horizontal radius.
    private double topRightHorizontalRadius = Double.NaN;

    /// The current top-right vertical radius.
    private double topRightVerticalRadius = Double.NaN;

    /// The current bottom-right horizontal radius.
    private double bottomRightHorizontalRadius = Double.NaN;

    /// The current bottom-right vertical radius.
    private double bottomRightVerticalRadius = Double.NaN;

    /// The current bottom-left horizontal radius.
    private double bottomLeftHorizontalRadius = Double.NaN;

    /// The current bottom-left vertical radius.
    private double bottomLeftVerticalRadius = Double.NaN;

    /// Creates an empty reusable rounded rectangle.
    M3RoundedRectangleShape() {
        setFill(Color.BLACK);
        setStroke(null);
        getElements().addAll(
                start,
                topEdge,
                topRightArc,
                rightEdge,
                bottomRightArc,
                bottomEdge,
                bottomLeftArc,
                leftEdge,
                topLeftArc,
                new ClosePath()
        );
    }

    /// Updates the rounded rectangle to the supplied bounds and corner radii.
    ///
    /// Horizontal and vertical radii are clamped independently to half their corresponding dimension. Callers
    /// that resolve CSS radii first preserve JavaFX's uniform scaling for over-constrained corners.
    ///
    /// @param width the shape width
    /// @param height the shape height
    /// @param topLeftRadius the top-left corner radius
    /// @param topRightRadius the top-right corner radius
    /// @param bottomRightRadius the bottom-right corner radius
    /// @param bottomLeftRadius the bottom-left corner radius
    void update(
            double width,
            double height,
            double topLeftRadius,
            double topRightRadius,
            double bottomRightRadius,
            double bottomLeftRadius
    ) {
        update(
                width,
                height,
                topLeftRadius,
                topLeftRadius,
                topRightRadius,
                topRightRadius,
                bottomRightRadius,
                bottomRightRadius,
                bottomLeftRadius,
                bottomLeftRadius
        );
    }

    /// Updates the rounded rectangle to the supplied bounds and elliptical corner radii.
    ///
    /// @param width the shape width
    /// @param height the shape height
    /// @param topLeftHorizontalRadius the top-left horizontal radius
    /// @param topLeftVerticalRadius the top-left vertical radius
    /// @param topRightHorizontalRadius the top-right horizontal radius
    /// @param topRightVerticalRadius the top-right vertical radius
    /// @param bottomRightHorizontalRadius the bottom-right horizontal radius
    /// @param bottomRightVerticalRadius the bottom-right vertical radius
    /// @param bottomLeftHorizontalRadius the bottom-left horizontal radius
    /// @param bottomLeftVerticalRadius the bottom-left vertical radius
    void update(
            double width,
            double height,
            double topLeftHorizontalRadius,
            double topLeftVerticalRadius,
            double topRightHorizontalRadius,
            double topRightVerticalRadius,
            double bottomRightHorizontalRadius,
            double bottomRightVerticalRadius,
            double bottomLeftHorizontalRadius,
            double bottomLeftVerticalRadius
    ) {
        double maximumHorizontalRadius = Math.max(0.0, width / 2.0);
        double maximumVerticalRadius = Math.max(0.0, height / 2.0);
        double topLeftHorizontal = boundedRadius(topLeftHorizontalRadius, maximumHorizontalRadius);
        double topLeftVertical = boundedRadius(topLeftVerticalRadius, maximumVerticalRadius);
        double topRightHorizontal = boundedRadius(topRightHorizontalRadius, maximumHorizontalRadius);
        double topRightVertical = boundedRadius(topRightVerticalRadius, maximumVerticalRadius);
        double bottomRightHorizontal = boundedRadius(bottomRightHorizontalRadius, maximumHorizontalRadius);
        double bottomRightVertical = boundedRadius(bottomRightVerticalRadius, maximumVerticalRadius);
        double bottomLeftHorizontal = boundedRadius(bottomLeftHorizontalRadius, maximumHorizontalRadius);
        double bottomLeftVertical = boundedRadius(bottomLeftVerticalRadius, maximumVerticalRadius);
        if (Double.compare(this.width, width) == 0
                && Double.compare(this.height, height) == 0
                && Double.compare(this.topLeftHorizontalRadius, topLeftHorizontal) == 0
                && Double.compare(this.topLeftVerticalRadius, topLeftVertical) == 0
                && Double.compare(this.topRightHorizontalRadius, topRightHorizontal) == 0
                && Double.compare(this.topRightVerticalRadius, topRightVertical) == 0
                && Double.compare(this.bottomRightHorizontalRadius, bottomRightHorizontal) == 0
                && Double.compare(this.bottomRightVerticalRadius, bottomRightVertical) == 0
                && Double.compare(this.bottomLeftHorizontalRadius, bottomLeftHorizontal) == 0
                && Double.compare(this.bottomLeftVerticalRadius, bottomLeftVertical) == 0) {
            return;
        }

        this.width = width;
        this.height = height;
        this.topLeftHorizontalRadius = topLeftHorizontal;
        this.topLeftVerticalRadius = topLeftVertical;
        this.topRightHorizontalRadius = topRightHorizontal;
        this.topRightVerticalRadius = topRightVertical;
        this.bottomRightHorizontalRadius = bottomRightHorizontal;
        this.bottomRightVerticalRadius = bottomRightVertical;
        this.bottomLeftHorizontalRadius = bottomLeftHorizontal;
        this.bottomLeftVerticalRadius = bottomLeftVertical;

        start.setX(topLeftHorizontal);
        start.setY(0.0);
        topEdge.setX(width - topRightHorizontal);
        topEdge.setY(0.0);
        updateCorner(
                topRightArc,
                topRightHorizontal,
                topRightVertical,
                width,
                topRightVertical
        );
        rightEdge.setX(width);
        rightEdge.setY(height - bottomRightVertical);
        updateCorner(
                bottomRightArc,
                bottomRightHorizontal,
                bottomRightVertical,
                width - bottomRightHorizontal,
                height
        );
        bottomEdge.setX(bottomLeftHorizontal);
        bottomEdge.setY(height);
        updateCorner(
                bottomLeftArc,
                bottomLeftHorizontal,
                bottomLeftVertical,
                0.0,
                height - bottomLeftVertical
        );
        leftEdge.setX(0.0);
        leftEdge.setY(topLeftVertical);
        updateCorner(
                topLeftArc,
                topLeftHorizontal,
                topLeftVertical,
                topLeftHorizontal,
                0.0
        );
    }

    /// Clamps a requested radius to the current geometric limit.
    ///
    /// @param radius the requested radius
    /// @param maximumRadius the largest supported radius
    /// @return the finite non-negative radius used by the path
    private static double boundedRadius(double radius, double maximumRadius) {
        if (!Double.isFinite(radius)) {
            return radius > 0.0 ? maximumRadius : 0.0;
        }
        return Math.min(maximumRadius, Math.max(0.0, radius));
    }

    /// Updates one corner without replacing path elements.
    ///
    /// @param arc the retained arc element
    /// @param radiusX the resolved horizontal radius
    /// @param radiusY the resolved vertical radius
    /// @param x the arc endpoint x-coordinate
    /// @param y the arc endpoint y-coordinate
    private static void updateCorner(ArcTo arc, double radiusX, double radiusY, double x, double y) {
        arc.setRadiusX(radiusX);
        arc.setRadiusY(radiusY);
        arc.setX(x);
        arc.setY(y);
        arc.setSweepFlag(true);
    }
}
