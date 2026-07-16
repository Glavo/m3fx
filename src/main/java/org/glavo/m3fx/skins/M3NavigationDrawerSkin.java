// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NavigationDrawer].
@NotNullByDefault
public final class M3NavigationDrawerSkin extends M3ItemContainerSkinBase<M3NavigationDrawer, VBox, Node> {
    /// The vertically scrollable viewport containing the drawer destinations.
    private final ScrollPane viewport = new ScrollPane();

    /// Creates a navigation drawer skin.
    ///
    /// @param control the skinned navigation drawer
    public M3NavigationDrawerSkin(M3NavigationDrawer control) {
        super(control, control.getItems(), new VBox());
        VBox container = getContainer();
        container.setManaged(true);
        container.spacingProperty().bind(control.itemSpacingProperty());

        getChildren().clear();
        viewport.setContent(container);
        viewport.setFitToWidth(true);
        viewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewport.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewport.setPannable(true);
        viewport.setFocusTraversable(false);
        viewport.getStyleClass().add("m3-navigation-drawer-viewport");
        viewport.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        M3ScrollPanes.style(viewport);
        M3ScrollPanes.enableSmoothScrolling(viewport);
        getChildren().setAll(viewport);
    }

    /// Removes viewport behavior, bindings, and content references before disposal.
    @Override
    public void dispose() {
        M3ScrollPanes.disableSmoothScrolling(viewport);
        viewport.nodeOrientationProperty().unbind();
        viewport.setContent(null);
        getChildren().remove(viewport);
        getContainer().spacingProperty().unbind();
        super.dispose();
    }

    /// Allows the drawer to shrink below its content height so excess destinations can scroll.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + bottomInset;
    }

    /// Lays out the scroll viewport in the drawer content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        viewport.resizeRelocate(x, y, width, height);
    }
}
