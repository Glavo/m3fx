// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Parent;
import org.jetbrains.annotations.NotNullByDefault;

/// Defines the internal lifecycle boundary between a dialog and one presentation backend.
///
/// Implementations install the retained dialog pane either in an existing scene overlay or in a dedicated native
/// window. The dialog owns lifecycle events and pane motion; a presentation owns only its host surface, background
/// transition, and host-specific cleanup.
@NotNullByDefault
public interface M3DialogPresentation {
    /// Returns the root that supplies theme, orientation, and motion context before and during installation.
    ///
    /// @return the non-null presentation context root
    Parent getContextRoot();

    /// Prepares host context and verifies host-specific preconditions before the dialog emits its showing event.
    ///
    /// Preparation may install theme context or synchronize directionality, but must not attach the dialog pane or
    /// make a native window visible.
    ///
    /// @throws IllegalStateException if this presentation cannot currently be installed
    void prepare();

    /// Installs the dialog surface and makes its host visible when necessary.
    ///
    /// @throws IllegalStateException if this presentation is already installed
    void install();

    /// Updates whether activation of an in-scene scrim requests dialog dismissal.
    ///
    /// Presentations without a scrim ignore this setting.
    ///
    /// @param dismissOnClick whether primary scrim activation requests dismissal
    void setDismissOnScrimClick(boolean dismissOnClick);

    /// Starts the host background's exit transition.
    ///
    /// Presentations without a separately animated background invoke `onFinished` immediately.
    ///
    /// @param onFinished the action invoked after the background has settled
    /// @throws IllegalStateException if another background exit callback is already pending
    /// @throws NullPointerException  if `onFinished` is `null`
    void startBackgroundExit(Runnable onFinished);

    /// Removes the dialog surface and releases all host-specific listeners and references.
    void dispose();
}
