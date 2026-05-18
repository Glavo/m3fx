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
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3AvatarVariant;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BadgedBox;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3ChipSelectionMode;
import org.glavo.m3fx.controls.M3ChipVariant;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3IconToggleButtonSelectionMode;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3List;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListSelectionMode;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3MenuSectionHeader;
import org.glavo.m3fx.controls.M3MenuSelectionMode;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
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
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SegmentedButtonSelectionMode;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SnackbarHost;
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
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/// A demo application that showcases M3FX controls.
@NotNullByDefault
public final class M3FXDemoApp extends Application {
    /// Seed colors shown in the demo header.
    private static final @Unmodifiable List<Color> SEED_COLORS = List.of(
            Color.web("#6750a4"),
            Color.web("#006a6a"),
            Color.web("#b3261e"),
            Color.web("#386a20"),
            Color.web("#7d5260")
    );

    /// The current seed color used by the demo theme.
    private Color seedColor = M3Theme.DEFAULT_SEED_COLOR;

    /// The current Material Design profile.
    private M3Profile profile = M3Profile.BASELINE_2021;

    /// The current theme brightness.
    private Brightness brightness = Brightness.LIGHT;

    /// The current density scale applied to component tokens.
    private double densityScale;

    /// Animations owned by the active demo page.
    private final List<Animation> animations = new ArrayList<>();

    /// Sidebar items used to switch component pages.
    private final List<M3ListItem> sidebarItems = new ArrayList<>();

    /// The active JavaFX scene.
    private @Nullable Scene scene;

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

        List<DemoPage> pages = createPages();
        StackPane centerStack = new StackPane(createContent(pages), snackbarHost);
        StackPane.setAlignment(snackbarHost, Pos.BOTTOM_CENTER);

        root.setTop(createHeader());
        root.setCenter(centerStack);

        Scene scene = new Scene(root, 1180.0, 820.0);
        scene.getStylesheets().add(demoStylesheetUrl());
        this.scene = scene;
        applyTheme();
        showPage(pages.get(0));

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

        HBox header = new HBox(18.0, titleBox, spacer, seedButtons, profileButton, densityButton, brightnessButton);
        header.getStyleClass().add("demo-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /// Creates all component demo pages.
    private List<DemoPage> createPages() {
        return List.of(
                new DemoPage("Buttons", "Variants, icon buttons, and floating actions", this::createButtonsPage),
                new DemoPage("Typography", "Token-driven Material type roles", this::createTypographyPage),
                new DemoPage("Icons", "Size roles and semantic icon colors", this::createIconsPage),
                new DemoPage("Text Fields", "Filled, outlined, populated, error, and disabled fields", this::createTextFieldsPage),
                new DemoPage("Search", "Search bars, actions, and result surfaces", this::createSearchPage),
                new DemoPage("Checkboxes", "Checked, unchecked, and disabled states", this::createCheckboxesPage),
                new DemoPage("Radio Buttons", "Grouped single selection states", this::createRadioButtonsPage),
                new DemoPage("Switches", "On, off, and disabled switch states", this::createSwitchesPage),
                new DemoPage("Sliders", "Different values and disabled slider states", this::createSlidersPage),
                new DemoPage("Chips", "Assist, filter, input, suggestion, and disabled chips", this::createChipsPage),
                new DemoPage("Menus", "Menu surfaces, actions, and menu buttons", this::createMenusPage),
                new DemoPage("Segmented Buttons", "Single- and multi-select segmented control states", this::createSegmentedButtonsPage),
                new DemoPage("Tabs", "Primary tabs with animated active indicators", this::createTabsPage),
                new DemoPage("App Bars", "Top app bars with navigation and actions", this::createAppBarsPage),
                new DemoPage("Bottom App Bars", "Bottom app bars with actions and floating actions", this::createBottomAppBarsPage),
                new DemoPage("Navigation", "Bottom navigation items and selected indicators", this::createNavigationPage),
                new DemoPage("Navigation Rail", "Vertical destinations for wide layouts", this::createNavigationRailPage),
                new DemoPage("Navigation Drawer", "Drawer destinations with selected rows", this::createNavigationDrawerPage),
                new DemoPage("Progress", "Linear and circular progress indicators", this::createProgressPage),
                new DemoPage("Lists", "One-line, two-line, three-line, and selected rows", this::createListPage),
                new DemoPage("Badges", "Dot, count, overflow, and attached badges", this::createBadgesPage),
                new DemoPage("Avatars", "Initials and graphic avatar slots", this::createAvatarsPage),
                new DemoPage("Dividers", "Full-width, inset, middle inset, and vertical dividers", this::createDividersPage),
                new DemoPage("Surfaces", "Color containers, shape, padding, and elevation", this::createSurfacesPage),
                new DemoPage("Cards", "Filled, outlined, elevated, and interactive cards", this::createCardsPage),
                new DemoPage("Sheets", "Side and bottom containment surfaces", this::createSheetsPage),
                new DemoPage("Scrims", "Modal overlays and dismiss actions", this::createScrimsPage),
                new DemoPage("Dialogs", "Dialog pane with themed actions", this::createDialogsPage),
                new DemoPage("Banners", "Persistent inline feedback with optional actions", this::createBannersPage),
                new DemoPage("Snackbars", "Snackbar host with action and queued messages", this::createSnackbarsPage),
                new DemoPage("Tooltips", "Plain and longer contextual help", this::createTooltipsPage)
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

        Label heading = new Label("Components");
        heading.getStyleClass().add("demo-drawer-section");
        sidebar.getItems().add(heading);

        sidebarItems.clear();
        for (DemoPage page : pages) {
            M3ListItem item = new M3ListItem(page.title());
            item.setLeading(createNavigationIcon(sidebarIconText(page.title())));
            item.setOnAction(event -> showPage(page));
            sidebarItems.add(item);
            sidebar.getItems().add(item);
        }

        ScrollPane scrollPane = new ScrollPane(sidebar);
        scrollPane.getStyleClass().add("demo-sidebar-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    /// Creates the scrollable page host.
    private Node createPageScrollPane() {
        StackPane host = new StackPane();
        host.getStyleClass().add("demo-page-host");
        pageHost = host;

        ScrollPane scrollPane = new ScrollPane(host);
        scrollPane.getStyleClass().add("demo-scroll-pane");
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

        for (M3ListItem item : sidebarItems) {
            item.setSelected(item.getHeadlineText().equals(page.title()));
        }

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

    /// Stops animations owned by the previous page.
    private void stopPageAnimations() {
        for (Animation animation : animations) {
            animation.stop();
        }
        animations.clear();
    }

    /// Creates the button component page.
    private Node createButtonsPage() {
        M3Button disabledFilled = createButton("Disabled", M3ButtonVariant.FILLED);
        disabledFilled.setDisable(true);
        M3IconButton disabledIcon = createIconButton("i");
        disabledIcon.setDisable(true);

        return createGallery(
                createShowcaseGroup(
                        "Button Variants",
                        createButton("Filled", M3ButtonVariant.FILLED),
                        createButton("Tonal", M3ButtonVariant.TONAL),
                        createButton("Outlined", M3ButtonVariant.OUTLINED),
                        createButton("Text", M3ButtonVariant.TEXT),
                        createButton("Elevated", M3ButtonVariant.ELEVATED),
                        disabledFilled
                ),
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
                ),
                createShowcaseGroup(
                        "Floating Action Buttons",
                        createFab("+", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.SMALL),
                        createFab("+", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR),
                        createFab("*", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.LARGE),
                        createExtendedFab()
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
        M3PasswordField password = M3PasswordField.withVariant("", M3TextInputVariant.OUTLINED);
        password.setPromptText("Password");
        password.setPrefWidth(320.0);
        M3TextField filledError = createTextField("Filled error", "Invalid value", M3TextInputVariant.FILLED, false);
        filledError.setError(true);
        M3TextField outlinedError = createTextField("Outlined error", "", M3TextInputVariant.OUTLINED, false);
        outlinedError.setError(true);
        M3PasswordField passwordError = M3PasswordField.withVariant("", M3TextInputVariant.OUTLINED);
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
        validatedEmailLayout.setValidator((input, text) -> text.isBlank()
                ? "Email is required"
                : text.contains("@") ? null : "Use an email address");
        validatedEmailLayout.validate();
        M3TextField requiredProject = createTextField("Required project", "", M3TextInputVariant.FILLED, false);
        M3TextInputLayout requiredProjectLayout = createTextInputLayout(requiredProject, "Required field");
        requiredProjectLayout.setValidator((input, text) -> text.isBlank() ? "Project name is required" : null);
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
        M3CheckBox checked = M3CheckBox.withSelected("Checked", true);
        M3CheckBox unchecked = new M3CheckBox("Unchecked");
        M3CheckBox disabled = new M3CheckBox("Disabled");
        disabled.setDisable(true);

        return createGallery(createShowcaseGroup("States", checked, unchecked, disabled));
    }

    /// Creates the radio button component page.
    private Node createRadioButtonsPage() {
        ToggleGroup radioGroup = new ToggleGroup();
        M3RadioButton radioOne = M3RadioButton.withSelected("Radio A", true);
        M3RadioButton radioTwo = new M3RadioButton("Radio B");
        M3RadioButton radioDisabled = new M3RadioButton("Disabled");
        radioOne.setToggleGroup(radioGroup);
        radioTwo.setToggleGroup(radioGroup);
        radioDisabled.setDisable(true);

        return createGallery(createShowcaseGroup("Group", radioOne, radioTwo, radioDisabled));
    }

    /// Creates the switch component page.
    private Node createSwitchesPage() {
        M3Switch enabledSwitch = M3Switch.withSelected("On", true);
        M3Switch offSwitch = new M3Switch("Off");
        M3Switch disabledSwitch = new M3Switch("Disabled");
        disabledSwitch.setDisable(true);

        return createGallery(createShowcaseGroup("States", enabledSwitch, offSwitch, disabledSwitch));
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
        multiSelectMenu.selectIndex(0);
        multiSelectMenu.selectIndex(2);

        return createGallery(
                createShowcaseGroup("Menu Button", menuButton),
                createShowcaseGroup("Inline Menus", inlineMenu, selectedMenu, multiSelectMenu)
        );
    }

    /// Creates the segmented button component page.
    private Node createSegmentedButtonsPage() {
        M3SegmentedButtonGroup dateRange = createSegmentedGroup("Day", "Week", "Month");
        M3SegmentedButtonGroup priority = createSegmentedGroup("Low", "Medium", "High");
        priority.getChildren().get(2).setDisable(true);
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

    /// Creates the progress component page.
    private Node createProgressPage() {
        M3ProgressBar determinateBar = new M3ProgressBar(0.32);
        determinateBar.setPrefWidth(380.0);
        M3ProgressBar indeterminateBar = new M3ProgressBar();
        indeterminateBar.setPrefWidth(380.0);
        M3ProgressIndicator determinateIndicator = new M3ProgressIndicator(0.32);
        determinateIndicator.setPrefSize(64.0, 64.0);
        M3ProgressIndicator indeterminateIndicator = new M3ProgressIndicator();
        indeterminateIndicator.setPrefSize(64.0, 64.0);

        playProgressShowcaseAnimation(determinateBar, determinateIndicator);

        return createGallery(
                createShowcaseGroup("Linear", determinateBar, indeterminateBar),
                createShowcaseGroup("Circular", determinateIndicator, indeterminateIndicator)
        );
    }

    /// Creates the list component page.
    private Node createListPage() {
        M3ListItem oneLine = new M3ListItem("One-line item");
        oneLine.setLeading(new M3Badge());

        M3ListItem twoLine = new M3ListItem("Two-line item");
        twoLine.setSupportingText("Supporting text");
        twoLine.setTrailing(new M3Badge("3"));

        M3ListItem threeLine = new M3ListItem("Three-line item");
        threeLine.setOverlineText("Overline");
        threeLine.setSupportingText("Supporting text can span a denser row.");

        M3ListItem selected = new M3ListItem("Selected item");
        selected.setSupportingText("Current destination");

        M3List list = new M3List();
        list.getStyleClass().add("demo-list");
        list.setSelectionMode(M3ListSelectionMode.SINGLE);
        list.getItems().addAll(
                new M3ListSectionHeader("Recent"),
                oneLine,
                new M3Divider(),
                twoLine,
                new M3Divider(),
                threeLine,
                new M3Divider(),
                new M3ListSectionHeader("Pinned"),
                selected
        );
        list.select(selected);

        return createGallery(createShowcaseGroup("Rows", list));
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
        M3Avatar single = M3Avatar.withVariant("M", M3AvatarVariant.SECONDARY);
        M3Avatar graphic = M3Avatar.withVariant(createNavigationIcon("G"), M3AvatarVariant.TERTIARY);
        M3Avatar surface = M3Avatar.withVariant("S", M3AvatarVariant.SURFACE);

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

    /// Creates the sheet component page.
    private Node createSheetsPage() {
        M3SideSheet sideSheet = new M3SideSheet("Details", createSheetContent(), createIconButton("X"));

        M3SideSheet modalSideSheet = new M3SideSheet("Filters", createSheetContent(), createIconButton("X"));
        modalSideSheet.setVariant(M3SheetVariant.MODAL);

        M3BottomSheet bottomSheet = new M3BottomSheet("Now playing", createSheetContent(), createIconButton("X"));
        bottomSheet.setPrefWidth(520.0);

        M3BottomSheet compactBottomSheet = new M3BottomSheet("Compact", createSheetContent());
        compactBottomSheet.setDragHandleVisible(false);
        compactBottomSheet.setPrefWidth(520.0);

        return createGallery(
                createShowcaseGroup("Side Sheets", sideSheet, modalSideSheet),
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
        M3Banner informational = M3Banner.withIcon(
                "M3FX can install generated token stylesheets for each JavaFX scene while keeping application scene management explicit.",
                new M3Icon("i", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                learnButton,
                dismissButton
        );
        informational.setPrefWidth(760.0);

        M3Button reviewButton = createButton("Review", M3ButtonVariant.TEXT);
        reviewButton.setOnAction(event -> showActionSnackbar());
        M3Banner warning = M3Banner.withIcon(
                "The selected jlink target uses platform-specific BellSoft LibericaJDK Full jmods.",
                new M3Icon("!", M3IconSize.MEDIUM, M3IconVariant.ERROR),
                reviewButton
        );
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

    /// Creates a button configured with the requested variant.
    private static M3Button createButton(String text, M3ButtonVariant variant) {
        return M3Button.withVariant(text, variant);
    }

    /// Creates a text field for the page gallery.
    private static M3TextField createTextField(
            String prompt,
            String text,
            M3TextInputVariant variant,
            boolean disabled
    ) {
        M3TextField textField = M3TextField.withVariant(text, variant);
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
        M3TextArea textArea = M3TextArea.withVariant(text, variant);
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
        M3Chip chip = M3Chip.withVariant(text, variant, selected);
        chip.setDisable(disabled);
        return chip;
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
        M3SegmentedButton secondButton = M3SegmentedButton.withSelected(second, true);
        M3SegmentedButton thirdButton = new M3SegmentedButton(third);
        return new M3SegmentedButtonGroup(firstButton, secondButton, thirdButton);
    }

    /// Creates a tab bar sample.
    private static M3TabBar createTabBar(String first, String second, String third) {
        return new M3TabBar(M3Tab.withSelected(first, true), new M3Tab(second), new M3Tab(third));
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
        return M3NavigationItem.withSelected(text, createNavigationIcon(iconText), false);
    }

    /// Creates a sample navigation icon.
    private static M3Icon createNavigationIcon(String iconText) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT);
        icon.getStyleClass().add("demo-navigation-icon");
        return icon;
    }

    /// Creates compact icon text from a page title.
    private static String sidebarIconText(String title) {
        Objects.requireNonNull(title, "title");
        StringBuilder builder = new StringBuilder(2);
        for (String part : title.split(" ")) {
            if (!part.isBlank()) {
                builder.append(part.charAt(0));
                if (builder.length() == 2) {
                    break;
                }
            }
        }
        return builder.length() == 0 ? "?" : builder.toString();
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
        return M3IconToggleButton.withVariant(icon, variant, selected);
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
        return M3FloatingActionButton.withGraphic(icon, variant, size);
    }

    /// Creates a sample standalone icon.
    private static M3Icon createDemoIcon(String text, M3IconSize size, M3IconVariant variant) {
        M3Icon icon = new M3Icon(text, size, variant);
        icon.getStyleClass().add("demo-sample-icon");
        return icon;
    }

    /// Creates a sample extended floating action button.
    private static M3FloatingActionButton createExtendedFab() {
        return M3FloatingActionButton.withVariant(
                "Create",
                M3FloatingActionButtonVariant.SURFACE,
                M3FloatingActionButtonSize.REGULAR
        );
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
        animation.play();
        animations.add(animation);
    }

    /// Opens the demo dialog.
    private void showDemoDialog() {
        M3Dialog<Void> dialog = new M3Dialog<>("M3 Dialog");
        M3DialogPane pane = dialog.getM3DialogPane();
        pane.setHeaderText("Dialog title");
        pane.setContentText("This dialog uses the M3FX dialog pane style and active theme tokens.");
        pane.getButtonTypes().add(ButtonType.OK);

        Scene activeScene = scene;
        if (activeScene != null) {
            dialog.initOwner(activeScene.getWindow());
        }
        dialog.showAndWait();
    }

    /// Shows the demo snackbar.
    private void showSnackbar() {
        M3SnackbarHost snackbarHost = this.snackbarHost;
        if (snackbarHost == null) {
            return;
        }
        snackbarHost.show("Theme-aware snackbar");
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

    /// Describes one demo component page.
    private static final class DemoPage {
        /// The page title.
        private final String title;

        /// The page subtitle.
        private final String subtitle;

        /// Creates page content on demand.
        private final Supplier<Node> contentFactory;

        /// Creates a demo page descriptor.
        private DemoPage(String title, String subtitle, Supplier<Node> contentFactory) {
            this.title = Objects.requireNonNull(title, "title");
            this.subtitle = Objects.requireNonNull(subtitle, "subtitle");
            this.contentFactory = Objects.requireNonNull(contentFactory, "contentFactory");
        }

        /// Returns the page title.
        private String title() {
            return title;
        }

        /// Returns the page subtitle.
        private String subtitle() {
            return subtitle;
        }

        /// Creates page content.
        private Node createContent() {
            return contentFactory.get();
        }
    }
}
