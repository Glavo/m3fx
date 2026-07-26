// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3PickerAccessibilityPresentation;
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

/// A Material Design 3 calendar for selecting one local date.
///
/// The picker maintains a nullable selected [value][#valueProperty()] independently from the
/// [displayed month][#displayedMonthProperty()]. Selecting a date also displays its month; changing only the
/// displayed month does not change selection. Inclusive minimum and maximum dates constrain selection. Updating a
/// bound so that the current value falls outside the permitted interval clears the value. When the value property is
/// bound, its binding must continue to supply a selectable date as the bounds change.
///
/// The calendar is focus traversable. Arrow keys move by day or week, Page Up and Page Down move by month, and Home
/// and End move to the first and last day of the displayed month. Keyboard navigation changes selection only when
/// the target date is enabled. The first weekday defaults to the current locale when the picker is constructed.
///
/// A typical bounded picker can be configured as follows:
///
/// ```java
/// private M3DatePicker createDatePicker() {
///     LocalDate today = LocalDate.now();
///     M3DatePicker picker = new M3DatePicker();
///     picker.setMinDate(today);
///     picker.setMaxDate(today.plusMonths(3));
///     picker.valueProperty().addListener((observable, oldDate, newDate) ->
///             System.out.println(newDate));
///     return picker;
/// }
/// ```
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DatePicker extends Control {
    /// The pseudo-class applied when the calendar is rendered by a modal picker dialog.
    static final PseudoClass MODAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("modal");

    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-date-picker";

    /// Creates a picker showing the current month with no selected date and no date bounds.
    public M3DatePicker() {
        initialize();
    }

    /// Creates a picker whose selected value and displayed month are initialized from the specified date.
    ///
    /// @param value the initially selected date
    public M3DatePicker(LocalDate value) {
        initialize();
        setValue(value);
    }

    /// The selected date, or `null` when selection is empty.
    ///
    /// The default value is `null`. A non-null value set through [setValue][#setValue(LocalDate)] must be within
    /// the current inclusive bounds. Writing this property directly bypasses that setter validation; callers doing
    /// so are responsible for supplying a selectable date.
    ///
    /// @defaultValue `null`
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
                    M3Accessible.notifyFocusNodeChanged(M3DatePicker.this);
                }
            };

    /// Returns the selected date, or `null` when no date is selected.
    ///
    /// @return the selected date, or `null` when no date is selected
    public final @Nullable LocalDate getValue() {
        return value.get();
    }

    /// Sets the selected date, or clears selection when `null` is supplied.
    ///
    /// A non-null value also changes the displayed month to the value's month.
    ///
    /// @param value the selected date, or `null` to clear selection
    /// @throws IllegalArgumentException if `value` is outside the current inclusive bounds
    public final void setValue(@Nullable LocalDate value) {
        if (value != null && isDateDisabled(value)) {
            throw new IllegalArgumentException("value is outside the selectable range");
        }
        this.value.set(value);
    }

    /// Returns the observable property that stores the selected date.
    ///
    /// The property can be observed and bound. Its default value is `null`. Direct non-null assignments through
    /// the property bypass [setValue][#setValue(LocalDate)] range validation and change the displayed month. A
    /// binding must supply only dates within the current bounds; changing a bound cannot clear an invalid value while
    /// this property remains bound.
    ///
    /// @return the selected-date property
    public final ObjectProperty<@Nullable LocalDate> valueProperty() {
        return value;
    }

    /// The month currently displayed by the calendar.
    ///
    /// The initial value is the month containing the construction date. A direct `null` assignment replaces it with
    /// the month containing the assignment date; bound values must be non-null. Changing this property does not
    /// change [value][#valueProperty()].
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

    /// Returns the month currently displayed by the calendar grid.
    ///
    /// @return the displayed calendar month
    public final YearMonth getDisplayedMonth() {
        return displayedMonth.get();
    }

    /// Sets the month displayed by the calendar grid.
    ///
    /// @param displayedMonth the month displayed by the calendar grid
    /// @throws NullPointerException if `displayedMonth` is `null`
    public final void setDisplayedMonth(YearMonth displayedMonth) {
        this.displayedMonth.set(Objects.requireNonNull(displayedMonth, "displayedMonth"));
    }

    /// Returns the observable property that stores the displayed month.
    ///
    /// The property can be observed and bound. Its initial value is the month containing the construction date.
    /// Direct `null` assignments restore the month containing the assignment date; bound values must be non-null.
    ///
    /// @return the displayed-month property
    public final ObjectProperty<YearMonth> displayedMonthProperty() {
        return displayedMonth;
    }

    /// The weekday shown in the first calendar column.
    ///
    /// The initial value is derived from the default locale at construction time. A direct `null` assignment
    /// recomputes the locale default. Changing the JVM default locale does not otherwise update an existing picker.
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

    /// Returns the weekday shown in the first calendar column.
    ///
    /// @return the weekday shown in the first calendar column
    public final DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek.get();
    }

    /// Sets the weekday shown in the first calendar column.
    ///
    /// @param firstDayOfWeek the weekday shown in the first calendar column
    /// @throws NullPointerException if `firstDayOfWeek` is `null`
    public final void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        this.firstDayOfWeek.set(Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek"));
    }

    /// Returns the observable property that stores the first day of the week.
    ///
    /// The property can be observed and bound. Its initial value is derived from the default locale at construction
    /// time. Direct `null` assignments recompute that locale default; bound values must be non-null.
    ///
    /// @return the first-day-of-week property
    public final ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return firstDayOfWeek;
    }

    /// The inclusive lower selection bound, or `null` for no lower bound.
    ///
    /// The default value is `null`. [setMinDate][#setMinDate(LocalDate)] rejects a value after the current maximum.
    /// Direct property writes are not prevalidated and must preserve the bound ordering. Changing this property
    /// clears a selected value that becomes out of range.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalDate> minDate =
            new SimpleObjectProperty<>(this, "minDate") {
                /// Clears the selected date when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                    notifyAccessibleItemsChanged();
                }
            };

    /// Returns the earliest selectable date, or `null` when there is no lower bound.
    ///
    /// @return the earliest selectable date, or `null` when there is no lower bound
    public final @Nullable LocalDate getMinDate() {
        return minDate.get();
    }

    /// Sets the earliest selectable date, or clears the lower bound when `null` is supplied.
    ///
    /// @param minDate the earliest selectable date, or `null` to clear the lower bound
    /// @throws IllegalArgumentException if `minDate` is after the current [maxDate][#maxDateProperty()]
    /// @throws RuntimeException         if the selected value becomes invalid and [#valueProperty()] is bound
    public final void setMinDate(@Nullable LocalDate minDate) {
        validateDateRange(minDate, getMaxDate());
        this.minDate.set(minDate);
    }

    /// Returns the observable property that stores the inclusive lower date bound.
    ///
    /// The property can be observed and bound, and its default value is `null`. Direct property writes bypass the
    /// ordering check performed by [setMinDate][#setMinDate(LocalDate)] and clear a selection that becomes invalid.
    /// If [#valueProperty()] is bound, its binding must update the selected date before this property excludes it.
    ///
    /// @return the minimum-date property
    public final ObjectProperty<@Nullable LocalDate> minDateProperty() {
        return minDate;
    }

    /// The inclusive upper selection bound, or `null` for no upper bound.
    ///
    /// The default value is `null`. [setMaxDate][#setMaxDate(LocalDate)] rejects a value before the current minimum.
    /// Direct property writes are not prevalidated and must preserve the bound ordering. Changing this property
    /// clears a selected value that becomes out of range.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalDate> maxDate =
            new SimpleObjectProperty<>(this, "maxDate") {
                /// Clears the selected date when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                    notifyAccessibleItemsChanged();
                }
            };

    /// Returns the latest selectable date, or `null` when there is no upper bound.
    ///
    /// @return the latest selectable date, or `null` when there is no upper bound
    public final @Nullable LocalDate getMaxDate() {
        return maxDate.get();
    }

    /// Sets the latest selectable date, or clears the upper bound when `null` is supplied.
    ///
    /// @param maxDate the latest selectable date, or `null` to clear the upper bound
    /// @throws IllegalArgumentException if `maxDate` is before the current [minDate][#minDateProperty()]
    /// @throws RuntimeException         if the selected value becomes invalid and [#valueProperty()] is bound
    public final void setMaxDate(@Nullable LocalDate maxDate) {
        validateDateRange(getMinDate(), maxDate);
        this.maxDate.set(maxDate);
    }

    /// Returns the observable property that stores the inclusive upper date bound.
    ///
    /// The property can be observed and bound, and its default value is `null`. Direct property writes bypass the
    /// ordering check performed by [setMaxDate][#setMaxDate(LocalDate)] and clear a selection that becomes invalid.
    /// If [#valueProperty()] is bound, its binding must update the selected date before this property excludes it.
    ///
    /// @return the maximum-date property
    public final ObjectProperty<@Nullable LocalDate> maxDateProperty() {
        return maxDate;
    }

    /// Whether leading and trailing cells display dates from adjacent months.
    ///
    /// The default value is `true`. Hidden adjacent-month dates remain available by navigating to their month.
    ///
    /// @defaultValue `true`
    private final BooleanProperty showAdjacentMonthDays =
            new SimpleBooleanProperty(this, "showAdjacentMonthDays", true) {
                /// Notifies accessibility clients when visible day cells change.
                @Override
                protected void invalidated() {
                    notifyAccessibleItemsChanged();
                }
            };

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

    /// Returns the observable property that controls whether adjacent-month dates are visible.
    ///
    /// The property can be observed and bound. Its default value is `true`.
    ///
    /// @return the show-adjacent-month-days property
    public final BooleanProperty showAdjacentMonthDaysProperty() {
        return showAdjacentMonthDays;
    }

    /// Selects the specified date and displays its month.
    ///
    /// @param date the date to select
    /// @throws NullPointerException     if `date` is `null`
    /// @throws IllegalArgumentException if `date` is outside the current inclusive bounds
    public final void selectDate(LocalDate date) {
        setValue(Objects.requireNonNull(date, "date"));
    }

    /// Selects today's date and displays the current month.
    ///
    /// @throws IllegalArgumentException if today's date is outside the current inclusive bounds
    public final void selectToday() {
        selectDate(LocalDate.now());
    }

    /// Selects the date represented by a preset and displays its month.
    ///
    /// @param preset the date preset to apply
    /// @throws NullPointerException     if `preset` is `null`
    /// @throws IllegalArgumentException if the preset date is outside the current inclusive bounds
    public final void applyPreset(M3DatePreset preset) {
        LocalDate date = Objects.requireNonNull(preset, "preset").date();
        selectDate(date);
        showMonth(YearMonth.from(date));
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
    /// @throws NullPointerException if `month` is `null`
    public final void showMonth(YearMonth month) {
        setDisplayedMonth(month);
    }

    /// Shows the month containing today's date without changing the selected date.
    public final void showToday() {
        showMonth(YearMonth.from(LocalDate.now()));
    }

    /// Returns whether the specified date is outside the configured inclusive bounds.
    ///
    /// @param date the date to test
    /// @return `true` when the date is outside the configured selectable range
    /// @throws NullPointerException if `date` is `null`
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

    /// Returns accessibility state for the selected date and the currently rendered day cells.
    ///
    /// `ITEM_COUNT` and `ITEM_AT_INDEX` describe currently available day-cell nodes. When no indexed day-cell
    /// presentation is available, the item count is zero and indexed item queries return `null`. An indexed item
    /// query never returns a [LocalDate] model value in place of a node.
    ///
    /// @throws NullPointerException if `attribute` is `null`
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
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

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
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleDay,
                this::handlesAccessibleShowTarget);
        setFocusTraversable(true);
        skinProperty().addListener(observable -> notifyAccessibleItemsChanged());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
    }

    /// Handles keyboard date navigation.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }

        boolean handled = switch (event.getCode()) {
            case LEFT -> {
                moveSelectionHorizontally(false);
                yield true;
            }
            case RIGHT -> {
                moveSelectionHorizontally(true);
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

    /// Moves keyboard selection horizontally in the visual direction of the pressed arrow key.
    private void moveSelectionHorizontally(boolean rightKey) {
        boolean forward = M3NodeLayout.isRightToLeft(this) != rightKey;
        moveSelectionByDays(forward ? 1 : -1);
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
        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        return presentation == null ? 0 : presentation.accessibleItemCount();
    }

    /// Returns the visible day cell at an accessibility index.
    private @Nullable Node accessibleDayCellAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        return presentation == null ? null : presentation.accessibleItemAt(index);
    }

    /// Returns the preferred focus node for the currently displayed dates.
    private Node accessibleFocusNode() {
        if (accessibilityPresentation() == null) {
            return this;
        }

        @Nullable Node focusedCell = currentFocusedDayCell();
        if (focusedCell != null) {
            return focusedCell;
        }

        @Nullable LocalDate selectedDate = getValue();
        if (selectedDate != null) {
            @Nullable Node selectedCell = dayCellForDate(selectedDate);
            if (selectedCell != null && !selectedCell.isDisabled()) {
                return selectedCell;
            }
        }

        LocalDate today = LocalDate.now();
        if (YearMonth.from(today).equals(getDisplayedMonth())) {
            @Nullable Node todayCell = dayCellForDate(today);
            if (todayCell != null && !todayCell.isDisabled()) {
                return todayCell;
            }
        }

        @Nullable Node firstEnabledCell = firstEnabledDayCell();
        return firstEnabledCell == null ? this : firstEnabledCell;
    }

    /// Returns the currently focused visible day cell, or `null` when focus is outside this picker.
    private @Nullable Node currentFocusedDayCell() {
        @Nullable Node focusOwner = getScene() == null ? null : getScene().getFocusOwner();
        if (focusOwner == null) {
            return null;
        }

        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        if (presentation == null) {
            return null;
        }
        int itemCount = presentation.accessibleItemCount();
        for (int index = 0; index < itemCount; index++) {
            @Nullable Node cell = presentation.accessibleItemAt(index);
            if (cell == null) {
                continue;
            }
            if (!cell.isDisabled() && M3Accessible.containsNode(cell, focusOwner)) {
                return M3Accessible.canReach(focusOwner) ? focusOwner : cell;
            }
        }
        return null;
    }

    /// Focuses the current accessibility target or the picker itself.
    final boolean focusAccessibleNode() {
        return focusAccessibleNode(accessibleFocusNode());
    }

    /// Focuses an accessibility target or the picker itself.
    private boolean focusAccessibleNode(@Nullable Node node) {
        if (node != null && node != this && M3Accessible.showItem(this, node)) {
            return true;
        }
        return M3Accessible.showDirectItem(this, this);
    }

    /// Returns whether this picker can reveal the supplied accessibility date target.
    private boolean handlesAccessibleShowTarget(@Nullable Object parameter) {
        return parameter instanceof LocalDate date && !isDateDisabled(date);
    }

    /// Shows and focuses the day requested by accessibility parameters.
    final boolean showAccessibleDay(Object... parameters) {
        @Nullable Object item = accessibleDayItem(parameters);
        if (item instanceof Node node && M3Accessible.showItem(this, node)) {
            return true;
        }
        if (item instanceof LocalDate date) {
            if (isDateDisabled(date)) {
                return false;
            }
            showMonth(YearMonth.from(date));
            return showAccessibleDate(date);
        }
        return parameters.length == 0 && focusAccessibleNode();
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

    /// Shows the rendered day cell for a date when it is visible.
    private boolean showAccessibleDate(LocalDate date) {
        @Nullable Node cell = dayCellForDate(date);
        return cell != null && !cell.isDisabled() && M3Accessible.showItem(this, cell);
    }

    /// Focuses the rendered day cell for a selectable date when it is visible.
    private void focusAccessibleDate(LocalDate date) {
        if (!isDateDisabled(date)) {
            focusAccessibleNode(dayCellForDate(date));
        }
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
            return M3Accessible.isEffectivelyReachable(node) && dateFromNode(node) != null ? node : null;
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

    /// Returns the current indexed day-cell accessibility presentation.
    private @Nullable M3PickerAccessibilityPresentation accessibilityPresentation() {
        return getSkin() instanceof M3PickerAccessibilityPresentation presentation ? presentation : null;
    }

    /// Returns the rendered visible day cell for the supplied date.
    private @Nullable Node dayCellForDate(LocalDate date) {
        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        if (presentation == null) {
            return null;
        }
        int itemCount = presentation.accessibleItemCount();
        for (int index = 0; index < itemCount; index++) {
            @Nullable Node cell = presentation.accessibleItemAt(index);
            if (date.equals(dateFromNode(cell))) {
                return cell;
            }
        }
        return null;
    }

    /// Returns the first rendered visible enabled day cell.
    private @Nullable Node firstEnabledDayCell() {
        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        if (presentation == null) {
            return null;
        }
        int itemCount = presentation.accessibleItemCount();
        for (int index = 0; index < itemCount; index++) {
            @Nullable Node cell = presentation.accessibleItemAt(index);
            if (cell != null && !cell.isDisabled()) {
                return cell;
            }
        }
        return null;
    }

    /// Notifies accessibility clients that visible day cells changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        M3Accessible.notifyFocusNodeChanged(this);
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
