// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Snackbar;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Snackbars component showcase page.
@NotNullByDefault
final class SnackbarsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SnackbarsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the snackbar component page.
    Node createContent() {
        M3Button messageButton = new M3Button("Show message", M3ButtonVariant.FILLED);
        messageButton.setOnAction(event -> context.showSnackbar("Theme-aware snackbar"));
        M3Button actionButton = new M3Button("Show action", M3ButtonVariant.TONAL);
        actionButton.setOnAction(event -> showActionSnackbar());
        M3Button queueButton = new M3Button("Queue messages", M3ButtonVariant.OUTLINED);
        queueButton.setOnAction(event -> showQueuedSnackbars());
        M3Button dismissibleButton = new M3Button("Show dismissible", M3ButtonVariant.ELEVATED);
        dismissibleButton.setOnAction(event -> {
            M3OverlayPane activeOverlay = context.overlayPane();
            if (activeOverlay == null) {
                return;
            }
            M3Snackbar snackbar = new M3Snackbar("Dismiss this message");
            snackbar.setCloseButtonVisible(true);
            activeOverlay.showSnackbar(snackbar);
        });

        return createGallery(createShowcaseGroup(
                "Snackbar Presentation",
                messageButton,
                actionButton,
                dismissibleButton,
                queueButton
        ));
    }

    /// Shows a snackbar that contains an action.
    private void showActionSnackbar() {
        M3OverlayPane activeOverlay = context.overlayPane();
        if (activeOverlay == null) {
            return;
        }

        M3Snackbar snackbar = new M3Snackbar("Theme-aware snackbar");
        snackbar.setActionText("Action");
        snackbar.setAction(() -> activeOverlay.enqueueSnackbar(new M3Snackbar("Action pressed")));
        activeOverlay.showSnackbar(snackbar);
    }

    /// Enqueues the multi-message snackbar sample.
    private void showQueuedSnackbars() {
        M3OverlayPane activeOverlay = context.overlayPane();
        if (activeOverlay == null) {
            return;
        }

        activeOverlay.enqueueSnackbar(new M3Snackbar("First queued message"));
        M3Snackbar second = new M3Snackbar("Second queued message");
        second.setActionText("Undo");
        second.setAction(() -> activeOverlay.enqueueSnackbar(new M3Snackbar("Undo pressed")));
        activeOverlay.enqueueSnackbar(second);
        activeOverlay.enqueueSnackbar(new M3Snackbar("Third queued message"));
    }
}

