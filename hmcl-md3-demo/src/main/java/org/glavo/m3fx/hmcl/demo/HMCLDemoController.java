// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.glavo.m3fx.controls.M3OverlayPane;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Application-level navigation and feedback used by retained demo pages.
@NotNullByDefault
interface HMCLDemoController {
    /// Returns the overlay host used for dialogs and snackbars.
    ///
    /// @return the scene overlay pane
    M3OverlayPane overlay();

    /// Returns the shared demo state.
    ///
    /// @return the state
    HMCLDemoState state();

    /// Returns the localization service.
    ///
    /// @return the string resolver
    HMCLDemoStrings strings();

    /// Navigates to the home primary destination and clears the secondary stack.
    void goHome();

    /// Opens the account list as a secondary route.
    void openAccounts();

    /// Opens the instances primary destination.
    void openInstances();

    /// Opens management for the currently selected instance when one exists.
    void openSelectedInstance();

    /// Opens management for the instance identified by `instanceId`.
    ///
    /// @param instanceId the target instance identifier
    /// @param section the management section to show
    void openInstance(String instanceId, HMCLDemoRoute.InstanceSection section);

    /// Opens the download center.
    ///
    /// @param category the initial category
    void openDownload(HMCLDemoRoute.DownloadCategory category);

    /// Opens launcher settings.
    ///
    /// @param section the initial settings section
    void openSettings(HMCLDemoRoute.SettingsSection section);

    /// Opens the multiplayer page as a secondary route.
    void openMultiplayer();

    /// Returns one page through the navigation stack, or the owning primary destination when empty.
    void goBack();

    /// Simulates launching the selected instance with the selected account.
    void launchSelected();

    /// Starts the install wizard for a Minecraft version.
    ///
    /// @param version the version to install
    void startInstallWizard(HMCLDemoMinecraftVersion version);

    /// Starts a multi-step task dialog with the supplied title and step labels.
    ///
    /// @param title the dialog title
    /// @param steps ordered step labels
    /// @param onCompleted called when every step finishes, or `null`
    /// @param onCancelled called when the user cancels, or `null`
    void runTask(
            String title,
            java.util.List<String> steps,
            @Nullable Runnable onCompleted,
            @Nullable Runnable onCancelled
    );

    /// Shows a short Material snackbar.
    ///
    /// @param message the message text
    void showMessage(String message);

    /// Shows a short Material snackbar resolved from a localization key.
    ///
    /// @param key the message key
    /// @param args optional format arguments
    void showMessageKey(String key, Object... args);

    /// Refreshes chrome that depends on locale or route metadata.
    void refreshChrome();
}
