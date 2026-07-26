// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3PickerPresetController;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/// A Material Design 3 dialog for selecting one date.
///
/// While the dialog [value][#valueProperty()] is unbound, it stays synchronized with its associated
/// [picker][#getPicker()]. The OK button is disabled until a date is selected. Activating OK requests dialog closure,
/// and the current date remains available through [#valueProperty()]. Cancel and other dismissal paths retain that
/// state, so callers must inspect
/// [M3DialogEvent#getAction()] from the hidden event before treating it as confirmed. Close requests remain
/// subject to the inherited [cancellable lifecycle][M3Dialog#onCloseRequestProperty()].
///
/// The picker is managed by this dialog and must not be added to another parent. Optional presets are exposed as a
/// live ordered list and appear beside the calendar. Bounds, locale, and adjacent-month display are configured
/// through the picker. Present the configured dialog in an existing scene with
/// [M3OverlayPane#showDialog(M3Dialog)] or in a dedicated native window with
/// [M3DialogWindow#showDialog(M3Dialog)].
///
/// ```java
/// private void showDateDialog(M3OverlayPane overlayPane) {
///     LocalDate today = LocalDate.now();
///     M3DatePickerDialog dialog = new M3DatePickerDialog(today);
///     dialog.getPicker().setMinDate(today);
///     dialog.getPicker().setMaxDate(today.plusMonths(3));
///     dialog.getPresets().addAll(M3DatePresets.common(today));
///     dialog.setOnHidden(event -> {
///         if (event.getAction() == dialog.getDialogPane().getDefaultAction()) {
///             LocalDate selectedDate = dialog.getValue();
///         }
///     });
///     overlayPane.showDialog(dialog);
/// }
/// ```
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DatePickerDialog extends M3Dialog {
    /// The default headline text for date picker dialogs.
    private static final String DEFAULT_TITLE = "Select date";

    /// The style class applied to dialog content when preset actions are visible.
    private static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    private static final String PRESET_LIST_STYLE_CLASS = "m3-date-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    private static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-picker-dialog-preset-button";

    /// Creates a date picker dialog with no selected date, no presets, and no date bounds.
    ///
    /// The headline is initialized to `Select date`, and Cancel and OK buttons are installed.
    public M3DatePickerDialog() {
        initialize();
    }

    /// Creates a date picker dialog initialized with the specified selected date.
    ///
    /// @param value the initially selected date, or `null` for no selected date
    public M3DatePickerDialog(@Nullable LocalDate value) {
        initialize();
        setValue(value);
    }

    /// The selected date, or `null` when no date is selected.
    ///
    /// The default value is `null`. While unbound, this property is bidirectionally synchronized with the associated
    /// picker. A non-null assignment is validated against the picker's current inclusive bounds and displays its
    /// month. Assigning `null` clears selection and disables the OK action. Changes made through the associated picker
    /// update an unbound property as well.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalDate> value =
            new SimpleObjectProperty<>(this, "value") {
                /// Validates direct writes through the picker before committing the public value.
                @Override
                public void set(@Nullable LocalDate newValue) {
                    if (synchronizingValue) {
                        super.set(newValue);
                        return;
                    }
                    synchronizingValue = true;
                    try {
                        picker.setValue(newValue);
                        super.set(picker.getValue());
                    } finally {
                        synchronizingValue = false;
                    }
                }
            };

    /// Returns the selected date, or `null` when no date is selected.
    ///
    /// @return the selected date, or `null` when no date is selected
    public @Nullable LocalDate getValue() {
        return value.get();
    }

    /// Sets the selected date in both this dialog and its associated picker, or clears selection for `null`.
    ///
    /// @param value the selected date, or `null` to clear selection
    /// @throws IllegalArgumentException if `value` is outside the picker's current inclusive bounds
    public void setValue(@Nullable LocalDate value) {
        this.value.set(value);
    }

    /// Returns the observable property that stores the selected date.
    ///
    /// The property can be observed and bound, and its default value is `null`. While unbound, direct assignments
    /// are validated through and synchronized with the associated picker; picker changes update this property.
    ///
    /// @return the selected-date property
    public ObjectProperty<@Nullable LocalDate> valueProperty() {
        return value;
    }

    /// The date picker displayed as dialog content.
    private final M3DatePicker picker = new M3DatePicker();

    /// The retained cancel action shown in the dialog action row.
    private final M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);

    /// The retained confirmation action shown in the dialog action row.
    private final M3Button confirmAction = new M3Button("OK", M3ButtonVariant.TEXT);

    /// Whether the dialog and picker are currently synchronizing selected values.
    private boolean synchronizingValue;

    /// The live, mutable, ordered list of date presets rendered before the picker.
    ///
    /// The list initially is empty, rejects `null` elements, permits duplicates, and observes additions, removals,
    /// and reordering. Presets outside the current picker bounds remain in the list but their actions are disabled.
    private final ObservableList<M3DatePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// The container for the preset list and picker.
    private final HBox presetContent = new HBox(16.0, presetList, picker);

    /// Incrementally maintains preset actions without rebuilding unaffected buttons.
    private final M3PickerPresetController<M3DatePreset> presetController =
            new M3PickerPresetController<>(presets, presetList, PRESET_BUTTON_STYLE_CLASS) {
                /// Returns one date preset label.
                @Override
                protected String presetText(M3DatePreset preset) {
                    return preset.text();
                }

                /// Applies one selected date preset.
                @Override
                protected void applyPreset(M3DatePreset preset) {
                    picker.applyPreset(preset);
                }

                /// Returns whether one date preset is outside the selectable bounds.
                @Override
                protected boolean isPresetDisabled(M3DatePreset preset) {
                    return picker.isDateDisabled(preset.date());
                }
            };

    /// Refreshes existing button disabled states after picker bounds change.
    private final InvalidationListener presetBoundsInvalidation =
            observable -> presetController.refreshDisabledStates();

    /// Returns the date picker owned and displayed by this dialog.
    ///
    /// @return the date picker displayed by this dialog
    public M3DatePicker getPicker() {
        return picker;
    }

    /// Returns the live, mutable date preset list.
    ///
    /// The list is initially empty, preserves insertion order and duplicates, and rejects `null` elements before
    /// mutation. Each entry creates a distinct preset action. A preset outside the picker's current bounds remains
    /// present but is disabled.
    ///
    /// @return the live, mutable date preset list
    public ObservableList<M3DatePreset> getPresets() {
        return presets;
    }

    /// Configures dialog content, buttons, value synchronization, and button state.
    private void initialize() {
        picker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!synchronizingValue) {
                value.set(newValue);
            }
        });
        M3DialogPane pane = getDialogPane();
        picker.pseudoClassStateChanged(M3DatePicker.MODAL_PSEUDO_CLASS, true);
        pane.setHeaderText(DEFAULT_TITLE);
        pane.setContent(presetContent);
        picker.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetList.setAlignment(Pos.TOP_LEFT);
        M3PresetNavigation.installColumn(presetList, pane, () -> M3Accessible.requestAccessibleFocus(pane, picker));
        cancelAction.setCancelButton(true);
        confirmAction.setDefaultButton(true);
        pane.getActions().setAll(cancelAction, confirmAction);
        value.addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.minDateProperty().addListener(presetBoundsInvalidation);
        picker.maxDateProperty().addListener(presetBoundsInvalidation);
        presetController.install();
        updateOkButtonState();
    }

    /// Enables the OK button only when a selected date exists.
    private void updateOkButtonState() {
        confirmAction.setDisable(getValue() == null);
    }

}
