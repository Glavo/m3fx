// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents one Material dialog as an in-scene overlay above its owner content.
///
/// The presenter never creates a native window. A scene receives one temporary host while at least one dialog is
/// showing, and each dialog contributes a layer containing its scrim and pane. This preserves native window chrome
/// interaction, provides deterministic stacking for nested dialogs, and lets the final layer restore the exact
/// scene root that preceded presentation.
@NotNullByDefault
public final class M3DialogPresenter {
    /// The scene property key that stores its active dialog host.
    private static final String HOST_PROPERTY_KEY = M3DialogPresenter.class.getName() + ".host";

    /// The fallback scrim opacity used without an installed Material theme.
    private static final double FALLBACK_SCRIM_OPACITY = 0.32;

    /// The minimum logical inset between a dialog and the owner scene edge.
    private static final double DIALOG_EDGE_INSET = 24.0;

    /// The Material scrim placed immediately below this dialog pane.
    private final M3Scrim scrim = new M3Scrim();

    /// The retained overlay layer containing the scrim and dialog pane.
    private final DialogLayer layer;

    /// The host currently containing this presenter's layer.
    private @Nullable DialogHost host;

    /// The focus owner captured before this layer became active.
    private @Nullable Node restoreFocusOwner;

    /// Creates a detached presenter for one dialog pane.
    ///
    /// @param pane the pane rendered above this presenter's scrim
    /// @throws NullPointerException if `pane` is `null`
    public M3DialogPresenter(M3DialogPane pane) {
        M3DialogPane nonNullPane = Objects.requireNonNull(pane, "pane");
        scrim.setDismissOnClick(false);
        scrim.setFocusTraversable(false);
        scrim.hide();
        layer = new DialogLayer(scrim, nonNullPane);
    }

    /// Installs this dialog layer over the attached, visible scene containing an owner node.
    ///
    /// @param owner the node whose scene receives the dialog overlay
    /// @throws IllegalStateException if the owner is detached or its window is not showing
    /// @throws NullPointerException if `owner` is `null`
    public void show(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Scene scene = owner.getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        if (scene == null || window == null || !window.isShowing()) {
            throw new IllegalStateException("dialog owner must be attached to a showing window");
        }
        if (host != null) {
            throw new IllegalStateException("dialog is already presented");
        }

        restoreFocusOwner = scene.getFocusOwner();
        DialogHost currentHost = DialogHost.acquire(scene);
        host = currentHost;
        boolean completed = false;
        try {
            layer.attach();
            currentHost.add(layer);
            sync();
            layer.applyCss();
            layer.layout();
            scrim.setOpacity(0.0);
            scrim.show();
            layer.requestLayout();
            completed = true;
        } finally {
            if (!completed) {
                host = null;
                currentHost.release(layer);
                layer.detach();
            }
        }
    }

    /// Synchronizes the scrim opacity from the theme already installed on this presenter's context root.
    public void sync() {
        @Nullable M3Theme theme = M3ThemeMetadata.getTheme(layer);
        scrim.setVisibleOpacity(theme == null
                ? FALLBACK_SCRIM_OPACITY
                : theme.tokens().componentTokens().scrim().containerOpacity());
    }

    /// Returns the transparent root that receives this dialog's inherited or explicit theme context.
    ///
    /// @return the retained context root
    public Parent contextRoot() {
        return layer;
    }

    /// Starts the scrim exit transition while retaining the layer for the pane exit transition.
    public void hideScrim() {
        if (host != null) {
            scrim.hide();
        }
    }

    /// Removes the layer, restores the original scene root when appropriate, and restores prior focus.
    public void dispose() {
        @Nullable DialogHost currentHost = host;
        @Nullable Node focusOwner = restoreFocusOwner;
        host = null;
        restoreFocusOwner = null;
        scrim.hide();
        if (currentHost == null) {
            return;
        }

        Scene scene = currentHost.scene();
        boolean removedTopLayer = currentHost.isTopLayer(layer);
        if (!currentHost.release(layer)) {
            layer.detach();
            return;
        }
        layer.detach();
        if (!removedTopLayer) {
            return;
        }

        @Nullable DialogLayer expectedTopLayer = currentHost.topLayer();
        @Nullable Node focusTarget = expectedTopLayer == null
                ? currentHost.initialFocusOwner()
                : focusOwner;
        if (focusTarget != null) {
            Platform.runLater(() -> {
                if (currentHost.canRestoreFocus(expectedTopLayer)
                        && focusTarget.getScene() == scene
                        && focusTarget.isVisible()
                        && !focusTarget.isDisabled()) {
                    focusTarget.requestFocus();
                }
            });
        }
    }

    /// Returns whether this presenter currently owns an installed dialog layer.
    ///
    /// @return `true` while the layer belongs to a scene host
    public boolean isShowing() {
        return host != null;
    }

    /// A scene-wide host that retains original content below all active dialog layers.
    @NotNullByDefault
    private static final class DialogHost {
        /// The scene whose root is temporarily wrapped.
        private final Scene scene;

        /// The exact scene root retained for restoration.
        private final Parent contentRoot;

        /// The alignment constraint present on the retained root before it entered the temporary stack.
        private final @Nullable Pos contentAlignment;

        /// The scene focus owner captured before the first dialog layer was installed.
        private final @Nullable Node initialFocusOwner;

        /// The transparent root stacking content and dialog layers.
        private final StackPane overlayRoot = new StackPane();

        /// The number of active dialog layers above the retained content root.
        private int layerCount;

        /// Creates and installs a host around the scene's current root.
        private DialogHost(Scene scene) {
            this.scene = scene;
            contentRoot = scene.getRoot();
            contentAlignment = StackPane.getAlignment(contentRoot);
            initialFocusOwner = scene.getFocusOwner();
            StackPane.setAlignment(contentRoot, Pos.TOP_LEFT);
            scene.setRoot(overlayRoot);
            overlayRoot.getChildren().add(contentRoot);
        }

        /// Returns the active host for a scene, installing one when this is its first dialog.
        private static DialogHost acquire(Scene scene) {
            Object value = scene.getProperties().get(HOST_PROPERTY_KEY);
            if (value instanceof DialogHost existing && scene.getRoot() == existing.overlayRoot) {
                return existing;
            }

            DialogHost host = new DialogHost(scene);
            scene.getProperties().put(HOST_PROPERTY_KEY, host);
            return host;
        }

        /// Adds one dialog layer above all existing scene content.
        private void add(DialogLayer layer) {
            overlayRoot.getChildren().add(Objects.requireNonNull(layer, "layer"));
            layerCount++;
        }

        /// Removes one layer and restores the original scene root after the final dialog closes.
        ///
        /// @return `true` when the layer belonged to this host
        private boolean release(DialogLayer layer) {
            if (!overlayRoot.getChildren().remove(layer)) {
                return false;
            }
            layerCount--;
            if (layerCount > 0) {
                return true;
            }

            overlayRoot.getChildren().remove(contentRoot);
            StackPane.setAlignment(contentRoot, contentAlignment);
            if (scene.getRoot() == overlayRoot) {
                scene.setRoot(contentRoot);
            }
            if (scene.getProperties().get(HOST_PROPERTY_KEY) == this) {
                scene.getProperties().remove(HOST_PROPERTY_KEY);
            }
            return true;
        }

        /// Returns whether a layer is currently the uppermost dialog in this host.
        private boolean isTopLayer(DialogLayer layer) {
            return topLayer() == layer;
        }

        /// Returns the uppermost active dialog layer.
        private @Nullable DialogLayer topLayer() {
            if (layerCount <= 0 || overlayRoot.getChildren().isEmpty()) {
                return null;
            }
            Node child = overlayRoot.getChildren().get(overlayRoot.getChildren().size() - 1);
            return child instanceof DialogLayer dialogLayer ? dialogLayer : null;
        }

        /// Returns the focus owner captured before this host replaced the scene root.
        private @Nullable Node initialFocusOwner() {
            return initialFocusOwner;
        }

        /// Returns whether a deferred focus restoration still targets the active host state.
        private boolean canRestoreFocus(@Nullable DialogLayer expectedTopLayer) {
            if (expectedTopLayer == null) {
                return layerCount == 0 && scene.getRoot() == contentRoot;
            }
            return scene.getProperties().get(HOST_PROPERTY_KEY) == this
                    && scene.getRoot() == overlayRoot
                    && topLayer() == expectedTopLayer;
        }

        /// Returns the scene owned by this host.
        private Scene scene() {
            return scene;
        }
    }

    /// A lightweight layer that fills its scene and centers one bounded dialog pane above a full scrim.
    @NotNullByDefault
    private static final class DialogLayer extends Pane {
        /// The scrim resized to this layer's complete bounds.
        private final M3Scrim scrim;

        /// The dialog pane centered inside the scene edge inset.
        private final M3DialogPane pane;

        /// Creates a transparent layer retaining its scrim below its pane.
        private DialogLayer(M3Scrim scrim, M3DialogPane pane) {
            this.scrim = scrim;
            this.pane = pane;
            setPickOnBounds(true);
            setStyle("-fx-background-color: transparent;");
        }

        /// Attaches the retained scrim and pane for one presentation.
        private void attach() {
            if (!getChildren().isEmpty()) {
                throw new IllegalStateException("dialog layer is already attached");
            }
            getChildren().setAll(scrim, pane);
        }

        /// Detaches presentation nodes so the pane has no parent while its dialog is hidden.
        private void detach() {
            getChildren().clear();
        }

        /// Sizes the scrim to the scene and centers the pane at its bounded preferred size.
        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            scrim.resizeRelocate(0.0, 0.0, width, height);

            double availableWidth = Math.max(0.0, width - DIALOG_EDGE_INSET * 2.0);
            double availableHeight = Math.max(0.0, height - DIALOG_EDGE_INSET * 2.0);
            double paneWidth = boundedPreferredSize(
                    pane.minWidth(-1.0),
                    pane.prefWidth(-1.0),
                    pane.maxWidth(-1.0),
                    availableWidth
            );
            double paneHeight = boundedPreferredSize(
                    pane.minHeight(paneWidth),
                    pane.prefHeight(paneWidth),
                    pane.maxHeight(paneWidth),
                    availableHeight
            );
            pane.resizeRelocate(
                    Math.max(DIALOG_EDGE_INSET, (width - paneWidth) / 2.0),
                    Math.max(DIALOG_EDGE_INSET, (height - paneHeight) / 2.0),
                    paneWidth,
                    paneHeight
            );
        }

        /// Returns a preferred region size constrained by its minimum, maximum, and available scene extent.
        private static double boundedPreferredSize(double minimum, double preferred, double maximum, double available) {
            double resolvedPreferred = preferred == Region.USE_COMPUTED_SIZE || preferred == Region.USE_PREF_SIZE
                    ? 0.0
                    : Math.max(0.0, preferred);
            double resolvedMinimum = minimum == Region.USE_PREF_SIZE
                    ? resolvedPreferred
                    : minimum == Region.USE_COMPUTED_SIZE ? 0.0 : Math.max(0.0, minimum);
            resolvedPreferred = Math.max(resolvedMinimum, resolvedPreferred);
            double resolvedMaximum = maximum == Region.USE_PREF_SIZE
                    ? resolvedPreferred
                    : maximum == Region.USE_COMPUTED_SIZE || maximum == Double.MAX_VALUE
                    ? Double.MAX_VALUE
                    : Math.max(resolvedMinimum, maximum);
            return Math.max(0.0, Math.min(available, Math.min(resolvedPreferred, resolvedMaximum)));
        }
    }
}
