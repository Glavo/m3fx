// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertEquals(36, components.size());
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
    }

    /// Verifies the alphabetical grid and absence of the former destination shell.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
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
    /// @param app the running Catalog application
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
    /// @param app the running Catalog application
    private static void assertThemeSettings(Scene scene, M3FXCatalogApp app) throws InterruptedException {
        FxTestUtils.runOnFxThread(() -> {
            M3BottomSheet sheet = assertInstanceOf(
                    M3BottomSheet.class,
                    Objects.requireNonNull(scene.lookup(".catalog-settings-sheet"), "settings sheet")
            );
            M3Scrim scrim = assertInstanceOf(
                    M3Scrim.class,
                    Objects.requireNonNull(scene.lookup(".catalog-settings-scrim"), "settings scrim")
            );
            assertFalse(sheet.isShown());
            assertFalse(scrim.isShown());
            app.showSettings();
            layout(scene);
            assertTrue(sheet.isShown());
            assertTrue(scrim.isShown());
            assertTrue(sheet.getHeight() <= 680.5);
            assertTrue(sheet.localToScene(sheet.getBoundsInLocal()).getMinY() >= 15.0);
            app.hideSettings();
            assertFalse(sheet.isShown());
            assertFalse(scrim.isShown());
        });
    }

    /// Creates and lays out every registered example in the running application.
    ///
    /// @param scene the Catalog scene
    /// @param app the running Catalog application
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

    /// Completes CSS and layout before querying scene-graph geometry.
    ///
    /// @param scene the Catalog scene
    private static void layout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }
}
