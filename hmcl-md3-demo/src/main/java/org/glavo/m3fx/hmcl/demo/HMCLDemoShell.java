// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/// Adaptive Material shell for the HMCL-inspired demo.
@NotNullByDefault
final class HMCLDemoShell extends StackPane implements HMCLDemoController {
    /// The overlay host used for dialogs and snackbars.
    private final M3OverlayPane overlay;

    /// The localization service.
    private final HMCLDemoStrings strings;

    /// The deterministic application state.
    private final HMCLDemoState state;

    /// Adaptive scaffold chrome.
    private final M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();

    /// Shared top app bar.
    private final M3TopAppBar topAppBar = new M3TopAppBar();

    /// Compact navigation bar.
    private final M3NavigationBar navigationBar = new M3NavigationBar();

    /// Expanded navigation rail.
    private final M3NavigationRail navigationRail = new M3NavigationRail();

    /// Animated route host.
    private final M3AnimatedContent routeHost = new M3AnimatedContent();

    /// Primary navigation items shared conceptually between bar and rail.
    private final M3NavigationItem homeBarItem = new M3NavigationItem();
    private final M3NavigationItem instancesBarItem = new M3NavigationItem();
    private final M3NavigationItem downloadBarItem = new M3NavigationItem();
    private final M3NavigationItem settingsBarItem = new M3NavigationItem();
    private final M3NavigationItem homeRailItem = new M3NavigationItem();
    private final M3NavigationItem instancesRailItem = new M3NavigationItem();
    private final M3NavigationItem downloadRailItem = new M3NavigationItem();
    private final M3NavigationItem settingsRailItem = new M3NavigationItem();

    /// Back control for secondary routes.
    private final M3IconButton backButton = new M3IconButton(HMCLDemoIcons.back());

    /// Help action.
    private final M3IconButton helpButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.HELP));

    /// Secondary route stack.
    private final Deque<HMCLDemoRoute> backStack = new ArrayDeque<>();

    /// Active route.
    private HMCLDemoRoute currentRoute = new HMCLDemoRoute.Home();

    private final HMCLHomeView homeView;
    private final HMCLAccountsView accountsView;
    private final HMCLInstancesView instancesView;
    private final HMCLInstanceDetailView instanceDetailView;
    private final HMCLDownloadView downloadView;
    private final HMCLSettingsView settingsView;
    private final HMCLMultiplayerView multiplayerView;

    /// Creates the adaptive shell.
    ///
    /// @param overlay the overlay host
    /// @param strings the localization service
    /// @param state the shared state
    HMCLDemoShell(
            M3OverlayPane overlay,
            HMCLDemoStrings strings,
            HMCLDemoState state
    ) {
        this.overlay = overlay;
        this.strings = strings;
        this.state = state;

        homeView = new HMCLHomeView(this);
        accountsView = new HMCLAccountsView(this);
        instancesView = new HMCLInstancesView(this);
        instanceDetailView = new HMCLInstanceDetailView(this);
        downloadView = new HMCLDownloadView(this);
        settingsView = new HMCLSettingsView(this);
        multiplayerView = new HMCLMultiplayerView(this);

        getStyleClass().add("hmcl-demo-shell");
        HMCLDemoUi.fill(this);
        configureChrome();
        getChildren().setAll(scaffold);
        StackPane.setAlignment(scaffold, Pos.CENTER);
        HMCLDemoUi.fill(scaffold);

        strings.localeProperty().addListener((observable, oldLocale, newLocale) -> {
            refreshLocaleLabels();
            refreshChrome();
            homeView.refreshLocale();
            accountsView.refreshLocale();
            instancesView.refreshLocale();
            instanceDetailView.refreshLocale();
            downloadView.refreshLocale();
            settingsView.refreshLocale();
            multiplayerView.refreshLocale();
        });
        refreshLocaleLabels();
        showRoute(currentRoute, TransitionKind.IMMEDIATE);
    }

    @Override
    public M3OverlayPane overlay() {
        return overlay;
    }

    @Override
    public HMCLDemoState state() {
        return state;
    }

    @Override
    public HMCLDemoStrings strings() {
        return strings;
    }

    @Override
    public void goHome() {
        selectPrimary(new HMCLDemoRoute.Home());
    }

    @Override
    public void openAccounts() {
        pushRoute(new HMCLDemoRoute.Accounts());
    }

    @Override
    public void openInstances() {
        selectPrimary(new HMCLDemoRoute.Instances());
    }

    @Override
    public void openSelectedInstance() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance != null) {
            openInstance(instance.id(), HMCLDemoRoute.InstanceSection.SETTINGS);
        }
    }

    @Override
    public void openInstance(String instanceId, HMCLDemoRoute.InstanceSection section) {
        if (state.selectInstance(instanceId)) {
            HMCLDemoRoute target = new HMCLDemoRoute.Instance(instanceId, section);
            if (currentRoute instanceof HMCLDemoRoute.Instance current
                    && current.instanceId().equals(instanceId)) {
                replaceSection(target);
                return;
            }
            pushRoute(target);
        }
    }

    @Override
    public void openDownload(HMCLDemoRoute.DownloadCategory category) {
        HMCLDemoRoute target = new HMCLDemoRoute.Download(category);
        if (currentRoute instanceof HMCLDemoRoute.Download) {
            replaceSection(target);
            return;
        }
        selectPrimary(target);
    }

    @Override
    public void openSettings(HMCLDemoRoute.SettingsSection section) {
        HMCLDemoRoute target = new HMCLDemoRoute.Settings(section);
        if (currentRoute instanceof HMCLDemoRoute.Settings) {
            replaceSection(target);
            return;
        }
        selectPrimary(target);
    }

    @Override
    public void openMultiplayer() {
        pushRoute(new HMCLDemoRoute.Multiplayer());
    }

    @Override
    public void goBack() {
        if (backStack.isEmpty()) {
            selectPrimary(currentRoute.primaryDestination());
            return;
        }
        showRoute(backStack.pop(), TransitionKind.BACKWARD);
    }

    @Override
    public void launchSelected() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        @Nullable HMCLDemoAccount account = state.getSelectedAccount();
        if (instance == null) {
            showMessageKey("snackbar.no_instance");
            return;
        }
        if (account == null) {
            showMessageKey("snackbar.no_account");
            return;
        }
        showMessageKey("snackbar.launching", instance.name(), account.displayName());
    }

    @Override
    public void startInstallWizard(HMCLDemoMinecraftVersion version) {
        HMCLInstallWizard.show(this, version);
    }

    @Override
    public void runTask(
            String title,
            List<String> steps,
            @Nullable Runnable onCompleted,
            @Nullable Runnable onCancelled
    ) {
        HMCLTaskDialogs.run(overlay, strings, title, steps, onCompleted, onCancelled);
    }

    @Override
    public void showMessage(String message) {
        overlay.showSnackbar(new M3Snackbar(message));
    }

    @Override
    public void showMessageKey(String key, Object... args) {
        showMessage(args.length == 0 ? strings.get(key) : strings.format(key, args));
    }

    @Override
    public void refreshChrome() {
        configureTopAppBar();
        syncPrimarySelection();
    }

    /// Configures scaffold, navigation, and route host once.
    private void configureChrome() {
        scaffold.getStyleClass().add("hmcl-demo-scaffold");
        scaffold.setContentMargin(0.0);
        topAppBar.getStyleClass().add("hmcl-demo-top-app-bar");
        scaffold.setTopBar(topAppBar);
        scaffold.setNavigationBar(navigationBar);
        scaffold.setNavigationRail(navigationRail);

        configurePrimaryItem(homeBarItem, HMCLDemoIcons.HOME, () -> selectPrimary(new HMCLDemoRoute.Home()));
        configurePrimaryItem(instancesBarItem, HMCLDemoIcons.INSTANCES, () -> selectPrimary(new HMCLDemoRoute.Instances()));
        configurePrimaryItem(downloadBarItem, HMCLDemoIcons.DOWNLOAD, () -> selectPrimary(new HMCLDemoRoute.Download()));
        configurePrimaryItem(settingsBarItem, HMCLDemoIcons.SETTINGS, () -> selectPrimary(new HMCLDemoRoute.Settings()));
        configurePrimaryItem(homeRailItem, HMCLDemoIcons.HOME, () -> selectPrimary(new HMCLDemoRoute.Home()));
        configurePrimaryItem(instancesRailItem, HMCLDemoIcons.INSTANCES, () -> selectPrimary(new HMCLDemoRoute.Instances()));
        configurePrimaryItem(downloadRailItem, HMCLDemoIcons.DOWNLOAD, () -> selectPrimary(new HMCLDemoRoute.Download()));
        configurePrimaryItem(settingsRailItem, HMCLDemoIcons.SETTINGS, () -> selectPrimary(new HMCLDemoRoute.Settings()));

        navigationBar.getItems().setAll(homeBarItem, instancesBarItem, downloadBarItem, settingsBarItem);
        navigationRail.getItems().setAll(homeRailItem, instancesRailItem, downloadRailItem, settingsRailItem);

        backButton.setOnAction(event -> goBack());
        helpButton.setOnAction(event -> openSettings(HMCLDemoRoute.SettingsSection.HELP));

        HMCLDemoUi.fill(routeHost);
        routeHost.getStyleClass().add("hmcl-route-host");
        routeHost.setFitToWidth(true);
        routeHost.setFitToHeight(true);
        scaffold.setMainPane(routeHost);
    }

    /// Configures one primary navigation item.
    ///
    /// @param item the navigation item
    /// @param iconPath the icon path data
    /// @param action the selection action
    private void configurePrimaryItem(M3NavigationItem item, String iconPath, Runnable action) {
        item.setGraphic(HMCLDemoIcons.create(iconPath));
        item.setOnAction(event -> action.run());
    }

    /// Refreshes labels that depend only on locale.
    private void refreshLocaleLabels() {
        homeBarItem.setText(strings.get("nav.home"));
        instancesBarItem.setText(strings.get("nav.instances"));
        downloadBarItem.setText(strings.get("nav.download"));
        settingsBarItem.setText(strings.get("nav.settings"));
        homeRailItem.setText(strings.get("nav.home"));
        instancesRailItem.setText(strings.get("nav.instances"));
        downloadRailItem.setText(strings.get("nav.download"));
        settingsRailItem.setText(strings.get("nav.settings"));
        backButton.setAccessibleText(strings.get("common.back"));
        helpButton.setAccessibleText(strings.get("common.help"));
    }

    /// Selects a primary destination and clears the secondary stack.
    ///
    /// @param route the primary route
    private void selectPrimary(HMCLDemoRoute route) {
        Objects.requireNonNull(route, "route");
        HMCLDemoRoute primary = route.isPrimary() ? route : route.primaryDestination();
        if (primary.equals(currentRoute) && backStack.isEmpty()) {
            return;
        }
        backStack.clear();
        showRoute(primary, TransitionKind.NAVIGATION);
    }

    /// Pushes the current route and shows a secondary route.
    ///
    /// @param route the secondary route
    private void pushRoute(HMCLDemoRoute route) {
        if (route.equals(currentRoute)) {
            return;
        }
        backStack.push(currentRoute);
        showRoute(route, TransitionKind.FORWARD);
    }

    /// Replaces the active route without stacking when only a section changed.
    ///
    /// @param route the replacement route
    private void replaceSection(HMCLDemoRoute route) {
        if (route.equals(currentRoute)) {
            return;
        }
        showRoute(route, TransitionKind.SECTION);
    }

    /// Displays `route` with the requested transition.
    ///
    /// @param route the route to show
    /// @param kind the transition kind
    private void showRoute(HMCLDemoRoute route, TransitionKind kind) {
        currentRoute = route;
        if (route instanceof HMCLDemoRoute.Instance instanceRoute) {
            state.selectInstance(instanceRoute.instanceId());
        }
        routeHost.setContentTransform(transformFor(kind));
        routeHost.setContent(pageFor(route));
        if (kind == TransitionKind.IMMEDIATE || state.isAnimationDisabled()) {
            routeHost.snapToCurrentState();
        }
        refreshChrome();
        if (route.isPrimary()) {
            backStack.clear();
        }
    }

    /// Returns the transform for a transition kind.
    ///
    /// @param kind the transition kind
    /// @return the content transform
    private M3ContentTransform transformFor(TransitionKind kind) {
        if (state.isAnimationDisabled() || kind == TransitionKind.IMMEDIATE) {
            return HMCLDemoTransitions.none();
        }
        return switch (kind) {
            case NAVIGATION -> HMCLDemoTransitions.navigation();
            case FORWARD -> HMCLDemoTransitions.forward();
            case BACKWARD -> HMCLDemoTransitions.backward();
            case SECTION -> HMCLDemoTransitions.sectionUp();
            case IMMEDIATE -> HMCLDemoTransitions.none();
        };
    }

    /// Returns the retained page for `route`.
    ///
    /// @param route the route
    /// @return the page node
    private Node pageFor(HMCLDemoRoute route) {
        if (route instanceof HMCLDemoRoute.Home) {
            return homeView;
        }
        if (route instanceof HMCLDemoRoute.Accounts) {
            return accountsView;
        }
        if (route instanceof HMCLDemoRoute.Instances) {
            return instancesView;
        }
        if (route instanceof HMCLDemoRoute.Instance instance) {
            instanceDetailView.showInstance(instance.instanceId(), instance.section());
            return instanceDetailView;
        }
        if (route instanceof HMCLDemoRoute.Download download) {
            downloadView.showCategory(download.category());
            return downloadView;
        }
        if (route instanceof HMCLDemoRoute.Settings settings) {
            settingsView.showSection(settings.section());
            return settingsView;
        }
        if (route instanceof HMCLDemoRoute.Multiplayer) {
            return multiplayerView;
        }
        throw new IllegalStateException("Unsupported route: " + route);
    }

    /// Updates the top app bar for the active route.
    private void configureTopAppBar() {
        topAppBar.setTitle(routeTitle(currentRoute));
        boolean showBack = !currentRoute.isPrimary() || !backStack.isEmpty();
        topAppBar.setNavigation(showBack ? backButton : null);
        topAppBar.getActions().setAll(helpButton);
    }

    /// Synchronizes bar and rail selection with the active primary destination.
    private void syncPrimarySelection() {
        HMCLDemoRoute primary = currentRoute.primaryDestination();
        if (primary instanceof HMCLDemoRoute.Home) {
            navigationBar.select(homeBarItem);
            navigationRail.select(homeRailItem);
        } else if (primary instanceof HMCLDemoRoute.Instances) {
            navigationBar.select(instancesBarItem);
            navigationRail.select(instancesRailItem);
        } else if (primary instanceof HMCLDemoRoute.Download) {
            navigationBar.select(downloadBarItem);
            navigationRail.select(downloadRailItem);
        } else if (primary instanceof HMCLDemoRoute.Settings) {
            navigationBar.select(settingsBarItem);
            navigationRail.select(settingsRailItem);
        }
    }

    /// Returns the top-app-bar title for `route`.
    ///
    /// @param route the route
    /// @return the title text
    private String routeTitle(HMCLDemoRoute route) {
        if (route instanceof HMCLDemoRoute.Home) {
            return strings.get("app.title");
        }
        if (route instanceof HMCLDemoRoute.Accounts) {
            return strings.get("accounts.title");
        }
        if (route instanceof HMCLDemoRoute.Instances) {
            return strings.get("instances.title");
        }
        if (route instanceof HMCLDemoRoute.Instance instance) {
            @Nullable HMCLDemoInstance selected = state.getSelectedInstance();
            if (selected != null && selected.id().equals(instance.instanceId())) {
                return selected.name();
            }
            return strings.get("instance.title");
        }
        if (route instanceof HMCLDemoRoute.Download) {
            return strings.get("download.title");
        }
        if (route instanceof HMCLDemoRoute.Settings) {
            return strings.get("settings.title");
        }
        if (route instanceof HMCLDemoRoute.Multiplayer) {
            return strings.get("multiplayer.title");
        }
        return strings.get("app.title");
    }

    /// Identifies how a route replacement should animate.
    private enum TransitionKind {
        /// Primary destination switch.
        NAVIGATION,

        /// Secondary stack push.
        FORWARD,

        /// Secondary stack pop.
        BACKWARD,

        /// In-page section replacement.
        SECTION,

        /// Initial presentation without motion.
        IMMEDIATE
    }
}
