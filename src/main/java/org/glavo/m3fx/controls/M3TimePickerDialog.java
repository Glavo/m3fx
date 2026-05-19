// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.Objects;

/// A Material Design 3 dialog preset for selecting one time.
@NotNullByDefault
public class M3TimePickerDialog extends M3Dialog<LocalTime> {
    /// The default title and header text for time picker dialogs.
    public static final String DEFAULT_TITLE = "Select time";

    /// The time picker displayed as dialog content.
    private final M3TimePicker picker = new M3TimePicker();

    /// Creates an empty time picker dialog.
    public M3TimePickerDialog() {
        initialize();
    }

    /// Creates a time picker dialog initialized with the supplied selected time.
    public M3TimePickerDialog(@Nullable LocalTime value) {
        initialize();
        setValue(value);
    }

    /// Returns the time picker displayed by this dialog.
    public final M3TimePicker getPicker() {
        return picker;
    }

    /// Returns the selected time, or `null` when no time is selected.
    public final @Nullable LocalTime getValue() {
        return picker.getValue();
    }

    /// Sets the selected time, or clears selection when `null` is supplied.
    public final void setValue(@Nullable LocalTime value) {
        picker.setValue(value);
    }

    /// Returns the selected time property.
    public final ObjectProperty<@Nullable LocalTime> valueProperty() {
        return picker.valueProperty();
    }

    /// Clears the selected time.
    public final void clearValue() {
        picker.clearValue();
    }

    /// Returns whether the picker displays 24-hour time.
    public final boolean isUse24HourClock() {
        return picker.isUse24HourClock();
    }

    /// Sets whether the picker displays 24-hour time.
    public final void setUse24HourClock(boolean use24HourClock) {
        picker.setUse24HourClock(use24HourClock);
    }

    /// Returns the 24-hour display property from the picker.
    public final BooleanProperty use24HourClockProperty() {
        return picker.use24HourClockProperty();
    }

    /// Returns the minute interval used by the picker minute grid.
    public final int getMinuteStep() {
        return picker.getMinuteStep();
    }

    /// Sets the minute interval used by the picker minute grid.
    public final void setMinuteStep(int minuteStep) {
        picker.setMinuteStep(minuteStep);
    }

    /// Returns the minute step property from the picker.
    public final IntegerProperty minuteStepProperty() {
        return picker.minuteStepProperty();
    }

    /// Returns the earliest selectable time, or `null` when there is no lower bound.
    public final @Nullable LocalTime getMinTime() {
        return picker.getMinTime();
    }

    /// Sets the earliest selectable time, or clears the lower bound when `null` is supplied.
    public final void setMinTime(@Nullable LocalTime minTime) {
        picker.setMinTime(minTime);
    }

    /// Returns the minimum time property from the picker.
    public final ObjectProperty<@Nullable LocalTime> minTimeProperty() {
        return picker.minTimeProperty();
    }

    /// Returns the latest selectable time, or `null` when there is no upper bound.
    public final @Nullable LocalTime getMaxTime() {
        return picker.getMaxTime();
    }

    /// Sets the latest selectable time, or clears the upper bound when `null` is supplied.
    public final void setMaxTime(@Nullable LocalTime maxTime) {
        picker.setMaxTime(maxTime);
    }

    /// Returns the maximum time property from the picker.
    public final ObjectProperty<@Nullable LocalTime> maxTimeProperty() {
        return picker.maxTimeProperty();
    }

    /// Sets the selected time from hour and minute fields.
    public final void setTime(int hour, int minute) {
        picker.setTime(hour, minute);
    }

    /// Selects the current time with seconds and nanos cleared.
    public final void selectNow() {
        picker.selectNow();
    }

    /// Returns whether the supplied time is outside the configured selectable range.
    public final boolean isTimeDisabled(LocalTime time) {
        return picker.isTimeDisabled(Objects.requireNonNull(time, "time"));
    }

    /// Configures dialog content, buttons, result conversion, and button state.
    @SuppressWarnings("DataFlowIssue")
    private void initialize() {
        setTitle(DEFAULT_TITLE);
        M3DialogPane pane = getM3DialogPane();
        pane.setHeaderText(DEFAULT_TITLE);
        pane.setContent(picker);
        pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(this::convertResult);
        picker.valueProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        updateOkButtonState();
    }

    /// Converts a dialog button into the selected time result.
    private @Nullable LocalTime convertResult(@Nullable ButtonType buttonType) {
        return buttonType == ButtonType.OK ? getValue() : null;
    }

    /// Enables the OK button only when a selected time exists.
    private void updateOkButtonState() {
        @Nullable Node okButton = getM3DialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(getValue() == null);
        }
    }
}
