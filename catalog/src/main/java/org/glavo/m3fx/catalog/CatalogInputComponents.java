// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.glavo.m3fx.controls.M3ChipStyle;
import org.glavo.m3fx.controls.M3ColorPlane;
import org.glavo.m3fx.controls.M3NumberFieldCommitBehavior;
import org.glavo.m3fx.controls.M3SearchViewLayout;
import org.glavo.m3fx.controls.M3SearchViewStyle;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SliderSize;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Supplies selection and text-input entries for the Catalog registry.
@NotNullByDefault
final class CatalogInputComponents {
    /// Prevents utility class instantiation.
    private CatalogInputComponents() {
    }

    /// Creates the input component descriptors.
    ///
    /// @return the immutable descriptor list
    static @Unmodifiable List<CatalogComponent> create() {
        return List.of(
                CatalogComponents.component(
                        "Checkboxes",
                        "Checkboxes select one or more options and can represent an indeterminate aggregate state.",
                        CatalogIcons.CHECKBOX,
                        "checkbox",
                        "M3CheckBox",
                        checkBoxExamples()
                ),
                CatalogComponents.component(
                        "Chips",
                        "Chips represent compact actions, filters, input values, and suggestions.",
                        CatalogIcons.CHIP,
                        "chips",
                        "M3Chip",
                        chipExamples()
                ),
                CatalogComponents.extensionComponent(
                        "Color pickers",
                        "Color pickers combine a color plane, channels, fields, and optional preset swatches.",
                        CatalogIcons.PALETTE,
                        "https://react-spectrum.adobe.com/ColorArea",
                        "M3ColorPicker",
                        colorPickerExamples()
                ),
                CatalogComponents.component(
                        "Date pickers",
                        "Date pickers let users select a date from a calendar-oriented surface.",
                        CatalogIcons.CALENDAR,
                        "date-pickers",
                        "M3DatePicker",
                        datePickerExamples()
                ),
                CatalogComponents.extensionComponent(
                        "Forms",
                        "Forms align related labels, supporting text, inputs, and settings into structured sections.",
                        CatalogIcons.FORM,
                        "https://m3.material.io/components/text-fields/overview",
                        "M3FormPane",
                        formExamples()
                ),
                CatalogComponents.extensionComponent(
                        "Number fields",
                        "Number fields edit localized numeric values with range-aware step actions and commit policies.",
                        CatalogIcons.TEXT_FIELD,
                        "https://react-spectrum.adobe.com/NumberField",
                        "M3NumberField",
                        numberFieldExamples()
                ),
                CatalogComponents.component(
                        "Radio buttons",
                        "Radio buttons select exactly one option from a mutually exclusive group.",
                        CatalogIcons.RADIO,
                        "radio-button",
                        "M3RadioButton",
                        radioButtonExamples()
                ),
                CatalogComponents.component(
                        "Search",
                        "Search combines a query entry bar with optional suggestion and result content.",
                        CatalogIcons.SEARCH,
                        "search",
                        "M3SearchBar",
                        searchExamples()
                ),
                CatalogComponents.component(
                        "Segmented buttons",
                        "Segmented buttons select one or more options from a connected set.",
                        CatalogIcons.SEGMENTED_BUTTON,
                        "segmented-buttons",
                        "M3SegmentedButtonGroup",
                        segmentedButtonExamples()
                ),
                CatalogComponents.extensionComponent(
                        "Settings",
                        "Setting rows combine list semantics with actions, switches, checkboxes, and radio choices.",
                        CatalogIcons.SETTINGS,
                        "https://m3.material.io/components/lists/overview",
                        "M3SettingItem",
                        settingExamples()
                ),
                CatalogComponents.component(
                        "Sliders",
                        "Sliders choose a single value or range along a continuous or discrete track.",
                        CatalogIcons.SLIDER,
                        "sliders",
                        "M3Slider",
                        sliderExamples()
                ),
                CatalogComponents.component(
                        "Switches",
                        "Switches immediately toggle a single binary setting.",
                        CatalogIcons.SWITCH,
                        "switch",
                        "M3Switch",
                        switchExamples()
                ),
                CatalogComponents.component(
                        "Text fields",
                        "Text fields accept and validate user-entered text in filled or outlined containers.",
                        CatalogIcons.TEXT_FIELD,
                        "text-fields",
                        "M3TextField",
                        textFieldExamples()
                ),
                CatalogComponents.component(
                        "Time pickers",
                        "Time pickers let users select hours and minutes with clock-oriented controls.",
                        CatalogIcons.TIME,
                        "time-pickers",
                        "M3TimePicker",
                        timePickerExamples()
                )
        );
    }

    /// Creates the checkbox state examples.
    ///
    /// @return the complete checkbox example array
    private static CatalogExample[] checkBoxExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Unchecked checkbox",
                        "An enabled checkbox in its unselected state.",
                        false,
                        () -> CatalogInputSamples.checkBox(false, false, false, false)
                ),
                CatalogComponents.example(
                        "Checked checkbox",
                        "An enabled checkbox in its selected state.",
                        false,
                        () -> CatalogInputSamples.checkBox(true, false, false, false)
                ),
                CatalogComponents.example(
                        "Indeterminate checkbox",
                        "A checkbox representing an aggregate mixed state.",
                        false,
                        () -> CatalogInputSamples.checkBox(false, true, false, false)
                ),
                CatalogComponents.example(
                        "Error checkbox",
                        "An unselected checkbox using the error color mapping.",
                        false,
                        () -> CatalogInputSamples.checkBox(false, false, true, false)
                ),
                CatalogComponents.example(
                        "Disabled checkbox",
                        "A selected checkbox with interaction disabled.",
                        false,
                        () -> CatalogInputSamples.checkBox(true, false, false, true)
                )
        };
    }

    /// Creates examples for all four chip roles, styles, graphics, and states.
    ///
    /// @return the complete chip example array
    private static CatalogExample[] chipExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Assist chip",
                        "A flat contextual action without a graphic.",
                        false,
                        () -> CatalogInputSamples.assistChip(M3ChipStyle.FLAT, false, false)
                ),
                CatalogComponents.example(
                        "Assist chip with icon",
                        "A flat contextual action with a leading icon.",
                        false,
                        () -> CatalogInputSamples.assistChip(M3ChipStyle.FLAT, true, false)
                ),
                CatalogComponents.example(
                        "Elevated assist chip",
                        "A contextual action elevated above a visually complex surface.",
                        false,
                        () -> CatalogInputSamples.assistChip(M3ChipStyle.ELEVATED, true, false)
                ),
                CatalogComponents.example(
                        "Disabled assist chip",
                        "An unavailable contextual action.",
                        false,
                        () -> CatalogInputSamples.assistChip(M3ChipStyle.FLAT, true, true)
                ),
                CatalogComponents.example(
                        "Filter chip",
                        "An unselected flat filtering choice.",
                        false,
                        () -> CatalogInputSamples.filterChip(M3ChipStyle.FLAT, false, false, false)
                ),
                CatalogComponents.example(
                        "Selected filter chip",
                        "A selected filtering choice.",
                        false,
                        () -> CatalogInputSamples.filterChip(M3ChipStyle.FLAT, true, false, false)
                ),
                CatalogComponents.example(
                        "Filter chip with icon",
                        "A selected filtering choice with a leading state graphic.",
                        false,
                        () -> CatalogInputSamples.filterChip(M3ChipStyle.FLAT, true, true, false)
                ),
                CatalogComponents.example(
                        "Elevated filter chip",
                        "An elevated filtering choice in its unselected state.",
                        false,
                        () -> CatalogInputSamples.filterChip(M3ChipStyle.ELEVATED, false, true, false)
                ),
                CatalogComponents.example(
                        "Input chip",
                        "A compact representation of an entered value.",
                        false,
                        () -> CatalogInputSamples.inputChip(false, false, false, false)
                ),
                CatalogComponents.example(
                        "Input chip with avatar",
                        "An input value with a leading entity graphic.",
                        false,
                        () -> CatalogInputSamples.inputChip(false, true, false, false)
                ),
                CatalogComponents.example(
                        "Selected removable input chip",
                        "A selected input value with a trailing remove action.",
                        false,
                        () -> CatalogInputSamples.inputChip(true, true, true, false)
                ),
                CatalogComponents.example(
                        "Disabled input chip",
                        "An unavailable input value retaining its graphics.",
                        false,
                        () -> CatalogInputSamples.inputChip(false, true, true, true)
                ),
                CatalogComponents.example(
                        "Suggestion chip",
                        "A flat suggested response.",
                        false,
                        () -> CatalogInputSamples.suggestionChip(M3ChipStyle.FLAT, false, false)
                ),
                CatalogComponents.example(
                        "Suggestion chip with icon",
                        "A suggested response with a leading graphic.",
                        false,
                        () -> CatalogInputSamples.suggestionChip(M3ChipStyle.FLAT, true, false)
                ),
                CatalogComponents.example(
                        "Elevated suggestion chip",
                        "A suggested response using an elevated container.",
                        false,
                        () -> CatalogInputSamples.suggestionChip(M3ChipStyle.ELEVATED, true, false)
                ),
                CatalogComponents.example(
                        "Disabled suggestion chip",
                        "An unavailable suggested response.",
                        false,
                        () -> CatalogInputSamples.suggestionChip(M3ChipStyle.FLAT, false, true)
                )
        };
    }

    /// Creates the color-control examples.
    ///
    /// @return the complete color-picker example array
    private static CatalogExample[] colorPickerExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Complete color picker",
                        "An editable color with a wheel and preset palette.",
                        false,
                        CatalogInputSamples::colorPicker
                ),
                CatalogComponents.example(
                        "HSB color area",
                        "A saturation-and-brightness plane that preserves hue.",
                        false,
                        () -> CatalogInputSamples.colorArea(M3ColorPlane.HSB_SATURATION_BRIGHTNESS)
                ),
                CatalogComponents.example(
                        "HSL color area",
                        "A saturation-and-lightness plane that preserves hue.",
                        false,
                        () -> CatalogInputSamples.colorArea(M3ColorPlane.HSL_SATURATION_LIGHTNESS)
                ),
                CatalogComponents.example(
                        "RGB color area",
                        "A red-and-green plane that preserves blue.",
                        false,
                        () -> CatalogInputSamples.colorArea(M3ColorPlane.RGB_RED_GREEN)
                ),
                CatalogComponents.example(
                        "Hue controls",
                        "A color wheel beside a vertical hue slider.",
                        false,
                        CatalogInputSamples::hueControls
                ),
                CatalogComponents.example(
                        "Channel sliders",
                        "Hue, saturation, brightness, and alpha channel controls.",
                        false,
                        CatalogInputSamples::colorSliders
                ),
                CatalogComponents.example(
                        "Hexadecimal fields",
                        "Opaque, alpha-enabled, and disabled color fields.",
                        false,
                        CatalogInputSamples::colorFields
                ),
                CatalogComponents.example(
                        "Swatch picker",
                        "A selectable grid of preset colors.",
                        false,
                        CatalogInputSamples::swatchPicker
                ),
                CatalogComponents.example(
                        "Swatch sizes and shapes",
                        "Every swatch size with default, square, and fully rounded corners.",
                        false,
                        CatalogInputSamples::swatches
                ),
                CatalogComponents.example(
                        "Transparency and no color",
                        "Translucent, absent, and named swatch states.",
                        false,
                        CatalogInputSamples::specialSwatches
                )
        };
    }

    /// Creates date-field and inline-picker examples.
    ///
    /// @return the complete date-picker example array
    private static CatalogExample[] datePickerExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Outlined date field",
                        "An editable outlined date field with presets.",
                        false,
                        () -> CatalogInputSamples.dateField(M3TextInputVariant.OUTLINED, false)
                ),
                CatalogComponents.example(
                        "Bounded filled date field",
                        "A filled field restricted to the next two weeks.",
                        false,
                        () -> CatalogInputSamples.dateField(M3TextInputVariant.FILLED, true)
                ),
                CatalogComponents.example(
                        "Date range field",
                        "Editable range endpoints with common presets.",
                        false,
                        CatalogInputSamples::dateRangeField
                ),
                CatalogComponents.example(
                        "Selected date",
                        "An inline calendar with one selected date.",
                        false,
                        CatalogInputSamples::selectedDatePicker
                ),
                CatalogComponents.example(
                        "Bounded date picker",
                        "An inline calendar with minimum and maximum dates.",
                        false,
                        CatalogInputSamples::boundedDatePicker
                ),
                CatalogComponents.example(
                        "Date range picker",
                        "An inline calendar with an inclusive selected range.",
                        false,
                        CatalogInputSamples::dateRangePicker
                ),
                CatalogComponents.example(
                        "Month-only date picker",
                        "A calendar that omits adjacent-month days.",
                        false,
                        CatalogInputSamples::monthOnlyDatePicker
                )
        };
    }

    /// Creates structured form and validation examples.
    ///
    /// @return the complete form example array
    private static CatalogExample[] formExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Single-section form",
                        "Aligned labels, descriptions, and editable controls.",
                        false,
                        () -> CatalogInputSamples.form(false, false)
                ),
                CatalogComponents.example(
                        "Multi-section form",
                        "Account and preference sections sharing one form grid.",
                        false,
                        () -> CatalogInputSamples.form(true, false)
                ),
                CatalogComponents.example(
                        "Validated form",
                        "A form validator and summary coordinating invalid inputs.",
                        false,
                        () -> CatalogInputSamples.form(true, true)
                )
        };
    }

    /// Creates interactive and disabled radio-button examples.
    ///
    /// @return the complete radio-button example array
    private static CatalogExample[] radioButtonExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Selection group",
                        "A mutually exclusive radio-button group.",
                        false,
                        () -> CatalogInputSamples.radioButtons(false)
                ),
                CatalogComponents.example(
                        "Disabled states",
                        "Selected and unselected radio buttons with interaction disabled.",
                        false,
                        () -> CatalogInputSamples.radioButtons(true)
                )
        };
    }

    /// Creates search bar and view examples.
    ///
    /// @return the complete search example array
    private static CatalogExample[] searchExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Empty search bar",
                        "An empty query field with a trailing clear action.",
                        false,
                        () -> CatalogInputSamples.searchBar(false)
                ),
                CatalogComponents.example(
                        "Populated search bar",
                        "A search bar containing an existing query.",
                        false,
                        () -> CatalogInputSamples.searchBar(true)
                ),
                CatalogComponents.example(
                        "Contained docked search",
                        "Contained results below a docked search input.",
                        false,
                        () -> CatalogInputSamples.searchView(
                                M3SearchViewLayout.DOCKED,
                                M3SearchViewStyle.CONTAINED
                        )
                ),
                CatalogComponents.example(
                        "Divided docked search",
                        "Divided results below a docked search input.",
                        true,
                        () -> CatalogInputSamples.searchView(
                                M3SearchViewLayout.DOCKED,
                                M3SearchViewStyle.DIVIDED
                        )
                ),
                CatalogComponents.example(
                        "Contained full-screen search",
                        "Contained results using the full-screen search layout.",
                        false,
                        () -> CatalogInputSamples.searchView(
                                M3SearchViewLayout.FULL_SCREEN,
                                M3SearchViewStyle.CONTAINED
                        )
                ),
                CatalogComponents.example(
                        "Divided full-screen search",
                        "Divided results using the full-screen search layout.",
                        true,
                        () -> CatalogInputSamples.searchView(
                                M3SearchViewLayout.FULL_SCREEN,
                                M3SearchViewStyle.DIVIDED
                        )
                )
        };
    }

    /// Creates segmented-button selection examples.
    ///
    /// @return the complete segmented-button example array
    private static CatalogExample[] segmentedButtonExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Text single selection",
                        "A connected single-select group with text labels.",
                        false,
                        () -> CatalogInputSamples.segmentedButtons(M3SelectionMode.SINGLE, false)
                ),
                CatalogComponents.example(
                        "Icon and label",
                        "A single-select segmented group with leading graphics.",
                        false,
                        () -> CatalogInputSamples.segmentedButtons(M3SelectionMode.SINGLE, true)
                ),
                CatalogComponents.example(
                        "Icon multi selection",
                        "A connected group with independent selected segments.",
                        false,
                        () -> CatalogInputSamples.segmentedButtons(M3SelectionMode.MULTIPLE, true)
                )
        };
    }

    /// Creates the setting-row examples.
    ///
    /// @return the complete settings example array
    private static CatalogExample[] settingExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Action settings",
                        "Rows that open related configuration destinations.",
                        false,
                        CatalogInputSamples::actionSettings
                ),
                CatalogComponents.example(
                        "Toggle settings",
                        "Switch and tri-state checkbox setting rows.",
                        false,
                        CatalogInputSamples::toggleSettings
                ),
                CatalogComponents.example(
                        "Single-choice settings",
                        "Radio setting rows coordinated by one toggle group.",
                        false,
                        CatalogInputSamples::choiceSettings
                )
        };
    }

    /// Creates the continuous, discrete, range, orientation, and size slider examples.
    ///
    /// @return the complete slider example array
    private static CatalogExample[] sliderExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Continuous slider",
                        "A baseline continuous single-value slider.",
                        false,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_SMALL, 0.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Disabled slider",
                        "A continuous slider with interaction disabled.",
                        false,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_SMALL, 0.0, false, false, false, true
                        )
                ),
                CatalogComponents.example(
                        "Discrete slider",
                        "A single-value slider quantized to ten-unit steps.",
                        false,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_SMALL, 10.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Centered slider",
                        "A bipolar slider whose active track originates at zero.",
                        true,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_SMALL, 0.0, true, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Continuous range slider",
                        "A two-thumb slider selecting a continuous interval.",
                        false,
                        () -> CatalogInputSamples.slider(
                                true, M3SliderSize.EXTRA_SMALL, 0.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Discrete range slider",
                        "A two-thumb interval quantized to ten-unit steps.",
                        false,
                        () -> CatalogInputSamples.slider(
                                true, M3SliderSize.SMALL, 10.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Range value indicators",
                        "A range slider that displays values while adjusting either thumb.",
                        true,
                        () -> CatalogInputSamples.slider(
                                true, M3SliderSize.MEDIUM, 5.0, false, false, true, false
                        )
                ),
                CatalogComponents.example(
                        "Extra-small slider",
                        "The baseline 16-pixel track and 44-pixel handle.",
                        false,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_SMALL, 0.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Small slider",
                        "The 24-pixel expressive slider track.",
                        true,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.SMALL, 0.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Medium slider",
                        "The 40-pixel expressive slider track.",
                        true,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.MEDIUM, 0.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Large slider",
                        "The 56-pixel expressive slider track.",
                        true,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.LARGE, 0.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Extra-large slider",
                        "The 96-pixel expressive slider track.",
                        true,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_LARGE, 0.0, false, false, false, false
                        )
                ),
                CatalogComponents.example(
                        "Value indicator",
                        "A discrete slider that presents its active value.",
                        true,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_SMALL, 10.0, false, false, true, false
                        )
                ),
                CatalogComponents.example(
                        "Vertical slider",
                        "A continuous slider arranged vertically.",
                        false,
                        () -> CatalogInputSamples.slider(
                                false, M3SliderSize.EXTRA_SMALL, 0.0, false, true, false, false
                        )
                )
        };
    }

    /// Creates interactive, icon-bearing, and disabled switch examples.
    ///
    /// @return the complete switch example array
    private static CatalogExample[] switchExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Interactive states",
                        "Enabled switches in their off and on states.",
                        false,
                        () -> CatalogInputSamples.switches(false, false)
                ),
                CatalogComponents.example(
                        "Handle icons",
                        "Switches with selected and unselected handle graphics.",
                        false,
                        () -> CatalogInputSamples.switches(true, false)
                ),
                CatalogComponents.example(
                        "Disabled states",
                        "Disabled switches in their off and on states.",
                        false,
                        () -> CatalogInputSamples.switches(true, true)
                )
        };
    }

    /// Creates number-field examples across variants, commit policies, formatting, adornments, and states.
    ///
    /// @return the complete number-field example array
    private static CatalogExample[] numberFieldExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Filled snap field",
                        "A filled number field that clamps and snaps committed text.",
                        false,
                        () -> CatalogInputSamples.numberField(
                                M3TextInputVariant.FILLED,
                                M3NumberFieldCommitBehavior.SNAP,
                                false,
                                false,
                                false,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Outlined validate field",
                        "An outlined field that rejects values between configured steps.",
                        false,
                        () -> CatalogInputSamples.numberField(
                                M3TextInputVariant.OUTLINED,
                                M3NumberFieldCommitBehavior.VALIDATE,
                                false,
                                false,
                                false,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Validation error",
                        "A validate field preserving an off-step edit and its error message.",
                        false,
                        () -> CatalogInputSamples.numberField(
                                M3TextInputVariant.OUTLINED,
                                M3NumberFieldCommitBehavior.VALIDATE,
                                false,
                                false,
                                false,
                                false,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Localized percent",
                        "A compact field formatted as a locale-aware percentage.",
                        false,
                        () -> CatalogInputSamples.numberField(
                                M3TextInputVariant.FILLED,
                                M3NumberFieldCommitBehavior.SNAP,
                                true,
                                false,
                                true,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Prefix",
                        "A number field with a non-interactive leading prefix.",
                        false,
                        () -> CatalogInputSamples.numberField(
                                M3TextInputVariant.FILLED,
                                M3NumberFieldCommitBehavior.SNAP,
                                false,
                                true,
                                false,
                                false,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Disabled",
                        "A disabled number field with unavailable editing and step actions.",
                        false,
                        () -> CatalogInputSamples.numberField(
                                M3TextInputVariant.OUTLINED,
                                M3NumberFieldCommitBehavior.SNAP,
                                false,
                                false,
                                false,
                                true,
                                false
                        )
                )
        };
    }

    /// Creates filled, outlined, password, multiline, validation, graphic, and counter text-field examples.
    ///
    /// @return the complete text-field example array
    private static CatalogExample[] textFieldExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Filled and outlined overview",
                        "Labeled fields using both Material container variants.",
                        false,
                        CatalogSamples::textFields
                ),
                textFieldExample(
                        "Empty filled field", "A filled field without entered text.",
                        M3TextInputVariant.FILLED, false, false, false, false, false, false, false, false
                ),
                textFieldExample(
                        "Filled field with text", "A filled field containing an email address.",
                        M3TextInputVariant.FILLED, true, false, false, false, false, false, false, false
                ),
                textFieldExample(
                        "Empty outlined field", "An outlined field without entered text.",
                        M3TextInputVariant.OUTLINED, false, false, false, false, false, false, false, false
                ),
                textFieldExample(
                        "Outlined field with text", "An outlined field containing an email address.",
                        M3TextInputVariant.OUTLINED, true, false, false, false, false, false, false, false
                ),
                textFieldExample(
                        "Disabled filled field", "A filled field with editing disabled.",
                        M3TextInputVariant.FILLED, true, true, false, false, false, false, false, false
                ),
                textFieldExample(
                        "Disabled outlined field", "An outlined field with editing disabled.",
                        M3TextInputVariant.OUTLINED, true, true, false, false, false, false, false, false
                ),
                textFieldExample(
                        "Filled error field", "A filled field with failing validation.",
                        M3TextInputVariant.FILLED, true, false, true, false, false, false, false, false
                ),
                textFieldExample(
                        "Outlined error field", "An outlined field with failing validation.",
                        M3TextInputVariant.OUTLINED, false, false, true, false, false, false, false, false
                ),
                textFieldExample(
                        "Outlined password field", "A masked outlined single-line input.",
                        M3TextInputVariant.OUTLINED, true, false, false, false, false, false, false, true
                ),
                textFieldExample(
                        "Password error field", "A masked input with minimum-length validation.",
                        M3TextInputVariant.OUTLINED, false, false, true, false, true, false, false, true
                ),
                textFieldExample(
                        "Filled text area", "A filled multi-line input.",
                        M3TextInputVariant.FILLED, true, false, false, false, false, false, true, false
                ),
                textFieldExample(
                        "Outlined text area", "An outlined multi-line input.",
                        M3TextInputVariant.OUTLINED, true, false, false, false, false, false, true, false
                ),
                textFieldExample(
                        "Text area error", "A multi-line input with failing minimum-length validation.",
                        M3TextInputVariant.FILLED, true, false, true, false, false, false, true, false
                ),
                textFieldExample(
                        "Leading icon", "A filled field with a leading graphic slot.",
                        M3TextInputVariant.FILLED, true, false, false, true, false, false, false, false
                ),
                textFieldExample(
                        "Trailing action", "An outlined field with a trailing clear action.",
                        M3TextInputVariant.OUTLINED, true, false, false, false, true, false, false, false
                ),
                textFieldExample(
                        "Character counter", "An outlined field with a visible character limit.",
                        M3TextInputVariant.OUTLINED, true, false, false, false, false, true, false, false
                ),
                textFieldExample(
                        "Leading, clear, and counter", "A filled field combining both graphic slots and a counter.",
                        M3TextInputVariant.FILLED, true, false, false, true, true, true, false, false
                ),
                textFieldExample(
                        "Outlined area counter", "A multi-line field with an explicit character limit.",
                        M3TextInputVariant.OUTLINED, true, false, false, false, false, true, true, false
                ),
                textFieldExample(
                        "Required empty field", "An empty field with active required-style validation feedback.",
                        M3TextInputVariant.FILLED, false, false, true, true, false, false, false, false
                )
        };
    }

    /// Creates time-field and inline-picker examples.
    ///
    /// @return the complete time-picker example array
    private static CatalogExample[] timePickerExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Outlined time field",
                        "An editable outlined time field with presets.",
                        false,
                        () -> CatalogInputSamples.timeField(M3TextInputVariant.OUTLINED, false)
                ),
                CatalogComponents.example(
                        "Filled time field",
                        "An editable filled time field with presets.",
                        false,
                        () -> CatalogInputSamples.timeField(M3TextInputVariant.FILLED, false)
                ),
                CatalogComponents.example(
                        "Bounded time field",
                        "A field restricted to office hours.",
                        false,
                        () -> CatalogInputSamples.timeField(M3TextInputVariant.FILLED, true)
                ),
                CatalogComponents.example(
                        "12-hour time picker",
                        "An inline picker using the 12-hour clock.",
                        false,
                        () -> CatalogInputSamples.timePicker(false, false)
                ),
                CatalogComponents.example(
                        "24-hour time picker",
                        "An inline picker using the 24-hour clock and 15-minute steps.",
                        false,
                        () -> CatalogInputSamples.timePicker(true, false)
                ),
                CatalogComponents.example(
                        "Bounded time picker",
                        "An inline picker restricted to office hours.",
                        false,
                        () -> CatalogInputSamples.timePicker(true, true)
                )
        };
    }

    /// Creates one text-field descriptor from the shared scenario factory.
    ///
    /// @param name the example name
    /// @param description the example description
    /// @param variant the filled or outlined input variant
    /// @param text whether the input starts with text
    /// @param disabled whether the input is disabled
    /// @param error whether validation is active and failing
    /// @param leading whether a leading graphic is installed
    /// @param trailing whether a trailing action is installed
    /// @param counter whether a character counter is shown
    /// @param area whether the input is multi-line
    /// @param password whether the input masks text
    /// @return the example descriptor
    private static CatalogExample textFieldExample(
            String name,
            String description,
            M3TextInputVariant variant,
            boolean text,
            boolean disabled,
            boolean error,
            boolean leading,
            boolean trailing,
            boolean counter,
            boolean area,
            boolean password
    ) {
        return CatalogComponents.example(
                name,
                description,
                false,
                () -> CatalogInputSamples.textField(
                        variant,
                        text,
                        disabled,
                        error,
                        leading,
                        trailing,
                        counter,
                        area,
                        password
                )
        );
    }
}
