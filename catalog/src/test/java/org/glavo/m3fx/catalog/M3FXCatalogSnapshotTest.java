// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Produces stable visual reports for the defining Catalog surfaces and expanded component examples.
///
/// These reports supplement structural assertions in [M3FXCatalogVisualTest]. Each image comes from a showing
/// JavaFX Stage after CSS and layout, and is rejected when it lacks enough color variation to represent a rendered
/// interface.
@Tier2Test
@NotNullByDefault
final class M3FXCatalogSnapshotTest {
    /// The directory receiving generated Catalog visual reports.
    private static final Path REPORT_DIRECTORY =
            Path.of(System.getProperty("user.dir"), "build", "reports", "catalog-snapshots");

    /// Starts the JavaFX toolkit before creating a real window.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Captures navigation, theme settings, expanded control families, and compact-window surfaces.
    @Test
    void writesCatalogNavigationSnapshots() throws InterruptedException {
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
                    appReference.set(app);
                    Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                    sceneReference.set(scene);
                    M3MotionSettings.setReducedMotionRequested(scene.getRoot(), true);

                    writeSnapshot(scene, "home.png");

                    CatalogComponent buttons = app.components().stream()
                            .filter(component -> component.name().equals("Buttons"))
                            .findFirst()
                            .orElseThrow();
                    app.navigate(new CatalogRoute.Component(buttons));
                    writeSnapshot(scene, "component.png");

                    app.navigate(new CatalogRoute.Example(buttons, buttons.examples().get(0)));
                    writeSnapshot(scene, "example.png");

                    app.showSettings();
                    writeSnapshot(scene, "theme-settings.png");
                    app.hideSettings();

                    CatalogComponent search = componentNamed(app, "Search");
                    app.navigate(new CatalogRoute.Example(
                            search,
                            exampleNamed(search, "Contained docked search")
                    ));
                    writeSnapshot(scene, "search-view.png");

                    CatalogComponent datePickers = componentNamed(app, "Date pickers");
                    app.navigate(new CatalogRoute.Example(
                            datePickers,
                            exampleNamed(datePickers, "Date range picker")
                    ));
                    writeSnapshot(scene, "date-range-picker.png");

                    CatalogComponent sideSheets = componentNamed(app, "Side sheets");
                    app.navigate(new CatalogRoute.Example(
                            sideSheets,
                            exampleNamed(sideSheets, "Modal side sheet")
                    ));
                    writeSnapshot(scene, "modal-side-sheet.png");

                    writeExampleSnapshot(
                            scene,
                            app,
                            "Adaptive",
                            "Expanded two pane",
                            "adaptive-expanded.png"
                    );
                    writeExampleSnapshot(
                            scene,
                            app,
                            "Buttons",
                            "Extra-large buttons",
                            "button-extra-large.png"
                    );
                    writeExampleSnapshot(
                            scene,
                            app,
                            "Chips",
                            "Selected filter chip",
                            "chip-selected.png"
                    );
                    writeExampleSnapshot(
                            scene,
                            app,
                            "Navigation rail",
                            "Expanded standard rail",
                            "navigation-rail-expanded.png"
                    );
                    writeExampleSnapshot(
                            scene,
                            app,
                            "Progress indicators",
                            "Expressive determinate linear",
                            "progress-expressive.png"
                    );
                    writeExampleSnapshot(
                            scene,
                            app,
                            "Text fields",
                            "Outlined error field",
                            "text-field-error.png"
                    );
                    writeFirstExampleSnapshot(scene, app, "Avatars", "avatars.png");
                    writeFirstExampleSnapshot(scene, app, "Banners", "banners.png");
                    writeFirstExampleSnapshot(scene, app, "Color pickers", "color-picker.png");
                    writeFirstExampleSnapshot(scene, app, "Forms", "forms.png");
                    writeFirstExampleSnapshot(scene, app, "Icons", "icons.png");
                    writeFirstExampleSnapshot(scene, app, "Scrims", "scrims.png");
                    writeFirstExampleSnapshot(scene, app, "Settings", "settings.png");
                    writeFirstExampleSnapshot(scene, app, "Surfaces", "surfaces.png");
                });

                FxTestUtils.runOnFxThreadWhenStable(
                        () -> {
                            @Nullable Scene scene = sceneReference.get();
                            return scene != null
                                    && scene.lookup(".catalog-component-page") != null
                                    && scene.getWidth() >= 1_000.0;
                        },
                        2,
                        () -> {
                            Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                            M3FXCatalogApp app = Objects.requireNonNull(appReference.get(), "app");
                            CatalogComponent buttons = componentNamed(app, "Buttons");
                            stage.setWidth(1_080.0);
                            stage.setHeight(800.0);
                            app.navigate(new CatalogRoute.Component(buttons));
                        },
                        () -> writeSnapshot(
                                Objects.requireNonNull(sceneReference.get(), "scene"),
                                "sidebar-component.png"
                        )
                );

                FxTestUtils.runOnFxThreadWhenStable(
                        () -> {
                            @Nullable Scene scene = sceneReference.get();
                            return scene != null
                                    && scene.getWidth() <= 460.0
                                    && scene.getHeight() <= 560.0;
                        },
                        2,
                        () -> {
                            Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                            M3FXCatalogApp app = Objects.requireNonNull(appReference.get(), "app");
                            app.navigateHome();
                            stage.setWidth(460.0);
                            stage.setHeight(560.0);
                        },
                        () -> {
                            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                            M3FXCatalogApp app = Objects.requireNonNull(appReference.get(), "app");
                            writeSnapshot(scene, "compact-home.png");
                            CatalogComponent buttons = componentNamed(app, "Buttons");
                            app.navigate(new CatalogRoute.Component(buttons));
                            writeSnapshot(scene, "compact-component.png");
                            writeFirstExampleSnapshot(scene, app, "Forms", "compact-form.png");
                        }
                );

                FxTestUtils.runOnFxThreadWhenStable(
                        () -> {
                            @Nullable Scene scene = sceneReference.get();
                            if (scene == null) {
                                return false;
                            }
                            @Nullable Node drawerNode = scene.lookup(".catalog-sidebar-drawer");
                            @Nullable Node scrim = scene.lookup(".catalog-sidebar-scrim");
                            return drawerNode instanceof M3NavigationDrawer drawer
                                    && drawer.getVariant() == M3NavigationDrawerVariant.MODAL
                                    && Math.abs(drawer.getWidth() - 360.0) <= 0.5
                                    && scrim != null
                                    && scrim.isVisible();
                        },
                        2,
                        () -> {
                            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                            M3FXCatalogApp app = Objects.requireNonNull(appReference.get(), "app");
                            CatalogComponent buttons = componentNamed(app, "Buttons");
                            app.navigate(new CatalogRoute.Component(buttons));
                            M3IconButton browseButton = assertInstanceOf(
                                    M3IconButton.class,
                                    Objects.requireNonNull(
                                            scene.lookup(".catalog-sidebar-action"),
                                            "compact sidebar action"
                                    )
                            );
                            browseButton.fire();
                        },
                        () -> writeSnapshot(
                                Objects.requireNonNull(sceneReference.get(), "scene"),
                                "compact-sidebar.png"
                        )
                );
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

    /// Opens and captures the first example for a named component.
    ///
    /// @param scene    the showing Catalog scene
    /// @param app      the running Catalog application
    /// @param name     the component display name
    /// @param fileName the report file name
    private static void writeFirstExampleSnapshot(
            Scene scene,
            M3FXCatalogApp app,
            String name,
            String fileName
    ) {
        CatalogComponent component = componentNamed(app, name);
        app.navigate(new CatalogRoute.Example(component, component.examples().get(0)));
        writeSnapshot(scene, fileName);
    }

    /// Opens and captures a named example for a named component.
    ///
    /// @param scene the showing Catalog scene
    /// @param app the running Catalog application
    /// @param componentName the component display name
    /// @param exampleName the example display name
    /// @param fileName the report file name
    private static void writeExampleSnapshot(
            Scene scene,
            M3FXCatalogApp app,
            String componentName,
            String exampleName,
            String fileName
    ) {
        CatalogComponent component = componentNamed(app, componentName);
        app.navigate(new CatalogRoute.Example(component, exampleNamed(component, exampleName)));
        writeSnapshot(scene, fileName);
    }

    /// Finds a registered component by its display name.
    ///
    /// @param app  the running Catalog application
    /// @param name the component display name
    /// @return the matching component
    private static CatalogComponent componentNamed(M3FXCatalogApp app, String name) {
        return app.components().stream()
                .filter(component -> component.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing Catalog component: " + name));
    }

    /// Finds an example by its display name.
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

    /// Captures, validates, and writes one scene image.
    ///
    /// @param scene    the showing Catalog scene
    /// @param fileName the report file name
    private static void writeSnapshot(Scene scene, String fileName) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        int width = Math.max(1, (int) Math.ceil(scene.getWidth()));
        int height = Math.max(1, (int) Math.ceil(scene.getHeight()));
        WritableImage image = new WritableImage(width, height);
        scene.snapshot(image);
        scene.snapshot(image);
        assertVisualRange(image, fileName);

        try {
            Files.createDirectories(REPORT_DIRECTORY);
            ImageIO.write(toBufferedImage(image), "png", REPORT_DIRECTORY.resolve(fileName).toFile());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /// Verifies that a report contains a non-trivial set of rendered colors.
    ///
    /// @param image       the captured image
    /// @param description the report description used by assertion output
    private static void assertVisualRange(WritableImage image, String description) {
        Set<Integer> colors = new HashSet<>();
        int step = Math.max(2, Math.min((int) image.getWidth(), (int) image.getHeight()) / 160);
        for (int y = step / 2; y < image.getHeight(); y += step) {
            for (int x = step / 2; x < image.getWidth(); x += step) {
                colors.add(image.getPixelReader().getArgb(x, y));
            }
        }
        assertTrue(colors.size() >= 16, () -> description + " contains too little visual variation");
    }

    /// Converts a JavaFX image to the standard image representation accepted by ImageIO.
    ///
    /// @param image the JavaFX image
    /// @return the converted ARGB image
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(
                0,
                0,
                width,
                height,
                PixelFormat.getIntArgbInstance(),
                pixels,
                0,
                width
        );
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, width, height, pixels, 0, width);
        return result;
    }
}
