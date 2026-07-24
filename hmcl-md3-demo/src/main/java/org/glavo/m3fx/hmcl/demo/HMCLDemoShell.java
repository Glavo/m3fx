// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
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
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/// HMCL-style undecorated window shell with one title bar and one page host.
///
/// Custom title-bar chrome hosts an [M3AdaptiveScaffold] for primary navigation and page content.
///
/// Compact widths present a bottom [org.glavo.m3fx.controls.M3NavigationBar]; medium and wider widths present a
/// leading [org.glavo.m3fx.controls.M3NavigationRail] that expands labels from the expanded breakpoint upward.
/// Secondary pages may still use their own section sidebars inside the main pane. Window chrome is self-drawn:
/// - outer window padding 8 for shadow
/// - clipped body with 16px MD3 corner radius
/// - fixed title bar that never shrinks
/// - page content uses `min size = 0` so list preferred heights cannot steal title-bar space
/// - preferred size matches the classic HMCL window; minimum size is lower so the stage can shrink and grow freely
@NotNullByDefault
final class HMCLDemoShell extends StackPane implements HMCLDemoController {
    /// Title-bar height for the soft tonal chrome bar.
    private static final double TITLE_HEIGHT = 48.0;

    /// Title-bar control hit target size (slightly smaller than the bar for tonal padding).
    private static final double TITLE_BUTTON_SIZE = 40.0;

    /// Material 3 large corner radius for the clipped window body, in logical pixels.
    ///
    /// Matches the MD3 shape scale "large" token (`16`). JavaFX [Rectangle] arc properties use the corner
    /// ellipse *diameter*, so the clip is configured with `2 * WINDOW_CORNER_RADIUS`.
    private static final double WINDOW_CORNER_RADIUS = 16.0;

    /// Outer transparent inset reserved for the window shadow.
    static final double WINDOW_PADDING = 8.0;

    /// Smallest usable content width (sidebar + readable main column), without outer shadow.
    ///
    /// Lower than HMCL's fixed 802 so the demo window can shrink; 200px sidebars still fit with a compact main pane.
    static final double MIN_CONTENT_WIDTH = 640.0;

    /// Smallest usable content height (title bar + scrollable body), without outer shadow.
    static final double MIN_CONTENT_HEIGHT = 400.0;

    /// Comfortable default content width aligned with HMCL `Controllers.MIN_CONTENT_WIDTH`.
    static final double PREF_CONTENT_WIDTH = 802.0;

    /// Comfortable default content height aligned with HMCL `Controllers.MIN_CONTENT_HEIGHT`.
    static final double PREF_CONTENT_HEIGHT = 492.0;

    /// The overlay host used for transient Material feedback.
    private final M3OverlayPane overlay;

    /// The localization service shared with every page.
    private final HMCLDemoStrings strings;

    /// The deterministic application state.
    private final HMCLDemoState state;

    /// Wallpaper region behind the decorator frame.
    private final Region wallpaper = new Region();

    /// Animated page host in the scaffold main pane.
    private final M3AnimatedContent pageHost = new M3AnimatedContent();

    /// Adaptive host that switches primary navigation between bar and rail.
    private final M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();

    /// Primary destinations shared by the bar and rail.
    private final HMCLDemoPrimaryNav primaryNav;

    /// HMCL-style title bar container.
    private final StackPane titleContainer = new StackPane();

    /// Animated title navigation area (brand or back + title), matching HMCL decorator `TransitionPane`.
    private final M3AnimatedContent titleNavHost = new M3AnimatedContent();

    /// Help window button.
    private final M3IconButton helpButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.HELP));

    /// Minimize window button.
    private final M3IconButton minimizeButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.MINIMIZE));

    /// Close window button.
    private final M3IconButton closeButton = new M3IconButton(HMCLDemoIcons.create(HMCLDemoIcons.CLOSE));

    /// Routes retained for Back navigation, with the enter transition used to open each destination.
    private final Deque<StackEntry> backStack = new ArrayDeque<>();

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

    /// Whether an edge-resize gesture is active.
    private boolean resizing;

    /// Cursor that owns the active resize gesture, or the last edge under the pointer.
    private Cursor resizeCursor = Cursor.DEFAULT;

    /// Screen X at the start of a resize gesture.
    private double resizeStartScreenX;

    /// Screen Y at the start of a resize gesture.
    private double resizeStartScreenY;

    /// Stage X at the start of a resize gesture.
    private double resizeStartStageX;

    /// Stage Y at the start of a resize gesture.
    private double resizeStartStageY;

    /// Stage width at the start of a resize gesture.
    private double resizeStartWidth;

    /// Stage height at the start of a resize gesture.
    private double resizeStartHeight;

    /// Creates the HMCL-style application shell.
    ///
    /// @param overlay the overlay host
    /// @param strings the localization service
    /// @param state the shared deterministic state
    HMCLDemoShell(M3OverlayPane overlay, HMCLDemoStrings strings, HMCLDemoState state) {
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
        primaryNav = new HMCLDemoPrimaryNav(this);
        preparePage(homeView);
        preparePage(accountsView);
        preparePage(instancesView);
        preparePage(instanceDetailView);
        preparePage(downloadView);
        preparePage(settingsView);
        preparePage(multiplayerView);

        getStyleClass().add("hmcl-demo-shell");
        setPadding(new Insets(WINDOW_PADDING));
        setPickOnBounds(true);
        setMinSize(minWindowWidth(), minWindowHeight());
        setPrefSize(prefWindowWidth(), prefWindowHeight());
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        wallpaper.getStyleClass().add("hmcl-window-wallpaper");
        wallpaper.setMouseTransparent(true);
        HMCLDemoUi.fill(wallpaper);

        StackPane parent = HMCLDemoUi.fill(new StackPane());
        parent.getStyleClass().add("hmcl-window-parent");
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(parent.widthProperty());
        clip.heightProperty().bind(parent.heightProperty());
        // Rectangle arcWidth/arcHeight are diameters; convert the MD3 radius to that API.
        clip.setArcWidth(WINDOW_CORNER_RADIUS * 2.0);
        clip.setArcHeight(WINDOW_CORNER_RADIUS * 2.0);
        parent.setClip(clip);

        configureTitleBar();
        configureAdaptiveScaffold();

        // Title is a non-growing sibling; the adaptive scaffold absorbs remaining height.
        VBox frame = new VBox(titleContainer, scaffold);
        frame.getStyleClass().add("hmcl-window-frame");
        HMCLDemoUi.fill(frame);
        VBox.setVgrow(scaffold, Priority.ALWAYS);

        parent.getChildren().setAll(wallpaper, frame);

        StackPane body = HMCLDemoUi.fill(new StackPane(parent));
        body.getStyleClass().add("hmcl-window-body");
        body.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, 0.4), 10.0, 0.3, 0.0, 0.0));
        getChildren().setAll(body);
        StackPane.setAlignment(body, Pos.CENTER);

        state.wallpaperProperty().addListener((observable, oldValue, newValue) -> updateWallpaper());
        strings.localeProperty().addListener((observable, oldLocale, newLocale) -> refreshLocale());
        installWindowResizeSupport();
        updateWallpaper();
        showRoute(currentRoute, TransitionKind.IMMEDIATE);
    }

    /// Installs the adaptive bar/rail scaffold around the animated page host.
    private void configureAdaptiveScaffold() {
        HMCLDemoUi.fill(pageHost);
        pageHost.getStyleClass().add("hmcl-route-host");
        pageHost.setFitToWidth(true);
        pageHost.setFitToHeight(true);
        // Default to soft home navigation; hierarchical push still sets FORWARD per route.
        pageHost.setContentTransform(HMCLDemoTransitions.navigation());

        scaffold.getStyleClass().add("hmcl-adaptive-scaffold");
        scaffold.setNavigationLayout(M3NavigationLayout.ADAPTIVE);
        // Page-level section sidebars live inside each route; the scaffold only hosts primary navigation.
        scaffold.setPaneLayout(M3PaneLayout.SINGLE);
        scaffold.setContentMargin(0.0);
        scaffold.setNavigationBar(primaryNav.navigationBar());
        scaffold.setNavigationRail(primaryNav.navigationRail());
        scaffold.setMainPane(pageHost);
        HMCLDemoUi.fill(scaffold);
        scaffold.breakpointProperty().addListener((observable, oldBreakpoint, newBreakpoint) ->
                primaryNav.applyBreakpoint(newBreakpoint));
        primaryNav.applyBreakpoint(scaffold.getBreakpoint());
    }

    /// Returns the undecorated stage minimum width including shadow padding.
    ///
    /// @return minimum window width in logical pixels
    static double minWindowWidth() {
        return MIN_CONTENT_WIDTH + WINDOW_PADDING * 2.0;
    }

    /// Returns the undecorated stage minimum height including shadow padding.
    ///
    /// @return minimum window height in logical pixels
    static double minWindowHeight() {
        return MIN_CONTENT_HEIGHT + WINDOW_PADDING * 2.0;
    }

    /// Returns the default stage width including shadow padding (HMCL-sized start).
    ///
    /// @return preferred window width in logical pixels
    static double prefWindowWidth() {
        return PREF_CONTENT_WIDTH + WINDOW_PADDING * 2.0;
    }

    /// Returns the default stage height including shadow padding (HMCL-sized start).
    ///
    /// @return preferred window height in logical pixels
    static double prefWindowHeight() {
        return PREF_CONTENT_HEIGHT + WINDOW_PADDING * 2.0;
    }

    /// Shell minimum size is intentionally below the classic HMCL window so users can shrink freely.
    @Override
    protected double computeMinWidth(double height) {
        return minWindowWidth();
    }

    /// Shell minimum size is intentionally below the classic HMCL window so users can shrink freely.
    @Override
    protected double computeMinHeight(double width) {
        return minWindowHeight();
    }

    /// Preferred size stays at the classic HMCL window, independent of page content.
    @Override
    protected double computePrefWidth(double height) {
        return prefWindowWidth();
    }

    /// Preferred size stays at the classic HMCL window, independent of page content.
    @Override
    protected double computePrefHeight(double width) {
        return prefWindowHeight();
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
        if (currentRoute instanceof HMCLDemoRoute.Home) {
            backStack.clear();
            return;
        }
        backStack.clear();
        // Returning to the wallpaper home uses the soft navigation reverse, not a full shared-axis pan.
        showRoute(new HMCLDemoRoute.Home(), TransitionKind.NAVIGATION_BACK);
    }

    @Override
    public void openAccounts() {
        pushRoute(new HMCLDemoRoute.Accounts(), TransitionKind.NAVIGATION);
    }

    @Override
    public void openInstances() {
        pushRoute(new HMCLDemoRoute.Instances(), TransitionKind.NAVIGATION);
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
            pushRoute(target, TransitionKind.FORWARD);
        }
    }

    @Override
    public void openDownload(HMCLDemoRoute.DownloadCategory category) {
        HMCLDemoRoute target = new HMCLDemoRoute.Download(category);
        if (currentRoute instanceof HMCLDemoRoute.Download) {
            replaceSection(target);
            return;
        }
        pushRoute(target, TransitionKind.NAVIGATION);
    }

    @Override
    public void openSettings(HMCLDemoRoute.SettingsSection section) {
        HMCLDemoRoute target = new HMCLDemoRoute.Settings(section);
        if (currentRoute instanceof HMCLDemoRoute.Settings) {
            replaceSection(target);
            return;
        }
        pushRoute(target, TransitionKind.NAVIGATION);
    }

    @Override
    public void openMultiplayer() {
        pushRoute(new HMCLDemoRoute.Multiplayer(), TransitionKind.NAVIGATION);
    }

    @Override
    public void goBack() {
        if (backStack.isEmpty()) {
            goHome();
            return;
        }
        StackEntry entry = backStack.pop();
        showRoute(entry.route(), reverseOf(entry.enterKind()));
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
        updateTitleBar(TransitionKind.SECTION);
    }

    /// Pushes the current route and shows `route` with the requested enter transition.
    ///
    /// @param route the destination route
    /// @param enterKind the enter transition stored for the matching back navigation
    private void pushRoute(HMCLDemoRoute route, TransitionKind enterKind) {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(enterKind, "enterKind");
        if (route.equals(currentRoute)) {
            return;
        }
        backStack.push(new StackEntry(currentRoute, enterKind));
        showRoute(route, enterKind);
    }

    /// Returns the reverse transition for a stored enter kind.
    ///
    /// @param enterKind the enter transition used to open the current page
    /// @return the reverse transition
    private static TransitionKind reverseOf(TransitionKind enterKind) {
        return switch (enterKind) {
            case FORWARD -> TransitionKind.BACKWARD;
            case BACKWARD -> TransitionKind.FORWARD;
            case NAVIGATION -> TransitionKind.NAVIGATION_BACK;
            case NAVIGATION_BACK -> TransitionKind.NAVIGATION;
            case SECTION, IMMEDIATE -> TransitionKind.SECTION;
        };
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

    /// Displays `route` in the animated page host.
    ///
    /// @param route the route to show
    /// @param kind the transition kind
    private void showRoute(HMCLDemoRoute route, TransitionKind kind) {
        currentRoute = route;
        if (route instanceof HMCLDemoRoute.Instance instanceRoute) {
            state.selectInstance(instanceRoute.instanceId());
        }
        Node page = pageFor(route);
        // Clear residual split offsets from a previous navigation animation on this retained page.
        clearNavigationSplit(page);
        pageHost.setContentTransform(transformFor(kind));
        pageHost.setContent(page);
        if (kind == TransitionKind.IMMEDIATE || state.isAnimationDisabled()) {
            pageHost.snapToCurrentState();
        } else if (kind == TransitionKind.NAVIGATION || kind == TransitionKind.NAVIGATION_BACK) {
            // Host fades; left/center reassemble with ±30px like HMCL DecoratorAnimatedPage NAVIGATION.
            playNavigationSplitEnter(page);
        }
        primaryNav.selectRoute(route);
        updateTitleBar(kind);
        if (route instanceof HMCLDemoRoute.Home
                && kind != TransitionKind.FORWARD
                && kind != TransitionKind.NAVIGATION) {
            backStack.clear();
        }
    }

    /// Resets left/center translation installed by [#playNavigationSplitEnter(Node)].
    ///
    /// @param page the page root
    private static void clearNavigationSplit(Node page) {
        if (!(page instanceof BorderPane borderPane)) {
            return;
        }
        @Nullable Node left = borderPane.getLeft();
        @Nullable Node center = borderPane.getCenter();
        if (left != null) {
            left.setTranslateX(0.0);
        }
        if (center != null) {
            center.setTranslateX(0.0);
        }
    }

    /// Plays the HMCL-style left/center reassemble for a two-pane page after a soft navigation fade.
    ///
    /// @param page the incoming page root
    private void playNavigationSplitEnter(Node page) {
        if (!(page instanceof BorderPane borderPane) || state.isAnimationDisabled()) {
            return;
        }
        @Nullable Node left = borderPane.getLeft();
        @Nullable Node center = borderPane.getCenter();
        if (left == null && center == null) {
            return;
        }

        double distance = HMCLDemoTransitions.navigationSplitDistance();
        List<KeyValue> startValues = new ArrayList<>(2);
        List<KeyValue> endValues = new ArrayList<>(2);
        if (left != null) {
            left.setTranslateX(-distance);
            startValues.add(new KeyValue(left.translateXProperty(), -distance, Interpolator.EASE_BOTH));
            endValues.add(new KeyValue(left.translateXProperty(), 0.0, Interpolator.EASE_BOTH));
        }
        if (center != null) {
            center.setTranslateX(distance);
            startValues.add(new KeyValue(center.translateXProperty(), distance, Interpolator.EASE_BOTH));
            endValues.add(new KeyValue(center.translateXProperty(), 0.0, Interpolator.EASE_BOTH));
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, startValues.toArray(KeyValue[]::new)),
                // Second half of a short navigation beat (matches HMCL half-duration reassemble).
                new KeyFrame(Duration.millis(160.0), endValues.toArray(KeyValue[]::new))
        );
        timeline.play();
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
            case FORWARD -> HMCLDemoTransitions.forward();
            case BACKWARD -> HMCLDemoTransitions.backward();
            case NAVIGATION -> HMCLDemoTransitions.navigation();
            case NAVIGATION_BACK -> HMCLDemoTransitions.navigationBack();
            case SECTION -> HMCLDemoTransitions.sectionUp();
            case IMMEDIATE -> HMCLDemoTransitions.none();
        };
    }

    /// One retained back-stack entry.
    ///
    /// @param route the previous route
    /// @param enterKind the enter transition used to leave `route`
    private record StackEntry(HMCLDemoRoute route, TransitionKind enterKind) {
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

    /// Ensures a page never contributes a content-driven minimum size to the decorator.
    ///
    /// @param page the page root
    private static void preparePage(Region page) {
        HMCLDemoUi.fill(page);
    }

    /// Builds the HMCL-style title bar once, with an animated navigation center matching decorator transitions.
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

        titleNavHost.getStyleClass().add("hmcl-window-title-nav");
        titleNavHost.setAlignment(Pos.CENTER_LEFT);
        titleNavHost.setMinHeight(TITLE_HEIGHT);
        titleNavHost.setPrefHeight(TITLE_HEIGHT);
        titleNavHost.setMaxHeight(TITLE_HEIGHT);
        titleNavHost.setMinWidth(0.0);
        titleNavHost.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setAlignment(titleNavHost, Pos.CENTER_LEFT);
        BorderPane.setMargin(titleNavHost, new Insets(0.0, 8.0, 0.0, 0.0));

        styleWindowButton(helpButton);
        styleWindowButton(minimizeButton);
        styleWindowButton(closeButton);
        closeButton.getStyleClass().add("hmcl-window-close");

        helpButton.setOnAction(event -> openSettings(HMCLDemoRoute.SettingsSection.HELP));
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
        titleBar.setCenter(titleNavHost);
        titleBar.setRight(windowButtons);
        BorderPane.setAlignment(windowButtons, Pos.CENTER_RIGHT);
        titleBar.setMinHeight(TITLE_HEIGHT);
        titleBar.setPrefHeight(TITLE_HEIGHT);
        titleBar.setMaxHeight(TITLE_HEIGHT);
        titleContainer.getChildren().setAll(titleBar);

        refreshLocale();
        updateTitleBar(TransitionKind.IMMEDIATE);
    }

    /// Applies soft icon-button sizing to one title-bar control.
    ///
    /// @param button the button
    private static void styleWindowButton(M3IconButton button) {
        button.getStyleClass().add("hmcl-window-button");
        button.setMinSize(TITLE_BUTTON_SIZE, TITLE_BUTTON_SIZE);
        button.setPrefSize(TITLE_BUTTON_SIZE, TITLE_BUTTON_SIZE);
        button.setMaxSize(TITLE_BUTTON_SIZE, TITLE_BUTTON_SIZE);
        button.setFocusTraversable(false);
        button.setCursor(Cursor.HAND);
    }

    /// Updates title-bar labels that depend on the current locale.
    private void refreshLocale() {
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
        primaryNav.refreshLocale();
        // Rebuild title content for the new language without a direction-specific pan.
        updateTitleBar(TransitionKind.SECTION);
    }

    /// Synchronizes animated title-bar navigation content with the active route.
    ///
    /// @param kind the page transition that owns this title update
    private void updateTitleBar(TransitionKind kind) {
        titleNavHost.setContentTransform(titleTransformFor(kind));
        titleNavHost.setContent(createTitleNavContent());
        if (kind == TransitionKind.IMMEDIATE || state.isAnimationDisabled()) {
            titleNavHost.snapToCurrentState();
        }
    }

    /// Returns the HMCL decorator title transform for a page transition kind.
    ///
    /// @param kind the page transition kind
    /// @return the title navigation transform
    private M3ContentTransform titleTransformFor(TransitionKind kind) {
        if (state.isAnimationDisabled() || kind == TransitionKind.IMMEDIATE) {
            return HMCLDemoTransitions.none();
        }
        return switch (kind) {
            case FORWARD, NAVIGATION -> HMCLDemoTransitions.titleNext();
            case BACKWARD, NAVIGATION_BACK -> HMCLDemoTransitions.titlePrevious();
            case SECTION -> HMCLDemoTransitions.titleFade();
            case IMMEDIATE -> HMCLDemoTransitions.none();
        };
    }

    /// Creates a fresh title navigation row so enter/exit holders never share nodes.
    ///
    /// @return the leading title content
    private Node createTitleNavContent() {
        HBox leading = new HBox(4.0);
        leading.getStyleClass().add("hmcl-window-title-leading");
        leading.setAlignment(Pos.CENTER_LEFT);
        leading.setPadding(new Insets(0.0, 5.0, 0.0, 5.0));
        leading.setMinWidth(0.0);
        leading.setMaxWidth(Double.MAX_VALUE);
        leading.setMinHeight(TITLE_HEIGHT);
        leading.setPrefHeight(TITLE_HEIGHT);
        leading.setMaxHeight(TITLE_HEIGHT);

        if (currentRoute instanceof HMCLDemoRoute.Home) {
            ImageView brandIcon = HMCLDemoAssets.imageView("img/icon-title.png", 20.0, 20.0);
            M3Text brandText = new M3Text(strings.get("app.title"), M3TextRole.TITLE_SMALL);
            brandText.getStyleClass().add("hmcl-window-title-label");
            HBox brand = new HBox(8.0, brandIcon, brandText);
            brand.getStyleClass().add("hmcl-window-brand");
            brand.setAlignment(Pos.CENTER_LEFT);
            brand.setPadding(new Insets(0.0, 0.0, 0.0, 2.0));
            leading.getChildren().add(brand);
            return leading;
        }

        M3IconButton back = new M3IconButton(HMCLDemoIcons.back());
        styleWindowButton(back);
        back.setAccessibleText(strings.get("common.back"));
        back.setOnAction(event -> goBack());

        M3Text title = new M3Text(titleFor(currentRoute), M3TextRole.TITLE_SMALL);
        title.getStyleClass().add("hmcl-window-title-label");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setMargin(title, new Insets(0.0, 0.0, 0.0, 4.0));
        HBox.setHgrow(title, Priority.ALWAYS);
        leading.getChildren().addAll(back, title);
        return leading;
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
        if (event.getButton() != MouseButton.PRIMARY || isResizeCursor(resizeCursor)) {
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
        if (!event.isPrimaryButtonDown() || resizing || isResizeCursor(resizeCursor)) {
            return;
        }
        @Nullable Window window = getScene() == null ? null : getScene().getWindow();
        if (window == null) {
            return;
        }
        window.setX(event.getScreenX() - dragOffsetX);
        window.setY(event.getScreenY() - dragOffsetY);
    }

    /// Installs edge and corner resize for the transparent undecorated stage.
    ///
    /// Transparent stages have no system chrome, so the outer padding acts as the resize grip, matching HMCL's
    /// decorator inset behavior.
    private void installWindowResizeSupport() {
        addEventFilter(MouseEvent.MOUSE_MOVED, this::handleResizeMouseMoved);
        addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleResizeMousePressed);
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleResizeMouseDragged);
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleResizeMouseReleased);
        addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            if (!resizing) {
                resizeCursor = Cursor.DEFAULT;
                setCursor(Cursor.DEFAULT);
            }
        });
    }

    /// Updates the resize cursor when the pointer is over a stage edge or corner.
    private void handleResizeMouseMoved(MouseEvent event) {
        @Nullable Stage stage = currentStage();
        if (stage == null || !stage.isResizable() || stage.isMaximized() || stage.isFullScreen()) {
            resizeCursor = Cursor.DEFAULT;
            setCursor(Cursor.DEFAULT);
            return;
        }
        Point2D local = localPointer(event);
        resizeCursor = resizeCursorAt(local.getX(), local.getY());
        setCursor(resizeCursor);
    }

    /// Begins an edge-resize gesture when pressing on a resize grip.
    private void handleResizeMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        @Nullable Stage stage = currentStage();
        if (stage == null || !stage.isResizable() || stage.isMaximized() || stage.isFullScreen()) {
            return;
        }
        Point2D local = localPointer(event);
        Cursor cursor = resizeCursorAt(local.getX(), local.getY());
        if (!isResizeCursor(cursor)) {
            return;
        }
        resizing = true;
        resizeCursor = cursor;
        setCursor(cursor);
        resizeStartScreenX = event.getScreenX();
        resizeStartScreenY = event.getScreenY();
        resizeStartStageX = stage.getX();
        resizeStartStageY = stage.getY();
        resizeStartWidth = stage.getWidth();
        resizeStartHeight = stage.getHeight();
        event.consume();
    }

    /// Applies the active edge-resize gesture to the owning stage.
    private void handleResizeMouseDragged(MouseEvent event) {
        if (!resizing || !event.isPrimaryButtonDown()) {
            return;
        }
        @Nullable Stage stage = currentStage();
        if (stage == null || !stage.isResizable() || stage.isMaximized() || stage.isFullScreen()) {
            resizing = false;
            return;
        }

        double dx = event.getScreenX() - resizeStartScreenX;
        double dy = event.getScreenY() - resizeStartScreenY;
        double minWidth = Math.max(stage.getMinWidth(), computeMinWidth(-1.0));
        double minHeight = Math.max(stage.getMinHeight(), computeMinHeight(-1.0));

        double newWidth = resizeStartWidth;
        double newHeight = resizeStartHeight;
        double newX = resizeStartStageX;
        double newY = resizeStartStageY;

        if (resizeCursor == Cursor.E_RESIZE || resizeCursor == Cursor.NE_RESIZE || resizeCursor == Cursor.SE_RESIZE) {
            newWidth = Math.max(minWidth, resizeStartWidth + dx);
        }
        if (resizeCursor == Cursor.S_RESIZE || resizeCursor == Cursor.SE_RESIZE || resizeCursor == Cursor.SW_RESIZE) {
            newHeight = Math.max(minHeight, resizeStartHeight + dy);
        }
        if (resizeCursor == Cursor.W_RESIZE || resizeCursor == Cursor.NW_RESIZE || resizeCursor == Cursor.SW_RESIZE) {
            newWidth = Math.max(minWidth, resizeStartWidth - dx);
            newX = resizeStartStageX + (resizeStartWidth - newWidth);
        }
        if (resizeCursor == Cursor.N_RESIZE || resizeCursor == Cursor.NW_RESIZE || resizeCursor == Cursor.NE_RESIZE) {
            newHeight = Math.max(minHeight, resizeStartHeight - dy);
            newY = resizeStartStageY + (resizeStartHeight - newHeight);
        }

        // Width and height must be assigned together to avoid JDK-8344372 layout glitches.
        stage.setX(newX);
        stage.setY(newY);
        stage.setWidth(newWidth);
        stage.setHeight(newHeight);
        event.consume();
    }

    /// Ends an edge-resize gesture.
    private void handleResizeMouseReleased(MouseEvent event) {
        if (!resizing) {
            return;
        }
        resizing = false;
        Point2D local = localPointer(event);
        resizeCursor = resizeCursorAt(local.getX(), local.getY());
        setCursor(resizeCursor);
        event.consume();
    }

    /// Converts a mouse event to shell-local coordinates.
    private Point2D localPointer(MouseEvent event) {
        return sceneToLocal(event.getSceneX(), event.getSceneY());
    }

    /// Returns the resize cursor for a shell-local pointer position.
    private Cursor resizeCursorAt(double x, double y) {
        double width = getWidth();
        double height = getHeight();
        if (width <= 0.0 || height <= 0.0) {
            return Cursor.DEFAULT;
        }

        double edge = WINDOW_PADDING;
        double corner = WINDOW_PADDING + 10.0;
        boolean left = x >= 0.0 && x <= edge;
        boolean right = x < width && x >= width - edge;
        boolean top = y >= 0.0 && y <= edge;
        boolean bottom = y < height && y >= height - edge;

        if (right) {
            if (y < corner) {
                return Cursor.NE_RESIZE;
            }
            if (y > height - corner) {
                return Cursor.SE_RESIZE;
            }
            return Cursor.E_RESIZE;
        }
        if (left) {
            if (y < corner) {
                return Cursor.NW_RESIZE;
            }
            if (y > height - corner) {
                return Cursor.SW_RESIZE;
            }
            return Cursor.W_RESIZE;
        }
        if (top) {
            if (x < corner) {
                return Cursor.NW_RESIZE;
            }
            if (x > width - corner) {
                return Cursor.NE_RESIZE;
            }
            return Cursor.N_RESIZE;
        }
        if (bottom) {
            if (x < corner) {
                return Cursor.SW_RESIZE;
            }
            if (x > width - corner) {
                return Cursor.SE_RESIZE;
            }
            return Cursor.S_RESIZE;
        }
        return Cursor.DEFAULT;
    }

    /// Returns whether `cursor` is one of the eight stage-resize cursors.
    private static boolean isResizeCursor(Cursor cursor) {
        return cursor == Cursor.N_RESIZE
                || cursor == Cursor.S_RESIZE
                || cursor == Cursor.E_RESIZE
                || cursor == Cursor.W_RESIZE
                || cursor == Cursor.NE_RESIZE
                || cursor == Cursor.NW_RESIZE
                || cursor == Cursor.SE_RESIZE
                || cursor == Cursor.SW_RESIZE;
    }

    /// Returns the owning stage when available.
    ///
    /// @return the stage, or `null`
    private @Nullable Stage currentStage() {
        @Nullable Window window = getScene() == null ? null : getScene().getWindow();
        return window instanceof Stage stage ? stage : null;
    }

    /// Identifies how a route replacement should animate.
    private enum TransitionKind {
        /// Hierarchical push (for example instance detail).
        FORWARD,

        /// Hierarchical pop.
        BACKWARD,

        /// Ordinary shell navigation from home (HMCL `NAVIGATION`).
        NAVIGATION,

        /// Reverse of [#NAVIGATION].
        NAVIGATION_BACK,

        /// In-page section replacement.
        SECTION,

        /// Initial presentation without motion.
        IMMEDIATE
    }
}
