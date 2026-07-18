// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.glavo.m3fx.internal.M3Accessible;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests Material picker dialog presets, state, and action lifecycle contracts.
@NotNullByDefault
final class M3PickerDialogTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies single-date dialog content and acceptance state.
    @Test
    void datePickerDialogConfiguresContentAndAcceptance() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate value = LocalDate.of(2026, 5, 19);
            M3DatePickerDialog dialog = new M3DatePickerDialog();
            M3DialogPane pane = dialog.getDialogPane();

            applyCss(pane);
            HBox content = assertInstanceOf(HBox.class, pane.getContent());
            assertSame(dialog.getPicker(), content.getChildren().get(1));
            assertSame(content, dialog.getPicker().getParent());
            assertFalse(content.getChildren().get(0).isManaged());
            assertEquals("Select date", pane.getHeaderText());
            assertTrue(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            dialog.setValue(value);

            assertFalse(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            dialog.setValue(null);

            assertTrue(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());
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

            dialog.getPicker().setMinDate(min);
            dialog.getPicker().setMaxDate(max);
            dialog.getPicker().setDisplayedMonth(month);
            dialog.getPicker().setFirstDayOfWeek(DayOfWeek.MONDAY);
            dialog.getPicker().setShowAdjacentMonthDays(false);

            assertEquals(min, dialog.getPicker().getMinDate());
            assertEquals(max, dialog.getPicker().getMaxDate());
            assertEquals(month, dialog.getPicker().getDisplayedMonth());
            assertEquals(DayOfWeek.MONDAY, dialog.getPicker().getFirstDayOfWeek());
            assertFalse(dialog.getPicker().isShowAdjacentMonthDays());
            assertTrue(dialog.getPicker().isDateDisabled(LocalDate.of(2026, 6, 1)));
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

    /// Verifies single-date preset factory failure paths and immutable common lists.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void datePresetFactoriesValidateInputsAndReturnImmutableLists() {
        LocalDate anchor = LocalDate.of(2026, 5, 19);

        assertThrows(NullPointerException.class, () -> M3DatePresets.today(null));
        assertThrows(NullPointerException.class, () -> M3DatePresets.tomorrow(null));
        assertThrows(NullPointerException.class, () -> M3DatePresets.yesterday(null));
        assertThrows(NullPointerException.class, () -> M3DatePresets.daysFrom(null, 7));
        assertThrows(NullPointerException.class, () -> M3DatePresets.thisMonthStart(null));
        assertThrows(NullPointerException.class, () -> M3DatePresets.nextMonthStart(null));
        assertThrows(NullPointerException.class, () -> M3DatePresets.common(null));
        assertThrows(UnsupportedOperationException.class,
                () -> M3DatePresets.common(anchor).add(M3DatePresets.today(anchor)));
    }

    /// Verifies single-date dialog preset actions update the selected date.
    @Test
    void datePickerDialogAppliesPresetActions() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DatePickerDialog dialog = new M3DatePickerDialog();
            M3DialogPane pane = dialog.getDialogPane();

            applyCss(pane);
            HBox content = assertInstanceOf(HBox.class, pane.getContent());
            Node pickerParent = dialog.getPicker().getParent();
            assertFalse(content.getChildren().get(0).isManaged());

            dialog.getPresets().add(M3DatePresets.today(anchor));
            dialog.getPresets().addAll(
                    M3DatePresets.tomorrow(anchor),
                    M3DatePresets.daysFrom(anchor, 7),
                    M3DatePresets.thisMonthStart(anchor),
                    M3DatePresets.nextMonthStart(anchor)
            );
            applyCss(pane);

            assertSame(content, pane.getContent());
            assertSame(pickerParent, dialog.getPicker().getParent());
            assertEquals(5, dialog.getPresets().size());
            assertEquals(5, pane.lookupAll("." + M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());

            presetButton(pane, M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS, "In 7 days").fire();

            assertEquals(anchor.plusDays(7), dialog.getValue());
            assertEquals(YearMonth.from(anchor), dialog.getPicker().getDisplayedMonth());
            assertFalse(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            M3DatePreset custom = new M3DatePreset("Release", LocalDate.of(2026, 6, 15));
            dialog.getPicker().applyPreset(custom);

            assertEquals(custom.date(), dialog.getValue());
            assertEquals(YearMonth.of(2026, 6), dialog.getPicker().getDisplayedMonth());

            dialog.getPresets().clear();

            assertSame(content, pane.getContent());
            assertSame(pickerParent, dialog.getPicker().getParent());
            assertFalse(content.getChildren().get(0).isManaged());
            assertTrue(pane.lookupAll("." + M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS).isEmpty());
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

    /// Verifies date range preset factory failure paths and immutable common lists.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void dateRangePresetFactoriesValidateInputsAndReturnImmutableLists() {
        LocalDate anchor = LocalDate.of(2026, 5, 19);

        assertThrows(NullPointerException.class, () -> M3DateRangePresets.today(null));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.tomorrow(null));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.nextDays(null, 7));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.previousDays(null, 7));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.thisWeek(null, DayOfWeek.MONDAY));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.thisWeek(anchor, null));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.nextWeek(null, DayOfWeek.MONDAY));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.nextWeek(anchor, null));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.thisMonth(null));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.nextMonth(null));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.common(null, DayOfWeek.MONDAY));
        assertThrows(NullPointerException.class, () -> M3DateRangePresets.common(anchor, null));
        assertThrows(IllegalArgumentException.class, () -> M3DateRangePresets.nextDays(anchor, -1));
        assertThrows(IllegalArgumentException.class, () -> M3DateRangePresets.previousDays(anchor, 0));
        assertThrows(IllegalArgumentException.class, () -> M3DateRangePresets.previousDays(anchor, -1));
        assertThrows(UnsupportedOperationException.class,
                () -> M3DateRangePresets.common(anchor, DayOfWeek.MONDAY)
                        .add(M3DateRangePresets.today(anchor)));
    }

    /// Verifies date-range dialog content and acceptance state.
    @Test
    void dateRangePickerDialogConfiguresContentAndAcceptance() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate start = LocalDate.of(2026, 5, 19);
            LocalDate end = LocalDate.of(2026, 5, 23);
            M3DateRange range = new M3DateRange(start, end);
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
            M3DialogPane pane = dialog.getDialogPane();

            applyCss(pane);
            HBox content = assertInstanceOf(HBox.class, pane.getContent());
            assertSame(dialog.getPicker(), content.getChildren().get(1));
            assertSame(content, dialog.getPicker().getParent());
            assertFalse(content.getChildren().get(0).isManaged());
            assertEquals("Select date range", pane.getHeaderText());
            assertTrue(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            dialog.getPicker().setStartDate(start);

            assertTrue(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            dialog.getPicker().setEndDate(end);

            assertFalse(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());
            assertEquals(range, dialog.getPicker().getRange());
        });
    }

    /// Verifies date range dialog delegates picker configuration.
    @Test
    void dateRangePickerDialogDelegatesPickerConfiguration() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate start = LocalDate.of(2026, 5, 19);
            LocalDate end = LocalDate.of(2026, 5, 23);
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog(new M3DateRange(start, end));

            dialog.getPicker().setMinDate(LocalDate.of(2026, 5, 1));
            dialog.getPicker().setMaxDate(LocalDate.of(2026, 5, 31));
            dialog.getPicker().setDisplayedMonth(YearMonth.of(2026, 5));
            dialog.getPicker().setFirstDayOfWeek(DayOfWeek.SUNDAY);
            dialog.getPicker().setShowAdjacentMonthDays(false);

            assertEquals(start, dialog.getPicker().getStartDate());
            assertEquals(end, dialog.getPicker().getEndDate());
            assertTrue(dialog.getPicker().isRangeComplete());
            assertTrue(dialog.getPicker().isDateInSelectedRange(LocalDate.of(2026, 5, 21)));
            assertFalse(dialog.getPicker().isShowAdjacentMonthDays());
            assertTrue(dialog.getPicker().isDateDisabled(LocalDate.of(2026, 6, 1)));

            dialog.getPicker().clearRange();

            assertNull(dialog.getPicker().getRange());
        });
    }

    /// Verifies date range dialog preset actions update the selected range.
    @Test
    void dateRangePickerDialogAppliesPresetActions() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
            M3DialogPane pane = dialog.getDialogPane();

            applyCss(pane);
            HBox content = assertInstanceOf(HBox.class, pane.getContent());
            Node pickerParent = dialog.getPicker().getParent();
            assertFalse(content.getChildren().get(0).isManaged());

            dialog.getPresets().add(M3DateRangePresets.today(anchor));
            dialog.getPresets().addAll(
                    M3DateRangePresets.tomorrow(anchor),
                    M3DateRangePresets.nextDays(anchor, 7),
                    M3DateRangePresets.thisWeek(anchor, dialog.getPicker().getFirstDayOfWeek()),
                    M3DateRangePresets.nextWeek(anchor, dialog.getPicker().getFirstDayOfWeek()),
                    M3DateRangePresets.thisMonth(anchor)
            );
            applyCss(pane);

            assertSame(content, pane.getContent());
            assertSame(pickerParent, dialog.getPicker().getParent());
            assertEquals(6, dialog.getPresets().size());
            assertEquals(6, pane.lookupAll("." + M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());

            presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Next 7 days").fire();

            assertEquals(new M3DateRange(anchor, anchor.plusDays(6)), dialog.getPicker().getRange());
            assertEquals(YearMonth.from(anchor), dialog.getPicker().getDisplayedMonth());
            assertFalse(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            M3DateRangePreset custom = new M3DateRangePreset(
                    "Sprint",
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 14)
            );
            dialog.getPicker().applyPreset(custom);

            assertEquals(custom.range(), dialog.getPicker().getRange());
            assertEquals(YearMonth.of(2026, 6), dialog.getPicker().getDisplayedMonth());

            dialog.getPresets().clear();

            assertSame(content, pane.getContent());
            assertSame(pickerParent, dialog.getPicker().getParent());
            assertFalse(content.getChildren().get(0).isManaged());
            assertTrue(pane.lookupAll("." + M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS).isEmpty());
        });
    }

    /// Verifies date range dialog preset buttons track picker date bounds.
    @Test
    void dateRangePickerDialogPresetButtonsTrackDateBounds() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate anchor = LocalDate.of(2026, 5, 19);
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
            M3DialogPane pane = dialog.getDialogPane();

            dialog.getPresets().setAll(
                    M3DateRangePresets.today(anchor),
                    M3DateRangePresets.tomorrow(anchor),
                    M3DateRangePresets.nextDays(anchor, 7)
            );
            applyCss(pane);

            assertFalse(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Today").isDisabled());
            assertFalse(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Tomorrow").isDisabled());
            assertFalse(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Next 7 days").isDisabled());

            dialog.getPicker().setMaxDate(anchor.plusDays(2));

            assertFalse(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Today").isDisabled());
            assertFalse(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Tomorrow").isDisabled());
            assertTrue(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Next 7 days").isDisabled());

            dialog.getPicker().setMinDate(anchor.plusDays(1));

            assertTrue(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Today").isDisabled());
            assertFalse(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Tomorrow").isDisabled());
            assertTrue(presetButton(pane, M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Next 7 days").isDisabled());
        });
    }

    /// Verifies that picker bounds and incremental preset changes preserve unaffected controls, skins, and focus.
    @Test
    void pickerDialogPresetUpdatesPreserveNodesAndFocus() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate dateAnchor = LocalDate.of(2026, 5, 19);
            LocalTime timeAnchor = LocalTime.of(10, 30);
            M3DatePickerDialog dateDialog = new M3DatePickerDialog(dateAnchor);
            M3DateRangePickerDialog rangeDialog = new M3DateRangePickerDialog(
                    dateAnchor,
                    dateAnchor.plusDays(1)
            );
            M3TimePickerDialog timeDialog = new M3TimePickerDialog(timeAnchor);

            M3DatePreset dateTodayPreset = M3DatePresets.today(dateAnchor);
            M3DatePreset dateTomorrowPreset = M3DatePresets.tomorrow(dateAnchor);
            dateDialog.getPresets().setAll(
                    dateTodayPreset,
                    dateTomorrowPreset,
                    M3DatePresets.daysFrom(dateAnchor, 7)
            );
            rangeDialog.getPresets().setAll(
                    M3DateRangePresets.today(dateAnchor),
                    M3DateRangePresets.nextDays(dateAnchor, 7)
            );
            timeDialog.getPresets().setAll(
                    M3TimePresets.now(timeAnchor),
                    M3TimePresets.evening()
            );

            M3DialogPane datePane = dateDialog.getDialogPane();
            M3DialogPane rangePane = rangeDialog.getDialogPane();
            M3DialogPane timePane = timeDialog.getDialogPane();
            Pane root = new Pane(datePane, rangePane, timePane);
            Scene scene = new Scene(root, 960.0, 920.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                datePane.resizeRelocate(24.0, 24.0, 700.0, 260.0);
                rangePane.resizeRelocate(24.0, 316.0, 780.0, 260.0);
                timePane.resizeRelocate(24.0, 608.0, 700.0, 260.0);
                root.applyCss();
                root.layout();

                Node dateContent = datePane.getContent();
                Node datePickerParent = dateDialog.getPicker().getParent();
                M3Button dateToday = presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Today"
                );
                M3Button dateTomorrow = presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Tomorrow"
                );
                Object dateTodaySkin = dateToday.getSkin();
                dateToday.requestFocus();
                assertTrue(dateToday.isFocused());

                dateDialog.getPicker().setMaxDate(dateAnchor.plusDays(2));

                assertSame(dateContent, datePane.getContent());
                assertSame(datePickerParent, dateDialog.getPicker().getParent());
                assertSame(dateToday, presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Today"
                ));
                assertSame(dateTodaySkin, dateToday.getSkin());
                assertTrue(dateToday.isFocused());
                assertTrue(presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "In 7 days"
                ).isDisabled());

                M3DatePreset releasePreset = new M3DatePreset("Release", dateAnchor.plusDays(2));
                dateDialog.getPresets().add(releasePreset);

                assertSame(dateToday, presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Today"
                ));
                assertSame(dateTomorrow, presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Tomorrow"
                ));
                dateDialog.getPresets().remove(dateTomorrowPreset);
                assertNull(dateTomorrow.getParent());
                assertNull(dateTomorrow.getOnAction());
                assertSame(dateToday, presetButton(
                        datePane,
                        M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Today"
                ));
                presetButton(datePane, M3DatePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Release").fire();
                assertEquals(releasePreset.date(), dateDialog.getValue());

                Node rangeContent = rangePane.getContent();
                Node rangePickerParent = rangeDialog.getPicker().getParent();
                M3Button rangeToday = presetButton(
                        rangePane,
                        M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Today"
                );
                Object rangeTodaySkin = rangeToday.getSkin();

                rangeDialog.getPicker().setMaxDate(dateAnchor.plusDays(2));

                assertSame(rangeContent, rangePane.getContent());
                assertSame(rangePickerParent, rangeDialog.getPicker().getParent());
                assertSame(rangeToday, presetButton(
                        rangePane,
                        M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Today"
                ));
                assertSame(rangeTodaySkin, rangeToday.getSkin());
                assertTrue(presetButton(
                        rangePane,
                        M3DateRangePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Next 7 days"
                ).isDisabled());

                Node timeContent = timePane.getContent();
                Node timePickerParent = timeDialog.getPicker().getParent();
                M3Button now = presetButton(
                        timePane,
                        M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Now"
                );
                Object nowSkin = now.getSkin();

                timeDialog.getPicker().setMaxTime(LocalTime.NOON);

                assertSame(timeContent, timePane.getContent());
                assertSame(timePickerParent, timeDialog.getPicker().getParent());
                assertSame(now, presetButton(
                        timePane,
                        M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Now"
                ));
                assertSame(nowSkin, now.getSkin());
                assertTrue(presetButton(
                        timePane,
                        M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Evening"
                ).isDisabled());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies time dialog content and acceptance state.
    @Test
    void timePickerDialogConfiguresContentAndAcceptance() {
        FxTestUtils.runOnFxThread(() -> {
            LocalTime value = LocalTime.of(10, 30);
            M3TimePickerDialog dialog = new M3TimePickerDialog();
            M3DialogPane pane = dialog.getDialogPane();

            applyCss(pane);
            HBox content = assertInstanceOf(HBox.class, pane.getContent());
            assertSame(dialog.getPicker(), content.getChildren().get(1));
            assertSame(content, dialog.getPicker().getParent());
            assertFalse(content.getChildren().get(0).isManaged());
            Region pickerContainer = assertInstanceOf(
                    Region.class,
                    dialog.getPicker().lookup("." + M3TimePicker.CONTAINER_STYLE_CLASS)
            );
            assertEquals(0.0, pickerContainer.getPadding().getTop(), 0.0001);
            assertEquals("Select time", pane.getHeaderText());
            assertTrue(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());
            assertEquals(1, pane.lookupAll("." + M3TimePicker.MODE_BUTTON_STYLE_CLASS).size());
            M3IconButton modeButton = assertInstanceOf(
                    M3IconButton.class,
                    pane.lookup("." + M3TimePicker.MODE_BUTTON_STYLE_CLASS)
            );
            assertFalse(dialog.getPicker().isInputMode());
            modeButton.fire();
            assertTrue(dialog.getPicker().isInputMode());
            assertEquals("Use clock dial", modeButton.getAccessibleText());

            dialog.setValue(value);

            assertFalse(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            dialog.setValue(null);

            assertTrue(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());
        });
    }

    /// Verifies that picker dialogs distinguish confirmation from cancellation and programmatic dismissal.
    @Test
    void pickerDialogHiddenEventsExposeActionsAndCurrentState() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            LocalDate date = LocalDate.of(2026, 5, 19);
            M3DateRange range = new M3DateRange(date, date.plusDays(4));
            LocalTime time = LocalTime.of(10, 30);
            Stage stage = new Stage();
            Pane owner = new Pane();
            M3OverlayPane overlay = new M3OverlayPane();
            overlay.setContent(owner);
            Scene scene = new Scene(overlay, 720.0, 520.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();

            M3DatePickerDialog dateDialog = new M3DatePickerDialog(date);
            M3DateRangePickerDialog rangeDialog = new M3DateRangePickerDialog(range);
            M3TimePickerDialog timeDialog = new M3TimePickerDialog(time);
            M3Button dateConfirmAction = Objects.requireNonNull(
                    dateDialog.getDialogPane().getDefaultAction(),
                    "date confirmation action"
            );
            M3Button rangeConfirmAction = Objects.requireNonNull(
                    rangeDialog.getDialogPane().getDefaultAction(),
                    "range confirmation action"
            );
            M3Button rangeCancelAction = Objects.requireNonNull(
                    rangeDialog.getDialogPane().getCancelAction(),
                    "range cancel action"
            );
            M3Button timeConfirmAction = Objects.requireNonNull(
                    timeDialog.getDialogPane().getDefaultAction(),
                    "time confirmation action"
            );

            List<M3DialogEvent> hiddenEvents = new ArrayList<>();
            AtomicReference<@Nullable LocalDate> confirmedDate = new AtomicReference<>();
            AtomicReference<@Nullable M3DateRange> confirmedRange = new AtomicReference<>();
            AtomicReference<@Nullable LocalTime> confirmedTime = new AtomicReference<>();
            dateDialog.setOnHidden(event -> {
                hiddenEvents.add(event);
                if (event.getAction() == dateConfirmAction) {
                    confirmedDate.set(dateDialog.getValue());
                }
            });
            rangeDialog.setOnHidden(event -> {
                hiddenEvents.add(event);
                if (event.getAction() == rangeConfirmAction) {
                    confirmedRange.set(rangeDialog.getPicker().getRange());
                }
            });
            timeDialog.setOnHidden(event -> {
                hiddenEvents.add(event);
                if (event.getAction() == timeConfirmAction) {
                    confirmedTime.set(timeDialog.getValue());
                }
            });

            try {
                overlay.showDialog(dateDialog);
                dateConfirmAction.fire();

                assertEquals(1, hiddenEvents.size());
                assertSame(dateConfirmAction, hiddenEvents.get(0).getAction());
                assertEquals(date, confirmedDate.get());
                assertEquals(date, dateDialog.getValue());

                hiddenEvents.clear();
                overlay.showDialog(rangeDialog);
                rangeCancelAction.fire();

                assertEquals(1, hiddenEvents.size());
                assertSame(rangeCancelAction, hiddenEvents.get(0).getAction());
                assertNull(confirmedRange.get());
                assertEquals(range, rangeDialog.getPicker().getRange());

                hiddenEvents.clear();
                M3DialogHandle timeHandle = overlay.showDialog(timeDialog);
                assertTrue(timeHandle.requestClose());

                assertEquals(1, hiddenEvents.size());
                assertNull(hiddenEvents.get(0).getAction());
                assertNull(confirmedTime.get());
                assertEquals(time, timeDialog.getValue());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies time dialog delegates picker configuration.
    @Test
    void timePickerDialogDelegatesPickerConfiguration() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePickerDialog dialog = new M3TimePickerDialog(LocalTime.of(10, 30, 45));
            LocalTime min = LocalTime.of(9, 0);
            LocalTime max = LocalTime.of(17, 30);

            dialog.getPicker().setUse24HourClock(true);
            dialog.getPicker().setMinuteStep(15);
            dialog.getPicker().setMinTime(min);
            dialog.getPicker().setMaxTime(max);

            assertEquals(LocalTime.of(10, 30), dialog.getValue());
            assertTrue(dialog.getPicker().isUse24HourClock());
            assertEquals(15, dialog.getPicker().getMinuteStep());
            assertEquals(min, dialog.getPicker().getMinTime());
            assertEquals(max, dialog.getPicker().getMaxTime());
            assertTrue(dialog.getPicker().isTimeDisabled(LocalTime.of(18, 0)));
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

    /// Verifies time preset factory normalization, failure paths, and immutable common lists.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void timePresetFactoriesValidateInputsNormalizePrecisionAndReturnImmutableLists() {
        LocalTime anchor = LocalTime.of(10, 30, 45, 123_000_000);

        assertEquals(LocalTime.of(10, 30), M3TimePresets.minutesFrom(anchor, 0).time());
        assertEquals(LocalTime.of(10, 45), M3TimePresets.minutesFrom(anchor, 15).time());
        assertThrows(NullPointerException.class, () -> M3TimePresets.now(null));
        assertThrows(NullPointerException.class, () -> M3TimePresets.minutesFrom(null, 15));
        assertThrows(NullPointerException.class, () -> M3TimePresets.common(null));
        assertThrows(UnsupportedOperationException.class,
                () -> M3TimePresets.common(anchor).add(M3TimePresets.noon()));
    }

    /// Verifies time dialog preset actions update the selected time.
    @Test
    void timePickerDialogAppliesPresetActions() {
        FxTestUtils.runOnFxThread(() -> {
            LocalTime anchor = LocalTime.of(10, 30);
            M3TimePickerDialog dialog = new M3TimePickerDialog();
            M3DialogPane pane = dialog.getDialogPane();

            applyCss(pane);
            HBox content = assertInstanceOf(HBox.class, pane.getContent());
            Node pickerParent = dialog.getPicker().getParent();
            assertFalse(content.getChildren().get(0).isManaged());

            dialog.getPresets().add(M3TimePresets.now(anchor));
            dialog.getPresets().addAll(
                    M3TimePresets.minutesFrom(anchor, 15),
                    M3TimePresets.morning(),
                    M3TimePresets.noon(),
                    M3TimePresets.evening()
            );
            applyCss(pane);

            assertSame(content, pane.getContent());
            assertSame(pickerParent, dialog.getPicker().getParent());
            assertEquals(5, dialog.getPresets().size());
            assertEquals(5, pane.lookupAll("." + M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS).size());

            presetButton(pane, M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS, "In 15 min").fire();

            assertEquals(LocalTime.of(10, 45), dialog.getValue());
            assertFalse(Objects.requireNonNull(pane.getDefaultAction(), "default action").isDisabled());

            M3TimePreset custom = new M3TimePreset("Release", LocalTime.of(16, 30));
            dialog.getPicker().applyPreset(custom);

            assertEquals(custom.time(), dialog.getValue());

            dialog.getPresets().clear();

            assertSame(content, pane.getContent());
            assertSame(pickerParent, dialog.getPicker().getParent());
            assertFalse(content.getChildren().get(0).isManaged());
            assertTrue(pane.lookupAll("." + M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS).isEmpty());
        });
    }

    /// Verifies that picker dialog preset lists reject null mutations without partial insertion.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void pickerDialogPresetListsRejectNullElements() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate dateAnchor = LocalDate.of(2026, 5, 19);
            M3DatePickerDialog dateDialog = new M3DatePickerDialog();
            M3DatePreset datePreset = M3DatePresets.today(dateAnchor);

            assertThrows(NullPointerException.class, () -> dateDialog.getPresets().add(null));
            assertThrows(NullPointerException.class, () -> dateDialog.getPresets().addAll(datePreset, null));
            assertTrue(dateDialog.getPresets().isEmpty());

            M3DateRangePickerDialog rangeDialog = new M3DateRangePickerDialog();
            M3DateRangePreset rangePreset = M3DateRangePresets.today(dateAnchor);

            assertThrows(NullPointerException.class, () -> rangeDialog.getPresets().add(null));
            assertThrows(NullPointerException.class, () -> rangeDialog.getPresets().addAll(rangePreset, null));
            assertTrue(rangeDialog.getPresets().isEmpty());

            LocalTime timeAnchor = LocalTime.of(10, 30);
            M3TimePickerDialog timeDialog = new M3TimePickerDialog();
            M3TimePreset timePreset = M3TimePresets.now(timeAnchor);

            assertThrows(NullPointerException.class, () -> timeDialog.getPresets().add(null));
            assertThrows(NullPointerException.class, () -> timeDialog.getPresets().addAll(timePreset, null));
            assertTrue(timeDialog.getPresets().isEmpty());
        });
    }

    /// Verifies that dialog preset grids and the vertical time column support keyboard traversal.
    @Test
    void pickerDialogPresetKeyboardNavigationMovesWithinListsAndGrid() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate dateAnchor = LocalDate.of(2026, 5, 19);
            LocalTime timeAnchor = LocalTime.of(10, 30);
            M3DatePickerDialog dateDialog = new M3DatePickerDialog(dateAnchor);
            M3DateRangePickerDialog rangeDialog = new M3DateRangePickerDialog(
                    dateAnchor,
                    dateAnchor.plusDays(6)
            );
            M3TimePickerDialog timeDialog = new M3TimePickerDialog(timeAnchor);
            dateDialog.getPresets().setAll(M3DatePresets.common(dateAnchor));
            rangeDialog.getPresets().setAll(M3DateRangePresets.common(dateAnchor, rangeDialog.getPicker().getFirstDayOfWeek()));
            timeDialog.getPresets().setAll(M3TimePresets.common(timeAnchor));

            M3DialogPane datePane = dateDialog.getDialogPane();
            M3DialogPane rangePane = rangeDialog.getDialogPane();
            M3DialogPane timePane = timeDialog.getDialogPane();
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
                M3Button morning = presetButton(
                        timePane,
                        M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS,
                        "Morning"
                );
                M3Button evening = presetButton(timePane, M3TimePickerDialog.PRESET_BUTTON_STYLE_CLASS, "Evening");

                now.requestFocus();
                now.fireEvent(keyPressed(KeyCode.DOWN));

                assertTrue(inFifteenMinutes.isFocused());

                inFifteenMinutes.fireEvent(keyPressed(KeyCode.END));

                assertTrue(evening.isFocused());

                evening.fireEvent(keyPressed(KeyCode.PAGE_UP));

                assertTrue(now.isFocused());

                morning.requestFocus();
                morning.fireEvent(keyPressed(KeyCode.RIGHT));

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
