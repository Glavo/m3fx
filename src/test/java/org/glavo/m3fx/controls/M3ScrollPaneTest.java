// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies M3 scroll-pane defaults, styling, and smooth scrolling behavior.
@NotNullByDefault
@Tier2Test
final class M3ScrollPaneTest {
    /// The pulse count used after a smooth scroll reaches its target position.
    private static final int SMOOTH_SCROLL_COMPLETION_STABLE_PULSES = 2;

    /// Starts JavaFX before constructing scroll panes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }
    /// Verifies that both Material scroll-pane constructors install styling and smooth wheel behavior.
    @Test
    void materialScrollPaneConstructorsApplyDefaults() {
        Region content = new Region();
        M3ScrollPane empty = new M3ScrollPane();
        M3ScrollPane explicitlyEmpty = new M3ScrollPane(null);
        M3ScrollPane populated = new M3ScrollPane(content);

        assertNull(empty.getContent());
        assertNull(explicitlyEmpty.getContent());
        assertTrue(empty.getStyleClass().contains("m3-scroll-pane"));
        assertTrue(M3ScrollPane.isSmoothScrollingEnabled(empty));
        assertSame(content, populated.getContent());
        assertTrue(populated.getStyleClass().contains("m3-scroll-pane"));
        assertTrue(M3ScrollPane.isSmoothScrollingEnabled(populated));
    }

    /// Verifies that the static behavior API remains authoritative for Material scroll panes.
    @Test
    void materialScrollPaneSupportsStaticBehaviorControl() {
        M3ScrollPane scrollPane = new M3ScrollPane();

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        assertFalse(M3ScrollPane.isSmoothScrollingEnabled(scrollPane));

        M3ScrollPane.enableSmoothScrolling(scrollPane);
        assertTrue(M3ScrollPane.isSmoothScrollingEnabled(scrollPane));
    }

    /// Verifies uninstalled smooth-scroll queries and cleanup do not allocate a node properties map.
    @Test
    void uninstalledSmoothScrollStateDoesNotAllocateProperties() {
        ScrollPane scrollPane = new ScrollPane();

        assertFalse(scrollPane.hasProperties());
        assertFalse(M3ScrollPane.isSmoothScrollingEnabled(scrollPane));
        M3ScrollPane.disableSmoothScrolling(scrollPane);
        assertFalse(scrollPane.hasProperties());
    }

    /// Verifies that repeated external scroll styling remains idempotent and follows later scene attachment.
    @Test
    void repeatedScrollPaneStylingInstallsStandaloneFallbackAfterSceneAttachment() {
        ScrollPane scrollPane = new ScrollPane();

        M3ScrollPane.style(scrollPane);
        M3ScrollPane.style(scrollPane);

        assertEquals(1, scrollPane.getStyleClass().stream()
                .filter("m3-scroll-pane"::equals)
                .count());

        StackPane firstRoot = new StackPane(scrollPane);
        Scene scene = new Scene(firstRoot);

        assertTrue(firstRoot.getStyleClass().contains("root"));
        assertEquals(1, scene.getStylesheets().stream()
                .filter(M3Stylesheets.fallbackStylesheet()::equals)
                .count());

        StackPane replacementRoot = new StackPane();
        scene.setRoot(replacementRoot);

        assertTrue(replacementRoot.getStyleClass().contains("root"));
        assertEquals(1, scene.getStylesheets().stream()
                .filter(M3Stylesheets.fallbackStylesheet()::equals)
                .count());
    }

    /// Verifies that repeated external scroll-bar styling remains idempotent after scene attachment.
    @Test
    void repeatedScrollBarStylingInstallsStandaloneFallback() {
        ScrollBar scrollBar = new ScrollBar();
        StackPane root = new StackPane(scrollBar);
        Scene scene = new Scene(root);

        M3ScrollPane.style(scrollBar);
        M3ScrollPane.style(scrollBar);

        assertEquals(1, scrollBar.getStyleClass().stream()
                .filter("m3-scroll-bar"::equals)
                .count());
        assertTrue(root.getStyleClass().contains("root"));
        assertEquals(1, scene.getStylesheets().stream()
                .filter(M3Stylesheets.fallbackStylesheet()::equals)
                .count());
    }

    /// Verifies that Material scroll styling can be applied to JavaFX scroll panes.
    @Test
    void scrollPaneMaterialStyleAppliesScrollbarColors() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        M3ScrollPane.style(scrollPane);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " -monet-surface-tint: rgb(51,52,53);");
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();

        ScrollBar scrollBar = lookupScrollBar(scrollPane, Orientation.VERTICAL);
        Region thumb = lookupRegion(scrollBar, ".thumb");
        Region track = lookupRegion(scrollBar, ".track");

        assertTrue(scrollPane.getStyleClass().contains("m3-scroll-pane"));
        assertEquals(16.0, scrollBar.prefWidth(-1.0), 0.0001);
        assertRegionFill(track, Color.TRANSPARENT);
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.48, thumb.getOpacity(), 0.0001);

        scrollBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.64, thumb.getOpacity(), 0.0001);

        scrollBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        root.applyCss();
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.78, thumb.getOpacity(), 0.0001);
    }

    /// Verifies that Material scroll styling can be applied to standalone JavaFX scroll bars.
    @Test
    void standaloneScrollBarMaterialStyleAppliesScrollbarColors() {
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        M3ScrollPane.style(scrollBar);
        StackPane root = new StackPane(scrollBar);
        Scene scene = new Scene(root, 80.0, 160.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " -monet-surface-tint: rgb(51,52,53);");
        root.applyCss();
        root.resize(80.0, 160.0);
        root.layout();

        Region thumb = lookupRegion(scrollBar, ".thumb");

        assertTrue(scrollBar.getStyleClass().contains("m3-scroll-bar"));
        assertEquals(16.0, scrollBar.prefWidth(-1.0), 0.0001);
        assertRegionFill(thumb, Color.rgb(51, 52, 53));
        assertEquals(0.48, thumb.getOpacity(), 0.0001);
    }

    /// Verifies that Material smooth scrolling can be enabled for JavaFX scroll panes.
    @Test
    void scrollPaneSmoothScrollingAnimatesWheelScroll() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
        scrollPane.fireEvent(event);

        assertTrue(M3ScrollPane.isSmoothScrollingEnabled(scrollPane));
        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
        assertFalse(M3ScrollPane.isSmoothScrollingEnabled(scrollPane));
    }

    /// Verifies that completed scroll pane smooth scrolling settles at its rendered target value.
    @Test
    void scrollPaneSmoothScrollingSettlesAtTargetAfterCompletion() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable ScrollPane> scrollPaneReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> contentReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> targetValueReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        ScrollPane scrollPane = Objects.requireNonNull(scrollPaneReference.get(), "scrollPane");
                        double targetValue = Objects.requireNonNull(targetValueReference.get(), "targetValue");
                        return Math.abs(scrollPane.getVvalue() - targetValue) <= 0.0001;
                    },
                    SMOOTH_SCROLL_COMPLETION_STABLE_PULSES,
                    () -> {
                        Region content = new Region();
                        content.setPrefSize(160.0, 480.0);
                        ScrollPane scrollPane = new ScrollPane(content);
                        scrollPane.setPrefSize(160.0, 120.0);
                        StackPane root = new StackPane(scrollPane);
                        Scene scene = new Scene(root, 180.0, 140.0);
                        Stage stage = new Stage();

                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.resize(180.0, 140.0);
                        root.layout();

                        M3ScrollPane.enableSmoothScrolling(scrollPane);
                        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                        stageReference.set(stage);
                        scrollPaneReference.set(scrollPane);
                        contentReference.set(content);
                        targetValueReference.set(expectedScrollPaneVerticalTargetValue(scrollPane, content, -80.0));

                        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
                        scrollPane.fireEvent(event);
                        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
                        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);
                    },
                    () -> {
                        ScrollPane scrollPane = Objects.requireNonNull(scrollPaneReference.get(), "scrollPane");
                        Region content = Objects.requireNonNull(contentReference.get(), "content");
                        double targetValue = Objects.requireNonNull(targetValueReference.get(), "targetValue");
                        assertEquals(targetValue, scrollPane.getVvalue(), 0.0001, () -> scrollPaneDebug(
                                scrollPane,
                                content,
                                scrollEvent(scrollPane, 0.0, -80.0)
                        ));
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable ScrollPane scrollPane = scrollPaneReference.get();
                if (scrollPane != null) {
                    M3ScrollPane.disableSmoothScrolling(scrollPane);
                    M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that an overshooting spatial fraction is clipped at either end of a scroll axis.
    @Test
    void scrollPaneInterpolationClipsSpatialOvershootAtAxisBounds() {
        double overshootingFraction = M3MotionScheme.expressive()
                .defaultSpatial()
                .interpolator()
                .interpolate(0.0, 1.0, 0.6);

        assertTrue(overshootingFraction > 1.0, () -> "fraction=" + overshootingFraction);
        assertEquals(1.0, M3ScrollPane.interpolateScrollValue(
                0.0,
                1.0,
                overshootingFraction,
                0.0,
                1.0
        ));
        assertEquals(0.0, M3ScrollPane.interpolateScrollValue(
                1.0,
                0.0,
                overshootingFraction,
                0.0,
                1.0
        ));
        assertEquals(0.0, M3ScrollPane.interpolateScrollValue(
                Double.NaN,
                1.0,
                0.5,
                0.0,
                1.0
        ));
    }

    /// Verifies that spatial easing never exposes scroll values beyond either configured axis range.
    @Test
    void scrollPaneSmoothScrollingKeepsObservableValuesWithinAxisRanges() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable ScrollPane> scrollPaneReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> invalidHValueReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> invalidVValueReference = new AtomicReference<>();
        AtomicLong verificationDeadlineNanos = new AtomicLong();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> System.nanoTime() >= verificationDeadlineNanos.get(),
                    1,
                    () -> {
                        Region content = new Region();
                        content.setPrefSize(480.0, 480.0);
                        ScrollPane scrollPane = new ScrollPane(content);
                        scrollPane.setPrefSize(160.0, 120.0);
                        StackPane root = new StackPane(scrollPane);
                        Scene scene = new Scene(root, 180.0, 140.0);
                        Stage stage = new Stage();

                        M3ThemeManager.install(scene, M3Theme.fromSeed(
                                Color.web("#6750A4"),
                                M3Profile.EXPRESSIVE_2025,
                                Brightness.LIGHT
                        ));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.resize(180.0, 140.0);
                        root.layout();

                        scrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> {
                            double value = newValue.doubleValue();
                            if (!Double.isFinite(value)
                                    || value < scrollPane.getHmin()
                                    || value > scrollPane.getHmax()) {
                                invalidHValueReference.compareAndSet(null, value);
                            }
                        });
                        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                            double value = newValue.doubleValue();
                            if (!Double.isFinite(value)
                                    || value < scrollPane.getVmin()
                                    || value > scrollPane.getVmax()) {
                                invalidVValueReference.compareAndSet(null, value);
                            }
                        });
                        M3ScrollPane.enableSmoothScrolling(scrollPane);
                        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                        stageReference.set(stage);
                        scrollPaneReference.set(scrollPane);
                        verificationDeadlineNanos.set(System.nanoTime() + (long) (
                                (M3MotionScheme.expressive().defaultSpatial().duration().toMillis() + 150.0)
                                        * 1_000_000.0
                        ));

                        ScrollEvent event = scrollEvent(scrollPane, -10_000.0, -10_000.0);
                        scrollPane.fireEvent(event);
                        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
                        assertEquals(scrollPane.getHmin(), scrollPane.getHvalue(), 0.0001);
                        assertEquals(scrollPane.getVmin(), scrollPane.getVvalue(), 0.0001);
                    },
                    () -> {
                        ScrollPane scrollPane = Objects.requireNonNull(scrollPaneReference.get(), "scrollPane");
                        assertNull(invalidHValueReference.get(),
                                () -> "out-of-range hvalue=" + invalidHValueReference.get());
                        assertNull(invalidVValueReference.get(),
                                () -> "out-of-range vvalue=" + invalidVValueReference.get());
                        assertEquals(scrollPane.getHmax(), scrollPane.getHvalue(), 0.0001);
                        assertEquals(scrollPane.getVmax(), scrollPane.getVvalue(), 0.0001);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable ScrollPane scrollPane = scrollPaneReference.get();
                if (scrollPane != null) {
                    M3ScrollPane.disableSmoothScrolling(scrollPane);
                    M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that a running smooth scroll settles when animations are disabled at runtime.
    @Test
    void scrollPaneSmoothScrollingSettlesWhenAnimationsAreDisabledAtRuntime() {
        FxTestUtils.runOnFxThread(() -> {
            Region content = new Region();
            content.setPrefSize(160.0, 480.0);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(160.0, 120.0);
            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 180.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(180.0, 140.0);
            root.layout();
            M3ScrollPane.enableSmoothScrolling(scrollPane);
            M3MotionSettings.setReducedMotionRequested(scrollPane, false);
            try {
                ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
                scrollPane.fireEvent(event);

                assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
                assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

                M3MotionSettings.setReducedMotionRequested(scrollPane, true);

                assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            } finally {
                M3ScrollPane.disableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);
            }
        });
    }

    /// Verifies that hiding the presenting window settles a running smooth scroll and releases its observer.
    @Test
    void scrollPaneSmoothScrollingSettlesWhenWindowHides() {
        FxTestUtils.runOnFxThread(() -> {
            Region content = new Region();
            content.setPrefSize(160.0, 480.0);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(160.0, 120.0);
            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 180.0, 140.0);
            Stage stage = new Stage();

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();
            M3ScrollPane.enableSmoothScrolling(scrollPane);
            M3MotionSettings.setReducedMotionRequested(scrollPane, false);
            try {
                ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
                scrollPane.fireEvent(event);

                assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
                assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

                stage.hide();

                assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            } finally {
                M3ScrollPane.disableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                stage.close();
            }
        });
    }

    /// Verifies that disabled animation settings make smooth scrolling finish synchronously.
    @Test
    void scrollPaneSmoothScrollingHonorsDisabledAnimations() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that vertical wheel input scrolls horizontally when only the horizontal axis can scroll.
    @Test
    void scrollPaneSmoothScrollingMapsWheelToHorizontalOnlyContent() {
        Region content = new Region();
        content.setPrefSize(480.0, 80.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0);
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that explicit horizontal wheel input moves the horizontal axis without changing vertical position.
    @Test
    void scrollPaneSmoothScrollingHandlesHorizontalWheelInput() {
        Region content = new Region();
        content.setPrefSize(480.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        double expectedHValue = expectedScrollPaneHorizontalTargetValue(scrollPane, content, -80.0);
        ScrollEvent event = scrollEvent(scrollPane, -80.0, 0.0);
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(expectedHValue, scrollPane.getHvalue(), 0.0001, () -> scrollPaneDebug(
                scrollPane,
                content,
                event
        ));
        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that Shift+wheel maps vertical wheel input to the horizontal axis when both axes can scroll.
    @Test
    void scrollPaneSmoothScrollingMapsShiftWheelToHorizontalAxis() {
        Region content = new Region();
        content.setPrefSize(480.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        double expectedHValue = expectedScrollPaneHorizontalTargetValue(scrollPane, content, -80.0);
        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0, false, true);
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(expectedHValue, scrollPane.getHvalue(), 0.0001, () -> scrollPaneDebug(
                scrollPane,
                content,
                event
        ));
        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that Shift+wheel keeps vertical scrolling when the horizontal axis cannot scroll.
    @Test
    void scrollPaneSmoothScrollingKeepsShiftWheelVerticalWhenHorizontalAxisCannotScroll() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        double expectedVValue = expectedScrollPaneVerticalTargetValue(scrollPane, content, -80.0);
        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0, false, true);
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(0.0, scrollPane.getHvalue(), 0.0001);
        assertEquals(expectedVValue, scrollPane.getVvalue(), 0.0001, () -> scrollPaneDebug(
                scrollPane,
                content,
                event
        ));

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that horizontal character scroll units are converted to Material wheel distances.
    @Test
    void scrollPaneSmoothScrollingUsesHorizontalCharacterScrollUnits() {
        Region content = new Region();
        content.setPrefSize(480.0, 120.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        double expectedHValue = expectedScrollPaneHorizontalTargetValue(scrollPane, content, -80.0);
        ScrollEvent event = scrollEvent(
                scrollPane,
                0.0,
                0.0,
                false,
                false,
                ScrollEvent.HorizontalTextScrollUnits.CHARACTERS,
                -2.0,
                ScrollEvent.VerticalTextScrollUnits.NONE,
                0.0
        );
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(expectedHValue, scrollPane.getHvalue(), 0.0001, () -> scrollPaneDebug(
                scrollPane,
                content,
                event
        ));
        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that wheel events accumulate while a smooth scroll animation is still running.
    @Test
    void scrollPaneSmoothScrollingAccumulatesWheelEventsWhileAnimationRuns() {
        FxTestUtils.runOnFxThread(() -> {
            Region content = new Region();
            content.setPrefSize(160.0, 480.0);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(160.0, 120.0);
            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 180.0, 140.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                M3ScrollPane.enableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);

                double expectedAccumulatedVValue = expectedScrollPaneVerticalTargetValue(scrollPane, content, -160.0);
                ScrollEvent firstEvent = scrollEvent(scrollPane, 0.0, -80.0);
                scrollPane.fireEvent(firstEvent);
                ScrollEvent secondEvent = scrollEvent(scrollPane, 0.0, -80.0);
                scrollPane.fireEvent(secondEvent);

                assertTrue(firstEvent.isConsumed(), () -> scrollPaneDebug(scrollPane, content, firstEvent));
                assertTrue(secondEvent.isConsumed(), () -> scrollPaneDebug(scrollPane, content, secondEvent));
                assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

                M3MotionSettings.setReducedMotionRequested(scrollPane, true);

                assertEquals(expectedAccumulatedVValue, scrollPane.getVvalue(), 0.0001, () -> scrollPaneDebug(
                        scrollPane,
                        content,
                        secondEvent
                ));
            } finally {
                M3ScrollPane.disableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                stage.close();
            }
        });
    }

    /// Verifies that in-flight smooth scroll targets keep pixel distance when content height changes.
    @Test
    void scrollPaneSmoothScrollingPreservesPixelTargetWhenContentHeightChangesDuringAnimation() {
        FxTestUtils.runOnFxThread(() -> {
            Region content = new Region();
            content.setPrefSize(160.0, 480.0);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(160.0, 120.0);
            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 180.0, 140.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                M3ScrollPane.enableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);

                ScrollEvent firstEvent = scrollEvent(scrollPane, 0.0, -80.0);
                scrollPane.fireEvent(firstEvent);
                assertTrue(firstEvent.isConsumed(), () -> scrollPaneDebug(scrollPane, content, firstEvent));
                assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

                content.setPrefHeight(960.0);
                root.applyCss();
                root.layout();

                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double expandedScrollablePixels = Math.max(
                        content.getBoundsInLocal().getHeight(),
                        content.prefHeight(-1.0)
                ) - viewportHeight;
                assertTrue(expandedScrollablePixels > 0.0,
                        () -> scrollPaneDebug(scrollPane, content, firstEvent));
                double expectedAccumulatedVValue = scrollPane.getVmin()
                        + 160.0 / expandedScrollablePixels * (scrollPane.getVmax() - scrollPane.getVmin());

                ScrollEvent secondEvent = scrollEvent(scrollPane, 0.0, -80.0);
                scrollPane.fireEvent(secondEvent);
                assertTrue(secondEvent.isConsumed(), () -> scrollPaneDebug(scrollPane, content, secondEvent));
                M3MotionSettings.setReducedMotionRequested(scrollPane, true);

                assertEquals(expectedAccumulatedVValue, scrollPane.getVvalue(), 0.0001, () -> scrollPaneDebug(
                        scrollPane,
                        content,
                        secondEvent
                ));
            } finally {
                M3ScrollPane.disableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                stage.close();
            }
        });
    }

    /// Verifies that unchanged content geometry is not measured again for every wheel event.
    @Test
    void scrollPaneSmoothScrollingCachesStableContentMetrics() {
        FxTestUtils.runOnFxThread(() -> {
            CountingContent content = new CountingContent();
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(160.0, 120.0);
            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 180.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(180.0, 140.0);
            root.layout();
            M3ScrollPane.enableSmoothScrolling(scrollPane);
            M3MotionSettings.setReducedMotionRequested(scrollPane, true);

            try {
                int initialWidthMeasurements = content.preferredWidthMeasurements;
                int initialHeightMeasurements = content.preferredHeightMeasurements;
                ScrollEvent firstEvent = scrollEvent(scrollPane, 0.0, -40.0);
                scrollPane.fireEvent(firstEvent);

                assertTrue(firstEvent.isConsumed(), () -> scrollPaneDebug(scrollPane, content, firstEvent));
                assertTrue(content.preferredWidthMeasurements > initialWidthMeasurements);
                assertTrue(content.preferredHeightMeasurements > initialHeightMeasurements);
                int stableWidthMeasurements = content.preferredWidthMeasurements;
                int stableHeightMeasurements = content.preferredHeightMeasurements;

                ScrollEvent secondEvent = scrollEvent(scrollPane, 0.0, -40.0);
                scrollPane.fireEvent(secondEvent);

                assertTrue(secondEvent.isConsumed(), () -> scrollPaneDebug(scrollPane, content, secondEvent));
                assertEquals(stableWidthMeasurements, content.preferredWidthMeasurements);
                assertEquals(stableHeightMeasurements, content.preferredHeightMeasurements);
            } finally {
                M3ScrollPane.disableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);
            }
        });
    }

    /// Verifies that platform text-line scroll units are converted to Material wheel distances.
    @Test
    void scrollPaneSmoothScrollingUsesTextLineScrollUnits() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        double expectedVValue = expectedScrollPaneVerticalTargetValue(scrollPane, content, -120.0);
        ScrollEvent event = scrollEvent(
                scrollPane,
                0.0,
                0.0,
                false,
                false,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0.0,
                ScrollEvent.VerticalTextScrollUnits.LINES,
                -3.0
        );
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(expectedVValue, scrollPane.getVvalue(), 0.0001, () -> scrollPaneDebug(
                scrollPane,
                content,
                event
        ));

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that platform page scroll units use the current viewport height.
    @Test
    void scrollPaneSmoothScrollingUsesPageScrollUnits() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, true);

        double expectedVValue = expectedScrollPaneVerticalTargetValue(
                scrollPane,
                content,
                -scrollPane.getViewportBounds().getHeight()
        );
        ScrollEvent event = scrollEvent(
                scrollPane,
                0.0,
                0.0,
                false,
                false,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0.0,
                ScrollEvent.VerticalTextScrollUnits.PAGES,
                -1.0
        );
        scrollPane.fireEvent(event);

        assertTrue(event.isConsumed(), () -> scrollPaneDebug(scrollPane, content, event));
        assertEquals(expectedVValue, scrollPane.getVvalue(), 0.0001, () -> scrollPaneDebug(
                scrollPane,
                content,
                event
        ));

        M3ScrollPane.disableSmoothScrolling(scrollPane);
        M3MotionSettings.setReducedMotionRequested(scrollPane, false);
    }

    /// Verifies that outer smooth scroll panes do not consume wheel events owned by nested virtualized lists.
    @Test
    void scrollPaneSmoothScrollingIgnoresNestedVirtualFlowScrollEvents() {
        FxTestUtils.assertNoM3CssTokenWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int index = 0; index < 100; index++) {
                listView.getItems().add(index);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);

            Region filler = new Region();
            filler.setPrefSize(260.0, 360.0);
            VBox content = new VBox(listView, filler);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setPrefSize(300.0, 180.0);
            M3ScrollPane.style(scrollPane);
            StackPane root = new StackPane(scrollPane);
            Scene scene = new Scene(root, 340.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                M3ScrollPane.enableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, true);
                M3MotionSettings.setReducedMotionRequested(listView, true);

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );

                Node cell = assertInstanceOf(
                        Node.class,
                        listView.lookup("." + "m3-list-view-cell")
                );

                assertFalse(M3ScrollPane.isEventTargetForScrollPane(scrollPane, listView));
                assertFalse(M3ScrollPane.isEventTargetForScrollPane(scrollPane, flow));
                assertFalse(M3ScrollPane.isEventTargetForScrollPane(scrollPane, cell));
                assertTrue(M3ScrollPane.isEventTargetForScrollPane(scrollPane, filler));
                assertEquals(0.0, scrollPane.getVvalue(), 0.0001);
            } finally {
                M3ScrollPane.disableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);
                M3MotionSettings.setReducedMotionRequested(listView, false);
                stage.close();
            }
        }));
    }

    /// Verifies that common nested scroll controls keep wheel ownership when they are direct event targets.
    @Test
    void scrollPaneSmoothScrollingClassifiesCommonNestedScrollOwners() {
        FxTestUtils.assertNoM3CssTokenWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            TextArea textArea = new TextArea("Line 1\nLine 2\nLine 3");
            M3TextArea materialTextArea = new M3TextArea("Line 1\nLine 2\nLine 3");
            ListView<String> listView = new ListView<>();
            listView.getItems().setAll("Alpha", "Beta", "Gamma");
            TreeView<String> treeView = new TreeView<>();
            TableView<String> tableView = new TableView<>();
            TreeTableView<String> treeTableView = new TreeTableView<>();
            Region filler = new Region();
            filler.setPrefSize(240.0, 160.0);
            VBox content = new VBox(
                    textArea,
                    materialTextArea,
                    listView,
                    treeView,
                    tableView,
                    treeTableView,
                    filler
            );
            ScrollPane outerScrollPane = new ScrollPane(content);
            outerScrollPane.setPrefSize(280.0, 180.0);
            M3ScrollPane.style(outerScrollPane);
            StackPane root = new StackPane(outerScrollPane);
            Scene scene = new Scene(root, 320.0, 220.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, textArea));
            assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, materialTextArea));
            assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, listView));
            assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, treeView));
            assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, tableView));
            assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, treeTableView));
            assertTrue(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, filler));
        }));
    }

    /// Verifies that nested smooth scroll panes retain ownership of their own wheel events.
    @Test
    void scrollPaneSmoothScrollingIgnoresNestedScrollPaneTargets() {
        FxTestUtils.assertNoM3CssTokenWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            Region innerContent = new Region();
            innerContent.setPrefSize(180.0, 480.0);
            ScrollPane innerScrollPane = new ScrollPane(innerContent);
            innerScrollPane.setPrefSize(220.0, 140.0);
            M3ScrollPane.style(innerScrollPane);

            Region filler = new Region();
            filler.setPrefSize(220.0, 360.0);
            VBox content = new VBox(innerScrollPane, filler);
            ScrollPane outerScrollPane = new ScrollPane(content);
            outerScrollPane.setPrefSize(260.0, 180.0);
            M3ScrollPane.style(outerScrollPane);
            StackPane root = new StackPane(outerScrollPane);
            Scene scene = new Scene(root, 320.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                M3ScrollPane.enableSmoothScrolling(outerScrollPane);
                M3ScrollPane.enableSmoothScrolling(innerScrollPane);
                M3MotionSettings.setReducedMotionRequested(outerScrollPane, true);
                M3MotionSettings.setReducedMotionRequested(innerScrollPane, true);

                assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, innerScrollPane));
                assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, innerContent));
                assertTrue(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, filler));

                ScrollEvent event = scrollEvent(innerScrollPane, 0.0, -80.0);
                innerScrollPane.fireEvent(event);

                assertTrue(innerScrollPane.getVvalue() > 0.0, () -> "inner.vvalue=" + innerScrollPane.getVvalue());
                assertEquals(0.0, outerScrollPane.getVvalue(), 0.0001);
            } finally {
                M3ScrollPane.disableSmoothScrolling(innerScrollPane);
                M3ScrollPane.disableSmoothScrolling(outerScrollPane);
                M3MotionSettings.setReducedMotionRequested(innerScrollPane, false);
                M3MotionSettings.setReducedMotionRequested(outerScrollPane, false);
                stage.close();
            }
        }));
    }

    /// Verifies that direct touch scroll events are left to JavaFX's native panning behavior.
    @Test
    void scrollPaneSmoothScrollingIgnoresDirectScrollEvents() {
        Region content = new Region();
        content.setPrefSize(160.0, 480.0);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(160.0, 120.0);
        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 180.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.resize(180.0, 140.0);
        root.layout();
        M3ScrollPane.enableSmoothScrolling(scrollPane);

        ScrollEvent event = scrollEvent(scrollPane, 0.0, -80.0, true);
        scrollPane.fireEvent(event);

        assertEquals(0.0, scrollPane.getVvalue(), 0.0001);

        M3ScrollPane.disableSmoothScrolling(scrollPane);
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

            M3ScrollPane.enableSmoothScrolling(scrollPane);
            M3MotionSettings.setReducedMotionRequested(scrollPane, true);
            try {
                content.setExpanded(true);

                ScrollEvent event = new ScrollEvent(
                        scrollPane,
                        scrollPane,
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
                        -80.0,
                        0.0,
                        -80.0,
                        ScrollEvent.HorizontalTextScrollUnits.NONE,
                        0.0,
                        ScrollEvent.VerticalTextScrollUnits.NONE,
                        0.0,
                        0,
                        null
                );
                scrollPane.fireEvent(event);

                assertTrue(event.isConsumed(), () -> "vvalue=" + scrollPane.getVvalue()
                        + ", viewport=" + scrollPane.getViewportBounds()
                        + ", bounds=" + content.getBoundsInLocal()
                        + ", prefHeightAtViewport=" + content.prefHeight(scrollPane.getViewportBounds().getWidth())
                );
                assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            } finally {
                M3ScrollPane.disableSmoothScrolling(scrollPane);
                M3MotionSettings.setReducedMotionRequested(scrollPane, false);
            }
        });
    }

    /// Returns a region looked up below a node.
    private static Region lookupRegion(Node node, String selector) {
        Node child = node.lookup(selector);
        assertInstanceOf(Region.class, child);
        return (Region) child;
    }

    /// Returns a scroll bar with the requested orientation from a parent node.
    private static ScrollBar lookupScrollBar(Parent parent, Orientation orientation) {
        for (Node node : parent.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == orientation) {
                return scrollBar;
            }
        }
        throw new AssertionError("Missing " + orientation + " scroll bar below " + parent);
    }

    /// Verifies the first background fill for a region.
    private static void assertRegionFill(Region region, Color expectedFill) {
        assertEquals(1, region.getBackground().getFills().size());
        assertEquals(expectedFill, region.getBackground().getFills().get(0).getFill());
    }

    /// Creates an indirect scroll event for scroll behavior tests.
    private static ScrollEvent scrollEvent(Node target, double deltaX, double deltaY) {
        return scrollEvent(target, deltaX, deltaY, false);
    }

    /// Returns the expected vertical value after a scroll pane wheel-scroll delta.
    private static double expectedScrollPaneVerticalTargetValue(
            ScrollPane scrollPane,
            Region content,
            double deltaY
    ) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double contentHeight = Math.max(content.getBoundsInLocal().getHeight(), content.prefHeight(-1.0));
        double scrollablePixels = contentHeight - viewportHeight;
        assertTrue(scrollablePixels > 0.0,
                () -> scrollPaneDebug(scrollPane, content, scrollEvent(scrollPane, 0.0, deltaY)));
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        double currentPixels = (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange * scrollablePixels;
        double targetPixels = clamp(currentPixels - deltaY, 0.0, scrollablePixels);
        return scrollPane.getVmin() + targetPixels / scrollablePixels * valueRange;
    }

    /// Returns the expected horizontal value after a scroll pane wheel-scroll delta.
    private static double expectedScrollPaneHorizontalTargetValue(
            ScrollPane scrollPane,
            Region content,
            double deltaX
    ) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double contentWidth = Math.max(content.getBoundsInLocal().getWidth(), content.prefWidth(-1.0));
        double scrollablePixels = contentWidth - viewportWidth;
        assertTrue(scrollablePixels > 0.0,
                () -> scrollPaneDebug(scrollPane, content, scrollEvent(scrollPane, deltaX, 0.0)));
        double valueRange = scrollPane.getHmax() - scrollPane.getHmin();
        double currentPixels = (scrollPane.getHvalue() - scrollPane.getHmin()) / valueRange * scrollablePixels;
        double targetPixels = clamp(currentPixels - deltaX, 0.0, scrollablePixels);
        return scrollPane.getHmin() + targetPixels / scrollablePixels * valueRange;
    }

    /// Returns a value clamped into a closed range.
    private static double clamp(double value, double minValue, double maxValue) {
        if (value <= minValue) {
            return minValue;
        }
        return Math.min(value, maxValue);
    }

    /// Creates a scroll event for scroll behavior tests.
    private static ScrollEvent scrollEvent(Node target, double deltaX, double deltaY, boolean direct) {
        return scrollEvent(target, deltaX, deltaY, direct, false);
    }

    /// Creates a scroll event for scroll behavior tests.
    private static ScrollEvent scrollEvent(
            Node target,
            double deltaX,
            double deltaY,
            boolean direct,
            boolean shiftDown
    ) {
        return scrollEvent(
                target,
                deltaX,
                deltaY,
                direct,
                shiftDown,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0.0,
                ScrollEvent.VerticalTextScrollUnits.NONE,
                0.0
        );
    }

    /// Creates a scroll event with explicit platform text-scroll units for scroll behavior tests.
    private static ScrollEvent scrollEvent(
            Node target,
            double deltaX,
            double deltaY,
            boolean direct,
            boolean shiftDown,
            ScrollEvent.HorizontalTextScrollUnits horizontalUnits,
            double textDeltaX,
            ScrollEvent.VerticalTextScrollUnits verticalUnits,
            double textDeltaY
    ) {
        return new ScrollEvent(
                target,
                target,
                ScrollEvent.SCROLL,
                40.0,
                40.0,
                40.0,
                40.0,
                shiftDown,
                false,
                false,
                false,
                direct,
                false,
                deltaX,
                deltaY,
                deltaX,
                deltaY,
                horizontalUnits,
                textDeltaX,
                verticalUnits,
                textDeltaY,
                0,
                null
        );
    }

    /// Returns diagnostic geometry for scroll behavior assertions.
    private static String scrollPaneDebug(ScrollPane scrollPane, Region content, ScrollEvent event) {
        return "viewport=" + scrollPane.getViewportBounds()
                + ", contentBounds=" + content.getBoundsInLocal()
                + ", prefHeight=" + content.prefHeight(-1.0)
                + ", vmin=" + scrollPane.getVmin()
                + ", vmax=" + scrollPane.getVmax()
                + ", vvalue=" + scrollPane.getVvalue()
                + ", deltaY=" + event.getDeltaY()
                + ", textDeltaY=" + event.getTextDeltaY()
                + ", textDeltaYUnits=" + event.getTextDeltaYUnits();
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

    /// Region that records preferred-size computations for scroll performance assertions.
    @NotNullByDefault
    private static final class CountingContent extends Region {
        /// The number of preferred-width computations.
        private int preferredWidthMeasurements;

        /// The number of preferred-height computations.
        private int preferredHeightMeasurements;

        /// Computes the preferred width and records the measurement.
        @Override
        protected double computePrefWidth(double height) {
            preferredWidthMeasurements++;
            return 160.0;
        }

        /// Computes the preferred height and records the measurement.
        @Override
        protected double computePrefHeight(double width) {
            preferredHeightMeasurements++;
            return 480.0;
        }
    }
}
