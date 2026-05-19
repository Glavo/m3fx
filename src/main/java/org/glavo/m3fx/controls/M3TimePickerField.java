// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Skin;
import org.glavo.m3fx.skins.M3PickerFieldSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/// A Material Design 3 time picker field that combines an editable text field with a popup time picker.
@NotNullByDefault
public final class M3TimePickerField extends M3PickerField<LocalTime, M3TimePicker> {
    /// The style class applied to time picker field controls.
    public static final String STYLE_CLASS = "m3-time-picker-field";

    /// The style class applied to time picker field popup surfaces.
    public static final String POPUP_STYLE_CLASS = "m3-time-picker-field-popup";

    /// Creates an empty time picker field.
    public M3TimePickerField() {
        this(new M3TimePicker());
    }

    /// Creates a time picker field initialized with the supplied value.
    public M3TimePickerField(LocalTime value) {
        this(new M3TimePicker());
        setValue(value);
    }

    /// Creates a time picker field around a fresh popup time picker.
    private M3TimePickerField(M3TimePicker picker) {
        super(
                picker,
                picker.valueProperty(),
                DateTimeFormatter.ofPattern("HH:mm"),
                STYLE_CLASS,
                POPUP_STYLE_CLASS,
                "v",
                "Open time picker",
                "Enter a valid time",
                "Time is outside the selectable range"
        );
    }

    /// Returns whether the popup picker displays 24-hour time.
    public boolean isUse24HourClock() {
        return getPicker().isUse24HourClock();
    }

    /// Sets whether the popup picker displays 24-hour time.
    public void setUse24HourClock(boolean use24HourClock) {
        getPicker().setUse24HourClock(use24HourClock);
    }

    /// Returns the 24-hour display property from the popup time picker.
    public BooleanProperty use24HourClockProperty() {
        return getPicker().use24HourClockProperty();
    }

    /// Returns the minute interval used by the popup minute grid.
    public int getMinuteStep() {
        return getPicker().getMinuteStep();
    }

    /// Sets the minute interval used by the popup minute grid.
    public void setMinuteStep(int minuteStep) {
        getPicker().setMinuteStep(minuteStep);
    }

    /// Returns the minute step property from the popup time picker.
    public IntegerProperty minuteStepProperty() {
        return getPicker().minuteStepProperty();
    }

    /// Returns the earliest selectable time, or `null` when there is no lower bound.
    public @Nullable LocalTime getMinTime() {
        return getPicker().getMinTime();
    }

    /// Sets the earliest selectable time, or clears the lower bound when `null` is supplied.
    public void setMinTime(@Nullable LocalTime minTime) {
        getPicker().setMinTime(minTime);
    }

    /// Returns the minimum time property from the popup time picker.
    public ObjectProperty<@Nullable LocalTime> minTimeProperty() {
        return getPicker().minTimeProperty();
    }

    /// Returns the latest selectable time, or `null` when there is no upper bound.
    public @Nullable LocalTime getMaxTime() {
        return getPicker().getMaxTime();
    }

    /// Sets the latest selectable time, or clears the upper bound when `null` is supplied.
    public void setMaxTime(@Nullable LocalTime maxTime) {
        getPicker().setMaxTime(maxTime);
    }

    /// Returns the maximum time property from the popup time picker.
    public ObjectProperty<@Nullable LocalTime> maxTimeProperty() {
        return getPicker().maxTimeProperty();
    }

    /// Sets the selected time from hour and minute fields.
    public void setTime(int hour, int minute) {
        setValue(LocalTime.of(hour, minute));
    }

    /// Selects the current time with seconds and nanos cleared.
    public void selectNow() {
        setValue(normalizeValue(LocalTime.now()));
    }

    /// Clears the selected time.
    public void clearValue() {
        setValue(null);
    }

    /// Returns whether the supplied time is outside the configured selectable range.
    public boolean isTimeDisabled(LocalTime time) {
        return getPicker().isTimeDisabled(time);
    }

    /// Creates the default Material Design 3 picker field skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3PickerFieldSkin<>(this);
    }

    /// Parses one editor time string.
    @Override
    protected LocalTime parseValue(String text, DateTimeFormatter formatter) {
        return LocalTime.from(formatter.parse(text));
    }

    /// Formats one time value for editor display.
    @Override
    protected String formatValue(LocalTime value, DateTimeFormatter formatter) {
        return formatter.format(value);
    }

    /// Clears seconds and nanos because this field edits hour and minute precision.
    @Override
    protected LocalTime normalizeValue(LocalTime value) {
        return Objects.requireNonNull(value, "value").withSecond(0).withNano(0);
    }

    /// Returns whether a time is outside the popup picker's selectable range.
    @Override
    protected boolean isPickerValueDisabled(LocalTime value) {
        return getPicker().isTimeDisabled(value);
    }

    /// Applies a value to the popup time picker.
    @Override
    protected void setPickerValue(@Nullable LocalTime value) {
        getPicker().setValue(value);
    }
}
