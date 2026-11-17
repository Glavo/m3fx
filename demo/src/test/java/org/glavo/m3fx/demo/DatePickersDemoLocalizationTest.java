// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.localization.M3Localizer;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies runtime localization in the Date Pickers demo's retained calendar and editor.
@NotNullByDefault
final class DatePickersDemoLocalizationTest {
    /// The deterministic selected date used by the sample tests.
    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    /// Starts JavaFX before creating demo controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Keeps selection, bindings, and explicit formatting while updating the existing calendar header.
    @Test
    void switchesExistingCalendarAndPreservesApplicationOverrides() {
        FxTestUtils.runOnFxThread(() -> {
            VBox sample = createSample();
            M3DatePicker calendar = assertInstanceOf(M3DatePicker.class, sample.lookup(".m3-date-picker"));
            M3DatePickerField field = assertInstanceOf(M3DatePickerField.class,
                    sample.lookup(".m3-date-picker-field"));
            M3SegmentedButtonGroup language = assertInstanceOf(M3SegmentedButtonGroup.class,
                    sample.lookup(".m3-segmented-button-group"));
            M3MenuButton month = assertInstanceOf(M3MenuButton.class,
                    calendar.lookup(".m3-date-picker-month-menu-button"));
            assertEquals(Locale.US, M3Localizer.getEffectiveLocalizer(calendar).getLocale());
            assertEquals(DayOfWeek.SUNDAY, calendar.getFirstDayOfWeek());

            language.selectIndex(1);
            assertEquals(Locale.GERMANY, M3Localizer.getEffectiveLocalizer(calendar).getLocale());
            assertSame(M3Localizer.getEffectiveLocalizer(calendar), M3Localizer.getEffectiveLocalizer(field));
            assertEquals(Month.OCTOBER.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.GERMANY), month.getText());
            assertEquals(DayOfWeek.MONDAY, calendar.getFirstDayOfWeek());
            assertEquals("Event date", field.getLabelText());
            assertTrue(field.labelTextProperty().isBound());
            assertSame(DateTimeFormatter.ISO_LOCAL_DATE, field.getFormatter());
            assertEquals(DATE.toString(), field.getText());
            assertEquals(DATE, calendar.getValue());

            calendar.setValue(DATE.plusDays(1));
            assertEquals(DATE.plusDays(1), field.getValue());
            language.selectIndex(0);
            assertSame(month, calendar.lookup(".m3-date-picker-month-menu-button"));
            assertEquals(Month.OCTOBER.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.US), month.getText());
            assertEquals(DATE.plusDays(1), field.getValue());
            assertTrue(field.labelTextProperty().isBound());
        });
    }

    /// Refreshes generated validation without discarding pending input or changing the committed value.
    @Test
    void switchesValidationMessagesWithoutCommittingPendingInput() {
        FxTestUtils.runOnFxThread(() -> {
            VBox sample = createSample();
            M3DatePickerField field = assertInstanceOf(M3DatePickerField.class,
                    sample.lookup(".m3-date-picker-field"));
            M3SegmentedButtonGroup language = assertInstanceOf(M3SegmentedButtonGroup.class,
                    sample.lookup(".m3-segmented-button-group"));
            field.setText("not a date");
            assertFalse(field.commitEditorText());
            assertEquals("Enter a valid date", field.getValidationErrorText());

            language.selectIndex(1);
            assertEquals("Bitte ein korrektes Datum eingeben", field.getValidationErrorText());
            assertEquals("not a date", field.getText());
            assertEquals(DATE, field.getValue());

            language.selectIndex(0);
            assertEquals("Enter a valid date", field.getValidationErrorText());
            assertEquals("not a date", field.getText());
        });
    }

    /// Attaches the focused sample to a scene and creates its normal CSS-selected skins.
    private static VBox createSample() {
        VBox sample = DatePickersDemoPage.createLocalizationSample(DATE);
        new Scene(sample, 760.0, 520.0);
        sample.applyCss();
        sample.layout();
        return sample;
    }
}
