// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.skins.M3PickerFieldSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3DatePickerField] and [M3TimePickerField] value editing, validation, and skin installation.
@NotNullByDefault
final class M3PickerFieldTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
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
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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

    /// Verifies that picker fields install the shared picker field skin and render their input layouts.
    @Test
    void pickerFieldSkinInstallsInputLayouts() {
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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

    /// Runs a task on the FX application thread and propagates failures.
    private static void runOnFxThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }

        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                failure.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        @Nullable Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
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
}
