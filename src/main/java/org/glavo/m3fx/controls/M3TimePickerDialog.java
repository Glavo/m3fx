// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3PickerPresetController;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;

/// An M3FX dialog preset for selecting one time with Material Design 3 tokens.
///
/// The dialog provides an [M3TimePicker], OK and cancel actions, and the selected [LocalTime] through
/// [#valueProperty()]. Its Dial/Input mode switch is presented with the dialog actions.
///
/// [M3OverlayPane#showDialog(M3Dialog)] and [M3DialogWindow#showDialog(M3Dialog)] present the dialog without
/// blocking. OK is disabled until a value is selected. Callers can inspect [M3DialogEvent#getAction()] from the
/// hidden event to distinguish confirmation from dismissal; cancellation retains the current value.
///
/// ```java
/// M3TimePickerDialog dialog = new M3TimePickerDialog(LocalTime.of(9, 30));
/// dialog.setOnHidden(event -> {
///     if (event.getAction() == dialog.getDialogPane().getDefaultAction()) {
///         LocalTime acceptedTime = dialog.getValue();
///     }
/// });
/// overlayPane.showDialog(dialog);
/// ```
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
@NotNullByDefault
public final class M3TimePickerDialog extends M3Dialog {
    /// The dialog preset content style class.
    private static final String PRESET_CONTENT_STYLE_CLASS = "m3-time-picker-dialog-preset-content";

    /// The dialog preset list style class.
    private static final String PRESET_LIST_STYLE_CLASS = "m3-time-picker-dialog-preset-list";

    /// The dialog preset button style class.
    private static final String PRESET_BUTTON_STYLE_CLASS = "m3-time-picker-dialog-preset-button";

    /// The time-picker dialog content style class.
    private static final String TIME_PICKER_DIALOG_CONTENT_STYLE_CLASS = "m3-time-picker-dialog-content";

    /// The time-picker mode button style class.
    private static final String TIME_PICKER_MODE_BUTTON_STYLE_CLASS = "m3-time-picker-mode-button";

    /// The default headline text for time picker dialogs.
    private static final String DEFAULT_TITLE = "Select time";

    /// The pseudo-class that delegates mode-switch placement to the dialog action row.
    private static final PseudoClass DIALOG_EMBEDDED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("dialog-embedded");

    /// The time picker displayed as dialog content.
    private final M3TimePicker picker;

    /// The retained cancel action shown at the logical end of the action row.
    private final M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);

    /// The retained confirmation action shown at the logical end of the action row.
    private final M3Button confirmAction = new M3Button("OK", M3ButtonVariant.TEXT);

    /// Whether dialog state and time selection are currently synchronizing.
    private boolean synchronizingValue;

    /// The mutable time preset list rendered before the picker.
    private final ObservableList<M3TimePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// The stable horizontal wrapper that keeps presets beside the picker for the dialog lifetime.
    private final HBox presetContent;

    /// Incrementally maintains preset actions without rebuilding unaffected buttons.
    private final M3PickerPresetController<M3TimePreset> presetController;

    /// Refreshes existing button disabled states after picker bounds change.
    private final InvalidationListener presetBoundsInvalidation;

    /// Creates a time picker dialog with no selected time.
    ///
    /// The dialog headline is `Select time`, the OK action is disabled, and no time is selected initially.
    public M3TimePickerDialog() {
        this(new TimePickerDialogPane(), null);
    }

    /// Creates a time picker dialog initialized with the supplied selected time.
    ///
    /// @param value the initially selected time, or `null` for no selected time
    public M3TimePickerDialog(@Nullable LocalTime value) {
        this(new TimePickerDialogPane(), value);
    }

    /// Creates a dialog around one prebuilt specialized pane.
    private M3TimePickerDialog(TimePickerDialogPane pane, @Nullable LocalTime value) {
        super(pane);
        picker = pane.picker;
        this.value = createValueProperty();
        presetContent = new HBox(16.0, presetList, picker);
        presetController = new M3PickerPresetController<>(presets, presetList, PRESET_BUTTON_STYLE_CLASS) {
            /// Returns one time preset label.
            @Override
            protected String presetText(M3TimePreset preset) {
                return preset.text();
            }

            /// Applies one selected time preset.
            @Override
            protected void applyPreset(M3TimePreset preset) {
                picker.applyPreset(preset);
            }

            /// Returns whether one time preset is outside the selectable bounds.
            @Override
            protected boolean isPresetDisabled(M3TimePreset preset) {
                return picker.isTimeDisabled(preset.time());
            }
        };
        presetBoundsInvalidation = observable -> presetController.refreshDisabledStates();
        initialize();
        setValue(value);
    }

    /// The selected time for this dialog.
    ///
    /// Direct non-null assignments discard seconds and nanoseconds, then validate the normalized time against the
    /// picker's inclusive bounds. Assigning `null` clears selection and disables the OK action. A binding source
    /// must provide minute-precision values within those bounds. User selection updates this property while it is
    /// not bound.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalTime> value;

    /// Returns the selected time, or `null` when no time is selected.
    ///
    /// @return the selected time, or `null` when no time is selected
    public @Nullable LocalTime getValue() {
        return value.get();
    }

    /// Sets the selected time, or clears selection when `null` is supplied.
    ///
    /// Seconds and nanoseconds are discarded. Values outside the current selectable range are rejected. A successful
    /// change updates the confirmation action immediately.
    ///
    /// @param value the selected time, or `null` to clear selection
    /// @throws IllegalArgumentException if `value` is outside the current selectable range
    public void setValue(@Nullable LocalTime value) {
        this.value.set(value);
    }

    /// Returns the selected minute-precision time property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`. A binding source must provide
    /// either `null` or a value whose seconds and nanoseconds are zero and which lies within the current selectable
    /// bounds. While the property is bound, user selection cannot replace the bound value.
    ///
    /// @return the selected-time property
    public ObjectProperty<@Nullable LocalTime> valueProperty() {
        return value;
    }

    /// Returns the time picker used by this dialog.
    ///
    /// @return the time picker used by this dialog
    public M3TimePicker getPicker() {
        return picker;
    }

    /// Returns the mutable time preset list.
    ///
    /// The returned list is live, mutable, ordered, and rejects `null` elements. Mutations update the dialog action
    /// column immediately. Duplicate presets are retained as separate actions in list order. Presets outside the
    /// current selectable range remain in the list but are disabled.
    ///
    /// @return the live mutable time preset list
    public ObservableList<M3TimePreset> getPresets() {
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
        pane.setHeaderText(DEFAULT_TITLE);
        pane.setContent(presetContent);
        picker.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetList.setAlignment(Pos.TOP_LEFT);
        M3PresetNavigation.installColumn(
                presetList,
                pane,
                () -> M3Accessible.requestAccessibleFocus(pane, picker)
        );
        cancelAction.setCancelButton(true);
        confirmAction.setDefaultButton(true);
        pane.getActions().setAll(cancelAction, confirmAction);
        value.addListener((observable, oldValue, newValue) -> updateOkButtonState());
        picker.minTimeProperty().addListener(presetBoundsInvalidation);
        picker.maxTimeProperty().addListener(presetBoundsInvalidation);
        presetController.install();
        updateOkButtonState();
    }

    /// Creates the dialog-owned value property synchronized with the already constructed picker.
    private ObjectProperty<@Nullable LocalTime> createValueProperty() {
        return new SimpleObjectProperty<>(this, "value") {
            /// Validates and normalizes direct writes through the picker before committing the public value.
            @Override
            public void set(@Nullable LocalTime newValue) {
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

            /// Synchronizes valid values supplied by a binding source with the time selection.
            @Override
            protected void invalidated() {
                if (!isBound() || synchronizingValue) {
                    return;
                }

                @Nullable LocalTime selectedTime = get();
                if (selectedTime != null
                        && (selectedTime.getSecond() != 0
                        || selectedTime.getNano() != 0
                        || picker.isTimeDisabled(selectedTime))) {
                    return;
                }

                synchronizingValue = true;
                try {
                    picker.setValue(selectedTime);
                } finally {
                    synchronizingValue = false;
                }
            }
        };
    }

    /// Enables the OK button only when a selected time exists.
    private void updateOkButtonState() {
        confirmAction.setDisable(getValue() == null);
    }

    /// Dialog pane that owns the specification's bottom-row mode switch.
    @NotNullByDefault
    private static final class TimePickerDialogPane extends M3DialogPane {
        /// The picker rendered by this pane.
        private final M3TimePicker picker = new M3TimePicker();

        /// The mode icon updated in place when the picker variant changes.
        private final M3InternalIcon modeIcon = new M3InternalIcon(
                M3InternalIcon.Glyph.KEYBOARD,
                M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
        );

        /// The mode switch placed at the logical start of the dialog action row.
        private final M3IconButton modeButton = new M3IconButton(modeIcon);

        /// Creates the specialized pane and its retained mode switch.
        private TimePickerDialogPane() {
            picker.getStyleClass().add(TIME_PICKER_DIALOG_CONTENT_STYLE_CLASS);
            picker.pseudoClassStateChanged(DIALOG_EMBEDDED_PSEUDO_CLASS, true);
            modeButton.getStyleClass().add(TIME_PICKER_MODE_BUTTON_STYLE_CLASS);
            modeButton.setOnAction(event -> {
                picker.setInputMode(!picker.isInputMode());
                event.consume();
            });
            setLeadingAction(modeButton);
            picker.inputModeProperty().addListener(observable -> updateModeButton());
            updateModeButton();
        }

        /// Updates the retained mode icon and accessibility label.
        private void updateModeButton() {
            boolean inputMode = picker.isInputMode();
            modeIcon.setGlyph(inputMode ? M3InternalIcon.Glyph.SCHEDULE : M3InternalIcon.Glyph.KEYBOARD);
            modeButton.setAccessibleText(inputMode ? "Use clock dial" : "Use keyboard input");
        }
    }
}
