// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests Material picker dialog presets and their result conversion.
@NotNullByDefault
final class M3PickerDialogTest {
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

    /// Verifies single-date dialog content, OK state, and result conversion.
    @Test
    void datePickerDialogConvertsAcceptedDate() {
        runOnFxThread(() -> {
            LocalDate value = LocalDate.of(2026, 5, 19);
            M3DatePickerDialog dialog = new M3DatePickerDialog();
            M3DialogPane pane = dialog.getM3DialogPane();

            applyCss(pane);
            assertSame(dialog.getPicker(), pane.getContent());
            assertEquals(M3DatePickerDialog.DEFAULT_TITLE, dialog.getTitle());
            assertEquals(M3DatePickerDialog.DEFAULT_TITLE, pane.getHeaderText());
            assertInstanceOf(M3DatePicker.class, pane.getContent());
            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());

            dialog.setValue(value);

            assertFalse(pane.lookupButton(ButtonType.OK).isDisabled());
            assertEquals(value, convertResult(dialog, ButtonType.OK));
            assertNull(convertResult(dialog, ButtonType.CANCEL));

            dialog.clearValue();

            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());
        });
    }

    /// Verifies single-date dialog delegates picker configuration.
    @Test
    void datePickerDialogDelegatesPickerConfiguration() {
        runOnFxThread(() -> {
            M3DatePickerDialog dialog = new M3DatePickerDialog(LocalDate.of(2026, 5, 20));
            LocalDate min = LocalDate.of(2026, 5, 1);
            LocalDate max = LocalDate.of(2026, 5, 31);
            YearMonth month = YearMonth.of(2026, 6);

            dialog.setMinDate(min);
            dialog.setMaxDate(max);
            dialog.setDisplayedMonth(month);
            dialog.setFirstDayOfWeek(DayOfWeek.MONDAY);
            dialog.setShowAdjacentMonthDays(false);

            assertEquals(min, dialog.getMinDate());
            assertEquals(max, dialog.getMaxDate());
            assertEquals(month, dialog.getDisplayedMonth());
            assertEquals(DayOfWeek.MONDAY, dialog.getFirstDayOfWeek());
            assertFalse(dialog.isShowAdjacentMonthDays());
            assertTrue(dialog.isDateDisabled(LocalDate.of(2026, 6, 1)));
        });
    }

    /// Verifies date range record validation and containment.
    @Test
    void dateRangeValidatesOrderingAndContainsDates() {
        LocalDate start = LocalDate.of(2026, 5, 19);
        LocalDate end = LocalDate.of(2026, 5, 23);
        M3DateRange range = new M3DateRange(start, end);

        assertEquals(start, range.startDate());
        assertEquals(end, range.endDate());
        assertTrue(range.contains(LocalDate.of(2026, 5, 21)));
        assertFalse(range.contains(LocalDate.of(2026, 5, 24)));
        assertThrows(IllegalArgumentException.class, () -> new M3DateRange(end, start));
    }

    /// Verifies common date range preset factories.
    @Test
    void dateRangePresetsCreateCommonRanges() {
        LocalDate anchor = LocalDate.of(2026, 5, 19);

        assertEquals(new M3DateRange(anchor, anchor), M3DateRangePresets.today(anchor).range());
        assertEquals(new M3DateRange(anchor.plusDays(1), anchor.plusDays(1)),
                M3DateRangePresets.tomorrow(anchor).range());
        assertEquals(new M3DateRange(anchor, anchor.plusDays(6)), M3DateRangePresets.nextDays(anchor, 7).range());
        assertEquals(new M3DateRange(anchor.minusDays(6), anchor),
                M3DateRangePresets.previousDays(anchor, 7).range());
        assertEquals(new M3DateRange(LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 24)),
                M3DateRangePresets.thisWeek(anchor, DayOfWeek.MONDAY).range());
        assertEquals(new M3DateRange(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 31)),
                M3DateRangePresets.nextWeek(anchor, DayOfWeek.MONDAY).range());
        assertEquals(new M3DateRange(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)),
                M3DateRangePresets.thisMonth(anchor).range());
        assertEquals(new M3DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)),
                M3DateRangePresets.nextMonth(anchor).range());
        assertEquals(6, M3DateRangePresets.common(anchor, DayOfWeek.MONDAY).size());
        assertThrows(IllegalArgumentException.class, () -> M3DateRangePresets.nextDays(anchor, 0));
    }

    /// Verifies range dialog content, OK state, and result conversion.
    @Test
    void dateRangePickerDialogConvertsCompleteRange() {
        runOnFxThread(() -> {
            LocalDate start = LocalDate.of(2026, 5, 19);
            LocalDate end = LocalDate.of(2026, 5, 23);
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
            M3DialogPane pane = dialog.getM3DialogPane();

            applyCss(pane);
            assertSame(dialog.getPicker(), pane.getContent());
            assertEquals(M3DateRangePickerDialog.DEFAULT_TITLE, dialog.getTitle());
            assertEquals(M3DateRangePickerDialog.DEFAULT_TITLE, pane.getHeaderText());
            assertInstanceOf(M3DateRangePicker.class, pane.getContent());
            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());

            dialog.setStartDate(start);

            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());

            dialog.setEndDate(end);

            assertFalse(pane.lookupButton(ButtonType.OK).isDisabled());
            assertEquals(new M3DateRange(start, end), dialog.getRange());
            assertEquals(new M3DateRange(start, end), convertResult(dialog, ButtonType.OK));
            assertNull(convertResult(dialog, ButtonType.CANCEL));
        });
    }

    /// Verifies date range dialog delegates picker configuration.
    @Test
    void dateRangePickerDialogDelegatesPickerConfiguration() {
        runOnFxThread(() -> {
            LocalDate start = LocalDate.of(2026, 5, 19);
            LocalDate end = LocalDate.of(2026, 5, 23);
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog(new M3DateRange(start, end));

            dialog.setMinDate(LocalDate.of(2026, 5, 1));
            dialog.setMaxDate(LocalDate.of(2026, 5, 31));
            dialog.setDisplayedMonth(YearMonth.of(2026, 5));
            dialog.setFirstDayOfWeek(DayOfWeek.SUNDAY);
            dialog.setShowAdjacentMonthDays(false);

            assertEquals(start, dialog.getStartDate());
            assertEquals(end, dialog.getEndDate());
            assertTrue(dialog.isRangeComplete());
            assertTrue(dialog.isDateInSelectedRange(LocalDate.of(2026, 5, 21)));
            assertFalse(dialog.isShowAdjacentMonthDays());
            assertTrue(dialog.isDateDisabled(LocalDate.of(2026, 6, 1)));

            dialog.clearRange();

            assertNull(dialog.getRange());
        });
    }

    /// Verifies date range dialog preset actions update the selected range.
    @Test
    void dateRangePickerDialogAppliesPresetActions() {
        runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
            M3DialogPane pane = dialog.getM3DialogPane();

            applyCss(pane);
            assertSame(dialog.getPicker(), pane.getContent());

            dialog.addPreset(M3DateRangePresets.today(anchor));
            dialog.addPresets(
                    M3DateRangePresets.tomorrow(anchor),
                    M3DateRangePresets.nextDays(anchor, 7),
                    M3DateRangePresets.thisWeek(anchor, dialog.getFirstDayOfWeek()),
                    M3DateRangePresets.nextWeek(anchor, dialog.getFirstDayOfWeek()),
                    M3DateRangePresets.thisMonth(anchor)
            );
            applyCss(pane);

            assertInstanceOf(HBox.class, pane.getContent());
            assertEquals(6, dialog.getPresets().size());
            assertEquals(6, pane.lookupAll("." + M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());

            nextSevenDaysPresetButton(pane).fire();

            assertEquals(new M3DateRange(anchor, anchor.plusDays(6)), dialog.getRange());
            assertEquals(YearMonth.from(anchor), dialog.getDisplayedMonth());
            assertFalse(pane.lookupButton(ButtonType.OK).isDisabled());

            M3DateRangePreset custom = new M3DateRangePreset(
                    "Sprint",
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 14)
            );
            custom.applyTo(dialog.getPicker());

            assertEquals(custom.range(), dialog.getRange());
            assertEquals(YearMonth.of(2026, 6), dialog.getDisplayedMonth());

            dialog.clearPresets();

            assertSame(dialog.getPicker(), pane.getContent());
        });
    }

    /// Verifies time dialog content, OK state, and result conversion.
    @Test
    void timePickerDialogConvertsAcceptedTime() {
        runOnFxThread(() -> {
            LocalTime value = LocalTime.of(10, 30);
            M3TimePickerDialog dialog = new M3TimePickerDialog();
            M3DialogPane pane = dialog.getM3DialogPane();

            applyCss(pane);
            assertSame(dialog.getPicker(), pane.getContent());
            assertEquals(M3TimePickerDialog.DEFAULT_TITLE, dialog.getTitle());
            assertEquals(M3TimePickerDialog.DEFAULT_TITLE, pane.getHeaderText());
            assertInstanceOf(M3TimePicker.class, pane.getContent());
            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());

            dialog.setValue(value);

            assertFalse(pane.lookupButton(ButtonType.OK).isDisabled());
            assertEquals(value, convertResult(dialog, ButtonType.OK));
            assertNull(convertResult(dialog, ButtonType.CANCEL));

            dialog.clearValue();

            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());
        });
    }

    /// Verifies time dialog delegates picker configuration.
    @Test
    void timePickerDialogDelegatesPickerConfiguration() {
        runOnFxThread(() -> {
            M3TimePickerDialog dialog = new M3TimePickerDialog(LocalTime.of(10, 30, 45));
            LocalTime min = LocalTime.of(9, 0);
            LocalTime max = LocalTime.of(17, 30);

            dialog.setUse24HourClock(true);
            dialog.setMinuteStep(15);
            dialog.setMinTime(min);
            dialog.setMaxTime(max);

            assertEquals(LocalTime.of(10, 30), dialog.getValue());
            assertTrue(dialog.isUse24HourClock());
            assertEquals(15, dialog.getMinuteStep());
            assertEquals(min, dialog.getMinTime());
            assertEquals(max, dialog.getMaxTime());
            assertTrue(dialog.isTimeDisabled(LocalTime.of(18, 0)));
        });
    }

    /// Applies the M3FX theme to a dialog pane and creates its content skin.
    private static void applyCss(M3DialogPane pane) {
        Pane root = new Pane(pane);
        Scene scene = new Scene(root, 640.0, 420.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }

    /// Converts a dialog button through the dialog result converter.
    @SuppressWarnings("DataFlowIssue")
    private static <T> @Nullable T convertResult(M3Dialog<T> dialog, ButtonType buttonType) {
        return dialog.getResultConverter().call(buttonType);
    }

    /// Returns the `Next 7 days` preset button.
    private static M3Button nextSevenDaysPresetButton(M3DialogPane pane) {
        for (Node node : pane.lookupAll("." + M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS)) {
            if (node instanceof M3Button button && button.getText().equals("Next 7 days")) {
                return button;
            }
        }
        throw new AssertionError("Preset button not found: Next 7 days");
    }

    /// Runs one assertion block on the JavaFX application thread.
    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }

        @Nullable Throwable throwable = failure.get();
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        if (throwable != null) {
            throw new AssertionError(throwable);
        }
    }
}
