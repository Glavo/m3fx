// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/// A Material Design 3 dialog preset for selecting one date.
///
/// The dialog installs an [M3DatePicker] as its content, wires OK and cancel actions, and keeps the selected
/// [LocalDate] as the dialog result when the user accepts the choice.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public class M3DatePickerDialog extends M3Dialog<LocalDate> {
    /// The default title and header text for date picker dialogs.
    public static final String DEFAULT_TITLE = "Select date";

    /// The style class applied to dialog content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-date-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-picker-dialog-preset-button";

    /// The date picker displayed as dialog content.
    private final M3DatePicker picker = new M3DatePicker();

    /// The mutable preset list rendered before the picker.
    private final ObservableList<M3DatePreset> presets = FXCollections.observableArrayList();

    /// The wrapper used when the dialog renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3DatePreset> presetsListener = change -> updatePresetContent();

    /// Creates an empty date picker dialog.
    public M3DatePickerDialog() {
        initialize();
    }

    /// Creates a date picker dialog initialized with the supplied selected date.
    ///
    /// @param value the initially selected date, or `null` for no selected date
    public M3DatePickerDialog(@Nullable LocalDate value) {
        initialize();
        setValue(value);
    }

    /// Returns the date picker displayed by this dialog.
    ///
    /// @return the date picker displayed by this dialog
    public final M3DatePicker getPicker() {
        return picker;
    }

    /// Returns the mutable date preset list.
    ///
    /// @return the mutable date preset list
    public final ObservableList<M3DatePreset> getPresets() {
        return presets;
    }

    /// Adds one date preset.
    ///
    /// @param preset the date preset to add
    public final void addPreset(M3DatePreset preset) {
        presets.add(Objects.requireNonNull(preset, "preset"));
    }

    /// Adds date presets after validating the preset array.
    ///
    /// @param presets the date presets to add
    public final void addPresets(M3DatePreset... presets) {
        validatePresets(presets);
        this.presets.addAll(presets);
    }

    /// Replaces all date presets.
    ///
    /// @param presets the replacement date presets
    public final void setPresets(M3DatePreset... presets) {
        validatePresets(presets);
        this.presets.setAll(presets);
    }

    /// Replaces all date presets with the default common date set.
    ///
    /// @param anchorDate the date used to compute relative common presets
    public final void setCommonPresets(LocalDate anchorDate) {
        presets.setAll(M3DatePresets.common(anchorDate));
    }

    /// Removes all date presets.
    public final void clearPresets() {
        presets.clear();
    }

    /// Returns the selected date, or `null` when no date is selected.
    ///
    /// @return the selected date, or `null` when no date is selected
    public final @Nullable LocalDate getValue() {
        return picker.getValue();
    }

    /// Sets the selected date, or clears selection when `null` is supplied.
    ///
    /// @param value the selected date, or `null` to clear selection
    public final void setValue(@Nullable LocalDate value) {
        picker.setValue(value);
    }

    /// Returns the selected date property.
    ///
    /// @return the selected date property from the picker
    public final ObjectProperty<@Nullable LocalDate> valueProperty() {
        return picker.valueProperty();
    }

    /// Clears the selected date.
    public final void clearValue() {
        picker.clearValue();
    }

    /// Returns the month currently displayed by the picker.
    ///
    /// @return the month currently displayed by the picker
    public final YearMonth getDisplayedMonth() {
        return picker.getDisplayedMonth();
    }

    /// Sets the month displayed by the picker.
    ///
    /// @param displayedMonth the month displayed by the picker
    public final void setDisplayedMonth(YearMonth displayedMonth) {
        picker.setDisplayedMonth(displayedMonth);
    }

    /// Returns the displayed month property from the picker.
    ///
    /// @return the displayed month property from the picker
    public final ObjectProperty<YearMonth> displayedMonthProperty() {
        return picker.displayedMonthProperty();
    }

    /// Returns the weekday shown in the first picker column.
    ///
    /// @return the weekday shown in the first picker column
    public final DayOfWeek getFirstDayOfWeek() {
        return picker.getFirstDayOfWeek();
    }

    /// Sets the weekday shown in the first picker column.
    ///
    /// @param firstDayOfWeek the weekday shown in the first picker column
    public final void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        picker.setFirstDayOfWeek(firstDayOfWeek);
    }

    /// Returns the first day of week property from the picker.
    ///
    /// @return the first day of week property from the picker
    public final ObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return picker.firstDayOfWeekProperty();
    }

    /// Returns the earliest selectable date, or `null` when there is no lower bound.
    ///
    /// @return the earliest selectable date, or `null` when there is no lower bound
    public final @Nullable LocalDate getMinDate() {
        return picker.getMinDate();
    }

    /// Sets the earliest selectable date, or clears the lower bound when `null` is supplied.
    ///
    /// @param minDate the earliest selectable date, or `null` to clear the lower bound
    public final void setMinDate(@Nullable LocalDate minDate) {
        picker.setMinDate(minDate);
    }

    /// Returns the minimum date property from the picker.
    ///
    /// @return the minimum date property from the picker
    public final ObjectProperty<@Nullable LocalDate> minDateProperty() {
        return picker.minDateProperty();
    }

    /// Returns the latest selectable date, or `null` when there is no upper bound.
    ///
    /// @return the latest selectable date, or `null` when there is no upper bound
    public final @Nullable LocalDate getMaxDate() {
        return picker.getMaxDate();
    }

    /// Sets the latest selectable date, or clears the upper bound when `null` is supplied.
    ///
    /// @param maxDate the latest selectable date, or `null` to clear the upper bound
    public final void setMaxDate(@Nullable LocalDate maxDate) {
        picker.setMaxDate(maxDate);
    }

    /// Returns the maximum date property from the picker.
    ///
    /// @return the maximum date property from the picker
    public final ObjectProperty<@Nullable LocalDate> maxDateProperty() {
        return picker.maxDateProperty();
    }

    /// Returns whether adjacent-month days are visible in leading and trailing picker cells.
    ///
    /// @return `true` when adjacent-month days are visible
    public final boolean isShowAdjacentMonthDays() {
        return picker.isShowAdjacentMonthDays();
    }

    /// Sets whether adjacent-month days are visible in leading and trailing picker cells.
    ///
    /// @param showAdjacentMonthDays whether adjacent-month days should be visible
    public final void setShowAdjacentMonthDays(boolean showAdjacentMonthDays) {
        picker.setShowAdjacentMonthDays(showAdjacentMonthDays);
    }

    /// Returns the adjacent-month visibility property from the picker.
    ///
    /// @return the adjacent-month visibility property from the picker
    public final BooleanProperty showAdjacentMonthDaysProperty() {
        return picker.showAdjacentMonthDaysProperty();
    }

    /// Selects a date if it is inside the configured range.
    ///
    /// @param date the date to select
    public final void selectDate(LocalDate date) {
        picker.selectDate(Objects.requireNonNull(date, "date"));
    }

    /// Selects today's date when it is inside the configured range.
    public final void selectToday() {
        picker.selectToday();
    }

    /// Applies a date preset and leaves the dialog open for confirmation.
    ///
    /// @param preset the date preset to apply
    public final void applyPreset(M3DatePreset preset) {
        picker.applyPreset(Objects.requireNonNull(preset, "preset"));
        updateOkButtonState();
    }

    /// Shows the month before the current displayed month.
    public final void showPreviousMonth() {
        picker.showPreviousMonth();
    }

    /// Shows the month after the current displayed month.
    public final void showNextMonth() {
        picker.showNextMonth();
    }

    /// Shows the supplied month without changing the selected date.
    ///
    /// @param month the month to display
    public final void showMonth(YearMonth month) {
        picker.showMonth(month);
    }

    /// Shows the month containing today's date without changing the selected date.
    public final void showToday() {
        picker.showToday();
    }

    /// Returns whether the supplied date is outside the configured selectable range.
    ///
    /// @param date the date to test
    /// @return `true` when the date is outside the configured selectable range
    public final boolean isDateDisabled(LocalDate date) {
        return picker.isDateDisabled(date);
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
        picker.minDateProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
        picker.maxDateProperty().addListener((observable, oldValue, newValue) -> updatePresetContent());
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

        for (M3DatePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, picker);
        pane.setContent(presetContent);
    }

    /// Creates one preset action button.
    private M3Button createPresetButton(M3DatePreset preset) {
        M3Button button = new M3Button(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setDisable(picker.isDateDisabled(preset.date()));
        button.setOnAction(event -> applyPreset(preset));
        return button;
    }

    /// Converts a dialog button into the selected date result.
    private @Nullable LocalDate convertResult(@Nullable ButtonType buttonType) {
        return buttonType == ButtonType.OK ? getValue() : null;
    }

    /// Enables the OK button only when a selected date exists.
    private void updateOkButtonState() {
        @Nullable Node okButton = getM3DialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(getValue() == null);
        }
    }

    /// Validates a date preset array.
    private static void validatePresets(M3DatePreset... presets) {
        Objects.requireNonNull(presets, "presets");
        for (M3DatePreset preset : presets) {
            Objects.requireNonNull(preset, "preset");
        }
    }
}
