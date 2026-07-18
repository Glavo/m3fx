// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

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

/// A Material Design 3 dialog for selecting an inclusive date range.
///
/// The OK button is disabled until the embedded [picker][#getPicker()] contains both endpoints. Activating OK
/// requests dialog closure. The complete range remains available from the embedded picker after every dismissal;
/// callers must inspect [M3DialogEvent#getButtonType()] from the hidden event before treating it as confirmed. An
/// incomplete start-only range remains visible but cannot be accepted. Close requests remain subject to the inherited
/// [cancellable lifecycle][M3Dialog#onCloseRequestProperty()].
///
/// The picker is owned by this dialog and must not be reparented. Optional presets are exposed as a live ordered
/// list and appear beside the calendar. Present the configured dialog with
/// [M3OverlayPane#showDialog(M3Dialog)] or [M3DialogWindow#showDialog(M3Dialog)].
///
/// ```java
/// private void showRangeDialog(M3OverlayPane overlayPane) {
///     LocalDate today = LocalDate.now();
///     M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
///     dialog.getPicker().setMinDate(today.minusMonths(1));
///     dialog.getPicker().setMaxDate(today.plusMonths(6));
///     dialog.getPresets().addAll(M3DateRangePresets.common(
///             today, dialog.getPicker().getFirstDayOfWeek()));
///     dialog.setOnHidden(event -> {
///         if (event.getButtonType() == ButtonType.OK) {
///             M3DateRange selectedRange = dialog.getPicker().getRange();
///         }
///     });
///     overlayPane.showDialog(dialog);
/// }
/// ```
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DateRangePickerDialog extends M3Dialog {
    /// The default headline text for date range picker dialogs.
    private static final String DEFAULT_TITLE = "Select date range";

    /// The style class applied to dialog content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-range-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-date-range-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-range-picker-dialog-preset-button";

    /// The date range picker displayed as dialog content.
    private final M3DateRangePicker picker = new M3DateRangePicker();

    /// The live, mutable, ordered list of date-range presets rendered before the picker.
    ///
    /// The list initially is empty, rejects `null` elements, permits duplicates, and observes additions, removals,
    /// and reordering. Presets with an endpoint outside the current picker bounds remain visible but disabled.
    private final ObservableList<M3DateRangePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// The stable wrapper that keeps the preset list and picker parented for the dialog lifetime.
    private final HBox presetContent = new HBox(16.0, presetList, picker);

    /// Incrementally maintains preset actions without rebuilding unaffected buttons.
    private final M3PickerPresetController<M3DateRangePreset> presetController =
            new M3PickerPresetController<>(presets, presetList, PRESET_BUTTON_STYLE_CLASS) {
                /// Returns one date range preset label.
                @Override
                protected String presetText(M3DateRangePreset preset) {
                    return preset.text();
                }

                /// Applies one selected date range preset.
                @Override
                protected void applyPreset(M3DateRangePreset preset) {
                    picker.applyPreset(preset);
                }

                /// Returns whether either date range endpoint is outside the selectable bounds.
                @Override
                protected boolean isPresetDisabled(M3DateRangePreset preset) {
                    return M3DateRangePickerDialog.this.isPresetDisabled(preset);
                }
            };

    /// Refreshes existing button disabled states after picker bounds change.
    private final InvalidationListener presetBoundsInvalidation =
            observable -> presetController.refreshDisabledStates();

    /// Creates a date range picker dialog with no selected endpoints, presets, or date bounds.
    ///
    /// The headline is initialized to `Select date range`, and Cancel and OK buttons are installed.
    public M3DateRangePickerDialog() {
        initialize();
    }

    /// Creates a date range picker dialog initialized with the specified complete range.
    ///
    /// @param range the initial selected date range
    /// @throws NullPointerException if `range` is `null`
    public M3DateRangePickerDialog(M3DateRange range) {
        this(range.startDate(), range.endDate());
    }

    /// Creates a date range picker dialog initialized with the specified endpoints.
    ///
    /// @param startDate the first selected date, or `null` for no selected range
    /// @param endDate   the last selected date, or `null` for an incomplete range
    /// @throws IllegalArgumentException if `startDate` is after `endDate`
    public M3DateRangePickerDialog(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        initialize();
        picker.setRange(startDate, endDate);
    }

    /// Returns the date range picker owned and displayed by this dialog.
    ///
    /// @return the date range picker displayed by this dialog
    public M3DateRangePicker getPicker() {
        return picker;
    }

    /// Returns the live, mutable date range preset list.
    ///
    /// The list is initially empty, preserves insertion order and duplicates, and rejects `null` elements before
    /// mutation. Each entry creates a distinct preset action. A preset with an endpoint outside the picker's current
    /// bounds remains present but is disabled.
    ///
    /// @return the live, mutable date range preset list
    public ObservableList<M3DateRangePreset> getPresets() {
        return presets;
    }

    /// Configures dialog content, buttons, picker state, and button state.
    private void initialize() {
        M3DialogPane pane = getDialogPane();
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
        picker.startDateProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.endDateProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.minDateProperty().addListener(presetBoundsInvalidation);
        picker.maxDateProperty().addListener(presetBoundsInvalidation);
        presetController.install();
        updateOkButtonState();
    }

    /// Returns whether the preset range falls outside the current picker date bounds.
    private boolean isPresetDisabled(M3DateRangePreset preset) {
        M3DateRange range = preset.range();
        return picker.isDateDisabled(range.startDate()) || picker.isDateDisabled(range.endDate());
    }

    /// Enables the OK button only when both range endpoints are selected.
    private void updateOkButtonState() {
        @Nullable Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(!picker.isRangeComplete());
        }
    }

}