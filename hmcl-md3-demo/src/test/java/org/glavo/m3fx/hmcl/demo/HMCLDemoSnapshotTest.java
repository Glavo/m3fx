// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

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
import java.util.concurrent.atomic.AtomicReference;

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

    /// Captures home, accounts, instances, download, settings, and multiplayer surfaces.
    @Test
    void writesHmclShellSnapshots() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThread(() -> {
                Stage stage = new Stage();
                HMCLM3DemoApp app = new HMCLM3DemoApp();
                app.start(stage);
                stageReference.set(stage);
                Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                M3MotionSettings.setReducedMotionRequested(scene.getRoot(), true);
                M3MotionSettings.setGlobalReducedMotionRequested(true);

                M3OverlayPaneRoot shell = resolveShell(scene);
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
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Resolves the shell controller from the scene root.
    private static M3OverlayPaneRoot resolveShell(Scene scene) {
        if (!(scene.getRoot() instanceof org.glavo.m3fx.controls.M3OverlayPane overlay)
                || !(overlay.getContent() instanceof HMCLDemoShell shell)) {
            throw new IllegalStateException("HMCL demo shell is not installed as scene content");
        }
        return new M3OverlayPaneRoot(shell);
    }

    /// Thin access wrapper for the shell controller.
    private record M3OverlayPaneRoot(HMCLDemoShell shell) {
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
