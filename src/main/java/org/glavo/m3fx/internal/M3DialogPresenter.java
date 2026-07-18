// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ChangeListener;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents one Material dialog as an in-scene overlay above its owner content.
///
/// The presenter never creates a native window or replaces a scene root. Each dialog contributes one layer to the
/// stable [M3OverlayPane] found in its owner hierarchy. This preserves native window chrome interaction and gives
/// nested dialogs deterministic stacking without changing application scene-graph ownership.
@NotNullByDefault
public final class M3DialogPresenter {
    /// The fallback scrim opacity used without an installed Material theme.
    private static final double FALLBACK_SCRIM_OPACITY = 0.32;

    /// The minimum logical inset between a dialog and the owner scene edge.
    private static final double DIALOG_EDGE_INSET = 24.0;

    /// The Material scrim placed immediately below this dialog pane.
    private final M3Scrim scrim = new M3Scrim();

    /// The retained overlay layer containing the scrim and dialog pane.
    private final DialogLayer layer;

    /// Mirrors effective motion settings while this presenter is attached to an owner.
    private @Nullable M3MotionSettingsObserver motionSettingsObserver;

    /// The owner whose local context currently controls this presentation.
    private @Nullable Node contextOwner;

    /// The theme currently used to resolve scrim component tokens.
    private @Nullable M3Theme effectiveTheme;

    /// The theme installed directly on the layer because ordinary ancestry cannot provide it.
    private @Nullable M3Theme installedLayerTheme;

    /// The generated stylesheet associated with [installedLayerTheme].
    private @Nullable String installedLayerThemeStylesheet;

    /// Mirrors owner orientation changes into the sibling overlay layer.
    private final ChangeListener<NodeOrientation> ownerOrientationListener =
            (observable, oldOrientation, newOrientation) -> syncOrientation();

    /// The action completed after the active scrim exit transition, or `null` when no exit is pending.
    private @Nullable Runnable pendingScrimHiddenAction;

    /// The active modal-presentation handle, or `null` while this presenter is detached.
    private @Nullable M3OverlayPane.OverlayHandle overlayHandle;

    /// Creates a detached presenter for one dialog pane.
    ///
    /// @param pane           the pane rendered above this presenter's scrim
    /// @param dismissRequest the action invoked when the user activates the scrim
    /// @throws NullPointerException if `pane` is `null`
    /// @throws NullPointerException if `dismissRequest` is `null`
    public M3DialogPresenter(M3DialogPane pane, Runnable dismissRequest) {
        M3DialogPane nonNullPane = Objects.requireNonNull(pane, "pane");
        Runnable nonNullDismissRequest = Objects.requireNonNull(dismissRequest, "dismissRequest");
        scrim.setOnAction(event -> nonNullDismissRequest.run());
        scrim.setFocusTraversable(false);
        scrim.visibleProperty().addListener((observable, oldVisible, visible) -> {
            if (!visible) {
                completeScrimHide();
            }
        });
        scrim.hide();
        layer = new DialogLayer(scrim, nonNullPane);
    }

    /// Installs this dialog layer in the overlay pane containing an attached owner node.
    ///
    /// @param owner the node whose hierarchy contains the target overlay pane
    /// @throws IllegalStateException if the owner hierarchy does not contain an [M3OverlayPane] or this presenter is
    ///                               already showing
    /// @throws NullPointerException  if `owner` is `null`
    public void show(Node owner) {
        Objects.requireNonNull(owner, "owner");
        if (overlayHandle != null) {
            throw new IllegalStateException("dialog is already presented");
        }

        @Nullable M3OverlayPane currentHost = findHost(owner);
        if (currentHost == null) {
            throw new IllegalStateException("dialog owner must belong to an M3OverlayPane");
        }

        @Nullable M3OverlayPane.OverlayHandle shownHandle = null;
        boolean completed = false;
        try {
            layer.attach();
            shownHandle = currentHost.showModalOverlay(layer);
            overlayHandle = shownHandle;
            startContextSynchronization(owner);
            layer.applyCss();
            layer.layout();
            scrim.setOpacity(0.0);
            scrim.show();
            layer.requestLayout();
            completed = true;
        } finally {
            if (!completed) {
                stopContextSynchronization();
                overlayHandle = null;
                if (shownHandle != null) {
                    shownHandle.hide();
                }
                layer.detach();
            }
        }
    }

    /// Sets whether a primary click on the scrim requests dialog dismissal.
    ///
    /// @param dismissOnClick whether primary clicks activate the scrim
    public void setDismissOnScrimClick(boolean dismissOnClick) {
        scrim.setDismissOnClick(dismissOnClick);
    }

    /// Starts the scrim exit transition while retaining the layer until both dialog transitions have completed.
    ///
    /// @param onHidden the action invoked after the scrim becomes fully hidden
    /// @throws IllegalStateException if another scrim exit callback is already pending
    /// @throws NullPointerException  if `onHidden` is `null`
    public void hideScrim(Runnable onHidden) {
        Runnable nonNullOnHidden = Objects.requireNonNull(onHidden, "onHidden");
        if (pendingScrimHiddenAction != null) {
            throw new IllegalStateException("scrim exit is already pending");
        }
        if (overlayHandle == null || !scrim.isVisible()) {
            nonNullOnHidden.run();
            return;
        }

        pendingScrimHiddenAction = nonNullOnHidden;
        scrim.hide();
    }

    /// Removes the layer from its stable host and lets the overlay pane restore the appropriate prior focus owner.
    public void dispose() {
        @Nullable M3OverlayPane.OverlayHandle currentHandle = overlayHandle;
        overlayHandle = null;
        pendingScrimHiddenAction = null;
        scrim.hide();
        stopContextSynchronization();
        if (currentHandle == null) {
            return;
        }
        currentHandle.hide();
        layer.detach();
    }

    /// Runs and clears the callback retained for the active scrim exit transition.
    private void completeScrimHide() {
        @Nullable Runnable action = pendingScrimHiddenAction;
        pendingScrimHiddenAction = null;
        if (action != null) {
            action.run();
        }
    }

    /// Starts synchronization of owner-local context that cannot flow through the overlay sibling relationship.
    private void startContextSynchronization(Node owner) {
        stopContextSynchronization();
        contextOwner = owner;
        owner.effectiveNodeOrientationProperty().addListener(ownerOrientationListener);

        M3MotionSettingsObserver observer = new M3MotionSettingsObserver(owner, this::syncContext, false);
        motionSettingsObserver = observer;
        boolean completed = false;
        try {
            observer.start();
            syncContext();
            completed = true;
        } finally {
            if (!completed) {
                stopContextSynchronization();
            }
        }
    }

    /// Releases owner listeners and direct layer state after presentation ends or fails.
    private void stopContextSynchronization() {
        @Nullable Node owner = contextOwner;
        contextOwner = null;
        effectiveTheme = null;
        scrim.setVisibleOpacity(FALLBACK_SCRIM_OPACITY);
        if (owner != null) {
            owner.effectiveNodeOrientationProperty().removeListener(ownerOrientationListener);
        }

        @Nullable M3MotionSettingsObserver observer = motionSettingsObserver;
        motionSettingsObserver = null;
        if (observer != null) {
            observer.dispose();
        }

        M3MotionSettings.setReducedMotionRequested(layer, false);
        layer.setNodeOrientation(NodeOrientation.INHERIT);
        setDirectLayerTheme(null);
    }

    /// Refreshes theme, orientation, and motion settings from the active owner context.
    private void syncContext() {
        syncOrientation();
        @Nullable Node owner = contextOwner;
        if (owner != null) {
            M3MotionSettings.setReducedMotionRequested(layer, M3MotionSettings.shouldReduceMotion(owner));
        }
        syncThemeContext();
    }

    /// Mirrors the owner's effective node orientation into the overlay layer.
    private void syncOrientation() {
        @Nullable Node owner = contextOwner;
        if (owner != null) {
            layer.setNodeOrientation(owner.getEffectiveNodeOrientation());
        }
    }

    /// Resolves the effective theme and installs it locally only when normal ancestry cannot supply it.
    private void syncThemeContext() {
        @Nullable Node owner = contextOwner;
        if (owner == null) {
            return;
        }

        @Nullable Parent themeRoot = M3ThemeResolver.findThemeRoot(owner);
        @Nullable M3Theme nextEffectiveTheme = themeRoot == null ? null : M3ThemeMetadata.getTheme(themeRoot);
        @Nullable M3Theme nextDirectTheme =
                themeRoot != null && !contains(themeRoot, layer) ? nextEffectiveTheme : null;

        boolean effectiveThemeChanged = !Objects.equals(effectiveTheme, nextEffectiveTheme);
        effectiveTheme = nextEffectiveTheme;
        boolean directThemeChanged = setDirectLayerTheme(nextDirectTheme);
        if (effectiveThemeChanged) {
            scrim.setVisibleOpacity(nextEffectiveTheme == null
                    ? FALLBACK_SCRIM_OPACITY
                    : nextEffectiveTheme.tokens().componentTokens().scrim().containerOpacity());
        }
        if (effectiveThemeChanged || directThemeChanged) {
            layer.applyCss();
            layer.requestLayout();
        }
    }

    /// Installs or clears one direct theme without retaining copied owner stylesheets.
    ///
    /// @return `true` when the direct layer theme changed
    private boolean setDirectLayerTheme(@Nullable M3Theme theme) {
        if (Objects.equals(installedLayerTheme, theme)) {
            return false;
        }

        @Nullable String previousStylesheet = installedLayerThemeStylesheet;
        installedLayerThemeStylesheet = null;
        if (previousStylesheet != null) {
            layer.getStylesheets().remove(previousStylesheet);
        }

        if (theme == null) {
            M3ThemeRuntime.uninstall(layer);
            installedLayerTheme = null;
            return true;
        }

        M3ThemeRuntime.install(layer, theme);
        String stylesheet = M3ThemeRuntime.themeStylesheetUrl(theme);
        layer.getStylesheets().remove(stylesheet);
        layer.getStylesheets().add(stylesheet);
        installedLayerTheme = theme;
        installedLayerThemeStylesheet = stylesheet;
        return true;
    }

    /// Returns whether one parent is the supplied node or appears in its current parent chain.
    private static boolean contains(Parent ancestor, Node node) {
        @Nullable Node current = node;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /// Finds the nearest overlay pane containing an owner node.
    private static @Nullable M3OverlayPane findHost(Node owner) {
        @Nullable Node current = owner;
        while (current != null) {
            if (current instanceof M3OverlayPane overlayPane) {
                return overlayPane;
            }
            current = current.getParent();
        }
        return null;
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
