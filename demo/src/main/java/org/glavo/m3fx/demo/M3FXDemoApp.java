// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.Animation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3AnimatedVisibility;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.glavo.m3fx.animation.M3VisibilityState;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3DialogWindow;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/// A demo application that showcases M3FX controls.
@NotNullByDefault
public final class M3FXDemoApp extends Application {
    /// Creates the demo application.
    public M3FXDemoApp() {
    }

    /// Seed colors shown in the demo settings dialog.
    private static final @Unmodifiable List<Color> SEED_COLORS = List.of(
            Color.web("#6750a4"),
            Color.web("#006a6a"),
            Color.web("#b3261e"),
            Color.web("#386a20"),
            Color.web("#7d5260")
    );

    /// The fixed width of the persistent and modal navigation drawer.
    private static final double NAVIGATION_DRAWER_WIDTH = 360.0;

    /// The logical distance traversed by incoming demo pages.
    private static final double PAGE_ENTER_DISTANCE = 24.0;

    /// The smaller parallax distance traversed by outgoing demo pages.
    private static final double PAGE_EXIT_DISTANCE = 12.0;

    /// The delay that separates outgoing and incoming page opacity.
    private static final Duration PAGE_ENTER_DELAY = Duration.millis(60.0);

    /// The current seed color used by the demo theme.
    private Color seedColor = M3Theme.DEFAULT_SEED_COLOR;

    /// The current Material Design profile.
    private M3Profile profile = M3Profile.BASELINE_2021;

    /// The current theme brightness.
    private Brightness brightness = Brightness.LIGHT;

    /// The current density scale applied to component tokens.
    private double densityScale;

    /// Whether demo animations are enabled.
    private boolean animationsEnabled = true;

    /// Animations owned by the active demo page.
    private final List<Animation> animations = new ArrayList<>();

    /// Sidebar groups rendered by the navigation drawer.
    private final List<SidebarGroup> sidebarGroups = new ArrayList<>();

    /// Demo pages created for the current application instance.
    private @Unmodifiable List<DemoPage> pages = List.of();

    /// The active JavaFX scene.
    private @Nullable Scene scene;

    /// Whether a deferred Windows per-monitor scale repair is pending.
    private boolean windowsScaleRepairPending;

    /// The navigation drawer used by the demo sidebar.
    private @Nullable M3NavigationDrawer sidebarDrawer;

    /// The scroll pane that hosts the current demo page.
    private @Nullable ScrollPane pageScrollPane;

    /// The currently shown demo page.
    private @Nullable DemoPage currentPage;

    /// The retained content host that transitions between demo pages.
    private @Nullable M3AnimatedContent pageHost;

    /// The stable root that owns dialogs, snackbars, and other in-scene overlays.
    private @Nullable M3OverlayPane overlayPane;

    /// The adaptive scaffold that owns the demo header, navigation, and page content.
    private @Nullable M3AdaptiveScaffold adaptiveScaffold;

    /// The header action that opens navigation while the persistent drawer is unavailable.
    private @Nullable M3IconButton navigationButton;

    /// The tooltip installed on the header navigation button.
    private @Nullable M3Tooltip navigationTooltip;

    /// Whether the header navigation tooltip is currently installed.
    private boolean navigationTooltipInstalled;

    /// The active modal navigation presentation, or `null` while no drawer overlay is shown.
    private @Nullable M3OverlayPane.OverlayHandle navigationOverlayHandle;

    /// The reusable scrim behind compact and medium navigation.
    private final M3Scrim navigationOverlayScrim = new M3Scrim();

    /// The reusable interruptible slide host for compact and medium navigation.
    private final M3AnimatedVisibility navigationOverlayVisibility = new M3AnimatedVisibility();

    /// The reusable modal layer containing the navigation scrim and slide host.
    private final StackPane navigationOverlayLayer = new StackPane();

    /// Starts the demo application.
    @Override
    public void start(Stage stage) {
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");
        if (isWindows && Screen.getPrimary().getOutputScaleX() > 1) {
            System.getProperties().putIfAbsent("prism.lcdtext", "false");
        }

        M3OverlayPane root = new M3OverlayPane();
        root.getStyleClass().add("demo-root");
        overlayPane = root;
        configureNavigationOverlay();

        List<DemoPage> createdPages = DemoPageCatalog.createPages(new DemoPageContext(this));
        pages = createdPages;

        M3AdaptiveScaffold scaffold = createContent(createdPages);
        scaffold.setTopBar(createHeader(root));
        root.setContent(scaffold);
        updateAdaptiveLayout();

        Scene scene = new Scene(root, 1180.0, 820.0);
        scene.getStylesheets().add(demoStylesheetUrl());
        this.scene = scene;
        applyTheme();
        applyMotionSettings();
        presentPage(createdPages.get(0), false);

        stage.setTitle("M3FX Demo");
        stage.setMinWidth(360.0);
        stage.setMinHeight(520.0);
        stage.setScene(scene);
        stage.show();
        installWindowsScaleTransitionRepair(stage);
    }

    /// Repairs a Windows JavaFX bounds mismatch when the demo crosses monitors at its minimum size.
    ///
    /// When a stage is exactly at one of its minimum-size constraints, the Windows Glass backend can retain the
    /// previous monitor's physical bound for that axis while the JavaFX window and scene continue reporting the
    /// logical minimum. Re-submitting only the affected constraint after the asynchronous output-scale notification
    /// reconciles those bounds. Windows scale changes away from a minimum-size boundary need no application repair.
    ///
    /// @param stage the demo stage
    private void installWindowsScaleTransitionRepair(Stage stage) {
        if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
            return;
        }

        stage.outputScaleXProperty().addListener(observable -> scheduleWindowsScaleTransitionRepair(stage));
        stage.outputScaleYProperty().addListener(observable -> scheduleWindowsScaleTransitionRepair(stage));
    }

    /// Schedules one bounds refresh after the current native output-scale notification completes.
    ///
    /// @param stage the demo stage
    private void scheduleWindowsScaleTransitionRepair(Stage stage) {
        if (windowsScaleRepairPending) {
            return;
        }

        windowsScaleRepairPending = true;
        Platform.runLater(() -> {
            windowsScaleRepairPending = false;
            if (!stage.isShowing() || stage.isIconified() || stage.isMaximized() || stage.isFullScreen()) {
                return;
            }

            double minWidth = stage.getMinWidth();
            double minHeight = stage.getMinHeight();
            boolean widthAtMinimum = minWidth > 0.0 && stage.getWidth() <= minWidth + 0.5;
            boolean heightAtMinimum = minHeight > 0.0 && stage.getHeight() <= minHeight + 0.5;
            if (!widthAtMinimum && !heightAtMinimum) {
                return;
            }

            if (widthAtMinimum) {
                stage.setMinWidth(0.0);
                stage.setMinWidth(minWidth);
            }
            if (heightAtMinimum) {
                stage.setMinHeight(0.0);
                stage.setMinHeight(minHeight);
            }
            stage.setWidth(stage.getWidth());
            stage.setHeight(stage.getHeight());

            @Nullable Scene activeScene = stage.getScene();
            if (activeScene != null) {
                activeScene.getRoot().requestLayout();
            }
        });
    }

    /// Creates the small Material top app bar used by the demo shell.
    ///
    /// Global presentation settings are intentionally placed behind one trailing action so the app bar retains the
    /// title-and-actions responsibility defined by Material Design.
    ///
    /// @param root the demo root whose orientation is controlled by the settings dialog
    /// @return the configured top app bar
    private M3TopAppBar createHeader(M3OverlayPane root) {
        M3IconButton menuButton = new M3IconButton(DemoPageSupport.createSurfaceVariantIcon("menu"));
        menuButton.getStyleClass().add("demo-navigation-button");
        menuButton.setAccessibleText("Open component navigation");
        menuButton.setOnAction(event -> showNavigationDrawer());
        M3Tooltip navigationTooltip = new M3Tooltip("Open component navigation");
        M3Tooltip.install(menuButton, navigationTooltip);
        this.navigationTooltip = navigationTooltip;
        navigationTooltipInstalled = true;
        navigationButton = menuButton;

        M3IconButton settingsButton =
                new M3IconButton(DemoPageSupport.createSurfaceVariantIcon("settings"));
        settingsButton.getStyleClass().add("demo-settings-button");
        settingsButton.setAccessibleText("Open demo settings");
        settingsButton.setOnAction(event -> showDemoPreferences(root));
        M3Tooltip.install(settingsButton, new M3Tooltip("Demo settings"));

        M3TopAppBar appBar = new M3TopAppBar("M3FX");
        appBar.setVariant(M3TopAppBarVariant.SMALL);
        appBar.getStyleClass().add("demo-header");
        appBar.setNavigation(menuButton);
        appBar.getActions().add(settingsButton);

        ScrollPane scrollPane = pageScrollPane;
        if (scrollPane != null) {
            Runnable updateScrolledUnder = () -> appBar.setScrolledUnder(scrollPane.getVvalue() > 0.001);
            scrollPane.vvalueProperty().addListener(observable -> updateScrolledUnder.run());
            updateScrolledUnder.run();
        }
        return appBar;
    }

    /// Opens the global demo presentation settings.
    ///
    /// @param root the demo root whose orientation is configured by the dialog
    private void showDemoPreferences(M3OverlayPane root) {
        M3Button done = new M3Button("Done", M3ButtonVariant.TEXT);
        done.setDefaultButton(true);
        done.setCancelButton(true);

        M3Dialog dialog = new M3Dialog();
        M3DialogPane pane = dialog.getDialogPane();
        pane.setHeaderText("Demo settings");
        pane.setContentText("Adjust the catalog presentation without changing individual component examples.");
        pane.setContent(createDemoPreferencesContent(root));
        pane.setPrefWidth(520.0);
        pane.getActions().add(done);
        showDialog(dialog);
    }

    /// Creates the settings content shown from the top app bar.
    ///
    /// @param root the demo root whose orientation is configured by the direction switch
    /// @return the settings content
    private Node createDemoPreferencesContent(M3OverlayPane root) {
        HBox seedButtons = new HBox(8.0);
        seedButtons.getStyleClass().add("demo-seed-buttons");
        for (Color color : SEED_COLORS) {
            M3IconButton button = new M3IconButton();
            button.getStyleClass().add("demo-seed-button");
            button.setAccessibleText("Use color seed " + toHex(color));
            button.setStyle("-fx-background-color: " + toHex(color) + ";");
            button.setOnAction(event -> {
                seedColor = color;
                applyTheme();
            });
            seedButtons.getChildren().add(button);
        }

        M3SegmentedButton baseline = new M3SegmentedButton("Standard");
        M3SegmentedButton expressive = new M3SegmentedButton("Expressive");
        baseline.setSelected(profile == M3Profile.BASELINE_2021);
        expressive.setSelected(profile == M3Profile.EXPRESSIVE_2025);
        M3SegmentedButtonGroup profileGroup =
                DemoPageSupport.createSegmentedButtonGroup(baseline, expressive);
        profileGroup.getStyleClass().add("demo-profile-settings");
        profileGroup.setAllowEmptySelection(false);
        baseline.setOnAction(event -> {
            if (baseline.isSelected()) {
                profile = M3Profile.BASELINE_2021;
                applyTheme();
            }
        });
        expressive.setOnAction(event -> {
            if (expressive.isSelected()) {
                profile = M3Profile.EXPRESSIVE_2025;
                applyTheme();
            }
        });

        M3SegmentedButton compact = new M3SegmentedButton("Compact");
        M3SegmentedButton standard = new M3SegmentedButton("Standard");
        M3SegmentedButton comfortable = new M3SegmentedButton("Comfort");
        compact.setSelected(densityScale < 0.0);
        standard.setSelected(densityScale == 0.0);
        comfortable.setSelected(densityScale > 0.0);
        M3SegmentedButtonGroup densityGroup =
                DemoPageSupport.createSegmentedButtonGroup(compact, standard, comfortable);
        densityGroup.getStyleClass().add("demo-density-settings");
        densityGroup.setAllowEmptySelection(false);
        compact.setOnAction(event -> updateDensity(compact, -1.0));
        standard.setOnAction(event -> updateDensity(standard, 0.0));
        comfortable.setOnAction(event -> updateDensity(comfortable, 1.0));

        M3Switch animationsSwitch = new M3Switch("Animations");
        animationsSwitch.getStyleClass().add("demo-animations-switch");
        animationsSwitch.setSelected(animationsEnabled);
        animationsSwitch.setOnAction(event -> {
            animationsEnabled = animationsSwitch.isSelected();
            applyMotionSettings();
            refreshCurrentPage();
        });

        M3Switch directionSwitch = new M3Switch("Right-to-left layout");
        directionSwitch.getStyleClass().add("demo-direction-switch");
        directionSwitch.setAccessibleText("Use right-to-left layout");
        directionSwitch.setSelected(root.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT);
        directionSwitch.setOnAction(event -> {
            hideNavigationDrawer(false);
            root.setNodeOrientation(directionSwitch.isSelected()
                    ? NodeOrientation.RIGHT_TO_LEFT
                    : NodeOrientation.LEFT_TO_RIGHT);
        });

        M3Switch brightnessSwitch = new M3Switch("Dark theme");
        brightnessSwitch.getStyleClass().add("demo-brightness-switch");
        brightnessSwitch.setSelected(brightness == Brightness.DARK);
        brightnessSwitch.setOnAction(event -> {
            brightness = brightnessSwitch.isSelected() ? Brightness.DARK : Brightness.LIGHT;
            applyTheme();
        });

        VBox content = new VBox(
                20.0,
                createSettingsGroup("Color theme", seedButtons),
                createSettingsGroup("Material profile", profileGroup),
                createSettingsGroup("Density", densityGroup),
                createSettingsGroup("Behavior", animationsSwitch, directionSwitch, brightnessSwitch)
        );
        content.getStyleClass().add("demo-settings-content");
        return content;
    }

    /// Applies a density option selected in the settings dialog.
    ///
    /// @param option the option that initiated the action
    /// @param scale  the density scale represented by the option
    private void updateDensity(M3SegmentedButton option, double scale) {
        if (option.isSelected()) {
            densityScale = scale;
            applyTheme();
        }
    }

    /// Creates one labeled group in the demo settings dialog.
    ///
    /// @param title the group title
    /// @param nodes the settings controls in display order
    /// @return the assembled settings group
    private static VBox createSettingsGroup(String title, Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");
        VBox group = new VBox(10.0, label);
        group.getStyleClass().add("demo-settings-group");
        group.getChildren().addAll(nodes);
        return group;
    }

    /// Creates the adaptive main content shell with sidebar and page host.
    ///
    /// @param pages the pages shown by the sidebar and content host
    /// @return the assembled demo content shell
    private M3AdaptiveScaffold createContent(List<DemoPage> pages) {
        M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
        scaffold.getStyleClass().addAll("demo-shell", "demo-adaptive-scaffold");
        scaffold.setContentMargin(0.0);
        scaffold.setPaneSpacing(0.0);
        scaffold.setFixedLeadingPaneWidth(NAVIGATION_DRAWER_WIDTH);
        scaffold.setLeadingPane(createSidebar(pages));
        scaffold.setMainPane(createPageScrollPane());
        scaffold.setActivePane(M3PaneRole.MAIN);
        scaffold.breakpointProperty().addListener(
                (observable, oldBreakpoint, newBreakpoint) -> updateAdaptiveLayout()
        );
        adaptiveScaffold = scaffold;
        return scaffold;
    }

    /// Creates the component sidebar.
    private Node createSidebar(List<DemoPage> pages) {
        M3NavigationDrawer sidebar = new M3NavigationDrawer();
        sidebar.getStyleClass().add("demo-sidebar-drawer");
        sidebar.setAllowEmptySelection(true);
        sidebarDrawer = sidebar;

        sidebarGroups.clear();
        sidebarGroups.addAll(createSidebarGroups(pages));
        buildSidebarItems();
        return sidebar;
    }

    /// Applies the current Material breakpoint to persistent navigation and the header navigation action.
    private void updateAdaptiveLayout() {
        M3AdaptiveScaffold scaffold = adaptiveScaffold;
        M3NavigationDrawer sidebar = sidebarDrawer;
        if (scaffold == null || sidebar == null) {
            return;
        }

        boolean persistentNavigation = usesPersistentNavigation(scaffold.getBreakpoint());
        M3IconButton menuButton = navigationButton;
        if (menuButton != null) {
            menuButton.setManaged(!persistentNavigation);
            menuButton.setVisible(!persistentNavigation);
        }

        M3Tooltip tooltip = navigationTooltip;
        if (menuButton != null && tooltip != null) {
            if (persistentNavigation) {
                if (navigationTooltipInstalled) {
                    M3Tooltip.uninstall(menuButton, tooltip);
                    navigationTooltipInstalled = false;
                }
            } else if (!navigationTooltipInstalled) {
                M3Tooltip.install(menuButton, tooltip);
                navigationTooltipInstalled = true;
            }
        }

        if (persistentNavigation) {
            hideNavigationDrawer(false);
            sidebar.setTranslateX(0.0);
            sidebar.setNodeOrientation(NodeOrientation.INHERIT);
            StackPane.setAlignment(sidebar, null);
            sidebar.getStyleClass().remove("demo-modal-sidebar-drawer");
            sidebar.setVariant(M3NavigationDrawerVariant.STANDARD);
            if (scaffold.getLeadingPane() != sidebar) {
                scaffold.setLeadingPane(sidebar);
            }
            scaffold.setPaneLayout(M3PaneLayout.FIXED_LEADING);
        } else {
            scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            // Retain the stable leading slot so M3AdaptiveScaffold can animate it out. The node is detached only
            // when the modal drawer is actually requested and must move into the overlay layer.
        }
    }

    /// Returns whether a breakpoint retains the component drawer beside a sufficiently wide page pane.
    ///
    /// @param breakpoint the current Material width breakpoint
    /// @return `true` for expanded, large, and extra-large layouts
    private static boolean usesPersistentNavigation(M3Breakpoint breakpoint) {
        return switch (breakpoint) {
            case COMPACT, MEDIUM -> false;
            case EXPANDED, LARGE, EXTRA_LARGE -> true;
        };
    }

    /// Configures the reusable compact and medium navigation presentation.
    private void configureNavigationOverlay() {
        navigationOverlayScrim.setAccessibleText("Close component navigation");
        navigationOverlayScrim.setShown(false);
        navigationOverlayScrim.setOnAction(event -> hideNavigationDrawer(true));
        navigationOverlayScrim.visibleProperty().addListener(
                (observable, oldVisible, newVisible) -> removeNavigationOverlayWhenHidden()
        );

        navigationOverlayVisibility.getStyleClass().add("demo-navigation-visibility");
        navigationOverlayVisibility.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        navigationOverlayVisibility.setFitToHeight(true);
        navigationOverlayVisibility.setSizeTransform(null);
        configureNavigationOverlayDirection(NodeOrientation.LEFT_TO_RIGHT);
        navigationOverlayVisibility.setShowing(false);
        navigationOverlayVisibility.stateProperty().addListener(
                (observable, oldState, newState) -> removeNavigationOverlayWhenHidden()
        );

        navigationOverlayLayer.getStyleClass().add("demo-modal-navigation-layer");
        navigationOverlayLayer.setPickOnBounds(true);
        navigationOverlayLayer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        navigationOverlayLayer.getChildren().setAll(navigationOverlayScrim, navigationOverlayVisibility);
        navigationOverlayLayer.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hideNavigationDrawer(true);
                event.consume();
            }
        });
    }

    /// Applies physical alignment and logical-edge motion for one content direction.
    private void configureNavigationOverlayDirection(NodeOrientation orientation) {
        boolean rightToLeft = orientation == NodeOrientation.RIGHT_TO_LEFT;
        Pos alignment = rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT;
        M3TransitionEdge edge = rightToLeft ? M3TransitionEdge.END : M3TransitionEdge.START;
        navigationOverlayVisibility.setAlignment(alignment);
        navigationOverlayVisibility.setEnterTransition(
                M3EnterTransition.slideFrom(edge, NAVIGATION_DRAWER_WIDTH)
        );
        navigationOverlayVisibility.setExitTransition(
                M3ExitTransition.slideTo(edge, NAVIGATION_DRAWER_WIDTH)
        );
        StackPane.setAlignment(navigationOverlayVisibility, alignment);
    }

    /// Shows the component drawer as a modal leading-edge overlay in compact and medium layouts.
    private void showNavigationDrawer() {
        M3AdaptiveScaffold scaffold = adaptiveScaffold;
        M3OverlayPane overlay = overlayPane;
        M3NavigationDrawer drawer = sidebarDrawer;
        if (scaffold == null
                || overlay == null
                || drawer == null
                || usesPersistentNavigation(scaffold.getBreakpoint())) {
            return;
        }

        scaffold.setLeadingPane(null);
        drawer.setVariant(M3NavigationDrawerVariant.MODAL);
        if (!drawer.getStyleClass().contains("demo-modal-sidebar-drawer")) {
            drawer.getStyleClass().add("demo-modal-sidebar-drawer");
        }

        NodeOrientation drawerOrientation = overlay.getEffectiveNodeOrientation();
        drawer.setNodeOrientation(drawerOrientation);
        configureNavigationOverlayDirection(drawerOrientation);
        if (navigationOverlayVisibility.getContent() != drawer) {
            navigationOverlayVisibility.setContent(drawer);
        }

        M3OverlayPane.OverlayHandle currentHandle = navigationOverlayHandle;
        if (currentHandle == null || !currentHandle.isShowing()) {
            navigationOverlayHandle = overlay.showModalOverlay(navigationOverlayLayer);
        }
        navigationOverlayScrim.show();
        navigationOverlayVisibility.setShowing(true);
        drawer.requestFocus();
        DemoPage page = currentPage;
        if (page != null) {
            revealSidebarPage(page);
        }
    }

    /// Hides the compact or medium modal component drawer.
    ///
    /// @param animated whether to run the drawer and scrim exit transitions
    private void hideNavigationDrawer(boolean animated) {
        if (navigationOverlayHandle == null && navigationOverlayVisibility.getContent() == null) {
            return;
        }
        navigationOverlayVisibility.setShowing(false);
        navigationOverlayScrim.hide();
        if (!animated) {
            navigationOverlayVisibility.snapToCurrentState();
            releaseNavigationOverlay();
        }
    }

    /// Releases the modal layer after both reusable visibility components finish exiting.
    private void removeNavigationOverlayWhenHidden() {
        if (navigationOverlayVisibility.getState() != M3VisibilityState.HIDDEN
                || navigationOverlayScrim.isVisible()) {
            return;
        }
        releaseNavigationOverlay();
    }

    /// Detaches the reusable modal presentation and restores the breakpoint-selected drawer state.
    private void releaseNavigationOverlay() {
        M3OverlayPane.OverlayHandle currentHandle = navigationOverlayHandle;
        if (currentHandle == null && navigationOverlayVisibility.getContent() == null) {
            return;
        }

        navigationOverlayHandle = null;
        if (currentHandle != null) {
            currentHandle.hide();
        }
        navigationOverlayVisibility.setContent(null);
        M3NavigationDrawer drawer = sidebarDrawer;
        if (drawer != null) {
            drawer.setTranslateX(0.0);
            drawer.setNodeOrientation(NodeOrientation.INHERIT);
            drawer.getStyleClass().remove("demo-modal-sidebar-drawer");
            drawer.setVariant(M3NavigationDrawerVariant.STANDARD);
        }
    }

    /// Creates sidebar groups from ordered demo pages.
    private static List<SidebarGroup> createSidebarGroups(List<DemoPage> pages) {
        if (pages.isEmpty()) {
            return List.of();
        }

        List<SidebarGroup> groups = new ArrayList<>();
        String currentGroupTitle = pages.get(0).sidebarSection();
        List<DemoPage> currentGroupPages = new ArrayList<>();
        for (DemoPage page : pages) {
            if (!page.sidebarSection().equals(currentGroupTitle)) {
                addSidebarGroup(groups, currentGroupTitle, currentGroupPages);
                currentGroupTitle = page.sidebarSection();
                currentGroupPages = new ArrayList<>();
            }
            currentGroupPages.add(page);
        }
        addSidebarGroup(groups, currentGroupTitle, currentGroupPages);
        return groups;
    }

    /// Adds one completed sidebar group when it contains pages.
    private static void addSidebarGroup(
            List<SidebarGroup> groups,
            String title,
            List<DemoPage> pages
    ) {
        if (!pages.isEmpty()) {
            groups.add(new SidebarGroup(title, pages));
        }
    }

    /// Builds sidebar drawer items from the configured component groups.
    private void buildSidebarItems() {
        M3NavigationDrawer sidebar = sidebarDrawer;
        if (sidebar == null) {
            return;
        }

        sidebar.getItems().clear();
        for (SidebarGroup group : sidebarGroups) {
            if (group.pages.size() > 1) {
                M3NavigationDrawerGroup drawerGroup = createSidebarDrawerGroup(group);
                group.drawerGroup = drawerGroup;
                group.topLevelItem = null;
                sidebar.getItems().add(drawerGroup);
            } else {
                M3ListItem item = createSidebarPageItem(group.pages.get(0), false);
                group.topLevelItem = item;
                group.drawerGroup = null;
                sidebar.getItems().add(item);
            }
        }
        refreshSidebarSelection();
    }

    /// Creates a collapsible sidebar group control.
    private M3NavigationDrawerGroup createSidebarDrawerGroup(SidebarGroup group) {
        M3NavigationDrawerGroup drawerGroup = new M3NavigationDrawerGroup(group.title);
        drawerGroup.getHeaderItem().getStyleClass().add("demo-sidebar-group-item");
        drawerGroup.getHeaderItem().setUserData(group);
        drawerGroup.expandedProperty().addListener((observable, oldValue, newValue) -> refreshSidebarSelection());
        for (DemoPage page : group.pages) {
            drawerGroup.getItems().add(createSidebarPageItem(page, true));
        }
        return drawerGroup;
    }

    /// Creates one sidebar page destination item.
    private M3ListItem createSidebarPageItem(DemoPage page, boolean child) {
        M3ListItem item = new M3ListItem(page.navigationTitle());
        item.getStyleClass().add(child ? "demo-sidebar-child-item" : "demo-sidebar-top-item");
        item.setUserData(page);
        item.setOnAction(event -> showPage(page));
        return item;
    }

    /// Creates the scrollable page host.
    private Node createPageScrollPane() {
        M3AnimatedContent host = new M3AnimatedContent();
        host.getStyleClass().add("demo-page-host");
        host.setMinWidth(0.0);
        host.setMaxWidth(Double.MAX_VALUE);
        host.setFitToWidth(true);
        pageHost = host;

        ScrollPane scrollPane = new ScrollPane(host);
        pageScrollPane = scrollPane;
        scrollPane.getStyleClass().add("demo-scroll-pane");
        M3ScrollPanes.style(scrollPane);
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        scrollPane.setFocusTraversable(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    /// Shows a registered component page in the center pane.
    ///
    /// @param page the registered destination page
    /// @throws NullPointerException     if `page` is `null`
    /// @throws IllegalArgumentException if `page` is not registered by this application
    void showPage(DemoPage page) {
        presentPage(page, true);
    }

    /// Presents a registered component page, optionally retaining the previous page for a content transition.
    ///
    /// @param page     the registered destination page
    /// @param animated whether to animate replacement of an already displayed page
    /// @throws NullPointerException     if `page` is `null`
    /// @throws IllegalArgumentException if `page` is not registered by this application
    private void presentPage(DemoPage page, boolean animated) {
        Objects.requireNonNull(page, "page");
        if (!pages.contains(page)) {
            throw new IllegalArgumentException("demo page is not registered: " + page.title());
        }

        stopPageAnimations();
        M3AnimatedContent host = pageHost;
        if (host == null) {
            return;
        }

        @Nullable DemoPage previousPage = currentPage;
        boolean animateReplacement = animated && previousPage != null && previousPage != page;
        if (animateReplacement) {
            configurePageTransition(host, previousPage, page);
        }

        currentPage = page;
        expandSidebarGroupForPage(page);
        refreshSidebarSelection();

        VBox pageNode = new VBox(24.0);
        pageNode.getStyleClass().add("demo-page");
        pageNode.setFillWidth(true);
        pageNode.setMinWidth(0.0);
        pageNode.setMaxWidth(Double.MAX_VALUE);

        Node pageContent = page.contentFactory().get();
        if (pageContent instanceof Region region) {
            region.setMinWidth(0.0);
            region.setMaxWidth(Double.MAX_VALUE);
        }
        pageNode.getChildren().addAll(createPageHeader(page), pageContent);
        host.setContent(pageNode);
        if (!animateReplacement) {
            host.snapToCurrentState();
        }
        ScrollPane scrollPane = pageScrollPane;
        if (scrollPane != null) {
            scrollPane.setHvalue(0.0);
            scrollPane.setVvalue(0.0);
        }
        revealSidebarPage(page);
        hideNavigationDrawer(true);
    }

    /// Configures a logical-direction shared-axis transition between two catalog destinations.
    private void configurePageTransition(M3AnimatedContent host, DemoPage previousPage, DemoPage targetPage) {
        boolean forward = pages.indexOf(targetPage) > pages.indexOf(previousPage);
        M3TransitionEdge enterEdge = forward ? M3TransitionEdge.END : M3TransitionEdge.START;
        M3TransitionEdge exitEdge = forward ? M3TransitionEdge.START : M3TransitionEdge.END;
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(PAGE_ENTER_DELAY)
                .and(M3EnterTransition.slideFrom(enterEdge, PAGE_ENTER_DISTANCE));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(exitEdge, PAGE_EXIT_DISTANCE));
        host.setContentTransform(new M3ContentTransform(enter, exit, null, 0.0));
    }

    /// Creates the title, subtitle, and optional Material documentation action for a page.
    private Node createPageHeader(DemoPage page) {
        return new DemoPageHeader(
                page.title(),
                page.subtitle(),
                page.documentationLabel(),
                () -> getHostServices().showDocument(page.documentationUrl())
        );
    }

    /// Recreates the current page so resolved runtime settings affect active controls immediately.
    private void refreshCurrentPage() {
        DemoPage page = currentPage;
        if (page != null) {
            presentPage(page, false);
        }
    }

    /// Returns the demo page titles created for this application instance.
    @Unmodifiable
    List<String> demoPageTitles() {
        return pages.stream().map(DemoPage::title).toList();
    }

    /// Returns the immutable demo page catalog in navigation order.
    ///
    /// @return the registered demo pages
    @Unmodifiable
    List<DemoPage> demoPages() {
        return pages;
    }

    /// Shows the demo page with the requested title without animating content replacement.
    ///
    /// This package-level entry point provides deterministic programmatic navigation for demo verification. User
    /// activation through the sidebar continues to use [#showPage(DemoPage)] and its configured page transition.
    ///
    /// @param title the exact registered page title
    /// @throws NullPointerException     if `title` is `null`
    /// @throws IllegalStateException    if the page catalog has not been created
    /// @throws IllegalArgumentException if no registered page has the requested title
    void showPageByTitle(String title) {
        Objects.requireNonNull(title, "title");
        if (pages.isEmpty()) {
            throw new IllegalStateException("demo pages have not been created");
        }
        for (DemoPage page : pages) {
            if (page.title().equals(title)) {
                presentPage(page, false);
                return;
            }
        }
        throw new IllegalArgumentException("unknown demo page title: " + title);
    }

    /// Returns the active scene.
    @Nullable Scene activeScene() {
        return scene;
    }

    /// Returns the current page's sidebar navigation title.
    @Nullable String currentPageNavigationTitle() {
        DemoPage page = currentPage;
        return page == null ? null : page.navigationTitle();
    }

    /// Returns the selected sidebar item title.
    @Nullable String selectedSidebarNavigationTitle() {
        for (SidebarGroup group : sidebarGroups) {
            @Nullable M3ListItem selectedItem = group.selectedItem();
            if (selectedItem != null) {
                return selectedItem.getHeadlineText();
            }
        }
        return null;
    }

    /// Applies a presentation mode directly for rendered demo validation.
    ///
    /// @param profile           the Material profile to install
    /// @param brightness        the theme brightness to install
    /// @param animationsEnabled whether full component motion is enabled
    void configurePresentation(M3Profile profile, Brightness brightness, boolean animationsEnabled) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.brightness = Objects.requireNonNull(brightness, "brightness");
        this.animationsEnabled = animationsEnabled;
        applyTheme();
        applyMotionSettings();
        refreshCurrentPage();
    }

    /// Expands the collapsible sidebar group containing the requested page.
    private void expandSidebarGroupForPage(DemoPage page) {
        for (SidebarGroup group : sidebarGroups) {
            M3NavigationDrawerGroup drawerGroup = group.drawerGroup;
            if (drawerGroup != null && group.pages.contains(page)) {
                drawerGroup.setExpanded(true);
                return;
            }
        }
    }

    /// Refreshes selected state on currently visible sidebar destination items.
    private void refreshSidebarSelection() {
        @Nullable DemoPage page = currentPage;
        for (SidebarGroup group : sidebarGroups) {
            group.updateSelection(page);
        }
    }

    /// Reveals the active sidebar destination through the drawer's layout-aware scroll contract.
    private void revealSidebarPage(DemoPage page) {
        @Nullable M3NavigationDrawer drawer = sidebarDrawer;
        @Nullable M3ListItem item = sidebarItemForPage(page);
        if (currentPage == page && drawer != null && item != null) {
            drawer.scrollTo(item);
        }
    }

    /// Returns the rendered sidebar destination item for a page.
    private @Nullable M3ListItem sidebarItemForPage(DemoPage page) {
        for (SidebarGroup group : sidebarGroups) {
            @Nullable M3ListItem item = group.itemForPage(page);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Stops animations owned by the previous page.
    private void stopPageAnimations() {
        for (Animation animation : animations) {
            animation.stop();
        }
        animations.clear();
    }

    /// Registers an animation whose lifetime follows the active demo page.
    ///
    /// The animation starts immediately when full motion is enabled. Otherwise it remains stopped until presentation
    /// settings enable animation or the active page is replaced.
    ///
    /// @param animation the animation to register
    void registerPageAnimation(Animation animation) {
        Objects.requireNonNull(animation, "animation");
        animations.add(animation);
        if (animationsEnabled) {
            animation.play();
        }
    }

    /// Applies the current demo animation switch to page-owned animations.
    private void updatePageAnimations() {
        for (Animation animation : animations) {
            if (animationsEnabled) {
                animation.play();
            } else {
                animation.pause();
            }
        }
    }

    /// Presents a dialog in an ownerless native window using the current demo theme.
    ///
    /// @param dialog the dialog to present
    void showStandaloneDialog(M3Dialog dialog) {
        M3DialogWindow window = new M3DialogWindow();
        window.setTitle("M3FX Standalone Dialog");
        window.setTheme(M3Theme.fromSeed(
                seedColor,
                profile,
                brightness,
                M3Density.of(densityScale)
        ));
        window.showDialog(dialog);
    }

    /// Presents a dialog in the demo's stable overlay host.
    void showDialog(M3Dialog dialog) {
        M3OverlayPane activeOverlay = overlayPane;
        if (activeOverlay != null) {
            activeOverlay.showDialog(dialog);
        }
    }

    /// Returns the overlay pane that owns in-scene demo presentations.
    ///
    /// @return the active overlay pane, or `null` before the application root is assembled
    @Nullable M3OverlayPane activeOverlayPane() {
        return overlayPane;
    }

    /// Shows a demo snackbar message.
    void showSnackbar(String message) {
        M3OverlayPane activeOverlay = overlayPane;
        if (activeOverlay == null) {
            return;
        }
        activeOverlay.enqueueSnackbar(new M3Snackbar(message));
    }

    /// Applies the current theme to the scene.
    private void applyTheme() {
        Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }

        M3ThemeManager.install(activeScene, M3Theme.fromSeed(
                seedColor,
                profile,
                brightness,
                M3Density.of(densityScale)
        ));
    }

    /// Applies the current demo animation switch to the active scene.
    private void applyMotionSettings() {
        Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }

        M3MotionSettings.setReducedMotionRequested(activeScene.getRoot(), !animationsEnabled);
        updatePageAnimations();
    }

    /// Returns the demo stylesheet URL.
    private static String demoStylesheetUrl() {
        URL url = M3FXDemoApp.class.getResource("/org/glavo/m3fx/demo/m3fx-demo.css");
        if (url == null) {
            throw new IllegalStateException("Missing demo stylesheet resource");
        }
        return url.toExternalForm();
    }

    /// Converts a color to a hexadecimal CSS value.
    private static String toHex(Color color) {
        Objects.requireNonNull(color, "color");
        return "#"
                + toHexChannel(color.getRed())
                + toHexChannel(color.getGreen())
                + toHexChannel(color.getBlue());
    }

    /// Converts a color channel to a two-character hexadecimal value.
    private static String toHexChannel(double value) {
        String hex = Integer.toHexString((int) Math.round(value * 255.0));
        return hex.length() == 1 ? "0" + hex : hex;
    }

    /// Describes one sidebar group and its drawer controls.
    @NotNullByDefault
    private static final class SidebarGroup {
        /// The group title displayed in the sidebar.
        private final String title;

        /// The pages that belong to the group.
        private final @Unmodifiable List<DemoPage> pages;

        /// The drawer group used for collapsible sidebar sections.
        private @Nullable M3NavigationDrawerGroup drawerGroup = null;

        /// The direct list item used for non-collapsible sidebar sections.
        private @Nullable M3ListItem topLevelItem = null;

        /// Creates a sidebar group.
        private SidebarGroup(String title, List<DemoPage> pages) {
            this.title = Objects.requireNonNull(title, "title");
            if (pages.isEmpty()) {
                throw new IllegalArgumentException("pages must not be empty");
            }
            this.pages = List.copyOf(pages);
        }

        /// Updates selected state for all rendered items in this sidebar group.
        private void updateSelection(@Nullable DemoPage page) {
            M3NavigationDrawerGroup drawerGroup = this.drawerGroup;
            if (drawerGroup != null) {
                boolean containsCurrentPage = page != null && pages.contains(page);
                boolean expanded = drawerGroup.isExpanded();
                drawerGroup.getHeaderItem().setSelected(containsCurrentPage && !expanded);
                for (M3ListItem item : drawerGroup.getItems()) {
                    item.setSelected(containsCurrentPage && expanded && item.getUserData() == page);
                }
                return;
            }

            M3ListItem topLevelItem = this.topLevelItem;
            if (topLevelItem != null) {
                topLevelItem.setSelected(topLevelItem.getUserData() == page);
            }
        }

        /// Returns the rendered item for a page in this sidebar group.
        private @Nullable M3ListItem itemForPage(DemoPage page) {
            M3NavigationDrawerGroup drawerGroup = this.drawerGroup;
            if (drawerGroup != null) {
                for (M3ListItem item : drawerGroup.getItems()) {
                    if (item.getUserData() == page) {
                        return item;
                    }
                }
                return null;
            }

            M3ListItem topLevelItem = this.topLevelItem;
            if (topLevelItem != null && topLevelItem.getUserData() == page) {
                return topLevelItem;
            }
            return null;
        }

        /// Returns the currently selected rendered item in this sidebar group.
        private @Nullable M3ListItem selectedItem() {
            M3NavigationDrawerGroup drawerGroup = this.drawerGroup;
            if (drawerGroup != null) {
                if (drawerGroup.getHeaderItem().isSelected()) {
                    return drawerGroup.getHeaderItem();
                }
                for (M3ListItem item : drawerGroup.getItems()) {
                    if (item.isSelected()) {
                        return item;
                    }
                }
                return null;
            }

            M3ListItem topLevelItem = this.topLevelItem;
            return topLevelItem != null && topLevelItem.isSelected() ? topLevelItem : null;
        }
    }

}
