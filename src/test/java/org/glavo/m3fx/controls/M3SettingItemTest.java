// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Insets;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the value, action, layout, and focus contracts of Material settings rows.
@NotNullByDefault
final class M3SettingItemTest {
    /// Starts the JavaFX toolkit before setting rows are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that an action setting row dispatches only while enabled.
    @Test
    void actionSettingItemDispatchesActionsWithoutIntroducingSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3SettingItem item = new M3SettingItem("Account");
            AtomicInteger actionCount = new AtomicInteger();
            item.setOnAction(event -> actionCount.incrementAndGet());

            assertEquals(AccessibleRole.BUTTON, item.getAccessibleRole());
            assertTrue(item.isFocusTraversable());
            assertFalse(item.isSelected());

            item.fire();
            assertEquals(1, actionCount.get());
            assertFalse(item.isSelected());

            item.setDisable(true);
            item.fire();
            assertEquals(1, actionCount.get());
        });
    }

    /// Verifies that a switch setting row toggles before its action event and owns an interactive trailing switch.
    @Test
    void switchSettingItemTogglesOncePerActivation() {
        FxTestUtils.runOnFxThread(() -> {
            M3SwitchSettingItem item = new M3SwitchSettingItem("Automatic updates");
            AtomicInteger actionCount = new AtomicInteger();
            item.setOnAction(event -> {
                assertTrue(item.isSelected(), "action must observe the updated switch value");
                actionCount.incrementAndGet();
            });

            assertEquals(AccessibleRole.CHECK_BOX, item.getAccessibleRole());
            M3Switch indicator = assertInstanceOf(M3Switch.class, item.getTrailing());
            assertSame(indicator, item.getSwitch());
            assertFalse(indicator.isMouseTransparent(), "the trailing switch must accept pointer input");
            assertFalse(indicator.isFocusTraversable(), "the row remains the keyboard target");

            item.fire();
            assertTrue(item.isSelected());
            assertTrue(indicator.isSelected());
            assertEquals(1, actionCount.get());

            item.setSelected(false);
            assertFalse(indicator.isSelected());
            assertEquals(1, actionCount.get(), "direct property changes must not fire actions");

            item.executeAccessibleAction(AccessibleAction.FIRE);
            assertTrue(item.isSelected());
            assertEquals(2, actionCount.get());

            item.setDisable(true);
            item.fire();
            assertTrue(item.isSelected());
            assertEquals(2, actionCount.get());
        });
    }

    /// Verifies that activating the nested switch forwards one row action without a second value toggle.
    @Test
    void switchSettingItemForwardsNestedSwitchActionOnce() {
        FxTestUtils.runOnFxThread(() -> {
            M3SwitchSettingItem item = new M3SwitchSettingItem("Notifications");
            AtomicInteger actionCount = new AtomicInteger();
            item.setOnAction(event -> actionCount.incrementAndGet());

            item.getSwitch().fire();
            assertTrue(item.isSelected());
            assertTrue(item.getSwitch().isSelected());
            assertEquals(1, actionCount.get());

            item.getSwitch().fire();
            assertFalse(item.isSelected());
            assertEquals(2, actionCount.get());
        });
    }

    /// Verifies the checkbox setting tri-state activation cycle and accessibility state.
    @Test
    void checkBoxSettingItemCyclesTriStateBeforeAction() {
        FxTestUtils.runOnFxThread(() -> {
            M3CheckBoxSettingItem item = new M3CheckBoxSettingItem("Use mobile data");
            item.setAllowIndeterminate(true);
            AtomicInteger actionCount = new AtomicInteger();
            item.setOnAction(event -> actionCount.incrementAndGet());

            assertEquals(AccessibleRole.CHECK_BOX, item.getAccessibleRole());
            assertInstanceOf(M3CheckBox.class, item.getTrailing());
            assertFalse(item.isSelected());
            assertFalse(item.isIndeterminate());

            item.fire();
            assertFalse(item.isSelected());
            assertTrue(item.isIndeterminate());
            assertEquals(true, item.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));

            item.fire();
            assertTrue(item.isSelected());
            assertFalse(item.isIndeterminate());

            item.fire();
            assertFalse(item.isSelected());
            assertFalse(item.isIndeterminate());
            assertEquals(3, actionCount.get());

            item.setIndeterminate(true);
            assertEquals(3, actionCount.get(), "direct property changes must not fire actions");
            item.setDisable(true);
            item.executeAccessibleAction(AccessibleAction.FIRE);
            assertTrue(item.isIndeterminate());
            assertEquals(3, actionCount.get());
        });
    }

    /// Verifies grouped and independent radio setting-row activation semantics.
    @Test
    void radioButtonSettingItemsFollowToggleGroupContract() {
        FxTestUtils.runOnFxThread(() -> {
            ToggleGroup group = new ToggleGroup();
            M3RadioButtonSettingItem first = new M3RadioButtonSettingItem("System");
            M3RadioButtonSettingItem second = new M3RadioButtonSettingItem("Dark");
            first.setToggleGroup(group);
            second.setToggleGroup(group);
            AtomicInteger firstActions = new AtomicInteger();
            AtomicInteger secondActions = new AtomicInteger();
            first.setOnAction(event -> firstActions.incrementAndGet());
            second.setOnAction(event -> secondActions.incrementAndGet());

            assertEquals(AccessibleRole.RADIO_BUTTON, first.getAccessibleRole());
            assertInstanceOf(M3RadioButton.class, first.getTrailing());

            first.fire();
            assertTrue(first.isSelected());
            assertFalse(second.isSelected());
            assertEquals(first, group.getSelectedToggle());
            assertEquals(1, firstActions.get());

            first.fire();
            assertEquals(1, firstActions.get(), "selected group members must ignore repeated activation");

            second.fire();
            assertFalse(first.isSelected());
            assertTrue(second.isSelected());
            assertEquals(second, group.getSelectedToggle());
            assertEquals(1, secondActions.get());

            M3RadioButtonSettingItem independent = new M3RadioButtonSettingItem("Independent");
            AtomicInteger independentActions = new AtomicInteger();
            independent.setOnAction(event -> independentActions.incrementAndGet());
            independent.fire();
            independent.fire();
            assertFalse(independent.isSelected());
            assertEquals(2, independentActions.get());
        });
    }

    /// Verifies that an expandable setting row toggles expansion and keeps trailing disclosure presentation.
    @Test
    void expandableSettingItemTogglesExpandedStateOnActivation() {
        FxTestUtils.runOnFxThread(() -> {
            M3ExpandableSettingItem item = new M3ExpandableSettingItem("Advanced");
            javafx.scene.control.Label body = new javafx.scene.control.Label("Nested settings");
            item.setContent(body);
            AtomicInteger actionCount = new AtomicInteger();
            item.setOnAction(event -> actionCount.incrementAndGet());

            assertEquals(AccessibleRole.BUTTON, item.getAccessibleRole());
            assertFalse(item.isExpanded());
            assertEquals(false, item.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            assertInstanceOf(org.glavo.m3fx.internal.M3DisclosureIcon.class, item.getTrailing());

            item.fire();
            assertTrue(item.isExpanded());
            assertEquals(true, item.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            assertEquals(1, actionCount.get());
            assertTrue(item.getTrailing() instanceof org.glavo.m3fx.internal.M3DisclosureIcon disclosure
                    && disclosure.isExpanded());

            item.setExpanded(false);
            assertFalse(item.isExpanded());
            assertEquals(1, actionCount.get(), "direct expanded changes must not fire actions");

            item.fire();
            assertTrue(item.isExpanded());
            assertEquals(2, actionCount.get());

            item.setDisable(true);
            item.fire();
            assertTrue(item.isExpanded());
            assertEquals(2, actionCount.get());
        });
    }

    /// Verifies that a select setting row formats values, owns a passive disclosure indicator, and fires on choice.
    @Test
    void selectSettingItemFormatsValueAndFiresOnChoice() {
        FxTestUtils.runOnFxThread(() -> {
            M3SelectSettingItem<String> item = new M3SelectSettingItem<>("Language");
            item.getItems().setAll("English", "中文", "日本語");
            item.setConverter(value -> switch (value) {
                case "English" -> "English";
                case "中文" -> "Chinese";
                case "日本語" -> "Japanese";
                default -> value;
            });
            item.setValue("English");
            AtomicInteger actionCount = new AtomicInteger();
            item.setOnAction(event -> actionCount.incrementAndGet());

            assertEquals(AccessibleRole.COMBO_BOX, item.getAccessibleRole());
            assertEquals("English", item.getTrailingSupportingText());
            assertInstanceOf(org.glavo.m3fx.internal.M3DisclosureIcon.class, item.getTrailing());
            assertTrue(item.getTrailing().isMouseTransparent());
            assertFalse(item.getTrailing().isFocusTraversable());

            item.fire();
            assertEquals(0, actionCount.get(), "opening the menu must not fire a value action");
            assertFalse(item.isShowing(), "popup requires a showing window and stays closed in headless fire()");

            item.setValue("中文");
            assertEquals("Chinese", item.getTrailingSupportingText());
            assertEquals(0, actionCount.get(), "direct value changes must not fire actions");

            item.setShowValue(false);
            assertEquals("", item.getTrailingSupportingText());
            item.setShowValue(true);
            assertEquals("Chinese", item.getTrailingSupportingText());

            assertInstanceOf(M3MenuItem.class, item.getMenu().getItems().get(2)).fire();
            assertEquals("日本語", item.getValue());
            assertEquals("Japanese", item.getTrailingSupportingText());
            assertEquals(1, actionCount.get(), "choosing a menu item must fire one action");
        });
    }

    /// Verifies that segmented lists insert their official gap only between adjacent list-derived rows.
    @Test
    void segmentedListAppliesGapToSettingRowsOnly() {
        FxTestUtils.runOnFxThread(() -> {
            M3SwitchSettingItem first = new M3SwitchSettingItem("First");
            M3CheckBoxSettingItem second = new M3CheckBoxSettingItem("Second");
            M3Divider divider = new M3Divider();
            M3RadioButtonSettingItem third = new M3RadioButtonSettingItem("Third");
            M3SettingItem fourth = new M3SettingItem("Fourth");
            M3SelectSettingItem<String> fifth = new M3SelectSettingItem<>("Fifth");
            M3ListPane listPane = new M3ListPane();
            listPane.setListStyle(M3ListStyle.SEGMENTED);
            listPane.setSelectionMode(M3SelectionMode.NONE);
            listPane.getItems().addAll(first, second, divider, third, fourth, fifth);

            StackPane root = new StackPane(listPane);
            Scene scene = new Scene(root, 400.0, 300.0);
            root.applyCss();
            root.layout();

            assertEquals(2.0, listPane.getItemSpacing(), 0.0001);
            assertEquals(new Insets(0.0, 0.0, 2.0, 0.0), VBox.getMargin(first));
            assertNull(VBox.getMargin(second), "a divider must not inherit a segmented list gap");
            assertNull(VBox.getMargin(divider), "a divider must not receive a segmented list gap");
            assertEquals(new Insets(0.0, 0.0, 2.0, 0.0), VBox.getMargin(third));
            assertEquals(new Insets(0.0, 0.0, 2.0, 0.0), VBox.getMargin(fourth));
            assertNull(VBox.getMargin(fifth), "the final row must not receive trailing list spacing");
        });
    }

    /// Verifies that unselected list panes move focus across setting rows with directional keys.
    @Tier2Test
    @Test
    void nonSelectableListTraversesSettingRowsWithDirectionKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3SwitchSettingItem first = new M3SwitchSettingItem("First");
            M3CheckBoxSettingItem second = new M3CheckBoxSettingItem("Second");
            M3RadioButtonSettingItem third = new M3RadioButtonSettingItem("Third");
            M3ListPane listPane = new M3ListPane();
            listPane.setSelectionMode(M3SelectionMode.NONE);
            listPane.getItems().addAll(first, second, third);

            Stage stage = new Stage();
            try {
                StackPane root = new StackPane(listPane);
                stage.setScene(new Scene(root, 360.0, 240.0));
                stage.show();
                root.applyCss();
                root.layout();
                assertSame(
                        first,
                        listPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE),
                        "an unselected settings list should expose its first row as the accessibility focus target"
                );
                listPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(first.isFocused(), "accessibility focus should target the setting row, not its indicator");

                listPane.fireEvent(keyPressed(KeyCode.DOWN));
                assertTrue(second.isFocused(), "down key should move focus to the next setting row");

                listPane.fireEvent(keyPressed(KeyCode.DOWN));
                assertTrue(third.isFocused(), "down key should continue through setting rows");
            } finally {
                stage.close();
            }
        });
    }

    /// Creates an unmodified key-pressed event for focus traversal tests.
    ///
    /// @param keyCode the key code to dispatch
    /// @return the key event
    private static KeyEvent keyPressed(KeyCode keyCode) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false);
    }
}