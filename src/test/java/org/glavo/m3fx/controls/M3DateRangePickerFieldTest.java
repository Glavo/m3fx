// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.skins.M3DateRangePickerFieldSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3DateRangePickerField] text editing, popup synchronization, and skin installation.
@NotNullByDefault
final class M3DateRangePickerFieldTest {
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

    /// Verifies that the range field commits both editor values and mirrors them into the popup picker.
    @Test
    void dateRangePickerFieldCommitsEditorTextAndSyncsPicker() {
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
    }

    /// Verifies partial ranges, invalid text, reversed ranges, and bounds errors.
    @Test
    void dateRangePickerFieldValidatesEditorText() {
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

        field.setMinDate(LocalDate.of(2026, 5, 10));
        field.setMaxDate(LocalDate.of(2026, 5, 24));
        field.getStartEditor().setText("2026-05-20");
        field.getEndEditor().setText("2026-05-25");
        assertFalse(field.commitEditorText());
        assertEquals(field.getRangeErrorText(), field.getStartInputLayout().getErrorText());
        assertEquals(field.getRangeErrorText(), field.getEndInputLayout().getErrorText());
    }

    /// Verifies that popup picker selections are coalesced and mirrored back into the field editors.
    @Test
    void dateRangePickerFieldSyncsPopupPickerSelection() {
        M3DateRangePickerField field = new M3DateRangePickerField();

        runOnFxThread(() -> field.getPicker().selectDate(LocalDate.of(2026, 5, 18)));
        runOnFxThread(() -> {
            assertEquals(LocalDate.of(2026, 5, 18), field.getStartDate());
            assertNull(field.getEndDate());
            assertEquals("2026-05-18", field.getStartEditor().getText());
            field.getPicker().selectDate(LocalDate.of(2026, 5, 22));
        });
        runOnFxThread(() -> {
            assertEquals(LocalDate.of(2026, 5, 18), field.getStartDate());
            assertEquals(LocalDate.of(2026, 5, 22), field.getEndDate());
            assertEquals("2026-05-22", field.getEndEditor().getText());
        });
    }

    /// Verifies that popup presets render next to the picker and update the field range immediately.
    @Test
    void dateRangePickerFieldPresetsRenderAndApplyRange() {
        runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerField field = new M3DateRangePickerField();

            field.setCommonPresets(anchor);

            assertEquals(6, field.getPresets().size());
            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(presetContent.getStyleClass().contains(M3DateRangePickerField.PRESET_CONTENT_STYLE_CLASS));
            assertEquals(
                    6,
                    presetContent.lookupAll("." + M3DateRangePickerField.PRESET_BUTTON_STYLE_CLASS).size()
            );

            M3DateRangePreset preset = M3DateRangePresets.nextDays(anchor, 7);
            field.applyPreset(preset);

            assertEquals(preset.range(), field.getRange());
            assertEquals(YearMonth.from(anchor), field.getDisplayedMonth());
            assertEquals("2026-05-19", field.getStartEditor().getText());
            assertEquals("2026-05-25", field.getEndEditor().getText());

            field.clearPresets();

            assertTrue(field.getPresets().isEmpty());
            assertFalse(assertInstanceOf(Node.class, field.getPicker().getParent()).getStyleClass()
                    .contains(M3DateRangePickerField.PRESET_CONTENT_STYLE_CLASS));
        });
    }

    /// Verifies that presets outside the current field bounds are rendered disabled.
    @Test
    void dateRangePickerFieldDisablesOutOfBoundsPresetButtons() {
        runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerField field = new M3DateRangePickerField();
            field.setMinDate(anchor.plusDays(1));
            field.setMaxDate(anchor.plusDays(30));

            field.setPresets(
                    M3DateRangePresets.today(anchor),
                    M3DateRangePresets.nextDays(anchor.plusDays(1), 7)
            );

            Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
            assertTrue(findPresetButton(presetContent, "Today").isDisabled());
            assertFalse(findPresetButton(presetContent, "Next 7 days").isDisabled());
        });
    }

    /// Verifies that the range field installs its skin and lays out both input layouts.
    @Test
    void dateRangePickerFieldSkinInstallsInputLayouts() {
        runOnFxThread(() -> {
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
    @Test
    void dateRangePickerFieldCanShowPopupFromVisibleWindow() {
        runOnFxThread(() -> {
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

    /// Returns a preset button with the supplied text.
    private static M3Button findPresetButton(Node root, String text) {
        for (Node node : root.lookupAll("." + M3DateRangePickerField.PRESET_BUTTON_STYLE_CLASS)) {
            if (node instanceof M3Button button && button.getText().equals(text)) {
                return button;
            }
        }
        throw new AssertionError("Missing preset button: " + text);
    }
}
