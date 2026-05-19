// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3DatePickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;

/// A Material Design 3 calendar date picker control.
@NotNullByDefault
public class M3DatePicker extends Control {
    /// The base style class for M3FX date pickers.
    public static final String STYLE_CLASS = "m3-date-picker";

    /// The style class applied to the internal layout container.
    public static final String CONTAINER_STYLE_CLASS = "m3-date-picker-container";

    /// The style class applied to the header row.
    public static final String HEADER_STYLE_CLASS = "m3-date-picker-header";

    /// The style class applied to the displayed month label.
    public static final String MONTH_LABEL_STYLE_CLASS = "m3-date-picker-month-label";

    /// The style class applied to previous and next month buttons.
    public static final String NAVIGATION_BUTTON_STYLE_CLASS = "m3-date-picker-navigation-button";

    /// The style class applied to the weekday label row.
    public static final String WEEKDAY_ROW_STYLE_CLASS = "m3-date-picker-weekday-row";

    /// The style class applied to weekday labels.
    public static final String WEEKDAY_LABEL_STYLE_CLASS = "m3-date-picker-weekday-label";

    /// The style class applied to the day grid.
    public static final String DAY_GRID_STYLE_CLASS = "m3-date-picker-day-grid";

    /// The style class applied to every day cell button.
    public static final String DAY_CELL_STYLE_CLASS = "m3-date-picker-day-cell";

    /// The style class applied to visible days outside the displayed month.
    public static final String OUTSIDE_MONTH_DAY_STYLE_CLASS = "m3-date-picker-outside-month-day";

    /// The style class applied to today's day cell.
    public static final String TODAY_DAY_STYLE_CLASS = "m3-date-picker-today-day";

    /// The style class applied to the selected day cell.
    public static final String SELECTED_DAY_STYLE_CLASS = "m3-date-picker-selected-day";

    /// The selected date, or `null` when no date is selected.
    private final ObjectProperty<@Nullable LocalDate> value =
            new SimpleObjectProperty<>(this, "value") {
                /// Keeps the visible month aligned with a non-null selected date.
                @Override
                protected void invalidated() {
                    @Nullable LocalDate selectedDate = get();
                    if (selectedDate != null) {
                        showMonth(YearMonth.from(selectedDate));
                    }
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                }
            };

    /// The month currently displayed in the calendar grid.
    private final ObjectProperty<YearMonth> displayedMonth =
            new SimpleObjectProperty<>(this, "displayedMonth", YearMonth.now()) {
                /// Restores a valid month when callers set the property directly to `null`.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(YearMonth.now());
                        return;
                    }
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                }
            };

    /// The weekday that appears in the first calendar column.
    private final ObjectProperty<DayOfWeek> firstDayOfWeek =
            new SimpleObjectProperty<>(this, "firstDayOfWeek", defaultFirstDayOfWeek()) {
                /// Restores the locale default when callers set the property directly to `null`.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(defaultFirstDayOfWeek());
                    }
                }
            };

    /// The earliest selectable date, or `null` when there is no lower bound.
    private final ObjectProperty<@Nullable LocalDate> minDate =
            new SimpleObjectProperty<>(this, "minDate") {
                /// Clears the selected date when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                }
            };

    /// The latest selectable date, or `null` when there is no upper bound.
    private final ObjectProperty<@Nullable LocalDate> maxDate =
            new SimpleObjectProperty<>(this, "maxDate") {
                /// Clears the selected date when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                }
            };

    /// Whether days from adjacent months are shown in leading and trailing grid cells.
    private final BooleanProperty showAdjacentMonthDays =
            new SimpleBooleanProperty(this, "showAdjacentMonthDays", true);

    /// Creates a date picker showing the current month with no selected date.
    public M3DatePicker() {
        initialize();
    }

    /// Creates a date picker initialized with a selected date.
    public M3DatePicker(LocalDate value) {
        initialize();
        setValue(value);
    }

    /// Returns the selected date, or `null` when no date is selected.
    public final @Nullable LocalDate getValue() {
        return value.get();
    }

    /// Sets the selected date, or clears selection when `null` is supplied.
    public final void setValue(@Nullable LocalDate value) {
        if (value != null && isDateDisabled(value)) {
            throw new IllegalArgumentException("value is outside the selectable range");
        }
        this.value.set(value);
    }

    /// Returns the selected date property.
    public final ObjectProperty<@Nullable LocalDate> valueProperty() {
        return value;
    }

    /// Returns the month currently displayed by the calendar grid.
    public final YearMonth getDisplayedMonth() {
        return displayedMonth.get();
    }

    /// Sets the month displayed by the calendar grid.
    public final void setDisplayedMonth(YearMonth displayedMonth) {
        this.displayedMonth.set(Objects.requireNonNull(displayedMonth, "displayedMonth"));
    }

    /// Returns the displayed month property.
    public final ObjectProperty<YearMonth> displayedMonthProperty() {
        return displayedMonth;
    }

    /// Returns the weekday shown in the first calendar column.
    public final DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek.get();
    }

    /// Sets the weekday shown in the first calendar column.
    public final void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        this.firstDayOfWeek.set(Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek"));
    }

    /// Returns the first day of week property.
    public final ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return firstDayOfWeek;
    }

    /// Returns the earliest selectable date, or `null` when there is no lower bound.
    public final @Nullable LocalDate getMinDate() {
        return minDate.get();
    }

    /// Sets the earliest selectable date, or clears the lower bound when `null` is supplied.
    public final void setMinDate(@Nullable LocalDate minDate) {
        validateDateRange(minDate, getMaxDate());
        this.minDate.set(minDate);
    }

    /// Returns the minimum date property.
    public final ObjectProperty<@Nullable LocalDate> minDateProperty() {
        return minDate;
    }

    /// Returns the latest selectable date, or `null` when there is no upper bound.
    public final @Nullable LocalDate getMaxDate() {
        return maxDate.get();
    }

    /// Sets the latest selectable date, or clears the upper bound when `null` is supplied.
    public final void setMaxDate(@Nullable LocalDate maxDate) {
        validateDateRange(getMinDate(), maxDate);
        this.maxDate.set(maxDate);
    }

    /// Returns the maximum date property.
    public final ObjectProperty<@Nullable LocalDate> maxDateProperty() {
        return maxDate;
    }

    /// Returns whether adjacent-month days are visible in leading and trailing grid cells.
    public final boolean isShowAdjacentMonthDays() {
        return showAdjacentMonthDays.get();
    }

    /// Sets whether adjacent-month days are visible in leading and trailing grid cells.
    public final void setShowAdjacentMonthDays(boolean showAdjacentMonthDays) {
        this.showAdjacentMonthDays.set(showAdjacentMonthDays);
    }

    /// Returns the adjacent-month visibility property.
    public final BooleanProperty showAdjacentMonthDaysProperty() {
        return showAdjacentMonthDays;
    }

    /// Selects a date if it is inside the configured range.
    public final void selectDate(LocalDate date) {
        setValue(Objects.requireNonNull(date, "date"));
    }

    /// Selects today's date when it is inside the configured range.
    public final void selectToday() {
        selectDate(LocalDate.now());
    }

    /// Clears the selected date.
    public final void clearValue() {
        setValue(null);
    }

    /// Shows the month before the current displayed month.
    public final void showPreviousMonth() {
        showMonth(getDisplayedMonth().minusMonths(1));
    }

    /// Shows the month after the current displayed month.
    public final void showNextMonth() {
        showMonth(getDisplayedMonth().plusMonths(1));
    }

    /// Shows the supplied month without changing the selected date.
    public final void showMonth(YearMonth month) {
        setDisplayedMonth(month);
    }

    /// Shows the month containing today's date without changing the selected date.
    public final void showToday() {
        showMonth(YearMonth.from(LocalDate.now()));
    }

    /// Returns whether the supplied date is outside the configured selectable range.
    public final boolean isDateDisabled(LocalDate date) {
        Objects.requireNonNull(date, "date");
        @Nullable LocalDate min = getMinDate();
        @Nullable LocalDate max = getMaxDate();
        return min != null && date.isBefore(min) || max != null && date.isAfter(max);
    }

    /// Returns the user-agent stylesheet for M3FX date pickers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("date-picker.css");
    }

    /// Returns accessibility text for the selected date or displayed month.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates the default Material Design 3 date picker skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DatePickerSkin(this);
    }

    /// Adds base style classes, accessibility role, and keyboard navigation.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
    }

    /// Handles keyboard date navigation.
    private void handleNavigationKeyPressed(KeyEvent event) {
        boolean handled = switch (event.getCode()) {
            case LEFT -> {
                moveSelectionByDays(-1);
                yield true;
            }
            case RIGHT -> {
                moveSelectionByDays(1);
                yield true;
            }
            case UP -> {
                moveSelectionByDays(-7);
                yield true;
            }
            case DOWN -> {
                moveSelectionByDays(7);
                yield true;
            }
            case PAGE_UP -> {
                moveSelectionByMonths(-1);
                yield true;
            }
            case PAGE_DOWN -> {
                moveSelectionByMonths(1);
                yield true;
            }
            case HOME -> {
                selectKeyboardDate(getDisplayedMonth().atDay(1));
                yield true;
            }
            case END -> {
                selectKeyboardDate(getDisplayedMonth().atEndOfMonth());
                yield true;
            }
            default -> false;
        };

        if (handled) {
            event.consume();
        }
    }

    /// Moves keyboard selection by a number of days.
    private void moveSelectionByDays(int days) {
        selectKeyboardDate(navigationBaseDate().plusDays(days));
    }

    /// Moves keyboard selection by a number of months.
    private void moveSelectionByMonths(int months) {
        @Nullable LocalDate selectedDate = getValue();
        if (selectedDate == null) {
            showMonth(getDisplayedMonth().plusMonths(months));
        } else {
            selectKeyboardDate(selectedDate.plusMonths(months));
        }
    }

    /// Selects a keyboard navigation target when it is enabled.
    private void selectKeyboardDate(LocalDate date) {
        showMonth(YearMonth.from(date));
        if (!isDateDisabled(date)) {
            setValue(date);
        }
    }

    /// Returns the base date used when keyboard navigation starts without a selected date.
    private LocalDate navigationBaseDate() {
        @Nullable LocalDate selectedDate = getValue();
        if (selectedDate != null) {
            return selectedDate;
        }

        YearMonth month = getDisplayedMonth();
        LocalDate today = LocalDate.now();
        return YearMonth.from(today).equals(month) ? today : month.atDay(1);
    }

    /// Clears the selected date when min or max bounds exclude it.
    private void clearValueIfOutOfRange() {
        @Nullable LocalDate selectedDate = getValue();
        if (selectedDate != null && isDateDisabled(selectedDate)) {
            value.set(null);
        }
    }

    /// Returns accessible text for the selected date or current month.
    private String accessibleText() {
        @Nullable LocalDate selectedDate = getValue();
        return selectedDate == null ? getDisplayedMonth().toString() : selectedDate.toString();
    }

    /// Validates an optional inclusive date range.
    private static void validateDateRange(@Nullable LocalDate minDate, @Nullable LocalDate maxDate) {
        if (minDate != null && maxDate != null && minDate.isAfter(maxDate)) {
            throw new IllegalArgumentException("minDate must not be after maxDate");
        }
    }

    /// Returns the locale's default first day of week.
    private static DayOfWeek defaultFirstDayOfWeek() {
        return WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    }
}
