// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.css.PseudoClass;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
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
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that transient interaction ownership ends when a control can no longer receive its matching release.
@NotNullByDefault
final class M3InteractionLifecycleTest {
    /// Stages owned by the current test, including stages hidden before cleanup.
    private final List<Stage> stages = new ArrayList<>();

    /// Starts the JavaFX toolkit before interaction lifecycle tests create scenes and stages.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Closes real stages opened by focus-transfer tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Stage stage : stages) {
                stage.close();
            }
            stages.clear();
        });
    }

    /// Releasing outside cancels activation without suppressing subsequent pointer hover.
    @ParameterizedTest
    @ValueSource(strings = {"ripple", "state-layer"})
    void buttonHoverRecoversAfterReleaseOutside(String feedback) {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Action");
            button.setAnimationsEnabled(false);
            button.setStyle("-m3-press-feedback: " + feedback + ";");
            Pane root = new Pane(button);
            new Scene(root, 320, 180);
            root.applyCss();
            button.resize(120, 40);
            button.layout();
            AtomicInteger actions = new AtomicInteger();
            button.setOnAction(event -> actions.incrementAndGet());
            Region overlay = lookupRegion(button, ".m3-state-layer");
            try {
                button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_ENTERED_TARGET, 20, 20, false));
                assertTrue(button.isHover());
                assertEquals(0.08, overlay.getOpacity(), 0.0001);
                button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_PRESSED, 20, 20, true));
                assertTrue(button.isArmed());
                button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_EXITED_TARGET, 160, 20, true));
                assertFalse(button.isHover());
                assertFalse(button.isArmed());
                button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_RELEASED, 160, 20, false));
                assertFalse(button.isPressed());
                assertEquals(0, actions.get());
                assertEquals(0.0, overlay.getOpacity(), 0.0001);
                button.fireEvent(primaryMouseEvent(button, MouseEvent.MOUSE_ENTERED_TARGET, 20, 20, false));
                assertTrue(button.isHover());
                assertFalse(button.isArmed());
                assertEquals(0.08, overlay.getOpacity(), 0.0001);
            } finally {
                button.setSkin(null);
            }
        });
    }

    /// Native pointer capture must release animated feedback even when the pointer leaves the button.
    @Tier3Test
    @ParameterizedTest
    @CsvSource({"ripple, 20", "ripple, 80", "state-layer, 20", "state-layer, 80"})
    void nativeButtonDragOutPreservesHover(String feedback, double pointerX) throws InterruptedException {
        M3Button button = FxTestUtils.callOnFxThread(() -> {
            M3Button control = new M3Button("Action");
            control.setStyle("-m3-press-feedback: " + feedback + ";");
            control.setFocusTraversable(false);
            control.setPrefSize(160, 48);
            Pane root = new Pane(control);
            control.relocate(40, 40);
            Stage stage = show(root, 360, 200);
            stage.setX(200);
            stage.setY(200);
            stage.toFront();
            return control;
        });
        Robot robot = FxTestUtils.callOnFxThread(Robot::new);
        Point2D originalPointer = FxTestUtils.callOnFxThread(robot::getMousePosition);
        AtomicInteger actions = new AtomicInteger();
        FxTestUtils.runOnFxThread(() -> button.setOnAction(event -> actions.incrementAndGet()));
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> button.getScene().getWindow().isFocused(), 2, () -> {}, () -> {});
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> !button.isHover(), 2,
                    () -> robot.mouseMove(Objects.requireNonNull(button.localToScreen(240, 80))), () -> {});
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> button.isHover() && !stateLayer(button).isOverlayOpacityAnimationRunning(), 2,
                    () -> robot.mouseMove(Objects.requireNonNull(button.localToScreen(pointerX, 24))),
                    () -> assertEquals(0.08, lookupRegion(button, ".m3-state-layer").getOpacity(), 0.0001));
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> button.isArmed() && !stateLayer(button).isRippleAnimationRunning(), 2,
                    () -> robot.mousePress(MouseButton.PRIMARY),
                    () -> assertTrue(button.isPressed()));
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> !button.isHover() && !button.isArmed(), 2,
                    () -> robot.mouseMove(Objects.requireNonNull(button.localToScreen(240, 20))),
                    () -> assertTrue(button.isPressed()));
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> !button.isPressed() && !stateLayer(button).isRippleAnimationRunning()
                            && !stateLayer(button).isOverlayOpacityAnimationRunning(), 2,
                    () -> robot.mouseRelease(MouseButton.PRIMARY),
                    () -> {
                        assertEquals(0, actions.get());
                        assertEquals(0.0, lookupRegion(button, ".m3-state-layer").getOpacity(), 0.0001);
                        @Nullable Node ripple = button.lookup(".m3-ripple");
                        if (ripple != null) {
                            assertEquals(0.0, ripple.getOpacity());
                        }
                    });
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> button.isHover() && !stateLayer(button).isOverlayOpacityAnimationRunning(), 2,
                    () -> robot.mouseMove(Objects.requireNonNull(button.localToScreen(pointerX, 24))),
                    () -> {
                        assertFalse(button.isArmed());
                        assertEquals(0.08, lookupRegion(button, ".m3-state-layer").getOpacity(), 0.0001);
                    });
        } catch (AssertionError failure) {
            String state = FxTestUtils.callOnFxThread(() -> {
                @Nullable Node ripple = button.lookup(".m3-ripple");
                return "hover=" + button.isHover()
                        + ", opacity=" + lookupRegion(button, ".m3-state-layer").getOpacity()
                        + ", animating=" + stateLayer(button).isOverlayOpacityAnimationRunning()
                        + ", ripple=" + (ripple == null ? "absent" : ripple.getOpacity())
                        + ", pointer=" + robot.getMousePosition() + ", target=" + button.localToScreen(pointerX, 24)
                        + ", focused=" + button.getScene().getWindow().isFocused();
            });
            throw new AssertionError(state, failure);
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                robot.mouseRelease(MouseButton.PRIMARY);
                robot.mouseMove(originalPointer);
                button.setSkin(null);
            });
        }
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

    /// Verifies that a completed selection animation cannot restore child CSS state after a list item is detached.
    @Tier2Test
    @Test
    void listItemDetachStopsSelectionAnimationAndRestoresSelectedStateOnReattach() throws InterruptedException {
        PseudoClass selectedPseudoClass = PseudoClass.getPseudoClass("selected");
        M3ListItem[] itemReference = new M3ListItem[1];
        Label[] headlineReference = new Label[1];
        VBox[] rootReference = new VBox[1];
        AtomicReference<@Nullable Label> referenceHeadline = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhenStable(
                () -> Objects.requireNonNull(referenceHeadline.get()).getPseudoClassStates().contains(selectedPseudoClass),
                2,
                () -> {
                    M3ListItem item = new M3ListItem("Selected item");
                    M3ListItem attachedItem = new M3ListItem("Attached item");
                    VBox root = new VBox(item, attachedItem);
                    show(root, 320.0, 120.0);
                    Label headline = assertInstanceOf(
                            Label.class,
                            item.lookup(".m3-list-item-headline")
                    );

                    item.setSelected(true);
                    attachedItem.setSelected(true);
                    root.applyCss();
                    root.layout();
                    root.getChildren().remove(item);

                    assertNull(item.getScene());
                    assertFalse(headline.getPseudoClassStates().contains(selectedPseudoClass));
                    itemReference[0] = item;
                    headlineReference[0] = headline;
                    rootReference[0] = root;
                    referenceHeadline.set(assertInstanceOf(Label.class, attachedItem.lookup(".m3-list-item-headline")));
                },
                () -> {
                    M3ListItem item = itemReference[0];
                    Label headline = headlineReference[0];
                    assertNull(item.getScene());
                    assertFalse(headline.getPseudoClassStates().contains(selectedPseudoClass));
                }
        );

        FxTestUtils.runOnFxThreadWhenStable(
                () -> headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass),
                2,
                () -> {
                    VBox root = rootReference[0];
                    root.getChildren().add(itemReference[0]);
                    root.applyCss();
                    root.layout();
                },
                () -> assertTrue(
                        headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass)
                )
        );
    }

    /// Verifies that hidden windows suspend selected child CSS state until the item is shown again.
    @Tier2Test
    @Test
    void listItemWindowHideSuspendsSelectedChildCssStateUntilWindowShownAgain() throws InterruptedException {
        PseudoClass selectedPseudoClass = PseudoClass.getPseudoClass("selected");
        M3ListItem[] itemReference = new M3ListItem[1];
        Label[] headlineReference = new Label[1];
        Stage[] stageReference = new Stage[1];

        FxTestUtils.runOnFxThreadWhenStable(
                () -> headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass),
                2,
                () -> {
                    M3ListItem item = new M3ListItem("Selected item");
                    item.setSelected(true);
                    Stage stage = show(new VBox(item), 320.0, 120.0);
                    itemReference[0] = item;
                    headlineReference[0] = assertInstanceOf(
                            Label.class,
                            item.lookup(".m3-list-item-headline")
                    );
                    stageReference[0] = stage;
                },
                () -> assertTrue(
                        headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass)
                )
        );

        FxTestUtils.runOnFxThreadWhenStable(
                () -> !headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass),
                2,
                () -> stageReference[0].close(),
                () -> {
                    assertFalse(stageReference[0].isShowing());
                    assertNotNull(itemReference[0].getScene());
                    assertFalse(headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass));
                }
        );

        FxTestUtils.runOnFxThreadWhenStable(
                () -> headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass),
                2,
                () -> stageReference[0].show(),
                () -> assertTrue(
                        headlineReference[0].getPseudoClassStates().contains(selectedPseudoClass)
                )
        );
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
    private Stage show(Pane root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        Stage stage = new Stage();
        stages.add(stage);
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
        @Nullable Point2D screenPoint = node.localToScreen(x, y);
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

    /// Returns the shared press-feedback layer of a skinned button.
    private static M3StateLayer stateLayer(M3Button button) {
        return assertInstanceOf(M3StateLayer.class, button.lookup(".m3-state-layer-container"));
    }

    /// Looks up a required region below a node.
    private static Region lookupRegion(Node owner, String selector) {
        return assertInstanceOf(Region.class, owner.lookup(selector), () -> "missing region " + selector);
    }
}
