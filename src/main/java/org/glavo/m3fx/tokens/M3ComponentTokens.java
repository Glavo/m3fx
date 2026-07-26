// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ComponentTokensImpl;
import org.glavo.monetfx.ColorRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Defines the immutable component-level Material Design 3 token groups used by M3FX controls.
///
/// Component tokens collect the shape, size, padding, spacing, semantic color-role mappings, and other values
/// associated with individual controls. Unless a record component states otherwise, numeric geometry is expressed
/// in JavaFX logical pixels; opacity values are dimensionless. Each nested record is an immutable value object and
/// validates the constraints documented by its canonical constructor.
///
/// [builder(M3Profile,M3ShapeTokens,M3Density)] creates a complete snapshot derived from a profile, shape scale,
/// and density. Density affects only the generated component metrics for which density is defined, primarily
/// component heights and vertical spacing. It does not rescale all component geometry. [builder(M3ComponentTokens)]
/// copies an existing snapshot for selective replacement. Derived token groups do not retain a live relationship
/// with the profile, shape scale, or density supplied to the builder.
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

    /// Creates a builder initialized with generated component tokens for a profile.
    ///
    /// @param profile the Material token profile
    /// @param shapeTokens the shape scale used by generated component tokens
    /// @param density the density adjustment applied to density-sensitive component metrics
    /// @return a mutable component-token builder
    /// @throws NullPointerException if `profile`, `shapeTokens`, or `density` is `null`
    static M3ComponentTokensBuilder builder(M3Profile profile, M3ShapeTokens shapeTokens, M3Density density) {
        return new M3ComponentTokensBuilder(defaultsForProfile(profile, shapeTokens, density));
    }

    /// Creates a builder initialized from an existing component token set.
    ///
    /// @param tokens the component tokens to copy
    /// @return a mutable component-token builder
    /// @throws NullPointerException if `tokens` is `null`
    static M3ComponentTokensBuilder builder(M3ComponentTokens tokens) {
        return new M3ComponentTokensBuilder(tokens);
    }

    /// Creates component tokens for a profile.
    ///
    /// @param profile     the Material profile whose component defaults should be generated
    /// @param shapeTokens the shape scale used by generated component tokens
    /// @param density     the density adjustment applied to selected generated component metrics
    /// @return a component token set generated from the supplied profile, shape scale, and density
    private static M3ComponentTokens defaultsForProfile(
            M3Profile profile,
            M3ShapeTokens shapeTokens,
            M3Density density
    ) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(density, "density");

        double buttonHeight = density.compact(40.0);
        double iconSmallSize = profile == M3Profile.EXPRESSIVE_2025 ? 20.0 : 18.0;
        double iconMediumSize = 24.0;
        double iconLargeSize = profile == M3Profile.EXPRESSIVE_2025 ? 36.0 : 32.0;
        double iconExtraLargeSize = profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 40.0;
        double segmentedButtonHeight = density.compact(40.0);
        double tabHeight = density.compact(48.0);
        double tabMinWidth = 90.0;
        double fieldHeight = density.compact(56.0);
        double textAreaHeight = density.compact(112.0);
        double menuItemHeight = density.compact(profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 48.0);
        double searchBarHeight = density.compact(56.0);
        double pickerNavigationButtonSize = 40.0;
        double sideSheetWidth = 256.0;
        double sideSheetMaxWidth = 400.0;
        double bottomSheetMaxWidth = 640.0;
        double chipHeight = 32.0;
        double badgeSmallSize = 6.0;
        double badgeLargeHeight = 16.0;
        double badgeLargeMinWidth = 16.0;
        double avatarSize = 40.0;
        double topAppBarHeight = density.compact(64.0);
        double topAppBarMediumHeight = density.compact(112.0);
        double topAppBarLargeHeight = density.compact(152.0);
        double topAppBarMediumFlexibleHeight = density.compact(112.0);
        double topAppBarMediumFlexibleSubtitleHeight = density.compact(136.0);
        double topAppBarLargeFlexibleHeight = density.compact(120.0);
        double topAppBarLargeFlexibleSubtitleHeight = density.compact(152.0);
        double bottomAppBarHeight = density.compact(80.0);
        double navigationBarHeight = density.compact(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 80.0);
        double navigationItemWidth = 80.0;
        double navigationIndicatorWidth = profile == M3Profile.EXPRESSIVE_2025 ? 56.0 : 64.0;
        double navigationIndicatorHeight = 32.0;
        double navigationRailWidth = profile == M3Profile.EXPRESSIVE_2025 ? 96.0 : 80.0;
        double navigationRailItemWidth = 80.0;
        double navigationRailIndicatorWidth = 56.0;
        double navigationDrawerWidth = 360.0;
        double navigationDrawerOneLineItemHeight = Math.max(48.0, density.compact(56.0));
        double navigationDrawerTwoLineItemHeight = Math.max(48.0, density.compact(72.0));
        double navigationDrawerThreeLineItemHeight = Math.max(48.0, density.compact(88.0));
        double listItemOneLineHeight =
                Math.max(48.0, density.compact(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0));
        double listItemTwoLineHeight =
                Math.max(48.0, density.compact(profile == M3Profile.EXPRESSIVE_2025 ? 80.0 : 72.0));
        double listItemThreeLineHeight =
                Math.max(48.0, density.compact(profile == M3Profile.EXPRESSIVE_2025 ? 96.0 : 88.0));
        double listSectionHeaderHeight =
                Math.max(48.0, density.compact(profile == M3Profile.EXPRESSIVE_2025 ? 56.0 : 48.0));
        double progressLinearWaveAmplitude = profile == M3Profile.EXPRESSIVE_2025 ? 3.0 : 0.0;
        double progressCircularWaveAmplitude = profile == M3Profile.EXPRESSIVE_2025 ? 1.6 : 0.0;
        double loadingIndicatorContainerSize = 48.0;
        double loadingIndicatorIndicatorSize = 38.0;
        double progressCircularIndicatorSize = 40.0;
        double progressCircularWaveIndicatorSize = 48.0;
        boolean expressive = profile == M3Profile.EXPRESSIVE_2025;
        double navigationContentSpacing = 4.0;
        double navigationHorizontalPadding = 8.0;
        double navigationBarItemSpacing = expressive ? 6.0 : 0.0;
        double navigationRailCollapsedTopPadding = expressive ? 44.0 : 16.0;
        double navigationRailCollapsedBottomPadding = expressive ? 0.0 : 16.0;
        double navigationRailHorizontalPadding = 0.0;
        double navigationRailItemSpacing = expressive ? 4.0 : 8.0;
        double navigationRailHeaderSpacing = 40.0;
        double navigationDrawerContainerPadding = 12.0;
        double navigationDrawerItemHorizontalPadding = 16.0;
        double navigationDrawerItemContentSpacing = 12.0;
        double navigationDrawerItemSpacing = 0.0;
        double navigationDrawerGroupChildPadding = 32.0;
        double listItemHorizontalPadding = expressive ? 20.0 : 16.0;
        double listItemVerticalPadding = density.compact(expressive ? 10.0 : 8.0);
        double listItemContentSpacing = expressive ? 20.0 : 16.0;
        double listSectionHeaderHorizontalPadding = expressive ? 20.0 : 16.0;
        double menuContainerShape = expressive ? shapeTokens.large() : shapeTokens.extraSmall();
        double menuContainerPadding = expressive ? 2.0 : 8.0;
        double menuItemContainerShape = shapeTokens.extraSmall();
        double menuSelectedItemContainerShape = expressive ? shapeTokens.medium() : menuItemContainerShape;
        double menuActiveItemContainerShape = expressive ? shapeTokens.large() : menuItemContainerShape;
        double menuFirstItemContainerShape = expressive ? shapeTokens.medium() : menuItemContainerShape;
        double menuLastItemContainerShape = expressive ? shapeTokens.medium() : menuItemContainerShape;
        double menuItemHorizontalPadding = expressive ? 16.0 : 12.0;
        double menuItemContentSpacing = 12.0;
        double menuItemSpacing = expressive ? 2.0 : 0.0;
        double searchBarHorizontalPadding = 16.0;
        double searchBarContentSpacing = 16.0;
        double searchContainedBarHorizontalPadding = 4.0;
        double searchContainedBarContentSpacing = 4.0;
        double searchDividedBarHorizontalPadding = 16.0;
        double searchDividedBarContentSpacing = 16.0;
        double searchBarTrailingActionsGap = 0.0;
        double searchViewContainerShape = shapeTokens.extraLarge();
        double searchViewHorizontalPadding = 12.0;
        double searchViewBarResultsGap = 2.0;
        double searchViewResultsShape = shapeTokens.medium();
        double searchViewDockedBottomPadding = 4.0;
        double searchViewFullScreenBottomPadding = 16.0;
        double searchViewMinWidth = 360.0;
        double searchViewMaxWidth = 720.0;
        double searchViewDockedMinHeight = 240.0;
        double searchViewFullScreenDividedHeaderHeight = 72.0;
        double pickerFieldPopupShape = shapeTokens.extraLarge();
        double pickerFieldPopupPadding = 16.0;
        double pickerFieldPopupSpacing = 16.0;
        double pickerFieldPresetListWidth = 132.0;
        double pickerFieldPresetListSpacing = 6.0;
        double pickerFieldPresetButtonHorizontalPadding = 12.0;
        double datePickerContainerWidth = 360.0;
        double datePickerDockedContainerShape = shapeTokens.large();
        double datePickerModalContainerShape = shapeTokens.extraLarge();
        double datePickerHorizontalPadding = 12.0;
        double datePickerContainerSpacing = 0.0;
        double datePickerHeaderHeight = density.compact(64.0);
        double datePickerHeaderSpacing = 0.0;
        double datePickerNavigationButtonSize = 40.0;
        double datePickerMenuButtonHeight = density.compact(40.0);
        double datePickerDayCellSize = 48.0;
        double datePickerDayStateLayerSize = 40.0;
        double datePickerGridGap = 0.0;
        double timePickerContainerShape = shapeTokens.extraLarge();
        double timePickerContainerPadding = 24.0;
        double timePickerContainerSpacing = 24.0;
        double timePickerDisplaySpacing = 4.0;
        double timePickerDisplayCellShape = shapeTokens.small();
        double timePickerDisplayCellWidth = 96.0;
        double timePickerDisplay24HourCellWidth = 114.0;
        double timePickerDisplayCellHeight = 80.0;
        double timePickerPeriodVerticalWidth = 52.0;
        double timePickerPeriodVerticalHeight = 80.0;
        double timePickerPeriodHorizontalWidth = 216.0;
        double timePickerPeriodHorizontalHeight = 38.0;
        double timePickerDialSize = 256.0;
        double timePickerDialHandleSize = 48.0;
        double timePickerDialCenterSize = 8.0;
        double timePickerDialTrackWidth = 2.0;
        double timePickerInputFieldWidth = 96.0;
        double timePickerInputFieldHeight = 72.0;
        double sheetContentPadding = 24.0;
        double sheetHeaderPadding = 24.0;
        double sheetHeaderContentSpacing = 12.0;
        double sheetDragHandleVerticalPadding = 22.0;
        double sheetDragHandleWidth = 32.0;
        double sheetDragHandleHeight = 4.0;
        double cardContainerShape = shapeTokens.medium();
        double cardContentPadding = 16.0;
        double dialogContentPadding = 24.0;
        double dialogContainerMinWidth = 280.0;
        double dialogContainerMaxWidth = 560.0;
        double dialogActionSpacing = 8.0;
        double dialogIconSize = 24.0;
        double snackbarContainerShape = shapeTokens.extraSmall();
        double snackbarContentPadding = 16.0;
        double snackbarContainerMinWidth = 344.0;
        double snackbarContainerMaxWidth = 672.0;
        double snackbarSingleLineContainerHeight = density.compact(48.0);
        double snackbarTwoLineContainerHeight = density.compact(68.0);
        double snackbarActionContainerHeight = 32.0;
        double bannerMinHeight = 80.0;
        double bannerVerticalPadding = 16.0;
        double bannerHorizontalPadding = 24.0;
        double bannerContentSpacing = 16.0;
        double bannerActionSpacing = 8.0;
        double tooltipPlainContainerShape = shapeTokens.extraSmall();
        double tooltipPlainVerticalPadding = 4.0;
        double tooltipPlainHorizontalPadding = 8.0;
        double tooltipRichContainerShape = shapeTokens.medium();
        double tooltipRichTopPadding = 12.0;
        double tooltipRichHorizontalPadding = 16.0;
        double tooltipRichBottomPadding = 8.0;
        double tooltipRichContentSpacing = 8.0;
        double tooltipRichPreferredWidth = 320.0;
        double tooltipRichActionSpacing = 8.0;
        double tooltipRichActionButtonHeight = 32.0;
        double tooltipRichActionButtonHorizontalPadding = 12.0;
        double appBarHorizontalPadding = 16.0;
        double topAppBarEdgePadding = 4.0;
        double topAppBarContentSpacing = 0.0;
        double topAppBarActionSpacing = 0.0;
        double bottomAppBarContentSpacing = 16.0;
        double bottomAppBarActionSpacing = 0.0;
        double topAppBarMediumBottomPadding = 20.0;
        double topAppBarLargeBottomPadding = 28.0;
        double topAppBarFlexibleBottomPadding = 12.0;
        double toolbarContainerHeight = density.compact(64.0);
        double toolbarContainerWidth = 64.0;
        double toolbarContainerShape = expressive ? shapeTokens.full() : shapeTokens.large();
        double toolbarItemSlotSize = 48.0;
        double toolbarContentPadding = 8.0;
        double toolbarDockedContentPadding = 16.0;
        double toolbarItemSpacing = 4.0;
        double toolbarDockedMaxItemSpacing = 32.0;
        double buttonHorizontalPadding = expressive ? 16.0 : 24.0;
        double textButtonHorizontalPadding = expressive ? 16.0 : 12.0;
        double segmentedButtonHorizontalPadding = 12.0;
        double tabHorizontalPadding = 16.0;
        double tabActiveIndicatorHeight = 3.0;
        double tabSecondaryActiveIndicatorHeight = 2.0;
        double tabActiveIndicatorShape = 3.0;
        double tabActiveIndicatorMinWidth = 24.0;
        double tabActiveIndicatorHorizontalInset = 2.0;
        double chipHorizontalPadding = 16.0;
        double chipIconHorizontalPadding = 8.0;
        double chipElementSpacing = 8.0;
        double chipIconSize = 18.0;
        double chipAvatarSize = 24.0;
        double chipAvatarShape = chipAvatarSize / 2.0;
        double chipOutlineWidth = 1.0;
        double chipGroupHorizontalGap = 8.0;
        double chipGroupVerticalGap = 8.0;
        double fieldHorizontalPadding = 16.0;
        double textAreaHorizontalPadding = 16.0;
        double textAreaVerticalPadding = density.compact(16.0);
        double formRowSpacing = 16.0;
        double formSectionContentSpacing = 12.0;
        double formSectionHeaderSpacing = 4.0;
        double formSectionHeaderBottomPadding = 4.0;
        double formRowLabelWidth = 180.0;
        double formRowColumnSpacing = 24.0;
        double formRowMinHeight = density.compact(64.0);
        double formRowTextSpacing = 2.0;
        double validationSummaryContainerShape = shapeTokens.small();
        double validationSummaryContentPadding = 16.0;
        double validationSummaryItemsSpacing = 4.0;
        double validationSummaryItemShape = shapeTokens.extraSmall();
        double validationSummaryItemVerticalPadding = 8.0;
        double validationSummaryItemHorizontalPadding = 10.0;
        double selectionTouchTargetSize = 48.0;
        double selectionStateLayerSize = 40.0;
        double checkboxContainerSize = 18.0;
        double checkboxSelectedMarkWidth = 12.0;
        double checkboxSelectedMarkHeight = 10.0;
        double checkboxIndeterminateMarkWidth = 12.0;
        double checkboxIndeterminateMarkHeight = 2.0;
        double radioContainerSize = 20.0;
        double radioSelectedDotSize = 10.0;
        double switchTouchTargetSize = 48.0;
        double switchTrackWidth = 52.0;
        double switchTrackHeight = 32.0;
        double switchStateLayerSize = 40.0;
        double switchUnselectedHandleSize = 16.0;
        double switchWithIconHandleSize = 24.0;
        double switchSelectedHandleSize = 24.0;
        double switchPressedHandleSize = 28.0;
        double switchIconSize = 16.0;
        double sliderStopIndicatorSize = 4.0;
        double sliderStopIndicatorTrailingSpace = 4.0;
        double sliderThumbWidth = 4.0;
        double sliderFocusedThumbWidth = 2.0;
        double sliderPressedThumbWidth = 2.0;
        double sliderThumbTrackGap = 6.0;
        double sliderTouchTargetSize = 48.0;
        SliderSizingTokens sliderSizing = new SliderSizingTokens(
                new SliderSizeTokens(
                        16.0,
                        8.0,
                        44.0,
                        0.0,
                        0.0
                ),
                new SliderSizeTokens(
                        24.0,
                        8.0,
                        44.0,
                        0.0,
                        0.0
                ),
                new SliderSizeTokens(
                        40.0,
                        12.0,
                        52.0,
                        24.0,
                        6.0
                ),
                new SliderSizeTokens(
                        56.0,
                        16.0,
                        68.0,
                        24.0,
                        6.0
                ),
                new SliderSizeTokens(
                        96.0,
                        28.0,
                        108.0,
                        32.0,
                        8.0
                )
        );
        double surfaceContainerShape = shapeTokens.medium();
        double surfaceContentPadding = 16.0;
        double carouselTrackHorizontalPadding = 16.0;
        double carouselTrackVerticalPadding = 8.0;
        double carouselItemSpacing = 8.0;
        double carouselItemShape = shapeTokens.extraLarge();
        double carouselSmallItemMinWidth = 40.0;
        double carouselSmallItemMaxWidth = 56.0;
        double carouselLargeItemMaxWidth = 320.0;

        return new M3ComponentTokensImpl(
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), textButtonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonSizingTokens(
                        new ButtonSizeTokens(
                                density.compact(32.0),
                                20.0,
                                shapeTokens.full(),
                                shapeTokens.medium(),
                                expressive ? shapeTokens.small() : shapeTokens.full(),
                                expressive ? shapeTokens.small() : shapeTokens.medium(),
                                12.0,
                                12.0,
                                8.0,
                                1.0
                        ),
                        new ButtonSizeTokens(
                                density.compact(40.0),
                                20.0,
                                shapeTokens.full(),
                                shapeTokens.medium(),
                                expressive ? shapeTokens.small() : shapeTokens.full(),
                                expressive ? shapeTokens.small() : shapeTokens.medium(),
                                buttonHorizontalPadding,
                                textButtonHorizontalPadding,
                                8.0,
                                1.0
                        ),
                        new ButtonSizeTokens(
                                density.compact(56.0),
                                24.0,
                                shapeTokens.full(),
                                shapeTokens.large(),
                                expressive ? shapeTokens.medium() : shapeTokens.full(),
                                expressive ? shapeTokens.medium() : shapeTokens.large(),
                                24.0,
                                24.0,
                                8.0,
                                1.0
                        ),
                        new ButtonSizeTokens(
                                density.compact(96.0),
                                32.0,
                                shapeTokens.full(),
                                shapeTokens.extraLarge(),
                                expressive ? shapeTokens.large() : shapeTokens.full(),
                                expressive ? shapeTokens.large() : shapeTokens.extraLarge(),
                                48.0,
                                48.0,
                                12.0,
                                2.0
                        ),
                        new ButtonSizeTokens(
                                density.compact(136.0),
                                40.0,
                                shapeTokens.full(),
                                shapeTokens.extraLarge(),
                                expressive ? shapeTokens.large() : shapeTokens.full(),
                                expressive ? shapeTokens.large() : shapeTokens.extraLarge(),
                                64.0,
                                64.0,
                                16.0,
                                3.0
                        )
                ),
                new IconButtonTokens(
                        new IconButtonSizeTokens(
                                32.0,
                                20.0,
                                28.0,
                                32.0,
                                40.0,
                                shapeTokens.full(),
                                12.0,
                                expressive ? 8.0 : shapeTokens.full(),
                                expressive ? 8.0 : 12.0,
                                expressive ? 12.0 : shapeTokens.full(),
                                expressive ? shapeTokens.full() : 12.0,
                                1.0
                        ),
                        new IconButtonSizeTokens(
                                40.0,
                                24.0,
                                32.0,
                                40.0,
                                52.0,
                                shapeTokens.full(),
                                12.0,
                                expressive ? 8.0 : shapeTokens.full(),
                                expressive ? 8.0 : 12.0,
                                expressive ? 12.0 : shapeTokens.full(),
                                expressive ? shapeTokens.full() : 12.0,
                                1.0
                        ),
                        new IconButtonSizeTokens(
                                56.0,
                                24.0,
                                48.0,
                                56.0,
                                72.0,
                                shapeTokens.full(),
                                16.0,
                                expressive ? 12.0 : shapeTokens.full(),
                                expressive ? 12.0 : 16.0,
                                expressive ? 16.0 : shapeTokens.full(),
                                expressive ? shapeTokens.full() : 16.0,
                                1.0
                        ),
                        new IconButtonSizeTokens(
                                96.0,
                                32.0,
                                64.0,
                                96.0,
                                128.0,
                                shapeTokens.full(),
                                28.0,
                                expressive ? 16.0 : shapeTokens.full(),
                                expressive ? 16.0 : 28.0,
                                expressive ? 28.0 : shapeTokens.full(),
                                expressive ? shapeTokens.full() : 28.0,
                                2.0
                        ),
                        new IconButtonSizeTokens(
                                136.0,
                                40.0,
                                104.0,
                                136.0,
                                184.0,
                                shapeTokens.full(),
                                28.0,
                                expressive ? 16.0 : shapeTokens.full(),
                                expressive ? 16.0 : 28.0,
                                expressive ? 28.0 : shapeTokens.full(),
                                expressive ? shapeTokens.full() : 28.0,
                                3.0
                        )
                ),
                new FabTokens(
                        new FabSizeTokens(
                                40.0,
                                shapeTokens.medium(),
                                24.0,
                                12.0,
                                8.0,
                                12.0
                        ),
                        new FabSizeTokens(
                                56.0,
                                shapeTokens.large(),
                                24.0,
                                16.0,
                                expressive ? 8.0 : 12.0,
                                expressive ? 16.0 : 20.0
                        ),
                        new FabSizeTokens(
                                80.0,
                                shapeTokens.largeIncreased(),
                                28.0,
                                26.0,
                                12.0,
                                26.0
                        ),
                        new FabSizeTokens(
                                96.0,
                                shapeTokens.extraLarge(),
                                36.0,
                                28.0,
                                16.0,
                                28.0
                        ),
                        new FabSizeTokens(
                                56.0,
                                shapeTokens.full(),
                                24.0,
                                24.0,
                                8.0,
                                24.0
                        ),
                        new FabSizeTokens(
                                56.0,
                                shapeTokens.full(),
                                20.0,
                                18.0,
                                0.0,
                                18.0
                        ),
                        4.0,
                        8.0,
                        expressive ? 16.0 : 14.0
                ),
                new IconTokens(iconSmallSize, iconMediumSize, iconLargeSize, iconExtraLargeSize),
                createButtonGroupTokens(
                        density,
                        shapeTokens,
                        expressive ? 10.0 : 8.0,
                        expressive
                ),
                createSplitButtonTokens(density),
                new ButtonTokens(segmentedButtonHeight, shapeTokens.full(), segmentedButtonHorizontalPadding),
                new TabTokens(
                        tabHeight,
                        tabMinWidth,
                        tabHorizontalPadding,
                        tabActiveIndicatorHeight,
                        tabSecondaryActiveIndicatorHeight,
                        tabActiveIndicatorShape,
                        tabActiveIndicatorMinWidth,
                        tabActiveIndicatorHorizontalInset
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
                        menuActiveItemContainerShape,
                        menuItemContainerShape,
                        menuFirstItemContainerShape,
                        menuLastItemContainerShape,
                        new MenuColorTokens(
                                expressive ? ColorRole.SURFACE_CONTAINER_LOW : ColorRole.SURFACE_CONTAINER,
                                ColorRole.ON_SURFACE,
                                expressive ? ColorRole.TERTIARY_CONTAINER : ColorRole.SECONDARY_CONTAINER,
                                expressive ? ColorRole.ON_TERTIARY_CONTAINER : ColorRole.ON_SECONDARY_CONTAINER,
                                expressive ? 0.38 : 1.0,
                                ColorRole.TERTIARY_CONTAINER,
                                ColorRole.ON_TERTIARY_CONTAINER,
                                ColorRole.ON_TERTIARY_CONTAINER,
                                ColorRole.TERTIARY,
                                ColorRole.ON_TERTIARY,
                                expressive ? ColorRole.TERTIARY : ColorRole.ON_TERTIARY_CONTAINER
                        ),
                        expressive ? 20.0 : 24.0,
                        menuItemHorizontalPadding,
                        expressive ? 16.0 : 12.0,
                        menuItemContentSpacing,
                        menuItemSpacing
                ),
                new SearchTokens(
                        searchBarHeight,
                        shapeTokens.full(),
                        searchBarHorizontalPadding,
                        searchBarContentSpacing,
                        searchContainedBarHorizontalPadding,
                        searchContainedBarContentSpacing,
                        searchDividedBarHorizontalPadding,
                        searchDividedBarContentSpacing,
                        searchBarTrailingActionsGap,
                        searchViewContainerShape,
                        searchViewHorizontalPadding,
                        searchViewBarResultsGap,
                        searchViewResultsShape,
                        searchViewDockedBottomPadding,
                        searchViewFullScreenBottomPadding,
                        searchViewMinWidth,
                        searchViewMaxWidth,
                        searchViewDockedMinHeight,
                        searchViewFullScreenDividedHeaderHeight
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
                        datePickerContainerWidth,
                        datePickerDockedContainerShape,
                        datePickerModalContainerShape,
                        datePickerHorizontalPadding,
                        datePickerContainerSpacing,
                        datePickerHeaderHeight,
                        datePickerHeaderSpacing,
                        datePickerNavigationButtonSize,
                        shapeTokens.full(),
                        datePickerMenuButtonHeight,
                        datePickerDayCellSize,
                        datePickerDayStateLayerSize,
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
                        timePickerDisplay24HourCellWidth,
                        timePickerDisplayCellHeight,
                        timePickerPeriodVerticalWidth,
                        timePickerPeriodVerticalHeight,
                        timePickerPeriodHorizontalWidth,
                        timePickerPeriodHorizontalHeight,
                        timePickerDialSize,
                        timePickerDialHandleSize,
                        timePickerDialCenterSize,
                        timePickerDialTrackWidth,
                        timePickerInputFieldWidth,
                        timePickerInputFieldHeight
                ),
                new SheetTokens(
                        sideSheetWidth,
                        sideSheetMaxWidth,
                        16.0,
                        bottomSheetMaxWidth,
                        28.0,
                        sheetContentPadding,
                        sheetHeaderPadding,
                        sheetHeaderContentSpacing,
                        sheetDragHandleVerticalPadding,
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
                        switchWithIconHandleSize,
                        switchSelectedHandleSize,
                        switchPressedHandleSize,
                        switchIconSize
                ),
                new SliderTokens(
                        sliderSizing,
                        sliderStopIndicatorSize,
                        sliderStopIndicatorTrailingSpace,
                        sliderThumbWidth,
                        sliderFocusedThumbWidth,
                        sliderPressedThumbWidth,
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
                        4.0,
                        shapeTokens.full(),
                        progressCircularIndicatorSize,
                        progressCircularWaveIndicatorSize,
                        progressLinearWaveAmplitude,
                        40.0,
                        20.0,
                        4.0,
                        4.0,
                        progressCircularWaveAmplitude,
                        15.0,
                        4.0
                ),
                new LoadingIndicatorTokens(
                        loadingIndicatorContainerSize,
                        loadingIndicatorIndicatorSize
                ),
                new SurfaceTokens(surfaceContainerShape, surfaceContentPadding),
                new CarouselTokens(
                        carouselTrackHorizontalPadding,
                        carouselTrackVerticalPadding,
                        carouselItemSpacing,
                        carouselItemShape,
                        carouselSmallItemMinWidth,
                        carouselSmallItemMaxWidth,
                        carouselLargeItemMaxWidth
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
                        topAppBarMediumFlexibleHeight,
                        topAppBarMediumFlexibleSubtitleHeight,
                        topAppBarLargeFlexibleHeight,
                        topAppBarLargeFlexibleSubtitleHeight,
                        topAppBarEdgePadding,
                        appBarHorizontalPadding,
                        topAppBarMediumBottomPadding,
                        topAppBarLargeBottomPadding,
                        topAppBarFlexibleBottomPadding,
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
                        toolbarDockedContentPadding,
                        toolbarItemSpacing,
                        toolbarDockedMaxItemSpacing
                ),
                new NavigationBarTokens(
                        navigationBarHeight,
                        navigationItemWidth,
                        navigationIndicatorWidth,
                        navigationIndicatorHeight,
                        shapeTokens.full(),
                        navigationContentSpacing,
                        navigationHorizontalPadding,
                        navigationBarItemSpacing,
                        new NavigationBarColorTokens(
                                expressive ? ColorRole.SECONDARY : ColorRole.ON_SURFACE,
                                expressive ? ColorRole.ON_SECONDARY_CONTAINER : ColorRole.ON_SURFACE
                        ),
                        expressive
                ),
                new NavigationRailTokens(
                        navigationRailWidth,
                        80.0,
                        220.0,
                        280.0,
                        360.0,
                        navigationBarHeight,
                        navigationRailItemWidth,
                        navigationRailIndicatorWidth,
                        navigationIndicatorHeight,
                        shapeTokens.full(),
                        navigationContentSpacing,
                        navigationRailCollapsedTopPadding,
                        navigationRailCollapsedBottomPadding,
                        navigationRailHorizontalPadding,
                        navigationRailItemSpacing,
                        44.0,
                        20.0,
                        navigationRailHeaderSpacing,
                        shapeTokens.large()
                ),
                new NavigationDrawerTokens(
                        navigationDrawerWidth,
                        navigationDrawerOneLineItemHeight,
                        navigationDrawerTwoLineItemHeight,
                        navigationDrawerThreeLineItemHeight,
                        shapeTokens.full(),
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
                        0.0,
                        listItemHorizontalPadding,
                        listItemVerticalPadding,
                        listItemContentSpacing,
                        2.0,
                        expressive ? shapeTokens.extraSmall() : 0.0,
                        expressive ? shapeTokens.medium() : 0.0,
                        expressive ? shapeTokens.large() : 0.0,
                        expressive ? shapeTokens.extraSmall() : 0.0,
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
    /// @param density                the density used for group container heights
    /// @param shapeTokens            the shape system used by connected inner-corner aliases
    /// @param iconToggleGroupSpacing the resolved spacing between icon toggle group children
    /// @param expressive              whether Expressive connected-button state shapes should be generated
    /// @return the button-group size token scale
    private static ButtonGroupTokens createButtonGroupTokens(
            M3Density density,
            M3ShapeTokens shapeTokens,
            double iconToggleGroupSpacing,
            boolean expressive
    ) {
        double connectedSpacing = 2.0;
        return new ButtonGroupTokens(
                createButtonGroupSizeTokens(
                        density,
                        32.0,
                        18.0,
                        connectedSpacing,
                        shapeTokens.small(),
                        expressive ? shapeTokens.extraSmall() : shapeTokens.small(),
                        expressive
                ),
                createButtonGroupSizeTokens(
                        density,
                        40.0,
                        12.0,
                        connectedSpacing,
                        shapeTokens.small(),
                        expressive ? shapeTokens.extraSmall() : shapeTokens.small(),
                        expressive
                ),
                createButtonGroupSizeTokens(
                        density,
                        56.0,
                        8.0,
                        connectedSpacing,
                        shapeTokens.small(),
                        expressive ? shapeTokens.extraSmall() : shapeTokens.small(),
                        expressive
                ),
                createButtonGroupSizeTokens(
                        density,
                        96.0,
                        8.0,
                        connectedSpacing,
                        shapeTokens.large(),
                        expressive ? shapeTokens.medium() : shapeTokens.large(),
                        expressive
                ),
                createButtonGroupSizeTokens(
                        density,
                        136.0,
                        8.0,
                        connectedSpacing,
                        shapeTokens.largeIncreased(),
                        expressive ? shapeTokens.large() : shapeTokens.largeIncreased(),
                        expressive
                ),
                -1.0,
                iconToggleGroupSpacing
        );
    }

    /// Creates tokens for one button-group size.
    ///
    /// @param density                     the density used for the container height
    /// @param containerHeight             the baseline container height
    /// @param standardSpacing             the baseline spacing between standard-group items
    /// @param connectedSpacing            the resolved spacing between connected-group items
    /// @param connectedInnerCorner        the resting connected inner corner
    /// @param connectedPressedInnerCorner the pressed connected inner corner
    /// @param expressive                  whether the selected state expands to a full-height inner corner
    /// @return the size-specific button-group tokens
    private static ButtonGroupSizeTokens createButtonGroupSizeTokens(
            M3Density density,
            double containerHeight,
            double standardSpacing,
            double connectedSpacing,
            double connectedInnerCorner,
            double connectedPressedInnerCorner,
            boolean expressive
    ) {
        double resolvedContainerHeight = density.compact(containerHeight);
        return new ButtonGroupSizeTokens(
                resolvedContainerHeight,
                standardSpacing,
                0.15,
                connectedSpacing,
                connectedInnerCorner,
                connectedPressedInnerCorner,
                expressive ? resolvedContainerHeight / 2.0 : connectedInnerCorner
        );
    }

    /// Creates the five-step split-button size scale.
    ///
    /// Split buttons are an Expressive component without a baseline joined variant, so all profiles retain the
    /// current between-space, asymmetric padding, state corners, optical icon offsets, and selected trailing shape.
    ///
    /// @param density the density used for split-button container heights
    /// @return the split-button size token scale
    private static SplitButtonTokens createSplitButtonTokens(M3Density density) {
        double spacing = 2.0;
        return new SplitButtonTokens(
                new SplitButtonSizeTokens(
                        density.compact(32.0),
                        spacing,
                        4.0,
                        8.0,
                        8.0,
                        12.0,
                        10.0,
                        22.0,
                        1.0,
                        13.0,
                        13.0,
                        16.0
                ),
                new SplitButtonSizeTokens(
                        density.compact(40.0),
                        spacing,
                        4.0,
                        12.0,
                        12.0,
                        16.0,
                        12.0,
                        22.0,
                        1.0,
                        13.0,
                        13.0,
                        20.0
                ),
                new SplitButtonSizeTokens(
                        density.compact(56.0),
                        spacing,
                        4.0,
                        12.0,
                        12.0,
                        24.0,
                        24.0,
                        26.0,
                        2.0,
                        15.0,
                        15.0,
                        28.0
                ),
                new SplitButtonSizeTokens(
                        density.compact(96.0),
                        spacing,
                        8.0,
                        20.0,
                        20.0,
                        48.0,
                        48.0,
                        38.0,
                        3.0,
                        29.0,
                        29.0,
                        48.0
                ),
                new SplitButtonSizeTokens(
                        density.compact(136.0),
                        spacing,
                        12.0,
                        20.0,
                        20.0,
                        64.0,
                        64.0,
                        50.0,
                        6.0,
                        43.0,
                        43.0,
                        68.0
                )
        );
    }

    /// Tokens shared by button variants.
    ///
    /// @param height            the preferred button height
    /// @param containerShape    the button container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    record ButtonTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates button tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `height`, `containerShape`, `horizontalPadding`
        public ButtonTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens used by the five Material button sizes.
    ///
    /// @param extraSmall the extra-small button size tokens
    /// @param small      the small button size tokens
    /// @param medium     the medium button size tokens
    /// @param large      the large button size tokens
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
        ///
        /// @throws NullPointerException if one of the following values is `null`:
        ///         `extraSmall`, `small`, `medium`, `large`,
        ///         `extraLarge`
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
    /// @param containerHeight       the visual container height
    /// @param iconSize              the icon glyph size
    /// @param roundContainerShape   the round resting container shape
    /// @param squareContainerShape  the rounded-square resting container shape
    /// @param pressedRoundContainerShape  the pressed shape for round buttons
    /// @param pressedSquareContainerShape the pressed shape for square buttons
    /// @param horizontalPadding     the leading and trailing padding for non-text variants
    /// @param textHorizontalPadding the leading and trailing padding for text variants
    /// @param iconLabelSpace        the spacing between an icon and label
    /// @param outlineWidth          the outlined variant stroke width
    @NotNullByDefault
    record ButtonSizeTokens(
            double containerHeight,
            double iconSize,
            double roundContainerShape,
            double squareContainerShape,
            double pressedRoundContainerShape,
            double pressedSquareContainerShape,
            double horizontalPadding,
            double textHorizontalPadding,
            double iconLabelSpace,
            double outlineWidth
    ) {
        /// Creates tokens for one Material button size.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `iconSize`, `roundContainerShape`, `squareContainerShape`,
        ///         `pressedRoundContainerShape`, `pressedSquareContainerShape`, `horizontalPadding`,
        ///         `textHorizontalPadding`, `iconLabelSpace`,
        ///         `outlineWidth`
        public ButtonSizeTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(iconSize, "iconSize");
            validateNonNegative(roundContainerShape, "roundContainerShape");
            validateNonNegative(squareContainerShape, "squareContainerShape");
            validateNonNegative(pressedRoundContainerShape, "pressedRoundContainerShape");
            validateNonNegative(pressedSquareContainerShape, "pressedSquareContainerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(textHorizontalPadding, "textHorizontalPadding");
            validateNonNegative(iconLabelSpace, "iconLabelSpace");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }

    /// Tokens used by icon button size families.
    ///
    /// @param extraSmall the extra-small icon button size tokens
    /// @param small      the small icon button size tokens
    /// @param medium     the medium icon button size tokens
    /// @param large      the large icon button size tokens
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
        ///
        /// @throws NullPointerException if one of the following values is `null`:
        ///         `extraSmall`, `small`, `medium`, `large`,
        ///         `extraLarge`
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
    /// @param containerHeight              the visual container height
    /// @param iconSize                     the icon glyph size
    /// @param narrowWidth                  the narrow visual container width
    /// @param defaultWidth                 the default visual container width
    /// @param wideWidth                    the wide visual container width
    /// @param roundContainerShape          the round resting container shape
    /// @param squareContainerShape         the square resting container shape
    /// @param pressedRoundContainerShape   the pressed shape for round icon buttons
    /// @param pressedSquareContainerShape  the pressed shape for square icon buttons
    /// @param selectedRoundContainerShape  the selected shape for round toggle icon buttons
    /// @param selectedSquareContainerShape the selected shape for square toggle icon buttons
    /// @param outlineWidth                 the outlined variant stroke width
    @NotNullByDefault
    record IconButtonSizeTokens(
            double containerHeight,
            double iconSize,
            double narrowWidth,
            double defaultWidth,
            double wideWidth,
            double roundContainerShape,
            double squareContainerShape,
            double pressedRoundContainerShape,
            double pressedSquareContainerShape,
            double selectedRoundContainerShape,
            double selectedSquareContainerShape,
            double outlineWidth
    ) {
        /// Creates icon button size tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `iconSize`, `narrowWidth`, `defaultWidth`,
        ///         `wideWidth`, `roundContainerShape`, `squareContainerShape`,
        ///         `pressedRoundContainerShape`, `pressedSquareContainerShape`,
        ///         `selectedRoundContainerShape`, `selectedSquareContainerShape`, `outlineWidth`
        public IconButtonSizeTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(iconSize, "iconSize");
            validateNonNegative(narrowWidth, "narrowWidth");
            validateNonNegative(defaultWidth, "defaultWidth");
            validateNonNegative(wideWidth, "wideWidth");
            validateNonNegative(roundContainerShape, "roundContainerShape");
            validateNonNegative(squareContainerShape, "squareContainerShape");
            validateNonNegative(pressedRoundContainerShape, "pressedRoundContainerShape");
            validateNonNegative(pressedSquareContainerShape, "pressedSquareContainerShape");
            validateNonNegative(selectedRoundContainerShape, "selectedRoundContainerShape");
            validateNonNegative(selectedSquareContainerShape, "selectedSquareContainerShape");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }

    /// Tokens shared by floating action button sizes.
    ///
    /// @param small             the small FAB tokens
    /// @param regular           the regular FAB and small extended FAB tokens
    /// @param medium            the medium FAB tokens
    /// @param large             the large FAB tokens
    /// @param menuItem          the 56-pixel labeled FAB menu item tokens
    /// @param menuCloseButton   the 56-pixel FAB menu close button tokens
    /// @param menuActionSpacing the vertical spacing between expanded FAB menu actions
    /// @param menuCloseSpacing  the vertical spacing between the last action and close button
    /// @param regularLabelFontSize the label font size of a regular extended FAB
    @NotNullByDefault
    record FabTokens(
            FabSizeTokens small,
            FabSizeTokens regular,
            FabSizeTokens medium,
            FabSizeTokens large,
            FabSizeTokens menuItem,
            FabSizeTokens menuCloseButton,
            double menuActionSpacing,
            double menuCloseSpacing,
            double regularLabelFontSize
    ) {
        /// Creates floating action button tokens.
        ///
        /// @throws NullPointerException if one of the following values is `null`:
        ///         `small`, `regular`, `medium`, `large`,
        ///         `menuItem`, `menuCloseButton`
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `menuActionSpacing`, `menuCloseSpacing`, `regularLabelFontSize`
        public FabTokens {
            Objects.requireNonNull(small, "small");
            Objects.requireNonNull(regular, "regular");
            Objects.requireNonNull(medium, "medium");
            Objects.requireNonNull(large, "large");
            validateNonNegative(menuActionSpacing, "menuActionSpacing");
            validateNonNegative(menuCloseSpacing, "menuCloseSpacing");
            validateNonNegative(regularLabelFontSize, "regularLabelFontSize");
            Objects.requireNonNull(menuItem, "menuItem");
            Objects.requireNonNull(menuCloseButton, "menuCloseButton");
        }
    }

    /// Size-dependent metrics shared by icon-only and extended floating action buttons.
    ///
    /// @param containerSize  the square icon-only container size and extended container height
    /// @param containerShape the container corner radius
    /// @param iconSize       the icon size
    /// @param leadingSpace   the extended button's logical leading space
    /// @param iconLabelSpace the space between an icon and label
    /// @param trailingSpace  the extended button's logical trailing space
    @NotNullByDefault
    record FabSizeTokens(
            double containerSize,
            double containerShape,
            double iconSize,
            double leadingSpace,
            double iconLabelSpace,
            double trailingSpace
    ) {
        /// Creates a floating action button size token set.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerSize`, `containerShape`, `iconSize`, `leadingSpace`,
        ///         `iconLabelSpace`, `trailingSpace`
        public FabSizeTokens {
            validateNonNegative(containerSize, "containerSize");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(iconSize, "iconSize");
            validateNonNegative(leadingSpace, "leadingSpace");
            validateNonNegative(iconLabelSpace, "iconLabelSpace");
            validateNonNegative(trailingSpace, "trailingSpace");
        }
    }

    /// Tokens shared by icon size roles.
    ///
    /// @param smallSize      the small icon glyph size
    /// @param mediumSize     the medium icon glyph size
    /// @param largeSize      the large icon glyph size
    /// @param extraLargeSize the extra-large icon glyph size
    @NotNullByDefault
    record IconTokens(
            double smallSize,
            double mediumSize,
            double largeSize,
            double extraLargeSize
    ) {
        /// Creates icon tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `smallSize`, `mediumSize`, `largeSize`, `extraLargeSize`
        public IconTokens {
            validateNonNegative(smallSize, "smallSize");
            validateNonNegative(mediumSize, "mediumSize");
            validateNonNegative(largeSize, "largeSize");
            validateNonNegative(extraLargeSize, "extraLargeSize");
        }
    }

    /// The five-step Material button-group token scale.
    ///
    /// @param extraSmall             tokens for extra-small button groups
    /// @param small                  tokens for small button groups
    /// @param medium                 tokens for medium button groups
    /// @param large                  tokens for large button groups
    /// @param extraLarge             tokens for extra-large button groups
    /// @param segmentedGroupSpacing  the spacing between segmented button group children
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
        ///
        /// @throws NullPointerException if one of the following values is `null`:
        ///         `extraSmall`, `small`, `medium`, `large`,
        ///         `extraLarge`
        /// @throws IllegalArgumentException if `iconToggleGroupSpacing` is negative
        /// @throws IllegalArgumentException if `segmentedGroupSpacing` is not finite
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
    /// @param containerHeight                the group container height
    /// @param standardSpacing                the spacing between standard-group children
    /// @param standardPressedWidthMultiplier the proportional width increase applied to an activated standard item
    /// @param connectedSpacing               the spacing between connected-group children
    /// @param connectedInnerCorner           the resting connected inner-corner radius
    /// @param connectedPressedInnerCorner    the pressed connected inner-corner radius
    /// @param connectedSelectedInnerCorner   the selected connected inner-corner radius
    @NotNullByDefault
    record ButtonGroupSizeTokens(
            double containerHeight,
            double standardSpacing,
            double standardPressedWidthMultiplier,
            double connectedSpacing,
            double connectedInnerCorner,
            double connectedPressedInnerCorner,
            double connectedSelectedInnerCorner
    ) {
        /// Creates button-group tokens for one size role.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `standardPressedWidthMultiplier`, `connectedInnerCorner`, `connectedPressedInnerCorner`,
        ///         `connectedSelectedInnerCorner`
        /// @throws IllegalArgumentException if one of the following values is not finite:
        ///         `standardSpacing`, `connectedSpacing`
        public ButtonGroupSizeTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateFinite(standardSpacing, "standardSpacing");
            validateNonNegative(standardPressedWidthMultiplier, "standardPressedWidthMultiplier");
            validateFinite(connectedSpacing, "connectedSpacing");
            validateNonNegative(connectedInnerCorner, "connectedInnerCorner");
            validateNonNegative(connectedPressedInnerCorner, "connectedPressedInnerCorner");
            validateNonNegative(connectedSelectedInnerCorner, "connectedSelectedInnerCorner");
        }
    }

    /// The five-step Material split-button size token scale.
    ///
    /// @param extraSmall tokens for extra-small split buttons
    /// @param small      tokens for small split buttons
    /// @param medium     tokens for medium split buttons
    /// @param large      tokens for large split buttons
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
        ///
        /// @throws NullPointerException if one of the following values is `null`:
        ///         `extraSmall`, `small`, `medium`, `large`,
        ///         `extraLarge`
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
    /// @param containerHeight     the split-button container height
    /// @param spacing             the spacing between the leading and trailing button parts
    /// @param innerCorner         the resting inner-corner radius
    /// @param hoveredInnerCorner  the hovered and focused inner-corner radius
    /// @param pressedInnerCorner  the pressed inner-corner radius
    /// @param actionLeadingSpace  the logical leading padding of the primary action
    /// @param actionTrailingSpace the logical trailing padding of the primary action
    /// @param menuIconSize        the trailing menu icon viewport size
    /// @param menuIconOffset      the unselected menu icon offset toward the group center
    /// @param menuLeadingSpace    the logical leading space around the trailing icon
    /// @param menuTrailingSpace   the logical trailing space around the trailing icon
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `innerCorner`, `hoveredInnerCorner`, `pressedInnerCorner`,
        ///         `actionLeadingSpace`, `actionTrailingSpace`, `menuIconSize`, `menuIconOffset`,
        ///         `menuLeadingSpace`, `menuTrailingSpace`, `selectedInnerCorner`
        /// @throws IllegalArgumentException if `spacing` is not finite
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
    /// @param containerHeight       the tab container height
    /// @param tabMinWidth           the tab minimum width
    /// @param horizontalPadding     the horizontal content padding
    /// @param activeIndicatorHeight           the primary active indicator height
    /// @param secondaryActiveIndicatorHeight  the secondary active indicator height
    /// @param activeIndicatorShape            the active indicator radius
    /// @param activeIndicatorMinWidth         the minimum primary active indicator length
    /// @param activeIndicatorHorizontalInset  the extension on each side of primary tab content
    @NotNullByDefault
    record TabTokens(
            double containerHeight,
            double tabMinWidth,
            double horizontalPadding,
            double activeIndicatorHeight,
            double secondaryActiveIndicatorHeight,
            double activeIndicatorShape,
            double activeIndicatorMinWidth,
            double activeIndicatorHorizontalInset
    ) {
        /// Creates tab tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `tabMinWidth`, `horizontalPadding`, `activeIndicatorHeight`,
        ///         `secondaryActiveIndicatorHeight`, `activeIndicatorShape`, `activeIndicatorMinWidth`, `activeIndicatorHorizontalInset`
        public TabTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(tabMinWidth, "tabMinWidth");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(activeIndicatorHeight, "activeIndicatorHeight");
            validateNonNegative(secondaryActiveIndicatorHeight, "secondaryActiveIndicatorHeight");
            validateNonNegative(activeIndicatorShape, "activeIndicatorShape");
            validateNonNegative(activeIndicatorMinWidth, "activeIndicatorMinWidth");
            validateNonNegative(activeIndicatorHorizontalInset, "activeIndicatorHorizontalInset");
        }
    }

    /// Tokens shared by text input controls.
    ///
    /// @param height            the preferred field height
    /// @param containerShape    the field container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    record FieldTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates field tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `height`, `containerShape`, `horizontalPadding`
        public FieldTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Component tokens for text area controls.
    ///
    /// @param height            the preferred text area container height
    /// @param containerShape    the text area container corner radius
    /// @param horizontalPadding the horizontal content padding
    /// @param verticalPadding   the vertical content padding
    @NotNullByDefault
    record TextAreaTokens(
            double height,
            double containerShape,
            double horizontalPadding,
            double verticalPadding
    ) {
        /// Validates text area tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `height`, `containerShape`, `horizontalPadding`, `verticalPadding`
        public TextAreaTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(verticalPadding, "verticalPadding");
        }
    }

    /// Component tokens for form containers.
    ///
    /// @param contentPadding             the uniform padding around top-level form content
    /// @param rowSpacing                 the vertical spacing between top-level form rows and sections
    /// @param sectionContentSpacing      the vertical spacing between section content nodes
    /// @param sectionHeaderSpacing       the vertical spacing between section title and supporting text
    /// @param sectionHeaderBottomPadding the bottom padding below a section header
    /// @param rowLabelWidth              the width reserved for form row labels
    /// @param rowColumnSpacing           the horizontal spacing between row label, content, and trailing regions
    /// @param rowMinHeight               the minimum height of each form row
    /// @param rowTextSpacing             the vertical spacing between row label and supporting text
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `contentPadding`, `rowSpacing`, `sectionContentSpacing`, `sectionHeaderSpacing`,
        ///         `sectionHeaderBottomPadding`, `rowLabelWidth`, `rowColumnSpacing`, `rowMinHeight`,
        ///         `rowTextSpacing`
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
    /// @param containerShape        the summary container corner radius
    /// @param contentPadding        the uniform summary content padding
    /// @param itemsSpacing          the vertical spacing between invalid item rows
    /// @param itemShape             the invalid item state container corner radius
    /// @param itemVerticalPadding   the vertical padding inside each invalid item
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerShape`, `contentPadding`, `itemsSpacing`, `itemShape`,
        ///         `itemVerticalPadding`, `itemHorizontalPadding`
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
    /// @param containerShape             the menu container corner radius
    /// @param containerPadding           the padding around menu items
    /// @param itemHeight                 the one-line menu item height
    /// @param itemContainerShape         the menu item state container corner radius
    /// @param selectedItemContainerShape the selected menu item state container corner radius
    /// @param activeItemContainerShape   the active submenu-owner container corner radius
    /// @param innerCornerShape           the inner-corner radius at grouped-menu boundaries
    /// @param firstItemContainerShape    the first direct menu item state container corner radius
    /// @param lastItemContainerShape     the last direct menu item state container corner radius
    /// @param colors                      the semantic color-role mappings used by menus
    /// @param itemIconSize               the leading and trailing icon size
    /// @param itemHorizontalPadding      the horizontal item content padding
    /// @param sectionHeaderHorizontalPadding the horizontal section-header padding
    /// @param itemContentSpacing         the spacing between item content regions
    /// @param itemSpacing                the vertical spacing between direct menu items
    @NotNullByDefault
    record MenuTokens(
            double containerShape,
            double containerPadding,
            double itemHeight,
            double itemContainerShape,
            double selectedItemContainerShape,
            double activeItemContainerShape,
            double innerCornerShape,
            double firstItemContainerShape,
            double lastItemContainerShape,
            MenuColorTokens colors,
            double itemIconSize,
            double itemHorizontalPadding,
            double sectionHeaderHorizontalPadding,
            double itemContentSpacing,
            double itemSpacing
    ) {
        /// Validates menu tokens.
        ///
        /// @throws NullPointerException if `colors` is `null`
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerShape`, `containerPadding`, `itemHeight`, `itemContainerShape`,
        ///         `selectedItemContainerShape`, `activeItemContainerShape`, `innerCornerShape`, `firstItemContainerShape`,
        ///         `lastItemContainerShape`, `itemIconSize`, `itemHorizontalPadding`,
        ///         `sectionHeaderHorizontalPadding`, `itemContentSpacing`, `itemSpacing`
        public MenuTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(containerPadding, "containerPadding");
            validateNonNegative(itemHeight, "itemHeight");
            validateNonNegative(itemContainerShape, "itemContainerShape");
            validateNonNegative(selectedItemContainerShape, "selectedItemContainerShape");
            validateNonNegative(activeItemContainerShape, "activeItemContainerShape");
            validateNonNegative(innerCornerShape, "innerCornerShape");
            validateNonNegative(firstItemContainerShape, "firstItemContainerShape");
            validateNonNegative(lastItemContainerShape, "lastItemContainerShape");
            Objects.requireNonNull(colors, "colors");
            validateNonNegative(itemIconSize, "itemIconSize");
            validateNonNegative(itemHorizontalPadding, "itemHorizontalPadding");
            validateNonNegative(sectionHeaderHorizontalPadding, "sectionHeaderHorizontalPadding");
            validateNonNegative(itemContentSpacing, "itemContentSpacing");
            validateNonNegative(itemSpacing, "itemSpacing");
        }
    }

    /// Semantic color-role mappings used by menus.
    ///
    /// Each role is resolved against the [M3ColorTokens] in the same complete token set when a theme is compiled.
    /// This preserves dynamic color schemes while allowing menu color semantics to be selected independently of
    /// the token set's retained [M3Profile].
    ///
    /// @param containerRole                         the standard menu container role
    /// @param itemStateLayerRole                    the standard item interaction-state role
    /// @param selectedItemContainerRole             the standard selected-item container role
    /// @param selectedItemContentRole               the standard selected-item content role
    /// @param selectedDisabledContainerOpacity      the opacity of a disabled selected-item container
    /// @param vibrantContainerRole                  the vibrant menu container role
    /// @param vibrantItemContentRole                the vibrant item content role
    /// @param vibrantItemStateLayerRole             the vibrant item interaction-state role
    /// @param vibrantSelectedItemContainerRole      the vibrant selected-item container role
    /// @param vibrantSelectedItemContentRole        the vibrant selected-item content role
    /// @param vibrantInteractionIconRole            the vibrant interacting icon role
    @NotNullByDefault
    record MenuColorTokens(
            ColorRole containerRole,
            ColorRole itemStateLayerRole,
            ColorRole selectedItemContainerRole,
            ColorRole selectedItemContentRole,
            double selectedDisabledContainerOpacity,
            ColorRole vibrantContainerRole,
            ColorRole vibrantItemContentRole,
            ColorRole vibrantItemStateLayerRole,
            ColorRole vibrantSelectedItemContainerRole,
            ColorRole vibrantSelectedItemContentRole,
            ColorRole vibrantInteractionIconRole
    ) {
        /// Creates menu color-role tokens.
        ///
        /// @throws NullPointerException if any color role is `null`
        /// @throws IllegalArgumentException if `selectedDisabledContainerOpacity` is outside `[0, 1]`
        public MenuColorTokens {
            Objects.requireNonNull(containerRole, "containerRole");
            Objects.requireNonNull(itemStateLayerRole, "itemStateLayerRole");
            Objects.requireNonNull(selectedItemContainerRole, "selectedItemContainerRole");
            Objects.requireNonNull(selectedItemContentRole, "selectedItemContentRole");
            validateOpacity(selectedDisabledContainerOpacity);
            Objects.requireNonNull(vibrantContainerRole, "vibrantContainerRole");
            Objects.requireNonNull(vibrantItemContentRole, "vibrantItemContentRole");
            Objects.requireNonNull(vibrantItemStateLayerRole, "vibrantItemStateLayerRole");
            Objects.requireNonNull(vibrantSelectedItemContainerRole, "vibrantSelectedItemContainerRole");
            Objects.requireNonNull(vibrantSelectedItemContentRole, "vibrantSelectedItemContentRole");
            Objects.requireNonNull(vibrantInteractionIconRole, "vibrantInteractionIconRole");
        }
    }

    /// Component tokens for search components.
    ///
    /// @param barHeight                            the search bar container height
    /// @param barContainerShape                    the search bar container corner radius
    /// @param barHorizontalPadding                 the standalone search bar horizontal content padding
    /// @param barContentSpacing                    the standalone spacing between search bar content regions
    /// @param containedBarHorizontalPadding        the contained search view bar horizontal padding
    /// @param containedBarContentSpacing           the contained search view bar content spacing
    /// @param dividedBarHorizontalPadding          the divided search view header horizontal padding
    /// @param dividedBarContentSpacing             the divided search view header content spacing
    /// @param barTrailingActionsGap                the spacing between trailing search bar actions
    /// @param viewContainerShape                   the docked search view corner radius
    /// @param viewHorizontalPadding                the horizontal inset around contained search surfaces
    /// @param viewBarResultsGap                    the gap between contained bar and result surfaces
    /// @param viewResultsShape                     the contained search results surface corner radius
    /// @param viewDockedBottomPadding              the bottom inset in a docked contained search view
    /// @param viewFullScreenBottomPadding          the bottom inset in a full-screen contained search view
    /// @param viewMinWidth                         the minimum docked search view width
    /// @param viewMaxWidth                         the maximum docked search view width
    /// @param viewDockedMinHeight                  the minimum active docked search view height
    /// @param viewFullScreenDividedHeaderHeight    the divided full-screen search header height
    @NotNullByDefault
    record SearchTokens(
            double barHeight,
            double barContainerShape,
            double barHorizontalPadding,
            double barContentSpacing,
            double containedBarHorizontalPadding,
            double containedBarContentSpacing,
            double dividedBarHorizontalPadding,
            double dividedBarContentSpacing,
            double barTrailingActionsGap,
            double viewContainerShape,
            double viewHorizontalPadding,
            double viewBarResultsGap,
            double viewResultsShape,
            double viewDockedBottomPadding,
            double viewFullScreenBottomPadding,
            double viewMinWidth,
            double viewMaxWidth,
            double viewDockedMinHeight,
            double viewFullScreenDividedHeaderHeight
    ) {
        /// Validates search tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `barHeight`, `barContainerShape`, `barHorizontalPadding`, `barContentSpacing`,
        ///         `containedBarHorizontalPadding`, `containedBarContentSpacing`, `dividedBarHorizontalPadding`, `dividedBarContentSpacing`,
        ///         `barTrailingActionsGap`, `viewContainerShape`, `viewHorizontalPadding`, `viewBarResultsGap`,
        ///         `viewResultsShape`, `viewDockedBottomPadding`, `viewFullScreenBottomPadding`, `viewMinWidth`,
        ///         `viewMaxWidth`, `viewDockedMinHeight`, `viewFullScreenDividedHeaderHeight`
        public SearchTokens {
            validateNonNegative(barHeight, "barHeight");
            validateNonNegative(barContainerShape, "barContainerShape");
            validateNonNegative(barHorizontalPadding, "barHorizontalPadding");
            validateNonNegative(barContentSpacing, "barContentSpacing");
            validateNonNegative(containedBarHorizontalPadding, "containedBarHorizontalPadding");
            validateNonNegative(containedBarContentSpacing, "containedBarContentSpacing");
            validateNonNegative(dividedBarHorizontalPadding, "dividedBarHorizontalPadding");
            validateNonNegative(dividedBarContentSpacing, "dividedBarContentSpacing");
            validateNonNegative(barTrailingActionsGap, "barTrailingActionsGap");
            validateNonNegative(viewContainerShape, "viewContainerShape");
            validateNonNegative(viewHorizontalPadding, "viewHorizontalPadding");
            validateNonNegative(viewBarResultsGap, "viewBarResultsGap");
            validateNonNegative(viewResultsShape, "viewResultsShape");
            validateNonNegative(viewDockedBottomPadding, "viewDockedBottomPadding");
            validateNonNegative(viewFullScreenBottomPadding, "viewFullScreenBottomPadding");
            validateNonNegative(viewMinWidth, "viewMinWidth");
            validateNonNegative(viewMaxWidth, "viewMaxWidth");
            validateNonNegative(viewDockedMinHeight, "viewDockedMinHeight");
            validateNonNegative(viewFullScreenDividedHeaderHeight, "viewFullScreenDividedHeaderHeight");
        }
    }

    /// Component tokens for picker fields and their preset popup surfaces.
    ///
    /// @param openButtonSize                the trailing open button size
    /// @param openButtonShape               the trailing open button radius
    /// @param popupShape                    the popup surface radius
    /// @param popupPadding                  the popup preset content padding
    /// @param popupSpacing                  the spacing between popup preset list and picker content
    /// @param presetListWidth               the preset list preferred width
    /// @param presetListSpacing             the spacing between preset buttons
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `openButtonSize`, `openButtonShape`, `popupShape`, `popupPadding`,
        ///         `popupSpacing`, `presetListWidth`, `presetListSpacing`, `presetButtonHorizontalPadding`
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
    /// @param containerWidth        the docked and modal calendar width
    /// @param dockedContainerShape  the docked calendar container radius
    /// @param modalContainerShape   the modal calendar container radius
    /// @param horizontalPadding     the horizontal padding around calendar targets
    /// @param containerSpacing      the spacing between container rows
    /// @param headerHeight          the month and year navigation header height
    /// @param headerSpacing         the spacing between adjacent header controls
    /// @param navigationButtonSize  the previous and next navigation button target size
    /// @param navigationButtonShape the previous and next navigation button radius
    /// @param menuButtonHeight      the month and year menu button height
    /// @param dayCellSize           the day and weekday target size
    /// @param dayStateLayerSize     the selected day and state-layer diameter
    /// @param dayCellShape          the selected day and range endpoint radius
    /// @param gridGap               the gap between adjacent day targets
    @NotNullByDefault
    record DatePickerTokens(
            double containerWidth,
            double dockedContainerShape,
            double modalContainerShape,
            double horizontalPadding,
            double containerSpacing,
            double headerHeight,
            double headerSpacing,
            double navigationButtonSize,
            double navigationButtonShape,
            double menuButtonHeight,
            double dayCellSize,
            double dayStateLayerSize,
            double dayCellShape,
            double gridGap
    ) {
        /// Validates date picker tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerWidth`, `dockedContainerShape`, `modalContainerShape`, `horizontalPadding`,
        ///         `containerSpacing`, `headerHeight`, `headerSpacing`, `navigationButtonSize`,
        ///         `navigationButtonShape`, `menuButtonHeight`, `dayCellSize`, `dayStateLayerSize`,
        ///         `dayCellShape`, `gridGap`
        public DatePickerTokens {
            validateNonNegative(containerWidth, "containerWidth");
            validateNonNegative(dockedContainerShape, "dockedContainerShape");
            validateNonNegative(modalContainerShape, "modalContainerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(containerSpacing, "containerSpacing");
            validateNonNegative(headerHeight, "headerHeight");
            validateNonNegative(headerSpacing, "headerSpacing");
            validateNonNegative(navigationButtonSize, "navigationButtonSize");
            validateNonNegative(navigationButtonShape, "navigationButtonShape");
            validateNonNegative(menuButtonHeight, "menuButtonHeight");
            validateNonNegative(dayCellSize, "dayCellSize");
            validateNonNegative(dayStateLayerSize, "dayStateLayerSize");
            validateNonNegative(dayCellShape, "dayCellShape");
            validateNonNegative(gridGap, "gridGap");
        }
    }

    /// Component tokens for time pickers.
    ///
    /// @param containerShape         the picker container radius
    /// @param containerPadding       the picker container padding
    /// @param containerSpacing       the spacing between container rows
    /// @param displaySpacing         the spacing between selected time display fields
    /// @param displayCellShape       the selected time display field radius
    /// @param displayCellWidth       the selected hour or minute display width
    /// @param display24HourCellWidth the selected hour display width in 24-hour mode
    /// @param displayCellHeight      the selected hour or minute display height
    /// @param periodVerticalWidth    the vertical period selector width
    /// @param periodVerticalHeight   the vertical period selector height
    /// @param periodHorizontalWidth  the horizontal period selector width
    /// @param periodHorizontalHeight the horizontal period selector height
    /// @param dialSize               the clock dial diameter
    /// @param dialHandleSize         the clock dial selector handle diameter
    /// @param dialCenterSize         the clock dial center dot diameter
    /// @param dialTrackWidth         the clock dial selector track width
    /// @param inputFieldWidth        the keyboard input field width
    /// @param inputFieldHeight       the keyboard input field height
    @NotNullByDefault
    record TimePickerTokens(
            double containerShape,
            double containerPadding,
            double containerSpacing,
            double displaySpacing,
            double displayCellShape,
            double displayCellWidth,
            double display24HourCellWidth,
            double displayCellHeight,
            double periodVerticalWidth,
            double periodVerticalHeight,
            double periodHorizontalWidth,
            double periodHorizontalHeight,
            double dialSize,
            double dialHandleSize,
            double dialCenterSize,
            double dialTrackWidth,
            double inputFieldWidth,
            double inputFieldHeight
    ) {
        /// Validates time picker tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerShape`, `containerPadding`, `containerSpacing`, `displaySpacing`,
        ///         `displayCellShape`, `displayCellWidth`, `display24HourCellWidth`, `displayCellHeight`,
        ///         `periodVerticalWidth`, `periodVerticalHeight`, `periodHorizontalWidth`, `periodHorizontalHeight`,
        ///         `dialSize`, `dialHandleSize`, `dialCenterSize`, `dialTrackWidth`,
        ///         `inputFieldWidth`, `inputFieldHeight`
        public TimePickerTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(containerPadding, "containerPadding");
            validateNonNegative(containerSpacing, "containerSpacing");
            validateNonNegative(displaySpacing, "displaySpacing");
            validateNonNegative(displayCellShape, "displayCellShape");
            validateNonNegative(displayCellWidth, "displayCellWidth");
            validateNonNegative(display24HourCellWidth, "display24HourCellWidth");
            validateNonNegative(displayCellHeight, "displayCellHeight");
            validateNonNegative(periodVerticalWidth, "periodVerticalWidth");
            validateNonNegative(periodVerticalHeight, "periodVerticalHeight");
            validateNonNegative(periodHorizontalWidth, "periodHorizontalWidth");
            validateNonNegative(periodHorizontalHeight, "periodHorizontalHeight");
            validateNonNegative(dialSize, "dialSize");
            validateNonNegative(dialHandleSize, "dialHandleSize");
            validateNonNegative(dialCenterSize, "dialCenterSize");
            validateNonNegative(dialTrackWidth, "dialTrackWidth");
            validateNonNegative(inputFieldWidth, "inputFieldWidth");
            validateNonNegative(inputFieldHeight, "inputFieldHeight");
        }
    }

    /// Component tokens for sheet containers.
    ///
    /// @param sideContainerWidth       the default side sheet container width
    /// @param sideContainerMaxWidth    the maximum side sheet container width
    /// @param sideContainerShape       the modal or detached side sheet corner radius
    /// @param bottomContainerMaxWidth  the maximum bottom sheet container width
    /// @param bottomContainerShape     the bottom sheet top corner radius
    /// @param contentPadding           the sheet content padding
    /// @param headerPadding            the sheet header edge padding
    /// @param headerContentSpacing     the spacing below the sheet header
    /// @param dragHandleVerticalPadding the bottom sheet drag handle top and bottom padding
    /// @param dragHandleWidth          the bottom sheet drag handle width
    /// @param dragHandleHeight         the bottom sheet drag handle height
    @NotNullByDefault
    record SheetTokens(
            double sideContainerWidth,
            double sideContainerMaxWidth,
            double sideContainerShape,
            double bottomContainerMaxWidth,
            double bottomContainerShape,
            double contentPadding,
            double headerPadding,
            double headerContentSpacing,
            double dragHandleVerticalPadding,
            double dragHandleWidth,
            double dragHandleHeight
    ) {
        /// Validates sheet tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `sideContainerWidth`, `sideContainerMaxWidth`, `sideContainerShape`, `bottomContainerMaxWidth`,
        ///         `bottomContainerShape`, `contentPadding`, `headerPadding`, `headerContentSpacing`,
        ///         `dragHandleVerticalPadding`, `dragHandleWidth`, `dragHandleHeight`
        /// @throws IllegalArgumentException if `sideContainerMaxWidth` is less than
        ///         `sideContainerWidth`
        public SheetTokens {
            validateNonNegative(sideContainerWidth, "sideContainerWidth");
            validateNonNegative(sideContainerMaxWidth, "sideContainerMaxWidth");
            validateNonNegative(sideContainerShape, "sideContainerShape");
            validateNonNegative(bottomContainerMaxWidth, "bottomContainerMaxWidth");
            validateNonNegative(bottomContainerShape, "bottomContainerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(headerPadding, "headerPadding");
            validateNonNegative(headerContentSpacing, "headerContentSpacing");
            validateNonNegative(dragHandleVerticalPadding, "dragHandleVerticalPadding");
            validateNonNegative(dragHandleWidth, "dragHandleWidth");
            validateNonNegative(dragHandleHeight, "dragHandleHeight");
            if (sideContainerMaxWidth < sideContainerWidth) {
                throw new IllegalArgumentException(
                        "sideContainerMaxWidth must be greater than or equal to sideContainerWidth"
                );
            }
        }
    }

    /// Component tokens for scrims.
    ///
    /// @param containerOpacity the scrim container opacity
    @NotNullByDefault
    record ScrimTokens(double containerOpacity) {
        /// Validates scrim tokens.
        ///
        /// @throws IllegalArgumentException if `containerOpacity` is less than `0.0` or greater than `1.0`
        public ScrimTokens {
            validateOpacity(containerOpacity);
        }
    }

    /// Tokens shared by selection controls.
    ///
    /// @param touchTargetSize                 the preferred checkbox and radio touch target size
    /// @param stateLayerSize                  the checkbox and radio indicator state layer size
    /// @param checkboxContainerSize           the checkbox container size
    /// @param checkboxSelectedMarkWidth       the selected checkbox mark width
    /// @param checkboxSelectedMarkHeight      the selected checkbox mark height
    /// @param checkboxIndeterminateMarkWidth  the indeterminate checkbox mark width
    /// @param checkboxIndeterminateMarkHeight the indeterminate checkbox mark height
    /// @param radioContainerSize              the radio indicator container size
    /// @param radioSelectedDotSize            the selected radio dot size
    /// @param trackShape                      the switch track radius
    /// @param switchTouchTargetSize           the preferred switch touch target size
    /// @param switchTrackWidth                the switch track width
    /// @param switchTrackHeight               the switch track height
    /// @param switchStateLayerSize            the switch state layer size
    /// @param switchUnselectedHandleSize      the unselected switch handle size
    /// @param switchWithIconHandleSize        the switch handle size when it contains an icon
    /// @param switchSelectedHandleSize        the selected switch handle size
    /// @param switchPressedHandleSize         the pressed switch handle size
    /// @param switchIconSize                  the selected or unselected switch handle icon size
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
            double switchWithIconHandleSize,
            double switchSelectedHandleSize,
            double switchPressedHandleSize,
            double switchIconSize
    ) {
        /// Creates selection tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `touchTargetSize`, `stateLayerSize`, `checkboxContainerSize`, `checkboxSelectedMarkWidth`,
        ///         `checkboxSelectedMarkHeight`, `checkboxIndeterminateMarkWidth`, `checkboxIndeterminateMarkHeight`, `radioContainerSize`,
        ///         `radioSelectedDotSize`, `trackShape`, `switchTouchTargetSize`, `switchTrackWidth`,
        ///         `switchTrackHeight`, `switchStateLayerSize`, `switchUnselectedHandleSize`, `switchWithIconHandleSize`,
        ///         `switchSelectedHandleSize`, `switchPressedHandleSize`, `switchIconSize`
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
            validateNonNegative(switchWithIconHandleSize, "switchWithIconHandleSize");
            validateNonNegative(switchSelectedHandleSize, "switchSelectedHandleSize");
            validateNonNegative(switchPressedHandleSize, "switchPressedHandleSize");
            validateNonNegative(switchIconSize, "switchIconSize");
        }
    }

    /// Tokens for the Material slider size scale.
    ///
    /// @param extraSmall the extra-small slider tokens
    /// @param small      the small slider tokens
    /// @param medium     the medium slider tokens
    /// @param large      the large slider tokens
    /// @param extraLarge the extra-large slider tokens
    @NotNullByDefault
    record SliderSizingTokens(
            SliderSizeTokens extraSmall,
            SliderSizeTokens small,
            SliderSizeTokens medium,
            SliderSizeTokens large,
            SliderSizeTokens extraLarge
    ) {
        /// Creates slider sizing tokens.
        ///
        /// @throws NullPointerException if one of the following values is `null`:
        ///         `extraSmall`, `small`, `medium`, `large`,
        ///         `extraLarge`
        public SliderSizingTokens {
            Objects.requireNonNull(extraSmall, "extraSmall");
            Objects.requireNonNull(small, "small");
            Objects.requireNonNull(medium, "medium");
            Objects.requireNonNull(large, "large");
            Objects.requireNonNull(extraLarge, "extraLarge");
        }
    }

    /// Tokens for one Material slider size.
    ///
    /// @param trackThickness the active and inactive track height
    /// @param trackShape     the outer track corner radius
    /// @param thumbSize      the handle long-side size
    /// @param iconSize       the inset-icon size, or zero when inset icons are unavailable
    /// @param iconPadding    the inset-icon distance from the outer track edge
    @NotNullByDefault
    record SliderSizeTokens(
            double trackThickness,
            double trackShape,
            double thumbSize,
            double iconSize,
            double iconPadding
    ) {
        /// Creates tokens for one slider size.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `trackThickness`, `trackShape`, `thumbSize`, `iconSize`,
        ///         `iconPadding`
        public SliderSizeTokens {
            validateNonNegative(trackThickness, "trackThickness");
            validateNonNegative(trackShape, "trackShape");
            validateNonNegative(thumbSize, "thumbSize");
            validateNonNegative(iconSize, "iconSize");
            validateNonNegative(iconPadding, "iconPadding");
        }
    }

    /// Tokens shared by sliders.
    ///
    /// @param sizing                     the Material slider size-scale tokens
    /// @param stopIndicatorSize          the diameter of the inactive-track stop indicator
    /// @param stopIndicatorTrailingSpace the distance from the outer track edge to the nearest stop-indicator edge
    /// @param thumbWidth                 the slider handle short-side width while enabled or hovered
    /// @param focusedThumbWidth          the slider handle short-side width while keyboard focused
    /// @param pressedThumbWidth          the slider handle short-side width while pressed
    /// @param thumbTrackGap              the gap between the handle and each adjacent track segment
    /// @param touchTargetSize            the preferred slider touch target size
    @NotNullByDefault
    record SliderTokens(
            SliderSizingTokens sizing,
            double stopIndicatorSize,
            double stopIndicatorTrailingSpace,
            double thumbWidth,
            double focusedThumbWidth,
            double pressedThumbWidth,
            double thumbTrackGap,
            double touchTargetSize
    ) {
        /// Creates slider tokens.
        ///
        /// @throws NullPointerException if `sizing` is `null`
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `stopIndicatorSize`, `stopIndicatorTrailingSpace`, `thumbWidth`, `focusedThumbWidth`,
        ///         `pressedThumbWidth`, `thumbTrackGap`, `touchTargetSize`
        public SliderTokens {
            Objects.requireNonNull(sizing, "sizing");
            validateNonNegative(stopIndicatorSize, "stopIndicatorSize");
            validateNonNegative(stopIndicatorTrailingSpace, "stopIndicatorTrailingSpace");
            validateNonNegative(thumbWidth, "thumbWidth");
            validateNonNegative(focusedThumbWidth, "focusedThumbWidth");
            validateNonNegative(pressedThumbWidth, "pressedThumbWidth");
            validateNonNegative(thumbTrackGap, "thumbTrackGap");
            validateNonNegative(touchTargetSize, "touchTargetSize");
        }
    }

    /// Tokens shared by chip variants.
    ///
    /// @param height                the preferred chip height
    /// @param containerShape        the chip container radius
    /// @param horizontalPadding     the horizontal content padding for chips without a leading graphic
    /// @param iconHorizontalPadding the horizontal content padding for chips with a leading graphic
    /// @param elementSpacing        the spacing between chip content elements
    /// @param iconSize              the size of a leading or trailing icon
    /// @param avatarSize            the size of a leading avatar image
    /// @param avatarShape           the corner radius used for avatar images
    /// @param outlineWidth          the outline stroke width for flat unselected chips
    /// @param groupHorizontalGap    the horizontal gap between chips in a chip group
    /// @param groupVerticalGap      the vertical gap between wrapped rows in a chip group
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `height`, `containerShape`, `horizontalPadding`, `iconHorizontalPadding`,
        ///         `elementSpacing`, `iconSize`, `avatarSize`, `avatarShape`,
        ///         `outlineWidth`, `groupHorizontalGap`, `groupVerticalGap`
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
    /// @param thickness                     the default track thickness
    /// @param shape                         the progress indicator radius
    /// @param indicatorSize                 the flat circular indicator size
    /// @param waveIndicatorSize             the wavy circular indicator size
    /// @param linearWaveAmplitude           the linear wavy indicator amplitude
    /// @param linearWavelength              the determinate linear wavy indicator wavelength
    /// @param linearIndeterminateWavelength the indeterminate linear wavy indicator wavelength
    /// @param linearTrackGap                the gap between the linear active indicator and track
    /// @param linearStopSize                the stop indicator diameter at the end of the linear track
    /// @param circularWaveAmplitude         the circular wavy indicator amplitude
    /// @param circularWavelength            the circular wavy indicator wavelength
    /// @param circularTrackGap              the gap between the circular active indicator and track
    @NotNullByDefault
    record ProgressTokens(
            double thickness,
            double shape,
            double indicatorSize,
            double waveIndicatorSize,
            double linearWaveAmplitude,
            double linearWavelength,
            double linearIndeterminateWavelength,
            double linearTrackGap,
            double linearStopSize,
            double circularWaveAmplitude,
            double circularWavelength,
            double circularTrackGap
    ) {
        /// Creates progress tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `thickness`, `shape`, `indicatorSize`, `waveIndicatorSize`,
        ///         `linearWaveAmplitude`, `linearWavelength`, `linearIndeterminateWavelength`, `linearTrackGap`,
        ///         `linearStopSize`, `circularWaveAmplitude`, `circularWavelength`, `circularTrackGap`
        public ProgressTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(shape, "shape");
            validateNonNegative(indicatorSize, "indicatorSize");
            validateNonNegative(waveIndicatorSize, "waveIndicatorSize");
            validateNonNegative(linearWaveAmplitude, "linearWaveAmplitude");
            validateNonNegative(linearWavelength, "linearWavelength");
            validateNonNegative(linearIndeterminateWavelength, "linearIndeterminateWavelength");
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerSize`, `indicatorSize`
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerShape`, `contentPadding`
        public SurfaceTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
        }
    }

    /// Tokens used by carousels.
    ///
    /// @param trackHorizontalPadding the leading and trailing track padding
    /// @param trackVerticalPadding   the top and bottom track padding
    /// @param itemSpacing            the spacing between carousel items
    /// @param itemShape              the corner radius of a carousel item mask
    /// @param smallItemMinWidth      the minimum width of a contained small item
    /// @param smallItemMaxWidth      the maximum width of a contained small item
    /// @param largeItemMaxWidth      the preferred maximum width of a contained large item
    @NotNullByDefault
    record CarouselTokens(
            double trackHorizontalPadding,
            double trackVerticalPadding,
            double itemSpacing,
            double itemShape,
            double smallItemMinWidth,
            double smallItemMaxWidth,
            double largeItemMaxWidth
    ) {
        /// Creates carousel tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `trackHorizontalPadding`, `trackVerticalPadding`, `itemSpacing`, `itemShape`,
        ///         `smallItemMinWidth`, `smallItemMaxWidth`, `largeItemMaxWidth`
        /// @throws IllegalArgumentException if `smallItemMaxWidth` is less than `smallItemMinWidth`,
        ///         or if `largeItemMaxWidth` is less than `smallItemMaxWidth`
        public CarouselTokens {
            validateNonNegative(trackHorizontalPadding, "trackHorizontalPadding");
            validateNonNegative(trackVerticalPadding, "trackVerticalPadding");
            validateNonNegative(itemSpacing, "itemSpacing");
            validateNonNegative(itemShape, "itemShape");
            validateNonNegative(smallItemMinWidth, "smallItemMinWidth");
            validateNonNegative(smallItemMaxWidth, "smallItemMaxWidth");
            validateNonNegative(largeItemMaxWidth, "largeItemMaxWidth");
            if (smallItemMaxWidth < smallItemMinWidth) {
                throw new IllegalArgumentException("smallItemMaxWidth must not be less than smallItemMinWidth");
            }
            if (largeItemMaxWidth < smallItemMaxWidth) {
                throw new IllegalArgumentException("largeItemMaxWidth must not be less than smallItemMaxWidth");
            }
        }
    }

    /// Tokens used by cards.
    ///
    /// @param containerShape the card container radius
    /// @param contentPadding the card content padding
    /// @param outlineWidth   the outlined card border width
    @NotNullByDefault
    record CardTokens(
            double containerShape,
            double contentPadding,
            double outlineWidth
    ) {
        /// Creates card tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerShape`, `contentPadding`, `outlineWidth`
        public CardTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }

    /// Tokens used by dialogs.
    ///
    /// @param containerShape    the dialog container radius
    /// @param contentPadding    the dialog content padding
    /// @param containerMinWidth the minimum dialog container width
    /// @param containerMaxWidth the maximum dialog container width
    /// @param actionSpacing     the spacing between dialog action buttons
    /// @param iconSize          the dialog graphic icon size
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerShape`, `contentPadding`, `containerMinWidth`, `containerMaxWidth`,
        ///         `actionSpacing`, `iconSize`
        /// @throws IllegalArgumentException if `containerMaxWidth` is less than `containerMinWidth`
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
    /// @param containerShape            the snackbar container radius
    /// @param contentPadding            the snackbar content padding
    /// @param containerMinWidth         the minimum snackbar container width
    /// @param containerMaxWidth         the maximum snackbar container width
    /// @param singleLineContainerHeight the single-line snackbar container height
    /// @param twoLineContainerHeight    the two-line snackbar container height
    /// @param actionContainerHeight     the snackbar action button container height
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerShape`, `contentPadding`, `containerMinWidth`, `containerMaxWidth`,
        ///         `singleLineContainerHeight`, `twoLineContainerHeight`, `actionContainerHeight`
        /// @throws IllegalArgumentException if `containerMaxWidth` is less than `containerMinWidth`
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
    /// @param verticalPadding    the vertical banner content padding
    /// @param horizontalPadding  the horizontal banner content padding
    /// @param contentSpacing     the spacing between icon, message, and actions
    /// @param actionSpacing      the spacing between action nodes
    @NotNullByDefault
    record BannerTokens(
            double containerMinHeight,
            double verticalPadding,
            double horizontalPadding,
            double contentSpacing,
            double actionSpacing
    ) {
        /// Creates banner tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerMinHeight`, `verticalPadding`, `horizontalPadding`, `contentSpacing`,
        ///         `actionSpacing`
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
    /// @param plainContainerShape               the plain tooltip container radius
    /// @param plainVerticalPadding              the plain tooltip vertical content padding
    /// @param plainHorizontalPadding            the plain tooltip horizontal content padding
    /// @param richContainerShape                the rich tooltip container radius
    /// @param richTopPadding                    the rich tooltip top content padding
    /// @param richHorizontalPadding             the rich tooltip horizontal content padding
    /// @param richBottomPadding                 the rich tooltip bottom content padding
    /// @param richContentSpacing                the spacing between rich tooltip content rows
    /// @param richPreferredWidth                the rich tooltip preferred content width
    /// @param richActionSpacing                 the spacing between rich tooltip action nodes
    /// @param richActionButtonHeight            the rich tooltip action button container height
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `plainContainerShape`, `plainVerticalPadding`, `plainHorizontalPadding`, `richContainerShape`,
        ///         `richTopPadding`, `richHorizontalPadding`, `richBottomPadding`, `richContentSpacing`,
        ///         `richPreferredWidth`, `richActionSpacing`, `richActionButtonHeight`, `richActionButtonHorizontalPadding`
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
    /// @param thickness  the divider line thickness
    /// @param insetStart the leading inset before the divider line
    /// @param insetEnd   the trailing inset after the divider line
    @NotNullByDefault
    record DividerTokens(
            double thickness,
            double insetStart,
            double insetEnd
    ) {
        /// Creates divider tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `thickness`, `insetStart`, `insetEnd`
        public DividerTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(insetStart, "insetStart");
            validateNonNegative(insetEnd, "insetEnd");
        }
    }

    /// Tokens used by badges.
    ///
    /// @param smallSize         the dot badge size
    /// @param largeHeight       the text badge height
    /// @param largeMinWidth     the text badge minimum width
    /// @param containerShape    the text badge container radius
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `smallSize`, `largeHeight`, `largeMinWidth`, `containerShape`,
        ///         `horizontalPadding`
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
    /// @param containerSize  the avatar container size
    /// @param containerShape the avatar container radius
    @NotNullByDefault
    record AvatarTokens(
            double containerSize,
            double containerShape
    ) {
        /// Creates avatar tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerSize`, `containerShape`
        public AvatarTokens {
            validateNonNegative(containerSize, "containerSize");
            validateNonNegative(containerShape, "containerShape");
        }
    }

    /// Tokens used by top app bars.
    ///
    /// @param containerHeight       the small top app bar container height
    /// @param mediumContainerHeight the medium top app bar container height
    /// @param largeContainerHeight  the large top app bar container height
    /// @param mediumFlexibleContainerHeight the medium flexible container height without a subtitle
    /// @param mediumFlexibleSubtitleContainerHeight the medium flexible container height with a subtitle
    /// @param largeFlexibleContainerHeight the large flexible container height without a subtitle
    /// @param largeFlexibleSubtitleContainerHeight the large flexible container height with a subtitle
    /// @param edgePadding            the outer space before leading and after trailing action slots
    /// @param horizontalPadding     the horizontal content padding
    /// @param mediumBottomPadding   the medium top app bar bottom content padding
    /// @param largeBottomPadding    the large top app bar bottom content padding
    /// @param flexibleBottomPadding the bottom space below flexible title content
    /// @param contentSpacing        the spacing between leading, title, and trailing regions
    /// @param actionSpacing         the spacing between trailing action nodes
    @NotNullByDefault
    record TopAppBarTokens(
            double containerHeight,
            double mediumContainerHeight,
            double largeContainerHeight,
            double mediumFlexibleContainerHeight,
            double mediumFlexibleSubtitleContainerHeight,
            double largeFlexibleContainerHeight,
            double largeFlexibleSubtitleContainerHeight,
            double edgePadding,
            double horizontalPadding,
            double mediumBottomPadding,
            double largeBottomPadding,
            double flexibleBottomPadding,
            double contentSpacing,
            double actionSpacing
    ) {
        /// Creates top app bar tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `mediumContainerHeight`, `largeContainerHeight`, `mediumFlexibleContainerHeight`,
        ///         `mediumFlexibleSubtitleContainerHeight`, `largeFlexibleContainerHeight`, `largeFlexibleSubtitleContainerHeight`, `edgePadding`,
        ///         `horizontalPadding`, `mediumBottomPadding`, `largeBottomPadding`, `flexibleBottomPadding`,
        ///         `contentSpacing`, `actionSpacing`
        public TopAppBarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(mediumContainerHeight, "mediumContainerHeight");
            validateNonNegative(largeContainerHeight, "largeContainerHeight");
            validateNonNegative(mediumFlexibleContainerHeight, "mediumFlexibleContainerHeight");
            validateNonNegative(mediumFlexibleSubtitleContainerHeight,
                    "mediumFlexibleSubtitleContainerHeight");
            validateNonNegative(largeFlexibleContainerHeight, "largeFlexibleContainerHeight");
            validateNonNegative(largeFlexibleSubtitleContainerHeight,
                    "largeFlexibleSubtitleContainerHeight");
            validateNonNegative(edgePadding, "edgePadding");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(mediumBottomPadding, "mediumBottomPadding");
            validateNonNegative(largeBottomPadding, "largeBottomPadding");
            validateNonNegative(flexibleBottomPadding, "flexibleBottomPadding");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(actionSpacing, "actionSpacing");
        }
    }

    /// Tokens used by bottom app bars.
    ///
    /// @param containerHeight   the bottom app bar container height
    /// @param horizontalPadding the horizontal content padding
    /// @param contentSpacing    the spacing between action and floating action regions
    /// @param actionSpacing     the spacing between action nodes
    @NotNullByDefault
    record BottomAppBarTokens(
            double containerHeight,
            double horizontalPadding,
            double contentSpacing,
            double actionSpacing
    ) {
        /// Creates bottom app bar tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `horizontalPadding`, `contentSpacing`, `actionSpacing`
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
    /// @param containerWidth  the vertical toolbar container width
    /// @param containerShape  the toolbar container radius
    /// @param itemSlotSize    the minimum action slot width and height
    /// @param contentPadding  the padding around the toolbar item flow
    /// @param dockedContentPadding the leading and trailing padding of a docked toolbar
    /// @param itemSpacing     the minimum spacing between action slots
    /// @param dockedMaxItemSpacing the preferred maximum spacing between docked action slots
    @NotNullByDefault
    record ToolbarTokens(
            double containerHeight,
            double containerWidth,
            double containerShape,
            double itemSlotSize,
            double contentPadding,
            double dockedContentPadding,
            double itemSpacing,
            double dockedMaxItemSpacing
    ) {
        /// Creates toolbar tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `containerWidth`, `containerShape`, `itemSlotSize`,
        ///         `contentPadding`, `dockedContentPadding`, `itemSpacing`, `dockedMaxItemSpacing`
        public ToolbarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(containerWidth, "containerWidth");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(itemSlotSize, "itemSlotSize");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(dockedContentPadding, "dockedContentPadding");
            validateNonNegative(itemSpacing, "itemSpacing");
            validateNonNegative(dockedMaxItemSpacing, "dockedMaxItemSpacing");
        }
    }

    /// Tokens used by navigation bars.
    ///
    /// @param containerHeight   the navigation bar container height
    /// @param itemWidth         the preferred navigation item width
    /// @param indicatorWidth    the selected indicator width
    /// @param indicatorHeight   the selected indicator height
    /// @param indicatorShape    the selected indicator radius
    /// @param contentSpacing    the spacing between item icon and label
    /// @param horizontalPadding the horizontal padding around items
    /// @param itemSpacing       the spacing between adjacent navigation item target areas
    /// @param colors            the semantic color-role mappings used by navigation items
    /// @param elevated          whether the bar uses the elevated container treatment
    @NotNullByDefault
    record NavigationBarTokens(
            double containerHeight,
            double itemWidth,
            double indicatorWidth,
            double indicatorHeight,
            double indicatorShape,
            double contentSpacing,
            double horizontalPadding,
            double itemSpacing,
            NavigationBarColorTokens colors,
            boolean elevated
    ) {
        /// Creates navigation bar tokens.
        ///
        /// @throws NullPointerException if `colors` is `null`
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerHeight`, `itemWidth`, `indicatorWidth`, `indicatorHeight`,
        ///         `indicatorShape`, `contentSpacing`, `horizontalPadding`, `itemSpacing`
        public NavigationBarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(itemWidth, "itemWidth");
            validateNonNegative(indicatorWidth, "indicatorWidth");
            validateNonNegative(indicatorHeight, "indicatorHeight");
            validateNonNegative(indicatorShape, "indicatorShape");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(itemSpacing, "itemSpacing");
            Objects.requireNonNull(colors, "colors");
        }
    }

    /// Semantic color-role mappings used by navigation bars.
    ///
    /// @param selectedLabelRole the selected destination label role
    /// @param stateLayerRole    the destination interaction-state role
    @NotNullByDefault
    record NavigationBarColorTokens(
            ColorRole selectedLabelRole,
            ColorRole stateLayerRole
    ) {
        /// Creates navigation bar color-role tokens.
        ///
        /// @throws NullPointerException if either color role is `null`
        public NavigationBarColorTokens {
            Objects.requireNonNull(selectedLabelRole, "selectedLabelRole");
            Objects.requireNonNull(stateLayerRole, "stateLayerRole");
        }
    }

    /// Tokens used by navigation rails.
    ///
    /// @param collapsedContainerWidth       the regular collapsed navigation rail width
    /// @param narrowCollapsedContainerWidth the narrow collapsed navigation rail width
    /// @param expandedMinimumContainerWidth the minimum expanded navigation rail width
    /// @param expandedContainerWidth        the preferred expanded navigation rail width
    /// @param expandedMaximumContainerWidth the maximum expanded navigation rail width
    /// @param itemHeight                    the preferred navigation item height
    /// @param itemWidth                     the preferred navigation item width
    /// @param indicatorWidth                the selected indicator width
    /// @param indicatorHeight               the selected indicator height
    /// @param indicatorShape                the selected indicator radius
    /// @param contentSpacing                the spacing between item icon and label
    /// @param collapsedTopPadding           the top space of a collapsed rail
    /// @param collapsedBottomPadding        the bottom space of a collapsed rail
    /// @param horizontalPadding             the horizontal padding around items
    /// @param itemSpacing                   the spacing between items
    /// @param expandedTopPadding            the top padding of an expanded rail
    /// @param expandedBottomPadding         the bottom padding of an expanded rail
    /// @param headerSpacing                 the minimum spacing between header content and destination items
    /// @param modalContainerShape           the corner radius of a modal expanded rail
    @NotNullByDefault
    record NavigationRailTokens(
            double collapsedContainerWidth,
            double narrowCollapsedContainerWidth,
            double expandedMinimumContainerWidth,
            double expandedContainerWidth,
            double expandedMaximumContainerWidth,
            double itemHeight,
            double itemWidth,
            double indicatorWidth,
            double indicatorHeight,
            double indicatorShape,
            double contentSpacing,
            double collapsedTopPadding,
            double collapsedBottomPadding,
            double horizontalPadding,
            double itemSpacing,
            double expandedTopPadding,
            double expandedBottomPadding,
            double headerSpacing,
            double modalContainerShape
    ) {
        /// Creates navigation rail tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `collapsedContainerWidth`, `narrowCollapsedContainerWidth`, `expandedMinimumContainerWidth`, `expandedContainerWidth`,
        ///         `expandedMaximumContainerWidth`, `itemHeight`, `itemWidth`, `indicatorWidth`,
        ///         `indicatorHeight`, `indicatorShape`, `contentSpacing`, `collapsedTopPadding`,
        ///         `collapsedBottomPadding`, `horizontalPadding`, `itemSpacing`, `expandedTopPadding`,
        ///         `expandedBottomPadding`, `headerSpacing`, `modalContainerShape`
        /// @throws IllegalArgumentException unless the expanded widths satisfy
        ///         `expandedMinimumContainerWidth <= expandedContainerWidth <= expandedMaximumContainerWidth`
        public NavigationRailTokens {
            validateNonNegative(collapsedContainerWidth, "collapsedContainerWidth");
            validateNonNegative(narrowCollapsedContainerWidth, "narrowCollapsedContainerWidth");
            validateNonNegative(expandedMinimumContainerWidth, "expandedMinimumContainerWidth");
            validateNonNegative(expandedContainerWidth, "expandedContainerWidth");
            validateNonNegative(expandedMaximumContainerWidth, "expandedMaximumContainerWidth");
            if (expandedMinimumContainerWidth > expandedContainerWidth
                    || expandedContainerWidth > expandedMaximumContainerWidth) {
                throw new IllegalArgumentException(
                        "expanded navigation rail widths must satisfy minimum <= preferred <= maximum"
                );
            }
            validateNonNegative(itemHeight, "itemHeight");
            validateNonNegative(itemWidth, "itemWidth");
            validateNonNegative(indicatorWidth, "indicatorWidth");
            validateNonNegative(indicatorHeight, "indicatorHeight");
            validateNonNegative(indicatorShape, "indicatorShape");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(collapsedTopPadding, "collapsedTopPadding");
            validateNonNegative(collapsedBottomPadding, "collapsedBottomPadding");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(itemSpacing, "itemSpacing");
            validateNonNegative(expandedTopPadding, "expandedTopPadding");
            validateNonNegative(expandedBottomPadding, "expandedBottomPadding");
            validateNonNegative(headerSpacing, "headerSpacing");
            validateNonNegative(modalContainerShape, "modalContainerShape");
        }
    }

    /// Tokens used by navigation drawers.
    ///
    /// @param containerWidth                  the navigation drawer container width
    /// @param oneLineItemHeight               the preferred one-line drawer item height
    /// @param twoLineItemHeight               the preferred two-line drawer item height
    /// @param threeLineItemHeight             the preferred three-line drawer item height
    /// @param itemContainerShape              the drawer item container radius
    /// @param containerPadding                the padding around drawer items
    /// @param itemHorizontalPadding           the horizontal item content padding
    /// @param itemVerticalPadding             the vertical item content padding
    /// @param itemContentSpacing              the spacing between item content regions
    /// @param itemSpacing                     the spacing between drawer items
    /// @param groupChildItemHeight            the preferred one-line child item height inside collapsible groups
    /// @param groupChildItemContainerShape    the child item container radius inside collapsible groups
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
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `containerWidth`, `oneLineItemHeight`, `twoLineItemHeight`, `threeLineItemHeight`,
        ///         `itemContainerShape`, `containerPadding`, `itemHorizontalPadding`, `itemVerticalPadding`,
        ///         `itemContentSpacing`, `itemSpacing`, `groupChildItemHeight`, `groupChildItemContainerShape`,
        ///         `groupChildItemHorizontalPadding`
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
    /// @param oneLineHeight                  the preferred one-line item height
    /// @param twoLineHeight                  the preferred two-line item height
    /// @param threeLineHeight                the preferred three-line item height
    /// @param containerShape                 the list item container radius
    /// @param horizontalPadding              the horizontal content padding
    /// @param verticalPadding                the vertical content padding
    /// @param contentSpacing                 the spacing between content regions
    /// @param segmentedGap                   the gap between adjacent segmented list items
    /// @param segmentedContainerShape        the resting segmented-item container radius
    /// @param segmentedHoverContainerShape   the hovered segmented-item container radius
    /// @param segmentedActiveContainerShape  the focused, pressed, or selected segmented-item container radius
    /// @param segmentedDisabledContainerShape the disabled segmented-item container radius
    /// @param sectionHeaderHeight            the preferred list section header height
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
            double segmentedGap,
            double segmentedContainerShape,
            double segmentedHoverContainerShape,
            double segmentedActiveContainerShape,
            double segmentedDisabledContainerShape,
            double sectionHeaderHeight,
            double sectionHeaderHorizontalPadding
    ) {
        /// Creates list item tokens.
        ///
        /// @throws IllegalArgumentException if one of the following values is negative:
        ///         `oneLineHeight`, `twoLineHeight`, `threeLineHeight`, `containerShape`,
        ///         `horizontalPadding`, `verticalPadding`, `contentSpacing`, `segmentedGap`,
        ///         `segmentedContainerShape`, `segmentedHoverContainerShape`, `segmentedActiveContainerShape`,
        ///         `segmentedDisabledContainerShape`,
        ///         `sectionHeaderHeight`, `sectionHeaderHorizontalPadding`
        public ListItemTokens {
            validateNonNegative(oneLineHeight, "oneLineHeight");
            validateNonNegative(twoLineHeight, "twoLineHeight");
            validateNonNegative(threeLineHeight, "threeLineHeight");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(verticalPadding, "verticalPadding");
            validateNonNegative(contentSpacing, "contentSpacing");
            validateNonNegative(segmentedGap, "segmentedGap");
            validateNonNegative(segmentedContainerShape, "segmentedContainerShape");
            validateNonNegative(segmentedHoverContainerShape, "segmentedHoverContainerShape");
            validateNonNegative(segmentedActiveContainerShape, "segmentedActiveContainerShape");
            validateNonNegative(segmentedDisabledContainerShape, "segmentedDisabledContainerShape");
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
