// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3SVGIcon;
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
                assertHome(scene, app);
                assertAdaptiveGrid(scene, stage);
                assertComponentAndExampleNavigation(scene, app);
                assertThemeSettings(scene, app);
                assertExpandedComponentCoverage(scene, app);
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

    /// Verifies registry size, ordering, uniqueness, and required links.
    ///
    /// @param components the Catalog registry
    private static void assertRegistry(List<CatalogComponent> components) {
        assertEquals(38, components.size());
        Set<String> names = new HashSet<>();
        String previous = "";
        for (CatalogComponent component : components) {
            assertTrue(names.add(component.name()), () -> "duplicate component: " + component.name());
            assertTrue(previous.compareToIgnoreCase(component.name()) <= 0, () -> "registry is not alphabetical");
            assertTrue(component.guidelinesUrl().startsWith("https://m3.material.io/"));
            assertTrue(component.docsUrl().startsWith("https://"));
            assertTrue(component.sourceUrl().startsWith("https://github.com/Glavo/m3fx/"));
            assertFalse(component.examples().isEmpty());
            previous = component.name();
        }
        assertTrue(names.containsAll(Set.of("Dividers", "Side sheets", "Search", "Date pickers")));
        assertEquals(2, componentNamed(components, "Date pickers").examples().size());
        assertEquals(2, componentNamed(components, "Lists").examples().size());
        assertEquals(2, componentNamed(components, "Search").examples().size());
        assertEquals(2, componentNamed(components, "Side sheets").examples().size());
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
            M3SVGIcon firstIcon = assertInstanceOf(
                    M3SVGIcon.class,
                    Objects.requireNonNull(firstCard.lookup(".catalog-component-card-icon"), "component icon")
            );
            assertEquals(new Rectangle2D(0.0, 0.0, 24.0, 24.0), firstIcon.getViewBox());
            assertEquals(80.0, firstIcon.getIconSize(), 0.01);
            assertTrue(scene.getRoot().lookupAll(".catalog-icon").stream()
                    .allMatch(M3SVGIcon.class::isInstance));
            assertNotNull(scene.lookup(".catalog-top-app-bar"));
            assertNull(scene.lookup(".catalog-navigation-drawer"));
            assertNull(scene.lookup(".catalog-navigation-rail"));
            assertNull(scene.lookup(".catalog-navigation-bar"));
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
            assertTrue(componentPage.getLayoutBounds().getWidth() >= scene.getWidth() - 40.0);
            assertEquals(4, scene.getRoot().lookupAll(".catalog-example-card").size());

            CatalogExample example = chips.examples().get(0);
            app.navigate(new CatalogRoute.Example(chips, example));
            layout(scene);
            assertInstanceOf(CatalogRoute.Example.class, app.currentRoute());
            assertNotNull(scene.lookup(".catalog-example-page"));
            assertNull(scene.lookup(".catalog-sample-surface"));

            app.navigateBack();
            assertInstanceOf(CatalogRoute.Component.class, app.currentRoute());
            app.navigateBack();
            assertInstanceOf(CatalogRoute.Home.class, app.currentRoute());
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
            app.navigate(new CatalogRoute.Example(datePickers, datePickers.examples().get(1)));
            layout(scene);
            M3DateRangePicker dateRangePicker = assertInstanceOf(
                    M3DateRangePicker.class,
                    Objects.requireNonNull(scene.lookup(".m3-date-range-picker"), "date range picker")
            );
            assertTrue(dateRangePicker.getWidth() <= 420.5);
            assertTrue(dateRangePicker.getHeight() < scene.getHeight() - 160.0);

            CatalogComponent search = componentNamed(app.components(), "Search");
            app.navigate(new CatalogRoute.Example(search, search.examples().get(1)));
            layout(scene);
            M3SearchView searchView = assertInstanceOf(
                    M3SearchView.class,
                    Objects.requireNonNull(scene.lookup(".m3-search-view"), "search view")
            );
            assertEquals(3, searchView.getResults().size());
            Node styleSelector = Objects.requireNonNull(
                    scene.lookup(".m3-segmented-button-group"),
                    "search-view style selector"
            );
            assertEquals(
                    searchView.localToScene(searchView.getBoundsInLocal()).getCenterX(),
                    styleSelector.localToScene(styleSelector.getBoundsInLocal()).getCenterX(),
                    0.5
            );

            CatalogComponent sideSheets = componentNamed(app.components(), "Side sheets");
            app.navigate(new CatalogRoute.Example(sideSheets, sideSheets.examples().get(1)));
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
        });
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
                    assertFalse(((javafx.scene.Parent) page).getChildrenUnmodifiable().isEmpty());
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

    /// Completes CSS and layout before querying scene-graph geometry.
    ///
    /// @param scene the Catalog scene
    private static void layout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }
}
