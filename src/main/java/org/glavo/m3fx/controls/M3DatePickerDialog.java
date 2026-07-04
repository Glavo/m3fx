// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

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

    /// The mutable date preset list rendered before the picker.
    private final ObservableList<M3DatePreset> presets = M3ObservableLists.nonNullElementList("preset");

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
        presetContent.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(pane));
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetList.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(pane));
        M3PresetNavigation.install(presetList, pane, () -> M3Accessible.requestAccessibleFocus(pane, picker));
        pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(buttonType -> buttonType == ButtonType.OK ? getValue() : null);
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
        M3Css.setMaxWidthIfUnbound(button, Double.MAX_VALUE);
        button.setDisable(picker.isDateDisabled(preset.date()));
        button.setOnAction(event -> picker.applyPreset(preset));
        return button;
    }

    /// Enables the OK button only when a selected date exists.
    private void updateOkButtonState() {
        @Nullable Node okButton = getM3DialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(getValue() == null);
        }
    }

}