// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Button;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests allocation-free routing of shared accessible focus notifications.
@NotNullByDefault
final class M3AccessibleFocusNotifierTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that nested and duplicate notifiers receive only focus transitions crossing their physical subtrees.
    @Test
    void dispatchesOnlyAffectedAncestorPaths() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button outsideBranch = new M3Button("Outside branch");
            M3Button unrelated = new M3Button("Unrelated");
            VBox branch = new VBox(first, second);
            VBox activeContainer = new VBox(branch, outsideBranch);
            VBox unrelatedContainer = new VBox(unrelated);
            HBox sceneRoot = new HBox(activeContainer, unrelatedContainer);

            AtomicInteger firstBranchReads = new AtomicInteger();
            AtomicInteger secondBranchReads = new AtomicInteger();
            AtomicInteger activeReads = new AtomicInteger();
            AtomicInteger unrelatedReads = new AtomicInteger();
            M3AccessibleFocusNotifier firstBranchNotifier = new M3AccessibleFocusNotifier(
                    branch,
                    () -> {
                        firstBranchReads.incrementAndGet();
                        return focusedDescendant(branch);
                    },
                    () -> {
                    }
            );
            M3AccessibleFocusNotifier secondBranchNotifier = new M3AccessibleFocusNotifier(
                    branch,
                    () -> {
                        secondBranchReads.incrementAndGet();
                        return focusedDescendant(branch);
                    },
                    () -> {
                    }
            );
            M3AccessibleFocusNotifier activeNotifier = new M3AccessibleFocusNotifier(
                    activeContainer,
                    () -> {
                        activeReads.incrementAndGet();
                        return focusedDescendant(activeContainer);
                    },
                    () -> {
                    }
            );
            M3AccessibleFocusNotifier unrelatedNotifier = new M3AccessibleFocusNotifier(
                    unrelatedContainer,
                    () -> {
                        unrelatedReads.incrementAndGet();
                        return focusedDescendant(unrelatedContainer);
                    },
                    () -> {
                    }
            );

            Stage stage = new Stage();
            try {
                firstBranchNotifier.start();
                secondBranchNotifier.start();
                activeNotifier.start();
                unrelatedNotifier.start();
                stage.setScene(new Scene(sceneRoot, 480.0, 180.0));
                stage.show();
                sceneRoot.applyCss();
                sceneRoot.layout();
                firstBranchNotifier.refresh();
                secondBranchNotifier.refresh();
                activeNotifier.refresh();
                unrelatedNotifier.refresh();
                reset(firstBranchReads, secondBranchReads, activeReads, unrelatedReads);

                first.requestFocus();

                assertTrue(first.isFocused());
                assertReads(1, 1, 1, 0, firstBranchReads, secondBranchReads, activeReads, unrelatedReads);
                reset(firstBranchReads, secondBranchReads, activeReads, unrelatedReads);

                second.requestFocus();

                assertTrue(second.isFocused());
                assertReads(1, 1, 1, 0, firstBranchReads, secondBranchReads, activeReads, unrelatedReads);
                reset(firstBranchReads, secondBranchReads, activeReads, unrelatedReads);

                outsideBranch.requestFocus();

                assertTrue(outsideBranch.isFocused());
                assertReads(1, 1, 1, 0, firstBranchReads, secondBranchReads, activeReads, unrelatedReads);
                reset(firstBranchReads, secondBranchReads, activeReads, unrelatedReads);

                unrelated.requestFocus();

                assertTrue(unrelated.isFocused());
                assertReads(0, 0, 1, 1, firstBranchReads, secondBranchReads, activeReads, unrelatedReads);
                reset(firstBranchReads, secondBranchReads, activeReads, unrelatedReads);

                firstBranchNotifier.stop();
                reset(firstBranchReads, secondBranchReads, activeReads, unrelatedReads);
                first.requestFocus();

                assertTrue(first.isFocused());
                assertReads(0, 1, 1, 1, firstBranchReads, secondBranchReads, activeReads, unrelatedReads);
            } finally {
                firstBranchNotifier.stop();
                secondBranchNotifier.stop();
                activeNotifier.stop();
                unrelatedNotifier.stop();
                stage.close();
            }
        });
    }

    /// Returns the current scene focus owner when it is physically contained by the supplied root.
    private static @Nullable Node focusedDescendant(Node root) {
        Scene scene = root.getScene();
        if (scene == null) {
            return null;
        }

        @Nullable Node focusOwner = scene.getFocusOwner();
        @Nullable Node current = focusOwner;
        while (current != null) {
            if (current == root) {
                return focusOwner;
            }
            current = current.getParent();
        }
        return null;
    }

    /// Resets supplier invocation counters between focus transitions.
    private static void reset(AtomicInteger... counters) {
        for (AtomicInteger counter : counters) {
            counter.set(0);
        }
    }

    /// Verifies supplier invocation counts for one focus transition.
    private static void assertReads(
            int expectedFirstBranch,
            int expectedSecondBranch,
            int expectedActive,
            int expectedUnrelated,
            AtomicInteger firstBranchReads,
            AtomicInteger secondBranchReads,
            AtomicInteger activeReads,
            AtomicInteger unrelatedReads
    ) {
        assertEquals(expectedFirstBranch, firstBranchReads.get());
        assertEquals(expectedSecondBranch, secondBranchReads.get());
        assertEquals(expectedActive, activeReads.get());
        assertEquals(expectedUnrelated, unrelatedReads.get());
    }
}