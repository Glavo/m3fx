// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3PickerPresetController;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;

/// A Material Design 3 dialog preset for selecting one time.
///
/// The dialog installs an [M3TimePicker] as its content, wires OK and cancel actions, and keeps the selected
/// [LocalTime] as the dialog result when the user accepts the choice. Its Dial/Input mode switch shares the
/// specification's bottom action row with the dialog actions.
///
/// The inherited [#show()] method is non-blocking and [#showAndWait()] waits through a JavaFX nested event loop.
/// Cancel closes the dialog with a `null` result; OK is disabled until a value is selected and closes with the
/// selected minute-precision time. The dialog is rendered inside its owner scene, so configure an owner node before
/// showing it:
///
/// ```java
/// M3TimePickerDialog dialog = new M3TimePickerDialog(LocalTime.of(9, 30));
/// dialog.setOwner(ownerNode);
/// LocalTime acceptedTime = dialog.showAndWait();
/// ```
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
@NotNullByDefault
public final class M3TimePickerDialog extends M3Dialog<LocalTime> {
    /// The default headline text for time picker dialogs.
    private static final String DEFAULT_TITLE = "Select time";

    /// The style class applied to dialog content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-time-picker-dialog-preset-content";

    /// The style class applied to the preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-time-picker-dialog-preset-list";

    /// The style class applied to each preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-time-picker-dialog-preset-button";

    /// The pseudo-class that delegates mode-switch placement to the dialog action row.
    private static final PseudoClass DIALOG_EMBEDDED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("dialog-embedded");

    /// The private button type used to place the mode switch at the logical start of the action row.
    private static final ButtonType MODE_SWITCH_BUTTON_TYPE =
            new ButtonType("", ButtonBar.ButtonData.LEFT);

    /// The Material action order: mode switch, flexible gap, cancel, and confirmation.
    private static final String BUTTON_ORDER = ButtonBar.ButtonData.LEFT.getTypeCode()
            + ButtonBar.ButtonData.BIG_GAP.getTypeCode()
            + ButtonBar.ButtonData.CANCEL_CLOSE.getTypeCode()
            + ButtonBar.ButtonData.OK_DONE.getTypeCode();

    /// The time picker displayed as dialog content.
    private final M3TimePicker picker;

    /// The selected time synchronized with the embedded picker.
    ///
    /// Non-null assignments discard seconds and nanoseconds, then validate the normalized time against the picker's
    /// inclusive bounds. Assigning `null` clears selection and disables the OK action. Changes made through the
    /// embedded picker update this property as well.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalTime> value;

    /// Whether the dialog and embedded picker are currently synchronizing selected values.
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
    /// The dialog headline is `Select time`, the OK action is disabled, and no owner is configured. The
    /// embedded picker uses its standard defaults.
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

    /// Returns the time picker displayed by this dialog.
    ///
    /// @return the time picker displayed by this dialog
    public final M3TimePicker getPicker() {
        return picker;
    }

    /// Returns the mutable time preset list.
    ///
    /// The returned list is live, mutable, ordered, and rejects `null` elements. Mutations update the dialog action
    /// column immediately. Duplicate presets are retained as separate actions in list order. Presets outside the
    /// picker's current bounds remain in the list but are disabled.
    ///
    /// @return the live mutable time preset list
    public final ObservableList<M3TimePreset> getPresets() {
        return presets;
    }

    /// Returns the selected time, or `null` when no time is selected.
    ///
    /// @return the selected time, or `null` when no time is selected
    public final @Nullable LocalTime getValue() {
        return value.get();
    }

    /// Sets the selected time, or clears selection when `null` is supplied.
    ///
    /// The embedded picker normalizes seconds and nanoseconds and rejects values outside its current bounds. A
    /// successful change updates the OK action immediately.
    ///
    /// @param value the selected time, or `null` to clear selection
    /// @throws IllegalArgumentException if `value` is outside the embedded picker's selectable range
    public final void setValue(@Nullable LocalTime value) {
        this.value.set(value);
    }

    public final ObjectProperty<@Nullable LocalTime> valueProperty() {
        return value;
    }

    /// Configures dialog content, buttons, result conversion, and button state.
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
        presetContent.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(pane));
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(pane.effectiveNodeOrientationProperty());
        presetList.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(pane));
        M3PresetNavigation.installColumn(
                presetList,
                pane,
                () -> M3Accessible.requestAccessibleFocus(pane, picker)
        );
        pane.getButtonTypes().setAll(MODE_SWITCH_BUTTON_TYPE, ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(buttonType -> buttonType == ButtonType.OK ? getValue() : null);
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
        };
    }

    /// Enables the OK button only when a selected time exists.
    private void updateOkButtonState() {
        @Nullable Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(getValue() == null);
        }
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

        /// The mode switch placed by ButtonBar at logical start.
        private final M3IconButton modeButton = new M3IconButton(modeIcon);

        /// Creates the specialized pane and its retained mode switch.
        private TimePickerDialogPane() {
            picker.getStyleClass().add(M3TimePicker.DIALOG_CONTENT_STYLE_CLASS);
            picker.pseudoClassStateChanged(DIALOG_EMBEDDED_PSEUDO_CLASS, true);
            modeButton.getStyleClass().add(M3TimePicker.MODE_BUTTON_STYLE_CLASS);
            modeButton.setOnAction(event -> {
                picker.setInputMode(!picker.isInputMode());
                event.consume();
            });
            picker.inputModeProperty().addListener(observable -> updateModeButton());
            updateModeButton();
        }


        /// Creates the action row with a flexible gap between the mode switch and text actions.
        @Override
        protected ButtonBar createButtonBar() {
            ButtonBar buttonBar = super.createButtonBar();
            buttonBar.setButtonOrder(BUTTON_ORDER);
            return buttonBar;
        }

        /// Creates the retained mode switch for its private button type.
        @Override
        protected Node createButton(ButtonType buttonType) {
            if (buttonType == MODE_SWITCH_BUTTON_TYPE) {
                ButtonBar.setButtonData(modeButton, ButtonBar.ButtonData.LEFT);
                ButtonBar.setButtonUniformSize(modeButton, false);
                return modeButton;
            }
            return super.createButton(buttonType);
        }

        /// Updates the retained mode icon and accessibility label.
        private void updateModeButton() {
            boolean inputMode = picker.isInputMode();
            modeIcon.setGlyph(inputMode ? M3InternalIcon.Glyph.SCHEDULE : M3InternalIcon.Glyph.KEYBOARD);
            modeButton.setAccessibleText(inputMode ? "Use clock dial" : "Use keyboard input");
        }
    }
}
