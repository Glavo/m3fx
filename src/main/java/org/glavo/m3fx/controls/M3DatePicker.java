// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/// A Material Design 3 calendar date picker control.
///
/// `M3DatePicker` displays a month grid, weekday labels, previous and next month navigation, locale-aware first
/// day of week, disabled-day predicates, and a nullable selected [LocalDate] value. The control is the calendar
/// body used by [M3DatePickerDialog] and [M3DatePickerField].
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
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

    // The selected date, or `null` when no date is selected.
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
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
                }
            };

    // The month currently displayed in the calendar grid.
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
                    notifyAccessibleItemsChanged();
                }
            };

    // The weekday that appears in the first calendar column.
    private final ObjectProperty<DayOfWeek> firstDayOfWeek =
            new SimpleObjectProperty<>(this, "firstDayOfWeek", defaultFirstDayOfWeek()) {
                /// Restores the locale default when callers set the property directly to `null`.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(defaultFirstDayOfWeek());
                    }
                    notifyAccessibleItemsChanged();
                }
            };

    // The earliest selectable date, or `null` when there is no lower bound.
    private final ObjectProperty<@Nullable LocalDate> minDate =
            new SimpleObjectProperty<>(this, "minDate") {
                /// Clears the selected date when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                    notifyAccessibleItemsChanged();
                }
            };

    // The latest selectable date, or `null` when there is no upper bound.
    private final ObjectProperty<@Nullable LocalDate> maxDate =
            new SimpleObjectProperty<>(this, "maxDate") {
                /// Clears the selected date when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                    notifyAccessibleItemsChanged();
                }
            };

    // Whether days from adjacent months are shown in leading and trailing grid cells.
    private final BooleanProperty showAdjacentMonthDays =
            new SimpleBooleanProperty(this, "showAdjacentMonthDays", true) {
                /// Notifies accessibility clients when visible day cells change.
                @Override
                protected void invalidated() {
                    notifyAccessibleItemsChanged();
                }
            };

    /// Creates a date picker showing the current month with no selected date.
    public M3DatePicker() {
        initialize();
    }

    /// Creates a date picker initialized with a selected date.
    ///
    /// @param value the initially selected date
    public M3DatePicker(LocalDate value) {
        initialize();
        setValue(value);
    }

    /// Returns the selected date, or `null` when no date is selected.
    ///
    /// @return the selected date, or `null` when no date is selected
    public final @Nullable LocalDate getValue() {
        return value.get();
    }

    /// Sets the selected date, or clears selection when `null` is supplied.
    ///
    /// @param value the selected date, or `null` to clear selection
    public final void setValue(@Nullable LocalDate value) {
        if (value != null && isDateDisabled(value)) {
            throw new IllegalArgumentException("value is outside the selectable range");
        }
        this.value.set(value);
    }

    /// Returns the selected date property.
    ///
    /// @return the selected date property
    public final ObjectProperty<@Nullable LocalDate> valueProperty() {
        return value;
    }

    /// Returns the month currently displayed by the calendar grid.
    ///
    /// @return the displayed calendar month
    public final YearMonth getDisplayedMonth() {
        return displayedMonth.get();
    }

    /// Sets the month displayed by the calendar grid.
    ///
    /// @param displayedMonth the month displayed by the calendar grid
    public final void setDisplayedMonth(YearMonth displayedMonth) {
        this.displayedMonth.set(Objects.requireNonNull(displayedMonth, "displayedMonth"));
    }

    /// Returns the displayed month property.
    ///
    /// @return the displayed month property
    public final ObjectProperty<YearMonth> displayedMonthProperty() {
        return displayedMonth;
    }

    /// Returns the weekday shown in the first calendar column.
    ///
    /// @return the weekday shown in the first calendar column
    public final DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek.get();
    }

    /// Sets the weekday shown in the first calendar column.
    ///
    /// @param firstDayOfWeek the weekday shown in the first calendar column
    public final void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        this.firstDayOfWeek.set(Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek"));
    }

    /// Returns the first day of week property.
    ///
    /// @return the first day of week property
    public final ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return firstDayOfWeek;
    }

    /// Returns the earliest selectable date, or `null` when there is no lower bound.
    ///
    /// @return the earliest selectable date, or `null` when there is no lower bound
    public final @Nullable LocalDate getMinDate() {
        return minDate.get();
    }

    /// Sets the earliest selectable date, or clears the lower bound when `null` is supplied.
    ///
    /// @param minDate the earliest selectable date, or `null` to clear the lower bound
    public final void setMinDate(@Nullable LocalDate minDate) {
        validateDateRange(minDate, getMaxDate());
        this.minDate.set(minDate);
    }

    /// Returns the minimum date property.
    ///
    /// @return the minimum date property
    public final ObjectProperty<@Nullable LocalDate> minDateProperty() {
        return minDate;
    }

    /// Returns the latest selectable date, or `null` when there is no upper bound.
    ///
    /// @return the latest selectable date, or `null` when there is no upper bound
    public final @Nullable LocalDate getMaxDate() {
        return maxDate.get();
    }

    /// Sets the latest selectable date, or clears the upper bound when `null` is supplied.
    ///
    /// @param maxDate the latest selectable date, or `null` to clear the upper bound
    public final void setMaxDate(@Nullable LocalDate maxDate) {
        validateDateRange(getMinDate(), maxDate);
        this.maxDate.set(maxDate);
    }

    /// Returns the maximum date property.
    ///
    /// @return the maximum date property
    public final ObjectProperty<@Nullable LocalDate> maxDateProperty() {
        return maxDate;
    }

    /// Returns whether adjacent-month days are visible in leading and trailing grid cells.
    ///
    /// @return `true` when adjacent-month days are visible
    public final boolean isShowAdjacentMonthDays() {
        return showAdjacentMonthDays.get();
    }

    /// Sets whether adjacent-month days are visible in leading and trailing grid cells.
    ///
    /// @param showAdjacentMonthDays whether adjacent-month days should be visible
    public final void setShowAdjacentMonthDays(boolean showAdjacentMonthDays) {
        this.showAdjacentMonthDays.set(showAdjacentMonthDays);
    }

    /// Returns the adjacent-month visibility property.
    ///
    /// @return the adjacent-month visibility property
    public final BooleanProperty showAdjacentMonthDaysProperty() {
        return showAdjacentMonthDays;
    }

    /// Selects a date if it is inside the configured range.
    ///
    /// @param date the date to select
    public final void selectDate(LocalDate date) {
        setValue(Objects.requireNonNull(date, "date"));
    }

    /// Selects today's date when it is inside the configured range.
    public final void selectToday() {
        selectDate(LocalDate.now());
    }

    /// Applies a labeled date preset and shows the preset month.
    ///
    /// @param preset the date preset to apply
    public final void applyPreset(M3DatePreset preset) {
        LocalDate date = Objects.requireNonNull(preset, "preset").date();
        selectDate(date);
        showMonth(YearMonth.from(date));
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
    ///
    /// @param month the month to display
    public final void showMonth(YearMonth month) {
        setDisplayedMonth(month);
    }

    /// Shows the month containing today's date without changing the selected date.
    public final void showToday() {
        showMonth(YearMonth.from(LocalDate.now()));
    }

    /// Returns whether the supplied date is outside the configured selectable range.
    ///
    /// @param date the date to test
    /// @return `true` when the date is outside the configured selectable range
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
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> accessibleDayCellCount();
            case ITEM_AT_INDEX -> accessibleDayCellAt(parameters);
            case SELECTED_ITEMS -> selectedItems();
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for day selection and focus.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode(accessibleFocusNode());
            case INCREMENT -> moveSelectionByDays(1);
            case DECREMENT -> moveSelectionByDays(-1);
            case BLOCK_INCREMENT -> moveSelectionByMonths(1);
            case BLOCK_DECREMENT -> moveSelectionByMonths(-1);
            case SHOW_ITEM -> showAccessibleDay(parameters);
            case SET_SELECTED_ITEMS -> selectAccessibleDay(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
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

    /// Returns the current selected date as an immutable accessibility selection list.
    private List<LocalDate> selectedItems() {
        @Nullable LocalDate selectedDate = getValue();
        return selectedDate == null ? List.of() : List.of(selectedDate);
    }

    /// Returns the number of visible day cells.
    private int accessibleDayCellCount() {
        @Nullable M3DatePickerSkin skin = materialSkin();
        return skin == null ? visibleDateCount() : skin.getVisibleDayCellCount();
    }

    /// Returns the visible day cell or logical date at an accessibility index.
    private @Nullable Object accessibleDayCellAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }

        @Nullable M3DatePickerSkin skin = materialSkin();
        if (skin != null) {
            return skin.getVisibleDayCell(index);
        }
        return visibleDateAt(index);
    }

    /// Returns the preferred focus node for the currently displayed dates.
    private Node accessibleFocusNode() {
        @Nullable M3DatePickerSkin skin = materialSkin();
        if (skin == null) {
            return this;
        }

        @Nullable LocalDate selectedDate = getValue();
        if (selectedDate != null) {
            @Nullable Node selectedCell = skin.getDayCell(selectedDate);
            if (selectedCell != null && !selectedCell.isDisabled()) {
                return selectedCell;
            }
        }

        LocalDate today = LocalDate.now();
        if (YearMonth.from(today).equals(getDisplayedMonth())) {
            @Nullable Node todayCell = skin.getDayCell(today);
            if (todayCell != null && !todayCell.isDisabled()) {
                return todayCell;
            }
        }

        @Nullable Node firstEnabledCell = skin.getFirstEnabledDayCell();
        return firstEnabledCell == null ? this : firstEnabledCell;
    }

    /// Focuses an accessibility target or the picker itself.
    private void focusAccessibleNode(@Nullable Node node) {
        if (node == null) {
            requestFocus();
        } else {
            M3Accessible.showItem(node);
        }
    }

    /// Shows and focuses the day requested by accessibility parameters.
    private void showAccessibleDay(Object... parameters) {
        @Nullable Object item = accessibleDayItem(parameters);
        if (item instanceof Node node) {
            M3Accessible.showItem(node);
            return;
        }
        if (item instanceof LocalDate date) {
            showMonth(YearMonth.from(date));
            focusAccessibleDate(date);
            return;
        }
        focusAccessibleNode(accessibleFocusNode());
    }

    /// Selects the day requested by accessibility parameters.
    private void selectAccessibleDay(Object... parameters) {
        @Nullable Object item = accessibleDayItem(parameters);
        @Nullable LocalDate date = item instanceof LocalDate localDate ? localDate : dateFromNode(item);
        if (date != null && !isDateDisabled(date)) {
            setValue(date);
            focusAccessibleDate(date);
        }
    }

    /// Focuses the rendered day cell for a date when it is visible.
    private void focusAccessibleDate(LocalDate date) {
        @Nullable M3DatePickerSkin skin = materialSkin();
        focusAccessibleNode(skin == null ? this : skin.getDayCell(date));
    }

    /// Returns the day item requested by accessibility parameters.
    private @Nullable Object accessibleDayItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return accessibleFocusNode();
        }
        if (parameters[0] instanceof Number) {
            return accessibleDayCellAt(parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Object item = accessibleDayItem(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the day item requested by one accessibility action parameter.
    private @Nullable Object accessibleDayItem(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return accessibleDayCellAt(number);
        }
        if (parameter instanceof LocalDate date) {
            return date;
        }
        if (parameter instanceof Node node) {
            return dateFromNode(node) == null ? null : node;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Object item = accessibleDayItem(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Object item = accessibleDayItem(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the date stored on a rendered day cell.
    private static @Nullable LocalDate dateFromNode(@Nullable Object item) {
        return item instanceof Node node && node.getUserData() instanceof LocalDate date ? date : null;
    }

    /// Returns the date at a visible logical index.
    private @Nullable LocalDate visibleDateAt(int index) {
        if (index < 0 || index >= visibleDateCount()) {
            return null;
        }
        if (!isShowAdjacentMonthDays()) {
            return getDisplayedMonth().atDay(index + 1);
        }
        return gridStartDate().plusDays(index);
    }

    /// Returns the number of visible logical dates.
    private int visibleDateCount() {
        return isShowAdjacentMonthDays() ? 42 : getDisplayedMonth().lengthOfMonth();
    }

    /// Returns the first logical date in the displayed calendar grid.
    private LocalDate gridStartDate() {
        LocalDate firstOfMonth = getDisplayedMonth().atDay(1);
        int leadingDays = Math.floorMod(
                firstOfMonth.getDayOfWeek().getValue() - getFirstDayOfWeek().getValue(),
                7
        );
        return firstOfMonth.minusDays(leadingDays);
    }

    /// Returns the current Material date picker skin.
    private @Nullable M3DatePickerSkin materialSkin() {
        Skin<?> skin = getSkin();
        return skin instanceof M3DatePickerSkin materialSkin ? materialSkin : null;
    }

    /// Notifies accessibility clients that visible day cells changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
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
