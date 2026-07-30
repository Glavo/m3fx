// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3AnimatedVisibility;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.glavo.m3fx.animation.M3VisibilityState;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3Breakpoint;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.glavo.m3fx.layout.M3PaneRole;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/// A JavaFX adaptation of the AndroidX Material 3 Catalog navigation and presentation model.
///
/// The application uses the Catalog's three-level information architecture: an alphabetical component grid opens
/// a component description and example list, and each example opens as a focused full-page interactive specimen.
/// Navigation uses a standard drawer that shares space with content at expanded breakpoints and a modal drawer at
/// compact and medium breakpoints.
/// Theme and display settings live in a modal bottom sheet so they remain available without replacing the
/// Catalog's navigation hierarchy.
@NotNullByDefault
public final class M3FXCatalogApp extends Application {
    /// The initial scene width in logical pixels.
    private static final double INITIAL_WIDTH = 1_080.0;

    /// The initial scene height in logical pixels.
    private static final double INITIAL_HEIGHT = 800.0;

    /// The maximum theme-settings sheet height in logical pixels.
    private static final double SETTINGS_SHEET_MAX_HEIGHT = 680.0;

    /// The fixed width of the persistent Catalog sidebar in expanded layouts.
    private static final double SIDEBAR_WIDTH = 360.0;

    /// The content transform used when navigating deeper into the route hierarchy.
    private static final M3ContentTransform FORWARD_ROUTE_TRANSFORM = new M3ContentTransform(
            M3EnterTransition.fade(0.0).and(M3EnterTransition.slideFrom(M3TransitionEdge.END, 32.0)),
            M3ExitTransition.fade(0.0).and(M3ExitTransition.slideTo(M3TransitionEdge.START, 16.0)),
            null,
            0.0
    );

    /// The content transform used when returning toward the Catalog home route.
    private static final M3ContentTransform BACKWARD_ROUTE_TRANSFORM = new M3ContentTransform(
            M3EnterTransition.fade(0.0).and(M3EnterTransition.slideFrom(M3TransitionEdge.START, 32.0)),
            M3ExitTransition.fade(0.0).and(M3ExitTransition.slideTo(M3TransitionEdge.END, 16.0)),
            null,
            0.0
    );

    /// The content transform used for route rebuilds that do not represent navigation.
    private static final M3ContentTransform INSTANT_ROUTE_TRANSFORM = new M3ContentTransform(
            M3EnterTransition.none(),
            M3ExitTransition.none(),
            null,
            0.0
    );

    /// The default M3 seed color.
    private static final Color DEFAULT_SEED_COLOR = Color.web("#6750A4");

    /// Seed colors available from the theme settings sheet.
    private static final @Unmodifiable List<Color> SEED_COLORS = List.of(
            DEFAULT_SEED_COLOR,
            Color.web("#006A6A"),
            Color.web("#9C4146"),
            Color.web("#426900"),
            Color.web("#79536A")
    );

    /// The immutable Catalog component registry.
    private final @Unmodifiable List<CatalogComponent> components = CatalogComponents.all();

    /// The persistent responsive navigation for components and examples.
    private final CatalogSidebar sidebar = new CatalogSidebar(components, this::navigate, this::navigateHome);

    /// Routes retained for top-app-bar back navigation.
    private final Deque<CatalogRoute> backStack = new ArrayDeque<>();

    /// Component names marked as favorites during this application session.
    private final Set<String> favorites = new HashSet<>();

    /// Restorable browser state for the Home route.
    private CatalogBrowserState homeBrowserState = CatalogBrowserState.INITIAL;

    /// Restorable browser state retained independently for each component route.
    private final Map<CatalogComponent, CatalogBrowserState> componentBrowserStates = new HashMap<>();

    /// Restorable vertical scroll positions retained independently for each example route.
    private final Map<CatalogRoute.Example, Double> exampleScrollPositions = new HashMap<>();

    /// The stable root containing application content and in-scene presentation layers.
    private final M3OverlayPane root = new M3OverlayPane();

    /// The persistent app-bar and route-content scaffold.
    private final M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();

    /// The shared Material top app bar.
    private final M3TopAppBar topAppBar = new M3TopAppBar("M3FX");

    /// The host for the active route content.
    private final M3AnimatedContent routeHost = new M3AnimatedContent();

    /// The scrim behind the compact modal navigation drawer.
    private final M3Scrim sidebarScrim = new M3Scrim();

    /// The interruptible logical-start slide transition used by compact modal navigation.
    private final M3AnimatedVisibility sidebarVisibility = new M3AnimatedVisibility();

    /// The coordinated full-size layer containing compact modal navigation.
    private final StackPane sidebarLayer = new StackPane();

    /// The active compact sidebar presentation, or `null` while the layer is detached.
    private @Nullable M3OverlayPane.OverlayHandle sidebarOverlayHandle;

    /// The modal theme-settings scrim.
    private final M3Scrim settingsScrim = new M3Scrim();

    /// The modal theme-settings bottom sheet.
    private final M3BottomSheet settingsSheet = new M3BottomSheet("Theme");

    /// The coordinated full-size layer containing the settings scrim and sheet.
    private final StackPane settingsLayer = new StackPane();

    /// The active settings presentation, or `null` while the settings layer is detached.
    private @Nullable M3OverlayPane.OverlayHandle settingsOverlayHandle;

    /// The brightness control retained so reset can synchronize its state.
    private final M3Switch darkThemeSwitch = new M3Switch("Dark theme");

    /// The component-profile control retained so reset can synchronize its state.
    private final M3Switch expressiveSwitch = new M3Switch("Expressive components");

    /// The orientation control retained so reset can synchronize its state.
    private final M3Switch rightToLeftSwitch = new M3Switch("Right-to-left layout");

    /// The motion control retained so reset can synchronize its state.
    private final M3Switch reducedMotionSwitch = new M3Switch("Reduced motion");

    /// The Expressive-label control retained so reset can synchronize its state.
    private final M3Switch markExpressiveSwitch = new M3Switch("Mark Expressive examples");

    /// The Expressive-filter control retained so reset can synchronize its state.
    private final M3Switch expressiveOnlySwitch = new M3Switch("Show only Expressive components");

    /// The scene after startup, or `null` before the primary stage is initialized.
    private @Nullable Scene scene;

    /// The route currently displayed by the scaffold.
    private CatalogRoute currentRoute = new CatalogRoute.Home();

    /// The seed color used by the active theme.
    private Color seedColor = DEFAULT_SEED_COLOR;

    /// The active Material component profile.
    private M3Profile profile = M3Profile.BASELINE_2021;

    /// The active theme brightness.
    private Brightness brightness = Brightness.LIGHT;

    /// Whether the root subtree uses right-to-left node orientation.
    private boolean rightToLeft;

    /// Whether the Catalog requests reduced motion from M3FX controls.
    private boolean reducedMotion;

    /// Whether Expressive components and examples receive a visible marker.
    private boolean markExpressive = true;

    /// Whether the home grid is filtered to components that have Expressive examples.
    private boolean expressiveOnly;

    /// Whether the current breakpoint displays the standard persistent drawer.
    private boolean permanentSidebar;

    /// Creates a Catalog application instance for the JavaFX launcher.
    public M3FXCatalogApp() {
    }

    /// Starts the Catalog in the supplied JavaFX primary stage.
    ///
    /// @param stage the JavaFX primary stage
    @Override
    public void start(Stage stage) {
        configureScaffold();
        configureSidebarOverlay();
        configureSettingsOverlay();

        root.getStyleClass().add("catalog-root");
        root.setContent(scaffold);

        Scene activeScene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
        activeScene.getStylesheets().add(Objects.requireNonNull(
                M3FXCatalogApp.class.getResource("m3fx-catalog.css"),
                "m3fx-catalog.css"
        ).toExternalForm());
        scene = activeScene;
        applyTheme();
        applyDisplaySettings();
        renderCurrentRoute();

        stage.setTitle("M3FX Catalog");
        stage.setMinWidth(460.0);
        stage.setMinHeight(560.0);
        stage.setScene(activeScene);
        stage.show();
    }

    /// Configures the shared top app bar and route host.
    private void configureScaffold() {
        scaffold.getStyleClass().add("catalog-scaffold");
        scaffold.setContentMargin(0.0);
        topAppBar.getStyleClass().add("catalog-top-app-bar");
        scaffold.setTopBar(topAppBar);

        scaffold.setLeadingPane(sidebar);
        scaffold.setFixedLeadingPaneWidth(SIDEBAR_WIDTH);
        scaffold.setPaneSpacing(0.0);
        scaffold.setActivePane(M3PaneRole.MAIN);
        scaffold.setPaneLayout(M3PaneLayout.SINGLE);
        sidebar.setVariant(M3NavigationDrawerVariant.STANDARD);
        scaffold.widthProperty().addListener((observable, oldWidth, newWidth) ->
                updateCatalogPaneLayout(newWidth.doubleValue()));

        routeHost.setAlignment(Pos.TOP_CENTER);
        routeHost.setFitToWidth(true);
        routeHost.setFitToHeight(true);
        routeHost.setMinSize(0.0, 0.0);
        routeHost.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        routeHost.getStyleClass().add("catalog-route-host");
        scaffold.setMainPane(routeHost);
    }

    /// Selects modal navigation below the expanded breakpoint and standard navigation at wider sizes.
    ///
    /// @param width the scaffold width in logical pixels
    private void updateCatalogPaneLayout(double width) {
        boolean permanent = M3Breakpoint.forWidth(Math.max(0.0, width)).getRecommendedPaneCount() > 1;
        boolean presentationChanged = permanentSidebar != permanent;
        permanentSidebar = permanent;
        if (permanent) {
            dismissModalSidebarImmediately();
            sidebar.setVariant(M3NavigationDrawerVariant.STANDARD);
            boolean attached = scaffold.getLeadingPane() == sidebar;
            if (!attached) {
                scaffold.setLeadingPane(sidebar);
            }
            scaffold.setPaneLayout(M3PaneLayout.FIXED_LEADING);
            if (!attached) {
                sidebar.refresh(currentRoute, expressiveOnly);
            }
        } else {
            if (scaffold.getLeadingPane() == sidebar) {
                scaffold.setLeadingPane(null);
            }
            scaffold.setActivePane(M3PaneRole.MAIN);
            scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            sidebar.setVariant(M3NavigationDrawerVariant.MODAL);
        }
        if (scene != null && presentationChanged) {
            configureTopAppBar();
        }
    }

    /// Configures the compact modal navigation layer and its dismissal scrim.
    private void configureSidebarOverlay() {
        sidebarScrim.getStyleClass().add("catalog-sidebar-scrim");
        sidebarScrim.setShown(false);
        sidebarScrim.setOnAction(event -> hideModalSidebar());

        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        sidebarVisibility.getStyleClass().add("catalog-sidebar-visibility");
        sidebarVisibility.setAlignment(Pos.CENTER_LEFT);
        sidebarVisibility.setSizeTransform(null);
        sidebarVisibility.setEnterTransition(
                M3EnterTransition.slideFrom(M3TransitionEdge.START, SIDEBAR_WIDTH)
        );
        sidebarVisibility.setExitTransition(
                M3ExitTransition.slideTo(M3TransitionEdge.START, SIDEBAR_WIDTH)
        );
        sidebarVisibility.setShowing(false);
        sidebarVisibility.stateProperty().addListener((observable, oldState, newState) ->
                removeSidebarLayerWhenHidden());
        sidebarScrim.visibleProperty().addListener((observable, oldVisible, visible) ->
                removeSidebarLayerWhenHidden());

        sidebarLayer.getStyleClass().add("catalog-sidebar-layer");
        sidebarLayer.setPickOnBounds(false);
        sidebarLayer.getChildren().setAll(sidebarScrim, sidebarVisibility);
        StackPane.setAlignment(sidebarVisibility, Pos.CENTER_LEFT);
    }

    /// Configures the modal theme settings sheet and its coordinated scrim.
    private void configureSettingsOverlay() {
        settingsScrim.getStyleClass().add("catalog-settings-scrim");
        settingsScrim.setShown(false);
        settingsScrim.setOnAction(event -> hideSettings());

        settingsSheet.getStyleClass().add("catalog-settings-sheet");
        settingsSheet.setVariant(M3SheetVariant.MODAL);
        settingsSheet.setPrefWidth(640.0);
        settingsSheet.setMaxWidth(640.0);
        settingsSheet.setMaxHeight(SETTINGS_SHEET_MAX_HEIGHT);
        settingsSheet.setRestoreFocusOnHide(false);

        ScrollPane settingsScroll = new ScrollPane(createSettingsContent());
        settingsScroll.getStyleClass().add("catalog-settings-scroll");
        settingsScroll.setFitToWidth(true);
        settingsScroll.setMinHeight(0.0);
        M3ScrollPanes.style(settingsScroll);
        M3ScrollPanes.enableSmoothScrolling(settingsScroll);
        settingsSheet.setContent(settingsScroll);
        settingsSheet.setShown(false);
        StackPane.setAlignment(settingsSheet, Pos.BOTTOM_CENTER);
        StackPane.setMargin(settingsSheet, new Insets(16.0));

        settingsLayer.setPickOnBounds(false);
        settingsLayer.getChildren().setAll(settingsScrim, settingsSheet);
        settingsScrim.visibleProperty().addListener((observable, oldVisible, visible) ->
                removeSettingsLayerWhenHidden());
        settingsSheet.visibleProperty().addListener((observable, oldVisible, visible) ->
                removeSettingsLayerWhenHidden());

        M3Button closeButton = new M3Button("Done", M3ButtonVariant.TEXT);
        closeButton.setOnAction(event -> hideSettings());
        settingsSheet.getActions().add(closeButton);
    }

    /// Creates the settings content shown by the modal bottom sheet.
    ///
    /// @return the configured settings content
    private Node createSettingsContent() {
        darkThemeSwitch.setOnAction(event -> {
            brightness = darkThemeSwitch.isSelected() ? Brightness.DARK : Brightness.LIGHT;
            applyTheme();
        });
        expressiveSwitch.setOnAction(event -> {
            profile = expressiveSwitch.isSelected() ? M3Profile.EXPRESSIVE_2025 : M3Profile.BASELINE_2021;
            applyTheme();
            renderCurrentRoute();
        });
        rightToLeftSwitch.setOnAction(event -> {
            rightToLeft = rightToLeftSwitch.isSelected();
            applyDisplaySettings();
        });
        reducedMotionSwitch.setOnAction(event -> {
            reducedMotion = reducedMotionSwitch.isSelected();
            applyDisplaySettings();
        });
        markExpressiveSwitch.setSelected(markExpressive);
        markExpressiveSwitch.setOnAction(event -> {
            markExpressive = markExpressiveSwitch.isSelected();
            renderCurrentRoute();
        });
        expressiveOnlySwitch.setOnAction(event -> {
            expressiveOnly = expressiveOnlySwitch.isSelected();
            if (currentRoute instanceof CatalogRoute.Home) {
                renderCurrentRoute();
            } else {
                navigateHome();
            }
        });

        FlowPane seedPicker = new FlowPane(12.0, 12.0);
        seedPicker.getStyleClass().add("catalog-seed-picker");
        for (Color color : SEED_COLORS) {
            M3IconButton seedButton = new M3IconButton();
            seedButton.getStyleClass().add("catalog-seed-button");
            seedButton.setAccessibleText("Use seed color " + toHex(color));
            seedButton.setStyle("-fx-background-color: " + toHex(color) + ";");
            seedButton.setOnAction(event -> {
                seedColor = color;
                applyTheme();
            });
            seedPicker.getChildren().add(seedButton);
        }

        M3Button resetButton = new M3Button(
                "Reset settings",
                CatalogIcons.create(CatalogIcons.RESET),
                M3ButtonVariant.TEXT
        );
        resetButton.getStyleClass().add("catalog-reset-button");
        resetButton.setOnAction(event -> resetSettings());

        VBox content = new VBox(
                12.0,
                settingHeading("Color"),
                seedPicker,
                darkThemeSwitch,
                settingHeading("Components"),
                expressiveSwitch,
                markExpressiveSwitch,
                expressiveOnlySwitch,
                settingHeading("Display"),
                rightToLeftSwitch,
                reducedMotionSwitch,
                resetButton
        );
        content.getStyleClass().add("catalog-settings-content");
        return content;
    }

    /// Creates a heading in the settings sheet.
    ///
    /// @param text the heading text
    /// @return the configured heading node
    private static M3Text settingHeading(String text) {
        M3Text heading = new M3Text(text, M3TextRole.TITLE_MEDIUM);
        heading.getStyleClass().add("catalog-settings-heading");
        return heading;
    }

    /// Navigates to a route and records the previous route for back navigation.
    ///
    /// @param route the destination route
    void navigate(CatalogRoute route) {
        Objects.requireNonNull(route, "route");
        if (route.equals(currentRoute)) {
            hideModalSidebar();
            return;
        }
        backStack.push(currentRoute);
        currentRoute = route;
        scaffold.setActivePane(M3PaneRole.MAIN);
        renderCurrentRoute(FORWARD_ROUTE_TRANSFORM);
        hideModalSidebar();
    }

    /// Returns to the preceding route, or to Home if no route is retained.
    void navigateBack() {
        currentRoute = backStack.isEmpty() ? new CatalogRoute.Home() : backStack.pop();
        scaffold.setActivePane(M3PaneRole.MAIN);
        renderCurrentRoute(BACKWARD_ROUTE_TRANSFORM);
        hideModalSidebar();
    }

    /// Clears navigation history and displays the Home route.
    void navigateHome() {
        boolean routeChanged = !(currentRoute instanceof CatalogRoute.Home);
        backStack.clear();
        currentRoute = new CatalogRoute.Home();
        scaffold.setActivePane(M3PaneRole.MAIN);
        renderCurrentRoute(routeChanged ? BACKWARD_ROUTE_TRANSFORM : INSTANT_ROUTE_TRANSFORM);
        hideModalSidebar();
    }

    /// Rebuilds the active route and synchronizes app-bar actions with its context.
    private void renderCurrentRoute() {
        renderCurrentRoute(INSTANT_ROUTE_TRANSFORM);
    }

    /// Rebuilds the active route using the supplied content-replacement transform.
    ///
    /// @param contentTransform the transform for the route replacement
    private void renderCurrentRoute(M3ContentTransform contentTransform) {
        configureTopAppBar();
        Node content;
        if (currentRoute instanceof CatalogRoute.Home) {
            content = CatalogViews.createHome(
                    components,
                    favorites,
                    homeBrowserState,
                    state -> homeBrowserState = state,
                    this::navigate,
                    expressiveOnly,
                    markExpressive
            );
        } else if (currentRoute instanceof CatalogRoute.Component componentRoute) {
            CatalogComponent component = componentRoute.component();
            content = CatalogViews.createComponent(
                    component,
                    componentBrowserStates.getOrDefault(component, CatalogBrowserState.INITIAL),
                    state -> componentBrowserStates.put(component, state),
                    this::navigate,
                    this::openDocument,
                    markExpressive
            );
        } else {
            CatalogRoute.Example exampleRoute = (CatalogRoute.Example) currentRoute;
            content = CatalogViews.createExample(
                    exampleRoute.component(),
                    exampleRoute.example(),
                    exampleScrollPositions.getOrDefault(exampleRoute, 0.0),
                    position -> exampleScrollPositions.put(exampleRoute, position),
                    this::navigateBack,
                    this::openDocument
            );
        }
        sidebar.refresh(currentRoute, expressiveOnly);
        routeHost.setContentTransform(contentTransform);
        routeHost.setContent(content);
    }

    /// Configures the shared top app bar for the active route.
    private void configureTopAppBar() {
        topAppBar.setTitle(routeTitle());
        topAppBar.setNavigation(isPermanentSidebar() ? null : createSidebarButton());
        topAppBar.getActions().clear();

        @Nullable CatalogComponent component = routeComponent();
        if (component != null) {
            topAppBar.getActions().add(createFavoriteButton(component));
        }
        topAppBar.getActions().add(createThemeButton());
        @Nullable String sourceUrl = currentRoute instanceof CatalogRoute.Example exampleRoute
                ? exampleRoute.example().sourceUrl()
                : component == null ? null : component.sourceUrl();
        topAppBar.getActions().add(createOverflowButton(component, sourceUrl));
    }

    /// Creates the top-app-bar action that toggles the sidebar in single-pane layouts.
    ///
    /// @return the configured icon button
    private M3IconButton createSidebarButton() {
        M3IconButton button = new M3IconButton(CatalogIcons.create(CatalogIcons.MENU));
        button.getStyleClass().addAll("catalog-top-action", "catalog-sidebar-action");
        button.setAccessibleText("Browse components");
        button.setOnAction(event -> toggleModalSidebar());
        return button;
    }

    /// Returns whether the current breakpoint uses persistent standard navigation.
    ///
    /// @return `true` when the standard drawer shares space with route content
    private boolean isPermanentSidebar() {
        return permanentSidebar;
    }

    /// Toggles the compact modal navigation drawer.
    private void toggleModalSidebar() {
        if (sidebarVisibility.isShowing()) {
            hideModalSidebar();
        } else {
            showModalSidebar();
        }
    }

    /// Presents modal navigation over compact and medium layouts.
    private void showModalSidebar() {
        if (isPermanentSidebar()) {
            return;
        }

        if (scaffold.getLeadingPane() == sidebar) {
            scaffold.setLeadingPane(null);
        }
        sidebar.setVariant(M3NavigationDrawerVariant.MODAL);
        if (sidebarVisibility.getContent() != sidebar) {
            sidebarVisibility.setContent(sidebar);
        }
        sidebar.refresh(currentRoute, expressiveOnly);
        @Nullable M3OverlayPane.OverlayHandle currentHandle = sidebarOverlayHandle;
        if (currentHandle == null || !currentHandle.isShowing()) {
            sidebarOverlayHandle = root.showModalOverlay(sidebarLayer);
        }
        sidebarScrim.show();
        sidebarVisibility.setShowing(true);
    }

    /// Starts the coordinated modal drawer and scrim exit transitions.
    private void hideModalSidebar() {
        if (sidebarOverlayHandle == null && sidebarVisibility.getContent() == null) {
            return;
        }
        sidebarVisibility.setShowing(false);
        sidebarScrim.hide();
    }

    /// Detaches the compact navigation layer after both coordinated exit transitions complete.
    private void removeSidebarLayerWhenHidden() {
        if (sidebarVisibility.getState() != M3VisibilityState.HIDDEN || sidebarScrim.isVisible()) {
            return;
        }
        @Nullable M3OverlayPane.OverlayHandle currentHandle = sidebarOverlayHandle;
        sidebarOverlayHandle = null;
        if (currentHandle != null) {
            currentHandle.hide();
        }
        sidebarVisibility.setContent(null);
    }

    /// Removes compact navigation synchronously before attaching the drawer to the standard scaffold slot.
    private void dismissModalSidebarImmediately() {
        @Nullable M3OverlayPane.OverlayHandle currentHandle = sidebarOverlayHandle;
        sidebarOverlayHandle = null;
        if (currentHandle != null) {
            currentHandle.hide();
        }
        sidebarVisibility.setShowing(false);
        sidebarVisibility.snapToCurrentState();
        sidebarVisibility.setContent(null);
        sidebarScrim.hide();
    }

    /// Returns the top-app-bar title for the current route.
    ///
    /// @return the route title
    private String routeTitle() {
        if (currentRoute instanceof CatalogRoute.Component componentRoute) {
            return componentRoute.component().name();
        }
        if (currentRoute instanceof CatalogRoute.Example exampleRoute) {
            return exampleRoute.example().name();
        }
        return "M3FX";
    }

    /// Returns the component associated with the current route.
    ///
    /// @return the current component, or `null` on Home
    private @Nullable CatalogComponent routeComponent() {
        if (currentRoute instanceof CatalogRoute.Component componentRoute) {
            return componentRoute.component();
        }
        if (currentRoute instanceof CatalogRoute.Example exampleRoute) {
            return exampleRoute.component();
        }
        return null;
    }

    /// Creates the favorite action for a component route.
    ///
    /// @param component the route component
    /// @return the configured toggle action
    private M3IconToggleButton createFavoriteButton(CatalogComponent component) {
        M3IconToggleButton button = new M3IconToggleButton(CatalogIcons.create(CatalogIcons.FAVORITE));
        button.getStyleClass().addAll("catalog-top-action", "catalog-favorite-action");
        button.setAccessibleText("Favorite " + component.name());
        button.setSelected(favorites.contains(component.name()));
        button.setOnAction(event -> {
            if (button.isSelected()) {
                favorites.add(component.name());
                showMessage(component.name() + " added to favorites");
            } else {
                favorites.remove(component.name());
                showMessage(component.name() + " removed from favorites");
            }
        });
        return button;
    }

    /// Creates the action that presents theme settings.
    ///
    /// @return the configured icon button
    private M3IconButton createThemeButton() {
        M3IconButton button = new M3IconButton(CatalogIcons.create(CatalogIcons.PALETTE));
        button.getStyleClass().addAll("catalog-top-action", "catalog-theme-action");
        button.setAccessibleText("Theme settings");
        button.setOnAction(event -> showSettings());
        return button;
    }

    /// Creates the route-specific overflow menu.
    ///
    /// @param component the active component, or `null` on Home
    /// @param sourceUrl the source URL for the active component or example, or `null` on Home
    /// @return the configured menu button
    private M3MenuButton createOverflowButton(
            @Nullable CatalogComponent component,
            @Nullable String sourceUrl
    ) {
        M3MenuButton button = new M3MenuButton();
        button.setGraphic(CatalogIcons.create(CatalogIcons.MORE_VERTICAL));
        button.getStyleClass().addAll("catalog-top-action", "catalog-overflow-action");
        button.setAccessibleText("More options");

        if (component != null) {
            button.getItems().addAll(
                    externalMenuItem("Material guidelines", component.guidelinesUrl()),
                    externalMenuItem("M3FX API reference", component.docsUrl())
            );
            button.getItems().add(externalMenuItem(
                    "View source",
                    Objects.requireNonNull(sourceUrl, "sourceUrl")
            ));
        } else {
            button.getItems().add(externalMenuItem("M3FX source", "https://github.com/Glavo/m3fx"));
        }
        button.getItems().add(externalMenuItem(
                "Report an issue",
                "https://github.com/Glavo/m3fx/issues/new"
        ));
        return button;
    }

    /// Creates an overflow-menu item that opens an external URL.
    ///
    /// @param text the menu-item label
    /// @param url  the absolute destination URL
    /// @return the configured menu item
    private M3MenuItem externalMenuItem(String text, String url) {
        M3MenuItem item = new M3MenuItem(text);
        item.setOnAction(event -> openDocument(url));
        return item;
    }

    /// Shows the modal theme settings overlay.
    void showSettings() {
        hideModalSidebar();
        @Nullable M3OverlayPane.OverlayHandle currentHandle = settingsOverlayHandle;
        if (currentHandle == null || !currentHandle.isShowing()) {
            settingsOverlayHandle = root.showModalOverlay(settingsLayer);
        }
        settingsScrim.show();
        settingsSheet.show();
    }

    /// Hides the modal theme settings overlay.
    void hideSettings() {
        settingsSheet.hide();
        settingsScrim.hide();
    }

    /// Detaches the settings layer after both coordinated exit transitions complete.
    private void removeSettingsLayerWhenHidden() {
        if (!settingsScrim.isVisible() && !settingsSheet.isVisible()) {
            @Nullable M3OverlayPane.OverlayHandle currentHandle = settingsOverlayHandle;
            settingsOverlayHandle = null;
            if (currentHandle != null) {
                currentHandle.hide();
            }
        }
    }

    /// Restores the Catalog theme and display controls to their defaults.
    private void resetSettings() {
        seedColor = DEFAULT_SEED_COLOR;
        profile = M3Profile.BASELINE_2021;
        brightness = Brightness.LIGHT;
        rightToLeft = false;
        reducedMotion = false;
        markExpressive = true;
        expressiveOnly = false;

        darkThemeSwitch.setSelected(false);
        expressiveSwitch.setSelected(false);
        rightToLeftSwitch.setSelected(false);
        reducedMotionSwitch.setSelected(false);
        markExpressiveSwitch.setSelected(true);
        expressiveOnlySwitch.setSelected(false);

        applyTheme();
        applyDisplaySettings();
        renderCurrentRoute();
        showMessage("Catalog settings reset");
    }

    /// Applies the currently selected theme to the active scene.
    private void applyTheme() {
        @Nullable Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }
        M3ThemeManager.install(activeScene, M3Theme.fromSeed(
                seedColor,
                profile,
                brightness,
                M3Density.standard()
        ));
    }

    /// Applies node orientation and motion settings to the Catalog subtree.
    private void applyDisplaySettings() {
        root.setNodeOrientation(rightToLeft
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT);
        M3MotionSettings.setReducedMotionRequested(root, reducedMotion);
    }

    /// Opens a document using the host platform.
    ///
    /// @param url the absolute URL to open
    private void openDocument(String url) {
        getHostServices().showDocument(url);
    }

    /// Presents transient feedback at the bottom of the Catalog.
    ///
    /// @param message the snackbar message
    private void showMessage(String message) {
        root.enqueueSnackbar(new M3Snackbar(message));
    }

    /// Returns the immutable component registry for package-level verification.
    ///
    /// @return the Catalog components
    @Unmodifiable
    List<CatalogComponent> components() {
        return components;
    }

    /// Returns the route currently displayed by the application.
    ///
    /// @return the current route
    CatalogRoute currentRoute() {
        return currentRoute;
    }

    /// Converts an opaque color to a hexadecimal CSS color literal.
    ///
    /// @param color the color to convert
    /// @return a color literal in the form `#RRGGBB`
    private static String toHex(Color color) {
        return String.format(
                "#%02X%02X%02X",
                Math.round(color.getRed() * 255.0),
                Math.round(color.getGreen() * 255.0),
                Math.round(color.getBlue() * 255.0)
        );
    }

    /// Launches the JavaFX Catalog application.
    ///
    /// @param args command-line arguments forwarded to JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
