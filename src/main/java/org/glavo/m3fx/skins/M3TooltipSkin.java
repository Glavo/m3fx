// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Tooltip].
@NotNullByDefault
public final class M3TooltipSkin implements Skin<M3Tooltip> {
    /// The tooltip rendered by this skin.
    private final M3Tooltip tooltip;

    /// The root label that renders tooltip text and graphic content.
    private final Label root = new Label();

    /// Keeps the root style classes aligned with the tooltip style classes.
    private final ListChangeListener<String> styleClassListener = change -> syncStyleClasses();

    /// Creates a tooltip skin.
    public M3TooltipSkin(M3Tooltip tooltip) {
        this.tooltip = tooltip;

        syncStyleClasses();
        root.getStylesheets().add(M3Stylesheets.controlStylesheet("tooltip.css"));
        tooltip.getStyleClass().addListener(styleClassListener);
        root.textProperty().bind(tooltip.textProperty());
        root.graphicProperty().bind(tooltip.graphicProperty());
        root.contentDisplayProperty().bind(tooltip.contentDisplayProperty());
        root.wrapTextProperty().bind(tooltip.wrapTextProperty());
        root.styleProperty().bind(tooltip.styleProperty());
        root.prefWidthProperty().bind(tooltip.prefWidthProperty());
        root.minWidthProperty().bind(tooltip.minWidthProperty());
        root.maxWidthProperty().bind(tooltip.maxWidthProperty());
        root.prefHeightProperty().bind(tooltip.prefHeightProperty());
        root.minHeightProperty().bind(tooltip.minHeightProperty());
        root.maxHeightProperty().bind(tooltip.maxHeightProperty());
    }

    /// Returns the tooltip rendered by this skin.
    @Override
    public M3Tooltip getSkinnable() {
        return tooltip;
    }

    /// Returns the root node rendered inside the popup scene.
    @Override
    public Node getNode() {
        return root;
    }

    /// Releases bindings and listeners installed by this skin.
    @Override
    public void dispose() {
        tooltip.getStyleClass().removeListener(styleClassListener);
        root.textProperty().unbind();
        root.graphicProperty().unbind();
        root.contentDisplayProperty().unbind();
        root.wrapTextProperty().unbind();
        root.styleProperty().unbind();
        root.prefWidthProperty().unbind();
        root.minWidthProperty().unbind();
        root.maxWidthProperty().unbind();
        root.prefHeightProperty().unbind();
        root.minHeightProperty().unbind();
        root.maxHeightProperty().unbind();
        root.setGraphic(null);
    }

    /// Copies current tooltip style classes onto the rendered root node.
    private void syncStyleClasses() {
        root.getStyleClass().setAll(tooltip.getStyleClass());
    }
}
