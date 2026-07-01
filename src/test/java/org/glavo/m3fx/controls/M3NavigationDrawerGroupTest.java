// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleAttribute;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for navigation drawer group layout behavior.
@NotNullByDefault
final class M3NavigationDrawerGroupTest {
    /// The width used for drawer group preferred-height measurement.
    private static final double GROUP_WIDTH = 320.0;

    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies drawer disclosure keys do not steal focus from an embedded text input.
    @Test
    void embeddedTextInputKeepsDrawerDisclosureKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField editor = new M3TextField("Edit");
            M3ListItem child = new M3ListItem("Editable child");
            child.setTrailingMedia(editor, M3ListItemSlotSize.WIDE_THUMBNAIL);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
            group.addItem(child);
            group.setExpanded(true);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
            M3MotionSettings.setAnimationsEnabled(drawer, false);

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
                M3MotionSettings.clearAnimationsEnabled(drawer);
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
            M3NavigationDrawer drawer = new M3NavigationDrawer(editable, archive);
            M3MotionSettings.setAnimationsEnabled(drawer, false);

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
                KeyEvent typedEvent = keyTyped("a");
                drawer.fireEvent(typedEvent);

                assertSame(editable, drawer.getSelectedItem());
                assertTrue(editor.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertFalse(typedEvent.isConsumed());
            } finally {
                stage.close();
                M3MotionSettings.clearAnimationsEnabled(drawer);
            }
        });
    }

    /// Verifies expanded child rows contribute to preferred height for scroll pane content sizing.
    @Test
    void expandedChildRowsContributeToPreferredHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            group.addItems(new M3ListItem("Bottom sheets"), new M3ListItem("Side sheets"));
            M3MotionSettings.setAnimationsEnabled(group, false);

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
            } finally {
                M3MotionSettings.clearAnimationsEnabled(group);
            }
        });
    }

    /// Verifies collapsing a focused drawer child reports the group header as the active focus node.
    @Test
    void collapsingFocusedChildReportsHeaderFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem child = new M3ListItem("Invoices");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Workspaces");
            group.addItem(child);
            group.setExpanded(true);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
            M3MotionSettings.setAnimationsEnabled(drawer, false);

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
                M3MotionSettings.clearAnimationsEnabled(drawer);
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
            M3NavigationDrawer drawer = new M3NavigationDrawer(inbox, archive);
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

    /// Verifies accessibility reveal expands a collapsed group and focuses the requested child item.
    @Test
    void collapsedDrawerGroupChildAcceptsAccessibleReveal() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem bottomSheets = new M3ListItem("Bottom sheets");
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Sheets");
            group.addItem(bottomSheets);
            group.setExpanded(false);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
            M3MotionSettings.setAnimationsEnabled(drawer, false);

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
                M3MotionSettings.clearAnimationsEnabled(drawer);
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

    /// Creates a typed key event for drawer type-ahead tests.
    private static KeyEvent keyTyped(String character) {
        return new KeyEvent(
                KeyEvent.KEY_TYPED,
                character,
                character,
                KeyCode.UNDEFINED,
                false,
                false,
                false,
                false
        );
    }
}
