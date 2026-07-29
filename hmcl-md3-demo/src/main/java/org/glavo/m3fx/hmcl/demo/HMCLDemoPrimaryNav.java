// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.glavo.m3fx.layout.M3Breakpoint;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Primary app destinations shared by the adaptive navigation bar and rail.
///
/// Compact widths present a bottom [M3NavigationBar]. Medium and wider widths present an [M3NavigationRail]. The
/// rail collapses to icon-oriented destinations at medium widths and expands labels at expanded and larger
/// breakpoints. Bar and rail own separate [M3NavigationItem] nodes (a node cannot have two parents); selection is
/// synchronized between the two surfaces.
@NotNullByDefault
final class HMCLDemoPrimaryNav {
    /// Smallest rail item height that retains a touch-sized destination and its label.
    private static final double MIN_RAIL_ITEM_HEIGHT = 52.0;

    /// Preferred rail item height used when the window provides sufficient vertical space.
    private static final double MAX_RAIL_ITEM_HEIGHT = 64.0;

    /// Stable destination ids used for selection mapping.
    enum Destination {
        /// Launcher home.
        HOME,

        /// Account list.
        ACCOUNTS,

        /// Instance list.
        INSTANCES,

        /// Download center.
        DOWNLOAD,

        /// Launcher settings.
        SETTINGS,

        /// Multiplayer mock.
        MULTIPLAYER
    }

    /// Bottom navigation used at compact breakpoints.
    private final M3NavigationBar navigationBar = new M3NavigationBar();

    /// Leading rail used at medium and wider breakpoints.
    private final M3NavigationRail navigationRail = new M3NavigationRail();

    /// Height shared by rail destinations and constrained by the rail's current content area.
    private final DoubleBinding railItemHeight = Bindings.createDoubleBinding(
            this::computeRailItemHeight,
            navigationRail.heightProperty(),
            navigationRail.insetsProperty(),
            navigationRail.itemSpacingProperty()
    );

    /// Localization for destination labels.
    private final HMCLDemoStrings strings;

    /// Route controller invoked when a destination is activated.
    private final HMCLDemoController controller;

    /// When true, selection listeners must not re-enter navigation.
    private boolean synchronizingSelection;

    /// The destination that must be selected on whichever adaptive navigation surface is reachable.
    private Destination selectedDestination = Destination.HOME;

    /// The most recently applied scaffold breakpoint.
    private M3Breakpoint currentBreakpoint = M3Breakpoint.COMPACT;

    /// Bar destination items in display order.
    private final M3NavigationItem barHome = item(Destination.HOME);
    private final M3NavigationItem barAccounts = item(Destination.ACCOUNTS);
    private final M3NavigationItem barInstances = item(Destination.INSTANCES);
    private final M3NavigationItem barDownload = item(Destination.DOWNLOAD);
    private final M3NavigationItem barSettings = item(Destination.SETTINGS);
    private final M3NavigationItem barMultiplayer = item(Destination.MULTIPLAYER);

    /// Rail destination items in display order.
    private final M3NavigationItem railHome = item(Destination.HOME);
    private final M3NavigationItem railAccounts = item(Destination.ACCOUNTS);
    private final M3NavigationItem railInstances = item(Destination.INSTANCES);
    private final M3NavigationItem railDownload = item(Destination.DOWNLOAD);
    private final M3NavigationItem railSettings = item(Destination.SETTINGS);
    private final M3NavigationItem railMultiplayer = item(Destination.MULTIPLAYER);

    /// Creates primary navigation for the shell.
    ///
    /// @param controller the application controller
    HMCLDemoPrimaryNav(HMCLDemoController controller) {
        this.controller = controller;
        this.strings = controller.strings();

        navigationBar.getStyleClass().add("hmcl-primary-nav-bar");
        navigationRail.getStyleClass().add("hmcl-primary-nav-rail");
        navigationRail.setNarrow(false);
        navigationRail.setItemsCentered(true);
        navigationRail.setMinHeight(0.0);
        navigationRail.setMaxHeight(Double.MAX_VALUE);

        navigationBar.getItems().setAll(
                barHome, barAccounts, barInstances, barDownload, barSettings, barMultiplayer
        );
        navigationRail.getItems().setAll(
                railHome, railAccounts, railInstances, railDownload, railSettings, railMultiplayer
        );
        for (M3NavigationItem item : navigationRail.getItems()) {
            item.containerHeightProperty().bind(railItemHeight);
        }

        wireActivation(barHome, Destination.HOME);
        wireActivation(barAccounts, Destination.ACCOUNTS);
        wireActivation(barInstances, Destination.INSTANCES);
        wireActivation(barDownload, Destination.DOWNLOAD);
        wireActivation(barSettings, Destination.SETTINGS);
        wireActivation(barMultiplayer, Destination.MULTIPLAYER);

        wireActivation(railHome, Destination.HOME);
        wireActivation(railAccounts, Destination.ACCOUNTS);
        wireActivation(railInstances, Destination.INSTANCES);
        wireActivation(railDownload, Destination.DOWNLOAD);
        wireActivation(railSettings, Destination.SETTINGS);
        wireActivation(railMultiplayer, Destination.MULTIPLAYER);

        refreshLocale();
        select(Destination.HOME);
    }

    /// Returns the compact bottom navigation bar.
    ///
    /// @return the navigation bar
    M3NavigationBar navigationBar() {
        return navigationBar;
    }

    /// Returns the adaptive navigation rail.
    ///
    /// @return the navigation rail
    M3NavigationRail navigationRail() {
        return navigationRail;
    }

    /// Updates destination labels for the active locale.
    void refreshLocale() {
        label(barHome, railHome, "nav.home");
        label(barAccounts, railAccounts, "nav.accounts");
        label(barInstances, railInstances, "nav.instances");
        label(barDownload, railDownload, "nav.download");
        label(barSettings, railSettings, "nav.settings");
        label(barMultiplayer, railMultiplayer, "nav.multiplayer");
    }

    /// Selects the destination that best matches the active route without firing navigation.
    ///
    /// @param route the active route
    void selectRoute(HMCLDemoRoute route) {
        select(destinationFor(route));
    }

    /// Applies Material-style rail expansion for the scaffold breakpoint.
    ///
    /// Compact uses the bottom bar, so the rail's expanded flag is unused while hidden. Medium keeps a collapsed
    /// icon rail; expanded and larger breakpoints reveal labels.
    ///
    /// @param breakpoint the scaffold's effective breakpoint
    void applyBreakpoint(M3Breakpoint breakpoint) {
        currentBreakpoint = breakpoint;
        boolean expandLabels = breakpoint == M3Breakpoint.EXPANDED
                || breakpoint == M3Breakpoint.LARGE
                || breakpoint == M3Breakpoint.EXTRA_LARGE;
        navigationRail.setExpanded(expandLabels);
        // Compact bar items stay vertical (icon above label); medium bar is unused when rail is shown.
        navigationBar.setItemLayout(
                org.glavo.m3fx.controls.M3NavigationItemLayout.VERTICAL
        );
        select(selectedDestination);
        Platform.runLater(() -> {
            if (currentBreakpoint == breakpoint) {
                select(selectedDestination);
            }
        });
    }

    /// Selects one destination on both surfaces without re-entering navigation.
    private void select(Destination destination) {
        selectedDestination = destination;
        synchronizingSelection = true;
        try {
            if (isEffectivelyReachable(navigationBar)) {
                navigationBar.select(barItem(destination));
            }
            if (isEffectivelyReachable(navigationRail)) {
                navigationRail.select(railItem(destination));
            }
        } finally {
            synchronizingSelection = false;
        }
    }

    /// Returns whether a navigation surface and all of its ancestors are visible and enabled.
    private static boolean isEffectivelyReachable(Node node) {
        if (node.getScene() == null) {
            return false;
        }
        Node current = node;
        while (true) {
            if (!current.isVisible() || current.isDisabled()) {
                return false;
            }
            @Nullable Parent parent = current.getParent();
            if (parent == null) {
                return true;
            }
            current = parent;
        }
    }

    /// Returns a shared item height that keeps all destinations within the available rail height.
    private double computeRailItemHeight() {
        int itemCount = navigationRail.getItems().size();
        if (itemCount == 0) {
            return MAX_RAIL_ITEM_HEIGHT;
        }

        Insets insets = navigationRail.getInsets();
        double spacing = navigationRail.getItemSpacing() * Math.max(0, itemCount - 1);
        double availableItemHeight = (
                navigationRail.getHeight() - insets.getTop() - insets.getBottom() - spacing
        ) / itemCount;
        return Math.max(
                MIN_RAIL_ITEM_HEIGHT,
                Math.min(MAX_RAIL_ITEM_HEIGHT, availableItemHeight)
        );
    }

    /// Maps a route onto a primary destination.
    private static Destination destinationFor(HMCLDemoRoute route) {
        if (route instanceof HMCLDemoRoute.Home) {
            return Destination.HOME;
        }
        if (route instanceof HMCLDemoRoute.Accounts) {
            return Destination.ACCOUNTS;
        }
        if (route instanceof HMCLDemoRoute.Instances || route instanceof HMCLDemoRoute.Instance) {
            return Destination.INSTANCES;
        }
        if (route instanceof HMCLDemoRoute.Download) {
            return Destination.DOWNLOAD;
        }
        if (route instanceof HMCLDemoRoute.Settings) {
            return Destination.SETTINGS;
        }
        if (route instanceof HMCLDemoRoute.Multiplayer) {
            return Destination.MULTIPLAYER;
        }
        return Destination.HOME;
    }

    /// Wires one item to navigate when the user activates it.
    private void wireActivation(M3NavigationItem item, Destination destination) {
        item.setOnAction(event -> {
            if (synchronizingSelection) {
                return;
            }
            select(destination);
            navigate(destination);
        });
    }

    /// Opens the shell destination.
    private void navigate(Destination destination) {
        switch (destination) {
            case HOME -> controller.goHome();
            case ACCOUNTS -> controller.openAccounts();
            case INSTANCES -> controller.openInstances();
            case DOWNLOAD -> controller.openDownload(HMCLDemoRoute.DownloadCategory.GAME);
            case SETTINGS -> controller.openSettings(HMCLDemoRoute.SettingsSection.GLOBAL_GAME);
            case MULTIPLAYER -> controller.openMultiplayer();
        }
    }

    /// Returns the bar item for a destination.
    private M3NavigationItem barItem(Destination destination) {
        return switch (destination) {
            case HOME -> barHome;
            case ACCOUNTS -> barAccounts;
            case INSTANCES -> barInstances;
            case DOWNLOAD -> barDownload;
            case SETTINGS -> barSettings;
            case MULTIPLAYER -> barMultiplayer;
        };
    }

    /// Returns the rail item for a destination.
    private M3NavigationItem railItem(Destination destination) {
        return switch (destination) {
            case HOME -> railHome;
            case ACCOUNTS -> railAccounts;
            case INSTANCES -> railInstances;
            case DOWNLOAD -> railDownload;
            case SETTINGS -> railSettings;
            case MULTIPLAYER -> railMultiplayer;
        };
    }

    /// Creates an unselected navigation item with the destination icon.
    private static M3NavigationItem item(Destination destination) {
        M3NavigationItem item = new M3NavigationItem("", icon(destination));
        item.getStyleClass().add("hmcl-primary-nav-item");
        return item;
    }

    /// Applies the localized label to a bar/rail pair.
    private void label(M3NavigationItem barItem, M3NavigationItem railItem, String key) {
        String text = strings.get(key);
        barItem.setText(text);
        railItem.setText(text);
    }

    /// Returns a fresh icon node for a destination (nodes cannot be shared across items).
    private static Node icon(Destination destination) {
        String path = switch (destination) {
            case HOME -> HMCLDemoIcons.HOME;
            case ACCOUNTS -> HMCLDemoIcons.ACCOUNTS;
            case INSTANCES -> HMCLDemoIcons.INSTANCES;
            case DOWNLOAD -> HMCLDemoIcons.DOWNLOAD;
            case SETTINGS -> HMCLDemoIcons.SETTINGS;
            case MULTIPLAYER -> HMCLDemoIcons.GROUP;
        };
        M3SVGIcon icon = HMCLDemoIcons.create(path);
        icon.setIconSize(24.0);
        return icon;
    }
}
