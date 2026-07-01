// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for Material smooth scrolling behavior.
@NotNullByDefault
final class M3ScrollPanesTest {
    /// Starts JavaFX before constructing scroll panes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies smooth scrolling uses the viewport width when content pref height changes before the next layout pass.
    @Test
    void smoothScrollingUsesViewportWidthForDynamicContentHeight() {
        FxTestUtils.runOnFxThread(() -> {
            WidthDependentContent content = new WidthDependentContent();
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(160.0, 120.0);
            scrollPane.setFitToWidth(true);

            StackPane root = new StackPane(scrollPane);
            new Scene(root, 180.0, 140.0);
            root.applyCss();
            root.resize(180.0, 140.0);
            root.layout();

            M3ScrollPanes.enableSmoothScrolling(scrollPane);
            M3MotionSettings.setAnimationsEnabled(scrollPane, false);
            try {
                content.setExpanded(true);

                ScrollEvent event = scrollEvent(scrollPane, -80.0);
                scrollPane.fireEvent(event);

                assertTrue(event.isConsumed(), () -> "vvalue=" + scrollPane.getVvalue()
                        + ", viewport=" + scrollPane.getViewportBounds()
                        + ", bounds=" + content.getBoundsInLocal()
                        + ", prefHeightAtViewport=" + content.prefHeight(scrollPane.getViewportBounds().getWidth())
                );
                assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            } finally {
                M3ScrollPanes.disableSmoothScrolling(scrollPane);
                M3MotionSettings.clearAnimationsEnabled(scrollPane);
            }
        });
    }

    /// Creates a vertical wheel scroll event for one scroll pane target.
    private static ScrollEvent scrollEvent(Node target, double deltaY) {
        return new ScrollEvent(
                target,
                target,
                ScrollEvent.SCROLL,
                40.0,
                40.0,
                40.0,
                40.0,
                false,
                false,
                false,
                false,
                false,
                false,
                0.0,
                deltaY,
                0.0,
                deltaY,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0.0,
                ScrollEvent.VerticalTextScrollUnits.NONE,
                0.0,
                0,
                null
        );
    }

    /// Region whose expanded height depends on the width supplied by its scroll pane viewport.
    @NotNullByDefault
    private static final class WidthDependentContent extends Region {
        /// The content height before expansion.
        private static final double COLLAPSED_HEIGHT = 96.0;

        /// The content height after expansion when measured with a known width.
        private static final double EXPANDED_HEIGHT = 520.0;

        /// Whether this content should report its expanded height.
        private boolean expanded;

        /// Creates the dynamic content region.
        private WidthDependentContent() {
        }

        /// Sets whether this content reports an expanded width-dependent preferred height.
        private void setExpanded(boolean expanded) {
            this.expanded = expanded;
            requestLayout();
        }

        /// Computes the preferred width used by the scroll pane viewport.
        @Override
        protected double computePrefWidth(double height) {
            return 160.0;
        }

        /// Computes the preferred height, falling back to collapsed height when no width has been supplied.
        @Override
        protected double computePrefHeight(double width) {
            if (!expanded || width <= 0.0) {
                return COLLAPSED_HEIGHT;
            }
            return EXPANDED_HEIGHT;
        }
    }
}