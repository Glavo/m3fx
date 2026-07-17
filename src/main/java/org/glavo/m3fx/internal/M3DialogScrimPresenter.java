// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents a dialog scrim over the client area of an owner scene.
///
/// The scrim is installed in a temporary overlay around the owner scene root. Keeping the scrim in the owner scene
/// guarantees that a JavaFX dialog's separate window remains above it on every platform, without relying on popup
/// or sibling-window ordering. The original scene root and its theme state are restored when presentation ends.
@NotNullByDefault
public final class M3DialogScrimPresenter {
    /// The fallback opacity used when no Material theme is installed on the dialog.
    private static final double FALLBACK_SCRIM_OPACITY = 0.32;

    /// The Material scrim that fills the owner scene.
    private final M3Scrim scrim = new M3Scrim();

    /// The transparent scrim layer that carries copied Material theme context.
    private final StackPane scrimRoot = new StackPane(scrim);

    /// The owner scene currently covered by the scrim.
    private @Nullable Scene ownerScene;

    /// The owner scene root retained for restoration after the dialog closes.
    private @Nullable Parent ownerRoot;

    /// The temporary scene root that stacks the original root below the scrim.
    private @Nullable StackPane overlayRoot;

    /// The dialog pane whose Material context is copied into the scrim layer.
    private @Nullable Parent themeRoot;

    /// Synchronizes scrim styling and motion settings while the scrim is active.
    private @Nullable M3PopupContextSynchronizer contextSynchronizer;

    /// Creates a hidden presenter whose scrim cannot dismiss the dialog independently.
    public M3DialogScrimPresenter() {
        scrim.setDismissOnClick(false);
        scrim.setFocusTraversable(false);
        scrim.hide();
        scrimRoot.setStyle("-fx-background-color: transparent;");
        scrimRoot.setPickOnBounds(true);
    }

    /// Shows the scrim over an owner scene and starts observing its presentation context.
    ///
    /// A request for a detached scene or a hidden owner window is ignored. Repeating the request for the active
    /// scene refreshes styling and reverses an in-progress exit transition.
    ///
    /// @param ownerNode    a node in the owner scene used to validate the owner context
    /// @param contextOwner the dialog node that supplies orientation and reduced-motion context
    /// @param scene        the owner scene whose client area is covered
    /// @param themeRoot    the dialog root whose stylesheets and local Material theme are copied
    public void show(Node ownerNode, Node contextOwner, Scene scene, Parent themeRoot) {
        Objects.requireNonNull(ownerNode, "ownerNode");
        Objects.requireNonNull(contextOwner, "contextOwner");
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(themeRoot, "themeRoot");

        @Nullable Window window = scene.getWindow();
        if (ownerNode.getScene() != scene || window == null || !window.isShowing()) {
            return;
        }
        @Nullable StackPane activeOverlay = overlayRoot;
        if (activeOverlay != null && scene.getRoot() == activeOverlay && ownerScene == scene) {
            this.themeRoot = themeRoot;
            sync();
            if (!scrim.isShown()) {
                scrim.setOpacity(0.0);
            }
            scrim.show();
            scrimRoot.layout();
            return;
        }

        dispose();
        ownerScene = scene;
        Parent currentRoot = scene.getRoot();
        ownerRoot = currentRoot;
        this.themeRoot = themeRoot;

        StackPane currentOverlay = new StackPane();
        currentOverlay.setStyle("-fx-background-color: transparent;");
        StackPane.setAlignment(currentRoot, Pos.TOP_LEFT);
        overlayRoot = currentOverlay;
        boolean installed = false;
        try {
            scene.setRoot(currentOverlay);
            currentOverlay.getChildren().setAll(currentRoot, scrimRoot);
            contextSynchronizer = new M3PopupContextSynchronizer(
                    contextOwner,
                    scrimRoot,
                    themeRoot::getStylesheets,
                    () -> themeRoot
            );
            contextSynchronizer.start();
            syncScrimOpacity();
            currentOverlay.applyCss();
            currentOverlay.layout();
            scrim.setOpacity(0.0);
            scrim.show();
            currentOverlay.layout();
            installed = true;
        } finally {
            if (!installed) {
                dispose();
            }
        }
    }

    /// Synchronizes the current theme, motion context, and opacity token.
    public void sync() {
        @Nullable M3PopupContextSynchronizer synchronizer = contextSynchronizer;
        if (synchronizer != null) {
            synchronizer.sync();
        }
        syncScrimOpacity();
    }

    /// Starts the scrim exit transition while retaining its owner-scene overlay until the dialog closes.
    public void hide() {
        if (ownerScene != null) {
            scrim.hide();
        }
    }

    /// Restores the scrim after a dialog close request is cancelled.
    public void restore() {
        if (ownerScene != null) {
            scrim.show();
        }
    }

    /// Removes the scrim overlay, restores the original scene root, and releases synchronized context.
    public void dispose() {
        @Nullable M3PopupContextSynchronizer synchronizer = contextSynchronizer;
        contextSynchronizer = null;
        if (synchronizer != null) {
            synchronizer.stop();
        }

        @Nullable Scene scene = ownerScene;
        @Nullable Parent root = ownerRoot;
        @Nullable StackPane overlay = overlayRoot;
        ownerScene = null;
        ownerRoot = null;
        overlayRoot = null;
        themeRoot = null;

        if (overlay != null) {
            overlay.getChildren().remove(scrimRoot);
            if (root != null) {
                overlay.getChildren().remove(root);
            }
        }
        if (scene != null && root != null && scene.getRoot() == overlay) {
            scene.setRoot(root);
        }
        scrim.hide();
    }

    /// Applies the active theme's scrim opacity token to the private dialog scrim.
    private void syncScrimOpacity() {
        @Nullable Parent root = themeRoot;
        @Nullable M3Theme theme = root == null ? null : M3ThemeMetadata.getTheme(root);
        scrim.setVisibleOpacity(theme == null
                ? FALLBACK_SCRIM_OPACITY
                : theme.tokens().componentTokens().scrim().containerOpacity());
    }
}
