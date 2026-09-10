// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3ShapeTokens;
import org.glavo.monetfx.ColorRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3ComponentTokens].
///
/// @param filledButton         tokens used by filled buttons
/// @param tonalButton          tokens used by filled tonal buttons
/// @param outlinedButton       tokens used by outlined buttons
/// @param textButton           tokens used by text buttons
/// @param elevatedButton       tokens used by elevated buttons
/// @param buttonSizing         the five-step button size token scale
/// @param iconButton           tokens used by icon buttons
/// @param floatingActionButton tokens used by floating action buttons
/// @param icon                 tokens used by icon glyph primitives
/// @param buttonGroup          tokens used by button groups
/// @param splitButton          tokens used by split buttons
/// @param segmentedButton      tokens used by segmented buttons
/// @param tab                  tokens used by tabs
/// @param field                tokens used by text input controls
/// @param textArea             tokens used by text area controls
/// @param form                 tokens used by form containers
/// @param validationSummary    tokens used by validation summaries
/// @param menu                 tokens used by menus
/// @param search               tokens used by search components
/// @param pickerField          tokens used by picker fields
/// @param datePicker           tokens used by date pickers
/// @param timePicker           tokens used by time pickers
/// @param sheet                tokens used by sheet containers
/// @param scrim                tokens used by scrims
/// @param selection            tokens used by selection controls
/// @param slider               tokens used by sliders
/// @param chip                 tokens used by chips
/// @param progress             tokens used by progress controls
/// @param loadingIndicator     tokens used by loading indicators
/// @param surface              tokens used by surfaces
/// @param carousel             tokens used by carousels
/// @param card                 tokens used by cards
/// @param dialog               tokens used by dialogs
/// @param snackbar             tokens used by snackbar controls
/// @param banner               tokens used by banners
/// @param tooltip              tokens used by tooltips
/// @param divider              tokens used by dividers
/// @param badge                tokens used by badges
/// @param avatar               tokens used by avatars
/// @param topAppBar            tokens used by top app bars
/// @param bottomAppBar         tokens used by bottom app bars
/// @param toolbar              tokens used by toolbars
/// @param navigationBar        tokens used by navigation bars
/// @param navigationRail       tokens used by navigation rails
/// @param navigationDrawer     tokens used by navigation drawers
/// @param listItem             tokens used by list items
@NotNullByDefault
public record M3ComponentTokensImpl(
        M3ComponentTokens.ButtonTokens filledButton,
        M3ComponentTokens.ButtonTokens tonalButton,
        M3ComponentTokens.ButtonTokens outlinedButton,
        M3ComponentTokens.ButtonTokens textButton,
        M3ComponentTokens.ButtonTokens elevatedButton,
        M3ComponentTokens.ButtonSizingTokens buttonSizing,
        M3ComponentTokens.IconButtonTokens iconButton,
        M3ComponentTokens.FabTokens floatingActionButton,
        M3ComponentTokens.IconTokens icon,
        M3ComponentTokens.ButtonGroupTokens buttonGroup,
        M3ComponentTokens.SplitButtonTokens splitButton,
        M3ComponentTokens.ButtonTokens segmentedButton,
        M3ComponentTokens.TabTokens tab,
        M3ComponentTokens.FieldTokens field,
        M3ComponentTokens.TextAreaTokens textArea,
        M3ComponentTokens.FormTokens form,
        M3ComponentTokens.ValidationSummaryTokens validationSummary,
        M3ComponentTokens.MenuTokens menu,
        M3ComponentTokens.SearchTokens search,
        M3ComponentTokens.PickerFieldTokens pickerField,
        M3ComponentTokens.DatePickerTokens datePicker,
        M3ComponentTokens.TimePickerTokens timePicker,
        M3ComponentTokens.SheetTokens sheet,
        M3ComponentTokens.ScrimTokens scrim,
        M3ComponentTokens.SelectionTokens selection,
        M3ComponentTokens.SliderTokens slider,
        M3ComponentTokens.ChipTokens chip,
        M3ComponentTokens.ProgressTokens progress,
        M3ComponentTokens.LoadingIndicatorTokens loadingIndicator,
        M3ComponentTokens.SurfaceTokens surface,
        M3ComponentTokens.CarouselTokens carousel,
        M3ComponentTokens.CardTokens card,
        M3ComponentTokens.DialogTokens dialog,
        M3ComponentTokens.SnackbarTokens snackbar,
        M3ComponentTokens.BannerTokens banner,
        M3ComponentTokens.TooltipTokens tooltip,
        M3ComponentTokens.DividerTokens divider,
        M3ComponentTokens.BadgeTokens badge,
        M3ComponentTokens.AvatarTokens avatar,
        M3ComponentTokens.TopAppBarTokens topAppBar,
        M3ComponentTokens.BottomAppBarTokens bottomAppBar,
        M3ComponentTokens.ToolbarTokens toolbar,
        M3ComponentTokens.NavigationBarTokens navigationBar,
        M3ComponentTokens.NavigationRailTokens navigationRail,
        M3ComponentTokens.NavigationDrawerTokens navigationDrawer,
        M3ComponentTokens.ListItemTokens listItem
) implements M3ComponentTokens {
    /// Creates a complete component token set.
    ///
    /// @throws NullPointerException if any component token group is `null`
    public M3ComponentTokensImpl {
        Objects.requireNonNull(filledButton, "filledButton");
        Objects.requireNonNull(tonalButton, "tonalButton");
        Objects.requireNonNull(outlinedButton, "outlinedButton");
        Objects.requireNonNull(textButton, "textButton");
        Objects.requireNonNull(elevatedButton, "elevatedButton");
        Objects.requireNonNull(buttonSizing, "buttonSizing");
        Objects.requireNonNull(iconButton, "iconButton");
        Objects.requireNonNull(floatingActionButton, "floatingActionButton");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(buttonGroup, "buttonGroup");
        Objects.requireNonNull(splitButton, "splitButton");
        Objects.requireNonNull(segmentedButton, "segmentedButton");
        Objects.requireNonNull(tab, "tab");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(textArea, "textArea");
        Objects.requireNonNull(form, "form");
        Objects.requireNonNull(validationSummary, "validationSummary");
        Objects.requireNonNull(menu, "menu");
        Objects.requireNonNull(search, "search");
        Objects.requireNonNull(pickerField, "pickerField");
        Objects.requireNonNull(datePicker, "datePicker");
        Objects.requireNonNull(timePicker, "timePicker");
        Objects.requireNonNull(sheet, "sheet");
        Objects.requireNonNull(scrim, "scrim");
        Objects.requireNonNull(selection, "selection");
        Objects.requireNonNull(slider, "slider");
        Objects.requireNonNull(chip, "chip");
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(loadingIndicator, "loadingIndicator");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(carousel, "carousel");
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(dialog, "dialog");
        Objects.requireNonNull(snackbar, "snackbar");
        Objects.requireNonNull(banner, "banner");
        Objects.requireNonNull(tooltip, "tooltip");
        Objects.requireNonNull(divider, "divider");
        Objects.requireNonNull(badge, "badge");
        Objects.requireNonNull(avatar, "avatar");
        Objects.requireNonNull(topAppBar, "topAppBar");
        Objects.requireNonNull(bottomAppBar, "bottomAppBar");
        Objects.requireNonNull(toolbar, "toolbar");
        Objects.requireNonNull(navigationBar, "navigationBar");
        Objects.requireNonNull(navigationRail, "navigationRail");
        Objects.requireNonNull(navigationDrawer, "navigationDrawer");
        Objects.requireNonNull(listItem, "listItem");
    }

    /// Creates component tokens for a profile.
    ///
    /// @param profile     the Material profile whose component defaults should be generated
    /// @param shapeTokens the shape scale used by generated component tokens
    /// @param density     the density adjustment applied to selected generated component metrics
    /// @return a component token set generated from the supplied profile, shape scale, and density
    /// @throws NullPointerException if `profile`, `shapeTokens`, or `density` is `null`
    public static M3ComponentTokens defaultsForProfile(
            M3Profile profile,
            M3ShapeTokens shapeTokens,
            M3Density density
    ) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(density, "density");

        boolean expressive = profile == M3Profile.EXPRESSIVE_2025;

        double buttonHeight = density.compact(40.0);
        double iconSmallSize = expressive ? 20.0 : 18.0;
        double iconMediumSize = 24.0;
        double iconLargeSize = expressive ? 36.0 : 32.0;
        double iconExtraLargeSize = expressive ? 44.0 : 40.0;
        double segmentedButtonHeight = density.compact(40.0);
        double tabHeight = density.compact(48.0);
        double tabMinWidth = 90.0;
        double fieldHeight = density.compact(56.0);
        double textAreaHeight = density.compact(112.0);
        double menuItemHeight = density.compact(expressive ? 44.0 : 48.0);
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
        double navigationBarHeight = density.compact(expressive ? 64.0 : 80.0);
        double navigationItemWidth = 80.0;
        double navigationIndicatorWidth = expressive ? 56.0 : 64.0;
        double navigationIndicatorHeight = 32.0;
        double navigationRailWidth = expressive ? 96.0 : 80.0;
        double navigationRailItemWidth = 80.0;
        double navigationRailIndicatorWidth = 56.0;
        double navigationDrawerWidth = 360.0;
        double navigationDrawerOneLineItemHeight = Math.max(48.0, density.compact(56.0));
        double navigationDrawerTwoLineItemHeight = Math.max(48.0, density.compact(72.0));
        double navigationDrawerThreeLineItemHeight = Math.max(48.0, density.compact(88.0));
        double listItemOneLineHeight =
                Math.max(48.0, density.compact(expressive ? 64.0 : 56.0));
        double listItemTwoLineHeight =
                Math.max(48.0, density.compact(expressive ? 80.0 : 72.0));
        double listItemThreeLineHeight =
                Math.max(48.0, density.compact(expressive ? 96.0 : 88.0));
        double listSectionHeaderHeight =
                Math.max(48.0, density.compact(expressive ? 56.0 : 48.0));
        double progressLinearWaveAmplitude = expressive ? 3.0 : 0.0;
        double progressCircularWaveAmplitude = expressive ? 1.6 : 0.0;
        double loadingIndicatorContainerSize = 48.0;
        double loadingIndicatorIndicatorSize = 38.0;
        double progressCircularIndicatorSize = 40.0;
        double progressCircularWaveIndicatorSize = 48.0;
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
}
