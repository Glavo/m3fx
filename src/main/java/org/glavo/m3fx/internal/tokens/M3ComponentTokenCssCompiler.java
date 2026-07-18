// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.glavo.m3fx.tokens.M3ComponentTokens.*;
import org.glavo.m3fx.tokens.M3Profile;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;
import java.util.Objects;

/// Compiles component token data into the JavaFX CSS consumed by M3FX skins.
@NotNullByDefault
public final class M3ComponentTokenCssCompiler {
    /// Prevents utility class instantiation.
    private M3ComponentTokenCssCompiler() {
    }

    /// Converts component tokens into inline JavaFX CSS declarations.
    ///
    /// @param tokens the component token set to compile
    /// @return inline JavaFX CSS declarations for all component tokens
    public static String styleDeclarations(M3ComponentTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        append(builder, "button-filled", tokens.filledButton());
        append(builder, "button-tonal", tokens.tonalButton());
        append(builder, "button-outlined", tokens.outlinedButton());
        append(builder, "button-text", tokens.textButton());
        append(builder, "button-elevated", tokens.elevatedButton());
        append(builder, tokens.buttonSizing());
        append(builder, tokens.iconButton());
        append(builder, tokens.floatingActionButton());
        append(builder, tokens.icon());
        append(builder, tokens.buttonGroup());
        append(builder, tokens.splitButton());
        append(builder, "segmented-button", tokens.segmentedButton());
        append(builder, tokens.tab());
        append(builder, tokens.field());
        append(builder, tokens.textArea());
        append(builder, tokens.form());
        append(builder, tokens.validationSummary());
        append(builder, tokens.menu());
        append(builder, tokens.search());
        append(builder, tokens.pickerField());
        append(builder, tokens.datePicker());
        append(builder, tokens.timePicker());
        append(builder, tokens.sheet());
        append(builder, tokens.scrim());
        append(builder, tokens.selection());
        append(builder, tokens.slider());
        append(builder, tokens.chip());
        append(builder, tokens.progress());
        append(builder, tokens.loadingIndicator());
        append(builder, tokens.surface());
        append(builder, tokens.carousel());
        append(builder, tokens.card());
        append(builder, tokens.dialog());
        append(builder, tokens.snackbar());
        append(builder, tokens.banner());
        append(builder, tokens.tooltip());
        append(builder, tokens.divider());
        append(builder, tokens.badge());
        append(builder, tokens.avatar());
        append(builder, tokens.topAppBar());
        append(builder, tokens.bottomAppBar());
        append(builder, tokens.toolbar());
        append(builder, tokens.navigationBar());
        append(builder, tokens.navigationRail());
        append(builder, tokens.navigationDrawer());
        append(builder, tokens.listItem());
        return builder.toString().trim();
    }

    /// Converts component tokens into JavaFX CSS rules including all profile-dependent state rules.
    ///
    /// This overload is intended for isolated component-token inspection. Complete themes should use
    /// [controlStyleRules(M3ComponentTokens, M3Profile)] so Baseline themes omit Expressive-only shape morphs.
    ///
    /// @param tokens the component token set to compile
    /// @return JavaFX CSS rules that apply these component tokens to M3FX controls
    public static String controlStyleRules(M3ComponentTokens tokens) {
        return controlStyleRules(tokens, M3Profile.EXPRESSIVE_2025);
    }

    /// Converts component tokens into JavaFX CSS rules for one Material profile.
    ///
    /// @param tokens  the component token set to compile
    /// @param profile the profile that controls availability of profile-specific state rules
    /// @return JavaFX CSS rules that apply these component tokens to M3FX controls
    public static String controlStyleRules(M3ComponentTokens tokens, M3Profile profile) {
        Objects.requireNonNull(tokens, "tokens");
        boolean expressive = Objects.requireNonNull(profile, "profile") == M3Profile.EXPRESSIVE_2025;
        StringBuilder builder = new StringBuilder();
        appendButtonRule(builder, ".m3-filled-button", tokens.filledButton());
        appendButtonRule(builder, ".m3-tonal-button", tokens.tonalButton());
        appendButtonRule(builder, ".m3-outlined-button", tokens.outlinedButton());
        appendButtonRule(builder, ".m3-text-button", tokens.textButton());
        appendButtonRule(builder, ".m3-elevated-button", tokens.elevatedButton());
        appendButtonSizeRules(builder, tokens.buttonSizing(), expressive);
        appendIconButtonRules(builder, tokens.iconButton(), expressive);
        appendIconRules(builder, tokens.icon());
        appendConnectedButtonRules(builder, tokens.buttonGroup(), expressive);
        appendSplitButtonRules(builder, tokens.splitButton());
        appendGroupSpacingRule(
                builder,
                ".m3-segmented-button-group",
                "-m3-segmented-button-group-spacing",
                tokens.buttonGroup().segmentedGroupSpacing()
        );
        appendGroupSpacingRule(
                builder,
                ".m3-icon-toggle-button-group",
                "-m3-icon-toggle-button-group-spacing",
                tokens.buttonGroup().iconToggleGroupSpacing()
        );
        appendGroupSpacingRule(
                builder,
                ".m3-fab-menu",
                "-m3-fab-menu-action-spacing",
                tokens.floatingActionButton().menuActionSpacing()
        );
        appendGroupSpacingRule(
                builder,
                ".m3-fab-menu",
                "-m3-fab-menu-close-spacing",
                tokens.floatingActionButton().menuCloseSpacing()
        );

        appendFabRule(
                builder,
                ".m3-small-fab",
                tokens.floatingActionButton().small()
        );
        appendFabRule(
                builder,
                ".m3-regular-fab",
                tokens.floatingActionButton().regular()
        );
        appendFabRule(
                builder,
                ".m3-medium-fab",
                tokens.floatingActionButton().medium()
        );
        appendFabRule(
                builder,
                ".m3-large-fab",
                tokens.floatingActionButton().large()
        );
        appendFabRule(
                builder,
                ".m3-fab.m3-fab-menu-action",
                tokens.floatingActionButton().menuItem()
        );
        appendFabRule(
                builder,
                ".m3-fab.m3-fab-menu-close",
                tokens.floatingActionButton().menuCloseButton()
        );
        appendButtonRule(builder, ".m3-segmented-button", tokens.segmentedButton());
        appendSegmentedButtonPositionRules(builder, tokens.segmentedButton());
        appendTabRule(builder, tokens.tab());
        appendTabIndicatorRule(builder, tokens.tab());
        appendFieldRule(builder, tokens.field());
        appendTextAreaRule(builder, tokens.textArea());
        appendFilledFieldRule(builder, tokens.field());
        appendOutlinedFieldRule(builder, tokens.field());
        appendFilledTextAreaRule(builder, tokens.textArea());
        appendOutlinedTextAreaRule(builder, tokens.textArea());
        appendFormPaneRule(builder, tokens.form());
        appendFormSectionRule(builder, tokens.form());
        appendFormSectionHeaderRule(builder, tokens.form());
        appendFormRowRule(builder, tokens.form());
        appendFormRowTextColumnRule(builder, tokens.form());
        appendValidationSummaryRule(builder, tokens.validationSummary());
        appendValidationSummaryItemsRule(builder, tokens.validationSummary());
        appendValidationSummaryItemRule(builder, tokens.validationSummary());
        appendMenuRule(builder, tokens.menu());
        appendMenuContainerRule(builder, tokens.menu());
        appendMenuItemRule(builder, tokens.menu());
        appendMenuEdgeItemRules(builder, tokens.menu());
        appendSelectedMenuItemRule(builder, tokens.menu());
        appendSearchBarRule(builder, tokens.search());
        appendSearchBarContentRule(builder, tokens.search());
        appendSearchBarTrailingRule(builder, tokens.search());
        appendSearchViewRule(builder, tokens.search());
        appendSearchViewContentRule(builder, tokens.search());
        appendSearchViewResultsRule(builder, tokens.search());
        appendPickerFieldRule(builder, tokens.pickerField());
        appendPickerFieldOpenButtonRule(builder, tokens.pickerField());
        appendPickerFieldPresetContentRule(builder, tokens.pickerField());
        appendPickerFieldPresetListRule(builder, tokens.pickerField());
        appendPickerFieldPresetButtonRule(builder, tokens.pickerField());
        appendDatePickerRule(builder, tokens.datePicker());
        appendDatePickerHeaderRule(builder, tokens.datePicker());
        appendDatePickerNavigationButtonRule(builder, tokens.datePicker());
        appendDatePickerWeekdayRowRule(builder, tokens.datePicker());
        appendDatePickerGridRule(builder, tokens.datePicker());
        appendDatePickerCellRule(builder, tokens.datePicker());
        appendDatePickerCellShapeRules(builder, tokens.datePicker());
        appendTimePickerControlRule(builder, tokens.timePicker());
        appendTimePickerContainerRule(builder, tokens.timePicker());
        appendTimePickerDisplayRule(builder, tokens.timePicker());
        appendTimePickerDisplayCellRule(builder, tokens.timePicker());
        appendTimePicker24HourDisplayCellRule(builder, tokens.timePicker());
        appendTimePickerPeriodRule(builder, tokens.timePicker());
        appendTimePickerDialRule(builder, tokens.timePicker());
        appendTimePickerDialTrackRule(builder, tokens.timePicker());
        appendTimePickerInputFieldRule(builder, tokens.timePicker());

        appendSideSheetRule(builder, tokens.sheet());
        appendBottomSheetRule(builder, tokens.sheet());
        appendSheetHeaderRule(builder, tokens.sheet());
        appendSheetContentRule(builder, tokens.sheet());
        appendBottomSheetDragHandleRule(builder, tokens.sheet());
        appendScrimRule(builder, tokens.scrim());
        appendSelectionRule(builder, tokens.selection());
        appendSwitchRule(builder, tokens.selection());
        appendSwitchBoxRule(builder, tokens.selection());
        appendSliderRules(builder, tokens.slider());
        appendChipRule(builder, tokens.chip());
        appendChipGroupRule(builder, tokens.chip());
        appendProgressBarRule(builder, tokens.progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .track", tokens.progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .bar", tokens.progress());
        appendProgressIndicatorRule(builder, tokens.progress());
        appendLoadingIndicatorRule(builder, tokens.loadingIndicator());
        appendSurfaceRule(builder, tokens.surface());
        appendCarouselTrackRule(builder, tokens.carousel());
        appendCardRule(builder, tokens.card());
        appendDialogRule(builder, tokens.dialog());
        appendSnackbarRule(builder, tokens.snackbar());
        appendBannerRule(builder, tokens.banner());
        appendTooltipRule(builder, tokens.tooltip());
        appendRichTooltipRule(builder, tokens.tooltip());
        appendRichTooltipActionsRule(builder, tokens.tooltip());
        appendRichTooltipActionButtonRule(builder, tokens.tooltip());
        appendDividerRule(builder, tokens.divider());
        appendBadgeRule(builder, tokens.badge());
        appendAvatarRule(builder, tokens.avatar());
        appendTopAppBarRule(builder, tokens.topAppBar());
        appendBottomAppBarRule(builder, tokens.bottomAppBar());
        appendToolbarRule(builder, tokens.toolbar());
        appendNavigationBarRule(builder, tokens.navigationBar());
        appendNavigationItemRule(builder, tokens.navigationBar());
        appendNavigationIndicatorRule(builder, tokens.navigationBar());
        appendNavigationRailRule(builder, tokens.navigationRail());
        appendNavigationRailItemRule(builder, tokens.navigationRail());
        appendNavigationRailIndicatorRule(builder, tokens.navigationRail());
        appendListItemRule(builder, tokens.listItem());
        appendSegmentedListRule(builder, tokens.listItem(), expressive);
        appendListSectionHeaderRule(builder, tokens.listItem());
        appendNavigationDrawerRule(builder, tokens.navigationDrawer());
        appendNavigationDrawerItemRule(builder, tokens.navigationDrawer());
        appendNavigationDrawerGroupChildItemRule(
                builder,
                ".m3-navigation-drawer-group .m3-list-item.m3-navigation-drawer-group-child",
                tokens.navigationDrawer()
        );
        appendNavigationDrawerGroupChildItemRule(
                builder,
                ".m3-navigation-drawer .m3-navigation-drawer-group .m3-list-item.m3-navigation-drawer-group-child",
                tokens.navigationDrawer()
        );
        return builder.toString().stripTrailing();
    }

    /// Appends button token declarations.
    private static void append(StringBuilder builder, String prefix, ButtonTokens tokens) {
        append(builder, "-m3-" + prefix + "-container-height", pixels(tokens.height()));
        append(builder, "-m3-" + prefix + "-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-" + prefix + "-horizontal-padding", pixels(tokens.horizontalPadding()));
    }

    /// Appends the five-step button size token declarations.
    private static void append(StringBuilder builder, ButtonSizingTokens tokens) {
        append(builder, "button-extra-small", tokens.extraSmall());
        append(builder, "button-small", tokens.small());
        append(builder, "button-medium", tokens.medium());
        append(builder, "button-large", tokens.large());
        append(builder, "button-extra-large", tokens.extraLarge());
    }

    /// Appends declarations for one Material button size.
    private static void append(StringBuilder builder, String prefix, ButtonSizeTokens tokens) {
        append(builder, "-m3-" + prefix + "-container-height", pixels(tokens.containerHeight()));
        append(builder, "-m3-" + prefix + "-icon-size", pixels(tokens.iconSize()));
        append(
                builder,
                "-m3-" + prefix + "-round-container-shape",
                pixels(tokens.roundContainerShape())
        );
        append(
                builder,
                "-m3-" + prefix + "-square-container-shape",
                pixels(tokens.squareContainerShape())
        );
        append(
                builder,
                "-m3-" + prefix + "-pressed-container-shape",
                pixels(tokens.pressedContainerShape())
        );
        append(
                builder,
                "-m3-" + prefix + "-horizontal-padding",
                pixels(tokens.horizontalPadding())
        );
        append(
                builder,
                "-m3-" + prefix + "-text-horizontal-padding",
                pixels(tokens.textHorizontalPadding())
        );
        append(
                builder,
                "-m3-" + prefix + "-icon-label-space",
                pixels(tokens.iconLabelSpace())
        );
        append(builder, "-m3-" + prefix + "-outline-width", pixels(tokens.outlineWidth()));
    }

    /// Appends floating action button token declarations.
    private static void append(StringBuilder builder, FabTokens tokens) {
        append(builder, "small", tokens.small());
        append(builder, "regular", tokens.regular());
        append(builder, "medium", tokens.medium());
        append(builder, "large", tokens.large());
        append(builder, "-m3-fab-menu-action-spacing", pixels(tokens.menuActionSpacing()));
        append(builder, "menu-item", tokens.menuItem());
        append(builder, "menu-close-button", tokens.menuCloseButton());
        append(builder, "-m3-fab-menu-close-spacing", pixels(tokens.menuCloseSpacing()));
    }

    /// Appends one floating action button size token set.
    private static void append(StringBuilder builder, String name, FabSizeTokens tokens) {
        String prefix = "-m3-fab-" + name;
        append(builder, prefix + "-container-size", pixels(tokens.containerSize()));
        append(builder, prefix + "-container-shape", pixels(tokens.containerShape()));
        append(builder, prefix + "-icon-size", pixels(tokens.iconSize()));
        append(builder, prefix + "-leading-space", pixels(tokens.leadingSpace()));
        append(builder, prefix + "-icon-label-space", pixels(tokens.iconLabelSpace()));
        append(builder, prefix + "-trailing-space", pixels(tokens.trailingSpace()));
    }

    /// Appends icon token declarations.
    private static void append(StringBuilder builder, IconTokens tokens) {
        append(builder, "-m3-icon-small-size", pixels(tokens.smallSize()));
        append(builder, "-m3-icon-medium-size", pixels(tokens.mediumSize()));
        append(builder, "-m3-icon-large-size", pixels(tokens.largeSize()));
        append(builder, "-m3-icon-extra-large-size", pixels(tokens.extraLargeSize()));
    }

    /// Appends icon button token declarations.
    private static void append(StringBuilder builder, IconButtonTokens tokens) {
        append(builder, "icon-button-extra-small", tokens.extraSmall());
        append(builder, "icon-button-small", tokens.small());
        append(builder, "icon-button-medium", tokens.medium());
        append(builder, "icon-button-large", tokens.large());
        append(builder, "icon-button-extra-large", tokens.extraLarge());
    }

    /// Appends icon button size token declarations.
    private static void append(StringBuilder builder, String prefix, IconButtonSizeTokens tokens) {
        append(builder, "-m3-" + prefix + "-container-height", pixels(tokens.containerHeight()));
        append(builder, "-m3-" + prefix + "-icon-size", pixels(tokens.iconSize()));
        append(builder, "-m3-" + prefix + "-narrow-width", pixels(tokens.narrowWidth()));
        append(builder, "-m3-" + prefix + "-default-width", pixels(tokens.defaultWidth()));
        append(builder, "-m3-" + prefix + "-wide-width", pixels(tokens.wideWidth()));
        append(builder, "-m3-" + prefix + "-round-container-shape", pixels(tokens.roundContainerShape()));
        append(builder, "-m3-" + prefix + "-square-container-shape", pixels(tokens.squareContainerShape()));
        append(builder, "-m3-" + prefix + "-pressed-container-shape", pixels(tokens.pressedContainerShape()));
        append(
                builder,
                "-m3-" + prefix + "-selected-round-container-shape",
                pixels(tokens.selectedRoundContainerShape())
        );
        append(
                builder,
                "-m3-" + prefix + "-selected-square-container-shape",
                pixels(tokens.selectedSquareContainerShape())
        );
        append(builder, "-m3-" + prefix + "-outline-width", pixels(tokens.outlineWidth()));
    }

    /// Appends button-group token declarations.
    private static void append(StringBuilder builder, ButtonGroupTokens tokens) {
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group", tokens.small());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-extra-small", tokens.extraSmall());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-small", tokens.small());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-medium", tokens.medium());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-large", tokens.large());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-extra-large", tokens.extraLarge());
        append(
                builder,
                "-m3-button-group-spacing",
                pixels(tokens.small().connectedSpacing())
        );
        append(
                builder,
                "-m3-segmented-button-group-spacing",
                pixels(tokens.segmentedGroupSpacing())
        );
        append(
                builder,
                "-m3-icon-toggle-button-group-spacing",
                pixels(tokens.iconToggleGroupSpacing())
        );
    }

    /// Appends inline declarations for one button-group size role.
    ///
    /// @param builder the target CSS declaration builder
    /// @param prefix  the property prefix for the size role
    /// @param tokens  the size-specific button-group tokens
    private static void appendButtonGroupSizeDeclarations(
            StringBuilder builder,
            String prefix,
            ButtonGroupSizeTokens tokens
    ) {
        append(builder, prefix + "-container-height", pixels(tokens.containerHeight()));
        append(builder, prefix + "-standard-spacing", pixels(tokens.standardSpacing()));
        append(
                builder,
                prefix + "-standard-pressed-width-multiplier",
                Double.toString(tokens.standardPressedWidthMultiplier())
        );
        append(builder, prefix + "-connected-spacing", pixels(tokens.connectedSpacing()));
        append(
                builder,
                prefix + "-connected-inner-corner",
                pixels(tokens.connectedInnerCorner())
        );
        append(
                builder,
                prefix + "-connected-pressed-inner-corner",
                pixels(tokens.connectedPressedInnerCorner())
        );
        append(
                builder,
                prefix + "-connected-selected-inner-corner",
                pixels(tokens.connectedSelectedInnerCorner())
        );
    }

    /// Appends split button token declarations.
    private static void append(StringBuilder builder, SplitButtonTokens tokens) {
        appendSplitButtonSizeDeclarations(builder, "-m3-split-button", tokens.small());
        appendSplitButtonSizeDeclarations(builder, "-m3-split-button-extra-small", tokens.extraSmall());
        appendSplitButtonSizeDeclarations(builder, "-m3-split-button-small", tokens.small());
        appendSplitButtonSizeDeclarations(builder, "-m3-split-button-medium", tokens.medium());
        appendSplitButtonSizeDeclarations(builder, "-m3-split-button-large", tokens.large());
        appendSplitButtonSizeDeclarations(builder, "-m3-split-button-extra-large", tokens.extraLarge());
    }

    /// Appends inline declarations for one split-button size role.
    ///
    /// @param builder the target CSS declaration builder
    /// @param prefix  the property prefix for the size role
    /// @param tokens  the split-button size tokens
    private static void appendSplitButtonSizeDeclarations(
            StringBuilder builder,
            String prefix,
            SplitButtonSizeTokens tokens
    ) {
        append(builder, prefix + "-container-height", pixels(tokens.containerHeight()));
        append(builder, prefix + "-spacing", pixels(tokens.spacing()));
        append(builder, prefix + "-inner-corner", pixels(tokens.innerCorner()));
        append(
                builder,
                prefix + "-hovered-inner-corner",
                pixels(tokens.hoveredInnerCorner())
        );
        append(
                builder,
                prefix + "-pressed-inner-corner",
                pixels(tokens.pressedInnerCorner())
        );
        append(
                builder,
                prefix + "-action-leading-space",
                pixels(tokens.actionLeadingSpace())
        );
        append(
                builder,
                prefix + "-action-trailing-space",
                pixels(tokens.actionTrailingSpace())
        );
        append(
                builder,
                prefix + "-menu-width",
                pixels(tokens.menuLeadingSpace() + tokens.menuIconSize() + tokens.menuTrailingSpace())
        );
        append(builder, prefix + "-menu-icon-size", pixels(tokens.menuIconSize()));
        append(builder, prefix + "-menu-icon-offset", pixels(tokens.menuIconOffset()));
        append(
                builder,
                prefix + "-menu-leading-space",
                pixels(tokens.menuLeadingSpace())
        );
        append(
                builder,
                prefix + "-menu-trailing-space",
                pixels(tokens.menuTrailingSpace())
        );
        append(
                builder,
                prefix + "-selected-inner-corner",
                pixels(tokens.selectedInnerCorner())
        );
    }

    /// Appends tab token declarations.
    private static void append(StringBuilder builder, TabTokens tokens) {
        append(builder, "-m3-tab-container-height", pixels(tokens.containerHeight()));
        append(builder, "-m3-tab-min-width", pixels(tokens.tabMinWidth()));
        append(builder, "-m3-tab-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-tab-active-indicator-height", pixels(tokens.activeIndicatorHeight()));
        append(
                builder,
                "-m3-secondary-tab-active-indicator-height",
                pixels(tokens.secondaryActiveIndicatorHeight())
        );
        append(builder, "-m3-tab-active-indicator-shape", pixels(tokens.activeIndicatorShape()));
        append(
                builder,
                "-m3-tab-active-indicator-min-width",
                pixels(tokens.activeIndicatorMinWidth())
        );
        append(
                builder,
                "-m3-tab-active-indicator-horizontal-inset",
                pixels(tokens.activeIndicatorHorizontalInset())
        );
    }

    /// Appends field token declarations.
    private static void append(StringBuilder builder, FieldTokens tokens) {
        append(builder, "-m3-field-container-height", pixels(tokens.height()));
        append(builder, "-m3-field-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-field-horizontal-padding", pixels(tokens.horizontalPadding()));
    }

    /// Appends text area token declarations.
    private static void append(StringBuilder builder, TextAreaTokens tokens) {
        append(builder, "-m3-text-area-container-height", pixels(tokens.height()));
        append(builder, "-m3-text-area-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-text-area-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-text-area-vertical-padding", pixels(tokens.verticalPadding()));
    }

    /// Appends form token declarations.
    private static void append(StringBuilder builder, FormTokens tokens) {
        append(builder, "-m3-form-content-padding", pixels(tokens.contentPadding()));
        append(builder, "-m3-form-row-spacing", pixels(tokens.rowSpacing()));
        append(builder, "-m3-form-section-content-spacing", pixels(tokens.sectionContentSpacing()));
        append(builder, "-m3-form-section-header-spacing", pixels(tokens.sectionHeaderSpacing()));
        append(
                builder,
                "-m3-form-section-header-bottom-padding",
                pixels(tokens.sectionHeaderBottomPadding())
        );
        append(builder, "-m3-form-row-label-width", pixels(tokens.rowLabelWidth()));
        append(builder, "-m3-form-row-column-spacing", pixels(tokens.rowColumnSpacing()));
        append(builder, "-m3-form-row-min-height", pixels(tokens.rowMinHeight()));
        append(builder, "-m3-form-row-text-spacing", pixels(tokens.rowTextSpacing()));
    }

    /// Appends validation summary token declarations.
    private static void append(StringBuilder builder, ValidationSummaryTokens tokens) {
        append(
                builder,
                "-m3-validation-summary-container-shape",
                pixels(tokens.containerShape())
        );
        append(
                builder,
                "-m3-validation-summary-content-padding",
                pixels(tokens.contentPadding())
        );
        append(
                builder,
                "-m3-validation-summary-items-spacing",
                pixels(tokens.itemsSpacing())
        );
        append(builder, "-m3-validation-summary-item-shape", pixels(tokens.itemShape()));
        append(
                builder,
                "-m3-validation-summary-item-vertical-padding",
                pixels(tokens.itemVerticalPadding())
        );
        append(
                builder,
                "-m3-validation-summary-item-horizontal-padding",
                pixels(tokens.itemHorizontalPadding())
        );
    }

    /// Appends menu token declarations.
    private static void append(StringBuilder builder, MenuTokens tokens) {
        append(builder, "-m3-menu-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-menu-container-padding", pixels(tokens.containerPadding()));
        append(builder, "-m3-menu-item-height", pixels(tokens.itemHeight()));
        append(builder, "-m3-menu-item-container-shape", pixels(tokens.itemContainerShape()));
        append(
                builder,
                "-m3-menu-selected-item-container-shape",
                pixels(tokens.selectedItemContainerShape())
        );
        append(
                builder,
                "-m3-menu-active-item-container-shape",
                pixels(tokens.activeItemContainerShape())
        );
        append(
                builder,
                "-m3-menu-inner-corner-shape",
                pixels(tokens.innerCornerShape())
        );
        append(
                builder,
                "-m3-menu-first-item-container-shape",
                pixels(tokens.firstItemContainerShape())
        );
        append(
                builder,
                "-m3-menu-last-item-container-shape",
                pixels(tokens.lastItemContainerShape())
        );
        append(builder, "-m3-menu-item-horizontal-padding", pixels(tokens.itemHorizontalPadding()));
        append(builder, "-m3-menu-item-content-spacing", pixels(tokens.itemContentSpacing()));
        append(builder, "-m3-menu-item-spacing", pixels(tokens.itemSpacing()));
    }

    /// Appends search token declarations.
    private static void append(StringBuilder builder, SearchTokens tokens) {
        append(builder, "-m3-search-bar-container-height", pixels(tokens.barHeight()));
        append(builder, "-m3-search-bar-container-shape", pixels(tokens.barContainerShape()));
        append(builder, "-m3-search-bar-horizontal-padding", pixels(tokens.barHorizontalPadding()));
        append(builder, "-m3-search-bar-content-spacing", pixels(tokens.barContentSpacing()));
        append(builder, "-m3-search-contained-bar-horizontal-padding", pixels(tokens.containedBarHorizontalPadding()));
        append(builder, "-m3-search-contained-bar-content-spacing", pixels(tokens.containedBarContentSpacing()));
        append(builder, "-m3-search-divided-bar-horizontal-padding", pixels(tokens.dividedBarHorizontalPadding()));
        append(builder, "-m3-search-divided-bar-content-spacing", pixels(tokens.dividedBarContentSpacing()));
        append(builder, "-m3-search-bar-trailing-actions-gap", pixels(tokens.barTrailingActionsGap()));
        append(builder, "-m3-search-view-container-shape", pixels(tokens.viewContainerShape()));
        append(builder, "-m3-search-view-horizontal-padding", pixels(tokens.viewHorizontalPadding()));
        append(builder, "-m3-search-view-bar-results-gap", pixels(tokens.viewBarResultsGap()));
        append(builder, "-m3-search-view-results-shape", pixels(tokens.viewResultsShape()));
        append(builder, "-m3-search-view-docked-bottom-padding", pixels(tokens.viewDockedBottomPadding()));
        append(builder, "-m3-search-view-full-screen-bottom-padding", pixels(tokens.viewFullScreenBottomPadding()));
        append(builder, "-m3-search-view-min-width", pixels(tokens.viewMinWidth()));
        append(builder, "-m3-search-view-max-width", pixels(tokens.viewMaxWidth()));
        append(builder, "-m3-search-view-docked-min-height", pixels(tokens.viewDockedMinHeight()));
        append(builder, "-m3-search-view-full-screen-divided-header-height", pixels(tokens.viewFullScreenDividedHeaderHeight()));
    }

    /// Appends picker field token declarations.
    private static void append(StringBuilder builder, PickerFieldTokens tokens) {
        append(builder, "-m3-picker-field-open-button-size", pixels(tokens.openButtonSize()));
        append(builder, "-m3-picker-field-open-button-shape", pixels(tokens.openButtonShape()));
        append(builder, "-m3-picker-field-popup-shape", pixels(tokens.popupShape()));
        append(builder, "-m3-picker-field-popup-padding", pixels(tokens.popupPadding()));
        append(builder, "-m3-picker-field-popup-spacing", pixels(tokens.popupSpacing()));
        append(builder, "-m3-picker-field-preset-list-width", pixels(tokens.presetListWidth()));
        append(builder, "-m3-picker-field-preset-list-spacing", pixels(tokens.presetListSpacing()));
        append(
                builder,
                "-m3-picker-field-preset-button-horizontal-padding",
                pixels(tokens.presetButtonHorizontalPadding())
        );
    }

    /// Appends date picker token declarations.
    private static void append(StringBuilder builder, DatePickerTokens tokens) {
        append(builder, "-m3-date-picker-container-width", pixels(tokens.containerWidth()));
        append(builder, "-m3-date-picker-docked-container-shape", pixels(tokens.dockedContainerShape()));
        append(builder, "-m3-date-picker-modal-container-shape", pixels(tokens.modalContainerShape()));
        append(builder, "-m3-date-picker-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-date-picker-container-spacing", pixels(tokens.containerSpacing()));
        append(builder, "-m3-date-picker-header-height", pixels(tokens.headerHeight()));
        append(builder, "-m3-date-picker-header-spacing", pixels(tokens.headerSpacing()));
        append(builder, "-m3-date-picker-navigation-button-size", pixels(tokens.navigationButtonSize()));
        append(builder, "-m3-date-picker-navigation-button-shape", pixels(tokens.navigationButtonShape()));
        append(builder, "-m3-date-picker-menu-button-height", pixels(tokens.menuButtonHeight()));
        append(builder, "-m3-date-picker-day-cell-size", pixels(tokens.dayCellSize()));
        append(builder, "-m3-date-picker-day-state-layer-size", pixels(tokens.dayStateLayerSize()));
        append(builder, "-m3-date-picker-day-cell-shape", pixels(tokens.dayCellShape()));
        append(builder, "-m3-date-picker-grid-gap", pixels(tokens.gridGap()));
    }

    /// Appends time picker token declarations.
    private static void append(StringBuilder builder, TimePickerTokens tokens) {
        append(builder, "-m3-time-picker-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-time-picker-container-padding", pixels(tokens.containerPadding()));
        append(builder, "-m3-time-picker-container-spacing", pixels(tokens.containerSpacing()));
        append(builder, "-m3-time-picker-display-spacing", pixels(tokens.displaySpacing()));
        append(builder, "-m3-time-picker-display-cell-shape", pixels(tokens.displayCellShape()));
        append(builder, "-m3-time-picker-display-cell-width", pixels(tokens.displayCellWidth()));
        append(builder, "-m3-time-picker-display-24-hour-cell-width", pixels(tokens.display24HourCellWidth()));
        append(builder, "-m3-time-picker-display-cell-height", pixels(tokens.displayCellHeight()));
        append(builder, "-m3-time-picker-period-vertical-width", pixels(tokens.periodVerticalWidth()));
        append(builder, "-m3-time-picker-period-vertical-height", pixels(tokens.periodVerticalHeight()));
        append(builder, "-m3-time-picker-period-horizontal-width", pixels(tokens.periodHorizontalWidth()));
        append(builder, "-m3-time-picker-period-horizontal-height", pixels(tokens.periodHorizontalHeight()));
        append(builder, "-m3-time-picker-dial-size", pixels(tokens.dialSize()));
        append(builder, "-m3-time-picker-dial-handle-size", pixels(tokens.dialHandleSize()));
        append(builder, "-m3-time-picker-dial-center-size", pixels(tokens.dialCenterSize()));
        append(builder, "-m3-time-picker-dial-track-width", pixels(tokens.dialTrackWidth()));
        append(builder, "-m3-time-picker-input-field-width", pixels(tokens.inputFieldWidth()));
        append(builder, "-m3-time-picker-input-field-height", pixels(tokens.inputFieldHeight()));
    }

    /// Appends sheet token declarations.
    private static void append(StringBuilder builder, SheetTokens tokens) {
        append(builder, "-m3-sheet-side-container-width", pixels(tokens.sideContainerWidth()));
        append(builder, "-m3-sheet-side-container-max-width", pixels(tokens.sideContainerMaxWidth()));
        append(builder, "-m3-sheet-side-container-shape", pixels(tokens.sideContainerShape()));
        append(builder, "-m3-sheet-bottom-container-max-width", pixels(tokens.bottomContainerMaxWidth()));
        append(builder, "-m3-sheet-bottom-container-shape", pixels(tokens.bottomContainerShape()));
        append(builder, "-m3-sheet-content-padding", pixels(tokens.contentPadding()));
        append(builder, "-m3-sheet-header-padding", pixels(tokens.headerPadding()));
        append(builder, "-m3-sheet-header-content-spacing", pixels(tokens.headerContentSpacing()));
        append(builder, "-m3-sheet-drag-handle-vertical-padding", pixels(tokens.dragHandleVerticalPadding()));
        append(builder, "-m3-sheet-drag-handle-width", pixels(tokens.dragHandleWidth()));
        append(builder, "-m3-sheet-drag-handle-height", pixels(tokens.dragHandleHeight()));
    }

    /// Appends scrim token declarations.
    private static void append(StringBuilder builder, ScrimTokens tokens) {
        append(builder, "-m3-scrim-container-opacity", Double.toString(tokens.containerOpacity()));
    }

    /// Appends selection token declarations.
    private static void append(StringBuilder builder, SelectionTokens tokens) {
        append(builder, "-m3-selection-touch-target-size", pixels(tokens.touchTargetSize()));
        append(builder, "-m3-selection-state-layer-size", pixels(tokens.stateLayerSize()));
        append(builder, "-m3-checkbox-container-size", pixels(tokens.checkboxContainerSize()));
        append(builder, "-m3-checkbox-selected-mark-width", pixels(tokens.checkboxSelectedMarkWidth()));
        append(builder, "-m3-checkbox-selected-mark-height", pixels(tokens.checkboxSelectedMarkHeight()));
        append(builder, "-m3-checkbox-indeterminate-mark-width", pixels(tokens.checkboxIndeterminateMarkWidth()));
        append(builder, "-m3-checkbox-indeterminate-mark-height", pixels(tokens.checkboxIndeterminateMarkHeight()));
        append(builder, "-m3-radio-container-size", pixels(tokens.radioContainerSize()));
        append(builder, "-m3-radio-selected-dot-size", pixels(tokens.radioSelectedDotSize()));
        append(builder, "-m3-selection-track-shape", pixels(tokens.trackShape()));
        append(builder, "-m3-switch-touch-target-size", pixels(tokens.switchTouchTargetSize()));
        append(builder, "-m3-switch-track-width", pixels(tokens.switchTrackWidth()));
        append(builder, "-m3-switch-track-height", pixels(tokens.switchTrackHeight()));
        append(builder, "-m3-switch-state-layer-size", pixels(tokens.switchStateLayerSize()));
        append(builder, "-m3-switch-unselected-handle-size", pixels(tokens.switchUnselectedHandleSize()));
        append(builder, "-m3-switch-with-icon-handle-size", pixels(tokens.switchWithIconHandleSize()));
        append(builder, "-m3-switch-selected-handle-size", pixels(tokens.switchSelectedHandleSize()));
        append(builder, "-m3-switch-pressed-handle-size", pixels(tokens.switchPressedHandleSize()));
        append(builder, "-m3-switch-icon-size", pixels(tokens.switchIconSize()));
    }

    /// Appends slider token declarations.
    private static void append(StringBuilder builder, SliderTokens tokens) {
        SliderSizeTokens defaultSize = tokens.sizing().extraSmall();
        append(builder, "-m3-slider-track-thickness", pixels(defaultSize.trackThickness()));
        append(builder, "-m3-slider-track-shape", pixels(defaultSize.trackShape()));
        append(
                builder,
                "-m3-slider-stop-indicator-size",
                pixels(tokens.stopIndicatorSize())
        );
        append(
                builder,
                "-m3-slider-stop-indicator-trailing-space",
                pixels(tokens.stopIndicatorTrailingSpace())
        );
        append(builder, "-m3-slider-thumb-size", pixels(defaultSize.thumbSize()));
        append(builder, "-m3-slider-thumb-width", pixels(tokens.thumbWidth()));
        append(
                builder,
                "-m3-slider-focused-thumb-width",
                pixels(tokens.focusedThumbWidth())
        );
        append(
                builder,
                "-m3-slider-pressed-thumb-width",
                pixels(tokens.pressedThumbWidth())
        );
        append(builder, "-m3-slider-thumb-track-gap", pixels(tokens.thumbTrackGap()));
        append(builder, "-m3-slider-touch-target-size", pixels(tokens.touchTargetSize()));
    }

    /// Appends chip token declarations.
    private static void append(StringBuilder builder, ChipTokens tokens) {
        append(builder, "-m3-chip-container-height", pixels(tokens.height()));
        append(builder, "-m3-chip-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-chip-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-chip-icon-horizontal-padding", pixels(tokens.iconHorizontalPadding()));
        append(builder, "-m3-chip-element-spacing", pixels(tokens.elementSpacing()));
        append(builder, "-m3-chip-icon-size", pixels(tokens.iconSize()));
        append(builder, "-m3-chip-avatar-size", pixels(tokens.avatarSize()));
        append(builder, "-m3-chip-avatar-shape", pixels(tokens.avatarShape()));
        append(builder, "-m3-chip-outline-width", pixels(tokens.outlineWidth()));
        append(builder, "-m3-chip-group-horizontal-gap", pixels(tokens.groupHorizontalGap()));
        append(builder, "-m3-chip-group-vertical-gap", pixels(tokens.groupVerticalGap()));
    }

    /// Appends progress token declarations.
    private static void append(StringBuilder builder, ProgressTokens tokens) {
        append(builder, "-m3-progress-thickness", pixels(tokens.thickness()));
        append(builder, "-m3-progress-shape", pixels(tokens.shape()));
        append(builder, "-m3-progress-indicator-size", pixels(tokens.indicatorSize()));
        append(
                builder,
                "-m3-progress-wave-indicator-size",
                pixels(tokens.waveIndicatorSize())
        );
        append(builder, "-m3-progress-linear-wave-amplitude", pixels(tokens.linearWaveAmplitude()));
        append(builder, "-m3-progress-linear-wavelength", pixels(tokens.linearWavelength()));
        append(
                builder,
                "-m3-progress-linear-indeterminate-wavelength",
                pixels(tokens.linearIndeterminateWavelength())
        );
        append(builder, "-m3-progress-linear-track-gap", pixels(tokens.linearTrackGap()));
        append(builder, "-m3-progress-linear-stop-size", pixels(tokens.linearStopSize()));
        append(builder, "-m3-progress-circular-wave-amplitude", pixels(tokens.circularWaveAmplitude()));
        append(builder, "-m3-progress-circular-wavelength", pixels(tokens.circularWavelength()));
        append(builder, "-m3-progress-circular-track-gap", pixels(tokens.circularTrackGap()));
    }

    /// Appends loading indicator token declarations.
    private static void append(StringBuilder builder, LoadingIndicatorTokens tokens) {
        append(builder, "-m3-loading-indicator-container-size", pixels(tokens.containerSize()));
        append(builder, "-m3-loading-indicator-indicator-size", pixels(tokens.indicatorSize()));
    }

    /// Appends surface token declarations.
    private static void append(StringBuilder builder, SurfaceTokens tokens) {
        append(builder, "-m3-surface-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-surface-content-padding", pixels(tokens.contentPadding()));
    }

    /// Appends carousel token declarations.
    private static void append(StringBuilder builder, CarouselTokens tokens) {
        append(
                builder,
                "-m3-carousel-track-horizontal-padding",
                pixels(tokens.trackHorizontalPadding())
        );
        append(
                builder,
                "-m3-carousel-track-vertical-padding",
                pixels(tokens.trackVerticalPadding())
        );
        append(builder, "-m3-carousel-item-spacing", pixels(tokens.itemSpacing()));
        append(builder, "-m3-carousel-item-shape", pixels(tokens.itemShape()));
        append(
                builder,
                "-m3-carousel-small-item-min-width",
                pixels(tokens.smallItemMinWidth())
        );
        append(
                builder,
                "-m3-carousel-small-item-max-width",
                pixels(tokens.smallItemMaxWidth())
        );
        append(
                builder,
                "-m3-carousel-large-item-max-width",
                pixels(tokens.largeItemMaxWidth())
        );
    }

    /// Appends card token declarations.
    private static void append(StringBuilder builder, CardTokens tokens) {
        append(builder, "-m3-card-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-card-content-padding", pixels(tokens.contentPadding()));
        append(builder, "-m3-card-outline-width", pixels(tokens.outlineWidth()));
    }

    /// Appends dialog token declarations.
    private static void append(StringBuilder builder, DialogTokens tokens) {
        append(builder, "-m3-dialog-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-dialog-content-padding", pixels(tokens.contentPadding()));
        append(builder, "-m3-dialog-container-min-width", pixels(tokens.containerMinWidth()));
        append(builder, "-m3-dialog-container-max-width", pixels(tokens.containerMaxWidth()));
        append(builder, "-m3-dialog-action-spacing", pixels(tokens.actionSpacing()));
        append(builder, "-m3-dialog-icon-size", pixels(tokens.iconSize()));
    }

    /// Appends snackbar token declarations.
    private static void append(StringBuilder builder, SnackbarTokens tokens) {
        append(builder, "-m3-snackbar-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-snackbar-content-padding", pixels(tokens.contentPadding()));
        append(builder, "-m3-snackbar-container-min-width", pixels(tokens.containerMinWidth()));
        append(builder, "-m3-snackbar-container-max-width", pixels(tokens.containerMaxWidth()));
        append(
                builder,
                "-m3-snackbar-single-line-container-height",
                pixels(tokens.singleLineContainerHeight())
        );
        append(
                builder,
                "-m3-snackbar-two-line-container-height",
                pixels(tokens.twoLineContainerHeight())
        );
        append(
                builder,
                "-m3-snackbar-action-container-height",
                pixels(tokens.actionContainerHeight())
        );
    }

    /// Appends banner token declarations.
    private static void append(StringBuilder builder, BannerTokens tokens) {
        append(builder, "-m3-banner-container-min-height", pixels(tokens.containerMinHeight()));
        append(builder, "-m3-banner-vertical-padding", pixels(tokens.verticalPadding()));
        append(builder, "-m3-banner-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-banner-content-spacing", pixels(tokens.contentSpacing()));
        append(builder, "-m3-banner-action-spacing", pixels(tokens.actionSpacing()));
    }

    /// Appends tooltip token declarations.
    private static void append(StringBuilder builder, TooltipTokens tokens) {
        append(builder, "-m3-tooltip-plain-container-shape", pixels(tokens.plainContainerShape()));
        append(builder, "-m3-tooltip-plain-vertical-padding", pixels(tokens.plainVerticalPadding()));
        append(builder, "-m3-tooltip-plain-horizontal-padding", pixels(tokens.plainHorizontalPadding()));
        append(builder, "-m3-tooltip-rich-container-shape", pixels(tokens.richContainerShape()));
        append(builder, "-m3-tooltip-rich-top-padding", pixels(tokens.richTopPadding()));
        append(builder, "-m3-tooltip-rich-horizontal-padding", pixels(tokens.richHorizontalPadding()));
        append(builder, "-m3-tooltip-rich-bottom-padding", pixels(tokens.richBottomPadding()));
        append(builder, "-m3-tooltip-rich-content-spacing", pixels(tokens.richContentSpacing()));
        append(builder, "-m3-tooltip-rich-pref-width", pixels(tokens.richPreferredWidth()));
        append(builder, "-m3-tooltip-rich-action-spacing", pixels(tokens.richActionSpacing()));
        append(
                builder,
                "-m3-tooltip-rich-action-button-container-height",
                pixels(tokens.richActionButtonHeight())
        );
        append(
                builder,
                "-m3-tooltip-rich-action-button-horizontal-padding",
                pixels(tokens.richActionButtonHorizontalPadding())
        );
    }

    /// Appends divider token declarations.
    private static void append(StringBuilder builder, DividerTokens tokens) {
        append(builder, "-m3-divider-thickness", pixels(tokens.thickness()));
        append(builder, "-m3-divider-inset-start", pixels(tokens.insetStart()));
        append(builder, "-m3-divider-inset-end", pixels(tokens.insetEnd()));
    }

    /// Appends badge token declarations.
    private static void append(StringBuilder builder, BadgeTokens tokens) {
        append(builder, "-m3-badge-small-size", pixels(tokens.smallSize()));
        append(builder, "-m3-badge-large-height", pixels(tokens.largeHeight()));
        append(builder, "-m3-badge-large-min-width", pixels(tokens.largeMinWidth()));
        append(builder, "-m3-badge-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-badge-horizontal-padding", pixels(tokens.horizontalPadding()));
    }

    /// Appends avatar token declarations.
    private static void append(StringBuilder builder, AvatarTokens tokens) {
        append(builder, "-m3-avatar-container-size", pixels(tokens.containerSize()));
        append(builder, "-m3-avatar-container-shape", pixels(tokens.containerShape()));
    }

    /// Appends top app bar token declarations.
    private static void append(StringBuilder builder, TopAppBarTokens tokens) {
        append(builder, "-m3-top-app-bar-container-height", pixels(tokens.containerHeight()));
        append(builder, "-m3-top-app-bar-medium-container-height", pixels(tokens.mediumContainerHeight()));
        append(builder, "-m3-top-app-bar-large-container-height", pixels(tokens.largeContainerHeight()));
        append(builder, "-m3-top-app-bar-medium-flexible-container-height",
                pixels(tokens.mediumFlexibleContainerHeight()));
        append(builder, "-m3-top-app-bar-medium-flexible-subtitle-container-height",
                pixels(tokens.mediumFlexibleSubtitleContainerHeight()));
        append(builder, "-m3-top-app-bar-large-flexible-container-height",
                pixels(tokens.largeFlexibleContainerHeight()));
        append(builder, "-m3-top-app-bar-large-flexible-subtitle-container-height",
                pixels(tokens.largeFlexibleSubtitleContainerHeight()));
        append(builder, "-m3-top-app-bar-edge-padding", pixels(tokens.edgePadding()));
        append(builder, "-m3-top-app-bar-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-top-app-bar-medium-bottom-padding", pixels(tokens.mediumBottomPadding()));
        append(builder, "-m3-top-app-bar-large-bottom-padding", pixels(tokens.largeBottomPadding()));
        append(builder, "-m3-top-app-bar-flexible-bottom-padding",
                pixels(tokens.flexibleBottomPadding()));
        append(builder, "-m3-top-app-bar-content-spacing", pixels(tokens.contentSpacing()));
        append(builder, "-m3-top-app-bar-action-spacing", pixels(tokens.actionSpacing()));
    }

    /// Appends bottom app bar token declarations.
    private static void append(StringBuilder builder, BottomAppBarTokens tokens) {
        append(builder, "-m3-bottom-app-bar-container-height", pixels(tokens.containerHeight()));
        append(builder, "-m3-bottom-app-bar-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-bottom-app-bar-content-spacing", pixels(tokens.contentSpacing()));
        append(builder, "-m3-bottom-app-bar-action-spacing", pixels(tokens.actionSpacing()));
    }

    /// Appends toolbar token declarations.
    private static void append(StringBuilder builder, ToolbarTokens tokens) {
        append(builder, "-m3-toolbar-container-height", pixels(tokens.containerHeight()));
        append(builder, "-m3-toolbar-container-width", pixels(tokens.containerWidth()));
        append(builder, "-m3-toolbar-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-toolbar-item-slot-size", pixels(tokens.itemSlotSize()));
        append(builder, "-m3-toolbar-content-padding", pixels(tokens.contentPadding()));
        append(
                builder,
                "-m3-toolbar-docked-content-padding",
                pixels(tokens.dockedContentPadding())
        );
        append(builder, "-m3-toolbar-item-spacing", pixels(tokens.itemSpacing()));
        append(
                builder,
                "-m3-toolbar-docked-max-item-spacing",
                pixels(tokens.dockedMaxItemSpacing())
        );
    }

    /// Appends navigation bar token declarations.
    private static void append(StringBuilder builder, NavigationBarTokens tokens) {
        append(builder, "-m3-navigation-bar-container-height", pixels(tokens.containerHeight()));
        append(builder, "-m3-navigation-bar-item-width", pixels(tokens.itemWidth()));
        append(builder, "-m3-navigation-bar-indicator-width", pixels(tokens.indicatorWidth()));
        append(builder, "-m3-navigation-bar-indicator-height", pixels(tokens.indicatorHeight()));
        append(builder, "-m3-navigation-bar-indicator-shape", pixels(tokens.indicatorShape()));
        append(builder, "-m3-navigation-bar-content-spacing", pixels(tokens.contentSpacing()));
        append(builder, "-m3-navigation-bar-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-navigation-bar-item-spacing", pixels(tokens.itemSpacing()));
    }

    /// Appends navigation rail token declarations.
    private static void append(StringBuilder builder, NavigationRailTokens tokens) {
        append(
                builder,
                "-m3-navigation-rail-collapsed-container-width",
                pixels(tokens.collapsedContainerWidth())
        );
        append(
                builder,
                "-m3-navigation-rail-narrow-collapsed-container-width",
                pixels(tokens.narrowCollapsedContainerWidth())
        );
        append(
                builder,
                "-m3-navigation-rail-expanded-minimum-container-width",
                pixels(tokens.expandedMinimumContainerWidth())
        );
        append(
                builder,
                "-m3-navigation-rail-expanded-container-width",
                pixels(tokens.expandedContainerWidth())
        );
        append(
                builder,
                "-m3-navigation-rail-expanded-maximum-container-width",
                pixels(tokens.expandedMaximumContainerWidth())
        );
        append(builder, "-m3-navigation-rail-item-height", pixels(tokens.itemHeight()));
        append(builder, "-m3-navigation-rail-item-width", pixels(tokens.itemWidth()));
        append(builder, "-m3-navigation-rail-indicator-width", pixels(tokens.indicatorWidth()));
        append(builder, "-m3-navigation-rail-indicator-height", pixels(tokens.indicatorHeight()));
        append(builder, "-m3-navigation-rail-indicator-shape", pixels(tokens.indicatorShape()));
        append(builder, "-m3-navigation-rail-content-spacing", pixels(tokens.contentSpacing()));
        append(
                builder,
                "-m3-navigation-rail-collapsed-top-padding",
                pixels(tokens.collapsedTopPadding())
        );
        append(
                builder,
                "-m3-navigation-rail-collapsed-bottom-padding",
                pixels(tokens.collapsedBottomPadding())
        );
        append(builder, "-m3-navigation-rail-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-navigation-rail-item-spacing", pixels(tokens.itemSpacing()));
        append(
                builder,
                "-m3-navigation-rail-expanded-top-padding",
                pixels(tokens.expandedTopPadding())
        );
        append(
                builder,
                "-m3-navigation-rail-expanded-bottom-padding",
                pixels(tokens.expandedBottomPadding())
        );
        append(
                builder,
                "-m3-navigation-rail-modal-container-shape",
                pixels(tokens.modalContainerShape())
        );
    }

    /// Appends navigation drawer token declarations.
    private static void append(StringBuilder builder, NavigationDrawerTokens tokens) {
        append(builder, "-m3-navigation-drawer-container-width", pixels(tokens.containerWidth()));
        append(builder, "-m3-navigation-drawer-one-line-item-height", pixels(tokens.oneLineItemHeight()));
        append(builder, "-m3-navigation-drawer-two-line-item-height", pixels(tokens.twoLineItemHeight()));
        append(builder, "-m3-navigation-drawer-three-line-item-height", pixels(tokens.threeLineItemHeight()));
        append(builder, "-m3-navigation-drawer-item-container-shape", pixels(tokens.itemContainerShape()));
        append(builder, "-m3-navigation-drawer-container-padding", pixels(tokens.containerPadding()));
        append(builder, "-m3-navigation-drawer-item-horizontal-padding", pixels(tokens.itemHorizontalPadding()));
        append(builder, "-m3-navigation-drawer-item-vertical-padding", pixels(tokens.itemVerticalPadding()));
        append(builder, "-m3-navigation-drawer-item-content-spacing", pixels(tokens.itemContentSpacing()));
        append(builder, "-m3-navigation-drawer-item-spacing", pixels(tokens.itemSpacing()));
        append(
                builder,
                "-m3-navigation-drawer-group-child-item-height",
                pixels(tokens.groupChildItemHeight())
        );
        append(
                builder,
                "-m3-navigation-drawer-group-child-item-container-shape",
                pixels(tokens.groupChildItemContainerShape())
        );
        append(
                builder,
                "-m3-navigation-drawer-group-child-item-horizontal-padding",
                pixels(tokens.groupChildItemHorizontalPadding())
        );
    }

    /// Appends list item token declarations.
    private static void append(StringBuilder builder, ListItemTokens tokens) {
        append(builder, "-m3-list-item-one-line-height", pixels(tokens.oneLineHeight()));
        append(builder, "-m3-list-item-two-line-height", pixels(tokens.twoLineHeight()));
        append(builder, "-m3-list-item-three-line-height", pixels(tokens.threeLineHeight()));
        append(builder, "-m3-list-item-container-shape", pixels(tokens.containerShape()));
        append(builder, "-m3-list-item-horizontal-padding", pixels(tokens.horizontalPadding()));
        append(builder, "-m3-list-item-vertical-padding", pixels(tokens.verticalPadding()));
        append(builder, "-m3-list-item-content-spacing", pixels(tokens.contentSpacing()));
        append(builder, "-m3-list-segmented-gap", pixels(tokens.segmentedGap()));
        append(builder, "-m3-list-section-header-height", pixels(tokens.sectionHeaderHeight()));
        append(
                builder,
                "-m3-list-section-header-horizontal-padding",
                pixels(tokens.sectionHeaderHorizontalPadding())
        );
    }

    /// Appends a button token CSS rule.
    private static void appendButtonRule(StringBuilder builder, String selector, ButtonTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends generated metrics and state shapes for every Material button size.
    private static void appendButtonSizeRules(
            StringBuilder builder,
            ButtonSizingTokens tokens,
            boolean expressive
    ) {
        appendButtonSizeRules(builder, ".m3-button-extra-small", tokens.extraSmall(), expressive);
        appendButtonSizeRules(builder, ".m3-button-small", tokens.small(), expressive);
        appendButtonSizeRules(builder, ".m3-button-medium", tokens.medium(), expressive);
        appendButtonSizeRules(builder, ".m3-button-large", tokens.large(), expressive);
        appendButtonSizeRules(builder, ".m3-button-extra-large", tokens.extraLarge(), expressive);
    }

    /// Appends generated metrics and shapes for one Material button size.
    private static void appendButtonSizeRules(
            StringBuilder builder,
            String sizeSelector,
            ButtonSizeTokens tokens,
            boolean expressive
    ) {
        String selector = ".m3-button" + sizeSelector;
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-button-icon-size", pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-graphic-text-gap", pixels(tokens.iconLabelSpace()));
        endRule(builder);

        beginRule(builder, selector + ".m3-text-button");
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.textHorizontalPadding()));
        endRule(builder);

        beginRule(builder, selector + ".m3-outlined-button");
        appendDeclaration(builder, "-fx-border-width", pixels(tokens.outlineWidth()));
        endRule(builder);

        beginRule(builder, selector + ".m3-button-round");
        appendShapeDeclarations(builder, tokens.roundContainerShape());
        endRule(builder);

        beginRule(builder, selector + ".m3-button-square");
        appendShapeDeclarations(builder, tokens.squareContainerShape());
        endRule(builder);

        if (expressive) {
            beginRule(
                    builder,
                    selector + ".m3-button-round:armed, "
                            + selector + ".m3-button-round:pressed, "
                            + selector + ".m3-button-square:armed, "
                            + selector + ".m3-button-square:pressed"
            );
            appendShapeDeclarations(builder, tokens.pressedContainerShape());
            endRule(builder);
        }
    }

    /// Appends generated icon size rules.
    private static void appendIconRules(StringBuilder builder, IconTokens tokens) {
        appendIconRule(builder, ".m3-small-icon", tokens.smallSize());
        appendIconRule(builder, ".m3-medium-icon", tokens.mediumSize());
        appendIconRule(builder, ".m3-large-icon", tokens.largeSize());
        appendIconRule(builder, ".m3-extra-large-icon", tokens.extraLargeSize());
    }

    /// Appends a generated icon size rule.
    private static void appendIconRule(StringBuilder builder, String selector, double size) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-icon-size", pixels(size));
        endRule(builder);
    }

    /// Appends generated icon button size, width, and shape rules.
    private static void appendIconButtonRules(
            StringBuilder builder,
            IconButtonTokens tokens,
            boolean expressive
    ) {
        appendIconButtonSizeRules(builder, ".m3-icon-button-extra-small", tokens.extraSmall());
        appendIconButtonSizeRules(builder, ".m3-icon-button-small", tokens.small());
        appendIconButtonSizeRules(builder, ".m3-icon-button-medium", tokens.medium());
        appendIconButtonSizeRules(builder, ".m3-icon-button-large", tokens.large());
        appendIconButtonSizeRules(builder, ".m3-icon-button-extra-large", tokens.extraLarge());

        appendIconButtonShapeRules(builder, ".m3-icon-button-extra-small", tokens.extraSmall(), expressive);
        appendIconButtonShapeRules(builder, ".m3-icon-button-small", tokens.small(), expressive);
        appendIconButtonShapeRules(builder, ".m3-icon-button-medium", tokens.medium(), expressive);
        appendIconButtonShapeRules(builder, ".m3-icon-button-large", tokens.large(), expressive);
        appendIconButtonShapeRules(builder, ".m3-icon-button-extra-large", tokens.extraLarge(), expressive);
    }

    /// Appends generated icon button metrics for one size class.
    private static void appendIconButtonSizeRules(
            StringBuilder builder,
            String selector,
            IconButtonSizeTokens tokens
    ) {
        String iconButtonSelector = ".m3-button.m3-icon-button" + selector;
        String toggleButtonSelector = ".m3-icon-toggle-button" + selector;
        beginRule(
                builder,
                iconButtonSelector + ", " + toggleButtonSelector
        );
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-container-width", pixels(tokens.defaultWidth()));
        appendDeclaration(builder, "-m3-button-icon-size", pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-icon-button-icon-size", pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-icon-button-outline-width", pixels(tokens.outlineWidth()));
        appendDeclaration(builder, "-m3-horizontal-padding", "0px");
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-icon-button-narrow-width, "
                + toggleButtonSelector + ".m3-icon-button-narrow-width");
        appendDeclaration(builder, "-m3-container-width", pixels(tokens.narrowWidth()));
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-icon-button-wide-width, "
                + toggleButtonSelector + ".m3-icon-button-wide-width");
        appendDeclaration(builder, "-m3-container-width", pixels(tokens.wideWidth()));
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-outlined-button, "
                + toggleButtonSelector + ".m3-outlined-icon-toggle-button");
        appendDeclaration(builder, "-fx-border-width", pixels(tokens.outlineWidth()));
        endRule(builder);
    }


    /// Appends generated square, selected, and pressed shape rules for one icon button size.
    private static void appendIconButtonShapeRules(
            StringBuilder builder,
            String selector,
            IconButtonSizeTokens tokens,
            boolean expressive
    ) {
        String iconButtonSelector = ".m3-button.m3-icon-button" + selector;
        String toggleButtonSelector = ".m3-icon-toggle-button" + selector;
        beginRule(builder, iconButtonSelector + ".m3-icon-button-round");
        appendShapeDeclarations(builder, tokens.roundContainerShape());
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-icon-button-square");
        appendShapeDeclarations(builder, tokens.squareContainerShape());
        endRule(builder);

        if (expressive) {
            beginRule(builder, toggleButtonSelector + ".m3-icon-button-round:selected");
            appendShapeDeclarations(builder, tokens.selectedRoundContainerShape());
            endRule(builder);

            beginRule(builder, toggleButtonSelector + ".m3-icon-button-square:selected");
            appendShapeDeclarations(builder, tokens.selectedSquareContainerShape());
            endRule(builder);

            beginRule(
                    builder,
                    iconButtonSelector + ".m3-icon-button-round:armed, "
                            + iconButtonSelector + ".m3-icon-button-round:pressed, "
                            + iconButtonSelector + ".m3-icon-button-square:armed, "
                            + iconButtonSelector + ".m3-icon-button-square:pressed, "
                            + toggleButtonSelector + ".m3-icon-button-round:armed, "
                            + toggleButtonSelector + ".m3-icon-button-round:pressed, "
                            + toggleButtonSelector + ".m3-icon-button-square:armed, "
                            + toggleButtonSelector + ".m3-icon-button-square:pressed"
            );
            appendShapeDeclarations(builder, tokens.pressedContainerShape());
            endRule(builder);
        }
    }

    /// Appends generated shape declarations shared by icon button shape states.
    private static void appendShapeDeclarations(StringBuilder builder, double shape) {
        String radius = pixels(shape);
        appendDeclaration(builder, "-m3-container-shape", radius);
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
    }

    /// Appends a generated single spacing rule.
    private static void appendGroupSpacingRule(StringBuilder builder, String selector, String property, double spacing) {
        beginRule(builder, selector);
        appendDeclaration(builder, property, pixels(spacing));
        endRule(builder);
    }

    /// Appends a floating action button token CSS rule.
    private static void appendFabRule(
            StringBuilder builder,
            String selector,
            FabSizeTokens tokens
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-size", pixels(tokens.containerSize()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-fab-icon-size", pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.leadingSpace()));
        appendDeclaration(builder, "-m3-trailing-padding", pixels(tokens.trailingSpace()));
        appendDeclaration(builder, "-fx-graphic-text-gap", pixels(tokens.iconLabelSpace()));
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a tab token CSS rule.
    private static void appendTabRule(StringBuilder builder, TabTokens tokens) {
        beginRule(builder, ".m3-tab");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-tab-min-width", pixels(tokens.tabMinWidth()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-active-indicator-height", pixels(tokens.activeIndicatorHeight()));
        appendDeclaration(builder, "-m3-active-indicator-shape", pixels(tokens.activeIndicatorShape()));
        appendDeclaration(builder, "-m3-active-indicator-min-width", pixels(tokens.activeIndicatorMinWidth()));
        appendDeclaration(
                builder,
                "-m3-active-indicator-horizontal-inset",
                pixels(tokens.activeIndicatorHorizontalInset())
        );
        endRule(builder);

        beginRule(builder, ".m3-tab:secondary");
        appendDeclaration(
                builder,
                "-m3-active-indicator-height",
                pixels(tokens.secondaryActiveIndicatorHeight())
        );
        appendDeclaration(builder, "-m3-active-indicator-shape", "0px");
        endRule(builder);
    }

    /// Appends a tab active indicator token CSS rule.
    private static void appendTabIndicatorRule(StringBuilder builder, TabTokens tokens) {
        beginRule(builder, ".m3-tab-active-indicator");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.activeIndicatorShape())
                + " "
                + pixels(tokens.activeIndicatorShape())
                + " 0 0");
        endRule(builder);
    }

    /// Appends segmented button position shape CSS rules.
    private static void appendSegmentedButtonPositionRules(StringBuilder builder, ButtonTokens tokens) {
        String radius = pixels(tokens.containerShape());
        appendSegmentedButtonPositionRule(
                builder,
                ".m3-segmented-button.m3-segmented-button-single",
                radius,
                radius,
                radius,
                radius
        );
        appendSegmentedButtonPositionRule(
                builder,
                ".m3-segmented-button.m3-segmented-button-first",
                radius,
                "0",
                "0",
                radius
        );
        appendSegmentedButtonPositionRule(
                builder,
                ".m3-segmented-button.m3-segmented-button-middle",
                "0",
                "0",
                "0",
                "0"
        );
        appendSegmentedButtonPositionRule(
                builder,
                ".m3-segmented-button.m3-segmented-button-last",
                "0",
                radius,
                radius,
                "0"
        );
    }

    /// Appends standard and connected button-group rules for every Material size role.
    private static void appendConnectedButtonRules(
            StringBuilder builder,
            ButtonGroupTokens groupTokens,
            boolean expressive
    ) {
        appendButtonGroupSizeRules(
                builder,
                ".m3-button-group-extra-small",
                groupTokens.extraSmall(),
                expressive
        );
        appendButtonGroupSizeRules(builder, ".m3-button-group-small", groupTokens.small(), expressive);
        appendButtonGroupSizeRules(builder, ".m3-button-group-medium", groupTokens.medium(), expressive);
        appendButtonGroupSizeRules(builder, ".m3-button-group-large", groupTokens.large(), expressive);
        appendButtonGroupSizeRules(
                builder,
                ".m3-button-group-extra-large",
                groupTokens.extraLarge(),
                expressive
        );
    }

    /// Appends spacing and connected-state shape rules for one button-group size.
    ///
    /// @param builder      the target CSS builder
    /// @param sizeSelector the button-group size selector
    /// @param tokens       the size-specific button-group tokens
    private static void appendButtonGroupSizeRules(
            StringBuilder builder,
            String sizeSelector,
            ButtonGroupSizeTokens tokens,
            boolean expressive
    ) {
        String groupSelector = ".m3-button-group" + sizeSelector;
        String outerRadius = pixels(tokens.containerHeight() / 2.0);
        beginRule(builder, groupSelector);
        appendDeclaration(
                builder,
                "-m3-button-group-container-height",
                pixels(tokens.containerHeight())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-standard-spacing",
                pixels(tokens.standardSpacing())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-standard-pressed-width-multiplier",
                Double.toString(tokens.standardPressedWidthMultiplier())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-spacing",
                pixels(tokens.connectedSpacing())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-inner-corner",
                pixels(tokens.connectedInnerCorner())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-pressed-inner-corner",
                pixels(tokens.connectedPressedInnerCorner())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-selected-inner-corner",
                pixels(tokens.connectedSelectedInnerCorner())
        );
        endRule(builder);

        beginRule(builder, groupSelector + ".m3-standard-button-group");
        appendDeclaration(builder, "-m3-button-group-spacing", pixels(tokens.standardSpacing()));
        endRule(builder);

        String connectedSelector = groupSelector + ".m3-connected-button-group";
        beginRule(builder, connectedSelector);
        appendDeclaration(builder, "-m3-button-group-spacing", pixels(tokens.connectedSpacing()));
        endRule(builder);

        appendConnectedButtonStateShapeRules(
                builder,
                connectedSelector,
                "",
                outerRadius,
                pixels(tokens.connectedInnerCorner())
        );
        if (expressive) {
            appendConnectedButtonStateShapeRules(
                    builder,
                    connectedSelector,
                    ":selected",
                    outerRadius,
                    pixels(tokens.connectedSelectedInnerCorner())
            );
            appendConnectedButtonStateShapeRules(
                    builder,
                    connectedSelector,
                    ":armed",
                    outerRadius,
                    pixels(tokens.connectedPressedInnerCorner())
            );
            appendConnectedButtonStateShapeRules(
                    builder,
                    connectedSelector,
                    ":pressed",
                    outerRadius,
                    pixels(tokens.connectedPressedInnerCorner())
            );
        }
    }

    /// Appends position-specific connected-button shapes for one interaction state.
    ///
    /// @param builder       the target CSS builder
    /// @param groupSelector the connected button-group selector
    /// @param stateSuffix   the child pseudo-class suffix, or an empty string for the resting state
    /// @param outerRadius   the outer corner radius
    /// @param innerRadius   the inner corner radius for this state
    private static void appendConnectedButtonStateShapeRules(
            StringBuilder builder,
            String groupSelector,
            String stateSuffix,
            String outerRadius,
            String innerRadius
    ) {
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-grouped-button.m3-button-group-single" + stateSuffix,
                outerRadius,
                outerRadius,
                outerRadius,
                outerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-grouped-button.m3-button-group-first" + stateSuffix,
                outerRadius,
                innerRadius,
                innerRadius,
                outerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-grouped-button.m3-button-group-middle" + stateSuffix,
                innerRadius,
                innerRadius,
                innerRadius,
                innerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-grouped-button.m3-button-group-last" + stateSuffix,
                innerRadius,
                outerRadius,
                outerRadius,
                innerRadius
        );
    }

    /// Appends split-button size, logical spacing, icon, and state-shape rules.
    private static void appendSplitButtonRules(
            StringBuilder builder,
            SplitButtonTokens splitButtonTokens
    ) {
        appendSplitButtonSizeRule(builder, ".m3-split-button", splitButtonTokens.small());
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-extra-small",
                splitButtonTokens.extraSmall()
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-small",
                splitButtonTokens.small()
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-medium",
                splitButtonTokens.medium()
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-large",
                splitButtonTokens.large()
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-extra-large",
                splitButtonTokens.extraLarge()
        );
    }

    /// Appends generated split-button rules for one size role.
    ///
    /// @param builder       the target generated stylesheet
    /// @param ownerSelector the split-button owner selector
    /// @param tokens        the metrics for the size role
    private static void appendSplitButtonSizeRule(
            StringBuilder builder,
            String ownerSelector,
            SplitButtonSizeTokens tokens
    ) {
        double menuWidth = tokens.menuLeadingSpace() + tokens.menuIconSize() + tokens.menuTrailingSpace();
        String outerRadius = pixels(tokens.containerHeight() / 2.0);
        beginRule(builder, ownerSelector);
        appendDeclaration(builder, "-m3-split-button-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-split-button-spacing", pixels(tokens.spacing()));
        appendDeclaration(builder, "-m3-split-button-outer-corner", outerRadius);
        appendDeclaration(builder, "-m3-split-button-inner-corner", pixels(tokens.innerCorner()));
        appendDeclaration(
                builder,
                "-m3-split-button-hovered-inner-corner",
                pixels(tokens.hoveredInnerCorner())
        );
        appendDeclaration(
                builder,
                "-m3-split-button-pressed-inner-corner",
                pixels(tokens.pressedInnerCorner())
        );
        appendDeclaration(
                builder,
                "-m3-split-button-selected-inner-corner",
                pixels(tokens.selectedInnerCorner())
        );
        appendDeclaration(builder, "-m3-split-button-menu-width", pixels(menuWidth));
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-action");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-action:left-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + pixels(tokens.actionTrailingSpace())
                        + " 0 "
                        + pixels(tokens.actionLeadingSpace())
        );
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-action:right-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + pixels(tokens.actionLeadingSpace())
                        + " 0 "
                        + pixels(tokens.actionTrailingSpace())
        );
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-menu");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-horizontal-padding", "0px");
        appendDeclaration(builder, "-fx-min-width", pixels(menuWidth));
        appendDeclaration(builder, "-fx-pref-width", pixels(menuWidth));
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-menu:left-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + pixels(tokens.menuLeadingSpace())
                        + " 0 "
                        + pixels(tokens.menuTrailingSpace())
        );
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-menu:right-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + pixels(tokens.menuTrailingSpace())
                        + " 0 "
                        + pixels(tokens.menuLeadingSpace())
        );
        endRule(builder);

        String indicatorSelector =
                ownerSelector + " .m3-button.m3-split-button-menu .m3-disclosure-icon";
        beginRule(builder, indicatorSelector);
        appendDeclaration(builder, "-m3-disclosure-icon-size", pixels(tokens.menuIconSize()));
        endRule(builder);

        beginRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:left-edge .m3-disclosure-icon"
        );
        appendDeclaration(builder, "-fx-translate-x", pixels(tokens.menuIconOffset()));
        endRule(builder);

        beginRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:right-edge .m3-disclosure-icon"
        );
        appendDeclaration(builder, "-fx-translate-x", pixels(-tokens.menuIconOffset()));
        endRule(builder);

        beginRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:showing .m3-disclosure-icon"
        );
        appendDeclaration(builder, "-fx-translate-x", "0px");
        endRule(builder);

    }

    /// Appends a connected button position shape CSS rule.
    private static void appendConnectedButtonShapeRule(
            StringBuilder builder,
            String selector,
            String topLeft,
            String topRight,
            String bottomRight,
            String bottomLeft
    ) {
        String radii = topLeft + " " + topRight + " " + bottomRight + " " + bottomLeft;
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radii);
        appendDeclaration(builder, "-fx-border-radius", radii);
        endRule(builder);
    }

    /// Appends a segmented button position shape CSS rule.
    private static void appendSegmentedButtonPositionRule(
            StringBuilder builder,
            String selector,
            String topLeft,
            String topRight,
            String bottomRight,
            String bottomLeft
    ) {
        String radii = topLeft + " " + topRight + " " + bottomRight + " " + bottomLeft;
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radii);
        appendDeclaration(builder, "-fx-border-radius", radii);
        endRule(builder);
    }

    /// Appends a field token CSS rule.
    private static void appendFieldRule(StringBuilder builder, FieldTokens tokens) {
        beginRule(builder, ".m3-text-field, .m3-password-field");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a text area token CSS rule.
    private static void appendTextAreaRule(StringBuilder builder, TextAreaTokens tokens) {
        beginRule(builder, ".m3-text-area");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", pixels(tokens.verticalPadding()));
        endRule(builder);
    }

    /// Appends a filled field shape CSS rule.
    private static void appendFilledFieldRule(StringBuilder builder, FieldTokens tokens) {
        String radius = pixels(tokens.containerShape());
        beginRule(builder, ".m3-filled-field");
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends an outlined field shape CSS rule.
    private static void appendOutlinedFieldRule(StringBuilder builder, FieldTokens tokens) {
        String radius = pixels(tokens.containerShape());
        beginRule(builder, ".m3-outlined-field");
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a filled text area shape CSS rule.
    private static void appendFilledTextAreaRule(StringBuilder builder, TextAreaTokens tokens) {
        String radius = pixels(tokens.containerShape());
        beginRule(builder, ".m3-text-area.m3-filled-field");
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends an outlined text area shape CSS rule.
    private static void appendOutlinedTextAreaRule(StringBuilder builder, TextAreaTokens tokens) {
        String radius = pixels(tokens.containerShape());
        beginRule(builder, ".m3-text-area.m3-outlined-field");
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a form pane token CSS rule.
    private static void appendFormPaneRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-pane");
        appendDeclaration(builder, "-m3-content-padding", pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-row-spacing", pixels(tokens.rowSpacing()));
        endRule(builder);
    }

    /// Appends a form section token CSS rule.
    private static void appendFormSectionRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-section");
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.sectionContentSpacing()));
        endRule(builder);
    }

    /// Appends a form section header token CSS rule.
    private static void appendFormSectionHeaderRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-section-header");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.sectionHeaderSpacing()));
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 0 " + pixels(tokens.sectionHeaderBottomPadding()) + " 0"
        );
        endRule(builder);
    }

    /// Appends a form row token CSS rule.
    private static void appendFormRowRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-row");
        appendDeclaration(builder, "-m3-label-width", pixels(tokens.rowLabelWidth()));
        appendDeclaration(builder, "-m3-column-spacing", pixels(tokens.rowColumnSpacing()));
        appendDeclaration(builder, "-m3-row-min-height", pixels(tokens.rowMinHeight()));
        endRule(builder);
    }

    /// Appends a form row text column token CSS rule.
    private static void appendFormRowTextColumnRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-row-text-column");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.rowTextSpacing()));
        endRule(builder);
    }

    /// Appends a validation summary token CSS rule.
    private static void appendValidationSummaryRule(
            StringBuilder builder,
            ValidationSummaryTokens tokens
    ) {
        beginRule(builder, ".m3-validation-summary");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a validation summary item container token CSS rule.
    private static void appendValidationSummaryItemsRule(
            StringBuilder builder,
            ValidationSummaryTokens tokens
    ) {
        beginRule(builder, ".m3-validation-summary-items");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.itemsSpacing()));
        endRule(builder);
    }

    /// Appends a validation summary item token CSS rule.
    private static void appendValidationSummaryItemRule(
            StringBuilder builder,
            ValidationSummaryTokens tokens
    ) {
        String shape = pixels(tokens.itemShape());
        String verticalPadding = pixels(tokens.itemVerticalPadding());
        String horizontalPadding = pixels(tokens.itemHorizontalPadding());
        beginRule(builder, ".m3-validation-summary-item");
        appendDeclaration(builder, "-fx-background-radius", shape);
        appendDeclaration(builder, "-fx-border-radius", shape);
        appendDeclaration(builder, "-fx-padding", verticalPadding + " " + horizontalPadding);
        endRule(builder);
    }

    /// Appends a menu token CSS rule.
    private static void appendMenuRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu.m3-menu");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", pixels(tokens.containerPadding()));
        endRule(builder);
    }

    /// Appends a menu item container token CSS rule.
    private static void appendMenuContainerRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-container");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a menu item token CSS rule.
    private static void appendMenuItemRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item");
        appendDeclaration(builder, "-m3-one-line-height", pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-two-line-height", pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-three-line-height", pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.itemContainerShape()));
        appendDeclaration(builder, "-m3-menu-inner-corner-shape", pixels(tokens.innerCornerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.itemHorizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", pixels(0.0));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.itemContentSpacing()));
        endRule(builder);
    }

    /// Appends structural menu item token CSS rules.
    private static void appendMenuEdgeItemRules(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item:first-menu-item");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.firstItemContainerShape()));
        endRule(builder);
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item:last-menu-item");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.lastItemContainerShape()));
        endRule(builder);
    }

    /// Appends selected menu item token CSS rule.
    private static void appendSelectedMenuItemRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item:selected");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.selectedItemContainerShape()));
        endRule(builder);

        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item:active");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.activeItemContainerShape()));
        endRule(builder);
    }

    /// Appends a search bar token CSS rule.
    private static void appendSearchBarRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-bar.m3-search-bar");
        appendDeclaration(builder, "-fx-min-height", pixels(tokens.barHeight()));
        appendDeclaration(builder, "-fx-pref-height", pixels(tokens.barHeight()));
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.barContainerShape()));
        appendDeclaration(builder, "-fx-padding", "0 " + pixels(tokens.barHorizontalPadding()));
        endRule(builder);
    }

    /// Appends a search bar content token CSS rule.
    private static void appendSearchBarContentRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-bar .m3-search-bar-content");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.barContentSpacing()));
        endRule(builder);
    }

    /// Appends a search bar trailing-actions token CSS rule.
    private static void appendSearchBarTrailingRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-bar .m3-search-bar-trailing");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.barTrailingActionsGap()));
        endRule(builder);
    }

    /// Appends a search view token CSS rule.
    private static void appendSearchViewRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-view.m3-search-view:docked");
        appendDeclaration(builder, "-fx-min-width", pixels(tokens.viewMinWidth()));
        appendDeclaration(builder, "-fx-max-width", pixels(tokens.viewMaxWidth()));
        endRule(builder);

        beginRule(builder, ".m3-search-view.m3-search-view:docked:active");
        appendDeclaration(builder, "-fx-min-height", pixels(tokens.viewDockedMinHeight()));
        endRule(builder);

        beginRule(builder, ".m3-search-view.m3-search-view:full-screen");
        appendDeclaration(builder, "-fx-min-width", "-1");
        appendDeclaration(builder, "-fx-max-width", "-1");
        appendDeclaration(builder, "-fx-min-height", "-1");
        endRule(builder);

        beginRule(builder, ".m3-search-view.m3-search-view:contained:docked");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.viewContainerShape()));
        String horizontalPadding = pixels(tokens.viewHorizontalPadding());
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 " + horizontalPadding + " "
                        + pixels(tokens.viewDockedBottomPadding()) + " " + horizontalPadding
        );
        endRule(builder);

        beginRule(builder, ".m3-search-view.m3-search-view:contained:full-screen");
        appendDeclaration(builder, "-fx-background-radius", "0");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 " + horizontalPadding + " "
                        + pixels(tokens.viewFullScreenBottomPadding()) + " " + horizontalPadding
        );
        endRule(builder);

        beginRule(builder, ".m3-search-view.m3-search-view:divided:docked");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.viewContainerShape()));
        appendDeclaration(builder, "-fx-padding", "0");
        endRule(builder);

        beginRule(builder, ".m3-search-view.m3-search-view:divided:full-screen");
        appendDeclaration(builder, "-fx-background-radius", "0");
        appendDeclaration(builder, "-fx-padding", "0");
        endRule(builder);

        beginRule(builder, ".m3-search-view:contained .m3-search-bar.m3-search-bar");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 " + pixels(tokens.containedBarHorizontalPadding())
        );
        endRule(builder);

        beginRule(builder, ".m3-search-view:divided .m3-search-bar.m3-search-bar");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 " + pixels(tokens.dividedBarHorizontalPadding())
        );
        endRule(builder);

        beginRule(builder, ".m3-search-view:divided:full-screen .m3-search-bar.m3-search-bar");
        appendDeclaration(builder, "-fx-min-height", pixels(tokens.viewFullScreenDividedHeaderHeight()));
        appendDeclaration(builder, "-fx-pref-height", pixels(tokens.viewFullScreenDividedHeaderHeight()));
        endRule(builder);
    }

    /// Appends a search view content token CSS rule.
    private static void appendSearchViewContentRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-view:contained .m3-search-view-content");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.viewBarResultsGap()));
        endRule(builder);

        beginRule(builder, ".m3-search-view:divided .m3-search-view-content");
        appendDeclaration(builder, "-fx-spacing", "0");
        endRule(builder);

        beginRule(builder, ".m3-search-view:contained .m3-search-bar-content");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.containedBarContentSpacing()));
        endRule(builder);

        beginRule(builder, ".m3-search-view:divided .m3-search-bar-content");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.dividedBarContentSpacing()));
        endRule(builder);
    }

    /// Appends a search view results container token CSS rule.
    private static void appendSearchViewResultsRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-view:contained .m3-search-view-results");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.viewResultsShape()));
        endRule(builder);

        beginRule(builder, ".m3-search-view:divided .m3-search-view-results");
        appendDeclaration(builder, "-fx-background-radius", "0");
        endRule(builder);
    }

    /// Appends a picker field popup token CSS rule.
    private static void appendPickerFieldRule(StringBuilder builder, PickerFieldTokens tokens) {
        beginRule(builder, ".m3-picker-field-popup");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.popupShape()));
        endRule(builder);
    }

    /// Appends a picker field open button token CSS rule.
    private static void appendPickerFieldOpenButtonRule(
            StringBuilder builder,
            PickerFieldTokens tokens
    ) {
        beginRule(builder, ".m3-picker-field-open-button");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.openButtonShape()));
        appendDeclaration(builder, "-fx-min-width", pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-fx-pref-width", pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-fx-min-height", pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-fx-pref-height", pixels(tokens.openButtonSize()));
        endRule(builder);
    }

    /// Appends a picker field preset content token CSS rule.
    private static void appendPickerFieldPresetContentRule(StringBuilder builder, PickerFieldTokens tokens) {
        beginRule(
                builder,
                ".m3-date-picker-field-preset-content, "
                        + ".m3-date-range-picker-field-preset-content, "
                        + ".m3-time-picker-field-preset-content"
        );
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.popupShape()));
        appendDeclaration(builder, "-fx-padding", pixels(tokens.popupPadding()));
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.popupSpacing()));
        endRule(builder);
    }

    /// Appends a picker field preset list token CSS rule.
    private static void appendPickerFieldPresetListRule(StringBuilder builder, PickerFieldTokens tokens) {
        beginRule(
                builder,
                ".m3-date-picker-field-preset-list, "
                        + ".m3-date-range-picker-field-preset-list, "
                        + ".m3-time-picker-field-preset-list"
        );
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.presetListSpacing()));
        appendDeclaration(builder, "-fx-pref-width", pixels(tokens.presetListWidth()));
        endRule(builder);
    }

    /// Appends a picker field preset button token CSS rule.
    private static void appendPickerFieldPresetButtonRule(StringBuilder builder, PickerFieldTokens tokens) {
        beginRule(
                builder,
                ".m3-date-picker-field-preset-button, "
                        + ".m3-date-range-picker-field-preset-button, "
                        + ".m3-time-picker-field-preset-button"
        );
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.presetButtonHorizontalPadding()));
        appendDeclaration(builder, "-fx-pref-width", pixels(tokens.presetListWidth()));
        appendDeclaration(builder, "-fx-max-width", pixels(tokens.presetListWidth()));
        endRule(builder);
    }

    /// Appends a date picker container token CSS rule.
    private static void appendDatePickerRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-container");
        String width = pixels(tokens.containerWidth());
        appendDeclaration(builder, "-fx-min-width", width);
        appendDeclaration(builder, "-fx-pref-width", width);
        appendDeclaration(builder, "-fx-max-width", width);
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.dockedContainerShape()));
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 " + pixels(tokens.horizontalPadding())
        );
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.containerSpacing()));
        endRule(builder);
        beginRule(builder, ".m3-date-picker:modal .m3-date-picker-container");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.modalContainerShape()));
        endRule(builder);
        beginRule(builder, ".m3-date-range-picker .m3-date-picker-container");
        appendDeclaration(builder, "-fx-background-radius", pixels(0.0));
        endRule(builder);
    }

    /// Appends a date picker header token CSS rule.
    private static void appendDatePickerHeaderRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-header");
        String height = pixels(tokens.headerHeight());
        appendDeclaration(builder, "-fx-min-height", height);
        appendDeclaration(builder, "-fx-pref-height", height);
        appendDeclaration(builder, "-fx-max-height", height);
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.headerSpacing()));
        endRule(builder);
        beginRule(builder, ".m3-date-picker-header-section");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.headerSpacing()));
        endRule(builder);
        beginRule(builder, ".m3-date-picker-menu-button");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.menuButtonHeight()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.navigationButtonShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(4.0));
        endRule(builder);
        beginRule(builder, ".m3-date-picker-month-menu-button");
        appendDeclaration(builder, "-fx-min-width", pixels(80.0));
        appendDeclaration(builder, "-fx-pref-width", pixels(80.0));
        appendDeclaration(builder, "-fx-max-width", pixels(80.0));
        endRule(builder);
        beginRule(builder, ".m3-date-picker-year-menu-button");
        appendDeclaration(builder, "-fx-min-width", pixels(96.0));
        appendDeclaration(builder, "-fx-pref-width", pixels(96.0));
        appendDeclaration(builder, "-fx-max-width", pixels(96.0));
        endRule(builder);
    }

    /// Appends a date picker navigation button token CSS rule.
    private static void appendDatePickerNavigationButtonRule(
            StringBuilder builder,
            DatePickerTokens tokens
    ) {
        beginRule(builder, ".m3-date-picker-navigation-button");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.navigationButtonSize()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.navigationButtonShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(0.0));
        appendDeclaration(builder, "-fx-min-width", pixels(tokens.navigationButtonSize()));
        appendDeclaration(builder, "-fx-pref-width", pixels(tokens.navigationButtonSize()));
        appendDeclaration(builder, "-fx-max-width", pixels(tokens.navigationButtonSize()));
        endRule(builder);
    }

    /// Appends a date picker weekday row token CSS rule.
    private static void appendDatePickerWeekdayRowRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-weekday-row");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.gridGap()));
        endRule(builder);
    }

    /// Appends a date picker day grid token CSS rule.
    private static void appendDatePickerGridRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-day-grid");
        appendDeclaration(builder, "-fx-hgap", pixels(tokens.gridGap()));
        appendDeclaration(builder, "-fx-vgap", pixels(tokens.gridGap()));
        endRule(builder);
    }

    /// Appends date picker day and weekday cell token CSS rules.
    private static void appendDatePickerCellRule(StringBuilder builder, DatePickerTokens tokens) {
        String size = pixels(tokens.dayCellSize());
        String shape = pixels(tokens.dayCellShape());
        String stateLayerInset = pixels(
                Math.max(0.0, (tokens.dayCellSize() - tokens.dayStateLayerSize()) / 2.0)
        );
        beginRule(builder, ".m3-date-picker-day-cell, .m3-date-picker-weekday-label");
        appendDeclaration(builder, "-fx-min-width", size);
        appendDeclaration(builder, "-fx-pref-width", size);
        appendDeclaration(builder, "-fx-max-width", size);
        appendDeclaration(builder, "-fx-min-height", size);
        appendDeclaration(builder, "-fx-pref-height", size);
        appendDeclaration(builder, "-fx-max-height", size);
        endRule(builder);
        beginRule(builder, ".m3-date-picker-day-cell");
        appendDeclaration(builder, "-fx-background-radius", shape);
        appendDeclaration(builder, "-fx-background-insets", stateLayerInset);
        appendDeclaration(builder, "-fx-border-radius", shape);
        appendDeclaration(builder, "-fx-border-insets", stateLayerInset);
        endRule(builder);
    }

    /// Appends date range cell shape token CSS rules.
    private static void appendDatePickerCellShapeRules(StringBuilder builder, DatePickerTokens tokens) {
        String shape = pixels(tokens.dayCellShape());
        String inset = pixels(
                Math.max(0.0, (tokens.dayCellSize() - tokens.dayStateLayerSize()) / 2.0)
        );
        String halfCell = pixels(tokens.dayCellSize() / 2.0);

        beginRule(builder, ".m3-date-picker-day-cell.m3-date-range-picker-range-middle-day");
        appendDeclaration(builder, "-fx-background-insets", inset + " 0");
        appendDeclaration(builder, "-fx-background-radius", "0");
        endRule(builder);
        beginRule(
                builder,
                ".m3-date-picker-day-cell.m3-date-range-picker-range-middle-day"
                        + ".m3-date-range-picker-range-row-start-day"
        );
        appendDeclaration(builder, "-fx-background-radius", shape + " 0 0 " + shape);
        endRule(builder);
        beginRule(
                builder,
                ".m3-date-picker-day-cell.m3-date-range-picker-range-middle-day"
                        + ".m3-date-range-picker-range-row-end-day"
        );
        appendDeclaration(builder, "-fx-background-radius", "0 " + shape + " " + shape + " 0");
        endRule(builder);
        beginRule(
                builder,
                ".m3-date-picker-day-cell:rtl.m3-date-range-picker-range-middle-day"
                        + ".m3-date-range-picker-range-row-start-day"
        );
        appendDeclaration(builder, "-fx-background-radius", "0 " + shape + " " + shape + " 0");
        endRule(builder);
        beginRule(
                builder,
                ".m3-date-picker-day-cell:rtl.m3-date-range-picker-range-middle-day"
                        + ".m3-date-range-picker-range-row-end-day"
        );
        appendDeclaration(builder, "-fx-background-radius", shape + " 0 0 " + shape);
        endRule(builder);
        beginRule(builder, ".m3-date-picker-day-cell.m3-date-range-picker-range-start-day");
        appendDeclaration(builder, "-fx-background-insets", inset + " 0 " + inset + " " + halfCell + ", " + inset);
        appendDeclaration(builder, "-fx-background-radius", "0, " + shape);
        endRule(builder);
        beginRule(builder, ".m3-date-picker-day-cell.m3-date-range-picker-range-end-day");
        appendDeclaration(builder, "-fx-background-insets", inset + " " + halfCell + " " + inset + " 0, " + inset);
        appendDeclaration(builder, "-fx-background-radius", "0, " + shape);
        endRule(builder);
        beginRule(builder, ".m3-date-picker-day-cell:rtl.m3-date-range-picker-range-start-day");
        appendDeclaration(builder, "-fx-background-insets", inset + " " + halfCell + " " + inset + " 0, " + inset);
        appendDeclaration(builder, "-fx-background-radius", "0, " + shape);
        endRule(builder);
        beginRule(builder, ".m3-date-picker-day-cell:rtl.m3-date-range-picker-range-end-day");
        appendDeclaration(builder, "-fx-background-insets", inset + " 0 " + inset + " " + halfCell + ", " + inset);
        appendDeclaration(builder, "-fx-background-radius", "0, " + shape);
        endRule(builder);
        beginRule(builder, ".m3-date-picker-day-cell.m3-date-range-picker-range-single-day");
        appendDeclaration(builder, "-fx-background-insets", inset);
        appendDeclaration(builder, "-fx-background-radius", shape);
        endRule(builder);
    }

    /// Appends typed Time Picker metrics consumed directly by the custom skin.
    private static void appendTimePickerControlRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker");
        appendDeclaration(builder, "-m3-container-spacing", pixels(tokens.containerSpacing()));
        appendDeclaration(builder, "-m3-dial-handle-size", pixels(tokens.dialHandleSize()));
        appendDeclaration(builder, "-m3-dial-center-size", pixels(tokens.dialCenterSize()));
        endRule(builder);
    }

    /// Appends Time Picker container shape and padding, excluding padding already owned by dialogs.
    private static void appendTimePickerContainerRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-container");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", pixels(tokens.containerPadding()));
        endRule(builder);

        beginRule(builder, ".m3-time-picker-dialog-content .m3-time-picker-container");
        appendDeclaration(builder, "-fx-padding", pixels(0.0));
        endRule(builder);
    }

    /// Appends spacing for Time Picker display and keyboard-input rows.
    private static void appendTimePickerDisplayRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-display, .m3-time-picker-input-content");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.displaySpacing()));
        endRule(builder);
    }

    /// Appends shape and dimensions for 12-hour Time Picker display cells.
    private static void appendTimePickerDisplayCellRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-hour-display, .m3-time-picker-minute-display");
        String shape = pixels(tokens.displayCellShape());
        appendDeclaration(builder, "-fx-background-radius", shape);
        appendDeclaration(builder, "-fx-border-radius", shape);
        appendTimePickerSize(builder, tokens.displayCellWidth(), tokens.displayCellHeight());
        endRule(builder);
    }

    /// Appends the wider display-cell dimensions used by 24-hour Time Pickers.
    private static void appendTimePicker24HourDisplayCellRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(
                builder,
                ".m3-time-picker:twenty-four-hour .m3-time-picker-hour-display, "
                        + ".m3-time-picker:twenty-four-hour .m3-time-picker-minute-display"
        );
        appendTimePickerWidth(builder, tokens.display24HourCellWidth());
        endRule(builder);
    }

    /// Appends vertical, horizontal, and input-mode period selector dimensions.
    private static void appendTimePickerPeriodRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-period-row:vertical");
        appendTimePickerSize(builder, tokens.periodVerticalWidth(), tokens.periodVerticalHeight());
        endRule(builder);

        beginRule(builder, ".m3-time-picker-period-row:vertical:input");
        appendTimePickerHeight(builder, tokens.inputFieldHeight());
        endRule(builder);

        beginRule(builder, ".m3-time-picker-period-row:horizontal");
        appendTimePickerSize(builder, tokens.periodHorizontalWidth(), tokens.periodHorizontalHeight());
        endRule(builder);
    }

    /// Appends the clock dial diameter.
    private static void appendTimePickerDialRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-dial");
        appendTimePickerSize(builder, tokens.dialSize(), tokens.dialSize());
        endRule(builder);
    }

    /// Appends the clock dial selector track width.
    private static void appendTimePickerDialTrackRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-dial-track");
        appendDeclaration(builder, "-fx-stroke-width", pixels(tokens.dialTrackWidth()));
        endRule(builder);
    }

    /// Appends shape and dimensions for keyboard-input fields.
    private static void appendTimePickerInputFieldRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-input-field");
        String shape = pixels(tokens.displayCellShape());
        appendDeclaration(builder, "-fx-background-radius", shape);
        appendDeclaration(builder, "-fx-border-radius", shape);
        appendTimePickerSize(builder, tokens.inputFieldWidth(), tokens.inputFieldHeight());
        endRule(builder);
    }

    /// Appends fixed width and height declarations for one Time Picker role.
    private static void appendTimePickerSize(StringBuilder builder, double width, double height) {
        appendTimePickerWidth(builder, width);
        appendTimePickerHeight(builder, height);
    }

    /// Appends fixed width declarations for one Time Picker role.
    private static void appendTimePickerWidth(StringBuilder builder, double width) {
        String value = pixels(width);
        appendDeclaration(builder, "-fx-min-width", value);
        appendDeclaration(builder, "-fx-pref-width", value);
        appendDeclaration(builder, "-fx-max-width", value);
    }

    /// Appends fixed height declarations for one Time Picker role.
    private static void appendTimePickerHeight(StringBuilder builder, double height) {
        String value = pixels(height);
        appendDeclaration(builder, "-fx-min-height", value);
        appendDeclaration(builder, "-fx-pref-height", value);
        appendDeclaration(builder, "-fx-max-height", value);
    }


    /// Appends a side sheet token CSS rule.
    private static void appendSideSheetRule(StringBuilder builder, SheetTokens tokens) {
        String radius = pixels(tokens.sideContainerShape());
        beginRule(builder, ".m3-side-sheet.m3-side-sheet");
        appendDeclaration(builder, "-fx-min-width", pixels(tokens.sideContainerWidth()));
        appendDeclaration(builder, "-fx-pref-width", pixels(tokens.sideContainerWidth()));
        appendDeclaration(builder, "-fx-max-width", pixels(tokens.sideContainerMaxWidth()));
        appendDeclaration(builder, "-fx-background-radius", "0");
        endRule(builder);

        beginRule(builder, ".m3-side-sheet.m3-modal-sheet");
        appendDeclaration(builder, "-fx-background-radius", radius + " 0 0 " + radius);
        endRule(builder);

        beginRule(builder, ".m3-side-sheet.m3-modal-sheet:rtl");
        appendDeclaration(builder, "-fx-background-radius", "0 " + radius + " " + radius + " 0");
        endRule(builder);

        beginRule(builder, ".m3-side-sheet.m3-side-sheet:detached");
        appendDeclaration(builder, "-fx-background-radius", radius);
        endRule(builder);
    }

    /// Appends a bottom sheet token CSS rule.
    private static void appendBottomSheetRule(StringBuilder builder, SheetTokens tokens) {
        String radius = pixels(tokens.bottomContainerShape());
        beginRule(builder, ".m3-bottom-sheet.m3-bottom-sheet");
        appendDeclaration(builder, "-fx-max-width", pixels(tokens.bottomContainerMaxWidth()));
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends a sheet header token CSS rule.
    private static void appendSheetHeaderRule(StringBuilder builder, SheetTokens tokens) {
        String padding = pixels(tokens.headerPadding());
        String bottomPadding = pixels(tokens.headerContentSpacing());
        beginRule(builder, ".m3-side-sheet .m3-sheet-header, .m3-bottom-sheet .m3-sheet-header");
        appendDeclaration(builder, "-fx-padding", padding + " " + padding + " " + bottomPadding + " " + padding);
        endRule(builder);
    }

    /// Appends a sheet content token CSS rule.
    private static void appendSheetContentRule(StringBuilder builder, SheetTokens tokens) {
        beginRule(builder, ".m3-side-sheet .m3-sheet-content, .m3-bottom-sheet .m3-sheet-content");
        appendDeclaration(builder, "-fx-padding", pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a bottom sheet drag handle token CSS rule.
    private static void appendBottomSheetDragHandleRule(StringBuilder builder, SheetTokens tokens) {
        String handleWidth = pixels(tokens.dragHandleWidth());
        String handleHeight = pixels(tokens.dragHandleHeight());
        beginRule(builder, ".m3-bottom-sheet .m3-bottom-sheet-drag-handle-container");
        appendDeclaration(
                builder,
                "-fx-padding",
                pixels(tokens.dragHandleVerticalPadding()) + " 0"
        );
        endRule(builder);

        beginRule(builder, ".m3-bottom-sheet .m3-bottom-sheet-drag-handle");
        appendDeclaration(builder, "-fx-min-width", handleWidth);
        appendDeclaration(builder, "-fx-pref-width", handleWidth);
        appendDeclaration(builder, "-fx-max-width", handleWidth);
        appendDeclaration(builder, "-fx-min-height", handleHeight);
        appendDeclaration(builder, "-fx-pref-height", handleHeight);
        appendDeclaration(builder, "-fx-max-height", handleHeight);
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.dragHandleHeight() / 2.0));
        endRule(builder);
    }

    /// Appends a scrim token CSS rule.
    private static void appendScrimRule(StringBuilder builder, ScrimTokens tokens) {
        beginRule(builder, ".m3-scrim.m3-scrim");
        appendDeclaration(builder, "-fx-opacity", Double.toString(tokens.containerOpacity()));
        endRule(builder);
    }

    /// Appends a selection token CSS rule.
    private static void appendSelectionRule(StringBuilder builder, SelectionTokens tokens) {
        beginRule(builder, ".m3-checkbox, .m3-radio-button");
        appendDeclaration(builder, "-m3-touch-target-size", pixels(tokens.touchTargetSize()));
        appendDeclaration(builder, "-m3-state-layer-size", pixels(tokens.stateLayerSize()));
        endRule(builder);

        beginRule(builder, ".m3-checkbox");
        appendDeclaration(builder, "-m3-container-size", pixels(tokens.checkboxContainerSize()));
        appendDeclaration(builder, "-m3-selected-mark-width", pixels(tokens.checkboxSelectedMarkWidth()));
        appendDeclaration(builder, "-m3-selected-mark-height", pixels(tokens.checkboxSelectedMarkHeight()));
        appendDeclaration(builder, "-m3-indeterminate-mark-width", pixels(tokens.checkboxIndeterminateMarkWidth()));
        appendDeclaration(builder, "-m3-indeterminate-mark-height", pixels(tokens.checkboxIndeterminateMarkHeight()));
        endRule(builder);

        beginRule(builder, ".m3-radio-button");
        appendDeclaration(builder, "-m3-container-size", pixels(tokens.radioContainerSize()));
        appendDeclaration(builder, "-m3-selected-dot-size", pixels(tokens.radioSelectedDotSize()));
        endRule(builder);
    }

    /// Appends a switch token CSS rule.
    private static void appendSwitchRule(StringBuilder builder, SelectionTokens tokens) {
        beginRule(builder, ".m3-switch");
        appendDeclaration(builder, "-m3-touch-target-size", pixels(tokens.switchTouchTargetSize()));
        appendDeclaration(builder, "-m3-track-shape", pixels(tokens.trackShape()));
        appendDeclaration(builder, "-m3-track-width", pixels(tokens.switchTrackWidth()));
        appendDeclaration(builder, "-m3-track-height", pixels(tokens.switchTrackHeight()));
        appendDeclaration(builder, "-m3-state-layer-size", pixels(tokens.switchStateLayerSize()));
        appendDeclaration(builder, "-m3-unselected-handle-size", pixels(tokens.switchUnselectedHandleSize()));
        appendDeclaration(builder, "-m3-with-icon-handle-size", pixels(tokens.switchWithIconHandleSize()));
        appendDeclaration(builder, "-m3-selected-handle-size", pixels(tokens.switchSelectedHandleSize()));
        appendDeclaration(builder, "-m3-pressed-handle-size", pixels(tokens.switchPressedHandleSize()));
        appendDeclaration(builder, "-m3-icon-size", pixels(tokens.switchIconSize()));
        endRule(builder);
    }

    /// Appends a switch box shape CSS rule.
    private static void appendSwitchBoxRule(StringBuilder builder, SelectionTokens tokens) {
        String radius = pixels(tokens.trackShape());
        beginRule(builder, ".m3-switch .box");
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends common and size-specific slider token CSS rules.
    private static void appendSliderRules(StringBuilder builder, SliderTokens tokens) {
        beginRule(builder, ".m3-slider");
        appendDeclaration(builder, "-m3-stop-indicator-size", pixels(tokens.stopIndicatorSize()));
        appendDeclaration(
                builder,
                "-m3-stop-indicator-trailing-space",
                pixels(tokens.stopIndicatorTrailingSpace())
        );
        appendDeclaration(builder, "-m3-thumb-width", pixels(tokens.thumbWidth()));
        appendDeclaration(builder, "-m3-focused-thumb-width", pixels(tokens.focusedThumbWidth()));
        appendDeclaration(builder, "-m3-pressed-thumb-width", pixels(tokens.pressedThumbWidth()));
        appendDeclaration(builder, "-m3-thumb-track-gap", pixels(tokens.thumbTrackGap()));
        appendDeclaration(builder, "-m3-touch-target-size", pixels(tokens.touchTargetSize()));
        endRule(builder);

        beginRule(builder, ".m3-slider:focus-visible");
        appendDeclaration(builder, "-m3-thumb-width", pixels(tokens.focusedThumbWidth()));
        endRule(builder);

        beginRule(builder, ".m3-slider:pressed");
        appendDeclaration(builder, "-m3-thumb-width", pixels(tokens.pressedThumbWidth()));
        endRule(builder);

        SliderSizingTokens sizing = tokens.sizing();
        appendSliderSizeRule(builder, ".m3-slider-extra-small", sizing.extraSmall());
        appendSliderSizeRule(builder, ".m3-slider-small", sizing.small());
        appendSliderSizeRule(builder, ".m3-slider-medium", sizing.medium());
        appendSliderSizeRule(builder, ".m3-slider-large", sizing.large());
        appendSliderSizeRule(builder, ".m3-slider-extra-large", sizing.extraLarge());
    }

    /// Appends one Material slider size token rule.
    private static void appendSliderSizeRule(
            StringBuilder builder,
            String selector,
            SliderSizeTokens tokens
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-track-thickness", pixels(tokens.trackThickness()));
        appendDeclaration(builder, "-m3-track-shape", pixels(tokens.trackShape()));
        appendDeclaration(builder, "-m3-thumb-size", pixels(tokens.thumbSize()));
        appendDeclaration(builder, "-m3-slider-icon-size", pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-slider-icon-padding", pixels(tokens.iconPadding()));
        endRule(builder);
    }

    /// Appends a chip token CSS rule.
    private static void appendChipRule(StringBuilder builder, ChipTokens tokens) {
        beginRule(builder, ".m3-chip");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-icon-horizontal-padding", pixels(tokens.iconHorizontalPadding()));
        appendDeclaration(builder, "-m3-chip-element-spacing", pixels(tokens.elementSpacing()));
        appendDeclaration(builder, "-m3-chip-icon-size", pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-chip-avatar-size", pixels(tokens.avatarSize()));
        appendDeclaration(builder, "-m3-chip-avatar-shape", pixels(tokens.avatarShape()));
        appendDeclaration(builder, "-m3-chip-outline-width", pixels(tokens.outlineWidth()));
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-width", pixels(tokens.outlineWidth()));
        appendDeclaration(builder, "-fx-graphic-text-gap", pixels(tokens.elementSpacing()));
        endRule(builder);
    }

    /// Appends a generated chip group rule.
    private static void appendChipGroupRule(StringBuilder builder, ChipTokens tokens) {
        beginRule(builder, ".m3-chip-group");
        appendDeclaration(builder, "-m3-chip-group-horizontal-gap", pixels(tokens.groupHorizontalGap()));
        appendDeclaration(builder, "-m3-chip-group-vertical-gap", pixels(tokens.groupVerticalGap()));
        endRule(builder);
    }

    /// Appends a progress bar token CSS rule.
    private static void appendProgressBarRule(StringBuilder builder, ProgressTokens tokens) {
        beginRule(builder, ".m3-progress-bar");
        appendDeclaration(builder, "-m3-track-thickness", pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-track-shape", pixels(tokens.shape()));
        appendDeclaration(builder, "-m3-wave-amplitude", pixels(0.0));
        appendDeclaration(builder, "-m3-wavelength", pixels(tokens.linearWavelength()));
        appendDeclaration(
                builder,
                "-m3-indeterminate-wavelength",
                pixels(tokens.linearIndeterminateWavelength())
        );
        appendDeclaration(builder, "-m3-track-gap", pixels(tokens.linearTrackGap()));
        appendDeclaration(builder, "-m3-stop-size", pixels(tokens.linearStopSize()));
        endRule(builder);
    }

    /// Appends a progress bar track visual CSS rule.
    private static void appendProgressBarTrackRule(StringBuilder builder, String selector, ProgressTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-arc-width", pixels(progressTrackRadius(tokens) * 2.0));
        appendDeclaration(builder, "-fx-arc-height", pixels(progressTrackRadius(tokens) * 2.0));
        endRule(builder);
    }

    /// Returns a cleanly renderable progress track radius for the current thickness.
    private static double progressTrackRadius(ProgressTokens tokens) {
        return Math.min(tokens.shape(), tokens.thickness() / 2.0);
    }

    /// Appends a progress indicator token CSS rule.
    private static void appendProgressIndicatorRule(StringBuilder builder, ProgressTokens tokens) {
        beginRule(builder, ".m3-progress-indicator");
        appendDeclaration(builder, "-m3-track-thickness", pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-indicator-size", pixels(tokens.indicatorSize()));
        appendDeclaration(builder, "-m3-wave-indicator-size", pixels(tokens.waveIndicatorSize()));
        appendDeclaration(builder, "-m3-wave-amplitude", pixels(0.0));
        appendDeclaration(builder, "-m3-wavelength", pixels(tokens.circularWavelength()));
        appendDeclaration(builder, "-m3-track-gap", pixels(tokens.circularTrackGap()));
        endRule(builder);
    }

    /// Appends a loading indicator token CSS rule.
    private static void appendLoadingIndicatorRule(
            StringBuilder builder,
            LoadingIndicatorTokens tokens
    ) {
        beginRule(builder, ".m3-loading-indicator");
        appendDeclaration(builder, "-m3-container-size", pixels(tokens.containerSize()));
        appendDeclaration(builder, "-m3-indicator-size", pixels(tokens.indicatorSize()));
        endRule(builder);
    }

    /// Appends a surface token CSS rule.
    private static void appendSurfaceRule(StringBuilder builder, SurfaceTokens tokens) {
        beginRule(builder, ".m3-surface");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a carousel track token CSS rule.
    private static void appendCarouselTrackRule(StringBuilder builder, CarouselTokens tokens) {
        String verticalPadding = pixels(tokens.trackVerticalPadding());
        String horizontalPadding = pixels(tokens.trackHorizontalPadding());
        beginRule(builder, ".m3-carousel-track");
        appendDeclaration(
                builder,
                "-fx-padding",
                verticalPadding + " " + horizontalPadding + " " + verticalPadding + " " + horizontalPadding
        );
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.itemSpacing()));
        appendDeclaration(builder, "-m3-carousel-item-mask-shape", pixels(tokens.itemShape()));
        appendDeclaration(
                builder,
                "-m3-carousel-small-item-min-width",
                pixels(tokens.smallItemMinWidth())
        );
        appendDeclaration(
                builder,
                "-m3-carousel-small-item-max-width",
                pixels(tokens.smallItemMaxWidth())
        );
        appendDeclaration(
                builder,
                "-m3-carousel-large-item-max-width",
                pixels(tokens.largeItemMaxWidth())
        );
        endRule(builder);

        beginRule(builder, ".m3-carousel .m3-card");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.itemShape()));
        endRule(builder);
    }

    /// Appends a dialog pane token CSS rule.
    private static void appendDialogRule(StringBuilder builder, DialogTokens tokens) {
        beginRule(builder, ".m3-dialog-pane");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-container-min-width", pixels(tokens.containerMinWidth()));
        appendDeclaration(builder, "-m3-container-max-width", pixels(tokens.containerMaxWidth()));
        appendDeclaration(builder, "-m3-action-spacing", pixels(tokens.actionSpacing()));
        appendDeclaration(builder, "-m3-dialog-icon-size", pixels(tokens.iconSize()));
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a card token CSS rule.
    private static void appendCardRule(StringBuilder builder, CardTokens tokens) {
        beginRule(builder, ".m3-card");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-outline-width", pixels(tokens.outlineWidth()));
        endRule(builder);
    }

    /// Appends a snackbar token CSS rule.
    private static void appendSnackbarRule(StringBuilder builder, SnackbarTokens tokens) {
        beginRule(builder, ".m3-snackbar-presenter");
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-container-min-width", pixels(tokens.containerMinWidth()));
        appendDeclaration(builder, "-m3-container-max-width", pixels(tokens.containerMaxWidth()));
        appendDeclaration(
                builder,
                "-m3-single-line-container-height",
                pixels(tokens.singleLineContainerHeight())
        );
        appendDeclaration(
                builder,
                "-m3-two-line-container-height",
                pixels(tokens.twoLineContainerHeight())
        );
        appendDeclaration(builder, "-m3-action-container-height", pixels(tokens.actionContainerHeight()));
        endRule(builder);

        beginRule(builder, ".m3-snackbar-presenter .m3-snackbar-action");
        appendDeclaration(builder, "-m3-container-height", "-m3-action-container-height");
        endRule(builder);
    }

    /// Appends a banner token CSS rule.
    private static void appendBannerRule(StringBuilder builder, BannerTokens tokens) {
        beginRule(builder, ".m3-banner");
        appendDeclaration(builder, "-m3-container-min-height", pixels(tokens.containerMinHeight()));
        appendDeclaration(builder, "-m3-vertical-padding", pixels(tokens.verticalPadding()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.contentSpacing()));
        appendDeclaration(builder, "-m3-action-spacing", pixels(tokens.actionSpacing()));
        endRule(builder);
    }

    /// Appends a plain tooltip token CSS rule.
    private static void appendTooltipRule(StringBuilder builder, TooltipTokens tokens) {
        String verticalPadding = pixels(tokens.plainVerticalPadding());
        String horizontalPadding = pixels(tokens.plainHorizontalPadding());
        beginRule(builder, ".m3-tooltip");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.plainContainerShape()));
        appendDeclaration(builder, "-fx-padding", verticalPadding + " " + horizontalPadding);
        endRule(builder);
    }

    /// Appends a rich tooltip container token CSS rule.
    private static void appendRichTooltipRule(StringBuilder builder, TooltipTokens tokens) {
        String horizontalPadding = pixels(tokens.richHorizontalPadding());
        String padding = pixels(tokens.richTopPadding())
                + " "
                + horizontalPadding
                + " "
                + pixels(tokens.richBottomPadding())
                + " "
                + horizontalPadding;
        beginRule(builder, ".m3-rich-tooltip-container");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.richContainerShape()));
        appendDeclaration(builder, "-fx-padding", padding);
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.richContentSpacing()));
        appendDeclaration(builder, "-fx-pref-width", pixels(tokens.richPreferredWidth()));
        endRule(builder);
    }

    /// Appends a rich tooltip actions token CSS rule.
    private static void appendRichTooltipActionsRule(StringBuilder builder, TooltipTokens tokens) {
        beginRule(builder, ".m3-rich-tooltip-actions");
        appendDeclaration(builder, "-fx-spacing", pixels(tokens.richActionSpacing()));
        endRule(builder);
    }

    /// Appends a rich tooltip action button token CSS rule.
    private static void appendRichTooltipActionButtonRule(StringBuilder builder, TooltipTokens tokens) {
        beginRule(builder, ".m3-rich-tooltip-actions .m3-button");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.richActionButtonHeight()));
        appendDeclaration(
                builder,
                "-m3-horizontal-padding",
                pixels(tokens.richActionButtonHorizontalPadding())
        );
        endRule(builder);
    }

    /// Appends a divider token CSS rule.
    private static void appendDividerRule(StringBuilder builder, DividerTokens tokens) {
        beginRule(builder, ".m3-divider");
        appendDeclaration(builder, "-m3-thickness", pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-inset-start", pixels(tokens.insetStart()));
        appendDeclaration(builder, "-m3-inset-end", pixels(tokens.insetEnd()));
        endRule(builder);
    }

    /// Appends a badge token CSS rule.
    private static void appendBadgeRule(StringBuilder builder, BadgeTokens tokens) {
        beginRule(builder, ".m3-badge");
        appendDeclaration(builder, "-m3-small-size", pixels(tokens.smallSize()));
        appendDeclaration(builder, "-m3-large-height", pixels(tokens.largeHeight()));
        appendDeclaration(builder, "-m3-large-min-width", pixels(tokens.largeMinWidth()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends an avatar token CSS rule.
    private static void appendAvatarRule(StringBuilder builder, AvatarTokens tokens) {
        String size = pixels(tokens.containerSize());
        beginRule(builder, ".m3-avatar.m3-avatar");
        appendDeclaration(builder, "-m3-container-size", size);
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a top app bar token CSS rule.
    private static void appendTopAppBarRule(StringBuilder builder, TopAppBarTokens tokens) {
        beginRule(builder, ".m3-top-app-bar");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-medium-container-height", pixels(tokens.mediumContainerHeight()));
        appendDeclaration(builder, "-m3-large-container-height", pixels(tokens.largeContainerHeight()));
        appendDeclaration(builder, "-m3-medium-flexible-container-height",
                pixels(tokens.mediumFlexibleContainerHeight()));
        appendDeclaration(builder, "-m3-medium-flexible-subtitle-container-height",
                pixels(tokens.mediumFlexibleSubtitleContainerHeight()));
        appendDeclaration(builder, "-m3-large-flexible-container-height",
                pixels(tokens.largeFlexibleContainerHeight()));
        appendDeclaration(builder, "-m3-large-flexible-subtitle-container-height",
                pixels(tokens.largeFlexibleSubtitleContainerHeight()));
        appendDeclaration(builder, "-m3-edge-padding", pixels(tokens.edgePadding()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-medium-bottom-padding", pixels(tokens.mediumBottomPadding()));
        appendDeclaration(builder, "-m3-large-bottom-padding", pixels(tokens.largeBottomPadding()));
        appendDeclaration(builder, "-m3-flexible-bottom-padding", pixels(tokens.flexibleBottomPadding()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.contentSpacing()));
        appendDeclaration(builder, "-m3-action-spacing", pixels(tokens.actionSpacing()));
        appendDeclaration(builder, "-fx-padding", "0");
        endRule(builder);
    }

    /// Appends a bottom app bar token CSS rule.
    private static void appendBottomAppBarRule(StringBuilder builder, BottomAppBarTokens tokens) {
        beginRule(builder, ".m3-bottom-app-bar");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.contentSpacing()));
        appendDeclaration(builder, "-m3-action-spacing", pixels(tokens.actionSpacing()));
        appendDeclaration(builder, "-fx-min-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a toolbar token CSS rule.
    private static void appendToolbarRule(StringBuilder builder, ToolbarTokens tokens) {
        beginRule(builder, ".m3-toolbar");
        appendDeclaration(builder, "-m3-toolbar-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-toolbar-container-width", pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-m3-toolbar-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-toolbar-item-slot-size", pixels(tokens.itemSlotSize()));
        appendDeclaration(builder, "-m3-toolbar-content-padding", pixels(tokens.contentPadding()));
        appendDeclaration(
                builder,
                "-m3-toolbar-docked-content-padding",
                pixels(tokens.dockedContentPadding())
        );
        appendDeclaration(builder, "-m3-toolbar-item-spacing", pixels(tokens.itemSpacing()));
        appendDeclaration(
                builder,
                "-m3-toolbar-docked-max-item-spacing",
                pixels(tokens.dockedMaxItemSpacing())
        );
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-container-width", pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-item-slot-size", pixels(tokens.itemSlotSize()));
        appendDeclaration(builder, "-m3-content-padding", pixels(tokens.contentPadding()));
        appendDeclaration(
                builder,
                "-m3-docked-content-padding",
                pixels(tokens.dockedContentPadding())
        );
        appendDeclaration(builder, "-m3-item-spacing", pixels(tokens.itemSpacing()));
        appendDeclaration(
                builder,
                "-m3-docked-max-item-spacing",
                pixels(tokens.dockedMaxItemSpacing())
        );
        endRule(builder);

        beginRule(builder, ".m3-toolbar.m3-toolbar-floating");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.containerShape()));
        endRule(builder);

        beginRule(builder, ".m3-toolbar.m3-toolbar-docked");
        appendDeclaration(
                builder,
                "-m3-docked-content-padding",
                pixels(tokens.dockedContentPadding())
        );
        endRule(builder);
    }

    /// Appends a navigation bar token CSS rule.
    private static void appendNavigationBarRule(StringBuilder builder, NavigationBarTokens tokens) {
        beginRule(builder, ".m3-navigation-bar");
        appendDeclaration(builder, "-fx-min-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-item-spacing", pixels(tokens.itemSpacing()));
        endRule(builder);

        double densityScale = tokens.indicatorHeight() / 32.0;
        beginRule(builder, ".m3-navigation-bar.m3-navigation-bar-horizontal");
        appendDeclaration(builder, "-fx-min-height", pixels(64.0 * densityScale));
        appendDeclaration(builder, "-fx-pref-height", pixels(64.0 * densityScale));
        appendDeclaration(builder, "-m3-item-spacing", pixels(0.0));
        endRule(builder);
    }

    /// Appends a navigation item token CSS rule.
    private static void appendNavigationItemRule(StringBuilder builder, NavigationBarTokens tokens) {
        beginRule(builder, ".m3-navigation-item");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-item-width", pixels(tokens.itemWidth()));
        appendDeclaration(builder, "-m3-indicator-width", pixels(tokens.indicatorWidth()));
        appendDeclaration(builder, "-m3-indicator-height", pixels(tokens.indicatorHeight()));
        appendDeclaration(builder, "-m3-indicator-shape", pixels(tokens.indicatorShape()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.contentSpacing()));
        endRule(builder);

        double densityScale = tokens.indicatorHeight() / 32.0;
        beginRule(builder, ".m3-navigation-bar .m3-navigation-item-horizontal");
        appendDeclaration(builder, "-m3-container-height", pixels(64.0 * densityScale));
        appendDeclaration(builder, "-m3-item-width", pixels(160.0 * densityScale));
        appendDeclaration(builder, "-m3-indicator-width", pixels(64.0 * densityScale));
        appendDeclaration(builder, "-m3-indicator-height", pixels(40.0 * densityScale));
        appendDeclaration(builder, "-m3-indicator-shape", pixels(20.0 * densityScale));
        appendDeclaration(builder, "-m3-content-spacing", pixels(4.0 * densityScale));
        endRule(builder);
    }

    /// Appends a navigation selected indicator token CSS rule.
    private static void appendNavigationIndicatorRule(StringBuilder builder, NavigationBarTokens tokens) {
        beginRule(builder, ".m3-navigation-item-indicator");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.indicatorShape()));
        endRule(builder);
    }

    /// Appends a navigation rail token CSS rule.
    private static void appendNavigationRailRule(StringBuilder builder, NavigationRailTokens tokens) {
        beginRule(builder, ".m3-navigation-rail");
        appendDeclaration(
                builder,
                "-m3-collapsed-container-width",
                pixels(tokens.collapsedContainerWidth())
        );
        appendDeclaration(
                builder,
                "-m3-expanded-minimum-container-width",
                pixels(tokens.expandedMinimumContainerWidth())
        );
        appendDeclaration(
                builder,
                "-m3-expanded-container-width",
                pixels(tokens.expandedContainerWidth())
        );
        appendDeclaration(
                builder,
                "-m3-expanded-maximum-container-width",
                pixels(tokens.expandedMaximumContainerWidth())
        );
        appendDeclaration(
                builder,
                "-fx-padding",
                pixels(tokens.collapsedTopPadding())
                        + " "
                        + pixels(tokens.horizontalPadding())
                        + " "
                        + pixels(tokens.collapsedBottomPadding())
                        + " "
                        + pixels(tokens.horizontalPadding())
        );
        appendDeclaration(builder, "-m3-item-spacing", pixels(tokens.itemSpacing()));
        appendDeclaration(builder, "-m3-header-spacing", pixels(tokens.headerSpacing()));
        endRule(builder);

        beginRule(builder, ".m3-navigation-rail:narrow");
        appendDeclaration(
                builder,
                "-m3-collapsed-container-width",
                pixels(tokens.narrowCollapsedContainerWidth())
        );
        endRule(builder);

        beginRule(builder, ".m3-navigation-rail:expanded");
        appendDeclaration(
                builder,
                "-fx-padding",
                pixels(tokens.expandedTopPadding())
                        + " "
                        + pixels(tokens.horizontalPadding())
                        + " "
                        + pixels(tokens.expandedBottomPadding())
                        + " "
                        + pixels(tokens.horizontalPadding())
        );
        appendDeclaration(builder, "-m3-item-spacing", pixels(0.0));
        endRule(builder);

        beginRule(builder, ".m3-navigation-rail:expanded:modal");
        appendDeclaration(
                builder,
                "-fx-background-radius",
                pixels(tokens.modalContainerShape())
        );
        endRule(builder);
    }

    /// Appends a navigation rail item token CSS rule.
    private static void appendNavigationRailItemRule(StringBuilder builder, NavigationRailTokens tokens) {
        beginRule(builder, ".m3-navigation-rail .m3-navigation-item");
        appendDeclaration(builder, "-m3-container-height", pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-item-width", pixels(tokens.itemWidth()));
        appendDeclaration(builder, "-m3-indicator-width", pixels(tokens.indicatorWidth()));
        appendDeclaration(builder, "-m3-indicator-height", pixels(tokens.indicatorHeight()));
        appendDeclaration(builder, "-m3-indicator-shape", pixels(tokens.indicatorShape()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.contentSpacing()));
        endRule(builder);

        double densityScale = tokens.indicatorHeight() / 32.0;
        beginRule(builder, ".m3-navigation-rail:expanded .m3-navigation-item-horizontal");
        appendDeclaration(builder, "-m3-container-height", pixels(64.0 * densityScale));
        appendDeclaration(
                builder,
                "-m3-item-width",
                pixels(Math.max(
                        0.0,
                        tokens.expandedContainerWidth() - 2.0 * tokens.horizontalPadding()
                ))
        );
        appendDeclaration(builder, "-m3-indicator-width", pixels(56.0 * densityScale));
        appendDeclaration(builder, "-m3-indicator-height", pixels(56.0 * densityScale));
        appendDeclaration(builder, "-m3-indicator-shape", pixels(28.0 * densityScale));
        appendDeclaration(builder, "-m3-content-spacing", pixels(8.0 * densityScale));
        endRule(builder);
    }

    /// Appends a navigation rail selected indicator token CSS rule.
    private static void appendNavigationRailIndicatorRule(StringBuilder builder, NavigationRailTokens tokens) {
        beginRule(builder, ".m3-navigation-rail .m3-navigation-item-indicator");
        appendDeclaration(builder, "-fx-background-radius", pixels(tokens.indicatorShape()));
        endRule(builder);
    }

    /// Appends a navigation drawer token CSS rule.
    private static void appendNavigationDrawerRule(
            StringBuilder builder,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, ".m3-navigation-drawer");
        appendDeclaration(builder, "-fx-min-width", pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-pref-width", pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-padding", pixels(tokens.containerPadding()));
        appendDeclaration(builder, "-m3-item-spacing", pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a navigation drawer item token CSS rule.
    private static void appendNavigationDrawerItemRule(
            StringBuilder builder,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, ".m3-navigation-drawer .m3-list-item");
        appendDeclaration(builder, "-m3-one-line-height", pixels(tokens.oneLineItemHeight()));
        appendDeclaration(builder, "-m3-two-line-height", pixels(tokens.twoLineItemHeight()));
        appendDeclaration(builder, "-m3-three-line-height", pixels(tokens.threeLineItemHeight()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.itemContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.itemHorizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", pixels(tokens.itemVerticalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.itemContentSpacing()));
        endRule(builder);
    }

    /// Appends a navigation drawer child group item token CSS rule.
    private static void appendNavigationDrawerGroupChildItemRule(
            StringBuilder builder,
            String selector,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-one-line-height", pixels(tokens.groupChildItemHeight()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.groupChildItemContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.groupChildItemHorizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", pixels(tokens.itemVerticalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.itemContentSpacing()));
        endRule(builder);
    }

    /// Appends a list item token CSS rule.
    private static void appendListItemRule(StringBuilder builder, ListItemTokens tokens) {
        beginRule(builder, ".m3-list-item");
        appendDeclaration(builder, "-m3-one-line-height", pixels(tokens.oneLineHeight()));
        appendDeclaration(builder, "-m3-two-line-height", pixels(tokens.twoLineHeight()));
        appendDeclaration(builder, "-m3-three-line-height", pixels(tokens.threeLineHeight()));
        appendDeclaration(builder, "-m3-container-shape", pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", pixels(tokens.verticalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends the gap and profile-specific shapes used by static and virtualized segmented lists.
    private static void appendSegmentedListRule(
            StringBuilder builder,
            ListItemTokens tokens,
            boolean expressive
    ) {
        beginRule(builder, ".m3-list-pane.m3-segmented-list,\n.m3-list-view.m3-segmented-list");
        appendDeclaration(builder, "-m3-list-item-spacing", pixels(tokens.segmentedGap()));
        endRule(builder);

        if (!expressive) {
            return;
        }

        beginRule(builder, ".m3-segmented-list .m3-list-item");
        appendDeclaration(builder, "-m3-container-shape", "-m3-shape-corner-extra-small");
        endRule(builder);

        beginRule(builder, ".m3-segmented-list .m3-list-item:hover");
        appendDeclaration(builder, "-m3-container-shape", "-m3-shape-corner-medium");
        endRule(builder);

        beginRule(builder, ".m3-segmented-list .m3-list-item:disabled");
        appendDeclaration(builder, "-m3-container-shape", "-m3-shape-corner-extra-small");
        endRule(builder);

        beginRule(
                builder,
                ".m3-segmented-list .m3-list-item:focus-visible, "
                        + ".m3-segmented-list .m3-list-item:pressed, "
                        + ".m3-segmented-list .m3-list-item:armed, "
                        + ".m3-segmented-list .m3-list-item:selected, "
                        + ".m3-segmented-list .m3-list-item:selected:disabled"
        );
        appendDeclaration(builder, "-m3-container-shape", "-m3-shape-corner-large");
        endRule(builder);
    }

    /// Appends a list section header token CSS rule.
    private static void appendListSectionHeaderRule(StringBuilder builder, ListItemTokens tokens) {
        String height = pixels(tokens.sectionHeaderHeight());
        String horizontalPadding = pixels(tokens.sectionHeaderHorizontalPadding());
        beginRule(builder, ".m3-list-section-header");
        appendDeclaration(builder, "-fx-min-height", height);
        appendDeclaration(builder, "-fx-pref-height", height);
        appendDeclaration(builder, "-fx-padding", "0px " + horizontalPadding + " 0px " + horizontalPadding);
        endRule(builder);
    }

    /// Appends a CSS rule header.
    private static void beginRule(StringBuilder builder, String selector) {
        builder.append(selector).append(" {\n");
    }

    /// Appends a CSS declaration line.
    private static void appendDeclaration(StringBuilder builder, String name, String value) {
        builder.append("    ").append(name).append(": ").append(value).append(";\n");
    }

    /// Appends a CSS rule footer.
    private static void endRule(StringBuilder builder) {
        builder.append("}\n\n");
    }


    /// Formats a decimal number with stable locale-independent output.
    private static String format(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /// Formats a decimal number as a pixel value.
    private static String pixels(double value) {
        return format(value) + "px";
    }

    /// Appends an inline CSS declaration.
    private static void append(StringBuilder builder, String name, String value) {
        builder.append(name).append(": ").append(value).append("; ");
    }
}
