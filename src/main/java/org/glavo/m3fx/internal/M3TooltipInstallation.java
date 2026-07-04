// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Tooltip;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Exposes installed tooltip interaction state to internal accessibility routing.
@NotNullByDefault
public interface M3TooltipInstallation {
    /// Returns the tooltip owned by this installation.
    ///
    /// @return the installed tooltip
    M3Tooltip tooltip();

    /// Returns the active focus target inside the tooltip popup.
    ///
    /// @return the active popup focus target, or `null`
    @Nullable Node activePopupFocusTarget();

    /// Returns whether the tooltip popup currently owns pointer or keyboard interaction.
    ///
    /// @return `true` when popup interaction is active
    boolean hasActivePopupInteraction();

    /// Returns whether the tooltip exposes an interactive accessibility target.
    ///
    /// @param parameters the accessibility action parameters
    /// @return `true` when an interactive target matches the parameters
    boolean containsInteractiveFocusTarget(Object... parameters);

    /// Shows the tooltip and focuses the requested interactive target.
    ///
    /// @param parameters the accessibility action parameters
    /// @return `true` when the target was shown and focused
    boolean showInteractiveFocusTarget(Object... parameters);
}