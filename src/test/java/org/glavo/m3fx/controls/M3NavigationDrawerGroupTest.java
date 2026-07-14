// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.skins.M3NavigationDrawerGroupSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.glavo.m3fx.M3TestControls.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies navigation drawer group layout behavior.
@NotNullByDefault
final class M3NavigationDrawerGroupTest {
    /// The width used for drawer group preferred-height measurement.
    private static final double GROUP_WIDTH = 320.0;

    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies drawer disclosure keys do not steal focus from an embedded text input.
    @Test
    void embeddedTextInputKeepsDrawerDisclosureKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField editor = new M3TextField("Edit");
            M3ListItem child = new M3ListItem("Editable child");
            child.setTrailingMedia(editor, M3ListItemSlotSize.WIDE_THUMBNAIL);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
            group.getItems().add(child);
            group.setExpanded(true);
            M3NavigationDrawer drawer = navigationDrawer(group);
            M3MotionSettings.setReducedMotionRequested(drawer, true);

            StackPane root = new StackPane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                drawer.select(group.getHeaderItem());
                editor.requestFocus();
                KeyEvent collapseEvent = keyPressed(KeyCode.LEFT);
                drawer.fireEvent(collapseEvent);

                assertTrue(group.isExpanded());
                assertTrue(editor.isFocused(), () -> "focused=" + scene.getFocusOwner());
            } finally {
                stage.close();
                M3MotionSettings.setReducedMotionRequested(drawer, false);
            }
        });
    }

    /// Verifies modified drawer disclosure keys are left to application shortcuts.
    @Test
    void modifiedDrawerDisclosureKeysAreIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
            M3ListItem child = new M3ListItem("Child");
            group.getItems().add(child);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            M3MotionSettings.setReducedMotionRequested(drawer, true);

            StackPane root = new StackPane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                drawer.select(group.getHeaderItem());
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        KeyCode.RIGHT.getName(),
                        KeyCode.RIGHT.getName(),
                        KeyCode.RIGHT,
                        false,
                        true,
                        false,
                        false
                );
                drawer.fireEvent(event);

                assertFalse(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertFalse(event.isConsumed());
            } finally {
                stage.close();
                M3MotionSettings.setReducedMotionRequested(drawer, false);
            }
        });
    }

    /// Verifies drawer type-ahead does not steal printable keys from an embedded text input.
    @Test
    void embeddedTextInputKeepsDrawerTypeAheadKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField editor = new M3TextField("Edit");
            M3ListItem editable = new M3ListItem("Editable");
            editable.setTrailingMedia(editor, M3ListItemSlotSize.WIDE_THUMBNAIL);
            M3ListItem archive = new M3ListItem("Archive");
            M3NavigationDrawer drawer = navigationDrawer(editable, archive);
            M3MotionSettings.setReducedMotionRequested(drawer, true);

            StackPane root = new StackPane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                drawer.select(editable);
                editor.requestFocus();
                KeyEvent typedEvent = new KeyEvent(
                        KeyEvent.KEY_TYPED,
                        "a",
                        "a",
                        KeyCode.UNDEFINED,
                        false,
                        false,
                        false,
                        false
                );
                drawer.fireEvent(typedEvent);

                assertSame(editable, drawer.getSelectedItem());
                assertTrue(editor.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertFalse(typedEvent.isConsumed());
            } finally {
                stage.close();
                M3MotionSettings.setReducedMotionRequested(drawer, false);
            }
        });
    }

    /// Verifies that the child item list rejects null mutations without partial insertion.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void childItemListRejectsNullElements() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
        M3ListItem child = new M3ListItem("Child");

        assertThrows(NullPointerException.class, () -> group.getItems().add(null));
        assertThrows(NullPointerException.class, () -> group.getItems().addAll(child, null));
        assertTrue(group.getItems().isEmpty());
    }

    /// Verifies that the decorative disclosure icon leaves the complete header row as the pointer target.
    @Test
    void disclosureIconDelegatesPointerPickingToHeader() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Buttons");
            StackPane root = new StackPane(group);
            Scene scene = new Scene(root, GROUP_WIDTH, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            Node disclosureIcon = group.lookup(".m3-disclosure-icon");
            assertTrue(disclosureIcon != null && disclosureIcon.isMouseTransparent());
        });
    }

    /// Verifies expanded child rows contribute to preferred height for scroll pane content sizing.
    @Test
    void expandedChildRowsContributeToPreferredHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            group.getItems().addAll(new M3ListItem("Bottom sheets"), new M3ListItem("Side sheets"));
            M3MotionSettings.setReducedMotionRequested(group, true);

            StackPane root = new StackPane(group);
            Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            try {
                double collapsedHeight = group.prefHeight(GROUP_WIDTH);

                group.setExpanded(true);
                root.applyCss();
                root.layout();

                double expandedHeight = group.prefHeight(GROUP_WIDTH);
                assertTrue(expandedHeight > collapsedHeight, () -> "collapsed=" + collapsedHeight
                        + ", expanded=" + expandedHeight);
                assertEquals(2, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());

                group.setSkin(new M3NavigationDrawerGroupSkin(group));
                root.applyCss();
                root.layout();

                assertSame(scene, group.getHeaderItem().getScene());
                assertEquals(2, group.lookupAll("." + M3NavigationDrawerGroup.CHILD_STYLE_CLASS).size());
            } finally {
                M3MotionSettings.setReducedMotionRequested(group, false);
            }
        });
    }

    /// Verifies that the skin inherits layout direction without rewriting application-owned row properties.
    @Test
    void skinPreservesApplicationOwnedRowProperties() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem inheritedChild = new M3ListItem("Inherited");
            inheritedChild.setMinWidth(40.0);
            inheritedChild.setMaxWidth(180.0);

            SimpleObjectProperty<NodeOrientation> boundOrientation =
                    new SimpleObjectProperty<>(NodeOrientation.RIGHT_TO_LEFT);
            M3ListItem boundChild = new M3ListItem("Bound");
            boundChild.nodeOrientationProperty().bind(boundOrientation);

            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
            group.getHeaderItem().setMinWidth(48.0);
            group.getHeaderItem().setMaxWidth(240.0);
            group.getItems().addAll(inheritedChild, boundChild);
            group.setExpanded(true);
            group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3MotionSettings.setReducedMotionRequested(group, true);

            StackPane root = new StackPane(group);
            Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            try {
                root.applyCss();
                root.layout();

                assertSame(NodeOrientation.INHERIT, inheritedChild.getNodeOrientation());
                assertSame(NodeOrientation.RIGHT_TO_LEFT, inheritedChild.getEffectiveNodeOrientation());
                assertSame(NodeOrientation.RIGHT_TO_LEFT, boundChild.getNodeOrientation());
                assertTrue(boundChild.nodeOrientationProperty().isBound());
                assertEquals(40.0, inheritedChild.getMinWidth(), 0.0001);
                assertEquals(180.0, inheritedChild.getMaxWidth(), 0.0001);
                assertEquals(48.0, group.getHeaderItem().getMinWidth(), 0.0001);
                assertEquals(240.0, group.getHeaderItem().getMaxWidth(), 0.0001);

                group.getItems().remove(inheritedChild);
                assertSame(NodeOrientation.INHERIT, inheritedChild.getNodeOrientation());
                assertNull(inheritedChild.getParent());
                assertEquals(40.0, inheritedChild.getMinWidth(), 0.0001);
                assertEquals(180.0, inheritedChild.getMaxWidth(), 0.0001);

                group.getItems().add(inheritedChild);
                group.setSkin(new M3NavigationDrawerGroupSkin(group));
                root.applyCss();
                root.layout();

                assertSame(NodeOrientation.INHERIT, inheritedChild.getNodeOrientation());
                assertSame(NodeOrientation.RIGHT_TO_LEFT, inheritedChild.getEffectiveNodeOrientation());
                assertEquals(40.0, inheritedChild.getMinWidth(), 0.0001);
                assertEquals(180.0, inheritedChild.getMaxWidth(), 0.0001);

                boundOrientation.set(NodeOrientation.LEFT_TO_RIGHT);
                root.layout();
                assertSame(NodeOrientation.LEFT_TO_RIGHT, boundChild.getNodeOrientation());
                assertTrue(boundChild.nodeOrientationProperty().isBound());
            } finally {
                boundChild.nodeOrientationProperty().unbind();
                M3MotionSettings.setReducedMotionRequested(group, false);
            }
        });
    }

    /// Verifies collapsing a focused drawer child reports the group header as the active focus node.
    @Test
    void collapsingFocusedChildReportsHeaderFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem child = new M3ListItem("Invoices");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Workspaces");
            group.getItems().add(child);
            group.setExpanded(true);
            M3NavigationDrawer drawer = navigationDrawer(group);
            M3MotionSettings.setReducedMotionRequested(drawer, true);

            StackPane root = new StackPane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                drawer.select(child);
                child.requestFocus();
                assertTrue(child.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(child, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                KeyEvent collapseEvent = keyPressed(KeyCode.LEFT);
                drawer.fireEvent(collapseEvent);

                assertFalse(group.isExpanded());
                assertSame(group.getHeaderItem(), drawer.getSelectedItem());
                assertTrue(group.getHeaderItem().isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(group.getHeaderItem(), drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
                M3MotionSettings.setReducedMotionRequested(drawer, false);
            }
        });
    }

    /// Verifies accessibility focus reports failure for an empty drawer.
    @Test
    void emptyDrawerAccessibleFocusReportsFailure() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawer drawer = new M3NavigationDrawer();
            StackPane root = new StackPane(drawer);
            Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            assertFalse(M3Accessible.requestAccessibleFocus(drawer));
            assertFalse(drawer.isFocused());
            assertNull(drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        });
    }

    /// Verifies accessibility focus moves to the selected visible drawer item.
    @Test
    void selectedDrawerItemAcceptsAccessibleFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem inbox = new M3ListItem("Inbox");
            M3ListItem archive = new M3ListItem("Archive");
            M3NavigationDrawer drawer = navigationDrawer(inbox, archive);
            StackPane root = new StackPane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                drawer.select(archive);

                assertTrue(M3Accessible.requestAccessibleFocus(drawer));
                assertTrue(archive.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(archive, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies hidden collapsed-group descendants do not expand groups through reveal or selection actions.
    @Test
    void collapsedDrawerGroupRejectsHiddenDescendantTargetsBeforeExpanding() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button hiddenAction = new M3Button("Hidden action");
            hiddenAction.setVisible(false);
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            bottomSheets.setTrailing(hiddenAction);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            group.getItems().add(bottomSheets);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            M3MotionSettings.setReducedMotionRequested(drawer, true);

            StackPane root = new StackPane(drawer);
            Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            try {
                root.applyCss();
                root.layout();
                drawer.clearSelection();
                M3ListItem baselineSelection = drawer.getSelectedItem();
                Node baselineFocusOwner = scene.getFocusOwner();

                assertFalse(M3Accessible.showAccessibleActionTarget(group, hiddenAction));
                assertFalse(group.isExpanded());
                assertFalse(hiddenAction.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(baselineSelection, drawer.getSelectedItem());
                assertSame(baselineFocusOwner, scene.getFocusOwner());

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenAction);

                assertFalse(group.isExpanded());
                assertFalse(hiddenAction.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(baselineSelection, drawer.getSelectedItem());
                assertSame(baselineFocusOwner, scene.getFocusOwner());

                assertFalse(M3Accessible.showAccessibleActionTarget(drawer, hiddenAction));
                assertFalse(group.isExpanded());
                assertFalse(hiddenAction.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(baselineSelection, drawer.getSelectedItem());
                assertSame(baselineFocusOwner, scene.getFocusOwner());

                drawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenAction);

                assertFalse(group.isExpanded());
                assertFalse(hiddenAction.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(baselineSelection, drawer.getSelectedItem());
                assertSame(baselineFocusOwner, scene.getFocusOwner());

                drawer.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, hiddenAction);

                assertFalse(group.isExpanded());
                assertSame(baselineSelection, drawer.getSelectedItem());
                assertSame(baselineFocusOwner, scene.getFocusOwner());
            } finally {
                M3MotionSettings.setReducedMotionRequested(drawer, false);
            }
        });
    }

    /// Verifies accessibility reveal expands a collapsed group and focuses the requested child item.
    @Test
    void collapsedDrawerGroupChildAcceptsAccessibleReveal() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            group.getItems().add(bottomSheets);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            M3MotionSettings.setReducedMotionRequested(drawer, true);

            StackPane root = new StackPane(drawer);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, GROUP_WIDTH, 240.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertTrue(M3Accessible.showAccessibleActionTarget(drawer, bottomSheets));

                assertTrue(group.isExpanded());
                assertTrue(bottomSheets.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertSame(bottomSheets, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
                M3MotionSettings.setReducedMotionRequested(drawer, false);
            }
        });
    }

    /// Creates a pressed key event for drawer keyboard handling tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                code.getName(),
                code.getName(),
                code,
                false,
                false,
                false,
                false
        );
    }


}
