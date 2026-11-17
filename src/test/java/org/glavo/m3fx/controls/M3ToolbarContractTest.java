// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies toolbar item layout and interactive state presentation.
@NotNullByDefault
final class M3ToolbarContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that toolbar component token properties are styleable from CSS.
    @Test
    void toolbarTokensAreStyleable() {
        M3Toolbar toolbar = toolbar(new M3Button("Archive"), new M3Button("Share"));
        toolbar.setStyle(
                "-m3-container-height: 72px; "
                        + "-m3-container-width: 80px; "
                        + "-m3-item-slot-size: 52px; "
                        + "-m3-content-padding: 10px; "
                        + "-m3-docked-content-padding: 12px; "
                        + "-m3-item-spacing: 6px; "
                        + "-m3-docked-max-item-spacing: 24px;"
        );

        applyCss(toolbar);

        assertEquals(72.0, toolbar.getContainerHeight(), 0.0001);
        assertEquals(80.0, toolbar.getContainerWidth(), 0.0001);
        assertEquals(52.0, toolbar.getItemSlotSize(), 0.0001);
        assertEquals(10.0, toolbar.getContentPadding(), 0.0001);
        assertEquals(12.0, toolbar.getDockedContentPadding(), 0.0001);
        assertEquals(6.0, toolbar.getItemSpacing(), 0.0001);
        assertEquals(24.0, toolbar.getDockedMaxItemSpacing(), 0.0001);
        assertEquals(72.0, toolbar.prefHeight(-1.0), 0.0001);
        assertEquals(10.0, toolbar.getPadding().getLeft(), 0.0001);

        toolbar.setOrientation(Orientation.VERTICAL);
        applyCss(toolbar);

        assertEquals(80.0, toolbar.prefWidth(-1.0), 0.0001);
    }

    /// Verifies that toolbar position variants and color styles update independently.
    @Test
    void toolbarVariantsAndActionStylesUpdateIndependently() {
        M3Button first = new M3Button("Archive");
        M3Button second = new M3Button("Share");
        M3Toolbar toolbar = toolbar(first, second);

        assertEquals(M3ToolbarVariant.FLOATING, toolbar.getVariant());
        assertEquals(M3ToolbarColorStyle.STANDARD, toolbar.getColorStyle());
        assertTrue(toolbar.getStyleClass().contains(M3ToolbarVariant.FLOATING.styleClass()));
        applyCss(toolbar);
        assertRegionRadii(toolbar, 16.0, 16.0, 16.0, 16.0);

        toolbar.setColorStyle(M3ToolbarColorStyle.VIBRANT);
        assertTrue(toolbar.getPseudoClassStates().stream()
                .anyMatch(pseudoClass -> pseudoClass.getPseudoClassName().equals("vibrant")));

        toolbar.setVariant(M3ToolbarVariant.DOCKED);
        assertTrue(toolbar.getStyleClass().contains(M3ToolbarVariant.DOCKED.styleClass()));
        assertFalse(toolbar.getStyleClass().contains(M3ToolbarVariant.FLOATING.styleClass()));
        applyCss(toolbar);
        assertRegionRadii(toolbar, 0.0, 0.0, 0.0, 0.0);
        assertEquals(8.0, toolbar.getPadding().getTop(), 0.0001);
        assertEquals(16.0, toolbar.getPadding().getRight(), 0.0001);
        assertEquals(8.0, toolbar.getPadding().getBottom(), 0.0001);
        assertEquals(16.0, toolbar.getPadding().getLeft(), 0.0001);

        assertTrue(first.getPseudoClassStates().stream()
                .anyMatch(pseudoClass -> pseudoClass.getPseudoClassName().equals("toolbar-action")));
        assertTrue(first.getPseudoClassStates().stream()
                .anyMatch(pseudoClass -> pseudoClass.getPseudoClassName().equals("vibrant")));
        toolbar.getItems().remove(first);
        assertFalse(first.getPseudoClassStates().stream()
                .anyMatch(pseudoClass -> pseudoClass.getPseudoClassName().equals("toolbar-action")));
        assertFalse(first.getPseudoClassStates().stream()
                .anyMatch(pseudoClass -> pseudoClass.getPseudoClassName().equals("vibrant")));
    }

    /// Verifies the published Standard and Vibrant toolbar color mappings for action states.
    @Tier2Test
    @Test
    void toolbarColorStylesMapActionStates() {
        FxTestUtils.runOnFxThread(() -> {
            M3Icon unselectedIcon = new M3Icon("A");
            M3Icon selectedIcon = new M3Icon("B");
            M3Icon disabledIcon = new M3Icon("C");
            M3IconButton unselected = new M3IconButton(unselectedIcon);
            M3IconToggleButton selected = new M3IconToggleButton(selectedIcon);
            selected.setSelected(true);
            M3IconButton disabled = new M3IconButton(disabledIcon);
            disabled.setDisable(true);
            M3Toolbar toolbar = toolbar(unselected, selected, disabled);
            VBox root = new VBox(toolbar);
            root.setStyle(visualTestColors());
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 360.0, 120.0));
                stage.show();
                root.applyCss();
                root.layout();

                Color surfaceContainer = Color.rgb(243, 237, 247);
                Color secondaryContainer = Color.rgb(222, 214, 250);
                Color onSurfaceVariant = Color.rgb(73, 69, 79);
                Color onSecondaryContainer = Color.rgb(40, 27, 92);
                Color onSurface = Color.rgb(30, 28, 32);
                assertEquals(surfaceContainer, toolbar.getBackground().getFills().get(0).getFill());
                assertLabeledColors(unselected, Color.TRANSPARENT, onSurfaceVariant);
                assertLabeledColors(selected, secondaryContainer, onSecondaryContainer);
                assertEquals(onSurfaceVariant, iconFill(unselectedIcon));
                assertEquals(onSecondaryContainer, iconFill(selectedIcon));
                assertEquals(onSurfaceVariant, lookupRegion(unselected, ".m3-state-layer")
                        .getBackground().getFills().get(0).getFill());
                assertEquals(onSecondaryContainer, lookupRegion(selected, ".m3-state-layer")
                        .getBackground().getFills().get(0).getFill());
                assertEquals(onSurface, iconFill(disabledIcon));

                toolbar.setColorStyle(M3ToolbarColorStyle.VIBRANT);
                root.applyCss();
                root.layout();

                Color primaryContainer = Color.rgb(226, 221, 255);
                Color onPrimaryContainer = Color.rgb(36, 14, 110);
                assertEquals(primaryContainer, toolbar.getBackground().getFills().get(0).getFill());
                assertLabeledColors(unselected, Color.TRANSPARENT, onPrimaryContainer);
                assertLabeledColors(selected, surfaceContainer, onSurface);
                assertEquals(onPrimaryContainer, iconFill(unselectedIcon));
                assertEquals(onSurface, iconFill(selectedIcon));
                unselected.fireEvent(primaryMouseEvent(
                        unselected,
                        MouseEvent.MOUSE_PRESSED,
                        unselected.getWidth() / 2.0,
                        unselected.getHeight() / 2.0,
                        true
                ));
                selected.fireEvent(primaryMouseEvent(
                        selected,
                        MouseEvent.MOUSE_PRESSED,
                        selected.getWidth() / 2.0,
                        selected.getHeight() / 2.0,
                        true
                ));
                assertEquals(onPrimaryContainer, lookupRegion(unselected, ".m3-ripple")
                        .getBackground().getFills().get(0).getFill());
                assertEquals(onSurface, lookupRegion(selected, ".m3-ripple")
                        .getBackground().getFills().get(0).getFill());
                assertEquals(onSurface, iconFill(disabledIcon));
                unselected.fireEvent(primaryMouseEvent(
                        unselected,
                        MouseEvent.MOUSE_RELEASED,
                        unselected.getWidth() / 2.0,
                        unselected.getHeight() / 2.0,
                        false
                ));
                selected.fireEvent(primaryMouseEvent(
                        selected,
                        MouseEvent.MOUSE_RELEASED,
                        selected.getWidth() / 2.0,
                        selected.getHeight() / 2.0,
                        false
                ));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that toolbar arrow keys follow orientation, right-to-left direction, and reachable item filtering.
    @Test
    void toolbarKeyboardTraversalFollowsOrientationDirectionAndReachability() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("Archive");
            M3Button hidden = new M3Button("Hidden");
            M3Button disabled = new M3Button("Disabled");
            M3Button second = new M3Button("Share");
            M3Button third = new M3Button("Edit");
            M3Button outside = new M3Button("Outside");
            hidden.setVisible(false);
            disabled.setDisable(true);
            M3Toolbar toolbar = toolbar(first, hidden, disabled, second, third);
            VBox root = new VBox(8.0, toolbar, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 520.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                first.requestFocus();
                assertTrue(first.isFocused());
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(second.isFocused());
                second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(third.isFocused());
                third.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(first.isFocused());
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(third.isFocused());
                third.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
                assertTrue(first.isFocused());
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                assertTrue(third.isFocused());

                outside.requestFocus();
                assertTrue(outside.isFocused());
                toolbar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(first.isFocused());
                outside.requestFocus();
                toolbar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(third.isFocused());

                toolbar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                first.requestFocus();
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(third.isFocused());
                third.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(first.isFocused());

                toolbar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                toolbar.setOrientation(Orientation.VERTICAL);
                root.applyCss();
                root.layout();
                first.requestFocus();
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(second.isFocused());
                second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(first.isFocused());
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(third.isFocused());

                M3TextField editor = createTextField("Edit", M3TextInputVariant.OUTLINED);
                M3Button afterEditor = new M3Button("After editor");
                M3Toolbar editorToolbar = toolbar(editor, afterEditor);
                root.getChildren().add(editorToolbar);
                assertContainerNavigationDoesNotStealTextInputFocus(root, editorToolbar, editor, afterEditor, KeyCode.RIGHT);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that toolbar navigation and accessibility actions use each item's actual exposed focus target.
    @Test
    void toolbarAccessibilityUsesNestedAndCurrentFocusTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button groupedFirst = new M3Button("Grouped first");
            M3Button groupedSecond = new M3Button("Grouped second");
            M3Button hidden = new M3Button("Hidden");
            M3Button standalone = new M3Button("Standalone");
            M3Button outside = new M3Button("Outside");
            hidden.setVisible(false);
            M3ButtonGroup group = buttonGroup(groupedFirst, groupedSecond);
            M3Toolbar toolbar = toolbar(group, hidden, standalone);
            VBox root = new VBox(8.0, toolbar, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 560.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertSame(groupedFirst, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                toolbar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(groupedFirst.isFocused());

                groupedSecond.requestFocus();
                assertTrue(groupedSecond.isFocused());
                assertSame(groupedSecond, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                toolbar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(groupedSecond.isFocused());
                toolbar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(standalone.isFocused());
                toolbar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(groupedFirst.isFocused());

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, standalone);
                assertTrue(standalone.isFocused());
                outside.requestFocus();
                assertTrue(outside.isFocused());
                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hidden);
                assertTrue(outside.isFocused());
                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 2);
                assertTrue(standalone.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that toolbar accessibility actions can reveal popup-backed nested action targets.
    @Test
    void toolbarRevealsNestedMenuPopupTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem firstItem = new M3MenuItem("First");
            M3MenuItem secondItem = new M3MenuItem("Second");
            M3MenuButton menuButton = new M3MenuButton("Open", firstItem, secondItem);
            M3Button standalone = new M3Button("Standalone");
            M3Button outside = new M3Button("Outside");
            M3Toolbar toolbar = toolbar(menuButton, standalone);
            menuButton.setAnimationsEnabled(false);
            VBox root = new VBox(8.0, toolbar, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 560.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertFalse(menuButton.isShowing());

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, secondItem);

                assertTrue(menuButton.isShowing());
                assertTrue(secondItem.isFocused());
                assertSame(secondItem, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                toolbar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(secondItem.isFocused());
                assertSame(secondItem, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                outside.requestFocus();
                toolbar.setVisible(false);
                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, firstItem);

                assertFalse(menuButton.isShowing());
                assertTrue(outside.isFocused());
            } finally {
                try {
                    menuButton.hideMenu();
                } finally {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that horizontal toolbar item slots mirror their visual order in right-to-left scenes.
    @Test
    void toolbarRightToLeftLayoutMirrorsHorizontalActionSlots() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            M3Toolbar toolbar = toolbar(first, second, third);
            toolbar.setItemSlotSize(56.0);
            toolbar.setItemSpacing(4.0);
            toolbar.setContentPadding(8.0);
            Pane root = new Pane(toolbar);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 120.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();

                toolbar.resizeRelocate(16.0, 20.0, 260.0, toolbar.prefHeight(-1.0));
                root.layout();
                toolbar.layout();

                assertTrue(sceneCenter(first).getX() < sceneCenter(second).getX());
                assertTrue(sceneCenter(second).getX() < sceneCenter(third).getX());

                toolbar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                toolbar.layout();

                assertTrue(sceneCenter(first).getX() > sceneCenter(second).getX());
                assertTrue(sceneCenter(second).getX() > sceneCenter(third).getX());
                double slotCenterY = Double.NaN;
                int slotCount = 0;
                for (Node slot : toolbar.lookupAll("." + "m3-toolbar-item-slot")) {
                    assertTrue(slot.getLayoutBounds().getWidth() >= 56.0);
                    assertEquals(56.0, slot.getLayoutBounds().getHeight(), 0.0001);
                    double currentCenterY = sceneCenter(slot).getY();
                    if (Double.isNaN(slotCenterY)) {
                        slotCenterY = currentCenterY;
                    } else {
                        assertEquals(slotCenterY, currentCenterY, 0.75);
                    }
                    slotCount++;
                }
                assertEquals(3, slotCount);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies item mutations update accessible indices and generated slots without retaining removed nodes.
    @Test
    void toolbarItemMutationsRebuildGeneratedSlots() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            M3Toolbar toolbar = toolbar(first, second);
            Pane root = new Pane(toolbar);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 360.0, 120.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(16.0, 20.0, 300.0, toolbar.prefHeight(-1.0));
                root.layout();
                toolbar.layout();

                assertEquals(2, toolbar.lookupAll("." + "m3-toolbar-item-slot").size());
                assertEquals(2, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(first, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(second, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertTrue(first.getParent() != null);
                assertTrue(second.getParent() != null);

                toolbar.getItems().add(third);
                root.applyCss();
                root.layout();
                toolbar.layout();

                assertEquals(3, toolbar.lookupAll("." + "m3-toolbar-item-slot").size());
                assertEquals(3, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(third, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
                assertTrue(third.getParent() != null);

                assertTrue(toolbar.getItems().remove(second));
                root.applyCss();
                root.layout();
                toolbar.layout();

                assertEquals(2, toolbar.lookupAll("." + "m3-toolbar-item-slot").size());
                assertNull(second.getParent());
                assertEquals(2, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(first, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(third, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertTrue(first.getParent() != null);
                assertTrue(third.getParent() != null);

                toolbar.getItems().clear();
                root.applyCss();
                root.layout();
                toolbar.layout();

                assertEquals(0, toolbar.lookupAll("." + "m3-toolbar-item-slot").size());
                assertEquals(0, toolbar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertNull(first.getParent());
                assertNull(third.getParent());
            } finally {
                stage.close();
            }
        });
    }
}
