// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.SVGPath;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3DisclosureIcon].
@NotNullByDefault
public final class M3DisclosureIconSkin extends SkinBase<M3DisclosureIcon> {
    /// The reference viewport used by the disclosure triangle path.
    private static final double REFERENCE_ICON_SIZE = 24.0;

    /// The collapsed arrow rotation in degrees.
    private static final double COLLAPSED_ROTATION = -90.0;

    /// The expanded arrow rotation in degrees.
    private static final double EXPANDED_ROTATION = 0.0;

    /// The collapsed right-to-left arrow rotation in degrees.
    private static final double RIGHT_TO_LEFT_COLLAPSED_ROTATION = 90.0;

    /// The disclosure triangle path in a 24 by 24 icon box.
    private static final String TRIANGLE_PATH = "M 7 9 L 17 9 L 12 15 Z";

    /// The disclosure triangle node.
    private final SVGPath arrow = new SVGPath();

    /// The rotation animation played when expanded state changes.
    private final M3DoubleTransition rotationAnimation = new M3DoubleTransition(arrow.rotateProperty());

    /// Settles running rotation transitions when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    () -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), rotationAnimation)
            );

    /// Applies expanded-state changes to the arrow rotation.
    private final ChangeListener<Boolean> expandedListener =
            (observable, oldValue, newValue) -> animateExpandedState(newValue);

    /// Applies node-orientation changes to collapsed arrow direction.
    private final InvalidationListener nodeOrientationInvalidation =
            observable -> animateExpandedState(getSkinnable().isExpanded());

    /// Creates a disclosure icon skin.
    ///
    /// @param control the disclosure icon controlled by this skin
    public M3DisclosureIconSkin(M3DisclosureIcon control) {
        super(control);
        arrow.setContent(TRIANGLE_PATH);
        arrow.getStyleClass().add("m3-disclosure-icon-shape");
        arrow.setManaged(false);
        arrow.setMouseTransparent(true);
        arrow.setRotate(rotationFor(control.isExpanded(), isRightToLeft()));
        getChildren().setAll(arrow);
        control.expandedProperty().addListener(expandedListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
    }

    /// Removes listeners and animations before disposal.
    @Override
    public void dispose() {
        rotationAnimation.stop();
        motionSettingsObserver.dispose();
        getSkinnable().expandedProperty().removeListener(expandedListener);
        getSkinnable().effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        getChildren().remove(arrow);
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
        return leftInset + getSkinnable().getIconSize() + rightInset;
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
        return topInset + getSkinnable().getIconSize() + bottomInset;
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
        return leftInset + getSkinnable().getIconSize() + rightInset;
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
        return topInset + getSkinnable().getIconSize() + bottomInset;
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
        double iconSize = getSkinnable().getIconSize();
        double scale = iconSize / REFERENCE_ICON_SIZE;
        arrow.setScaleX(scale);
        arrow.setScaleY(scale);
        arrow.setLayoutX(x + (width - REFERENCE_ICON_SIZE) / 2.0);
        arrow.setLayoutY(y + (height - REFERENCE_ICON_SIZE) / 2.0);
    }

    /// Animates the arrow rotation to match the expanded state.
    private void animateExpandedState(boolean expanded) {
        rotationAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        rotationAnimation.configure(spec, rotationFor(expanded, isRightToLeft()));
        M3Animation.playFromStart(getSkinnable(), rotationAnimation);
    }

    /// Returns the arrow rotation for an expanded state.
    private static double rotationFor(boolean expanded, boolean rightToLeft) {
        if (expanded) {
            return EXPANDED_ROTATION;
        }
        return rightToLeft ? RIGHT_TO_LEFT_COLLAPSED_ROTATION : COLLAPSED_ROTATION;
    }

    /// Returns whether the icon is rendered in right-to-left orientation.
    private boolean isRightToLeft() {
        return M3NodeLayout.isRightToLeft(getSkinnable());
    }
}
