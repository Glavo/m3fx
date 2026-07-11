// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Stores tooltip installations for internal accessibility and popup interaction routing.
@NotNullByDefault
public final class M3TooltipRegistry {
    /// The node property key used to store tooltip activation handlers.
    private static final String INSTALLATION_KEY = M3TooltipRegistry.class.getName() + ".installation";

    /// Prevents utility class instantiation.
    private M3TooltipRegistry() {
    }

    /// Stores one tooltip installation on its target node.
    ///
    /// @param node the tooltip target node
    /// @param installation the installed tooltip state
    public static void install(Node node, M3TooltipInstallation installation) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(installation, "installation");
        node.getProperties().put(INSTALLATION_KEY, installation);
    }

    /// Removes any tooltip installation from a target node.
    ///
    /// @param node the tooltip target node
    public static void remove(Node node) {
        Objects.requireNonNull(node, "node");
        if (node.hasProperties()) {
            node.getProperties().remove(INSTALLATION_KEY);
        }
    }

    /// Returns the tooltip installation stored on a target node.
    ///
    /// @param node the tooltip target node
    /// @return the stored installation, or `null`
    public static @Nullable M3TooltipInstallation installation(Node node) {
        Objects.requireNonNull(node, "node");
        if (!node.hasProperties()) {
            return null;
        }
        Object installation = node.getProperties().get(INSTALLATION_KEY);
        return installation instanceof M3TooltipInstallation tooltipInstallation ? tooltipInstallation : null;
    }

    /// Returns the focused node inside an installed interactive tooltip popup for a target node.
    ///
    /// @param node the tooltip target node
    /// @return the focused popup node, or `null`
    public static @Nullable Node activeInstalledTooltipFocusTarget(Node node) {
        M3TooltipInstallation installation = installation(node);
        return installation == null ? null : installation.activePopupFocusTarget();
    }

    /// Returns whether an installed interactive tooltip currently owns pointer or keyboard focus inside its popup.
    ///
    /// @param node the tooltip target node
    /// @return `true` when popup interaction is active
    public static boolean activeInstalledTooltipPopupOwnsInteraction(Node node) {
        M3TooltipInstallation installation = installation(node);
        return installation != null && installation.hasActivePopupInteraction();
    }

    /// Returns whether an installed interactive tooltip exposes an action target requested by accessibility parameters.
    ///
    /// @param node the tooltip target node
    /// @param parameters the accessibility action parameters
    /// @return `true` when an interactive target matches the parameters
    public static boolean containsInstalledTooltipActionTarget(Node node, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        M3TooltipInstallation installation = installation(node);
        return installation != null && installation.containsInteractiveFocusTarget(parameters);
    }

    /// Shows an installed interactive tooltip and focuses the requested action target.
    ///
    /// @param node the tooltip target node
    /// @param parameters the accessibility action parameters
    /// @return `true` when the target was shown and focused
    public static boolean showInstalledTooltipActionTarget(Node node, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        M3TooltipInstallation installation = installation(node);
        return installation != null && installation.showInteractiveFocusTarget(parameters);
    }
}
