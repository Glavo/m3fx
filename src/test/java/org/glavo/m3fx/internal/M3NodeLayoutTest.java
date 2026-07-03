// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.binding.ObjectBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies logical layout behavior used by orientation-aware controls.
@NotNullByDefault
final class M3NodeLayoutTest {
    /// Starts JavaFX before constructing scene graph nodes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that right-to-left detection follows the node's effective orientation.
    @Test
    void detectsEffectiveRightToLeftOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            Pane parent = new Pane();
            Pane child = new Pane();
            parent.getChildren().add(child);

            assertFalse(M3NodeLayout.isRightToLeft(child));

            parent.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            assertTrue(M3NodeLayout.isRightToLeft(child));

            child.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            assertFalse(M3NodeLayout.isRightToLeft(child));
        });
    }

    /// Verifies logical start and end alignments for both layout directions.
    @Test
    void resolvesLogicalStartAndEndAlignments() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();

            assertEquals(Pos.CENTER_LEFT, M3NodeLayout.logicalStartCenterAlignment(node));
            assertEquals(Pos.CENTER_RIGHT, M3NodeLayout.logicalEndCenterAlignment(node));
            assertEquals(Pos.TOP_LEFT, M3NodeLayout.logicalStartTopAlignment(node));
            assertEquals(Pos.TOP_RIGHT, M3NodeLayout.logicalEndTopAlignment(node));
            assertEquals(HPos.LEFT, M3NodeLayout.logicalStartHorizontalAlignment(node));

            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            assertEquals(Pos.CENTER_RIGHT, M3NodeLayout.logicalStartCenterAlignment(node));
            assertEquals(Pos.CENTER_LEFT, M3NodeLayout.logicalEndCenterAlignment(node));
            assertEquals(Pos.TOP_RIGHT, M3NodeLayout.logicalStartTopAlignment(node));
            assertEquals(Pos.TOP_LEFT, M3NodeLayout.logicalEndTopAlignment(node));
            assertEquals(HPos.RIGHT, M3NodeLayout.logicalStartHorizontalAlignment(node));
        });
    }

    /// Verifies that logical alignment mirrors only horizontal edge positions in right-to-left layouts.
    @Test
    void mirrorsLogicalAlignmentHorizontalEdges() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();

            assertEquals(Pos.TOP_LEFT, M3NodeLayout.logicalAlignment(node, Pos.TOP_LEFT));
            assertEquals(Pos.CENTER, M3NodeLayout.logicalAlignment(node, Pos.CENTER));

            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            assertEquals(Pos.TOP_RIGHT, M3NodeLayout.logicalAlignment(node, Pos.TOP_LEFT));
            assertEquals(Pos.TOP_LEFT, M3NodeLayout.logicalAlignment(node, Pos.TOP_RIGHT));
            assertEquals(Pos.CENTER_RIGHT, M3NodeLayout.logicalAlignment(node, Pos.CENTER_LEFT));
            assertEquals(Pos.CENTER_LEFT, M3NodeLayout.logicalAlignment(node, Pos.CENTER_RIGHT));
            assertEquals(Pos.BOTTOM_RIGHT, M3NodeLayout.logicalAlignment(node, Pos.BOTTOM_LEFT));
            assertEquals(Pos.BOTTOM_LEFT, M3NodeLayout.logicalAlignment(node, Pos.BOTTOM_RIGHT));
            assertEquals(Pos.BASELINE_RIGHT, M3NodeLayout.logicalAlignment(node, Pos.BASELINE_LEFT));
            assertEquals(Pos.BASELINE_LEFT, M3NodeLayout.logicalAlignment(node, Pos.BASELINE_RIGHT));
            assertEquals(Pos.TOP_CENTER, M3NodeLayout.logicalAlignment(node, Pos.TOP_CENTER));
            assertEquals(Pos.CENTER, M3NodeLayout.logicalAlignment(node, Pos.CENTER));
            assertEquals(Pos.BOTTOM_CENTER, M3NodeLayout.logicalAlignment(node, Pos.BOTTOM_CENTER));
            assertEquals(Pos.BASELINE_CENTER, M3NodeLayout.logicalAlignment(node, Pos.BASELINE_CENTER));
        });
    }

    /// Verifies conversion from logical leading and trailing insets to physical JavaFX insets.
    @Test
    void resolvesLogicalInsetsToPhysicalSides() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();

            assertEquals(new Insets(1.0, 4.0, 3.0, 2.0),
                    M3NodeLayout.logicalInsets(node, 1.0, 2.0, 3.0, 4.0));

            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            assertEquals(new Insets(1.0, 2.0, 3.0, 4.0),
                    M3NodeLayout.logicalInsets(node, 1.0, 2.0, 3.0, 4.0));
        });
    }

    /// Verifies that logical alignment bindings update when effective orientation changes.
    @Test
    void logicalAlignmentBindingsTrackEffectiveOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            Pane parent = new Pane();
            Pane child = new Pane();
            parent.getChildren().add(child);

            ObjectBinding<Pos> startCenterBinding = M3NodeLayout.createLogicalStartCenterAlignmentBinding(child);
            ObjectBinding<Pos> startTopBinding = M3NodeLayout.createLogicalStartTopAlignmentBinding(child);
            ObjectBinding<Pos> endCenterBinding = M3NodeLayout.createLogicalEndCenterAlignmentBinding(child);
            try {
                assertEquals(Pos.CENTER_LEFT, startCenterBinding.get());
                assertEquals(Pos.TOP_LEFT, startTopBinding.get());
                assertEquals(Pos.CENTER_RIGHT, endCenterBinding.get());

                parent.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

                assertEquals(Pos.CENTER_RIGHT, startCenterBinding.get());
                assertEquals(Pos.TOP_RIGHT, startTopBinding.get());
                assertEquals(Pos.CENTER_LEFT, endCenterBinding.get());

                child.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

                assertEquals(Pos.CENTER_LEFT, startCenterBinding.get());
                assertEquals(Pos.TOP_LEFT, startTopBinding.get());
                assertEquals(Pos.CENTER_RIGHT, endCenterBinding.get());
            } finally {
                startCenterBinding.dispose();
                startTopBinding.dispose();
                endCenterBinding.dispose();
            }
        });
    }
}