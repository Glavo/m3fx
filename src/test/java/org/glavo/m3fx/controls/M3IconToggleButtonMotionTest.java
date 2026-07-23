// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3IconPaints;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the fast, deterministic selection motion contract of [M3IconToggleButton].
@NotNullByDefault
final class M3IconToggleButtonMotionTest {
    /// A test-local duration long enough to expose intermediate color and shape frames reliably.
    private static final Duration OBSERVABLE_DURATION = Duration.millis(400.0);

    /// Starts the JavaFX toolkit used by detached-scene animation tests.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies selected and unselected targets interpolate without changing button or sibling layout metrics.
    @Tier2Test
    @Test
    void selectionInterpolatesContainerAndContentWithoutRelayout() throws InterruptedException {
        MotionFixture fixture = createFixture(false);
        try {
            Color[] startContainer = new Color[1];
            Color[] startContent = new Color[1];
            Color[] targetContainer = new Color[1];
            Color[] targetContent = new Color[1];
            LayoutMetrics[] layout = new LayoutMetrics[1];

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> isIntermediate(
                            renderedContainerColor(fixture.button),
                            startContainer[0],
                            targetContainer[0]
                    ) && isIntermediate(
                            renderedContentColor(fixture.icon),
                            startContent[0],
                            targetContent[0]
                    ),
                    1,
                    () -> {
                        startContainer[0] = assertInstanceOf(Color.class, fixture.button.getContainerColor());
                        startContent[0] = assertInstanceOf(Color.class, fixture.button.getContentColor());
                        layout[0] = captureLayout(fixture);
                        click(fixture.button);
                        assertTrue(fixture.button.isSelected());
                        targetContainer[0] = assertInstanceOf(Color.class, fixture.button.getContainerColor());
                        targetContent[0] = assertInstanceOf(Color.class, fixture.button.getContentColor());
                        assertNotEquals(startContainer[0], targetContainer[0]);
                        assertNotEquals(startContent[0], targetContent[0]);
                    },
                    () -> {
                        assertLayoutEquals(layout[0], captureLayout(fixture));
                        assertEquals(renderedContentColor(fixture.icon), fixture.button.getTextFill());
                    }
            );

            awaitSettledPaints(fixture, targetContainer[0], targetContent[0], layout[0]);

            Color selectedContainer = targetContainer[0];
            Color selectedContent = targetContent[0];
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> isIntermediate(
                            renderedContainerColor(fixture.button),
                            targetContainer[0],
                            selectedContainer
                    ) && isIntermediate(
                            renderedContentColor(fixture.icon),
                            targetContent[0],
                            selectedContent
                    ),
                    1,
                    () -> {
                        click(fixture.button);
                        assertFalse(fixture.button.isSelected());
                        targetContainer[0] = assertInstanceOf(Color.class, fixture.button.getContainerColor());
                        targetContent[0] = assertInstanceOf(Color.class, fixture.button.getContentColor());
                    },
                    () -> assertLayoutEquals(layout[0], captureLayout(fixture))
            );
            awaitSettledPaints(fixture, targetContainer[0], targetContent[0], layout[0]);
        } finally {
            disposeFixture(fixture);
        }
    }

    /// Verifies reduced motion settles selection colors and Expressive shape synchronously.
    @Test
    void reducedMotionSettlesSelectionSynchronously() {
        MotionFixture fixture = createFixture(true);
        try {
            FxTestUtils.runOnFxThread(() -> {
                LayoutMetrics layout = captureLayout(fixture);
                Color startContainer = assertInstanceOf(Color.class, fixture.button.getContainerColor());
                Color startContent = assertInstanceOf(Color.class, fixture.button.getContentColor());

                click(fixture.button);
                assertTrue(fixture.button.isSelected());

                Color targetContainer = assertInstanceOf(Color.class, fixture.button.getContainerColor());
                Color targetContent = assertInstanceOf(Color.class, fixture.button.getContentColor());
                assertNotEquals(startContainer, targetContainer);
                assertNotEquals(startContent, targetContent);
                assertEquals(targetContainer, renderedContainerColor(fixture.button));
                assertEquals(targetContent, renderedContentColor(fixture.icon));
                assertEquals(targetContent, fixture.button.getTextFill());
                assertNull(fixture.button.getShape());
                assertLayoutEquals(layout, captureLayout(fixture));
            });
        } finally {
            disposeFixture(fixture);
        }
    }

    /// Creates a detached themed scene whose animation pulses remain deterministic and inexpensive.
    ///
    /// @param reducedMotion whether the fixture requests reduced motion
    /// @return the initialized motion fixture
    private static MotionFixture createFixture(boolean reducedMotion) {
        MotionFixture[] result = new MotionFixture[1];
        FxTestUtils.runOnFxThread(() -> {
            M3Icon icon = new M3Icon("favorite");
            M3IconToggleButton button = new M3IconToggleButton(icon);
            button.setVariant(M3IconToggleButtonVariant.TONAL);
            button.setButtonShape(M3ButtonShape.ROUND);

            Region trailing = new Region();
            trailing.setPrefSize(48.0, 40.0);
            HBox root = new HBox(20.0, button, trailing);
            Scene scene = new Scene(root, 260.0, 100.0);
            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#6750A4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            M3MotionSettings.setReducedMotionRequested(root, reducedMotion);
            FxTestUtils.setMotionScheme(root, observableMotionScheme());
            root.applyCss();
            root.layout();

            result[0] = new MotionFixture(root, button, icon, trailing);
        });
        return Objects.requireNonNull(result[0], "motion fixture");
    }

    /// Clears test-local animation settings and detaches the fixture scene.
    ///
    /// @param fixture the fixture to dispose
    private static void disposeFixture(MotionFixture fixture) {
        FxTestUtils.runOnFxThread(() -> {
            M3MotionSettings.setReducedMotionRequested(fixture.root, false);
            FxTestUtils.clearMotionScheme(fixture.root);
            Scene scene = Objects.requireNonNull(fixture.root.getScene(), "fixture scene");
            scene.setRoot(new Region());
        });
    }

    /// Waits for the current selection transition to settle at its target paints.
    ///
    /// @param fixture        the active fixture
    /// @param containerColor the expected settled container color
    /// @param contentColor   the expected settled content color
    /// @param layout         the invariant layout metrics
    private static void awaitSettledPaints(
            MotionFixture fixture,
            Color containerColor,
            Color contentColor,
            LayoutMetrics layout
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> containerColor.equals(renderedContainerColor(fixture.button))
                        && contentColor.equals(renderedContentColor(fixture.icon))
                        && fixture.button.getShape() == null,
                2,
                () -> {
                },
                () -> {
                    assertEquals(containerColor, renderedContainerColor(fixture.button));
                    assertEquals(contentColor, renderedContentColor(fixture.icon));
                    assertLayoutEquals(layout, captureLayout(fixture));
                }
        );
    }

    /// Performs one primary-button click through the control's mouse behavior.
    ///
    /// @param button the button to click
    private static void click(M3IconToggleButton button) {
        double x = button.getWidth() / 2.0;
        double y = button.getHeight() / 2.0;
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, x, y, true));
        button.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, x, y, false));
    }

    /// Creates a primary mouse event for the requested local point.
    ///
    /// @param eventType         the mouse event type
    /// @param x                 the local horizontal coordinate
    /// @param y                 the local vertical coordinate
    /// @param primaryButtonDown whether the primary button is held
    /// @return the new mouse event
    private static MouseEvent primaryMouseEvent(
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        return new MouseEvent(
                eventType,
                x,
                y,
                x,
                y,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                null
        );
    }

    /// Returns whether a color is strictly between two distinct endpoints.
    ///
    /// @param actual the rendered color
    /// @param start  the start color
    /// @param target the target color
    /// @return `true` when `actual` differs from both endpoints
    private static boolean isIntermediate(Color actual, Color start, Color target) {
        return !actual.equals(start) && !actual.equals(target);
    }

    /// Returns the concrete container color rendered by the retained skin node.
    ///
    /// @param button the toggle button to inspect
    /// @return the rendered container color
    private static Color renderedContainerColor(M3IconToggleButton button) {
        Node node = Objects.requireNonNull(button.lookup(".m3-container-paint"), "container paint node");
        Region region = assertInstanceOf(Region.class, node);
        assertNotNull(region.getBackground());
        Paint paint = region.getBackground().getFills().get(0).getFill();
        return assertInstanceOf(Color.class, paint);
    }

    /// Returns the concrete content color inherited by a direct M3FX icon.
    ///
    /// @param icon the icon to inspect
    /// @return the rendered content color
    private static Color renderedContentColor(M3Icon icon) {
        Paint paint = Objects.requireNonNull(M3IconPaints.inheritedPaintProperty(icon).get(), "inherited paint");
        return assertInstanceOf(Color.class, paint);
    }

    /// Captures the measurements that selection motion must not change.
    ///
    /// @param fixture the active fixture
    /// @return the current layout metrics
    private static LayoutMetrics captureLayout(MotionFixture fixture) {
        return new LayoutMetrics(
                fixture.button.getWidth(),
                fixture.button.getHeight(),
                fixture.button.prefWidth(-1.0),
                fixture.button.prefHeight(-1.0),
                fixture.trailing.getLayoutX()
        );
    }

    /// Verifies that selection motion preserved all measured layout values.
    ///
    /// @param expected the initial layout metrics
    /// @param actual   the current layout metrics
    private static void assertLayoutEquals(LayoutMetrics expected, LayoutMetrics actual) {
        assertEquals(expected.width, actual.width, 0.0001, "button width");
        assertEquals(expected.height, actual.height, 0.0001, "button height");
        assertEquals(expected.prefWidth, actual.prefWidth, 0.0001, "button preferred width");
        assertEquals(expected.prefHeight, actual.prefHeight, 0.0001, "button preferred height");
        assertEquals(expected.trailingLayoutX, actual.trailingLayoutX, 0.0001, "trailing sibling position");
    }

    /// Returns a motion scheme with observable fast effect and spatial transitions.
    ///
    /// @return the test-local motion scheme
    private static M3MotionScheme observableMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observable = M3MotionSpec.of(OBSERVABLE_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.builder(standard)
                .fastEffects(observable)
                .fastSpatial(observable)
                .build();
    }

    /// Holds the detached scene nodes participating in one motion test.
    ///
    /// @param root     the themed motion owner
    /// @param button   the tested toggle icon button
    /// @param icon     the direct M3FX icon graphic
    /// @param trailing the sibling used to detect layout movement
    private record MotionFixture(
            HBox root,
            M3IconToggleButton button,
            M3Icon icon,
            Region trailing
    ) {
    }

    /// Captures the button size and the position of its following sibling.
    ///
    /// @param width           the laid-out button width
    /// @param height          the laid-out button height
    /// @param prefWidth       the button's preferred width
    /// @param prefHeight      the button's preferred height
    /// @param trailingLayoutX the following sibling's horizontal position
    private record LayoutMetrics(
            double width,
            double height,
            double prefWidth,
            double prefHeight,
            double trailingLayoutX
    ) {
    }
}
