// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3AvatarVariant;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BadgedBox;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3ChipSelectionMode;
import org.glavo.m3fx.controls.M3ChipVariant;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.controls.M3DateRangePickerField;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3FormRow;
import org.glavo.m3fx.controls.M3FormSection;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3IconToggleButtonSelectionMode;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3ListViewCell;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3LoadingIndicatorVariant;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3MenuSectionHeader;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3PasswordField;
import org.glavo.m3fx.controls.M3PickerField;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SegmentedButtonSelectionMode;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3SnackbarHost;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SubMenuItem;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3SurfaceElevation;
import org.glavo.m3fx.controls.M3SurfaceVariant;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInput;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.controls.M3TimePickerField;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.controls.M3Toolbar;
import org.glavo.m3fx.controls.M3ToolbarVariant;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/// Visual smoke tests for the demo application's real JavaFX window hierarchy.
@NotNullByDefault
final class M3FXDemoVisualSmokeTest {
    /// Demo pages expected in the same order as the demo page registry.
    private static final @Unmodifiable List<String> ALL_DEMO_PAGE_TITLES = List.of(
            "Components Overview",
            "App Bars",
            "Bottom App Bars",
            "Badges",
            "Button Groups",
            "Buttons",
            "Extended FABs",
            "FAB Menu",
            "Floating Action Buttons",
            "Icon Buttons",
            "Segmented Buttons",
            "Split Buttons",
            "Cards",
            "Carousel",
            "Checkboxes",
            "Chips",
            "Date Pickers",
            "Time Pickers",
            "Dialogs",
            "Dividers",
            "Lists",
            "Loading Indicator",
            "Progress",
            "Menus",
            "Navigation",
            "Navigation Drawer",
            "Navigation Rail",
            "Radio Buttons",
            "Search",
            "Bottom Sheets",
            "Side Sheets",
            "Sliders",
            "Snackbars",
            "Switches",
            "Tabs",
            "Text Fields",
            "Toolbars",
            "Tooltips",
            "Banners",
            "Forms",
            "Typography",
            "Icons",
            "Avatars",
            "Surfaces",
            "Scrims"
    );

    /// Demo pages rendered under the dark expressive theme combination.
    private static final @Unmodifiable List<String> DARK_EXPRESSIVE_VISUAL_PAGES = ALL_DEMO_PAGE_TITLES;

    /// Demo pages rendered under right-to-left mirroring in real window layouts.
    private static final @Unmodifiable List<String> RTL_VISUAL_PAGES = ALL_DEMO_PAGE_TITLES;

    /// Material documentation URLs expected for demo pages that map to official Material pages.
    private static final @Unmodifiable Map<String, String> EXPECTED_MATERIAL_URLS = Map.ofEntries(
            Map.entry("Components Overview", DemoMaterialDocs.COMPONENTS),
            Map.entry("App Bars", DemoMaterialDocs.APP_BARS),
            Map.entry("Bottom App Bars", DemoMaterialDocs.BOTTOM_APP_BARS),
            Map.entry("Badges", DemoMaterialDocs.BADGES),
            Map.entry("Button Groups", DemoMaterialDocs.BUTTON_GROUPS),
            Map.entry("Buttons", DemoMaterialDocs.BUTTONS),
            Map.entry("Extended FABs", DemoMaterialDocs.EXTENDED_FAB),
            Map.entry("FAB Menu", DemoMaterialDocs.FAB_MENU),
            Map.entry("Floating Action Buttons", DemoMaterialDocs.FLOATING_ACTION_BUTTON),
            Map.entry("Icon Buttons", DemoMaterialDocs.ICON_BUTTONS),
            Map.entry("Segmented Buttons", DemoMaterialDocs.SEGMENTED_BUTTONS),
            Map.entry("Split Buttons", DemoMaterialDocs.SPLIT_BUTTON),
            Map.entry("Cards", DemoMaterialDocs.CARDS),
            Map.entry("Carousel", DemoMaterialDocs.CAROUSEL),
            Map.entry("Checkboxes", DemoMaterialDocs.CHECKBOX),
            Map.entry("Chips", DemoMaterialDocs.CHIPS),
            Map.entry("Date Pickers", DemoMaterialDocs.DATE_PICKERS),
            Map.entry("Time Pickers", DemoMaterialDocs.TIME_PICKERS),
            Map.entry("Dialogs", DemoMaterialDocs.DIALOGS),
            Map.entry("Dividers", DemoMaterialDocs.DIVIDER),
            Map.entry("Lists", DemoMaterialDocs.LISTS),
            Map.entry("Loading Indicator", DemoMaterialDocs.LOADING_INDICATOR),
            Map.entry("Progress", DemoMaterialDocs.PROGRESS_INDICATORS),
            Map.entry("Menus", DemoMaterialDocs.MENUS),
            Map.entry("Navigation", DemoMaterialDocs.NAVIGATION_BAR),
            Map.entry("Navigation Drawer", DemoMaterialDocs.NAVIGATION_DRAWER),
            Map.entry("Navigation Rail", DemoMaterialDocs.NAVIGATION_RAIL),
            Map.entry("Radio Buttons", DemoMaterialDocs.RADIO_BUTTON),
            Map.entry("Search", DemoMaterialDocs.SEARCH),
            Map.entry("Bottom Sheets", DemoMaterialDocs.BOTTOM_SHEETS),
            Map.entry("Side Sheets", DemoMaterialDocs.SIDE_SHEETS),
            Map.entry("Sliders", DemoMaterialDocs.SLIDERS),
            Map.entry("Snackbars", DemoMaterialDocs.SNACKBAR),
            Map.entry("Switches", DemoMaterialDocs.SWITCH),
            Map.entry("Tabs", DemoMaterialDocs.TABS),
            Map.entry("Text Fields", DemoMaterialDocs.TEXT_FIELDS),
            Map.entry("Toolbars", DemoMaterialDocs.TOOLBARS),
            Map.entry("Tooltips", DemoMaterialDocs.TOOLTIPS),
            Map.entry("Banners", DemoMaterialDocs.BANNERS),
            Map.entry("Forms", DemoMaterialDocs.FORMS),
            Map.entry("Typography", DemoMaterialDocs.TYPOGRAPHY),
            Map.entry("Icons", DemoMaterialDocs.ICONS),
            Map.entry("Avatars", DemoMaterialDocs.AVATARS),
            Map.entry("Surfaces", DemoMaterialDocs.SURFACES),
            Map.entry("Scrims", DemoMaterialDocs.SCRIMS)
    );

    /// Page-specific visual-state assertions required for every registered demo page.
    private static final @Unmodifiable Map<String, Consumer<Scene>> DEMO_PAGE_VISUAL_STATE_ASSERTIONS = Map.ofEntries(
            Map.entry("Components Overview", M3FXDemoVisualSmokeTest::assertComponentsOverviewPageVisualState),
            Map.entry("App Bars", M3FXDemoVisualSmokeTest::assertAppBarsPageVisualState),
            Map.entry("Bottom App Bars", M3FXDemoVisualSmokeTest::assertBottomAppBarsPageVisualState),
            Map.entry("Badges", M3FXDemoVisualSmokeTest::assertBadgesPageVisualState),
            Map.entry("Button Groups", M3FXDemoVisualSmokeTest::assertButtonGroupsPageVisualState),
            Map.entry("Buttons", M3FXDemoVisualSmokeTest::assertButtonsPageVisualState),
            Map.entry("Extended FABs", M3FXDemoVisualSmokeTest::assertExtendedFabsPageVisualState),
            Map.entry("FAB Menu", M3FXDemoVisualSmokeTest::assertFabMenuPageVisualState),
            Map.entry("Floating Action Buttons", M3FXDemoVisualSmokeTest::assertFloatingActionButtonsPageVisualState),
            Map.entry("Icon Buttons", M3FXDemoVisualSmokeTest::assertIconButtonsPageVisualState),
            Map.entry("Segmented Buttons", M3FXDemoVisualSmokeTest::assertSegmentedButtonsPageVisualState),
            Map.entry("Split Buttons", M3FXDemoVisualSmokeTest::assertSplitButtonsPageVisualState),
            Map.entry("Cards", M3FXDemoVisualSmokeTest::assertCardsPageVisualState),
            Map.entry("Carousel", M3FXDemoVisualSmokeTest::assertCarouselPageVisualState),
            Map.entry("Checkboxes", M3FXDemoVisualSmokeTest::assertCheckboxesPageVisualState),
            Map.entry("Chips", M3FXDemoVisualSmokeTest::assertChipsPageVisualState),
            Map.entry("Date Pickers", M3FXDemoVisualSmokeTest::assertDatePickersPageVisualState),
            Map.entry("Time Pickers", M3FXDemoVisualSmokeTest::assertTimePickersPageVisualState),
            Map.entry("Dialogs", M3FXDemoVisualSmokeTest::assertDialogsPageVisualState),
            Map.entry("Dividers", M3FXDemoVisualSmokeTest::assertDividersPageVisualState),
            Map.entry("Lists", M3FXDemoVisualSmokeTest::assertListsPageVisualState),
            Map.entry("Loading Indicator", M3FXDemoVisualSmokeTest::assertLoadingIndicatorPageVisualState),
            Map.entry("Progress", M3FXDemoVisualSmokeTest::assertProgressPageVisualState),
            Map.entry("Menus", M3FXDemoVisualSmokeTest::assertMenusPageVisualState),
            Map.entry("Navigation", M3FXDemoVisualSmokeTest::assertNavigationPageVisualState),
            Map.entry("Navigation Drawer", M3FXDemoVisualSmokeTest::assertNavigationDrawerPageVisualState),
            Map.entry("Navigation Rail", M3FXDemoVisualSmokeTest::assertNavigationRailPageVisualState),
            Map.entry("Radio Buttons", M3FXDemoVisualSmokeTest::assertRadioButtonsPageVisualState),
            Map.entry("Search", M3FXDemoVisualSmokeTest::assertSearchPageVisualState),
            Map.entry("Bottom Sheets", M3FXDemoVisualSmokeTest::assertBottomSheetsPageVisualState),
            Map.entry("Side Sheets", M3FXDemoVisualSmokeTest::assertSideSheetsPageVisualState),
            Map.entry("Sliders", M3FXDemoVisualSmokeTest::assertSlidersPageVisualState),
            Map.entry("Snackbars", M3FXDemoVisualSmokeTest::assertSnackbarsPageVisualState),
            Map.entry("Switches", M3FXDemoVisualSmokeTest::assertSwitchesPageVisualState),
            Map.entry("Tabs", M3FXDemoVisualSmokeTest::assertTabsPageVisualState),
            Map.entry("Text Fields", M3FXDemoVisualSmokeTest::assertTextFieldsPageVisualState),
            Map.entry("Toolbars", M3FXDemoVisualSmokeTest::assertToolbarsPageVisualState),
            Map.entry("Tooltips", M3FXDemoVisualSmokeTest::assertTooltipsPageVisualState),
            Map.entry("Banners", M3FXDemoVisualSmokeTest::assertBannersPageVisualState),
            Map.entry("Forms", M3FXDemoVisualSmokeTest::assertFormsPageVisualState),
            Map.entry("Typography", M3FXDemoVisualSmokeTest::assertTypographyPageVisualState),
            Map.entry("Icons", M3FXDemoVisualSmokeTest::assertIconsPageVisualState),
            Map.entry("Avatars", M3FXDemoVisualSmokeTest::assertAvatarsPageVisualState),
            Map.entry("Surfaces", M3FXDemoVisualSmokeTest::assertSurfacesPageVisualState),
            Map.entry("Scrims", M3FXDemoVisualSmokeTest::assertScrimsPageVisualState)
    );

    /// Fixed-target controls whose visible glyph content should stay centered.
    private static final @Unmodifiable Set<String> CENTERED_TARGET_STYLE_CLASSES = Set.of(
            M3DatePicker.DAY_CELL_STYLE_CLASS,
            M3TimePicker.CELL_STYLE_CLASS,
            M3IconButton.STYLE_CLASS,
            M3IconToggleButton.STYLE_CLASS,
            M3SegmentedButton.STYLE_CLASS
    );

    /// Demo pages that should use SVG icons instead of text-placeholder icon controls in interactive slots.
    private static final @Unmodifiable Map<String, Integer> DEMO_VECTOR_ICON_PAGE_MINIMUMS = Map.ofEntries(
            Map.entry("App Bars", 18),
            Map.entry("Bottom App Bars", 9),
            Map.entry("Avatars", 1),
            Map.entry("Banners", 2),
            Map.entry("Bottom Sheets", 3),
            Map.entry("Chips", 1),
            Map.entry("FAB Menu", 6),
            Map.entry("Floating Action Buttons", 3),
            Map.entry("Icon Buttons", 12),
            Map.entry("Icons", 12),
            Map.entry("Lists", 8),
            Map.entry("Menus", 10),
            Map.entry("Navigation", 7),
            Map.entry("Navigation Drawer", 7),
            Map.entry("Navigation Rail", 7),
            Map.entry("Search", 5),
            Map.entry("Side Sheets", 3),
            Map.entry("Text Fields", 5),
            Map.entry("Toolbars", 9),
            Map.entry("Tooltips", 1)
    );

    /// The edge tolerance used when comparing text bounds against scene and viewport bounds.
    private static final double TEXT_EDGE_TOLERANCE = 1.0;

    /// The largest accepted vertical antialiasing overflow for rendered text ink.
    private static final double TEXT_INK_VERTICAL_OVERFLOW_TOLERANCE = 1.0;

    /// The minimum rendered ink height expected for visible alphanumeric text.
    private static final double TEXT_INK_MINIMUM_FONT_HEIGHT_RATIO = 0.42;

    /// The pixel tolerance used by rendered text ink height coverage checks.
    private static final double TEXT_INK_HEIGHT_COVERAGE_TOLERANCE = 1.0;

    /// The edge tolerance used when comparing visible control bounds against scene and viewport bounds.
    private static final double CONTROL_EDGE_TOLERANCE = 2.0;

    /// The minimum width expected from app bar previews in desktop demo captures.
    private static final double APP_BAR_PREVIEW_MIN_WIDTH = 680.0;

    /// The tolerance used when checking app bar slot placement against component tokens.
    private static final double APP_BAR_SLOT_TOLERANCE = 4.0;

    /// The Material top app bar navigation and action slot size.
    private static final double APP_BAR_ACTION_SLOT_SIZE = 48.0;

    /// The baseline Material icon button container size used by demo app bar actions.
    private static final double APP_BAR_ICON_BUTTON_SIZE = 40.0;

    /// The Material icon viewport size used by demo SVG icon slots.
    private static final double DEMO_ICON_VIEWPORT_SIZE = 24.0;

    /// The tolerance used when checking icon layout centering inside fixed action targets.
    private static final double DEMO_ICON_CENTER_TOLERANCE = 2.0;

    /// The tolerance used when checking rendered icon pixels inside fixed demo viewports.
    private static final double DEMO_ICON_PIXEL_CENTER_TOLERANCE = 3.0;

    /// The minimum safe vertical room for single-line input text inside its editable area.
    private static final double INPUT_TEXT_MINIMUM_VERTICAL_ROOM = 4.0;

    /// The tolerance used when checking text input adornment centering.
    private static final double TEXT_INPUT_SLOT_CENTER_TOLERANCE = 5.0;

    /// The tolerance used when checking rendered text input ink centering.
    private static final double TEXT_INPUT_INK_CENTER_TOLERANCE = 3.0;

    /// The tolerance used when checking rendered picker-cell text ink centering.
    private static final double PICKER_CELL_TEXT_INK_CENTER_TOLERANCE = 2.0;

    /// The lowest acceptable rendered ink center ratio for outlined fields with a floating label.
    private static final double OUTLINED_FLOATING_INK_MINIMUM_CENTER_RATIO = 0.46;

    /// The highest acceptable rendered ink center ratio for outlined fields with a floating label.
    private static final double OUTLINED_FLOATING_INK_MAXIMUM_CENTER_RATIO = 0.66;

    /// The maximum color distance allowed between an outlined floating label's empty pixels and nearby background.
    private static final double OUTLINED_FLOATING_LABEL_BACKGROUND_TOLERANCE = 0.055;

    /// The largest accepted ratio of sampled label-background pixels that may differ from nearby background.
    private static final double OUTLINED_FLOATING_LABEL_BACKGROUND_MISMATCH_RATIO = 0.08;

    /// The lowest acceptable rendered ink center ratio for filled fields with a floating label.
    private static final double FILLED_FLOATING_INK_MINIMUM_CENTER_RATIO = 0.48;

    /// The highest acceptable rendered ink center ratio for filled fields with a floating label.
    private static final double FILLED_FLOATING_INK_MAXIMUM_CENTER_RATIO = 0.72;

    /// The tolerance used when checking rendered selection-control pixel centering.
    private static final double SELECTION_PIXEL_CENTER_TOLERANCE = 1.25;

    /// The tolerance used when checking square rendered selection-control pixel shapes.
    private static final double SELECTION_PIXEL_SHAPE_TOLERANCE = 2.0;

    /// The lowest acceptable vertical center ratio for single-line input text.
    private static final double INPUT_TEXT_MINIMUM_CENTER_RATIO = 0.40;

    /// The highest acceptable vertical center ratio for single-line input text.
    private static final double INPUT_TEXT_MAXIMUM_CENTER_RATIO = 0.66;

    /// The tolerance used when checking rendered text ink centering inside `Labeled` controls.
    private static final double LABELED_TEXT_INK_VERTICAL_CENTER_TOLERANCE = 4.0;

    /// The list item headline label style class used by the list item skin.
    private static final String LIST_ITEM_HEADLINE_STYLE_CLASS = "m3-list-item-headline";

    /// The list item overline label style class used by the list item skin.
    private static final String LIST_ITEM_OVERLINE_STYLE_CLASS = "m3-list-item-overline";

    /// The list item supporting label style class used by the list item skin.
    private static final String LIST_ITEM_SUPPORTING_STYLE_CLASS = "m3-list-item-supporting";

    /// The list item trailing supporting label style class used by the list item skin.
    private static final String LIST_ITEM_TRAILING_SUPPORTING_STYLE_CLASS = "m3-list-item-trailing-supporting";

    /// The list item leading slot style class used by the list item skin.
    private static final String LIST_ITEM_LEADING_STYLE_CLASS = "m3-list-item-leading";

    /// The list item trailing slot style class used by the list item skin.
    private static final String LIST_ITEM_TRAILING_STYLE_CLASS = "m3-list-item-trailing";

    /// The tolerance used when checking rendered headline ink centering inside one-line list item rows.
    private static final double LIST_ITEM_HEADLINE_INK_VERTICAL_CENTER_TOLERANCE = 4.5;

    /// The tolerance used when checking one-line list item slot vertical centering.
    private static final double LIST_ITEM_SLOT_CENTER_TOLERANCE = 4.0;

    /// The tolerance used when checking fixed list item slot token sizes.
    private static final double LIST_ITEM_FIXED_SLOT_SIZE_TOLERANCE = 1.5;

    /// The hover pseudo-class used when rendering synthetic interaction snapshots.
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("hover");

    /// The style class used by animated Material ripple nodes.
    private static final String RIPPLE_STYLE_CLASS = "m3-ripple";

    /// The JavaFX pulse count required after a final visual state first becomes true.
    private static final int SETTLED_STATE_PULSES = 2;

    /// The number of advancing rendered frames required for continuously animated demo components.
    private static final int CONTINUOUS_ANIMATION_FRAME_COUNT = 4;

    /// The number of advancing rendered frame batches required to prove long-running animations keep moving.
    private static final int CONTINUOUS_ANIMATION_BATCH_COUNT = 2;

    /// The tolerance used when checking rendered loading-indicator centroid stability in demo snapshots.
    private static final double LOADING_INDICATOR_PIXEL_CENTER_TOLERANCE = 4.0;

    /// The long linear motion spec duration used to make animation frames visually observable in tests.
    private static final Duration OBSERVABLE_MOTION_DURATION = Duration.millis(600.0);

    /// The fixed size expected for demo header seed-color icon buttons.
    private static final double HEADER_SEED_BUTTON_SIZE = 32.0;

    /// The tolerance used when checking demo header seed-color button dimensions.
    private static final double HEADER_SEED_BUTTON_SIZE_TOLERANCE = 1.5;

    /// The tolerance used when checking demo header seed-color button circular shape tokens.
    private static final double HEADER_SEED_BUTTON_ROUNDNESS_TOLERANCE = 1.0;

    /// Starts the JavaFX toolkit before creating the demo stage.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        DemoFxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that every demo page opens cleanly and exposes a stable Material documentation link.
    @Test
    void demoPagesOpenWithoutCssWarningsAndExposeMaterialDocumentationLinks() throws InterruptedException {
        assertDemoPagesOpenWithoutCssWarningsAndExposeMaterialDocumentationLinks(
                "standard",
                ALL_DEMO_PAGE_TITLES,
                1280.0,
                900.0,
                (app, scene) -> {
                },
                scene -> {
                }
        );
        assertDemoPagesOpenWithoutCssWarningsAndExposeMaterialDocumentationLinks(
                "dark expressive",
                DARK_EXPRESSIVE_VISUAL_PAGES,
                1366.0,
                900.0,
                (app, scene) -> app.setDarkExpressiveThemeForTesting(),
                scene -> {
                    assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                    assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                }
        );
        assertDemoPagesOpenWithoutCssWarningsAndExposeMaterialDocumentationLinks(
                "right-to-left",
                RTL_VISUAL_PAGES,
                1366.0,
                900.0,
                (app, scene) -> scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT),
                scene -> assertEquals(NodeOrientation.RIGHT_TO_LEFT, scene.getRoot().getEffectiveNodeOrientation())
        );
    }

    /// Opens every registered demo page in one runtime mode while JavaFX CSS warnings are captured.
    private static void assertDemoPagesOpenWithoutCssWarningsAndExposeMaterialDocumentationLinks(
            String modeName,
            List<String> pageTitles,
            double stageWidth,
            double stageHeight,
            BiConsumer<M3FXDemoApp, Scene> modeSetup,
            Consumer<Scene> modeAssertion
    ) throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                runOnFxThread(() -> {
                    Stage stage = new Stage();
                    M3FXDemoApp app = new M3FXDemoApp();
                    app.start(stage);
                    stage.setWidth(stageWidth);
                    stage.setHeight(stageHeight);

                    Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
                    modeSetup.accept(app, scene);

                    stageReference.set(stage);
                    appReference.set(app);
                    sceneReference.set(scene);

                    assertEquals(pageTitles, app.demoPageTitlesForTesting(), modeName + " demo page registry");
                    assertEquals(EXPECTED_MATERIAL_URLS, app.demoPageMaterialUrlsForTesting(),
                            modeName + " Material documentation URL registry");
                    assertEquals(pageTitles, new ArrayList<>(app.demoPageMaterialUrlsForTesting().keySet()),
                            modeName + " Material documentation URL order");

                    for (String title : pageTitles) {
                        String url = Objects.requireNonNull(
                                app.demoPageMaterialUrlsForTesting().get(title),
                                title + " Material documentation URL"
                        );
                        assertMaterialDocumentationUrl(title, url);
                    }
                });

                for (String title : pageTitles) {
                    showPageWhenSidebarSelectionSettled(
                            appReference,
                            sceneReference,
                            title,
                            scene -> {
                                modeAssertion.accept(scene);
                                resetDemoPageScroll(scene);
                            },
                            () -> {
                                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                                modeAssertion.accept(scene);
                                assertCurrentPageTitle(scene, title);
                                assertSidebarSelectionMatchesCurrentPage(app, title);
                                assertMaterialDocumentationLink(scene, title);
                            }
                    );
                }
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies one rendered demo page header documentation link.
    private static void assertMaterialDocumentationLink(Scene scene, String title) {
        Node docsLink = Objects.requireNonNull(
                scene.lookup(".demo-page-doc-link"),
                title + " docs link"
        );
        M3Button docsButton = assertInstanceOf(M3Button.class, docsLink, title + " docs link control");
        assertEquals("Material docs", docsButton.getText(), title + " docs link text");
        assertTrue(docsButton.isVisible(), title + " docs link visible");
        assertTrue(docsButton.isManaged(), title + " docs link managed");
        assertFalse(docsButton.isDisabled(), title + " docs link enabled");
        assertNotNull(docsButton.getOnAction(), title + " docs link action");
        assertTrue(docsButton.getBoundsInParent().getWidth() > 0.0, title + " docs link width");
        assertTrue(docsButton.getBoundsInParent().getHeight() > 0.0, title + " docs link height");
    }

    /// Verifies that every registered demo page contributes a dedicated visual-state assertion.
    @Test
    void demoPageVisualStateAssertionsCoverRegisteredPages() {
        assertEquals(
                ALL_DEMO_PAGE_TITLES.size(),
                DEMO_PAGE_VISUAL_STATE_ASSERTIONS.size(),
                "every registered demo page should have one page-specific visual assertion"
        );

        for (String pageTitle : ALL_DEMO_PAGE_TITLES) {
            assertNotNull(
                    DEMO_PAGE_VISUAL_STATE_ASSERTIONS.get(pageTitle),
                    () -> pageTitle + " page-specific visual assertion"
            );
        }
        for (String pageTitle : DEMO_PAGE_VISUAL_STATE_ASSERTIONS.keySet()) {
            assertTrue(
                    ALL_DEMO_PAGE_TITLES.contains(pageTitle),
                    () -> "unexpected page-specific visual assertion: " + pageTitle
            );
        }
    }

    /// Verifies that a demo page URL points at the official Material Design site.
    private static void assertMaterialDocumentationUrl(String title, String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            fail(title + " Material documentation URL is not a valid URI: " + url, e);
            return;
        }

        assertEquals("https", uri.getScheme(), title + " Material documentation URL scheme");
        assertEquals("m3.material.io", uri.getHost(), title + " Material documentation URL host");
        assertTrue(uri.getPath().startsWith("/"), title + " Material documentation URL path");
        assertFalse(uri.getPath().isBlank(), title + " Material documentation URL blank path");
        assertNull(uri.getRawQuery(), title + " Material documentation URL query");
        assertFalse(url.contains(" "), title + " Material documentation URL whitespace");
    }

    /// Verifies that clicking demo header theme controls reapplies the theme without style or shape regressions.
    @Test
    void headerThemeControlsReapplyThemeWithoutCssWarningsOrShapeRegression() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> runOnFxThread(() -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                app.showPageForTesting("Buttons");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                assertCurrentPageTitle(scene, "Buttons");
                assertDemoHeaderThemeControlGeometry(scene, "initial header");

                Node header = requireVisibleStyledDescendant(scene.getRoot(), "demo-header", "demo header");
                List<M3IconButton> seedButtons = headerSeedButtons(header);
                assertEquals(5, seedButtons.size(), "demo header seed button count");
                for (int index = 0; index < seedButtons.size(); index++) {
                    clickHeaderThemeControl(scene, seedButtons.get(index), "seed color " + index);
                }

                clickHeaderThemeButton(scene, "Expressive", "profile expressive");
                assertRootStyleMode(
                        scene,
                        M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS,
                        M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS,
                        "expressive profile"
                );
                clickHeaderThemeButton(scene, "Baseline", "profile baseline");
                assertRootStyleMode(
                        scene,
                        M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS,
                        M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS,
                        "baseline profile"
                );

                clickHeaderThemeButton(scene, "Standard", "density comfort");
                clickHeaderThemeButton(scene, "Comfort", "density compact");
                clickHeaderThemeButton(scene, "Compact", "density standard");

                clickHeaderThemeSwitch(scene, "Animations", "animations disabled");
                clickHeaderThemeSwitch(scene, "Animations", "animations enabled");

                clickHeaderThemeButton(scene, "Dark", "dark brightness");
                assertRootStyleMode(
                        scene,
                        M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS,
                        M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS,
                        "dark brightness"
                );
                clickHeaderThemeButton(scene, "Light", "light brightness");
                assertRootStyleMode(
                        scene,
                        M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS,
                        M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS,
                        "light brightness"
                );

                assertHeaderSeedButtonsRenderCircular(scene, "after header theme toggles");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that app bar demo pages render full-width app bar components without placeholder icons.
    @Test
    void appBarDemoPagesUseRealFullWidthPreviewBars() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThread(() -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                app.showPageForTesting("App Bars");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                assertDemoVectorIcons(scene.getRoot(), "App Bars", 18);
                List<M3TopAppBar> topAppBars = visibleNodesOfType(scene.getRoot(), M3TopAppBar.class);
                assertEquals(6, topAppBars.size(), "top app bar demo count");
                assertEquals(M3TopAppBarVariant.SMALL, topAppBars.get(0).getVariant());
                assertEquals(M3TopAppBarVariant.CENTER_ALIGNED, topAppBars.get(1).getVariant());
                assertEquals(M3TopAppBarVariant.MEDIUM, topAppBars.get(2).getVariant());
                assertEquals(M3TopAppBarVariant.LARGE, topAppBars.get(3).getVariant());
                assertFalse(topAppBars.get(0).isScrolledUnder(), "first top app bar should show the default state");
                assertFalse(topAppBars.get(1).isScrolledUnder(), "center-aligned top app bar should show the default state");
                assertFalse(topAppBars.get(2).isScrolledUnder(), "medium top app bar should show the default state");
                assertFalse(topAppBars.get(3).isScrolledUnder(), "large top app bar should show the default state");
                assertTrue(topAppBars.get(4).isScrolledUnder(), "fifth top app bar should show the scrolled-under state");
                assertTrue(topAppBars.get(5).isScrolledUnder(), "sixth top app bar should show the scrolled-under state");
                for (int index = 0; index < topAppBars.size(); index++) {
                    M3TopAppBar appBar = topAppBars.get(index);
                    assertAppBarFitsPreview(appBar, "top app bar");
                    assertTopAppBarPreviewBalance(appBar);
                    assertAppBarPreviewIsNotInsideGenericFlow(appBar);
                    assertTopAppBarSlotGeometry(appBar);
                    if (index == 1) {
                        assertAppBarUsesVectorIconButtons(appBar, "top app bar", "back", "add", "more");
                    } else {
                        assertAppBarUsesVectorIconButtons(appBar, "top app bar", "menu", "search", "more");
                    }
                }
                List<M3BottomAppBar> appBarsPageBottomAppBars =
                        visibleNodesOfType(scene.getRoot(), M3BottomAppBar.class);
                assertEquals(0, appBarsPageBottomAppBars.size(),
                        "App Bars page should not duplicate bottom app bars that belong to Toolbars");
                WritableImage appBarsImage = snapshot(scene);
                writeVisualSnapshot(appBarsImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "app-bars-top-variants.png"
                ));
                assertSnapshotHasVisibleContent(appBarsImage, "App Bars");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(scene.getRoot(), "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertTrue(topAppBars.stream()
                                .filter(M3TopAppBar::isScrolledUnder)
                                .anyMatch(appBar -> !isOutsideSceneViewport(
                                        appBar,
                                        appBar.localToScene(appBar.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "App Bars page should render the scrolled-under examples in a captured viewport");
                WritableImage appBarsScrolledImage = snapshot(scene);
                writeVisualSnapshot(appBarsScrolledImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "app-bars-scrolled-under.png"
                ));
                assertSnapshotHasVisibleContent(appBarsScrolledImage, "App Bars scrolled-under");
            });

            runOnFxThreadWhenStable(
                    () -> {
                        Scene scene = sceneReference.get();
                        return scene != null && visibleNodesOfType(scene.getRoot(), M3BottomAppBar.class).size() == 3;
                    },
                    SETTLED_STATE_PULSES,
                    () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting("Bottom App Bars");
                        ScrollPane pageScrollPane = assertInstanceOf(
                                ScrollPane.class,
                                requireVisibleStyledDescendant(scene.getRoot(), "demo-scroll-pane", "demo page scroll pane")
                        );
                        pageScrollPane.setVvalue(0.0);
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        assertCurrentPageTitle(scene, "Bottom App Bars");
                        assertDemoVectorIcons(scene.getRoot(), "Bottom App Bars", 9);
                        List<M3TopAppBar> topAppBars = visibleNodesOfType(scene.getRoot(), M3TopAppBar.class);
                        assertEquals(0, topAppBars.size(),
                                "Bottom App Bars page should not duplicate top app bars");
                        List<M3BottomAppBar> bottomAppBars =
                                visibleNodesOfType(scene.getRoot(), M3BottomAppBar.class);
                        assertEquals(3, bottomAppBars.size(), "bottom app bar demo count");
                        assertEquals(M3BottomAppBarFloatingActionAlignment.END,
                                bottomAppBars.get(0).getFloatingActionAlignment());
                        assertEquals(M3BottomAppBarFloatingActionAlignment.CENTER,
                                bottomAppBars.get(1).getFloatingActionAlignment());
                        assertEquals(M3BottomAppBarFloatingActionAlignment.START,
                                bottomAppBars.get(2).getFloatingActionAlignment());
                        for (M3BottomAppBar appBar : bottomAppBars) {
                            assertAppBarFitsPreview(appBar, "bottom app bar");
                            assertBottomAppBarPreviewBalance(appBar);
                            assertBottomAppBarSlotGeometry(appBar);
                            assertAppBarUsesVectorIconButtons(appBar, "bottom app bar", "search", "favorite");
                        }
                        WritableImage bottomAppBarsImage = snapshot(scene);
                        writeVisualSnapshot(bottomAppBarsImage, Path.of(
                                "build",
                                "reports",
                                "m3fx-demo-visual",
                                "bottom-app-bars.png"
                        ));
                        assertSnapshotHasVisibleContent(bottomAppBarsImage, "Bottom App Bars");
                    }
            );

            runOnFxThreadWhenStable(
                    () -> {
                        Scene scene = sceneReference.get();
                        return scene != null && visibleNodesOfType(scene.getRoot(), M3Toolbar.class).size() == 4;
                    },
                    SETTLED_STATE_PULSES,
                    () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting("Toolbars");
                        ScrollPane pageScrollPane = assertInstanceOf(
                                ScrollPane.class,
                                requireVisibleStyledDescendant(scene.getRoot(), "demo-scroll-pane", "demo page scroll pane")
                        );
                        pageScrollPane.setVvalue(0.0);
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        assertCurrentPageTitle(scene, "Toolbars");
                        List<M3Toolbar> toolbars = visibleNodesOfType(scene.getRoot(), M3Toolbar.class);
                        assertEquals(4, toolbars.size(), "toolbar demo count");
                        assertEquals(M3ToolbarVariant.STANDARD, toolbars.get(0).getVariant());
                        assertEquals(M3ToolbarVariant.FLOATING, toolbars.get(1).getVariant());
                        assertEquals(M3ToolbarVariant.DOCKED, toolbars.get(2).getVariant());
                        assertEquals(Orientation.VERTICAL, toolbars.get(3).getOrientation());
                        for (M3Toolbar toolbar : toolbars) {
                            assertToolbarDemoGeometry(toolbar);
                        }
                        assertEquals(0, visibleNodesOfType(scene.getRoot(), M3BottomAppBar.class).size(),
                                "Toolbars page should showcase M3Toolbar instead of bottom app bars");
                        WritableImage toolbarsImage = snapshot(scene);
                        writeVisualSnapshot(toolbarsImage, Path.of(
                                "build",
                                "reports",
                                "m3fx-demo-visual",
                                "toolbars.png"
                        ));
                        assertSnapshotHasVisibleContent(toolbarsImage, "Toolbars");
                    }
            );
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Lists demo renders the data-driven list through reusable virtualized rows.
    @Test
    void listPageUsesVirtualizedReusableRowsInDemo() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Lists", scene -> {
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                assertCurrentPageTitle(scene, "Lists");

                @SuppressWarnings("rawtypes")
                List<M3ListView> listViews = visibleNodesOfType(scene.getRoot(), M3ListView.class);
                assertEquals(1, listViews.size(), "Lists page should show one virtualized list view");
                @SuppressWarnings("rawtypes")
                M3ListView listView = listViews.get(0);
                assertEquals(240, listView.getItems().size(), "demo list should contain enough rows for virtualization");
                assertEquals(72.0, listView.getFixedCellSize(), 0.01, "demo list should use two-line row height");
                assertEquals(2, listView.getSelectedIndex(), "demo list should show a non-first selected row");
                assertInstanceOf(VirtualFlow.class, listView.lookup(".m3-list-view-flow"));

                List<M3ListViewCell> visibleCells = visibleNodesOfType(listView, M3ListViewCell.class);
                int maxExpectedVisibleCells =
                        (int) Math.ceil(listView.getPrefHeight() / listView.getFixedCellSize()) + 8;
                assertTrue(!visibleCells.isEmpty(), "virtualized list should attach visible cells");
                assertTrue(visibleCells.size() <= maxExpectedVisibleCells,
                        () -> "virtualized list attached too many cells: " + visibleCells.size());
                assertTrue(visibleCells.size() < listView.getItems().size(),
                        () -> "virtualized list should not materialize every row: " + visibleCells.size());

                listView.setAnimatedScroll(false);
                listView.scrollTo(180);
                scene.getRoot().applyCss();
                scene.getRoot().layout();

                List<M3ListItem> visibleRows = visibleNodesOfType(listView, M3ListItem.class);
                assertTrue(visibleRows.stream().anyMatch(item -> "Virtualized row 181".equals(item.getHeadlineText())),
                        () -> "scrolling to a far row did not render the expected reused cell: " + visibleRows);
                assertTrue(visibleRows.stream().noneMatch(item -> "Virtualized row 1".equals(item.getHeadlineText())),
                        "far-row scroll should replace the initially visible first row");

                WritableImage image = snapshotNode(listView);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "lists-virtualized-view.png"
                ));
                assertSnapshotHasVisibleContent(image, "Lists virtualized view");
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that date and time picker demo pages render fields, dialogs, and centered picker cells.
    @Test
    void dateAndTimePickerPagesRenderStableFieldsAndCells() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Date Pickers", scene -> {
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                assertDatePickersPageVisualState(scene);

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "date-pickers-fields-and-cells.png"
                ));
                assertSnapshotHasVisibleContent(image, "Date Pickers fields and cells");
            });

            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Time Pickers", scene -> {
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                assertTimePickersPageVisualState(scene);

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "time-pickers-fields-and-cells.png"
                ));
                assertSnapshotHasVisibleContent(image, "Time Pickers fields and cells");
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that button-family demo pages render their variants, groups, actions, and icon targets.
    @Test
    void buttonFamilyDemoPagesRenderVariantsGroupsAndActions() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(950.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                assertButtonFamilyDemoPage(
                        appReference,
                        sceneReference,
                        "Buttons",
                        M3FXDemoVisualSmokeTest::assertButtonsPageVisualState
                );
                assertButtonFamilyDemoPage(
                        appReference,
                        sceneReference,
                        "Button Groups",
                        M3FXDemoVisualSmokeTest::assertButtonGroupsPageVisualState
                );
                assertButtonFamilyDemoPage(
                        appReference,
                        sceneReference,
                        "Icon Buttons",
                        M3FXDemoVisualSmokeTest::assertIconButtonsPageVisualState
                );
                assertButtonFamilyDemoPage(
                        appReference,
                        sceneReference,
                        "Floating Action Buttons",
                        M3FXDemoVisualSmokeTest::assertFloatingActionButtonsPageVisualState
                );
                assertButtonFamilyDemoPage(
                        appReference,
                        sceneReference,
                        "Extended FABs",
                        M3FXDemoVisualSmokeTest::assertExtendedFabsPageVisualState
                );
                assertButtonFamilyDemoPage(
                        appReference,
                        sceneReference,
                        "FAB Menu",
                        M3FXDemoVisualSmokeTest::assertFabMenuPageVisualState
                );
                assertButtonFamilyDemoPage(
                        appReference,
                        sceneReference,
                        "Split Buttons",
                        M3FXDemoVisualSmokeTest::assertSplitButtonsPageVisualState
                );
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Search demo renders active, inactive, and keyboard-reachable result states.
    @Test
    void searchPageRendersActiveInactiveAndKeyboardReachableResults() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> showPageWhenSidebarSelectionSettled(
                    appReference, sceneReference, "Search", scene -> {
                    }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                assertCurrentPageTitle(scene, "Search");

                List<M3SearchView> searchViews = visibleNodesOfType(scene.getRoot(), M3SearchView.class);
                assertEquals(2, searchViews.size(), "Search page should show active and inactive search views");
                M3SearchView activeView = Objects.requireNonNull(
                        searchViews.stream().filter(M3SearchView::isActive).findFirst().orElse(null),
                        "active search view"
                );
                M3SearchView inactiveView = Objects.requireNonNull(
                        searchViews.stream().filter(view -> !view.isActive()).findFirst().orElse(null),
                        "inactive search view"
                );

                List<M3SearchBar> searchBars = visibleNodesOfType(scene.getRoot(), M3SearchBar.class);
                assertEquals(4, searchBars.size(),
                        "Search page should expose two standalone bars and two embedded search bars");
                searchBars.forEach(M3FXDemoVisualSmokeTest::assertSearchBarVisualGeometry);

                assertSearchViewResultsVisible(activeView);
                assertSearchViewResultsHidden(inactiveView);
                assertFalse(visibleNodesOfType(scene.getRoot(), Text.class).stream()
                                .anyMatch(text -> "Hidden result".equals(text.getText())),
                        "inactive search view should not render hidden result text");

                M3ListItem firstResult = assertInstanceOf(M3ListItem.class, activeView.getResults().get(0));
                activeView.getEditor().requestFocus();
                activeView.getEditor().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                scene.getRoot().applyCss();
                scene.getRoot().layout();

                @Nullable Node focusOwner = scene.getFocusOwner();
                assertNotNull(focusOwner, "search result keyboard navigation should produce a focus owner");
                assertTrue(isNodeOrDescendant(firstResult, focusOwner),
                        () -> "DOWN from the search editor should focus the first result: focusOwner=" + focusOwner);

                WritableImage image = snapshotNode(activeView);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "search-active-view.png"
                ));
                assertSnapshotHasVisibleContent(image, "Search active view");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Cards demo renders variant, media, action, and disabled states.
    @Test
    void cardsPageRendersVariantsMediaActionsAndStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> showPageWhenSidebarSelectionSettled(
                    appReference, sceneReference, "Cards", scene -> {
                    }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Cards");

                List<M3Card> cards = visibleNodesOfType(root, M3Card.class);
                assertEquals(6, cards.size(), "Cards page should render compact, media, and state card examples");
                assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.FILLED).count());
                assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.OUTLINED).count());
                assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.ELEVATED).count());
                assertTrue(cards.stream().allMatch(card -> card.getOnAction() != null),
                        "demo cards should exercise actionable surface semantics");
                assertEquals(1, cards.stream().filter(Node::isDisabled).count(),
                        "Cards page should include one disabled card state");

                assertEquals(3, visibleNodesWithStyle(root, "demo-card-media").size(),
                        "Cards page should render three media cards");
                assertEquals(3, visibleNodesWithStyle(root, "demo-card-actions").size(),
                        "Cards page should render action rows for the media cards");
                long cardActionButtons = visibleNodesOfType(root, M3Button.class).stream()
                        .filter(button -> nearestAncestorOfType(button, M3Card.class) != null)
                        .count();
                assertEquals(6, cardActionButtons, "media cards should expose two actions each");

                for (M3Card card : cards) {
                    assertCardDemoGeometry(card);
                    assertNotNull(firstVisibleStyledDescendant(card, "demo-card-title"),
                            "each demo card should render a title");
                    assertNotNull(firstVisibleStyledDescendant(card, "demo-card-body"),
                            "each demo card should render supporting text");
                }

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "cards-rich-states.png"
                ));
                assertSnapshotHasVisibleContent(image, "Cards rich states");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(root, "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertTrue(cards.stream()
                                .filter(card -> firstVisibleStyledDescendant(card, "demo-card-actions") != null)
                                .anyMatch(card -> !isOutsideSceneViewport(
                                        card,
                                        card.localToScene(card.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Cards page should render media card actions in a captured viewport");
                assertTrue(cards.stream()
                                .filter(Node::isDisabled)
                                .anyMatch(card -> !isOutsideSceneViewport(
                                        card,
                                        card.localToScene(card.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Cards page should render the disabled card state in a captured viewport");

                WritableImage mediaImage = snapshot(scene);
                writeVisualSnapshot(mediaImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "cards-media-actions.png"
                ));
                assertSnapshotHasVisibleContent(mediaImage, "Cards media and actions");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Carousel demo renders scrollable viewports, selected items, and action-driven selection.
    @Test
    void carouselPageRendersViewportSelectionAndActionNavigation() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Carousel> multiBrowseReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Carousel> compactReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Carousel", scene -> {
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Carousel");
                assertVisibleText(root, "Multi-browse", "Carousel");
                assertVisibleText(root, "Compact", "Carousel");
                assertVisibleText(root, "Previous", "Carousel");
                assertVisibleText(root, "Next", "Carousel");

                List<M3Carousel> carousels = visibleNodesOfType(root, M3Carousel.class);
                assertEquals(2, carousels.size(),
                        () -> "Carousel page should render two carousel variants, found " + carousels.size());

                M3Carousel multiBrowse = carousels.stream()
                        .filter(carousel -> carousel.getItems().size() == 5)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("missing multi-browse carousel"));
                M3Carousel compact = carousels.stream()
                        .filter(carousel -> carousel.getItems().size() == 4)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("missing compact carousel"));
                assertEquals(1, multiBrowse.getSelectedIndex(), "multi-browse carousel should select Design review");
                assertEquals(0, compact.getSelectedIndex(), "compact carousel should select Inbox");
                assertCarouselDemoGeometry(multiBrowse, "multi-browse carousel");
                assertCarouselDemoGeometry(compact, "compact carousel");

                multiBrowseReference.set(multiBrowse);
                compactReference.set(compact);

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "carousel-viewport-selection.png"
                ));
                assertSnapshotHasVisibleContent(image, "Carousel viewport selection");
            });

            runOnFxThreadWhenStable(() -> {
                @Nullable Scene scene = sceneReference.get();
                @Nullable M3Carousel multiBrowse = multiBrowseReference.get();
                @Nullable M3Carousel compact = compactReference.get();
                if (scene == null || multiBrowse == null || compact == null) {
                    return false;
                }
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                return multiBrowse.getSelectedIndex() == 2
                        && compact.getSelectedIndex() == 0
                        && hasRenderableBounds(multiBrowse)
                        && hasRenderableBounds(compact);
            }, SETTLED_STATE_PULSES, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                M3Carousel multiBrowse = Objects.requireNonNull(multiBrowseReference.get(), "multi-browse carousel");
                M3MotionSettings.setAnimationsEnabled(multiBrowse, false);
                multiBrowse.setAnimatedScroll(false);
                M3Button next = Objects.requireNonNull(
                        firstVisibleButtonWithText(scene.getRoot(), "Next"),
                        "carousel next button"
                );
                next.fire();
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                multiBrowse.scrollSelectedItemIntoView(false);
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                M3Carousel multiBrowse = Objects.requireNonNull(multiBrowseReference.get(), "multi-browse carousel");
                M3Carousel compact = Objects.requireNonNull(compactReference.get(), "compact carousel");
                assertEquals(2, multiBrowse.getSelectedIndex(), "Next action should select Release notes");
                assertEquals(0, compact.getSelectedIndex(), "compact carousel should remain unchanged");
                assertCarouselDemoGeometry(multiBrowse, "multi-browse carousel after Next");
                assertCarouselDemoGeometry(compact, "compact carousel after Next");

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "carousel-after-next-action.png"
                ));
                assertSnapshotHasVisibleContent(image, "Carousel after next action");
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3Carousel multiBrowse = multiBrowseReference.get();
                if (multiBrowse != null) {
                    multiBrowse.setAnimatedScroll(true);
                    M3MotionSettings.clearAnimationsEnabled(multiBrowse);
                }
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Dialogs demo renders launchers, inline panes, actions, and scrollable content.
    @Test
    void dialogsPageRendersInlinePanesActionsAndScrollableContent() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> showPageWhenSidebarSelectionSettled(
                    appReference, sceneReference, "Dialogs", scene -> {
                    }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Dialogs");
                assertVisibleText(root, "Open basic", "Dialogs");
                assertVisibleText(root, "Open settings", "Dialogs");
                assertVisibleText(root, "Open destructive", "Dialogs");
                assertVisibleText(root, "Cancel", "Dialogs");
                assertVisibleText(root, "OK", "Dialogs");
                assertVisibleText(root, "Apply", "Dialogs");

                List<M3DialogPane> panes = visibleNodesOfType(root, M3DialogPane.class);
                assertEquals(3, panes.size(), "Dialogs page should render basic, form, and scrollable panes");
                assertEquals(1, visibleNodesOfType(root, M3TextInputLayout.class).stream()
                                .filter(layout -> nearestAncestorOfType(layout, M3DialogPane.class) != null)
                                .count(),
                        "Dialogs page should include one form-like text input layout");
                assertEquals(1, visibleNodesOfType(root, M3Switch.class).stream()
                                .filter(toggle -> nearestAncestorOfType(toggle, M3DialogPane.class) != null)
                                .count(),
                        "Dialogs page should include one settings switch");
                assertEquals(1, visibleNodesOfType(root, M3CheckBox.class).stream()
                                .filter(checkbox -> nearestAncestorOfType(checkbox, M3DialogPane.class) != null)
                                .count(),
                        "Dialogs page should include one settings checkbox");
                assertEquals(1, visibleNodesOfType(root, ScrollPane.class).stream()
                                .filter(scrollPane -> scrollPane.getStyleClass().contains("demo-dialog-scroll-pane"))
                                .count(),
                        "Dialogs page should include one scrollable dialog body");

                for (M3DialogPane pane : panes) {
                    assertDialogPaneDemoGeometry(scene, pane);
                }

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "dialogs-inline-panes.png"
                ));
                assertSnapshotHasVisibleContent(image, "Dialogs inline panes");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(root, "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertVisibleText(root, "Close", "Dialogs");
                assertTrue(panes.stream()
                                .filter(pane -> pane.getContent() instanceof ScrollPane)
                                .anyMatch(pane -> !isOutsideSceneViewport(
                                        pane,
                                        pane.localToScene(pane.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Dialogs page should render the scrollable dialog pane in a captured viewport");

                WritableImage scrollableImage = snapshot(scene);
                writeVisualSnapshot(scrollableImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "dialogs-scrollable-content.png"
                ));
                assertSnapshotHasVisibleContent(scrollableImage, "Dialogs scrollable content");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Banners demo renders icons, actions, passive, narrow, and RTL states safely.
    @Test
    void bannersPageRendersIconsActionsPassiveNarrowAndRtlStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> showPageWhenSidebarSelectionSettled(
                    appReference, sceneReference, "Banners", scene -> {
                    }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Banners");
                assertVisibleText(root, "Learn", "Banners");
                assertVisibleText(root, "Dismiss", "Banners");
                assertVisibleText(root, "Details", "Banners");
                assertVisibleText(root, "Secondary", "Banners");

                List<M3Banner> banners = visibleNodesOfType(root, M3Banner.class);
                assertEquals(6, banners.size(), "Banners page should render six banner states");
                assertEquals(4, visibleNodesWithStyle(root, M3Banner.ICON_STYLE_CLASS).size(),
                        "Banners page should render four visible icon slots");
                long actionButtonCount = visibleNodesOfType(root, M3Button.class).stream()
                        .filter(button -> nearestAncestorOfType(button, M3Banner.class) != null)
                        .count();
                assertEquals(8, actionButtonCount, "Banners page should render all action buttons");
                assertTrue(banners.stream().anyMatch(banner -> banner.getActions().isEmpty()),
                        "Banners page should include a passive banner");
                assertTrue(banners.stream().anyMatch(banner -> banner.getPrefWidth() <= 420.0),
                        "Banners page should include a narrow wrapping banner");
                assertTrue(banners.stream().anyMatch(banner ->
                                banner.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT),
                        "Banners page should include a right-to-left banner");

                for (M3Banner banner : banners) {
                    assertBannerDemoGeometry(banner);
                }

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "banners-states.png"
                ));
                assertSnapshotHasVisibleContent(image, "Banners states");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(root, "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                assertTrue(banners.stream()
                                .filter(banner -> banner.getPrefWidth() <= 420.0)
                                .anyMatch(banner -> !isOutsideSceneViewport(
                                        banner,
                                        banner.localToScene(banner.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Banners page should render the narrow banner in a captured viewport");
                assertTrue(banners.stream()
                                .filter(banner ->
                                        banner.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT)
                                .anyMatch(banner -> !isOutsideSceneViewport(
                                        banner,
                                        banner.localToScene(banner.getLayoutBounds()),
                                        sceneBounds
                                )),
                        "Banners page should render the right-to-left banner in a captured viewport");

                WritableImage responsiveImage = snapshot(scene);
                writeVisualSnapshot(responsiveImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "banners-responsive-rtl.png"
                ));
                assertSnapshotHasVisibleContent(responsiveImage, "Banners responsive and RTL");
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that sheet and snackbar demo pages render stable overlay and feedback states.
    @Test
    void sheetsAndSnackbarsDemoPagesRenderSurfaceAndHostStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(950.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                assertOverlayDemoPage(
                        appReference,
                        sceneReference,
                        "Bottom Sheets",
                        M3FXDemoVisualSmokeTest::assertBottomSheetsPageVisualState
                );
                assertOverlayDemoPage(
                        appReference,
                        sceneReference,
                        "Side Sheets",
                        M3FXDemoVisualSmokeTest::assertSideSheetsPageVisualState
                );
                assertOverlayDemoPage(
                        appReference,
                        sceneReference,
                        "Snackbars",
                        M3FXDemoVisualSmokeTest::assertSnackbarsPageVisualState
                );
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Tooltips page opens compact plain, long, and rich popup surfaces.
    @Test
    void tooltipsPageRendersPlainLongAndRichPopupSurfaces() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                showPageWhenSidebarSelectionSettled(
                        appReference,
                        sceneReference,
                        "Tooltips",
                        M3FXDemoVisualSmokeTest::resetDemoPageScroll,
                        () -> {
                            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                            assertTooltipsPageVisualState(scene);
                            assertDemoPageVisualGeometry(scene, "Tooltips");
                            writePageSnapshot(
                                    scene,
                                    "tooltips-owners.png",
                                    "Tooltips owners"
                            );
                        }
                );
                verifyTooltipPopupSurface(
                        sceneReference,
                        "Hover me",
                        "Tooltip",
                        "plain",
                        owner -> {
                            M3Tooltip tooltip = M3Tooltip.install(owner, "Tooltip");
                            tooltip.setShowDelay(Duration.ZERO);
                            tooltip.setHideDelay(Duration.ZERO);
                            tooltip.setShowDuration(Duration.INDEFINITE);
                            return tooltip;
                        },
                        popupRoot -> assertPlainTooltipPopupSize(popupRoot, "plain tooltip")
                );
                verifyTooltipPopupSurface(
                        sceneReference,
                        "Long tooltip",
                        "Use tooltips for brief contextual labels when a control needs clarification.",
                        "long",
                        owner -> {
                            M3Tooltip tooltip = new M3Tooltip(
                                    "Use tooltips for brief contextual labels when a control needs clarification."
                            );
                            tooltip.setPrefWidth(260.0);
                            tooltip.setShowDelay(Duration.ZERO);
                            tooltip.setHideDelay(Duration.ZERO);
                            tooltip.setShowDuration(Duration.INDEFINITE);
                            M3Tooltip.install(owner, tooltip);
                            return tooltip;
                        },
                        popupRoot -> assertLongTooltipPopupSize(popupRoot, "long tooltip")
                );
                verifyTooltipPopupSurface(
                        sceneReference,
                        "Rich action",
                        "Generated theme",
                        "rich-action",
                        owner -> {
                            M3Button action = new M3Button("Open");
                            M3RichTooltip tooltip = M3RichTooltip.install(
                                    owner,
                                    "Generated theme",
                                    "The tooltip can inherit the owning scene theme and expose action nodes in the content surface.",
                                    action
                            );
                            tooltip.setShowDelay(Duration.ZERO);
                            tooltip.setHideDelay(Duration.ZERO);
                            tooltip.setShowDuration(Duration.INDEFINITE);
                            return tooltip;
                        },
                        popupRoot -> {
                            assertVisibleTextContains(
                                    popupRoot,
                                    "The tooltip can inherit the owning scene theme and expose action nodes in the content surface.",
                                    "rich action tooltip"
                            );
                            assertRichTooltipPopupSize(popupRoot, "rich action tooltip");
                            assertRichTooltipActionInsidePopup(popupRoot);
                        }
                );
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that utility and foundation demo pages expose concrete controls with stable rendered geometry.
    @Test
    void foundationDemoPagesRenderUtilityComponentsWithConcreteGeometry() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(950.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Badges",
                        M3FXDemoVisualSmokeTest::assertBadgesPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Avatars",
                        M3FXDemoVisualSmokeTest::assertAvatarsPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Dividers",
                        M3FXDemoVisualSmokeTest::assertDividersPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Surfaces",
                        M3FXDemoVisualSmokeTest::assertSurfacesPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Scrims",
                        M3FXDemoVisualSmokeTest::assertScrimsPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Forms",
                        M3FXDemoVisualSmokeTest::assertFormsPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Typography",
                        M3FXDemoVisualSmokeTest::assertTypographyPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Icons",
                        M3FXDemoVisualSmokeTest::assertIconsPageVisualState
                );
                assertFoundationDemoPage(
                        appReference,
                        sceneReference,
                        "Tooltips",
                        M3FXDemoVisualSmokeTest::assertTooltipsPageVisualState
                );
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Menus page renders inline selection states and a compact nested popup stack.
    @Test
    void menusPageRendersInlineSelectionAndNestedPopupStack() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> ownerPopupSnapshotReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> subMenuSnapshotReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> {
                showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Menus", scene -> {
                }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    Parent root = scene.getRoot();
                    assertCurrentPageTitle(scene, "Menus");
                    assertVisibleText(root, "Menu Button", "Menus");
                    assertVisibleText(root, "Inline Menus", "Menus");
                    assertDemoVectorIcons(root, "Menus", 10);

                    List<M3Menu> inlineMenus = visibleNodesOfType(root, M3Menu.class);
                    assertEquals(3, inlineMenus.size(), "Menus page should render three inline menu surfaces");

                    M3Menu selectedMenu = requireMenuContainingText(inlineMenus, "Selected item");
                    assertEquals("Selected item",
                            Objects.requireNonNull(selectedMenu.getSelectedItem(), "selected item").getHeadlineText());
                    assertEquals(1, selectedMenu.getSelectedItems().size(), "selected menu should be single-selection");

                    M3Menu multiSelectMenu = requireMenuContainingText(inlineMenus, "Icons");
                    assertEquals(2, multiSelectMenu.getSelectedItems().size(),
                            "multi-select menu should render two selected rows");
                    assertTrue(menuHasSelectedItem(multiSelectMenu, "Icons"),
                            "multi-select menu should select Icons");
                    assertTrue(menuHasSelectedItem(multiSelectMenu, "Badges"),
                            "multi-select menu should select Badges");

                    WritableImage image = snapshot(scene);
                    for (M3Menu menu : inlineMenus) {
                        assertMenuSurfaceGeometry(menu, image, "Menus inline surface");
                        assertDemoVectorIcons(menu, "Menus inline surface", 1);
                    }

                    writeVisualSnapshot(image, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "menus-inline-states.png"
                    ));
                    assertSnapshotHasVisibleContent(image, "Menus inline states");

                    M3MenuButton menuButton = Objects.requireNonNull(
                            firstVisibleMenuButtonWithText(root, "Open menu"),
                            "menu button"
                    );
                    M3MotionSettings.setAnimationsEnabled(menuButton, false);
                    menuButtonReference.set(menuButton);
                });

                runOnFxThreadWhenNodeSnapshotStable(() -> {
                    @Nullable M3MenuButton menuButton = menuButtonReference.get();
                    return menuButton == null ? null : menuButton.getMenu();
                }, ownerPopupSnapshotReference, () -> {
                    @Nullable M3MenuButton menuButton = menuButtonReference.get();
                    return menuButton != null && menuButton.isShowing() && popupRootSettled(menuButton.getMenu());
                }, "Menus owner popup settled frame", () -> {
                    M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
                    menuButton.showMenu();
                }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
                    M3Menu ownerMenu = menuButton.getMenu();
                    layoutPopupRoot(ownerMenu);
                    assertTrue(menuButton.isShowing(), "menu button popup should be showing");
                    assertPopupThemeContext(scene.getRoot(), ownerMenu, "menu button");
                    assertMenuSurfaceGeometry(ownerMenu, null, "Menus owner popup");
                    assertDemoVectorIcons(ownerMenu, "Menus popup", 3);
                    assertNotNull(requireMenuItemWithText(ownerMenu, "Duplicate"), "popup duplicate item");
                    assertNotNull(requireMenuItemWithText(ownerMenu, "Rename"), "popup rename item");
                    assertNotNull(requireMenuItemWithText(ownerMenu, "Delete"), "popup delete item");

                    WritableImage ownerPopupImage = Objects.requireNonNull(
                            ownerPopupSnapshotReference.get(),
                            "Menus owner popup snapshot"
                    );
                    writeVisualSnapshot(ownerPopupImage, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "menus-popup-owner.png"
                    ));
                    assertSnapshotHasVisibleContent(ownerPopupImage, "Menus owner popup");

                    M3SubMenuItem subMenuItem = Objects.requireNonNull(
                            firstVisibleSubMenuItemWithText(ownerMenu, "Move to"),
                            "submenu item"
                    );
                    M3MotionSettings.setAnimationsEnabled(subMenuItem, false);
                    subMenuItem.showSubMenu();
                    layoutPopupRoot(subMenuItem.getSubMenu());
                    subMenuItemReference.set(subMenuItem);
                });

                runOnFxThreadWhenNodeSnapshotStable(() -> {
                    @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                    return subMenuItem == null ? null : subMenuItem.getSubMenu();
                }, subMenuSnapshotReference, () -> {
                    @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                    return subMenuItem != null
                            && subMenuItem.isSubMenuShowing()
                            && popupRootSettled(subMenuItem.getSubMenu());
                }, "Menus nested submenu settled frame", () -> {
                }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
                    M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
                    M3Menu ownerMenu = menuButton.getMenu();
                    M3Menu subMenu = subMenuItem.getSubMenu();
                    layoutPopupRoot(ownerMenu);
                    layoutPopupRoot(subMenu);
                    assertTrue(menuButton.isShowing(), "owner popup should remain showing while submenu is open");
                    assertTrue(subMenuItem.isSubMenuShowing(), "nested submenu should be showing");
                    assertPopupThemeContext(scene.getRoot(), subMenu, "nested submenu");
                    assertMenuSurfaceGeometry(ownerMenu, null, "Menus owner popup with submenu");
                    assertMenuSurfaceGeometry(subMenu, null, "Menus nested submenu");
                    assertPopupStackSideBySide(ownerMenu, subMenu);
                    assertDemoVectorIcons(subMenu, "Menus nested submenu", 2);
                    assertNotNull(requireMenuItemWithText(subMenu, "Archive"), "submenu archive item");
                    assertNotNull(requireMenuItemWithText(subMenu, "Inbox"), "submenu inbox item");

                    WritableImage subMenuImage = Objects.requireNonNull(
                            subMenuSnapshotReference.get(),
                            "Menus nested submenu snapshot"
                    );
                    writeVisualSnapshot(subMenuImage, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "menus-popup-submenu.png"
                    ));
                    assertSnapshotHasVisibleContent(subMenuImage, "Menus nested submenu");
                });
            });
        } finally {
            runOnFxThread(() -> {
                @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                if (subMenuItem != null) {
                    subMenuItem.hideSubMenu();
                    M3MotionSettings.clearAnimationsEnabled(subMenuItem);
                }
                @Nullable M3MenuButton menuButton = menuButtonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                    M3MotionSettings.clearAnimationsEnabled(menuButton);
                }
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that the Text Fields page renders stable labels, notches, counters, and adornment geometry.
    @Test
    void textFieldsPageRendersStableLabelAdornmentAndCounterGeometry() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(950.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> showPageWhenSidebarSelectionSettled(
                    appReference, sceneReference, "Text Fields", scene -> {
                    }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                Parent root = scene.getRoot();
                assertCurrentPageTitle(scene, "Text Fields");
                assertVisibleText(root, "Filled", "Text Fields");
                assertVisibleText(root, "Outlined", "Text Fields");
                assertVisibleText(root, "Validation", "Text Fields");
                assertVisibleText(root, "Error", "Text Fields");
                assertVisibleText(root, "Text Areas", "Text Fields");
                assertVisibleText(root, "Right-to-left", "Text Fields");

                List<M3TextInputLayout> layouts = visibleNodesOfType(root, M3TextInputLayout.class);
                assertTrue(layouts.size() >= 15,
                        () -> "Text Fields page should render many text input layouts, found " + layouts.size());
                for (M3TextInputLayout layout : layouts) {
                    assertTextInputLayoutContainerGeometry(layout, "Text Fields");
                }

                M3TextInputLayout filledTextLayout = requireTextInputLayout(
                        layouts,
                        "support@example.com",
                        "Filled with text"
                );
                M3TextInputLayout outlinedTextLayout = requireTextInputLayout(layouts, "M3FX", "Outlined with text");
                M3TextInputLayout passwordLayout = requireTextInputLayout(layouts, "", "Password");
                M3TextInputLayout validatedEmailLayout =
                        requireTextInputLayout(layouts, "support", "Validated email");
                M3TextInputLayout filledErrorLayout = requireTextInputLayout(layouts, "Invalid value", "Filled error");
                M3TextInputLayout outlinedErrorLayout = requireTextInputLayout(layouts, "", "Outlined error");
                M3TextInputLayout outlinedAreaLayout = requireTextInputLayout(
                        layouts,
                        "Material text areas share field colors but keep multi-line height tokens.",
                        "Outlined text area"
                );
                M3TextInputLayout rtlFilledLayout = requireTextInputLayout(layouts, "rtl@example.com", "RTL filled");
                M3TextInputLayout rtlOutlinedLayout = requireTextInputLayout(layouts, "M3FX RTL", "RTL outlined");
                M3TextInputLayout rtlPasswordLayout = requireTextInputLayout(layouts, "", "RTL password");

                assertTextInputVariant(filledTextLayout, M3TextInputVariant.FILLED, "filled populated field");
                assertTextInputVariant(outlinedTextLayout, M3TextInputVariant.OUTLINED, "outlined populated field");
                assertTextInputVariant(passwordLayout, M3TextInputVariant.OUTLINED, "password field");
                assertTextInputVariant(outlinedAreaLayout, M3TextInputVariant.OUTLINED, "outlined text area");
                assertTextInputVariant(rtlFilledLayout, M3TextInputVariant.FILLED, "RTL filled field");
                assertTextInputVariant(rtlOutlinedLayout, M3TextInputVariant.OUTLINED, "RTL outlined field");
                assertTextInputVariant(rtlPasswordLayout, M3TextInputVariant.OUTLINED, "RTL password field");
                assertOutlinedFloatingLabelGeometry(outlinedTextLayout, "outlined populated field");
                assertTextInputCounterText(filledTextLayout, "19 / 32", "filled populated field");
                assertTextInputCounterText(outlinedTextLayout, "4 / 24", "outlined populated field");
                assertTextInputCounterText(rtlFilledLayout, "15 / 32", "RTL filled field");
                assertTextInputCounterText(rtlOutlinedLayout, "8 / 24", "RTL outlined field");
                assertTextInputTrailingActionGeometry(passwordLayout, "password field");
                assertTextInputTrailingActionGeometry(rtlPasswordLayout, "RTL password field");
                assertTextInputClearButtonGeometry(filledTextLayout, "filled populated field");
                assertTextInputClearButtonGeometry(rtlFilledLayout, "RTL filled field");
                assertRightToLeftTextInputLayoutGeometry(rtlFilledLayout, "RTL filled field");
                assertRightToLeftTextInputLayoutGeometry(rtlOutlinedLayout, "RTL outlined field");
                assertRightToLeftTextInputLayoutGeometry(rtlPasswordLayout, "RTL password field");
                assertDemoValidationFeedbackRemainsStableOnEdits(validatedEmailLayout);
                assertTextInputErrorState(filledErrorLayout, "filled error field");
                assertTextInputErrorState(outlinedErrorLayout, "outlined error field");
                assertSingleLineTextInputsHaveVerticalRoom(scene, "Text Fields");

                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "text-fields-layout-geometry.png"
                ));
                assertSnapshotHasVisibleContent(image, "Text Fields layout geometry");
                assertOutlinedFloatingLabelBackgroundMatchesSurrounding(
                        image,
                        outlinedTextLayout,
                        "outlined populated field"
                );

                WritableImage validationImage = snapshot(scene);
                writeVisualSnapshot(validationImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "text-fields-validation-stability.png"
                ));
                assertSnapshotHasVisibleContent(validationImage, "Text Fields validation stability");

                ScrollPane pageScrollPane = assertInstanceOf(
                        ScrollPane.class,
                        requireVisibleStyledDescendant(root, "demo-scroll-pane", "demo page scroll pane")
                );
                pageScrollPane.setVvalue(1.0);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getLayoutBounds());
                for (M3TextInputLayout rtlLayout : List.of(rtlFilledLayout, rtlOutlinedLayout, rtlPasswordLayout)) {
                    Bounds rtlBounds = rtlLayout.localToScene(rtlLayout.getLayoutBounds());
                    assertFalse(isOutsideSceneViewport(rtlLayout, rtlBounds, sceneBounds),
                            () -> "Text Fields RTL layout should be visible in the scrolled capture: " + rtlBounds);
                }

                WritableImage rtlImage = snapshot(scene);
                writeVisualSnapshot(rtlImage, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "text-fields-rtl-geometry.png"
                ));
                assertSnapshotHasVisibleContent(rtlImage, "Text Fields RTL geometry");
                assertOutlinedFloatingLabelBackgroundMatchesSurrounding(
                        rtlImage,
                        rtlOutlinedLayout,
                        "RTL outlined field"
                );
            }));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that selection control pages render all advertised states with stable indicator geometry.
    @Test
    void selectionPagesRenderStableStatesAndGeometry() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(950.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Checkboxes", scene -> {
            }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertCheckboxesPageVisualState(scene);
                    writePageSnapshot(scene, "selection-checkboxes-states.png", "Checkboxes states");
            });

            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Radio Buttons", scene -> {
            }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertRadioButtonsPageVisualState(scene);
                    writePageSnapshot(scene, "selection-radio-buttons-states.png", "Radio Buttons states");
            });

            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Switches", scene -> {
            }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertSwitchesPageVisualState(scene);
                    writePageSnapshot(scene, "selection-switches-states.png", "Switches states");
            });

            showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Sliders", scene -> {
            }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertSlidersPageVisualState(scene);
                    writePageSnapshot(scene, "selection-sliders-geometry.png", "Sliders geometry");
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that sequential page changes settle to one visually selected sidebar destination.
    @Test
    void sidebarSelectionSettlesToSingleVisibleDestinationAfterPageChanges() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(950.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            String[] pageTitles = {"Checkboxes", "Radio Buttons", "Switches", "Sliders", "Text Fields"};
            for (String pageTitle : pageTitles) {
                showPageWhenSidebarSelectionSettled(appReference, sceneReference, pageTitle, scene -> {
                }, () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertCurrentPageTitle(scene, pageTitle);
                    assertSidebarSelectionMatchesCurrentPage(app, pageTitle);
                    assertSidebarVisualSelectionSettled(app, scene, pageTitle);
                });
            }

            runOnFxThread(() -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                WritableImage image = snapshot(scene);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-demo-visual",
                        "sidebar-selection-settled.png"
                ));
                assertSnapshotHasVisibleContent(image, "Sidebar selection settled");
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that interactive demo icon slots use SVG graphics instead of text placeholder icons.
    @Test
    void interactiveDemoIconSlotsUseVectorGraphics() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThread(() -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                for (Map.Entry<String, Integer> entry : DEMO_VECTOR_ICON_PAGE_MINIMUMS.entrySet()) {
                    String pageTitle = entry.getKey();
                    app.showPageForTesting(pageTitle);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                    assertDemoVectorIcons(scene.getRoot(), pageTitle, entry.getValue());
                }
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that every registered demo page renders visible content without obvious clipping or off-center glyphs.
    @Test
    void allDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertTrue(app.demoPageTitlesForTesting().size() > 20);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            @Unmodifiable List<String> pageTitles =
                    Objects.requireNonNull(appReference.get(), "app").demoPageTitlesForTesting();
            for (String pageTitle : pageTitles) {
                showPageWhenSidebarSelectionSettled(
                        appReference,
                        sceneReference,
                        pageTitle,
                        M3FXDemoVisualSmokeTest::resetDemoPageScroll,
                        () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertCurrentPageTitle(scene, pageTitle);
                    assertSidebarSelectionMatchesCurrentPage(app, pageTitle);

                    WritableImage image = snapshot(scene);
                    writeVisualSnapshot(image, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "demo-" + snapshotFileName(pageTitle) + ".png"
                    ));
                    assertSnapshotHasVisibleContent(image, pageTitle);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                    assertDemoPageSpecificVisualState(scene, pageTitle);
                });
            }
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that every demo page stays readable under the dark expressive theme combination.
    @Test
    void darkExpressiveDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(900.0);
            app.setDarkExpressiveThemeForTesting();

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertEquals(app.demoPageTitlesForTesting(), DARK_EXPRESSIVE_VISUAL_PAGES);
            assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            for (String pageTitle : DARK_EXPRESSIVE_VISUAL_PAGES) {
                showPageWhenSidebarSelectionSettled(
                        appReference,
                        sceneReference,
                        pageTitle,
                        M3FXDemoVisualSmokeTest::resetDemoPageScroll,
                        () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertCurrentPageTitle(scene, pageTitle);
                    assertSidebarSelectionMatchesCurrentPage(app, pageTitle);
                    assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                    assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

                    WritableImage image = snapshot(scene);
                    writeVisualSnapshot(image, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "demo-dark-expressive-" + snapshotFileName(pageTitle) + ".png"
                    ));
                    assertSnapshotHasVisibleContent(image, pageTitle);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                    assertDemoPageSpecificVisualState(scene, pageTitle);
                });
            }
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that every demo page keeps visible content valid when the scene is mirrored for RTL locales.
    @Test
    void rightToLeftDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(900.0);

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertEquals(app.demoPageTitlesForTesting(), RTL_VISUAL_PAGES);
            scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            for (String pageTitle : RTL_VISUAL_PAGES) {
                showPageWhenSidebarSelectionSettled(appReference, sceneReference, pageTitle, scene ->
                        scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT), () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertEquals(NodeOrientation.RIGHT_TO_LEFT, scene.getRoot().getEffectiveNodeOrientation());
                    assertCurrentPageTitle(scene, pageTitle);
                    assertSidebarSelectionMatchesCurrentPage(app, pageTitle);

                    WritableImage image = snapshot(scene);
                    writeVisualSnapshot(image, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "demo-rtl-" + snapshotFileName(pageTitle) + ".png"
                    ));
                    assertSnapshotHasVisibleContent(image, pageTitle);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                    assertDemoPageSpecificVisualState(scene, pageTitle);
                    if ("App Bars".equals(pageTitle)) {
                        for (M3TopAppBar appBar : visibleNodesOfType(scene.getRoot(), M3TopAppBar.class)) {
                            assertTopAppBarSlotGeometry(appBar);
                        }
                    }
                });
            }
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that real mouse and focus interactions produce visible feedback in the demo window.
    @Test
    void interactiveDemoStatesProduceVisibleFeedback() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyButtonMouseFeedback(appReference, sceneReference);
            verifyButtonRippleReleaseAnimation(appReference, sceneReference);
            verifyTextFieldFocusFeedback(appReference, sceneReference);
            verifySidebarMouseFeedback(appReference, sceneReference);
            verifySidebarRippleReleaseAnimation(appReference, sceneReference);
            verifyIconToggleButtonMouseFeedback(appReference, sceneReference);
            verifyIconToggleButtonRippleReleaseAnimation(appReference, sceneReference);
            verifySwitchSelectionAnimation(appReference, sceneReference);
            verifyDisabledAnimationInteractionFeedback(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that popup-backed demo controls expose visible enter and exit motion frames.
    @Test
    void popupDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifySplitButtonPopupAnimation(appReference, sceneReference);
            verifyDatePickerFieldPopupAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that nested popup menu stacks expose visible motion and sane screen placement.
    @Test
    void nestedMenuPopupStackProducesDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            DemoFxTestUtils.assertNoCssWarnings(() -> verifyNestedMenuPopupStackAnimation(
                    appReference,
                    sceneReference
            ));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that navigation selection and drawer disclosure animations expose visible intermediate frames.
    @Test
    void navigationDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyNavigationItemSelectionAnimation(
                    appReference,
                    sceneReference,
                    "Navigation",
                    "navigation-bar-selection"
            );
            verifyNavigationItemSelectionAnimation(
                    appReference,
                    sceneReference,
                    "Navigation Rail",
                    "navigation-rail-selection"
            );
            verifySidebarDrawerGroupExpansionAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that sheet visibility changes expose animated intermediate frames in the demo.
    @Test
    void sheetDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyBottomSheetVisibilityAnimation(appReference, sceneReference);
            verifySideSheetVisibilityAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that overlay surfaces render compactly and expose their expected interactive motion.
    @Test
    void overlayDemoSurfacesRenderAndAnimate() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifySnackbarHostAnimation(appReference, sceneReference);
            verifyFabMenuExpansionAnimation(appReference, sceneReference);
            verifyRichTooltipInteractiveLifetime(appReference, sceneReference);
            verifyDialogPopupSurface(sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that animated progress indicators visibly advance between real rendered frames.
    @Test
    void progressDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> pageReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> baselineFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> continuedBaselineFrameReference = new AtomicReference<>();
        List<WritableImage> animationFrames = new ArrayList<>(
                CONTINUOUS_ANIMATION_FRAME_COUNT * CONTINUOUS_ANIMATION_BATCH_COUNT
        );
        List<WritableImage> continuedAnimationFrames = new ArrayList<>(CONTINUOUS_ANIMATION_FRAME_COUNT);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThreadWhenNodeAreaAdvances(
                    pageReference,
                    sceneReference,
                    baselineFrameReference,
                    animationFrames,
                    CONTINUOUS_ANIMATION_FRAME_COUNT,
                    () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting("Progress");
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                        pageReference.set(Objects.requireNonNull(firstVisibleNodeWithStyle(
                                scene.getRoot(),
                                "demo-page"
                        ), "demo page"));
                        baselineFrameReference.set(snapshot(scene));
                    },
                    () -> writeAnimationFrameSnapshots(animationFrames, "progress")
            );

            Node progressPage = Objects.requireNonNull(pageReference.get(), "progress page");
            runOnFxThread(() -> assertNodeAreaFramesAdvance(
                    progressPage,
                    animationFrames,
                    "progress animation frames"
            ));
            continuedBaselineFrameReference.set(lastFrame(animationFrames, "progress animation frames"));

            runOnFxThreadWhenNodeAreaAdvances(
                    pageReference,
                    sceneReference,
                    continuedBaselineFrameReference,
                    continuedAnimationFrames,
                    CONTINUOUS_ANIMATION_FRAME_COUNT,
                    () -> {
                    },
                    () -> writeAnimationFrameSnapshots(continuedAnimationFrames, "progress-continued")
            );

            runOnFxThread(() -> assertNodeAreaFramesAdvance(
                    progressPage,
                    continuedAnimationFrames,
                    "progress continued animation frames"
            ));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that animated loading indicators visibly morph between real rendered frames.
    @Test
    void loadingIndicatorDemoAnimationProducesDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> indicatorReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> baselineFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> continuedBaselineFrameReference = new AtomicReference<>();
        List<WritableImage> animationFrames = new ArrayList<>(
                CONTINUOUS_ANIMATION_FRAME_COUNT * CONTINUOUS_ANIMATION_BATCH_COUNT
        );
        List<WritableImage> continuedAnimationFrames = new ArrayList<>(CONTINUOUS_ANIMATION_FRAME_COUNT);

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThreadWhenNodeAreaAdvances(
                    indicatorReference,
                    sceneReference,
                    baselineFrameReference,
                    animationFrames,
                    CONTINUOUS_ANIMATION_FRAME_COUNT,
                    () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting("Loading Indicator");
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                        indicatorReference.set(Objects.requireNonNull(firstVisibleNodeWithStyle(
                                scene.getRoot(),
                                M3LoadingIndicator.STYLE_CLASS
                        ), "loading indicator"));
                        baselineFrameReference.set(snapshot(scene));
                    },
                    () -> writeAnimationFrameSnapshots(animationFrames, "loading-indicator")
            );

            Node loadingIndicator = Objects.requireNonNull(indicatorReference.get(), "loading indicator");
            runOnFxThread(() -> assertNodeAreaFramesAdvance(
                    loadingIndicator,
                    animationFrames,
                    "loading indicator animation frames"
            ));
            continuedBaselineFrameReference.set(lastFrame(animationFrames, "loading indicator animation frames"));

            runOnFxThreadWhenNodeAreaAdvances(
                    indicatorReference,
                    sceneReference,
                    continuedBaselineFrameReference,
                    continuedAnimationFrames,
                    CONTINUOUS_ANIMATION_FRAME_COUNT,
                    () -> {
                    },
                    () -> writeAnimationFrameSnapshots(continuedAnimationFrames, "loading-indicator-continued")
            );

            runOnFxThread(() -> assertNodeAreaFramesAdvance(
                    loadingIndicator,
                    continuedAnimationFrames,
                    "loading indicator continued animation frames"
            ));
            animationFrames.addAll(continuedAnimationFrames);
            runOnFxThread(() -> assertLoadingIndicatorFramesRemainCentered(
                    loadingIndicator,
                    animationFrames,
                    "loading indicator animation"
            ));
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that disabled demo animations keep reduced indeterminate progress and loading motion alive.
    @Test
    void reducedMotionProgressAndLoadingDemosKeepBasicIndeterminateMotion() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyReducedMotionPageKeepsBasicIndeterminateMotion(
                    appReference,
                    sceneReference,
                    "Progress",
                    scene -> firstVisibleIndeterminateProgressBar(scene.getRoot()),
                    "progress-reduced-motion"
            );
            verifyReducedMotionPageKeepsBasicIndeterminateMotion(
                    appReference,
                    sceneReference,
                    "Loading Indicator",
                    scene -> Objects.requireNonNull(firstVisibleNodeWithStyle(
                            scene.getRoot(),
                            M3LoadingIndicator.STYLE_CLASS
                    ), "loading indicator"),
                    "loading-indicator-reduced-motion"
            );
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that one demo page keeps a basic indeterminate animation while full motion is disabled.
    private static void verifyReducedMotionPageKeepsBasicIndeterminateMotion(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            Function<Scene, Node> targetFactory,
            String snapshotName
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> baselineFrameReference = new AtomicReference<>();
        List<WritableImage> animationFrames = new ArrayList<>(CONTINUOUS_ANIMATION_FRAME_COUNT);

        try {
            runOnFxThreadWhenNodeAreaAdvances(
                    targetReference,
                    sceneReference,
                    baselineFrameReference,
                    animationFrames,
                    CONTINUOUS_ANIMATION_FRAME_COUNT,
                    () -> {
                        M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        app.showPageForTesting(pageTitle);
                        M3MotionSettings.setAnimationsEnabled(scene.getRoot(), false);
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                        Node target = targetFactory.apply(scene);
                        targetReference.set(target);
                        baselineFrameReference.set(snapshot(scene));
                    },
                    () -> writeAnimationFrameSnapshots(animationFrames, snapshotName)
            );

            Node target = Objects.requireNonNull(targetReference.get(), snapshotName + " target");
            runOnFxThread(() -> assertNodeAreaFramesAdvance(
                    target,
                    animationFrames,
                    snapshotName + " reduced animation frames"
            ));
        } finally {
            runOnFxThread(() -> {
                Scene scene = sceneReference.get();
                if (scene != null) {
                    M3MotionSettings.clearAnimationsEnabled(scene.getRoot());
                }
            });
        }
    }

    /// Returns the first visible indeterminate progress bar in a page.
    private static M3ProgressBar firstVisibleIndeterminateProgressBar(Node root) {
        return visibleNodesOfType(root, M3ProgressBar.class).stream()
                .filter(M3ProgressBar::isIndeterminate)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No visible indeterminate progress bar"));
    }

    /// Verifies hover and pressed feedback on a regular demo button.
    private static void verifyButtonMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, hoverReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Filled"
            ), "button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal button snapshot"),
                    "button",
                    "normal"
            );
            applyHoverPseudoState(target);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                    "button",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hoverReference, sceneReference, pressedReference, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed button snapshot"),
                    "button",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearHoverPseudoState(target);
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(normalReference.get(), "normal button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                "button hover"
        );
        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed button snapshot"),
                "button pressed"
        );
    }

    /// Verifies that button ripple release remains visible for an intermediate fade-out frame.
    private static void verifyButtonRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Buttons",
                "button-ripple",
                "button",
                root -> firstVisibleButtonWithText(root, "Filled")
        );
    }

    /// Verifies that toggle icon button ripple release remains visible for an intermediate fade-out frame.
    private static void verifyIconToggleButtonRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Icon Buttons",
                "icon-toggle-button-ripple",
                "toggle icon button",
                root -> firstVisibleNodeWithStyle(root, M3IconToggleButton.STYLE_CLASS)
        );
    }

    /// Verifies that sidebar navigation item ripple release remains visible for an intermediate fade-out frame.
    private static void verifySidebarRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Buttons",
                "sidebar-ripple",
                "sidebar item",
                root -> firstVisibleNodeWithStyle(root, "demo-sidebar-child-item")
        );
    }

    /// Verifies that a target's ripple release includes a visible intermediate fade-out frame.
    private static void verifyRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            String snapshotName,
            String targetName,
            Function<Node, @Nullable Node> targetLookup
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> rippleReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseStartOpacityReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> releaseStartReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseOpacityReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> releaseReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> settledOpacityReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, pressedReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting(pageTitle);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(targetLookup.apply(scene.getRoot()), targetName);
            M3MotionSettings.setMotionScheme(target, visualRippleMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal ripple target snapshot"),
                    snapshotName,
                    "normal"
            );
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed ripple target snapshot"),
                    snapshotName,
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), targetName);
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            Node ripple = Objects.requireNonNull(
                    target.lookup("." + RIPPLE_STYLE_CLASS),
                    targetName + " ripple node"
            );
            rippleReference.set(ripple);
            releaseStartOpacityReference.set(ripple.getOpacity());
            releaseStartReference.set(snapshot(scene));
        });

        runOnFxThreadWhen(() -> {
            @Nullable Node target = targetReference.get();
            @Nullable Node ripple = rippleReference.get();
            @Nullable Double releaseStartOpacity = releaseStartOpacityReference.get();
            @Nullable WritableImage releaseStart = releaseStartReference.get();
            @Nullable Scene scene = sceneReference.get();
            if (target == null
                    || ripple == null
                    || releaseStartOpacity == null
                    || ripple.getOpacity() <= 0.02
                    || ripple.getOpacity() >= releaseStartOpacity * 0.92
                    || releaseStart == null
                    || scene == null) {
                return false;
            }

            if (!captureSceneFrameWithChangedNodeArea(target, releaseStart, scene, releaseReference)) {
                return false;
            }

            releaseOpacityReference.set(ripple.getOpacity());
            return true;
        }, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(releaseReference.get(), "released ripple target snapshot"),
                    snapshotName,
                    "released"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, settledReference, () -> {
            @Nullable Node ripple = rippleReference.get();
            return ripple != null && ripple.getOpacity() <= 0.001;
        }, () -> {
        }, () -> {
            Node ripple = Objects.requireNonNull(rippleReference.get(), targetName + " ripple node");
            settledOpacityReference.set(ripple.getOpacity());
            writeInteractionSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled ripple target snapshot"),
                    snapshotName,
                    "settled"
            );
        });

        Node target = Objects.requireNonNull(targetReference.get(), targetName);
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal ripple target snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed ripple target snapshot"),
                targetName + " ripple pressed frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(releaseStartReference.get(), "release-start ripple target snapshot"),
                Objects.requireNonNull(releaseReference.get(), "released ripple target snapshot"),
                targetName + " ripple release intermediate frame"
        );
        assertRippleOpacityFaded(
                Objects.requireNonNull(releaseOpacityReference.get(), "released ripple opacity"),
                Objects.requireNonNull(settledOpacityReference.get(), "settled ripple opacity"),
                targetName + " ripple release fade-out"
        );
        M3MotionSettings.clearMotionScheme(target);
    }

    /// Returns a ripple-specific motion scheme that makes release fade frames observable in real snapshots.
    private static M3MotionScheme visualRippleMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                observableSpec,
                standard.slowEffects(),
                standard.fastSpatial(),
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies that switch selection produces visible thumb animation intermediate frames.
    private static void verifySwitchSelectionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Switch> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> intermediateReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, intermediateReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Switches");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3Switch target = Objects.requireNonNull(firstVisibleSwitchWithText(
                    scene.getRoot(),
                    "Off"
            ), "switch");
            M3MotionSettings.setMotionScheme(target, visualSwitchMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal switch snapshot"),
                    "switch-selection",
                    "normal"
            );
            target.fire();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertTrue(target.isSelected());
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                    "switch-selection",
                    "intermediate"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, settledReference, () -> {
            @Nullable M3Switch target = targetReference.get();
            return target != null && target.isSelected();
        }, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled switch snapshot"),
                    "switch-selection",
                    "settled"
            );
            M3MotionSettings.clearMotionScheme(Objects.requireNonNull(targetReference.get(), "switch"));
        });

        M3Switch target = Objects.requireNonNull(targetReference.get(), "switch");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal switch snapshot"),
                Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                "switch selection intermediate frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled switch snapshot"),
                "switch selection settling frame"
        );
    }

    /// Returns a switch-specific motion scheme that makes real visual intermediate frames observable.
    private static M3MotionScheme visualSwitchMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR),
                standard.defaultSpatial(),
                standard.slowSpatial()
        );
    }

    /// Verifies popup enter and exit motion on the demo split button menu.
    private static void verifySplitButtonPopupAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SplitButton> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> popupRootReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadWhenNodeSnapshotChanged(
                popupRootReference::get,
                openingBaselineReference,
                openingReference,
                "split button popup opening frame",
                () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Split Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SplitButton target = Objects.requireNonNull(firstVisibleSplitButtonWithText(
                    scene.getRoot(),
                    "Create"
            ), "split button");
            M3MotionSettings.setMotionScheme(target, visualPopupMotionScheme());
            target.showMenu();
            assertTrue(target.isShowing());

            Node popupRoot = target.getMenu();
            layoutPopupRoot(popupRoot);
            targetReference.set(target);
            popupRootReference.set(popupRoot);
            openingBaselineReference.set(snapshotNode(popupRoot));
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening split button popup snapshot"),
                    "split-button-popup",
                    "opening"
            );
        });

        runOnFxThreadWhenNodeSnapshotStable(
                popupRootReference::get,
                settledReference,
                () -> popupRootSettled(popupRootReference.get()),
                "split button popup settled frame",
                () -> {
        }, () -> {
            M3SplitButton target = Objects.requireNonNull(targetReference.get(), "split button");
            assertTrue(target.isShowing());
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "split button popup");
            layoutPopupRoot(popupRoot);
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                    "split-button-popup",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                    "split button popup"
            );
            target.hideMenu();
        });

        runOnFxThreadWhenNodeSnapshotChanged(
                popupRootReference::get,
                settledReference,
                hidingReference,
                "split button popup hiding frame",
                () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding split button popup snapshot"),
                    "split-button-popup",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3SplitButton target = targetReference.get();
            return target != null && !target.isShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3SplitButton target = Objects.requireNonNull(targetReference.get(), "split button");
            assertFalse(target.isShowing());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening split button popup snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                "split button popup enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding split button popup snapshot"),
                "split button popup exit motion"
        );
    }

    /// Verifies nested menu popup enter and exit motion with side-by-side popup placement.
    private static void verifyNestedMenuPopupStackAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> ownerMenuReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadWhenStable(() -> {
            @Nullable M3MenuButton menuButton = menuButtonReference.get();
            return menuButton != null
                    && menuButton.isShowing()
                    && menuButton.getMenu().getScene() != null;
        }, SETTLED_STATE_PULSES, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Menus");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3MenuButton menuButton = Objects.requireNonNull(firstVisibleMenuButtonWithText(
                    scene.getRoot(),
                    "Open menu"
            ), "menu button");
            M3MotionSettings.setMotionScheme(menuButton, visualPopupMotionScheme());
            menuButton.showMenu();
            assertTrue(menuButton.isShowing());
            menuButtonReference.set(menuButton);
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            layoutPopupRoot(menuButton.getMenu());
        });

        runOnFxThreadWhenNodeSnapshotStable(() -> {
            @Nullable M3MenuButton menuButton = menuButtonReference.get();
            return menuButton == null ? null : menuButton.getMenu();
        }, ownerMenuReference, () -> {
            @Nullable M3MenuButton menuButton = menuButtonReference.get();
            return menuButton != null && popupRootSettled(menuButton.getMenu());
        }, "nested owner menu settled frame", () -> {
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            assertTrue(menuButton.isShowing());
            layoutPopupRoot(menuButton.getMenu());
            writeAnimationSnapshot(
                    Objects.requireNonNull(ownerMenuReference.get(), "owner menu snapshot"),
                    "nested-menu-owner",
                    "settled"
            );

            M3SubMenuItem subMenuItem = Objects.requireNonNull(firstVisibleSubMenuItemWithText(
                    menuButton.getMenu(),
                    "Move to"
            ), "submenu item");
            M3MotionSettings.setMotionScheme(subMenuItem, visualPopupMotionScheme());
            subMenuItem.showSubMenu();
            assertTrue(subMenuItem.isSubMenuShowing());
            layoutPopupRoot(subMenuItem.getSubMenu());
            openingBaselineReference.set(snapshotNode(subMenuItem.getSubMenu()));
            subMenuItemReference.set(subMenuItem);
        });

        runOnFxThreadWhenNodeSnapshotChanged(
                () -> {
                    @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                    return subMenuItem == null ? null : subMenuItem.getSubMenu();
                },
                openingBaselineReference,
                openingReference,
                "nested submenu opening frame",
                () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening submenu snapshot"),
                    "nested-submenu",
                    "opening"
            );
        });

        runOnFxThreadWhenNodeSnapshotStable(() -> {
            @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
            return subMenuItem == null ? null : subMenuItem.getSubMenu();
        }, settledReference, () -> {
            @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
            return subMenuItem != null && popupRootSettled(subMenuItem.getSubMenu());
        }, "nested submenu settled frame", () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            assertTrue(menuButton.isShowing());
            assertTrue(subMenuItem.isSubMenuShowing());
            layoutPopupRoot(menuButton.getMenu());
            layoutPopupRoot(subMenuItem.getSubMenu());
            assertPopupThemeContext(scene.getRoot(), menuButton.getMenu(), "owner menu");
            assertPopupThemeContext(scene.getRoot(), subMenuItem.getSubMenu(), "nested submenu");
            assertMenuSurfaceGeometry(menuButton.getMenu(), null, "nested owner menu");
            assertMenuSurfaceGeometry(subMenuItem.getSubMenu(), null, "nested submenu");
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                    "nested-submenu",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(ownerMenuReference.get(), "owner menu snapshot"),
                    "nested owner menu"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                    "nested submenu"
            );
            assertPopupStackSideBySide(menuButton.getMenu(), subMenuItem.getSubMenu());
            subMenuItem.hideSubMenu();
        });

        runOnFxThreadWhenNodeSnapshotChanged(
                () -> {
                    @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                    return subMenuItem == null ? null : subMenuItem.getSubMenu();
                },
                settledReference,
                hidingReference,
                "nested submenu hiding frame",
                () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding submenu snapshot"),
                    "nested-submenu",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
            return subMenuItem != null && !subMenuItem.isSubMenuShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            assertFalse(subMenuItem.isSubMenuShowing());
            menuButton.hideMenu();
            M3MotionSettings.clearMotionScheme(subMenuItem);
            M3MotionSettings.clearMotionScheme(menuButton);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening submenu snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                "nested submenu enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding submenu snapshot"),
                "nested submenu exit motion"
        );
    }

    /// Verifies popup enter and exit motion on the demo date picker field.
    private static void verifyDatePickerFieldPopupAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3DatePickerField> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> popupRootReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadWhenNodeSnapshotChanged(
                popupRootReference::get,
                openingBaselineReference,
                openingReference,
                "date picker field popup opening frame",
                () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Date Pickers");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3DatePickerField target = Objects.requireNonNull(firstVisibleDatePickerField(
                    scene.getRoot()
            ), "date picker field");
            M3MotionSettings.setMotionScheme(target, visualPopupMotionScheme());
            target.showPicker();
            assertTrue(target.isShowing());

            Node popupRoot = Objects.requireNonNull(pickerPopupRoot(target), "date picker field popup");
            layoutPopupRoot(popupRoot);
            targetReference.set(target);
            popupRootReference.set(popupRoot);
            openingBaselineReference.set(snapshotNode(popupRoot));
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening date picker popup snapshot"),
                    "date-picker-field-popup",
                    "opening"
            );
        });

        runOnFxThreadWhenNodeSnapshotStable(
                popupRootReference::get,
                settledReference,
                () -> popupRootSettled(popupRootReference.get()),
                "date picker field popup settled frame",
                () -> {
        }, () -> {
            M3DatePickerField target = Objects.requireNonNull(targetReference.get(), "date picker field");
            assertTrue(target.isShowing());
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "date picker field popup");
            layoutPopupRoot(popupRoot);
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                    "date-picker-field-popup",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                    "date picker field popup"
            );
            target.hidePicker();
        });

        runOnFxThreadWhenNodeSnapshotChanged(
                popupRootReference::get,
                settledReference,
                hidingReference,
                "date picker field popup hiding frame",
                () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding date picker popup snapshot"),
                    "date-picker-field-popup",
                    "hiding"
            );
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3DatePickerField target = targetReference.get();
            return target != null && !target.isShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3DatePickerField target = Objects.requireNonNull(targetReference.get(), "date picker field");
            assertFalse(target.isShowing());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening date picker popup snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                "date picker field popup enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding date picker popup snapshot"),
                "date picker field popup exit motion"
        );
    }

    /// Returns a popup-specific motion scheme that makes real visual intermediate frames observable.
    private static M3MotionScheme visualPopupMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR),
                standard.defaultSpatial(),
                standard.slowSpatial()
        );
    }

    /// Verifies selected-indicator motion on a demo navigation item.
    private static void verifyNavigationItemSelectionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            String snapshotName
    ) throws InterruptedException {
        AtomicReference<@Nullable M3NavigationItem> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> intermediateReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhen(() -> {
            @Nullable M3NavigationItem target = targetReference.get();
            @Nullable WritableImage normal = normalReference.get();
            @Nullable Scene scene = sceneReference.get();
            if (target == null || normal == null || scene == null || !navigationIndicatorIsPartiallySelected(target)) {
                return false;
            }

            WritableImage frame = snapshot(scene);
            if (!nodeAreaChangedEnough(target, normal, frame)) {
                return false;
            }

            intermediateReference.set(frame);
            return true;
        }, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting(pageTitle);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3NavigationItem target = Objects.requireNonNull(firstVisibleNavigationItemWithText(
                    scene.getRoot(),
                    "Search"
            ), "navigation item");
            assertFalse(target.isSelected());
            M3MotionSettings.setMotionScheme(target, visualNavigationMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal navigation snapshot"),
                    snapshotName,
                    "normal"
            );
            target.fire();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertTrue(target.isSelected());
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                    snapshotName,
                    "intermediate"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, settledReference, () -> {
            @Nullable M3NavigationItem target = targetReference.get();
            return target != null && target.isSelected() && navigationIndicatorIsFullySelected(target);
        }, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled navigation snapshot"),
                    snapshotName,
                    "settled"
            );
            M3MotionSettings.clearMotionScheme(Objects.requireNonNull(targetReference.get(), "navigation item"));
        });

        M3NavigationItem target = Objects.requireNonNull(targetReference.get(), "navigation item");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal navigation snapshot"),
                Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                snapshotName + " intermediate frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled navigation snapshot"),
                snapshotName + " settling frame"
        );
    }

    /// Returns whether the navigation selected indicator is between hidden and selected visual states.
    private static boolean navigationIndicatorIsPartiallySelected(M3NavigationItem item) {
        @Nullable Node indicator = navigationIndicator(item);
        if (indicator == null) {
            return false;
        }

        double opacity = indicator.getOpacity();
        double scaleX = indicator.getScaleX();
        return opacity > 0.05 && opacity < 0.95 && scaleX > 0.74 && scaleX < 0.99;
    }

    /// Returns whether the navigation selected indicator reached its selected visual state.
    private static boolean navigationIndicatorIsFullySelected(M3NavigationItem item) {
        @Nullable Node indicator = navigationIndicator(item);
        return indicator != null && indicator.getOpacity() >= 0.999 && indicator.getScaleX() >= 0.999;
    }

    /// Returns the selected indicator node for a navigation item.
    private static @Nullable Node navigationIndicator(M3NavigationItem item) {
        return item.lookup(".m3-navigation-item-indicator");
    }

    /// Verifies expand and collapse motion on the demo sidebar's visible drawer group.
    private static void verifySidebarDrawerGroupExpansionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3NavigationDrawerGroup> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, collapsedReference, sceneReference, expandingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3NavigationDrawerGroup target = Objects.requireNonNull(firstVisibleDrawerGroupWithTitle(
                    scene.getRoot(),
                    "Buttons"
            ), "sidebar drawer group");
            M3MotionSettings.setAnimationsEnabled(target, false);
            target.setExpanded(false);
            M3MotionSettings.clearAnimationsEnabled(target);
            M3MotionSettings.setMotionScheme(target, visualNavigationMotionScheme());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            targetReference.set(target);
            collapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsedReference.get(), "collapsed drawer group snapshot"),
                    "sidebar-drawer-group",
                    "collapsed"
            );
            target.setExpanded(true);
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                    "sidebar-drawer-group",
                    "expanding"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, expandedReference, () -> {
            @Nullable M3NavigationDrawerGroup target = targetReference.get();
            return target != null && target.isExpanded();
        }, () -> {
        }, () -> {
            M3NavigationDrawerGroup target = Objects.requireNonNull(targetReference.get(), "sidebar drawer group");
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                    "sidebar-drawer-group",
                    "expanded"
            );
            assertTrue(target.isExpanded());
            target.setExpanded(false);
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, expandedReference, sceneReference, collapsingReference, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                    "sidebar-drawer-group",
                    "collapsing"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, settledReference, () -> {
            @Nullable M3NavigationDrawerGroup target = targetReference.get();
            return target != null && !target.isExpanded();
        }, () -> {
        }, () -> {
            M3NavigationDrawerGroup target = Objects.requireNonNull(targetReference.get(), "sidebar drawer group");
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled drawer group snapshot"),
                    "sidebar-drawer-group",
                    "settled"
            );
            assertFalse(target.isExpanded());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(collapsedReference.get(), "collapsed drawer group snapshot"),
                Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                "sidebar drawer group expand intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                "sidebar drawer group expand settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                "sidebar drawer group collapse intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled drawer group snapshot"),
                "sidebar drawer group collapse settling frame"
        );
    }

    /// Returns a navigation-specific motion scheme that makes selection and disclosure frames observable.
    private static M3MotionScheme visualNavigationMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                observableSpec,
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies bottom sheet hide and show motion in the demo page.
    private static void verifyBottomSheetVisibilityAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3BottomSheet> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> shownReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> showingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> resettledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, shownReference, sceneReference, hidingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Bottom Sheets");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3BottomSheet target = Objects.requireNonNull(firstVisibleBottomSheetWithHeadline(
                    scene.getRoot(),
                    "Now playing"
            ), "bottom sheet");
            M3MotionSettings.setMotionScheme(target, visualSheetMotionScheme());
            targetReference.set(target);
            shownReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(shownReference.get(), "shown bottom sheet snapshot"),
                    "bottom-sheet",
                    "shown"
            );
            target.hide();
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                    "bottom-sheet",
                    "hiding"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, hiddenReference, () ->
                bottomSheetHidden(targetReference.get()), () -> {
        }, () -> {
            M3BottomSheet target = Objects.requireNonNull(targetReference.get(), "bottom sheet");
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                    "bottom-sheet",
                    "hidden"
            );
            assertFalse(target.isVisible());
            target.show();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hiddenReference, sceneReference, showingReference, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                    "bottom-sheet",
                    "showing"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, resettledReference, () ->
                bottomSheetShown(targetReference.get()), () -> {
        }, () -> {
            M3BottomSheet target = Objects.requireNonNull(targetReference.get(), "bottom sheet");
            writeAnimationSnapshot(
                    Objects.requireNonNull(resettledReference.get(), "resettled bottom sheet snapshot"),
                    "bottom-sheet",
                    "resettled"
            );
            assertTrue(target.isShown());
            assertTrue(target.isVisible());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(shownReference.get(), "shown bottom sheet snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                "bottom sheet hide intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                "bottom sheet hide settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                "bottom sheet show intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                Objects.requireNonNull(resettledReference.get(), "resettled bottom sheet snapshot"),
                "bottom sheet show settling frame"
        );
    }

    /// Verifies side sheet hide and show motion in the demo page.
    private static void verifySideSheetVisibilityAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SideSheet> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> shownReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> showingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> resettledReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, shownReference, sceneReference, hidingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Side Sheets");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SideSheet target = Objects.requireNonNull(firstVisibleSideSheetWithHeadline(
                    scene.getRoot(),
                    "Details"
            ), "side sheet");
            M3MotionSettings.setMotionScheme(target, visualSheetMotionScheme());
            targetReference.set(target);
            shownReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(shownReference.get(), "shown side sheet snapshot"),
                    "side-sheet",
                    "shown"
            );
            target.hide();
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                    "side-sheet",
                    "hiding"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, hiddenReference, () ->
                sideSheetHidden(targetReference.get()), () -> {
        }, () -> {
            M3SideSheet target = Objects.requireNonNull(targetReference.get(), "side sheet");
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                    "side-sheet",
                    "hidden"
            );
            assertFalse(target.isVisible());
            target.show();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hiddenReference, sceneReference, showingReference, () -> {
        }, () -> {
            writeAnimationSnapshot(
                    Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                    "side-sheet",
                    "showing"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, resettledReference, () ->
                sideSheetShown(targetReference.get()), () -> {
        }, () -> {
            M3SideSheet target = Objects.requireNonNull(targetReference.get(), "side sheet");
            writeAnimationSnapshot(
                    Objects.requireNonNull(resettledReference.get(), "resettled side sheet snapshot"),
                    "side-sheet",
                    "resettled"
            );
            assertTrue(target.isShown());
            assertTrue(target.isVisible());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(shownReference.get(), "shown side sheet snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                "side sheet hide intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                "side sheet hide settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                "side sheet show intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                Objects.requireNonNull(resettledReference.get(), "resettled side sheet snapshot"),
                "side sheet show settling frame"
        );
    }

    /// Returns a sheet-specific motion scheme that makes visibility motion frames observable.
    private static M3MotionScheme visualSheetMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies snackbar entrance and dismissal motion on the demo host.
    private static void verifySnackbarHostAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SnackbarHost> hostReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenBaselineReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(hostReference, hiddenBaselineReference, sceneReference, openingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Snackbars");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SnackbarHost host = Objects.requireNonNull(firstVisibleSnackbarHost(scene.getRoot()), "snackbar host");
            M3MotionSettings.setMotionScheme(host, visualOverlayMotionScheme());
            host.setDisplayDuration(Duration.INDEFINITE);
            hostReference.set(host);
            hiddenBaselineReference.set(snapshot(scene));
            host.show("Theme-aware snackbar", "Action", event -> {
            });
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            M3Snackbar snackbar = Objects.requireNonNull(host.getSnackbar(), "opening snackbar");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertSnackbarStaysCompact(scene, snackbar);
            openingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening snackbar snapshot"),
                    "snackbar-host",
                    "opening"
            );
        });

        runOnFxThreadWhenNodeAreaStable(hostReference, sceneReference, settledReference, () ->
                snackbarSettled(hostReference.get()), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            M3Snackbar snackbar = Objects.requireNonNull(host.getSnackbar(), "settled snackbar");
            assertTrue(host.isShowing());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertSnackbarStaysCompact(scene, snackbar);
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                    "snackbar-host",
                    "settled"
            );
            host.dismiss();
        });

        runOnFxThreadWhenNodeAreaChanged(hostReference, settledReference, sceneReference, hidingReference, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            assertNotNull(host.getSnackbar(), "hiding snackbar");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                    "snackbar-host",
                    "hiding"
            );
        });

        runOnFxThreadWhenNodeAreaStable(hostReference, sceneReference, hiddenReference, () ->
                snackbarHidden(hostReference.get()), () -> {
        }, () -> {
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            assertFalse(host.isShowing());
            assertNull(host.getSnackbar(), "hidden snackbar");
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden snackbar snapshot"),
                    "snackbar-host",
                    "hidden"
            );
            M3MotionSettings.clearMotionScheme(host);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening snackbar snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                "snackbar host enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                "snackbar host exit motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden snackbar snapshot"),
                "snackbar host hidden frame"
        );
    }

    /// Verifies floating action button menu expand and collapse motion.
    private static void verifyFabMenuExpansionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3FabMenu> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> recollapsedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, collapsedReference, sceneReference, expandingReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("FAB Menu");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3FabMenu target = Objects.requireNonNull(firstVisibleFabMenu(scene.getRoot(), false), "collapsed FAB menu");
            M3MotionSettings.setMotionScheme(target, visualOverlayMotionScheme());
            targetReference.set(target);
            collapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsedReference.get(), "collapsed FAB menu snapshot"),
                    "fab-menu",
                    "collapsed"
            );
            target.show();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                    "fab-menu",
                    "expanding"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, expandedReference, () ->
                fabMenuExpandedSettled(targetReference.get()), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3FabMenu target = Objects.requireNonNull(targetReference.get(), "FAB menu");
            assertTrue(target.isExpanded());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertFabMenuActionsStayInsideShowcase(target);
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                    "fab-menu",
                    "expanded"
            );
            target.hide();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, expandedReference, sceneReference, collapsingReference, () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                    "fab-menu",
                    "collapsing"
            );
        });

        runOnFxThreadWhenNodeAreaStable(targetReference, sceneReference, recollapsedReference, () ->
                fabMenuCollapsedSettled(targetReference.get()), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3FabMenu target = Objects.requireNonNull(targetReference.get(), "FAB menu");
            assertFalse(target.isExpanded());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            writeAnimationSnapshot(
                    Objects.requireNonNull(recollapsedReference.get(), "recollapsed FAB menu snapshot"),
                    "fab-menu",
                    "recollapsed"
            );
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(collapsedReference.get(), "collapsed FAB menu snapshot"),
                Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                "FAB menu expanding frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                "FAB menu expanded frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                "FAB menu collapsing frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                Objects.requireNonNull(recollapsedReference.get(), "recollapsed FAB menu snapshot"),
                "FAB menu recollapsed frame"
        );
    }

    /// Verifies one installed tooltip popup opened from the real demo page.
    private static void verifyTooltipPopupSurface(
            AtomicReference<@Nullable Scene> sceneReference,
            String ownerText,
            String expectedPopupText,
            String snapshotName,
            Function<M3Button, M3Tooltip> tooltipInstaller,
            Consumer<Node> popupAssertions
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Button> ownerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> popupReference = new AtomicReference<>();

        runOnFxThreadWhenNodeSnapshotStable(() -> {
            @Nullable M3Tooltip tooltip = tooltipReference.get();
            return tooltip == null || tooltip.getScene() == null ? null : tooltip.getScene().getRoot();
        }, popupReference, () -> {
            @Nullable M3Tooltip tooltip = tooltipReference.get();
            if (tooltip == null || !tooltip.isShowing()) {
                return false;
            }
            return tooltipPopupAttached(tooltip);
        }, snapshotName + " tooltip popup settled frame", () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3Button owner = Objects.requireNonNull(firstVisibleButtonWithText(scene.getRoot(), ownerText),
                    () -> "tooltip owner: " + ownerText);
            M3Tooltip tooltip = tooltipInstaller.apply(owner);
            ownerReference.set(owner);
            tooltipReference.set(tooltip);
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_ENTERED, false);
        }, () -> {
            Scene ownerScene = Objects.requireNonNull(sceneReference.get(), "scene");
            Parent ownerRoot = assertInstanceOf(Parent.class, ownerScene.getRoot(), "owner scene root");
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "tooltip owner");
            M3Tooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
            Node popupRoot = Objects.requireNonNull(tooltip.getScene(), "tooltip scene").getRoot();
            Parent popupParent = assertInstanceOf(Parent.class, popupRoot, "tooltip popup root");
            layoutPopupRoot(popupRoot);

            assertTooltipNearOwner(owner, popupRoot);
            assertPopupThemeContext(ownerRoot, popupParent, snapshotName + " tooltip");
            assertVisibleTextContains(popupRoot, expectedPopupText, snapshotName + " tooltip");
            popupAssertions.accept(popupRoot);

            WritableImage image = Objects.requireNonNull(popupReference.get(), "tooltip popup snapshot");
            writeVisualSnapshot(image, Path.of(
                    "build",
                    "reports",
                    "m3fx-demo-visual",
                    "tooltip-popup-" + snapshotName + ".png"
            ));
            assertSnapshotHasVisibleContent(image, snapshotName + " tooltip popup");
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_EXITED, false);
            tooltip.hide();
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3Tooltip tooltip = tooltipReference.get();
            return tooltip != null && !tooltip.isShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "tooltip owner");
            M3Tooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
            assertFalse(tooltip.isShowing());
            M3Tooltip.uninstall(owner, tooltip);
        });
    }

    /// Verifies that a rich tooltip remains interactive while pointer focus transfers into its popup.
    private static void verifyRichTooltipInteractiveLifetime(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Button> ownerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> actionReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> popupReference = new AtomicReference<>();
        AtomicInteger actionCount = new AtomicInteger();

        runOnFxThreadWhenNodeSnapshotStable(() -> {
            @Nullable M3RichTooltip tooltip = tooltipReference.get();
            return tooltip == null || tooltip.getScene() == null ? null : tooltip.getScene().getRoot();
        }, popupReference, () -> {
            @Nullable M3RichTooltip tooltip = tooltipReference.get();
            return tooltip != null && tooltip.isShowing() && tooltip.getScene() != null;
        }, "rich tooltip popup settled frame", () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Tooltips");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3Button owner = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Rich action"
            ), "rich tooltip owner");
            M3Button action = new M3Button("Open");
            action.setOnAction(event -> actionCount.incrementAndGet());
            M3RichTooltip tooltip = M3RichTooltip.install(
                    owner,
                    "Generated theme",
                    "The tooltip keeps its action surface available while pointer focus moves into the popup.",
                    action
            );
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setHideDelay(Duration.ZERO);
            tooltip.setShowDuration(Duration.INDEFINITE);
            ownerReference.set(owner);
            actionReference.set(action);
            tooltipReference.set(tooltip);
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_ENTERED, false);
        }, () -> {
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "rich tooltip owner");
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertTrue(tooltip.isShowing());
            Node popupRoot = Objects.requireNonNull(tooltip.getScene(), "rich tooltip scene").getRoot();
            layoutPopupRoot(popupRoot);
            assertTooltipNearOwner(owner, popupRoot);
            assertRichTooltipContentGeometry(popupRoot, "rich tooltip");
            M3Button action = assertRichTooltipActionInsidePopup(popupRoot);
            assertSame(actionReference.get(), action, "rich tooltip action should be the installed action node");
            writeAnimationSnapshot(
                    Objects.requireNonNull(popupReference.get(), "rich tooltip snapshot"),
                    "rich-tooltip",
                    "shown"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(popupReference.get(), "rich tooltip snapshot"),
                    "rich tooltip"
            );
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_EXITED, false);
            firePrimaryMouseEvent(popupRoot, MouseEvent.MOUSE_ENTERED, false);
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3RichTooltip tooltip = tooltipReference.get();
            return tooltip != null && tooltip.isShowing() && tooltip.getScene() != null;
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertTrue(tooltip.isShowing());
            M3Button action = Objects.requireNonNull(actionReference.get(), "rich tooltip action");
            firePrimaryMouseEventAtLayoutCenter(action, MouseEvent.MOUSE_ENTERED, false);
            firePrimaryMouseEventAtLayoutCenter(action, MouseEvent.MOUSE_PRESSED, true);
            assertTrue(action.isArmed(), "rich tooltip action should arm from a real pointer press");
            firePrimaryMouseEventAtLayoutCenter(action, MouseEvent.MOUSE_RELEASED, false);
            assertFalse(action.isArmed(), "rich tooltip action should disarm after pointer release");
            assertEquals(1, actionCount.get(),
                    "rich tooltip action should remain clickable after pointer transfers into the popup");
            assertTrue(tooltip.isShowing(),
                    "rich tooltip should stay visible while the transferred pointer clicks an action");
            tooltip.hide();
        });

        runOnFxThreadWhenStable(() -> {
            @Nullable M3RichTooltip tooltip = tooltipReference.get();
            return tooltip != null && !tooltip.isShowing();
        }, SETTLED_STATE_PULSES, () -> {
        }, () -> {
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "rich tooltip owner");
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertFalse(tooltip.isShowing());
            M3RichTooltip.uninstall(owner, tooltip);
        });
    }

    /// Verifies that the real dialog window uses a compact Material dialog pane.
    private static void verifyDialogPopupSurface(
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> dialogSnapshotReference = new AtomicReference<>();

        runOnFxThreadWhenNodeSnapshotStable(() -> {
            @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
            return dialog == null ? null : dialog.getDialogPane();
        }, dialogSnapshotReference, () -> {
            @Nullable M3Dialog<ButtonType> dialog = dialogReference.get();
            if (dialog == null || !dialog.isShowing()) {
                return false;
            }

            Parent dialogPane = dialog.getDialogPane();
            return dialogPane.getScene() != null
                    && dialogPane.getScene().getWindow() != null
                    && dialogPane.getScene().getWindow().isShowing()
                    && dialogPane.getLayoutBounds().getWidth() > 0.0
                    && dialogPane.getLayoutBounds().getHeight() > 0.0;
        }, "dialog popup settled frame", () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3Dialog<ButtonType> dialog = new M3Dialog<>(
                    "M3FX Demo Dialog",
                    "Dialog title",
                    "The active theme is applied to this dialog pane.",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );
            dialog.initOwner(scene.getRoot());
            dialog.getDialogPane().setPrefWidth(420.0);
            dialog.show();
            dialogReference.set(dialog);
        }, () -> {
            Scene ownerScene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3Dialog<ButtonType> dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
            assertTrue(dialog.isShowing());
            Parent dialogPane = dialog.getDialogPane();
            dialogPane.applyCss();
            dialogPane.layout();
            assertDialogPaneStaysCompact(ownerScene, dialogPane);
            WritableImage dialogImage = Objects.requireNonNull(
                    dialogSnapshotReference.get(),
                    "dialog popup snapshot"
            );
            assertDialogPopupHeaderUsesContainerSurface(dialogImage);
            writeAnimationSnapshot(
                    Objects.requireNonNull(dialogSnapshotReference.get(), "dialog popup snapshot"),
                    "dialog-popup",
                    "shown"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(dialogSnapshotReference.get(), "dialog popup snapshot"),
                    "dialog popup"
            );
            dialog.close();
        });
    }

    /// Returns an overlay-specific motion scheme that makes popup and surface transitions observable.
    private static M3MotionScheme visualOverlayMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(OBSERVABLE_MOTION_DURATION, M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                observableSpec,
                observableSpec,
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies focus feedback on a populated text field in the demo page.
    private static void verifyTextFieldFocusFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> focusedReference = new AtomicReference<>();

        runOnFxThreadWhenStable(() -> {
            @Nullable Node target = targetReference.get();
            return target != null && target.isFocused();
        }, SETTLED_STATE_PULSES, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Text Fields");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    M3TextField.STYLE_CLASS
            ), "text field");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal text field snapshot"),
                    "text-field",
                    "normal"
            );
            target.requestFocus();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            focusedReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(focusedReference.get(), "focused text field snapshot"),
                    "text-field",
                    "focused"
            );
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "text field"),
                Objects.requireNonNull(normalReference.get(), "normal text field snapshot"),
                Objects.requireNonNull(focusedReference.get(), "focused text field snapshot"),
                "text field focus"
        );
    }

    /// Verifies hover and pressed feedback on a sidebar destination row.
    private static void verifySidebarMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, hoverReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    "demo-sidebar-child-item"
            ), "sidebar item");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal sidebar snapshot"),
                    "sidebar",
                    "normal"
            );
            applyHoverPseudoState(target);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                    "sidebar",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "sidebar item");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hoverReference, sceneReference, pressedReference, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed sidebar snapshot"),
                    "sidebar",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "sidebar item");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearHoverPseudoState(target);
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "sidebar item"),
                Objects.requireNonNull(normalReference.get(), "normal sidebar snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                "sidebar hover"
        );
        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "sidebar item"),
                Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed sidebar snapshot"),
                "sidebar pressed"
        );
    }

    /// Verifies hover and pressed feedback on a toggle icon button in the demo page.
    private static void verifyIconToggleButtonMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadWhenNodeAreaChanged(targetReference, normalReference, sceneReference, hoverReference, () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Icon Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    M3IconToggleButton.STYLE_CLASS
            ), "toggle icon button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal toggle icon button snapshot"),
                    "icon-toggle-button",
                    "normal"
            );
            applyHoverPseudoState(target);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                    "icon-toggle-button",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadWhenNodeAreaChanged(targetReference, hoverReference, sceneReference, pressedReference, () -> {
        }, () -> {
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed toggle icon button snapshot"),
                    "icon-toggle-button",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearHoverPseudoState(target);
        });

        Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal toggle icon button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                "toggle icon button hover"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed toggle icon button snapshot"),
                "toggle icon button pressed"
        );
    }

    /// Verifies that disabled animations still apply interaction states immediately.
    private static void verifyDisabledAnimationInteractionFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();

        showPageWhenSidebarSelectionSettled(appReference, sceneReference, "Buttons", scene -> {
            M3MotionSettings.setAnimationsEnabled(scene.getRoot(), false);
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            try {
                Node target = Objects.requireNonNull(firstVisibleButtonWithText(
                        scene.getRoot(),
                        "Filled"
                ), "button");
                targetReference.set(target);
                normalReference.set(snapshot(scene));
                applyHoverPseudoState(target);
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                hoverReference.set(snapshot(scene));
            } finally {
                M3MotionSettings.clearAnimationsEnabled(scene.getRoot());
                @Nullable Node target = targetReference.get();
                if (target != null) {
                    clearHoverPseudoState(target);
                }
            }
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(normalReference.get(), "normal no-motion button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover no-motion button snapshot"),
                "button hover with animations disabled"
        );
    }

    /// Verifies that the requested page title is visible in the content area.
    private static void assertCurrentPageTitle(Scene scene, String pageTitle) {
        @Nullable Label title = null;
        for (Node node : scene.getRoot().lookupAll(".demo-page-title")) {
            if (node instanceof Label label && label.isVisible()) {
                title = label;
                break;
            }
        }
        assertNotNull(title, () -> "No visible page title for " + pageTitle);
        assertEquals(pageTitle, title.getText());
    }

    /// Verifies that the demo sidebar selection matches the displayed page.
    private static void assertSidebarSelectionMatchesCurrentPage(M3FXDemoApp app, String pageTitle) {
        String expected = Objects.requireNonNull(
                app.currentPageNavigationTitleForTesting(),
                () -> "No current page navigation title for " + pageTitle
        );
        String selected = Objects.requireNonNull(
                app.selectedSidebarNavigationTitleForTesting(),
                () -> "No selected sidebar item for " + pageTitle
        );
        assertEquals(expected, selected,
                () -> "Sidebar selection does not match page " + pageTitle
                        + ": expected=" + expected + ", selected=" + selected);
    }

    /// Clicks one demo header theme button and verifies the header geometry after theme reapplication.
    private static void clickHeaderThemeButton(Scene scene, String text, String description) {
        Node header = requireVisibleStyledDescendant(scene.getRoot(), "demo-header", "demo header");
        clickHeaderThemeControl(scene, requireHeaderButton(header, text, text), description);
    }

    /// Clicks one demo header theme switch and verifies the header geometry after motion setting reapplication.
    private static void clickHeaderThemeSwitch(Scene scene, String text, String description) {
        Node header = requireVisibleStyledDescendant(scene.getRoot(), "demo-header", "demo header");
        clickHeaderThemeControl(scene, requireHeaderSwitch(header, text), description);
    }

    /// Activates one demo header control and verifies the header geometry after the action reapplies settings.
    private static void clickHeaderThemeControl(Scene scene, Node control, String description) {
        ButtonBase button = assertInstanceOf(
                ButtonBase.class,
                control,
                description + " header control should be activatable"
        );
        button.fire();
        scene.getRoot().applyCss();
        scene.getRoot().layout();

        assertDemoHeaderThemeControlGeometry(scene, "after " + description);
    }

    /// Verifies the demo root contains the expected theme mode style class and not the opposite mode.
    private static void assertRootStyleMode(Scene scene, String expectedStyleClass, String absentStyleClass, String description) {
        List<String> styleClasses = scene.getRoot().getStyleClass();
        assertTrue(styleClasses.contains(expectedStyleClass),
                () -> description + " should install " + expectedStyleClass + ": " + styleClasses);
        assertFalse(styleClasses.contains(absentStyleClass),
                () -> description + " should remove " + absentStyleClass + ": " + styleClasses);
    }

    /// Verifies the demo header controls remain usable and fixed-size after theme reapplication.
    private static void assertDemoHeaderThemeControlGeometry(Scene scene, String description) {
        Parent root = scene.getRoot();
        assertExclusiveRootThemeModes(scene, description);

        Node header = requireVisibleStyledDescendant(root, "demo-header", "demo header");
        assertVisibleControlBounds(header, description + " header");

        List<M3IconButton> seedButtons = headerSeedButtons(header);
        assertEquals(5, seedButtons.size(), description + " seed button count");
        for (M3IconButton seedButton : seedButtons) {
            assertHeaderSeedButtonGeometry(seedButton, description);
        }

        assertVisibleControlBounds(
                requireHeaderButton(header, "profile button", "Expressive", "Baseline"),
                description + " profile button"
        );
        assertVisibleControlBounds(
                requireHeaderButton(header, "density button", "Standard", "Comfort", "Compact"),
                description + " density button"
        );
        assertVisibleControlBounds(
                requireHeaderButton(header, "brightness button", "Dark", "Light"),
                description + " brightness button"
        );
        assertVisibleControlBounds(requireHeaderSwitch(header, "Animations"), description + " animations switch");
    }

    /// Verifies that exactly one profile and one brightness style class is installed on the demo root.
    private static void assertExclusiveRootThemeModes(Scene scene, String description) {
        List<String> styleClasses = scene.getRoot().getStyleClass();
        int profileModeCount = 0;
        if (styleClasses.contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS)) {
            profileModeCount++;
        }
        if (styleClasses.contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS)) {
            profileModeCount++;
        }
        int brightnessModeCount = 0;
        if (styleClasses.contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS)) {
            brightnessModeCount++;
        }
        if (styleClasses.contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS)) {
            brightnessModeCount++;
        }

        assertEquals(1, profileModeCount, () -> description + " should expose one profile mode: " + styleClasses);
        assertEquals(1, brightnessModeCount, () -> description + " should expose one brightness mode: " + styleClasses);
    }

    /// Returns the visible demo header seed-color buttons.
    private static List<M3IconButton> headerSeedButtons(Node header) {
        List<M3IconButton> seedButtons = new ArrayList<>();
        for (M3IconButton button : visibleNodesOfType(header, M3IconButton.class)) {
            if (button.getStyleClass().contains("demo-seed-button")) {
                seedButtons.add(button);
            }
        }
        return seedButtons;
    }

    /// Returns a visible demo header button with one of the requested labels.
    private static M3Button requireHeaderButton(Node header, String description, String... textOptions) {
        for (M3Button button : visibleNodesOfType(header, M3Button.class)) {
            if (!button.isVisible() || !hasRenderableBounds(button)) {
                continue;
            }
            for (String textOption : textOptions) {
                if (textOption.equals(button.getText())) {
                    return button;
                }
            }
        }
        fail("Missing demo header " + description);
        throw new AssertionError("unreachable");
    }

    /// Returns a visible demo header switch with the requested label.
    private static M3Switch requireHeaderSwitch(Node header, String text) {
        for (M3Switch switchControl : visibleNodesOfType(header, M3Switch.class)) {
            if (switchControl.isVisible()
                    && hasRenderableBounds(switchControl)
                    && text.equals(switchControl.getText())) {
                return switchControl;
            }
        }
        fail("Missing demo header switch: " + text);
        throw new AssertionError("unreachable");
    }

    /// Verifies one demo header seed-color button keeps its fixed square layout and circular token shape.
    private static void assertHeaderSeedButtonGeometry(M3IconButton button, String description) {
        Bounds bounds = button.localToScene(button.getBoundsInLocal());
        assertEquals(HEADER_SEED_BUTTON_SIZE, bounds.getWidth(), HEADER_SEED_BUTTON_SIZE_TOLERANCE,
                () -> description + " seed button width changed: " + bounds);
        assertEquals(HEADER_SEED_BUTTON_SIZE, bounds.getHeight(), HEADER_SEED_BUTTON_SIZE_TOLERANCE,
                () -> description + " seed button height changed: " + bounds);
        assertTrue(button.getContainerShape() >= Math.min(bounds.getWidth(), bounds.getHeight()) / 2.0
                        - HEADER_SEED_BUTTON_ROUNDNESS_TOLERANCE,
                () -> description + " seed button container shape is not circular: shape="
                        + button.getContainerShape() + ", bounds=" + bounds);
        assertEquals(0.0, button.getHorizontalPadding(), 0.01,
                () -> description + " seed button horizontal padding should stay zero");
    }

    /// Verifies the seed-color buttons render as rounded circles in the final scene snapshot.
    private static void assertHeaderSeedButtonsRenderCircular(Scene scene, String description) {
        WritableImage image = snapshot(scene);
        Node header = requireVisibleStyledDescendant(scene.getRoot(), "demo-header", "demo header");
        for (M3IconButton button : headerSeedButtons(header)) {
            assertHeaderSeedButtonRenderedCircular(image, button, description);
        }
    }

    /// Verifies that the rendered corners of a seed-color button are outside the colored circular fill.
    private static void assertHeaderSeedButtonRenderedCircular(
            WritableImage image,
            M3IconButton button,
            String description
    ) {
        Bounds bounds = button.localToScene(button.getBoundsInLocal());
        Color center = snapshotColorAt(image, bounds.getCenterX(), bounds.getCenterY());
        Color topLeft = snapshotColorAt(image, bounds.getMinX() + 2.0, bounds.getMinY() + 2.0);
        Color topRight = snapshotColorAt(image, bounds.getMaxX() - 2.0, bounds.getMinY() + 2.0);
        Color bottomLeft = snapshotColorAt(image, bounds.getMinX() + 2.0, bounds.getMaxY() - 2.0);
        Color bottomRight = snapshotColorAt(image, bounds.getMaxX() - 2.0, bounds.getMaxY() - 2.0);

        assertTrue(center.getOpacity() > 0.75,
                () -> description + " seed button center should be opaque: " + center);
        assertTrue(pixelDistance(center, topLeft) > 0.08
                        && pixelDistance(center, topRight) > 0.08
                        && pixelDistance(center, bottomLeft) > 0.08
                        && pixelDistance(center, bottomRight) > 0.08,
                () -> description + " seed button corners look filled instead of circular: bounds=" + bounds
                        + ", center=" + center
                        + ", topLeft=" + topLeft
                        + ", topRight=" + topRight
                        + ", bottomLeft=" + bottomLeft
                        + ", bottomRight=" + bottomRight);
    }

    /// Returns the color at a scene-snapshot coordinate.
    private static Color snapshotColorAt(WritableImage image, double x, double y) {
        int pixelX = clampPixelCoordinate((int) Math.round(x), (int) image.getWidth());
        int pixelY = clampPixelCoordinate((int) Math.round(y), (int) image.getHeight());
        return image.getPixelReader().getColor(pixelX, pixelY);
    }

    /// Samples the surface color immediately outside a rendered bounds.
    private static Color sampleColorOutsideBounds(WritableImage image, Bounds bounds) {
        double offset = 4.0;
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();
        if (bounds.getMinY() - offset >= 0.0) {
            return snapshotColorAt(image, centerX, bounds.getMinY() - offset);
        }
        if (bounds.getMaxY() + offset < image.getHeight()) {
            return snapshotColorAt(image, centerX, bounds.getMaxY() + offset);
        }
        if (bounds.getMinX() - offset >= 0.0) {
            return snapshotColorAt(image, bounds.getMinX() - offset, centerY);
        }
        if (bounds.getMaxX() + offset < image.getWidth()) {
            return snapshotColorAt(image, bounds.getMaxX() + offset, centerY);
        }
        return snapshotColorAt(image, bounds.getMinX(), bounds.getMinY());
    }

    /// Verifies that a header control has visible scene bounds.
    private static void assertVisibleControlBounds(Node node, String description) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(node.isVisible() && bounds.getWidth() > 0.0 && bounds.getHeight() > 0.0,
                () -> description + " should have visible bounds: " + bounds);
    }

    /// Shows a page and waits until the sidebar selected indicator has reached its final visual state.
    private static void showPageWhenSidebarSelectionSettled(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            Consumer<Scene> sceneSetup,
            Runnable verification
    ) throws InterruptedException {
        runOnFxThreadWhenStable(
                () -> {
                    M3FXDemoApp app = appReference.get();
                    Scene scene = sceneReference.get();
                    return app != null && scene != null && sidebarVisualSelectionSettled(app, scene);
                },
                SETTLED_STATE_PULSES,
                () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    app.showPageForTesting(pageTitle);
                    sceneSetup.accept(scene);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                },
                verification
        );
    }

    /// Shows one foundation demo page and applies shared utility-page visual assertions.
    private static void assertFoundationDemoPage(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            Consumer<Scene> pageAssertions
    ) throws InterruptedException {
        showPageWhenSidebarSelectionSettled(
                appReference,
                sceneReference,
                pageTitle,
                M3FXDemoVisualSmokeTest::resetDemoPageScroll,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    pageAssertions.accept(scene);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                    writePageSnapshot(
                            scene,
                            "foundation-" + snapshotFileName(pageTitle) + ".png",
                            pageTitle + " foundation controls"
                    );
                }
        );
    }

    /// Shows one button-family demo page and applies state-matrix plus shared visual assertions.
    private static void assertButtonFamilyDemoPage(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            Consumer<Scene> pageAssertions
    ) throws InterruptedException {
        showPageWhenSidebarSelectionSettled(
                appReference,
                sceneReference,
                pageTitle,
                M3FXDemoVisualSmokeTest::resetDemoPageScroll,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    pageAssertions.accept(scene);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                    writePageSnapshot(
                            scene,
                            "button-family-" + snapshotFileName(pageTitle) + ".png",
                            pageTitle + " button-family controls"
                    );
                }
        );
    }

    /// Shows one overlay or feedback demo page and applies state-matrix plus shared visual assertions.
    private static void assertOverlayDemoPage(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            Consumer<Scene> pageAssertions
    ) throws InterruptedException {
        showPageWhenSidebarSelectionSettled(
                appReference,
                sceneReference,
                pageTitle,
                M3FXDemoVisualSmokeTest::resetDemoPageScroll,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    pageAssertions.accept(scene);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                    writePageSnapshot(
                            scene,
                            "overlay-" + snapshotFileName(pageTitle) + ".png",
                            pageTitle + " overlay controls"
                    );
                }
        );
    }

    /// Resets the shared demo page scroll pane before a visual page assertion.
    private static void resetDemoPageScroll(Scene scene) {
        @Nullable Node node = scene.getRoot().lookup(".demo-scroll-pane");
        if (node instanceof ScrollPane scrollPane) {
            scrollPane.setVvalue(0.0);
            scrollPane.setHvalue(0.0);
        }
    }

    /// Asserts that the sidebar selected indicator has no outgoing visual selection residue.
    private static void assertSidebarVisualSelectionSettled(M3FXDemoApp app, Scene scene, String pageTitle) {
        assertTrue(sidebarVisualSelectionSettled(app, scene),
                () -> "Sidebar visual selection has not settled for " + pageTitle
                        + ": " + sidebarSelectionDebug(scene));
        assertSidebarSelectionGeometry(scene, pageTitle);
    }

    /// Returns whether the sidebar selected indicator has settled on the current page only.
    private static boolean sidebarVisualSelectionSettled(M3FXDemoApp app, Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        String expected = app.currentPageNavigationTitleForTesting();
        if (expected == null) {
            return false;
        }

        List<M3ListItem> items = demoSidebarItems(scene.getRoot());
        List<M3ListItem> selectedItems = items.stream().filter(M3ListItem::isSelected).toList();
        if (selectedItems.size() != 1 || !expected.equals(selectedItems.get(0).getHeadlineText())) {
            return false;
        }

        for (M3ListItem item : items) {
            @Nullable Node selectionContainer = item.lookup(".m3-list-item-selection-container");
            if (selectionContainer == null) {
                return false;
            }

            if (item.isSelected()) {
                if (selectionContainer.getOpacity() < 0.995
                        || Math.abs(selectionContainer.getScaleX() - 1.0) > 0.005
                        || Math.abs(selectionContainer.getScaleY() - 1.0) > 0.005) {
                    return false;
                }
            } else if (selectionContainer.getOpacity() > 0.005) {
                return false;
            }
        }
        return true;
    }

    /// Returns a compact description of sidebar selected indicator state for assertion messages.
    private static String sidebarSelectionDebug(Scene scene) {
        StringBuilder builder = new StringBuilder();
        for (M3ListItem item : demoSidebarItems(scene.getRoot())) {
            if (!builder.isEmpty()) {
                builder.append("; ");
            }
            @Nullable Node selectionContainer = item.lookup(".m3-list-item-selection-container");
            builder.append(item.getHeadlineText())
                    .append("[selected=")
                    .append(item.isSelected())
                    .append(", opacity=");
            if (selectionContainer == null) {
                builder.append("missing");
            } else {
                builder.append(selectionContainer.getOpacity())
                        .append(", scaleX=")
                        .append(selectionContainer.getScaleX())
                        .append(", scaleY=")
                        .append(selectionContainer.getScaleY());
            }
            builder.append(']');
        }
        return builder.toString();
    }

    /// Returns visible list items that belong to the demo navigation drawer sidebar.
    private static List<M3ListItem> demoSidebarItems(Node root) {
        return visibleNodesOfType(root, M3ListItem.class)
                .stream()
                .filter(M3FXDemoVisualSmokeTest::isDemoSidebarItem)
                .toList();
    }

    /// Returns whether a list item is one of the demo sidebar destinations or group headers.
    private static boolean isDemoSidebarItem(M3ListItem item) {
        return item.getStyleClass().contains("demo-sidebar-top-item")
                || item.getStyleClass().contains("demo-sidebar-child-item")
                || item.getStyleClass().contains("demo-sidebar-group-item");
    }

    /// Verifies that the demo sidebar selected row geometry matches Material navigation drawer expectations.
    private static void assertSidebarSelectionGeometry(Scene scene, String pageTitle) {
        WritableImage image = snapshot(scene);
        List<M3ListItem> selectedItems = demoSidebarItems(scene.getRoot()).stream()
                .filter(M3ListItem::isSelected)
                .toList();
        assertEquals(1, selectedItems.size(),
                () -> "Sidebar should expose one selected visual row for " + pageTitle + ": " + selectedItems);
        M3ListItem selected = selectedItems.get(0);
        assertListItemSelectedContainerGeometry(image, selected, "sidebar selected `" + selected.getHeadlineText() + "`");

        if (selected.getStyleClass().contains("demo-sidebar-child-item")) {
            M3NavigationDrawerGroup group = Objects.requireNonNull(
                    nearestAncestorOfType(selected, M3NavigationDrawerGroup.class),
                    "sidebar selected child should belong to a navigation drawer group"
            );
            assertDrawerGroupChildSelectionGeometry(group, selected, "sidebar selected child");
        } else if (selected.getStyleClass().contains("demo-sidebar-top-item")
                || selected.getStyleClass().contains("demo-sidebar-group-item")) {
            Bounds selectedBounds = selected.localToScene(selected.getBoundsInLocal());
            Bounds containerBounds = listItemSelectionContainer(selected)
                    .localToScene(listItemSelectionContainer(selected).getBoundsInLocal());
            assertEquals(selectedBounds.getMinX(), containerBounds.getMinX(), CONTROL_EDGE_TOLERANCE,
                    () -> "Sidebar top-level selected container should align to the row start: row="
                            + selectedBounds + ", container=" + containerBounds);
            assertEquals(selectedBounds.getMaxX(), containerBounds.getMaxX(), CONTROL_EDGE_TOLERANCE,
                    () -> "Sidebar top-level selected container should align to the row end: row="
                            + selectedBounds + ", container=" + containerBounds);
        }
    }

    /// Captures the current scene root as a writable image.
    private static WritableImage snapshot(Scene scene) {
        int width = Math.max(1, (int) Math.ceil(scene.getWidth()));
        int height = Math.max(1, (int) Math.ceil(scene.getHeight()));
        WritableImage image = new WritableImage(width, height);
        scene.getRoot().snapshot(null, image);
        return image;
    }

    /// Captures one standalone node as a writable image.
    private static WritableImage snapshotNode(Node node) {
        Bounds bounds = node.getLayoutBounds();
        int width = Math.max(1, (int) Math.ceil(bounds.getWidth()));
        int height = Math.max(1, (int) Math.ceil(bounds.getHeight()));
        WritableImage image = new WritableImage(width, height);
        node.snapshot(null, image);
        return image;
    }

    /// Verifies that a snapshot contains enough non-background pixels to be useful as a visual artifact.
    private static void assertSnapshotHasVisibleContent(WritableImage image, String pageTitle) {
        int contrastingPixels = 0;
        Color background = image.getPixelReader().getColor(8, 8);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, background) > 0.08) {
                    contrastingPixels++;
                }
            }
        }
        int visiblePixels = contrastingPixels;
        assertTrue(visiblePixels > image.getWidth() * image.getHeight() * 0.02,
                () -> pageTitle + " snapshot has too little visible content: " + visiblePixels);
    }

    /// Verifies the shared page-level geometry checks for a rendered demo page.
    private static void assertDemoPageVisualGeometry(Scene scene, String pageTitle) {
        assertVisibleTextInsideScene(scene, pageTitle);
        assertVisibleMaterialControlsInsideScene(scene, pageTitle);
        assertLabeledControlTextInkGeometry(scene, pageTitle);
        assertListItemHeadlineTextInkGeometry(scene, pageTitle);
        assertListItemTextSegmentGeometry(scene, pageTitle);
        assertListItemSlotGeometry(scene, pageTitle);
        assertFixedTargetGlyphsCentered(scene, pageTitle);
        assertNavigationItemIconSlotsCentered(scene, pageTitle);
        assertSingleLineTextInputsHaveVerticalRoom(scene, pageTitle);
        assertSelectionIndicatorsCentered(scene, pageTitle);
        assertNavigationBadgesStayCompact(scene, pageTitle);
    }

    /// Applies the dedicated visual-state assertion for a rendered demo page.
    private static void assertDemoPageSpecificVisualState(Scene scene, String pageTitle) {
        Consumer<Scene> assertion = Objects.requireNonNull(
                DEMO_PAGE_VISUAL_STATE_ASSERTIONS.get(pageTitle),
                pageTitle + " page-specific visual assertion"
        );
        assertion.accept(scene);
    }

    /// Verifies that a search bar keeps its editor and optional slots inside the rounded search container.
    private static void assertSearchBarVisualGeometry(M3SearchBar searchBar) {
        assertTrue(hasRenderableBounds(searchBar), () -> "search bar has no renderable bounds: " + searchBar);
        Bounds searchBounds = searchBar.localToScene(searchBar.getBoundsInLocal());
        Bounds editorBounds = searchBar.getEditor().localToScene(searchBar.getEditor().getBoundsInLocal());
        boolean embeddedInSearchView = nearestAncestorOfType(searchBar, M3SearchView.class) != null;
        double expectedHeight = expectedSearchBarHeight(searchBar);

        if (embeddedInSearchView) {
            assertTrue(searchBounds.getHeight() >= expectedHeight - CONTROL_EDGE_TOLERANCE
                            && searchBounds.getHeight() <= expectedHeight + 16.0 + CONTROL_EDGE_TOLERANCE,
                    () -> "embedded search bar should stay within the active profile header height range: bounds="
                            + searchBounds + ", expectedHeight=" + expectedHeight);
        } else {
            assertEquals(expectedHeight, searchBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "standalone search bar should use the active profile container height: bounds="
                            + searchBounds + ", expectedHeight=" + expectedHeight);
        }
        assertTrue(containsBoundsWithTolerance(searchBounds, editorBounds, CONTROL_EDGE_TOLERANCE),
                () -> "search editor leaves the search bar container: editor="
                        + editorBounds + ", searchBar=" + searchBounds);

        @Nullable Node leading = searchBar.getLeading();
        assertNotNull(leading, "search bar should render a leading icon slot");
        assertTrue(hasRenderableBounds(leading), () -> "search leading slot has no renderable bounds: " + leading);
        Bounds leadingBounds = leading.localToScene(leading.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(searchBounds, leadingBounds, CONTROL_EDGE_TOLERANCE),
                () -> "search leading slot leaves the search bar container: leading="
                        + leadingBounds + ", searchBar=" + searchBounds);
        assertEquals(searchBounds.getCenterY(), leadingBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                () -> "search leading slot should stay vertically centered: leading="
                        + leadingBounds + ", searchBar=" + searchBounds);
        if (searchBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            assertTrue(leadingBounds.getCenterX() > editorBounds.getCenterX(),
                    () -> "RTL search leading slot should render after the editor: leading="
                            + leadingBounds + ", editor=" + editorBounds + ", searchBar=" + searchBounds);
        } else {
            assertTrue(leadingBounds.getCenterX() < editorBounds.getCenterX(),
                    () -> "search leading slot should render before the editor: leading="
                            + leadingBounds + ", editor=" + editorBounds + ", searchBar=" + searchBounds);
        }

        for (Node action : searchBar.getTrailingActions()) {
            assertTrue(action.isVisible() && hasRenderableBounds(action),
                    () -> "search trailing action should be visible: " + action);
            Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(searchBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "search trailing action leaves the search bar container: action="
                            + actionBounds + ", searchBar=" + searchBounds);
            assertEquals(searchBounds.getCenterY(), actionBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                    () -> "search trailing action should stay vertically centered: action="
                            + actionBounds + ", searchBar=" + searchBounds);
            if (searchBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                assertTrue(actionBounds.getCenterX() < editorBounds.getCenterX(),
                        () -> "RTL search trailing action should render before the editor: action="
                                + actionBounds + ", editor=" + editorBounds + ", searchBar=" + searchBounds);
            } else {
                assertTrue(actionBounds.getCenterX() > editorBounds.getCenterX(),
                        () -> "search trailing action should render after the editor: action="
                                + actionBounds + ", editor=" + editorBounds + ", searchBar=" + searchBounds);
            }
        }
    }

    /// Returns the search bar height expected from the active demo profile.
    private static double expectedSearchBarHeight(M3SearchBar searchBar) {
        @Nullable Scene scene = searchBar.getScene();
        if (scene != null
                && scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS)) {
            return 64.0;
        }
        return 56.0;
    }

    /// Verifies that an active search view shows its result rows as reachable Material list items.
    private static void assertSearchViewResultsVisible(M3SearchView searchView) {
        assertTrue(searchView.isActive(), "search view should be active");
        assertEquals(3, searchView.getResults().size(), "active search view should show three demo results");
        assertTrue(searchView.getResultsContainer().isVisible(), "active search results should be visible");
        assertTrue(searchView.getResultsContainer().isManaged(), "active search results should be managed");
        assertEquals(1.0, searchView.getResultsContainer().getOpacity(), 0.001,
                "active search results should be fully opaque");
        assertEquals(0.0, searchView.getResultsContainer().getTranslateY(), 0.001,
                "active search results should not be translated");
        for (Node result : searchView.getResults()) {
            assertInstanceOf(M3ListItem.class, result,
                    () -> "search demo results should use Material list rows: " + result);
            assertTrue(result.isVisible() && hasRenderableBounds(result),
                    () -> "active search result should render visible content: " + result);
        }
    }

    /// Verifies that an active search view keeps the embedded bar and result rows in one compact surface.
    private static void assertActiveSearchViewDemoGeometry(M3SearchView searchView) {
        Bounds viewBounds = searchView.localToScene(searchView.getBoundsInLocal());
        Bounds barBounds = searchView.getSearchBar().localToScene(searchView.getSearchBar().getBoundsInLocal());
        Bounds resultsBounds = searchView.getResultsContainer()
                .localToScene(searchView.getResultsContainer().getBoundsInLocal());

        assertTrue(containsBoundsWithTolerance(viewBounds, barBounds, CONTROL_EDGE_TOLERANCE),
                () -> "active search view embedded bar leaves the view surface: bar="
                        + barBounds + ", view=" + viewBounds);
        assertTrue(containsBoundsWithTolerance(viewBounds, resultsBounds, CONTROL_EDGE_TOLERANCE),
                () -> "active search view results leave the view surface: results="
                        + resultsBounds + ", view=" + viewBounds);
        assertSearchViewContentInsets(viewBounds, barBounds, "active search view bar");
        assertSearchViewResultsInset(barBounds, resultsBounds);
        double topInset = barBounds.getMinY() - viewBounds.getMinY();
        assertTrue(topInset >= -CONTROL_EDGE_TOLERANCE && topInset <= 24.0,
                () -> "active search view bar should start near the surface top inset: inset="
                        + topInset + ", bar=" + barBounds + ", view=" + viewBounds);
        double headerOverlap = barBounds.getMaxY() - resultsBounds.getMinY();
        assertTrue(headerOverlap >= -CONTROL_EDGE_TOLERANCE && headerOverlap <= 16.0,
                () -> "active search view results should start near the embedded bar bottom with compact overlap: overlap="
                        + headerOverlap + ", bar=" + barBounds + ", results=" + resultsBounds);

        double expectedResultsHeight = 0.0;
        double previousMaxY = resultsBounds.getMinY();
        for (Node result : searchView.getResults()) {
            assertInstanceOf(M3ListItem.class, result,
                    () -> "active search view demo result should be an M3ListItem: " + result);
            M3ListItem item = (M3ListItem) result;
            Bounds resultBounds = item.localToScene(item.getBoundsInLocal());
            assertSearchResultRowGeometry(item, resultBounds, resultsBounds);
            double expectedMinY = previousMaxY;
            assertEquals(expectedMinY, resultBounds.getMinY(), CONTROL_EDGE_TOLERANCE,
                    () -> "active search view result rows should be contiguous: previousMaxY="
                            + expectedMinY + ", result=" + resultBounds + ", item=" + item.getHeadlineText());
            expectedResultsHeight += resultBounds.getHeight();
            previousMaxY = resultBounds.getMaxY();
        }

        double finalExpectedResultsHeight = expectedResultsHeight;
        assertEquals(expectedResultsHeight, resultsBounds.getHeight(),
                CONTROL_EDGE_TOLERANCE * Math.max(1, searchView.getResults().size()),
                () -> "active search view results container height should match its rendered rows: expected="
                        + finalExpectedResultsHeight + ", results=" + resultsBounds);
        assertEquals(barBounds.getHeight() + resultsBounds.getHeight(), viewBounds.getHeight(),
                32.0,
                () -> "active search view surface height should be the embedded bar plus results: bar="
                        + barBounds + ", results=" + resultsBounds + ", view=" + viewBounds);
    }

    /// Verifies that search view content has a compact and symmetric horizontal inset.
    private static void assertSearchViewContentInsets(Bounds viewBounds, Bounds contentBounds, String description) {
        double leftInset = contentBounds.getMinX() - viewBounds.getMinX();
        double rightInset = viewBounds.getMaxX() - contentBounds.getMaxX();
        assertTrue(leftInset >= -CONTROL_EDGE_TOLERANCE && rightInset >= -CONTROL_EDGE_TOLERANCE,
                () -> description + " should stay inside the search view surface: leftInset="
                        + leftInset + ", rightInset=" + rightInset + ", content=" + contentBounds
                        + ", view=" + viewBounds);
        assertEquals(leftInset, rightInset, CONTROL_EDGE_TOLERANCE,
                () -> description + " should keep symmetric horizontal surface insets: leftInset="
                        + leftInset + ", rightInset=" + rightInset + ", content=" + contentBounds
                        + ", view=" + viewBounds);
        assertTrue(leftInset <= 24.0 && rightInset <= 24.0,
                () -> description + " horizontal surface inset is too large: leftInset="
                        + leftInset + ", rightInset=" + rightInset + ", content=" + contentBounds
                        + ", view=" + viewBounds);
    }

    /// Verifies the inset of active search results relative to the embedded search bar.
    private static void assertSearchViewResultsInset(Bounds barBounds, Bounds resultsBounds) {
        assertTrue(containsHorizontalBoundsWithTolerance(barBounds, resultsBounds, CONTROL_EDGE_TOLERANCE),
                () -> "active search view results should stay horizontally inside the embedded bar: results="
                        + resultsBounds + ", bar=" + barBounds);
        double leftInset = resultsBounds.getMinX() - barBounds.getMinX();
        double rightInset = barBounds.getMaxX() - resultsBounds.getMaxX();
        assertEquals(leftInset, rightInset, CONTROL_EDGE_TOLERANCE,
                () -> "active search view results should keep symmetric inset from the embedded bar: leftInset="
                        + leftInset + ", rightInset=" + rightInset + ", results=" + resultsBounds
                        + ", bar=" + barBounds);
        assertTrue(leftInset <= 16.0 && rightInset <= 16.0,
                () -> "active search view results inset from the embedded bar is too large: leftInset="
                        + leftInset + ", rightInset=" + rightInset + ", results=" + resultsBounds
                        + ", bar=" + barBounds);
    }

    /// Verifies one visible search result row inside an active search view.
    private static void assertSearchResultRowGeometry(M3ListItem item, Bounds rowBounds, Bounds resultsBounds) {
        assertTrue(containsBoundsWithTolerance(resultsBounds, rowBounds, CONTROL_EDGE_TOLERANCE),
                () -> "search result row leaves the results container: row="
                        + rowBounds + ", results=" + resultsBounds + ", item=" + item.getHeadlineText());
        assertEquals(resultsBounds.getWidth(), rowBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> "search result row should fill the results container width: row="
                        + rowBounds + ", results=" + resultsBounds + ", item=" + item.getHeadlineText());
        assertEquals(item.getTwoLineHeight(), rowBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "search result row should use the two-line list item height token: row="
                        + rowBounds + ", token=" + item.getTwoLineHeight() + ", item=" + item.getHeadlineText());
        Node headline = requireVisibleStyledDescendant(
                item,
                LIST_ITEM_HEADLINE_STYLE_CLASS,
                "search result headline for `" + item.getHeadlineText() + "`"
        );
        Node supporting = requireVisibleStyledDescendant(
                item,
                LIST_ITEM_SUPPORTING_STYLE_CLASS,
                "search result supporting text for `" + item.getHeadlineText() + "`"
        );
        Bounds headlineBounds = headline.localToScene(headline.getBoundsInLocal());
        Bounds supportingBounds = supporting.localToScene(supporting.getBoundsInLocal());
        assertTrue(headlineBounds.getCenterY() < supportingBounds.getCenterY(),
                () -> "search result headline should render above supporting text: headline="
                        + headlineBounds + ", supporting=" + supportingBounds + ", item=" + item.getHeadlineText());
    }

    /// Verifies that an inactive search view keeps its results out of layout and rendering.
    private static void assertSearchViewResultsHidden(M3SearchView searchView) {
        assertFalse(searchView.isActive(), "search view should be inactive");
        assertEquals(1, searchView.getResults().size(), "inactive search view should keep its model result");
        assertFalse(searchView.getResultsContainer().isVisible(), "inactive search results should be hidden");
        assertFalse(searchView.getResultsContainer().isManaged(), "inactive search results should not affect layout");
        assertEquals(0.0, searchView.getResultsContainer().getOpacity(), 0.001,
                "inactive search results should be transparent");
        assertTrue(searchView.getResultsContainer().getTranslateY() < 0.0,
                "inactive search results should keep the hidden offset");
    }

    /// Verifies that an inactive search view collapses to its embedded search bar instead of reserving result space.
    private static void assertInactiveSearchViewDemoGeometry(M3SearchView searchView) {
        Bounds viewBounds = searchView.localToScene(searchView.getBoundsInLocal());
        Bounds barBounds = searchView.getSearchBar().localToScene(searchView.getSearchBar().getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(viewBounds, barBounds, CONTROL_EDGE_TOLERANCE),
                () -> "inactive search view embedded bar leaves the view surface: bar="
                        + barBounds + ", view=" + viewBounds);
        assertSearchViewContentInsets(viewBounds, barBounds, "inactive search view bar");
        double topInset = barBounds.getMinY() - viewBounds.getMinY();
        assertTrue(topInset >= -CONTROL_EDGE_TOLERANCE && topInset <= 24.0,
                () -> "inactive search view bar should start near the surface top inset: inset="
                        + topInset + ", bar=" + barBounds + ", view=" + viewBounds);
        assertTrue(viewBounds.getHeight() >= barBounds.getHeight() - CONTROL_EDGE_TOLERANCE
                        && viewBounds.getHeight() <= barBounds.getHeight() + 40.0,
                () -> "inactive search view should collapse to its embedded search bar plus compact vertical insets: bar="
                        + barBounds + ", view=" + viewBounds);
        assertTrue(visibleNodesOfType(searchView.getResultsContainer(), M3ListItem.class).isEmpty(),
                "inactive search view should not render visible result rows");
    }

    /// Returns a text input layout matching the requested input text and prompt text.
    private static M3TextInputLayout requireTextInputLayout(
            List<M3TextInputLayout> layouts,
            String text,
            String labelText
    ) {
        for (M3TextInputLayout layout : layouts) {
            @Nullable TextInputControl input = layout.getInput();
            if (input != null
                    && text.equals(input.getText())
                    && labelText.equals(layout.getLabelText())) {
                return layout;
            }
        }
        return fail("No text input layout matches text `" + text + "` and label `" + labelText + "`");
    }

    /// Verifies that a text input layout keeps input, supporting row, and adornment slots in stable bounds.
    private static void assertTextInputLayoutContainerGeometry(M3TextInputLayout layout, String pageTitle) {
        @Nullable TextInputControl input = layout.getInput();
        assertNotNull(input, () -> pageTitle + " text input layout has no input: " + layout);
        TextInputControl actualInput = Objects.requireNonNull(input, "input");
        assertTrue(hasRenderableBounds(layout), () -> pageTitle + " text input layout has no bounds: " + layout);
        assertTrue(hasRenderableBounds(actualInput), () -> pageTitle + " text input has no bounds: " + actualInput);

        Bounds layoutBounds = layout.localToScene(layout.getBoundsInLocal());
        Bounds containerBounds = layout.getInputContainer().localToScene(layout.getInputContainer().getBoundsInLocal());
        Bounds inputBounds = actualInput.localToScene(actualInput.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(layoutBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> pageTitle + " input container escapes its layout: layout="
                        + layoutBounds + ", container=" + containerBounds);
        assertTrue(containsBoundsWithTolerance(containerBounds, inputBounds, CONTROL_EDGE_TOLERANCE),
                () -> pageTitle + " input escapes its container: input="
                        + inputBounds + ", container=" + containerBounds);

        assertTextInputSlotInsideContainer(layout, M3TextInputLayout.LEADING_STYLE_CLASS, pageTitle);
        assertTextInputSlotInsideContainer(layout, M3TextInputLayout.TRAILING_STYLE_CLASS, pageTitle);

        if (layout.getSupportingRow().isVisible() && hasRenderableBounds(layout.getSupportingRow())) {
            Bounds rowBounds = layout.getSupportingRow().localToScene(layout.getSupportingRow().getBoundsInLocal());
            assertTrue(containsHorizontalBoundsWithTolerance(layoutBounds, rowBounds, CONTROL_EDGE_TOLERANCE),
                    () -> pageTitle + " supporting row escapes layout horizontally: row="
                            + rowBounds + ", layout=" + layoutBounds);
            assertTrue(rowBounds.getMinY() >= containerBounds.getMaxY() - CONTROL_EDGE_TOLERANCE,
                    () -> pageTitle + " supporting row overlaps input container: row="
                            + rowBounds + ", container=" + containerBounds);
        }
    }

    /// Verifies that one text input slot remains inside the input container.
    private static void assertTextInputSlotInsideContainer(
            M3TextInputLayout layout,
            String slotStyleClass,
            String pageTitle
    ) {
        Bounds containerBounds = layout.getInputContainer().localToScene(layout.getInputContainer().getBoundsInLocal());
        for (Node slot : visibleNodesWithStyle(layout, slotStyleClass)) {
            Bounds slotBounds = slot.localToScene(slot.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(containerBounds, slotBounds, CONTROL_EDGE_TOLERANCE),
                    () -> pageTitle + " text input slot escapes container: style="
                            + slotStyleClass + ", slot=" + slotBounds + ", container=" + containerBounds);
            assertTrue(Math.abs(slotBounds.getCenterY() - containerBounds.getCenterY())
                            <= TEXT_INPUT_SLOT_CENTER_TOLERANCE,
                    () -> pageTitle + " text input slot is vertically off-center: style="
                            + slotStyleClass + ", slot=" + slotBounds + ", container=" + containerBounds);
        }
    }

    /// Verifies that a text input uses the expected Material variant.
    private static void assertTextInputVariant(
            M3TextInputLayout layout,
            M3TextInputVariant expected,
            String description
    ) {
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");
        M3TextInput textInput = assertInstanceOf(M3TextInput.class, input, description + " should use M3TextInput");
        assertEquals(expected, textInput.getVariant(), () -> description + " variant");
    }

    /// Verifies that an outlined floating label uses an opened outline notch instead of a masking background.
    private static void assertOutlinedFloatingLabelGeometry(M3TextInputLayout layout, String description) {
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");
        assertTextInputVariant(layout, M3TextInputVariant.OUTLINED, description);
        Label label = assertInstanceOf(
                Label.class,
                requireVisibleStyledDescendant(layout, M3TextInputLayout.LABEL_STYLE_CLASS, description + " label")
        );
        assertTrue(label.getBackground() == null || label.getBackground().isEmpty(),
                () -> description + " floating label should not use a background mask");

        Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
        Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
        assertTrue(Math.abs(labelBounds.getCenterY() - inputBounds.getMinY()) <= 14.0,
                () -> description + " floating label should straddle the outline top edge: label="
                        + labelBounds + ", input=" + inputBounds);
        assertTrue(labelBounds.getMinX() > inputBounds.getMinX() + 8.0
                        && labelBounds.getMaxX() < inputBounds.getMaxX() - 8.0,
                () -> description + " floating label should stay inside the outline safe inset: label="
                        + labelBounds + ", input=" + inputBounds);

        javafx.scene.shape.Path outline = assertInstanceOf(
                javafx.scene.shape.Path.class,
                requireVisibleStyledDescendant(layout, M3TextInputLayout.OUTLINE_STYLE_CLASS, description + " outline")
        );
        assertOutlinedPathHasOpenLabelNotch(outline, label, description);
    }

    /// Verifies that an outlined floating label does not paint a rectangular background mask in a real snapshot.
    private static void assertOutlinedFloatingLabelBackgroundMatchesSurrounding(
            WritableImage image,
            M3TextInputLayout layout,
            String description
    ) {
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");
        assertTextInputVariant(layout, M3TextInputVariant.OUTLINED, description);
        Label label = assertInstanceOf(
                Label.class,
                requireVisibleStyledDescendant(layout, M3TextInputLayout.LABEL_STYLE_CLASS, description + " label")
        );
        Text labelText = Objects.requireNonNull(firstVisibleText(label), description + " label text");
        Rectangle2D inkBounds = Objects.requireNonNull(
                renderedTextInkBounds(image, labelText),
                description + " rendered floating-label ink"
        );

        Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
        Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
        Color reference = averageSnapshotColor(
                image,
                (int) Math.floor(labelBounds.getMinX()),
                (int) Math.floor(labelBounds.getMinY() - 6.0),
                (int) Math.ceil(labelBounds.getMaxX()),
                (int) Math.ceil(labelBounds.getMinY() - 2.0)
        );

        int sampledPixels = 0;
        int mismatchedPixels = 0;
        double worstDistance = 0.0;
        @Nullable Point2D worstPoint = null;
        @Nullable Color worstColor = null;
        int startX = Math.max(0, (int) Math.floor(labelBounds.getMinX()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(labelBounds.getMaxX()));
        int startY = Math.max(0, (int) Math.floor(labelBounds.getMinY()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(labelBounds.getMaxY()));
        for (int y = startY; y < endY; y += 2) {
            for (int x = startX; x < endX; x += 2) {
                double sampleX = x + 0.5;
                double sampleY = y + 0.5;
                if (isInsideRectangleWithTolerance(inkBounds, sampleX, sampleY, 2.0)
                        || Math.abs(sampleY - inputBounds.getMinY()) <= 1.5) {
                    continue;
                }

                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() <= 0.1) {
                    continue;
                }

                sampledPixels++;
                double distance = pixelDistance(color, reference);
                if (distance > worstDistance) {
                    worstDistance = distance;
                    worstPoint = new Point2D(sampleX, sampleY);
                    worstColor = color;
                }
                if (distance > OUTLINED_FLOATING_LABEL_BACKGROUND_TOLERANCE) {
                    mismatchedPixels++;
                }
            }
        }

        int finalInitialSampledPixels = sampledPixels;
        assertTrue(finalInitialSampledPixels >= 8,
                () -> description + " floating label did not expose enough background pixels: label="
                        + labelBounds + ", ink=" + inkBounds + ", sampled=" + finalInitialSampledPixels);
        int allowedMismatches = Math.max(2, (int) Math.ceil(
                sampledPixels * OUTLINED_FLOATING_LABEL_BACKGROUND_MISMATCH_RATIO
        ));
        int finalSampledPixels = sampledPixels;
        int finalMismatchedPixels = mismatchedPixels;
        double finalWorstDistance = worstDistance;
        @Nullable Point2D finalWorstPoint = worstPoint;
        @Nullable Color finalWorstColor = worstColor;
        assertTrue(mismatchedPixels <= allowedMismatches,
                () -> description + " floating label paints a background mask: mismatched="
                        + finalMismatchedPixels + "/" + finalSampledPixels
                        + ", allowed=" + allowedMismatches
                        + ", worstDistance=" + finalWorstDistance
                        + ", worstPoint=" + finalWorstPoint
                        + ", worstColor=" + finalWorstColor
                        + ", reference=" + reference
                        + ", label=" + labelBounds
                        + ", ink=" + inkBounds
                        + ", input=" + inputBounds);
    }

    /// Returns whether a point falls inside a rendered rectangle after applying an edge tolerance.
    private static boolean isInsideRectangleWithTolerance(
            Rectangle2D rectangle,
            double x,
            double y,
            double tolerance
    ) {
        return x >= rectangle.getMinX() - tolerance
                && x <= rectangle.getMaxX() + tolerance
                && y >= rectangle.getMinY() - tolerance
                && y <= rectangle.getMaxY() + tolerance;
    }

    /// Verifies that an outlined input path has an open top notch around the floating label.
    private static void assertOutlinedPathHasOpenLabelNotch(
            javafx.scene.shape.Path outline,
            Label label,
            String description
    ) {
        assertTrue(outline.getElements().size() >= 4,
                () -> description + " outline path should contain a top notch");
        LineTo notchStart = assertInstanceOf(LineTo.class, outline.getElements().get(1),
                description + " notch start segment");
        MoveTo notchEnd = assertInstanceOf(MoveTo.class, outline.getElements().get(2),
                description + " notch end move");
        Bounds labelBounds = label.getBoundsInParent();
        double notchGap = notchEnd.getX() - notchStart.getX();
        assertTrue(notchGap >= Math.max(24.0, labelBounds.getWidth() * 0.55),
                () -> description + " outline notch is too small: gap=" + notchGap
                        + ", labelWidth=" + labelBounds.getWidth());
        assertTrue(notchStart.getX() <= labelBounds.getCenterX() && notchEnd.getX() >= labelBounds.getCenterX(),
                () -> description + " outline notch should cover the label center: notchStart="
                        + notchStart.getX() + ", notchEnd=" + notchEnd.getX()
                        + ", label=" + labelBounds);
    }

    /// Verifies that a visible text input counter renders the expected text inside the supporting row.
    private static void assertTextInputCounterText(M3TextInputLayout layout, String expectedText, String description) {
        Node counter = requireVisibleStyledDescendant(
                layout,
                M3TextInputLayout.COUNTER_STYLE_CLASS,
                description + " counter"
        );
        assertEquals(expectedText, visibleText(counter), () -> description + " counter text");
        Bounds rowBounds = layout.getSupportingRow().localToScene(layout.getSupportingRow().getBoundsInLocal());
        Bounds counterBounds = counter.localToScene(counter.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(rowBounds, counterBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " counter escapes supporting row: counter="
                        + counterBounds + ", row=" + rowBounds);
    }

    /// Verifies that an explicit trailing action remains inside the input container and centered.
    private static void assertTextInputTrailingActionGeometry(M3TextInputLayout layout, String description) {
        Node trailing = Objects.requireNonNull(layout.getTrailing(), () -> description + " trailing action");
        assertTrue(trailing.isVisible() && hasRenderableBounds(trailing),
                () -> description + " trailing action should be visible: " + trailing);
        Bounds containerBounds = layout.getInputContainer().localToScene(layout.getInputContainer().getBoundsInLocal());
        Bounds trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(containerBounds, trailingBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " trailing action escapes input container: trailing="
                        + trailingBounds + ", container=" + containerBounds);
        assertTrue(Math.abs(trailingBounds.getCenterY() - containerBounds.getCenterY()) <= CONTROL_EDGE_TOLERANCE,
                () -> description + " trailing action is vertically off-center: trailing="
                        + trailingBounds + ", container=" + containerBounds);
    }

    /// Verifies that an RTL text input layout mirrors input, leading, trailing, and clear-button slots.
    private static void assertRightToLeftTextInputLayoutGeometry(M3TextInputLayout layout, String description) {
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, layout.getEffectiveNodeOrientation(),
                () -> description + " layout should be right-to-left");
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, input.getEffectiveNodeOrientation(),
                () -> description + " input should inherit right-to-left orientation");

        Bounds containerBounds = layout.getInputContainer().localToScene(layout.getInputContainer().getBoundsInLocal());
        @Nullable Node leading = layout.getLeading();
        if (leading != null && leading.isVisible() && hasRenderableBounds(leading)) {
            Bounds leadingBounds = leading.localToScene(leading.getBoundsInLocal());
            assertTrue(leadingBounds.getCenterX() > containerBounds.getCenterX(),
                    () -> description + " leading slot should mirror to the physical right: leading="
                            + leadingBounds + ", container=" + containerBounds);
        }

        @Nullable Node trailing = layout.getTrailing();
        if (trailing != null && trailing.isVisible() && hasRenderableBounds(trailing)) {
            Bounds trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());
            assertTrue(trailingBounds.getCenterX() < containerBounds.getCenterX(),
                    () -> description + " trailing slot should mirror to the physical left: trailing="
                            + trailingBounds + ", container=" + containerBounds);
        }

        @Nullable Node clearButton = firstVisibleStyledDescendant(layout, M3TextInputLayout.CLEAR_BUTTON_STYLE_CLASS);
        if (clearButton != null) {
            Bounds clearBounds = clearButton.localToScene(clearButton.getBoundsInLocal());
            assertTrue(clearBounds.getCenterX() < containerBounds.getCenterX(),
                    () -> description + " clear button should mirror to the physical left: clear="
                            + clearBounds + ", container=" + containerBounds);
        }
    }

    /// Verifies that the built-in clear button remains inside the input container and centered.
    private static void assertTextInputClearButtonGeometry(M3TextInputLayout layout, String description) {
        Node clearButton = requireVisibleStyledDescendant(
                layout,
                M3TextInputLayout.CLEAR_BUTTON_STYLE_CLASS,
                description + " clear button"
        );
        Bounds containerBounds = layout.getInputContainer().localToScene(layout.getInputContainer().getBoundsInLocal());
        Bounds clearBounds = clearButton.localToScene(clearButton.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(containerBounds, clearBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " clear button escapes input container: clear="
                        + clearBounds + ", container=" + containerBounds);
        assertTrue(Math.abs(clearBounds.getCenterY() - containerBounds.getCenterY()) <= CONTROL_EDGE_TOLERANCE,
                () -> description + " clear button is vertically off-center: clear="
                        + clearBounds + ", container=" + containerBounds);
    }

    /// Verifies that a layout renders an active error state in both the input and supporting text.
    private static void assertTextInputErrorState(M3TextInputLayout layout, String description) {
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");
        M3TextInput textInput = assertInstanceOf(M3TextInput.class, input, description + " input");
        assertTrue(textInput.isError(), () -> description + " input should expose error state");
        Node supportingText = requireVisibleStyledDescendant(
                layout,
                M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS,
                description + " supporting error text"
        );
        assertFalse(visibleText(supportingText).isBlank(),
                () -> description + " should render error supporting text");
    }

    /// Verifies that demo validation feedback stays settled while active validation refreshes on edits.
    private static void assertDemoValidationFeedbackRemainsStableOnEdits(M3TextInputLayout layout) {
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");
        M3TextInput textInput = assertInstanceOf(M3TextInput.class, input, "validated email input");
        HBox supportingRow = layout.getSupportingRow();

        assertEquals("Use an email address", layout.getValidationErrorText(),
                "validated email should start with active validation feedback");
        assertTrue(textInput.isError(), "validated email should start in error state");
        assertVisibleSupportingRowIsSettled(supportingRow, "validated email initial feedback");

        input.replaceText(0, input.getLength(), "supportx");
        assertEquals("Use an email address", layout.getValidationErrorText(),
                "validated email should keep error feedback for invalid edits");
        assertTrue(textInput.isError(), "validated email should remain in error state for invalid edits");
        assertVisibleSupportingRowIsSettled(supportingRow, "validated email invalid edit feedback");

        input.replaceText(0, input.getLength(), "support@example.com");
        assertEquals("", layout.getValidationErrorText(),
                "validated email should clear validation feedback for valid edits");
        assertFalse(textInput.isError(), "validated email should clear input error state for valid edits");
        assertEquals("Validation runs on focus loss", visibleText(requireVisibleStyledDescendant(
                layout,
                M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS,
                "validated email helper text"
        )));
        assertVisibleSupportingRowIsSettled(supportingRow, "validated email valid edit feedback");

        input.replaceText(0, input.getLength(), "support");
        assertEquals("Use an email address", layout.getValidationErrorText(),
                "validated email should restore error feedback for invalid edits");
        assertTrue(textInput.isError(), "validated email should restore input error state for invalid edits");
        assertVisibleSupportingRowIsSettled(supportingRow, "validated email restored error feedback");
        assertTextInputLayoutContainerGeometry(layout, "Text Fields validation");
    }

    /// Verifies that an already visible supporting row is not replaying its entry transition.
    private static void assertVisibleSupportingRowIsSettled(HBox supportingRow, String description) {
        assertTrue(supportingRow.isVisible(), () -> description + " supporting row should be visible");
        assertTrue(supportingRow.isManaged(), () -> description + " supporting row should be managed");
        assertEquals(1.0, supportingRow.getOpacity(), 0.0001,
                () -> description + " supporting row opacity should be settled");
        assertEquals(0.0, supportingRow.getTranslateY(), 0.0001,
                () -> description + " supporting row translateY should be settled");
    }

    /// Returns the first rendered text value below a node.
    private static String visibleText(Node node) {
        if (node instanceof Label label) {
            return label.getText();
        }
        @Nullable Text text = firstVisibleText(node);
        return text == null ? "" : text.getText();
    }

    /// Returns a visible menu containing a menu item with the requested headline text.
    private static M3Menu requireMenuContainingText(List<M3Menu> menus, String text) {
        for (M3Menu menu : menus) {
            if (menuItemWithText(menu, text) != null) {
                return menu;
            }
        }
        return fail("No menu contains item text: " + text);
    }

    /// Returns a menu item with the requested headline text.
    private static M3MenuItem requireMenuItemWithText(M3Menu menu, String text) {
        @Nullable M3MenuItem item = menuItemWithText(menu, text);
        if (item != null) {
            return item;
        }
        return fail("No menu item has text `" + text + "` in menu: " + menu);
    }

    /// Returns whether a menu has a selected item with the requested headline text.
    private static boolean menuHasSelectedItem(M3Menu menu, String text) {
        for (M3MenuItem selectedItem : menu.getSelectedItems()) {
            if (text.equals(selectedItem.getHeadlineText())) {
                return true;
            }
        }
        return false;
    }

    /// Returns the first direct menu item with the requested headline text.
    private static @Nullable M3MenuItem menuItemWithText(M3Menu menu, String text) {
        for (Node item : menu.getItems()) {
            if (item instanceof M3MenuItem menuItem && text.equals(menuItem.getHeadlineText())) {
                return menuItem;
            }
        }
        return null;
    }

    /// Verifies a menu surface and its direct rows with real rendered geometry.
    private static void assertMenuSurfaceGeometry(
            M3Menu menu,
            @Nullable WritableImage sceneImage,
            String description
    ) {
        layoutPopupRoot(menu);
        assertPopupSurfaceSize(menu, description);
        assertNodeSnapshotHasOpaquePixels(menu, description);

        Bounds menuBounds = menu.localToScene(menu.getBoundsInLocal());
        assertTrue(menuBounds.getWidth() >= 112.0,
                () -> description + " should keep the Material minimum menu width: " + menuBounds);
        assertTrue(menuBounds.getHeight() > 0.0,
                () -> description + " should have positive height: " + menuBounds);

        int menuItemCount = 0;
        double previousMaxY = menuBounds.getMinY();
        for (Node item : menu.getItems()) {
            assertTrue(item.isVisible() && hasRenderableBounds(item),
                    () -> description + " contains a non-rendered direct item: " + item);
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsHorizontalBoundsWithTolerance(menuBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " item escapes the menu horizontally: menu="
                            + menuBounds + ", item=" + itemBounds + ", node=" + item);
            double previousRowMaxY = previousMaxY;
            assertTrue(itemBounds.getMinY() >= previousRowMaxY - CONTROL_EDGE_TOLERANCE,
                    () -> description + " item rows overlap or render out of order: previousMaxY="
                            + previousRowMaxY + ", item=" + itemBounds + ", node=" + item);
            assertTrue(itemBounds.getMaxY() <= menuBounds.getMaxY() + CONTROL_EDGE_TOLERANCE,
                    () -> description + " item escapes the menu vertically: menu="
                            + menuBounds + ", item=" + itemBounds + ", node=" + item);

            if (item instanceof M3MenuItem menuItem) {
                menuItemCount++;
                assertMenuItemRowGeometry(menuItem, itemBounds, sceneImage, description);
            } else if (item instanceof M3MenuSectionHeader header) {
                assertMenuSectionHeaderGeometry(header, itemBounds, description);
            } else if (item instanceof M3Divider divider) {
                assertMenuDividerGeometry(divider, itemBounds, menuBounds, description);
            }
            previousMaxY = Math.max(previousMaxY, itemBounds.getMaxY());
        }

        assertTrue(menuItemCount > 0, () -> description + " should render at least one actionable menu item");
        double lastRowMaxY = previousMaxY;
        assertTrue(lastRowMaxY <= menuBounds.getMaxY() + CONTROL_EDGE_TOLERANCE,
                () -> description + " rows extend below the surface: menu="
                        + menuBounds + ", lastRowMaxY=" + lastRowMaxY);
    }

    /// Verifies one direct menu item row.
    private static void assertMenuItemRowGeometry(
            M3MenuItem item,
            Bounds itemBounds,
            @Nullable WritableImage sceneImage,
            String description
    ) {
        double expectedHeight = switch (item.getLineCount()) {
            case ONE_LINE -> item.getOneLineHeight();
            case TWO_LINE -> item.getTwoLineHeight();
            case THREE_LINE -> item.getThreeLineHeight();
        };
        assertEquals(expectedHeight, itemBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " menu item height should match its resolved line-count token: item="
                        + item.getHeadlineText() + ", bounds=" + itemBounds + ", expected=" + expectedHeight);

        assertListItemSlotGeometry(
                item,
                item.getLeading(),
                item.getLeadingSlotSize(),
                LIST_ITEM_LEADING_STYLE_CLASS,
                "leading",
                itemBounds,
                description
        );
        assertListItemSlotGeometry(
                item,
                item.getTrailing(),
                item.getTrailingSlotSize(),
                LIST_ITEM_TRAILING_STYLE_CLASS,
                "trailing",
                itemBounds,
                description
        );

        Node selectionContainer = listItemSelectionContainer(item);
        if (item.isSelected()) {
            assertListItemSelectedContainerLayout(item, selectionContainer, description
                    + " selected menu item `" + item.getHeadlineText() + "`");
            if (sceneImage != null) {
                assertListItemSelectedContainerPixels(
                        sceneImage,
                        selectionContainer,
                        description + " selected menu item `" + item.getHeadlineText() + "`"
                );
            }
        } else {
            assertTrue(selectionContainer.getOpacity() <= 0.05,
                    () -> description + " unselected menu item should not leave selected-container residue: item="
                            + item.getHeadlineText() + ", opacity=" + selectionContainer.getOpacity());
        }

        if (item instanceof M3SubMenuItem subMenuItem) {
            assertSubMenuIndicatorGeometry(subMenuItem, itemBounds, description);
        }
    }

    /// Verifies one menu section header row.
    private static void assertMenuSectionHeaderGeometry(
            M3MenuSectionHeader header,
            Bounds headerBounds,
            String description
    ) {
        assertEquals(32.0, headerBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " section header should keep the Material compact header height: text="
                        + visibleText(header) + ", bounds=" + headerBounds);
        assertFalse(visibleText(header).isBlank(),
                () -> description + " section header should render non-empty text: " + header);
    }

    /// Verifies one menu divider row.
    private static void assertMenuDividerGeometry(
            M3Divider divider,
            Bounds dividerBounds,
            Bounds menuBounds,
            String description
    ) {
        assertTrue(dividerBounds.getWidth() >= menuBounds.getWidth() * 0.75,
                () -> description + " divider should span most of the menu width: divider="
                        + dividerBounds + ", menu=" + menuBounds);
        assertTrue(dividerBounds.getHeight() <= 24.0,
                () -> description + " divider row should stay compact: " + dividerBounds);
    }

    /// Verifies that a submenu indicator stays in the trailing slot and vertically centered.
    private static void assertSubMenuIndicatorGeometry(
            M3SubMenuItem item,
            Bounds itemBounds,
            String description
    ) {
        Node indicator = requireVisibleStyledDescendant(
                item,
                M3SubMenuItem.INDICATOR_STYLE_CLASS,
                description + " submenu indicator"
        );
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(itemBounds, indicatorBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " submenu indicator escapes its row: row="
                        + itemBounds + ", indicator=" + indicatorBounds);
        assertTrue(Math.abs(itemBounds.getCenterY() - indicatorBounds.getCenterY()) <= LIST_ITEM_SLOT_CENTER_TOLERANCE,
                () -> description + " submenu indicator is vertically off-center: row="
                        + itemBounds + ", indicator=" + indicatorBounds);
        if (item.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            assertTrue(indicatorBounds.getCenterX() < itemBounds.getCenterX(),
                    () -> description + " RTL submenu indicator should stay on the visual leading side: row="
                            + itemBounds + ", indicator=" + indicatorBounds);
        } else {
            assertTrue(indicatorBounds.getCenterX() > itemBounds.getCenterX(),
                    () -> description + " submenu indicator should stay on the visual trailing side: row="
                            + itemBounds + ", indicator=" + indicatorBounds);
        }
    }

    /// Verifies that demo icon slots use renderable SVG paths and no legacy text-placeholder `M3Icon` nodes.
    private static void assertDemoVectorIcons(Node root, String pageTitle, int minimumIconCount) {
        int[] iconCount = {0};
        visitVisibleNodes(root, node -> {
            if (node.getStyleClass().contains(DemoIcons.STYLE_CLASS)) {
                iconCount[0]++;
                assertInstanceOf(SVGPath.class, node,
                        () -> pageTitle + " demo vector icon style is not applied to an SVGPath: " + node);
                assertTrue(hasRenderableBounds(node),
                        () -> pageTitle + " demo vector icon has no renderable bounds: " + node);
                assertTrue(isInsideStableDemoIconViewport(node),
                        () -> pageTitle + " demo vector icon is not wrapped in a stable viewport: " + node);
            }

            if (node instanceof M3Icon && hasDemoIconSlotStyle(node)) {
                fail(() -> pageTitle + " still uses a text-placeholder M3Icon in an interactive icon slot: "
                        + node.getStyleClass());
            }

            if (node instanceof M3ListItem listItem) {
                assertListItemSlotIsNotTextPlaceholder(listItem.getLeading(), pageTitle, "leading");
                assertListItemSlotIsNotTextPlaceholder(listItem.getTrailing(), pageTitle, "trailing");
            }
        });

        assertTrue(iconCount[0] >= minimumIconCount,
                () -> pageTitle + " rendered too few demo SVG icons: actual="
                        + iconCount[0] + ", minimum=" + minimumIconCount);
    }

    /// Verifies that a list item icon slot does not use a text-placeholder `M3Icon`.
    private static void assertListItemSlotIsNotTextPlaceholder(
            @Nullable Node slot,
            String pageTitle,
            String slotName
    ) {
        if (slot instanceof M3Icon icon) {
            fail(() -> pageTitle + " list item " + slotName
                    + " slot still uses a text-placeholder M3Icon: " + icon.getText());
        }
    }

    /// Returns whether a node has a demo style class historically used for interactive icon slots.
    private static boolean hasDemoIconSlotStyle(Node node) {
        return node.getStyleClass().contains("demo-icon-label")
                || node.getStyleClass().contains("demo-fab-icon")
                || node.getStyleClass().contains("demo-navigation-icon")
                || node.getStyleClass().contains("demo-app-bar-icon");
    }

    /// Returns whether a demo SVG icon is inside a fixed or explicitly sized icon viewport.
    private static boolean isInsideStableDemoIconViewport(Node node) {
        return nearestAncestorWithStyle(node, "demo-vector-icon-viewport") != null
                || nearestAncestorWithStyle(node, "demo-sample-icon") != null;
    }

    /// Verifies that visible text nodes and their rendered ink are not clipped by scene or explicit ancestor clips.
    private static void assertVisibleTextInsideScene(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        WritableImage image = snapshot(scene);
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof Text text) || text.getText().isBlank() || !hasRenderableBounds(text)) {
                return;
            }

            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            if (!sceneBounds.intersects(textBounds) || !sceneBounds.contains(textBounds.getCenterX(), textBounds.getCenterY())) {
                return;
            }

            @Nullable Rectangle2D inkBounds = renderedTextInkBounds(image, text);
            if (inkBounds != null) {
                assertVisibleTextInkHasVerticalRoom(text, textBounds, inkBounds, pageTitle);
                assertVisibleTextInkInsideNearestVisualBoundary(text, inkBounds, pageTitle);
            }

            @Nullable Node scrollViewport = nearestScrollViewport(text);
            if (scrollViewport != null) {
                Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
                if (!viewportBounds.intersects(textBounds)
                        || !viewportBounds.contains(textBounds.getCenterX(), textBounds.getCenterY())) {
                    return;
                }
                assertTrue(containsHorizontalBoundsWithTolerance(viewportBounds, textBounds, TEXT_EDGE_TOLERANCE),
                        () -> pageTitle + " visible text leaves its scroll viewport horizontally: text="
                                + text.getText() + ", bounds=" + textBounds + ", viewport=" + viewportBounds);
                if (inkBounds != null) {
                    assertRectangleHorizontallyInsideBounds(
                            viewportBounds,
                            inkBounds,
                            TEXT_EDGE_TOLERANCE,
                            pageTitle + " visible text ink in scroll viewport"
                    );
                }
                if (touchesVerticalViewportEdge(textBounds, viewportBounds, TEXT_EDGE_TOLERANCE)) {
                    return;
                }
                if (inkBounds != null) {
                    assertVisibleTextInkHasExpectedCoverage(text, textBounds, inkBounds, pageTitle);
                }
                assertTrue(containsBoundsWithTolerance(viewportBounds, textBounds, TEXT_EDGE_TOLERANCE),
                        () -> pageTitle + " visible text leaves its scroll viewport: text="
                                + text.getText() + ", bounds=" + textBounds + ", viewport=" + viewportBounds);
                if (inkBounds != null) {
                    assertRectangleInsideBounds(
                            viewportBounds,
                            inkBounds,
                            TEXT_EDGE_TOLERANCE,
                            pageTitle + " visible text ink in scroll viewport"
                    );
                }
                return;
            }

            assertVisibleTextInsideAncestorClips(text, textBounds, inkBounds, pageTitle);
            if (inkBounds != null) {
                assertVisibleTextInkHasExpectedCoverage(text, textBounds, inkBounds, pageTitle);
            }
            assertTrue(containsBoundsWithTolerance(sceneBounds, textBounds, TEXT_EDGE_TOLERANCE),
                    () -> pageTitle + " visible text leaves the scene viewport: text="
                            + text.getText() + ", bounds=" + textBounds + ", scene=" + sceneBounds);
            if (inkBounds != null) {
                assertRectangleInsideBounds(
                        sceneBounds,
                        inkBounds,
                        TEXT_EDGE_TOLERANCE,
                        pageTitle + " visible text ink in scene"
                );
            }
        });
    }

    /// Verifies that rendered text ink does not materially overflow the text layout bounds.
    private static void assertVisibleTextInkHasVerticalRoom(
            Text text,
            Bounds textBounds,
            Rectangle2D inkBounds,
            String pageTitle
    ) {
        double topRoom = inkBounds.getMinY() - textBounds.getMinY();
        double bottomRoom = textBounds.getMaxY() - inkBounds.getMaxY();
        assertTrue(topRoom >= -TEXT_INK_VERTICAL_OVERFLOW_TOLERANCE
                        && bottomRoom >= -TEXT_INK_VERTICAL_OVERFLOW_TOLERANCE,
                () -> pageTitle + " visible text ink materially overflows its vertical layout edge: text="
                        + text.getText() + ", topRoom=" + topRoom + ", bottomRoom=" + bottomRoom
                        + ", textBounds=" + textBounds + ", inkBounds=" + inkBounds);
    }

    /// Verifies that rendered text ink keeps enough height to catch clipping that still remains inside layout bounds.
    private static void assertVisibleTextInkHasExpectedCoverage(
            Text text,
            Bounds textBounds,
            Rectangle2D inkBounds,
            String pageTitle
    ) {
        if (!shouldCheckRenderedTextInkCoverage(text)) {
            return;
        }
        if (!renderedTextInkWidthIsRepresentative(inkBounds, textBounds)) {
            return;
        }

        double fontSize = text.getFont().getSize();
        double minimumInkHeight = Math.max(3.0, fontSize * TEXT_INK_MINIMUM_FONT_HEIGHT_RATIO);
        assertTrue(inkBounds.getHeight() + TEXT_INK_HEIGHT_COVERAGE_TOLERANCE >= minimumInkHeight,
                () -> pageTitle + " visible text ink is too short for its font size and may be clipped: text="
                        + text.getText() + ", fontSize=" + fontSize + ", minimumInkHeight="
                        + minimumInkHeight + ", tolerance=" + TEXT_INK_HEIGHT_COVERAGE_TOLERANCE
                        + ", inkBounds=" + inkBounds);
    }

    /// Returns whether a text node should participate in rendered-ink height coverage checks.
    private static boolean shouldCheckRenderedTextInkCoverage(Text text) {
        if (text.getFont().getSize() < 6.0) {
            return false;
        }
        if (nearestAncestorWithStyle(text, "demo-sidebar-drawer") != null) {
            return false;
        }

        String value = text.getText();
        for (int index = 0; index < value.length(); index++) {
            if (Character.isLetterOrDigit(value.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether extracted text ink width is broad enough for a height coverage assertion.
    private static boolean renderedTextInkWidthIsRepresentative(Rectangle2D inkBounds, Bounds textBounds) {
        return inkBounds.getWidth() >= Math.max(24.0, textBounds.getWidth() * 0.35);
    }

    /// Returns whether extracted text ink height is broad enough for center-based geometry assertions.
    private static boolean renderedTextInkHeightIsRepresentative(Rectangle2D inkBounds, Text text) {
        return inkBounds.getHeight() >= Math.max(8.0, text.getFont().getSize() * 0.40);
    }

    /// Verifies that rendered text pixels stay inside the closest Material visual owner.
    private static void assertVisibleTextInkInsideNearestVisualBoundary(
            Text text,
            Rectangle2D inkBounds,
            String pageTitle
    ) {
        @Nullable Node boundary = nearestTextVisualBoundary(text);
        if (boundary == null || !hasRenderableBounds(boundary)) {
            return;
        }

        Bounds boundaryBounds = boundary.localToScene(boundary.getBoundsInLocal());
        assertRectangleInsideBounds(
                boundaryBounds,
                inkBounds,
                TEXT_EDGE_TOLERANCE,
                pageTitle + " visible text ink inside " + boundary.getClass().getSimpleName()
        );
    }

    /// Returns the nearest rendered control or Material component that should visually contain text ink.
    private static @Nullable Node nearestTextVisualBoundary(Text text) {
        @Nullable Parent parent = text.getParent();
        while (parent != null) {
            if (isTextVisualBoundary(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns whether a node is a visible owner that should contain descendant text ink.
    private static boolean isTextVisualBoundary(Node node) {
        return isPageLevelMaterialControl(node);
    }

    /// Verifies that visible text stays inside every explicit ancestor clip intersecting the text.
    private static void assertVisibleTextInsideAncestorClips(
            Text text,
            Bounds textBounds,
            @Nullable Rectangle2D inkBounds,
            String pageTitle
    ) {
        @Nullable Parent parent = text.getParent();
        while (parent != null) {
            @Nullable Node clip = parent.getClip();
            if (clip != null && hasRenderableBounds(clip)) {
                Bounds clipBounds = clip.localToScene(clip.getBoundsInLocal());
                if (clipBounds.intersects(textBounds)
                        && clipBounds.contains(textBounds.getCenterX(), textBounds.getCenterY())) {
                    Parent clipOwner = parent;
                    assertTrue(containsBoundsWithTolerance(clipBounds, textBounds, TEXT_EDGE_TOLERANCE),
                            () -> pageTitle + " visible text is clipped by an ancestor clip: text="
                                    + text.getText() + ", textBounds=" + textBounds
                                    + ", clipOwner=" + clipOwner + ", clipBounds=" + clipBounds);
                    if (inkBounds != null) {
                        assertRectangleInsideBounds(
                                clipBounds,
                                inkBounds,
                                TEXT_EDGE_TOLERANCE,
                                pageTitle + " visible text ink inside ancestor clip"
                        );
                    }
                }
            }
            parent = parent.getParent();
        }
    }

    /// Verifies that visible Material controls stay inside the visible scene and scroll viewport.
    private static void assertVisibleMaterialControlsInsideScene(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!isPageLevelMaterialControl(node) || !hasRenderableBounds(node)) {
                return;
            }

            Bounds controlBounds = node.localToScene(node.getBoundsInLocal());
            if (isOutsideSceneViewport(node, controlBounds, sceneBounds)) {
                return;
            }

            @Nullable Node scrollViewport = nearestScrollViewport(node);
            if (scrollViewport != null) {
                Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
                if (allowsHorizontalViewportClipping(node)) {
                    return;
                }
                assertTrue(containsHorizontalBoundsWithTolerance(viewportBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                        () -> pageTitle + " visible control leaves its scroll viewport horizontally: node="
                                + node + ", bounds=" + controlBounds + ", viewport=" + viewportBounds);
                if (touchesVerticalViewportEdge(controlBounds, viewportBounds, CONTROL_EDGE_TOLERANCE)) {
                    return;
                }
                assertTrue(containsBoundsWithTolerance(viewportBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                        () -> pageTitle + " visible control leaves its scroll viewport: node="
                                + node + ", bounds=" + controlBounds + ", viewport=" + viewportBounds);
                return;
            }

            assertTrue(containsBoundsWithTolerance(sceneBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                    () -> pageTitle + " visible control leaves the scene viewport: node="
                            + node + ", bounds=" + controlBounds + ", scene=" + sceneBounds);
        });
    }

    /// Returns whether a node belongs to a horizontal viewport that intentionally clips content at its edges.
    private static boolean allowsHorizontalViewportClipping(Node node) {
        return nearestAncestorWithStyle(node, M3Carousel.VIEWPORT_STYLE_CLASS) != null;
    }

    /// Returns the nearest scroll pane viewport that clips a node, or `null` when the node is not inside one.
    private static @Nullable Node nearestScrollViewport(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains("viewport") && hasRenderableBounds(parent)) {
                return parent;
            }
            if (parent instanceof ScrollPane scrollPane) {
                return scrollPane.lookup(".viewport");
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Verifies that visible `Labeled` control text ink stays inside and vertically centered in its control.
    private static void assertLabeledControlTextInkGeometry(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        WritableImage image = snapshot(scene);
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof Labeled labeled)
                    || !shouldCheckLabeledControlTextInkGeometry(labeled)
                    || !hasRenderableBounds(labeled)) {
                return;
            }

            @Nullable Text text = firstVisibleText(labeled);
            if (text == null || !hasRenderableBounds(text)) {
                return;
            }

            Bounds labeledBounds = labeled.localToScene(labeled.getBoundsInLocal());
            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            if (isOutsideSceneViewport(labeled, labeledBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(labeled, labeledBounds)
                    || isOutsideSceneViewport(text, textBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(text, textBounds)) {
                return;
            }

            @Nullable Rectangle2D inkBounds = renderedTextInkBounds(image, text);
            if (inkBounds == null || !renderedTextInkWidthIsRepresentative(inkBounds, textBounds)) {
                return;
            }

            assertRectangleInsideBounds(
                    labeledBounds,
                    inkBounds,
                    CONTROL_EDGE_TOLERANCE,
                    pageTitle + " labeled control text ink"
            );

            double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
            double deltaY = Math.abs(labeledBounds.getCenterY() - inkCenterY);
            assertTrue(deltaY <= LABELED_TEXT_INK_VERTICAL_CENTER_TOLERANCE,
                    () -> pageTitle + " labeled control text ink is vertically off-center: control="
                            + labeled + ", text=" + text.getText() + ", deltaY=" + deltaY
                            + ", controlBounds=" + labeledBounds + ", textBounds=" + textBounds
                            + ", inkBounds=" + inkBounds);
        });
    }

    /// Returns whether a `Labeled` control should participate in page-wide text ink geometry checks.
    private static boolean shouldCheckLabeledControlTextInkGeometry(Labeled labeled) {
        String text = labeled.getText();
        if (text == null || text.isBlank()) {
            return false;
        }
        if (!labeled.isVisible() || labeled.getOpacity() <= 0.0) {
            return false;
        }
        if (labeled instanceof Label) {
            return false;
        }
        if (labeled.getStyleClass().contains(M3Text.STYLE_CLASS)) {
            return false;
        }
        if (!isCenteredLabeledTextTarget(labeled)) {
            return false;
        }
        Bounds bounds = labeled.getBoundsInLocal();
        return bounds.getHeight() <= 96.0;
    }

    /// Returns whether a labeled control should vertically center its text ink in the full target.
    private static boolean isCenteredLabeledTextTarget(Labeled labeled) {
        return labeled instanceof M3Button
                || labeled instanceof M3Chip
                || labeled instanceof M3FloatingActionButton
                || labeled instanceof M3IconToggleButton
                || labeled instanceof M3SegmentedButton;
    }

    /// Verifies that one-line list item headlines are rendered inside and centered in their item rows.
    private static void assertListItemHeadlineTextInkGeometry(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        WritableImage image = snapshot(scene);
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3ListItem item)
                    || !shouldCheckListItemHeadlineTextInkGeometry(item)
                    || !hasRenderableBounds(item)) {
                return;
            }

            @Nullable Text text = listItemHeadlineText(item);
            assertNotNull(text, () -> pageTitle + " one-line list item should render its headline text: item="
                    + item + ", headline=" + item.getHeadlineText());
            Text headlineText = Objects.requireNonNull(text);
            if (!hasRenderableBounds(headlineText)) {
                return;
            }

            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            Bounds textBounds = headlineText.localToScene(headlineText.getBoundsInLocal());
            if (isOutsideSceneViewport(item, itemBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(item, itemBounds)
                    || isOutsideSceneViewport(headlineText, textBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(headlineText, textBounds)) {
                return;
            }

            @Nullable Rectangle2D inkBounds = renderedTextInkBounds(image, headlineText);
            if (inkBounds == null
                    || !renderedTextInkWidthIsRepresentative(inkBounds, textBounds)
                    || !renderedTextInkHeightIsRepresentative(inkBounds, headlineText)) {
                return;
            }

            assertRectangleInsideBounds(
                    itemBounds,
                    inkBounds,
                    CONTROL_EDGE_TOLERANCE,
                    pageTitle + " one-line list item headline ink"
            );

            double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
            double deltaY = Math.abs(itemBounds.getCenterY() - inkCenterY);
            assertTrue(deltaY <= LIST_ITEM_HEADLINE_INK_VERTICAL_CENTER_TOLERANCE,
                    () -> pageTitle + " one-line list item headline ink is vertically off-center: item="
                            + item + ", headline=" + headlineText.getText() + ", deltaY=" + deltaY
                            + ", itemBounds=" + itemBounds + ", textBounds=" + textBounds
                            + ", inkBounds=" + inkBounds);
        });
    }

    /// Returns whether a list item should participate in one-line headline ink centering checks.
    private static boolean shouldCheckListItemHeadlineTextInkGeometry(M3ListItem item) {
        if (!item.isVisible() || item.getOpacity() <= 0.0 || item.getHeadlineText().isBlank()) {
            return false;
        }
        if (!item.getOverlineText().isBlank() || !item.getSupportingText().isBlank()) {
            return false;
        }
        Bounds bounds = item.getBoundsInLocal();
        return bounds.getHeight() <= 96.0;
    }

    /// Returns the rendered headline text node for one list item.
    private static @Nullable Text listItemHeadlineText(M3ListItem item) {
        @Nullable Node headline = firstVisibleStyledDescendant(item, LIST_ITEM_HEADLINE_STYLE_CLASS);
        return headline == null ? null : firstVisibleText(headline);
    }

    /// Verifies that list item text segments stay inside their row and keep rendered ink within row bounds.
    private static void assertListItemTextSegmentGeometry(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        WritableImage image = snapshot(scene);
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3ListItem item) || !hasRenderableBounds(item)) {
                return;
            }

            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            if (isOutsideSceneViewport(item, itemBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(item, itemBounds)) {
                return;
            }

            @Nullable Bounds overlineBounds = assertListItemTextSegmentGeometry(
                    item,
                    LIST_ITEM_OVERLINE_STYLE_CLASS,
                    item.getOverlineText(),
                    "overline",
                    itemBounds,
                    image,
                    sceneBounds,
                    pageTitle
            );
            @Nullable Bounds headlineBounds = assertListItemTextSegmentGeometry(
                    item,
                    LIST_ITEM_HEADLINE_STYLE_CLASS,
                    item.getHeadlineText(),
                    "headline",
                    itemBounds,
                    image,
                    sceneBounds,
                    pageTitle
            );
            @Nullable Bounds supportingBounds = assertListItemTextSegmentGeometry(
                    item,
                    LIST_ITEM_SUPPORTING_STYLE_CLASS,
                    item.getSupportingText(),
                    "supporting",
                    itemBounds,
                    image,
                    sceneBounds,
                    pageTitle
            );
            assertListItemTextSegmentOrder(item, overlineBounds, headlineBounds, supportingBounds, pageTitle);
            assertListItemTextSegmentGeometry(
                    item,
                    LIST_ITEM_TRAILING_SUPPORTING_STYLE_CLASS,
                    item.getTrailingSupportingText(),
                    "trailing supporting",
                    itemBounds,
                    image,
                    sceneBounds,
                    pageTitle
            );
        });
    }

    /// Verifies one rendered list item text segment and returns its text bounds when checked.
    private static @Nullable Bounds assertListItemTextSegmentGeometry(
            M3ListItem item,
            String styleClass,
            String expectedText,
            String segmentName,
            Bounds itemBounds,
            WritableImage image,
            Bounds sceneBounds,
            String pageTitle
    ) {
        if (expectedText.isBlank()) {
            return null;
        }

        @Nullable Node segment = firstVisibleStyledDescendant(item, styleClass);
        assertNotNull(segment, () -> pageTitle + " list item " + segmentName
                + " segment is missing: item=" + item + ", text=" + expectedText);
        Node actualSegment = Objects.requireNonNull(segment);
        @Nullable Text text = firstVisibleText(actualSegment);
        assertNotNull(text, () -> pageTitle + " list item " + segmentName
                + " segment has no rendered text: item=" + item + ", text=" + expectedText);
        Text actualText = Objects.requireNonNull(text);
        if (!hasRenderableBounds(actualText)) {
            return null;
        }

        Bounds segmentBounds = actualSegment.localToScene(actualSegment.getBoundsInLocal());
        Bounds textBounds = actualText.localToScene(actualText.getBoundsInLocal());
        if (isOutsideSceneViewport(actualText, textBounds, sceneBounds)
                || isClippedAtScrollViewportEdge(actualText, textBounds)) {
            return null;
        }

        assertTrue(containsBoundsWithTolerance(itemBounds, segmentBounds, CONTROL_EDGE_TOLERANCE),
                () -> pageTitle + " list item " + segmentName + " segment leaves its row: item="
                        + itemBounds + ", segment=" + segmentBounds + ", text=" + actualText.getText());
        assertTrue(containsBoundsWithTolerance(itemBounds, textBounds, TEXT_EDGE_TOLERANCE),
                () -> pageTitle + " list item " + segmentName + " text leaves its row: item="
                        + itemBounds + ", textBounds=" + textBounds + ", text=" + actualText.getText());
        assertTrue(containsBoundsWithTolerance(segmentBounds, textBounds, TEXT_EDGE_TOLERANCE),
                () -> pageTitle + " list item " + segmentName + " text leaves its label segment: segment="
                        + segmentBounds + ", textBounds=" + textBounds + ", text=" + actualText.getText());

        @Nullable Rectangle2D inkBounds = renderedTextInkBounds(image, actualText);
        if (inkBounds != null
                && renderedTextInkWidthIsRepresentative(inkBounds, textBounds)
                && renderedTextInkHeightIsRepresentative(inkBounds, actualText)) {
            assertVisibleTextInkHasVerticalRoom(actualText, textBounds, inkBounds, pageTitle);
            assertVisibleTextInkHasExpectedCoverage(actualText, textBounds, inkBounds, pageTitle);
            assertRectangleInsideBounds(
                    itemBounds,
                    inkBounds,
                    TEXT_EDGE_TOLERANCE,
                    pageTitle + " list item " + segmentName + " rendered text ink"
            );
        }

        return textBounds;
    }

    /// Verifies that stacked list item text segments preserve their vertical order.
    private static void assertListItemTextSegmentOrder(
            M3ListItem item,
            @Nullable Bounds overlineBounds,
            @Nullable Bounds headlineBounds,
            @Nullable Bounds supportingBounds,
            String pageTitle
    ) {
        if (overlineBounds != null && headlineBounds != null) {
            assertTrue(overlineBounds.getCenterY() < headlineBounds.getCenterY(),
                    () -> pageTitle + " list item overline should render above headline: item="
                            + item + ", overline=" + overlineBounds + ", headline=" + headlineBounds);
        }
        if (headlineBounds != null && supportingBounds != null) {
            assertTrue(headlineBounds.getCenterY() < supportingBounds.getCenterY(),
                    () -> pageTitle + " list item headline should render above supporting text: item="
                            + item + ", headline=" + headlineBounds + ", supporting=" + supportingBounds);
        }
    }

    /// Verifies that list item leading and trailing slots remain inside their row and centered when one-line.
    private static void assertListItemSlotGeometry(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3ListItem item) || !hasRenderableBounds(item)) {
                return;
            }

            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            if (isOutsideSceneViewport(item, itemBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(item, itemBounds)) {
                return;
            }

            assertListItemSlotGeometry(
                    item,
                    item.getLeading(),
                    item.getLeadingSlotSize(),
                    LIST_ITEM_LEADING_STYLE_CLASS,
                    "leading",
                    itemBounds,
                    pageTitle
            );
            assertListItemSlotGeometry(
                    item,
                    item.getTrailing(),
                    item.getTrailingSlotSize(),
                    LIST_ITEM_TRAILING_STYLE_CLASS,
                    "trailing",
                    itemBounds,
                    pageTitle
            );
        });
    }

    /// Verifies one list item slot and its directly assigned content.
    private static void assertListItemSlotGeometry(
            M3ListItem item,
            @Nullable Node content,
            M3ListItemSlotSize slotSize,
            String slotStyleClass,
            String slotName,
            Bounds itemBounds,
            String pageTitle
    ) {
        if (content == null || !content.isVisible()) {
            return;
        }

        @Nullable Node slot = firstVisibleStyledDescendant(item, slotStyleClass);
        assertNotNull(slot, () -> pageTitle + " list item " + slotName
                + " slot is missing for visible content: item=" + item + ", content=" + content);
        Node actualSlot = Objects.requireNonNull(slot);
        Bounds slotBounds = actualSlot.localToScene(actualSlot.getBoundsInLocal());

        assertTrue(containsBoundsWithTolerance(itemBounds, slotBounds, CONTROL_EDGE_TOLERANCE),
                () -> pageTitle + " list item " + slotName + " slot leaves its row: item="
                        + itemBounds + ", slot=" + slotBounds + ", content=" + content);
        if (isOneLineListItem(item)) {
            assertTrue(Math.abs(slotBounds.getCenterY() - itemBounds.getCenterY()) <= LIST_ITEM_SLOT_CENTER_TOLERANCE,
                    () -> pageTitle + " one-line list item " + slotName + " slot is vertically off-center: item="
                            + itemBounds + ", slot=" + slotBounds + ", content=" + content);
        }

        if (slotSize.isFixedSize()) {
            assertEquals(slotSize.getWidth(), slotBounds.getWidth(), LIST_ITEM_FIXED_SLOT_SIZE_TOLERANCE,
                    () -> pageTitle + " list item " + slotName + " slot width does not match its token: slot="
                            + slotBounds + ", expected=" + slotSize.getWidth() + ", content=" + content);
            assertEquals(slotSize.getHeight(), slotBounds.getHeight(), LIST_ITEM_FIXED_SLOT_SIZE_TOLERANCE,
                    () -> pageTitle + " list item " + slotName + " slot height does not match its token: slot="
                            + slotBounds + ", expected=" + slotSize.getHeight() + ", content=" + content);
        }

        if (hasRenderableBounds(content)) {
            Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(itemBounds, contentBounds, CONTROL_EDGE_TOLERANCE),
                    () -> pageTitle + " list item " + slotName + " content leaves its row: item="
                            + itemBounds + ", content=" + contentBounds + ", node=" + content);
        }

        @Nullable Node vectorIcon = firstVisibleDemoVectorIcon(actualSlot);
        if (vectorIcon != null) {
            assertVectorIconCenteredInContainer(
                    actualSlot,
                    vectorIcon,
                    pageTitle + " list item " + slotName + " vector icon"
            );
        }
    }

    /// Returns whether a list item uses the one-line text structure.
    private static boolean isOneLineListItem(M3ListItem item) {
        return item.getOverlineText().isBlank() && item.getSupportingText().isBlank();
    }

    /// Verifies that fixed-size Material targets keep their glyph text centered.
    private static void assertFixedTargetGlyphsCentered(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        WritableImage image = snapshot(scene);
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!isCenteredTarget(node) || !hasRenderableBounds(node)) {
                return;
            }

            @Nullable Node glyph = firstVisibleText(node);
            if (glyph == null || !hasRenderableBounds(glyph)) {
                glyph = centeredMaterialVectorIconNode(node);
            }
            if (glyph == null || !hasRenderableBounds(glyph)) {
                assertFalse(
                        shouldRequireCenteredGlyph(node),
                        () -> pageTitle + " fixed target should render a centered glyph: target=" + node
                );
                return;
            }

            Node renderedGlyph = glyph;
            Bounds targetBounds = node.localToScene(node.getLayoutBounds());
            Bounds glyphBounds = renderedGlyph.localToScene(renderedGlyph.getLayoutBounds());
            if (isOutsideSceneViewport(node, targetBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(node, targetBounds)
                    || isOutsideSceneViewport(renderedGlyph, glyphBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(renderedGlyph, glyphBounds)) {
                return;
            }

            double dx = Math.abs(targetBounds.getCenterX() - glyphBounds.getCenterX());
            double dy = Math.abs(targetBounds.getCenterY() - glyphBounds.getCenterY());
            assertTrue(dx <= 3.0 && dy <= 3.5,
                    () -> pageTitle + " fixed target glyph is off-center: target="
                            + node + ", glyph=" + renderedGlyph + ", dx=" + dx + ", dy=" + dy
                            + ", targetBounds=" + targetBounds + ", glyphBounds=" + glyphBounds);

            if (renderedGlyph instanceof Text) {
                @Nullable Rectangle2D inkBounds = renderedTextInkBounds(image, (Text) renderedGlyph);
                if (inkBounds != null
                        && shouldCheckRenderedTextInkCenter(node, (Text) renderedGlyph, inkBounds, glyphBounds)) {
                    double inkCenterX = inkBounds.getMinX() + inkBounds.getWidth() / 2.0;
                    double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
                    double pixelDx = Math.abs(targetBounds.getCenterX() - inkCenterX);
                    double pixelDy = Math.abs(targetBounds.getCenterY() - inkCenterY);
                    assertTrue(pixelDx <= DEMO_ICON_PIXEL_CENTER_TOLERANCE
                                    && pixelDy <= DEMO_ICON_PIXEL_CENTER_TOLERANCE,
                            () -> pageTitle + " fixed target rendered ink is off-center: target="
                                    + node + ", glyph=" + renderedGlyph + ", pixelDx=" + pixelDx
                                    + ", pixelDy=" + pixelDy + ", targetBounds=" + targetBounds
                                    + ", inkBounds=" + inkBounds);
                    assertRectangleInsideBounds(
                            targetBounds,
                            inkBounds,
                            CONTROL_EDGE_TOLERANCE,
                            pageTitle + " fixed target rendered ink"
                    );
                }
            }

            @Nullable Node vectorIcon = firstVisibleMaterialVectorIcon(node);
            if (vectorIcon != null && hasRenderableBounds(vectorIcon)) {
                assertVectorIconCenteredInContainer(node, vectorIcon, pageTitle + " fixed target vector icon");
            }
        });
    }

    /// Returns whether a fixed target should always expose visible centered glyph content.
    private static boolean shouldRequireCenteredGlyph(Node target) {
        if (target.getStyleClass().contains("demo-seed-button")) {
            return false;
        }
        if (target instanceof M3IconButton || target instanceof M3IconToggleButton) {
            return true;
        }
        return target instanceof Labeled labeled && !Objects.toString(labeled.getText(), "").isBlank();
    }

    /// Returns whether extracted text ink bounds represent enough of the glyph for pixel-centering checks.
    private static boolean textInkBoundsAreRepresentative(Rectangle2D inkBounds, Bounds glyphBounds) {
        return inkBounds.getWidth() >= Math.max(2.0, glyphBounds.getWidth() * 0.35)
                && inkBounds.getHeight() >= Math.max(2.0, glyphBounds.getHeight() * 0.25);
    }

    /// Returns whether text ink centering is meaningful for a rendered fixed target.
    private static boolean shouldCheckRenderedTextInkCenter(
            Node target,
            Text renderedText,
            Rectangle2D inkBounds,
            Bounds glyphBounds
    ) {
        if (!textInkBoundsAreRepresentative(inkBounds, glyphBounds)) {
            return false;
        }

        String text = renderedText.getText().strip();
        if (target instanceof M3SegmentedButton && text.length() > 1) {
            return false;
        }
        if (text.length() > 1 && !isNumericTargetText(text)) {
            return false;
        }

        return true;
    }

    /// Returns whether a short text target is numeric enough for rendered-ink center checks.
    private static boolean isNumericTargetText(String text) {
        for (int index = 0; index < text.length(); index++) {
            if (!Character.isDigit(text.charAt(index))) {
                return false;
            }
        }
        return !text.isEmpty();
    }

    /// Verifies that navigation item indicator and icon slots share stable centers.
    private static void assertNavigationItemIconSlotsCentered(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3NavigationItem item) || !hasRenderableBounds(item)) {
                return;
            }

            @Nullable Node iconContainer = item.lookup(".m3-navigation-item-icon-container");
            if (iconContainer == null || !hasRenderableBounds(iconContainer)) {
                return;
            }

            Bounds iconContainerBounds = iconContainer.localToScene(iconContainer.getLayoutBounds());
            if (isOutsideSceneViewport(iconContainer, iconContainerBounds, sceneBounds)
                    || isClippedAtScrollViewportEdge(iconContainer, iconContainerBounds)) {
                return;
            }

            @Nullable Node indicator = item.lookup(".m3-navigation-item-indicator");
            if (indicator != null && hasRenderableBounds(indicator)) {
                assertNodeCentersAligned(
                        iconContainer,
                        indicator,
                        DEMO_ICON_CENTER_TOLERANCE,
                        pageTitle + " navigation indicator"
                );
            }

            if (item.getGraphic() == null) {
                return;
            }

            @Nullable Node graphicContainer = item.lookup(".m3-navigation-item-graphic");
            if (graphicContainer == null || !hasRenderableBounds(graphicContainer)) {
                return;
            }

            assertNodeCentersAligned(
                    iconContainer,
                    graphicContainer,
                    DEMO_ICON_CENTER_TOLERANCE,
                    pageTitle + " navigation graphic slot"
            );

            @Nullable Node vectorIcon = firstVisibleDemoVectorIcon(graphicContainer);
            if (vectorIcon != null && hasRenderableBounds(vectorIcon)) {
                assertVectorIconCenteredInContainer(iconContainer, vectorIcon, pageTitle + " navigation vector icon");
                return;
            }

            @Nullable Text text = firstVisibleText(graphicContainer);
            if (text != null && hasRenderableBounds(text)) {
                assertNodeCentersAligned(
                        iconContainer,
                        text,
                        DEMO_ICON_CENTER_TOLERANCE,
                        pageTitle + " navigation text icon"
                );
            }
        });
    }

    /// Returns whether a node is partially clipped by its nearest scroll viewport edge.
    private static boolean isClippedAtScrollViewportEdge(Node node, Bounds nodeBounds) {
        @Nullable Node viewport = nearestScrollViewport(node);
        if (viewport == null) {
            return false;
        }

        Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        return !viewportBounds.contains(nodeBounds.getCenterX(), nodeBounds.getCenterY())
                || touchesVerticalViewportEdge(nodeBounds, viewportBounds, CONTROL_EDGE_TOLERANCE);
    }

    /// Verifies that a demo vector icon is visually centered inside a target container.
    private static void assertVectorIconCenteredInContainer(Node container, Node vectorIcon, String description) {
        @Nullable Parent viewport = vectorIconViewportFor(vectorIcon);
        if (viewport == null) {
            assertNodeCentersAligned(container, vectorIcon, DEMO_ICON_CENTER_TOLERANCE, description);
            return;
        }

        assertNodeCentersAligned(container, viewport, DEMO_ICON_CENTER_TOLERANCE, description + " viewport");
        Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
        Bounds iconBounds = vectorIcon.localToScene(vectorIcon.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(viewportBounds, iconBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " path leaves its stable viewport: viewport="
                        + viewportBounds + ", icon=" + iconBounds);
        assertVectorIconPixelsCenteredInViewport(viewport, description);
    }

    /// Verifies that a demo icon viewport renders visible pixels around the viewport center.
    private static void assertVectorIconPixelsCenteredInViewport(Node viewport, String description) {
        WritableImage image = snapshotNode(viewport);
        Color background = image.getPixelReader().getColor(0, 0);
        Rectangle2D pixels = contrastingPixelBounds(
                image,
                background,
                0.02,
                description + " rendered icon"
        );
        double centerX = pixels.getMinX() + pixels.getWidth() / 2.0;
        double centerY = pixels.getMinY() + pixels.getHeight() / 2.0;
        double expectedCenterX = image.getWidth() / 2.0;
        double expectedCenterY = image.getHeight() / 2.0;
        assertEquals(expectedCenterX, centerX, DEMO_ICON_PIXEL_CENTER_TOLERANCE,
                () -> description + " rendered pixels are horizontally off-center in the icon viewport: pixels="
                        + pixels + ", imageSize=" + image.getWidth() + "x" + image.getHeight());
        assertEquals(expectedCenterY, centerY, DEMO_ICON_PIXEL_CENTER_TOLERANCE,
                () -> description + " rendered pixels are vertically off-center in the icon viewport: pixels="
                        + pixels + ", imageSize=" + image.getWidth() + "x" + image.getHeight());
    }

    /// Returns the stable viewport that owns a vector icon.
    private static @Nullable Parent vectorIconViewportFor(Node vectorIcon) {
        @Nullable Parent viewport = nearestAncestorWithStyle(vectorIcon, "demo-vector-icon-viewport");
        if (viewport != null) {
            return viewport;
        }
        viewport = nearestAncestorWithStyle(vectorIcon, "demo-sample-icon");
        if (viewport != null) {
            return viewport;
        }
        return nearestAncestorWithStyle(vectorIcon, "m3-internal-icon");
    }

    /// Verifies that two node layout centers align in scene coordinates.
    private static void assertNodeCentersAligned(Node container, Node content, double tolerance, String description) {
        Bounds containerBounds = container.localToScene(container.getLayoutBounds());
        Bounds contentBounds = content.localToScene(content.getLayoutBounds());
        double dx = Math.abs(containerBounds.getCenterX() - contentBounds.getCenterX());
        double dy = Math.abs(containerBounds.getCenterY() - contentBounds.getCenterY());
        assertTrue(dx <= tolerance && dy <= tolerance,
                () -> description + " is off-center: container=" + container
                        + ", content=" + content + ", dx=" + dx + ", dy=" + dy
                        + ", containerBounds=" + containerBounds + ", contentBounds=" + contentBounds);
    }

    /// Returns the node whose center represents a Material vector icon target.
    private static @Nullable Node centeredMaterialVectorIconNode(Node root) {
        @Nullable Node icon = firstVisibleMaterialVectorIcon(root);
        if (icon == null) {
            return null;
        }

        @Nullable Parent viewport = vectorIconViewportFor(icon);
        return viewport == null ? icon : viewport;
    }

    /// Verifies that single-line text input glyphs have visible vertical room inside their field containers.
    private static void assertSingleLineTextInputsHaveVerticalRoom(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        WritableImage image = snapshot(scene);
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3TextInputLayout layout) || !hasRenderableBounds(layout)) {
                return;
            }

            TextInputControl input = layout.getInput();
            if (input == null || input instanceof M3TextArea || !input.isVisible() || !hasRenderableBounds(input)) {
                return;
            }

            @Nullable Text text = firstVisibleText(input);
            if (text == null || !hasRenderableBounds(text)) {
                return;
            }

            Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            if (isOutsideSceneViewport(text, textBounds, sceneBounds)) {
                return;
            }

            @Nullable Rectangle2D renderedInkBounds = renderedTextInkBounds(image, text);
            assertNotNull(renderedInkBounds,
                    () -> pageTitle + " text input glyph should be measurable in the real scene snapshot: text="
                            + text.getText() + ", inputBounds=" + inputBounds + ", textBounds=" + textBounds);
            Rectangle2D inkBounds = Objects.requireNonNull(renderedInkBounds);
            assertTrue(renderedTextInkWidthIsRepresentative(inkBounds, textBounds)
                            && renderedTextInkHeightIsRepresentative(inkBounds, text),
                    () -> pageTitle + " text input glyph snapshot is not representative enough for geometry checks: text="
                            + text.getText() + ", inputBounds=" + inputBounds + ", textBounds=" + textBounds
                            + ", inkBounds=" + inkBounds);
            assertRectangleInsideBounds(
                    inputBounds,
                    inkBounds,
                    TEXT_EDGE_TOLERANCE,
                    pageTitle + " text input rendered glyph"
            );
            double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
            double topRoom = inkBounds.getMinY() - inputBounds.getMinY();
            double bottomRoom = inputBounds.getMaxY() - inkBounds.getMaxY();
            double centerRatio = (inkCenterY - inputBounds.getMinY()) / inputBounds.getHeight();
            assertTrue(topRoom >= INPUT_TEXT_MINIMUM_VERTICAL_ROOM
                            && bottomRoom >= INPUT_TEXT_MINIMUM_VERTICAL_ROOM
                            && centerRatio >= INPUT_TEXT_MINIMUM_CENTER_RATIO
                            && centerRatio <= INPUT_TEXT_MAXIMUM_CENTER_RATIO,
                    () -> pageTitle + " text input glyph has unsafe vertical geometry: text="
                            + text.getText() + ", topRoom=" + topRoom + ", bottomRoom=" + bottomRoom
                            + ", centerRatio=" + centerRatio + ", inputBounds=" + inputBounds
                            + ", textBounds=" + textBounds + ", inkBounds=" + inkBounds);
            if (isSingleLineM3TextInputWithVisibleText(input)) {
                assertSingleLineTextInputInkAligned(layout, input, inkBounds, inputBounds, pageTitle);
            }
        });
    }

    /// Returns whether a single-line M3 text input should check rendered ink alignment.
    private static boolean isSingleLineM3TextInputWithVisibleText(TextInputControl input) {
        return input instanceof M3TextInput textInput
                && (textInput.getVariant() == M3TextInputVariant.FILLED
                        || textInput.getVariant() == M3TextInputVariant.OUTLINED)
                && input.getText() != null
                && !input.getText().isBlank();
    }

    /// Verifies that a single-line text input's rendered ink center matches its active content slot.
    private static void assertSingleLineTextInputInkAligned(
            M3TextInputLayout layout,
            TextInputControl input,
            Rectangle2D inkBounds,
            Bounds inputBounds,
            String pageTitle
    ) {
        double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
        M3TextInput textInput = assertInstanceOf(M3TextInput.class, input);
        if (layout.isLabelFloating() && textInput.getVariant() == M3TextInputVariant.FILLED) {
            double centerRatio = (inkCenterY - inputBounds.getMinY()) / inputBounds.getHeight();
            assertTrue(centerRatio >= FILLED_FLOATING_INK_MINIMUM_CENTER_RATIO
                            && centerRatio <= FILLED_FLOATING_INK_MAXIMUM_CENTER_RATIO,
                    () -> pageTitle + " filled floating-label text input ink is outside its content slot: input="
                            + input + ", inputBounds=" + inputBounds + ", inkBounds=" + inkBounds
                            + ", centerRatio=" + centerRatio);
            return;
        }
        if (layout.isLabelFloating() && textInput.getVariant() == M3TextInputVariant.OUTLINED) {
            double centerRatio = (inkCenterY - inputBounds.getMinY()) / inputBounds.getHeight();
            assertTrue(centerRatio >= OUTLINED_FLOATING_INK_MINIMUM_CENTER_RATIO
                            && centerRatio <= OUTLINED_FLOATING_INK_MAXIMUM_CENTER_RATIO,
                    () -> pageTitle + " outlined floating-label text input ink is outside its content slot: input="
                            + input + ", inputBounds=" + inputBounds + ", inkBounds=" + inkBounds
                            + ", centerRatio=" + centerRatio);
            return;
        }

        double inputCenterY = inputBounds.getCenterY();
        assertTrue(Math.abs(inputCenterY - inkCenterY) <= TEXT_INPUT_INK_CENTER_TOLERANCE,
                () -> pageTitle + " single-line text input ink is vertically off-center: input="
                        + input + ", inputBounds=" + inputBounds + ", inkBounds=" + inkBounds
                        + ", delta=" + Math.abs(inputCenterY - inkCenterY));
    }

    /// Verifies the real Components Overview demo page list structure.
    private static void assertComponentsOverviewPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Components Overview");
        assertCurrentPageTitle(scene, "Components Overview");
        assertVisibleText(root, "Primary Components", "Components Overview");
        assertVisibleText(root, "Feedback And Containers", "Components Overview");
        assertVisibleText(root, "Text fields", "Components Overview");
        assertVisibleText(root, "Loading & progress", "Components Overview");

        List<M3ListPane> lists = visibleNodesOfType(page, M3ListPane.class);
        assertEquals(2, lists.size(), () -> "Components Overview should render two overview lists: " + lists);
        List<M3ListItem> items = visibleNodesOfType(page, M3ListItem.class);
        assertEquals(9, items.size(), () -> "Components Overview should render nine overview items: " + items);
        assertTrue(items.stream().allMatch(item -> !item.getSupportingText().isBlank()),
                "overview list items should expose supporting descriptions");
    }

    /// Verifies the real App Bars demo page variant matrix and slot geometry.
    private static void assertAppBarsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "App Bars");
        assertCurrentPageTitle(scene, "App Bars");
        assertVisibleText(root, "Top App Bars", "App Bars");
        assertVisibleText(root, "Scrolled Under", "App Bars");

        List<M3TopAppBar> appBars = visibleNodesOfType(page, M3TopAppBar.class);
        assertEquals(6, appBars.size(), () -> "App Bars page should render six top app bars: " + appBars);
        assertEquals(M3TopAppBarVariant.SMALL, appBars.get(0).getVariant(), "small app bar variant");
        assertEquals(M3TopAppBarVariant.CENTER_ALIGNED, appBars.get(1).getVariant(), "center app bar variant");
        assertEquals(M3TopAppBarVariant.MEDIUM, appBars.get(2).getVariant(), "medium app bar variant");
        assertEquals(M3TopAppBarVariant.LARGE, appBars.get(3).getVariant(), "large app bar variant");
        assertTrue(appBars.get(4).isScrolledUnder(), "small scrolled app bar should set scrolled-under state");
        assertTrue(appBars.get(5).isScrolledUnder(), "medium scrolled app bar should set scrolled-under state");
        for (M3TopAppBar appBar : appBars) {
            assertAppBarFitsPreview(appBar, "App Bars " + appBar.getVariant());
            assertTopAppBarPreviewBalance(appBar);
            assertTopAppBarSlotGeometry(appBar);
        }
        assertDemoVectorIcons(page, "App Bars", 18);
    }

    /// Verifies the real Bottom App Bars demo page floating-action alignments and slot geometry.
    private static void assertBottomAppBarsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Bottom App Bars");
        assertCurrentPageTitle(scene, "Bottom App Bars");
        assertVisibleText(root, "Floating Action", "Bottom App Bars");

        List<M3BottomAppBar> appBars = visibleNodesOfType(page, M3BottomAppBar.class);
        assertEquals(3, appBars.size(), () -> "Bottom App Bars page should render three bottom app bars: " + appBars);
        assertEquals(M3BottomAppBarFloatingActionAlignment.END, appBars.get(0).getFloatingActionAlignment());
        assertEquals(M3BottomAppBarFloatingActionAlignment.CENTER, appBars.get(1).getFloatingActionAlignment());
        assertEquals(M3BottomAppBarFloatingActionAlignment.START, appBars.get(2).getFloatingActionAlignment());
        for (M3BottomAppBar appBar : appBars) {
            assertBottomAppBarPreviewBalance(appBar);
            assertBottomAppBarSlotGeometry(appBar);
        }
        assertDemoVectorIcons(page, "Bottom App Bars", 9);
    }

    /// Verifies the real Segmented Buttons demo page selection modes and segment states.
    private static void assertSegmentedButtonsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Segmented Buttons");
        assertCurrentPageTitle(scene, "Segmented Buttons");
        assertVisibleText(root, "Date Range", "Segmented Buttons");
        assertVisibleText(root, "Availability", "Segmented Buttons");
        assertVisibleText(root, "Multi Select", "Segmented Buttons");

        List<M3SegmentedButtonGroup> groups = visibleNodesOfType(page, M3SegmentedButtonGroup.class);
        assertEquals(3, groups.size(), () -> "Segmented Buttons page should render three groups: " + groups);
        assertEquals(M3SegmentedButtonSelectionMode.SINGLE, groups.get(0).getSelectionMode());
        assertEquals(M3SegmentedButtonSelectionMode.SINGLE, groups.get(1).getSelectionMode());
        assertEquals(M3SegmentedButtonSelectionMode.MULTIPLE, groups.get(2).getSelectionMode());
        assertEquals(1, groups.get(0).getSelectedButtons().size(), "date range selected count");
        assertEquals(1, groups.get(1).getSelectedButtons().size(), "availability selected count");
        assertEquals(2, groups.get(2).getSelectedButtons().size(), "channel selected count");
        WritableImage image = snapshot(scene);
        for (int index = 0; index < groups.size(); index++) {
            M3SegmentedButtonGroup group = groups.get(index);
            assertEquals(3, group.getItems().size(), "segmented group item count");
            assertSegmentedButtonGroupDemoGeometry(group, "segmented group " + index);
        }

        List<M3SegmentedButton> buttons = visibleNodesOfType(page, M3SegmentedButton.class);
        assertEquals(9, buttons.size(), () -> "Segmented Buttons page should render nine segments: " + buttons);
        assertEquals(4, buttons.stream().filter(M3SegmentedButton::isSelected).count(),
                "Segmented Buttons page should render four selected segments");
        assertEquals(1, buttons.stream().filter(Node::isDisabled).count(),
                "Segmented Buttons page should render one disabled segment");
        for (M3SegmentedButton button : buttons) {
            assertSegmentedButtonDemoGeometry(image, button);
        }
        assertFixedTargetGlyphsCentered(scene, "Segmented Buttons");
    }

    /// Verifies a demo segmented button group keeps child segments contiguous and physically rounded.
    private static void assertSegmentedButtonGroupDemoGeometry(
            M3SegmentedButtonGroup group,
            String description
    ) {
        Bounds groupBounds = group.localToScene(group.getBoundsInLocal());
        List<M3SegmentedButton> buttons = visibleNodesOfType(group, M3SegmentedButton.class);
        assertFalse(buttons.isEmpty(), () -> description + " should render segmented buttons");
        long itemButtonCount = group.getItems().stream()
                .filter(M3SegmentedButton.class::isInstance)
                .count();
        assertEquals(itemButtonCount, buttons.size(),
                () -> description + " rendered button count should match the item list");
        assertEquals(group.getSelectedButtons().size(), buttons.stream().filter(M3SegmentedButton::isSelected).count(),
                () -> description + " selected child count should match rendered child state");

        double expectedHeight = buttons.get(0).getContainerHeight();
        for (M3SegmentedButton button : buttons) {
            Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(groupBounds, buttonBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " child escapes group bounds: group="
                            + groupBounds + ", button=" + buttonBounds + ", text=" + button.getText());
            assertEquals(expectedHeight, buttonBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " child height does not match the first segment token: button="
                            + buttonBounds + ", expectedHeight=" + expectedHeight);
            assertEquals(groupBounds.getCenterY(), buttonBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " child is vertically misaligned in the group: group="
                            + groupBounds + ", button=" + buttonBounds);
        }

        List<M3SegmentedButton> physicalOrder = buttons.stream()
                .sorted(java.util.Comparator.comparingDouble(button ->
                        button.localToScene(button.getBoundsInLocal()).getMinX()))
                .toList();
        assertSegmentedButtonPhysicalPositionClasses(physicalOrder, description);
        assertSegmentedButtonAdjacency(physicalOrder, group.getSpacing(), description);
    }

    /// Verifies the physical edge style classes of a segmented button group.
    private static void assertSegmentedButtonPhysicalPositionClasses(
            List<M3SegmentedButton> physicalOrder,
            String description
    ) {
        if (physicalOrder.size() == 1) {
            assertTrue(physicalOrder.get(0).getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS),
                    () -> description + " single segment should use the single segment style class");
            return;
        }

        M3SegmentedButton left = physicalOrder.get(0);
        M3SegmentedButton right = physicalOrder.get(physicalOrder.size() - 1);
        assertTrue(left.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS),
                () -> description + " physical left segment should use the first-segment style class: "
                        + left.getStyleClass());
        assertTrue(right.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS),
                () -> description + " physical right segment should use the last-segment style class: "
                        + right.getStyleClass());
        for (int index = 1; index < physicalOrder.size() - 1; index++) {
            M3SegmentedButton middle = physicalOrder.get(index);
            assertTrue(middle.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS),
                    () -> description + " physical middle segment should use the middle-segment style class: "
                            + middle.getStyleClass());
        }
    }

    /// Verifies adjacent segmented buttons overlap by the configured border-sharing spacing.
    private static void assertSegmentedButtonAdjacency(
            List<M3SegmentedButton> physicalOrder,
            double expectedSpacing,
            String description
    ) {
        for (int index = 1; index < physicalOrder.size(); index++) {
            M3SegmentedButton previous = physicalOrder.get(index - 1);
            M3SegmentedButton current = physicalOrder.get(index);
            Bounds previousBounds = previous.localToScene(previous.getBoundsInLocal());
            Bounds currentBounds = current.localToScene(current.getBoundsInLocal());
            double actualSpacing = currentBounds.getMinX() - previousBounds.getMaxX();
            assertEquals(expectedSpacing, actualSpacing, 1.25,
                    () -> description + " adjacent segments should share their outline border: previous="
                            + previousBounds + ", current=" + currentBounds
                            + ", expectedSpacing=" + expectedSpacing + ", actualSpacing=" + actualSpacing);
            assertEquals(previousBounds.getMinY(), currentBounds.getMinY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent segment tops are misaligned: previous="
                            + previousBounds + ", current=" + currentBounds);
            assertEquals(previousBounds.getMaxY(), currentBounds.getMaxY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent segment bottoms are misaligned: previous="
                            + previousBounds + ", current=" + currentBounds);
        }
    }

    /// Verifies one demo segmented button's rendered outline, selection fill, and text geometry.
    private static void assertSegmentedButtonDemoGeometry(WritableImage image, M3SegmentedButton button) {
        String description = "segmented button `" + button.getText() + "`";
        Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());
        assertNodeSnapshotHasOpaquePixels(button, description);
        assertEquals(button.getContainerHeight(), buttonBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " height should match its resolved container token: bounds="
                        + buttonBounds + ", token=" + button.getContainerHeight());
        assertTrue(buttonBounds.getWidth() > buttonBounds.getHeight(),
                () -> description + " should be wider than tall: " + buttonBounds);

        Color surroundingColor = sampleColorOutsideBounds(image, buttonBounds);
        Rectangle2D visualPixels = contrastingPixelBounds(
                image,
                button,
                surroundingColor,
                button.isDisabled() ? 0.012 : 0.02
        );
        double edgeTolerance = button.isDisabled() ? 5.0 : 4.0;
        assertTrue(visualPixels.getMinX() <= buttonBounds.getMinX() + edgeTolerance
                        && visualPixels.getMaxX() >= buttonBounds.getMaxX() - edgeTolerance
                        && visualPixels.getMinY() <= buttonBounds.getMinY() + edgeTolerance
                        && visualPixels.getMaxY() >= buttonBounds.getMaxY() - edgeTolerance,
                () -> description + " rendered outline or selected fill does not cover the segment container: pixels="
                        + visualPixels + ", bounds=" + buttonBounds);

        assertSegmentedButtonSelectionContainerGeometry(button, buttonBounds, description);
        assertSegmentedButtonInteriorFillState(image, button, buttonBounds, surroundingColor, description);
        assertSegmentedButtonTextInkGeometry(image, button, buttonBounds, description);
    }

    /// Verifies that a selected segmented button keeps its selected container inside the outline stroke.
    private static void assertSegmentedButtonSelectionContainerGeometry(
            M3SegmentedButton button,
            Bounds buttonBounds,
            String description
    ) {
        @Nullable Node selectionContainer =
                firstVisibleStyledDescendant(button, "m3-segmented-button-selection-container");
        assertNotNull(selectionContainer, () -> description + " should expose a selected-container layer");
        Node actualContainer = Objects.requireNonNull(selectionContainer);

        if (!button.isSelected()) {
            assertTrue(actualContainer.getOpacity() <= 0.05,
                    () -> description + " unselected container should be transparent: opacity="
                            + actualContainer.getOpacity());
            return;
        }

        Bounds containerBounds = actualContainer.localToScene(actualContainer.getBoundsInLocal());
        assertTrue(actualContainer.getOpacity() >= 0.95,
                () -> description + " selected container should be fully visible: opacity="
                        + actualContainer.getOpacity());
        assertTrue(containerBounds.getMinX() >= buttonBounds.getMinX() + 0.5
                        && containerBounds.getMaxX() <= buttonBounds.getMaxX() - 0.5
                        && containerBounds.getMinY() >= buttonBounds.getMinY() + 0.5
                        && containerBounds.getMaxY() <= buttonBounds.getMaxY() - 0.5,
                () -> description + " selected container should stay inside the outline stroke: container="
                        + containerBounds + ", button=" + buttonBounds);
        assertEquals(buttonBounds.getCenterY(), containerBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                () -> description + " selected container is vertically off-center: container="
                        + containerBounds + ", button=" + buttonBounds);
        assertTrue(containerBounds.getHeight() >= buttonBounds.getHeight() - 4.0,
                () -> description + " selected container is too short for the outlined segment: container="
                        + containerBounds + ", button=" + buttonBounds);
    }

    /// Verifies that selected segmented buttons render a fill and unselected segments keep a transparent center.
    private static void assertSegmentedButtonInteriorFillState(
            WritableImage image,
            M3SegmentedButton button,
            Bounds buttonBounds,
            Color surroundingColor,
            String description
    ) {
        Color interiorColor = snapshotColorAt(
                image,
                buttonBounds.getCenterX(),
                buttonBounds.getMinY() + Math.max(4.0, buttonBounds.getHeight() * 0.25)
        );
        double distance = pixelDistance(interiorColor, surroundingColor);
        if (button.isSelected()) {
            assertTrue(distance >= 0.03,
                    () -> description + " selected segment interior is too close to the surrounding surface: interior="
                            + interiorColor + ", surrounding=" + surroundingColor + ", distance=" + distance);
        } else if (!button.isDisabled()) {
            assertTrue(distance <= 0.14,
                    () -> description + " unselected segment interior looks filled instead of transparent: interior="
                            + interiorColor + ", surrounding=" + surroundingColor + ", distance=" + distance);
        }
    }

    /// Verifies one segmented button's rendered label ink stays inside and vertically centered.
    private static void assertSegmentedButtonTextInkGeometry(
            WritableImage image,
            M3SegmentedButton button,
            Bounds buttonBounds,
            String description
    ) {
        Text text = Objects.requireNonNull(firstVisibleText(button), description + " text node");
        Rectangle2D inkBounds = Objects.requireNonNull(
                renderedTextInkBounds(image, text),
                description + " rendered text ink"
        );
        assertRectangleInsideBounds(buttonBounds, inkBounds, CONTROL_EDGE_TOLERANCE,
                description + " rendered text ink");
        double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
        double deltaY = Math.abs(buttonBounds.getCenterY() - inkCenterY);
        assertTrue(deltaY <= LABELED_TEXT_INK_VERTICAL_CENTER_TOLERANCE,
                () -> description + " rendered text ink is vertically off-center: deltaY="
                        + deltaY + ", button=" + buttonBounds + ", ink=" + inkBounds);
    }

    /// Verifies the real Cards demo page variants, media rows, and actionable states.
    private static void assertCardsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Cards");
        assertCurrentPageTitle(scene, "Cards");
        assertVisibleText(root, "Variants", "Cards");
        assertVisibleText(root, "Media And Actions", "Cards");

        List<M3Card> cards = visibleNodesOfType(page, M3Card.class);
        assertEquals(6, cards.size(), "Cards page should render six card examples");
        assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.FILLED).count());
        assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.OUTLINED).count());
        assertEquals(2, cards.stream().filter(card -> card.getVariant() == M3CardVariant.ELEVATED).count());
        assertEquals(1, cards.stream().filter(Node::isDisabled).count(), "Cards page disabled card count");
        assertTrue(cards.stream().allMatch(card -> card.getOnAction() != null),
                "demo cards should exercise actionable surface semantics");
        assertEquals(3, visibleNodesWithStyle(page, "demo-card-media").size(),
                "Cards page should render three media rows");
        assertEquals(3, visibleNodesWithStyle(page, "demo-card-actions").size(),
                "Cards page should render three action rows");
        for (M3Card card : cards) {
            assertCardDemoGeometry(card);
            assertNotNull(firstVisibleStyledDescendant(card, "demo-card-title"),
                    "each demo card should render a title");
            assertNotNull(firstVisibleStyledDescendant(card, "demo-card-body"),
                    "each demo card should render body text");
        }
    }

    /// Verifies the real Carousel demo page item counts, selection, and viewport geometry.
    private static void assertCarouselPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Carousel");
        assertCurrentPageTitle(scene, "Carousel");
        assertVisibleText(root, "Multi-browse", "Carousel");
        assertVisibleText(root, "Compact", "Carousel");
        assertVisibleText(root, "Previous", "Carousel");
        assertVisibleText(root, "Next", "Carousel");

        List<M3Carousel> carousels = visibleNodesOfType(page, M3Carousel.class);
        assertEquals(2, carousels.size(), () -> "Carousel page should render two carousel variants: " + carousels);
        M3Carousel multiBrowse = carousels.stream()
                .filter(carousel -> carousel.getItems().size() == 5)
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing multi-browse carousel"));
        M3Carousel compact = carousels.stream()
                .filter(carousel -> carousel.getItems().size() == 4)
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing compact carousel"));
        assertEquals(1, multiBrowse.getSelectedIndex(), "multi-browse carousel selected index");
        assertEquals(0, compact.getSelectedIndex(), "compact carousel selected index");
        assertCarouselDemoGeometry(multiBrowse, "multi-browse carousel");
        assertCarouselDemoGeometry(compact, "compact carousel");
    }

    /// Verifies the real Chips demo page variants, groups, and selected states.
    private static void assertChipsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Chips");
        assertCurrentPageTitle(scene, "Chips");
        assertVisibleText(root, "Variants", "Chips");
        assertVisibleText(root, "States", "Chips");
        assertVisibleText(root, "Multi Select", "Chips");
        assertVisibleText(root, "Single Select", "Chips");

        List<M3Chip> chips = visibleNodesOfType(page, M3Chip.class);
        assertEquals(13, chips.size(), () -> "Chips page should render thirteen chips: " + chips);
        assertChipVariantCount(chips, M3ChipVariant.ASSIST, 2, "Chips");
        assertChipVariantCount(chips, M3ChipVariant.SUGGESTION, 1, "Chips");
        assertChipVariantCount(chips, M3ChipVariant.INPUT, 1, "Chips");
        assertChipVariantCount(chips, M3ChipVariant.FILTER, 9, "Chips");
        assertEquals(4, chips.stream().filter(M3Chip::isSelected).count(), "Chips selected count");
        assertEquals(1, chips.stream().filter(Node::isDisabled).count(), "Chips disabled count");

        WritableImage image = snapshot(scene);
        for (M3Chip chip : chips) {
            assertChipDemoGeometry(image, chip);
        }

        List<M3ChipGroup> groups = visibleNodesOfType(page, M3ChipGroup.class);
        assertEquals(2, groups.size(), () -> "Chips page should render two chip groups: " + groups);
        assertEquals(M3ChipSelectionMode.MULTIPLE, groups.get(0).getSelectionMode());
        assertEquals(M3ChipSelectionMode.SINGLE, groups.get(1).getSelectionMode());
        assertEquals(2, groups.get(0).getSelectedChips().size(), "multi-select chip count");
        assertEquals(1, groups.get(1).getSelectedChips().size(), "single-select chip count");
        for (M3ChipGroup group : groups) {
            assertChipGroupDemoGeometry(group);
        }
        assertDemoVectorIcons(page, "Chips", 1);
    }

    /// Verifies the real Dialogs demo page inline pane structure and embedded form controls.
    private static void assertDialogsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Dialogs");
        assertCurrentPageTitle(scene, "Dialogs");
        assertVisibleText(root, "Open basic", "Dialogs");
        assertVisibleText(root, "Open settings", "Dialogs");
        assertVisibleText(root, "Open destructive", "Dialogs");

        List<M3DialogPane> panes = visibleNodesOfType(page, M3DialogPane.class);
        assertEquals(3, panes.size(), "Dialogs page should render three inline dialog panes");
        assertEquals(1, visibleNodesOfType(page, M3TextInputLayout.class).stream()
                        .filter(layout -> nearestAncestorOfType(layout, M3DialogPane.class) != null)
                        .count(),
                "Dialogs page should include one form-like text input layout");
        assertEquals(1, visibleNodesOfType(page, M3Switch.class).stream()
                        .filter(toggle -> nearestAncestorOfType(toggle, M3DialogPane.class) != null)
                        .count(),
                "Dialogs page should include one settings switch");
        assertEquals(1, visibleNodesOfType(page, M3CheckBox.class).stream()
                        .filter(checkbox -> nearestAncestorOfType(checkbox, M3DialogPane.class) != null)
                        .count(),
                "Dialogs page should include one settings checkbox");
        assertEquals(1, visibleNodesOfType(page, ScrollPane.class).stream()
                        .filter(scrollPane -> scrollPane.getStyleClass().contains("demo-dialog-scroll-pane"))
                        .count(),
                "Dialogs page should include one scrollable body");
        for (M3DialogPane pane : panes) {
            assertDialogPaneDemoGeometry(scene, pane);
        }
    }

    /// Verifies the real Lists demo page static rows and virtualized list view.
    @SuppressWarnings("rawtypes")
    private static void assertListsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Lists");
        assertCurrentPageTitle(scene, "Lists");
        assertVisibleText(root, "Static Pane", "Lists");
        assertVisibleText(root, "Virtualized View", "Lists");
        assertVisibleText(root, "Selected item", "Lists");

        List<M3ListPane> listPanes = visibleNodesOfType(page, M3ListPane.class);
        assertEquals(1, listPanes.size(), () -> "Lists page should render one static list pane: " + listPanes);
        List<M3ListView> listViews = visibleNodesOfType(page, M3ListView.class);
        assertEquals(1, listViews.size(), () -> "Lists page should render one virtualized list view: " + listViews);
        M3ListView listView = listViews.get(0);
        assertEquals(240, listView.getItems().size(), "virtualized list item count");
        assertEquals(2, listView.getSelectedIndex(), "virtualized list selected index");
        assertEquals(72.0, listView.getFixedCellSize(), 0.0001, "virtualized list fixed cell size");
        assertNotNull(firstVisibleStyledDescendant(listView, "m3-list-view-flow"), "M3ListView should use VirtualFlow");
        assertTrue(visibleNodesOfType(page, M3ListItem.class).stream()
                        .anyMatch(item -> item.isSelected() && "Selected item".equals(item.getHeadlineText())),
                "static list should render a selected Material list item");
    }

    /// Verifies the real Loading Indicator demo page default and contained variants.
    private static void assertLoadingIndicatorPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Loading Indicator");
        assertCurrentPageTitle(scene, "Loading Indicator");
        assertVisibleText(root, "Default", "Loading Indicator");
        assertVisibleText(root, "Contained", "Loading Indicator");

        List<M3LoadingIndicator> indicators = visibleNodesOfType(page, M3LoadingIndicator.class);
        assertEquals(2, indicators.size(),
                () -> "Loading Indicator page should render two indicator variants: " + indicators);
        assertEquals(M3LoadingIndicatorVariant.DEFAULT, indicators.get(0).getVariant(), "default indicator variant");
        assertEquals(M3LoadingIndicatorVariant.CONTAINED, indicators.get(1).getVariant(), "contained indicator variant");
        assertTrue(indicators.stream().allMatch(M3LoadingIndicator::isIndeterminate),
                "loading indicators should be indeterminate in the demo");

        WritableImage image = snapshot(scene);
        for (M3LoadingIndicator indicator : indicators) {
            Bounds bounds = indicator.localToScene(indicator.getBoundsInLocal());
            assertEquals(indicator.getContainerSize(), bounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    "loading indicator width");
            assertEquals(indicator.getContainerSize(), bounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    "loading indicator height");
            assertNodeSnapshotHasOpaquePixels(indicator, "loading indicator " + indicator.getVariant());
            assertLoadingIndicatorPageGeometry(image, indicator, "loading indicator " + indicator.getVariant());
        }
    }

    /// Verifies the real Progress demo page determinate, indeterminate, standard, and expressive samples.
    private static void assertProgressPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Progress");
        assertCurrentPageTitle(scene, "Progress");
        assertVisibleText(root, "Standard Linear", "Progress");
        assertVisibleText(root, "Expressive Wavy Linear", "Progress");
        assertVisibleText(root, "Track Heights", "Progress");
        assertVisibleText(root, "Circular expressive indeterminate", "Progress");

        List<M3ProgressBar> bars = visibleNodesOfType(page, M3ProgressBar.class);
        List<M3ProgressIndicator> indicators = visibleNodesOfType(page, M3ProgressIndicator.class);
        assertTrue(bars.size() >= 24, () -> "Progress page should render many linear samples, found " + bars.size());
        assertTrue(indicators.size() >= 24,
                () -> "Progress page should render many circular samples, found " + indicators.size());
        assertTrue(bars.stream().anyMatch(M3ProgressBar::isIndeterminate),
                "Progress page should render indeterminate bars");
        assertTrue(bars.stream().anyMatch(bar -> !bar.isIndeterminate() && bar.getProgress() > 0.0),
                "Progress page should render determinate bars");
        assertTrue(indicators.stream().anyMatch(M3ProgressIndicator::isIndeterminate),
                "Progress page should render indeterminate circular indicators");
        assertTrue(indicators.stream().anyMatch(indicator -> !indicator.isIndeterminate() && indicator.getProgress() > 0.0),
                "Progress page should render determinate circular indicators");
        for (int index = 0; index < bars.size(); index++) {
            assertProgressBarDemoGeometry(bars.get(index), "Progress linear sample " + index);
        }
        for (int index = 0; index < indicators.size(); index++) {
            assertProgressIndicatorDemoGeometry(indicators.get(index), "Progress circular sample " + index);
        }
    }

    /// Verifies the real Menus demo page inline surfaces, selection states, and menu button.
    private static void assertMenusPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Menus");
        assertCurrentPageTitle(scene, "Menus");
        assertVisibleText(root, "Menu Button", "Menus");
        assertVisibleText(root, "Inline Menus", "Menus");
        assertVisibleText(root, "Open menu", "Menus");
        assertDemoVectorIcons(page, "Menus", 10);

        List<M3Menu> inlineMenus = visibleNodesOfType(page, M3Menu.class);
        assertEquals(3, inlineMenus.size(), "Menus page should render three inline menu surfaces");
        M3Menu selectedMenu = requireMenuContainingText(inlineMenus, "Selected item");
        assertEquals(1, selectedMenu.getSelectedItems().size(), "single-selection inline menu selected count");
        assertEquals("Selected item",
                Objects.requireNonNull(selectedMenu.getSelectedItem(), "selected menu item").getHeadlineText());
        M3Menu multiSelectMenu = requireMenuContainingText(inlineMenus, "Icons");
        assertEquals(2, multiSelectMenu.getSelectedItems().size(), "multi-selection inline menu selected count");
        assertTrue(menuHasSelectedItem(multiSelectMenu, "Icons"), "Icons row should be selected");
        assertTrue(menuHasSelectedItem(multiSelectMenu, "Badges"), "Badges row should be selected");
        WritableImage image = snapshot(scene);
        for (M3Menu menu : inlineMenus) {
            assertMenuSurfaceGeometry(menu, image, "Menus page inline menu");
        }

        M3MenuButton menuButton = Objects.requireNonNull(
                firstVisibleMenuButtonWithText(page, "Open menu"),
                "Menus page menu button"
        );
        assertEquals(M3ButtonVariant.OUTLINED, menuButton.getVariant(), "menu button variant");
        assertEquals(7, menuButton.getItems().size(), "menu button item count including sections and divider");
    }

    /// Verifies the real Navigation demo page bars, selected destinations, and badge geometry.
    private static void assertNavigationPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Navigation");
        assertCurrentPageTitle(scene, "Navigation");
        assertVisibleText(root, "Four Items", "Navigation");
        assertVisibleText(root, "Three Items", "Navigation");

        List<M3NavigationBar> bars = visibleNodesOfType(page, M3NavigationBar.class);
        assertEquals(2, bars.size(), () -> "Navigation page should render two navigation bars: " + bars);
        assertEquals(4, bars.get(0).getItems().size(), "four-item navigation bar count");
        assertEquals(3, bars.get(1).getItems().size(), "three-item navigation bar count");
        assertEquals(0, bars.get(0).getSelectedIndex(), "four-item navigation selected index");
        assertEquals(0, bars.get(1).getSelectedIndex(), "three-item navigation selected index");
        assertEquals(2, visibleNodesOfType(page, M3NavigationItem.class).stream()
                        .filter(M3NavigationItem::isSelected)
                        .count(),
                "Navigation page should render two selected destinations");
        WritableImage image = snapshot(scene);
        for (int index = 0; index < bars.size(); index++) {
            assertNavigationBarDemoGeometry(bars.get(index), "navigation bar " + index);
        }
        visibleNodesOfType(page, M3NavigationItem.class).forEach(item ->
                assertNavigationItemDemoGeometry(image, item, "Navigation"));
        assertDemoVectorIcons(page, "Navigation", 7);
        assertNavigationItemIconSlotsCentered(scene, "Navigation");
        assertNavigationBadgesStayCompact(scene, "Navigation");
    }

    /// Verifies the real Navigation Drawer demo page drawer and grouped destination states.
    private static void assertNavigationDrawerPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Navigation Drawer");
        assertCurrentPageTitle(scene, "Navigation Drawer");
        assertVisibleText(root, "Destinations", "Navigation Drawer");
        assertVisibleText(root, "Section", "Navigation Drawer");
        assertVisibleText(root, "Workspace", "Navigation Drawer");

        List<M3NavigationDrawer> drawers = visibleNodesOfType(page, M3NavigationDrawer.class);
        assertEquals(2, drawers.size(), () -> "Navigation Drawer page should render two drawers: " + drawers);
        for (M3NavigationDrawer drawer : drawers) {
            assertNotNull(drawer.getSelectedItem(), "drawer selected item");
        }
        assertTrue(drawers.stream().anyMatch(drawer -> drawer.getSelectedIndex() == 0),
                "Navigation Drawer page should render one plain drawer selected at index 0");
        assertTrue(drawers.stream().anyMatch(drawer -> drawer.getSelectedIndex() == 1),
                "Navigation Drawer page should render one sectioned drawer selected after its section label");
        assertTrue(visibleNodesOfType(page, M3ListItem.class).stream().filter(M3ListItem::isSelected).count() >= 2,
                "Navigation Drawer page should render selected drawer rows");
        WritableImage image = snapshot(scene);
        for (M3NavigationDrawer drawer : drawers) {
            assertNavigationDrawerDemoGeometry(image, drawer);
        }
        assertDemoVectorIcons(page, "Navigation Drawer", 7);
    }

    /// Verifies one demo navigation drawer's selected row geometry.
    private static void assertNavigationDrawerDemoGeometry(WritableImage image, M3NavigationDrawer drawer) {
        Bounds drawerBounds = drawer.localToScene(drawer.getBoundsInLocal());
        assertTrue(drawerBounds.getWidth() >= 320.0,
                () -> "demo navigation drawer should be wide enough for Material drawer rows: " + drawerBounds);
        M3ListItem selected = Objects.requireNonNull(drawer.getSelectedItem(), "navigation drawer selected item");
        assertTrue(selected.isSelected(), () -> "drawer selected item should expose selected state: " + selected);
        assertListItemSelectedContainerGeometry(image, selected,
                "navigation drawer selected `" + selected.getHeadlineText() + "`");

        List<M3ListItem> selectedRows = visibleNodesOfType(drawer, M3ListItem.class).stream()
                .filter(M3ListItem::isSelected)
                .toList();
        assertEquals(1, selectedRows.size(),
                () -> "navigation drawer should render exactly one selected row: " + selectedRows);

        for (M3ListItem item : visibleNodesOfType(drawer, M3ListItem.class)) {
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(drawerBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "navigation drawer item escapes drawer bounds: drawer="
                            + drawerBounds + ", item=" + itemBounds + ", text=" + item.getHeadlineText());
            assertEquals(item.getOneLineHeight(), itemBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "navigation drawer item height should match its resolved one-line token: item="
                            + itemBounds + ", token=" + item.getOneLineHeight());
            if (!item.isSelected()) {
                Node selectionContainer = listItemSelectionContainer(item);
                assertTrue(selectionContainer.getOpacity() <= 0.05,
                        () -> "unselected navigation drawer item should not leave selected-container residue: item="
                                + item.getHeadlineText() + ", opacity=" + selectionContainer.getOpacity());
            }
        }
    }

    /// Verifies a selected list item's selected-container layout and real pixels.
    private static void assertListItemSelectedContainerGeometry(
            WritableImage image,
            M3ListItem item,
            String description
    ) {
        Node selectionContainer = listItemSelectionContainer(item);
        assertListItemSelectedContainerLayout(item, selectionContainer, description);
        assertListItemSelectedContainerPixels(image, selectionContainer, description);
    }

    /// Verifies a selected list item's selected-container layout.
    private static void assertListItemSelectedContainerLayout(
            M3ListItem item,
            Node selectionContainer,
            String description
    ) {
        Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
        Bounds containerBounds = selectionContainer.localToScene(selectionContainer.getBoundsInLocal());
        assertTrue(selectionContainer.getOpacity() >= 0.95
                        && selectionContainer.getScaleX() >= 0.99
                        && selectionContainer.getScaleY() >= 0.99,
                () -> description + " selected container should be fully visible: opacity="
                        + selectionContainer.getOpacity() + ", scaleX=" + selectionContainer.getScaleX()
                        + ", scaleY=" + selectionContainer.getScaleY());
        assertTrue(containsBoundsWithTolerance(itemBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " selected container escapes the row bounds: row="
                        + itemBounds + ", container=" + containerBounds);
        assertEquals(itemBounds.getWidth(), selectionContainer.getBoundsInLocal().getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " selected container local width should match the row width");
        assertEquals(itemBounds.getHeight(), selectionContainer.getBoundsInLocal().getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " selected container local height should match the row height");
        assertEquals(itemBounds.getCenterY(), containerBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                () -> description + " selected container should be vertically centered in the row: row="
                        + itemBounds + ", container=" + containerBounds);
    }

    /// Verifies a selected list item's selected-container rendered pixels.
    private static void assertListItemSelectedContainerPixels(
            WritableImage image,
            Node selectionContainer,
            String description
    ) {
        Bounds containerBounds = selectionContainer.localToScene(selectionContainer.getBoundsInLocal());
        Rectangle2D pixels = contrastingPixelBounds(
                image,
                selectionContainer,
                sampleColorOutsideBounds(image, containerBounds),
                0.018
        );
        assertRectangleInsideBounds(containerBounds, pixels, CONTROL_EDGE_TOLERANCE,
                description + " selected-container pixels");
        assertTrue(pixels.getWidth() >= containerBounds.getWidth() * 0.75,
                () -> description + " selected-container pixels are too narrow: pixels="
                        + pixels + ", container=" + containerBounds);
        assertTrue(pixels.getHeight() >= containerBounds.getHeight() * 0.55,
                () -> description + " selected-container pixels are too short: pixels="
                        + pixels + ", container=" + containerBounds);
    }

    /// Verifies that a group child selected row keeps the expected physical indentation.
    private static void assertDrawerGroupChildSelectionGeometry(
            M3NavigationDrawerGroup group,
            M3ListItem child,
            String description
    ) {
        Bounds headerBounds = group.getHeaderItem().localToScene(group.getHeaderItem().getBoundsInLocal());
        Bounds childBounds = child.localToScene(child.getBoundsInLocal());
        Bounds childSelectionBounds = listItemSelectionContainer(child)
                .localToScene(listItemSelectionContainer(child).getBoundsInLocal());
        assertEquals(headerBounds.getHeight(), childBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " row height should match the parent header: header="
                        + headerBounds + ", child=" + childBounds);
        assertEquals(childBounds.getWidth(), childSelectionBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " selected child pill should fill the indented child row: child="
                        + childBounds + ", selection=" + childSelectionBounds);
        if (group.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            assertEquals(headerBounds.getMinX(), childBounds.getMinX(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " RTL child row should align to the parent start edge: header="
                            + headerBounds + ", child=" + childBounds);
            assertTrue(childBounds.getMaxX() < headerBounds.getMaxX() - 6.0,
                    () -> description + " RTL child row should be indented from the parent end edge: header="
                            + headerBounds + ", child=" + childBounds);
        } else {
            assertEquals(headerBounds.getMaxX(), childBounds.getMaxX(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " child row should align to the parent end edge: header="
                            + headerBounds + ", child=" + childBounds);
            assertTrue(childBounds.getMinX() > headerBounds.getMinX() + 6.0,
                    () -> description + " child row should be indented from the parent start edge: header="
                            + headerBounds + ", child=" + childBounds);
        }
    }

    /// Returns the selected-container node for one list item.
    private static Node listItemSelectionContainer(M3ListItem item) {
        return requireVisibleStyledDescendant(
                item,
                "m3-list-item-selection-container",
                "list item selected container for `" + item.getHeadlineText() + "`"
        );
    }

    /// Verifies the real Navigation Rail demo page rail item counts and selected states.
    private static void assertNavigationRailPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Navigation Rail");
        assertCurrentPageTitle(scene, "Navigation Rail");
        assertVisibleText(root, "Four Items", "Navigation Rail");
        assertVisibleText(root, "Three Items", "Navigation Rail");

        List<M3NavigationRail> rails = visibleNodesOfType(page, M3NavigationRail.class);
        assertEquals(2, rails.size(), () -> "Navigation Rail page should render two rails: " + rails);
        assertEquals(4, rails.get(0).getItems().size(), "four-item navigation rail count");
        assertEquals(3, rails.get(1).getItems().size(), "three-item navigation rail count");
        assertEquals(0, rails.get(0).getSelectedIndex(), "four-item navigation rail selected index");
        assertEquals(0, rails.get(1).getSelectedIndex(), "three-item navigation rail selected index");
        assertEquals(2, visibleNodesOfType(page, M3NavigationItem.class).stream()
                        .filter(M3NavigationItem::isSelected)
                        .count(),
                "Navigation Rail page should render two selected destinations");
        WritableImage image = snapshot(scene);
        for (int index = 0; index < rails.size(); index++) {
            assertNavigationRailDemoGeometry(rails.get(index), "navigation rail " + index);
        }
        visibleNodesOfType(page, M3NavigationItem.class).forEach(item ->
                assertNavigationItemDemoGeometry(image, item, "Navigation Rail"));
        assertDemoVectorIcons(page, "Navigation Rail", 7);
        assertNavigationItemIconSlotsCentered(scene, "Navigation Rail");
        assertNavigationBadgesStayCompact(scene, "Navigation Rail");
    }

    /// Verifies the real Search demo page active, inactive, and standalone search controls.
    private static void assertSearchPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Search");
        assertCurrentPageTitle(scene, "Search");
        assertVisibleText(root, "Search Bars", "Search");
        assertVisibleText(root, "Search View", "Search");
        assertVisibleText(root, "Inactive View", "Search");

        List<M3SearchView> searchViews = visibleNodesOfType(page, M3SearchView.class);
        assertEquals(2, searchViews.size(), "Search page should render two search views");
        M3SearchView active = searchViews.stream()
                .filter(M3SearchView::isActive)
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing active search view"));
        M3SearchView inactive = searchViews.stream()
                .filter(searchView -> !searchView.isActive())
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing inactive search view"));
        assertSearchViewResultsVisible(active);
        assertSearchViewResultsHidden(inactive);
        assertActiveSearchViewDemoGeometry(active);
        assertInactiveSearchViewDemoGeometry(inactive);

        List<M3SearchBar> searchBars = visibleNodesOfType(page, M3SearchBar.class);
        assertEquals(4, searchBars.size(), "Search page should render two standalone and two embedded bars");
        for (M3SearchBar searchBar : searchBars) {
            assertSearchBarVisualGeometry(searchBar);
        }
    }

    /// Verifies the real Tabs demo page tab bars, selected tabs, and disabled state.
    private static void assertTabsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Tabs");
        assertCurrentPageTitle(scene, "Tabs");
        assertVisibleText(root, "Primary", "Tabs");
        assertVisibleText(root, "Disabled", "Tabs");

        List<M3TabBar> tabBars = visibleNodesOfType(page, M3TabBar.class);
        assertEquals(2, tabBars.size(), () -> "Tabs page should render two tab bars: " + tabBars);
        WritableImage image = snapshot(scene);
        for (int index = 0; index < tabBars.size(); index++) {
            M3TabBar tabBar = tabBars.get(index);
            assertEquals(3, tabBar.getTabs().size(), "tab bar item count");
            assertEquals(0, tabBar.getSelectedIndex(), "tab bar selected index");
            assertTabBarDemoGeometry(tabBar, "tab bar " + index);
        }
        List<M3Tab> tabs = visibleNodesOfType(page, M3Tab.class);
        assertEquals(6, tabs.size(), () -> "Tabs page should render six tabs: " + tabs);
        assertEquals(2, tabs.stream().filter(M3Tab::isSelected).count(), "Tabs selected count");
        assertEquals(1, tabs.stream().filter(Node::isDisabled).count(), "Tabs disabled count");
        for (M3Tab tab : tabs) {
            assertTabDemoGeometry(image, tab);
        }
    }

    /// Verifies that a navigation bar keeps rendered items contiguous and ordered.
    private static void assertNavigationBarDemoGeometry(M3NavigationBar bar, String description) {
        Bounds barBounds = bar.localToScene(bar.getBoundsInLocal());
        List<M3NavigationItem> items = visibleNodesOfType(bar, M3NavigationItem.class);
        assertEquals(bar.getItems().stream().filter(M3NavigationItem.class::isInstance).count(), items.size(),
                () -> description + " rendered item count should match the public item list");
        assertEquals(bar.getSelectedItems().size(), items.stream().filter(M3NavigationItem::isSelected).count(),
                () -> description + " selected child count should match rendered child state");
        for (M3NavigationItem item : items) {
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(barBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " item escapes navigation bar bounds: bar="
                            + barBounds + ", item=" + itemBounds + ", text=" + item.getText());
            assertEquals(item.getContainerHeight(), itemBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " item height should match its resolved container token: item="
                            + itemBounds + ", token=" + item.getContainerHeight());
            assertEquals(item.getItemWidth(), itemBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " item width should match its resolved width token: item="
                            + itemBounds + ", token=" + item.getItemWidth());
            assertEquals(barBounds.getCenterY(), itemBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " item should be vertically centered in the bar: bar="
                            + barBounds + ", item=" + itemBounds);
        }
        assertHorizontalNavigationItemOrder(bar.getItems(), items, bar.getEffectiveNodeOrientation(), description);
    }

    /// Verifies that a navigation rail keeps rendered items aligned in a vertical column.
    private static void assertNavigationRailDemoGeometry(M3NavigationRail rail, String description) {
        Bounds railBounds = rail.localToScene(rail.getBoundsInLocal());
        List<M3NavigationItem> items = visibleNodesOfType(rail, M3NavigationItem.class);
        assertEquals(rail.getItems().stream().filter(M3NavigationItem.class::isInstance).count(), items.size(),
                () -> description + " rendered item count should match the public item list");
        assertEquals(rail.getSelectedItems().size(), items.stream().filter(M3NavigationItem::isSelected).count(),
                () -> description + " selected child count should match rendered child state");
        for (M3NavigationItem item : items) {
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(railBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " item escapes navigation rail bounds: rail="
                            + railBounds + ", item=" + itemBounds + ", text=" + item.getText());
            assertEquals(item.getContainerHeight(), itemBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " item height should match its resolved container token: item="
                            + itemBounds + ", token=" + item.getContainerHeight());
            assertEquals(item.getItemWidth(), itemBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " item width should match its resolved width token: item="
                            + itemBounds + ", token=" + item.getItemWidth());
            assertEquals(railBounds.getCenterX(), itemBounds.getCenterX(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " item should be horizontally centered in the rail: rail="
                            + railBounds + ", item=" + itemBounds);
        }
        assertVerticalNavigationItemOrder(rail.getItems(), items, description);
    }

    /// Verifies one demo navigation item's selected indicator and label ink geometry.
    private static void assertNavigationItemDemoGeometry(
            WritableImage image,
            M3NavigationItem item,
            String pageTitle
    ) {
        String description = pageTitle + " navigation item `" + item.getText() + "`";
        Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
        assertNodeSnapshotHasOpaquePixels(item, description);
        assertEquals(item.getContainerHeight(), itemBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " height should match its resolved container token: bounds="
                        + itemBounds + ", token=" + item.getContainerHeight());
        assertEquals(item.getItemWidth(), itemBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " width should match its resolved width token: bounds="
                        + itemBounds + ", token=" + item.getItemWidth());

        Node indicator = requireVisibleStyledDescendant(
                item,
                "m3-navigation-item-indicator",
                description + " selected indicator"
        );
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(itemBounds, indicatorBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " selected indicator escapes item bounds: item="
                        + itemBounds + ", indicator=" + indicatorBounds);
        Bounds indicatorLocalBounds = indicator.getBoundsInLocal();
        assertEquals(item.getIndicatorWidth(), indicatorLocalBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " selected indicator width should match its token: indicator="
                        + indicatorLocalBounds + ", token=" + item.getIndicatorWidth());
        assertEquals(item.getIndicatorHeight(), indicatorLocalBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " selected indicator height should match its token: indicator="
                        + indicatorLocalBounds + ", token=" + item.getIndicatorHeight());
        assertEquals(itemBounds.getCenterX(), indicatorBounds.getCenterX(), DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " selected indicator is horizontally off-center: item="
                        + itemBounds + ", indicator=" + indicatorBounds);

        if (item.isSelected()) {
            assertTrue(indicator.getOpacity() >= 0.95 && indicator.getScaleX() >= 0.98,
                    () -> description + " selected indicator should be fully visible: opacity="
                            + indicator.getOpacity() + ", scaleX=" + indicator.getScaleX());
            Rectangle2D indicatorPixels = contrastingPixelBounds(
                    image,
                    indicator,
                    sampleColorOutsideBounds(image, indicatorBounds),
                    0.02
            );
            assertRectangleInsideBounds(indicatorBounds, indicatorPixels, CONTROL_EDGE_TOLERANCE,
                    description + " selected indicator pixels");
            assertTrue(indicatorPixels.getWidth() >= indicator.getBoundsInLocal().getWidth() * 0.75,
                    () -> description + " selected indicator pixels are too narrow: pixels="
                            + indicatorPixels + ", indicator=" + indicatorBounds);
            assertTrue(indicatorPixels.getHeight() >= indicator.getBoundsInLocal().getHeight() * 0.55,
                    () -> description + " selected indicator pixels are too short: pixels="
                            + indicatorPixels + ", indicator=" + indicatorBounds);
        } else {
            assertTrue(indicator.getOpacity() <= 0.05,
                    () -> description + " unselected indicator should be transparent: opacity="
                            + indicator.getOpacity());
        }

        Node label = requireVisibleStyledDescendant(item, "m3-navigation-item-label", description + " label");
        Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(itemBounds, labelBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " label escapes item bounds: item=" + itemBounds + ", label=" + labelBounds);
        Text labelText = Objects.requireNonNull(firstVisibleText(label), description + " label text");
        assertEquals(item.getText(), labelText.getText(),
                () -> description + " label text should match the navigation item text");
        @Nullable Rectangle2D labelInk = renderedTextInkBounds(image, labelText);
        if (labelInk != null) {
            assertRectangleInsideBounds(itemBounds, labelInk, CONTROL_EDGE_TOLERANCE,
                    description + " rendered label ink");
        }
    }

    /// Verifies physical order for horizontal navigation items.
    private static void assertHorizontalNavigationItemOrder(
            List<Node> logicalItems,
            List<M3NavigationItem> renderedItems,
            NodeOrientation orientation,
            String description
    ) {
        List<M3NavigationItem> logicalNavigationItems = logicalItems.stream()
                .filter(M3NavigationItem.class::isInstance)
                .map(M3NavigationItem.class::cast)
                .toList();
        List<M3NavigationItem> physicalOrder = renderedItems.stream()
                .sorted(java.util.Comparator.comparingDouble(item ->
                        item.localToScene(item.getBoundsInLocal()).getMinX()))
                .toList();
        for (int index = 0; index < physicalOrder.size(); index++) {
            M3NavigationItem expected = orientation == NodeOrientation.RIGHT_TO_LEFT
                    ? logicalNavigationItems.get(logicalNavigationItems.size() - 1 - index)
                    : logicalNavigationItems.get(index);
            assertEquals(expected, physicalOrder.get(index),
                    () -> description + " physical order does not match effective node orientation");
        }
        for (int index = 1; index < physicalOrder.size(); index++) {
            Bounds previousBounds = physicalOrder.get(index - 1).localToScene(
                    physicalOrder.get(index - 1).getBoundsInLocal());
            Bounds currentBounds = physicalOrder.get(index).localToScene(physicalOrder.get(index).getBoundsInLocal());
            assertEquals(previousBounds.getMaxX(), currentBounds.getMinX(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent navigation items are not contiguous: previous="
                            + previousBounds + ", current=" + currentBounds);
            assertEquals(previousBounds.getMinY(), currentBounds.getMinY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent navigation item tops are misaligned: previous="
                            + previousBounds + ", current=" + currentBounds);
            assertEquals(previousBounds.getMaxY(), currentBounds.getMaxY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent navigation item bottoms are misaligned: previous="
                            + previousBounds + ", current=" + currentBounds);
        }
    }

    /// Verifies physical order for vertical navigation items.
    private static void assertVerticalNavigationItemOrder(
            List<Node> logicalItems,
            List<M3NavigationItem> renderedItems,
            String description
    ) {
        List<M3NavigationItem> logicalNavigationItems = logicalItems.stream()
                .filter(M3NavigationItem.class::isInstance)
                .map(M3NavigationItem.class::cast)
                .toList();
        List<M3NavigationItem> physicalOrder = renderedItems.stream()
                .sorted(java.util.Comparator.comparingDouble(item ->
                        item.localToScene(item.getBoundsInLocal()).getMinY()))
                .toList();
        assertEquals(logicalNavigationItems, physicalOrder,
                () -> description + " vertical physical order should follow the logical item list");
        for (int index = 1; index < physicalOrder.size(); index++) {
            Bounds previousBounds = physicalOrder.get(index - 1).localToScene(
                    physicalOrder.get(index - 1).getBoundsInLocal());
            Bounds currentBounds = physicalOrder.get(index).localToScene(physicalOrder.get(index).getBoundsInLocal());
            assertTrue(currentBounds.getMinY() > previousBounds.getMaxY(),
                    () -> description + " vertical navigation items should not overlap: previous="
                            + previousBounds + ", current=" + currentBounds);
            assertEquals(previousBounds.getCenterX(), currentBounds.getCenterX(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " vertical navigation item centers are misaligned: previous="
                            + previousBounds + ", current=" + currentBounds);
        }
    }

    /// Verifies a demo tab bar keeps child tabs contiguous and oriented correctly.
    private static void assertTabBarDemoGeometry(M3TabBar tabBar, String description) {
        Bounds tabBarBounds = tabBar.localToScene(tabBar.getBoundsInLocal());
        List<M3Tab> tabs = visibleNodesOfType(tabBar, M3Tab.class);
        assertEquals(tabBar.getTabs().stream().filter(M3Tab.class::isInstance).count(), tabs.size(),
                () -> description + " rendered tab count should match the public tab list");
        assertEquals(tabBar.getSelectedTabs().size(), tabs.stream().filter(M3Tab::isSelected).count(),
                () -> description + " selected child count should match rendered child state");

        for (M3Tab tab : tabs) {
            Bounds tabBounds = tab.localToScene(tab.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(tabBarBounds, tabBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " tab escapes tab bar bounds: tabBar="
                            + tabBarBounds + ", tab=" + tabBounds + ", text=" + tab.getText());
            assertEquals(tab.getContainerHeight(), tabBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " tab height should match its resolved container token: tab="
                            + tabBounds + ", token=" + tab.getContainerHeight());
            assertTrue(tabBounds.getWidth() >= tab.getTabMinWidth() - CONTROL_EDGE_TOLERANCE,
                    () -> description + " tab width should not be smaller than its min-width token: tab="
                            + tabBounds + ", token=" + tab.getTabMinWidth());
            assertEquals(tabBarBounds.getCenterY(), tabBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " tab should be vertically centered in the tab bar: tabBar="
                            + tabBarBounds + ", tab=" + tabBounds);
        }

        assertHorizontalTabOrder(tabBar.getTabs(), tabs, tabBar.getEffectiveNodeOrientation(), description);
    }

    /// Verifies one demo tab's active indicator and label ink geometry.
    private static void assertTabDemoGeometry(WritableImage image, M3Tab tab) {
        String description = "tab `" + tab.getText() + "`";
        Bounds tabBounds = tab.localToScene(tab.getBoundsInLocal());
        assertNodeSnapshotHasOpaquePixels(tab, description);
        assertEquals(tab.getContainerHeight(), tabBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " height should match its resolved container token: bounds="
                        + tabBounds + ", token=" + tab.getContainerHeight());
        assertTrue(tabBounds.getWidth() >= tab.getTabMinWidth() - CONTROL_EDGE_TOLERANCE,
                () -> description + " width should not be smaller than its min-width token: bounds="
                        + tabBounds + ", token=" + tab.getTabMinWidth());

        Node indicator = requireVisibleStyledDescendant(
                tab,
                "m3-tab-active-indicator",
                description + " active indicator"
        );
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(tabBounds, indicatorBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " active indicator escapes tab bounds: tab="
                        + tabBounds + ", indicator=" + indicatorBounds);
        assertEquals(tabBounds.getMaxY(), indicatorBounds.getMaxY(), CONTROL_EDGE_TOLERANCE,
                () -> description + " active indicator should be anchored to the tab bottom: tab="
                        + tabBounds + ", indicator=" + indicatorBounds);
        assertEquals(tab.getActiveIndicatorHeight(), indicatorBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " active indicator height should match its token: indicator="
                        + indicatorBounds + ", token=" + tab.getActiveIndicatorHeight());
        assertEquals(tabBounds.getCenterX(), indicatorBounds.getCenterX(), CONTROL_EDGE_TOLERANCE,
                () -> description + " active indicator should be horizontally centered: tab="
                        + tabBounds + ", indicator=" + indicatorBounds);

        if (tab.isSelected()) {
            assertTrue(indicator.getOpacity() >= 0.95 && indicator.getScaleX() >= 0.98,
                    () -> description + " selected indicator should be fully visible: opacity="
                            + indicator.getOpacity() + ", scaleX=" + indicator.getScaleX());
            Rectangle2D indicatorPixels = contrastingPixelBounds(
                    image,
                    indicator,
                    sampleColorOutsideBounds(image, indicatorBounds),
                    0.02
            );
            assertRectangleInsideBounds(indicatorBounds, indicatorPixels, CONTROL_EDGE_TOLERANCE,
                    description + " active indicator pixels");
            assertTrue(indicatorPixels.getWidth() >= indicatorBounds.getWidth() * 0.75,
                    () -> description + " active indicator pixels are too narrow: pixels="
                            + indicatorPixels + ", indicator=" + indicatorBounds);
            assertTrue(indicatorPixels.getHeight() >= Math.max(1.0, tab.getActiveIndicatorHeight() - 1.0),
                    () -> description + " active indicator pixels are too short: pixels="
                            + indicatorPixels + ", token=" + tab.getActiveIndicatorHeight());
        } else {
            assertTrue(indicator.getOpacity() <= 0.05,
                    () -> description + " unselected indicator should be transparent: opacity="
                            + indicator.getOpacity());
        }

        Text labelText = Objects.requireNonNull(firstVisibleText(tab), description + " label text");
        Rectangle2D labelInk = Objects.requireNonNull(renderedTextInkBounds(image, labelText),
                description + " rendered label ink");
        assertRectangleInsideBounds(tabBounds, labelInk, CONTROL_EDGE_TOLERANCE,
                description + " rendered label ink");
        double expectedCenterY = tabBounds.getMinY() + (tabBounds.getHeight() - tab.getActiveIndicatorHeight()) / 2.0;
        double inkCenterY = labelInk.getMinY() + labelInk.getHeight() / 2.0;
        double deltaY = Math.abs(expectedCenterY - inkCenterY);
        assertTrue(deltaY <= LABELED_TEXT_INK_VERTICAL_CENTER_TOLERANCE + 1.5,
                () -> description + " rendered label ink is vertically off-center: deltaY="
                        + deltaY + ", expectedCenterY=" + expectedCenterY
                        + ", tab=" + tabBounds + ", ink=" + labelInk);
    }

    /// Verifies physical order for horizontal tab bars.
    private static void assertHorizontalTabOrder(
            List<Node> logicalTabs,
            List<M3Tab> renderedTabs,
            NodeOrientation orientation,
            String description
    ) {
        List<M3Tab> logicalM3Tabs = logicalTabs.stream()
                .filter(M3Tab.class::isInstance)
                .map(M3Tab.class::cast)
                .toList();
        List<M3Tab> physicalOrder = renderedTabs.stream()
                .sorted(java.util.Comparator.comparingDouble(tab ->
                        tab.localToScene(tab.getBoundsInLocal()).getMinX()))
                .toList();
        for (int index = 0; index < physicalOrder.size(); index++) {
            M3Tab expected = orientation == NodeOrientation.RIGHT_TO_LEFT
                    ? logicalM3Tabs.get(logicalM3Tabs.size() - 1 - index)
                    : logicalM3Tabs.get(index);
            assertEquals(expected, physicalOrder.get(index),
                    () -> description + " physical order does not match effective node orientation");
        }
        for (int index = 1; index < physicalOrder.size(); index++) {
            Bounds previousBounds = physicalOrder.get(index - 1).localToScene(
                    physicalOrder.get(index - 1).getBoundsInLocal());
            Bounds currentBounds = physicalOrder.get(index).localToScene(physicalOrder.get(index).getBoundsInLocal());
            assertEquals(previousBounds.getMaxX(), currentBounds.getMinX(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent tabs are not contiguous: previous="
                            + previousBounds + ", current=" + currentBounds);
            assertEquals(previousBounds.getMinY(), currentBounds.getMinY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent tab tops are misaligned: previous="
                            + previousBounds + ", current=" + currentBounds);
            assertEquals(previousBounds.getMaxY(), currentBounds.getMaxY(), CONTROL_EDGE_TOLERANCE,
                    () -> description + " adjacent tab bottoms are misaligned: previous="
                            + previousBounds + ", current=" + currentBounds);
        }
    }

    /// Verifies the real Text Fields demo page layout matrix, variants, errors, and trailing actions.
    private static void assertTextFieldsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Text Fields");
        assertCurrentPageTitle(scene, "Text Fields");
        assertVisibleText(root, "Filled", "Text Fields");
        assertVisibleText(root, "Outlined", "Text Fields");
        assertVisibleText(root, "Validation", "Text Fields");
        assertVisibleText(root, "Error", "Text Fields");
        assertVisibleText(root, "Text Areas", "Text Fields");
        assertVisibleText(root, "Right-to-left", "Text Fields");

        List<M3TextInputLayout> layouts = visibleNodesOfType(page, M3TextInputLayout.class);
        assertTrue(layouts.size() >= 15,
                () -> "Text Fields page should render the full input matrix, found " + layouts.size());
        assertTrue(layouts.stream().anyMatch(M3TextInputLayout::isClearButtonEnabled),
                "Text Fields page should render a clear button layout");
        assertTrue(layouts.stream().anyMatch(layout -> !layout.getErrorText().isBlank()),
                "Text Fields page should render error supporting text");
        assertTrue(layouts.stream().anyMatch(layout -> layout.getCharacterLimit() > 0),
                "Text Fields page should render character counters");
        assertTrue(layouts.stream().anyMatch(layout ->
                        layout.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT),
                "Text Fields page should render right-to-left inputs");

        List<M3TextInput> inputs = layouts.stream()
                .map(M3TextInputLayout::getTextInput)
                .filter(Objects::nonNull)
                .toList();
        assertTrue(inputs.stream().anyMatch(input -> textInputVariant(input) == M3TextInputVariant.FILLED),
                "Text Fields page should render filled text inputs");
        assertTrue(inputs.stream().anyMatch(input -> textInputVariant(input) == M3TextInputVariant.OUTLINED),
                "Text Fields page should render outlined text inputs");
        assertTrue(inputs.stream().anyMatch(input -> input instanceof M3TextArea),
                "Text Fields page should render text areas");
        assertTrue(inputs.stream().anyMatch(input -> input instanceof M3TextField field && field.isError()),
                "Text Fields page should render field error state");
        assertTrue(inputs.stream().anyMatch(input -> input instanceof M3PasswordField password && password.isError()),
                "Text Fields page should render password error state");
        for (M3TextInputLayout layout : layouts) {
            assertTextInputLayoutContainerGeometry(layout, "Text Fields");
        }

        M3TextInputLayout outlinedTextLayout = requireTextInputLayout(layouts, "M3FX", "Outlined with text");
        assertOutlinedFloatingLabelGeometry(outlinedTextLayout, "Text Fields outlined populated field");
        assertOutlinedFloatingLabelBackgroundMatchesSurrounding(
                snapshot(scene),
                outlinedTextLayout,
                "Text Fields outlined populated field"
        );
    }

    /// Verifies the real Toolbars demo page variants, orientation, and item-slot geometry.
    private static void assertToolbarsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Toolbars");
        assertCurrentPageTitle(scene, "Toolbars");
        assertVisibleText(root, "Standard", "Toolbars");
        assertVisibleText(root, "Floating", "Toolbars");
        assertVisibleText(root, "Docked", "Toolbars");
        assertVisibleText(root, "Vertical", "Toolbars");

        List<M3Toolbar> toolbars = visibleNodesOfType(page, M3Toolbar.class);
        assertEquals(4, toolbars.size(), () -> "Toolbars page should render four toolbars: " + toolbars);
        assertEquals(M3ToolbarVariant.STANDARD, toolbars.get(0).getVariant());
        assertEquals(M3ToolbarVariant.FLOATING, toolbars.get(1).getVariant());
        assertEquals(M3ToolbarVariant.DOCKED, toolbars.get(2).getVariant());
        assertEquals(Orientation.VERTICAL, toolbars.get(3).getOrientation());
        for (M3Toolbar toolbar : toolbars) {
            assertToolbarDemoGeometry(toolbar);
        }
        assertEquals(0, visibleNodesOfType(page, M3BottomAppBar.class).size(),
                "Toolbars page should showcase M3Toolbar instead of bottom app bars");
        assertDemoVectorIcons(page, "Toolbars", 17);
    }

    /// Verifies the real Banners demo page icons, actions, narrow layout, and right-to-left state.
    private static void assertBannersPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Banners");
        assertCurrentPageTitle(scene, "Banners");
        assertVisibleText(root, "With Actions", "Banners");
        assertVisibleText(root, "Without Icon", "Banners");
        assertVisibleText(root, "Passive", "Banners");
        assertVisibleText(root, "Responsive And RTL", "Banners");

        List<M3Banner> banners = visibleNodesOfType(page, M3Banner.class);
        assertEquals(6, banners.size(), "Banners page should render six banner states");
        assertEquals(4, visibleNodesWithStyle(page, M3Banner.ICON_STYLE_CLASS).size(),
                "Banners page should render four icon slots");
        long actionButtonCount = visibleNodesOfType(page, M3Button.class).stream()
                .filter(button -> nearestAncestorOfType(button, M3Banner.class) != null)
                .count();
        assertEquals(8, actionButtonCount, "Banners page should render all action buttons");
        assertTrue(banners.stream().anyMatch(banner -> banner.getActions().isEmpty()),
                "Banners page should include a passive banner");
        assertTrue(banners.stream().anyMatch(banner -> banner.getPrefWidth() <= 420.0),
                "Banners page should include a narrow banner");
        assertTrue(banners.stream().anyMatch(banner ->
                        banner.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT),
                "Banners page should include a right-to-left banner");
        for (M3Banner banner : banners) {
            assertBannerDemoGeometry(banner);
        }
    }

    /// Returns the concrete variant of one text input control.
    private static @Nullable M3TextInputVariant textInputVariant(M3TextInput input) {
        if (input instanceof M3TextField textField) {
            return textField.getVariant();
        }
        if (input instanceof M3PasswordField passwordField) {
            return passwordField.getVariant();
        }
        if (input instanceof M3TextArea textArea) {
            return textArea.getVariant();
        }
        return null;
    }

    /// Verifies the number of chips with one Material chip variant.
    private static void assertChipVariantCount(
            List<M3Chip> chips,
            M3ChipVariant variant,
            long expected,
            String description
    ) {
        long actual = chips.stream().filter(chip -> chip.getVariant() == variant).count();
        assertEquals(expected, actual, () -> description + " should render " + expected + " "
                + variant + " chips, found " + actual);
    }

    /// Verifies that one demo chip resolves visible container, text, optional icon, and selected fill geometry.
    private static void assertChipDemoGeometry(WritableImage image, M3Chip chip) {
        String description = "chip `" + chip.getText() + "`";
        Bounds chipBounds = chip.localToScene(chip.getBoundsInLocal());
        assertNodeSnapshotHasOpaquePixels(chip, description);
        assertEquals(chip.getContainerHeight(), chipBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " height should match its resolved container token: bounds="
                        + chipBounds + ", token=" + chip.getContainerHeight());
        assertTrue(chipBounds.getWidth() > chipBounds.getHeight(),
                () -> description + " should be wider than tall: " + chipBounds);

        Color surroundingColor = sampleColorOutsideBounds(image, chipBounds);
        Rectangle2D visualPixels = contrastingPixelBounds(
                image,
                chip,
                surroundingColor,
                chip.isDisabled() ? 0.012 : 0.02
        );
        double edgeTolerance = chip.isDisabled() ? 5.0 : 4.0;
        assertTrue(visualPixels.getMinX() <= chipBounds.getMinX() + edgeTolerance
                        && visualPixels.getMaxX() >= chipBounds.getMaxX() - edgeTolerance
                        && visualPixels.getMinY() <= chipBounds.getMinY() + edgeTolerance
                        && visualPixels.getMaxY() >= chipBounds.getMaxY() - edgeTolerance,
                () -> description + " rendered outline or selected fill does not cover the chip container: pixels="
                        + visualPixels + ", bounds=" + chipBounds);

        assertChipInteriorFillState(image, chip, chipBounds, surroundingColor, description);
        assertChipTextInkGeometry(image, chip, chipBounds, description);
        assertChipGraphicGeometry(chip, chipBounds, description);
    }

    /// Verifies that selected chips render a filled interior and unselected chips keep a transparent interior.
    private static void assertChipInteriorFillState(
            WritableImage image,
            M3Chip chip,
            Bounds chipBounds,
            Color surroundingColor,
            String description
    ) {
        Color interiorColor = snapshotColorAt(
                image,
                chipBounds.getCenterX(),
                chipBounds.getMinY() + Math.max(4.0, chipBounds.getHeight() * 0.25)
        );
        double distance = pixelDistance(interiorColor, surroundingColor);
        if (chip.isSelected()) {
            assertTrue(distance >= 0.035,
                    () -> description + " selected chip interior is too close to its surrounding surface: interior="
                            + interiorColor + ", surrounding=" + surroundingColor + ", distance=" + distance);
        } else if (!chip.isDisabled()) {
            assertTrue(distance <= 0.14,
                    () -> description + " unselected chip interior looks filled instead of transparent: interior="
                            + interiorColor + ", surrounding=" + surroundingColor + ", distance=" + distance);
        }
    }

    /// Verifies that one chip's rendered label ink stays inside the container and remains vertically centered.
    private static void assertChipTextInkGeometry(
            WritableImage image,
            M3Chip chip,
            Bounds chipBounds,
            String description
    ) {
        Text text = Objects.requireNonNull(firstVisibleText(chip), description + " text node");
        Rectangle2D inkBounds = Objects.requireNonNull(
                renderedTextInkBounds(image, text),
                description + " rendered text ink"
        );
        assertRectangleInsideBounds(chipBounds, inkBounds, CONTROL_EDGE_TOLERANCE,
                description + " rendered text ink");
        double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
        double deltaY = Math.abs(chipBounds.getCenterY() - inkCenterY);
        assertTrue(deltaY <= LABELED_TEXT_INK_VERTICAL_CENTER_TOLERANCE,
                () -> description + " rendered text ink is vertically off-center: deltaY="
                        + deltaY + ", chip=" + chipBounds + ", ink=" + inkBounds);
    }

    /// Verifies that an optional chip graphic remains inside and vertically centered in the chip container.
    private static void assertChipGraphicGeometry(M3Chip chip, Bounds chipBounds, String description) {
        @Nullable Node graphic = chip.getGraphic();
        if (graphic == null || !graphic.isVisible()) {
            return;
        }

        Bounds graphicBounds = graphic.localToScene(graphic.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(chipBounds, graphicBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " graphic leaves the chip container: graphic="
                        + graphicBounds + ", chip=" + chipBounds);
        assertTrue(Math.abs(chipBounds.getCenterY() - graphicBounds.getCenterY()) <= DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " graphic is vertically off-center: graphic="
                        + graphicBounds + ", chip=" + chipBounds);

        @Nullable Node vectorIcon = firstVisibleDemoVectorIcon(graphic);
        if (vectorIcon != null) {
            assertVectorIconCenteredInContainer(graphic, vectorIcon, description + " graphic vector icon");
        }
    }

    /// Verifies that chip groups contain each visible chip and keep selected state visible through their children.
    private static void assertChipGroupDemoGeometry(M3ChipGroup group) {
        Bounds groupBounds = group.localToScene(group.getBoundsInLocal());
        List<M3Chip> chips = visibleNodesOfType(group, M3Chip.class);
        assertFalse(chips.isEmpty(), () -> "chip group should render at least one chip: " + group);
        assertEquals(group.getSelectedChips().size(), chips.stream().filter(M3Chip::isSelected).count(),
                "chip group selected chip count should match rendered child state");
        for (M3Chip chip : chips) {
            Bounds chipBounds = chip.localToScene(chip.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(groupBounds, chipBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "chip group child leaves the group bounds: group="
                            + groupBounds + ", chip=" + chipBounds + ", text=" + chip.getText());
        }
    }

    /// Verifies the real Checkboxes demo page state matrix and indicator geometry.
    private static void assertCheckboxesPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Checkboxes");
        assertVisibleText(root, "Interactive States", "Checkboxes");
        assertVisibleText(root, "Disabled States", "Checkboxes");
        assertVisibleText(root, "Unchecked", "Checkboxes");
        assertVisibleText(root, "Checked", "Checkboxes");
        assertVisibleText(root, "Indeterminate", "Checkboxes");
        assertVisibleText(root, "Three-state cycle", "Checkboxes");
        assertVisibleText(root, "Disabled unchecked", "Checkboxes");
        assertVisibleText(root, "Disabled checked", "Checkboxes");
        assertVisibleText(root, "Disabled indeterminate", "Checkboxes");

        List<M3CheckBox> checkBoxes = visibleNodesOfType(root, M3CheckBox.class);
        assertEquals(7, checkBoxes.size(),
                () -> "Checkboxes page should render seven checkbox states, found " + checkBoxes.size());
        assertEquals(2, checkBoxes.stream().filter(M3CheckBox::isSelected).count(),
                "Checkboxes page should render two selected states");
        assertEquals(2, checkBoxes.stream().filter(M3CheckBox::isIndeterminate).count(),
                "Checkboxes page should render two indeterminate states");
        assertEquals(2, checkBoxes.stream().filter(M3CheckBox::isAllowIndeterminate).count(),
                "Checkboxes page should render two user-cycle indeterminate controls");
        assertEquals(3, checkBoxes.stream().filter(Node::isDisabled).count(),
                "Checkboxes page should render three disabled states");
        assertSelectionIndicatorsCentered(scene, "Checkboxes");
    }

    /// Verifies the real Radio Buttons demo page state matrix and indicator geometry.
    private static void assertRadioButtonsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Radio Buttons");
        assertVisibleText(root, "Selection Group", "Radio Buttons");
        assertVisibleText(root, "Disabled States", "Radio Buttons");
        assertVisibleText(root, "Radio A", "Radio Buttons");
        assertVisibleText(root, "Radio B", "Radio Buttons");
        assertVisibleText(root, "Disabled unchecked", "Radio Buttons");
        assertVisibleText(root, "Disabled selected", "Radio Buttons");

        List<M3RadioButton> radioButtons = visibleNodesOfType(root, M3RadioButton.class);
        assertEquals(4, radioButtons.size(),
                () -> "Radio Buttons page should render four radio states, found " + radioButtons.size());
        assertEquals(2, radioButtons.stream().filter(M3RadioButton::isSelected).count(),
                "Radio Buttons page should render one enabled and one disabled selected state");
        assertEquals(2, radioButtons.stream().filter(Node::isDisabled).count(),
                "Radio Buttons page should render two disabled states");
        assertSelectionIndicatorsCentered(scene, "Radio Buttons");
    }

    /// Verifies the real Switches demo page state matrix and thumb geometry.
    private static void assertSwitchesPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Switches");
        assertVisibleText(root, "Interactive States", "Switches");
        assertVisibleText(root, "Disabled States", "Switches");
        assertVisibleText(root, "On", "Switches");
        assertVisibleText(root, "Off", "Switches");
        assertVisibleText(root, "Disabled off", "Switches");
        assertVisibleText(root, "Disabled on", "Switches");

        Set<String> expectedSwitchLabels = Set.of("On", "Off", "Disabled off", "Disabled on");
        List<M3Switch> switches = visibleNodesOfType(root, M3Switch.class)
                .stream()
                .filter(switchControl -> expectedSwitchLabels.contains(switchControl.getText()))
                .toList();
        assertEquals(4, switches.size(),
                () -> "Switches page should render four switch states, found " + switches.size());
        assertEquals(2, switches.stream().filter(M3Switch::isSelected).count(),
                "Switches page should render one enabled and one disabled selected state");
        assertEquals(2, switches.stream().filter(Node::isDisabled).count(),
                "Switches page should render two disabled states");
        assertSelectionIndicatorsCentered(scene, "Switches");
    }

    /// Verifies the real Buttons demo page variant matrix and disabled state.
    private static void assertButtonsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Buttons");
        assertCurrentPageTitle(scene, "Buttons");
        assertVisibleText(root, "Button Variants", "Buttons");
        assertVisibleText(root, "Filled", "Buttons");
        assertVisibleText(root, "Tonal", "Buttons");
        assertVisibleText(root, "Outlined", "Buttons");
        assertVisibleText(root, "Text", "Buttons");
        assertVisibleText(root, "Elevated", "Buttons");
        assertVisibleText(root, "Disabled", "Buttons");

        List<M3Button> buttons = visibleDemoPageButtons(page);
        assertEquals(6, buttons.size(), () -> "Buttons page should render six sample buttons: " + buttons);
        assertButtonVariantCount(buttons, M3ButtonVariant.FILLED, 2, "Buttons");
        assertButtonVariantCount(buttons, M3ButtonVariant.TONAL, 1, "Buttons");
        assertButtonVariantCount(buttons, M3ButtonVariant.OUTLINED, 1, "Buttons");
        assertButtonVariantCount(buttons, M3ButtonVariant.TEXT, 1, "Buttons");
        assertButtonVariantCount(buttons, M3ButtonVariant.ELEVATED, 1, "Buttons");
        assertEquals(1, buttons.stream().filter(Node::isDisabled).count(),
                "Buttons page should render one disabled button state");
    }

    /// Verifies the real Button Groups demo page connected group structure and variants.
    private static void assertButtonGroupsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Button Groups");
        assertVisibleText(root, "Tonal Group", "Button Groups");
        assertVisibleText(root, "Outlined Group", "Button Groups");
        assertVisibleText(root, "Filled Group", "Button Groups");
        assertVisibleText(root, "Edit", "Button Groups");
        assertVisibleText(root, "Day", "Button Groups");
        assertVisibleText(root, "Accept", "Button Groups");

        List<M3ButtonGroup> groups = visibleNodesOfType(root, M3ButtonGroup.class);
        assertEquals(3, groups.size(), () -> "Button Groups page should render three groups: " + groups);
        List<M3Button> groupButtons = new ArrayList<>();
        for (int index = 0; index < groups.size(); index++) {
            int groupIndex = index;
            M3ButtonGroup group = groups.get(index);
            assertEquals(3, group.getItems().size(),
                    () -> "Button group " + groupIndex + " should expose three action items");
            for (Node item : group.getItems()) {
                groupButtons.add(assertInstanceOf(M3Button.class, item, "button group item"));
            }
            assertButtonGroupItemsInsideGroup(group, "Button Groups group " + groupIndex);
        }
        assertEquals(9, groupButtons.size(), "Button Groups page should expose nine grouped actions");
        assertButtonVariantCount(groupButtons, M3ButtonVariant.TONAL, 3, "Button Groups");
        assertButtonVariantCount(groupButtons, M3ButtonVariant.OUTLINED, 3, "Button Groups");
        assertButtonVariantCount(groupButtons, M3ButtonVariant.FILLED, 3, "Button Groups");
        assertEquals(1, groupButtons.stream().filter(Node::isDisabled).count(),
                "Button Groups page should include one disabled grouped action");
    }

    /// Verifies the real Icon Buttons demo page icon, toggle, and selection-state matrix.
    private static void assertIconButtonsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Icon Buttons");
        assertCurrentPageTitle(scene, "Icon Buttons");
        assertVisibleText(root, "Icon Buttons", "Icon Buttons");
        assertVisibleText(root, "Toggle Icon Buttons", "Icon Buttons");

        List<M3IconButton> iconButtons = visibleNodesOfType(page, M3IconButton.class);
        assertEquals(3, iconButtons.size(), () -> "Icon Buttons page should render three icon buttons: "
                + iconButtons);
        assertEquals(1, iconButtons.stream().filter(Node::isDisabled).count(),
                "Icon Buttons page should render one disabled icon button");
        for (M3IconButton button : iconButtons) {
            assertTrue(Objects.toString(button.getText(), "").isBlank(),
                    () -> "icon buttons should not expose placeholder text: " + button.getText());
            assertNotNull(firstVisibleDemoVectorIcon(button), "icon button should use an SVG demo icon");
        }

        List<M3IconToggleButtonGroup> groups = visibleNodesOfType(page, M3IconToggleButtonGroup.class);
        assertEquals(3, groups.size(), () -> "Icon Buttons page should render three toggle groups: " + groups);
        assertEquals(4, groups.get(0).getItems().size(), "standard toggle group item count");
        assertEquals(3, groups.get(1).getItems().size(), "tonal toggle group item count");
        assertEquals(3, groups.get(2).getItems().size(), "formatting toggle group item count");
        assertEquals(M3IconToggleButtonSelectionMode.SINGLE, groups.get(0).getSelectionMode());
        assertEquals(M3IconToggleButtonSelectionMode.SINGLE, groups.get(1).getSelectionMode());
        assertEquals(M3IconToggleButtonSelectionMode.MULTIPLE, groups.get(2).getSelectionMode());
        assertEquals(1, groups.get(0).getSelectedButtons().size(), "standard toggle selected count");
        assertEquals(1, groups.get(1).getSelectedButtons().size(), "tonal toggle selected count");
        assertEquals(2, groups.get(2).getSelectedButtons().size(), "formatting toggle selected count");

        List<M3IconToggleButton> toggles = visibleNodesOfType(page, M3IconToggleButton.class);
        assertEquals(11, toggles.size(), () -> "Icon Buttons page should render eleven toggle icon buttons: "
                + toggles);
        assertEquals(4, toggles.stream().filter(M3IconToggleButton::isSelected).count(),
                "Icon Buttons page should render four selected toggle states");
        assertIconToggleVariantCount(toggles, M3IconToggleButtonVariant.STANDARD, 4, "Icon Buttons");
        assertIconToggleVariantCount(toggles, M3IconToggleButtonVariant.TONAL, 4, "Icon Buttons");
        assertIconToggleVariantCount(toggles, M3IconToggleButtonVariant.OUTLINED, 3, "Icon Buttons");
        assertDemoVectorIcons(page, "Icon Buttons", 14);
    }

    /// Verifies the real Floating Action Buttons demo page size and color-role matrix.
    private static void assertFloatingActionButtonsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Floating Action Buttons");
        assertCurrentPageTitle(scene, "Floating Action Buttons");
        assertVisibleText(root, "Floating Action Buttons", "Floating Action Buttons");

        List<M3FloatingActionButton> fabs = visibleNodesOfType(page, M3FloatingActionButton.class);
        assertEquals(3, fabs.size(), () -> "Floating Action Buttons page should render three FABs: " + fabs);
        assertFloatingActionButtonSizeCount(fabs, M3FloatingActionButtonSize.SMALL, 1, "Floating Action Buttons");
        assertFloatingActionButtonSizeCount(fabs, M3FloatingActionButtonSize.REGULAR, 1, "Floating Action Buttons");
        assertFloatingActionButtonSizeCount(fabs, M3FloatingActionButtonSize.LARGE, 1, "Floating Action Buttons");
        assertFloatingActionButtonVariantCount(
                fabs,
                M3FloatingActionButtonVariant.PRIMARY,
                2,
                "Floating Action Buttons"
        );
        assertFloatingActionButtonVariantCount(
                fabs,
                M3FloatingActionButtonVariant.TERTIARY,
                1,
                "Floating Action Buttons"
        );
        for (M3FloatingActionButton fab : fabs) {
            assertTrue(Objects.toString(fab.getText(), "").isBlank(),
                    () -> "icon-only FAB should not expose label text: " + fab.getText());
            assertNotNull(firstVisibleDemoVectorIcon(fab), "FAB should use an SVG demo icon");
            assertNodeSnapshotHasOpaquePixels(fab, "floating action button " + fab.getSize());
        }
        assertDemoVectorIcons(page, "Floating Action Buttons", 3);
    }

    /// Verifies the real Extended FABs demo page label and variant matrix.
    private static void assertExtendedFabsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Extended FABs");
        assertCurrentPageTitle(scene, "Extended FABs");
        assertVisibleText(root, "Extended FABs", "Extended FABs");
        assertVisibleText(root, "Create", "Extended FABs");
        assertVisibleText(root, "Compose", "Extended FABs");
        assertVisibleText(root, "Upload", "Extended FABs");

        List<M3FloatingActionButton> fabs = visibleNodesOfType(page, M3FloatingActionButton.class);
        assertEquals(3, fabs.size(), () -> "Extended FABs page should render three labeled FABs: " + fabs);
        assertFloatingActionButtonSizeCount(fabs, M3FloatingActionButtonSize.REGULAR, 3, "Extended FABs");
        assertFloatingActionButtonVariantCount(fabs, M3FloatingActionButtonVariant.SURFACE, 1, "Extended FABs");
        assertFloatingActionButtonVariantCount(fabs, M3FloatingActionButtonVariant.PRIMARY, 1, "Extended FABs");
        assertFloatingActionButtonVariantCount(fabs, M3FloatingActionButtonVariant.SECONDARY, 1, "Extended FABs");
        for (M3FloatingActionButton fab : fabs) {
            assertFalse(Objects.toString(fab.getText(), "").isBlank(),
                    () -> "extended FAB should render visible label text: " + fab);
            assertNodeSnapshotHasOpaquePixels(fab, "extended FAB " + fab.getText());
        }
    }

    /// Verifies the real FAB Menu demo page expanded, collapsed, and variant menu states.
    private static void assertFabMenuPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "FAB Menu");
        assertVisibleText(root, "Expanded", "FAB Menu");
        assertVisibleText(root, "Collapsed", "FAB Menu");
        assertVisibleText(root, "Variants", "FAB Menu");

        List<M3FabMenu> menus = visibleNodesOfType(root, M3FabMenu.class);
        assertEquals(3, menus.size(), () -> "FAB Menu page should render three FAB menus: " + menus);
        assertEquals(2, menus.stream().filter(M3FabMenu::isExpanded).count(),
                "FAB Menu page should render two expanded menu examples");
        for (int index = 0; index < menus.size(); index++) {
            int menuIndex = index;
            M3FabMenu menu = menus.get(index);
            assertEquals(3, menu.getItems().size(), () -> "FAB menu " + menuIndex + " action count");
            for (Node item : menu.getItems()) {
                M3FloatingActionButton action =
                        assertInstanceOf(M3FloatingActionButton.class, item, "FAB menu action");
                assertEquals(M3FloatingActionButtonSize.SMALL, action.getSize(),
                        "FAB menu actions should use compact FAB size");
                assertNotNull(firstVisibleDemoVectorIcon(action), "FAB menu action should use an SVG demo icon");
            }
            assertEquals(M3FloatingActionButtonSize.REGULAR, menu.getToggleButton().getSize(),
                    "FAB menu toggle should use the regular FAB size");
            assertNotNull(firstVisibleDemoVectorIcon(menu.getToggleButton()),
                    "FAB menu toggle should use an SVG demo icon");
            if (menu.isExpanded()) {
                assertFabMenuActionsStayInsideShowcase(menu);
            }
        }
        assertDemoVectorIcons(root, "FAB Menu", 9);
    }

    /// Verifies the real Split Buttons demo page action/menu structure and variants.
    private static void assertSplitButtonsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Split Buttons");
        assertCurrentPageTitle(scene, "Split Buttons");
        assertVisibleText(root, "Variants", "Split Buttons");
        assertVisibleText(root, "Create", "Split Buttons");
        assertVisibleText(root, "Export", "Split Buttons");
        assertVisibleText(root, "Publish", "Split Buttons");
        assertVisibleText(root, "Disabled", "Split Buttons");

        List<M3SplitButton> splitButtons = visibleNodesOfType(page, M3SplitButton.class);
        assertEquals(4, splitButtons.size(),
                () -> "Split Buttons page should render four split button variants: " + splitButtons);
        assertSplitButtonVariantCount(splitButtons, M3ButtonVariant.TONAL, 2, "Split Buttons");
        assertSplitButtonVariantCount(splitButtons, M3ButtonVariant.OUTLINED, 1, "Split Buttons");
        assertSplitButtonVariantCount(splitButtons, M3ButtonVariant.FILLED, 1, "Split Buttons");
        assertEquals(1, splitButtons.stream().filter(Node::isDisabled).count(),
                "Split Buttons page should render one disabled split button");
        for (M3SplitButton splitButton : splitButtons) {
            assertEquals(3, splitButton.getItems().size(),
                    () -> "split button should expose three attached menu items: " + splitButton.getText());
            assertEquals(splitButton.getVariant(), splitButton.getActionButton().getVariant(),
                    "split action button variant should follow the owner");
            assertEquals(splitButton.getVariant(), splitButton.getMenuButton().getVariant(),
                    "split menu button variant should follow the owner");
            assertSplitButtonPartsInsideOwner(splitButton);
        }
    }

    /// Verifies the real Bottom Sheets demo page standard and compact sheet states.
    private static void assertBottomSheetsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Bottom Sheets");
        assertVisibleText(root, "Bottom Sheets", "Bottom Sheets");
        assertVisibleText(root, "Now playing", "Bottom Sheets");
        assertVisibleText(root, "Compact", "Bottom Sheets");
        assertVisibleText(root, "Overview", "Bottom Sheets");
        assertVisibleText(root, "Activity", "Bottom Sheets");
        assertVisibleText(root, "Settings", "Bottom Sheets");

        List<M3BottomSheet> sheets = visibleNodesOfType(root, M3BottomSheet.class);
        assertEquals(2, sheets.size(), () -> "Bottom Sheets page should render two sheet states: " + sheets);

        M3BottomSheet standard = Objects.requireNonNull(
                firstVisibleBottomSheetWithHeadline(root, "Now playing"),
                "standard bottom sheet"
        );
        assertEquals(M3SheetVariant.STANDARD, standard.getVariant(), "standard bottom sheet variant");
        assertTrue(standard.isShown(), "standard bottom sheet should be shown in the demo matrix");
        assertTrue(standard.isDragHandleVisible(), "standard bottom sheet should expose its drag handle");
        assertEquals(1, standard.getActions().size(), "standard bottom sheet action count");
        assertBottomSheetDemoGeometry(standard, "standard bottom sheet");

        M3BottomSheet compact = Objects.requireNonNull(
                firstVisibleBottomSheetWithHeadline(root, "Compact"),
                "compact bottom sheet"
        );
        assertEquals(M3SheetVariant.STANDARD, compact.getVariant(), "compact bottom sheet variant");
        assertTrue(compact.isShown(), "compact bottom sheet should be shown in the demo matrix");
        assertFalse(compact.isDragHandleVisible(), "compact bottom sheet should hide its drag handle");
        assertTrue(compact.getActions().isEmpty(), "compact bottom sheet should omit trailing actions");
        assertBottomSheetDemoGeometry(compact, "compact bottom sheet");
    }

    /// Verifies the real Side Sheets demo page standard and modal sheet states.
    private static void assertSideSheetsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Side Sheets");
        assertVisibleText(root, "Side Sheets", "Side Sheets");
        assertVisibleText(root, "Details", "Side Sheets");
        assertVisibleText(root, "Filters", "Side Sheets");
        assertVisibleText(root, "Overview", "Side Sheets");
        assertVisibleText(root, "Activity", "Side Sheets");
        assertVisibleText(root, "Settings", "Side Sheets");

        List<M3SideSheet> sheets = visibleNodesOfType(root, M3SideSheet.class);
        assertEquals(2, sheets.size(), () -> "Side Sheets page should render two sheet states: " + sheets);

        M3SideSheet standard = Objects.requireNonNull(
                firstVisibleSideSheetWithHeadline(root, "Details"),
                "standard side sheet"
        );
        assertEquals(M3SheetVariant.STANDARD, standard.getVariant(), "standard side sheet variant");
        assertTrue(standard.isShown(), "standard side sheet should be shown in the demo matrix");
        assertEquals(1, standard.getActions().size(), "standard side sheet action count");
        assertSideSheetDemoGeometry(standard, "standard side sheet");

        M3SideSheet modal = Objects.requireNonNull(
                firstVisibleSideSheetWithHeadline(root, "Filters"),
                "modal side sheet"
        );
        assertEquals(M3SheetVariant.MODAL, modal.getVariant(), "modal side sheet variant");
        assertTrue(modal.isShown(), "modal side sheet should be shown in the demo matrix");
        assertEquals(1, modal.getActions().size(), "modal side sheet action count");
        assertSideSheetDemoGeometry(modal, "modal side sheet");
    }

    /// Verifies the real Snackbars demo page buttons, compact host surface, actions, and queue state.
    private static void assertSnackbarsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        Node page = currentDemoPage(scene, "Snackbars");
        assertCurrentPageTitle(scene, "Snackbars");
        assertVisibleText(root, "Snackbar Host", "Snackbars");
        assertVisibleText(root, "Show message", "Snackbars");
        assertVisibleText(root, "Show action", "Snackbars");
        assertVisibleText(root, "Queue messages", "Snackbars");

        List<M3Button> buttons = visibleDemoPageButtons(page);
        assertEquals(3, buttons.size(), () -> "Snackbars page should render three trigger buttons: " + buttons);
        assertButtonVariantCount(buttons, M3ButtonVariant.FILLED, 1, "Snackbars");
        assertButtonVariantCount(buttons, M3ButtonVariant.TONAL, 1, "Snackbars");
        assertButtonVariantCount(buttons, M3ButtonVariant.OUTLINED, 1, "Snackbars");

        M3SnackbarHost host = Objects.requireNonNull(firstVisibleSnackbarHost(root), "snackbar host");
        assertFalse(host.isShowing(), "snackbar host should start idle");
        assertNull(host.getSnackbar(), "snackbar host should not start with a visible snackbar");
        assertTrue(host.getQueue().isEmpty(), "snackbar host queue should start empty");

        @Nullable Duration previousDisplayDuration = host.displayDurationProperty().get();
        M3MotionSettings.setAnimationsEnabled(host, false);
        host.setDisplayDuration(Duration.INDEFINITE);
        try {
            M3Button messageButton = Objects.requireNonNull(
                    firstVisibleButtonWithText(page, "Show message"),
                    "message snackbar button"
            );
            messageButton.fire();
            applySceneCssAndLayout(scene);
            assertSnackbarHostShowsCompactMessage(scene, host, "Theme-aware snackbar");
            writePageSnapshot(scene, "overlay-snackbars-message.png", "Snackbars message state");

            M3Button actionButton = Objects.requireNonNull(
                    firstVisibleButtonWithText(page, "Show action"),
                    "action snackbar button"
            );
            actionButton.fire();
            applySceneCssAndLayout(scene);
            M3Snackbar actionSnackbar = assertSnackbarHostShowsCompactMessage(scene, host, "Theme-aware snackbar");
            assertNotNull(firstVisibleButtonWithText(actionSnackbar, "Action"),
                    "action snackbar should expose its action button");
            writePageSnapshot(scene, "overlay-snackbars-action.png", "Snackbars action state");

            host.dismissAll();
            applySceneCssAndLayout(scene);
            assertFalse(host.isShowing(), "snackbar host should hide before queue verification");
            assertNull(host.getSnackbar(), "snackbar host should clear the dismissed snackbar");

            M3Button queueButton = Objects.requireNonNull(
                    firstVisibleButtonWithText(page, "Queue messages"),
                    "queued snackbar button"
            );
            queueButton.fire();
            applySceneCssAndLayout(scene);
            assertSnackbarHostShowsCompactMessage(scene, host, "First queued message");
            assertEquals(2, host.getQueue().size(), "queued snackbar demo should leave two pending messages");
            writePageSnapshot(scene, "overlay-snackbars-queued.png", "Snackbars queued state");
        } finally {
            host.dismissAll();
            host.displayDurationProperty().set(previousDisplayDuration);
            M3MotionSettings.clearAnimationsEnabled(host);
            applySceneCssAndLayout(scene);
        }
    }

    /// Verifies that one bottom sheet sample keeps content, actions, and pixels inside a compact surface.
    private static void assertBottomSheetDemoGeometry(M3BottomSheet sheet, String description) {
        Bounds sheetBounds = sheet.localToScene(sheet.getBoundsInLocal());
        assertTrue(bottomSheetShown(sheet), () -> description + " should be fully shown: " + sheetBounds);
        assertTrue(sheetBounds.getWidth() >= 320.0 && sheetBounds.getWidth() <= 560.0,
                () -> description + " width should stay near the demo sheet width: " + sheetBounds);
        assertTrue(sheetBounds.getHeight() >= 160.0 && sheetBounds.getHeight() <= 420.0,
                () -> description + " height should stay compact: " + sheetBounds);
        assertSheetContentGeometry(sheet, sheet.getContent(), sheet.getActions(), description);
        assertNodeSnapshotHasOpaquePixels(sheet, description);
    }

    /// Verifies that one side sheet sample keeps content, actions, and pixels inside a compact surface.
    private static void assertSideSheetDemoGeometry(M3SideSheet sheet, String description) {
        Bounds sheetBounds = sheet.localToScene(sheet.getBoundsInLocal());
        assertTrue(sideSheetShown(sheet), () -> description + " should be fully shown: " + sheetBounds);
        assertTrue(sheetBounds.getWidth() >= 240.0 && sheetBounds.getWidth() <= 420.0,
                () -> description + " width should stay near the side sheet width: " + sheetBounds);
        assertTrue(sheetBounds.getHeight() >= 160.0 && sheetBounds.getHeight() <= 520.0,
                () -> description + " height should stay compact: " + sheetBounds);
        assertSheetContentGeometry(sheet, sheet.getContent(), sheet.getActions(), description);
        assertNodeSnapshotHasOpaquePixels(sheet, description);
    }

    /// Verifies shared sheet content and action-node geometry.
    private static void assertSheetContentGeometry(
            Node sheet,
            @Nullable Node content,
            List<Node> actions,
            String description
    ) {
        Bounds sheetBounds = sheet.localToScene(sheet.getBoundsInLocal());
        assertNotNull(content, () -> description + " should expose a content node");
        Node resolvedContent = Objects.requireNonNull(content, "content");
        assertTrue(resolvedContent.isVisible() && hasRenderableBounds(resolvedContent),
                () -> description + " content should be visible: " + resolvedContent);
        Bounds contentBounds = resolvedContent.localToScene(resolvedContent.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(sheetBounds, contentBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " content leaves the sheet bounds: sheet="
                        + sheetBounds + ", content=" + contentBounds);

        List<M3ListItem> rows = visibleNodesOfType(sheet, M3ListItem.class);
        assertEquals(3, rows.size(), () -> description + " should render three list-item rows: " + rows);
        for (M3ListItem row : rows) {
            Bounds rowBounds = row.localToScene(row.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(sheetBounds, rowBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " list row leaves the sheet bounds: sheet="
                            + sheetBounds + ", row=" + rowBounds);
            assertNotNull(firstVisibleDemoVectorIcon(row), () -> description + " list row should render an icon");
        }

        for (Node action : actions) {
            assertTrue(action.isVisible() && hasRenderableBounds(action),
                    () -> description + " action should be visible: " + action);
            Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(sheetBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " action leaves the sheet bounds: sheet="
                            + sheetBounds + ", action=" + actionBounds);
        }
    }

    /// Verifies that a snackbar host is showing the requested compact message surface.
    private static M3Snackbar assertSnackbarHostShowsCompactMessage(
            Scene scene,
            M3SnackbarHost host,
            String message
    ) {
        assertTrue(snackbarSettled(host), () -> "snackbar host should be settled for message: " + message);
        M3Snackbar snackbar = Objects.requireNonNull(host.getSnackbar(), "snackbar");
        assertVisibleText(snackbar, message, "Snackbars");
        assertSnackbarStaysCompact(scene, snackbar);
        assertNodeSnapshotHasOpaquePixels(snackbar, "snackbar " + message);
        return snackbar;
    }

    /// Applies CSS and layout to the whole demo scene.
    private static void applySceneCssAndLayout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    /// Returns the currently rendered demo page node.
    private static Node currentDemoPage(Scene scene, String pageTitle) {
        return requireVisibleStyledDescendant(scene.getRoot(), "demo-page", pageTitle + " page");
    }

    /// Returns sample `M3Button` controls from a demo page, excluding the page-level Material docs action.
    private static List<M3Button> visibleDemoPageButtons(Node page) {
        return visibleNodesOfType(page, M3Button.class)
                .stream()
                .filter(button -> !button.getStyleClass().contains("demo-page-doc-link"))
                .toList();
    }

    /// Verifies the number of buttons with one Material button variant.
    private static void assertButtonVariantCount(
            List<M3Button> buttons,
            M3ButtonVariant variant,
            long expected,
            String description
    ) {
        long actual = buttons.stream().filter(button -> button.getVariant() == variant).count();
        assertEquals(expected, actual, () -> description + " should render " + expected + " "
                + variant + " buttons, found " + actual);
    }

    /// Verifies the number of toggle icon buttons with one variant.
    private static void assertIconToggleVariantCount(
            List<M3IconToggleButton> buttons,
            M3IconToggleButtonVariant variant,
            long expected,
            String description
    ) {
        long actual = buttons.stream().filter(button -> button.getVariant() == variant).count();
        assertEquals(expected, actual, () -> description + " should render " + expected + " "
                + variant + " toggle icon buttons, found " + actual);
    }

    /// Verifies the number of floating action buttons with one size token.
    private static void assertFloatingActionButtonSizeCount(
            List<M3FloatingActionButton> buttons,
            M3FloatingActionButtonSize size,
            long expected,
            String description
    ) {
        long actual = buttons.stream().filter(button -> button.getSize() == size).count();
        assertEquals(expected, actual, () -> description + " should render " + expected + " "
                + size + " FABs, found " + actual);
    }

    /// Verifies the number of floating action buttons with one variant.
    private static void assertFloatingActionButtonVariantCount(
            List<M3FloatingActionButton> buttons,
            M3FloatingActionButtonVariant variant,
            long expected,
            String description
    ) {
        long actual = buttons.stream().filter(button -> button.getVariant() == variant).count();
        assertEquals(expected, actual, () -> description + " should render " + expected + " "
                + variant + " FABs, found " + actual);
    }

    /// Verifies the number of split buttons with one button variant.
    private static void assertSplitButtonVariantCount(
            List<M3SplitButton> buttons,
            M3ButtonVariant variant,
            long expected,
            String description
    ) {
        long actual = buttons.stream().filter(button -> button.getVariant() == variant).count();
        assertEquals(expected, actual, () -> description + " should render " + expected + " "
                + variant + " split buttons, found " + actual);
    }

    /// Verifies that a button group's public items render inside the group control bounds.
    private static void assertButtonGroupItemsInsideGroup(M3ButtonGroup group, String description) {
        Bounds groupBounds = group.localToScene(group.getBoundsInLocal());
        for (Node item : group.getItems()) {
            if (!item.isVisible() || !hasRenderableBounds(item)) {
                continue;
            }
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(groupBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> description + " item leaves the group bounds: group="
                            + groupBounds + ", item=" + itemBounds);
        }
    }

    /// Verifies that the primary and menu halves of a split button remain inside the owner control.
    private static void assertSplitButtonPartsInsideOwner(M3SplitButton splitButton) {
        Bounds ownerBounds = splitButton.localToScene(splitButton.getBoundsInLocal());
        Bounds actionBounds = splitButton.getActionButton().localToScene(
                splitButton.getActionButton().getBoundsInLocal()
        );
        Bounds menuBounds = splitButton.getMenuButton().localToScene(
                splitButton.getMenuButton().getBoundsInLocal()
        );
        assertTrue(containsBoundsWithTolerance(ownerBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                () -> "split action button leaves owner bounds: owner="
                        + ownerBounds + ", action=" + actionBounds);
        assertTrue(containsBoundsWithTolerance(ownerBounds, menuBounds, CONTROL_EDGE_TOLERANCE),
                () -> "split menu button leaves owner bounds: owner="
                        + ownerBounds + ", menu=" + menuBounds);
        assertEquals(actionBounds.getCenterY(), menuBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                () -> "split button halves should be vertically aligned: action="
                        + actionBounds + ", menu=" + menuBounds);
    }

    /// Verifies the real Date Pickers demo page field, dialog, and calendar-cell geometry.
    private static void assertDatePickersPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Date Pickers");
        assertVisibleText(root, "Fields", "Date Pickers");
        assertVisibleText(root, "Range Field", "Date Pickers");
        assertVisibleText(root, "Dialogs", "Date Pickers");
        assertVisibleText(root, "Selected Date", "Date Pickers");
        assertVisibleText(root, "Event date", "Date Pickers");
        assertVisibleText(root, "Booking date", "Date Pickers");
        assertVisibleText(root, "Start date", "Date Pickers");
        assertVisibleText(root, "End date", "Date Pickers");
        assertVisibleText(root, "Open date dialog", "Date Pickers");
        assertVisibleText(root, "Open range dialog", "Date Pickers");

        List<M3DatePickerField> dateFields = visibleNodesOfType(root, M3DatePickerField.class);
        assertEquals(2, dateFields.size(), () -> "Date Pickers page should render two date fields: "
                + dateFields.size());
        List<M3DateRangePickerField> rangeFields = visibleNodesOfType(root, M3DateRangePickerField.class);
        assertEquals(1, rangeFields.size(), () -> "Date Pickers page should render one range field: "
                + rangeFields.size());
        List<M3DatePicker> datePickers = visibleNodesOfType(root, M3DatePicker.class);
        assertEquals(3, datePickers.size(), () -> "Date Pickers page should render three inline date pickers: "
                + datePickers.size());
        List<M3DateRangePicker> rangePickers = visibleNodesOfType(root, M3DateRangePicker.class);
        assertEquals(1, rangePickers.size(), () -> "Date Pickers page should render one inline range picker: "
                + rangePickers.size());

        ScrollPane pageScrollPane = assertInstanceOf(
                ScrollPane.class,
                requireVisibleStyledDescendant(root, "demo-scroll-pane", "Date Pickers page scroll pane")
        );
        pageScrollPane.setVvalue(1.0);
        root.applyCss();
        root.layout();

        WritableImage image = snapshot(scene);
        assertVisiblePickerCellTextInkCentered(
                scene,
                image,
                M3DatePicker.DAY_CELL_STYLE_CLASS,
                "Date Pickers visible day cells",
                10
        );
        assertDemoPageVisualGeometry(scene, "Date Pickers");
    }

    /// Verifies the real Time Pickers demo page field, dialog, and time-cell geometry.
    private static void assertTimePickersPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Time Pickers");
        assertVisibleText(root, "Fields", "Time Pickers");
        assertVisibleText(root, "Dialog", "Time Pickers");
        assertVisibleText(root, "12 Hour", "Time Pickers");
        assertVisibleText(root, "24 Hour", "Time Pickers");
        assertVisibleText(root, "Start time", "Time Pickers");
        assertVisibleText(root, "Office hours", "Time Pickers");
        assertVisibleText(root, "Open time dialog", "Time Pickers");

        List<M3TimePickerField> timeFields = visibleNodesOfType(root, M3TimePickerField.class);
        assertEquals(2, timeFields.size(), () -> "Time Pickers page should render two time fields: "
                + timeFields.size());
        List<M3TimePicker> timePickers = visibleNodesOfType(root, M3TimePicker.class);
        assertEquals(3, timePickers.size(), () -> "Time Pickers page should render three inline time pickers: "
                + timePickers.size());
        assertTrue(timePickers.stream().anyMatch(M3TimePicker::isUse24HourClock),
                "Time Pickers page should render a 24-hour picker");
        assertTrue(timePickers.stream().anyMatch(picker -> picker.getMinTime() != null && picker.getMaxTime() != null),
                "Time Pickers page should render a bounded picker");

        ScrollPane pageScrollPane = assertInstanceOf(
                ScrollPane.class,
                requireVisibleStyledDescendant(root, "demo-scroll-pane", "Time Pickers page scroll pane")
        );
        pageScrollPane.setVvalue(1.0);
        root.applyCss();
        root.layout();

        WritableImage image = snapshot(scene);
        assertVisiblePickerCellTextInkCentered(
                scene,
                image,
                M3TimePicker.CELL_STYLE_CLASS,
                "Time Pickers visible cells",
                10
        );
        assertDemoPageVisualGeometry(scene, "Time Pickers");
    }

    /// Verifies that visible picker cells keep their rendered text centered in the stable cell target.
    private static void assertVisiblePickerCellTextInkCentered(
            Scene scene,
            WritableImage image,
            String cellStyleClass,
            String description,
            int minimumVisibleCells
    ) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        List<ButtonBase> visibleCells = visiblePickerCells(scene.getRoot(), sceneBounds, cellStyleClass);
        assertTrue(visibleCells.size() >= minimumVisibleCells,
                () -> description + " should expose at least " + minimumVisibleCells
                        + " visible cells, found " + visibleCells.size());
        for (ButtonBase cell : visibleCells) {
            assertPickerCellTextInkCentered(image, cell, description);
        }
    }

    /// Returns visible picker cells with the requested style class that are inside the scene viewport.
    private static List<ButtonBase> visiblePickerCells(Node root, Bounds sceneBounds, String cellStyleClass) {
        List<ButtonBase> cells = new ArrayList<>();
        visitVisibleNodes(root, node -> {
            if (!(node instanceof ButtonBase cell)
                    || !cell.getStyleClass().contains(cellStyleClass)
                    || !hasRenderableBounds(cell)) {
                return;
            }

            Bounds cellBounds = cell.localToScene(cell.getLayoutBounds());
            if (!isOutsideSceneViewport(cell, cellBounds, sceneBounds)) {
                cells.add(cell);
            }
        });
        return cells;
    }

    /// Verifies one picker cell's rendered text ink center, not only its layout bounds.
    private static void assertPickerCellTextInkCentered(WritableImage image, ButtonBase cell, String description) {
        Text text = Objects.requireNonNull(firstVisibleText(cell), description + " text");
        if (text.getText().isBlank()) {
            return;
        }

        Bounds cellBounds = cell.localToScene(cell.getLayoutBounds());
        Bounds textBounds = text.localToScene(text.getLayoutBounds());
        assertNodeCentersAligned(cell, text, 1.0, description + " text layout");

        Rectangle2D inkBounds = Objects.requireNonNull(
                renderedTextInkBounds(image, text),
                description + " rendered ink for " + text.getText()
        );
        double inkCenterX = inkBounds.getMinX() + inkBounds.getWidth() / 2.0;
        double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
        assertEquals(cellBounds.getCenterX(), inkCenterX, PICKER_CELL_TEXT_INK_CENTER_TOLERANCE,
                () -> description + " text ink is horizontally off-center: cell="
                        + cell + ", text=" + text.getText() + ", textBounds=" + textBounds
                        + ", inkBounds=" + inkBounds);
        assertEquals(cellBounds.getCenterY(), inkCenterY, PICKER_CELL_TEXT_INK_CENTER_TOLERANCE,
                () -> description + " text ink is vertically off-center: cell="
                        + cell + ", text=" + text.getText() + ", textBounds=" + textBounds
                        + ", inkBounds=" + inkBounds);
        assertTrue(
                inkBounds.getMinX() >= cellBounds.getMinX() - TEXT_EDGE_TOLERANCE
                        && inkBounds.getMaxX() <= cellBounds.getMaxX() + TEXT_EDGE_TOLERANCE
                        && inkBounds.getMinY() >= cellBounds.getMinY() - TEXT_EDGE_TOLERANCE
                        && inkBounds.getMaxY() <= cellBounds.getMaxY() + TEXT_EDGE_TOLERANCE,
                () -> description + " rendered text ink leaves its cell: cell="
                        + cellBounds + ", inkBounds=" + inkBounds
        );
    }

    /// Verifies the real Sliders demo page state matrix and track/thumb geometry.
    private static void assertSlidersPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Sliders");
        assertVisibleText(root, "Continuous", "Sliders");
        assertVisibleText(root, "Discrete", "Sliders");
        assertVisibleText(root, "Vertical", "Sliders");

        List<M3Slider> sliders = visibleNodesOfType(root, M3Slider.class);
        assertEquals(6, sliders.size(),
                () -> "Sliders page should render six slider states, found " + sliders.size());
        assertEquals(1, sliders.stream().filter(Node::isDisabled).count(),
                "Sliders page should render one disabled slider");
        assertEquals(1, sliders.stream().filter(slider -> slider.getOrientation() == Orientation.VERTICAL).count(),
                "Sliders page should render one vertical slider");
        assertEquals(2, sliders.stream().filter(slider -> slider.getStepSize() > 0.0).count(),
                "Sliders page should render two discrete sliders");
        assertSelectionIndicatorsCentered(scene, "Sliders");
    }

    /// Verifies the real Badges demo page dot, label, overflow, and attached badge geometry.
    private static void assertBadgesPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Badges");
        assertVisibleText(root, "Badges", "Badges");
        assertVisibleText(root, "Attached", "Badges");
        assertVisibleText(root, "Inbox", "Badges");

        List<M3Badge> badges = visibleNodesOfType(root, M3Badge.class);
        assertEquals(4, badges.size(), () -> "Badges page should render four badges, found " + badges.size());
        assertTrue(badges.stream().anyMatch(badge -> badge.getText().isEmpty()),
                "Badges page should render one dot badge");
        assertTrue(badges.stream().anyMatch(badge -> "7".equals(badge.getText())),
                "Badges page should render a one-digit badge");
        assertTrue(badges.stream().anyMatch(badge -> "1234".equals(badge.getText())),
                "Badges page should render an overflow-width badge");
        assertTrue(badges.stream().anyMatch(badge -> "9".equals(badge.getText())),
                "Badges page should render the attached badge");
        for (M3Badge badge : badges) {
            assertBadgeDemoGeometry(badge);
        }

        List<M3BadgedBox> badgedBoxes = visibleNodesOfType(root, M3BadgedBox.class);
        assertEquals(1, badgedBoxes.size(), "Badges page should render one attached badged box");
        assertBadgedBoxDemoGeometry(badgedBoxes.get(0));
    }

    /// Verifies that one demo badge keeps Material badge proportions and visible pixels.
    private static void assertBadgeDemoGeometry(M3Badge badge) {
        Bounds badgeBounds = badge.localToScene(badge.getBoundsInLocal());
        assertTrue(badgeBounds.getWidth() >= 6.0 && badgeBounds.getHeight() >= 6.0,
                () -> "badge is too small to render visibly: " + badgeBounds);
        assertNodeSnapshotHasOpaquePixels(badge, "badge " + badge.getText());

        if (badge.getText().isEmpty()) {
            Rectangle2D renderedBounds = renderedNodePixelBoundsInScene(badge, "dot badge");
            assertTrue(renderedBounds.getWidth() <= 14.0 && renderedBounds.getHeight() <= 14.0,
                    () -> "dot badge should stay visually compact: layout="
                            + badgeBounds + ", pixels=" + renderedBounds);
            assertTrue(Math.abs(renderedBounds.getWidth() - renderedBounds.getHeight()) <= 2.0,
                    () -> "dot badge should render as a near-square shape: layout="
                            + badgeBounds + ", pixels=" + renderedBounds);
            return;
        }

        assertTrue(badgeBounds.getHeight() >= 14.0 && badgeBounds.getHeight() <= 26.0,
                () -> "large badge height is outside the Material range: " + badgeBounds);
        assertTrue(badgeBounds.getWidth() >= badgeBounds.getHeight(),
                () -> "text badge should be at least as wide as it is tall: " + badgeBounds);
        assertNotNull(firstVisibleText(badge), () -> "text badge should render text: " + badge.getText());
    }

    /// Verifies that an attached badge is anchored to its content without taking over layout.
    private static void assertBadgedBoxDemoGeometry(M3BadgedBox box) {
        @Nullable Node content = box.getContent();
        @Nullable M3Badge badge = box.getBadge();
        assertNotNull(content, "demo badged box should have content");
        assertNotNull(badge, "demo badged box should have a badge");

        Bounds boxBounds = box.localToScene(box.getBoundsInLocal());
        Bounds contentBounds = Objects.requireNonNull(content).localToScene(content.getBoundsInLocal());
        Bounds badgeBounds = Objects.requireNonNull(badge).localToScene(badge.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(boxBounds, contentBounds, CONTROL_EDGE_TOLERANCE),
                () -> "badged box content should stay inside the layout bounds: box="
                        + boxBounds + ", content=" + contentBounds);
        assertTrue(containsBoundsWithTolerance(boxBounds, badgeBounds, CONTROL_EDGE_TOLERANCE),
                () -> "attached badge should stay inside the badged box bounds: box="
                        + boxBounds + ", badge=" + badgeBounds);
        boolean rightToLeft = box.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        boolean badgeOnTrailingHalf = rightToLeft
                ? badgeBounds.getCenterX() <= contentBounds.getCenterX() + CONTROL_EDGE_TOLERANCE
                : badgeBounds.getCenterX() >= contentBounds.getCenterX() - CONTROL_EDGE_TOLERANCE;
        assertTrue(badgeOnTrailingHalf,
                () -> "attached badge should sit on the logical trailing half of the content: orientation="
                        + box.getEffectiveNodeOrientation() + ", content=" + contentBounds + ", badge=" + badgeBounds);
        assertTrue(badgeBounds.getCenterY() <= contentBounds.getCenterY() + CONTROL_EDGE_TOLERANCE,
                () -> "attached badge should sit near the top edge of the content: content="
                        + contentBounds + ", badge=" + badgeBounds);
    }

    /// Verifies the real Avatars demo page text, graphic, variant, and list-item avatar states.
    private static void assertAvatarsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Avatars");
        assertVisibleText(root, "Avatars", "Avatars");
        assertVisibleText(root, "List Usage", "Avatars");
        assertVisibleText(root, "Account", "Avatars");
        assertVisibleText(root, "Avatar as leading content", "Avatars");
        assertDemoVectorIcons(root, "Avatars", 1);

        List<M3Avatar> avatars = visibleNodesOfType(root, M3Avatar.class);
        assertEquals(5, avatars.size(), () -> "Avatars page should render five avatars, found " + avatars.size());
        assertEquals(2, avatars.stream().filter(avatar -> avatar.getVariant() == M3AvatarVariant.PRIMARY).count(),
                "Avatars page should render two primary avatars including list usage");
        assertEquals(1, avatars.stream().filter(avatar -> avatar.getVariant() == M3AvatarVariant.SECONDARY).count(),
                "Avatars page should render one secondary avatar");
        assertEquals(1, avatars.stream().filter(avatar -> avatar.getVariant() == M3AvatarVariant.TERTIARY).count(),
                "Avatars page should render one tertiary avatar");
        assertEquals(1, avatars.stream().filter(avatar -> avatar.getVariant() == M3AvatarVariant.SURFACE).count(),
                "Avatars page should render one surface avatar");
        for (M3Avatar avatar : avatars) {
            assertAvatarDemoGeometry(avatar);
        }
    }

    /// Verifies that one demo avatar keeps a stable circular slot and visible label or graphic content.
    private static void assertAvatarDemoGeometry(M3Avatar avatar) {
        Bounds avatarBounds = avatar.localToScene(avatar.getBoundsInLocal());
        assertTrue(avatarBounds.getWidth() >= 36.0 && avatarBounds.getWidth() <= 48.0,
                () -> "avatar width should stay near the 40dp token: " + avatarBounds);
        assertTrue(avatarBounds.getHeight() >= 36.0 && avatarBounds.getHeight() <= 48.0,
                () -> "avatar height should stay near the 40dp token: " + avatarBounds);
        assertTrue(Math.abs(avatarBounds.getWidth() - avatarBounds.getHeight()) <= 1.0,
                () -> "avatar should render as a square circular slot: " + avatarBounds);
        assertNodeSnapshotHasOpaquePixels(avatar, "avatar " + avatar.getText());

        if (avatar.getGraphic() == null) {
            assertNotNull(firstVisibleText(avatar), () -> "text avatar should render its initials: " + avatar.getText());
        } else {
            Bounds graphicBounds = avatar.getGraphic().localToScene(avatar.getGraphic().getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(avatarBounds, graphicBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "avatar graphic should stay inside the avatar slot: avatar="
                            + avatarBounds + ", graphic=" + graphicBounds);
            assertNotNull(firstVisibleDemoVectorIcon(avatar), "graphic avatar should use a demo vector icon");
        }
    }

    /// Verifies the real Dividers demo page horizontal, inset, middle-inset, and vertical states.
    private static void assertDividersPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Dividers");
        assertVisibleText(root, "Horizontal", "Dividers");
        assertVisibleText(root, "Vertical", "Dividers");

        List<M3Divider> dividers = visibleNodesOfType(root, M3Divider.class);
        assertEquals(4, dividers.size(), () -> "Dividers page should render four dividers, found " + dividers.size());
        assertEquals(3, dividers.stream().filter(divider -> divider.getOrientation() == Orientation.HORIZONTAL).count(),
                "Dividers page should render three horizontal dividers");
        assertEquals(1, dividers.stream().filter(divider -> divider.getOrientation() == Orientation.VERTICAL).count(),
                "Dividers page should render one vertical divider");
        assertTrue(dividers.stream().anyMatch(divider -> divider.getInsetStart() > 0.0),
                "Dividers page should render an inset divider");
        assertTrue(dividers.stream().anyMatch(divider -> divider.getInsetStart() > 0.0
                        && divider.getInsetEnd() > 0.0),
                "Dividers page should render a middle-inset divider");
        for (M3Divider divider : dividers) {
            assertDividerDemoGeometry(divider);
        }
    }

    /// Verifies that one demo divider remains a one-dimensional visual separator.
    private static void assertDividerDemoGeometry(M3Divider divider) {
        Bounds dividerBounds = divider.localToScene(divider.getBoundsInLocal());
        assertNodeSnapshotHasOpaquePixels(divider, "divider " + divider.getOrientation());
        if (divider.getOrientation() == Orientation.HORIZONTAL) {
            assertTrue(dividerBounds.getWidth() >= 320.0,
                    () -> "horizontal divider should span the demo sample width: " + dividerBounds);
            assertTrue(dividerBounds.getHeight() >= 0.5 && dividerBounds.getHeight() <= 6.0,
                    () -> "horizontal divider should stay visually thin: " + dividerBounds);
        } else {
            assertTrue(dividerBounds.getHeight() >= 64.0,
                    () -> "vertical divider should span the demo sample height: " + dividerBounds);
            assertTrue(dividerBounds.getWidth() >= 0.5 && dividerBounds.getWidth() <= 6.0,
                    () -> "vertical divider should stay visually thin: " + dividerBounds);
        }
    }

    /// Verifies the real Surfaces demo page color-container variants and elevation states.
    private static void assertSurfacesPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Surfaces");
        assertVisibleText(root, "Surface Tones", "Surfaces");
        assertVisibleText(root, "Container Colors", "Surfaces");
        assertVisibleText(root, "Surface", "Surfaces");
        assertVisibleText(root, "Tertiary", "Surfaces");

        List<M3Surface> surfaces = visibleNodesOfType(root, M3Surface.class);
        assertEquals(6, surfaces.size(), () -> "Surfaces page should render six surfaces, found " + surfaces.size());
        assertTrue(surfaces.stream().anyMatch(surface -> surface.getVariant() == M3SurfaceVariant.SURFACE
                        && surface.getElevation() == M3SurfaceElevation.LEVEL0),
                "Surfaces page should render a level-0 base surface");
        assertTrue(surfaces.stream().anyMatch(surface -> surface.getVariant() == M3SurfaceVariant.CONTAINER_HIGH
                        && surface.getElevation() == M3SurfaceElevation.LEVEL3),
                "Surfaces page should render a high container with level-3 elevation");
        assertTrue(surfaces.stream().anyMatch(surface -> surface.getVariant() == M3SurfaceVariant.PRIMARY_CONTAINER),
                "Surfaces page should render a primary container");
        assertTrue(surfaces.stream().anyMatch(surface -> surface.getVariant() == M3SurfaceVariant.SECONDARY_CONTAINER),
                "Surfaces page should render a secondary container");
        assertTrue(surfaces.stream().anyMatch(surface -> surface.getVariant() == M3SurfaceVariant.TERTIARY_CONTAINER),
                "Surfaces page should render a tertiary container");
        for (M3Surface surface : surfaces) {
            assertSurfaceDemoGeometry(surface);
        }
    }

    /// Verifies that one demo surface keeps its content inside a visible Material container.
    private static void assertSurfaceDemoGeometry(M3Surface surface) {
        Bounds surfaceBounds = surface.localToScene(surface.getBoundsInLocal());
        assertTrue(surfaceBounds.getWidth() >= 170.0 && surfaceBounds.getHeight() >= 88.0,
                () -> "surface should keep the demo card-sized sample bounds: " + surfaceBounds);
        assertFalse(surface.getContent().isEmpty(), "surface demo should expose content");
        assertNotNull(surface.getBackground(), "surface should resolve a Material background");
        assertFalse(surface.getBackground().getFills().isEmpty(), "surface should resolve a visible background fill");
        assertNodeSnapshotHasOpaquePixels(surface, "surface " + surface.getVariant());

        Node content = surface.getContent().get(0);
        Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(surfaceBounds, contentBounds, CONTROL_EDGE_TOLERANCE),
                () -> "surface content should stay inside the container: surface="
                        + surfaceBounds + ", content=" + contentBounds);
    }

    /// Verifies the real Scrims demo page plain and actionable overlay previews.
    private static void assertScrimsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Scrims");
        assertVisibleText(root, "States", "Scrims");
        assertVisibleText(root, "Modal content", "Scrims");
        assertVisibleText(root, "Click scrim", "Scrims");

        List<M3Scrim> scrims = visibleNodesOfType(root, M3Scrim.class);
        assertEquals(2, scrims.size(), () -> "Scrims page should render two scrims, found " + scrims.size());
        assertEquals(1, scrims.stream().filter(scrim -> scrim.getOnAction() != null).count(),
                "Scrims page should render exactly one actionable scrim");
        List<Node> previews = visibleNodesWithStyle(root, "demo-scrim-preview");
        assertEquals(2, previews.size(), "Scrims page should render two fixed preview panes");
        for (Node preview : previews) {
            assertScrimPreviewDemoGeometry(preview);
        }
        for (M3Scrim scrim : scrims) {
            assertScrimDemoGeometry(scrim);
        }
    }

    /// Verifies that a scrim preview keeps the documented demo dimensions.
    private static void assertScrimPreviewDemoGeometry(Node preview) {
        Bounds previewBounds = preview.localToScene(preview.getBoundsInLocal());
        assertEquals(360.0, previewBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> "scrim preview should use the fixed demo width: " + previewBounds);
        assertEquals(180.0, previewBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "scrim preview should use the fixed demo height: " + previewBounds);
        assertNodeSnapshotHasOpaquePixels(preview, "scrim preview");
    }

    /// Verifies that a demo scrim fills its preview surface.
    private static void assertScrimDemoGeometry(M3Scrim scrim) {
        Parent preview = nearestAncestorWithStyle(scrim, "demo-scrim-preview");
        assertNotNull(preview, "scrim should live inside a demo preview");
        Bounds previewBounds = Objects.requireNonNull(preview).localToScene(preview.getBoundsInLocal());
        Bounds scrimBounds = scrim.localToScene(scrim.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(previewBounds, scrimBounds, CONTROL_EDGE_TOLERANCE),
                () -> "scrim should fill but not escape its preview: preview="
                        + previewBounds + ", scrim=" + scrimBounds);
        assertTrue(containsBoundsWithTolerance(scrimBounds, previewBounds, CONTROL_EDGE_TOLERANCE),
                () -> "scrim should cover the full preview: preview="
                        + previewBounds + ", scrim=" + scrimBounds);
        assertNodeSnapshotHasOpaquePixels(scrim, "scrim");
    }

    /// Verifies the real Forms demo page section, row, validation, and embedded control structure.
    private static void assertFormsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Forms");
        assertVisibleText(root, "Structured Form", "Forms");
        assertVisibleText(root, "Account", "Forms");
        assertVisibleText(root, "Preferences", "Forms");
        assertVisibleText(root, "Validation", "Forms");
        assertVisibleText(root, "Display name", "Forms");
        assertVisibleText(root, "Email", "Forms");
        assertVisibleText(root, "Validate form", "Forms");

        List<M3FormPane> forms = visibleNodesOfType(root, M3FormPane.class);
        assertEquals(1, forms.size(), "Forms page should render one form pane");
        assertFormPaneDemoGeometry(forms.get(0));

        List<M3FormSection> sections = visibleNodesOfType(root, M3FormSection.class);
        assertEquals(3, sections.size(), () -> "Forms page should render three sections, found " + sections.size());
        for (M3FormSection section : sections) {
            assertFormSectionDemoGeometry(section);
        }

        List<M3FormRow> rows = visibleNodesOfType(root, M3FormRow.class);
        assertEquals(6, rows.size(), () -> "Forms page should render six form rows, found " + rows.size());
        for (M3FormRow row : rows) {
            assertFormRowDemoGeometry(row);
        }

        assertTrue(visibleNodesOfType(root, M3TextInputLayout.class).size() >= 2,
                "Forms page should render text input layouts");
        assertTrue(visibleNodesOfType(root, M3Switch.class).stream().anyMatch(M3Switch::isSelected),
                "Forms page should render a selected switch");
        assertTrue(visibleNodesOfType(root, M3CheckBox.class).stream().anyMatch(M3CheckBox::isIndeterminate),
                "Forms page should render an indeterminate checkbox");
    }

    /// Verifies that the form pane keeps all top-level items inside its material surface.
    private static void assertFormPaneDemoGeometry(M3FormPane form) {
        Bounds formBounds = form.localToScene(form.getBoundsInLocal());
        assertTrue(formBounds.getWidth() >= 700.0,
                () -> "form pane should keep its wide structured layout: " + formBounds);
        assertEquals(4, form.getItems().size(), "form pane should include summary plus three sections");
        for (Node item : form.getItems()) {
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsHorizontalBoundsWithTolerance(formBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "form pane item should stay horizontally inside the pane: form="
                            + formBounds + ", item=" + itemBounds);
        }
    }

    /// Verifies that one form section lays out its header and rows inside the section bounds.
    private static void assertFormSectionDemoGeometry(M3FormSection section) {
        Bounds sectionBounds = section.localToScene(section.getBoundsInLocal());
        assertFalse(section.getContent().isEmpty(), "form section should expose content rows");
        Node header = requireVisibleStyledDescendant(
                section,
                M3FormSection.HEADER_STYLE_CLASS,
                "form section header"
        );
        Bounds headerBounds = header.localToScene(header.getBoundsInLocal());
        assertTrue(containsHorizontalBoundsWithTolerance(sectionBounds, headerBounds, CONTROL_EDGE_TOLERANCE),
                () -> "form section header should stay inside the section: section="
                        + sectionBounds + ", header=" + headerBounds);
        for (Node row : section.getContent()) {
            Bounds rowBounds = row.localToScene(row.getBoundsInLocal());
            assertTrue(containsHorizontalBoundsWithTolerance(sectionBounds, rowBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "form section row should stay inside the section: section="
                            + sectionBounds + ", row=" + rowBounds);
        }
    }

    /// Verifies that one form row keeps label, supporting text, and content inside the row container.
    private static void assertFormRowDemoGeometry(M3FormRow row) {
        Bounds rowBounds = row.localToScene(row.getBoundsInLocal());
        assertTrue(rowBounds.getHeight() >= 56.0,
                () -> "form row should provide enough vertical room for Material row content: " + rowBounds);
        assertNotNull(firstVisibleText(row), () -> "form row should render a label: " + row.getLabelText());
        @Nullable Node content = row.getContent();
        assertNotNull(content, () -> "form row should expose content for label: " + row.getLabelText());
        Bounds contentBounds = Objects.requireNonNull(content).localToScene(content.getBoundsInLocal());
        assertTrue(containsHorizontalBoundsWithTolerance(rowBounds, contentBounds, CONTROL_EDGE_TOLERANCE),
                () -> "form row content should stay horizontally inside the row: row="
                        + rowBounds + ", content=" + contentBounds);
        @Nullable Node trailing = row.getTrailing();
        if (trailing != null && hasRenderableBounds(trailing)) {
            Bounds trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(rowBounds, trailingBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "form row trailing content should stay inside the row: row="
                            + rowBounds + ", trailing=" + trailingBounds);
        }
    }

    /// Verifies the real Typography demo page token-backed text roles.
    private static void assertTypographyPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Typography");
        assertVisibleText(root, "Scale", "Typography");
        assertVisibleText(root, "Body And Labels", "Typography");
        assertVisibleText(root, "Display Large", "Typography");
        assertVisibleText(root, "Body Large text follows the active theme typography tokens.", "Typography");

        List<M3Text> texts = visibleNodesOfType(root, M3Text.class);
        assertEquals(6, texts.size(), () -> "Typography page should render six M3Text samples, found " + texts.size());
        assertTrue(texts.stream().anyMatch(text -> text.getRole() == M3TextRole.DISPLAY_LARGE),
                "Typography page should render display large");
        assertTrue(texts.stream().anyMatch(text -> text.getRole() == M3TextRole.HEADLINE_MEDIUM),
                "Typography page should render headline medium");
        assertTrue(texts.stream().anyMatch(text -> text.getRole() == M3TextRole.TITLE_LARGE),
                "Typography page should render title large");
        assertTrue(texts.stream().anyMatch(text -> text.getRole() == M3TextRole.LABEL_LARGE),
                "Typography page should render label large");
        assertTrue(texts.stream().anyMatch(text -> text.getRole() == M3TextRole.BODY_LARGE),
                "Typography page should render body large");
        assertTrue(texts.stream().anyMatch(text -> text.getRole() == M3TextRole.BODY_MEDIUM),
                "Typography page should render body medium");
        for (M3Text text : texts) {
            assertTypographyDemoGeometry(text);
        }
    }

    /// Verifies that one typography sample renders non-empty text with usable bounds.
    private static void assertTypographyDemoGeometry(M3Text text) {
        Bounds textBounds = text.localToScene(text.getBoundsInLocal());
        assertTrue(textBounds.getWidth() >= 24.0 && textBounds.getHeight() >= 10.0,
                () -> "typography sample should have visible text bounds: " + textBounds);
        assertNotNull(firstVisibleText(text), () -> "M3Text should render a concrete text node: " + text.getText());
    }

    /// Verifies the real Icons demo page vector size, color, and button usage examples.
    private static void assertIconsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Icons");
        assertVisibleText(root, "Sizes", "Icons");
        assertVisibleText(root, "Color Variants", "Icons");
        assertVisibleText(root, "Button Usage", "Icons");
        assertDemoVectorIcons(root, "Icons", 12);

        List<Node> icons = visibleNodesWithStyle(root, DemoIcons.STYLE_CLASS);
        assertTrue(icons.size() >= 12, () -> "Icons page should render at least 12 vector icons, found " + icons.size());
        for (Node icon : icons) {
            Bounds iconBounds = icon.localToScene(icon.getBoundsInLocal());
            assertTrue(iconBounds.getWidth() >= 8.0 && iconBounds.getHeight() >= 8.0,
                    () -> "demo vector icon should render with visible geometry: " + iconBounds);
        }
        assertTrue(visibleNodesOfType(root, M3IconButton.class).size() >= 2,
                "Icons page should render icon button usage");
        assertTrue(visibleNodesOfType(root, M3FloatingActionButton.class).size() >= 2,
                "Icons page should render FAB icon usage");
    }

    /// Verifies the real Tooltips demo page owner controls and vector icon trigger.
    private static void assertTooltipsPageVisualState(Scene scene) {
        Parent root = scene.getRoot();
        assertCurrentPageTitle(scene, "Tooltips");
        assertVisibleText(root, "Plain", "Tooltips");
        assertVisibleText(root, "Rich", "Tooltips");
        assertVisibleText(root, "Hover me", "Tooltips");
        assertVisibleText(root, "Long tooltip", "Tooltips");
        assertVisibleText(root, "Rich tooltip", "Tooltips");
        assertVisibleText(root, "Rich action", "Tooltips");
        assertDemoVectorIcons(root, "Tooltips", 1);

        assertNotNull(firstVisibleButtonWithText(root, "Hover me"), "plain tooltip owner should be visible");
        assertNotNull(firstVisibleButtonWithText(root, "Long tooltip"), "long tooltip owner should be visible");
        assertNotNull(firstVisibleButtonWithText(root, "Rich tooltip"), "rich tooltip owner should be visible");
        assertNotNull(firstVisibleButtonWithText(root, "Rich action"), "rich action tooltip owner should be visible");
        assertTrue(visibleNodesOfType(root, M3IconButton.class).size() >= 1,
                "Tooltips page should render an icon-button tooltip owner");
    }

    /// Writes a full-scene page snapshot to the demo visual report directory.
    private static void writePageSnapshot(Scene scene, String fileName, String label) {
        WritableImage image = snapshot(scene);
        writeVisualSnapshot(image, Path.of(
                "build",
                "reports",
                "m3fx-demo-visual",
                fileName
        ));
        assertSnapshotHasVisibleContent(image, label);
    }

    /// Verifies that selection-control indicators keep their active pieces centered in real rendered geometry.
    private static void assertSelectionIndicatorsCentered(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        AtomicReference<@Nullable WritableImage> snapshotReference = new AtomicReference<>();
        visitVisibleNodes(scene.getRoot(), node -> {
            if (node instanceof M3CheckBox checkBox && hasRenderableBounds(checkBox)) {
                assertCheckboxMarkCentered(
                        checkBox,
                        sceneBounds,
                        selectionGeometrySnapshot(scene, snapshotReference),
                        pageTitle
                );
            } else if (node instanceof M3RadioButton radioButton && hasRenderableBounds(radioButton)) {
                assertRadioDotCentered(
                        radioButton,
                        sceneBounds,
                        selectionGeometrySnapshot(scene, snapshotReference),
                        pageTitle
                );
            } else if (node instanceof M3Switch switchControl && hasRenderableBounds(switchControl)) {
                assertSwitchThumbInsideTrack(
                        switchControl,
                        sceneBounds,
                        selectionGeometrySnapshot(scene, snapshotReference),
                        pageTitle
                );
            } else if (node instanceof M3Slider slider && hasRenderableBounds(slider)) {
                assertSliderTrackThumbGeometry(
                        slider,
                        sceneBounds,
                        selectionGeometrySnapshot(scene, snapshotReference),
                        pageTitle
                );
            }
        });
    }

    /// Returns the shared snapshot used by selection-control pixel geometry checks.
    private static WritableImage selectionGeometrySnapshot(
            Scene scene,
            AtomicReference<@Nullable WritableImage> snapshotReference
    ) {
        WritableImage image = snapshotReference.get();
        if (image == null) {
            image = snapshot(scene);
            snapshotReference.set(image);
        }
        return image;
    }

    /// Verifies that navigation badges stay compact and anchored instead of stretching over the selected indicator.
    private static void assertNavigationBadgesStayCompact(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3NavigationItem item) || !hasRenderableBounds(item)) {
                return;
            }

            @Nullable Node badge = item.lookup(".m3-navigation-item-badge");
            @Nullable Node indicator = item.lookup(".m3-navigation-item-indicator");
            if (badge == null || indicator == null || !hasRenderableBounds(badge) || !hasRenderableBounds(indicator)) {
                return;
            }

            Bounds badgeBounds = badge.localToScene(badge.getBoundsInLocal());
            Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
            if (isOutsideSceneViewport(badge, badgeBounds, sceneBounds)) {
                return;
            }

            double maximumBadgeWidth = Math.max(24.0, indicatorBounds.getWidth() * 0.65);
            double minimumCenterOffset = Math.max(6.0, indicatorBounds.getWidth() * 0.16);
            double centerOffset = Math.abs(badgeBounds.getCenterX() - indicatorBounds.getCenterX());
            assertTrue(badgeBounds.getWidth() <= maximumBadgeWidth && centerOffset >= minimumCenterOffset,
                    () -> pageTitle + " navigation badge has unsafe indicator geometry: badgeBounds="
                            + badgeBounds + ", indicatorBounds=" + indicatorBounds + ", centerOffset=" + centerOffset);
        });
    }

    /// Verifies that an app bar fills the horizontal span of its demo preview surface.
    private static void assertAppBarFitsPreview(Node appBar, String description) {
        Parent preview = Objects.requireNonNull(
                nearestAncestorWithStyle(appBar, "demo-app-bar-preview"),
                description + " preview"
        );
        Bounds previewBounds = preview.localToScene(preview.getLayoutBounds());
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        assertTrue(appBarBounds.getWidth() >= APP_BAR_PREVIEW_MIN_WIDTH,
                () -> description + " is too narrow for an app preview: bounds=" + appBarBounds);
        assertTrue(Math.abs(appBarBounds.getMinX() - previewBounds.getMinX()) <= CONTROL_EDGE_TOLERANCE
                        && Math.abs(appBarBounds.getMaxX() - previewBounds.getMaxX()) <= CONTROL_EDGE_TOLERANCE,
                () -> description + " does not fill its preview horizontally: appBarBounds=" + appBarBounds
                        + ", previewBounds=" + previewBounds);
        assertTrue(appBarBounds.getHeight() >= 60.0,
                () -> description + " height is too small: bounds=" + appBarBounds);
    }

    /// Verifies that a toolbar demo instance resolves container, slot, and icon geometry.
    private static void assertToolbarDemoGeometry(M3Toolbar toolbar) {
        Bounds toolbarBounds = toolbar.localToScene(toolbar.getLayoutBounds());
        assertNotNull(toolbar.getBackground(), "toolbar should resolve a Material background");
        assertFalse(toolbar.getBackground().getFills().isEmpty(), "toolbar should resolve a visible background fill");
        if (toolbar.getOrientation() == Orientation.HORIZONTAL) {
            assertEquals(toolbar.getContainerHeight(), toolbarBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "horizontal toolbar height should follow its container token: " + toolbarBounds);
            assertTrue(toolbarBounds.getWidth() >= toolbar.getItems().size() * toolbar.getItemSlotSize(),
                    () -> "horizontal toolbar is too narrow for its action slots: " + toolbarBounds);
        } else {
            assertEquals(toolbar.getContainerWidth(), toolbarBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    () -> "vertical toolbar width should follow its container token: " + toolbarBounds);
            assertTrue(toolbarBounds.getHeight() >= toolbar.getItems().size() * toolbar.getItemSlotSize(),
                    () -> "vertical toolbar is too short for its action slots: " + toolbarBounds);
        }

        List<Node> itemSlots = visibleNodesWithStyle(toolbar, M3Toolbar.ITEM_SLOT_STYLE_CLASS);
        assertEquals(toolbar.getItems().size(), itemSlots.size(), "toolbar item slot count");
        for (Node itemSlot : itemSlots) {
            Bounds slotBounds = itemSlot.localToScene(itemSlot.getLayoutBounds());
            assertTrue(containsBoundsWithTolerance(toolbarBounds, slotBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "toolbar item slot should stay inside toolbar bounds: toolbar="
                            + toolbarBounds + ", slot=" + slotBounds);
            assertTrue(slotBounds.getWidth() >= toolbar.getItemSlotSize() - CONTROL_EDGE_TOLERANCE,
                    () -> "toolbar item slot is narrower than its token: " + slotBounds);
            assertTrue(slotBounds.getHeight() >= toolbar.getItemSlotSize() - CONTROL_EDGE_TOLERANCE,
                    () -> "toolbar item slot is shorter than its token: " + slotBounds);
        }
        assertToolbarUsesVectorIconButtons(toolbar);
    }

    /// Verifies that toolbar actions use centered SVG icon buttons rather than text placeholders.
    private static void assertToolbarUsesVectorIconButtons(M3Toolbar toolbar) {
        List<M3IconButton> iconButtons = visibleNodesOfType(toolbar, M3IconButton.class);
        assertEquals(toolbar.getItems().size(), iconButtons.size(), "toolbar action button count");
        for (M3IconButton iconButton : iconButtons) {
            assertTrue(iconButton.getText().isEmpty(),
                    () -> "toolbar icon button should not expose placeholder text: " + iconButton.getText());
            Node graphic = Objects.requireNonNull(iconButton.getGraphic(), "toolbar icon button graphic");
            assertNull(firstVisibleText(graphic),
                    () -> "toolbar icon button graphic should not contain rendered text placeholders: " + graphic);
            @Nullable Node vectorIcon = firstVisibleDemoVectorIcon(graphic);
            assertNotNull(vectorIcon, "toolbar icon button should contain a visible demo vector icon graphic");
            assertToolbarIconButtonGeometry(iconButton, graphic, Objects.requireNonNull(vectorIcon));
        }
    }

    /// Verifies that a toolbar icon button uses a centered 24 dp vector icon viewport.
    private static void assertToolbarIconButtonGeometry(M3IconButton iconButton, Node graphic, Node vectorIcon) {
        Bounds buttonBounds = iconButton.localToScene(iconButton.getBoundsInLocal());
        Bounds graphicBounds = graphic.localToScene(graphic.getBoundsInLocal());
        @Nullable Parent viewport = nearestAncestorWithStyle(vectorIcon, "demo-vector-icon-viewport");
        assertNotNull(viewport, "toolbar icon SVG is not wrapped in the fixed demo icon viewport");
        Bounds viewportBounds = Objects.requireNonNull(viewport).localToScene(viewport.getBoundsInLocal());

        assertTrue(containsBoundsWithTolerance(buttonBounds, graphicBounds, CONTROL_EDGE_TOLERANCE),
                () -> "toolbar icon graphic leaves its button target: button="
                        + buttonBounds + ", graphic=" + graphicBounds);
        assertEquals(DEMO_ICON_VIEWPORT_SIZE, viewportBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> "toolbar icon viewport width is not 24dp: " + viewportBounds);
        assertEquals(DEMO_ICON_VIEWPORT_SIZE, viewportBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "toolbar icon viewport height is not 24dp: " + viewportBounds);
        assertEquals(buttonBounds.getCenterX(), viewportBounds.getCenterX(), DEMO_ICON_CENTER_TOLERANCE,
                () -> "toolbar icon viewport is horizontally off-center inside its action target: button="
                        + buttonBounds + ", viewport=" + viewportBounds);
        assertEquals(buttonBounds.getCenterY(), viewportBounds.getCenterY(), DEMO_ICON_CENTER_TOLERANCE,
                () -> "toolbar icon viewport is vertically off-center inside its action target: button="
                        + buttonBounds + ", viewport=" + viewportBounds);
        assertNodeSnapshotHasOpaquePixels(vectorIcon, "toolbar icon");
    }

    /// Verifies that a top app bar preview uses token height and includes an app content context.
    private static void assertTopAppBarPreviewBalance(M3TopAppBar appBar) {
        Parent preview = Objects.requireNonNull(
                nearestAncestorWithStyle(appBar, "demo-app-bar-preview"),
                "top app bar preview"
        );
        Node previewContent = requireVisibleStyledDescendant(
                preview,
                "demo-top-app-bar-preview-content",
                "top app bar preview content"
        );
        Bounds previewBounds = preview.localToScene(preview.getLayoutBounds());
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        Bounds previewContentBounds = previewContent.localToScene(previewContent.getLayoutBounds());
        double expectedHeight = expectedTopAppBarHeight(appBar);

        assertEquals(expectedHeight, appBarBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar variant height drifted: variant=" + appBar.getVariant()
                        + ", expected=" + expectedHeight + ", bounds=" + appBarBounds);
        assertEquals(previewBounds.getMinY(), appBarBounds.getMinY(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar should sit at the top edge of its preview: variant=" + appBar.getVariant()
                        + ", previewBounds=" + previewBounds + ", appBarBounds=" + appBarBounds);
        assertTrue(previewContentBounds.getMinY() >= appBarBounds.getMaxY() - CONTROL_EDGE_TOLERANCE,
                () -> "top app bar preview content should be below the bar: variant=" + appBar.getVariant()
                        + ", contentBounds=" + previewContentBounds + ", appBarBounds=" + appBarBounds);
        assertTrue(previewBounds.getHeight() >= expectedHeight + 96.0,
                () -> "top app bar preview should include visible app content context: variant=" + appBar.getVariant()
                        + ", previewBounds=" + previewBounds + ", appBarBounds=" + appBarBounds);
        assertTopAppBarPreviewSurfaceTreatment(appBar, preview, previewContent);
    }

    /// Verifies that default and scrolled-under previews use the expected Material surface treatment.
    private static void assertTopAppBarPreviewSurfaceTreatment(
            M3TopAppBar appBar,
            Parent preview,
            Node previewContent
    ) {
        Region previewRegion = assertInstanceOf(Region.class, preview, "top app bar preview region");
        Region contentRegion = assertInstanceOf(Region.class, previewContent, "top app bar preview content region");
        assertNotNull(previewRegion.getBorder(), "top app bar preview should expose a visible app frame border");
        assertFalse(previewRegion.getBorder().getStrokes().isEmpty(),
                "top app bar preview should expose a visible app frame border");

        Color appBarColor = Objects.requireNonNull(firstBackgroundColor(appBar), "top app bar background color");
        Color contentColor = Objects.requireNonNull(
                firstBackgroundColor(contentRegion),
                "top app bar preview content background color"
        );
        double surfaceDistance = colorDistance(appBarColor, contentColor);
        if (appBar.isScrolledUnder()) {
            assertTrue(surfaceDistance > 0.01,
                    () -> "scrolled-under top app bar should use a distinct container surface: appBar="
                            + appBarColor + ", content=" + contentColor);
            assertNotNull(appBar.getEffect(), "scrolled-under top app bar should expose elevation in the preview");
        } else {
            assertTrue(surfaceDistance <= 0.01,
                    () -> "default top app bar should share the app content surface until content scrolls under it: appBar="
                            + appBarColor + ", content=" + contentColor);
            assertNull(appBar.getEffect(), "default top app bar should not show scrolled-under elevation");
        }
    }

    /// Verifies that app bar previews are not placed in the generic rounded showcase container.
    private static void assertAppBarPreviewIsNotInsideGenericFlow(M3TopAppBar appBar) {
        @Nullable Parent preview = nearestAncestorWithStyle(appBar, "demo-app-bar-preview");
        assertNotNull(preview, "top app bar preview");
        @Nullable Node genericFlow = nearestDemoFlowAncestor(preview);
        assertNull(genericFlow, () -> "top app bar preview should not sit inside the generic rounded demo flow: "
                + genericFlow);
    }

    /// Verifies that a bottom app bar preview frames only the toolbar component.
    private static void assertBottomAppBarPreviewBalance(M3BottomAppBar appBar) {
        Parent preview = Objects.requireNonNull(
                nearestAncestorWithStyle(appBar, "demo-app-bar-preview"),
                "bottom app bar preview"
        );
        Bounds previewBounds = preview.localToScene(preview.getBoundsInLocal());
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        double expectedHeight = appBar.getContainerHeight();

        assertEquals(expectedHeight, appBarBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "bottom app bar token height drifted: expected=" + expectedHeight
                        + ", bounds=" + appBarBounds);
        assertTrue(previewBounds.getHeight() >= expectedHeight
                        && previewBounds.getHeight() <= expectedHeight + CONTROL_EDGE_TOLERANCE * 2.0,
                () -> "bottom app bar preview should frame only the component and its outline: previewBounds="
                        + previewBounds + ", appBarBounds=" + appBarBounds);
        assertTrue(containsBoundsWithTolerance(previewBounds, appBarBounds, CONTROL_EDGE_TOLERANCE),
                () -> "bottom app bar should remain inside its preview frame: previewBounds="
                        + previewBounds + ", appBarBounds=" + appBarBounds);
    }

    /// Verifies that a demo bottom app bar places its floating action slot with Material geometry.
    private static void assertBottomAppBarSlotGeometry(M3BottomAppBar appBar) {
        Node actions = requireVisibleStyledDescendant(
                appBar,
                M3BottomAppBar.ACTIONS_STYLE_CLASS,
                "bottom app bar actions slot"
        );
        Node floatingAction = requireVisibleStyledDescendant(
                appBar,
                M3BottomAppBar.FLOATING_ACTION_STYLE_CLASS,
                "bottom app bar floating action slot"
        );
        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        Bounds actionsBounds = actions.localToScene(actions.getLayoutBounds());
        Bounds floatingActionBounds = floatingAction.localToScene(floatingAction.getLayoutBounds());
        List<Node> actionSlots = visibleNodesWithStyle(appBar, M3BottomAppBar.ACTION_SLOT_STYLE_CLASS);
        double contentMinX = appBarBounds.getMinX() + appBar.getHorizontalPadding();
        double contentMaxX = appBarBounds.getMaxX() - appBar.getHorizontalPadding();
        double contentCenterX = (contentMinX + contentMaxX) / 2.0;
        boolean rightToLeft = appBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        assertEquals(appBarBounds.getCenterY(), actionsBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                () -> "bottom app bar actions are not vertically centered: actionsBounds=" + actionsBounds
                        + ", appBarBounds=" + appBarBounds);
        assertEquals(appBarBounds.getCenterY(), floatingActionBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                () -> "bottom app bar floating action is not vertically centered: floatingActionBounds="
                        + floatingActionBounds + ", appBarBounds=" + appBarBounds);
        assertBottomAppBarActionSlots(appBar, actionSlots, appBarBounds.getCenterY());

        if (appBar.getFloatingActionAlignment() == M3BottomAppBarFloatingActionAlignment.CENTER) {
            assertEquals(contentCenterX, floatingActionBounds.getCenterX(), APP_BAR_SLOT_TOLERANCE,
                    () -> "center bottom app bar floating action should stay centered in the full content area: "
                            + floatingActionBounds + ", appBarBounds=" + appBarBounds);
        } else if (appBar.getFloatingActionAlignment() == M3BottomAppBarFloatingActionAlignment.START) {
            if (rightToLeft) {
                assertEquals(contentMaxX, floatingActionBounds.getMaxX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "RTL start bottom app bar floating action should be on the physical right edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            } else {
                assertEquals(contentMinX, floatingActionBounds.getMinX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "start bottom app bar floating action should be on the leading edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            }
        } else {
            if (rightToLeft) {
                assertEquals(contentMinX, floatingActionBounds.getMinX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "RTL end bottom app bar floating action should be on the physical left edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            } else {
                assertEquals(contentMaxX, floatingActionBounds.getMaxX(), APP_BAR_SLOT_TOLERANCE,
                        () -> "end bottom app bar floating action should be on the trailing edge: "
                                + floatingActionBounds + ", appBarBounds=" + appBarBounds);
            }
        }
    }

    /// Verifies that a demo bottom app bar uses 48 dp Material action slots.
    private static void assertBottomAppBarActionSlots(
            M3BottomAppBar appBar,
            List<Node> actionSlots,
            double rowCenterY
    ) {
        assertEquals(appBar.getActions().size(), actionSlots.size(),
                () -> "bottom app bar action slot count mismatch: expected=" + appBar.getActions().size()
                        + ", actual=" + actionSlots.size());

        @Nullable Bounds previousBounds = null;
        for (Node actionSlot : actionSlots) {
            Bounds slotBounds = actionSlot.localToScene(actionSlot.getBoundsInLocal());
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    () -> "bottom app bar action slot width is not 48dp: " + slotBounds);
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "bottom app bar action slot height is not 48dp: " + slotBounds);
            assertEquals(rowCenterY, slotBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                    () -> "bottom app bar action slot is not centered in the bar row: "
                            + slotBounds + ", rowCenterY=" + rowCenterY);
            if (previousBounds != null) {
                Bounds previousSlotBounds = previousBounds;
                assertEquals(appBar.getActionSpacing(), slotBounds.getMinX() - previousSlotBounds.getMaxX(),
                        CONTROL_EDGE_TOLERANCE,
                        () -> "bottom app bar action slots do not use the configured spacing: previous="
                                + previousSlotBounds + ", slot=" + slotBounds);
            }
            previousBounds = slotBounds;
        }
    }

    /// Returns the expected rendered height for a top app bar variant.
    private static double expectedTopAppBarHeight(M3TopAppBar appBar) {
        return switch (appBar.getVariant()) {
            case MEDIUM -> appBar.getMediumContainerHeight();
            case LARGE -> appBar.getLargeContainerHeight();
            case SMALL, CENTER_ALIGNED -> appBar.getContainerHeight();
        };
    }

    /// Verifies that top app bar slots use token-backed padding and variant-specific title placement.
    private static void assertTopAppBarSlotGeometry(M3TopAppBar appBar) {
        Node navigation = requireVisibleStyledDescendant(
                appBar,
                M3TopAppBar.NAVIGATION_STYLE_CLASS,
                "top app bar navigation slot"
        );
        Node title = requireVisibleStyledDescendant(
                appBar,
                M3TopAppBar.TITLE_STYLE_CLASS,
                "top app bar title slot"
        );
        Node actions = requireVisibleStyledDescendant(
                appBar,
                M3TopAppBar.ACTIONS_STYLE_CLASS,
                "top app bar actions slot"
        );

        Bounds appBarBounds = appBar.localToScene(appBar.getLayoutBounds());
        Bounds navigationBounds = navigation.localToScene(navigation.getBoundsInLocal());
        Bounds titleBounds = title.localToScene(title.getBoundsInLocal());
        Bounds actionsBounds = actions.localToScene(actions.getBoundsInLocal());
        Node titleText = Objects.requireNonNull(firstVisibleText(title), "top app bar rendered title text");
        Bounds titleTextBounds = titleText.localToScene(titleText.getBoundsInLocal());
        List<Node> actionSlots = visibleNodesWithStyle(appBar, M3TopAppBar.ACTION_SLOT_STYLE_CLASS);
        double horizontalPadding = appBar.getHorizontalPadding();
        double rowCenterY = appBarBounds.getMinY() + appBar.getContainerHeight() / 2.0;
        boolean rightToLeft = appBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        assertTopAppBarActionSlots(appBar, navigation, actionSlots, rowCenterY);
        if (rightToLeft) {
            assertEquals(
                    appBarBounds.getMaxX() - horizontalPadding,
                    navigationBounds.getMaxX(),
                    APP_BAR_SLOT_TOLERANCE,
                    () -> "RTL top app bar navigation slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", navigationBounds=" + navigationBounds
            );
            assertEquals(
                    appBarBounds.getMinX() + horizontalPadding,
                    actionsBounds.getMinX(),
                    APP_BAR_SLOT_TOLERANCE,
                    () -> "RTL top app bar actions slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", actionsBounds=" + actionsBounds
            );
        } else {
            assertEquals(appBarBounds.getMinX() + horizontalPadding, navigationBounds.getMinX(), APP_BAR_SLOT_TOLERANCE,
                    () -> "top app bar navigation slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", navigationBounds=" + navigationBounds);
            assertEquals(appBarBounds.getMaxX() - horizontalPadding, actionsBounds.getMaxX(), APP_BAR_SLOT_TOLERANCE,
                    () -> "top app bar actions slot does not respect horizontal padding: variant="
                            + appBar.getVariant() + ", appBarBounds=" + appBarBounds
                            + ", actionsBounds=" + actionsBounds);
        }

        switch (appBar.getVariant()) {
            case CENTER_ALIGNED -> assertEquals(
                    appBarBounds.getCenterX(),
                    titleTextBounds.getCenterX(),
                    APP_BAR_SLOT_TOLERANCE,
                    () -> "center-aligned top app bar title is not centered: titleBounds=" + titleBounds
                            + ", titleTextBounds=" + titleTextBounds
                            + ", appBarBounds=" + appBarBounds
            );
            case MEDIUM, LARGE -> {
                double bottomPadding = appBar.getVariant() == M3TopAppBarVariant.MEDIUM
                        ? appBar.getMediumBottomPadding()
                        : appBar.getLargeBottomPadding();
                if (rightToLeft) {
                    assertEquals(
                            appBarBounds.getMaxX() - horizontalPadding,
                            titleTextBounds.getMaxX(),
                            APP_BAR_SLOT_TOLERANCE,
                            () -> "RTL tall top app bar title text does not respect leading padding: variant="
                                    + appBar.getVariant() + ", titleTextBounds=" + titleTextBounds
                                    + ", titleBounds=" + titleBounds + ", appBarBounds=" + appBarBounds
                    );
                } else {
                    assertEquals(
                            appBarBounds.getMinX() + horizontalPadding,
                            titleBounds.getMinX(),
                            APP_BAR_SLOT_TOLERANCE,
                            () -> "tall top app bar title does not respect leading padding: variant="
                                    + appBar.getVariant() + ", titleBounds=" + titleBounds
                                    + ", appBarBounds=" + appBarBounds
                    );
                }
                assertEquals(appBarBounds.getMaxY() - bottomPadding, titleBounds.getMaxY(), APP_BAR_SLOT_TOLERANCE,
                        () -> "tall top app bar title does not respect bottom padding: variant="
                                + appBar.getVariant() + ", titleBounds=" + titleBounds
                                + ", appBarBounds=" + appBarBounds);
                assertTrue(titleBounds.getCenterY() > appBarBounds.getCenterY(),
                        () -> "tall top app bar title should sit below the icon row: variant="
                                + appBar.getVariant() + ", titleBounds=" + titleBounds
                                + ", appBarBounds=" + appBarBounds);
            }
            case SMALL -> {
                assertEquals(rowCenterY, titleTextBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                        () -> "small top app bar title is not centered in the toolbar row: titleTextBounds="
                                + titleTextBounds + ", titleBounds=" + titleBounds + ", appBarBounds=" + appBarBounds);
                if (rightToLeft) {
                    assertTrue(titleTextBounds.getMaxX() <= navigationBounds.getMinX() + APP_BAR_SLOT_TOLERANCE,
                            () -> "RTL small top app bar title text overlaps navigation slot: titleTextBounds="
                                    + titleTextBounds + ", navigationBounds=" + navigationBounds);
                } else {
                    assertTrue(titleBounds.getMinX() >= navigationBounds.getMaxX() - APP_BAR_SLOT_TOLERANCE,
                            () -> "small top app bar title overlaps navigation slot: titleBounds="
                                    + titleBounds + ", navigationBounds=" + navigationBounds);
                }
            }
        }
    }

    /// Verifies that a demo top app bar uses 48 dp Material navigation and action slots.
    private static void assertTopAppBarActionSlots(
            M3TopAppBar appBar,
            Node navigation,
            List<Node> actionSlots,
            double rowCenterY
    ) {
        Bounds navigationBounds = navigation.localToScene(navigation.getBoundsInLocal());

        assertEquals(APP_BAR_ACTION_SLOT_SIZE, navigationBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar navigation slot width is not 48dp: " + navigationBounds);
        assertEquals(APP_BAR_ACTION_SLOT_SIZE, navigationBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> "top app bar navigation slot height is not 48dp: " + navigationBounds);
        assertEquals(rowCenterY, navigationBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                () -> "top app bar navigation slot is not centered in the icon row: "
                        + navigationBounds + ", rowCenterY=" + rowCenterY);
        assertEquals(appBar.getActions().size(), actionSlots.size(),
                () -> "top app bar action slot count mismatch: expected=" + appBar.getActions().size()
                        + ", actual=" + actionSlots.size());

        @Nullable Bounds previousBounds = null;
        for (Node actionSlot : actionSlots) {
            Bounds slotBounds = actionSlot.localToScene(actionSlot.getBoundsInLocal());
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                    () -> "top app bar action slot width is not 48dp: " + slotBounds);
            assertEquals(APP_BAR_ACTION_SLOT_SIZE, slotBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                    () -> "top app bar action slot height is not 48dp: " + slotBounds);
            assertEquals(rowCenterY, slotBounds.getCenterY(), APP_BAR_SLOT_TOLERANCE,
                    () -> "top app bar action slot is not centered in the icon row: "
                            + slotBounds + ", rowCenterY=" + rowCenterY);
            if (previousBounds != null) {
                Bounds previousSlotBounds = previousBounds;
                assertEquals(appBar.getActionSpacing(), slotBounds.getMinX() - previousSlotBounds.getMaxX(),
                        CONTROL_EDGE_TOLERANCE,
                        () -> "top app bar action slots do not use the configured spacing: previous="
                                + previousSlotBounds + ", slot=" + slotBounds);
            }
            previousBounds = slotBounds;
        }
    }

    /// Verifies that app bar actions are icon-only vector buttons rather than text placeholders.
    private static void assertAppBarUsesVectorIconButtons(Node appBar, String description, String... expectedIconNames) {
        List<M3IconButton> iconButtons = visibleNodesOfType(appBar, M3IconButton.class);
        assertEquals(expectedIconNames.length, iconButtons.size(),
                () -> description + " should use icon button actions");
        for (int index = 0; index < iconButtons.size(); index++) {
            M3IconButton iconButton = iconButtons.get(index);
            assertTrue(iconButton.getText().isEmpty(),
                    () -> description + " icon button should not expose placeholder text: " + iconButton.getText());
            Node graphic = Objects.requireNonNull(iconButton.getGraphic(),
                    description + " icon button graphic");
            assertNull(firstVisibleText(graphic),
                    () -> description + " icon button graphic should not contain rendered text placeholders: "
                            + graphic);
            @Nullable Node vectorIcon = firstVisibleDemoVectorIcon(graphic);
            assertNotNull(vectorIcon,
                    () -> description + " icon button should contain a visible demo vector icon graphic");
            String expectedIconColorStyle = expectedAppBarIconColorStyle(appBar, iconButton);
            assertTrue(vectorIcon.getStyleClass().contains(expectedIconColorStyle),
                    () -> description + " icon button should use the Material app bar icon color "
                            + expectedIconColorStyle + ": " + vectorIcon.getStyleClass());
            String expectedIconName = expectedIconNames[index];
            assertAppBarIconIdentity(vectorIcon, expectedIconName, description);
            assertAppBarIconButtonGeometry(iconButton, graphic, vectorIcon, expectedIconName, description);
        }
    }

    /// Verifies that an app bar icon button renders the expected logical SVG asset.
    private static void assertAppBarIconIdentity(Node vectorIcon, String expectedIconName, String description) {
        assertEquals(expectedIconName, vectorIcon.getProperties().get(DemoIcons.ICON_NAME_PROPERTY),
                () -> description + " icon button uses the wrong demo icon: expected=" + expectedIconName
                        + ", properties=" + vectorIcon.getProperties());
        SVGPath svgPath = assertInstanceOf(SVGPath.class, vectorIcon,
                () -> description + " app bar icon should be an SVGPath");
        assertEquals(DemoIcons.path(expectedIconName), svgPath.getContent(),
                () -> description + " app bar icon path does not match " + expectedIconName);
    }

    /// Returns the expected semantic icon color style for an app bar action slot.
    private static String expectedAppBarIconColorStyle(Node appBar, M3IconButton iconButton) {
        if (appBar instanceof M3TopAppBar topAppBar && topAppBar.getNavigation() == iconButton) {
            return DemoIcons.ON_SURFACE_STYLE_CLASS;
        }
        return DemoIcons.ON_SURFACE_VARIANT_STYLE_CLASS;
    }

    /// Verifies that a demo app bar icon button uses Material-sized centered vector geometry.
    private static void assertAppBarIconButtonGeometry(
            M3IconButton iconButton,
            Node graphic,
            Node vectorIcon,
            String expectedIconName,
            String description
    ) {
        Bounds buttonBounds = iconButton.localToScene(iconButton.getBoundsInLocal());
        Bounds graphicBounds = graphic.localToScene(graphic.getBoundsInLocal());
        Bounds vectorIconBounds = vectorIcon.localToScene(vectorIcon.getBoundsInLocal());
        @Nullable Parent viewport = nearestAncestorWithStyle(vectorIcon, "demo-vector-icon-viewport");
        assertNotNull(viewport,
                () -> description + " icon button SVG is not wrapped in the fixed demo icon viewport");
        Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());

        assertEquals(APP_BAR_ICON_BUTTON_SIZE, buttonBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon button width does not match the Material action target: "
                        + buttonBounds);
        assertEquals(APP_BAR_ICON_BUTTON_SIZE, buttonBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon button height does not match the Material action target: "
                        + buttonBounds);
        assertEquals(DEMO_ICON_VIEWPORT_SIZE, viewportBounds.getWidth(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon viewport width is not 24dp: " + viewportBounds);
        assertEquals(DEMO_ICON_VIEWPORT_SIZE, viewportBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " icon viewport height is not 24dp: " + viewportBounds);
        assertTrue(containsBoundsWithTolerance(buttonBounds, graphicBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " icon graphic leaves its button target: button="
                        + buttonBounds + ", graphic=" + graphicBounds);
        assertTrue(containsBoundsWithTolerance(viewportBounds, vectorIconBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " SVG path leaves its 24dp viewport: viewport="
                        + viewportBounds + ", path=" + vectorIconBounds);
        assertEquals(buttonBounds.getCenterX(), viewportBounds.getCenterX(), DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " icon viewport is horizontally off-center inside its action target: button="
                        + buttonBounds + ", viewport=" + viewportBounds);
        assertEquals(buttonBounds.getCenterY(), viewportBounds.getCenterY(), DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " icon viewport is vertically off-center inside its action target: button="
                        + buttonBounds + ", viewport=" + viewportBounds);
        assertAppBarIconHasVisiblePaint(vectorIcon, description);
        assertAppBarIconRenderedShape(viewport, expectedIconName, description);
    }

    /// Verifies that an app bar icon renders visible SVG pixels with contrast against its app bar container.
    private static void assertAppBarIconHasVisiblePaint(Node vectorIcon, String description) {
        assertNodeSnapshotHasOpaquePixels(vectorIcon, description + " icon");
        assertInstanceOf(SVGPath.class, vectorIcon,
                () -> description + " demo vector icon should be an SVG path");

        SVGPath svgPath = (SVGPath) vectorIcon;
        assertInstanceOf(Color.class, svgPath.getFill(),
                () -> description + " demo vector icon should resolve to a concrete fill color");
        Color iconColor = (Color) svgPath.getFill();
        assertTrue(iconColor.getOpacity() > 0.1,
                () -> description + " demo vector icon fill is transparent: " + iconColor);

        @Nullable Region appBar = nearestAppBarRegion(vectorIcon);
        assertNotNull(appBar, () -> description + " demo vector icon is not inside an app bar region");
        Color containerColor = Objects.requireNonNull(
                firstBackgroundColor(Objects.requireNonNull(appBar)),
                description + " app bar background color"
        );
        assertTrue(colorDistance(iconColor, containerColor) > 0.08,
                () -> description + " demo vector icon has too little contrast against the app bar background: icon="
                        + iconColor + ", background=" + containerColor);
    }

    /// Verifies that a rendered app bar icon has the expected shape footprint inside its 24 dp viewport.
    private static void assertAppBarIconRenderedShape(Node viewport, String expectedIconName, String description) {
        WritableImage image = snapshotNode(viewport);
        Color background = image.getPixelReader().getColor(0, 0);
        Rectangle2D pixels = contrastingPixelBounds(
                image,
                background,
                0.04,
                description + " " + expectedIconName + " icon"
        );
        double width = pixels.getWidth();
        double height = pixels.getHeight();
        double centerX = pixels.getMinX() + width / 2.0;
        double centerY = pixels.getMinY() + height / 2.0;

        assertEquals(image.getWidth() / 2.0, centerX, DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " " + expectedIconName
                        + " rendered pixels are horizontally off-center in the icon viewport: pixels=" + pixels);
        assertEquals(image.getHeight() / 2.0, centerY, DEMO_ICON_CENTER_TOLERANCE,
                () -> description + " " + expectedIconName
                        + " rendered pixels are vertically off-center in the icon viewport: pixels=" + pixels);

        switch (expectedIconName) {
            case "menu" -> {
                assertTrue(width >= 15.0,
                        () -> description + " menu icon should render wide horizontal bars: pixels=" + pixels);
                assertTrue(height >= 9.0 && height <= 15.0,
                        () -> description + " menu icon rendered height is outside the expected bar stack: pixels="
                                + pixels);
                assertTrue(width / height >= 1.2,
                        () -> description + " menu icon should be wider than tall: pixels=" + pixels);
            }
            case "more" -> {
                assertTrue(width <= 8.0,
                        () -> description + " more icon should render as a narrow vertical dot column: pixels="
                                + pixels);
                assertTrue(height >= 15.0,
                        () -> description + " more icon rendered height is too small: pixels=" + pixels);
                assertTrue(height / width >= 2.0,
                        () -> description + " more icon should be much taller than wide: pixels=" + pixels);
            }
            case "add", "search", "back", "favorite" -> {
                assertTrue(width >= 13.0,
                        () -> description + " " + expectedIconName + " icon rendered width is too small: pixels="
                                + pixels);
                assertTrue(height >= 13.0,
                        () -> description + " " + expectedIconName + " icon rendered height is too small: pixels="
                                + pixels);
                assertTrue(width / height >= 0.75 && width / height <= 1.6,
                        () -> description + " " + expectedIconName
                                + " icon rendered aspect ratio is outside the expected vector footprint: pixels="
                                + pixels);
            }
            default -> throw new IllegalArgumentException("Unsupported app bar icon shape check: " + expectedIconName);
        }
    }

    /// Verifies that a node snapshot contains non-transparent rendered pixels.
    private static void assertNodeSnapshotHasOpaquePixels(Node node, String description) {
        WritableImage image = snapshotNode(node);
        int opaquePixels = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getPixelReader().getColor(x, y).getOpacity() > 0.1) {
                    opaquePixels++;
                }
            }
        }
        int minimumOpaquePixels = Math.max(4, (int) (image.getWidth() * image.getHeight() * 0.02));
        int finalOpaquePixels = opaquePixels;
        assertTrue(finalOpaquePixels >= minimumOpaquePixels,
                () -> description + " snapshot has too few opaque pixels: opaque="
                        + finalOpaquePixels + ", minimum=" + minimumOpaquePixels
                        + ", size=" + image.getWidth() + "x" + image.getHeight());
    }

    /// Returns the opaque pixel bounds for a transparent-background node snapshot.
    private static Rectangle2D opaquePixelBounds(WritableImage image, String description) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getPixelReader().getColor(x, y).getOpacity() > 0.1) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        assertTrue(maxX >= minX && maxY >= minY,
                () -> description + " snapshot has no opaque pixels: size="
                        + image.getWidth() + "x" + image.getHeight());
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Returns the non-transparent rendered pixel bounds of a node mapped to scene coordinates.
    private static Rectangle2D renderedNodePixelBoundsInScene(Node node, String description) {
        WritableImage image = snapshotNode(node);
        Rectangle2D pixels = opaquePixelBounds(image, description);
        Bounds sceneBounds = node.localToScene(node.getBoundsInLocal());
        double scaleX = sceneBounds.getWidth() / image.getWidth();
        double scaleY = sceneBounds.getHeight() / image.getHeight();
        return new Rectangle2D(
                sceneBounds.getMinX() + pixels.getMinX() * scaleX,
                sceneBounds.getMinY() + pixels.getMinY() * scaleY,
                pixels.getWidth() * scaleX,
                pixels.getHeight() * scaleY
        );
    }

    /// Returns the bounds of pixels in an image that contrast with the supplied reference color.
    private static Rectangle2D contrastingPixelBounds(
            WritableImage image,
            Color reference,
            double minimumDistance,
            String description
    ) {
        int minX = (int) image.getWidth();
        int minY = (int) image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, reference) >= minimumDistance) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        assertTrue(maxX >= minX && maxY >= minY,
                () -> description + " snapshot has no contrasting pixels: size="
                        + image.getWidth() + "x" + image.getHeight());
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Returns the nearest top or bottom app bar region containing a node.
    private static @Nullable Region nearestAppBarRegion(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof Region region
                    && (region.getStyleClass().contains(M3TopAppBar.STYLE_CLASS)
                    || region.getStyleClass().contains(M3BottomAppBar.STYLE_CLASS))) {
                return region;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns the first concrete background color used by a region.
    private static @Nullable Color firstBackgroundColor(Region region) {
        if (region.getBackground() == null) {
            return null;
        }
        for (javafx.scene.layout.BackgroundFill fill : region.getBackground().getFills()) {
            Paint paint = fill.getFill();
            if (paint instanceof Color color) {
                return color;
            }
        }
        return null;
    }

    /// Verifies that a checkbox box and its selected or indeterminate mark share a stable center.
    private static void assertCheckboxMarkCentered(
            M3CheckBox checkBox,
            Bounds sceneBounds,
            WritableImage image,
            String pageTitle
    ) {
        @Nullable Node box = checkBox.lookup(".m3-checkbox-box");
        @Nullable Node mark = checkBox.lookup(".m3-checkbox-mark");
        if (box == null || mark == null || !hasRenderableBounds(box) || !hasRenderableBounds(mark)) {
            fail(pageTitle + " checkbox is missing box or mark geometry: " + checkBox);
        }

        Bounds boxBounds = box.localToScene(box.getBoundsInLocal());
        if (isOutsideSceneViewport(box, boxBounds, sceneBounds)) {
            return;
        }

        assertEquals(18.0, boxBounds.getWidth(), 1.0,
                () -> pageTitle + " checkbox box has unexpected width: " + boxBounds);
        assertEquals(18.0, boxBounds.getHeight(), 1.0,
                () -> pageTitle + " checkbox box has unexpected height: " + boxBounds);

        Bounds markBounds = mark.localToScene(mark.getBoundsInLocal());
        boolean markShouldBeVisible = checkBox.isSelected() || checkBox.isIndeterminate();
        if (!markShouldBeVisible) {
            assertTrue(mark.getOpacity() <= 0.05,
                    () -> pageTitle + " unchecked checkbox mark should be hidden: opacity="
                            + mark.getOpacity() + ", markBounds=" + markBounds);
            return;
        }

        double dx = Math.abs(boxBounds.getCenterX() - markBounds.getCenterX());
        double dy = Math.abs(boxBounds.getCenterY() - markBounds.getCenterY());
        assertTrue(mark.getOpacity() > 0.20 && dx <= 1.0 && dy <= 1.0,
                () -> pageTitle + " checkbox mark is not centered in its box: dx=" + dx
                        + ", dy=" + dy + ", opacity=" + mark.getOpacity()
                        + ", boxBounds=" + boxBounds + ", markBounds=" + markBounds);
        if (checkBox.isIndeterminate()) {
            assertTrue(markBounds.getWidth() >= 9.0 && markBounds.getHeight() <= 4.0,
                    () -> pageTitle + " indeterminate checkbox mark should be a centered dash: "
                            + markBounds);
        } else {
            assertTrue(markBounds.getWidth() >= 9.0 && markBounds.getHeight() >= 7.0,
                    () -> pageTitle + " selected checkbox mark should be a visible check: "
                            + markBounds);
        }
        assertCheckboxMarkRenderedPixels(checkBox, mark, boxBounds, image, pageTitle);
    }

    /// Verifies that checkbox mark pixels match the selected or indeterminate state, not only layout bounds.
    private static void assertCheckboxMarkRenderedPixels(
            M3CheckBox checkBox,
            Node mark,
            Bounds boxBounds,
            WritableImage image,
            String pageTitle
    ) {
        Rectangle2D pixels = contrastingPixelBounds(image, mark, sampledNodeBackgroundColor(image, mark), 0.02);
        double centerX = pixels.getMinX() + pixels.getWidth() / 2.0;
        double centerY = pixels.getMinY() + pixels.getHeight() / 2.0;
        double dx = Math.abs(boxBounds.getCenterX() - centerX);
        double dy = Math.abs(boxBounds.getCenterY() - centerY);
        assertTrue(dx <= SELECTION_PIXEL_CENTER_TOLERANCE && dy <= SELECTION_PIXEL_CENTER_TOLERANCE,
                () -> pageTitle + " rendered checkbox mark pixels are off-center: dx=" + dx
                        + ", dy=" + dy + ", box=" + boxBounds + ", markPixels=" + pixels);

        assertRectangleInsideBounds(boxBounds, pixels, CONTROL_EDGE_TOLERANCE,
                pageTitle + " rendered checkbox mark pixels");
        if (checkBox.isIndeterminate()) {
            assertTrue(pixels.getWidth() >= 9.0 && pixels.getHeight() <= 4.0,
                    () -> pageTitle + " rendered indeterminate checkbox mark should be a dash: "
                            + pixels);
            assertTrue(pixels.getWidth() >= pixels.getHeight() * 3.0,
                    () -> pageTitle + " rendered indeterminate checkbox mark is not wide enough for a dash: "
                            + pixels);
        } else {
            assertTrue(pixels.getWidth() >= 8.0 && pixels.getHeight() >= 6.0,
                    () -> pageTitle + " rendered selected checkbox mark should be a visible check: "
                            + pixels);
            assertTrue(pixels.getWidth() <= pixels.getHeight() * 2.4,
                    () -> pageTitle + " rendered selected checkbox mark is too flat and looks like a dash: "
                            + pixels);
        }
    }

    /// Verifies that a radio button dot shares the same rendered center as its ring.
    private static void assertRadioDotCentered(
            Node root,
            Bounds sceneBounds,
            WritableImage image,
            String pageTitle
    ) {
        @Nullable Node container = root.lookup(".m3-radio-ring");
        @Nullable Node indicator = root.lookup(".m3-radio-dot");
        if (container == null || indicator == null || !hasRenderableBounds(container) || !hasRenderableBounds(indicator)) {
            return;
        }
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        if (isOutsideSceneViewport(container, containerBounds, sceneBounds)) {
            return;
        }

        double dx = Math.abs(containerBounds.getCenterX() - indicatorBounds.getCenterX());
        double dy = Math.abs(containerBounds.getCenterY() - indicatorBounds.getCenterY());
        assertTrue(dx <= 0.75 && dy <= 0.75,
                () -> pageTitle + " radio dot is off-center: dx=" + dx + ", dy=" + dy
                        + ", containerBounds=" + containerBounds + ", indicatorBounds=" + indicatorBounds);
        if (indicator.getOpacity() <= 0.2) {
            return;
        }

        Color dotReference = sampledNodeBackgroundColor(image, indicator);
        Point2D renderedCenter = contrastingPixelCentroid(image, indicator, dotReference, 0.03);
        Rectangle2D renderedBounds = contrastingPixelBounds(image, indicator, dotReference, 0.03);
        Color ringReference = sampledNodeBackgroundColor(image, container);
        Point2D renderedRingCenter = contrastingPixelCentroid(image, container, ringReference, 0.03);
        Rectangle2D renderedRingBounds = contrastingPixelBounds(image, container, ringReference, 0.03);
        double renderedDx = Math.abs(containerBounds.getCenterX() - renderedCenter.getX());
        double renderedDy = Math.abs(containerBounds.getCenterY() - renderedCenter.getY());
        assertTrue(renderedDx <= SELECTION_PIXEL_CENTER_TOLERANCE
                        && renderedDy <= SELECTION_PIXEL_CENTER_TOLERANCE,
                () -> pageTitle + " rendered radio dot pixels are off-center: dx=" + renderedDx
                        + ", dy=" + renderedDy + ", ringBounds=" + containerBounds
                        + ", dotPixels=" + renderedBounds);
        double dotToRingDx = Math.abs(renderedRingCenter.getX() - renderedCenter.getX());
        double dotToRingDy = Math.abs(renderedRingCenter.getY() - renderedCenter.getY());
        assertTrue(dotToRingDx <= SELECTION_PIXEL_CENTER_TOLERANCE
                        && dotToRingDy <= SELECTION_PIXEL_CENTER_TOLERANCE,
                () -> pageTitle + " rendered radio dot pixels do not share the ring pixel center: dx="
                        + dotToRingDx + ", dy=" + dotToRingDy
                        + ", ringPixels=" + renderedRingBounds + ", dotPixels=" + renderedBounds);
        assertTrue(Math.abs(renderedRingBounds.getWidth() - renderedRingBounds.getHeight())
                        <= SELECTION_PIXEL_SHAPE_TOLERANCE,
                () -> pageTitle + " rendered radio ring pixels are not square: " + renderedRingBounds);
        assertTrue(Math.abs(renderedBounds.getWidth() - renderedBounds.getHeight()) <= SELECTION_PIXEL_SHAPE_TOLERANCE,
                () -> pageTitle + " rendered radio dot pixels are not square: " + renderedBounds);
    }

    /// Verifies that a switch thumb stays vertically centered and inside its track.
    private static void assertSwitchThumbInsideTrack(
            Node root,
            Bounds sceneBounds,
            WritableImage image,
            String pageTitle
    ) {
        @Nullable Node track = root.lookup(".m3-switch-track");
        @Nullable Node thumb = root.lookup(".m3-switch-thumb");
        if (track == null || thumb == null || !hasRenderableBounds(track) || !hasRenderableBounds(thumb)) {
            return;
        }
        Bounds trackBounds = track.localToScene(track.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        if (isOutsideSceneViewport(track, trackBounds, sceneBounds)) {
            return;
        }

        double dy = Math.abs(trackBounds.getCenterY() - thumbBounds.getCenterY());
        assertTrue(dy <= 1.0
                        && thumbBounds.getMinX() >= trackBounds.getMinX() - 0.75
                        && thumbBounds.getMaxX() <= trackBounds.getMaxX() + 0.75,
                () -> pageTitle + " switch thumb has unsafe geometry: dy=" + dy
                        + ", trackBounds=" + trackBounds + ", thumbBounds=" + thumbBounds);

        @Nullable Node stateLayer = root.lookup(".m3-state-layer-container");
        if (stateLayer != null && hasRenderableBounds(stateLayer)) {
            Bounds stateLayerBounds = stateLayer.localToScene(stateLayer.getBoundsInLocal());
            assertEquals(stateLayerBounds.getWidth(), stateLayerBounds.getHeight(), 0.75,
                    () -> pageTitle + " switch state layer should stay square: " + stateLayerBounds);
            assertEquals(thumbBounds.getCenterX(), stateLayerBounds.getCenterX(), 1.0,
                    () -> pageTitle + " switch state layer should track thumb center horizontally: stateLayer="
                            + stateLayerBounds + ", thumb=" + thumbBounds);
            assertEquals(thumbBounds.getCenterY(), stateLayerBounds.getCenterY(), 1.0,
                    () -> pageTitle + " switch state layer should track thumb center vertically: stateLayer="
                            + stateLayerBounds + ", thumb=" + thumbBounds);
        }

        if (root.isDisabled()) {
            return;
        }
        Color thumbReference = sampledNodeBackgroundColor(image, thumb);
        Rectangle2D thumbPixels = contrastingPixelBounds(image, thumb, thumbReference, 0.02);
        double thumbPixelCenterY = thumbPixels.getMinY() + thumbPixels.getHeight() / 2.0;
        assertTrue(Math.abs(thumbBounds.getCenterY() - thumbPixelCenterY) <= SELECTION_PIXEL_CENTER_TOLERANCE,
                () -> pageTitle + " rendered switch thumb pixels are vertically off-center: thumb="
                        + thumbBounds + ", thumbPixels=" + thumbPixels);
        assertTrue(Math.abs(thumbPixels.getWidth() - thumbPixels.getHeight()) <= SELECTION_PIXEL_SHAPE_TOLERANCE,
                () -> pageTitle + " rendered switch thumb pixels are not square: " + thumbPixels);
    }

    /// Verifies that a slider track, active track, and thumb match the current value and orientation.
    private static void assertSliderTrackThumbGeometry(
            M3Slider slider,
            Bounds sceneBounds,
            WritableImage image,
            String pageTitle
    ) {
        @Nullable Node track = slider.lookup(".track");
        @Nullable Node activeTrack = slider.lookup(".active-track");
        @Nullable Node thumb = slider.lookup(".thumb");
        if (track == null
                || activeTrack == null
                || thumb == null
                || !hasRenderableBounds(track)
                || !hasRenderableBounds(activeTrack)
                || !hasRenderableBounds(thumb)) {
            fail(pageTitle + " slider is missing track, active track, or thumb geometry: " + slider);
        }

        Bounds sliderBounds = slider.localToScene(slider.getBoundsInLocal());
        Bounds trackBounds = track.localToScene(track.getBoundsInLocal());
        Bounds activeBounds = activeTrack.localToScene(activeTrack.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        if (isOutsideSceneViewport(track, trackBounds, sceneBounds)) {
            return;
        }

        assertTrue(containsBoundsWithTolerance(sliderBounds, trackBounds, CONTROL_EDGE_TOLERANCE),
                () -> pageTitle + " slider track leaves the control bounds: slider="
                        + sliderBounds + ", track=" + trackBounds);
        assertTrue(containsBoundsWithTolerance(sliderBounds, activeBounds, CONTROL_EDGE_TOLERANCE),
                () -> pageTitle + " slider active track leaves the control bounds: slider="
                        + sliderBounds + ", activeTrack=" + activeBounds);
        assertTrue(containsBoundsWithTolerance(sliderBounds, thumbBounds, CONTROL_EDGE_TOLERANCE),
                () -> pageTitle + " slider thumb leaves the control bounds: slider="
                        + sliderBounds + ", thumb=" + thumbBounds);

        double logicalPosition = normalizedSliderPosition(slider);
        if (slider.getOrientation() == Orientation.VERTICAL) {
            double expectedThumbCenterY = trackBounds.getMaxY() - trackBounds.getHeight() * logicalPosition;
            assertTrue(trackBounds.getHeight() >= 120.0,
                    () -> pageTitle + " vertical slider track is too short: " + trackBounds);
            assertEquals(slider.getTrackThickness(), trackBounds.getWidth(), 1.0,
                    () -> pageTitle + " vertical slider track width does not match token: " + trackBounds);
            assertEquals(slider.getThumbSize(), thumbBounds.getWidth(), 1.0,
                    () -> pageTitle + " vertical slider thumb width does not match token: " + thumbBounds);
            assertEquals(trackBounds.getCenterX(), thumbBounds.getCenterX(), 1.0,
                    () -> pageTitle + " vertical slider thumb is horizontally off track: track="
                            + trackBounds + ", thumb=" + thumbBounds);
            assertEquals(expectedThumbCenterY, thumbBounds.getCenterY(), 1.25,
                    () -> pageTitle + " vertical slider thumb does not match value: value="
                            + slider.getValue() + ", track=" + trackBounds + ", thumb=" + thumbBounds);
            assertEquals(trackBounds.getHeight() * logicalPosition, activeBounds.getHeight(), 1.25,
                    () -> pageTitle + " vertical slider active track does not match value: value="
                            + slider.getValue() + ", activeTrack=" + activeBounds + ", track=" + trackBounds);
            assertEquals(trackBounds.getMaxY(), activeBounds.getMaxY(), 1.25,
                    () -> pageTitle + " vertical slider active track should end at the track bottom: activeTrack="
                            + activeBounds + ", track=" + trackBounds);
            assertSliderThumbPixelsCentered(image, thumb, thumbBounds, pageTitle);
            return;
        }

        double visualPosition = slider.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT
                ? 1.0 - logicalPosition
                : logicalPosition;
        double expectedThumbCenterX = trackBounds.getMinX() + trackBounds.getWidth() * visualPosition;
        assertTrue(trackBounds.getWidth() >= 160.0,
                () -> pageTitle + " horizontal slider track is too short: " + trackBounds);
        assertEquals(slider.getTrackThickness(), trackBounds.getHeight(), 1.0,
                () -> pageTitle + " horizontal slider track height does not match token: " + trackBounds);
        assertEquals(slider.getThumbSize(), thumbBounds.getHeight(), 1.0,
                () -> pageTitle + " horizontal slider thumb height does not match token: " + thumbBounds);
        assertEquals(trackBounds.getCenterY(), thumbBounds.getCenterY(), 1.0,
                () -> pageTitle + " horizontal slider thumb is vertically off track: track="
                        + trackBounds + ", thumb=" + thumbBounds);
        assertEquals(expectedThumbCenterX, thumbBounds.getCenterX(), 1.25,
                () -> pageTitle + " horizontal slider thumb does not match value: value="
                        + slider.getValue() + ", track=" + trackBounds + ", thumb=" + thumbBounds);
        assertEquals(trackBounds.getWidth() * logicalPosition, activeBounds.getWidth(), 1.25,
                () -> pageTitle + " horizontal slider active track does not match value: value="
                        + slider.getValue() + ", activeTrack=" + activeBounds + ", track=" + trackBounds);
        if (slider.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            assertEquals(trackBounds.getMaxX(), activeBounds.getMaxX(), 1.25,
                    () -> pageTitle + " RTL horizontal slider active track should end at the track trailing edge: "
                            + activeBounds);
        } else {
            assertEquals(trackBounds.getMinX(), activeBounds.getMinX(), 1.25,
                    () -> pageTitle + " horizontal slider active track should start at the track leading edge: "
                            + activeBounds);
        }
        assertSliderThumbPixelsCentered(image, thumb, thumbBounds, pageTitle);
    }

    /// Verifies that the slider thumb's rendered pixels are centered inside its layout bounds.
    private static void assertSliderThumbPixelsCentered(
            WritableImage image,
            Node thumb,
            Bounds thumbBounds,
            String pageTitle
    ) {
        if (thumb.isDisabled()) {
            return;
        }
        Color thumbReference = sampledNodeBackgroundColor(image, thumb);
        Rectangle2D thumbPixels = contrastingPixelBounds(image, thumb, thumbReference, 0.02);
        double renderedCenterX = thumbPixels.getMinX() + thumbPixels.getWidth() / 2.0;
        double renderedCenterY = thumbPixels.getMinY() + thumbPixels.getHeight() / 2.0;
        assertTrue(Math.abs(thumbBounds.getCenterX() - renderedCenterX) <= SELECTION_PIXEL_CENTER_TOLERANCE
                        && Math.abs(thumbBounds.getCenterY() - renderedCenterY) <= SELECTION_PIXEL_CENTER_TOLERANCE,
                () -> pageTitle + " rendered slider thumb pixels are off-center: thumb="
                        + thumbBounds + ", thumbPixels=" + thumbPixels);
        assertTrue(Math.abs(thumbPixels.getWidth() - thumbPixels.getHeight()) <= SELECTION_PIXEL_SHAPE_TOLERANCE,
                () -> pageTitle + " rendered slider thumb pixels are not square: " + thumbPixels);
    }

    /// Returns the current slider value normalized to the `[0, 1]` range.
    private static double normalizedSliderPosition(M3Slider slider) {
        double min = slider.getMin();
        double max = slider.getMax();
        if (max <= min) {
            return 0.0;
        }
        double position = (slider.getValue() - min) / (max - min);
        if (position < 0.0) {
            return 0.0;
        }
        if (position > 1.0) {
            return 1.0;
        }
        return position;
    }

    /// Verifies that a carousel demo instance has a viewport, track, and visible selected item.
    private static void assertCarouselDemoGeometry(M3Carousel carousel, String description) {
        ScrollPane viewport = assertInstanceOf(
                ScrollPane.class,
                requireVisibleStyledDescendant(
                        carousel,
                        M3Carousel.VIEWPORT_STYLE_CLASS,
                        description + " viewport"
                )
        );
        Node track = requireVisibleStyledDescendant(carousel, M3Carousel.TRACK_STYLE_CLASS, description + " track");
        Node selectedItem = Objects.requireNonNull(carousel.getSelectedItem(), description + " selected item");
        @Nullable Node viewportNode = viewport.lookup(".viewport");
        assertNotNull(viewportNode, () -> description + " missing ScrollPane viewport node");
        assertTrue(hasRenderableBounds(viewportNode), () -> description + " viewport has no renderable bounds");
        assertTrue(hasRenderableBounds(track), () -> description + " track has no renderable bounds");
        assertTrue(hasRenderableBounds(selectedItem), () -> description + " selected item has no renderable bounds");
        assertTrue(selectedItem.getStyleClass().contains(M3Carousel.SELECTED_ITEM_STYLE_CLASS),
                () -> description + " selected item missing selected style class");
        assertEquals(1, carousel.getItems()
                        .stream()
                        .filter(item -> item.getStyleClass().contains(M3Carousel.SELECTED_ITEM_STYLE_CLASS))
                        .count(),
                () -> description + " should expose exactly one selected carousel item style class");

        Bounds carouselBounds = carousel.localToScene(carousel.getLayoutBounds());
        Bounds viewportBounds = viewportNode.localToScene(viewportNode.getLayoutBounds());
        Bounds trackBounds = track.localToScene(track.getLayoutBounds());
        Bounds selectedLayoutBounds = selectedItem.localToScene(selectedItem.getLayoutBounds());
        assertTrue(containsBoundsWithTolerance(carouselBounds, viewportBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " viewport leaves carousel layout bounds: carousel="
                        + carouselBounds + ", viewport=" + viewportBounds);
        assertTrue(trackBounds.getWidth() >= viewportBounds.getWidth(),
                () -> description + " track should be at least as wide as the viewport: track="
                        + trackBounds + ", viewport=" + viewportBounds);
        assertTrue(viewportBounds.intersects(selectedLayoutBounds)
                        && viewportBounds.contains(
                                selectedLayoutBounds.getCenterX(),
                                selectedLayoutBounds.getCenterY()
                        ),
                () -> description + " selected item layout should be visible inside viewport: selected="
                        + selectedLayoutBounds + ", viewport=" + viewportBounds);
        assertTrue(containsBoundsWithTolerance(viewportBounds, selectedLayoutBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " selected item layout should stay within viewport; visual effects may extend beyond it: selected="
                        + selectedLayoutBounds + ", viewport=" + viewportBounds);

        Node selectedText = Objects.requireNonNull(firstVisibleText(selectedItem), description + " selected text");
        Bounds selectedTextBounds = selectedText.localToScene(selectedText.getLayoutBounds());
        assertTrue(containsBoundsWithTolerance(selectedLayoutBounds, selectedTextBounds, TEXT_EDGE_TOLERANCE),
                () -> description + " selected item text leaves item layout: text="
                        + selectedTextBounds + ", item=" + selectedLayoutBounds);
        assertTrue(containsBoundsWithTolerance(viewportBounds, selectedTextBounds, TEXT_EDGE_TOLERANCE),
                () -> description + " selected item text leaves viewport: text="
                        + selectedTextBounds + ", viewport=" + viewportBounds);
    }

    /// Verifies that a demo card uses a visible Material surface, variant treatment, and stable content bounds.
    private static void assertCardDemoGeometry(M3Card card) {
        Region container = assertInstanceOf(
                Region.class,
                requireVisibleStyledDescendant(card, "m3-card-container", "card container")
        );
        Bounds cardBounds = card.localToScene(card.getLayoutBounds());
        Bounds containerBounds = container.localToScene(container.getLayoutBounds());

        assertTrue(cardBounds.getWidth() >= 260.0,
                () -> "demo card is too narrow for meaningful content: " + cardBounds);
        assertTrue(cardBounds.getHeight() >= 150.0,
                () -> "demo card is too short for meaningful content: " + cardBounds);
        assertTrue(containsBoundsWithTolerance(cardBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> "card surface should stay inside the control bounds: card="
                        + cardBounds + ", container=" + containerBounds);
        assertNotNull(container.getBackground(), "card container should resolve a background");
        assertFalse(container.getBackground().getFills().isEmpty(),
                "card container should resolve a visible background fill");
        assertEquals(card.getContentPadding(), container.getPadding().getLeft(), 0.0001,
                "card container should apply the public content padding token");

        switch (card.getVariant()) {
            case ELEVATED -> assertNotNull(container.getEffect(), "elevated cards should render elevation");
            case OUTLINED -> {
                assertNotNull(container.getBorder(), "outlined cards should render an outline border");
                assertFalse(container.getBorder().getStrokes().isEmpty(),
                        "outlined cards should render an outline border");
            }
            case FILLED -> assertNull(container.getEffect(), "filled cards should not render elevation by default");
        }
    }

    /// Verifies that a demo dialog pane resolves Material surface, action, and content geometry.
    private static void assertDialogPaneDemoGeometry(Scene scene, M3DialogPane pane) {
        assertEquals(AccessibleRole.DIALOG, pane.getAccessibleRole());
        assertDialogPaneStaysCompact(scene, pane);
        assertNotNull(pane.getBackground(), "dialog pane should resolve a Material background");
        assertFalse(pane.getBackground().getFills().isEmpty(),
                "dialog pane should resolve a visible Material background fill");
        assertFalse(pane.getButtonTypes().isEmpty(), "demo dialog panes should expose action buttons");

        Bounds paneBounds = pane.localToScene(pane.getBoundsInLocal());
        Node buttonBar = requireVisibleStyledDescendant(
                pane,
                M3DialogPane.BUTTON_BAR_STYLE_CLASS,
                "dialog button bar"
        );
        Bounds buttonBarBounds = buttonBar.localToScene(buttonBar.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(paneBounds, buttonBarBounds, CONTROL_EDGE_TOLERANCE),
                () -> "dialog button bar should stay inside the pane: pane="
                        + paneBounds + ", buttonBar=" + buttonBarBounds);

        for (ButtonType buttonType : pane.getButtonTypes()) {
            Node action = Objects.requireNonNull(pane.lookupButton(buttonType), "dialog action button");
            M3Button materialAction = assertInstanceOf(M3Button.class, action);
            assertTrue(materialAction.getStyleClass().contains(M3DialogPane.BUTTON_STYLE_CLASS),
                    "dialog action should use the Material dialog button style class");
            Bounds actionBounds = materialAction.localToScene(materialAction.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(paneBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "dialog action button should stay inside the pane: pane="
                            + paneBounds + ", action=" + actionBounds);
        }

        @Nullable Node content = pane.getContent();
        if (content != null && content.isVisible() && hasRenderableBounds(content)) {
            Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(paneBounds, contentBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "dialog content should stay inside the pane: pane="
                            + paneBounds + ", content=" + contentBounds);
        }
    }

    /// Verifies that a demo banner uses visible Material surface and safe slot geometry.
    private static void assertBannerDemoGeometry(M3Banner banner) {
        Bounds bannerBounds = banner.localToScene(banner.getBoundsInLocal());
        assertTrue(bannerBounds.getWidth() >= 360.0,
                () -> "demo banner is too narrow for the showcased content: " + bannerBounds);
        assertTrue(bannerBounds.getHeight() >= 72.0,
                () -> "demo banner is too short for Material banner content: " + bannerBounds);
        assertNotNull(banner.getBackground(), "banner should resolve a Material background");
        assertFalse(banner.getBackground().getFills().isEmpty(), "banner should resolve a visible background fill");

        Node container = requireVisibleStyledDescendant(
                banner,
                M3Banner.CONTAINER_STYLE_CLASS,
                "banner container"
        );
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(bannerBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> "banner container should stay inside the control bounds: banner="
                        + bannerBounds + ", container=" + containerBounds);

        Node text = requireVisibleStyledDescendant(banner, M3Banner.TEXT_STYLE_CLASS, "banner text");
        Bounds textBounds = text.localToScene(text.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(bannerBounds, textBounds, CONTROL_EDGE_TOLERANCE),
                () -> "banner text should stay inside the control bounds: banner="
                        + bannerBounds + ", text=" + textBounds);

        @Nullable Node actions = firstVisibleStyledDescendant(banner, M3Banner.ACTIONS_STYLE_CLASS);
        if (!banner.getActions().isEmpty()) {
            assertNotNull(actions, "banner actions should be visible when action nodes are present");
            Bounds actionBounds = actions.localToScene(actions.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(bannerBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "banner actions should stay inside the control bounds: banner="
                            + bannerBounds + ", actions=" + actionBounds);
            for (Node action : banner.getActions()) {
                Bounds singleActionBounds = action.localToScene(action.getBoundsInLocal());
                assertTrue(containsBoundsWithTolerance(bannerBounds, singleActionBounds, CONTROL_EDGE_TOLERANCE),
                        () -> "banner action should stay inside the control bounds: banner="
                                + bannerBounds + ", action=" + singleActionBounds);
            }
        }

        @Nullable Node icon = firstVisibleStyledDescendant(banner, M3Banner.ICON_STYLE_CLASS);
        if (banner.getIcon() != null) {
            assertNotNull(icon, "banner icon slot should be visible when an icon is present");
            Bounds iconBounds = icon.localToScene(icon.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(bannerBounds, iconBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "banner icon should stay inside the control bounds: banner="
                            + bannerBounds + ", icon=" + iconBounds);
            if (banner.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT && actions != null) {
                Bounds actionBounds = actions.localToScene(actions.getBoundsInLocal());
                assertTrue(iconBounds.getMinX() > actionBounds.getMaxX(),
                        () -> "RTL banner should mirror icon and action slots: icon="
                                + iconBounds + ", actions=" + actionBounds);
            }
        }
    }

    /// Verifies that the expected text appears in the visible node tree.
    private static void assertVisibleText(Node root, String expectedText, String pageTitle) {
        assertTrue(visibleNodesOfType(root, Text.class).stream().anyMatch(text -> expectedText.equals(text.getText())),
                () -> pageTitle + " did not render visible text: " + expectedText);
    }

    /// Verifies that visible rendered text contains the expected content after JavaFX wrapping and skin splitting.
    private static void assertVisibleTextContains(Node root, String expectedText, String description) {
        String actualText = normalizedVisibleText(root);
        String normalizedExpectedText = normalizeVisibleText(expectedText);
        assertTrue(actualText.contains(normalizedExpectedText),
                () -> description + " did not render visible text containing: "
                        + normalizedExpectedText + ", actual=" + actualText);
    }

    /// Returns normalized text from visible text-bearing nodes.
    private static String normalizedVisibleText(Node node) {
        StringBuilder builder = new StringBuilder();
        appendVisibleText(node, builder);
        return normalizeVisibleText(builder.toString());
    }

    /// Appends text from visible text-bearing descendants.
    private static void appendVisibleText(Node node, StringBuilder builder) {
        if (!node.isVisible() || !hasRenderableBounds(node)) {
            return;
        }
        if (node instanceof Text text && !text.getText().isBlank()) {
            builder.append(text.getText()).append('\n');
        } else if (node instanceof Labeled labeled && labeled.getText() != null && !labeled.getText().isBlank()) {
            builder.append(labeled.getText()).append('\n');
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                appendVisibleText(child, builder);
            }
        }
    }

    /// Normalizes rendered text for containment checks across wrapping and skin text-node boundaries.
    private static String normalizeVisibleText(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    /// Returns whether a node should have centered glyph content.
    private static boolean isCenteredTarget(Node node) {
        for (String styleClass : CENTERED_TARGET_STYLE_CLASSES) {
            if (node.getStyleClass().contains(styleClass)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a node is a demo-level Material control whose visible bounds should fit the viewport.
    private static boolean isPageLevelMaterialControl(Node node) {
        return node instanceof M3Avatar
                || node instanceof M3Badge
                || node instanceof M3BadgedBox
                || node instanceof M3Banner
                || node instanceof M3BottomAppBar
                || node instanceof M3BottomSheet
                || node instanceof M3Button
                || node instanceof M3Card
                || node instanceof M3CheckBox
                || node instanceof M3Chip
                || node instanceof M3DatePicker
                || node instanceof M3Divider
                || node instanceof M3FloatingActionButton
                || node instanceof M3FormPane
                || node instanceof M3FormRow
                || node instanceof M3FormSection
                || node instanceof M3IconToggleButton
                || node instanceof M3ListItem
                || node instanceof M3LoadingIndicator
                || node instanceof M3Menu
                || node instanceof M3NavigationDrawerGroup
                || node instanceof M3NavigationItem
                || node instanceof M3PickerField<?, ?>
                || node instanceof M3RadioButton
                || node instanceof M3Scrim
                || node instanceof M3SearchBar
                || node instanceof M3SearchView
                || node instanceof M3SegmentedButton
                || node instanceof M3SideSheet
                || node instanceof M3Snackbar
                || node instanceof M3SplitButton
                || node instanceof M3Slider
                || node instanceof M3Surface
                || node instanceof M3Switch
                || node instanceof M3Tab
                || node instanceof M3Text
                || node instanceof M3TextField
                || node instanceof M3TextInputLayout
                || node instanceof M3TimePicker
                || node instanceof M3Toolbar
                || node instanceof M3TopAppBar;
    }

    /// Returns the first visible descendant with the requested style class.
    private static @Nullable Node firstVisibleNodeWithStyle(Node root, String styleClass) {
        if (root.isVisible() && root.getStyleClass().contains(styleClass) && hasRenderableBounds(root)) {
            return root;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node result = firstVisibleNodeWithStyle(child, styleClass);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns visible descendants assignable to the requested type.
    private static <T> List<T> visibleNodesOfType(Node root, Class<T> type) {
        List<T> result = new ArrayList<>();
        collectVisibleNodesOfType(root, type, result);
        return result;
    }

    /// Returns visible descendants with the requested style class sorted by their scene x coordinate.
    private static List<Node> visibleNodesWithStyle(Node root, String styleClass) {
        return root.lookupAll("." + styleClass).stream()
                .filter(node -> node.isVisible() && hasRenderableBounds(node))
                .sorted(java.util.Comparator.comparingDouble(node ->
                        node.localToScene(node.getBoundsInLocal()).getMinX()))
                .toList();
    }

    /// Adds visible descendants assignable to the requested type into a result list.
    private static <T> void collectVisibleNodesOfType(Node root, Class<T> type, List<T> result) {
        if (!root.isVisible()) {
            return;
        }
        if (type.isInstance(root) && hasRenderableBounds(root)) {
            result.add(type.cast(root));
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleNodesOfType(child, type, result);
            }
        }
    }

    /// Returns the nearest ancestor with the requested style class.
    private static @Nullable Parent nearestAncestorWithStyle(Node node, String styleClass) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains(styleClass)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns the nearest ancestor assignable to the requested type.
    private static <T> @Nullable T nearestAncestorOfType(Node node, Class<T> type) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (type.isInstance(parent)) {
                return type.cast(parent);
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Returns whether `candidate` is `root` or belongs to its descendant subtree.
    private static boolean isNodeOrDescendant(Node root, Node candidate) {
        Node node = candidate;
        while (true) {
            if (node == root) {
                return true;
            }
            @Nullable Parent parent = node.getParent();
            if (parent == null) {
                return false;
            }
            node = parent;
        }
    }

    /// Returns a visible descendant with the requested style class or fails the test.
    private static Node requireVisibleStyledDescendant(Node root, String styleClass, String description) {
        @Nullable Node node = firstVisibleStyledDescendant(root, styleClass);
        assertNotNull(node, description);
        return Objects.requireNonNull(node, description);
    }

    /// Returns the first visible descendant with the requested style class.
    private static @Nullable Node firstVisibleStyledDescendant(Node root, String styleClass) {
        if (root.isVisible() && root.getStyleClass().contains(styleClass) && hasRenderableBounds(root)) {
            return root;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node result = firstVisibleStyledDescendant(child, styleClass);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns whether a node bounds should be skipped because it is outside the visible scene or scroll viewport.
    private static boolean isOutsideSceneViewport(Node node, Bounds nodeBounds, Bounds sceneBounds) {
        if (!sceneBounds.intersects(nodeBounds)
                || !sceneBounds.contains(nodeBounds.getCenterX(), nodeBounds.getCenterY())) {
            return true;
        }
        @Nullable Node scrollViewport = nearestScrollViewport(node);
        if (scrollViewport == null) {
            return false;
        }
        Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
        return !viewportBounds.intersects(nodeBounds)
                || !viewportBounds.contains(nodeBounds.getCenterX(), nodeBounds.getCenterY());
    }

    /// Returns the first visible M3 button with the requested text.
    private static @Nullable M3Button firstVisibleButtonWithText(Node root, String text) {
        if (root instanceof M3Button button
                && button.isVisible()
                && text.equals(button.getText())
                && hasRenderableBounds(button)) {
            return button;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3Button result = firstVisibleButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 switch with the requested text.
    private static @Nullable M3Switch firstVisibleSwitchWithText(Node root, String text) {
        if (root instanceof M3Switch switchControl
                && switchControl.isVisible()
                && text.equals(switchControl.getText())
                && hasRenderableBounds(switchControl)) {
            return switchControl;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3Switch result = firstVisibleSwitchWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 split button with the requested text.
    private static @Nullable M3SplitButton firstVisibleSplitButtonWithText(Node root, String text) {
        if (root instanceof M3SplitButton splitButton
                && splitButton.isVisible()
                && text.equals(splitButton.getText())
                && hasRenderableBounds(splitButton)) {
            return splitButton;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SplitButton result = firstVisibleSplitButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 menu button with the requested text.
    private static @Nullable M3MenuButton firstVisibleMenuButtonWithText(Node root, String text) {
        if (root instanceof M3MenuButton menuButton
                && menuButton.isVisible()
                && text.equals(menuButton.getText())
                && hasRenderableBounds(menuButton)) {
            return menuButton;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3MenuButton result = firstVisibleMenuButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 date picker field.
    private static @Nullable M3DatePickerField firstVisibleDatePickerField(Node root) {
        if (root instanceof M3DatePickerField field && field.isVisible() && hasRenderableBounds(field)) {
            return field;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3DatePickerField result = firstVisibleDatePickerField(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 submenu item with the requested text.
    private static @Nullable M3SubMenuItem firstVisibleSubMenuItemWithText(Node root, String text) {
        if (root instanceof M3SubMenuItem subMenuItem
                && subMenuItem.isVisible()
                && text.equals(subMenuItem.getHeadlineText())
                && hasRenderableBounds(subMenuItem)) {
            return subMenuItem;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SubMenuItem result = firstVisibleSubMenuItemWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 navigation item with the requested text.
    private static @Nullable M3NavigationItem firstVisibleNavigationItemWithText(Node root, String text) {
        if (root instanceof M3NavigationItem item
                && item.isVisible()
                && text.equals(item.getText())
                && hasRenderableBounds(item)) {
            return item;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3NavigationItem result = firstVisibleNavigationItemWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible navigation drawer group with the requested title.
    private static @Nullable M3NavigationDrawerGroup firstVisibleDrawerGroupWithTitle(Node root, String title) {
        if (root instanceof M3NavigationDrawerGroup group
                && group.isVisible()
                && title.equals(group.getTitle())
                && hasRenderableBounds(group)) {
            return group;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3NavigationDrawerGroup result = firstVisibleDrawerGroupWithTitle(child, title);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible bottom sheet with the requested headline.
    private static @Nullable M3BottomSheet firstVisibleBottomSheetWithHeadline(Node root, String headline) {
        if (root instanceof M3BottomSheet sheet
                && sheet.isVisible()
                && headline.equals(sheet.getHeadline())
                && hasRenderableBounds(sheet)) {
            return sheet;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3BottomSheet result = firstVisibleBottomSheetWithHeadline(child, headline);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible side sheet with the requested headline.
    private static @Nullable M3SideSheet firstVisibleSideSheetWithHeadline(Node root, String headline) {
        if (root instanceof M3SideSheet sheet
                && sheet.isVisible()
                && headline.equals(sheet.getHeadline())
                && hasRenderableBounds(sheet)) {
            return sheet;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SideSheet result = firstVisibleSideSheetWithHeadline(child, headline);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible snackbar host.
    private static @Nullable M3SnackbarHost firstVisibleSnackbarHost(Node root) {
        if (root instanceof M3SnackbarHost host && host.isVisible() && hasRenderableBounds(host)) {
            return host;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SnackbarHost result = firstVisibleSnackbarHost(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible FAB menu with the requested expanded state.
    private static @Nullable M3FabMenu firstVisibleFabMenu(Node root, boolean expanded) {
        if (root instanceof M3FabMenu menu
                && menu.isVisible()
                && menu.isExpanded() == expanded
                && hasRenderableBounds(menu)) {
            return menu;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3FabMenu result = firstVisibleFabMenu(child, expanded);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the popup root that hosts a picker field's popup picker.
    private static @Nullable Node pickerPopupRoot(M3DatePickerField field) {
        @Nullable Parent parent = field.getPicker().getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains(M3PickerField.POPUP_STYLE_CLASS)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Applies CSS and layout to a popup root before capturing it.
    private static void layoutPopupRoot(Node popupRoot) {
        popupRoot.applyCss();
        if (popupRoot instanceof Parent parent) {
            parent.layout();
        }
    }

    /// Returns whether a popup root has reached its fully shown visual state.
    private static boolean popupRootSettled(@Nullable Node popupRoot) {
        if (popupRoot == null
                || popupRoot.getScene() == null
                || !popupRoot.isVisible()
                || !hasRenderableBounds(popupRoot)) {
            return false;
        }

        layoutPopupRoot(popupRoot);
        @Nullable Bounds screenBounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        return screenBounds != null
                && screenBounds.getWidth() > 0.0
                && screenBounds.getHeight() > 0.0
                && popupRoot.getOpacity() >= 0.995
                && Math.abs(popupRoot.getScaleX() - 1.0) <= 0.005
                && Math.abs(popupRoot.getScaleY() - 1.0) <= 0.005
                && Math.abs(popupRoot.getTranslateX()) <= CONTROL_EDGE_TOLERANCE
                && Math.abs(popupRoot.getTranslateY()) <= CONTROL_EDGE_TOLERANCE;
    }

    /// Returns whether a tooltip popup has a visible root that can be verified through stable snapshots.
    private static boolean tooltipPopupAttached(@Nullable M3Tooltip tooltip) {
        if (tooltip == null || !tooltip.isShowing() || tooltip.getScene() == null) {
            return false;
        }

        Parent popupRoot = tooltip.getScene().getRoot();
        layoutPopupRoot(popupRoot);
        @Nullable Bounds screenBounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        return popupRoot.isVisible()
                && hasRenderableBounds(popupRoot)
                && screenBounds != null
                && screenBounds.getWidth() > 0.0
                && screenBounds.getHeight() > 0.0
                && popupRoot.getOpacity() > 0.0;
    }

    /// Returns whether a bottom sheet has reached its fully hidden state.
    private static boolean bottomSheetHidden(@Nullable M3BottomSheet sheet) {
        return sheet != null && !sheet.isShown() && !sheet.isVisible() && !sheet.isManaged();
    }

    /// Returns whether a bottom sheet has reached its fully shown state.
    private static boolean bottomSheetShown(@Nullable M3BottomSheet sheet) {
        return sheet != null
                && sheet.isShown()
                && sheet.isVisible()
                && sheet.isManaged()
                && sheet.getOpacity() >= 0.995
                && Math.abs(sheet.getTranslateY()) <= CONTROL_EDGE_TOLERANCE
                && hasRenderableBounds(sheet);
    }

    /// Returns whether a side sheet has reached its fully hidden state.
    private static boolean sideSheetHidden(@Nullable M3SideSheet sheet) {
        return sheet != null && !sheet.isShown() && !sheet.isVisible() && !sheet.isManaged();
    }

    /// Returns whether a side sheet has reached its fully shown state.
    private static boolean sideSheetShown(@Nullable M3SideSheet sheet) {
        return sheet != null
                && sheet.isShown()
                && sheet.isVisible()
                && sheet.isManaged()
                && sheet.getOpacity() >= 0.995
                && Math.abs(sheet.getTranslateX()) <= CONTROL_EDGE_TOLERANCE
                && hasRenderableBounds(sheet);
    }

    /// Returns whether a snackbar host has reached its fully shown state.
    private static boolean snackbarSettled(@Nullable M3SnackbarHost host) {
        if (host == null || !host.isShowing()) {
            return false;
        }

        @Nullable M3Snackbar snackbar = host.getSnackbar();
        return snackbar != null
                && snackbar.isVisible()
                && snackbar.isManaged()
                && snackbar.getOpacity() >= 0.995
                && Math.abs(snackbar.getTranslateY()) <= CONTROL_EDGE_TOLERANCE
                && hasRenderableBounds(snackbar);
    }

    /// Returns whether a snackbar host has reached its fully hidden state.
    private static boolean snackbarHidden(@Nullable M3SnackbarHost host) {
        return host != null && !host.isShowing() && host.getSnackbar() == null;
    }

    /// Returns whether a FAB menu has reached its fully expanded visual state.
    private static boolean fabMenuExpandedSettled(@Nullable M3FabMenu menu) {
        if (menu == null || !menu.isExpanded()) {
            return false;
        }

        for (Node item : menu.getItems()) {
            if (!item.isVisible()
                    || !item.isManaged()
                    || item.getOpacity() < 0.995
                    || Math.abs(item.getScaleX() - 1.0) > 0.005
                    || Math.abs(item.getScaleY() - 1.0) > 0.005
                    || Math.abs(item.getTranslateY()) > CONTROL_EDGE_TOLERANCE) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether a FAB menu has reached its fully collapsed visual state.
    private static boolean fabMenuCollapsedSettled(@Nullable M3FabMenu menu) {
        if (menu == null || menu.isExpanded()) {
            return false;
        }

        for (Node item : menu.getItems()) {
            if (item.isVisible() || item.isManaged()) {
                return false;
            }
        }
        return true;
    }

    /// Verifies that an owning popup and nested popup are positioned beside each other on screen.
    private static void assertPopupStackSideBySide(Node ownerPopupRoot, Node childPopupRoot) {
        @Nullable Bounds ownerBounds = ownerPopupRoot.localToScreen(ownerPopupRoot.getBoundsInLocal());
        @Nullable Bounds childBounds = childPopupRoot.localToScreen(childPopupRoot.getBoundsInLocal());
        assertNotNull(ownerBounds, "owner popup screen bounds");
        assertNotNull(childBounds, "child popup screen bounds");

        double horizontalOverlap = Math.max(
                0.0,
                Math.min(ownerBounds.getMaxX(), childBounds.getMaxX())
                        - Math.max(ownerBounds.getMinX(), childBounds.getMinX())
        );
        double verticalOverlap = Math.max(
                0.0,
                Math.min(ownerBounds.getMaxY(), childBounds.getMaxY())
                        - Math.max(ownerBounds.getMinY(), childBounds.getMinY())
        );
        double maximumHorizontalOverlap = Math.max(24.0, Math.min(ownerBounds.getWidth(), childBounds.getWidth()) * 0.16);
        double minimumVerticalOverlap = Math.min(ownerBounds.getHeight(), childBounds.getHeight()) * 0.25;
        assertTrue(horizontalOverlap <= maximumHorizontalOverlap && verticalOverlap >= minimumVerticalOverlap,
                () -> "Nested popup stack has unsafe placement: ownerBounds=" + ownerBounds
                        + ", childBounds=" + childBounds + ", horizontalOverlap=" + horizontalOverlap
                        + ", verticalOverlap=" + verticalOverlap);
    }

    /// Verifies that popup roots keep the same installed demo theme context as the owner scene root.
    private static void assertPopupThemeContext(Parent sceneRoot, Parent popupRoot, String description) {
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS),
                () -> description + " popup root does not have the M3 theme root style class: "
                        + popupRoot.getStyleClass());
        assertSameThemeModeStyleClass(
                sceneRoot,
                popupRoot,
                M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS,
                M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS,
                description + " profile"
        );
        assertSameThemeModeStyleClass(
                sceneRoot,
                popupRoot,
                M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS,
                M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS,
                description + " brightness"
        );
    }

    /// Verifies that exactly the active owner theme mode class is present on a popup root.
    private static void assertSameThemeModeStyleClass(
            Parent sceneRoot,
            Parent popupRoot,
            String firstStyleClass,
            String secondStyleClass,
            String description
    ) {
        boolean sceneHasFirst = sceneRoot.getStyleClass().contains(firstStyleClass);
        boolean sceneHasSecond = sceneRoot.getStyleClass().contains(secondStyleClass);
        boolean popupHasFirst = popupRoot.getStyleClass().contains(firstStyleClass);
        boolean popupHasSecond = popupRoot.getStyleClass().contains(secondStyleClass);
        assertEquals(sceneHasFirst, popupHasFirst,
                () -> description + " first style class mismatch: scene=" + sceneRoot.getStyleClass()
                        + ", popup=" + popupRoot.getStyleClass());
        assertEquals(sceneHasSecond, popupHasSecond,
                () -> description + " second style class mismatch: scene=" + sceneRoot.getStyleClass()
                        + ", popup=" + popupRoot.getStyleClass());
    }

    /// Verifies that a popup menu root remains a compact menu surface.
    private static void assertPopupSurfaceSize(Node popupRoot, String description) {
        @Nullable Bounds bounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        assertNotNull(bounds, () -> description + " popup screen bounds");
        assertTrue(bounds.getWidth() >= 112.0 && bounds.getWidth() <= 360.0,
                () -> description + " popup width is not menu-like: bounds=" + bounds);
        assertTrue(bounds.getHeight() >= 48.0 && bounds.getHeight() <= 520.0,
                () -> description + " popup height is not menu-like: bounds=" + bounds);
    }

    /// Verifies that a snackbar occupies its compact message surface instead of the whole overlay.
    private static void assertSnackbarStaysCompact(Scene scene, M3Snackbar snackbar) {
        Bounds bounds = snackbar.localToScene(snackbar.getBoundsInLocal());
        assertTrue(bounds.getWidth() >= 160.0 && bounds.getWidth() <= scene.getWidth() * 0.75,
                () -> "Snackbar width is not compact: bounds=" + bounds + ", sceneWidth=" + scene.getWidth());
        assertTrue(bounds.getHeight() >= 48.0 && bounds.getHeight() <= 96.0,
                () -> "Snackbar height is not compact: bounds=" + bounds);
    }

    /// Verifies that a tooltip popup appears near its owner and stays at tooltip scale.
    private static void assertTooltipNearOwner(Node owner, Node popupRoot) {
        @Nullable Bounds ownerBounds = owner.localToScreen(owner.getBoundsInLocal());
        @Nullable Bounds popupBounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        assertNotNull(ownerBounds, "tooltip owner screen bounds");
        assertNotNull(popupBounds, "tooltip popup screen bounds");
        assertTrue(popupBounds.getMinY() >= ownerBounds.getMaxY() - 2.0,
                () -> "Tooltip popup is not below its owner: ownerBounds=" + ownerBounds
                        + ", popupBounds=" + popupBounds);
        assertTrue(popupBounds.getWidth() <= 420.0 && popupBounds.getHeight() <= 220.0,
                () -> "Tooltip popup is not compact: popupBounds=" + popupBounds);
    }

    /// Verifies that a plain tooltip popup keeps a compact label-sized surface.
    private static void assertPlainTooltipPopupSize(Node popupRoot, String description) {
        Bounds bounds = tooltipPopupScreenBounds(popupRoot, description);
        assertTrue(bounds.getWidth() >= 40.0 && bounds.getWidth() <= 220.0,
                () -> description + " width is not plain-tooltip sized: " + bounds);
        assertTrue(bounds.getHeight() >= 20.0 && bounds.getHeight() <= 72.0,
                () -> description + " height is not plain-tooltip sized: " + bounds);
    }

    /// Verifies that a long plain tooltip popup wraps inside its configured width.
    private static void assertLongTooltipPopupSize(Node popupRoot, String description) {
        Bounds bounds = tooltipPopupScreenBounds(popupRoot, description);
        assertTrue(bounds.getWidth() >= 220.0 && bounds.getWidth() <= 340.0,
                () -> description + " width does not reflect the configured wrapping width: " + bounds);
        assertTrue(bounds.getHeight() >= 36.0 && bounds.getHeight() <= 140.0,
                () -> description + " height is not a wrapped plain-tooltip height: " + bounds);
    }

    /// Verifies that a rich tooltip popup keeps a rich surface rather than a full overlay.
    private static void assertRichTooltipPopupSize(Node popupRoot, String description) {
        Bounds bounds = tooltipPopupScreenBounds(popupRoot, description);
        assertTrue(bounds.getWidth() >= 240.0 && bounds.getWidth() <= 440.0,
                () -> description + " width is not rich-tooltip sized: " + bounds);
        assertTrue(bounds.getHeight() >= 120.0 && bounds.getHeight() <= 260.0,
                () -> description + " height is not rich-tooltip sized: " + bounds);
        assertRichTooltipContentGeometry(popupRoot, description);
    }

    /// Returns a tooltip popup's screen bounds.
    private static Bounds tooltipPopupScreenBounds(Node popupRoot, String description) {
        @Nullable Bounds bounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        assertNotNull(bounds, () -> description + " screen bounds");
        return Objects.requireNonNull(bounds, "bounds");
    }

    /// Verifies that a rich tooltip popup keeps the expected structured content layout.
    private static void assertRichTooltipContentGeometry(Node popupRoot, String description) {
        Bounds popupBounds = popupRoot.localToScene(popupRoot.getBoundsInLocal());
        Node container = requireVisibleStyledDescendant(
                popupRoot,
                M3RichTooltip.CONTAINER_STYLE_CLASS,
                description + " container"
        );
        Node title = requireVisibleStyledDescendant(
                popupRoot,
                M3RichTooltip.TITLE_STYLE_CLASS,
                description + " title"
        );
        Node supportingText = requireVisibleStyledDescendant(
                popupRoot,
                M3RichTooltip.SUPPORTING_TEXT_STYLE_CLASS,
                description + " supporting text"
        );
        Node actions = requireVisibleStyledDescendant(
                popupRoot,
                M3RichTooltip.ACTIONS_STYLE_CLASS,
                description + " actions"
        );

        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        Bounds titleBounds = title.localToScene(title.getBoundsInLocal());
        Bounds supportingTextBounds = supportingText.localToScene(supportingText.getBoundsInLocal());
        Bounds actionsBounds = actions.localToScene(actions.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(popupBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " container escapes popup bounds: popup="
                        + popupBounds + ", container=" + containerBounds);
        assertTrue(containsBoundsWithTolerance(containerBounds, titleBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " title escapes container: container="
                        + containerBounds + ", title=" + titleBounds);
        assertTrue(containsBoundsWithTolerance(containerBounds, supportingTextBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " supporting text escapes container: container="
                        + containerBounds + ", supporting=" + supportingTextBounds);
        assertTrue(containsBoundsWithTolerance(containerBounds, actionsBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " actions row escapes container: container="
                        + containerBounds + ", actions=" + actionsBounds);
        assertTrue(titleBounds.getMaxY() <= supportingTextBounds.getMinY() + CONTROL_EDGE_TOLERANCE,
                () -> description + " title should render above supporting text: title="
                        + titleBounds + ", supporting=" + supportingTextBounds);
        assertTrue(supportingTextBounds.getMaxY() <= actionsBounds.getMinY() + CONTROL_EDGE_TOLERANCE,
                () -> description + " supporting text should render above actions: supporting="
                        + supportingTextBounds + ", actions=" + actionsBounds);
        assertFalse(visibleText(title).isBlank(), () -> description + " title text should be visible");
        assertFalse(visibleText(supportingText).isBlank(),
                () -> description + " supporting text should be visible");
        assertTrue(containerBounds.getWidth() >= 240.0 && containerBounds.getWidth() <= 440.0,
                () -> description + " container width is not rich-tooltip sized: " + containerBounds);
        assertTrue(actionsBounds.getHeight() >= 28.0 && actionsBounds.getHeight() <= 56.0,
                () -> description + " actions row height is not compact: " + actionsBounds);
    }

    /// Verifies that a rich tooltip action button is fully contained by the popup root.
    private static M3Button assertRichTooltipActionInsidePopup(Node popupRoot) {
        M3Button action = Objects.requireNonNull(firstVisibleButtonWithText(popupRoot, "Open"), "rich tooltip action");
        Node actions = requireVisibleStyledDescendant(
                popupRoot,
                M3RichTooltip.ACTIONS_STYLE_CLASS,
                "rich tooltip actions row"
        );
        Bounds popupBounds = popupRoot.localToScene(popupRoot.getBoundsInLocal());
        Bounds actionsBounds = actions.localToScene(actions.getBoundsInLocal());
        Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(popupBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                () -> "Rich tooltip action is clipped: popupBounds=" + popupBounds
                        + ", actionBounds=" + actionBounds);
        assertTrue(containsBoundsWithTolerance(actionsBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                () -> "Rich tooltip action should stay inside the action row: actionsBounds="
                        + actionsBounds + ", actionBounds=" + actionBounds);
        assertTrue(Math.abs(actionsBounds.getCenterY() - actionBounds.getCenterY()) <= CONTROL_EDGE_TOLERANCE,
                () -> "Rich tooltip action should be vertically centered in the action row: actionsBounds="
                        + actionsBounds + ", actionBounds=" + actionBounds);
        assertTrue(actionBounds.getHeight() >= 28.0 && actionBounds.getHeight() <= 44.0,
                () -> "Rich tooltip action height is not compact: " + actionBounds);
        assertTrue(popupBounds.getMaxY() - actionBounds.getMaxY() >= 4.0,
                () -> "Rich tooltip action has no safe bottom padding: popupBounds=" + popupBounds
                        + ", actionBounds=" + actionBounds);
        return action;
    }

    /// Verifies that a dialog pane remains a compact dialog surface.
    private static void assertDialogPaneStaysCompact(Scene ownerScene, Node dialogPane) {
        Bounds bounds = dialogPane.getBoundsInLocal();
        assertTrue(bounds.getWidth() >= 280.0 && bounds.getWidth() <= ownerScene.getWidth() * 0.70,
                () -> "Dialog pane width is not compact: bounds=" + bounds
                        + ", ownerWidth=" + ownerScene.getWidth());
        assertTrue(bounds.getHeight() >= 120.0 && bounds.getHeight() <= ownerScene.getHeight() * 0.70,
                () -> "Dialog pane height is not compact: bounds=" + bounds
                        + ", ownerHeight=" + ownerScene.getHeight());
    }

    /// Verifies that a dialog popup header does not paint a default JavaFX header strip.
    private static void assertDialogPopupHeaderUsesContainerSurface(WritableImage image) {
        int width = (int) image.getWidth();
        int headerStartX = Math.max(32, width - 96);
        int headerEndX = Math.max(headerStartX + 1, width - 32);
        Color headerSurface = averageSnapshotColor(image, headerStartX, 32, headerEndX, 58);
        Color bodySurface = averageSnapshotColor(image, headerStartX, 124, headerEndX, 150);
        double distance = colorDistance(headerSurface, bodySurface);
        assertTrue(distance <= 0.05,
                () -> "Dialog popup header paints a surface strip: header=" + headerSurface
                        + ", body=" + bodySurface
                        + ", distance=" + distance);
    }

    /// Returns the average color in a snapshot rectangle.
    private static Color averageSnapshotColor(
            WritableImage image,
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int clampedStartX = clampPixelCoordinate(startX, width);
        int clampedStartY = clampPixelCoordinate(startY, height);
        int clampedEndX = Math.max(clampedStartX + 1, clampPixelCoordinate(endX, width));
        int clampedEndY = Math.max(clampedStartY + 1, clampPixelCoordinate(endY, height));
        double red = 0.0;
        double green = 0.0;
        double blue = 0.0;
        double opacity = 0.0;
        int count = 0;
        for (int y = clampedStartY; y < clampedEndY; y++) {
            for (int x = clampedStartX; x < clampedEndX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                red += color.getRed();
                green += color.getGreen();
                blue += color.getBlue();
                opacity += color.getOpacity();
                count++;
            }
        }
        return Color.color(red / count, green / count, blue / count, opacity / count);
    }

    /// Verifies that expanded FAB menu action items remain within the owning demo showcase surface.
    private static void assertFabMenuActionsStayInsideShowcase(M3FabMenu menu) {
        Node showcase = Objects.requireNonNull(
                nearestDemoFlowAncestor(menu),
                "FAB menu showcase flow"
        );
        Bounds showcaseBounds = showcase.localToScene(showcase.getBoundsInLocal());
        for (Node item : menu.getItems()) {
            if (!item.isVisible() || !hasRenderableBounds(item)) {
                continue;
            }
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(showcaseBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "FAB menu action item escaped its showcase: showcaseBounds=" + showcaseBounds
                            + ", itemBounds=" + itemBounds);
        }
    }

    /// Verifies that an interaction visibly changes the snapshot region occupied by a node.
    private static void assertNodeAreaChanged(Node node, WritableImage before, WritableImage after, String description) {
        Bounds bounds = nodeAreaBounds(node);
        int changedPixels = countNodeAreaChangedPixels(bounds, before, after);
        int minimumChangedPixels = minimumNodeAreaChangedPixels(bounds, before, after);
        int finalChangedPixels = changedPixels;
        assertTrue(finalChangedPixels >= minimumChangedPixels,
                () -> description + " produced too little visual change: changed="
                        + finalChangedPixels + ", minimum=" + minimumChangedPixels + ", bounds=" + bounds);
    }

    /// Verifies that a sequence of rendered node-area frames visibly advances without repeating the previous frame.
    private static void assertNodeAreaFramesAdvance(Node node, List<WritableImage> frames, String description) {
        assertEquals(CONTINUOUS_ANIMATION_FRAME_COUNT, frames.size(),
                () -> description + " did not capture the expected advancing frame count: " + frames.size());
        for (int i = 1; i < frames.size(); i++) {
            assertNodeAreaChanged(node, frames.get(i - 1), frames.get(i), description + " frame " + i);
            for (int j = 0; j < i - 1; j++) {
                assertNodeAreaChanged(
                        node,
                        frames.get(j),
                        frames.get(i),
                        description + " frame " + i + " must not repeat frame " + j
                );
            }
        }
    }

    /// Verifies that loading-indicator rendered pixels stay anchored to the control center while animating.
    private static void assertLoadingIndicatorFramesRemainCentered(
            Node loadingIndicator,
            List<WritableImage> frames,
            String description
    ) {
        Bounds controlBounds = loadingIndicator.localToScene(loadingIndicator.getBoundsInLocal());
        for (int i = 0; i < frames.size(); i++) {
            int frameIndex = i;
            WritableImage frame = frames.get(i);
            Color background = sampledNodeBackgroundColor(frame, loadingIndicator);
            Point2D centroid = contrastingPixelCentroid(frame, loadingIndicator, background, 0.04);
            double dx = Math.abs(controlBounds.getCenterX() - centroid.getX());
            double dy = Math.abs(controlBounds.getCenterY() - centroid.getY());
            assertTrue(dx <= LOADING_INDICATOR_PIXEL_CENTER_TOLERANCE
                            && dy <= LOADING_INDICATOR_PIXEL_CENTER_TOLERANCE,
                    () -> description + " rendered centroid drifted in frame " + frameIndex
                            + ": dx=" + dx + ", dy=" + dy
                            + ", centroid=" + centroid + ", controlBounds=" + controlBounds);
        }
    }

    /// Verifies one loading indicator's page-level active-shape and contained-container geometry.
    private static void assertLoadingIndicatorPageGeometry(
            WritableImage image,
            M3LoadingIndicator loadingIndicator,
            String description
    ) {
        Bounds controlBounds = loadingIndicator.localToScene(loadingIndicator.getBoundsInLocal());
        Node activeShape = requireVisibleStyledDescendant(
                loadingIndicator,
                "m3-loading-indicator-indicator",
                description + " active shape"
        );
        Bounds activeShapeBounds = activeShape.localToScene(activeShape.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(controlBounds, activeShapeBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " active shape escapes the control bounds: active="
                        + activeShapeBounds + ", control=" + controlBounds);

        Color activeReference = sampledNodeBackgroundColor(image, activeShape);
        Rectangle2D activePixels = contrastingPixelBounds(image, activeShape, activeReference, 0.04);
        Point2D activeCentroid = contrastingPixelCentroid(image, activeShape, activeReference, 0.04);
        assertRectangleInsideBounds(controlBounds, activePixels, CONTROL_EDGE_TOLERANCE,
                description + " active shape pixels");
        assertLoadingIndicatorActivePixelsCentered(controlBounds, activeCentroid, description);
        assertLoadingIndicatorActivePixelExtent(activePixels, loadingIndicator, description);

        @Nullable Node container = firstVisibleStyledDescendant(loadingIndicator, "m3-loading-indicator-container");
        if (loadingIndicator.getVariant() == M3LoadingIndicatorVariant.CONTAINED) {
            Node visibleContainer = Objects.requireNonNull(container, description + " contained background");
            assertLoadingIndicatorContainedBackground(image, visibleContainer, controlBounds, description);
        } else {
            assertNull(container, () -> description + " should not expose a visible contained background");
        }
    }

    /// Verifies that loading indicator active pixels stay close to the control center.
    private static void assertLoadingIndicatorActivePixelsCentered(
            Bounds controlBounds,
            Point2D activeCentroid,
            String description
    ) {
        double dx = Math.abs(controlBounds.getCenterX() - activeCentroid.getX());
        double dy = Math.abs(controlBounds.getCenterY() - activeCentroid.getY());
        assertTrue(dx <= LOADING_INDICATOR_PIXEL_CENTER_TOLERANCE
                        && dy <= LOADING_INDICATOR_PIXEL_CENTER_TOLERANCE,
                () -> description + " active pixels are off-center: dx=" + dx + ", dy=" + dy
                        + ", centroid=" + activeCentroid + ", control=" + controlBounds);
    }

    /// Verifies that loading indicator active pixels keep the configured target size envelope.
    private static void assertLoadingIndicatorActivePixelExtent(
            Rectangle2D activePixels,
            M3LoadingIndicator loadingIndicator,
            String description
    ) {
        double indicatorSize = loadingIndicator.getIndicatorSize();
        double minimumExtent = indicatorSize * 0.35;
        double maximumExtent = indicatorSize * 1.35;
        assertTrue(activePixels.getWidth() >= minimumExtent && activePixels.getHeight() >= minimumExtent,
                () -> description + " active shape is too small for the configured indicator size: pixels="
                        + activePixels + ", indicatorSize=" + indicatorSize);
        assertTrue(activePixels.getWidth() <= maximumExtent && activePixels.getHeight() <= maximumExtent,
                () -> description + " active shape is too large for the configured indicator size: pixels="
                        + activePixels + ", indicatorSize=" + indicatorSize);
    }

    /// Verifies that a contained loading indicator paints a centered circular background.
    private static void assertLoadingIndicatorContainedBackground(
            WritableImage image,
            Node container,
            Bounds controlBounds,
            String description
    ) {
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(controlBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " contained background escapes control bounds: container="
                        + containerBounds + ", control=" + controlBounds);
        assertTrue(containsBoundsWithTolerance(containerBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " contained background does not cover the control bounds: container="
                        + containerBounds + ", control=" + controlBounds);

        Color background = sampledNodeBackgroundColor(image, container);
        Rectangle2D containerPixels = contrastingPixelBounds(image, container, background, 0.02);
        double minimumContainerExtent = Math.min(controlBounds.getWidth(), controlBounds.getHeight()) * 0.80;
        assertTrue(containerPixels.getWidth() >= minimumContainerExtent
                        && containerPixels.getHeight() >= minimumContainerExtent,
                () -> description + " contained background is not visibly filling the container: pixels="
                        + containerPixels + ", control=" + controlBounds);
        Point2D centroid = contrastingPixelCentroid(image, container, background, 0.02);
        assertLoadingIndicatorActivePixelsCentered(controlBounds, centroid, description + " contained background");
    }

    /// Verifies one demo linear progress bar's real track, active segment, and optional expressive geometry.
    private static void assertProgressBarDemoGeometry(M3ProgressBar progressBar, String description) {
        Bounds controlBounds = progressBar.localToScene(progressBar.getBoundsInLocal());
        double expectedVisualHeight = progressBar.getTrackThickness() + progressBar.getWaveAmplitude() * 2.0;
        assertTrue(controlBounds.getWidth() >= 120.0,
                () -> description + " width is too small for visual review: " + controlBounds);
        assertTrue(controlBounds.getHeight() >= progressBar.getTrackThickness() - CONTROL_EDGE_TOLERANCE
                        && controlBounds.getHeight() <= expectedVisualHeight + CONTROL_EDGE_TOLERANCE,
                () -> description + " height does not match the configured track envelope: bounds="
                        + controlBounds + ", expectedVisualHeight=" + expectedVisualHeight);

        Node container = requireVisibleStyledDescendant(
                progressBar,
                "m3-progress-bar-container",
                description + " visual container"
        );
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(controlBounds, containerBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " visual container escapes control bounds: container="
                        + containerBounds + ", control=" + controlBounds);
        assertEquals(expectedVisualHeight, containerBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " visual container height should match the configured track envelope: "
                        + containerBounds);

        List<Node> trackSegments = visibleNodesWithStyle(progressBar, "track");
        assertFalse(trackSegments.isEmpty(), () -> description + " should render at least one track segment");
        for (Node trackSegment : trackSegments) {
            assertProgressBarSegmentGeometry(
                    progressBar,
                    controlBounds,
                    trackSegment,
                    progressBar.getTrackThickness(),
                    description + " track segment"
            );
        }

        if (progressBar.getWaveAmplitude() > 0.0) {
            Node wave = requireVisibleStyledDescendant(
                    progressBar,
                    "m3-progress-bar-wave",
                    description + " expressive active wave"
            );
            assertProgressBarWaveGeometry(progressBar, controlBounds, wave, description + " expressive active wave");
            if (!progressBar.isIndeterminate()) {
                Node stop = requireVisibleStyledDescendant(
                        progressBar,
                        "m3-progress-stop",
                        description + " stop indicator"
                );
                assertProgressStopGeometry(progressBar, controlBounds, stop, description + " stop indicator");
            }
        } else {
            Node activeBar = requireVisibleStyledDescendant(progressBar, "bar", description + " active bar");
            assertProgressBarSegmentGeometry(
                    progressBar,
                    controlBounds,
                    activeBar,
                    progressBar.getTrackThickness(),
                    description + " active bar"
            );
        }
    }

    /// Verifies one rectangular progress track or active segment.
    private static void assertProgressBarSegmentGeometry(
            M3ProgressBar progressBar,
            Bounds controlBounds,
            Node segment,
            double expectedHeight,
            String description
    ) {
        Bounds segmentBounds = segment.localToScene(segment.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(controlBounds, segmentBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " escapes the progress bar: segment="
                        + segmentBounds + ", control=" + controlBounds);
        assertEquals(expectedHeight, segmentBounds.getHeight(), CONTROL_EDGE_TOLERANCE,
                () -> description + " height should match the track thickness token: segment="
                        + segmentBounds + ", trackThickness=" + progressBar.getTrackThickness());
        assertEquals(controlBounds.getCenterY(), segmentBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                () -> description + " should stay vertically centered: segment="
                        + segmentBounds + ", control=" + controlBounds);
        assertNodeSnapshotHasOpaquePixels(segment, description);
    }

    /// Verifies one expressive linear progress active wave path.
    private static void assertProgressBarWaveGeometry(
            M3ProgressBar progressBar,
            Bounds controlBounds,
            Node wave,
            String description
    ) {
        Rectangle2D pixels = renderedNodePixelBoundsInScene(wave, description);
        double capTolerance = CONTROL_EDGE_TOLERANCE + progressBar.getTrackThickness() / 2.0;
        assertRectangleInsideBounds(controlBounds, pixels, capTolerance, description);
        double minimumHeight = Math.max(1.0, progressBar.getTrackThickness() - CONTROL_EDGE_TOLERANCE);
        double maximumHeight = progressBar.getTrackThickness()
                + progressBar.getWaveAmplitude() * 2.0
                + CONTROL_EDGE_TOLERANCE * 2.0;
        assertTrue(pixels.getHeight() >= minimumHeight && pixels.getHeight() <= maximumHeight,
                () -> description + " rendered height does not match the configured wave envelope: pixels="
                        + pixels + ", minimumHeight=" + minimumHeight + ", maximumHeight=" + maximumHeight);
        assertTrue(pixels.getWidth() >= 16.0,
                () -> description + " rendered active segment is too narrow for review: pixels=" + pixels);
    }

    /// Verifies an expressive linear progress stop indicator.
    private static void assertProgressStopGeometry(
            M3ProgressBar progressBar,
            Bounds controlBounds,
            Node stop,
            String description
    ) {
        Bounds stopBounds = stop.localToScene(stop.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(controlBounds, stopBounds, CONTROL_EDGE_TOLERANCE),
                () -> description + " escapes the progress bar: stop="
                        + stopBounds + ", control=" + controlBounds);
        double maximumDiameter = Math.min(progressBar.getTrackThickness(), progressBar.getStopSize())
                + CONTROL_EDGE_TOLERANCE;
        assertTrue(stopBounds.getWidth() <= maximumDiameter && stopBounds.getHeight() <= maximumDiameter,
                () -> description + " diameter should follow the stop-size and track-thickness tokens: stop="
                        + stopBounds + ", maximumDiameter=" + maximumDiameter);
        assertEquals(controlBounds.getCenterY(), stopBounds.getCenterY(), CONTROL_EDGE_TOLERANCE,
                () -> description + " should stay vertically centered: stop="
                        + stopBounds + ", control=" + controlBounds);
        assertNodeSnapshotHasOpaquePixels(stop, description);
    }

    /// Verifies one demo circular progress indicator's real active and track geometry.
    private static void assertProgressIndicatorDemoGeometry(M3ProgressIndicator indicator, String description) {
        Bounds controlBounds = indicator.localToScene(indicator.getBoundsInLocal());
        assertTrue(controlBounds.getWidth() >= indicator.getIndicatorSize() - CONTROL_EDGE_TOLERANCE
                        && controlBounds.getHeight() >= indicator.getIndicatorSize() - CONTROL_EDGE_TOLERANCE,
                () -> description + " layout is smaller than the configured indicator size: bounds="
                        + controlBounds + ", indicatorSize=" + indicator.getIndicatorSize());
        assertNodeSnapshotHasOpaquePixels(indicator, description);

        if (indicator.getWaveAmplitude() > 0.0) {
            Node trackWave = requireVisibleStyledDescendant(
                    indicator,
                    "m3-progress-indicator-track-wave",
                    description + " expressive track"
            );
            Node activeWave = requireVisibleStyledDescendant(
                    indicator,
                    "m3-progress-indicator-wave",
                    description + " expressive active wave"
            );
            assertProgressCircularNodeGeometry(indicator, controlBounds, trackWave, description + " expressive track");
            assertProgressCircularNodeGeometry(indicator, controlBounds, activeWave, description + " expressive active");
        } else {
            Node activeArc = requireVisibleStyledDescendant(indicator, "indicator", description + " active arc");
            assertProgressCircularNodeGeometry(indicator, controlBounds, activeArc, description + " active arc");
            if (!indicator.isIndeterminate()) {
                Node track = requireVisibleStyledDescendant(indicator, "track", description + " track");
                assertProgressCircularNodeGeometry(indicator, controlBounds, track, description + " track");
            }
        }
    }

    /// Verifies one circular progress path or arc stays within the expected visual envelope.
    private static void assertProgressCircularNodeGeometry(
            M3ProgressIndicator indicator,
            Bounds controlBounds,
            Node node,
            String description
    ) {
        Rectangle2D pixels = renderedNodePixelBoundsInScene(node, description);
        double tolerance = CONTROL_EDGE_TOLERANCE + indicator.getWaveAmplitude() + indicator.getTrackThickness();
        assertRectangleInsideBounds(controlBounds, pixels, tolerance, description);
        assertTrue(pixels.getWidth() >= indicator.getTrackThickness()
                        && pixels.getHeight() >= indicator.getTrackThickness(),
                () -> description + " rendered pixels are too small for the configured stroke: pixels="
                        + pixels + ", trackThickness=" + indicator.getTrackThickness());
        assertTrue(pixels.getWidth() <= controlBounds.getWidth() + tolerance * 2.0
                        && pixels.getHeight() <= controlBounds.getHeight() + tolerance * 2.0,
                () -> description + " rendered pixels exceed the circular progress envelope: pixels="
                        + pixels + ", control=" + controlBounds + ", tolerance=" + tolerance);
    }

    /// Returns the last frame in a non-empty animation frame list.
    private static WritableImage lastFrame(List<WritableImage> frames, String description) {
        assertFalse(frames.isEmpty(), () -> description + " did not capture any frames");
        return frames.get(frames.size() - 1);
    }

    /// Verifies that ripple opacity reaches a visible release frame and then fades to transparency.
    private static void assertRippleOpacityFaded(double releaseOpacity, double settledOpacity, String description) {
        assertTrue(releaseOpacity > 0.02,
                () -> description + " release opacity was not visible: " + releaseOpacity);
        assertTrue(settledOpacity <= 0.001,
                () -> description + " settled opacity did not clear: " + settledOpacity);
        assertTrue(settledOpacity < releaseOpacity,
                () -> description + " opacity did not decrease: release="
                        + releaseOpacity + ", settled=" + settledOpacity);
    }

    /// Verifies that two standalone snapshots differ by more than antialiasing noise.
    private static void assertSnapshotChanged(WritableImage before, WritableImage after, String description) {
        int changedPixels = countSnapshotChangedPixels(before, after);
        int minimumChangedPixels = minimumSnapshotChangedPixels(before, after);
        int finalChangedPixels = changedPixels;
        assertTrue(finalChangedPixels >= minimumChangedPixels,
                () -> description + " produced too little visual change: changed="
                        + finalChangedPixels + ", minimum=" + minimumChangedPixels
                        + ", before=" + before.getWidth() + "x" + before.getHeight()
                        + ", after=" + after.getWidth() + "x" + after.getHeight());
    }

    /// Captures a scene frame when the snapshot area occupied by a node changes enough from a baseline.
    private static boolean captureSceneFrameWithChangedNodeArea(
            Node node,
            WritableImage baseline,
            Scene scene,
            AtomicReference<@Nullable WritableImage> frameReference
    ) {
        return captureSceneFrameWithChangedNodeArea(node, baseline, scene, frameReference, null, "node area");
    }

    /// Captures a scene frame when the snapshot area occupied by a node changes enough from a baseline.
    private static boolean captureSceneFrameWithChangedNodeArea(
            Node node,
            WritableImage baseline,
            Scene scene,
            AtomicReference<@Nullable WritableImage> frameReference,
            @Nullable AtomicReference<String> diagnostics,
            String description
    ) {
        WritableImage frame = snapshot(scene);
        Bounds bounds = nodeAreaBounds(node);
        int changedPixels = countNodeAreaChangedPixels(bounds, baseline, frame);
        int minimumChangedPixels = minimumNodeAreaChangedPixels(bounds, baseline, frame);
        if (changedPixels < minimumChangedPixels) {
            updateVisualWaitDiagnostics(
                    diagnostics,
                    description + " changed too little: changed=" + changedPixels
                            + ", minimum=" + minimumChangedPixels
                            + ", bounds=" + bounds
            );
            return false;
        }

        frameReference.set(frame);
        updateVisualWaitDiagnostics(
                diagnostics,
                description + " changed enough: changed=" + changedPixels
                        + ", minimum=" + minimumChangedPixels
                        + ", bounds=" + bounds
        );
        return true;
    }

    /// Captures a node frame when it changes enough from a baseline snapshot.
    private static boolean captureNodeFrameChangedFrom(
            Node node,
            WritableImage baseline,
            AtomicReference<@Nullable WritableImage> frameReference,
            @Nullable AtomicReference<String> diagnostics,
            String description
    ) {
        layoutPopupRoot(node);
        WritableImage frame = snapshotNode(node);
        int changedPixels = countSnapshotChangedPixels(baseline, frame);
        int minimumChangedPixels = minimumSnapshotChangedPixels(baseline, frame);
        if (changedPixels < minimumChangedPixels) {
            updateVisualWaitDiagnostics(
                    diagnostics,
                    description + " changed too little: changed=" + changedPixels
                            + ", minimum=" + minimumChangedPixels
                            + ", size=" + baseline.getWidth() + "x" + baseline.getHeight()
            );
            return false;
        }

        frameReference.set(frame);
        updateVisualWaitDiagnostics(
                diagnostics,
                description + " changed enough: changed=" + changedPixels
                        + ", minimum=" + minimumChangedPixels
                        + ", size=" + frame.getWidth() + "x" + frame.getHeight()
        );
        return true;
    }

    /// Updates a visual wait diagnostic when a diagnostic sink is available.
    private static void updateVisualWaitDiagnostics(
            @Nullable AtomicReference<String> diagnostics,
            String message
    ) {
        if (diagnostics != null) {
            diagnostics.set(message);
        }
    }

    /// Returns whether the snapshot area occupied by a node changed enough between two scene snapshots.
    private static boolean nodeAreaChangedEnough(Node node, WritableImage before, WritableImage after) {
        return nodeAreaChangedEnough(node, before, after, null, "node area");
    }

    /// Returns whether the snapshot area occupied by a node changed enough between two scene snapshots.
    private static boolean nodeAreaChangedEnough(
            Node node,
            WritableImage before,
            WritableImage after,
            @Nullable AtomicReference<String> diagnostics,
            String description
    ) {
        Bounds bounds = nodeAreaBounds(node);
        int changedPixels = countNodeAreaChangedPixels(bounds, before, after);
        int minimumChangedPixels = minimumNodeAreaChangedPixels(bounds, before, after);
        if (changedPixels < minimumChangedPixels) {
            updateVisualWaitDiagnostics(
                    diagnostics,
                    description + " changed too little: changed=" + changedPixels
                            + ", minimum=" + minimumChangedPixels
                            + ", bounds=" + bounds
            );
            return false;
        }

        updateVisualWaitDiagnostics(
                diagnostics,
                description + " changed enough: changed=" + changedPixels
                        + ", minimum=" + minimumChangedPixels
                        + ", bounds=" + bounds
        );
        return true;
    }

    /// Counts changed pixels within the snapshot area occupied by a node.
    private static int countNodeAreaChangedPixels(Bounds bounds, WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        int minX = clampPixel(Math.floor(bounds.getMinX()), width);
        int minY = clampPixel(Math.floor(bounds.getMinY()), height);
        int maxX = clampPixel(Math.ceil(bounds.getMaxX()), width);
        int maxY = clampPixel(Math.ceil(bounds.getMaxY()), height);
        int changedPixels = 0;
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (colorDistance(before.getPixelReader().getColor(x, y), after.getPixelReader().getColor(x, y)) > 0.02) {
                    changedPixels++;
                }
            }
        }
        return changedPixels;
    }

    /// Returns the minimum changed pixel count for a node-area visual change assertion.
    private static int minimumNodeAreaChangedPixels(Bounds bounds, WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        int minX = clampPixel(Math.floor(bounds.getMinX()), width);
        int minY = clampPixel(Math.floor(bounds.getMinY()), height);
        int maxX = clampPixel(Math.ceil(bounds.getMaxX()), width);
        int maxY = clampPixel(Math.ceil(bounds.getMaxY()), height);
        return minimumChangedPixels(Math.max(1, (maxX - minX) * (maxY - minY)));
    }

    /// Counts changed pixels between two standalone snapshots.
    private static int countSnapshotChangedPixels(WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        int changedPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixelDistance(before.getPixelReader().getColor(x, y), after.getPixelReader().getColor(x, y)) > 0.02) {
                    changedPixels++;
                }
            }
        }
        return changedPixels;
    }

    /// Returns the minimum changed pixel count for a standalone snapshot visual change assertion.
    private static int minimumSnapshotChangedPixels(WritableImage before, WritableImage after) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        return minimumChangedPixels(Math.max(1, width * height));
    }

    /// Returns the minimum changed pixel count for a visual change assertion.
    private static int minimumChangedPixels(int totalPixels) {
        return Math.max(8, totalPixels / 250);
    }

    /// Writes a named interaction-state visual snapshot to the build report directory.
    private static void writeInteractionSnapshot(WritableImage image, String targetName, String stateName) {
        writeVisualSnapshot(image, Path.of(
                "build",
                "reports",
                "m3fx-demo-visual",
                "interaction-" + targetName + "-" + stateName + ".png"
        ));
    }

    /// Writes a named animation-frame visual snapshot to the build report directory.
    private static void writeAnimationSnapshot(WritableImage image, String targetName, String stateName) {
        writeVisualSnapshot(image, Path.of(
                "build",
                "reports",
                "m3fx-demo-visual",
                "animation-" + targetName + "-" + stateName + ".png"
        ));
    }

    /// Writes a sequence of advancing animation-frame snapshots to the build report directory.
    private static void writeAnimationFrameSnapshots(List<WritableImage> frames, String targetName) {
        for (int i = 0; i < frames.size(); i++) {
            writeAnimationSnapshot(frames.get(i), targetName, "frame-" + (char) ('a' + i));
        }
    }

    /// Clamps a floating point coordinate to a valid snapshot pixel coordinate.
    private static int clampPixel(double coordinate, double size) {
        return Math.max(0, Math.min((int) Math.ceil(size), (int) coordinate));
    }

    /// Applies the hover pseudo-class to a node before rendering an interaction state.
    private static void applyHoverPseudoState(Node node) {
        node.pseudoClassStateChanged(HOVER_PSEUDO_CLASS, true);
    }

    /// Clears the hover pseudo-class from a node after rendering an interaction state.
    private static void clearHoverPseudoState(Node node) {
        node.pseudoClassStateChanged(HOVER_PSEUDO_CLASS, false);
    }

    /// Fires a primary-button mouse event at the center of a node.
    private static void firePrimaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            boolean primaryButtonDown
    ) {
        Bounds bounds = node.getBoundsInLocal();
        double x = bounds.getMinX() + bounds.getWidth() / 2.0;
        double y = bounds.getMinY() + bounds.getHeight() / 2.0;
        node.fireEvent(primaryMouseEvent(node, eventType, x, y, primaryButtonDown));
    }

    /// Fires a primary-button mouse event at the center of a node's layout bounds.
    private static void firePrimaryMouseEventAtLayoutCenter(
            Node node,
            EventType<MouseEvent> eventType,
            boolean primaryButtonDown
    ) {
        Bounds bounds = node.getLayoutBounds();
        double x = bounds.getMinX() + bounds.getWidth() / 2.0;
        double y = bounds.getMinY() + bounds.getHeight() / 2.0;
        node.fireEvent(primaryMouseEvent(node, eventType, x, y, primaryButtonDown));
    }

    /// Creates a primary-button mouse event at one local point of a node.
    private static MouseEvent primaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                scenePoint.getX(),
                scenePoint.getY(),
                screenX,
                screenY,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(node, scenePoint.getX(), scenePoint.getY())
        );
    }

    /// Creates one keyboard event for synthetic demo keyboard traversal checks.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }

    /// Returns the first visible text node below a target node.
    private static @Nullable Text firstVisibleText(Node node) {
        if (node instanceof Text text && !text.getText().isBlank() && hasRenderableBounds(text)) {
            return text;
        }
        if (node instanceof M3Icon icon) {
            @Nullable Node iconText = icon.lookup(".text");
            if (iconText instanceof Text text && !text.getText().isBlank() && hasRenderableBounds(text)) {
                return text;
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Text text = firstVisibleText(child);
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    /// Returns the first visible demo SVG icon below a target node.
    private static @Nullable Node firstVisibleDemoVectorIcon(Node node) {
        return firstVisibleVectorIconWithStyle(node, DemoIcons.STYLE_CLASS);
    }

    /// Returns the first visible Material SVG icon below a target node.
    private static @Nullable Node firstVisibleMaterialVectorIcon(Node node) {
        @Nullable Node demoIcon = firstVisibleDemoVectorIcon(node);
        return demoIcon == null ? firstVisibleVectorIconWithStyle(node, "m3-internal-icon-path") : demoIcon;
    }

    /// Returns the first visible SVG icon below a target node with the requested style class.
    private static @Nullable Node firstVisibleVectorIconWithStyle(Node node, String styleClass) {
        if (node instanceof SVGPath
                && node.getStyleClass().contains(styleClass)
                && hasRenderableBounds(node)) {
            return node;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node icon = firstVisibleVectorIconWithStyle(child, styleClass);
                if (icon != null) {
                    return icon;
                }
            }
        }
        return null;
    }

    /// Returns whether a node has visible non-empty bounds.
    private static boolean hasRenderableBounds(Node node) {
        Bounds bounds = node.getBoundsInLocal();
        return bounds.getWidth() > 0.5 && bounds.getHeight() > 0.5;
    }

    /// Returns a stable scene-space area for visual change comparisons.
    private static Bounds nodeAreaBounds(Node node) {
        try {
            return node.localToScene(node.getBoundsInLocal());
        } catch (ConcurrentModificationException | IndexOutOfBoundsException e) {
            return node.localToScene(node.getLayoutBounds());
        }
    }

    /// Returns whether `outer` fully contains `inner` after applying a small edge tolerance.
    private static boolean containsBoundsWithTolerance(Bounds outer, Bounds inner, double tolerance) {
        return inner.getMinX() >= outer.getMinX() - tolerance
                && inner.getMinY() >= outer.getMinY() - tolerance
                && inner.getMaxX() <= outer.getMaxX() + tolerance
                && inner.getMaxY() <= outer.getMaxY() + tolerance;
    }

    /// Returns whether `outer` contains the horizontal span of `inner` after applying a small edge tolerance.
    private static boolean containsHorizontalBoundsWithTolerance(Bounds outer, Bounds inner, double tolerance) {
        return inner.getMinX() >= outer.getMinX() - tolerance
                && inner.getMaxX() <= outer.getMaxX() + tolerance;
    }

    /// Verifies that a rendered pixel rectangle remains inside a layout bounds.
    private static void assertRectangleInsideBounds(
            Bounds outer,
            Rectangle2D inner,
            double tolerance,
            String description
    ) {
        assertTrue(
                inner.getMinX() >= outer.getMinX() - tolerance
                        && inner.getMinY() >= outer.getMinY() - tolerance
                        && inner.getMaxX() <= outer.getMaxX() + tolerance
                        && inner.getMaxY() <= outer.getMaxY() + tolerance,
                () -> description + " leaves its target bounds: pixels=" + inner + ", target=" + outer
        );
    }

    /// Verifies that a rendered pixel rectangle remains horizontally inside a layout bounds.
    private static void assertRectangleHorizontallyInsideBounds(
            Bounds outer,
            Rectangle2D inner,
            double tolerance,
            String description
    ) {
        assertTrue(
                inner.getMinX() >= outer.getMinX() - tolerance
                        && inner.getMaxX() <= outer.getMaxX() + tolerance,
                () -> description + " leaves its target bounds horizontally: pixels=" + inner + ", target=" + outer
        );
    }

    /// Returns whether a node bounds touches a scroll viewport edge where partial vertical visibility is expected.
    private static boolean touchesVerticalViewportEdge(Bounds inner, Bounds viewport, double tolerance) {
        return inner.getMinY() < viewport.getMinY() + tolerance
                || inner.getMaxY() > viewport.getMaxY() - tolerance;
    }

    /// Returns the nearest demo flow ancestor.
    private static @Nullable Node nearestDemoFlowAncestor(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains("demo-flow")) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Visits visible descendants in a rendered hierarchy.
    private static void visitVisibleNodes(Node node, Consumer<Node> visitor) {
        if (!node.isVisible()) {
            return;
        }

        visitor.accept(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                visitVisibleNodes(child, visitor);
            }
        }
    }

    /// Writes a visual snapshot to the build report directory.
    private static void writeVisualSnapshot(WritableImage image, Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ImageIO.write(toBufferedImage(image), "png", path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Converts a JavaFX image into a desktop image.
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        return bufferedImage;
    }

    /// Returns a filesystem-safe snapshot file name.
    private static String snapshotFileName(String pageTitle) {
        return pageTitle.toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    /// Returns the centroid of rendered pixels inside a node that contrast with the reference color.
    private static Point2D contrastingPixelCentroid(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        double totalWeight = 0.0;
        double weightedX = 0.0;
        double weightedY = 0.0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                double distance = colorDistance(color, reference);
                if (color.getOpacity() > 0.1 && distance >= minimumDistance) {
                    double weight = color.getOpacity() * Math.min(1.0, distance);
                    totalWeight += weight;
                    weightedX += (x + 0.5) * weight;
                    weightedY += (y + 0.5) * weight;
                }
            }
        }

        assertTrue(totalWeight > 0.0, () -> "No contrasting pixels found for " + node);
        return new Point2D(weightedX / totalWeight, weightedY / totalWeight);
    }

    /// Returns the rendered-pixel bounds inside a node that contrast with the supplied reference color.
    private static Rectangle2D contrastingPixelBounds(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        int minX = endX;
        int minY = endY;
        int maxX = startX - 1;
        int maxY = startY - 1;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, reference) >= minimumDistance) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        assertTrue(maxX >= minX && maxY >= minY, () -> "No contrasting pixels found for " + node);
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Returns the rendered text ink bounds inside a scene snapshot, or `null` when pixel classification is inconclusive.
    private static @Nullable Rectangle2D renderedTextInkBounds(WritableImage image, Text text) {
        if (text.getFill() instanceof Color fill) {
            @Nullable Rectangle2D inkBounds = null;
            @Nullable Rectangle2D strictFillBounds = textFillPixelBounds(image, text, fill, null, 0.24);
            if (strictFillBounds != null) {
                inkBounds = strictFillBounds;
            }

            Color background = sampledTextBackgroundColor(image, text);
            @Nullable Rectangle2D blendedFillBounds = textFillPixelBounds(image, text, fill, background, 0.45);
            if (blendedFillBounds != null) {
                inkBounds = unionRectangles(inkBounds, blendedFillBounds);
            }

            @Nullable Rectangle2D contrastingBounds = contrastingPixelBoundsOrNull(image, text, background, 0.04);
            return unionRectangles(inkBounds, contrastingBounds);
        }

        return contrastingPixelBoundsOrNull(image, text, sampledTextBackgroundColor(image, text), 0.04);
    }

    /// Returns the union of two nullable rectangle bounds.
    private static @Nullable Rectangle2D unionRectangles(
            @Nullable Rectangle2D first,
            @Nullable Rectangle2D second
    ) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }

        double minX = Math.min(first.getMinX(), second.getMinX());
        double minY = Math.min(first.getMinY(), second.getMinY());
        double maxX = Math.max(first.getMaxX(), second.getMaxX());
        double maxY = Math.max(first.getMaxY(), second.getMaxY());
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    /// Returns the rendered-pixel bounds inside a node that contrast with a reference color, or `null`.
    private static @Nullable Rectangle2D contrastingPixelBoundsOrNull(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        int minX = endX;
        int minY = endY;
        int maxX = startX - 1;
        int maxY = startY - 1;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, reference) >= minimumDistance) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return null;
        }
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Returns pixel bounds for text pixels close to the requested fill color.
    private static @Nullable Rectangle2D textFillPixelBounds(
            WritableImage image,
            Text text,
            Color fill,
            @Nullable Color background,
            double maximumFillDistance
    ) {
        Bounds bounds = text.localToScene(text.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        int minX = endX;
        int minY = endY;
        int maxX = startX - 1;
        int maxY = startY - 1;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                double fillDistance = colorDistance(color, fill);
                if (fillDistance > maximumFillDistance) {
                    continue;
                }
                if (background != null && fillDistance + 0.02 >= colorDistance(color, background)) {
                    continue;
                }

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        if (maxX < minX || maxY < minY) {
            return null;
        }
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Samples the local background near a node before measuring rendered ink pixels.
    private static Color sampledNodeBackgroundColor(WritableImage image, Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int x = clampPixelCoordinate((int) Math.floor(bounds.getMinX()), (int) image.getWidth());
        int y = clampPixelCoordinate((int) Math.floor(bounds.getMinY()), (int) image.getHeight());
        return image.getPixelReader().getColor(x, y);
    }

    /// Samples a nearby background pixel around a text node before measuring rendered text ink.
    private static Color sampledTextBackgroundColor(WritableImage image, Text text) {
        Bounds bounds = text.localToScene(text.getBoundsInLocal());
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int left = Math.max(0, (int) Math.floor(bounds.getMinX()) - 3);
        int right = Math.min(width - 1, (int) Math.ceil(bounds.getMaxX()) + 3);
        int top = Math.max(0, (int) Math.floor(bounds.getMinY()) - 3);
        int bottom = Math.min(height - 1, (int) Math.ceil(bounds.getMaxY()) + 3);
        @Nullable Color textFill = text.getFill() instanceof Color color ? color : null;
        @Nullable Color bestColor = null;
        double bestDistance = -1.0;

        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                if (x >= bounds.getMinX() && x <= bounds.getMaxX()
                        && y >= bounds.getMinY() && y <= bounds.getMaxY()) {
                    continue;
                }

                Color color = image.getPixelReader().getColor(x, y);
                if (textFill == null) {
                    return color;
                }

                double distance = colorDistance(color, textFill);
                if (distance > bestDistance) {
                    bestDistance = distance;
                    bestColor = color;
                }
            }
        }

        return bestColor == null ? sampledNodeBackgroundColor(image, text) : bestColor;
    }

    /// Clamps one pixel coordinate to a readable image index.
    private static int clampPixelCoordinate(int coordinate, int dimension) {
        return Math.max(0, Math.min(dimension - 1, coordinate));
    }

    /// Returns a simple RGB distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }

    /// Returns an RGBA distance between two pixels.
    private static double pixelDistance(Color first, Color second) {
        return colorDistance(first, second) + Math.abs(first.getOpacity() - second.getOpacity());
    }

    /// Runs a task on the JavaFX application thread and propagates failures.
    private static void runOnFxThread(Runnable task) {
        DemoFxTestUtils.runOnFxThread(task);
    }

    /// Runs setup on the FX thread and verifies the result when a condition becomes true.
    private static void runOnFxThreadWhen(
            BooleanSupplier condition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        DemoFxTestUtils.runOnFxThreadWhen(condition, setup, verification);
    }

    /// Runs setup on the FX thread and verifies the result when a condition becomes true.
    private static void runOnFxThreadWhen(
            BooleanSupplier condition,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        DemoFxTestUtils.runOnFxThreadWhen(condition, timeoutMessage, setup, verification);
    }

    /// Runs setup on the FX thread and verifies the result after a condition stays true for pulses.
    private static void runOnFxThreadWhenStable(
            BooleanSupplier condition,
            int stablePulseCount,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        DemoFxTestUtils.runOnFxThreadWhenStable(condition, stablePulseCount, setup, verification);
    }

    /// Runs setup on the FX thread and verifies the result after a condition stays true for pulses.
    private static void runOnFxThreadWhenStable(
            BooleanSupplier condition,
            int stablePulseCount,
            Supplier<String> timeoutMessage,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        DemoFxTestUtils.runOnFxThreadWhenStable(condition, stablePulseCount, timeoutMessage, setup, verification);
    }

    /// Runs setup and verifies after a referenced node snapshot visibly changes from a baseline snapshot.
    private static void runOnFxThreadWhenNodeSnapshotChanged(
            Supplier<@Nullable Node> nodeSupplier,
            AtomicReference<@Nullable WritableImage> baselineReference,
            AtomicReference<@Nullable WritableImage> frameReference,
            String description,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<String> diagnostics = new AtomicReference<>("visual snapshot wait has not run yet");
        runOnFxThreadWhen(() -> {
            @Nullable Node node = nodeSupplier.get();
            @Nullable WritableImage baseline = baselineReference.get();
            if (node == null) {
                diagnostics.set(description + " node reference is not set");
                return false;
            }
            if (baseline == null) {
                diagnostics.set(description + " baseline snapshot is not set for " + node);
                return false;
            }
            if (node.getScene() == null) {
                diagnostics.set(description + " node is not attached to a scene: " + node);
                return false;
            }

            return captureNodeFrameChangedFrom(node, baseline, frameReference, diagnostics, description);
        }, () -> "Timed out waiting for node snapshot visual change: " + diagnostics.get(), setup, verification);
    }

    /// Runs setup and verifies after a referenced node snapshot stops visibly changing between pulses.
    private static void runOnFxThreadWhenNodeSnapshotStable(
            Supplier<@Nullable Node> nodeSupplier,
            AtomicReference<@Nullable WritableImage> frameReference,
            BooleanSupplier additionalCondition,
            String description,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<@Nullable WritableImage> previousFrameReference = new AtomicReference<>();
        AtomicReference<String> diagnostics = new AtomicReference<>("visual snapshot stability wait has not run yet");
        runOnFxThreadWhenStable(() -> {
            @Nullable Node node = nodeSupplier.get();
            if (node == null) {
                diagnostics.set(description + " node reference is not set");
                return false;
            }
            if (node.getScene() == null) {
                diagnostics.set(description + " node is not attached to a scene: " + node);
                return false;
            }
            if (!additionalCondition.getAsBoolean()) {
                diagnostics.set(description + " additional stability condition is not satisfied for " + node);
                return false;
            }

            layoutPopupRoot(node);
            WritableImage frame = snapshotNode(node);
            @Nullable WritableImage previousFrame = previousFrameReference.get();
            previousFrameReference.set(frame);
            frameReference.set(frame);
            if (previousFrame == null) {
                diagnostics.set(description + " captured first stability frame for " + node);
                return false;
            }

            int changedPixels = countSnapshotChangedPixels(previousFrame, frame);
            int minimumChangedPixels = minimumSnapshotChangedPixels(previousFrame, frame);
            if (changedPixels >= minimumChangedPixels) {
                diagnostics.set(description + " is still changing: changed="
                        + changedPixels + ", minimum=" + minimumChangedPixels
                        + ", size=" + frame.getWidth() + "x" + frame.getHeight());
                return false;
            }

            diagnostics.set(description + " snapshot is stable for " + node);
            return true;
        }, SETTLED_STATE_PULSES, () -> "Timed out waiting for stable node snapshot: " + diagnostics.get(),
                setup, verification);
    }

    /// Runs setup and verifies after a referenced node area visibly changes from a baseline snapshot.
    private static <T extends Node> void runOnFxThreadWhenNodeAreaChanged(
            AtomicReference<@Nullable T> nodeReference,
            AtomicReference<@Nullable WritableImage> baselineReference,
            AtomicReference<@Nullable Scene> sceneReference,
            AtomicReference<@Nullable WritableImage> frameReference,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<String> diagnostics = new AtomicReference<>("visual change wait has not run yet");
        runOnFxThreadWhen(() -> {
            @Nullable Node node = nodeReference.get();
            @Nullable WritableImage baseline = baselineReference.get();
            @Nullable Scene scene = sceneReference.get();
            if (node == null) {
                diagnostics.set("node reference is not set");
                return false;
            }
            if (baseline == null) {
                diagnostics.set("baseline snapshot is not set for " + node);
                return false;
            }
            if (scene == null) {
                diagnostics.set("scene reference is not set for " + node);
                return false;
            }

            return captureSceneFrameWithChangedNodeArea(
                    node,
                    baseline,
                    scene,
                    frameReference,
                    diagnostics,
                    "node area for " + node
            );
        }, () -> "Timed out waiting for node-area visual change: " + diagnostics.get(), setup, verification);
    }

    /// Runs setup and verifies after a referenced node area visibly advances through multiple frames.
    private static <T extends Node> void runOnFxThreadWhenNodeAreaAdvances(
            AtomicReference<@Nullable T> nodeReference,
            AtomicReference<@Nullable Scene> sceneReference,
            AtomicReference<@Nullable WritableImage> baselineReference,
            List<WritableImage> frameList,
            int frameCount,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        if (frameCount < 2) {
            throw new IllegalArgumentException("frameCount must be at least 2");
        }

        frameList.clear();
        AtomicReference<@Nullable WritableImage> previousFrameReference = new AtomicReference<>();
        AtomicReference<String> diagnostics = new AtomicReference<>("visual frame sequence wait has not run yet");
        runOnFxThreadWhen(() -> {
            @Nullable Node node = nodeReference.get();
            @Nullable Scene scene = sceneReference.get();
            @Nullable WritableImage baseline = baselineReference.get();
            if (node == null) {
                diagnostics.set("node reference is not set");
                return false;
            }
            if (scene == null) {
                diagnostics.set("scene reference is not set for " + node);
                return false;
            }
            if (baseline == null) {
                diagnostics.set("baseline snapshot is not set for " + node);
                return false;
            }

            WritableImage frame = snapshot(scene);
            @Nullable WritableImage previousFrame = previousFrameReference.get();
            if (previousFrame == null) {
                previousFrame = baseline;
            }

            if (!nodeAreaChangedEnough(
                    node,
                    previousFrame,
                    frame,
                    diagnostics,
                    "candidate frame " + (frameList.size() + 1) + " versus previous frame for " + node
            )) {
                return false;
            }
            if (!nodeAreaChangedEnough(
                    node,
                    baseline,
                    frame,
                    diagnostics,
                    "candidate frame " + (frameList.size() + 1) + " versus baseline for " + node
            )) {
                return false;
            }
            for (WritableImage acceptedFrame : frameList) {
                if (!nodeAreaChangedEnough(
                        node,
                        acceptedFrame,
                        frame,
                        diagnostics,
                        "candidate frame " + (frameList.size() + 1)
                                + " versus accepted frame " + frameList.indexOf(acceptedFrame)
                                + " for " + node
                )) {
                    return false;
                }
            }

            previousFrameReference.set(frame);
            frameList.add(frame);
            diagnostics.set("captured " + frameList.size() + " of " + frameCount + " advancing frames for " + node);
            return frameList.size() >= frameCount;
        }, () -> "Timed out waiting for advancing node-area visual frames: " + diagnostics.get(), setup, verification);
    }

    /// Runs setup and verifies after a referenced node area stops visibly changing between pulses.
    private static <T extends Node> void runOnFxThreadWhenNodeAreaStable(
            AtomicReference<@Nullable T> nodeReference,
            AtomicReference<@Nullable Scene> sceneReference,
            AtomicReference<@Nullable WritableImage> frameReference,
            BooleanSupplier additionalCondition,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<@Nullable WritableImage> previousFrameReference = new AtomicReference<>();
        AtomicReference<String> diagnostics = new AtomicReference<>("visual stability wait has not run yet");
        runOnFxThreadWhenStable(() -> {
            @Nullable Node node = nodeReference.get();
            @Nullable Scene scene = sceneReference.get();
            if (node == null) {
                diagnostics.set("node reference is not set");
                return false;
            }
            if (scene == null) {
                diagnostics.set("scene reference is not set for " + node);
                return false;
            }
            if (!additionalCondition.getAsBoolean()) {
                diagnostics.set("additional stability condition is not satisfied for " + node);
                return false;
            }

            WritableImage frame = snapshot(scene);
            @Nullable WritableImage previousFrame = previousFrameReference.get();
            previousFrameReference.set(frame);
            frameReference.set(frame);
            if (previousFrame == null) {
                diagnostics.set("captured first stability frame for " + node);
                return false;
            }
            if (nodeAreaChangedEnough(
                    node,
                    previousFrame,
                    frame,
                    diagnostics,
                    "stability comparison for " + node
            )) {
                return false;
            }
            diagnostics.set("node area is stable for " + node);
            return true;
        }, SETTLED_STATE_PULSES, () -> "Timed out waiting for stable node-area frame: " + diagnostics.get(),
                setup, verification);
    }
}
