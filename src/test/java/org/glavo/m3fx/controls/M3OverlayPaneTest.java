// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the public content, overlay, sizing, and snackbar contracts of [M3OverlayPane].
@NotNullByDefault
final class M3OverlayPaneTest {
    /// Starts the JavaFX toolkit before scene-graph nodes are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that valid and invalid content updates preserve one atomic observable content state.
    @Test
    void contentUpdatesAreAtomic() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane overlayPane = new M3OverlayPane();
            StackPane firstContent = new StackPane();
            StackPane secondContent = new StackPane();
            StackPane foreignContent = new StackPane();
            StackPane foreignOwner = new StackPane(foreignContent);
            AtomicInteger changes = new AtomicInteger();
            overlayPane.contentProperty().addListener((observable, oldContent, newContent) -> changes.incrementAndGet());

            overlayPane.setContent(firstContent);
            overlayPane.setContent(secondContent);
            assertSame(secondContent, overlayPane.getContent());
            assertSame(overlayPane, secondContent.getParent());
            assertNull(firstContent.getParent());
            assertEquals(2, changes.get());
            assertSame(overlayPane, overlayPane.contentProperty().getBean());

            StackPane ancestor = new StackPane(overlayPane);
            Pane activeOverlay = new Pane();
            M3OverlayPane.OverlayHandle overlayHandle = overlayPane.showOverlay(activeOverlay);

            assertThrows(IllegalArgumentException.class, () -> overlayPane.setContent(foreignContent));
            assertThrows(IllegalArgumentException.class, () -> overlayPane.setContent(overlayPane));
            assertThrows(IllegalArgumentException.class, () -> overlayPane.setContent(ancestor));
            assertThrows(IllegalArgumentException.class, () -> overlayPane.setContent(activeOverlay));
            assertSame(secondContent, overlayPane.getContent());
            assertSame(overlayPane, secondContent.getParent());
            assertSame(foreignOwner, foreignContent.getParent());
            assertEquals(2, changes.get());

            assertTrue(overlayHandle.hide());
            overlayPane.setContent(null);
            assertNull(overlayPane.getContent());
            assertNull(secondContent.getParent());
            assertEquals(3, changes.get());
        });
    }

    /// Verifies that each overlay handle exclusively owns one presentation and hides it idempotently.
    @Test
    void overlayHandlesOwnOnePresentation() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane overlayPane = new M3OverlayPane();
            M3OverlayPane otherPane = new M3OverlayPane();
            Pane regularOverlay = new Pane();
            Pane modalOverlay = new Pane();

            M3OverlayPane.OverlayHandle regularHandle = overlayPane.showOverlay(regularOverlay);
            M3OverlayPane.OverlayHandle modalHandle = overlayPane.showModalOverlay(modalOverlay);
            assertTrue(regularHandle.isShowing());
            assertTrue(modalHandle.isShowing());
            assertSame(overlayPane, regularOverlay.getParent());
            assertSame(overlayPane, modalOverlay.getParent());
            assertThrows(IllegalArgumentException.class, () -> overlayPane.showOverlay(regularOverlay));
            assertThrows(IllegalArgumentException.class, () -> otherPane.showOverlay(modalOverlay));

            assertTrue(regularHandle.hide());
            assertFalse(regularHandle.isShowing());
            assertFalse(regularHandle.hide());
            assertNull(regularOverlay.getParent());
            assertTrue(modalHandle.isShowing());

            assertTrue(modalHandle.hide());
            assertFalse(modalHandle.isShowing());
            assertFalse(modalHandle.hide());
            assertNull(modalOverlay.getParent());

            M3OverlayPane.OverlayHandle reusedHandle = otherPane.showOverlay(regularOverlay);
            assertSame(otherPane, regularOverlay.getParent());
            assertTrue(reusedHandle.hide());
        });
    }

    /// Verifies that transient layers fill the client area without contributing preferred size.
    @Test
    void layoutFillsLayersButMeasuresOnlyContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane overlayPane = new M3OverlayPane();
            Region content = fixedRegion(240.0, 160.0);
            Region oversizedOverlay = fixedRegion(900.0, 700.0);
            overlayPane.setContent(content);
            M3OverlayPane.OverlayHandle overlayHandle = overlayPane.showOverlay(oversizedOverlay);

            assertEquals(240.0, overlayPane.prefWidth(-1.0), 0.01);
            assertEquals(160.0, overlayPane.prefHeight(-1.0), 0.01);

            overlayPane.resize(480.0, 320.0);
            overlayPane.layout();
            assertEquals(480.0, content.getWidth(), 0.01);
            assertEquals(320.0, content.getHeight(), 0.01);
            assertEquals(480.0, oversizedOverlay.getWidth(), 0.01);
            assertEquals(320.0, oversizedOverlay.getHeight(), 0.01);
            assertTrue(overlayHandle.hide());
        });
    }

    /// Verifies that the uppermost modal layer consumes lower-layer input without blocking its own subtree.
    @Test
    void modalOverlayBlocksInputOutsideItsSubtree() {
        FxTestUtils.runOnFxThread(() -> {
            Pane background = new Pane();
            Pane modal = new Pane();
            M3OverlayPane overlayPane = new M3OverlayPane();
            AtomicInteger backgroundPresses = new AtomicInteger();
            AtomicInteger modalPresses = new AtomicInteger();
            overlayPane.setContent(background);
            background.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> backgroundPresses.incrementAndGet());
            modal.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> modalPresses.incrementAndGet());
            new Scene(overlayPane, 320.0, 180.0);

            M3OverlayPane.OverlayHandle modalHandle = overlayPane.showModalOverlay(modal);
            background.fireEvent(primaryMousePress(background));
            modal.fireEvent(primaryMousePress(modal));
            assertEquals(0, backgroundPresses.get());
            assertEquals(1, modalPresses.get());

            assertTrue(modalHandle.hide());
            background.fireEvent(primaryMousePress(background));
            assertEquals(1, backgroundPresses.get());
        });
    }

    /// Verifies current, queue, duration, and mutation behavior exposed by the snackbar facade.
    @SuppressWarnings("DataFlowIssue")
    @Test
    void snackbarFacadeOwnsPresentationState() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane overlayPane = new M3OverlayPane();
            M3Snackbar first = new M3Snackbar("First");
            M3Snackbar second = new M3Snackbar("Second");

            assertNull(overlayPane.getSnackbarDisplayDuration());
            overlayPane.setSnackbarDisplayDuration(Duration.INDEFINITE);
            assertEquals(Duration.INDEFINITE, overlayPane.getSnackbarDisplayDuration());
            assertSame(overlayPane, overlayPane.snackbarDisplayDurationProperty().getBean());
            assertSame(overlayPane, overlayPane.snackbarProperty().getBean());
            assertSame(overlayPane, overlayPane.snackbarShowingProperty().getBean());

            overlayPane.showSnackbar(first);
            overlayPane.enqueueSnackbar(second);
            assertSame(first, overlayPane.getSnackbar());
            assertTrue(overlayPane.isSnackbarShowing());
            assertEquals(1, overlayPane.getSnackbarQueue().size());
            assertSame(second, overlayPane.getSnackbarQueue().get(0));
            assertThrows(UnsupportedOperationException.class, () -> overlayPane.getSnackbarQueue().clear());

            overlayPane.clearSnackbarQueue();
            assertTrue(overlayPane.getSnackbarQueue().isEmpty());
            overlayPane.setSnackbarDisplayDuration(null);
            assertNull(overlayPane.getSnackbarDisplayDuration());
        });
    }

    /// Verifies current messages update in place while pending messages expose their latest state after promotion.
    @Test
    void snackbarPropertiesRemainIsolatedUntilQueuedMessagesArePromoted() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = new M3OverlayPane();
            M3Snackbar current = new M3Snackbar("Current");
            current.setActionText("Undo");
            M3Snackbar pending = new M3Snackbar("Queued");
            overlayPane.setSnackbarDisplayDuration(Duration.INDEFINITE);
            new Scene(overlayPane, 640.0, 360.0);

            overlayPane.showSnackbar(current);
            overlayPane.enqueueSnackbar(pending);
            overlayPane.applyCss();
            overlayPane.resize(640.0, 360.0);
            overlayPane.layout();

            Region presenter = assertInstanceOf(Region.class, overlayPane.lookup(".m3-snackbar-presenter"));
            Label text = assertInstanceOf(Label.class, presenter.lookup(".m3-snackbar-text"));
            M3Button action = assertInstanceOf(M3Button.class, presenter.lookup(".m3-snackbar-action"));
            M3IconButton close = assertInstanceOf(M3IconButton.class, presenter.lookup(".m3-snackbar-close"));

            pending.setText("Queued latest");
            pending.setActionText("Open");
            pending.setAction(null);
            pending.setCloseButtonVisible(true);
            assertEquals("Current", text.getText());
            assertEquals("Undo", action.getText());
            assertTrue(action.isManaged());
            assertFalse(close.isManaged());

            current.setText("Current latest");
            current.setActionText("");
            current.setCloseButtonVisible(true);
            overlayPane.applyCss();
            overlayPane.layout();
            assertEquals("Current latest", text.getText());
            assertFalse(action.isManaged());
            assertTrue(close.isManaged());
            assertEquals("Current latest", presenter.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(1, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

            current.setActionText("Dismiss");
            current.setCloseButtonVisible(false);
            overlayPane.applyCss();
            overlayPane.layout();
            assertTrue(action.isManaged());
            assertFalse(close.isManaged());
            action.fire();
            overlayPane.applyCss();
            overlayPane.layout();

            assertSame(pending, overlayPane.getSnackbar());
            assertEquals("Queued latest", text.getText());
            assertEquals("Open", action.getText());
            assertTrue(action.isManaged());
            assertTrue(close.isManaged());
            assertEquals(2, presenter.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        });
    }

    /// Verifies the built-in snackbar layer never turns its full-window layout bounds into an input shield.
    @Test
    void snackbarLayerOnlyPicksItsVisibleSurface() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button contentAction = new M3Button("Content action");
            M3OverlayPane overlayPane = new M3OverlayPane();
            overlayPane.setContent(new StackPane(contentAction));
            new Scene(overlayPane, 640.0, 360.0);
            overlayPane.applyCss();
            overlayPane.resize(640.0, 360.0);
            overlayPane.layout();

            Region presenter = assertInstanceOf(
                    Region.class,
                    overlayPane.lookup(".m3-snackbar-presenter")
            );
            assertTrue(presenter.isMouseTransparent());
            assertTrue(presenter.getBackground() == null || presenter.getBackground().isEmpty());

            overlayPane.showSnackbar(new M3Snackbar("Message"));
            overlayPane.applyCss();
            overlayPane.layout();

            assertFalse(presenter.isMouseTransparent());
            assertTrue(presenter.getBackground() == null || presenter.getBackground().isEmpty());
            assertFalse(presenter.contains(8.0, 8.0));
            Region surface = assertInstanceOf(
                    Region.class,
                    presenter.lookup(".m3-snackbar-container")
            );
            assertFalse(surface.isMouseTransparent());
            assertTrue(surface.contains(surface.getWidth() / 2.0, surface.getHeight() / 2.0));
        });
    }

    /// Verifies that accessibility exposes only the uppermost modal layer while a modal stack is active.
    @Test
    void accessibilityIsConstrainedToTopModalOverlay() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane overlayPane = new M3OverlayPane();
            M3Button firstAction = new M3Button("First action");
            M3Button secondAction = new M3Button("Second action");
            StackPane firstModal = new StackPane(firstAction);
            StackPane secondModal = new StackPane(secondAction);
            new Scene(overlayPane, 320.0, 180.0);

            M3OverlayPane.OverlayHandle firstHandle = overlayPane.showModalOverlay(firstModal);
            assertEquals(
                    List.of(firstModal),
                    overlayPane.queryAccessibleAttribute(AccessibleAttribute.CHILDREN)
            );
            assertSame(firstModal, overlayPane.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertEquals(1, overlayPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(firstModal, overlayPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertNull(overlayPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
            assertSame(firstAction, overlayPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

            M3OverlayPane.OverlayHandle secondHandle = overlayPane.showModalOverlay(secondModal);
            assertEquals(
                    List.of(secondModal),
                    overlayPane.queryAccessibleAttribute(AccessibleAttribute.CHILDREN)
            );
            assertSame(secondModal, overlayPane.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertSame(secondModal, overlayPane.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertSame(secondAction, overlayPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

            assertTrue(secondHandle.hide());
            assertSame(firstModal, overlayPane.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertTrue(firstHandle.hide());
        });
    }

    /// Verifies modal focus containment and restoration when nested overlays close out of presentation order.
    @Tier2Test
    @Test
    void modalFocusIsContainedAndRestoredAfterOutOfOrderClosure() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayPaneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> backgroundActionReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> firstActionReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> secondActionReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> firstModalReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> secondModalReference = new AtomicReference<>();
        AtomicReference<M3OverlayPane.@Nullable OverlayHandle> firstHandleReference = new AtomicReference<>();
        AtomicReference<M3OverlayPane.@Nullable OverlayHandle> secondHandleReference = new AtomicReference<>();
        AtomicBoolean focusWasLost = new AtomicBoolean();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> focused(backgroundActionReference.get()),
                    () -> {
                        M3Button backgroundAction = new M3Button("Background action");
                        M3Button firstAction = new M3Button("First modal action");
                        M3Button secondAction = new M3Button("Second modal action");
                        StackPane firstModal = new StackPane(firstAction);
                        StackPane secondModal = new StackPane(secondAction);
                        M3OverlayPane overlayPane = new M3OverlayPane();
                        overlayPane.setContent(new StackPane(backgroundAction));
                        Stage stage = new Stage();
                        stage.setScene(new Scene(overlayPane, 480.0, 280.0));
                        stage.show();
                        stage.requestFocus();
                        backgroundAction.requestFocus();

                        stageReference.set(stage);
                        overlayPaneReference.set(overlayPane);
                        backgroundActionReference.set(backgroundAction);
                        firstActionReference.set(firstAction);
                        secondActionReference.set(secondAction);
                        firstModalReference.set(firstModal);
                        secondModalReference.set(secondModal);
                    },
                    () -> {
                        assertTrue(Objects.requireNonNull(
                                backgroundActionReference.get(),
                                "backgroundAction"
                        ).isFocused());
                        Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                        stage.getScene().focusOwnerProperty().addListener(
                                (observable, oldFocusOwner, newFocusOwner) -> {
                                    if (newFocusOwner == null && stage.isShowing() && stage.isFocused()) {
                                        focusWasLost.set(true);
                                    }
                                }
                        );
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> focused(firstActionReference.get()),
                    () -> firstHandleReference.set(Objects.requireNonNull(
                            overlayPaneReference.get(),
                            "overlayPane"
                    ).showModalOverlay(Objects.requireNonNull(firstModalReference.get(), "firstModal"))),
                    () -> assertTrue(Objects.requireNonNull(
                            firstHandleReference.get(),
                            "firstHandle"
                    ).isShowing())
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> focused(secondActionReference.get()),
                    () -> secondHandleReference.set(Objects.requireNonNull(
                            overlayPaneReference.get(),
                            "overlayPane"
                    ).showModalOverlay(Objects.requireNonNull(secondModalReference.get(), "secondModal"))),
                    () -> assertTrue(Objects.requireNonNull(
                            secondHandleReference.get(),
                            "secondHandle"
                    ).isShowing())
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> focused(secondActionReference.get()),
                    () -> Objects.requireNonNull(
                            backgroundActionReference.get(),
                            "backgroundAction"
                    ).requestFocus(),
                    () -> assertFalse(Objects.requireNonNull(
                            backgroundActionReference.get(),
                            "backgroundAction"
                    ).isFocused())
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> !showing(firstHandleReference.get())
                            && showing(secondHandleReference.get())
                            && focused(secondActionReference.get()),
                    () -> assertTrue(Objects.requireNonNull(
                            firstHandleReference.get(),
                            "firstHandle"
                    ).hide()),
                    () -> {
                        assertNull(Objects.requireNonNull(firstModalReference.get(), "firstModal").getParent());
                        assertTrue(Objects.requireNonNull(
                                secondHandleReference.get(),
                                "secondHandle"
                        ).isShowing());
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> !showing(secondHandleReference.get()) && focused(backgroundActionReference.get()),
                    () -> assertTrue(Objects.requireNonNull(
                            secondHandleReference.get(),
                            "secondHandle"
                    ).hide()),
                    () -> {
                        assertNull(Objects.requireNonNull(secondModalReference.get(), "secondModal").getParent());
                        assertTrue(Objects.requireNonNull(
                                backgroundActionReference.get(),
                                "backgroundAction"
                        ).isFocused());
                        assertFalse(focusWasLost.get(), "modal focus handoff must not expose a null focus owner");
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Creates a resizable region with fixed minimum and preferred dimensions.
    private static Region fixedRegion(double width, double height) {
        Region region = new Region();
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        return region;
    }

    /// Returns a primary-button press event targeted at the supplied node.
    private static MouseEvent primaryMousePress(Node target) {
        return new MouseEvent(
                MouseEvent.MOUSE_PRESSED,
                4.0,
                4.0,
                4.0,
                4.0,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                false,
                true,
                new PickResult(target, 4.0, 4.0)
        );
    }

    /// Returns whether the referenced node currently owns keyboard focus.
    private static boolean focused(@Nullable Node node) {
        return node != null && node.isFocused();
    }

    /// Returns whether the referenced overlay handle remains active.
    private static boolean showing(M3OverlayPane.@Nullable OverlayHandle handle) {
        return handle != null && handle.isShowing();
    }
}
