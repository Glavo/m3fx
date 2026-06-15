// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
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
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests keyboard focus routing across nested Material popup and overlay stacks.
@NotNullByDefault
final class M3MixedPopupFocusTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that a rich tooltip opened from a menu item keeps the parent menu stack active.
    @Test
    void richTooltipInsideMenuPopupRestoresFocusWithoutClosingMenu() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem detailsItem = new M3MenuItem("Details");
            M3Button action = new M3Button("Learn");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    detailsItem,
                    "Details",
                    "Supplemental menu help",
                    action
            );
            M3MenuButton menuButton = new M3MenuButton(
                    "Open",
                    detailsItem,
                    new M3MenuItem("Other")
            );
            Stage stage = new Stage();

            try {
                Pane root = new Pane(menuButton);
                Scene scene = new Scene(root, 360.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menuButton.resizeRelocate(32.0, 32.0, 120.0, 48.0);
                root.layout();

                menuButton.showMenu();
                detailsItem.requestFocus();
                tooltip.show(detailsItem, stage.getX() + 144.0, stage.getY() + 128.0);

                assertTrue(menuButton.isShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(detailsItem.isFocused());
                assertSame(detailsItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                detailsItem.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(action.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(menuButton.isShowing());
                assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                action.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(detailsItem.isFocused());
                assertTrue(menuButton.isShowing());
                assertSame(detailsItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that a rich tooltip opened from a nested submenu item preserves the full popup focus chain.
    @Test
    void richTooltipInsideNestedSubMenuPreservesPopupFocusChain() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3MenuItem htmlItem = new M3MenuItem("HTML");
            M3Button action = new M3Button("Describe");
            M3RichTooltip tooltip = M3RichTooltip.install(
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
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                menuButton.showMenu();
                exportItem.showSubMenu();
                recentItem.showSubMenu();
                pdfItem.requestFocus();
                tooltip.show(pdfItem, stage.getX() + 312.0, stage.getY() + 144.0);

                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(pdfItem.isFocused());
                assertSame(pdfItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                pdfItem.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(action.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                action.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(pdfItem.isFocused());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertSame(pdfItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that composite owners can reveal a rich tooltip action inside a closed nested submenu branch.
    @Test
    void surfaceRevealsRichTooltipActionInsideNestedSubMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3MenuItem htmlItem = new M3MenuItem("HTML");
            M3Button action = new M3Button("Describe");
            M3RichTooltip tooltip = M3RichTooltip.install(
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
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                assertFalse(menuButton.isShowing());
                assertFalse(exportItem.isSubMenuShowing());
                assertFalse(recentItem.isSubMenuShowing());
                assertFalse(tooltip.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, action);

                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(action.isFocused());
                assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                action.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(pdfItem.isFocused());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertSame(pdfItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that composite owners reject rich tooltip action reveal when the requested action is unreachable.
    @Test
    void surfaceRejectsUnreachableRichTooltipActionInsideNestedSubMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3Button action = new M3Button("Describe");
            action.setDisable(true);
            M3RichTooltip tooltip = M3RichTooltip.install(
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
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, action);

                assertFalse(menuButton.isShowing());
                assertFalse(exportItem.isSubMenuShowing());
                assertFalse(recentItem.isSubMenuShowing());
                assertFalse(tooltip.isShowing());
                assertFalse(action.isFocused());
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that menu buttons reveal picker value targets inside nested submenu branches.
    @Test
    void menuButtonRevealsPickerValueTargetInsideNestedSubMenu() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 7, 8));
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3MenuButton menuButton = new M3MenuButton("More", scheduleItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(menuButton);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menuButton.resizeRelocate(32.0, 32.0, 180.0, 56.0);
                root.layout();

                assertNestedSubMenuPickerValueTargetRoutedByContainer(
                        menuButton,
                        menuButton,
                        scheduleItem,
                        field,
                        LocalDate.of(2026, 7, 12)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that composite owners reveal picker value targets inside closed nested submenu branches.
    @Test
    void surfaceRevealsPickerValueTargetInsideClosedNestedSubMenuBranch() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(8, 30));
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3MenuButton menuButton = new M3MenuButton("More", scheduleItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 420.0, 144.0);
                menuButton.resizeRelocate(24.0, 24.0, 180.0, 56.0);
                root.layout();

                assertNestedSubMenuPickerValueTargetRoutedByContainer(
                        surface,
                        menuButton,
                        scheduleItem,
                        field,
                        LocalTime.of(9, 45)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that nested submenu date range picker reveal preserves the focused endpoint editor.
    @Test
    void menuButtonRevealsDateRangePickerValueTargetInsideActiveNestedSubMenu() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 7, 8),
                    LocalDate.of(2026, 7, 12)
            );
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3MenuButton menuButton = new M3MenuButton("More", scheduleItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(menuButton);
                Scene scene = new Scene(root, 860.0, 480.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menuButton.resizeRelocate(32.0, 32.0, 180.0, 56.0);
                root.layout();

                assertNestedSubMenuDateRangePickerValueTargetRoutedByContainer(
                        menuButton,
                        menuButton,
                        scheduleItem,
                        field,
                        LocalDate.of(2026, 7, 18)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog panes expose rich tooltip action focus from nested menu popup items.
    @Test
    void dialogPaneRoutesFocusThroughRichTooltipActionInsideNestedMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem detailsItem = new M3MenuItem("Details");
            M3Button action = new M3Button("Inspect");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    detailsItem,
                    "Details",
                    "Dialog menu item help",
                    action
            );
            M3MenuButton menuButton = new M3MenuButton("Open menu", detailsItem);
            Pane content = new Pane(menuButton);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 560.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 420.0, 220.0);
                content.resizeRelocate(0.0, 0.0, 320.0, 80.0);
                menuButton.resizeRelocate(0.0, 0.0, 160.0, 48.0);
                root.layout();

                menuButton.showMenu();
                detailsItem.requestFocus();
                tooltip.show(detailsItem, stage.getX() + 216.0, stage.getY() + 144.0);

                assertTrue(menuButton.isShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(detailsItem.isFocused());
                assertSame(detailsItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                detailsItem.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(action.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(menuButton.isShowing());
                assertSame(action, menuButton.getMenu().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(action.isFocused());
                assertSame(action, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                action.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(detailsItem.isFocused());
                assertTrue(menuButton.isShowing());
                assertSame(detailsItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that dialog panes expose focus from a nested menu popup and restore it on dismissal.
    @Test
    void dialogPaneRoutesFocusThroughNestedMenuPopup() {
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
                assertSame(secondItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(secondItem.isFocused());
                assertSame(secondItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                secondItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog action buttons expose rich tooltip action focus.
    @Test
    void dialogPaneRoutesFocusThroughActionRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3Button tooltipAction = new M3Button("Details");
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

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = M3RichTooltip.install(
                        okButton,
                        "Confirm",
                        "Dialog action help",
                        tooltipAction
                );

                try {
                    okButton.requestFocus();
                    tooltip.show(okButton, stage.getX() + 216.0, stage.getY() + 184.0);

                    assertTrue(tooltip.isShowing());
                    assertTrue(okButton.isFocused());
                    assertSame(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                    okButton.fireEvent(keyPressed(KeyCode.F6));

                    assertTrue(tooltipAction.isFocused());
                    assertTrue(tooltip.isShowing());
                    assertSame(tooltipAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                    assertPopupFocusRoutedByContainer(dialogPane, tooltipAction);

                    tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                    assertFalse(tooltip.isShowing());
                    assertTrue(okButton.isFocused());
                    assertSame(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog action buttons reveal rich tooltip action targets from explicit accessibility actions.
    @Test
    void dialogPaneRevealsActionRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3Button tooltipAction = new M3Button("Details");
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

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = M3RichTooltip.install(
                        okButton,
                        "Confirm",
                        "Dialog action help",
                        tooltipAction
                );

                try {
                    assertFalse(tooltip.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                    root.layout();

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());
                    assertSame(tooltipAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                    assertPopupFocusRoutedByContainer(dialogPane, tooltipAction);

                    tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                    assertFalse(tooltip.isShowing());
                    assertTrue(okButton.isFocused());
                    assertSame(okButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog panes expose nested picker popup focus through ordinary content containers.
    @Test
    void dialogPaneRoutesFocusThroughNestedPickerPopupInContentContainer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            Pane content = new Pane(field);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 720.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 560.0, 280.0);
                content.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                field.resizeRelocate(0.0, 0.0, 320.0, 72.0);
                root.layout();

                field.showPicker();
                field.getPicker().requestFocus();
                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertSame(pickerFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(pickerFocusNode.isFocused());
                assertSame(pickerFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(pickerFocusNode.isFocused());
                assertSame(pickerFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog panes preserve the date range endpoint that opened a nested picker popup.
    @Test
    void dialogPaneRestoresDateRangeEndpointFocusAfterNestedPickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 19),
                    LocalDate.of(2026, 5, 23)
            );
            Pane content = new Pane(field);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 760.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 620.0, 300.0);
                content.resizeRelocate(0.0, 0.0, 560.0, 96.0);
                field.resizeRelocate(0.0, 0.0, 520.0, 72.0);
                root.layout();

                field.getEndEditor().requestFocus();
                field.showPicker();
                field.getPicker().requestFocus();
                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertSame(pickerFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(pickerFocusNode.isFocused());
                assertSame(pickerFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEndEditor().isFocused());
                assertSame(field.getEndEditor(), dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that indexed item containers expose active popup focus owned by child menu buttons.
    @Test
    void indexedContainersRouteFocusThroughNestedMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem firstItem = new M3MenuItem("First");
            M3MenuItem secondItem = new M3MenuItem("Second");
            M3MenuButton menuButton = new M3MenuButton("Open menu", firstItem, secondItem);
            M3ButtonGroup group = new M3ButtonGroup(menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 520.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 260.0, 64.0);
                root.layout();

                menuButton.showMenu();
                secondItem.requestFocus();

                assertTrue(menuButton.isShowing());
                assertTrue(secondItem.isFocused());
                assertPopupFocusRoutedByContainer(group, secondItem);

                secondItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that indexed item containers reveal nested popup targets from explicit accessibility actions.
    @Test
    void indexedContainersRevealNestedMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem firstItem = new M3MenuItem("First");
            M3MenuItem secondItem = new M3MenuItem("Second");
            M3MenuButton menuButton = new M3MenuButton("Open menu", firstItem, secondItem);
            M3ButtonGroup group = new M3ButtonGroup(menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 520.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 260.0, 64.0);
                root.layout();

                assertFalse(menuButton.isShowing());

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, secondItem);

                assertTrue(menuButton.isShowing());
                assertTrue(secondItem.isFocused());
                assertSame(secondItem, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active picker popup focus to the surface owner.
    @Test
    void surfaceRoutesFocusThroughNestedPickerPopupInContentContainer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 5, 20));
            Pane content = new Pane(field);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                field.resizeRelocate(0.0, 0.0, 320.0, 72.0);
                root.layout();

                field.showPicker();
                field.getPicker().requestFocus();
                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active time picker popup focus to the surface owner.
    @Test
    void surfaceRoutesFocusThroughNestedTimePickerPopupInContentContainer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(10, 30));
            Pane content = new Pane(field);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                field.resizeRelocate(0.0, 0.0, 320.0, 72.0);
                root.layout();

                field.showPicker();
                field.getPicker().requestFocus();
                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal nested date picker value targets from explicit actions.
    @Test
    void surfaceRevealsNestedDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 5, 20));
            LocalDate targetDate = LocalDate.of(2026, 5, 21);
            Pane content = new Pane(field);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                field.resizeRelocate(0.0, 0.0, 320.0, 72.0);
                root.layout();

                assertFalse(field.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal nested date range picker value targets from explicit actions.
    @Test
    void dialogPaneRevealsNestedDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 19),
                    LocalDate.of(2026, 5, 23)
            );
            LocalDate targetDate = LocalDate.of(2026, 5, 22);
            Pane content = new Pane(field);
            content.setPrefSize(560.0, 96.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 760.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 620.0, 300.0);
                content.resizeRelocate(0.0, 0.0, 560.0, 96.0);
                field.resizeRelocate(0.0, 0.0, 520.0, 72.0);
                root.layout();

                assertFalse(field.isShowing());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(dialogPane, pickerFocusNode);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal nested time picker value targets from explicit actions.
    @Test
    void surfaceRevealsNestedTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(10, 30));
            LocalTime targetTime = LocalTime.of(11, 0);
            Pane content = new Pane(field);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                field.resizeRelocate(0.0, 0.0, 320.0, 72.0);
                root.layout();

                assertFalse(field.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetTime);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal nested menu popup targets from explicit actions.
    @Test
    void dialogPaneRevealsNestedMenuPopupTargetInContentContainer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3MenuItem htmlItem = new M3MenuItem("HTML");
            M3SubMenuItem recentItem = new M3SubMenuItem("Recent", pdfItem, htmlItem);
            M3SubMenuItem exportItem = new M3SubMenuItem("Export", recentItem);
            M3MenuButton menuButton = new M3MenuButton("More", exportItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(360.0, 80.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 560.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 440.0, 220.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                assertFalse(menuButton.isShowing());
                assertFalse(exportItem.isSubMenuShowing());
                assertFalse(recentItem.isSubMenuShowing());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, htmlItem);

                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertTrue(htmlItem.isFocused());
                assertSame(htmlItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active split-button menu popup focus to the surface owner.
    @Test
    void surfaceRoutesFocusThroughNestedSplitButtonPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = new M3SplitButton("Create", draftItem, publishItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                splitButton.showMenu();
                publishItem.requestFocus();

                assertTrue(splitButton.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, publishItem);

                publishItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(splitButton.isShowing());
                assertTrue(splitButton.getMenuButton().isFocused());
                assertSame(splitButton.getMenuButton(), surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal nested split-button menu targets from explicit actions.
    @Test
    void surfaceRevealsNestedSplitButtonPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = new M3SplitButton("Create", draftItem, publishItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                assertFalse(splitButton.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, publishItem);

                assertTrue(splitButton.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active FAB-menu rich tooltip action focus.
    @Test
    void surfaceRoutesFocusThroughNestedFabMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3FloatingActionButton ownerAction = new M3FloatingActionButton("Edit");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Edit",
                    "Edits the selected item.",
                    tooltipAction
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.addItem(ownerAction);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 220.0);
                fabMenu.resizeRelocate(0.0, 0.0, 160.0, 160.0);
                root.layout();

                fabMenu.show();
                root.applyCss();
                root.layout();
                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 216.0, stage.getY() + 144.0);

                assertTrue(fabMenu.isExpanded());
                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(fabMenu.isExpanded());
                assertSame(tooltipAction, fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertTrue(fabMenu.isExpanded());
                assertSame(ownerAction, fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal FAB-menu rich tooltip actions from explicit actions.
    @Test
    void surfaceRevealsNestedFabMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3FloatingActionButton ownerAction = new M3FloatingActionButton("Edit");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Edit",
                    "Edits the selected item.",
                    tooltipAction
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.addItem(ownerAction);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 220.0);
                fabMenu.resizeRelocate(0.0, 0.0, 160.0, 160.0);
                root.layout();

                assertFalse(fabMenu.isExpanded());
                assertFalse(tooltip.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(fabMenu.isExpanded());
                assertTrue(ownerAction.isVisible());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that FAB-menu rich tooltip reveal rejects unreachable action targets.
    @Test
    void surfaceRejectsUnreachableNestedFabMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3FloatingActionButton ownerAction = new M3FloatingActionButton("Edit");
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Edit",
                    "Edits the selected item.",
                    tooltipAction
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.addItem(ownerAction);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = new M3Surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 220.0);
                fabMenu.resizeRelocate(0.0, 0.0, 160.0, 160.0);
                root.layout();

                assertFalse(fabMenu.isExpanded());
                assertFalse(tooltip.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertFalse(fabMenu.isExpanded());
                assertFalse(ownerAction.isVisible());
                assertFalse(tooltip.isShowing());
                assertFalse(tooltipAction.isFocused());
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose hosted snackbar action focus.
    @Test
    void surfaceRoutesFocusThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            M3Surface surface = new M3Surface(host);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 520.0, 160.0);
                host.resizeRelocate(0.0, 0.0, 480.0, 96.0);
                root.layout();

                host.show(snackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, actionButton);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active snackbar rich tooltip action focus.
    @Test
    void surfaceRoutesFocusThroughNestedSnackbarRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            M3Surface surface = new M3Surface(host);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 520.0, 160.0);
                host.resizeRelocate(0.0, 0.0, 480.0, 96.0);
                root.layout();

                host.show(snackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = M3RichTooltip.install(
                        actionButton,
                        "Undo",
                        "Restores the previous item state.",
                        tooltipAction
                );

                try {
                    actionButton.requestFocus();
                    tooltip.show(actionButton, stage.getX() + 216.0, stage.getY() + 144.0);

                    assertTrue(host.isShowing());
                    assertTrue(tooltip.isShowing());
                    assertTrue(actionButton.isFocused());
                    assertSame(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                    actionButton.fireEvent(keyPressed(KeyCode.F6));

                    assertTrue(tooltipAction.isFocused());
                    assertTrue(tooltip.isShowing());
                    assertTrue(host.isShowing());
                    assertSame(tooltipAction, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertPopupFocusRoutedByContainer(surface, tooltipAction);

                    tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                    assertFalse(tooltip.isShowing());
                    assertTrue(actionButton.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal snackbar rich tooltip actions from explicit actions.
    @Test
    void surfaceRevealsNestedSnackbarRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            M3Surface surface = new M3Surface(host);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 520.0, 160.0);
                host.resizeRelocate(0.0, 0.0, 480.0, 96.0);
                root.layout();

                host.show(snackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = M3RichTooltip.install(
                        actionButton,
                        "Undo",
                        "Restores the previous item state.",
                        tooltipAction
                );

                try {
                    assertFalse(tooltip.isShowing());

                    surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(tooltipAction, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that snackbar rich tooltip reveal rejects unreachable action targets.
    @Test
    void surfaceRejectsUnreachableNestedSnackbarRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            M3Surface surface = new M3Surface(host);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 520.0, 160.0);
                host.resizeRelocate(0.0, 0.0, 480.0, 96.0);
                root.layout();

                host.show(snackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                tooltipAction.setDisable(true);
                M3RichTooltip tooltip = M3RichTooltip.install(
                        actionButton,
                        "Undo",
                        "Restores the previous item state.",
                        tooltipAction
                );

                try {
                    assertFalse(tooltip.isShowing());

                    surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipAction.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active banner rich tooltip action focus.
    @Test
    void surfaceRoutesFocusThroughNestedBannerRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Retry");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Retry",
                    "Retries the interrupted task.",
                    tooltipAction
            );
            M3Banner banner = new M3Banner("Connection interrupted", ownerAction);
            banner.setIcon(new M3Icon("!"));
            M3Surface surface = new M3Surface(banner);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                banner.resizeRelocate(0.0, 0.0, 560.0, 96.0);
                root.layout();

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 296.0, stage.getY() + 144.0);

                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertSame(tooltipAction, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal banner rich tooltip actions from explicit actions.
    @Test
    void surfaceRevealsNestedBannerRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Retry");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Retry",
                    "Retries the interrupted task.",
                    tooltipAction
            );
            M3Banner banner = new M3Banner("Connection interrupted", ownerAction);
            banner.setIcon(new M3Icon("!"));
            M3Surface surface = new M3Surface(banner);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                banner.resizeRelocate(0.0, 0.0, 560.0, 96.0);
                root.layout();

                assertFalse(tooltip.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that banner rich tooltip reveal rejects unreachable action targets.
    @Test
    void surfaceRejectsUnreachableNestedBannerRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Retry");
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Retry",
                    "Retries the interrupted task.",
                    tooltipAction
            );
            M3Banner banner = new M3Banner("Connection interrupted", ownerAction);
            banner.setIcon(new M3Icon("!"));
            M3Surface surface = new M3Surface(banner);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                banner.resizeRelocate(0.0, 0.0, 560.0, 96.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                ownerAction.requestFocus();
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipAction.isFocused());
                assertSame(ownerAction, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active card rich tooltip action focus.
    @Test
    void surfaceRoutesFocusThroughNestedCardRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Open");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Open",
                    "Opens the selected card item.",
                    tooltipAction
            );
            M3Card card = new M3Card(ownerAction);
            M3Surface surface = new M3Surface(card);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                card.resizeRelocate(0.0, 0.0, 260.0, 112.0);
                root.layout();

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 240.0, stage.getY() + 144.0);

                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertSame(tooltipAction, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal card rich tooltip actions from explicit actions.
    @Test
    void surfaceRevealsNestedCardRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Open");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Open",
                    "Opens the selected card item.",
                    tooltipAction
            );
            M3Card card = new M3Card(ownerAction);
            M3Surface surface = new M3Surface(card);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                card.resizeRelocate(0.0, 0.0, 260.0, 112.0);
                root.layout();

                assertFalse(tooltip.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that card rich tooltip reveal rejects unreachable action targets.
    @Test
    void surfaceRejectsUnreachableNestedCardRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Open");
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Open",
                    "Opens the selected card item.",
                    tooltipAction
            );
            M3Card card = new M3Card(ownerAction);
            M3Surface surface = new M3Surface(card);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                card.resizeRelocate(0.0, 0.0, 260.0, 112.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                ownerAction.requestFocus();
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipAction.isFocused());
                assertSame(ownerAction, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees expose active list item slot rich tooltip action focus.
    @Test
    void surfaceRoutesFocusThroughNestedListItemRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows additional list item actions.",
                    tooltipAction
            );
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setTrailing(ownerAction);
            M3Surface surface = new M3Surface(listItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                listItem.resizeRelocate(0.0, 0.0, 420.0, 72.0);
                root.layout();

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 320.0, stage.getY() + 144.0);

                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertSame(tooltipAction, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal list item slot rich tooltip actions from explicit actions.
    @Test
    void surfaceRevealsNestedListItemRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows additional list item actions.",
                    tooltipAction
            );
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setTrailing(ownerAction);
            M3Surface surface = new M3Surface(listItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                listItem.resizeRelocate(0.0, 0.0, 420.0, 72.0);
                root.layout();

                assertFalse(tooltip.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that list item slot rich tooltip reveal rejects unreachable action targets.
    @Test
    void surfaceRejectsUnreachableNestedListItemRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows additional list item actions.",
                    tooltipAction
            );
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setTrailing(ownerAction);
            M3Surface surface = new M3Surface(listItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 600.0, 160.0);
                listItem.resizeRelocate(0.0, 0.0, 420.0, 72.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                ownerAction.requestFocus();
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipAction.isFocused());
                assertSame(ownerAction, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that a hidden side sheet can reveal a nested submenu target from its content subtree.
    @Test
    void hiddenSideSheetRevealsNestedContentMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3SubMenuItem moveItem = new M3SubMenuItem("Move to", archiveItem);
            M3MenuButton menuButton = new M3MenuButton("Actions", moveItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(260.0, 88.0);
            M3SideSheet sheet = new M3SideSheet("Details", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 320.0, 260.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertFalse(menuButton.isShowing());
                assertFalse(moveItem.isSubMenuShowing());

                sheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);
                root.layout();

                assertTrue(sheet.isShown());
                assertTrue(menuButton.isShowing());
                assertTrue(moveItem.isSubMenuShowing());
                assertTrue(archiveItem.isFocused());
                assertPopupFocusRoutedByContainer(sheet, archiveItem);
            } finally {
                moveItem.hideSubMenu();
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that a hidden bottom sheet can reveal a rich tooltip action from its content subtree.
    @Test
    void hiddenBottomSheetRevealsNestedContentRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows additional sheet details.",
                    tooltipAction
            );
            Pane content = new Pane(ownerAction);
            content.setPrefSize(260.0, 88.0);
            M3BottomSheet sheet = new M3BottomSheet("Queue", content);
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
                ownerAction.resizeRelocate(0.0, 0.0, 160.0, 48.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertFalse(tooltip.isShowing());

                sheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertTrue(sheet.isShown());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(sheet, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that search bars reveal trailing menu popup targets from explicit accessibility actions.
    @Test
    void searchBarRevealsNestedTrailingMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setTrailingActions(menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchBar);
                Scene scene = new Scene(root, 560.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchBar.resizeRelocate(32.0, 32.0, 420.0, 56.0);
                root.layout();

                assertFalse(menuButton.isShowing());

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(searchBar.isActive());
                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(searchBar, archiveItem);
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that search bars reveal trailing rich tooltip actions from explicit accessibility actions.
    @Test
    void searchBarRevealsNestedTrailingRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows more search options.",
                    tooltipAction
            );
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setTrailingActions(ownerAction);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchBar);
                Scene scene = new Scene(root, 560.0, 180.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchBar.resizeRelocate(32.0, 32.0, 420.0, 56.0);
                root.layout();

                assertFalse(tooltip.isShowing());

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(searchBar.isActive());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(searchBar, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that search views expose active menu popup focus from result rows.
    @Test
    void searchViewRoutesFocusThroughNestedResultMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(menuButton);
            M3SearchView searchView = new M3SearchView("Search", result);
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

    /// Verifies that search views reveal nested menu popup targets from result rows.
    @Test
    void searchViewRevealsNestedResultMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(menuButton);
            M3SearchView searchView = new M3SearchView("Search", result);
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

                assertFalse(menuButton.isShowing());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(searchView.isActive());
                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(searchView, archiveItem);
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that search views reveal nested rich tooltip actions from result rows.
    @Test
    void searchViewRevealsNestedResultRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows result details.",
                    tooltipAction
            );
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(ownerAction);
            M3SearchView searchView = new M3SearchView("Search", result);
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

                assertFalse(tooltip.isShowing());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(searchView.isActive());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, result.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(searchView, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that search views reveal rich tooltip actions owned by the embedded search bar.
    @Test
    void searchViewRevealsNestedSearchBarTrailingRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows search view options.",
                    tooltipAction
            );
            M3SearchView searchView = new M3SearchView("Search");
            searchView.setTrailingActions(ownerAction);
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

                assertFalse(tooltip.isShowing());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(searchView.isActive());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(searchView, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that search views reveal date range picker targets owned by the embedded search bar.
    @Test
    void searchViewRevealsNestedSearchBarDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3SearchView searchView = new M3SearchView("Search schedule");
            searchView.setTrailingActions(field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchView);
                Scene scene = new Scene(root, 820.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchView.resizeRelocate(32.0, 32.0, 720.0, 220.0);
                root.layout();

                assertDateRangePickerValueTargetRoutedByContainer(
                        searchView,
                        field,
                        LocalDate.of(2026, 6, 23)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search views reveal date range picker targets from result content.
    @Test
    void searchViewRevealsNestedResultDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            Pane result = new Pane(field);
            result.setPrefSize(640.0, 96.0);
            M3SearchView searchView = new M3SearchView("Search schedule", result);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchView);
                Scene scene = new Scene(root, 820.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchView.resizeRelocate(32.0, 32.0, 720.0, 260.0);
                field.resizeRelocate(0.0, 0.0, 600.0, 72.0);
                root.layout();

                assertDateRangePickerValueTargetRoutedByContainer(
                        searchView,
                        field,
                        LocalDate.of(2026, 6, 24)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that collapsed navigation drawer groups reveal nested child rich tooltip actions.
    @Test
    void navigationDrawerGroupRevealsNestedChildRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(ownerAction);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.addItem(childItem);
            group.setExpanded(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 420.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 320.0, 160.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertFalse(tooltip.isShowing());

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertTrue(group.isExpanded());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, childItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(group, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reveal nested menu popup targets from collapsed group child rows.
    @Test
    void navigationDrawerRevealsNestedCollapsedGroupMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(menuButton);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.addItem(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
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
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(drawer, archiveItem);
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reveal nested rich tooltip actions from collapsed group child rows.
    @Test
    void navigationDrawerRevealsNestedCollapsedGroupRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(ownerAction);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.addItem(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = new M3NavigationDrawer(group);
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
                assertFalse(tooltip.isShowing());

                drawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertTrue(group.isExpanded());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, childItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(drawer, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that slot-based containers reveal nested popup targets from explicit accessibility actions.
    @Test
    void slotContainersRevealNestedMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3TopAppBar appBar = new M3TopAppBar("Project", (Node) null, menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 720.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 0.0, 640.0, 72.0);
                root.layout();

                assertFalse(menuButton.isShowing());

                appBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, appBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that slot-based containers expose active popup focus owned by action nodes.
    @Test
    void slotContainersRouteFocusThroughNestedMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3TopAppBar appBar = new M3TopAppBar("Project", (Node) null, menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 720.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 0.0, 640.0, 72.0);
                root.layout();

                menuButton.showMenu();
                archiveItem.requestFocus();

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertPopupFocusRoutedByContainer(appBar, archiveItem);

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, appBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that top app bars reveal rich tooltip actions owned by navigation slots.
    @Test
    void topAppBarRevealsNavigationRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button navigation = new M3Button("Nav");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    navigation,
                    "Navigation",
                    "Shows navigation details.",
                    tooltipAction
            );
            M3TopAppBar appBar = new M3TopAppBar("Project", navigation);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 720.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 0.0, 640.0, 72.0);
                root.layout();

                assertFalse(tooltip.isShowing());

                appBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(appBar, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that top app bars route rich tooltip action focus from trailing action slots.
    @Test
    void topAppBarRoutesFocusThroughActionRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows project actions.",
                    tooltipAction
            );
            M3TopAppBar appBar = new M3TopAppBar("Project", (Node) null, ownerAction);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 720.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 0.0, 640.0, 72.0);
                root.layout();

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 520.0, stage.getY() + 112.0);

                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, appBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertPopupFocusRoutedByContainer(appBar, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, appBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that bottom app bars reveal menu targets owned by the floating action slot.
    @Test
    void bottomAppBarRevealsFloatingActionMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton floatingAction = new M3MenuButton("Create", archiveItem);
            M3BottomAppBar appBar = new M3BottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    new M3Button("Search")
            );
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 720.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 120.0, 640.0, 96.0);
                root.layout();

                assertFalse(floatingAction.isShowing());

                appBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(floatingAction.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, floatingAction.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(appBar, archiveItem);
            } finally {
                floatingAction.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that bottom app bars route rich tooltip action focus from the floating action slot.
    @Test
    void bottomAppBarRoutesFocusThroughFloatingActionRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button floatingAction = new M3Button("Create");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    floatingAction,
                    "Create",
                    "Shows creation details.",
                    tooltipAction
            );
            M3BottomAppBar appBar = new M3BottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    new M3Button("Search")
            );
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 720.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 120.0, 640.0, 96.0);
                root.layout();

                floatingAction.requestFocus();
                tooltip.show(floatingAction, stage.getX() + 520.0, stage.getY() + 160.0);

                assertTrue(tooltip.isShowing());
                assertTrue(floatingAction.isFocused());
                assertSame(floatingAction, appBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                floatingAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertPopupFocusRoutedByContainer(appBar, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(floatingAction.isFocused());
                assertSame(floatingAction, appBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that toolbars reveal rich tooltip actions owned by toolbar items.
    @Test
    void toolbarRevealsNestedRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Format");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Format",
                    "Shows formatting details.",
                    tooltipAction
            );
            M3Toolbar toolbar = new M3Toolbar(ownerAction, new M3Button("Share"));
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

                assertFalse(tooltip.isShowing());

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(toolbar, tooltipAction);
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that toolbars route rich tooltip action focus from currently focused toolbar items.
    @Test
    void toolbarRoutesFocusThroughNestedRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Format");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Format",
                    "Shows formatting details.",
                    tooltipAction
            );
            M3Toolbar toolbar = new M3Toolbar(ownerAction, new M3Button("Share"));
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

                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

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
                stage.close();
            }
        });
    }

    /// Verifies that top app bar action slots reveal picker value targets and expose popup focus.
    @Test
    void topAppBarRevealsNestedDatePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 18);
            M3TopAppBar appBar = new M3TopAppBar("Schedule", (Node) null, field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 0.0, 700.0, 88.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(appBar, field, targetDate);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that bottom app bar action slots reveal picker value targets and expose popup focus.
    @Test
    void bottomAppBarRevealsNestedDatePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 20);
            M3BottomAppBar appBar = new M3BottomAppBar(new M3Button("Search"), field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 160.0, 700.0, 96.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(appBar, field, targetDate);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that toolbar item slots reveal picker value targets and expose popup focus.
    @Test
    void toolbarRevealsNestedTimePickerValueTargetFromItemSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(10, 45);
            M3Toolbar toolbar = new M3Toolbar(new M3Button("Back"), field, new M3Button("Save"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 520.0, 80.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(toolbar, field, targetTime);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search bar trailing action slots reveal picker value targets and expose popup focus.
    @Test
    void searchBarRevealsNestedDatePickerValueTargetFromTrailingAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 21);
            M3SearchBar searchBar = new M3SearchBar("Search schedule");
            searchBar.setTrailingActions(field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchBar);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchBar.resizeRelocate(32.0, 32.0, 640.0, 72.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(searchBar, field, targetDate);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that top app bar action slots preserve date range endpoint focus through nested popup routing.
    @Test
    void topAppBarRevealsNestedDateRangePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3TopAppBar appBar = new M3TopAppBar("Schedule", (Node) null, field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 820.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 0.0, 760.0, 96.0);
                root.layout();

                assertDateRangePickerValueTargetRoutedByContainer(
                        appBar,
                        field,
                        LocalDate.of(2026, 6, 20)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that bottom app bar action slots preserve date range endpoint focus through nested popup routing.
    @Test
    void bottomAppBarRevealsNestedDateRangePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3BottomAppBar appBar = new M3BottomAppBar(new M3Button("Search"), field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(appBar);
                Scene scene = new Scene(root, 820.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                appBar.resizeRelocate(0.0, 200.0, 760.0, 96.0);
                root.layout();

                assertDateRangePickerValueTargetRoutedByContainer(
                        appBar,
                        field,
                        LocalDate.of(2026, 6, 21)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that toolbar item slots preserve date range endpoint focus through nested popup routing.
    @Test
    void toolbarRevealsNestedDateRangePickerValueTargetFromItemSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3Toolbar toolbar = new M3Toolbar(new M3Button("Back"), field, new M3Button("Save"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 820.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 640.0, 96.0);
                root.layout();

                assertDateRangePickerValueTargetRoutedByContainer(
                        toolbar,
                        field,
                        LocalDate.of(2026, 6, 22)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search bar trailing slots preserve date range endpoint focus through nested popup routing.
    @Test
    void searchBarRevealsNestedDateRangePickerValueTargetFromTrailingAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3SearchBar searchBar = new M3SearchBar("Search schedule");
            searchBar.setTrailingActions(field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchBar);
                Scene scene = new Scene(root, 820.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchBar.resizeRelocate(32.0, 32.0, 720.0, 80.0);
                root.layout();

                assertDateRangePickerValueTargetRoutedByContainer(
                        searchBar,
                        field,
                        LocalDate.of(2026, 6, 23)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Creates a key press event for popup keyboard tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /// Verifies picker reveal, popup focus routing, and Escape restoration through a menu button submenu branch.
    private static void assertNestedSubMenuPickerValueTargetRoutedByContainer(
            Node container,
            M3MenuButton menuButton,
            M3SubMenuItem subMenuItem,
            M3PickerField<?, ?> field,
            Object valueTarget
    ) {
        assertFalse(menuButton.isShowing());
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                Node.class,
                field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
        ));
        assertTrue(menuButton.isShowing());
        assertTrue(subMenuItem.isSubMenuShowing());
        assertTrue(field.isShowing());
        assertTrue(pickerFocusNode.isFocused());
        assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
        assertSame(pickerFocusNode, subMenuItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(pickerFocusNode, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertPopupFocusRoutedByContainer(container, pickerFocusNode);

        field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

        assertFalse(field.isShowing());
        assertTrue(subMenuItem.isSubMenuShowing());
        assertTrue(menuButton.isShowing());
        assertTrue(field.getEditor().isFocused());
        assertSame(field.getEditor(), subMenuItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(field.getEditor(), menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(field.getEditor(), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        field.getEditor().fireEvent(keyPressed(KeyCode.ESCAPE));

        assertFalse(subMenuItem.isSubMenuShowing());
        assertTrue(menuButton.isShowing());
        assertTrue(subMenuItem.isFocused());
        assertSame(subMenuItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(subMenuItem, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        subMenuItem.fireEvent(keyPressed(KeyCode.ESCAPE));

        assertFalse(menuButton.isShowing());
        assertTrue(menuButton.isFocused());
        assertSame(menuButton, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies date range picker reveal and endpoint restoration through an already open submenu branch.
    private static void assertNestedSubMenuDateRangePickerValueTargetRoutedByContainer(
            Node container,
            M3MenuButton menuButton,
            M3SubMenuItem subMenuItem,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(menuButton.isShowing());
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());

        menuButton.showMenu();
        subMenuItem.showSubMenu();
        field.getEndEditor().requestFocus();

        assertTrue(menuButton.isShowing());
        assertTrue(subMenuItem.isSubMenuShowing());
        assertTrue(field.getEndEditor().isFocused());
        assertSame(field.getEndEditor(), subMenuItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(field.getEndEditor(), menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(field.getEndEditor(), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                Node.class,
                field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
        ));
        assertTrue(menuButton.isShowing());
        assertTrue(subMenuItem.isSubMenuShowing());
        assertTrue(field.isShowing());
        assertTrue(pickerFocusNode.isFocused());
        assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
        assertSame(pickerFocusNode, subMenuItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(pickerFocusNode, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertPopupFocusRoutedByContainer(container, pickerFocusNode);

        field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

        assertFalse(field.isShowing());
        assertTrue(subMenuItem.isSubMenuShowing());
        assertTrue(menuButton.isShowing());
        assertTrue(field.getEndEditor().isFocused());
        assertSame(field.getEndEditor(), subMenuItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(field.getEndEditor(), menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(field.getEndEditor(), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies that a composite owner reveals a picker value target and preserves its popup focus target.
    private static void assertPickerValueTargetRoutedByContainer(
            Node container,
            M3PickerField<?, ?> field,
            Object valueTarget
    ) {
        assertFalse(field.isShowing());

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                Node.class,
                field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
        ));
        assertTrue(field.isShowing());
        assertTrue(pickerFocusNode.isFocused());
        assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
        assertPopupFocusRoutedByContainer(container, pickerFocusNode);

        field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

        assertFalse(field.isShowing());
        assertTrue(field.getEditor().isFocused());
        assertSame(field.getEditor(), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies that a composite owner reveals a date range picker target and preserves its owning endpoint.
    private static void assertDateRangePickerValueTargetRoutedByContainer(
            Node container,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(field.isShowing());

        field.getEndEditor().requestFocus();

        assertTrue(field.getEndEditor().isFocused());
        assertSame(field.getEndEditor(), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                Node.class,
                field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
        ));
        assertTrue(field.isShowing());
        assertTrue(pickerFocusNode.isFocused());
        assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
        assertPopupFocusRoutedByContainer(container, pickerFocusNode);

        field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

        assertFalse(field.isShowing());
        assertTrue(field.getEndEditor().isFocused());
        assertSame(field.getEndEditor(), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies that a composite owner preserves and reveals an active external popup focus target.
    private static void assertPopupFocusRoutedByContainer(Node container, Node focusTarget) {
        assertSame(focusTarget, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        container.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

        assertTrue(focusTarget.isFocused());
        assertSame(focusTarget, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

        assertTrue(focusTarget.isFocused());
        assertSame(focusTarget, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }
}
