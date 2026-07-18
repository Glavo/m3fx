// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

    /// Verifies that showing requires a configured owner attached to a visible window.
    @Test
    void showRejectsMissingOrDetachedOwner() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Dialog<Void> dialog = new M3Dialog<>();

            assertThrows(IllegalStateException.class, dialog::show);

            StackPane detachedRoot = new StackPane();
            dialog.setOwner(detachedRoot);

            assertThrows(IllegalStateException.class, dialog::show);
            assertFalse(dialog.isShowing());
            assertNull(dialog.getResult());
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
            StackPane root = new StackPane(pane);
            new Scene(root, 520.0, 340.0);
            root.applyCss();

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
            StackPane ownerRoot = new StackPane(new M3Button("Owner"), alternateOwner);
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            M3Dialog<Void> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            AtomicInteger showingCalls = new AtomicInteger();
            dialog.setOnShowing(event -> {
                showingCalls.incrementAndGet();
                dialog.show();
                assertThrows(IllegalStateException.class, () -> dialog.setOwner(alternateOwner));
                assertThrows(IllegalStateException.class, dialog::showAndWait);
            });

            try {
                dialog.show();

                assertTrue(dialog.isShowing());
                assertEquals(1, showingCalls.get());
                assertSame(ownerRoot, dialog.getOwner());
                assertSame(scene, dialog.getDialogPane().getScene());
            } finally {
                dialog.setOnShowing(null);
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies action-event cancellation, close-request cancellation, result conversion, and lifecycle order.
    @Test
    void actionButtonsDriveCancellableLifecycleAndResultConversion() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerRoot = new StackPane(new M3Button("Owner"));
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType acceptType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            M3Dialog<String> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            dialog.getDialogPane().setHeaderText("Confirm action");
            dialog.getDialogPane().getButtonTypes().setAll(cancelType, acceptType);
            dialog.setResultConverter(type -> type == acceptType ? "accepted" : null);
            List<String> lifecycle = new ArrayList<>();
            AtomicInteger closeRequests = new AtomicInteger();
            dialog.setOnShowing(event -> lifecycle.add("showing"));
            dialog.setOnShown(event -> lifecycle.add("shown"));
            dialog.setOnCloseRequest(event -> {
                lifecycle.add("close-request");
                if (closeRequests.getAndIncrement() == 0) {
                    event.consume();
                }
            });
            dialog.setOnHiding(event -> lifecycle.add("hiding"));
            dialog.setOnHidden(event -> lifecycle.add("hidden"));

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
                assertNull(dialog.getResult());
                assertEquals(List.of("showing", "shown", "close-request"), lifecycle);

                accept.fire();

                assertFalse(dialog.isShowing());
                assertEquals("accepted", dialog.getResult());
                assertEquals(
                        List.of("showing", "shown", "close-request", "close-request", "hiding", "hidden"),
                        lifecycle
                );
                assertSame(ownerRoot, scene.getRoot());
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
            StackPane ownerRoot = new StackPane(new M3Button("Owner"));
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            AtomicInteger closeRequests = new AtomicInteger();
            M3Dialog<Void> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            dialog.setOnCloseRequest(event -> {
                if (closeRequests.incrementAndGet() == 1) {
                    event.consume();
                }
            });
            try {
                assertTrue(dialog.isDismissOnScrimClick());
                dialog.show();
                M3Scrim scrim = descendantScrims(scene.getRoot()).get(0);

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
                assertSame(ownerRoot, scene.getRoot());
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
        AtomicReference<@Nullable StackPane> ownerRootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog<Void>> dialogReference = new AtomicReference<>();
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
                        StackPane ownerRoot = new StackPane(new M3Button("Owner"));
                        Scene scene = new Scene(ownerRoot, 520.0, 340.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();

                        M3Dialog<Void> dialog = new M3Dialog<>();
                        dialog.setOwner(ownerRoot);
                        dialog.getDialogPane().setHeaderText("Animated dialog");
                        dialog.show();

                        stageReference.set(stage);
                        ownerRootReference.set(ownerRoot);
                        dialogReference.set(dialog);
                        scrimReference.set(descendantScrims(scene.getRoot()).get(0));
                    },
                    () -> {
                        M3Dialog<Void> dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
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
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Dialog<Void> dialog = dialogReference.get();
                        return dialog != null && !dialog.isShowing();
                    },
                    () -> {
                    },
                    () -> {
                        Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                        StackPane ownerRoot = Objects.requireNonNull(ownerRootReference.get(), "owner root");
                        M3Dialog<Void> dialog = Objects.requireNonNull(dialogReference.get(), "dialog");

                        assertTrue(observedIntermediateOpacity.get(),
                                "scrim exit should include at least one intermediate opacity");
                        assertTrue(minimumAttachedOpacity.get() <= 0.001,
                                () -> "overlay detached before scrim reached zero opacity: "
                                        + minimumAttachedOpacity.get());
                        assertNull(dialog.getDialogPane().getParent());
                        assertSame(ownerRoot, stage.getScene().getRoot());
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                M3Dialog<Void> dialog = dialogReference.get();
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

    /// Verifies that blocking presentation returns a converted action result and restores the exact scene root.
    @Tier2Test
    @Test
    void showAndWaitReturnsConvertedResultAndRestoresScene() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerRoot = new StackPane(new M3Button("Owner"));
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            ButtonType acceptType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            M3Dialog<String> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, acceptType);
            dialog.setResultConverter(type -> type == acceptType ? "accepted" : null);
            dialog.setOnShown(event -> Platform.runLater(() -> {
                M3Button accept = assertInstanceOf(
                        M3Button.class,
                        Objects.requireNonNull(dialog.getDialogPane().lookupButton(acceptType), "accept button")
                );
                accept.fire();
            }));

            try {
                assertEquals("accepted", dialog.showAndWait());
                assertEquals("accepted", dialog.getResult());
                assertFalse(dialog.isShowing());
                assertNull(dialog.getDialogPane().getParent());
                assertSame(ownerRoot, scene.getRoot());
            } finally {
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies lifecycle-handler failures leave either an untouched scene or a usable visible dialog.
    @Tier2Test
    @Test
    void lifecycleFailuresRollbackPresentationState() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerRoot = new StackPane(new M3Button("Owner"));
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            ButtonType acceptType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            M3Dialog<String> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, acceptType);
            dialog.setResultConverter(type -> type == acceptType ? "accepted" : null);
            RuntimeException showingFailure = new RuntimeException("showing failure");
            RuntimeException shownFailure = new RuntimeException("shown failure");
            RuntimeException hidingFailure = new RuntimeException("hiding failure");
            RuntimeException hiddenFailure = new RuntimeException("hidden failure");

            try {
                dialog.setOnShowing(event -> {
                    throw showingFailure;
                });
                assertSame(showingFailure, assertThrows(RuntimeException.class, dialog::show));
                assertHiddenWithOriginalRoot(dialog, scene, ownerRoot);

                dialog.setOnShowing(null);
                dialog.setOnShown(event -> {
                    throw shownFailure;
                });
                assertSame(shownFailure, assertThrows(RuntimeException.class, dialog::show));
                assertHiddenWithOriginalRoot(dialog, scene, ownerRoot);

                dialog.setOnShown(null);
                dialog.show();
                dialog.setResult("previous");
                dialog.setOnHiding(event -> {
                    throw hidingFailure;
                });
                M3Button accept = assertInstanceOf(
                        M3Button.class,
                        Objects.requireNonNull(dialog.getDialogPane().lookupButton(acceptType), "accept button")
                );
                assertSame(hidingFailure, assertThrows(RuntimeException.class, accept::fire));
                assertTrue(dialog.isShowing());
                assertEquals("previous", dialog.getResult());
                assertNotSame(ownerRoot, scene.getRoot());
                assertSame(scene, dialog.getDialogPane().getScene());

                dialog.setOnHiding(null);
                dialog.setOnHidden(event -> {
                    throw hiddenFailure;
                });
                assertSame(hiddenFailure, assertThrows(RuntimeException.class, accept::fire));
                assertEquals("accepted", dialog.getResult());
                assertHiddenWithOriginalRoot(dialog, scene, ownerRoot);
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

    /// Verifies explicit and inherited theme changes restyle a visible dialog without replacing its pane.
    @Tier2Test
    @Test
    void dialogThemeContextUpdatesWithoutRecreatingPane() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerRoot = new StackPane(new M3Button("Owner"));
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            M3Theme inheritedTheme = M3Theme.defaultTheme();
            M3Theme explicitDarkTheme = M3Theme.fromSeed(Color.web("#006a6a"), Brightness.DARK);
            M3Theme explicitLightTheme = M3Theme.fromSeed(Color.web("#7d5260"));
            M3ThemeManager.install(scene, inheritedTheme);
            stage.setScene(scene);
            stage.show();

            M3Dialog<Void> dialog = new M3Dialog<>();
            M3DialogPane pane = dialog.getDialogPane();
            dialog.setOwner(ownerRoot);
            dialog.setTheme(explicitDarkTheme);
            pane.setHeaderText("Themed dialog");
            pane.getButtonTypes().setAll(ButtonType.OK);
            try {
                dialog.show();

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(explicitDarkTheme), renderedDialogSurface(dialog));

                dialog.setTheme(explicitLightTheme);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(explicitLightTheme), renderedDialogSurface(dialog));

                dialog.setTheme(null);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(inheritedTheme), renderedDialogSurface(dialog));
            } finally {
                dialog.close();
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
            StackPane ownerRoot = new StackPane(marker);
            StackPane.setAlignment(marker, Pos.TOP_LEFT);
            ownerRoot.setStyle("-fx-background-color: white;");
            Scene scene = new Scene(ownerRoot, 640.0, 420.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            long showingWindowCount = Window.getWindows().stream().filter(Window::isShowing).count();

            M3Dialog<ButtonType> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            dialog.getDialogPane().setHeaderText("Visible dialog");
            dialog.getDialogPane().setContentText("Owner content remains visible below a Material scrim.");
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
            try {
                dialog.show();
                Parent overlayRoot = scene.getRoot();
                overlayRoot.applyCss();
                overlayRoot.layout();

                assertNotSame(ownerRoot, overlayRoot);
                assertSame(scene, dialog.getDialogPane().getScene());
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

                assertSame(ownerRoot, scene.getRoot());
                assertSame(scene, ownerRoot.getScene());
                assertFalse(dialog.isShowing());
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
            StackPane ownerRoot = new StackPane(new M3Button("Owner"));
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            AtomicInteger closeRequests = new AtomicInteger();
            List<String> lifecycle = new ArrayList<>();
            M3Dialog<Void> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            dialog.getDialogPane().setHeaderText("Owned dialog");
            dialog.setOnCloseRequest(event -> {
                closeRequests.incrementAndGet();
                event.consume();
            });
            dialog.setOnHiding(event -> lifecycle.add("hiding"));
            dialog.setOnHidden(event -> lifecycle.add("hidden"));
            try {
                dialog.show();

                assertTrue(dialog.isShowing());
                assertNotSame(ownerRoot, scene.getRoot());

                stage.hide();

                assertFalse(dialog.isShowing());
                assertNull(dialog.getDialogPane().getParent());
                assertSame(ownerRoot, scene.getRoot());
                assertEquals(0, closeRequests.get());
                assertEquals(List.of("hiding", "hidden"), lifecycle);

                stage.show();
                dialog.show();

                assertTrue(dialog.isShowing());

                dialog.setOnCloseRequest(null);
                dialog.close();

                assertFalse(dialog.isShowing());
                assertSame(ownerRoot, scene.getRoot());
            } finally {
                dialog.setOnCloseRequest(null);
                dialog.close();
                stage.close();
            }
        });
    }

    /// Verifies nested dialogs stack in one scene and restore the root only after the final layer closes.
    @Tier2Test
    @Test
    void nestedDialogsShareOneOverlayHostAndCloseInStackOrder() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerRoot = new StackPane(new M3Button("Owner"));
            StackPane.setAlignment(ownerRoot, Pos.BOTTOM_RIGHT);
            Scene scene = new Scene(ownerRoot, 600.0, 400.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            M3Dialog<Void> first = new M3Dialog<>();
            first.setOwner(ownerRoot);
            first.getDialogPane().setHeaderText("First");
            M3Dialog<Void> second = new M3Dialog<>();
            second.setOwner(ownerRoot);
            second.getDialogPane().setHeaderText("Second");
            try {
                first.show();
                Parent overlayRoot = scene.getRoot();
                second.show();

                assertSame(overlayRoot, scene.getRoot());
                assertEquals(2, descendantScrims(overlayRoot).size());
                assertSame(scene, first.getDialogPane().getScene());
                assertSame(scene, second.getDialogPane().getScene());

                second.close();

                assertTrue(first.isShowing());
                assertFalse(second.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertEquals(1, descendantScrims(overlayRoot).size());

                first.close();

                assertSame(ownerRoot, scene.getRoot());
                assertEquals(Pos.BOTTOM_RIGHT, StackPane.getAlignment(ownerRoot));
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
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            M3Button owner = new M3Button("Owner");
            StackPane ownerRoot = new StackPane(owner);
            Scene scene = new Scene(ownerRoot, 600.0, 400.0);
            stage.setScene(scene);
            stage.show();
            owner.requestFocus();

            M3TextField firstContent = new M3TextField("First content");
            M3Dialog<Void> first = new M3Dialog<>();
            first.setOwner(ownerRoot);
            first.getDialogPane().setContent(firstContent);
            M3TextField secondContent = new M3TextField("Second content");
            M3Dialog<Void> second = new M3Dialog<>();
            second.setOwner(ownerRoot);
            second.getDialogPane().setContent(secondContent);
            try {
                first.show();
                firstContent.requestFocus();
                second.show();
                secondContent.requestFocus();
                Parent overlayRoot = scene.getRoot();

                assertTrue(secondContent.isFocused());

                first.close();

                assertFalse(first.isShowing());
                assertTrue(second.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertTrue(secondContent.isFocused());
                assertEquals(1, descendantScrims(overlayRoot).size());

                second.close();

                assertSame(ownerRoot, scene.getRoot());
            } finally {
                second.close();
                first.close();
                stage.close();
            }
        });
    }

    /// Verifies Escape chooses the cancel action and Tab remains inside the active dialog surface.
    @Tier2Test
    @Test
    void keyboardFocusIsTrappedAndEscapeUsesCancelAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            M3Button outside = new M3Button("Outside");
            StackPane ownerRoot = new StackPane(outside);
            Scene scene = new Scene(ownerRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();
            outside.requestFocus();

            M3TextField content = new M3TextField("Editable");
            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType acceptType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            M3Dialog<ButtonType> dialog = new M3Dialog<>();
            dialog.setOwner(ownerRoot);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().setAll(cancelType, acceptType);
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
                assertSame(cancelType, dialog.getResult());
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

    /// Verifies a dialog is detached and the exact owner root is active after presentation cleanup.
    private static void assertHiddenWithOriginalRoot(M3Dialog<?> dialog, Scene scene, Parent ownerRoot) {
        assertFalse(dialog.isShowing());
        assertNull(dialog.getDialogPane().getParent());
        assertSame(ownerRoot, scene.getRoot());
    }

    /// Returns the rendered Material container color of a visible dialog.
    private static Color renderedDialogSurface(M3Dialog<?> dialog) {
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
