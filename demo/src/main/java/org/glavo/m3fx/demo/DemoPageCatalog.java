// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/// Defines the ordered pages and navigation sections shown by the M3FX demo application.
@NotNullByDefault
final class DemoPageCatalog {
    /// The sidebar destination for the components overview page.
    private static final String COMPONENTS_OVERVIEW_GROUP = "Components overview";

    /// The official components sidebar group for app bar components.
    private static final String APP_BARS_GROUP = "App bars";

    /// The official components sidebar group for button-related components.
    private static final String BUTTONS_GROUP = "Buttons";

    /// The official components sidebar group for date and time picker components.
    private static final String DATE_TIME_PICKERS_GROUP = "Date & time pickers";

    /// The official components sidebar group for loading and progress components.
    private static final String LOADING_PROGRESS_GROUP = "Loading & progress";

    /// The official components sidebar group for navigation components.
    private static final String NAVIGATION_GROUP = "Navigation";

    /// The official components sidebar group for sheet components.
    private static final String SHEETS_GROUP = "Sheets";

    /// The official components sidebar group for toolbar components.
    private static final String TOOLBARS_GROUP = "Toolbars";

    /// The sidebar section for demos absent from the Material components navigation drawer.
    private static final String ADDITIONAL_DEMOS_GROUP = "Additional demos";

    /// Prevents instantiation.
    private DemoPageCatalog() {
    }

    /// Creates all component demo pages.
    ///
    /// @param context the application services used by interactive examples
    /// @return the immutable page catalog in navigation order
    static @Unmodifiable List<DemoPage> createPages(DemoPageContext context) {
        Objects.requireNonNull(context, "context");
        return List.of(
                new DemoPage("Components Overview", "Components overview", COMPONENTS_OVERVIEW_GROUP, "Browse the implemented Material Design 3 component demos", DemoMaterialDocs.COMPONENTS, (new ComponentsOverviewDemoPage(context))::createContent),
                new DemoPage("App Bars", "App bars", APP_BARS_GROUP, "Top app bars with navigation and actions", DemoMaterialDocs.APP_BARS, (new AppBarsDemoPage(context))::createContent),
                new DemoPage("Badges", "Badges", "Badges", "Dot, count, overflow, and attached badges", DemoMaterialDocs.BADGES, (new BadgesDemoPage(context))::createContent),
                new DemoPage("All Buttons", "All buttons", BUTTONS_GROUP, "Overview of button families and action patterns", DemoMaterialDocs.ALL_BUTTONS, (new AllButtonsDemoPage(context))::createContent),
                new DemoPage("Button Groups", "Button groups", BUTTONS_GROUP, "Standard and connected groups for related actions", DemoMaterialDocs.BUTTON_GROUPS, (new ButtonGroupsDemoPage(context))::createContent),
                new DemoPage("Buttons", "Buttons", BUTTONS_GROUP, "Common button variants", DemoMaterialDocs.BUTTONS, (new ButtonsDemoPage(context))::createContent),
                new DemoPage("Extended FABs", "Extended FABs", BUTTONS_GROUP, "Extended floating action button examples", DemoMaterialDocs.EXTENDED_FAB, (new ExtendedFabsDemoPage(context))::createContent),
                new DemoPage("FAB Menu", "FAB menu", BUTTONS_GROUP, "Expandable floating action shortcuts", DemoMaterialDocs.FAB_MENU, (new FabMenuDemoPage(context))::createContent),
                new DemoPage("Floating Action Buttons", "Floating action buttons (FABs)", BUTTONS_GROUP, "Floating action button sizes and variants", DemoMaterialDocs.FLOATING_ACTION_BUTTON, (new FloatingActionButtonsDemoPage(context))::createContent),
                new DemoPage("Icon Buttons", "Icon buttons", BUTTONS_GROUP, "Icon button and toggle icon button states", DemoMaterialDocs.ICON_BUTTONS, (new IconButtonsDemoPage(context))::createContent),
                new DemoPage("Segmented Buttons", "Segmented buttons", BUTTONS_GROUP, "Single- and multi-select segmented control states", DemoMaterialDocs.SEGMENTED_BUTTONS, (new SegmentedButtonsDemoPage(context))::createContent),
                new DemoPage("Split Buttons", "Split buttons", BUTTONS_GROUP, "Primary actions with attached menus", DemoMaterialDocs.SPLIT_BUTTON, (new SplitButtonsDemoPage(context))::createContent),
                new DemoPage("Cards", "Cards", "Cards", "Filled, outlined, elevated, and interactive cards", DemoMaterialDocs.CARDS, (new CardsDemoPage(context))::createContent),
                new DemoPage("Carousel", "Carousel", "Carousel", "Adaptive visual browsing with Material keyline layouts", DemoMaterialDocs.CAROUSEL, (new CarouselDemoPage(context))::createContent),
                new DemoPage("Checkboxes", "Checkbox", "Checkbox", "Checked, unchecked, indeterminate, error, and disabled states", DemoMaterialDocs.CHECKBOX, (new CheckboxesDemoPage(context))::createContent),
                new DemoPage("Chips", "Chips", "Chips", "Assist, filter, input, suggestion, and disabled chips", DemoMaterialDocs.CHIPS, (new ChipsDemoPage(context))::createContent),
                new DemoPage("Date Pickers", "Date pickers", DATE_TIME_PICKERS_GROUP, "Calendar date selection, ranges, and month visibility", DemoMaterialDocs.DATE_PICKERS, (new DatePickersDemoPage(context))::createContent),
                new DemoPage("Time Pickers", "Time pickers", DATE_TIME_PICKERS_GROUP, "12-hour, 24-hour, and bounded time selection", DemoMaterialDocs.TIME_PICKERS, (new TimePickersDemoPage(context))::createContent),
                new DemoPage("Dialogs", "Dialogs", "Dialogs", "Dialog pane with themed actions", DemoMaterialDocs.DIALOGS, (new DialogsDemoPage(context))::createContent),
                new DemoPage("Dividers", "Divider", "Divider", "Full-width, inset, middle inset, and vertical dividers", DemoMaterialDocs.DIVIDER, (new DividersDemoPage(context))::createContent),
                new DemoPage("Lists", "Lists", "Lists", "One-line, two-line, three-line, selected, and disabled rows", DemoMaterialDocs.LISTS, (new ListDemoPage(context))::createContent),
                new DemoPage("Loading Indicator", "Loading indicator", LOADING_PROGRESS_GROUP, "Indeterminate loading indicators", DemoMaterialDocs.LOADING_INDICATOR, (new LoadingIndicatorDemoPage(context))::createContent),
                new DemoPage("Progress", "Progress indicators", LOADING_PROGRESS_GROUP, "Linear and circular progress indicators", DemoMaterialDocs.PROGRESS_INDICATORS, (new ProgressDemoPage(context))::createContent),
                new DemoPage("Menus", "Menus", "Menus", "Menu surfaces, actions, and menu buttons", DemoMaterialDocs.MENUS, (new MenusDemoPage(context))::createContent),
                new DemoPage("Navigation", "Navigation bar", NAVIGATION_GROUP, "Bottom navigation items and selected indicators", DemoMaterialDocs.NAVIGATION_BAR, (new NavigationDemoPage(context))::createContent),
                new DemoPage("Navigation Drawer", "Navigation drawer", NAVIGATION_GROUP, "Drawer destinations with selected rows", DemoMaterialDocs.NAVIGATION_DRAWER, (new NavigationDrawerDemoPage(context))::createContent),
                new DemoPage("Navigation Rail", "Navigation rail", NAVIGATION_GROUP, "Vertical destinations for wide layouts", DemoMaterialDocs.NAVIGATION_RAIL, (new NavigationRailDemoPage(context))::createContent),
                new DemoPage("Radio Buttons", "Radio button", "Radio button", "Grouped single selection states", DemoMaterialDocs.RADIO_BUTTON, (new RadioButtonsDemoPage(context))::createContent),
                new DemoPage("Search", "Search", "Search", "Search bars, actions, and result surfaces", DemoMaterialDocs.SEARCH, (new SearchDemoPage(context))::createContent),
                new DemoPage("Bottom Sheets", "Bottom sheets", SHEETS_GROUP, "Bottom sheet containment surfaces", DemoMaterialDocs.BOTTOM_SHEETS, (new BottomSheetsDemoPage(context))::createContent),
                new DemoPage("Side Sheets", "Side sheets", SHEETS_GROUP, "Side sheet containment surfaces", DemoMaterialDocs.SIDE_SHEETS, (new SideSheetsDemoPage(context))::createContent),
                new DemoPage("Sliders", "Sliders", "Sliders", "Different values and disabled slider states", DemoMaterialDocs.SLIDERS, (new SlidersDemoPage(context))::createContent),
                new DemoPage("Snackbars", "Snackbar", "Snackbar", "Snackbar presentation with action and queued messages", DemoMaterialDocs.SNACKBAR, (new SnackbarsDemoPage(context))::createContent),
                new DemoPage("Switches", "Switch", "Switch", "On, off, and disabled switch states", DemoMaterialDocs.SWITCH, (new SwitchesDemoPage(context))::createContent),
                new DemoPage("Tabs", "Tabs", "Tabs", "Primary and secondary fixed and scrollable tabs", DemoMaterialDocs.TABS, (new TabsDemoPage(context))::createContent),
                new DemoPage("Text Fields", "Text fields", "Text fields", "Filled, outlined, populated, error, and disabled fields", DemoMaterialDocs.TEXT_FIELDS, (new TextFieldsDemoPage(context))::createContent),
                new DemoPage("Toolbars", "Toolbars", TOOLBARS_GROUP, "Floating and docked toolbars with Standard and Vibrant colors", DemoMaterialDocs.TOOLBARS, (new ToolbarsDemoPage(context))::createContent),
                new DemoPage("Bottom App Bars", "Bottom app bars", TOOLBARS_GROUP, "Legacy bottom app bars with floating action alignment", DemoMaterialDocs.BOTTOM_APP_BARS, (new BottomAppBarsDemoPage(context))::createContent),
                new DemoPage("Tooltips", "Tooltips", "Tooltips", "Plain and longer contextual help", DemoMaterialDocs.TOOLTIPS, (new TooltipsDemoPage(context))::createContent),
                new DemoPage("Banners", "Banners", ADDITIONAL_DEMOS_GROUP, "Persistent inline feedback with optional actions", DemoMaterialDocs.BANNERS, (new BannersDemoPage(context))::createContent),
                new DemoPage("Forms", "Forms", ADDITIONAL_DEMOS_GROUP, "Form rows and sections for structured input", DemoMaterialDocs.FORMS, (new FormsDemoPage(context))::createContent),
                new DemoPage("Motion", "Motion", ADDITIONAL_DEMOS_GROUP, "Typed state, retained content, and interruptible layout motion", DemoMaterialDocs.MOTION, (new MotionDemoPage(context))::createContent),
                new DemoPage("Typography", "Typography", ADDITIONAL_DEMOS_GROUP, "Token-driven Material type roles", DemoMaterialDocs.TYPOGRAPHY, (new TypographyDemoPage(context))::createContent),
                new DemoPage("Icons", "Icons", ADDITIONAL_DEMOS_GROUP, "Size roles and semantic icon colors", DemoMaterialDocs.ICONS, (new IconsDemoPage(context))::createContent),
                new DemoPage("Avatars", "Avatars", ADDITIONAL_DEMOS_GROUP, "Initials and graphic avatar slots", DemoMaterialDocs.AVATARS, (new AvatarsDemoPage(context))::createContent),
                new DemoPage("Surfaces", "Surfaces", ADDITIONAL_DEMOS_GROUP, "Color containers, shape, padding, and elevation", DemoMaterialDocs.SURFACES, (new SurfacesDemoPage(context))::createContent),
                new DemoPage("Scrims", "Scrims", ADDITIONAL_DEMOS_GROUP, "Modal overlays and dismiss actions", DemoMaterialDocs.SCRIMS, (new ScrimsDemoPage(context))::createContent)
        );
    }
}