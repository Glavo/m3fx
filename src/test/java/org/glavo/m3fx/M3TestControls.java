// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3FormSection;
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
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3Toolbar;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Creates M3FX test controls with pre-populated mutable child lists.
@NotNullByDefault
public final class M3TestControls {
    /// Prevents instantiation of this utility class.
    private M3TestControls() {
    }

    /// Creates an empty banner.
    public static M3Banner banner() {
        return new M3Banner();
    }

    /// Creates a banner with message text and trailing actions.
    public static M3Banner banner(String text, Node... actions) {
        M3Banner banner = new M3Banner(text);
        banner.getActions().addAll(actions);
        return banner;
    }

    /// Creates a bottom app bar with initial actions.
    public static M3BottomAppBar bottomAppBar(Node... actions) {
        M3BottomAppBar appBar = new M3BottomAppBar();
        appBar.getActions().addAll(actions);
        return appBar;
    }

    /// Creates a bottom app bar with a floating action and initial actions.
    public static M3BottomAppBar bottomAppBar(
            M3BottomAppBarFloatingActionAlignment floatingActionAlignment,
            @Nullable Node floatingAction,
            Node... actions
    ) {
        M3BottomAppBar appBar = bottomAppBar(actions);
        appBar.setFloatingActionAlignment(floatingActionAlignment);
        appBar.setFloatingAction(floatingAction);
        return appBar;
    }

    /// Creates an empty bottom sheet.
    public static M3BottomSheet bottomSheet() {
        return new M3BottomSheet();
    }

    /// Creates a bottom sheet with headline text.
    public static M3BottomSheet bottomSheet(String headline) {
        return new M3BottomSheet(headline);
    }

    /// Creates a bottom sheet with content and initial actions.
    public static M3BottomSheet bottomSheet(String headline, @Nullable Node content, Node... actions) {
        M3BottomSheet sheet = new M3BottomSheet(headline, content);
        sheet.getActions().addAll(actions);
        return sheet;
    }

    /// Creates an empty rich tooltip.
    public static M3RichTooltip richTooltip() {
        return new M3RichTooltip();
    }

    /// Creates a rich tooltip with initial actions.
    public static M3RichTooltip richTooltip(String title, String supportingText, Node... actions) {
        M3RichTooltip tooltip = new M3RichTooltip(title, supportingText);
        tooltip.getActions().addAll(actions);
        return tooltip;
    }

    /// Creates an empty search view.
    public static M3SearchView searchView() {
        return new M3SearchView();
    }

    /// Creates a search view with prompt text and initial results.
    public static M3SearchView searchView(String promptText, Node... results) {
        M3SearchView searchView = new M3SearchView(promptText);
        searchView.getResults().addAll(results);
        return searchView;
    }

    /// Creates an empty side sheet.
    public static M3SideSheet sideSheet() {
        return new M3SideSheet();
    }

    /// Creates a side sheet with headline text.
    public static M3SideSheet sideSheet(String headline) {
        return new M3SideSheet(headline);
    }

    /// Creates a side sheet with content and initial actions.
    public static M3SideSheet sideSheet(String headline, @Nullable Node content, Node... actions) {
        M3SideSheet sheet = new M3SideSheet(headline, content);
        sheet.getActions().addAll(actions);
        return sheet;
    }

    /// Creates an empty top app bar.
    public static M3TopAppBar topAppBar() {
        return new M3TopAppBar();
    }

    /// Creates a top app bar with title text.
    public static M3TopAppBar topAppBar(String title) {
        return new M3TopAppBar(title);
    }

    /// Creates a top app bar with navigation content and initial actions.
    public static M3TopAppBar topAppBar(String title, @Nullable Node navigation, Node... actions) {
        M3TopAppBar appBar = new M3TopAppBar(title);
        appBar.setNavigation(navigation);
        appBar.getActions().addAll(actions);
        return appBar;
    }

    /// Creates a top app bar with variant, navigation content, and initial actions.
    public static M3TopAppBar topAppBar(
            String title,
            M3TopAppBarVariant variant,
            @Nullable Node navigation,
            Node... actions
    ) {
        M3TopAppBar appBar = topAppBar(title, navigation, actions);
        appBar.setVariant(variant);
        return appBar;
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

    /// Creates an empty form section.
    public static M3FormSection formSection() {
        return new M3FormSection();
    }

    /// Creates a form section with initial content.
    public static M3FormSection formSection(String titleText, Node... content) {
        M3FormSection section = new M3FormSection(titleText);
        section.getContent().addAll(content);
        return section;
    }

    /// Creates a form section with supporting text and initial content.
    public static M3FormSection formSection(String titleText, String supportingText, Node... content) {
        M3FormSection section = new M3FormSection(titleText, supportingText);
        section.getContent().addAll(content);
        return section;
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

    /// Creates a split button with initial menu items.
    public static M3SplitButton splitButton(String text, Node... items) {
        M3SplitButton splitButton = new M3SplitButton(text);
        splitButton.getItems().addAll(items);
        return splitButton;
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