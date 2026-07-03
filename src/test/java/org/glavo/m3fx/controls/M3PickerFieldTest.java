// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3PickerFieldSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3DatePickerField] and [M3TimePickerField] value editing, validation, and skin installation.
@NotNullByDefault
final class M3PickerFieldTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that a date picker field commits editor text and mirrors popup date selections.
    @Test
    void datePickerFieldCommitsEditorTextAndSyncsPicker() {
        M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 5, 19));

        assertEquals("2026-05-19", field.getEditor().getText());
        assertEquals(LocalDate.of(2026, 5, 19), field.getPicker().getValue());

        field.getEditor().setText("2026-05-21");
        assertTrue(field.commitEditorText());
        assertEquals(LocalDate.of(2026, 5, 21), field.getValue());
        assertEquals(LocalDate.of(2026, 5, 21), field.getPicker().getValue());
        assertEquals("2026-05-21", field.getEditor().getText());

        field.setMinDate(LocalDate.of(2026, 5, 18));
        field.setMaxDate(LocalDate.of(2026, 5, 24));
        field.getEditor().setText("2026-05-25");
        assertFalse(field.commitEditorText());
        assertEquals(LocalDate.of(2026, 5, 21), field.getValue());
        assertEquals(field.getRangeErrorText(), field.getErrorText());

        field.getPicker().setValue(LocalDate.of(2026, 5, 22));
        assertEquals(LocalDate.of(2026, 5, 22), field.getValue());
        assertEquals("2026-05-22", field.getEditor().getText());
    }

    /// Verifies that date picker field presets render next to the picker and update the field value.
    @Test
    void datePickerFieldPresetsRenderAndApplyDate() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DatePickerField field = new M3DatePickerField();

            field.setCommonPresets(anchor);

            assertEquals(5, field.getPresets().size());
            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(presetContent.getStyleClass().contains(M3DatePickerField.PRESET_CONTENT_STYLE_CLASS));
            assertEquals(5, presetContent.lookupAll("." + M3DatePickerField.PRESET_BUTTON_STYLE_CLASS).size());

            M3DatePreset preset = M3DatePresets.daysFrom(anchor, 7);
            field.applyPreset(preset);

            assertEquals(anchor.plusDays(7), field.getValue());
            assertEquals(YearMonth.from(anchor), field.getDisplayedMonth());
            assertEquals("2026-05-26", field.getEditor().getText());

            field.clearPresets();

            assertTrue(field.getPresets().isEmpty());
            assertFalse(assertInstanceOf(Node.class, field.getPicker().getParent()).getStyleClass()
                    .contains(M3DatePickerField.PRESET_CONTENT_STYLE_CLASS));
        });
    }

    /// Verifies that date picker field presets outside the current bounds are rendered disabled.
    @Test
    void datePickerFieldDisablesOutOfBoundsPresetButtons() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DatePickerField field = new M3DatePickerField();
            field.setMinDate(anchor.plusDays(1));
            field.setMaxDate(anchor.plusDays(30));

            field.setPresets(M3DatePresets.today(anchor), M3DatePresets.tomorrow(anchor));

            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(findPresetButton(presetContent, "Today").isDisabled());
            assertFalse(findPresetButton(presetContent, "Tomorrow").isDisabled());
        });
    }

    /// Verifies that a time picker field normalizes seconds, parses text, and reports invalid input.
    @Test
    void timePickerFieldNormalizesAndValidatesEditorText() {
        M3TimePickerField field = new M3TimePickerField(LocalTime.of(10, 30, 12));

        assertEquals(LocalTime.of(10, 30), field.getValue());
        assertEquals("10:30", field.getEditor().getText());

        field.getEditor().setText("11:45");
        assertTrue(field.commitEditorText());
        assertEquals(LocalTime.of(11, 45), field.getValue());
        assertEquals("11:45", field.getEditor().getText());

        field.setMinTime(LocalTime.of(9, 0));
        field.setMaxTime(LocalTime.of(17, 30));
        field.getEditor().setText("18:00");
        assertFalse(field.commitEditorText());
        assertEquals(LocalTime.of(11, 45), field.getValue());
        assertEquals(field.getRangeErrorText(), field.getErrorText());

        field.getEditor().setText("not a time");
        assertFalse(field.commitEditorText());
        assertEquals(field.getInvalidTextErrorText(), field.getErrorText());
    }

    /// Verifies that time picker field presets render next to the picker and update the field value.
    @Test
    void timePickerFieldPresetsRenderAndApplyTime() {
        FxTestUtils.runOnFxThread(() -> {
            LocalTime anchor = LocalTime.of(10, 30);
            M3TimePickerField field = new M3TimePickerField();

            field.setCommonPresets(anchor);

            assertEquals(5, field.getPresets().size());
            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(presetContent.getStyleClass().contains(M3TimePickerField.PRESET_CONTENT_STYLE_CLASS));
            assertEquals(5, presetContent.lookupAll("." + M3TimePickerField.PRESET_BUTTON_STYLE_CLASS).size());

            M3TimePreset preset = M3TimePresets.minutesFrom(anchor, 15);
            field.applyPreset(preset);

            assertEquals(LocalTime.of(10, 45), field.getValue());
            assertEquals("10:45", field.getEditor().getText());

            field.clearPresets();

            assertTrue(field.getPresets().isEmpty());
            assertFalse(assertInstanceOf(Node.class, field.getPicker().getParent()).getStyleClass()
                    .contains(M3TimePickerField.PRESET_CONTENT_STYLE_CLASS));
        });
    }

    /// Verifies that time picker field presets outside the current bounds are rendered disabled.
    @Test
    void timePickerFieldDisablesOutOfBoundsPresetButtons() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePickerField field = new M3TimePickerField();
            field.setMinTime(LocalTime.of(9, 0));
            field.setMaxTime(LocalTime.of(17, 30));

            field.setPresets(M3TimePresets.midnight(), M3TimePresets.morning());

            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(findTimePresetButton(presetContent, "Midnight").isDisabled());
            assertFalse(findTimePresetButton(presetContent, "Morning").isDisabled());
        });
    }

    /// Verifies that picker fields forward accessibility value actions to their popup pickers.
    @Test
    void pickerFieldsForwardAccessibleValueActions() {
        M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 19));
        dateField.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(LocalDate.of(2026, 5, 20), dateField.getValue());
        assertEquals("2026-05-20", dateField.getEditor().getText());
        assertEquals(List.of(LocalDate.of(2026, 5, 20)),
                dateField.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
        timeField.setMinuteStep(15);
        timeField.executeAccessibleAction(AccessibleAction.BLOCK_INCREMENT);
        assertEquals(LocalTime.of(11, 30), timeField.getValue());
        assertEquals("11:30", timeField.getEditor().getText());
        assertEquals(List.of(LocalTime.of(11, 30)),
                timeField.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
    }

    /// Verifies that picker fields reject out-of-range accessibility value targets before opening popups.
    @Test
    void pickerFieldsRejectOutOfRangeAccessibleValueTargets() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            dateField.setMinDate(LocalDate.of(2026, 5, 1));
            dateField.setMaxDate(LocalDate.of(2026, 5, 31));
            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
            timeField.setMinTime(LocalTime.of(9, 0));
            timeField.setMaxTime(LocalTime.of(17, 30));
            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 10),
                    LocalDate.of(2026, 5, 12)
            );
            rangeField.setMinDate(LocalDate.of(2026, 5, 1));
            rangeField.setMaxDate(LocalDate.of(2026, 5, 31));
            M3DialogPane pane = new M3DialogPane();
            pane.setContent(new VBox(16.0, dateField, timeField, rangeField));
            Pane root = new Pane(pane);
            Scene scene = new Scene(root, 780.0, 420.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                pane.resizeRelocate(24.0, 24.0, 700.0, 340.0);
                root.layout();

                pane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 6, 2));
                pane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalTime.of(18, 0));

                assertFalse(dateField.isShowing());
                assertFalse(timeField.isShowing());
                assertFalse(rangeField.isShowing());

                dateField.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 6, 2));
                timeField.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalTime.of(18, 0));
                rangeField.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 6, 2));

                assertFalse(dateField.isShowing());
                assertFalse(timeField.isShowing());
                assertFalse(rangeField.isShowing());

                dateField.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalDate.of(2026, 6, 2));
                timeField.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalTime.of(18, 0));
                rangeField.executeAccessibleAction(
                        AccessibleAction.SET_SELECTED_ITEMS,
                        LocalDate.of(2026, 6, 2),
                        LocalDate.of(2026, 6, 4)
                );

                assertEquals(LocalDate.of(2026, 5, 19), dateField.getValue());
                assertEquals(LocalTime.of(10, 30), timeField.getValue());
                assertEquals(LocalDate.of(2026, 5, 10), rangeField.getStartDate());
                assertEquals(LocalDate.of(2026, 5, 12), rangeField.getEndDate());

                dateField.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 5, 20));
                assertTrue(dateField.isShowing());
                dateField.hidePicker();

                timeField.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalTime.of(10, 45));
                assertTrue(timeField.isShowing());
                timeField.hidePicker();

                rangeField.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 5, 20));
                assertTrue(rangeField.isShowing());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that picker fields install the shared picker field skin and render their input layouts.
    @Test
    void pickerFieldSkinInstallsInputLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
            HBox root = new HBox(16.0, dateField, timeField);
            Scene scene = new Scene(root, 720.0, 160.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(720.0, 160.0);
            root.layout();

            assertInstanceOf(M3PickerFieldSkin.class, dateField.getSkin());
            assertInstanceOf(M3PickerFieldSkin.class, timeField.getSkin());
            assertTrue(dateField.getInputLayout().getStyleClass().contains(M3TextInputLayout.STYLE_CLASS));
            assertTrue(timeField.getInputLayout().getStyleClass().contains(M3TextInputLayout.STYLE_CLASS));
        });
    }

    /// Verifies that a picker field can show its popup when attached to a visible window.
    @Test
    void pickerFieldCanShowPopupFromVisibleWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 420.0, 180.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 320.0, 72.0);
                root.layout();

                field.showPicker();

                assertTrue(field.isShowing());
                assertEquals(true, field.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.EXPANDED));

                field.getPicker().setValue(LocalDate.of(2026, 5, 20));
                assertEquals(LocalDate.of(2026, 5, 20), field.getValue());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that keyboard dismissal from popup picker content returns focus to the editor.
    @Test
    void pickerFieldRestoresEditorFocusAfterKeyboardDismissal() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 420.0, 180.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 320.0, 72.0);
                root.layout();

                field.getEditor().requestFocus();
                assertTrue(field.getEditor().isFocused());

                field.showPicker();
                field.getPicker().requestFocus();
                assertTrue(field.isShowing());
                Node popupFocusNode =
                        assertInstanceOf(Node.class, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertTrue(popupFocusNode == field.getPicker()
                        || M3Accessible.containsNode(field.getPicker(), popupFocusNode));

                field.getPicker().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that preset popup actions expose focus and support keyboard dismissal.
    @Test
    void pickerFieldPresetFocusIsExposedAndDismissibleFromPopupContent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DatePickerField field = new M3DatePickerField(anchor);
            field.setCommonPresets(anchor);
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 640.0, 360.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 320.0, 72.0);
                root.layout();

                Node openButton = assertInstanceOf(Node.class, field.getInputLayout().getTrailing());
                openButton.requestFocus();

                assertTrue(openButton.isFocused());
                assertSame(openButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(openButton.isFocused());
                assertSame(openButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.showPicker();
                Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                M3Button today = findPresetButton(presetContent, "Today");

                today.requestFocus();

                assertTrue(today.isFocused());
                assertSame(today, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(today.isFocused());
                assertSame(today, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(today.isFocused());
                assertSame(today, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                today.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies modified date picker navigation keys are left to application shortcuts.
    @Test
    void datePickerModifiedNavigationKeysAreIgnored() {
        M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 19));

        picker.fireEvent(modifiedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT, false, true, false, false));
        picker.fireEvent(modifiedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN, true, false, false, false));

        assertEquals(LocalDate.of(2026, 5, 19), picker.getValue());
    }

    /// Verifies modified date range picker navigation keys are left to application shortcuts.
    @Test
    void dateRangePickerModifiedNavigationKeysAreIgnored() {
        M3DateRangePicker picker = new M3DateRangePicker(
                LocalDate.of(2026, 5, 19),
                LocalDate.of(2026, 5, 21)
        );

        picker.fireEvent(modifiedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT, false, true, false, false));
        picker.fireEvent(modifiedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN, true, false, false, false));

        assertEquals(LocalDate.of(2026, 5, 19), picker.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 21), picker.getEndDate());
    }

    /// Verifies that preset popup action columns support keyboard traversal and picker handoff.
    @Test
    void pickerFieldPresetKeyboardNavigationMovesWithinColumnAndToPicker() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate dateAnchor = LocalDate.of(2026, 5, 19);
            LocalTime timeAnchor = LocalTime.of(10, 30);
            M3DatePickerField dateField = new M3DatePickerField(dateAnchor);
            M3TimePickerField timeField = new M3TimePickerField(timeAnchor);
            dateField.setCommonPresets(dateAnchor);
            timeField.setCommonPresets(timeAnchor);
            Pane root = new Pane(dateField, timeField);
            Scene scene = new Scene(root, 720.0, 260.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dateField.resizeRelocate(24.0, 24.0, 320.0, 72.0);
                timeField.resizeRelocate(24.0, 120.0, 320.0, 72.0);
                root.layout();

                dateField.showPicker();
                Node datePresetContent = assertInstanceOf(Node.class, dateField.getPicker().getParent());
                M3Button today = findPresetButton(datePresetContent, "Today");
                M3Button tomorrow = findPresetButton(datePresetContent, "Tomorrow");
                M3Button nextMonth = findPresetButton(datePresetContent, "Next month");

                today.requestFocus();
                today.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(tomorrow.isFocused());

                tomorrow.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(nextMonth.isFocused());

                nextMonth.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                Node datePickerFocusNode = assertInstanceOf(
                        Node.class,
                        dateField.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );
                assertTrue(datePickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(dateField.getPicker(), datePickerFocusNode));

                dateField.hidePicker();
                timeField.showPicker();
                Node timePresetContent = assertInstanceOf(Node.class, timeField.getPicker().getParent());
                M3Button now = findTimePresetButton(timePresetContent, "Now");
                M3Button inFifteenMinutes = findTimePresetButton(timePresetContent, "In 15 min");
                M3Button evening = findTimePresetButton(timePresetContent, "Evening");

                now.requestFocus();
                now.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(inFifteenMinutes.isFocused());

                inFifteenMinutes.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(evening.isFocused());

                evening.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));

                assertTrue(now.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that dialog panes route focus to popup content exposed by a nested picker field.
    @Test
    void dialogPaneRoutesFocusToNestedPickerPopupContent() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DatePickerField field = new M3DatePickerField(anchor);
            field.setCommonPresets(anchor);
            M3DialogPane pane = new M3DialogPane();
            pane.setContent(field);
            pane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            Pane root = new Pane(pane);
            Scene scene = new Scene(root, 720.0, 480.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                pane.resizeRelocate(24.0, 24.0, 560.0, 360.0);
                root.layout();

                field.showPicker();
                Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                M3Button tomorrow = findPresetButton(presetContent, "Tomorrow");

                tomorrow.requestFocus();

                assertTrue(tomorrow.isFocused());
                assertSame(tomorrow, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(tomorrow, pane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                pane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(tomorrow.isFocused());

                tomorrow.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEditor().isFocused());
                assertSame(field.getEditor(), pane.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that picker popup content inherits a locally installed parent theme.
    @Test
    void pickerFieldPopupInheritsLocalParentThemeContext() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            Pane localRoot = new Pane(field);
            Pane root = new Pane(localRoot);
            Scene scene = new Scene(root, 420.0, 180.0);
            Stage stage = new Stage();
            M3Theme localTheme = M3Theme.defaultTheme();

            try {
                M3ThemeManager.install(localRoot, localTheme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                localRoot.resizeRelocate(0.0, 0.0, 420.0, 180.0);
                field.resizeRelocate(24.0, 24.0, 320.0, 72.0);
                root.layout();

                field.showPicker();

                Parent popupRoot = assertInstanceOf(Parent.class, field.getPicker().getParent());
                assertSame(localTheme, M3ThemeManager.getTheme(popupRoot));
            } finally {
                stage.close();
            }
        });
    }

    /// Returns a date preset button with the supplied text.
    private static M3Button findPresetButton(Node root, String text) {
        for (Node node : root.lookupAll("." + M3DatePickerField.PRESET_BUTTON_STYLE_CLASS)) {
            if (node instanceof M3Button button && button.getText().equals(text)) {
                return button;
            }
        }
        throw new AssertionError("Missing preset button: " + text);
    }

    /// Returns a time preset button with the supplied text.
    private static M3Button findTimePresetButton(Node root, String text) {
        for (Node node : root.lookupAll("." + M3TimePickerField.PRESET_BUTTON_STYLE_CLASS)) {
            if (node instanceof M3Button button && button.getText().equals(text)) {
                return button;
            }
        }
        throw new AssertionError("Missing preset button: " + text);
    }

    /// Creates a modified key event for picker field keyboard tests.
    private static KeyEvent modifiedKeyEvent(
            EventType<KeyEvent> eventType,
            KeyCode code,
            boolean shiftDown,
            boolean controlDown,
            boolean altDown,
            boolean metaDown
    ) {
        return new KeyEvent(eventType, "", "", code, shiftDown, controlDown, altDown, metaDown);
    }

    /// Creates a key event for picker field keyboard tests.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }
}
