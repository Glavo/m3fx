// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3LocalizedObjectProperty;
import org.glavo.m3fx.localization.M3Localizer;
import org.glavo.m3fx.skins.M3DatePickerSkin;
import org.glavo.m3fx.skins.M3DateRangePickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests inherited calendar localization, explicit overrides, and observer disposal for both calendar controls.
@NotNullByDefault
final class M3CalendarLocalizationTest {
    /// The selected date shared by the test calendars.
    private static final LocalDate DATE = LocalDate.of(2026, 5, 18);

    /// Starts JavaFX before controls are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies exact property notifications and getter agreement without installing a skin.
    @Test
    void firstWeekdayPropertyTracksLocalizationWithoutSkin() {
        FxTestUtils.runOnFxThread(() -> {
            for (Control picker : createPickers()) {
                Pane root = new Pane(picker);
                M3Localizer american = new M3Localizer(Locale.US);
                M3Localizer french = new M3Localizer(Locale.FRANCE);
                ObjectProperty<DayOfWeek> property = firstWeekdayProperty(picker);
                assertSame(M3Localizer.getEffectiveLocalizer(picker).getFirstDayOfWeek(), property.get());
                assertSame(property.get(), firstWeekday(picker));
                assertNull(picker.getSkin());
                M3Localizer.install(root, american);
                assertEquals(DayOfWeek.SUNDAY, property.get());
                List<DayOfWeek> changes = new ArrayList<>();
                List<DayOfWeek> getterValues = new ArrayList<>();
                property.addListener((observable, oldValue, newValue) -> {
                    assertSame(property, observable);
                    changes.add(newValue);
                    getterValues.add(firstWeekday(picker));
                });
                M3Localizer.install(root, french);
                M3Localizer.install(root, french);
                M3Localizer.install(root, new MarkedLocalizer(Locale.FRANCE));
                assertEquals(List.of(DayOfWeek.MONDAY), changes);
                M3Localizer.install(picker, american);
                M3Localizer.uninstall(picker);
                assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY), changes);
                assertEquals(changes, getterValues);
                assertSame(property.get(), firstWeekday(picker));
                root.getChildren().remove(picker);
                assertEquals(1, root.getProperties().size(), "detaching must release the ancestor context");
                Pane newRoot = new Pane();
                M3Localizer.install(newRoot, american);
                newRoot.getChildren().add(picker);
                assertEquals(DayOfWeek.SUNDAY, property.get());
                assertSame(property.get(), firstWeekday(picker));
            }
        });
    }

    /// Verifies that both setter and direct same-value assignments prevent future default updates.
    @Test
    void explicitSameWeekdayKeepsApplicationOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            for (boolean useSetter : List.of(false, true)) {
                for (Control picker : createPickers()) {
                    M3Localizer.install(picker, new M3Localizer(Locale.US));
                    ObjectProperty<DayOfWeek> property = firstWeekdayProperty(picker);
                    if (!useSetter) {
                        property.set(property.get());
                    } else if (picker instanceof M3DatePicker single) {
                        single.setFirstDayOfWeek(single.getFirstDayOfWeek());
                    } else {
                        M3DateRangePicker range = (M3DateRangePicker) picker;
                        range.setFirstDayOfWeek(range.getFirstDayOfWeek());
                    }
                    List<DayOfWeek> changes = new ArrayList<>();
                    property.addListener((observable, oldValue, newValue) -> changes.add(newValue));
                    M3Localizer.install(picker, new M3Localizer(Locale.FRANCE));
                    assertEquals(DayOfWeek.SUNDAY, property.get());
                    assertSame(property.get(), firstWeekday(picker));
                    assertTrue(changes.isEmpty());
                }
            }
        });
    }

    /// Verifies same-value binding ownership and retention of the last value after unbinding.
    @Test
    void boundFirstWeekdayRetainsOwnershipAfterUnbinding() {
        FxTestUtils.runOnFxThread(() -> {
            for (Control picker : createPickers()) {
                M3Localizer.install(picker, new M3Localizer(Locale.US));
                ObjectProperty<DayOfWeek> property = firstWeekdayProperty(picker);
                ObjectProperty<DayOfWeek> source = new SimpleObjectProperty<>(property.get());
                property.bind(source);
                M3Localizer.install(picker, new M3Localizer(Locale.FRANCE));
                assertTrue(property.isBound());
                assertEquals(DayOfWeek.SUNDAY, property.get());
                assertThrows(RuntimeException.class, () -> property.set(DayOfWeek.MONDAY));
                source.set(DayOfWeek.FRIDAY);
                assertEquals(DayOfWeek.FRIDAY, firstWeekday(picker));
                property.unbind();
                assertFalse(property.isBound());
                M3Localizer.install(picker, new M3Localizer(Locale.US));
                assertEquals(DayOfWeek.FRIDAY, property.get());
                assertSame(property.get(), firstWeekday(picker));
            }
        });
    }

    /// Rejects null assignments and initial bindings without disabling localized defaults.
    @Test
    void firstWeekdayRejectsNullWithoutTakingOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            for (Control picker : createPickers()) {
                M3Localizer.install(picker, new M3Localizer(Locale.US));
                ObjectProperty<DayOfWeek> property = firstWeekdayProperty(picker);
                assertThrows(NullPointerException.class, () -> property.set(null));
                assertThrows(NullPointerException.class, () -> property.bind(new SimpleObjectProperty<>()));
                if (picker instanceof M3DatePicker single) {
                    assertThrows(NullPointerException.class, () -> single.setFirstDayOfWeek(null));
                } else {
                    M3DateRangePicker range = (M3DateRangePicker) picker;
                    assertThrows(NullPointerException.class, () -> range.setFirstDayOfWeek(null));
                }
                M3Localizer.install(picker, new M3Localizer(Locale.FRANCE));
                assertEquals(DayOfWeek.MONDAY, property.get());
                assertSame(property.get(), firstWeekday(picker));
            }
        });
    }

    /// Rejects a binding that becomes null when its value is read.
    @Test
    void localizedObjectPropertyRejectsNullBoundReads() {
        M3LocalizedObjectProperty<DayOfWeek> property =
                new M3LocalizedObjectProperty<>(this, "firstDayOfWeek", DayOfWeek.SUNDAY);
        ObjectProperty<DayOfWeek> source = new SimpleObjectProperty<>(DayOfWeek.MONDAY);
        property.bind(source);
        source.set(null);
        assertThrows(NullPointerException.class, property::get);
        source.set(DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.FRIDAY, property.get());
        assertThrows(NullPointerException.class, () -> property.updateDefault(null));
        property.unbind();
    }

    /// Verifies text, grid order, accessibility, and node reuse on installation, replacement, and reparenting.
    @Test
    void skinsRefreshInheritedAndSameLocaleReplacements() {
        FxTestUtils.runOnFxThread(() -> {
            for (Control picker : createPickers()) {
                Pane root = new Pane(picker);
                M3Localizer american = new M3Localizer(Locale.US);
                M3Localizer french = new M3Localizer(Locale.FRANCE);
                M3Localizer.install(root, american);
                installSkin(picker);
                GridPane grid = assertInstanceOf(GridPane.class, picker.lookup(".m3-date-picker-day-grid"));
                @Unmodifiable List<Node> cells = List.copyOf(grid.getChildren());
                assertCalendarText(picker, american);

                M3Localizer.install(root, french);
                assertCalendarText(picker, french);
                M3Localizer replacement = new MarkedLocalizer(Locale.FRANCE);
                M3Localizer.install(root, replacement);
                assertCalendarText(picker, replacement);
                Pane newRoot = new Pane();
                M3Localizer.install(newRoot, french);
                root.getChildren().remove(picker);
                newRoot.getChildren().add(picker);
                assertCalendarText(picker, french);
                M3Localizer.install(picker, replacement);
                M3Localizer.install(newRoot, american);
                assertCalendarText(picker, replacement);
                M3Localizer.uninstall(picker);
                assertCalendarText(picker, american);
                firstWeekdayProperty(picker).set(DayOfWeek.SATURDAY);
                M3Localizer.install(newRoot, french);
                assertEquals(DayOfWeek.SATURDAY, firstWeekday(picker));
                assertCalendarText(picker, french);
                M3Localizer.install(newRoot, american);
                assertCalendarText(picker, american);
                for (int index = 0; index < cells.size(); index++) {
                    assertSame(cells.get(index), grid.getChildren().get(index));
                }
                if (picker instanceof M3DatePicker single) {
                    assertEquals(DATE, single.getValue());
                    single.setValue(null);
                } else {
                    M3DateRangePicker range = (M3DateRangePicker) picker;
                    assertEquals(DATE, range.getStartDate());
                    assertEquals(DATE, range.getEndDate());
                    range.setEndDate(null);
                    assertEquals(american.formatDate(DATE),
                            range.queryAccessibleAttribute(AccessibleAttribute.TEXT));
                    range.clearRange();
                }
                assertEquals(american.formatMonth(Month.MAY, TextStyle.FULL_STANDALONE) + " "
                                + american.formatYear(2026),
                        picker.queryAccessibleAttribute(AccessibleAttribute.TEXT));
                picker.setSkin(null);
            }
        });
    }

    /// Verifies that disposed skin and header nodes no longer react to localization changes.
    @Test
    void disposedSkinsStopRefreshingCalendarAndHeader() {
        FxTestUtils.runOnFxThread(() -> {
            for (Control picker : createPickers()) {
                Pane root = new Pane(picker);
                M3Localizer american = new M3Localizer(Locale.US);
                M3Localizer.install(root, american);
                installSkin(picker);
                M3MenuButton month = assertInstanceOf(M3MenuButton.class,
                        picker.lookup(".m3-date-picker-month-menu-button"));
                ButtonBase cell = assertInstanceOf(ButtonBase.class, picker.lookup(".m3-date-picker-day-cell"));
                String monthText = month.getText();
                String dateText = cell.getAccessibleText();
                picker.setSkin(null);
                M3Localizer.install(root, new MarkedLocalizer(Locale.FRANCE));
                assertEquals(DayOfWeek.MONDAY, firstWeekdayProperty(picker).get());
                assertSame(firstWeekdayProperty(picker).get(), firstWeekday(picker));
                assertEquals(monthText, month.getText());
                assertEquals(dateText, cell.getAccessibleText());
                installSkin(picker);
                assertCalendarText(picker, M3Localizer.getEffectiveLocalizer(picker));
                picker.setSkin(null);
            }
        });
    }

    /// Creates both calendar variants with the same selected date and displayed month.
    private static @Unmodifiable List<Control> createPickers() {
        return List.of(new M3DatePicker(DATE), new M3DateRangePicker(DATE, DATE));
    }

    /// Installs the corresponding calendar skin without depending on a CSS pulse.
    private static void installSkin(Control picker) {
        if (picker instanceof M3DatePicker single) {
            single.setSkin(new M3DatePickerSkin(single));
        } else {
            M3DateRangePicker range = (M3DateRangePicker) picker;
            range.setSkin(new M3DateRangePickerSkin(range));
        }
    }

    /// Returns either calendar's non-null first-weekday property.
    private static ObjectProperty<DayOfWeek> firstWeekdayProperty(Control picker) {
        return picker instanceof M3DatePicker single ? single.firstDayOfWeekProperty()
                : ((M3DateRangePicker) picker).firstDayOfWeekProperty();
    }

    /// Returns either calendar's effective first weekday.
    private static DayOfWeek firstWeekday(Control picker) {
        return picker instanceof M3DatePicker single ? single.getFirstDayOfWeek()
                : ((M3DateRangePicker) picker).getFirstDayOfWeek();
    }

    /// Checks displayed labels, date mappings, menu entries, and accessible descriptions against the localizer.
    private static void assertCalendarText(Control picker, M3Localizer localizer) {
        M3MenuButton month = assertInstanceOf(M3MenuButton.class,
                picker.lookup(".m3-date-picker-month-menu-button"));
        M3MenuButton year = assertInstanceOf(M3MenuButton.class,
                picker.lookup(".m3-date-picker-year-menu-button"));
        assertEquals(localizer.formatMonth(Month.MAY, TextStyle.SHORT_STANDALONE), month.getText());
        assertEquals(localizer.formatYear(2026), year.getText());
        assertEquals(localizer.getChooseMonthText() + ": " + month.getText(), month.getAccessibleText());
        assertEquals(localizer.getChooseYearText() + ": " + year.getText(), year.getAccessibleText());
        for (int index = 0; index < 12; index++) {
            M3MenuItem item = assertInstanceOf(M3MenuItem.class, month.getItems().get(index));
            assertEquals(localizer.formatMonth(Month.of(index + 1), TextStyle.FULL_STANDALONE),
                    item.getHeadlineText());
        }
        for (var entry : year.getItems()) {
            M3MenuItem item = assertInstanceOf(M3MenuItem.class, entry);
            assertEquals(localizer.formatYear(assertInstanceOf(Integer.class, item.getUserData())),
                    item.getHeadlineText());
        }
        @Unmodifiable List<String> navigationText = picker.lookupAll(".m3-date-picker-navigation-button").stream()
                .map(Node::getAccessibleText).toList();
        assertTrue(navigationText.containsAll(List.of(localizer.getPreviousMonthText(), localizer.getNextMonthText(),
                localizer.getPreviousYearText(), localizer.getNextYearText())));
        HBox weekdays = assertInstanceOf(HBox.class, picker.lookup(".m3-date-picker-weekday-row"));
        for (int column = 0; column < 7; column++) {
            Label label = assertInstanceOf(Label.class, weekdays.getChildren().get(column));
            DayOfWeek day = firstWeekday(picker).plus(column);
            assertEquals(localizer.formatWeekday(day, TextStyle.SHORT_STANDALONE), label.getText());
            assertEquals(localizer.formatWeekday(day, TextStyle.FULL_STANDALONE), label.getAccessibleText());
        }
        GridPane grid = assertInstanceOf(GridPane.class, picker.lookup(".m3-date-picker-day-grid"));
        LocalDate first = YearMonth.from(DATE).atDay(1);
        LocalDate gridStart = first.minusDays(Math.floorMod(
                first.getDayOfWeek().getValue() - firstWeekday(picker).getValue(), 7));
        for (int index = 0; index < 42; index++) {
            ButtonBase cell = assertInstanceOf(ButtonBase.class, grid.getChildren().get(index));
            LocalDate date = gridStart.plusDays(index);
            assertEquals(date, cell.getUserData());
            assertEquals(localizer.formatDayOfMonth(date), cell.getText());
            assertEquals(localizer.formatDate(date), cell.getAccessibleText());
        }
        String selectedText = localizer.formatDate(DATE);
        assertEquals(picker instanceof M3DatePicker ? selectedText : selectedText + " / " + selectedText,
                picker.queryAccessibleAttribute(AccessibleAttribute.TEXT));
    }

    /// Changes date labels independently of locale to expose identity-based refresh regressions.
    @NotNullByDefault
    private static final class MarkedLocalizer extends M3Localizer {
        /// Creates a distinct localizer using an existing regional format.
        private MarkedLocalizer(Locale locale) {
            super(locale);
        }

        /// Adds a marker to month labels and menu entries.
        @Override
        public String formatMonth(Month month, TextStyle style) {
            return "month:" + super.formatMonth(month, style);
        }

        /// Adds a marker to weekday labels.
        @Override
        public String formatWeekday(DayOfWeek day, TextStyle style) {
            return "weekday:" + super.formatWeekday(day, style);
        }

        /// Adds a marker to year labels and menu entries.
        @Override
        public String formatYear(int year) {
            return "year:" + super.formatYear(year);
        }

        /// Adds a marker to visible day labels.
        @Override
        public String formatDayOfMonth(LocalDate date) {
            return "day:" + super.formatDayOfMonth(date);
        }

        /// Adds a marker to complete accessible dates.
        @Override
        public String formatDate(LocalDate date) {
            return "date:" + super.formatDate(date);
        }
    }
}
