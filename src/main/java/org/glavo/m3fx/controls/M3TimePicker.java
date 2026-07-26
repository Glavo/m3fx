// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3PickerAccessibilityPresentation;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TimePickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 time picker control.
///
/// `M3TimePicker` provides the Material dial and keyboard-input variants, optional 12-hour period selection,
/// selectable-time bounds, and a nullable selected [LocalTime] value. The control is the picker body used by
/// [M3TimePickerDialog] and [M3TimePickerField].
///
/// Selection is stored at minute precision. By default the picker uses a 12-hour dial, a five-minute adjustment
/// step, no selected value, and no lower or upper bound. Changing a bound clears an existing selection that no
/// longer belongs to the inclusive range. The input mode changes presentation and editing behavior without changing
/// the selected value.
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
@NotNullByDefault
public final class M3TimePicker extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-time-picker";

    /// The pseudo-class applied while the keyboard input variant is active.
    private static final PseudoClass INPUT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("input-mode");

    /// The pseudo-class applied while the clock dial variant is active.
    private static final PseudoClass DIAL_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("dial-mode");

    /// The default minute interval used by dial and keyboard adjustments.
    private static final int DEFAULT_MINUTE_STEP = 5;

    /// The default spacing between major Time Picker regions.
    private static final double DEFAULT_CONTAINER_SPACING = 24.0;

    /// The default diameter of the clock dial selector handle.
    private static final double DEFAULT_DIAL_HANDLE_SIZE = 48.0;

    /// The default diameter of the clock dial center dot.
    private static final double DEFAULT_DIAL_CENTER_SIZE = 8.0;

    /// Creates a time picker with no selected time, no bounds, a 12-hour dial, and a five-minute adjustment step.
    public M3TimePicker() {
        initialize();
    }

    /// Creates a time picker initialized with a selected time.
    ///
    /// Seconds and nanoseconds are discarded.
    ///
    /// @param value the initial selected time
    /// @throws NullPointerException if `value` is `null`
    public M3TimePicker(LocalTime value) {
        initialize();
        setValue(Objects.requireNonNull(value, "value"));
    }

    /// The selected time, or `null` when no time is selected.
    ///
    /// Values are normalized to minute precision. [#setValue(LocalTime)] rejects values outside the configured
    /// range; direct property writes are normalized but do not perform that range check.
    ///
    /// @defaultValue `null`
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
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
                    notifyAccessibleItemsChanged();
                }
            };

    /// Returns the selected time, or `null` when no time is selected.
    ///
    /// @return the minute-precision selected time, or `null`
    public final @Nullable LocalTime getValue() {
        return value.get();
    }

    /// Sets the selected time, or clears selection when `null` is supplied.
    ///
    /// Seconds and nanoseconds are discarded before the value is stored. Both configured bounds are inclusive.
    ///
    /// @param value the time to select, or `null` to clear selection
    /// @throws IllegalArgumentException if `value` is outside the configured selectable range
    public final void setValue(@Nullable LocalTime value) {
        if (value != null && isTimeDisabled(value)) {
            throw new IllegalArgumentException("value is outside the selectable range");
        }
        this.value.set(value == null ? null : normalizeTime(value));
    }

    /// Returns the `value` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`. A binding must supply values
    /// with zero seconds and nanoseconds that remain within the current bounds; values that require normalization
    /// cannot be written back while the property is bound.
    ///
    /// @return the `value` property
    public final ObjectProperty<@Nullable LocalTime> valueProperty() {
        return value;
    }

    /// Whether the picker displays and interprets input as 24-hour time.
    ///
    /// @defaultValue `false`
    private final BooleanProperty use24HourClock =
            new SimpleBooleanProperty(this, "use24HourClock", false) {
                /// Notifies accessibility clients when display formatting changes.
                @Override
                protected void invalidated() {
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                    notifyAccessibleItemsChanged();
                }
            };

    /// Returns whether the picker displays 24-hour time.
    ///
    /// @return whether the picker uses 24-hour labels; the default is `false`
    public final boolean isUse24HourClock() {
        return use24HourClock.get();
    }

    /// Sets whether the picker displays 24-hour time.
    ///
    /// This affects display and input interpretation without changing the selected [LocalTime].
    ///
    /// @param use24HourClock whether to use 24-hour labels
    public final void setUse24HourClock(boolean use24HourClock) {
        this.use24HourClock.set(use24HourClock);
    }

    /// Returns the `use24HourClock` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `use24HourClock` property
    public final BooleanProperty use24HourClockProperty() {
        return use24HourClock;
    }

    /// Whether the keyboard input variant is active instead of the dial.
    ///
    /// @defaultValue `false`
    private final BooleanProperty inputMode =
            new SimpleBooleanProperty(this, "inputMode", false) {
                /// Updates variant pseudo-classes and accessibility when the mode changes.
                @Override
                protected void invalidated() {
                    updateInputModePseudoClasses();
                    notifyAccessibleItemsChanged();
                }
            };

    /// Returns whether the keyboard input variant is active.
    ///
    /// @return `true` for keyboard input mode or `false` for dial mode
    public final boolean isInputMode() {
        return inputMode.get();
    }

    /// Sets whether the keyboard input variant is active.
    ///
    /// @param inputMode `true` for keyboard input mode or `false` for dial mode
    public final void setInputMode(boolean inputMode) {
        this.inputMode.set(inputMode);
    }

    /// Returns the `inputMode` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `inputMode` property
    public final BooleanProperty inputModeProperty() {
        return inputMode;
    }

    /// The minute interval used by dial and keyboard adjustments.
    ///
    /// The value must be a positive divisor of 60 no greater than 30. Changing it does not rewrite the current
    /// selected value.
    ///
    /// @defaultValue `5`
    private final IntegerProperty minuteStep = new IntegerPropertyBase(DEFAULT_MINUTE_STEP) {
        /// Validates the minute step whenever it changes.
        @Override
        protected void invalidated() {
            validateMinuteStep(get());
            notifyAccessibleItemsChanged();
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

    /// Returns the minute interval used by dial and keyboard adjustments.
    ///
    /// @return a positive divisor of 60 no greater than 30; the default is 5
    public final int getMinuteStep() {
        return minuteStep.get();
    }

    /// Sets the minute interval used by dial and keyboard adjustments.
    ///
    /// This value controls generated minute choices and adjustment steps. It does not rewrite an existing selected
    /// value.
    ///
    /// @param minuteStep a positive divisor of 60 between 1 and 30, inclusive
    /// @throws IllegalArgumentException if `minuteStep` is outside the supported range or does not divide 60
    public final void setMinuteStep(int minuteStep) {
        validateMinuteStep(minuteStep);
        this.minuteStep.set(minuteStep);
    }

    /// Returns the `minuteStep` property.
    ///
    /// The returned property is observable and bindable. Its default value is `5`.
    ///
    /// @return the `minuteStep` property
    public final IntegerProperty minuteStepProperty() {
        return minuteStep;
    }

    /// The spacing between major time-picker regions in logical pixels.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty containerSpacingStyleable;

    /// Returns the spacing between major Time Picker regions.
    ///
    /// @return the region spacing in pixels
    public final double getContainerSpacing() {
        return containerSpacingStyleable == null ? DEFAULT_CONTAINER_SPACING : containerSpacingStyleable.get();
    }

    /// Sets the spacing between major Time Picker regions.
    ///
    /// @param spacing the non-negative region spacing in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerSpacing(double spacing) {
        containerSpacingProperty().set(M3Css.nonNegative(spacing, "containerSpacing"));
    }

    /// Returns the styleable spacing property for major Time Picker regions.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `24.0` logical pixels.
    ///
    /// @return the region spacing property
    public final StyleableDoubleProperty containerSpacingProperty() {
        if (containerSpacingStyleable == null) {
            containerSpacingStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SPACING,
                    this,
                    "containerSpacing",
                    StyleableProperties.CONTAINER_SPACING,
                    this::requestLayout
            );
        }
        return containerSpacingStyleable;
    }

    /// The clock-dial selector handle diameter in logical pixels.
    ///
    /// @defaultValue `48.0`
    private @Nullable StyleableDoubleProperty dialHandleSizeStyleable;

    /// Returns the clock dial selector handle diameter.
    ///
    /// @return the handle diameter in pixels
    public final double getDialHandleSize() {
        return dialHandleSizeStyleable == null ? DEFAULT_DIAL_HANDLE_SIZE : dialHandleSizeStyleable.get();
    }

    /// Sets the clock dial selector handle diameter.
    ///
    /// @param size the non-negative handle diameter in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setDialHandleSize(double size) {
        dialHandleSizeProperty().set(M3Css.nonNegative(size, "dialHandleSize"));
    }

    /// Returns the styleable clock dial selector handle diameter property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `48.0` logical pixels.
    ///
    /// @return the handle diameter property
    public final StyleableDoubleProperty dialHandleSizeProperty() {
        if (dialHandleSizeStyleable == null) {
            dialHandleSizeStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_DIAL_HANDLE_SIZE,
                    this,
                    "dialHandleSize",
                    StyleableProperties.DIAL_HANDLE_SIZE,
                    this::requestLayout
            );
        }
        return dialHandleSizeStyleable;
    }

    /// The clock-dial center-dot diameter in logical pixels.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty dialCenterSizeStyleable;

    /// Returns the clock dial center dot diameter.
    ///
    /// @return the center dot diameter in pixels
    public final double getDialCenterSize() {
        return dialCenterSizeStyleable == null ? DEFAULT_DIAL_CENTER_SIZE : dialCenterSizeStyleable.get();
    }

    /// Sets the clock dial center dot diameter.
    ///
    /// @param size the non-negative center dot diameter in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setDialCenterSize(double size) {
        dialCenterSizeProperty().set(M3Css.nonNegative(size, "dialCenterSize"));
    }

    /// Returns the styleable clock dial center dot diameter property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `8.0` logical pixels.
    ///
    /// @return the center dot diameter property
    public final StyleableDoubleProperty dialCenterSizeProperty() {
        if (dialCenterSizeStyleable == null) {
            dialCenterSizeStyleable = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_DIAL_CENTER_SIZE,
                    this,
                    "dialCenterSize",
                    StyleableProperties.DIAL_CENTER_SIZE,
                    this::requestLayout
            );
        }
        return dialCenterSizeStyleable;
    }

    /// The earliest selectable time, or `null` when there is no lower bound.
    ///
    /// [#setMinTime(LocalTime)] normalizes to minute precision and validates the relationship with the upper bound.
    /// Direct property writes do not normalize or validate the bound relationship. Either path clears an existing
    /// selection that is before the new bound.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalTime> minTime =
            new SimpleObjectProperty<>(this, "minTime") {
                /// Clears the selected value when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                    notifyAccessibleItemsChanged();
                }
            };

    /// Returns the earliest selectable time, or `null` when there is no lower bound.
    ///
    /// @return the inclusive minute-precision lower bound, or `null`
    public final @Nullable LocalTime getMinTime() {
        return minTime.get();
    }

    /// Sets the earliest selectable time, or clears the lower bound when `null` is supplied.
    ///
    /// Seconds and nanoseconds are discarded. If the current selection falls outside the resulting range, it is
    /// cleared.
    ///
    /// @param minTime the inclusive lower bound, or `null` for no lower bound
    /// @throws IllegalArgumentException if the normalized lower bound is after the current upper bound
    /// @throws RuntimeException         if the selected value becomes invalid and [#valueProperty()] is bound
    public final void setMinTime(@Nullable LocalTime minTime) {
        @Nullable LocalTime normalizedMinTime = minTime == null ? null : normalizeTime(minTime);
        validateTimeRange(normalizedMinTime, getMaxTime());
        this.minTime.set(normalizedMinTime);
    }

    /// Returns the `minTime` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`. Direct property writes bypass
    /// minute normalization and bound-order validation. If [#valueProperty()] is bound, its binding must update the
    /// selected time before this property excludes it.
    ///
    /// @return the `minTime` property
    public final ObjectProperty<@Nullable LocalTime> minTimeProperty() {
        return minTime;
    }

    /// The latest selectable time, or `null` when there is no upper bound.
    ///
    /// [#setMaxTime(LocalTime)] normalizes to minute precision and validates the relationship with the lower bound.
    /// Direct property writes do not normalize or validate the bound relationship. Either path clears an existing
    /// selection that is after the new bound.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalTime> maxTime =
            new SimpleObjectProperty<>(this, "maxTime") {
                /// Clears the selected value when it no longer satisfies the range.
                @Override
                protected void invalidated() {
                    clearValueIfOutOfRange();
                    notifyAccessibleItemsChanged();
                }
            };

    /// Returns the latest selectable time, or `null` when there is no upper bound.
    ///
    /// @return the inclusive minute-precision upper bound, or `null`
    public final @Nullable LocalTime getMaxTime() {
        return maxTime.get();
    }

    /// Sets the latest selectable time, or clears the upper bound when `null` is supplied.
    ///
    /// Seconds and nanoseconds are discarded. If the current selection falls outside the resulting range, it is
    /// cleared.
    ///
    /// @param maxTime the inclusive upper bound, or `null` for no upper bound
    /// @throws IllegalArgumentException if the normalized upper bound is before the current lower bound
    /// @throws RuntimeException         if the selected value becomes invalid and [#valueProperty()] is bound
    public final void setMaxTime(@Nullable LocalTime maxTime) {
        @Nullable LocalTime normalizedMaxTime = maxTime == null ? null : normalizeTime(maxTime);
        validateTimeRange(getMinTime(), normalizedMaxTime);
        this.maxTime.set(normalizedMaxTime);
    }

    /// Returns the `maxTime` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`. Direct property writes bypass
    /// minute normalization and bound-order validation. If [#valueProperty()] is bound, its binding must update the
    /// selected time before this property excludes it.
    ///
    /// @return the `maxTime` property
    public final ObjectProperty<@Nullable LocalTime> maxTimeProperty() {
        return maxTime;
    }

    /// Sets the selected time from hour and minute fields.
    ///
    /// @param hour   the hour from 0 through 23
    /// @param minute the minute from 0 through 59
    /// @throws IllegalArgumentException if `hour` is outside `0..23`, `minute` is outside `0..59`, or the resulting
    ///     time is outside the configured selectable range
    public final void setTime(int hour, int minute) {
        validateHour(hour);
        validateMinute(minute);
        setValue(LocalTime.of(hour, minute));
    }

    /// Selects the current time with seconds and nanos cleared.
    public final void selectNow() {
        setValue(normalizeTime(LocalTime.now()));
    }

    /// Applies a labeled time preset.
    ///
    /// @param preset the preset whose time should be selected
    /// @throws NullPointerException     if `preset` is `null`
    /// @throws IllegalArgumentException if the preset time is outside the configured selectable range
    public final void applyPreset(M3TimePreset preset) {
        setValue(Objects.requireNonNull(preset, "preset").time());
    }

    /// Returns whether the supplied time is outside the configured selectable range.
    ///
    /// Seconds and nanoseconds are ignored. Both bounds are inclusive.
    ///
    /// @param time the time to test
    /// @return `true` when `time` is before the lower bound or after the upper bound
    /// @throws NullPointerException if `time` is `null`
    public final boolean isTimeDisabled(LocalTime time) {
        Objects.requireNonNull(time, "time");
        LocalTime normalizedTime = normalizeTime(time);
        @Nullable LocalTime min = getMinTime();
        @Nullable LocalTime max = getMaxTime();
        return min != null && normalizedTime.isBefore(min) || max != null && normalizedTime.isAfter(max);
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3TimePicker`
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the CSS metadata for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX time pickers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("time-picker.css");
    }

    /// Returns accessibility state for the selected time and the selectable cells that are currently presented.
    ///
    /// `ITEM_COUNT` and `ITEM_AT_INDEX` describe the presented time-cell nodes. If no indexed cell presentation is
    /// available, the item count is zero and indexed item queries return `null`. An indexed item query never returns
    /// a [LocalTime] model value in place of a node.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> accessibleCellCount();
            case ITEM_AT_INDEX -> accessibleCellAt(parameters);
            case SELECTED_ITEMS -> selectedItems();
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for time selection and focus.
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
            case INCREMENT -> moveByMinutes(getMinuteStep());
            case DECREMENT -> moveByMinutes(-getMinuteStep());
            case BLOCK_INCREMENT -> moveByHours(1);
            case BLOCK_DECREMENT -> moveByHours(-1);
            case SHOW_ITEM -> showAccessibleTime(parameters);
            case SET_SELECTED_ITEMS -> selectAccessibleTime(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 time picker skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TimePickerSkin(this);
    }

    /// Adds base style classes, accessibility role, and keyboard navigation.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleTime,
                this::handlesAccessibleShowTarget);
        updateInputModePseudoClasses();
        setFocusTraversable(true);
        skinProperty().addListener(observable -> notifyAccessibleItemsChanged());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
    }

    /// Synchronizes the mutually exclusive Dial/Input mode pseudo-classes.
    private void updateInputModePseudoClasses() {
        boolean input = isInputMode();
        pseudoClassStateChanged(INPUT_MODE_PSEUDO_CLASS, input);
        pseudoClassStateChanged(DIAL_MODE_PSEUDO_CLASS, !input);
    }

    /// Handles keyboard time navigation.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }

        boolean handled = switch (event.getCode()) {
            case LEFT -> {
                moveHorizontally(false);
                yield true;
            }
            case RIGHT -> {
                moveHorizontally(true);
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

    /// Moves the selected or implied time in the visual horizontal arrow direction.
    private void moveHorizontally(boolean rightKey) {
        boolean forward = M3NodeLayout.isRightToLeft(this) != rightKey;
        moveByHours(forward ? 1 : -1);
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

    /// Returns the current selected time as an immutable accessibility selection list.
    private List<LocalTime> selectedItems() {
        @Nullable LocalTime selectedTime = getValue();
        return selectedTime == null ? List.of() : List.of(selectedTime);
    }

    /// Returns the number of visible selectable cells.
    private int accessibleCellCount() {
        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        return presentation == null ? 0 : presentation.accessibleItemCount();
    }

    /// Returns the visible selectable cell at an accessibility index.
    private @Nullable Node accessibleCellAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        return presentation == null ? null : presentation.accessibleItemAt(index);
    }

    /// Returns the preferred focus node for the currently displayed time cells.
    private Node accessibleFocusNode() {
        if (accessibilityPresentation() == null) {
            return this;
        }

        @Nullable Node focusedCell = currentFocusedCell();
        if (focusedCell != null) {
            return focusedCell;
        }

        @Nullable LocalTime selectedTime = getValue();
        if (selectedTime != null) {
            @Nullable Node selectedCell = timeCellForTime(selectedTime);
            if (selectedCell != null && !selectedCell.isDisabled()) {
                return selectedCell;
            }
        }

        @Nullable Node firstEnabledCell = firstEnabledTimeCell();
        return firstEnabledCell == null ? this : firstEnabledCell;
    }

    /// Returns the currently focused visible time cell, or `null` when focus is outside this picker.
    private @Nullable Node currentFocusedCell() {
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

    /// Returns whether this picker can reveal the supplied accessibility time target.
    private boolean handlesAccessibleShowTarget(@Nullable Object parameter) {
        return parameter instanceof LocalTime time && !isTimeDisabled(time);
    }

    /// Shows and focuses the time item requested by accessibility parameters.
    final boolean showAccessibleTime(Object... parameters) {
        @Nullable Object item = accessibleTimeItem(parameters);
        if (item instanceof Node node && M3Accessible.showItem(this, node)) {
            return true;
        }
        if (item instanceof LocalTime time) {
            return !isTimeDisabled(time) && showAccessibleTime(time);
        }
        return parameters.length == 0 && focusAccessibleNode();
    }

    /// Selects the time requested by accessibility parameters.
    private void selectAccessibleTime(Object... parameters) {
        @Nullable Object item = accessibleTimeItem(parameters);
        @Nullable LocalTime time = item instanceof LocalTime localTime ? localTime : timeFromNode(item);
        if (time != null && !isTimeDisabled(time)) {
            setValue(time);
            focusAccessibleTime(time);
        }
    }

    /// Shows the rendered time cell for a time when it is visible.
    private boolean showAccessibleTime(LocalTime time) {
        @Nullable Node cell = timeCellForTime(normalizeTime(time));
        return cell != null && !cell.isDisabled() && M3Accessible.showItem(this, cell);
    }

    /// Focuses the rendered time cell for a selectable time when it is visible.
    private void focusAccessibleTime(LocalTime time) {
        if (!isTimeDisabled(time)) {
            focusAccessibleNode(timeCellForTime(normalizeTime(time)));
        }
    }

    /// Returns the time item requested by accessibility parameters.
    private @Nullable Object accessibleTimeItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return accessibleFocusNode();
        }
        if (parameters[0] instanceof Number) {
            return accessibleCellAt(parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Object item = accessibleTimeItem(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the time item requested by one accessibility action parameter.
    private @Nullable Object accessibleTimeItem(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return accessibleCellAt(number);
        }
        if (parameter instanceof LocalTime time) {
            return normalizeTime(time);
        }
        if (parameter instanceof Node node) {
            return M3Accessible.isEffectivelyReachable(node) && timeFromNode(node) != null ? node : null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Object item = accessibleTimeItem(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Object item = accessibleTimeItem(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the time stored on a rendered selectable cell.
    private static @Nullable LocalTime timeFromNode(@Nullable Object item) {
        return item instanceof Node node && node.getUserData() instanceof LocalTime time ? time : null;
    }

    /// Returns the indexed accessibility presentation supplied by the active presentation.
    private @Nullable M3PickerAccessibilityPresentation accessibilityPresentation() {
        return getSkin() instanceof M3PickerAccessibilityPresentation presentation ? presentation : null;
    }

    /// Returns the rendered visible time cell for the supplied time.
    private @Nullable Node timeCellForTime(LocalTime time) {
        LocalTime normalizedTime = normalizeTime(time);
        @Nullable M3PickerAccessibilityPresentation presentation = accessibilityPresentation();
        if (presentation == null) {
            return null;
        }
        int itemCount = presentation.accessibleItemCount();
        for (int index = 0; index < itemCount; index++) {
            @Nullable Node cell = presentation.accessibleItemAt(index);
            if (normalizedTime.equals(timeFromNode(cell))) {
                return cell;
            }
        }
        return null;
    }

    /// Returns the first rendered visible enabled time cell.
    private @Nullable Node firstEnabledTimeCell() {
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

    /// Notifies accessibility clients that visible time cells changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        M3Accessible.notifyFocusNodeChanged(this);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
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

    /// CSS metadata for metrics consumed by the time-picker presentation.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for spacing between major picker regions.
        private static final CssMetaData<M3TimePicker, Number> CONTAINER_SPACING =
                new CssMetaData<>(
                        "-m3-container-spacing",
                        SizeConverter.getInstance(),
                        DEFAULT_CONTAINER_SPACING
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3TimePicker control) {
                        return M3Css.isSettable(control.containerSpacingProperty());
                    }

                    /// Returns the styleable property for a Time Picker.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3TimePicker control) {
                        return control.containerSpacingProperty();
                    }
                };

        /// CSS metadata for the dial selector handle diameter.
        private static final CssMetaData<M3TimePicker, Number> DIAL_HANDLE_SIZE =
                new CssMetaData<>(
                        "-m3-dial-handle-size",
                        SizeConverter.getInstance(),
                        DEFAULT_DIAL_HANDLE_SIZE
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3TimePicker control) {
                        return M3Css.isSettable(control.dialHandleSizeProperty());
                    }

                    /// Returns the styleable property for a Time Picker.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3TimePicker control) {
                        return control.dialHandleSizeProperty();
                    }
                };

        /// CSS metadata for the dial center dot diameter.
        private static final CssMetaData<M3TimePicker, Number> DIAL_CENTER_SIZE =
                new CssMetaData<>(
                        "-m3-dial-center-size",
                        SizeConverter.getInstance(),
                        DEFAULT_DIAL_CENTER_SIZE
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3TimePicker control) {
                        return M3Css.isSettable(control.dialCenterSizeProperty());
                    }

                    /// Returns the styleable property for a Time Picker.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3TimePicker control) {
                        return control.dialCenterSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_SPACING);
            styleables.add(DIAL_HANDLE_SIZE);
            styleables.add(DIAL_CENTER_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
