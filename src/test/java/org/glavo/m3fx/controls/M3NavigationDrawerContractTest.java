// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.skins.M3NavigationDrawerGroupSkin;
import org.glavo.m3fx.skins.M3NavigationDrawerSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies drawer destinations, expandable groups, and selection feedback.
@NotNullByDefault
final class M3NavigationDrawerContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that navigation drawers support printable-key type-ahead across expanded groups.
    @Test
    void navigationDrawerSupportsTypeAheadKeyboardNavigation() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem overview = new M3ListItem("Overview");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem segmentedButtons = new M3ListItem("Segmented buttons");
            M3ListItem splitButtons = new M3ListItem("Split buttons");
            group.getItems().addAll(buttons, segmentedButtons, splitButtons);
            group.setExpanded(true);
            segmentedButtons.setDisable(true);
            M3NavigationDrawer drawer = navigationDrawer(overview, group);
            Pane root = new Pane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 360.0, 260.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.requestFocus();

                drawer.fireEvent(keyTypedEvent("s"));

                assertTrue(splitButtons.isFocused());
                assertSame(splitButtons, drawer.getSelectedItem());
                assertSame(splitButtons, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                drawer.fireEvent(keyTypedEvent("o"));

                assertTrue(overview.isFocused());
                assertSame(overview, drawer.getSelectedItem());
                assertSame(overview, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that drawer group disclosure keys keep focus on visible rows.
    @Test
    void navigationDrawerGroupDisclosureKeysRestoreHeaderFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem iconButtons = new M3ListItem("Icon buttons");
            group.getItems().addAll(buttons, iconButtons);
            group.setExpanded(true);
            M3NavigationDrawer drawer = navigationDrawer(group);
            drawer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            Pane root = new Pane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 360.0, 260.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                iconButtons.requestFocus();
                assertTrue(iconButtons.isFocused());

                iconButtons.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertFalse(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());
                assertSame(group.getHeaderItem(), drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                group.getHeaderItem().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());

                drawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                iconButtons.requestFocus();
                drawer.select(iconButtons);

                iconButtons.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertFalse(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());

                group.getHeaderItem().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that mouse selection keeps ripple feedback while the selected container transitions.
    @Tier2Test
    @Test
    void navigationDrawerMouseSelectionKeepsRippleAndAnimatesSelection() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Parent> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> selectedItemReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> {
                M3ListItem first = new M3ListItem("Sheets");
                M3ListItem second = new M3ListItem("Bottom sheets");
                M3NavigationDrawer drawer = navigationDrawer(first, second);
                drawer.select(first);
                drawer.setPrefWidth(320.0);

                StackPane root = new StackPane(drawer);
                root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
                Scene scene = new Scene(root, 380.0, 180.0);
                Stage stage = new Stage();

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, true);
                FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(380.0, 180.0);
                root.layout();
                drawer.applyCss();
                drawer.resize(320.0, 116.0);
                drawer.layout();
                first.layout();
                second.layout();

                double clickX = second.getWidth() / 2.0;
                double clickY = second.getHeight() / 2.0;
                second.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, clickX, clickY, true));
                Region ripple = lookupRegion(second, ".m3-ripple");
                assertTrue(ripple.getOpacity() > 0.0);
                second.fire();
                second.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, clickX, clickY, false));

                stageReference.set(stage);
                rootReference.set(root);
                selectedItemReference.set(second);
            });

            FxTestUtils.runOnFxThreadWhen(
                    () -> drawerMouseSelectionIsTransitioning(rootReference, selectedItemReference),
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3ListItem selectedItem = Objects.requireNonNull(selectedItemReference.get(), "selectedItem");
                        Region selection = listItemSelectionContainer(selectedItem);
                        Region ripple = lookupRegion(selectedItem, ".m3-ripple");
                        Label selectedHeadline = assertInstanceOf(
                                Label.class,
                                selectedItem.lookup(".m3-list-item-headline")
                        );

                        assertTrue(selectedItem.isSelected());
                        assertTrue(ripple.getOpacity() > 0.0);
                        assertFalse(
                                selectedHeadline.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")),
                                "selected list item text should wait for the selected container transition"
                        );
                        assertBetween(selection.getOpacity(), 0.25, 1.0, "mouse-selected drawer item opacity");
                        assertBetween(selection.getScaleX(), 0.96, 1.0, "mouse-selected drawer item horizontal scale");
                        assertBetween(selection.getScaleY(), 0.96, 1.0, "mouse-selected drawer item vertical scale");

                        WritableImage image = snapshotImageOnFxThread(root);
                        assertSnapshotNodeContainsContrast(image, selection, Color.WHITE, 0.03);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-navigation-drawer-click-selection-frame.png"
                        ));
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        M3ListItem selectedItem = selectedItemReference.get();
                        if (selectedItem == null || observableRootUnavailable(rootReference)) {
                            return false;
                        }

                        Region selection = listItemSelectionContainer(selectedItem);
                        return Math.abs(selection.getOpacity() - 1.0) < 0.0001
                                && Math.abs(selection.getScaleX() - 1.0) < 0.0001
                                && Math.abs(selection.getScaleY() - 1.0) < 0.0001;
                    },
                    () -> {
                    },
                    () -> {
                        M3ListItem selectedItem = Objects.requireNonNull(selectedItemReference.get(), "selectedItem");
                        Label selectedHeadline = assertInstanceOf(
                                Label.class,
                                selectedItem.lookup(".m3-list-item-headline")
                        );
                        assertTrue(
                                selectedHeadline.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")),
                                "selected list item text should update after the selected container transition settles"
                        );
                    }
            );
        } finally {
            closeObservableMotionWindow(stageReference, rootReference);
        }
    }

    /// Verifies that navigation drawers group list items and keep one selected item.
    @Test
    void navigationDrawerGroupsItemsAndKeepsSelection() {
        M3ListItem home = new M3ListItem("Home");
        M3ListItem search = new M3ListItem("Search");
        M3NavigationDrawer navigationDrawer = navigationDrawer(home, new M3Divider(), search);

        assertTrue(home.isSelected());
        assertEquals(home, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationDrawer.getSelectedItems());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertThrows(IllegalArgumentException.class, () -> navigationDrawer.selectIndex(1));

        navigationDrawer.selectIndex(2);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationDrawer.getSelectedItems());
        assertEquals(2, navigationDrawer.getSelectedIndex());

        navigationDrawer.selectNext();

        assertEquals(home, navigationDrawer.getSelectedItem());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertTrue(home.isSelected());
        assertFalse(search.isSelected());

        navigationDrawer.selectPrevious();

        assertEquals(search, navigationDrawer.getSelectedItem());
        assertEquals(2, navigationDrawer.getSelectedIndex());
        assertFalse(home.isSelected());
        assertTrue(search.isSelected());

        home.setSelected(true);

        assertTrue(home.isSelected());
        assertFalse(search.isSelected());
        assertEquals(home, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(home), navigationDrawer.getSelectedItems());

        navigationDrawer.getItems().remove(home);

        assertFalse(home.isSelected());
        assertTrue(search.isSelected());
        assertEquals(search, navigationDrawer.getSelectedItem());
        assertEquals(java.util.List.of(search), navigationDrawer.getSelectedItems());

        navigationDrawer.setAllowEmptySelection(true);
        navigationDrawer.clearSelection();

        assertNull(navigationDrawer.getSelectedItem());
        assertTrue(navigationDrawer.getSelectedItems().isEmpty());
        assertEquals(-1, navigationDrawer.getSelectedIndex());
        assertFalse(search.isSelected());
    }

    /// Verifies that navigation drawers reject disabled, hidden, and collapsed selection targets.
    @Test
    void navigationDrawerSelectionRejectsUnreachableItems() {
        M3ListItem disabled = new M3ListItem("Disabled");
        M3ListItem hidden = new M3ListItem("Hidden");
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
        M3ListItem collapsed = new M3ListItem("Collapsed");
        M3ListItem fallback = new M3ListItem("Fallback");
        disabled.setDisable(true);
        hidden.setVisible(false);
        group.getItems().add(collapsed);
        M3NavigationDrawer navigationDrawer = navigationDrawer(disabled, hidden, group, fallback);

        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> navigationDrawer.select(disabled));
        assertThrows(IllegalArgumentException.class, () -> navigationDrawer.select(hidden));
        assertThrows(IllegalArgumentException.class, () -> navigationDrawer.select(collapsed));
        assertThrows(IllegalArgumentException.class, () -> navigationDrawer.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> navigationDrawer.selectIndex(1));

        disabled.setSelected(true);
        hidden.setSelected(true);
        collapsed.setSelected(true);

        assertFalse(disabled.isSelected());
        assertFalse(hidden.isSelected());
        assertFalse(collapsed.isSelected());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(List.of(group.getHeaderItem()), navigationDrawer.getSelectedItems());

        group.setExpanded(true);
        navigationDrawer.select(collapsed);

        assertEquals(collapsed, navigationDrawer.getSelectedItem());
        assertTrue(collapsed.isSelected());

        collapsed.setDisable(true);

        assertFalse(collapsed.isSelected());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());

        group.getHeaderItem().setDisable(true);

        assertFalse(group.getHeaderItem().isSelected());
        assertEquals(fallback, navigationDrawer.getSelectedItem());
        assertEquals(List.of(fallback), navigationDrawer.getSelectedItems());

        fallback.setVisible(false);

        assertFalse(fallback.isSelected());
        assertNull(navigationDrawer.getSelectedItem());
        assertTrue(navigationDrawer.getSelectedItems().isEmpty());
    }

    /// Verifies that navigation drawers support collapsible destination groups.
    @Test
    void navigationDrawerSupportsCollapsibleGroups() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem buttons = new M3ListItem("Buttons");
        M3ListItem fabs = new M3ListItem("FABs");
        group.getItems().addAll(buttons, fabs);
        M3ListItem overview = new M3ListItem("Overview");
        M3NavigationDrawer navigationDrawer = navigationDrawer(group, overview);

        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertEquals(2, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(group.getHeaderItem(), navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(overview, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertInstanceOf(M3DisclosureIcon.class, group.getHeaderItem().getTrailing());

        group.getHeaderItem().fire();

        assertTrue(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(4, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(buttons, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

        buttons.fire();

        assertEquals(buttons, navigationDrawer.getSelectedItem());
        assertEquals(1, navigationDrawer.getSelectedIndex());
        assertTrue(buttons.isSelected());
        assertFalse(group.getHeaderItem().isSelected());

        group.setExpanded(false);

        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(0, navigationDrawer.getSelectedIndex());
        assertTrue(group.getHeaderItem().isSelected());
        assertFalse(buttons.isSelected());
        navigationDrawer.selectNext();
        assertEquals(overview, navigationDrawer.getSelectedItem());

        group.setExpanded(false);
        navigationDrawer.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, fabs);

        assertTrue(group.isExpanded());
        assertEquals(fabs, navigationDrawer.getSelectedItem());
        assertEquals(2, navigationDrawer.getSelectedIndex());

        group.setExpanded(false);
        navigationDrawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, buttons);

        assertTrue(group.isExpanded());
    }

    /// Verifies that navigation drawers expand and collapse destination groups from keyboard disclosure keys.
    @Test
    void navigationDrawerHandlesGroupDisclosureKeys() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem commonButtons = new M3ListItem("Common buttons");
        M3ListItem floatingActions = new M3ListItem("Floating actions");
        group.getItems().addAll(commonButtons, floatingActions);
        M3ListItem overview = new M3ListItem("Overview");
        M3NavigationDrawer navigationDrawer = navigationDrawer(group, overview);

        KeyEvent expandEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);
        navigationDrawer.fireEvent(expandEvent);

        assertTrue(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(4, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        navigationDrawer.select(commonButtons);
        KeyEvent collapseFromChildEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT);
        navigationDrawer.fireEvent(collapseFromChildEvent);

        assertFalse(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
        assertEquals(2, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        KeyEvent nextEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);
        navigationDrawer.fireEvent(nextEvent);

        assertEquals(overview, navigationDrawer.getSelectedItem());
    }

    /// Verifies that right-to-left navigation drawers mirror disclosure arrow keys.
    @Test
    void navigationDrawerMirrorsGroupDisclosureKeysForRightToLeft() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem commonButtons = new M3ListItem("Common buttons");
        M3ListItem floatingActions = new M3ListItem("Floating actions");
        group.getItems().addAll(commonButtons, floatingActions);
        M3NavigationDrawer navigationDrawer = navigationDrawer(group);
        navigationDrawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        KeyEvent expandEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT);
        navigationDrawer.fireEvent(expandEvent);

        assertTrue(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());

        navigationDrawer.select(commonButtons);
        KeyEvent collapseFromChildEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);
        navigationDrawer.fireEvent(collapseFromChildEvent);

        assertFalse(group.isExpanded());
        assertEquals(group.getHeaderItem(), navigationDrawer.getSelectedItem());
    }

    /// Verifies that navigation drawer groups expose disclosure accessibility state and actions.
    @Test
    void navigationDrawerGroupExposesAccessibleDisclosureStateAndActions() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem commonButtons = new M3ListItem("Common buttons");
        M3ListItem floatingActions = new M3ListItem("Floating actions");
        group.getItems().addAll(commonButtons, floatingActions);

        applyCss(group);

        assertEquals("Buttons", group.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(false, group.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(group.getHeaderItem(), group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertEquals(1, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(group.getHeaderItem(), group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        group.executeAccessibleAction(AccessibleAction.FIRE);

        assertTrue(group.isExpanded());
        assertEquals(true, group.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        assertEquals(3, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(commonButtons, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(floatingActions, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));

        group.executeAccessibleAction(AccessibleAction.COLLAPSE);

        assertFalse(group.isExpanded());
        assertEquals(1, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 2);

        assertTrue(group.isExpanded());
        assertEquals(floatingActions, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));

        group.executeAccessibleAction(AccessibleAction.COLLAPSE);
        group.executeAccessibleAction(AccessibleAction.EXPAND);

        assertTrue(group.isExpanded());
    }

    /// Verifies that navigation drawer groups route focus to visible rows and restore it after collapse.
    @Test
    void navigationDrawerGroupRoutesAccessibleFocusAcrossExpandedRows() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem commonButtons = new M3ListItem("Common buttons");
            M3ListItem floatingActions = new M3ListItem("Floating actions");
            M3Button floatingActionDetails = new M3Button("Open");
            floatingActions.setTrailing(floatingActionDetails);
            group.getItems().addAll(commonButtons, floatingActions);
            group.setExpanded(true);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(group);
                stage.setScene(new Scene(root, 320.0, 240.0));
                stage.show();
                root.applyCss();
                root.layout();
                group.layout();

                commonButtons.requestFocus();

                assertTrue(commonButtons.isFocused());
                assertSame(commonButtons, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                group.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(group.isExpanded());
                assertTrue(group.getHeaderItem().isFocused());
                assertSame(group.getHeaderItem(), group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(1, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, floatingActions);

                assertTrue(group.isExpanded());
                assertTrue(floatingActions.isFocused());
                assertSame(floatingActions, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(3, group.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, floatingActionDetails);

                assertTrue(floatingActionDetails.isFocused());
                assertSame(floatingActionDetails, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers can reveal and focus descendants inside collapsed group rows.
    @Test
    void navigationDrawerAccessibleShowItemFocusesDescendantInCollapsedGroup() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem commonButtons = new M3ListItem("Common buttons");
            M3ListItem floatingActions = new M3ListItem("Floating actions");
            M3Button floatingActionDetails = new M3Button("Open");
            floatingActions.setTrailing(floatingActionDetails);
            group.getItems().addAll(commonButtons, floatingActions);
            M3ListItem overview = new M3ListItem("Overview");
            M3NavigationDrawer drawer = navigationDrawer(overview, group);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 360.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                drawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, floatingActionDetails);

                assertTrue(group.isExpanded());
                assertTrue(floatingActionDetails.isFocused());
                assertSame(floatingActionDetails, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                group.setExpanded(false);
                drawer.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, floatingActionDetails);

                assertTrue(group.isExpanded());
                assertSame(floatingActions, drawer.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawer groups expose their skin and visible child structure.
    @Test
    void navigationDrawerGroupCreatesMaterialSkinAndTogglesChildren() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
        M3ListItem buttons = new M3ListItem("Buttons");
        M3ListItem fabs = new M3ListItem("FABs");
        group.getItems().addAll(buttons, fabs);
        Pane root = new Pane(group);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3NavigationDrawerGroupSkin.class, group.getSkin());
        assertEquals(0, group.lookupAll("." + "m3-navigation-drawer-group-child").size());

        group.setExpanded(true);
        root.applyCss();
        group.layout();

        assertEquals(2, group.lookupAll("." + "m3-navigation-drawer-group-child").size());
        assertEquals(56.0, group.getHeaderItem().getOneLineHeight(), 0.0001);
        assertEquals(56.0, buttons.getOneLineHeight(), 0.0001);
        assertEquals(32.0, buttons.getHorizontalPadding(), 0.0001);
    }

    /// Verifies that selected child rows indent from the parent while aligning their right edge.
    @Test
    void navigationDrawerGroupChildSelectionPillIndentsAndAlignsRight() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            M3ListItem sideSheets = new M3ListItem("Side sheets");
            group.getItems().addAll(bottomSheets, sideSheets);
            group.setExpanded(true);
            M3NavigationDrawer drawer = navigationDrawer(group);
            drawer.select(bottomSheets);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 260.0);
            root.layout();
            group.layout();
            bottomSheets.layout();

            assertDrawerGroupChildIndentUsesLeftToRightGeometry(group, bottomSheets);

            WritableImage image = snapshotImageOnFxThread(root);
            M3ListItem header = group.getHeaderItem();
            Color beforeHeaderLayer = snapshotNodePixel(image, header, -4.0, header.getHeight() / 2.0);
            Color insideHeaderLayer = snapshotNodePixel(image, header, 12.0, header.getHeight() / 2.0);
            Color beforeChildPill = snapshotNodePixel(image, bottomSheets, -4.0, bottomSheets.getHeight() / 2.0);
            Color insideChildPill = snapshotNodePixel(image, bottomSheets, 12.0, bottomSheets.getHeight() / 2.0);
            assertTrue(colorDistance(beforeHeaderLayer, insideHeaderLayer) > 0.01);
            assertFalse(listItemHeadlineLabel(header).getFont().getStyle().contains("Bold"));
            assertTrue(colorDistance(beforeChildPill, insideChildPill) > 0.01);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-drawer-child-selection.png"
            ));
        });
    }

    /// Verifies that selected child rows mirror their indentation in right-to-left drawers.
    @Test
    void navigationDrawerGroupChildSelectionPillMirrorsIndentForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            M3ListItem sideSheets = new M3ListItem("Side sheets");
            group.getItems().addAll(bottomSheets, sideSheets);
            group.setExpanded(true);
            M3NavigationDrawer drawer = navigationDrawer(group);
            drawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            drawer.select(bottomSheets);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 260.0);
            root.layout();
            group.layout();
            bottomSheets.layout();

            assertDrawerGroupChildIndentUsesRightToLeftGeometry(group, bottomSheets);

            WritableImage image = snapshotImageOnFxThread(root);
            Color afterChildPill =
                    snapshotNodePixel(image, bottomSheets, bottomSheets.getWidth() + 4.0, bottomSheets.getHeight() / 2.0);
            Color insideChildPill =
                    snapshotNodePixel(image, bottomSheets, bottomSheets.getWidth() - 12.0, bottomSheets.getHeight() / 2.0);
            assertTrue(colorDistance(afterChildPill, insideChildPill) > 0.01);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-drawer-child-selection-rtl.png"
            ));
        });
    }

    /// Verifies that selected child rows mirror their indentation after runtime orientation changes.
    @Test
    void navigationDrawerGroupChildSelectionPillReflowsAfterRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            M3ListItem sideSheets = new M3ListItem("Side sheets");
            group.getItems().addAll(bottomSheets, sideSheets);
            group.setExpanded(true);
            M3NavigationDrawer drawer = navigationDrawer(group);
            drawer.select(bottomSheets);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 260.0);
            root.layout();
            group.layout();
            bottomSheets.layout();

            assertDrawerGroupChildIndentUsesLeftToRightGeometry(group, bottomSheets);

            drawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            group.layout();
            bottomSheets.layout();

            assertDrawerGroupChildIndentUsesRightToLeftGeometry(group, bottomSheets);

            drawer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            root.applyCss();
            root.layout();
            group.layout();
            bottomSheets.layout();

            assertDrawerGroupChildIndentUsesLeftToRightGeometry(group, bottomSheets);
        });
    }

    /// Verifies that navigation drawer groups animate child row expansion and collapse.
    @Tier2Test
    @Test
    void navigationDrawerGroupAnimatesExpansionAndCollapse() throws InterruptedException {
        AtomicReference<@Nullable M3NavigationDrawerGroup> groupReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> collapsedHeightReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> expandedHeightReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> navigationDrawerGroupHeightBetween(
                        groupReference,
                        collapsedHeightReference,
                        expandedHeightReference
                ) && navigationDrawerGroupChildCount(groupReference) == 2,
                () -> {
                    M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
                    M3ListItem buttons = new M3ListItem("Buttons");
                    M3ListItem iconButtons = new M3ListItem("Icon buttons");
                    group.getItems().addAll(buttons, iconButtons);
                    Pane root = new Pane(group);
                    Scene scene = new Scene(root, 280.0, 200.0);

                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    root.applyCss();
                    root.resize(280.0, 200.0);
                    root.layout();

                    double collapsedHeight = group.prefHeight(240.0);
                    group.setAnimationsEnabled(false);
                    group.setExpanded(true);
                    root.applyCss();
                    root.layout();
                    double expandedHeight = group.prefHeight(240.0);
                    group.setExpanded(false);
                    root.applyCss();
                    root.layout();
                    group.setAnimationsEnabled(true);
                    groupReference.set(group);
                    collapsedHeightReference.set(collapsedHeight);
                    expandedHeightReference.set(expandedHeight);
                    group.setExpanded(true);
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());
                    double height = group.prefHeight(240.0);

                    assertTrue(height > collapsedHeightReference.get());
                    assertTrue(height < expandedHeightReference.get());
                    assertEquals(2, group.lookupAll("." + "m3-navigation-drawer-group-child").size());
                }
        );

        FxTestUtils.runOnFxThreadWhen(
                () -> navigationDrawerGroupHeightSettled(groupReference, expandedHeightReference)
                        && navigationDrawerGroupChildCount(groupReference) == 2,
                () -> {
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());

                    assertEquals(expandedHeightReference.get(), group.prefHeight(240.0), 0.5);
                    group.setExpanded(false);
                }
        );

        FxTestUtils.runOnFxThreadWhen(
                () -> navigationDrawerGroupHeightBetween(
                        groupReference,
                        collapsedHeightReference,
                        expandedHeightReference
                ) && navigationDrawerGroupChildCount(groupReference) == 2,
                () -> {
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());
                    double height = group.prefHeight(240.0);

                    assertTrue(height > collapsedHeightReference.get());
                    assertTrue(height < expandedHeightReference.get());
                    assertEquals(2, group.lookupAll("." + "m3-navigation-drawer-group-child").size());
                }
        );

        FxTestUtils.runOnFxThreadWhen(
                () -> navigationDrawerGroupHeightSettled(groupReference, collapsedHeightReference)
                        && navigationDrawerGroupChildCount(groupReference) == 0,
                () -> {
                },
                () -> {
                    M3NavigationDrawerGroup group = Objects.requireNonNull(groupReference.get());

                    assertEquals(collapsedHeightReference.get(), group.prefHeight(240.0), 0.5);
                    assertEquals(0, group.lookupAll("." + "m3-navigation-drawer-group-child").size());
                }
        );
    }

    /// Verifies that navigation drawer token rules override list item metrics.
    @Test
    void navigationDrawerAppliesItemMetrics() {
        M3ListItem home = new M3ListItem("Home");
        M3NavigationDrawer navigationDrawer = navigationDrawer(home);
        navigationDrawer.setStyle("-m3-item-spacing: 6px;");
        Pane root = new Pane(navigationDrawer);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(360.0, navigationDrawer.getPrefWidth(), 0.0001);
        assertEquals(6.0, navigationDrawer.getItemSpacing(), 0.0001);
        assertEquals(56.0, home.getOneLineHeight(), 0.0001);
        assertEquals(999.0, home.getContainerShape(), 0.0001);
        assertEquals(12.0, home.getContentSpacing(), 0.0001);
    }

    /// Verifies standard, modal, and right-to-left navigation drawer surface treatments.
    @Test
    void navigationDrawerResolvesVariantSurfaceAndLogicalCorners() {
        M3ListItem home = new M3ListItem("Home");
        M3NavigationDrawer navigationDrawer = navigationDrawer(home);
        Pane root = new Pane(navigationDrawer);
        Scene scene = new Scene(root, 420.0, 240.0);
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));

        M3ThemeManager.install(scene, theme);
        root.applyCss();

        assertEquals(M3NavigationDrawerVariant.STANDARD, navigationDrawer.getVariant());
        assertTrue(navigationDrawer.getPseudoClassStates().contains(PseudoClass.getPseudoClass("standard")));
        assertFalse(navigationDrawer.getPseudoClassStates().contains(PseudoClass.getPseudoClass("modal")));
        assertRegionFill(
                navigationDrawer,
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE)
        );
        assertNull(navigationDrawer.getEffect());
        assertRegionRadii(navigationDrawer, 0.0, 16.0, 16.0, 0.0);

        navigationDrawer.setVariant(M3NavigationDrawerVariant.MODAL);
        root.applyCss();
        assertFalse(navigationDrawer.getPseudoClassStates().contains(PseudoClass.getPseudoClass("standard")));
        assertTrue(navigationDrawer.getPseudoClassStates().contains(PseudoClass.getPseudoClass("modal")));
        assertRegionFill(
                navigationDrawer,
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SURFACE_CONTAINER_LOW)
        );
        assertNotNull(navigationDrawer.getEffect());

        navigationDrawer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        assertTrue(navigationDrawer.getPseudoClassStates().contains(PseudoClass.getPseudoClass("rtl")));
        assertRegionRadii(navigationDrawer, 16.0, 0.0, 0.0, 16.0);

        navigationDrawer.variantProperty().set(null);
        assertEquals(M3NavigationDrawerVariant.STANDARD, navigationDrawer.getVariant());
    }

    /// Verifies that navigation drawer list items stay within the drawer padding.
    @Test
    void navigationDrawerConstrainsItemWidthToContentArea() {
        M3ListItem home = new M3ListItem("Home");
        M3NavigationDrawer navigationDrawer = navigationDrawer(home);
        navigationDrawer.setStyle("-m3-item-spacing: 6px;");
        Pane root = new Pane(navigationDrawer);
        Scene scene = new Scene(root, 360.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        navigationDrawer.resize(320.0, 120.0);
        navigationDrawer.layout();
        home.layout();

        assertEquals(
                navigationDrawer.localToScene(12.0, 0.0).getX(),
                home.localToScene(0.0, 0.0).getX(),
                0.0001
        );
        assertEquals(296.0, home.getWidth(), 0.0001);
        assertEquals(296.0, listItemContainer(home).getWidth(), 0.0001);
        assertEquals(296.0, listItemSelectionContainer(home).getWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, home.getMinWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, home.getMaxWidth(), 0.0001);

        Color selectedPixel = snapshotPixel(navigationDrawer, 30, 40);
        Color rightPaddingPixel = snapshotPixel(navigationDrawer, 318, 40);
        assertTrue(colorDistance(selectedPixel, rightPaddingPixel) > 0.01);

        Color roundedCornerPixel = snapshotPixel(navigationDrawer, 306, 14);
        assertTrue(colorDistance(selectedPixel, roundedCornerPixel) > 0.01);
    }

    /// Verifies that a constrained drawer owns vertical scrolling and reveals requested destinations.
    @Test
    void navigationDrawerScrollsContentAndRevealsRequestedDestinations() throws InterruptedException {
        AtomicReference<@Nullable DrawerScrollFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhen(() -> {
                @Nullable DrawerScrollFixture fixture = fixtureReference.get();
                return fixture != null && fixture.viewport().getVvalue() > 0.5;
            }, () -> {
                M3ListItem archive = new M3ListItem("Archive");
                M3ListItem drafts = new M3ListItem("Drafts");
                M3ListItem inbox = new M3ListItem("Inbox");
                M3ListItem labels = new M3ListItem("Labels");
                M3ListItem sent = new M3ListItem("Sent");
                M3ListItem settings = new M3ListItem("Settings");
                M3ListItem spam = new M3ListItem("Spam");
                M3ListItem trash = new M3ListItem("Trash");
                M3NavigationDrawer drawer = navigationDrawer(
                        archive,
                        drafts,
                        inbox,
                        labels,
                        sent,
                        settings,
                        spam,
                        trash
                );
                drawer.setMinHeight(180.0);
                drawer.setPrefHeight(180.0);
                drawer.setMaxHeight(180.0);
                assertNull(drawer.getSkin());
                drawer.scrollTo(trash);
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 360.0, 180.0);
                Stage stage = new Stage();
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(0.0, 0.0, 360.0, 180.0);
                root.layout();

                ScrollPane viewport = assertInstanceOf(
                        ScrollPane.class,
                        drawer.lookup(".m3-navigation-drawer-viewport")
                );
                viewport.applyCss();
                viewport.layout();
                assertInstanceOf(Parent.class, viewport.getContent()).layout();
                viewport.layout();
                assertSame(viewport.getContent(), archive.getParent());
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER, viewport.getHbarPolicy());
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER, viewport.getVbarPolicy());
                assertTrue(viewport.isFitToWidth());
                assertTrue(viewport.isPannable());
                assertTrue(viewport.getContent().getLayoutBounds().getHeight()
                                > viewport.getViewportBounds().getHeight(),
                        () -> "content=" + viewport.getContent().getLayoutBounds()
                                + ", viewport=" + viewport.getViewportBounds());
                assertTrue(M3ScrollPane.isSmoothScrollingEnabled(viewport));
                fixtureReference.set(new DrawerScrollFixture(stage, root, drawer, trash, viewport));
            }, () -> {
                DrawerScrollFixture fixture = Objects.requireNonNull(fixtureReference.get());
                Pane root = fixture.root();
                M3NavigationDrawer drawer = fixture.drawer();
                M3ListItem trash = fixture.trash();
                ScrollPane viewport = fixture.viewport();
                assertTrue(
                        viewport.getVvalue() > 0.5,
                        () -> "a pre-skin scroll request should be retained: vvalue=" + viewport.getVvalue()
                );

                viewport.setVvalue(0.0);
                drawer.scrollTo(trash);
                root.layout();
                assertFalse(trash.isFocused());
                assertTrue(viewport.getVvalue() > 0.5, () -> "vvalue=" + viewport.getVvalue());

                drawer.setAnimationsEnabled(false);
                viewport.setVvalue(0.0);
                ScrollEvent event = scrollEvent(viewport, 0.0, -80.0);
                viewport.fireEvent(event);
                root.layout();
                assertTrue(event.isConsumed());
                assertTrue(viewport.getVvalue() > 0.0, () -> "vvalue=" + viewport.getVvalue());
                assertEquals(0.0, viewport.getHvalue(), 0.0001);

                viewport.setVvalue(0.0);
                drawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 7);
                root.layout();
                assertTrue(trash.isFocused());
                assertTrue(viewport.getVvalue() > 0.5, () -> "vvalue=" + viewport.getVvalue());
                drawer.setAnimationsEnabled(true);
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable DrawerScrollFixture fixture = fixtureReference.get();
                if (fixture != null) {
                    fixture.stage().close();
                }
            });
        }
    }

    /// Verifies navigation drawer typography emphasis and interaction colors against the baseline component tokens.
    @Test
    void navigationDrawerResolvesDestinationTypographyAndStateColors() {
        M3ListItem home = new M3ListItem("Home");
        M3ListItem archive = new M3ListItem("Archive");
        M3NavigationDrawer drawer = navigationDrawer(home, archive);
        Pane root = new Pane(drawer);
        Scene scene = new Scene(root, 360.0, 160.0);
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));

        M3ThemeManager.install(scene, theme);
        FxTestUtils.setCssAnimationsEnabled(root, false);
        try {
            root.applyCss();
            drawer.resize(360.0, 160.0);
            drawer.layout();
            root.applyCss();
            drawer.layout();

            Label archiveLabel = listItemHeadlineLabel(archive);
            Region archiveStateLayer = lookupRegion(archive, ".m3-state-layer");
            assertTrue(archiveLabel.getStyleClass().contains("m3-label-large-text"));
            assertFalse(archiveLabel.getStyleClass().contains("m3-body-large-text"));
            assertFalse(archiveLabel.getStyleClass().contains("m3-prominent-text"));
            assertEquals(14.0, archiveLabel.getFont().getSize(), 0.0001);
            assertFalse(archiveLabel.getFont().getStyle().contains("Bold"),
                    () -> "unselected drawer label style=" + archiveLabel.getFont().getStyle());
            assertEquals(
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE_VARIANT),
                    archiveLabel.getTextFill()
            );

            archive.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            root.applyCss();
            assertEquals(
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE),
                    archiveLabel.getTextFill()
            );
            archive.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
            archive.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
            root.applyCss();
            archive.fireEvent(primaryMouseEvent(
                    archive,
                    MouseEvent.MOUSE_PRESSED,
                    archive.getWidth() / 2.0,
                    archive.getHeight() / 2.0,
                    true
            ));
            Region archiveRipple = lookupRegion(archive, ".m3-ripple");
            assertEquals(
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE),
                    archiveLabel.getTextFill()
            );
            assertRegionFill(
                    archiveStateLayer,
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SECONDARY_CONTAINER)
            );
            assertRegionFill(
                    archiveRipple,
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SECONDARY_CONTAINER)
            );
            archive.fireEvent(primaryMouseEvent(
                    archive,
                    MouseEvent.MOUSE_RELEASED,
                    archive.getWidth() / 2.0,
                    archive.getHeight() / 2.0,
                    false
            ));

            archive.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false);
            drawer.select(archive);
            root.applyCss();
            assertEquals(
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SECONDARY_CONTAINER),
                    archiveLabel.getTextFill()
            );
            assertTrue(archiveLabel.getFont().getStyle().contains("Bold"),
                    () -> "selected drawer label style=" + archiveLabel.getFont().getStyle());
            assertFalse(archiveLabel.getStyleClass().contains("m3-prominent-text"));

            drawer.getItems().remove(archive);

            assertTrue(archiveLabel.getStyleClass().contains("m3-body-large-text"));
            assertFalse(archiveLabel.getStyleClass().contains("m3-label-large-text"));
            assertFalse(archiveLabel.getStyleClass().contains("m3-prominent-text"));
        } finally {
            FxTestUtils.setCssAnimationsEnabled(root, true);
        }
    }

    /// Verifies that a selected child gives its group header hover-level feedback without selected typography.
    @Test
    void navigationDrawerGroupHeaderReflectsSelectedChildWithoutTypographyEmphasis() {
        M3ListItem overview = new M3ListItem("Overview");
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
        M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
        group.getItems().add(bottomSheets);
        group.setExpanded(true);
        M3NavigationDrawer drawer = navigationDrawer(overview, group);
        Pane root = new Pane(drawer);
        Scene scene = new Scene(root, 360.0, 180.0);
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));
        PseudoClass childSelected = PseudoClass.getPseudoClass("child-selected");

        M3ThemeManager.install(scene, theme);
        FxTestUtils.setCssAnimationsEnabled(root, false);
        try {
            root.applyCss();
            drawer.resize(360.0, 180.0);
            drawer.layout();

            M3ListItem header = group.getHeaderItem();
            Label headline = listItemHeadlineLabel(header);
            Region stateLayer = lookupRegion(header, ".m3-state-layer");
            assertFalse(group.isAnimationsEnabled(), "group inherits the disabled-motion scope");
            assertFalse(header.isAnimationsEnabled(), "header follows its group motion setting");
            var restingFont = headline.getFont();
            assertFalse(header.getPseudoClassStates().contains(childSelected));
            assertEquals(0.0, stateLayer.getOpacity(), 0.0001);

            drawer.select(bottomSheets);
            root.applyCss();

            assertTrue(header.getPseudoClassStates().contains(childSelected));
            assertFalse(header.isSelected());
            assertEquals(theme.tokens().stateLayerTokens().hoverOpacity(), stateLayer.getOpacity(), 0.0001);
            assertRegionFill(stateLayer, theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE));
            assertEquals(
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE),
                    headline.getTextFill()
            );
            assertEquals(restingFont, headline.getFont());
            assertFalse(headline.getFont().getStyle().contains("Bold"),
                    () -> "parent-of-selection drawer label style=" + headline.getFont().getStyle());

            drawer.select(overview);
            root.applyCss();

            assertFalse(header.getPseudoClassStates().contains(childSelected));
            assertEquals(0.0, stateLayer.getOpacity(), 0.0001);
            assertEquals(restingFont, headline.getFont());
        } finally {
            FxTestUtils.setCssAnimationsEnabled(root, true);
        }
    }

    /// Verifies that selected drawer typography changes before the selected-container animation settles.
    @Tier2Test
    @Test
    void navigationDrawerAppliesSelectedWeightImmediately() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> archiveReference = new AtomicReference<>();
        AtomicReference<@Nullable Label> headlineReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> selectionContainerReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable Region selectionContainer = selectionContainerReference.get();
                        if (selectionContainer == null) {
                            return false;
                        }
                        double opacity = selectionContainer.getOpacity();
                        return opacity > 0.05 && opacity < 0.95;
                    },
                    () -> {
                        M3ListItem home = new M3ListItem("Home");
                        M3ListItem archive = new M3ListItem("Archive");
                        M3NavigationDrawer drawer = navigationDrawer(home, archive);
                        Pane root = new Pane(drawer);
                        Scene scene = new Scene(root, 360.0, 160.0);
                        Stage stage = new Stage();

                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setCssAnimationsEnabled(root, true);
                        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        drawer.resize(360.0, 160.0);
                        root.layout();

                        Label headline = listItemHeadlineLabel(archive);
                        assertFalse(headline.getFont().getStyle().contains("Bold"),
                                () -> "unselected drawer label style=" + headline.getFont().getStyle());

                        stageReference.set(stage);
                        rootReference.set(root);
                        archiveReference.set(archive);
                        headlineReference.set(headline);
                        selectionContainerReference.set(
                                lookupRegion(archive, ".m3-list-item-selection-container")
                        );
                        drawer.select(archive);
                    },
                    () -> {
                        Pane root = Objects.requireNonNull(rootReference.get(), "root");
                        M3ListItem archive = Objects.requireNonNull(archiveReference.get(), "archive");
                        Label headline = Objects.requireNonNull(headlineReference.get(), "headline");
                        Region selectionContainer = Objects.requireNonNull(
                                selectionContainerReference.get(),
                                "selection container"
                        );
                        root.applyCss();
                        assertBetween(selectionContainer.getOpacity(), 0.0, 1.0, "selection container opacity");
                        assertTrue(headline.getFont().getStyle().contains("Bold"),
                                () -> "intermediate selected drawer label style=" + headline.getFont().getStyle());
                        assertNull(archive.lookup(".m3-list-item-headline-emphasis"));
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
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
    }

    /// Verifies that navigation drawer skins preserve application-owned child width constraints.
    @Test
    void navigationDrawerSkinsPreserveApplicationOwnedChildWidths() {
        M3ListItem topLevel = new M3ListItem("Home");
        SimpleDoubleProperty topMinWidth = new SimpleDoubleProperty(32.0);
        SimpleDoubleProperty topMaxWidth = new SimpleDoubleProperty(220.0);
        topLevel.minWidthProperty().bind(topMinWidth);
        topLevel.maxWidthProperty().bind(topMaxWidth);

        M3ListItem unboundTopLevel = new M3ListItem("Archive");
        unboundTopLevel.setMinWidth(34.0);
        unboundTopLevel.setMaxWidth(218.0);

        M3ListItem child = new M3ListItem("Bottom sheets");
        SimpleDoubleProperty childMinWidth = new SimpleDoubleProperty(28.0);
        SimpleDoubleProperty childMaxWidth = new SimpleDoubleProperty(180.0);
        child.minWidthProperty().bind(childMinWidth);
        child.maxWidthProperty().bind(childMaxWidth);

        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
        group.setExpanded(true);
        group.getItems().add(child);

        M3NavigationDrawer navigationDrawer = navigationDrawer(topLevel, unboundTopLevel, group);
        Pane root = new Pane(navigationDrawer);
        Scene scene = new Scene(root, 360.0, 240.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        navigationDrawer.resize(320.0, 240.0);
        navigationDrawer.layout();
        group.layout();

        assertEquals(32.0, topLevel.getMinWidth(), 0.0001);
        assertEquals(220.0, topLevel.getMaxWidth(), 0.0001);
        assertEquals(34.0, unboundTopLevel.getMinWidth(), 0.0001);
        assertEquals(218.0, unboundTopLevel.getMaxWidth(), 0.0001);
        assertEquals(28.0, child.getMinWidth(), 0.0001);
        assertEquals(180.0, child.getMaxWidth(), 0.0001);

        topMinWidth.set(36.0);
        topMaxWidth.set(224.0);
        childMinWidth.set(30.0);
        childMaxWidth.set(184.0);
        navigationDrawer.layout();
        group.layout();

        assertEquals(36.0, topLevel.getMinWidth(), 0.0001);
        assertEquals(224.0, topLevel.getMaxWidth(), 0.0001);
        assertEquals(34.0, unboundTopLevel.getMinWidth(), 0.0001);
        assertEquals(218.0, unboundTopLevel.getMaxWidth(), 0.0001);
        assertEquals(30.0, child.getMinWidth(), 0.0001);
        assertEquals(184.0, child.getMaxWidth(), 0.0001);

        FxTestUtils.replaceSkin(navigationDrawer, M3NavigationDrawerSkin::new);
        root.applyCss();
        navigationDrawer.layout();
        assertEquals(36.0, topLevel.getMinWidth(), 0.0001);
        assertEquals(224.0, topLevel.getMaxWidth(), 0.0001);
        assertEquals(34.0, unboundTopLevel.getMinWidth(), 0.0001);
        assertEquals(218.0, unboundTopLevel.getMaxWidth(), 0.0001);

        navigationDrawer.getItems().remove(unboundTopLevel);
        assertNull(unboundTopLevel.getParent());
        assertEquals(34.0, unboundTopLevel.getMinWidth(), 0.0001);
        assertEquals(218.0, unboundTopLevel.getMaxWidth(), 0.0001);

        navigationDrawer.getSkin().dispose();
        assertNull(topLevel.getParent());
        assertNull(group.getParent());
    }

    /// Verifies that expanded navigation drawer groups render header and child rows.
    @Tier2Test
    @Test
    void navigationDrawerGroupSnapshotRendersExpandedRows() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem overview = new M3ListItem("Components overview");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem iconButtons = new M3ListItem("Icon buttons");
            group.getItems().addAll(buttons, iconButtons);
            group.setExpanded(true);
            M3NavigationDrawer drawer = navigationDrawer(overview, group);
            drawer.select(buttons);
            drawer.setPrefWidth(320.0);

            StackPane root = new StackPane(drawer);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 380.0, 300.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(380.0, 300.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertEquals(2, group.lookupAll("." + "m3-navigation-drawer-group-child").size());
            assertEquals(56.0, buttons.getOneLineHeight(), 0.0001);
            assertEquals(32.0, buttons.getHorizontalPadding(), 0.0001);
            assertTrue(assertInstanceOf(M3DisclosureIcon.class, group.getHeaderItem().getTrailing()).isExpanded());
            assertSnapshotNodeContainsContrast(image, drawer, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, listItemContainer(buttons), Color.WHITE, 0.05);
            assertSnapshotHasColorVariety(image, 8);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-navigation-drawer-group.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Returns whether a navigation drawer group has settled to the expected preferred height.
    private static boolean navigationDrawerGroupHeightSettled(
            AtomicReference<@Nullable M3NavigationDrawerGroup> groupReference,
            AtomicReference<@Nullable Double> expectedHeightReference
    ) {
        @Nullable M3NavigationDrawerGroup group = groupReference.get();
        @Nullable Double expectedHeight = expectedHeightReference.get();
        return group != null
                && expectedHeight != null
                && Math.abs(group.prefHeight(240.0) - expectedHeight) <= 0.5;
    }

    /// Returns whether a navigation drawer group is between collapsed and expanded preferred heights.
    private static boolean navigationDrawerGroupHeightBetween(
            AtomicReference<@Nullable M3NavigationDrawerGroup> groupReference,
            AtomicReference<@Nullable Double> collapsedHeightReference,
            AtomicReference<@Nullable Double> expandedHeightReference
    ) {
        @Nullable M3NavigationDrawerGroup group = groupReference.get();
        @Nullable Double collapsedHeight = collapsedHeightReference.get();
        @Nullable Double expandedHeight = expandedHeightReference.get();
        if (group == null || collapsedHeight == null || expandedHeight == null) {
            return false;
        }

        double height = group.prefHeight(240.0);
        return height > collapsedHeight + 0.5 && height < expandedHeight - 0.5;
    }

    /// Returns the currently rendered child row count for a navigation drawer group.
    private static int navigationDrawerGroupChildCount(
            AtomicReference<@Nullable M3NavigationDrawerGroup> groupReference
    ) {
        @Nullable M3NavigationDrawerGroup group = groupReference.get();
        return group == null
                ? -1
                : group.lookupAll("." + "m3-navigation-drawer-group-child").size();
    }

    /// Returns whether a drawer mouse selection is rendering both ripple and selected-container motion.
    private static boolean drawerMouseSelectionIsTransitioning(
            AtomicReference<@Nullable Parent> rootReference,
            AtomicReference<@Nullable M3ListItem> itemReference
    ) {
        @Nullable M3ListItem item = itemReference.get();
        if (item == null || observableRootUnavailable(rootReference)) {
            return false;
        }

        Region selection = listItemSelectionContainer(item);
        Region ripple = lookupRegion(item, ".m3-ripple");
        return item.isSelected()
                && ripple.getOpacity() > 0.0
                && isBetweenExclusive(selection.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(selection.getScaleX(), 0.96, 1.0)
                && isBetweenExclusive(selection.getScaleY(), 0.96, 1.0);
    }

    /// Verifies that a drawer group child row uses left-to-right selected pill indentation.
    private static void assertDrawerGroupChildIndentUsesLeftToRightGeometry(
            M3NavigationDrawerGroup group,
            M3ListItem child
    ) {
        Bounds headerBounds = group.getHeaderItem().localToScene(group.getHeaderItem().getBoundsInLocal());
        Bounds childBounds = child.localToScene(child.getBoundsInLocal());
        Region childSelection = listItemSelectionContainer(child);

        assertEquals(headerBounds.getMaxX(), childBounds.getMaxX(), 0.0001,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertTrue(childBounds.getMinX() > headerBounds.getMinX() + 6.0,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertEquals(headerBounds.getHeight(), childBounds.getHeight(), 0.0001,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertEquals(childBounds.getWidth(), childSelection.getWidth(), 0.0001,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertEquals(56.0, child.getOneLineHeight(), 0.0001);
        assertRegionRadii(childSelection, 28.0, 28.0, 28.0, 28.0);
    }

    /// Verifies that a drawer group child row uses right-to-left selected pill indentation.
    private static void assertDrawerGroupChildIndentUsesRightToLeftGeometry(
            M3NavigationDrawerGroup group,
            M3ListItem child
    ) {
        Bounds headerBounds = group.getHeaderItem().localToScene(group.getHeaderItem().getBoundsInLocal());
        Bounds childBounds = child.localToScene(child.getBoundsInLocal());
        Region childSelection = listItemSelectionContainer(child);

        assertEquals(headerBounds.getMinX(), childBounds.getMinX(), 0.0001,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertTrue(childBounds.getMaxX() < headerBounds.getMaxX() - 6.0,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertEquals(headerBounds.getHeight(), childBounds.getHeight(), 0.0001,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertEquals(childBounds.getWidth(), childSelection.getWidth(), 0.0001,
                () -> describeDrawerGroupChildGeometry(headerBounds, childBounds, childSelection));
        assertEquals(56.0, child.getOneLineHeight(), 0.0001);
        assertRegionRadii(childSelection, 28.0, 28.0, 28.0, 28.0);
    }

    /// Describes drawer group child indentation geometry for assertion failures.
    private static String describeDrawerGroupChildGeometry(
            Bounds headerBounds,
            Bounds childBounds,
            Region childSelection
    ) {
        return "headerBounds=" + headerBounds
                + ", childBounds=" + childBounds
                + ", childSelectionBounds=" + childSelection.getBoundsInParent()
                + ", childSelectionWidth=" + childSelection.getWidth();
    }

    /// Retains the nodes used while a drawer reveal waits for a real layout pulse.
    ///
    /// @param stage the stage owning the rendered drawer
    /// @param root the scene root
    /// @param drawer the drawer under test
    /// @param trash the destination requested before skin creation
    /// @param viewport the drawer-owned scroll viewport
    @NotNullByDefault
    private record DrawerScrollFixture(
            Stage stage,
            Pane root,
            M3NavigationDrawer drawer,
            M3ListItem trash,
            ScrollPane viewport
    ) {
    }
}
