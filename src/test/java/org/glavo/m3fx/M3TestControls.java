// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3Toolbar;
import org.jetbrains.annotations.NotNullByDefault;

/// Creates M3FX test controls with pre-populated mutable child lists.
@NotNullByDefault
public final class M3TestControls {
    /// Prevents instantiation of this utility class.
    private M3TestControls() {
    }

    /// Creates a button group with initial buttons.
    public static M3ButtonGroup buttonGroup(M3Button... buttons) {
        M3ButtonGroup group = new M3ButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates a carousel with initial items.
    public static M3Carousel carousel(Node... items) {
        M3Carousel carousel = new M3Carousel();
        carousel.getItems().addAll(items);
        return carousel;
    }

    /// Creates a chip group with initial chips.
    public static M3ChipGroup chipGroup(M3Chip... chips) {
        M3ChipGroup group = new M3ChipGroup();
        group.getItems().addAll(chips);
        return group;
    }

    /// Creates a form pane with initial items.
    public static M3FormPane formPane(Node... items) {
        M3FormPane formPane = new M3FormPane();
        formPane.getItems().addAll(items);
        return formPane;
    }

    /// Creates an icon toggle button group with initial buttons.
    public static M3IconToggleButtonGroup iconToggleButtonGroup(M3IconToggleButton... buttons) {
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates a list pane with initial items.
    public static M3ListPane listPane(Node... items) {
        M3ListPane listPane = new M3ListPane();
        listPane.getItems().addAll(items);
        return listPane;
    }

    /// Creates a list view with initial data items.
    @SafeVarargs
    public static <T> M3ListView<T> listView(T... items) {
        M3ListView<T> listView = new M3ListView<>();
        listView.getItems().addAll(items);
        return listView;
    }

    /// Creates a navigation bar with initial items.
    public static M3NavigationBar navigationBar(M3NavigationItem... items) {
        M3NavigationBar navigationBar = new M3NavigationBar();
        navigationBar.getItems().addAll(items);
        return navigationBar;
    }

    /// Creates a navigation drawer with initial items.
    public static M3NavigationDrawer navigationDrawer(Node... items) {
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer();
        navigationDrawer.getItems().addAll(items);
        return navigationDrawer;
    }

    /// Creates a navigation rail with initial items.
    public static M3NavigationRail navigationRail(M3NavigationItem... items) {
        M3NavigationRail navigationRail = new M3NavigationRail();
        navigationRail.getItems().addAll(items);
        return navigationRail;
    }

    /// Creates a segmented button group with initial buttons.
    public static M3SegmentedButtonGroup segmentedButtonGroup(M3SegmentedButton... buttons) {
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates a surface with initial content nodes.
    public static M3Surface surface(Node... children) {
        M3Surface surface = new M3Surface();
        surface.getContent().addAll(children);
        return surface;
    }

    /// Creates a tab bar with initial tabs.
    public static M3TabBar tabBar(M3Tab... tabs) {
        M3TabBar tabBar = new M3TabBar();
        tabBar.getTabs().addAll(tabs);
        return tabBar;
    }

    /// Creates a toolbar with initial items.
    public static M3Toolbar toolbar(Node... items) {
        M3Toolbar toolbar = new M3Toolbar();
        toolbar.getItems().addAll(items);
        return toolbar;
    }
}