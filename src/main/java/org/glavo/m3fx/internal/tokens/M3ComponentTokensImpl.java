// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3ComponentTokens].
///
/// @param filledButton tokens used by filled buttons
/// @param tonalButton tokens used by filled tonal buttons
/// @param outlinedButton tokens used by outlined buttons
/// @param textButton tokens used by text buttons
/// @param elevatedButton tokens used by elevated buttons
/// @param buttonSizing the five-step button size token scale
/// @param iconButton tokens used by icon buttons
/// @param floatingActionButton tokens used by floating action buttons
/// @param icon tokens used by icon glyph primitives
/// @param buttonGroup tokens used by button groups
/// @param splitButton tokens used by split buttons
/// @param segmentedButton tokens used by segmented buttons
/// @param tab tokens used by tabs
/// @param field tokens used by text input controls
/// @param textArea tokens used by text area controls
/// @param form tokens used by form containers
/// @param validationSummary tokens used by validation summaries
/// @param menu tokens used by menus
/// @param search tokens used by search components
/// @param pickerField tokens used by picker fields
/// @param datePicker tokens used by date pickers
/// @param timePicker tokens used by time pickers
/// @param sheet tokens used by sheet containers
/// @param scrim tokens used by scrims
/// @param selection tokens used by selection controls
/// @param slider tokens used by sliders
/// @param chip tokens used by chips
/// @param progress tokens used by progress controls
/// @param loadingIndicator tokens used by loading indicators
/// @param surface tokens used by surfaces
/// @param carousel tokens used by carousels
/// @param card tokens used by cards
/// @param dialog tokens used by dialogs
/// @param snackbar tokens used by snackbar controls
/// @param banner tokens used by banners
/// @param tooltip tokens used by tooltips
/// @param divider tokens used by dividers
/// @param badge tokens used by badges
/// @param avatar tokens used by avatars
/// @param topAppBar tokens used by top app bars
/// @param bottomAppBar tokens used by bottom app bars
/// @param toolbar tokens used by toolbars
/// @param navigationBar tokens used by navigation bars
/// @param navigationRail tokens used by navigation rails
/// @param navigationDrawer tokens used by navigation drawers
/// @param listItem tokens used by list items
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
    /// Creates component token implementation.
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
}
