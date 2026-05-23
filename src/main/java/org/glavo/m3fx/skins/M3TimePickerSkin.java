// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
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
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/// The default selectable-grid skin for [M3TimePicker].
@NotNullByDefault
public class M3TimePickerSkin extends SkinBase<M3TimePicker> {
    /// The number of columns used by the hour grid in 24-hour mode.
    private static final int TWENTY_FOUR_HOUR_COLUMNS = 6;

    /// The number of columns used by compact 12-item grids.
    private static final int COMPACT_GRID_COLUMNS = 4;

    /// The root skin container.
    private final VBox container = new VBox();

    /// The selected time display.
    private final HBox display = new HBox();

    /// The displayed hour label.
    private final Label hourDisplay = new Label();

    /// The displayed separator label.
    private final Label displaySeparator = new Label(":");

    /// The displayed minute label.
    private final Label minuteDisplay = new Label();

    /// The displayed period label.
    private final Label periodDisplay = new Label();

    /// The row containing hour and minute selection sections.
    private final HBox sections = new HBox();

    /// The section containing hour cells.
    private final VBox hourSection = new VBox();

    /// The section containing minute cells.
    private final VBox minuteSection = new VBox();

    /// The grid containing hour cells.
    private final GridPane hourGrid = new GridPane();

    /// The grid containing minute cells.
    private final GridPane minuteGrid = new GridPane();

    /// The AM/PM row shown in 12-hour mode.
    private final HBox periodRow = new HBox();

    /// Refreshes visible text, style classes, and disabled states after control changes.
    private final InvalidationListener refreshListener = observable -> refresh();

    /// Updates logical layout when the effective node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateNodeOrientationLayout();

    /// Creates a time picker skin.
    public M3TimePickerSkin(M3TimePicker control) {
        super(control);
        initializeNodes();
        installListeners(control);
        getChildren().add(container);
        refresh();
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3TimePicker control = getSkinnable();
        control.valueProperty().removeListener(refreshListener);
        control.use24HourClockProperty().removeListener(refreshListener);
        control.minuteStepProperty().removeListener(refreshListener);
        control.minTimeProperty().removeListener(refreshListener);
        control.maxTimeProperty().removeListener(refreshListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationInvalidation);
        container.nodeOrientationProperty().unbind();
        display.nodeOrientationProperty().unbind();
        sections.nodeOrientationProperty().unbind();
        hourSection.nodeOrientationProperty().unbind();
        minuteSection.nodeOrientationProperty().unbind();
        hourGrid.nodeOrientationProperty().unbind();
        minuteGrid.nodeOrientationProperty().unbind();
        periodRow.nodeOrientationProperty().unbind();
        super.dispose();
    }

    /// Returns the number of visible selectable cells currently exposed by the skin.
    public final int getVisibleCellCount() {
        int count = 0;
        for (Node cell : selectableCells()) {
            if (isAccessibleCell(cell)) {
                count++;
            }
        }
        return count;
    }

    /// Returns a visible selectable cell by visible index.
    public final @Nullable Node getVisibleCell(int visibleIndex) {
        if (visibleIndex < 0) {
            return null;
        }

        int currentIndex = 0;
        for (Node cell : selectableCells()) {
            if (!isAccessibleCell(cell)) {
                continue;
            }
            if (currentIndex == visibleIndex) {
                return cell;
            }
            currentIndex++;
        }
        return null;
    }

    /// Returns the first visible enabled cell representing the supplied time.
    public final @Nullable Node getCell(LocalTime time) {
        LocalTime normalizedTime = time.withSecond(0).withNano(0);
        for (Node cell : selectableCells()) {
            if (isAccessibleCell(cell) && normalizedTime.equals(cell.getUserData())) {
                return cell;
            }
        }
        return null;
    }

    /// Returns the first visible enabled selectable cell.
    public final @Nullable Node getFirstEnabledCell() {
        for (Node cell : selectableCells()) {
            if (isAccessibleCell(cell) && !cell.isDisabled()) {
                return cell;
            }
        }
        return null;
    }

    /// Computes the minimum width from the picker container.
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

    /// Computes the minimum height from the picker container.
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

    /// Computes the preferred width from the picker container.
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

    /// Computes the preferred height from the picker container.
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

    /// Lays out the picker container inside the skin bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Creates and styles the reusable skin nodes.
    private void initializeNodes() {
        container.getStyleClass().add(M3TimePicker.CONTAINER_STYLE_CLASS);
        display.getStyleClass().add(M3TimePicker.DISPLAY_STYLE_CLASS);
        hourDisplay.getStyleClass().add(M3TimePicker.HOUR_DISPLAY_STYLE_CLASS);
        displaySeparator.getStyleClass().add(M3TimePicker.DISPLAY_SEPARATOR_STYLE_CLASS);
        minuteDisplay.getStyleClass().add(M3TimePicker.MINUTE_DISPLAY_STYLE_CLASS);
        periodDisplay.getStyleClass().add(M3TimePicker.PERIOD_DISPLAY_STYLE_CLASS);
        hourSection.getStyleClass().add(M3TimePicker.SECTION_STYLE_CLASS);
        minuteSection.getStyleClass().add(M3TimePicker.SECTION_STYLE_CLASS);
        hourGrid.getStyleClass().add(M3TimePicker.GRID_STYLE_CLASS);
        minuteGrid.getStyleClass().add(M3TimePicker.GRID_STYLE_CLASS);
        periodRow.getStyleClass().add(M3TimePicker.PERIOD_ROW_STYLE_CLASS);
        container.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        display.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        sections.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        hourSection.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        minuteSection.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        hourGrid.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        minuteGrid.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());
        periodRow.nodeOrientationProperty().bind(getSkinnable().effectiveNodeOrientationProperty());

        display.setAlignment(Pos.CENTER_LEFT);
        display.getChildren().addAll(hourDisplay, displaySeparator, minuteDisplay, periodDisplay);

        hourSection.getChildren().addAll(createSectionTitle("Hour"), hourGrid);
        minuteSection.getChildren().addAll(createSectionTitle("Minute"), minuteGrid);
        sections.getChildren().addAll(hourSection, minuteSection);
        container.getChildren().addAll(display, sections, periodRow);
        updateNodeOrientationLayout();
    }

    /// Installs listeners that keep the skin synchronized with control state.
    private void installListeners(M3TimePicker control) {
        control.valueProperty().addListener(refreshListener);
        control.use24HourClockProperty().addListener(refreshListener);
        control.minuteStepProperty().addListener(refreshListener);
        control.minTimeProperty().addListener(refreshListener);
        control.maxTimeProperty().addListener(refreshListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
    }

    /// Updates orientation-dependent alignment.
    private void updateNodeOrientationLayout() {
        boolean rightToLeft = getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        display.setAlignment(rightToLeft ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    }

    /// Updates visible labels, selectable cells, and disabled states.
    private void refresh() {
        M3TimePicker control = getSkinnable();
        @Nullable LocalTime selectedTime = control.getValue();
        LocalTime baseTime = selectedTime == null ? fallbackTime(control) : selectedTime;

        hourDisplay.setText(selectedTime == null ? "--" : formatHour(selectedTime, control.isUse24HourClock()));
        minuteDisplay.setText(selectedTime == null ? "--" : formatTwoDigits(selectedTime.getMinute()));
        periodDisplay.setText(control.isUse24HourClock() || selectedTime == null
                ? ""
                : selectedTime.getHour() < 12 ? "AM" : "PM");
        periodDisplay.setManaged(!periodDisplay.getText().isEmpty());
        periodDisplay.setVisible(!periodDisplay.getText().isEmpty());

        refreshHourGrid(control, baseTime, selectedTime);
        refreshMinuteGrid(control, baseTime, selectedTime);
        refreshPeriodRow(control, baseTime, selectedTime);
    }

    /// Rebuilds the hour grid for the active clock mode.
    private void refreshHourGrid(M3TimePicker control, LocalTime baseTime, @Nullable LocalTime selectedTime) {
        hourGrid.getChildren().clear();
        if (control.isUse24HourClock()) {
            for (int hour = 0; hour < 24; hour++) {
                LocalTime candidate = baseTime.withHour(hour);
                TimeCellButton cell = createCell(formatTwoDigits(hour), M3TimePicker.HOUR_CELL_STYLE_CLASS);
                cell.setUserData(candidate);
                cell.setAccessibleText(candidate.toString());
                setStyleClass(cell, M3TimePicker.SELECTED_CELL_STYLE_CLASS,
                        selectedTime != null && selectedTime.getHour() == hour);
                cell.setDisable(!hourHasSelectableMinute(control, hour));
                cell.setOnAction(this::handleTimeCellAction);
                hourGrid.add(cell, hour % TWENTY_FOUR_HOUR_COLUMNS, hour / TWENTY_FOUR_HOUR_COLUMNS);
            }
        } else {
            boolean afternoon = baseTime.getHour() >= 12;
            for (int displayHour = 1; displayHour <= 12; displayHour++) {
                int actualHour = toActualHour(displayHour, afternoon);
                LocalTime candidate = baseTime.withHour(actualHour);
                TimeCellButton cell = createCell(Integer.toString(displayHour), M3TimePicker.HOUR_CELL_STYLE_CLASS);
                cell.setUserData(candidate);
                cell.setAccessibleText(candidate.toString());
                setStyleClass(cell, M3TimePicker.SELECTED_CELL_STYLE_CLASS,
                        selectedTime != null && toDisplayHour(selectedTime.getHour()) == displayHour);
                cell.setDisable(!hourHasSelectableMinute(control, actualHour));
                cell.setOnAction(this::handleTimeCellAction);
                int index = displayHour - 1;
                hourGrid.add(cell, index % COMPACT_GRID_COLUMNS, index / COMPACT_GRID_COLUMNS);
            }
        }
    }

    /// Rebuilds the minute grid for the active minute step.
    private void refreshMinuteGrid(M3TimePicker control, LocalTime baseTime, @Nullable LocalTime selectedTime) {
        minuteGrid.getChildren().clear();
        int index = 0;
        for (int minute = 0; minute < 60; minute += control.getMinuteStep()) {
            LocalTime candidate = baseTime.withMinute(minute);
            TimeCellButton cell = createCell(formatTwoDigits(minute), M3TimePicker.MINUTE_CELL_STYLE_CLASS);
            cell.setUserData(candidate);
            cell.setAccessibleText(candidate.toString());
            setStyleClass(cell, M3TimePicker.SELECTED_CELL_STYLE_CLASS,
                    selectedTime != null && selectedTime.getMinute() == minute);
            cell.setDisable(control.isTimeDisabled(candidate));
            cell.setOnAction(this::handleTimeCellAction);
            minuteGrid.add(cell, index % COMPACT_GRID_COLUMNS, index / COMPACT_GRID_COLUMNS);
            index++;
        }
    }

    /// Rebuilds the AM/PM row for 12-hour mode.
    private void refreshPeriodRow(M3TimePicker control, LocalTime baseTime, @Nullable LocalTime selectedTime) {
        periodRow.getChildren().clear();
        periodRow.setManaged(!control.isUse24HourClock());
        periodRow.setVisible(!control.isUse24HourClock());
        if (control.isUse24HourClock()) {
            return;
        }

        boolean afternoon = selectedTime == null ? baseTime.getHour() >= 12 : selectedTime.getHour() >= 12;
        TimeCellButton am = createCell("AM", M3TimePicker.PERIOD_CELL_STYLE_CLASS);
        am.getStyleClass().add("m3-time-picker-period-start");
        am.setUserData(baseTime.withHour(toActualHour(toDisplayHour(baseTime.getHour()), false)));
        am.setAccessibleText(am.getUserData().toString());
        am.setDisable(!periodHasSelectableTime(control, false));
        setStyleClass(am, M3TimePicker.SELECTED_CELL_STYLE_CLASS, !afternoon);
        am.setOnAction(this::handleTimeCellAction);

        TimeCellButton pm = createCell("PM", M3TimePicker.PERIOD_CELL_STYLE_CLASS);
        pm.getStyleClass().add("m3-time-picker-period-end");
        pm.setUserData(baseTime.withHour(toActualHour(toDisplayHour(baseTime.getHour()), true)));
        pm.setAccessibleText(pm.getUserData().toString());
        pm.setDisable(!periodHasSelectableTime(control, true));
        setStyleClass(pm, M3TimePicker.SELECTED_CELL_STYLE_CLASS, afternoon);
        pm.setOnAction(this::handleTimeCellAction);

        periodRow.getChildren().addAll(am, pm);
    }

    /// Selects the time represented by a cell action.
    private void handleTimeCellAction(ActionEvent event) {
        if (event.getSource() instanceof Node node && node.getUserData() instanceof LocalTime time) {
            M3TimePicker control = getSkinnable();
            if (!control.isTimeDisabled(time)) {
                control.setValue(time);
            }
        }
    }

    /// Returns whether a given hour has at least one selectable minute.
    private boolean hourHasSelectableMinute(M3TimePicker control, int hour) {
        for (int minute = 0; minute < 60; minute += control.getMinuteStep()) {
            if (!control.isTimeDisabled(LocalTime.of(hour, minute))) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether AM or PM contains at least one selectable time.
    private boolean periodHasSelectableTime(M3TimePicker control, boolean afternoon) {
        int startHour = afternoon ? 12 : 0;
        int endHour = afternoon ? 24 : 12;
        for (int hour = startHour; hour < endHour; hour++) {
            if (hourHasSelectableMinute(control, hour)) {
                return true;
            }
        }
        return false;
    }

    /// Returns a fallback time used when no value is selected.
    private static LocalTime fallbackTime(M3TimePicker control) {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        int minute = now.getMinute() / control.getMinuteStep() * control.getMinuteStep();
        return now.withMinute(minute);
    }

    /// Creates a section title label.
    private static Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(M3TimePicker.SECTION_TITLE_STYLE_CLASS);
        return label;
    }

    /// Creates a selectable time cell.
    private static TimeCellButton createCell(String text, String roleStyleClass) {
        TimeCellButton cell = new TimeCellButton(text);
        cell.getStyleClass().add(roleStyleClass);
        return cell;
    }

    /// Returns selectable cells in their visible traversal order.
    private List<Node> selectableCells() {
        ArrayList<Node> cells = new ArrayList<>();
        cells.addAll(hourGrid.getChildren());
        cells.addAll(minuteGrid.getChildren());
        cells.addAll(periodRow.getChildren());
        return cells;
    }

    /// Returns whether a time cell is visible to users and accessibility clients.
    private static boolean isAccessibleCell(Node cell) {
        return cell.isVisible() && !cell.isMouseTransparent();
    }

    /// Formats an hour for the active display mode.
    private static String formatHour(LocalTime time, boolean use24HourClock) {
        if (use24HourClock) {
            return formatTwoDigits(time.getHour());
        }
        return Integer.toString(toDisplayHour(time.getHour()));
    }

    /// Converts a 24-hour value to a 12-hour display value.
    private static int toDisplayHour(int actualHour) {
        int displayHour = actualHour % 12;
        return displayHour == 0 ? 12 : displayHour;
    }

    /// Converts a 12-hour display value and period to a 24-hour value.
    private static int toActualHour(int displayHour, boolean afternoon) {
        int normalizedHour = displayHour == 12 ? 0 : displayHour;
        return afternoon ? normalizedHour + 12 : normalizedHour;
    }

    /// Formats a number with two decimal digits.
    private static String formatTwoDigits(int value) {
        return value < 10 ? "0" + value : Integer.toString(value);
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

    /// An internal time cell button that avoids inheriting generic button user-agent rules.
    @NotNullByDefault
    private static final class TimeCellButton extends ButtonBase {
        /// Creates a selectable time cell.
        private TimeCellButton(String text) {
            super(text);
            getStyleClass().add(M3TimePicker.CELL_STYLE_CLASS);
            setAccessibleRole(AccessibleRole.BUTTON);
            setAlignment(Pos.CENTER);
            setFocusTraversable(true);
            setMnemonicParsing(false);
            setTextOverrun(OverrunStyle.CLIP);
        }

        /// Fires this time cell's action handler.
        @Override
        public void fire() {
            if (!isDisabled()) {
                fireEvent(new ActionEvent(this, this));
            }
        }

        /// Creates the animated time cell skin.
        @Override
        protected Skin<?> createDefaultSkin() {
            return new TimeCellSkin(this);
        }

        /// Returns the user-agent stylesheet for time cell states.
        @Override
        public String getUserAgentStylesheet() {
            return M3Stylesheets.controlStylesheet("time-picker.css");
        }
    }

    /// The animated labeled skin used by time cell buttons.
    @NotNullByDefault
    private static final class TimeCellSkin extends M3LabeledButtonSkinBase<TimeCellButton> {
        /// Creates a time cell skin.
        private TimeCellSkin(TimeCellButton control) {
            super(control);
        }
    }
}
