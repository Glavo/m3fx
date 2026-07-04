// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAction;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies shared child selection navigation behavior.
@NotNullByDefault
final class M3SelectionNavigationTest {
    /// Starts the JavaFX toolkit for Material control construction.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real windows created by selection navigation reveal tests.
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

    /// Verifies shared selection navigation mirroring for right-to-left horizontal movement.
    @Test
    void mirrorsAnchoredHorizontalKeysForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            HBox owner = new HBox(first, second, third);
            AtomicReference<@Nullable M3Button> selected = new AtomicReference<>();

            KeyEvent leftToRightForward = keyEvent(KeyCode.RIGHT);
            assertTrue(M3SelectionNavigation.handleKeySelection(
                    leftToRightForward,
                    owner.getChildren(),
                    second,
                    M3Button.class,
                    true,
                    false,
                    false,
                    selected::set
            ));
            assertSame(third, selected.get());
            assertTrue(leftToRightForward.isConsumed());

            KeyEvent leftToRightBackward = keyEvent(KeyCode.LEFT);
            assertTrue(M3SelectionNavigation.handleKeySelection(
                    leftToRightBackward,
                    owner.getChildren(),
                    second,
                    M3Button.class,
                    true,
                    false,
                    false,
                    selected::set
            ));
            assertSame(first, selected.get());
            assertTrue(leftToRightBackward.isConsumed());

            KeyEvent rightToLeftForward = keyEvent(KeyCode.LEFT);
            assertTrue(M3SelectionNavigation.handleKeySelection(
                    rightToLeftForward,
                    owner.getChildren(),
                    second,
                    M3Button.class,
                    true,
                    false,
                    true,
                    selected::set
            ));
            assertSame(third, selected.get());
            assertTrue(rightToLeftForward.isConsumed());

            KeyEvent rightToLeftBackward = keyEvent(KeyCode.RIGHT);
            assertTrue(M3SelectionNavigation.handleKeySelection(
                    rightToLeftBackward,
                    owner.getChildren(),
                    second,
                    M3Button.class,
                    true,
                    false,
                    true,
                    selected::set
            ));
            assertSame(first, selected.get());
            assertTrue(rightToLeftBackward.isConsumed());
        });
    }

    /// Verifies that unanchored horizontal selection chooses physical edge targets before RTL mirroring applies.
    @Test
    void keepsUnanchoredHorizontalKeysPhysicalBeforeAnchorExists() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            HBox owner = new HBox(first, second, third);
            AtomicReference<@Nullable M3Button> selected = new AtomicReference<>();

            KeyEvent unanchoredStart = keyEvent(KeyCode.RIGHT);
            assertTrue(M3SelectionNavigation.handleKeySelection(
                    unanchoredStart,
                    owner.getChildren(),
                    null,
                    M3Button.class,
                    true,
                    false,
                    true,
                    selected::set
            ));
            assertSame(first, selected.get());
            assertTrue(unanchoredStart.isConsumed());

            KeyEvent unanchoredEnd = keyEvent(KeyCode.LEFT);
            assertTrue(M3SelectionNavigation.handleKeySelection(
                    unanchoredEnd,
                    owner.getChildren(),
                    null,
                    M3Button.class,
                    true,
                    false,
                    true,
                    selected::set
            ));
            assertSame(third, selected.get());
            assertTrue(unanchoredEnd.isConsumed());
        });
    }

    /// Verifies modified selection navigation keys are left to application shortcuts and platform behavior.
    @Test
    void modifiedSelectionNavigationKeysAreIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Button third = new M3Button("Third");
            HBox owner = new HBox(first, second, third);
            AtomicReference<@Nullable M3Button> selected = new AtomicReference<>(second);

            KeyEvent event = modifiedKeyEvent(KeyCode.RIGHT, false, true, false, false);
            assertFalse(M3SelectionNavigation.handleKeySelection(
                    event,
                    owner.getChildren(),
                    second,
                    M3Button.class,
                    true,
                    false,
                    selected::set
            ));

            assertSame(second, selected.get());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies modified page navigation keys are left to application shortcuts and platform behavior.
    @Test
    void modifiedPageNavigationKeysAreIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            NavigationRow first = new NavigationRow();
            NavigationRow second = new NavigationRow();
            VBox owner = new VBox(first, second);
            AtomicReference<@Nullable NavigationRow> selected = new AtomicReference<>(first);

            KeyEvent event = modifiedKeyEvent(KeyCode.PAGE_DOWN, true, false, false, false);
            assertFalse(M3SelectionNavigation.handlePageKeySelection(
                    event,
                    owner,
                    owner.getChildren(),
                    first,
                    NavigationRow.class,
                    selected::set
            ));

            assertSame(first, selected.get());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies focus navigation does not report success for detached targets.
    @Test
    void keyFocusRejectsDetachedTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button target = new M3Button("Detached");
            HBox owner = new HBox(target);

            KeyEvent event = keyEvent(KeyCode.RIGHT);
            assertFalse(M3SelectionNavigation.handleKeyFocus(
                    event,
                    owner.getChildren(),
                    null,
                    M3Button.class,
                    true,
                    false
            ));
            assertFalse(event.isConsumed());
            assertFalse(target.isFocused());
        });
    }

    /// Verifies page navigation uses the enclosing scroll pane viewport instead of the full content height.
    @Test
    void pageNavigationUsesEnclosingScrollPaneViewportHeight() {
        FxTestUtils.runOnFxThread(() -> {
            NavigationRow first = new NavigationRow();
            NavigationRow second = new NavigationRow();
            NavigationRow third = new NavigationRow();
            NavigationRow fourth = new NavigationRow();
            NavigationRow fifth = new NavigationRow();
            NavigationRow sixth = new NavigationRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();

            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            int expectedStep = Math.max(1, (int) Math.floor(viewportHeight / NavigationRow.ROW_HEIGHT));
            assertTrue(
                    viewportHeight > 0.0 && expectedStep >= 1 && expectedStep < 5,
                    () -> "viewportHeight=" + viewportHeight
            );

            AtomicReference<@Nullable NavigationRow> selected = new AtomicReference<>();
            KeyEvent event = keyEvent(KeyCode.PAGE_DOWN);
            assertTrue(M3SelectionNavigation.handlePageKeySelection(
                    event,
                    owner,
                    owner.getChildren(),
                    first,
                    NavigationRow.class,
                    selected::set
            ));
            assertSame(rowAtStep(second, third, fourth, fifth, expectedStep), selected.get());
            assertNotSame(sixth, selected.get());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies nested selection owners use the viewport of the scroll pane that contains their outer content.
    @Test
    void pageNavigationUsesViewportForNestedScrollPaneContent() {
        FxTestUtils.runOnFxThread(() -> {
            NavigationRow first = new NavigationRow();
            NavigationRow second = new NavigationRow();
            NavigationRow third = new NavigationRow();
            NavigationRow fourth = new NavigationRow();
            NavigationRow fifth = new NavigationRow();
            NavigationRow sixth = new NavigationRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            VBox scrollContent = new VBox(owner);
            ScrollPane scrollPane = new ScrollPane(scrollContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            scrollContent.layout();
            owner.layout();

            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            int expectedStep = Math.max(1, (int) Math.floor(viewportHeight / NavigationRow.ROW_HEIGHT));
            assertTrue(
                    viewportHeight > 0.0 && expectedStep >= 1 && expectedStep < 5,
                    () -> "viewportHeight=" + viewportHeight
            );

            AtomicReference<@Nullable NavigationRow> selected = new AtomicReference<>();
            KeyEvent event = keyEvent(KeyCode.PAGE_DOWN);
            assertTrue(M3SelectionNavigation.handlePageKeySelection(
                    event,
                    owner,
                    owner.getChildren(),
                    first,
                    NavigationRow.class,
                    selected::set
            ));
            assertSame(rowAtStep(second, third, fourth, fifth, expectedStep), selected.get());
            assertNotSame(sixth, selected.get());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies owner-aware arrow focus navigation reveals the focused target.
    @Test
    void keyFocusNavigationRevealsFocusedTarget() {
        FxTestUtils.runOnFxThread(() -> {
            NavigationRow first = new NavigationRow();
            NavigationRow second = new NavigationRow();
            NavigationRow third = new NavigationRow();
            NavigationRow fourth = new NavigationRow();
            NavigationRow fifth = new NavigationRow();
            NavigationRow sixth = new NavigationRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();

            KeyEvent event = keyEvent(KeyCode.DOWN);
            assertTrue(M3SelectionNavigation.handleKeyFocus(
                    event,
                    owner,
                    owner.getChildren(),
                    fifth,
                    NavigationRow.class,
                    false,
                    true
            ));

            assertTargetVisible(scrollPane, owner, sixth);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies a real owner-aware selection container reveals targets reached by keyboard selection.
    @Test
    void navigationRailKeyboardSelectionRevealsSelectedTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationItem first = new M3NavigationItem("First");
            M3NavigationItem second = new M3NavigationItem("Second");
            M3NavigationItem third = new M3NavigationItem("Third");
            M3NavigationItem fourth = new M3NavigationItem("Fourth");
            M3NavigationItem fifth = new M3NavigationItem("Fifth");
            M3NavigationItem sixth = new M3NavigationItem("Sixth");
            M3NavigationRail rail = navigationRail(first, second, third, fourth, fifth, sixth);
            VBox content = new VBox(rail);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(160.0, 96.0);

            show(scrollPane, 160.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(160.0, 96.0);
            scrollPane.layout();
            content.layout();
            rail.layout();
            rail.select(fifth);
            assertTrue(M3Accessible.requestAccessibleFocus(content, rail));

            KeyEvent event = keyEvent(KeyCode.DOWN);
            rail.fireEvent(event);

            assertSame(sixth, rail.getSelectedItem());
            assertTargetVisible(scrollPane, content, sixth);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies a real owner-aware horizontal focus container reveals targets reached by keyboard focus.
    @Test
    void buttonGroupKeyboardFocusRevealsHorizontalTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = wideButton("First");
            M3Button second = wideButton("Second");
            M3Button third = wideButton("Third");
            M3Button fourth = wideButton("Fourth");
            M3Button fifth = wideButton("Fifth");
            M3Button sixth = wideButton("Sixth");
            M3ButtonGroup group = buttonGroup(first, second, third, fourth, fifth, sixth);
            HBox content = new HBox(group);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefSize(180.0, 80.0);

            show(scrollPane, 180.0, 80.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 80.0);
            scrollPane.layout();
            content.layout();
            group.layout();
            assertTrue(M3Accessible.showDirectItem(content, fifth));

            KeyEvent event = keyEvent(KeyCode.RIGHT);
            group.fireEvent(event);

            assertTrue(sixth.isFocused());
            assertTargetHorizontallyVisible(scrollPane, content, sixth);
            assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        });
    }

    /// Verifies owner-aware accessibility item reveal scrolls vertical selection containers.
    @Test
    void listPaneAccessibleShowItemRevealsVerticalTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem first = new M3ListItem("First");
            M3ListItem second = new M3ListItem("Second");
            M3ListItem third = new M3ListItem("Third");
            M3ListItem fourth = new M3ListItem("Fourth");
            M3ListItem fifth = new M3ListItem("Fifth");
            M3ListItem sixth = new M3ListItem("Sixth");
            M3ListPane listPane = listPane(first, second, third, fourth, fifth, sixth);
            VBox content = new VBox(listPane);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            listPane.layout();

            listPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 5);

            assertTrue(sixth.isFocused());
            assertTargetVisible(scrollPane, content, sixth);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies list pane type-ahead focus scrolls the matched item into view.
    @Test
    void listPaneTypeAheadRevealsMatchedTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem archive = new M3ListItem("Archive");
            M3ListItem drafts = new M3ListItem("Drafts");
            M3ListItem inbox = new M3ListItem("Inbox");
            M3ListItem labels = new M3ListItem("Labels");
            M3ListItem settings = new M3ListItem("Settings");
            M3ListItem search = new M3ListItem("Search");
            M3ListPane listPane = listPane(archive, drafts, inbox, labels, settings, search);
            VBox content = new VBox(listPane);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            listPane.layout();
            assertTrue(M3Accessible.requestAccessibleFocus(content, listPane));

            listPane.fireEvent(typedKeyEvent("s"));
            listPane.fireEvent(typedKeyEvent("e"));

            assertTrue(search.isFocused());
            assertTargetVisible(scrollPane, content, search);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies shortcut-modified type-ahead keys are left to application shortcuts.
    @Test
    void listPaneShortcutModifiedTypeAheadIsIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem archive = new M3ListItem("Archive");
            M3ListItem settings = new M3ListItem("Settings");
            M3ListPane listPane = listPane(archive, settings);
            layout(listPane);
            assertTrue(M3Accessible.requestAccessibleFocus(listPane, archive));

            KeyEvent event = modifiedTypedKeyEvent("s", false, true, false, false);
            listPane.fireEvent(event);

            assertTrue(archive.isFocused());
            assertFalse(settings.isFocused());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies shift-modified type-ahead keeps matching uppercase printable keys.
    @Test
    void listPaneShiftTypeAheadMatchesUppercaseText() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem archive = new M3ListItem("Archive");
            M3ListItem settings = new M3ListItem("Settings");
            M3ListPane listPane = listPane(archive, settings);
            layout(listPane);
            assertTrue(M3Accessible.requestAccessibleFocus(listPane, archive));

            KeyEvent event = modifiedTypedKeyEvent("S", true, false, false, false);
            listPane.fireEvent(event);

            assertTrue(settings.isFocused());
        });
    }

    /// Verifies menu type-ahead focus scrolls the matched item into view.
    @Test
    void menuTypeAheadRevealsMatchedTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuItem drafts = new M3MenuItem("Drafts");
            M3MenuItem inbox = new M3MenuItem("Inbox");
            M3MenuItem labels = new M3MenuItem("Labels");
            M3MenuItem settings = new M3MenuItem("Settings");
            M3MenuItem search = new M3MenuItem("Search");
            M3Menu menu = new M3Menu(archive, drafts, inbox, labels, settings, search);
            VBox content = new VBox(menu);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            menu.layout();
            assertTrue(M3Accessible.requestAccessibleFocus(content, menu));

            menu.fireEvent(typedKeyEvent("s"));
            menu.fireEvent(typedKeyEvent("e"));

            assertTrue(search.isFocused());
            assertTargetVisible(scrollPane, content, search);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies modified menu navigation keys are left to application shortcuts.
    @Test
    void menuModifiedNavigationKeysAreIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem archive = new M3MenuItem("Archive");
            M3MenuItem settings = new M3MenuItem("Settings");
            M3Menu menu = new M3Menu(archive, settings);
            menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
            layout(menu);
            assertTrue(M3Accessible.requestAccessibleFocus(menu, archive));
            menu.select(archive);

            KeyEvent event = modifiedKeyEvent(KeyCode.DOWN, false, true, false, false);
            menu.fireEvent(event);

            assertSame(archive, menu.getSelectedItem());
            assertTrue(archive.isFocused());
            assertFalse(settings.isFocused());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies list view modified navigation keys do not move virtualized focus or selection.
    @Test
    void listViewModifiedNavigationKeysAreIgnored() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("Archive", "Settings", "Search");
            listView.setSelectionMode(M3ListSelectionMode.SINGLE);
            listView.selectIndex(0);
            layout(listView);

            KeyEvent event = modifiedKeyEvent(KeyCode.DOWN, false, true, false, false);
            listView.fireEvent(event);

            assertEquals(0, listView.getSelectedIndex());
            assertEquals(-1, listView.getFocusedIndex());
            assertFalse(event.isConsumed());
        });
    }

    /// Verifies list view unmodified navigation keys still move virtualized focus and selection.
    @Test
    void listViewUnmodifiedNavigationKeysMoveFocusAndSelection() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("Archive", "Settings", "Search");
            listView.setSelectionMode(M3ListSelectionMode.SINGLE);
            listView.selectIndex(0);
            layout(listView);

            KeyEvent event = keyEvent(KeyCode.DOWN);
            listView.fireEvent(event);

            assertEquals(1, listView.getSelectedIndex());
            assertEquals(1, listView.getFocusedIndex());
        });
    }

    /// Verifies navigation drawer type-ahead focus scrolls the matched item into view.
    @Test
    void navigationDrawerTypeAheadRevealsMatchedTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem archive = new M3ListItem("Archive");
            M3ListItem drafts = new M3ListItem("Drafts");
            M3ListItem inbox = new M3ListItem("Inbox");
            M3ListItem labels = new M3ListItem("Labels");
            M3ListItem settings = new M3ListItem("Settings");
            M3ListItem search = new M3ListItem("Search");
            M3NavigationDrawer drawer = navigationDrawer(archive, drafts, inbox, labels, settings, search);
            VBox content = new VBox(drawer);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(220.0, 112.0);

            show(scrollPane, 220.0, 112.0);
            scrollPane.applyCss();
            scrollPane.resize(220.0, 112.0);
            scrollPane.layout();
            content.layout();
            drawer.layout();
            assertTrue(M3Accessible.requestAccessibleFocus(content, drawer));

            drawer.fireEvent(typedKeyEvent("s"));
            drawer.fireEvent(typedKeyEvent("e"));

            assertTrue(search.isFocused());
            assertTargetVisible(scrollPane, content, search);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies owner-aware accessibility item reveal scrolls horizontal selection containers.
    @Test
    void buttonGroupAccessibleShowItemRevealsHorizontalTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = wideButton("First");
            M3Button second = wideButton("Second");
            M3Button third = wideButton("Third");
            M3Button fourth = wideButton("Fourth");
            M3Button fifth = wideButton("Fifth");
            M3Button sixth = wideButton("Sixth");
            M3ButtonGroup group = buttonGroup(first, second, third, fourth, fifth, sixth);
            HBox content = new HBox(group);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefSize(180.0, 80.0);

            show(scrollPane, 180.0, 80.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 80.0);
            scrollPane.layout();
            content.layout();
            group.layout();

            group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 5);

            assertTrue(sixth.isFocused());
            assertTargetHorizontallyVisible(scrollPane, content, sixth);
            assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        });
    }

    /// Verifies page-down navigation aligns an oversized target to the top of the viewport.
    @Test
    void pageDownNavigationAlignsTallTargetToViewportTop() {
        FxTestUtils.runOnFxThread(() -> {
            NavigationRow first = new NavigationRow();
            NavigationRow second = new NavigationRow();
            NavigationRow tall = new TallNavigationRow();
            NavigationRow fourth = new NavigationRow();
            VBox owner = new VBox(first, second, tall, fourth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();

            AtomicReference<@Nullable NavigationRow> selected = new AtomicReference<>();
            KeyEvent event = keyEvent(KeyCode.PAGE_DOWN);
            assertTrue(M3SelectionNavigation.handlePageKeySelection(
                    event,
                    owner,
                    owner.getChildren(),
                    first,
                    NavigationRow.class,
                    selected::set
            ));
            assertSame(tall, selected.get());
            assertTargetTopAligned(scrollPane, owner, tall);
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies page-down navigation scrolls a lower target into view with a custom scroll value range.
    @Test
    void pageDownNavigationRevealsTargetWithCustomScrollRange() {
        FxTestUtils.runOnFxThread(() -> {
            NavigationRow first = new NavigationRow();
            NavigationRow second = new NavigationRow();
            NavigationRow third = new NavigationRow();
            NavigationRow fourth = new NavigationRow();
            NavigationRow fifth = new NavigationRow();
            NavigationRow sixth = new NavigationRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);
            scrollPane.setVmin(10.0);
            scrollPane.setVmax(30.0);
            scrollPane.setVvalue(10.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();

            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            int expectedStep = Math.max(1, (int) Math.floor(viewportHeight / NavigationRow.ROW_HEIGHT));
            assertTrue(
                    viewportHeight > 0.0 && expectedStep >= 1 && expectedStep < 5,
                    () -> "viewportHeight=" + viewportHeight
            );

            AtomicReference<@Nullable NavigationRow> selected = new AtomicReference<>();
            KeyEvent event = keyEvent(KeyCode.PAGE_DOWN);
            assertTrue(M3SelectionNavigation.handlePageKeySelection(
                    event,
                    owner,
                    owner.getChildren(),
                    first,
                    NavigationRow.class,
                    selected::set
            ));
            NavigationRow expectedTarget = rowAtStep(second, third, fourth, fifth, expectedStep);
            assertSame(expectedTarget, selected.get());
            assertTargetVisible(scrollPane, owner, expectedTarget);
            assertTrue(scrollPane.getVvalue() > scrollPane.getVmin(), () -> "vvalue=" + scrollPane.getVvalue());
            assertTrue(event.isConsumed());
        });
    }

    /// Verifies page navigation scrolls an offscreen target row back into the viewport.
    @Test
    void pageNavigationRevealsSelectedTargetInScrollPane() {
        FxTestUtils.runOnFxThread(() -> {
            NavigationRow first = new NavigationRow();
            NavigationRow second = new NavigationRow();
            NavigationRow third = new NavigationRow();
            NavigationRow fourth = new NavigationRow();
            NavigationRow fifth = new NavigationRow();
            NavigationRow sixth = new NavigationRow();
            VBox owner = new VBox(first, second, third, fourth, fifth, sixth);
            ScrollPane scrollPane = new ScrollPane(owner);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(120.0, 48.0);

            show(scrollPane, 120.0, 48.0);
            scrollPane.applyCss();
            scrollPane.resize(120.0, 48.0);
            scrollPane.layout();
            owner.layout();
            scrollPane.setVvalue(1.0);

            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            int expectedStep = Math.max(1, (int) Math.floor(viewportHeight / NavigationRow.ROW_HEIGHT));
            assertTrue(
                    viewportHeight > 0.0 && expectedStep >= 1 && expectedStep < 5,
                    () -> "viewportHeight=" + viewportHeight
            );

            AtomicReference<@Nullable NavigationRow> selected = new AtomicReference<>();
            KeyEvent event = keyEvent(KeyCode.PAGE_UP);
            assertTrue(M3SelectionNavigation.handlePageKeySelection(
                    event,
                    owner,
                    owner.getChildren(),
                    sixth,
                    NavigationRow.class,
                    selected::set
            ));
            NavigationRow expectedTarget = rowBeforeStep(second, third, fourth, fifth, expectedStep);
            assertSame(expectedTarget, selected.get());
            assertTargetVisible(scrollPane, owner, expectedTarget);
            assertTrue(scrollPane.getVvalue() < 1.0, () -> "vvalue=" + scrollPane.getVvalue());
            assertTrue(event.isConsumed());
        });
    }

    /// Shows the supplied scroll pane in a real JavaFX window and performs an initial layout pass.
    private static Scene show(ScrollPane scrollPane, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(scrollPane, width, height);
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();
        scrollPane.setFocusTraversable(true);
        scrollPane.requestFocus();
        scrollPane.applyCss();
        scrollPane.layout();
        return scene;
    }

    /// Verifies that the target node is inside the current horizontal scroll pane viewport.
    private static void assertTargetHorizontallyVisible(ScrollPane scrollPane, Node content, Node target) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double scrollableWidth = content.getBoundsInLocal().getWidth() - viewportWidth;
        double valueRange = scrollPane.getHmax() - scrollPane.getHmin();
        assertTrue(scrollableWidth > 0.0, () -> "scrollableWidth=" + scrollableWidth);
        assertTrue(valueRange > 0.0, () -> "hvalue range=" + valueRange);
        double fraction = (scrollPane.getHvalue() - scrollPane.getHmin()) / valueRange;
        double visibleLeft = Math.max(0.0, Math.min(1.0, fraction)) * scrollableWidth;
        double visibleRight = visibleLeft + viewportWidth;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(targetBounds.getMinX() >= visibleLeft - 0.5, () -> "targetLeft=" + targetBounds.getMinX());
        assertTrue(targetBounds.getMaxX() <= visibleRight + 0.5, () -> "targetRight=" + targetBounds.getMaxX());
    }

    /// Verifies that the target row is aligned to the top of the current scroll pane viewport.
    private static void assertTargetTopAligned(ScrollPane scrollPane, VBox content, NavigationRow target) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollableHeight = content.getBoundsInLocal().getHeight() - viewportHeight;
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        double fraction = (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange;
        double visibleTop = Math.max(0.0, Math.min(1.0, fraction)) * scrollableHeight;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(Math.abs(targetBounds.getMinY() - visibleTop) <= 0.5,
                () -> "targetTop=" + targetBounds.getMinY() + ", visibleTop=" + visibleTop);
    }

    /// Verifies that the target row is inside the current scroll pane viewport.
    private static void assertTargetVisible(ScrollPane scrollPane, VBox content, Node target) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollableHeight = content.getBoundsInLocal().getHeight() - viewportHeight;
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        double fraction = (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange;
        double visibleTop = Math.max(0.0, Math.min(1.0, fraction)) * scrollableHeight;
        double visibleBottom = visibleTop + viewportHeight;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(targetBounds.getMinY() >= visibleTop - 0.5, () -> "targetTop=" + targetBounds.getMinY());
        assertTrue(targetBounds.getMaxY() <= visibleBottom + 0.5, () -> "targetBottom=" + targetBounds.getMaxY());
    }

    /// Returns the row reached from the first row after the supplied page-navigation step.
    private static NavigationRow rowAtStep(
            NavigationRow second,
            NavigationRow third,
            NavigationRow fourth,
            NavigationRow fifth,
            int step
    ) {
        return switch (step) {
            case 1 -> second;
            case 2 -> third;
            case 3 -> fourth;
            default -> fifth;
        };
    }

    /// Returns the row reached from the sixth row after the supplied backward page-navigation step.
    private static NavigationRow rowBeforeStep(
            NavigationRow second,
            NavigationRow third,
            NavigationRow fourth,
            NavigationRow fifth,
            int step
    ) {
        return switch (step) {
            case 1 -> fifth;
            case 2 -> fourth;
            case 3 -> third;
            default -> second;
        };
    }

    /// Verifies empty collection containers report failed accessibility focus requests.
    @Test
    void collectionContainersRejectAccessibleFocusWhenEmpty() {
        FxTestUtils.runOnFxThread(() -> {
            VBox root = new VBox(
                    new M3BadgedBox(),
                    new M3Banner("Message"),
                    new M3BottomAppBar(),
                    new M3ButtonGroup(),
                    new M3ChipGroup(),
                    new M3FormPane(),
                    new M3FormRow(),
                    new M3FormSection(),
                    new M3ListPane(),
                    new M3NavigationBar(),
                    new M3NavigationRail(),
                    new M3Surface(),
                    new M3TabBar(),
                    new M3Toolbar(),
                    new M3TopAppBar(),
                    new M3SegmentedButtonGroup(),
                    new M3IconToggleButtonGroup()
            );
            layout(root);

            for (Node child : root.getChildren()) {
                assertFalse(M3Accessible.requestAccessibleFocus(child), child::toString);
            }
        });
    }

    /// Verifies selection containers focus their selected item through accessibility focus requests.
    @Test
    void selectionContainersFocusSelectedItemsThroughAccessibleRequest() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationItem navFirst = new M3NavigationItem("Home");
            M3NavigationItem navSecond = new M3NavigationItem("Settings");
            M3NavigationBar navigationBar = navigationBar(navFirst, navSecond);
            navigationBar.select(navSecond);

            M3NavigationItem railFirst = new M3NavigationItem("Inbox");
            M3NavigationItem railSecond = new M3NavigationItem("Archive");
            M3NavigationRail navigationRail = navigationRail(railFirst, railSecond);
            navigationRail.select(railSecond);

            M3Tab tabFirst = new M3Tab("Day");
            M3Tab tabSecond = new M3Tab("Week");
            M3TabBar tabBar = tabBar(tabFirst, tabSecond);
            tabBar.select(tabSecond);

            M3SegmentedButton segmentedFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentedSecond = new M3SegmentedButton("Week");
            M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(segmentedFirst, segmentedSecond);
            segmentedGroup.select(segmentedSecond);

            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond);
            iconGroup.select(iconSecond);

            layout(new VBox(navigationBar, navigationRail, tabBar, segmentedGroup, iconGroup));

            assertTrue(M3Accessible.requestAccessibleFocus(navigationBar));
            assertTrue(navSecond.isFocused());
            assertTrue(M3Accessible.requestAccessibleFocus(navigationRail));
            assertTrue(railSecond.isFocused());
            assertTrue(M3Accessible.requestAccessibleFocus(tabBar));
            assertTrue(tabSecond.isFocused());
            assertTrue(M3Accessible.requestAccessibleFocus(segmentedGroup));
            assertTrue(segmentedSecond.isFocused());
            assertTrue(M3Accessible.requestAccessibleFocus(iconGroup));
            assertTrue(iconSecond.isFocused());
        });
    }

    /// Verifies selection containers reveal explicitly requested accessibility items.
    @Test
    void selectionContainersRevealExplicitAccessibleItems() {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationItem navFirst = new M3NavigationItem("Home");
            M3NavigationItem navSecond = new M3NavigationItem("Settings");
            M3NavigationBar navigationBar = navigationBar(navFirst, navSecond);

            M3NavigationItem railFirst = new M3NavigationItem("Inbox");
            M3NavigationItem railSecond = new M3NavigationItem("Archive");
            M3NavigationRail navigationRail = navigationRail(railFirst, railSecond);

            M3Tab tabFirst = new M3Tab("Day");
            M3Tab tabSecond = new M3Tab("Week");
            M3TabBar tabBar = tabBar(tabFirst, tabSecond);

            M3SegmentedButton segmentedFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentedSecond = new M3SegmentedButton("Week");
            M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(segmentedFirst, segmentedSecond);

            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond);

            VBox root = new VBox(navigationBar, navigationRail, tabBar, segmentedGroup, iconGroup);
            layout(root);
            assertTrue(M3Accessible.requestAccessibleFocus(root, navigationBar));

            assertTrue(navigationBar.showAccessibleItem(navSecond));
            assertTrue(navSecond.isFocused());
            assertTrue(navigationRail.showAccessibleItem(railSecond));
            assertTrue(railSecond.isFocused());
            assertTrue(tabBar.showAccessibleItem(tabSecond));
            assertTrue(tabSecond.isFocused());
            assertTrue(segmentedGroup.showAccessibleItem(segmentedSecond));
            assertTrue(segmentedSecond.isFocused());
            assertTrue(iconGroup.showAccessibleItem(iconSecond));
            assertTrue(iconSecond.isFocused());
        });
    }

    /// Test row with a stable preferred size for page navigation calculations.
    @NotNullByDefault
    private static class NavigationRow extends Region {
        /// The preferred height of one navigation row.
        private static final double ROW_HEIGHT = 20.0;

        /// Creates a reachable focusable navigation row.
        private NavigationRow() {
            setFocusTraversable(true);
        }

        /// Computes the preferred row width.
        @Override
        protected double computePrefWidth(double height) {
            return 100.0;
        }

        /// Computes the preferred row height.
        @Override
        protected double computePrefHeight(double width) {
            return ROW_HEIGHT;
        }
    }

    /// Test row that is taller than the page-navigation viewport.
    @NotNullByDefault
    private static final class TallNavigationRow extends NavigationRow {
        /// Creates a tall reachable focusable row.
        private TallNavigationRow() {
        }

        /// Computes the preferred row height.
        @Override
        protected double computePrefHeight(double width) {
            return 72.0;
        }
    }

    /// Creates a wide test button for horizontal viewport reveal checks.
    private static M3Button wideButton(String text) {
        M3Button button = new M3Button(text);
        button.setPrefWidth(96.0);
        return button;
    }

    /// Shows the supplied root in a real JavaFX window and performs an initial layout pass.
    private static Scene show(Parent root, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();
        root.setFocusTraversable(true);
        root.requestFocus();
        root.applyCss();
        root.layout();
        return scene;
    }

    /// Creates a scene and lays out the supplied root for focus tests.
    private static void layout(Parent root) {
        show(root, 360.0, 240.0);
    }

    /// Creates a typed key event for type-ahead navigation behavior checks.
    private static KeyEvent typedKeyEvent(String character) {
        return new KeyEvent(
                KeyEvent.KEY_TYPED,
                character,
                "",
                KeyCode.UNDEFINED,
                false,
                false,
                false,
                false
        );
    }

    /// Creates a modified typed key event for type-ahead navigation behavior checks.
    private static KeyEvent modifiedTypedKeyEvent(
            String character,
            boolean shiftDown,
            boolean controlDown,
            boolean altDown,
            boolean metaDown
    ) {
        return new KeyEvent(
                KeyEvent.KEY_TYPED,
                character,
                character,
                KeyCode.UNDEFINED,
                shiftDown,
                controlDown,
                altDown,
                metaDown
        );
    }

    /// Creates a pressed key event for shared navigation behavior checks.
    private static KeyEvent keyEvent(KeyCode code) {
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

    /// Creates a modified pressed key event for shared navigation behavior checks.
    private static KeyEvent modifiedKeyEvent(
            KeyCode code,
            boolean shiftDown,
            boolean controlDown,
            boolean altDown,
            boolean metaDown
    ) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                code.getName(),
                code.getName(),
                code,
                shiftDown,
                controlDown,
                altDown,
                metaDown
        );
    }
}
