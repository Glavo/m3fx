// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/// Coordinates HMCL-style page replacement, window chrome, and dummy actions for the Material 3 demo.
///
/// The home page owns HMCL's launcher navigation sidebar. Opening accounts, instances, downloads, or settings
/// replaces that page with a contextual two-pane view and exposes a Back button in the title bar.
@NotNullByDefault
final class HMCLDemoShell extends StackPane {
    /// The overlay host used for transient Material feedback.
    private final M3OverlayPane overlay;

    /// The localization service shared with every page.
    private final HMCLDemoStrings strings;

    /// The deterministic application state.
    private final HMCLDemoState state;

    /// The stable page host below the custom title bar.
    private final StackPane routeHost = new StackPane();

    /// The custom title bar shared by every route.
    private final M3TopAppBar topAppBar = new M3TopAppBar();

    /// Routes retained for HMCL-style Back navigation.
    private final Deque<HMCLDemoRoute> backStack = new ArrayDeque<>();

    /// The home page retaining its local control state.
    private final HMCLHomeView homeView;

    /// The instance-list page retaining its local control state.
    private final HMCLInstancesView instancesView;

    /// The selected-instance page retaining its local control state.
    private final HMCLInstanceDetailView instanceDetailView;

    /// The download and content page retaining its local control state.
    private final HMCLDiscoverView discoverView;

    /// The account page retaining its local control state.
    private final HMCLAccountsView accountsView;

    /// The launcher-settings page retaining its local control state.
    private final HMCLSettingsView settingsView;

    /// The active route.
    private HMCLDemoRoute currentRoute = new HMCLDemoRoute.Home();

    /// The horizontal window offset captured when title-bar dragging starts.
    private double dragOffsetX;

    /// The vertical window offset captured when title-bar dragging starts.
    private double dragOffsetY;

    /// Creates the HMCL-style application shell.
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

        getStyleClass().add("hmcl-demo-shell");
        topAppBar.getStyleClass().add("hmcl-window-title-bar");
        topAppBar.setMinHeight(48.0);
        topAppBar.setPrefHeight(48.0);
        topAppBar.setMaxHeight(48.0);
        topAppBar.setOnMousePressed(this::handleWindowDragPressed);
        topAppBar.setOnMouseDragged(this::handleWindowDragged);

        routeHost.getStyleClass().add("hmcl-route-host");
        VBox windowFrame = new VBox(topAppBar, routeHost);
        windowFrame.getStyleClass().add("hmcl-window-frame");
        VBox.setVgrow(routeHost, Priority.ALWAYS);
        getChildren().add(windowFrame);

        renderCurrentRoute();
        strings.localeProperty().addListener((observable, oldLocale, newLocale) -> renderCurrentRoute());
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
            case "brightness", "theme-color", "wallpaper" ->
                    showMessage(strings.get("snackbar.settings_saved"));
            case "select-instance", "select-account", "toggle-mod" -> renderCurrentRoute();
            default -> showMessage(strings.get("snackbar.action_simulated"));
        }
    }

    /// Resolves a route identifier emitted by a page.
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
                @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
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

    /// Replaces the current page and records a route for Back navigation.
    ///
    /// @param route the destination route
    private void navigate(HMCLDemoRoute route) {
        if (route.equals(currentRoute)) {
            return;
        }
        if (route instanceof HMCLDemoRoute.Home) {
            backStack.clear();
        } else {
            backStack.push(currentRoute);
        }
        currentRoute = route;
        renderCurrentRoute();
    }

    /// Returns to the previous route, or to Home when history is empty.
    private void navigateBack() {
        currentRoute = backStack.isEmpty() ? new HMCLDemoRoute.Home() : backStack.pop();
        renderCurrentRoute();
    }

    /// Replaces route content and synchronizes the custom title bar.
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
    }

    /// Updates title, Back affordance, and window actions for the active route.
    private void updateTopAppBar() {
        topAppBar.setTitle(routeTitle());
        topAppBar.setSubtitle("");
        if (currentRoute instanceof HMCLDemoRoute.Home) {
            HBox brand = new HBox(
                    8.0,
                    HMCLDemoAssets.imageView("img/icon-title.png", 24.0, 24.0),
                    new M3Text(strings.get("app.launcher_title"), M3TextRole.TITLE_MEDIUM)
            );
            brand.setAlignment(Pos.CENTER_LEFT);
            brand.getStyleClass().add("hmcl-window-brand");
            topAppBar.setTitleContent(brand);
            topAppBar.setNavigation(null);
        } else {
            topAppBar.setTitleContent(null);
            M3IconButton back = new M3IconButton(HMCLDemoIcons.directional(HMCLDemoIcons.BACK));
            back.setAccessibleText(strings.get("common.back"));
            back.setOnAction(event -> navigateBack());
            topAppBar.setNavigation(back);
        }

        M3IconButton help = windowAction(
                HMCLDemoIcons.HELP,
                strings.get("window.help"),
                () -> showMessage(strings.get("snackbar.action_simulated"))
        );
        M3IconButton minimize = windowAction(
                HMCLDemoIcons.MINIMIZE,
                strings.get("window.minimize"),
                this::minimizeWindow
        );
        M3IconButton close = windowAction(
                HMCLDemoIcons.CLOSE,
                strings.get("window.close"),
                this::closeWindow
        );
        close.getStyleClass().add("hmcl-window-close");
        topAppBar.getActions().setAll(help, minimize, close);
    }

    /// Creates one title-bar action button.
    ///
    /// @param iconPath the SVG path rendered by the button
    /// @param accessibleText the localized accessibility label
    /// @param action the operation invoked by the button
    /// @return the configured icon button
    private M3IconButton windowAction(String iconPath, String accessibleText, Runnable action) {
        M3IconButton button = new M3IconButton(HMCLDemoIcons.create(iconPath));
        button.setAccessibleText(accessibleText);
        button.setOnAction(event -> action.run());
        return button;
    }

    /// Returns the localized or model-derived active route title.
    ///
    /// @return the title shown by the top application bar
    private String routeTitle() {
        if (currentRoute instanceof HMCLDemoRoute.Home) {
            return strings.get("app.launcher_title");
        }
        if (currentRoute instanceof HMCLDemoRoute.InstanceDetail) {
            @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
            return instance == null
                    ? strings.get("instance.title")
                    : strings.format("instance.manage_title", instance.name());
        }
        if (currentRoute instanceof HMCLDemoRoute.ContentDetail) {
            @Nullable HMCLDemoContent content = state.getSelectedContent();
            return content == null ? strings.get("discover.title") : content.title();
        }
        return strings.get(currentRoute.titleKey());
    }

    /// Creates a focused content detail with the same contextual sidebar used by downloads.
    ///
    /// @param contentId the requested content identifier
    /// @return the localized detail content
    private Node createContentDetail(String contentId) {
        if (!state.selectContent(contentId) || state.getSelectedContent() == null) {
            return discoverView;
        }
        HMCLDemoContent content = state.getSelectedContent();

        M3ListItem catalog = new M3ListItem(strings.get("discover.all"));
        catalog.setLeading(HMCLDemoIcons.create(HMCLDemoIcons.DISCOVER));
        catalog.setOnAction(event -> navigateBack());
        catalog.setSelected(true);
        VBox sidebar = new VBox(
                6.0,
                new M3Text(strings.get("discover.title"), M3TextRole.LABEL_LARGE),
                catalog
        );
        sidebar.getStyleClass().add("hmcl-context-sidebar");
        sidebar.setPrefWidth(208.0);
        sidebar.setMinWidth(208.0);
        sidebar.setPadding(new Insets(20.0, 8.0, 16.0, 8.0));

        M3Text title = new M3Text(content.title(), M3TextRole.HEADLINE_MEDIUM);
        M3Text author = new M3Text(
                strings.format("discover.by_author", content.author()),
                M3TextRole.TITLE_MEDIUM
        );
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

        VBox body = new VBox(16.0, title, author, summary, versions, downloads, install);
        body.getStyleClass().add("hmcl-context-body");
        body.setPadding(new Insets(28.0, 32.0, 40.0, 32.0));
        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        M3ScrollPanes.style(scrollPane);

        BorderPane page = new BorderPane();
        page.getStyleClass().add("hmcl-secondary-page");
        page.setLeft(sidebar);
        page.setCenter(scrollPane);
        return page;
    }

    /// Captures the title-bar offset used for subsequent window dragging.
    ///
    /// @param event the mouse press delivered by the title bar
    private void handleWindowDragPressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || isWindowActionTarget(event.getTarget())) {
            return;
        }
        @Nullable Stage stage = stage();
        if (stage != null) {
            dragOffsetX = event.getScreenX() - stage.getX();
            dragOffsetY = event.getScreenY() - stage.getY();
        }
    }

    /// Moves the window while the primary pointer drags the title bar.
    ///
    /// @param event the mouse drag delivered by the title bar
    private void handleWindowDragged(MouseEvent event) {
        if (!event.isPrimaryButtonDown() || isWindowActionTarget(event.getTarget())) {
            return;
        }
        @Nullable Stage stage = stage();
        if (stage != null) {
            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        }
    }

    /// Returns whether an event target belongs to a title-bar action button.
    ///
    /// @param target the JavaFX event target
    /// @return `true` when the target is inside an icon button
    private boolean isWindowActionTarget(Object target) {
        @Nullable Node current = target instanceof Node node ? node : null;
        while (current != null && current != topAppBar) {
            if (current instanceof M3IconButton) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /// Returns the Stage currently hosting this shell.
    ///
    /// @return the hosting stage, or `null` before attachment
    private @Nullable Stage stage() {
        if (getScene() == null) {
            return null;
        }
        @Nullable Window window = getScene().getWindow();
        return window instanceof Stage stage ? stage : null;
    }

    /// Minimizes the hosting window when it is attached.
    private void minimizeWindow() {
        @Nullable Stage stage = stage();
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    /// Closes the hosting window when it is attached.
    private void closeWindow() {
        @Nullable Stage stage = stage();
        if (stage != null) {
            stage.close();
        }
    }

    /// Shows localized launch feedback for the selected instance.
    private void showLaunchMessage() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        showMessage(instance == null
                ? strings.get("home.no_instance")
                : strings.format("home.launching", instance.name()));
    }

    /// Shows feedback matching the current foreground installation state.
    private void showInstallMessage() {
        @Nullable HMCLDemoContent content = state.getInstallingContent();
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
}
