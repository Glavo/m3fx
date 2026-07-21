// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// The default calendar skin for [M3DateRangePicker].
@NotNullByDefault
public class M3DateRangePickerSkin extends SkinBase<M3DateRangePicker> {
    /// The number of columns in a weekly calendar grid.
    private static final int COLUMN_COUNT = 7;

    /// The number of visible rows in the month grid.
    private static final int ROW_COUNT = 6;

    /// The number of day cells kept in the reusable grid.
    private static final int DAY_CELL_COUNT = COLUMN_COUNT * ROW_COUNT;

    /// The pseudo-class applied to day cells while the picker is displayed right-to-left.
    private static final PseudoClass RTL_PSEUDO_CLASS = PseudoClass.getPseudoClass("rtl");

    /// The root skin container.
    private final VBox container = new VBox();

    /// The month navigation header.
    private final M3DatePickerHeader header;

    /// The row containing localized weekday labels.
    private final HBox weekdayRow = new HBox();

    /// The reusable month day grid.
    private final GridPane dayGrid = new GridPane();

    /// The weekday labels in display order.
    private final List<Label> weekdayLabels = new ArrayList<>(COLUMN_COUNT);

    /// The reusable day cell buttons in row-major order.
    private final List<DateCellButton> dayCells = new ArrayList<>(DAY_CELL_COUNT);

    /// The locale used to create the cached month formatter.
    private @Nullable Locale cachedFormatLocale;

    /// The month currently mapped into reusable day cells.
    private @Nullable YearMonth mappedMonth;

    /// The first day of week used by the current reusable day-cell mapping.
    private @Nullable DayOfWeek mappedFirstDayOfWeek;

    /// Whether the current reusable day-cell mapping exposes adjacent-month days.
    private boolean mappedShowAdjacentMonthDays;

    /// Refreshes range selection classes after either selected endpoint changes.
    private final InvalidationListener rangeInvalidation = observable -> refreshRangeSelection();

    /// Refreshes calendar structure after month, weekday-order, or adjacent-day changes.
    private final InvalidationListener calendarInvalidation = observable -> refreshCalendar();

    /// Refreshes cell availability and navigation after date-bound changes.
    private final InvalidationListener boundsInvalidation = observable -> refreshBounds();

    /// Updates logical layout when the effective node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> refreshRangeOrientation();

    /// Creates a date range picker skin.
    ///
    /// @param control the date range picker controlled by this skin
    public M3DateRangePickerSkin(M3DateRangePicker control) {
        super(control);
        header = new M3DatePickerHeader(
                control,
                control.displayedMonthProperty(),
                control.minDateProperty(),
                control.maxDateProperty()
        );
        initializeNodes();
        installListeners(control);
        getChildren().setAll(container);
        refreshCalendar();
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3DateRangePicker control = getSkinnable();
        control.startDateProperty().removeListener(rangeInvalidation);
        control.endDateProperty().removeListener(rangeInvalidation);
        control.displayedMonthProperty().removeListener(calendarInvalidation);
        control.firstDayOfWeekProperty().removeListener(calendarInvalidation);
        control.minDateProperty().removeListener(boundsInvalidation);
        control.maxDateProperty().removeListener(boundsInvalidation);
        control.showAdjacentMonthDaysProperty().removeListener(calendarInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        header.dispose();
        weekdayRow.nodeOrientationProperty().unbind();
        dayGrid.nodeOrientationProperty().unbind();
        getChildren().remove(container);
        super.dispose();
    }


    /// Computes the minimum width from the calendar container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the calendar container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the calendar container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the calendar container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Lays out the calendar container inside the skin bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Creates and styles the reusable skin nodes.
    private void initializeNodes() {
        container.getStyleClass().add(M3DatePicker.CONTAINER_STYLE_CLASS);
        weekdayRow.getStyleClass().add(M3DatePicker.WEEKDAY_ROW_STYLE_CLASS);
        weekdayRow.getStyleClass().add(M3DateRangePicker.WEEKDAY_ROW_STYLE_CLASS);
        dayGrid.getStyleClass().add(M3DatePicker.DAY_GRID_STYLE_CLASS);
        dayGrid.getStyleClass().add(M3DateRangePicker.DAY_GRID_STYLE_CLASS);
        container.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        weekdayRow.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        dayGrid.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());

        weekdayRow.setAlignment(Pos.CENTER_LEFT);
        for (int column = 0; column < COLUMN_COUNT; column++) {
            Label label = createWeekdayLabel();
            weekdayLabels.add(label);
            weekdayRow.getChildren().add(label);
        }

        for (int index = 0; index < DAY_CELL_COUNT; index++) {
            DateCellButton dayCell = createDayCell();
            dayCells.add(dayCell);
            dayGrid.add(dayCell, index % COLUMN_COUNT, index / COLUMN_COUNT);
        }

        container.getChildren().addAll(header, weekdayRow, dayGrid);
    }

    /// Installs listeners that keep the skin synchronized with control state.
    private void installListeners(M3DateRangePicker control) {
        control.startDateProperty().addListener(rangeInvalidation);
        control.endDateProperty().addListener(rangeInvalidation);
        control.displayedMonthProperty().addListener(calendarInvalidation);
        control.firstDayOfWeekProperty().addListener(calendarInvalidation);
        control.minDateProperty().addListener(boundsInvalidation);
        control.maxDateProperty().addListener(boundsInvalidation);
        control.showAdjacentMonthDaysProperty().addListener(calendarInvalidation);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
    }

    /// Updates localized labels, date mappings, range states, and navigation for a calendar structure change.
    private void refreshCalendar() {
        M3DateRangePicker control = getSkinnable();
        YearMonth displayedMonth = control.getDisplayedMonth();
        DayOfWeek firstDayOfWeek = control.getFirstDayOfWeek();
        boolean showAdjacentMonthDays = control.isShowAdjacentMonthDays();
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        boolean localeChanged = !locale.equals(cachedFormatLocale);
        boolean monthChanged = !displayedMonth.equals(mappedMonth);
        boolean firstDayChanged = firstDayOfWeek != mappedFirstDayOfWeek;
        boolean adjacentVisibilityChanged = showAdjacentMonthDays != mappedShowAdjacentMonthDays;
        if (localeChanged || firstDayChanged) {
            refreshWeekdayLabels(control, locale);
        }
        if (monthChanged || firstDayChanged || adjacentVisibilityChanged) {
            refreshDayCellStructure(control, displayedMonth);
            mappedMonth = displayedMonth;
            mappedFirstDayOfWeek = firstDayOfWeek;
            mappedShowAdjacentMonthDays = showAdjacentMonthDays;
        }
        refreshDayCellAvailability(control);
        refreshRangeSelection();
        refreshRangeOrientation();
        header.refresh();
    }

    /// Updates disabled day states and navigation after optional date bounds change.
    private void refreshBounds() {
        M3DateRangePicker control = getSkinnable();
        refreshDayCellAvailability(control);
        header.refresh();
    }

    /// Updates localized weekday labels starting from the configured first day.
    private void refreshWeekdayLabels(M3DateRangePicker control, Locale locale) {
        DayOfWeek firstDayOfWeek = control.getFirstDayOfWeek();
        for (int column = 0; column < COLUMN_COUNT; column++) {
            DayOfWeek dayOfWeek = firstDayOfWeek.plus(column);
            weekdayLabels.get(column).setText(dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale));
        }
        cachedFormatLocale = locale;
    }

    /// Updates day cell dates, labels, visibility, and structural state classes.
    private void refreshDayCellStructure(M3DateRangePicker control, YearMonth displayedMonth) {
        LocalDate firstOfMonth = displayedMonth.atDay(1);
        int leadingDays = Math.floorMod(
                firstOfMonth.getDayOfWeek().getValue() - control.getFirstDayOfWeek().getValue(),
                COLUMN_COUNT
        );
        LocalDate gridStart = firstOfMonth.minusDays(leadingDays);
        LocalDate today = LocalDate.now();

        for (int index = 0; index < DAY_CELL_COUNT; index++) {
            LocalDate date = gridStart.plusDays(index);
            DateCellButton dayCell = dayCells.get(index);
            boolean outsideMonth = !YearMonth.from(date).equals(displayedMonth);
            boolean visible = !outsideMonth || control.isShowAdjacentMonthDays();
            boolean todayDate = date.equals(today);

            String dayText = Integer.toString(date.getDayOfMonth());
            if (!dayText.equals(dayCell.getText())) {
                dayCell.setText(dayText);
            }
            dayCell.setUserData(date);
            dayCell.setAccessibleText(date.toString());
            dayCell.setVisible(visible);
            dayCell.setMouseTransparent(!visible);
            setStyleClass(dayCell, M3DatePicker.OUTSIDE_MONTH_DAY_STYLE_CLASS, outsideMonth && visible);
            setStyleClass(dayCell, M3DatePicker.TODAY_DAY_STYLE_CLASS, todayDate && visible);
        }
    }

    /// Updates disabled day states without rebuilding date mappings.
    private void refreshDayCellAvailability(M3DateRangePicker control) {
        for (DateCellButton dayCell : dayCells) {
            if (dayCell.getUserData() instanceof LocalDate date) {
                dayCell.setDisable(!dayCell.isVisible() || control.isDateDisabled(date));
            }
        }
    }

    /// Updates selected endpoints and contiguous range classes without rebuilding date mappings.
    private void refreshRangeSelection() {
        M3DateRangePicker control = getSkinnable();
        @Nullable LocalDate startDate = control.getStartDate();
        @Nullable LocalDate endDate = control.getEndDate();
        boolean completeRange = startDate != null && endDate != null;
        for (int index = 0; index < DAY_CELL_COUNT; index++) {
            DateCellButton dayCell = dayCells.get(index);
            @Nullable LocalDate date = dayCell.getUserData() instanceof LocalDate value ? value : null;
            boolean visible = dayCell.isVisible();
            boolean rangeStart = date != null && date.equals(startDate) && visible;
            boolean rangeEnd = date != null && date.equals(endDate) && visible;
            boolean singleDayRange = rangeStart && (!completeRange || rangeEnd);
            boolean rangeMiddle = completeRange
                    && date != null
                    && startDate != null
                    && endDate != null
                    && date.isAfter(startDate)
                    && date.isBefore(endDate)
                    && visible;
            boolean selected = rangeStart || rangeEnd;
            boolean rangeDay = selected || rangeMiddle;
            int column = index % COLUMN_COUNT;
            boolean rowStart = rangeDay && (rangeStart || column == 0);
            boolean rowEnd = rangeDay && (rangeEnd || column == COLUMN_COUNT - 1 || !completeRange);

            setStyleClass(dayCell, M3DatePicker.SELECTED_DAY_STYLE_CLASS, selected);
            setStyleClass(dayCell, M3DateRangePicker.RANGE_START_DAY_STYLE_CLASS, rangeStart);
            setStyleClass(dayCell, M3DateRangePicker.RANGE_END_DAY_STYLE_CLASS, rangeEnd);
            setStyleClass(dayCell, M3DateRangePicker.RANGE_SINGLE_DAY_STYLE_CLASS, singleDayRange);
            setStyleClass(dayCell, M3DateRangePicker.RANGE_MIDDLE_DAY_STYLE_CLASS, rangeMiddle);
            setStyleClass(dayCell, M3DateRangePicker.RANGE_ROW_START_DAY_STYLE_CLASS, rowStart);
            setStyleClass(dayCell, M3DateRangePicker.RANGE_ROW_END_DAY_STYLE_CLASS, rowEnd);
        }
    }

    /// Updates the RTL pseudo-class on existing day cells without rebuilding date mappings.
    private void refreshRangeOrientation() {
        boolean rightToLeft = M3NodeLayout.isRightToLeft(getSkinnable());
        for (DateCellButton dayCell : dayCells) {
            dayCell.pseudoClassStateChanged(RTL_PSEUDO_CLASS, rightToLeft);
        }
    }

    /// Selects the date represented by a day cell action.
    private void handleDayCellAction(ActionEvent event) {
        if (event.getSource() instanceof Node node && node.getUserData() instanceof LocalDate date) {
            M3DateRangePicker control = getSkinnable();
            if (!control.isDateDisabled(date)) {
                control.selectDate(date);
            }
        }
    }

    /// Creates a weekday label.
    private static Label createWeekdayLabel() {
        Label label = new Label();
        label.getStyleClass().add(M3DatePicker.WEEKDAY_LABEL_STYLE_CLASS);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    /// Creates a reusable day cell button.
    private DateCellButton createDayCell() {
        DateCellButton button = new DateCellButton();
        button.setOnAction(this::handleDayCellAction);
        return button;
    }

    /// Adds or removes a style class.
    private static void setStyleClass(Node node, String styleClass, boolean active) {
        List<String> styleClasses = node.getStyleClass();
        if (active) {
            if (!styleClasses.contains(styleClass)) {
                styleClasses.add(styleClass);
            }
        } else {
            styleClasses.remove(styleClass);
        }
    }

    /// An internal date cell button that avoids inheriting generic button user-agent rules.
    @NotNullByDefault
    private static final class DateCellButton extends ButtonBase {
        /// Creates a reusable date cell button.
        private DateCellButton() {
            super("");
            getStyleClass().add(M3DatePicker.DAY_CELL_STYLE_CLASS);
            setAccessibleRole(AccessibleRole.BUTTON);
            setAlignment(Pos.CENTER);
            setFocusTraversable(true);
            setMnemonicParsing(false);
            setTextOverrun(OverrunStyle.CLIP);
        }

        /// Fires this date cell's action handler.
        @Override
        public void fire() {
            if (!isDisabled()) {
                fireEvent(new ActionEvent(this, this));
            }
        }

        /// Creates the animated date cell skin.
        @Override
        protected Skin<?> createDefaultSkin() {
            return new M3DateCellSkin(this);
        }
    }
}
