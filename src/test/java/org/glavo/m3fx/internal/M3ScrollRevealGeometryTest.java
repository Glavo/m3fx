// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Shear;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies reveal geometry without scene-space bounding-box inflation or inverse-transform requirements.
@NotNullByDefault
final class M3ScrollRevealGeometryTest {
    /// Starts JavaFX before laying out scroll panes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Keeps logical reveal offsets independent of transforms on the content or its outer ancestors.
    @ParameterizedTest
    @CsvSource({"content,rotate", "ancestor,rotate", "content,collapse", "ancestor,collapse",
            "content,shear", "ancestor,shear"})
    void revealUsesContentCoordinatesUnderAncestorTransforms(String transformed, String operation) {
        FxTestUtils.runOnFxThread(() -> {
            Region target = target();
            target.relocate(300, 200);
            Pane content = new Pane(target);
            ScrollPane scrollPane = scrollPane(content);
            Node node = transformed.equals("content") ? content : scrollPane;
            switch (operation) {
                case "rotate" -> node.getTransforms().add(new Rotate(35));
                case "collapse" -> node.setScaleX(0);
                case "shear" -> node.getTransforms().add(new Shear(0.5, 0.25));
                default -> throw new AssertionError(operation);
            }

            M3ScrollReveal.revealTargetInScrollPane(scrollPane, target);

            assertRevealOffsets(scrollPane, content);
        });
    }

    /// Composes descendant transforms before measuring their bounds so canceling rotations remain exact.
    @Test
    void cancelingDescendantRotationsDoNotInflateBounds() {
        FxTestUtils.runOnFxThread(() -> {
            Region target = target();
            target.getTransforms().add(new Rotate(35));
            Pane group = new Pane(target);
            group.setManaged(false);
            group.relocate(300, 200);
            group.getTransforms().add(new Rotate(-35));
            Pane content = new Pane(group);
            ScrollPane scrollPane = scrollPane(content);

            M3ScrollReveal.revealTargetInScrollPane(scrollPane, target);

            assertRevealOffsets(scrollPane, content);
        });
    }

    /// Creates a target with fixed geometry independent of parent layout.
    private static Region target() {
        Region target = new Region();
        target.setManaged(false);
        target.resize(40, 30);
        return target;
    }

    /// Establishes a viewport and content extent before applying transforms under test.
    private static ScrollPane scrollPane(Pane content) {
        content.setPrefSize(800, 600);
        ScrollPane scrollPane = new ScrollPane(content);
        new Scene(scrollPane, 180, 120);
        scrollPane.applyCss();
        scrollPane.resize(180, 120);
        scrollPane.layout();
        content.layout();
        return scrollPane;
    }

    /// Compares scroll values with the smallest offsets revealing a target ending at (340, 230).
    private static void assertRevealOffsets(ScrollPane scrollPane, Pane content) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        assertEquals((340 - viewportWidth) / (content.getWidth() - viewportWidth), scrollPane.getHvalue(), 1.0e-9);
        assertEquals((230 - viewportHeight) / (content.getHeight() - viewportHeight), scrollPane.getVvalue(), 1.0e-9);
    }
}
