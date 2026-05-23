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

    /// Returns tokens used by icon buttons.
    ///
    /// @return the icon button component tokens
    ButtonTokens iconButton();

    /// Returns tokens used by floating action buttons.
    ///
    /// @return the floating action button component tokens
    FabTokens floatingActionButton();

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

    /// Returns tokens used by menus.
    ///
    /// @return the menu component tokens
    MenuTokens menu();

    /// Returns tokens used by search components.
    ///
    /// @return the search component tokens
    SearchTokens search();

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
    /// @param iconButton the icon button component tokens
    /// @param floatingActionButton the floating action button component tokens
    /// @param segmentedButton the segmented button component tokens
    /// @param tab the tab component tokens
    /// @param field the text input component tokens
    /// @param textArea the text area component tokens
    /// @param menu the menu component tokens
    /// @param search the search component tokens
    /// @param sheet the sheet component tokens
    /// @param scrim the scrim component tokens
    /// @param selection the selection control component tokens
    /// @param slider the slider component tokens
    /// @param chip the chip component tokens
    /// @param progress the progress component tokens
    /// @param card the card component tokens
    /// @param dialog the dialog component tokens
    /// @param snackbar the snackbar component tokens
    /// @param divider the divider component tokens
    /// @param badge the badge component tokens
    /// @param avatar the avatar component tokens
    /// @param topAppBar the top app bar component tokens
    /// @param bottomAppBar the bottom app bar component tokens
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
            ButtonTokens iconButton,
            FabTokens floatingActionButton,
            ButtonTokens segmentedButton,
            TabTokens tab,
            FieldTokens field,
            TextAreaTokens textArea,
            MenuTokens menu,
            SearchTokens search,
            SheetTokens sheet,
            ScrimTokens scrim,
            SelectionTokens selection,
            SliderTokens slider,
            ChipTokens chip,
            ProgressTokens progress,
            CardTokens card,
            DialogTokens dialog,
            SnackbarTokens snackbar,
            DividerTokens divider,
            BadgeTokens badge,
            AvatarTokens avatar,
            TopAppBarTokens topAppBar,
            BottomAppBarTokens bottomAppBar,
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
                iconButton,
                floatingActionButton,
                segmentedButton,
                tab,
                field,
                textArea,
                menu,
                search,
                sheet,
                scrim,
                selection,
                slider,
                chip,
                progress,
                card,
                dialog,
                snackbar,
                divider,
                badge,
                avatar,
                topAppBar,
                bottomAppBar,
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

        double buttonHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double iconButtonSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double fabSmallSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double fabRegularSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double fabLargeSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 104.0 : 96.0);
        double segmentedButtonHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double tabHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 56.0 : 48.0);
        double tabMinWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 104.0 : 90.0);
        double fieldHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double textAreaHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 128.0 : 112.0);
        double menuItemHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 56.0 : 48.0);
        double searchBarHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double searchViewResultHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double sideSheetWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 384.0 : 360.0);
        double bottomSheetHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 360.0 : 320.0);
        double chipHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 36.0 : 32.0);
        double badgeSmallSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 8.0 : 6.0);
        double badgeLargeHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 18.0 : 16.0);
        double badgeLargeMinWidth = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 18.0 : 16.0);
        double avatarSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 44.0 : 40.0);
        double topAppBarHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 72.0 : 64.0);
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
        double progressCircularWaveAmplitude = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 2.0 : 0.0);
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
        double menuContainerPadding = density.apply(expressive ? 10.0 : 8.0);
        double menuItemContainerShape = expressive ? shapeTokens.small() : shapeTokens.extraSmall();
        double menuItemHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double menuItemContentSpacing = density.apply(expressive ? 16.0 : 12.0);
        double searchBarHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double searchBarContentSpacing = density.apply(expressive ? 16.0 : 12.0);
        double searchViewContainerShape = expressive ? shapeTokens.extraLarge() : 28.0;
        double searchViewResultPadding = density.apply(expressive ? 12.0 : 8.0);
        double sheetContentPadding = density.apply(expressive ? 28.0 : 24.0);
        double sheetHeaderPadding = density.apply(expressive ? 28.0 : 24.0);
        double sheetDragHandleWidth = density.apply(expressive ? 36.0 : 32.0);
        double sheetDragHandleHeight = density.apply(expressive ? 5.0 : 4.0);
        double cardContainerShape = expressive ? shapeTokens.large() : shapeTokens.medium();
        double cardContentPadding = density.apply(expressive ? 20.0 : 16.0);
        double dialogContentPadding = density.apply(expressive ? 28.0 : 24.0);
        double snackbarContainerShape = expressive ? shapeTokens.medium() : shapeTokens.extraSmall();
        double snackbarContentPadding = density.apply(expressive ? 18.0 : 16.0);
        double appBarHorizontalPadding = density.apply(expressive ? 24.0 : 16.0);
        double appBarContentSpacing = density.apply(expressive ? 20.0 : 16.0);
        double appBarActionSpacing = density.apply(expressive ? 12.0 : 8.0);
        double buttonHorizontalPadding = density.apply(expressive ? 28.0 : 24.0);
        double textButtonHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double fabSmallHorizontalPadding = density.apply(expressive ? 14.0 : 12.0);
        double fabRegularHorizontalPadding = density.apply(expressive ? 18.0 : 16.0);
        double fabLargeHorizontalPadding = density.apply(expressive ? 28.0 : 24.0);
        double segmentedButtonHorizontalPadding = density.apply(expressive ? 16.0 : 12.0);
        double tabHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double tabActiveIndicatorHeight = density.apply(expressive ? 4.0 : 3.0);
        double tabActiveIndicatorShape = density.apply(expressive ? 4.0 : 3.0);
        double chipHorizontalPadding = density.apply(expressive ? 18.0 : 16.0);
        double fieldHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double textAreaHorizontalPadding = density.apply(expressive ? 20.0 : 16.0);
        double textAreaVerticalPadding = density.apply(expressive ? 20.0 : 16.0);
        double selectionTouchTargetSize = density.apply(expressive ? 48.0 : 40.0);
        double sliderTrackThickness = density.apply(expressive ? 6.0 : 4.0);
        double sliderThumbSize = density.apply(expressive ? 24.0 : 20.0);
        double sliderTouchTargetSize = density.apply(expressive ? 56.0 : 48.0);

        return create(
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), textButtonHorizontalPadding),
                new ButtonTokens(buttonHeight, shapeTokens.full(), buttonHorizontalPadding),
                new ButtonTokens(iconButtonSize, shapeTokens.full(), 0.0),
                new FabTokens(
                        fabSmallSize,
                        fabRegularSize,
                        fabLargeSize,
                        shapeTokens.medium(),
                        shapeTokens.large(),
                        shapeTokens.extraLarge(),
                        fabSmallHorizontalPadding,
                        fabRegularHorizontalPadding,
                        fabLargeHorizontalPadding
                ),
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
                new MenuTokens(
                        shapeTokens.extraSmall(),
                        menuContainerPadding,
                        menuItemHeight,
                        menuItemContainerShape,
                        menuItemHorizontalPadding,
                        menuItemContentSpacing
                ),
                new SearchTokens(
                        searchBarHeight,
                        shapeTokens.full(),
                        searchBarHorizontalPadding,
                        searchBarContentSpacing,
                        searchViewContainerShape,
                        searchViewResultPadding,
                        searchViewResultHeight
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
                new SelectionTokens(selectionTouchTargetSize, shapeTokens.full()),
                new SliderTokens(sliderTrackThickness, shapeTokens.full(), sliderThumbSize, sliderTouchTargetSize),
                new ChipTokens(chipHeight, shapeTokens.small(), chipHorizontalPadding),
                new ProgressTokens(
                        density.apply(4.0),
                        shapeTokens.full(),
                        density.apply(48.0),
                        progressLinearWaveAmplitude,
                        density.apply(40.0),
                        density.apply(4.0),
                        density.apply(4.0),
                        progressCircularWaveAmplitude,
                        density.apply(15.0),
                        density.apply(4.0)
                ),
                new CardTokens(cardContainerShape, cardContentPadding, 1.0),
                new DialogTokens(shapeTokens.extraLarge(), dialogContentPadding),
                new SnackbarTokens(snackbarContainerShape, snackbarContentPadding),
                new DividerTokens(1.0, 0.0, 0.0),
                new BadgeTokens(badgeSmallSize, badgeLargeHeight, badgeLargeMinWidth, badgeLargeHeight / 2.0, 4.0),
                new AvatarTokens(avatarSize, shapeTokens.full()),
                new TopAppBarTokens(topAppBarHeight, appBarHorizontalPadding, appBarContentSpacing, appBarActionSpacing),
                new BottomAppBarTokens(bottomAppBarHeight, appBarHorizontalPadding, appBarContentSpacing, appBarActionSpacing),
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
        append(builder, "button-icon", iconButton());
        append(builder, floatingActionButton());
        append(builder, "segmented-button", segmentedButton());
        append(builder, tab());
        append(builder, field());
        append(builder, textArea());
        append(builder, menu());
        append(builder, search());
        append(builder, sheet());
        append(builder, scrim());
        append(builder, selection());
        append(builder, slider());
        append(builder, chip());
        append(builder, progress());
        append(builder, card());
        append(builder, dialog());
        append(builder, snackbar());
        append(builder, divider());
        append(builder, badge());
        append(builder, avatar());
        append(builder, topAppBar());
        append(builder, bottomAppBar());
        append(builder, navigationBar());
        append(builder, navigationRail());
        append(builder, navigationDrawer());
        append(builder, listItem());
        return builder.toString().trim();
    }

    /// Converts component tokens into JavaFX CSS rules for m3fx controls.
    ///
    /// @return JavaFX CSS rules that apply these component tokens to M3FX controls
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendButtonRule(builder, ".m3-filled-button", filledButton());
        appendButtonRule(builder, ".m3-tonal-button", tonalButton());
        appendButtonRule(builder, ".m3-outlined-button", outlinedButton());
        appendButtonRule(builder, ".m3-text-button", textButton());
        appendButtonRule(builder, ".m3-elevated-button", elevatedButton());
        appendButtonRule(builder, ".m3-icon-button", iconButton());
        appendButtonRule(builder, ".m3-icon-toggle-button", iconButton());
        appendConnectedButtonRules(builder, filledButton());
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
        appendTabRule(builder, ".m3-tab", tab());
        appendTabIndicatorRule(builder, ".m3-tab-active-indicator", tab());
        appendFieldRule(builder, ".m3-text-field, .m3-password-field", field());
        appendTextAreaRule(builder, ".m3-text-area", textArea());
        appendFilledFieldRule(builder, ".m3-filled-field", field());
        appendOutlinedFieldRule(builder, ".m3-outlined-field", field());
        appendFilledTextAreaRule(builder, ".m3-text-area.m3-filled-field", textArea());
        appendOutlinedTextAreaRule(builder, ".m3-text-area.m3-outlined-field", textArea());
        appendMenuRule(builder, ".m3-menu.m3-menu", menu());
        appendMenuItemRule(builder, ".m3-menu .m3-menu-item.m3-menu-item", menu());
        appendSearchBarRule(builder, ".m3-search-bar.m3-search-bar", search());
        appendSearchViewRule(builder, ".m3-search-view.m3-search-view", search());
        appendSearchViewResultRule(builder, ".m3-search-view .m3-list-item.m3-list-item", search());
        appendSideSheetRule(builder, ".m3-side-sheet.m3-side-sheet", sheet());
        appendBottomSheetRule(builder, ".m3-bottom-sheet.m3-bottom-sheet", sheet());
        appendSheetHeaderRule(builder, ".m3-side-sheet .m3-sheet-header, .m3-bottom-sheet .m3-sheet-header", sheet());
        appendSheetContentRule(
                builder,
                ".m3-side-sheet .m3-sheet-content, .m3-bottom-sheet .m3-sheet-content",
                sheet()
        );
        appendBottomSheetDragHandleRule(builder, ".m3-bottom-sheet .m3-bottom-sheet-drag-handle", sheet());
        appendScrimRule(builder, ".m3-scrim.m3-scrim", scrim());
        appendSelectionRule(builder, ".m3-checkbox, .m3-radio-button, .m3-switch", selection());
        appendSwitchRule(builder, ".m3-switch", selection());
        appendSwitchBoxRule(builder, ".m3-switch .box", selection());
        appendSliderRule(builder, ".m3-slider", slider());
        appendSliderTrackRule(builder, ".m3-slider .track", slider());
        appendSliderThumbRule(builder, ".m3-slider .thumb", slider());
        appendChipRule(builder, ".m3-chip", chip());
        appendProgressBarRule(builder, ".m3-progress-bar", progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .track", progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .bar", progress());
        appendProgressIndicatorRule(builder, ".m3-progress-indicator", progress());
        appendCardRule(builder, ".m3-card", card());
        appendDialogRule(builder, ".m3-dialog-pane", dialog());
        appendSnackbarRule(builder, ".m3-snackbar", snackbar());
        appendDividerRule(builder, ".m3-divider", divider());
        appendBadgeRule(builder, ".m3-badge", badge());
        appendAvatarRule(builder, ".m3-avatar.m3-avatar", avatar());
        appendTopAppBarRule(builder, ".m3-top-app-bar", topAppBar());
        appendTopAppBarActionsRule(builder, ".m3-top-app-bar-actions", topAppBar());
        appendBottomAppBarRule(builder, ".m3-bottom-app-bar", bottomAppBar());
        appendBottomAppBarActionsRule(builder, ".m3-bottom-app-bar-actions", bottomAppBar());
        appendNavigationBarRule(builder, ".m3-navigation-bar", navigationBar());
        appendNavigationItemRule(builder, ".m3-navigation-item", navigationBar());
        appendNavigationIndicatorRule(builder, ".m3-navigation-item-indicator", navigationBar());
        appendNavigationRailRule(builder, ".m3-navigation-rail", navigationRail());
        appendNavigationRailItemRule(builder, ".m3-navigation-rail .m3-navigation-item", navigationRail());
        appendNavigationRailIndicatorRule(
                builder,
                ".m3-navigation-rail .m3-navigation-item-indicator",
                navigationRail()
        );
        appendListItemRule(builder, ".m3-list-item", listItem());
        appendListSectionHeaderRule(builder, ".m3-list-section-header", listItem());
        appendNavigationDrawerRule(builder, ".m3-navigation-drawer", navigationDrawer());
        appendNavigationDrawerItemRule(builder, ".m3-navigation-drawer .m3-list-item", navigationDrawer());
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

    /// Appends menu token declarations.
    private static void append(StringBuilder builder, MenuTokens tokens) {
        M3TokenCss.append(builder, "-m3-menu-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-menu-container-padding", M3TokenCss.pixels(tokens.containerPadding()));
        M3TokenCss.append(builder, "-m3-menu-item-height", M3TokenCss.pixels(tokens.itemHeight()));
        M3TokenCss.append(builder, "-m3-menu-item-container-shape", M3TokenCss.pixels(tokens.itemContainerShape()));
        M3TokenCss.append(builder, "-m3-menu-item-horizontal-padding", M3TokenCss.pixels(tokens.itemHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-menu-item-content-spacing", M3TokenCss.pixels(tokens.itemContentSpacing()));
    }

    /// Appends search token declarations.
    private static void append(StringBuilder builder, SearchTokens tokens) {
        M3TokenCss.append(builder, "-m3-search-bar-container-height", M3TokenCss.pixels(tokens.barHeight()));
        M3TokenCss.append(builder, "-m3-search-bar-container-shape", M3TokenCss.pixels(tokens.barContainerShape()));
        M3TokenCss.append(builder, "-m3-search-bar-horizontal-padding", M3TokenCss.pixels(tokens.barHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-search-bar-content-spacing", M3TokenCss.pixels(tokens.barContentSpacing()));
        M3TokenCss.append(builder, "-m3-search-view-container-shape", M3TokenCss.pixels(tokens.viewContainerShape()));
        M3TokenCss.append(builder, "-m3-search-view-result-padding", M3TokenCss.pixels(tokens.viewResultPadding()));
        M3TokenCss.append(builder, "-m3-search-view-result-height", M3TokenCss.pixels(tokens.resultHeight()));
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
        M3TokenCss.append(builder, "-m3-selection-track-shape", M3TokenCss.pixels(tokens.trackShape()));
    }

    /// Appends slider token declarations.
    private static void append(StringBuilder builder, SliderTokens tokens) {
        M3TokenCss.append(builder, "-m3-slider-track-thickness", M3TokenCss.pixels(tokens.trackThickness()));
        M3TokenCss.append(builder, "-m3-slider-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        M3TokenCss.append(builder, "-m3-slider-thumb-size", M3TokenCss.pixels(tokens.thumbSize()));
        M3TokenCss.append(builder, "-m3-slider-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
    }

    /// Appends chip token declarations.
    private static void append(StringBuilder builder, ChipTokens tokens) {
        M3TokenCss.append(builder, "-m3-chip-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-chip-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-chip-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
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
    }

    /// Appends snackbar token declarations.
    private static void append(StringBuilder builder, SnackbarTokens tokens) {
        M3TokenCss.append(builder, "-m3-snackbar-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-snackbar-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
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
        M3TokenCss.append(builder, "-m3-top-app-bar-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
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
    private static void appendTabRule(StringBuilder builder, String selector, TabTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-tab-min-width", M3TokenCss.pixels(tokens.tabMinWidth()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-active-indicator-height", M3TokenCss.pixels(tokens.activeIndicatorHeight()));
        appendDeclaration(builder, "-m3-active-indicator-shape", M3TokenCss.pixels(tokens.activeIndicatorShape()));
        endRule(builder);
    }

    /// Appends a tab active indicator token CSS rule.
    private static void appendTabIndicatorRule(StringBuilder builder, String selector, TabTokens tokens) {
        beginRule(builder, selector);
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

    /// Appends connected button group and split button override rules.
    private static void appendConnectedButtonRules(StringBuilder builder, ButtonTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, ".m3-button.m3-grouped-button");
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(20.0));
        endRule(builder);

        appendConnectedButtonShapeRule(
                builder,
                ".m3-button.m3-button-group-single",
                radius,
                radius,
                radius,
                radius
        );
        appendConnectedButtonShapeRule(
                builder,
                ".m3-button.m3-button-group-first",
                radius,
                "0",
                "0",
                radius
        );
        appendConnectedButtonShapeRule(
                builder,
                ".m3-button.m3-button-group-middle",
                "0",
                "0",
                "0",
                "0"
        );
        appendConnectedButtonShapeRule(
                builder,
                ".m3-button.m3-button-group-last",
                "0",
                radius,
                radius,
                "0"
        );

        beginRule(builder, ".m3-button.m3-split-button-action");
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(20.0));
        appendDeclaration(builder, "-fx-background-radius", radius + " 0 0 " + radius);
        appendDeclaration(builder, "-fx-border-radius", radius + " 0 0 " + radius);
        endRule(builder);

        beginRule(builder, ".m3-button.m3-split-button-menu");
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(0.0));
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(48.0));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(48.0));
        appendDeclaration(builder, "-fx-background-radius", "0 " + radius + " " + radius + " 0");
        appendDeclaration(builder, "-fx-border-radius", "0 " + radius + " " + radius + " 0");
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
    private static void appendFieldRule(StringBuilder builder, String selector, FieldTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a text area token CSS rule.
    private static void appendTextAreaRule(StringBuilder builder, String selector, TextAreaTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        endRule(builder);
    }

    /// Appends a filled field shape CSS rule.
    private static void appendFilledFieldRule(StringBuilder builder, String selector, FieldTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends an outlined field shape CSS rule.
    private static void appendOutlinedFieldRule(StringBuilder builder, String selector, FieldTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a filled text area shape CSS rule.
    private static void appendFilledTextAreaRule(StringBuilder builder, String selector, TextAreaTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends an outlined text area shape CSS rule.
    private static void appendOutlinedTextAreaRule(StringBuilder builder, String selector, TextAreaTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a menu token CSS rule.
    private static void appendMenuRule(StringBuilder builder, String selector, MenuTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.containerPadding()));
        endRule(builder);
    }

    /// Appends a menu item token CSS rule.
    private static void appendMenuItemRule(StringBuilder builder, String selector, MenuTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-two-line-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-three-line-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.itemContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.itemHorizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(0.0));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.itemContentSpacing()));
        endRule(builder);
    }

    /// Appends a search bar token CSS rule.
    private static void appendSearchBarRule(StringBuilder builder, String selector, SearchTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.barHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.barHeight()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.barContainerShape()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.barHorizontalPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.barContentSpacing()));
        endRule(builder);
    }

    /// Appends a search view token CSS rule.
    private static void appendSearchViewRule(StringBuilder builder, String selector, SearchTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.viewContainerShape()));
        appendDeclaration(builder, "-fx-padding", "0 0 " + M3TokenCss.pixels(tokens.viewResultPadding()) + " 0");
        endRule(builder);
    }

    /// Appends search result item token CSS rules.
    private static void appendSearchViewResultRule(StringBuilder builder, String selector, SearchTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.resultHeight()));
        appendDeclaration(builder, "-m3-two-line-height", M3TokenCss.pixels(tokens.resultHeight() + 16.0));
        appendDeclaration(builder, "-m3-three-line-height", M3TokenCss.pixels(tokens.resultHeight() + 32.0));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.barContainerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.barHorizontalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.barContentSpacing()));
        endRule(builder);
    }

    /// Appends a side sheet token CSS rule.
    private static void appendSideSheetRule(StringBuilder builder, String selector, SheetTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.sideContainerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.sideContainerWidth()));
        appendDeclaration(builder, "-fx-background-radius", radius + " 0 0 " + radius);
        endRule(builder);
    }

    /// Appends a bottom sheet token CSS rule.
    private static void appendBottomSheetRule(StringBuilder builder, String selector, SheetTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.bottomContainerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.bottomContainerHeight()));
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends a sheet header token CSS rule.
    private static void appendSheetHeaderRule(StringBuilder builder, String selector, SheetTokens tokens) {
        String padding = M3TokenCss.pixels(tokens.headerPadding());
        String bottomPadding = M3TokenCss.pixels(tokens.headerPadding() / 3.0);
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-padding", padding + " " + padding + " " + bottomPadding + " " + padding);
        endRule(builder);
    }

    /// Appends a sheet content token CSS rule.
    private static void appendSheetContentRule(StringBuilder builder, String selector, SheetTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a bottom sheet drag handle token CSS rule.
    private static void appendBottomSheetDragHandleRule(StringBuilder builder, String selector, SheetTokens tokens) {
        String handleWidth = M3TokenCss.pixels(tokens.dragHandleWidth());
        String handleHeight = M3TokenCss.pixels(tokens.dragHandleHeight());
        beginRule(builder, selector);
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
    private static void appendScrimRule(StringBuilder builder, String selector, ScrimTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-opacity", Double.toString(tokens.containerOpacity()));
        endRule(builder);
    }

    /// Appends a selection token CSS rule.
    private static void appendSelectionRule(StringBuilder builder, String selector, SelectionTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        endRule(builder);
    }

    /// Appends a switch token CSS rule.
    private static void appendSwitchRule(StringBuilder builder, String selector, SelectionTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        endRule(builder);
    }

    /// Appends a switch box shape CSS rule.
    private static void appendSwitchBoxRule(StringBuilder builder, String selector, SelectionTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.trackShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a slider token CSS rule.
    private static void appendSliderRule(StringBuilder builder, String selector, SliderTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-track-thickness", M3TokenCss.pixels(tokens.trackThickness()));
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-m3-thumb-size", M3TokenCss.pixels(tokens.thumbSize()));
        appendDeclaration(builder, "-m3-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        endRule(builder);
    }

    /// Appends a slider track visual CSS rule.
    private static void appendSliderTrackRule(StringBuilder builder, String selector, SliderTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.trackThickness()));
        endRule(builder);
    }

    /// Appends a slider thumb visual CSS rule.
    private static void appendSliderThumbRule(StringBuilder builder, String selector, SliderTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.trackShape()));
        endRule(builder);
    }

    /// Appends a chip token CSS rule.
    private static void appendChipRule(StringBuilder builder, String selector, ChipTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a progress bar token CSS rule.
    private static void appendProgressBarRule(StringBuilder builder, String selector, ProgressTokens tokens) {
        beginRule(builder, selector);
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
    private static void appendProgressIndicatorRule(StringBuilder builder, String selector, ProgressTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-track-thickness", M3TokenCss.pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-indicator-size", M3TokenCss.pixels(tokens.indicatorSize()));
        appendDeclaration(builder, "-m3-wave-amplitude", M3TokenCss.pixels(tokens.circularWaveAmplitude()));
        appendDeclaration(builder, "-m3-wavelength", M3TokenCss.pixels(tokens.circularWavelength()));
        appendDeclaration(builder, "-m3-track-gap", M3TokenCss.pixels(tokens.circularTrackGap()));
        endRule(builder);
    }

    /// Appends a dialog pane token CSS rule.
    private static void appendDialogRule(StringBuilder builder, String selector, DialogTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a card token CSS rule.
    private static void appendCardRule(StringBuilder builder, String selector, CardTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
        endRule(builder);
    }

    /// Appends a snackbar token CSS rule.
    private static void appendSnackbarRule(StringBuilder builder, String selector, SnackbarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a divider token CSS rule.
    private static void appendDividerRule(StringBuilder builder, String selector, DividerTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-thickness", M3TokenCss.pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-inset-start", M3TokenCss.pixels(tokens.insetStart()));
        appendDeclaration(builder, "-m3-inset-end", M3TokenCss.pixels(tokens.insetEnd()));
        endRule(builder);
    }

    /// Appends a badge token CSS rule.
    private static void appendBadgeRule(StringBuilder builder, String selector, BadgeTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-small-size", M3TokenCss.pixels(tokens.smallSize()));
        appendDeclaration(builder, "-m3-large-height", M3TokenCss.pixels(tokens.largeHeight()));
        appendDeclaration(builder, "-m3-large-min-width", M3TokenCss.pixels(tokens.largeMinWidth()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends an avatar token CSS rule.
    private static void appendAvatarRule(StringBuilder builder, String selector, AvatarTokens tokens) {
        String size = M3TokenCss.pixels(tokens.containerSize());
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-size", size);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a top app bar token CSS rule.
    private static void appendTopAppBarRule(StringBuilder builder, String selector, TopAppBarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a top app bar actions token CSS rule.
    private static void appendTopAppBarActionsRule(StringBuilder builder, String selector, TopAppBarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
        endRule(builder);
    }

    /// Appends a bottom app bar token CSS rule.
    private static void appendBottomAppBarRule(StringBuilder builder, String selector, BottomAppBarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a bottom app bar actions token CSS rule.
    private static void appendBottomAppBarActionsRule(StringBuilder builder, String selector, BottomAppBarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.actionSpacing()));
        endRule(builder);
    }

    /// Appends a navigation bar token CSS rule.
    private static void appendNavigationBarRule(StringBuilder builder, String selector, NavigationBarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-min-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-fx-padding", "0 " + M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a navigation item token CSS rule.
    private static void appendNavigationItemRule(StringBuilder builder, String selector, NavigationBarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.containerHeight()));
        appendDeclaration(builder, "-m3-item-width", M3TokenCss.pixels(tokens.itemWidth()));
        appendDeclaration(builder, "-m3-indicator-width", M3TokenCss.pixels(tokens.indicatorWidth()));
        appendDeclaration(builder, "-m3-indicator-height", M3TokenCss.pixels(tokens.indicatorHeight()));
        appendDeclaration(builder, "-m3-indicator-shape", M3TokenCss.pixels(tokens.indicatorShape()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a navigation selected indicator token CSS rule.
    private static void appendNavigationIndicatorRule(StringBuilder builder, String selector, NavigationBarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.indicatorShape()));
        endRule(builder);
    }

    /// Appends a navigation rail token CSS rule.
    private static void appendNavigationRailRule(StringBuilder builder, String selector, NavigationRailTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.verticalPadding())
                + " "
                + M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a navigation rail item token CSS rule.
    private static void appendNavigationRailItemRule(StringBuilder builder, String selector, NavigationRailTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.itemHeight()));
        appendDeclaration(builder, "-m3-item-width", M3TokenCss.pixels(tokens.itemWidth()));
        appendDeclaration(builder, "-m3-indicator-width", M3TokenCss.pixels(tokens.indicatorWidth()));
        appendDeclaration(builder, "-m3-indicator-height", M3TokenCss.pixels(tokens.indicatorHeight()));
        appendDeclaration(builder, "-m3-indicator-shape", M3TokenCss.pixels(tokens.indicatorShape()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a navigation rail selected indicator token CSS rule.
    private static void appendNavigationRailIndicatorRule(
            StringBuilder builder,
            String selector,
            NavigationRailTokens tokens
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.indicatorShape()));
        endRule(builder);
    }

    /// Appends a navigation drawer token CSS rule.
    private static void appendNavigationDrawerRule(
            StringBuilder builder,
            String selector,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-min-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-pref-width", M3TokenCss.pixels(tokens.containerWidth()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.containerPadding()));
        appendDeclaration(builder, "-fx-spacing", M3TokenCss.pixels(tokens.itemSpacing()));
        endRule(builder);
    }

    /// Appends a navigation drawer item token CSS rule.
    private static void appendNavigationDrawerItemRule(
            StringBuilder builder,
            String selector,
            NavigationDrawerTokens tokens
    ) {
        beginRule(builder, selector);
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
    private static void appendListItemRule(StringBuilder builder, String selector, ListItemTokens tokens) {
        beginRule(builder, selector);
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
    private static void appendListSectionHeaderRule(StringBuilder builder, String selector, ListItemTokens tokens) {
        String height = M3TokenCss.pixels(tokens.sectionHeaderHeight());
        String horizontalPadding = M3TokenCss.pixels(tokens.sectionHeaderHorizontalPadding());
        beginRule(builder, selector);
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
            double largeHorizontalPadding
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

    /// Component tokens for menus.
    ///
    /// @param containerShape the menu container corner radius
    /// @param containerPadding the padding around menu items
    /// @param itemHeight the one-line menu item height
    /// @param itemContainerShape the menu item state container corner radius
    /// @param itemHorizontalPadding the horizontal item content padding
    /// @param itemContentSpacing the spacing between item content regions
    @NotNullByDefault
    record MenuTokens(
            double containerShape,
            double containerPadding,
            double itemHeight,
            double itemContainerShape,
            double itemHorizontalPadding,
            double itemContentSpacing
    ) {
        /// Validates menu tokens.
        public MenuTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(containerPadding, "containerPadding");
            validateNonNegative(itemHeight, "itemHeight");
            validateNonNegative(itemContainerShape, "itemContainerShape");
            validateNonNegative(itemHorizontalPadding, "itemHorizontalPadding");
            validateNonNegative(itemContentSpacing, "itemContentSpacing");
        }
    }

    /// Component tokens for search components.
    ///
    /// @param barHeight the search bar container height
    /// @param barContainerShape the search bar container corner radius
    /// @param barHorizontalPadding the search bar horizontal content padding
    /// @param barContentSpacing the spacing between search bar content regions
    /// @param viewContainerShape the expanded search view corner radius
    /// @param viewResultPadding the padding below search results
    /// @param resultHeight the one-line search result item height
    @NotNullByDefault
    record SearchTokens(
            double barHeight,
            double barContainerShape,
            double barHorizontalPadding,
            double barContentSpacing,
            double viewContainerShape,
            double viewResultPadding,
            double resultHeight
    ) {
        /// Validates search tokens.
        public SearchTokens {
            validateNonNegative(barHeight, "barHeight");
            validateNonNegative(barContainerShape, "barContainerShape");
            validateNonNegative(barHorizontalPadding, "barHorizontalPadding");
            validateNonNegative(barContentSpacing, "barContentSpacing");
            validateNonNegative(viewContainerShape, "viewContainerShape");
            validateNonNegative(viewResultPadding, "viewResultPadding");
            validateNonNegative(resultHeight, "resultHeight");
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
    /// @param touchTargetSize the preferred touch target size
    /// @param trackShape the switch track radius
    @NotNullByDefault
    record SelectionTokens(
            double touchTargetSize,
            double trackShape
    ) {
        /// Creates selection tokens.
        public SelectionTokens {
            validateNonNegative(touchTargetSize, "touchTargetSize");
            validateNonNegative(trackShape, "trackShape");
        }
    }

    /// Tokens shared by sliders.
    ///
    /// @param trackThickness the slider track thickness
    /// @param trackShape the slider track radius
    /// @param thumbSize the slider thumb size
    /// @param touchTargetSize the preferred slider touch target size
    @NotNullByDefault
    record SliderTokens(
            double trackThickness,
            double trackShape,
            double thumbSize,
            double touchTargetSize
    ) {
        /// Creates slider tokens.
        public SliderTokens {
            validateNonNegative(trackThickness, "trackThickness");
            validateNonNegative(trackShape, "trackShape");
            validateNonNegative(thumbSize, "thumbSize");
            validateNonNegative(touchTargetSize, "touchTargetSize");
        }
    }

    /// Tokens shared by chip variants.
    ///
    /// @param height the preferred chip height
    /// @param containerShape the chip container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    record ChipTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates chip tokens.
        public ChipTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
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
        /// Creates baseline progress tokens without wavy rendering.
        ///
        /// @param thickness the default track thickness
        /// @param shape the progress indicator radius
        /// @param indicatorSize the circular indicator size
        public ProgressTokens(double thickness, double shape, double indicatorSize) {
            this(thickness, shape, indicatorSize, 0.0, 40.0, 4.0, 4.0, 0.0, 15.0, 4.0);
        }

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
    @NotNullByDefault
    record DialogTokens(
            double containerShape,
            double contentPadding
    ) {
        /// Creates dialog tokens.
        public DialogTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
        }
    }

    /// Tokens used by snackbar controls.
    ///
    /// @param containerShape the snackbar container radius
    /// @param contentPadding the snackbar content padding
    @NotNullByDefault
    record SnackbarTokens(
            double containerShape,
            double contentPadding
    ) {
        /// Creates snackbar tokens.
        public SnackbarTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
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
    /// @param containerHeight the top app bar container height
    /// @param horizontalPadding the horizontal content padding
    /// @param contentSpacing the spacing between leading, title, and trailing regions
    /// @param actionSpacing the spacing between trailing action nodes
    @NotNullByDefault
    record TopAppBarTokens(
            double containerHeight,
            double horizontalPadding,
            double contentSpacing,
            double actionSpacing
    ) {
        /// Creates top app bar tokens.
        public TopAppBarTokens {
            validateNonNegative(containerHeight, "containerHeight");
            validateNonNegative(horizontalPadding, "horizontalPadding");
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

    /// Validates that a token is a JavaFX opacity value.
    private static void validateOpacity(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("containerOpacity must be between 0 and 1");
        }
    }
}
