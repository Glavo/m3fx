// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3LocalizedStringProperty;
import org.glavo.m3fx.internal.M3LocalizedTextDefault;
import org.glavo.m3fx.internal.M3LocalizerObserver;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3PickerPresetController;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.localization.M3Localizer;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/// A Material Design 3 dialog for selecting an inclusive date range.
///
/// The OK button is disabled until the associated [picker][#getPicker()] contains both endpoints. Activating OK
/// requests dialog closure. The complete range remains available from the picker after every dismissal;
/// callers must inspect [M3DialogEvent#getAction()] from the hidden event before treating it as confirmed. An
/// incomplete start-only range remains visible but cannot be accepted. Close requests remain subject to the inherited
/// [cancellable lifecycle][M3Dialog#onCloseRequestProperty()].
///
/// The picker is managed by this dialog and must not be added to another parent. Optional presets are exposed as a
/// live ordered list and appear beside the calendar. Present the configured dialog with
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
///         if (event.getAction() == dialog.getDialogPane().getDefaultAction()) {
///             M3DateRange selectedRange = dialog.getPicker().getRange();
///         }
///     });
///     overlayPane.showDialog(dialog);
/// }
/// ```
///
/// The pane headline and action labels follow this dialog's localizer until customized or bound. Assigning the
/// headline explicitly preserves it even when unchanged. JavaFX does not notify unchanged action-text assignments;
/// those labels continue following localization unless their value changes or their text property is bound.
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DateRangePickerDialog extends M3Dialog {
    /// The style class applied to dialog content when preset actions are visible.
    private static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-range-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    private static final String PRESET_LIST_STYLE_CLASS = "m3-date-range-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    private static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-range-picker-dialog-preset-button";

    /// The date range picker displayed as dialog content.
    private final M3DateRangePicker picker = new M3DateRangePicker();

    /// The retained cancel action shown in the dialog action row.
    private final M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);

    /// The retained confirmation action shown in the dialog action row.
    private final M3Button confirmAction = new M3Button("OK", M3ButtonVariant.TEXT);

    /// Tracks application ownership of the existing cancellation action text.
    private final M3LocalizedTextDefault cancelText = new M3LocalizedTextDefault(cancelAction.textProperty());

    /// Tracks application ownership of the existing confirmation action text.
    private final M3LocalizedTextDefault confirmText = new M3LocalizedTextDefault(confirmAction.textProperty());

    /// Retains direct localization observation for this reusable dialog's lifetime.
    private final M3LocalizerObserver localizerObserver =
            new M3LocalizerObserver(this, this::refreshLocalization);

    /// Refreshes only text that has not been customized or bound by the application.
    private void refreshLocalization(M3Localizer localizer) {
        ((M3LocalizedStringProperty) getDialogPane().headlineProperty()).updateDefault(localizer.getSelectDateRangeText());
        cancelText.updateDefault(localizer.getCancelText());
        confirmText.updateDefault(localizer.getConfirmText());
    }

    /// The live, mutable, ordered list of date-range presets rendered before the picker.
    ///
    /// The list initially is empty, rejects `null` elements, permits duplicates, and observes additions, removals,
    /// and reordering. Presets with an endpoint outside the current picker bounds remain visible but disabled.
    private final ObservableList<M3DateRangePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// The container for the preset list and picker.
    private final HBox presetContent = new HBox(16.0, presetList, picker);

    /// Incrementally maintains preset actions without rebuilding unaffected buttons.
    private final M3PickerPresetController<M3DateRangePreset> presetController =
            new M3PickerPresetController<>(
                    presets, presetList, PRESET_BUTTON_STYLE_CLASS, M3DateRangePreset::text,
                    preset -> isPresetDisabled(preset),
                    (preset, event) -> picker.applyPreset(preset)
            );

    /// Refreshes existing button disabled states after picker bounds change.
    private final InvalidationListener presetBoundsInvalidation =
            observable -> presetController.refreshDisabledStates();

    /// Creates a date range picker dialog with no selected endpoints, presets, or date bounds.
    ///
    /// The headline and action labels follow this dialog's localizer until explicitly customized.
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
    /// Its localizer property is bound to this dialog's [#localizerProperty()].
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
        // This dialog owns the picker and controls its built-in text for the picker lifetime.
        picker.localizerProperty().bind(localizerProperty());
        M3DialogPane pane = getDialogPane();
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
        picker.selectionProperty().addListener((observable, oldValue, newValue) -> updateOkButtonState());
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
        confirmAction.setDisable(!picker.isRangeComplete());
    }

}
