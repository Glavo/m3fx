// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Objects;

import static org.glavo.m3fx.M3TestControls.bottomSheet;
import static org.glavo.m3fx.M3TestControls.listView;
import static org.glavo.m3fx.M3TestControls.navigationDrawer;
import static org.glavo.m3fx.M3TestControls.richTooltip;
import static org.glavo.m3fx.M3TestControls.searchView;
import static org.glavo.m3fx.M3TestControls.surface;
import static org.glavo.m3fx.M3TestControls.toolbar;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies representative keyboard and accessibility routes across mixed popup and overlay stacks.
@NotNullByDefault
@Tier2Test
final class M3MixedPopupFocusTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies nested submenu and rich-tooltip focus routing through runtime orientation changes.
    @Test
    void nestedSubMenuRichTooltipPreservesFocusAndOrientation() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3MenuItem htmlItem = new M3MenuItem("HTML");
            M3Button action = new M3Button("Describe");
            M3RichTooltip tooltip = installRichTooltip(
                    pdfItem,
                    "PDF export",
                    "Exports the active document.",
                    action
            );
            M3SubMenuItem recentItem = new M3SubMenuItem("Recent", pdfItem, htmlItem);
            M3SubMenuItem exportItem = new M3SubMenuItem("Export", recentItem);
            M3MenuButton menuButton = new M3MenuButton("More", exportItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface owner = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(owner);
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                owner.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                menuButton.showMenu();
                exportItem.showSubMenu();
                recentItem.showSubMenu();
                pdfItem.requestFocus();
                tooltip.show(pdfItem, stage.getX() + 312.0, stage.getY() + 144.0);
                pdfItem.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(action.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertNestedMenuStackOrientation(menuButton, exportItem, NodeOrientation.LEFT_TO_RIGHT);
                assertNestedMenuStackOrientation(exportItem, recentItem, NodeOrientation.LEFT_TO_RIGHT);
                assertPopupFocusRoutedByContainer(owner, action);

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                assertTrue(action.isFocused());
                assertNestedMenuStackOrientation(menuButton, exportItem, NodeOrientation.RIGHT_TO_LEFT);
                assertNestedMenuStackOrientation(exportItem, recentItem, NodeOrientation.RIGHT_TO_LEFT);
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, action.getEffectiveNodeOrientation());
                assertPopupFocusRoutedByContainer(owner, action);

                action.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(pdfItem.isFocused());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertSame(pdfItem, owner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(pdfItem, tooltip);
                recentItem.hideSubMenu();
                exportItem.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies unreachable tooltip actions do not open any popup branch as a side effect.
    @Test
    void inaccessibleNestedPopupTargetLeavesStackClosed() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3Button action = new M3Button("Describe");
            action.setDisable(true);
            M3RichTooltip tooltip = installRichTooltip(
                    pdfItem,
                    "PDF export",
                    "Exports the active document.",
                    action
            );
            M3SubMenuItem recentItem = new M3SubMenuItem("Recent", pdfItem);
            M3SubMenuItem exportItem = new M3SubMenuItem("Export", recentItem);
            M3MenuButton menuButton = new M3MenuButton("More", exportItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface owner = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(owner);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                owner.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                owner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, action);

                assertFalse(menuButton.isShowing());
                assertFalse(exportItem.isSubMenuShowing());
                assertFalse(recentItem.isSubMenuShowing());
                assertFalse(tooltip.isShowing());
                assertFalse(action.isFocused());
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(pdfItem, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies nested menu and picker focus routing, RTL propagation, and Escape restoration.
    @Test
    void nestedSubMenuPickerPreservesFocusAndOrientation() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 6, 22);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3MenuButton menuButton = new M3MenuButton("More", scheduleItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface owner = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(owner);
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                owner.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                owner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(menuButton.isShowing());
                assertTrue(scheduleItem.isSubMenuShowing());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertNestedMenuStackOrientation(menuButton, scheduleItem, NodeOrientation.LEFT_TO_RIGHT);
                assertPopupFocusRoutedByContainer(owner, pickerFocusNode);

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertNestedMenuStackOrientation(menuButton, scheduleItem, NodeOrientation.RIGHT_TO_LEFT);
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, field.getPicker().getEffectiveNodeOrientation());
                assertSame(pickerFocusNode, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(owner, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(scheduleItem.isSubMenuShowing());
                assertTrue(menuButton.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), owner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                scheduleItem.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies dialog panes expose nested menu focus and restore it to the menu owner on dismissal.
    @Test
    void dialogPaneRoutesNestedMenuFocus() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem firstItem = new M3MenuItem("First");
            M3MenuItem secondItem = new M3MenuItem("Second");
            M3MenuButton menuButton = new M3MenuButton("Open menu", firstItem, secondItem);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(menuButton);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 520.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 360.0, 220.0);
                root.layout();

                menuButton.showMenu();
                secondItem.requestFocus();

                assertTrue(menuButton.isShowing());
                assertTrue(secondItem.isFocused());
                assertSame(secondItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, secondItem);

                secondItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies a virtualized row exposes rich-tooltip action focus without losing the row route.
    @Test
    void virtualizedListRowRoutesRichTooltipFocus() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button rowAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    rowAction,
                    "Row action",
                    "Shows details for the visible row.",
                    tooltipAction
            );
            M3ListItem row = new M3ListItem("Project Alpha");
            row.setTrailing(rowAction);
            M3ListView<String> listView = listView("Project Alpha");
            listView.setCellFactory(view -> new M3ListCell<>(view) {
                /// Creates the retained row configured for the popup-focus scenario.
                @Override
                protected M3ListItem createListItem() {
                    return row;
                }
            });
            listView.setFixedCellSize(72.0);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(listView);
                Scene scene = new Scene(root, 620.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                listView.resizeRelocate(32.0, 32.0, 480.0, 160.0);
                root.layout();

                rowAction.requestFocus();
                tooltip.show(rowAction, stage.getX() + 360.0, stage.getY() + 112.0);
                rowAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertPopupFocusRoutedByContainer(listView, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(rowAction.isFocused());
                assertSame(rowAction, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(rowAction, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies a modal overlay prevents queued snackbar promotion from stealing focus.
    @Test
    void modalOverlayBlocksSnackbarFocusTransfer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button pageAction = new M3Button("Page action");
            Pane content = new Pane(pageAction);
            M3OverlayPane overlayPane = new M3OverlayPane();
            overlayPane.setContent(content);
            overlayPane.setSnackbarDisplayDuration(javafx.util.Duration.INDEFINITE);
            M3Snackbar firstSnackbar = new M3Snackbar(
                    "Saved",
                    new M3Snackbar.Action("Undo", () -> {
                    })
            );
            M3Snackbar secondSnackbar = new M3Snackbar(
                    "Deleted",
                    new M3Snackbar.Action("Restore", () -> {
                    })
            );
            M3Button modalAction = new M3Button("Modal action");
            Pane modalLayer = new Pane(modalAction);
            Stage stage = new Stage();

            try {
                Scene scene = new Scene(overlayPane, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                pageAction.resizeRelocate(32.0, 32.0, 160.0, 48.0);
                overlayPane.showSnackbar(firstSnackbar);
                overlayPane.enqueueSnackbar(secondSnackbar);
                overlayPane.applyCss();
                overlayPane.layout();
                Node presenter = Objects.requireNonNull(
                        overlayPane.lookup(".m3-snackbar-presenter"),
                        "snackbar presenter"
                );
                Node firstAction = Objects.requireNonNull(
                        (Node) presenter.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE),
                        "first snackbar action"
                );
                firstAction.requestFocus();
                assertTrue(firstAction.isFocused());

                M3OverlayPane.OverlayHandle modalHandle = overlayPane.showModalOverlay(modalLayer);
                overlayPane.applyCss();
                overlayPane.layout();
                modalAction.resizeRelocate(240.0, 144.0, 160.0, 48.0);
                modalAction.requestFocus();
                overlayPane.dismissSnackbar();
                overlayPane.applyCss();
                overlayPane.layout();

                Node secondAction = Objects.requireNonNull(
                        presenter.lookup(".m3-snackbar-action"),
                        "second snackbar action"
                );
                assertSame(secondSnackbar, overlayPane.getSnackbar());
                assertTrue(overlayPane.getSnackbarQueue().isEmpty());
                assertTrue(modalAction.isFocused());
                assertFalse(secondAction.isFocused());

                assertTrue(modalHandle.hide());
                assertTrue(M3Accessible.requestAccessibleFocus(overlayPane, presenter));
                assertTrue(secondAction.isFocused());
            } finally {
                overlayPane.dismissAllSnackbars();
                stage.close();
            }
        });
    }

    /// Verifies a hidden modal sheet can reveal a nested submenu target and route its popup focus.
    @Test
    void hiddenBottomSheetRevealsNestedMenuTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3SubMenuItem moveItem = new M3SubMenuItem("Move to", archiveItem);
            M3MenuButton menuButton = new M3MenuButton("Actions", moveItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(260.0, 88.0);
            M3BottomSheet sheet = bottomSheet("Queue", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);
                root.layout();

                assertTrue(sheet.isShown());
                assertTrue(menuButton.isShowing());
                assertTrue(moveItem.isSubMenuShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, moveItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(sheet, archiveItem);
            } finally {
                moveItem.hideSubMenu();
                menuButton.hideMenu();
                sheet.hide();
                stage.close();
            }
        });
    }

    /// Verifies search result rows expose nested menu focus and restore it after Escape.
    @Test
    void searchViewRoutesNestedResultMenuFocus() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(menuButton);
            M3SearchView searchView = searchView("Search", result);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchView);
                Scene scene = new Scene(root, 640.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchView.resizeRelocate(32.0, 32.0, 480.0, 180.0);
                root.layout();

                menuButton.showMenu();
                archiveItem.requestFocus();

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(searchView, archiveItem);

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies accessibility reveal expands a drawer group before opening a nested menu target.
    @Test
    void navigationDrawerRevealsCollapsedGroupMenuTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(menuButton);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 480.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 360.0, 240.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertFalse(menuButton.isShowing());

                drawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);
                root.layout();

                assertTrue(group.isExpanded());
                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(drawer, archiveItem);
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies toolbar items route rich-tooltip focus and restore the originating item after Escape.
    @Test
    void toolbarRoutesNestedRichTooltipFocus() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Format");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Format",
                    "Shows formatting details.",
                    tooltipAction
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 520.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 260.0, 72.0);
                root.layout();

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 160.0, stage.getY() + 112.0);
                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertPopupFocusRoutedByContainer(toolbar, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(ownerAction, tooltip);
                stage.close();
            }
        });
    }

    /// Creates a key press event for popup keyboard tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /// Verifies the effective orientation of a menu button and its child submenu.
    private static void assertNestedMenuStackOrientation(
            M3MenuButton menuButton,
            M3SubMenuItem subMenuItem,
            NodeOrientation orientation
    ) {
        assertEquals(orientation, menuButton.getEffectiveNodeOrientation());
        assertEquals(orientation, menuButton.getMenu().getNodeOrientation());
        assertEquals(orientation, subMenuItem.getEffectiveNodeOrientation());
        assertEquals(orientation, subMenuItem.getSubMenu().getNodeOrientation());
    }

    /// Verifies the effective orientation of a parent submenu and its child submenu.
    private static void assertNestedMenuStackOrientation(
            M3SubMenuItem parentSubMenuItem,
            M3SubMenuItem childSubMenuItem,
            NodeOrientation orientation
    ) {
        assertEquals(orientation, parentSubMenuItem.getEffectiveNodeOrientation());
        assertEquals(orientation, parentSubMenuItem.getSubMenu().getNodeOrientation());
        assertEquals(orientation, childSubMenuItem.getEffectiveNodeOrientation());
        assertEquals(orientation, childSubMenuItem.getSubMenu().getNodeOrientation());
    }

    /// Verifies a composite owner preserves and reveals an active external popup focus target.
    private static void assertPopupFocusRoutedByContainer(Node container, Node focusTarget) {
        assertSame(focusTarget, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        container.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

        assertTrue(focusTarget.isFocused());
        assertSame(focusTarget, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

        assertTrue(focusTarget.isFocused());
        assertSame(focusTarget, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Creates and installs a rich tooltip for mixed popup focus tests.
    private static M3RichTooltip installRichTooltip(
            Node node,
            String title,
            String supportingText,
            Node... actions
    ) {
        M3RichTooltip tooltip = richTooltip(title, supportingText, actions);
        M3Tooltip.install(node, tooltip);
        return tooltip;
    }
}
