// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

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
                        CatalogComponents.example(
                                "Checkbox states",
                                "Selected, indeterminate, and unselected states.",
                                false,
                                CatalogSamples::checkboxes
                        )
                ),
                CatalogComponents.component(
                        "Chips",
                        "Chips represent compact actions, filters, input values, and suggestions.",
                        CatalogIcons.CHIP,
                        "chips",
                        "M3Chip",
                        CatalogComponents.example(
                                "Assist chips",
                                "Compact actions with optional leading icons.",
                                false,
                                CatalogSamples::assistChips
                        ),
                        CatalogComponents.example(
                                "Filter chips",
                                "Multi-select filtering choices.",
                                false,
                                CatalogSamples::filterChips
                        ),
                        CatalogComponents.example(
                                "Input chips",
                                "Compact representations of entered values.",
                                false,
                                CatalogSamples::inputChips
                        ),
                        CatalogComponents.example(
                                "Suggestion chips",
                                "Stateless suggested responses.",
                                false,
                                CatalogSamples::suggestionChips
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Color pickers",
                        "Color pickers combine a color plane, channels, fields, and optional preset swatches.",
                        CatalogIcons.PALETTE,
                        "https://react-spectrum.adobe.com/ColorArea",
                        "M3ColorPicker",
                        CatalogComponents.example(
                                "Complete color picker",
                                "An editable color with a wheel and preset palette.",
                                false,
                                CatalogSamples::colorPicker
                        )
                ),
                CatalogComponents.component(
                        "Date pickers",
                        "Date pickers let users select a date from a calendar-oriented surface.",
                        CatalogIcons.CALENDAR,
                        "date-pickers",
                        "M3DatePicker",
                        CatalogComponents.example(
                                "Date picker",
                                "An inline single-date picker.",
                                false,
                                CatalogSamples::datePicker
                        ),
                        CatalogComponents.example(
                                "Date range picker",
                                "An inline picker with an inclusive selected range.",
                                false,
                                CatalogSamples::dateRangePicker
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Forms",
                        "Forms align related labels, supporting text, inputs, and settings into structured sections.",
                        CatalogIcons.FORM,
                        "https://m3.material.io/components/text-fields/overview",
                        "M3FormPane",
                        CatalogComponents.example(
                                "Structured form",
                                "A responsive form section containing text and boolean inputs.",
                                false,
                                CatalogSamples::forms
                        )
                ),
                CatalogComponents.component(
                        "Radio buttons",
                        "Radio buttons select exactly one option from a mutually exclusive group.",
                        CatalogIcons.RADIO,
                        "radio-button",
                        "M3RadioButton",
                        CatalogComponents.example(
                                "Selection group",
                                "A mutually exclusive radio-button group.",
                                false,
                                CatalogSamples::radioButtons
                        )
                ),
                CatalogComponents.component(
                        "Search",
                        "Search combines a query entry bar with optional suggestion and result content.",
                        CatalogIcons.SEARCH,
                        "search",
                        "M3SearchBar",
                        CatalogComponents.example(
                                "Search bar",
                                "A populated search input with actions.",
                                false,
                                CatalogSamples::searchBar
                        ),
                        CatalogComponents.example(
                                "Search view",
                                "An active docked search view with contained and divided treatments.",
                                true,
                                CatalogSamples::searchView
                        )
                ),
                CatalogComponents.component(
                        "Segmented buttons",
                        "Segmented buttons select one or more options from a connected set.",
                        CatalogIcons.SEGMENTED_BUTTON,
                        "segmented-buttons",
                        "M3SegmentedButtonGroup",
                        CatalogComponents.example(
                                "Single selection",
                                "A connected single-select segmented group.",
                                false,
                                CatalogSamples::segmentedButtons
                        )
                ),
                CatalogComponents.extensionComponent(
                        "Settings",
                        "Setting rows combine list semantics with actions, switches, checkboxes, and radio choices.",
                        CatalogIcons.SETTINGS,
                        "https://m3.material.io/components/lists/overview",
                        "M3SettingItem",
                        CatalogComponents.example(
                                "Settings list",
                                "Action and selection settings in a segmented list.",
                                false,
                                CatalogSamples::settings
                        )
                ),
                CatalogComponents.component(
                        "Sliders",
                        "Sliders choose a single value or range along a continuous or discrete track.",
                        CatalogIcons.SLIDER,
                        "sliders",
                        "M3Slider",
                        CatalogComponents.example(
                                "Value and range",
                                "Single-value and range sliders.",
                                true,
                                CatalogSamples::sliders
                        )
                ),
                CatalogComponents.component(
                        "Switches",
                        "Switches immediately toggle a single binary setting.",
                        CatalogIcons.SWITCH,
                        "switch",
                        "M3Switch",
                        CatalogComponents.example(
                                "Switch states",
                                "On and off settings.",
                                false,
                                CatalogSamples::switches
                        )
                ),
                CatalogComponents.component(
                        "Text fields",
                        "Text fields accept and validate user-entered text in filled or outlined containers.",
                        CatalogIcons.TEXT_FIELD,
                        "text-fields",
                        "M3TextField",
                        CatalogComponents.example(
                                "Filled and outlined",
                                "Labelled text fields with supporting text.",
                                false,
                                CatalogSamples::textFields
                        )
                ),
                CatalogComponents.component(
                        "Time pickers",
                        "Time pickers let users select hours and minutes with clock-oriented controls.",
                        CatalogIcons.TIME,
                        "time-pickers",
                        "M3TimePicker",
                        CatalogComponents.example(
                                "Time picker",
                                "An inline time picker.",
                                false,
                                CatalogSamples::timePicker
                        )
                )
        );
    }
}
