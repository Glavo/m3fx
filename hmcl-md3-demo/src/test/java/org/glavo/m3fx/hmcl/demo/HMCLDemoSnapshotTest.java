// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Bounds;
import javafx.scene.Node;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Captures reviewable screenshots of the HMCL Material 3 demo shell and primary pages.
@Tier2Test
@NotNullByDefault
final class HMCLDemoSnapshotTest {
    /// Directory receiving generated visual reports.
    private static final Path REPORT_DIRECTORY =
            Path.of(System.getProperty("user.dir"), "build", "reports", "hmcl-snapshots");

    /// Starts the JavaFX toolkit before creating a real window.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Captures primary pages plus minimum-window, English, and dark-theme shell states.
    @Test
    void writesHmclShellSnapshots() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        try {
            FxTestUtils.assertNoCssWarningsInterruptibly(() -> {
                FxTestUtils.runOnFxThread(() -> {
                    Stage stage = new Stage();
                    HMCLM3DemoApp app = new HMCLM3DemoApp();
                    app.start(stage);
                    stageReference.set(stage);
                    Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                    M3MotionSettings.setReducedMotionRequested(scene.getRoot(), true);
                    M3MotionSettings.setGlobalReducedMotionRequested(true);

                    M3OverlayPaneRoot shell = resolveShell(scene);
                    assertPrimaryNavigationItemsAreContained(scene);
                    writeSnapshot(scene, "01-home.png");

                    shell.controller().openAccounts();
                    writeSnapshot(scene, "02-accounts.png");

                    shell.controller().goHome();
                    shell.controller().openInstances();
                    writeSnapshot(scene, "03-instances.png");

                    shell.controller().openSelectedInstance();
                    writeSnapshot(scene, "04-instance-detail.png");

                    shell.controller().goHome();
                    shell.controller().openDownload(HMCLDemoRoute.DownloadCategory.GAME);
                    writeSnapshot(scene, "05-download.png");

                    shell.controller().goHome();
                    shell.controller().openSettings(HMCLDemoRoute.SettingsSection.APPEARANCE);
                    writeSnapshot(scene, "06-settings-appearance.png");

                    shell.controller().goHome();
                    shell.controller().openMultiplayer();
                    writeSnapshot(scene, "07-multiplayer.png");
                });

                FxTestUtils.runOnFxThreadWhenStable(
                        () -> windowHasSize(
                                Objects.requireNonNull(stageReference.get(), "stage"),
                                HMCLDemoShell.minWindowWidth(),
                                HMCLDemoShell.minWindowHeight()
                        ),
                        2,
                        () -> {
                            Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                            Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                            M3OverlayPaneRoot shell = resolveShell(scene);
                            shell.controller().goHome();
                            shell.controller().state().setLanguage(HMCLDemoStrings.ENGLISH);
                            stage.setWidth(HMCLDemoShell.minWindowWidth());
                            stage.setHeight(HMCLDemoShell.minWindowHeight());
                        },
                        () -> {
                            Scene scene = Objects.requireNonNull(
                                    Objects.requireNonNull(stageReference.get(), "stage").getScene(),
                                    "scene"
                            );
                            writeSnapshot(scene, "08-home-english-minimum-window.png");
                            assertPrimaryNavigationItemsAreContained(scene);
                        }
                );

                FxTestUtils.runOnFxThreadWhenStable(
                        () -> windowHasSize(
                                Objects.requireNonNull(stageReference.get(), "stage"),
                                HMCLDemoShell.prefWindowWidth(),
                                HMCLDemoShell.prefWindowHeight()
                        ),
                        2,
                        () -> {
                            Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                            Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                            resolveShell(scene).controller().state().setBrightness(HMCLDemoState.Brightness.DARK);
                            stage.setWidth(HMCLDemoShell.prefWindowWidth());
                            stage.setHeight(HMCLDemoShell.prefWindowHeight());
                        },
                        () -> {
                            Scene scene = Objects.requireNonNull(
                                    Objects.requireNonNull(stageReference.get(), "stage").getScene(),
                                    "scene"
                            );
                            writeSnapshot(scene, "09-home-dark-english.png");
                            assertPrimaryNavigationItemsAreContained(scene);
                        }
                );
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
                M3MotionSettings.setGlobalReducedMotionRequested(false);
            });
        }
    }

    /// Returns whether a stage has settled at the requested outer dimensions.
    ///
    /// @param stage  the stage to inspect
    /// @param width  the requested outer width
    /// @param height the requested outer height
    /// @return `true` when both dimensions are within one logical pixel
    private static boolean windowHasSize(Stage stage, double width, double height) {
        return Math.abs(stage.getWidth() - width) <= 1.0
                && Math.abs(stage.getHeight() - height) <= 1.0;
    }

    /// Resolves the shell controller from the scene root.
    private static M3OverlayPaneRoot resolveShell(Scene scene) {
        if (!(scene.getRoot() instanceof org.glavo.m3fx.controls.M3OverlayPane overlay)
                || !(overlay.getContent() instanceof HMCLDemoShell shell)) {
            throw new IllegalStateException("HMCL demo shell is not installed as scene content");
        }
        return new M3OverlayPaneRoot(shell);
    }

    /// Verifies that every primary rail destination remains visible in the review window.
    private static void assertPrimaryNavigationItemsAreContained(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();

        Node rail = Objects.requireNonNull(
                scene.lookup(".hmcl-primary-nav-rail"),
                "primary navigation rail"
        );
        Bounds railBounds = rail.localToScene(rail.getBoundsInLocal());
        Set<Node> items = rail.lookupAll(".hmcl-primary-nav-item");
        assertEquals(6, items.size(), "primary navigation item count");
        for (Node item : items) {
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(
                    itemBounds.getMinY() >= railBounds.getMinY() - 0.5,
                    () -> "Navigation item begins above the rail: " + itemBounds
            );
            assertTrue(
                    itemBounds.getMaxY() <= railBounds.getMaxY() + 0.5,
                    () -> "Navigation item extends below the rail: " + itemBounds
            );

            Node label = Objects.requireNonNull(
                    item.lookup(".m3-navigation-item-label"),
                    "navigation item label"
            );
            Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
            assertTrue(
                    labelBounds.getMinX() >= railBounds.getMinX() - 0.5,
                    () -> "Navigation label begins outside the rail: " + labelBounds
            );
            assertTrue(
                    labelBounds.getMaxX() <= railBounds.getMaxX() + 0.5,
                    () -> "Navigation label extends outside the rail: label=" + labelBounds
                            + ", item=" + itemBounds + ", rail=" + railBounds
            );
            assertTrue(
                    labelBounds.getMinY() >= itemBounds.getMinY() - 0.5,
                    () -> "Navigation label begins above its item: " + labelBounds
            );
            assertTrue(
                    labelBounds.getMaxY() <= itemBounds.getMaxY() + 0.5,
                    () -> "Navigation label extends below its item: " + labelBounds
            );
        }
    }

    /// Provides access to the shell controller used by the visual route sequence.
    ///
    /// @param shell the demo shell
    private record M3OverlayPaneRoot(HMCLDemoShell shell) {
        /// Returns the route controller.
        ///
        /// @return the route controller
        HMCLDemoController controller() {
            return shell;
        }
    }

    /// Captures and writes one scene image.
    private static void writeSnapshot(Scene scene, String fileName) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        int width = Math.max(1, (int) Math.ceil(scene.getWidth()));
        int height = Math.max(1, (int) Math.ceil(scene.getHeight()));
        WritableImage image = new WritableImage(width, height);
        scene.snapshot(image);
        scene.snapshot(image);

        try {
            Files.createDirectories(REPORT_DIRECTORY);
            ImageIO.write(toBufferedImage(image), "png", REPORT_DIRECTORY.resolve(fileName).toFile());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /// Converts a JavaFX image for ImageIO.
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] buffer = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), buffer, 0, width);
        bufferedImage.setRGB(0, 0, width, height, buffer, 0, width);
        return bufferedImage;
    }
}
