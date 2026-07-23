// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3Breakpoint;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.glavo.m3fx.layout.M3PaneRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/// Coordinates adaptive navigation, routing, and dummy actions for the HMCL Material 3 demo.
///
/// Compact windows use bottom navigation, medium windows use a navigation rail, and expanded windows use a
/// persistent navigation drawer. Top-level routes clear navigation history while detail routes retain a back target.
@NotNullByDefault
final class HMCLDemoShell extends StackPane {
    /// The fixed width of the expanded navigation drawer.
    private static final double DRAWER_WIDTH = 304.0;

    /// The ordered top-level destinations shared by all navigation presentations.
    private static final @Unmodifiable List<Destination> DESTINATIONS = List.of(
            new Destination("home", "nav.home", HMCLDemoIcons.HOME),
            new Destination("instances", "nav.instances", HMCLDemoIcons.INSTANCES),
            new Destination("discover", "nav.discover", HMCLDemoIcons.DISCOVER),
            new Destination("accounts", "nav.accounts", HMCLDemoIcons.ACCOUNTS),
            new Destination("settings", "nav.settings", HMCLDemoIcons.SETTINGS)
    );

    /// The overlay host used for transient Material feedback.
    private final M3OverlayPane overlay;

    /// The localization service shared with pages and navigation.
    private final HMCLDemoStrings strings;

    /// The deterministic application state.
    private final HMCLDemoState state;

    /// The stable adaptive layout control.
    private final M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();

    /// The stable route content host.
    private final StackPane routeHost = new StackPane();

    /// The stable top application bar.
    private final M3TopAppBar topAppBar = new M3TopAppBar();

    /// Routes retained for detail-page back navigation.
    private final Deque<HMCLDemoRoute> backStack = new ArrayDeque<>();

    /// The home page retaining its local control state.
    private final HMCLHomeView homeView;

    /// The instance-list page retaining its local control state.
    private final HMCLInstancesView instancesView;

    /// The selected-instance page retaining its local control state.
    private final HMCLInstanceDetailView instanceDetailView;

    /// The Discover page retaining its local control state.
    private final HMCLDiscoverView discoverView;

    /// The account page retaining its local control state.
    private final HMCLAccountsView accountsView;

    /// The settings page retaining its local control state.
    private final HMCLSettingsView settingsView;

    /// The active route.
    private HMCLDemoRoute currentRoute = new HMCLDemoRoute.Home();

    /// The compact navigation presentation created for the active locale.
    private M3NavigationBar navigationBar = new M3NavigationBar();

    /// The medium navigation presentation created for the active locale.
    private M3NavigationRail navigationRail = new M3NavigationRail();

    /// The expanded navigation presentation created for the active locale.
    private ScrollPane navigationDrawer = new ScrollPane();

    /// Creates the adaptive application shell.
    ///
    /// @param overlay the overlay host used by this shell
    /// @param strings the runtime localization service
    /// @param state the shared deterministic state
    HMCLDemoShell(M3OverlayPane overlay, HMCLDemoStrings strings, HMCLDemoState state) {
        this.overlay = overlay;
        this.strings = strings;
        this.state = state;

        HMCLDemoActions actions = this::dispatch;
        homeView = new HMCLHomeView(strings, state, actions);
        instancesView = new HMCLInstancesView(strings, state, actions);
        instanceDetailView = new HMCLInstanceDetailView(strings, state, actions);
        discoverView = new HMCLDiscoverView(strings, state, actions);
        accountsView = new HMCLAccountsView(strings, state, actions);
        settingsView = new HMCLSettingsView(strings, state, actions);

        getStyleClass().addAll("hmcl-demo-shell", "hmcl-wallpaper-meadow");
        scaffold.getStyleClass().add("hmcl-demo-scaffold");
        scaffold.setContentMargin(0.0);
        scaffold.setPaneSpacing(0.0);
        scaffold.setFixedLeadingPaneWidth(DRAWER_WIDTH);
        scaffold.setTopBar(topAppBar);
        scaffold.setMainPane(routeHost);
        scaffold.setActivePane(M3PaneRole.MAIN);
        scaffold.breakpointProperty().addListener(
                (observable, oldBreakpoint, newBreakpoint) -> updateAdaptiveNavigation(newBreakpoint)
        );
        getChildren().add(scaffold);

        rebuildNavigation();
        renderCurrentRoute();
        updateWallpaper();
        strings.localeProperty().addListener((observable, oldLocale, newLocale) -> {
            rebuildNavigation();
            renderCurrentRoute();
        });
        state.wallpaperProperty().addListener((observable, oldWallpaper, newWallpaper) -> updateWallpaper());
    }

    /// Recreates localized navigation controls and installs the breakpoint-appropriate presentation.
    private void rebuildNavigation() {
        scaffold.setNavigationBar(null);
        scaffold.setNavigationRail(null);
        scaffold.setLeadingPane(null);

        M3NavigationBar compact = new M3NavigationBar();
        M3NavigationRail medium = new M3NavigationRail();
        medium.setExpanded(false);
        M3NavigationDrawer expanded = new M3NavigationDrawer();
        expanded.setAllowEmptySelection(true);

        for (Destination destination : DESTINATIONS) {
            M3NavigationItem compactItem = createNavigationItem(destination);
            M3NavigationItem mediumItem = createNavigationItem(destination);
            compact.getItems().add(compactItem);
            medium.getItems().add(mediumItem);

            M3ListItem drawerItem = new M3ListItem(strings.get(destination.labelKey()));
            drawerItem.setLeading(HMCLDemoIcons.create(destination.iconPath()));
            drawerItem.setUserData(destination.id());
            drawerItem.setOnAction(event -> navigate(destination.route()));
            expanded.getItems().add(drawerItem);
        }

        VBox drawerContent = new VBox(12.0, createDrawerHeader(), expanded);
        drawerContent.getStyleClass().add("hmcl-demo-drawer-content");
        ScrollPane drawerScroll = new ScrollPane(drawerContent);
        drawerScroll.getStyleClass().add("hmcl-demo-drawer");
        drawerScroll.setFitToWidth(true);
        drawerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        M3ScrollPanes.style(drawerScroll);

        navigationBar = compact;
        navigationRail = medium;
        navigationDrawer = drawerScroll;
        updateAdaptiveNavigation(scaffold.getBreakpoint());
        refreshNavigationSelection();
    }

    /// Creates the launcher identity shown above expanded navigation destinations.
    ///
    /// @return the localized drawer header
    private Node createDrawerHeader() {
        M3Text title = new M3Text(strings.get("app.title"), M3TextRole.TITLE_LARGE);
        M3Text subtitle = new M3Text(strings.get("app.subtitle"), M3TextRole.BODY_MEDIUM);
        subtitle.setWrapText(true);
        VBox text = new VBox(3.0, title, subtitle);
        VBox header = new VBox(14.0, HMCLDemoAssets.skinFace("img/skin/wide/steve.png", 48.0), text);
        header.getStyleClass().add("hmcl-demo-drawer-header");
        header.setPadding(new Insets(24.0, 20.0, 12.0, 20.0));
        return header;
    }

    /// Creates one selectable navigation item.
    ///
    /// @param destination the represented destination
    /// @return the configured navigation item
    private M3NavigationItem createNavigationItem(Destination destination) {
        M3NavigationItem item = new M3NavigationItem(
                strings.get(destination.labelKey()),
                HMCLDemoIcons.create(destination.iconPath())
        );
        item.setUserData(destination.id());
        item.setOnAction(event -> navigate(destination.route()));
        return item;
    }

    /// Applies compact, medium, or expanded navigation slots for the current width class.
    ///
    /// @param breakpoint the active Material width breakpoint
    private void updateAdaptiveNavigation(M3Breakpoint breakpoint) {
        switch (breakpoint) {
            case COMPACT -> {
                scaffold.setLeadingPane(null);
                scaffold.setNavigationRail(null);
                scaffold.setNavigationBar(navigationBar);
                scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            }
            case MEDIUM -> {
                scaffold.setLeadingPane(null);
                scaffold.setNavigationBar(null);
                scaffold.setNavigationRail(navigationRail);
                scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            }
            case EXPANDED, LARGE, EXTRA_LARGE -> {
                scaffold.setNavigationBar(null);
                scaffold.setNavigationRail(null);
                scaffold.setLeadingPane(navigationDrawer);
                scaffold.setPaneLayout(M3PaneLayout.FIXED_LEADING);
            }
        }
    }

    /// Handles one page command encoded by [HMCLDemoActions].
    ///
    /// @param command the encoded command
    private void dispatch(String command) {
        int separator = command.indexOf(':');
        String action = separator < 0 ? command : command.substring(0, separator);
        @Nullable String target = separator < 0 ? null : command.substring(separator + 1);
        switch (action) {
            case "navigate" -> navigate(routeForId(target));
            case "content-detail" -> {
                if (target != null && state.selectContent(target)) {
                    navigate(new HMCLDemoRoute.ContentDetail(target));
                }
            }
            case "play" -> showLaunchMessage();
            case "add-instance" -> {
                state.addDemoInstance();
                showMessage(strings.get("snackbar.instance_added"));
            }
            case "copy-instance" -> showMessage(strings.get("snackbar.instance_copied"));
            case "delete-instance" -> showMessage(strings.get("snackbar.instance_deleted"));
            case "add-account" -> {
                HMCLDemoAccount.AccountType type = "microsoft".equals(target)
                        ? HMCLDemoAccount.AccountType.MICROSOFT
                        : HMCLDemoAccount.AccountType.OFFLINE;
                state.addDummyAccount(type);
                showMessage(strings.get("snackbar.account_added"));
            }
            case "remove-account" -> showMessage(strings.get("snackbar.account_removed"));
            case "install" -> showInstallMessage();
            case "refresh" -> showMessage(strings.get("snackbar.refreshed"));
            case "brightness", "theme-color", "wallpaper" -> showMessage(strings.get("snackbar.settings_saved"));
            case "select-instance", "select-account", "toggle-mod" -> renderCurrentRoute();
            default -> showMessage(strings.get("snackbar.action_simulated"));
        }
    }

    /// Resolves a top-level route identifier.
    ///
    /// @param id the route identifier, or `null`
    /// @return the resolved route
    private HMCLDemoRoute routeForId(@Nullable String id) {
        if (id == null) {
            return new HMCLDemoRoute.Home();
        }
        return switch (id) {
            case HMCLDemoActions.ROUTE_INSTANCES -> new HMCLDemoRoute.Instances();
            case HMCLDemoActions.ROUTE_INSTANCE_DETAIL -> {
                HMCLDemoInstance instance = state.getSelectedInstance();
                yield instance == null
                        ? new HMCLDemoRoute.Instances()
                        : new HMCLDemoRoute.InstanceDetail(instance.id());
            }
            case HMCLDemoActions.ROUTE_DISCOVER -> new HMCLDemoRoute.Discover();
            case HMCLDemoActions.ROUTE_ACCOUNTS -> new HMCLDemoRoute.Accounts();
            case HMCLDemoActions.ROUTE_SETTINGS -> new HMCLDemoRoute.Settings();
            default -> new HMCLDemoRoute.Home();
        };
    }

    /// Navigates to one route and updates top-level history semantics.
    ///
    /// @param route the destination route
    private void navigate(HMCLDemoRoute route) {
        if (route.equals(currentRoute)) {
            return;
        }
        if (route.topLevel()) {
            backStack.clear();
        } else {
            backStack.push(currentRoute);
        }
        currentRoute = route;
        renderCurrentRoute();
    }

    /// Returns to the retained route or to the route's natural top-level parent.
    private void navigateBack() {
        if (!backStack.isEmpty()) {
            currentRoute = backStack.pop();
        } else if (currentRoute instanceof HMCLDemoRoute.InstanceDetail) {
            currentRoute = new HMCLDemoRoute.Instances();
        } else {
            currentRoute = new HMCLDemoRoute.Discover();
        }
        renderCurrentRoute();
    }

    /// Replaces route content and synchronizes the top app bar and navigation selection.
    private void renderCurrentRoute() {
        Node content;
        if (currentRoute instanceof HMCLDemoRoute.Home) {
            content = homeView;
        } else if (currentRoute instanceof HMCLDemoRoute.Instances) {
            content = instancesView;
        } else if (currentRoute instanceof HMCLDemoRoute.InstanceDetail detail) {
            state.selectInstance(detail.instanceId());
            content = instanceDetailView;
        } else if (currentRoute instanceof HMCLDemoRoute.Discover) {
            content = discoverView;
        } else if (currentRoute instanceof HMCLDemoRoute.ContentDetail detail) {
            content = createContentDetail(detail.contentId());
        } else if (currentRoute instanceof HMCLDemoRoute.Accounts) {
            content = accountsView;
        } else {
            content = settingsView;
        }
        routeHost.getChildren().setAll(content);
        updateTopAppBar();
        refreshNavigationSelection();
    }

    /// Updates the shared top app bar for the active route.
    private void updateTopAppBar() {
        topAppBar.setTitle(routeTitle());
        if (currentRoute.topLevel()) {
            topAppBar.setNavigation(null);
        } else {
            M3IconButton back = new M3IconButton(HMCLDemoIcons.directional(HMCLDemoIcons.BACK));
            back.setAccessibleText(strings.get("common.back"));
            back.setOnAction(event -> navigateBack());
            topAppBar.setNavigation(back);
        }
        topAppBar.getActions().clear();
        if (!(currentRoute instanceof HMCLDemoRoute.Settings)) {
            M3IconButton appearance = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.PALETTE));
            appearance.setAccessibleText(strings.get("settings.appearance"));
            appearance.setOnAction(event -> navigate(new HMCLDemoRoute.Settings()));
            topAppBar.getActions().add(appearance);
        }
    }

    /// Returns the localized or model-derived active route title.
    ///
    /// @return the title shown by the top app bar
    private String routeTitle() {
        if (currentRoute instanceof HMCLDemoRoute.InstanceDetail) {
            HMCLDemoInstance instance = state.getSelectedInstance();
            return instance == null ? strings.get("instance.title") : instance.name();
        }
        if (currentRoute instanceof HMCLDemoRoute.ContentDetail) {
            HMCLDemoContent content = state.getSelectedContent();
            return content == null ? strings.get("discover.title") : content.title();
        }
        return strings.get(currentRoute.titleKey());
    }

    /// Creates the focused Discover detail page for one dummy catalog entry.
    ///
    /// @param contentId the requested content identifier
    /// @return the localized detail content
    private Node createContentDetail(String contentId) {
        if (!state.selectContent(contentId) || state.getSelectedContent() == null) {
            return discoverView;
        }
        HMCLDemoContent content = state.getSelectedContent();
        M3Text title = new M3Text(content.title(), M3TextRole.HEADLINE_LARGE);
        M3Text author = new M3Text(strings.format("discover.by_author", content.author()), M3TextRole.TITLE_MEDIUM);
        M3Text summary = new M3Text(
                HMCLDemoModelText.contentSummary(strings, content),
                M3TextRole.BODY_LARGE
        );
        summary.setWrapText(true);
        M3Text versions = new M3Text(
                strings.format("discover.versions", String.join(", ", content.gameVersions())),
                M3TextRole.BODY_MEDIUM
        );
        M3Text downloads = new M3Text(
                strings.format("discover.downloads", content.downloadCount()),
                M3TextRole.BODY_MEDIUM
        );
        M3Button install = new M3Button(strings.get("common.install"), M3ButtonVariant.FILLED);
        install.setDisable(state.installStateFor(content) == HMCLDemoState.InstallState.INSTALLED);
        install.setOnAction(event -> {
            if (state.startInstallation(content)) {
                state.setInstallProgress(1.0);
            }
            showInstallMessage();
            renderCurrentRoute();
        });

        VBox page = new VBox(18.0, title, author, summary, versions, downloads, install);
        page.getStyleClass().add("hmcl-demo-detail-page");
        page.setPadding(new Insets(32.0));
        page.setAlignment(Pos.TOP_LEFT);
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        M3ScrollPanes.style(scrollPane);
        return scrollPane;
    }

    /// Synchronizes selected state across all adaptive navigation presentations.
    private void refreshNavigationSelection() {
        String selectedId = selectedTopLevelId();
        for (M3NavigationItem item : navigationBar.getItems()) {
            item.setSelected(selectedId.equals(item.getUserData()));
        }
        for (M3NavigationItem item : navigationRail.getItems()) {
            item.setSelected(selectedId.equals(item.getUserData()));
        }
        Node content = navigationDrawer.getContent();
        if (content instanceof VBox box && box.getChildren().size() > 1
                && box.getChildren().get(1) instanceof M3NavigationDrawer drawer) {
            for (Node item : drawer.getItems()) {
                if (item instanceof M3ListItem listItem) {
                    listItem.setSelected(selectedId.equals(listItem.getUserData()));
                }
            }
        }
    }

    /// Returns the selected navigation identifier, including the parent of a detail route.
    ///
    /// @return the selected top-level destination identifier
    private String selectedTopLevelId() {
        if (currentRoute instanceof HMCLDemoRoute.Instances
                || currentRoute instanceof HMCLDemoRoute.InstanceDetail) {
            return "instances";
        }
        if (currentRoute instanceof HMCLDemoRoute.Discover
                || currentRoute instanceof HMCLDemoRoute.ContentDetail) {
            return "discover";
        }
        if (currentRoute instanceof HMCLDemoRoute.Accounts) {
            return "accounts";
        }
        if (currentRoute instanceof HMCLDemoRoute.Settings) {
            return "settings";
        }
        return "home";
    }

    /// Applies the selected decorative wallpaper class without changing Material color roles.
    private void updateWallpaper() {
        getStyleClass().removeAll("hmcl-wallpaper-meadow", "hmcl-wallpaper-caves", "hmcl-wallpaper-sunset");
        getStyleClass().add("hmcl-wallpaper-" + state.getWallpaper().name().toLowerCase(Locale.ROOT));
    }

    /// Shows localized launch feedback for the selected instance.
    private void showLaunchMessage() {
        HMCLDemoInstance instance = state.getSelectedInstance();
        showMessage(instance == null
                ? strings.get("home.no_instance")
                : strings.format("home.launching", instance.name()));
    }

    /// Shows feedback matching the current foreground installation state.
    private void showInstallMessage() {
        HMCLDemoContent content = state.getInstallingContent();
        String title = content == null && state.getSelectedContent() != null
                ? state.getSelectedContent().title()
                : content == null ? strings.get("discover.title") : content.title();
        String key = state.installStateProperty().get() == HMCLDemoState.InstallState.INSTALLED
                ? "snackbar.install_completed"
                : "snackbar.install_started";
        showMessage(strings.format(key, title));
    }

    /// Enqueues a Material snackbar at the overlay root.
    ///
    /// @param message the localized message
    private void showMessage(String message) {
        overlay.enqueueSnackbar(new M3Snackbar(message));
    }

    /// Describes one top-level adaptive navigation destination.
    ///
    /// @param id the stable route identifier
    /// @param labelKey the localization key for its label
    /// @param iconPath the SVG path used by its icon
    private record Destination(String id, String labelKey, String iconPath) {
        /// Creates a destination descriptor.
        ///
        /// @param id the stable route identifier
        /// @param labelKey the localization key for its label
        /// @param iconPath the SVG path used by its icon
        private Destination {
        }

        /// Creates the represented route.
        ///
        /// @return a new top-level route value
        private HMCLDemoRoute route() {
            return switch (id) {
                case "instances" -> new HMCLDemoRoute.Instances();
                case "discover" -> new HMCLDemoRoute.Discover();
                case "accounts" -> new HMCLDemoRoute.Accounts();
                case "settings" -> new HMCLDemoRoute.Settings();
                default -> new HMCLDemoRoute.Home();
            };
        }
    }
}
