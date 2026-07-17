// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents a dialog scrim over the client area of an owner scene.
///
/// The scrim lives in a popup owned by the application window so a JavaFX dialog can remain a separate window
/// without replacing or reparenting the application scene root. The popup follows owner geometry and copies the
/// dialog's theme, orientation, stylesheet, and motion context while it is active.
@NotNullByDefault
public final class M3DialogScrimPresenter {
    /// The fallback opacity used when no Material theme is installed on the dialog.
    private static final double FALLBACK_SCRIM_OPACITY = 0.32;

    /// The popup window placed between the owner window and the dialog window.
    private final Popup popup = new Popup();

    /// The Material scrim that fills the popup.
    private final M3Scrim scrim = new M3Scrim();

    /// The transparent popup root that carries copied Material theme context.
    private final StackPane popupRoot = new StackPane(scrim);

    /// Updates the popup bounds after owner window or scene geometry changes.
    private final ChangeListener<Number> geometryListener =
            (observable, oldValue, newValue) -> updateGeometry();

    /// The owner window currently covered by the popup.
    private @Nullable Window ownerWindow;

    /// The owner scene currently covered by the popup.
    private @Nullable Scene ownerScene;

    /// The dialog pane whose detached Material context is copied into the scrim.
    private @Nullable Parent themeRoot;

    /// Synchronizes detached popup styling and motion settings while the scrim is active.
    private @Nullable M3PopupContextSynchronizer contextSynchronizer;

    /// Creates a hidden presenter whose scrim cannot dismiss the dialog independently.
    public M3DialogScrimPresenter() {
        popup.setAutoFix(false);
        popup.setAutoHide(false);
        popup.setHideOnEscape(false);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_LEFT);

        scrim.setDismissOnClick(false);
        scrim.setFocusTraversable(false);
        scrim.hide();
        popupRoot.setStyle("-fx-background-color: transparent;");
        popup.getContent().add(popupRoot);
    }

    /// Shows the scrim over an owner scene and starts observing its presentation context.
    ///
    /// A request for a detached scene or a hidden owner window is ignored. Repeating the request for the active
    /// scene refreshes styling and reverses an in-progress exit transition.
    ///
    /// @param popupOwner   a node in the owner scene used to establish popup ownership
    /// @param contextOwner the dialog node that supplies orientation and reduced-motion context
    /// @param scene        the owner scene whose client area is covered
    /// @param themeRoot    the dialog root whose stylesheets and local Material theme are copied
    public void show(Node popupOwner, Node contextOwner, Scene scene, Parent themeRoot) {
        Objects.requireNonNull(popupOwner, "popupOwner");
        Objects.requireNonNull(contextOwner, "contextOwner");
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(themeRoot, "themeRoot");

        @Nullable Window window = scene.getWindow();
        if (popupOwner.getScene() != scene || window == null || !window.isShowing()) {
            return;
        }
        if (popup.isShowing() && ownerScene == scene) {
            this.themeRoot = themeRoot;
            sync();
            if (!scrim.isShown()) {
                scrim.setOpacity(0.0);
            }
            scrim.show();
            popupRoot.layout();
            return;
        }

        dispose();
        ownerWindow = window;
        ownerScene = scene;
        this.themeRoot = themeRoot;
        contextSynchronizer = new M3PopupContextSynchronizer(
                contextOwner,
                popupRoot,
                themeRoot::getStylesheets,
                () -> themeRoot
        );
        installGeometryListeners(window, scene);
        contextSynchronizer.start();
        syncScrimOpacity();
        updateGeometry();

        boolean shown = false;
        try {
            shown = M3PopupWindows.show(popup, popupOwner, popupX(window, scene), popupY(window, scene));
            if (shown) {
                scrim.setOpacity(0.0);
                scrim.show();
                popupRoot.layout();
            }
        } finally {
            if (!shown) {
                dispose();
            }
        }
    }

    /// Synchronizes the current theme, motion context, opacity token, and owner geometry.
    public void sync() {
        @Nullable M3PopupContextSynchronizer synchronizer = contextSynchronizer;
        if (synchronizer != null) {
            synchronizer.sync();
        }
        syncScrimOpacity();
        updateGeometry();
    }

    /// Starts the scrim exit transition while retaining the popup until the dialog closes.
    public void hide() {
        if (popup.isShowing()) {
            scrim.hide();
        }
    }

    /// Restores the scrim after a dialog close request is cancelled.
    public void restore() {
        if (popup.isShowing()) {
            scrim.show();
        }
    }

    /// Hides the popup and releases all owner and context listeners.
    public void dispose() {
        @Nullable M3PopupContextSynchronizer synchronizer = contextSynchronizer;
        contextSynchronizer = null;
        if (synchronizer != null) {
            synchronizer.stop();
        }

        @Nullable Window window = ownerWindow;
        @Nullable Scene scene = ownerScene;
        ownerWindow = null;
        ownerScene = null;
        themeRoot = null;
        if (window != null && scene != null) {
            removeGeometryListeners(window, scene);
        }

        popup.hide();
        scrim.hide();
    }

    /// Installs listeners for all coordinates and dimensions used by the popup bounds.
    private void installGeometryListeners(Window window, Scene scene) {
        window.xProperty().addListener(geometryListener);
        window.yProperty().addListener(geometryListener);
        scene.xProperty().addListener(geometryListener);
        scene.yProperty().addListener(geometryListener);
        scene.widthProperty().addListener(geometryListener);
        scene.heightProperty().addListener(geometryListener);
    }

    /// Removes listeners installed by [installGeometryListeners(Window, Scene)].
    private void removeGeometryListeners(Window window, Scene scene) {
        window.xProperty().removeListener(geometryListener);
        window.yProperty().removeListener(geometryListener);
        scene.xProperty().removeListener(geometryListener);
        scene.yProperty().removeListener(geometryListener);
        scene.widthProperty().removeListener(geometryListener);
        scene.heightProperty().removeListener(geometryListener);
    }

    /// Updates the scrim size and popup position from the active owner scene.
    private void updateGeometry() {
        @Nullable Window window = ownerWindow;
        @Nullable Scene scene = ownerScene;
        if (window == null || scene == null) {
            return;
        }

        double width = Math.max(0.0, scene.getWidth());
        double height = Math.max(0.0, scene.getHeight());
        popupRoot.setMinSize(width, height);
        popupRoot.setPrefSize(width, height);
        popupRoot.setMaxSize(width, height);
        popupRoot.resize(width, height);
        popupRoot.layout();
        if (popup.isShowing()) {
            popup.setX(popupX(window, scene));
            popup.setY(popupY(window, scene));
        }
    }

    /// Applies the active theme's scrim opacity token to the private dialog scrim.
    private void syncScrimOpacity() {
        @Nullable Parent root = themeRoot;
        @Nullable M3Theme theme = root == null ? null : M3ThemeMetadata.getTheme(root);
        scrim.setVisibleOpacity(theme == null
                ? FALLBACK_SCRIM_OPACITY
                : theme.tokens().componentTokens().scrim().containerOpacity());
    }

    /// Returns the popup's screen x coordinate for an owner scene.
    private static double popupX(Window window, Scene scene) {
        return window.getX() + scene.getX();
    }

    /// Returns the popup's screen y coordinate for an owner scene.
    private static double popupY(Window window, Scene scene) {
        return window.getY() + scene.getY();
    }
}
