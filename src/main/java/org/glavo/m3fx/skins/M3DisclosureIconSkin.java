// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.SVGPath;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3DisclosureIcon].
@NotNullByDefault
public final class M3DisclosureIconSkin extends SkinBase<M3DisclosureIcon> {
    /// The icon layout box size.
    private static final double ICON_SIZE = 24.0;

    /// The collapsed arrow rotation in degrees.
    private static final double COLLAPSED_ROTATION = -90.0;

    /// The expanded arrow rotation in degrees.
    private static final double EXPANDED_ROTATION = 0.0;

    /// The disclosure triangle path in a 24 by 24 icon box.
    private static final String TRIANGLE_PATH = "M 7 9 L 17 9 L 12 15 Z";

    /// The disclosure triangle node.
    private final SVGPath arrow = new SVGPath();

    /// The rotation animation played when expanded state changes.
    private final Timeline rotationAnimation = new Timeline();

    /// Applies expanded-state changes to the arrow rotation.
    private final ChangeListener<Boolean> expandedListener =
            (observable, oldValue, newValue) -> animateExpandedState(newValue);

    /// Creates a disclosure icon skin.
    public M3DisclosureIconSkin(M3DisclosureIcon control) {
        super(control);
        arrow.setContent(TRIANGLE_PATH);
        arrow.getStyleClass().add("m3-disclosure-icon-shape");
        arrow.setManaged(false);
        arrow.setMouseTransparent(true);
        arrow.setRotate(rotationFor(control.isExpanded()));
        getChildren().add(arrow);
        control.expandedProperty().addListener(expandedListener);
    }

    /// Removes listeners and animations before disposal.
    @Override
    public void dispose() {
        rotationAnimation.stop();
        getSkinnable().expandedProperty().removeListener(expandedListener);
        super.dispose();
    }

    /// Computes the minimum width of the icon box.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + ICON_SIZE + rightInset;
    }

    /// Computes the minimum height of the icon box.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + ICON_SIZE + bottomInset;
    }

    /// Computes the preferred width of the icon box.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + ICON_SIZE + rightInset;
    }

    /// Computes the preferred height of the icon box.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + ICON_SIZE + bottomInset;
    }

    /// Computes the maximum width of the icon box.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /// Computes the maximum height of the icon box.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /// Lays out the arrow inside the icon box.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double iconX = x + (width - ICON_SIZE) / 2.0;
        double iconY = y + (height - ICON_SIZE) / 2.0;
        arrow.setLayoutX(iconX);
        arrow.setLayoutY(iconY);
    }

    /// Animates the arrow rotation to match the expanded state.
    private void animateExpandedState(boolean expanded) {
        rotationAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        rotationAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(arrow.rotateProperty(), rotationFor(expanded), spec.interpolator())
        ));
        M3Animation.playFromStart(getSkinnable(), rotationAnimation);
    }

    /// Returns the arrow rotation for an expanded state.
    private static double rotationFor(boolean expanded) {
        return expanded ? EXPANDED_ROTATION : COLLAPSED_ROTATION;
    }
}
