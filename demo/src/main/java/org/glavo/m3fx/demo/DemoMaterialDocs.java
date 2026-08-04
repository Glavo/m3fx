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

    /// The Material Design all-buttons documentation URL.
    static final String ALL_BUTTONS = ROOT + "components/all-buttons";

    /// The Material Design color role documentation URL.
    static final String COLOR_ROLES = ROOT + "styles/color/roles";

    /// The Material Design elevation documentation URL.
    static final String ELEVATION = styleUrl("elevation");

    /// The Material Design interaction states documentation URL.
    static final String INTERACTION_STATES = ROOT + "foundations/interaction/states/overview";

    /// The Material Design motion overview URL.
    static final String MOTION = styleUrl("motion");

    /// The app bars component documentation URL.
    static final String APP_BARS = componentUrl("app-bars");

    /// The Material toolbar documentation URL used for bottom app bar guidance.
    static final String BOTTOM_APP_BARS = componentUrl("toolbars");

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

    /// The Spectrum Web Components breadcrumbs documentation URL.
    static final String BREADCRUMBS =
            "https://opensource.adobe.com/spectrum-web-components/components/breadcrumbs/";

    /// The Spectrum composable color-control documentation used for color-picker behavior.
    static final String COLOR_PICKERS = "https://react-spectrum.adobe.com/ColorArea";

    /// The Spectrum Web Components drop-zone documentation URL.
    static final String DROP_ZONES =
            "https://opensource.adobe.com/spectrum-web-components/components/dropzone/";

    /// The Spectrum Web Components status-light documentation URL.
    static final String STATUS_LIGHTS =
            "https://opensource.adobe.com/spectrum-web-components/components/status-light/";

    /// The Adobe Spectrum tree-view documentation URL.
    static final String TREE_VIEWS = "https://react-spectrum.adobe.com/TreeView";

    /// The Adobe Spectrum table-view documentation URL.
    static final String TABLE_VIEWS = "https://react-spectrum.adobe.com/TableView";

    /// The Adobe Spectrum 2 number-field documentation URL.
    static final String NUMBER_FIELDS = "https://react-spectrum.adobe.com/NumberField";

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

    /// The closest Material documentation URL for settings rows.
    static final String SETTINGS = LISTS;

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

    /// The closest Material documentation URL for the banner demo, which has no current M3 component page.
    static final String BANNERS = COMPONENTS;

    /// The closest Material documentation URL for form layout demos.
    static final String FORMS = TEXT_FIELDS;

    /// The closest Material documentation URL for avatar color and content treatment.
    static final String AVATARS = COLOR_ROLES;

    /// The closest Material documentation URL for Material surfaces and elevation.
    static final String SURFACES = ELEVATION;

    /// The closest Material documentation URL for scrim and overlay state treatment.
    static final String SCRIMS = INTERACTION_STATES;

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
