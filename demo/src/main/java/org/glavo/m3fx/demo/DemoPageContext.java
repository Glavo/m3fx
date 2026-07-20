// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.Animation;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides page implementations with the application-level actions needed by interactive samples.
///
/// The context deliberately excludes theme, navigation, window, and scene state. Page implementations use it only
/// when a sample must present an overlay or register an animation whose lifetime follows the active page.
@NotNullByDefault
final class DemoPageContext {
    /// The demo application that owns the active presentation.
    private final M3FXDemoApp application;

    /// Creates a context backed by the specified demo application.
    ///
    /// @param application the application that owns overlay and animation state
    DemoPageContext(M3FXDemoApp application) {
        this.application = Objects.requireNonNull(application, "application");
    }

    /// Returns the active overlay pane.
    ///
    /// @return the active overlay pane, or `null` before the application scene is assembled
    @Nullable M3OverlayPane overlayPane() {
        return application.activeOverlayPane();
    }

    /// Registers an animation whose lifetime follows the current page.
    ///
    /// @param animation the animation to register
    void registerAnimation(Animation animation) {
        application.registerPageAnimation(animation);
    }

    /// Shows a transient snackbar message.
    ///
    /// @param message the message text
    void showSnackbar(String message) {
        application.showSnackbar(message);
    }

    /// Presents a dialog in the demo's in-scene overlay.
    ///
    /// @param dialog the dialog to present
    void showDialog(M3Dialog dialog) {
        application.showDialog(dialog);
    }

    /// Presents a dialog in an ownerless native window.
    ///
    /// @param dialog the dialog to present
    void showStandaloneDialog(M3Dialog dialog) {
        application.showStandaloneDialog(dialog);
    }
}
