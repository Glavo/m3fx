// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3PickerPresetController;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/// A Material Design 3 dialog for selecting one date.
///
/// The embedded [picker][#getPicker()] and the dialog [value][#valueProperty()] stay synchronized. The OK button is
/// disabled until a date is selected. Activating OK closes the dialog and produces that date through the standard
/// [javafx.scene.control.Dialog] result APIs; Cancel or a window close produces `null`.
///
/// The picker is owned by this dialog and must not be reparented. Optional presets are exposed as a live ordered
/// list and appear beside the calendar. Bounds, locale, and adjacent-month display are configured through the
/// embedded picker.
///
/// ```java
/// private void showDateDialog(Node owner) {
///     LocalDate today = LocalDate.now();
///     M3DatePickerDialog dialog = new M3DatePickerDialog(today);
///     dialog.initOwner(owner);
///     dialog.getPicker().setMinDate(today);
///     dialog.getPicker().setMaxDate(today.plusMonths(3));
///     dialog.getPresets().addAll(M3DatePresets.common(today));
///     dialog.showAndWait().ifPresent(System.out::println);
/// }
/// ```
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DatePickerDialog extends M3Dialog<LocalDate> {
    /// The default title and header text for date picker dialogs.
    private static final String DEFAULT_TITLE = "Select date";

    /// The style class applied to dialog content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-date-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-picker-dialog-preset-button";

    /// The date picker displayed as dialog content.
    private final M3DatePicker picker = new M3DatePicker();

    /// The selected date, or `null` when no date is selected.
    ///
    /// The default value is `null`. This property is bidirectionally synchronized with the embedded picker. A
    /// non-null assignment is validated against the picker's current inclusive bounds and displays its month.
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

    /// Whether the dialog and embedded picker are currently synchronizing selected values.
    private boolean synchronizingValue;

    /// The live, mutable, ordered list of date presets rendered before the picker.
    ///
    /// The list initially is empty, rejects `null` elements, permits duplicates, and observes additions, removals,
    /// and reordering. Presets outside the current picker bounds remain in the list but their actions are disabled.
    private final ObservableList<M3DatePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// The stable wrapper that keeps the preset list and picker parented for the dialog lifetime.
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

    /// Creates a date picker dialog with no selected date, no presets, and no date bounds.
    ///
    /// The title and header text are initialized to `Select date`, and Cancel and OK buttons are installed.
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

    /// Returns the date picker owned and displayed by this dialog.
    ///
    /// @return the date picker displayed by this dialog
    public final M3DatePicker getPicker() {
        return picker;
    }

    /// Returns the live, mutable date preset list.
    ///
    /// @return the live, mutable date preset list
    public final ObservableList<M3DatePreset> getPresets() {
        return presets;
    }

    /// Returns the selected date, or `null` when no date is selected.
    ///
    /// @return the selected date, or `null` when no date is selected
    public final @Nullable LocalDate getValue() {
        return value.get();
    }

    /// Sets the selected date in both this dialog and its embedded picker, or clears selection for `null`.
    ///
    /// @param value the selected date, or `null` to clear selection
    /// @throws IllegalArgumentException if `value` is outside the picker's current inclusive bounds
    public final void setValue(@Nullable LocalDate value) {
        this.value.set(value);
    }

    public final ObjectProperty<@Nullable LocalDate> valueProperty() {
        return value;
    }

    /// Configures dialog content, buttons, result conversion, and button state.
    @SuppressWarnings("DataFlowIssue")
    private void initialize() {
        picker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!synchronizingValue) {
                value.set(newValue);
            }
        });
        setTitle(DEFAULT_TITLE);
        M3DialogPane pane = getM3DialogPane();
        picker.pseudoClassStateChanged(M3DatePicker.MODAL_PSEUDO_CLASS, true);
        pane.setHeaderText(DEFAULT_TITLE);
        pane.setContent(presetContent);
        picker.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(pane));
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetList.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(pane));
        M3PresetNavigation.installColumn(presetList, pane, () -> M3Accessible.requestAccessibleFocus(pane, picker));
        pane.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(buttonType -> buttonType == ButtonType.OK ? getValue() : null);
        value.addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.minDateProperty().addListener(presetBoundsInvalidation);
        picker.maxDateProperty().addListener(presetBoundsInvalidation);
        presetController.install();
        updateOkButtonState();
    }

    /// Enables the OK button only when a selected date exists.
    private void updateOkButtonState() {
        @Nullable Node okButton = getM3DialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(getValue() == null);
        }
    }

}
