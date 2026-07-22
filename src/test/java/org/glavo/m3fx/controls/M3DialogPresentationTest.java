// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
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
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3OverlayDialogPresentation;
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

    /// Verifies that dialog lifecycle events use the ordinary JavaFX capture and bubble dispatch phases.
    @Test
    void lifecycleEventsUseJavaFxEventDispatchChain() {
        M3Dialog dialog = new M3Dialog();
        M3DialogHandle handle = detachedHandle(dialog);
        List<String> order = new ArrayList<>();
        AtomicReference<@Nullable M3DialogEvent> observedEvent = new AtomicReference<>();

        EventHandler<M3DialogEvent> showingFilter = event -> {
            order.add("showing-filter");
            observedEvent.set(event);
        };
        EventHandler<M3DialogEvent> anyFilter = event -> order.add("any-filter");
        EventHandler<M3DialogEvent> showingHandler = event -> order.add("showing-handler");
        EventHandler<M3DialogEvent> anyHandler = event -> order.add("any-handler");

        dialog.addEventFilter(M3DialogEvent.SHOWING, showingFilter);
        dialog.addEventFilter(M3DialogEvent.ANY, anyFilter);
        dialog.addEventHandler(M3DialogEvent.SHOWING, showingHandler);
        dialog.addEventHandler(M3DialogEvent.SHOWING, showingHandler);
        dialog.addEventHandler(M3DialogEvent.ANY, anyHandler);
        dialog.setOnShowing(event -> order.add("showing-property"));

        M3DialogEvent showingEvent = new M3DialogEvent(dialog, handle, M3DialogEvent.SHOWING, null);
        Event.fireEvent(dialog, showingEvent);

        assertSame(showingEvent, observedEvent.get());
        assertSame(dialog, showingEvent.getSource());
        assertSame(dialog, showingEvent.getTarget());
        assertSame(dialog, showingEvent.getDialog());
        assertSame(handle, showingEvent.getHandle());
        assertEquals(
                List.of("showing-filter", "any-filter", "showing-handler", "showing-property", "any-handler"),
                order
        );

        dialog.removeEventFilter(M3DialogEvent.SHOWING, showingFilter);
        dialog.removeEventFilter(M3DialogEvent.ANY, anyFilter);
        dialog.removeEventHandler(M3DialogEvent.SHOWING, showingHandler);
        dialog.removeEventHandler(M3DialogEvent.ANY, anyHandler);
        dialog.setOnShowing(null);
        order.clear();

        Event.fireEvent(dialog, new M3DialogEvent(dialog, handle, M3DialogEvent.SHOWING, null));

        assertTrue(order.isEmpty());

        dialog.addEventFilter(M3DialogEvent.CLOSE_REQUEST, Event::consume);
        dialog.addEventHandler(M3DialogEvent.CLOSE_REQUEST, event -> order.add("close-handler"));
        dialog.setOnCloseRequest(event -> order.add("close-property"));
        M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
        cancelAction.setCancelButton(true);
        M3DialogEvent closeEvent =
                new M3DialogEvent(dialog, handle, M3DialogEvent.CLOSE_REQUEST, cancelAction);

        Event.fireEvent(dialog, closeEvent);

        assertTrue(closeEvent.isConsumed());
        assertTrue(order.isEmpty());
    }

    /// Verifies that singleton lifecycle setters and their lazily created properties share one manager-owned value.
    @Test
    void lifecycleHandlerPropertiesShareEventManagerStorage() {
        M3Dialog dialog = new M3Dialog();
        M3DialogHandle handle = detachedHandle(dialog);
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger secondCalls = new AtomicInteger();
        AtomicInteger propertyChanges = new AtomicInteger();
        EventHandler<M3DialogEvent> firstHandler = event -> firstCalls.incrementAndGet();
        EventHandler<M3DialogEvent> secondHandler = event -> secondCalls.incrementAndGet();

        assertNull(dialog.getOnShowing());
        dialog.setOnShowing(firstHandler);
        assertSame(firstHandler, dialog.getOnShowing());

        ObjectProperty<@Nullable EventHandler<M3DialogEvent>> property = dialog.onShowingProperty();
        assertSame(dialog, property.getBean());
        assertEquals("onShowing", property.getName());
        assertSame(firstHandler, property.get());
        assertSame(property, dialog.onShowingProperty());
        property.addListener((observable, oldHandler, newHandler) -> propertyChanges.incrementAndGet());

        dialog.setOnShowing(secondHandler);
        assertSame(secondHandler, property.get());
        Event.fireEvent(dialog, new M3DialogEvent(dialog, handle, M3DialogEvent.SHOWING, null));
        assertEquals(0, firstCalls.get());
        assertEquals(1, secondCalls.get());

        SimpleObjectProperty<@Nullable EventHandler<M3DialogEvent>> source =
                new SimpleObjectProperty<>(firstHandler);
        property.bind(source);
        assertSame(firstHandler, dialog.getOnShowing());
        assertThrows(RuntimeException.class, () -> dialog.setOnShowing(secondHandler));
        Event.fireEvent(dialog, new M3DialogEvent(dialog, handle, M3DialogEvent.SHOWING, null));
        assertEquals(1, firstCalls.get());

        source.set(secondHandler);
        assertSame(secondHandler, dialog.getOnShowing());
        Event.fireEvent(dialog, new M3DialogEvent(dialog, handle, M3DialogEvent.SHOWING, null));
        assertEquals(2, secondCalls.get());

        property.unbind();
        dialog.setOnShowing(null);
        assertNull(dialog.getOnShowing());
        assertNull(property.get());
        assertTrue(propertyChanges.get() >= 3);
    }

    /// Verifies that a dialog can be presented only by an overlay pane attached to a showing window.
    @Test
    void showRejectsDetachedOrHiddenHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Dialog dialog = new M3Dialog();
            StackPane ownerContent = new StackPane();
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);

            assertThrows(IllegalStateException.class, () -> overlayRoot.showDialog(dialog));

            Stage stage = new Stage();
            Scene scene = new Scene(overlayRoot, 320.0, 200.0);
            stage.setScene(scene);
            assertThrows(IllegalStateException.class, () -> overlayRoot.showDialog(dialog));

            stage.show();
            M3DialogHandle handle = overlayRoot.showDialog(dialog);
            try {
                assertTrue(handle.isShowing());
                assertSame(dialog, handle.getDialog());
                assertDialogAttached(handle, dialog, scene, overlayRoot);
            } finally {
                handle.requestClose();
                stage.close();
            }
        });
    }

    /// Verifies unconstrained height measurement keeps JavaFX's natural-width sentinel semantics.
    @Test
    void unconstrainedPreferredHeightDoesNotMeasureTextAtZeroWidth() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogPane pane = new M3DialogPane();
            pane.setHeaderText("Dialog title");
            pane.setContentText("A short supporting sentence should occupy its natural number of lines.");
            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            pane.getActions().setAll(cancelAction, okAction);
            StackPane contentRoot = new StackPane(pane);
            M3OverlayPane overlayRoot = createOverlayRoot(contentRoot);
            new Scene(overlayRoot, 520.0, 340.0);
            overlayRoot.applyCss();

            double preferredHeight = pane.prefHeight(-1.0);

            assertTrue(preferredHeight >= 120.0 && preferredHeight < 300.0,
                    () -> "unconstrained dialog height should remain compact: " + preferredHeight);
        });
    }

    /// Verifies the showing callback cannot recursively present the same dialog on either the active or another host.
    @Tier2Test
    @Test
    void showingLifecycleRejectsConcurrentPresentation() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            Stage alternateStage = new Stage();
            M3OverlayPane alternateOverlay = createOverlayRoot(new StackPane());
            alternateStage.setScene(new Scene(alternateOverlay, 320.0, 200.0));
            alternateStage.show();

            M3Dialog dialog = new M3Dialog();
            AtomicInteger showingCalls = new AtomicInteger();
            dialog.setOnShowing(event -> {
                showingCalls.incrementAndGet();
                assertNull(event.getAction());
                assertThrows(IllegalStateException.class, () -> overlayRoot.showDialog(dialog));
                assertThrows(IllegalStateException.class, () -> alternateOverlay.showDialog(dialog));
            });

            @Nullable M3DialogHandle handle = null;
            try {
                handle = overlayRoot.showDialog(dialog);

                assertTrue(handle.isShowing());
                assertSame(handle, handle.showingProperty().getBean());
                assertEquals("showing", handle.showingProperty().getName());
                assertEquals(1, showingCalls.get());
                assertSame(overlayRoot, scene.getRoot());
                assertSame(ownerContent, overlayRoot.getContent());
                assertDialogAttached(handle, dialog, scene, overlayRoot);
                assertTrue(handle.requestClose());
                assertFalse(handle.isShowing());
                assertFalse(handle.showingProperty().get());
            } finally {
                dialog.setOnShowing(null);
                if (handle != null) {
                    handle.requestClose();
                }
                alternateStage.close();
                stage.close();
            }
        });
    }

    /// Verifies action cancellation and the initiating button throughout the cancellable close lifecycle.
    @Test
    void actionButtonsDriveCancellableLifecycle() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            M3Button acceptAction = new M3Button("Accept", M3ButtonVariant.TEXT);
            acceptAction.setDefaultButton(true);
            M3Dialog dialog = new M3Dialog();
            dialog.getDialogPane().setHeaderText("Confirm action");
            dialog.getDialogPane().getActions().setAll(cancelAction, acceptAction);
            List<String> lifecycle = new ArrayList<>();
            AtomicInteger closeRequests = new AtomicInteger();
            dialog.setOnShowing(event -> {
                assertNull(event.getAction());
                lifecycle.add("showing");
            });
            dialog.setOnShown(event -> {
                assertNull(event.getAction());
                lifecycle.add("shown");
            });
            dialog.setOnCloseRequest(event -> {
                assertSame(acceptAction, event.getAction());
                lifecycle.add("close-request");
                if (closeRequests.getAndIncrement() == 0) {
                    event.consume();
                }
            });
            dialog.setOnHiding(event -> {
                assertSame(acceptAction, event.getAction());
                lifecycle.add("hiding");
            });
            dialog.setOnHidden(event -> {
                assertSame(acceptAction, event.getAction());
                lifecycle.add("hidden");
            });

            M3DialogHandle handle = overlayRoot.showDialog(dialog);
            try {
                EventHandler<ActionEvent> consumeAction = ActionEvent::consume;
                acceptAction.addEventFilter(ActionEvent.ACTION, consumeAction);
                acceptAction.fire();

                assertTrue(handle.isShowing());
                assertEquals(List.of("showing", "shown"), lifecycle);

                acceptAction.removeEventFilter(ActionEvent.ACTION, consumeAction);
                acceptAction.fire();

                assertTrue(handle.isShowing());
                assertEquals(List.of("showing", "shown", "close-request"), lifecycle);

                acceptAction.fire();

                assertFalse(handle.isShowing());
                assertEquals(
                        List.of("showing", "shown", "close-request", "close-request", "hiding", "hidden"),
                        lifecycle
                );
                assertStableEmptyOverlay(handle, dialog, scene, overlayRoot, ownerContent);
            } finally {
                handle.requestClose();
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
            dialog.setOnCloseRequest(event -> {
                assertNull(event.getAction());
                if (closeRequests.incrementAndGet() == 1) {
                    event.consume();
                }
            });
            dialog.setOnHidden(event -> assertNull(event.getAction()));
            assertTrue(dialog.isDismissOnScrimClick());
            M3DialogHandle handle = overlayRoot.showDialog(dialog);
            try {
                assertSame(overlayRoot, scene.getRoot());
                assertDialogAttached(handle, dialog, scene, overlayRoot);
                assertEquals(1, descendantScrims(overlayRoot).size());
                M3Scrim scrim = descendantScrims(overlayRoot).get(0);

                dialog.dismissOnScrimClickProperty().set(false);
                scrim.fireEvent(primaryMouseClick());

                assertTrue(handle.isShowing());
                assertEquals(0, closeRequests.get());
                assertFalse(scrim.isMouseTransparent());

                dialog.setDismissOnScrimClick(true);
                scrim.fireEvent(primaryMouseClick());

                assertTrue(handle.isShowing());
                assertEquals(1, closeRequests.get());

                scrim.fireEvent(primaryMouseClick());

                assertFalse(handle.isShowing());
                assertEquals(2, closeRequests.get());
                assertStableEmptyOverlay(handle, dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.setOnCloseRequest(null);
                handle.requestClose();
                stage.close();
            }
        });
    }

    /// Verifies an animated entrance makes the dialog surface visibly contribute to a real window.
    @Test
    void animatedEntranceRendersDialogSurface() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayRootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog> dialogReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DialogHandle> handleReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        M3Dialog dialog = dialogReference.get();
                        return dialog != null
                                && dialog.getDialogPane().getOpacity() >= 0.999
                                && dialog.getDialogPane().getScaleY() >= 0.999;
                    },
                    3,
                    () -> {
                        Stage stage = new Stage();
                        StackPane ownerContent = new StackPane(new M3Button("Owner"));
                        M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
                        Scene scene = new Scene(overlayRoot, 520.0, 340.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        M3MotionSpec shortMotion =
                                M3MotionSpec.of(Duration.millis(80.0), M3MotionEasing.LINEAR);
                        FxTestUtils.setMotionScheme(
                                overlayRoot,
                                M3MotionScheme.builder(M3MotionScheme.standard())
                                        .fastEffects(shortMotion)
                                        .defaultSpatial(shortMotion)
                                        .build()
                        );
                        stage.setScene(scene);
                        stage.show();

                        M3Dialog dialog = new M3Dialog();
                        dialog.getDialogPane().setHeaderText("Rendered dialog");
                        dialog.getDialogPane().setContentText("Visible content");
                        M3DialogHandle handle = overlayRoot.showDialog(dialog);

                        stageReference.set(stage);
                        overlayRootReference.set(overlayRoot);
                        dialogReference.set(dialog);
                        handleReference.set(handle);
                    },
                    () -> {
                        Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                        M3OverlayPane overlayRoot =
                                Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                        M3Dialog dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
                        M3DialogHandle handle =
                                Objects.requireNonNull(handleReference.get(), "dialog handle");
                        try {
                            overlayRoot.applyCss();
                            overlayRoot.layout();
                            assertDialogAttached(
                                    handle,
                                    dialog,
                                    Objects.requireNonNull(stage.getScene(), "stage scene"),
                                    overlayRoot
                            );

                            M3DialogPane pane = dialog.getDialogPane();
                            assertTrue(pane.getWidth() > 1.0);
                            assertTrue(pane.getHeight() > 1.0);
                            assertEquals(1.0, pane.getOpacity(), 0.001);
                            assertEquals(1.0, pane.getScaleY(), 0.001);

                            SnapshotParameters snapshotParameters = new SnapshotParameters();
                            snapshotParameters.setFill(Color.TRANSPARENT);
                            WritableImage image = pane.snapshot(snapshotParameters, null);
                            int centerX = (int) (image.getWidth() / 2.0);
                            int centerY = (int) (image.getHeight() / 2.0);
                            Color centerPixel = image.getPixelReader().getColor(centerX, centerY);
                            assertTrue(
                                    centerPixel.getOpacity() >= 0.99,
                                    () -> "dialog surface did not contribute visible pixels: " + centerPixel
                            );
                        } finally {
                            handle.requestClose();
                            stage.close();
                        }
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                M3DialogHandle handle = handleReference.get();
                if (handle != null) {
                    handle.requestClose();
                }
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies animated dismissal retains the overlay until content effects, spatial motion, and scrim fade settle.
    @Tier2Test
    @Test
    void dialogWaitsForPresentationMotionBeforeRemovingOverlay() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> ownerContentReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayRootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Dialog> dialogReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DialogHandle> handleReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Scrim> scrimReference = new AtomicReference<>();
        AtomicBoolean observedIntermediateScrimOpacity = new AtomicBoolean();
        AtomicBoolean observedIntermediatePaneOpacity = new AtomicBoolean();
        AtomicBoolean observedIntermediatePaneScale = new AtomicBoolean();
        AtomicReference<Double> minimumAttachedOpacity = new AtomicReference<>(Double.POSITIVE_INFINITY);
        AtomicReference<Double> minimumAttachedScale = new AtomicReference<>(Double.POSITIVE_INFINITY);

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Scrim scrim = scrimReference.get();
                        M3Dialog dialog = dialogReference.get();
                        return scrim != null
                                && dialog != null
                                && scrim.getOpacity() >= scrim.getVisibleOpacity() - 0.001
                                && dialog.getDialogPane().getOpacity() >= 0.999
                                && dialog.getDialogPane().getScaleY() >= 0.999;
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
                        dialog.getDialogPane().setHeaderText("Animated dialog");
                        M3DialogHandle handle = overlayRoot.showDialog(dialog);

                        stageReference.set(stage);
                        ownerContentReference.set(ownerContent);
                        overlayRootReference.set(overlayRoot);
                        dialogReference.set(dialog);
                        handleReference.set(handle);
                        scrimReference.set(descendantScrims(overlayRoot).get(0));
                    },
                    () -> {
                        M3Dialog dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
                        M3DialogHandle handle = Objects.requireNonNull(handleReference.get(), "dialog handle");
                        M3Scrim scrim = Objects.requireNonNull(scrimReference.get(), "scrim");
                        scrim.opacityProperty().addListener((observable, oldOpacity, opacity) -> {
                            if (dialog.getDialogPane().getParent() == null) {
                                return;
                            }
                            double value = opacity.doubleValue();
                            minimumAttachedOpacity.accumulateAndGet(value, Math::min);
                            if (value > 0.001 && value < scrim.getVisibleOpacity() - 0.001) {
                                observedIntermediateScrimOpacity.set(true);
                            }
                        });
                        M3DialogPane pane = dialog.getDialogPane();
                        pane.opacityProperty().addListener((observable, oldOpacity, opacity) -> {
                            if (pane.getParent() != null && opacity.doubleValue() > 0.001 && opacity.doubleValue() < 0.999) {
                                observedIntermediatePaneOpacity.set(true);
                            }
                        });
                        pane.scaleYProperty().addListener((observable, oldScale, scale) -> {
                            if (pane.getParent() == null) {
                                return;
                            }
                            double value = scale.doubleValue();
                            minimumAttachedScale.accumulateAndGet(value, Math::min);
                            if (value > 0.921 && value < 0.999) {
                                observedIntermediatePaneScale.set(true);
                            }
                        });

                        assertTrue(handle.requestClose());

                        assertTrue(handle.isShowing(), "animated close should retain the overlay until motion settles");
                        M3OverlayPane overlayRoot =
                                Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                        assertDialogAttached(
                                handle,
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
                        M3DialogHandle handle = handleReference.get();
                        return handle != null && !handle.isShowing();
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
                        M3DialogHandle handle = Objects.requireNonNull(handleReference.get(), "dialog handle");

                        assertTrue(observedIntermediateScrimOpacity.get(),
                                "scrim exit should include at least one intermediate opacity");
                        assertTrue(observedIntermediatePaneOpacity.get(),
                                "dialog exit should include an intermediate effects value");
                        assertTrue(observedIntermediatePaneScale.get(),
                                "dialog exit should include an intermediate vertical spatial value");
                        assertTrue(minimumAttachedOpacity.get() <= 0.001,
                                () -> "overlay detached before scrim reached zero opacity: "
                                        + minimumAttachedOpacity.get());
                        assertTrue(minimumAttachedScale.get() <= 0.921,
                                () -> "overlay detached before dialog spatial motion settled: "
                                        + minimumAttachedScale.get());
                        assertEquals(1.0, dialog.getDialogPane().getOpacity(), 0.0001);
                        assertEquals(1.0, dialog.getDialogPane().getScaleY(), 0.0001);
                        assertNull(dialog.getDialogPane().getParent());
                        assertStableEmptyOverlay(
                                handle,
                                dialog,
                                Objects.requireNonNull(stage.getScene(), "stage scene"),
                                overlayRoot,
                                ownerContent
                        );
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                M3DialogHandle handle = handleReference.get();
                if (handle != null) {
                    handle.requestClose();
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

            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            M3Button acceptAction = new M3Button("Accept", M3ButtonVariant.TEXT);
            acceptAction.setDefaultButton(true);
            M3Dialog dialog = new M3Dialog();
            dialog.getDialogPane().getActions().setAll(cancelAction, acceptAction);
            RuntimeException showingFailure = new RuntimeException("showing failure");
            RuntimeException shownFailure = new RuntimeException("shown failure");
            RuntimeException hidingFailure = new RuntimeException("hiding failure");
            RuntimeException hiddenFailure = new RuntimeException("hidden failure");

            @Nullable M3DialogHandle handle = null;
            try {
                dialog.setOnShowing(event -> {
                    assertNull(event.getAction());
                    throw showingFailure;
                });
                assertSame(showingFailure,
                        assertThrows(RuntimeException.class, () -> overlayRoot.showDialog(dialog)));
                assertStableEmptyOverlay(null, dialog, scene, overlayRoot, ownerContent);

                dialog.setOnShowing(null);
                dialog.setOnShown(event -> {
                    assertNull(event.getAction());
                    throw shownFailure;
                });
                assertSame(shownFailure,
                        assertThrows(RuntimeException.class, () -> overlayRoot.showDialog(dialog)));
                assertStableEmptyOverlay(null, dialog, scene, overlayRoot, ownerContent);

                dialog.setOnShown(null);
                handle = overlayRoot.showDialog(dialog);
                dialog.setOnHiding(event -> {
                    assertSame(acceptAction, event.getAction());
                    throw hidingFailure;
                });
                assertSame(hidingFailure, assertThrows(RuntimeException.class, acceptAction::fire));
                assertTrue(handle.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertDialogAttached(handle, dialog, scene, overlayRoot);

                dialog.setOnHiding(event -> assertSame(acceptAction, event.getAction()));
                dialog.setOnHidden(event -> {
                    assertSame(acceptAction, event.getAction());
                    throw hiddenFailure;
                });
                assertSame(hiddenFailure, assertThrows(RuntimeException.class, acceptAction::fire));
                assertStableEmptyOverlay(handle, dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.setOnShowing(null);
                dialog.setOnShown(null);
                dialog.setOnHiding(null);
                dialog.setOnHidden(null);
                if (handle != null) {
                    handle.requestClose();
                }
                stage.close();
            }
        });
    }

    /// Verifies inherited scene and host theme changes restyle a visible dialog without replacing its pane.
    @Test
    void dialogTracksHostThemeContextWithoutRecreatingPane() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            M3Theme initialSceneTheme = M3Theme.defaultTheme();
            M3Theme replacementSceneTheme = M3Theme.fromSeed(Color.web("#386a20"));
            M3Theme initialHostTheme = M3Theme.fromSeed(Color.web("#984061"), Brightness.DARK);
            M3Theme replacementHostTheme = M3Theme.fromSeed(Color.web("#006a6a"), Brightness.DARK);
            M3ThemeManager.install(scene, initialSceneTheme);
            stage.setScene(scene);
            stage.show();

            M3Dialog dialog = new M3Dialog();
            M3DialogPane pane = dialog.getDialogPane();
            pane.setHeaderText("Themed dialog");
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            pane.getActions().setAll(okAction);
            M3DialogHandle handle = overlayRoot.showDialog(dialog);
            try {
                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(initialSceneTheme), renderedDialogSurface(dialog));

                M3ThemeManager.install(scene, replacementSceneTheme);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(replacementSceneTheme), renderedDialogSurface(dialog));

                M3ThemeManager.uninstall(scene);
                M3ThemeManager.install(overlayRoot, initialHostTheme);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(initialHostTheme), renderedDialogSurface(dialog));

                M3ThemeManager.install(overlayRoot, replacementHostTheme);

                assertSame(pane, dialog.getDialogPane());
                assertEquals(dialogSurfaceColor(replacementHostTheme), renderedDialogSurface(dialog));

                M3ThemeManager.install(scene, initialSceneTheme);

                assertEquals(dialogSurfaceColor(initialSceneTheme), renderedDialogSurface(dialog),
                        "a scene theme should remain authoritative over a local host theme");
            } finally {
                handle.requestClose();
                M3ThemeManager.uninstall(overlayRoot);
                M3ThemeManager.uninstall(scene);
                stage.close();
            }
        });
    }

    /// Verifies a dialog inherits host orientation and reduced-motion context only for its presentation lifetime.
    @Test
    void dialogInheritsHostOrientationAndMotionContextWhileShowing() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            overlayRoot.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3MotionSettings.setReducedMotionRequested(overlayRoot, true);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            M3Dialog dialog = new M3Dialog();
            dialog.getDialogPane().setHeaderText("Context dialog");
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            dialog.getDialogPane().getActions().setAll(okAction);
            M3DialogHandle handle = overlayRoot.showDialog(dialog);
            try {
                Parent layer = Objects.requireNonNull(dialog.getDialogPane().getParent(), "dialog layer");

                assertEquals(NodeOrientation.INHERIT, layer.getNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, layer.getEffectiveNodeOrientation());
                assertTrue(M3MotionSettings.shouldReduceMotion(dialog.getDialogPane()));

                overlayRoot.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                M3MotionSettings.setReducedMotionRequested(overlayRoot, false);

                assertEquals(NodeOrientation.INHERIT, layer.getNodeOrientation());
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, layer.getEffectiveNodeOrientation());
                assertFalse(M3MotionSettings.shouldReduceMotion(dialog.getDialogPane()));

                M3MotionSettings.setReducedMotionRequested(overlayRoot, true);
                handle.requestClose();

                assertEquals(NodeOrientation.INHERIT, layer.getNodeOrientation());
                assertFalse(M3MotionSettings.isReducedMotionRequested(layer));
            } finally {
                M3MotionSettings.setReducedMotionRequested(overlayRoot, true);
                handle.requestClose();
                M3MotionSettings.setReducedMotionRequested(overlayRoot, false);
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
            dialog.getDialogPane().setHeaderText("Visible dialog");
            dialog.getDialogPane().setContentText("Owner content remains visible below a Material scrim.");
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            dialog.getDialogPane().getActions().setAll(okAction);
            M3DialogHandle handle = overlayRoot.showDialog(dialog);
            try {
                overlayRoot.applyCss();
                overlayRoot.layout();

                assertSame(overlayRoot, scene.getRoot());
                assertSame(ownerContent, overlayRoot.getContent());
                assertDialogAttached(handle, dialog, scene, overlayRoot);
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

                handle.requestClose();

                assertStableEmptyOverlay(handle, dialog, scene, overlayRoot, ownerContent);
                assertSame(scene, ownerContent.getScene());
            } finally {
                handle.requestClose();
                stage.close();
            }
        });
    }

    /// Verifies that hiding the host window forcibly removes its overlay without a cancellable close request.
    @Tier2Test
    @Test
    void hostWindowHidingForcesPresentationCleanup() {
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
            dialog.getDialogPane().setHeaderText("Owned dialog");
            dialog.setOnCloseRequest(event -> {
                assertNull(event.getAction());
                closeRequests.incrementAndGet();
                event.consume();
            });
            dialog.setOnHiding(event -> {
                assertNull(event.getAction());
                lifecycle.add("hiding");
            });
            dialog.setOnHidden(event -> {
                assertNull(event.getAction());
                lifecycle.add("hidden");
            });
            @Nullable M3DialogHandle activeHandle = null;
            try {
                M3DialogHandle firstHandle = overlayRoot.showDialog(dialog);
                activeHandle = firstHandle;

                assertTrue(firstHandle.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertDialogAttached(firstHandle, dialog, scene, overlayRoot);

                stage.hide();

                assertFalse(firstHandle.isShowing());
                assertNull(dialog.getDialogPane().getParent());
                assertStableEmptyOverlay(firstHandle, dialog, scene, overlayRoot, ownerContent);
                assertEquals(0, closeRequests.get());
                assertEquals(List.of("hiding", "hidden"), lifecycle);

                stage.show();
                M3DialogHandle secondHandle = overlayRoot.showDialog(dialog);
                activeHandle = secondHandle;

                assertTrue(secondHandle.isShowing());
                assertFalse(firstHandle.requestClose(),
                        "a stale handle must not close a later presentation of the same dialog");
                assertTrue(secondHandle.isShowing());

                dialog.setOnCloseRequest(null);
                assertTrue(secondHandle.requestClose());

                assertStableEmptyOverlay(secondHandle, dialog, scene, overlayRoot, ownerContent);
            } finally {
                dialog.setOnCloseRequest(null);
                if (activeHandle != null) {
                    activeHandle.requestClose();
                }
                stage.close();
            }
        });
    }

    /// Verifies removing a host from its scene invalidates the active presentation and its retained handle.
    @Test
    void detachingHostForcesPresentationCleanup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage stage = new Stage();
            StackPane ownerContent = new StackPane(new M3Button("Owner"));
            M3OverlayPane overlayRoot = createOverlayRoot(ownerContent);
            Scene scene = new Scene(overlayRoot, 520.0, 340.0);
            stage.setScene(scene);
            stage.show();

            AtomicInteger closeRequests = new AtomicInteger();
            AtomicReference<@Nullable M3DialogHandle> eventHandle = new AtomicReference<>();
            M3Dialog dialog = new M3Dialog();
            dialog.setOnCloseRequest(event -> closeRequests.incrementAndGet());
            dialog.setOnHidden(event -> {
                assertFalse(event.getHandle().isShowing());
                eventHandle.set(event.getHandle());
            });
            M3DialogHandle handle = overlayRoot.showDialog(dialog);

            try {
                scene.setRoot(new StackPane());

                assertFalse(handle.isShowing());
                assertSame(handle, eventHandle.get());
                assertEquals(0, closeRequests.get());
                assertNull(dialog.getDialogPane().getParent());
                assertNull(overlayRoot.getScene());
                assertSame(ownerContent, overlayRoot.getContent());
                assertTrue(descendantScrims(overlayRoot).isEmpty());
                assertFalse(handle.requestClose());
            } finally {
                handle.requestClose();
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
            first.getDialogPane().setHeaderText("First");
            M3Dialog second = new M3Dialog();
            second.getDialogPane().setHeaderText("Second");
            @Nullable M3DialogHandle firstHandle = null;
            @Nullable M3DialogHandle secondHandle = null;
            try {
                firstHandle = overlayRoot.showDialog(first);
                Parent firstLayer = assertDialogAttached(firstHandle, first, scene, overlayRoot);
                secondHandle = overlayRoot.showDialog(second);
                Parent secondLayer = assertDialogAttached(secondHandle, second, scene, overlayRoot);

                assertSame(overlayRoot, scene.getRoot());
                assertNotSame(firstLayer, secondLayer, "nested dialogs should use distinct presentation layers");
                assertEquals(2, descendantScrims(overlayRoot).size());
                assertSame(firstLayer, first.getDialogPane().getParent());
                assertSame(secondLayer, second.getDialogPane().getParent());

                secondHandle.requestClose();

                assertTrue(firstHandle.isShowing());
                assertFalse(secondHandle.isShowing());
                assertSame(overlayRoot, scene.getRoot());
                assertSame(firstLayer, first.getDialogPane().getParent());
                assertNull(second.getDialogPane().getParent());
                assertEquals(1, descendantScrims(overlayRoot).size());

                firstHandle.requestClose();

                assertStableEmptyOverlay(firstHandle, first, scene, overlayRoot, ownerContent);
            } finally {
                if (secondHandle != null) {
                    secondHandle.requestClose();
                }
                if (firstHandle != null) {
                    firstHandle.requestClose();
                }
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
        AtomicReference<@Nullable M3Dialog> secondDialogReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DialogHandle> firstHandleReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DialogHandle> secondHandleReference = new AtomicReference<>();

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
                first.getDialogPane().setContent(firstContent);
                M3TextField secondContent = new M3TextField("Second content");
                M3Dialog second = new M3Dialog();
                second.getDialogPane().setContent(secondContent);

                M3DialogHandle firstHandle = overlayRoot.showDialog(first);
                firstContent.requestFocus();
                M3DialogHandle secondHandle = overlayRoot.showDialog(second);
                secondContent.requestFocus();

                stageReference.set(stage);
                sceneReference.set(scene);
                overlayRootReference.set(overlayRoot);
                ownerContentReference.set(ownerContent);
                ownerReference.set(owner);
                secondDialogReference.set(second);
                firstHandleReference.set(firstHandle);
                secondHandleReference.set(secondHandle);

                assertTrue(secondContent.isFocused());

                firstHandle.requestClose();

                assertFalse(firstHandle.isShowing());
                assertNull(first.getDialogPane().getParent());
                assertDialogAttached(secondHandle, second, scene, overlayRoot);
                assertTrue(secondContent.isFocused());
                assertEquals(1, descendantScrims(overlayRoot).size());

                secondHandle.requestClose();
            });

            FxTestUtils.runOnFxThread(() -> {
                M3Button owner = Objects.requireNonNull(ownerReference.get(), "owner");
                M3Dialog second = Objects.requireNonNull(secondDialogReference.get(), "second dialog");
                M3DialogHandle secondHandle =
                        Objects.requireNonNull(secondHandleReference.get(), "second dialog handle");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                M3OverlayPane overlayRoot =
                        Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                StackPane ownerContent =
                        Objects.requireNonNull(ownerContentReference.get(), "owner content");

                assertTrue(owner.isFocused(),
                        "closing dialogs out of stack order should ultimately restore original background focus");
                assertStableEmptyOverlay(secondHandle, second, scene, overlayRoot, ownerContent);
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3DialogHandle secondHandle = secondHandleReference.get();
                if (secondHandle != null) {
                    secondHandle.requestClose();
                }
                @Nullable M3DialogHandle firstHandle = firstHandleReference.get();
                if (firstHandle != null) {
                    firstHandle.requestClose();
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
        AtomicReference<@Nullable M3DialogHandle> firstHandleReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DialogHandle> secondHandleReference = new AtomicReference<>();

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
                first.getDialogPane().setContent(firstContent);
                M3DialogHandle firstHandle = overlayRoot.showDialog(first);
                firstContent.requestFocus();

                M3TextField secondContent = new M3TextField("Second content");
                M3Dialog second = new M3Dialog();
                second.getDialogPane().setContent(secondContent);
                M3DialogHandle secondHandle = overlayRoot.showDialog(second);
                secondContent.requestFocus();

                stageReference.set(stage);
                sceneReference.set(scene);
                overlayRootReference.set(overlayRoot);
                ownerContentReference.set(ownerContent);
                ownerReference.set(owner);
                firstContentReference.set(firstContent);
                firstDialogReference.set(first);
                firstHandleReference.set(firstHandle);
                secondHandleReference.set(secondHandle);

                assertTrue(secondContent.isFocused());
                secondHandle.requestClose();
                assertNull(second.getDialogPane().getParent());
                assertDialogAttached(firstHandle, first, scene, overlayRoot);
            });

            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                M3TextField firstContent =
                        Objects.requireNonNull(firstContentReference.get(), "first content");
                M3Dialog first = Objects.requireNonNull(firstDialogReference.get(), "first dialog");
                M3DialogHandle firstHandle =
                        Objects.requireNonNull(firstHandleReference.get(), "first dialog handle");
                M3DialogHandle secondHandle =
                        Objects.requireNonNull(secondHandleReference.get(), "second dialog handle");
                M3OverlayPane overlayRoot =
                        Objects.requireNonNull(overlayRootReference.get(), "overlay root");

                assertFalse(secondHandle.isShowing());
                assertTrue(firstHandle.isShowing());
                assertTrue(firstContent.isFocused(), "closing the top dialog should restore focus to the lower dialog");

                firstHandle.requestClose();
                assertNull(first.getDialogPane().getParent());
                assertTrue(descendantScrims(overlayRoot).isEmpty());
            });

            FxTestUtils.runOnFxThread(() -> {
                M3Button owner = Objects.requireNonNull(ownerReference.get(), "owner");
                M3Dialog first = Objects.requireNonNull(firstDialogReference.get(), "first dialog");
                M3DialogHandle firstHandle =
                        Objects.requireNonNull(firstHandleReference.get(), "first dialog handle");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                M3OverlayPane overlayRoot =
                        Objects.requireNonNull(overlayRootReference.get(), "overlay root");
                StackPane ownerContent =
                        Objects.requireNonNull(ownerContentReference.get(), "owner content");

                assertTrue(owner.isFocused(), "closing the final dialog should restore focus to its original owner");
                assertStableEmptyOverlay(firstHandle, first, scene, overlayRoot, ownerContent);
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3DialogHandle secondHandle = secondHandleReference.get();
                if (secondHandle != null) {
                    secondHandle.requestClose();
                }
                @Nullable M3DialogHandle firstHandle = firstHandleReference.get();
                if (firstHandle != null) {
                    firstHandle.requestClose();
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
            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            M3Button acceptAction = new M3Button("Accept", M3ButtonVariant.TEXT);
            acceptAction.setDefaultButton(true);
            M3Dialog dialog = new M3Dialog();
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getActions().setAll(cancelAction, acceptAction);
            AtomicReference<@Nullable M3Button> hiddenAction = new AtomicReference<>();
            dialog.setOnHidden(event -> hiddenAction.set(event.getAction()));
            M3DialogHandle handle = overlayRoot.showDialog(dialog);
            try {
                dialog.getDialogPane().requestInitialFocus();

                content.requestFocus();
                content.fireEvent(keyPressed(KeyCode.TAB));
                assertTrue(cancelAction.isFocused());
                assertTrue(cancelAction.getPseudoClassStates().contains(PseudoClass.getPseudoClass("focus-visible")));
                assertVisibleFocusIndicator(cancelAction);

                cancelAction.fireEvent(keyPressed(KeyCode.TAB));
                assertTrue(acceptAction.isFocused());
                assertFalse(cancelAction.getPseudoClassStates().contains(PseudoClass.getPseudoClass("focus-visible")));
                assertTrue(acceptAction.getPseudoClassStates().contains(PseudoClass.getPseudoClass("focus-visible")));
                assertVisibleFocusIndicator(acceptAction);

                acceptAction.fireEvent(keyPressed(KeyCode.TAB));
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

                assertFalse(handle.isShowing());
                assertSame(cancelAction, hiddenAction.get());
                assertStableEmptyOverlay(handle, dialog, scene, overlayRoot, ownerContent);
            } finally {
                handle.requestClose();
                stage.close();
            }
        });
    }

    /// Creates an unmodified key-press event for keyboard lifecycle tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /// Verifies that a keyboard-focused control materializes an opaque focus-indicator node.
    private static void assertVisibleFocusIndicator(Node control) {
        @Nullable Node indicator = control.lookup(".m3-focus-indicator");
        assertTrue(indicator != null && indicator.isVisible());
        assertEquals(1.0, indicator.getOpacity(), 0.000_001);
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

    /// Creates a detached handle for event-dispatch tests that do not install a visible presentation.
    private static M3DialogHandle detachedHandle(M3Dialog dialog) {
        M3Dialog nonNullDialog = Objects.requireNonNull(dialog, "dialog");
        return new M3DialogHandle(
                nonNullDialog,
                new M3OverlayDialogPresentation(
                        new M3OverlayPane(),
                        nonNullDialog.getDialogPane(),
                        () -> {
                        }
                )
        );
    }

    /// Verifies a dialog is detached while its stable overlay root and ordinary content remain installed.
    private static void assertStableEmptyOverlay(
            @Nullable M3DialogHandle handle,
            M3Dialog dialog,
            Scene scene,
            M3OverlayPane overlayRoot,
            Node ownerContent
    ) {
        if (handle != null) {
            assertFalse(handle.isShowing());
            assertSame(dialog, handle.getDialog());
        }
        assertNull(dialog.getDialogPane().getParent());
        assertSame(overlayRoot, scene.getRoot());
        assertSame(ownerContent, overlayRoot.getContent());
        assertSame(overlayRoot, ownerContent.getParent());
        assertTrue(descendantScrims(overlayRoot).isEmpty());
    }

    /// Verifies that a showing dialog pane is attached within the stable host scene and returns its presentation parent.
    private static Parent assertDialogAttached(
            M3DialogHandle handle,
            M3Dialog dialog,
            Scene scene,
            M3OverlayPane overlayRoot
    ) {
        assertTrue(handle.isShowing());
        assertSame(dialog, handle.getDialog());
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
