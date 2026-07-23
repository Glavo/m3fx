// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.glavo.m3fx.controls.M3OverlayPane;
import org.jetbrains.annotations.NotNullByDefault;

/// Application-level navigation and feedback used by retained demo pages.
@NotNullByDefault
interface HMCLDemoController {
    /// Returns the overlay host used for dialogs and snackbars.
    ///
    /// @return the scene overlay pane
    M3OverlayPane overlay();

    /// Navigates to the home page and clears the back stack.
    void goHome();

    /// Opens the account list.
    void openAccounts();

    /// Opens the instance list.
    void openInstances();

    /// Opens management for the currently selected instance when one exists.
    void openSelectedInstance();

    /// Opens management for the instance identified by `instanceId`.
    ///
    /// @param instanceId the target instance identifier
    void openInstance(String instanceId);

    /// Opens the download center.
    void openDownload();

    /// Opens launcher settings.
    void openSettings();

    /// Opens the multiplayer page.
    void openMultiplayer();

    /// Returns one page through the navigation stack, or home when the stack is empty.
    void goBack();

    /// Simulates launching the selected instance with the selected account.
    void launchSelected();

    /// Shows a short Material snackbar.
    ///
    /// @param message the message text
    void showMessage(String message);

    /// Shows a short Material snackbar resolved from a localization key.
    ///
    /// @param key the message key
    /// @param args optional format arguments
    void showMessageKey(String key, Object... args);

    /// Refreshes window chrome that depends on the active page's local mode.
    void refreshChrome();
}
