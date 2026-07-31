// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.Node;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.jetbrains.annotations.NotNullByDefault;

/// Primary app destinations shared by the adaptive navigation bar and rail.
///
/// Compact widths present a bottom [M3NavigationBar]. Medium and wider widths present an [M3NavigationRail]. The
/// rail remains collapsed with icon-oriented destinations at every breakpoint. Bar and rail own separate
/// [M3NavigationItem] nodes (a node cannot have two parents); selection is synchronized between the two surfaces.
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
        navigationRail.setExpanded(false);
        navigationRail.setItemsCentered(true);
        navigationRail.setMinHeight(0.0);
        navigationRail.setMaxHeight(Double.MAX_VALUE);
        navigationBar.setItemLayout(M3NavigationItemLayout.VERTICAL);

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

    /// Selects one destination on both adaptive surfaces without re-entering navigation.
    private void select(Destination destination) {
        synchronizingSelection = true;
        try {
            navigationBar.select(barItem(destination));
            navigationRail.select(railItem(destination));
        } finally {
            synchronizingSelection = false;
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
