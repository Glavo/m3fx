// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.Objects;

/// A Material Design 3 dialog preset for selecting one time.
///
/// The dialog installs an [M3TimePicker] as its content, wires OK and cancel actions, and keeps the selected
/// [LocalTime] as the dialog result when the user accepts the choice.
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
@NotNullByDefault
public class M3TimePickerDialog extends M3Dialog<LocalTime> {
    /// The default title and header text for time picker dialogs.
    public static final String DEFAULT_TITLE = "Select time";

    /// The style class applied to dialog content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-time-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-time-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-time-picker-dialog-preset-button";

    /// The time picker displayed as dialog content.
    private final M3TimePicker picker = new M3TimePicker();

    /// The mutable preset list rendered before the picker.
    private final ObservableList<M3TimePreset> presets = FXCollections.observableArrayList();

    /// The wrapper used when the dialog renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3TimePreset> presetsListener = change -> updatePresetContent();

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

    /// Returns the mutable time preset list.
    public final ObservableList<M3TimePreset> getPresets() {
        return presets;
    }

    /// Adds one time preset.
    public final void addPreset(M3TimePreset preset) {
        presets.add(Objects.requireNonNull(preset, "preset"));
    }

    /// Adds time presets after validating the preset array.
    public final void addPresets(M3TimePreset... presets) {
        validatePresets(presets);
        this.presets.addAll(presets);
    }

    /// Replaces all time presets.
    public final void setPresets(M3TimePreset... presets) {
        validatePresets(presets);
        this.presets.setAll(presets);
    }

    /// Replaces all time presets with the default common time set.
    public final void setCommonPresets(LocalTime anchorTime) {
        presets.setAll(M3TimePresets.common(anchorTime));
    }

    /// Removes all time presets.
    public final void clearPresets() {
        presets.clear();
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

    /// Applies a time preset and leaves the dialog open for confirmation.
    public final void applyPreset(M3TimePreset preset) {
        picker.applyPreset(Objects.requireNonNull(preset, "preset"));
        updateOkButtonState();
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
        picker.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetList.setAlignment(Pos.TOP_LEFT);
        pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(this::convertResult);
        picker.valueProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.minTimeProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
        picker.maxTimeProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
        presets.addListener(presetsListener);
        updateOkButtonState();
    }

    /// Rebuilds dialog content from the current preset list.
    private void updatePresetContent() {
        M3DialogPane pane = getM3DialogPane();
        presetContent.getChildren().clear();
        presetList.getChildren().clear();
        pane.setContent(null);

        if (presets.isEmpty()) {
            pane.setContent(picker);
            return;
        }

        for (M3TimePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, picker);
        pane.setContent(presetContent);
    }

    /// Creates one preset action button.
    private M3Button createPresetButton(M3TimePreset preset) {
        M3Button button = M3Button.withVariant(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setDisable(picker.isTimeDisabled(preset.time()));
        button.setOnAction(event -> applyPreset(preset));
        return button;
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

    /// Validates a time preset array.
    private static void validatePresets(M3TimePreset... presets) {
        Objects.requireNonNull(presets, "presets");
        for (M3TimePreset preset : presets) {
            Objects.requireNonNull(preset, "preset");
        }
    }
}
