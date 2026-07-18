// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/// Verifies exclusive snackbar ownership across [M3OverlayPane] instances.
@NotNullByDefault
final class M3SnackbarOverlayOwnershipTest {
    /// Starts the JavaFX toolkit before scene-graph nodes are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that current and queued claims reject cross-pane reuse without changing either pane.
    @Test
    void crossPaneClaimsRejectBeforeEitherPaneChanges() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane firstPane = overlayPane();
            M3OverlayPane secondPane = overlayPane();
            M3Snackbar sharedCurrent = new M3Snackbar("Shared current");
            M3Snackbar sharedQueued = new M3Snackbar("Shared queued");
            M3Snackbar secondCurrent = new M3Snackbar("Second current");
            M3Snackbar secondQueued = new M3Snackbar("Second queued");

            firstPane.showSnackbar(sharedCurrent);
            firstPane.enqueueSnackbar(sharedQueued);
            firstPane.enqueueSnackbar(sharedQueued);
            secondPane.showSnackbar(secondCurrent);
            secondPane.enqueueSnackbar(secondQueued);

            assertThrows(IllegalArgumentException.class, () -> secondPane.showSnackbar(sharedCurrent));
            assertThrows(IllegalArgumentException.class, () -> secondPane.enqueueSnackbar(sharedQueued));

            assertSame(sharedCurrent, firstPane.getSnackbar());
            assertEquals(List.of(sharedQueued, sharedQueued), firstPane.getSnackbarQueue());
            assertTrue(firstPane.isSnackbarShowing());
            assertSame(secondCurrent, secondPane.getSnackbar());
            assertEquals(List.of(secondQueued), secondPane.getSnackbarQueue());
            assertTrue(secondPane.isSnackbarShowing());
        });
    }

    /// Verifies that one pane may reuse the same instance as current and in duplicate queue entries.
    @Test
    void onePaneRetainsExistingSameInstanceSemantics() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar snackbar = new M3Snackbar("Reusable");

            overlayPane.showSnackbar(snackbar);
            overlayPane.enqueueSnackbar(snackbar);
            overlayPane.enqueueSnackbar(snackbar);
            assertDoesNotThrow(() -> overlayPane.showSnackbar(snackbar));

            assertSame(snackbar, overlayPane.getSnackbar());
            assertEquals(2, overlayPane.getSnackbarQueue().size());
            assertSame(snackbar, overlayPane.getSnackbarQueue().get(0));
            assertSame(snackbar, overlayPane.getSnackbarQueue().get(1));
        });
    }

    /// Verifies that duplicate queued claims are released only after their final host use is removed.
    @Test
    void duplicateQueueEntriesReleaseOnlyAfterLastUse() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane sourcePane = overlayPane();
            M3OverlayPane destinationPane = overlayPane();
            M3Snackbar current = new M3Snackbar("Current");
            M3Snackbar shared = new M3Snackbar("Shared");

            sourcePane.showSnackbar(current);
            sourcePane.enqueueSnackbar(shared);
            sourcePane.enqueueSnackbar(shared);
            assertThrows(IllegalArgumentException.class, () -> destinationPane.showSnackbar(shared));

            sourcePane.clearSnackbarQueue();

            assertTrue(sourcePane.getSnackbarQueue().isEmpty());
            assertDoesNotThrow(() -> destinationPane.showSnackbar(shared));
            assertSame(shared, destinationPane.getSnackbar());
        });
    }

    /// Verifies that replacement retains a claim while the replaced instance also remains queued.
    @Test
    void replacementReleasesOnlyInstancesNoLongerQueued() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane sourcePane = overlayPane();
            M3OverlayPane destinationPane = overlayPane();
            M3Snackbar shared = new M3Snackbar("Shared");
            M3Snackbar replacement = new M3Snackbar("Replacement");

            sourcePane.showSnackbar(shared);
            sourcePane.enqueueSnackbar(shared);
            sourcePane.showSnackbar(replacement);

            assertSame(replacement, sourcePane.getSnackbar());
            assertEquals(List.of(shared), sourcePane.getSnackbarQueue());
            assertThrows(IllegalArgumentException.class, () -> destinationPane.enqueueSnackbar(shared));

            sourcePane.clearSnackbarQueue();

            assertDoesNotThrow(() -> destinationPane.enqueueSnackbar(shared));
            assertSame(shared, destinationPane.getSnackbar());
        });
    }

    /// Verifies that dismissal releases the old current claim while advancing the queued claim.
    @Test
    void dismissalAndQueueAdvanceTransferClaimsIndependently() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane sourcePane = overlayPane();
            M3OverlayPane destinationPane = overlayPane();
            M3Snackbar dismissed = new M3Snackbar("Dismissed");
            M3Snackbar promoted = new M3Snackbar("Promoted");

            sourcePane.showSnackbar(dismissed);
            sourcePane.enqueueSnackbar(promoted);
            sourcePane.dismissSnackbar();

            assertSame(promoted, sourcePane.getSnackbar());
            assertTrue(sourcePane.isSnackbarShowing());
            assertTrue(sourcePane.getSnackbarQueue().isEmpty());
            assertDoesNotThrow(() -> destinationPane.showSnackbar(dismissed));
            assertSame(dismissed, destinationPane.getSnackbar());
            assertThrows(IllegalArgumentException.class, () -> destinationPane.enqueueSnackbar(promoted));
        });
    }

    /// Verifies that unrelated parents are rejected without disturbing existing snackbar state.
    @Test
    void unrelatedParentIsRejectedBeforeHostStateChanges() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            M3Snackbar current = new M3Snackbar("Current");
            M3Snackbar queued = new M3Snackbar("Queued");
            M3Snackbar foreign = new M3Snackbar("Foreign");
            StackPane foreignParent = new StackPane(foreign);
            overlayPane.showSnackbar(current);
            overlayPane.enqueueSnackbar(queued);

            assertThrows(IllegalArgumentException.class, () -> overlayPane.showSnackbar(foreign));
            assertThrows(IllegalArgumentException.class, () -> overlayPane.enqueueSnackbar(foreign));

            assertSame(foreignParent, foreign.getParent());
            assertSame(current, overlayPane.getSnackbar());
            assertEquals(List.of(queued), overlayPane.getSnackbarQueue());
            assertTrue(overlayPane.isSnackbarShowing());
        });
    }

    /// Verifies ownership while a rendered snackbar exits and after its queued successor is promoted.
    @Tier2Test
    @Test
    void renderedExitRetainsThenReleasesOwnership() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> sourceReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> destinationReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Snackbar> exitingReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Snackbar> promotedReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> {
                    @Nullable M3OverlayPane source = sourceReference.get();
                    @Nullable M3Snackbar promoted = promotedReference.get();
                    return source != null
                            && promoted != null
                            && source.getSnackbar() == promoted
                            && source.isSnackbarShowing();
                },
                () -> "queued snackbar was not promoted after the rendered predecessor exited",
                () -> {
                    M3OverlayPane sourcePane = overlayPane();
                    M3OverlayPane destinationPane = overlayPane();
                    sourcePane.setContent(new Pane());
                    Stage stage = new Stage();
                    Scene scene = new Scene(sourcePane, 360.0, 140.0);
                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    stage.setScene(scene);
                    stage.show();

                    M3Snackbar exiting = new M3Snackbar("Exiting");
                    M3Snackbar promoted = new M3Snackbar("Promoted");
                    sourcePane.showSnackbar(exiting);
                    sourcePane.enqueueSnackbar(promoted);
                    sourcePane.applyCss();
                    sourcePane.layout();

                    assertNotNull(exiting.getParent());
                    assertDoesNotThrow(() -> sourcePane.showSnackbar(exiting));
                    assertThrows(IllegalArgumentException.class, () -> destinationPane.showSnackbar(exiting));

                    stageReference.set(stage);
                    sourceReference.set(sourcePane);
                    destinationReference.set(destinationPane);
                    exitingReference.set(exiting);
                    promotedReference.set(promoted);
                    sourcePane.dismissSnackbar();
                    assertThrows(IllegalArgumentException.class, () -> destinationPane.enqueueSnackbar(exiting));
                },
                () -> {
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    M3OverlayPane sourcePane = Objects.requireNonNull(sourceReference.get(), "sourcePane");
                    M3OverlayPane destinationPane =
                            Objects.requireNonNull(destinationReference.get(), "destinationPane");
                    M3Snackbar exiting = Objects.requireNonNull(exitingReference.get(), "exiting");
                    M3Snackbar promoted = Objects.requireNonNull(promotedReference.get(), "promoted");
                    try {
                        assertNull(exiting.getParent());
                        assertSame(promoted, sourcePane.getSnackbar());
                        assertTrue(sourcePane.getSnackbarQueue().isEmpty());
                        assertDoesNotThrow(() -> destinationPane.showSnackbar(exiting));
                        assertSame(exiting, destinationPane.getSnackbar());
                        assertNotSame(exiting, sourcePane.getSnackbar());
                    } finally {
                        stage.close();
                    }
                }
        );
    }

    /// Verifies every snackbar accessibility result hidden and restored by modal presentation.
    @Tier2Test
    @Test
    void modalBlockingChangesAndRestoresSnackbarAccessibilityResults() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlayPane = overlayPane();
            overlayPane.setContent(new Pane());
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(overlayPane, 360.0, 140.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();

                M3Snackbar current = new M3Snackbar("Current", "Undo");
                M3Snackbar queued = new M3Snackbar("Queued");
                overlayPane.showSnackbar(current);
                overlayPane.enqueueSnackbar(queued);
                overlayPane.applyCss();
                overlayPane.layout();
                Node host = snackbarHost(overlayPane);

                assertSame(current, host.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertEquals(true, host.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
                assertInstanceOf(Node.class, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(2, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(current, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(queued, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertEquals("Current Undo", host.queryAccessibleAttribute(AccessibleAttribute.TEXT));

                M3OverlayPane.OverlayHandle modalHandle = overlayPane.showModalOverlay(new Pane());
                assertNull(host.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertEquals(false, host.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
                assertNull(host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(0, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertNull(host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals("", host.queryAccessibleAttribute(AccessibleAttribute.TEXT));

                modalHandle.hide();
                assertSame(current, host.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertEquals(true, host.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
                assertInstanceOf(Node.class, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(2, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(current, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(queued, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertEquals("Current Undo", host.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            } finally {
                stage.close();
            }
        });
    }

    /// Creates an overlay pane with automatic snackbar dismissal disabled.
    private static M3OverlayPane overlayPane() {
        M3OverlayPane overlayPane = new M3OverlayPane();
        overlayPane.setSnackbarDisplayDuration(Duration.INDEFINITE);
        return overlayPane;
    }

    /// Returns the internal snackbar presentation node through its stable style class.
    private static Node snackbarHost(M3OverlayPane overlayPane) {
        for (Node child : overlayPane.getChildrenUnmodifiable()) {
            if (child.getStyleClass().contains("m3-snackbar-host")) {
                return child;
            }
        }
        throw new AssertionError("overlay pane has no snackbar presentation layer");
    }
}
