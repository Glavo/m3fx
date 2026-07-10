// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ComponentTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds component-level Material Design 3 tokens used by M3FX controls.
///
/// Component tokens collect the shape, size, padding, and metric defaults that individual controls consume.
/// They keep component geometry separate from hard-coded CSS values and allow a theme profile, such as baseline
/// or expressive, to change component behavior consistently.
///
/// See [Material Design components](https://m3.material.io/components) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public sealed interface M3ComponentTokens permits M3ComponentTokensImpl {
    /// Returns tokens used by filled buttons.
    ///
    /// @return the filled button component tokens
    ButtonTokens filledButton();

    /// Returns tokens used by filled tonal buttons.
    ///
    /// @return the filled tonal button component tokens
    ButtonTokens tonalButton();

    /// Returns tokens used by outlined buttons.
    ///
    /// @return the outlined button component tokens
    ButtonTokens outlinedButton();

    /// Returns tokens used by text buttons.
    ///
    /// @return the text button component tokens
    ButtonTokens textButton();

    /// Returns tokens used by elevated buttons.
    ///
    /// @return the elevated button component tokens
    ButtonTokens elevatedButton();

    /// Returns the shared Material button size tokens.
    ///
    /// @return the five-step button size token scale
    ButtonSizingTokens buttonSizing();

    /// Returns tokens used by icon buttons.
    ///
    /// @return the icon button component tokens
    IconButtonTokens iconButton();

    /// Returns tokens used by floating action buttons.
    ///
    /// @return the floating action button component tokens
    FabTokens floatingActionButton();

    /// Returns tokens used by icon glyph primitives.
    ///
    /// @return the icon component tokens
    IconTokens icon();

    /// Returns tokens used by button groups.
    ///
    /// @return the button group component tokens
    ButtonGroupTokens buttonGroup();

    /// Returns tokens used by split buttons.
    ///
    /// @return the split button component tokens
    SplitButtonTokens splitButton();

    /// Returns tokens used by segmented buttons.
    ///
    /// @return the segmented button component tokens
    ButtonTokens segmentedButton();

    /// Returns tokens used by tabs.
    ///
    /// @return the tab component tokens
    TabTokens tab();

    /// Returns tokens used by text input controls.
    ///
    /// @return the text input component tokens
    FieldTokens field();

    /// Returns tokens used by text area controls.
    ///
    /// @return the text area component tokens
    TextAreaTokens textArea();

    /// Returns tokens used by form containers.
    ///
    /// @return the form component tokens
    FormTokens form();

    /// Returns tokens used by validation summaries.
    ///
    /// @return the validation summary component tokens
    ValidationSummaryTokens validationSummary();

    /// Returns tokens used by menus.
    ///
    /// @return the menu component tokens
    MenuTokens menu();

    /// Returns tokens used by search components.
    ///
    /// @return the search component tokens
    SearchTokens search();

    /// Returns tokens used by picker fields.
    ///
    /// @return the picker field component tokens
    PickerFieldTokens pickerField();

    /// Returns tokens used by date pickers.
    ///
    /// @return the date picker component tokens
    DatePickerTokens datePicker();

    /// Returns tokens used by time pickers.
    ///
    /// @return the time picker component tokens
    TimePickerTokens timePicker();

    /// Returns tokens used by sheet containers.
    ///
    /// @return the sheet component tokens
    SheetTokens sheet();

    /// Returns tokens used by scrims.
    ///
    /// @return the scrim component tokens
    ScrimTokens scrim();

    /// Returns tokens used by selection controls.
    ///
    /// @return the selection control component tokens
    SelectionTokens selection();

    /// Returns tokens used by sliders.
    ///
    /// @return the slider component tokens
    SliderTokens slider();

    /// Returns tokens used by chips.
    ///
    /// @return the chip component tokens
    ChipTokens chip();

    /// Returns tokens used by progress controls.
    ///
    /// @return the progress component tokens
    ProgressTokens progress();

    /// Returns tokens used by loading indicators.
    ///
    /// @return the loading indicator component tokens
    LoadingIndicatorTokens loadingIndicator();

    /// Returns tokens used by surfaces.
    ///
    /// @return the surface component tokens
    SurfaceTokens surface();

    /// Returns tokens used by carousels.
    ///
    /// @return the carousel component tokens
    CarouselTokens carousel();

    /// Returns tokens used by cards.
    ///
    /// @return the card component tokens
    CardTokens card();

    /// Returns tokens used by dialogs.
    ///
    /// @return the dialog component tokens
    DialogTokens dialog();

    /// Returns tokens used by snackbar controls.
    ///
    /// @return the snackbar component tokens
    SnackbarTokens snackbar();

    /// Returns tokens used by banner controls.
    ///
    /// @return the banner component tokens
    BannerTokens banner();

    /// Returns tokens used by tooltip controls.
    ///
    /// @return the tooltip component tokens
    TooltipTokens tooltip();

    /// Returns tokens used by dividers.
    ///
    /// @return the divider component tokens
    DividerTokens divider();

    /// Returns tokens used by badges.
    ///
    /// @return the badge component tokens
    BadgeTokens badge();

    /// Returns tokens used by avatars.
    ///
    /// @return the avatar component tokens
    AvatarTokens avatar();

    /// Returns tokens used by top app bars.
    ///
    /// @return the top app bar component tokens
    TopAppBarTokens topAppBar();

    /// Returns tokens used by bottom app bars.
    ///
    /// @return the bottom app bar component tokens
    BottomAppBarTokens bottomAppBar();

    /// Returns tokens used by toolbars.
    ///
    /// @return the toolbar component tokens
    ToolbarTokens toolbar();

    /// Returns tokens used by navigation bars.
    ///
    /// @return the navigation bar component tokens
    NavigationBarTokens navigationBar();

    /// Returns tokens used by navigation rails.
    ///
    /// @return the navigation rail component tokens
    NavigationRailTokens navigationRail();

    /// Returns tokens used by navigation drawers.
    ///
    /// @return the navigation drawer component tokens
    NavigationDrawerTokens navigationDrawer();

    /// Returns tokens used by list items.
    ///
    /// @return the list item component tokens
    ListItemTokens listItem();

    /// Creates component tokens from explicit component token values.
    ///
    /// @param filledButton the filled button component tokens
    /// @param tonalButton the filled tonal button component tokens
    /// @param outlinedButton the outlined button component tokens
    /// @param textButton the text button component tokens
    /// @param elevatedButton the elevated button component tokens
    /// @param buttonSizing the five-step button size token scale
    /// @param iconButton the icon button component tokens
    /// @param floatingActionButton the floating action button component tokens
    /// @param icon the icon component tokens
    /// @param buttonGroup the button group component tokens
    /// @param splitButton the split button component tokens
    /// @param segmentedButton the segmented button component tokens
    /// @param tab the tab component tokens
    /// @param field the text input component tokens
    /// @param textArea the text area component tokens
    /// @param form the form component tokens
    /// @param validationSummary the validation summary component tokens
    /// @param menu the menu component tokens
    /// @param search the search component tokens
    /// @param pickerField the picker field component tokens
    /// @param datePicker the date picker component tokens
    /// @param timePicker the time picker component tokens
    /// @param sheet the sheet component tokens
    /// @param scrim the scrim component tokens
    /// @param selection the selection control component tokens
    /// @param slider the slider component tokens
    /// @param chip the chip component tokens
    /// @param progress the progress component tokens
    /// @param loadingIndicator the loading indicator component tokens
    /// @param surface the surface component tokens
    /// @param carousel the carousel component tokens
    /// @param card the card component tokens
    /// @param dialog the dialog component tokens
    /// @param snackbar the snackbar component tokens
    /// @param banner the banner component tokens
    /// @param tooltip the tooltip component tokens
    /// @param divider the divider component tokens
    /// @param badge the badge component tokens
    /// @param avatar the avatar component tokens
    /// @param topAppBar the top app bar component tokens
    /// @param bottomAppBar the bottom app bar component tokens
    /// @param toolbar the toolbar component tokens
    /// @param navigationBar the navigation bar component tokens
    /// @param navigationRail the navigation rail component tokens
    /// @param navigationDrawer the navigation drawer component tokens
    /// @param listItem the list item component tokens
    /// @return a component token set containing the supplied values
    static M3ComponentTokens create(
            ButtonTokens filledButton,
            ButtonTokens tonalButton,
            ButtonTokens outlinedButton,
            ButtonTokens textButton,
            ButtonTokens elevatedButton,
            ButtonSizingTokens buttonSizing,
            IconButtonTokens iconButton,
            FabTokens floatingActionButton,
            IconTokens icon,
            ButtonGroupTokens buttonGroup,
            SplitButtonTokens splitButton,
            ButtonTokens segmentedButton,
            TabTokens tab,
            FieldTokens field,
            TextAreaTokens textArea,
            FormTokens form,
            ValidationSummaryTokens validationSummary,
            MenuTokens menu,
            SearchTokens search,
            PickerFieldTokens pickerField,
            DatePickerTokens datePicker,
            TimePickerTokens timePicker,
            SheetTokens sheet,
            ScrimTokens scrim,
            SelectionTokens selection,
            SliderTokens slider,
            ChipTokens chip,
            ProgressTokens progress,
            LoadingIndicatorTokens loadingIndicator,
            SurfaceTokens surface,
            CarouselTokens carousel,
            CardTokens card,
            DialogTokens dialog,
            SnackbarTokens snackbar,
            BannerTokens banner,
            TooltipTokens tooltip,
            DividerTokens divider,
            BadgeTokens badge,
            AvatarTokens avatar,
            TopAppBarTokens topAppBar,
            BottomAppBarTokens bottomAppBar,
            ToolbarTokens toolbar,
            NavigationBarTokens navigationBar,
            NavigationRailTokens navigationRail,
            NavigationDrawerTokens navigationDrawer,
            ListItemTokens listItem
    ) {
        return new M3ComponentTokensImpl(
                filledButton,
                tonalButton,
                outlinedButton,
                textButton,
                elevatedButton,
                buttonSizing,
                iconButton,
                floatingActionButton,
                icon,
                buttonGroup,
                splitButton,
                segmentedButton,
                tab,
                field,
                textArea,
                form,
                validationSummary,
                menu,
                search,
                pickerField,
                datePicker,
                timePicker,
                sheet,
                scrim,
                selection,
                slider,
                chip,
                progress,
                loadingIndicator,
                surface,
                carousel,
                card,
                dialog,
                snackbar,
                banner,
                tooltip,
                divider,
                badge,
                avatar,
                topAppBar,
                bottomAppBar,
                toolbar,
                navigationBar,
                navigationRail,
                navigationDrawer,
                listItem
        );
    }

    /// Creates component tokens for a profile.
    ///
    /// @param profile the Material profile whose component metrics should be generated
    /// @param shapeTokens the shape scale used by generated component tokens
    /// @param density the density adjustment applied to generated component metrics
    /// @return a component token set generated from the supplied profile, shape scale, and density
    static M3ComponentTokens create(M3Profile profile, M3ShapeTokens shapeTokens, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(density, "density");

        double buttonHeight = density.apply(40.0);
        double iconSmallSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 20.0 : 18.0);
        double iconMediumSize = density.apply(24.0);
        double iconLargeSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 36.0 : 32.0);
        double iconExtraLargeSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 40.0);
        double fabSmallSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double fabRegularSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double fabLargeSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 104.0 : 96.0);
        double segmentedButtonHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double tabHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 56.0 : 48.0);
        double tabMinWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 104.0 : 90.0);
        double fieldHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double textAreaHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 128.0 : 112.0);
        double menuItemHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 48.0);
        double searchBarHeight = density.apply(56.0);
        double searchViewResultHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double pickerNavigationButtonSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double datePickerDayCellSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 40.0);
        double timePickerCellHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 40.0);
        double timePickerCellWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 44.0);
        double sideSheetWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 384.0 : 360.0);
        double bottomSheetHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 360.0 : 320.0);
        double chipHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 36.0 : 32.0);
        double badgeSmallSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 8.0 : 6.0);
        double badgeLargeHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 18.0 : 16.0);
        double badgeLargeMinWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 18.0 : 16.0);
        double avatarSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 40.0);
        double topAppBarHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 72.0 : 64.0);
        double topAppBarMediumHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 120.0 : 112.0);
        double topAppBarLargeHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 160.0 : 152.0);
        double bottomAppBarHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 88.0 : 80.0);
        double navigationBarHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 88.0 : 80.0);
        double navigationItemWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 96.0 : 80.0);
        double navigationIndicatorWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 72.0 : 64.0);
        double navigationIndicatorHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 36.0 : 32.0);
        double navigationRailWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 112.0 : 96.0);
        double navigationRailItemWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 96.0 : 80.0);
        double navigationRailIndicatorWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double navigationDrawerWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 384.0 : 360.0);
        double navigationDrawerOneLineItemHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double navigationDrawerTwoLineItemHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 80.0 : 72.0);
        double navigationDrawerThreeLineItemHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 96.0 : 88.0);
        double listItemOneLineHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double listItemTwoLineHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 80.0 : 72.0);
        double listItemThreeLineHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 96.0 : 88.0);
        double listSectionHeaderHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 56.0 : 48.0);
        double progressLinearWaveAmplitude = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 3.0 : 0.0);
        double progressCircularWaveAmplitude = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 1.6 : 0.0);
        double loadingIndicatorContainerSize = density.apply(48.0);
        double loadingIndicatorIndicatorSize = density.apply(38.0);
        double progressCircularIndicatorSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        boolean expressive = profile == M3Profile.EXPRESSIVE_2025;
        double navigationContentSpacing = density.apply(expressive ? 6.0 : 4.0);
        double navigationHorizontalPadding = density.apply(expressive ? 12.0 : 8.0);
        double navigationRailVerticalPadding = density.apply(expressive ? 20.0 : 16.0);
        double navigationRailHorizontalPadding = density.apply(expressive ? 12.0 : 8.0);
        double navigationRailItemSpacing = density.apply(expressive ? 12.0 : 8.0);
        double navigationDrawerContainerPadding = density.apply(expressive ? 16.0 : 12.0);
        double navigationDrawerItemHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double navigationDrawerItemContentSpacing = density.apply(expressive ? 16.0 : 12.0);
        double navigationDrawerItemSpacing = density.apply(expressive ? 6.0 : 4.0);
        double navigationDrawerGroupChildPadding = density.apply(expressive ? 40.0 : 32.0);
        double listItemHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double listItemVerticalPadding = density.apply(expressive ? 10.0 : 8.0);
        double listItemContentSpacing = density.apply(expressive ? 20.0 : 16.0);
        double listSectionHeaderHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double menuContainerShape = expressive ? shapeTokens.large() : shapeTokens.extraSmall();
        double menuContainerPadding = density.apply(expressive ? 10.0 : 8.0);
        double menuItemContainerShape = shapeTokens.extraSmall();
        double menuSelectedItemContainerShape = expressive ? shapeTokens.medium() : menuItemContainerShape;
        double menuFirstItemContainerShape = expressive ? shapeTokens.medium() : menuItemContainerShape;
        double menuLastItemContainerShape = expressive ? shapeTokens.medium() : menuItemContainerShape;
        double menuItemHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double menuItemContentSpacing = density.apply(12.0);
        double menuItemSpacing = density.apply(expressive ? 2.0 : 0.0);
        double searchBarHorizontalPadding = density.apply(expressive ? 24.0 : 16.0);
        double searchBarContentSpacing = density.apply(expressive ? 4.0 : 16.0);
        double searchBarTrailingActionsGap = density.apply(0.0);
        double searchViewContainerShape = expressive ? shapeTokens.extraLarge() : 28.0;
        double searchViewHorizontalPadding = density.apply(expressive ? 12.0 : 0.0);
        double searchViewBarResultsGap = density.apply(expressive ? 2.0 : 0.0);
        double searchViewResultsShape = expressive ? shapeTokens.medium() : 0.0;
        double searchResultContainerShape = expressive ? shapeTokens.medium() : 0.0;
        double searchViewResultPadding = density.apply(expressive ? 12.0 : 8.0);
        double searchResultHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double searchResultContentSpacing = density.apply(expressive ? 16.0 : 12.0);
        double pickerFieldPopupShape = expressive ? shapeTokens.extraLarge() : 28.0;
        double pickerFieldPopupPadding = density.apply(expressive ? 20.0 : 16.0);
        double pickerFieldPopupSpacing = density.apply(expressive ? 20.0 : 16.0);
        double pickerFieldPresetListWidth = density.apply(expressive ? 148.0 : 132.0);
        double pickerFieldPresetListSpacing = density.apply(expressive ? 8.0 : 6.0);
        double pickerFieldPresetButtonHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double datePickerContainerShape = expressive ? shapeTokens.extraLarge() : 28.0;
        double datePickerContainerPadding = density.apply(expressive ? 20.0 : 16.0);
        double datePickerContainerSpacing = density.apply(expressive ? 16.0 : 12.0);
        double datePickerHeaderSpacing = density.apply(expressive ? 8.0 : 4.0);
        double datePickerGridGap = density.apply(expressive ? 6.0 : 4.0);
        double timePickerContainerShape = expressive ? shapeTokens.extraLarge() : 28.0;
        double timePickerContainerPadding = density.apply(expressive ? 22.0 : 18.0);
        double timePickerContainerSpacing = density.apply(expressive ? 20.0 : 16.0);
        double timePickerDisplaySpacing = density.apply(expressive ? 6.0 : 4.0);
        double timePickerDisplayCellShape = expressive ? shapeTokens.large() : shapeTokens.medium();
        double timePickerDisplayCellWidth = density.apply(expressive ? 80.0 : 72.0);
        double timePickerDisplayCellHeight = density.apply(expressive ? 72.0 : 64.0);
        double timePickerSectionSpacing = density.apply(expressive ? 10.0 : 8.0);
        double timePickerGridGap = density.apply(expressive ? 8.0 : 6.0);
        double timePickerPeriodCellWidth = density.apply(expressive ? 100.0 : 92.0);
        double sheetContentPadding = density.apply(expressive ? 28.0 : 24.0);
        double sheetHeaderPadding = density.apply(expressive ? 28.0 : 24.0);
        double sheetDragHandleWidth = density.apply(expressive ? 36.0 : 32.0);
        double sheetDragHandleHeight = density.apply(expressive ? 5.0 : 4.0);
        double cardContainerShape = expressive ? shapeTokens.large() : shapeTokens.medium();
        double cardContentPadding = density.apply(expressive ? 20.0 : 16.0);
        double dialogContentPadding = density.apply(expressive ? 28.0 : 24.0);
        double dialogContainerMinWidth = density.apply(280.0);
        double dialogContainerMaxWidth = density.apply(560.0);
        double dialogActionSpacing = density.apply(8.0);
        double dialogIconSize = density.apply(24.0);
        double snackbarContainerShape = expressive ? shapeTokens.medium() : shapeTokens.extraSmall();
        double snackbarContentPadding = density.apply(expressive ? 18.0 : 16.0);
        double snackbarContainerMinWidth = density.apply(344.0);
        double snackbarContainerMaxWidth = density.apply(672.0);
        double snackbarSingleLineContainerHeight = density.apply(48.0);
        double snackbarTwoLineContainerHeight = density.apply(68.0);
        double snackbarActionContainerHeight = density.apply(32.0);
        double bannerMinHeight = density.apply(expressive ? 88.0 : 80.0);
        double bannerVerticalPadding = density.apply(expressive ? 20.0 : 16.0);
        double bannerHorizontalPadding = density.apply(expressive ? 28.0 : 24.0);
        double bannerContentSpacing = density.apply(expressive ? 20.0 : 16.0);
        double bannerActionSpacing = density.apply(expressive ? 12.0 : 8.0);
        double tooltipPlainContainerShape = expressive ? shapeTokens.small() : shapeTokens.extraSmall();
        double tooltipPlainVerticalPadding = density.apply(expressive ? 8.0 : 4.0);
        double tooltipPlainHorizontalPadding = density.apply(expressive ? 12.0 : 8.0);
        double tooltipRichContainerShape = shapeTokens.medium();
        double tooltipRichTopPadding = density.apply(expressive ? 16.0 : 12.0);
        double tooltipRichHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double tooltipRichBottomPadding = density.apply(expressive ? 12.0 : 8.0);
        double tooltipRichContentSpacing = density.apply(expressive ? 12.0 : 8.0);
        double tooltipRichPreferredWidth = density.apply(expressive ? 360.0 : 320.0);
        double tooltipRichActionSpacing = density.apply(expressive ? 12.0 : 8.0);
        double tooltipRichActionButtonHeight = density.apply(expressive ? 36.0 : 32.0);
        double tooltipRichActionButtonHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double appBarHorizontalPadding = density.apply(expressive ? 24.0 : 16.0);
        double topAppBarContentSpacing = density.apply(expressive ? 12.0 : 8.0);
        double topAppBarActionSpacing = 0.0;
        double bottomAppBarContentSpacing = density.apply(expressive ? 20.0 : 16.0);
        double bottomAppBarActionSpacing = 0.0;
        double topAppBarMediumBottomPadding = density.apply(expressive ? 24.0 : 20.0);
        double topAppBarLargeBottomPadding = density.apply(expressive ? 32.0 : 28.0);
        double toolbarContainerHeight = density.apply(expressive ? 72.0 : 64.0);
        double toolbarContainerWidth = density.apply(expressive ? 72.0 : 64.0);
        double toolbarContainerShape = expressive ? shapeTokens.extraLarge() : shapeTokens.large();
        double toolbarItemSlotSize = density.apply(expressive ? 56.0 : 48.0);
        double toolbarContentPadding = density.apply(expressive ? 10.0 : 8.0);
        double toolbarItemSpacing = density.apply(expressive ? 4.0 : 0.0);
        double buttonHorizontalPadding = density.apply(expressive ? 16.0 : 24.0);
        double textButtonHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double fabSmallHorizontalPadding = density.apply(expressive ? 14.0 : 12.0);
        double fabRegularHorizontalPadding = density.apply(expressive ? 18.0 : 16.0);
        double fabLargeHorizontalPadding = density.apply(expressive ? 28.0 : 24.0);
        double segmentedButtonHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double tabHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double tabActiveIndicatorHeight = density.apply(expressive ? 4.0 : 3.0);
        double tabActiveIndicatorShape = density.apply(expressive ? 4.0 : 3.0);
        double chipHorizontalPadding = density.apply(expressive ? 18.0 : 16.0);
        double chipIconHorizontalPadding = density.apply(expressive ? 10.0 : 8.0);
        double chipElementSpacing = density.apply(8.0);
        double chipIconSize = density.apply(expressive ? 20.0 : 18.0);
        double chipAvatarSize = density.apply(expressive ? 28.0 : 24.0);
        double chipAvatarShape = chipAvatarSize / 2.0;
        double chipOutlineWidth = density.apply(1.0);
        double chipGroupHorizontalGap = density.apply(expressive ? 10.0 : 8.0);
        double chipGroupVerticalGap = density.apply(expressive ? 10.0 : 8.0);
        double fieldHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double textAreaHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double textAreaVerticalPadding = density.apply(expressive ? 20.0 : 16.0);
        double formRowSpacing = density.apply(expressive ? 20.0 : 16.0);
        double formSectionContentSpacing = density.apply(expressive ? 16.0 : 12.0);
        double formSectionHeaderSpacing = density.apply(expressive ? 6.0 : 4.0);
        double formSectionHeaderBottomPadding = density.apply(expressive ? 6.0 : 4.0);
        double formRowLabelWidth = density.apply(expressive ? 200.0 : 180.0);
        double formRowColumnSpacing = density.apply(expressive ? 28.0 : 24.0);
        double formRowMinHeight = density.apply(expressive ? 72.0 : 64.0);
        double formRowTextSpacing = density.apply(expressive ? 4.0 : 2.0);
        double validationSummaryContainerShape = expressive ? shapeTokens.medium() : shapeTokens.small();
        double validationSummaryContentPadding = density.apply(expressive ? 20.0 : 16.0);
        double validationSummaryItemsSpacing = density.apply(expressive ? 6.0 : 4.0);
        double validationSummaryItemShape = expressive ? shapeTokens.small() : shapeTokens.extraSmall();
        double validationSummaryItemVerticalPadding = density.apply(expressive ? 10.0 : 8.0);
        double validationSummaryItemHorizontalPadding = density.apply(expressive ? 12.0 : 10.0);
        double selectionTouchTargetSize = density.apply(48.0);
        double selectionStateLayerSize = density.apply(40.0);
        double checkboxContainerSize = density.apply(18.0);
        double checkboxSelectedMarkWidth = density.apply(12.0);
        double checkboxSelectedMarkHeight = density.apply(10.0);
        double checkboxIndeterminateMarkWidth = density.apply(12.0);
        double checkboxIndeterminateMarkHeight = density.apply(2.0);
        double radioContainerSize = density.apply(20.0);
        double radioSelectedDotSize = density.apply(10.0);
        double switchTouchTargetSize = density.apply(48.0);
        double switchTrackWidth = density.apply(52.0);
        double switchTrackHeight = density.apply(32.0);
        double switchStateLayerSize = density.apply(40.0);
        double switchUnselectedHandleSize = density.apply(16.0);
        double switchSelectedHandleSize = density.apply(24.0);
        double switchPressedHandleSize = density.apply(28.0);
        double sliderTrackThickness = density.apply(16.0);
        double sliderStopIndicatorSize = density.apply(4.0);
        double sliderThumbSize = density.apply(44.0);
        double sliderThumbWidth = density.apply(4.0);
        double sliderThumbTrackGap = density.apply(6.0);
        double sliderTouchTargetSize = density.apply(48.0);
        double surfaceContainerShape = expressive ? shapeTokens.large() : shapeTokens.medium();
        double surfaceContentPadding = density.apply(expressive ? 20.0 : 16.0);
        double carouselTrackPadding = density.apply(expressive ? 8.0 : 4.0);
        double carouselItemSpacing = density.apply(expressive ? 16.0 : 12.0);
        double carouselItemOpacity = expressive ? 0.94 : 0.92;
        double carouselSelectedShadowRadius = density.apply(expressive ? 12.0 : 10.0);
        double carouselSelectedShadowSpread = expressive ? 0.14 : 0.12;
        double carouselSelectedShadowOffsetY = density.apply(expressive ? 4.0 : 3.0);

        return create(
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), textButtonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonSizingTokens(
                        new ButtonSizeTokens(
                                density.apply(32.0),
                                density.apply(20.0),
                                shapeTokens.full(),
                                shapeTokens.medium(),
                                expressive ? shapeTokens.small() : shapeTokens.full(),
                                density.apply(12.0),
                                density.apply(12.0),
                                density.apply(8.0),
                                density.apply(1.0)
                        ),
                        new ButtonSizeTokens(
                                density.apply(40.0),
                                density.apply(20.0),
                                shapeTokens.full(),
                                shapeTokens.medium(),
                                expressive ? shapeTokens.small() : shapeTokens.full(),
                                buttonHorizontalPadding,
                                textButtonHorizontalPadding,
                                density.apply(8.0),
                                density.apply(1.0)
                        ),
                        new ButtonSizeTokens(
                                density.apply(56.0),
                                density.apply(24.0),
                                shapeTokens.full(),
                                shapeTokens.large(),
                                expressive ? shapeTokens.medium() : shapeTokens.full(),
                                density.apply(24.0),
                                density.apply(24.0),
                                density.apply(8.0),
                                density.apply(1.0)
                        ),
                        new ButtonSizeTokens(
                                density.apply(96.0),
                                density.apply(32.0),
                                shapeTokens.full(),
                                shapeTokens.extraLarge(),
                                expressive ? shapeTokens.large() : shapeTokens.full(),
                                density.apply(48.0),
                                density.apply(48.0),
                                density.apply(12.0),
                                density.apply(2.0)
                        ),
                        new ButtonSizeTokens(
                                density.apply(136.0),
                                density.apply(40.0),
                                shapeTokens.full(),
                                shapeTokens.extraLarge(),
                                expressive ? shapeTokens.large() : shapeTokens.full(),
                                density.apply(64.0),
                                density.apply(64.0),
                                density.apply(16.0),
                                density.apply(3.0)
                        )
                ),
                new IconButtonTokens(
                        new IconButtonSizeTokens(
                                density.apply(32.0),
                                density.apply(20.0),
                                density.apply(28.0),
                                density.apply(32.0),
                                density.apply(40.0),
                                shapeTokens.full(),
                                density.apply(12.0),
                                density.apply(8.0),
                                density.apply(12.0),
                                shapeTokens.full(),
                                density.apply(1.0)
                        ),
                        new IconButtonSizeTokens(
                                density.apply(40.0),
                                density.apply(24.0),
                                density.apply(32.0),
                                density.apply(40.0),
                                density.apply(52.0),
                                shapeTokens.full(),
                                density.apply(12.0),
                                density.apply(8.0),
                                density.apply(12.0),
                                shapeTokens.full(),
                                density.apply(1.0)
                        ),
                        new IconButtonSizeTokens(
                                density.apply(56.0),
                                density.apply(24.0),
                                density.apply(48.0),
                                density.apply(56.0),
                                density.apply(72.0),
                                shapeTokens.full(),
                                density.apply(16.0),
                                density.apply(12.0),
                                density.apply(16.0),
                                shapeTokens.full(),
                                density.apply(1.0)
                        ),
                        new IconButtonSizeTokens(
                                density.apply(96.0),
                                density.apply(32.0),
                                density.apply(64.0),
                                density.apply(96.0),
                                density.apply(128.0),
                                shapeTokens.full(),
                                density.apply(28.0),
                                density.apply(16.0),
                                density.apply(28.0),
                                shapeTokens.full(),
                                density.apply(2.0)
                        ),
                        new IconButtonSizeTokens(
                                density.apply(136.0),
                                density.apply(40.0),
                                density.apply(104.0),
                                density.apply(136.0),
                                density.apply(184.0),
                                shapeTokens.full(),
                                density.apply(28.0),
                                density.apply(16.0),
                                density.apply(28.0),
                                shapeTokens.full(),
                                density.apply(3.0)
                        )
                ),
                new FabTokens(
                        fabSmallSize,
                        fabRegularSize,
                        fabLargeSize,
                        shapeTokens.medium(),
                        shapeTokens.large(),
                        shapeTokens.extraLarge(),
                        fabSmallHorizontalPadding,
                        fabRegularHorizontalPadding,
                        fabLargeHorizontalPadding,
                        density.apply(expressive ? 14.0 : 12.0)
                ),
                new IconTokens(iconSmallSize, iconMediumSize, iconLargeSize, iconExtraLargeSize),
                createButtonGroupTokens(expressive, density, shapeTokens),
                createSplitButtonTokens(expressive, density),
                new ButtonTokens(segmentedButtonHeight, shapeTokens.full(), segmentedButtonHorizontalPadding),
                new TabTokens(
                        tabHeight,
                        tabMinWidth,
                        tabHorizontalPadding,
                        tabActiveIndicatorHeight,
                        tabActiveIndicatorShape
                ),
                new FieldTokens(fieldHeight, shapeTokens.extraSmall(), fieldHorizontalPadding),
                new TextAreaTokens(
                        textAreaHeight,
                        shapeTokens.extraSmall(),
                        textAreaHorizontalPadding,
                        textAreaVerticalPadding
                ),
                new FormTokens(
                        0.0,
                        formRowSpacing,
                        formSectionContentSpacing,
                        formSectionHeaderSpacing,
                        formSectionHeaderBottomPadding,
                        formRowLabelWidth,
                        formRowColumnSpacing,
                        formRowMinHeight,
                        formRowTextSpacing
                ),
                new ValidationSummaryTokens(
                        validationSummaryContainerShape,
                        validationSummaryContentPadding,
                        validationSummaryItemsSpacing,
                        validationSummaryItemShape,
                        validationSummaryItemVerticalPadding,
                        validationSummaryItemHorizontalPadding
                ),
                new MenuTokens(
                        menuContainerShape,
                        menuContainerPadding,
                        menuItemHeight,
                        menuItemContainerShape,
                        menuSelectedItemContainerShape,
                        menuFirstItemContainerShape,
                        menuLastItemContainerShape,
                        menuItemHorizontalPadding,
                        menuItemContentSpacing,
                        menuItemSpacing
                ),
                new SearchTokens(
                        searchBarHeight,
                        shapeTokens.full(),
                        searchBarHorizontalPadding,
                        searchBarContentSpacing,
                        searchBarTrailingActionsGap,
                        searchViewContainerShape,
                        searchViewHorizontalPadding,
                        searchViewBarResultsGap,
                        searchViewResultsShape,
                        searchResultContainerShape,
                        searchViewResultPadding,
                        searchViewResultHeight,
                        searchResultHorizontalPadding,
                        searchResultContentSpacing
                ),
                new PickerFieldTokens(
                        pickerNavigationButtonSize,
                        shapeTokens.full(),
                        pickerFieldPopupShape,
                        pickerFieldPopupPadding,
                        pickerFieldPopupSpacing,
                        pickerFieldPresetListWidth,
                        pickerFieldPresetListSpacing,
                        pickerFieldPresetButtonHorizontalPadding
                ),
                new DatePickerTokens(
                        datePickerContainerShape,
                        datePickerContainerPadding,
                        datePickerContainerSpacing,
                        datePickerHeaderSpacing,
                        pickerNavigationButtonSize,
                        shapeTokens.full(),
                        datePickerDayCellSize,
                        shapeTokens.full(),
                        datePickerGridGap
                ),
                new TimePickerTokens(
                        timePickerContainerShape,
                        timePickerContainerPadding,
                        timePickerContainerSpacing,
                        timePickerDisplaySpacing,
                        timePickerDisplayCellShape,
                        timePickerDisplayCellWidth,
                        timePickerDisplayCellHeight,
                        timePickerSectionSpacing,
                        timePickerGridGap,
                        timePickerCellWidth,
                        timePickerCellHeight,
                        timePickerPeriodCellWidth,
                        shapeTokens.full()
                ),
                new SheetTokens(
                        sideSheetWidth,
                        shapeTokens.extraLarge(),
                        bottomSheetHeight,
                        shapeTokens.extraLarge(),
                        sheetContentPadding,
                        sheetHeaderPadding,
                        sheetDragHandleWidth,
                        sheetDragHandleHeight
                ),
                new ScrimTokens(0.32),
                new SelectionTokens(
                        selectionTouchTargetSize,
                        selectionStateLayerSize,
                        checkboxContainerSize,
                        checkboxSelectedMarkWidth,
                        checkboxSelectedMarkHeight,
                        checkboxIndeterminateMarkWidth,
                        checkboxIndeterminateMarkHeight,
                        radioContainerSize,
                        radioSelectedDotSize,
                        shapeTokens.full(),
                        switchTouchTargetSize,
                        switchTrackWidth,
                        switchTrackHeight,
                        switchStateLayerSize,
                        switchUnselectedHandleSize,
                        switchSelectedHandleSize,
                        switchPressedHandleSize
                ),
                new SliderTokens(
                        sliderTrackThickness,
                        shapeTokens.full(),
                        sliderStopIndicatorSize,
                        sliderThumbSize,
                        sliderThumbWidth,
                        sliderThumbTrackGap,
                        sliderTouchTargetSize
                ),
                new ChipTokens(
                        chipHeight,
                        shapeTokens.small(),
                        chipHorizontalPadding,
                        chipIconHorizontalPadding,
                        chipElementSpacing,
                        chipIconSize,
                        chipAvatarSize,
                        chipAvatarShape,
                        chipOutlineWidth,
                        chipGroupHorizontalGap,
                        chipGroupVerticalGap
                ),
                new ProgressTokens(
                        density.apply(4.0),
                        shapeTokens.full(),
                        progressCircularIndicatorSize,
                        progressLinearWaveAmplitude,
                        density.apply(40.0),
                        density.apply(4.0),
                        density.apply(4.0),
                        progressCircularWaveAmplitude,
                        density.apply(15.0),
                        density.apply(4.0)
                ),
                new LoadingIndicatorTokens(
                        loadingIndicatorContainerSize,
                        loadingIndicatorIndicatorSize
                ),
                new SurfaceTokens(surfaceContainerShape, surfaceContentPadding),
                new CarouselTokens(
                        carouselTrackPadding,
                        carouselItemSpacing,
                        carouselItemOpacity,
                        carouselSelectedShadowRadius,
                        carouselSelectedShadowSpread,
                        carouselSelectedShadowOffsetY
                ),
                new CardTokens(cardContainerShape, cardContentPadding, 1.0),
                new DialogTokens(
                        shapeTokens.extraLarge(),
                        dialogContentPadding,
                        dialogContainerMinWidth,
                        dialogContainerMaxWidth,
                        dialogActionSpacing,
                        dialogIconSize
                ),
                new SnackbarTokens(
                        snackbarContainerShape,
                        snackbarContentPadding,
                        snackbarContainerMinWidth,
                        snackbarContainerMaxWidth,
                        snackbarSingleLineContainerHeight,
                        snackbarTwoLineContainerHeight,
                        snackbarActionContainerHeight
                ),
                new BannerTokens(
                        bannerMinHeight,
                        bannerVerticalPadding,
                        bannerHorizontalPadding,
                        bannerContentSpacing,
                        bannerActionSpacing
                ),
                new TooltipTokens(
                        tooltipPlainContainerShape,
                        tooltipPlainVerticalPadding,
                        tooltipPlainHorizontalPadding,
                        tooltipRichContainerShape,
                        tooltipRichTopPadding,
                        tooltipRichHorizontalPadding,
                        tooltipRichBottomPadding,
                        tooltipRichContentSpacing,
                        tooltipRichPreferredWidth,
                        tooltipRichActionSpacing,
                        tooltipRichActionButtonHeight,
                        tooltipRichActionButtonHorizontalPadding
                ),
                new DividerTokens(1.0, 0.0, 0.0),
                new BadgeTokens(badgeSmallSize, badgeLargeHeight, badgeLargeMinWidth, badgeLargeHeight / 2.0, 4.0),
                new AvatarTokens(avatarSize, shapeTokens.full()),
                new TopAppBarTokens(
                        topAppBarHeight,
                        topAppBarMediumHeight,
                        topAppBarLargeHeight,
                        appBarHorizontalPadding,
                        topAppBarMediumBottomPadding,
                        topAppBarLargeBottomPadding,
                        topAppBarContentSpacing,
                        topAppBarActionSpacing
                ),
                new BottomAppBarTokens(
                        bottomAppBarHeight,
                        appBarHorizontalPadding,
                        bottomAppBarContentSpacing,
                        bottomAppBarActionSpacing
                ),
                new ToolbarTokens(
                        toolbarContainerHeight,
                        toolbarContainerWidth,
                        toolbarContainerShape,
                        toolbarItemSlotSize,
                        toolbarContentPadding,
                        toolbarItemSpacing
                ),
                new NavigationBarTokens(
                        navigationBarHeight,
                        navigationItemWidth,
                        navigationIndicatorWidth,
                        navigationIndicatorHeight,
                        shapeTokens.full(),
                        navigationContentSpacing,
                        navigationHorizontalPadding
                ),
                new NavigationRailTokens(
                        navigationRailWidth,
                        navigationBarHeight,
                        navigationRailItemWidth,
                        navigationRailIndicatorWidth,
                        navigationIndicatorHeight,
                        shapeTokens.full(),
                        navigationContentSpacing,
                        navigationRailVerticalPadding,
                        navigationRailHorizontalPadding,
                        navigationRailItemSpacing
                ),
                new NavigationDrawerTokens(
                        navigationDrawerWidth,
                        navigationDrawerOneLineItemHeight,
                        navigationDrawerTwoLineItemHeight,
                        navigationDrawerThreeLineItemHeight,
                        expressive ? shapeTokens.large() : shapeTokens.full(),
                        navigationDrawerContainerPadding,
                        navigationDrawerItemHorizontalPadding,
                        0.0,
                        navigationDrawerItemContentSpacing,
                        navigationDrawerItemSpacing,
                        navigationDrawerOneLineItemHeight,
                        shapeTokens.full(),
                        navigationDrawerGroupChildPadding
                ),
                new ListItemTokens(
                        listItemOneLineHeight,
                        listItemTwoLineHeight,
                        listItemThreeLineHeight,
                        expressive ? shapeTokens.small() : 0.0,
                        listItemHorizontalPadding,
                        listItemVerticalPadding,
                        listItemContentSpacing,
                        listSectionHeaderHeight,
                        listSectionHeaderHorizontalPadding
                )
        );
    }

    /// Creates the five-step button-group token scale for one component profile.
    ///
    /// Button groups own spacing and connected inner-corner behavior. Their child buttons retain the shared
    /// [ButtonSizingTokens] metrics for height, content padding, typography, icon size, and outline width.
    ///
    /// @param expressive whether the Expressive component profile is active
    /// @param density the density transform applied to layout lengths
    /// @param shapeTokens the shape system used by connected inner-corner aliases
    /// @return the button-group size token scale
    private static ButtonGroupTokens createButtonGroupTokens(
            boolean expressive,
            M3Density density,
            M3ShapeTokens shapeTokens
    ) {
        double connectedSpacing = expressive ? density.apply(2.0) : -1.0;
        return new ButtonGroupTokens(
                createButtonGroupSizeTokens(
                        density,
                        32.0,
                        18.0,
                        connectedSpacing,
                        expressive ? shapeTokens.small() : 0.0,
                        expressive ? shapeTokens.extraSmall() : 0.0
                ),
                createButtonGroupSizeTokens(
                        density,
                        40.0,
                        12.0,
                        connectedSpacing,
                        expressive ? shapeTokens.small() : 0.0,
                        expressive ? shapeTokens.extraSmall() : 0.0
                ),
                createButtonGroupSizeTokens(
                        density,
                        56.0,
                        8.0,
                        connectedSpacing,
                        expressive ? shapeTokens.small() : 0.0,
                        expressive ? shapeTokens.extraSmall() : 0.0
                ),
                createButtonGroupSizeTokens(
                        density,
                        96.0,
                        8.0,
                        connectedSpacing,
                        expressive ? shapeTokens.large() : 0.0,
                        expressive ? shapeTokens.medium() : 0.0
                ),
                createButtonGroupSizeTokens(
                        density,
                        136.0,
                        8.0,
                        connectedSpacing,
                        expressive ? shapeTokens.largeIncreased() : 0.0,
                        expressive ? shapeTokens.large() : 0.0
                ),
                -1.0,
                density.apply(expressive ? 10.0 : 8.0)
        );
    }

    /// Creates tokens for one button-group size.
    ///
    /// @param density the density transform applied to layout lengths
    /// @param containerHeight the baseline container height
    /// @param standardSpacing the baseline spacing between standard-group items
    /// @param connectedSpacing the resolved spacing between connected-group items
    /// @param connectedInnerCorner the resting connected inner corner
    /// @param connectedPressedInnerCorner the pressed connected inner corner
    /// @return the size-specific button-group tokens
    private static ButtonGroupSizeTokens createButtonGroupSizeTokens(
            M3Density density,
            double containerHeight,
            double standardSpacing,
            double connectedSpacing,
            double connectedInnerCorner,
            double connectedPressedInnerCorner
    ) {
        double resolvedContainerHeight = density.apply(containerHeight);
        return new ButtonGroupSizeTokens(
                resolvedContainerHeight,
                density.apply(standardSpacing),
                connectedSpacing,
                connectedInnerCorner,
                connectedPressedInnerCorner,
                resolvedContainerHeight / 2.0
        );
    }

    /// Creates the five-step split-button size scale for one component profile.
    ///
    /// Baseline keeps the legacy joined fallback shape while Expressive uses the current split-button spacing,
    /// asymmetric leading-button padding, state corners, optical menu-icon offsets, and selected trailing shape.
    ///
    /// @param expressive whether the Expressive component profile is active
    /// @param density the density transform applied to length tokens
    /// @return the split-button size token scale
    private static SplitButtonTokens createSplitButtonTokens(boolean expressive, M3Density density) {
        double spacing = expressive ? density.apply(2.0) : -1.0;
        return new SplitButtonTokens(
                new SplitButtonSizeTokens(
                        density.apply(32.0),
                        spacing,
                        expressive ? density.apply(4.0) : 0.0,
                        expressive ? density.apply(8.0) : 0.0,
                        expressive ? density.apply(8.0) : 0.0,
                        density.apply(12.0),
                        density.apply(expressive ? 10.0 : 12.0),
                        density.apply(22.0),
                        density.apply(1.0),
                        density.apply(13.0),
                        density.apply(13.0),
                        expressive ? density.apply(16.0) : 0.0
                ),
                new SplitButtonSizeTokens(
                        density.apply(40.0),
                        spacing,
                        expressive ? density.apply(4.0) : 0.0,
                        expressive ? density.apply(12.0) : 0.0,
                        expressive ? density.apply(12.0) : 0.0,
                        density.apply(expressive ? 16.0 : 20.0),
                        density.apply(expressive ? 12.0 : 20.0),
                        density.apply(22.0),
                        density.apply(1.0),
                        density.apply(13.0),
                        density.apply(13.0),
                        expressive ? density.apply(20.0) : 0.0
                ),
                new SplitButtonSizeTokens(
                        density.apply(56.0),
                        spacing,
                        expressive ? density.apply(4.0) : 0.0,
                        expressive ? density.apply(12.0) : 0.0,
                        expressive ? density.apply(12.0) : 0.0,
                        density.apply(24.0),
                        density.apply(24.0),
                        density.apply(26.0),
                        density.apply(2.0),
                        density.apply(15.0),
                        density.apply(15.0),
                        expressive ? density.apply(28.0) : 0.0
                ),
                new SplitButtonSizeTokens(
                        density.apply(96.0),
                        spacing,
                        expressive ? density.apply(8.0) : 0.0,
                        expressive ? density.apply(20.0) : 0.0,
                        expressive ? density.apply(20.0) : 0.0,
                        density.apply(48.0),
                        density.apply(48.0),
                        density.apply(38.0),
                        density.apply(3.0),
                        density.apply(29.0),
                        density.apply(29.0),
                        expressive ? density.apply(48.0) : 0.0
                ),
                new SplitButtonSizeTokens(
                        density.apply(136.0),
                        spacing,
                        expressive ? density.apply(12.0) : 0.0,
                        expressive ? density.apply(20.0) : 0.0,
                        expressive ? density.apply(20.0) : 0.0,
                        density.apply(64.0),
                        density.apply(64.0),
                        density.apply(50.0),
                        density.apply(6.0),
                        density.apply(43.0),
                        density.apply(43.0),
                        expressive ? density.apply(68.0) : 0.0
                )
        );
    }

    /// Converts component tokens into inline JavaFX CSS declarations.
    ///
    /// @return inline JavaFX CSS declarations for all component tokens
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        append(builder, "button-filled", filledButton());
        append(builder, "button-tonal", tonalButton());
        append(builder, "button-outlined", outlinedButton());
        append(builder, "button-text", textButton());
        append(builder, "button-elevated", elevatedButton());
        append(builder, buttonSizing());
        append(builder, iconButton());
        append(builder, floatingActionButton());
        append(builder, icon());
        append(builder, buttonGroup());
        append(builder, splitButton());
        append(builder, "segmented-button", segmentedButton());
        append(builder, tab());
        append(builder, field());
        append(builder, textArea());
        append(builder, form());
        append(builder, validationSummary());
        append(builder, menu());
        append(builder, search());
        append(builder, pickerField());
        append(builder, datePicker());
        append(builder, timePicker());
        append(builder, sheet());
        append(builder, scrim());
        append(builder, selection());
        append(builder, slider());
        append(builder, chip());
        append(builder, progress());
        append(builder, loadingIndicator());
        append(builder, surface());
        append(builder, carousel());
        append(builder, card());
        append(builder, dialog());
        append(builder, snackbar());
        append(builder, banner());
        append(builder, tooltip());
        append(builder, divider());
        append(builder, badge());
        append(builder, avatar());
        append(builder, topAppBar());
        append(builder, bottomAppBar());
        append(builder, toolbar());
        append(builder, navigationBar());
        append(builder, navigationRail());
        append(builder, navigationDrawer());
        append(builder, listItem());
        return builder.toString().trim();
    }

    /// Converts component tokens into JavaFX CSS rules for M3FX controls.
    ///
    /// @return JavaFX CSS rules that apply these component tokens to M3FX controls
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendButtonRule(builder, ".m3-filled-button", filledButton());
        appendButtonRule(builder, ".m3-tonal-button", tonalButton());
        appendButtonRule(builder, ".m3-outlined-button", outlinedButton());
        appendButtonRule(builder, ".m3-text-button", textButton());
        appendButtonRule(builder, ".m3-elevated-button", elevatedButton());
        appendButtonSizeRules(builder, buttonSizing());
        appendIconButtonRules(builder, iconButton());
        appendIconRules(builder, icon());
        appendConnectedButtonRules(builder, filledButton(), buttonGroup());
        appendSplitButtonRules(builder, filledButton(), splitButton());
        appendGroupSpacingRule(
                builder,
                ".m3-segmented-button-group",
                "-m3-segmented-button-group-spacing",
                buttonGroup().segmentedGroupSpacing()
        );
        appendGroupSpacingRule(
                builder,
                ".m3-icon-toggle-button-group",
                "-m3-icon-toggle-button-group-spacing",
                buttonGroup().iconToggleGroupSpacing()
        );
        appendGroupSpacingRule(
                builder,
                ".m3-fab-menu",
                "-m3-fab-menu-action-spacing",
                floatingActionButton().menuActionSpacing()
        );
        appendFabRule(
                builder,
                ".m3-small-fab",
                floatingActionButton().smallSize(),
                floatingActionButton().smallShape(),
                floatingActionButton().smallHorizontalPadding()
        );
        appendFabRule(
                builder,
                ".m3-regular-fab",
                floatingActionButton().regularSize(),
                floatingActionButton().regularShape(),
                floatingActionButton().regularHorizontalPadding()
        );
        appendFabRule(
                builder,
                ".m3-large-fab",
                floatingActionButton().largeSize(),
                floatingActionButton().largeShape(),
                floatingActionButton().largeHorizontalPadding()
        );
        appendButtonRule(builder, ".m3-segmented-button", segmentedButton());
        appendSegmentedButtonPositionRules(builder, segmentedButton());
        appendTabRule(builder, tab());
        appendTabIndicatorRule(builder, tab());
        appendFieldRule(builder, field());
        appendTextAreaRule(builder, textArea());
        appendFilledFieldRule(builder, field());
        appendOutlinedFieldRule(builder, field());
        appendFilledTextAreaRule(builder, textArea());
        appendOutlinedTextAreaRule(builder, textArea());
        appendFormPaneRule(builder, form());
        appendFormSectionRule(builder, form());
        appendFormSectionHeaderRule(builder, form());
        appendFormRowRule(builder, form());
        appendFormRowTextColumnRule(builder, form());
        appendValidationSummaryRule(builder, validationSummary());
        appendValidationSummaryItemsRule(builder, validationSummary());
        appendValidationSummaryItemRule(builder, validationSummary());
        appendMenuRule(builder, menu());
        appendMenuContainerRule(builder, menu());
        appendMenuItemRule(builder, menu());
        appendMenuEdgeItemRules(builder, menu());
        appendSelectedMenuItemRule(builder, menu());
        appendSearchBarRule(builder, search());
        appendSearchBarContentRule(builder, search());
        appendSearchBarTrailingRule(builder, search());
        appendSearchViewRule(builder, search());
        appendSearchViewContentRule(builder, search());
        appendSearchViewResultsRule(builder, search());
        appendSearchViewResultRule(builder, search());
        appendPickerFieldRule(builder, pickerField());
        appendPickerFieldOpenButtonRule(builder, pickerField());
        appendPickerFieldPresetContentRule(builder, pickerField());
        appendPickerFieldPresetListRule(builder, pickerField());
        appendPickerFieldPresetButtonRule(builder, pickerField());
        appendDatePickerRule(builder, datePicker());
        appendDatePickerHeaderRule(builder, datePicker());
        appendDatePickerNavigationButtonRule(builder, datePicker());
        appendDatePickerWeekdayRowRule(builder, datePicker());
        appendDatePickerGridRule(builder, datePicker());
        appendDatePickerCellRule(builder, datePicker());
        appendDatePickerCellShapeRules(builder, datePicker());
        appendTimePickerRule(builder, timePicker());
        appendTimePickerDisplayRule(builder, timePicker());
        appendTimePickerDisplayCellRule(builder, timePicker());
        appendTimePickerSectionRule(builder, timePicker());
        appendTimePickerGridRule(builder, timePicker());
        appendTimePickerCellRule(builder, timePicker());
        appendTimePickerPeriodCellRule(builder, timePicker());
        appendTimePickerPeriodCellShapeRules(builder, timePicker());
        appendSideSheetRule(builder, sheet());
        appendBottomSheetRule(builder, sheet());
        appendSheetHeaderRule(builder, sheet());
        appendSheetContentRule(builder, sheet());
        appendBottomSheetDragHandleRule(builder, sheet());
        appendScrimRule(builder, scrim());
        appendSelectionRule(builder, selection());
        appendSwitchRule(builder, selection());
        appendSwitchBoxRule(builder, selection());
        appendSliderRule(builder, slider());
        appendSliderTrackRule(builder, slider());
        appendSliderThumbRule(builder, slider());
        appendChipRule(builder, chip());
        appendChipGroupRule(builder, chip());
        appendProgressBarRule(builder, progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .track", progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .bar", progress());
        appendProgressIndicatorRule(builder, progress());
        appendLoadingIndicatorRule(builder, loadingIndicator());
        appendSurfaceRule(builder, surface());
        appendCarouselTrackRule(builder, carousel());
        appendCarouselItemRule(builder, carousel());
        appendCarouselSelectedItemRule(builder, carousel());
        appendCardRule(builder, card());
        appendDialogRule(builder, dialog());
        appendSnackbarRule(builder, snackbar());
        appendBannerRule(builder, banner());
        appendTooltipRule(builder, tooltip());
        appendRichTooltipRule(builder, tooltip());
        appendRichTooltipActionsRule(builder, tooltip());
        appendRichTooltipActionButtonRule(builder, tooltip());
        appendDividerRule(builder, divider());
        appendBadgeRule(builder, badge());
        appendAvatarRule(builder, avatar());
        appendTopAppBarRule(builder, topAppBar());
        appendTopAppBarVariantRule(
                builder,
                ".m3-top-app-bar-medium",
                topAppBar().mediumContainerHeight(),
                topAppBar().mediumBottomPadding(),
                topAppBar()
        );
        appendTopAppBarVariantRule(
                builder,
                ".m3-top-app-bar-large",
                topAppBar().largeContainerHeight(),
                topAppBar().largeBottomPadding(),
                topAppBar()
        );
        appendBottomAppBarRule(builder, bottomAppBar());
        appendToolbarRule(builder, toolbar());
        appendNavigationBarRule(builder, navigationBar());
        appendNavigationItemRule(builder, navigationBar());
        appendNavigationIndicatorRule(builder, navigationBar());
        appendNavigationRailRule(builder, navigationRail());
        appendNavigationRailItemRule(builder, navigationRail());
        appendNavigationRailIndicatorRule(builder, navigationRail());
        appendListItemRule(builder, listItem());
        appendListSectionHeaderRule(builder, listItem());
        appendNavigationDrawerRule(builder, navigationDrawer());
        appendNavigationDrawerItemRule(builder, navigationDrawer());
        appendNavigationDrawerGroupChildItemRule(
                builder,
                ".m3-navigation-drawer-group .m3-list-item.m3-navigation-drawer-group-child",
                navigationDrawer()
        );
        appendNavigationDrawerGroupChildItemRule(
                builder,
                ".m3-navigation-drawer .m3-navigation-drawer-group .m3-list-item.m3-navigation-drawer-group-child",
                navigationDrawer()
        );
        return builder.toString().stripTrailing();
    }

    /// Appends button token declarations.
    private static void append(StringBuilder builder, String prefix, ButtonTokens tokens) {
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
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
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-round-container-shape",
                M3TokenCss.pixels(tokens.roundContainerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-square-container-shape",
                M3TokenCss.pixels(tokens.squareContainerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-pressed-container-shape",
                M3TokenCss.pixels(tokens.pressedContainerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-horizontal-padding",
                M3TokenCss.pixels(tokens.horizontalPadding())
        );
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-text-horizontal-padding",
                M3TokenCss.pixels(tokens.textHorizontalPadding())
        );
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-icon-label-space",
                M3TokenCss.pixels(tokens.iconLabelSpace())
        );
        M3TokenCss.append(builder, "-m3-" + prefix + "-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
    }

    /// Appends floating action button token declarations.
    private static void append(StringBuilder builder, FabTokens tokens) {
        M3TokenCss.append(builder, "-m3-fab-small-container-size", M3TokenCss.pixels(tokens.smallSize()));
        M3TokenCss.append(builder, "-m3-fab-regular-container-size", M3TokenCss.pixels(tokens.regularSize()));
        M3TokenCss.append(builder, "-m3-fab-large-container-size", M3TokenCss.pixels(tokens.largeSize()));
        M3TokenCss.append(builder, "-m3-fab-small-container-shape", M3TokenCss.pixels(tokens.smallShape()));
        M3TokenCss.append(builder, "-m3-fab-regular-container-shape", M3TokenCss.pixels(tokens.regularShape()));
        M3TokenCss.append(builder, "-m3-fab-large-container-shape", M3TokenCss.pixels(tokens.largeShape()));
        M3TokenCss.append(builder, "-m3-fab-small-horizontal-padding", M3TokenCss.pixels(tokens.smallHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-fab-regular-horizontal-padding", M3TokenCss.pixels(tokens.regularHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-fab-large-horizontal-padding", M3TokenCss.pixels(tokens.largeHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-fab-menu-action-spacing", M3TokenCss.pixels(tokens.menuActionSpacing()));
    }

    /// Appends icon token declarations.
    private static void append(StringBuilder builder, IconTokens tokens) {
        M3TokenCss.append(builder, "-m3-icon-small-size", M3TokenCss.pixels(tokens.smallSize()));
        M3TokenCss.append(builder, "-m3-icon-medium-size", M3TokenCss.pixels(tokens.mediumSize()));
        M3TokenCss.append(builder, "-m3-icon-large-size", M3TokenCss.pixels(tokens.largeSize()));
        M3TokenCss.append(builder, "-m3-icon-extra-large-size", M3TokenCss.pixels(tokens.extraLargeSize()));
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
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-narrow-width", M3TokenCss.pixels(tokens.narrowWidth()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-default-width", M3TokenCss.pixels(tokens.defaultWidth()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-wide-width", M3TokenCss.pixels(tokens.wideWidth()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-round-container-shape", M3TokenCss.pixels(tokens.roundContainerShape()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-square-container-shape", M3TokenCss.pixels(tokens.squareContainerShape()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-pressed-container-shape", M3TokenCss.pixels(tokens.pressedContainerShape()));
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-selected-round-container-shape",
                M3TokenCss.pixels(tokens.selectedRoundContainerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-" + prefix + "-selected-square-container-shape",
                M3TokenCss.pixels(tokens.selectedSquareContainerShape())
        );
        M3TokenCss.append(builder, "-m3-" + prefix + "-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
    }
    /// Appends button-group token declarations.
    private static void append(StringBuilder builder, ButtonGroupTokens tokens) {
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group", tokens.small());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-extra-small", tokens.extraSmall());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-small", tokens.small());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-medium", tokens.medium());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-large", tokens.large());
        appendButtonGroupSizeDeclarations(builder, "-m3-button-group-extra-large", tokens.extraLarge());
        M3TokenCss.append(
                builder,
                "-m3-button-group-spacing",
                M3TokenCss.pixels(tokens.small().connectedSpacing())
        );
        M3TokenCss.append(
                builder,
                "-m3-segmented-button-group-spacing",
                M3TokenCss.pixels(tokens.segmentedGroupSpacing())
        );
        M3TokenCss.append(
                builder,
                "-m3-icon-toggle-button-group-spacing",
                M3TokenCss.pixels(tokens.iconToggleGroupSpacing())
        );
    }

    /// Appends inline declarations for one button-group size role.
    ///
    /// @param builder the target CSS declaration builder
    /// @param prefix the property prefix for the size role
    /// @param tokens the size-specific button-group tokens
    private static void appendButtonGroupSizeDeclarations(
            StringBuilder builder,
            String prefix,
            ButtonGroupSizeTokens tokens
    ) {
        M3TokenCss.append(builder, prefix + "-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, prefix + "-standard-spacing", M3TokenCss.pixels(tokens.standardSpacing()));
        M3TokenCss.append(builder, prefix + "-connected-spacing", M3TokenCss.pixels(tokens.connectedSpacing()));
        M3TokenCss.append(
                builder,
                prefix + "-connected-inner-corner",
                M3TokenCss.pixels(tokens.connectedInnerCorner())
        );
        M3TokenCss.append(
                builder,
                prefix + "-connected-pressed-inner-corner",
                M3TokenCss.pixels(tokens.connectedPressedInnerCorner())
        );
        M3TokenCss.append(
                builder,
                prefix + "-connected-selected-inner-corner",
                M3TokenCss.pixels(tokens.connectedSelectedInnerCorner())
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
    /// @param prefix the property prefix for the size role
    /// @param tokens the split-button size tokens
    private static void appendSplitButtonSizeDeclarations(
            StringBuilder builder,
            String prefix,
            SplitButtonSizeTokens tokens
    ) {
        M3TokenCss.append(builder, prefix + "-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, prefix + "-spacing", M3TokenCss.pixels(tokens.spacing()));
        M3TokenCss.append(builder, prefix + "-inner-corner", M3TokenCss.pixels(tokens.innerCorner()));
        M3TokenCss.append(
                builder,
                prefix + "-hovered-inner-corner",
                M3TokenCss.pixels(tokens.hoveredInnerCorner())
        );
        M3TokenCss.append(
                builder,
                prefix + "-pressed-inner-corner",
                M3TokenCss.pixels(tokens.pressedInnerCorner())
        );
        M3TokenCss.append(
                builder,
                prefix + "-action-leading-space",
                M3TokenCss.pixels(tokens.actionLeadingSpace())
        );
        M3TokenCss.append(
                builder,
                prefix + "-action-trailing-space",
                M3TokenCss.pixels(tokens.actionTrailingSpace())
        );
        M3TokenCss.append(
                builder,
                prefix + "-menu-width",
                M3TokenCss.pixels(tokens.menuLeadingSpace() + tokens.menuIconSize() + tokens.menuTrailingSpace())
        );
        M3TokenCss.append(builder, prefix + "-menu-icon-size", M3TokenCss.pixels(tokens.menuIconSize()));
        M3TokenCss.append(builder, prefix + "-menu-icon-offset", M3TokenCss.pixels(tokens.menuIconOffset()));
        M3TokenCss.append(
                builder,
                prefix + "-menu-leading-space",
                M3TokenCss.pixels(tokens.menuLeadingSpace())
        );
        M3TokenCss.append(
                builder,
                prefix + "-menu-trailing-space",
                M3TokenCss.pixels(tokens.menuTrailingSpace())
        );
        M3TokenCss.append(
                builder,
                prefix + "-selected-inner-corner",
                M3TokenCss.pixels(tokens.selectedInnerCorner())
        );
    }

    /// Appends tab token declarations.
    private static void append(StringBuilder builder, TabTokens tokens) {
        M3TokenCss.append(builder, "-m3-tab-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, "-m3-tab-min-width", M3TokenCss.pixels(tokens.tabMinWidth()));
        M3TokenCss.append(builder, "-m3-tab-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-tab-active-indicator-height", M3TokenCss.pixels(tokens.activeIndicatorHeight()));
        M3TokenCss.append(builder, "-m3-tab-active-indicator-shape", M3TokenCss.pixels(tokens.activeIndicatorShape()));
    }

    /// Appends field token declarations.
    private static void append(StringBuilder builder, FieldTokens tokens) {
        M3TokenCss.append(builder, "-m3-field-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-field-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-field-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
    }

    /// Appends text area token declarations.
    private static void append(StringBuilder builder, TextAreaTokens tokens) {
        M3TokenCss.append(builder, "-m3-text-area-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-text-area-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-text-area-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-text-area-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
    }

    /// Appends form token declarations.
    private static void append(StringBuilder builder, FormTokens tokens) {
        M3TokenCss.append(builder, "-m3-form-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        M3TokenCss.append(builder, "-m3-form-row-spacing", M3TokenCss.pixels(tokens.rowSpacing()));
        M3TokenCss.append(builder, "-m3-form-section-content-spacing", M3TokenCss.pixels(tokens.sectionContentSpacing()));
        M3TokenCss.append(builder, "-m3-form-section-header-spacing", M3TokenCss.pixels(tokens.sectionHeaderSpacing()));
        M3TokenCss.append(
                builder,
                "-m3-form-section-header-bottom-padding",
                M3TokenCss.pixels(tokens.sectionHeaderBottomPadding())
        );
        M3TokenCss.append(builder, "-m3-form-row-label-width", M3TokenCss.pixels(tokens.rowLabelWidth()));
        M3TokenCss.append(builder, "-m3-form-row-column-spacing", M3TokenCss.pixels(tokens.rowColumnSpacing()));
        M3TokenCss.append(builder, "-m3-form-row-min-height", M3TokenCss.pixels(tokens.rowMinHeight()));
        M3TokenCss.append(builder, "-m3-form-row-text-spacing", M3TokenCss.pixels(tokens.rowTextSpacing()));
    }

    /// Appends validation summary token declarations.
    private static void append(StringBuilder builder, ValidationSummaryTokens tokens) {
        M3TokenCss.append(
                builder,
                "-m3-validation-summary-container-shape",
                M3TokenCss.pixels(tokens.containerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-validation-summary-content-padding",
                M3TokenCss.pixels(tokens.contentPadding())
        );
        M3TokenCss.append(
                builder,
                "-m3-validation-summary-items-spacing",
                M3TokenCss.pixels(tokens.itemsSpacing())
        );
        M3TokenCss.append(builder, "-m3-validation-summary-item-shape", M3TokenCss.pixels(tokens.itemShape()));
        M3TokenCss.append(
                builder,
                "-m3-validation-summary-item-vertical-padding",
                M3TokenCss.pixels(tokens.itemVerticalPadding())
        );
        M3TokenCss.append(
                builder,
                "-m3-validation-summary-item-horizontal-padding",
                M3TokenCss.pixels(tokens.itemHorizontalPadding())
        );
    }

    /// Appends menu token declarations.
    private static void append(StringBuilder builder, MenuTokens tokens) {
        M3TokenCss.append(builder, "-m3-menu-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-menu-container-padding", M3TokenCss.pixels(tokens.containerPadding()));
        M3TokenCss.append(builder, "-m3-menu-item-height", M3TokenCss.pixels(tokens.itemHeight()));
        M3TokenCss.append(builder, "-m3-menu-item-container-shape", M3TokenCss.pixels(tokens.itemContainerShape()));
        M3TokenCss.append(
                builder,
                "-m3-menu-selected-item-container-shape",
                M3TokenCss.pixels(tokens.selectedItemContainerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-menu-first-item-container-shape",
                M3TokenCss.pixels(tokens.firstItemContainerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-menu-last-item-container-shape",
                M3TokenCss.pixels(tokens.lastItemContainerShape())
        );
        M3TokenCss.append(builder, "-m3-menu-item-horizontal-padding", M3TokenCss.pixels(tokens.itemHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-menu-item-content-spacing", M3TokenCss.pixels(tokens.itemContentSpacing()));
        M3TokenCss.append(builder, "-m3-menu-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
    }

    /// Appends search token declarations.
    private static void append(StringBuilder builder, SearchTokens tokens) {
        M3TokenCss.append(builder, "-m3-search-bar-container-height", M3TokenCss.pixels(tokens.barHeight()));
        M3TokenCss.append(builder, "-m3-search-bar-container-shape", M3TokenCss.pixels(tokens.barContainerShape()));
        M3TokenCss.append(builder, "-m3-search-bar-horizontal-padding", M3TokenCss.pixels(tokens.barHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-search-bar-content-spacing", M3TokenCss.pixels(tokens.barContentSpacing()));
        M3TokenCss.append(builder, "-m3-search-bar-trailing-actions-gap", M3TokenCss.pixels(tokens.barTrailingActionsGap()));
        M3TokenCss.append(builder, "-m3-search-view-container-shape", M3TokenCss.pixels(tokens.viewContainerShape()));
        M3TokenCss.append(builder, "-m3-search-view-horizontal-padding", M3TokenCss.pixels(tokens.viewHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-search-view-bar-results-gap", M3TokenCss.pixels(tokens.viewBarResultsGap()));
        M3TokenCss.append(builder, "-m3-search-view-results-shape", M3TokenCss.pixels(tokens.viewResultsShape()));
        M3TokenCss.append(builder, "-m3-search-view-result-container-shape", M3TokenCss.pixels(tokens.resultContainerShape()));
        M3TokenCss.append(builder, "-m3-search-view-result-padding", M3TokenCss.pixels(tokens.viewResultPadding()));
        M3TokenCss.append(builder, "-m3-search-view-result-height", M3TokenCss.pixels(tokens.resultHeight()));
        M3TokenCss.append(builder, "-m3-search-view-result-horizontal-padding", M3TokenCss.pixels(tokens.resultHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-search-view-result-content-spacing", M3TokenCss.pixels(tokens.resultContentSpacing()));
    }

    /// Appends picker field token declarations.
    private static void append(StringBuilder builder, PickerFieldTokens tokens) {
        M3TokenCss.append(builder, "-m3-picker-field-open-button-size", M3TokenCss.pixels(tokens.openButtonSize()));
        M3TokenCss.append(builder, "-m3-picker-field-open-button-shape", M3TokenCss.pixels(tokens.openButtonShape()));
        M3TokenCss.append(builder, "-m3-picker-field-popup-shape", M3TokenCss.pixels(tokens.popupShape()));
        M3TokenCss.append(builder, "-m3-picker-field-popup-padding", M3TokenCss.pixels(tokens.popupPadding()));
        M3TokenCss.append(builder, "-m3-picker-field-popup-spacing", M3TokenCss.pixels(tokens.popupSpacing()));
        M3TokenCss.append(builder, "-m3-picker-field-preset-list-width", M3TokenCss.pixels(tokens.presetListWidth()));
        M3TokenCss.append(builder, "-m3-picker-field-preset-list-spacing", M3TokenCss.pixels(tokens.presetListSpacing()));
        M3TokenCss.append(
                builder,
                "-m3-picker-field-preset-button-horizontal-padding",
                M3TokenCss.pixels(tokens.presetButtonHorizontalPadding())
        );
    }

    /// Appends date picker token declarations.
    private static void append(StringBuilder builder, DatePickerTokens tokens) {
        M3TokenCss.append(builder, "-m3-date-picker-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-date-picker-container-padding", M3TokenCss.pixels(tokens.containerPadding()));
        M3TokenCss.append(builder, "-m3-date-picker-container-spacing", M3TokenCss.pixels(tokens.containerSpacing()));
        M3TokenCss.append(builder, "-m3-date-picker-header-spacing", M3TokenCss.pixels(tokens.headerSpacing()));
        M3TokenCss.append(builder, "-m3-date-picker-navigation-button-size", M3TokenCss.pixels(tokens.navigationButtonSize()));
        M3TokenCss.append(builder, "-m3-date-picker-navigation-button-shape", M3TokenCss.pixels(tokens.navigationButtonShape()));
        M3TokenCss.append(builder, "-m3-date-picker-day-cell-size", M3TokenCss.pixels(tokens.dayCellSize()));
        M3TokenCss.append(builder, "-m3-date-picker-day-cell-shape", M3TokenCss.pixels(tokens.dayCellShape()));
        M3TokenCss.append(builder, "-m3-date-picker-grid-gap", M3TokenCss.pixels(tokens.gridGap()));
    }

    /// Appends time picker token declarations.
    private static void append(StringBuilder builder, TimePickerTokens tokens) {
        M3TokenCss.append(builder, "-m3-time-picker-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-time-picker-container-padding", M3TokenCss.pixels(tokens.containerPadding()));
        M3TokenCss.append(builder, "-m3-time-picker-container-spacing", M3TokenCss.pixels(tokens.containerSpacing()));
        M3TokenCss.append(builder, "-m3-time-picker-display-spacing", M3TokenCss.pixels(tokens.displaySpacing()));
        M3TokenCss.append(builder, "-m3-time-picker-display-cell-shape", M3TokenCss.pixels(tokens.displayCellShape()));
        M3TokenCss.append(builder, "-m3-time-picker-display-cell-width", M3TokenCss.pixels(tokens.displayCellWidth()));
        M3TokenCss.append(builder, "-m3-time-picker-display-cell-height", M3TokenCss.pixels(tokens.displayCellHeight()));
        M3TokenCss.append(builder, "-m3-time-picker-section-spacing", M3TokenCss.pixels(tokens.sectionSpacing()));
        M3TokenCss.append(builder, "-m3-time-picker-grid-gap", M3TokenCss.pixels(tokens.gridGap()));
        M3TokenCss.append(builder, "-m3-time-picker-cell-width", M3TokenCss.pixels(tokens.cellWidth()));
        M3TokenCss.append(builder, "-m3-time-picker-cell-height", M3TokenCss.pixels(tokens.cellHeight()));
        M3TokenCss.append(builder, "-m3-time-picker-period-cell-width", M3TokenCss.pixels(tokens.periodCellWidth()));
        M3TokenCss.append(builder, "-m3-time-picker-cell-shape", M3TokenCss.pixels(tokens.cellShape()));
    }

    /// Appends sheet token declarations.
    private static void append(StringBuilder builder, SheetTokens tokens) {
        M3TokenCss.append(builder, "-m3-sheet-side-container-width", M3TokenCss.pixels(tokens.sideContainerWidth()));
        M3TokenCss.append(builder, "-m3-sheet-side-container-shape", M3TokenCss.pixels(tokens.sideContainerShape()));
        M3TokenCss.append(builder, "-m3-sheet-bottom-container-height", M3TokenCss.pixels(tokens.bottomContainerHeight()));
        M3TokenCss.append(builder, "-m3-sheet-bottom-container-shape", M3TokenCss.pixels(tokens.bottomContainerShape()));
        M3TokenCss.append(builder, "-m3-sheet-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        M3TokenCss.append(builder, "-m3-sheet-header-padding", M3TokenCss.pixels(tokens.headerPadding()));
        M3TokenCss.append(builder, "-m3-sheet-drag-handle-width", M3TokenCss.pixels(tokens.dragHandleWidth()));
        M3TokenCss.append(builder, "-m3-sheet-drag-handle-height", M3TokenCss.pixels(tokens.dragHandleHeight()));
    }

    /// Appends scrim token declarations.
    private static void append(StringBuilder builder, ScrimTokens tokens) {
        M3TokenCss.append(builder, "-m3-scrim-container-opacity", Double.toString(tokens.containerOpacity()));
    }

    /// Appends selection token declarations.
    private static void append(StringBuilder builder, SelectionTokens tokens) {
        M3TokenCss.append(builder, "-m3-selection-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        M3TokenCss.append(builder, "-m3-selection-state-layer-size", M3TokenCss.pixels(tokens.stateLayerSize()));
        M3TokenCss.append(builder, "-m3-checkbox-container-size", M3TokenCss.pixels(tokens.checkboxContainerSize()));
        M3TokenCss.append(builder, "-m3-checkbox-selected-mark-width", M3TokenCss.pixels(tokens.checkboxSelectedMarkWidth()));
        M3TokenCss.append(builder, "-m3-checkbox-selected-mark-height", M3TokenCss.pixels(tokens.checkboxSelectedMarkHeight()));
        M3TokenCss.append(builder, "-m3-checkbox-indeterminate-mark-width", M3TokenCss.pixels(tokens.checkboxIndeterminateMarkWidth()));
        M3TokenCss.append(builder, "-m3-checkbox-indeterminate-mark-height", M3TokenCss.pixels(tokens.checkboxIndeterminateMarkHeight()));
        M3TokenCss.append(builder, "-m3-radio-container-size", M3TokenCss.pixels(tokens.radioContainerSize()));
        M3TokenCss.append(builder, "-m3-radio-selected-dot-size", M3TokenCss.pixels(tokens.radioSelectedDotSize()));
        M3TokenCss.append(builder, "-m3-selection-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        M3TokenCss.append(builder, "-m3-switch-touch-target-size", M3TokenCss.pixels(tokens.switchTouchTargetSize()));
        M3TokenCss.append(builder, "-m3-switch-track-width", M3TokenCss.pixels(tokens.switchTrackWidth()));
        M3TokenCss.append(builder, "-m3-switch-track-height", M3TokenCss.pixels(tokens.switchTrackHeight()));
        M3TokenCss.append(builder, "-m3-switch-state-layer-size", M3TokenCss.pixels(tokens.switchStateLayerSize()));
        M3TokenCss.append(builder, "-m3-switch-unselected-handle-size", M3TokenCss.pixels(tokens.switchUnselectedHandleSize()));
        M3TokenCss.append(builder, "-m3-switch-selected-handle-size", M3TokenCss.pixels(tokens.switchSelectedHandleSize()));
        M3TokenCss.append(builder, "-m3-switch-pressed-handle-size", M3TokenCss.pixels(tokens.switchPressedHandleSize()));
    }

    /// Appends slider token declarations.
    private static void append(StringBuilder builder, SliderTokens tokens) {
        M3TokenCss.append(builder, "-m3-slider-track-thickness", M3TokenCss.pixels(tokens.trackThickness()));
        M3TokenCss.append(builder, "-m3-slider-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        M3TokenCss.append(
                builder,
                "-m3-slider-stop-indicator-size",
                M3TokenCss.pixels(tokens.stopIndicatorSize())
        );
        M3TokenCss.append(builder, "-m3-slider-thumb-size", M3TokenCss.pixels(tokens.thumbSize()));
        M3TokenCss.append(builder, "-m3-slider-thumb-width", M3TokenCss.pixels(tokens.thumbWidth()));
        M3TokenCss.append(builder, "-m3-slider-thumb-track-gap", M3TokenCss.pixels(tokens.thumbTrackGap()));
        M3TokenCss.append(builder, "-m3-slider-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
    }

    /// Appends chip token declarations.
    private static void append(StringBuilder builder, ChipTokens tokens) {
        M3TokenCss.append(builder, "-m3-chip-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-chip-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-chip-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-chip-icon-horizontal-padding", M3TokenCss.pixels(tokens.iconHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-chip-element-spacing", M3TokenCss.pixels(tokens.elementSpacing()));
        M3TokenCss.append(builder, "-m3-chip-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        M3TokenCss.append(builder, "-m3-chip-avatar-size", M3TokenCss.pixels(tokens.avatarSize()));
        M3TokenCss.append(builder, "-m3-chip-avatar-shape", M3TokenCss.pixels(tokens.avatarShape()));
        M3TokenCss.append(builder, "-m3-chip-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
        M3TokenCss.append(builder, "-m3-chip-group-horizontal-gap", M3TokenCss.pixels(tokens.groupHorizontalGap()));
        M3TokenCss.append(builder, "-m3-chip-group-vertical-gap", M3TokenCss.pixels(tokens.groupVerticalGap()));
    }

    /// Appends progress token declarations.
    private static void append(StringBuilder builder, ProgressTokens tokens) {
        M3TokenCss.append(builder, "-m3-progress-thickness", M3TokenCss.pixels(tokens.thickness()));
        M3TokenCss.append(builder, "-m3-progress-shape", M3TokenCss.pixels(tokens.shape()));
        M3TokenCss.append(builder, "-m3-progress-indicator-size", M3TokenCss.pixels(tokens.indicatorSize()));
        M3TokenCss.append(builder, "-m3-progress-linear-wave-amplitude", M3TokenCss.pixels(tokens.linearWaveAmplitude()));
        M3TokenCss.append(builder, "-m3-progress-linear-wavelength", M3TokenCss.pixels(tokens.linearWavelength()));
        M3TokenCss.append(builder, "-m3-progress-linear-track-gap", M3TokenCss.pixels(tokens.linearTrackGap()));
        M3TokenCss.append(builder, "-m3-progress-linear-stop-size", M3TokenCss.pixels(tokens.linearStopSize()));
        M3TokenCss.append(builder, "-m3-progress-circular-wave-amplitude", M3TokenCss.pixels(tokens.circularWaveAmplitude()));
        M3TokenCss.append(builder, "-m3-progress-circular-wavelength", M3TokenCss.pixels(tokens.circularWavelength()));
        M3TokenCss.append(builder, "-m3-progress-circular-track-gap", M3TokenCss.pixels(tokens.circularTrackGap()));
    }

    /// Appends loading indicator token declarations.
    private static void append(StringBuilder builder, LoadingIndicatorTokens tokens) {
        M3TokenCss.append(builder, "-m3-loading-indicator-container-size", M3TokenCss.pixels(tokens.containerSize()));
        M3TokenCss.append(builder, "-m3-loading-indicator-indicator-size", M3TokenCss.pixels(tokens.indicatorSize()));
    }

    /// Appends surface token declarations.
    private static void append(StringBuilder builder, SurfaceTokens tokens) {
        M3TokenCss.append(builder, "-m3-surface-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-surface-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
    }

    /// Appends carousel token declarations.
    private static void append(StringBuilder builder, CarouselTokens tokens) {
        M3TokenCss.append(builder, "-m3-carousel-track-padding", M3TokenCss.pixels(tokens.trackPadding()));
        M3TokenCss.append(builder, "-m3-carousel-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        M3TokenCss.append(builder, "-m3-carousel-item-opacity", Double.toString(tokens.itemOpacity()));
        M3TokenCss.append(
                builder,
                "-m3-carousel-selected-shadow-radius",
                M3TokenCss.pixels(tokens.selectedShadowRadius())
        );
        M3TokenCss.append(
                builder,
                "-m3-carousel-selected-shadow-spread",
                Double.toString(tokens.selectedShadowSpread())
        );
        M3TokenCss.append(
                builder,
                "-m3-carousel-selected-shadow-offset-y",
                M3TokenCss.pixels(tokens.selectedShadowOffsetY())
        );
    }

    /// Appends card token declarations.
    private static void append(StringBuilder builder, CardTokens tokens) {
        M3TokenCss.append(builder, "-m3-card-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-card-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        M3TokenCss.append(builder, "-m3-card-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
    }

    /// Appends dialog token declarations.
    private static void append(StringBuilder builder, DialogTokens tokens) {
        M3TokenCss.append(builder, "-m3-dialog-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-dialog-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        M3TokenCss.append(builder, "-m3-dialog-container-min-width", M3TokenCss.pixels(tokens.containerMinWidth()));
        M3TokenCss.append(builder, "-m3-dialog-container-max-width", M3TokenCss.pixels(tokens.containerMaxWidth()));
        M3TokenCss.append(builder, "-m3-dialog-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
        M3TokenCss.append(builder, "-m3-dialog-icon-size", M3TokenCss.pixels(tokens.iconSize()));
    }

    /// Appends snackbar token declarations.
    private static void append(StringBuilder builder, SnackbarTokens tokens) {
        M3TokenCss.append(builder, "-m3-snackbar-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-snackbar-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        M3TokenCss.append(builder, "-m3-snackbar-container-min-width", M3TokenCss.pixels(tokens.containerMinWidth()));
        M3TokenCss.append(builder, "-m3-snackbar-container-max-width", M3TokenCss.pixels(tokens.containerMaxWidth()));
        M3TokenCss.append(
                builder,
                "-m3-snackbar-single-line-container-height",
                M3TokenCss.pixels(tokens.singleLineContainerHeight())
        );
        M3TokenCss.append(
                builder,
                "-m3-snackbar-two-line-container-height",
                M3TokenCss.pixels(tokens.twoLineContainerHeight())
        );
        M3TokenCss.append(
                builder,
                "-m3-snackbar-action-container-height",
                M3TokenCss.pixels(tokens.actionContainerHeight())
        );
    }

    /// Appends banner token declarations.
    private static void append(StringBuilder builder, BannerTokens tokens) {
        M3TokenCss.append(builder, "-m3-banner-container-min-height", M3TokenCss.pixels(tokens.containerMinHeight()));
        M3TokenCss.append(builder, "-m3-banner-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        M3TokenCss.append(builder, "-m3-banner-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-banner-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        M3TokenCss.append(builder, "-m3-banner-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
    }

    /// Appends tooltip token declarations.
    private static void append(StringBuilder builder, TooltipTokens tokens) {
        M3TokenCss.append(builder, "-m3-tooltip-plain-container-shape", M3TokenCss.pixels(tokens.plainContainerShape()));
        M3TokenCss.append(builder, "-m3-tooltip-plain-vertical-padding", M3TokenCss.pixels(tokens.plainVerticalPadding()));
        M3TokenCss.append(builder, "-m3-tooltip-plain-horizontal-padding", M3TokenCss.pixels(tokens.plainHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-tooltip-rich-container-shape", M3TokenCss.pixels(tokens.richContainerShape()));
        M3TokenCss.append(builder, "-m3-tooltip-rich-top-padding", M3TokenCss.pixels(tokens.richTopPadding()));
        M3TokenCss.append(builder, "-m3-tooltip-rich-horizontal-padding", M3TokenCss.pixels(tokens.richHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-tooltip-rich-bottom-padding", M3TokenCss.pixels(tokens.richBottomPadding()));
        M3TokenCss.append(builder, "-m3-tooltip-rich-content-spacing", M3TokenCss.pixels(tokens.richContentSpacing()));
        M3TokenCss.append(builder, "-m3-tooltip-rich-pref-width", M3TokenCss.pixels(tokens.richPreferredWidth()));
        M3TokenCss.append(builder, "-m3-tooltip-rich-action-spacing", M3TokenCss.pixels(tokens.richActionSpacing()));
        M3TokenCss.append(
                builder,
                "-m3-tooltip-rich-action-button-container-height",
                M3TokenCss.pixels(tokens.richActionButtonHeight())
        );
        M3TokenCss.append(
                builder,
                "-m3-tooltip-rich-action-button-horizontal-padding",
                M3TokenCss.pixels(tokens.richActionButtonHorizontalPadding())
        );
    }

    /// Appends divider token declarations.
    private static void append(StringBuilder builder, DividerTokens tokens) {
        M3TokenCss.append(builder, "-m3-divider-thickness", M3TokenCss.pixels(tokens.thickness()));
        M3TokenCss.append(builder, "-m3-divider-inset-start", M3TokenCss.pixels(tokens.insetStart()));
        M3TokenCss.append(builder, "-m3-divider-inset-end", M3TokenCss.pixels(tokens.insetEnd()));
    }

    /// Appends badge token declarations.
    private static void append(StringBuilder builder, BadgeTokens tokens) {
        M3TokenCss.append(builder, "-m3-badge-small-size", M3TokenCss.pixels(tokens.smallSize()));
        M3TokenCss.append(builder, "-m3-badge-large-height", M3TokenCss.pixels(tokens.largeHeight()));
        M3TokenCss.append(builder, "-m3-badge-large-min-width", M3TokenCss.pixels(tokens.largeMinWidth()));
        M3TokenCss.append(builder, "-m3-badge-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-badge-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
    }

    /// Appends avatar token declarations.
    private static void append(StringBuilder builder, AvatarTokens tokens) {
        M3TokenCss.append(builder, "-m3-avatar-container-size", M3TokenCss.pixels(tokens.containerSize()));
        M3TokenCss.append(builder, "-m3-avatar-container-shape", M3TokenCss.pixels(tokens.containerShape()));
    }

    /// Appends top app bar token declarations.
    private static void append(StringBuilder builder, TopAppBarTokens tokens) {
        M3TokenCss.append(builder, "-m3-top-app-bar-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, "-m3-top-app-bar-medium-container-height", M3TokenCss.pixels(tokens.mediumContainerHeight()));
        M3TokenCss.append(builder, "-m3-top-app-bar-large-container-height", M3TokenCss.pixels(tokens.largeContainerHeight()));
        M3TokenCss.append(builder, "-m3-top-app-bar-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-top-app-bar-medium-bottom-padding", M3TokenCss.pixels(tokens.mediumBottomPadding()));
        M3TokenCss.append(builder, "-m3-top-app-bar-large-bottom-padding", M3TokenCss.pixels(tokens.largeBottomPadding()));
        M3TokenCss.append(builder, "-m3-top-app-bar-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        M3TokenCss.append(builder, "-m3-top-app-bar-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
    }

    /// Appends bottom app bar token declarations.
    private static void append(StringBuilder builder, BottomAppBarTokens tokens) {
        M3TokenCss.append(builder, "-m3-bottom-app-bar-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, "-m3-bottom-app-bar-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-bottom-app-bar-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        M3TokenCss.append(builder, "-m3-bottom-app-bar-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
    }

    /// Appends toolbar token declarations.
    private static void append(StringBuilder builder, ToolbarTokens tokens) {
        M3TokenCss.append(builder, "-m3-toolbar-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, "-m3-toolbar-container-width", M3TokenCss.pixels(tokens.containerWidth()));
        M3TokenCss.append(builder, "-m3-toolbar-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-toolbar-item-slot-size", M3TokenCss.pixels(tokens.itemSlotSize()));
        M3TokenCss.append(builder, "-m3-toolbar-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        M3TokenCss.append(builder, "-m3-toolbar-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
    }

    /// Appends navigation bar token declarations.
    private static void append(StringBuilder builder, NavigationBarTokens tokens) {
        M3TokenCss.append(builder, "-m3-navigation-bar-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        M3TokenCss.append(builder, "-m3-navigation-bar-item-width", M3TokenCss.pixels(tokens.itemWidth()));
        M3TokenCss.append(builder, "-m3-navigation-bar-indicator-width", M3TokenCss.pixels(tokens.indicatorWidth()));
        M3TokenCss.append(builder, "-m3-navigation-bar-indicator-height", M3TokenCss.pixels(tokens.indicatorHeight()));
        M3TokenCss.append(builder, "-m3-navigation-bar-indicator-shape", M3TokenCss.pixels(tokens.indicatorShape()));
        M3TokenCss.append(builder, "-m3-navigation-bar-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        M3TokenCss.append(builder, "-m3-navigation-bar-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
    }

    /// Appends navigation rail token declarations.
    private static void append(StringBuilder builder, NavigationRailTokens tokens) {
        M3TokenCss.append(builder, "-m3-navigation-rail-container-width", M3TokenCss.pixels(tokens.containerWidth()));
        M3TokenCss.append(builder, "-m3-navigation-rail-item-height", M3TokenCss.pixels(tokens.itemHeight()));
        M3TokenCss.append(builder, "-m3-navigation-rail-item-width", M3TokenCss.pixels(tokens.itemWidth()));
        M3TokenCss.append(builder, "-m3-navigation-rail-indicator-width", M3TokenCss.pixels(tokens.indicatorWidth()));
        M3TokenCss.append(builder, "-m3-navigation-rail-indicator-height", M3TokenCss.pixels(tokens.indicatorHeight()));
        M3TokenCss.append(builder, "-m3-navigation-rail-indicator-shape", M3TokenCss.pixels(tokens.indicatorShape()));
        M3TokenCss.append(builder, "-m3-navigation-rail-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        M3TokenCss.append(builder, "-m3-navigation-rail-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        M3TokenCss.append(builder, "-m3-navigation-rail-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-navigation-rail-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
    }

    /// Appends navigation drawer token declarations.
    private static void append(StringBuilder builder, NavigationDrawerTokens tokens) {
        M3TokenCss.append(builder, "-m3-navigation-drawer-container-width", M3TokenCss.pixels(tokens.containerWidth()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-one-line-item-height", M3TokenCss.pixels(tokens.oneLineItemHeight()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-two-line-item-height", M3TokenCss.pixels(tokens.twoLineItemHeight()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-three-line-item-height", M3TokenCss.pixels(tokens.threeLineItemHeight()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-item-container-shape", M3TokenCss.pixels(tokens.itemContainerShape()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-container-padding", M3TokenCss.pixels(tokens.containerPadding()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-item-horizontal-padding", M3TokenCss.pixels(tokens.itemHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-item-vertical-padding", M3TokenCss.pixels(tokens.itemVerticalPadding()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-item-content-spacing", M3TokenCss.pixels(tokens.itemContentSpacing()));
        M3TokenCss.append(builder, "-m3-navigation-drawer-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        M3TokenCss.append(
                builder,
                "-m3-navigation-drawer-group-child-item-height",
                M3TokenCss.pixels(tokens.groupChildItemHeight())
        );
        M3TokenCss.append(
                builder,
                "-m3-navigation-drawer-group-child-item-container-shape",
                M3TokenCss.pixels(tokens.groupChildItemContainerShape())
        );
        M3TokenCss.append(
                builder,
                "-m3-navigation-drawer-group-child-item-horizontal-padding",
                M3TokenCss.pixels(tokens.groupChildItemHorizontalPadding())
        );
    }

    /// Appends list item token declarations.
    private static void append(StringBuilder builder, ListItemTokens tokens) {
        M3TokenCss.append(builder, "-m3-list-item-one-line-height", M3TokenCss.pixels(tokens.oneLineHeight()));
        M3TokenCss.append(builder, "-m3-list-item-two-line-height", M3TokenCss.pixels(tokens.twoLineHeight()));
        M3TokenCss.append(builder, "-m3-list-item-three-line-height", M3TokenCss.pixels(tokens.threeLineHeight()));
        M3TokenCss.append(builder, "-m3-list-item-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-list-item-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-list-item-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        M3TokenCss.append(builder, "-m3-list-item-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        M3TokenCss.append(builder, "-m3-list-section-header-height", M3TokenCss.pixels(tokens.sectionHeaderHeight()));
        M3TokenCss.append(
                builder,
                "-m3-list-section-header-horizontal-padding",
                M3TokenCss.pixels(tokens.sectionHeaderHorizontalPadding())
        );
    }

    /// Appends a button token CSS rule.
    private static void appendButtonRule(StringBuilder builder, String selector, ButtonTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends generated metrics and state shapes for every Material button size.
    private static void appendButtonSizeRules(StringBuilder builder, ButtonSizingTokens tokens) {
        appendButtonSizeRules(builder, ".m3-button-extra-small", tokens.extraSmall());
        appendButtonSizeRules(builder, ".m3-button-small", tokens.small());
        appendButtonSizeRules(builder, ".m3-button-medium", tokens.medium());
        appendButtonSizeRules(builder, ".m3-button-large", tokens.large());
        appendButtonSizeRules(builder, ".m3-button-extra-large", tokens.extraLarge());
    }

    /// Appends generated metrics and shapes for one Material button size.
    private static void appendButtonSizeRules(
            StringBuilder builder,
            String sizeSelector,
            ButtonSizeTokens tokens
    ) {
        String selector = ".m3-button" + sizeSelector;
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-button-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-graphic-text-gap", M3TokenCss.pixels(tokens.iconLabelSpace()));
        endRule(builder);

        beginRule(builder, selector + ".m3-text-button");
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.textHorizontalPadding()));
        endRule(builder);

        beginRule(builder, selector + ".m3-outlined-button");
        appendDeclaration(builder, "-fx-border-width", M3TokenCss.pixels(tokens.outlineWidth()));
        endRule(builder);

        beginRule(builder, selector + ".m3-button-round");
        appendShapeDeclarations(builder, tokens.roundContainerShape());
        endRule(builder);

        beginRule(builder, selector + ".m3-button-square");
        appendShapeDeclarations(builder, tokens.squareContainerShape());
        endRule(builder);

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
        appendDeclaration(builder, "-m3-icon-size", M3TokenCss.pixels(size));
        endRule(builder);
    }

    /// Appends generated icon button size, width, and shape rules.
    private static void appendIconButtonRules(StringBuilder builder, IconButtonTokens tokens) {
        appendIconButtonSizeRules(builder, ".m3-icon-button-extra-small", tokens.extraSmall());
        appendIconButtonSizeRules(builder, ".m3-icon-button-small", tokens.small());
        appendIconButtonSizeRules(builder, ".m3-icon-button-medium", tokens.medium());
        appendIconButtonSizeRules(builder, ".m3-icon-button-large", tokens.large());
        appendIconButtonSizeRules(builder, ".m3-icon-button-extra-large", tokens.extraLarge());

        appendIconButtonShapeRules(builder, ".m3-icon-button-extra-small", tokens.extraSmall());
        appendIconButtonShapeRules(builder, ".m3-icon-button-small", tokens.small());
        appendIconButtonShapeRules(builder, ".m3-icon-button-medium", tokens.medium());
        appendIconButtonShapeRules(builder, ".m3-icon-button-large", tokens.large());
        appendIconButtonShapeRules(builder, ".m3-icon-button-extra-large", tokens.extraLarge());
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
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-container-width", M3TokenCss.pixels(tokens.defaultWidth()));
        appendDeclaration(builder, "-m3-button-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-icon-button-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-icon-button-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
        appendDeclaration(builder, "-m3-horizontal-padding", "0px");
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-icon-button-narrow-width, "
                + toggleButtonSelector + ".m3-icon-button-narrow-width");
        appendDeclaration(builder, "-m3-container-width", M3TokenCss.pixels(tokens.narrowWidth()));
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-icon-button-wide-width, "
                + toggleButtonSelector + ".m3-icon-button-wide-width");
        appendDeclaration(builder, "-m3-container-width", M3TokenCss.pixels(tokens.wideWidth()));
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-outlined-button, "
                + toggleButtonSelector + ".m3-outlined-icon-toggle-button");
        appendDeclaration(builder, "-fx-border-width", M3TokenCss.pixels(tokens.outlineWidth()));
        endRule(builder);
    }


    /// Appends generated square, selected, and pressed shape rules for one icon button size.
    private static void appendIconButtonShapeRules(
            StringBuilder builder,
            String selector,
            IconButtonSizeTokens tokens
    ) {
        String iconButtonSelector = ".m3-button.m3-icon-button" + selector;
        String toggleButtonSelector = ".m3-icon-toggle-button" + selector;
        beginRule(builder, iconButtonSelector + ".m3-icon-button-round");
        appendShapeDeclarations(builder, tokens.roundContainerShape());
        endRule(builder);

        beginRule(builder, iconButtonSelector + ".m3-icon-button-square");
        appendShapeDeclarations(builder, tokens.squareContainerShape());
        endRule(builder);

        beginRule(builder, toggleButtonSelector + ".m3-icon-button-round:selected");
        appendShapeDeclarations(builder, tokens.selectedRoundContainerShape());
        endRule(builder);

        beginRule(builder, toggleButtonSelector + ".m3-icon-button-square:selected");
        appendShapeDeclarations(builder, tokens.selectedSquareContainerShape());
        endRule(builder);

        beginRule(builder, iconButtonSelector + ":armed, " + iconButtonSelector + ":pressed, "
                + toggleButtonSelector + ":armed, " + toggleButtonSelector + ":pressed");
        appendShapeDeclarations(builder, tokens.pressedContainerShape());
        endRule(builder);
    }

    /// Appends generated shape declarations shared by icon button shape states.
    private static void appendShapeDeclarations(StringBuilder builder, double shape) {
        String radius = M3TokenCss.pixels(shape);
        appendDeclaration(builder, "-m3-container-shape", radius);
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
    }
    /// Appends a generated single spacing rule.
    private static void appendGroupSpacingRule(StringBuilder builder, String selector, String property, double spacing) {
        beginRule(builder, selector);
        appendDeclaration(builder, property, M3TokenCss.pixels(spacing));
        endRule(builder);
    }

    /// Appends a floating action button token CSS rule.
    private static void appendFabRule(
            StringBuilder builder,
            String selector,
            double size,
            double shape,
            double horizontalPadding
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-size", M3TokenCss.pixels(size));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(shape));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(horizontalPadding));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(shape));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(shape));
        endRule(builder);
    }

    /// Appends a tab token CSS rule.
    private static void appendTabRule(StringBuilder builder, TabTokens tokens) {
        beginRule(builder, ".m3-tab");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-tab-min-width", M3TokenCss.pixels(tokens.tabMinWidth()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-active-indicator-height", M3TokenCss.pixels(tokens.activeIndicatorHeight()));
        appendDeclaration(builder, "-m3-active-indicator-shape", M3TokenCss.pixels(tokens.activeIndicatorShape()));
        endRule(builder);
    }

    /// Appends a tab active indicator token CSS rule.
    private static void appendTabIndicatorRule(StringBuilder builder, TabTokens tokens) {
        beginRule(builder, ".m3-tab-active-indicator");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.activeIndicatorShape())
                + " "
                + M3TokenCss.pixels(tokens.activeIndicatorShape())
                + " 0 0");
        endRule(builder);
    }

    /// Appends segmented button position shape CSS rules.
    private static void appendSegmentedButtonPositionRules(StringBuilder builder, ButtonTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
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
            ButtonTokens tokens,
            ButtonGroupTokens groupTokens
    ) {
        String outerRadius = M3TokenCss.pixels(tokens.containerShape());
        appendButtonGroupSizeRules(
                builder,
                ".m3-button-group-extra-small",
                groupTokens.extraSmall(),
                outerRadius
        );
        appendButtonGroupSizeRules(builder, ".m3-button-group-small", groupTokens.small(), outerRadius);
        appendButtonGroupSizeRules(builder, ".m3-button-group-medium", groupTokens.medium(), outerRadius);
        appendButtonGroupSizeRules(builder, ".m3-button-group-large", groupTokens.large(), outerRadius);
        appendButtonGroupSizeRules(
                builder,
                ".m3-button-group-extra-large",
                groupTokens.extraLarge(),
                outerRadius
        );
    }

    /// Appends spacing and connected-state shape rules for one button-group size.
    ///
    /// @param builder the target CSS builder
    /// @param sizeSelector the button-group size selector
    /// @param tokens the size-specific button-group tokens
    /// @param outerRadius the fully rounded outer corner value
    private static void appendButtonGroupSizeRules(
            StringBuilder builder,
            String sizeSelector,
            ButtonGroupSizeTokens tokens,
            String outerRadius
    ) {
        String groupSelector = ".m3-button-group" + sizeSelector;
        beginRule(builder, groupSelector);
        appendDeclaration(
                builder,
                "-m3-button-group-container-height",
                M3TokenCss.pixels(tokens.containerHeight())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-standard-spacing",
                M3TokenCss.pixels(tokens.standardSpacing())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-spacing",
                M3TokenCss.pixels(tokens.connectedSpacing())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-inner-corner",
                M3TokenCss.pixels(tokens.connectedInnerCorner())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-pressed-inner-corner",
                M3TokenCss.pixels(tokens.connectedPressedInnerCorner())
        );
        appendDeclaration(
                builder,
                "-m3-button-group-connected-selected-inner-corner",
                M3TokenCss.pixels(tokens.connectedSelectedInnerCorner())
        );
        endRule(builder);

        beginRule(builder, groupSelector + ".m3-standard-button-group");
        appendDeclaration(builder, "-m3-button-group-spacing", M3TokenCss.pixels(tokens.standardSpacing()));
        endRule(builder);

        String connectedSelector = groupSelector + ".m3-connected-button-group";
        beginRule(builder, connectedSelector);
        appendDeclaration(builder, "-m3-button-group-spacing", M3TokenCss.pixels(tokens.connectedSpacing()));
        endRule(builder);

        appendConnectedButtonStateShapeRules(
                builder,
                connectedSelector,
                "",
                outerRadius,
                M3TokenCss.pixels(tokens.connectedInnerCorner())
        );
        appendConnectedButtonStateShapeRules(
                builder,
                connectedSelector,
                ":selected",
                outerRadius,
                M3TokenCss.pixels(tokens.connectedSelectedInnerCorner())
        );
        appendConnectedButtonStateShapeRules(
                builder,
                connectedSelector,
                ":armed",
                outerRadius,
                M3TokenCss.pixels(tokens.connectedPressedInnerCorner())
        );
        appendConnectedButtonStateShapeRules(
                builder,
                connectedSelector,
                ":pressed",
                outerRadius,
                M3TokenCss.pixels(tokens.connectedPressedInnerCorner())
        );
    }

    /// Appends position-specific connected-button shapes for one interaction state.
    ///
    /// @param builder the target CSS builder
    /// @param groupSelector the connected button-group selector
    /// @param stateSuffix the child pseudo-class suffix, or an empty string for the resting state
    /// @param outerRadius the outer corner radius
    /// @param innerRadius the inner corner radius for this state
    private static void appendConnectedButtonStateShapeRules(
            StringBuilder builder,
            String groupSelector,
            String stateSuffix,
            String outerRadius,
            String innerRadius
    ) {
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-button.m3-button-group-single" + stateSuffix,
                outerRadius,
                outerRadius,
                outerRadius,
                outerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-button.m3-button-group-first" + stateSuffix,
                outerRadius,
                innerRadius,
                innerRadius,
                outerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-button.m3-button-group-middle" + stateSuffix,
                innerRadius,
                innerRadius,
                innerRadius,
                innerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                groupSelector + " .m3-button.m3-button-group-last" + stateSuffix,
                innerRadius,
                outerRadius,
                outerRadius,
                innerRadius
        );
    }

    /// Appends split-button size, logical spacing, icon, and state-shape rules.
    private static void appendSplitButtonRules(
            StringBuilder builder,
            ButtonTokens buttonTokens,
            SplitButtonTokens splitButtonTokens
    ) {
        String outerRadius = M3TokenCss.pixels(buttonTokens.containerShape());

        appendSplitButtonSizeRule(builder, ".m3-split-button", splitButtonTokens.small(), outerRadius);
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-extra-small",
                splitButtonTokens.extraSmall(),
                outerRadius
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-small",
                splitButtonTokens.small(),
                outerRadius
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-medium",
                splitButtonTokens.medium(),
                outerRadius
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-large",
                splitButtonTokens.large(),
                outerRadius
        );
        appendSplitButtonSizeRule(
                builder,
                ".m3-split-button.m3-split-button-extra-large",
                splitButtonTokens.extraLarge(),
                outerRadius
        );
    }

    /// Appends generated split-button rules for one size role.
    ///
    /// @param builder the target generated stylesheet
    /// @param ownerSelector the split-button owner selector
    /// @param tokens the metrics for the size role
    /// @param outerRadius the rounded outer-corner radius
    private static void appendSplitButtonSizeRule(
            StringBuilder builder,
            String ownerSelector,
            SplitButtonSizeTokens tokens,
            String outerRadius
    ) {
        double menuWidth = tokens.menuLeadingSpace() + tokens.menuIconSize() + tokens.menuTrailingSpace();
        beginRule(builder, ownerSelector);
        appendDeclaration(builder, "-m3-split-button-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-split-button-spacing", M3TokenCss.pixels(tokens.spacing()));
        appendDeclaration(builder, "-m3-split-button-menu-width", M3TokenCss.pixels(menuWidth));
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-action");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-action:left-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + M3TokenCss.pixels(tokens.actionTrailingSpace())
                        + " 0 "
                        + M3TokenCss.pixels(tokens.actionLeadingSpace())
        );
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-action:right-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + M3TokenCss.pixels(tokens.actionLeadingSpace())
                        + " 0 "
                        + M3TokenCss.pixels(tokens.actionTrailingSpace())
        );
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-menu");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-horizontal-padding", "0px");
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(menuWidth));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(menuWidth));
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-menu:left-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + M3TokenCss.pixels(tokens.menuLeadingSpace())
                        + " 0 "
                        + M3TokenCss.pixels(tokens.menuTrailingSpace())
        );
        endRule(builder);

        beginRule(builder, ownerSelector + " .m3-button.m3-split-button-menu:right-edge");
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 "
                        + M3TokenCss.pixels(tokens.menuTrailingSpace())
                        + " 0 "
                        + M3TokenCss.pixels(tokens.menuLeadingSpace())
        );
        endRule(builder);

        String indicatorSelector =
                ownerSelector + " .m3-button.m3-split-button-menu .m3-disclosure-icon";
        beginRule(builder, indicatorSelector);
        appendDeclaration(builder, "-m3-disclosure-icon-size", M3TokenCss.pixels(tokens.menuIconSize()));
        endRule(builder);

        beginRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:left-edge .m3-disclosure-icon"
        );
        appendDeclaration(builder, "-fx-translate-x", M3TokenCss.pixels(tokens.menuIconOffset()));
        endRule(builder);

        beginRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:right-edge .m3-disclosure-icon"
        );
        appendDeclaration(builder, "-fx-translate-x", M3TokenCss.pixels(-tokens.menuIconOffset()));
        endRule(builder);

        beginRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:showing .m3-disclosure-icon"
        );
        appendDeclaration(builder, "-fx-translate-x", "0px");
        endRule(builder);

        appendSplitButtonPartShapeRules(builder, ownerSelector, tokens, outerRadius);
    }

    /// Appends resting and interactive split-button corner rules for one size role.
    ///
    /// @param builder the target generated stylesheet
    /// @param ownerSelector the split-button owner selector
    /// @param tokens the metrics for the size role
    /// @param outerRadius the rounded outer-corner radius
    private static void appendSplitButtonPartShapeRules(
            StringBuilder builder,
            String ownerSelector,
            SplitButtonSizeTokens tokens,
            String outerRadius
    ) {
        appendSplitButtonEdgeShapeRules(
                builder,
                ownerSelector,
                "",
                outerRadius,
                M3TokenCss.pixels(tokens.innerCorner())
        );
        appendSplitButtonEdgeShapeRules(
                builder,
                ownerSelector,
                ":hover, :focused, :focus-visible",
                outerRadius,
                M3TokenCss.pixels(tokens.hoveredInnerCorner())
        );
        appendSplitButtonEdgeShapeRules(
                builder,
                ownerSelector,
                ":armed, :pressed",
                outerRadius,
                M3TokenCss.pixels(tokens.pressedInnerCorner())
        );

        String selectedInnerRadius = M3TokenCss.pixels(tokens.selectedInnerCorner());
        appendConnectedButtonShapeRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:left-edge:showing",
                outerRadius,
                selectedInnerRadius,
                selectedInnerRadius,
                outerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-menu:right-edge:showing",
                selectedInnerRadius,
                outerRadius,
                outerRadius,
                selectedInnerRadius
        );
    }

    /// Appends physical left- and right-edge corner rules for split-button parts in one interaction state.
    ///
    /// @param builder the target generated stylesheet
    /// @param ownerSelector the split-button owner selector
    /// @param stateSuffixes comma-separated pseudo-class suffixes, or an empty string for the resting state
    /// @param outerRadius the outer-corner radius
    /// @param innerRadius the active inner-corner radius
    private static void appendSplitButtonEdgeShapeRules(
            StringBuilder builder,
            String ownerSelector,
            String stateSuffixes,
            String outerRadius,
            String innerRadius
    ) {
        if (stateSuffixes.isEmpty()) {
            appendSplitButtonEdgeShapeRule(builder, ownerSelector, "", outerRadius, innerRadius);
            return;
        }
        for (String stateSuffix : stateSuffixes.split(", ")) {
            appendSplitButtonEdgeShapeRule(builder, ownerSelector, stateSuffix, outerRadius, innerRadius);
        }
    }

    /// Appends both physical edge rules for one split-button interaction state.
    ///
    /// @param builder the target generated stylesheet
    /// @param ownerSelector the split-button owner selector
    /// @param stateSuffix the child pseudo-class suffix
    /// @param outerRadius the outer-corner radius
    /// @param innerRadius the inner-corner radius
    private static void appendSplitButtonEdgeShapeRule(
            StringBuilder builder,
            String ownerSelector,
            String stateSuffix,
            String outerRadius,
            String innerRadius
    ) {
        appendConnectedButtonShapeRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-action:left-edge" + stateSuffix
                        + ", "
                        + ownerSelector + " .m3-button.m3-split-button-menu:left-edge" + stateSuffix,
                outerRadius,
                innerRadius,
                innerRadius,
                outerRadius
        );
        appendConnectedButtonShapeRule(
                builder,
                ownerSelector + " .m3-button.m3-split-button-action:right-edge" + stateSuffix
                        + ", "
                        + ownerSelector + " .m3-button.m3-split-button-menu:right-edge" + stateSuffix,
                innerRadius,
                outerRadius,
                outerRadius,
                innerRadius
        );
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
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a text area token CSS rule.
    private static void appendTextAreaRule(StringBuilder builder, TextAreaTokens tokens) {
        beginRule(builder, ".m3-text-area");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        endRule(builder);
    }

    /// Appends a filled field shape CSS rule.
    private static void appendFilledFieldRule(StringBuilder builder, FieldTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, ".m3-filled-field");
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends an outlined field shape CSS rule.
    private static void appendOutlinedFieldRule(StringBuilder builder, FieldTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, ".m3-outlined-field");
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a filled text area shape CSS rule.
    private static void appendFilledTextAreaRule(StringBuilder builder, TextAreaTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, ".m3-text-area.m3-filled-field");
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends an outlined text area shape CSS rule.
    private static void appendOutlinedTextAreaRule(StringBuilder builder, TextAreaTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, ".m3-text-area.m3-outlined-field");
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a form pane token CSS rule.
    private static void appendFormPaneRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-pane");
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-row-spacing", M3TokenCss.pixels(tokens.rowSpacing()));
        endRule(builder);
    }

    /// Appends a form section token CSS rule.
    private static void appendFormSectionRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-section");
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.sectionContentSpacing()));
        endRule(builder);
    }

    /// Appends a form section header token CSS rule.
    private static void appendFormSectionHeaderRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-section-header");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.sectionHeaderSpacing()));
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 0 " + M3TokenCss.pixels(tokens.sectionHeaderBottomPadding()) + " 0"
        );
        endRule(builder);
    }

    /// Appends a form row token CSS rule.
    private static void appendFormRowRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-row");
        appendDeclaration(builder, "-m3-label-width", M3TokenCss.pixels(tokens.rowLabelWidth()));
        appendDeclaration(builder, "-m3-column-spacing", M3TokenCss.pixels(tokens.rowColumnSpacing()));
        appendDeclaration(builder, "-m3-row-min-height", M3TokenCss.pixels(tokens.rowMinHeight()));
        endRule(builder);
    }

    /// Appends a form row text column token CSS rule.
    private static void appendFormRowTextColumnRule(StringBuilder builder, FormTokens tokens) {
        beginRule(builder, ".m3-form-row-text-column");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.rowTextSpacing()));
        endRule(builder);
    }

    /// Appends a validation summary token CSS rule.
    private static void appendValidationSummaryRule(
            StringBuilder builder,
            ValidationSummaryTokens tokens
    ) {
        beginRule(builder, ".m3-validation-summary");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a validation summary item container token CSS rule.
    private static void appendValidationSummaryItemsRule(
            StringBuilder builder,
            ValidationSummaryTokens tokens
    ) {
        beginRule(builder, ".m3-validation-summary-items");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.itemsSpacing()));
        endRule(builder);
    }

    /// Appends a validation summary item token CSS rule.
    private static void appendValidationSummaryItemRule(
            StringBuilder builder,
            ValidationSummaryTokens tokens
    ) {
        String shape = M3TokenCss.pixels(tokens.itemShape());
        String verticalPadding = M3TokenCss.pixels(tokens.itemVerticalPadding());
        String horizontalPadding = M3TokenCss.pixels(tokens.itemHorizontalPadding());
        beginRule(builder, ".m3-validation-summary-item");
        appendDeclaration(builder, "-fx-background-radius", shape);
        appendDeclaration(builder, "-fx-border-radius", shape);
        appendDeclaration(builder, "-fx-padding", verticalPadding + " " + horizontalPadding);
        endRule(builder);
    }

    /// Appends a menu token CSS rule.
    private static void appendMenuRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu.m3-menu");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.containerPadding()));
        endRule(builder);
    }

    /// Appends a menu item container token CSS rule.
    private static void appendMenuContainerRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-container");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a menu item token CSS rule.
    private static void appendMenuItemRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item");
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-two-line-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-three-line-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.itemContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.itemHorizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(0.0));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.itemContentSpacing()));
        endRule(builder);
    }

    /// Appends structural menu item token CSS rules.
    private static void appendMenuEdgeItemRules(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item:first-menu-item");
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.firstItemContainerShape()));
        endRule(builder);
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item:last-menu-item");
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.lastItemContainerShape()));
        endRule(builder);
    }

    /// Appends selected menu item token CSS rule.
    private static void appendSelectedMenuItemRule(StringBuilder builder, MenuTokens tokens) {
        beginRule(builder, ".m3-menu .m3-menu-item.m3-menu-item:selected");
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.selectedItemContainerShape()));
        endRule(builder);
    }

    /// Appends a search bar token CSS rule.
    private static void appendSearchBarRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-bar.m3-search-bar");
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.barHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.barHeight()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.barContainerShape()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.barHorizontalPadding()));
        endRule(builder);
    }

    /// Appends a search bar content token CSS rule.
    private static void appendSearchBarContentRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-bar .m3-search-bar-content");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.barContentSpacing()));
        endRule(builder);
    }

    /// Appends a search bar trailing-actions token CSS rule.
    private static void appendSearchBarTrailingRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-bar .m3-search-bar-trailing");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.barTrailingActionsGap()));
        endRule(builder);
    }

    /// Appends a search view token CSS rule.
    private static void appendSearchViewRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-view.m3-search-view");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.viewContainerShape()));
        String horizontalPadding = M3TokenCss.pixels(tokens.viewHorizontalPadding());
        appendDeclaration(
                builder,
                "-fx-padding",
                "0 " + horizontalPadding + " " + M3TokenCss.pixels(tokens.viewResultPadding()) + " " + horizontalPadding
        );
        endRule(builder);
    }

    /// Appends a search view content token CSS rule.
    private static void appendSearchViewContentRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-view .m3-search-view-content");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.viewBarResultsGap()));
        endRule(builder);
    }

    /// Appends a search view results container token CSS rule.
    private static void appendSearchViewResultsRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-view .m3-search-view-results");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.viewResultsShape()));
        endRule(builder);
    }

    /// Appends search result item token CSS rules.
    private static void appendSearchViewResultRule(StringBuilder builder, SearchTokens tokens) {
        beginRule(builder, ".m3-search-view .m3-list-item.m3-list-item");
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.resultHeight()));
        appendDeclaration(builder, "-m3-two-line-height", M3TokenCss.pixels(tokens.resultHeight() + 16.0));
        appendDeclaration(builder, "-m3-three-line-height", M3TokenCss.pixels(tokens.resultHeight() + 32.0));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.resultContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.resultHorizontalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.resultContentSpacing()));
        endRule(builder);
    }

    /// Appends a picker field popup token CSS rule.
    private static void appendPickerFieldRule(StringBuilder builder, PickerFieldTokens tokens) {
        beginRule(builder, ".m3-picker-field-popup");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.popupShape()));
        endRule(builder);
    }

    /// Appends a picker field open button token CSS rule.
    private static void appendPickerFieldOpenButtonRule(
            StringBuilder builder,
            PickerFieldTokens tokens
    ) {
        beginRule(builder, ".m3-picker-field-open-button");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.openButtonShape()));
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.openButtonSize()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.openButtonSize()));
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
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.popupShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.popupPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.popupSpacing()));
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
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.presetListSpacing()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.presetListWidth()));
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
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.presetButtonHorizontalPadding()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.presetListWidth()));
        appendDeclaration(builder, "-fx-max-width", M3TokenCss.pixels(tokens.presetListWidth()));
        endRule(builder);
    }

    /// Appends a date picker container token CSS rule.
    private static void appendDatePickerRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-container");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.containerPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.containerSpacing()));
        endRule(builder);
    }

    /// Appends a date picker header token CSS rule.
    private static void appendDatePickerHeaderRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-header");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.headerSpacing()));
        endRule(builder);
    }

    /// Appends a date picker navigation button token CSS rule.
    private static void appendDatePickerNavigationButtonRule(
            StringBuilder builder,
            DatePickerTokens tokens
    ) {
        beginRule(builder, ".m3-date-picker-navigation-button");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.navigationButtonSize()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.navigationButtonShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(0.0));
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(tokens.navigationButtonSize()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.navigationButtonSize()));
        appendDeclaration(builder, "-fx-max-width", M3TokenCss.pixels(tokens.navigationButtonSize()));
        endRule(builder);
    }

    /// Appends a date picker weekday row token CSS rule.
    private static void appendDatePickerWeekdayRowRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-weekday-row");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.gridGap()));
        endRule(builder);
    }

    /// Appends a date picker day grid token CSS rule.
    private static void appendDatePickerGridRule(StringBuilder builder, DatePickerTokens tokens) {
        beginRule(builder, ".m3-date-picker-day-grid");
        appendDeclaration(builder, "-fx-hgap", M3TokenCss.pixels(tokens.gridGap()));
        appendDeclaration(builder, "-fx-vgap", M3TokenCss.pixels(tokens.gridGap()));
        endRule(builder);
    }

    /// Appends date picker day and weekday cell token CSS rules.
    private static void appendDatePickerCellRule(StringBuilder builder, DatePickerTokens tokens) {
        String size = M3TokenCss.pixels(tokens.dayCellSize());
        String shape = M3TokenCss.pixels(tokens.dayCellShape());
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
        appendDeclaration(builder, "-fx-border-radius", shape);
        endRule(builder);
    }

    /// Appends date range cell shape token CSS rules.
    private static void appendDatePickerCellShapeRules(StringBuilder builder, DatePickerTokens tokens) {
        String shape = M3TokenCss.pixels(tokens.dayCellShape());
        appendDatePickerRangeShapeRule(
                builder,
                ".m3-date-picker-day-cell.m3-date-range-picker-range-start-day",
                shape + " 0 0 " + shape
        );
        appendDatePickerRangeShapeRule(
                builder,
                ".m3-date-picker-day-cell.m3-date-range-picker-range-end-day",
                "0 " + shape + " " + shape + " 0"
        );
        appendDatePickerRangeShapeRule(
                builder,
                ".m3-date-picker-day-cell.m3-date-range-picker-range-single-day",
                shape
        );
    }

    /// Appends one date range cell shape token CSS rule.
    private static void appendDatePickerRangeShapeRule(StringBuilder builder, String selector, String radius) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius);
        endRule(builder);
    }

    /// Appends a time picker container token CSS rule.
    private static void appendTimePickerRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-container");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.containerPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.containerSpacing()));
        endRule(builder);
    }

    /// Appends a time picker display token CSS rule.
    private static void appendTimePickerDisplayRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-display");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.displaySpacing()));
        endRule(builder);
    }

    /// Appends a time picker selected display cell token CSS rule.
    private static void appendTimePickerDisplayCellRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-hour-display, .m3-time-picker-minute-display");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.displayCellShape()));
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(tokens.displayCellWidth()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.displayCellWidth()));
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.displayCellHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.displayCellHeight()));
        endRule(builder);
    }

    /// Appends a time picker section token CSS rule.
    private static void appendTimePickerSectionRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-section");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.sectionSpacing()));
        endRule(builder);
    }

    /// Appends a time picker grid token CSS rule.
    private static void appendTimePickerGridRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-grid");
        appendDeclaration(builder, "-fx-hgap", M3TokenCss.pixels(tokens.gridGap()));
        appendDeclaration(builder, "-fx-vgap", M3TokenCss.pixels(tokens.gridGap()));
        endRule(builder);
    }

    /// Appends a time picker selectable cell token CSS rule.
    private static void appendTimePickerCellRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-cell");
        appendTimePickerCellSize(builder, tokens.cellWidth(), tokens.cellHeight());
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.cellShape()));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(tokens.cellShape()));
        endRule(builder);
    }

    /// Appends a time picker period cell token CSS rule.
    private static void appendTimePickerPeriodCellRule(StringBuilder builder, TimePickerTokens tokens) {
        beginRule(builder, ".m3-time-picker-period-cell");
        appendTimePickerCellSize(builder, tokens.periodCellWidth(), tokens.cellHeight());
        endRule(builder);
    }

    /// Appends tokenized time cell size declarations.
    private static void appendTimePickerCellSize(StringBuilder builder, double width, double height) {
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(width));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(width));
        appendDeclaration(builder, "-fx-max-width", M3TokenCss.pixels(width));
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(height));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(height));
        appendDeclaration(builder, "-fx-max-height", M3TokenCss.pixels(height));
    }

    /// Appends time picker period segmented shape token CSS rules.
    private static void appendTimePickerPeriodCellShapeRules(StringBuilder builder, TimePickerTokens tokens) {
        String shape = M3TokenCss.pixels(tokens.cellShape());
        beginRule(builder, ".m3-time-picker-period-start");
        appendDeclaration(builder, "-fx-background-radius", shape + " 0 0 " + shape);
        appendDeclaration(builder, "-fx-border-radius", shape + " 0 0 " + shape);
        endRule(builder);
        beginRule(builder, ".m3-time-picker-period-end");
        appendDeclaration(builder, "-fx-background-radius", "0 " + shape + " " + shape + " 0");
        appendDeclaration(builder, "-fx-border-radius", "0 " + shape + " " + shape + " 0");
        endRule(builder);
    }

    /// Appends a side sheet token CSS rule.
    private static void appendSideSheetRule(StringBuilder builder, SheetTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.sideContainerShape());
        beginRule(builder, ".m3-side-sheet.m3-side-sheet");
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.sideContainerWidth()));
        appendDeclaration(builder, "-fx-background-radius", radius + " 0 0 " + radius);
        endRule(builder);
    }

    /// Appends a bottom sheet token CSS rule.
    private static void appendBottomSheetRule(StringBuilder builder, SheetTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.bottomContainerShape());
        beginRule(builder, ".m3-bottom-sheet.m3-bottom-sheet");
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.bottomContainerHeight()));
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends a sheet header token CSS rule.
    private static void appendSheetHeaderRule(StringBuilder builder, SheetTokens tokens) {
        String padding = M3TokenCss.pixels(tokens.headerPadding());
        String bottomPadding = M3TokenCss.pixels(tokens.headerPadding() / 3.0);
        beginRule(builder, ".m3-side-sheet .m3-sheet-header, .m3-bottom-sheet .m3-sheet-header");
        appendDeclaration(builder, "-fx-padding", padding + " " + padding + " " + bottomPadding + " " + padding);
        endRule(builder);
    }

    /// Appends a sheet content token CSS rule.
    private static void appendSheetContentRule(StringBuilder builder, SheetTokens tokens) {
        beginRule(builder, ".m3-side-sheet .m3-sheet-content, .m3-bottom-sheet .m3-sheet-content");
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a bottom sheet drag handle token CSS rule.
    private static void appendBottomSheetDragHandleRule(StringBuilder builder, SheetTokens tokens) {
        String handleWidth = M3TokenCss.pixels(tokens.dragHandleWidth());
        String handleHeight = M3TokenCss.pixels(tokens.dragHandleHeight());
        beginRule(builder, ".m3-bottom-sheet .m3-bottom-sheet-drag-handle");
        appendDeclaration(builder, "-fx-min-width", handleWidth);
        appendDeclaration(builder, "-fx-pref-width", handleWidth);
        appendDeclaration(builder, "-fx-max-width", handleWidth);
        appendDeclaration(builder, "-fx-min-height", handleHeight);
        appendDeclaration(builder, "-fx-pref-height", handleHeight);
        appendDeclaration(builder, "-fx-max-height", handleHeight);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.dragHandleHeight() / 2.0));
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
        appendDeclaration(builder, "-m3-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        appendDeclaration(builder, "-m3-state-layer-size", M3TokenCss.pixels(tokens.stateLayerSize()));
        endRule(builder);

        beginRule(builder, ".m3-checkbox");
        appendDeclaration(builder, "-m3-container-size", M3TokenCss.pixels(tokens.checkboxContainerSize()));
        appendDeclaration(builder, "-m3-selected-mark-width", M3TokenCss.pixels(tokens.checkboxSelectedMarkWidth()));
        appendDeclaration(builder, "-m3-selected-mark-height", M3TokenCss.pixels(tokens.checkboxSelectedMarkHeight()));
        appendDeclaration(builder, "-m3-indeterminate-mark-width", M3TokenCss.pixels(tokens.checkboxIndeterminateMarkWidth()));
        appendDeclaration(builder, "-m3-indeterminate-mark-height", M3TokenCss.pixels(tokens.checkboxIndeterminateMarkHeight()));
        endRule(builder);

        beginRule(builder, ".m3-radio-button");
        appendDeclaration(builder, "-m3-container-size", M3TokenCss.pixels(tokens.radioContainerSize()));
        appendDeclaration(builder, "-m3-selected-dot-size", M3TokenCss.pixels(tokens.radioSelectedDotSize()));
        endRule(builder);
    }

    /// Appends a switch token CSS rule.
    private static void appendSwitchRule(StringBuilder builder, SelectionTokens tokens) {
        beginRule(builder, ".m3-switch");
        appendDeclaration(builder, "-m3-touch-target-size", M3TokenCss.pixels(tokens.switchTouchTargetSize()));
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-m3-track-width", M3TokenCss.pixels(tokens.switchTrackWidth()));
        appendDeclaration(builder, "-m3-track-height", M3TokenCss.pixels(tokens.switchTrackHeight()));
        appendDeclaration(builder, "-m3-state-layer-size", M3TokenCss.pixels(tokens.switchStateLayerSize()));
        appendDeclaration(builder, "-m3-unselected-handle-size", M3TokenCss.pixels(tokens.switchUnselectedHandleSize()));
        appendDeclaration(builder, "-m3-selected-handle-size", M3TokenCss.pixels(tokens.switchSelectedHandleSize()));
        appendDeclaration(builder, "-m3-pressed-handle-size", M3TokenCss.pixels(tokens.switchPressedHandleSize()));
        endRule(builder);
    }

    /// Appends a switch box shape CSS rule.
    private static void appendSwitchBoxRule(StringBuilder builder, SelectionTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.trackShape());
        beginRule(builder, ".m3-switch .box");
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a slider token CSS rule.
    private static void appendSliderRule(StringBuilder builder, SliderTokens tokens) {
        beginRule(builder, ".m3-slider");
        appendDeclaration(builder, "-m3-track-thickness", M3TokenCss.pixels(tokens.trackThickness()));
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-m3-stop-indicator-size", M3TokenCss.pixels(tokens.stopIndicatorSize()));
        appendDeclaration(builder, "-m3-thumb-size", M3TokenCss.pixels(tokens.thumbSize()));
        appendDeclaration(builder, "-m3-thumb-width", M3TokenCss.pixels(tokens.thumbWidth()));
        appendDeclaration(builder, "-m3-thumb-track-gap", M3TokenCss.pixels(tokens.thumbTrackGap()));
        appendDeclaration(builder, "-m3-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        endRule(builder);
    }

    /// Appends a slider track visual CSS rule.
    private static void appendSliderTrackRule(StringBuilder builder, SliderTokens tokens) {
        beginRule(builder, ".m3-slider .track");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.trackThickness()));
        endRule(builder);
    }

    /// Appends a slider thumb visual CSS rule.
    private static void appendSliderThumbRule(StringBuilder builder, SliderTokens tokens) {
        beginRule(builder, ".m3-slider .thumb");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.trackShape()));
        endRule(builder);
    }

    /// Appends a chip token CSS rule.
    private static void appendChipRule(StringBuilder builder, ChipTokens tokens) {
        beginRule(builder, ".m3-chip");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-icon-horizontal-padding", M3TokenCss.pixels(tokens.iconHorizontalPadding()));
        appendDeclaration(builder, "-m3-chip-element-spacing", M3TokenCss.pixels(tokens.elementSpacing()));
        appendDeclaration(builder, "-m3-chip-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        appendDeclaration(builder, "-m3-chip-avatar-size", M3TokenCss.pixels(tokens.avatarSize()));
        appendDeclaration(builder, "-m3-chip-avatar-shape", M3TokenCss.pixels(tokens.avatarShape()));
        appendDeclaration(builder, "-m3-chip-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-width", M3TokenCss.pixels(tokens.outlineWidth()));
        appendDeclaration(builder, "-fx-graphic-text-gap", M3TokenCss.pixels(tokens.elementSpacing()));
        endRule(builder);
    }

    /// Appends a generated chip group rule.
    private static void appendChipGroupRule(StringBuilder builder, ChipTokens tokens) {
        beginRule(builder, ".m3-chip-group");
        appendDeclaration(builder, "-m3-chip-group-horizontal-gap", M3TokenCss.pixels(tokens.groupHorizontalGap()));
        appendDeclaration(builder, "-m3-chip-group-vertical-gap", M3TokenCss.pixels(tokens.groupVerticalGap()));
        endRule(builder);
    }

    /// Appends a progress bar token CSS rule.
    private static void appendProgressBarRule(StringBuilder builder, ProgressTokens tokens) {
        beginRule(builder, ".m3-progress-bar");
        appendDeclaration(builder, "-m3-track-thickness", M3TokenCss.pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.shape()));
        appendDeclaration(builder, "-m3-wave-amplitude", M3TokenCss.pixels(tokens.linearWaveAmplitude()));
        appendDeclaration(builder, "-m3-wavelength", M3TokenCss.pixels(tokens.linearWavelength()));
        appendDeclaration(builder, "-m3-track-gap", M3TokenCss.pixels(tokens.linearTrackGap()));
        appendDeclaration(builder, "-m3-stop-size", M3TokenCss.pixels(tokens.linearStopSize()));
        endRule(builder);
    }

    /// Appends a progress bar track visual CSS rule.
    private static void appendProgressBarTrackRule(StringBuilder builder, String selector, ProgressTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-arc-width", M3TokenCss.pixels(progressTrackRadius(tokens) * 2.0));
        appendDeclaration(builder, "-fx-arc-height", M3TokenCss.pixels(progressTrackRadius(tokens) * 2.0));
        endRule(builder);
    }

    /// Returns a cleanly renderable progress track radius for the current thickness.
    private static double progressTrackRadius(ProgressTokens tokens) {
        return Math.min(tokens.shape(), tokens.thickness() / 2.0);
    }

    /// Appends a progress indicator token CSS rule.
    private static void appendProgressIndicatorRule(StringBuilder builder, ProgressTokens tokens) {
        beginRule(builder, ".m3-progress-indicator");
        appendDeclaration(builder, "-m3-track-thickness", M3TokenCss.pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-indicator-size", M3TokenCss.pixels(tokens.indicatorSize()));
        appendDeclaration(builder, "-m3-wave-amplitude", M3TokenCss.pixels(tokens.circularWaveAmplitude()));
        appendDeclaration(builder, "-m3-wavelength", M3TokenCss.pixels(tokens.circularWavelength()));
        appendDeclaration(builder, "-m3-track-gap", M3TokenCss.pixels(tokens.circularTrackGap()));
        endRule(builder);
    }

    /// Appends a loading indicator token CSS rule.
    private static void appendLoadingIndicatorRule(
            StringBuilder builder,
            LoadingIndicatorTokens tokens
    ) {
        beginRule(builder, ".m3-loading-indicator");
        appendDeclaration(builder, "-m3-container-size", M3TokenCss.pixels(tokens.containerSize()));
        appendDeclaration(builder, "-m3-indicator-size", M3TokenCss.pixels(tokens.indicatorSize()));
        endRule(builder);
    }

    /// Appends a surface token CSS rule.
    private static void appendSurfaceRule(StringBuilder builder, SurfaceTokens tokens) {
        beginRule(builder, ".m3-surface");
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a carousel track token CSS rule.
    private static void appendCarouselTrackRule(StringBuilder builder, CarouselTokens tokens) {
        beginRule(builder, ".m3-carousel-track");
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.trackPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a carousel item token CSS rule.
    private static void appendCarouselItemRule(StringBuilder builder, CarouselTokens tokens) {
        beginRule(builder, ".m3-carousel-item");
        appendDeclaration(builder, "-fx-opacity", Double.toString(tokens.itemOpacity()));
        endRule(builder);
    }

    /// Appends a selected carousel item token CSS rule.
    private static void appendCarouselSelectedItemRule(StringBuilder builder, CarouselTokens tokens) {
        beginRule(builder, ".m3-carousel-selected-item");
        appendDeclaration(builder, "-fx-opacity", "1.0");
        appendDeclaration(
                builder,
                "-fx-effect",
                "dropshadow(gaussian, rgba(0,0,0,0.14), "
                        + M3TokenCss.pixels(tokens.selectedShadowRadius())
                        + ", "
                        + tokens.selectedShadowSpread()
                        + ", 0, "
                        + M3TokenCss.pixels(tokens.selectedShadowOffsetY())
                        + ")"
        );
        endRule(builder);
    }

    /// Appends a dialog pane token CSS rule.
    private static void appendDialogRule(StringBuilder builder, DialogTokens tokens) {
        beginRule(builder, ".m3-dialog-pane");
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-container-min-width", M3TokenCss.pixels(tokens.containerMinWidth()));
        appendDeclaration(builder, "-m3-container-max-width", M3TokenCss.pixels(tokens.containerMaxWidth()));
        appendDeclaration(builder, "-m3-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
        appendDeclaration(builder, "-m3-dialog-icon-size", M3TokenCss.pixels(tokens.iconSize()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a card token CSS rule.
    private static void appendCardRule(StringBuilder builder, CardTokens tokens) {
        beginRule(builder, ".m3-card");
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
        endRule(builder);
    }

    /// Appends a snackbar token CSS rule.
    private static void appendSnackbarRule(StringBuilder builder, SnackbarTokens tokens) {
        beginRule(builder, ".m3-snackbar");
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-container-min-width", M3TokenCss.pixels(tokens.containerMinWidth()));
        appendDeclaration(builder, "-m3-container-max-width", M3TokenCss.pixels(tokens.containerMaxWidth()));
        appendDeclaration(
                builder,
                "-m3-single-line-container-height",
                M3TokenCss.pixels(tokens.singleLineContainerHeight())
        );
        appendDeclaration(
                builder,
                "-m3-two-line-container-height",
                M3TokenCss.pixels(tokens.twoLineContainerHeight())
        );
        appendDeclaration(builder, "-m3-action-container-height", M3TokenCss.pixels(tokens.actionContainerHeight()));
        endRule(builder);

        beginRule(builder, ".m3-snackbar .m3-snackbar-action");
        appendDeclaration(builder, "-m3-container-height", "-m3-action-container-height");
        endRule(builder);
    }

    /// Appends a banner token CSS rule.
    private static void appendBannerRule(StringBuilder builder, BannerTokens tokens) {
        beginRule(builder, ".m3-banner");
        appendDeclaration(builder, "-m3-container-min-height", M3TokenCss.pixels(tokens.containerMinHeight()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        appendDeclaration(builder, "-m3-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
        endRule(builder);
    }

    /// Appends a plain tooltip token CSS rule.
    private static void appendTooltipRule(StringBuilder builder, TooltipTokens tokens) {
        String verticalPadding = M3TokenCss.pixels(tokens.plainVerticalPadding());
        String horizontalPadding = M3TokenCss.pixels(tokens.plainHorizontalPadding());
        beginRule(builder, ".m3-tooltip");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.plainContainerShape()));
        appendDeclaration(builder, "-fx-padding", verticalPadding + " " + horizontalPadding);
        endRule(builder);
    }

    /// Appends a rich tooltip container token CSS rule.
    private static void appendRichTooltipRule(StringBuilder builder, TooltipTokens tokens) {
        String horizontalPadding = M3TokenCss.pixels(tokens.richHorizontalPadding());
        String padding = M3TokenCss.pixels(tokens.richTopPadding())
                + " "
                + horizontalPadding
                + " "
                + M3TokenCss.pixels(tokens.richBottomPadding())
                + " "
                + horizontalPadding;
        beginRule(builder, ".m3-rich-tooltip-container");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.richContainerShape()));
        appendDeclaration(builder, "-fx-padding", padding);
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.richContentSpacing()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.richPreferredWidth()));
        endRule(builder);
    }

    /// Appends a rich tooltip actions token CSS rule.
    private static void appendRichTooltipActionsRule(StringBuilder builder, TooltipTokens tokens) {
        beginRule(builder, ".m3-rich-tooltip-actions");
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.richActionSpacing()));
        endRule(builder);
    }

    /// Appends a rich tooltip action button token CSS rule.
    private static void appendRichTooltipActionButtonRule(StringBuilder builder, TooltipTokens tokens) {
        beginRule(builder, ".m3-rich-tooltip-actions .m3-button");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.richActionButtonHeight()));
        appendDeclaration(
                builder,
                "-m3-horizontal-padding",
                M3TokenCss.pixels(tokens.richActionButtonHorizontalPadding())
        );
        endRule(builder);
    }

    /// Appends a divider token CSS rule.
    private static void appendDividerRule(StringBuilder builder, DividerTokens tokens) {
        beginRule(builder, ".m3-divider");
        appendDeclaration(builder, "-m3-thickness", M3TokenCss.pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-inset-start", M3TokenCss.pixels(tokens.insetStart()));
        appendDeclaration(builder, "-m3-inset-end", M3TokenCss.pixels(tokens.insetEnd()));
        endRule(builder);
    }

    /// Appends a badge token CSS rule.
    private static void appendBadgeRule(StringBuilder builder, BadgeTokens tokens) {
        beginRule(builder, ".m3-badge");
        appendDeclaration(builder, "-m3-small-size", M3TokenCss.pixels(tokens.smallSize()));
        appendDeclaration(builder, "-m3-large-height", M3TokenCss.pixels(tokens.largeHeight()));
        appendDeclaration(builder, "-m3-large-min-width", M3TokenCss.pixels(tokens.largeMinWidth()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends an avatar token CSS rule.
    private static void appendAvatarRule(StringBuilder builder, AvatarTokens tokens) {
        String size = M3TokenCss.pixels(tokens.containerSize());
        beginRule(builder, ".m3-avatar.m3-avatar");
        appendDeclaration(builder, "-m3-container-size", size);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a top app bar token CSS rule.
    private static void appendTopAppBarRule(StringBuilder builder, TopAppBarTokens tokens) {
        beginRule(builder, ".m3-top-app-bar");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-medium-container-height", M3TokenCss.pixels(tokens.mediumContainerHeight()));
        appendDeclaration(builder, "-m3-large-container-height", M3TokenCss.pixels(tokens.largeContainerHeight()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-medium-bottom-padding", M3TokenCss.pixels(tokens.mediumBottomPadding()));
        appendDeclaration(builder, "-m3-large-bottom-padding", M3TokenCss.pixels(tokens.largeBottomPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        appendDeclaration(builder, "-m3-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a top app bar variant token CSS rule.
    private static void appendTopAppBarVariantRule(
            StringBuilder builder,
            String selector,
            double containerHeight,
            double bottomPadding,
            TopAppBarTokens tokens
    ) {
        String horizontalPadding = M3TokenCss.pixels(tokens.horizontalPadding());
        String padding = "0 " + horizontalPadding + " " + M3TokenCss.pixels(bottomPadding) + " " + horizontalPadding;
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(containerHeight));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(containerHeight));
        appendDeclaration(builder, "-fx-padding", padding);
        endRule(builder);
    }

    /// Appends a bottom app bar token CSS rule.
    private static void appendBottomAppBarRule(StringBuilder builder, BottomAppBarTokens tokens) {
        beginRule(builder, ".m3-bottom-app-bar");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        appendDeclaration(builder, "-m3-action-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a toolbar token CSS rule.
    private static void appendToolbarRule(StringBuilder builder, ToolbarTokens tokens) {
        beginRule(builder, ".m3-toolbar");
        appendDeclaration(builder, "-m3-toolbar-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-toolbar-container-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-m3-toolbar-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-toolbar-item-slot-size", M3TokenCss.pixels(tokens.itemSlotSize()));
        appendDeclaration(builder, "-m3-toolbar-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-toolbar-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-container-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-item-slot-size", M3TokenCss.pixels(tokens.itemSlotSize()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        endRule(builder);

        beginRule(builder, ".m3-toolbar.m3-toolbar-standard");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);

        beginRule(builder, ".m3-toolbar.m3-toolbar-floating");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a navigation bar token CSS rule.
    private static void appendNavigationBarRule(StringBuilder builder, NavigationBarTokens tokens) {
        beginRule(builder, ".m3-navigation-bar");
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a navigation item token CSS rule.
    private static void appendNavigationItemRule(StringBuilder builder, NavigationBarTokens tokens) {
        beginRule(builder, ".m3-navigation-item");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-item-width", M3TokenCss.pixels(tokens.itemWidth()));
        appendDeclaration(builder, "-m3-indicator-width", M3TokenCss.pixels(tokens.indicatorWidth()));
        appendDeclaration(builder, "-m3-indicator-height", M3TokenCss.pixels(tokens.indicatorHeight()));
        appendDeclaration(builder, "-m3-indicator-shape", M3TokenCss.pixels(tokens.indicatorShape()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a navigation selected indicator token CSS rule.
    private static void appendNavigationIndicatorRule(StringBuilder builder, NavigationBarTokens tokens) {
        beginRule(builder, ".m3-navigation-item-indicator");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.indicatorShape()));
        endRule(builder);
    }

    /// Appends a navigation rail token CSS rule.
    private static void appendNavigationRailRule(StringBuilder builder, NavigationRailTokens tokens) {
        beginRule(builder, ".m3-navigation-rail");
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.verticalPadding())
                + " "
                + M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a navigation rail item token CSS rule.
    private static void appendNavigationRailItemRule(StringBuilder builder, NavigationRailTokens tokens) {
        beginRule(builder, ".m3-navigation-rail .m3-navigation-item");
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-item-width", M3TokenCss.pixels(tokens.itemWidth()));
        appendDeclaration(builder, "-m3-indicator-width", M3TokenCss.pixels(tokens.indicatorWidth()));
        appendDeclaration(builder, "-m3-indicator-height", M3TokenCss.pixels(tokens.indicatorHeight()));
        appendDeclaration(builder, "-m3-indicator-shape", M3TokenCss.pixels(tokens.indicatorShape()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a navigation rail selected indicator token CSS rule.
    private static void appendNavigationRailIndicatorRule(StringBuilder builder, NavigationRailTokens tokens) {
        beginRule(builder, ".m3-navigation-rail .m3-navigation-item-indicator");
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.indicatorShape()));
        endRule(builder);
    }

    /// Appends a navigation drawer token CSS rule.
    private static void appendNavigationDrawerRule(
            StringBuilder builder,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, ".m3-navigation-drawer");
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.containerPadding()));
        appendDeclaration(builder, "-m3-item-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a navigation drawer item token CSS rule.
    private static void appendNavigationDrawerItemRule(
            StringBuilder builder,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, ".m3-navigation-drawer .m3-list-item");
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.oneLineItemHeight()));
        appendDeclaration(builder, "-m3-two-line-height", M3TokenCss.pixels(tokens.twoLineItemHeight()));
        appendDeclaration(builder, "-m3-three-line-height", M3TokenCss.pixels(tokens.threeLineItemHeight()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.itemContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.itemHorizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(tokens.itemVerticalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.itemContentSpacing()));
        endRule(builder);
    }

    /// Appends a navigation drawer child group item token CSS rule.
    private static void appendNavigationDrawerGroupChildItemRule(
            StringBuilder builder,
            String selector,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.groupChildItemHeight()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.groupChildItemContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.groupChildItemHorizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(tokens.itemVerticalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.itemContentSpacing()));
        endRule(builder);
    }

    /// Appends a list item token CSS rule.
    private static void appendListItemRule(StringBuilder builder, ListItemTokens tokens) {
        beginRule(builder, ".m3-list-item");
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.oneLineHeight()));
        appendDeclaration(builder, "-m3-two-line-height", M3TokenCss.pixels(tokens.twoLineHeight()));
        appendDeclaration(builder, "-m3-three-line-height", M3TokenCss.pixels(tokens.threeLineHeight()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a list section header token CSS rule.
    private static void appendListSectionHeaderRule(StringBuilder builder, ListItemTokens tokens) {
        String height = M3TokenCss.pixels(tokens.sectionHeaderHeight());
        String horizontalPadding = M3TokenCss.pixels(tokens.sectionHeaderHorizontalPadding());
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

    /// Tokens shared by button variants.
    ///
    /// @param height the preferred button height
    /// @param containerShape the button container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    record ButtonTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates button tokens.
        public ButtonTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens used by the five Material button sizes.
    ///
    /// @param extraSmall the extra-small button size tokens
    /// @param small the small button size tokens
    /// @param medium the medium button size tokens
    /// @param large the large button size tokens
    /// @param extraLarge the extra-large button size tokens
    @NotNullByDefault
    record ButtonSizingTokens(
            ButtonSizeTokens extraSmall,
            ButtonSizeTokens small,
            ButtonSizeTokens medium,
            ButtonSizeTokens large,
            ButtonSizeTokens extraLarge
    ) {
        /// Creates a five-step button size token scale.
        public ButtonSizingTokens {
            Objects.requireNonNull(extraSmall, "extraSmall");
            Objects.requireNonNull(small, "small");
            Objects.requireNonNull(medium, "medium");
            Objects.requireNonNull(large, "large");
            Objects.requireNonNull(extraLarge, "extraLarge");
        }
    }

    /// Tokens used by one Material button size.
    ///
    /// @param containerHeight the visual container height
    /// @param iconSize the icon glyph size
    /// @param roundContainerShape the round resting container shape
    /// @param squareContainerShape the rounded-square resting container shape
    /// @param pressedContainerShape the pressed container shape
    /// @param horizontalPadding the leading and trailing padding for non-text variants
    /// @param textHorizontalPadding the leading and trailing padding for text variants
    /// @param iconLabelSpace the spacing between an icon and label
    /// @param outlineWidth the outlined variant stroke width
    @NotNullByDefault
    record ButtonSizeTokens(
            double containerHeight,
            double iconSize,
            double roundContainerShape,
            double squareContainerShape,
            double pressedContainerShape,
            double horizontalPadding,
            double textHorizontalPadding,
            double iconLabelSpace,
            double outlineWidth
    ) {
        /// Creates tokens for one Material button size.
        public ButtonSizeTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(iconSize, "iconSize");
            validateNonNegative(roundContainerShape, "roundContainerShape");
            validateNonNegative(squareContainerShape, "squareContainerShape");
            validateNonNegative(pressedContainerShape, "pressedContainerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(textHorizontalPadding, "textHorizontalPadding");
            validateNonNegative(iconLabelSpace, "iconLabelSpace");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }

    /// Tokens used by icon button size families.
    ///
    /// @param extraSmall the extra-small icon button size tokens
    /// @param small the small icon button size tokens
    /// @param medium the medium icon button size tokens
    /// @param large the large icon button size tokens
    /// @param extraLarge the extra-large icon button size tokens
    @NotNullByDefault
    record IconButtonTokens(
            IconButtonSizeTokens extraSmall,
            IconButtonSizeTokens small,
            IconButtonSizeTokens medium,
            IconButtonSizeTokens large,
            IconButtonSizeTokens extraLarge
    ) {
        /// Creates icon button tokens.
        public IconButtonTokens {
            Objects.requireNonNull(extraSmall, "extraSmall");
            Objects.requireNonNull(small, "small");
            Objects.requireNonNull(medium, "medium");
            Objects.requireNonNull(large, "large");
            Objects.requireNonNull(extraLarge, "extraLarge");
        }
    }

    /// Tokens used by one icon button size.
    ///
    /// @param containerHeight the visual container height
    /// @param iconSize the icon glyph size
    /// @param narrowWidth the narrow visual container width
    /// @param defaultWidth the default visual container width
    /// @param wideWidth the wide visual container width
    /// @param roundContainerShape the round resting container shape
    /// @param squareContainerShape the square resting container shape
    /// @param pressedContainerShape the pressed container shape
    /// @param selectedRoundContainerShape the selected shape for round toggle icon buttons
    /// @param selectedSquareContainerShape the selected shape for square toggle icon buttons
    /// @param outlineWidth the outlined variant stroke width
    @NotNullByDefault
    record IconButtonSizeTokens(
            double containerHeight,
            double iconSize,
            double narrowWidth,
            double defaultWidth,
            double wideWidth,
            double roundContainerShape,
            double squareContainerShape,
            double pressedContainerShape,
            double selectedRoundContainerShape,
            double selectedSquareContainerShape,
            double outlineWidth
    ) {
        /// Creates icon button size tokens.
        public IconButtonSizeTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(iconSize, "iconSize");
            validateNonNegative(narrowWidth, "narrowWidth");
            validateNonNegative(defaultWidth, "defaultWidth");
            validateNonNegative(wideWidth, "wideWidth");
            validateNonNegative(roundContainerShape, "roundContainerShape");
            validateNonNegative(squareContainerShape, "squareContainerShape");
            validateNonNegative(pressedContainerShape, "pressedContainerShape");
            validateNonNegative(selectedRoundContainerShape, "selectedRoundContainerShape");
            validateNonNegative(selectedSquareContainerShape, "selectedSquareContainerShape");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }
    /// Tokens shared by floating action button sizes.
    ///
    /// @param smallSize the small floating action button square size
    /// @param regularSize the regular floating action button square size
    /// @param largeSize the large floating action button square size
    /// @param smallShape the small floating action button corner radius
    /// @param regularShape the regular floating action button corner radius
    /// @param largeShape the large floating action button corner radius
    /// @param smallHorizontalPadding the horizontal padding for small extended floating action buttons
    /// @param regularHorizontalPadding the horizontal padding for regular extended floating action buttons
    /// @param largeHorizontalPadding the horizontal padding for large extended floating action buttons
    /// @param menuActionSpacing the vertical spacing between expanded FAB menu actions
    @NotNullByDefault
    record FabTokens(
            double smallSize,
            double regularSize,
            double largeSize,
            double smallShape,
            double regularShape,
            double largeShape,
            double smallHorizontalPadding,
            double regularHorizontalPadding,
            double largeHorizontalPadding,
            double menuActionSpacing
    ) {
        /// Creates floating action button tokens.
        public FabTokens {
            validateNonNegative(smallSize, "smallSize");
            validateNonNegative(regularSize, "regularSize");
            validateNonNegative(largeSize, "largeSize");
            validateNonNegative(smallShape, "smallShape");
            validateNonNegative(regularShape, "regularShape");
            validateNonNegative(largeShape, "largeShape");
            validateNonNegative(smallHorizontalPadding, "smallHorizontalPadding");
            validateNonNegative(regularHorizontalPadding, "regularHorizontalPadding");
            validateNonNegative(largeHorizontalPadding, "largeHorizontalPadding");
            validateNonNegative(menuActionSpacing, "menuActionSpacing");
        }
    }

    /// Tokens shared by icon size roles.
    ///
    /// @param smallSize the small icon glyph size
    /// @param mediumSize the medium icon glyph size
    /// @param largeSize the large icon glyph size
    /// @param extraLargeSize the extra-large icon glyph size
    @NotNullByDefault
    record IconTokens(
            double smallSize,
            double mediumSize,
            double largeSize,
            double extraLargeSize
    ) {
        /// Creates icon tokens.
        public IconTokens {
            validateNonNegative(smallSize, "smallSize");
            validateNonNegative(mediumSize, "mediumSize");
            validateNonNegative(largeSize, "largeSize");
            validateNonNegative(extraLargeSize, "extraLargeSize");
        }
    }

    /// The five-step Material button-group token scale.
    ///
    /// @param extraSmall tokens for extra-small button groups
    /// @param small tokens for small button groups
    /// @param medium tokens for medium button groups
    /// @param large tokens for large button groups
    /// @param extraLarge tokens for extra-large button groups
    /// @param segmentedGroupSpacing the spacing between segmented button group children
    /// @param iconToggleGroupSpacing the spacing between icon toggle button group children
    @NotNullByDefault
    record ButtonGroupTokens(
            ButtonGroupSizeTokens extraSmall,
            ButtonGroupSizeTokens small,
            ButtonGroupSizeTokens medium,
            ButtonGroupSizeTokens large,
            ButtonGroupSizeTokens extraLarge,
            double segmentedGroupSpacing,
            double iconToggleGroupSpacing
    ) {
        /// Creates a complete button-group token scale.
        public ButtonGroupTokens {
            Objects.requireNonNull(extraSmall, "extraSmall");
            Objects.requireNonNull(small, "small");
            Objects.requireNonNull(medium, "medium");
            Objects.requireNonNull(large, "large");
            Objects.requireNonNull(extraLarge, "extraLarge");
            validateFinite(segmentedGroupSpacing, "segmentedGroupSpacing");
            validateNonNegative(iconToggleGroupSpacing, "iconToggleGroupSpacing");
        }
    }

    /// Material button-group metrics for one size role.
    ///
    /// @param containerHeight the group container height
    /// @param standardSpacing the spacing between standard-group children
    /// @param connectedSpacing the spacing between connected-group children
    /// @param connectedInnerCorner the resting connected inner-corner radius
    /// @param connectedPressedInnerCorner the pressed connected inner-corner radius
    /// @param connectedSelectedInnerCorner the selected connected inner-corner radius
    @NotNullByDefault
    record ButtonGroupSizeTokens(
            double containerHeight,
            double standardSpacing,
            double connectedSpacing,
            double connectedInnerCorner,
            double connectedPressedInnerCorner,
            double connectedSelectedInnerCorner
    ) {
        /// Creates button-group tokens for one size role.
        public ButtonGroupSizeTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateFinite(standardSpacing, "standardSpacing");
            validateFinite(connectedSpacing, "connectedSpacing");
            validateNonNegative(connectedInnerCorner, "connectedInnerCorner");
            validateNonNegative(connectedPressedInnerCorner, "connectedPressedInnerCorner");
            validateNonNegative(connectedSelectedInnerCorner, "connectedSelectedInnerCorner");
        }
    }

    /// The five-step Material split-button size token scale.
    ///
    /// @param extraSmall tokens for extra-small split buttons
    /// @param small tokens for small split buttons
    /// @param medium tokens for medium split buttons
    /// @param large tokens for large split buttons
    /// @param extraLarge tokens for extra-large split buttons
    @NotNullByDefault
    record SplitButtonTokens(
            SplitButtonSizeTokens extraSmall,
            SplitButtonSizeTokens small,
            SplitButtonSizeTokens medium,
            SplitButtonSizeTokens large,
            SplitButtonSizeTokens extraLarge
    ) {
        /// Creates a complete split-button size token scale.
        public SplitButtonTokens {
            Objects.requireNonNull(extraSmall, "extraSmall");
            Objects.requireNonNull(small, "small");
            Objects.requireNonNull(medium, "medium");
            Objects.requireNonNull(large, "large");
            Objects.requireNonNull(extraLarge, "extraLarge");
        }
    }

    /// Material split-button metrics for one size role.
    ///
    /// @param containerHeight the split-button container height
    /// @param spacing the spacing between the leading and trailing button parts
    /// @param innerCorner the resting inner-corner radius
    /// @param hoveredInnerCorner the hovered and focused inner-corner radius
    /// @param pressedInnerCorner the pressed inner-corner radius
    /// @param actionLeadingSpace the logical leading padding of the primary action
    /// @param actionTrailingSpace the logical trailing padding of the primary action
    /// @param menuIconSize the trailing menu icon viewport size
    /// @param menuIconOffset the unselected menu icon offset toward the group center
    /// @param menuLeadingSpace the logical leading space around the trailing icon
    /// @param menuTrailingSpace the logical trailing space around the trailing icon
    /// @param selectedInnerCorner the inner-corner radius while the menu is showing
    @NotNullByDefault
    record SplitButtonSizeTokens(
            double containerHeight,
            double spacing,
            double innerCorner,
            double hoveredInnerCorner,
            double pressedInnerCorner,
            double actionLeadingSpace,
            double actionTrailingSpace,
            double menuIconSize,
            double menuIconOffset,
            double menuLeadingSpace,
            double menuTrailingSpace,
            double selectedInnerCorner
    ) {
        /// Validates split-button metrics for one size role.
        public SplitButtonSizeTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateFinite(spacing, "spacing");
            validateNonNegative(innerCorner, "innerCorner");
            validateNonNegative(hoveredInnerCorner, "hoveredInnerCorner");
            validateNonNegative(pressedInnerCorner, "pressedInnerCorner");
            validateNonNegative(actionLeadingSpace, "actionLeadingSpace");
            validateNonNegative(actionTrailingSpace, "actionTrailingSpace");
            validateNonNegative(menuIconSize, "menuIconSize");
            validateNonNegative(menuIconOffset, "menuIconOffset");
            validateNonNegative(menuLeadingSpace, "menuLeadingSpace");
            validateNonNegative(menuTrailingSpace, "menuTrailingSpace");
            validateNonNegative(selectedInnerCorner, "selectedInnerCorner");
        }
    }

    /// Tokens used by tabs.
    ///
    /// @param containerHeight the tab container height
    /// @param tabMinWidth the tab minimum width
    /// @param horizontalPadding the horizontal content padding
    /// @param activeIndicatorHeight the active indicator height
    /// @param activeIndicatorShape the active indicator radius
    @NotNullByDefault
    record TabTokens(
            double containerHeight,
            double tabMinWidth,
            double horizontalPadding,
            double activeIndicatorHeight,
            double activeIndicatorShape
    ) {
        /// Creates tab tokens.
        public TabTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(tabMinWidth, "tabMinWidth");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(activeIndicatorHeight, "activeIndicatorHeight");
            validateNonNegative(activeIndicatorShape, "activeIndicatorShape");
        }
    }

    /// Tokens shared by text input controls.
    ///
    /// @param height the preferred field height
    /// @param containerShape the field container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    record FieldTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates field tokens.
        public FieldTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Component tokens for text area controls.
    ///
    /// @param height the preferred text area container height
    /// @param containerShape the text area container corner radius
    /// @param horizontalPadding the horizontal content padding
    /// @param verticalPadding the vertical content padding
    @NotNullByDefault
    record TextAreaTokens(
            double height,
            double containerShape,
            double horizontalPadding,
            double verticalPadding
    ) {
        /// Validates text area tokens.
        public TextAreaTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(verticalPadding, "verticalPadding");
        }
    }

    /// Component tokens for form containers.
    ///
    /// @param contentPadding the uniform padding around top-level form content
    /// @param rowSpacing the vertical spacing between top-level form rows and sections
    /// @param sectionContentSpacing the vertical spacing between section content nodes
    /// @param sectionHeaderSpacing the vertical spacing between section title and supporting text
    /// @param sectionHeaderBottomPadding the bottom padding below a section header
    /// @param rowLabelWidth the width reserved for form row labels
    /// @param rowColumnSpacing the horizontal spacing between row label, content, and trailing regions
    /// @param rowMinHeight the minimum height of each form row
    /// @param rowTextSpacing the vertical spacing between row label and supporting text
    @NotNullByDefault
    record FormTokens(
            double contentPadding,
            double rowSpacing,
            double sectionContentSpacing,
            double sectionHeaderSpacing,
            double sectionHeaderBottomPadding,
            double rowLabelWidth,
            double rowColumnSpacing,
            double rowMinHeight,
            double rowTextSpacing
    ) {
        /// Validates form tokens.
        public FormTokens {
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(rowSpacing, "rowSpacing");
            validateNonNegative(sectionContentSpacing, "sectionContentSpacing");
            validateNonNegative(sectionHeaderSpacing, "sectionHeaderSpacing");
            validateNonNegative(sectionHeaderBottomPadding, "sectionHeaderBottomPadding");
            validateNonNegative(rowLabelWidth, "rowLabelWidth");
            validateNonNegative(rowColumnSpacing, "rowColumnSpacing");
            validateNonNegative(rowMinHeight, "rowMinHeight");
            validateNonNegative(rowTextSpacing, "rowTextSpacing");
        }
    }

    /// Component tokens for validation summaries.
    ///
    /// @param containerShape the summary container corner radius
    /// @param contentPadding the uniform summary content padding
    /// @param itemsSpacing the vertical spacing between invalid item rows
    /// @param itemShape the invalid item state container corner radius
    /// @param itemVerticalPadding the vertical padding inside each invalid item
    /// @param itemHorizontalPadding the horizontal padding inside each invalid item
    @NotNullByDefault
    record ValidationSummaryTokens(
            double containerShape,
            double contentPadding,
            double itemsSpacing,
            double itemShape,
            double itemVerticalPadding,
            double itemHorizontalPadding
    ) {
        /// Validates validation summary tokens.
        public ValidationSummaryTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(itemsSpacing, "itemsSpacing");
            validateNonNegative(itemShape, "itemShape");
            validateNonNegative(itemVerticalPadding, "itemVerticalPadding");
            validateNonNegative(itemHorizontalPadding, "itemHorizontalPadding");
        }
    }

    /// Component tokens for menus.
    ///
    /// @param containerShape the menu container corner radius
    /// @param containerPadding the padding around menu items
    /// @param itemHeight the one-line menu item height
    /// @param itemContainerShape the menu item state container corner radius
    /// @param selectedItemContainerShape the selected menu item state container corner radius
    /// @param firstItemContainerShape the first direct menu item state container corner radius
    /// @param lastItemContainerShape the last direct menu item state container corner radius
    /// @param itemHorizontalPadding the horizontal item content padding
    /// @param itemContentSpacing the spacing between item content regions
    /// @param itemSpacing the vertical spacing between direct menu items
    @NotNullByDefault
    record MenuTokens(
            double containerShape,
            double containerPadding,
            double itemHeight,
            double itemContainerShape,
            double selectedItemContainerShape,
            double firstItemContainerShape,
            double lastItemContainerShape,
            double itemHorizontalPadding,
            double itemContentSpacing,
            double itemSpacing
    ) {
        /// Validates menu tokens.
        public MenuTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(containerPadding, "containerPadding");
            validateNonNegative(itemHeight, "itemHeight");
            validateNonNegative(itemContainerShape, "itemContainerShape");
            validateNonNegative(selectedItemContainerShape, "selectedItemContainerShape");
            validateNonNegative(firstItemContainerShape, "firstItemContainerShape");
            validateNonNegative(lastItemContainerShape, "lastItemContainerShape");
            validateNonNegative(itemHorizontalPadding, "itemHorizontalPadding");
            validateNonNegative(itemContentSpacing, "itemContentSpacing");
            validateNonNegative(itemSpacing, "itemSpacing");
        }
    }

    /// Component tokens for search components.
    ///
    /// @param barHeight the search bar container height
    /// @param barContainerShape the search bar container corner radius
    /// @param barHorizontalPadding the search bar horizontal content padding
    /// @param barContentSpacing the spacing between search bar content regions
    /// @param barTrailingActionsGap the spacing between trailing search bar actions
    /// @param viewContainerShape the expanded search view corner radius
    /// @param viewHorizontalPadding the horizontal padding around contained search view content
    /// @param viewBarResultsGap the gap between the embedded search bar and results container
    /// @param viewResultsShape the search results container corner radius
    /// @param resultContainerShape the search result row state container corner radius
    /// @param viewResultPadding the padding below search results
    /// @param resultHeight the one-line search result item height
    /// @param resultHorizontalPadding the horizontal search result row padding
    /// @param resultContentSpacing the spacing between search result content regions
    @NotNullByDefault
    record SearchTokens(
            double barHeight,
            double barContainerShape,
            double barHorizontalPadding,
            double barContentSpacing,
            double barTrailingActionsGap,
            double viewContainerShape,
            double viewHorizontalPadding,
            double viewBarResultsGap,
            double viewResultsShape,
            double resultContainerShape,
            double viewResultPadding,
            double resultHeight,
            double resultHorizontalPadding,
            double resultContentSpacing
    ) {
        /// Validates search tokens.
        public SearchTokens {
            validateNonNegative(barHeight, "barHeight");
            validateNonNegative(barContainerShape, "barContainerShape");
            validateNonNegative(barHorizontalPadding, "barHorizontalPadding");
            validateNonNegative(barContentSpacing, "barContentSpacing");
            validateNonNegative(barTrailingActionsGap, "barTrailingActionsGap");
            validateNonNegative(viewContainerShape, "viewContainerShape");
            validateNonNegative(viewHorizontalPadding, "viewHorizontalPadding");
            validateNonNegative(viewBarResultsGap, "viewBarResultsGap");
            validateNonNegative(viewResultsShape, "viewResultsShape");
            validateNonNegative(resultContainerShape, "resultContainerShape");
            validateNonNegative(viewResultPadding, "viewResultPadding");
            validateNonNegative(resultHeight, "resultHeight");
            validateNonNegative(resultHorizontalPadding, "resultHorizontalPadding");
            validateNonNegative(resultContentSpacing, "resultContentSpacing");
        }
    }

    /// Component tokens for picker fields and their preset popup surfaces.
    ///
    /// @param openButtonSize the trailing open button size
    /// @param openButtonShape the trailing open button radius
    /// @param popupShape the popup surface radius
    /// @param popupPadding the popup preset content padding
    /// @param popupSpacing the spacing between popup preset list and picker content
    /// @param presetListWidth the preset list preferred width
    /// @param presetListSpacing the spacing between preset buttons
    /// @param presetButtonHorizontalPadding the preset button horizontal padding
    @NotNullByDefault
    record PickerFieldTokens(
            double openButtonSize,
            double openButtonShape,
            double popupShape,
            double popupPadding,
            double popupSpacing,
            double presetListWidth,
            double presetListSpacing,
            double presetButtonHorizontalPadding
    ) {
        /// Validates picker field tokens.
        public PickerFieldTokens {
            validateNonNegative(openButtonSize, "openButtonSize");
            validateNonNegative(openButtonShape, "openButtonShape");
            validateNonNegative(popupShape, "popupShape");
            validateNonNegative(popupPadding, "popupPadding");
            validateNonNegative(popupSpacing, "popupSpacing");
            validateNonNegative(presetListWidth, "presetListWidth");
            validateNonNegative(presetListSpacing, "presetListSpacing");
            validateNonNegative(presetButtonHorizontalPadding, "presetButtonHorizontalPadding");
        }
    }

    /// Component tokens for date pickers.
    ///
    /// @param containerShape the picker container radius
    /// @param containerPadding the picker container padding
    /// @param containerSpacing the spacing between container rows
    /// @param headerSpacing the spacing between header controls
    /// @param navigationButtonSize the previous and next month button size
    /// @param navigationButtonShape the previous and next month button radius
    /// @param dayCellSize the day and weekday cell size
    /// @param dayCellShape the selected day and range endpoint radius
    /// @param gridGap the day grid gap
    @NotNullByDefault
    record DatePickerTokens(
            double containerShape,
            double containerPadding,
            double containerSpacing,
            double headerSpacing,
            double navigationButtonSize,
            double navigationButtonShape,
            double dayCellSize,
            double dayCellShape,
            double gridGap
    ) {
        /// Validates date picker tokens.
        public DatePickerTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(containerPadding, "containerPadding");
            validateNonNegative(containerSpacing, "containerSpacing");
            validateNonNegative(headerSpacing, "headerSpacing");
            validateNonNegative(navigationButtonSize, "navigationButtonSize");
            validateNonNegative(navigationButtonShape, "navigationButtonShape");
            validateNonNegative(dayCellSize, "dayCellSize");
            validateNonNegative(dayCellShape, "dayCellShape");
            validateNonNegative(gridGap, "gridGap");
        }
    }

    /// Component tokens for time pickers.
    ///
    /// @param containerShape the picker container radius
    /// @param containerPadding the picker container padding
    /// @param containerSpacing the spacing between container rows
    /// @param displaySpacing the spacing between selected time display fields
    /// @param displayCellShape the selected time display field radius
    /// @param displayCellWidth the selected hour or minute display width
    /// @param displayCellHeight the selected hour or minute display height
    /// @param sectionSpacing the spacing inside hour and minute sections
    /// @param gridGap the selectable time grid gap
    /// @param cellWidth the hour and minute selectable cell width
    /// @param cellHeight the selectable cell height
    /// @param periodCellWidth the AM and PM selectable cell width
    /// @param cellShape the selectable cell radius
    @NotNullByDefault
    record TimePickerTokens(
            double containerShape,
            double containerPadding,
            double containerSpacing,
            double displaySpacing,
            double displayCellShape,
            double displayCellWidth,
            double displayCellHeight,
            double sectionSpacing,
            double gridGap,
            double cellWidth,
            double cellHeight,
            double periodCellWidth,
            double cellShape
    ) {
        /// Validates time picker tokens.
        public TimePickerTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(containerPadding, "containerPadding");
            validateNonNegative(containerSpacing, "containerSpacing");
            validateNonNegative(displaySpacing, "displaySpacing");
            validateNonNegative(displayCellShape, "displayCellShape");
            validateNonNegative(displayCellWidth, "displayCellWidth");
            validateNonNegative(displayCellHeight, "displayCellHeight");
            validateNonNegative(sectionSpacing, "sectionSpacing");
            validateNonNegative(gridGap, "gridGap");
            validateNonNegative(cellWidth, "cellWidth");
            validateNonNegative(cellHeight, "cellHeight");
            validateNonNegative(periodCellWidth, "periodCellWidth");
            validateNonNegative(cellShape, "cellShape");
        }
    }

    /// Component tokens for sheet containers.
    ///
    /// @param sideContainerWidth the side sheet container width
    /// @param sideContainerShape the side sheet leading corner radius
    /// @param bottomContainerHeight the bottom sheet container height
    /// @param bottomContainerShape the bottom sheet top corner radius
    /// @param contentPadding the sheet content padding
    /// @param headerPadding the sheet header edge padding
    /// @param dragHandleWidth the bottom sheet drag handle width
    /// @param dragHandleHeight the bottom sheet drag handle height
    @NotNullByDefault
    record SheetTokens(
            double sideContainerWidth,
            double sideContainerShape,
            double bottomContainerHeight,
            double bottomContainerShape,
            double contentPadding,
            double headerPadding,
            double dragHandleWidth,
            double dragHandleHeight
    ) {
        /// Validates sheet tokens.
        public SheetTokens {
            validateNonNegative(sideContainerWidth, "sideContainerWidth");
            validateNonNegative(sideContainerShape, "sideContainerShape");
            validateNonNegative(bottomContainerHeight, "bottomContainerHeight");
            validateNonNegative(bottomContainerShape, "bottomContainerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(headerPadding, "headerPadding");
            validateNonNegative(dragHandleWidth, "dragHandleWidth");
            validateNonNegative(dragHandleHeight, "dragHandleHeight");
        }
    }

    /// Component tokens for scrims.
    ///
    /// @param containerOpacity the scrim container opacity
    @NotNullByDefault
    record ScrimTokens(double containerOpacity) {
        /// Validates scrim tokens.
        public ScrimTokens {
            validateOpacity(containerOpacity);
        }
    }

    /// Tokens shared by selection controls.
    ///
    /// @param touchTargetSize the preferred checkbox and radio touch target size
    /// @param stateLayerSize the checkbox and radio indicator state layer size
    /// @param checkboxContainerSize the checkbox container size
    /// @param checkboxSelectedMarkWidth the selected checkbox mark width
    /// @param checkboxSelectedMarkHeight the selected checkbox mark height
    /// @param checkboxIndeterminateMarkWidth the indeterminate checkbox mark width
    /// @param checkboxIndeterminateMarkHeight the indeterminate checkbox mark height
    /// @param radioContainerSize the radio indicator container size
    /// @param radioSelectedDotSize the selected radio dot size
    /// @param trackShape the switch track radius
    /// @param switchTouchTargetSize the preferred switch touch target size
    /// @param switchTrackWidth the switch track width
    /// @param switchTrackHeight the switch track height
    /// @param switchStateLayerSize the switch state layer size
    /// @param switchUnselectedHandleSize the unselected switch handle size
    /// @param switchSelectedHandleSize the selected switch handle size
    /// @param switchPressedHandleSize the pressed switch handle size
    @NotNullByDefault
    record SelectionTokens(
            double touchTargetSize,
            double stateLayerSize,
            double checkboxContainerSize,
            double checkboxSelectedMarkWidth,
            double checkboxSelectedMarkHeight,
            double checkboxIndeterminateMarkWidth,
            double checkboxIndeterminateMarkHeight,
            double radioContainerSize,
            double radioSelectedDotSize,
            double trackShape,
            double switchTouchTargetSize,
            double switchTrackWidth,
            double switchTrackHeight,
            double switchStateLayerSize,
            double switchUnselectedHandleSize,
            double switchSelectedHandleSize,
            double switchPressedHandleSize
    ) {
        /// Creates selection tokens.
        public SelectionTokens {
            validateNonNegative(touchTargetSize, "touchTargetSize");
            validateNonNegative(stateLayerSize, "stateLayerSize");
            validateNonNegative(checkboxContainerSize, "checkboxContainerSize");
            validateNonNegative(checkboxSelectedMarkWidth, "checkboxSelectedMarkWidth");
            validateNonNegative(checkboxSelectedMarkHeight, "checkboxSelectedMarkHeight");
            validateNonNegative(checkboxIndeterminateMarkWidth, "checkboxIndeterminateMarkWidth");
            validateNonNegative(checkboxIndeterminateMarkHeight, "checkboxIndeterminateMarkHeight");
            validateNonNegative(radioContainerSize, "radioContainerSize");
            validateNonNegative(radioSelectedDotSize, "radioSelectedDotSize");
            validateNonNegative(trackShape, "trackShape");
            validateNonNegative(switchTouchTargetSize, "switchTouchTargetSize");
            validateNonNegative(switchTrackWidth, "switchTrackWidth");
            validateNonNegative(switchTrackHeight, "switchTrackHeight");
            validateNonNegative(switchStateLayerSize, "switchStateLayerSize");
            validateNonNegative(switchUnselectedHandleSize, "switchUnselectedHandleSize");
            validateNonNegative(switchSelectedHandleSize, "switchSelectedHandleSize");
            validateNonNegative(switchPressedHandleSize, "switchPressedHandleSize");
        }
    }

    /// Tokens shared by sliders.
    ///
    /// @param trackThickness the slider track thickness
    /// @param trackShape the slider track radius
    /// @param stopIndicatorSize the diameter of the inactive-track stop indicator
    /// @param thumbSize the slider handle long-side size
    /// @param thumbWidth the slider handle short-side width
    /// @param thumbTrackGap the gap between the handle and each adjacent track segment
    /// @param touchTargetSize the preferred slider touch target size
    @NotNullByDefault
    record SliderTokens(
            double trackThickness,
            double trackShape,
            double stopIndicatorSize,
            double thumbSize,
            double thumbWidth,
            double thumbTrackGap,
            double touchTargetSize
    ) {
        /// Creates slider tokens.
        public SliderTokens {
            validateNonNegative(trackThickness, "trackThickness");
            validateNonNegative(trackShape, "trackShape");
            validateNonNegative(stopIndicatorSize, "stopIndicatorSize");
            validateNonNegative(thumbSize, "thumbSize");
            validateNonNegative(thumbWidth, "thumbWidth");
            validateNonNegative(thumbTrackGap, "thumbTrackGap");
            validateNonNegative(touchTargetSize, "touchTargetSize");
        }
    }

    /// Tokens shared by chip variants.
    ///
    /// @param height the preferred chip height
    /// @param containerShape the chip container radius
    /// @param horizontalPadding the horizontal content padding for chips without a leading graphic
    /// @param iconHorizontalPadding the horizontal content padding for chips with a leading graphic
    /// @param elementSpacing the spacing between chip content elements
    /// @param iconSize the size of a leading or trailing icon
    /// @param avatarSize the size of a leading avatar image
    /// @param avatarShape the corner radius used for avatar images
    /// @param outlineWidth the outline stroke width for flat unselected chips
    /// @param groupHorizontalGap the horizontal gap between chips in a chip group
    /// @param groupVerticalGap the vertical gap between wrapped rows in a chip group
    @NotNullByDefault
    record ChipTokens(
            double height,
            double containerShape,
            double horizontalPadding,
            double iconHorizontalPadding,
            double elementSpacing,
            double iconSize,
            double avatarSize,
            double avatarShape,
            double outlineWidth,
            double groupHorizontalGap,
            double groupVerticalGap
    ) {
        /// Creates chip tokens.
        public ChipTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(iconHorizontalPadding, "iconHorizontalPadding");
            validateNonNegative(elementSpacing, "elementSpacing");
            validateNonNegative(iconSize, "iconSize");
            validateNonNegative(avatarSize, "avatarSize");
            validateNonNegative(avatarShape, "avatarShape");
            validateNonNegative(outlineWidth, "outlineWidth");
            validateNonNegative(groupHorizontalGap, "groupHorizontalGap");
            validateNonNegative(groupVerticalGap, "groupVerticalGap");
        }
    }

    /// Tokens shared by progress indicators.
    ///
    /// @param thickness the default track thickness
    /// @param shape the progress indicator radius
    /// @param indicatorSize the circular indicator size
    /// @param linearWaveAmplitude the linear wavy indicator amplitude
    /// @param linearWavelength the linear wavy indicator wavelength
    /// @param linearTrackGap the gap between the linear active indicator and track
    /// @param linearStopSize the stop indicator diameter at the end of the linear track
    /// @param circularWaveAmplitude the circular wavy indicator amplitude
    /// @param circularWavelength the circular wavy indicator wavelength
    /// @param circularTrackGap the gap between the circular active indicator and track
    @NotNullByDefault
    record ProgressTokens(
            double thickness,
            double shape,
            double indicatorSize,
            double linearWaveAmplitude,
            double linearWavelength,
            double linearTrackGap,
            double linearStopSize,
            double circularWaveAmplitude,
            double circularWavelength,
            double circularTrackGap
    ) {
        /// Creates progress tokens.
        public ProgressTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(shape, "shape");
            validateNonNegative(indicatorSize, "indicatorSize");
            validateNonNegative(linearWaveAmplitude, "linearWaveAmplitude");
            validateNonNegative(linearWavelength, "linearWavelength");
            validateNonNegative(linearTrackGap, "linearTrackGap");
            validateNonNegative(linearStopSize, "linearStopSize");
            validateNonNegative(circularWaveAmplitude, "circularWaveAmplitude");
            validateNonNegative(circularWavelength, "circularWavelength");
            validateNonNegative(circularTrackGap, "circularTrackGap");
        }
    }

    /// Tokens used by loading indicators.
    ///
    /// @param containerSize the loading indicator container size
    /// @param indicatorSize the active indicator shape size
    @NotNullByDefault
    record LoadingIndicatorTokens(
            double containerSize,
            double indicatorSize
    ) {
        /// Creates loading indicator tokens.
        public LoadingIndicatorTokens {
            validateNonNegative(containerSize, "containerSize");
            validateNonNegative(indicatorSize, "indicatorSize");
        }
    }

    /// Tokens used by surfaces.
    ///
    /// @param containerShape the surface container radius
    /// @param contentPadding the surface content padding
    @NotNullByDefault
    record SurfaceTokens(
            double containerShape,
            double contentPadding
    ) {
        /// Creates surface tokens.
        public SurfaceTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
        }
    }

    /// Tokens used by carousels.
    ///
    /// @param trackPadding the padding around the carousel item track
    /// @param itemSpacing the spacing between carousel items
    /// @param itemOpacity the default opacity for unselected carousel items
    /// @param selectedShadowRadius the selected item shadow radius
    /// @param selectedShadowSpread the selected item shadow spread
    /// @param selectedShadowOffsetY the selected item shadow vertical offset
    @NotNullByDefault
    record CarouselTokens(
            double trackPadding,
            double itemSpacing,
            double itemOpacity,
            double selectedShadowRadius,
            double selectedShadowSpread,
            double selectedShadowOffsetY
    ) {
        /// Creates carousel tokens.
        public CarouselTokens {
            validateNonNegative(trackPadding, "trackPadding");
            validateNonNegative(itemSpacing, "itemSpacing");
            validateOpacity(itemOpacity);
            validateNonNegative(selectedShadowRadius, "selectedShadowRadius");
            validateNonNegative(selectedShadowSpread, "selectedShadowSpread");
            validateNonNegative(selectedShadowOffsetY, "selectedShadowOffsetY");
        }
    }

    /// Tokens used by cards.
    ///
    /// @param containerShape the card container radius
    /// @param contentPadding the card content padding
    /// @param outlineWidth the outlined card border width
    @NotNullByDefault
    record CardTokens(
            double containerShape,
            double contentPadding,
            double outlineWidth
    ) {
        /// Creates card tokens.
        public CardTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }

    /// Tokens used by dialogs.
    ///
    /// @param containerShape the dialog container radius
    /// @param contentPadding the dialog content padding
    /// @param containerMinWidth the minimum dialog container width
    /// @param containerMaxWidth the maximum dialog container width
    /// @param actionSpacing the spacing between dialog action buttons
    /// @param iconSize the dialog graphic icon size
    @NotNullByDefault
    record DialogTokens(
            double containerShape,
            double contentPadding,
            double containerMinWidth,
            double containerMaxWidth,
            double actionSpacing,
            double iconSize
    ) {
        /// Creates dialog tokens.
        public DialogTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(containerMinWidth, "containerMinWidth");
            validateNonNegative(containerMaxWidth, "containerMaxWidth");
            validateNonNegative(actionSpacing, "actionSpacing");
            validateNonNegative(iconSize, "iconSize");
            if (containerMaxWidth < containerMinWidth) {
                throw new IllegalArgumentException(
                        "containerMaxWidth must be greater than or equal to containerMinWidth"
                );
            }
        }
    }

    /// Tokens used by snackbar controls.
    ///
    /// @param containerShape the snackbar container radius
    /// @param contentPadding the snackbar content padding
    /// @param containerMinWidth the minimum snackbar container width
    /// @param containerMaxWidth the maximum snackbar container width
    /// @param singleLineContainerHeight the single-line snackbar container height
    /// @param twoLineContainerHeight the two-line snackbar container height
    /// @param actionContainerHeight the snackbar action button container height
    @NotNullByDefault
    record SnackbarTokens(
            double containerShape,
            double contentPadding,
            double containerMinWidth,
            double containerMaxWidth,
            double singleLineContainerHeight,
            double twoLineContainerHeight,
            double actionContainerHeight
    ) {
        /// Creates snackbar tokens.
        public SnackbarTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(containerMinWidth, "containerMinWidth");
            validateNonNegative(containerMaxWidth, "containerMaxWidth");
            validateNonNegative(singleLineContainerHeight, "singleLineContainerHeight");
            if (containerMaxWidth < containerMinWidth) {
                throw new IllegalArgumentException(
                        "containerMaxWidth must be greater than or equal to containerMinWidth"
                );
            }
            validateNonNegative(twoLineContainerHeight, "twoLineContainerHeight");
            validateNonNegative(actionContainerHeight, "actionContainerHeight");
        }
    }

    /// Tokens used by banners.
    ///
    /// @param containerMinHeight the minimum banner container height
    /// @param verticalPadding the vertical banner content padding
    /// @param horizontalPadding the horizontal banner content padding
    /// @param contentSpacing the spacing between icon, message, and actions
    /// @param actionSpacing the spacing between action nodes
    @NotNullByDefault
    record BannerTokens(
            double containerMinHeight,
            double verticalPadding,
            double horizontalPadding,
            double contentSpacing,
            double actionSpacing
    ) {
        /// Creates banner tokens.
        public BannerTokens {
            validateNonNegative(containerMinHeight, "containerMinHeight");
            validateNonNegative(verticalPadding, "verticalPadding");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(actionSpacing, "actionSpacing");
        }
    }

    /// Tokens used by plain and rich tooltips.
    ///
    /// @param plainContainerShape the plain tooltip container radius
    /// @param plainVerticalPadding the plain tooltip vertical content padding
    /// @param plainHorizontalPadding the plain tooltip horizontal content padding
    /// @param richContainerShape the rich tooltip container radius
    /// @param richTopPadding the rich tooltip top content padding
    /// @param richHorizontalPadding the rich tooltip horizontal content padding
    /// @param richBottomPadding the rich tooltip bottom content padding
    /// @param richContentSpacing the spacing between rich tooltip content rows
    /// @param richPreferredWidth the rich tooltip preferred content width
    /// @param richActionSpacing the spacing between rich tooltip action nodes
    /// @param richActionButtonHeight the rich tooltip action button container height
    /// @param richActionButtonHorizontalPadding the rich tooltip action button horizontal padding
    @NotNullByDefault
    record TooltipTokens(
            double plainContainerShape,
            double plainVerticalPadding,
            double plainHorizontalPadding,
            double richContainerShape,
            double richTopPadding,
            double richHorizontalPadding,
            double richBottomPadding,
            double richContentSpacing,
            double richPreferredWidth,
            double richActionSpacing,
            double richActionButtonHeight,
            double richActionButtonHorizontalPadding
    ) {
        /// Creates tooltip tokens.
        public TooltipTokens {
            validateNonNegative(plainContainerShape, "plainContainerShape");
            validateNonNegative(plainVerticalPadding, "plainVerticalPadding");
            validateNonNegative(plainHorizontalPadding, "plainHorizontalPadding");
            validateNonNegative(richContainerShape, "richContainerShape");
            validateNonNegative(richTopPadding, "richTopPadding");
            validateNonNegative(richHorizontalPadding, "richHorizontalPadding");
            validateNonNegative(richBottomPadding, "richBottomPadding");
            validateNonNegative(richContentSpacing, "richContentSpacing");
            validateNonNegative(richPreferredWidth, "richPreferredWidth");
            validateNonNegative(richActionSpacing, "richActionSpacing");
            validateNonNegative(richActionButtonHeight, "richActionButtonHeight");
            validateNonNegative(richActionButtonHorizontalPadding, "richActionButtonHorizontalPadding");
        }
    }

    /// Tokens used by dividers.
    ///
    /// @param thickness the divider line thickness
    /// @param insetStart the leading inset before the divider line
    /// @param insetEnd the trailing inset after the divider line
    @NotNullByDefault
    record DividerTokens(
            double thickness,
            double insetStart,
            double insetEnd
    ) {
        /// Creates divider tokens.
        public DividerTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(insetStart, "insetStart");
            validateNonNegative(insetEnd, "insetEnd");
        }
    }

    /// Tokens used by badges.
    ///
    /// @param smallSize the dot badge size
    /// @param largeHeight the text badge height
    /// @param largeMinWidth the text badge minimum width
    /// @param containerShape the text badge container radius
    /// @param horizontalPadding the text badge horizontal padding
    @NotNullByDefault
    record BadgeTokens(
            double smallSize,
            double largeHeight,
            double largeMinWidth,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates badge tokens.
        public BadgeTokens {
            validateNonNegative(smallSize, "smallSize");
            validateNonNegative(largeHeight, "largeHeight");
            validateNonNegative(largeMinWidth, "largeMinWidth");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens used by avatars.
    ///
    /// @param containerSize the avatar container size
    /// @param containerShape the avatar container radius
    @NotNullByDefault
    record AvatarTokens(
            double containerSize,
            double containerShape
    ) {
        /// Creates avatar tokens.
        public AvatarTokens {
            validateNonNegative(containerSize, "containerSize");
            validateNonNegative(containerShape, "containerShape");
        }
    }

    /// Tokens used by top app bars.
    ///
    /// @param containerHeight the small top app bar container height
    /// @param mediumContainerHeight the medium top app bar container height
    /// @param largeContainerHeight the large top app bar container height
    /// @param horizontalPadding the horizontal content padding
    /// @param mediumBottomPadding the medium top app bar bottom content padding
    /// @param largeBottomPadding the large top app bar bottom content padding
    /// @param contentSpacing the spacing between leading, title, and trailing regions
    /// @param actionSpacing the spacing between trailing action nodes
    @NotNullByDefault
    record TopAppBarTokens(
            double containerHeight,
            double mediumContainerHeight,
            double largeContainerHeight,
            double horizontalPadding,
            double mediumBottomPadding,
            double largeBottomPadding,
            double contentSpacing,
            double actionSpacing
    ) {
        /// Creates top app bar tokens.
        public TopAppBarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(mediumContainerHeight, "mediumContainerHeight");
            validateNonNegative(largeContainerHeight, "largeContainerHeight");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(mediumBottomPadding, "mediumBottomPadding");
            validateNonNegative(largeBottomPadding, "largeBottomPadding");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(actionSpacing, "actionSpacing");
        }
    }

    /// Tokens used by bottom app bars.
    ///
    /// @param containerHeight the bottom app bar container height
    /// @param horizontalPadding the horizontal content padding
    /// @param contentSpacing the spacing between action and floating action regions
    /// @param actionSpacing the spacing between action nodes
    @NotNullByDefault
    record BottomAppBarTokens(
            double containerHeight,
            double horizontalPadding,
            double contentSpacing,
            double actionSpacing
    ) {
        /// Creates bottom app bar tokens.
        public BottomAppBarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(actionSpacing, "actionSpacing");
        }
    }

    /// Tokens used by toolbars.
    ///
    /// @param containerHeight the horizontal toolbar container height
    /// @param containerWidth the vertical toolbar container width
    /// @param containerShape the toolbar container radius
    /// @param itemSlotSize the minimum action slot width and height
    /// @param contentPadding the padding around the toolbar item flow
    /// @param itemSpacing the spacing between action slots
    @NotNullByDefault
    record ToolbarTokens(
            double containerHeight,
            double containerWidth,
            double containerShape,
            double itemSlotSize,
            double contentPadding,
            double itemSpacing
    ) {
        /// Creates toolbar tokens.
        public ToolbarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(containerWidth, "containerWidth");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(itemSlotSize, "itemSlotSize");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(itemSpacing, "itemSpacing");
        }
    }

    /// Tokens used by navigation bars.
    ///
    /// @param containerHeight the navigation bar container height
    /// @param itemWidth the preferred navigation item width
    /// @param indicatorWidth the selected indicator width
    /// @param indicatorHeight the selected indicator height
    /// @param indicatorShape the selected indicator radius
    /// @param contentSpacing the spacing between item icon and label
    /// @param horizontalPadding the horizontal padding around items
    @NotNullByDefault
    record NavigationBarTokens(
            double containerHeight,
            double itemWidth,
            double indicatorWidth,
            double indicatorHeight,
            double indicatorShape,
            double contentSpacing,
            double horizontalPadding
    ) {
        /// Creates navigation bar tokens.
        public NavigationBarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(itemWidth, "itemWidth");
            validateNonNegative(indicatorWidth, "indicatorWidth");
            validateNonNegative(indicatorHeight, "indicatorHeight");
            validateNonNegative(indicatorShape, "indicatorShape");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens used by navigation rails.
    ///
    /// @param containerWidth the navigation rail container width
    /// @param itemHeight the preferred navigation item height
    /// @param itemWidth the preferred navigation item width
    /// @param indicatorWidth the selected indicator width
    /// @param indicatorHeight the selected indicator height
    /// @param indicatorShape the selected indicator radius
    /// @param contentSpacing the spacing between item icon and label
    /// @param verticalPadding the vertical padding around items
    /// @param horizontalPadding the horizontal padding around items
    /// @param itemSpacing the spacing between items
    @NotNullByDefault
    record NavigationRailTokens(
            double containerWidth,
            double itemHeight,
            double itemWidth,
            double indicatorWidth,
            double indicatorHeight,
            double indicatorShape,
            double contentSpacing,
            double verticalPadding,
            double horizontalPadding,
            double itemSpacing
    ) {
        /// Creates navigation rail tokens.
        public NavigationRailTokens {
            validateNonNegative(containerWidth, "containerWidth");
            validateNonNegative(itemHeight, "itemHeight");
            validateNonNegative(itemWidth, "itemWidth");
            validateNonNegative(indicatorWidth, "indicatorWidth");
            validateNonNegative(indicatorHeight, "indicatorHeight");
            validateNonNegative(indicatorShape, "indicatorShape");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(verticalPadding, "verticalPadding");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(itemSpacing, "itemSpacing");
        }
    }

    /// Tokens used by navigation drawers.
    ///
    /// @param containerWidth the navigation drawer container width
    /// @param oneLineItemHeight the preferred one-line drawer item height
    /// @param twoLineItemHeight the preferred two-line drawer item height
    /// @param threeLineItemHeight the preferred three-line drawer item height
    /// @param itemContainerShape the drawer item container radius
    /// @param containerPadding the padding around drawer items
    /// @param itemHorizontalPadding the horizontal item content padding
    /// @param itemVerticalPadding the vertical item content padding
    /// @param itemContentSpacing the spacing between item content regions
    /// @param itemSpacing the spacing between drawer items
    /// @param groupChildItemHeight the preferred one-line child item height inside collapsible groups
    /// @param groupChildItemContainerShape the child item container radius inside collapsible groups
    /// @param groupChildItemHorizontalPadding the child item horizontal content padding inside collapsible groups
    @NotNullByDefault
    record NavigationDrawerTokens(
            double containerWidth,
            double oneLineItemHeight,
            double twoLineItemHeight,
            double threeLineItemHeight,
            double itemContainerShape,
            double containerPadding,
            double itemHorizontalPadding,
            double itemVerticalPadding,
            double itemContentSpacing,
            double itemSpacing,
            double groupChildItemHeight,
            double groupChildItemContainerShape,
            double groupChildItemHorizontalPadding
    ) {
        /// Creates navigation drawer tokens.
        public NavigationDrawerTokens {
            validateNonNegative(containerWidth, "containerWidth");
            validateNonNegative(oneLineItemHeight, "oneLineItemHeight");
            validateNonNegative(twoLineItemHeight, "twoLineItemHeight");
            validateNonNegative(threeLineItemHeight, "threeLineItemHeight");
            validateNonNegative(itemContainerShape, "itemContainerShape");
            validateNonNegative(containerPadding, "containerPadding");
            validateNonNegative(itemHorizontalPadding, "itemHorizontalPadding");
            validateNonNegative(itemVerticalPadding, "itemVerticalPadding");
            validateNonNegative(itemContentSpacing, "itemContentSpacing");
            validateNonNegative(itemSpacing, "itemSpacing");
            validateNonNegative(groupChildItemHeight, "groupChildItemHeight");
            validateNonNegative(groupChildItemContainerShape, "groupChildItemContainerShape");
            validateNonNegative(groupChildItemHorizontalPadding, "groupChildItemHorizontalPadding");
        }
    }

    /// Tokens used by list items.
    ///
    /// @param oneLineHeight the preferred one-line item height
    /// @param twoLineHeight the preferred two-line item height
    /// @param threeLineHeight the preferred three-line item height
    /// @param containerShape the list item container radius
    /// @param horizontalPadding the horizontal content padding
    /// @param verticalPadding the vertical content padding
    /// @param contentSpacing the spacing between content regions
    /// @param sectionHeaderHeight the preferred list section header height
    /// @param sectionHeaderHorizontalPadding the horizontal list section header padding
    @NotNullByDefault
    record ListItemTokens(
            double oneLineHeight,
            double twoLineHeight,
            double threeLineHeight,
            double containerShape,
            double horizontalPadding,
            double verticalPadding,
            double contentSpacing,
            double sectionHeaderHeight,
            double sectionHeaderHorizontalPadding
    ) {
        /// Creates list item tokens.
        public ListItemTokens {
            validateNonNegative(oneLineHeight, "oneLineHeight");
            validateNonNegative(twoLineHeight, "twoLineHeight");
            validateNonNegative(threeLineHeight, "threeLineHeight");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(verticalPadding, "verticalPadding");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(sectionHeaderHeight, "sectionHeaderHeight");
            validateNonNegative(sectionHeaderHorizontalPadding, "sectionHeaderHorizontalPadding");
        }
    }

    /// Validates a non-negative component token.
    private static void validateNonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }

    /// Validates that a component token is finite.
    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    /// Validates that a token is a JavaFX opacity value.
    private static void validateOpacity(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("opacity must be between 0 and 1");
        }
    }
}
