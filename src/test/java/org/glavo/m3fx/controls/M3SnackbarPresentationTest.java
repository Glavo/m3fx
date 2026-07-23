// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies snackbar rendering, interaction, focus, and accessibility through [M3OverlayPane].
@NotNullByDefault
@Tier2Test
final class M3SnackbarPresentationTest {
    /// Starts the JavaFX toolkit before presenter nodes are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that one presenter and one surface are reused while queued messages advance.
    @Test
    void onePresenterReusesItsSurfaceAcrossQueuedMessages() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar first = new M3Snackbar("Saved");
            first.setActionText("Undo");
            first.setAction(() -> {
            });
            M3Snackbar second = new M3Snackbar("Offline");
            second.setCloseButtonVisible(true);

            overlayPane.showSnackbar(first);
            overlayPane.applyCss();
            overlayPane.layout();
            Node presenter = snackbarPresenter(overlayPane);
            Region firstSurface = snackbarSurface(presenter);
            Label text = assertInstanceOf(Label.class, presenter.lookup(".m3-snackbar-text"));
            M3Button action = assertInstanceOf(M3Button.class, presenter.lookup(".m3-snackbar-action"));
            M3IconButton close = assertInstanceOf(M3IconButton.class, presenter.lookup(".m3-snackbar-close"));

            assertEquals(1, directPresenterCount(overlayPane));
            assertEquals("Saved", text.getText());
            assertEquals("Undo", action.getText());
            assertTrue(action.isManaged());
            assertFalse(close.isManaged());

            overlayPane.showSnackbar(second);
            overlayPane.applyCss();
            overlayPane.layout();

            assertEquals(1, directPresenterCount(overlayPane));
            assertSame(presenter, snackbarPresenter(overlayPane));
            assertSame(firstSurface, snackbarSurface(presenter));
            assertEquals("Saved", text.getText());
            assertTrue(action.isManaged());
            assertFalse(close.isManaged());
            assertSame(first, overlayPane.getSnackbar());
            assertEquals(List.of(second), overlayPane.getSnackbarQueue());

            overlayPane.dismissSnackbar();
            assertSame(second, overlayPane.getSnackbar());
            assertTrue(overlayPane.getSnackbarQueue().isEmpty());
        });
    }

    /// Verifies that action activation runs once, dismisses its message, and advances the FIFO queue.
    @Test
    void actionActivationDismissesAndPromotesQueuedMessage() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            AtomicInteger invocations = new AtomicInteger();
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar first = new M3Snackbar("Deleted");
            first.setActionText("Restore");
            first.setAction(invocations::incrementAndGet);
            M3Snackbar second = new M3Snackbar("Restored");
            overlayPane.showSnackbar(first);
            overlayPane.enqueueSnackbar(second);
            overlayPane.applyCss();
            overlayPane.layout();
            Node presenter = snackbarPresenter(overlayPane);
            Region surface = snackbarSurface(presenter);

            assertInstanceOf(M3Button.class, presenter.lookup(".m3-snackbar-action")).fire();

            assertEquals(1, invocations.get());
            assertSame(second, overlayPane.getSnackbar());
            assertTrue(overlayPane.isSnackbarShowing());
            assertTrue(overlayPane.getSnackbarQueue().isEmpty());
            assertSame(surface, snackbarSurface(presenter));
            assertEquals(
                    "Restored",
                    assertInstanceOf(Label.class, presenter.lookup(".m3-snackbar-text")).getText()
            );
        });
    }

    /// Verifies that the close affordance dismisses without invoking the optional action.
    @Test
    void closeAffordanceDismissesWithoutInvokingAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            AtomicInteger invocations = new AtomicInteger();
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar snackbar = new M3Snackbar("Connection lost");
            snackbar.setActionText("Retry");
            snackbar.setAction(invocations::incrementAndGet);
            snackbar.setCloseButtonVisible(true);
            overlayPane.showSnackbar(snackbar);
            overlayPane.applyCss();
            overlayPane.layout();
            Node presenter = snackbarPresenter(overlayPane);

            assertInstanceOf(M3IconButton.class, presenter.lookup(".m3-snackbar-close")).fire();

            assertEquals(0, invocations.get());
            assertNull(overlayPane.getSnackbar());
            assertFalse(overlayPane.isSnackbarShowing());
        });
    }

    /// Verifies that presenter accessibility describes only the currently rendered interactive affordances.
    @Test
    void accessibilityExposesCurrentSurfaceAndAffordancesOnly() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar current = new M3Snackbar("Saved");
            current.setActionText("Undo");
            current.setAction(() -> {
            });
            current.setCloseButtonVisible(true);
            M3Snackbar queued = new M3Snackbar("Queued");
            overlayPane.showSnackbar(current);
            overlayPane.enqueueSnackbar(queued);
            overlayPane.applyCss();
            overlayPane.layout();
            Node presenter = snackbarPresenter(overlayPane);
            Node surface = snackbarSurface(presenter);
            Node action = assertInstanceOf(Node.class, presenter.lookup(".m3-snackbar-action"));
            Node close = assertInstanceOf(Node.class, presenter.lookup(".m3-snackbar-close"));

            assertSame(surface, presenter.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertEquals(true, presenter.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            assertSame(action, presenter.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            assertEquals(2, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(action, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertSame(close, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
            assertNull(presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
            assertEquals("Saved", presenter.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(List.of(queued), overlayPane.getSnackbarQueue());
        });
    }

    /// Verifies that a modal overlay hides snackbar accessibility and restores it after dismissal.
    @Test
    void modalPresentationTemporarilySuppressesSnackbarAccessibility() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar snackbar = new M3Snackbar("Saved");
            snackbar.setActionText("Undo");
            snackbar.setAction(() -> {
            });
            overlayPane.showSnackbar(snackbar);
            overlayPane.applyCss();
            overlayPane.layout();
            Node presenter = snackbarPresenter(overlayPane);

            M3OverlayPane.OverlayHandle modalHandle = overlayPane.showModalOverlay(new Pane());
            assertNull(presenter.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertEquals(false, presenter.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            assertNull(presenter.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            assertEquals(0, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertEquals("", presenter.queryAccessibleAttribute(AccessibleAttribute.TEXT));

            assertTrue(modalHandle.hide());
            assertSame(
                    snackbarSurface(presenter),
                    presenter.queryAccessibleAttribute(AccessibleAttribute.CONTENTS)
            );
            assertEquals(true, presenter.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            assertEquals(1, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertEquals("Saved", presenter.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        });
    }

    /// Verifies accessible focus and collapse actions are routed through the single presenter.
    @Test
    void accessibleActionsFocusAndDismissCurrentMessage() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            overlayPane.setContent(new Pane());
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(overlayPane, 480.0, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                M3Snackbar snackbar = new M3Snackbar("Saved");
                snackbar.setActionText("Undo");
                snackbar.setAction(() -> {
                });
                snackbar.setCloseButtonVisible(true);
                overlayPane.showSnackbar(snackbar);
                overlayPane.applyCss();
                overlayPane.layout();
                Node presenter = snackbarPresenter(overlayPane);
                Node action = Objects.requireNonNull(
                        (Node) presenter.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE),
                        "action"
                );

                assertTrue(M3Accessible.requestAccessibleFocus(overlayPane, presenter));
                assertTrue(action.isFocused());
                snackbar.setActionText("");
                overlayPane.applyCss();
                overlayPane.layout();
                Node close = Objects.requireNonNull(
                        (Node) presenter.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE),
                        "close"
                );
                assertTrue(close.isFocused());
                presenter.executeAccessibleAction(AccessibleAction.COLLAPSE);
                assertNull(overlayPane.getSnackbar());
                assertFalse(overlayPane.isSnackbarShowing());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies current property changes update rendering, accessibility, and nullable action behavior in place.
    @Test
    void currentMessagePropertiesUpdateTheExistingSurface() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar snackbar = new M3Snackbar("Saved");
            overlayPane.showSnackbar(snackbar);
            overlayPane.applyCss();
            overlayPane.layout();
            Node presenter = snackbarPresenter(overlayPane);
            Region surface = snackbarSurface(presenter);
            Label text = assertInstanceOf(Label.class, presenter.lookup(".m3-snackbar-text"));
            M3Button action = assertInstanceOf(M3Button.class, presenter.lookup(".m3-snackbar-action"));
            M3IconButton close = assertInstanceOf(M3IconButton.class, presenter.lookup(".m3-snackbar-close"));

            snackbar.setText("Projekt gespeichert");
            snackbar.setActionText("Schließen");
            snackbar.setAction(null);
            snackbar.setCloseButtonVisible(true);
            overlayPane.applyCss();
            overlayPane.layout();

            assertSame(surface, snackbarSurface(presenter));
            assertEquals("Projekt gespeichert", text.getText());
            assertEquals("Schließen", action.getText());
            assertTrue(action.isManaged());
            assertTrue(close.isManaged());
            assertEquals("Projekt gespeichert", presenter.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(2, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

            action.fire();
            assertNull(overlayPane.getSnackbar());
            assertFalse(overlayPane.isSnackbarShowing());
        });
    }

    /// Verifies that the reusable surface mirrors text and affordances exactly once when direction changes.
    @Test
    void surfaceContentTracksLogicalEdgesAcrossRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar snackbar = new M3Snackbar("Saved");
            snackbar.setActionText("Undo");
            snackbar.setCloseButtonVisible(true);
            new Scene(overlayPane, 640.0, 240.0);
            overlayPane.showSnackbar(snackbar);
            overlayPane.applyCss();
            overlayPane.resize(640.0, 240.0);
            overlayPane.layout();

            Node presenter = snackbarPresenter(overlayPane);
            Region surface = snackbarSurface(presenter);
            Label text = assertInstanceOf(Label.class, presenter.lookup(".m3-snackbar-text"));
            M3Button action = assertInstanceOf(M3Button.class, presenter.lookup(".m3-snackbar-action"));
            M3IconButton close = assertInstanceOf(M3IconButton.class, presenter.lookup(".m3-snackbar-close"));

            Bounds leftToRightSurface = surface.localToScene(surface.getBoundsInLocal());
            Bounds leftToRightText = text.localToScene(text.getBoundsInLocal());
            Bounds leftToRightAction = action.localToScene(action.getBoundsInLocal());
            Bounds leftToRightClose = close.localToScene(close.getBoundsInLocal());
            double logicalStartGap = leftToRightText.getMinX() - leftToRightSurface.getMinX();
            assertTrue(logicalStartGap >= surface.getPadding().getLeft());
            assertTrue(leftToRightText.getMaxX() < leftToRightAction.getMinX());
            assertTrue(leftToRightAction.getMaxX() <= leftToRightClose.getMinX());

            overlayPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            overlayPane.applyCss();
            overlayPane.layout();

            Bounds rightToLeftSurface = surface.localToScene(surface.getBoundsInLocal());
            Bounds rightToLeftText = text.localToScene(text.getBoundsInLocal());
            Bounds rightToLeftAction = action.localToScene(action.getBoundsInLocal());
            Bounds rightToLeftClose = close.localToScene(close.getBoundsInLocal());
            assertEquals(
                    logicalStartGap,
                    rightToLeftSurface.getMaxX() - rightToLeftText.getMaxX(),
                    1.0
            );
            assertTrue(rightToLeftAction.getMaxX() < rightToLeftText.getMinX());
            assertTrue(rightToLeftClose.getMaxX() <= rightToLeftAction.getMinX());
        });
    }

    /// Verifies host configuration, gesture intent, affordance isolation, and mirrored swipe dismissal.
    @Test
    void swipeDismissalRespectsHostConfigurationAndGestureIntent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            overlayPane.setContent(new Pane());
            Scene scene = new Scene(overlayPane, 640.0, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());

            M3Snackbar snackbar = new M3Snackbar("Swipe interaction");
            snackbar.setActionText("Undo");
            overlayPane.showSnackbar(snackbar);
            overlayPane.applyCss();
            overlayPane.layout();

            Node presenter = snackbarPresenter(overlayPane);
            Region surface = snackbarSurface(presenter);
            Bounds surfaceBounds = surface.localToScene(surface.getBoundsInLocal());
            double centerX = surfaceBounds.getCenterX();
            double centerY = surfaceBounds.getCenterY();
            double dismissDistance = surface.getWidth() * 0.6;

            assertTrue(overlayPane.isSnackbarSwipeToDismissEnabled());
            assertSame(overlayPane, overlayPane.snackbarSwipeToDismissEnabledProperty().getBean());
            assertEquals("snackbarSwipeToDismissEnabled",
                    overlayPane.snackbarSwipeToDismissEnabledProperty().getName());

            overlayPane.setSnackbarSwipeToDismissEnabled(false);
            fireSwipe(surface, centerX, centerY, centerX + dismissDistance, centerY);
            assertSame(snackbar, overlayPane.getSnackbar());
            assertEquals(0.0, surface.getTranslateX(), 0.0);

            overlayPane.setSnackbarSwipeToDismissEnabled(true);
            fireSwipe(surface, centerX, centerY, centerX + surface.getWidth() * 0.25, centerY);
            assertSame(snackbar, overlayPane.getSnackbar());
            assertEquals(0.0, surface.getTranslateX(), 0.0);

            surface.fireEvent(primaryMouseEvent(surface, MouseEvent.MOUSE_PRESSED, centerX, centerY, true));
            surface.fireEvent(primaryMouseEvent(
                    surface,
                    MouseEvent.MOUSE_DRAGGED,
                    centerX + surface.getWidth() * 0.25,
                    centerY,
                    true
            ));
            assertTrue(surface.getTranslateX() > 0.0);
            overlayPane.setSnackbarSwipeToDismissEnabled(false);
            assertEquals(0.0, surface.getTranslateX(), 0.0);
            assertEquals(1.0, surface.getOpacity(), 0.0);

            overlayPane.setSnackbarSwipeToDismissEnabled(true);
            fireSwipe(surface, centerX, centerY, centerX + 12.0, centerY + 40.0);
            assertSame(snackbar, overlayPane.getSnackbar());
            assertEquals(0.0, surface.getTranslateX(), 0.0);

            M3Button action = assertInstanceOf(M3Button.class, presenter.lookup(".m3-snackbar-action"));
            action.setDisable(true);
            Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
            fireSwipe(
                    action,
                    actionBounds.getCenterX(),
                    actionBounds.getCenterY(),
                    actionBounds.getCenterX() + dismissDistance,
                    actionBounds.getCenterY()
            );
            assertSame(snackbar, overlayPane.getSnackbar());
            assertEquals(0.0, surface.getTranslateX(), 0.0);

            overlayPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            overlayPane.applyCss();
            overlayPane.layout();
            Bounds rightToLeftBounds = surface.localToScene(surface.getBoundsInLocal());
            fireSwipe(
                    surface,
                    rightToLeftBounds.getCenterX(),
                    rightToLeftBounds.getCenterY(),
                    rightToLeftBounds.getCenterX() - dismissDistance,
                    rightToLeftBounds.getCenterY()
            );
            assertNull(overlayPane.getSnackbar());
            assertFalse(overlayPane.isSnackbarShowing());
            assertEquals(0.0, surface.getTranslateX(), 0.0);
            assertEquals(1.0, surface.getOpacity(), 0.0);
        });
    }

    /// Verifies that an animated swipe exits horizontally and promotes the queued message.
    @Test
    void animatedSwipeDismissalAdvancesTheQueue() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> surfaceReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Snackbar> firstReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Snackbar> secondReference = new AtomicReference<>();
        AtomicBoolean observedHorizontalExit = new AtomicBoolean();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        M3OverlayPane overlay = Objects.requireNonNull(overlayReference.get(), "overlay");
                        Region surface = Objects.requireNonNull(surfaceReference.get(), "surface");
                        return overlay.getSnackbar() == firstReference.get()
                                && overlay.isSnackbarShowing()
                                && Math.abs(surface.getTranslateY()) <= 1.0e-6
                                && Math.abs(surface.getOpacity() - 1.0) <= 1.0e-6;
                    },
                    2,
                    () -> {
                        M3OverlayPane overlayPane = overlayPane();
                        M3MotionSpec motionSpec =
                                M3MotionSpec.of(Duration.millis(400.0), M3MotionEasing.LINEAR);
                        FxTestUtils.setMotionScheme(
                                overlayPane,
                                M3MotionScheme.builder(M3MotionScheme.standard())
                                        .fastSpatial(motionSpec)
                                        .build()
                        );
                        overlayPane.setContent(new Pane());

                        Stage stage = new Stage();
                        Scene scene = new Scene(overlayPane, 640.0, 240.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        overlayPane.applyCss();
                        overlayPane.layout();

                        M3Snackbar first = new M3Snackbar("First swipe message");
                        M3Snackbar second = new M3Snackbar("Second queued message");
                        overlayPane.showSnackbar(first);
                        overlayPane.enqueueSnackbar(second);
                        overlayPane.applyCss();
                        overlayPane.layout();

                        stageReference.set(stage);
                        overlayReference.set(overlayPane);
                        firstReference.set(first);
                        secondReference.set(second);
                        surfaceReference.set(snackbarSurface(snackbarPresenter(overlayPane)));
                    },
                    () -> assertEquals(1, Objects.requireNonNull(overlayReference.get(), "overlay")
                            .getSnackbarQueue().size())
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3OverlayPane overlay = Objects.requireNonNull(overlayReference.get(), "overlay");
                        Region surface = Objects.requireNonNull(surfaceReference.get(), "surface");
                        double scaledTranslateX = surface.getTranslateX()
                                * Objects.requireNonNull(stageReference.get(), "stage").getOutputScaleX();
                        assertEquals(Math.rint(scaledTranslateX), scaledTranslateX, 1.0e-6);
                        if (overlay.getSnackbar() == firstReference.get()
                                && Math.abs(surface.getTranslateX()) > surface.getWidth() * 0.65) {
                            observedHorizontalExit.set(true);
                        }
                        return overlay.getSnackbar() == secondReference.get()
                                && overlay.isSnackbarShowing()
                                && Math.abs(surface.getTranslateX()) <= 1.0e-6
                                && Math.abs(surface.getTranslateY()) <= 1.0e-6
                                && Math.abs(surface.getOpacity() - 1.0) <= 1.0e-6;
                    },
                    () -> {
                        Region surface = Objects.requireNonNull(surfaceReference.get(), "surface");
                        Bounds bounds = surface.localToScene(surface.getBoundsInLocal());
                        double dragDistance = surface.getWidth() * 0.6;
                        fireSwipe(
                                surface,
                                bounds.getCenterX(),
                                bounds.getCenterY(),
                                bounds.getCenterX() + dragDistance,
                                bounds.getCenterY()
                        );
                    },
                    () -> {
                        assertTrue(observedHorizontalExit.get(), "the test must observe horizontal exit motion");
                        assertTrue(Objects.requireNonNull(overlayReference.get(), "overlay")
                                .getSnackbarQueue().isEmpty());
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3OverlayPane overlayPane = overlayReference.get();
                if (overlayPane != null) {
                    FxTestUtils.clearMotionScheme(overlayPane);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that entrance motion remains pixel aligned and text geometry stays stable after settlement.
    @Test
    void entranceMotionSnapsToOutputPixelsAndSettlesTextGeometry() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> surfaceReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> textReference = new AtomicReference<>();
        AtomicBoolean observedIntermediateOffset = new AtomicBoolean();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                        Region surface = Objects.requireNonNull(surfaceReference.get(), "surface");
                        double translateY = surface.getTranslateY();
                        double scaledTranslateY = translateY * stage.getOutputScaleY();
                        assertEquals(
                                Math.rint(scaledTranslateY),
                                scaledTranslateY,
                                1.0e-6,
                                "snackbar translation must align to a physical output pixel"
                        );
                        if (Math.abs(translateY) > 1.0e-6 && Math.abs(translateY - 16.0) > 1.0e-6) {
                            observedIntermediateOffset.set(true);
                        }
                        return observedIntermediateOffset.get()
                                && Math.abs(translateY) <= 1.0e-6
                                && Math.abs(surface.getOpacity() - 1.0) <= 1.0e-6;
                    },
                    () -> {
                        M3OverlayPane overlayPane = overlayPane();
                        M3MotionSpec motionSpec =
                                M3MotionSpec.of(Duration.millis(400.0), M3MotionEasing.LINEAR);
                        FxTestUtils.setMotionScheme(
                                overlayPane,
                                M3MotionScheme.builder(M3MotionScheme.standard())
                                        .fastSpatial(motionSpec)
                                        .build()
                        );
                        overlayPane.setContent(new Pane());

                        Stage stage = new Stage();
                        Scene scene = new Scene(overlayPane, 640.0, 240.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        overlayPane.applyCss();
                        overlayPane.layout();

                        overlayPane.showSnackbar(new M3Snackbar("Stable snackbar text"));
                        overlayPane.applyCss();
                        overlayPane.layout();
                        Node presenter = snackbarPresenter(overlayPane);

                        stageReference.set(stage);
                        overlayReference.set(overlayPane);
                        surfaceReference.set(snackbarSurface(presenter));
                        textReference.set(assertInstanceOf(Label.class, presenter.lookup(".m3-snackbar-text")));
                    },
                    () -> {
                        assertTrue(observedIntermediateOffset.get(), "the test must observe an entrance frame");
                        assertTrue(Objects.requireNonNull(overlayReference.get(), "overlay").isSnackbarShowing());
                    }
            );

            AtomicReference<@Nullable Bounds> settledBoundsReference = new AtomicReference<>();
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        Label text = Objects.requireNonNull(textReference.get(), "text");
                        Bounds currentBounds = text.localToScene(text.getBoundsInLocal());
                        @Nullable Bounds settledBounds = settledBoundsReference.get();
                        if (settledBounds == null) {
                            settledBoundsReference.set(currentBounds);
                        } else {
                            assertEquals(settledBounds.getMinX(), currentBounds.getMinX(), 0.0);
                            assertEquals(settledBounds.getMinY(), currentBounds.getMinY(), 0.0);
                            assertEquals(settledBounds.getWidth(), currentBounds.getWidth(), 0.0);
                            assertEquals(settledBounds.getHeight(), currentBounds.getHeight(), 0.0);
                        }
                        Region surface = Objects.requireNonNull(surfaceReference.get(), "surface");
                        return Math.abs(surface.getTranslateY()) <= 1.0e-6
                                && Math.abs(surface.getOpacity() - 1.0) <= 1.0e-6;
                    },
                    8,
                    () -> {
                    },
                    () -> assertFalse(Objects.requireNonNull(surfaceReference.get(), "surface").isNeedsLayout())
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3OverlayPane overlayPane = overlayReference.get();
                if (overlayPane != null) {
                    FxTestUtils.clearMotionScheme(overlayPane);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Creates an overlay pane with automatic dismissal disabled.
    private static M3OverlayPane overlayPane() {
        M3OverlayPane overlayPane = new M3OverlayPane();
        overlayPane.setSnackbarDisplayDuration(Duration.INDEFINITE);
        return overlayPane;
    }

    /// Returns the unique internal presenter through its stable style class.
    private static Node snackbarPresenter(M3OverlayPane overlayPane) {
        return Objects.requireNonNull(overlayPane.lookup(".m3-snackbar-presenter"), "snackbar presenter");
    }

    /// Returns the presenter's stable snackbar surface.
    private static Region snackbarSurface(Node presenter) {
        return Objects.requireNonNull(
                assertInstanceOf(Region.class, presenter.lookup(".m3-snackbar-container")),
                "snackbar surface"
        );
    }

    /// Sends one primary-button swipe through a concrete event target using scene-space coordinates.
    private static void fireSwipe(
            Node target,
            double startSceneX,
            double startSceneY,
            double endSceneX,
            double endSceneY
    ) {
        target.fireEvent(primaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, startSceneX, startSceneY, true));
        target.fireEvent(primaryMouseEvent(target, MouseEvent.MOUSE_DRAGGED, endSceneX, endSceneY, true));
        target.fireEvent(primaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, endSceneX, endSceneY, false));
    }

    /// Creates one primary mouse event at a scene-space coordinate.
    private static MouseEvent primaryMouseEvent(
            Node target,
            javafx.event.EventType<MouseEvent> eventType,
            double sceneX,
            double sceneY,
            boolean primaryButtonDown
    ) {
        Point2D localPoint = target.sceneToLocal(sceneX, sceneY);
        Point2D screenPoint = target.localToScreen(localPoint);
        double screenX = screenPoint == null ? sceneX : screenPoint.getX();
        double screenY = screenPoint == null ? sceneY : screenPoint.getY();
        return new MouseEvent(
                eventType,
                sceneX,
                sceneY,
                screenX,
                screenY,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(target, sceneX, sceneY)
        );
    }

    /// Counts direct presenter layers owned by one overlay pane.
    private static long directPresenterCount(M3OverlayPane overlayPane) {
        return overlayPane.getChildrenUnmodifiable().stream()
                .filter(node -> node.getStyleClass().contains("m3-snackbar-presenter"))
                .count();
    }
}
