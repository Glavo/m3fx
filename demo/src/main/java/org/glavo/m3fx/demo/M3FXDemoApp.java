// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3AvatarVariant;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BadgedBox;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.controls.M3ButtonShape;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3ChipStyle;
import org.glavo.m3fx.controls.M3DateRange;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerDialog;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3DatePresets;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.controls.M3DateRangePickerDialog;
import org.glavo.m3fx.controls.M3DateRangePickerField;
import org.glavo.m3fx.controls.M3DateRangePresets;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FilterChip;
import org.glavo.m3fx.controls.M3InputChip;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3FormRow;
import org.glavo.m3fx.controls.M3FormSection;
import org.glavo.m3fx.controls.M3FormValidator;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconButtonWidth;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListCell;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3LoadingIndicatorVariant;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuColorStyle;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3MenuSectionHeader;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationDrawerVariant;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3NavigationRailVariant;
import org.glavo.m3fx.controls.M3PasswordField;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3RangeSlider;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SearchViewLayout;
import org.glavo.m3fx.controls.M3SearchViewStyle;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SliderSize;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3SuggestionChip;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3SubMenuItem;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3SurfaceElevation;
import org.glavo.m3fx.controls.M3SurfaceVariant;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3TabBarLayout;
import org.glavo.m3fx.controls.M3TabBarVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputValidators;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.controls.M3TimePickerDialog;
import org.glavo.m3fx.controls.M3TimePickerField;
import org.glavo.m3fx.controls.M3TimePresets;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.controls.M3Toolbar;
import org.glavo.m3fx.controls.M3ToolbarColorStyle;
import org.glavo.m3fx.controls.M3ToolbarVariant;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.controls.M3ValidationSummary;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/// A demo application that showcases M3FX controls.
@NotNullByDefault
public final class M3FXDemoApp extends Application {
    /// Creates the demo application.
    public M3FXDemoApp() {
    }

    /// Seed colors shown in the demo header.
    private static final @Unmodifiable List<Color> SEED_COLORS = List.of(
            Color.web("#6750a4"),
            Color.web("#006a6a"),
            Color.web("#b3261e"),
            Color.web("#386a20"),
            Color.web("#7d5260")
    );

    /// The bundled demo font resource path.
    private static final String DEMO_FONT_RESOURCE =
            "/org/glavo/m3fx/demo/fonts/AlibabaPuHuiTi-3-65-Medium.ttf";

    /// The size used only when registering the bundled demo font with JavaFX.
    private static final double DEMO_FONT_LOAD_SIZE = 12.0;

    /// Progress track heights shown in the progress demo page.
    private static final @Unmodifiable List<Double> PROGRESS_TRACK_HEIGHTS = List.of(2.0, 4.0, 6.0, 8.0, 12.0);

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

    /// The sidebar section for demo pages absent from the Material components navigation drawer.
    private static final String ADDITIONAL_DEMOS_GROUP = "Additional demos";

    /// The fixed icon viewport style used by interactive SVG icon samples.
    private static final String DEMO_VECTOR_ICON_VIEWPORT_STYLE_CLASS = "demo-vector-icon-viewport";

    /// The authored viewport used by the demo's compact SVG path set.
    private static final Rectangle2D DEMO_ICON_VIEW_BOX = new Rectangle2D(0.0, 0.0, 24.0, 24.0);

    /// A Material Symbols add path using the coordinates published by the official SVG asset.
    private static final String MATERIAL_SYMBOL_ADD_PATH =
            "M440-120v-320H120v-80h320v-320h80v320h320v80H520v320h-80Z";

    /// The viewport published with the Material Symbols add path.
    private static final Rectangle2D MATERIAL_SYMBOL_VIEW_BOX =
            new Rectangle2D(0.0, -960.0, 960.0, 960.0);

    /// The current seed color used by the demo theme.
    private Color seedColor = M3Theme.DEFAULT_SEED_COLOR;

    /// The current Material Design profile.
    private M3Profile profile = M3Profile.BASELINE_2021;

    /// The current theme brightness.
    private Brightness brightness = Brightness.LIGHT;

    /// The current density scale applied to component tokens.
    private double densityScale;

    /// Whether demo animations are enabled.
    private boolean animationsEnabled = true;

    /// Animations owned by the active demo page.
    private final List<Animation> animations = new ArrayList<>();

    /// Sidebar groups rendered by the navigation drawer.
    private final List<SidebarGroup> sidebarGroups = new ArrayList<>();

    /// Demo pages created for the current application instance.
    private @Unmodifiable List<DemoPage> pages = List.of();

    /// The active JavaFX scene.
    private @Nullable Scene scene;

    /// Whether a deferred Windows per-monitor scale repair is pending.
    private boolean windowsScaleRepairPending;

    /// The navigation drawer used by the demo sidebar.
    private @Nullable M3NavigationDrawer sidebarDrawer;

    /// The scroll pane that hosts the demo sidebar.
    private @Nullable ScrollPane sidebarScrollPane;

    /// The scroll pane that hosts the current demo page.
    private @Nullable ScrollPane pageScrollPane;

    /// The pending retry that scrolls the active sidebar destination after drawer expansion settles.
    private @Nullable Timeline sidebarScrollRetryAnimation;

    /// The currently shown demo page.
    private @Nullable DemoPage currentPage;

    /// The page host replaced when sidebar selection changes.
    private @Nullable StackPane pageHost;

    /// The stable root that owns dialogs, snackbars, and other in-scene overlays.
    private @Nullable M3OverlayPane overlayPane;

    /// Starts the demo application.
    @Override
    public void start(Stage stage) {
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");
        if (isWindows && Screen.getPrimary().getOutputScaleX() > 1) {
            System.getProperties().putIfAbsent("prism.lcdtext", "false");
        }

        BorderPane scaffold = new BorderPane();
        scaffold.getStyleClass().add("demo-root");
        M3OverlayPane root = new M3OverlayPane();
        root.setContent(scaffold);
        overlayPane = root;
        applyDemoFont(root);

        List<DemoPage> createdPages = createPages();
        pages = createdPages;

        scaffold.setTop(createHeader());
        scaffold.setCenter(createContent(createdPages));

        Scene scene = new Scene(root, 1180.0, 820.0);
        scene.getStylesheets().add(demoStylesheetUrl());
        this.scene = scene;
        applyTheme();
        applyMotionSettings();
        showPage(createdPages.get(0));

        stage.setTitle("M3FX Demo");
        stage.setMinWidth(960.0);
        stage.setMinHeight(680.0);
        stage.setScene(scene);
        stage.show();
        installWindowsScaleTransitionRepair(stage);
    }

    /// Repairs a Windows JavaFX bounds mismatch when the demo crosses monitors at its minimum size.
    ///
    /// When a stage is exactly at one of its minimum-size constraints, the Windows Glass backend can retain the
    /// previous monitor's physical bound for that axis while the JavaFX window and scene continue reporting the
    /// logical minimum. Re-submitting only the affected constraint after the asynchronous output-scale notification
    /// reconciles those bounds. Windows scale changes away from a minimum-size boundary need no application repair.
    ///
    /// @param stage the demo stage
    private void installWindowsScaleTransitionRepair(Stage stage) {
        if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
            return;
        }

        stage.outputScaleXProperty().addListener(observable -> scheduleWindowsScaleTransitionRepair(stage));
        stage.outputScaleYProperty().addListener(observable -> scheduleWindowsScaleTransitionRepair(stage));
    }

    /// Schedules one bounds refresh after the current native output-scale notification completes.
    ///
    /// @param stage the demo stage
    private void scheduleWindowsScaleTransitionRepair(Stage stage) {
        if (windowsScaleRepairPending) {
            return;
        }

        windowsScaleRepairPending = true;
        Platform.runLater(() -> {
            windowsScaleRepairPending = false;
            if (!stage.isShowing() || stage.isIconified() || stage.isMaximized() || stage.isFullScreen()) {
                return;
            }

            double minWidth = stage.getMinWidth();
            double minHeight = stage.getMinHeight();
            boolean widthAtMinimum = minWidth > 0.0 && stage.getWidth() <= minWidth + 0.5;
            boolean heightAtMinimum = minHeight > 0.0 && stage.getHeight() <= minHeight + 0.5;
            if (!widthAtMinimum && !heightAtMinimum) {
                return;
            }

            if (widthAtMinimum) {
                stage.setMinWidth(0.0);
                stage.setMinWidth(minWidth);
            }
            if (heightAtMinimum) {
                stage.setMinHeight(0.0);
                stage.setMinHeight(minHeight);
            }
            stage.setWidth(stage.getWidth());
            stage.setHeight(stage.getHeight());

            @Nullable Scene activeScene = stage.getScene();
            if (activeScene != null) {
                activeScene.getRoot().requestLayout();
            }
        });
    }

    /// Applies the bundled default font to the demo root when the resource is present.
    ///
    /// The loaded font family is read from JavaFX rather than hard-coded so the CSS uses the exact family name
    /// reported by the font file on the current runtime.
    ///
    /// @param root the demo root node
    private static void applyDemoFont(Region root) {
        @Nullable String fontFamily = loadDemoFontFamily();
        if (fontFamily == null) {
            return;
        }

        String escapedFontFamily = fontFamily.replace("\\", "\\\\").replace("\"", "\\\"");
        String fontStyle = "-fx-font-family: \"" + escapedFontFamily + "\";";
        String currentStyle = root.getStyle();
        root.setStyle(currentStyle.isBlank() ? fontStyle : currentStyle + " " + fontStyle);
    }

    /// Loads the bundled demo font and returns the JavaFX font family name.
    ///
    /// @return the loaded font family, or `null` when the resource cannot be loaded
    private static @Nullable String loadDemoFontFamily() {
        @Nullable URL fontUrl = M3FXDemoApp.class.getResource(DEMO_FONT_RESOURCE);
        if (fontUrl == null) {
            return null;
        }

        @Nullable Font font = Font.loadFont(fontUrl.toExternalForm(), DEMO_FONT_LOAD_SIZE);
        return font == null ? null : font.getFamily();
    }

    /// Creates the header with theme controls.
    private Node createHeader() {
        VBox titleBox = new VBox(2.0);
        titleBox.getStyleClass().add("demo-title-box");

        Label title = new Label("M3FX");
        title.getStyleClass().add("demo-title");
        Label subtitle = new Label("Material Design 3 controls for JavaFX");
        subtitle.getStyleClass().add("demo-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox seedButtons = new HBox(8.0);
        seedButtons.getStyleClass().add("demo-seed-buttons");
        for (Color color : SEED_COLORS) {
            M3IconButton button = new M3IconButton();
            button.getStyleClass().add("demo-seed-button");
            button.setStyle("-fx-background-color: " + toHex(color) + ";");
            button.setOnAction(event -> {
                seedColor = color;
                applyTheme();
            });
            seedButtons.getChildren().add(button);
        }

        M3Button profileButton = new M3Button(
                profile == M3Profile.BASELINE_2021 ? "Expressive" : "Baseline"
        );
        profileButton.setVariant(M3ButtonVariant.OUTLINED);
        profileButton.setOnAction(event -> {
            profile = profile == M3Profile.BASELINE_2021 ? M3Profile.EXPRESSIVE_2025 : M3Profile.BASELINE_2021;
            profileButton.setText(profile == M3Profile.BASELINE_2021 ? "Expressive" : "Baseline");
            applyTheme();
        });

        M3Button brightnessButton = new M3Button(brightness == Brightness.LIGHT ? "Dark" : "Light");
        brightnessButton.setVariant(M3ButtonVariant.TONAL);
        brightnessButton.setOnAction(event -> {
            brightness = brightness == Brightness.LIGHT ? Brightness.DARK : Brightness.LIGHT;
            brightnessButton.setText(brightness == Brightness.LIGHT ? "Dark" : "Light");
            applyTheme();
        });

        M3Button densityButton = new M3Button(densityLabel());
        densityButton.setVariant(M3ButtonVariant.OUTLINED);
        densityButton.setOnAction(event -> {
            densityScale = nextDensityScale();
            densityButton.setText(densityLabel());
            applyTheme();
        });

        M3Switch animationsSwitch = new M3Switch("Animations");
        animationsSwitch.setSelected(animationsEnabled);
        animationsSwitch.setOnAction(event -> {
            animationsEnabled = animationsSwitch.isSelected();
            applyMotionSettings();
            refreshCurrentPage();
        });

        HBox header = new HBox(
                18.0,
                titleBox,
                spacer,
                seedButtons,
                profileButton,
                densityButton,
                animationsSwitch,
                brightnessButton
        );
        header.getStyleClass().add("demo-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /// Creates all component demo pages.
    private List<DemoPage> createPages() {
        return List.of(
                new DemoPage("Components Overview", "Components overview", COMPONENTS_OVERVIEW_GROUP, "Browse the implemented Material Design 3 component demos", DemoMaterialDocs.COMPONENTS, this::createComponentsOverviewPage),
                new DemoPage("App Bars", "App bars", APP_BARS_GROUP, "Top app bars with navigation and actions", DemoMaterialDocs.APP_BARS, this::createAppBarsPage),
                new DemoPage("Badges", "Badges", "Badges", "Dot, count, overflow, and attached badges", DemoMaterialDocs.BADGES, this::createBadgesPage),
                new DemoPage("All Buttons", "All buttons", BUTTONS_GROUP, "Overview of button families and action patterns", DemoMaterialDocs.ALL_BUTTONS, this::createAllButtonsPage),
                new DemoPage("Button Groups", "Button groups", BUTTONS_GROUP, "Standard and connected groups for related actions", DemoMaterialDocs.BUTTON_GROUPS, this::createButtonGroupsPage),
                new DemoPage("Buttons", "Buttons", BUTTONS_GROUP, "Common button variants", DemoMaterialDocs.BUTTONS, this::createButtonsPage),
                new DemoPage("Extended FABs", "Extended FABs", BUTTONS_GROUP, "Extended floating action button examples", DemoMaterialDocs.EXTENDED_FAB, this::createExtendedFabsPage),
                new DemoPage("FAB Menu", "FAB menu", BUTTONS_GROUP, "Expandable floating action shortcuts", DemoMaterialDocs.FAB_MENU, this::createFabMenuPage),
                new DemoPage("Floating Action Buttons", "Floating action buttons (FABs)", BUTTONS_GROUP, "Floating action button sizes and variants", DemoMaterialDocs.FLOATING_ACTION_BUTTON, this::createFloatingActionButtonsPage),
                new DemoPage("Icon Buttons", "Icon buttons", BUTTONS_GROUP, "Icon button and toggle icon button states", DemoMaterialDocs.ICON_BUTTONS, this::createIconButtonsPage),
                new DemoPage("Segmented Buttons", "Segmented buttons", BUTTONS_GROUP, "Single- and multi-select segmented control states", DemoMaterialDocs.SEGMENTED_BUTTONS, this::createSegmentedButtonsPage),
                new DemoPage("Split Buttons", "Split buttons", BUTTONS_GROUP, "Primary actions with attached menus", DemoMaterialDocs.SPLIT_BUTTON, this::createSplitButtonsPage),
                new DemoPage("Cards", "Cards", "Cards", "Filled, outlined, elevated, and interactive cards", DemoMaterialDocs.CARDS, this::createCardsPage),
                new DemoPage("Carousel", "Carousel", "Carousel", "Adaptive visual browsing with Material keyline layouts", DemoMaterialDocs.CAROUSEL, this::createCarouselPage),
                new DemoPage("Checkboxes", "Checkbox", "Checkbox", "Checked, unchecked, indeterminate, error, and disabled states", DemoMaterialDocs.CHECKBOX, this::createCheckboxesPage),
                new DemoPage("Chips", "Chips", "Chips", "Assist, filter, input, suggestion, and disabled chips", DemoMaterialDocs.CHIPS, this::createChipsPage),
                new DemoPage("Date Pickers", "Date pickers", DATE_TIME_PICKERS_GROUP, "Calendar date selection, ranges, and month visibility", DemoMaterialDocs.DATE_PICKERS, this::createDatePickersPage),
                new DemoPage("Time Pickers", "Time pickers", DATE_TIME_PICKERS_GROUP, "12-hour, 24-hour, and bounded time selection", DemoMaterialDocs.TIME_PICKERS, this::createTimePickersPage),
                new DemoPage("Dialogs", "Dialogs", "Dialogs", "Dialog pane with themed actions", DemoMaterialDocs.DIALOGS, this::createDialogsPage),
                new DemoPage("Dividers", "Divider", "Divider", "Full-width, inset, middle inset, and vertical dividers", DemoMaterialDocs.DIVIDER, this::createDividersPage),
                new DemoPage("Lists", "Lists", "Lists", "One-line, two-line, three-line, selected, and disabled rows", DemoMaterialDocs.LISTS, this::createListPage),
                new DemoPage("Loading Indicator", "Loading indicator", LOADING_PROGRESS_GROUP, "Indeterminate loading indicators", DemoMaterialDocs.LOADING_INDICATOR, this::createLoadingIndicatorPage),
                new DemoPage("Progress", "Progress indicators", LOADING_PROGRESS_GROUP, "Linear and circular progress indicators", DemoMaterialDocs.PROGRESS_INDICATORS, this::createProgressPage),
                new DemoPage("Menus", "Menus", "Menus", "Menu surfaces, actions, and menu buttons", DemoMaterialDocs.MENUS, this::createMenusPage),
                new DemoPage("Navigation", "Navigation bar", NAVIGATION_GROUP, "Bottom navigation items and selected indicators", DemoMaterialDocs.NAVIGATION_BAR, this::createNavigationPage),
                new DemoPage("Navigation Drawer", "Navigation drawer", NAVIGATION_GROUP, "Drawer destinations with selected rows", DemoMaterialDocs.NAVIGATION_DRAWER, this::createNavigationDrawerPage),
                new DemoPage("Navigation Rail", "Navigation rail", NAVIGATION_GROUP, "Vertical destinations for wide layouts", DemoMaterialDocs.NAVIGATION_RAIL, this::createNavigationRailPage),
                new DemoPage("Radio Buttons", "Radio button", "Radio button", "Grouped single selection states", DemoMaterialDocs.RADIO_BUTTON, this::createRadioButtonsPage),
                new DemoPage("Search", "Search", "Search", "Search bars, actions, and result surfaces", DemoMaterialDocs.SEARCH, this::createSearchPage),
                new DemoPage("Bottom Sheets", "Bottom sheets", SHEETS_GROUP, "Bottom sheet containment surfaces", DemoMaterialDocs.BOTTOM_SHEETS, this::createBottomSheetsPage),
                new DemoPage("Side Sheets", "Side sheets", SHEETS_GROUP, "Side sheet containment surfaces", DemoMaterialDocs.SIDE_SHEETS, this::createSideSheetsPage),
                new DemoPage("Sliders", "Sliders", "Sliders", "Different values and disabled slider states", DemoMaterialDocs.SLIDERS, this::createSlidersPage),
                new DemoPage("Snackbars", "Snackbar", "Snackbar", "Snackbar presentation with action and queued messages", DemoMaterialDocs.SNACKBAR, this::createSnackbarsPage),
                new DemoPage("Switches", "Switch", "Switch", "On, off, and disabled switch states", DemoMaterialDocs.SWITCH, this::createSwitchesPage),
                new DemoPage("Tabs", "Tabs", "Tabs", "Primary and secondary fixed and scrollable tabs", DemoMaterialDocs.TABS, this::createTabsPage),
                new DemoPage("Text Fields", "Text fields", "Text fields", "Filled, outlined, populated, error, and disabled fields", DemoMaterialDocs.TEXT_FIELDS, this::createTextFieldsPage),
                new DemoPage("Toolbars", "Toolbars", TOOLBARS_GROUP, "Floating and docked toolbars with Standard and Vibrant colors", DemoMaterialDocs.TOOLBARS, this::createToolbarsPage),
                new DemoPage("Bottom App Bars", "Bottom app bars", TOOLBARS_GROUP, "Legacy bottom app bars with floating action alignment", DemoMaterialDocs.BOTTOM_APP_BARS, this::createBottomAppBarsPage),
                new DemoPage("Tooltips", "Tooltips", "Tooltips", "Plain and longer contextual help", DemoMaterialDocs.TOOLTIPS, this::createTooltipsPage),
                new DemoPage("Banners", "Banners", ADDITIONAL_DEMOS_GROUP, "Persistent inline feedback with optional actions", DemoMaterialDocs.BANNERS, this::createBannersPage),
                new DemoPage("Forms", "Forms", ADDITIONAL_DEMOS_GROUP, "Form rows and sections for structured input", DemoMaterialDocs.FORMS, this::createFormsPage),
                new DemoPage("Typography", "Typography", ADDITIONAL_DEMOS_GROUP, "Token-driven Material type roles", DemoMaterialDocs.TYPOGRAPHY, this::createTypographyPage),
                new DemoPage("Icons", "Icons", ADDITIONAL_DEMOS_GROUP, "Size roles and semantic icon colors", DemoMaterialDocs.ICONS, this::createIconsPage),
                new DemoPage("Avatars", "Avatars", ADDITIONAL_DEMOS_GROUP, "Initials and graphic avatar slots", DemoMaterialDocs.AVATARS, this::createAvatarsPage),
                new DemoPage("Surfaces", "Surfaces", ADDITIONAL_DEMOS_GROUP, "Color containers, shape, padding, and elevation", DemoMaterialDocs.SURFACES, this::createSurfacesPage),
                new DemoPage("Scrims", "Scrims", ADDITIONAL_DEMOS_GROUP, "Modal overlays and dismiss actions", DemoMaterialDocs.SCRIMS, this::createScrimsPage)
        );
    }

    /// Creates the main content shell with sidebar and page host.
    ///
    /// @param pages the pages shown by the sidebar and content host
    /// @return the assembled demo content shell
    private Node createContent(List<DemoPage> pages) {
        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("demo-shell");
        shell.setLeft(createSidebar(pages));
        shell.setCenter(createPageScrollPane());
        return shell;
    }

    /// Creates the component sidebar.
    private Node createSidebar(List<DemoPage> pages) {
        M3NavigationDrawer sidebar = new M3NavigationDrawer();
        sidebar.getStyleClass().add("demo-sidebar-drawer");
        sidebar.setAllowEmptySelection(true);
        sidebarDrawer = sidebar;

        sidebarGroups.clear();
        sidebarGroups.addAll(createSidebarGroups(pages));
        buildSidebarItems();

        ScrollPane scrollPane = new ScrollPane(sidebar);
        scrollPane.getStyleClass().add("demo-sidebar-scroll-pane");
        M3ScrollPanes.style(scrollPane);
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> cancelSidebarScrollRetry());
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScrollPane = scrollPane;
        return scrollPane;
    }

    /// Creates sidebar groups from ordered demo pages.
    private static List<SidebarGroup> createSidebarGroups(List<DemoPage> pages) {
        if (pages.isEmpty()) {
            return List.of();
        }

        List<SidebarGroup> groups = new ArrayList<>();
        String currentGroupTitle = pages.get(0).sidebarSection();
        List<DemoPage> currentGroupPages = new ArrayList<>();
        for (DemoPage page : pages) {
            if (!page.sidebarSection().equals(currentGroupTitle)) {
                addSidebarGroup(groups, currentGroupTitle, currentGroupPages);
                currentGroupTitle = page.sidebarSection();
                currentGroupPages = new ArrayList<>();
            }
            currentGroupPages.add(page);
        }
        addSidebarGroup(groups, currentGroupTitle, currentGroupPages);
        return groups;
    }

    /// Adds one completed sidebar group when it contains pages.
    private static void addSidebarGroup(
            List<SidebarGroup> groups,
            String title,
            List<DemoPage> pages
    ) {
        if (!pages.isEmpty()) {
            groups.add(new SidebarGroup(title, pages));
        }
    }

    /// Builds sidebar drawer items from the configured component groups.
    private void buildSidebarItems() {
        M3NavigationDrawer sidebar = sidebarDrawer;
        if (sidebar == null) {
            return;
        }

        sidebar.getItems().clear();
        for (SidebarGroup group : sidebarGroups) {
            if (group.pages.size() > 1) {
                M3NavigationDrawerGroup drawerGroup = createSidebarDrawerGroup(group);
                group.drawerGroup = drawerGroup;
                group.topLevelItem = null;
                sidebar.getItems().add(drawerGroup);
            } else {
                M3ListItem item = createSidebarPageItem(group.pages.get(0), false);
                group.topLevelItem = item;
                group.drawerGroup = null;
                sidebar.getItems().add(item);
            }
        }
        refreshSidebarSelection();
    }

    /// Creates a collapsible sidebar group control.
    private M3NavigationDrawerGroup createSidebarDrawerGroup(SidebarGroup group) {
        M3NavigationDrawerGroup drawerGroup = new M3NavigationDrawerGroup(group.title);
        drawerGroup.getHeaderItem().getStyleClass().add("demo-sidebar-group-item");
        drawerGroup.getHeaderItem().setUserData(group);
        drawerGroup.expandedProperty().addListener((observable, oldValue, newValue) -> refreshSidebarSelection());
        for (DemoPage page : group.pages) {
            drawerGroup.getItems().add(createSidebarPageItem(page, true));
        }
        return drawerGroup;
    }

    /// Creates one sidebar page destination item.
    private M3ListItem createSidebarPageItem(DemoPage page, boolean child) {
        M3ListItem item = new M3ListItem(page.navigationTitle());
        item.getStyleClass().add(child ? "demo-sidebar-child-item" : "demo-sidebar-top-item");
        item.setUserData(page);
        item.setOnAction(event -> showPage(page));
        return item;
    }

    /// Creates the scrollable page host.
    private Node createPageScrollPane() {
        StackPane host = new StackPane();
        host.getStyleClass().add("demo-page-host");
        pageHost = host;

        ScrollPane scrollPane = new ScrollPane(host);
        pageScrollPane = scrollPane;
        scrollPane.getStyleClass().add("demo-scroll-pane");
        M3ScrollPanes.style(scrollPane);
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    /// Shows a component page in the center pane.
    private void showPage(DemoPage page) {
        stopPageAnimations();
        StackPane host = pageHost;
        if (host == null) {
            return;
        }

        currentPage = page;
        expandSidebarGroupForPage(page);
        refreshSidebarSelection();

        VBox pageNode = new VBox(24.0);
        pageNode.getStyleClass().add("demo-page");

        pageNode.getChildren().addAll(createPageHeader(page), page.contentFactory().get());
        host.getChildren().setAll(pageNode);
        ScrollPane scrollPane = pageScrollPane;
        if (scrollPane != null) {
            scrollPane.setHvalue(0.0);
            scrollPane.setVvalue(0.0);
        }
        scrollSidebarPageIntoViewLater(page);
    }

    /// Creates the title, subtitle, and optional Material documentation action for a page.
    private Node createPageHeader(DemoPage page) {
        Label title = new Label(page.title());
        title.getStyleClass().add("demo-page-title");
        Label subtitle = new Label(page.subtitle());
        subtitle.getStyleClass().add("demo-page-subtitle");
        subtitle.setWrapText(true);

        VBox heading = new VBox(8.0, title, subtitle);
        heading.getStyleClass().add("demo-page-heading");
        HBox.setHgrow(heading, Priority.ALWAYS);

        HBox header = new HBox(16.0, heading);
        header.getStyleClass().add("demo-page-header");
        header.setAlignment(Pos.CENTER_LEFT);

        M3Button docsButton = new M3Button("Material docs");
        docsButton.setVariant(M3ButtonVariant.OUTLINED);
        docsButton.getStyleClass().add("demo-page-doc-link");
        docsButton.setOnAction(event -> getHostServices().showDocument(page.materialUrl()));
        header.getChildren().add(docsButton);

        return header;
    }

    /// Recreates the current page so resolved runtime settings affect active controls immediately.
    private void refreshCurrentPage() {
        DemoPage page = currentPage;
        if (page != null) {
            showPage(page);
        }
    }

    /// Returns the demo page titles created for this application instance.
    @Unmodifiable
    List<String> demoPageTitles() {
        return pages.stream().map(DemoPage::title).toList();
    }

    /// Shows the demo page with the requested title.
    void showPageByTitle(String title) {
        Objects.requireNonNull(title, "title");
        if (pages.isEmpty()) {
            throw new IllegalStateException("demo pages have not been created");
        }
        for (DemoPage page : pages) {
            if (page.title().equals(title)) {
                showPage(page);
                return;
            }
        }
        throw new IllegalArgumentException("unknown demo page title: " + title);
    }

    /// Returns the active scene.
    @Nullable Scene activeScene() {
        return scene;
    }

    /// Returns the current page's sidebar navigation title.
    @Nullable String currentPageNavigationTitle() {
        DemoPage page = currentPage;
        return page == null ? null : page.navigationTitle();
    }

    /// Returns the selected sidebar item title.
    @Nullable String selectedSidebarNavigationTitle() {
        for (SidebarGroup group : sidebarGroups) {
            @Nullable M3ListItem selectedItem = group.selectedItem();
            if (selectedItem != null) {
                return selectedItem.getHeadlineText();
            }
        }
        return null;
    }

    /// Applies a presentation mode directly for rendered demo validation.
    ///
    /// @param profile           the Material profile to install
    /// @param brightness        the theme brightness to install
    /// @param animationsEnabled whether full component motion is enabled
    void configurePresentation(M3Profile profile, Brightness brightness, boolean animationsEnabled) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.brightness = Objects.requireNonNull(brightness, "brightness");
        this.animationsEnabled = animationsEnabled;
        applyTheme();
        applyMotionSettings();
        refreshCurrentPage();
    }

    /// Expands the collapsible sidebar group containing the requested page.
    private void expandSidebarGroupForPage(DemoPage page) {
        for (SidebarGroup group : sidebarGroups) {
            M3NavigationDrawerGroup drawerGroup = group.drawerGroup;
            if (drawerGroup != null && group.pages.contains(page)) {
                drawerGroup.setExpanded(true);
                return;
            }
        }
    }

    /// Refreshes selected state on currently visible sidebar destination items.
    private void refreshSidebarSelection() {
        @Nullable DemoPage page = currentPage;
        for (SidebarGroup group : sidebarGroups) {
            group.updateSelection(page);
        }
    }

    /// Schedules scrolling the active sidebar destination after layout and disclosure motion settle.
    private void scrollSidebarPageIntoViewLater(DemoPage page) {
        cancelSidebarScrollRetry();
        Platform.runLater(() -> {
            scrollSidebarPageIntoViewIfCurrent(page);
            Platform.runLater(() -> scrollSidebarPageIntoViewIfCurrent(page));
        });

        Timeline retryAnimation = new Timeline(
                new KeyFrame(Duration.millis(120.0), event -> scrollSidebarPageIntoViewIfCurrent(page)),
                new KeyFrame(Duration.millis(260.0), event -> scrollSidebarPageIntoViewIfCurrent(page)),
                new KeyFrame(Duration.millis(420.0), event -> scrollSidebarPageIntoViewIfCurrent(page)),
                new KeyFrame(Duration.millis(680.0), event -> scrollSidebarPageIntoViewIfCurrent(page))
        );
        sidebarScrollRetryAnimation = retryAnimation;
        retryAnimation.setOnFinished(event -> {
            if (sidebarScrollRetryAnimation == retryAnimation) {
                sidebarScrollRetryAnimation = null;
            }
        });
        retryAnimation.play();
    }

    /// Cancels a pending sidebar scroll retry from a previously selected page.
    private void cancelSidebarScrollRetry() {
        Timeline retryAnimation = sidebarScrollRetryAnimation;
        if (retryAnimation != null) {
            retryAnimation.stop();
            sidebarScrollRetryAnimation = null;
        }
    }

    /// Scrolls the sidebar destination only when it still belongs to the active page.
    private void scrollSidebarPageIntoViewIfCurrent(DemoPage page) {
        if (currentPage == page) {
            scrollSidebarPageIntoView(page);
        }
    }

    /// Scrolls the active sidebar destination into view when it is outside the current viewport.
    private void scrollSidebarPageIntoView(DemoPage page) {
        ScrollPane scrollPane = sidebarScrollPane;
        Node content = scrollPane == null ? null : scrollPane.getContent();
        @Nullable M3ListItem item = sidebarItemForPage(page);
        if (scrollPane == null || content == null || item == null || item.getScene() == null) {
            return;
        }

        scrollPane.applyCss();
        content.applyCss();
        if (content instanceof Parent parent) {
            parent.layout();
        }

        Bounds viewportBounds = scrollPane.getViewportBounds();
        double viewportHeight = viewportBounds.getHeight();
        double contentHeight = scrollContentHeight(content, viewportBounds.getWidth());
        double scrollableHeight = contentHeight - viewportHeight;
        if (viewportHeight <= 0.0 || scrollableHeight <= 0.0) {
            return;
        }

        Bounds itemBounds = content.sceneToLocal(item.localToScene(item.getBoundsInLocal()));
        double visibleTop = scrollPane.getVvalue() * scrollableHeight;
        double visibleBottom = visibleTop + viewportHeight;
        double safePadding = 24.0;
        if (itemBounds.getMinY() >= visibleTop + safePadding
                && itemBounds.getMaxY() <= visibleBottom - safePadding) {
            return;
        }

        double targetTop;
        if (itemBounds.getMinY() < visibleTop + safePadding) {
            targetTop = itemBounds.getMinY() - safePadding;
        } else {
            targetTop = itemBounds.getMaxY() + safePadding - viewportHeight;
        }

        double clampedTop = Math.max(0.0, Math.min(scrollableHeight, targetTop));
        scrollPane.setVvalue(clampedTop / scrollableHeight);
    }

    /// Returns the current scroll content height, including width-dependent preferred height updates.
    private static double scrollContentHeight(Node content, double viewportWidth) {
        double height = content.getLayoutBounds().getHeight();
        if (content instanceof Region region) {
            double preferredHeight = region.prefHeight(viewportWidth > 0.0 ? viewportWidth : -1.0);
            height = Math.max(height, preferredHeight);
        }
        return height;
    }

    /// Returns the rendered sidebar destination item for a page.
    private @Nullable M3ListItem sidebarItemForPage(DemoPage page) {
        for (SidebarGroup group : sidebarGroups) {
            @Nullable M3ListItem item = group.itemForPage(page);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Stops animations owned by the previous page.
    private void stopPageAnimations() {
        for (Animation animation : animations) {
            animation.stop();
        }
        animations.clear();
    }

    /// Applies the current demo animation switch to page-owned animations.
    private void updatePageAnimations() {
        for (Animation animation : animations) {
            if (animationsEnabled) {
                animation.play();
            } else {
                animation.pause();
            }
        }
    }

    /// Creates the component overview page.
    private Node createComponentsOverviewPage() {
        M3ListPane primaryComponents = createListPane(
                createOverviewItem("App bars", "Top and bottom app bars for persistent actions."),
                createOverviewItem("Buttons", "Common actions, icon buttons, split buttons, and FABs."),
                createOverviewItem("Text fields", "Filled, outlined, validation, and supporting text patterns."),
                createOverviewItem("Selection", "Checkbox, radio button, switch, chips, and segmented controls."),
                createOverviewItem("Navigation", "Navigation bar, rail, drawer, and tabs.")
        );
        primaryComponents.getStyleClass().add("demo-overview-list");
        primaryComponents.setMaxWidth(720.0);

        M3ListPane feedbackComponents = createListPane(
                createOverviewItem("Loading & progress", "Linear and circular progress plus loading indicators."),
                createOverviewItem("Date & time pickers", "Date, range, and time selection controls."),
                createOverviewItem("Dialogs & sheets", "Dialogs, bottom sheets, side sheets, scrims, and snackbars."),
                createOverviewItem("Lists & surfaces", "Lists, cards, carousel, badges, menus, and surfaces.")
        );
        feedbackComponents.getStyleClass().add("demo-overview-list");
        feedbackComponents.setMaxWidth(720.0);

        return createGallery(
                createShowcaseGroup("Primary Components", primaryComponents),
                createShowcaseGroup("Feedback And Containers", feedbackComponents)
        );
    }

    /// Creates the all-buttons overview component page.
    private Node createAllButtonsPage() {
        M3Button disabledFilled = new M3Button("Disabled", M3ButtonVariant.FILLED);
        disabledFilled.setDisable(true);

        M3IconToggleButtonGroup toggleGroup = createIconToggleGroup(
                M3IconToggleButtonVariant.TONAL,
                "bookmark",
                "schedule",
                "notifications"
        );

        M3FabMenu fabMenu = createFabMenu();
        fabMenu.setExpanded(true);

        M3ButtonGroup actionGroup = createButtonGroup(
                new M3Button("Archive", M3ButtonVariant.TONAL),
                new M3Button("Share", M3ButtonVariant.TONAL),
                new M3Button("Edit", M3ButtonVariant.TONAL)
        );
        actionGroup.setVariant(M3ButtonGroupVariant.STANDARD);

        M3SegmentedButtonGroup segmentedGroup = createSegmentedGroup("Day", "Week", "Month");
        M3SplitButton splitButton = createSplitButton("Create", M3ButtonVariant.TONAL);

        return createGallery(
                createShowcaseGroup(
                        "Common Buttons",
                        new M3Button("Filled", M3ButtonVariant.FILLED),
                        new M3Button("Tonal", M3ButtonVariant.TONAL),
                        new M3Button("Outlined", M3ButtonVariant.OUTLINED),
                        new M3Button("Text", M3ButtonVariant.TEXT),
                        new M3Button("Elevated", M3ButtonVariant.ELEVATED),
                        disabledFilled
                ),
                createShowcaseGroup(
                        "Icon Buttons",
                        createIconButton("search"),
                        createIconButton("favorite", M3ButtonSize.MEDIUM, M3IconButtonWidth.WIDE, M3ButtonShape.ROUND),
                        toggleGroup
                ),
                createShowcaseGroup(
                        "Floating Actions",
                        createFab("add", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createExtendedFab(
                                "Create",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        fabMenu
                ),
                createShowcaseGroup(
                        "Grouped Actions",
                        actionGroup,
                        segmentedGroup,
                        splitButton
                )
        );
    }

    /// Creates the button group component page.
    private Node createButtonGroupsPage() {
        M3ButtonGroup standardGroup = createButtonGroup(
                new M3Button("Archive", M3ButtonVariant.TONAL),
                new M3Button("Share", M3ButtonVariant.TONAL),
                new M3Button("Edit", M3ButtonVariant.TONAL)
        );
        standardGroup.setVariant(M3ButtonGroupVariant.STANDARD);
        standardGroup.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup standardSingleSelect = createToggleButtonGroup(
                M3ButtonGroupVariant.STANDARD,
                false,
                "format_align_left",
                "format_align_center",
                "format_align_right"
        );
        standardSingleSelect.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup connectedSingleSelect = createToggleButtonGroup(
                M3ButtonGroupVariant.CONNECTED,
                false,
                "format_align_left",
                "format_align_center",
                "format_align_right"
        );
        connectedSingleSelect.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup connectedMultiSelect = createToggleButtonGroup(
                M3ButtonGroupVariant.CONNECTED,
                true,
                "bold",
                "italic",
                "underline"
        );
        connectedMultiSelect.setSize(M3ButtonSize.MEDIUM);

        M3ButtonGroup small = createButtonGroup(
                new M3Button("Previous", M3ButtonVariant.TONAL),
                new M3Button("Next", M3ButtonVariant.TONAL)
        );
        small.setVariant(M3ButtonGroupVariant.STANDARD);
        small.setSize(M3ButtonSize.SMALL);

        M3ButtonGroup large = createButtonGroup(
                new M3Button("Decline", M3ButtonVariant.OUTLINED),
                new M3Button("Accept", M3ButtonVariant.FILLED)
        );
        large.setVariant(M3ButtonGroupVariant.STANDARD);
        large.setSize(M3ButtonSize.LARGE);

        return createGallery(
                createShowcaseGroup("Standard Actions", standardGroup),
                createShowcaseGroup("Standard Toggle Selection", standardSingleSelect),
                createFullWidthShowcaseGroup("Connected Single Select", connectedSingleSelect),
                createFullWidthShowcaseGroup("Connected Multi Select", connectedMultiSelect),
                createShowcaseGroup("Size Scale", small, large)
        );
    }

    /// Creates the button component page.
    private Node createButtonsPage() {
        M3Button disabledFilled = new M3Button("Disabled", M3ButtonVariant.FILLED);
        disabledFilled.setDisable(true);

        M3Button mediumWithIcon = createSizedButton(
                "Create",
                M3ButtonVariant.FILLED,
                M3ButtonSize.MEDIUM,
                M3ButtonShape.ROUND
        );
        mediumWithIcon.setGraphic(createIconViewport(
                DemoIcons.onPrimary("add"),
                defaultButtonGlyphSize(M3ButtonSize.MEDIUM)
        ));

        M3Button largeSquareWithIcon = createSizedButton(
                "Launch",
                M3ButtonVariant.TONAL,
                M3ButtonSize.LARGE,
                M3ButtonShape.SQUARE
        );
        largeSquareWithIcon.setGraphic(createIconViewport(
                DemoIcons.onSecondaryContainer("send"),
                defaultButtonGlyphSize(M3ButtonSize.LARGE)
        ));

        return createGallery(
                createShowcaseGroup(
                        "Button Variants",
                        new M3Button("Filled", M3ButtonVariant.FILLED),
                        new M3Button("Tonal", M3ButtonVariant.TONAL),
                        new M3Button("Outlined", M3ButtonVariant.OUTLINED),
                        new M3Button("Text", M3ButtonVariant.TEXT),
                        new M3Button("Elevated", M3ButtonVariant.ELEVATED),
                        disabledFilled
                ),
                createShowcaseGroup(
                        "Button Sizes",
                        createSizedButton("XS", M3ButtonVariant.FILLED, M3ButtonSize.EXTRA_SMALL, M3ButtonShape.ROUND),
                        createSizedButton("Small", M3ButtonVariant.FILLED, M3ButtonSize.SMALL, M3ButtonShape.ROUND),
                        createSizedButton("Medium", M3ButtonVariant.TONAL, M3ButtonSize.MEDIUM, M3ButtonShape.ROUND),
                        createSizedButton("Large", M3ButtonVariant.TONAL, M3ButtonSize.LARGE, M3ButtonShape.ROUND),
                        createSizedButton("XL", M3ButtonVariant.OUTLINED, M3ButtonSize.EXTRA_LARGE, M3ButtonShape.ROUND)
                ),
                createShowcaseGroup(
                        "Button Shapes",
                        createSizedButton("Round", M3ButtonVariant.FILLED, M3ButtonSize.SMALL, M3ButtonShape.ROUND),
                        createSizedButton("Square", M3ButtonVariant.FILLED, M3ButtonSize.SMALL, M3ButtonShape.SQUARE),
                        createSizedButton("Medium square", M3ButtonVariant.OUTLINED, M3ButtonSize.MEDIUM, M3ButtonShape.SQUARE),
                        createSizedButton("Large square", M3ButtonVariant.TONAL, M3ButtonSize.LARGE, M3ButtonShape.SQUARE)
                ),
                createShowcaseGroup(
                        "Buttons With Icons",
                        mediumWithIcon,
                        largeSquareWithIcon
                )
        );
    }

    /// Creates the extended floating action button component page.
    private Node createExtendedFabsPage() {
        return createGallery(
                createShowcaseGroup(
                        "Expressive Sizes",
                        createExtendedFab(
                                "Create",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Compose",
                                "edit",
                                M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                                M3FloatingActionButtonSize.MEDIUM
                        ),
                        createExtendedFab(
                                "Favorite",
                                "favorite",
                                M3FloatingActionButtonVariant.TERTIARY_CONTAINER,
                                M3FloatingActionButtonSize.LARGE
                        )
                ),
                createShowcaseGroup(
                        "Tonal Colors",
                        createExtendedFab(
                                "Primary",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Secondary",
                                "upload",
                                M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Tertiary",
                                "favorite",
                                M3FloatingActionButtonVariant.TERTIARY_CONTAINER,
                                M3FloatingActionButtonSize.REGULAR
                        )
                ),
                createShowcaseGroup(
                        "Solid Colors",
                        createExtendedFab(
                                "Primary",
                                "add",
                                M3FloatingActionButtonVariant.PRIMARY,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Secondary",
                                "upload",
                                M3FloatingActionButtonVariant.SECONDARY,
                                M3FloatingActionButtonSize.REGULAR
                        ),
                        createExtendedFab(
                                "Tertiary",
                                "favorite",
                                M3FloatingActionButtonVariant.TERTIARY,
                                M3FloatingActionButtonSize.REGULAR
                        )
                ),
                createShowcaseGroup(
                        "Baseline Surface",
                        createExtendedFab(
                                "Surface",
                                "edit",
                                M3FloatingActionButtonVariant.SURFACE,
                                M3FloatingActionButtonSize.REGULAR
                        )
                )
        );
    }

    /// Creates the floating action button menu component page.
    private Node createFabMenuPage() {
        M3FabMenu expanded = createFabMenu();
        expanded.setExpanded(true);

        M3FabMenu collapsed = createFabMenu();
        M3FabMenu secondary = createFabMenu(
                M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                M3FloatingActionButtonVariant.SECONDARY_CONTAINER
        );
        secondary.setExpanded(true);
        M3FabMenu tertiary = createFabMenu(
                M3FloatingActionButtonVariant.TERTIARY_CONTAINER,
                M3FloatingActionButtonVariant.TERTIARY_CONTAINER
        );
        tertiary.setExpanded(true);

        return createGallery(
                createShowcaseGroup("Expanded", expanded),
                createShowcaseGroup("Collapsed", collapsed),
                createShowcaseGroup("Color Families", secondary, tertiary)
        );
    }

    /// Creates the floating action button component page.
    private Node createFloatingActionButtonsPage() {
        return createGallery(
                createShowcaseGroup(
                        "Sizes With Icons",
                        createFab("add", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.SMALL),
                        createFab("edit", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createFab("share", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.MEDIUM),
                        createFab("navigation", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.LARGE)
                ),
                createShowcaseGroup(
                        "Tonal Colors",
                        createFab("add", M3FloatingActionButtonVariant.SURFACE, M3FloatingActionButtonSize.REGULAR),
                        createFab("edit", M3FloatingActionButtonVariant.PRIMARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createFab("share", M3FloatingActionButtonVariant.SECONDARY_CONTAINER, M3FloatingActionButtonSize.REGULAR),
                        createFab("favorite", M3FloatingActionButtonVariant.TERTIARY_CONTAINER, M3FloatingActionButtonSize.REGULAR)
                ),
                createShowcaseGroup(
                        "Solid Colors",
                        createFab("edit", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR),
                        createFab("share", M3FloatingActionButtonVariant.SECONDARY, M3FloatingActionButtonSize.REGULAR),
                        createFab("favorite", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.REGULAR)
                )
        );
    }

    /// Creates the icon button component page.
    private Node createIconButtonsPage() {
        M3IconButton disabledIcon = createIconButton("info");
        disabledIcon.setDisable(true);

        return createGallery(
                createShowcaseGroup(
                        "Icon Buttons",
                        createIconButton("info"),
                        createIconButton("add"),
                        disabledIcon
                ),
                createShowcaseGroup(
                        "Icon Button Sizes",
                        createIconButton("star", M3ButtonSize.EXTRA_SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.LARGE, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("star", M3ButtonSize.EXTRA_LARGE, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND)
                ),
                createShowcaseGroup(
                        "Icon Button Widths",
                        createIconButton("search", M3ButtonSize.MEDIUM, M3IconButtonWidth.NARROW, M3ButtonShape.ROUND),
                        createIconButton("search", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("search", M3ButtonSize.MEDIUM, M3IconButtonWidth.WIDE, M3ButtonShape.ROUND)
                ),
                createShowcaseGroup(
                        "Icon Button Shapes",
                        createIconButton("favorite", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createIconButton("favorite", M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.SQUARE),
                        createIconButton("favorite", M3ButtonSize.LARGE, M3IconButtonWidth.DEFAULT, M3ButtonShape.SQUARE)
                ),
                createShowcaseGroup(
                        "Toggle Icon Buttons",
                        createIconToggleGroup(
                                M3IconToggleButtonVariant.STANDARD,
                                "star",
                                "favorite",
                                "tune",
                                "visibility"
                        ),
                        createIconToggleGroup(
                                M3IconToggleButtonVariant.TONAL,
                                "bookmark",
                                "schedule",
                                "notifications"
                        ),
                        createFormattingToggleGroup(),
                        createIconToggleButton("delete", M3IconToggleButtonVariant.TONAL, false)
                ),
                createShowcaseGroup(
                        "Toggle Size And Shape",
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.EXTRA_SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.SMALL, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.MEDIUM, M3IconButtonWidth.DEFAULT, M3ButtonShape.ROUND),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.LARGE, M3IconButtonWidth.WIDE, M3ButtonShape.SQUARE),
                        createExpressiveFavoriteToggleButton(
                                M3ButtonSize.EXTRA_LARGE, M3IconButtonWidth.WIDE, M3ButtonShape.SQUARE)
                )
        );
    }

    /// Creates the typography component page.
    private Node createTypographyPage() {
        return createGallery(
                createShowcaseGroup(
                        "Display",
                        new M3Text("Display Large", M3TextRole.DISPLAY_LARGE),
                        new M3Text("Display Medium", M3TextRole.DISPLAY_MEDIUM),
                        new M3Text("Display Small", M3TextRole.DISPLAY_SMALL)
                ),
                createShowcaseGroup(
                        "Headline",
                        new M3Text("Headline Large", M3TextRole.HEADLINE_LARGE),
                        new M3Text("Headline Medium", M3TextRole.HEADLINE_MEDIUM),
                        new M3Text("Headline Small", M3TextRole.HEADLINE_SMALL)
                ),
                createShowcaseGroup(
                        "Title",
                        new M3Text("Title Large", M3TextRole.TITLE_LARGE),
                        new M3Text("Title Medium", M3TextRole.TITLE_MEDIUM),
                        new M3Text("Title Small", M3TextRole.TITLE_SMALL)
                ),
                createShowcaseGroup(
                        "Body",
                        new M3Text("Body Large text follows the active theme typography tokens.", M3TextRole.BODY_LARGE),
                        new M3Text("Body Medium text", M3TextRole.BODY_MEDIUM),
                        new M3Text("Body Small text", M3TextRole.BODY_SMALL)
                ),
                createShowcaseGroup(
                        "Label",
                        new M3Text("Label Large", M3TextRole.LABEL_LARGE),
                        new M3Text("Label Medium", M3TextRole.LABEL_MEDIUM),
                        new M3Text("Label Small", M3TextRole.LABEL_SMALL)
                )
        );
    }

    /// Creates the icon component page.
    private Node createIconsPage() {
        Node disabledIcon = createDemoIcon("notifications", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT);
        disabledIcon.setDisable(true);
        M3IconButton svgIconButton = new M3IconButton(
                createMaterialSymbolIcon(M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT)
        );
        M3IconToggleButton svgToggleButton = new M3IconToggleButton(
                createDemoIcon("favorite", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT)
        );
        svgToggleButton.setVariant(M3IconToggleButtonVariant.TONAL);
        svgToggleButton.setSelected(true);

        return createGallery(
                createShowcaseGroup(
                        "Sizes",
                        createDemoIcon("search", M3IconSize.SMALL, M3IconVariant.PRIMARY),
                        createDemoIcon("search", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                        createDemoIcon("search", M3IconSize.LARGE, M3IconVariant.PRIMARY),
                        createDemoIcon("search", M3IconSize.EXTRA_LARGE, M3IconVariant.PRIMARY)
                ),
                createShowcaseGroup(
                        "Color Variants",
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.SECONDARY),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.TERTIARY),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.ERROR),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE),
                        disabledIcon
                ),
                createShowcaseGroup(
                        "Source Viewports",
                        createDemoIcon("search", M3IconSize.LARGE, M3IconVariant.PRIMARY),
                        createMaterialSymbolIcon(M3IconSize.LARGE, M3IconVariant.PRIMARY)
                ),
                createShowcaseGroup(
                        "Button Usage",
                        createIconButton("info"),
                        svgIconButton,
                        svgToggleButton,
                        createFab("add", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.SMALL),
                        createFab("spark", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.REGULAR)
                )
        );
    }

    /// Creates the text field component page.
    private Node createTextFieldsPage() {
        M3TextField filled = createTextField("Filled text field", "", M3TextInputVariant.FILLED, false);
        M3TextField filledText = createTextField("Filled with text", "support@example.com", M3TextInputVariant.FILLED, false);
        filledText.setPrefWidth(340.0);
        M3TextField filledDisabled = createTextField("Disabled filled", "Read only", M3TextInputVariant.FILLED, true);
        M3TextField outlined = createTextField("Outlined text field", "", M3TextInputVariant.OUTLINED, false);
        M3TextField outlinedDisabled =
                createTextField("Disabled outlined", "Read only", M3TextInputVariant.OUTLINED, true);
        M3TextField outlinedText = createTextField("Outlined with text", "M3FX", M3TextInputVariant.OUTLINED, false);
        outlinedText.setPrefWidth(320.0);
        M3PasswordField password = new M3PasswordField("");
        password.setVariant(M3TextInputVariant.OUTLINED);
        password.setPromptText("Password");
        password.setPrefWidth(320.0);
        M3TextField filledError = createTextField("Filled error", "Invalid value", M3TextInputVariant.FILLED, false);
        filledError.setError(true);
        M3TextField outlinedError = createTextField("Outlined error", "", M3TextInputVariant.OUTLINED, false);
        outlinedError.setError(true);
        M3PasswordField passwordError = new M3PasswordField("");
        passwordError.setVariant(M3TextInputVariant.OUTLINED);
        passwordError.setPromptText("Password error");
        passwordError.setError(true);
        passwordError.setPrefWidth(280.0);
        M3TextArea filledArea = createTextArea(
                "Filled text area",
                "Write longer notes across multiple lines.",
                M3TextInputVariant.FILLED
        );
        M3TextArea outlinedArea = createTextArea(
                "Outlined text area",
                "Material text areas share field colors but keep multi-line height tokens.",
                M3TextInputVariant.OUTLINED
        );
        M3TextArea areaError = createTextArea(
                "Text area error",
                "This content needs review.",
                M3TextInputVariant.FILLED
        );
        areaError.setError(true);
        M3TextField rtlFilled = createTextField("RTL filled", "rtl@example.com", M3TextInputVariant.FILLED, false);
        rtlFilled.setPrefWidth(320.0);
        M3TextField rtlOutlined = createTextField("RTL outlined", "M3FX RTL", M3TextInputVariant.OUTLINED, false);
        rtlOutlined.setPrefWidth(320.0);
        M3TextField rtlOutlinedAction = createTextField(
                "RTL outlined action",
                "M3FX RTL Action",
                M3TextInputVariant.OUTLINED,
                false
        );
        rtlOutlinedAction.setPrefWidth(360.0);
        M3PasswordField rtlPassword = new M3PasswordField("");
        rtlPassword.setVariant(M3TextInputVariant.OUTLINED);
        rtlPassword.setPromptText("RTL password");
        rtlPassword.setPrefWidth(320.0);
        M3TextArea rtlOutlinedArea = createTextArea(
                "RTL outlined text area",
                "M3FX RTL multiline content keeps visual order.",
                M3TextInputVariant.OUTLINED
        );
        rtlOutlinedArea.setPrefWidth(380.0);

        M3TextInputLayout filledLayout = createTextInputLayout(filled, "Supporting text");
        M3TextInputLayout filledTextLayout = createTextInputLayout(filledText, "Email address");
        filledTextLayout.setLeading(createSurfaceVariantIcon("email"));
        filledTextLayout.setClearButtonEnabled(true);
        filledTextLayout.setCharacterCounterVisible(true);
        filledTextLayout.setCharacterLimit(32);
        M3TextInputLayout filledDisabledLayout = createTextInputLayout(filledDisabled, "Disabled supporting text");
        filledDisabledLayout.setLeading(createSurfaceVariantIcon("lock"));
        M3TextInputLayout outlinedLayout = createTextInputLayout(outlined, "Outlined supporting text");
        M3TextInputLayout outlinedDisabledLayout =
                createTextInputLayout(outlinedDisabled, "Disabled outlined supporting text");
        outlinedDisabledLayout.setLeading(createSurfaceVariantIcon("lock"));
        M3TextInputLayout outlinedTextLayout = createTextInputLayout(outlinedText, "Project name");
        outlinedTextLayout.setLeading(createSurfaceVariantIcon("text"));
        outlinedTextLayout.setCharacterCounterVisible(true);
        outlinedTextLayout.setCharacterLimit(24);
        outlinedTextLayout.setCharacterLimitEnforced(true);
        M3TextInputLayout passwordLayout = createTextInputLayout(password, "At least 8 characters");
        passwordLayout.setTrailing(createIconButton("visibility"));
        M3TextField validatedEmail = createTextField("Validated email", "support", M3TextInputVariant.OUTLINED, false);
        validatedEmail.setPrefWidth(340.0);
        M3TextInputLayout validatedEmailLayout = createTextInputLayout(validatedEmail, "Validation runs on focus loss");
        validatedEmailLayout.setValidator(M3TextInputValidators.required("Email is required"));
        validatedEmailLayout.getValidators().add(M3TextInputValidators.pattern(
                Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                "Use an email address"
        ));
        validatedEmailLayout.validate();
        M3TextField requiredProject = createTextField("Required project", "", M3TextInputVariant.FILLED, false);
        M3TextInputLayout requiredProjectLayout = createTextInputLayout(requiredProject, "Required field");
        requiredProjectLayout.setValidator(M3TextInputValidators.required("Project name is required"));
        requiredProjectLayout.setValidateOnTextChange(true);
        M3TextInputLayout filledErrorLayout = createTextInputLayout(filledError, "Supporting text");
        filledErrorLayout.setErrorText("Use a valid value");
        M3TextInputLayout outlinedErrorLayout = createTextInputLayout(outlinedError, "Supporting text");
        outlinedErrorLayout.setLeading(createErrorIcon("error"));
        outlinedErrorLayout.setErrorText("This field is required");
        M3TextInputLayout passwordErrorLayout = createTextInputLayout(passwordError, "Supporting text");
        passwordErrorLayout.setErrorText("Password cannot be empty");
        M3TextInputLayout filledAreaLayout = createTextInputLayout(filledArea, "Filled multi-line input");
        M3TextInputLayout outlinedAreaLayout = createTextInputLayout(outlinedArea, "Outlined multi-line input");
        outlinedAreaLayout.setCharacterCounterVisible(true);
        outlinedAreaLayout.setCharacterLimit(96);
        M3TextInputLayout areaErrorLayout = createTextInputLayout(areaError, "Supporting text");
        areaErrorLayout.setErrorText("Review this text before continuing");
        M3TextInputLayout rtlFilledLayout = createTextInputLayout(rtlFilled, "RTL email address");
        rtlFilledLayout.setLeading(createSurfaceVariantIcon("email"));
        rtlFilledLayout.setClearButtonEnabled(true);
        rtlFilledLayout.setCharacterCounterVisible(true);
        rtlFilledLayout.setCharacterLimit(32);
        rtlFilledLayout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        M3TextInputLayout rtlOutlinedLayout = createTextInputLayout(rtlOutlined, "RTL project name");
        rtlOutlinedLayout.setLeading(createSurfaceVariantIcon("text"));
        rtlOutlinedLayout.setCharacterCounterVisible(true);
        rtlOutlinedLayout.setCharacterLimit(24);
        rtlOutlinedLayout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        M3TextInputLayout rtlOutlinedActionLayout = createTextInputLayout(rtlOutlinedAction, "RTL action target");
        rtlOutlinedActionLayout.setTrailing(createIconButton("visibility"));
        rtlOutlinedActionLayout.setCharacterCounterVisible(true);
        rtlOutlinedActionLayout.setCharacterLimit(32);
        rtlOutlinedActionLayout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        M3TextInputLayout rtlPasswordLayout = createTextInputLayout(rtlPassword, "RTL at least 8 characters");
        rtlPasswordLayout.setTrailing(createIconButton("visibility"));
        rtlPasswordLayout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        M3TextInputLayout rtlOutlinedAreaLayout = createTextInputLayout(rtlOutlinedArea, "RTL multi-line input");
        rtlOutlinedAreaLayout.setCharacterCounterVisible(true);
        rtlOutlinedAreaLayout.setCharacterLimit(96);
        rtlOutlinedAreaLayout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        return createGallery(
                createShowcaseGroup("Filled", filledLayout, filledTextLayout),
                createShowcaseGroup("Outlined", outlinedLayout, outlinedTextLayout, passwordLayout),
                createShowcaseGroup("Disabled", filledDisabledLayout, outlinedDisabledLayout),
                createShowcaseGroup("Validation", validatedEmailLayout, requiredProjectLayout),
                createShowcaseGroup("Error", filledErrorLayout, outlinedErrorLayout, passwordErrorLayout, areaErrorLayout),
                createShowcaseGroup("Text Areas", filledAreaLayout, outlinedAreaLayout),
                createShowcaseGroup(
                        "Right-to-left",
                        rtlFilledLayout,
                        rtlOutlinedLayout,
                        rtlOutlinedActionLayout,
                        rtlPasswordLayout,
                        rtlOutlinedAreaLayout
                )
        );
    }

    /// Creates the search component page.
    private Node createSearchPage() {
        M3SearchBar searchBar = new M3SearchBar("Search M3FX");
        searchBar.setPrefWidth(420.0);
        M3IconButton clearSearchBar = createIconButton("close");
        clearSearchBar.setOnAction(event -> searchBar.clear());
        searchBar.getTrailingActions().add(clearSearchBar);

        M3SearchBar populated = new M3SearchBar("Search M3FX");
        populated.setText("Buttons");
        populated.setPrefWidth(420.0);

        M3SearchBar rtlSearchBar = new M3SearchBar("RTL search");
        rtlSearchBar.setText("M3FX");
        rtlSearchBar.setPrefWidth(420.0);
        rtlSearchBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        rtlSearchBar.getTrailingActions().add(createIconButton("tune"));

        M3SearchView containedDocked = new M3SearchView("Search components");
        containedDocked.setPrefWidth(520.0);
        M3IconButton tuneSearchView = createIconButton("tune");
        M3IconButton clearSearchView = createIconButton("close");
        clearSearchView.setOnAction(event -> containedDocked.clear());
        containedDocked.getTrailingActions().addAll(tuneSearchView, clearSearchView);
        containedDocked.getResults().addAll(
                createSearchResult("Buttons", "Filled, tonal, outlined, text, and elevated variants"),
                createSearchResult("Menus", "Menu surfaces, selected rows, and menu buttons"),
                createSearchResult("Navigation", "Bars, rails, drawers, and destination items")
        );

        M3SearchView dividedDocked = new M3SearchView("Search divided results");
        dividedDocked.setViewStyle(M3SearchViewStyle.DIVIDED);
        dividedDocked.setPrefWidth(520.0);
        dividedDocked.getResults().addAll(
                createSearchResult("Color", "Dynamic color and component color roles"),
                createSearchResult("Typography", "Material type roles and font metrics")
        );

        M3SearchView containedFullScreen = new M3SearchView("Search full-screen content");
        containedFullScreen.setViewLayout(M3SearchViewLayout.FULL_SCREEN);
        containedFullScreen.setPrefWidth(520.0);
        containedFullScreen.getResults().addAll(
                createSearchResult("Cards", "Elevated, filled, and outlined cards"),
                createSearchResult("Dialogs", "Basic and full-screen dialogs")
        );

        M3SearchView dividedFullScreen = new M3SearchView("Search full-screen divided content");
        dividedFullScreen.setViewStyle(M3SearchViewStyle.DIVIDED);
        dividedFullScreen.setViewLayout(M3SearchViewLayout.FULL_SCREEN);
        dividedFullScreen.setPrefWidth(520.0);
        dividedFullScreen.getTrailingActions().add(createIconButton("tune"));
        dividedFullScreen.getResults().addAll(
                createSearchResult("Date pickers", "Docked and modal date selection"),
                createSearchResult("Time pickers", "Dial and keyboard time entry")
        );

        M3SearchView rtlSearchView = new M3SearchView("RTL components");
        rtlSearchView.setPrefWidth(520.0);
        rtlSearchView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        rtlSearchView.getTrailingActions().add(createIconButton("tune"));
        rtlSearchView.getResults().addAll(
                createSearchResult("RTL Buttons", "Mirrored leading and trailing actions"),
                createSearchResult("RTL Menus", "Mirrored result-row geometry")
        );

        return createGallery(
                createShowcaseGroup("Search Bars", searchBar, populated, rtlSearchBar),
                createShowcaseGroup("Contained Docked", containedDocked),
                createShowcaseGroup("Divided Docked", dividedDocked),
                createShowcaseGroup("Contained Full-screen", containedFullScreen),
                createShowcaseGroup("Divided Full-screen", dividedFullScreen),
                createShowcaseGroup("Right-to-left", rtlSearchView)
        );
    }

    /// Creates the checkbox component page.
    private Node createCheckboxesPage() {
        M3CheckBox unchecked = createCheckBox("Unchecked", false, false, false, false, false);
        M3CheckBox checked = createCheckBox("Checked", true, false, false, false, false);
        M3CheckBox indeterminate = createCheckBox("Indeterminate", false, true, true, false, false);
        M3CheckBox threeState = createCheckBox("Three-state cycle", false, false, true, false, false);

        M3CheckBox errorUnchecked = createCheckBox("Error unchecked", false, false, false, true, false);
        M3CheckBox errorChecked = createCheckBox("Error checked", true, false, false, true, false);
        M3CheckBox errorIndeterminate = createCheckBox("Error indeterminate", false, true, false, true, false);

        M3CheckBox disabledUnchecked = createCheckBox("Disabled unchecked", false, false, false, false, true);
        M3CheckBox disabledChecked = createCheckBox("Disabled checked", true, false, false, false, true);
        M3CheckBox disabledIndeterminate =
                createCheckBox("Disabled indeterminate", false, true, false, false, true);

        return createGallery(
                createShowcaseGroup("Interactive States", unchecked, checked, indeterminate, threeState),
                createShowcaseGroup("Error States", errorUnchecked, errorChecked, errorIndeterminate),
                createShowcaseGroup("Disabled States", disabledUnchecked, disabledChecked, disabledIndeterminate)
        );
    }

    /// Creates the radio button component page.
    private Node createRadioButtonsPage() {
        ToggleGroup radioGroup = new ToggleGroup();
        M3RadioButton radioOne = new M3RadioButton("Radio A");
        radioOne.setSelected(true);
        M3RadioButton radioTwo = new M3RadioButton("Radio B");
        M3RadioButton disabledUnchecked = new M3RadioButton("Disabled unchecked");
        M3RadioButton disabledSelected = new M3RadioButton("Disabled selected");
        disabledSelected.setSelected(true);
        radioOne.setToggleGroup(radioGroup);
        radioTwo.setToggleGroup(radioGroup);
        disabledUnchecked.setDisable(true);
        disabledSelected.setDisable(true);

        return createGallery(
                createShowcaseGroup("Selection Group", radioOne, radioTwo),
                createShowcaseGroup("Disabled States", disabledUnchecked, disabledSelected)
        );
    }

    /// Creates the switch component page.
    private Node createSwitchesPage() {
        M3Switch onSwitch = new M3Switch("On");
        onSwitch.setSelected(true);
        M3Switch offSwitch = new M3Switch("Off");
        M3Switch disabledOffSwitch = new M3Switch("Disabled off");
        M3Switch disabledOnSwitch = new M3Switch("Disabled on");
        disabledOnSwitch.setSelected(true);
        disabledOffSwitch.setDisable(true);
        disabledOnSwitch.setDisable(true);

        M3Switch disabledIconOff = new M3Switch("Disabled icon off");
        disabledIconOff.setUnselectedIcon(createSwitchHandleIcon("close"));
        disabledIconOff.setDisable(true);
        M3Switch disabledIconOn = new M3Switch("Disabled icon on");
        disabledIconOn.setSelectedIcon(createSwitchHandleIcon("check"));
        disabledIconOn.setSelected(true);
        disabledIconOn.setDisable(true);

        M3Switch selectedIconOff = new M3Switch("Selected icon off");
        selectedIconOff.setSelectedIcon(createSwitchHandleIcon("check"));
        M3Switch selectedIconOn = new M3Switch("Selected icon on");
        selectedIconOn.setSelectedIcon(createSwitchHandleIcon("check"));
        selectedIconOn.setSelected(true);

        M3Switch bothIconsOff = new M3Switch("Both icons off");
        bothIconsOff.setSelectedIcon(createSwitchHandleIcon("check"));
        bothIconsOff.setUnselectedIcon(createSwitchHandleIcon("close"));
        M3Switch bothIconsOn = new M3Switch("Both icons on");
        bothIconsOn.setSelectedIcon(createSwitchHandleIcon("check"));
        bothIconsOn.setUnselectedIcon(createSwitchHandleIcon("close"));
        bothIconsOn.setSelected(true);

        return createGallery(
                createShowcaseGroup("Interactive States", onSwitch, offSwitch),
                createShowcaseGroup(
                        "Handle Icons",
                        selectedIconOff,
                        selectedIconOn,
                        bothIconsOff,
                        bothIconsOn
                ),
                createShowcaseGroup(
                        "Disabled States",
                        disabledOffSwitch,
                        disabledOnSwitch,
                        disabledIconOff,
                        disabledIconOn
                )
        );
    }

    /// Creates the slider component page.
    private Node createSlidersPage() {
        M3RangeSlider continuousRange = new M3RangeSlider(0.0, 100.0, 20.0, 78.0);
        continuousRange.setPrefWidth(300.0);

        M3RangeSlider discreteRange = new M3RangeSlider(0.0, 100.0, 30.0, 70.0);
        discreteRange.setStepSize(10.0);
        discreteRange.setSize(M3SliderSize.SMALL);
        discreteRange.setPrefWidth(300.0);

        M3RangeSlider indicatorRange = new M3RangeSlider(0.0, 100.0, 25.0, 65.0);
        indicatorRange.setShowValueIndicator(true);
        indicatorRange.setPrefWidth(300.0);

        M3RangeSlider disabledRange = new M3RangeSlider(0.0, 100.0, 35.0, 85.0);
        disabledRange.setDisable(true);
        disabledRange.setPrefWidth(300.0);

        M3Slider vertical = createSlider(48.0, false);
        vertical.setOrientation(Orientation.VERTICAL);
        vertical.setPrefSize(56.0, 180.0);

        M3Slider centeredNegative = new M3Slider(-100.0, 100.0, -45.0);
        centeredNegative.setCentered(true);
        centeredNegative.setPrefWidth(260.0);
        M3Slider centeredNeutral = new M3Slider(-100.0, 100.0, 0.0);
        centeredNeutral.setCentered(true);
        centeredNeutral.setPrefWidth(260.0);
        M3Slider centeredPositive = new M3Slider(-100.0, 100.0, 60.0);
        centeredPositive.setCentered(true);
        centeredPositive.setStepSize(20.0);
        centeredPositive.setPrefWidth(260.0);

        M3Slider centeredVertical = new M3Slider(-100.0, 100.0, -40.0);
        centeredVertical.setCentered(true);
        centeredVertical.setOrientation(Orientation.VERTICAL);
        centeredVertical.setPrefSize(56.0, 180.0);

        VBox sizeSamples = new VBox(12.0);
        M3SliderSize[] sizes = M3SliderSize.values();
        String[] sizeLabels = {"XS · 16 dp", "S · 24 dp", "M · 40 dp", "L · 56 dp", "XL · 96 dp"};
        for (int index = 0; index < sizes.length; index++) {
            M3Slider slider = createSlider(20.0 + index * 15.0, false);
            slider.setSize(sizes[index]);
            slider.setPrefWidth(360.0);
            if (index >= M3SliderSize.MEDIUM.ordinal()) {
                SVGPath activeIcon = DemoIcons.onPrimary("visibility");
                SVGPath inactiveIcon = DemoIcons.onSecondaryContainer("visibility");
                if (sizes[index] == M3SliderSize.EXTRA_LARGE) {
                    activeIcon.setScaleX(4.0 / 3.0);
                    activeIcon.setScaleY(4.0 / 3.0);
                    inactiveIcon.setScaleX(4.0 / 3.0);
                    inactiveIcon.setScaleY(4.0 / 3.0);
                }
                slider.setActiveTrackGraphic(activeIcon);
                slider.setInactiveTrackGraphic(inactiveIcon);
            }

            M3Text sizeLabel = new M3Text(sizeLabels[index], M3TextRole.LABEL_LARGE);
            sizeLabel.setMinWidth(88.0);
            sizeLabel.setPrefWidth(88.0);
            HBox row = new HBox(16.0, sizeLabel, slider);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(slider, Priority.ALWAYS);
            sizeSamples.getChildren().add(row);
        }

        M3Slider valueIndicator = createSlider(50.0, false);
        valueIndicator.setStepSize(10.0);
        valueIndicator.setShowValueIndicator(true);

        return createGallery(
                createShowcaseGroup(
                        "Continuous",
                        createSlider(24.0, false),
                        createSlider(64.0, false),
                        createSlider(50.0, true)
                ),
                createShowcaseGroup(
                        "Discrete",
                        createSteppedSlider(30.0, 10.0),
                        createSteppedSlider(70.0, 5.0)
                ),
                createShowcaseGroup(
                        "Centered",
                        centeredNegative,
                        centeredNeutral,
                        centeredPositive
                ),
                createShowcaseGroup(
                        "Range",
                        continuousRange,
                        discreteRange,
                        indicatorRange,
                        disabledRange
                ),
                createFullWidthShowcaseGroup("Expressive Sizes", sizeSamples),
                createShowcaseGroup("Value Indicator", valueIndicator),
                createShowcaseGroup("Vertical", vertical, centeredVertical)
        );
    }

    /// Creates the chip component page.
    private Node createChipsPage() {
        M3AssistChip assistDirections = new M3AssistChip("Directions");
        assistDirections.setGraphic(createNavigationIcon("navigation"));
        M3AssistChip assistCalendar = new M3AssistChip("Add to calendar");
        assistCalendar.setGraphic(createNavigationIcon("calendar"));
        assistCalendar.setChipStyle(M3ChipStyle.ELEVATED);
        M3AssistChip assistDisabled = new M3AssistChip("Disabled");
        assistDisabled.setDisable(true);

        M3FilterChip filterAll = new M3FilterChip("All");
        filterAll.setSelected(true);
        filterAll.setGraphic(createNavigationIcon("check"));
        M3FilterChip filterNearby = new M3FilterChip("Nearby");
        M3FilterChip filterOpen = new M3FilterChip("Open now");
        filterOpen.setSelected(true);
        filterOpen.setGraphic(createNavigationIcon("check"));
        M3ChipGroup filters = createChipGroup(filterAll, filterNearby, filterOpen);
        filters.setPrefWrapLength(420.0);

        M3InputChip inputPerson = new M3InputChip("Alex Morgan");
        inputPerson.setGraphic(createNavigationIcon("person"));
        M3IconButton removePerson = new M3IconButton(
                createIconViewport(DemoIcons.onSurfaceVariant("close"), 18.0)
        );
        removePerson.setAccessibleText("Remove Alex Morgan");
        removePerson.setContainerWidth(24.0);
        removePerson.setContainerHeight(24.0);
        removePerson.setOnAction(event -> inputPerson.setVisible(false));
        inputPerson.setTrailingGraphic(removePerson);

        M3InputChip inputTeam = new M3InputChip("Design team");
        inputTeam.setSelected(true);
        inputTeam.setGraphic(createNavigationIcon("group"));
        M3IconButton removeTeam = new M3IconButton(
                createIconViewport(DemoIcons.onSurfaceVariant("close"), 18.0)
        );
        removeTeam.setAccessibleText("Remove Design team");
        removeTeam.setContainerWidth(24.0);
        removeTeam.setContainerHeight(24.0);
        removeTeam.setOnAction(event -> inputTeam.setSelected(false));
        inputTeam.setTrailingGraphic(removeTeam);

        M3InputChip inputDisabled = new M3InputChip("Unavailable");
        inputDisabled.setDisable(true);
        M3IconButton removeDisabled = new M3IconButton(
                createIconViewport(DemoIcons.onSurfaceVariant("close"), 18.0)
        );
        removeDisabled.setAccessibleText("Remove unavailable input");
        removeDisabled.setContainerWidth(24.0);
        removeDisabled.setContainerHeight(24.0);
        inputDisabled.setTrailingGraphic(removeDisabled);

        M3SuggestionChip suggestionReply = new M3SuggestionChip("Reply");
        M3SuggestionChip suggestionRemind = new M3SuggestionChip("Remind me");
        suggestionRemind.setGraphic(createNavigationIcon("schedule"));
        M3SuggestionChip suggestionElevated = new M3SuggestionChip("View details");
        suggestionElevated.setChipStyle(M3ChipStyle.ELEVATED);

        return createGallery(
                createShowcaseGroup("Assist Chips", assistDirections, assistCalendar, assistDisabled),
                createShowcaseGroup("Filter Chips", filters),
                createShowcaseGroup("Input Chips", inputPerson, inputTeam, inputDisabled),
                createShowcaseGroup("Suggestion Chips", suggestionReply, suggestionRemind, suggestionElevated)
        );
    }

    /// Creates the date picker component page.
    private Node createDatePickersPage() {
        LocalDate today = LocalDate.now();
        M3DatePickerField field = new M3DatePickerField(today);
        field.setLabelText("Event date");
        field.setSupportingText("Editable ISO date with popup calendar");
        field.setVariant(M3TextInputVariant.OUTLINED);
        field.getPresets().setAll(M3DatePresets.common(today));
        field.setPrefWidth(320.0);
        field.setMaxWidth(320.0);

        M3DatePickerField boundedField = new M3DatePickerField(today.plusDays(2));
        boundedField.setLabelText("Booking date");
        boundedField.setSupportingText("Limited to the next two weeks");
        boundedField.setVariant(M3TextInputVariant.FILLED);
        boundedField.getPicker().setMinDate(today);
        boundedField.getPicker().setMaxDate(today.plusDays(14));
        boundedField.getPresets().setAll(
                M3DatePresets.today(today),
                M3DatePresets.tomorrow(today),
                M3DatePresets.daysFrom(today, 7)
        );
        boundedField.setPrefWidth(320.0);
        boundedField.setMaxWidth(320.0);

        M3DateRangePickerField rangeField = new M3DateRangePickerField(today.plusDays(2), today.plusDays(8));
        rangeField.setStartLabelText("Start date");
        rangeField.setEndLabelText("End date");
        rangeField.setStartSupportingText("Editable range start");
        rangeField.setEndSupportingText("Editable range end");
        rangeField.setStartVariant(M3TextInputVariant.OUTLINED);
        rangeField.setEndVariant(M3TextInputVariant.OUTLINED);
        rangeField.getPicker().setMinDate(today.minusDays(7));
        rangeField.getPicker().setMaxDate(today.plusDays(30));
        rangeField.getPresets().setAll(M3DateRangePresets.common(today, rangeField.getPicker().getFirstDayOfWeek()));
        rangeField.setPrefWidth(680.0);
        rangeField.setMaxWidth(680.0);

        M3DatePicker selected = new M3DatePicker(today);

        M3DatePicker range = new M3DatePicker(today.plusDays(4));
        range.setMinDate(today.minusDays(3));
        range.setMaxDate(today.plusDays(18));

        M3DateRangePicker dateRange = new M3DateRangePicker(today.plusDays(2), today.plusDays(8));
        dateRange.setMinDate(today.minusDays(7));
        dateRange.setMaxDate(today.plusDays(30));

        M3DatePicker monthOnly = new M3DatePicker();
        monthOnly.setDisplayedMonth(YearMonth.from(today.plusMonths(1)));
        monthOnly.setShowAdjacentMonthDays(false);

        M3Button dateDialogButton = new M3Button("Open date dialog", M3ButtonVariant.FILLED);
        dateDialogButton.setOnAction(event -> showDatePickerDialog(today));
        M3Button rangeDialogButton = new M3Button("Open range dialog", M3ButtonVariant.TONAL);
        rangeDialogButton.setOnAction(event -> showDateRangePickerDialog(today.plusDays(2), today.plusDays(8)));
        M3Button presetRangeDialogButton = new M3Button("Open preset range dialog", M3ButtonVariant.OUTLINED);
        presetRangeDialogButton.setOnAction(event -> showPresetDateRangePickerDialog(today));

        return createGallery(
                createShowcaseGroup("Fields", field, boundedField),
                createShowcaseGroup("Range Field", rangeField),
                createShowcaseGroup("Dialogs", dateDialogButton, rangeDialogButton, presetRangeDialogButton),
                createShowcaseGroup("Selected Date", selected),
                createShowcaseGroup("Bounded Range", range, dateRange),
                createShowcaseGroup("Month Only", monthOnly)
        );
    }

    /// Creates the time picker component page.
    private Node createTimePickersPage() {
        M3TimePickerField field = new M3TimePickerField(LocalTime.of(10, 30));
        field.setLabelText("Start time");
        field.setSupportingText("Editable 24-hour time with popup picker");
        field.setVariant(M3TextInputVariant.OUTLINED);
        field.getPicker().setUse24HourClock(true);
        field.getPicker().setMinuteStep(15);
        field.getPresets().setAll(M3TimePresets.common(LocalTime.of(10, 30)));
        field.setPrefWidth(320.0);
        field.setMaxWidth(320.0);

        M3TimePickerField boundedField = new M3TimePickerField(LocalTime.of(9, 30));
        boundedField.setLabelText("Office hours");
        boundedField.setSupportingText("Limited to 09:00 through 17:30");
        boundedField.setVariant(M3TextInputVariant.FILLED);
        boundedField.getPicker().setMinTime(LocalTime.of(9, 0));
        boundedField.getPicker().setMaxTime(LocalTime.of(17, 30));
        boundedField.getPicker().setMinuteStep(30);
        boundedField.getPresets().setAll(M3TimePresets.morning(), M3TimePresets.noon(), M3TimePresets.afternoon());
        boundedField.setPrefWidth(320.0);
        boundedField.setMaxWidth(320.0);

        M3TimePicker twelveHour = new M3TimePicker(LocalTime.of(10, 30));

        M3TimePicker twentyFourHour = new M3TimePicker(LocalTime.of(14, 45));
        twentyFourHour.setUse24HourClock(true);
        twentyFourHour.setMinuteStep(15);

        M3TimePicker bounded = new M3TimePicker(LocalTime.of(9, 30));
        bounded.setMinTime(LocalTime.of(9, 0));
        bounded.setMaxTime(LocalTime.of(17, 30));

        M3Button dialogButton = new M3Button("Open time dialog", M3ButtonVariant.FILLED);
        dialogButton.setOnAction(event -> showTimePickerDialog(LocalTime.of(10, 30)));

        return createGallery(
                createShowcaseGroup("Fields", field, boundedField),
                createShowcaseGroup("Dialog", dialogButton),
                createShowcaseGroup("12 Hour", twelveHour),
                createShowcaseGroup("24 Hour", twentyFourHour),
                createShowcaseGroup("Bounded Range", bounded)
        );
    }

    /// Creates the menu component page.
    private Node createMenusPage() {
        M3Menu inlineMenu = new M3Menu(
                new M3MenuSectionHeader("File"),
                createMenuItem("New", "add", "Ctrl+N"),
                createMenuItem("Open", "folder", "Ctrl+O"),
                new M3SubMenuItem(
                        "Open Recent",
                        createMenuItem("Project Alpha", "work", ""),
                        createMenuItem("Project Beta", "bookmark", "")
                ),
                createMenuItem("Save", "save", "Ctrl+S"),
                new M3Divider(),
                new M3MenuSectionHeader("Recent"),
                createMenuItem("Project Alpha", "work", ""),
                createMenuItem("Project Beta", "bookmark", "")
        );

        M3MenuButton menuButton = new M3MenuButton(
                "Open menu",
                new M3MenuSectionHeader("Document"),
                createMenuItem("Duplicate", "bookmark", "Ctrl+D"),
                new M3SubMenuItem(
                        "Move to",
                        createMenuItem("Archive", "archive", ""),
                        createMenuItem("Inbox", "inbox", "")
                ),
                createMenuItem("Rename", "edit", ""),
                new M3Divider(),
                new M3MenuSectionHeader("Danger"),
                createMenuItem("Delete", "delete", "")
        );
        menuButton.setVariant(M3ButtonVariant.OUTLINED);
        menuButton.getMenu().setSelectionMode(M3SelectionMode.SINGLE);

        M3MenuItem selected = createMenuItem("Selected item", "check", "");
        M3MenuItem disabledStandard = createMenuItem("Unavailable", "delete", "");
        disabledStandard.setDisable(true);
        M3Menu selectedMenu = new M3Menu(
                selected,
                createMenuItem("Regular item", "label", ""),
                disabledStandard
        );
        selectedMenu.setSelectionMode(M3SelectionMode.SINGLE);
        selectedMenu.setAllowEmptySelection(false);
        selectedMenu.selectIndex(0);

        M3Menu multiSelectMenu = new M3Menu(
                new M3MenuSectionHeader("Visibility"),
                createMenuItem("Icons", "visibility", ""),
                createMenuItem("Labels", "label", ""),
                createMenuItem("Badges", "bookmark", "")
        );
        multiSelectMenu.setSelectionMode(M3SelectionMode.MULTIPLE);
        multiSelectMenu.selectIndex(1);
        multiSelectMenu.selectIndex(3);

        M3MenuItem vibrantSelected = createMenuItem("Pinned", "bookmark", "");
        M3MenuItem disabledVibrant = createMenuItem("Unavailable", "delete", "");
        disabledVibrant.setDisable(true);
        M3Menu vibrantMenu = new M3Menu(
                new M3MenuSectionHeader("Vibrant"),
                vibrantSelected,
                createMenuItem("Shared", "share", ""),
                createMenuItem("Archived", "archive", ""),
                disabledVibrant
        );
        vibrantMenu.setColorStyle(M3MenuColorStyle.VIBRANT);
        vibrantMenu.setSelectionMode(M3SelectionMode.SINGLE);
        vibrantMenu.setAllowEmptySelection(false);
        vibrantMenu.select(vibrantSelected);

        return createGallery(
                createShowcaseGroup("Menu Button", menuButton),
                createShowcaseGroup("Inline Menus", inlineMenu, selectedMenu, multiSelectMenu, vibrantMenu)
        );
    }

    /// Creates the segmented button component page.
    private Node createSegmentedButtonsPage() {
        M3SegmentedButtonGroup dateRange = createSegmentedGroup("Day", "Week", "Month");

        M3SegmentedButton lowPriority = createSegmentedButton("Low", "task");
        M3SegmentedButton mediumPriority = createSegmentedButton("Medium", "schedule");
        mediumPriority.setSelected(true);
        M3SegmentedButton highPriority = createSegmentedButton("High", "warning");
        M3SegmentedButtonGroup priority =
                createSegmentedButtonGroup(lowPriority, mediumPriority, highPriority);
        priority.getItems().get(2).setDisable(true);

        M3SegmentedButtonGroup channels = createSegmentedButtonGroup(
                createSegmentedButton("Email", "email"),
                createSegmentedButton("Chat", "group"),
                createSegmentedButton("Push", "notifications")
        );
        channels.clearSelection();
        channels.setSelectionMode(M3SelectionMode.MULTIPLE);
        channels.selectIndex(0);
        channels.selectIndex(2);

        return createGallery(
                createShowcaseGroup("Text With Selection Indicator", dateRange),
                createShowcaseGroup("Icon And Label", priority),
                createShowcaseGroup("Icon Multi Select", channels)
        );
    }

    /// Creates the split button component page.
    private Node createSplitButtonsPage() {
        M3SplitButton tonal = createSplitButton("Create", M3ButtonVariant.TONAL);
        M3SplitButton outlined = createSplitButton("Export", M3ButtonVariant.OUTLINED);
        M3SplitButton filled = createSplitButton("Publish", M3ButtonVariant.FILLED);
        M3SplitButton elevated = createSplitButton("Save", M3ButtonVariant.ELEVATED);
        tonal.setGraphic(createSplitButtonGraphic("create", M3ButtonVariant.TONAL, M3IconSize.SMALL));
        outlined.setGraphic(createSplitButtonGraphic("share", M3ButtonVariant.OUTLINED, M3IconSize.SMALL));
        filled.setGraphic(createSplitButtonGraphic("send", M3ButtonVariant.FILLED, M3IconSize.SMALL));
        elevated.setGraphic(createSplitButtonGraphic("save", M3ButtonVariant.ELEVATED, M3IconSize.SMALL));

        M3SplitButton disabled = createSplitButton("Disabled", M3ButtonVariant.TONAL);
        disabled.setDisable(true);

        M3SplitButton extraSmall = createSplitButton("XS", M3ButtonVariant.TONAL);
        M3SplitButton small = createSplitButton("Small", M3ButtonVariant.TONAL);
        M3SplitButton medium = createSplitButton("Medium", M3ButtonVariant.TONAL);
        M3SplitButton large = createSplitButton("Large", M3ButtonVariant.TONAL);
        M3SplitButton extraLarge = createSplitButton("XL", M3ButtonVariant.TONAL);
        extraSmall.setSize(M3ButtonSize.EXTRA_SMALL);
        small.setSize(M3ButtonSize.SMALL);
        medium.setSize(M3ButtonSize.MEDIUM);
        large.setSize(M3ButtonSize.LARGE);
        extraLarge.setSize(M3ButtonSize.EXTRA_LARGE);
        extraSmall.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.SMALL));
        small.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.SMALL));
        medium.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.MEDIUM));
        large.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.LARGE));
        extraLarge.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.EXTRA_LARGE));

        return createGallery(
                createShowcaseGroup("Color Roles", tonal, outlined, filled, elevated),
                createShowcaseGroup("Disabled", disabled),
                createShowcaseGroup("Size Scale", extraSmall, small, medium, large, extraLarge)
        );
    }

    /// Creates the tab component page.
    private Node createTabsPage() {
        M3TabBar primary = createTabBar("Overview", "Activity", "Files");
        M3TabBar secondary = createTabBar("Overview", "Details", "Reviews");
        secondary.setVariant(M3TabBarVariant.SECONDARY);
        secondary.getTabs().get(2).setDisable(true);

        M3TabBar scrollablePrimary = createTabBar(
                new M3Tab("Overview"),
                new M3Tab("Recent activity"),
                new M3Tab("Shared with me"),
                new M3Tab("Offline files"),
                new M3Tab("Storage management"),
                new M3Tab("Notifications"),
                new M3Tab("Security and privacy"),
                new M3Tab("Connected applications")
        );
        scrollablePrimary.setTabLayout(M3TabBarLayout.SCROLLABLE);
        scrollablePrimary.setPrefWidth(720.0);
        scrollablePrimary.setMaxWidth(720.0);

        M3TabBar scrollableSecondary = createTabBar(
                new M3Tab("Highlights"),
                new M3Tab("Technical specifications"),
                new M3Tab("Customer reviews"),
                new M3Tab("Compatibility"),
                new M3Tab("Accessories"),
                new M3Tab("Support resources"),
                new M3Tab("Release history")
        );
        scrollableSecondary.setVariant(M3TabBarVariant.SECONDARY);
        scrollableSecondary.setTabLayout(M3TabBarLayout.SCROLLABLE);
        scrollableSecondary.setPrefWidth(720.0);
        scrollableSecondary.setMaxWidth(720.0);

        M3TabBar hierarchyPrimary = createTabBar("Flights", "Trips", "Explore");
        M3TabBar hierarchySecondary = createTabBar("Upcoming", "Previous", "Saved");
        hierarchySecondary.setVariant(M3TabBarVariant.SECONDARY);
        VBox hierarchy = new VBox(hierarchyPrimary, hierarchySecondary);
        hierarchy.getStyleClass().add("demo-tab-hierarchy");

        return createGallery(
                createShowcaseGroup("Primary Fixed", primary),
                createShowcaseGroup("Secondary Fixed", secondary),
                createFullWidthShowcaseGroup("Primary Scrollable", scrollablePrimary),
                createFullWidthShowcaseGroup("Secondary Scrollable", scrollableSecondary),
                createShowcaseGroup("Primary And Secondary Hierarchy", hierarchy)
        );
    }

    /// Creates the app bar component page.
    private Node createAppBarsPage() {
        M3TopAppBar small = createTopAppBar("Inbox", M3TopAppBarVariant.SMALL, "menu", "search", "more");
        M3TopAppBar centerAligned = createTopAppBar("Calendar", M3TopAppBarVariant.CENTER_ALIGNED,
                "back", "add", "more");
        M3TopAppBar mediumFlexible = createTopAppBar(
                "Library",
                M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                "menu",
                "search",
                "more"
        );
        M3TopAppBar mediumFlexibleSubtitle = createTopAppBar(
                "Messages",
                M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                "back",
                "search",
                "more"
        );
        mediumFlexibleSubtitle.setSubtitle("4 unread conversations");
        M3TopAppBar largeFlexible = createTopAppBar(
                "Discover",
                M3TopAppBarVariant.LARGE_FLEXIBLE,
                "menu",
                "search",
                "more"
        );
        M3TopAppBar largeFlexibleSubtitle = createTopAppBar(
                "Collections",
                M3TopAppBarVariant.LARGE_FLEXIBLE,
                "back",
                "search",
                "more"
        );
        largeFlexibleSubtitle.setSubtitle("Recently updated");

        M3TopAppBar medium = createTopAppBar("Project", M3TopAppBarVariant.MEDIUM, "menu", "search", "more");
        M3TopAppBar large = createTopAppBar("Workspace", M3TopAppBarVariant.LARGE, "menu", "search", "more");

        M3TopAppBar mediumScrolled = createTopAppBar(
                "Downloads",
                M3TopAppBarVariant.MEDIUM_FLEXIBLE,
                "menu",
                "search",
                "more"
        );
        M3TopAppBar largeScrolled = createTopAppBar(
                "Photography",
                M3TopAppBarVariant.LARGE_FLEXIBLE,
                "back",
                "search",
                "more"
        );
        largeScrolled.setSubtitle("Shared collection");
        mediumScrolled.setScrolledUnder(true);
        largeScrolled.setScrolledUnder(true);

        M3Button toggleScrollState = new M3Button("Toggle scroll state");
        toggleScrollState.setVariant(M3ButtonVariant.TONAL);
        toggleScrollState.setOnAction(event -> {
            boolean scrolledUnder = !mediumScrolled.isScrolledUnder();
            mediumScrolled.setScrolledUnder(scrolledUnder);
            largeScrolled.setScrolledUnder(scrolledUnder);
        });

        return createGallery(
                createAppBarShowcaseGroup(
                        "Current Variants",
                        createLabeledAppBarPreview("Small", createTopAppBarPreview(small)),
                        createLabeledAppBarPreview("Center Aligned", createTopAppBarPreview(centerAligned)),
                        createLabeledAppBarPreview("Medium Flexible", createTopAppBarPreview(mediumFlexible)),
                        createLabeledAppBarPreview(
                                "Medium Flexible with Subtitle",
                                createTopAppBarPreview(mediumFlexibleSubtitle)
                        ),
                        createLabeledAppBarPreview("Large Flexible", createTopAppBarPreview(largeFlexible)),
                        createLabeledAppBarPreview(
                                "Large Flexible with Subtitle",
                                createTopAppBarPreview(largeFlexibleSubtitle)
                        )
                ),
                createAppBarShowcaseGroup(
                        "Baseline Compatibility",
                        createLabeledAppBarPreview("Medium", createTopAppBarPreview(medium)),
                        createLabeledAppBarPreview("Large", createTopAppBarPreview(large))
                ),
                createAppBarShowcaseGroup(
                        "Scroll Transformation",
                        toggleScrollState,
                        createLabeledAppBarPreview("Medium Flexible collapsed", createTopAppBarPreview(mediumScrolled)),
                        createLabeledAppBarPreview("Large Flexible collapsed", createTopAppBarPreview(largeScrolled))
                )
        );
    }

    /// Creates the toolbar component page.
    private Node createToolbarsPage() {
        M3Toolbar floatingStandard = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.STANDARD,
                Orientation.HORIZONTAL,
                "archive",
                "share",
                "edit",
                "more"
        );
        M3Toolbar floatingVibrant = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.VIBRANT,
                Orientation.HORIZONTAL,
                "bold",
                "italic",
                "underline",
                "tune",
                "visibility"
        );
        M3Toolbar dockedStandard = createToolbar(
                M3ToolbarVariant.DOCKED,
                M3ToolbarColorStyle.STANDARD,
                Orientation.HORIZONTAL,
                "home",
                "search",
                "notifications",
                "person"
        );
        dockedStandard.setMaxWidth(Double.MAX_VALUE);

        M3Toolbar dockedVibrant = createToolbar(
                M3ToolbarVariant.DOCKED,
                M3ToolbarColorStyle.VIBRANT,
                Orientation.HORIZONTAL,
                "search",
                "favorite",
                "settings",
                "more"
        );
        dockedVibrant.setMaxWidth(Double.MAX_VALUE);

        M3Toolbar vertical = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.VIBRANT,
                Orientation.VERTICAL,
                "search",
                "favorite",
                "settings",
                "more"
        );

        M3Toolbar pairedToolbar = createToolbar(
                M3ToolbarVariant.FLOATING,
                M3ToolbarColorStyle.STANDARD,
                Orientation.HORIZONTAL,
                "archive",
                "share",
                "more"
        );
        HBox toolbarWithFab = new HBox(
                8.0,
                pairedToolbar,
                createFab(
                        "add",
                        M3FloatingActionButtonVariant.SECONDARY_CONTAINER,
                        M3FloatingActionButtonSize.REGULAR
                )
        );
        toolbarWithFab.setAlignment(Pos.CENTER_LEFT);
        toolbarWithFab.getStyleClass().add("demo-toolbar-fab-pair");

        return createGallery(
                createShowcaseGroup("Floating Standard", floatingStandard),
                createShowcaseGroup("Floating Vibrant", floatingVibrant),
                createFullWidthShowcaseGroup("Docked Standard", createToolbarPreview(dockedStandard)),
                createFullWidthShowcaseGroup("Docked Vibrant", createToolbarPreview(dockedVibrant)),
                createShowcaseGroup("Floating With FAB", toolbarWithFab),
                createShowcaseGroup("Vertical Floating", vertical)
        );
    }

    /// Creates the bottom app bar component page.
    private Node createBottomAppBarsPage() {
        M3BottomAppBar end = createBottomAppBar();
        M3BottomAppBar center = createBottomAppBar();
        center.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.CENTER);
        M3BottomAppBar start = createBottomAppBar();
        start.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.START);

        return createGallery(
                createShowcaseGroup(
                        "Floating Action",
                        createBottomAppBarPreview(end),
                        createBottomAppBarPreview(center),
                        createBottomAppBarPreview(start)
                )
        );
    }

    /// Creates the navigation component page.
    private Node createNavigationPage() {
        M3NavigationBar compact = createFourItemNavigationBar();
        M3NavigationBar medium = createFourItemNavigationBar();
        medium.setItemLayout(M3NavigationItemLayout.HORIZONTAL);
        medium.setMaxWidth(720.0);

        return createGallery(
                createFullWidthShowcaseGroup("Compact Window", compact),
                createFullWidthShowcaseGroup("Medium Window", medium)
        );
    }

    /// Creates the navigation rail component page.
    private Node createNavigationRailPage() {
        M3NavigationRail collapsed = createFourItemNavigationRail();
        collapsed.setPrefHeight(520.0);

        M3NavigationRail narrow = createFourItemNavigationRail();
        narrow.setNarrow(true);
        narrow.setItemsCentered(true);
        narrow.setPrefHeight(520.0);

        M3NavigationRail standard = createFourItemNavigationRail();
        standard.setExpanded(true);
        standard.setPrefHeight(520.0);

        M3NavigationRail modal = createFourItemNavigationRail();
        modal.setVariant(M3NavigationRailVariant.MODAL);
        modal.setFullWidthIndicator(true);
        modal.setExpanded(true);
        modal.setPrefHeight(520.0);

        M3NavigationRail immersive = createFourItemNavigationRail();
        immersive.setVariant(M3NavigationRailVariant.MODAL);
        immersive.setHideWhenCollapsed(true);
        immersive.setExpanded(true);
        immersive.setPrefHeight(520.0);

        return createGallery(
                createShowcaseGroup(
                        "Collapsed",
                        createNavigationRailPreview("Collapsed with action", collapsed, true),
                        createNavigationRailPreview("Narrow, centered destinations", narrow, false)
                ),
                createShowcaseGroup(
                        "Expanded",
                        createNavigationRailPreview("Expanded standard", standard, false),
                        createNavigationRailPreview("Expanded modal, full-width indicator", modal, false),
                        createNavigationRailPreview("Immersive, hide when collapsed", immersive, false)
                )
        );
    }

    /// Creates the navigation drawer component page.
    private Node createNavigationDrawerPage() {
        M3NavigationDrawer standard = createFourItemNavigationDrawer();
        standard.getItems().addAll(
                new M3Divider(),
                createDrawerItem("Drafts", "edit"),
                createDrawerItem("Spam", "warning"),
                createDrawerItem("Trash", "delete"),
                createDrawerItem("Settings", "settings")
        );
        standard.setPrefHeight(280.0);
        M3NavigationDrawer modal = createFourItemNavigationDrawer();
        modal.setVariant(M3NavigationDrawerVariant.MODAL);
        M3NavigationDrawer grouped = createSectionNavigationDrawer();
        grouped.setPrefHeight(360.0);

        return createGallery(
                createShowcaseGroup("Standard", standard),
                createFullWidthShowcaseGroup("Modal", createModalNavigationDrawerPreview(modal)),
                createShowcaseGroup("Grouped destinations", grouped)
        );
    }

    /// Creates a modal navigation drawer preview above application content and a scrim.
    ///
    /// @param drawer the modal drawer
    /// @return the modal drawer preview
    private static StackPane createModalNavigationDrawerPreview(M3NavigationDrawer drawer) {
        Label contentLabel = new Label("Application content");
        contentLabel.getStyleClass().add("demo-modal-drawer-content-label");
        StackPane applicationContent = new StackPane(contentLabel);
        applicationContent.getStyleClass().add("demo-modal-drawer-content");

        M3Scrim scrim = new M3Scrim();
        scrim.setFocusTraversable(false);

        drawer.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane preview = new StackPane(applicationContent, scrim, drawer);
        preview.getStyleClass().add("demo-modal-drawer-preview");
        StackPane.setAlignment(drawer, Pos.TOP_LEFT);
        return preview;
    }

    /// Creates the loading indicator component page.
    private Node createLoadingIndicatorPage() {
        M3LoadingIndicator defaultIndicator = new M3LoadingIndicator();
        applyLargeLoadingIndicator(defaultIndicator);

        M3LoadingIndicator containedIndicator = new M3LoadingIndicator();
        applyLargeLoadingIndicator(containedIndicator);
        containedIndicator.setVariant(M3LoadingIndicatorVariant.CONTAINED);

        return createGallery(
                createShowcaseGroup("Default", defaultIndicator),
                createShowcaseGroup("Contained", containedIndicator)
        );
    }

    /// Applies the large demo loading indicator geometry.
    private static void applyLargeLoadingIndicator(M3LoadingIndicator loadingIndicator) {
        loadingIndicator.setStyle("-m3-container-size: 112px; -m3-indicator-size: 89px;");
        loadingIndicator.setMinSize(112.0, 112.0);
        loadingIndicator.setPrefSize(112.0, 112.0);
        loadingIndicator.setMaxSize(112.0, 112.0);
    }

    /// Creates the progress component page.
    private Node createProgressPage() {
        M3ProgressBar determinateBar = new M3ProgressBar(0.32);
        determinateBar.setPrefWidth(380.0);
        applyBaselineProgress(determinateBar);
        M3ProgressBar indeterminateBar = new M3ProgressBar();
        indeterminateBar.setPrefWidth(380.0);
        applyBaselineProgress(indeterminateBar);
        M3ProgressIndicator determinateIndicator = new M3ProgressIndicator(0.32);
        applyBaselineProgress(determinateIndicator);
        M3ProgressIndicator indeterminateIndicator = new M3ProgressIndicator();
        applyBaselineProgress(indeterminateIndicator);

        M3ProgressBar expressiveDeterminateBar = new M3ProgressBar(0.32);
        expressiveDeterminateBar.setPrefWidth(380.0);
        applyExpressiveLinearProgress(expressiveDeterminateBar);
        M3ProgressBar expressiveIndeterminateBar = new M3ProgressBar();
        expressiveIndeterminateBar.setPrefWidth(380.0);
        applyExpressiveLinearProgress(expressiveIndeterminateBar);
        M3ProgressIndicator expressiveDeterminateIndicator = new M3ProgressIndicator(0.32);
        applyExpressiveCircularProgress(expressiveDeterminateIndicator);
        M3ProgressIndicator expressiveIndeterminateIndicator = new M3ProgressIndicator();
        applyExpressiveCircularProgress(expressiveIndeterminateIndicator);

        playProgressShowcaseAnimation(determinateBar, determinateIndicator);
        playProgressShowcaseAnimation(expressiveDeterminateBar, expressiveDeterminateIndicator);

        return createGallery(
                createShowcaseGroup("Standard Linear", determinateBar, indeterminateBar),
                createShowcaseGroup("Standard Circular", determinateIndicator, indeterminateIndicator),
                createShowcaseGroup("Expressive Wavy Linear", expressiveDeterminateBar, expressiveIndeterminateBar),
                createShowcaseGroup("Track Heights", createProgressTrackHeightMatrix()),
                createShowcaseGroup(
                        "Expressive Wavy Circular",
                        expressiveDeterminateIndicator,
                        expressiveIndeterminateIndicator
                )
        );
    }

    /// Creates the list component page.
    private Node createListPage() {
        M3ListItem oneLine = new M3ListItem("One-line item");
        oneLine.setLeading(createSurfaceVariantIcon("inbox"));

        M3ListItem disabled = new M3ListItem("Disabled item");
        disabled.setSupportingText("Unavailable destination");
        disabled.setLeading(createSurfaceVariantIcon("lock"));
        disabled.setDisable(true);

        M3ListItem twoLine = new M3ListItem("Two-line item");
        twoLine.setSupportingText("Supporting text");
        twoLine.setTrailingSupportingText("3 min");
        twoLine.setTrailing(createSurfaceVariantIcon("chevron-right"));

        M3ListItem threeLine = new M3ListItem("Three-line item");
        threeLine.setOverlineText("Overline");
        threeLine.setSupportingText("Supporting text can span a denser row.");
        threeLine.setLeadingAvatar("A");

        M3ListPane standardList = new M3ListPane();
        standardList.getStyleClass().add("demo-list");
        standardList.setSelectionMode(M3SelectionMode.SINGLE);
        standardList.getItems().addAll(
                new M3ListSectionHeader("Recent"),
                oneLine,
                disabled,
                twoLine,
                threeLine
        );

        M3ListItem thumbnail = new M3ListItem("Thumbnail item");
        thumbnail.setSupportingText("Leading square media and trailing metadata.");
        thumbnail.setLeadingThumbnail(createListThumbnail());
        thumbnail.setTrailingSupportingText("12:40");

        M3ListItem wideThumbnail = new M3ListItem("Wide thumbnail item");
        wideThumbnail.setSupportingText("Media content is clipped to the configured slot size.");
        wideThumbnail.setLeadingMedia(createListThumbnail(), M3ListItemSlotSize.WIDE_THUMBNAIL);
        wideThumbnail.setTrailing(createSurfaceVariantIcon("chevron-right"));

        M3ListItem selected = new M3ListItem("Selected item");
        selected.setSupportingText("Current destination");
        selected.setLeading(createSurfaceVariantIcon("done"));
        selected.setTrailingSupportingText("Now");

        M3ListPane segmentedList = new M3ListPane();
        segmentedList.getStyleClass().add("demo-list");
        segmentedList.setListStyle(M3ListStyle.SEGMENTED);
        segmentedList.setSelectionMode(M3SelectionMode.SINGLE);
        segmentedList.getItems().addAll(thumbnail, wideThumbnail, selected);
        segmentedList.select(selected);

        return createGallery(
                createShowcaseGroup("Standard", standardList),
                createShowcaseGroup("Segmented", segmentedList),
                createShowcaseGroup("Virtualized Segmented", createVirtualizedListView())
        );
    }

    /// Creates the virtualized list view sample.
    private static M3ListView<String> createVirtualizedListView() {
        M3ListView<String> listView = new M3ListView<>();
        listView.getStyleClass().add("demo-virtualized-list");
        listView.setListStyle(M3ListStyle.SEGMENTED);
        for (int i = 1; i <= 240; i++) {
            listView.getItems().add("Virtualized row " + i);
        }
        listView.setSelectionMode(M3SelectionMode.SINGLE);
        listView.setFixedCellSize(72.0);
        listView.setPrefSize(520.0, 360.0);
        listView.setCellFactory(view -> new M3ListCell<>(view) {
            /// Creates the reusable row structure for this virtualized cell.
            @Override
            protected M3ListItem createListItem() {
                M3ListItem item = new M3ListItem();
                item.setSupportingText("Reused VirtualFlow row with generated content");
                item.setLeading(createSurfaceVariantIcon("task"));
                return item;
            }

            /// Updates the reusable row with the current data value.
            @Override
            protected void updateListItem(M3ListItem item, String text) {
                item.setHeadlineText(text);
                item.setTrailingSupportingText(Integer.toString(text.length()));
            }
        });
        listView.selectIndex(2);
        return listView;
    }

    /// Creates a sample thumbnail used by list item media rows.
    private static StackPane createListThumbnail() {
        Node icon = createIconViewport(DemoIcons.onSurface("image"));
        StackPane thumbnail = new StackPane(icon);
        thumbnail.getStyleClass().add("demo-list-thumbnail");
        return thumbnail;
    }

    /// Creates the badge component page.
    private Node createBadgesPage() {
        M3Button button = new M3Button("Inbox", M3ButtonVariant.TONAL);
        M3BadgedBox buttonWithBadge = new M3BadgedBox(button, new M3Badge("9"));

        return createGallery(
                createShowcaseGroup("Badges", new M3Badge(), new M3Badge("7"), new M3Badge("1234")),
                createShowcaseGroup("Attached", buttonWithBadge)
        );
    }

    /// Creates the avatar component page.
    private Node createAvatarsPage() {
        M3Avatar initials = new M3Avatar("AB");
        M3Avatar single = new M3Avatar("M");
        single.setVariant(M3AvatarVariant.SECONDARY);
        M3Avatar graphic = new M3Avatar(createNavigationIcon("person"));
        graphic.setVariant(M3AvatarVariant.TERTIARY);
        M3Avatar surface = new M3Avatar("S");
        surface.setVariant(M3AvatarVariant.SURFACE);

        M3ListItem account = new M3ListItem("Account");
        account.setSupportingText("Avatar as leading content");
        account.setLeading(new M3Avatar("A"));

        return createGallery(
                createShowcaseGroup("Avatars", initials, single, graphic, surface),
                createShowcaseGroup("List Usage", account)
        );
    }

    /// Creates the divider component page.
    private Node createDividersPage() {
        M3Divider full = new M3Divider();
        full.setPrefWidth(360.0);
        M3Divider inset = new M3Divider();
        inset.setInsetStart(32.0);
        inset.setPrefWidth(360.0);
        M3Divider middle = new M3Divider();
        middle.setInsetStart(32.0);
        middle.setInsetEnd(32.0);
        middle.setPrefWidth(360.0);
        M3Divider vertical = new M3Divider(Orientation.VERTICAL);
        vertical.setPrefHeight(72.0);

        return createGallery(
                createShowcaseGroup("Horizontal", full, inset, middle),
                createShowcaseGroup("Vertical", vertical)
        );
    }

    /// Creates the surface component page.
    private Node createSurfacesPage() {
        M3Surface surface = createSurface("Surface", M3SurfaceVariant.SURFACE, M3SurfaceElevation.LEVEL0);
        M3Surface container = createSurface("Container", M3SurfaceVariant.CONTAINER, M3SurfaceElevation.LEVEL1);
        M3Surface high = createSurface("High", M3SurfaceVariant.CONTAINER_HIGH, M3SurfaceElevation.LEVEL3);
        M3Surface primary = createSurface("Primary", M3SurfaceVariant.PRIMARY_CONTAINER, M3SurfaceElevation.LEVEL2);
        M3Surface secondary = createSurface("Secondary", M3SurfaceVariant.SECONDARY_CONTAINER, M3SurfaceElevation.LEVEL2);
        M3Surface tertiary = createSurface("Tertiary", M3SurfaceVariant.TERTIARY_CONTAINER, M3SurfaceElevation.LEVEL2);

        return createGallery(
                createShowcaseGroup("Surface Tones", surface, container, high),
                createShowcaseGroup("Container Colors", primary, secondary, tertiary)
        );
    }

    /// Creates the card component page.
    private Node createCardsPage() {
        M3Card filled = createSampleCard(
                "Filled card",
                "Status summary",
                "Use filled cards for related content on a higher emphasis container.",
                M3CardVariant.FILLED
        );
        M3Card outlined = createSampleCard(
                "Outlined card",
                "Document details",
                "Use outlined cards when the page already has stronger filled surfaces.",
                M3CardVariant.OUTLINED
        );
        M3Card elevated = createSampleCard(
                "Elevated card",
                "Pinned project",
                "Use elevated cards sparingly when separation from the page background matters.",
                M3CardVariant.ELEVATED
        );

        M3Card media = createMediaCard(
                "Media card",
                "Preview",
                "Use media cards for concise previews with clear supporting actions.",
                M3CardVariant.FILLED
        );
        M3Card elevatedMedia = createMediaCard(
                "Release candidate",
                "Passive container",
                "Cards with nested actions keep the container itself passive.",
                M3CardVariant.ELEVATED
        );
        M3Card outlinedMedia = createMediaCard(
                "Archived review",
                "Supporting actions",
                "Each nested action remains an independent keyboard and pointer target.",
                M3CardVariant.OUTLINED
        );

        M3Card dragged = createSampleCard(
                "Dragged card",
                "Reordering state",
                "Dragged cards use the published state layer and elevated container level.",
                M3CardVariant.ELEVATED
        );
        dragged.setDragged(true);

        M3Card disabled = createSampleCard(
                "Disabled card",
                "Unavailable",
                "Disabled cards retain their variant-specific container treatment without interaction feedback.",
                M3CardVariant.OUTLINED
        );
        disabled.setDisable(true);

        return createGallery(
                createShowcaseGroup("Variants", filled, outlined, elevated),
                createShowcaseGroup("Passive Cards With Actions", media, elevatedMedia, outlinedMedia),
                createShowcaseGroup("States", dragged, disabled)
        );
    }

    /// Creates the carousel component page.
    private Node createCarouselPage() {
        M3Carousel multiBrowse = createCarousel(
                createCarouselCard("Morning focus", "Deep work", "schedule", 280.0, 140.0),
                createCarouselCard("Design review", "Components", "edit", 280.0, 140.0),
                createCarouselCard("Release notes", "Packaging", "reports", 280.0, 140.0),
                createCarouselCard("Accessibility", "Keyboard", "visibility", 280.0, 140.0),
                createCarouselCard("Motion study", "Expressive", "spark", 280.0, 140.0),
                createCarouselCard("Color system", "Palettes", "image", 280.0, 140.0)
        );
        multiBrowse.setCarouselLayout(M3CarouselLayout.MULTI_BROWSE);
        multiBrowse.setMaxWidth(Double.MAX_VALUE);
        multiBrowse.selectFirst();

        M3Button showAll = new M3Button("Show all", M3ButtonVariant.TEXT);
        showAll.setOnAction(event -> showSnackbar("Open the complete carousel collection"));

        VBox multiBrowseSample = new VBox(4.0, multiBrowse, showAll);
        multiBrowseSample.setFillWidth(true);
        multiBrowseSample.setMaxWidth(Double.MAX_VALUE);

        M3Carousel hero = createCarousel(
                createCarouselCard("City guide", "Featured", "navigation", 320.0, 168.0),
                createCarouselCard("Architecture", "Collection", "dashboard", 320.0, 168.0),
                createCarouselCard("Landscape", "Weekend", "image", 320.0, 168.0),
                createCarouselCard("Portraits", "Stories", "person", 320.0, 168.0)
        );
        hero.setCarouselLayout(M3CarouselLayout.HERO);
        hero.setMaxWidth(Double.MAX_VALUE);
        hero.selectFirst();

        M3Carousel centerAlignedHero = createCarousel(
                createCarouselCard("Previous", "Preview", "back", 320.0, 168.0),
                createCarouselCard("Research", "Background", "search", 320.0, 168.0),
                createCarouselCard("Featured story", "Centered", "star", 320.0, 168.0),
                createCarouselCard("Gallery", "Related", "image", 320.0, 168.0),
                createCarouselCard("Next", "Preview", "chevron-right", 320.0, 168.0)
        );
        centerAlignedHero.setCarouselLayout(M3CarouselLayout.CENTER_ALIGNED_HERO);
        centerAlignedHero.setMaxWidth(Double.MAX_VALUE);
        centerAlignedHero.selectIndex(2);

        M3Carousel uncontained = createCarousel(
                createCarouselCard("Inbox", "24 unread", "inbox", 176.0, 112.0),
                createCarouselCard("Tasks", "6 due", "task", 176.0, 112.0),
                createCarouselCard("Files", "Recent", "folder", 176.0, 112.0),
                createCarouselCard("People", "Updates", "group", 176.0, 112.0),
                createCarouselCard("Calendar", "3 events", "calendar", 176.0, 112.0),
                createCarouselCard("Messages", "8 new", "email", 176.0, 112.0)
        );
        uncontained.setCarouselLayout(M3CarouselLayout.UNCONTAINED);
        uncontained.setMaxWidth(Double.MAX_VALUE);
        uncontained.selectFirst();

        M3Carousel multiAspectRatio = createCarousel(
                createCarouselCard("Morning focus", "Deep work", "schedule", 232.0, 140.0),
                createCarouselCard("Design review", "Components", "edit", 280.0, 140.0),
                createCarouselCard("Release notes", "Packaging", "reports", 196.0, 140.0),
                createCarouselCard("Mood board", "Inspiration", "image", 252.0, 140.0),
                createCarouselCard("Accessibility", "Keyboard", "visibility", 180.0, 140.0),
                createCarouselCard("Motion study", "Expressive", "spark", 264.0, 140.0)
        );
        multiAspectRatio.setCarouselLayout(M3CarouselLayout.UNCONTAINED_MULTI_ASPECT_RATIO);
        multiAspectRatio.setMaxWidth(Double.MAX_VALUE);
        multiAspectRatio.selectFirst();

        M3Carousel fullScreen = createCarousel(
                createCarouselCard("Workspace", "Edge-to-edge", "work", 320.0, 420.0),
                createCarouselCard("Timeline", "Project activity", "schedule", 320.0, 420.0),
                createCarouselCard("Insights", "Reporting", "reports", 320.0, 420.0)
        );
        fullScreen.setCarouselLayout(M3CarouselLayout.FULL_SCREEN);
        fullScreen.setPrefSize(320.0, 420.0);
        fullScreen.setMaxSize(320.0, 420.0);
        fullScreen.selectFirst();

        return createGallery(
                createFullWidthShowcaseGroup("Multi-browse", multiBrowseSample),
                createFullWidthShowcaseGroup("Hero", hero),
                createFullWidthShowcaseGroup("Center-aligned hero", centerAlignedHero),
                createFullWidthShowcaseGroup("Uncontained", uncontained),
                createFullWidthShowcaseGroup("Uncontained multi-aspect ratio", multiAspectRatio),
                createFullWidthShowcaseGroup("Full-screen", fullScreen)
        );
    }

    /// Creates the side sheet component page.
    private Node createSideSheetsPage() {
        M3SideSheet sideSheet = new M3SideSheet("Details", createSheetContent());
        sideSheet.getHeaderActions().add(createIconButton("close"));

        M3SideSheet modalSideSheet = new M3SideSheet("Filters", createSheetContent());
        modalSideSheet.getHeaderActions().add(createIconButton("close"));
        modalSideSheet.getActions().addAll(
                new M3Button("Cancel", M3ButtonVariant.TEXT),
                new M3Button("Apply", M3ButtonVariant.FILLED)
        );
        modalSideSheet.setVariant(M3SheetVariant.MODAL);

        M3SideSheet detachedSideSheet = new M3SideSheet("Detached", createSheetContent());
        detachedSideSheet.getHeaderActions().add(createIconButton("close"));
        detachedSideSheet.setDetached(true);

        return createGallery(
                createShowcaseGroup("Standard, Modal, And Detached", sideSheet, modalSideSheet, detachedSideSheet)
        );
    }

    /// Creates the bottom sheet component page.
    private Node createBottomSheetsPage() {
        M3BottomSheet bottomSheet = new M3BottomSheet("Now playing", createSheetContent());
        bottomSheet.getActions().add(createIconButton("close"));
        bottomSheet.setPrefWidth(520.0);
        bottomSheet.setOnDragHandleAction(event -> bottomSheet.setPrefHeight(
                bottomSheet.getPrefHeight() > 360.0 ? Region.USE_COMPUTED_SIZE : 420.0
        ));

        M3BottomSheet modalBottomSheet = new M3BottomSheet("Filters", createSheetContent());
        modalBottomSheet.getActions().add(createIconButton("close"));
        modalBottomSheet.setVariant(M3SheetVariant.MODAL);
        modalBottomSheet.setDragHandleVisible(false);
        modalBottomSheet.setPrefWidth(520.0);

        return createGallery(
                createShowcaseGroup("Standard And Modal", bottomSheet, modalBottomSheet)
        );
    }

    /// Creates the scrim component page.
    private Node createScrimsPage() {
        StackPane plainScrim = createScrimPreview(false);
        StackPane actionScrim = createScrimPreview(true);

        return createGallery(
                createShowcaseGroup("States", plainScrim, actionScrim)
        );
    }

    /// Creates the dialog component page.
    private Node createDialogsPage() {
        M3Button basicButton = new M3Button("Open basic", M3ButtonVariant.FILLED);
        basicButton.setOnAction(event -> showDemoDialog());
        M3Button settingsButton = new M3Button("Open settings", M3ButtonVariant.TONAL);
        settingsButton.setOnAction(event -> showSettingsDialog());
        M3Button destructiveButton = new M3Button("Open destructive", M3ButtonVariant.OUTLINED);
        destructiveButton.setOnAction(event -> showDestructiveDialog());

        M3DialogPane basicPane = createDialogPreviewPane(
                "Dialog title",
                "The active theme is applied to this dialog pane.",
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE)
        );
        basicPane.setPrefWidth(420.0);

        M3DialogPane settingsPane = createDialogPreviewPane(
                "Project settings",
                null,
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
                new ButtonType("Apply", ButtonBar.ButtonData.APPLY)
        );
        settingsPane.setContent(createDialogSettingsContent(false));
        settingsPane.setPrefWidth(520.0);

        M3DialogPane longPane = createDialogPreviewPane(
                "Release notes",
                null,
                new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        longPane.setContent(createScrollableDialogContent());
        longPane.setPrefWidth(520.0);

        return createGallery(
                createShowcaseGroup("Launchers", basicButton, settingsButton, destructiveButton),
                createShowcaseGroup("Inline Panes", basicPane, settingsPane),
                createShowcaseGroup("Scrollable Content", longPane)
        );
    }

    /// Creates one inline dialog pane preview.
    private static M3DialogPane createDialogPreviewPane(
            String headerText,
            @Nullable String contentText,
            ButtonType... buttonTypes
    ) {
        M3DialogPane pane = new M3DialogPane();
        pane.getStyleClass().add("demo-dialog-pane");
        pane.setMaxHeight(Region.USE_PREF_SIZE);
        pane.setHeaderText(headerText);
        if (contentText != null) {
            pane.setContentText(contentText);
        }
        pane.getButtonTypes().addAll(buttonTypes);
        return pane;
    }

    /// Creates form-like content for dialog previews and popups.
    private static Node createDialogSettingsContent(boolean popup) {
        M3TextField projectName = createTextField("Project name", "M3FX", M3TextInputVariant.OUTLINED, false);
        projectName.setPrefWidth(popup ? 360.0 : 320.0);
        M3TextInputLayout projectLayout = createTextInputLayout(projectName, "Shown in generated artifacts");
        projectLayout.setPrefWidth(projectName.getPrefWidth());
        projectLayout.setMaxWidth(projectName.getPrefWidth());

        M3Switch notifications = new M3Switch("Notify contributors");
        notifications.setSelected(true);
        M3CheckBox rememberChoice = new M3CheckBox("Remember this choice");
        rememberChoice.setSelected(true);

        VBox content = new VBox(12.0, projectLayout, notifications, rememberChoice);
        content.getStyleClass().add("demo-dialog-content");
        content.setMaxWidth(Double.MAX_VALUE);
        return content;
    }

    /// Creates scrollable long-form dialog content.
    private static Node createScrollableDialogContent() {
        VBox content = new VBox(8.0);
        content.getStyleClass().add("demo-dialog-scroll-content");
        content.getChildren().addAll(
                new Label("Review theme inheritance, popup focus restoration, and generated runtime packaging."),
                new Label("The dialog body can host regular JavaFX content while M3FX supplies the surrounding surface, actions, shape, and color tokens."),
                new Label("Use this form for dense supporting information that should stay inside a compact modal surface."),
                new Label("Scrolling keeps the action row visible while the body remains inspectable.")
        );
        for (Node node : content.getChildren()) {
            if (node instanceof Label label) {
                label.setWrapText(true);
                label.getStyleClass().add("demo-dialog-body-line");
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("demo-dialog-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(132.0);
        scrollPane.setMaxHeight(Region.USE_PREF_SIZE);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        M3ScrollPanes.style(scrollPane);
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        return scrollPane;
    }

    /// Creates the banner component page.
    private Node createBannersPage() {
        M3Banner informational = createBanner(
                "M3FX can install generated token stylesheets for each JavaFX scene while keeping application scene management explicit.",
                createInfoIcon(),
                "Learn",
                "Dismiss"
        );

        M3Banner warning = createBanner(
                "The selected jlink target uses platform-specific BellSoft LibericaJDK Full jmods.",
                createErrorIcon("warning"),
                "Review"
        );

        M3Banner noIcon = createBanner(
                "Banners may omit the leading icon when surrounding context already makes the message clear.",
                null,
                "Manage"
        );

        M3Banner passive = createBanner(
                "Passive banners keep persistent contextual information visible without interrupting the current task.",
                null
        );

        M3Banner narrow = createBanner(
                "A narrow banner wraps longer text while keeping actions reachable.",
                createInfoIcon(),
                "Details",
                "Close"
        );
        narrow.setPrefWidth(420.0);

        M3Banner rightToLeft = createBanner(
                "Right-to-left layout mirrors the icon and actions while preserving logical action order.",
                createInfoIcon(),
                "Primary",
                "Secondary"
        );
        rightToLeft.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        return createGallery(
                createShowcaseGroup("With Actions", informational, warning),
                createShowcaseGroup("Without Icon", noIcon),
                createShowcaseGroup("Passive", passive),
                createShowcaseGroup("Responsive And RTL", narrow, rightToLeft)
        );
    }

    /// Creates a sample banner for the page gallery.
    private M3Banner createBanner(String text, @Nullable Node icon, String... actionTexts) {
        M3Banner banner = new M3Banner(text);
        banner.setIcon(icon);
        banner.setPrefWidth(760.0);
        banner.getStyleClass().add("demo-banner");
        for (String actionText : actionTexts) {
            M3Button action = new M3Button(actionText, M3ButtonVariant.TEXT);
            action.setOnAction(event -> showSnackbar(actionText + " pressed"));
            banner.getActions().add(action);
        }
        return banner;
    }

    /// Creates the snackbar component page.
    private Node createSnackbarsPage() {
        M3Button messageButton = new M3Button("Show message", M3ButtonVariant.FILLED);
        messageButton.setOnAction(event -> showSnackbar("Theme-aware snackbar"));
        M3Button actionButton = new M3Button("Show action", M3ButtonVariant.TONAL);
        actionButton.setOnAction(event -> showActionSnackbar());
        M3Button queueButton = new M3Button("Queue messages", M3ButtonVariant.OUTLINED);
        queueButton.setOnAction(event -> showQueuedSnackbars());
        M3Button dismissibleButton = new M3Button("Show dismissible", M3ButtonVariant.ELEVATED);
        dismissibleButton.setOnAction(event -> {
            M3OverlayPane activeOverlay = overlayPane;
            if (activeOverlay == null) {
                return;
            }
            M3Snackbar snackbar = new M3Snackbar("Dismiss this message");
            snackbar.setCloseButtonVisible(true);
            activeOverlay.showSnackbar(snackbar);
        });

        return createGallery(createShowcaseGroup(
                "Snackbar Presentation",
                messageButton,
                actionButton,
                dismissibleButton,
                queueButton
        ));
    }

    /// Creates the form helpers demo page.
    private Node createFormsPage() {
        M3TextField displayName = createTextField("Display name", "", M3TextInputVariant.OUTLINED, false);
        M3TextInputLayout displayNameLayout = createTextInputLayout(displayName, "Visible to collaborators");
        displayNameLayout.setValidator(M3TextInputValidators.required("Display name is required"));
        displayNameLayout.setValidateOnFocusLost(true);

        M3TextField email = createTextField("Email", "support@example.com", M3TextInputVariant.OUTLINED, false);
        M3TextInputLayout emailLayout = createTextInputLayout(email, "Used for project notifications");
        emailLayout.setValidator(M3TextInputValidators.pattern(
                Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                "Enter a valid email address"
        ));
        emailLayout.setValidateOnFocusLost(true);

        M3DateRangePickerField availability =
                new M3DateRangePickerField(LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        availability.setStartLabelText("Start");
        availability.setEndLabelText("End");
        availability.getPresets().setAll(M3DateRangePresets.common(LocalDate.now(), availability.getPicker().getFirstDayOfWeek()));
        availability.setPrefWidth(420.0);
        availability.setMaxWidth(420.0);

        M3Switch notifications = new M3Switch("");
        notifications.setSelected(true);
        M3CheckBox beta = new M3CheckBox();
        beta.setAllowIndeterminate(true);
        beta.setIndeterminate(true);

        M3FormSection account = new M3FormSection(
                "Account",
                "Common fields use the same label column and content alignment."
        );
        account.getContent().addAll(
                new M3FormRow("Display name", "Primary profile label", displayNameLayout),
                new M3FormRow("Email", "Validated on focus loss", emailLayout),
                new M3FormRow("Availability", "Editable start and end dates", availability)
        );

        M3FormSection preferences = new M3FormSection(
                "Preferences",
                "Boolean settings keep labels aligned with selection controls."
        );
        preferences.getContent().addAll(
                new M3FormRow("Notifications", "Receive product and release updates", notifications),
                new M3FormRow("Beta channel", "Tri-state checkbox in a form row", beta)
        );

        M3FormValidator validator = new M3FormValidator(displayNameLayout, emailLayout);
        M3ValidationSummary validationSummary = new M3ValidationSummary(validator);
        validationSummary.setTitleText("Review form fields");
        validationSummary.setEmptyText("All registered fields are valid");
        validationSummary.setPrefWidth(720.0);
        validationSummary.setMaxWidth(720.0);

        M3Button validateButton = new M3Button("Validate form", M3ButtonVariant.FILLED);
        validateButton.setOnAction(event -> {
            if (validator.validateAndFocusFirstInvalidInput()) {
                validationSummary.setShowWhenValid(true);
                showSnackbar("Form is valid");
            } else {
                validationSummary.setShowWhenValid(false);
                int invalidCount = validator.getInvalidInputCount();
                showSnackbar(invalidCount == 1 ? "Fix 1 field" : "Fix " + invalidCount + " fields");
            }
        });

        M3Button clearValidationButton = new M3Button("Clear validation", M3ButtonVariant.OUTLINED);
        clearValidationButton.disableProperty().bind(validator.validationActiveProperty().not());
        clearValidationButton.setOnAction(event -> {
            validator.clearValidation();
            validationSummary.setShowWhenValid(false);
            showSnackbar("Validation cleared");
        });

        HBox validationActions = new HBox(12.0, validateButton, clearValidationButton);
        validationActions.setAlignment(Pos.CENTER_LEFT);

        M3FormSection validation = new M3FormSection(
                "Validation",
                "Group-level validation keeps form feedback and focus movement coordinated."
        );
        validation.getContent().add(new M3FormRow("Actions", "Validate all registered inputs", validationActions));

        validator.validate();

        M3FormPane form = createFormPane(validationSummary, account, preferences, validation);
        form.getStyleClass().add("demo-form");
        form.setContentPadding(18.0);
        form.setPrefWidth(760.0);
        form.setMaxWidth(760.0);

        return createGallery(createShowcaseGroup("Structured Form", form));
    }

    /// Creates the tooltip component page.
    private Node createTooltipsPage() {
        M3Button plain = new M3Button("Hover me", M3ButtonVariant.FILLED);
        M3Tooltip.install(plain, new M3Tooltip("Tooltip"));

        M3Button longText = new M3Button("Long tooltip", M3ButtonVariant.OUTLINED);
        M3Tooltip tooltip = new M3Tooltip("Use tooltips for brief contextual labels when a control needs clarification.");
        tooltip.setPrefWidth(260.0);
        M3Tooltip.install(longText, tooltip);

        M3IconButton iconButton = createIconButton("info");
        M3Tooltip.install(iconButton, new M3Tooltip("Icon button"));

        M3Button rich = new M3Button("Rich tooltip", M3ButtonVariant.TONAL);
        M3Tooltip.install(
                rich,
                new M3RichTooltip(
                        "Rich tooltip",
                        "Use rich tooltips when brief supporting context needs a title and a wider surface."
                )
        );

        M3Button actionButton = new M3Button("Open", M3ButtonVariant.TEXT);
        actionButton.setOnAction(event -> showSnackbar("Theme-aware snackbar"));
        M3Button richAction = new M3Button("Rich action", M3ButtonVariant.OUTLINED);
        M3RichTooltip richActionTooltip = new M3RichTooltip(
                "Generated theme",
                "The tooltip can inherit the owning scene theme and expose action nodes in the content surface."
        );
        richActionTooltip.getActions().add(actionButton);
        M3Tooltip.install(richAction, richActionTooltip);

        M3Button persistent = new M3Button("Persistent rich tooltip", M3ButtonVariant.FILLED);
        M3RichTooltip persistentTooltip = new M3RichTooltip(
                "Try keyboard shortcuts",
                "Persistent rich tooltips open from an explicit click and remain available while their actions are used."
        );
        persistentTooltip.setPersistent(true);
        M3Button learnMore = new M3Button("Learn more", M3ButtonVariant.TEXT);
        M3Button dismiss = new M3Button("Dismiss", M3ButtonVariant.TEXT);
        dismiss.setOnAction(event -> persistentTooltip.hide());
        persistentTooltip.getActions().addAll(learnMore, dismiss);
        M3Tooltip.install(persistent, persistentTooltip);

        return createGallery(
                createShowcaseGroup("Plain", plain, longText, iconButton),
                createShowcaseGroup("Transient Rich", rich, richAction),
                createShowcaseGroup("Persistent Rich", persistent)
        );
    }

    /// Creates a page gallery.
    private static VBox createGallery(Node... groups) {
        VBox gallery = new VBox(18.0);
        gallery.getStyleClass().add("demo-gallery");
        gallery.getChildren().addAll(groups);
        return gallery;
    }

    /// Creates one showcase group.
    private static VBox createShowcaseGroup(String title, Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");

        FlowPane flow = new FlowPane(16.0, 16.0);
        flow.getStyleClass().add("demo-flow");
        flow.setAlignment(Pos.CENTER_LEFT);
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.getChildren().addAll(nodes);

        VBox group = new VBox(10.0, label, flow);
        group.getStyleClass().add("demo-showcase-group");
        group.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    /// Creates one showcase group whose samples use the full content width.
    private static VBox createFullWidthShowcaseGroup(String title, Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");

        VBox stack = new VBox(16.0);
        stack.getStyleClass().add("demo-stacked-flow");
        stack.setFillWidth(true);
        stack.setMaxWidth(Double.MAX_VALUE);
        stack.getChildren().addAll(nodes);

        VBox group = new VBox(10.0, label, stack);
        group.getStyleClass().add("demo-showcase-group");
        group.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    /// Creates the app bar showcase group whose samples are stacked and expanded to the available width.
    private static VBox createAppBarShowcaseGroup(String title, Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");

        VBox stack = new VBox(16.0);
        stack.getStyleClass().addAll("demo-app-bar-stack", "demo-stacked-flow");
        stack.setFillWidth(true);
        stack.setMaxWidth(Double.MAX_VALUE);
        stack.getChildren().addAll(nodes);

        VBox group = new VBox(10.0, label, stack);
        group.getStyleClass().add("demo-showcase-group");
        group.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    /// Applies baseline linear progress geometry to a single demo progress bar.
    private static void applyBaselineProgress(M3ProgressBar progressBar) {
        progressBar.setStyle("-m3-wave-amplitude: 0px;");
    }

    /// Applies baseline circular progress geometry to a single demo progress indicator.
    private static void applyBaselineProgress(M3ProgressIndicator progressIndicator) {
        progressIndicator.setStyle("-m3-wave-amplitude: 0px;");
    }

    /// Applies expressive wavy linear progress geometry to a single demo progress bar.
    private static void applyExpressiveLinearProgress(M3ProgressBar progressBar) {
        progressBar.setStyle("-m3-wave-amplitude: 3px; "
                + "-m3-wavelength: 40px; "
                + "-m3-indeterminate-wavelength: 20px; "
                + "-m3-track-gap: 4px; "
                + "-m3-stop-size: 4px;");
    }

    /// Applies expressive wavy circular progress geometry to a single demo progress indicator.
    private static void applyExpressiveCircularProgress(M3ProgressIndicator progressIndicator) {
        progressIndicator.setStyle("-m3-wave-amplitude: 1.6px; "
                + "-m3-wave-indicator-size: 48px; "
                + "-m3-wavelength: 15px; "
                + "-m3-track-gap: 4px;");
    }

    /// Creates the track height comparison matrix for progress indicators.
    private static VBox createProgressTrackHeightMatrix() {
        VBox matrix = new VBox(
                14.0,
                createProgressTrackHeightRow("Linear standard determinate", false, false, false),
                createProgressTrackHeightRow("Linear standard indeterminate", false, false, true),
                createProgressTrackHeightRow("Linear expressive determinate", false, true, false),
                createProgressTrackHeightRow("Linear expressive indeterminate", false, true, true),
                createProgressTrackHeightRow("Circular standard determinate", true, false, false),
                createProgressTrackHeightRow("Circular standard indeterminate", true, false, true),
                createProgressTrackHeightRow("Circular expressive determinate", true, true, false),
                createProgressTrackHeightRow("Circular expressive indeterminate", true, true, true)
        );
        matrix.setFillWidth(true);
        matrix.setMaxWidth(Double.MAX_VALUE);
        return matrix;
    }

    /// Creates one row in the progress track height comparison matrix.
    private static VBox createProgressTrackHeightRow(
            String title,
            boolean circular,
            boolean expressive,
            boolean indeterminate
    ) {
        Label label = new Label(title);
        label.getStyleClass().add("demo-group-title");

        FlowPane indicators = new FlowPane(16.0, 12.0);
        indicators.setAlignment(Pos.CENTER_LEFT);
        indicators.setPrefWrapLength(760.0);
        for (double trackHeight : PROGRESS_TRACK_HEIGHTS) {
            indicators.getChildren().add(createProgressTrackHeightSample(
                    trackHeight,
                    circular,
                    expressive,
                    indeterminate
            ));
        }

        VBox row = new VBox(8.0, label, indicators);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    /// Creates one labeled progress sample for a requested track height.
    private static VBox createProgressTrackHeightSample(
            double trackHeight,
            boolean circular,
            boolean expressive,
            boolean indeterminate
    ) {
        Node indicator;
        double sampleWidth;
        if (circular) {
            M3ProgressIndicator progressIndicator = indeterminate
                    ? new M3ProgressIndicator()
                    : new M3ProgressIndicator(0.62);
            if (expressive) {
                applyExpressiveCircularProgress(progressIndicator);
            } else {
                applyBaselineProgress(progressIndicator);
            }
            appendInlineStyle(progressIndicator, "-m3-track-thickness: " + trackHeight + "px;");
            indicator = progressIndicator;
            sampleWidth = 64.0;
        } else {
            M3ProgressBar progressBar = indeterminate ? new M3ProgressBar() : new M3ProgressBar(0.62);
            progressBar.setPrefWidth(180.0);
            if (expressive) {
                applyExpressiveLinearProgress(progressBar);
            } else {
                applyBaselineProgress(progressBar);
            }
            appendInlineStyle(progressBar, "-m3-track-thickness: " + trackHeight + "px;");
            indicator = progressBar;
            sampleWidth = 180.0;
        }

        Label heightLabel = new Label((int) trackHeight + " px");
        heightLabel.getStyleClass().add("demo-progress-track-height-label");
        VBox sample = new VBox(6.0, heightLabel, indicator);
        sample.setAlignment(Pos.CENTER_LEFT);
        sample.setMinWidth(sampleWidth);
        sample.setPrefWidth(sampleWidth);
        return sample;
    }

    /// Appends inline CSS to a node while preserving styles already applied by demo helpers.
    private static void appendInlineStyle(Node node, String style) {
        String currentStyle = node.getStyle();
        node.setStyle(currentStyle.isBlank() ? style : currentStyle + " " + style);
    }

    /// Creates a split button configured with the requested variant.
    private M3SplitButton createSplitButton(String text, M3ButtonVariant variant) {
        M3SplitButton splitButton = new M3SplitButton(text);
        splitButton.getItems().addAll(
                new M3MenuItem("Duplicate"),
                new M3MenuItem("Move"),
                new M3MenuItem("Delete")
        );
        splitButton.setVariant(variant);
        splitButton.setOnAction(event -> showSnackbar("Theme-aware snackbar"));
        return splitButton;
    }

    /// Creates a split-button leading icon whose color follows the button variant.
    private static StackPane createSplitButtonGraphic(
            String iconName,
            M3ButtonVariant variant,
            M3IconSize size
    ) {
        SVGPath icon = switch (variant) {
            case FILLED -> DemoIcons.onPrimary(iconName);
            case TONAL -> DemoIcons.onSecondaryContainer(iconName);
            case OUTLINED, TEXT, ELEVATED -> DemoIcons.primary(iconName);
        };
        return createIconViewport(
                icon,
                defaultIconGlyphSize(size),
                "demo-split-button-leading-icon"
        );
    }

    /// Creates a text field for the page gallery.
    private static M3TextField createTextField(
            String prompt,
            String text,
            M3TextInputVariant variant,
            boolean disabled
    ) {
        M3TextField textField = new M3TextField(text);
        textField.setVariant(variant);
        textField.setPromptText(prompt);
        textField.setDisable(disabled);
        textField.setPrefWidth(280.0);
        return textField;
    }

    /// Creates a text area for the page gallery.
    private static M3TextArea createTextArea(
            String prompt,
            String text,
            M3TextInputVariant variant
    ) {
        M3TextArea textArea = new M3TextArea(text);
        textArea.setVariant(variant);
        textArea.setPromptText(prompt);
        textArea.setPrefWidth(360.0);
        return textArea;
    }

    /// Creates a text input layout for the page gallery.
    private static M3TextInputLayout createTextInputLayout(TextInputControl input, String supportingText) {
        M3TextInputLayout layout = new M3TextInputLayout(input, supportingText);
        layout.setLabelText(input.getPromptText());
        input.setPromptText("");
        layout.setPrefWidth(input.getPrefWidth());
        layout.setMaxWidth(input.getPrefWidth());
        if (input.isDisabled()) {
            layout.setDisable(true);
        }
        return layout;
    }

    /// Creates one overview list item.
    private static M3ListItem createOverviewItem(String title, String supportingText) {
        M3ListItem item = new M3ListItem(title);
        item.setSupportingText(supportingText);
        item.setLeading(createNavigationIcon(overviewIconName(title)));
        return item;
    }

    /// Creates a sample search result row.
    private static M3ListItem createSearchResult(String title, String supportingText) {
        M3ListItem item = new M3ListItem(title);
        item.setSupportingText(supportingText);
        item.setLeading(createNavigationIcon(overviewIconName(title)));
        return item;
    }

    /// Returns the demo icon name that best matches a list row title.
    private static String overviewIconName(String title) {
        String normalized = title.toLowerCase(Locale.ROOT);
        if (normalized.contains("button")) {
            return "add";
        } else if (normalized.contains("input") || normalized.contains("text")) {
            return "text";
        } else if (normalized.contains("selection") || normalized.contains("checkbox")) {
            return "check";
        } else if (normalized.contains("navigation")) {
            return "home";
        } else if (normalized.contains("loading") || normalized.contains("progress")) {
            return "schedule";
        } else if (normalized.contains("date") || normalized.contains("time")) {
            return "calendar";
        } else if (normalized.contains("dialog") || normalized.contains("sheet")) {
            return "info";
        } else if (normalized.contains("list") || normalized.contains("surface")) {
            return "label";
        } else if (normalized.contains("menu")) {
            return "menu";
        } else if (normalized.contains("search")) {
            return "search";
        } else if (normalized.contains("profile")) {
            return "person";
        } else if (normalized.contains("settings")) {
            return "settings";
        }
        return "bookmark";
    }

    /// Creates a sample menu item.
    private static M3MenuItem createMenuItem(String text, String iconText, String shortcutText) {
        M3MenuItem item = new M3MenuItem(text);
        item.setLeading(createNavigationIcon(iconText));
        if (!shortcutText.isBlank()) {
            Label shortcut = new Label(shortcutText);
            shortcut.getStyleClass().add("demo-menu-shortcut");
            item.setTrailing(shortcut);
        }
        return item;
    }

    /// Creates a checkbox sample.
    private static M3CheckBox createCheckBox(
            String text,
            boolean selected,
            boolean indeterminate,
            boolean allowIndeterminate,
            boolean error,
            boolean disabled
    ) {
        M3CheckBox checkBox = new M3CheckBox(text);
        checkBox.setSelected(selected);
        checkBox.setIndeterminate(indeterminate);
        checkBox.setAllowIndeterminate(allowIndeterminate);
        checkBox.setError(error);
        checkBox.setDisable(disabled);
        return checkBox;
    }

    /// Creates a slider sample.
    private static M3Slider createSlider(double value, boolean disabled) {
        M3Slider slider = new M3Slider(0.0, 100.0, value);
        slider.setPrefWidth(260.0);
        slider.setDisable(disabled);
        return slider;
    }

    /// Creates a discrete slider sample.
    private static M3Slider createSteppedSlider(double value, double stepSize) {
        M3Slider slider = createSlider(value, false);
        slider.setStepSize(stepSize);
        return slider;
    }

    /// Creates a list pane sample with initial content nodes.
    private static M3ListPane createListPane(Node... items) {
        M3ListPane listPane = new M3ListPane();
        listPane.getItems().addAll(items);
        return listPane;
    }

    /// Creates a button group sample with initial buttons.
    private static M3ButtonGroup createButtonGroup(ButtonBase... buttons) {
        M3ButtonGroup group = new M3ButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates a chip group sample with initial chips.
    private static M3ChipGroup createChipGroup(M3Chip... chips) {
        M3ChipGroup group = new M3ChipGroup();
        group.getItems().addAll(chips);
        return group;
    }

    /// Creates a carousel sample with initial items.
    private static M3Carousel createCarousel(Node... items) {
        M3Carousel carousel = new M3Carousel();
        carousel.getItems().addAll(items);
        return carousel;
    }

    /// Creates a form pane sample with initial items.
    private static M3FormPane createFormPane(Node... items) {
        M3FormPane formPane = new M3FormPane();
        formPane.getItems().addAll(items);
        return formPane;
    }

    /// Creates a segmented button group sample with initial buttons.
    private static M3SegmentedButtonGroup createSegmentedButtonGroup(M3SegmentedButton... buttons) {
        M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates a tab bar sample with initial tabs.
    private static M3TabBar createTabBar(M3Tab... tabs) {
        M3TabBar tabBar = new M3TabBar();
        tabBar.getTabs().addAll(tabs);
        return tabBar;
    }

    /// Creates a surface sample with initial content nodes.
    private static M3Surface createSurface(Node... children) {
        M3Surface surface = new M3Surface();
        surface.getContent().addAll(children);
        return surface;
    }

    /// Creates a navigation bar sample with initial items.
    private static M3NavigationBar createNavigationBar(M3NavigationItem... items) {
        M3NavigationBar navigationBar = new M3NavigationBar();
        navigationBar.getItems().addAll(items);
        return navigationBar;
    }

    /// Creates a navigation rail sample with initial items.
    private static M3NavigationRail createNavigationRail(M3NavigationItem... items) {
        M3NavigationRail navigationRail = new M3NavigationRail();
        navigationRail.getItems().addAll(items);
        return navigationRail;
    }

    /// Creates a navigation drawer sample with initial items.
    private static M3NavigationDrawer createNavigationDrawer(Node... items) {
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer();
        navigationDrawer.getItems().addAll(items);
        return navigationDrawer;
    }

    /// Creates an icon toggle button group sample with initial buttons.
    private static M3IconToggleButtonGroup createIconToggleButtonGroup(M3IconToggleButton... buttons) {
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup();
        group.getItems().addAll(buttons);
        return group;
    }

    /// Creates a segmented button group sample.
    private static M3SegmentedButtonGroup createSegmentedGroup(String first, String second, String third) {
        M3SegmentedButton firstButton = new M3SegmentedButton(first);
        M3SegmentedButton secondButton = new M3SegmentedButton(second);
        secondButton.setSelected(true);
        M3SegmentedButton thirdButton = new M3SegmentedButton(third);
        return createSegmentedButtonGroup(firstButton, secondButton, thirdButton);
    }

    /// Creates a segmented button sample with an 18dp leading SVG icon.
    private static M3SegmentedButton createSegmentedButton(String text, String iconName) {
        return new M3SegmentedButton(
                text,
                createDemoIcon(iconName, M3IconSize.SMALL, M3IconVariant.ON_SURFACE)
        );
    }

    /// Creates a tab bar sample.
    private static M3TabBar createTabBar(String first, String second, String third) {
        M3Tab firstTab = new M3Tab(first);
        firstTab.setSelected(true);
        return createTabBar(firstTab, new M3Tab(second), new M3Tab(third));
    }

    /// Creates a toolbar sample.
    private static M3Toolbar createToolbar(
            M3ToolbarVariant variant,
            M3ToolbarColorStyle colorStyle,
            Orientation orientation,
            String... iconNames
    ) {
        Objects.requireNonNull(iconNames, "iconNames");

        M3Toolbar toolbar = new M3Toolbar();
        toolbar.setVariant(variant);
        toolbar.setColorStyle(colorStyle);
        toolbar.setOrientation(orientation);
        for (int index = 0; index < iconNames.length; index++) {
            String iconName = iconNames[index];
            if (index == 1) {
                M3IconToggleButton selected = createIconToggleButton(
                        iconName,
                        M3IconToggleButtonVariant.STANDARD,
                        true
                );
                selected.setAccessibleText(toolbarIconAccessibleText(iconName));
                toolbar.getItems().add(selected);
            } else {
                M3IconButton button = createToolbarIconButton(iconName);
                button.setDisable(index == iconNames.length - 1);
                toolbar.getItems().add(button);
            }
        }
        return toolbar;
    }

    /// Creates a top app bar sample.
    private static M3TopAppBar createTopAppBar(
            String title,
            M3TopAppBarVariant variant,
            String navigationIcon,
            String... actionIcons
    ) {
        Objects.requireNonNull(actionIcons, "actionIcons");

        M3TopAppBar topAppBar = new M3TopAppBar(title);
        topAppBar.setVariant(variant);
        topAppBar.setNavigation(createLeadingAppBarIconButton(navigationIcon));
        for (String actionIcon : actionIcons) {
            topAppBar.getActions().add(createTrailingAppBarIconButton(actionIcon));
        }
        topAppBar.setMaxWidth(Double.MAX_VALUE);
        return topAppBar;
    }

    /// Creates a preview surface for a top app bar.
    private static VBox createTopAppBarPreview(M3TopAppBar topAppBar) {
        VBox preview = createAppBarPreview();
        preview.getStyleClass().add("demo-top-app-bar-preview");
        preview.getChildren().addAll(topAppBar, createTopAppBarPreviewContent(topAppBar.getTitle()));
        return preview;
    }

    /// Creates a preview surface for a bottom app bar.
    private static VBox createBottomAppBarPreview(M3BottomAppBar bottomAppBar) {
        VBox preview = createAppBarPreview();
        preview.getChildren().add(bottomAppBar);
        return preview;
    }

    /// Creates a preview surface for a toolbar sample.
    private static StackPane createToolbarPreview(M3Toolbar toolbar) {
        StackPane preview = new StackPane(toolbar);
        preview.getStyleClass().add("demo-toolbar-preview");
        preview.setMinWidth(560.0);
        preview.setPrefWidth(760.0);
        preview.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(toolbar, Pos.CENTER_LEFT);
        return preview;
    }

    /// Creates a labeled app bar preview for variant comparison.
    private static VBox createLabeledAppBarPreview(String labelText, Node preview) {
        Label label = new Label(labelText);
        label.getStyleClass().add("demo-app-bar-sample-label");

        VBox sample = new VBox(8.0, label, preview);
        sample.getStyleClass().add("demo-app-bar-sample");
        sample.setFillWidth(true);
        sample.setMaxWidth(Double.MAX_VALUE);
        if (preview instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(region, Priority.NEVER);
        }
        return sample;
    }

    /// Creates the shared app bar preview container.
    private static VBox createAppBarPreview() {
        VBox preview = new VBox();
        preview.getStyleClass().add("demo-app-bar-preview");
        preview.setFillWidth(true);
        preview.setMinWidth(560.0);
        preview.setPrefWidth(760.0);
        preview.setMaxWidth(Double.MAX_VALUE);
        return preview;
    }

    /// Creates the lightweight content area shown below a top app bar preview.
    private static VBox createTopAppBarPreviewContent(String title) {
        VBox content = new VBox(10.0);
        content.getStyleClass().add("demo-top-app-bar-preview-content");
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(
                createTopAppBarPreviewRow(title + " updates", "Updated just now"),
                createTopAppBarPreviewRow("Pinned items", "3 active entries")
        );
        return content;
    }

    /// Creates one content row for a top app bar preview.
    private static HBox createTopAppBarPreviewRow(String title, String supportingText) {
        StackPane leading = new StackPane();
        leading.getStyleClass().add("demo-top-app-bar-preview-leading");
        leading.setMinSize(40.0, 40.0);
        leading.setPrefSize(40.0, 40.0);
        leading.setMaxSize(40.0, 40.0);

        Label headline = new Label(title);
        headline.getStyleClass().add("demo-top-app-bar-preview-headline");
        Label supporting = new Label(supportingText);
        supporting.getStyleClass().add("demo-top-app-bar-preview-supporting");
        VBox text = new VBox(2.0, headline, supporting);
        text.getStyleClass().add("demo-top-app-bar-preview-text");
        HBox.setHgrow(text, Priority.ALWAYS);

        Region trailing = new Region();
        trailing.getStyleClass().add("demo-top-app-bar-preview-trailing");
        trailing.setMinSize(56.0, 12.0);
        trailing.setPrefSize(56.0, 12.0);
        trailing.setMaxSize(56.0, 12.0);

        HBox row = new HBox(16.0, leading, text, trailing);
        row.getStyleClass().add("demo-top-app-bar-preview-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    /// Creates a sample surface.
    private static M3Surface createSurface(
            String title,
            M3SurfaceVariant variant,
            M3SurfaceElevation elevation
    ) {
        M3Text label = new M3Text(title, M3TextRole.TITLE_MEDIUM);
        M3Surface surface = createSurface(label);
        surface.setVariant(variant);
        surface.setElevation(elevation);
        surface.setPrefSize(180.0, 96.0);
        return surface;
    }

    /// Creates a bottom app bar sample.
    private static M3BottomAppBar createBottomAppBar() {
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        bottomAppBar.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.END);
        bottomAppBar.setFloatingAction(
                createFab("add", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR)
        );
        bottomAppBar.getActions().addAll(
                createTrailingAppBarIconButton("search"),
                createTrailingAppBarIconButton("favorite")
        );
        bottomAppBar.setMaxWidth(Double.MAX_VALUE);
        return bottomAppBar;
    }

    /// Creates the four-item navigation bar sample.
    private M3NavigationBar createFourItemNavigationBar() {
        M3NavigationItem firstItem = createNavigationItem("Home", "home");
        M3NavigationItem secondItem = createNavigationItem("Search", "search");
        M3NavigationItem thirdItem = createNavigationItem("Profile", "person");
        M3NavigationItem fourthItem = createNavigationItem("Settings", "settings");
        secondItem.setBadge(new M3Badge("3"));

        M3NavigationBar navigationBar = createNavigationBar(
                firstItem,
                secondItem,
                thirdItem,
                fourthItem
        );
        navigationBar.selectIndex(0);
        return navigationBar;
    }

    /// Creates the four-item navigation rail sample.
    private M3NavigationRail createFourItemNavigationRail() {
        M3NavigationItem firstItem = createNavigationItem("Home", "home");
        M3NavigationItem secondItem = createNavigationItem("Search", "search");
        M3NavigationItem thirdItem = createNavigationItem("Profile", "person");
        M3NavigationItem fourthItem = createNavigationItem("Settings", "settings");
        secondItem.setBadge(new M3Badge());

        M3NavigationRail navigationRail = createNavigationRail(
                firstItem,
                secondItem,
                thirdItem,
                fourthItem
        );
        navigationRail.selectIndex(0);
        return navigationRail;
    }

    /// Creates the four-item navigation drawer sample.
    private static M3NavigationDrawer createFourItemNavigationDrawer() {
        M3ListItem firstItem = createDrawerItem("Inbox", "inbox");
        M3ListItem secondItem = createDrawerItem("Starred", "star");
        M3ListItem thirdItem = createDrawerItem("Sent", "send");
        M3ListItem fourthItem = createDrawerItem("Archive", "archive");
        secondItem.setTrailing(new M3Badge("3"));

        M3NavigationDrawer navigationDrawer = createNavigationDrawer(
                firstItem,
                secondItem,
                new M3Divider(),
                thirdItem,
                fourthItem
        );
        navigationDrawer.selectIndex(0);
        return navigationDrawer;
    }

    /// Creates the sectioned navigation drawer sample.
    private static M3NavigationDrawer createSectionNavigationDrawer() {
        M3NavigationDrawerGroup workspace = new M3NavigationDrawerGroup("Workspace");
        workspace.getHeaderItem().setLeading(createNavigationIcon("dashboard"));
        M3ListItem dashboard = createDrawerItem("Dashboard", "home");
        workspace.getItems().addAll(
                dashboard,
                createDrawerItem("Reports", "reports")
        );
        workspace.setExpanded(true);

        M3NavigationDrawer navigationDrawer = createNavigationDrawer(
                workspace,
                createDrawerItem("Settings", "settings")
        );
        navigationDrawer.select(dashboard);
        return navigationDrawer;
    }

    /// Creates a sample drawer item.
    private static M3ListItem createDrawerItem(String text, String iconName) {
        M3ListItem item = new M3ListItem(text);
        item.setLeading(createNavigationIcon(iconName));
        return item;
    }

    /// Creates a sample navigation item.
    private static M3NavigationItem createNavigationItem(String text, String iconName) {
        return new M3NavigationItem(text, createNavigationIcon(iconName));
    }

    /// Creates a sample button with explicit Material size and shape roles.
    ///
    /// @param text    the button label
    /// @param variant the button color and elevation variant
    /// @param size    the Material button size
    /// @param shape   the resting Material button shape
    /// @return the configured sample button
    private static M3Button createSizedButton(
            String text,
            M3ButtonVariant variant,
            M3ButtonSize size,
            M3ButtonShape shape
    ) {
        M3Button button = new M3Button(text, variant);
        button.setSize(size);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates a sample navigation icon.
    private static Node createNavigationIcon(String iconName) {
        StackPane icon = createSurfaceVariantIcon(iconName);
        icon.getStyleClass().add("demo-navigation-icon");
        return icon;
    }

    /// Creates a 16-pixel vector icon whose paint is supplied by the switch handle state.
    private static SVGPath createSwitchHandleIcon(String iconName) {
        SVGPath icon = DemoIcons.primary(iconName);
        icon.getStyleClass().remove(DemoIcons.PRIMARY_STYLE_CLASS);
        icon.setScaleX(2.0 / 3.0);
        icon.setScaleY(2.0 / 3.0);
        return icon;
    }

    /// Creates the sample icon button.
    private static M3IconButton createIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.primary(iconName));
        return new M3IconButton(icon);
    }

    /// Creates a sample icon button with Material Expressive sizing roles.
    private static M3IconButton createIconButton(
            String iconName,
            M3ButtonSize size,
            M3IconButtonWidth widthRole,
            M3ButtonShape shape
    ) {
        Node icon = createIconViewport(DemoIcons.primary(iconName), defaultIconButtonGlyphSize(size));
        M3IconButton button = new M3IconButton(icon);
        button.setSize(size);
        button.setWidthRole(widthRole);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates a sample icon button for toolbar action slots.
    private static M3IconButton createToolbarIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant(iconName));
        M3IconButton button = new M3IconButton(icon);
        button.setAccessibleText(toolbarIconAccessibleText(iconName));
        return button;
    }

    /// Creates a sample icon button for leading app bar slots.
    private static M3IconButton createLeadingAppBarIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.onSurface(iconName), "demo-app-bar-icon");
        M3IconButton button = new M3IconButton(icon);
        button.setAccessibleText(appBarIconAccessibleText(iconName));
        return button;
    }

    /// Creates a sample icon button for trailing app bar action slots.
    private static M3IconButton createTrailingAppBarIconButton(String iconName) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant(iconName), "demo-app-bar-icon");
        M3IconButton button = new M3IconButton(icon);
        button.setAccessibleText(appBarIconAccessibleText(iconName));
        return button;
    }

    /// Returns the accessible action text used by app bar icon buttons.
    private static String appBarIconAccessibleText(String iconName) {
        return switch (iconName) {
            case "add" -> "Add";
            case "back" -> "Back";
            case "favorite" -> "Favorites";
            case "menu" -> "Menu";
            case "more" -> "More options";
            case "search" -> "Search";
            default -> throw new IllegalArgumentException("Unknown app bar icon: " + iconName);
        };
    }

    /// Returns the accessible action text used by toolbar icon buttons.
    private static String toolbarIconAccessibleText(String iconName) {
        return switch (iconName) {
            case "archive" -> "Archive";
            case "bold" -> "Bold";
            case "edit" -> "Edit";
            case "favorite" -> "Favorite";
            case "home" -> "Home";
            case "italic" -> "Italic";
            case "more" -> "More options";
            case "notifications" -> "Notifications";
            case "person" -> "Account";
            case "search" -> "Search";
            case "settings" -> "Settings";
            case "share" -> "Share";
            case "tune" -> "Tune";
            case "underline" -> "Underline";
            case "visibility" -> "Visibility";
            default -> throw new IllegalArgumentException("Unknown toolbar icon: " + iconName);
        };
    }

    /// Creates the sample toggle icon button.
    private static M3IconToggleButton createIconToggleButton(
            String iconName,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant(iconName));
        M3IconToggleButton button = new M3IconToggleButton(icon);
        button.setVariant(variant);
        button.setSelected(selected);
        return button;
    }

    /// Creates a selected filled favorite toggle icon button with Material Expressive sizing roles.
    private static M3IconToggleButton createExpressiveFavoriteToggleButton(
            M3ButtonSize size,
            M3IconButtonWidth widthRole,
            M3ButtonShape shape
    ) {
        Node icon = createIconViewport(DemoIcons.onSurfaceVariant("favorite"), defaultIconButtonGlyphSize(size));
        M3IconToggleButton button = new M3IconToggleButton(icon);
        button.setVariant(M3IconToggleButtonVariant.FILLED);
        button.setSelected(true);
        button.setSize(size);
        button.setWidthRole(widthRole);
        button.setButtonShape(shape);
        return button;
    }

    /// Creates a sample single-selection toggle icon button group.
    private static M3IconToggleButtonGroup createIconToggleGroup(
            M3IconToggleButtonVariant variant,
            String first,
            String second,
            String third,
            String... rest
    ) {
        M3IconToggleButtonGroup group = createIconToggleButtonGroup(
                createIconToggleButton(first, variant, false),
                createIconToggleButton(second, variant, false),
                createIconToggleButton(third, variant, false)
        );
        for (String iconName : rest) {
            group.getItems().add(createIconToggleButton(iconName, variant, false));
        }
        group.setAllowEmptySelection(false);
        group.selectIndex(0);
        return group;
    }

    /// Creates the formatting multi-selection toggle icon button group.
    private static M3IconToggleButtonGroup createFormattingToggleGroup() {
        M3IconToggleButton firstButton = createIconToggleButton("bold", M3IconToggleButtonVariant.OUTLINED, false);
        M3IconToggleButton secondButton = createIconToggleButton("italic", M3IconToggleButtonVariant.OUTLINED, false);
        M3IconToggleButton thirdButton = createIconToggleButton("underline", M3IconToggleButtonVariant.OUTLINED, false);
        M3IconToggleButtonGroup group = createIconToggleButtonGroup(
                firstButton,
                secondButton,
                thirdButton
        );
        group.setSelectionMode(M3SelectionMode.MULTIPLE);
        group.selectIndex(0);
        group.selectIndex(2);
        return group;
    }

    /// Creates a selectable icon-toggle button group for one Material button-group variant.
    ///
    /// @param variant   the standard or connected group behavior
    /// @param multiple  whether independent multi-selection is allowed
    /// @param iconNames the icon names used by the toggle buttons
    /// @return the configured selectable button group
    private static M3ButtonGroup createToggleButtonGroup(
            M3ButtonGroupVariant variant,
            boolean multiple,
            String... iconNames
    ) {
        M3ButtonGroup group = new M3ButtonGroup();
        group.setVariant(variant);
        List<M3IconToggleButton> buttons = new ArrayList<>(iconNames.length);
        for (int index = 0; index < iconNames.length; index++) {
            M3IconToggleButton button = createIconToggleButton(
                    iconNames[index],
                    M3IconToggleButtonVariant.TONAL,
                    index == 0 || multiple && index == iconNames.length - 1
            );
            buttons.add(button);
            group.getItems().add(button);
        }
        if (!multiple) {
            for (M3IconToggleButton button : buttons) {
                button.setOnAction(event -> {
                    for (M3IconToggleButton candidate : buttons) {
                        candidate.setSelected(candidate == button);
                    }
                });
            }
        }
        return group;
    }

    /// Creates a labeled interactive collapsed or expanded navigation rail preview.
    ///
    /// @param title          the variant title displayed above the preview
    /// @param navigationRail the navigation rail to preview
    /// @param showAction     whether the header includes a floating action button
    /// @return the labeled interactive preview
    private static VBox createNavigationRailPreview(
            String title,
            M3NavigationRail navigationRail,
            boolean showAction
    ) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-group-title");

        boolean initiallyExpanded = navigationRail.isExpanded();
        M3IconButton menuButton = createIconButton(initiallyExpanded ? "close" : "menu");
        menuButton.setAccessibleText(initiallyExpanded ? "Collapse navigation rail" : "Expand navigation rail");
        menuButton.setOnAction(event -> {
            boolean expanded = !navigationRail.isExpanded();
            navigationRail.setExpanded(expanded);
            menuButton.setGraphic(createIconViewport(DemoIcons.primary(expanded ? "close" : "menu")));
            menuButton.setAccessibleText(expanded ? "Collapse navigation rail" : "Expand navigation rail");
        });

        VBox header = new VBox(16.0, menuButton);
        header.setAlignment(Pos.TOP_CENTER);
        if (showAction) {
            M3FloatingActionButton action = createFab(
                    "add",
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            action.setAccessibleText("Create");
            header.getChildren().add(action);
        }
        navigationRail.setHeader(header);

        Node railPresentation = navigationRail;
        if (navigationRail.isHideWhenCollapsed()) {
            M3IconButton revealButton = createIconButton("menu");
            revealButton.setAccessibleText("Show navigation rail");
            revealButton.setVisible(!navigationRail.isExpanded());
            revealButton.setManaged(!navigationRail.isExpanded());
            revealButton.setOnAction(event -> {
                navigationRail.setExpanded(true);
                revealButton.setVisible(false);
                revealButton.setManaged(false);
                menuButton.setGraphic(createIconViewport(DemoIcons.primary("close")));
                menuButton.setAccessibleText("Hide navigation rail");
            });
            menuButton.setOnAction(event -> {
                navigationRail.setExpanded(false);
                revealButton.setVisible(true);
                revealButton.setManaged(true);
                menuButton.setGraphic(createIconViewport(DemoIcons.primary("menu")));
                menuButton.setAccessibleText("Show navigation rail");
            });

            StackPane immersivePresentation = new StackPane(navigationRail, revealButton);
            immersivePresentation.setAlignment(Pos.TOP_LEFT);
            StackPane.setAlignment(navigationRail, Pos.TOP_LEFT);
            StackPane.setAlignment(revealButton, Pos.TOP_LEFT);
            railPresentation = immersivePresentation;
        }

        VBox preview = new VBox(12.0, titleLabel, railPresentation);
        preview.setAlignment(Pos.TOP_LEFT);
        return preview;
    }

    /// Creates a sample floating action button.
    private static M3FloatingActionButton createFab(
            String iconName,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        Node icon = createIconViewport(DemoIcons.fab(iconName), switch (size) {
            case SMALL, REGULAR -> 24.0;
            case MEDIUM -> 28.0;
            case LARGE -> 36.0;
        });
        M3FloatingActionButton button = new M3FloatingActionButton(icon);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a fixed viewport for a primary-colored icon slot.
    private static StackPane createInfoIcon() {
        return createIconViewport(DemoIcons.primary("info"));
    }

    /// Creates a fixed viewport for an on-surface-variant icon slot.
    private static StackPane createSurfaceVariantIcon(String iconName) {
        return createIconViewport(DemoIcons.onSurfaceVariant(iconName));
    }

    /// Creates a fixed viewport for an error-colored icon slot.
    private static StackPane createErrorIcon(String iconName) {
        return createIconViewport(DemoIcons.error(iconName));
    }

    /// Wraps a demo SVG icon in a stable 24 dp viewport.
    private static StackPane createIconViewport(Node icon, String... styleClasses) {
        return createIconViewport(icon, defaultIconGlyphSize(M3IconSize.MEDIUM), styleClasses);
    }

    /// Wraps a demo SVG icon in a stable viewport with the requested icon size.
    private static StackPane createIconViewport(Node icon, double iconSize, String... styleClasses) {
        StackPane viewport = new StackPane(icon);
        viewport.getStyleClass().add(DEMO_VECTOR_ICON_VIEWPORT_STYLE_CLASS);
        viewport.getStyleClass().addAll(styleClasses);
        viewport.setMouseTransparent(true);
        viewport.setStyle(String.format(
                Locale.ROOT,
                "-fx-min-width: %.1fpx; -fx-min-height: %.1fpx; -fx-pref-width: %.1fpx; "
                        + "-fx-pref-height: %.1fpx; -fx-max-width: %.1fpx; -fx-max-height: %.1fpx;",
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize
        ));
        if (icon instanceof SVGPath svgIcon) {
            double scale = iconSize / defaultIconGlyphSize(M3IconSize.MEDIUM);
            svgIcon.setScaleX(scale);
            svgIcon.setScaleY(scale);
        }
        return viewport;
    }

    /// Creates a floating action button menu sample.
    private M3FabMenu createFabMenu() {
        return createFabMenu(
                M3FloatingActionButtonVariant.PRIMARY_CONTAINER,
                M3FloatingActionButtonVariant.PRIMARY_CONTAINER
        );
    }

    /// Creates a floating action button menu sample using one paired color family.
    private M3FabMenu createFabMenu(
            M3FloatingActionButtonVariant actionVariant,
            M3FloatingActionButtonVariant toggleVariant
    ) {
        M3FloatingActionButton create = createExtendedFab(
                "Create",
                "create",
                actionVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        M3FloatingActionButton edit = createExtendedFab(
                "Edit",
                "edit",
                actionVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        M3FloatingActionButton share = createExtendedFab(
                "Share",
                "share",
                actionVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        create.setOnAction(event -> showSnackbar("Created"));
        edit.setOnAction(event -> showSnackbar("Edited"));
        share.setOnAction(event -> showSnackbar("Shared"));
        M3FloatingActionButton toggle = createFab(
                "add",
                toggleVariant,
                M3FloatingActionButtonSize.REGULAR
        );
        M3FabMenu menu = new M3FabMenu(toggle);
        menu.getItems().addAll(create, edit, share);
        return menu;
    }

    /// Creates a sample standalone icon.
    private static M3SVGIcon createDemoIcon(String iconName, M3IconSize size, M3IconVariant variant) {
        M3SVGIcon icon = new M3SVGIcon(DemoIcons.path(iconName), DEMO_ICON_VIEW_BOX);
        icon.setSize(size);
        icon.setVariant(variant);
        icon.getProperties().put(DemoIcons.ICON_NAME_PROPERTY, iconName);
        icon.getStyleClass().add("demo-sample-icon");
        icon.setMouseTransparent(true);
        return icon;
    }

    /// Creates an SVG icon from an official Material Symbols 960-unit source viewport.
    ///
    /// @param size    the semantic rendered size
    /// @param variant the semantic color role
    /// @return the configured Material Symbols icon
    private static M3SVGIcon createMaterialSymbolIcon(M3IconSize size, M3IconVariant variant) {
        M3SVGIcon icon = new M3SVGIcon(MATERIAL_SYMBOL_ADD_PATH, MATERIAL_SYMBOL_VIEW_BOX);
        icon.setSize(size);
        icon.setVariant(variant);
        icon.getProperties().put(DemoIcons.ICON_NAME_PROPERTY, "add");
        icon.getStyleClass().add("demo-sample-icon");
        icon.setMouseTransparent(true);
        return icon;
    }

    /// Returns the default demo glyph size for an icon size role.
    private static double defaultIconGlyphSize(M3IconSize size) {
        return switch (size) {
            case SMALL -> 18.0;
            case MEDIUM -> 24.0;
            case LARGE -> 32.0;
            case EXTRA_LARGE -> 40.0;
        };
    }

    /// Returns the default demo glyph size for an icon button size role.
    private static double defaultIconButtonGlyphSize(M3ButtonSize size) {
        return switch (size) {
            case EXTRA_SMALL -> 20.0;
            case SMALL, MEDIUM -> 24.0;
            case LARGE -> 32.0;
            case EXTRA_LARGE -> 40.0;
        };
    }

    /// Returns the default glyph size for a labeled Material button size.
    ///
    /// @param size the Material button size
    /// @return the labeled button glyph size in pixels
    private static double defaultButtonGlyphSize(M3ButtonSize size) {
        return switch (size) {
            case EXTRA_SMALL, SMALL -> 20.0;
            case MEDIUM -> 24.0;
            case LARGE -> 32.0;
            case EXTRA_LARGE -> 40.0;
        };
    }

    /// Creates a sample extended floating action button with a variant.
    private static M3FloatingActionButton createExtendedFab(
            String text,
            String iconName,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        M3FloatingActionButton button = new M3FloatingActionButton(
                text,
                createIconViewport(DemoIcons.fab(iconName), switch (size) {
                    case SMALL, REGULAR -> 24.0;
                    case MEDIUM -> 28.0;
                    case LARGE -> 36.0;
                })
        );
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a compact sample card.
    private M3Card createSampleCard(String title, String overline, String body, M3CardVariant variant) {
        VBox content = createCardTextContent(title, overline, body);
        M3Card card = new M3Card(content, variant);
        card.setOnAction(event -> showSnackbar("Theme-aware snackbar"));
        card.getStyleClass().add("demo-card");
        card.setPrefSize(280.0, 168.0);
        return card;
    }

    /// Creates a sample card with media, supporting text, and actions.
    private M3Card createMediaCard(String title, String overline, String body, M3CardVariant variant) {
        VBox content = new VBox(12.0);
        content.getStyleClass().add("demo-card-content");

        Region media = new Region();
        media.getStyleClass().add("demo-card-media");
        media.setMinHeight(104.0);
        media.setPrefHeight(104.0);
        media.setMaxHeight(104.0);
        media.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(12.0);
        header.getStyleClass().add("demo-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        M3Avatar avatar = new M3Avatar(title.substring(0, 1));
        avatar.setVariant(M3AvatarVariant.SECONDARY);

        VBox text = createCardTextContent(title, overline, body);
        HBox.setHgrow(text, Priority.ALWAYS);

        header.getChildren().addAll(avatar, text);

        HBox actions = new HBox(8.0);
        actions.getStyleClass().add("demo-card-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        M3Button details = new M3Button("Details", M3ButtonVariant.TEXT);
        details.setOnAction(event -> showSnackbar("Open card details"));
        M3Button open = new M3Button("Open", M3ButtonVariant.TONAL);
        open.setOnAction(event -> showSnackbar("Open card content"));
        actions.getChildren().addAll(details, open);

        content.getChildren().addAll(media, header, actions);

        M3Card card = new M3Card(content, variant);
        card.getStyleClass().add("demo-card");
        card.setPrefSize(360.0, 300.0);
        return card;
    }

    /// Creates the text stack used by demo cards.
    private static VBox createCardTextContent(String title, String overline, String body) {
        Label overlineLabel = new Label(overline);
        overlineLabel.getStyleClass().add("demo-card-overline");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-card-title");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("demo-card-body");
        bodyLabel.setWrapText(true);

        VBox content = new VBox(4.0, overlineLabel, titleLabel, bodyLabel);
        content.getStyleClass().add("demo-card-text");
        return content;
    }

    /// Creates a sample carousel card.
    private M3Card createCarouselCard(
            String title,
            String body,
            String iconName,
            double width,
            double height
    ) {
        VBox content = new VBox(4.0);
        content.getStyleClass().add("demo-carousel-card-content");
        content.setAlignment(Pos.CENTER);

        StackPane icon = createIconViewport(DemoIcons.secondary(iconName), 48.0);
        icon.getStyleClass().add("demo-carousel-card-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-card-title");
        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("demo-card-body");

        VBox copy = new VBox(2.0, titleLabel, bodyLabel);
        copy.getStyleClass().add("demo-carousel-card-copy");
        copy.setAlignment(Pos.CENTER);
        copy.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        content.getChildren().addAll(icon, spacer, copy);
        M3Card card = new M3Card(content, M3CardVariant.FILLED);
        card.getStyleClass().add("demo-carousel-card");
        card.setOnAction(event -> showSnackbar("Theme-aware snackbar"));
        card.setPrefSize(width, height);
        return card;
    }

    /// Creates sample sheet content.
    private static VBox createSheetContent() {
        M3ListItem first = new M3ListItem("Overview");
        first.setSupportingText("Primary sheet content");
        first.setLeading(createNavigationIcon("info"));
        M3ListItem second = new M3ListItem("Activity");
        second.setSupportingText("Recent updates and state");
        second.setLeading(createNavigationIcon("schedule"));
        M3ListItem third = new M3ListItem("Settings");
        third.setLeading(createNavigationIcon("settings"));

        VBox content = new VBox(first, second, third);
        content.getStyleClass().add("demo-sheet-content");
        return content;
    }

    /// Creates a sample scrim preview.
    private StackPane createScrimPreview(boolean actionEnabled) {
        Label content = new Label(actionEnabled ? "Click scrim" : "Modal content");
        content.getStyleClass().add("demo-scrim-content");

        M3Scrim scrim = new M3Scrim();
        scrim.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        if (actionEnabled) {
            scrim.setOnAction(event -> showSnackbar("Theme-aware snackbar"));
        }

        StackPane preview = new StackPane(content, scrim);
        preview.getStyleClass().add("demo-scrim-preview");
        preview.setMinSize(360.0, 180.0);
        preview.setPrefSize(360.0, 180.0);
        preview.setMaxSize(360.0, 180.0);
        return preview;
    }

    /// Plays the determinate progress showcase animation.
    private void playProgressShowcaseAnimation(M3ProgressBar progressBar, M3ProgressIndicator progressIndicator) {
        Timeline animation = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    progressBar.setProgress(0.08);
                    progressIndicator.setProgress(0.08);
                }),
                new KeyFrame(Duration.seconds(1.4), event -> {
                    progressBar.setProgress(0.86);
                    progressIndicator.setProgress(0.86);
                }),
                new KeyFrame(Duration.seconds(2.8), event -> {
                    progressBar.setProgress(0.24);
                    progressIndicator.setProgress(0.24);
                }),
                new KeyFrame(Duration.seconds(4.2), event -> {
                    progressBar.setProgress(0.68);
                    progressIndicator.setProgress(0.68);
                }),
                new KeyFrame(Duration.seconds(5.6), event -> {
                    progressBar.setProgress(0.08);
                    progressIndicator.setProgress(0.08);
                })
        );
        animation.setCycleCount(Animation.INDEFINITE);
        animations.add(animation);
        if (animationsEnabled) {
            animation.play();
        }
    }

    /// Opens the demo dialog.
    private void showDemoDialog() {
        M3Dialog<ButtonType> dialog = createDemoDialog(
                "Dialog title",
                "This dialog uses the M3FX dialog pane style and active theme tokens.",
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE)
        );
        dialog.show();
    }

    /// Opens a demo dialog with form-like content.
    private void showSettingsDialog() {
        M3Dialog<ButtonType> dialog = createDemoDialog(
                "Project settings",
                null,
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
                new ButtonType("Apply", ButtonBar.ButtonData.APPLY)
        );
        dialog.getDialogPane().setContent(createDialogSettingsContent(true));
        dialog.getDialogPane().setPrefWidth(460.0);
        dialog.show();
    }

    /// Opens a demo dialog for a destructive confirmation flow.
    private void showDestructiveDialog() {
        ButtonType delete = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        M3Dialog<ButtonType> dialog = createDemoDialog(
                "Delete draft?",
                "Deleting this local draft cannot be undone. Published project files are not affected.",
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
                delete
        );
        dialog.getDialogPane().setGraphic(createErrorIcon("warning"));
        dialog.show();
    }

    /// Creates a demo dialog and initializes its owner from the active scene.
    private M3Dialog<ButtonType> createDemoDialog(
            String headerText,
            @Nullable String contentText,
            ButtonType... buttonTypes
    ) {
        M3Dialog<ButtonType> dialog = new M3Dialog<>();
        M3DialogPane pane = dialog.getDialogPane();
        pane.setHeaderText(headerText);
        pane.setContentText(contentText == null ? "" : contentText);
        pane.getButtonTypes().addAll(buttonTypes);
        initDialogOwner(dialog);
        return dialog;
    }

    /// Opens a date picker dialog and reports the accepted date.
    private void showDatePickerDialog(LocalDate initialDate) {
        M3DatePickerDialog dialog = new M3DatePickerDialog(initialDate);
        dialog.getPresets().setAll(M3DatePresets.common(initialDate));
        initDialogOwner(dialog);
        dialog.setOnHidden(event -> {
            LocalDate result = dialog.getResult();
            if (result != null) {
                showSnackbar("Selected date " + result);
            }
        });
        dialog.show();
    }

    /// Opens a date range picker dialog and reports the accepted range.
    private void showDateRangePickerDialog(LocalDate startDate, LocalDate endDate) {
        M3DateRangePickerDialog dialog = new M3DateRangePickerDialog(startDate, endDate);
        initDialogOwner(dialog);
        dialog.setOnHidden(event -> {
            M3DateRange result = dialog.getResult();
            if (result != null) {
                showSnackbar("Selected range " + result.startDate() + " to " + result.endDate());
            }
        });
        dialog.show();
    }

    /// Opens a date range picker dialog with common range presets.
    private void showPresetDateRangePickerDialog(LocalDate anchorDate) {
        M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
        dialog.getPicker().setMinDate(anchorDate.minusMonths(1));
        dialog.getPicker().setMaxDate(anchorDate.plusMonths(3));
        dialog.getPresets().setAll(M3DateRangePresets.common(anchorDate, dialog.getPicker().getFirstDayOfWeek()));
        initDialogOwner(dialog);
        dialog.setOnHidden(event -> {
            M3DateRange result = dialog.getResult();
            if (result != null) {
                showSnackbar("Selected preset range " + result.startDate() + " to " + result.endDate());
            }
        });
        dialog.show();
    }

    /// Opens a time picker dialog and reports the accepted time.
    private void showTimePickerDialog(LocalTime initialTime) {
        M3TimePickerDialog dialog = new M3TimePickerDialog(initialTime);
        dialog.getPicker().setUse24HourClock(true);
        dialog.getPicker().setMinuteStep(15);
        dialog.getPresets().setAll(M3TimePresets.common(initialTime));
        initDialogOwner(dialog);
        dialog.setOnHidden(event -> {
            LocalTime result = dialog.getResult();
            if (result != null) {
                showSnackbar("Selected time " + result);
            }
        });
        dialog.show();
    }

    /// Initializes a dialog owner from the active demo scene.
    private void initDialogOwner(M3Dialog<?> dialog) {
        Scene activeScene = scene;
        if (activeScene != null) {
            dialog.setOwner(activeScene.getRoot());
        }
    }

    /// Shows a demo snackbar message.
    private void showSnackbar(String message) {
        M3OverlayPane activeOverlay = overlayPane;
        if (activeOverlay == null) {
            return;
        }
        activeOverlay.showSnackbar(new M3Snackbar(message));
    }

    /// Shows the demo snackbar with an action.
    private void showActionSnackbar() {
        M3OverlayPane activeOverlay = overlayPane;
        if (activeOverlay == null) {
            return;
        }

        M3Snackbar snackbar = new M3Snackbar("Theme-aware snackbar");
        snackbar.setActionText("Action");
        snackbar.setAction(() -> activeOverlay.enqueueSnackbar(new M3Snackbar("Action pressed")));
        activeOverlay.showSnackbar(snackbar);
    }

    /// Shows multiple demo snackbars through the host queue.
    private void showQueuedSnackbars() {
        M3OverlayPane activeOverlay = overlayPane;
        if (activeOverlay == null) {
            return;
        }

        activeOverlay.enqueueSnackbar(new M3Snackbar("First queued message"));
        M3Snackbar second = new M3Snackbar("Second queued message");
        second.setActionText("Undo");
        second.setAction(() -> activeOverlay.enqueueSnackbar(new M3Snackbar("Undo pressed")));
        activeOverlay.enqueueSnackbar(second);
        activeOverlay.enqueueSnackbar(new M3Snackbar("Third queued message"));
    }

    /// Applies the current theme to the scene.
    private void applyTheme() {
        Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }

        M3ThemeManager.install(activeScene, M3Theme.fromSeed(
                seedColor,
                profile,
                brightness,
                M3Density.of(densityScale)
        ));
    }

    /// Applies the current demo animation switch to the active scene.
    private void applyMotionSettings() {
        Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }

        M3MotionSettings.setReducedMotionRequested(activeScene.getRoot(), !animationsEnabled);
        updatePageAnimations();
    }

    /// Returns the next density scale for the header toggle.
    private double nextDensityScale() {
        if (densityScale < 0.0) {
            return 0.0;
        }
        if (densityScale == 0.0) {
            return 1.0;
        }
        return -1.0;
    }

    /// Returns the display label for the current density scale.
    private String densityLabel() {
        if (densityScale < 0.0) {
            return "Compact";
        }
        if (densityScale > 0.0) {
            return "Comfort";
        }
        return "Standard";
    }

    /// Returns the demo stylesheet URL.
    private static String demoStylesheetUrl() {
        URL url = M3FXDemoApp.class.getResource("/org/glavo/m3fx/demo/m3fx-demo.css");
        if (url == null) {
            throw new IllegalStateException("Missing demo stylesheet resource");
        }
        return url.toExternalForm();
    }

    /// Converts a color to a hexadecimal CSS value.
    private static String toHex(Color color) {
        Objects.requireNonNull(color, "color");
        return "#"
                + toHexChannel(color.getRed())
                + toHexChannel(color.getGreen())
                + toHexChannel(color.getBlue());
    }

    /// Converts a color channel to a two-character hexadecimal value.
    private static String toHexChannel(double value) {
        String hex = Integer.toHexString((int) Math.round(value * 255.0));
        return hex.length() == 1 ? "0" + hex : hex;
    }

    /// Describes one sidebar group and its drawer controls.
    @NotNullByDefault
    private static final class SidebarGroup {
        /// The group title displayed in the sidebar.
        private final String title;

        /// The pages that belong to the group.
        private final @Unmodifiable List<DemoPage> pages;

        /// The drawer group used for collapsible sidebar sections.
        private @Nullable M3NavigationDrawerGroup drawerGroup = null;

        /// The direct list item used for non-collapsible sidebar sections.
        private @Nullable M3ListItem topLevelItem = null;

        /// Creates a sidebar group.
        private SidebarGroup(String title, List<DemoPage> pages) {
            this.title = Objects.requireNonNull(title, "title");
            if (pages.isEmpty()) {
                throw new IllegalArgumentException("pages must not be empty");
            }
            this.pages = List.copyOf(pages);
        }

        /// Updates selected state for all rendered items in this sidebar group.
        private void updateSelection(@Nullable DemoPage page) {
            M3NavigationDrawerGroup drawerGroup = this.drawerGroup;
            if (drawerGroup != null) {
                boolean containsCurrentPage = page != null && pages.contains(page);
                boolean expanded = drawerGroup.isExpanded();
                drawerGroup.getHeaderItem().setSelected(containsCurrentPage && !expanded);
                for (M3ListItem item : drawerGroup.getItems()) {
                    item.setSelected(containsCurrentPage && expanded && item.getUserData() == page);
                }
                return;
            }

            M3ListItem topLevelItem = this.topLevelItem;
            if (topLevelItem != null) {
                topLevelItem.setSelected(topLevelItem.getUserData() == page);
            }
        }

        /// Returns the rendered item for a page in this sidebar group.
        private @Nullable M3ListItem itemForPage(DemoPage page) {
            M3NavigationDrawerGroup drawerGroup = this.drawerGroup;
            if (drawerGroup != null) {
                for (M3ListItem item : drawerGroup.getItems()) {
                    if (item.getUserData() == page) {
                        return item;
                    }
                }
                return null;
            }

            M3ListItem topLevelItem = this.topLevelItem;
            if (topLevelItem != null && topLevelItem.getUserData() == page) {
                return topLevelItem;
            }
            return null;
        }

        /// Returns the currently selected rendered item in this sidebar group.
        private @Nullable M3ListItem selectedItem() {
            M3NavigationDrawerGroup drawerGroup = this.drawerGroup;
            if (drawerGroup != null) {
                if (drawerGroup.getHeaderItem().isSelected()) {
                    return drawerGroup.getHeaderItem();
                }
                for (M3ListItem item : drawerGroup.getItems()) {
                    if (item.isSelected()) {
                        return item;
                    }
                }
                return null;
            }

            M3ListItem topLevelItem = this.topLevelItem;
            return topLevelItem != null && topLevelItem.isSelected() ? topLevelItem : null;
        }
    }

    /// Describes one demo component page.
    ///
    /// @param title           the page title
    /// @param navigationTitle the page title displayed in the sidebar
    /// @param sidebarSection  the sidebar section containing this page
    /// @param subtitle        the page subtitle
    /// @param materialUrl     the Material Design documentation URL for the page or its closest related guidance
    /// @param contentFactory  the factory used to create page content on demand
    @NotNullByDefault
    private record DemoPage(
            String title,
            String navigationTitle,
            String sidebarSection,
            String subtitle,
            String materialUrl,
            Supplier<Node> contentFactory
    ) {
        /// Creates a demo page descriptor with a sidebar title, section, and documentation URL.
        private DemoPage(
                String title,
                String navigationTitle,
                String sidebarSection,
                String subtitle,
                String materialUrl,
                Supplier<Node> contentFactory
        ) {
            this.title = Objects.requireNonNull(title, "title");
            this.navigationTitle = Objects.requireNonNull(navigationTitle, "navigationTitle");
            this.sidebarSection = Objects.requireNonNull(sidebarSection, "sidebarSection");
            this.subtitle = Objects.requireNonNull(subtitle, "subtitle");
            this.materialUrl = Objects.requireNonNull(materialUrl, "materialUrl");
            this.contentFactory = Objects.requireNonNull(contentFactory, "contentFactory");
        }

    }
}
