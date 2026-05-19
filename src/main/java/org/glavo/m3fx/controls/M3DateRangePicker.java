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
import org.glavo.m3fx.skins.M3DateRangePickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/// A Material Design 3 calendar date range picker control.
@NotNullByDefault
public class M3DateRangePicker extends Control {
    /// The base style class for M3FX date range pickers.
    public static final String STYLE_CLASS = "m3-date-range-picker";

    /// The style class applied to a selected range start day cell.
    public static final String RANGE_START_DAY_STYLE_CLASS = "m3-date-range-picker-range-start-day";

    /// The style class applied to a selected range end day cell.
    public static final String RANGE_END_DAY_STYLE_CLASS = "m3-date-range-picker-range-end-day";

    /// The style class applied to a selected single-day range cell.
    public static final String RANGE_SINGLE_DAY_STYLE_CLASS = "m3-date-range-picker-range-single-day";

    /// The style class applied to days between selected range endpoints.
    public static final String RANGE_MIDDLE_DAY_STYLE_CLASS = "m3-date-range-picker-range-middle-day";

    /// The style class applied to the first day of one visible range row segment.
    public static final String RANGE_ROW_START_DAY_STYLE_CLASS = "m3-date-range-picker-range-row-start-day";

    /// The style class applied to the last day of one visible range row segment.
    public static final String RANGE_ROW_END_DAY_STYLE_CLASS = "m3-date-range-picker-range-row-end-day";

    /// The style class applied to the range picker's weekday label row.
    public static final String WEEKDAY_ROW_STYLE_CLASS = "m3-date-range-picker-weekday-row";

    /// The style class applied to the range picker's day grid.
    public static final String DAY_GRID_STYLE_CLASS = "m3-date-range-picker-day-grid";

    /// The first selected date, or `null` when no range start is selected.
    private final ObjectProperty<@Nullable LocalDate> startDate =
            new SimpleObjectProperty<>(this, "startDate") {
                /// Validates the date range before applying direct property writes.
                @Override
                public void set(@Nullable LocalDate newValue) {
                    if (!applyingRange) {
                        validateDate(newValue);
                        validateDateRange(newValue, getEndDate());
                    }
                    super.set(newValue);
                }

                /// Keeps the displayed month and accessibility state synchronized.
                @Override
                protected void invalidated() {
                    @Nullable LocalDate date = get();
                    if (!applyingRange && date == null && getEndDate() != null) {
                        endDate.set(null);
                    }
                    if (date != null) {
                        showMonth(YearMonth.from(date));
                    }
                    notifyAccessibleRangeChanged();
                }
            };

    /// The last selected date, or `null` while only a range start is selected.
    private final ObjectProperty<@Nullable LocalDate> endDate =
            new SimpleObjectProperty<>(this, "endDate") {
                /// Validates the date range before applying direct property writes.
                @Override
                public void set(@Nullable LocalDate newValue) {
                    if (!applyingRange) {
                        if (newValue != null && getStartDate() == null) {
                            throw new IllegalArgumentException("startDate must be selected before endDate");
                        }
                        validateDate(newValue);
                        validateDateRange(getStartDate(), newValue);
                    }
                    super.set(newValue);
                }

                /// Keeps the displayed month and accessibility state synchronized.
                @Override
                protected void invalidated() {
                    @Nullable LocalDate date = get();
                    if (date != null) {
                        showMonth(YearMonth.from(date));
                    }
                    notifyAccessibleRangeChanged();
                }
            };

    /// The month currently displayed in the calendar grid.
    private final ObjectProperty<YearMonth> displayedMonth =
            new SimpleObjectProperty<>(this, "displayedMonth", YearMonth.now()) {
                /// Keeps displayed month values non-null.
                @Override
                public void set(YearMonth newValue) {
                    super.set(Objects.requireNonNull(newValue, "displayedMonth"));
                }

                /// Notifies accessibility clients when visible day cells change.
                @Override
                protected void invalidated() {
                    notifyAccessibleItemsChanged();
                }
            };

    /// The weekday that appears in the first calendar column.
    private final ObjectProperty<DayOfWeek> firstDayOfWeek =
            new SimpleObjectProperty<>(this, "firstDayOfWeek", defaultFirstDayOfWeek()) {
                /// Keeps first day of week values non-null.
                @Override
                public void set(DayOfWeek newValue) {
                    super.set(Objects.requireNonNull(newValue, "firstDayOfWeek"));
                }

                /// Notifies accessibility clients when visible day cells change.
                @Override
                protected void invalidated() {
                    notifyAccessibleItemsChanged();
                }
            };

    /// The earliest selectable date, or `null` when there is no lower bound.
    private final ObjectProperty<@Nullable LocalDate> minDate =
            new SimpleObjectProperty<>(this, "minDate") {
                /// Clears the current range when bounds exclude any selected date.
                @Override
                protected void invalidated() {
                    clearRangeIfOutOfBounds();
                    notifyAccessibleItemsChanged();
                }
            };

    /// The latest selectable date, or `null` when there is no upper bound.
    private final ObjectProperty<@Nullable LocalDate> maxDate =
            new SimpleObjectProperty<>(this, "maxDate") {
                /// Clears the current range when bounds exclude any selected date.
                @Override
                protected void invalidated() {
                    clearRangeIfOutOfBounds();
                    notifyAccessibleItemsChanged();
                }
            };

    /// Whether days from adjacent months are shown in leading and trailing grid cells.
    private final BooleanProperty showAdjacentMonthDays =
            new SimpleBooleanProperty(this, "showAdjacentMonthDays", true) {
                /// Notifies accessibility clients when visible day cells change.
                @Override
                protected void invalidated() {
                    notifyAccessibleItemsChanged();
                }
            };

    /// Whether both endpoints are currently being assigned through [setRange].
    private boolean applyingRange;

    /// Creates a date range picker showing the current month with no selected dates.
    public M3DateRangePicker() {
        initialize();
    }

    /// Creates a date range picker initialized with the supplied selected range.
    public M3DateRangePicker(LocalDate startDate, LocalDate endDate) {
        initialize();
        setRange(startDate, endDate);
    }

    /// Returns the first selected date, or `null` when no range start is selected.
    public final @Nullable LocalDate getStartDate() {
        return startDate.get();
    }

    /// Sets the first selected date, or clears the range start when `null` is supplied.
    public final void setStartDate(@Nullable LocalDate startDate) {
        this.startDate.set(startDate);
    }

    /// Returns the range start property.
    public final ObjectProperty<@Nullable LocalDate> startDateProperty() {
        return startDate;
    }

    /// Returns the last selected date, or `null` while only a range start is selected.
    public final @Nullable LocalDate getEndDate() {
        return endDate.get();
    }

    /// Sets the last selected date, or clears the range end when `null` is supplied.
    public final void setEndDate(@Nullable LocalDate endDate) {
        this.endDate.set(endDate);
    }

    /// Returns the range end property.
    public final ObjectProperty<@Nullable LocalDate> endDateProperty() {
        return endDate;
    }

    /// Sets both range endpoints atomically after validating ordering and selectable bounds.
    public final void setRange(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        validateDate(startDate);
        validateDate(endDate);
        validateDateRange(startDate, endDate);
        applyingRange = true;
        try {
            this.startDate.set(startDate);
            this.endDate.set(endDate);
        } finally {
            applyingRange = false;
        }
    }

    /// Clears both range endpoints.
    public final void clearRange() {
        setRange(null, null);
    }

    /// Returns whether both range endpoints are selected.
    public final boolean isRangeComplete() {
        return getStartDate() != null && getEndDate() != null;
    }

    /// Returns whether the supplied date is inside the selected inclusive range.
    public final boolean isDateInSelectedRange(LocalDate date) {
        Objects.requireNonNull(date, "date");
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        return start != null && end != null && !date.isBefore(start) && !date.isAfter(end);
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
        validateSelectableBounds(minDate, getMaxDate());
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
        validateSelectableBounds(getMinDate(), maxDate);
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

    /// Selects a date as the next range endpoint.
    public final void selectDate(LocalDate date) {
        Objects.requireNonNull(date, "date");
        validateDate(date);

        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start == null || end != null) {
            setRange(date, null);
        } else if (date.isBefore(start)) {
            setRange(date, start);
        } else {
            setRange(start, date);
        }
    }

    /// Selects today's date as the next range endpoint when it is inside the configured range.
    public final void selectToday() {
        selectDate(LocalDate.now());
    }

    /// Shows the month before the current displayed month.
    public final void showPreviousMonth() {
        showMonth(getDisplayedMonth().minusMonths(1));
    }

    /// Shows the month after the current displayed month.
    public final void showNextMonth() {
        showMonth(getDisplayedMonth().plusMonths(1));
    }

    /// Shows the supplied month without changing the selected range.
    public final void showMonth(YearMonth month) {
        setDisplayedMonth(month);
    }

    /// Shows the month containing today's date without changing the selected range.
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

    /// Returns the user-agent stylesheet for M3FX date range pickers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("date-picker.css");
    }

    /// Returns accessibility attributes for the selected range and visible dates.
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

    /// Executes accessibility actions for date range selection and focus.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode(accessibleFocusNode());
            case SHOW_ITEM -> showAccessibleDay(parameters);
            case SET_SELECTED_ITEMS -> selectAccessibleDay(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 date range picker skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DateRangePickerSkin(this);
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
        selectKeyboardDate(navigationBaseDate().plusMonths(months));
    }

    /// Selects a keyboard navigation target when it is enabled.
    private void selectKeyboardDate(LocalDate date) {
        showMonth(YearMonth.from(date));
        if (!isDateDisabled(date)) {
            selectDate(date);
        }
    }

    /// Returns the base date used when keyboard navigation starts.
    private LocalDate navigationBaseDate() {
        @Nullable LocalDate end = getEndDate();
        if (end != null) {
            return end;
        }

        @Nullable LocalDate start = getStartDate();
        if (start != null) {
            return start;
        }

        YearMonth month = getDisplayedMonth();
        LocalDate today = LocalDate.now();
        return YearMonth.from(today).equals(month) ? today : month.atDay(1);
    }

    /// Validates that one date is selectable.
    private void validateDate(@Nullable LocalDate date) {
        if (date != null && isDateDisabled(date)) {
            throw new IllegalArgumentException("date is outside the selectable range");
        }
    }

    /// Clears the selected range when min or max bounds exclude one endpoint.
    private void clearRangeIfOutOfBounds() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start != null && isDateDisabled(start) || end != null && isDateDisabled(end)) {
            clearRange();
        }
    }

    /// Returns accessible text for the selected range or displayed month.
    private String accessibleText() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start == null) {
            return getDisplayedMonth().toString();
        }
        return end == null ? start.toString() : start + "/" + end;
    }

    /// Returns the selected endpoints as an immutable accessibility selection list.
    private List<LocalDate> selectedItems() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start == null) {
            return List.of();
        }
        if (end == null) {
            return List.of(start);
        }
        return List.of(start, end);
    }

    /// Returns the number of visible day cells.
    private int accessibleDayCellCount() {
        @Nullable M3DateRangePickerSkin skin = materialSkin();
        return skin == null ? visibleDateCount() : skin.getVisibleDayCellCount();
    }

    /// Returns the visible day cell or logical date at an accessibility index.
    private @Nullable Object accessibleDayCellAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }

        @Nullable M3DateRangePickerSkin skin = materialSkin();
        if (skin != null) {
            return skin.getVisibleDayCell(index);
        }
        return visibleDateAt(index);
    }

    /// Returns the preferred focus node for the currently displayed dates.
    private Node accessibleFocusNode() {
        @Nullable M3DateRangePickerSkin skin = materialSkin();
        if (skin == null) {
            return this;
        }

        @Nullable LocalDate end = getEndDate();
        if (end != null) {
            @Nullable Node endCell = skin.getDayCell(end);
            if (endCell != null && !endCell.isDisabled()) {
                return endCell;
            }
        }

        @Nullable LocalDate start = getStartDate();
        if (start != null) {
            @Nullable Node startCell = skin.getDayCell(start);
            if (startCell != null && !startCell.isDisabled()) {
                return startCell;
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
            selectDate(date);
            focusAccessibleDate(date);
        }
    }

    /// Focuses the rendered day cell for a date when it is visible.
    private void focusAccessibleDate(LocalDate date) {
        @Nullable M3DateRangePickerSkin skin = materialSkin();
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

    /// Returns the current Material date range picker skin.
    private @Nullable M3DateRangePickerSkin materialSkin() {
        Skin<?> skin = getSkin();
        return skin instanceof M3DateRangePickerSkin materialSkin ? materialSkin : null;
    }

    /// Notifies accessibility clients that selected range values changed.
    private void notifyAccessibleRangeChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    }

    /// Notifies accessibility clients that visible day cells changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
    }

    /// Validates an optional inclusive selected date range.
    private static void validateDateRange(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }

    /// Validates optional inclusive selectable bounds.
    private static void validateSelectableBounds(@Nullable LocalDate minDate, @Nullable LocalDate maxDate) {
        if (minDate != null && maxDate != null && minDate.isAfter(maxDate)) {
            throw new IllegalArgumentException("minDate must not be after maxDate");
        }
    }

    /// Returns the locale's default first day of week.
    private static DayOfWeek defaultFirstDayOfWeek() {
        return WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    }
}
