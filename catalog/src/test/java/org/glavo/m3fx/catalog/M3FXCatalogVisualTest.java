// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3AnimatedVisibility;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3VisibilityState;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3ColorPicker;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the Catalog's Home, Component, and Example hierarchy in a real JavaFX window.
///
/// The test intentionally covers the Catalog as one application workflow rather than duplicating component-level
/// visual tests owned by the main demo suite. It validates the official-style navigation structure, adaptive home
/// grid, modal theme settings, and the ability to instantiate every registered example without CSS warnings.
@Tier2Test
@NotNullByDefault
final class M3FXCatalogVisualTest {
    /// The number of JavaFX pulses allowed to stabilize after a window resize.
    private static final int STABLE_PULSE_COUNT = 2;

    /// Starts the JavaFX toolkit before creating a real window.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies the complete Catalog route workflow and adaptive grid.
    @Test
    void catalogUsesComposeStyleHomeComponentAndExampleRoutes() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXCatalogApp> appReference = new AtomicReference<>();

        try {
            FxTestUtils.assertNoCssWarningsInterruptibly(() -> {
                FxTestUtils.runOnFxThread(() -> {
                    Stage stage = new Stage();
                    M3FXCatalogApp app = new M3FXCatalogApp();
                    app.start(stage);
                    stageReference.set(stage);
                    sceneReference.set(Objects.requireNonNull(stage.getScene(), "scene"));
                    appReference.set(app);
                });

                Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                M3FXCatalogApp app = Objects.requireNonNull(appReference.get(), "app");

                assertRegistry(app.components());
                assertRouteTransitionsAndSmoothScrolling(scene, app);
                assertHome(scene, app);
                assertHomeBrowserAndFavorites(scene, app);
                assertSidebarSearch(scene, app);
                assertAdaptiveGrid(scene, stage);
                assertBreakpointContinuity(scene, stage, app);
                assertComponentAndExampleNavigation(scene, app);
                assertSegmentedButtonIconStability(scene, app);
                assertExampleBrowserFiltering(scene, app);
                assertRouteStateRestoration(scene, app);
                assertSidebarScrollStability(scene, app);
                assertProfileSwitchPreservesRoute(scene, app);
                assertThemeSettings(scene, app);
                assertRightToLeftLayout(scene, app);
                assertExpandedComponentCoverage(scene, app);
                assertCompactExampleLayouts(scene, stage, app);
                assertCompactSidebarNavigation(scene, app);
                assertEveryExampleRenders(scene, app);
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies directional route replacement and smooth scrolling before enabling reduced motion for later checks.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertRouteTransitionsAndSmoothScrolling(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            M3MotionSettings.setReducedMotionRequested(scene.getRoot(), false);
            app.navigateHome();
            layout(scene);

            M3AnimatedContent routeHost = assertInstanceOf(
                    M3AnimatedContent.class,
                    Objects.requireNonNull(scene.lookup(".catalog-route-host"), "route host")
            );
            ScrollPane homeScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-scroll"), "home scroll pane")
            );
            CatalogSidebar sidebar = assertInstanceOf(
                    CatalogSidebar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-sidebar"), "Catalog sidebar")
            );
            ScrollPane sidebarScroll = drawerViewport(sidebar);
            assertTrue(M3ScrollPane.isSmoothScrollingEnabled(homeScroll));
            assertTrue(M3ScrollPane.isSmoothScrollingEnabled(sidebarScroll));
            assertEquals(ScrollPane.ScrollBarPolicy.NEVER, sidebarScroll.getVbarPolicy());

            CatalogComponent buttons = componentNamed(app.components(), "Buttons");
            app.navigate(new CatalogRoute.Component(buttons));
            assertTrue(routeHost.isTransitioning(), "forward navigation should animate route replacement");
            routeHost.finish();

            app.navigateBack();
            assertTrue(routeHost.isTransitioning(), "back navigation should animate route replacement");
            routeHost.finish();

            M3MotionSettings.setReducedMotionRequested(scene.getRoot(), true);
            layout(scene);
        });
    }

    /// Verifies registry size, ordering, uniqueness, and required links.
    ///
    /// @param components the Catalog registry
    private static void assertRegistry(List<CatalogComponent> components) {
        assertEquals(50, components.size());
        Set<String> names = new HashSet<>();
        String previous = "";
        int totalExamples = 0;
        for (CatalogComponent component : components) {
            assertTrue(names.add(component.name()), () -> "duplicate component: " + component.name());
            assertTrue(previous.compareToIgnoreCase(component.name()) <= 0, () -> "registry is not alphabetical");
            assertTrue(component.guidelinesUrl().startsWith("https://"));
            assertTrue(component.docsUrl().startsWith("https://"));
            assertTrue(component.sourceUrl().startsWith("https://github.com/Glavo/m3fx/"));
            assertFalse(component.examples().isEmpty());
            Set<String> exampleNames = new HashSet<>();
            for (CatalogExample example : component.examples()) {
                assertTrue(
                        exampleNames.add(example.name()),
                        () -> "duplicate example in " + component.name() + ": " + example.name()
                );
                assertTrue(example.sourceUrl().startsWith("https://github.com/Glavo/m3fx/"));
            }
            totalExamples += component.examples().size();
            previous = component.name();
        }
        assertEquals(332, totalExamples);
        assertEquals(
                Set.of(
                        "Adaptive",
                        "Avatars",
                        "Badges",
                        "Banners",
                        "Bottom app bars",
                        "Bottom sheets",
                        "Button groups",
                        "Buttons",
                        "Cards",
                        "Carousel",
                        "Checkboxes",
                        "Chips",
                        "Color pickers",
                        "Date pickers",
                        "Dialogs",
                        "Dividers",
                        "Drop zones",
                        "Extended FABs",
                        "FAB menu",
                        "Floating action buttons",
                        "Floating toolbars",
                        "Forms",
                        "Icon buttons",
                        "Icons",
                        "Lists",
                        "Loading indicators",
                        "Menus",
                        "Navigation bar",
                        "Navigation drawer",
                        "Navigation rail",
                        "Progress indicators",
                        "Radio buttons",
                        "Scrims",
                        "Scroll panes",
                        "Search",
                        "Segmented buttons",
                        "Settings",
                        "Side sheets",
                        "Sliders",
                        "Snackbars",
                        "Split buttons",
                        "Status lights",
                        "Surfaces",
                        "Switches",
                        "Tabs",
                        "Text fields",
                        "Time pickers",
                        "Tooltips",
                        "Top app bars",
                        "Typography"
                ),
                names
        );
        assertEquals(7, componentNamed(components, "Adaptive").examples().size());
        assertEquals(17, componentNamed(components, "Buttons").examples().size());
        assertEquals(16, componentNamed(components, "Chips").examples().size());
        assertEquals(7, componentNamed(components, "Date pickers").examples().size());
        assertEquals(8, componentNamed(components, "Lists").examples().size());
        assertEquals(6, componentNamed(components, "Search").examples().size());
        assertEquals(6, componentNamed(components, "Scroll panes").examples().size());
        assertEquals(3, componentNamed(components, "Side sheets").examples().size());
        assertEquals(4, componentNamed(components, "Status lights").examples().size());
        assertEquals(20, componentNamed(components, "Text fields").examples().size());
        assertEquals(10, componentNamed(components, "Top app bars").examples().size());
    }

    /// Verifies the alphabetical grid and absence of the former destination shell.
    ///
    /// @param scene the Catalog scene
    /// @param app   the running Catalog application
    private static void assertHome(Scene scene, M3FXCatalogApp app) throws InterruptedException {
        FxTestUtils.runOnFxThread(() -> {
            app.navigateHome();
            layout(scene);
            assertInstanceOf(CatalogRoute.Home.class, app.currentRoute());
            TilePane grid = assertInstanceOf(
                    TilePane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-grid"), "component grid")
            );
            assertEquals(app.components().size(), grid.getChildren().size());
            Node firstCell = grid.getChildren().get(0);
            M3Card firstCard = assertInstanceOf(
                    M3Card.class,
                    Objects.requireNonNull(firstCell.lookup(".catalog-component-card"), "component card")
            );
            Node cardSurface = Objects.requireNonNull(firstCard.lookup(".m3-card-container"), "card surface");
            assertEquals(180.0, firstCell.getLayoutBounds().getHeight(), 0.5);
            assertEquals(firstCell.getLayoutBounds().getWidth() - 8.0, firstCard.getWidth(), 0.5);
            assertEquals(172.0, firstCard.getHeight(), 0.5);
            assertEquals(firstCard.getWidth(), cardSurface.getLayoutBounds().getWidth(), 0.5);
            firstCard.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            scene.getRoot().applyCss();
            assertNull(cardSurface.getEffect(), "Catalog component cards must not add delayed hover elevation");
            firstCard.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
            scene.getRoot().applyCss();
            M3SVGIcon firstIcon = assertInstanceOf(
                    M3SVGIcon.class,
                    Objects.requireNonNull(firstCard.lookup(".catalog-component-card-icon"), "component icon")
            );
            assertEquals(new Rectangle2D(0.0, 0.0, 24.0, 24.0), firstIcon.getViewBox());
            assertEquals(80.0, firstIcon.getIconSize(), 0.01);
            assertTrue(scene.getRoot().lookupAll(".catalog-icon").stream()
                    .allMatch(M3SVGIcon.class::isInstance));
            assertNotNull(scene.lookup(".catalog-top-app-bar"));
            CatalogSidebar sidebar = assertInstanceOf(
                    CatalogSidebar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-sidebar"), "Catalog sidebar")
            );
            assertEquals(360.0, sidebar.getWidth(), 0.5);
            assertEquals(M3NavigationDrawerVariant.STANDARD, sidebar.drawer().getVariant());
            assertEquals(app.components().size(), scene.getRoot().lookupAll(".catalog-sidebar-component").size());
            assertTrue(scene.getRoot().lookupAll(".catalog-sidebar-example").isEmpty());
            assertTrue(scene.getRoot().lookupAll(".catalog-sidebar-component-group").isEmpty());
            M3ListItem homeItem = assertInstanceOf(
                    M3ListItem.class,
                    Objects.requireNonNull(scene.lookup(".catalog-sidebar-home"), "sidebar Home item")
            );
            assertSame(homeItem, sidebar.drawer().getSelectedItem());
            assertEquals("Home", homeItem.getHeadlineText());
            assertNull(scene.lookup(".catalog-sidebar-action"));
            assertNull(scene.lookup(".catalog-navigation-drawer"));
            assertNull(scene.lookup(".catalog-navigation-rail"));
            assertNull(scene.lookup(".catalog-navigation-bar"));
            M3MenuButton overflowAction = assertInstanceOf(
                    M3MenuButton.class,
                    Objects.requireNonNull(scene.lookup(".catalog-overflow-action"), "overflow action")
            );
            assertEquals(M3ButtonVariant.TEXT, overflowAction.getVariant());
            assertEquals(10.0, overflowAction.getHorizontalPadding(), 0.01);
            assertEquals(48.0, overflowAction.getWidth(), 0.5);
            assertEquals(48.0, overflowAction.getHeight(), 0.5);
        });
    }

    /// Verifies that the home tile pane derives more columns from a wider viewport.
    ///
    /// @param scene the Catalog scene
    /// @param stage the Catalog stage
    private static void assertAdaptiveGrid(Scene scene, Stage stage) throws InterruptedException {
        AtomicInteger compactColumns = new AtomicInteger();
        FxTestUtils.runOnFxThreadWhenStable(
                () -> scene.lookup(".catalog-component-grid") != null,
                STABLE_PULSE_COUNT,
                () -> stage.setWidth(520.0),
                () -> compactColumns.set(columnCount(scene))
        );
        FxTestUtils.runOnFxThreadWhenStable(
                () -> scene.lookup(".catalog-component-grid") != null,
                STABLE_PULSE_COUNT,
                () -> stage.setWidth(1_120.0),
                () -> {
                    assertTrue(columnCount(scene) > compactColumns.get());
                    assertGridUsesAvailableWidth(scene);
                }
        );
    }

    /// Verifies that crossing the expanded breakpoint cannot retain a stale modal drawer or lose route selection.
    ///
    /// @param scene the Catalog scene
    /// @param stage the Catalog stage
    /// @param app the running Catalog application
    private static void assertBreakpointContinuity(
            Scene scene,
            Stage stage,
            M3FXCatalogApp app
    ) throws InterruptedException {
        CatalogComponent buttons = componentNamed(app.components(), "Buttons");
        FxTestUtils.runOnFxThreadWhenStable(
                () -> Math.abs(scene.getWidth() - 839.0) <= 0.5
                        && scene.lookup(".catalog-sidebar-action") != null,
                STABLE_PULSE_COUNT,
                () -> {
                    app.navigate(new CatalogRoute.Component(buttons));
                    resizeSceneToWidth(stage, scene, 839.0);
                },
                () -> {
                    M3IconButton browseButton = assertInstanceOf(
                            M3IconButton.class,
                            Objects.requireNonNull(scene.lookup(".catalog-sidebar-action"), "modal sidebar action")
                    );
                    browseButton.fire();
                    M3AnimatedVisibility visibility = assertInstanceOf(
                            M3AnimatedVisibility.class,
                            Objects.requireNonNull(
                                    scene.lookup(".catalog-sidebar-visibility"),
                                    "modal sidebar visibility"
                            )
                    );
                    assertTrue(visibility.isShowing());
                }
        );

        FxTestUtils.runOnFxThreadWhenStable(
                () -> Math.abs(scene.getWidth() - 840.0) <= 0.5
                        && scene.lookup(".catalog-sidebar-action") == null
                        && scene.lookup(".catalog-sidebar-scrim") == null,
                STABLE_PULSE_COUNT,
                () -> resizeSceneToWidth(stage, scene, 840.0),
                () -> {
                    CatalogSidebar sidebar = visibleSidebar(scene);
                    assertEquals(M3NavigationDrawerVariant.STANDARD, sidebar.drawer().getVariant());
                    assertEquals(360.0, sidebar.getWidth(), 0.5);
                    assertEquals(
                            "Buttons",
                            Objects.requireNonNull(
                                    sidebar.drawer().getSelectedItem(),
                                    "selected standard destination"
                            ).getHeadlineText()
                    );
                    assertInstanceOf(CatalogRoute.Component.class, app.currentRoute());
                }
        );

        FxTestUtils.runOnFxThreadWhenStable(
                () -> Math.abs(scene.getWidth() - 839.0) <= 0.5
                        && scene.lookup(".catalog-sidebar-action") != null
                        && scene.lookup(".catalog-sidebar") == null,
                STABLE_PULSE_COUNT,
                () -> resizeSceneToWidth(stage, scene, 839.0),
                () -> assertInstanceOf(CatalogRoute.Component.class, app.currentRoute())
        );

        FxTestUtils.runOnFxThreadWhenStable(
                () -> scene.getWidth() >= 1_100.0 && scene.lookup(".catalog-sidebar") != null,
                STABLE_PULSE_COUNT,
                () -> resizeSceneToWidth(stage, scene, 1_120.0),
                () -> {
                    CatalogSidebar sidebar = visibleSidebar(scene);
                    assertEquals(M3NavigationDrawerVariant.STANDARD, sidebar.drawer().getVariant());
                    assertEquals(
                            "Buttons",
                            Objects.requireNonNull(
                                    sidebar.drawer().getSelectedItem(),
                                    "restored standard destination"
                            ).getHeadlineText()
                    );
                }
        );
    }

    /// Verifies Home search, collection filtering, empty state, and the favorite browsing loop.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertHomeBrowserAndFavorites(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            app.navigateHome();
            layout(scene);

            Parent root = scene.getRoot();
            M3SearchBar search = assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-search"), "Home component search")
            );
            Set<Node> originalCells = Set.copyOf(root.lookupAll(".catalog-component-cell"));

            search.setText("Selected filter chip");
            layout(scene);
            assertEquals(1L, visibleNodeCount(root, ".catalog-component-cell"));
            assertEquals(
                    originalCells,
                    Set.copyOf(root.lookupAll(".catalog-component-cell")),
                    "Home search must not rebuild component cells"
            );

            search.clear();
            M3SegmentedButton expressiveFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-home-filter-expressive"),
                            "Expressive component filter"
                    )
            );
            expressiveFilter.fire();
            layout(scene);
            long expressiveCount = app.components().stream()
                    .filter(CatalogComponent::hasExpressiveExamples)
                    .count();
            assertTrue(expressiveCount > 0 && expressiveCount < app.components().size());
            assertEquals(expressiveCount, visibleNodeCount(root, ".catalog-component-cell"));
            assertEquals(
                    originalCells,
                    Set.copyOf(root.lookupAll(".catalog-component-cell")),
                    "Home collection filters must not rebuild component cells"
            );

            M3SegmentedButton allFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-filter-all"), "All component filter")
            );
            allFilter.fire();
            search.setText("No component has this phrase");
            layout(scene);
            assertEquals(0L, visibleNodeCount(root, ".catalog-component-cell"));
            assertFalse(Objects.requireNonNull(
                    scene.lookup(".catalog-component-grid"),
                    "component grid"
            ).isVisible());
            assertTrue(Objects.requireNonNull(
                    scene.lookup(".catalog-home-empty-state"),
                    "Home empty state"
            ).isVisible());

            search.clear();
            M3SegmentedButton favoritesFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-home-filter-favorites"),
                            "Favorites component filter"
                    )
            );
            favoritesFilter.fire();
            layout(scene);
            assertEquals(0L, visibleNodeCount(root, ".catalog-component-cell"));

            CatalogComponent buttons = componentNamed(app.components(), "Buttons");
            app.navigate(new CatalogRoute.Component(buttons));
            layout(scene);
            M3IconToggleButton favoriteAction = assertInstanceOf(
                    M3IconToggleButton.class,
                    Objects.requireNonNull(scene.lookup(".catalog-favorite-action"), "favorite action")
            );
            assertFalse(favoriteAction.isSelected());
            favoriteAction.fire();
            assertTrue(favoriteAction.isSelected());

            app.navigateHome();
            layout(scene);
            favoritesFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-home-filter-favorites"),
                            "Favorites component filter"
                    )
            );
            favoritesFilter.fire();
            layout(scene);
            assertEquals(1L, visibleNodeCount(root, ".catalog-component-cell"));
            assertEquals(1L, visibleNodeCount(root, ".catalog-component-card-favorite-marker"));

            app.navigate(new CatalogRoute.Component(buttons));
            layout(scene);
            favoriteAction = assertInstanceOf(
                    M3IconToggleButton.class,
                    Objects.requireNonNull(scene.lookup(".catalog-favorite-action"), "favorite action")
            );
            assertTrue(favoriteAction.isSelected());
            favoriteAction.fire();
            assertFalse(favoriteAction.isSelected());
            assertInstanceOf(M3OverlayPane.class, root).dismissAllSnackbars();
            app.navigateHome();
            layout(scene);
            assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-filter-all"), "All component filter")
            ).fire();
            layout(scene);
        });
    }

    /// Verifies that sidebar search filters persistent destinations and restores route selection when cleared.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertSidebarSearch(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            CatalogComponent buttons = componentNamed(app.components(), "Buttons");
            app.navigate(new CatalogRoute.Component(buttons));
            layout(scene);

            CatalogSidebar sidebar = visibleSidebar(scene);
            M3SearchBar search = assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(sidebar.lookup(".catalog-sidebar-search"), "sidebar component search")
            );
            assertEquals(new Insets(0.0, 12.0, 0.0, 12.0), search.getPadding());
            assertEquals(new Insets(8.0, 0.0, 0.0, 0.0), VBox.getMargin(search));
            assertEquals(48.0, search.getHeight(), 0.5);
            assertNull(search.getEffect(), "the drawer search must not render as a floating surface");
            Bounds searchBounds = search.localToScene(search.getBoundsInLocal());
            Node searchContent = Objects.requireNonNull(
                    search.lookup(".m3-search-bar-content"),
                    "search bar content"
            );
            Bounds contentBounds = searchContent.localToScene(searchContent.getBoundsInLocal());
            assertEquals(
                    searchBounds.getCenterY(),
                    contentBounds.getCenterY(),
                    0.5,
                    "search content must remain vertically centered"
            );
            Set<Node> originalItems = Set.copyOf(sidebar.lookupAll(".catalog-sidebar-component"));
            M3ListItem buttonsItem = listItemNamed(sidebar, ".catalog-sidebar-component", "Buttons");
            assertSame(buttonsItem, sidebar.drawer().getSelectedItem());

            search.setText("Navigation drawer");
            layout(scene);
            assertEquals(1L, visibleNodeCount(sidebar, ".catalog-sidebar-component"));
            assertTrue(listItemNamed(
                    sidebar,
                    ".catalog-sidebar-component",
                    "Navigation drawer"
            ).isVisible());
            assertSame(
                    sidebar.lookup(".catalog-sidebar-home"),
                    sidebar.drawer().getSelectedItem(),
                    "a hidden active destination should fall back to Home"
            );
            assertEquals(
                    originalItems,
                    Set.copyOf(sidebar.lookupAll(".catalog-sidebar-component")),
                    "search must not rebuild component destinations"
            );

            search.setText("No component has this phrase");
            layout(scene);
            assertEquals(0L, visibleNodeCount(sidebar, ".catalog-sidebar-component"));
            assertTrue(Objects.requireNonNull(
                    sidebar.lookup(".catalog-sidebar-empty"),
                    "sidebar empty state"
            ).isVisible());

            search.clear();
            layout(scene);
            assertEquals(app.components().size(), visibleNodeCount(sidebar, ".catalog-sidebar-component"));
            assertSame(buttonsItem, sidebar.drawer().getSelectedItem());
            app.navigateHome();
            layout(scene);
        });
    }

    /// Verifies component-card navigation, example-card navigation, and back behavior.
    ///
    /// @param scene the Catalog scene
    /// @param app   the running Catalog application
    private static void assertComponentAndExampleNavigation(
            Scene scene,
            M3FXCatalogApp app
    ) throws InterruptedException {
        FxTestUtils.runOnFxThread(() -> {
            CatalogComponent chips = app.components().stream()
                    .filter(component -> component.name().equals("Chips"))
                    .findFirst()
                    .orElseThrow();
            app.navigateHome();
            app.navigate(new CatalogRoute.Component(chips));
            layout(scene);

            assertInstanceOf(CatalogRoute.Component.class, app.currentRoute());
            Node componentPage = Objects.requireNonNull(
                    scene.lookup(".catalog-component-page"),
                    "component page"
            );
            ScrollPane componentScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-scroll"), "component scroll pane")
            );
            assertEquals(
                    componentScroll.getViewportBounds().getWidth(),
                    componentPage.getLayoutBounds().getWidth(),
                    0.5
            );
            assertTrue(M3ScrollPane.isSmoothScrollingEnabled(componentScroll));
            assertEquals(16, scene.getRoot().lookupAll(".catalog-example-card").size());
            assertEquals(16, scene.getRoot().lookupAll(".catalog-example-cell").size());
            assertEquals(3, scene.getRoot().lookupAll(".catalog-component-reference-action").size());
            assertNotNull(scene.lookup(".catalog-component-reference"));
            M3Card firstExampleCard = assertInstanceOf(
                    M3Card.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-card"), "first example card")
            );
            Node firstExampleCardSurface = Objects.requireNonNull(
                    firstExampleCard.lookup(".m3-card-container"),
                    "first example card surface"
            );
            firstExampleCard.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            scene.getRoot().applyCss();
            assertNull(
                    firstExampleCardSurface.getEffect(),
                    "Catalog example cards must not add delayed hover elevation"
            );
            firstExampleCard.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
            scene.getRoot().applyCss();
            TilePane exampleGrid = assertInstanceOf(
                    TilePane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-grid"), "example grid")
            );
            assertTrue(
                    exampleGrid.getChildren().stream().map(Node::getLayoutX).distinct().count() > 1,
                    "expanded component pages should use more than one example column"
            );
            CatalogSidebar sidebar = visibleSidebar(scene);
            M3ListItem selectedComponent = Objects.requireNonNull(
                    sidebar.drawer().getSelectedItem(),
                    "selected sidebar component"
            );
            assertEquals("Chips", selectedComponent.getHeadlineText());

            CatalogExample example = chips.examples().get(0);
            app.navigate(new CatalogRoute.Example(chips, example));
            layout(scene);
            assertInstanceOf(CatalogRoute.Example.class, app.currentRoute());
            assertNotNull(scene.lookup(".catalog-example-page"));
            assertNotNull(scene.lookup(".catalog-example-detail-header"));
            assertNotNull(scene.lookup(".catalog-sample-surface"));
            Parent sampleContent = assertInstanceOf(
                    Parent.class,
                    Objects.requireNonNull(scene.lookup(".catalog-sample-content"), "live sample content")
            );
            assertEquals(1, sampleContent.getChildrenUnmodifiable().size());
            M3Text profile = assertInstanceOf(
                    M3Text.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-profile"), "example profile")
            );
            assertEquals("Baseline", profile.getText());
            assertNotNull(scene.lookup(".catalog-example-source-action"));
            M3ListItem selectedOwner = Objects.requireNonNull(
                    sidebar.drawer().getSelectedItem(),
                    "selected sidebar component owner"
            );
            assertEquals("Chips", selectedOwner.getHeadlineText());

            M3Button backAction = assertInstanceOf(
                    M3Button.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-back-action"), "example back action")
            );
            backAction.fire();
            assertInstanceOf(CatalogRoute.Component.class, app.currentRoute());
            app.navigateBack();
            assertInstanceOf(CatalogRoute.Home.class, app.currentRoute());

            CatalogComponent sliders = componentNamed(app.components(), "Sliders");
            CatalogExample expressiveExample = sliders.examples().stream()
                    .filter(CatalogExample::expressive)
                    .findFirst()
                    .orElseThrow();
            app.navigate(new CatalogRoute.Example(sliders, expressiveExample));
            layout(scene);
            profile = assertInstanceOf(
                    M3Text.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-profile"), "Expressive example profile")
            );
            assertEquals("Expressive", profile.getText());
            assertTrue(profile.getStyleClass().contains("catalog-example-profile-expressive"));
            app.navigateHome();
            layout(scene);
        });
    }

    /// Verifies that switching an icon-bearing segmented group keeps every leading slot at a fixed position.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertSegmentedButtonIconStability(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            Parent sampleContent = openExample(
                    scene,
                    app,
                    "Segmented buttons",
                    "Icon and label"
            );
            M3SegmentedButtonGroup group = assertInstanceOf(
                    M3SegmentedButtonGroup.class,
                    Objects.requireNonNull(
                            sampleContent.lookup(".m3-segmented-button-group"),
                            "icon segmented group"
                    )
            );
            M3SegmentedButton day = group.getItems().get(0);
            M3SegmentedButton week = group.getItems().get(1);
            Node dayGraphic = Objects.requireNonNull(day.getGraphic(), "Day graphic");
            Node weekGraphic = Objects.requireNonNull(week.getGraphic(), "Week graphic");
            layout(scene);

            double dayGraphicCenterBefore = sceneCenterX(dayGraphic);
            double weekGraphicCenterBefore = sceneCenterX(weekGraphic);
            Region weekIndicator = assertInstanceOf(
                    Region.class,
                    Objects.requireNonNull(
                            week.lookup(".m3-segmented-button-selection-indicator"),
                            "Week selection indicator"
                    )
            );
            assertEquals(weekGraphicCenterBefore, sceneCenterX(weekIndicator), 0.01);
            assertSame(weekGraphic.getParent(), weekIndicator.getParent());
            assertTrue(weekIndicator.getViewOrder() < weekGraphic.getViewOrder());
            assertEquals(18.0, dayGraphic.getLayoutBounds().getWidth(), 0.01);
            assertEquals(18.0, weekGraphic.getLayoutBounds().getWidth(), 0.01);
            assertEquals(0.0, weekGraphic.getOpacity(), 0.0001);
            assertNull(week.lookup(".m3-segmented-button-selection-indicator-backdrop"));
            Region weekMark = assertInstanceOf(
                    Region.class,
                    Objects.requireNonNull(
                            week.lookup(".m3-segmented-button-selection-indicator-mark"),
                            "Week selection indicator mark"
                    )
            );
            assertEquals(18.0, weekMark.getWidth(), 0.01);
            assertFalse(weekMark.getBackground().getFills().isEmpty());

            day.fire();
            layout(scene);
            assertTrue(day.isSelected());
            assertFalse(week.isSelected());

            Region dayIndicator = assertInstanceOf(
                    Region.class,
                    Objects.requireNonNull(
                            day.lookup(".m3-segmented-button-selection-indicator"),
                            "Day selection indicator"
                    )
            );
            assertEquals(dayGraphicCenterBefore, sceneCenterX(dayGraphic), 0.01);
            assertEquals(weekGraphicCenterBefore, sceneCenterX(weekGraphic), 0.01);
            assertEquals(dayGraphicCenterBefore, sceneCenterX(dayIndicator), 0.01);
            assertEquals(0.0, dayGraphic.getOpacity(), 0.0001);
            assertEquals(1.0, weekGraphic.getOpacity(), 0.0001);
            assertNull(day.lookup(".m3-segmented-button-selection-indicator-backdrop"));
            Region dayMark = assertInstanceOf(
                    Region.class,
                    Objects.requireNonNull(
                            day.lookup(".m3-segmented-button-selection-indicator-mark"),
                            "Day selection indicator mark"
                    )
            );
            assertEquals(18.0, dayMark.getWidth(), 0.01);
            assertFalse(dayMark.getBackground().getFills().isEmpty());
        });
    }

    /// Verifies example search, profile filtering, empty state, and cell identity preservation.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertExampleBrowserFiltering(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            CatalogComponent chips = componentNamed(app.components(), "Chips");
            app.navigate(new CatalogRoute.Component(chips));
            layout(scene);

            M3SearchBar search = assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-search"), "example search")
            );
            Set<Node> originalCells = Set.copyOf(scene.getRoot().lookupAll(".catalog-example-cell"));
            search.setText("Selected filter chip");
            layout(scene);
            assertEquals(1L, visibleNodeCount(scene.getRoot(), ".catalog-example-cell"));
            assertEquals(
                    originalCells,
                    Set.copyOf(scene.getRoot().lookupAll(".catalog-example-cell")),
                    "search must not rebuild example cells"
            );

            search.clear();
            M3SegmentedButton expressiveFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-example-filter-expressive"),
                            "Expressive example filter"
                    )
            );
            expressiveFilter.fire();
            layout(scene);
            assertEquals(0L, visibleNodeCount(scene.getRoot(), ".catalog-example-cell"));
            assertTrue(Objects.requireNonNull(
                    scene.lookup(".catalog-example-empty-state"),
                    "example empty state"
            ).isVisible());

            M3SegmentedButton allFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-filter-all"), "All example filter")
            );
            allFilter.fire();
            layout(scene);
            assertEquals(chips.examples().size(), visibleNodeCount(scene.getRoot(), ".catalog-example-cell"));
            assertFalse(Objects.requireNonNull(
                    scene.lookup(".catalog-example-empty-state"),
                    "example empty state"
            ).isVisible());

            CatalogComponent sliders = componentNamed(app.components(), "Sliders");
            app.navigate(new CatalogRoute.Component(sliders));
            layout(scene);
            long expressiveCount = sliders.examples().stream().filter(CatalogExample::expressive).count();
            assertTrue(expressiveCount > 0 && expressiveCount < sliders.examples().size());
            expressiveFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-example-filter-expressive"),
                            "Expressive example filter"
                    )
            );
            expressiveFilter.fire();
            layout(scene);
            assertEquals(expressiveCount, visibleNodeCount(scene.getRoot(), ".catalog-example-cell"));
            app.navigateHome();
            layout(scene);
        });
    }

    /// Verifies that route reconstruction restores browser controls and independent scroll positions.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertRouteStateRestoration(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            app.navigateHome();
            layout(scene);

            M3SearchBar homeSearch = assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-search"), "Home component search")
            );
            M3SegmentedButton expressiveFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-home-filter-expressive"),
                            "Expressive component filter"
                    )
            );
            ScrollPane homeScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-scroll"), "Home scroll pane")
            );
            homeSearch.setText("a");
            expressiveFilter.fire();
            homeScroll.setVvalue(0.37);

            CatalogComponent chips = componentNamed(app.components(), "Chips");
            app.navigate(new CatalogRoute.Component(chips));
            layout(scene);
            app.navigateBack();
            layout(scene);

            homeSearch = assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-search"), "restored Home component search")
            );
            expressiveFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-home-filter-expressive"),
                            "restored Expressive component filter"
                    )
            );
            homeScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-scroll"), "restored Home scroll pane")
            );
            assertEquals("a", homeSearch.getText());
            assertTrue(expressiveFilter.isSelected());
            assertEquals(0.37, homeScroll.getVvalue(), 0.001);

            homeSearch.clear();
            assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(scene.lookup(".catalog-home-filter-all"), "All component filter")
            ).fire();
            homeScroll.setVvalue(0.0);

            app.navigate(new CatalogRoute.Component(chips));
            layout(scene);
            M3SearchBar exampleSearch = assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-search"), "example search")
            );
            M3SegmentedButton baselineFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-example-filter-baseline"),
                            "Baseline example filter"
                    )
            );
            ScrollPane componentScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-scroll"), "component scroll pane")
            );
            exampleSearch.setText("chip");
            baselineFilter.fire();
            componentScroll.setVvalue(0.43);

            CatalogExample selectedFilterChip = exampleNamed(chips, "Selected filter chip");
            CatalogRoute.Example exampleRoute = new CatalogRoute.Example(chips, selectedFilterChip);
            app.navigate(exampleRoute);
            layout(scene);
            ScrollPane exampleScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-scroll"), "example scroll pane")
            );
            exampleScroll.setVvalue(0.61);
            app.navigateBack();
            layout(scene);

            exampleSearch = assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-search"), "restored example search")
            );
            baselineFilter = assertInstanceOf(
                    M3SegmentedButton.class,
                    Objects.requireNonNull(
                            scene.lookup(".catalog-example-filter-baseline"),
                            "restored Baseline example filter"
                    )
            );
            componentScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-scroll"), "restored component scroll pane")
            );
            assertEquals("chip", exampleSearch.getText());
            assertTrue(baselineFilter.isSelected());
            assertEquals(0.43, componentScroll.getVvalue(), 0.001);

            app.navigate(exampleRoute);
            layout(scene);
            exampleScroll = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-scroll"), "restored example scroll pane")
            );
            assertEquals(0.61, exampleScroll.getVvalue(), 0.001);

            app.navigateBack();
            layout(scene);
            assertInstanceOf(
                    M3SearchBar.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-search"), "example search")
            ).clear();
            M3SegmentedButtonGroup filterGroup = assertInstanceOf(
                    M3SegmentedButtonGroup.class,
                    Objects.requireNonNull(scene.lookup(".catalog-example-filter-group"), "example filter group")
            );
            filterGroup.selectIndex(0);
            assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-scroll"), "component scroll pane")
            ).setVvalue(0.0);
            app.navigateHome();
            layout(scene);
        });
    }

    /// Verifies that destination activation preserves the persistent drawer hierarchy and exact scroll position.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertSidebarScrollStability(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            CatalogComponent buttons = componentNamed(app.components(), "Buttons");
            app.navigate(new CatalogRoute.Component(buttons));
            layout(scene);

            CatalogSidebar sidebar = visibleSidebar(scene);
            M3NavigationDrawer drawer = sidebar.drawer();
            ScrollPane viewport = drawerViewport(sidebar);
            M3ListItem buttonsItem = listItemNamed(sidebar, ".catalog-sidebar-component", "Buttons");
            M3ListItem cardsItem = listItemNamed(sidebar, ".catalog-sidebar-component", "Cards");
            assertTrue(sidebar.lookupAll(".catalog-sidebar-example").isEmpty());

            viewport.setVvalue(0.42);
            layout(scene);
            double initialVvalue = viewport.getVvalue();

            cardsItem.fire();
            layout(scene);
            assertSame(drawer, sidebar.drawer(), "route changes must retain the drawer");
            assertSame(cardsItem, drawer.getSelectedItem(), "selection must retain the destination node");
            assertSame(buttonsItem, listItemNamed(sidebar, ".catalog-sidebar-component", "Buttons"));
            assertEquals(initialVvalue, viewport.getVvalue(), 1.0e-9, "Cards changed scroll position");

            buttonsItem.fire();
            layout(scene);
            assertSame(buttonsItem, drawer.getSelectedItem(), "selection must retain the destination node");
            assertSame(cardsItem, listItemNamed(sidebar, ".catalog-sidebar-component", "Cards"));
            assertEquals(initialVvalue, viewport.getVvalue(), 1.0e-9, "Buttons changed scroll position");
            app.navigateHome();
            layout(scene);
        });
    }

    /// Verifies that changing the component profile updates CSS without rebuilding route content.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    private static void assertProfileSwitchPreservesRoute(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            M3MotionSettings.setReducedMotionRequested(scene.getRoot(), true);
            CatalogComponent buttons = componentNamed(app.components(), "Buttons");
            app.navigate(new CatalogRoute.Component(buttons));
            layout(scene);

            Node page = Objects.requireNonNull(scene.lookup(".catalog-component-page"), "component page");
            ScrollPane scrollPane = assertInstanceOf(
                    ScrollPane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-scroll"), "component scroll pane")
            );
            scrollPane.setVvalue(0.37);
            layout(scene);
            double scrollPosition = scrollPane.getVvalue();

            app.showSettings();
            layout(scene);
            M3Switch expressiveSwitch = scene.getRoot().lookupAll(".m3-switch").stream()
                    .filter(M3Switch.class::isInstance)
                    .map(M3Switch.class::cast)
                    .filter(control -> control.getText().equals("Expressive components"))
                    .findFirst()
                    .orElseThrow();
            expressiveSwitch.fire();
            layout(scene);

            assertSame(page, scene.lookup(".catalog-component-page"));
            assertSame(scrollPane, scene.lookup(".catalog-component-scroll"));
            assertEquals(
                    scrollPosition,
                    scrollPane.getVvalue(),
                    0.05,
                    "profile-dependent layout must not reset the retained viewport to the top"
            );

            expressiveSwitch.fire();
            app.hideSettings();
            app.navigateHome();
            layout(scene);
        });
    }

    /// Verifies the coordinated modal sheet and scrim lifecycle.
    ///
    /// @param scene the Catalog scene
    /// @param app   the running Catalog application
    private static void assertThemeSettings(Scene scene, M3FXCatalogApp app) throws InterruptedException {
        AtomicReference<@Nullable M3OverlayPane> overlayReference = new AtomicReference<>();
        AtomicReference<@Nullable Parent> layerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3BottomSheet> sheetReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Scrim> scrimReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> previousFocusReference = new AtomicReference<>();
        AtomicBoolean detachedBeforeBothExitsFinished = new AtomicBoolean();

        FxTestUtils.runOnFxThread(() -> {
            M3MotionSettings.setReducedMotionRequested(scene.getRoot(), false);
            M3OverlayPane overlay = assertInstanceOf(
                    M3OverlayPane.class,
                    scene.getRoot(),
                    "Catalog scene root"
            );
            overlayReference.set(overlay);
            assertNull(scene.lookup(".catalog-settings-sheet"), "settings sheet should start detached");
            assertNull(scene.lookup(".catalog-settings-scrim"), "settings scrim should start detached");

            Node previousFocus = Objects.requireNonNull(
                    scene.lookup(".catalog-component-card"),
                    "focus owner before settings"
            );
            previousFocus.requestFocus();
            assertSame(previousFocus, scene.getFocusOwner());
            previousFocusReference.set(previousFocus);
        });

        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    @Nullable Node sheetNode = scene.lookup(".catalog-settings-sheet");
                    @Nullable Node scrimNode = scene.lookup(".catalog-settings-scrim");
                    if (!(sheetNode instanceof M3BottomSheet sheet) || !(scrimNode instanceof M3Scrim scrim)) {
                        return false;
                    }
                    layout(scene);
                    @Nullable Parent layer = sheet.getParent();
                    @Nullable Node focusOwner = scene.getFocusOwner();
                    return layer != null
                            && layer == scrim.getParent()
                            && layer.getParent() == overlayReference.get()
                            && sheet.isShown()
                            && sheet.isVisible()
                            && scrim.isShown()
                            && scrim.isVisible()
                            && Math.abs(sheet.getTranslateY()) <= 0.5
                            && sheet.getOpacity() >= 0.99
                            && scrim.getOpacity() >= scrim.getVisibleOpacity() - 0.01
                            && focusOwner != null
                            && containsNode(layer, focusOwner);
                },
                STABLE_PULSE_COUNT,
                app::showSettings,
                () -> {
                    M3OverlayPane overlay = Objects.requireNonNull(overlayReference.get(), "overlay pane");
                    assertSame(overlay, scene.getRoot(), "showing settings must keep the Catalog scene root stable");

                    M3BottomSheet sheet = assertInstanceOf(
                            M3BottomSheet.class,
                            Objects.requireNonNull(scene.lookup(".catalog-settings-sheet"), "settings sheet")
                    );
                    M3Scrim scrim = assertInstanceOf(
                            M3Scrim.class,
                            Objects.requireNonNull(scene.lookup(".catalog-settings-scrim"), "settings scrim")
                    );
                    Parent layer = Objects.requireNonNull(sheet.getParent(), "settings layer");
                    layerReference.set(layer);
                    sheetReference.set(sheet);
                    scrimReference.set(scrim);

                    assertSame(layer, scrim.getParent(), "settings sheet and scrim should share one layer");
                    assertSame(overlay, layer.getParent(), "settings layer should be attached to the overlay pane");
                    assertSame(scene, layer.getScene());
                    assertTrue(layer.isVisible());
                    assertTrue(sheet.isShown());
                    assertTrue(sheet.isVisible());
                    assertTrue(sheet.isManaged());
                    assertTrue(scrim.isShown());
                    assertTrue(scrim.isVisible());
                    assertTrue(scrim.isManaged());
                    assertEquals(1.0, sheet.getOpacity(), 0.01);
                    assertEquals(scrim.getVisibleOpacity(), scrim.getOpacity(), 0.01);
                    assertEquals(0.0, sheet.getTranslateY(), 0.5);
                    double horizontalPixel = 1.0
                            / Objects.requireNonNull(scene.getWindow(), "window").getOutputScaleX();
                    double verticalPixel = 1.0 / scene.getWindow().getOutputScaleY();
                    assertEquals(overlay.getWidth(), layer.getLayoutBounds().getWidth(), horizontalPixel + 0.01);
                    assertEquals(overlay.getHeight(), layer.getLayoutBounds().getHeight(), verticalPixel + 0.01);
                    assertTrue(sheet.getHeight() <= 680.5);
                    assertTrue(sheet.localToScene(sheet.getBoundsInLocal()).getMinY() >= 15.0);
                    ScrollPane settingsScroll = assertInstanceOf(
                            ScrollPane.class,
                            Objects.requireNonNull(
                                    scene.lookup(".catalog-settings-scroll"),
                                    "settings scroll pane"
                            )
                    );
                    assertTrue(M3ScrollPane.isSmoothScrollingEnabled(settingsScroll));

                    Node focusOwner = Objects.requireNonNull(scene.getFocusOwner(), "settings focus owner");
                    assertTrue(containsNode(layer, focusOwner), "modal settings should contain keyboard focus");
                    layer.parentProperty().addListener((observable, oldParent, newParent) -> {
                        if (newParent == null && (sheet.isVisible() || scrim.isVisible())) {
                            detachedBeforeBothExitsFinished.set(true);
                        }
                    });
                }
        );

        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane overlay = Objects.requireNonNull(overlayReference.get(), "overlay pane");
            Parent layer = Objects.requireNonNull(layerReference.get(), "settings layer");
            M3BottomSheet sheet = Objects.requireNonNull(sheetReference.get(), "settings sheet");
            M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "settings scrim");
            app.hideSettings();
            assertFalse(sheet.isShown());
            assertFalse(scrim.isShown());
            assertTrue(sheet.isVisible(), "settings sheet should remain rendered during its exit transition");
            assertTrue(scrim.isVisible(), "settings scrim should remain rendered during its exit transition");
            assertSame(layer, sheet.getParent());
            assertSame(layer, scrim.getParent());
            assertSame(overlay, layer.getParent(), "settings layer should remain attached during both exits");
            assertSame(overlay, scene.getRoot(), "hiding settings must keep the Catalog scene root stable");
        });

        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    Parent layer = Objects.requireNonNull(layerReference.get(), "settings layer");
                    M3BottomSheet sheet = Objects.requireNonNull(sheetReference.get(), "settings sheet");
                    M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "settings scrim");
                    Node previousFocus = Objects.requireNonNull(previousFocusReference.get(), "previous focus owner");
                    return layer.getParent() == null
                            && !sheet.isVisible()
                            && !scrim.isVisible()
                            && scene.getFocusOwner() == previousFocus;
                },
                STABLE_PULSE_COUNT,
                () -> {
                },
                () -> {
                    M3OverlayPane overlay = Objects.requireNonNull(overlayReference.get(), "overlay pane");
                    Parent layer = Objects.requireNonNull(layerReference.get(), "settings layer");
                    M3BottomSheet sheet = Objects.requireNonNull(sheetReference.get(), "settings sheet");
                    M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "settings scrim");
                    Node previousFocus = Objects.requireNonNull(previousFocusReference.get(), "previous focus owner");
                    assertSame(overlay, scene.getRoot(),
                            "settings exit must keep the Catalog scene root stable");
                    assertFalse(detachedBeforeBothExitsFinished.get(),
                            "settings layer must not detach before both exit transitions finish");
                    assertNull(layer.getParent(), "settings layer should detach after both exit transitions");
                    assertNull(layer.getScene());
                    assertSame(layer, sheet.getParent());
                    assertSame(layer, scrim.getParent());
                    assertFalse(sheet.isVisible());
                    assertFalse(sheet.isManaged());
                    assertFalse(scrim.isVisible());
                    assertFalse(scrim.isManaged());
                    assertNull(scene.lookup(".catalog-settings-sheet"));
                    assertNull(scene.lookup(".catalog-settings-scrim"));
                    assertSame(previousFocus, scene.getFocusOwner(),
                            "closing settings should restore the previous Catalog focus owner");
                    M3MotionSettings.setReducedMotionRequested(scene.getRoot(), true);
                }
        );
    }

    /// Returns whether one scene-graph node is the supplied node or one of its ancestors.
    ///
    /// @param ancestor the prospective ancestor
    /// @param node     the prospective descendant
    /// @return `true` when `node` belongs to the subtree rooted at `ancestor`
    private static boolean containsNode(Node ancestor, @Nullable Node node) {
        for (@Nullable Node current = node; current != null; current = current.getParent()) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
    }

    /// Verifies that Catalog content follows the logical start and end edges when orientation changes at runtime.
    ///
    /// @param scene the Catalog scene
    /// @param app   the running Catalog application
    private static void assertRightToLeftLayout(Scene scene, M3FXCatalogApp app) {
        FxTestUtils.runOnFxThread(() -> {
            Parent root = scene.getRoot();
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            app.navigateHome();
            layout(scene);

            TilePane grid = assertInstanceOf(
                    TilePane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-grid"), "component grid")
            );
            Node firstCell = grid.getChildren().get(0);
            Node secondCell = grid.getChildren().get(1);
            assertTrue(
                    firstCell.localToScene(firstCell.getBoundsInLocal()).getMinX()
                            > secondCell.localToScene(secondCell.getBoundsInLocal()).getMinX(),
                    "the first Catalog cell must occupy the visual start of an RTL row"
            );

            CatalogComponent chips = componentNamed(app.components(), "Chips");
            app.navigate(new CatalogRoute.Component(chips));
            layout(scene);
            Node firstCard = Objects.requireNonNull(scene.lookup(".catalog-example-card"), "example card");
            Node labels = Objects.requireNonNull(
                    firstCard.lookup(".catalog-example-card-labels"),
                    "example labels"
            );
            Node trailing = Objects.requireNonNull(
                    firstCard.lookup(".catalog-example-card-trailing"),
                    "example trailing action"
            );
            assertTrue(
                    trailing.localToScene(trailing.getBoundsInLocal()).getCenterX()
                            < labels.localToScene(labels.getBoundsInLocal()).getCenterX(),
                    "the trailing action must occupy the visual end of an RTL example card"
            );

            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            app.navigateHome();
            layout(scene);
            grid = assertInstanceOf(
                    TilePane.class,
                    Objects.requireNonNull(scene.lookup(".catalog-component-grid"), "component grid")
            );
            firstCell = grid.getChildren().get(0);
            secondCell = grid.getChildren().get(1);
            assertTrue(
                    firstCell.localToScene(firstCell.getBoundsInLocal()).getMinX()
                            < secondCell.localToScene(secondCell.getBoundsInLocal()).getMinX(),
                    "the first Catalog cell must return to the visual start after restoring LTR"
            );
        });
    }

    /// Verifies the controls and layout boundaries added to the expanded Catalog registry.
    ///
    /// @param scene the Catalog scene
    /// @param app   the running Catalog application
    private static void assertExpandedComponentCoverage(
            Scene scene,
            M3FXCatalogApp app
    ) throws InterruptedException {
        FxTestUtils.runOnFxThread(() -> {
            CatalogComponent dividers = componentNamed(app.components(), "Dividers");
            app.navigate(new CatalogRoute.Example(dividers, dividers.examples().get(0)));
            layout(scene);
            assertFalse(scene.getRoot().lookupAll(".m3-divider").isEmpty());
            assertTrue(scene.getRoot().lookupAll(".m3-divider").stream()
                    .allMatch(M3Divider.class::isInstance));

            CatalogComponent datePickers = componentNamed(app.components(), "Date pickers");
            app.navigate(new CatalogRoute.Example(
                    datePickers,
                    exampleNamed(datePickers, "Date range picker")
            ));
            layout(scene);
            M3DateRangePicker dateRangePicker = assertInstanceOf(
                    M3DateRangePicker.class,
                    Objects.requireNonNull(scene.lookup(".m3-date-range-picker"), "date range picker")
            );
            assertTrue(dateRangePicker.getWidth() <= 420.5);
            assertTrue(dateRangePicker.getHeight() > 0.0);

            CatalogComponent search = componentNamed(app.components(), "Search");
            app.navigate(new CatalogRoute.Example(
                    search,
                    exampleNamed(search, "Contained docked search")
            ));
            layout(scene);
            M3SearchView searchView = assertInstanceOf(
                    M3SearchView.class,
                    Objects.requireNonNull(scene.lookup(".m3-search-view"), "search view")
            );
            assertEquals(3, searchView.getResults().size());

            CatalogComponent sideSheets = componentNamed(app.components(), "Side sheets");
            app.navigate(new CatalogRoute.Example(
                    sideSheets,
                    exampleNamed(sideSheets, "Modal side sheet")
            ));
            layout(scene);
            Node preview = Objects.requireNonNull(
                    scene.lookup(".catalog-side-sheet-preview"),
                    "side-sheet preview"
            );
            M3SideSheet sideSheet = assertInstanceOf(
                    M3SideSheet.class,
                    Objects.requireNonNull(preview.lookup(".m3-side-sheet"), "side sheet")
            );
            M3Scrim scrim = assertInstanceOf(
                    M3Scrim.class,
                    Objects.requireNonNull(preview.lookup(".m3-scrim"), "side-sheet scrim")
            );
            assertTrue(sideSheet.isShown());
            assertTrue(scrim.isShown());
            assertTrue(sideSheet.localToScene(sideSheet.getBoundsInLocal()).getMaxX()
                    <= preview.localToScene(preview.getBoundsInLocal()).getMaxX() + 0.5);
            preview.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout(scene);
            assertEquals(
                    preview.localToScene(preview.getBoundsInLocal()).getMinX(),
                    sideSheet.localToScene(sideSheet.getBoundsInLocal()).getMinX(),
                    0.5
            );

            Parent avatarsPage = openFirstExample(scene, app, "Avatars");
            assertEquals(1, avatarsPage.lookupAll(".m3-avatar").size());
            assertTrue(avatarsPage.lookupAll(".m3-avatar").stream().allMatch(M3Avatar.class::isInstance));

            Parent adaptivePage = openFirstExample(scene, app, "Adaptive");
            M3AdaptiveScaffold adaptiveScaffold = assertInstanceOf(
                    M3AdaptiveScaffold.class,
                    Objects.requireNonNull(adaptivePage.lookup(".m3-adaptive-scaffold"), "adaptive scaffold")
            );
            assertEquals(0.36, adaptiveScaffold.getSplitPosition(), 0.0);

            Parent bannersPage = openFirstExample(scene, app, "Banners");
            assertInstanceOf(
                    M3Banner.class,
                    Objects.requireNonNull(bannersPage.lookup(".m3-banner"), "banner")
            );

            Parent colorPickersPage = openFirstExample(scene, app, "Color pickers");
            assertInstanceOf(
                    M3ColorPicker.class,
                    Objects.requireNonNull(colorPickersPage.lookup(".m3-color-picker"), "color picker")
            );

            Parent formsPage = openFirstExample(scene, app, "Forms");
            assertInstanceOf(
                    M3FormPane.class,
                    Objects.requireNonNull(formsPage.lookup(".m3-form-pane"), "form pane")
            );

            Parent iconsPage = openFirstExample(scene, app, "Icons");
            assertEquals(4, iconsPage.lookupAll(".m3-svg-icon").size());
            assertTrue(iconsPage.lookupAll(".m3-svg-icon").stream().allMatch(M3SVGIcon.class::isInstance));

            Parent scrimsPage = openFirstExample(scene, app, "Scrims");
            M3Scrim sampleScrim = assertInstanceOf(
                    M3Scrim.class,
                    Objects.requireNonNull(scrimsPage.lookup(".m3-scrim"), "scrim")
            );
            assertTrue(sampleScrim.isShown());

            Parent settingsPage = openFirstExample(scene, app, "Settings");
            assertInstanceOf(
                    M3SettingItem.class,
                    Objects.requireNonNull(settingsPage.lookup(".m3-action-setting-item"), "action setting")
            );
            Parent toggleSettingsPage = openExample(scene, app, "Settings", "Toggle settings");
            assertNotNull(toggleSettingsPage.lookup(".m3-switch-setting-item"));
            assertNotNull(toggleSettingsPage.lookup(".m3-checkbox-setting-item"));

            Parent surfacesPage = openFirstExample(scene, app, "Surfaces");
            assertEquals(1, surfacesPage.lookupAll(".m3-surface").size());
            assertTrue(surfacesPage.lookupAll(".m3-surface").stream().allMatch(M3Surface.class::isInstance));
        });
    }

    /// Opens the first example for a named component and returns its live-sample content.
    ///
    /// @param scene         the Catalog scene
    /// @param app           the running Catalog application
    /// @param componentName the component display name
    /// @return the live-sample content wrapper
    private static Parent openFirstExample(Scene scene, M3FXCatalogApp app, String componentName) {
        CatalogComponent component = componentNamed(app.components(), componentName);
        app.navigate(new CatalogRoute.Example(component, component.examples().get(0)));
        layout(scene);
        Parent page = assertInstanceOf(
                Parent.class,
                Objects.requireNonNull(scene.lookup(".catalog-example-page"), componentName + " example page")
        );
        return assertInstanceOf(
                Parent.class,
                Objects.requireNonNull(page.lookup(".catalog-sample-content"), componentName + " sample content")
        );
    }

    /// Opens a named example for a named component and returns its live-sample content.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
    /// @param componentName the component display name
    /// @param exampleName the example display name
    /// @return the live-sample content wrapper
    private static Parent openExample(
            Scene scene,
            M3FXCatalogApp app,
            String componentName,
            String exampleName
    ) {
        CatalogComponent component = componentNamed(app.components(), componentName);
        app.navigate(new CatalogRoute.Example(component, exampleNamed(component, exampleName)));
        layout(scene);
        Parent page = assertInstanceOf(
                Parent.class,
                Objects.requireNonNull(scene.lookup(".catalog-example-page"), componentName + " example page")
        );
        return assertInstanceOf(
                Parent.class,
                Objects.requireNonNull(page.lookup(".catalog-sample-content"), componentName + " sample content")
        );
    }

    /// Verifies that every example remains within the route viewport at the application's minimum window size.
    ///
    /// @param scene the Catalog scene
    /// @param stage the Catalog stage
    /// @param app   the running Catalog application
    private static void assertCompactExampleLayouts(
            Scene scene,
            Stage stage,
            M3FXCatalogApp app
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> scene.lookup(".catalog-route-host") != null
                        && scene.getWidth() <= 460.0
                        && scene.getHeight() <= 560.0,
                STABLE_PULSE_COUNT,
                () -> {
                    stage.setWidth(460.0);
                    stage.setHeight(560.0);
                },
                () -> {
                    app.navigateHome();
                    layout(scene);
                    Node leadingPane = Objects.requireNonNull(
                            scene.lookup(".m3-scaffold-leading-pane"),
                            "adaptive leading pane"
                    );
                    assertFalse(leadingPane.isVisible(), "the sidebar should start collapsed at compact width");
                    Node routeHost = Objects.requireNonNull(scene.lookup(".catalog-route-host"), "route host");
                    assertTrue(routeHost.getLayoutBounds().getWidth() >= scene.getWidth() - 1.0);
                    Parent homePage = assertInstanceOf(
                            Parent.class,
                            Objects.requireNonNull(scene.lookup(".catalog-home-page"), "compact Home page")
                    );
                    Node homeBrowser = Objects.requireNonNull(
                            homePage.lookup(".catalog-home-browser"),
                            "compact Home browser"
                    );
                    Bounds homeBounds = homePage.localToScene(homePage.getBoundsInLocal());
                    Bounds homeBrowserBounds = homeBrowser.localToScene(homeBrowser.getBoundsInLocal());
                    assertTrue(homeBrowserBounds.getMinX() >= homeBounds.getMinX() - 0.5);
                    assertTrue(homeBrowserBounds.getMaxX() <= homeBounds.getMaxX() + 0.5);

                    CatalogComponent buttons = componentNamed(app.components(), "Buttons");
                    app.navigate(new CatalogRoute.Component(buttons));
                    layout(scene);
                    Parent componentPage = assertInstanceOf(
                            Parent.class,
                            Objects.requireNonNull(scene.lookup(".catalog-component-page"), "compact component page")
                    );
                    Node reference = Objects.requireNonNull(
                            componentPage.lookup(".catalog-component-reference"),
                            "compact component reference"
                    );
                    Bounds componentBounds = componentPage.localToScene(componentPage.getBoundsInLocal());
                    Bounds referenceBounds = reference.localToScene(reference.getBoundsInLocal());
                    assertTrue(referenceBounds.getMinX() >= componentBounds.getMinX() - 0.5);
                    assertTrue(referenceBounds.getMaxX() <= componentBounds.getMaxX() + 0.5);
                    TilePane exampleGrid = assertInstanceOf(
                            TilePane.class,
                            Objects.requireNonNull(scene.lookup(".catalog-example-grid"), "compact example grid")
                    );
                    assertEquals(
                            1L,
                            exampleGrid.getChildren().stream()
                                    .filter(Node::isManaged)
                                    .map(Node::getLayoutX)
                                    .distinct()
                                    .count(),
                            "compact component pages should use one example column"
                    );
                    Node browser = Objects.requireNonNull(
                            componentPage.lookup(".catalog-example-browser"),
                            "compact example browser"
                    );
                    Bounds browserBounds = browser.localToScene(browser.getBoundsInLocal());
                    assertTrue(browserBounds.getMinX() >= componentBounds.getMinX() - 0.5);
                    assertTrue(browserBounds.getMaxX() <= componentBounds.getMaxX() + 0.5);

                    for (CatalogComponent component : app.components()) {
                        for (CatalogExample example : component.examples()) {
                            app.navigate(new CatalogRoute.Example(component, example));
                            layout(scene);

                            ScrollPane scrollPane = assertInstanceOf(
                                    ScrollPane.class,
                                    Objects.requireNonNull(
                                            scene.lookup(".catalog-example-scroll"),
                                            component.name() + " example scroll pane"
                                    )
                            );
                            Parent page = assertInstanceOf(
                                    Parent.class,
                                    Objects.requireNonNull(
                                            scene.lookup(".catalog-example-page"),
                                            component.name() + " example page"
                                    )
                            );
                            String description = component.name() + " / " + example.name();
                            Parent sampleContent = assertInstanceOf(
                                    Parent.class,
                                    Objects.requireNonNull(
                                            page.lookup(".catalog-sample-content"),
                                            description + " sample content"
                                    )
                            );
                            Node sample = sampleContent.getChildrenUnmodifiable().get(0);
                            Bounds pageBounds = page.localToScene(page.getBoundsInLocal());
                            Bounds sampleBounds = sample.localToScene(sample.getBoundsInLocal());

                            assertEquals(
                                    scrollPane.getViewportBounds().getWidth(),
                                    page.getLayoutBounds().getWidth(),
                                    0.5,
                                    description + " page must fit the compact viewport"
                            );
                            assertTrue(
                                    sampleBounds.getMinX() >= pageBounds.getMinX() - 0.5,
                                    description + " escapes the compact route at the leading edge"
                            );
                            assertTrue(
                                    sampleBounds.getMaxX() <= pageBounds.getMaxX() + 0.5,
                                    description + " escapes the compact route at the trailing edge"
                            );
                            assertTrue(
                                    sampleBounds.getMinY() >= pageBounds.getMinY() - 0.5,
                                    description + " escapes the compact route above its scrollable page"
                            );
                            assertTrue(
                                    sampleBounds.getMaxY() <= pageBounds.getMaxY() + 0.5,
                                    description + " escapes the compact route below its scrollable page"
                            );
                        }
                    }
                }
        );
    }

    /// Verifies modal drawer presentation, persistent selection, and dismissal after choosing a destination.
    ///
    /// @param scene the compact Catalog scene
    /// @param app the running Catalog application
    private static void assertCompactSidebarNavigation(
            Scene scene,
            M3FXCatalogApp app
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    layout(scene);
                    @Nullable Node drawerNode = scene.lookup(".catalog-sidebar-drawer");
                    @Nullable Node scrim = scene.lookup(".catalog-sidebar-scrim");
                    @Nullable Node visibilityNode = scene.lookup(".catalog-sidebar-visibility");
                    if (!(drawerNode instanceof M3NavigationDrawer drawer)
                            || !(visibilityNode instanceof M3AnimatedVisibility visibility)
                            || scrim == null
                            || !scrim.isVisible()) {
                        return false;
                    }
                    return drawer.getVariant() == M3NavigationDrawerVariant.MODAL
                            && Math.abs(drawer.getWidth() - 360.0) <= 0.5
                            && visibility.getState() == M3VisibilityState.VISIBLE;
                },
                STABLE_PULSE_COUNT,
                () -> {
                    CatalogComponent buttons = componentNamed(app.components(), "Buttons");
                    app.navigate(new CatalogRoute.Component(buttons));
                    layout(scene);
                    M3MotionSettings.setReducedMotionRequested(scene.getRoot(), false);
                    M3IconButton browseButton = assertInstanceOf(
                            M3IconButton.class,
                            Objects.requireNonNull(
                                    scene.lookup(".catalog-sidebar-action"),
                                    "compact sidebar action"
                            )
                    );
                    browseButton.fire();
                    M3AnimatedVisibility visibility = assertInstanceOf(
                            M3AnimatedVisibility.class,
                            Objects.requireNonNull(
                                    scene.lookup(".catalog-sidebar-visibility"),
                                    "sidebar visibility transition"
                            )
                    );
                    assertEquals(M3VisibilityState.ENTERING, visibility.getState());
                    assertTrue(visibility.isTransitioning(), "modal drawer entry should animate");
                },
                () -> {
                    CatalogSidebar sidebar = visibleSidebar(scene);
                    M3AnimatedVisibility visibility = assertInstanceOf(
                            M3AnimatedVisibility.class,
                            Objects.requireNonNull(
                                    scene.lookup(".catalog-sidebar-visibility"),
                                    "sidebar visibility transition"
                            )
                    );
                    M3ListItem selectedComponent = Objects.requireNonNull(
                            sidebar.drawer().getSelectedItem(),
                            "selected sidebar component"
                    );
                    assertEquals("Buttons", selectedComponent.getHeadlineText());
                    ScrollPane viewport = drawerViewport(sidebar);
                    assertTrue(
                            viewport.localToScene(viewport.getBoundsInLocal()).intersects(
                                    selectedComponent.localToScene(selectedComponent.getBoundsInLocal())
                            ),
                            () -> "the modal drawer should reveal its selected destination when opened: vvalue="
                                    + viewport.getVvalue()
                                    + ", viewport="
                                    + viewport.localToScene(viewport.getBoundsInLocal())
                                    + ", selected="
                                    + selectedComponent.localToScene(selectedComponent.getBoundsInLocal())
                    );
                    M3ListItem cardsItem = listItemNamed(
                            sidebar,
                            ".catalog-sidebar-component",
                            "Cards"
                    );
                    cardsItem.fire();
                    layout(scene);
                    CatalogRoute.Component cardsRoute = assertInstanceOf(
                            CatalogRoute.Component.class,
                            app.currentRoute()
                    );
                    assertEquals("Cards", cardsRoute.component().name());
                    assertEquals(M3VisibilityState.EXITING, visibility.getState());
                    assertTrue(visibility.isTransitioning(), "modal drawer exit should animate");
                    M3MotionSettings.setReducedMotionRequested(scene.getRoot(), true);
                    layout(scene);
                    assertNull(scene.lookup(".catalog-sidebar-drawer"));
                    assertNull(scene.lookup(".catalog-sidebar-scrim"));
                }
        );
    }

    /// Returns the visible persistent Catalog sidebar.
    ///
    /// @param scene the Catalog scene
    /// @return the attached Catalog sidebar
    private static CatalogSidebar visibleSidebar(Scene scene) {
        return assertInstanceOf(
                CatalogSidebar.class,
                Objects.requireNonNull(scene.lookup(".catalog-sidebar"), "Catalog sidebar")
        );
    }

    /// Returns the native drawer viewport after applying CSS.
    ///
    /// @param sidebar the attached Catalog sidebar
    /// @return the drawer scroll viewport
    private static ScrollPane drawerViewport(CatalogSidebar sidebar) {
        sidebar.applyCss();
        sidebar.layout();
        return assertInstanceOf(
                ScrollPane.class,
                Objects.requireNonNull(
                        sidebar.lookup(".m3-navigation-drawer-viewport"),
                        "navigation drawer viewport"
                )
        );
    }

    /// Returns an attached list item with a specific headline.
    ///
    /// @param sidebar the attached Catalog sidebar
    /// @param selector the item style selector
    /// @param headline the required headline
    /// @return the matching list item
    private static M3ListItem listItemNamed(CatalogSidebar sidebar, String selector, String headline) {
        return sidebar.lookupAll(selector).stream()
                .filter(M3ListItem.class::isInstance)
                .map(M3ListItem.class::cast)
                .filter(item -> item.getHeadlineText().equals(headline))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing sidebar item: " + headline));
    }

    /// Counts visible and managed nodes matching a CSS selector below a root.
    ///
    /// @param root the lookup root
    /// @param selector the CSS selector
    /// @return the number of nodes currently participating in layout
    private static long visibleNodeCount(Parent root, String selector) {
        return root.lookupAll(selector).stream()
                .filter(Node::isVisible)
                .filter(Node::isManaged)
                .count();
    }

    /// Creates and lays out every registered example in the running application.
    ///
    /// @param scene the Catalog scene
    /// @param app   the running Catalog application
    private static void assertEveryExampleRenders(Scene scene, M3FXCatalogApp app) throws InterruptedException {
        FxTestUtils.runOnFxThread(() -> {
            for (CatalogComponent component : app.components()) {
                for (CatalogExample example : component.examples()) {
                    app.navigate(new CatalogRoute.Example(component, example));
                    layout(scene);
                    Node page = Objects.requireNonNull(scene.lookup(".catalog-example-page"), component.name());
                    Parent sampleContent = assertInstanceOf(
                            Parent.class,
                            Objects.requireNonNull(
                                    page.lookup(".catalog-sample-content"),
                                    component.name() + " sample content"
                            )
                    );
                    assertEquals(1, sampleContent.getChildrenUnmodifiable().size());
                }
            }
        });
    }

    /// Verifies that adaptive cells distribute the usable row width instead of remaining fixed at their minimum.
    ///
    /// @param scene the Catalog scene
    private static void assertGridUsesAvailableWidth(Scene scene) {
        layout(scene);
        TilePane grid = (TilePane) Objects.requireNonNull(scene.lookup(".catalog-component-grid"), "component grid");
        double availableWidth = grid.getWidth() - grid.getInsets().getLeft() - grid.getInsets().getRight();
        int columns = Math.max(1, (int) Math.floor(availableWidth / 180.0));
        Node lastCell = grid.getChildren().get(columns - 1);
        double lastCellEdge = lastCell.getLayoutX() + lastCell.getLayoutBounds().getWidth();
        String diagnostics = "gridWidth=" + grid.getWidth()
                + ", insets=" + grid.getInsets()
                + ", prefTileWidth=" + grid.getPrefTileWidth()
                + ", cells=" + grid.getChildren().stream()
                .limit(columns + 1L)
                .map(child -> "[x=" + child.getLayoutX()
                        + ", y=" + child.getLayoutY()
                        + ", w=" + child.getLayoutBounds().getWidth() + "]")
                .toList();
        double contentRight = grid.getWidth() - grid.getInsets().getRight();
        double trailingSpace = contentRight - lastCellEdge;
        double outputScale = Objects.requireNonNull(scene.getWindow(), "window").getOutputScaleX();
        assertTrue(
                trailingSpace >= -0.5 && trailingSpace <= columns / outputScale + 0.5,
                diagnostics + ", trailingSpace=" + trailingSpace
        );
    }

    /// Counts distinct tile x-coordinates in the active home grid.
    ///
    /// @param scene the Catalog scene
    /// @return the number of visible grid columns
    private static int columnCount(Scene scene) {
        layout(scene);
        TilePane grid = (TilePane) Objects.requireNonNull(scene.lookup(".catalog-component-grid"), "component grid");
        double firstRowY = grid.getChildren().get(0).getLayoutY();
        int columns = 0;
        for (Node child : grid.getChildren()) {
            if (Math.abs(child.getLayoutY() - firstRowY) > 0.5) {
                break;
            }
            columns++;
        }
        return columns;
    }

    /// Finds a registered component by its display name.
    ///
    /// @param components the Catalog registry
    /// @param name       the component display name
    /// @return the matching component
    private static CatalogComponent componentNamed(List<CatalogComponent> components, String name) {
        return components.stream()
                .filter(component -> component.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing Catalog component: " + name));
    }

    /// Finds a registered example by its display name.
    ///
    /// @param component the owning component
    /// @param name the example display name
    /// @return the matching example
    private static CatalogExample exampleNamed(CatalogComponent component, String name) {
        return component.examples().stream()
                .filter(example -> example.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "missing Catalog example: " + component.name() + " / " + name
                ));
    }

    /// Resizes the decorated stage so its scene reaches a requested logical width.
    ///
    /// @param stage the showing Catalog stage
    /// @param scene the stage scene
    /// @param width the requested scene width in logical pixels
    private static void resizeSceneToWidth(Stage stage, Scene scene, double width) {
        stage.setWidth(stage.getWidth() + width - scene.getWidth());
    }

    /// Returns the horizontal center of a node's rendered bounds in scene coordinates.
    ///
    /// @param node the attached node
    /// @return the scene-space center x-coordinate
    private static double sceneCenterX(Node node) {
        return node.localToScene(node.getBoundsInLocal()).getCenterX();
    }

    /// Completes CSS and layout before querying scene-graph geometry.
    ///
    /// @param scene the Catalog scene
    private static void layout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }
}
