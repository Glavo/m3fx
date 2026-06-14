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
                assertSame(detailsItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

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
                assertSame(pdfItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                action.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(pdfItem.isFocused());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertSame(pdfItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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

    /// Creates a key press event for popup keyboard tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
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
