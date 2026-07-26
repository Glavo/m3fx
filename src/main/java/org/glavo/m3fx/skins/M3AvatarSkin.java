// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Avatar;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Avatar].
@NotNullByDefault
public final class M3AvatarSkin extends SkinBase<M3Avatar> {
    /// The internal text-label style class.
    private static final String LABEL_STYLE_CLASS = "m3-avatar-label";

    /// The internal content container.
    private final StackPane container = new StackPane();

    /// The label used when the avatar has no graphic node.
    private final Label textLabel = new Label();

    /// Updates displayed content when the graphic node changes.
    private final InvalidationListener graphicInvalidation = observable -> updateContent();

    /// Creates an avatar skin.
    ///
    /// @param control the avatar controlled by this skin
    public M3AvatarSkin(M3Avatar control) {
        super(control);
        container.setManaged(false);
        textLabel.getStyleClass().add(LABEL_STYLE_CLASS);
        textLabel.textProperty().bind(control.textProperty());
        getChildren().setAll(container);
        control.graphicProperty().addListener(graphicInvalidation);
        updateContent();
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        getSkinnable().graphicProperty().removeListener(graphicInvalidation);
        textLabel.textProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
        super.dispose();
    }

    /// Computes the minimum width from the avatar size token.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getContainerSize() + rightInset;
    }

    /// Computes the minimum height from the avatar size token.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getContainerSize() + bottomInset;
    }

    /// Computes the preferred width from the avatar size token.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getContainerSize() + rightInset;
    }

    /// Computes the preferred height from the avatar size token.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getContainerSize() + bottomInset;
    }

    /// Computes the maximum width from the avatar size token.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + getSkinnable().getContainerSize() + rightInset;
    }

    /// Computes the maximum height from the avatar size token.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + getSkinnable().getContainerSize() + bottomInset;
    }

    /// Lays out avatar content in the control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Shows the graphic node when one is present, otherwise shows the bound text label.
    private void updateContent() {
        @Nullable Node graphic = getSkinnable().getGraphic();
        if (graphic == null) {
            container.getChildren().setAll(textLabel);
        } else {
            container.getChildren().setAll(graphic);
        }
        getSkinnable().requestLayout();
    }
}
