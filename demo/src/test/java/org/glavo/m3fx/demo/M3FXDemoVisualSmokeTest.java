// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3ListViewCell;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3PickerField;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3SnackbarHost;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3SubMenuItem;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/// Visual smoke tests for the demo application's real JavaFX window hierarchy.
@NotNullByDefault
final class M3FXDemoVisualSmokeTest {
    /// Representative pages rendered under the dark expressive theme combination.
    private static final @Unmodifiable List<String> DARK_EXPRESSIVE_VISUAL_PAGES = List.of(
            "App Bars",
            "Buttons",
            "Button Groups",
            "Text Fields",
            "Date Pickers",
            "Loading Indicator",
            "Progress",
            "Menus",
            "Navigation Drawer",
            "Toolbars"
    );

    /// Demo pages that are sensitive to right-to-left mirroring in real window layouts.
    private static final @Unmodifiable List<String> RTL_VISUAL_PAGES = List.of(
            "App Bars",
            "Button Groups",
            "Icon Buttons",
            "Text Fields",
            "Date Pickers",
            "Menus",
            "Navigation Drawer",
            "Toolbars"
    );

    /// Material documentation URLs expected for demo pages that map to official Material pages.
    private static final @Unmodifiable Map<String, String> EXPECTED_MATERIAL_URLS = Map.ofEntries(
            Map.entry("Components Overview", DemoMaterialDocs.COMPONENTS),
            Map.entry("App Bars", DemoMaterialDocs.APP_BARS),
            Map.entry("Badges", DemoMaterialDocs.BADGES),
            Map.entry("Button Groups", DemoMaterialDocs.BUTTON_GROUPS),
            Map.entry("Buttons", DemoMaterialDocs.BUTTONS),
            Map.entry("Extended FABs", DemoMaterialDocs.EXTENDED_FAB),
            Map.entry("FAB Menu", DemoMaterialDocs.FAB_MENU),
            Map.entry("Floating Action Buttons", DemoMaterialDocs.FLOATING_ACTION_BUTTON),
            Map.entry("Icon Buttons", DemoMaterialDocs.ICON_BUTTONS),
            Map.entry("Segmented Buttons", DemoMaterialDocs.SEGMENTED_BUTTONS),
            Map.entry("Split Buttons", DemoMaterialDocs.SPLIT_BUTTON),
            Map.entry("Cards", DemoMaterialDocs.CARDS),
            Map.entry("Carousel", DemoMaterialDocs.CAROUSEL),
            Map.entry("Checkboxes", DemoMaterialDocs.CHECKBOX),
            Map.entry("Chips", DemoMaterialDocs.CHIPS),
            Map.entry("Date Pickers", DemoMaterialDocs.DATE_PICKERS),
            Map.entry("Time Pickers", DemoMaterialDocs.TIME_PICKERS),
            Map.entry("Dialogs", DemoMaterialDocs.DIALOGS),
            Map.entry("Dividers", DemoMaterialDocs.DIVIDER),
            Map.entry("Lists", DemoMaterialDocs.LISTS),
            Map.entry("Loading Indicator", DemoMaterialDocs.LOADING_INDICATOR),
            Map.entry("Progress", DemoMaterialDocs.PROGRESS_INDICATORS),
            Map.entry("Menus", DemoMaterialDocs.MENUS),
            Map.entry("Navigation", DemoMaterialDocs.NAVIGATION_BAR),
            Map.entry("Navigation Drawer", DemoMaterialDocs.NAVIGATION_DRAWER),
            Map.entry("Navigation Rail", DemoMaterialDocs.NAVIGATION_RAIL),
            Map.entry("Radio Buttons", DemoMaterialDocs.RADIO_BUTTON),
            Map.entry("Search", DemoMaterialDocs.SEARCH),
            Map.entry("Bottom Sheets", DemoMaterialDocs.BOTTOM_SHEETS),
            Map.entry("Side Sheets", DemoMaterialDocs.SIDE_SHEETS),
            Map.entry("Sliders", DemoMaterialDocs.SLIDERS),
            Map.entry("Snackbars", DemoMaterialDocs.SNACKBAR),
            Map.entry("Switches", DemoMaterialDocs.SWITCH),
            Map.entry("Tabs", DemoMaterialDocs.TABS),
            Map.entry("Text Fields", DemoMaterialDocs.TEXT_FIELDS),
            Map.entry("Toolbars", DemoMaterialDocs.TOOLBARS),
            Map.entry("Tooltips", DemoMaterialDocs.TOOLTIPS),
            Map.entry("Typography", DemoMaterialDocs.TYPOGRAPHY),
            Map.entry("Icons", DemoMaterialDocs.ICONS)
    );

    /// Fixed-target controls whose visible glyph content should stay centered.
    private static final @Unmodifiable Set<String> CENTERED_TARGET_STYLE_CLASSES = Set.of(
            M3DatePicker.DAY_CELL_STYLE_CLASS,
            M3FloatingActionButton.STYLE_CLASS,
            M3IconButton.STYLE_CLASS,
            M3IconToggleButton.STYLE_CLASS,
            M3SegmentedButton.STYLE_CLASS
    );

    /// Demo pages that should use SVG icons instead of text-placeholder icon controls in interactive slots.
    private static final @Unmodifiable Map<String, Integer> DEMO_VECTOR_ICON_PAGE_MINIMUMS = Map.ofEntries(
            Map.entry("App Bars", 18),
            Map.entry("Avatars", 1),
            Map.entry("Banners", 2),
            Map.entry("Bottom Sheets", 3),
            Map.entry("Chips", 1),
            Map.entry("FAB Menu", 6),
            Map.entry("Floating Action Buttons", 3),
            Map.entry("Icon Buttons", 12),
            Map.entry("Icons", 12),
            Map.entry("Lists", 8),
            Map.entry("Menus", 10),
            Map.entry("Navigation", 7),
            Map.entry("Navigation Drawer", 7),
            Map.entry("Navigation Rail", 7),
            Map.entry("Search", 5),
            Map.entry("Side Sheets", 3),
            Map.entry("Text Fields", 5),
            Map.entry("Toolbars", 9),
            Map.entry("Tooltips", 1)
    );

    /// The edge tolerance used when comparing text bounds against scene and viewport bounds.
    private static final double TEXT_EDGE_TOLERANCE = 1.0;

    /// The edge tolerance used when comparing visible control bounds against scene and viewport bounds.
    private static final double CONTROL_EDGE_TOLERANCE = 2.0;

    /// The minimum width expected from app bar previews in desktop demo captures.
    private static final double APP_BAR_PREVIEW_MIN_WIDTH = 680.0;

    /// The tolerance used when checking app bar slot placement against component tokens.
    private static final double APP_BAR_SLOT_TOLERANCE = 4.0;

    /// The Material top app bar navigation and action slot size.
    private static final double APP_BAR_ACTION_SLOT_SIZE = 48.0;

    /// The baseline Material icon button container size used by demo app bar actions.
    private static final double APP_BAR_ICON_BUTTON_SIZE = 40.0;

    /// The Material icon viewport size used by demo SVG icon slots.
    private static final double DEMO_ICON_VIEWPORT_SIZE = 24.0;

    /// The tolerance used when checking icon centering inside fixed action targets.
    private static final double DEMO_ICON_CENTER_TOLERANCE = 2.0;

    /// The minimum safe vertical room for single-line input text inside its editable area.
    private static final double INPUT_TEXT_MINIMUM_VERTICAL_ROOM = 4.0;

    /// The lowest acceptable vertical center ratio for single-line input text.
    private static final double INPUT_TEXT_MINIMUM_CENTER_RATIO = 0.33;

    /// The highest acceptable vertical center ratio for single-line input text.
    private static final double INPUT_TEXT_MAXIMUM_CENTER_RATIO = 0.70;

    /// The hover pseudo-class used when rendering synthetic interaction snapshots.
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("hover");

    /// The style class used by animated Material ripple nodes.
    private static final String RIPPLE_STYLE_CLASS = "m3-ripple";

    /// The JavaFX pulse count used after switching ordinary demo pages before layout-sensitive assertions.
    private static final int PAGE_LAYOUT_PULSES = 2;

    /// The JavaFX pulse count used after switching theme-sensitive pages before layout-sensitive assertions.
    private static final int THEME_LAYOUT_PULSES = 3;

    /// The JavaFX pulse count required after a final visual state first becomes true.
    private static final int SETTLED_STATE_PULSES = 2;

    /// The long linear motion spec duration used to make animation frames visually observable in tests.
    private static final Duration OBSERVABLE_MOTION_DURATION = Duration.millis(600.0);

    /// Starts the JavaFX toolkit before creating the demo stage.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        DemoFxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that demo pages expose stable Material documentation links in their page header.
    @Test
    void demoPagesExposeMaterialDocumentationLinks() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThread(() -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                assertEquals(EXPECTED_MATERIAL_URLS, app.demoPageMaterialUrlsForTesting());

                app.showPageForTesting("Buttons");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Node docsLink = Objects.requireNonNull(scene.lookup(".demo-page-doc-link"), "docsLink");
                M3Button docsButton = assertInstanceOf(M3Button.class, docsLink);
                assertEquals("Material docs", docsButton.getText());

                app.showPageForTesting("Forms");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                assertNull(scene.lookup(".demo-page-doc-link"));
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that app bar demo pages render full-width app bar components without placeholder icons.
    @Test
    void appBarDemoPagesUseRealFullWidthPreviewBars() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThread(() -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                app.showPageForTesting("App Bars");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                assertDemoVectorIcons(scene.getRoot(), "App Bars", 18);
                List<M3TopAppBar> topAppBars = visibleNodesOfType(scene.getRoot(), M3TopAppBar.class);
                assertEquals(6, topAppBars.size(), "top app bar demo count");
                assertEquals(M3TopAppBarVariant.SMALL, topAppBars.get(0).getVariant());
                assertEquals(M3TopAppBarVariant.CENTER_ALIGNED, topAppBars.get(1).getVariant());
                assertEquals(M3TopAppBarVariant.MEDIUM, topAppBars.get(2).getVariant());
                assertEquals(M3TopAppBarVariant.LARGE, topAppBars.get(3).getVariant());
                assertFalse(topAppBars.get(0).isScrolledUnder(), "first top app bar should show the default state");
                assertFalse(topAppBars.get(1).isScrolledUnder(), "center-aligned top app bar should show the default state");
                assertFalse(topAppBars.get(2).isScrolledUnder(), "medium top app bar should show the default state");
                assertFalse(topAppBars.get(3).isScrolledUnder(), "large top app bar should show the default state");
                assertTrue(topAppBars.get(4).isScrolledUnder(), "fifth top app bar should show the scrolled-under state");
                assertTrue(topAppBars.get(5).isScrolledUnder(), "sixth top app bar should show the scrolled-under state");
                for (int index = 0; index < topAppBars.size(); index++) {
                    M3TopAppBar appBar = topAppBars.get(index);
                    assertAppBarFitsPreview(appBar, "top app bar");
                    assertTopAppBarPreviewBalance(appBar);
                    assertAppBarPreviewIsNotInsideGenericFlow(appBar);
                    assertTopAppBarSlotGeometry(appBar);
                    if (index == 1) {
                        assertAppBarUsesVectorIconButtons(appBar, "top app bar", "back", "add", "more");
                    } else {
                        assertAppBarUsesVectorIconButtons(appBar, "top app bar", "menu", "search", "more");
                    }
                }
                List<M3BottomAppBar> appBarsPageBottomAppBars =
                        visibleNodesOfType(scene.getRoot(), M3BottomAppBar.class);
                assertEquals(0, appBarsPageBottomAppBars.size(),
                        "App Bars page should not duplicate bottom app bars that belong to Toolbars");
                WritableImage appBarsImage = snapshot(scene);
                writeVisualSnapshot(appBarsImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "app-bars-top-variants.png"
                ));
                assertSnapshotHasVisibleContent(appBarsImage, "App Bars");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(scene.getRoot(), "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertTrue(topAppBars.stream()
                                .filter(M3TopAppBar::isScrolledUnder)
                                .anyMatch(appBar -> !isOutsideSceneViewport(
                                        appBar,
                                        appBar.localToScene(appBar.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "App Bars page should render the scrolled-under examples in a captured viewport");
                WritableImage appBarsScrolledImage = snapshot(scene);
                writeVisualSnapshot(appBarsScrolledImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "app-bars-scrolled-under.png"
                ));
                assertSnapshotHasVisibleContent(appBarsScrolledImage, "App Bars scrolled-under");

                app.showPageForTesting("Toolbars");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                List<M3BottomAppBar> bottomAppBars = visibleNodesOfType(scene.getRoot(), M3BottomAppBar.class);
                assertEquals(3, bottomAppBars.size(), "bottom app bar demo count");
                for (M3BottomAppBar appBar : bottomAppBars) {
                    assertAppBarFitsPreview(appBar, "bottom app bar");
                    assertBottomAppBarPreviewBalance(appBar);
                    assertBottomAppBarSlotGeometry(appBar);
                    assertAppBarUsesVectorIconButtons(appBar, "bottom app bar", "search", "favorite");
                    assertEquals(1, visibleNodesOfType(appBar, M3FloatingActionButton.class).size(),
                            "bottom app bar should show a floating action button");
                }
                WritableImage toolbarsImage = snapshot(scene);
                writeVisualSnapshot(toolbarsImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "app-bars-bottom-toolbar.png"
                ));
                assertSnapshotHasVisibleContent(toolbarsImage, "Toolbars");
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Lists demo renders the data-driven list through reusable virtualized rows.
    @Test
    void listPageUsesVirtualizedReusableRowsInDemo() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Lists");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                assertCurrentPageTitle(scene, "Lists");

                @SuppressWarnings("rawtypes")
                List<M3ListView> listViews = visibleNodesOfType(scene.getRoot(), M3ListView.class);
                assertEquals(1, listViews.size(), "Lists page should show one virtualized list view");
                @SuppressWarnings("rawtypes")
                M3ListView listView = listViews.get(0);
                assertEquals(240, listView.getItems().size(), "demo list should contain enough rows for virtualization");
                assertEquals(72.0, listView.getFixedCellSize(), 0.01, "demo list should use two-line row height");
                assertEquals(2, listView.getSelectedIndex(), "demo list should show a non-first selected row");
                assertInstanceOf(VirtualFlow.class, listView.lookup(".m3-list-view-flow"));

                List<M3ListViewCell> visibleCells = visibleNodesOfType(listView, M3ListViewCell.class);
                int maxExpectedVisibleCells =
                        (int) Math.ceil(listView.getPrefHeight() / listView.getFixedCellSize()) + 8;
                assertTrue(!visibleCells.isEmpty(), "virtualized list should attach visible cells");
                assertTrue(visibleCells.size() <= maxExpectedVisibleCells,
                        () -> "virtualized list attached too many cells: " + visibleCells.size());
                assertTrue(visibleCells.size() < listView.getItems().size(),
                        () -> "virtualized list should not materialize every row: " + visibleCells.size());

                listView.setAnimatedScroll(false);
                listView.scrollTo(180);
                scene.getRoot().applyCss();
                scene.getRoot().layout();

                List<M3ListItem> visibleRows = visibleNodesOfType(listView, M3ListItem.class);
                assertTrue(visibleRows.stream().anyMatch(item -> "Virtualized row 181".equals(item.getHeadlineText())),
                        () -> "scrolling to a far row did not render the expected reused cell: " + visibleRows);
                assertTrue(visibleRows.stream().noneMatch(item -> "Virtualized row 1".equals(item.getHeadlineText())),
                        "far-row scroll should replace the initially visible first row");

                WritableImage image = snapshotNode(listView);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "lists-virtualized-view.png"
                ));
                assertSnapshotHasVisibleContent(image, "Lists virtualized view");
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Search demo renders active, inactive, and keyboard-reachable result states.
    @Test
    void searchPageRendersActiveInactiveAndKeyboardReachableResults() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Search");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                assertCurrentPageTitle(scene, "Search");

                List<M3SearchView> searchViews = visibleNodesOfType(scene.getRoot(), M3SearchView.class);
                assertEquals(2, searchViews.size(), "Search page should show active and inactive search views");
                M3SearchView activeView = Objects.requireNonNull(
                        searchViews.stream().filter(M3SearchView::isActive).findFirst().orElse(null),
                        "active search view"
                );
                M3SearchView inactiveView = Objects.requireNonNull(
                        searchViews.stream().filter(view -> !view.isActive()).findFirst().orElse(null),
                        "inactive search view"
                );

                List<M3SearchBar> searchBars = visibleNodesOfType(scene.getRoot(), M3SearchBar.class);
                assertEquals(4, searchBars.size(),
                        "Search page should expose two standalone bars and two embedded search bars");
                searchBars.forEach(M3FXDemoVisualSmokeTest::assertSearchBarVisualGeometry);

                assertSearchViewResultsVisible(activeView);
                assertSearchViewResultsHidden(inactiveView);
                assertFalse(visibleNodesOfType(scene.getRoot(), Text.class).stream()
                                .anyMatch(text -> "Hidden result".equals(text.getText())),
                        "inactive search view should not render hidden result text");

                M3ListItem firstResult = assertInstanceOf(M3ListItem.class, activeView.getResults().get(0));
                activeView.getEditor().requestFocus();
                activeView.getEditor().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                scene.getRoot().applyCss();
                scene.getRoot().layout();

                @Nullable Node focusOwner = scene.getFocusOwner();
                assertNotNull(focusOwner, "search result keyboard navigation should produce a focus owner");
                assertTrue(isNodeOrDescendant(firstResult, focusOwner),
                        () -> "DOWN from the search editor should focus the first result: focusOwner=" + focusOwner);

                WritableImage image = snapshotNode(activeView);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "search-active-view.png"
                ));
                assertSnapshotHasVisibleContent(image, "Search active view");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Cards demo renders variant, media, action, and disabled states.
    @Test
    void cardsPageRendersVariantsMediaActionsAndStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Cards");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Cards");

                List<M3Card> cards = visibleNodesOfType(root, M3Card.class);
                assertEquals(6, cards.size(), "Cards page should render compact, media, and state card examples");
                assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.FILLED).count());
                assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.OUTLINED).count());
                assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.ELEVATED).count());
                assertTrue(cards.stream().allMatch(card -> card.getOnAction() != null),
                        "demo cards should exercise actionable surface semantics");
                assertEquals(1, cards.stream().filter(Node::isDisabled).count(),
                        "Cards page should include one disabled card state");

                assertEquals(3, visibleNodesWithStyle(root, "demo-card-media").size(),
                        "Cards page should render three media cards");
                assertEquals(3, visibleNodesWithStyle(root, "demo-card-actions").size(),
                        "Cards page should render action rows for the media cards");
                long cardActionButtons = visibleNodesOfType(root, M3Button.class).stream()
                        .filter(button -> nearestAncestorOfType(button, M3Card.class) != null)
                        .count();
                assertEquals(6, cardActionButtons, "media cards should expose two actions each");

                for (M3Card card : cards) {
                    assertCardDemoGeometry(card);
                    assertNotNull(firstVisibleStyledDescendant(card, "demo-card-title"),
                            "each demo card should render a title");
                    assertNotNull(firstVisibleStyledDescendant(card, "demo-card-body"),
                            "each demo card should render supporting text");
                }

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "cards-rich-states.png"
                ));
                assertSnapshotHasVisibleContent(image, "Cards rich states");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(root, "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertTrue(cards.stream()
                                .filter(card -> firstVisibleStyledDescendant(card, "demo-card-actions") != null)
                                .anyMatch(card -> !isOutsideSceneViewport(
                                        card,
                                        card.localToScene(card.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Cards page should render media card actions in a captured viewport");
                assertTrue(cards.stream()
                                .filter(Node::isDisabled)
                                .anyMatch(card -> !isOutsideSceneViewport(
                                        card,
                                        card.localToScene(card.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Cards page should render the disabled card state in a captured viewport");

                WritableImage mediaImage = snapshot(scene);
                writeVisualSnapshot(mediaImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "cards-media-actions.png"
                ));
                assertSnapshotHasVisibleContent(mediaImage, "Cards media and actions");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Dialogs demo renders launchers, inline panes, actions, and scrollable content.
    @Test
    void dialogsPageRendersInlinePanesActionsAndScrollableContent() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Dialogs");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Dialogs");
                assertVisibleText(root, "Open basic", "Dialogs");
                assertVisibleText(root, "Open settings", "Dialogs");
                assertVisibleText(root, "Open destructive", "Dialogs");
                assertVisibleText(root, "Cancel", "Dialogs");
                assertVisibleText(root, "OK", "Dialogs");
                assertVisibleText(root, "Apply", "Dialogs");

                List<M3DialogPane> panes = visibleNodesOfType(root, M3DialogPane.class);
                assertEquals(3, panes.size(), "Dialogs page should render basic, form, and scrollable panes");
                assertEquals(1, visibleNodesOfType(root, M3TextInputLayout.class).stream()
                                .filter(layout -> nearestAncestorOfType(layout, M3DialogPane.class) != null)
                                .count(),
                        "Dialogs page should include one form-like text input layout");
                assertEquals(1, visibleNodesOfType(root, M3Switch.class).stream()
                                .filter(toggle -> nearestAncestorOfType(toggle, M3DialogPane.class) != null)
                                .count(),
                        "Dialogs page should include one settings switch");
                assertEquals(1, visibleNodesOfType(root, M3CheckBox.class).stream()
                                .filter(checkbox -> nearestAncestorOfType(checkbox, M3DialogPane.class) != null)
                                .count(),
                        "Dialogs page should include one settings checkbox");
                assertEquals(1, visibleNodesOfType(root, ScrollPane.class).stream()
                                .filter(scrollPane -> scrollPane.getStyleClass().contains("demo-dialog-scroll-pane"))
                                .count(),
                        "Dialogs page should include one scrollable dialog body");

                for (M3DialogPane pane : panes) {
                    assertDialogPaneDemoGeometry(scene, pane);
                }

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "dialogs-inline-panes.png"
                ));
                assertSnapshotHasVisibleContent(image, "Dialogs inline panes");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(root, "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertVisibleText(root, "Close", "Dialogs");
                assertTrue(panes.stream()
                                .filter(pane -> pane.getContent() instanceof ScrollPane)
                                .anyMatch(pane -> !isOutsideSceneViewport(
                                        pane,
                                        pane.localToScene(pane.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Dialogs page should render the scrollable dialog pane in a captured viewport");

                WritableImage scrollableImage = snapshot(scene);
                writeVisualSnapshot(scrollableImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "dialogs-scrollable-content.png"
                ));
                assertSnapshotHasVisibleContent(scrollableImage, "Dialogs scrollable content");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Banners demo renders icons, actions, passive, narrow, and RTL states safely.
    @Test
    void bannersPageRendersIconsActionsPassiveNarrowAndRtlStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Banners");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Banners");
                assertVisibleText(root, "Learn", "Banners");
                assertVisibleText(root, "Dismiss", "Banners");
                assertVisibleText(root, "Details", "Banners");
                assertVisibleText(root, "Secondary", "Banners");

                List<M3Banner> banners = visibleNodesOfType(root, M3Banner.class);
                assertEquals(6, banners.size(), "Banners page should render six banner states");
                assertEquals(4, visibleNodesWithStyle(root, M3Banner.ICON_STYLE_CLASS).size(),
                        "Banners page should render four visible icon slots");
                long actionButtonCount = visibleNodesOfType(root, M3Button.class).stream()
                        .filter(button -> nearestAncestorOfType(button, M3Banner.class) != null)
                        .count();
                assertEquals(8, actionButtonCount, "Banners page should render all action buttons");
                assertTrue(banners.stream().anyMatch(banner -> banner.getActions().isEmpty()),
                        "Banners page should include a passive banner");
                assertTrue(banners.stream().anyMatch(banner -> banner.getPrefWidth() <= 420.0),
                        "Banners page should include a narrow wrapping banner");
                assertTrue(banners.stream().anyMatch(banner ->
                                banner.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT),
                        "Banners page should include a right-to-left banner");

                for (M3Banner banner : banners) {
                    assertBannerDemoGeometry(banner);
                }

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "banners-states.png"
                ));
                assertSnapshotHasVisibleContent(image, "Banners states");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(root, "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertTrue(banners.stream()
                                .filter(banner -> banner.getPrefWidth() <= 420.0)
                                .anyMatch(banner -> !isOutsideSceneViewport(
                                        banner,
                                        banner.localToScene(banner.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Banners page should render the narrow banner in a captured viewport");
                assertTrue(banners.stream()
                                .filter(banner ->
                                        banner.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT)
                                .anyMatch(banner -> !isOutsideSceneViewport(
                                        banner,
                                        banner.localToScene(banner.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Banners page should render the right-to-left banner in a captured viewport");

                WritableImage responsiveImage = snapshot(scene);
                writeVisualSnapshot(responsiveImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "banners-responsive-rtl.png"
                ));
                assertSnapshotHasVisibleContent(responsiveImage, "Banners responsive and RTL");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that interactive demo icon slots use SVG graphics instead of text placeholder icons.
    @Test
    void interactiveDemoIconSlotsUseVectorGraphics() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThread(() -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                for (Map.Entry<String, Integer> entry : DEMO_VECTOR_ICON_PAGE_MINIMUMS.entrySet()) {
                    String pageTitle = entry.getKey();
                    app.showPageForTesting(pageTitle);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                    assertDemoVectorIcons(scene.getRoot(), pageTitle, entry.getValue());
                }
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that every registered demo page renders visible content without obvious clipping or off-center glyphs.
    @Test
    void allDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertTrue(app.demoPageTitlesForTesting().size() > 20);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            @Unmodifiable List<String> pageTitles =
                    Objects.requireNonNull(appReference.get(), "app").demoPageTitlesForTesting();
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                for (String pageTitle : pageTitles) {
                    runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting(pageTitle);
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                    }, () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        assertCurrentPageTitle(scene, pageTitle);
                        assertSidebarSelectionMatchesCurrentPage(app, pageTitle);

                        WritableImage image = snapshot(scene);
                        writeVisualSnapshot(image, Path.of(
                                "build",
                                "reports",
                                "m3fx-demo-visual",
                                "demo-" + snapshotFileName(pageTitle) + ".png"
                        ));
                        assertSnapshotHasVisibleContent(image, pageTitle);
                        assertDemoPageVisualGeometry(scene, pageTitle);
                    });
                }
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that representative demo pages stay readable under the dark expressive theme combination.
    @Test
    void darkExpressiveDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(900.0);
            app.setDarkExpressiveThemeForTesting();

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertTrue(app.demoPageTitlesForTesting().containsAll(DARK_EXPRESSIVE_VISUAL_PAGES));
            assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                for (String pageTitle : DARK_EXPRESSIVE_VISUAL_PAGES) {
                    runOnFxThreadAfterPulses(THEME_LAYOUT_PULSES, () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting(pageTitle);
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                    }, () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        assertCurrentPageTitle(scene, pageTitle);
                        assertSidebarSelectionMatchesCurrentPage(app, pageTitle);
                        assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                        assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

                        WritableImage image = snapshot(scene);
                        writeVisualSnapshot(image, Path.of(
                                "build",
                                "reports",
                                "m3fx-demo-visual",
                                "demo-dark-expressive-" + snapshotFileName(pageTitle) + ".png"
                        ));
                        assertSnapshotHasVisibleContent(image, pageTitle);
                        assertDemoPageVisualGeometry(scene, pageTitle);
                    });
                }
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that representative demo pages keep visible content valid when the scene is mirrored for RTL locales.
    @Test
    void rightToLeftDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(900.0);

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertTrue(app.demoPageTitlesForTesting().containsAll(RTL_VISUAL_PAGES));
            scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                for (String pageTitle : RTL_VISUAL_PAGES) {
                    runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting(pageTitle);
                        scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                    }, () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        assertEquals(NodeOrientation.RIGHT_TO_LEFT, scene.getRoot().getEffectiveNodeOrientation());
                        assertCurrentPageTitle(scene, pageTitle);
                        assertSidebarSelectionMatchesCurrentPage(app, pageTitle);

                        WritableImage image = snapshot(scene);
                        writeVisualSnapshot(image, Path.of(
                                "build",
                                "reports",
                                "m3fx-demo-visual",
                                "demo-rtl-" + snapshotFileName(pageTitle) + ".png"
                        ));
                        assertSnapshotHasVisibleContent(image, pageTitle);
                        assertDemoPageVisualGeometry(scene, pageTitle);
                        if ("App Bars".equals(pageTitle)) {
                            for (M3TopAppBar appBar : visibleNodesOfType(scene.getRoot(), M3TopAppBar.class)) {
                                assertTopAppBarSlotGeometry(appBar);
                            }
                        }
                    });
                }
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that real mouse and focus interactions produce visible feedback in the demo window.
    @Test
    void interactiveDemoStatesProduceVisibleFeedback() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyButtonMouseFeedback(appReference, sceneReference);
            verifyButtonRippleReleaseAnimation(appReference, sceneReference);
            verifyTextFieldFocusFeedback(appReference, sceneReference);
            verifySidebarMouseFeedback(appReference, sceneReference);
            verifySidebarRippleReleaseAnimation(appReference, sceneReference);
            verifyIconToggleButtonMouseFeedback(appReference, sceneReference);
            verifyIconToggleButtonRippleReleaseAnimation(appReference, sceneReference);
            verifySwitchSelectionAnimation(appReference, sceneReference);
            verifyDisabledAnimationInteractionFeedback(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that popup-backed demo controls expose visible enter and exit motion frames.
    @Test
    void popupDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifySplitButtonPopupAnimation(appReference, sceneReference);
            verifyDatePickerFieldPopupAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that nested popup menu stacks expose visible motion and sane screen placement.
    @Test
    void nestedMenuPopupStackProducesDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> verifyNestedMenuPopupStackAnimation(
                    appReference,
                    sceneReference
            ));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that navigation selection and drawer disclosure animations expose visible intermediate frames.
    @Test
    void navigationDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyNavigationItemSelectionAnimation(
                    appReference,
                    sceneReference,
                    "Navigation",
                    "navigation-bar-selection"
            );
            verifyNavigationItemSelectionAnimation(
                    appReference,
                    sceneReference,
                    "Navigation Rail",
                    "navigation-rail-selection"
            );
            verifySidebarDrawerGroupExpansionAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that sheet visibility changes expose animated intermediate frames in the demo.
    @Test
    void sheetDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyBottomSheetVisibilityAnimation(appReference, sceneReference);
            verifySideSheetVisibilityAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that overlay surfaces render compactly and expose their expected interactive motion.
    @Test
    void overlayDemoSurfacesRenderAndAnimate() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifySnackbarHostAnimation(appReference, sceneReference);
            verifyFabMenuExpansionAnimation(appReference, sceneReference);
            verifyRichTooltipInteractiveLifetime(appReference, sceneReference);
            verifyDialogPopupSurface(sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that animated progress indicators visibly advance between real rendered frames.
    @Test
    void progressDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> pageReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> baselineFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> firstFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> secondFrameReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThreadWhen(() -> {
                @Nullable Node page = pageReference.get();
                @Nullable WritableImage baselineFrame = baselineFrameReference.get();
                @Nullable Scene scene = sceneReference.get();
                return page != null
                        && baselineFrame != null
                        && scene != null
                        && captureSceneFrameWithChangedNodeArea(page, baselineFrame, scene, firstFrameReference);
            }, () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Progress");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                pageReference.set(Objects.requireNonNull(firstVisibleNodeWithStyle(
                        scene.getRoot(),
                        "demo-page"
                ), "demo page"));
                baselineFrameReference.set(snapshot(scene));
            }, () -> {
                writeAnimationSnapshot(
                        Objects.requireNonNull(firstFrameReference.get(), "first progress frame"),
                        "progress",
                        "frame-a"
                );
            });

            runOnFxThreadWhen(() -> {
                @Nullable Node page = pageReference.get();
                @Nullable WritableImage firstFrame = firstFrameReference.get();
                @Nullable Scene scene = sceneReference.get();
                return page != null
                        && firstFrame != null
                        && scene != null
                        && captureSceneFrameWithChangedNodeArea(page, firstFrame, scene, secondFrameReference);
            }, () -> {
            }, () -> {
                writeAnimationSnapshot(
                        Objects.requireNonNull(secondFrameReference.get(), "second progress frame"),
                        "progress",
                        "frame-b"
                );
            });

            assertNodeAreaChanged(
                    Objects.requireNonNull(pageReference.get(), "progress page"),
                    Objects.requireNonNull(firstFrameReference.get(), "first progress frame"),
                    Objects.requireNonNull(secondFrameReference.get(), "second progress frame"),
                    "progress animation frames"
            );
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that animated loading indicators visibly morph between real rendered frames.
    @Test
    void loadingIndicatorDemoAnimationProducesDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> indicatorReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> baselineFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> firstFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> secondFrameReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThreadWhen(() -> {
                @Nullable Node indicator = indicatorReference.get();
                @Nullable WritableImage baselineFrame = baselineFrameReference.get();
                @Nullable Scene scene = sceneReference.get();
                return indicator != null
                        && baselineFrame != null
                        && scene != null
                        && captureSceneFrameWithChangedNodeArea(indicator, baselineFrame, scene, firstFrameReference);
            }, () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Loading Indicator");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                indicatorReference.set(Objects.requireNonNull(firstVisibleNodeWithStyle(
                        scene.getRoot(),
                        M3LoadingIndicator.STYLE_CLASS
                ), "loading indicator"));
                baselineFrameReference.set(snapshot(scene));
            }, () -> {
                writeAnimationSnapshot(
                        Objects.requireNonNull(firstFrameReference.get(), "first loading indicator frame"),
                        "loading-indicator",
                        "frame-a"
                );
            });

            runOnFxThreadWhen(() -> {
                @Nullable Node indicator = indicatorReference.get();
                @Nullable WritableImage firstFrame = firstFrameReference.get();
                @Nullable Scene scene = sceneReference.get();
                return indicator != null
                        && firstFrame != null
                        && scene != null
                        && captureSceneFrameWithChangedNodeArea(indicator, firstFrame, scene, secondFrameReference);
            }, () -> {
            }, () -> {
                writeAnimationSnapshot(
                        Objects.requireNonNull(secondFrameReference.get(), "second loading indicator frame"),
                        "loading-indicator",
                        "frame-b"
                );
            });

            assertNodeAreaChanged(
                    Objects.requireNonNull(indicatorReference.get(), "loading indicator"),
                    Objects.requireNonNull(firstFrameReference.get(), "first loading indicator frame"),
                    Objects.requireNonNull(secondFrameReference.get(), "second loading indicator frame"),
                    "loading indicator animation frames"
            );
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies hover and pressed feedback on a regular demo button.
    private static void verifyButtonMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, hoverReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Filled"
            ), "button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal button snapshot"),
                    "button",
                    "normal"
            );
            applyHoverPseudoState(target);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                    "button",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hoverReference, sceneReference, pressedReference, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed button snapshot"),
                    "button",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearHoverPseudoState(target);
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(normalReference.get(), "normal button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                "button hover"
        );
        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed button snapshot"),
                "button pressed"
        );
    }

    /// Verifies that button ripple release remains visible for an intermediate fade-out frame.
    private static void verifyButtonRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Buttons",
                "button-ripple",
                "button",
                root -> firstVisibleButtonWithText(root, "Filled")
        );
    }

    /// Verifies that toggle icon button ripple release remains visible for an intermediate fade-out frame.
    private static void verifyIconToggleButtonRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Icon Buttons",
                "icon-toggle-button-ripple",
                "toggle icon button",
                root -> firstVisibleNodeWithStyle(root, M3IconToggleButton.STYLE_CLASS)
        );
    }

    /// Verifies that sidebar navigation item ripple release remains visible for an intermediate fade-out frame.
    private static void verifySidebarRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Buttons",
                "sidebar-ripple",
                "sidebar item",
                root -> firstVisibleNodeWithStyle(root, "demo-sidebar-child-item")
        );
    }

    /// Verifies that a target's ripple release includes a visible intermediate fade-out frame.
    private static void verifyRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            String snapshotName,
            String targetName,
            Function<Node, @Nullable Node> targetLookup
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> rippleReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseStartOpacityReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> releaseStartReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseOpacityReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> releaseReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> settledOpacityReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, pressedReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting(pageTitle);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(targetLookup.apply(scene.getRoot()), targetName);
            M3MotionSettings.setMotionScheme(target, visualRippleMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal ripple target snapshot"),
                    snapshotName,
                    "normal"
            );
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed ripple target snapshot"),
                    snapshotName,
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), targetName);
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            Node ripple = Objects.requireNonNull(
                    target.lookup("." + RIPPLE_STYLE_CLASS),
                    targetName + " ripple node"
            );
            rippleReference.set(ripple);
            releaseStartOpacityReference.set(ripple.getOpacity());
            releaseStartReference.set(snapshot(scene));
        });

        runOnFxThreadWhen(() -> {
            @Nullable Node target = targetReference.get();
            @Nullable Node ripple = rippleReference.get();
            @Nullable Double releaseStartOpacity = releaseStartOpacityReference.get();
            @Nullable WritableImage releaseStart = releaseStartReference.get();
            @Nullable Scene scene = sceneReference.get();
            if (target == null
                    || ripple == null
                    || releaseStartOpacity == null
                    || ripple.getOpacity() <= 0.02
                    || ripple.getOpacity() >= releaseStartOpacity * 0.92
                    || releaseStart == null
                    || scene == null) {
                return false;
            }

            if (!captureSceneFrameWithChangedNodeArea(target, releaseStart, scene, releaseReference)) {
                return false;
            }

            releaseOpacityReference.set(ripple.getOpacity());
            return true;
        }, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(releaseReference.get(), "released ripple target snapshot"),
                    snapshotName,
                    "released"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, settledReference, () -> {
            @Nullable Node ripple = rippleReference.get();
            return ripple != null && ripple.getOpacity() <= 0.001;
        }, () -> {
        }, () -> {
            Node ripple = Objects.requireNonNull(rippleReference.get(), targetName + " ripple node");
            settledOpacityReference.set(ripple.getOpacity());
            writeInteractionSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled ripple target snapshot"),
                    snapshotName,
                    "settled"
            );
        });

        Node target = Objects.requireNonNull(targetReference.get(), targetName);
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal ripple target snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed ripple target snapshot"),
                targetName + " ripple pressed frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(releaseStartReference.get(), "release-start ripple target snapshot"),
                Objects.requireNonNull(releaseReference.get(), "released ripple target snapshot"),
                targetName + " ripple release intermediate frame"
        );
        assertRippleOpacityFaded(
                Objects.requireNonNull(releaseOpacityReference.get(), "released ripple opacity"),
                Objects.requireNonNull(settledOpacityReference.get(), "settled ripple opacity"),
                targetName + " ripple release fade-out"
        );
        M3MotionSettings.clearMotionScheme(target);
    }

    /// Returns a ripple-specific motion scheme that makes release fade frames observable in real snapshots.
    private static M3MotionScheme visualRippleMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                observableSpec,
                standard.slowEffects(),
                standard.fastSpatial(),
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies that switch selection produces visible thumb animation intermediate frames.
    private static void verifySwitchSelectionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Switch> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> intermediateReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, intermediateReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Switches");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3Switch target = Objects.requireNonNull(firstVisibleSwitchWithText(
                    scene.getRoot(),
                    "Off"
            ), "switch");
            M3MotionSettings.setMotionScheme(target, visualSwitchMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal switch snapshot"),
                    "switch-selection",
                    "normal"
            );
            target.fire();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertTrue(target.isSelected());
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                    "switch-selection",
                    "intermediate"
            );
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, intermediateReference, sceneReference, settledReference, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled switch snapshot"),
                    "switch-selection",
                    "settled"
            );
            M3MotionSettings.clearMotionScheme(Objects.requireNonNull(targetReference.get(), "switch"));
        });

        M3Switch target = Objects.requireNonNull(targetReference.get(), "switch");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal switch snapshot"),
                Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                "switch selection intermediate frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled switch snapshot"),
                "switch selection settling frame"
        );
    }

    /// Returns a switch-specific motion scheme that makes real visual intermediate frames observable.
    private static M3MotionScheme visualSwitchMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR),
                standard.defaultSpatial(),
                standard.slowSpatial()
        );
    }

    /// Verifies popup enter and exit motion on the demo split button menu.
    private static void verifySplitButtonPopupAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SplitButton> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> popupRootReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadWhen(() -> {
            @Nullable Node popupRoot = popupRootReference.get();
            @Nullable WritableImage openingBaseline = openingBaselineReference.get();
            return popupRoot != null
                    && openingBaseline != null
                    && popupRoot.getScene() != null
                    && captureNodeFrameChangedFrom(popupRoot, openingBaseline, openingReference);
        }, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Split Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SplitButton target = Objects.requireNonNull(firstVisibleSplitButtonWithText(
                    scene.getRoot(),
                    "Create"
            ), "split button");
            M3MotionSettings.setMotionScheme(target, visualPopupMotionScheme());
            target.showMenu();
            assertTrue(target.isShowing());

            Node popupRoot = target.getMenu();
            layoutPopupRoot(popupRoot);
            targetReference.set(target);
            popupRootReference.set(popupRoot);
            openingBaselineReference.set(snapshotNode(popupRoot));
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening split button popup snapshot"),
                    "split-button-popup",
                    "opening"
            );
        });

        runOnFxThreadWhenStable(() -> popupRootSettled(popupRootReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3SplitButton target = Objects.requireNonNull(targetReference.get(), "split button");
            assertTrue(target.isShowing());
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "split button popup");
            layoutPopupRoot(popupRoot);
            settledReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                    "split-button-popup",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                    "split button popup"
            );
            target.hideMenu();
        });

        runOnFxThreadWhen(() -> {
            @Nullable Node popupRoot = popupRootReference.get();
            @Nullable WritableImage settledFrame = settledReference.get();
            return popupRoot != null
                    && settledFrame != null
                    && popupRoot.getScene() != null
                    && captureNodeFrameChangedFrom(popupRoot, settledFrame, hidingReference);
        }, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding split button popup snapshot"),
                    "split-button-popup",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3SplitButton target = targetReference.get();
            return target != null && !target.isShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3SplitButton target = Objects.requireNonNull(targetReference.get(), "split button");
            assertFalse(target.isShowing());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening split button popup snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                "split button popup enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding split button popup snapshot"),
                "split button popup exit motion"
        );
    }

    /// Verifies nested menu popup enter and exit motion with side-by-side popup placement.
    private static void verifyNestedMenuPopupStackAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> ownerMenuReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadWhenStable(() -> {
            @Nullable M3MenuButton menuButton = menuButtonReference.get();
            return menuButton != null
                    && menuButton.isShowing()
                    && menuButton.getMenu().getScene() != null;
        }, SETTLED_STATE_PULSES, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Menus");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3MenuButton menuButton = Objects.requireNonNull(firstVisibleMenuButtonWithText(
                    scene.getRoot(),
                    "Open menu"
            ), "menu button");
            M3MotionSettings.setMotionScheme(menuButton, visualPopupMotionScheme());
            menuButton.showMenu();
            assertTrue(menuButton.isShowing());
            menuButtonReference.set(menuButton);
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            layoutPopupRoot(menuButton.getMenu());
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3MenuButton menuButton = menuButtonReference.get();
            return menuButton != null && popupRootSettled(menuButton.getMenu());
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            assertTrue(menuButton.isShowing());
            layoutPopupRoot(menuButton.getMenu());
            ownerMenuReference.set(snapshotNode(menuButton.getMenu()));
            writeAnimationSnapshot(
                    Objects.requireNonNull(ownerMenuReference.get(), "owner menu snapshot"),
                    "nested-menu-owner",
                    "settled"
            );

            M3SubMenuItem subMenuItem = Objects.requireNonNull(firstVisibleSubMenuItemWithText(
                    menuButton.getMenu(),
                    "Move to"
            ), "submenu item");
            M3MotionSettings.setMotionScheme(subMenuItem, visualPopupMotionScheme());
            subMenuItem.showSubMenu();
            assertTrue(subMenuItem.isSubMenuShowing());
            layoutPopupRoot(subMenuItem.getSubMenu());
            openingBaselineReference.set(snapshotNode(subMenuItem.getSubMenu()));
            subMenuItemReference.set(subMenuItem);
        });

        runOnFxThreadWhen(() -> {
            @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
            @Nullable WritableImage openingBaseline = openingBaselineReference.get();
            return subMenuItem != null
                    && openingBaseline != null
                    && subMenuItem.getSubMenu().getScene() != null
                    && captureNodeFrameChangedFrom(subMenuItem.getSubMenu(), openingBaseline, openingReference);
        }, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening submenu snapshot"),
                    "nested-submenu",
                    "opening"
            );
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
            return subMenuItem != null && popupRootSettled(subMenuItem.getSubMenu());
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            assertTrue(menuButton.isShowing());
            assertTrue(subMenuItem.isSubMenuShowing());
            layoutPopupRoot(menuButton.getMenu());
            layoutPopupRoot(subMenuItem.getSubMenu());
            assertPopupThemeContext(scene.getRoot(), menuButton.getMenu(), "owner menu");
            assertPopupThemeContext(scene.getRoot(), subMenuItem.getSubMenu(), "nested submenu");
            assertPopupSurfaceSize(menuButton.getMenu(), "owner menu");
            assertPopupSurfaceSize(subMenuItem.getSubMenu(), "nested submenu");
            settledReference.set(snapshotNode(subMenuItem.getSubMenu()));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                    "nested-submenu",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(ownerMenuReference.get(), "owner menu snapshot"),
                    "nested owner menu"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                    "nested submenu"
            );
            assertPopupStackSideBySide(menuButton.getMenu(), subMenuItem.getSubMenu());
            subMenuItem.hideSubMenu();
        });

        runOnFxThreadWhen(() -> {
            @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
            @Nullable WritableImage settledFrame = settledReference.get();
            return subMenuItem != null
                    && settledFrame != null
                    && subMenuItem.getSubMenu().getScene() != null
                    && captureNodeFrameChangedFrom(subMenuItem.getSubMenu(), settledFrame, hidingReference);
        }, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding submenu snapshot"),
                    "nested-submenu",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
            return subMenuItem != null && !subMenuItem.isSubMenuShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            assertFalse(subMenuItem.isSubMenuShowing());
            menuButton.hideMenu();
            M3MotionSettings.clearMotionScheme(subMenuItem);
            M3MotionSettings.clearMotionScheme(menuButton);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening submenu snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                "nested submenu enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding submenu snapshot"),
                "nested submenu exit motion"
        );
    }

    /// Verifies popup enter and exit motion on the demo date picker field.
    private static void verifyDatePickerFieldPopupAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3DatePickerField> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> popupRootReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadWhen(() -> {
            @Nullable Node popupRoot = popupRootReference.get();
            @Nullable WritableImage openingBaseline = openingBaselineReference.get();
            return popupRoot != null
                    && openingBaseline != null
                    && popupRoot.getScene() != null
                    && captureNodeFrameChangedFrom(popupRoot, openingBaseline, openingReference);
        }, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Date Pickers");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3DatePickerField target = Objects.requireNonNull(firstVisibleDatePickerField(
                    scene.getRoot()
            ), "date picker field");
            M3MotionSettings.setMotionScheme(target, visualPopupMotionScheme());
            target.showPicker();
            assertTrue(target.isShowing());

            Node popupRoot = Objects.requireNonNull(pickerPopupRoot(target), "date picker field popup");
            layoutPopupRoot(popupRoot);
            targetReference.set(target);
            popupRootReference.set(popupRoot);
            openingBaselineReference.set(snapshotNode(popupRoot));
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening date picker popup snapshot"),
                    "date-picker-field-popup",
                    "opening"
            );
        });

        runOnFxThreadWhenStable(() -> popupRootSettled(popupRootReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3DatePickerField target = Objects.requireNonNull(targetReference.get(), "date picker field");
            assertTrue(target.isShowing());
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "date picker field popup");
            layoutPopupRoot(popupRoot);
            settledReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                    "date-picker-field-popup",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                    "date picker field popup"
            );
            target.hidePicker();
        });

        runOnFxThreadWhen(() -> {
            @Nullable Node popupRoot = popupRootReference.get();
            @Nullable WritableImage settledFrame = settledReference.get();
            return popupRoot != null
                    && settledFrame != null
                    && popupRoot.getScene() != null
                    && captureNodeFrameChangedFrom(popupRoot, settledFrame, hidingReference);
        }, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding date picker popup snapshot"),
                    "date-picker-field-popup",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3DatePickerField target = targetReference.get();
            return target != null && !target.isShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3DatePickerField target = Objects.requireNonNull(targetReference.get(), "date picker field");
            assertFalse(target.isShowing());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening date picker popup snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                "date picker field popup enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding date picker popup snapshot"),
                "date picker field popup exit motion"
        );
    }

    /// Returns a popup-specific motion scheme that makes real visual intermediate frames observable.
    private static M3MotionScheme visualPopupMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR),
                standard.defaultSpatial(),
                standard.slowSpatial()
        );
    }

    /// Verifies selected-indicator motion on a demo navigation item.
    private static void verifyNavigationItemSelectionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            String snapshotName
    ) throws InterruptedException {
        AtomicReference<@Nullable M3NavigationItem> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> intermediateReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, intermediateReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting(pageTitle);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3NavigationItem target = Objects.requireNonNull(firstVisibleNavigationItemWithText(
                    scene.getRoot(),
                    "Search"
            ), "navigation item");
            assertFalse(target.isSelected());
            M3MotionSettings.setMotionScheme(target, visualNavigationMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal navigation snapshot"),
                    snapshotName,
                    "normal"
            );
            target.fire();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertTrue(target.isSelected());
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                    snapshotName,
                    "intermediate"
            );
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, intermediateReference, sceneReference, settledReference, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled navigation snapshot"),
                    snapshotName,
                    "settled"
            );
            M3MotionSettings.clearMotionScheme(Objects.requireNonNull(targetReference.get(), "navigation item"));
        });

        M3NavigationItem target = Objects.requireNonNull(targetReference.get(), "navigation item");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal navigation snapshot"),
                Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                snapshotName + " intermediate frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled navigation snapshot"),
                snapshotName + " settling frame"
        );
    }

    /// Verifies expand and collapse motion on the demo sidebar's visible drawer group.
    private static void verifySidebarDrawerGroupExpansionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3NavigationDrawerGroup> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, collapsedReference, sceneReference, expandingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3NavigationDrawerGroup target = Objects.requireNonNull(firstVisibleDrawerGroupWithTitle(
                    scene.getRoot(),
                    "Buttons"
            ), "sidebar drawer group");
            M3MotionSettings.setAnimationsEnabled(target, false);
            target.setExpanded(false);
            M3MotionSettings.clearAnimationsEnabled(target);
            M3MotionSettings.setMotionScheme(target, visualNavigationMotionScheme());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            targetReference.set(target);
            collapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsedReference.get(), "collapsed drawer group snapshot"),
                    "sidebar-drawer-group",
                    "collapsed"
            );
            target.setExpanded(true);
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                    "sidebar-drawer-group",
                    "expanding"
            );
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, expandingReference, sceneReference, expandedReference, () -> {
        }, () -> {
            M3NavigationDrawerGroup target = Objects.requireNonNull(targetReference.get(), "sidebar drawer group");
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                    "sidebar-drawer-group",
                    "expanded"
            );
            assertTrue(target.isExpanded());
            target.setExpanded(false);
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, expandedReference, sceneReference, collapsingReference, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                    "sidebar-drawer-group",
                    "collapsing"
            );
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, collapsingReference, sceneReference, settledReference, () -> {
        }, () -> {
            M3NavigationDrawerGroup target = Objects.requireNonNull(targetReference.get(), "sidebar drawer group");
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled drawer group snapshot"),
                    "sidebar-drawer-group",
                    "settled"
            );
            assertFalse(target.isExpanded());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(collapsedReference.get(), "collapsed drawer group snapshot"),
                Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                "sidebar drawer group expand intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                "sidebar drawer group expand settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                "sidebar drawer group collapse intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled drawer group snapshot"),
                "sidebar drawer group collapse settling frame"
        );
    }

    /// Returns a navigation-specific motion scheme that makes selection and disclosure frames observable.
    private static M3MotionScheme visualNavigationMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                observableSpec,
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies bottom sheet hide and show motion in the demo page.
    private static void verifyBottomSheetVisibilityAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3BottomSheet> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> shownReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> showingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> resettledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, shownReference, sceneReference, hidingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Bottom Sheets");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3BottomSheet target = Objects.requireNonNull(firstVisibleBottomSheetWithHeadline(
                    scene.getRoot(),
                    "Now playing"
            ), "bottom sheet");
            M3MotionSettings.setMotionScheme(target, visualSheetMotionScheme());
            targetReference.set(target);
            shownReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(shownReference.get(), "shown bottom sheet snapshot"),
                    "bottom-sheet",
                    "shown"
            );
            target.hide();
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                    "bottom-sheet",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> bottomSheetHidden(targetReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3BottomSheet target = Objects.requireNonNull(targetReference.get(), "bottom sheet");
            hiddenReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                    "bottom-sheet",
                    "hidden"
            );
            assertFalse(target.isVisible());
            target.show();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hiddenReference, sceneReference, showingReference, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                    "bottom-sheet",
                    "showing"
            );
        });

        runOnFxThreadWhenStable(() -> bottomSheetShown(targetReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3BottomSheet target = Objects.requireNonNull(targetReference.get(), "bottom sheet");
            resettledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(resettledReference.get(), "resettled bottom sheet snapshot"),
                    "bottom-sheet",
                    "resettled"
            );
            assertTrue(target.isShown());
            assertTrue(target.isVisible());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(shownReference.get(), "shown bottom sheet snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                "bottom sheet hide intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                "bottom sheet hide settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                "bottom sheet show intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                Objects.requireNonNull(resettledReference.get(), "resettled bottom sheet snapshot"),
                "bottom sheet show settling frame"
        );
    }

    /// Verifies side sheet hide and show motion in the demo page.
    private static void verifySideSheetVisibilityAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SideSheet> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> shownReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> showingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> resettledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, shownReference, sceneReference, hidingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Side Sheets");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SideSheet target = Objects.requireNonNull(firstVisibleSideSheetWithHeadline(
                    scene.getRoot(),
                    "Details"
            ), "side sheet");
            M3MotionSettings.setMotionScheme(target, visualSheetMotionScheme());
            targetReference.set(target);
            shownReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(shownReference.get(), "shown side sheet snapshot"),
                    "side-sheet",
                    "shown"
            );
            target.hide();
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                    "side-sheet",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> sideSheetHidden(targetReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SideSheet target = Objects.requireNonNull(targetReference.get(), "side sheet");
            hiddenReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                    "side-sheet",
                    "hidden"
            );
            assertFalse(target.isVisible());
            target.show();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hiddenReference, sceneReference, showingReference, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                    "side-sheet",
                    "showing"
            );
        });

        runOnFxThreadWhenStable(() -> sideSheetShown(targetReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SideSheet target = Objects.requireNonNull(targetReference.get(), "side sheet");
            resettledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(resettledReference.get(), "resettled side sheet snapshot"),
                    "side-sheet",
                    "resettled"
            );
            assertTrue(target.isShown());
            assertTrue(target.isVisible());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(shownReference.get(), "shown side sheet snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                "side sheet hide intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                "side sheet hide settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                "side sheet show intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                Objects.requireNonNull(resettledReference.get(), "resettled side sheet snapshot"),
                "side sheet show settling frame"
        );
    }

    /// Returns a sheet-specific motion scheme that makes visibility motion frames observable.
    private static M3MotionScheme visualSheetMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies snackbar entrance and dismissal motion on the demo host.
    private static void verifySnackbarHostAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SnackbarHost> hostReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(hostReference, hiddenBaselineReference, sceneReference, openingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Snackbars");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SnackbarHost host = Objects.requireNonNull(firstVisibleSnackbarHost(scene.getRoot()), "snackbar host");
            M3MotionSettings.setMotionScheme(host, visualOverlayMotionScheme());
            host.setDisplayDuration(Duration.INDEFINITE);
            hostReference.set(host);
            hiddenBaselineReference.set(snapshot(scene));
            host.show("Theme-aware snackbar", "Action", event -> {
            });
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            M3Snackbar snackbar = Objects.requireNonNull(host.getSnackbar(), "opening snackbar");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertSnackbarStaysCompact(scene, snackbar);
            openingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening snackbar snapshot"),
                    "snackbar-host",
                    "opening"
            );
        });

        runOnFxThreadWhenStable(() -> snackbarSettled(hostReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            M3Snackbar snackbar = Objects.requireNonNull(host.getSnackbar(), "settled snackbar");
            assertTrue(host.isShowing());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertSnackbarStaysCompact(scene, snackbar);
            settledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                    "snackbar-host",
                    "settled"
            );
            host.dismiss();
        });

        runOnFxThreadWhenNodeAreaChanged(hostReference, settledReference, sceneReference, hidingReference, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            assertNotNull(host.getSnackbar(), "hiding snackbar");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                    "snackbar-host",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> snackbarHidden(hostReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            assertFalse(host.isShowing());
            assertNull(host.getSnackbar(), "hidden snackbar");
            hiddenReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden snackbar snapshot"),
                    "snackbar-host",
                    "hidden"
            );
            M3MotionSettings.clearMotionScheme(host);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening snackbar snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                "snackbar host enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                "snackbar host exit motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden snackbar snapshot"),
                "snackbar host hidden frame"
        );
    }

    /// Verifies floating action button menu expand and collapse motion.
    private static void verifyFabMenuExpansionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3FabMenu> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> recollapsedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, collapsedReference, sceneReference, expandingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("FAB Menu");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3FabMenu target = Objects.requireNonNull(firstVisibleFabMenu(scene.getRoot(), false), "collapsed FAB menu");
            M3MotionSettings.setMotionScheme(target, visualOverlayMotionScheme());
            targetReference.set(target);
            collapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsedReference.get(), "collapsed FAB menu snapshot"),
                    "fab-menu",
                    "collapsed"
            );
            target.show();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                    "fab-menu",
                    "expanding"
            );
        });

        runOnFxThreadWhenStable(() -> fabMenuExpandedSettled(targetReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3FabMenu target = Objects.requireNonNull(targetReference.get(), "FAB menu");
            assertTrue(target.isExpanded());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertFabMenuActionsStayInsideShowcase(target);
            expandedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                    "fab-menu",
                    "expanded"
            );
            target.hide();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, expandedReference, sceneReference, collapsingReference, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                    "fab-menu",
                    "collapsing"
            );
        });

        runOnFxThreadWhenStable(() -> fabMenuCollapsedSettled(targetReference.get()), SETTLED_STATE_PULSES, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3FabMenu target = Objects.requireNonNull(targetReference.get(), "FAB menu");
            assertFalse(target.isExpanded());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            recollapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(recollapsedReference.get(), "recollapsed FAB menu snapshot"),
                    "fab-menu",
                    "recollapsed"
            );
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(collapsedReference.get(), "collapsed FAB menu snapshot"),
                Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                "FAB menu expanding frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                "FAB menu expanded frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                "FAB menu collapsing frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                Objects.requireNonNull(recollapsedReference.get(), "recollapsed FAB menu snapshot"),
                "FAB menu recollapsed frame"
        );
    }

    /// Verifies that a rich tooltip remains interactive while pointer focus transfers into its popup.
    private static void verifyRichTooltipInteractiveLifetime(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Button> ownerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> popupReference = new AtomicReference<>();

        runOnFxThreadWhen(() -> {
            @Nullable M3RichTooltip tooltip = tooltipReference.get();
            return tooltip != null && tooltip.isShowing() && tooltip.getScene() != null;
        }, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Tooltips");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3Button owner = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Rich action"
            ), "rich tooltip owner");
            M3Button action = new M3Button("Open");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    owner,
                    "Generated theme",
                    "The tooltip keeps its action surface available while pointer focus moves into the popup.",
                    action
            );
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setHideDelay(Duration.ZERO);
            tooltip.setShowDuration(Duration.INDEFINITE);
            ownerReference.set(owner);
            tooltipReference.set(tooltip);
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_ENTERED, false);
        }, () -> {
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "rich tooltip owner");
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertTrue(tooltip.isShowing());
            Node popupRoot = Objects.requireNonNull(tooltip.getScene(), "rich tooltip scene").getRoot();
            layoutPopupRoot(popupRoot);
            assertTooltipNearOwner(owner, popupRoot);
            assertRichTooltipActionInsidePopup(popupRoot);
            popupReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(popupReference.get(), "rich tooltip snapshot"),
                    "rich-tooltip",
                    "shown"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(popupReference.get(), "rich tooltip snapshot"),
                    "rich tooltip"
            );
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_EXITED, false);
            firePrimaryMouseEvent(popupRoot, MouseEvent.MOUSE_ENTERED, false);
        });

        runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
        }, () -> {
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertTrue(tooltip.isShowing());
            tooltip.hide();
        });

        runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
        }, () -> {
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "rich tooltip owner");
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertFalse(tooltip.isShowing());
            M3RichTooltip.uninstall(owner, tooltip);
        });
    }

    /// Verifies that the real dialog window uses a compact Material dialog pane.
    private static void verifyDialogPopupSurface(
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> dialogSnapshotReference = new AtomicReference<>();

        runOnFxThreadWhenStable(() -> {
            @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
            if (dialog == null || !dialog.isShowing()) {
                return false;
            }

            Parent dialogPane = dialog.getDialogPane();
            return dialogPane.getScene() != null
                    && dialogPane.getScene().getWindow() != null
                    && dialogPane.getScene().getWindow().isShowing()
                    && dialogPane.getLayoutBounds().getWidth() > 0.0
                    && dialogPane.getLayoutBounds().getHeight() > 0.0;
        }, SETTLED_STATE_PULSES, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3Dialog<ButtonType> dialog = new M3Dialog<>(
                    "M3FX Demo Dialog",
                    "Dialog title",
                    "The active theme is applied to this dialog pane.",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );
            dialog.initOwner(scene.getRoot());
            dialog.getDialogPane().setPrefWidth(420.0);
            dialog.show();
            dialogReference.set(dialog);
        }, () -> {
            Scene ownerScene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3Dialog<ButtonType> dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
            assertTrue(dialog.isShowing());
            Parent dialogPane = dialog.getDialogPane();
            dialogPane.applyCss();
            dialogPane.layout();
            assertDialogPaneStaysCompact(ownerScene, dialogPane);
            dialogSnapshotReference.set(snapshotNode(dialogPane));
            writeAnimationSnapshot(
                    Objects.requireNonNull(dialogSnapshotReference.get(), "dialog popup snapshot"),
                    "dialog-popup",
                    "shown"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(dialogSnapshotReference.get(), "dialog popup snapshot"),
                    "dialog popup"
            );
            dialog.close();
        });
    }

    /// Returns an overlay-specific motion scheme that makes popup and surface transitions observable.
    private static M3MotionScheme visualOverlayMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                observableSpec,
                observableSpec,
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies focus feedback on a populated text field in the demo page.
    private static void verifyTextFieldFocusFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> focusedReference = new AtomicReference<>();

        runOnFxThreadWhenStable(() -> {
            @Nullable Node target = targetReference.get();
            return target != null && target.isFocused();
        }, SETTLED_STATE_PULSES, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Text Fields");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    M3TextField.STYLE_CLASS
            ), "text field");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal text field snapshot"),
                    "text-field",
                    "normal"
            );
            target.requestFocus();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            focusedReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(focusedReference.get(), "focused text field snapshot"),
                    "text-field",
                    "focused"
            );
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "text field"),
                Objects.requireNonNull(normalReference.get(), "normal text field snapshot"),
                Objects.requireNonNull(focusedReference.get(), "focused text field snapshot"),
                "text field focus"
        );
    }

    /// Verifies hover and pressed feedback on a sidebar destination row.
    private static void verifySidebarMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, hoverReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    "demo-sidebar-child-item"
            ), "sidebar item");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal sidebar snapshot"),
                    "sidebar",
                    "normal"
            );
            applyHoverPseudoState(target);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                    "sidebar",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "sidebar item");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hoverReference, sceneReference, pressedReference, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed sidebar snapshot"),
                    "sidebar",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "sidebar item");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearHoverPseudoState(target);
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "sidebar item"),
                Objects.requireNonNull(normalReference.get(), "normal sidebar snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                "sidebar hover"
        );
        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "sidebar item"),
                Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed sidebar snapshot"),
                "sidebar pressed"
        );
    }

    /// Verifies hover and pressed feedback on a toggle icon button in the demo page.
    private static void verifyIconToggleButtonMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, hoverReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Icon Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    M3IconToggleButton.STYLE_CLASS
            ), "toggle icon button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal toggle icon button snapshot"),
                    "icon-toggle-button",
                    "normal"
            );
            applyHoverPseudoState(target);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                    "icon-toggle-button",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hoverReference, sceneReference, pressedReference, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed toggle icon button snapshot"),
                    "icon-toggle-button",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearHoverPseudoState(target);
        });

        Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal toggle icon button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                "toggle icon button hover"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed toggle icon button snapshot"),
                "toggle icon button pressed"
        );
    }

    /// Verifies that disabled animations still apply interaction states immediately.
    private static void verifyDisabledAnimationInteractionFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();

        runOnFxThreadAfterPulses(PAGE_LAYOUT_PULSES, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3MotionSettings.setAnimationsEnabled(scene.getRoot(), false);
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Filled"
            ), "button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            applyHoverPseudoState(target);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            hoverReference.set(snapshot(scene));
            M3MotionSettings.clearAnimationsEnabled(scene.getRoot());
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            clearHoverPseudoState(target);
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(normalReference.get(), "normal no-motion button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover no-motion button snapshot"),
                "button hover with animations disabled"
        );
    }

    /// Verifies that the requested page title is visible in the content area.
    private static void assertCurrentPageTitle(Scene scene, String pageTitle) {
        @Nullable Label title = null;
        for (Node node : scene.getRoot().lookupAll(".demo-page-title")) {
            if (node instanceof Label label && label.isVisible()) {
                title = label;
                break;
            }
        }
        assertNotNull(title, () -> "No visible page title for " + pageTitle);
        assertEquals(pageTitle, title.getText());
    }

    /// Verifies that the demo sidebar selection matches the displayed page.
    private static void assertSidebarSelectionMatchesCurrentPage(M3FXDemoApp app, String pageTitle) {
        String expected = Objects.requireNonNull(
                app.currentPageNavigationTitleForTesting(),
                () -> "No current page navigation title for " + pageTitle
        );
        String selected = Objects.requireNonNull(
                app.selectedSidebarNavigationTitleForTesting(),
                () -> "No selected sidebar item for " + pageTitle
        );
        assertEquals(expected, selected,
                () -> "Sidebar selection does not match page " + pageTitle
                        + ": expected=" + expected + ", selected=" + selected);
    }

    /// Captures the current scene root as a writable image.
    private static WritableImage snapshot(Scene scene) {
        int width = Math.max(1, (int) Math.ceil(scene.getWidth()));
        int height = Math.max(1, (int) Math.ceil(scene.getHeight()));
        WritableImage image = new WritableImage(width, height);
        scene.getRoot().snapshot(null, image);
        return image;
    }

    /// Captures one standalone node as a writable image.
    private static WritableImage snapshotNode(Node node) {
        Bounds bounds = node.getLayoutBounds();
        int width = Math.max(1, (int) Math.ceil(bounds.getWidth()));
        int height = Math.max(1, (int) Math.ceil(bounds.getHeight()));
        WritableImage image = new WritableImage(width, height);
        node.snapshot(null, image);
        return image;
    }

    /// Verifies that a snapshot contains enough non-background pixels to be useful as a visual artifact.
    private static void assertSnapshotHasVisibleContent(WritableImage image, String pageTitle) {
        int contrastingPixels = 0;
        Color background = image.getPixelReader().getColor(8, 8);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, background) > 0.08) {
                    contrastingPixels++;
                }
            }
        }
        int visiblePixels = contrastingPixels;
        assertTrue(visiblePixels > image.getWidth() * image.getHeight() * 0.02,
                () -> pageTitle + " snapshot has too little visible content: " + visiblePixels);
    }

    /// Verifies the shared page-level geometry checks for a rendered demo page.
    private static void assertDemoPageVisualGeometry(Scene scene, String pageTitle) {
        assertVisibleTextInsideScene(scene, pageTitle);
        assertVisibleMaterialControlsInsideScene(scene, pageTitle);
        assertFixedTargetGlyphsCentered(scene.getRoot(), pageTitle);
        assertSingleLineTextInputsHaveVerticalRoom(scene, pageTitle);
        assertSelectionIndicatorsCentered(scene, pageTitle);
        assertNavigationBadgesStayCompact(scene, pageTitle);
    }

    /// Verifies that a search bar keeps its editor and optional slots inside the rounded search container.
    private static void assertSearchBarVisualGeometry(M3SearchBar searchBar) {
        assertTrue(hasRenderableBounds(searchBar), () -> "search bar has no renderable bounds: " + searchBar);
        Bounds searchBounds = searchBar.localToScene(searchBar.getBoundsInLocal());
        Bounds editorBounds = searchBar.getEditor().localToScene(searchBar.getEditor().getBoundsInLocal());
        boolean embeddedInSearchView = nearestAncestorOfType(searchBar, M3SearchView.class) != null;

        if (embeddedInSearchView) {
            assertTrue(searchBounds.getHeight() >= 56.0 && searchBounds.getHeight() <= 72.0 + CONTROL_EDGE_TOLERANCE,
                    () -> "embedded search view header should stay within the Material search height range: "
                            + searchBounds);
        } else {
            assertEquals(56.0, searchBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "standalone search bar should use the Material 56dp container height: " + searchBounds);
        }
        assertTrue(containsBoundsWithTolerance(searchBounds, editorBounds, CONTROL_EDGE_TOLERANCE),
                () -> "search editor leaves the search bar container: editor="
                        + editorBounds + ", searchBar=" + searchBounds);

        @Nullable Node leading = searchBar.getLeading();
        assertNotNull(leading, "search bar should render a leading icon slot");
        assertTrue(hasRenderableBounds(leading), () -> "search leading slot has no renderable bounds: " + leading);
        Bounds leadingBounds = leading.localToScene(leading.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(searchBounds, leadingBounds, CONTROL_EDGE_TOLERANCE),
                () -> "search leading slot leaves the search bar container: leading="
                        + leadingBounds + ", searchBar=" + searchBounds);

        for (Node action : searchBar.getTrailingActions()) {
            assertTrue(action.isVisible() && hasRenderableBounds(action),
                    () -> "search trailing action should be visible: " + action);
            Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(searchBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "search trailing action leaves the search bar container: action="
                            + actionBounds + ", searchBar=" + searchBounds);
        }
    }

    /// Verifies that an active search view shows its result rows as reachable Material list items.
    private static void assertSearchViewResultsVisible(M3SearchView searchView) {
        assertTrue(searchView.isActive(), "search view should be active");
        assertEquals(3, searchView.getResults().size(), "active search view should show three demo results");
        assertTrue(searchView.getResultsContainer().isVisible(), "active search results should be visible");
        assertTrue(searchView.getResultsContainer().isManaged(), "active search results should be managed");
        assertEquals(1.0, searchView.getResultsContainer().getOpacity(), 0.001,
                "active search results should be fully opaque");
        assertEquals(0.0, searchView.getResultsContainer().getTranslateY(), 0.001,
                "active search results should not be translated");
        for (Node result : searchView.getResults()) {
            assertInstanceOf(M3ListItem.class, result,
                    () -> "search demo results should use Material list rows: " + result);
            assertTrue(result.isVisible() && hasRenderableBounds(result),
                    () -> "active search result should render visible content: " + result);
        }
    }

    /// Verifies that an inactive search view keeps its results out of layout and rendering.
    private static void assertSearchViewResultsHidden(M3SearchView searchView) {
        assertFalse(searchView.isActive(), "search view should be inactive");
        assertEquals(1, searchView.getResults().size(), "inactive search view should keep its model result");
        assertFalse(searchView.getResultsContainer().isVisible(), "inactive search results should be hidden");
        assertFalse(searchView.getResultsContainer().isManaged(), "inactive search results should not affect layout");
        assertEquals(0.0, searchView.getResultsContainer().getOpacity(), 0.001,
                "inactive search results should be transparent");
        assertTrue(searchView.getResultsContainer().getTranslateY() < 0.0,
                "inactive search results should keep the hidden offset");
    }

    /// Verifies that demo icon slots use renderable SVG paths and no legacy text-placeholder `M3Icon` nodes.
    private static void assertDemoVectorIcons(Node root, String pageTitle, int minimumIconCount) {
        int[] iconCount = {0};
        visitVisibleNodes(root, node -> {
            if (node.getStyleClass().contains(DemoIcons.STYLE_CLASS)) {
                iconCount[0]++;
                assertInstanceOf(SVGPath.class, node,
                        () -> pageTitle + " demo vector icon style is not applied to an SVGPath: " + node);
                assertTrue(hasRenderableBounds(node),
                        () -> pageTitle + " demo vector icon has no renderable bounds: " + node);
                assertTrue(isInsideStableDemoIconViewport(node),
                        () -> pageTitle + " demo vector icon is not wrapped in a stable viewport: " + node);
            }

            if (node instanceof M3Icon && hasDemoIconSlotStyle(node)) {
                fail(() -> pageTitle + " still uses a text-placeholder M3Icon in an interactive icon slot: "
                        + node.getStyleClass());
            }

            if (node instanceof M3ListItem listItem) {
                assertListItemSlotIsNotTextPlaceholder(listItem.getLeading(), pageTitle, "leading");
                assertListItemSlotIsNotTextPlaceholder(listItem.getTrailing(), pageTitle, "trailing");
            }
        });

        assertTrue(iconCount[0] >= minimumIconCount,
                () -> pageTitle + " rendered too few demo SVG icons: actual="
                        + iconCount[0] + ", minimum=" + minimumIconCount);
    }

    /// Verifies that a list item icon slot does not use a text-placeholder `M3Icon`.
    private static void assertListItemSlotIsNotTextPlaceholder(
            @Nullable Node slot,
            String pageTitle,
            String slotName
    ) {
        if (slot instanceof M3Icon icon) {
            fail(() -> pageTitle + " list item " + slotName
                    + " slot still uses a text-placeholder M3Icon: " + icon.getText());
        }
    }

    /// Returns whether a node has a demo style class historically used for interactive icon slots.
    private static boolean hasDemoIconSlotStyle(Node node) {
        return node.getStyleClass().contains("demo-icon-label")
                || node.getStyleClass().contains("demo-fab-icon")
                || node.getStyleClass().contains("demo-navigation-icon")
                || node.getStyleClass().contains("demo-app-bar-icon");
    }

    /// Returns whether a demo SVG icon is inside a fixed or explicitly sized icon viewport.
    private static boolean isInsideStableDemoIconViewport(Node node) {
        return nearestAncestorWithStyle(node, "demo-vector-icon-viewport") != null
                || nearestAncestorWithStyle(node, "demo-sample-icon") != null;
    }

    /// Verifies that visible text nodes intersecting the scene are not clipped by the scene viewport.
    private static void assertVisibleTextInsideScene(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof Text text) || text.getText().isBlank() || !hasRenderableBounds(text)) {
                return;
            }

            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            if (!sceneBounds.intersects(textBounds) || !sceneBounds.contains(textBounds.getCenterX(), textBounds.getCenterY())) {
                return;
            }
            @Nullable Node scrollViewport = nearestScrollViewport(text);
            if (scrollViewport != null) {
                Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
                if (!viewportBounds.intersects(textBounds)
                        || !viewportBounds.contains(textBounds.getCenterX(), textBounds.getCenterY())) {
                    return;
                }
                assertTrue(containsHorizontalBoundsWithTolerance(viewportBounds, textBounds, TEXT_EDGE_TOLERANCE),
                        () -> pageTitle + " visible text leaves its scroll viewport horizontally: text="
                                + text.getText() + ", bounds=" + textBounds + ", viewport=" + viewportBounds);
                if (touchesVerticalViewportEdge(textBounds, viewportBounds, TEXT_EDGE_TOLERANCE)) {
                    return;
                }
                assertTrue(containsBoundsWithTolerance(viewportBounds, textBounds, TEXT_EDGE_TOLERANCE),
                        () -> pageTitle + " visible text leaves its scroll viewport: text="
                                + text.getText() + ", bounds=" + textBounds + ", viewport=" + viewportBounds);
                return;
            }

            assertTrue(containsBoundsWithTolerance(sceneBounds, textBounds, TEXT_EDGE_TOLERANCE),
                    () -> pageTitle + " visible text leaves the scene viewport: text="
                            + text.getText() + ", bounds=" + textBounds + ", scene=" + sceneBounds);
        });
    }

    /// Verifies that visible Material controls stay inside the visible scene and scroll viewport.
    private static void assertVisibleMaterialControlsInsideScene(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!isPageLevelMaterialControl(node) || !hasRenderableBounds(node)) {
                return;
            }

            Bounds controlBounds = node.localToScene(node.getBoundsInLocal());
            if (isOutsideSceneViewport(node, controlBounds, sceneBounds)) {
                return;
            }

            @Nullable Node scrollViewport = nearestScrollViewport(node);
            if (scrollViewport != null) {
                Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
                assertTrue(containsHorizontalBoundsWithTolerance(viewportBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                        () -> pageTitle + " visible control leaves its scroll viewport horizontally: node="
                                + node + ", bounds=" + controlBounds + ", viewport=" + viewportBounds);
                if (touchesVerticalViewportEdge(controlBounds, viewportBounds, CONTROL_EDGE_TOLERANCE)) {
                    return;
                }
                assertTrue(containsBoundsWithTolerance(viewportBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                        () -> pageTitle + " visible control leaves its scroll viewport: node="
                                + node + ", bounds=" + controlBounds + ", viewport=" + viewportBounds);
                return;
            }

            assertTrue(containsBoundsWithTolerance(sceneBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                    () -> pageTitle + " visible control leaves the scene viewport: node="
                            + node + ", bounds=" + controlBounds + ", scene=" + sceneBounds);
        });
    }

    /// Returns the nearest scroll pane viewport that clips a node, or `null` when the node is not inside one.
    private static @Nullable Node nearestScrollViewport(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane scrollPane) {
                return scrollPane.lookup(".viewport");
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Verifies that fixed-size Material targets keep their glyph text centered.
    private static void assertFixedTargetGlyphsCentered(Node root, String pageTitle) {
        visitVisibleNodes(root, node -> {
            if (!isCenteredTarget(node) || !hasRenderableBounds(node)) {
                return;
            }

            @Nullable Node glyph = firstVisibleText(node);
            if (glyph == null || !hasRenderableBounds(glyph)) {
                glyph = centeredDemoVectorIconNode(node);
            }
            if (glyph == null || !hasRenderableBounds(glyph)) {
                return;
            }

            Node renderedGlyph = glyph;
            Bounds targetBounds = node.localToScene(node.getBoundsInLocal());
            Bounds glyphBounds = renderedGlyph.localToScene(renderedGlyph.getBoundsInLocal());
            double dx = Math.abs(targetBounds.getCenterX() - glyphBounds.getCenterX());
            double dy = Math.abs(targetBounds.getCenterY() - glyphBounds.getCenterY());
            assertTrue(dx <= 3.0 && dy <= 3.5,
                    () -> pageTitle + " fixed target glyph is off-center: target="
                            + node + ", glyph=" + renderedGlyph + ", dx=" + dx + ", dy=" + dy
                            + ", targetBounds=" + targetBounds + ", glyphBounds=" + glyphBounds);
        });
    }

    /// Returns the node whose center represents a demo vector icon target.
    private static @Nullable Node centeredDemoVectorIconNode(Node root) {
        @Nullable Node icon = firstVisibleDemoVectorIcon(root);
        if (icon == null) {
            return null;
        }

        @Nullable Parent viewport = nearestAncestorWithStyle(icon, "demo-vector-icon-viewport");
        return viewport == null ? icon : viewport;
    }

    /// Verifies that single-line text input glyphs have visible vertical room inside their field containers.
    private static void assertSingleLineTextInputsHaveVerticalRoom(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3TextInputLayout layout) || !hasRenderableBounds(layout)) {
                return;
            }

            TextInputControl input = layout.getInput();
            if (input == null || input instanceof M3TextArea || !input.isVisible() || !hasRenderableBounds(input)) {
                return;
            }

            @Nullable Text text = firstVisibleText(input);
            if (text == null || !hasRenderableBounds(text)) {
                return;
            }

            Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            if (isOutsideSceneViewport(text, textBounds, sceneBounds)) {
                return;
            }

            double topRoom = textBounds.getMinY() - inputBounds.getMinY();
            double bottomRoom = inputBounds.getMaxY() - textBounds.getMaxY();
            double centerRatio = (textBounds.getCenterY() - inputBounds.getMinY()) / inputBounds.getHeight();
            assertTrue(topRoom >= INPUT_TEXT_MINIMUM_VERTICAL_ROOM
                            && bottomRoom >= INPUT_TEXT_MINIMUM_VERTICAL_ROOM
                            && centerRatio >= INPUT_TEXT_MINIMUM_CENTER_RATIO
                            && centerRatio <= INPUT_TEXT_MAXIMUM_CENTER_RATIO,
                    () -> pageTitle + " text input glyph has unsafe vertical geometry: text="
                            + text.getText() + ", topRoom=" + topRoom + ", bottomRoom=" + bottomRoom
                            + ", centerRatio=" + centerRatio + ", inputBounds=" + inputBounds
                            + ", textBounds=" + textBounds);
        });
    }

    /// Verifies that selection-control indicators keep their active pieces centered in real rendered geometry.
    private static void assertSelectionIndicatorsCentered(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (node instanceof M3RadioButton radioButton && hasRenderableBounds(radioButton)) {
                assertRadioDotCentered(radioButton, sceneBounds, pageTitle);
            } else if (node instanceof M3Switch switchControl && hasRenderableBounds(switchControl)) {
                assertSwitchThumbInsideTrack(
                        switchControl,
                        sceneBounds,
                        pageTitle
                );
            }
        });
    }

    /// Verifies that navigation badges stay compact and anchored instead of stretching over the selected indicator.
    private static void assertNavigationBadgesStayCompact(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3NavigationItem item) || !hasRenderableBounds(item)) {
                return;
            }

            @Nullable Node badge = item.lookup(".m3-navigation-item-badge");
            @Nullable Node indicator = item.lookup(".m3-navigation-item-indicator");
            if (badge == null || indicator == null || !hasRenderableBounds(badge) || !hasRenderableBounds(indicator)) {
                return;
            }

            Bounds badgeBounds = badge.localToScene(badge.getBoundsInLocal());
            Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
            if (isOutsideSceneViewport(badge, badgeBounds, sceneBounds)) {
                return;
            }

            double maximumBadgeWidth = Math.max(24.0, indicatorBounds.getWidth() * 0.65);
            double minimumCenterOffset = Math.max(6.0, indicatorBounds.getWidth() * 0.16);
            double centerOffset = Math.abs(badgeBounds.getCenterX() - indicatorBounds.getCenterX());
            assertTrue(badgeBounds.getWidth() <= maximumBadgeWidth && centerOffset >= minimumCenterOffset,
                    () -> pageTitle + " navigation badge has unsafe indicator geometry: badgeBounds="
                            + badgeBounds + ", indicatorBounds=" + indicatorBounds + ", centerOffset=" + centerOffset);
        });
    }

    /// Verifies that an app bar fills the horizontal span of its demo preview surface.
    private static void assertAppBarFitsPreview(Node appBar, String description) {
        Parent preview = Objects.requireNonNull(
                nearestAncestorWithStyle(appBar, "demo-app-bar-preview"),
                description + " preview"
        );
        Bounds previewBounds = preview.localToScene(preview.getLayoutBounds());
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        assertTrue(appBarBounds.getWidth() >= APP_BAR_PREVIEW_MIN_WIDTH,
                () -> description + " is too narrow for an app preview: bounds=" + appBarBounds);
        assertTrue(Math.abs(appBarBounds.getMinX() - previewBounds.getMinX()) <= CONTROL_EDGE_TOLERANCE
                        && Math.abs(appBarBounds.getMaxX() - previewBounds.getMaxX()) <= CONTROL_EDGE_TOLERANCE,
                () -> description + " does not fill its preview horizontally: appBarBounds=" + appBarBounds
                        + ", previewBounds=" + previewBounds);
        assertTrue(appBarBounds.getHeight() >= 60.0,
                () -> description + " height is too small: bounds=" + appBarBounds);
    }

    /// Verifies that a top app bar preview uses token height and includes an app content context.
    private static void assertTopAppBarPreviewBalance(M3TopAppBar appBar) {
        Parent preview = Objects.requireNonNull(
                nearestAncestorWithStyle(appBar, "demo-app-bar-preview"),
                "top app bar preview"
        );
        Node previewContent = requireVisibleStyledDescendant(
                preview,
                "demo-top-app-bar-preview-content",
                "top app bar preview content"
        );
        Bounds previewBounds = preview.localToScene(preview.getLayoutBounds());
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        Bounds previewContentBounds = previewContent.localToScene(previewContent.getLayoutBounds());
        double expectedHeight = expectedTopAppBarHeight(appBar);

        assertEquals(expectedHeight, appBarBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar variant height drifted: variant=" + appBar.getVariant()
                        + ", expected=" + expectedHeight + ", bounds=" + appBarBounds);
        assertEquals(previewBounds.getMinY(), appBarBounds.getMinY(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar should sit at the top edge of its preview: variant=" + appBar.getVariant()
                        + ", previewBounds=" + previewBounds + ", appBarBounds=" + appBarBounds);
        assertTrue(previewContentBounds.getMinY() >= appBarBounds.getMaxY() - CONTROL_EDGE_TOLERANCE,
                () -> "top app bar preview content should be below the bar: variant=" + appBar.getVariant()
                        + ", contentBounds=" + previewContentBounds + ", appBarBounds=" + appBarBounds);
        assertTrue(previewBounds.getHeight() >= expectedHeight + 96.0,
                () -> "top app bar preview should include visible app content context: variant=" + appBar.getVariant()
                        + ", previewBounds=" + previewBounds + ", appBarBounds=" + appBarBounds);
        assertTopAppBarPreviewSurfaceTreatment(appBar, preview, previewContent);
    }

    /// Verifies that default and scrolled-under previews use the expected Material surface treatment.
    private static void assertTopAppBarPreviewSurfaceTreatment(
            M3TopAppBar appBar,
            Parent preview,
            Node previewContent
    ) {
        Region previewRegion = assertInstanceOf(Region.class, preview, "top app bar preview region");
        Region contentRegion = assertInstanceOf(Region.class, previewContent, "top app bar preview content region");
        assertNotNull(previewRegion.getBorder(), "top app bar preview should expose a visible app frame border");
        assertFalse(previewRegion.getBorder().getStrokes().isEmpty(),
                "top app bar preview should expose a visible app frame border");

        Color appBarColor = Objects.requireNonNull(firstBackgroundColor(appBar), "top app bar background color");
        Color contentColor = Objects.requireNonNull(
                firstBackgroundColor(contentRegion),
                "top app bar preview content background color"
        );
        double surfaceDistance = colorDistance(appBarColor, contentColor);
        if (appBar.isScrolledUnder()) {
            assertTrue(surfaceDistance > 0.01,
                    () -> "scrolled-under top app bar should use a distinct container surface: appBar="
                            + appBarColor + ", content=" + contentColor);
            assertNotNull(appBar.getEffect(), "scrolled-under top app bar should expose elevation in the preview");
        } else {
            assertTrue(surfaceDistance <= 0.01,
                    () -> "default top app bar should share the app content surface until content scrolls under it: appBar="
                            + appBarColor + ", content=" + contentColor);
            assertNull(appBar.getEffect(), "default top app bar should not show scrolled-under elevation");
        }
    }

    /// Verifies that app bar previews are not placed in the generic rounded showcase container.
    private static void assertAppBarPreviewIsNotInsideGenericFlow(M3TopAppBar appBar) {
        @Nullable Parent preview = nearestAncestorWithStyle(appBar, "demo-app-bar-preview");
        assertNotNull(preview, "top app bar preview");
        @Nullable Node genericFlow = nearestDemoFlowAncestor(preview);
        assertNull(genericFlow, () -> "top app bar preview should not sit inside the generic rounded demo flow: "
                + genericFlow);
    }

    /// Verifies that a bottom app bar preview frames only the toolbar component.
    private static void assertBottomAppBarPreviewBalance(M3BottomAppBar appBar) {
        Parent preview = Objects.requireNonNull(
                nearestAncestorWithStyle(appBar, "demo-app-bar-preview"),
                "bottom app bar preview"
        );
        Bounds previewBounds = preview.localToScene(preview.getBoundsInLocal());
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        double expectedHeight = appBar.getContainerHeight();

        assertEquals(expectedHeight, appBarBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "bottom app bar token height drifted: expected=" + expectedHeight
                        + ", bounds=" + appBarBounds);
        assertTrue(previewBounds.getHeight() >= expectedHeight
                        && previewBounds.getHeight() <= expectedHeight + CONTROL_EDGE_TOLERANCE * 2.0,
                () -> "bottom app bar preview should frame only the component and its outline: previewBounds="
                        + previewBounds + ", appBarBounds=" + appBarBounds);
        assertTrue(containsBoundsWithTolerance(previewBounds, appBarBounds, CONTROL_EDGE_TOLERANCE),
                () -> "bottom app bar should remain inside its preview frame: previewBounds="
                        + previewBounds + ", appBarBounds=" + appBarBounds);
    }

    /// Verifies that a demo bottom app bar places its floating action slot with Material geometry.
    private static void assertBottomAppBarSlotGeometry(M3BottomAppBar appBar) {
        Node actions = requireVisibleStyledDescendant(
                appBar,
                M3BottomAppBar.ACTIONS_STYLE_CLASS,
                "bottom app bar actions slot"
        );
        Node floatingAction = requireVisibleStyledDescendant(
                appBar,
                M3BottomAppBar.FLOATING_ACTION_STYLE_CLASS,
                "bottom app bar floating action slot"
        );
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        Bounds actionsBounds = actions.localToScene(actions.getLayoutBounds());
        Bounds floatingActionBounds = floatingAction.localToScene(floatingAction.getLayoutBounds());
        List<Node> actionSlots = visibleNodesWithStyle(appBar, M3BottomAppBar.ACTION_SLOT_STYLE_CLASS);
        double contentMinX = appBarBounds.getMinX() + appBar.getHorizontalPadding();
        double contentMaxX = appBarBounds.getMaxX() - appBar.getHorizontalPadding();
        double contentCenterX = (contentMinX + contentMaxX) / 2.0;
        boolean rightToLeft = appBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        assertEquals(appBarBounds.getCenterY(), actionsBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                () -> "bottom app bar actions are not vertically centered: actionsBounds=" + actionsBounds
                        + ", appBarBounds=" + appBarBounds);
        assertEquals(appBarBounds.getCenterY(), floatingActionBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                () -> "bottom app bar floating action is not vertically centered: floatingActionBounds="
                        + floatingActionBounds + ", appBarBounds=" + appBarBounds);
        assertBottomAppBarActionSlots(appBar, actionSlots, appBarBounds.getCenterY());

        if (appBar.getFloatingActionAlignment() == M3BottomAppBarFloatingActionAlignment.CENTER) {
            assertEquals(contentCenterX, floatingActionBounds.getCenterX(), APP_BAR_SLOT_TOLERANCE,
                    () -> "center bottom app bar floating action should stay centered in the full content area: "
                            + floatingActionBounds + ", appBarBounds=" + appBarBounds);
        } else if (appBar.getFloatingActionAlignment() == M3BottomAppBarFloatingActionAlignment.START) {
            if (rightToLeft) {
                assertEquals(contentMaxX, floatingActionBounds.getMaxX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "RTL start bottom app bar floating action should be on the physical right edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            } else {
                assertEquals(contentMinX, floatingActionBounds.getMinX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "start bottom app bar floating action should be on the leading edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            }
        } else {
            if (rightToLeft) {
                assertEquals(contentMinX, floatingActionBounds.getMinX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "RTL end bottom app bar floating action should be on the physical left edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            } else {
                assertEquals(contentMaxX, floatingActionBounds.getMaxX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "end bottom app bar floating action should be on the trailing edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            }
        }
    }

    /// Verifies that a demo bottom app bar uses 48 dp Material action slots.
    private static void assertBottomAppBarActionSlots(
            M3BottomAppBar appBar,
            List<Node> actionSlots,
            double rowCenterY
    ) {
        assertEquals(appBar.getActions().size(), actionSlots.size(),
                () -> "bottom app bar action slot count mismatch: expected=" + appBar.getActions().size()
                        + ", actual=" + actionSlots.size());

        @Nullable Bounds previousBounds = null;
        for (Node actionSlot : actionSlots) {
            Bounds slotBounds = actionSlot.localToScene(actionSlot.getBoundsInLocal());
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    () -> "bottom app bar action slot width is not 48dp: " + slotBounds);
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "bottom app bar action slot height is not 48dp: " + slotBounds);
            assertEquals(rowCenterY, slotBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                    () -> "bottom app bar action slot is not centered in the bar row: "
                            + slotBounds + ", rowCenterY=" + rowCenterY);
            if (previousBounds != null) {
                Bounds previousSlotBounds = previousBounds;
                assertEquals(appBar.getActionSpacing(), slotBounds.getMinX() - previousSlotBounds.getMaxX(),
                        CONTROL_EDGE_TOLERANCE,
                        () -> "bottom app bar action slots do not use the configured spacing: previous="
                                + previousSlotBounds + ", slot=" + slotBounds);
            }
            previousBounds = slotBounds;
        }
    }

    /// Returns the expected rendered height for a top app bar variant.
    private static double expectedTopAppBarHeight(M3TopAppBar appBar) {
        return switch (appBar.getVariant()) {
            case MEDIUM -> appBar.getMediumContainerHeight();
            case LARGE -> appBar.getLargeContainerHeight();
            case SMALL, CENTER_ALIGNED -> appBar.getContainerHeight();
        };
    }

    /// Verifies that top app bar slots use token-backed padding and variant-specific title placement.
    private static void assertTopAppBarSlotGeometry(M3TopAppBar appBar) {
        Node navigation = requireVisibleStyledDescendant(
                appBar,
                M3TopAppBar.NAVIGATION_STYLE_CLASS,
                "top app bar navigation slot"
        );
        Node title = requireVisibleStyledDescendant(
                appBar,
                M3TopAppBar.TITLE_STYLE_CLASS,
                "top app bar title slot"
        );
        Node actions = requireVisibleStyledDescendant(
                appBar,
                M3TopAppBar.ACTIONS_STYLE_CLASS,
                "top app bar actions slot"
        );

        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        Bounds navigationBounds = navigation.localToScene(navigation.getBoundsInLocal());
        Bounds titleBounds = title.localToScene(title.getBoundsInLocal());
        Bounds actionsBounds = actions.localToScene(actions.getBoundsInLocal());
        Node titleText = Objects.requireNonNull(firstVisibleText(title), "top app bar rendered title text");
        Bounds titleTextBounds = titleText.localToScene(titleText.getBoundsInLocal());
        List<Node> actionSlots = visibleNodesWithStyle(appBar, M3TopAppBar.ACTION_SLOT_STYLE_CLASS);
        double horizontalPadding = appBar.getHorizontalPadding();
        double rowCenterY = appBarBounds.getMinY() + appBar.getContainerHeight() / 2.0;
        boolean rightToLeft = appBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        assertTopAppBarActionSlots(appBar, navigation, actionSlots, rowCenterY);
        if (rightToLeft) {
            assertEquals(
                    appBarBounds.getMaxX() - horizontalPadding,
                    navigationBounds.getMaxX(),
                    APP_BAR_SLOT_TOLERANCE,
                    () -> "RTL top app bar navigation slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", navigationBounds=" + navigationBounds
            );
            assertEquals(
                    appBarBounds.getMinX() + horizontalPadding,
                    actionsBounds.getMinX(),
                    APP_BAR_SLOT_TOLERANCE,
                    () -> "RTL top app bar actions slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", actionsBounds=" + actionsBounds
            );
        } else {
            assertEquals(appBarBounds.getMinX() + horizontalPadding, navigationBounds.getMinX(), APP_BAR_SLOT_TOLERANCE,
                    () -> "top app bar navigation slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", navigationBounds=" + navigationBounds);
            assertEquals(appBarBounds.getMaxX() - horizontalPadding, actionsBounds.getMaxX(), APP_BAR_SLOT_TOLERANCE,
                    () -> "top app bar actions slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", actionsBounds=" + actionsBounds);
        }

        switch (appBar.getVariant()) {
            case CENTER_ALIGNED -> assertEquals(
                    appBarBounds.getCenterX(),
                    titleTextBounds.getCenterX(),
                    APP_BAR_SLOT_TOLERANCE,
                    () -> "center-aligned top app bar title is not centered: titleBounds=" + titleBounds
                            + ", titleTextBounds=" + titleTextBounds
                            + ", appBarBounds=" + appBarBounds
            );
            case MEDIUM, LARGE -> {
                double bottomPadding = appBar.getVariant() == M3TopAppBarVariant.MEDIUM
                        ? appBar.getMediumBottomPadding()
                        : appBar.getLargeBottomPadding();
                if (rightToLeft) {
                    assertEquals(
                            appBarBounds.getMaxX() - horizontalPadding,
                            titleTextBounds.getMaxX(),
                            APP_BAR_SLOT_TOLERANCE,
                            () -> "RTL tall top app bar title text does not respect leading padding: variant="
                                    + appBar.getVariant() + ", titleTextBounds=" + titleTextBounds
                                    + ", titleBounds=" + titleBounds + ", appBarBounds=" + appBarBounds
                    );
                } else {
                    assertEquals(
                            appBarBounds.getMinX() + horizontalPadding,
                            titleBounds.getMinX(),
                            APP_BAR_SLOT_TOLERANCE,
                            () -> "tall top app bar title does not respect leading padding: variant="
                                    + appBar.getVariant() + ", titleBounds=" + titleBounds
                                    + ", appBarBounds=" + appBarBounds
                    );
                }
                assertEquals(appBarBounds.getMaxY() - bottomPadding, titleBounds.getMaxY(), APP_BAR_SLOT_TOLERANCE,
                        () -> "tall top app bar title does not respect bottom padding: variant="
                                + appBar.getVariant() + ", titleBounds=" + titleBounds
                                + ", appBarBounds=" + appBarBounds);
                assertTrue(titleBounds.getCenterY() > appBarBounds.getCenterY(),
                        () -> "tall top app bar title should sit below the icon row: variant="
                                + appBar.getVariant() + ", titleBounds=" + titleBounds
                                + ", appBarBounds=" + appBarBounds);
            }
            case SMALL -> {
                assertEquals(rowCenterY, titleTextBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                        () -> "small top app bar title is not centered in the toolbar row: titleTextBounds="
                                + titleTextBounds + ", titleBounds=" + titleBounds + ", appBarBounds=" + appBarBounds);
                if (rightToLeft) {
                    assertTrue(titleTextBounds.getMaxX() <= navigationBounds.getMinX() + APP_BAR_SLOT_TOLERANCE,
                            () -> "RTL small top app bar title text overlaps navigation slot: titleTextBounds="
                                    + titleTextBounds + ", navigationBounds=" + navigationBounds);
                } else {
                    assertTrue(titleBounds.getMinX() >= navigationBounds.getMaxX() - APP_BAR_SLOT_TOLERANCE,
                            () -> "small top app bar title overlaps navigation slot: titleBounds="
                                    + titleBounds + ", navigationBounds=" + navigationBounds);
                }
            }
        }
    }

    /// Verifies that a demo top app bar uses 48 dp Material navigation and action slots.
    private static void assertTopAppBarActionSlots(
            M3TopAppBar appBar,
            Node navigation,
            List<Node> actionSlots,
            double rowCenterY
    ) {
        Bounds navigationBounds = navigation.localToScene(navigation.getBoundsInLocal());

        assertEquals(APP_BAR_ACTION_SLOT_SIZE, navigationBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar navigation slot width is not 48dp: " + navigationBounds);
        assertEquals(APP_BAR_ACTION_SLOT_SIZE, navigationBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar navigation slot height is not 48dp: " + navigationBounds);
        assertEquals(rowCenterY, navigationBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                () -> "top app bar navigation slot is not centered in the icon row: "
                        + navigationBounds + ", rowCenterY=" + rowCenterY);
        assertEquals(appBar.getActions().size(), actionSlots.size(),
                () -> "top app bar action slot count mismatch: expected=" + appBar.getActions().size()
                        + ", actual=" + actionSlots.size());

        @Nullable Bounds previousBounds = null;
        for (Node actionSlot : actionSlots) {
            Bounds slotBounds = actionSlot.localToScene(actionSlot.getBoundsInLocal());
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    () -> "top app bar action slot width is not 48dp: " + slotBounds);
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "top app bar action slot height is not 48dp: " + slotBounds);
            assertEquals(rowCenterY, slotBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                    () -> "top app bar action slot is not centered in the icon row: "
                            + slotBounds + ", rowCenterY=" + rowCenterY);
            if (previousBounds != null) {
                Bounds previousSlotBounds = previousBounds;
                assertEquals(appBar.getActionSpacing(), slotBounds.getMinX() - previousSlotBounds.getMaxX(),
                        CONTROL_EDGE_TOLERANCE,
                        () -> "top app bar action slots do not use the configured spacing: previous="
                                + previousSlotBounds + ", slot=" + slotBounds);
            }
            previousBounds = slotBounds;
        }
    }

    /// Verifies that app bar actions are icon-only vector buttons rather than text placeholders.
    private static void assertAppBarUsesVectorIconButtons(Node appBar, String description, String... expectedIconNames) {
        List<M3IconButton> iconButtons = visibleNodesOfType(appBar, M3IconButton.class);
        assertEquals(expectedIconNames.length, iconButtons.size(),
                () -> description + " should use icon button actions");
        for (int index = 0; index < iconButtons.size(); index++) {
            M3IconButton iconButton = iconButtons.get(index);
            assertTrue(iconButton.getText().isEmpty(),
                    () -> description + " icon button should not expose placeholder text: " + iconButton.getText());
            Node graphic = Objects.requireNonNull(iconButton.getGraphic(),
                    description + " icon button graphic");
            assertNull(firstVisibleText(graphic),
                    () -> description + " icon button graphic should not contain rendered text placeholders: "
                            + graphic);
            @Nullable Node vectorIcon = firstVisibleDemoVectorIcon(graphic);
            assertNotNull(vectorIcon,
                    () -> description + " icon button should contain a visible demo vector icon graphic");
            String expectedIconColorStyle = expectedAppBarIconColorStyle(appBar, iconButton);
            assertTrue(vectorIcon.getStyleClass().contains(expectedIconColorStyle),
                    () -> description + " icon button should use the Material app bar icon color "
                            + expectedIconColorStyle + ": " + vectorIcon.getStyleClass());
            assertAppBarIconIdentity(vectorIcon, expectedIconNames[index], description);
            assertAppBarIconButtonGeometry(iconButton, graphic, vectorIcon, description);
        }
    }

    /// Verifies that an app bar icon button renders the expected logical SVG asset.
    private static void assertAppBarIconIdentity(Node vectorIcon, String expectedIconName, String description) {
        assertEquals(expectedIconName, vectorIcon.getProperties().get(DemoIcons.ICON_NAME_PROPERTY),
                () -> description + " icon button uses the wrong demo icon: expected=" + expectedIconName
                        + ", properties=" + vectorIcon.getProperties());
        SVGPath svgPath = assertInstanceOf(SVGPath.class, vectorIcon,
                () -> description + " app bar icon should be an SVGPath");
        assertEquals(DemoIcons.path(expectedIconName), svgPath.getContent(),
                () -> description + " app bar icon path does not match " + expectedIconName);
    }

    /// Returns the expected semantic icon color style for an app bar action slot.
    private static String expectedAppBarIconColorStyle(Node appBar, M3IconButton iconButton) {
        if (appBar instanceof M3TopAppBar topAppBar && topAppBar.getNavigation() == iconButton) {
            return DemoIcons.ON_SURFACE_STYLE_CLASS;
        }
        return DemoIcons.ON_SURFACE_VARIANT_STYLE_CLASS;
    }

    /// Verifies that a demo app bar icon button uses Material-sized centered vector geometry.
    private static void assertAppBarIconButtonGeometry(
            M3IconButton iconButton,
            Node graphic,
            Node vectorIcon,
            String description
    ) {
        Bounds buttonBounds = iconButton.localToScene(iconButton.getBoundsInLocal());
        Bounds graphicBounds = graphic.localToScene(graphic.getBoundsInLocal());
        Bounds vectorIconBounds = vectorIcon.localToScene(vectorIcon.getBoundsInLocal());
        @Nullable Parent viewport = nearestAncestorWithStyle(vectorIcon, "demo-vector-icon-viewport");
        assertNotNull(viewport,
                () -> description + " icon button SVG is not wrapped in the fixed demo icon viewport");
        Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());

        assertEquals(APP_BAR_ICON_BUTTON_SIZE, buttonBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon button width does not match the Material action target: "
                        + buttonBounds);
        assertEquals(APP_BAR_ICON_BUTTON_SIZE, buttonBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon button height does not match the Material action target: "
                        + buttonBounds);
        assertEquals(DEMO_ICON_VIEWPORT_SIZE, viewportBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon viewport width is not 24dp: " + viewportBounds);
        assertEquals(DEMO_ICON_VIEWPORT_SIZE, viewportBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon viewport height is not 24dp: " + viewportBounds);
        assertTrue(containsBoundsWithTolerance(buttonBounds, graphicBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " icon graphic leaves its button target: button="
                        + buttonBounds + ", graphic=" + graphicBounds);
        assertTrue(containsBoundsWithTolerance(viewportBounds, vectorIconBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " SVG path leaves its 24dp viewport: viewport="
                        + viewportBounds + ", path=" + vectorIconBounds);
        assertEquals(buttonBounds.getCenterX(), viewportBounds.getCenterX(), DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " icon viewport is horizontally off-center inside its action target: button="
                        + buttonBounds + ", viewport=" + viewportBounds);
        assertEquals(buttonBounds.getCenterY(), viewportBounds.getCenterY(), DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " icon viewport is vertically off-center inside its action target: button="
                        + buttonBounds + ", viewport=" + viewportBounds);
        assertAppBarIconHasVisiblePaint(vectorIcon, description);
    }

    /// Verifies that an app bar icon renders visible SVG pixels with contrast against its app bar container.
    private static void assertAppBarIconHasVisiblePaint(Node vectorIcon, String description) {
        assertNodeSnapshotHasOpaquePixels(vectorIcon, description + " icon");
        assertInstanceOf(SVGPath.class, vectorIcon,
                () -> description + " demo vector icon should be an SVG path");

        SVGPath svgPath = (SVGPath) vectorIcon;
        assertInstanceOf(Color.class, svgPath.getFill(),
                () -> description + " demo vector icon should resolve to a concrete fill color");
        Color iconColor = (Color) svgPath.getFill();
        assertTrue(iconColor.getOpacity() > 0.1,
                () -> description + " demo vector icon fill is transparent: " + iconColor);

        @Nullable Region appBar = nearestAppBarRegion(vectorIcon);
        assertNotNull(appBar, () -> description + " demo vector icon is not inside an app bar region");
        Color containerColor = Objects.requireNonNull(
                firstBackgroundColor(Objects.requireNonNull(appBar)),
                description + " app bar background color"
        );
        assertTrue(colorDistance(iconColor, containerColor) > 0.08,
                () -> description + " demo vector icon has too little contrast against the app bar background: icon="
                        + iconColor + ", background=" + containerColor);
    }

    /// Verifies that a node snapshot contains non-transparent rendered pixels.
    private static void assertNodeSnapshotHasOpaquePixels(Node node, String description) {
        WritableImage image = snapshotNode(node);
        int opaquePixels = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getPixelReader().getColor(x, y).getOpacity() > 0.1) {
                    opaquePixels++;
                }
            }
        }
        int minimumOpaquePixels = Math.max(4, (int) (image.getWidth() * image.getHeight() * 0.02));
        int finalOpaquePixels = opaquePixels;
        assertTrue(finalOpaquePixels >= minimumOpaquePixels,
                () -> description + " snapshot has too few opaque pixels: opaque="
                        + finalOpaquePixels + ", minimum=" + minimumOpaquePixels
                        + ", size=" + image.getWidth() + "x" + image.getHeight());
    }

    /// Returns the nearest top or bottom app bar region containing a node.
    private static @Nullable Region nearestAppBarRegion(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof Region region
                    && (region.getStyleClass().contains(M3TopAppBar.STYLE_CLASS)
                    || region.getStyleClass().contains(M3BottomAppBar.STYLE_CLASS))) {
                return region;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns the first concrete background color used by a region.
    private static @Nullable Color firstBackgroundColor(Region region) {
        if (region.getBackground() == null) {
            return null;
        }
        for (javafx.scene.layout.BackgroundFill fill : region.getBackground().getFills()) {
            Paint paint = fill.getFill();
            if (paint instanceof Color color) {
                return color;
            }
        }
        return null;
    }

    /// Verifies that a radio button dot shares the same rendered center as its ring.
    private static void assertRadioDotCentered(Node root, Bounds sceneBounds, String pageTitle) {
        @Nullable Node container = root.lookup(".m3-radio-ring");
        @Nullable Node indicator = root.lookup(".m3-radio-dot");
        if (container == null || indicator == null || !hasRenderableBounds(container) || !hasRenderableBounds(indicator)) {
            return;
        }
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        if (isOutsideSceneViewport(container, containerBounds, sceneBounds)) {
            return;
        }

        double dx = Math.abs(containerBounds.getCenterX() - indicatorBounds.getCenterX());
        double dy = Math.abs(containerBounds.getCenterY() - indicatorBounds.getCenterY());
        assertTrue(dx <= 0.75 && dy <= 0.75,
                () -> pageTitle + " radio dot is off-center: dx=" + dx + ", dy=" + dy
                        + ", containerBounds=" + containerBounds + ", indicatorBounds=" + indicatorBounds);
    }

    /// Verifies that a switch thumb stays vertically centered and inside its track.
    private static void assertSwitchThumbInsideTrack(Node root, Bounds sceneBounds, String pageTitle) {
        @Nullable Node track = root.lookup(".m3-switch-track");
        @Nullable Node thumb = root.lookup(".m3-switch-thumb");
        if (track == null || thumb == null || !hasRenderableBounds(track) || !hasRenderableBounds(thumb)) {
            return;
        }
        Bounds trackBounds = track.localToScene(track.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        if (isOutsideSceneViewport(track, trackBounds, sceneBounds)) {
            return;
        }

        double dy = Math.abs(trackBounds.getCenterY() - thumbBounds.getCenterY());
        assertTrue(dy <= 1.0
                        && thumbBounds.getMinX() >= trackBounds.getMinX() - 0.75
                        && thumbBounds.getMaxX() <= trackBounds.getMaxX() + 0.75,
                () -> pageTitle + " switch thumb has unsafe geometry: dy=" + dy
                        + ", trackBounds=" + trackBounds + ", thumbBounds=" + thumbBounds);
    }

    /// Verifies that a demo card uses a visible Material surface, variant treatment, and stable content bounds.
    private static void assertCardDemoGeometry(M3Card card) {
        Region container = assertInstanceOf(
                Region.class,
                requireVisibleStyledDescendant(card, "m3-card-container", "card container")
        );
        Bounds cardBounds = card.localToScene(card.getLayoutBounds());
        Bounds containerBounds = container.localToScene(container.getLayoutBounds());

        assertTrue(cardBounds.getWidth() >= 260.0,
                () -> "demo card is too narrow for meaningful content: " + cardBounds);
        assertTrue(cardBounds.getHeight() >= 150.0,
                () -> "demo card is too short for meaningful content: " + cardBounds);
        assertTrue(containsBoundsWithTolerance(cardBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> "card surface should stay inside the control bounds: card="
                        + cardBounds + ", container=" + containerBounds);
        assertNotNull(container.getBackground(), "card container should resolve a background");
        assertFalse(container.getBackground().getFills().isEmpty(),
                "card container should resolve a visible background fill");
        assertEquals(card.getContentPadding(), container.getPadding().getLeft(), 0.0001,
                "card container should apply the public content padding token");

        switch (card.getVariant()) {
            case ELEVATED -> assertNotNull(container.getEffect(), "elevated cards should render elevation");
            case OUTLINED -> {
                assertNotNull(container.getBorder(), "outlined cards should render an outline border");
                assertFalse(container.getBorder().getStrokes().isEmpty(),
                        "outlined cards should render an outline border");
            }
            case FILLED -> assertNull(container.getEffect(), "filled cards should not render elevation by default");
        }
    }

    /// Verifies that a demo dialog pane resolves Material surface, action, and content geometry.
    private static void assertDialogPaneDemoGeometry(Scene scene, M3DialogPane pane) {
        assertEquals(AccessibleRole.DIALOG, pane.getAccessibleRole());
        assertDialogPaneStaysCompact(scene, pane);
        assertNotNull(pane.getBackground(), "dialog pane should resolve a Material background");
        assertFalse(pane.getBackground().getFills().isEmpty(),
                "dialog pane should resolve a visible Material background fill");
        assertFalse(pane.getButtonTypes().isEmpty(), "demo dialog panes should expose action buttons");

        Bounds paneBounds = pane.localToScene(pane.getBoundsInLocal());
        Node buttonBar = requireVisibleStyledDescendant(
                pane,
                M3DialogPane.BUTTON_BAR_STYLE_CLASS,
                "dialog button bar"
        );
        Bounds buttonBarBounds = buttonBar.localToScene(buttonBar.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(paneBounds, buttonBarBounds, CONTROL_EDGE_TOLERANCE),
                () -> "dialog button bar should stay inside the pane: pane="
                        + paneBounds + ", buttonBar=" + buttonBarBounds);

        for (ButtonType buttonType : pane.getButtonTypes()) {
            Node action = Objects.requireNonNull(pane.lookupButton(buttonType), "dialog action button");
            M3Button materialAction = assertInstanceOf(M3Button.class, action);
            assertTrue(materialAction.getStyleClass().contains(M3DialogPane.BUTTON_STYLE_CLASS),
                    "dialog action should use the Material dialog button style class");
            Bounds actionBounds = materialAction.localToScene(materialAction.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(paneBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "dialog action button should stay inside the pane: pane="
                            + paneBounds + ", action=" + actionBounds);
        }

        @Nullable Node content = pane.getContent();
        if (content != null && content.isVisible() && hasRenderableBounds(content)) {
            Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(paneBounds, contentBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "dialog content should stay inside the pane: pane="
                            + paneBounds + ", content=" + contentBounds);
        }
    }

    /// Verifies that a demo banner uses visible Material surface and safe slot geometry.
    private static void assertBannerDemoGeometry(M3Banner banner) {
        Bounds bannerBounds = banner.localToScene(banner.getBoundsInLocal());
        assertTrue(bannerBounds.getWidth() >= 360.0,
                () -> "demo banner is too narrow for the showcased content: " + bannerBounds);
        assertTrue(bannerBounds.getHeight() >= 72.0,
                () -> "demo banner is too short for Material banner content: " + bannerBounds);
        assertNotNull(banner.getBackground(), "banner should resolve a Material background");
        assertFalse(banner.getBackground().getFills().isEmpty(), "banner should resolve a visible background fill");

        Node container = requireVisibleStyledDescendant(
                banner,
                M3Banner.CONTAINER_STYLE_CLASS,
                "banner container"
        );
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(bannerBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> "banner container should stay inside the control bounds: banner="
                        + bannerBounds + ", container=" + containerBounds);

        Node text = requireVisibleStyledDescendant(banner, M3Banner.TEXT_STYLE_CLASS, "banner text");
        Bounds textBounds = text.localToScene(text.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(bannerBounds, textBounds, CONTROL_EDGE_TOLERANCE),
                () -> "banner text should stay inside the control bounds: banner="
                        + bannerBounds + ", text=" + textBounds);

        @Nullable Node actions = firstVisibleStyledDescendant(banner, M3Banner.ACTIONS_STYLE_CLASS);
        if (!banner.getActions().isEmpty()) {
            assertNotNull(actions, "banner actions should be visible when action nodes are present");
            Bounds actionBounds = actions.localToScene(actions.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(bannerBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "banner actions should stay inside the control bounds: banner="
                            + bannerBounds + ", actions=" + actionBounds);
            for (Node action : banner.getActions()) {
                Bounds singleActionBounds = action.localToScene(action.getBoundsInLocal());
                assertTrue(containsBoundsWithTolerance(bannerBounds, singleActionBounds, CONTROL_EDGE_TOLERANCE),
                        () -> "banner action should stay inside the control bounds: banner="
                                + bannerBounds + ", action=" + singleActionBounds);
            }
        }

        @Nullable Node icon = firstVisibleStyledDescendant(banner, M3Banner.ICON_STYLE_CLASS);
        if (banner.getIcon() != null) {
            assertNotNull(icon, "banner icon slot should be visible when an icon is present");
            Bounds iconBounds = icon.localToScene(icon.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(bannerBounds, iconBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "banner icon should stay inside the control bounds: banner="
                            + bannerBounds + ", icon=" + iconBounds);
            if (banner.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT && actions != null) {
                Bounds actionBounds = actions.localToScene(actions.getBoundsInLocal());
                assertTrue(iconBounds.getMinX() > actionBounds.getMaxX(),
                        () -> "RTL banner should mirror icon and action slots: icon="
                                + iconBounds + ", actions=" + actionBounds);
            }
        }
    }

    /// Verifies that the expected text appears in the visible node tree.
    private static void assertVisibleText(Node root, String expectedText, String pageTitle) {
        assertTrue(visibleNodesOfType(root, Text.class).stream().anyMatch(text -> expectedText.equals(text.getText())),
                () -> pageTitle + " did not render visible text: " + expectedText);
    }

    /// Returns whether a node should have centered glyph content.
    private static boolean isCenteredTarget(Node node) {
        for (String styleClass : CENTERED_TARGET_STYLE_CLASSES) {
            if (node.getStyleClass().contains(styleClass)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a node is a demo-level Material control whose visible bounds should fit the viewport.
    private static boolean isPageLevelMaterialControl(Node node) {
        return node instanceof M3BottomAppBar
                || node instanceof M3Button
                || node instanceof M3Card
                || node instanceof M3DatePicker
                || node instanceof M3FloatingActionButton
                || node instanceof M3IconToggleButton
                || node instanceof M3LoadingIndicator
                || node instanceof M3PickerField<?, ?>
                || node instanceof M3RadioButton
                || node instanceof M3SegmentedButton
                || node instanceof M3SplitButton
                || node instanceof M3Switch
                || node instanceof M3TextField
                || node instanceof M3TextInputLayout
                || node instanceof M3TopAppBar;
    }

    /// Returns the first visible descendant with the requested style class.
    private static @Nullable Node firstVisibleNodeWithStyle(Node root, String styleClass) {
        if (root.isVisible() && root.getStyleClass().contains(styleClass) && hasRenderableBounds(root)) {
            return root;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node result = firstVisibleNodeWithStyle(child, styleClass);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns visible descendants assignable to the requested type.
    private static <T> List<T> visibleNodesOfType(Node root, Class<T> type) {
        List<T> result = new ArrayList<>();
        collectVisibleNodesOfType(root, type, result);
        return result;
    }

    /// Returns visible descendants with the requested style class sorted by their scene x coordinate.
    private static List<Node> visibleNodesWithStyle(Node root, String styleClass) {
        return root.lookupAll("." + styleClass).stream()
                .filter(node -> node.isVisible() && hasRenderableBounds(node))
                .sorted(java.util.Comparator.comparingDouble(node ->
                        node.localToScene(node.getBoundsInLocal()).getMinX()))
                .toList();
    }

    /// Adds visible descendants assignable to the requested type into a result list.
    private static <T> void collectVisibleNodesOfType(Node root, Class<T> type, List<T> result) {
        if (!root.isVisible()) {
            return;
        }
        if (type.isInstance(root) && hasRenderableBounds(root)) {
            result.add(type.cast(root));
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleNodesOfType(child, type, result);
            }
        }
    }

    /// Returns the nearest ancestor with the requested style class.
    private static @Nullable Parent nearestAncestorWithStyle(Node node, String styleClass) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains(styleClass)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns the nearest ancestor assignable to the requested type.
    private static <T> @Nullable T nearestAncestorOfType(Node node, Class<T> type) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (type.isInstance(parent)) {
                return type.cast(parent);
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns whether `candidate` is `root` or belongs to its descendant subtree.
    private static boolean isNodeOrDescendant(Node root, Node candidate) {
        Node node = candidate;
        while (true) {
            if (node == root) {
                return true;
            }
            @Nullable Parent parent = node.getParent();
            if (parent == null) {
                return false;
            }
            node = parent;
        }
    }

    /// Returns a visible descendant with the requested style class or fails the test.
    private static Node requireVisibleStyledDescendant(Node root, String styleClass, String description) {
        @Nullable Node node = firstVisibleStyledDescendant(root, styleClass);
        assertNotNull(node, description);
        return Objects.requireNonNull(node, description);
    }

    /// Returns the first visible descendant with the requested style class.
    private static @Nullable Node firstVisibleStyledDescendant(Node root, String styleClass) {
        if (root.isVisible() && root.getStyleClass().contains(styleClass) && hasRenderableBounds(root)) {
            return root;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node result = firstVisibleStyledDescendant(child, styleClass);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns whether a node bounds should be skipped because it is outside the visible scene or scroll viewport.
    private static boolean isOutsideSceneViewport(Node node, Bounds nodeBounds, Bounds sceneBounds) {
        if (!sceneBounds.intersects(nodeBounds)
                || !sceneBounds.contains(nodeBounds.getCenterX(), nodeBounds.getCenterY())) {
            return true;
        }
        @Nullable Node scrollViewport = nearestScrollViewport(node);
        if (scrollViewport == null) {
            return false;
        }
        Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
        return !viewportBounds.intersects(nodeBounds)
                || !viewportBounds.contains(nodeBounds.getCenterX(), nodeBounds.getCenterY());
    }

    /// Returns the first visible M3 button with the requested text.
    private static @Nullable M3Button firstVisibleButtonWithText(Node root, String text) {
        if (root instanceof M3Button button
                && button.isVisible()
                && text.equals(button.getText())
                && hasRenderableBounds(button)) {
            return button;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3Button result = firstVisibleButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 switch with the requested text.
    private static @Nullable M3Switch firstVisibleSwitchWithText(Node root, String text) {
        if (root instanceof M3Switch switchControl
                && switchControl.isVisible()
                && text.equals(switchControl.getText())
                && hasRenderableBounds(switchControl)) {
            return switchControl;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3Switch result = firstVisibleSwitchWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 split button with the requested text.
    private static @Nullable M3SplitButton firstVisibleSplitButtonWithText(Node root, String text) {
        if (root instanceof M3SplitButton splitButton
                && splitButton.isVisible()
                && text.equals(splitButton.getText())
                && hasRenderableBounds(splitButton)) {
            return splitButton;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SplitButton result = firstVisibleSplitButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 menu button with the requested text.
    private static @Nullable M3MenuButton firstVisibleMenuButtonWithText(Node root, String text) {
        if (root instanceof M3MenuButton menuButton
                && menuButton.isVisible()
                && text.equals(menuButton.getText())
                && hasRenderableBounds(menuButton)) {
            return menuButton;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3MenuButton result = firstVisibleMenuButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 date picker field.
    private static @Nullable M3DatePickerField firstVisibleDatePickerField(Node root) {
        if (root instanceof M3DatePickerField field && field.isVisible() && hasRenderableBounds(field)) {
            return field;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3DatePickerField result = firstVisibleDatePickerField(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 submenu item with the requested text.
    private static @Nullable M3SubMenuItem firstVisibleSubMenuItemWithText(Node root, String text) {
        if (root instanceof M3SubMenuItem subMenuItem
                && subMenuItem.isVisible()
                && text.equals(subMenuItem.getHeadlineText())
                && hasRenderableBounds(subMenuItem)) {
            return subMenuItem;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SubMenuItem result = firstVisibleSubMenuItemWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 navigation item with the requested text.
    private static @Nullable M3NavigationItem firstVisibleNavigationItemWithText(Node root, String text) {
        if (root instanceof M3NavigationItem item
                && item.isVisible()
                && text.equals(item.getText())
                && hasRenderableBounds(item)) {
            return item;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3NavigationItem result = firstVisibleNavigationItemWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible navigation drawer group with the requested title.
    private static @Nullable M3NavigationDrawerGroup firstVisibleDrawerGroupWithTitle(Node root, String title) {
        if (root instanceof M3NavigationDrawerGroup group
                && group.isVisible()
                && title.equals(group.getTitle())
                && hasRenderableBounds(group)) {
            return group;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3NavigationDrawerGroup result = firstVisibleDrawerGroupWithTitle(child, title);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible bottom sheet with the requested headline.
    private static @Nullable M3BottomSheet firstVisibleBottomSheetWithHeadline(Node root, String headline) {
        if (root instanceof M3BottomSheet sheet
                && sheet.isVisible()
                && headline.equals(sheet.getHeadline())
                && hasRenderableBounds(sheet)) {
            return sheet;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3BottomSheet result = firstVisibleBottomSheetWithHeadline(child, headline);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible side sheet with the requested headline.
    private static @Nullable M3SideSheet firstVisibleSideSheetWithHeadline(Node root, String headline) {
        if (root instanceof M3SideSheet sheet
                && sheet.isVisible()
                && headline.equals(sheet.getHeadline())
                && hasRenderableBounds(sheet)) {
            return sheet;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SideSheet result = firstVisibleSideSheetWithHeadline(child, headline);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible snackbar host.
    private static @Nullable M3SnackbarHost firstVisibleSnackbarHost(Node root) {
        if (root instanceof M3SnackbarHost host && host.isVisible() && hasRenderableBounds(host)) {
            return host;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SnackbarHost result = firstVisibleSnackbarHost(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible FAB menu with the requested expanded state.
    private static @Nullable M3FabMenu firstVisibleFabMenu(Node root, boolean expanded) {
        if (root instanceof M3FabMenu menu
                && menu.isVisible()
                && menu.isExpanded() == expanded
                && hasRenderableBounds(menu)) {
            return menu;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3FabMenu result = firstVisibleFabMenu(child, expanded);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the popup root that hosts a picker field's popup picker.
    private static @Nullable Node pickerPopupRoot(M3DatePickerField field) {
        @Nullable Parent parent = field.getPicker().getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains(M3PickerField.POPUP_STYLE_CLASS)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Applies CSS and layout to a popup root before capturing it.
    private static void layoutPopupRoot(Node popupRoot) {
        popupRoot.applyCss();
        if (popupRoot instanceof Parent parent) {
            parent.layout();
        }
    }

    /// Returns whether a popup root has reached its fully shown visual state.
    private static boolean popupRootSettled(@Nullable Node popupRoot) {
        if (popupRoot == null
                || popupRoot.getScene() == null
                || !popupRoot.isVisible()
                || !hasRenderableBounds(popupRoot)) {
            return false;
        }

        layoutPopupRoot(popupRoot);
        @Nullable Bounds screenBounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        return screenBounds != null
                && screenBounds.getWidth() > 0.0
                && screenBounds.getHeight() > 0.0
                && popupRoot.getOpacity() >= 0.995
                && Math.abs(popupRoot.getScaleX() - 1.0) <= 0.005
                && Math.abs(popupRoot.getScaleY() - 1.0) <= 0.005
                && Math.abs(popupRoot.getTranslateX()) <= CONTROL_EDGE_TOLERANCE
                && Math.abs(popupRoot.getTranslateY()) <= CONTROL_EDGE_TOLERANCE;
    }

    /// Returns whether a bottom sheet has reached its fully hidden state.
    private static boolean bottomSheetHidden(@Nullable M3BottomSheet sheet) {
        return sheet != null && !sheet.isShown() && !sheet.isVisible() && !sheet.isManaged();
    }

    /// Returns whether a bottom sheet has reached its fully shown state.
    private static boolean bottomSheetShown(@Nullable M3BottomSheet sheet) {
        return sheet != null
                && sheet.isShown()
                && sheet.isVisible()
                && sheet.isManaged()
                && sheet.getOpacity() >= 0.995
                && Math.abs(sheet.getTranslateY()) <= CONTROL_EDGE_TOLERANCE
                && hasRenderableBounds(sheet);
    }

    /// Returns whether a side sheet has reached its fully hidden state.
    private static boolean sideSheetHidden(@Nullable M3SideSheet sheet) {
        return sheet != null && !sheet.isShown() && !sheet.isVisible() && !sheet.isManaged();
    }

    /// Returns whether a side sheet has reached its fully shown state.
    private static boolean sideSheetShown(@Nullable M3SideSheet sheet) {
        return sheet != null
                && sheet.isShown()
                && sheet.isVisible()
                && sheet.isManaged()
                && sheet.getOpacity() >= 0.995
                && Math.abs(sheet.getTranslateX()) <= CONTROL_EDGE_TOLERANCE
                && hasRenderableBounds(sheet);
    }

    /// Returns whether a snackbar host has reached its fully shown state.
    private static boolean snackbarSettled(@Nullable M3SnackbarHost host) {
        if (host == null || !host.isShowing()) {
            return false;
        }

        @Nullable M3Snackbar snackbar = host.getSnackbar();
        return snackbar != null
                && snackbar.isVisible()
                && snackbar.isManaged()
                && snackbar.getOpacity() >= 0.995
                && Math.abs(snackbar.getTranslateY()) <= CONTROL_EDGE_TOLERANCE
                && hasRenderableBounds(snackbar);
    }

    /// Returns whether a snackbar host has reached its fully hidden state.
    private static boolean snackbarHidden(@Nullable M3SnackbarHost host) {
        return host != null && !host.isShowing() && host.getSnackbar() == null;
    }

    /// Returns whether a FAB menu has reached its fully expanded visual state.
    private static boolean fabMenuExpandedSettled(@Nullable M3FabMenu menu) {
        if (menu == null || !menu.isExpanded()) {
            return false;
        }

        for (Node item : menu.getItems()) {
            if (!item.isVisible()
                    || !item.isManaged()
                    || item.getOpacity() < 0.995
                    || Math.abs(item.getScaleX() - 1.0) > 0.005
                    || Math.abs(item.getScaleY() - 1.0) > 0.005
                    || Math.abs(item.getTranslateY()) > CONTROL_EDGE_TOLERANCE) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether a FAB menu has reached its fully collapsed visual state.
    private static boolean fabMenuCollapsedSettled(@Nullable M3FabMenu menu) {
        if (menu == null || menu.isExpanded()) {
            return false;
        }

        for (Node item : menu.getItems()) {
            if (item.isVisible() || item.isManaged()) {
                return false;
            }
        }
        return true;
    }

    /// Verifies that an owning popup and nested popup are positioned beside each other on screen.
    private static void assertPopupStackSideBySide(Node ownerPopupRoot, Node childPopupRoot) {
        @Nullable Bounds ownerBounds = ownerPopupRoot.localToScreen(ownerPopupRoot.getBoundsInLocal());
        @Nullable Bounds childBounds = childPopupRoot.localToScreen(childPopupRoot.getBoundsInLocal());
        assertNotNull(ownerBounds, "owner popup screen bounds");
        assertNotNull(childBounds, "child popup screen bounds");

        double horizontalOverlap = Math.max(
                0.0,
                Math.min(ownerBounds.getMaxX(), childBounds.getMaxX())
                        - Math.max(ownerBounds.getMinX(), childBounds.getMinX())
        );
        double verticalOverlap = Math.max(
                0.0,
                Math.min(ownerBounds.getMaxY(), childBounds.getMaxY())
                        - Math.max(ownerBounds.getMinY(), childBounds.getMinY())
        );
        double maximumHorizontalOverlap = Math.max(24.0, Math.min(ownerBounds.getWidth(), childBounds.getWidth()) * 0.16);
        double minimumVerticalOverlap = Math.min(ownerBounds.getHeight(), childBounds.getHeight()) * 0.25;
        assertTrue(horizontalOverlap <= maximumHorizontalOverlap && verticalOverlap >= minimumVerticalOverlap,
                () -> "Nested popup stack has unsafe placement: ownerBounds=" + ownerBounds
                        + ", childBounds=" + childBounds + ", horizontalOverlap=" + horizontalOverlap
                        + ", verticalOverlap=" + verticalOverlap);
    }

    /// Verifies that popup roots keep the same installed demo theme context as the owner scene root.
    private static void assertPopupThemeContext(Parent sceneRoot, Parent popupRoot, String description) {
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS),
                () -> description + " popup root does not have the M3 theme root style class: "
                        + popupRoot.getStyleClass());
        assertSameThemeModeStyleClass(
                sceneRoot,
                popupRoot,
                M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS,
                M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS,
                description + " profile"
        );
        assertSameThemeModeStyleClass(
                sceneRoot,
                popupRoot,
                M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS,
                M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS,
                description + " brightness"
        );
    }

    /// Verifies that exactly the active owner theme mode class is present on a popup root.
    private static void assertSameThemeModeStyleClass(
            Parent sceneRoot,
            Parent popupRoot,
            String firstStyleClass,
            String secondStyleClass,
            String description
    ) {
        boolean sceneHasFirst = sceneRoot.getStyleClass().contains(firstStyleClass);
        boolean sceneHasSecond = sceneRoot.getStyleClass().contains(secondStyleClass);
        boolean popupHasFirst = popupRoot.getStyleClass().contains(firstStyleClass);
        boolean popupHasSecond = popupRoot.getStyleClass().contains(secondStyleClass);
        assertEquals(sceneHasFirst, popupHasFirst,
                () -> description + " first style class mismatch: scene=" + sceneRoot.getStyleClass()
                        + ", popup=" + popupRoot.getStyleClass());
        assertEquals(sceneHasSecond, popupHasSecond,
                () -> description + " second style class mismatch: scene=" + sceneRoot.getStyleClass()
                        + ", popup=" + popupRoot.getStyleClass());
    }

    /// Verifies that a popup menu root remains a compact menu surface.
    private static void assertPopupSurfaceSize(Node popupRoot, String description) {
        @Nullable Bounds bounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        assertNotNull(bounds, () -> description + " popup screen bounds");
        assertTrue(bounds.getWidth() >= 112.0 && bounds.getWidth() <= 360.0,
                () -> description + " popup width is not menu-like: bounds=" + bounds);
        assertTrue(bounds.getHeight() >= 48.0 && bounds.getHeight() <= 520.0,
                () -> description + " popup height is not menu-like: bounds=" + bounds);
    }

    /// Verifies that a snackbar occupies its compact message surface instead of the whole overlay.
    private static void assertSnackbarStaysCompact(Scene scene, M3Snackbar snackbar) {
        Bounds bounds = snackbar.localToScene(snackbar.getBoundsInLocal());
        assertTrue(bounds.getWidth() >= 160.0 && bounds.getWidth() <= scene.getWidth() * 0.75,
                () -> "Snackbar width is not compact: bounds=" + bounds + ", sceneWidth=" + scene.getWidth());
        assertTrue(bounds.getHeight() >= 40.0 && bounds.getHeight() <= 96.0,
                () -> "Snackbar height is not compact: bounds=" + bounds);
    }

    /// Verifies that a tooltip popup appears near its owner and stays at tooltip scale.
    private static void assertTooltipNearOwner(Node owner, Node popupRoot) {
        @Nullable Bounds ownerBounds = owner.localToScreen(owner.getBoundsInLocal());
        @Nullable Bounds popupBounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        assertNotNull(ownerBounds, "tooltip owner screen bounds");
        assertNotNull(popupBounds, "tooltip popup screen bounds");
        assertTrue(popupBounds.getMinY() >= ownerBounds.getMaxY() - 2.0,
                () -> "Tooltip popup is not below its owner: ownerBounds=" + ownerBounds
                        + ", popupBounds=" + popupBounds);
        assertTrue(popupBounds.getWidth() <= 420.0 && popupBounds.getHeight() <= 220.0,
                () -> "Tooltip popup is not compact: popupBounds=" + popupBounds);
    }

    /// Verifies that a rich tooltip action button is fully contained by the popup root.
    private static void assertRichTooltipActionInsidePopup(Node popupRoot) {
        M3Button action = Objects.requireNonNull(firstVisibleButtonWithText(popupRoot, "Open"), "rich tooltip action");
        Bounds popupBounds = popupRoot.localToScene(popupRoot.getBoundsInLocal());
        Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(popupBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                () -> "Rich tooltip action is clipped: popupBounds=" + popupBounds
                        + ", actionBounds=" + actionBounds);
        assertTrue(popupBounds.getMaxY() - actionBounds.getMaxY() >= 4.0,
                () -> "Rich tooltip action has no safe bottom padding: popupBounds=" + popupBounds
                        + ", actionBounds=" + actionBounds);
    }

    /// Verifies that a dialog pane remains a compact dialog surface.
    private static void assertDialogPaneStaysCompact(Scene ownerScene, Node dialogPane) {
        Bounds bounds = dialogPane.getBoundsInLocal();
        assertTrue(bounds.getWidth() >= 280.0 && bounds.getWidth() <= ownerScene.getWidth() * 0.70,
                () -> "Dialog pane width is not compact: bounds=" + bounds
                        + ", ownerWidth=" + ownerScene.getWidth());
        assertTrue(bounds.getHeight() >= 120.0 && bounds.getHeight() <= ownerScene.getHeight() * 0.70,
                () -> "Dialog pane height is not compact: bounds=" + bounds
                        + ", ownerHeight=" + ownerScene.getHeight());
    }

    /// Verifies that expanded FAB menu action items remain within the owning demo showcase surface.
    private static void assertFabMenuActionsStayInsideShowcase(M3FabMenu menu) {
        Node showcase = Objects.requireNonNull(
                nearestDemoFlowAncestor(menu),
                "FAB menu showcase flow"
        );
        Bounds showcaseBounds = showcase.localToScene(showcase.getBoundsInLocal());
        for (Node item : menu.getItems()) {
            if (!item.isVisible() || !hasRenderableBounds(item)) {
                continue;
            }
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(showcaseBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "FAB menu action item escaped its showcase: showcaseBounds=" + showcaseBounds
                            + ", itemBounds=" + itemBounds);
        }
    }

    /// Verifies that an interaction visibly changes the snapshot region occupied by a node.
    private static void assertNodeAreaChanged(Node node, WritableImage before, WritableImage after, String description) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int changedPixels = countNodeAreaChangedPixels(bounds, before, after);
        int minimumChangedPixels = minimumNodeAreaChangedPixels(bounds, before, after);
        int finalChangedPixels = changedPixels;
        assertTrue(finalChangedPixels >= minimumChangedPixels,
                () -> description + " produced too little visual change: changed="
                        + finalChangedPixels + ", minimum=" + minimumChangedPixels + ", bounds=" + bounds);
    }

    /// Verifies that ripple opacity reaches a visible release frame and then fades to transparency.
    private static void assertRippleOpacityFaded(double releaseOpacity, double settledOpacity, String description) {
        assertTrue(releaseOpacity > 0.02,
                () -> description + " release opacity was not visible: " + releaseOpacity);
        assertTrue(settledOpacity <= 0.001,
                () -> description + " settled opacity did not clear: " + settledOpacity);
        assertTrue(settledOpacity < releaseOpacity,
                () -> description + " opacity did not decrease: release="
                        + releaseOpacity + ", settled=" + settledOpacity);
    }

    /// Verifies that two standalone snapshots differ by more than antialiasing noise.
    private static void assertSnapshotChanged(WritableImage before, WritableImage after, String description) {
        int changedPixels = countSnapshotChangedPixels(before, after);
        int minimumChangedPixels = minimumSnapshotChangedPixels(before, after);
        int finalChangedPixels = changedPixels;
        assertTrue(finalChangedPixels >= minimumChangedPixels,
                () -> description + " produced too little visual change: changed="
                        + finalChangedPixels + ", minimum=" + minimumChangedPixels
                        + ", before=" + before.getWidth() + "x" + before.getHeight()
                        + ", after=" + after.getWidth() + "x" + after.getHeight());
    }

    /// Captures a scene frame when the snapshot area occupied by a node changes enough from a baseline.
    private static boolean captureSceneFrameWithChangedNodeArea(
            Node node,
            WritableImage baseline,
            Scene scene,
            AtomicReference<@Nullable WritableImage> frameReference
    ) {
        WritableImage frame = snapshot(scene);
        if (!nodeAreaChangedEnough(node, baseline, frame)) {
            return false;
        }

        frameReference.set(frame);
        return true;
    }

    /// Captures a node frame when it changes enough from a baseline snapshot.
    private static boolean captureNodeFrameChangedFrom(
            Node node,
            WritableImage baseline,
            AtomicReference<@Nullable WritableImage> frameReference
    ) {
        layoutPopupRoot(node);
        WritableImage frame = snapshotNode(node);
        if (!snapshotChangedEnough(baseline, frame)) {
            return false;
        }

        frameReference.set(frame);
        return true;
    }

    /// Returns whether the snapshot area occupied by a node changed enough between two scene snapshots.
    private static boolean nodeAreaChangedEnough(Node node, WritableImage before, WritableImage after) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return countNodeAreaChangedPixels(bounds, before, after) >= minimumNodeAreaChangedPixels(bounds, before, after);
    }

    /// Counts changed pixels within the snapshot area occupied by a node.
    private static int countNodeAreaChangedPixels(Bounds bounds, WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        int minX = clampPixel(Math.floor(bounds.getMinX()), width);
        int minY = clampPixel(Math.floor(bounds.getMinY()), height);
        int maxX = clampPixel(Math.ceil(bounds.getMaxX()), width);
        int maxY = clampPixel(Math.ceil(bounds.getMaxY()), height);
        int changedPixels = 0;
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (colorDistance(before.getPixelReader().getColor(x, y), after.getPixelReader().getColor(x, y)) > 0.02) {
                    changedPixels++;
                }
            }
        }
        return changedPixels;
    }

    /// Returns the minimum changed pixel count for a node-area visual change assertion.
    private static int minimumNodeAreaChangedPixels(Bounds bounds, WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        int minX = clampPixel(Math.floor(bounds.getMinX()), width);
        int minY = clampPixel(Math.floor(bounds.getMinY()), height);
        int maxX = clampPixel(Math.ceil(bounds.getMaxX()), width);
        int maxY = clampPixel(Math.ceil(bounds.getMaxY()), height);
        return minimumChangedPixels(Math.max(1, (maxX - minX) * (maxY - minY)));
    }

    /// Returns whether two standalone snapshots changed enough to represent visible motion.
    private static boolean snapshotChangedEnough(WritableImage before, WritableImage after) {
        return countSnapshotChangedPixels(before, after) >= minimumSnapshotChangedPixels(before, after);
    }

    /// Counts changed pixels between two standalone snapshots.
    private static int countSnapshotChangedPixels(WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        int changedPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixelDistance(before.getPixelReader().getColor(x, y), after.getPixelReader().getColor(x, y)) > 0.02) {
                    changedPixels++;
                }
            }
        }
        return changedPixels;
    }

    /// Returns the minimum changed pixel count for a standalone snapshot visual change assertion.
    private static int minimumSnapshotChangedPixels(WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        return minimumChangedPixels(Math.max(1, width * height));
    }

    /// Returns the minimum changed pixel count for a visual change assertion.
    private static int minimumChangedPixels(int totalPixels) {
        return Math.max(8, totalPixels / 250);
    }

    /// Writes a named interaction-state visual snapshot to the build report directory.
    private static void writeInteractionSnapshot(WritableImage image, String targetName, String stateName) {
        writeVisualSnapshot(image, Path.of(
                "build",
                "reports",
                "m3fx-demo-visual",
                "interaction-" + targetName + "-" + stateName + ".png"
        ));
    }

    /// Writes a named animation-frame visual snapshot to the build report directory.
    private static void writeAnimationSnapshot(WritableImage image, String targetName, String stateName) {
        writeVisualSnapshot(image, Path.of(
                "build",
                "reports",
                "m3fx-demo-visual",
                "animation-" + targetName + "-" + stateName + ".png"
        ));
    }

    /// Clamps a floating point coordinate to a valid snapshot pixel coordinate.
    private static int clampPixel(double coordinate, double size) {
        return Math.max(0, Math.min((int) Math.ceil(size), (int) coordinate));
    }

    /// Applies the hover pseudo-class to a node before rendering an interaction state.
    private static void applyHoverPseudoState(Node node) {
        node.pseudoClassStateChanged(HOVER_PSEUDO_CLASS, true);
    }

    /// Clears the hover pseudo-class from a node after rendering an interaction state.
    private static void clearHoverPseudoState(Node node) {
        node.pseudoClassStateChanged(HOVER_PSEUDO_CLASS, false);
    }

    /// Fires a primary-button mouse event at the center of a node.
    private static void firePrimaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            boolean primaryButtonDown
    ) {
        Bounds bounds = node.getBoundsInLocal();
        double x = bounds.getMinX() + bounds.getWidth() / 2.0;
        double y = bounds.getMinY() + bounds.getHeight() / 2.0;
        node.fireEvent(new MouseEvent(
                eventType,
                x,
                y,
                x,
                y,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(node, x, y)
        ));
    }

    /// Creates one keyboard event for synthetic demo keyboard traversal checks.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }

    /// Returns the first visible text node below a target node.
    private static @Nullable Text firstVisibleText(Node node) {
        if (node instanceof Text text && !text.getText().isBlank() && hasRenderableBounds(text)) {
            return text;
        }
        if (node instanceof M3Icon icon) {
            @Nullable Node iconText = icon.lookup(".text");
            if (iconText instanceof Text text && !text.getText().isBlank() && hasRenderableBounds(text)) {
                return text;
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Text text = firstVisibleText(child);
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    /// Returns the first visible demo SVG icon below a target node.
    private static @Nullable Node firstVisibleDemoVectorIcon(Node node) {
        if (node instanceof SVGPath
                && node.getStyleClass().contains(DemoIcons.STYLE_CLASS)
                && hasRenderableBounds(node)) {
            return node;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node icon = firstVisibleDemoVectorIcon(child);
                if (icon != null) {
                    return icon;
                }
            }
        }
        return null;
    }

    /// Returns whether a node has visible non-empty bounds.
    private static boolean hasRenderableBounds(Node node) {
        Bounds bounds = node.getBoundsInLocal();
        return bounds.getWidth() > 0.5 && bounds.getHeight() > 0.5;
    }

    /// Returns whether `outer` fully contains `inner` after applying a small edge tolerance.
    private static boolean containsBoundsWithTolerance(Bounds outer, Bounds inner, double tolerance) {
        return inner.getMinX() >= outer.getMinX() - tolerance
                && inner.getMinY() >= outer.getMinY() - tolerance
                && inner.getMaxX() <= outer.getMaxX() + tolerance
                && inner.getMaxY() <= outer.getMaxY() + tolerance;
    }

    /// Returns whether `outer` contains the horizontal span of `inner` after applying a small edge tolerance.
    private static boolean containsHorizontalBoundsWithTolerance(Bounds outer, Bounds inner, double tolerance) {
        return inner.getMinX() >= outer.getMinX() - tolerance
                && inner.getMaxX() <= outer.getMaxX() + tolerance;
    }

    /// Returns whether a node bounds touches a scroll viewport edge where partial vertical visibility is expected.
    private static boolean touchesVerticalViewportEdge(Bounds inner, Bounds viewport, double tolerance) {
        return inner.getMinY() < viewport.getMinY() + tolerance
                || inner.getMaxY() > viewport.getMaxY() - tolerance;
    }

    /// Returns the nearest demo flow ancestor.
    private static @Nullable Node nearestDemoFlowAncestor(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains("demo-flow")) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Visits visible descendants in a rendered hierarchy.
    private static void visitVisibleNodes(Node node, Consumer<Node> visitor) {
        if (!node.isVisible()) {
            return;
        }

        visitor.accept(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                visitVisibleNodes(child, visitor);
            }
        }
    }

    /// Writes a visual snapshot to the build report directory.
    private static void writeVisualSnapshot(WritableImage image, Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ImageIO.write(toBufferedImage(image), "png", path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Converts a JavaFX image into a desktop image.
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        return bufferedImage;
    }

    /// Returns a filesystem-safe snapshot file name.
    private static String snapshotFileName(String pageTitle) {
        return pageTitle.toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    /// Returns a simple RGB distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }

    /// Returns an RGBA distance between two pixels.
    private static double pixelDistance(Color first, Color second) {
        return colorDistance(first, second) + Math.abs(first.getOpacity() - second.getOpacity());
    }

    /// Runs a task on the JavaFX application thread and propagates failures.
    private static void runOnFxThread(Runnable task) {
        DemoFxTestUtils.runOnFxThread(task);
    }

    /// Runs setup on the FX thread and verifies the result after JavaFX pulses.
    private static void runOnFxThreadAfterPulses(
            int pulseCount,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        DemoFxTestUtils.runOnFxThreadAfterPulses(pulseCount, setup, verification);
    }

    /// Runs setup on the FX thread and verifies the result when a condition becomes true.
    private static void runOnFxThreadWhen(
            BooleanSupplier condition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        DemoFxTestUtils.runOnFxThreadWhen(condition, setup, verification);
    }

    /// Runs setup on the FX thread and verifies the result after a condition stays true for pulses.
    private static void runOnFxThreadWhenStable(
            BooleanSupplier condition,
            int stablePulseCount,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        DemoFxTestUtils.runOnFxThreadWhenStable(condition, stablePulseCount, setup, verification);
    }

    /// Runs setup and verifies after a referenced node area visibly changes from a baseline snapshot.
    private static <T extends Node> void runOnFxThreadWhenNodeAreaChanged(
            AtomicReference<@Nullable T> nodeReference,
            AtomicReference<@Nullable WritableImage> baselineReference,
            AtomicReference<@Nullable Scene> sceneReference,
            AtomicReference<@Nullable WritableImage> frameReference,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        runOnFxThreadWhen(() -> {
            @Nullable Node node = nodeReference.get();
            @Nullable WritableImage baseline = baselineReference.get();
            @Nullable Scene scene = sceneReference.get();
            return node != null
                    && baseline != null
                    && scene != null
                    && captureSceneFrameWithChangedNodeArea(node, baseline, scene, frameReference);
        }, setup, verification);
    }

    /// Runs setup and verifies after a referenced node area stops visibly changing between pulses.
    private static <T extends Node> void runOnFxThreadWhenNodeAreaStable(
            AtomicReference<@Nullable T> nodeReference,
            AtomicReference<@Nullable Scene> sceneReference,
            AtomicReference<@Nullable WritableImage> frameReference,
            BooleanSupplier additionalCondition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<@Nullable WritableImage> previousFrameReference = new AtomicReference<>();
        runOnFxThreadWhenStable(() -> {
            @Nullable Node node = nodeReference.get();
            @Nullable Scene scene = sceneReference.get();
            if (node == null || scene == null) {
                return false;
            }
            if (!additionalCondition.getAsBoolean()) {
                return false;
            }

            WritableImage frame = snapshot(scene);
            @Nullable WritableImage previousFrame = previousFrameReference.get();
            previousFrameReference.set(frame);
            frameReference.set(frame);
            if (previousFrame == null) {
                return false;
            }
            return !nodeAreaChangedEnough(node, previousFrame, frame);
        }, SETTLED_STATE_PULSES, setup, verification);
    }
}
