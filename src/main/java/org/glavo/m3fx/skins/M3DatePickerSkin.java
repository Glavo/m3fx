// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// The default calendar skin for [M3DatePicker].
@NotNullByDefault
public class M3DatePickerSkin extends SkinBase<M3DatePicker> {
    /// The number of columns in a weekly calendar grid.
    private static final int COLUMN_COUNT = 7;

    /// The number of visible rows in the month grid.
    private static final int ROW_COUNT = 6;

    /// The number of day cells kept in the reusable grid.
    private static final int DAY_CELL_COUNT = COLUMN_COUNT * ROW_COUNT;

    /// The root skin container.
    private final VBox container = new VBox();

    /// The month navigation header.
    private final HBox header = new HBox();

    /// The localized displayed-month label.
    private final Label monthLabel = new Label();

    /// The button that navigates to the previous month.
    private final M3IconButton previousButton = createNavigationButton(M3InternalIcon.Glyph.CHEVRON_LEFT);

    /// The button that navigates to the next month.
    private final M3IconButton nextButton = createNavigationButton(M3InternalIcon.Glyph.CHEVRON_RIGHT);

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

    /// The cached formatter for the localized month heading.
    private @Nullable DateTimeFormatter cachedMonthFormatter;

    /// The month currently mapped into reusable day cells.
    private @Nullable YearMonth mappedMonth;

    /// The first day of week used by the current reusable day-cell mapping.
    private @Nullable DayOfWeek mappedFirstDayOfWeek;

    /// Whether the current reusable day-cell mapping exposes adjacent-month days.
    private boolean mappedShowAdjacentMonthDays;

    /// Refreshes the selected cell after value changes.
    private final InvalidationListener selectionInvalidation = observable -> refreshSelection();

    /// Refreshes calendar structure after month, weekday-order, or adjacent-day changes.
    private final InvalidationListener calendarInvalidation = observable -> refreshCalendar();

    /// Refreshes cell availability and navigation after date-bound changes.
    private final InvalidationListener boundsInvalidation = observable -> refreshBounds();

    /// Updates logical layout when the effective node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateNodeOrientationLayout();

    /// Creates a date picker skin.
    ///
    /// @param control the date picker controlled by this skin
    public M3DatePickerSkin(M3DatePicker control) {
        super(control);
        initializeNodes();
        installListeners(control);
        getChildren().setAll(container);
        refreshCalendar();
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3DatePicker control = getSkinnable();
        control.valueProperty().removeListener(selectionInvalidation);
        control.displayedMonthProperty().removeListener(calendarInvalidation);
        control.firstDayOfWeekProperty().removeListener(calendarInvalidation);
        control.minDateProperty().removeListener(boundsInvalidation);
        control.maxDateProperty().removeListener(boundsInvalidation);
        control.showAdjacentMonthDaysProperty().removeListener(calendarInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        header.nodeOrientationProperty().unbind();
        header.alignmentProperty().unbind();
        weekdayRow.nodeOrientationProperty().unbind();
        weekdayRow.alignmentProperty().unbind();
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
        header.getStyleClass().add(M3DatePicker.HEADER_STYLE_CLASS);
        monthLabel.getStyleClass().add(M3DatePicker.MONTH_LABEL_STYLE_CLASS);
        weekdayRow.getStyleClass().add(M3DatePicker.WEEKDAY_ROW_STYLE_CLASS);
        dayGrid.getStyleClass().add(M3DatePicker.DAY_GRID_STYLE_CLASS);
        container.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        header.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        weekdayRow.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        dayGrid.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(getSkinnable()));
        header.getChildren().addAll(monthLabel, spacer, previousButton, nextButton);

        weekdayRow.alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(getSkinnable()));
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

        previousButton.setOnAction(event -> getSkinnable().showPreviousMonth());
        nextButton.setOnAction(event -> getSkinnable().showNextMonth());
        container.getChildren().addAll(header, weekdayRow, dayGrid);
        updateNodeOrientationLayout();
    }

    /// Installs listeners that keep the skin synchronized with control state.
    private void installListeners(M3DatePicker control) {
        control.valueProperty().addListener(selectionInvalidation);
        control.displayedMonthProperty().addListener(calendarInvalidation);
        control.firstDayOfWeekProperty().addListener(calendarInvalidation);
        control.minDateProperty().addListener(boundsInvalidation);
        control.maxDateProperty().addListener(boundsInvalidation);
        control.showAdjacentMonthDaysProperty().addListener(calendarInvalidation);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
    }

    /// Updates localized labels, date mappings, state classes, and navigation for a calendar structure change.
    private void refreshCalendar() {
        M3DatePicker control = getSkinnable();
        YearMonth displayedMonth = control.getDisplayedMonth();
        DayOfWeek firstDayOfWeek = control.getFirstDayOfWeek();
        boolean showAdjacentMonthDays = control.isShowAdjacentMonthDays();
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        boolean localeChanged = !locale.equals(cachedFormatLocale);
        boolean monthChanged = !displayedMonth.equals(mappedMonth);
        boolean firstDayChanged = firstDayOfWeek != mappedFirstDayOfWeek;
        boolean adjacentVisibilityChanged = showAdjacentMonthDays != mappedShowAdjacentMonthDays;
        if (localeChanged || monthChanged) {
            monthLabel.setText(monthFormatter(locale).format(displayedMonth.atDay(1)));
        }
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
        refreshSelection();
        refreshNavigationButtons(control, displayedMonth);
    }

    /// Updates disabled day states and navigation after optional date bounds change.
    private void refreshBounds() {
        M3DatePicker control = getSkinnable();
        refreshDayCellAvailability(control);
        refreshNavigationButtons(control, control.getDisplayedMonth());
    }

    /// Updates localized weekday labels starting from the configured first day.
    private void refreshWeekdayLabels(M3DatePicker control, Locale locale) {
        DayOfWeek firstDayOfWeek = control.getFirstDayOfWeek();
        for (int column = 0; column < COLUMN_COUNT; column++) {
            DayOfWeek dayOfWeek = firstDayOfWeek.plus(column);
            weekdayLabels.get(column).setText(dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale));
        }
    }

    /// Updates day cell dates, labels, visibility, and structural state classes.
    private void refreshDayCellStructure(M3DatePicker control, YearMonth displayedMonth) {
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
    private void refreshDayCellAvailability(M3DatePicker control) {
        for (DateCellButton dayCell : dayCells) {
            if (dayCell.getUserData() instanceof LocalDate date) {
                dayCell.setDisable(!dayCell.isVisible() || control.isDateDisabled(date));
            }
        }
    }

    /// Updates the selected-day state without rebuilding date mappings.
    private void refreshSelection() {
        @Nullable LocalDate selectedDate = getSkinnable().getValue();
        for (DateCellButton dayCell : dayCells) {
            boolean selected = dayCell.isVisible() && selectedDate != null && selectedDate.equals(dayCell.getUserData());
            setStyleClass(dayCell, M3DatePicker.SELECTED_DAY_STYLE_CLASS, selected);
        }
    }

    /// Returns the cached localized month formatter, rebuilding it only when the default format locale changes.
    private DateTimeFormatter monthFormatter(Locale locale) {
        DateTimeFormatter formatter = cachedMonthFormatter;
        if (formatter == null || !locale.equals(cachedFormatLocale)) {
            formatter = DateTimeFormatter.ofPattern("MMMM uuuu", locale);
            cachedFormatLocale = locale;
            cachedMonthFormatter = formatter;
        }
        return formatter;
    }

    /// Updates month navigation buttons according to optional date bounds.
    private void refreshNavigationButtons(M3DatePicker control, YearMonth displayedMonth) {
        LocalDate previousMonthEnd = displayedMonth.minusMonths(1).atEndOfMonth();
        LocalDate nextMonthStart = displayedMonth.plusMonths(1).atDay(1);
        LocalDate minDate = control.getMinDate();
        LocalDate maxDate = control.getMaxDate();
        previousButton.setDisable(minDate != null && previousMonthEnd.isBefore(minDate));
        nextButton.setDisable(maxDate != null && nextMonthStart.isAfter(maxDate));
    }

    /// Updates orientation-dependent navigation glyphs.
    private void updateNodeOrientationLayout() {
        boolean rightToLeft = M3NodeLayout.isRightToLeft(getSkinnable());
        setNavigationIcon(previousButton, rightToLeft
                ? M3InternalIcon.Glyph.CHEVRON_RIGHT
                : M3InternalIcon.Glyph.CHEVRON_LEFT);
        setNavigationIcon(nextButton, rightToLeft
                ? M3InternalIcon.Glyph.CHEVRON_LEFT
                : M3InternalIcon.Glyph.CHEVRON_RIGHT);
    }

    /// Selects the date represented by a day cell action.
    private void handleDayCellAction(ActionEvent event) {
        if (event.getSource() instanceof Node node && node.getUserData() instanceof LocalDate date) {
            M3DatePicker control = getSkinnable();
            if (!control.isDateDisabled(date)) {
                control.selectDate(date);
            }
        }
    }

    /// Creates a navigation icon button.
    private static M3IconButton createNavigationButton(M3InternalIcon.Glyph glyph) {
        M3IconButton button = new M3IconButton(new M3InternalIcon(glyph, M3InternalIcon.ColorRole.PRIMARY));
        button.getStyleClass().add(M3DatePicker.NAVIGATION_BUTTON_STYLE_CLASS);
        return button;
    }

    /// Updates a navigation button icon glyph.
    private static void setNavigationIcon(M3IconButton button, M3InternalIcon.Glyph glyph) {
        if (button.getGraphic() instanceof M3InternalIcon icon) {
            icon.setGlyph(glyph);
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
            return new DateCellSkin(this);
        }

        /// Returns the user-agent stylesheet for date cell states.
        @Override
        public String getUserAgentStylesheet() {
            return M3Stylesheets.controlStylesheet("date-picker.css");
        }
    }

    /// The animated labeled skin used by date cell buttons.
    @NotNullByDefault
    private static final class DateCellSkin extends M3LabeledButtonSkinBase<DateCellButton> {
        /// Creates a date cell skin.
        private DateCellSkin(DateCellButton control) {
            super(control);
        }
    }
}
