// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies carousel measurement of resizable nodes that do not extend Region.
@NotNullByDefault
final class M3CarouselItemSizingTest {
    /// Starts JavaFX before creating item state layers.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Honors node constraints and measures preferred height at the unmasked focal width.
    @ParameterizedTest
    @CsvSource({"minWidth,40", "prefWidth,100", "minHeight,8", "prefHeight,20", "focalHeight,50"})
    void slotUsesNodeSizingContract(String metric, double expected) {
        FxTestUtils.runOnFxThread(() -> {
            M3CarouselItemSlot slot = new M3CarouselItemSlot(new ResizableItem());
            try {
                double actual = switch (metric) {
                    case "minWidth" -> slot.minWidth(-1);
                    case "prefWidth" -> slot.prefWidth(-1);
                    case "minHeight" -> slot.minHeight(80);
                    case "prefHeight" -> slot.prefHeight(80);
                    case "focalHeight" -> {
                        slot.configure(200, 0, false);
                        yield slot.prefHeight(60);
                    }
                    default -> throw new AssertionError(metric);
                };
                assertEquals(expected, actual);
            } finally {
                slot.dispose();
            }
        });
    }

    /// Uses authored constraints throughout the public uncontained carousel layout.
    @Test
    void carouselResizesNonRegionContentFromItsPreferredSize() {
        FxTestUtils.runOnFxThread(() -> {
            ResizableItem item = new ResizableItem();
            M3Carousel carousel = new M3Carousel();
            carousel.setCarouselLayout(M3CarouselLayout.UNCONTAINED);
            carousel.getItems().add(item);
            StackPane root = new StackPane(carousel);
            new Scene(root, 480, 150);
            root.applyCss();
            root.resize(480, 150);
            root.layout();

            assertEquals(100, item.getLayoutBounds().getWidth(), 1e-9);
            assertEquals(25, item.getLayoutBounds().getHeight(), 1e-9);
            carousel.getItems().clear();
        });
    }

    /// Provides width constraints and width-dependent height without inheriting Region's sizing properties.
    @NotNullByDefault
    private static final class ResizableItem extends Group {
        /// The visible content whose dimensions record the latest resize.
        private final Rectangle rectangle = new Rectangle(13, 17);

        /// Creates an item whose initial bounds differ from every preferred-size constraint.
        private ResizableItem() {
            setAutoSizeChildren(false);
            getChildren().add(rectangle);
        }

        /// Returns `true` so its parent assigns the measured dimensions.
        @Override
        public boolean isResizable() {
            return true;
        }

        /// Applies the dimensions selected by the parent layout.
        @Override
        public void resize(double width, double height) {
            rectangle.setWidth(width);
            rectangle.setHeight(height);
        }

        /// Returns the fixed minimum width.
        @Override
        public double minWidth(double height) {
            return 40;
        }

        /// Returns a preferred width exceeding the maximum to exercise constraint clamping.
        @Override
        public double prefWidth(double height) {
            return 120;
        }

        /// Returns the fixed maximum width.
        @Override
        public double maxWidth(double height) {
            return 100;
        }

        /// Returns a minimum height proportional to the supplied width.
        @Override
        public double minHeight(double width) {
            return (width < 0 ? 100 : width) / 10;
        }

        /// Returns a preferred height exceeding the width-dependent maximum.
        @Override
        public double prefHeight(double width) {
            return (width < 0 ? 100 : width) / 2;
        }

        /// Returns the maximum height at the supplied width.
        @Override
        public double maxHeight(double width) {
            return (width < 0 ? 100 : width) / 4;
        }
    }
}
