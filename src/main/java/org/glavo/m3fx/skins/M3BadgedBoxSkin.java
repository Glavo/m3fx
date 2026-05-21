// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BadgedBox;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3BadgedBox].
@NotNullByDefault
public final class M3BadgedBoxSkin extends SkinBase<M3BadgedBox> {
    /// The overlay container that positions content and badge nodes.
    private final StackPane container = new StackPane();

    /// Updates children after content or badge changes.
    private final InvalidationListener childrenInvalidation = observable -> updateChildren();

    /// Updates badge placement after alignment or offset changes.
    private final InvalidationListener badgePlacementInvalidation = observable -> updateBadgePlacement();

    /// Creates a badged box skin.
    ///
    /// @param control the badged box controlled by this skin
    public M3BadgedBoxSkin(M3BadgedBox control) {
        super(control);
        container.setManaged(false);
        getChildren().add(container);

        control.contentProperty().addListener(childrenInvalidation);
        control.badgeProperty().addListener(childrenInvalidation);
        control.badgeAlignmentProperty().addListener(badgePlacementInvalidation);
        control.badgeOffsetXProperty().addListener(badgePlacementInvalidation);
        control.badgeOffsetYProperty().addListener(badgePlacementInvalidation);
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
        container.getChildren().clear();
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
        return leftInset + container.prefWidth(height) + rightInset;
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
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Lays out the overlay container inside the control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Updates the overlay children.
    private void updateChildren() {
        @Nullable Node contentNode = getSkinnable().getContent();
        @Nullable M3Badge badgeNode = getSkinnable().getBadge();
        if (contentNode == null && badgeNode == null) {
            container.getChildren().clear();
        } else if (contentNode == null) {
            container.getChildren().setAll(badgeNode);
        } else if (badgeNode == null) {
            container.getChildren().setAll(contentNode);
        } else {
            container.getChildren().setAll(contentNode, badgeNode);
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

        StackPane.setAlignment(badgeNode, getSkinnable().getBadgeAlignment());
        badgeNode.setTranslateX(getSkinnable().getBadgeOffsetX());
        badgeNode.setTranslateY(getSkinnable().getBadgeOffsetY());
        getSkinnable().requestLayout();
    }
}
