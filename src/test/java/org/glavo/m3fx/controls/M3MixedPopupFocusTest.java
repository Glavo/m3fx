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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static org.glavo.m3fx.M3TestControls.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
            M3RichTooltip tooltip = installRichTooltip(
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

                action.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(detailsItem.isFocused());
                assertTrue(tooltip.isShowing());
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
            M3Surface surface = surface(content);
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

    /// Verifies that an active nested submenu rich tooltip focus chain survives runtime orientation changes.
    @Test
    void richTooltipInsideNestedSubMenuPreservesFocusThroughRuntimeOrientationChanges() {
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
            M3Surface surface = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
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

                pdfItem.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(action.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertNestedMenuStackOrientation(menuButton, exportItem, NodeOrientation.LEFT_TO_RIGHT);
                assertNestedMenuStackOrientation(exportItem, recentItem, NodeOrientation.LEFT_TO_RIGHT);
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, action.getEffectiveNodeOrientation());
                assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, action);

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                assertTrue(action.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertNestedMenuStackOrientation(menuButton, exportItem, NodeOrientation.RIGHT_TO_LEFT);
                assertNestedMenuStackOrientation(exportItem, recentItem, NodeOrientation.RIGHT_TO_LEFT);
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, action.getEffectiveNodeOrientation());
                assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(action, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, action);

                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.applyCss();
                root.layout();

                assertTrue(action.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(menuButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertNestedMenuStackOrientation(menuButton, exportItem, NodeOrientation.LEFT_TO_RIGHT);
                assertNestedMenuStackOrientation(exportItem, recentItem, NodeOrientation.LEFT_TO_RIGHT);
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, action.getEffectiveNodeOrientation());
                assertPopupFocusRoutedByContainer(surface, action);

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
            M3Surface surface = surface(content);
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
            M3Surface surface = surface(content);
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

    /// Verifies that validation summaries reveal invalid-input adornment rich tooltip actions.
    @Test
    void validationSummaryRevealsInvalidInputAdornmentRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TextField nameField = new M3TextField();
            M3IconButton helpButton = new M3IconButton(new M3Icon("?"));
            M3Button tooltipAction = new M3Button("Explain");
            M3RichTooltip tooltip = installRichTooltip(
                    helpButton,
                    "Name help",
                    "Explains why the field is invalid.",
                    tooltipAction
            );
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Name", "Required");
            nameLayout.setTrailing(helpButton);
            nameLayout.setValidator(M3TextInputValidators.required("Name is required"));

            M3TextField emailField = new M3TextField();
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Required");
            emailLayout.setValidator(M3TextInputValidators.required("Email is required"));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            Stage stage = new Stage();

            try {
                assertFalse(validator.validate());

                Pane root = new Pane(nameLayout, emailLayout, summary);
                Scene scene = new Scene(root, 620.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                nameLayout.resizeRelocate(32.0, 24.0, 360.0, 88.0);
                emailLayout.resizeRelocate(32.0, 128.0, 360.0, 88.0);
                summary.resizeRelocate(32.0, 236.0, 420.0, 120.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(summary, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(helpButton.isFocused());
                assertSame(helpButton, nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(helpButton, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that validation summaries reveal invalid-input adornment menu popup targets.
    @Test
    void validationSummaryRevealsInvalidInputAdornmentMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TextField nameField = new M3TextField();
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Name", "Required");
            nameLayout.setTrailing(menuButton);
            nameLayout.setValidator(M3TextInputValidators.required("Name is required"));

            M3TextField emailField = new M3TextField();
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Required");
            emailLayout.setValidator(M3TextInputValidators.required("Email is required"));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            Stage stage = new Stage();

            try {
                assertFalse(validator.validate());

                Pane root = new Pane(nameLayout, emailLayout, summary);
                Scene scene = new Scene(root, 680.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                nameLayout.resizeRelocate(32.0, 24.0, 460.0, 88.0);
                emailLayout.resizeRelocate(32.0, 128.0, 360.0, 88.0);
                summary.resizeRelocate(32.0, 236.0, 460.0, 120.0);
                root.layout();

                assertFalse(menuButton.isShowing());
                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);
                root.layout();

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(summary, archiveItem);

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(menuButton, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that validation summaries reveal invalid-input adornment picker value targets.
    @Test
    void validationSummaryRevealsInvalidInputAdornmentPickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TextField nameField = new M3TextField();
            LocalDate targetDate = LocalDate.of(2026, 6, 23);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 16));
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Name", "Required");
            nameLayout.setTrailing(field);
            nameLayout.setValidator(M3TextInputValidators.required("Name is required"));

            M3TextField emailField = new M3TextField();
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Required");
            emailLayout.setValidator(M3TextInputValidators.required("Email is required"));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            Stage stage = new Stage();

            try {
                assertFalse(validator.validate());

                Pane root = new Pane(nameLayout, emailLayout, summary);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                nameLayout.resizeRelocate(32.0, 24.0, 580.0, 88.0);
                emailLayout.resizeRelocate(32.0, 128.0, 360.0, 88.0);
                summary.resizeRelocate(32.0, 236.0, 460.0, 120.0);
                root.layout();

                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertPickerValueTargetRoutedByContainer(summary, field, targetDate);

                assertSame(field.getEditor(), nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }


    /// Verifies that validation summaries reveal invalid-input adornment time picker value targets.
    @Test
    void validationSummaryRevealsInvalidInputAdornmentTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TextField nameField = new M3TextField();
            LocalTime targetTime = LocalTime.of(10, 45);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Name", "Required");
            nameLayout.setTrailing(field);
            nameLayout.setValidator(M3TextInputValidators.required("Name is required"));

            M3TextField emailField = new M3TextField();
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Required");
            emailLayout.setValidator(M3TextInputValidators.required("Email is required"));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            Stage stage = new Stage();

            try {
                assertFalse(validator.validate());

                Pane root = new Pane(nameLayout, emailLayout, summary);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                nameLayout.resizeRelocate(32.0, 24.0, 580.0, 88.0);
                emailLayout.resizeRelocate(32.0, 128.0, 360.0, 88.0);
                summary.resizeRelocate(32.0, 236.0, 460.0, 120.0);
                root.layout();

                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertPickerValueTargetRoutedByContainer(summary, field, targetTime);

                assertSame(field.getEditor(), nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that validation summaries reveal invalid-input adornment date range picker value targets.
    @Test
    void validationSummaryRevealsInvalidInputAdornmentDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TextField nameField = new M3TextField();
            LocalDate targetDate = LocalDate.of(2026, 6, 27);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Name", "Required");
            nameLayout.setTrailing(field);
            nameLayout.setValidator(M3TextInputValidators.required("Name is required"));

            M3TextField emailField = new M3TextField();
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Required");
            emailLayout.setValidator(M3TextInputValidators.required("Email is required"));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            Stage stage = new Stage();

            try {
                assertFalse(validator.validate());

                Pane root = new Pane(nameLayout, emailLayout, summary);
                Scene scene = new Scene(root, 840.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                nameLayout.resizeRelocate(32.0, 24.0, 680.0, 88.0);
                emailLayout.resizeRelocate(32.0, 128.0, 360.0, 88.0);
                summary.resizeRelocate(32.0, 236.0, 500.0, 120.0);
                root.layout();

                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertDateRangePickerValueTargetRoutedByContainer(summary, field, targetDate);

                assertSame(field.getEndEditor(), nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that text input layouts reject disabled trailing tooltip actions without focusing the input.
    @Test
    void textInputLayoutRejectsDisabledTrailingRichTooltipActionTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3TextField field = new M3TextField("M3FX");
            M3Button helpButton = new M3Button("Help");
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            M3RichTooltip tooltip = installRichTooltip(
                    helpButton,
                    "Help",
                    "Explains this input.",
                    tooltipAction
            );
            M3TextInputLayout layout = new M3TextInputLayout(field, "Project", "Required");
            layout.setTrailing(helpButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(outside, layout);
                Scene scene = new Scene(root, 620.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(24.0, 24.0, 120.0, 48.0);
                layout.resizeRelocate(32.0, 96.0, 440.0, 88.0);
                root.layout();

                outside.requestFocus();
                assertTrue(outside.isFocused());
                assertFalse(tooltip.isShowing());

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipAction.isFocused());
                assertFalse(field.isFocused());
                assertFalse(helpButton.isFocused());
                assertSame(outside, scene.getFocusOwner());
                assertNotSame(tooltipAction, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(helpButton, tooltip);
                stage.close();
            }
        });
    }
    /// Verifies that validation summaries reject explicit invalid-input descendants that are not reachable.
    @Test
    void validationSummaryRejectsUnreachableInvalidInputAdornmentTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3TextField nameField = new M3TextField();
            M3Button visibleAction = new M3Button("Visible");
            M3Button hiddenAction = new M3Button("Hidden");
            hiddenAction.setVisible(false);
            M3Button disabledAction = new M3Button("Disabled");
            disabledAction.setDisable(true);
            StackPane trailingActions = new StackPane(visibleAction, hiddenAction, disabledAction);
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Name", "Required");
            nameLayout.setTrailing(trailingActions);
            nameLayout.setValidator(M3TextInputValidators.required("Name is required"));

            M3FormValidator validator = new M3FormValidator(nameLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            Stage stage = new Stage();

            try {
                assertFalse(validator.validate());

                Pane root = new Pane(outside, nameLayout, summary);
                Scene scene = new Scene(root, 620.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(32.0, 24.0, 120.0, 48.0);
                nameLayout.resizeRelocate(32.0, 96.0, 440.0, 88.0);
                summary.resizeRelocate(32.0, 212.0, 440.0, 88.0);
                root.layout();

                outside.requestFocus();
                assertTrue(outside.isFocused());

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenAction);
                root.layout();

                assertTrue(outside.isFocused());
                assertFalse(nameField.isFocused());
                assertFalse(hiddenAction.isFocused());

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledAction);
                root.layout();

                assertTrue(outside.isFocused());
                assertFalse(nameField.isFocused());
                assertFalse(disabledAction.isFocused());

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, visibleAction);
                root.layout();

                assertTrue(visibleAction.isFocused());
                assertSame(visibleAction, nameLayout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(visibleAction, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }
    /// Verifies that nested form containers reveal and route menu, tooltip, and picker popup targets.
    @Test
    void formContainersRevealNestedPopupTargetsAcrossRows() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);

            M3Button helpButton = new M3Button("Help");
            M3Button tooltipAction = new M3Button("Explain");
            M3RichTooltip tooltip = installRichTooltip(
                    helpButton,
                    "Form help",
                    "Explains how this form section is validated.",
                    tooltipAction
            );
            M3Button disabledHelpButton = new M3Button("Disabled help");
            M3Button disabledTooltipAction = new M3Button("Disabled explain");
            disabledTooltipAction.setDisable(true);
            M3RichTooltip disabledTooltip = installRichTooltip(
                    disabledHelpButton,
                    "Disabled form help",
                    "Disabled tooltip actions cannot be revealed.",
                    disabledTooltipAction
            );

            LocalDate targetDate = LocalDate.of(2026, 7, 6);
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 7, 1));
            HBox helpActions = new HBox(helpButton, disabledHelpButton);
            M3FormRow actionRow = new M3FormRow("Actions", "Menu and help affordances", menuButton, helpActions);
            M3FormRow dateRow = new M3FormRow("Due date", "Date picker target", dateField);
            M3FormSection section = formSection("Project", actionRow, dateRow);
            M3FormPane form = formPane(section);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(form);
                Scene scene = new Scene(root, 760.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                form.resizeRelocate(32.0, 32.0, 620.0, 300.0);
                root.layout();

                assertFalse(menuButton.isShowing());
                assertFalse(tooltip.isShowing());
                assertFalse(dateField.isShowing());

                form.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, actionRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, section.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(form, archiveItem);

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, actionRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(menuButton, form.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                form.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, actionRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, section.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(form, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(helpButton.isFocused());
                assertSame(helpButton, actionRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(helpButton, form.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                form.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledTooltipAction);
                root.layout();

                assertFalse(disabledTooltip.isShowing());
                assertFalse(disabledTooltipAction.isFocused());
                assertFalse(disabledHelpButton.isFocused());
                assertTrue(helpButton.isFocused());
                assertSame(helpButton, actionRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(helpButton, form.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertPickerValueTargetRoutedByContainer(form, dateField, targetDate);

                assertSame(dateField.getEditor(), dateRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(dateField.getEditor(), section.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                dateField.hidePicker();
                tooltip.hide();
                disabledTooltip.hide();
                M3Tooltip.uninstall(helpButton, tooltip);
                M3Tooltip.uninstall(disabledHelpButton, disabledTooltip);
                menuButton.hideMenu();
                stage.close();
            }
        });
    }


    /// Verifies that nested form containers reveal time picker value targets across rows.
    @Test
    void formContainersRevealNestedTimePickerValueTargetAcrossRows() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalTime targetTime = LocalTime.of(11, 15);
            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(9, 30));
            M3FormRow timeRow = new M3FormRow("Reminder", "Time picker target", timeField);
            M3FormSection section = formSection("Project", timeRow);
            M3FormPane form = formPane(section);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(form);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                form.resizeRelocate(32.0, 32.0, 620.0, 220.0);
                root.layout();

                assertFalse(timeField.isShowing());

                assertPickerValueTargetRoutedByContainer(form, timeField, targetTime);

                assertSame(timeField.getEditor(), timeRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(timeField.getEditor(), section.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                timeField.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that nested form containers reveal date range picker value targets across rows.
    @Test
    void formContainersRevealNestedDateRangePickerValueTargetAcrossRows() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 7, 8);
            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 7, 1),
                    LocalDate.of(2026, 7, 5)
            );
            M3FormRow rangeRow = new M3FormRow("Window", "Date range picker target", rangeField);
            M3FormSection section = formSection("Project", rangeRow);
            M3FormPane form = formPane(section);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(form);
                Scene scene = new Scene(root, 840.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                form.resizeRelocate(32.0, 32.0, 720.0, 240.0);
                root.layout();

                assertFalse(rangeField.isShowing());

                assertDateRangePickerValueTargetRoutedByContainer(form, rangeField, targetDate);

                assertSame(rangeField.getEndEditor(), rangeRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(rangeField.getEndEditor(), section.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                rangeField.hidePicker();
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

    /// Verifies that menu buttons reject disabled direct picker value targets before opening their popup.
    @Test
    void menuButtonRejectsDisabledDirectPickerValueTargetsBeforeOpeningMenu() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 8, 10));
            dateField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3MenuButton dateMenuButton = new M3MenuButton("Date", dateField);

            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 0));
            timeField.getPicker().setMaxTime(LocalTime.of(12, 0));
            M3MenuButton timeMenuButton = new M3MenuButton("Time", timeField);

            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 12)
            );
            rangeField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3MenuButton rangeMenuButton = new M3MenuButton("Range", rangeField);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dateMenuButton, timeMenuButton, rangeMenuButton);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dateMenuButton.resizeRelocate(32.0, 32.0, 180.0, 56.0);
                timeMenuButton.resizeRelocate(32.0, 104.0, 180.0, 56.0);
                rangeMenuButton.resizeRelocate(32.0, 176.0, 180.0, 56.0);
                root.layout();

                assertDirectMenuPickerValueTargetRejected(
                        dateMenuButton,
                        dateField,
                        LocalDate.of(2026, 8, 20)
                );
                assertDirectMenuPickerValueTargetRejected(
                        timeMenuButton,
                        timeField,
                        LocalTime.of(13, 0)
                );
                assertDirectMenuDateRangePickerValueTargetRejected(
                        rangeMenuButton,
                        rangeField,
                        LocalDate.of(2026, 8, 20)
                );
            } finally {
                dateMenuButton.hideMenu();
                timeMenuButton.hideMenu();
                rangeMenuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that menu buttons reject disabled nested picker value targets before opening any menu branch.
    @Test
    void menuButtonRejectsDisabledNestedPickerValueTargetsBeforeOpeningMenuBranch() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 8, 10));
            dateField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3SubMenuItem dateScheduleItem = new M3SubMenuItem("Date schedule", dateField);
            M3MenuButton dateMenuButton = new M3MenuButton("Date", dateScheduleItem);

            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 0));
            timeField.getPicker().setMaxTime(LocalTime.of(12, 0));
            M3SubMenuItem timeScheduleItem = new M3SubMenuItem("Time schedule", timeField);
            M3MenuButton timeMenuButton = new M3MenuButton("Time", timeScheduleItem);

            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 12)
            );
            rangeField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3SubMenuItem rangeScheduleItem = new M3SubMenuItem("Range schedule", rangeField);
            M3MenuButton rangeMenuButton = new M3MenuButton("Range", rangeScheduleItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dateMenuButton, timeMenuButton, rangeMenuButton);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dateMenuButton.resizeRelocate(32.0, 32.0, 180.0, 56.0);
                timeMenuButton.resizeRelocate(32.0, 104.0, 180.0, 56.0);
                rangeMenuButton.resizeRelocate(32.0, 176.0, 180.0, 56.0);
                root.layout();

                assertNestedMenuPickerValueTargetRejected(
                        dateMenuButton,
                        dateScheduleItem,
                        dateField,
                        LocalDate.of(2026, 8, 20)
                );
                assertNestedMenuPickerValueTargetRejected(
                        timeMenuButton,
                        timeScheduleItem,
                        timeField,
                        LocalTime.of(13, 0)
                );
                assertNestedMenuDateRangePickerValueTargetRejected(
                        rangeMenuButton,
                        rangeScheduleItem,
                        rangeField,
                        LocalDate.of(2026, 8, 20)
                );
            } finally {
                dateMenuButton.hideMenu();
                timeMenuButton.hideMenu();
                rangeMenuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that submenu items reject disabled picker value targets without opening their popup.
    @Test
    void subMenuItemRejectsDisabledPickerValueTargetsBeforeOpeningSubMenu() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 8, 10));
            dateField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3SubMenuItem dateScheduleItem = new M3SubMenuItem("Date schedule", dateField);

            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 0));
            timeField.getPicker().setMaxTime(LocalTime.of(12, 0));
            M3SubMenuItem timeScheduleItem = new M3SubMenuItem("Time schedule", timeField);

            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 12)
            );
            rangeField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3SubMenuItem rangeScheduleItem = new M3SubMenuItem("Range schedule", rangeField);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dateScheduleItem, timeScheduleItem, rangeScheduleItem);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dateScheduleItem.resizeRelocate(32.0, 32.0, 220.0, 56.0);
                timeScheduleItem.resizeRelocate(32.0, 104.0, 220.0, 56.0);
                rangeScheduleItem.resizeRelocate(32.0, 176.0, 220.0, 56.0);
                root.layout();

                assertSubMenuPickerValueTargetRejected(
                        dateScheduleItem,
                        dateField,
                        LocalDate.of(2026, 8, 20)
                );
                assertSubMenuPickerValueTargetRejected(
                        timeScheduleItem,
                        timeField,
                        LocalTime.of(13, 0)
                );
                assertSubMenuDateRangePickerValueTargetRejected(
                        rangeScheduleItem,
                        rangeField,
                        LocalDate.of(2026, 8, 20)
                );
            } finally {
                dateScheduleItem.hideSubMenu();
                timeScheduleItem.hideSubMenu();
                rangeScheduleItem.hideSubMenu();
                stage.close();
            }
        });
    }

    /// Verifies that menu owners reject unreachable node targets before opening popup branches.
    @Test
    void menuOwnersRejectUnreachableNodeTargetsBeforeOpeningPopupBranches() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenDirect = new M3MenuItem("Hidden direct");
            hiddenDirect.setVisible(false);
            M3MenuItem disabledDirect = new M3MenuItem("Disabled direct");
            disabledDirect.setDisable(true);

            M3Button hiddenNestedAction = new M3Button("Hidden nested action");
            hiddenNestedAction.setVisible(false);
            StackPane compositeDirect = new StackPane(hiddenNestedAction);
            compositeDirect.setPrefSize(160.0, 56.0);

            M3MenuItem hiddenBranchTarget = new M3MenuItem("Hidden branch target");
            M3SubMenuItem hiddenBranch = new M3SubMenuItem("Hidden branch", hiddenBranchTarget);
            hiddenBranch.setVisible(false);

            M3MenuItem disabledBranchTarget = new M3MenuItem("Disabled branch target");
            M3SubMenuItem disabledBranch = new M3SubMenuItem("Disabled branch", disabledBranchTarget);
            disabledBranch.setDisable(true);

            M3MenuItem hiddenNestedItem = new M3MenuItem("Hidden nested item");
            hiddenNestedItem.setVisible(false);
            M3MenuItem visibleNestedItem = new M3MenuItem("Visible nested item");
            M3SubMenuItem visibleBranch = new M3SubMenuItem("Visible branch", hiddenNestedItem, visibleNestedItem);

            M3MenuButton menuButton = new M3MenuButton(
                    "More",
                    hiddenDirect,
                    disabledDirect,
                    compositeDirect,
                    hiddenBranch,
                    disabledBranch,
                    visibleBranch
            );

            M3MenuItem standaloneHidden = new M3MenuItem("Standalone hidden");
            standaloneHidden.setVisible(false);
            M3MenuItem standaloneVisible = new M3MenuItem("Standalone visible");
            M3SubMenuItem standaloneSubMenu = new M3SubMenuItem(
                    "Standalone branch",
                    standaloneHidden,
                    standaloneVisible
            );

            M3MenuItem splitHidden = new M3MenuItem("Split hidden");
            splitHidden.setVisible(false);
            M3MenuItem splitVisible = new M3MenuItem("Split visible");
            M3SplitButton splitButton = splitButton("Create", splitHidden, splitVisible);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(menuButton, standaloneSubMenu, splitButton);
                Scene scene = new Scene(root, 820.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                menuButton.resizeRelocate(32.0, 32.0, 180.0, 56.0);
                standaloneSubMenu.resizeRelocate(32.0, 112.0, 220.0, 56.0);
                splitButton.resizeRelocate(32.0, 192.0, 220.0, 56.0);
                root.layout();

                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenDirect);
                assertFalse(menuButton.isShowing());
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertFalse(menuButton.isShowing());
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledDirect);
                assertFalse(menuButton.isShowing());
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenNestedAction);
                assertFalse(menuButton.isShowing());
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenBranchTarget);
                assertFalse(menuButton.isShowing());
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledBranchTarget);
                assertFalse(menuButton.isShowing());
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenNestedItem);
                assertFalse(menuButton.isShowing());
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 2, visibleNestedItem);
                assertFalse(menuButton.isShowing());

                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 5, visibleNestedItem);
                assertTrue(menuButton.isShowing());
                assertTrue(visibleBranch.isSubMenuShowing());
                assertTrue(visibleNestedItem.isFocused());
                menuButton.hideMenu();
                assertFalse(menuButton.isShowing());

                standaloneSubMenu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, standaloneHidden);
                assertFalse(standaloneSubMenu.isSubMenuShowing());
                standaloneSubMenu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertFalse(standaloneSubMenu.isSubMenuShowing());

                standaloneSubMenu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, standaloneVisible);
                assertTrue(standaloneSubMenu.isSubMenuShowing());
                assertTrue(standaloneVisible.isFocused());
                standaloneSubMenu.hideSubMenu();
                assertFalse(standaloneSubMenu.isSubMenuShowing());

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, splitHidden);
                assertFalse(splitButton.isShowing());

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, splitVisible);
                assertTrue(splitButton.isShowing());
                assertTrue(splitVisible.isFocused());
            } finally {
                menuButton.hideMenu();
                standaloneSubMenu.hideSubMenu();
                splitButton.hideMenu();
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
            M3Surface surface = surface(content);
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

    /// Verifies that an active nested submenu picker focus chain survives runtime orientation changes.
    @Test
    void nestedSubMenuPickerPopupPreservesFocusThroughRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 6, 22);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3MenuButton menuButton = new M3MenuButton("More", scheduleItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 144.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                root.layout();

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

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
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, field.getPicker().getEffectiveNodeOrientation());
                assertSame(pickerFocusNode, scheduleItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pickerFocusNode, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertNestedMenuStackOrientation(menuButton, scheduleItem, NodeOrientation.RIGHT_TO_LEFT);
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, field.getPicker().getEffectiveNodeOrientation());
                assertSame(pickerFocusNode, scheduleItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pickerFocusNode, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);

                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.applyCss();
                root.layout();

                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertNestedMenuStackOrientation(menuButton, scheduleItem, NodeOrientation.LEFT_TO_RIGHT);
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, field.getPicker().getEffectiveNodeOrientation());
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(scheduleItem.isSubMenuShowing());
                assertTrue(menuButton.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), scheduleItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(field.getEditor(), menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(field.getEditor(), surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                menuButton.hideMenu();
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
            M3RichTooltip tooltip = installRichTooltip(
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
                M3RichTooltip tooltip = installRichTooltip(
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
                M3RichTooltip tooltip = installRichTooltip(
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

    /// Verifies that dialog action rich tooltips reveal action-owned menu targets.
    @Test
    void dialogPaneRevealsMenuItemInsideActionRichTooltip() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3MenuItem targetItem = new M3MenuItem("Archive");
            M3MenuButton tooltipMenu = new M3MenuButton(
                    "More",
                    new M3MenuItem("Rename"),
                    targetItem
            );
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 560.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 400.0, 240.0);
                root.layout();

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = installRichTooltip(
                        okButton,
                        "Confirm",
                        "Dialog action help",
                        tooltipMenu
                );

                try {
                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipMenu.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetItem);
                    root.layout();

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipMenu.isShowing());
                    assertTrue(targetItem.isFocused());
                    assertSame(targetItem, tooltipMenu.getMenu().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(targetItem, tooltipMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(targetItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltipMenu.hideMenu();
                    tooltip.hide();
                    M3Tooltip.uninstall(okButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog action rich tooltips reject disabled action-owned menu targets before opening popups.
    @Test
    void dialogPaneRejectsDisabledMenuItemInsideActionRichTooltip() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3MenuItem disabledTarget = new M3MenuItem("Archive");
            disabledTarget.setDisable(true);
            M3MenuButton tooltipMenu = new M3MenuButton(
                    "More",
                    new M3MenuItem("Rename"),
                    disabledTarget
            );
            Stage stage = new Stage();

            try {
                Pane root = new Pane(outside, dialogPane);
                Scene scene = new Scene(root, 620.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(24.0, 24.0, 120.0, 48.0);
                dialogPane.resizeRelocate(32.0, 96.0, 400.0, 240.0);
                root.layout();

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = installRichTooltip(
                        okButton,
                        "Confirm",
                        "Dialog action help",
                        tooltipMenu
                );

                try {
                    outside.requestFocus();
                    assertTrue(outside.isFocused());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledTarget);
                    root.layout();

                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipMenu.isShowing());
                    assertFalse(disabledTarget.isFocused());
                    assertFalse(okButton.isFocused());
                    assertSame(outside, scene.getFocusOwner());
                    assertNotSame(disabledTarget, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltipMenu.hideMenu();
                    tooltip.hide();
                    M3Tooltip.uninstall(okButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }
    /// Verifies that dialog action rich tooltips reveal action-owned time picker targets.
    @Test
    void dialogPaneRevealsTimePickerValueInsideActionRichTooltip() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3TimePickerField tooltipTimeField = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(10, 45);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 620.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = installRichTooltip(
                        okButton,
                        "Schedule",
                        "Choose a target time.",
                        tooltipTimeField
                );

                try {
                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipTimeField.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetTime);
                    root.layout();

                    Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                            Node.class,
                            tooltipTimeField.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                    ));
                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipTimeField.isShowing());
                    assertTrue(pickerFocusNode.isFocused());
                    assertTrue(M3Accessible.containsNode(tooltipTimeField.getPicker(), pickerFocusNode));
                    assertSame(pickerFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltipTimeField.hidePicker();
                    tooltip.hide();
                    M3Tooltip.uninstall(okButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog action rich tooltips reject out-of-range action-owned time targets before opening popups.
    @Test
    void dialogPaneRejectsDisabledTimePickerValueInsideActionRichTooltip() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3TimePickerField tooltipTimeField = new M3TimePickerField(LocalTime.of(9, 30));
            tooltipTimeField.getPicker().setMaxTime(LocalTime.of(10, 0));
            LocalTime disabledTime = LocalTime.of(11, 15);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(outside, dialogPane);
                Scene scene = new Scene(root, 660.0, 440.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(24.0, 24.0, 120.0, 48.0);
                dialogPane.resizeRelocate(32.0, 96.0, 420.0, 260.0);
                root.layout();

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = installRichTooltip(
                        okButton,
                        "Schedule",
                        "Choose a target time.",
                        tooltipTimeField
                );

                try {
                    outside.requestFocus();
                    assertTrue(outside.isFocused());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledTime);
                    root.layout();

                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipTimeField.isShowing());
                    assertFalse(tooltipTimeField.getEditor().isFocused());
                    assertFalse(okButton.isFocused());
                    assertSame(outside, scene.getFocusOwner());

                } finally {
                    tooltipTimeField.hidePicker();
                    tooltip.hide();
                    M3Tooltip.uninstall(okButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog action rich tooltips reveal action-owned date-range picker targets.
    @Test
    void dialogPaneRevealsDateRangePickerValueInsideActionRichTooltip() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3DateRangePickerField tooltipRangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            LocalDate targetDate = LocalDate.of(2026, 6, 22);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 680.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 440.0, 280.0);
                root.layout();

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = installRichTooltip(
                        okButton,
                        "Schedule",
                        "Choose a target range.",
                        tooltipRangeField
                );

                try {
                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipRangeField.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);
                    root.layout();

                    Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                            Node.class,
                            tooltipRangeField.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                    ));
                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipRangeField.isShowing());
                    assertTrue(pickerFocusNode.isFocused());
                    assertTrue(M3Accessible.containsNode(tooltipRangeField.getPicker(), pickerFocusNode));
                    assertSame(pickerFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltipRangeField.hidePicker();
                    tooltip.hide();
                    M3Tooltip.uninstall(okButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog action rich tooltips reject out-of-range action-owned date-range targets before opening popups.
    @Test
    void dialogPaneRejectsDisabledDateRangePickerValueInsideActionRichTooltip() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3DateRangePickerField tooltipRangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            tooltipRangeField.getPicker().setMaxDate(LocalDate.of(2026, 6, 20));
            LocalDate disabledDate = LocalDate.of(2026, 6, 24);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(outside, dialogPane);
                Scene scene = new Scene(root, 700.0, 480.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(24.0, 24.0, 120.0, 48.0);
                dialogPane.resizeRelocate(32.0, 96.0, 440.0, 280.0);
                root.layout();

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = installRichTooltip(
                        okButton,
                        "Schedule",
                        "Choose a target range.",
                        tooltipRangeField
                );

                try {
                    outside.requestFocus();
                    assertTrue(outside.isFocused());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledDate);
                    root.layout();

                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipRangeField.isShowing());
                    assertFalse(tooltipRangeField.getStartEditor().isFocused());
                    assertFalse(tooltipRangeField.getEndEditor().isFocused());
                    assertFalse(okButton.isFocused());
                    assertSame(outside, scene.getFocusOwner());

                } finally {
                    tooltipRangeField.hidePicker();
                    tooltip.hide();
                    M3Tooltip.uninstall(okButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }
    /// Verifies that dialog action tooltip reveal rejects unreachable action targets without focusing the owner action.
    @Test
    void dialogPaneRejectsDisabledActionRichTooltipTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(new Pane());
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(outside, dialogPane);
                Scene scene = new Scene(root, 560.0, 340.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(24.0, 24.0, 120.0, 48.0);
                dialogPane.resizeRelocate(32.0, 96.0, 360.0, 220.0);
                root.layout();

                Node okButton = Objects.requireNonNull(dialogPane.lookupButton(ButtonType.OK), "okButton");
                M3RichTooltip tooltip = installRichTooltip(
                        okButton,
                        "Confirm",
                        "Dialog action help",
                        tooltipAction
                );

                try {
                    outside.requestFocus();
                    assertTrue(outside.isFocused());
                    assertFalse(tooltip.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                    root.layout();

                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipAction.isFocused());
                    assertFalse(okButton.isFocused());
                    assertSame(outside, scene.getFocusOwner());
                    assertNotSame(tooltipAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(okButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content tooltip reveal rejects unreachable action targets without focusing the content item.
    @Test
    void dialogPaneRejectsDisabledContentRichTooltipTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3ListItem contentItem = new M3ListItem("Content row");
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(contentItem);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(outside, dialogPane);
                Scene scene = new Scene(root, 560.0, 340.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(24.0, 24.0, 120.0, 48.0);
                dialogPane.resizeRelocate(32.0, 96.0, 360.0, 220.0);
                root.layout();

                M3RichTooltip tooltip = installRichTooltip(
                        contentItem,
                        "Content",
                        "Dialog content help",
                        tooltipAction
                );

                try {
                    outside.requestFocus();
                    assertTrue(outside.isFocused());
                    assertFalse(tooltip.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                    root.layout();

                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipAction.isFocused());
                    assertFalse(contentItem.isFocused());
                    assertSame(outside, scene.getFocusOwner());
                    assertNotSame(tooltipAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(contentItem, tooltip);
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
            M3ButtonGroup group = buttonGroup(menuButton);
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

    /// Verifies that virtualized list views preserve popup focus exposed by a visible row for default actions.
    @Test
    void listViewPreservesVisibleRowPopupFocusForDefaultAccessibilityActions() {
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
            M3ListView<M3ListItem> listView = listView(row);
            listView.setCellFactory(item -> item);
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

                assertTrue(tooltip.isShowing());
                assertTrue(rowAction.isFocused());
                assertSame(rowAction, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

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
            M3ButtonGroup group = buttonGroup(menuButton);
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

    /// Verifies that selection containers reveal nested menu targets owned by item content.
    @Test
    void selectionContainersRevealNestedMenuPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem iconTarget = new M3MenuItem("Icon target");
            M3MenuButton iconMenu = new M3MenuButton("More", new M3MenuItem("First"), iconTarget);
            M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(
                    new M3IconToggleButton(iconMenu),
                    new M3IconToggleButton("B")
            );
            assertContainerRevealsNestedMenuPopupTarget(iconGroup, iconMenu, iconTarget, 360.0, 96.0);

            M3MenuItem segmentedTarget = new M3MenuItem("Segment target");
            M3MenuButton segmentedMenu = new M3MenuButton("More", new M3MenuItem("First"), segmentedTarget);
            M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(
                    new M3SegmentedButton("More", segmentedMenu),
                    new M3SegmentedButton("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(
                    segmentedGroup,
                    segmentedMenu,
                    segmentedTarget,
                    420.0,
                    96.0
            );

            M3MenuItem chipTarget = new M3MenuItem("Chip target");
            M3MenuButton chipMenu = new M3MenuButton("More", new M3MenuItem("First"), chipTarget);
            M3ChipGroup chipGroup = chipGroup(
                    new M3Chip("More", chipMenu),
                    new M3Chip("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(chipGroup, chipMenu, chipTarget, 420.0, 96.0);

            M3MenuItem tabTarget = new M3MenuItem("Tab target");
            M3MenuButton tabMenu = new M3MenuButton("More", new M3MenuItem("First"), tabTarget);
            M3TabBar tabBar = tabBar(
                    new M3Tab("More", tabMenu),
                    new M3Tab("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(tabBar, tabMenu, tabTarget, 420.0, 96.0);
        });
    }

    /// Verifies that navigation and indexed item containers reveal nested menu targets owned by item content.
    @Test
    void navigationAndIndexedContainersRevealNestedMenuPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem barTarget = new M3MenuItem("Bar target");
            M3MenuButton barMenu = new M3MenuButton("More", new M3MenuItem("First"), barTarget);
            M3NavigationBar navigationBar = navigationBar(
                    new M3NavigationItem("More", barMenu),
                    new M3NavigationItem("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(navigationBar, barMenu, barTarget, 460.0, 112.0);

            M3MenuItem railTarget = new M3MenuItem("Rail target");
            M3MenuButton railMenu = new M3MenuButton("More", new M3MenuItem("First"), railTarget);
            M3NavigationRail navigationRail = navigationRail(
                    new M3NavigationItem("More", railMenu),
                    new M3NavigationItem("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(navigationRail, railMenu, railTarget, 180.0, 260.0);

            M3MenuItem listTarget = new M3MenuItem("List target");
            M3MenuButton listMenu = new M3MenuButton("More", new M3MenuItem("First"), listTarget);
            M3ListItem listItem = new M3ListItem("More");
            listItem.setTrailing(listMenu);
            M3ListPane listPane = listPane(listItem, new M3ListItem("Other"));
            assertContainerRevealsNestedMenuPopupTarget(listPane, listMenu, listTarget, 460.0, 160.0);

            M3MenuItem carouselTarget = new M3MenuItem("Carousel target");
            M3MenuButton carouselMenu = new M3MenuButton("More", new M3MenuItem("First"), carouselTarget);
            Pane carouselItem = new Pane(carouselMenu);
            carouselItem.setPrefSize(240.0, 128.0);
            carouselMenu.resizeRelocate(24.0, 24.0, 160.0, 56.0);
            Pane otherItem = new Pane(new M3Text("Other"));
            otherItem.setPrefSize(180.0, 128.0);
            M3Carousel carousel = carousel(carouselItem, otherItem);
            assertContainerRevealsNestedMenuPopupTarget(carousel, carouselMenu, carouselTarget, 560.0, 220.0);
        });
    }

    /// Verifies that carousel item content exposes active rich tooltip action focus.
    @Test
    void carouselRoutesFocusThroughNestedRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Pane firstItem = new Pane(new M3Text("First"));
            firstItem.setPrefSize(180.0, 128.0);
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Carousel item",
                    "Shows details for the selected carousel item.",
                    tooltipAction
            );
            Pane secondItem = new Pane(ownerAction);
            secondItem.setPrefSize(240.0, 128.0);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(carousel);
                Scene scene = new Scene(root, 680.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                carousel.resizeRelocate(32.0, 32.0, 560.0, 220.0);
                ownerAction.resizeRelocate(24.0, 24.0, 160.0, 56.0);
                carousel.select(secondItem);
                root.layout();

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 360.0, stage.getY() + 156.0);

                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertTrue(tooltip.isShowing());

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(carousel, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that carousel item content exposes active picker popup focus.
    @Test
    void carouselRoutesFocusThroughNestedDatePickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Pane firstItem = new Pane(new M3Text("First"));
            firstItem.setPrefSize(180.0, 128.0);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 9, 12));
            Pane secondItem = new Pane(field);
            secondItem.setPrefSize(300.0, 128.0);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(carousel);
                Scene scene = new Scene(root, 720.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                carousel.resizeRelocate(32.0, 32.0, 600.0, 220.0);
                field.resizeRelocate(24.0, 24.0, 260.0, 64.0);
                carousel.select(secondItem);
                root.layout();

                field.showPicker();
                field.getPicker().requestFocus();
                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(carousel, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }


    /// Verifies that carousel item content exposes active time picker popup focus.
    @Test
    void carouselRoutesFocusThroughNestedTimePickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Pane firstItem = new Pane(new M3Text("First"));
            firstItem.setPrefSize(180.0, 128.0);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(13, 15));
            Pane secondItem = new Pane(field);
            secondItem.setPrefSize(300.0, 128.0);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(carousel);
                Scene scene = new Scene(root, 720.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                carousel.resizeRelocate(32.0, 32.0, 600.0, 220.0);
                field.resizeRelocate(24.0, 24.0, 260.0, 64.0);
                carousel.select(secondItem);
                root.layout();

                field.showPicker();
                field.getPicker().requestFocus();
                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertSame(secondItem, carousel.getSelectedItem());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(carousel, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that carousel reveal requests open nested date picker value targets.
    @Test
    void carouselRevealsNestedDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 9, 18);
            Pane firstItem = new Pane(new M3Text("First"));
            firstItem.setPrefSize(180.0, 128.0);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 9, 12));
            Pane secondItem = new Pane(field);
            secondItem.setPrefSize(300.0, 128.0);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(carousel);
                Scene scene = new Scene(root, 720.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                carousel.resizeRelocate(32.0, 32.0, 600.0, 220.0);
                field.resizeRelocate(24.0, 24.0, 260.0, 64.0);
                carousel.select(firstItem);
                root.layout();

                assertSame(firstItem, carousel.getSelectedItem());

                assertPickerValueTargetRoutedByContainer(carousel, field, targetDate);

                assertSame(secondItem, carousel.getSelectedItem());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }


    /// Verifies that carousel reveal requests open nested time picker value targets.
    @Test
    void carouselRevealsNestedTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalTime targetTime = LocalTime.of(14, 30);
            Pane firstItem = new Pane(new M3Text("First"));
            firstItem.setPrefSize(180.0, 128.0);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(13, 15));
            Pane secondItem = new Pane(field);
            secondItem.setPrefSize(300.0, 128.0);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(carousel);
                Scene scene = new Scene(root, 720.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                carousel.resizeRelocate(32.0, 32.0, 600.0, 220.0);
                field.resizeRelocate(24.0, 24.0, 260.0, 64.0);
                carousel.select(firstItem);
                root.layout();

                assertSame(firstItem, carousel.getSelectedItem());

                assertPickerValueTargetRoutedByContainer(carousel, field, targetTime);

                assertSame(secondItem, carousel.getSelectedItem());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that carousel reveal requests preserve date range endpoint focus after nested picker dismissal.
    @Test
    void carouselRevealsNestedDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 9, 19);
            Pane firstItem = new Pane(new M3Text("First"));
            firstItem.setPrefSize(180.0, 128.0);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 9, 10),
                    LocalDate.of(2026, 9, 16)
            );
            Pane secondItem = new Pane(field);
            secondItem.setPrefSize(360.0, 128.0);
            M3Carousel carousel = carousel(firstItem, secondItem);
            carousel.setAnimatedScroll(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(carousel);
                Scene scene = new Scene(root, 780.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                carousel.resizeRelocate(32.0, 32.0, 660.0, 240.0);
                field.resizeRelocate(24.0, 24.0, 320.0, 64.0);
                carousel.select(secondItem);
                root.layout();

                assertDateRangePickerValueTargetRoutedByContainer(carousel, field, targetDate);

                assertSame(secondItem, carousel.getSelectedItem());
            } finally {
                field.hidePicker();
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
            M3Surface surface = surface(content);
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
            M3Surface surface = surface(content);
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
            M3Surface surface = surface(content);
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
            M3Surface surface = surface(content);
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

    /// Verifies that dialog content containers reject hidden popup owners before opening their menus.
    @Test
    void dialogPaneRejectsHiddenContentPopupOwnersBeforeOpeningBranches() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            menuButton.setVisible(false);

            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", publishItem);
            splitButton.setVisible(false);

            M3MenuItem nestedItem = new M3MenuItem("Nested");
            M3SubMenuItem hiddenSubMenu = new M3SubMenuItem("Hidden branch", nestedItem);
            hiddenSubMenu.setVisible(false);
            M3MenuButton parentMenuButton = new M3MenuButton("Parent", hiddenSubMenu);

            Pane content = new Pane(menuButton, splitButton, parentMenuButton);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                menuButton.resizeRelocate(0.0, 0.0, 180.0, 48.0);
                splitButton.resizeRelocate(0.0, 64.0, 240.0, 48.0);
                parentMenuButton.resizeRelocate(0.0, 128.0, 180.0, 48.0);
                root.layout();

                Object baselineFocusNode = dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
                assertFalse(menuButton.isShowing());
                assertFalse(splitButton.isShowing());
                assertFalse(parentMenuButton.isShowing());
                assertFalse(hiddenSubMenu.isSubMenuShowing());
                assertSame(baselineFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertFalse(menuButton.isShowing());
                assertFalse(archiveItem.isFocused());
                assertSame(baselineFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, publishItem);

                assertFalse(splitButton.isShowing());
                assertFalse(publishItem.isFocused());
                assertSame(baselineFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, nestedItem);

                assertFalse(parentMenuButton.isShowing());
                assertFalse(hiddenSubMenu.isSubMenuShowing());
                assertFalse(nestedItem.isFocused());
                assertSame(baselineFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content subtrees expose hosted snackbar action focus.
    @Test
    void dialogPaneRoutesFocusThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
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
                assertSame(host, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                actionButton.requestFocus();

                assertTrue(actionButton.isFocused());
                assertSame(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, actionButton);

                dialogPane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(actionButton.isFocused());
                assertPopupFocusRoutedByContainer(dialogPane, actionButton);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal queued snackbar actions through nested hosts.
    @Test
    void dialogPaneRevealsQueuedSnackbarThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                root.layout();

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();
                Node currentActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        currentSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(host, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, queuedSnackbar);
                root.applyCss();
                root.layout();
                Node queuedActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(queuedSnackbar, host.getSnackbar());
                assertTrue(host.getQueue().isEmpty());
                assertTrue(queuedActionButton.isFocused());
                assertSame(queuedActionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, queuedActionButton);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content subtrees can reveal queued snackbar actions by accessibility index.
    @Test
    void dialogPaneRevealsQueuedSnackbarActionByIndexThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                root.layout();

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();

                assertTrue(host.isShowing());
                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(currentSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(host, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0, 1);
                root.applyCss();
                root.layout();
                Node queuedActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(queuedSnackbar, host.getSnackbar());
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertNull(host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertTrue(host.getQueue().isEmpty());
                assertTrue(queuedActionButton.isFocused());
                assertSame(queuedActionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, queuedActionButton);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that hidden dialog snackbar hosts reject queued snackbar reveal without promotion.
    @Test
    void dialogPaneRejectsHiddenSnackbarHostBeforeQueuePromotion() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                root.layout();

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();
                Node currentActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        currentSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                host.setVisible(false);
                root.applyCss();
                root.layout();
                Object baselineFocusNode = dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, queuedSnackbar);
                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0, 1);
                root.applyCss();
                root.layout();

                assertTrue(host.isShowing());
                assertSame(currentSnackbar, host.getSnackbar());
                assertEquals(1, host.getQueue().size());
                assertSame(queuedSnackbar, host.getQueue().get(0));
                assertFalse(currentActionButton.isFocused());
                Object queuedFocusNode = queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
                if (queuedFocusNode instanceof Node queuedNode) {
                    assertFalse(queuedNode.isFocused());
                }
                assertSame(baselineFocusNode, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content subtrees expose active snackbar rich tooltip action focus.
    @Test
    void dialogPaneRoutesFocusThroughNestedSnackbarRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                root.layout();

                host.show(snackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = installRichTooltip(
                        actionButton,
                        "Undo",
                        "Restores the previous item state.",
                        tooltipAction
                );

                try {
                    actionButton.requestFocus();
                    tooltip.show(actionButton, stage.getX() + 216.0, stage.getY() + 184.0);

                    assertTrue(host.isShowing());
                    assertTrue(tooltip.isShowing());
                    assertTrue(actionButton.isFocused());
                    assertSame(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                    actionButton.fireEvent(keyPressed(KeyCode.F6));

                    assertTrue(tooltipAction.isFocused());
                    assertTrue(tooltip.isShowing());
                    assertTrue(host.isShowing());
                    assertSame(tooltipAction, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertPopupFocusRoutedByContainer(dialogPane, tooltipAction);

                    tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                    assertFalse(tooltip.isShowing());
                    assertTrue(actionButton.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal snackbar rich tooltip actions from explicit actions.
    @Test
    void dialogPaneRevealsNestedSnackbarRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                root.layout();

                host.show(snackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = installRichTooltip(
                        actionButton,
                        "Undo",
                        "Restores the previous item state.",
                        tooltipAction
                );

                try {
                    assertFalse(tooltip.isShowing());
                    assertSame(host, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(tooltipAction, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers can promote queued snackbars before revealing action tooltip targets.
    @Test
    void dialogPaneRevealsQueuedSnackbarRichTooltipActionThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                root.layout();

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();

                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(host, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, queuedSnackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = installRichTooltip(
                        actionButton,
                        "Restore",
                        "Restores the deleted item.",
                        tooltipAction
                );

                try {
                    assertSame(queuedSnackbar, host.getSnackbar());
                    assertTrue(host.getQueue().isEmpty());
                    assertTrue(actionButton.isFocused());
                    assertFalse(tooltip.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());
                    assertSame(tooltipAction, queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertPopupFocusRoutedByContainer(dialogPane, tooltipAction);
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers can reveal indexed queued snackbar tooltip targets.
    @Test
    void dialogPaneRevealsQueuedSnackbarRichTooltipActionByIndexThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
                root.layout();

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();

                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(currentSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(host, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0, 1);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = installRichTooltip(
                        actionButton,
                        "Restore",
                        "Restores the deleted item.",
                        tooltipAction
                );

                try {
                    assertSame(queuedSnackbar, host.getSnackbar());
                    assertNull(host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                    assertTrue(host.getQueue().isEmpty());
                    assertTrue(actionButton.isFocused());
                    assertFalse(tooltip.isShowing());

                    host.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0, tooltipAction);

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());

                    tooltip.hide();
                    actionButton.requestFocus();
                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0, tooltipAction);

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());
                    assertSame(tooltipAction, queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(tooltipAction, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertPopupFocusRoutedByContainer(dialogPane, tooltipAction);
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog snackbar rich tooltip reveal rejects unreachable action targets.
    @Test
    void dialogPaneRejectsUnreachableNestedSnackbarRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
            Pane content = new Pane(host);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 520.0, 260.0);
                host.resizeRelocate(0.0, 0.0, 420.0, 96.0);
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
                M3RichTooltip tooltip = installRichTooltip(
                        actionButton,
                        "Undo",
                        "Restores the previous item state.",
                        tooltipAction
                );

                try {
                    assertFalse(tooltip.isShowing());

                    dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                    assertFalse(tooltip.isShowing());
                    assertFalse(tooltipAction.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(actionButton, snackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(actionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    assertSame(host, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                } finally {
                    tooltip.hide();
                    M3Tooltip.uninstall(actionButton, tooltip);
                }
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers expose active split-button menu popup focus.
    @Test
    void dialogPaneRoutesFocusThroughNestedSplitButtonPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 480.0, 240.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                splitButton.showMenu();
                publishItem.requestFocus();

                assertTrue(splitButton.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, publishItem);

                publishItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(splitButton.isShowing());
                assertTrue(splitButtonMenuButton(splitButton).isFocused());
                assertSame(splitButtonMenuButton(splitButton), dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal nested split-button menu targets from explicit actions.
    @Test
    void dialogPaneRevealsNestedSplitButtonPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 480.0, 240.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                assertFalse(splitButton.isShowing());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, publishItem);

                assertTrue(splitButton.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, publishItem);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal picker value targets inside nested split-button submenu items.
    @Test
    void dialogPaneRevealsSplitButtonNestedSubMenuPickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 21);
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3SplitButton splitButton = splitButton("Create", scheduleItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 480.0, 240.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                assertNestedSubMenuPickerValueTargetRoutedByContainer(
                        dialogPane,
                        splitButtonMenuButton(splitButton),
                        scheduleItem,
                        field,
                        targetDate
                );
            } finally {
                field.hidePicker();
                scheduleItem.hideSubMenu();
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal date-range value targets inside nested split-button submenu items.
    @Test
    void dialogPaneRevealsSplitButtonNestedSubMenuDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 6, 24);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3SplitButton splitButton = splitButton("Create", scheduleItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(420.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 720.0, 400.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 560.0, 280.0);
                splitButton.resizeRelocate(0.0, 0.0, 300.0, 48.0);
                root.layout();

                assertNestedSubMenuDateRangePickerValueTargetRoutedByContainer(
                        dialogPane,
                        splitButtonMenuButton(splitButton),
                        scheduleItem,
                        field,
                        targetDate
                );
            } finally {
                field.hidePicker();
                scheduleItem.hideSubMenu();
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal time value targets inside nested split-button submenu items.
    @Test
    void dialogPaneRevealsSplitButtonNestedSubMenuTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(8, 30));
            LocalTime targetTime = LocalTime.of(9, 45);
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3SplitButton splitButton = splitButton("Create", scheduleItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 480.0, 240.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                assertNestedSubMenuPickerValueTargetRoutedByContainer(
                        dialogPane,
                        splitButtonMenuButton(splitButton),
                        scheduleItem,
                        field,
                        targetTime
                );
            } finally {
                field.hidePicker();
                scheduleItem.hideSubMenu();
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal rich tooltip actions inside nested split-button submenu items.
    @Test
    void dialogPaneRevealsSplitButtonNestedSubMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3MenuItem htmlItem = new M3MenuItem("HTML");
            M3Button tooltipAction = new M3Button("Describe");
            M3RichTooltip tooltip = installRichTooltip(
                    pdfItem,
                    "PDF export",
                    "Exports the current draft as a PDF.",
                    tooltipAction
            );
            M3SubMenuItem recentItem = new M3SubMenuItem("Recent", pdfItem, htmlItem);
            M3SubMenuItem exportItem = new M3SubMenuItem("Export", recentItem);
            M3SplitButton splitButton = splitButton("Create", exportItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 120.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 720.0, 400.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 560.0, 280.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                assertFalse(splitButton.isShowing());
                assertFalse(exportItem.isSubMenuShowing());
                assertFalse(recentItem.isSubMenuShowing());
                assertFalse(tooltip.isShowing());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(splitButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, splitButtonMenuButton(splitButton).queryAccessibleAttribute(
                        AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(pdfItem.isFocused());
                assertTrue(splitButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertSame(pdfItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, splitButtonMenuButton(splitButton).queryAccessibleAttribute(
                        AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                recentItem.hideSubMenu();
                exportItem.hideSubMenu();
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that dialog content containers reveal rich tooltip actions inside nested split-button menu items.
    @Test
    void dialogPaneRevealsNestedSplitButtonMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    publishItem,
                    "Publish",
                    "Publishes the current draft.",
                    tooltipAction
            );
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(dialogPane);
                Scene scene = new Scene(root, 640.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dialogPane.resizeRelocate(32.0, 32.0, 480.0, 240.0);
                splitButton.resizeRelocate(0.0, 0.0, 240.0, 48.0);
                root.layout();

                assertFalse(splitButton.isShowing());
                assertFalse(tooltip.isShowing());

                dialogPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(splitButton.isShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, splitButtonMenuButton(splitButton).queryAccessibleAttribute(
                        AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(dialogPane, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(publishItem.isFocused());
                assertTrue(splitButton.isShowing());
                assertSame(publishItem, dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
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
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = surface(content);
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
                assertTrue(splitButtonMenuButton(splitButton).isFocused());
                assertSame(splitButtonMenuButton(splitButton), surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            Pane content = new Pane(splitButton);
            content.setPrefSize(360.0, 96.0);
            M3Surface surface = surface(content);
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

    /// Verifies that split buttons expose rich tooltip action focus from items inside their open menu popup.
    @Test
    void splitButtonRoutesFocusThroughMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    publishItem,
                    "Publish",
                    "Publishes the current draft.",
                    tooltipAction
            );
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(splitButton);
                Scene scene = new Scene(root, 520.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                splitButton.resizeRelocate(32.0, 32.0, 240.0, 48.0);
                root.layout();

                splitButton.showMenu();
                publishItem.requestFocus();
                tooltip.show(publishItem, stage.getX() + 296.0, stage.getY() + 120.0);

                assertTrue(splitButton.isShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(publishItem, splitButtonMenuButton(splitButton).queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                publishItem.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(splitButton.isShowing());
                assertSame(tooltipAction, splitButtonMenuButton(splitButton).queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(splitButton, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(publishItem.isFocused());
                assertTrue(splitButton.isShowing());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(publishItem, splitButtonMenuButton(splitButton).queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that split buttons reveal rich tooltip actions owned by closed menu items.
    @Test
    void splitButtonRevealsMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    publishItem,
                    "Publish",
                    "Publishes the current draft.",
                    tooltipAction
            );
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(splitButton);
                Scene scene = new Scene(root, 520.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                splitButton.resizeRelocate(32.0, 32.0, 240.0, 48.0);
                root.layout();

                assertFalse(splitButton.isShowing());
                assertFalse(tooltip.isShowing());

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(splitButton.isShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, splitButtonMenuButton(splitButton).queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(splitButton, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(publishItem.isFocused());
                assertTrue(splitButton.isShowing());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that split buttons reveal rich tooltip actions inside closed nested submenu branches.
    @Test
    void splitButtonRevealsRichTooltipActionInsideNestedSubMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem pdfItem = new M3MenuItem("PDF");
            M3MenuItem htmlItem = new M3MenuItem("HTML");
            M3Button tooltipAction = new M3Button("Describe");
            M3RichTooltip tooltip = installRichTooltip(
                    pdfItem,
                    "PDF export",
                    "Exports the current draft as a PDF.",
                    tooltipAction
            );
            M3SubMenuItem recentItem = new M3SubMenuItem("Recent", pdfItem, htmlItem);
            M3SubMenuItem exportItem = new M3SubMenuItem("Export", recentItem);
            M3SplitButton splitButton = splitButton("Create", exportItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(splitButton);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                splitButton.resizeRelocate(32.0, 32.0, 240.0, 56.0);
                root.layout();

                assertFalse(splitButton.isShowing());
                assertFalse(exportItem.isSubMenuShowing());
                assertFalse(recentItem.isSubMenuShowing());
                assertFalse(tooltip.isShowing());

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                assertTrue(splitButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, splitButtonMenuButton(splitButton).queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(splitButton, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(pdfItem.isFocused());
                assertTrue(splitButton.isShowing());
                assertTrue(exportItem.isSubMenuShowing());
                assertTrue(recentItem.isSubMenuShowing());
                assertSame(pdfItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, splitButtonMenuButton(splitButton).queryAccessibleAttribute(
                        AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, exportItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(pdfItem, recentItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that split buttons reveal picker value targets inside nested submenu branches.
    @Test
    void splitButtonRevealsPickerValueTargetInsideNestedSubMenu() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(8, 30));
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3SplitButton splitButton = splitButton("Create", scheduleItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(splitButton);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                splitButton.resizeRelocate(32.0, 32.0, 240.0, 56.0);
                root.layout();

                assertNestedSubMenuPickerValueTargetRoutedByContainer(
                        splitButton,
                        splitButtonMenuButton(splitButton),
                        scheduleItem,
                        field,
                        LocalTime.of(9, 45)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that split buttons preserve date range endpoint focus through nested submenu picker routing.
    @Test
    void splitButtonRevealsDateRangePickerValueTargetInsideActiveNestedSubMenu() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 7, 8),
                    LocalDate.of(2026, 7, 12)
            );
            M3SubMenuItem scheduleItem = new M3SubMenuItem("Schedule", field);
            M3SplitButton splitButton = splitButton("Create", scheduleItem);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(splitButton);
                Scene scene = new Scene(root, 860.0, 480.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                splitButton.resizeRelocate(32.0, 32.0, 240.0, 56.0);
                root.layout();

                assertNestedSubMenuDateRangePickerValueTargetRoutedByContainer(
                        splitButton,
                        splitButtonMenuButton(splitButton),
                        scheduleItem,
                        field,
                        LocalDate.of(2026, 7, 18)
                );
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Edit",
                    "Edits the selected item.",
                    tooltipAction
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(ownerAction);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = surface(content);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Edit",
                    "Edits the selected item.",
                    tooltipAction
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(ownerAction);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = surface(content);
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

    /// Verifies that surface content containers reveal menu popup targets owned by FAB-menu actions.
    @Test
    void surfaceRevealsNestedFabMenuMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(menuButton);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 220.0);
                fabMenu.resizeRelocate(0.0, 0.0, 180.0, 160.0);
                root.layout();

                assertFalse(fabMenu.isExpanded());
                assertFalse(menuButton.isShowing());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(fabMenu.isExpanded());
                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, archiveItem);
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that FAB menus keep an action-owned menu popup focus for default accessibility focus actions.
    @Test
    void fabMenuPreservesActiveMenuPopupFocusForDefaultAccessibilityActions() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(fabMenu);
                Scene scene = new Scene(root, 520.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                fabMenu.resizeRelocate(32.0, 32.0, 220.0, 180.0);
                root.layout();

                assertFalse(fabMenu.isExpanded());
                assertFalse(menuButton.isShowing());

                fabMenu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);
                root.applyCss();
                root.layout();

                assertTrue(fabMenu.isExpanded());
                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(fabMenu, archiveItem);
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal picker value targets owned by FAB-menu actions.
    @Test
    void surfaceRevealsNestedFabMenuDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 6, 16);
            M3DatePickerField field = new M3DatePickerField();
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(field);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 220.0);
                fabMenu.resizeRelocate(0.0, 0.0, 220.0, 160.0);
                root.layout();

                assertFalse(fabMenu.isExpanded());

                assertPickerValueTargetRoutedByContainer(surface, field, targetDate);

                assertTrue(fabMenu.isExpanded());
                assertSame(field.getEditor(), fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }


    /// Verifies that surface content containers reveal time picker value targets owned by FAB-menu actions.
    @Test
    void surfaceRevealsNestedFabMenuTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalTime targetTime = LocalTime.of(14, 30);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(field);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 720.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 440.0, 220.0);
                fabMenu.resizeRelocate(0.0, 0.0, 220.0, 160.0);
                root.layout();

                assertFalse(fabMenu.isExpanded());

                assertPickerValueTargetRoutedByContainer(surface, field, targetTime);

                assertTrue(fabMenu.isExpanded());
                assertSame(field.getEditor(), fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that surface content containers reveal date range picker value targets owned by FAB-menu actions.
    @Test
    void surfaceRevealsNestedFabMenuDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 6, 24);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(field);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(420.0, 180.0);
            M3Surface surface = surface(content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(surface);
                Scene scene = new Scene(root, 780.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                surface.resizeRelocate(32.0, 32.0, 500.0, 240.0);
                fabMenu.resizeRelocate(0.0, 0.0, 320.0, 180.0);
                root.layout();

                assertFalse(fabMenu.isExpanded());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(fabMenu.isExpanded());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(surface, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getStartEditor().isFocused());
                assertSame(field.getStartEditor(), fabMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(field.getStartEditor(), surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Edit",
                    "Edits the selected item.",
                    tooltipAction
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(ownerAction);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(360.0, 180.0);
            M3Surface surface = surface(content);
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
            M3Surface surface = surface(host);
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

    /// Verifies that surface content subtrees can reveal queued snackbar actions through their host.
    @Test
    void surfaceRevealsQueuedSnackbarThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            M3Surface surface = surface(host);
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

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();
                Node currentActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        currentSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(currentActionButton, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, queuedSnackbar);
                root.applyCss();
                root.layout();
                Node queuedActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(queuedSnackbar, host.getSnackbar());
                assertTrue(host.getQueue().isEmpty());
                assertTrue(queuedActionButton.isFocused());
                assertSame(queuedActionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, queuedActionButton);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that surface content subtrees can reveal queued snackbar actions by accessibility index.
    @Test
    void surfaceRevealsQueuedSnackbarActionByIndexThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            M3Surface surface = surface(host);
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

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();
                Node currentActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        currentSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(currentSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(currentActionButton, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                root.applyCss();
                root.layout();
                Node queuedActionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));

                assertTrue(host.isShowing());
                assertSame(queuedSnackbar, host.getSnackbar());
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertNull(host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertTrue(host.getQueue().isEmpty());
                assertTrue(queuedActionButton.isFocused());
                assertSame(queuedActionButton, host.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(surface, queuedActionButton);
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
            M3Surface surface = surface(host);
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
                M3RichTooltip tooltip = installRichTooltip(
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
            M3Surface surface = surface(host);
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
                M3RichTooltip tooltip = installRichTooltip(
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

    /// Verifies that surface content containers can promote queued snackbars before revealing action tooltip targets.
    @Test
    void surfaceRevealsQueuedSnackbarRichTooltipActionThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            M3Surface surface = surface(host);
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

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();

                assertTrue(host.isShowing());
                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, queuedSnackbar);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = installRichTooltip(
                        actionButton,
                        "Restore",
                        "Restores the deleted item.",
                        tooltipAction
                );

                try {
                    assertSame(queuedSnackbar, host.getSnackbar());
                    assertFalse(tooltip.isShowing());

                    surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(queuedSnackbar, host.getSnackbar());
                    assertSame(tooltipAction, queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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

    /// Verifies that surface content containers can reveal indexed queued snackbar tooltip targets.
    @Test
    void surfaceRevealsQueuedSnackbarRichTooltipActionByIndexThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
            M3Surface surface = surface(host);
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

                host.show(currentSnackbar);
                host.enqueue(queuedSnackbar);
                root.applyCss();
                root.layout();

                assertTrue(host.isShowing());
                assertSame(currentSnackbar, host.getSnackbar());
                assertSame(currentSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                root.applyCss();
                root.layout();
                Node actionButton = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                M3Button tooltipAction = new M3Button("Details");
                M3RichTooltip tooltip = installRichTooltip(
                        actionButton,
                        "Restore",
                        "Restores the deleted item.",
                        tooltipAction
                );

                try {
                    assertSame(queuedSnackbar, host.getSnackbar());
                    assertSame(queuedSnackbar, host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                    assertNull(host.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                    assertTrue(host.getQueue().isEmpty());
                    assertFalse(tooltip.isShowing());

                    surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0, tooltipAction);

                    assertTrue(tooltip.isShowing());
                    assertTrue(tooltipAction.isFocused());
                    assertTrue(host.isShowing());
                    assertSame(queuedSnackbar, host.getSnackbar());
                    assertSame(tooltipAction, queuedSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
            M3Surface surface = surface(host);
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
                M3RichTooltip tooltip = installRichTooltip(
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Retry",
                    "Retries the interrupted task.",
                    tooltipAction
            );
            M3Banner banner = banner("Connection interrupted", ownerAction);
            banner.setIcon(new M3Icon("!"));
            M3Surface surface = surface(banner);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Retry",
                    "Retries the interrupted task.",
                    tooltipAction
            );
            M3Banner banner = banner("Connection interrupted", ownerAction);
            banner.setIcon(new M3Icon("!"));
            M3Surface surface = surface(banner);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Retry",
                    "Retries the interrupted task.",
                    tooltipAction
            );
            M3Banner banner = banner("Connection interrupted", ownerAction);
            banner.setIcon(new M3Icon("!"));
            M3Surface surface = surface(banner);
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

    /// Verifies that banner action slots reveal menu popup targets and expose popup focus.
    @Test
    void bannerRevealsNestedMenuPopupTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3Banner banner = banner("Sync required", menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(banner);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                banner.resizeRelocate(32.0, 32.0, 620.0, 104.0);
                menuButton.resizeRelocate(420.0, 24.0, 160.0, 48.0);
                root.layout();

                assertFalse(menuButton.isShowing());

                banner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that banner action slots reveal date picker value targets and expose popup focus.
    @Test
    void bannerRevealsNestedDatePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 22);
            M3Banner banner = banner("Choose a due date", field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(banner);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                banner.resizeRelocate(32.0, 32.0, 640.0, 112.0);
                field.resizeRelocate(368.0, 16.0, 220.0, 64.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(banner, field, targetDate);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that banner action slots reveal split-button menu targets and expose popup focus.
    @Test
    void bannerRevealsNestedSplitButtonPopupTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3Banner banner = banner("Create an item", splitButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(banner);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                banner.resizeRelocate(32.0, 32.0, 640.0, 112.0);
                splitButton.resizeRelocate(360.0, 24.0, 240.0, 48.0);
                root.layout();

                assertFalse(splitButton.isShowing());

                banner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, publishItem);

                assertTrue(splitButton.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(publishItem, banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                publishItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(splitButton.isShowing());
                assertTrue(splitButtonMenuButton(splitButton).isFocused());
                assertSame(splitButtonMenuButton(splitButton), banner.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that banner action slots reject unreachable menu popup targets before opening popups.
    @Test
    void bannerRejectsUnreachableNestedMenuPopupTargetsFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3Banner banner = banner("Sync required", menuButton);

            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    banner,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    640.0,
                    112.0
            );
        });
    }

    /// Verifies that banner action slots reject unreachable split-button popup targets before opening popups.
    @Test
    void bannerRejectsUnreachableNestedSplitButtonPopupTargetsFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3Banner banner = banner("Create an item", splitButton);

            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    banner,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    640.0,
                    112.0
            );
        });
    }

    /// Verifies that card content reveals menu popup targets and exposes popup focus.
    @Test
    void cardRevealsNestedMenuPopupTargetFromContent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem openItem = new M3MenuItem("Open");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", openItem, archiveItem);
            M3Card card = new M3Card(menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(card);
                Scene scene = new Scene(root, 720.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                card.resizeRelocate(32.0, 32.0, 300.0, 120.0);
                root.layout();

                assertFalse(menuButton.isShowing());

                card.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that card content reveals date picker value targets and exposes popup focus.
    @Test
    void cardRevealsNestedDatePickerValueTargetFromContent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 24);
            M3Card card = new M3Card(field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(card);
                Scene scene = new Scene(root, 720.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                card.resizeRelocate(32.0, 32.0, 360.0, 136.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(card, field, targetDate);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that card content reveals split-button menu targets and exposes popup focus.
    @Test
    void cardRevealsNestedSplitButtonPopupTargetFromContent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3Card card = new M3Card(splitButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(card);
                Scene scene = new Scene(root, 720.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                card.resizeRelocate(32.0, 32.0, 360.0, 120.0);
                root.layout();

                assertFalse(splitButton.isShowing());

                card.executeAccessibleAction(AccessibleAction.SHOW_ITEM, publishItem);

                assertTrue(splitButton.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(publishItem, card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                publishItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(splitButton.isShowing());
                assertTrue(splitButtonMenuButton(splitButton).isFocused());
                assertSame(splitButtonMenuButton(splitButton), card.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that card content rejects unreachable menu popup targets before opening popups.
    @Test
    void cardRejectsUnreachableNestedMenuPopupTargetsFromContent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3Card card = new M3Card(menuButton);

            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    card,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    360.0,
                    120.0
            );
        });
    }

    /// Verifies that card content rejects unreachable split-button popup targets before opening popups.
    @Test
    void cardRejectsUnreachableNestedSplitButtonPopupTargetsFromContent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3Card card = new M3Card(splitButton);

            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    card,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    360.0,
                    120.0
            );
        });
    }

    /// Verifies that surface content subtrees expose active card rich tooltip action focus.
    @Test
    void surfaceRoutesFocusThroughNestedCardRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Open");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Open",
                    "Opens the selected card item.",
                    tooltipAction
            );
            M3Card card = new M3Card(ownerAction);
            M3Surface surface = surface(card);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Open",
                    "Opens the selected card item.",
                    tooltipAction
            );
            M3Card card = new M3Card(ownerAction);
            M3Surface surface = surface(card);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Open",
                    "Opens the selected card item.",
                    tooltipAction
            );
            M3Card card = new M3Card(ownerAction);
            M3Surface surface = surface(card);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows additional list item actions.",
                    tooltipAction
            );
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setTrailing(ownerAction);
            M3Surface surface = surface(listItem);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows additional list item actions.",
                    tooltipAction
            );
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setTrailing(ownerAction);
            M3Surface surface = surface(listItem);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows additional list item actions.",
                    tooltipAction
            );
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setTrailing(ownerAction);
            M3Surface surface = surface(listItem);
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

    /// Verifies that list item leading slots reveal menu popup targets and expose popup focus.
    @Test
    void listItemLeadingSlotRevealsNestedMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem openItem = new M3MenuItem("Open");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", openItem, archiveItem);
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setLeading(menuButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(listItem);
                Scene scene = new Scene(root, 640.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                listItem.resizeRelocate(32.0, 32.0, 460.0, 72.0);
                root.layout();

                assertFalse(menuButton.isShowing());

                listItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that list item trailing slots reveal date picker value targets and expose popup focus.
    @Test
    void listItemTrailingSlotRevealsNestedDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 26);
            M3ListItem listItem = new M3ListItem("Due date");
            listItem.setTrailing(field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(listItem);
                Scene scene = new Scene(root, 720.0, 300.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                listItem.resizeRelocate(32.0, 32.0, 560.0, 88.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(listItem, field, targetDate);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that list item leading slots reveal split-button menu targets and expose popup focus.
    @Test
    void listItemLeadingSlotRevealsNestedSplitButtonPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3ListItem listItem = new M3ListItem("Template");
            listItem.setLeading(splitButton);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(listItem);
                Scene scene = new Scene(root, 720.0, 300.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                listItem.resizeRelocate(32.0, 32.0, 560.0, 88.0);
                root.layout();

                assertFalse(splitButton.isShowing());

                listItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM, publishItem);

                assertTrue(splitButton.isShowing());
                assertTrue(publishItem.isFocused());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(publishItem, listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                publishItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(splitButton.isShowing());
                assertTrue(splitButtonMenuButton(splitButton).isFocused());
                assertSame(splitButtonMenuButton(splitButton), listItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that list item leading slots reject unreachable menu popup targets before opening popups.
    @Test
    void listItemLeadingSlotRejectsUnreachableNestedMenuPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3ListItem listItem = new M3ListItem("Document");
            listItem.setLeading(menuButton);

            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    listItem,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    560.0,
                    88.0
            );
        });
    }

    /// Verifies that list item leading slots reject unreachable split-button popup targets before opening popups.
    @Test
    void listItemLeadingSlotRejectsUnreachableNestedSplitButtonPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3ListItem listItem = new M3ListItem("Template");
            listItem.setLeading(splitButton);

            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    listItem,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    560.0,
                    88.0
            );
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
            M3SideSheet sheet = sideSheet("Details", content);
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

    /// Verifies that shown side sheets expose active nested content menu popup focus.
    @Test
    void sideSheetRoutesFocusThroughNestedContentMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(260.0, 88.0);
            M3SideSheet sheet = sideSheet("Details", content);
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

                menuButton.showMenu();
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(sheet.isShown());
                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(sheet, archiveItem);

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sheet.hide();

                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows additional sheet details.",
                    tooltipAction
            );
            Pane content = new Pane(ownerAction);
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

    /// Verifies that shown bottom sheets expose active nested content rich tooltip focus.
    @Test
    void bottomSheetRoutesFocusThroughNestedContentRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows additional sheet details.",
                    tooltipAction
            );
            Pane content = new Pane(ownerAction);
            content.setPrefSize(260.0, 88.0);
            M3BottomSheet sheet = bottomSheet("Queue", content);
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

                tooltip.show(ownerAction, stage.getX() + 180.0, stage.getY() + 180.0);
                ownerAction.requestFocus();

                assertTrue(sheet.isShown());
                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(sheet, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sheet.hide();

                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that a hidden bottom sheet can reveal a nested submenu target from its content subtree.
    @Test
    void hiddenBottomSheetRevealsNestedContentMenuPopupTarget() {
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
                assertFalse(menuButton.isShowing());
                assertFalse(moveItem.isSubMenuShowing());

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
                stage.close();
            }
        });
    }

    /// Verifies that shown bottom sheets expose active nested content menu popup focus.
    @Test
    void bottomSheetRoutesFocusThroughNestedContentMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(260.0, 88.0);
            M3BottomSheet sheet = bottomSheet("Queue", content);
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

                menuButton.showMenu();
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);

                assertTrue(sheet.isShown());
                assertTrue(menuButton.isShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(sheet, archiveItem);

                archiveItem.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sheet.hide();

                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that a hidden side sheet can reveal a rich tooltip action from its content subtree.
    @Test
    void hiddenSideSheetRevealsNestedContentRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows additional sheet details.",
                    tooltipAction
            );
            Pane content = new Pane(ownerAction);
            content.setPrefSize(260.0, 88.0);
            M3SideSheet sheet = sideSheet("Details", content);
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

    /// Verifies that shown side sheets expose active nested content rich tooltip focus.
    @Test
    void sideSheetRoutesFocusThroughNestedContentRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows additional sheet details.",
                    tooltipAction
            );
            Pane content = new Pane(ownerAction);
            content.setPrefSize(260.0, 88.0);
            M3SideSheet sheet = sideSheet("Details", content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 720.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 320.0, 260.0);
                ownerAction.resizeRelocate(0.0, 0.0, 160.0, 48.0);
                root.layout();

                tooltip.show(ownerAction, stage.getX() + 180.0, stage.getY() + 180.0);
                ownerAction.requestFocus();

                assertTrue(sheet.isShown());
                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertPopupFocusRoutedByContainer(sheet, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sheet.hide();

                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that hidden bottom sheets reveal nested content picker value targets.
    @Test
    void hiddenBottomSheetRevealsNestedContentPickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 18);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            Pane content = new Pane(field);
            content.setPrefSize(320.0, 96.0);
            M3BottomSheet sheet = bottomSheet("Schedule", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 520.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertPickerValueTargetRoutedByContainer(sheet, field, targetDate);

                assertTrue(sheet.isShown());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that shown bottom sheets expose nested content picker popup focus.
    @Test
    void bottomSheetRoutesFocusThroughNestedContentPickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 19);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            Pane content = new Pane(field);
            content.setPrefSize(320.0, 96.0);
            M3BottomSheet sheet = bottomSheet("Schedule", content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 520.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertTrue(sheet.isShown());

                assertPickerValueTargetRoutedByContainer(sheet, field, targetDate);
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that hidden side sheets reveal nested content picker value targets.
    @Test
    void hiddenSideSheetRevealsNestedContentPickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 20);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            Pane content = new Pane(field);
            content.setPrefSize(300.0, 96.0);
            M3SideSheet sheet = sideSheet("Schedule", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 360.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertPickerValueTargetRoutedByContainer(sheet, field, targetDate);

                assertTrue(sheet.isShown());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that shown side sheets expose nested content picker popup focus.
    @Test
    void sideSheetRoutesFocusThroughNestedContentPickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 21);
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            Pane content = new Pane(field);
            content.setPrefSize(300.0, 96.0);
            M3SideSheet sheet = sideSheet("Schedule", content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 360.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertTrue(sheet.isShown());

                assertPickerValueTargetRoutedByContainer(sheet, field, targetDate);
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that hidden bottom sheets reveal nested content time picker value targets.
    @Test
    void hiddenBottomSheetRevealsNestedContentTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalTime targetTime = LocalTime.of(10, 45);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(8, 30));
            Pane content = new Pane(field);
            content.setPrefSize(320.0, 96.0);
            M3BottomSheet sheet = bottomSheet("Schedule", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 520.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertPickerValueTargetRoutedByContainer(sheet, field, targetTime);

                assertTrue(sheet.isShown());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that shown bottom sheets expose nested content time picker popup focus.
    @Test
    void bottomSheetRoutesFocusThroughNestedContentTimePickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalTime targetTime = LocalTime.of(11, 15);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(8, 30));
            Pane content = new Pane(field);
            content.setPrefSize(320.0, 96.0);
            M3BottomSheet sheet = bottomSheet("Schedule", content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 520.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertTrue(sheet.isShown());

                assertPickerValueTargetRoutedByContainer(sheet, field, targetTime);
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that hidden side sheets reveal nested content time picker value targets.
    @Test
    void hiddenSideSheetRevealsNestedContentTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalTime targetTime = LocalTime.of(12, 30);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 0));
            Pane content = new Pane(field);
            content.setPrefSize(300.0, 96.0);
            M3SideSheet sheet = sideSheet("Schedule", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 360.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertPickerValueTargetRoutedByContainer(sheet, field, targetTime);

                assertTrue(sheet.isShown());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that shown side sheets expose nested content time picker popup focus.
    @Test
    void sideSheetRoutesFocusThroughNestedContentTimePickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalTime targetTime = LocalTime.of(13, 45);
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 0));
            Pane content = new Pane(field);
            content.setPrefSize(300.0, 96.0);
            M3SideSheet sheet = sideSheet("Schedule", content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 760.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 360.0, 300.0);
                field.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                root.layout();

                assertTrue(sheet.isShown());

                assertPickerValueTargetRoutedByContainer(sheet, field, targetTime);
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that hidden bottom sheets reveal nested content date range picker value targets.
    @Test
    void hiddenBottomSheetRevealsNestedContentDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 22);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 16)
            );
            Pane content = new Pane(field);
            content.setPrefSize(380.0, 96.0);
            M3BottomSheet sheet = bottomSheet("Schedule", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 820.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 580.0, 320.0);
                field.resizeRelocate(0.0, 0.0, 340.0, 64.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(sheet.isShown());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(sheet, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getStartEditor().isFocused());
                assertSame(field.getStartEditor(), sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that shown bottom sheets preserve date range endpoint focus around nested picker popups.
    @Test
    void bottomSheetRoutesFocusThroughNestedContentDateRangePickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 23);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 16)
            );
            Pane content = new Pane(field);
            content.setPrefSize(380.0, 96.0);
            M3BottomSheet sheet = bottomSheet("Schedule", content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 820.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 580.0, 320.0);
                field.resizeRelocate(0.0, 0.0, 340.0, 64.0);
                root.layout();

                assertTrue(sheet.isShown());

                assertDateRangePickerValueTargetRoutedByContainer(sheet, field, targetDate);
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that hidden side sheets reveal nested content date range picker value targets.
    @Test
    void hiddenSideSheetRevealsNestedContentDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 24);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 16)
            );
            Pane content = new Pane(field);
            content.setPrefSize(380.0, 96.0);
            M3SideSheet sheet = sideSheet("Schedule", content);
            sheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 820.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 460.0, 320.0);
                field.resizeRelocate(0.0, 0.0, 340.0, 64.0);
                root.layout();

                assertFalse(sheet.isShown());
                assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(sheet.isShown());
                assertTrue(field.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
                assertPopupFocusRoutedByContainer(sheet, pickerFocusNode);

                field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getStartEditor().isFocused());
                assertSame(field.getStartEditor(), sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that shown side sheets preserve date range endpoint focus around nested picker popups.
    @Test
    void sideSheetRoutesFocusThroughNestedContentDateRangePickerPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate targetDate = LocalDate.of(2026, 8, 25);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 16)
            );
            Pane content = new Pane(field);
            content.setPrefSize(380.0, 96.0);
            M3SideSheet sheet = sideSheet("Schedule", content);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(sheet);
                Scene scene = new Scene(root, 820.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                sheet.resizeRelocate(32.0, 32.0, 460.0, 320.0);
                field.resizeRelocate(0.0, 0.0, 340.0, 64.0);
                root.layout();

                assertTrue(sheet.isShown());

                assertDateRangePickerValueTargetRoutedByContainer(sheet, field, targetDate);
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that hidden sheets reject disabled picker value targets before changing shown state.
    @Test
    void hiddenSheetsRejectDisabledContentPickerValueTargetsBeforeShowing() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField bottomDateField = new M3DatePickerField(LocalDate.of(2026, 8, 10));
            bottomDateField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3BottomSheet bottomDateSheet = bottomSheet("Bottom date", new Pane(bottomDateField));
            bottomDateSheet.setShown(false);

            M3DatePickerField sideDateField = new M3DatePickerField(LocalDate.of(2026, 8, 10));
            sideDateField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3SideSheet sideDateSheet = sideSheet("Side date", new Pane(sideDateField));
            sideDateSheet.setShown(false);

            M3TimePickerField bottomTimeField = new M3TimePickerField(LocalTime.of(9, 0));
            bottomTimeField.getPicker().setMaxTime(LocalTime.of(12, 0));
            M3BottomSheet bottomTimeSheet = bottomSheet("Bottom time", new Pane(bottomTimeField));
            bottomTimeSheet.setShown(false);

            M3TimePickerField sideTimeField = new M3TimePickerField(LocalTime.of(9, 0));
            sideTimeField.getPicker().setMaxTime(LocalTime.of(12, 0));
            M3SideSheet sideTimeSheet = sideSheet("Side time", new Pane(sideTimeField));
            sideTimeSheet.setShown(false);

            M3DateRangePickerField bottomRangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 12)
            );
            bottomRangeField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3BottomSheet bottomRangeSheet = bottomSheet("Bottom range", new Pane(bottomRangeField));
            bottomRangeSheet.setShown(false);

            M3DateRangePickerField sideRangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 12)
            );
            sideRangeField.getPicker().setMaxDate(LocalDate.of(2026, 8, 15));
            M3SideSheet sideRangeSheet = sideSheet("Side range", new Pane(sideRangeField));
            sideRangeSheet.setShown(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(
                        bottomDateSheet,
                        sideDateSheet,
                        bottomTimeSheet,
                        sideTimeSheet,
                        bottomRangeSheet,
                        sideRangeSheet
                );
                Scene scene = new Scene(root, 960.0, 720.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                bottomDateSheet.resizeRelocate(32.0, 32.0, 420.0, 220.0);
                sideDateSheet.resizeRelocate(500.0, 32.0, 360.0, 220.0);
                bottomTimeSheet.resizeRelocate(32.0, 260.0, 420.0, 220.0);
                sideTimeSheet.resizeRelocate(500.0, 260.0, 360.0, 220.0);
                bottomRangeSheet.resizeRelocate(32.0, 488.0, 420.0, 220.0);
                sideRangeSheet.resizeRelocate(500.0, 488.0, 360.0, 220.0);
                bottomDateField.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                sideDateField.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                bottomTimeField.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                sideTimeField.resizeRelocate(0.0, 0.0, 260.0, 64.0);
                bottomRangeField.resizeRelocate(0.0, 0.0, 340.0, 64.0);
                sideRangeField.resizeRelocate(0.0, 0.0, 340.0, 64.0);
                root.layout();

                assertHiddenSheetPickerValueTargetRejected(
                        bottomDateSheet,
                        bottomDateField,
                        LocalDate.of(2026, 8, 20)
                );
                assertHiddenSheetPickerValueTargetRejected(
                        sideDateSheet,
                        sideDateField,
                        LocalDate.of(2026, 8, 20)
                );
                assertHiddenSheetPickerValueTargetRejected(
                        bottomTimeSheet,
                        bottomTimeField,
                        LocalTime.of(13, 0)
                );
                assertHiddenSheetPickerValueTargetRejected(
                        sideTimeSheet,
                        sideTimeField,
                        LocalTime.of(13, 0)
                );
                assertHiddenSheetDateRangePickerValueTargetRejected(
                        bottomRangeSheet,
                        bottomRangeField,
                        LocalDate.of(2026, 8, 20)
                );
                assertHiddenSheetDateRangePickerValueTargetRejected(
                        sideRangeSheet,
                        sideRangeField,
                        LocalDate.of(2026, 8, 20)
                );
            } finally {
                bottomDateSheet.hide();
                sideDateSheet.hide();
                bottomTimeSheet.hide();
                sideTimeSheet.hide();
                bottomRangeSheet.hide();
                sideRangeSheet.hide();
                stage.close();
            }
        });
    }

    /// Verifies that closed reveal owners reject hidden explicit node targets before changing visible state.
    @Test
    void closedRevealOwnersRejectHiddenNodeTargetsBeforeStateChanges() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button bottomVisibleAction = new M3Button("Bottom visible");
            M3Button bottomHiddenAction = new M3Button("Bottom hidden");
            bottomHiddenAction.setVisible(false);
            M3BottomSheet bottomSheet = bottomSheet(
                    "Bottom actions",
                    new StackPane(bottomVisibleAction, bottomHiddenAction)
            );
            bottomSheet.hide();

            M3Button sideVisibleAction = new M3Button("Side visible");
            M3Button sideHiddenAction = new M3Button("Side hidden");
            sideHiddenAction.setVisible(false);
            M3SideSheet sideSheet = sideSheet(
                    "Side actions",
                    new StackPane(sideVisibleAction, sideHiddenAction)
            );
            sideSheet.hide();

            M3Button searchVisibleAction = new M3Button("Search visible");
            M3Button searchHiddenAction = new M3Button("Search hidden");
            searchHiddenAction.setVisible(false);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.getTrailingActions().setAll(new StackPane(searchVisibleAction, searchHiddenAction));

            M3Button resultVisibleAction = new M3Button("Result visible");
            M3Button resultHiddenAction = new M3Button("Result hidden");
            resultHiddenAction.setVisible(false);
            M3ListItem result = new M3ListItem("Result");
            result.setTrailing(new StackPane(resultVisibleAction, resultHiddenAction));
            M3SearchView searchView = searchView("Search results", result);

            M3Button outside = new M3Button("Outside");
            Stage stage = new Stage();
            try {
                Pane root = new Pane(outside, bottomSheet, sideSheet, searchBar, searchView);
                Scene scene = new Scene(root, 860.0, 620.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(16.0, 16.0, 120.0, 40.0);
                bottomSheet.resizeRelocate(32.0, 80.0, 360.0, 160.0);
                sideSheet.resizeRelocate(440.0, 80.0, 320.0, 180.0);
                searchBar.resizeRelocate(32.0, 300.0, 420.0, 56.0);
                searchView.resizeRelocate(32.0, 390.0, 480.0, 170.0);
                searchView.deactivate();
                root.layout();

                assertFalse(searchView.isActive());
                outside.requestFocus();
                assertTrue(outside.isFocused());

                bottomSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomHiddenAction);
                sideSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, sideHiddenAction);
                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, searchHiddenAction);
                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, resultHiddenAction);
                root.layout();

                assertFalse(bottomSheet.isShown());
                assertFalse(sideSheet.isShown());
                assertFalse(searchBar.isActive());
                assertFalse(searchView.isActive());
                assertFalse(bottomHiddenAction.isFocused());
                assertFalse(sideHiddenAction.isFocused());
                assertFalse(searchHiddenAction.isFocused());
                assertFalse(resultHiddenAction.isFocused());
                assertTrue(outside.isFocused());

                bottomSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomVisibleAction);
                assertTrue(bottomSheet.isShown());
                assertTrue(bottomVisibleAction.isFocused());
                assertSame(bottomVisibleAction, bottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                sideSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, sideVisibleAction);
                assertTrue(sideSheet.isShown());
                assertTrue(sideVisibleAction.isFocused());
                assertSame(sideVisibleAction, sideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, searchVisibleAction);
                assertTrue(searchBar.isActive());
                assertTrue(searchVisibleAction.isFocused());
                assertPopupFocusRoutedByContainer(searchBar, searchVisibleAction);

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, resultVisibleAction);
                assertTrue(searchView.isActive());
                assertTrue(resultVisibleAction.isFocused());
                assertPopupFocusRoutedByContainer(searchView, resultVisibleAction);
            } finally {
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
            searchBar.getTrailingActions().setAll(menuButton);
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

    /// Verifies that search bars reveal trailing split-button popup targets from explicit accessibility actions.
    @Test
    void searchBarRevealsNestedTrailingSplitButtonPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.getTrailingActions().setAll(splitButton);

            assertFalse(searchBar.isActive());
            assertContainerRevealsNestedSplitButtonPopupTarget(searchBar, splitButton, publishItem, 420.0, 56.0);
            assertTrue(searchBar.isActive());
        });
    }

    /// Verifies that search bars reject unreachable trailing menu popup targets before activating.
    @Test
    void searchBarRejectsUnreachableNestedTrailingMenuPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.getTrailingActions().setAll(menuButton);

            assertFalse(searchBar.isActive());
            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    searchBar,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    420.0,
                    56.0
            );
            assertFalse(searchBar.isActive());
        });
    }

    /// Verifies that search bars reject unreachable trailing split-button popup targets before activating.
    @Test
    void searchBarRejectsUnreachableNestedTrailingSplitButtonPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.getTrailingActions().setAll(splitButton);

            assertFalse(searchBar.isActive());
            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    searchBar,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    420.0,
                    56.0
            );
            assertFalse(searchBar.isActive());
        });
    }

    /// Verifies that search bars reveal trailing rich tooltip actions from explicit accessibility actions.
    @Test
    void searchBarRevealsNestedTrailingRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows more search options.",
                    tooltipAction
            );
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.getTrailingActions().setAll(ownerAction);
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

    /// Verifies that search views reveal nested menu popup targets from result rows.
    @Test
    void searchViewRevealsNestedResultMenuPopupTarget() {
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

    /// Verifies that search views reveal menu popup targets owned by the embedded search bar.
    @Test
    void searchViewRevealsNestedSearchBarMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            M3SearchView searchView = searchView("Search");
            searchView.getTrailingActions().setAll(menuButton);

            searchView.deactivate();
            assertFalse(searchView.isActive());
            assertContainerRevealsNestedMenuPopupTarget(searchView, menuButton, archiveItem, 480.0, 180.0);
            assertTrue(searchView.isActive());
        });
    }

    /// Verifies that search views reveal split-button targets owned by the embedded search bar.
    @Test
    void searchViewRevealsNestedSearchBarSplitButtonPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3SearchView searchView = searchView("Search");
            searchView.getTrailingActions().setAll(splitButton);

            searchView.deactivate();
            assertFalse(searchView.isActive());
            assertContainerRevealsNestedSplitButtonPopupTarget(searchView, splitButton, publishItem, 480.0, 180.0);
            assertTrue(searchView.isActive());
        });
    }

    /// Verifies that search views reveal nested split-button popup targets from result rows.
    @Test
    void searchViewRevealsNestedResultSplitButtonPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(splitButton);
            M3SearchView searchView = searchView("Search", result);

            searchView.deactivate();
            assertFalse(searchView.isActive());
            assertContainerRevealsNestedSplitButtonPopupTarget(searchView, splitButton, publishItem, 480.0, 180.0);
            assertTrue(searchView.isActive());
        });
    }

    /// Verifies that search views reject unreachable embedded search-bar menu targets before activating.
    @Test
    void searchViewRejectsUnreachableNestedSearchBarMenuPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3SearchView searchView = searchView("Search");
            searchView.getTrailingActions().setAll(menuButton);

            searchView.deactivate();
            assertFalse(searchView.isActive());
            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    searchView,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    480.0,
                    180.0
            );
            assertFalse(searchView.isActive());
        });
    }

    /// Verifies that search views reject unreachable embedded search-bar split-button targets before activating.
    @Test
    void searchViewRejectsUnreachableNestedSearchBarSplitButtonPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3SearchView searchView = searchView("Search");
            searchView.getTrailingActions().setAll(splitButton);

            searchView.deactivate();
            assertFalse(searchView.isActive());
            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    searchView,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    480.0,
                    180.0
            );
            assertFalse(searchView.isActive());
        });
    }

    /// Verifies that search views reject unreachable result-row menu targets before activating.
    @Test
    void searchViewRejectsUnreachableNestedResultMenuPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(menuButton);
            M3SearchView searchView = searchView("Search", result);

            searchView.deactivate();
            assertFalse(searchView.isActive());
            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    searchView,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    480.0,
                    180.0
            );
            assertFalse(searchView.isActive());
        });
    }

    /// Verifies that search views reject unreachable result-row split-button targets before activating.
    @Test
    void searchViewRejectsUnreachableNestedResultSplitButtonPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(splitButton);
            M3SearchView searchView = searchView("Search", result);

            searchView.deactivate();
            assertFalse(searchView.isActive());
            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    searchView,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    480.0,
                    180.0
            );
            assertFalse(searchView.isActive());
        });
    }

    /// Verifies that search views reveal nested rich tooltip actions from result rows.
    @Test
    void searchViewRevealsNestedResultRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows result details.",
                    tooltipAction
            );
            M3ListItem result = new M3ListItem("Document");
            result.setTrailing(ownerAction);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows search view options.",
                    tooltipAction
            );
            M3SearchView searchView = searchView("Search");
            searchView.getTrailingActions().setAll(ownerAction);
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


    /// Verifies that search views reveal date picker targets owned by the embedded search bar.
    @Test
    void searchViewRevealsNestedSearchBarDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 25);
            M3SearchView searchView = searchView("Search schedule");
            searchView.getTrailingActions().setAll(field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchView);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchView.resizeRelocate(32.0, 32.0, 640.0, 220.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(searchView, field, targetDate);
                assertTrue(searchView.isActive());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that search views reveal time picker targets owned by the embedded search bar.
    @Test
    void searchViewRevealsNestedSearchBarTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(13, 45);
            M3SearchView searchView = searchView("Search schedule");
            searchView.getTrailingActions().setAll(field);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchView);
                Scene scene = new Scene(root, 760.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchView.resizeRelocate(32.0, 32.0, 640.0, 220.0);
                root.layout();

                assertPickerValueTargetRoutedByContainer(searchView, field, targetTime);
                assertTrue(searchView.isActive());
            } finally {
                field.hidePicker();
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
            M3SearchView searchView = searchView("Search schedule");
            searchView.getTrailingActions().setAll(field);
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


    /// Verifies that search views reveal date picker targets from result content.
    @Test
    void searchViewRevealsNestedResultDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 26);
            Pane result = new Pane(field);
            result.setPrefSize(640.0, 96.0);
            M3SearchView searchView = searchView("Search schedule", result);
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

                assertPickerValueTargetRoutedByContainer(searchView, field, targetDate);
                assertTrue(searchView.isActive());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that search views reveal time picker targets from result content.
    @Test
    void searchViewRevealsNestedResultTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(14, 30);
            Pane result = new Pane(field);
            result.setPrefSize(640.0, 96.0);
            M3SearchView searchView = searchView("Search schedule", result);
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

                assertPickerValueTargetRoutedByContainer(searchView, field, targetTime);
                assertTrue(searchView.isActive());
            } finally {
                field.hidePicker();
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
            M3SearchView searchView = searchView("Search schedule", result);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(ownerAction);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
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

    /// Verifies that collapsed navigation drawer groups reveal nested child date picker targets.
    @Test
    void navigationDrawerGroupRevealsNestedChildDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertPickerValueTargetRoutedByContainer(
                        group,
                        field,
                        LocalDate.of(2026, 8, 20)
                );
                assertTrue(group.isExpanded());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that collapsed navigation drawer groups reveal nested child time picker targets.
    @Test
    void navigationDrawerGroupRevealsNestedChildTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertPickerValueTargetRoutedByContainer(
                        group,
                        field,
                        LocalTime.of(11, 45)
                );
                assertTrue(group.isExpanded());
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that collapsed navigation drawer groups reveal nested child date-range picker targets.
    @Test
    void navigationDrawerGroupRevealsNestedChildDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 12),
                    LocalDate.of(2026, 8, 18)
            );
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 620.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 500.0, 280.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertCollapsedDateRangePickerValueTargetRoutedByContainer(
                        group,
                        group,
                        field,
                        LocalDate.of(2026, 8, 24)
                );
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that collapsed navigation drawer groups reject disabled child date picker targets.
    @Test
    void navigationDrawerGroupRejectsDisabledNestedChildDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            field.getPicker().setMaxDate(LocalDate.of(2026, 8, 18));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertCollapsedPickerValueTargetRejectedByContainer(
                        group,
                        group,
                        field,
                        LocalDate.of(2026, 8, 20)
                );
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that collapsed navigation drawer groups reject disabled child time picker targets.
    @Test
    void navigationDrawerGroupRejectsDisabledNestedChildTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            field.getPicker().setMaxTime(LocalTime.of(10, 0));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertCollapsedPickerValueTargetRejectedByContainer(
                        group,
                        group,
                        field,
                        LocalTime.of(11, 45)
                );
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that collapsed navigation drawer groups reject disabled child date-range picker targets.
    @Test
    void navigationDrawerGroupRejectsDisabledNestedChildDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 12),
                    LocalDate.of(2026, 8, 18)
            );
            field.getPicker().setMaxDate(LocalDate.of(2026, 8, 18));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 620.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 500.0, 280.0);
                root.layout();

                assertCollapsedDateRangePickerValueTargetRejectedByContainer(
                        group,
                        group,
                        field,
                        LocalDate.of(2026, 8, 24)
                );
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that expanded navigation drawer groups preserve active child rich tooltip focus for default actions.
    @Test
    void navigationDrawerGroupPreservesActiveChildRichTooltipFocusForDefaultActions() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(ownerAction);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(true);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(group);
                Scene scene = new Scene(root, 420.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                group.resizeRelocate(32.0, 32.0, 320.0, 180.0);
                root.layout();

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 320.0, stage.getY() + 144.0);

                assertTrue(group.isExpanded());
                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, childItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(group, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers preserve active expanded group rich tooltip focus for default actions.
    @Test
    void navigationDrawerPreservesActiveExpandedGroupRichTooltipFocusForDefaultActions() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(ownerAction);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(true);
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

                ownerAction.requestFocus();
                tooltip.show(ownerAction, stage.getX() + 360.0, stage.getY() + 176.0);

                assertTrue(group.isExpanded());
                assertTrue(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(ownerAction, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                ownerAction.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, childItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tooltipAction, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(drawer, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(ownerAction.isFocused());
                assertSame(ownerAction, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
                assertSame(archiveItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(archiveItem, group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(drawer, archiveItem);
            } finally {
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reveal nested rich tooltip actions from top-level rows.
    @Test
    void navigationDrawerRevealsNestedTopLevelRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem item = new M3ListItem("Destination");
            item.setTrailing(ownerAction);
            M3NavigationDrawer drawer = navigationDrawer(item);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 480.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 360.0, 180.0);
                root.layout();

                assertFalse(tooltip.isShowing());

                drawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, item.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(drawer, tooltipAction);
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(ownerAction, tooltip);
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reject unreachable top-level rich tooltip actions without focusing the row.
    @Test
    void navigationDrawerRejectsDisabledNestedTopLevelRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button outside = new M3Button("Outside");
            M3Button ownerAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            tooltipAction.setDisable(true);
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem item = new M3ListItem("Destination");
            item.setTrailing(ownerAction);
            M3NavigationDrawer drawer = navigationDrawer(item);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(outside, drawer);
                Scene scene = new Scene(root, 520.0, 280.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                outside.resizeRelocate(16.0, 16.0, 120.0, 48.0);
                drawer.resizeRelocate(32.0, 88.0, 360.0, 160.0);
                root.layout();

                outside.requestFocus();
                assertTrue(outside.isFocused());
                assertFalse(tooltip.isShowing());

                drawer.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipAction.isFocused());
                assertFalse(item.isFocused());
                assertSame(outside, scene.getFocusOwner());
                assertNotSame(tooltipAction, drawer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltip.hide();
                M3Tooltip.uninstall(ownerAction, tooltip);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows drawer destination details.",
                    tooltipAction
            );
            M3ListItem childItem = new M3ListItem("Destination");
            childItem.setTrailing(ownerAction);
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

    /// Verifies that navigation drawers reveal nested date picker targets from collapsed group child rows.
    @Test
    void navigationDrawerRevealsNestedCollapsedGroupDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertPickerValueTargetRoutedByContainer(
                        drawer,
                        field,
                        LocalDate.of(2026, 8, 20)
                );
                assertTrue(group.isExpanded());
                assertSame(field.getEditor(), group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reveal nested time picker targets from collapsed group child rows.
    @Test
    void navigationDrawerRevealsNestedCollapsedGroupTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertPickerValueTargetRoutedByContainer(
                        drawer,
                        field,
                        LocalTime.of(11, 45)
                );
                assertTrue(group.isExpanded());
                assertSame(field.getEditor(), group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reveal nested date-range picker targets from collapsed group child rows.
    @Test
    void navigationDrawerRevealsNestedCollapsedGroupDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 12),
                    LocalDate.of(2026, 8, 18)
            );
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 620.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 500.0, 280.0);
                root.layout();

                assertFalse(group.isExpanded());
                assertCollapsedDateRangePickerValueTargetRoutedByContainer(
                        drawer,
                        group,
                        field,
                        LocalDate.of(2026, 8, 24)
                );
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reject disabled date picker targets from collapsed group child rows.
    @Test
    void navigationDrawerRejectsDisabledNestedCollapsedGroupDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 8, 12));
            field.getPicker().setMaxDate(LocalDate.of(2026, 8, 18));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertCollapsedPickerValueTargetRejectedByContainer(
                        drawer,
                        group,
                        field,
                        LocalDate.of(2026, 8, 20)
                );
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reject disabled time picker targets from collapsed group child rows.
    @Test
    void navigationDrawerRejectsDisabledNestedCollapsedGroupTimePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            field.getPicker().setMaxTime(LocalTime.of(10, 0));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 540.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 420.0, 260.0);
                root.layout();

                assertCollapsedPickerValueTargetRejectedByContainer(
                        drawer,
                        group,
                        field,
                        LocalTime.of(11, 45)
                );
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that navigation drawers reject disabled date-range picker targets from collapsed group child rows.
    @Test
    void navigationDrawerRejectsDisabledNestedCollapsedGroupDateRangePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 8, 12),
                    LocalDate.of(2026, 8, 18)
            );
            field.getPicker().setMaxDate(LocalDate.of(2026, 8, 18));
            M3ListItem childItem = new M3ListItem("Schedule");
            childItem.setTrailing(field);
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Navigation");
            group.getItems().add(childItem);
            group.setExpanded(false);
            M3NavigationDrawer drawer = navigationDrawer(group);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(drawer);
                Scene scene = new Scene(root, 620.0, 380.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                drawer.resizeRelocate(32.0, 32.0, 500.0, 280.0);
                root.layout();

                assertCollapsedDateRangePickerValueTargetRejectedByContainer(
                        drawer,
                        group,
                        field,
                        LocalDate.of(2026, 8, 24)
                );
            } finally {
                field.hidePicker();
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
            M3TopAppBar appBar = topAppBar("Project", (Node) null, menuButton);
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
            M3TopAppBar appBar = topAppBar("Project", (Node) null, menuButton);
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

    /// Verifies that top app bar action slots reveal split-button menu targets.
    @Test
    void topAppBarRevealsNestedSplitButtonPopupTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3TopAppBar appBar = topAppBar("Project", (Node) null, splitButton);

            assertContainerRevealsNestedSplitButtonPopupTarget(appBar, splitButton, publishItem, 640.0, 72.0);
        });
    }

    /// Verifies that bottom app bar action slots reveal menu popup targets.
    @Test
    void bottomAppBarRevealsNestedMenuPopupTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3BottomAppBar appBar = bottomAppBar(new M3Button("Search"), menuButton);

            assertContainerRevealsNestedMenuPopupTarget(appBar, menuButton, archiveItem, 640.0, 96.0);
        });
    }

    /// Verifies that bottom app bar action slots reveal split-button menu targets.
    @Test
    void bottomAppBarRevealsNestedSplitButtonPopupTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3BottomAppBar appBar = bottomAppBar(new M3Button("Search"), splitButton);

            assertContainerRevealsNestedSplitButtonPopupTarget(appBar, splitButton, publishItem, 640.0, 96.0);
        });
    }

    /// Verifies that toolbar item slots reveal menu popup targets.
    @Test
    void toolbarRevealsNestedMenuPopupTargetFromItemSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3Toolbar toolbar = toolbar(new M3Button("Back"), menuButton, new M3Button("Share"));

            assertContainerRevealsNestedMenuPopupTarget(toolbar, menuButton, archiveItem, 520.0, 72.0);
        });
    }

    /// Verifies that toolbar item slots reveal split-button menu targets.
    @Test
    void toolbarRevealsNestedSplitButtonPopupTargetFromItemSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = splitButton("Create", draftItem, publishItem);
            M3Toolbar toolbar = toolbar(new M3Button("Back"), splitButton, new M3Button("Share"));

            assertContainerRevealsNestedSplitButtonPopupTarget(toolbar, splitButton, publishItem, 560.0, 72.0);
        });
    }

    /// Verifies that top app bar action slots reject unreachable menu popup targets before opening popups.
    @Test
    void topAppBarRejectsUnreachableNestedMenuPopupTargetsFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3TopAppBar appBar = topAppBar("Project", (Node) null, menuButton);

            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    appBar,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    640.0,
                    72.0
            );
        });
    }

    /// Verifies that top app bar action slots reject unreachable split-button popup targets before opening popups.
    @Test
    void topAppBarRejectsUnreachableNestedSplitButtonPopupTargetsFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3TopAppBar appBar = topAppBar("Project", (Node) null, splitButton);

            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    appBar,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    640.0,
                    72.0
            );
        });
    }

    /// Verifies that bottom app bar action slots reject unreachable menu popup targets before opening popups.
    @Test
    void bottomAppBarRejectsUnreachableNestedMenuPopupTargetsFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3BottomAppBar appBar = bottomAppBar(new M3Button("Search"), menuButton);

            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    appBar,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    640.0,
                    96.0
            );
        });
    }

    /// Verifies that bottom app bar action slots reject unreachable split-button popup targets before opening popups.
    @Test
    void bottomAppBarRejectsUnreachableNestedSplitButtonPopupTargetsFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3BottomAppBar appBar = bottomAppBar(new M3Button("Search"), splitButton);

            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    appBar,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    640.0,
                    96.0
            );
        });
    }

    /// Verifies that toolbar item slots reject unreachable menu popup targets before opening popups.
    @Test
    void toolbarRejectsUnreachableNestedMenuPopupTargetsFromItemSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3MenuButton menuButton = unreachableTargetMenuButton(hiddenItem, disabledItem);
            M3Toolbar toolbar = toolbar(new M3Button("Back"), menuButton, new M3Button("Share"));

            assertContainerRejectsUnreachableNestedMenuPopupTargets(
                    toolbar,
                    menuButton,
                    hiddenItem,
                    disabledItem,
                    520.0,
                    72.0
            );
        });
    }

    /// Verifies that toolbar item slots reject unreachable split-button popup targets before opening popups.
    @Test
    void toolbarRejectsUnreachableNestedSplitButtonPopupTargetsFromItemSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem hiddenItem = new M3MenuItem("Hidden");
            M3MenuItem disabledItem = new M3MenuItem("Disabled");
            M3SplitButton splitButton = unreachableTargetSplitButton(hiddenItem, disabledItem);
            M3Toolbar toolbar = toolbar(new M3Button("Back"), splitButton, new M3Button("Share"));

            assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
                    toolbar,
                    splitButton,
                    hiddenItem,
                    disabledItem,
                    560.0,
                    72.0
            );
        });
    }

    /// Verifies that top app bars reveal rich tooltip actions owned by navigation slots.
    @Test
    void topAppBarRevealsNavigationRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button navigation = new M3Button("Nav");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = installRichTooltip(
                    navigation,
                    "Navigation",
                    "Shows navigation details.",
                    tooltipAction
            );
            M3TopAppBar appBar = topAppBar("Project", navigation);
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
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "More",
                    "Shows project actions.",
                    tooltipAction
            );
            M3TopAppBar appBar = topAppBar("Project", (Node) null, ownerAction);
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
            M3BottomAppBar appBar = bottomAppBar(
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
            M3RichTooltip tooltip = installRichTooltip(
                    floatingAction,
                    "Create",
                    "Shows creation details.",
                    tooltipAction
            );
            M3BottomAppBar appBar = bottomAppBar(
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

    /// Verifies that toolbars reveal menu targets owned by rich tooltip action nodes.
    @Test
    void toolbarRevealsMenuItemInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Format");
            M3MenuItem targetItem = new M3MenuItem("Archive");
            M3MenuButton tooltipMenu = new M3MenuButton(
                    "More",
                    new M3MenuItem("Rename"),
                    targetItem
            );
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Format",
                    "Shows formatting details.",
                    tooltipMenu
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 560.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 320.0, 72.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipMenu.isShowing());

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetItem);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipMenu.isShowing());
                assertTrue(targetItem.isFocused());
                assertSame(targetItem, tooltipMenu.getMenu().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(targetItem, tooltipMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(targetItem, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipMenu.hideMenu();
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip action nodes reject disabled nested menu targets before opening popups.
    @Test
    void toolbarRejectsDisabledMenuItemInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Format");
            M3MenuItem disabledTarget = new M3MenuItem("Archive");
            disabledTarget.setDisable(true);
            M3MenuButton tooltipMenu = new M3MenuButton(
                    "More",
                    new M3MenuItem("Rename"),
                    disabledTarget
            );
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Format",
                    "Shows formatting details.",
                    tooltipMenu
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 560.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 320.0, 72.0);
                root.layout();
                ownerAction.requestFocus();

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledTarget);

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipMenu.isShowing());
                assertFalse(disabledTarget.isFocused());
                assertSame(ownerAction, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipMenu.hideMenu();
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that toolbars reveal picker value targets owned by rich tooltip action nodes.
    @Test
    void toolbarRevealsDatePickerValueInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Schedule");
            M3DatePickerField tooltipDateField = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 18);
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Schedule",
                    "Choose a target date.",
                    tooltipDateField
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 640.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 360.0, 72.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipDateField.isShowing());

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                assertTrue(tooltip.isShowing());
                assertTrue(tooltipDateField.isShowing());
                assertSame(tooltipDateField.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE),
                        toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipDateField.hidePicker();
                tooltip.hide();
                stage.close();
            }
        });
    }
    /// Verifies that rich tooltip action nodes reject disabled picker values before opening popups.
    @Test
    void toolbarRejectsDisabledDatePickerValueInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Schedule");
            M3DatePickerField tooltipDateField = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            tooltipDateField.getPicker().setMaxDate(LocalDate.of(2026, 6, 18));
            LocalDate disabledDate = LocalDate.of(2026, 6, 24);
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Schedule",
                    "Choose a target date.",
                    tooltipDateField
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 640.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 360.0, 72.0);
                root.layout();
                ownerAction.requestFocus();

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledDate);

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipDateField.isShowing());
                assertFalse(tooltipDateField.getEditor().isFocused());
                assertSame(ownerAction, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipDateField.hidePicker();
                tooltip.hide();
                stage.close();
            }
        });
    }
    /// Verifies that toolbars reveal time picker targets owned by rich tooltip action nodes.
    @Test
    void toolbarRevealsTimePickerValueInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Schedule");
            M3TimePickerField tooltipTimeField = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(10, 45);
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Schedule",
                    "Choose a target time.",
                    tooltipTimeField
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 640.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 360.0, 72.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipTimeField.isShowing());

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetTime);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        tooltipTimeField.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipTimeField.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(tooltipTimeField.getPicker(), pickerFocusNode));
                assertSame(pickerFocusNode, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipTimeField.hidePicker();
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip action nodes reject out-of-range time picker values before opening popups.
    @Test
    void toolbarRejectsDisabledTimePickerValueInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Schedule");
            M3TimePickerField tooltipTimeField = new M3TimePickerField(LocalTime.of(9, 30));
            tooltipTimeField.getPicker().setMaxTime(LocalTime.of(10, 0));
            LocalTime disabledTime = LocalTime.of(11, 15);
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Schedule",
                    "Choose a target time.",
                    tooltipTimeField
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 640.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 360.0, 72.0);
                root.layout();
                ownerAction.requestFocus();

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledTime);

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipTimeField.isShowing());
                assertFalse(tooltipTimeField.getEditor().isFocused());
                assertSame(ownerAction, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipTimeField.hidePicker();
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that toolbars reveal date-range picker targets owned by rich tooltip action nodes.
    @Test
    void toolbarRevealsDateRangePickerValueInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Schedule");
            M3DateRangePickerField tooltipRangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            LocalDate targetDate = LocalDate.of(2026, 6, 22);
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Schedule",
                    "Choose a target range.",
                    tooltipRangeField
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 720.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 420.0, 72.0);
                root.layout();

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipRangeField.isShowing());

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetDate);

                Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                        Node.class,
                        tooltipRangeField.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                ));
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipRangeField.isShowing());
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(tooltipRangeField.getPicker(), pickerFocusNode));
                assertSame(pickerFocusNode, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipRangeField.hidePicker();
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip action nodes reject out-of-range date-range values before opening popups.
    @Test
    void toolbarRejectsDisabledDateRangePickerValueInsideRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button ownerAction = new M3Button("Schedule");
            M3DateRangePickerField tooltipRangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 14),
                    LocalDate.of(2026, 6, 18)
            );
            tooltipRangeField.getPicker().setMaxDate(LocalDate.of(2026, 6, 20));
            LocalDate disabledDate = LocalDate.of(2026, 6, 24);
            M3RichTooltip tooltip = installRichTooltip(
                    ownerAction,
                    "Schedule",
                    "Choose a target range.",
                    tooltipRangeField
            );
            M3Toolbar toolbar = toolbar(ownerAction, new M3Button("Share"));
            Stage stage = new Stage();

            try {
                Pane root = new Pane(toolbar);
                Scene scene = new Scene(root, 720.0, 460.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                toolbar.resizeRelocate(32.0, 32.0, 420.0, 72.0);
                root.layout();
                ownerAction.requestFocus();

                toolbar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, disabledDate);

                assertFalse(tooltip.isShowing());
                assertFalse(tooltipRangeField.isShowing());
                assertFalse(tooltipRangeField.getStartEditor().isFocused());
                assertFalse(tooltipRangeField.getEndEditor().isFocused());
                assertSame(ownerAction, toolbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                tooltipRangeField.hidePicker();
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
            M3TopAppBar appBar = topAppBar("Schedule", (Node) null, field);
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
            M3BottomAppBar appBar = bottomAppBar(new M3Button("Search"), field);
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


    /// Verifies that top app bar action slots reveal time picker value targets and expose popup focus.
    @Test
    void topAppBarRevealsNestedTimePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(10, 45);
            M3TopAppBar appBar = topAppBar("Schedule", (Node) null, field);
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

                assertPickerValueTargetRoutedByContainer(appBar, field, targetTime);
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that bottom app bar action slots reveal time picker value targets and expose popup focus.
    @Test
    void bottomAppBarRevealsNestedTimePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(11, 15);
            M3BottomAppBar appBar = bottomAppBar(new M3Button("Search"), field);
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

                assertPickerValueTargetRoutedByContainer(appBar, field, targetTime);
            } finally {
                field.hidePicker();
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
            M3Toolbar toolbar = toolbar(new M3Button("Back"), field, new M3Button("Save"));
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
            searchBar.getTrailingActions().setAll(field);
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


    /// Verifies that search bar trailing action slots reveal time picker value targets and expose popup focus.
    @Test
    void searchBarRevealsNestedTimePickerValueTargetFromTrailingAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(12, 30);
            M3SearchBar searchBar = new M3SearchBar("Search schedule");
            searchBar.getTrailingActions().setAll(field);
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

                assertPickerValueTargetRoutedByContainer(searchBar, field, targetTime);
            } finally {
                field.hidePicker();
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
            M3TopAppBar appBar = topAppBar("Schedule", (Node) null, field);
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
            M3BottomAppBar appBar = bottomAppBar(new M3Button("Search"), field);
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
            M3Toolbar toolbar = toolbar(new M3Button("Back"), field, new M3Button("Save"));
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
            searchBar.getTrailingActions().setAll(field);
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

    /// Verifies that search components reject disabled picker value targets before activating.
    @Test
    void searchComponentsRejectDisabledPickerValueTargetsBeforeActivating() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate invalidDate = LocalDate.of(2026, 6, 24);
            M3DatePickerField searchBarField = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            searchBarField.getPicker().setMaxDate(LocalDate.of(2026, 6, 18));
            M3SearchBar searchBar = new M3SearchBar("Search schedule");
            searchBar.getTrailingActions().setAll(searchBarField);
            searchBar.setActive(false);

            M3DatePickerField resultField = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            resultField.getPicker().setMaxDate(LocalDate.of(2026, 6, 18));
            M3SearchView searchView = searchView("Search results", new Pane(resultField));
            searchView.setActive(false);
            Stage stage = new Stage();

            try {
                Pane root = new Pane(searchBar, searchView);
                Scene scene = new Scene(root, 860.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                searchBar.resizeRelocate(32.0, 32.0, 720.0, 80.0);
                searchView.resizeRelocate(32.0, 140.0, 720.0, 180.0);
                resultField.resizeRelocate(0.0, 0.0, 320.0, 64.0);
                root.layout();

                assertFalse(searchBar.isActive());
                assertFalse(searchBarField.isShowing());
                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, invalidDate);
                assertFalse(searchBar.isActive());
                assertFalse(searchBarField.isShowing());
                assertFalse(searchBarField.getEditor().isFocused());

                assertFalse(searchView.isActive());
                assertFalse(resultField.isShowing());
                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, invalidDate);
                assertFalse(searchView.isActive());
                assertFalse(resultField.isShowing());
                assertFalse(resultField.getEditor().isFocused());
            } finally {
                searchBarField.hidePicker();
                resultField.hidePicker();
                stage.close();
            }
        });
    }

    /// Creates a key press event for popup keyboard tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /// Verifies the effective orientation of a menu item and the submenu it owns.
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

    /// Verifies the effective orientation of a parent submenu item and a nested submenu item.
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

    /// Verifies nested menu reveal, popup focus routing, and Escape restoration through one item container.
    private static void assertContainerRevealsNestedMenuPopupTarget(
            Node container,
            M3MenuButton menuButton,
            M3MenuItem targetItem,
            double width,
            double height
    ) {
        Stage stage = new Stage();

        try {
            Pane root = new Pane(container);
            Scene scene = new Scene(root, Math.max(520.0, width + 96.0), Math.max(320.0, height + 180.0));
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            container.resizeRelocate(32.0, 32.0, width, height);
            root.layout();

            assertFalse(menuButton.isShowing());

            container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetItem);
            root.layout();

            assertTrue(menuButton.isShowing());
            assertTrue(targetItem.isFocused());
            assertSame(targetItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            assertPopupFocusRoutedByContainer(container, targetItem);

            targetItem.fireEvent(keyPressed(KeyCode.ESCAPE));

            assertFalse(menuButton.isShowing());
            assertTrue(menuButton.isFocused());
            assertSame(menuButton, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        } finally {
            menuButton.hideMenu();
            stage.close();
        }
    }

    /// Verifies split-button reveal, popup focus routing, and Escape restoration through one item container.
    private static void assertContainerRevealsNestedSplitButtonPopupTarget(
            Node container,
            M3SplitButton splitButton,
            M3MenuItem targetItem,
            double width,
            double height
    ) {
        Stage stage = new Stage();

        try {
            Pane root = new Pane(container);
            Scene scene = new Scene(root, Math.max(560.0, width + 96.0), Math.max(340.0, height + 200.0));
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            container.resizeRelocate(32.0, 32.0, width, height);
            root.layout();

            assertFalse(splitButton.isShowing());

            container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetItem);
            root.layout();

            assertTrue(splitButton.isShowing());
            assertTrue(targetItem.isFocused());
            assertSame(targetItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            assertPopupFocusRoutedByContainer(container, targetItem);

            targetItem.fireEvent(keyPressed(KeyCode.ESCAPE));

            assertFalse(splitButton.isShowing());
            assertTrue(splitButtonMenuButton(splitButton).isFocused());
            assertSame(splitButtonMenuButton(splitButton), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        } finally {
            splitButton.hideMenu();
            stage.close();
        }
    }

    /// Creates a menu button with one hidden and one disabled target item.
    private static M3MenuButton unreachableTargetMenuButton(M3MenuItem hiddenItem, M3MenuItem disabledItem) {
        hiddenItem.setVisible(false);
        disabledItem.setDisable(true);
        return new M3MenuButton("Actions", hiddenItem, disabledItem, new M3MenuItem("Visible"));
    }

    /// Returns the primary action part exposed by a split button.
    private static M3Button splitButtonActionButton(M3SplitButton splitButton) {
        return assertInstanceOf(
                M3Button.class,
                splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0)
        );
    }

    /// Returns the menu part exposed by a split button.
    private static M3MenuButton splitButtonMenuButton(M3SplitButton splitButton) {
        return assertInstanceOf(
                M3MenuButton.class,
                splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1)
        );
    }

    /// Creates a split button with one hidden and one disabled target item.
    private static M3SplitButton unreachableTargetSplitButton(M3MenuItem hiddenItem, M3MenuItem disabledItem) {
        hiddenItem.setVisible(false);
        disabledItem.setDisable(true);
        return splitButton("Create", hiddenItem, disabledItem, new M3MenuItem("Visible"));
    }

    /// Verifies menu-target rejection through one container without opening the menu or moving focus.
    private static void assertContainerRejectsUnreachableNestedMenuPopupTargets(
            Node container,
            M3MenuButton menuButton,
            M3MenuItem hiddenTargetItem,
            M3MenuItem disabledTargetItem,
            double width,
            double height
    ) {
        Stage stage = new Stage();

        try {
            M3Button focusOwner = new M3Button("Focus owner");
            Pane root = new Pane(focusOwner, container);
            Scene scene = new Scene(root, Math.max(560.0, width + 96.0), Math.max(360.0, height + 220.0));
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            focusOwner.resizeRelocate(16.0, 16.0, 132.0, 48.0);
            container.resizeRelocate(32.0, 96.0, width, height);
            root.layout();

            assertContainerRejectsUnreachableMenuPopupTarget(
                    scene,
                    root,
                    focusOwner,
                    container,
                    menuButton,
                    hiddenTargetItem
            );
            assertContainerRejectsUnreachableMenuPopupTarget(
                    scene,
                    root,
                    focusOwner,
                    container,
                    menuButton,
                    disabledTargetItem
            );
        } finally {
            menuButton.hideMenu();
            stage.close();
        }
    }

    /// Verifies split-button target rejection through one container without opening the menu or moving focus.
    private static void assertContainerRejectsUnreachableNestedSplitButtonPopupTargets(
            Node container,
            M3SplitButton splitButton,
            M3MenuItem hiddenTargetItem,
            M3MenuItem disabledTargetItem,
            double width,
            double height
    ) {
        Stage stage = new Stage();

        try {
            M3Button focusOwner = new M3Button("Focus owner");
            Pane root = new Pane(focusOwner, container);
            Scene scene = new Scene(root, Math.max(560.0, width + 96.0), Math.max(360.0, height + 220.0));
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            focusOwner.resizeRelocate(16.0, 16.0, 132.0, 48.0);
            container.resizeRelocate(32.0, 96.0, width, height);
            root.layout();

            assertContainerRejectsUnreachableSplitButtonPopupTarget(
                    scene,
                    root,
                    focusOwner,
                    container,
                    splitButton,
                    hiddenTargetItem
            );
            assertContainerRejectsUnreachableSplitButtonPopupTarget(
                    scene,
                    root,
                    focusOwner,
                    container,
                    splitButton,
                    disabledTargetItem
            );
        } finally {
            splitButton.hideMenu();
            stage.close();
        }
    }

    /// Verifies that one unreachable menu target cannot open a menu popup.
    private static void assertContainerRejectsUnreachableMenuPopupTarget(
            Scene scene,
            Pane root,
            M3Button focusOwner,
            Node container,
            M3MenuButton menuButton,
            M3MenuItem targetItem
    ) {
        focusOwner.requestFocus();
        root.layout();

        assertTrue(focusOwner.isFocused());
        assertFalse(menuButton.isShowing());

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetItem);
        root.layout();

        assertFalse(menuButton.isShowing());
        assertFalse(targetItem.isFocused());
        assertSame(focusOwner, scene.getFocusOwner());
        assertNotSame(targetItem, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertNotSame(targetItem, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies that one unreachable split-button target cannot open a menu popup.
    private static void assertContainerRejectsUnreachableSplitButtonPopupTarget(
            Scene scene,
            Pane root,
            M3Button focusOwner,
            Node container,
            M3SplitButton splitButton,
            M3MenuItem targetItem
    ) {
        focusOwner.requestFocus();
        root.layout();

        assertTrue(focusOwner.isFocused());
        assertFalse(splitButton.isShowing());

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, targetItem);
        root.layout();

        assertFalse(splitButton.isShowing());
        assertFalse(targetItem.isFocused());
        assertSame(focusOwner, scene.getFocusOwner());
        assertNotSame(targetItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertNotSame(targetItem, container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies that a hidden sheet picker target is rejected without showing the sheet.
    private static void assertHiddenSheetPickerValueTargetRejected(
            Node sheet,
            M3PickerField<?, ?> field,
            Object valueTarget
    ) {
        assertFalse(isSheetShown(sheet));
        assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertFalse(field.isShowing());

        sheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(isSheetShown(sheet));
        assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertFalse(field.isShowing());
        assertFalse(field.getEditor().isFocused());
    }

    /// Verifies that a hidden sheet date-range target is rejected without showing the sheet.
    private static void assertHiddenSheetDateRangePickerValueTargetRejected(
            Node sheet,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(isSheetShown(sheet));
        assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertFalse(field.isShowing());

        sheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(isSheetShown(sheet));
        assertNull(sheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertFalse(field.isShowing());
        assertFalse(field.getStartEditor().isFocused());
        assertFalse(field.getEndEditor().isFocused());
    }

    /// Returns the shown state for a sheet control.
    private static boolean isSheetShown(Node sheet) {
        if (sheet instanceof M3BottomSheet bottomSheet) {
            return bottomSheet.isShown();
        }
        if (sheet instanceof M3SideSheet sideSheet) {
            return sideSheet.isShown();
        }
        throw new IllegalArgumentException("Unsupported sheet node: " + sheet);
    }

    /// Verifies that a direct menu-button picker target is rejected without opening its menu.
    private static void assertDirectMenuPickerValueTargetRejected(
            M3MenuButton menuButton,
            M3PickerField<?, ?> field,
            Object valueTarget
    ) {
        assertFalse(menuButton.isShowing());
        assertFalse(field.isShowing());

        menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(menuButton.isShowing());
        assertFalse(field.isShowing());
        assertFalse(field.getEditor().isFocused());
    }

    /// Verifies that a direct menu-button date-range target is rejected without opening its menu.
    private static void assertDirectMenuDateRangePickerValueTargetRejected(
            M3MenuButton menuButton,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(menuButton.isShowing());
        assertFalse(field.isShowing());

        menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(menuButton.isShowing());
        assertFalse(field.isShowing());
        assertFalse(field.getStartEditor().isFocused());
        assertFalse(field.getEndEditor().isFocused());
    }

    /// Verifies that a nested menu-button picker target is rejected without opening either menu.
    private static void assertNestedMenuPickerValueTargetRejected(
            M3MenuButton menuButton,
            M3SubMenuItem subMenuItem,
            M3PickerField<?, ?> field,
            Object valueTarget
    ) {
        assertFalse(menuButton.isShowing());
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());

        menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(menuButton.isShowing());
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());
        assertFalse(field.getEditor().isFocused());
    }

    /// Verifies that a nested menu-button date-range target is rejected without opening either menu.
    private static void assertNestedMenuDateRangePickerValueTargetRejected(
            M3MenuButton menuButton,
            M3SubMenuItem subMenuItem,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(menuButton.isShowing());
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());

        menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(menuButton.isShowing());
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());
        assertFalse(field.getStartEditor().isFocused());
        assertFalse(field.getEndEditor().isFocused());
    }

    /// Verifies that a submenu picker target is rejected without opening the submenu.
    private static void assertSubMenuPickerValueTargetRejected(
            M3SubMenuItem subMenuItem,
            M3PickerField<?, ?> field,
            Object valueTarget
    ) {
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());

        subMenuItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());
        assertFalse(field.getEditor().isFocused());
    }

    /// Verifies that a submenu date-range target is rejected without opening the submenu.
    private static void assertSubMenuDateRangePickerValueTargetRejected(
            M3SubMenuItem subMenuItem,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());

        subMenuItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(subMenuItem.isSubMenuShowing());
        assertFalse(field.isShowing());
        assertFalse(field.getStartEditor().isFocused());
        assertFalse(field.getEndEditor().isFocused());
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

    /// Verifies a collapsed container ignores a disabled picker value target.
    private static void assertCollapsedPickerValueTargetRejectedByContainer(
            Node container,
            M3NavigationDrawerGroup group,
            M3PickerField<?, ?> field,
            Object valueTarget
    ) {
        assertFalse(group.isExpanded());
        assertFalse(field.isShowing());

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(group.isExpanded());
        assertFalse(field.isShowing());
        assertFalse(field.getEditor().isFocused());
    }

    /// Verifies a collapsed container ignores a disabled date-range picker value target.
    private static void assertCollapsedDateRangePickerValueTargetRejectedByContainer(
            Node container,
            M3NavigationDrawerGroup group,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(group.isExpanded());
        assertFalse(field.isShowing());

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        assertFalse(group.isExpanded());
        assertFalse(field.isShowing());
        assertFalse(field.getStartEditor().isFocused());
        assertFalse(field.getEndEditor().isFocused());
    }

    /// Verifies a collapsed container reveals a date-range picker value target before endpoint focus exists.
    private static void assertCollapsedDateRangePickerValueTargetRoutedByContainer(
            Node container,
            M3NavigationDrawerGroup group,
            M3DateRangePickerField field,
            LocalDate valueTarget
    ) {
        assertFalse(field.isShowing());

        container.executeAccessibleAction(AccessibleAction.SHOW_ITEM, valueTarget);

        Node pickerFocusNode = Objects.requireNonNull(assertInstanceOf(
                Node.class,
                field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
        ));
        assertTrue(group.isExpanded());
        assertTrue(field.isShowing());
        assertTrue(pickerFocusNode.isFocused());
        assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
        assertPopupFocusRoutedByContainer(container, pickerFocusNode);

        field.getPicker().fireEvent(keyPressed(KeyCode.ESCAPE));

        assertFalse(field.isShowing());
        assertTrue(field.getStartEditor().isFocused());
        assertSame(field.getStartEditor(), group.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertSame(field.getStartEditor(), container.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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

    /// Creates and installs a rich tooltip for popup focus tests.
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
