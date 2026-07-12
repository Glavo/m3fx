// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3PickerFieldSkin;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/// A Material Design 3 time picker field that combines an editable text field with a popup time picker.
///
/// `M3TimePickerField` lets users type a time or choose one from an embedded [M3TimePicker]. It exposes
/// nullable selected-time state, parsing and formatting behavior, popup visibility, and optional preset actions
/// for inline form use.
///
/// See [Material Design time pickers](https://m3.material.io/components/time-pickers/overview).
@NotNullByDefault
public final class M3TimePickerField extends M3PickerField<LocalTime, M3TimePicker> {
    /// The style class applied to time picker field controls.
    public static final String STYLE_CLASS = "m3-time-picker-field";

    /// The style class applied to time picker field popup surfaces.
    public static final String POPUP_STYLE_CLASS = "m3-time-picker-field-popup";

    /// The style class applied to popup content when preset actions are visible.
    public static final String PRESET_CONTENT_STYLE_CLASS = "m3-time-picker-field-preset-content";

    /// The style class applied to the popup preset action column.
    public static final String PRESET_LIST_STYLE_CLASS = "m3-time-picker-field-preset-list";

    /// The style class applied to each popup preset action button.
    public static final String PRESET_BUTTON_STYLE_CLASS = "m3-time-picker-field-preset-button";

    /// The mutable preset list rendered before the popup picker.
    private final ObservableList<M3TimePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The wrapper used when the popup renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3TimePreset> presetsListener = change -> updatePresetContent();

    /// Creates an empty time picker field.
    public M3TimePickerField() {
        this(new M3TimePicker());
    }

    /// Creates a time picker field initialized with the supplied value.
    ///
    /// @param value the initially selected time
    public M3TimePickerField(LocalTime value) {
        this(new M3TimePicker());
        setValue(value);
    }

    /// Creates a time picker field around a fresh popup time picker.
    private M3TimePickerField(M3TimePicker picker) {
        super(
                picker,
                picker.valueProperty(),
                DateTimeFormatter.ofPattern("HH:mm"),
                STYLE_CLASS,
                POPUP_STYLE_CLASS,
                new M3InternalIcon(
                        M3InternalIcon.Glyph.SCHEDULE,
                        M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
                ),
                "Open time picker",
                "Enter a valid time",
                "Time is outside the selectable range"
        );
        initializePresetContent();
    }

    /// Returns whether this field can reveal the supplied accessibility time target.
    @Override
    protected boolean handlesAccessibleShowTarget(@Nullable Object parameter) {
        return parameter instanceof LocalTime time && !getPicker().isTimeDisabled(time);
    }

    /// Returns the mutable time preset list rendered in the popup.
    ///
    /// @return the mutable time preset list rendered in the popup
    public ObservableList<M3TimePreset> getPresets() {
        return presets;
    }

    /// Applies a time preset, updates the editor, and closes the popup when it is showing.
    ///
    /// @param preset the time preset to apply
    private void applyPreset(M3TimePreset preset) {
        setValue(Objects.requireNonNull(preset, "preset").time());
        if (isShowing()) {
            hidePicker();
        }
    }

    /// Creates the default Material Design 3 picker field skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3PickerFieldSkin<>(this);
    }

    /// Parses one editor time string.
    @Override
    protected LocalTime parseValue(String text, DateTimeFormatter formatter) {
        return LocalTime.from(formatter.parse(text));
    }

    /// Formats one time value for editor display.
    @Override
    protected String formatValue(LocalTime value, DateTimeFormatter formatter) {
        return formatter.format(value);
    }

    /// Clears seconds and nanos because this field edits hour and minute precision.
    @Override
    protected LocalTime normalizeValue(LocalTime value) {
        return Objects.requireNonNull(value, "value").withSecond(0).withNano(0);
    }

    /// Returns whether a time is outside the popup picker's selectable range.
    @Override
    protected boolean isPickerValueDisabled(LocalTime value) {
        return getPicker().isTimeDisabled(value);
    }

    /// Applies a value to the popup time picker.
    @Override
    protected void setPickerValue(@Nullable LocalTime value) {
        getPicker().setValue(value);
    }

    /// Configures popup preset containers and listeners.
    private void initializePresetContent() {
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetContent.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(this));
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetList.alignmentProperty().bind(M3NodeLayout.createLogicalStartTopAlignmentBinding(this));
        M3PresetNavigation.installColumn(presetList, this, () -> M3Accessible.requestAccessibleFocus(this, getPicker()));
        getPicker().minTimeProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        getPicker().maxTimeProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        presets.addListener(presetsListener);
    }

    /// Refreshes preset state and clears the selected time when picker bounds exclude it.
    private void handleSelectableBoundsChanged() {
        updatePresetContent();
        clearValueIfOutOfBounds();
    }

    /// Clears the selected time when current bounds exclude it.
    private void clearValueIfOutOfBounds() {
        @Nullable LocalTime value = getValue();
        if (value != null && getPicker().isTimeDisabled(value)) {
            setValue(null);
        }
    }

    /// Rebuilds popup content from the current preset list.
    private void updatePresetContent() {
        presetContent.getChildren().clear();
        presetList.getChildren().clear();

        if (presets.isEmpty()) {
            resetPopupContent();
            return;
        }

        for (M3TimePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, getPicker());
        setPopupContent(presetContent);
    }

    /// Creates one popup preset action button.
    private M3Button createPresetButton(M3TimePreset preset) {
        M3Button button = new M3Button(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        M3Css.setMaxWidthIfUnbound(button, Double.MAX_VALUE);
        button.setDisable(getPicker().isTimeDisabled(preset.time()));
        button.setOnAction(event -> {
            applyPreset(preset);
            event.consume();
        });
        return button;
    }

}
