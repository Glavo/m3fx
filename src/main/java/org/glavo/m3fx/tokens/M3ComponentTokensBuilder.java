// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ComponentTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds immutable [M3ComponentTokens] by replacing named component token groups.
///
/// The builder is initialized from a complete component token set, so every result remains complete even when only
/// one component group is customized. Each setter replaces one semantic group and returns this builder.
/// Replacement groups must not be `null`; replacement methods throw [NullPointerException] when that contract is
/// violated. A builder can be reused after [build].
///
/// See [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3ComponentTokensBuilder {
    /// The current filledButton token group.
    private M3ComponentTokens.ButtonTokens filledButton;

    /// The current tonalButton token group.
    private M3ComponentTokens.ButtonTokens tonalButton;

    /// The current outlinedButton token group.
    private M3ComponentTokens.ButtonTokens outlinedButton;

    /// The current textButton token group.
    private M3ComponentTokens.ButtonTokens textButton;

    /// The current elevatedButton token group.
    private M3ComponentTokens.ButtonTokens elevatedButton;

    /// The current buttonSizing token group.
    private M3ComponentTokens.ButtonSizingTokens buttonSizing;

    /// The current iconButton token group.
    private M3ComponentTokens.IconButtonTokens iconButton;

    /// The current floatingActionButton token group.
    private M3ComponentTokens.FabTokens floatingActionButton;

    /// The current icon token group.
    private M3ComponentTokens.IconTokens icon;

    /// The current buttonGroup token group.
    private M3ComponentTokens.ButtonGroupTokens buttonGroup;

    /// The current splitButton token group.
    private M3ComponentTokens.SplitButtonTokens splitButton;

    /// The current segmentedButton token group.
    private M3ComponentTokens.ButtonTokens segmentedButton;

    /// The current tab token group.
    private M3ComponentTokens.TabTokens tab;

    /// The current field token group.
    private M3ComponentTokens.FieldTokens field;

    /// The current textArea token group.
    private M3ComponentTokens.TextAreaTokens textArea;

    /// The current form token group.
    private M3ComponentTokens.FormTokens form;

    /// The current validationSummary token group.
    private M3ComponentTokens.ValidationSummaryTokens validationSummary;

    /// The current menu token group.
    private M3ComponentTokens.MenuTokens menu;

    /// The current search token group.
    private M3ComponentTokens.SearchTokens search;

    /// The current pickerField token group.
    private M3ComponentTokens.PickerFieldTokens pickerField;

    /// The current datePicker token group.
    private M3ComponentTokens.DatePickerTokens datePicker;

    /// The current timePicker token group.
    private M3ComponentTokens.TimePickerTokens timePicker;

    /// The current sheet token group.
    private M3ComponentTokens.SheetTokens sheet;

    /// The current scrim token group.
    private M3ComponentTokens.ScrimTokens scrim;

    /// The current selection token group.
    private M3ComponentTokens.SelectionTokens selection;

    /// The current slider token group.
    private M3ComponentTokens.SliderTokens slider;

    /// The current chip token group.
    private M3ComponentTokens.ChipTokens chip;

    /// The current progress token group.
    private M3ComponentTokens.ProgressTokens progress;

    /// The current loadingIndicator token group.
    private M3ComponentTokens.LoadingIndicatorTokens loadingIndicator;

    /// The current surface token group.
    private M3ComponentTokens.SurfaceTokens surface;

    /// The current carousel token group.
    private M3ComponentTokens.CarouselTokens carousel;

    /// The current card token group.
    private M3ComponentTokens.CardTokens card;

    /// The current dialog token group.
    private M3ComponentTokens.DialogTokens dialog;

    /// The current snackbar token group.
    private M3ComponentTokens.SnackbarTokens snackbar;

    /// The current banner token group.
    private M3ComponentTokens.BannerTokens banner;

    /// The current tooltip token group.
    private M3ComponentTokens.TooltipTokens tooltip;

    /// The current divider token group.
    private M3ComponentTokens.DividerTokens divider;

    /// The current badge token group.
    private M3ComponentTokens.BadgeTokens badge;

    /// The current avatar token group.
    private M3ComponentTokens.AvatarTokens avatar;

    /// The current topAppBar token group.
    private M3ComponentTokens.TopAppBarTokens topAppBar;

    /// The current bottomAppBar token group.
    private M3ComponentTokens.BottomAppBarTokens bottomAppBar;

    /// The current toolbar token group.
    private M3ComponentTokens.ToolbarTokens toolbar;

    /// The current navigationBar token group.
    private M3ComponentTokens.NavigationBarTokens navigationBar;

    /// The current navigationRail token group.
    private M3ComponentTokens.NavigationRailTokens navigationRail;

    /// The current navigationDrawer token group.
    private M3ComponentTokens.NavigationDrawerTokens navigationDrawer;

    /// The current listItem token group.
    private M3ComponentTokens.ListItemTokens listItem;

    /// Creates a builder initialized from an existing component token set.
    ///
    /// @param tokens the component token set to copy
    M3ComponentTokensBuilder(M3ComponentTokens tokens) {
        M3ComponentTokens source = Objects.requireNonNull(tokens, "tokens");
        filledButton = source.filledButton();
        tonalButton = source.tonalButton();
        outlinedButton = source.outlinedButton();
        textButton = source.textButton();
        elevatedButton = source.elevatedButton();
        buttonSizing = source.buttonSizing();
        iconButton = source.iconButton();
        floatingActionButton = source.floatingActionButton();
        icon = source.icon();
        buttonGroup = source.buttonGroup();
        splitButton = source.splitButton();
        segmentedButton = source.segmentedButton();
        tab = source.tab();
        field = source.field();
        textArea = source.textArea();
        form = source.form();
        validationSummary = source.validationSummary();
        menu = source.menu();
        search = source.search();
        pickerField = source.pickerField();
        datePicker = source.datePicker();
        timePicker = source.timePicker();
        sheet = source.sheet();
        scrim = source.scrim();
        selection = source.selection();
        slider = source.slider();
        chip = source.chip();
        progress = source.progress();
        loadingIndicator = source.loadingIndicator();
        surface = source.surface();
        carousel = source.carousel();
        card = source.card();
        dialog = source.dialog();
        snackbar = source.snackbar();
        banner = source.banner();
        tooltip = source.tooltip();
        divider = source.divider();
        badge = source.badge();
        avatar = source.avatar();
        topAppBar = source.topAppBar();
        bottomAppBar = source.bottomAppBar();
        toolbar = source.toolbar();
        navigationBar = source.navigationBar();
        navigationRail = source.navigationRail();
        navigationDrawer = source.navigationDrawer();
        listItem = source.listItem();
    }

    /// Replaces the filledButton token group.
    ///
    /// @param filledButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder filledButton(M3ComponentTokens.ButtonTokens filledButton) {
        this.filledButton = Objects.requireNonNull(filledButton, "filledButton");
        return this;
    }

    /// Replaces the tonalButton token group.
    ///
    /// @param tonalButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder tonalButton(M3ComponentTokens.ButtonTokens tonalButton) {
        this.tonalButton = Objects.requireNonNull(tonalButton, "tonalButton");
        return this;
    }

    /// Replaces the outlinedButton token group.
    ///
    /// @param outlinedButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder outlinedButton(M3ComponentTokens.ButtonTokens outlinedButton) {
        this.outlinedButton = Objects.requireNonNull(outlinedButton, "outlinedButton");
        return this;
    }

    /// Replaces the textButton token group.
    ///
    /// @param textButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder textButton(M3ComponentTokens.ButtonTokens textButton) {
        this.textButton = Objects.requireNonNull(textButton, "textButton");
        return this;
    }

    /// Replaces the elevatedButton token group.
    ///
    /// @param elevatedButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder elevatedButton(M3ComponentTokens.ButtonTokens elevatedButton) {
        this.elevatedButton = Objects.requireNonNull(elevatedButton, "elevatedButton");
        return this;
    }

    /// Replaces the buttonSizing token group.
    ///
    /// @param buttonSizing the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder buttonSizing(M3ComponentTokens.ButtonSizingTokens buttonSizing) {
        this.buttonSizing = Objects.requireNonNull(buttonSizing, "buttonSizing");
        return this;
    }

    /// Replaces the iconButton token group.
    ///
    /// @param iconButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder iconButton(M3ComponentTokens.IconButtonTokens iconButton) {
        this.iconButton = Objects.requireNonNull(iconButton, "iconButton");
        return this;
    }

    /// Replaces the floatingActionButton token group.
    ///
    /// @param floatingActionButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder floatingActionButton(M3ComponentTokens.FabTokens floatingActionButton) {
        this.floatingActionButton = Objects.requireNonNull(floatingActionButton, "floatingActionButton");
        return this;
    }

    /// Replaces the icon token group.
    ///
    /// @param icon the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder icon(M3ComponentTokens.IconTokens icon) {
        this.icon = Objects.requireNonNull(icon, "icon");
        return this;
    }

    /// Replaces the buttonGroup token group.
    ///
    /// @param buttonGroup the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder buttonGroup(M3ComponentTokens.ButtonGroupTokens buttonGroup) {
        this.buttonGroup = Objects.requireNonNull(buttonGroup, "buttonGroup");
        return this;
    }

    /// Replaces the splitButton token group.
    ///
    /// @param splitButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder splitButton(M3ComponentTokens.SplitButtonTokens splitButton) {
        this.splitButton = Objects.requireNonNull(splitButton, "splitButton");
        return this;
    }

    /// Replaces the segmentedButton token group.
    ///
    /// @param segmentedButton the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder segmentedButton(M3ComponentTokens.ButtonTokens segmentedButton) {
        this.segmentedButton = Objects.requireNonNull(segmentedButton, "segmentedButton");
        return this;
    }

    /// Replaces the tab token group.
    ///
    /// @param tab the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder tab(M3ComponentTokens.TabTokens tab) {
        this.tab = Objects.requireNonNull(tab, "tab");
        return this;
    }

    /// Replaces the field token group.
    ///
    /// @param field the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder field(M3ComponentTokens.FieldTokens field) {
        this.field = Objects.requireNonNull(field, "field");
        return this;
    }

    /// Replaces the textArea token group.
    ///
    /// @param textArea the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder textArea(M3ComponentTokens.TextAreaTokens textArea) {
        this.textArea = Objects.requireNonNull(textArea, "textArea");
        return this;
    }

    /// Replaces the form token group.
    ///
    /// @param form the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder form(M3ComponentTokens.FormTokens form) {
        this.form = Objects.requireNonNull(form, "form");
        return this;
    }

    /// Replaces the validationSummary token group.
    ///
    /// @param validationSummary the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder validationSummary(M3ComponentTokens.ValidationSummaryTokens validationSummary) {
        this.validationSummary = Objects.requireNonNull(validationSummary, "validationSummary");
        return this;
    }

    /// Replaces the menu token group.
    ///
    /// @param menu the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder menu(M3ComponentTokens.MenuTokens menu) {
        this.menu = Objects.requireNonNull(menu, "menu");
        return this;
    }

    /// Replaces the search token group.
    ///
    /// @param search the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder search(M3ComponentTokens.SearchTokens search) {
        this.search = Objects.requireNonNull(search, "search");
        return this;
    }

    /// Replaces the pickerField token group.
    ///
    /// @param pickerField the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder pickerField(M3ComponentTokens.PickerFieldTokens pickerField) {
        this.pickerField = Objects.requireNonNull(pickerField, "pickerField");
        return this;
    }

    /// Replaces the datePicker token group.
    ///
    /// @param datePicker the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder datePicker(M3ComponentTokens.DatePickerTokens datePicker) {
        this.datePicker = Objects.requireNonNull(datePicker, "datePicker");
        return this;
    }

    /// Replaces the timePicker token group.
    ///
    /// @param timePicker the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder timePicker(M3ComponentTokens.TimePickerTokens timePicker) {
        this.timePicker = Objects.requireNonNull(timePicker, "timePicker");
        return this;
    }

    /// Replaces the sheet token group.
    ///
    /// @param sheet the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder sheet(M3ComponentTokens.SheetTokens sheet) {
        this.sheet = Objects.requireNonNull(sheet, "sheet");
        return this;
    }

    /// Replaces the scrim token group.
    ///
    /// @param scrim the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder scrim(M3ComponentTokens.ScrimTokens scrim) {
        this.scrim = Objects.requireNonNull(scrim, "scrim");
        return this;
    }

    /// Replaces the selection token group.
    ///
    /// @param selection the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder selection(M3ComponentTokens.SelectionTokens selection) {
        this.selection = Objects.requireNonNull(selection, "selection");
        return this;
    }

    /// Replaces the slider token group.
    ///
    /// @param slider the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder slider(M3ComponentTokens.SliderTokens slider) {
        this.slider = Objects.requireNonNull(slider, "slider");
        return this;
    }

    /// Replaces the chip token group.
    ///
    /// @param chip the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder chip(M3ComponentTokens.ChipTokens chip) {
        this.chip = Objects.requireNonNull(chip, "chip");
        return this;
    }

    /// Replaces the progress token group.
    ///
    /// @param progress the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder progress(M3ComponentTokens.ProgressTokens progress) {
        this.progress = Objects.requireNonNull(progress, "progress");
        return this;
    }

    /// Replaces the loadingIndicator token group.
    ///
    /// @param loadingIndicator the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder loadingIndicator(M3ComponentTokens.LoadingIndicatorTokens loadingIndicator) {
        this.loadingIndicator = Objects.requireNonNull(loadingIndicator, "loadingIndicator");
        return this;
    }

    /// Replaces the surface token group.
    ///
    /// @param surface the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder surface(M3ComponentTokens.SurfaceTokens surface) {
        this.surface = Objects.requireNonNull(surface, "surface");
        return this;
    }

    /// Replaces the carousel token group.
    ///
    /// @param carousel the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder carousel(M3ComponentTokens.CarouselTokens carousel) {
        this.carousel = Objects.requireNonNull(carousel, "carousel");
        return this;
    }

    /// Replaces the card token group.
    ///
    /// @param card the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder card(M3ComponentTokens.CardTokens card) {
        this.card = Objects.requireNonNull(card, "card");
        return this;
    }

    /// Replaces the dialog token group.
    ///
    /// @param dialog the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder dialog(M3ComponentTokens.DialogTokens dialog) {
        this.dialog = Objects.requireNonNull(dialog, "dialog");
        return this;
    }

    /// Replaces the snackbar token group.
    ///
    /// @param snackbar the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder snackbar(M3ComponentTokens.SnackbarTokens snackbar) {
        this.snackbar = Objects.requireNonNull(snackbar, "snackbar");
        return this;
    }

    /// Replaces the banner token group.
    ///
    /// @param banner the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder banner(M3ComponentTokens.BannerTokens banner) {
        this.banner = Objects.requireNonNull(banner, "banner");
        return this;
    }

    /// Replaces the tooltip token group.
    ///
    /// @param tooltip the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder tooltip(M3ComponentTokens.TooltipTokens tooltip) {
        this.tooltip = Objects.requireNonNull(tooltip, "tooltip");
        return this;
    }

    /// Replaces the divider token group.
    ///
    /// @param divider the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder divider(M3ComponentTokens.DividerTokens divider) {
        this.divider = Objects.requireNonNull(divider, "divider");
        return this;
    }

    /// Replaces the badge token group.
    ///
    /// @param badge the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder badge(M3ComponentTokens.BadgeTokens badge) {
        this.badge = Objects.requireNonNull(badge, "badge");
        return this;
    }

    /// Replaces the avatar token group.
    ///
    /// @param avatar the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder avatar(M3ComponentTokens.AvatarTokens avatar) {
        this.avatar = Objects.requireNonNull(avatar, "avatar");
        return this;
    }

    /// Replaces the topAppBar token group.
    ///
    /// @param topAppBar the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder topAppBar(M3ComponentTokens.TopAppBarTokens topAppBar) {
        this.topAppBar = Objects.requireNonNull(topAppBar, "topAppBar");
        return this;
    }

    /// Replaces the bottomAppBar token group.
    ///
    /// @param bottomAppBar the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder bottomAppBar(M3ComponentTokens.BottomAppBarTokens bottomAppBar) {
        this.bottomAppBar = Objects.requireNonNull(bottomAppBar, "bottomAppBar");
        return this;
    }

    /// Replaces the toolbar token group.
    ///
    /// @param toolbar the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder toolbar(M3ComponentTokens.ToolbarTokens toolbar) {
        this.toolbar = Objects.requireNonNull(toolbar, "toolbar");
        return this;
    }

    /// Replaces the navigationBar token group.
    ///
    /// @param navigationBar the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder navigationBar(M3ComponentTokens.NavigationBarTokens navigationBar) {
        this.navigationBar = Objects.requireNonNull(navigationBar, "navigationBar");
        return this;
    }

    /// Replaces the navigationRail token group.
    ///
    /// @param navigationRail the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder navigationRail(M3ComponentTokens.NavigationRailTokens navigationRail) {
        this.navigationRail = Objects.requireNonNull(navigationRail, "navigationRail");
        return this;
    }

    /// Replaces the navigationDrawer token group.
    ///
    /// @param navigationDrawer the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder navigationDrawer(M3ComponentTokens.NavigationDrawerTokens navigationDrawer) {
        this.navigationDrawer = Objects.requireNonNull(navigationDrawer, "navigationDrawer");
        return this;
    }

    /// Replaces the listItem token group.
    ///
    /// @param listItem the replacement token group
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3ComponentTokensBuilder listItem(M3ComponentTokens.ListItemTokens listItem) {
        this.listItem = Objects.requireNonNull(listItem, "listItem");
        return this;
    }

    /// Creates an immutable component token set from the current builder state.
    ///
    /// @return the built component token set
    public M3ComponentTokens build() {
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
}
