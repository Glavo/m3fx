// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3CheckBoxSettingItem;
import org.glavo.m3fx.controls.M3ChipStyle;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorArea;
import org.glavo.m3fx.controls.M3ColorChannel;
import org.glavo.m3fx.controls.M3ColorField;
import org.glavo.m3fx.controls.M3ColorPicker;
import org.glavo.m3fx.controls.M3ColorPlane;
import org.glavo.m3fx.controls.M3ColorSlider;
import org.glavo.m3fx.controls.M3ColorSpace;
import org.glavo.m3fx.controls.M3ColorSwatch;
import org.glavo.m3fx.controls.M3ColorSwatchPicker;
import org.glavo.m3fx.controls.M3ColorSwatchRounding;
import org.glavo.m3fx.controls.M3ColorSwatchSize;
import org.glavo.m3fx.controls.M3ColorWheel;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3DatePresets;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.controls.M3DateRangePickerField;
import org.glavo.m3fx.controls.M3DateRangePresets;
import org.glavo.m3fx.controls.M3FilterChip;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3FormRow;
import org.glavo.m3fx.controls.M3FormSection;
import org.glavo.m3fx.controls.M3FormValidator;
import org.glavo.m3fx.controls.M3HsbColor;
import org.glavo.m3fx.controls.M3HslColor;
import org.glavo.m3fx.controls.M3InputChip;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3NumberField;
import org.glavo.m3fx.controls.M3NumberFieldCommitBehavior;
import org.glavo.m3fx.controls.M3PasswordField;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3RadioButtonSettingItem;
import org.glavo.m3fx.controls.M3RangeSlider;
import org.glavo.m3fx.controls.M3RgbColor;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SearchViewLayout;
import org.glavo.m3fx.controls.M3SearchViewStyle;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3SettingItem;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SliderSize;
import org.glavo.m3fx.controls.M3SuggestionChip;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3SwitchSettingItem;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInput;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputValidators;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.controls.M3TimePickerField;
import org.glavo.m3fx.controls.M3TimePresets;
import org.glavo.m3fx.controls.M3ValidationSummary;
import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/// Creates focused selection, color, date, form, and text-input samples for Catalog routes.
@NotNullByDefault
final class CatalogInputSamples {
    /// The representative editable color shared by color-control samples.
    private static final M3Color INITIAL_COLOR = new M3HsbColor(268.0, 0.62, 0.76);

    /// Prevents instantiation of this factory class.
    private CatalogInputSamples() {
    }

    /// Creates one checkbox state.
    ///
    /// @param selected whether the checkbox is selected
    /// @param indeterminate whether the checkbox is indeterminate
    /// @param error whether the checkbox uses its error state
    /// @param disabled whether the checkbox is disabled
    /// @return the configured checkbox
    static Node checkBox(boolean selected, boolean indeterminate, boolean error, boolean disabled) {
        M3CheckBox checkBox = new M3CheckBox(
                disabled ? "Disabled" : error ? "Error" : indeterminate ? "Indeterminate" : selected ? "Checked" : "Unchecked"
        );
        checkBox.setAllowIndeterminate(indeterminate);
        checkBox.setSelected(selected);
        checkBox.setIndeterminate(indeterminate);
        checkBox.setError(error);
        checkBox.setDisable(disabled);
        return checkBox;
    }

    /// Creates an assist chip in one style and state.
    ///
    /// @param style the flat or elevated treatment
    /// @param graphic whether the chip has a leading graphic
    /// @param disabled whether the chip is disabled
    /// @return the configured assist chip
    static Node assistChip(M3ChipStyle style, boolean graphic, boolean disabled) {
        M3AssistChip chip = new M3AssistChip("Directions");
        chip.setChipStyle(style);
        if (graphic) {
            chip.setGraphic(CatalogSamples.icon(CatalogIcons.NAVIGATION));
        }
        chip.setDisable(disabled);
        return chip;
    }

    /// Creates a filter chip in one style and selection state.
    ///
    /// @param style the flat or elevated treatment
    /// @param selected whether the filter is selected
    /// @param graphic whether the chip has a leading graphic
    /// @param disabled whether the chip is disabled
    /// @return the configured filter chip
    static Node filterChip(M3ChipStyle style, boolean selected, boolean graphic, boolean disabled) {
        M3FilterChip chip = new M3FilterChip("Open now");
        chip.setChipStyle(style);
        chip.setSelected(selected);
        if (graphic) {
            chip.setGraphic(CatalogSamples.icon(selected ? CatalogIcons.CHECKBOX : CatalogIcons.SEARCH));
        }
        chip.setDisable(disabled);
        return chip;
    }

    /// Creates an input chip in one selection and enabled state.
    ///
    /// @param selected whether the value is selected
    /// @param graphic whether a leading avatar-style graphic is present
    /// @param trailing whether a trailing remove action is present
    /// @param disabled whether the chip is disabled
    /// @return the configured input chip
    static Node inputChip(boolean selected, boolean graphic, boolean trailing, boolean disabled) {
        M3InputChip chip = new M3InputChip("Alex Morgan");
        chip.setSelected(selected);
        if (graphic) {
            chip.setGraphic(CatalogSamples.icon(CatalogIcons.AVATAR));
        }
        if (trailing) {
            chip.setTrailingGraphic(CatalogSamples.iconButton(CatalogIcons.CLOSE, "Remove Alex Morgan"));
        }
        chip.setDisable(disabled);
        return chip;
    }

    /// Creates a suggestion chip in one style and state.
    ///
    /// @param style the flat or elevated treatment
    /// @param graphic whether the chip has a leading graphic
    /// @param disabled whether the chip is disabled
    /// @return the configured suggestion chip
    static Node suggestionChip(M3ChipStyle style, boolean graphic, boolean disabled) {
        M3SuggestionChip chip = new M3SuggestionChip("Remind me");
        chip.setChipStyle(style);
        if (graphic) {
            chip.setGraphic(CatalogSamples.icon(CatalogIcons.TIME));
        }
        chip.setDisable(disabled);
        return chip;
    }

    /// Creates a complete color picker with a preset palette.
    ///
    /// @return the configured color picker
    static Node colorPicker() {
        M3ColorPicker picker = new M3ColorPicker(INITIAL_COLOR);
        picker.setShowColorWheel(true);
        picker.getPresets().setAll(palette());
        return CatalogSamples.configureResponsiveWidth(picker, 540.0);
    }

    /// Creates a two-dimensional color area for one plane.
    ///
    /// @param plane the color plane
    /// @return the configured color area
    static Node colorArea(M3ColorPlane plane) {
        M3Color value;
        if (plane.equals(M3ColorPlane.HSB_SATURATION_BRIGHTNESS)) {
            value = INITIAL_COLOR;
        } else if (plane.equals(M3ColorPlane.HSL_SATURATION_LIGHTNESS)) {
            value = INITIAL_COLOR.toColorSpace(M3ColorSpace.HSL);
        } else {
            value = INITIAL_COLOR.toColorSpace(M3ColorSpace.RGB);
        }
        M3ColorArea area = new M3ColorArea(value);
        area.setPlane(plane);
        area.setPrefSize(280.0, 184.0);
        return area;
    }

    /// Creates a hue wheel and a vertical hue slider.
    ///
    /// @return the hue-control comparison
    static Node hueControls() {
        M3ColorWheel wheel = new M3ColorWheel(INITIAL_COLOR);
        wheel.setPrefSize(176.0, 176.0);
        M3ColorSlider slider = new M3ColorSlider(M3ColorChannel.HUE);
        slider.setValue(INITIAL_COLOR);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setPrefSize(48.0, 188.0);
        return CatalogSamples.row(wheel, slider);
    }

    /// Creates horizontal channel sliders for hue, saturation, brightness, and alpha.
    ///
    /// @return the channel-slider stack
    static Node colorSliders() {
        VBox sliders = new VBox(12.0);
        for (M3ColorChannel channel : List.of(
                M3ColorChannel.HUE,
                M3ColorChannel.SATURATION,
                M3ColorChannel.BRIGHTNESS,
                M3ColorChannel.ALPHA
        )) {
            M3ColorSlider slider = new M3ColorSlider(channel);
            slider.setValue(INITIAL_COLOR);
            CatalogSamples.configureResponsiveWidth(slider, 420.0);
            sliders.getChildren().add(slider);
        }
        return sliders;
    }

    /// Creates opaque, alpha-enabled, and disabled color fields.
    ///
    /// @return the color-field comparison
    static Node colorFields() {
        M3ColorField opaque = new M3ColorField(new M3RgbColor(0.22, 0.47, 0.82));
        M3ColorField alpha = new M3ColorField(new M3HslColor(284.0, 0.58, 0.48, 0.56));
        alpha.setIncludeAlpha(true);
        M3ColorField disabled = new M3ColorField(new M3RgbColor(0.45, 0.45, 0.45));
        disabled.setDisable(true);
        return CatalogSamples.column(opaque, alpha, disabled);
    }

    /// Creates a selectable preset swatch grid.
    ///
    /// @return the configured swatch picker
    static Node swatchPicker() {
        M3ColorSwatchPicker picker = new M3ColorSwatchPicker();
        picker.getItems().setAll(palette());
        picker.setColumnCount(8);
        picker.select(4);
        return picker;
    }

    /// Creates swatches across the size and rounding matrix.
    ///
    /// @return the swatch matrix
    static Node swatches() {
        VBox rows = new VBox(12.0);
        for (M3ColorSwatchSize size : M3ColorSwatchSize.values()) {
            M3ColorSwatch rounded = swatch(INITIAL_COLOR, size, M3ColorSwatchRounding.DEFAULT);
            M3ColorSwatch square = swatch(
                    new M3HslColor(148.0, 0.52, 0.43, 0.62),
                    size,
                    M3ColorSwatchRounding.NONE
            );
            M3ColorSwatch circle = swatch(
                    new M3HsbColor(34.0, 0.84, 0.95),
                    size,
                    M3ColorSwatchRounding.FULL
            );
            rows.getChildren().add(CatalogSamples.row(rounded, square, circle));
        }
        return rows;
    }

    /// Creates transparent, absent, and named color swatches.
    ///
    /// @return the special swatch states
    static Node specialSwatches() {
        M3ColorSwatch translucent = new M3ColorSwatch(new M3RgbColor(0.16, 0.55, 0.76, 0.38));
        M3ColorSwatch noColor = new M3ColorSwatch();
        M3ColorSwatch named = new M3ColorSwatch(new M3HslColor(350.0, 0.74, 0.50));
        named.setColorName("Brand red");
        return CatalogSamples.row(translucent, noColor, named);
    }

    /// Creates a single-date field with optional bounds and presets.
    ///
    /// @param variant the text-input container variant
    /// @param bounded whether the picker is restricted to the next two weeks
    /// @return the configured date field
    static Node dateField(M3TextInputVariant variant, boolean bounded) {
        LocalDate today = LocalDate.now();
        M3DatePickerField field = new M3DatePickerField(bounded ? today.plusDays(2) : today);
        field.setLabelText(bounded ? "Booking date" : "Event date");
        field.setSupportingText(bounded ? "Limited to the next two weeks" : "Editable date with popup calendar");
        field.setVariant(variant);
        if (bounded) {
            field.getPicker().setMinDate(today);
            field.getPicker().setMaxDate(today.plusDays(14));
        }
        field.getPresets().setAll(M3DatePresets.common(today));
        return CatalogSamples.configureResponsiveWidth(field, 340.0);
    }

    /// Creates a date-range field with presets and bounds.
    ///
    /// @return the configured date-range field
    static Node dateRangeField() {
        LocalDate today = LocalDate.now();
        M3DateRangePickerField field = new M3DateRangePickerField(today.plusDays(2), today.plusDays(8));
        field.setStartLabelText("Start date");
        field.setEndLabelText("End date");
        field.setStartSupportingText("Editable range start");
        field.setEndSupportingText("Editable range end");
        field.getPicker().setMinDate(today.minusDays(7));
        field.getPicker().setMaxDate(today.plusDays(30));
        field.getPresets().setAll(M3DateRangePresets.common(today, field.getPicker().getFirstDayOfWeek()));
        return CatalogSamples.configureResponsiveWidth(field, 680.0);
    }

    /// Creates an inline date picker with one selected date.
    ///
    /// @return the configured date picker
    static Node selectedDatePicker() {
        M3DatePicker picker = new M3DatePicker(LocalDate.now());
        return CatalogSamples.configureResponsiveWidth(picker, 420.0);
    }

    /// Creates a bounded inline date picker.
    ///
    /// @return the bounded date picker
    static Node boundedDatePicker() {
        LocalDate today = LocalDate.now();
        M3DatePicker picker = new M3DatePicker(today.plusDays(4));
        picker.setMinDate(today.minusDays(3));
        picker.setMaxDate(today.plusDays(18));
        return CatalogSamples.configureResponsiveWidth(picker, 420.0);
    }

    /// Creates an inline date-range picker.
    ///
    /// @return the configured date-range picker
    static Node dateRangePicker() {
        LocalDate today = LocalDate.now();
        M3DateRangePicker picker = new M3DateRangePicker(today.plusDays(2), today.plusDays(8));
        picker.setMinDate(today.minusDays(7));
        picker.setMaxDate(today.plusDays(30));
        return CatalogSamples.configureResponsiveWidth(picker, 420.0);
    }

    /// Creates a month-only date picker without adjacent-month days.
    ///
    /// @return the configured month-only picker
    static Node monthOnlyDatePicker() {
        M3DatePicker picker = new M3DatePicker();
        picker.setDisplayedMonth(YearMonth.now().plusMonths(1));
        picker.setShowAdjacentMonthDays(false);
        return CatalogSamples.configureResponsiveWidth(picker, 420.0);
    }

    /// Creates a structured form with optional multiple sections and validation feedback.
    ///
    /// @param multipleSections whether account and preference sections are both shown
    /// @param validation whether validators and a validation summary are installed
    /// @return the configured form pane
    static Node form(boolean multipleSections, boolean validation) {
        M3TextField name = new M3TextField("");
        name.setVariant(M3TextInputVariant.OUTLINED);
        M3TextInputLayout nameLayout = new M3TextInputLayout(name);
        nameLayout.setLabelText("Display name");
        nameLayout.setSupportingText("Visible to collaborators");

        M3TextField email = new M3TextField("support");
        email.setVariant(M3TextInputVariant.OUTLINED);
        M3TextInputLayout emailLayout = new M3TextInputLayout(email);
        emailLayout.setLabelText("Email");
        emailLayout.setSupportingText("Used for notifications");

        M3FormSection account = new M3FormSection("Account", "Aligned identity fields.");
        account.getContent().addAll(
                new M3FormRow("Display name", "Primary profile label", nameLayout),
                new M3FormRow("Email", "Project notifications", emailLayout)
        );

        M3FormPane pane = new M3FormPane();
        if (validation) {
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));
            emailLayout.setValidator(M3TextInputValidators.pattern(
                    Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                    "Enter a valid email address"
            ));
            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            validator.validate();
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setTitleText("Review form fields");
            pane.getItems().add(summary);
        }
        pane.getItems().add(account);

        if (multipleSections) {
            M3Switch notifications = new M3Switch();
            notifications.setSelected(true);
            M3CheckBox beta = new M3CheckBox();
            beta.setAllowIndeterminate(true);
            beta.setIndeterminate(true);
            M3FormSection preferences = new M3FormSection("Preferences", "Aligned boolean controls.");
            preferences.getContent().addAll(
                    new M3FormRow("Notifications", "Receive product updates", notifications),
                    new M3FormRow("Beta channel", "Follow the preview channel", beta)
            );
            pane.getItems().add(preferences);
        }
        pane.setContentPadding(18.0);
        return CatalogSamples.configureResponsiveWidth(pane, 760.0);
    }

    /// Creates interactive or disabled radio-button states.
    ///
    /// @param disabled whether both options are disabled
    /// @return the radio-button state comparison
    static Node radioButtons(boolean disabled) {
        ToggleGroup group = new ToggleGroup();
        M3RadioButton first = new M3RadioButton(disabled ? "Disabled selected" : "Radio A");
        M3RadioButton second = new M3RadioButton(disabled ? "Disabled unchecked" : "Radio B");
        first.setToggleGroup(group);
        second.setToggleGroup(group);
        first.setSelected(true);
        first.setDisable(disabled);
        second.setDisable(disabled);
        return CatalogSamples.column(first, second);
    }

    /// Creates an empty or populated search bar.
    ///
    /// @param populated whether the query is prefilled
    /// @return the configured search bar
    static Node searchBar(boolean populated) {
        M3SearchBar bar = new M3SearchBar("Search M3FX");
        if (populated) {
            bar.setText("Buttons");
        }
        bar.getTrailingActions().add(CatalogSamples.iconButton(CatalogIcons.CLOSE, "Clear"));
        return CatalogSamples.configureResponsiveWidth(bar, 460.0);
    }

    /// Creates a contained or divided search view in docked or full-screen form.
    ///
    /// @param layout the docked or full-screen view layout
    /// @param style the contained or divided result treatment
    /// @return the configured search view
    static Node searchView(M3SearchViewLayout layout, M3SearchViewStyle style) {
        M3SearchView view = new M3SearchView("Search components");
        view.setViewLayout(layout);
        view.setViewStyle(style);
        view.getResults().addAll(
                searchResult("Buttons", "Filled, tonal, outlined, text, and elevated variants"),
                searchResult("Navigation", "Bars, rails, drawers, and adaptive destinations"),
                searchResult("Typography", "Semantic Material type roles")
        );
        return CatalogSamples.configureResponsiveWidth(view, 540.0);
    }

    /// Creates a single- or multiple-selection segmented group with optional graphics.
    ///
    /// @param selectionMode the group selection mode
    /// @param graphics whether each segment has a leading graphic
    /// @return the configured segmented group
    static Node segmentedButtons(M3SelectionMode selectionMode, boolean graphics) {
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
        group.setSelectionMode(selectionMode);
        M3SegmentedButton day = segment("Day", graphics ? CatalogIcons.CALENDAR : "");
        M3SegmentedButton week = segment("Week", graphics ? CatalogIcons.TIME : "");
        M3SegmentedButton month = segment("Month", graphics ? CatalogIcons.NAVIGATION : "");
        group.getItems().addAll(day, week, month);
        group.selectIndex(1);
        if (selectionMode == M3SelectionMode.MULTIPLE) {
            group.selectIndex(2);
        }
        return group;
    }

    /// Creates ordinary action settings.
    ///
    /// @return the action-settings list
    static Node actionSettings() {
        M3SettingItem account = new M3SettingItem("Account");
        account.setSupportingText("Profile, security, and linked devices");
        account.setLeading(CatalogSamples.icon(CatalogIcons.AVATAR));
        account.setTrailing(CatalogSamples.icon(CatalogIcons.ARROW_FORWARD));
        M3SettingItem storage = new M3SettingItem("Storage");
        storage.setSupportingText("Local files and cloud backups");
        storage.setLeading(CatalogSamples.icon(CatalogIcons.BOTTOM_SHEET));
        storage.setTrailing(CatalogSamples.icon(CatalogIcons.ARROW_FORWARD));
        return settingsList(M3ListStyle.STANDARD, account, storage);
    }

    /// Creates switch and checkbox setting rows.
    ///
    /// @return the toggle-settings list
    static Node toggleSettings() {
        M3SwitchSettingItem updates = new M3SwitchSettingItem("Automatic updates");
        updates.setSupportingText("Install updates while idle");
        updates.setSelected(true);
        M3CheckBoxSettingItem mobile = new M3CheckBoxSettingItem("Use mobile data");
        mobile.setSupportingText("Download away from Wi-Fi");
        mobile.setAllowIndeterminate(true);
        mobile.setIndeterminate(true);
        return settingsList(M3ListStyle.SEGMENTED, updates, mobile);
    }

    /// Creates mutually exclusive radio setting rows.
    ///
    /// @return the choice-settings list
    static Node choiceSettings() {
        ToggleGroup group = new ToggleGroup();
        M3RadioButtonSettingItem system = radioSetting("Use system theme", group);
        M3RadioButtonSettingItem light = radioSetting("Light", group);
        M3RadioButtonSettingItem dark = radioSetting("Dark", group);
        system.setSelected(true);
        return settingsList(M3ListStyle.SEGMENTED, system, light, dark);
    }

    /// Creates a slider or range-slider scenario.
    ///
    /// @param range whether to create a two-thumb range slider
    /// @param size the Material slider size
    /// @param stepSize the discrete step size, or zero for continuous adjustment
    /// @param centered whether a single slider uses a centered track
    /// @param vertical whether the slider is vertical
    /// @param indicator whether value indicators are enabled
    /// @param disabled whether the slider is disabled
    /// @return the configured slider
    static Node slider(
            boolean range,
            M3SliderSize size,
            double stepSize,
            boolean centered,
            boolean vertical,
            boolean indicator,
            boolean disabled
    ) {
        if (range) {
            M3RangeSlider slider = new M3RangeSlider(0.0, 100.0, 25.0, 70.0);
            slider.setSize(size);
            slider.setStepSize(stepSize);
            slider.setShowValueIndicator(indicator);
            slider.setDisable(disabled);
            if (vertical) {
                slider.setOrientation(Orientation.VERTICAL);
                slider.setPrefSize(64.0, 220.0);
            } else {
                CatalogSamples.configureResponsiveWidth(slider, 360.0);
            }
            return slider;
        }

        M3Slider slider = centered
                ? new M3Slider(-100.0, 100.0, 40.0)
                : new M3Slider(0.0, 100.0, 55.0);
        slider.setSize(size);
        slider.setStepSize(stepSize);
        slider.setCentered(centered);
        slider.setShowValueIndicator(indicator);
        slider.setDisable(disabled);
        if (vertical) {
            slider.setOrientation(Orientation.VERTICAL);
            slider.setPrefSize(64.0, 220.0);
        } else {
            CatalogSamples.configureResponsiveWidth(slider, 360.0);
        }
        return slider;
    }

    /// Creates interactive, icon-bearing, or disabled switches.
    ///
    /// @param icons whether selected and unselected handle graphics are shown
    /// @param disabled whether both switches are disabled
    /// @return the switch-state comparison
    static Node switches(boolean icons, boolean disabled) {
        M3Switch off = new M3Switch(disabled ? "Disabled off" : "Off");
        M3Switch on = new M3Switch(disabled ? "Disabled on" : "On");
        on.setSelected(true);
        if (icons) {
            off.setUnselectedIcon(CatalogSamples.icon(CatalogIcons.CLOSE));
            on.setSelectedIcon(CatalogSamples.icon(CatalogIcons.CHECKBOX));
        }
        off.setDisable(disabled);
        on.setDisable(disabled);
        return CatalogSamples.column(off, on);
    }

    /// Creates a number-field scenario.
    ///
    /// @param variant       the filled or outlined input variant
    /// @param commitBehavior the snap or validate commit policy
    /// @param localized     whether percent formatting is used
    /// @param prefix        whether a prefix node is installed
    /// @param hideStepper   whether increment and decrement buttons are hidden
    /// @param disabled      whether the field is disabled
    /// @param error         whether the initial text fails validation
    /// @return the configured number field
    static Node numberField(
            M3TextInputVariant variant,
            M3NumberFieldCommitBehavior commitBehavior,
            boolean localized,
            boolean prefix,
            boolean hideStepper,
            boolean disabled,
            boolean error
    ) {
        M3NumberField field = new M3NumberField(localized ? 0.375 : 8.0);
        field.setVariant(variant);
        field.setCommitBehavior(commitBehavior);
        field.setLabelText(localized ? "Completion" : "Amount");
        field.setSupportingText(commitBehavior == M3NumberFieldCommitBehavior.VALIDATE
                ? "Valid values begin at 2 and advance by 3"
                : "Range and step actions share one value scale");
        field.setMin(localized ? 0.0 : 2.0);
        field.setMax(localized ? 1.0 : 20.0);
        field.setStep(localized ? 0.05 : 3.0);
        field.setFormatter(localized
                ? NumberFormat.getPercentInstance(Locale.US)
                : NumberFormat.getNumberInstance(Locale.US));
        if (prefix) {
            field.setPrefix(new Label("W"));
        }
        field.setHideStepper(hideStepper);
        field.setDisable(disabled);
        if (error) {
            field.setText("9");
            field.commitEditorText();
        }
        return CatalogSamples.configureResponsiveWidth(field, 360.0);
    }

    /// Creates a text field, password field, or text area scenario.
    ///
    /// @param variant the filled or outlined input variant
    /// @param text whether the control begins with text
    /// @param disabled whether the control is disabled
    /// @param error whether validation is active and failing
    /// @param leading whether a leading graphic is installed
    /// @param trailing whether a trailing action is installed
    /// @param counter whether a character counter is visible
    /// @param area whether the input is multi-line
    /// @param password whether the input masks its text
    /// @return the configured text-input layout
    static Node textField(
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
        TextInputControl input;
        if (area) {
            M3TextArea textArea = new M3TextArea(text ? "Longer notes across multiple lines." : "");
            textArea.setPrefRowCount(3);
            input = textArea;
        } else if (password) {
            input = new M3PasswordField(text ? "password" : "");
        } else {
            input = new M3TextField(text ? "support@example.com" : "");
        }
        ((M3TextInput) input).setVariant(variant);
        input.setPromptText(area ? "Notes" : password ? "Password" : "Email");
        input.setDisable(disabled);

        M3TextInputLayout layout = new M3TextInputLayout(input);
        layout.setLabelText(area ? "Description" : password ? "Password" : "Email");
        layout.setSupportingText(error ? "Validation feedback" : "Supporting text");
        if (leading) {
            layout.setLeading(CatalogSamples.icon(CatalogIcons.AVATAR));
        }
        if (trailing) {
            layout.setTrailing(CatalogSamples.iconButton(CatalogIcons.CLOSE, "Clear"));
        }
        if (counter) {
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(area ? 120 : 32);
        }
        if (error) {
            layout.setValidator(M3TextInputValidators.minLength(24, "Use at least 24 characters"));
            layout.validate();
        }
        return CatalogSamples.configureResponsiveWidth(layout, area ? 420.0 : 360.0);
    }

    /// Creates a 12-hour or 24-hour inline time picker with optional bounds.
    ///
    /// @param use24HourClock whether the picker uses a 24-hour clock
    /// @param bounded whether office-hour bounds are applied
    /// @return the configured time picker
    static Node timePicker(boolean use24HourClock, boolean bounded) {
        M3TimePicker picker = new M3TimePicker(bounded ? LocalTime.of(9, 30) : LocalTime.of(14, 45));
        picker.setUse24HourClock(use24HourClock);
        picker.setMinuteStep(use24HourClock ? 15 : 5);
        if (bounded) {
            picker.setMinTime(LocalTime.of(9, 0));
            picker.setMaxTime(LocalTime.of(17, 30));
        }
        return picker;
    }

    /// Creates a filled or outlined time field with presets and optional bounds.
    ///
    /// @param variant the text-input container variant
    /// @param bounded whether office-hour bounds are applied
    /// @return the configured time field
    static Node timeField(M3TextInputVariant variant, boolean bounded) {
        LocalTime time = bounded ? LocalTime.of(9, 30) : LocalTime.of(10, 30);
        M3TimePickerField field = new M3TimePickerField(time);
        field.setLabelText(bounded ? "Office hours" : "Start time");
        field.setSupportingText(bounded ? "Limited to 09:00 through 17:30" : "Editable time with popup picker");
        field.setVariant(variant);
        field.getPicker().setUse24HourClock(true);
        field.getPicker().setMinuteStep(bounded ? 30 : 15);
        if (bounded) {
            field.getPicker().setMinTime(LocalTime.of(9, 0));
            field.getPicker().setMaxTime(LocalTime.of(17, 30));
        }
        field.getPresets().setAll(M3TimePresets.common(time));
        return CatalogSamples.configureResponsiveWidth(field, 340.0);
    }

    /// Creates one color swatch.
    ///
    /// @param color the swatch color
    /// @param size the swatch size
    /// @param rounding the swatch rounding
    /// @return the configured swatch
    private static M3ColorSwatch swatch(
            M3Color color,
            M3ColorSwatchSize size,
            M3ColorSwatchRounding rounding
    ) {
        M3ColorSwatch swatch = new M3ColorSwatch(color);
        swatch.setSize(size);
        swatch.setRounding(rounding);
        return swatch;
    }

    /// Creates the preset palette used by color examples.
    ///
    /// @return a fresh preset array
    private static M3Color[] palette() {
        return new M3Color[]{
                new M3HsbColor(0.0, 0.74, 0.83),
                new M3HsbColor(28.0, 0.82, 0.94),
                new M3HsbColor(52.0, 0.72, 0.96),
                new M3HsbColor(126.0, 0.59, 0.67),
                new M3HsbColor(174.0, 0.67, 0.68),
                new M3HsbColor(216.0, 0.68, 0.86),
                new M3HsbColor(267.0, 0.56, 0.76),
                new M3HsbColor(326.0, 0.60, 0.82)
        };
    }

    /// Creates one search-result row.
    ///
    /// @param title the result headline
    /// @param supportingText the result supporting text
    /// @return the configured result row
    private static org.glavo.m3fx.controls.M3ListItem searchResult(String title, String supportingText) {
        org.glavo.m3fx.controls.M3ListItem item = new org.glavo.m3fx.controls.M3ListItem(title);
        item.setSupportingText(supportingText);
        item.setLeading(CatalogSamples.icon(CatalogIcons.SEARCH));
        return item;
    }

    /// Creates one segmented button with an optional leading graphic.
    ///
    /// @param text the segment label
    /// @param iconPath the icon path, or an empty string
    /// @return the configured segment
    private static M3SegmentedButton segment(String text, String iconPath) {
        return iconPath.isEmpty()
                ? new M3SegmentedButton(text)
                : new M3SegmentedButton(text, CatalogSamples.icon(iconPath));
    }

    /// Creates a settings list with selection owned by its rows.
    ///
    /// @param style the list containment style
    /// @param items the setting rows
    /// @return the configured settings list
    private static M3ListPane settingsList(M3ListStyle style, Node... items) {
        M3ListPane list = new M3ListPane();
        list.setListStyle(style);
        list.setSelectionMode(M3SelectionMode.NONE);
        list.getItems().addAll(items);
        return CatalogSamples.configureResponsiveWidth(list, 620.0);
    }

    /// Creates one radio setting row.
    ///
    /// @param text the setting label
    /// @param group the coordinating toggle group
    /// @return the configured row
    private static M3RadioButtonSettingItem radioSetting(String text, ToggleGroup group) {
        M3RadioButtonSettingItem item = new M3RadioButtonSettingItem(text);
        item.setSupportingText("Theme preference");
        item.setToggleGroup(group);
        return item;
    }

}
