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

import java.time.LocalTime;

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

    /// The mutable time preset list rendered before the picker.
    private final ObservableList<M3TimePreset> presets = M3ObservableLists.nonNullElementList("preset");

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
    ///
    /// @param value the initially selected time, or `null` for no selected time
    public M3TimePickerDialog(@Nullable LocalTime value) {
        initialize();
        setValue(value);
    }

    /// Returns the time picker displayed by this dialog.
    ///
    /// @return the time picker displayed by this dialog
    public final M3TimePicker getPicker() {
        return picker;
    }

    /// Returns the mutable time preset list.
    ///
    /// @return the mutable time preset list
    public final ObservableList<M3TimePreset> getPresets() {
        return presets;
    }

    /// Returns the selected time, or `null` when no time is selected.
    ///
    /// @return the selected time, or `null` when no time is selected
    public final @Nullable LocalTime getValue() {
        return picker.getValue();
    }

    /// Sets the selected time, or clears selection when `null` is supplied.
    ///
    /// @param value the selected time, or `null` to clear selection
    public final void setValue(@Nullable LocalTime value) {
        picker.setValue(value);
    }

    /// Returns the selected time property.
    ///
    /// @return the selected time property from the picker
    public final ObjectProperty<@Nullable LocalTime> valueProperty() {
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
        M3Button button = new M3Button(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        M3Css.setMaxWidthIfUnbound(button, Double.MAX_VALUE);
        button.setDisable(picker.isTimeDisabled(preset.time()));
        button.setOnAction(event -> picker.applyPreset(preset));
        return button;
    }

    /// Enables the OK button only when a selected time exists.
    private void updateOkButtonState() {
        @Nullable Node okButton = getM3DialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(getValue() == null);
        }
    }

}