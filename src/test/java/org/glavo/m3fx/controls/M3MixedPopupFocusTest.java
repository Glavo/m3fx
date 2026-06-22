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
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /// Verifies that an active nested submenu rich tooltip focus chain survives runtime orientation changes.
    @Test
    void richTooltipInsideNestedSubMenuPreservesFocusThroughRuntimeOrientationChanges() {
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

    /// Verifies that validation summaries reveal invalid-input adornment rich tooltip actions.
    @Test
    void validationSummaryRevealsInvalidInputAdornmentRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TextField nameField = new M3TextField();
            M3IconButton helpButton = new M3IconButton(new M3Icon("?"));
            M3Button tooltipAction = new M3Button("Explain");
            M3RichTooltip tooltip = M3RichTooltip.install(
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

    /// Verifies that nested form containers reveal and route menu, tooltip, and picker popup targets.
    @Test
    void formContainersRevealNestedPopupTargetsAcrossRows() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);

            M3Button helpButton = new M3Button("Help");
            M3Button tooltipAction = new M3Button("Explain");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    helpButton,
                    "Form help",
                    "Explains how this form section is validated.",
                    tooltipAction
            );

            LocalDate targetDate = LocalDate.of(2026, 7, 6);
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 7, 1));
            M3FormRow actionRow = new M3FormRow("Actions", "Menu and help affordances", menuButton, helpButton);
            M3FormRow dateRow = new M3FormRow("Due date", "Date picker target", dateField);
            M3FormSection section = new M3FormSection("Project", actionRow, dateRow);
            M3FormPane form = new M3FormPane(section);
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

                assertPickerValueTargetRoutedByContainer(form, dateField, targetDate);

                assertSame(dateField.getEditor(), dateRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(dateField.getEditor(), section.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                dateField.hidePicker();
                tooltip.hide();
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
            M3FormSection section = new M3FormSection("Project", timeRow);
            M3FormPane form = new M3FormPane(section);
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
            M3FormSection section = new M3FormSection("Project", rangeRow);
            M3FormPane form = new M3FormPane(section);
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
            M3Surface surface = new M3Surface(content);
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

    /// Verifies that virtualized list views preserve popup focus exposed by a visible row for default actions.
    @Test
    void listViewPreservesVisibleRowPopupFocusForDefaultAccessibilityActions() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3Button rowAction = new M3Button("More");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    rowAction,
                    "Row action",
                    "Shows details for the visible row.",
                    tooltipAction
            );
            M3ListItem row = new M3ListItem("Project Alpha");
            row.setTrailing(rowAction);
            M3ListView<M3ListItem> listView = new M3ListView<>(row);
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

    /// Verifies that selection containers reveal nested menu targets owned by item content.
    @Test
    void selectionContainersRevealNestedMenuPopupTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem iconTarget = new M3MenuItem("Icon target");
            M3MenuButton iconMenu = new M3MenuButton("More", new M3MenuItem("First"), iconTarget);
            M3IconToggleButtonGroup iconGroup = new M3IconToggleButtonGroup(
                    new M3IconToggleButton(iconMenu),
                    new M3IconToggleButton("B")
            );
            assertContainerRevealsNestedMenuPopupTarget(iconGroup, iconMenu, iconTarget, 360.0, 96.0);

            M3MenuItem segmentedTarget = new M3MenuItem("Segment target");
            M3MenuButton segmentedMenu = new M3MenuButton("More", new M3MenuItem("First"), segmentedTarget);
            M3SegmentedButtonGroup segmentedGroup = new M3SegmentedButtonGroup(
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
            M3ChipGroup chipGroup = new M3ChipGroup(
                    new M3Chip("More", chipMenu),
                    new M3Chip("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(chipGroup, chipMenu, chipTarget, 420.0, 96.0);

            M3MenuItem tabTarget = new M3MenuItem("Tab target");
            M3MenuButton tabMenu = new M3MenuButton("More", new M3MenuItem("First"), tabTarget);
            M3TabBar tabBar = new M3TabBar(
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
            M3NavigationBar navigationBar = new M3NavigationBar(
                    new M3NavigationItem("More", barMenu),
                    new M3NavigationItem("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(navigationBar, barMenu, barTarget, 460.0, 112.0);

            M3MenuItem railTarget = new M3MenuItem("Rail target");
            M3MenuButton railMenu = new M3MenuButton("More", new M3MenuItem("First"), railTarget);
            M3NavigationRail navigationRail = new M3NavigationRail(
                    new M3NavigationItem("More", railMenu),
                    new M3NavigationItem("Other")
            );
            assertContainerRevealsNestedMenuPopupTarget(navigationRail, railMenu, railTarget, 180.0, 260.0);

            M3MenuItem listTarget = new M3MenuItem("List target");
            M3MenuButton listMenu = new M3MenuButton("More", new M3MenuItem("First"), listTarget);
            M3ListItem listItem = new M3ListItem("More");
            listItem.setTrailing(listMenu);
            M3ListPane listPane = new M3ListPane(listItem, new M3ListItem("Other"));
            assertContainerRevealsNestedMenuPopupTarget(listPane, listMenu, listTarget, 460.0, 160.0);

            M3MenuItem carouselTarget = new M3MenuItem("Carousel target");
            M3MenuButton carouselMenu = new M3MenuButton("More", new M3MenuItem("First"), carouselTarget);
            Pane carouselItem = new Pane(carouselMenu);
            carouselItem.setPrefSize(240.0, 128.0);
            carouselMenu.resizeRelocate(24.0, 24.0, 160.0, 56.0);
            Pane otherItem = new Pane(new M3Text("Other"));
            otherItem.setPrefSize(180.0, 128.0);
            M3Carousel carousel = new M3Carousel(carouselItem, otherItem);
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
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "Carousel item",
                    "Shows details for the selected carousel item.",
                    tooltipAction
            );
            Pane secondItem = new Pane(ownerAction);
            secondItem.setPrefSize(240.0, 128.0);
            M3Carousel carousel = new M3Carousel(firstItem, secondItem);
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
            M3Carousel carousel = new M3Carousel(firstItem, secondItem);
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
            M3Carousel carousel = new M3Carousel(firstItem, secondItem);
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
            M3Carousel carousel = new M3Carousel(firstItem, secondItem);
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
            M3Carousel carousel = new M3Carousel(firstItem, secondItem);
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
            M3Carousel carousel = new M3Carousel(firstItem, secondItem);
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

    /// Verifies that dialog content containers expose active split-button menu popup focus.
    @Test
    void dialogPaneRoutesFocusThroughNestedSplitButtonPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3SplitButton splitButton = new M3SplitButton("Create", draftItem, publishItem);
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
                assertTrue(splitButton.getMenuButton().isFocused());
                assertSame(splitButton.getMenuButton(), dialogPane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
            M3SplitButton splitButton = new M3SplitButton("Create", draftItem, publishItem);
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

    /// Verifies that dialog content containers reveal rich tooltip actions inside nested split-button menu items.
    @Test
    void dialogPaneRevealsNestedSplitButtonMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    publishItem,
                    "Publish",
                    "Publishes the current draft.",
                    tooltipAction
            );
            M3SplitButton splitButton = new M3SplitButton("Create", draftItem, publishItem);
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
                assertSame(tooltipAction, splitButton.getMenuButton().queryAccessibleAttribute(
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

    /// Verifies that split buttons expose rich tooltip action focus from items inside their open menu popup.
    @Test
    void splitButtonRoutesFocusThroughMenuRichTooltipAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem draftItem = new M3MenuItem("Draft");
            M3MenuItem publishItem = new M3MenuItem("Publish");
            M3Button tooltipAction = new M3Button("Details");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    publishItem,
                    "Publish",
                    "Publishes the current draft.",
                    tooltipAction
            );
            M3SplitButton splitButton = new M3SplitButton("Create", draftItem, publishItem);
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
                assertSame(publishItem, splitButton.getMenuButton().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                publishItem.fireEvent(keyPressed(KeyCode.F6));

                assertTrue(tooltipAction.isFocused());
                assertTrue(tooltip.isShowing());
                assertTrue(splitButton.isShowing());
                assertSame(tooltipAction, splitButton.getMenuButton().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertPopupFocusRoutedByContainer(splitButton, tooltipAction);

                tooltipAction.fireEvent(keyPressed(KeyCode.ESCAPE));

                assertFalse(tooltip.isShowing());
                assertTrue(publishItem.isFocused());
                assertTrue(splitButton.isShowing());
                assertSame(publishItem, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(publishItem, splitButton.getMenuButton().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
            M3RichTooltip tooltip = M3RichTooltip.install(
                    publishItem,
                    "Publish",
                    "Publishes the current draft.",
                    tooltipAction
            );
            M3SplitButton splitButton = new M3SplitButton("Create", draftItem, publishItem);
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
                assertSame(tooltipAction, splitButton.getMenuButton().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
            M3RichTooltip tooltip = M3RichTooltip.install(
                    pdfItem,
                    "PDF export",
                    "Exports the current draft as a PDF.",
                    tooltipAction
            );
            M3SubMenuItem recentItem = new M3SubMenuItem("Recent", pdfItem, htmlItem);
            M3SubMenuItem exportItem = new M3SubMenuItem("Export", recentItem);
            M3SplitButton splitButton = new M3SplitButton("Create", exportItem);
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
                assertSame(tooltipAction, splitButton.getMenuButton().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
                assertSame(pdfItem, splitButton.getMenuButton().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
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
            M3SplitButton splitButton = new M3SplitButton("Create", scheduleItem);
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
                        splitButton.getMenuButton(),
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
            M3SplitButton splitButton = new M3SplitButton("Create", scheduleItem);
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
                        splitButton.getMenuButton(),
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

    /// Verifies that surface content containers reveal menu popup targets owned by FAB-menu actions.
    @Test
    void surfaceRevealsNestedFabMenuMenuPopupTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem saveItem = new M3MenuItem("Save");
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", saveItem, archiveItem);
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.addItem(menuButton);
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
            fabMenu.addItem(menuButton);
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
            fabMenu.addItem(field);
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
            fabMenu.addItem(field);
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
            fabMenu.addItem(field);
            Pane content = new Pane(fabMenu);
            content.setPrefSize(420.0, 180.0);
            M3Surface surface = new M3Surface(content);
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

    /// Verifies that surface content subtrees can reveal queued snackbar actions through their host.
    @Test
    void surfaceRevealsQueuedSnackbarThroughNestedSnackbarHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3SnackbarHost host = new M3SnackbarHost();
            M3Snackbar currentSnackbar = new M3Snackbar("Saved", "Undo");
            M3Snackbar queuedSnackbar = new M3Snackbar("Deleted", "Restore");
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

    /// Verifies that shown side sheets expose active nested content menu popup focus.
    @Test
    void sideSheetRoutesFocusThroughNestedContentMenuPopup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3MenuItem archiveItem = new M3MenuItem("Archive");
            M3MenuButton menuButton = new M3MenuButton("Actions", archiveItem);
            Pane content = new Pane(menuButton);
            content.setPrefSize(260.0, 88.0);
            M3SideSheet sheet = new M3SideSheet("Details", content);
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

    /// Verifies that shown bottom sheets expose active nested content rich tooltip focus.
    @Test
    void bottomSheetRoutesFocusThroughNestedContentRichTooltipAction() {
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
            M3BottomSheet sheet = new M3BottomSheet("Queue", content);
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
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows additional sheet details.",
                    tooltipAction
            );
            Pane content = new Pane(ownerAction);
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
            M3RichTooltip tooltip = M3RichTooltip.install(
                    ownerAction,
                    "More",
                    "Shows additional sheet details.",
                    tooltipAction
            );
            Pane content = new Pane(ownerAction);
            content.setPrefSize(260.0, 88.0);
            M3SideSheet sheet = new M3SideSheet("Details", content);
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
            M3BottomSheet sheet = new M3BottomSheet("Schedule", content);
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
            M3BottomSheet sheet = new M3BottomSheet("Schedule", content);
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
            M3SideSheet sheet = new M3SideSheet("Schedule", content);
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
            M3SideSheet sheet = new M3SideSheet("Schedule", content);
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
            M3BottomSheet sheet = new M3BottomSheet("Schedule", content);
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
            M3BottomSheet sheet = new M3BottomSheet("Schedule", content);
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
            M3SideSheet sheet = new M3SideSheet("Schedule", content);
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
            M3SideSheet sheet = new M3SideSheet("Schedule", content);
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
            M3BottomSheet sheet = new M3BottomSheet("Schedule", content);
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
            M3BottomSheet sheet = new M3BottomSheet("Schedule", content);
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
            M3SideSheet sheet = new M3SideSheet("Schedule", content);
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
            M3SideSheet sheet = new M3SideSheet("Schedule", content);
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


    /// Verifies that search views reveal date picker targets owned by the embedded search bar.
    @Test
    void searchViewRevealsNestedSearchBarDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 25);
            M3SearchView searchView = new M3SearchView("Search schedule");
            searchView.setTrailingActions(field);
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
            M3SearchView searchView = new M3SearchView("Search schedule");
            searchView.setTrailingActions(field);
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


    /// Verifies that search views reveal date picker targets from result content.
    @Test
    void searchViewRevealsNestedResultDatePickerValueTarget() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 14));
            LocalDate targetDate = LocalDate.of(2026, 6, 26);
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

    /// Verifies that expanded navigation drawer groups preserve active child rich tooltip focus for default actions.
    @Test
    void navigationDrawerGroupPreservesActiveChildRichTooltipFocusForDefaultActions() {
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
            group.setExpanded(true);
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


    /// Verifies that top app bar action slots reveal time picker value targets and expose popup focus.
    @Test
    void topAppBarRevealsNestedTimePickerValueTargetFromActionSlot() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(10, 45);
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


    /// Verifies that search bar trailing action slots reveal time picker value targets and expose popup focus.
    @Test
    void searchBarRevealsNestedTimePickerValueTargetFromTrailingAction() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePickerField field = new M3TimePickerField(LocalTime.of(9, 30));
            LocalTime targetTime = LocalTime.of(12, 30);
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
