// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3RangeSlider;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.internal.M3FocusVisibleTracker;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that transient interaction ownership ends when a control can no longer receive its matching release.
@NotNullByDefault
final class M3InteractionLifecycleTest {
    /// Starts the JavaFX toolkit before interaction lifecycle tests create scenes and stages.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real stages opened by focus-transfer tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that a pending Space activation cannot survive focus transfer across interactive skin families.
    @Test
    void focusTransferCancelsPendingSpaceActivationAcrossInteractiveSkins() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            Region icon = new Region();
            icon.setPrefSize(18.0, 18.0);
            M3IconButton iconButton = new M3IconButton(icon);
            M3CheckBox checkBox = new M3CheckBox("Check box");
            M3Card card = new M3Card(new javafx.scene.control.Label("Card"));
            M3NavigationItem navigationItem = new M3NavigationItem("Navigation");
            M3ListItem listItem = new M3ListItem("List item");
            Pane focusSink = new Pane();
            focusSink.setFocusTraversable(true);
            focusSink.setPrefSize(24.0, 24.0);

            AtomicInteger buttonActions = new AtomicInteger();
            AtomicInteger iconButtonActions = new AtomicInteger();
            AtomicInteger checkBoxActions = new AtomicInteger();
            AtomicInteger cardActions = new AtomicInteger();
            AtomicInteger navigationActions = new AtomicInteger();
            AtomicInteger listItemActions = new AtomicInteger();
            button.setOnAction(event -> buttonActions.incrementAndGet());
            iconButton.setOnAction(event -> iconButtonActions.incrementAndGet());
            checkBox.setOnAction(event -> checkBoxActions.incrementAndGet());
            card.setOnAction(event -> cardActions.incrementAndGet());
            navigationItem.setOnAction(event -> navigationActions.incrementAndGet());
            listItem.setOnAction(event -> listItemActions.incrementAndGet());

            VBox root = new VBox(
                    button,
                    iconButton,
                    checkBox,
                    card,
                    navigationItem,
                    listItem,
                    focusSink
            );
            Stage stage = show(root, 360.0, 420.0);
            try {
                verifyFocusTransferCancelsSpace(button, focusSink, buttonActions);
                verifyFocusTransferCancelsSpace(iconButton, focusSink, iconButtonActions);
                verifyFocusTransferCancelsSpace(checkBox, focusSink, checkBoxActions);
                verifyFocusTransferCancelsSpace(card, focusSink, cardActions);
                verifyFocusTransferCancelsSpace(navigationItem, focusSink, navigationActions);
                verifyFocusTransferCancelsSpace(listItem, focusSink, listItemActions);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that cancelling keyboard ownership does not end a simultaneous pointer-owned press.
    @Test
    void focusTransferPreservesIndependentPointerOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            M3CheckBox checkBox = new M3CheckBox("Check box");
            Pane focusSink = new Pane();
            focusSink.setFocusTraversable(true);
            focusSink.setPrefSize(24.0, 24.0);

            AtomicInteger buttonActions = new AtomicInteger();
            AtomicInteger checkBoxActions = new AtomicInteger();
            button.setOnAction(event -> buttonActions.incrementAndGet());
            checkBox.setOnAction(event -> checkBoxActions.incrementAndGet());

            VBox root = new VBox(button, checkBox, focusSink);
            Stage stage = show(root, 320.0, 180.0);
            try {
                verifyFocusTransferPreservesPointer(button, focusSink, buttonActions);
                verifyFocusTransferPreservesPointer(checkBox, focusSink, checkBoxActions);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that scene detachment clears pointer ownership and slider direct-manipulation flags.
    @Test
    void sceneDetachClearsPointerAndSliderInteractionState() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
            M3RangeSlider rangeSlider = new M3RangeSlider(0.0, 100.0, 25.0, 75.0);
            button.setPrefWidth(160.0);
            slider.setPrefWidth(240.0);
            rangeSlider.setPrefWidth(240.0);
            VBox root = new VBox(button, slider, rangeSlider);
            Scene scene = new Scene(root, 320.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_PRESSED, 20.0, 20.0, true));
            slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_PRESSED, 100.0, 20.0, true));
            rangeSlider.fireEvent(primaryMouseEvent(
                    rangeSlider,
                    MouseEvent.MOUSE_PRESSED,
                    80.0,
                    20.0,
                    true
            ));
            assertTrue(button.isArmed());
            assertTrue(slider.isValueChanging());
            assertTrue(rangeSlider.isLowValueChanging() || rangeSlider.isHighValueChanging());

            root.getChildren().removeAll(button, slider, rangeSlider);

            assertFalse(button.isArmed());
            assertFalse(slider.isValueChanging());
            assertFalse(rangeSlider.isLowValueChanging());
            assertFalse(rangeSlider.isHighValueChanging());
            assertEquals(0.0, lookupRegion(button, ".m3-ripple").getOpacity(), 0.0001);
        });
    }

    /// Verifies that an unfinished switch drag cannot continue after the switch is detached and reattached.
    @Test
    void switchDragCannotSurviveSceneDetach() {
        FxTestUtils.runOnFxThread(() -> {
            M3Switch switchControl = new M3Switch("Switch");
            VBox root = new VBox(switchControl);
            Scene scene = new Scene(root, 240.0, 100.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();
            Region track = lookupRegion(switchControl, ".m3-switch-track");
            Region thumb = lookupRegion(switchControl, ".m3-switch-thumb");
            double restingThumbX = thumb.getLayoutX();

            track.fireEvent(primaryMouseEvent(
                    track,
                    MouseEvent.MOUSE_PRESSED,
                    track.getWidth() / 2.0,
                    track.getHeight() / 2.0,
                    true
            ));
            track.fireEvent(primaryMouseEvent(
                    track,
                    MouseEvent.MOUSE_DRAGGED,
                    track.getWidth(),
                    track.getHeight() / 2.0,
                    true
            ));
            assertTrue(thumb.getLayoutX() > restingThumbX);

            root.getChildren().remove(switchControl);
            assertEquals(restingThumbX, thumb.getLayoutX(), 0.0001);

            root.getChildren().add(switchControl);
            root.applyCss();
            root.layout();
            track.fireEvent(primaryMouseEvent(
                    track,
                    MouseEvent.MOUSE_DRAGGED,
                    track.getWidth(),
                    track.getHeight() / 2.0,
                    true
            ));

            assertEquals(restingThumbX, thumb.getLayoutX(), 0.0001);
        });
    }

    /// Verifies one focus-transfer cycle and one subsequent ordinary Space activation.
    private static void verifyFocusTransferCancelsSpace(
            Node control,
            Pane focusSink,
            AtomicInteger actionCount
    ) {
        control.requestFocus();
        assertTrue(control.isFocused(), control.getClass().getSimpleName() + " must accept focus");
        control.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
        control.fireEvent(spaceKeyEvent(KeyEvent.KEY_PRESSED));

        focusSink.requestFocus();
        assertTrue(focusSink.isFocused(), "the focus sink must receive transferred focus");
        assertFalse(control.isFocused());
        assertFalse(control.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS));
        control.fireEvent(spaceKeyEvent(KeyEvent.KEY_RELEASED));
        assertEquals(0, actionCount.get(), "a late release must not activate an unfocused control");

        control.requestFocus();
        control.fireEvent(spaceKeyEvent(KeyEvent.KEY_PRESSED));
        control.fireEvent(spaceKeyEvent(KeyEvent.KEY_RELEASED));
        assertEquals(1, actionCount.get(), "the next complete activation must remain usable");
    }

    /// Verifies one overlapping pointer and keyboard interaction.
    private static void verifyFocusTransferPreservesPointer(
            ButtonBase control,
            Pane focusSink,
            AtomicInteger actionCount
    ) {
        control.fireEvent(primaryMouseEvent(control, MouseEvent.MOUSE_PRESSED, 10.0, 10.0, true));
        assertTrue(control.isArmed(), "the pointer press must arm " + control.getClass().getSimpleName());
        control.fireEvent(spaceKeyEvent(KeyEvent.KEY_PRESSED));

        focusSink.requestFocus();
        assertTrue(focusSink.isFocused(), "the focus sink must receive transferred focus");
        assertTrue(control.isArmed(), "focus transfer must retain pointer-owned armed state");

        control.fireEvent(spaceKeyEvent(KeyEvent.KEY_RELEASED));
        assertTrue(control.isArmed(), "a cancelled keyboard release must not disarm the pointer gesture");
        assertEquals(0, actionCount.get());

        control.fireEvent(primaryMouseEvent(control, MouseEvent.MOUSE_RELEASED, 10.0, 10.0, false));
        assertFalse(control.isArmed());
        assertEquals(1, actionCount.get(), "the pointer release must complete its original activation");
    }

    /// Shows a themed scene and returns its stage.
    private static Stage show(Pane root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();
        return stage;
    }

    /// Creates a Space key event with no modifier keys.
    private static KeyEvent spaceKeyEvent(EventType<KeyEvent> eventType) {
        return new KeyEvent(eventType, "", "", KeyCode.SPACE, false, false, false, false);
    }

    /// Creates a primary-button mouse event at one node-local point.
    private static MouseEvent primaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                scenePoint.getX(),
                scenePoint.getY(),
                screenX,
                screenY,
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
                new PickResult(node, scenePoint.getX(), scenePoint.getY())
        );
    }

    /// Looks up a required region below a node.
    private static Region lookupRegion(Node owner, String selector) {
        return assertInstanceOf(Region.class, owner.lookup(selector), () -> "missing region " + selector);
    }
}
