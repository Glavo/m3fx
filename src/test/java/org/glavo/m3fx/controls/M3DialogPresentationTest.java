// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/// Verifies the real-window scrim and presentation lifecycle of Material dialogs.
@NotNullByDefault
final class M3DialogPresentationTest {
    /// Starts JavaFX before presentation tests create windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes windows left behind after a failed presentation assertion.
    @AfterEach
    void closeWindows() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                window.hide();
            }
        });
    }

    /// Verifies owner coverage, rendered scrim content, and synchronous reduced-motion cleanup.
    @Tier2Test
    @Test
    void ownedDialogShowsRenderedScrimAndReducedMotionClosesImmediately() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> FxTestUtils.assertNoCssWarnings(() -> {
            Stage ownerStage = new Stage();
            M3Dialog<ButtonType> dialog = new M3Dialog<>(
                    "Dialog",
                    "Confirm operation",
                    "The owner should be dimmed while this dialog is visible.",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );
            try {
                StackPane ownerRoot = new StackPane(new M3Button("Open dialog"));
                Scene ownerScene = new Scene(ownerRoot, 420.0, 260.0);
                M3Theme theme = M3Theme.defaultTheme();
                M3ThemeManager.install(ownerScene, theme);
                ownerStage.setScene(ownerScene);
                ownerStage.show();
                ownerRoot.applyCss();
                ownerRoot.layout();

                dialog.initOwner(ownerRoot);
                dialog.show();

                assertNotSame(ownerRoot, ownerScene.getRoot());
                M3Scrim scrim = Objects.requireNonNull(findShowingScrim(ownerStage), "dialog scrim");
                scrim.applyCss();
                scrim.layout();
                Scene scrimScene = Objects.requireNonNull(scrim.getScene(), "scrim scene");

                assertSame(ownerScene, scrimScene);
                assertSame(ownerStage, scrimScene.getWindow());
                assertTrue(scrim.isShown());
                assertTrue(scrim.isVisible());
                assertEquals(ownerScene.getWidth(), scrim.getWidth(), 0.5);
                assertEquals(ownerScene.getHeight(), scrim.getHeight(), 0.5);
                assertEquals(
                        theme.tokens().componentTokens().scrim().containerOpacity(),
                        scrim.getOpacity(),
                        0.001
                );
                Color scrimFill = assertInstanceOf(
                        Color.class,
                        scrim.getBackground().getFills().get(0).getFill()
                );
                assertEquals(Color.BLACK, scrimFill);

                WritableImage image = scrim.snapshot(null, null);
                Color center = image.getPixelReader().getColor(
                        Math.max(0, (int) image.getWidth() / 2),
                        Math.max(0, (int) image.getHeight() / 2)
                );
                assertEquals(1.0, center.getOpacity(), 0.04);
                double expectedComposite = 1.0 - scrim.getOpacity();
                assertEquals(expectedComposite, center.getRed(), 0.04);
                assertEquals(expectedComposite, center.getGreen(), 0.04);
                assertEquals(expectedComposite, center.getBlue(), 0.04);

                dialog.close();

                assertFalse(dialog.isShowing());
                assertSame(ownerRoot, ownerScene.getRoot());
                assertNull(findShowingScrim(ownerStage));
            } finally {
                dialog.close();
                ownerStage.close();
            }
        }));
    }

    /// Verifies the physical window composition keeps the dialog above its owner scrim.
    @Tier2Test
    @Test
    void dialogRemainsVisibleAbovePhysicalScrim() throws InterruptedException {
        AtomicReference<@Nullable Stage> ownerStageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference = new AtomicReference<>();
        boolean previousReducedMotion = M3MotionSettings.isGlobalReducedMotionRequested();

        FxTestUtils.runOnFxThread(() -> M3MotionSettings.setGlobalReducedMotionRequested(true));
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable Stage ownerStage = ownerStageReference.get();
                        @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
                        return ownerStage != null
                                && dialog != null
                                && dialog.isShowing()
                                && dialog.getM3DialogPane().localToScreen(
                                dialog.getM3DialogPane().getBoundsInLocal()
                        ) != null
                                && findShowingScrim(ownerStage) != null;
                    },
                    3,
                    () -> {
                        Stage ownerStage = new Stage();
                        StackPane ownerRoot = new StackPane(new M3Button("Owner content"));
                        Scene ownerScene = new Scene(ownerRoot, 520.0, 340.0);
                        M3ThemeManager.install(ownerScene, M3Theme.defaultTheme());
                        ownerStage.setScene(ownerScene);
                        ownerStage.show();

                        M3Dialog<ButtonType> dialog = new M3Dialog<>(
                                "Visible dialog",
                                "Dialog above scrim",
                                "The dialog surface must remain visible above the modal scrim.",
                                ButtonType.CANCEL,
                                ButtonType.OK
                        );
                        dialog.initOwner(ownerRoot);
                        dialog.getM3DialogPane().setPrefWidth(420.0);

                        ownerStageReference.set(ownerStage);
                        dialogReference.set(dialog);
                        dialog.show();
                    },
                    () -> {
                        M3Dialog<ButtonType> dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
                        Stage ownerStage = Objects.requireNonNull(ownerStageReference.get(), "owner stage");
                        M3Scrim scrim = Objects.requireNonNull(findShowingScrim(ownerStage), "dialog scrim");
                        assertSame(ownerStage.getScene(), scrim.getScene());
                        assertNotSame(ownerStage.getScene(), dialog.getM3DialogPane().getScene());
                        assertDialogVisibleOnScreen(dialog.getM3DialogPane());
                    }
            );
        } finally {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
                if (dialog != null) {
                    dialog.close();
                }
                @Nullable Stage ownerStage = ownerStageReference.get();
                if (ownerStage != null) {
                    ownerStage.close();
                }
                M3MotionSettings.setGlobalReducedMotionRequested(previousReducedMotion);
            });
        }
    }

    /// Verifies observable enter and exit frames, cancelled-close recovery, and final scrim disposal.
    @Tier3Test
    @Test
    void dialogPresentationAnimatesAndRecoversFromCancelledClose() throws InterruptedException {
        AtomicReference<@Nullable Stage> ownerStageReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> ownerRootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Scrim> scrimReference = new AtomicReference<>();
        AtomicInteger closeRequestCount = new AtomicInteger();
        AtomicInteger hidingCount = new AtomicInteger();
        boolean previousReducedMotion = M3MotionSettings.isGlobalReducedMotionRequested();

        FxTestUtils.runOnFxThread(() -> M3MotionSettings.setGlobalReducedMotionRequested(false));
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> isIntermediateOpacity(dialogReference),
                    1,
                    () -> {
                        Stage ownerStage = new Stage();
                        StackPane ownerRoot = new StackPane(new M3Button("Open dialog"));
                        Scene ownerScene = new Scene(ownerRoot, 420.0, 260.0);
                        M3ThemeManager.install(ownerScene, M3Theme.defaultTheme());
                        ownerStage.setScene(ownerScene);
                        ownerStage.show();
                        ownerRoot.applyCss();
                        ownerRoot.layout();

                        M3MotionSpec observableSpec = M3MotionSpec.of(
                                Duration.millis(400.0),
                                M3MotionEasing.STANDARD
                        );
                        M3MotionScheme motionScheme = M3MotionScheme.builder(M3MotionScheme.standard())
                                .fastEffects(observableSpec)
                                .defaultEffects(observableSpec)
                                .build();
                        FxTestUtils.setMotionScheme(ownerRoot, motionScheme);

                        M3Dialog<ButtonType> dialog = new M3Dialog<>(
                                "Animated dialog",
                                "Confirm operation",
                                "This dialog exercises presentation motion.",
                                ButtonType.CANCEL,
                                ButtonType.OK
                        );
                        dialog.initOwner(ownerRoot);
                        dialog.setOnHiding(event -> hidingCount.incrementAndGet());
                        dialog.setOnCloseRequest(event -> {
                            if (closeRequestCount.incrementAndGet() == 1) {
                                event.consume();
                            }
                        });

                        ownerStageReference.set(ownerStage);
                        ownerRootReference.set(ownerRoot);
                        dialogReference.set(dialog);
                        dialog.show();

                        M3DialogPane pane = dialog.getM3DialogPane();
                        M3Scrim scrim = Objects.requireNonNull(findShowingScrim(ownerStage), "dialog scrim");
                        scrimReference.set(scrim);
                        assertTrue(dialog.isShowing());
                        assertEquals(0.0, pane.getOpacity(), 0.001);
                        assertEquals(0.0, scrim.getOpacity(), 0.001);
                    },
                    () -> {
                        M3Dialog<ButtonType> dialog = requiredDialog(dialogReference);
                        M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "dialog scrim");
                        assertTrue(isIntermediate(dialog.getM3DialogPane().getOpacity()));
                        assertTrue(isIntermediateScrimOpacity(scrim));
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> isFullyShown(dialogReference, scrimReference),
                    2,
                    () -> {
                    },
                    () -> {
                        M3Dialog<ButtonType> dialog = requiredDialog(dialogReference);
                        dialog.close();

                        assertTrue(dialog.isShowing(), "exit motion should retain the dialog window");
                        assertEquals(0, closeRequestCount.get(), "the provisional close request is internal");
                        assertEquals(0, hidingCount.get(), "the provisional hiding event is internal");
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> isIntermediateOpacity(dialogReference),
                    1,
                    () -> {
                    },
                    () -> {
                        M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "dialog scrim");
                        assertTrue(isIntermediateScrimOpacity(scrim));
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> closeRequestCount.get() == 1 && isFullyShown(dialogReference, scrimReference),
                    2,
                    () -> {
                    },
                    () -> {
                        M3Dialog<ButtonType> dialog = requiredDialog(dialogReference);
                        assertEquals(1, hidingCount.get());
                        dialog.close();

                        assertTrue(dialog.isShowing(), "the second close should also play exit motion");
                        assertEquals(1, closeRequestCount.get());
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
                        @Nullable Stage ownerStage = ownerStageReference.get();
                        return dialog != null
                                && ownerStage != null
                                && !dialog.isShowing()
                                && findShowingScrim(ownerStage) == null;
                    },
                    2,
                    () -> {
                    },
                    () -> {
                        M3Dialog<ButtonType> dialog = requiredDialog(dialogReference);
                        M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "dialog scrim");
                        Stage ownerStage = Objects.requireNonNull(ownerStageReference.get(), "owner stage");
                        StackPane ownerRoot = Objects.requireNonNull(ownerRootReference.get(), "owner root");
                        assertEquals(2, closeRequestCount.get());
                        assertEquals(2, hidingCount.get());
                        assertEquals(1.0, dialog.getM3DialogPane().getOpacity(), 0.001);
                        assertFalse(scrim.isShown());
                        assertSame(ownerRoot, Objects.requireNonNull(ownerStage.getScene(), "owner scene").getRoot());
                    }
            );
        } finally {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
                if (dialog != null) {
                    dialog.setOnCloseRequest(null);
                    dialog.close();
                }
                @Nullable StackPane ownerRoot = ownerRootReference.get();
                if (ownerRoot != null) {
                    FxTestUtils.clearMotionScheme(ownerRoot);
                }
                @Nullable Stage ownerStage = ownerStageReference.get();
                if (ownerStage != null) {
                    ownerStage.close();
                }
                M3MotionSettings.setGlobalReducedMotionRequested(previousReducedMotion);
            });
        }
    }

    /// Returns the current dialog or fails with a useful null-contract message.
    private static M3Dialog<ButtonType> requiredDialog(
            AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference
    ) {
        return Objects.requireNonNull(dialogReference.get(), "dialog");
    }

    /// Returns whether the dialog pane currently has a visible intermediate opacity.
    private static boolean isIntermediateOpacity(
            AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference
    ) {
        @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
        return dialog != null && dialog.isShowing() && isIntermediate(dialog.getM3DialogPane().getOpacity());
    }

    /// Returns whether both dialog and scrim have reached their shown state.
    private static boolean isFullyShown(
            AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference,
            AtomicReference<@Nullable M3Scrim> scrimReference
    ) {
        @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
        @Nullable M3Scrim scrim = scrimReference.get();
        return dialog != null
                && scrim != null
                && dialog.isShowing()
                && dialog.getM3DialogPane().getOpacity() >= 0.99
                && scrim.isShown()
                && scrim.getOpacity() >= scrim.getVisibleOpacity() - 0.005;
    }

    /// Returns whether a normalized opacity is away from both settled endpoints.
    private static boolean isIntermediate(double opacity) {
        return opacity > 0.05 && opacity < 0.95;
    }

    /// Returns whether a scrim is between hidden and fully visible opacity.
    private static boolean isIntermediateScrimOpacity(M3Scrim scrim) {
        return scrim.getOpacity() > 0.01 && scrim.getOpacity() < scrim.getVisibleOpacity() - 0.01;
    }

    /// Verifies that physical screen pixels in the dialog bounds match the rendered dialog pane.
    private static void assertDialogVisibleOnScreen(M3DialogPane pane) {
        pane.applyCss();
        pane.layout();
        @Nullable Bounds screenBounds = pane.localToScreen(pane.getBoundsInLocal());
        assertNotNull(screenBounds, "dialog pane screen bounds");

        int width = Math.max(1, (int) Math.ceil(screenBounds.getWidth()));
        int height = Math.max(1, (int) Math.ceil(screenBounds.getHeight()));
        WritableImage expected = pane.snapshot(null, null);
        WritableImage actual = new Robot().getScreenCapture(
                new WritableImage(width, height),
                screenBounds.getMinX(),
                screenBounds.getMinY(),
                screenBounds.getWidth(),
                screenBounds.getHeight(),
                true
        );
        assumeTrue(
                screenCaptureHasContent(actual),
                "the platform returned an all-black Robot screen capture"
        );

        int sampleStep = Math.max(2, Math.min(width, height) / 80);
        int comparableSamples = 0;
        int matchingSamples = 0;
        int sampleWidth = Math.min(width, (int) expected.getWidth());
        int sampleHeight = Math.min(height, (int) expected.getHeight());
        for (int y = sampleStep / 2; y < sampleHeight; y += sampleStep) {
            for (int x = sampleStep / 2; x < sampleWidth; x += sampleStep) {
                Color expectedColor = expected.getPixelReader().getColor(x, y);
                if (expectedColor.getOpacity() < 0.95) {
                    continue;
                }
                Color actualColor = actual.getPixelReader().getColor(x, y);
                comparableSamples++;
                if (colorDistance(expectedColor, actualColor) <= 0.18) {
                    matchingSamples++;
                }
            }
        }

        assertTrue(comparableSamples > 0, "dialog screen comparison has no opaque samples");
        double matchingRatio = (double) matchingSamples / comparableSamples;
        int centerX = Math.min(sampleWidth - 1, Math.max(0, sampleWidth / 2));
        int centerY = Math.min(sampleHeight - 1, Math.max(0, sampleHeight / 2));
        Color expectedCenter = expected.getPixelReader().getColor(centerX, centerY);
        Color actualCenter = actual.getPixelReader().getColor(centerX, centerY);
        assertTrue(
                matchingRatio >= 0.70,
                () -> "dialog is obscured in the physical window composition: matchingRatio=" + matchingRatio
                        + ", expectedCenter=" + expectedCenter
                        + ", actualCenter=" + actualCenter
                        + ", screenBounds=" + screenBounds
        );
    }

    /// Returns the maximum normalized RGB channel distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.max(
                Math.abs(first.getRed() - second.getRed()),
                Math.max(
                        Math.abs(first.getGreen() - second.getGreen()),
                        Math.abs(first.getBlue() - second.getBlue())
                )
        );
    }

    /// Returns whether a physical screen capture contains any non-black screen content.
    private static boolean screenCaptureHasContent(WritableImage image) {
        int sampleStep = Math.max(1, Math.min((int) image.getWidth(), (int) image.getHeight()) / 80);
        for (int y = sampleStep / 2; y < image.getHeight(); y += sampleStep) {
            for (int x = sampleStep / 2; x < image.getWidth(); x += sampleStep) {
                Color color = image.getPixelReader().getColor(x, y);
                if (Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue())) > 0.05) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns the shown dialog scrim owned by a specific window.
    private static @Nullable M3Scrim findShowingScrim(Window ownerWindow) {
        @Nullable Scene scene = ownerWindow.getScene();
        if (scene == null) {
            return null;
        }
        @Nullable M3Scrim scrim = findScrim(scene.getRoot());
        return scrim != null && scrim.isShown() ? scrim : null;
    }

    /// Searches an owner scene graph for its Material scrim.
    private static @Nullable M3Scrim findScrim(Node node) {
        if (node instanceof M3Scrim scrim) {
            return scrim;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3Scrim scrim = findScrim(child);
                if (scrim != null) {
                    return scrim;
                }
            }
        }
        return null;
    }
}
