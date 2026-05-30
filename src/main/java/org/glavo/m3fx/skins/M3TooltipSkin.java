// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Tooltip].
@NotNullByDefault
public final class M3TooltipSkin implements Skin<M3Tooltip> {
    /// The tooltip rendered by this skin.
    private final M3Tooltip tooltip;

    /// The root label that renders tooltip text and graphic content.
    private final Label root = new Label();

    /// Keeps the root style classes aligned with the tooltip style classes.
    private final ListChangeListener<String> styleClassListener = change -> syncStyleClasses();

    /// Reinstalls generated theme CSS when the popup scene changes.
    private final ChangeListener<@Nullable Scene> sceneListener =
            (observable, oldValue, newValue) -> updateThemeStylesheet(oldValue, newValue);

    /// Reinstalls generated theme CSS when the tooltip theme changes.
    private final ChangeListener<@Nullable M3Theme> themeListener =
            (observable, oldValue, newValue) -> updateThemeStylesheet(root.getScene(), root.getScene());

    /// The generated theme stylesheet currently installed on the popup scene.
    private @Nullable String installedThemeStylesheet;

    /// Creates a tooltip skin.
    public M3TooltipSkin(M3Tooltip tooltip) {
        this.tooltip = tooltip;

        syncStyleClasses();
        root.getStylesheets().add(M3Stylesheets.controlStylesheet("tooltip.css"));
        tooltip.getStyleClass().addListener(styleClassListener);
        tooltip.themeProperty().addListener(themeListener);
        root.sceneProperty().addListener(sceneListener);
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
        updateThemeStylesheet(null, root.getScene());
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
        tooltip.themeProperty().removeListener(themeListener);
        root.sceneProperty().removeListener(sceneListener);
        updateThemeStylesheet(root.getScene(), null);
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

    /// Installs or removes the generated theme stylesheet used by popup content.
    private void updateThemeStylesheet(@Nullable Scene oldScene, @Nullable Scene newScene) {
        String currentStylesheet = installedThemeStylesheet;
        if (currentStylesheet != null && oldScene != null) {
            oldScene.getStylesheets().remove(currentStylesheet);
        }
        installedThemeStylesheet = null;

        @Nullable M3Theme theme = tooltip.getTheme();
        if (theme == null || newScene == null) {
            resizeShowingPopup();
            return;
        }

        String themeStylesheet = M3ThemeManager.themeStylesheetUrl(theme);
        if (!newScene.getStylesheets().contains(themeStylesheet)) {
            newScene.getStylesheets().add(themeStylesheet);
        }
        installedThemeStylesheet = themeStylesheet;
        resizeShowingPopup();
        Platform.runLater(this::resizeShowingPopup);
    }

    /// Resizes an already visible popup after theme CSS changes its preferred content size.
    private void resizeShowingPopup() {
        if (!tooltip.isShowing()) {
            return;
        }
        root.applyCss();
        root.autosize();
        tooltip.sizeToScene();
    }
}
