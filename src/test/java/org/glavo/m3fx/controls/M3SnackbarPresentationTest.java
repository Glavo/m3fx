// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    /// Counts direct presenter layers owned by one overlay pane.
    private static long directPresenterCount(M3OverlayPane overlayPane) {
        return overlayPane.getChildrenUnmodifiable().stream()
                .filter(node -> node.getStyleClass().contains("m3-snackbar-presenter"))
                .count();
    }
}
