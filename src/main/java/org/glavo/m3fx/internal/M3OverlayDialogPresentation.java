// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents one Material dialog as an in-scene overlay above application content.
///
/// The presenter never creates a native window or replaces a scene root. Each dialog contributes one layer to the
/// [M3OverlayPane] selected by the caller. This preserves native window chrome interaction and gives nested dialogs
/// deterministic stacking without changing application scene-graph ownership.
@NotNullByDefault
public final class M3OverlayDialogPresentation implements M3DialogPresentation {
    /// The fallback scrim opacity used without an installed Material theme.
    private static final double FALLBACK_SCRIM_OPACITY = 0.32;

    /// The minimum logical inset between a dialog and the host edge.
    private static final double DIALOG_EDGE_INSET = 24.0;

    /// The Material scrim placed immediately below this dialog pane.
    private final M3Scrim scrim = new M3Scrim();

    /// The retained overlay layer containing the scrim and dialog pane.
    private final DialogLayer layer;

    /// The overlay pane that owns this presentation and supplies its inherited context.
    private final M3OverlayPane host;

    /// Observes host token-context changes while this presenter is attached.
    private @Nullable M3MotionSettingsObserver contextObserver;

    /// The action completed after the active scrim exit transition, or `null` when no exit is pending.
    private @Nullable Runnable pendingScrimHiddenAction;

    /// The active modal-presentation handle, or `null` while this presenter is detached.
    private @Nullable M3OverlayPane.OverlayHandle overlayHandle;

    /// Creates a detached presenter for one dialog pane.
    ///
    /// @param host           the overlay pane that will own this presentation
    /// @param pane           the pane rendered above this presenter's scrim
    /// @param dismissRequest the action invoked when the user activates the scrim
    /// @throws NullPointerException if `pane` is `null`
    /// @throws NullPointerException if `dismissRequest` is `null`
    public M3OverlayDialogPresentation(M3OverlayPane host, M3DialogPane pane, Runnable dismissRequest) {
        this.host = Objects.requireNonNull(host, "host");
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

    /// Returns the overlay pane that supplies this presentation's inherited context.
    ///
    /// @return the non-null overlay pane
    @Override
    public Parent getContextRoot() {
        return host;
    }

    /// Verifies that the overlay pane belongs to a showing window.
    @Override
    public void prepare() {
        if (host.getScene() == null
                || host.getScene().getWindow() == null
                || !host.getScene().getWindow().isShowing()) {
            throw new IllegalStateException("dialog host must be attached to a showing window");
        }
    }

    /// Installs this dialog layer in the retained overlay pane.
    ///
    /// @throws IllegalStateException if this presenter is already showing
    @Override
    public void install() {
        if (overlayHandle != null) {
            throw new IllegalStateException("dialog is already presented");
        }

        @Nullable M3OverlayPane.OverlayHandle shownHandle = null;
        boolean completed = false;
        try {
            layer.attach();
            shownHandle = host.showModalOverlay(layer);
            overlayHandle = shownHandle;
            startContextSynchronization();
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
    @Override
    public void setDismissOnScrimClick(boolean dismissOnClick) {
        scrim.setDismissOnClick(dismissOnClick);
    }

    /// Starts the scrim exit transition while retaining the layer until both dialog transitions have completed.
    ///
    /// @param onHidden the action invoked after the scrim becomes fully hidden
    /// @throws IllegalStateException if another scrim exit callback is already pending
    /// @throws NullPointerException  if `onHidden` is `null`
    @Override
    public void startBackgroundExit(Runnable onHidden) {
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
    @Override
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

    /// Starts observation of host theme-token changes needed by non-CSS scrim geometry.
    ///
    private void startContextSynchronization() {
        stopContextSynchronization();

        M3MotionSettingsObserver observer = new M3MotionSettingsObserver(host, this::syncScrimOpacity, false);
        contextObserver = observer;
        boolean completed = false;
        try {
            observer.start();
            syncScrimOpacity();
            completed = true;
        } finally {
            if (!completed) {
                stopContextSynchronization();
            }
        }
    }

    /// Releases host token observation after presentation ends or fails.
    private void stopContextSynchronization() {
        scrim.setVisibleOpacity(FALLBACK_SCRIM_OPACITY);

        @Nullable M3MotionSettingsObserver observer = contextObserver;
        contextObserver = null;
        if (observer != null) {
            observer.dispose();
        }
    }

    /// Resolves and applies the effective Material scrim opacity token.
    private void syncScrimOpacity() {
        @Nullable Parent themeRoot = M3ThemeResolver.findThemeRoot(host);
        @Nullable M3Theme nextEffectiveTheme = themeRoot == null ? null : M3ThemeMetadata.getTheme(themeRoot);
        scrim.setVisibleOpacity(nextEffectiveTheme == null
                ? FALLBACK_SCRIM_OPACITY
                : nextEffectiveTheme.tokens().componentTokens().scrim().containerOpacity());
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
