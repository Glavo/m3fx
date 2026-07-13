// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Rendered geometry tests for Material carousel layout strategies.
@NotNullByDefault
final class M3CarouselLayoutTest {
    /// Geometry comparison tolerance for JavaFX layout rounding.
    private static final double GEOMETRY_TOLERANCE = 1.5;

    /// Starts the JavaFX toolkit before controls and stages are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies default finite behavior, layout properties, and variant style classes.
    @Test
    void exposesMaterialLayoutPropertyAndVariantStyles() {
        M3Carousel carousel = new M3Carousel();

        assertEquals(M3CarouselLayout.UNCONTAINED, carousel.getCarouselLayout());
        assertFalse(carousel.isWrapAround());
        assertTrue(carousel.getStyleClass().contains("m3-carousel-uncontained"));

        for (M3CarouselLayout layout : M3CarouselLayout.values()) {
            carousel.setCarouselLayout(layout);
            assertEquals(layout, carousel.getCarouselLayout());
            assertTrue(carousel.getStyleClass().contains(layout.styleClass()));
            assertEquals(1, countLayoutStyleClasses(carousel));
        }

        carousel.setCarouselLayout(null);

        assertEquals(M3CarouselLayout.UNCONTAINED, carousel.getCarouselLayout());
        assertTrue(carousel.getStyleClass().contains("m3-carousel-uncontained"));
    }

    /// Verifies uncontained layouts preserve authored widths and full-screen items match the viewport.
    @Test
    void rendersUncontainedAndFullScreenGeometry() {
        FxTestUtils.runOnFxThread(() -> {
            M3Carousel carousel = carousel(120.0, 180.0, 96.0);
            Stage stage = show(carousel, 480.0, 150.0);
            try {
                layout(stage, carousel, 480.0, 120.0);

                assertEquals(120.0, renderedWidth(carousel.getItems().get(0)), GEOMETRY_TOLERANCE);
                assertEquals(180.0, renderedWidth(carousel.getItems().get(1)), GEOMETRY_TOLERANCE);
                assertEquals(96.0, renderedWidth(carousel.getItems().get(2)), GEOMETRY_TOLERANCE);

                carousel.setCarouselLayout(M3CarouselLayout.UNCONTAINED_MULTI_ASPECT_RATIO);
                layout(stage, carousel, 480.0, 120.0);

                assertEquals(120.0, renderedWidth(carousel.getItems().get(0)), GEOMETRY_TOLERANCE);
                assertEquals(180.0, renderedWidth(carousel.getItems().get(1)), GEOMETRY_TOLERANCE);
                assertEquals(96.0, renderedWidth(carousel.getItems().get(2)), GEOMETRY_TOLERANCE);

                carousel.setCarouselLayout(M3CarouselLayout.FULL_SCREEN);
                layout(stage, carousel, 480.0, 120.0);
                ScrollPane viewport = viewport(carousel);
                double viewportWidth = viewport.getViewportBounds().getWidth();

                for (javafx.scene.Node item : carousel.getItems()) {
                    assertEquals(viewportWidth, renderedWidth(item), GEOMETRY_TOLERANCE);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies contained layouts produce their required focal, medium, and preview width roles.
    @Test
    void rendersContainedKeylineRoles() {
        FxTestUtils.runOnFxThread(() -> {
            M3Carousel carousel = carousel(280.0, 280.0, 280.0, 280.0, 280.0);
            carousel.selectIndex(0);
            Stage stage = show(carousel, 520.0, 170.0);
            try {
                carousel.setCarouselLayout(M3CarouselLayout.MULTI_BROWSE);
                layout(stage, carousel, 520.0, 140.0);
                double firstWidth = renderedWidth(carousel.getItems().get(0));
                double secondWidth = renderedWidth(carousel.getItems().get(1));
                double thirdWidth = renderedWidth(carousel.getItems().get(2));

                assertTrue(firstWidth > secondWidth, "multi-browse focal item must be wider than medium");
                assertTrue(secondWidth > thirdWidth, "multi-browse medium item must be wider than small");
                assertTrue(thirdWidth >= 40.0 - GEOMETRY_TOLERANCE);
                assertTrue(thirdWidth <= 56.0 + GEOMETRY_TOLERANCE);

                carousel.setCarouselLayout(M3CarouselLayout.HERO);
                layout(stage, carousel, 520.0, 140.0);
                firstWidth = renderedWidth(carousel.getItems().get(0));
                double previewWidth = renderedWidth(carousel.getItems().get(2));

                assertTrue(firstWidth > previewWidth * 4.0, "hero focal items must dominate the small preview");

                carousel.selectIndex(2);
                carousel.setCarouselLayout(M3CarouselLayout.CENTER_ALIGNED_HERO);
                layout(stage, carousel, 520.0, 140.0);
                double previousWidth = renderedWidth(carousel.getItems().get(1));
                double selectedWidth = renderedWidth(carousel.getItems().get(2));
                double nextWidth = renderedWidth(carousel.getItems().get(3));

                assertTrue(selectedWidth > previousWidth * 4.0);
                assertEquals(previousWidth, nextWidth, GEOMETRY_TOLERANCE);
            } finally {
                stage.close();
            }
        });
    }
    /// Verifies focal width changes expose a real intermediate rendered frame.
    @Test
    void animatesContainedFocalWidthsAcrossPulses() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Carousel> carouselReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> initialWidthReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Carousel carousel = Objects.requireNonNull(carouselReference.get(), "carousel");
                        Pane root = (Pane) carousel.getScene().getRoot();
                        root.layout();
                        double initialWidth = Objects.requireNonNull(initialWidthReference.get(), "initialWidth");
                        double currentWidth = renderedWidth(carousel.getItems().get(0));
                        return currentWidth < initialWidth - 2.0
                                && currentWidth > 56.0 + GEOMETRY_TOLERANCE;
                    },
                    () -> "carousel focal width never exposed an intermediate frame",
                    () -> {
                        M3Carousel carousel = carousel(280.0, 280.0, 280.0, 280.0, 280.0);
                        carousel.setCarouselLayout(M3CarouselLayout.MULTI_BROWSE);
                        carousel.selectIndex(0);
                        M3MotionSettings.setAnimationsEnabled(carousel, true);
                        Stage stage = show(carousel, 520.0, 150.0);
                        layout(stage, carousel, 520.0, 120.0);
                        stageReference.set(stage);
                        carouselReference.set(carousel);
                        initialWidthReference.set(renderedWidth(carousel.getItems().get(0)));

                        carousel.selectIndex(1);
                    },
                    () -> {
                        M3Carousel carousel = Objects.requireNonNull(carouselReference.get(), "carousel");
                        double initialWidth = Objects.requireNonNull(initialWidthReference.get(), "initialWidth");
                        double intermediateWidth = renderedWidth(carousel.getItems().get(0));
                        assertTrue(intermediateWidth < initialWidth - 2.0);
                        assertTrue(intermediateWidth > 56.0 + GEOMETRY_TOLERANCE);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3Carousel carousel = carouselReference.get();
                if (carousel != null) {
                    M3MotionSettings.clearAnimationsEnabled(carousel);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies free scrolling in snapping layouts settles on the nearest focal item.
    @Test
    void snapsFullScreenScrollingToNearestItem() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Carousel> carouselReference = new AtomicReference<>();
        AtomicReference<@Nullable ScrollPane> viewportReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3Carousel carousel = Objects.requireNonNull(carouselReference.get(), "carousel");
                        ScrollPane viewport = Objects.requireNonNull(viewportReference.get(), "viewport");
                        Pane root = (Pane) carousel.getScene().getRoot();
                        root.layout();
                        return carousel.getSelectedIndex() == 2
                                && Math.abs(viewport.getHvalue() - 2.0 / 3.0) < 0.03;
                    },
                    () -> "full-screen carousel did not snap to the nearest item",
                    () -> {
                        M3Carousel carousel = carousel(280.0, 280.0, 280.0, 280.0);
                        carousel.setCarouselLayout(M3CarouselLayout.FULL_SCREEN);
                        carousel.selectIndex(0);
                        Stage stage = show(carousel, 480.0, 150.0);
                        layout(stage, carousel, 480.0, 120.0);
                        ScrollPane viewport = viewport(carousel);

                        stageReference.set(stage);
                        carouselReference.set(carousel);
                        viewportReference.set(viewport);
                        viewport.fireEvent(new MouseEvent(
                                MouseEvent.MOUSE_DRAGGED,
                                8.0,
                                8.0,
                                8.0,
                                8.0,
                                MouseButton.PRIMARY,
                                1,
                                false,
                                false,
                                false,
                                false,
                                true,
                                false,
                                false,
                                false,
                                false,
                                false,
                                new PickResult(viewport, 8.0, 8.0)
                        ));
                        viewport.setHvalue(0.68);
                    },
                    () -> {
                        M3Carousel carousel = Objects.requireNonNull(carouselReference.get(), "carousel");
                        ScrollPane viewport = Objects.requireNonNull(viewportReference.get(), "viewport");
                        assertEquals(2, carousel.getSelectedIndex());
                        assertEquals(2.0 / 3.0, viewport.getHvalue(), 0.03);
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

    /// Verifies reduced motion removes focal expansion and keeps contained item widths stable.
    @Test
    void reducedMotionUsesStableEqualContainedWidths() {
        FxTestUtils.runOnFxThread(() -> {
            M3Carousel carousel = carousel(260.0, 260.0, 260.0, 260.0);
            carousel.setCarouselLayout(M3CarouselLayout.MULTI_BROWSE);
            carousel.selectIndex(1);
            M3MotionSettings.setAnimationsEnabled(carousel, false);
            Stage stage = show(carousel, 500.0, 150.0);
            try {
                layout(stage, carousel, 500.0, 120.0);
                double expectedWidth = renderedWidth(carousel.getItems().get(0));
                for (javafx.scene.Node item : carousel.getItems()) {
                    assertEquals(expectedWidth, renderedWidth(item), GEOMETRY_TOLERANCE);
                }

                carousel.selectIndex(2);
                layout(stage, carousel, 500.0, 120.0);
                for (javafx.scene.Node item : carousel.getItems()) {
                    assertEquals(expectedWidth, renderedWidth(item), GEOMETRY_TOLERANCE);
                }
            } finally {
                M3MotionSettings.clearAnimationsEnabled(carousel);
                stage.close();
            }
        });
    }

    /// Verifies RTL reverses physical placement and dynamic layouts preserve authored sizing properties.
    @Test
    void mirrorsPhysicalPlacementWithoutMutatingAuthoredSizes() {
        FxTestUtils.runOnFxThread(() -> {
            M3Carousel carousel = carousel(240.0, 220.0, 200.0, 180.0);
            carousel.setCarouselLayout(M3CarouselLayout.MULTI_BROWSE);
            carousel.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            carousel.selectIndex(0);
            List<Double> authoredWidths = carousel.getItems().stream()
                    .map(item -> ((StackPane) item).getPrefWidth())
                    .toList();
            Stage stage = show(carousel, 500.0, 150.0);
            try {
                layout(stage, carousel, 500.0, 120.0);

                assertTrue(
                        carousel.getItems().get(0).getBoundsInParent().getMinX()
                                > carousel.getItems().get(1).getBoundsInParent().getMinX(),
                        "logical first item must render on the right in RTL"
                );
                for (int index = 0; index < carousel.getItems().size(); index++) {
                    assertEquals(
                            authoredWidths.get(index),
                            ((StackPane) carousel.getItems().get(index)).getPrefWidth(),
                            0.0001
                    );
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Counts layout variant classes currently applied to a carousel.
    private static int countLayoutStyleClasses(M3Carousel carousel) {
        int count = 0;
        for (M3CarouselLayout layout : M3CarouselLayout.values()) {
            if (carousel.getStyleClass().contains(layout.styleClass())) {
                count++;
            }
        }
        return count;
    }

    /// Creates fixed-height carousel items with the supplied authored widths.
    private static M3Carousel carousel(double... widths) {
        M3Carousel carousel = new M3Carousel();
        for (double width : widths) {
            StackPane item = new StackPane();
            item.setMinSize(0.0, 0.0);
            item.setPrefSize(width, 88.0);
            item.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            carousel.getItems().add(item);
        }
        return carousel;
    }

    /// Shows one carousel in a real themed stage.
    private static Stage show(M3Carousel carousel, double width, double height) {
        Pane root = new Pane(carousel);
        Scene scene = new Scene(root, width, height);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        return stage;
    }

    /// Applies CSS and lays out one carousel at a stable size.
    private static void layout(Stage stage, M3Carousel carousel, double width, double height) {
        Pane root = (Pane) stage.getScene().getRoot();
        carousel.resizeRelocate(0.0, 0.0, width, height);
        root.applyCss();
        root.layout();
        root.applyCss();
        root.layout();
    }

    /// Returns the visible keyline width of one carousel item.
    private static double renderedWidth(javafx.scene.Node item) {
        javafx.scene.Node parent = item.getParent();
        if (parent != null && parent.getStyleClass().contains("m3-carousel-item-container")) {
            return parent.getBoundsInParent().getWidth();
        }
        return item.getBoundsInParent().getWidth();
    }

    /// Returns the internal carousel viewport.
    private static ScrollPane viewport(M3Carousel carousel) {
        return (ScrollPane) carousel.lookup("." + M3Carousel.VIEWPORT_STYLE_CLASS);
    }
}