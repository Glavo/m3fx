// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TimePickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.Objects;

/// A Material Design 3 time picker control.
@NotNullByDefault
public class M3TimePicker extends Control {
    /// The base style class for M3FX time pickers.
    public static final String STYLE_CLASS = "m3-time-picker";

    /// The style class applied to the internal layout container.
    public static final String CONTAINER_STYLE_CLASS = "m3-time-picker-container";

    /// The style class applied to the selected time display row.
    public static final String DISPLAY_STYLE_CLASS = "m3-time-picker-display";

    /// The style class applied to the hour display label.
    public static final String HOUR_DISPLAY_STYLE_CLASS = "m3-time-picker-hour-display";

    /// The style class applied to the display separator label.
    public static final String DISPLAY_SEPARATOR_STYLE_CLASS = "m3-time-picker-display-separator";

    /// The style class applied to the minute display label.
    public static final String MINUTE_DISPLAY_STYLE_CLASS = "m3-time-picker-minute-display";

    /// The style class applied to the period display label in 12-hour mode.
    public static final String PERIOD_DISPLAY_STYLE_CLASS = "m3-time-picker-period-display";

    /// The style class applied to an hour or minute section.
    public static final String SECTION_STYLE_CLASS = "m3-time-picker-section";

    /// The style class applied to section title labels.
    public static final String SECTION_TITLE_STYLE_CLASS = "m3-time-picker-section-title";

    /// The style class applied to hour and minute grids.
    public static final String GRID_STYLE_CLASS = "m3-time-picker-grid";

    /// The style class applied to every selectable time cell.
    public static final String CELL_STYLE_CLASS = "m3-time-picker-cell";

    /// The style class applied to selectable hour cells.
    public static final String HOUR_CELL_STYLE_CLASS = "m3-time-picker-hour-cell";

    /// The style class applied to selectable minute cells.
    public static final String MINUTE_CELL_STYLE_CLASS = "m3-time-picker-minute-cell";

    /// The style class applied to the AM/PM row in 12-hour mode.
    public static final String PERIOD_ROW_STYLE_CLASS = "m3-time-picker-period-row";

    /// The style class applied to AM and PM cells.
    public static final String PERIOD_CELL_STYLE_CLASS = "m3-time-picker-period-cell";

    /// The style class applied to a selected cell.
    public static final String SELECTED_CELL_STYLE_CLASS = "m3-time-picker-selected-cell";

    /// The default minute interval shown in the minute grid.
    private static final int DEFAULT_MINUTE_STEP = 5;

    /// The selected time, or `null` when no time is selected.
    private final ObjectProperty<@Nullable LocalTime> value =
            new SimpleObjectProperty<>(this, "value") {
                /// Normalizes seconds and notifies accessibility clients.
                @Override
                protected void invalidated() {
                    @Nullable LocalTime time = get();
                    if (time != null) {
                        LocalTime normalized = normalizeTime(time);
                        if (!normalized.equals(time)) {
                            set(normalized);
                            return;
                        }
                    }
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                }
            };

    /// Whether the picker displays 24-hour time.
    private final BooleanProperty use24HourClock =
            new SimpleBooleanProperty(this, "use24HourClock", false) {
                /// Notifies accessibility clients when display formatting changes.
                @Override
                protected void invalidated() {
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                }
            };

    /// The minute interval used by the minute selection grid.
    private final IntegerProperty minuteStep = new IntegerPropertyBase(DEFAULT_MINUTE_STEP) {
        /// Validates the minute step whenever it changes.
        @Override
        protected void invalidated() {
            validateMinuteStep(get());
        }

        /// Returns the owning bean.
        @Override
        public Object getBean() {
            return M3TimePicker.this;
        }

        /// Returns the property name.
        @Override
        public String getName() {
            return "minuteStep";
        }
    };

    /// The earliest selectable time, or `null` when there is no lower bound.
    private final ObjectProperty<@Nullable LocalTime> minTime =
            new SimpleObjectProperty<>(this, "minTime") {
                /// Clears the selected value when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                }
            };

    /// The latest selectable time, or `null` when there is no upper bound.
    private final ObjectProperty<@Nullable LocalTime> maxTime =
            new SimpleObjectProperty<>(this, "maxTime") {
                /// Clears the selected value when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                }
            };

    /// Creates a time picker with no selected time.
    public M3TimePicker() {
        initialize();
    }

    /// Creates a time picker initialized with a selected time.
    public M3TimePicker(LocalTime value) {
        initialize();
        setValue(value);
    }

    /// Returns the selected time, or `null` when no time is selected.
    public final @Nullable LocalTime getValue() {
        return value.get();
    }

    /// Sets the selected time, or clears selection when `null` is supplied.
    public final void setValue(@Nullable LocalTime value) {
        if (value != null && isTimeDisabled(value)) {
            throw new IllegalArgumentException("value is outside the selectable range");
        }
        this.value.set(value == null ? null : normalizeTime(value));
    }

    /// Returns the selected time property.
    public final ObjectProperty<@Nullable LocalTime> valueProperty() {
        return value;
    }

    /// Returns whether the picker displays 24-hour time.
    public final boolean isUse24HourClock() {
        return use24HourClock.get();
    }

    /// Sets whether the picker displays 24-hour time.
    public final void setUse24HourClock(boolean use24HourClock) {
        this.use24HourClock.set(use24HourClock);
    }

    /// Returns the 24-hour display property.
    public final BooleanProperty use24HourClockProperty() {
        return use24HourClock;
    }

    /// Returns the minute interval used by the minute grid.
    public final int getMinuteStep() {
        return minuteStep.get();
    }

    /// Sets the minute interval used by the minute grid.
    public final void setMinuteStep(int minuteStep) {
        validateMinuteStep(minuteStep);
        this.minuteStep.set(minuteStep);
    }

    /// Returns the minute step property.
    public final IntegerProperty minuteStepProperty() {
        return minuteStep;
    }

    /// Returns the earliest selectable time, or `null` when there is no lower bound.
    public final @Nullable LocalTime getMinTime() {
        return minTime.get();
    }

    /// Sets the earliest selectable time, or clears the lower bound when `null` is supplied.
    public final void setMinTime(@Nullable LocalTime minTime) {
        @Nullable LocalTime normalizedMinTime = minTime == null ? null : normalizeTime(minTime);
        validateTimeRange(normalizedMinTime, getMaxTime());
        this.minTime.set(normalizedMinTime);
    }

    /// Returns the minimum time property.
    public final ObjectProperty<@Nullable LocalTime> minTimeProperty() {
        return minTime;
    }

    /// Returns the latest selectable time, or `null` when there is no upper bound.
    public final @Nullable LocalTime getMaxTime() {
        return maxTime.get();
    }

    /// Sets the latest selectable time, or clears the upper bound when `null` is supplied.
    public final void setMaxTime(@Nullable LocalTime maxTime) {
        @Nullable LocalTime normalizedMaxTime = maxTime == null ? null : normalizeTime(maxTime);
        validateTimeRange(getMinTime(), normalizedMaxTime);
        this.maxTime.set(normalizedMaxTime);
    }

    /// Returns the maximum time property.
    public final ObjectProperty<@Nullable LocalTime> maxTimeProperty() {
        return maxTime;
    }

    /// Sets the selected time from hour and minute fields.
    public final void setTime(int hour, int minute) {
        validateHour(hour);
        validateMinute(minute);
        setValue(LocalTime.of(hour, minute));
    }

    /// Selects the current time with seconds and nanos cleared.
    public final void selectNow() {
        setValue(normalizeTime(LocalTime.now()));
    }

    /// Clears the selected time.
    public final void clearValue() {
        setValue(null);
    }

    /// Returns whether the supplied time is outside the configured selectable range.
    public final boolean isTimeDisabled(LocalTime time) {
        Objects.requireNonNull(time, "time");
        LocalTime normalizedTime = normalizeTime(time);
        @Nullable LocalTime min = getMinTime();
        @Nullable LocalTime max = getMaxTime();
        return min != null && normalizedTime.isBefore(min) || max != null && normalizedTime.isAfter(max);
    }

    /// Returns the user-agent stylesheet for M3FX time pickers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("time-picker.css");
    }

    /// Returns accessibility text for the selected time.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates the default Material Design 3 time picker skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TimePickerSkin(this);
    }

    /// Adds base style classes, accessibility role, and keyboard navigation.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
    }

    /// Handles keyboard time navigation.
    private void handleNavigationKeyPressed(KeyEvent event) {
        boolean handled = switch (event.getCode()) {
            case LEFT -> {
                moveByHours(-1);
                yield true;
            }
            case RIGHT -> {
                moveByHours(1);
                yield true;
            }
            case UP -> {
                moveByMinutes(getMinuteStep());
                yield true;
            }
            case DOWN -> {
                moveByMinutes(-getMinuteStep());
                yield true;
            }
            case HOME -> {
                selectIfEnabled(LocalTime.MIDNIGHT);
                yield true;
            }
            case END -> {
                selectIfEnabled(LocalTime.of(23, 59));
                yield true;
            }
            default -> false;
        };

        if (handled) {
            event.consume();
        }
    }

    /// Moves the selected or implied time by a number of hours.
    private void moveByHours(int hours) {
        selectIfEnabled(navigationBaseTime().plusHours(hours));
    }

    /// Moves the selected or implied time by a number of minutes.
    private void moveByMinutes(int minutes) {
        selectIfEnabled(navigationBaseTime().plusMinutes(minutes));
    }

    /// Selects the supplied time when it is inside the configured range.
    private void selectIfEnabled(LocalTime time) {
        LocalTime normalizedTime = normalizeTime(time);
        if (!isTimeDisabled(normalizedTime)) {
            setValue(normalizedTime);
        }
    }

    /// Returns the base time used when keyboard navigation starts without a selected time.
    private LocalTime navigationBaseTime() {
        @Nullable LocalTime selectedTime = getValue();
        if (selectedTime != null) {
            return selectedTime;
        }

        return snapMinuteToStep(normalizeTime(LocalTime.now()), getMinuteStep());
    }

    /// Clears the selected time when min or max bounds exclude it.
    private void clearValueIfOutOfRange() {
        @Nullable LocalTime selectedTime = getValue();
        if (selectedTime != null && isTimeDisabled(selectedTime)) {
            value.set(null);
        }
    }

    /// Returns accessible text for the selected time or empty selection.
    private String accessibleText() {
        @Nullable LocalTime selectedTime = getValue();
        return selectedTime == null ? "" : selectedTime.toString();
    }

    /// Clears seconds and nanos because this picker edits hour and minute precision.
    private static LocalTime normalizeTime(LocalTime time) {
        return Objects.requireNonNull(time, "time").withSecond(0).withNano(0);
    }

    /// Snaps a time down to the nearest configured minute step.
    private static LocalTime snapMinuteToStep(LocalTime time, int minuteStep) {
        int snappedMinute = time.getMinute() / minuteStep * minuteStep;
        return time.withMinute(snappedMinute);
    }

    /// Validates an optional inclusive time range.
    private static void validateTimeRange(@Nullable LocalTime minTime, @Nullable LocalTime maxTime) {
        if (minTime != null && maxTime != null && minTime.isAfter(maxTime)) {
            throw new IllegalArgumentException("minTime must not be after maxTime");
        }
    }

    /// Validates an hour value.
    private static void validateHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("hour must be between 0 and 23");
        }
    }

    /// Validates a minute value.
    private static void validateMinute(int minute) {
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("minute must be between 0 and 59");
        }
    }

    /// Validates a minute step value.
    private static void validateMinuteStep(int minuteStep) {
        if (minuteStep <= 0 || minuteStep > 30 || 60 % minuteStep != 0) {
            throw new IllegalArgumentException("minuteStep must evenly divide 60 and be between 1 and 30");
        }
    }
}
