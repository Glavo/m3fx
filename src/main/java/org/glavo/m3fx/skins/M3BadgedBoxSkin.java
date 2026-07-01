// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BadgedBox;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3BadgedBox].
@NotNullByDefault
public final class M3BadgedBoxSkin extends SkinBase<M3BadgedBox> {
    /// Updates children after content or badge changes.
    private final InvalidationListener childrenInvalidation = observable -> updateChildren();

    /// Updates badge placement after alignment or offset changes.
    private final InvalidationListener badgePlacementInvalidation = observable -> updateBadgePlacement();

    /// Creates a badged box skin.
    ///
    /// @param control the badged box controlled by this skin
    public M3BadgedBoxSkin(M3BadgedBox control) {
        super(control);
        control.contentProperty().addListener(childrenInvalidation);
        control.badgeProperty().addListener(childrenInvalidation);
        control.badgeAlignmentProperty().addListener(badgePlacementInvalidation);
        control.badgeOffsetXProperty().addListener(badgePlacementInvalidation);
        control.badgeOffsetYProperty().addListener(badgePlacementInvalidation);
        control.effectiveNodeOrientationProperty().addListener(badgePlacementInvalidation);
        updateChildren();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3BadgedBox control = getSkinnable();
        control.contentProperty().removeListener(childrenInvalidation);
        control.badgeProperty().removeListener(childrenInvalidation);
        control.badgeAlignmentProperty().removeListener(badgePlacementInvalidation);
        control.badgeOffsetXProperty().removeListener(badgePlacementInvalidation);
        control.badgeOffsetYProperty().removeListener(badgePlacementInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(badgePlacementInvalidation);
        getChildren().clear();
        super.dispose();
    }

    /// Computes preferred width from the current overlay content.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double contentWidth = prefWidth(getSkinnable().getContent(), height);
        double badgeWidth = prefWidth(getSkinnable().getBadge(), height);
        return leftInset + Math.max(contentWidth, badgeWidth) + rightInset;
    }

    /// Computes preferred height from the current overlay content.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double contentHeight = prefHeight(getSkinnable().getContent(), width);
        double badgeHeight = prefHeight(getSkinnable().getBadge(), width);
        return topInset + Math.max(contentHeight, badgeHeight) + bottomInset;
    }

    /// Lays out content centered and the badge at the resolved badge anchor.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double anchorX = x;
        double anchorY = y;
        double anchorWidth = width;
        double anchorHeight = height;

        @Nullable Node contentNode = getSkinnable().getContent();
        if (contentNode != null && getChildren().contains(contentNode)) {
            anchorWidth = snapSizeX(Math.min(width, contentNode.prefWidth(-1.0)));
            anchorHeight = snapSizeY(Math.min(height, contentNode.prefHeight(anchorWidth)));
            anchorX = x + (width - anchorWidth) / 2.0;
            anchorY = y + (height - anchorHeight) / 2.0;
            layoutInArea(contentNode, anchorX, anchorY, anchorWidth, anchorHeight, 0.0, HPos.CENTER, VPos.CENTER);
        }

        @Nullable M3Badge badgeNode = getSkinnable().getBadge();
        if (badgeNode != null && getChildren().contains(badgeNode)) {
            layoutBadge(badgeNode, anchorX, anchorY, anchorWidth, anchorHeight);
        }
    }

    /// Updates the overlay children.
    private void updateChildren() {
        @Nullable Node contentNode = getSkinnable().getContent();
        @Nullable M3Badge badgeNode = getSkinnable().getBadge();
        if (contentNode == null && badgeNode == null) {
            getChildren().clear();
        } else if (contentNode == null) {
            getChildren().setAll(badgeNode);
        } else if (badgeNode == null) {
            getChildren().setAll(contentNode);
        } else {
            getChildren().setAll(contentNode, badgeNode);
        }
        updateBadgePlacement();
        getSkinnable().requestLayout();
    }

    /// Applies alignment and offset to the current badge node.
    private void updateBadgePlacement() {
        @Nullable M3Badge badgeNode = getSkinnable().getBadge();
        if (badgeNode == null) {
            return;
        }

        badgeNode.setTranslateX(getSkinnable().getBadgeOffsetX());
        badgeNode.setTranslateY(getSkinnable().getBadgeOffsetY());
        getSkinnable().requestLayout();
    }

    /// Lays out one badge at its preferred size inside the badged box.
    private void layoutBadge(M3Badge badgeNode, double x, double y, double width, double height) {
        double badgeWidth = snapSizeX(badgeNode.prefWidth(-1.0));
        double badgeHeight = snapSizeY(badgeNode.prefHeight(badgeWidth));
        Pos alignment = M3NodeLayout.logicalAlignment(getSkinnable(), getSkinnable().getBadgeAlignment());
        double badgeX = x + alignedX(alignment.getHpos(), width, badgeWidth);
        double badgeY = y + alignedY(alignment.getVpos(), height, badgeHeight);

        badgeNode.resizeRelocate(
                snapPositionX(badgeX),
                snapPositionY(badgeY),
                badgeWidth,
                badgeHeight
        );
    }

    /// Returns the preferred width of a node, or zero when it is absent.
    private static double prefWidth(@Nullable Node node, double height) {
        return node == null ? 0.0 : node.prefWidth(height);
    }

    /// Returns the preferred height of a node, or zero when it is absent.
    private static double prefHeight(@Nullable Node node, double width) {
        return node == null ? 0.0 : node.prefHeight(width);
    }

    /// Resolves horizontal alignment inside the available width.
    private static double alignedX(HPos alignment, double width, double childWidth) {
        return switch (alignment) {
            case LEFT -> 0.0;
            case RIGHT -> width - childWidth;
            case CENTER -> (width - childWidth) / 2.0;
        };
    }

    /// Resolves vertical alignment inside the available height.
    private static double alignedY(VPos alignment, double height, double childHeight) {
        return switch (alignment) {
            case TOP -> 0.0;
            case BOTTOM -> height - childHeight;
            case BASELINE, CENTER -> (height - childHeight) / 2.0;
        };
    }

}
