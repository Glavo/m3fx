// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
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
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies single-date dialog content, OK state, and result conversion.
    @Test
    void datePickerDialogConvertsAcceptedDate() {
        FxTestUtils.runOnFxThread(() -> {
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
            assertEquals(value, dialog.getResultConverter().call(ButtonType.OK));
            assertNull(dialog.getResultConverter().call(ButtonType.CANCEL));

            dialog.clearValue();

            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());
        });
    }

    /// Verifies single-date dialog delegates picker configuration.
    @Test
    void datePickerDialogDelegatesPickerConfiguration() {
        FxTestUtils.runOnFxThread(() -> {
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

    /// Verifies common single-date preset factories.
    @Test
    void datePresetsCreateCommonDates() {
        LocalDate anchor = LocalDate.of(2026, 5, 19);

        assertEquals(anchor, M3DatePresets.today(anchor).date());
        assertEquals(anchor.plusDays(1), M3DatePresets.tomorrow(anchor).date());
        assertEquals(anchor.minusDays(1), M3DatePresets.yesterday(anchor).date());
        assertEquals(anchor.plusDays(7), M3DatePresets.daysFrom(anchor, 7).date());
        assertEquals(anchor.minusDays(7), M3DatePresets.daysFrom(anchor, -7).date());
        assertEquals(LocalDate.of(2026, 5, 1), M3DatePresets.thisMonthStart(anchor).date());
        assertEquals(LocalDate.of(2026, 6, 1), M3DatePresets.nextMonthStart(anchor).date());
        assertEquals(5, M3DatePresets.common(anchor).size());
    }

    /// Verifies single-date dialog preset actions update the selected date.
    @Test
    void datePickerDialogAppliesPresetActions() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DatePickerDialog dialog = new M3DatePickerDialog();
            M3DialogPane pane = dialog.getM3DialogPane();

            applyCss(pane);
            assertSame(dialog.getPicker(), pane.getContent());

            dialog.addPreset(M3DatePresets.today(anchor));
            dialog.addPresets(
                    M3DatePresets.tomorrow(anchor),
                    M3DatePresets.daysFrom(anchor, 7),
                    M3DatePresets.thisMonthStart(anchor),
                    M3DatePresets.nextMonthStart(anchor)
            );
            applyCss(pane);

            assertInstanceOf(HBox.class, pane.getContent());
            assertEquals(5, dialog.getPresets().size());
            assertEquals(5, pane.lookupAll("." + M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());

            presetButton(pane, M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS, "In 7 days").fire();

            assertEquals(anchor.plusDays(7), dialog.getValue());
            assertEquals(YearMonth.from(anchor), dialog.getDisplayedMonth());
            assertFalse(pane.lookupButton(ButtonType.OK).isDisabled());

            M3DatePreset custom = new M3DatePreset("Release", LocalDate.of(2026, 6, 15));
            custom.applyTo(dialog.getPicker());

            assertEquals(custom.date(), dialog.getValue());
            assertEquals(YearMonth.of(2026, 6), dialog.getDisplayedMonth());

            dialog.clearPresets();

            assertSame(dialog.getPicker(), pane.getContent());
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
        FxTestUtils.runOnFxThread(() -> {
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
            assertEquals(new M3DateRange(start, end), dialog.getResultConverter().call(ButtonType.OK));
            assertNull(dialog.getResultConverter().call(ButtonType.CANCEL));
        });
    }

    /// Verifies date range dialog delegates picker configuration.
    @Test
    void dateRangePickerDialogDelegatesPickerConfiguration() {
        FxTestUtils.runOnFxThread(() -> {
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
        FxTestUtils.runOnFxThread(() -> {
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

            presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Next 7 days").fire();

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
        FxTestUtils.runOnFxThread(() -> {
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
            assertEquals(value, dialog.getResultConverter().call(ButtonType.OK));
            assertNull(dialog.getResultConverter().call(ButtonType.CANCEL));

            dialog.clearValue();

            assertTrue(pane.lookupButton(ButtonType.OK).isDisabled());
        });
    }

    /// Verifies time dialog delegates picker configuration.
    @Test
    void timePickerDialogDelegatesPickerConfiguration() {
        FxTestUtils.runOnFxThread(() -> {
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

    /// Verifies common time preset factories.
    @Test
    void timePresetsCreateCommonTimes() {
        LocalTime anchor = LocalTime.of(10, 30, 45);

        assertEquals(LocalTime.of(10, 30), M3TimePresets.now(anchor).time());
        assertEquals(LocalTime.of(10, 45), M3TimePresets.minutesFrom(anchor, 15).time());
        assertEquals(LocalTime.of(10, 15), M3TimePresets.minutesFrom(anchor, -15).time());
        assertEquals(LocalTime.MIDNIGHT, M3TimePresets.midnight().time());
        assertEquals(LocalTime.of(9, 0), M3TimePresets.morning().time());
        assertEquals(LocalTime.NOON, M3TimePresets.noon().time());
        assertEquals(LocalTime.of(15, 0), M3TimePresets.afternoon().time());
        assertEquals(LocalTime.of(18, 0), M3TimePresets.evening().time());
        assertEquals(5, M3TimePresets.common(anchor).size());
    }

    /// Verifies time dialog preset actions update the selected time.
    @Test
    void timePickerDialogAppliesPresetActions() {
        FxTestUtils.runOnFxThread(() -> {
            LocalTime anchor = LocalTime.of(10, 30);
            M3TimePickerDialog dialog = new M3TimePickerDialog();
            M3DialogPane pane = dialog.getM3DialogPane();

            applyCss(pane);
            assertSame(dialog.getPicker(), pane.getContent());

            dialog.addPreset(M3TimePresets.now(anchor));
            dialog.addPresets(
                    M3TimePresets.minutesFrom(anchor, 15),
                    M3TimePresets.morning(),
                    M3TimePresets.noon(),
                    M3TimePresets.evening()
            );
            applyCss(pane);

            assertInstanceOf(HBox.class, pane.getContent());
            assertEquals(5, dialog.getPresets().size());
            assertEquals(5, pane.lookupAll("." + M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());

            presetButton(pane, M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS, "In 15 min").fire();

            assertEquals(LocalTime.of(10, 45), dialog.getValue());
            assertFalse(pane.lookupButton(ButtonType.OK).isDisabled());

            M3TimePreset custom = new M3TimePreset("Release", LocalTime.of(16, 30));
            custom.applyTo(dialog.getPicker());

            assertEquals(custom.time(), dialog.getValue());

            dialog.clearPresets();

            assertSame(dialog.getPicker(), pane.getContent());
        });
    }

    /// Verifies that dialog preset action columns support keyboard traversal and picker handoff.
    @Test
    void pickerDialogPresetKeyboardNavigationMovesWithinColumnAndToPicker() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate dateAnchor = LocalDate.of(2026, 5, 19);
            LocalTime timeAnchor = LocalTime.of(10, 30);
            M3DatePickerDialog dateDialog = new M3DatePickerDialog(dateAnchor);
            M3DateRangePickerDialog rangeDialog = new M3DateRangePickerDialog(
                    dateAnchor,
                    dateAnchor.plusDays(6)
            );
            M3TimePickerDialog timeDialog = new M3TimePickerDialog(timeAnchor);
            dateDialog.setCommonPresets(dateAnchor);
            rangeDialog.setCommonPresets(dateAnchor);
            timeDialog.setCommonPresets(timeAnchor);

            M3DialogPane datePane = dateDialog.getM3DialogPane();
            M3DialogPane rangePane = rangeDialog.getM3DialogPane();
            M3DialogPane timePane = timeDialog.getM3DialogPane();
            rangePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            Pane root = new Pane(datePane, rangePane, timePane);
            Scene scene = new Scene(root, 920.0, 860.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                datePane.resizeRelocate(24.0, 24.0, 680.0, 240.0);
                rangePane.resizeRelocate(24.0, 292.0, 780.0, 260.0);
                timePane.resizeRelocate(24.0, 580.0, 680.0, 240.0);
                root.layout();

                M3Button today = presetButton(datePane, M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Today");
                M3Button tomorrow = presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Tomorrow"
                );
                M3Button nextMonth = presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Next month"
                );

                today.requestFocus();

                datePane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(today.isFocused());

                datePane.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(today.isFocused());

                today.fireEvent(keyPressed(KeyCode.DOWN));

                assertTrue(tomorrow.isFocused());

                tomorrow.fireEvent(keyPressed(KeyCode.END));

                assertTrue(nextMonth.isFocused());

                nextMonth.fireEvent(keyPressed(KeyCode.RIGHT));

                assertFocusInsidePicker(scene, dateDialog.getPicker());

                datePane.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertFocusInsidePicker(scene, dateDialog.getPicker());

                M3Button rangeToday = presetButton(
                        rangePane,
                        M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Today"
                );
                M3Button rangeTomorrow = presetButton(
                        rangePane,
                        M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Tomorrow"
                );

                rangeToday.requestFocus();
                rangeToday.fireEvent(keyPressed(KeyCode.DOWN));

                assertTrue(rangeTomorrow.isFocused());

                rangePane.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(rangeTomorrow.isFocused());

                rangeTomorrow.fireEvent(keyPressed(KeyCode.LEFT));

                assertFocusInsidePicker(scene, rangeDialog.getPicker());

                M3Button now = presetButton(timePane, M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Now");
                M3Button inFifteenMinutes = presetButton(
                        timePane,
                        M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "In 15 min"
                );
                M3Button evening = presetButton(timePane, M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Evening");

                now.requestFocus();
                now.fireEvent(keyPressed(KeyCode.DOWN));

                assertTrue(inFifteenMinutes.isFocused());

                inFifteenMinutes.fireEvent(keyPressed(KeyCode.END));

                assertTrue(evening.isFocused());

                evening.fireEvent(keyPressed(KeyCode.PAGE_UP));

                assertTrue(now.isFocused());

                now.fireEvent(keyPressed(KeyCode.RIGHT));

                assertFocusInsidePicker(scene, timeDialog.getPicker());
            } finally {
                stage.close();
            }
        });
    }

    /// Applies the M3FX theme to a dialog pane and creates its content skin.
    private static void applyCss(M3DialogPane pane) {
        Pane root = new Pane(pane);
        Scene scene = new Scene(root, 640.0, 420.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }


    /// Returns the preset button with the supplied style class and text.
    private static M3Button presetButton(M3DialogPane pane, String styleClass, String text) {
        for (Node node : pane.lookupAll("." + styleClass)) {
            if (node instanceof M3Button button && button.getText().equals(text)) {
                return button;
            }
        }
        throw new AssertionError("Preset button not found: " + text);
    }

    /// Verifies that the current scene focus is on a picker or one of its focusable descendants.
    private static void assertFocusInsidePicker(Scene scene, Node picker) {
        @Nullable Node focusOwner = scene.getFocusOwner();
        assertTrue(focusOwner != null && (focusOwner == picker || M3Accessible.containsNode(picker, focusOwner)));
    }

    /// Creates a key press event for picker dialog keyboard tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

}
