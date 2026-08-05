// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3PickerFieldSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/// A Material Design 3 date picker field that combines an editable text field with a popup calendar.
///
/// Users may type a date or choose one from the owned [picker][#getPicker()]. Editor text may be temporarily
/// invalid and does not replace the selected value until [commitEditorText][M3PickerField#commitEditorText()]
/// succeeds. Selecting a calendar date or preset updates the value and editor text; selecting a preset also closes
/// the popup. The field initially uses [DateTimeFormatter#ISO_LOCAL_DATE].
///
/// The popup can be opened with the trailing button, Down, or F4 and dismissed with Escape. It is attached to the
/// field's window only while showing and inherits the field's theme, stylesheets, and node orientation. Bounds and
/// calendar presentation are configured through [getPicker()].
///
/// ```java
/// private M3DatePickerField createDateField() {
///     LocalDate today = LocalDate.now();
///     M3DatePickerField field = new M3DatePickerField();
///     field.setLabelText("Delivery date");
///     field.getPicker().setMinDate(today);
///     field.getPicker().setMaxDate(today.plusMonths(3));
///     field.getPresets().addAll(M3DatePresets.common(today));
///     return field;
/// }
/// ```
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DatePickerField extends M3PickerField<LocalDate, M3DatePicker> {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-date-picker-field";

    /// The style class applied to date picker field popup surfaces.
    private static final String POPUP_STYLE_CLASS = "m3-date-picker-field-popup";

    /// The style class applied to popup content when preset actions are visible.
    private static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-picker-field-preset-content";

    /// The style class applied to the popup preset action column.
    private static final String PRESET_LIST_STYLE_CLASS = "m3-date-picker-field-preset-list";

    /// The style class applied to each popup preset action button.
    private static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-picker-field-preset-button";

    /// The live, mutable, ordered list of presets rendered before the popup picker.
    ///
    /// The list initially is empty, rejects `null` elements, permits duplicates, and observes additions, removals,
    /// and reordering. Presets outside the current picker bounds remain visible but disabled.
    private final ObservableList<M3DatePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The wrapper used when the popup renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3DatePreset> presetsListener = change -> updatePresetContent();

    /// Creates an empty date picker field using ISO local-date text and an unbounded calendar.
    public M3DatePickerField() {
        this(new M3DatePicker());
    }

    /// Creates a date picker field initialized with the specified selected value.
    ///
    /// @param value the initially selected date
    public M3DatePickerField(LocalDate value) {
        this(new M3DatePicker());
        setValue(value);
    }

    /// Creates a date picker field around a fresh popup date picker.
    private M3DatePickerField(M3DatePicker picker) {
        super(
                picker,
                picker.valueProperty(),
                DateTimeFormatter.ISO_LOCAL_DATE,
                DEFAULT_STYLE_CLASS,
                POPUP_STYLE_CLASS,
                new M3InternalIcon(
                        M3InternalIcon.Glyph.CALENDAR,
                        M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
                ),
                "Open date picker",
                "Enter a valid date",
                "Date is outside the selectable range"
        );
        initializePresetContent();
    }

    /// Returns whether this field can reveal the supplied accessibility date target.
    @Override
    boolean handlesAccessibleShowTarget(@Nullable Object parameter) {
        return parameter instanceof LocalDate date && !getPicker().isDateDisabled(date);
    }

    /// Returns the live, mutable date preset list rendered in the popup.
    ///
    /// @return the live, mutable date preset list rendered in the popup
    public ObservableList<M3DatePreset> getPresets() {
        return presets;
    }

    /// Applies a date preset, updates the editor, and closes the popup when it is showing.
    ///
    /// @param preset the date preset to apply
    private void applyPreset(M3DatePreset preset) {
        LocalDate date = Objects.requireNonNull(preset, "preset").date();
        setValue(date);
        getPicker().showMonth(YearMonth.from(date));
        if (isShowing()) {
            hidePicker();
        }
    }

    /// Creates the default Material Design 3 picker field skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3PickerFieldSkin<>(this, getInputLayout());
    }

    /// Parses one editor date string.
    @Override
    LocalDate parseValue(String text, DateTimeFormatter formatter) {
        return LocalDate.from(formatter.parse(text));
    }

    /// Formats one date value for editor display.
    @Override
    String formatValue(LocalDate value, DateTimeFormatter formatter) {
        return formatter.format(value);
    }

    /// Dates already use the popup picker's precision.
    @Override
    LocalDate normalizeValue(LocalDate value) {
        return Objects.requireNonNull(value, "value");
    }

    /// Returns whether a date is outside the popup picker's selectable range.
    @Override
    boolean isPickerValueDisabled(LocalDate value) {
        return getPicker().isDateDisabled(value);
    }

    /// Applies a value to the popup date picker.
    @Override
    void setPickerValue(@Nullable LocalDate value) {
        getPicker().setValue(value);
    }

    /// Configures popup preset containers and listeners.
    private void initializePresetContent() {
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetList.setAlignment(Pos.TOP_LEFT);
        M3PresetNavigation.installColumn(presetList, this, () -> M3Accessible.requestAccessibleFocus(this, getPicker()));
        getPicker().minDateProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        getPicker().maxDateProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        presets.addListener(presetsListener);
    }

    /// Refreshes preset and field validation state after picker bounds change.
    private void handleSelectableBoundsChanged() {
        updatePresetContent();
        selectableRangeChanged();
    }

    /// Rebuilds popup content from the current preset list.
    private void updatePresetContent() {
        presetContent.getChildren().clear();
        presetList.getChildren().clear();

        if (presets.isEmpty()) {
            resetPopupContent();
            return;
        }

        for (M3DatePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, getPicker());
        setPopupContent(presetContent);
    }

    /// Creates one popup preset action button.
    private M3Button createPresetButton(M3DatePreset preset) {
        M3Button button = new M3Button(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        M3Css.setMaxWidthIfUnbound(button, Double.MAX_VALUE);
        button.setDisable(getPicker().isDateDisabled(preset.date()));
        button.setOnAction(event -> {
            applyPreset(preset);
            event.consume();
        });
        return button;
    }

}
