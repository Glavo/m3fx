// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Provides Material Design documentation URLs used by the demo application.
@NotNullByDefault
final class DemoMaterialDocs {
    /// The root Material Design 3 documentation URL.
    static final String ROOT = "https://m3.material.io/";

    /// The Material Design 3 components overview URL.
    static final String COMPONENTS = ROOT + "components";

    /// The top app bar component documentation URL.
    static final String TOP_APP_BAR = componentUrl("top-app-bar");

    /// The badges component documentation URL.
    static final String BADGES = componentUrl("badges");

    /// The button groups component documentation URL.
    static final String BUTTON_GROUPS = componentUrl("button-groups");

    /// The buttons component documentation URL.
    static final String BUTTONS = componentUrl("buttons");

    /// The extended floating action button component documentation URL.
    static final String EXTENDED_FAB = componentUrl("extended-fab");

    /// The floating action button menu component documentation URL.
    static final String FAB_MENU = componentUrl("fab-menu");

    /// The floating action button component documentation URL.
    static final String FLOATING_ACTION_BUTTON = componentUrl("floating-action-button");

    /// The icon buttons component documentation URL.
    static final String ICON_BUTTONS = componentUrl("icon-buttons");

    /// The segmented buttons component documentation URL.
    static final String SEGMENTED_BUTTONS = componentUrl("segmented-buttons");

    /// The split button component documentation URL.
    static final String SPLIT_BUTTON = componentUrl("split-button");

    /// The cards component documentation URL.
    static final String CARDS = componentUrl("cards");

    /// The carousel component documentation URL.
    static final String CAROUSEL = componentUrl("carousel");

    /// The checkbox component documentation URL.
    static final String CHECKBOX = componentUrl("checkbox");

    /// The chips component documentation URL.
    static final String CHIPS = componentUrl("chips");

    /// The date pickers component documentation URL.
    static final String DATE_PICKERS = componentUrl("date-pickers");

    /// The time pickers component documentation URL.
    static final String TIME_PICKERS = componentUrl("time-pickers");

    /// The dialogs component documentation URL.
    static final String DIALOGS = componentUrl("dialogs");

    /// The divider component documentation URL.
    static final String DIVIDER = componentUrl("divider");

    /// The lists component documentation URL.
    static final String LISTS = componentUrl("lists");

    /// The loading indicator component documentation URL.
    static final String LOADING_INDICATOR = componentUrl("loading-indicator");

    /// The progress indicators component documentation URL.
    static final String PROGRESS_INDICATORS = componentUrl("progress-indicators");

    /// The menus component documentation URL.
    static final String MENUS = componentUrl("menus");

    /// The navigation bar component documentation URL.
    static final String NAVIGATION_BAR = componentUrl("navigation-bar");

    /// The navigation drawer component documentation URL.
    static final String NAVIGATION_DRAWER = componentUrl("navigation-drawer");

    /// The navigation rail component documentation URL.
    static final String NAVIGATION_RAIL = componentUrl("navigation-rail");

    /// The radio button component documentation URL.
    static final String RADIO_BUTTON = componentUrl("radio-button");

    /// The search component documentation URL.
    static final String SEARCH = componentUrl("search");

    /// The bottom sheets component documentation URL.
    static final String BOTTOM_SHEETS = componentUrl("bottom-sheets");

    /// The side sheets component documentation URL.
    static final String SIDE_SHEETS = componentUrl("side-sheets");

    /// The sliders component documentation URL.
    static final String SLIDERS = componentUrl("sliders");

    /// The snackbar component documentation URL.
    static final String SNACKBAR = componentUrl("snackbar");

    /// The switch component documentation URL.
    static final String SWITCH = componentUrl("switch");

    /// The tabs component documentation URL.
    static final String TABS = componentUrl("tabs");

    /// The text fields component documentation URL.
    static final String TEXT_FIELDS = componentUrl("text-fields");

    /// The toolbars component documentation URL.
    static final String TOOLBARS = componentUrl("toolbars");

    /// The tooltips component documentation URL.
    static final String TOOLTIPS = componentUrl("tooltips");

    /// The typography style documentation URL.
    static final String TYPOGRAPHY = styleUrl("typography");

    /// The icons style documentation URL.
    static final String ICONS = styleUrl("icons");

    /// Prevents instantiation of this utility class.
    private DemoMaterialDocs() {
    }

    /// Returns a Material Design 3 component documentation URL for the given component slug.
    private static String componentUrl(String slug) {
        return ROOT + "components/" + slug + "/overview";
    }

    /// Returns a Material Design 3 style documentation URL for the given style slug.
    private static String styleUrl(String slug) {
        return ROOT + "styles/" + slug + "/overview";
    }
}
