// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3DateRangePickerFieldSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3DateRangePickerField] text editing, popup synchronization, and skin installation.
@NotNullByDefault
final class M3DateRangePickerFieldTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that the range field commits both editor values and mirrors them into the popup picker.
    @Test
    void dateRangePickerFieldCommitsEditorTextAndSyncsPicker() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 22)
            );

            assertEquals("2026-05-18", field.getStartEditor().getText());
            assertEquals("2026-05-22", field.getEndEditor().getText());
            assertEquals(LocalDate.of(2026, 5, 18), field.getPicker().getStartDate());
            assertEquals(LocalDate.of(2026, 5, 22), field.getPicker().getEndDate());

            field.getStartEditor().setText("2026-05-19");
            field.getEndEditor().setText("2026-05-25");
            assertTrue(field.commitEditorText());
            assertEquals(LocalDate.of(2026, 5, 19), field.getStartDate());
            assertEquals(LocalDate.of(2026, 5, 25), field.getEndDate());
            assertEquals(LocalDate.of(2026, 5, 19), field.getPicker().getStartDate());
            assertEquals(LocalDate.of(2026, 5, 25), field.getPicker().getEndDate());
        });
    }

    /// Verifies partial ranges, invalid text, reversed ranges, and bounds errors.
    @Test
    void dateRangePickerFieldValidatesEditorText() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField();

            field.getStartEditor().setText("2026-05-19");
            field.getEndEditor().setText("");
            assertTrue(field.commitEditorText());
            assertEquals(LocalDate.of(2026, 5, 19), field.getStartDate());
            assertNull(field.getEndDate());

            field.getStartEditor().setText("");
            field.getEndEditor().setText("2026-05-20");
            assertFalse(field.commitEditorText());
            assertEquals(field.getInvalidTextErrorText(), field.getStartInputLayout().getErrorText());

            field.getStartEditor().setText("2026-05-25");
            field.getEndEditor().setText("2026-05-20");
            assertFalse(field.commitEditorText());
            assertEquals(field.getRangeErrorText(), field.getStartInputLayout().getErrorText());
            assertEquals(field.getRangeErrorText(), field.getEndInputLayout().getErrorText());

            field.getPicker().setMinDate(LocalDate.of(2026, 5, 10));
            field.getPicker().setMaxDate(LocalDate.of(2026, 5, 24));
            field.getStartEditor().setText("2026-05-20");
            field.getEndEditor().setText("2026-05-25");
            assertFalse(field.commitEditorText());
            assertEquals(field.getRangeErrorText(), field.getStartInputLayout().getErrorText());
            assertEquals(field.getRangeErrorText(), field.getEndInputLayout().getErrorText());
        });
    }

    /// Verifies that popup picker selections are coalesced and mirrored back into the field editors.
    @Test
    void dateRangePickerFieldSyncsPopupPickerSelection() {
        AtomicReference<M3DateRangePickerField> fieldReference = new AtomicReference<>();

        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField();
            fieldReference.set(field);
            field.getPicker().selectDate(LocalDate.of(2026, 5, 18));
        });
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = fieldReference.get();
            assertEquals(LocalDate.of(2026, 5, 18), field.getStartDate());
            assertNull(field.getEndDate());
            assertEquals("2026-05-18", field.getStartEditor().getText());
            field.getPicker().selectDate(LocalDate.of(2026, 5, 22));
        });
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = fieldReference.get();
            assertEquals(LocalDate.of(2026, 5, 18), field.getStartDate());
            assertEquals(LocalDate.of(2026, 5, 22), field.getEndDate());
            assertEquals("2026-05-22", field.getEndEditor().getText());
        });
    }

    /// Verifies that popup presets render next to the picker and update the field range immediately.
    @Tier2Test
    @Test
    void dateRangePickerFieldPresetsRenderAndApplyRange() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerField field = new M3DateRangePickerField();

            field.getPresets().setAll(M3DateRangePresets.common(anchor, field.getPicker().getFirstDayOfWeek()));

            assertEquals(6, field.getPresets().size());
            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(presetContent.getStyleClass().contains(M3DateRangePickerField.PRESET_CONTENT_STYLE_CLASS));
            assertEquals(
                    6,
                    presetContent.lookupAll("." + M3DateRangePickerField.PRESET_BUTTON_STYLE_CLASS).size()
            );

            M3DateRangePreset preset = M3DateRangePresets.nextDays(anchor, 7);
            findPresetButton(presetContent, "Next 7 days").fire();

            assertEquals(preset.range(), field.getRange());
            assertEquals(YearMonth.from(anchor), field.getPicker().getDisplayedMonth());
            assertEquals("2026-05-19", field.getStartEditor().getText());
            assertEquals("2026-05-25", field.getEndEditor().getText());

            field.getPresets().clear();

            assertTrue(field.getPresets().isEmpty());
            assertFalse(assertInstanceOf(Node.class, field.getPicker().getParent()).getStyleClass()
                    .contains(M3DateRangePickerField.PRESET_CONTENT_STYLE_CLASS));
        });
    }

    /// Verifies that the date range preset list rejects null mutations without partial insertion.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void dateRangePickerFieldPresetListRejectsNullElements() {
        LocalDate anchor = LocalDate.of(2026, 5, 19);
        M3DateRangePickerField field = new M3DateRangePickerField();
        M3DateRangePreset preset = M3DateRangePresets.today(anchor);

        assertThrows(NullPointerException.class, () -> field.getPresets().add(null));
        assertThrows(NullPointerException.class, () -> field.getPresets().addAll(preset, null));
        assertTrue(field.getPresets().isEmpty());
    }

    /// Verifies that presets outside the current field bounds are rendered disabled.
    @Test
    void dateRangePickerFieldDisablesOutOfBoundsPresetButtons() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerField field = new M3DateRangePickerField();
            field.getPicker().setMinDate(anchor.plusDays(1));
            field.getPicker().setMaxDate(anchor.plusDays(30));

            field.getPresets().setAll(
                    M3DateRangePresets.today(anchor),
                    M3DateRangePresets.nextDays(anchor.plusDays(1), 7)
            );

            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(findPresetButton(presetContent, "Today").isDisabled());
            assertFalse(findPresetButton(presetContent, "Next 7 days").isDisabled());
        });
    }

    /// Verifies that date range field bounds clear an excluded range and refresh preset buttons.
    @Test
    void dateRangePickerFieldBoundsClearExcludedRangeAndRefreshPresets() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerField field = new M3DateRangePickerField(anchor, anchor.plusDays(1));
            field.getPresets().setAll(M3DateRangePresets.today(anchor), M3DateRangePresets.tomorrow(anchor));

            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertFalse(findPresetButton(presetContent, "Today").isDisabled());
            assertFalse(findPresetButton(presetContent, "Tomorrow").isDisabled());

            field.getPicker().setMinDate(anchor.plusDays(1));

            assertNull(field.getStartDate());
            assertNull(field.getEndDate());
            assertNull(field.getPicker().getStartDate());
            assertNull(field.getPicker().getEndDate());
            assertEquals("", field.getStartEditor().getText());
            assertEquals("", field.getEndEditor().getText());
            assertTrue(findPresetButton(presetContent, "Today").isDisabled());
            assertFalse(findPresetButton(presetContent, "Tomorrow").isDisabled());
        });
    }

    /// Verifies that the range field forwards accessibility value actions to its popup picker.
    @Test
    void dateRangePickerFieldForwardsAccessibleValueActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField();
            field.getPicker().setDisplayedMonth(YearMonth.of(2026, 1));

            field.executeAccessibleAction(
                    AccessibleAction.SET_SELECTED_ITEMS,
                    List.of(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 10))
            );
            assertEquals(LocalDate.of(2026, 1, 10), field.getStartDate());
            assertEquals(LocalDate.of(2026, 1, 12), field.getEndDate());
            assertEquals("2026-01-10", field.getStartEditor().getText());
            assertEquals("2026-01-12", field.getEndEditor().getText());
            assertEquals(List.of(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 12)),
                    field.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

            field.executeAccessibleAction(
                    AccessibleAction.SET_SELECTED_ITEMS,
                    List.of(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 10))
            );
            assertEquals("2026-01-10", field.getStartEditor().getText());
            assertEquals("2026-01-12", field.getEndEditor().getText());

            field.clearRange();
            field.getPicker().setDisplayedMonth(YearMonth.of(2026, 1));
            field.executeAccessibleAction(AccessibleAction.INCREMENT);
            assertEquals(LocalDate.of(2026, 1, 2), field.getStartDate());
            assertNull(field.getEndDate());
            assertEquals("2026-01-02", field.getStartEditor().getText());
        });
    }

    /// Verifies that the range field installs its skin and lays out both input layouts.
    @Test
    void dateRangePickerFieldSkinInstallsInputLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 22)
            );
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 760.0, 160.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
            root.layout();

            assertInstanceOf(M3DateRangePickerFieldSkin.class, field.getSkin());
            assertTrue(field.getStartInputLayout().getStyleClass().contains(M3TextInputLayout.STYLE_CLASS));
            assertTrue(field.getEndInputLayout().getStyleClass().contains(M3TextInputLayout.STYLE_CLASS));
        });
    }

    /// Verifies that the range field can show its popup when attached to a visible window.
    @Tier2Test
    @Test
    void dateRangePickerFieldCanShowPopupFromVisibleWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 22)
            );
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 760.0, 220.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.layout();

                field.showPicker();

                assertTrue(field.isShowing());
                assertEquals(true, field.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that keyboard dismissal from range popup picker content returns focus to the start editor.
    @Tier2Test
    @Test
    void dateRangePickerFieldRestoresStartEditorFocusAfterKeyboardDismissal() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 22)
            );
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 760.0, 220.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.layout();

                field.getStartEditor().requestFocus();
                assertTrue(field.getStartEditor().isFocused());

                field.showPicker();
                field.getPicker().requestFocus();
                assertTrue(field.isShowing());

                field.getPicker().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getStartEditor().isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that range field popup presets preserve accessibility focus while the popup is open.
    @Tier2Test
    @Test
    void dateRangePickerFieldPresetFocusIsPreservedByAccessibleActions() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    anchor,
                    anchor.plusDays(6)
            );
            field.getPresets().setAll(M3DateRangePresets.common(anchor, field.getPicker().getFirstDayOfWeek()));
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 760.0, 220.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.layout();

                Node endOpenButton = assertInstanceOf(Node.class, field.getEndInputLayout().getTrailing());
                endOpenButton.requestFocus();

                assertTrue(endOpenButton.isFocused());
                assertSame(endOpenButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(endOpenButton.isFocused());
                assertSame(endOpenButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

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
                assertTrue(field.getEndEditor().isFocused());
                assertSame(field.getEndEditor(), field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that range field preset columns mirror picker handoff keys in right-to-left layout.
    @Tier2Test
    @Test
    void dateRangePickerFieldPresetKeyboardNavigationMirrorsPickerHandoff() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerField field = new M3DateRangePickerField(
                    anchor,
                    anchor.plusDays(6)
            );
            field.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            field.getPresets().setAll(M3DateRangePresets.common(anchor, field.getPicker().getFirstDayOfWeek()));
            Pane root = new Pane(field);
            Scene scene = new Scene(root, 760.0, 220.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.layout();

                field.showPicker();
                Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                M3Button today = findPresetButton(presetContent, "Today");
                M3Button tomorrow = findPresetButton(presetContent, "Tomorrow");

                today.requestFocus();
                today.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(tomorrow.isFocused());

                tomorrow.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                Node pickerFocusNode = assertInstanceOf(
                        Node.class,
                        field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );
                assertTrue(pickerFocusNode.isFocused());
                assertTrue(M3Accessible.containsNode(field.getPicker(), pickerFocusNode));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that the range field popup inherits a locally installed parent theme.
    @Tier2Test
    @Test
    void dateRangePickerFieldPopupInheritsLocalParentThemeContext() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 22)
            );
            Pane localRoot = new Pane(field);
            Pane root = new Pane(localRoot);
            Scene scene = new Scene(root, 760.0, 220.0);
            Stage stage = new Stage();
            M3Theme localTheme = M3Theme.defaultTheme();

            try {
                M3ThemeManager.install(localRoot, localTheme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                localRoot.resizeRelocate(0.0, 0.0, 760.0, 220.0);
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.layout();

                field.showPicker();

                Parent popupRoot = assertInstanceOf(Parent.class, field.getPicker().getParent());
                assertSame(localTheme, M3ThemeManager.getTheme(popupRoot));
            } finally {
                stage.close();
            }
        });
    }

    /// Returns a preset button with the supplied text.
    private static M3Button findPresetButton(Node root, String text) {
        for (Node node : root.lookupAll("." + M3DateRangePickerField.PRESET_BUTTON_STYLE_CLASS)) {
            if (node instanceof M3Button button && button.getText().equals(text)) {
                return button;
            }
        }
        throw new AssertionError("Missing preset button: " + text);
    }

    /// Creates a key event for range picker field keyboard tests.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }
}
