// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3SizeTransform;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3NavigationRail;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Captures reviewable screenshots of the HMCL Material 3 demo shell and primary pages.
@Tier2Test
@NotNullByDefault
final class HMCLDemoSnapshotTest {
    /// Directory receiving generated visual reports.
    private static final Path REPORT_DIRECTORY =
            Path.of(System.getProperty("user.dir"), "build", "reports", "hmcl-snapshots");

    /// Wide review width that activates the expanded primary navigation rail.
    private static final double WIDE_WINDOW_WIDTH = 1_100.0;

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
                    shell.controller().state().setAnimationDisabled(true);
                    assertRouteHostClipsContent(scene);
                    assertPrimaryNavigationItemsAreContained(scene);
                    writeSnapshot(scene, "01-home.png");

                    shell.controller().openAccounts();
                    assertContextSidebarIsScrollable(scene);
                    writeSnapshot(scene, "02-accounts.png");

                    shell.controller().goHome();
                    shell.controller().openInstances();
                    assertContextSidebarIsScrollable(scene);
                    writeSnapshot(scene, "03-instances.png");

                    shell.controller().openSelectedInstance();
                    assertContextSidebarIsScrollable(scene);
                    writeSnapshot(scene, "04-instance-detail.png");

                    shell.controller().goHome();
                    shell.controller().openDownload(HMCLDemoRoute.DownloadCategory.GAME);
                    assertContextSidebarIsScrollable(scene);
                    writeSnapshot(scene, "05-download.png");

                    shell.controller().goHome();
                    shell.controller().openSettings(HMCLDemoRoute.SettingsSection.APPEARANCE);
                    assertContextSidebarIsScrollable(scene);
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

                            M3OverlayPaneRoot shell = resolveShell(scene);
                            shell.controller().state().setLanguage(HMCLDemoStrings.SIMPLIFIED_CHINESE);

                            shell.controller().openAccounts();
                            assertContextSidebarIsScrollable(scene);
                            assertRowActionLabelsAreUncompressed(scene);
                            writeSnapshot(scene, "10-accounts-minimum-window.png");

                            shell.controller().goHome();
                            shell.controller().openInstances();
                            assertContextSidebarIsScrollable(scene);
                            assertInstanceActionsRetainLabels(scene);
                            assertRowActionLabelsAreUncompressed(scene);
                            writeSnapshot(scene, "11-instances-minimum-window.png");

                            shell.controller().openSelectedInstance();
                            assertContextSidebarIsScrollable(scene);
                            assertContextSidebarShowsOverflow(scene);
                            writeSnapshot(scene, "12-instance-detail-minimum-window.png");
                            HMCLDemoInstance selectedInstance = Objects.requireNonNull(
                                    shell.controller().state().getSelectedInstance(),
                                    "selected instance"
                            );
                            shell.controller().openInstance(
                                    selectedInstance.id(),
                                    HMCLDemoRoute.InstanceSection.MODS
                            );
                            assertRowActionLabelsAreUncompressed(scene);
                            writeSnapshot(scene, "18-instance-mods-minimum-window.png");

                            shell.controller().goHome();
                            shell.controller().openDownload(HMCLDemoRoute.DownloadCategory.GAME);
                            assertContextSidebarIsScrollable(scene);
                            assertRowActionLabelsAreUncompressed(scene);
                            writeSnapshot(scene, "13-download-minimum-window.png");
                            shell.controller().openDownload(HMCLDemoRoute.DownloadCategory.MOD);
                            assertRowActionLabelsAreUncompressed(scene);
                            writeSnapshot(scene, "19-download-mods-minimum-window.png");

                            shell.controller().goHome();
                            shell.controller().openSettings(HMCLDemoRoute.SettingsSection.APPEARANCE);
                            assertContextSidebarIsScrollable(scene);
                            assertContextSidebarShowsOverflow(scene);
                            writeSnapshot(scene, "14-settings-minimum-window.png");
                            shell.controller().openSettings(HMCLDemoRoute.SettingsSection.DOWNLOAD);
                            writeSnapshot(scene, "20-settings-download-minimum-window.png");

                            shell.controller().goHome();
                            shell.controller().openMultiplayer();
                            assertMultiplayerActionsWrap(scene);
                            writeSnapshot(scene, "15-multiplayer-minimum-window.png");
                            assertPrimaryNavigationItemsAreContained(scene);
                            assertAdaptiveNavigationIsAttached(scene);
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
                            M3OverlayPaneRoot shell = resolveShell(scene);
                            shell.controller().goHome();
                            shell.controller().state().setLanguage(HMCLDemoStrings.ENGLISH);
                            shell.controller().state().setBrightness(HMCLDemoState.Brightness.DARK);
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

    /// Verifies the wide-to-narrow resize path with motion enabled and writes both endpoint snapshots.
    @Test
    void keepsPrimaryNavigationAttachedAfterWideNarrowResize() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<Double> maximumGeometryError = new AtomicReference<>(0.0);
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable Stage stage = stageReference.get();
                        if (stage == null || !windowHasSize(stage, WIDE_WINDOW_WIDTH, HMCLDemoShell.prefWindowHeight())) {
                            return false;
                        }
                        Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                        return primaryRailHasSettled(scene, true) && navigationGeometryError(scene) <= 1.0;
                    },
                    3,
                    () -> {
                        M3MotionSettings.setGlobalReducedMotionRequested(false);
                        Stage stage = new Stage();
                        HMCLM3DemoApp app = new HMCLM3DemoApp();
                        app.start(stage);
                        stageReference.set(stage);

                        Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                        M3MotionSettings.setReducedMotionRequested(scene.getRoot(), false);
                        M3OverlayPaneRoot shell = resolveShell(scene);
                        shell.controller().state().setLanguage(HMCLDemoStrings.SIMPLIFIED_CHINESE);
                        shell.controller().state().setBrightness(HMCLDemoState.Brightness.LIGHT);
                        shell.controller().goHome();
                        stage.setWidth(WIDE_WINDOW_WIDTH);
                        stage.setHeight(HMCLDemoShell.prefWindowHeight());
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(
                                Objects.requireNonNull(stageReference.get(), "stage").getScene(),
                                "scene"
                        );
                        assertAdaptiveNavigationIsAttached(scene);
                        writeSnapshot(scene, "16-home-wide-window.png");
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable Stage stage = stageReference.get();
                        if (stage == null) {
                            return false;
                        }
                        Scene scene = Objects.requireNonNull(stage.getScene(), "scene");
                        maximumGeometryError.set(Math.max(
                                maximumGeometryError.get(),
                                navigationGeometryError(scene)
                        ));
                        return windowHasSize(stage, HMCLDemoShell.minWindowWidth(), HMCLDemoShell.minWindowHeight())
                                && primaryRailHasSettled(scene, false)
                                && navigationGeometryError(scene) <= 1.0;
                    },
                    4,
                    () -> {
                        Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                        stage.setWidth(HMCLDemoShell.minWindowWidth());
                        stage.setHeight(HMCLDemoShell.minWindowHeight());
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(
                                Objects.requireNonNull(stageReference.get(), "stage").getScene(),
                                "scene"
                        );
                        assertTrue(
                                maximumGeometryError.get() <= 1.0,
                                () -> "Navigation detached during resize by "
                                        + maximumGeometryError.get() + " logical pixels"
                        );
                        assertAdaptiveNavigationIsAttached(scene);
                        assertPrimaryNavigationItemsAreContained(scene);
                        writeSnapshot(scene, "17-home-after-wide-narrow-resize.png");
                    }
            );
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

    /// Returns whether the primary rail's rendered width reached its expanded or collapsed endpoint.
    ///
    /// @param scene    the scene to inspect
    /// @param expanded whether the expanded endpoint is expected
    /// @return `true` when the rail width is within one logical pixel of the requested endpoint
    private static boolean primaryRailHasSettled(Scene scene, boolean expanded) {
        Node node = scene.lookup(".hmcl-primary-nav-rail");
        if (!(node instanceof M3NavigationRail rail) || rail.isExpanded() != expanded) {
            return false;
        }
        double expectedWidth = expanded
                ? rail.getExpandedContainerWidth()
                : rail.getCollapsedContainerWidth();
        return Math.abs(rail.getWidth() - expectedWidth) <= 1.0;
    }

    /// Returns the largest separation among the rail, its stable slot, and the adjacent main pane.
    ///
    /// @param scene the scene to inspect
    /// @return the largest absolute geometry mismatch in logical pixels
    private static double navigationGeometryError(Scene scene) {
        Node scaffold = Objects.requireNonNull(scene.lookup(".hmcl-adaptive-scaffold"), "adaptive scaffold");
        Node railSlot = Objects.requireNonNull(
                scene.lookup(".m3-scaffold-navigation-rail"),
                "navigation rail slot"
        );
        Node rail = Objects.requireNonNull(scene.lookup(".hmcl-primary-nav-rail"), "primary navigation rail");
        Node mainSlot = Objects.requireNonNull(scene.lookup(".m3-scaffold-main-pane"), "main pane slot");

        Bounds scaffoldBounds = scaffold.localToScene(scaffold.getBoundsInLocal());
        Bounds railSlotBounds = railSlot.localToScene(railSlot.getBoundsInLocal());
        Bounds railBounds = rail.localToScene(rail.getBoundsInLocal());
        Bounds mainBounds = mainSlot.localToScene(mainSlot.getBoundsInLocal());
        return Math.max(
                Math.max(
                        Math.abs(railSlotBounds.getMinX() - scaffoldBounds.getMinX()),
                        Math.abs(railBounds.getMinX() - railSlotBounds.getMinX())
                ),
                Math.max(
                        Math.abs(railBounds.getMaxX() - railSlotBounds.getMaxX()),
                        Math.abs(mainBounds.getMinX() - railSlotBounds.getMaxX())
                )
        );
    }

    /// Verifies that the rendered primary rail fills its stable slot and directly precedes the main pane.
    ///
    /// @param scene the scene to inspect
    private static void assertAdaptiveNavigationIsAttached(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        assertTrue(
                navigationGeometryError(scene) <= 0.5,
                () -> "Adaptive navigation geometry mismatch: " + navigationGeometryError(scene)
        );
    }

    /// Verifies that page transitions clip retained content to the scaffold main pane.
    ///
    /// @param scene the scene to inspect
    private static void assertRouteHostClipsContent(Scene scene) {
        Node node = Objects.requireNonNull(scene.lookup(".hmcl-route-host"), "route host");
        M3AnimatedContent host = assertInstanceOf(M3AnimatedContent.class, node, "route host type");
        M3SizeTransform sizeTransform = Objects.requireNonNull(
                host.getContentTransform().sizeTransform(),
                "route size transform"
        );
        assertTrue(sizeTransform.clip(), "route host clipping");
    }

    /// Verifies that the active contextual sidebar retains fixed width and vertical scrolling.
    ///
    /// @param scene the scene to inspect
    private static void assertContextSidebarIsScrollable(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        Node node = Objects.requireNonNull(
                scene.lookup(".hmcl-context-sidebar-host"),
                "context sidebar host"
        );
        ScrollPane host = assertInstanceOf(ScrollPane.class, node, "context sidebar host type");
        assertEquals(ScrollPane.ScrollBarPolicy.NEVER, host.getHbarPolicy());
        assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, host.getVbarPolicy());
        assertFalse(host.isFitToHeight(), "context sidebar content must retain its scrollable height");
        assertEquals(HMCLDemoUi.SIDEBAR_WIDTH, host.getWidth(), 0.5);
    }

    /// Verifies that an overflowing contextual sidebar exposes a visible vertical scroll control.
    ///
    /// @param scene the scene to inspect
    private static void assertContextSidebarShowsOverflow(Scene scene) {
        ScrollPane host = assertInstanceOf(
                ScrollPane.class,
                Objects.requireNonNull(scene.lookup(".hmcl-context-sidebar-host"), "context sidebar host")
        );
        Node viewport = Objects.requireNonNull(host.lookup(".viewport"), "context sidebar viewport");
        Node verticalBar = Objects.requireNonNull(
                host.lookup(".scroll-bar:vertical"),
                "context sidebar vertical scroll bar"
        );
        assertTrue(
                host.getContent().getLayoutBounds().getHeight() > viewport.getLayoutBounds().getHeight() + 0.5,
                "context sidebar content did not retain overflow height"
        );
        assertTrue(verticalBar.isVisible(), "context sidebar vertical scroll bar is hidden");
    }

    /// Verifies that narrow instance rows preserve complete trailing action labels.
    ///
    /// @param scene the scene to inspect
    private static void assertInstanceActionsRetainLabels(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        Set<Node> actionRows = scene.getRoot().lookupAll(".hmcl-instance-actions");
        assertEquals(4, actionRows.size(), "instance action row count");
        for (Node actionRow : actionRows) {
            HBox row = assertInstanceOf(HBox.class, actionRow, "instance action row type");
            for (Node child : row.getChildren()) {
                M3Button button = assertInstanceOf(
                        M3Button.class,
                        child,
                        "instance action control type"
                );
                assertTrue(
                        button.getWidth() >= button.prefWidth(-1.0) - 0.5,
                        () -> "Instance action label was compressed: " + button.getText()
                );
            }
        }
    }

    /// Verifies that visible trailing and toolbar actions retain enough width for their complete labels.
    ///
    /// @param scene the scene to inspect
    private static void assertRowActionLabelsAreUncompressed(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        Set<Node> actions = scene.getRoot().lookupAll(".hmcl-row-action");
        assertFalse(actions.isEmpty(), "visible row action count");
        for (Node node : actions) {
            M3Button button = assertInstanceOf(M3Button.class, node, "row action control type");
            assertTrue(
                    button.getWidth() >= button.prefWidth(-1.0) - 0.5,
                    () -> "Row action label was compressed: " + button.getText()
            );
        }
    }

    /// Verifies that multiplayer actions wrap instead of compressing their labels at minimum width.
    ///
    /// @param scene the scene to inspect
    private static void assertMultiplayerActionsWrap(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        Node node = Objects.requireNonNull(
                scene.lookup(".hmcl-multiplayer-actions"),
                "multiplayer actions"
        );
        FlowPane actions = assertInstanceOf(FlowPane.class, node, "multiplayer action container type");
        assertEquals(5, actions.getChildren().size(), "multiplayer action count");
        double firstRowY = actions.getChildren().get(0).getLayoutY();
        assertTrue(
                actions.getChildren().stream().anyMatch(child -> Math.abs(child.getLayoutY() - firstRowY) > 1.0),
                "multiplayer actions did not wrap at minimum width"
        );
        for (Node child : actions.getChildren()) {
            M3Button button = assertInstanceOf(
                    M3Button.class,
                    child,
                    "multiplayer action control type"
            );
            assertTrue(
                    button.getWidth() >= button.prefWidth(-1.0) - 0.5,
                    () -> "Multiplayer action label was compressed: " + button.getText()
            );
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
