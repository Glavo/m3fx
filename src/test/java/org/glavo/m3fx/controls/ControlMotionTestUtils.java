// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Provides bounded motion observation and window cleanup for control tests.
@NotNullByDefault
final class ControlMotionTestUtils {
    /// Prevents instantiation of this utility class.
    private ControlMotionTestUtils() {
    }

    /// The duration used to keep real-pulse animation states observable in tests.
    static final Duration OBSERVABLE_TEST_MOTION_DURATION = Duration.millis(600.0);

    /// Creates motion behavior that only overrides type-ahead and submenu hover timings from the standard profile.
    static M3MotionBehavior interactionTimingBehavior(
            Duration typeAheadResetDelay,
            Duration subMenuHoverOpenDelay,
            Duration subMenuHoverCloseDelay
    ) {
        return M3MotionBehavior.builder()
                .subMenuHoverOpenDelay(Objects.requireNonNull(subMenuHoverOpenDelay, "subMenuHoverOpenDelay"))
                .typeAheadResetDelay(Objects.requireNonNull(typeAheadResetDelay, "typeAheadResetDelay"))
                .subMenuHoverCloseDelay(Objects.requireNonNull(subMenuHoverCloseDelay, "subMenuHoverCloseDelay"))
                .build();
    }

    /// Returns whether a referenced tooltip is currently showing.
    static boolean tooltipShowing(AtomicReference<? extends @Nullable M3Tooltip> tooltipReference) {
        @Nullable M3Tooltip tooltip = tooltipReference.get();
        return tooltip != null && tooltip.isShowing();
    }

    /// Closes a real-window motion behavior test scene and clears local motion overrides.
    static void closeMotionTestScene(
            AtomicReference<? extends @Nullable Stage> stageReference,
            AtomicReference<? extends @Nullable Node> rootReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable Node root = rootReference.get();
            if (root != null) {
                FxTestUtils.clearMotionBehavior(root);
                FxTestUtils.setCssAnimationsEnabled(root, true);
            }

            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Returns whether a referenced tooltip is showing while the requested owner keeps keyboard focus.
    static boolean tooltipShowingWithFocus(
            AtomicReference<? extends @Nullable M3Tooltip> tooltipReference,
            AtomicReference<? extends @Nullable Node> focusOwnerReference
    ) {
        @Nullable Node focusOwner = focusOwnerReference.get();
        return focusOwner != null && focusOwner.isFocused() && tooltipShowing(tooltipReference);
    }

    /// Returns a local motion scheme with observable pulse-driven animation frames.
    static M3MotionScheme observableTestMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec =
                M3MotionSpec.of(OBSERVABLE_TEST_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.builder(standard)
                .fastEffects(observableSpec)
                .defaultEffects(observableSpec)
                .fastSpatial(observableSpec)
                .defaultSpatial(observableSpec)
                .build();
    }

    /// Returns whether a FAB menu action is rendering an intermediate entrance transition.
    static boolean fabActionEnterNodeIsTransitioning(Node node) {
        return node.isVisible()
                && node.isManaged()
                && isBetweenExclusive(node.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(node.getScaleX(), 0.86, 1.0)
                && isBetweenExclusive(node.getScaleY(), 0.86, 1.0)
                && hasVisibleTranslation(node);
    }

    /// Describes a node's render-affecting state for timeout diagnostics.
    static String describeNodeState(@Nullable Node node) {
        if (node == null) {
            return "<null>";
        }
        return "visible=" + node.isVisible()
                + ", managed=" + node.isManaged()
                + ", opacity=" + node.getOpacity()
                + ", scaleX=" + node.getScaleX()
                + ", scaleY=" + node.getScaleY()
                + ", translateX=" + node.getTranslateX()
                + ", translateY=" + node.getTranslateY()
                + ", bounds=" + node.getBoundsInParent()
                + ", scene=" + node.getScene();
    }

    /// Verifies that a spatial entrance node is rendering an intermediate transition.
    static void assertSpatialEnterNodeIntermediate(Node node, String description) {
        assertTrue(node.isVisible(), () -> description + " should be visible");
        assertTrue(node.isManaged(), () -> description + " should be managed");
        assertBetween(node.getOpacity(), 0.0, 1.0, description + " opacity");
        assertTrue(hasVisibleTranslation(node), () -> description
                + " translateX=" + node.getTranslateX()
                + ", translateY=" + node.getTranslateY());
    }

    /// Verifies that a FAB menu action is rendering an intermediate entrance transition.
    static void assertFabActionEnterNodeIntermediate(Node node) {
        assertSpatialEnterNodeIntermediate(node, "FAB menu action");
        assertBetween(node.getScaleX(), 0.86, 1.0, "FAB menu action scaleX");
        assertBetween(node.getScaleY(), 0.86, 1.0, "FAB menu action scaleY");
    }

    /// Returns whether a node has a non-identity transition translation.
    static boolean hasVisibleTranslation(Node node) {
        return Math.abs(node.getTranslateX()) > 0.0001 || Math.abs(node.getTranslateY()) > 0.0001;
    }

    /// Closes an observable motion test window and clears local motion overrides from its root.
    static void closeObservableMotionWindow(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Parent> rootReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable Parent root = rootReference.get();
            if (root != null) {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                FxTestUtils.clearMotionScheme(root);
            }

            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Applies CSS and layout to an observable root, or reports that it is not yet available.
    static boolean observableRootUnavailable(AtomicReference<@Nullable Parent> rootReference) {
        @Nullable Parent root = rootReference.get();
        if (root == null) {
            return true;
        }

        root.applyCss();
        root.layout();
        return false;
    }

    /// Returns the popup surface that hosts a picker field popup.
    static @Nullable Parent pickerPopupSurface(M3PickerField<?, ?> field) {
        Node parent = field.getPicker().getParent();
        return parent instanceof Parent popupSurface ? popupSurface : null;
    }

    /// Returns the popup surface that hosts a date range picker field popup.
    static @Nullable Parent pickerPopupSurface(M3DateRangePickerField field) {
        Node parent = field.getPicker().getParent();
        return parent instanceof Parent popupSurface ? popupSurface : null;
    }

    /// Returns whether a popup surface has reached its final identity transform.
    static boolean popupSurfaceIsSettled(Node surface) {
        surface.applyCss();
        if (surface instanceof Parent parent) {
            parent.layout();
        }

        return surface.getScene() != null
                && Math.abs(surface.getOpacity() - 1.0) < 0.0001
                && Math.abs(surface.getScaleX() - 1.0) < 0.0001
                && Math.abs(surface.getScaleY() - 1.0) < 0.0001
                && Math.abs(surface.getTranslateX()) < 0.0001
                && Math.abs(surface.getTranslateY()) < 0.0001;
    }

    /// Verifies that a popup surface has settled to its final identity transform.
    static void assertPopupSurfaceSettled(Node surface, String description) {
        surface.applyCss();
        if (surface instanceof Parent parent) {
            parent.layout();
        }

        assertEquals(1.0, surface.getOpacity(), 0.0001, description + " opacity");
        assertEquals(1.0, surface.getScaleX(), 0.0001, description + " scaleX");
        assertEquals(1.0, surface.getScaleY(), 0.0001, description + " scaleY");
        assertEquals(0.0, surface.getTranslateX(), 0.0001, description + " translateX");
        assertEquals(0.0, surface.getTranslateY(), 0.0001, description + " translateY");
    }

    /// Verifies a button's pressed scale through a real JavaFX window and pulse-driven animation.
    static void assertPressedScaleAfterRealPulse(
            Supplier<? extends ButtonBase> buttonFactory,
            double width,
            double height,
            double expectedScale,
            String description
    ) throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable ButtonBase> buttonReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> buttonScaleMatches(buttonReference, expectedScale),
                    2,
                    () -> {
                        ButtonBase button = buttonFactory.get();
                        showObservablePressedButton(
                                stageReference,
                                rootReference,
                                buttonReference,
                                button,
                                width,
                                height
                        );
                        button.fireEvent(primaryMouseEvent(
                                MouseEvent.MOUSE_PRESSED,
                                width / 2.0,
                                height / 2.0,
                                true
                        ));
                    },
                    () -> {
                        ButtonBase button = Objects.requireNonNull(buttonReference.get(), "button");

                        assertTrue(button.isArmed(), description + " should remain armed while pressed");
                        assertEquals(expectedScale, button.getScaleX(), 0.0001, description + " scaleX");
                        assertEquals(expectedScale, button.getScaleY(), 0.0001, description + " scaleY");
                    }
            );
        } finally {
            closeObservablePressedButton(stageReference, rootReference, buttonReference);
        }
    }

    /// Shows a button configured for observable pressed-state animation frames.
    static void showObservablePressedButton(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Pane> rootReference,
            AtomicReference<@Nullable ButtonBase> buttonReference,
            ButtonBase button,
            double width,
            double height
    ) {
        Pane root = new Pane(button);
        Scene scene = new Scene(root, Math.max(200.0, width + 80.0), Math.max(100.0, height + 80.0));
        Stage stage = new Stage();

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(root, true);
        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        button.resizeRelocate(20.0, 20.0, width, height);
        root.layout();
        button.layout();

        stageReference.set(stage);
        rootReference.set(root);
        buttonReference.set(button);
    }

    /// Closes a pressed-scale test window and clears local motion overrides.
    static void closeObservablePressedButton(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Pane> rootReference,
            AtomicReference<@Nullable ButtonBase> buttonReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable ButtonBase button = buttonReference.get();
            if (button != null && button.isArmed()) {
                button.fireEvent(primaryMouseEvent(
                        MouseEvent.MOUSE_RELEASED,
                        button.getWidth() / 2.0,
                        button.getHeight() / 2.0,
                        false
                ));
            }

            @Nullable Pane root = rootReference.get();
            if (root != null) {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                FxTestUtils.clearMotionScheme(root);
            }

            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Returns whether the tested button scale matches the expected pressed scale.
    static boolean buttonScaleMatches(
            AtomicReference<@Nullable ButtonBase> buttonReference,
            double expectedScale
    ) {
        @Nullable ButtonBase button = buttonReference.get();
        if (button == null) {
            return false;
        }

        button.applyCss();
        @Nullable Parent parent = button.getParent();
        if (parent != null) {
            parent.layout();
        }
        button.layout();

        return button.isArmed()
                && Math.abs(button.getScaleX() - expectedScale) <= 0.0001
                && Math.abs(button.getScaleY() - expectedScale) <= 0.0001;
    }

    /// Returns whether a value is strictly inside the supplied range.
    static boolean isBetweenExclusive(double value, double lowerBound, double upperBound) {
        return value > lowerBound && value < upperBound;
    }

    /// Verifies that a value is between two exclusive bounds.
    static void assertBetween(double actual, double lowerBound, double upperBound, String description) {
        assertTrue(actual > lowerBound && actual < upperBound,
                () -> description + ": actual=" + actual
                        + ", lowerBound=" + lowerBound
                        + ", upperBound=" + upperBound);
    }
}
