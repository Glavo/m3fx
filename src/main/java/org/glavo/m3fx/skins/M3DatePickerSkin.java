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

    /// Refreshes visible text, style classes, and disabled states after control changes.
    private final InvalidationListener refreshListener = observable -> refresh();

    /// Updates logical layout when the effective node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateNodeOrientationLayout();

    /// Creates a date picker skin.
    ///
    /// @param control the date picker controlled by this skin
    public M3DatePickerSkin(M3DatePicker control) {
        super(control);
        initializeNodes();
        installListeners(control);
        getChildren().add(container);
        refresh();
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3DatePicker control = getSkinnable();
        control.valueProperty().removeListener(refreshListener);
        control.displayedMonthProperty().removeListener(refreshListener);
        control.firstDayOfWeekProperty().removeListener(refreshListener);
        control.minDateProperty().removeListener(refreshListener);
        control.maxDateProperty().removeListener(refreshListener);
        control.showAdjacentMonthDaysProperty().removeListener(refreshListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        header.nodeOrientationProperty().unbind();
        header.alignmentProperty().unbind();
        weekdayRow.nodeOrientationProperty().unbind();
        weekdayRow.alignmentProperty().unbind();
        dayGrid.nodeOrientationProperty().unbind();
        super.dispose();
    }

    /// Returns the number of visible day cells currently exposed by the skin.
    ///
    /// @return the number of visible day cells currently exposed by the skin
    public final int getVisibleDayCellCount() {
        int count = 0;
        for (DateCellButton dayCell : dayCells) {
            if (isAccessibleDayCell(dayCell)) {
                count++;
            }
        }
        return count;
    }

    /// Returns a visible day cell by visible index.
    ///
    /// @param visibleIndex the index among visible day cells
    /// @return the visible day cell at the supplied index, or `null` when the index is out of range
    public final @Nullable Node getVisibleDayCell(int visibleIndex) {
        if (visibleIndex < 0) {
            return null;
        }

        int currentIndex = 0;
        for (DateCellButton dayCell : dayCells) {
            if (!isAccessibleDayCell(dayCell)) {
                continue;
            }
            if (currentIndex == visibleIndex) {
                return dayCell;
            }
            currentIndex++;
        }
        return null;
    }

    /// Returns the visible day cell for a date.
    ///
    /// @param date the date represented by the requested day cell
    /// @return the visible day cell for the supplied date, or `null` when it is not currently visible
    public final @Nullable Node getDayCell(LocalDate date) {
        for (DateCellButton dayCell : dayCells) {
            if (isAccessibleDayCell(dayCell) && date.equals(dayCell.getUserData())) {
                return dayCell;
            }
        }
        return null;
    }

    /// Returns the first visible enabled day cell.
    ///
    /// @return the first visible enabled day cell, or `null` when no visible day cell is enabled
    public final @Nullable Node getFirstEnabledDayCell() {
        for (DateCellButton dayCell : dayCells) {
            if (isAccessibleDayCell(dayCell) && !dayCell.isDisabled()) {
                return dayCell;
            }
        }
        return null;
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
        control.valueProperty().addListener(refreshListener);
        control.displayedMonthProperty().addListener(refreshListener);
        control.firstDayOfWeekProperty().addListener(refreshListener);
        control.minDateProperty().addListener(refreshListener);
        control.maxDateProperty().addListener(refreshListener);
        control.showAdjacentMonthDaysProperty().addListener(refreshListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
    }

    /// Updates all visible labels and day cells from the current control state.
    private void refresh() {
        M3DatePicker control = getSkinnable();
        YearMonth displayedMonth = control.getDisplayedMonth();
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        monthLabel.setText(DateTimeFormatter.ofPattern("MMMM uuuu", locale).format(displayedMonth.atDay(1)));
        refreshWeekdayLabels(control, locale);
        refreshDayCells(control, displayedMonth);
        refreshNavigationButtons(control, displayedMonth);
    }

    /// Updates localized weekday labels starting from the configured first day.
    private void refreshWeekdayLabels(M3DatePicker control, Locale locale) {
        DayOfWeek firstDayOfWeek = control.getFirstDayOfWeek();
        for (int column = 0; column < COLUMN_COUNT; column++) {
            DayOfWeek dayOfWeek = firstDayOfWeek.plus(column);
            weekdayLabels.get(column).setText(dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale));
        }
    }

    /// Updates day cell dates, labels, visibility, and state classes.
    private void refreshDayCells(M3DatePicker control, YearMonth displayedMonth) {
        LocalDate firstOfMonth = displayedMonth.atDay(1);
        int leadingDays = Math.floorMod(
                firstOfMonth.getDayOfWeek().getValue() - control.getFirstDayOfWeek().getValue(),
                COLUMN_COUNT
        );
        LocalDate gridStart = firstOfMonth.minusDays(leadingDays);
        LocalDate today = LocalDate.now();
        LocalDate selectedDate = control.getValue();

        for (int index = 0; index < DAY_CELL_COUNT; index++) {
            LocalDate date = gridStart.plusDays(index);
            DateCellButton dayCell = dayCells.get(index);
            boolean outsideMonth = !YearMonth.from(date).equals(displayedMonth);
            boolean visible = !outsideMonth || control.isShowAdjacentMonthDays();
            boolean disabled = !visible || control.isDateDisabled(date);
            boolean todayDate = date.equals(today);
            boolean selected = date.equals(selectedDate);

            dayCell.setText(Integer.toString(date.getDayOfMonth()));
            dayCell.setUserData(date);
            dayCell.setAccessibleText(date.toString());
            dayCell.setVisible(visible);
            dayCell.setMouseTransparent(!visible);
            dayCell.setDisable(disabled);
            setStyleClass(dayCell, M3DatePicker.OUTSIDE_MONTH_DAY_STYLE_CLASS, outsideMonth && visible);
            setStyleClass(dayCell, M3DatePicker.TODAY_DAY_STYLE_CLASS, todayDate && visible);
            setStyleClass(dayCell, M3DatePicker.SELECTED_DAY_STYLE_CLASS, selected && visible);
        }
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

    /// Returns whether a day cell is visible to users and accessibility clients.
    private static boolean isAccessibleDayCell(DateCellButton dayCell) {
        return isEffectivelyReachable(dayCell) && !dayCell.isMouseTransparent();
    }

    /// Returns whether a node and its ancestor chain are visible and enabled.
    private static boolean isEffectivelyReachable(Node node) {
        @Nullable Node current = node;
        while (current != null) {
            if (!current.isVisible() || current.isDisabled()) {
                return false;
            }
            current = current.getParent();
        }
        return true;
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
