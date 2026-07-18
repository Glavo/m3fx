// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.monetfx.ColorRole;
import org.glavo.monetfx.Brightness;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the public lifecycle, rendering, modality, and stacking contracts of in-scene Material dialogs.
@NotNullByDefault
final class M3DialogPresentationTest {
    /// Starts the JavaFX toolkit once before dialog lifecycle tests use the application thread.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that showing requires a configured owner attached to a visible window and hosted by an overlay pane.
    @Test
    void showRejectsMissingDetachedOrUnhostedOwner() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Dialog dialog = new M3Dialog();

            assertThrows(IllegalStateException.class, dialog::show);

            StackPane detachedRoot = new StackPane();
            dialog.setOwner(detachedRoot);

            assertThrows(IllegalStateException.class, dialog::show);

            Stage stage = new Stage();
            StackPane unhostedRoot = new StackPane();
            Scene unhostedScene = new Scene(unhostedRoot, 320.0, 200.0);
            stage.setScene(unhostedScene);
            stage.show();
            dialog.setOwner(unhostedRoot);
            try {
                assertThrows(IllegalStateException.class, dialog::show);
                assertSame(unhostedRoot, unhostedScene.getRoot());
            } finally {
                stage.close();
            }
            assertFalse(dialog.isShowing());
        });
    }

    /// Verifies unconstrained height measurement keeps JavaFX's natural-width sentinel semantics.
    @Test
    void unconstrainedPreferredHeightDoesNotMeasureTextAtZeroWidth() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogPane pane = new M3DialogPane();
            pane.setHeaderText("Dialog title");
            pane.setContentText("A short supporting sentence should occupy its natural number of lines.");
            pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            StackPane contentRoot = new StackPane(pane);
            M3OverlayPane overlayRoot = createOverlayRoot(contentRoot);
            new Scene(overlayRoot, 520.0, 340.0);
            overlayRoot.applyCss();

            double preferredHeight = pane.prefHeight(-1.0);

            assertTrue(preferredHeight >= 120.0 && preferredHeight < 300.0,
                    () -> "unconstrained dialog height should remain compact: " + preferredHeight);
        });
    }

    /// Verifies the showing callback cannot recursively present the dialog or replace its active owner.
    @Tier2Test
    @Test
    void showingLifecycleLocksPresentationAndOwnerState() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane alternateOwner = new StackPane();
            StackPane ownerContent = new StackPane(new M3Button("Owner"), alternateOwner);
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            AtomicInteger showingCalls = new AtomicInteger();
            dialog.setOnShowing(event -> {
                showingCalls.incrementAndGet();
                assertNull(event.getButtonType());
                dialog.show();
                assertThrows(IllegalStateException.class, () -> dialog.setOwner(alternateOwner));
            });

            try {
                dialog.show();

                assertTrue(dialog.isShowing());
                assertEquals(1, showingCalls.get());
                assertSame(ownerContent, dialog.getOwner());
                assertSame(overlayRoot, scene.getRoot());
                assertSame(ownerContent, overlayRoot.getContent());
                assertDialogAttached(dialog, scene, overlayRoot);
            } finally {
                dialog.setOnShowing(null);
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies action cancellation and the initiating button type throughout the cancellable close lifecycle.
    @Test
    void actionButtonsDriveCancellableLifecycleWithButtonType() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType acceptType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            dialog.getDialogPane().setHeaderText("Confirm action");
            dialog.getDialogPane().getButtonTypes().setAll(cancelType, acceptType);
            List<String> lifecycle = new ArrayList<>();
            AtomicInteger closeRequests = new AtomicInteger();
            dialog.setOnShowing(event -> {
                assertNull(event.getButtonType());
                lifecycle.add("showing");
            });
            dialog.setOnShown(event -> {
                assertNull(event.getButtonType());
                lifecycle.add("shown");
            });
            dialog.setOnCloseRequest(event -> {
                assertSame(acceptType, event.getButtonType());
                lifecycle.add("close-request");
                if (closeRequests.getAndIncrement() == 0) {
                    event.consume();
                }
            });
            dialog.setOnHiding(event -> {
                assertSame(acceptType, event.getButtonType());
                lifecycle.add("hiding");
            });
            dialog.setOnHidden(event -> {
                assertSame(acceptType, event.getButtonType());
                lifecycle.add("hidden");
            });

            try {
                dialog.show();
                M3Button accept = (M3Button) Objects.requireNonNull(
                        dialog.getDialogPane().lookupButton(acceptType),
                        "accept button"
                );
                EventHandler<ActionEvent> consumeAction = ActionEvent::consume;
                accept.addEventFilter(ActionEvent.ACTION, consumeAction);
                accept.fire();

                assertTrue(dialog.isShowing());
                assertEquals(List.of("showing", "shown"), lifecycle);

                accept.removeEventFilter(ActionEvent.ACTION, consumeAction);
                accept.fire();

                assertTrue(dialog.isShowing());
                assertEquals(List.of("showing", "shown", "close-request"), lifecycle);

                accept.fire();

                assertFalse(dialog.isShowing());
                assertEquals(
                        List.of("showing", "shown", "close-request", "close-request", "hiding", "hidden"),
                        lifecycle
                );
                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies scrim clicks use the cancellable dialog lifecycle and can be disabled without unblocking the owner.
    @Test
    void scrimClickRequestsCancellableDismissalWhenEnabled() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            AtomicInteger closeRequests = new AtomicInteger();
            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            dialog.setOnCloseRequest(event -> {
                assertNull(event.getButtonType());
                if (closeRequests.incrementAndGet() == 1) {
                    event.consume();
                }
            });
            dialog.setOnHidden(event -> assertNull(event.getButtonType()));
            try {
                assertTrue(dialog.isDismissOnScrimClick());
                dialog.show();
                assertSame(overlayRoot, scene.getRoot());
                assertDialogAttached(dialog, scene, overlayRoot);
                assertEquals(1, descendantScrims(overlayRoot).size());
                M3Scrim scrim = descendantScrims(overlayRoot).get(0);

                dialog.dismissOnScrimClickProperty().set(false);
                scrim.fireEvent(primaryMouseClick());

                assertTrue(dialog.isShowing());
                assertEquals(0, closeRequests.get());
                assertFalse(scrim.isMouseTransparent());

                dialog.setDismissOnScrimClick(true);
                scrim.fireEvent(primaryMouseClick());

                assertTrue(dialog.isShowing());
                assertEquals(1, closeRequests.get());

                scrim.fireEvent(primaryMouseClick());

                assertFalse(dialog.isShowing());
                assertEquals(2, closeRequests.get());
                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.setOnCloseRequest(null);
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies animated dismissal retains the overlay until the scrim has completed its opacity transition.
    @Tier2Test
    @Test
    void dialogWaitsForScrimFadeBeforeRemovingOverlay() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> ownerContentReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayRootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog> dialogReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Scrim> scrimReference = new AtomicReference<>();
        AtomicBoolean observedIntermediateOpacity = new AtomicBoolean();
        AtomicReference<Double> minimumAttachedOpacity = new AtomicReference<>(Double.POSITIVE_INFINITY);

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Scrim scrim = scrimReference.get();
                        return scrim != null && scrim.getOpacity() >= scrim.getVisibleOpacity() - 0.001;
                    },
                    () -> {
                        Stage stage = new Stage();
                        StackPane ownerContent = new StackPane(new M3Button("Owner"));
                        M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
                        Scene scene = new Scene(overlayRoot, 520.0, 340.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();

                        M3Dialog dialog = new M3Dialog();
                        dialog.setOwner(ownerContent);
                        dialog.getDialogPane().setHeaderText("Animated dialog");
                        dialog.show();

                        stageReference.set(stage);
                        ownerContentReference.set(ownerContent);
                        overlayRootReference.set(overlayRoot);
                        dialogReference.set(dialog);
                        scrimReference.set(descendantScrims(overlayRoot).get(0));
                    },
                    () -> {
                        M3Dialog dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
                        M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "scrim");
                        scrim.opacityProperty().addListener((observable, oldOpacity, opacity) -> {
                            if (dialog.getDialogPane().getParent() == null) {
                                return;
                            }
                            double value = opacity.doubleValue();
                            minimumAttachedOpacity.accumulateAndGet(value, Math::min);
                            if (value > 0.001 && value < scrim.getVisibleOpacity() - 0.001) {
                                observedIntermediateOpacity.set(true);
                            }
                        });

                        dialog.close();

                        assertTrue(dialog.isShowing(), "animated close should retain the overlay until motion settles");
                        M3OverlayPane overlayRoot =
                                Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                        assertDialogAttached(
                                dialog,
                                Objects.requireNonNull(overlayRoot.getScene(), "overlay scene"),
                                overlayRoot
                        );
                        assertEquals(1, descendantScrims(overlayRoot).size(),
                                "animated close should retain the dialog scrim");
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Dialog dialog = dialogReference.get();
                        return dialog != null && !dialog.isShowing();
                    },
                    () -> {
                    },
                    () -> {
                        Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                        StackPane ownerContent =
                                Objects.requireNonNull(ownerContentReference.get(), "owner content");
                        M3OverlayPane overlayRoot =
                                Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                        M3Dialog dialog = Objects.requireNonNull(dialogReference.get(), "dialog");

                        assertTrue(observedIntermediateOpacity.get(),
                                "scrim exit should include at least one intermediate opacity");
                        assertTrue(minimumAttachedOpacity.get() <= 0.001,
                                () -> "overlay detached before scrim reached zero opacity: "
                                        + minimumAttachedOpacity.get());
                        assertNull(dialog.getDialogPane().getParent());
                        assertStableEmptyOverlay(
                                dialog,
                                Objects.requireNonNull(stage.getScene(), "stage scene"),
                                overlayRoot,
                                ownerContent
                        );
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                M3Dialog dialog = dialogReference.get();
                if (dialog != null) {
                    dialog.close();
                }
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies lifecycle-handler failures leave either an untouched scene or a usable visible dialog.
    @Tier2Test
    @Test
    void lifecycleFailuresRollbackPresentationState() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            ButtonType acceptType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, acceptType);
            RuntimeException showingFailure = new RuntimeException("showing failure");
            RuntimeException shownFailure = new RuntimeException("shown failure");
            RuntimeException hidingFailure = new RuntimeException("hiding failure");
            RuntimeException hiddenFailure = new RuntimeException("hidden failure");

            try {
                dialog.setOnShowing(event -> {
                    assertNull(event.getButtonType());
                    throw showingFailure;
                });
                assertSame(showingFailure, assertThrows(RuntimeException.class, dialog::show));
                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);

                dialog.setOnShowing(null);
                dialog.setOnShown(event -> {
                    assertNull(event.getButtonType());
                    throw shownFailure;
                });
                assertSame(shownFailure, assertThrows(RuntimeException.class, dialog::show));
                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);

                dialog.setOnShown(null);
                dialog.show();
                dialog.setOnHiding(event -> {
                    assertSame(acceptType, event.getButtonType());
                    throw hidingFailure;
                });
                M3Button accept = assertInstanceOf(
                        M3Button.class,
                        Objects.requireNonNull(dialog.getDialogPane().lookupButton(acceptType), "accept button")
                );
                assertSame(hidingFailure, assertThrows(RuntimeException.class, accept::fire));
                assertTrue(dialog.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertDialogAttached(dialog, scene, overlayRoot);

                dialog.setOnHiding(event -> assertSame(acceptType, event.getButtonType()));
                dialog.setOnHidden(event -> {
                    assertSame(acceptType, event.getButtonType());
                    throw hiddenFailure;
                });
                assertSame(hiddenFailure, assertThrows(RuntimeException.class, accept::fire));
                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.setOnShowing(null);
                dialog.setOnShown(null);
                dialog.setOnHiding(null);
                dialog.setOnHidden(null);
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies inherited scene and owner theme changes restyle a visible dialog without replacing its pane.
    @Test
    void dialogTracksOwnerThemeContextWithoutRecreatingPane() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            M3Theme initialSceneTheme = M3Theme.defaultTheme();
            M3Theme replacementSceneTheme = M3Theme.fromSeed(Color.web("#386a20"));
            M3Theme initialOwnerTheme = M3Theme.fromSeed(Color.web("#984061"), Brightness.DARK);
            M3Theme replacementOwnerTheme = M3Theme.fromSeed(Color.web("#006a6a"), Brightness.DARK);
            M3ThemeManager.install(scene, initialSceneTheme);
            stage.setScene(scene);
            stage.show();

            M3Dialog dialog = new M3Dialog();
            M3DialogPane pane = dialog.getDialogPane();
            dialog.setOwner(ownerContent);
            pane.setHeaderText("Themed dialog");
            pane.getButtonTypes().setAll(ButtonType.OK);
            try {
                dialog.show();

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(initialSceneTheme), renderedDialogSurface(dialog));

                M3ThemeManager.install(scene, replacementSceneTheme);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(replacementSceneTheme), renderedDialogSurface(dialog));

                M3ThemeManager.uninstall(scene);
                M3ThemeManager.install(ownerContent, initialOwnerTheme);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(initialOwnerTheme), renderedDialogSurface(dialog));

                M3ThemeManager.install(ownerContent, replacementOwnerTheme);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(replacementOwnerTheme), renderedDialogSurface(dialog));

                M3ThemeManager.install(scene, initialSceneTheme);

                assertEquals(dialogSurfaceColor(initialSceneTheme), renderedDialogSurface(dialog),
                        "a scene theme should remain authoritative over a local owner theme");
            } finally {
                dialog.close();
                M3ThemeManager.uninstall(ownerContent);
                M3ThemeManager.uninstall(scene);
                stage.close();
            }
        });
    }

    /// Verifies a dialog mirrors owner orientation and reduced-motion context only for its presentation lifetime.
    @Test
    void dialogMirrorsOwnerOrientationAndMotionContextWhileShowing() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            ownerContent.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3MotionSettings.setReducedMotionRequested(ownerContent, true);
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            dialog.getDialogPane().setHeaderText("Context dialog");
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
            try {
                dialog.show();
                Parent layer = Objects.requireNonNull(dialog.getDialogPane().getParent(), "dialog layer");

                assertEquals(NodeOrientation.RIGHT_TO_LEFT, layer.getNodeOrientation());
                assertTrue(M3MotionSettings.shouldReduceMotion(dialog.getDialogPane()));

                ownerContent.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                M3MotionSettings.setReducedMotionRequested(ownerContent, false);

                assertEquals(NodeOrientation.LEFT_TO_RIGHT, layer.getNodeOrientation());
                assertFalse(M3MotionSettings.shouldReduceMotion(dialog.getDialogPane()));

                M3MotionSettings.setReducedMotionRequested(ownerContent, true);
                dialog.close();

                assertEquals(NodeOrientation.INHERIT, layer.getNodeOrientation());
                assertFalse(M3MotionSettings.isReducedMotionRequested(layer));
            } finally {
                M3MotionSettings.setReducedMotionRequested(ownerContent, true);
                dialog.close();
                M3MotionSettings.setReducedMotionRequested(ownerContent, false);
                stage.close();
            }
        });
    }

    /// Verifies physical composition uses one window and preserves visible owner content below a translucent scrim.
    @Tier2Test
    @Test
    void dialogUsesTranslucentInSceneScrimWithoutDisablingOwnerWindow() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            Rectangle marker = new Rectangle(80.0, 80.0, Color.RED);
            StackPane ownerContent = new StackPane(marker);
            StackPane.setAlignment(marker, Pos.TOP_LEFT);
            ownerContent.setStyle("-fx-background-color: white;");
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 640.0, 420.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            long showingWindowCount = Window.getWindows().stream().filter(Window::isShowing).count();

            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            dialog.getDialogPane().setHeaderText("Visible dialog");
            dialog.getDialogPane().setContentText("Owner content remains visible below a Material scrim.");
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
            try {
                dialog.show();
                overlayRoot.applyCss();
                overlayRoot.layout();

                assertSame(overlayRoot, scene.getRoot());
                assertSame(ownerContent, overlayRoot.getContent());
                assertDialogAttached(dialog, scene, overlayRoot);
                assertEquals(showingWindowCount, Window.getWindows().stream().filter(Window::isShowing).count());
                assertFalse(dialog.getDialogPane().getBackground().getFills().isEmpty());
                assertEquals(
                        M3Theme.defaultTheme().colorScheme().getColor(ColorRole.SURFACE_CONTAINER_HIGH),
                        dialog.getDialogPane().getBackground().getFills().get(0).getFill(),
                        () -> "dialog container should use the active Material surface-container-high color: style="
                                + dialog.getDialogPane().getStyle()
                                + ", classes=" + dialog.getDialogPane().getStyleClass()
                                + ", stylesheets=" + dialog.getDialogPane().getStylesheets()
                                + ", parentStyle=" + Objects.requireNonNull(
                                dialog.getDialogPane().getParent(),
                                "dialog parent"
                        ).getStyle()
                );
                List<M3Scrim> scrims = descendantScrims(overlayRoot);
                assertEquals(1, scrims.size());
                M3Scrim scrim = scrims.get(0);
                assertEquals(0.32, scrim.getVisibleOpacity(), 0.001);
                assertEquals(0.32, scrim.getOpacity(), 0.001);

                WritableImage image = overlayRoot.snapshot(null, null);
                Color markerPixel = image.getPixelReader().getColor(20, 20);
                assertTrue(markerPixel.getRed() > 0.55, () -> "owner marker is hidden: " + markerPixel);
                assertTrue(markerPixel.getGreen() < 0.08, () -> "scrim did not preserve marker color: " + markerPixel);
                assertTrue(markerPixel.getBlue() < 0.08, () -> "scrim did not preserve marker color: " + markerPixel);

                dialog.close();

                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);
                assertSame(scene, ownerContent.getScene());
            } finally {
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies that hiding the owner window forcibly removes its overlay without a cancellable close request.
    @Tier2Test
    @Test
    void ownerWindowHidingForcesPresentationCleanup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            AtomicInteger closeRequests = new AtomicInteger();
            List<String> lifecycle = new ArrayList<>();
            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            dialog.getDialogPane().setHeaderText("Owned dialog");
            dialog.setOnCloseRequest(event -> {
                assertNull(event.getButtonType());
                closeRequests.incrementAndGet();
                event.consume();
            });
            dialog.setOnHiding(event -> {
                assertNull(event.getButtonType());
                lifecycle.add("hiding");
            });
            dialog.setOnHidden(event -> {
                assertNull(event.getButtonType());
                lifecycle.add("hidden");
            });
            try {
                dialog.show();

                assertTrue(dialog.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertDialogAttached(dialog, scene, overlayRoot);

                stage.hide();

                assertFalse(dialog.isShowing());
                assertNull(dialog.getDialogPane().getParent());
                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);
                assertEquals(0, closeRequests.get());
                assertEquals(List.of("hiding", "hidden"), lifecycle);

                stage.show();
                dialog.show();

                assertTrue(dialog.isShowing());

                dialog.setOnCloseRequest(null);
                dialog.close();

                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.setOnCloseRequest(null);
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies nested dialogs stack in one stable overlay root and close in top-to-bottom order.
    @Tier2Test
    @Test
    void nestedDialogsShareOneOverlayHostAndCloseInStackOrder() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 600.0, 400.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            M3Dialog first = new M3Dialog();
            first.setOwner(ownerContent);
            first.getDialogPane().setHeaderText("First");
            M3Dialog second = new M3Dialog();
            second.setOwner(ownerContent);
            second.getDialogPane().setHeaderText("Second");
            try {
                first.show();
                Parent firstLayer = assertDialogAttached(first, scene, overlayRoot);
                second.show();
                Parent secondLayer = assertDialogAttached(second, scene, overlayRoot);

                assertSame(overlayRoot, scene.getRoot());
                assertNotSame(firstLayer, secondLayer, "nested dialogs should use distinct presentation layers");
                assertEquals(2, descendantScrims(overlayRoot).size());
                assertSame(firstLayer, first.getDialogPane().getParent());
                assertSame(secondLayer, second.getDialogPane().getParent());

                second.close();

                assertTrue(first.isShowing());
                assertFalse(second.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertSame(firstLayer, first.getDialogPane().getParent());
                assertNull(second.getDialogPane().getParent());
                assertEquals(1, descendantScrims(overlayRoot).size());

                first.close();

                assertStableEmptyOverlay(first, scene, overlayRoot, ownerContent);
            } finally {
                second.close();
                first.close();
                stage.close();
            }
        });
    }

    /// Verifies that closing a lower dialog leaves focus inside the dialog that remains above it.
    @Tier2Test
    @Test
    void closingLowerDialogDoesNotStealFocusFromTopDialog() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayRootReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> ownerContentReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> ownerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog> firstDialogReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog> secondDialogReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                Stage stage = new Stage();
                M3Button owner = new M3Button("Owner");
                StackPane ownerContent = new StackPane(owner);
                M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
                Scene scene = new Scene(overlayRoot, 600.0, 400.0);
                stage.setScene(scene);
                stage.show();
                owner.requestFocus();

                M3TextField firstContent = new M3TextField("First content");
                M3Dialog first = new M3Dialog();
                first.setOwner(ownerContent);
                first.getDialogPane().setContent(firstContent);
                M3TextField secondContent = new M3TextField("Second content");
                M3Dialog second = new M3Dialog();
                second.setOwner(ownerContent);
                second.getDialogPane().setContent(secondContent);

                first.show();
                firstContent.requestFocus();
                second.show();
                secondContent.requestFocus();

                stageReference.set(stage);
                sceneReference.set(scene);
                overlayRootReference.set(overlayRoot);
                ownerContentReference.set(ownerContent);
                ownerReference.set(owner);
                firstDialogReference.set(first);
                secondDialogReference.set(second);

                assertTrue(secondContent.isFocused());

                first.close();

                assertFalse(first.isShowing());
                assertNull(first.getDialogPane().getParent());
                assertDialogAttached(second, scene, overlayRoot);
                assertTrue(secondContent.isFocused());
                assertEquals(1, descendantScrims(overlayRoot).size());

                second.close();
            });

            FxTestUtils.runOnFxThread(() -> {
                M3Button owner = Objects.requireNonNull(ownerReference.get(), "owner");
                M3Dialog second = Objects.requireNonNull(secondDialogReference.get(), "second dialog");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                M3OverlayPane overlayRoot =
                        Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                StackPane ownerContent =
                        Objects.requireNonNull(ownerContentReference.get(), "owner content");

                assertTrue(owner.isFocused(),
                        "closing dialogs out of stack order should ultimately restore original background focus");
                assertStableEmptyOverlay(second, scene, overlayRoot, ownerContent);
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3Dialog second = secondDialogReference.get();
                if (second != null) {
                    second.close();
                }
                @Nullable M3Dialog first = firstDialogReference.get();
                if (first != null) {
                    first.close();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies closing topmost dialogs restores focus through the remaining dialog and finally to owner content.
    @Tier2Test
    @Test
    void closingTopDialogsRestoresFocusInStackOrder() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayRootReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> ownerContentReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> ownerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3TextField> firstContentReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog> firstDialogReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog> secondDialogReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                Stage stage = new Stage();
                M3Button owner = new M3Button("Owner");
                StackPane ownerContent = new StackPane(owner);
                M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
                Scene scene = new Scene(overlayRoot, 600.0, 400.0);
                stage.setScene(scene);
                stage.show();
                owner.requestFocus();

                M3TextField firstContent = new M3TextField("First content");
                M3Dialog first = new M3Dialog();
                first.setOwner(ownerContent);
                first.getDialogPane().setContent(firstContent);
                first.show();
                firstContent.requestFocus();

                M3TextField secondContent = new M3TextField("Second content");
                M3Dialog second = new M3Dialog();
                second.setOwner(ownerContent);
                second.getDialogPane().setContent(secondContent);
                second.show();
                secondContent.requestFocus();

                stageReference.set(stage);
                sceneReference.set(scene);
                overlayRootReference.set(overlayRoot);
                ownerContentReference.set(ownerContent);
                ownerReference.set(owner);
                firstContentReference.set(firstContent);
                firstDialogReference.set(first);
                secondDialogReference.set(second);

                assertTrue(secondContent.isFocused());
                second.close();
                assertNull(second.getDialogPane().getParent());
                assertDialogAttached(first, scene, overlayRoot);
            });

            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                M3TextField firstContent =
                        Objects.requireNonNull(firstContentReference.get(), "first content");
                M3Dialog first = Objects.requireNonNull(firstDialogReference.get(), "first dialog");
                M3Dialog second = Objects.requireNonNull(secondDialogReference.get(), "second dialog");
                M3OverlayPane overlayRoot =
                        Objects.requireNonNull(overlayRootReference.get(), "overlay root");

                assertFalse(second.isShowing());
                assertTrue(first.isShowing());
                assertTrue(firstContent.isFocused(), "closing the top dialog should restore focus to the lower dialog");

                first.close();
                assertNull(first.getDialogPane().getParent());
                assertTrue(descendantScrims(overlayRoot).isEmpty());
            });

            FxTestUtils.runOnFxThread(() -> {
                M3Button owner = Objects.requireNonNull(ownerReference.get(), "owner");
                M3Dialog first = Objects.requireNonNull(firstDialogReference.get(), "first dialog");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                M3OverlayPane overlayRoot =
                        Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                StackPane ownerContent =
                        Objects.requireNonNull(ownerContentReference.get(), "owner content");

                assertTrue(owner.isFocused(), "closing the final dialog should restore focus to its original owner");
                assertStableEmptyOverlay(first, scene, overlayRoot, ownerContent);
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3Dialog second = secondDialogReference.get();
                if (second != null) {
                    second.close();
                }
                @Nullable M3Dialog first = firstDialogReference.get();
                if (first != null) {
                    first.close();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies Escape chooses the cancel action and Tab remains inside the active dialog surface.
    @Tier2Test
    @Test
    void keyboardFocusIsTrappedAndEscapeUsesCancelAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            M3Button outside = new M3Button("Outside");
            StackPane ownerContent = new StackPane(outside);
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();
            outside.requestFocus();

            M3TextField content = new M3TextField("Editable");
            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType acceptType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            M3Dialog dialog = new M3Dialog();
            dialog.setOwner(ownerContent);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().setAll(cancelType, acceptType);
            AtomicReference<@Nullable ButtonType> hiddenButtonType = new AtomicReference<>();
            dialog.setOnHidden(event -> hiddenButtonType.set(event.getButtonType()));
            try {
                dialog.show();
                dialog.getDialogPane().requestInitialFocus();
                Node cancel = Objects.requireNonNull(dialog.getDialogPane().lookupButton(cancelType), "cancel button");
                Node accept = Objects.requireNonNull(dialog.getDialogPane().lookupButton(acceptType), "accept button");

                content.requestFocus();
                content.fireEvent(keyPressed(KeyCode.TAB));
                assertTrue(cancel.isFocused());

                cancel.fireEvent(keyPressed(KeyCode.TAB));
                assertTrue(accept.isFocused());

                accept.fireEvent(keyPressed(KeyCode.TAB));
                assertTrue(content.isFocused());
                assertFalse(outside.isFocused());

                KeyEvent controlTab = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "",
                        "",
                        KeyCode.TAB,
                        false,
                        true,
                        false,
                        false
                );
                content.fireEvent(controlTab);
                assertFalse(controlTab.isConsumed(), "Ctrl+Tab should remain available to application shortcuts");

                KeyEvent altF6 = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "",
                        "",
                        KeyCode.F6,
                        false,
                        false,
                        true,
                        false
                );
                content.fireEvent(altF6);
                assertFalse(altF6.isConsumed(), "Alt+F6 should remain available to platform shortcuts");

                content.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(dialog.isShowing());
                assertSame(cancelType, hiddenButtonType.get());
                assertStableEmptyOverlay(dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.close();
                stage.close();
            }
        });
    }

    /// Creates an unmodified key-press event for keyboard lifecycle tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /// Creates a primary mouse-click event for scrim dismissal tests.
    private static MouseEvent primaryMouseClick() {
        return new MouseEvent(
                MouseEvent.MOUSE_CLICKED,
                10.0,
                10.0,
                10.0,
                10.0,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                null
        );
    }

    /// Creates a stable Material overlay root containing one ordinary application content node.
    private static M3OverlayPane createOverlayRoot(Node content) {
        M3OverlayPane overlayRoot = new M3OverlayPane();
        overlayRoot.setContent(Objects.requireNonNull(content, "content"));
        return overlayRoot;
    }

    /// Verifies a dialog is detached while its stable overlay root and ordinary content remain installed.
    private static void assertStableEmptyOverlay(
            M3Dialog dialog,
            Scene scene,
            M3OverlayPane overlayRoot,
            Node ownerContent
    ) {
        assertFalse(dialog.isShowing());
        assertNull(dialog.getDialogPane().getParent());
        assertSame(overlayRoot, scene.getRoot());
        assertSame(ownerContent, overlayRoot.getContent());
        assertSame(overlayRoot, ownerContent.getParent());
        assertTrue(descendantScrims(overlayRoot).isEmpty());
    }

    /// Verifies that a showing dialog pane is attached within the stable owner scene and returns its presentation parent.
    private static Parent assertDialogAttached(M3Dialog dialog, Scene scene, M3OverlayPane overlayRoot) {
        assertTrue(dialog.isShowing());
        assertSame(overlayRoot, scene.getRoot());
        M3DialogPane pane = dialog.getDialogPane();
        assertSame(scene, pane.getScene());
        Parent presentationParent = assertInstanceOf(Parent.class, pane.getParent());
        assertTrue(isNodeOrDescendant(overlayRoot, pane),
                "a showing dialog pane should remain inside its stable overlay root");
        return presentationParent;
    }

    /// Returns whether the candidate is the supplied root or belongs to its descendant subtree.
    private static boolean isNodeOrDescendant(Node root, Node candidate) {
        Node current = candidate;
        while (true) {
            if (current == root) {
                return true;
            }
            @Nullable Parent parent = current.getParent();
            if (parent == null) {
                return false;
            }
            current = parent;
        }
    }

    /// Returns the rendered Material container color of a visible dialog.
    private static Color renderedDialogSurface(M3Dialog dialog) {
        M3DialogPane pane = dialog.getDialogPane();
        Parent sceneRoot = Objects.requireNonNull(pane.getScene(), "dialog scene").getRoot();
        sceneRoot.applyCss();
        sceneRoot.layout();
        pane.applyCss();
        pane.layout();
        assertFalse(pane.getBackground().getFills().isEmpty());
        return assertInstanceOf(Color.class, pane.getBackground().getFills().get(0).getFill());
    }

    /// Returns the Material dialog container color generated by a theme.
    private static Color dialogSurfaceColor(M3Theme theme) {
        return theme.colorScheme().getColor(ColorRole.SURFACE_CONTAINER_HIGH);
    }

    /// Returns all scrim descendants, including a supplied scrim root.
    private static List<M3Scrim> descendantScrims(Node root) {
        ArrayList<M3Scrim> matches = new ArrayList<>();
        collectDescendantScrims(root, matches);
        return matches;
    }

    /// Collects scrim descendants recursively in scene-graph order.
    private static void collectDescendantScrims(Node node, List<M3Scrim> matches) {
        if (node instanceof M3Scrim scrim) {
            matches.add(scrim);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectDescendantScrims(child, matches);
            }
        }
    }
}
