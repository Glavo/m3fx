// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.Scene;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Produces stable visual reports for the four defining Catalog surfaces.
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

    /// Captures Home, Component, Example, and theme-settings surfaces.
    @Test
    void writesCatalogNavigationSnapshots() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        try {
            FxTestUtils.assertNoCssWarningsInterruptibly(() -> FxTestUtils.runOnFxThread(() -> {
                Stage stage = new Stage();
                M3FXCatalogApp app = new M3FXCatalogApp();
                app.start(stage);
                stageReference.set(stage);
                Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
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
            }));
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Captures, validates, and writes one scene image.
    ///
    /// @param scene the showing Catalog scene
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
    /// @param image the captured image
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
