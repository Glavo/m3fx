// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/// HMCL-style undecorated window shell modeled on `DecoratorSkin`.
///
/// Critical layout contract matching HMCL:
/// - outer window padding 8 for shadow
/// - clipped body with 8px corner radius
/// - fixed 40px title bar that never shrinks
/// - page content uses `min size = 0` so list preferred heights cannot steal title-bar space
@NotNullByDefault
final class HMCLDemoShell extends StackPane implements HMCLDemoController {
    /// Title-bar height used by HMCL's `.jfx-tool-bar`.
    private static final double TITLE_HEIGHT = 40.0;

    /// Corner radius used by HMCL's decorator clip.
    private static final double WINDOW_RADIUS = 8.0;

    /// Outer transparent inset reserved for the window shadow.
    private static final double WINDOW_PADDING = 8.0;

    /// Minimum content width from HMCL `Controllers.MIN_CONTENT_WIDTH` without outer shadow.
    private static final double MIN_CONTENT_WIDTH = 802.0;

    /// Minimum content height from HMCL `Controllers.MIN_CONTENT_HEIGHT` without outer shadow.
    private static final double MIN_CONTENT_HEIGHT = 492.0;

    /// The overlay host used for transient Material feedback.
    private final M3OverlayPane overlay;

    /// The localization service shared with every page.
    private final HMCLDemoStrings strings;

    /// The deterministic application state.
    private final HMCLDemoState state;

    /// Wallpaper region behind the decorator frame.
    private final Region wallpaper = new Region();

    /// Animated page host in the decorator center slot.
    private final M3AnimatedContent pageHost = new M3AnimatedContent();

    /// HMCL-style title bar container.
    private final StackPane titleContainer = new StackPane();

    /// Left side of the title bar (back + title / brand).
    private final HBox titleLeading = new HBox(4.0);

    /// Center title text for non-home routes.
    private final M3Text titleLabel = new M3Text("", M3TextRole.TITLE_SMALL);

    /// Brand mark used on the home route.
    private final HBox brandTitle = new HBox(8.0);

    /// Brand title label.
    private final M3Text brandText = new M3Text("", M3TextRole.TITLE_SMALL);

    /// Back navigation control.
    private final M3IconButton backButton = new M3IconButton(HMCLDemoIcons.back());

    /// Help window button.
    private final M3IconButton helpButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.HELP));

    /// Minimize window button.
    private final M3IconButton minimizeButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.MINIMIZE));

    /// Close window button.
    private final M3IconButton closeButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.CLOSE));

    /// Routes retained for Back navigation.
    private final Deque<HMCLDemoRoute> backStack = new ArrayDeque<>();

    private final HMCLHomeView homeView;
    private final HMCLAccountsView accountsView;
    private final HMCLInstancesView instancesView;
    private final HMCLInstanceDetailView instanceDetailView;
    private final HMCLDownloadView downloadView;
    private final HMCLSettingsView settingsView;
    private final HMCLMultiplayerView multiplayerView;

    /// The active route.
    private HMCLDemoRoute currentRoute = new HMCLDemoRoute.Home();

    /// Drag origin for title-bar window movement.
    private double dragOffsetX;

    /// Drag origin for title-bar window movement.
    private double dragOffsetY;

    /// Creates the HMCL-style application shell.
    ///
    /// @param overlay the overlay host
    /// @param strings the localization service
    /// @param state the shared deterministic state
    HMCLDemoShell(M3OverlayPane overlay, HMCLDemoStrings strings, HMCLDemoState state) {
        this.overlay = overlay;
        this.strings = strings;
        this.state = state;

        homeView = new HMCLHomeView(strings, state, this);
        accountsView = new HMCLAccountsView(strings, state, this);
        instancesView = new HMCLInstancesView(strings, state, this);
        instanceDetailView = new HMCLInstanceDetailView(strings, state, this);
        downloadView = new HMCLDownloadView(strings, state, this);
        settingsView = new HMCLSettingsView(strings, state, this);
        multiplayerView = new HMCLMultiplayerView(strings, state, this);
        preparePage(homeView);
        preparePage(accountsView);
        preparePage(instancesView);
        preparePage(instanceDetailView);
        preparePage(downloadView);
        preparePage(settingsView);
        preparePage(multiplayerView);

        getStyleClass().add("hmcl-demo-shell");
        setPadding(new Insets(WINDOW_PADDING));
        setMinSize(MIN_CONTENT_WIDTH + WINDOW_PADDING * 2.0, MIN_CONTENT_HEIGHT + WINDOW_PADDING * 2.0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        wallpaper.getStyleClass().add("hmcl-window-wallpaper");
        wallpaper.setMouseTransparent(true);
        HMCLDemoUi.fill(wallpaper);

        StackPane parent = HMCLDemoUi.fill(new StackPane());
        parent.getStyleClass().add("hmcl-window-parent");
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(parent.widthProperty());
        clip.heightProperty().bind(parent.heightProperty());
        clip.setArcWidth(WINDOW_RADIUS);
        clip.setArcHeight(WINDOW_RADIUS);
        parent.setClip(clip);

        configureTitleBar();

        HMCLDemoUi.fill(pageHost);
        pageHost.getStyleClass().add("hmcl-route-host");
        pageHost.setFitToWidth(true);
        pageHost.setFitToHeight(true);
        pageHost.setContentTransform(HMCLDemoTransitions.forward());
        VBox.setVgrow(pageHost, Priority.ALWAYS);

        // Title is a non-growing sibling; content alone absorbs remaining height.
        VBox frame = new VBox(titleContainer, pageHost);
        frame.getStyleClass().add("hmcl-window-frame");
        HMCLDemoUi.fill(frame);

        parent.getChildren().setAll(wallpaper, frame);

        StackPane body = HMCLDemoUi.fill(new StackPane(parent));
        body.getStyleClass().add("hmcl-window-body");
        body.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, 0.4), 10.0, 0.3, 0.0, 0.0));
        getChildren().setAll(body);
        StackPane.setAlignment(body, Pos.CENTER);

        state.wallpaperProperty().addListener((observable, oldValue, newValue) -> updateWallpaper());
        strings.localeProperty().addListener((observable, oldLocale, newLocale) -> {
            refreshLocale();
            updateTitleBar();
        });
        updateWallpaper();
        showRoute(currentRoute, NavigationKind.IMMEDIATE);
    }

    /// Shell minimum size follows HMCL content metrics, never the active page list height.
    @Override
    protected double computeMinWidth(double height) {
        return MIN_CONTENT_WIDTH + WINDOW_PADDING * 2.0;
    }

    /// Shell minimum size follows HMCL content metrics, never the active page list height.
    @Override
    protected double computeMinHeight(double width) {
        return MIN_CONTENT_HEIGHT + WINDOW_PADDING * 2.0;
    }

    /// Preferred size stays at the HMCL default window, independent of page content.
    @Override
    protected double computePrefWidth(double height) {
        return computeMinWidth(height);
    }

    /// Preferred size stays at the HMCL default window, independent of page content.
    @Override
    protected double computePrefHeight(double width) {
        return computeMinHeight(width);
    }

    @Override
    public M3OverlayPane overlay() {
        return overlay;
    }

    @Override
    public void goHome() {
        if (currentRoute instanceof HMCLDemoRoute.Home) {
            backStack.clear();
            return;
        }
        backStack.clear();
        showRoute(new HMCLDemoRoute.Home(), NavigationKind.BACKWARD);
    }

    @Override
    public void openAccounts() {
        navigate(new HMCLDemoRoute.Accounts());
    }

    @Override
    public void openInstances() {
        navigate(new HMCLDemoRoute.Instances());
    }

    @Override
    public void openSelectedInstance() {
        @Nullable HMCLDemoInstance instance = state.getSelectedInstance();
        if (instance != null) {
            openInstance(instance.id());
        }
    }

    @Override
    public void openInstance(String instanceId) {
        if (state.selectInstance(instanceId)) {
            navigate(new HMCLDemoRoute.Instance(instanceId));
        }
    }

    @Override
    public void openDownload() {
        navigate(new HMCLDemoRoute.Download());
    }

    @Override
    public void openSettings() {
        navigate(new HMCLDemoRoute.Settings());
    }

    @Override
    public void openMultiplayer() {
        navigate(new HMCLDemoRoute.Multiplayer());
    }

    @Override
    public void goBack() {
        if (downloadView.consumeBack()) {
            updateTitleBar();
            return;
        }
        if (backStack.isEmpty()) {
            goHome();
            return;
        }
        showRoute(backStack.pop(), NavigationKind.BACKWARD);
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
    public void showMessage(String message) {
        overlay.showSnackbar(new M3Snackbar(message));
    }

    @Override
    public void showMessageKey(String key, Object... args) {
        showMessage(args.length == 0 ? strings.get(key) : strings.format(key, args));
    }

    @Override
    public void refreshChrome() {
        updateTitleBar();
    }

    /// Pushes the current route and shows `route` with a forward transition.
    ///
    /// @param route the destination route
    private void navigate(HMCLDemoRoute route) {
        if (route.equals(currentRoute)) {
            return;
        }
        backStack.push(currentRoute);
        showRoute(route, NavigationKind.FORWARD);
    }

    /// Displays `route` in the animated page host.
    ///
    /// @param route the route to show
    /// @param navigation the transition direction
    private void showRoute(HMCLDemoRoute route, NavigationKind navigation) {
        currentRoute = route;
        if (route instanceof HMCLDemoRoute.Instance instanceRoute) {
            state.selectInstance(instanceRoute.instanceId());
        }
        Node page = pageFor(route);
        switch (navigation) {
            case FORWARD -> pageHost.setContentTransform(HMCLDemoTransitions.forward());
            case BACKWARD -> pageHost.setContentTransform(HMCLDemoTransitions.backward());
            case IMMEDIATE -> pageHost.setContentTransform(HMCLDemoTransitions.sectionFade());
        }
        pageHost.setContent(page);
        if (navigation == NavigationKind.IMMEDIATE) {
            pageHost.snapToCurrentState();
        }
        updateTitleBar();
        if (route instanceof HMCLDemoRoute.Home && navigation != NavigationKind.FORWARD) {
            backStack.clear();
        }
    }

    /// Identifies how a route replacement should animate.
    private enum NavigationKind {
        /// Forward stack push.
        FORWARD,

        /// Back stack pop or explicit home return.
        BACKWARD,

        /// Initial presentation without motion.
        IMMEDIATE
    }

    /// Returns the retained page node for `route`.
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
            instanceDetailView.showInstance(instance.instanceId());
            return instanceDetailView;
        }
        if (route instanceof HMCLDemoRoute.Download) {
            return downloadView;
        }
        if (route instanceof HMCLDemoRoute.Settings) {
            return settingsView;
        }
        if (route instanceof HMCLDemoRoute.Multiplayer) {
            return multiplayerView;
        }
        throw new IllegalStateException("Unsupported route: " + route);
    }

    /// Ensures a page never contributes a content-driven minimum size to the decorator.
    ///
    /// @param page the page root
    private static void preparePage(Region page) {
        HMCLDemoUi.fill(page);
    }

    /// Builds the HMCL 40px title bar once.
    private void configureTitleBar() {
        titleContainer.getStyleClass().add("hmcl-window-title-bar");
        titleContainer.setMinHeight(TITLE_HEIGHT);
        titleContainer.setPrefHeight(TITLE_HEIGHT);
        titleContainer.setMaxHeight(TITLE_HEIGHT);
        titleContainer.setMinWidth(0.0);
        titleContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(titleContainer, Priority.NEVER);
        titleContainer.setOnMousePressed(this::handleWindowDragPressed);
        titleContainer.setOnMouseDragged(this::handleWindowDragged);
        titleContainer.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                @Nullable Stage stage = currentStage();
                if (stage != null) {
                    stage.setMaximized(!stage.isMaximized());
                }
            }
        });

        ImageView brandIcon = HMCLDemoAssets.imageView("img/icon-title.png", 20.0, 20.0);
        brandTitle.getStyleClass().add("hmcl-window-brand");
        brandTitle.setAlignment(Pos.CENTER_LEFT);
        brandTitle.setPadding(new Insets(0.0, 0.0, 0.0, 2.0));
        brandTitle.getChildren().setAll(brandIcon, brandText);

        titleLabel.getStyleClass().add("hmcl-window-title-label");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLeading.setAlignment(Pos.CENTER_LEFT);
        titleLeading.setPadding(new Insets(0.0, 5.0, 0.0, 5.0));
        titleLeading.setMinWidth(0.0);
        HBox.setHgrow(titleLeading, Priority.ALWAYS);

        styleWindowButton(backButton);
        styleWindowButton(helpButton);
        styleWindowButton(minimizeButton);
        styleWindowButton(closeButton);
        closeButton.getStyleClass().add("hmcl-window-close");

        backButton.setOnAction(event -> goBack());
        helpButton.setOnAction(event -> showMessageKey("snackbar.feedback"));
        minimizeButton.setOnAction(event -> {
            @Nullable Stage stage = currentStage();
            if (stage != null) {
                stage.setIconified(true);
            }
        });
        closeButton.setOnAction(event -> {
            @Nullable Stage stage = currentStage();
            if (stage != null) {
                stage.close();
            }
        });

        HBox windowButtons = new HBox(helpButton, minimizeButton, closeButton);
        windowButtons.getStyleClass().add("hmcl-window-buttons");
        windowButtons.setAlignment(Pos.CENTER_RIGHT);
        windowButtons.setMinWidth(Region.USE_PREF_SIZE);
        windowButtons.setMaxWidth(Region.USE_PREF_SIZE);

        BorderPane titleBar = new BorderPane();
        titleBar.setLeft(titleLeading);
        titleBar.setRight(windowButtons);
        BorderPane.setAlignment(titleLeading, Pos.CENTER_LEFT);
        BorderPane.setAlignment(windowButtons, Pos.CENTER_RIGHT);
        titleBar.setMinHeight(TITLE_HEIGHT);
        titleBar.setPrefHeight(TITLE_HEIGHT);
        titleBar.setMaxHeight(TITLE_HEIGHT);
        titleContainer.getChildren().setAll(titleBar);

        refreshLocale();
        updateTitleBar();
    }

    /// Applies HMCL decorator-button sizing to one title-bar icon button.
    ///
    /// @param button the button
    private static void styleWindowButton(M3IconButton button) {
        button.getStyleClass().add("hmcl-window-button");
        button.setMinSize(TITLE_HEIGHT, TITLE_HEIGHT);
        button.setPrefSize(TITLE_HEIGHT, TITLE_HEIGHT);
        button.setMaxSize(TITLE_HEIGHT, TITLE_HEIGHT);
        button.setFocusTraversable(false);
        button.setCursor(Cursor.HAND);
    }

    /// Updates title-bar labels that depend on the current locale.
    private void refreshLocale() {
        brandText.setText(strings.get("app.title"));
        backButton.setAccessibleText(strings.get("common.back"));
        helpButton.setAccessibleText(strings.get("common.help"));
        minimizeButton.setAccessibleText(strings.get("common.minimize"));
        closeButton.setAccessibleText(strings.get("common.close"));
        homeView.refreshLocale();
        accountsView.refreshLocale();
        instancesView.refreshLocale();
        instanceDetailView.refreshLocale();
        downloadView.refreshLocale();
        settingsView.refreshLocale();
        multiplayerView.refreshLocale();
    }

    /// Synchronizes title-bar navigation and title content with the active route.
    private void updateTitleBar() {
        boolean atHome = currentRoute instanceof HMCLDemoRoute.Home;
        titleLeading.getChildren().clear();
        if (atHome) {
            titleLeading.getChildren().add(brandTitle);
            titleLabel.setText("");
        } else {
            titleLeading.getChildren().add(backButton);
            titleLabel.setText(titleFor(currentRoute));
            HBox.setMargin(titleLabel, new Insets(0.0, 0.0, 0.0, 4.0));
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            titleLeading.getChildren().add(titleLabel);
        }
    }

    /// Returns the localized title for a non-home route.
    ///
    /// @param route the active route
    /// @return the title text
    private String titleFor(HMCLDemoRoute route) {
        if (route instanceof HMCLDemoRoute.Accounts) {
            return strings.get("accounts.title");
        }
        if (route instanceof HMCLDemoRoute.Instances) {
            return strings.get("instances.title");
        }
        if (route instanceof HMCLDemoRoute.Instance) {
            @Nullable HMCLDemoInstance selected = state.getSelectedInstance();
            return selected == null ? strings.get("instance.title") : selected.name();
        }
        if (route instanceof HMCLDemoRoute.Download) {
            return downloadView.titleText();
        }
        if (route instanceof HMCLDemoRoute.Settings) {
            return strings.get("settings.title");
        }
        if (route instanceof HMCLDemoRoute.Multiplayer) {
            return strings.get("multiplayer.title");
        }
        return strings.get("app.title");
    }

    /// Updates the decorator wallpaper image without letting image metrics affect layout.
    private void updateWallpaper() {
        String path = switch (state.getWallpaper()) {
            case MEADOW -> "img/wallpapers/2021-08-26.jpg";
            case CAVES -> "img/wallpapers/2016-02-25.jpg";
            case SUNSET -> "img/wallpapers/2015-06-22.jpg";
        };
        Image image = HMCLDemoAssets.image(path);
        wallpaper.setBackground(new Background(new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1.0, 1.0, true, true, false, true)
        )));
    }

    /// Captures the pointer offset used by title-bar window dragging.
    ///
    /// @param event the press event
    private void handleWindowDragPressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        @Nullable Window window = getScene() == null ? null : getScene().getWindow();
        if (window == null) {
            return;
        }
        dragOffsetX = event.getScreenX() - window.getX();
        dragOffsetY = event.getScreenY() - window.getY();
    }

    /// Moves the undecorated window while the title bar is dragged.
    ///
    /// @param event the drag event
    private void handleWindowDragged(MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            return;
        }
        @Nullable Window window = getScene() == null ? null : getScene().getWindow();
        if (window == null) {
            return;
        }
        window.setX(event.getScreenX() - dragOffsetX);
        window.setY(event.getScreenY() - dragOffsetY);
    }

    /// Returns the owning stage when available.
    ///
    /// @return the stage, or `null`
    private @Nullable Stage currentStage() {
        @Nullable Window window = getScene() == null ? null : getScene().getWindow();
        return window instanceof Stage stage ? stage : null;
    }
}
