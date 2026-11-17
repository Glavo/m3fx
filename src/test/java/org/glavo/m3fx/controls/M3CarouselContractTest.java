// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.skins.M3CarouselSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies carousel strategies, item layout, and interaction.
@NotNullByDefault
final class M3CarouselContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that carousels manage items, selected state, and item click selection.
    @Test
    void carouselManagesItemsSelectionAndItemClicks() {
        Label first = new Label("First");
        Label second = new Label("Second");
        Label third = new Label("Third");
        third.setDisable(true);
        M3Carousel carousel = carousel(first, second, third);

        assertFalse(carousel.isFocusTraversable());
        assertTrue(first.isFocusTraversable());
        assertTrue(first.getStyleClass().contains("m3-carousel-item"));
        assertEquals(-1, carousel.getSelectedIndex());
        assertNull(carousel.getSelectedItem());

        carousel.selectIndex(1);
        carousel.setWrapAround(true);

        assertSame(second, carousel.getSelectedItem());
        assertEquals(1, carousel.getSelectedIndex());
        assertEquals(carousel.getSelectedItems(), carousel.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
        assertTrue(second.getStyleClass().contains("m3-carousel-selected-item"));

        carousel.selectNext();

        assertSame(first, carousel.getSelectedItem());
        assertFalse(second.getStyleClass().contains("m3-carousel-selected-item"));
        assertTrue(first.getStyleClass().contains("m3-carousel-selected-item"));

        second.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_CLICKED, 4.0, 4.0, false));

        assertSame(second, carousel.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> carousel.select(new Label("Detached")));

        carousel.getItems().remove(second);

        assertFalse(second.isFocusTraversable());
        assertFalse(second.getStyleClass().contains("m3-carousel-item"));
        assertFalse(second.getStyleClass().contains("m3-carousel-selected-item"));
        assertSame(first, carousel.getSelectedItem());

        carousel.getItems().clear();

        assertEquals(-1, carousel.getSelectedIndex());
        assertNull(carousel.getSelectedItem());
    }

    /// Verifies that carousel selection rejects unreachable item nodes and tracks runtime reachability changes.
    @Test
    void carouselSelectionRejectsAndTracksUnreachableItems() {
        Label first = new Label("First");
        Label hidden = new Label("Hidden");
        Label disabled = new Label("Disabled");
        Label last = new Label("Last");
        hidden.setVisible(false);
        disabled.setDisable(true);
        M3Carousel carousel = carousel(first, hidden, disabled, last);

        assertThrows(IllegalArgumentException.class, () -> carousel.select(hidden));
        assertThrows(IllegalArgumentException.class, () -> carousel.select(disabled));
        assertThrows(IllegalArgumentException.class, () -> carousel.selectIndex(1));
        assertThrows(IllegalArgumentException.class, () -> carousel.selectIndex(2));

        carousel.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, hidden);

        assertNull(carousel.getSelectedItem());
        assertEquals(-1, carousel.getSelectedIndex());

        carousel.select(last);

        assertSame(last, carousel.getSelectedItem());
        assertEquals(3, carousel.getSelectedIndex());

        last.setVisible(false);

        assertSame(first, carousel.getSelectedItem());
        assertEquals(0, carousel.getSelectedIndex());
        assertFalse(last.getStyleClass().contains("m3-carousel-selected-item"));

        first.setDisable(true);

        assertNull(carousel.getSelectedItem());
        assertEquals(-1, carousel.getSelectedIndex());
        assertTrue(carousel.getSelectedItems().isEmpty());

        hidden.setVisible(true);
        carousel.setSelectedIndex(1);

        assertSame(hidden, carousel.getSelectedItem());
        assertEquals(1, carousel.getSelectedIndex());
    }

    /// Verifies that carousels support keyboard navigation and accessibility selection.
    @Test
    void carouselHandlesKeyboardAndAccessibilitySelection() {
        Label first = new Label("First");
        Label second = new Label("Second");
        Label third = new Label("Third");
        M3Carousel carousel = carousel(first, second, third);

        carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertSame(first, carousel.getSelectedItem());

        carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertSame(second, carousel.getSelectedItem());
        assertEquals(false, carousel.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(third, carousel.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));

        carousel.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, third);

        assertSame(third, carousel.getSelectedItem());

        carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);

        assertSame(first, carousel.getSelectedItem());

        carousel.setWrapAround(false);
        carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

        assertSame(first, carousel.getSelectedItem());
    }

    /// Verifies that carousel navigation keys do not steal focus from embedded text inputs.
    @Test
    void carouselNavigationDoesNotStealEmbeddedTextInputFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField editor = createTextField("Editable", M3TextInputVariant.OUTLINED);
            editor.setPrefWidth(180.0);
            M3Button next = new M3Button("Next");
            M3Carousel carousel = carousel(editor, next);
            M3TextField outsideEditor = createTextField("Outside", M3TextInputVariant.OUTLINED);
            VBox root = new VBox(12.0, carousel, outsideEditor);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 380.0, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                carousel.selectIndex(0);

                assertContainerNavigationDoesNotStealTextInputFocus(root, carousel, editor, next, KeyCode.RIGHT);

                assertSame(editor, carousel.getSelectedItem());

                outsideEditor.requestFocus();
                assertTrue(outsideEditor.isFocused());
                carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertSame(next, carousel.getSelectedItem());
                assertTrue(next.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that carousel accessibility reveal requests can target focusable descendants inside items.
    @Test
    void carouselAccessibleShowItemFocusesRequestedDescendant() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstPrimary = new M3Button("First primary");
            M3Button firstSecondary = new M3Button("First secondary");
            M3Button secondPrimary = new M3Button("Second primary");
            M3Button secondSecondary = new M3Button("Second secondary");
            HBox firstItem = new HBox(firstPrimary, firstSecondary);
            HBox secondItem = new HBox(secondPrimary, secondSecondary);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Pane root = new Pane(carousel);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 140.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                carousel.resizeRelocate(0.0, 0.0, 500.0, 120.0);
                root.applyCss();
                root.layout();

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, secondSecondary);

                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(secondSecondary.isFocused());
                assertSame(secondSecondary, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                carousel.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, firstSecondary);

                assertSame(firstItem, carousel.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that carousel reveal requests can open installed rich tooltip action targets inside item content.
    @Test
    void carouselAccessibleShowItemRevealsNestedTooltipTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstAction = new M3Button("First action");
            HBox firstItem = new HBox(firstAction);
            M3Button ownerAction = new M3Button("Settings action");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Settings",
                    "Shows carousel item details.",
                    tooltipAction
            );
            HBox secondItem = new HBox(ownerAction);
            M3Button hiddenOwnerAction = new M3Button("Hidden settings action");
            hiddenOwnerAction.setVisible(false);
            M3Button hiddenTooltipAction = new M3Button("Hidden details");
            M3RichTooltip hiddenTooltip = installRichTooltip(
                    hiddenOwnerAction,
                    "Hidden settings",
                    "This action cannot be revealed.",
                    hiddenTooltipAction
            );
            HBox thirdItem = new HBox(hiddenOwnerAction);
            M3Carousel carousel = carousel(firstItem, secondItem, thirdItem);
            carousel.setAnimatedScroll(false);
            Pane root = new Pane(carousel);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 160.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                carousel.resizeRelocate(0.0, 0.0, 500.0, 120.0);
                root.applyCss();
                root.layout();

                assertFalse(tooltip.isShowing());
                assertFalse(hiddenTooltip.isShowing());

                carousel.select(firstItem);
                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenTooltipAction);
                root.layout();

                assertSame(firstItem, carousel.getSelectedItem());
                assertFalse(hiddenTooltip.isShowing());
                assertFalse(hiddenTooltipAction.isFocused());

                carousel.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, hiddenTooltipAction);

                assertNull(carousel.getSelectedItem());
                assertEquals(-1, carousel.getSelectedIndex());

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                carousel.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertSame(secondItem, carousel.getSelectedItem());
                assertSame(tooltipAction, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                M3Tooltip.uninstall(ownerAction, tooltip);
                M3Tooltip.uninstall(hiddenOwnerAction, hiddenTooltip);
                stage.close();
            }
        });
    }

    /// Verifies that carousel reveal requests can open nested submenu targets inside item content.
    @Test
    void carouselAccessibleShowItemRevealsNestedMenuTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstAction = new M3Button("First action");
            HBox firstItem = new HBox(firstAction);
            M3MenuItem archiveItem = new M3MenuItem("Archive item");
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", archiveItem);
            M3MenuButton menuButton = new M3MenuButton("Actions", subMenuItem);
            HBox secondItem = new HBox(menuButton);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Pane root = new Pane(carousel);
            Stage stage = new Stage();
            try {
                menuButton.setAnimationsEnabled(false);
                subMenuItem.setAnimationsEnabled(false);

                stage.setScene(new Scene(root, 520.0, 180.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                carousel.resizeRelocate(0.0, 0.0, 500.0, 120.0);
                root.applyCss();
                root.layout();

                assertFalse(menuButton.isShowing());
                assertFalse(subMenuItem.isSubMenuShowing());

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);
                root.layout();

                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(menuButton.isShowing());
                assertTrue(subMenuItem.isSubMenuShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                carousel.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertSame(secondItem, carousel.getSelectedItem());
                assertSame(archiveItem, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                subMenuItem.hideSubMenu();
                menuButton.hideMenu();
                menuButton.setAnimationsEnabled(true);
                subMenuItem.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that carousel reveal requests can open nested picker value targets inside item content wrappers.
    @Test
    void carouselAccessibleShowItemRevealsNestedPickerValueTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstAction = new M3Button("First action");
            HBox firstItem = new HBox(firstAction);
            LocalDate targetDate = LocalDate.of(2026, 10, 12);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 10, 8));
            HBox secondItem = new HBox(field);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Pane root = new Pane(carousel);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 560.0, 220.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                carousel.resizeRelocate(0.0, 0.0, 520.0, 140.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                carousel.select(firstItem);
                root.applyCss();
                root.layout();

                assertSame(firstItem, carousel.getSelectedItem());
                assertFalse(field.isShowing());

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);
                root.layout();

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertSame(pickerFocusNode, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that carousel reveal requests can open nested time picker value targets inside item content wrappers.
    @Test
    void carouselAccessibleShowItemRevealsNestedTimePickerValueTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstAction = new M3Button("First action");
            HBox firstItem = new HBox(firstAction);
            LocalTime targetTime = LocalTime.of(14, 30);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(13, 15));
            HBox secondItem = new HBox(field);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Pane root = new Pane(carousel);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 560.0, 220.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                carousel.resizeRelocate(0.0, 0.0, 520.0, 140.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                carousel.select(firstItem);
                root.applyCss();
                root.layout();

                assertSame(firstItem, carousel.getSelectedItem());
                assertFalse(field.isShowing());

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetTime);
                root.layout();

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertSame(pickerFocusNode, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that carousel skins create an internal viewport and reveal selected items.
    @Test
    void carouselCreatesMaterialSkinAndScrollsSelectedItemIntoView() {
        FxTestUtils.runOnFxThread(() -> {
            M3Carousel carousel = carousel(
                    carouselTestItem("A"),
                    carouselTestItem("B"),
                    carouselTestItem("C"),
                    carouselTestItem("D"),
                    carouselTestItem("E")
            );
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(260.0, 100.0);
            Pane root = new Pane(carousel);
            Scene scene = new Scene(root, 280.0, 120.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                carousel.resizeRelocate(0.0, 0.0, 260.0, 100.0);
                root.layout();

                assertInstanceOf(M3CarouselSkin.class, carousel.getSkin());
                carousel.clearSelection();
                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertFalse(carousel.isFocused());
                assertEquals(0, carousel.getSelectedIndex());
                assertTrue(carousel.getItems().get(0).isFocused());

                carousel.getItems().get(0).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB));

                assertEquals(1, carousel.getSelectedIndex());
                assertTrue(carousel.getItems().get(1).isFocused());

                carousel.getItems().get(1).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB, true));

                assertEquals(0, carousel.getSelectedIndex());
                assertTrue(carousel.getItems().get(0).isFocused());

                carousel.selectIndex(4);
                root.layout();

                ScrollPane viewport = assertInstanceOf(
                        ScrollPane.class,
                        carousel.lookup("." + "m3-carousel-viewport")
                );
                assertTrue(viewport.getStyleClass().contains("m3-scroll-pane"));
                assertTrue(viewport.getHvalue() > 0.5, () -> "hvalue=" + viewport.getHvalue());
                assertTrue(M3ScrollPane.isSmoothScrollingEnabled(viewport));

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertFalse(carousel.isFocused());
                assertTrue(Objects.requireNonNull(carousel.getSelectedItem(), "selected carousel item").isFocused());

                carousel.setAnimationsEnabled(false);
                viewport.setHvalue(0.0);
                ScrollEvent event = scrollEvent(viewport, 0.0, -80.0);
                viewport.fireEvent(event);
                root.layout();

                assertTrue(event.isConsumed());
                assertTrue(viewport.getHvalue() > 0.0, () -> "hvalue=" + viewport.getHvalue()
                        + ", contentBounds=" + viewport.getContent().getLayoutBounds()
                        + ", contentPrefWidth=" + assertInstanceOf(Region.class, viewport.getContent()).prefWidth(-1.0)
                        + ", viewport=" + viewport.getViewportBounds());

                carousel.setAnimationsEnabled(true);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that carousel viewports keep wheel ownership inside outer smooth scroll panes.
    @Test
    void carouselViewportKeepsWheelEventsInsideOuterSmoothScrollPane() {
        FxTestUtils.assertNoM3CssTokenWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3Carousel carousel = carousel(
                    carouselTestItem("A"),
                    carouselTestItem("B"),
                    carouselTestItem("C"),
                    carouselTestItem("D"),
                    carouselTestItem("E")
            );
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(260.0, 100.0);

            Region filler = new Region();
            filler.setPrefSize(260.0, 360.0);
            VBox content = new VBox(carousel, filler);
            ScrollPane outerScrollPane = new ScrollPane(content);
            outerScrollPane.setPrefSize(300.0, 180.0);
            M3ScrollPane.style(outerScrollPane);
            StackPane root = new StackPane(outerScrollPane);
            Scene scene = new Scene(root, 340.0, 240.0);
            Stage stage = new Stage();
            @Nullable ScrollPane viewport = null;

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertInstanceOf(M3CarouselSkin.class, carousel.getSkin());
                viewport = assertInstanceOf(
                        ScrollPane.class,
                        carousel.lookup("." + "m3-carousel-viewport")
                );
                assertTrue(viewport.getStyleClass().contains("m3-scroll-pane"));
                assertTrue(M3ScrollPane.isSmoothScrollingEnabled(viewport));
                assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, viewport));
                assertFalse(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, viewport.getContent()));
                assertTrue(M3ScrollPane.isEventTargetForScrollPane(outerScrollPane, filler));

                carousel.selectIndex(4);
                root.layout();
                ScrollPane currentViewport = viewport;
                assertTrue(currentViewport.getHvalue() > 0.5, () -> "hvalue=" + currentViewport.getHvalue());
                M3ScrollPane.enableSmoothScrolling(outerScrollPane);
                carousel.setAnimationsEnabled(false);
                viewport.setHvalue(0.0);

                ScrollEvent event = scrollEvent(viewport, 0.0, -80.0);
                viewport.fireEvent(event);

                assertTrue(currentViewport.getHvalue() > 0.0, () -> "hvalue=" + currentViewport.getHvalue());
                assertEquals(0.0, outerScrollPane.getVvalue(), 0.0001);
            } finally {
                carousel.setAnimationsEnabled(true);
                M3ScrollPane.disableSmoothScrolling(outerScrollPane);
                stage.close();
            }
        }));
    }

    /// Verifies that running carousel scroll transitions settle when animations are disabled at runtime.
    @Tier2Test
    @Test
    void carouselSettlesRunningScrollAnimationWhenAnimationsAreDisabled() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Carousel> carouselReference = new AtomicReference<>();
        AtomicReference<@Nullable ScrollPane> viewportReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> carouselViewportScrollIsInProgress(viewportReference),
                    () -> describeCarouselViewportScrollState(
                            viewportReference,
                            "Carousel viewport never reached an in-progress scroll frame"
                    ),
                    () -> {
                        M3Carousel carousel = carousel(
                                carouselTestItem("A"),
                                carouselTestItem("B"),
                                carouselTestItem("C"),
                                carouselTestItem("D"),
                                carouselTestItem("E")
                        );
                        carousel.setAnimatedScroll(true);
                        carousel.setPrefSize(260.0, 100.0);
                        Pane root = new Pane(carousel);
                        Scene scene = new Scene(root, 280.0, 120.0);
                        Stage stage = new Stage();

                        carousel.setAnimationsEnabled(true);
                        FxTestUtils.setMotionScheme(carousel, observableTestMotionScheme());
                        stageReference.set(stage);
                        carouselReference.set(carousel);

                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        carousel.resizeRelocate(0.0, 0.0, 260.0, 100.0);
                        root.layout();

                        assertInstanceOf(M3CarouselSkin.class, carousel.getSkin());
                        ScrollPane viewport = assertInstanceOf(
                                ScrollPane.class,
                                carousel.lookup("." + "m3-carousel-viewport")
                        );
                        viewportReference.set(viewport);

                        carousel.selectIndex(4);
                        root.layout();
                    },
                    () -> {
                        M3Carousel carousel = Objects.requireNonNull(carouselReference.get(), "carousel");
                        ScrollPane viewport = Objects.requireNonNull(viewportReference.get(), "viewport");

                        assertBetween(viewport.getHvalue(), 0.0, 1.0, "carousel animated hvalue");
                        carousel.setAnimationsEnabled(false);
                        assertEquals(1.0, viewport.getHvalue(), 0.0001);
                    }
            );
        } finally {
            closeCarouselScrollTestWindow(stageReference, carouselReference);
        }
    }

    /// Verifies Carousel item geometry and interaction states in one rendered matrix.
    @Tier2Test
    @Test
    void carouselSnapshotRendersItemStatesAndFocusIndicator() {
        FxTestUtils.runOnFxThread(() -> {
            StackPane normal = carouselTestItem("Normal");
            StackPane hovered = carouselTestItem("Hovered");
            StackPane focused = carouselTestItem("Focused");
            StackPane pressed = carouselTestItem("Pressed");
            StackPane disabled = carouselTestItem("Disabled");
            for (StackPane item : List.of(normal, hovered, focused, pressed, disabled)) {
                item.setFocusTraversable(true);
            }
            disabled.setDisable(true);

            M3Carousel carousel = carousel(normal, hovered, focused, pressed, disabled);
            carousel.setCarouselLayout(M3CarouselLayout.UNCONTAINED);
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(732.0, 104.0);
            carousel.selectIndex(2);

            FlowPane root = new FlowPane(carousel);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 792.0, 160.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            root.applyCss();
            root.resize(792.0, 160.0);
            root.layout();

            applyPseudoState(hovered, "hover");
            applyPseudoState(focused, "focus-visible");
            applyPseudoState(pressed, "pressed");
            root.applyCss();
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);

            assertSnapshotNodeContainsContrast(image, carousel, Color.WHITE, 0.04);
            assertTrue(focused.getStyleClass().contains("m3-carousel-selected-item"));
            assertEquals(0.0, carouselItemStateLayer(normal).getOpacity(), 0.0001);
            assertTrue(carouselItemStateLayer(hovered).getOpacity() > 0.0);
            assertTrue(carouselItemStateLayer(pressed).getOpacity()
                    > carouselItemStateLayer(hovered).getOpacity());
            Region focusIndicator = lookupRegion(carouselItemSlot(focused), ".m3-focus-indicator");
            assertTrue(focusIndicator.isVisible());
            assertEquals(1.0, focusIndicator.getOpacity(), 0.0001);
            assertEquals(0.38, carouselItemSlot(disabled).getOpacity(), 0.0001);
            assertSnapshotNodeContainsContrast(image, focused, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-carousel-states.png"
            ));
            FxTestUtils.setCssAnimationsEnabled(root, true);

            normal.fireEvent(primaryMouseEvent(normal, MouseEvent.MOUSE_PRESSED, 70.0, 36.0, true));
            Region ripple = lookupRegion(carouselItemSlot(normal), ".m3-ripple");
            assertTrue(ripple.getOpacity() > 0.0, "Carousel press should start a visible bounded ripple");
            normal.fireEvent(primaryMouseEvent(normal, MouseEvent.MOUSE_RELEASED, 70.0, 36.0, false));
            assertTrue(ripple.getOpacity() > 0.0,
                    "Carousel release should retain the ripple while its fade transition starts");
        });
    }

    /// Describes the current carousel viewport scroll position for timeout diagnostics.
    private static String describeCarouselViewportScrollState(
            AtomicReference<@Nullable ScrollPane> viewportReference,
            String description
    ) {
        @Nullable ScrollPane viewport = viewportReference.get();
        return description
                + ": hvalue=" + (viewport == null ? "<null>" : viewport.getHvalue())
                + ", viewport=" + describeNodeState(viewport);
    }

    /// Returns the rendered Carousel keyline slot that hosts one public item.
    private static Region carouselItemSlot(Node item) {
        @Nullable Parent ancestor = item.getParent();
        while (ancestor != null) {
            if (ancestor.getStyleClass().contains("m3-carousel-item-container")) {
                return assertInstanceOf(Region.class, ancestor);
            }
            ancestor = ancestor.getParent();
        }
        throw new AssertionError("carousel item has no rendered keyline slot: " + item);
    }

    /// Returns the bounded state-layer overlay for one rendered Carousel item.
    private static Region carouselItemStateLayer(Node item) {
        return lookupRegion(carouselItemSlot(item), ".m3-state-layer");
    }

    /// Returns whether a carousel viewport is rendering an in-progress animated scroll position.
    private static boolean carouselViewportScrollIsInProgress(
            AtomicReference<@Nullable ScrollPane> viewportReference
    ) {
        @Nullable ScrollPane viewport = viewportReference.get();
        return viewport != null && isBetweenExclusive(viewport.getHvalue(), 0.0, 1.0);
    }

    /// Closes the carousel scroll test window and clears local motion settings.
    private static void closeCarouselScrollTestWindow(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable M3Carousel> carouselReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable M3Carousel carousel = carouselReference.get();
            if (carousel != null) {
                carousel.setAnimationsEnabled(true);
                FxTestUtils.clearMotionScheme(carousel);
            }

            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }
}
