// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.docs;

import org.jetbrains.annotations.NotNullByDefault;

/// Provides stable URLs for Material Design 3 documentation pages used by M3FX.
///
/// The constants in this class intentionally point to the public Material Design 3 documentation at
/// [m3.material.io](https://m3.material.io/). Applications can use these URLs for help buttons,
/// about pages, sample browsers, and generated documentation without duplicating URL slugs.
@NotNullByDefault
public final class M3MaterialDocs {
    /// The root Material Design 3 documentation URL.
    public static final String ROOT = "https://m3.material.io/";

    /// The Material Design 3 components overview URL.
    public static final String COMPONENTS = ROOT + "components";

    /// The Material Design 3 styles overview URL.
    public static final String STYLES = ROOT + "styles";

    /// The top app bar component documentation URL.
    public static final String TOP_APP_BAR = componentUrl("top-app-bar");

    /// The badges component documentation URL.
    public static final String BADGES = componentUrl("badges");

    /// The button groups component documentation URL.
    public static final String BUTTON_GROUPS = componentUrl("button-groups");

    /// The buttons component documentation URL.
    public static final String BUTTONS = componentUrl("buttons");

    /// The extended floating action button component documentation URL.
    public static final String EXTENDED_FAB = componentUrl("extended-fab");

    /// The floating action button menu component documentation URL.
    public static final String FAB_MENU = componentUrl("fab-menu");

    /// The floating action button component documentation URL.
    public static final String FLOATING_ACTION_BUTTON = componentUrl("floating-action-button");

    /// The icon buttons component documentation URL.
    public static final String ICON_BUTTONS = componentUrl("icon-buttons");

    /// The segmented buttons component documentation URL.
    public static final String SEGMENTED_BUTTONS = componentUrl("segmented-buttons");

    /// The split button component documentation URL.
    public static final String SPLIT_BUTTON = componentUrl("split-button");

    /// The cards component documentation URL.
    public static final String CARDS = componentUrl("cards");

    /// The carousel component documentation URL.
    public static final String CAROUSEL = componentUrl("carousel");

    /// The checkbox component documentation URL.
    public static final String CHECKBOX = componentUrl("checkbox");

    /// The chips component documentation URL.
    public static final String CHIPS = componentUrl("chips");

    /// The date pickers component documentation URL.
    public static final String DATE_PICKERS = componentUrl("date-pickers");

    /// The time pickers component documentation URL.
    public static final String TIME_PICKERS = componentUrl("time-pickers");

    /// The dialogs component documentation URL.
    public static final String DIALOGS = componentUrl("dialogs");

    /// The divider component documentation URL.
    public static final String DIVIDER = componentUrl("divider");

    /// The lists component documentation URL.
    public static final String LISTS = componentUrl("lists");

    /// The loading indicator component documentation URL.
    public static final String LOADING_INDICATOR = componentUrl("loading-indicator");

    /// The progress indicators component documentation URL.
    public static final String PROGRESS_INDICATORS = componentUrl("progress-indicators");

    /// The menus component documentation URL.
    public static final String MENUS = componentUrl("menus");

    /// The navigation bar component documentation URL.
    public static final String NAVIGATION_BAR = componentUrl("navigation-bar");

    /// The navigation drawer component documentation URL.
    public static final String NAVIGATION_DRAWER = componentUrl("navigation-drawer");

    /// The navigation rail component documentation URL.
    public static final String NAVIGATION_RAIL = componentUrl("navigation-rail");

    /// The radio button component documentation URL.
    public static final String RADIO_BUTTON = componentUrl("radio-button");

    /// The search component documentation URL.
    public static final String SEARCH = componentUrl("search");

    /// The bottom sheets component documentation URL.
    public static final String BOTTOM_SHEETS = componentUrl("bottom-sheets");

    /// The side sheets component documentation URL.
    public static final String SIDE_SHEETS = componentUrl("side-sheets");

    /// The sliders component documentation URL.
    public static final String SLIDERS = componentUrl("sliders");

    /// The snackbar component documentation URL.
    public static final String SNACKBAR = componentUrl("snackbar");

    /// The switch component documentation URL.
    public static final String SWITCH = componentUrl("switch");

    /// The tabs component documentation URL.
    public static final String TABS = componentUrl("tabs");

    /// The text fields component documentation URL.
    public static final String TEXT_FIELDS = componentUrl("text-fields");

    /// The toolbars component documentation URL.
    public static final String TOOLBARS = componentUrl("toolbars");

    /// The tooltips component documentation URL.
    public static final String TOOLTIPS = componentUrl("tooltips");

    /// The typography style documentation URL.
    public static final String TYPOGRAPHY = styleUrl("typography");

    /// The icons style documentation URL.
    public static final String ICONS = styleUrl("icons");

    /// Prevents instantiation of this utility class.
    private M3MaterialDocs() {
    }

    /// Returns the Material Design 3 component documentation URL for a component slug.
    ///
    /// @param slug the component slug used by `m3.material.io`
    /// @return the component overview documentation URL
    public static String componentUrl(String slug) {
        return ROOT + "components/" + slug + "/overview";
    }

    /// Returns the Material Design 3 style documentation URL for a style slug.
    ///
    /// @param slug the style slug used by `m3.material.io`
    /// @return the style overview documentation URL
    public static String styleUrl(String slug) {
        return ROOT + "styles/" + slug + "/overview";
    }
}
