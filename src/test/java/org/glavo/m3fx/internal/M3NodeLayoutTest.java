// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.binding.ObjectBinding;
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

    /// Verifies physical alignment for nodes isolated from automatic JavaFX mirroring.
    @Test
    void resolvesPhysicalAlignmentForIsolatedNodes() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();

            assertEquals(Pos.CENTER_LEFT, M3NodeLayout.physicalStartCenterAlignment(node));
            assertEquals(Pos.TOP_LEFT, M3NodeLayout.physicalStartTopAlignment(node));
            assertEquals(Pos.TOP_RIGHT, M3NodeLayout.physicalEndTopAlignment(node));

            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            assertEquals(Pos.CENTER_RIGHT, M3NodeLayout.physicalStartCenterAlignment(node));
            assertEquals(Pos.TOP_RIGHT, M3NodeLayout.physicalStartTopAlignment(node));
            assertEquals(Pos.TOP_LEFT, M3NodeLayout.physicalEndTopAlignment(node));
        });
    }

    /// Verifies that physical-alignment bindings update when the orientation owner changes.
    @Test
    void physicalAlignmentBindingsTrackEffectiveOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            Pane parent = new Pane();
            Pane child = new Pane();
            parent.getChildren().add(child);

            ObjectBinding<Pos> startCenterBinding = M3NodeLayout.createPhysicalStartCenterAlignmentBinding(child);
            ObjectBinding<Pos> startTopBinding = M3NodeLayout.createPhysicalStartTopAlignmentBinding(child);
            try {
                assertEquals(Pos.CENTER_LEFT, startCenterBinding.get());
                assertEquals(Pos.TOP_LEFT, startTopBinding.get());

                parent.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

                assertEquals(Pos.CENTER_RIGHT, startCenterBinding.get());
                assertEquals(Pos.TOP_RIGHT, startTopBinding.get());

                child.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

                assertEquals(Pos.CENTER_LEFT, startCenterBinding.get());
                assertEquals(Pos.TOP_LEFT, startTopBinding.get());
            } finally {
                startCenterBinding.dispose();
                startTopBinding.dispose();
            }
        });
    }
}