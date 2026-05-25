// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSettings;
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
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3FormRow;
import org.glavo.m3fx.controls.M3FormSection;
import org.glavo.m3fx.controls.M3FormValidator;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3IconToggleButtonSelectionMode;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListSelectionMode;
import org.glavo.m3fx.controls.M3ListItemSlotSize;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3MenuSectionHeader;
import org.glavo.m3fx.controls.M3MenuSelectionMode;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3PasswordField;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SegmentedButtonSelectionMode;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SnackbarHost;
import org.glavo.m3fx.controls.M3SplitButton;
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
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputValidators;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.controls.M3TimePickerDialog;
import org.glavo.m3fx.controls.M3TimePickerField;
import org.glavo.m3fx.controls.M3TimePresets;
import org.glavo.m3fx.controls.M3Tooltip;
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

    /// The sidebar section for demo pages absent from the Material components navigation drawer.
    private static final String ADDITIONAL_DEMOS_GROUP = "Additional demos";

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

    /// The navigation drawer used by the demo sidebar.
    private @Nullable M3NavigationDrawer sidebarDrawer;

    /// The currently shown demo page.
    private @Nullable DemoPage currentPage;

    /// The page host replaced when sidebar selection changes.
    private @Nullable StackPane pageHost;

    /// The snackbar host used by demo actions.
    private @Nullable M3SnackbarHost snackbarHost;

    /// Starts the demo application.
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("demo-root");

        M3SnackbarHost snackbarHost = new M3SnackbarHost();
        this.snackbarHost = snackbarHost;

        List<DemoPage> createdPages = createPages();
        pages = createdPages;
        StackPane centerStack = new StackPane(createContent(createdPages), snackbarHost);
        StackPane.setAlignment(snackbarHost, Pos.BOTTOM_CENTER);

        root.setTop(createHeader());
        root.setCenter(centerStack);

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

        M3Button profileButton = new M3Button("Expressive");
        profileButton.setVariant(M3ButtonVariant.OUTLINED);
        profileButton.setOnAction(event -> {
            profile = profile == M3Profile.BASELINE_2021 ? M3Profile.EXPRESSIVE_2025 : M3Profile.BASELINE_2021;
            profileButton.setText(profile == M3Profile.BASELINE_2021 ? "Expressive" : "Baseline");
            applyTheme();
        });

        M3Button brightnessButton = new M3Button("Dark");
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
                new DemoPage("Components Overview", "Components overview", COMPONENTS_OVERVIEW_GROUP, "Browse the implemented Material Design 3 component demos", this::createComponentsOverviewPage),
                new DemoPage("App Bars", "App bars", APP_BARS_GROUP, "Top app bars with navigation and actions", this::createAppBarsPage),
                new DemoPage("Badges", "Badges", "Badges", "Dot, count, overflow, and attached badges", this::createBadgesPage),
                new DemoPage("Button Groups", "Button groups", BUTTONS_GROUP, "Connected groups for related actions", this::createButtonGroupsPage),
                new DemoPage("Buttons", "Buttons", BUTTONS_GROUP, "Common button variants", this::createButtonsPage),
                new DemoPage("Extended FABs", "Extended FABs", BUTTONS_GROUP, "Extended floating action button examples", this::createExtendedFabsPage),
                new DemoPage("FAB Menu", "FAB menu", BUTTONS_GROUP, "Expandable floating action shortcuts", this::createFabMenuPage),
                new DemoPage("Floating Action Buttons", "Floating action buttons (FABs)", BUTTONS_GROUP, "Floating action button sizes and variants", this::createFloatingActionButtonsPage),
                new DemoPage("Icon Buttons", "Icon buttons", BUTTONS_GROUP, "Icon button and toggle icon button states", this::createIconButtonsPage),
                new DemoPage("Segmented Buttons", "Segmented buttons", BUTTONS_GROUP, "Single- and multi-select segmented control states", this::createSegmentedButtonsPage),
                new DemoPage("Split Buttons", "Split buttons", BUTTONS_GROUP, "Primary actions with attached menus", this::createSplitButtonsPage),
                new DemoPage("Cards", "Cards", "Cards", "Filled, outlined, elevated, and interactive cards", this::createCardsPage),
                new DemoPage("Carousel", "Carousel", "Carousel", "Horizontal content browsing with selected-item snapping", this::createCarouselPage),
                new DemoPage("Checkboxes", "Checkbox", "Checkbox", "Checked, unchecked, indeterminate, and disabled states", this::createCheckboxesPage),
                new DemoPage("Chips", "Chips", "Chips", "Assist, filter, input, suggestion, and disabled chips", this::createChipsPage),
                new DemoPage("Date Pickers", "Date pickers", DATE_TIME_PICKERS_GROUP, "Calendar date selection, ranges, and month visibility", this::createDatePickersPage),
                new DemoPage("Time Pickers", "Time pickers", DATE_TIME_PICKERS_GROUP, "12-hour, 24-hour, and bounded time selection", this::createTimePickersPage),
                new DemoPage("Dialogs", "Dialogs", "Dialogs", "Dialog pane with themed actions", this::createDialogsPage),
                new DemoPage("Dividers", "Divider", "Divider", "Full-width, inset, middle inset, and vertical dividers", this::createDividersPage),
                new DemoPage("Lists", "Lists", "Lists", "One-line, two-line, three-line, and selected rows", this::createListPage),
                new DemoPage("Loading Indicator", "Loading indicator", LOADING_PROGRESS_GROUP, "Indeterminate loading indicators", this::createLoadingIndicatorPage),
                new DemoPage("Progress", "Progress indicators", LOADING_PROGRESS_GROUP, "Linear and circular progress indicators", this::createProgressPage),
                new DemoPage("Menus", "Menus", "Menus", "Menu surfaces, actions, and menu buttons", this::createMenusPage),
                new DemoPage("Navigation", "Navigation bar", NAVIGATION_GROUP, "Bottom navigation items and selected indicators", this::createNavigationPage),
                new DemoPage("Navigation Drawer", "Navigation drawer", NAVIGATION_GROUP, "Drawer destinations with selected rows", this::createNavigationDrawerPage),
                new DemoPage("Navigation Rail", "Navigation rail", NAVIGATION_GROUP, "Vertical destinations for wide layouts", this::createNavigationRailPage),
                new DemoPage("Radio Buttons", "Radio button", "Radio button", "Grouped single selection states", this::createRadioButtonsPage),
                new DemoPage("Search", "Search", "Search", "Search bars, actions, and result surfaces", this::createSearchPage),
                new DemoPage("Bottom Sheets", "Bottom sheets", SHEETS_GROUP, "Bottom sheet containment surfaces", this::createBottomSheetsPage),
                new DemoPage("Side Sheets", "Side sheets", SHEETS_GROUP, "Side sheet containment surfaces", this::createSideSheetsPage),
                new DemoPage("Sliders", "Sliders", "Sliders", "Different values and disabled slider states", this::createSlidersPage),
                new DemoPage("Snackbars", "Snackbar", "Snackbar", "Snackbar host with action and queued messages", this::createSnackbarsPage),
                new DemoPage("Switches", "Switch", "Switch", "On, off, and disabled switch states", this::createSwitchesPage),
                new DemoPage("Tabs", "Tabs", "Tabs", "Primary tabs with animated active indicators", this::createTabsPage),
                new DemoPage("Text Fields", "Text fields", "Text fields", "Filled, outlined, populated, error, and disabled fields", this::createTextFieldsPage),
                new DemoPage("Toolbars", "Toolbars", "Toolbars", "Bottom app bars with actions and floating actions", this::createBottomAppBarsPage),
                new DemoPage("Tooltips", "Tooltips", "Tooltips", "Plain and longer contextual help", this::createTooltipsPage),
                new DemoPage("Banners", "Banners", ADDITIONAL_DEMOS_GROUP, "Persistent inline feedback with optional actions", this::createBannersPage),
                new DemoPage("Forms", "Forms", ADDITIONAL_DEMOS_GROUP, "Form rows and sections for structured input", this::createFormsPage),
                new DemoPage("Typography", "Typography", ADDITIONAL_DEMOS_GROUP, "Token-driven Material type roles", this::createTypographyPage),
                new DemoPage("Icons", "Icons", ADDITIONAL_DEMOS_GROUP, "Size roles and semantic icon colors", this::createIconsPage),
                new DemoPage("Avatars", "Avatars", ADDITIONAL_DEMOS_GROUP, "Initials and graphic avatar slots", this::createAvatarsPage),
                new DemoPage("Surfaces", "Surfaces", ADDITIONAL_DEMOS_GROUP, "Color containers, shape, padding, and elevation", this::createSurfacesPage),
                new DemoPage("Scrims", "Scrims", ADDITIONAL_DEMOS_GROUP, "Modal overlays and dismiss actions", this::createScrimsPage)
        );
    }

    /// Creates the main content shell with sidebar and page host.
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
        M3ScrollPanes.enableSmoothScrolling(scrollPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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
            if (group.isCollapsible()) {
                M3NavigationDrawerGroup drawerGroup = createSidebarDrawerGroup(group);
                group.setDrawerGroup(drawerGroup);
                sidebar.getItems().add(drawerGroup);
            } else {
                M3ListItem item = createSidebarPageItem(group.firstPage(), false);
                group.setTopLevelItem(item);
                sidebar.getItems().add(item);
            }
        }
        refreshSidebarSelection();
    }

    /// Creates a collapsible sidebar group control.
    private M3NavigationDrawerGroup createSidebarDrawerGroup(SidebarGroup group) {
        M3NavigationDrawerGroup drawerGroup = new M3NavigationDrawerGroup(group.title());
        drawerGroup.getHeaderItem().getStyleClass().add("demo-sidebar-group-item");
        drawerGroup.getHeaderItem().setUserData(group);
        drawerGroup.expandedProperty().addListener((observable, oldValue, newValue) -> refreshSidebarSelection());
        for (DemoPage page : group.pages()) {
            drawerGroup.addItem(createSidebarPageItem(page, true));
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

        Label title = new Label(page.title());
        title.getStyleClass().add("demo-page-title");
        Label subtitle = new Label(page.subtitle());
        subtitle.getStyleClass().add("demo-page-subtitle");
        subtitle.setWrapText(true);

        pageNode.getChildren().addAll(title, subtitle, page.createContent());
        host.getChildren().setAll(pageNode);
    }

    /// Recreates the current page so resolved runtime settings affect active controls immediately.
    private void refreshCurrentPage() {
        DemoPage page = currentPage;
        if (page != null) {
            showPage(page);
        }
    }

    /// Returns the demo page titles created for this application instance.
    @Unmodifiable List<String> demoPageTitlesForTesting() {
        return pages.stream().map(DemoPage::title).toList();
    }

    /// Shows the demo page with the requested title.
    void showPageForTesting(String title) {
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

    /// Returns the active scene for visual tests.
    @Nullable Scene sceneForTesting() {
        return scene;
    }

    /// Applies a demo theme mode directly for visual tests.
    void setThemeModeForTesting(M3Profile profile, Brightness brightness) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.brightness = Objects.requireNonNull(brightness, "brightness");
        applyTheme();
        applyMotionSettings();
        refreshCurrentPage();
    }

    /// Expands the collapsible sidebar group containing the requested page.
    private void expandSidebarGroupForPage(DemoPage page) {
        for (SidebarGroup group : sidebarGroups) {
            M3NavigationDrawerGroup drawerGroup = group.drawerGroup();
            if (drawerGroup != null && group.pages().contains(page)) {
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
        M3ListPane primaryComponents = new M3ListPane(
                createOverviewItem("App bars", "Top and bottom app bars for persistent actions."),
                createOverviewItem("Buttons", "Common actions, icon buttons, split buttons, and FABs."),
                createOverviewItem("Text fields", "Filled, outlined, validation, and supporting text patterns."),
                createOverviewItem("Selection", "Checkbox, radio button, switch, chips, and segmented controls."),
                createOverviewItem("Navigation", "Navigation bar, rail, drawer, and tabs.")
        );
        primaryComponents.getStyleClass().add("demo-overview-list");
        primaryComponents.setMaxWidth(720.0);

        M3ListPane feedbackComponents = new M3ListPane(
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

    /// Creates the button group component page.
    private Node createButtonGroupsPage() {
        M3ButtonGroup tonalGroup = new M3ButtonGroup(
                createButton("Edit", M3ButtonVariant.TONAL),
                createButton("Share", M3ButtonVariant.TONAL),
                createButton("Archive", M3ButtonVariant.TONAL)
        );

        M3ButtonGroup outlinedGroup = new M3ButtonGroup(
                createButton("Day", M3ButtonVariant.OUTLINED),
                createButton("Week", M3ButtonVariant.OUTLINED),
                createButton("Month", M3ButtonVariant.OUTLINED)
        );

        M3Button disabled = createButton("Disabled", M3ButtonVariant.FILLED);
        disabled.setDisable(true);
        M3ButtonGroup filledGroup = new M3ButtonGroup(
                createButton("Accept", M3ButtonVariant.FILLED),
                createButton("Review", M3ButtonVariant.FILLED),
                disabled
        );

        return createGallery(
                createShowcaseGroup("Tonal Group", tonalGroup),
                createShowcaseGroup("Outlined Group", outlinedGroup),
                createShowcaseGroup("Filled Group", filledGroup)
        );
    }

    /// Creates the button component page.
    private Node createButtonsPage() {
        M3Button disabledFilled = createButton("Disabled", M3ButtonVariant.FILLED);
        disabledFilled.setDisable(true);

        return createGallery(
                createShowcaseGroup(
                        "Button Variants",
                        createButton("Filled", M3ButtonVariant.FILLED),
                        createButton("Tonal", M3ButtonVariant.TONAL),
                        createButton("Outlined", M3ButtonVariant.OUTLINED),
                        createButton("Text", M3ButtonVariant.TEXT),
                        createButton("Elevated", M3ButtonVariant.ELEVATED),
                        disabledFilled
                )
        );
    }

    /// Creates the extended floating action button component page.
    private Node createExtendedFabsPage() {
        return createGallery(
                createShowcaseGroup(
                        "Extended FABs",
                        createExtendedFab(),
                        createExtendedFab("Compose", M3FloatingActionButtonVariant.PRIMARY),
                        createExtendedFab("Upload", M3FloatingActionButtonVariant.SECONDARY)
                )
        );
    }

    /// Creates the floating action button menu component page.
    private Node createFabMenuPage() {
        M3FabMenu expanded = createFabMenu();
        expanded.setExpanded(true);

        M3FabMenu collapsed = createFabMenu();
        M3FabMenu secondary = createFabMenu(
                M3FloatingActionButtonVariant.SECONDARY,
                M3FloatingActionButtonVariant.TERTIARY
        );
        secondary.setExpanded(true);

        return createGallery(
                createShowcaseGroup("Expanded", expanded),
                createShowcaseGroup("Collapsed", collapsed),
                createShowcaseGroup("Variants", secondary)
        );
    }

    /// Creates the floating action button component page.
    private Node createFloatingActionButtonsPage() {
        return createGallery(
                createShowcaseGroup(
                        "Floating Action Buttons",
                        createFab("+", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.SMALL),
                        createFab("+", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR),
                        createFab("*", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.LARGE)
                )
        );
    }

    /// Creates the icon button component page.
    private Node createIconButtonsPage() {
        M3IconButton disabledIcon = createIconButton("i");
        disabledIcon.setDisable(true);

        return createGallery(
                createShowcaseGroup(
                        "Icon Buttons",
                        createIconButton("i"),
                        createIconButton("+"),
                        disabledIcon
                ),
                createShowcaseGroup(
                        "Toggle Icon Buttons",
                        createIconToggleGroup(
                                M3IconToggleButtonVariant.STANDARD,
                                "S",
                                "F",
                                "T",
                                "O"
                        ),
                        createIconToggleGroup(
                                M3IconToggleButtonVariant.TONAL,
                                "1",
                                "2",
                                "3"
                        ),
                        createIconToggleMultiGroup(
                                M3IconToggleButtonVariant.OUTLINED,
                                "B",
                                "I",
                                "U"
                        ),
                        createIconToggleButton("D", M3IconToggleButtonVariant.TONAL, false)
                )
        );
    }

    /// Creates the typography component page.
    private Node createTypographyPage() {
        return createGallery(
                createShowcaseGroup(
                        "Scale",
                        new M3Text("Display Large", M3TextRole.DISPLAY_LARGE),
                        new M3Text("Headline Medium", M3TextRole.HEADLINE_MEDIUM),
                        new M3Text("Title Large", M3TextRole.TITLE_LARGE)
                ),
                createShowcaseGroup(
                        "Body And Labels",
                        new M3Text("Label Large", M3TextRole.LABEL_LARGE),
                        new M3Text("Body Large text follows the active theme typography tokens.", M3TextRole.BODY_LARGE),
                        new M3Text("Body Medium text", M3TextRole.BODY_MEDIUM)
                )
        );
    }

    /// Creates the icon component page.
    private Node createIconsPage() {
        M3Icon disabledIcon = createDemoIcon("D", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT);
        disabledIcon.setDisable(true);

        return createGallery(
                createShowcaseGroup(
                        "Sizes",
                        createDemoIcon("S", M3IconSize.SMALL, M3IconVariant.PRIMARY),
                        createDemoIcon("M", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                        createDemoIcon("L", M3IconSize.LARGE, M3IconVariant.PRIMARY),
                        createDemoIcon("X", M3IconSize.EXTRA_LARGE, M3IconVariant.PRIMARY)
                ),
                createShowcaseGroup(
                        "Color Variants",
                        createDemoIcon("P", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                        createDemoIcon("S", M3IconSize.MEDIUM, M3IconVariant.SECONDARY),
                        createDemoIcon("T", M3IconSize.MEDIUM, M3IconVariant.TERTIARY),
                        createDemoIcon("E", M3IconSize.MEDIUM, M3IconVariant.ERROR),
                        createDemoIcon("O", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE),
                        disabledIcon
                ),
                createShowcaseGroup(
                        "Button Usage",
                        createIconButton("i"),
                        createIconButton("+"),
                        createIconToggleButton("B", M3IconToggleButtonVariant.TONAL, true),
                        createFab("+", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.SMALL),
                        createFab("*", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.REGULAR)
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
                M3TextInputVariant.FILLED,
                false
        );
        M3TextArea outlinedArea = createTextArea(
                "Outlined text area",
                "Material text areas share field colors but keep multi-line height tokens.",
                M3TextInputVariant.OUTLINED,
                false
        );
        M3TextArea areaError = createTextArea(
                "Text area error",
                "This content needs review.",
                M3TextInputVariant.FILLED,
                false
        );
        areaError.setError(true);

        M3TextInputLayout filledLayout = createTextInputLayout(filled, "Supporting text");
        M3TextInputLayout filledTextLayout = createTextInputLayout(filledText, "Email address");
        filledTextLayout.setLeading(createDemoIcon("E", M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT));
        filledTextLayout.setClearButtonEnabled(true);
        filledTextLayout.setCharacterCounterVisible(true);
        filledTextLayout.setCharacterLimit(32);
        M3TextInputLayout filledDisabledLayout = createTextInputLayout(filledDisabled, "Disabled supporting text");
        filledDisabledLayout.setLeading(createDemoIcon("R", M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT));
        M3TextInputLayout outlinedLayout = createTextInputLayout(outlined, "Outlined supporting text");
        M3TextInputLayout outlinedTextLayout = createTextInputLayout(outlinedText, "Project name");
        outlinedTextLayout.setLeading(createDemoIcon("T", M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT));
        outlinedTextLayout.setCharacterCounterVisible(true);
        outlinedTextLayout.setCharacterLimit(24);
        outlinedTextLayout.setCharacterLimitEnforced(true);
        M3TextInputLayout passwordLayout = createTextInputLayout(password, "At least 8 characters");
        passwordLayout.setTrailing(createIconButton("V"));
        M3TextField validatedEmail = createTextField("Validated email", "support", M3TextInputVariant.OUTLINED, false);
        validatedEmail.setPrefWidth(340.0);
        M3TextInputLayout validatedEmailLayout = createTextInputLayout(validatedEmail, "Validation runs on focus loss");
        validatedEmailLayout.setValidator(M3TextInputValidators.required("Email is required"));
        validatedEmailLayout.addValidator(M3TextInputValidators.pattern(
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
        outlinedErrorLayout.setLeading(createDemoIcon("!", M3IconSize.SMALL, M3IconVariant.ERROR));
        outlinedErrorLayout.setErrorText("This field is required");
        M3TextInputLayout passwordErrorLayout = createTextInputLayout(passwordError, "Supporting text");
        passwordErrorLayout.setErrorText("Password cannot be empty");
        M3TextInputLayout filledAreaLayout = createTextInputLayout(filledArea, "Filled multi-line input");
        M3TextInputLayout outlinedAreaLayout = createTextInputLayout(outlinedArea, "Outlined multi-line input");
        outlinedAreaLayout.setCharacterCounterVisible(true);
        outlinedAreaLayout.setCharacterLimit(96);
        M3TextInputLayout areaErrorLayout = createTextInputLayout(areaError, "Supporting text");
        areaErrorLayout.setErrorText("Review this text before continuing");

        return createGallery(
                createShowcaseGroup("Filled", filledLayout, filledTextLayout, filledDisabledLayout),
                createShowcaseGroup("Outlined", outlinedLayout, outlinedTextLayout, passwordLayout),
                createShowcaseGroup("Validation", validatedEmailLayout, requiredProjectLayout),
                createShowcaseGroup("Error", filledErrorLayout, outlinedErrorLayout, passwordErrorLayout, areaErrorLayout),
                createShowcaseGroup("Text Areas", filledAreaLayout, outlinedAreaLayout)
        );
    }

    /// Creates the search component page.
    private Node createSearchPage() {
        M3SearchBar searchBar = new M3SearchBar("Search M3FX");
        searchBar.setPrefWidth(420.0);
        M3IconButton clearSearchBar = createIconButton("C");
        clearSearchBar.setOnAction(event -> searchBar.clear());
        searchBar.getTrailingActions().add(clearSearchBar);

        M3SearchBar populated = new M3SearchBar("Search M3FX");
        populated.setText("Buttons");
        populated.setPrefWidth(420.0);

        M3SearchView searchView = new M3SearchView("Search components");
        searchView.setPrefWidth(520.0);
        M3IconButton clearSearchView = createIconButton("C");
        clearSearchView.setOnAction(event -> searchView.clear());
        searchView.getTrailingActions().add(clearSearchView);
        searchView.getResults().addAll(
                createSearchResult("Buttons", "Filled, tonal, outlined, text, and elevated variants"),
                createSearchResult("Menus", "Menu surfaces, selected rows, and menu buttons"),
                createSearchResult("Navigation", "Bars, rails, drawers, and destination items")
        );

        M3SearchView inactiveView = new M3SearchView("Collapsed search");
        inactiveView.setPrefWidth(520.0);
        inactiveView.getResults().addAll(
                createSearchResult("Hidden result", "Result content is hidden while inactive")
        );
        inactiveView.deactivate();

        return createGallery(
                createShowcaseGroup("Search Bars", searchBar, populated),
                createShowcaseGroup("Search View", searchView),
                createShowcaseGroup("Inactive View", inactiveView)
        );
    }

    /// Creates the checkbox component page.
    private Node createCheckboxesPage() {
        M3CheckBox unchecked = createCheckBox("Unchecked", false, false, false, false);
        M3CheckBox checked = createCheckBox("Checked", true, false, false, false);
        M3CheckBox indeterminate = createCheckBox("Indeterminate", false, true, true, false);
        M3CheckBox threeState = createCheckBox("Three-state cycle", false, false, true, false);

        M3CheckBox disabledUnchecked = createCheckBox("Disabled unchecked", false, false, false, true);
        M3CheckBox disabledChecked = createCheckBox("Disabled checked", true, false, false, true);
        M3CheckBox disabledIndeterminate =
                createCheckBox("Disabled indeterminate", false, true, false, true);

        return createGallery(
                createShowcaseGroup("Interactive States", unchecked, checked, indeterminate, threeState),
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

        return createGallery(
                createShowcaseGroup("Interactive States", onSwitch, offSwitch),
                createShowcaseGroup("Disabled States", disabledOffSwitch, disabledOnSwitch)
        );
    }

    /// Creates the slider component page.
    private Node createSlidersPage() {
        return createGallery(createShowcaseGroup(
                "Values",
                createSlider(24.0, false),
                createSlider(64.0, false),
                createSlider(50.0, true)
        ));
    }

    /// Creates the chip component page.
    private Node createChipsPage() {
        M3Chip assist = createChip("Assist", M3ChipVariant.ASSIST, false, false);
        assist.setGraphic(createNavigationIcon("A"));
        M3Chip suggestion = createChip("Suggestion", M3ChipVariant.SUGGESTION, false, false);
        M3Chip input = createChip("Input", M3ChipVariant.INPUT, false, false);
        M3Chip filter = createChip("Filter", M3ChipVariant.FILTER, false, false);
        M3Chip selectedFilter = createChip("Selected", M3ChipVariant.FILTER, true, false);
        M3Chip disabled = createChip("Disabled", M3ChipVariant.ASSIST, false, true);

        M3ChipGroup multiSelect = new M3ChipGroup(
                createChip("Work", M3ChipVariant.FILTER, true, false),
                createChip("Personal", M3ChipVariant.FILTER, false, false),
                createChip("Travel", M3ChipVariant.FILTER, true, false),
                createChip("Finance", M3ChipVariant.FILTER, false, false)
        );
        multiSelect.setPrefWrapLength(360.0);

        M3ChipGroup singleSelect = new M3ChipGroup(
                createChip("All", M3ChipVariant.FILTER, false, false),
                createChip("Open", M3ChipVariant.FILTER, false, false),
                createChip("Closed", M3ChipVariant.FILTER, false, false)
        );
        singleSelect.setSelectionMode(M3ChipSelectionMode.SINGLE);
        singleSelect.setAllowEmptySelection(false);
        singleSelect.selectFirst();

        return createGallery(
                createShowcaseGroup("Variants", assist, suggestion, input, filter),
                createShowcaseGroup("States", selectedFilter, disabled),
                createShowcaseGroup("Multi Select", multiSelect),
                createShowcaseGroup("Single Select", singleSelect)
        );
    }

    /// Creates the date picker component page.
    private Node createDatePickersPage() {
        LocalDate today = LocalDate.now();
        M3DatePickerField field = new M3DatePickerField(today);
        field.setLabelText("Event date");
        field.setSupportingText("Editable ISO date with popup calendar");
        field.getEditor().setVariant(M3TextInputVariant.OUTLINED);
        field.setCommonPresets(today);
        field.setPrefWidth(320.0);
        field.setMaxWidth(320.0);

        M3DatePickerField boundedField = new M3DatePickerField(today.plusDays(2));
        boundedField.setLabelText("Booking date");
        boundedField.setSupportingText("Limited to the next two weeks");
        boundedField.getEditor().setVariant(M3TextInputVariant.FILLED);
        boundedField.setMinDate(today);
        boundedField.setMaxDate(today.plusDays(14));
        boundedField.setPresets(
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
        rangeField.getStartEditor().setVariant(M3TextInputVariant.OUTLINED);
        rangeField.getEndEditor().setVariant(M3TextInputVariant.OUTLINED);
        rangeField.setMinDate(today.minusDays(7));
        rangeField.setMaxDate(today.plusDays(30));
        rangeField.setCommonPresets(today);
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

        M3Button dateDialogButton = createButton("Open date dialog", M3ButtonVariant.FILLED);
        dateDialogButton.setOnAction(event -> showDatePickerDialog(today));
        M3Button rangeDialogButton = createButton("Open range dialog", M3ButtonVariant.TONAL);
        rangeDialogButton.setOnAction(event -> showDateRangePickerDialog(today.plusDays(2), today.plusDays(8)));
        M3Button presetRangeDialogButton = createButton("Open preset range dialog", M3ButtonVariant.OUTLINED);
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
        field.getEditor().setVariant(M3TextInputVariant.OUTLINED);
        field.setUse24HourClock(true);
        field.setMinuteStep(15);
        field.setCommonPresets(LocalTime.of(10, 30));
        field.setPrefWidth(320.0);
        field.setMaxWidth(320.0);

        M3TimePickerField boundedField = new M3TimePickerField(LocalTime.of(9, 30));
        boundedField.setLabelText("Office hours");
        boundedField.setSupportingText("Limited to 09:00 through 17:30");
        boundedField.getEditor().setVariant(M3TextInputVariant.FILLED);
        boundedField.setMinTime(LocalTime.of(9, 0));
        boundedField.setMaxTime(LocalTime.of(17, 30));
        boundedField.setMinuteStep(30);
        boundedField.setPresets(M3TimePresets.morning(), M3TimePresets.noon(), M3TimePresets.afternoon());
        boundedField.setPrefWidth(320.0);
        boundedField.setMaxWidth(320.0);

        M3TimePicker twelveHour = new M3TimePicker(LocalTime.of(10, 30));

        M3TimePicker twentyFourHour = new M3TimePicker(LocalTime.of(14, 45));
        twentyFourHour.setUse24HourClock(true);
        twentyFourHour.setMinuteStep(15);

        M3TimePicker bounded = new M3TimePicker(LocalTime.of(9, 30));
        bounded.setMinTime(LocalTime.of(9, 0));
        bounded.setMaxTime(LocalTime.of(17, 30));

        M3Button dialogButton = createButton("Open time dialog", M3ButtonVariant.FILLED);
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
                createMenuItem("New", "N", "Ctrl+N"),
                createMenuItem("Open", "O", "Ctrl+O"),
                new M3SubMenuItem(
                        "Open Recent",
                        createMenuItem("Project Alpha", "A", ""),
                        createMenuItem("Project Beta", "B", "")
                ),
                createMenuItem("Save", "S", "Ctrl+S"),
                new M3Divider(),
                new M3MenuSectionHeader("Recent"),
                createMenuItem("Project Alpha", "A", ""),
                createMenuItem("Project Beta", "B", "")
        );

        M3MenuButton menuButton = new M3MenuButton(
                "Open menu",
                new M3MenuSectionHeader("Document"),
                createMenuItem("Duplicate", "D", "Ctrl+D"),
                new M3SubMenuItem(
                        "Move to",
                        createMenuItem("Archive", "A", ""),
                        createMenuItem("Inbox", "I", "")
                ),
                createMenuItem("Rename", "R", ""),
                new M3Divider(),
                new M3MenuSectionHeader("Danger"),
                createMenuItem("Delete", "X", "")
        );
        menuButton.setVariant(M3ButtonVariant.OUTLINED);
        menuButton.setSelectionMode(M3MenuSelectionMode.SINGLE);

        M3MenuItem selected = createMenuItem("Selected item", "S", "");
        M3Menu selectedMenu = new M3Menu(selected, createMenuItem("Regular item", "R", ""));
        selectedMenu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        selectedMenu.setAllowEmptySelection(false);
        selectedMenu.selectIndex(0);

        M3Menu multiSelectMenu = new M3Menu(
                new M3MenuSectionHeader("Visibility"),
                createMenuItem("Icons", "I", ""),
                createMenuItem("Labels", "L", ""),
                createMenuItem("Badges", "B", "")
        );
        multiSelectMenu.setSelectionMode(M3MenuSelectionMode.MULTIPLE);
        multiSelectMenu.selectIndex(1);
        multiSelectMenu.selectIndex(3);

        return createGallery(
                createShowcaseGroup("Menu Button", menuButton),
                createShowcaseGroup("Inline Menus", inlineMenu, selectedMenu, multiSelectMenu)
        );
    }

    /// Creates the segmented button component page.
    private Node createSegmentedButtonsPage() {
        M3SegmentedButtonGroup dateRange = createSegmentedGroup("Day", "Week", "Month");
        M3SegmentedButtonGroup priority = createSegmentedGroup("Low", "Medium", "High");
        priority.getItems().get(2).setDisable(true);
        M3SegmentedButtonGroup channels = createSegmentedGroup("Email", "Chat", "Push");
        channels.clearSelection();
        channels.setSelectionMode(M3SegmentedButtonSelectionMode.MULTIPLE);
        channels.selectIndex(0);
        channels.selectIndex(2);

        return createGallery(
                createShowcaseGroup("Date Range", dateRange),
                createShowcaseGroup("Availability", priority),
                createShowcaseGroup("Multi Select", channels)
        );
    }

    /// Creates the split button component page.
    private Node createSplitButtonsPage() {
        M3SplitButton tonal = createSplitButton("Create", M3ButtonVariant.TONAL);
        M3SplitButton outlined = createSplitButton("Export", M3ButtonVariant.OUTLINED);
        M3SplitButton filled = createSplitButton("Publish", M3ButtonVariant.FILLED);
        M3SplitButton disabled = createSplitButton("Disabled", M3ButtonVariant.TONAL);
        disabled.setDisable(true);

        return createGallery(
                createShowcaseGroup("Variants", tonal, outlined, filled, disabled)
        );
    }

    /// Creates the tab component page.
    private Node createTabsPage() {
        M3TabBar primary = createTabBar("Overview", "Activity", "Files");
        M3TabBar disabled = createTabBar("Today", "Week", "Month");
        disabled.getTabs().get(2).setDisable(true);

        return createGallery(
                createShowcaseGroup("Primary", primary),
                createShowcaseGroup("Disabled", disabled)
        );
    }

    /// Creates the app bar component page.
    private Node createAppBarsPage() {
        M3TopAppBar small = createTopAppBar("Inbox");
        M3TopAppBar centerAligned = createTopAppBar("Calendar");
        centerAligned.setVariant(M3TopAppBarVariant.CENTER_ALIGNED);
        M3TopAppBar medium = createTopAppBar("Project");
        medium.setVariant(M3TopAppBarVariant.MEDIUM);
        M3TopAppBar large = createTopAppBar("Workspace");
        large.setVariant(M3TopAppBarVariant.LARGE);

        return createGallery(
                createShowcaseGroup("Small", small, centerAligned),
                createShowcaseGroup("Tall", medium, large)
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
                createShowcaseGroup("Floating Action", end, center, start)
        );
    }

    /// Creates the navigation component page.
    private Node createNavigationPage() {
        M3NavigationBar primary = createNavigationBar("Home", "Search", "Profile", "Settings");
        M3NavigationBar compact = createNavigationBar("Inbox", "Tasks", "Done");
        compact.setStyle("-fx-pref-height: 88px; -fx-padding: 0 24px;");

        return createGallery(
                createShowcaseGroup("Four Items", primary),
                createShowcaseGroup("Three Items", compact)
        );
    }

    /// Creates the navigation rail component page.
    private Node createNavigationRailPage() {
        M3NavigationRail primary = createNavigationRail("Home", "Search", "Profile", "Settings");
        M3NavigationRail compact = createNavigationRail("Inbox", "Tasks", "Done");

        return createGallery(
                createShowcaseGroup("Four Items", primary),
                createShowcaseGroup("Three Items", compact)
        );
    }

    /// Creates the navigation drawer component page.
    private Node createNavigationDrawerPage() {
        M3NavigationDrawer primary = createNavigationDrawer("Inbox", "Starred", "Sent", "Archive");
        M3NavigationDrawer labeled = createNavigationDrawer("Dashboard", "Reports", "Settings");
        Label section = new Label("Workspace");
        section.getStyleClass().add("demo-drawer-section");
        labeled.getItems().add(0, section);

        return createGallery(
                createShowcaseGroup("Destinations", primary),
                createShowcaseGroup("Section", labeled)
        );
    }

    /// Creates the loading indicator component page.
    private Node createLoadingIndicatorPage() {
        M3ProgressIndicator compactIndicator = new M3ProgressIndicator();
        compactIndicator.setPrefSize(48.0, 48.0);
        applyBaselineProgress(compactIndicator);

        M3ProgressIndicator regularIndicator = new M3ProgressIndicator();
        regularIndicator.setPrefSize(64.0, 64.0);
        applyBaselineProgress(regularIndicator);

        M3ProgressBar indeterminateBar = new M3ProgressBar();
        indeterminateBar.setPrefWidth(380.0);
        applyBaselineProgress(indeterminateBar);

        M3ProgressIndicator expressiveCompactIndicator = new M3ProgressIndicator();
        expressiveCompactIndicator.setPrefSize(48.0, 48.0);
        applyExpressiveCircularProgress(expressiveCompactIndicator);

        M3ProgressIndicator expressiveRegularIndicator = new M3ProgressIndicator();
        expressiveRegularIndicator.setPrefSize(64.0, 64.0);
        applyExpressiveCircularProgress(expressiveRegularIndicator);

        M3ProgressBar expressiveIndeterminateBar = new M3ProgressBar();
        expressiveIndeterminateBar.setPrefWidth(380.0);
        applyExpressiveLinearProgress(expressiveIndeterminateBar);

        return createGallery(
                createShowcaseGroup("Standard", compactIndicator, regularIndicator, indeterminateBar),
                createShowcaseGroup(
                        "Expressive Wavy",
                        expressiveCompactIndicator,
                        expressiveRegularIndicator,
                        expressiveIndeterminateBar
                )
        );
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
        determinateIndicator.setPrefSize(64.0, 64.0);
        applyBaselineProgress(determinateIndicator);
        M3ProgressIndicator indeterminateIndicator = new M3ProgressIndicator();
        indeterminateIndicator.setPrefSize(64.0, 64.0);
        applyBaselineProgress(indeterminateIndicator);

        M3ProgressBar expressiveDeterminateBar = new M3ProgressBar(0.32);
        expressiveDeterminateBar.setPrefWidth(380.0);
        applyExpressiveLinearProgress(expressiveDeterminateBar);
        M3ProgressBar expressiveIndeterminateBar = new M3ProgressBar();
        expressiveIndeterminateBar.setPrefWidth(380.0);
        applyExpressiveLinearProgress(expressiveIndeterminateBar);
        M3ProgressIndicator expressiveDeterminateIndicator = new M3ProgressIndicator(0.32);
        expressiveDeterminateIndicator.setPrefSize(64.0, 64.0);
        applyExpressiveCircularProgress(expressiveDeterminateIndicator);
        M3ProgressIndicator expressiveIndeterminateIndicator = new M3ProgressIndicator();
        expressiveIndeterminateIndicator.setPrefSize(64.0, 64.0);
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
        oneLine.setLeadingIcon("I");

        M3ListItem twoLine = new M3ListItem("Two-line item");
        twoLine.setSupportingText("Supporting text");
        twoLine.setTrailingSupportingText("3 min");
        twoLine.setTrailingIcon(">");

        M3ListItem threeLine = new M3ListItem("Three-line item");
        threeLine.setOverlineText("Overline");
        threeLine.setSupportingText("Supporting text can span a denser row.");
        threeLine.setLeadingAvatar("A");

        M3ListItem thumbnail = new M3ListItem("Thumbnail item");
        thumbnail.setSupportingText("Leading square media and trailing metadata.");
        thumbnail.setLeadingThumbnail(createListThumbnail("T"));
        thumbnail.setTrailingSupportingText("12:40");

        M3ListItem wideThumbnail = new M3ListItem("Wide thumbnail item");
        wideThumbnail.setSupportingText("Media content is clipped to the configured slot size.");
        wideThumbnail.setLeadingMedia(createListThumbnail("W"), M3ListItemSlotSize.WIDE_THUMBNAIL);
        wideThumbnail.setTrailingIcon(">");

        M3ListItem selected = new M3ListItem("Selected item");
        selected.setSupportingText("Current destination");
        selected.setLeadingIcon("S");
        selected.setTrailingSupportingText("Now");

        M3ListPane listPane = new M3ListPane();
        listPane.getStyleClass().add("demo-list");
        listPane.setSelectionMode(M3ListSelectionMode.SINGLE);
        listPane.getItems().addAll(
                new M3ListSectionHeader("Recent"),
                oneLine,
                new M3Divider(),
                twoLine,
                new M3Divider(),
                threeLine,
                new M3Divider(),
                thumbnail,
                new M3Divider(),
                wideThumbnail,
                new M3Divider(),
                new M3ListSectionHeader("Pinned"),
                selected
        );
        listPane.select(selected);

        M3ListView<String> listView = new M3ListView<>();
        for (int i = 1; i <= 100; i++) {
            listView.addItem("Virtualized row " + i);
        }
        listView.setSelectionMode(M3ListSelectionMode.SINGLE);
        listView.setFixedCellSize(56.0);
        listView.setPrefSize(360.0, 280.0);
        listView.setCellFactory(text -> {
            M3ListItem item = new M3ListItem(text);
            item.setLeadingIcon("#");
            item.setTrailingSupportingText(Integer.toString(text.length()));
            return item;
        });
        listView.selectIndex(0);

        return createGallery(
                createShowcaseGroup("Static Pane", listPane),
                createShowcaseGroup("Virtualized View", listView)
        );
    }

    /// Creates a sample thumbnail used by list item media rows.
    private static StackPane createListThumbnail(String iconText) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.SMALL, M3IconVariant.ON_SURFACE);
        StackPane thumbnail = new StackPane(icon);
        thumbnail.getStyleClass().add("demo-list-thumbnail");
        return thumbnail;
    }

    /// Creates the badge component page.
    private Node createBadgesPage() {
        M3Button button = createButton("Inbox", M3ButtonVariant.TONAL);
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
        M3Avatar graphic = new M3Avatar(createNavigationIcon("G"));
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
        M3Card filled = createSampleCard("Filled card", M3CardVariant.FILLED);
        M3Card outlined = createSampleCard("Outlined card", M3CardVariant.OUTLINED);
        M3Card elevated = createSampleCard("Elevated card", M3CardVariant.ELEVATED);

        return createGallery(createShowcaseGroup("Cards", filled, outlined, elevated));
    }

    /// Creates the carousel component page.
    private Node createCarouselPage() {
        M3Carousel multiBrowse = new M3Carousel(
                createCarouselCard("Morning focus", "Deep work block", M3CardVariant.FILLED, 220.0, 140.0),
                createCarouselCard("Design review", "Component polish", M3CardVariant.ELEVATED, 240.0, 140.0),
                createCarouselCard("Release notes", "Packaging updates", M3CardVariant.OUTLINED, 220.0, 140.0),
                createCarouselCard("Visual QA", "Snapshot inspection", M3CardVariant.FILLED, 220.0, 140.0),
                createCarouselCard("Accessibility", "Keyboard checks", M3CardVariant.OUTLINED, 220.0, 140.0)
        );
        multiBrowse.setPrefWidth(760.0);
        multiBrowse.selectIndex(1);

        M3Button previous = createButton("Previous", M3ButtonVariant.OUTLINED);
        previous.setOnAction(event -> multiBrowse.selectPrevious());
        M3Button next = createButton("Next", M3ButtonVariant.FILLED);
        next.setOnAction(event -> multiBrowse.selectNext());

        M3Carousel compact = new M3Carousel(
                createCarouselCard("Inbox", "24 unread", M3CardVariant.FILLED, 160.0, 112.0),
                createCarouselCard("Tasks", "6 due", M3CardVariant.ELEVATED, 160.0, 112.0),
                createCarouselCard("Files", "Recent docs", M3CardVariant.OUTLINED, 160.0, 112.0),
                createCarouselCard("People", "Team updates", M3CardVariant.FILLED, 160.0, 112.0)
        );
        compact.setPrefWidth(460.0);
        compact.selectFirst();

        return createGallery(
                createShowcaseGroup("Multi-browse", multiBrowse, previous, next),
                createShowcaseGroup("Compact", compact)
        );
    }

    /// Creates the side sheet component page.
    private Node createSideSheetsPage() {
        M3SideSheet sideSheet = new M3SideSheet("Details", createSheetContent(), createIconButton("X"));

        M3SideSheet modalSideSheet = new M3SideSheet("Filters", createSheetContent(), createIconButton("X"));
        modalSideSheet.setVariant(M3SheetVariant.MODAL);

        return createGallery(
                createShowcaseGroup("Side Sheets", sideSheet, modalSideSheet)
        );
    }

    /// Creates the bottom sheet component page.
    private Node createBottomSheetsPage() {
        M3BottomSheet bottomSheet = new M3BottomSheet("Now playing", createSheetContent(), createIconButton("X"));
        bottomSheet.setPrefWidth(520.0);

        M3BottomSheet compactBottomSheet = new M3BottomSheet("Compact", createSheetContent());
        compactBottomSheet.setDragHandleVisible(false);
        compactBottomSheet.setPrefWidth(520.0);

        return createGallery(
                createShowcaseGroup("Bottom Sheets", bottomSheet, compactBottomSheet)
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
        M3Button dialogButton = createButton("Open dialog", M3ButtonVariant.FILLED);
        dialogButton.setOnAction(event -> showDemoDialog());

        M3DialogPane inlinePane = new M3DialogPane();
        inlinePane.setHeaderText("Dialog title");
        inlinePane.setContentText("The active theme is applied to this dialog pane.");
        inlinePane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        inlinePane.setPrefWidth(420.0);

        return createGallery(
                createShowcaseGroup("Launcher", dialogButton),
                createShowcaseGroup("Pane", inlinePane)
        );
    }

    /// Creates the banner component page.
    private Node createBannersPage() {
        M3Button learnButton = createButton("Learn", M3ButtonVariant.TEXT);
        learnButton.setOnAction(event -> showSnackbar());
        M3Button dismissButton = createButton("Dismiss", M3ButtonVariant.TEXT);
        dismissButton.setOnAction(event -> showSnackbar());
        M3Banner informational = new M3Banner(
                "M3FX can install generated token stylesheets for each JavaFX scene while keeping application scene management explicit.",
                learnButton,
                dismissButton
        );
        informational.setIcon(new M3Icon("i", M3IconSize.MEDIUM, M3IconVariant.PRIMARY));
        informational.setPrefWidth(760.0);

        M3Button reviewButton = createButton("Review", M3ButtonVariant.TEXT);
        reviewButton.setOnAction(event -> showActionSnackbar());
        M3Banner warning = new M3Banner(
                "The selected jlink target uses platform-specific BellSoft LibericaJDK Full jmods.",
                reviewButton
        );
        warning.setIcon(new M3Icon("!", M3IconSize.MEDIUM, M3IconVariant.ERROR));
        warning.setPrefWidth(760.0);

        M3Button manageButton = createButton("Manage", M3ButtonVariant.TEXT);
        manageButton.setOnAction(event -> showSnackbar());
        M3Banner noIcon = new M3Banner(
                "Banners may omit the leading icon when surrounding context already makes the message clear.",
                manageButton
        );
        noIcon.setPrefWidth(760.0);

        M3Banner passive = new M3Banner(
                "Passive banners keep persistent contextual information visible without interrupting the current task."
        );
        passive.setPrefWidth(760.0);

        return createGallery(
                createShowcaseGroup("With Actions", informational, warning),
                createShowcaseGroup("Without Icon", noIcon),
                createShowcaseGroup("Passive", passive)
        );
    }

    /// Creates the snackbar component page.
    private Node createSnackbarsPage() {
        M3Button messageButton = createButton("Show message", M3ButtonVariant.FILLED);
        messageButton.setOnAction(event -> showSnackbar());
        M3Button actionButton = createButton("Show action", M3ButtonVariant.TONAL);
        actionButton.setOnAction(event -> showActionSnackbar());
        M3Button queueButton = createButton("Queue messages", M3ButtonVariant.OUTLINED);
        queueButton.setOnAction(event -> showQueuedSnackbars());

        return createGallery(createShowcaseGroup("Snackbar Host", messageButton, actionButton, queueButton));
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
        availability.setCommonPresets(LocalDate.now());
        availability.setPrefWidth(420.0);
        availability.setMaxWidth(420.0);

        M3Switch notifications = new M3Switch("");
        notifications.setSelected(true);
        M3CheckBox beta = new M3CheckBox();
        beta.setAllowIndeterminate(true);
        beta.setIndeterminate(true);

        M3FormSection account = new M3FormSection(
                "Account",
                "Common fields use the same label column and content alignment.",
                new M3FormRow("Display name", "Primary profile label", displayNameLayout),
                new M3FormRow("Email", "Validated on focus loss", emailLayout),
                new M3FormRow("Availability", "Editable start and end dates", availability)
        );

        M3FormSection preferences = new M3FormSection(
                "Preferences",
                "Boolean settings keep labels aligned with selection controls.",
                new M3FormRow("Notifications", "Receive product and release updates", notifications),
                new M3FormRow("Beta channel", "Tri-state checkbox in a form row", beta)
        );

        M3FormValidator validator = new M3FormValidator(displayNameLayout, emailLayout);
        M3ValidationSummary validationSummary = new M3ValidationSummary(validator);
        validationSummary.setTitleText("Review form fields");
        validationSummary.setEmptyText("All registered fields are valid");
        validationSummary.setPrefWidth(720.0);
        validationSummary.setMaxWidth(720.0);

        M3Button validateButton = createButton("Validate form", M3ButtonVariant.FILLED);
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

        M3Button clearValidationButton = createButton("Clear validation", M3ButtonVariant.OUTLINED);
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
                "Group-level validation keeps form feedback and focus movement coordinated.",
                new M3FormRow("Actions", "Validate all registered inputs", validationActions)
        );

        M3FormPane form = new M3FormPane(validationSummary, account, preferences, validation);
        form.getStyleClass().add("demo-form");
        form.setContentPadding(18.0);
        form.setPrefWidth(760.0);
        form.setMaxWidth(760.0);

        return createGallery(createShowcaseGroup("Structured Form", form));
    }

    /// Creates the tooltip component page.
    private Node createTooltipsPage() {
        M3Button plain = createButton("Hover me", M3ButtonVariant.FILLED);
        M3Tooltip.install(plain, "Tooltip");

        M3Button longText = createButton("Long tooltip", M3ButtonVariant.OUTLINED);
        M3Tooltip tooltip = new M3Tooltip("Use tooltips for brief contextual labels when a control needs clarification.");
        tooltip.setPrefWidth(260.0);
        M3Tooltip.install(longText, tooltip);

        M3IconButton iconButton = createIconButton("i");
        M3Tooltip.install(iconButton, "Icon button");

        M3Button rich = createButton("Rich tooltip", M3ButtonVariant.TONAL);
        M3RichTooltip.install(
                rich,
                "Rich tooltip",
                "Use rich tooltips when brief supporting context needs a title and a wider surface."
        );

        M3Button actionButton = createButton("Open", M3ButtonVariant.TEXT);
        actionButton.setOnAction(event -> showSnackbar());
        M3Button richAction = createButton("Rich action", M3ButtonVariant.OUTLINED);
        M3RichTooltip.install(
                richAction,
                "Generated theme",
                "The tooltip can inherit the owning scene theme and expose action nodes in the content surface.",
                actionButton
        );

        return createGallery(
                createShowcaseGroup("Plain", plain, longText, iconButton),
                createShowcaseGroup("Rich", rich, richAction)
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
                + "-m3-track-gap: 4px; "
                + "-m3-stop-size: 4px;");
    }

    /// Applies expressive wavy circular progress geometry to a single demo progress indicator.
    private static void applyExpressiveCircularProgress(M3ProgressIndicator progressIndicator) {
        progressIndicator.setStyle("-m3-wave-amplitude: 2px; "
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
            progressIndicator.setIndicatorSize(56.0);
            if (expressive) {
                applyExpressiveCircularProgress(progressIndicator);
            } else {
                applyBaselineProgress(progressIndicator);
            }
            appendInlineStyle(progressIndicator, "-m3-track-thickness: " + trackHeight + "px;");
            indicator = progressIndicator;
            sampleWidth = 72.0;
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

    /// Creates a button configured with the requested variant.
    private static M3Button createButton(String text, M3ButtonVariant variant) {
        return new M3Button(text, variant);
    }

    /// Creates a split button configured with the requested variant.
    private M3SplitButton createSplitButton(String text, M3ButtonVariant variant) {
        M3SplitButton splitButton = new M3SplitButton(
                text,
                new M3MenuItem("Duplicate"),
                new M3MenuItem("Move"),
                new M3MenuItem("Delete")
        );
        splitButton.setVariant(variant);
        splitButton.setOnAction(event -> showSnackbar());
        return splitButton;
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
            M3TextInputVariant variant,
            boolean disabled
    ) {
        M3TextArea textArea = new M3TextArea(text);
        textArea.setVariant(variant);
        textArea.setPromptText(prompt);
        textArea.setDisable(disabled);
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
        item.setLeading(createNavigationIcon(title.substring(0, 1)));
        return item;
    }

    /// Creates a sample search result row.
    private static M3ListItem createSearchResult(String title, String supportingText) {
        M3ListItem item = new M3ListItem(title);
        item.setSupportingText(supportingText);
        item.setLeading(createNavigationIcon(title.substring(0, 1)));
        return item;
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

    /// Creates a chip sample.
    private static M3Chip createChip(
            String text,
            M3ChipVariant variant,
            boolean selected,
            boolean disabled
    ) {
        M3Chip chip = new M3Chip(text);
        chip.setVariant(variant);
        chip.setSelected(selected);
        chip.setDisable(disabled);
        return chip;
    }

    /// Creates a checkbox sample.
    private static M3CheckBox createCheckBox(
            String text,
            boolean selected,
            boolean indeterminate,
            boolean allowIndeterminate,
            boolean disabled
    ) {
        M3CheckBox checkBox = new M3CheckBox(text);
        checkBox.setSelected(selected);
        checkBox.setIndeterminate(indeterminate);
        checkBox.setAllowIndeterminate(allowIndeterminate);
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

    /// Creates a segmented button group sample.
    private static M3SegmentedButtonGroup createSegmentedGroup(String first, String second, String third) {
        M3SegmentedButton firstButton = new M3SegmentedButton(first);
        M3SegmentedButton secondButton = new M3SegmentedButton(second);
        secondButton.setSelected(true);
        M3SegmentedButton thirdButton = new M3SegmentedButton(third);
        return new M3SegmentedButtonGroup(firstButton, secondButton, thirdButton);
    }

    /// Creates a tab bar sample.
    private static M3TabBar createTabBar(String first, String second, String third) {
        M3Tab firstTab = new M3Tab(first);
        firstTab.setSelected(true);
        return new M3TabBar(firstTab, new M3Tab(second), new M3Tab(third));
    }

    /// Creates a top app bar sample.
    private static M3TopAppBar createTopAppBar(String title) {
        M3TopAppBar topAppBar = new M3TopAppBar(
                title,
                createIconButton("M"),
                createIconButton("S"),
                createIconButton("A")
        );
        topAppBar.setPrefWidth(560.0);
        return topAppBar;
    }

    /// Creates a sample surface.
    private static M3Surface createSurface(
            String title,
            M3SurfaceVariant variant,
            M3SurfaceElevation elevation
    ) {
        M3Text label = new M3Text(title, M3TextRole.TITLE_MEDIUM);
        M3Surface surface = new M3Surface(label);
        surface.setVariant(variant);
        surface.setElevation(elevation);
        surface.setPrefSize(180.0, 96.0);
        return surface;
    }

    /// Creates a bottom app bar sample.
    private static M3BottomAppBar createBottomAppBar() {
        M3BottomAppBar bottomAppBar = new M3BottomAppBar(
                M3BottomAppBarFloatingActionAlignment.END,
                createFab("+", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR),
                createIconButton("M"),
                createIconButton("S")
        );
        bottomAppBar.setPrefWidth(560.0);
        return bottomAppBar;
    }

    /// Creates a navigation bar sample.
    private M3NavigationBar createNavigationBar(String first, String second, String third) {
        M3NavigationItem firstItem = createNavigationItem(first, first.substring(0, 1));
        M3NavigationItem secondItem = createNavigationItem(second, second.substring(0, 1));
        M3NavigationItem thirdItem = createNavigationItem(third, third.substring(0, 1));
        secondItem.setBadge(new M3Badge("3"));

        M3NavigationBar navigationBar = new M3NavigationBar(
                firstItem,
                secondItem,
                thirdItem
        );
        navigationBar.selectIndex(0);
        return navigationBar;
    }

    /// Creates a navigation bar sample.
    private M3NavigationBar createNavigationBar(String first, String second, String third, String fourth) {
        M3NavigationItem firstItem = createNavigationItem(first, first.substring(0, 1));
        M3NavigationItem secondItem = createNavigationItem(second, second.substring(0, 1));
        M3NavigationItem thirdItem = createNavigationItem(third, third.substring(0, 1));
        M3NavigationItem fourthItem = createNavigationItem(fourth, fourth.substring(0, 1));
        secondItem.setBadge(new M3Badge("3"));

        M3NavigationBar navigationBar = new M3NavigationBar(
                firstItem,
                secondItem,
                thirdItem,
                fourthItem
        );
        navigationBar.selectIndex(0);
        return navigationBar;
    }

    /// Creates a navigation rail sample.
    private M3NavigationRail createNavigationRail(String first, String second, String third) {
        M3NavigationItem firstItem = createNavigationItem(first, first.substring(0, 1));
        M3NavigationItem secondItem = createNavigationItem(second, second.substring(0, 1));
        M3NavigationItem thirdItem = createNavigationItem(third, third.substring(0, 1));
        secondItem.setBadge(new M3Badge());

        M3NavigationRail navigationRail = new M3NavigationRail(
                firstItem,
                secondItem,
                thirdItem
        );
        navigationRail.selectIndex(0);
        return navigationRail;
    }

    /// Creates a navigation rail sample.
    private M3NavigationRail createNavigationRail(String first, String second, String third, String fourth) {
        M3NavigationItem firstItem = createNavigationItem(first, first.substring(0, 1));
        M3NavigationItem secondItem = createNavigationItem(second, second.substring(0, 1));
        M3NavigationItem thirdItem = createNavigationItem(third, third.substring(0, 1));
        M3NavigationItem fourthItem = createNavigationItem(fourth, fourth.substring(0, 1));
        secondItem.setBadge(new M3Badge());

        M3NavigationRail navigationRail = new M3NavigationRail(
                firstItem,
                secondItem,
                thirdItem,
                fourthItem
        );
        navigationRail.selectIndex(0);
        return navigationRail;
    }

    /// Creates a navigation drawer sample.
    private static M3NavigationDrawer createNavigationDrawer(
            String first,
            String second,
            String third,
            String fourth
    ) {
        M3ListItem firstItem = createDrawerItem(first, first.substring(0, 1));
        M3ListItem secondItem = createDrawerItem(second, second.substring(0, 1));
        M3ListItem thirdItem = createDrawerItem(third, third.substring(0, 1));
        M3ListItem fourthItem = createDrawerItem(fourth, fourth.substring(0, 1));
        secondItem.setTrailing(new M3Badge("3"));

        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(
                firstItem,
                secondItem,
                new M3Divider(),
                thirdItem,
                fourthItem
        );
        navigationDrawer.selectIndex(0);
        return navigationDrawer;
    }

    /// Creates a navigation drawer sample.
    private static M3NavigationDrawer createNavigationDrawer(String first, String second, String third) {
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer(
                createDrawerItem(first, first.substring(0, 1)),
                createDrawerItem(second, second.substring(0, 1)),
                createDrawerItem(third, third.substring(0, 1))
        );
        navigationDrawer.selectIndex(0);
        return navigationDrawer;
    }

    /// Creates a sample drawer item.
    private static M3ListItem createDrawerItem(String text, String iconText) {
        M3ListItem item = new M3ListItem(text);
        item.setLeading(createNavigationIcon(iconText));
        return item;
    }

    /// Creates a sample navigation item.
    private static M3NavigationItem createNavigationItem(String text, String iconText) {
        return new M3NavigationItem(text, createNavigationIcon(iconText));
    }

    /// Creates a sample navigation icon.
    private static M3Icon createNavigationIcon(String iconText) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT);
        icon.getStyleClass().add("demo-navigation-icon");
        return icon;
    }

    /// Creates the sample icon button.
    private static M3IconButton createIconButton(String text) {
        M3Icon icon = new M3Icon(text, M3IconSize.SMALL, M3IconVariant.PRIMARY);
        icon.getStyleClass().add("demo-icon-label");
        return new M3IconButton(icon);
    }

    /// Creates the sample toggle icon button.
    private static M3IconToggleButton createIconToggleButton(
            String text,
            M3IconToggleButtonVariant variant,
            boolean selected
    ) {
        M3Icon icon = new M3Icon(text, M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT);
        icon.getStyleClass().add("demo-icon-label");
        M3IconToggleButton button = new M3IconToggleButton(icon);
        button.setVariant(variant);
        button.setSelected(selected);
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
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup(
                createIconToggleButton(first, variant, false),
                createIconToggleButton(second, variant, false),
                createIconToggleButton(third, variant, false)
        );
        for (String text : rest) {
            group.getItems().add(createIconToggleButton(text, variant, false));
        }
        group.setAllowEmptySelection(false);
        group.selectIndex(0);
        return group;
    }

    /// Creates a sample multi-selection toggle icon button group.
    private static M3IconToggleButtonGroup createIconToggleMultiGroup(
            M3IconToggleButtonVariant variant,
            String first,
            String second,
            String third
    ) {
        M3IconToggleButton firstButton = createIconToggleButton(first, variant, false);
        M3IconToggleButton secondButton = createIconToggleButton(second, variant, false);
        M3IconToggleButton thirdButton = createIconToggleButton(third, variant, false);
        M3IconToggleButtonGroup group = new M3IconToggleButtonGroup(
                firstButton,
                secondButton,
                thirdButton
        );
        group.setSelectionMode(M3IconToggleButtonSelectionMode.MULTIPLE);
        group.selectIndex(0);
        group.selectIndex(2);
        return group;
    }

    /// Creates a sample floating action button.
    private static M3FloatingActionButton createFab(
            String iconText,
            M3FloatingActionButtonVariant variant,
            M3FloatingActionButtonSize size
    ) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE);
        icon.getStyleClass().add("demo-fab-icon");
        M3FloatingActionButton button = new M3FloatingActionButton(icon);
        button.setVariant(variant);
        button.setSize(size);
        return button;
    }

    /// Creates a floating action button menu sample.
    private M3FabMenu createFabMenu() {
        return createFabMenu(
                M3FloatingActionButtonVariant.PRIMARY,
                M3FloatingActionButtonVariant.SECONDARY
        );
    }

    /// Creates a floating action button menu sample using item variants.
    private M3FabMenu createFabMenu(
            M3FloatingActionButtonVariant firstVariant,
            M3FloatingActionButtonVariant secondVariant
    ) {
        M3FloatingActionButton create = createFab("C", firstVariant, M3FloatingActionButtonSize.SMALL);
        M3FloatingActionButton edit = createFab("E", secondVariant, M3FloatingActionButtonSize.SMALL);
        M3FloatingActionButton share = createFab("S", M3FloatingActionButtonVariant.SURFACE, M3FloatingActionButtonSize.SMALL);
        create.setOnAction(event -> showSnackbar());
        edit.setOnAction(event -> showSnackbar());
        share.setOnAction(event -> showSnackbar());
        M3FabMenu menu = new M3FabMenu();
        menu.addItems(create, edit, share);
        return menu;
    }

    /// Creates a sample standalone icon.
    private static M3Icon createDemoIcon(String text, M3IconSize size, M3IconVariant variant) {
        M3Icon icon = new M3Icon(text, size, variant);
        icon.getStyleClass().add("demo-sample-icon");
        return icon;
    }

    /// Creates a sample extended floating action button.
    private static M3FloatingActionButton createExtendedFab() {
        return createExtendedFab("Create", M3FloatingActionButtonVariant.SURFACE);
    }

    /// Creates a sample extended floating action button with a variant.
    private static M3FloatingActionButton createExtendedFab(String text, M3FloatingActionButtonVariant variant) {
        M3FloatingActionButton button = new M3FloatingActionButton(text);
        button.setVariant(variant);
        button.setSize(M3FloatingActionButtonSize.REGULAR);
        return button;
    }

    /// Creates a sample card.
    private M3Card createSampleCard(String title, M3CardVariant variant) {
        VBox content = new VBox(6.0);
        content.getStyleClass().add("demo-card-content");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-card-title");
        Label bodyLabel = new Label("Project summary with active state, shape, and color tokens.");
        bodyLabel.getStyleClass().add("demo-card-body");
        bodyLabel.setWrapText(true);

        content.getChildren().addAll(titleLabel, bodyLabel);

        M3Card card = new M3Card(content, variant, event -> showSnackbar());
        card.setPrefWidth(260.0);
        return card;
    }

    /// Creates a sample carousel card.
    private M3Card createCarouselCard(
            String title,
            String body,
            M3CardVariant variant,
            double width,
            double height
    ) {
        VBox content = new VBox(6.0);
        content.getStyleClass().add("demo-carousel-card-content");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("demo-card-title");
        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("demo-card-body");

        content.getChildren().addAll(titleLabel, bodyLabel);
        M3Card card = new M3Card(content, variant, event -> showSnackbar());
        card.setPrefSize(width, height);
        return card;
    }

    /// Creates sample sheet content.
    private static VBox createSheetContent() {
        M3ListItem first = new M3ListItem("Overview");
        first.setSupportingText("Primary sheet content");
        first.setLeading(createNavigationIcon("O"));
        M3ListItem second = new M3ListItem("Activity");
        second.setSupportingText("Recent updates and state");
        second.setLeading(createNavigationIcon("A"));
        M3ListItem third = new M3ListItem("Settings");
        third.setLeading(createNavigationIcon("S"));

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
            scrim.setOnAction(event -> showSnackbar());
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
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0.08, Interpolator.EASE_BOTH),
                        new KeyValue(progressIndicator.progressProperty(), 0.08, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(
                        Duration.seconds(1.8),
                        new KeyValue(progressBar.progressProperty(), 0.86, Interpolator.EASE_BOTH),
                        new KeyValue(progressIndicator.progressProperty(), 0.86, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(
                        Duration.seconds(3.2),
                        new KeyValue(progressBar.progressProperty(), 0.24, Interpolator.EASE_BOTH),
                        new KeyValue(progressIndicator.progressProperty(), 0.24, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(
                        Duration.seconds(4.6),
                        new KeyValue(progressBar.progressProperty(), 0.68, Interpolator.EASE_BOTH),
                        new KeyValue(progressIndicator.progressProperty(), 0.68, Interpolator.EASE_BOTH)
                )
        );
        animation.setAutoReverse(true);
        animation.setCycleCount(Animation.INDEFINITE);
        animations.add(animation);
        if (animationsEnabled) {
            animation.play();
        }
    }

    /// Opens the demo dialog.
    private void showDemoDialog() {
        M3Dialog<Void> dialog = new M3Dialog<>("M3 Dialog");
        M3DialogPane pane = dialog.getM3DialogPane();
        pane.setHeaderText("Dialog title");
        pane.setContentText("This dialog uses the M3FX dialog pane style and active theme tokens.");
        pane.getButtonTypes().add(ButtonType.OK);

        initDialogOwner(dialog);
        dialog.showAndWait();
    }

    /// Opens a date picker dialog and reports the accepted date.
    private void showDatePickerDialog(LocalDate initialDate) {
        M3DatePickerDialog dialog = new M3DatePickerDialog(initialDate);
        dialog.setCommonPresets(initialDate);
        initDialogOwner(dialog);
        dialog.setOnHidden(event -> {
            @Nullable LocalDate result = dialog.getResult();
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
            @Nullable M3DateRange result = dialog.getResult();
            if (result != null) {
                showSnackbar("Selected range " + result.startDate() + " to " + result.endDate());
            }
        });
        dialog.show();
    }

    /// Opens a date range picker dialog with common range presets.
    private void showPresetDateRangePickerDialog(LocalDate anchorDate) {
        M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
        dialog.setMinDate(anchorDate.minusMonths(1));
        dialog.setMaxDate(anchorDate.plusMonths(3));
        dialog.getPresets().setAll(M3DateRangePresets.common(anchorDate, dialog.getFirstDayOfWeek()));
        initDialogOwner(dialog);
        dialog.setOnHidden(event -> {
            @Nullable M3DateRange result = dialog.getResult();
            if (result != null) {
                showSnackbar("Selected preset range " + result.startDate() + " to " + result.endDate());
            }
        });
        dialog.show();
    }

    /// Opens a time picker dialog and reports the accepted time.
    private void showTimePickerDialog(LocalTime initialTime) {
        M3TimePickerDialog dialog = new M3TimePickerDialog(initialTime);
        dialog.setUse24HourClock(true);
        dialog.setMinuteStep(15);
        dialog.setCommonPresets(initialTime);
        initDialogOwner(dialog);
        dialog.setOnHidden(event -> {
            @Nullable LocalTime result = dialog.getResult();
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
            dialog.initOwner(activeScene.getWindow());
        }
    }

    /// Shows the demo snackbar.
    private void showSnackbar() {
        showSnackbar("Theme-aware snackbar");
    }

    /// Shows a demo snackbar message.
    private void showSnackbar(String message) {
        M3SnackbarHost snackbarHost = this.snackbarHost;
        if (snackbarHost == null) {
            return;
        }
        snackbarHost.show(message);
    }

    /// Shows the demo snackbar with an action.
    private void showActionSnackbar() {
        M3SnackbarHost snackbarHost = this.snackbarHost;
        if (snackbarHost == null) {
            return;
        }
        snackbarHost.show("Theme-aware snackbar", "Action", event -> snackbarHost.show("Action pressed"));
    }

    /// Shows multiple demo snackbars through the host queue.
    private void showQueuedSnackbars() {
        M3SnackbarHost snackbarHost = this.snackbarHost;
        if (snackbarHost == null) {
            return;
        }
        snackbarHost.enqueue("First queued message");
        snackbarHost.enqueue("Second queued message", "Undo", event -> snackbarHost.enqueue("Undo pressed"));
        snackbarHost.enqueue("Third queued message");
    }

    /// Applies the current theme to the scene.
    private void applyTheme() {
        Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }

        M3ThemeManager.install(activeScene, createTheme());
    }

    /// Applies the current demo animation switch to the active scene.
    private void applyMotionSettings() {
        Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }

        M3MotionSettings.setAnimationsEnabled(activeScene.getRoot(), animationsEnabled);
        updatePageAnimations();
    }

    /// Creates a theme from the current demo controls.
    private M3Theme createTheme() {
        return M3Theme.fromSeed(seedColor, profile, brightness, M3Density.of(densityScale));
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

        /// Returns the group title displayed in the sidebar.
        private String title() {
            return title;
        }

        /// Returns the pages that belong to this group.
        private List<DemoPage> pages() {
            return pages;
        }

        /// Returns the first page in this group.
        private DemoPage firstPage() {
            return pages.get(0);
        }

        /// Returns whether this group should render a disclosure item.
        private boolean isCollapsible() {
            return pages.size() > 1;
        }

        /// Returns the collapsible drawer group, or `null` for direct sidebar items.
        private @Nullable M3NavigationDrawerGroup drawerGroup() {
            return drawerGroup;
        }

        /// Sets the collapsible drawer group used to render this sidebar group.
        private void setDrawerGroup(M3NavigationDrawerGroup drawerGroup) {
            this.drawerGroup = Objects.requireNonNull(drawerGroup, "drawerGroup");
            this.topLevelItem = null;
        }

        /// Sets the direct list item used to render this sidebar group.
        private void setTopLevelItem(M3ListItem topLevelItem) {
            this.topLevelItem = Objects.requireNonNull(topLevelItem, "topLevelItem");
            this.drawerGroup = null;
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
    }

    /// Describes one demo component page.
    ///
    /// @param title the page title
    /// @param navigationTitle the page title displayed in the sidebar
    /// @param sidebarSection the sidebar section containing this page
    /// @param subtitle the page subtitle
    /// @param contentFactory the factory used to create page content on demand
    @NotNullByDefault
    private record DemoPage(
            String title,
            String navigationTitle,
            String sidebarSection,
            String subtitle,
            Supplier<Node> contentFactory
    ) {
        /// Creates a demo page descriptor with a sidebar title and section.
        private DemoPage(
                String title,
                String navigationTitle,
                String sidebarSection,
                String subtitle,
                Supplier<Node> contentFactory
        ) {
            this.title = Objects.requireNonNull(title, "title");
            this.navigationTitle = Objects.requireNonNull(navigationTitle, "navigationTitle");
            this.sidebarSection = Objects.requireNonNull(sidebarSection, "sidebarSection");
            this.subtitle = Objects.requireNonNull(subtitle, "subtitle");
            this.contentFactory = Objects.requireNonNull(contentFactory, "contentFactory");
        }

        /// Creates page content.
        private Node createContent() {
            return contentFactory.get();
        }
    }
}
