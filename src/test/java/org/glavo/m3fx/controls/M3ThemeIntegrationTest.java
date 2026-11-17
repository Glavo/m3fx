// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.localization.M3Localized;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies theme installation, replacement, inheritance, and control refresh.
@NotNullByDefault
final class M3ThemeIntegrationTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// The pulse count used after standalone fallback styles have a ready rendered target.
    private static final int STANDALONE_FALLBACK_STYLE_STABLE_PULSES = 2;

    /// Verifies that scene-level themes follow runtime root replacement.
    @Test
    void sceneThemeInstallationFollowsRootReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            Pane firstRoot = new Pane();
            Pane secondRoot = new Pane();
            Pane thirdRoot = new Pane();
            Scene scene = new Scene(firstRoot);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );

            firstRoot.setStyle("-fx-padding: 4px;");
            secondRoot.setStyle("-fx-background-color: white;");

            M3ThemeManager.install(scene, baselineTheme);

            assertThemedRoot(firstRoot, baselineTheme);
            assertTrue(scene.getStylesheets().contains(M3ThemeRuntime.stylesheetUrl()));
            assertTrue(scene.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(baselineTheme)));

            scene.setRoot(secondRoot);

            assertUnthemedRoot(firstRoot, "-fx-padding: 4px;");
            assertThemedRoot(secondRoot, baselineTheme);

            M3ThemeManager.install(scene, expressiveDarkTheme);

            assertThemedRoot(secondRoot, expressiveDarkTheme);
            assertFalse(scene.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(baselineTheme)));
            assertTrue(scene.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)));

            scene.setRoot(thirdRoot);

            assertUnthemedRoot(secondRoot, "-fx-background-color: white;");
            assertThemedRoot(thirdRoot, expressiveDarkTheme);
        });
    }

    /// Verifies that uninstalling a scene theme stops future root tracking.
    @Test
    void sceneThemeUninstallStopsRootReplacementTracking() {
        FxTestUtils.runOnFxThread(() -> {
            Pane firstRoot = new Pane();
            Pane secondRoot = new Pane();
            Scene scene = new Scene(firstRoot);
            M3Theme theme = M3Theme.defaultTheme();

            M3ThemeManager.install(scene, theme);
            assertThemedRoot(firstRoot, theme);

            M3ThemeManager.uninstall(scene);

            assertUnthemedRoot(firstRoot, "");
            assertFalse(scene.getStylesheets().contains(M3ThemeRuntime.stylesheetUrl()));
            assertFalse(scene.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));

            scene.setRoot(secondRoot);

            assertUnthemedRoot(secondRoot, "");
        });
    }

    /// Verifies that scene-level themes restore a previous local root theme after root replacement.
    @Test
    void sceneThemeRootReplacementRestoresPreviousLocalTheme() {
        FxTestUtils.runOnFxThread(() -> {
            Pane firstRoot = new Pane();
            Pane secondRoot = new Pane();
            firstRoot.setStyle("-fx-padding: 8px;");
            secondRoot.setStyle("-fx-background-color: white;");
            Scene scene = new Scene(firstRoot);
            M3Theme localTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            M3Theme sceneTheme = M3Theme.defaultTheme();

            M3ThemeManager.install(firstRoot, localTheme);
            M3ThemeManager.install(scene, sceneTheme);

            assertThemedRoot(firstRoot, sceneTheme);

            scene.setRoot(secondRoot);

            assertThemedRoot(firstRoot, localTheme);
            assertTrue(firstRoot.getStyle().contains("-fx-padding: 8px;"));
            assertThemedRoot(secondRoot, sceneTheme);
        });
    }

    /// Verifies that uninstalling a scene-level theme restores a previous local root theme.
    @Test
    void sceneThemeUninstallRestoresPreviousLocalRootTheme() {
        FxTestUtils.runOnFxThread(() -> {
            Pane root = new Pane();
            root.setStyle("-fx-padding: 12px;");
            Scene scene = new Scene(root);
            M3Theme localTheme = M3Theme.fromSeed(
                    Color.web("#2d6b27"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            M3Theme sceneTheme = M3Theme.defaultTheme();

            M3ThemeManager.install(root, localTheme);
            M3ThemeManager.install(scene, sceneTheme);

            assertThemedRoot(root, sceneTheme);

            M3ThemeManager.uninstall(scene);

            assertThemedRoot(root, localTheme);
            assertTrue(root.getStyle().contains("-fx-padding: 12px;"));
            assertFalse(scene.getStylesheets().contains(M3ThemeRuntime.stylesheetUrl()));
            assertFalse(scene.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(sceneTheme)));
        });
    }

    /// Asserts that a root has the expected installed theme metadata and classes.
    private static void assertThemedRoot(Parent root, M3Theme theme) {
        assertSame(theme, M3ThemeManager.getTheme(root));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(theme.profile() == M3Profile.EXPRESSIVE_2025
                ? M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS
                : M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(theme.brightness() == Brightness.DARK
                ? M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS
                : M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
    }

    /// Asserts that a root has no installed theme metadata and restored base style.
    private static void assertUnthemedRoot(Parent root, String expectedStyle) {
        assertNull(M3ThemeManager.getTheme(root));
        assertEquals(expectedStyle, root.getStyle());
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
    }

    /// Verifies that standalone control stylesheets provide fallback color tokens without requiring a theme.
    @Tier2Test
    @Test
    void standaloneControlStylesheetsResolveFallbackColorTokens() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(() -> standaloneControlSceneReady(
                    stageReference
            ), STANDALONE_FALLBACK_STYLE_STABLE_PULSES, () -> {
                M3Button filledButton = new M3Button("Filled");
                M3Button tonalButton = new M3Button("Tonal", M3ButtonVariant.TONAL);
                M3Button outlinedButton = new M3Button("Outlined", M3ButtonVariant.OUTLINED);
                M3Button textButton = new M3Button("Text", M3ButtonVariant.TEXT);
                M3Button elevatedButton = new M3Button("Elevated", M3ButtonVariant.ELEVATED);
                M3IconButton iconButton = new M3IconButton(new M3Icon("I"));
                M3IconToggleButton iconToggleButton = new M3IconToggleButton("T");
                iconToggleButton.setSelected(true);
                M3ButtonGroup buttonGroup = buttonGroup(
                        new M3Button("One", M3ButtonVariant.TONAL),
                        new M3Button("Two", M3ButtonVariant.TONAL)
                );
                M3SplitButton splitButton = splitButton("Split", new M3MenuItem("Split item"));
                M3FloatingActionButton smallFab = new M3FloatingActionButton(new M3Icon("+"));
                smallFab.setSize(M3FloatingActionButtonSize.SMALL);
                M3FloatingActionButton largeFab = new M3FloatingActionButton(new M3Icon("*"));
                largeFab.setSize(M3FloatingActionButtonSize.LARGE);
                largeFab.setVariant(M3FloatingActionButtonVariant.PRIMARY);
                M3FabMenu fabMenu = new M3FabMenu();
                fabMenu.getItems().addAll(new M3FloatingActionButton("A"), new M3FloatingActionButton("B"));
                fabMenu.show();

                M3CheckBox checkBox = new M3CheckBox("Check");
                checkBox.setSelected(true);
                M3CheckBox indeterminateCheckBox = new M3CheckBox("Mixed");
                indeterminateCheckBox.setIndeterminate(true);
                M3RadioButton radioButton = new M3RadioButton("Radio");
                radioButton.setSelected(true);
                M3Switch switchControl = new M3Switch("Switch");
                switchControl.setSelected(true);
                M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
                slider.setPrefWidth(220.0);
                M3SegmentedButton segmentedDay = new M3SegmentedButton("Day");
                M3SegmentedButton segmentedWeek = new M3SegmentedButton("Week");
                M3SegmentedButton segmentedMonth = new M3SegmentedButton("Month");
                M3SegmentedButtonGroup segmentedButtonGroup = segmentedButtonGroup(
                        segmentedDay,
                        segmentedWeek,
                        segmentedMonth
                );
                segmentedButtonGroup.selectIndex(1);
                M3FilterChip filterChip = new M3FilterChip("Filter");
                filterChip.setSelected(true);
                M3ChipGroup chipGroup = chipGroup(
                        new M3AssistChip("Assist"),
                        filterChip,
                        new M3InputChip("Input")
                );
                M3IconToggleButtonGroup iconToggleButtonGroup = iconToggleButtonGroup(
                        new M3IconToggleButton("A"),
                        new M3IconToggleButton("B"),
                        new M3IconToggleButton("C")
                );
                iconToggleButtonGroup.selectIndex(0);

                M3ListItem listItem = new M3ListItem("List item");
                listItem.setOverlineText("Overline");
                listItem.setSupportingText("Supporting");
                listItem.setTrailingIcon("T");
                listItem.setSelected(true);
                M3ListPane listPane = listPane(new M3ListItem("Alpha"), new M3ListItem("Beta"));
                listPane.setSelectionMode(M3SelectionMode.SINGLE);
                listPane.selectIndex(0);
                M3ListView<String> listView = listView("Alpha", "Beta", "Gamma", "Delta");
                listView.setSelectionMode(M3SelectionMode.SINGLE);
                listView.selectIndex(1);
                listView.setPrefSize(240.0, 160.0);
                M3Menu menu = new M3Menu(new M3MenuItem("Menu item"));
                M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
                M3MenuButton menuButton = new M3MenuButton("Open menu", new M3MenuItem("Duplicate"), subMenuItem);

                M3NavigationItem navigationHome = new M3NavigationItem("Home", new M3Icon("H"));
                M3NavigationItem navigationSettings = new M3NavigationItem("Settings", new M3Icon("S"));
                M3NavigationBar navigationBar = navigationBar(navigationHome, navigationSettings);
                navigationBar.selectIndex(0);
                M3NavigationRail navigationRail = navigationRail(
                        new M3NavigationItem("Mail", new M3Icon("M")),
                        new M3NavigationItem("Tasks", new M3Icon("T"))
                );
                navigationRail.selectIndex(1);
                M3NavigationDrawerGroup navigationDrawerGroup = new M3NavigationDrawerGroup("Group");
                navigationDrawerGroup.getItems().setAll(new M3ListItem("Child A"), new M3ListItem("Child B"));
                navigationDrawerGroup.setExpanded(true);
                M3NavigationDrawer navigationDrawer = navigationDrawer(
                        new M3ListItem("Inbox"),
                        navigationDrawerGroup,
                        new M3ListItem("Archive")
                );
                navigationDrawer.selectIndex(0);
                M3TabBar tabBar = tabBar(new M3Tab("Tab A"), new M3Tab("Tab B"), new M3Tab("Tab C"));
                tabBar.selectIndex(1);
                M3ListSectionHeader listSectionHeader = new M3ListSectionHeader("List section");
                M3MenuSectionHeader menuSectionHeader = new M3MenuSectionHeader("Menu section");

                M3Text text = new M3Text("Text", M3TextRole.BODY_LARGE);
                M3Avatar avatar = new M3Avatar("A");
                M3Badge badge = new M3Badge("7");
                M3BadgedBox badgedBox = new M3BadgedBox(new M3Icon("N"), new M3Badge("3"));
                M3Divider horizontalDivider = new M3Divider();
                horizontalDivider.setPrefWidth(180.0);
                M3Divider verticalDivider = new M3Divider(Orientation.VERTICAL);
                verticalDivider.setPrefHeight(80.0);
                M3DisclosureIcon disclosureIcon = new M3DisclosureIcon(true);
                M3Surface surface = surface(new Label("Surface"));
                surface.setVariant(M3SurfaceVariant.CONTAINER);
                M3Card card = new M3Card(new Label("Card"), M3CardVariant.FILLED);
                M3Scrim scrim = new M3Scrim();
                scrim.setPrefSize(120.0, 80.0);
                M3BottomSheet bottomSheet = bottomSheet("Bottom sheet", new Label("Sheet content"), new M3Button("Done"));
                M3SideSheet sideSheet = sideSheet("Side sheet", new Label("Sheet content"), new M3Button("Close"));
                M3Carousel carousel = carousel(
                        new M3Card(new Label("One")),
                        new M3Card(new Label("Two")),
                        new M3Card(new Label("Three"))
                );
                carousel.selectIndex(1);
                carousel.setPrefSize(360.0, 150.0);

                M3TextField textField = new M3TextField("Text");
                M3PasswordField passwordField = new M3PasswordField("Secret");
                M3TextArea textArea = new M3TextArea("Multiline");
                textArea.setPrefRowCount(3);
                M3TextInputLayout textInputLayout = new M3TextInputLayout(new M3TextField("Wrapped"));
                textInputLayout.setLabelText("Label");
                textInputLayout.setSupportingText("Supporting");
                M3SearchBar searchBar = new M3SearchBar("Search");
                searchBar.setText("Query");
                searchBar.setActive(true);
                M3SearchView searchView = searchView(
                        "Search view",
                        new M3ListItem("Result A"),
                        new M3ListItem("Result B")
                );
                searchView.setActive(true);
                searchView.setPrefSize(320.0, 180.0);
                M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 6, 27));
                M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                        LocalDate.of(2026, 6, 27),
                        LocalDate.of(2026, 6, 29)
                );
                M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));
                M3DatePickerField datePickerField = new M3DatePickerField();
                M3TimePickerField timePickerField = new M3TimePickerField();
                M3DateRangePickerField dateRangePickerField = new M3DateRangePickerField();

                M3ProgressBar determinateProgressBar = new M3ProgressBar(0.62);
                determinateProgressBar.setPrefWidth(220.0);
                M3ProgressBar indeterminateProgressBar = new M3ProgressBar();
                indeterminateProgressBar.setPrefWidth(220.0);
                M3ProgressIndicator determinateProgressIndicator = new M3ProgressIndicator(0.62);
                M3ProgressIndicator indeterminateProgressIndicator = new M3ProgressIndicator();
                M3LoadingIndicator defaultLoadingIndicator = new M3LoadingIndicator();
                M3LoadingIndicator containedLoadingIndicator = new M3LoadingIndicator();
                containedLoadingIndicator.setVariant(M3LoadingIndicatorVariant.CONTAINED);

                M3Banner banner = banner("Banner", new M3Button("Action", M3ButtonVariant.TEXT));
                M3OverlayPane overlayPane = new M3OverlayPane();
                overlayPane.setSnackbarDisplayDuration(Duration.INDEFINITE);
                M3DialogPane dialogPane = new M3DialogPane();
                dialogPane.setHeaderText("Dialog");
                dialogPane.setContentText("Dialog content");
                M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
                okAction.setDefaultButton(true);
                M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
                cancelAction.setCancelButton(true);
                dialogPane.getActions().setAll(okAction, cancelAction);
                M3TopAppBar topAppBar = topAppBar(
                        "Top app bar",
                        M3TopAppBarVariant.MEDIUM,
                        new M3IconButton(new M3Icon("M")),
                        new M3IconButton(new M3Icon("S"))
                );
                topAppBar.setScrolledUnder(true);
                M3BottomAppBar bottomAppBar = bottomAppBar(
                        M3BottomAppBarFloatingActionAlignment.END,
                        new M3FloatingActionButton(new M3Icon("+")),
                        new M3IconButton(new M3Icon("A")),
                        new M3IconButton(new M3Icon("B"))
                );
                M3Toolbar toolbar = toolbar(new M3IconButton(new M3Icon("B")), new M3IconButton(new M3Icon("I")));
                toolbar.setVariant(M3ToolbarVariant.FLOATING);
                M3FormRow formRow = new M3FormRow("Field", "Supporting", new M3TextField("Value"));
                M3FormSection formSection = formSection("Section", "Supporting", formRow);
                M3FormPane formPane = formPane(formSection);
                M3ValidationSummary validationSummary = new M3ValidationSummary();
                validationSummary.setShowWhenValid(true);

                FlowPane root = new FlowPane(
                        12.0,
                        12.0,
                        filledButton,
                        tonalButton,
                        outlinedButton,
                        textButton,
                        elevatedButton,
                        iconButton,
                        iconToggleButton,
                        buttonGroup,
                        splitButton,
                        smallFab,
                        largeFab,
                        fabMenu,
                        checkBox,
                        indeterminateCheckBox,
                        radioButton,
                        switchControl,
                        slider,
                        segmentedButtonGroup,
                        chipGroup,
                        iconToggleButtonGroup,
                        listItem,
                        listPane,
                        listView,
                        menu,
                        menuButton,
                        navigationBar,
                        navigationRail,
                        navigationDrawer,
                        tabBar,
                        listSectionHeader,
                        menuSectionHeader,
                        text,
                        avatar,
                        badge,
                        badgedBox,
                        horizontalDivider,
                        verticalDivider,
                        disclosureIcon,
                        surface,
                        card,
                        scrim,
                        bottomSheet,
                        sideSheet,
                        carousel,
                        textField,
                        passwordField,
                        textArea,
                        textInputLayout,
                        searchBar,
                        searchView,
                        datePicker,
                        dateRangePicker,
                        timePicker,
                        datePickerField,
                        timePickerField,
                        dateRangePickerField,
                        determinateProgressBar,
                        indeterminateProgressBar,
                        determinateProgressIndicator,
                        indeterminateProgressIndicator,
                        defaultLoadingIndicator,
                        containedLoadingIndicator,
                        banner,
                        dialogPane,
                        topAppBar,
                        bottomAppBar,
                        toolbar,
                        formPane,
                        validationSummary
                );
                root.setPrefWrapLength(1180.0);
                overlayPane.setContent(root);
                Scene scene = new Scene(overlayPane, 1180.0, 900.0);
                Stage stage = new Stage();

                stageReference.set(stage);
                menuButtonReference.set(menuButton);
                subMenuItemReference.set(subMenuItem);
                stage.setScene(scene);
                stage.show();
                overlayPane.showSnackbar(new M3Snackbar("Hosted snackbar"));
                overlayPane.applyCss();
                overlayPane.layout();
                menuButton.showMenu();
                subMenuItem.showSubMenu();
            }, () -> assertTrue(standaloneControlSceneReady(stageReference)));
        } finally {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                if (subMenuItem != null) {
                    subMenuItem.hideSubMenu();
                }
                @Nullable M3MenuButton menuButton = menuButtonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that standalone picker field popups resolve fallback color tokens without requiring a theme.
    @Tier2Test
    @Test
    void standalonePickerFieldPopupsResolveFallbackColorTokens() throws InterruptedException {
        M3DatePickerField datePickerField = FxTestUtils.callOnFxThread(M3DatePickerField::new);
        M3TimePickerField timePickerField = FxTestUtils.callOnFxThread(M3TimePickerField::new);
        M3DateRangePickerField dateRangePickerField = FxTestUtils.callOnFxThread(M3DateRangePickerField::new);

        assertStandalonePickerPopupReady(datePickerField);
        assertStandalonePickerPopupReady(timePickerField);
        assertStandalonePickerPopupReady(dateRangePickerField);
    }

    /// Verifies that standalone tooltip popups resolve fallback color tokens without requiring a theme.
    @Tier2Test
    @Test
    void standaloneTooltipPopupsResolveFallbackColorTokens() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> plainTooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> richTooltipReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(() -> standaloneTooltipPopupsReady(
                    stageReference,
                    plainTooltipReference,
                    richTooltipReference
            ), STANDALONE_FALLBACK_STYLE_STABLE_PULSES, () -> {
                M3Button plainOwner = new M3Button("Plain owner");
                M3Button richOwner = new M3Button("Rich owner");
                M3Tooltip plainTooltip = new M3Tooltip("Plain tooltip");
                M3RichTooltip richTooltip = richTooltip(
                        "Rich tooltip",
                        "Standalone rich tooltip content",
                        new M3Button("Action", M3ButtonVariant.TEXT)
                );
                VBox root = new VBox(16.0, plainOwner, richOwner);
                Scene scene = new Scene(root, 360.0, 180.0);
                Stage stage = new Stage();

                stageReference.set(stage);
                plainTooltipReference.set(plainTooltip);
                richTooltipReference.set(richTooltip);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                plainTooltip.show(plainOwner, stage.getX() + 40.0, stage.getY() + 72.0);
                richTooltip.show(richOwner, stage.getX() + 40.0, stage.getY() + 124.0);
            }, () -> {
                assertStandaloneTooltipPopupReady(plainTooltipReference.get(), "m3-tooltip");
                assertStandaloneTooltipPopupReady(richTooltipReference.get(), "m3-rich-tooltip-container");
            });
        } finally {
            closeStandaloneTooltipScene(stageReference, plainTooltipReference, richTooltipReference);
        }
    }

    /// Verifies that standalone controls resolve internal icon colors from fallback token CSS.
    @Tier2Test
    @Test
    void standaloneInternalIconsResolveFallbackTokenColors() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3InternalIcon> iconReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(() -> standaloneInternalIconReady(stageReference, iconReference),
                    STANDALONE_FALLBACK_STYLE_STABLE_PULSES, () -> {
                        M3SearchBar searchBar = new M3SearchBar("Search");
                        VBox root = new VBox(searchBar);
                        Scene scene = new Scene(root, 360.0, 120.0);
                        Stage stage = new Stage();

                        stageReference.set(stage);
                        iconReference.set(assertInstanceOf(M3InternalIcon.class, searchBar.getLeading()));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();
                    }, () -> {
                        M3InternalIcon icon = Objects.requireNonNull(iconReference.get(), "icon");
                        @Nullable Paint fill = icon.getPath().getFill();
                        assertEquals(Color.rgb(29, 27, 32), fill, "standalone internal icon fallback fill");
                    });
        } finally {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that component metric tokens preserve application-bound JavaFX layout properties.
    @Test
    void componentMetricTokensPreserveBoundLayoutProperties() {
        M3Button button = new M3Button("Button");
        assertBoundRegionMetricsPreserved(button, () -> {
            button.setContainerHeight(52.0);
            button.setHorizontalPadding(18.0);
        }, () -> {
            assertEquals(52.0, button.getContainerHeight(), 0.0001);
            assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        });

        M3FloatingActionButton fab = new M3FloatingActionButton("Create");
        assertBoundRegionMetricsPreserved(fab, () -> {
            fab.setContainerSize(64.0);
            fab.setHorizontalPadding(24.0);
        }, () -> {
            assertEquals(64.0, fab.getContainerSize(), 0.0001);
            assertEquals(24.0, fab.getHorizontalPadding(), 0.0001);
        });

        M3Chip chip = new M3AssistChip("Filter");
        assertBoundRegionMetricsPreserved(chip, () -> {
            chip.setContainerHeight(40.0);
            chip.setHorizontalPadding(20.0);
        }, () -> {
            assertEquals(40.0, chip.getContainerHeight(), 0.0001);
            assertEquals(20.0, chip.getHorizontalPadding(), 0.0001);
        });

        M3SegmentedButton segmentedButton = new M3SegmentedButton("Day");
        assertBoundRegionMetricsPreserved(segmentedButton, () -> {
            segmentedButton.setContainerHeight(44.0);
            segmentedButton.setHorizontalPadding(22.0);
        }, () -> {
            assertEquals(44.0, segmentedButton.getContainerHeight(), 0.0001);
            assertEquals(22.0, segmentedButton.getHorizontalPadding(), 0.0001);
        });

        M3Tab tab = new M3Tab("Tab");
        assertBoundRegionMetricsPreserved(tab, () -> {
            tab.setContainerHeight(52.0);
            tab.setTabMinWidth(96.0);
            tab.setHorizontalPadding(18.0);
        }, () -> {
            assertEquals(52.0, tab.getContainerHeight(), 0.0001);
            assertEquals(96.0, tab.getTabMinWidth(), 0.0001);
            assertEquals(18.0, tab.getHorizontalPadding(), 0.0001);
        });

        M3CheckBox checkBox = new M3CheckBox("Check");
        assertBoundRegionMetricsPreserved(checkBox, () -> {
            checkBox.setTouchTargetSize(54.0);
            checkBox.setStateLayerSize(42.0);
        }, () -> {
            assertEquals(54.0, checkBox.getTouchTargetSize(), 0.0001);
            assertEquals(42.0, checkBox.getStateLayerSize(), 0.0001);
        });

        M3RadioButton radioButton = new M3RadioButton("Radio");
        assertBoundRegionMetricsPreserved(radioButton, () -> {
            radioButton.setTouchTargetSize(52.0);
            radioButton.setStateLayerSize(38.0);
        }, () -> {
            assertEquals(52.0, radioButton.getTouchTargetSize(), 0.0001);
            assertEquals(38.0, radioButton.getStateLayerSize(), 0.0001);
        });

        M3Switch switchControl = new M3Switch("Switch");
        assertBoundRegionMetricsPreserved(switchControl, () -> {
            switchControl.setTouchTargetSize(60.0);
            switchControl.setStateLayerSize(46.0);
        }, () -> {
            assertEquals(60.0, switchControl.getTouchTargetSize(), 0.0001);
            assertEquals(46.0, switchControl.getStateLayerSize(), 0.0001);
        });

        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        assertBoundRegionMetricsPreserved(progressBar, () -> {
            progressBar.setTrackThickness(10.0);
            progressBar.setWaveAmplitude(6.0);
        }, () -> {
            assertEquals(10.0, progressBar.getTrackThickness(), 0.0001);
            assertEquals(6.0, progressBar.getWaveAmplitude(), 0.0001);
        });

        M3BottomAppBar bottomAppBar = bottomAppBar();
        assertBoundRegionMetricsPreserved(bottomAppBar, () -> {
            bottomAppBar.setContainerHeight(88.0);
            bottomAppBar.setHorizontalPadding(20.0);
        }, () -> {
            assertEquals(88.0, bottomAppBar.getContainerHeight(), 0.0001);
            assertEquals(20.0, bottomAppBar.getHorizontalPadding(), 0.0001);
        });

        M3TopAppBar topAppBar = topAppBar("Title");
        assertBoundRegionMetricsPreserved(topAppBar, () -> {
            topAppBar.setContainerHeight(72.0);
            topAppBar.setHorizontalPadding(20.0);
        }, () -> {
            assertEquals(72.0, topAppBar.getContainerHeight(), 0.0001);
            assertEquals(20.0, topAppBar.getHorizontalPadding(), 0.0001);
        });

        M3Toolbar toolbar = new M3Toolbar();
        assertBoundRegionMetricsPreserved(toolbar, () -> {
            toolbar.setContainerHeight(64.0);
            toolbar.setContainerWidth(72.0);
            toolbar.setContentPadding(12.0);
            toolbar.setOrientation(Orientation.VERTICAL);
        }, () -> {
            assertEquals(64.0, toolbar.getContainerHeight(), 0.0001);
            assertEquals(72.0, toolbar.getContainerWidth(), 0.0001);
            assertEquals(12.0, toolbar.getContentPadding(), 0.0001);
            assertEquals(Orientation.VERTICAL, toolbar.getOrientation());
        });

        M3Surface surface = surface(new Label("Surface"));
        assertBoundRegionMetricsPreserved(surface, () -> surface.setContentPadding(22.0), () ->
                assertEquals(22.0, surface.getContentPadding(), 0.0001));

        M3DialogPane dialogPane = new M3DialogPane();
        assertBoundRegionMetricsPreserved(dialogPane, () -> {
            dialogPane.setContentPadding(28.0);
            dialogPane.setContainerMinWidth(320.0);
            dialogPane.setContainerMaxWidth(520.0);
        }, () -> {
            assertEquals(28.0, dialogPane.getContentPadding(), 0.0001);
            assertEquals(320.0, dialogPane.getContainerMinWidth(), 0.0001);
            assertEquals(520.0, dialogPane.getContainerMaxWidth(), 0.0001);
        });

        M3Banner banner = banner("Message");
        assertBoundRegionMetricsPreserved(banner, () -> {
            banner.setContainerMinHeight(92.0);
            banner.setVerticalPadding(22.0);
            banner.setHorizontalPadding(30.0);
        }, () -> {
            assertEquals(92.0, banner.getContainerMinHeight(), 0.0001);
            assertEquals(22.0, banner.getVerticalPadding(), 0.0001);
            assertEquals(30.0, banner.getHorizontalPadding(), 0.0001);
        });
    }

    /// Verifies that generated component token stylesheets preserve application-bound JavaFX layout properties.
    @Test
    void generatedComponentTokenStylesPreserveBoundLayoutProperties() {
        assertGeneratedThemeCssPreservesBoundRegionMetrics(new M3ValidationSummary());
        assertGeneratedThemeCssPreservesBoundRegionMetrics(new M3Menu(new M3MenuItem("Item")));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(new M3DialogPane());
        assertGeneratedThemeCssPreservesBoundRegionMetrics(banner("Message"));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(sideSheet("Details", new Label("Side content")));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(bottomSheet("Queue", new Label("Bottom content")));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(topAppBar("Title"));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(bottomAppBar());
        assertGeneratedThemeCssPreservesBoundRegionMetrics(navigationBar(new M3NavigationItem("Home")));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(navigationRail(new M3NavigationItem("Home")));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(navigationDrawer(new M3ListItem("Home")));
        assertGeneratedThemeCssPreservesBoundRegionMetrics(new M3ListSectionHeader("Section"));
    }

    /// Verifies that component metric tokens preserve application-set JavaFX layout properties.
    @Test
    void componentMetricTokensPreserveApplicationSetLayoutProperties() {
        M3Button button = new M3Button("Button");
        button.setPrefHeight(101.0);
        button.setPadding(new Insets(2.0, 3.0, 4.0, 5.0));
        button.setContainerHeight(52.0);
        button.setHorizontalPadding(18.0);

        assertEquals(52.0, button.getContainerHeight(), 0.0001);
        assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(101.0, button.getPrefHeight(), 0.0001);
        assertEquals(new Insets(2.0, 3.0, 4.0, 5.0), button.getPadding());

        M3TextField textField = new M3TextField("Text");
        textField.setPrefHeight(103.0);
        textField.setPadding(new Insets(6.0, 7.0, 8.0, 9.0));
        textField.setContainerHeight(64.0);
        textField.setHorizontalPadding(22.0);
        textField.setVerticalPadding(12.0);

        assertEquals(64.0, textField.getContainerHeight(), 0.0001);
        assertEquals(22.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, textField.getVerticalPadding(), 0.0001);
        assertEquals(103.0, textField.prefHeight(-1.0), 0.0001);
        assertEquals(new Insets(6.0, 7.0, 8.0, 9.0), textField.getPadding());

        M3Toolbar toolbar = new M3Toolbar();
        toolbar.setMinHeight(111.0);
        toolbar.setPrefHeight(112.0);
        toolbar.setPadding(new Insets(10.0, 11.0, 12.0, 13.0));
        toolbar.setContainerHeight(72.0);
        toolbar.setContentPadding(16.0);

        assertEquals(72.0, toolbar.getContainerHeight(), 0.0001);
        assertEquals(16.0, toolbar.getContentPadding(), 0.0001);
        assertEquals(111.0, toolbar.getMinHeight(), 0.0001);
        assertEquals(112.0, toolbar.getPrefHeight(), 0.0001);
        assertEquals(new Insets(10.0, 11.0, 12.0, 13.0), toolbar.getPadding());
    }

    /// Verifies that explicit component token API values survive later stylesheet passes.
    @Test
    void coreComponentTokenApiValuesSurviveStylesheetPasses() {
        M3Button button = new M3Button("Button");
        button.setStyle("-m3-container-height: 40px; -m3-container-shape: 999px; -m3-horizontal-padding: 24px;");
        button.setContainerHeight(52.0);
        button.setContainerShape(14.0);
        button.setHorizontalPadding(18.0);

        M3TextField textField = createTextField("Text", M3TextInputVariant.OUTLINED);
        textField.setStyle("-m3-container-height: 56px; -m3-container-shape: 4px; -m3-horizontal-padding: 16px;");
        textField.setContainerHeight(64.0);
        textField.setContainerShape(12.0);
        textField.setHorizontalPadding(22.0);

        M3CheckBox checkBox = new M3CheckBox("Check");
        checkBox.setStyle("""
                -m3-touch-target-size: 48px;
                -m3-state-layer-size: 40px;
                -m3-container-size: 18px;
                -m3-selected-mark-width: 12px;
                -m3-selected-mark-height: 10px;
                -m3-indeterminate-mark-width: 12px;
                -m3-indeterminate-mark-height: 2px;
                """);
        checkBox.setTouchTargetSize(54.0);
        checkBox.setStateLayerSize(42.0);
        checkBox.setContainerSize(20.0);
        checkBox.setSelectedMarkWidth(14.0);
        checkBox.setSelectedMarkHeight(11.0);
        checkBox.setIndeterminateMarkWidth(15.0);
        checkBox.setIndeterminateMarkHeight(3.0);

        M3RadioButton radioButton = new M3RadioButton("Radio");
        radioButton.setStyle("""
                -m3-touch-target-size: 48px;
                -m3-state-layer-size: 40px;
                -m3-container-size: 20px;
                -m3-selected-dot-size: 10px;
                """);
        radioButton.setTouchTargetSize(52.0);
        radioButton.setStateLayerSize(38.0);
        radioButton.setContainerSize(22.0);
        radioButton.setSelectedDotSize(12.0);

        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setStyle(
                "-m3-track-thickness: 4px; "
                        + "-m3-track-shape: 999px; "
                        + "-m3-stop-indicator-size: 4px; "
                        + "-m3-stop-indicator-trailing-space: 4px; "
                        + "-m3-thumb-size: 44px; "
                        + "-m3-thumb-width: 4px; "
                        + "-m3-thumb-track-gap: 6px; "
                        + "-m3-touch-target-size: 48px; "
                        + "-m3-slider-icon-size: 0px; "
                        + "-m3-slider-icon-padding: 0px; "
                        + "-m3-value-indicator-bottom-space: 12px;"
        );
        slider.setTrackThickness(8.0);
        slider.setTrackShape(12.0);
        slider.setStopIndicatorSize(6.0);
        slider.setStopIndicatorTrailingSpace(7.0);
        slider.setThumbSize(52.0);
        slider.setThumbWidth(8.0);
        slider.setThumbTrackGap(10.0);
        slider.setTouchTargetSize(56.0);
        slider.setIconSize(18.0);
        slider.setIconPadding(9.0);
        slider.setValueIndicatorBottomSpace(14.0);

        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setStyle("""
                -m3-touch-target-size: 50px;
                -m3-track-shape: 18px;
                -m3-track-width: 60px;
                -m3-track-height: 36px;
                -m3-state-layer-size: 44px;
                -m3-unselected-handle-size: 18px;
                -m3-with-icon-handle-size: 27px;
                -m3-selected-handle-size: 26px;
                -m3-pressed-handle-size: 30px;
                -m3-icon-size: 15px;
                """);
        switchControl.setTouchTargetSize(60.0);
        switchControl.setTrackShape(10.0);
        switchControl.setTrackWidth(66.0);
        switchControl.setTrackHeight(38.0);
        switchControl.setStateLayerSize(46.0);
        switchControl.setUnselectedHandleSize(20.0);
        switchControl.setSelectedHandleSize(28.0);
        switchControl.setPressedHandleSize(32.0);

        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        progressBar.setStyle(
                "-m3-track-thickness: 4px; "
                        + "-m3-track-shape: 999px; "
                        + "-m3-wave-amplitude: 2px; "
                        + "-m3-wavelength: 48px; "
                        + "-m3-track-gap: 4px; "
                        + "-m3-stop-size: 4px;"
        );
        progressBar.setTrackThickness(10.0);
        progressBar.setTrackShape(5.0);
        progressBar.setWaveAmplitude(6.0);
        progressBar.setWavelength(36.0);
        progressBar.setTrackGap(8.0);
        progressBar.setStopSize(7.0);

        Pane root = new Pane(button, textField, checkBox, radioButton, slider, switchControl, progressBar);
        Scene scene = new Scene(root, 520.0, 320.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.applyCss();

        assertEquals(52.0, button.getContainerHeight(), 0.0001);
        assertEquals(14.0, button.getContainerShape(), 0.0001);
        assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(52.0, button.prefHeight(-1.0), 0.0001);

        assertEquals(64.0, textField.getContainerHeight(), 0.0001);
        assertEquals(12.0, textField.getContainerShape(), 0.0001);
        assertEquals(22.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, textField.prefHeight(-1.0), 0.0001);

        assertEquals(54.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(42.0, checkBox.getStateLayerSize(), 0.0001);
        assertEquals(20.0, checkBox.getContainerSize(), 0.0001);
        assertEquals(14.0, checkBox.getSelectedMarkWidth(), 0.0001);
        assertEquals(11.0, checkBox.getSelectedMarkHeight(), 0.0001);
        assertEquals(15.0, checkBox.getIndeterminateMarkWidth(), 0.0001);
        assertEquals(3.0, checkBox.getIndeterminateMarkHeight(), 0.0001);
        assertEquals(54.0, checkBox.prefHeight(-1.0), 0.0001);

        assertEquals(52.0, radioButton.getTouchTargetSize(), 0.0001);
        assertEquals(38.0, radioButton.getStateLayerSize(), 0.0001);
        assertEquals(22.0, radioButton.getContainerSize(), 0.0001);
        assertEquals(12.0, radioButton.getSelectedDotSize(), 0.0001);
        assertEquals(52.0, radioButton.prefHeight(-1.0), 0.0001);

        assertEquals(8.0, slider.getTrackThickness(), 0.0001);
        assertEquals(12.0, slider.getTrackShape(), 0.0001);
        assertEquals(6.0, slider.getStopIndicatorSize(), 0.0001);
        assertEquals(7.0, slider.getStopIndicatorTrailingSpace(), 0.0001);
        assertEquals(52.0, slider.getThumbSize(), 0.0001);
        assertEquals(8.0, slider.getThumbWidth(), 0.0001);
        assertEquals(10.0, slider.getThumbTrackGap(), 0.0001);
        assertEquals(56.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(18.0, slider.getIconSize(), 0.0001);
        assertEquals(9.0, slider.getIconPadding(), 0.0001);
        assertEquals(14.0, slider.getValueIndicatorBottomSpace(), 0.0001);
        assertEquals(56.0, slider.prefHeight(-1.0), 0.0001);

        assertEquals(60.0, switchControl.getTouchTargetSize(), 0.0001);
        assertEquals(10.0, switchControl.getTrackShape(), 0.0001);
        assertEquals(66.0, switchControl.getTrackWidth(), 0.0001);
        assertEquals(38.0, switchControl.getTrackHeight(), 0.0001);
        assertEquals(46.0, switchControl.getStateLayerSize(), 0.0001);
        assertEquals(20.0, switchControl.getUnselectedHandleSize(), 0.0001);
        assertEquals(28.0, switchControl.getSelectedHandleSize(), 0.0001);
        assertEquals(32.0, switchControl.getPressedHandleSize(), 0.0001);
        assertEquals(60.0, switchControl.prefHeight(-1.0), 0.0001);

        assertEquals(10.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(5.0, progressBar.getTrackShape(), 0.0001);
        assertEquals(6.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(36.0, progressBar.getWavelength(), 0.0001);
        assertEquals(8.0, progressBar.getTrackGap(), 0.0001);
        assertEquals(7.0, progressBar.getStopSize(), 0.0001);
    }

    /// Verifies that explicit component token API values survive full theme and profile reinstallation.
    @Test
    void componentTokenApiValuesSurviveThemeReinstallation() {
        M3Button button = new M3Button("Button");
        button.setContainerHeight(54.0);
        button.setContainerShape(13.0);
        button.setHorizontalPadding(19.0);

        M3TextField textField = createTextField("Text", M3TextInputVariant.OUTLINED);
        textField.setContainerHeight(62.0);
        textField.setContainerShape(11.0);
        textField.setHorizontalPadding(21.0);
        textField.setVerticalPadding(9.0);

        M3CheckBox checkBox = new M3CheckBox("Check");
        checkBox.setTouchTargetSize(52.0);
        checkBox.setStateLayerSize(42.0);
        checkBox.setContainerSize(20.0);
        checkBox.setSelectedMarkWidth(14.0);
        checkBox.setSelectedMarkHeight(11.0);
        checkBox.setIndeterminateMarkWidth(15.0);
        checkBox.setIndeterminateMarkHeight(3.0);

        M3RadioButton radioButton = new M3RadioButton("Radio");
        radioButton.setTouchTargetSize(50.0);
        radioButton.setStateLayerSize(40.0);
        radioButton.setContainerSize(22.0);
        radioButton.setSelectedDotSize(12.0);

        M3Switch switchControl = new M3Switch("Switch");
        switchControl.setTouchTargetSize(58.0);
        switchControl.setTrackShape(9.0);
        switchControl.setTrackWidth(64.0);
        switchControl.setTrackHeight(34.0);
        switchControl.setStateLayerSize(42.0);
        switchControl.setUnselectedHandleSize(18.0);
        switchControl.setWithIconHandleSize(27.0);
        switchControl.setSelectedHandleSize(26.0);
        switchControl.setPressedHandleSize(30.0);
        switchControl.setIconSize(15.0);

        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setTrackThickness(7.0);
        slider.setTrackShape(11.0);
        slider.setStopIndicatorTrailingSpace(8.0);
        slider.setThumbSize(27.0);
        slider.setThumbWidth(5.0);
        slider.setThumbTrackGap(9.0);
        slider.setTouchTargetSize(55.0);

        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        progressBar.setTrackThickness(9.0);
        progressBar.setTrackShape(4.0);
        progressBar.setWaveAmplitude(5.0);
        progressBar.setWavelength(34.0);
        progressBar.setTrackGap(7.0);
        progressBar.setStopSize(6.0);

        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        progressIndicator.setTrackThickness(7.0);
        progressIndicator.setIndicatorSize(60.0);
        progressIndicator.setWaveAmplitude(5.0);
        progressIndicator.setWavelength(26.0);
        progressIndicator.setTrackGap(6.0);

        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        loadingIndicator.setContainerSize(62.0);
        loadingIndicator.setIndicatorSize(42.0);

        M3Icon icon = new M3Icon("A");
        icon.setIconFontFamily("Serif");
        icon.setIconSize(30.0);
        icon.setIconFontWeight(600.0);

        M3Text text = new M3Text("Text", M3TextRole.TITLE_MEDIUM);
        text.setTypographyFontFamily("Serif");
        text.setTypographyFontSize(18.0);
        text.setTypographyLineHeight(24.0);
        text.setTypographyFontWeight(600.0);

        M3Surface surface = surface(new Label("Surface"));
        surface.setContainerShape(18.0);
        surface.setContentPadding(22.0);

        M3Badge badge = new M3Badge("9");
        badge.setSmallSize(7.0);
        badge.setLargeHeight(21.0);
        badge.setLargeMinWidth(23.0);
        badge.setContainerShape(10.0);
        badge.setHorizontalPadding(5.0);

        M3Divider divider = new M3Divider();
        divider.setThickness(2.0);
        divider.setInsetStart(14.0);
        divider.setInsetEnd(18.0);

        Pane root = new Pane(
                button,
                textField,
                checkBox,
                radioButton,
                switchControl,
                slider,
                progressBar,
                progressIndicator,
                loadingIndicator,
                icon,
                text,
                surface,
                badge,
                divider
        );
        Scene scene = new Scene(root, 760.0, 480.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();
        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#6750a4"),
                M3Profile.BASELINE_2021,
                Brightness.DARK
        ));
        root.applyCss();

        assertEquals(54.0, button.getContainerHeight(), 0.0001);
        assertEquals(13.0, button.getContainerShape(), 0.0001);
        assertEquals(19.0, button.getHorizontalPadding(), 0.0001);

        assertEquals(62.0, textField.getContainerHeight(), 0.0001);
        assertEquals(11.0, textField.getContainerShape(), 0.0001);
        assertEquals(21.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(9.0, textField.getVerticalPadding(), 0.0001);

        assertEquals(52.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(42.0, checkBox.getStateLayerSize(), 0.0001);
        assertEquals(20.0, checkBox.getContainerSize(), 0.0001);
        assertEquals(14.0, checkBox.getSelectedMarkWidth(), 0.0001);
        assertEquals(11.0, checkBox.getSelectedMarkHeight(), 0.0001);
        assertEquals(15.0, checkBox.getIndeterminateMarkWidth(), 0.0001);
        assertEquals(3.0, checkBox.getIndeterminateMarkHeight(), 0.0001);
        assertEquals(50.0, radioButton.getTouchTargetSize(), 0.0001);
        assertEquals(40.0, radioButton.getStateLayerSize(), 0.0001);
        assertEquals(22.0, radioButton.getContainerSize(), 0.0001);
        assertEquals(12.0, radioButton.getSelectedDotSize(), 0.0001);
        assertEquals(58.0, switchControl.getTouchTargetSize(), 0.0001);
        assertEquals(9.0, switchControl.getTrackShape(), 0.0001);
        assertEquals(64.0, switchControl.getTrackWidth(), 0.0001);
        assertEquals(34.0, switchControl.getTrackHeight(), 0.0001);
        assertEquals(42.0, switchControl.getStateLayerSize(), 0.0001);
        assertEquals(18.0, switchControl.getUnselectedHandleSize(), 0.0001);
        assertEquals(27.0, switchControl.getWithIconHandleSize(), 0.0001);
        assertEquals(26.0, switchControl.getSelectedHandleSize(), 0.0001);
        assertEquals(30.0, switchControl.getPressedHandleSize(), 0.0001);
        assertEquals(15.0, switchControl.getIconSize(), 0.0001);

        assertEquals(7.0, slider.getTrackThickness(), 0.0001);
        assertEquals(11.0, slider.getTrackShape(), 0.0001);
        assertEquals(8.0, slider.getStopIndicatorTrailingSpace(), 0.0001);
        assertEquals(27.0, slider.getThumbSize(), 0.0001);
        assertEquals(5.0, slider.getThumbWidth(), 0.0001);
        assertEquals(9.0, slider.getThumbTrackGap(), 0.0001);
        assertEquals(55.0, slider.getTouchTargetSize(), 0.0001);

        assertEquals(9.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(4.0, progressBar.getTrackShape(), 0.0001);
        assertEquals(5.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(34.0, progressBar.getWavelength(), 0.0001);
        assertEquals(7.0, progressBar.getTrackGap(), 0.0001);
        assertEquals(6.0, progressBar.getStopSize(), 0.0001);

        assertEquals(7.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(60.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(5.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(26.0, progressIndicator.getWavelength(), 0.0001);
        assertEquals(6.0, progressIndicator.getTrackGap(), 0.0001);

        assertEquals(62.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(42.0, loadingIndicator.getIndicatorSize(), 0.0001);

        assertEquals("Serif", icon.getIconFontFamily());
        assertEquals(30.0, icon.getIconSize(), 0.0001);
        assertEquals(600.0, icon.getIconFontWeight(), 0.0001);

        assertEquals("Serif", text.getTypographyFontFamily());
        assertEquals(18.0, text.getTypographyFontSize(), 0.0001);
        assertEquals(24.0, text.getTypographyLineHeight(), 0.0001);
        assertEquals(600.0, text.getTypographyFontWeight(), 0.0001);

        assertEquals(18.0, surface.getContainerShape(), 0.0001);
        assertEquals(22.0, surface.getContentPadding(), 0.0001);

        assertEquals(7.0, badge.getSmallSize(), 0.0001);
        assertEquals(21.0, badge.getLargeHeight(), 0.0001);
        assertEquals(23.0, badge.getLargeMinWidth(), 0.0001);
        assertEquals(10.0, badge.getContainerShape(), 0.0001);
        assertEquals(5.0, badge.getHorizontalPadding(), 0.0001);

        assertEquals(2.0, divider.getThickness(), 0.0001);
        assertEquals(14.0, divider.getInsetStart(), 0.0001);
        assertEquals(18.0, divider.getInsetEnd(), 0.0001);
    }

    /// Verifies that extended component token API values survive later stylesheet passes.
    @Test
    void extendedComponentTokenApiValuesSurviveStylesheetPasses() {
        M3FloatingActionButton fab = new M3FloatingActionButton("Create");
        fab.setStyle("-m3-container-size: 56px; -m3-container-shape: 16px; -m3-horizontal-padding: 20px;");
        fab.setContainerSize(72.0);
        fab.setContainerShape(24.0);
        fab.setHorizontalPadding(28.0);

        M3Chip chip = new M3AssistChip("Chip");
        chip.setStyle("-m3-container-height: 32px; -m3-container-shape: 8px; -m3-horizontal-padding: 8px;");
        chip.setContainerHeight(40.0);
        chip.setContainerShape(12.0);
        chip.setHorizontalPadding(14.0);

        M3IconToggleButton toggleButton = new M3IconToggleButton("A");
        toggleButton.setStyle("-m3-container-height: 40px; -m3-container-shape: 20px;");
        toggleButton.setContainerHeight(48.0);
        toggleButton.setContainerShape(10.0);

        M3SegmentedButton segmentedButton = new M3SegmentedButton("Day");
        segmentedButton.setStyle("-m3-container-height: 40px; -m3-container-shape: 20px; -m3-horizontal-padding: 12px;");
        segmentedButton.setContainerHeight(48.0);
        segmentedButton.setContainerShape(8.0);
        segmentedButton.setHorizontalPadding(18.0);

        M3Tab tab = new M3Tab("Tab");
        tab.setStyle(
                "-m3-container-height: 48px; "
                        + "-m3-tab-min-width: 72px; "
                        + "-m3-horizontal-padding: 12px; "
                        + "-m3-active-indicator-height: 3px; "
                        + "-m3-active-indicator-shape: 2px;"
        );
        tab.setContainerHeight(56.0);
        tab.setTabMinWidth(92.0);
        tab.setHorizontalPadding(18.0);
        tab.setActiveIndicatorHeight(5.0);
        tab.setActiveIndicatorShape(4.0);

        M3NavigationItem navigationItem = new M3NavigationItem("Inbox");
        navigationItem.setStyle(
                "-m3-container-height: 80px; "
                        + "-m3-item-width: 80px; "
                        + "-m3-indicator-width: 64px; "
                        + "-m3-indicator-height: 32px; "
                        + "-m3-indicator-shape: 16px; "
                        + "-m3-content-spacing: 4px;"
        );
        navigationItem.setContainerHeight(88.0);
        navigationItem.setItemWidth(96.0);
        navigationItem.setIndicatorWidth(72.0);
        navigationItem.setIndicatorHeight(36.0);
        navigationItem.setIndicatorShape(18.0);
        navigationItem.setContentSpacing(8.0);

        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        progressIndicator.setStyle(
                "-m3-track-thickness: 4px; "
                        + "-m3-indicator-size: 48px; "
                        + "-m3-wave-amplitude: 2px; "
                        + "-m3-wavelength: 20px; "
                        + "-m3-track-gap: 4px;"
        );
        progressIndicator.setTrackThickness(8.0);
        progressIndicator.setIndicatorSize(64.0);
        progressIndicator.setWaveAmplitude(6.0);
        progressIndicator.setWavelength(28.0);
        progressIndicator.setTrackGap(7.0);

        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        loadingIndicator.setStyle("-m3-container-size: 48px; -m3-indicator-size: 38px;");
        loadingIndicator.setContainerSize(64.0);
        loadingIndicator.setIndicatorSize(44.0);

        M3Badge badge = new M3Badge("7");
        badge.setStyle(
                "-m3-small-size: 6px; "
                        + "-m3-large-height: 16px; "
                        + "-m3-large-min-width: 16px; "
                        + "-m3-container-shape: 8px; "
                        + "-m3-horizontal-padding: 4px;"
        );
        badge.setSmallSize(8.0);
        badge.setLargeHeight(22.0);
        badge.setLargeMinWidth(24.0);
        badge.setContainerShape(11.0);
        badge.setHorizontalPadding(6.0);

        M3Card card = new M3Card();
        card.setStyle("-m3-container-shape: 12px; -m3-content-padding: 16px; -m3-outline-width: 1px;");
        card.setContainerShape(20.0);
        card.setContentPadding(24.0);
        card.setOutlineWidth(2.0);

        M3Divider divider = new M3Divider();
        divider.setStyle("-m3-thickness: 1px; -m3-inset-start: 8px; -m3-inset-end: 8px;");
        divider.setThickness(2.0);
        divider.setInsetStart(16.0);
        divider.setInsetEnd(20.0);

        Pane root = new Pane(
                fab,
                chip,
                toggleButton,
                segmentedButton,
                tab,
                navigationItem,
                progressIndicator,
                loadingIndicator,
                badge,
                card,
                divider
        );
        Scene scene = new Scene(root, 680.0, 420.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        root.applyCss();

        assertEquals(72.0, fab.getContainerSize(), 0.0001);
        assertEquals(24.0, fab.getContainerShape(), 0.0001);
        assertEquals(28.0, fab.getHorizontalPadding(), 0.0001);

        assertEquals(40.0, chip.getContainerHeight(), 0.0001);
        assertEquals(12.0, chip.getContainerShape(), 0.0001);
        assertEquals(14.0, chip.getHorizontalPadding(), 0.0001);

        assertEquals(48.0, toggleButton.getContainerHeight(), 0.0001);
        assertEquals(10.0, toggleButton.getContainerShape(), 0.0001);

        assertEquals(48.0, segmentedButton.getContainerHeight(), 0.0001);
        assertEquals(8.0, segmentedButton.getContainerShape(), 0.0001);
        assertEquals(18.0, segmentedButton.getHorizontalPadding(), 0.0001);

        assertEquals(56.0, tab.getContainerHeight(), 0.0001);
        assertEquals(92.0, tab.getTabMinWidth(), 0.0001);
        assertEquals(18.0, tab.getHorizontalPadding(), 0.0001);
        assertEquals(5.0, tab.getActiveIndicatorHeight(), 0.0001);
        assertEquals(4.0, tab.getActiveIndicatorShape(), 0.0001);

        assertEquals(88.0, navigationItem.getContainerHeight(), 0.0001);
        assertEquals(96.0, navigationItem.getItemWidth(), 0.0001);
        assertEquals(72.0, navigationItem.getIndicatorWidth(), 0.0001);
        assertEquals(36.0, navigationItem.getIndicatorHeight(), 0.0001);
        assertEquals(18.0, navigationItem.getIndicatorShape(), 0.0001);
        assertEquals(8.0, navigationItem.getContentSpacing(), 0.0001);

        assertEquals(8.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(64.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(6.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(28.0, progressIndicator.getWavelength(), 0.0001);
        assertEquals(7.0, progressIndicator.getTrackGap(), 0.0001);

        assertEquals(64.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(44.0, loadingIndicator.getIndicatorSize(), 0.0001);

        assertEquals(8.0, badge.getSmallSize(), 0.0001);
        assertEquals(22.0, badge.getLargeHeight(), 0.0001);
        assertEquals(24.0, badge.getLargeMinWidth(), 0.0001);
        assertEquals(11.0, badge.getContainerShape(), 0.0001);
        assertEquals(6.0, badge.getHorizontalPadding(), 0.0001);

        assertEquals(20.0, card.getContainerShape(), 0.0001);
        assertEquals(24.0, card.getContentPadding(), 0.0001);
        assertEquals(2.0, card.getOutlineWidth(), 0.0001);

        assertEquals(2.0, divider.getThickness(), 0.0001);
        assertEquals(16.0, divider.getInsetStart(), 0.0001);
        assertEquals(20.0, divider.getInsetEnd(), 0.0001);
    }

    /// Verifies that generated state layer rules apply beyond button-like controls.
    @Test
    void generatedStateLayerRulesApplyToInteractiveControls() {
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        M3Tab tab = new M3Tab("Overview");
        M3NavigationItem navigationItem = new M3NavigationItem("Home");
        M3IconToggleButton iconToggleButton = new M3IconToggleButton("T");
        M3ListItem listItem = new M3ListItem("Headline");
        M3SearchBar searchBar = new M3SearchBar("Search");
        M3Card card = new M3Card();
        card.setOnAction(event -> {
        });
        Pane root = new Pane(
                checkBox,
                slider,
                tab,
                navigationItem,
                iconToggleButton,
                listItem,
                searchBar,
                card
        );
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(root, false);
        checkBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        slider.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        navigationItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        iconToggleButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        listItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        searchBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        card.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();

        assertEquals(1.0, checkBox.getOpacity(), 0.0001);
        assertEquals(0.08, lookupRegion(checkBox, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, slider.getOpacity(), 0.0001);
        assertEquals(0.0, lookupRegion(slider, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, tab.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(tab, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, navigationItem.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(navigationItem, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, iconToggleButton.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(iconToggleButton, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, listItem.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(listItem, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, searchBar.getOpacity(), 0.0001);
        assertEquals(0.1, lookupRegion(searchBar, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(1.0, card.getOpacity(), 0.0001);
        assertEquals(0.08, lookupRegion(card, ".m3-state-layer").getOpacity(), 0.0001);
    }

    /// Verifies that the base stylesheet provides visible default state layer feedback.
    @Test
    void baseStylesheetProvidesDefaultStateLayerOpacity() {
        M3Button button = new M3Button("Button");
        M3Tab tab = new M3Tab("Tab");
        M3NavigationItem navigationItem = new M3NavigationItem("Home");
        M3IconToggleButton iconToggleButton = new M3IconToggleButton("T");
        Pane root = new Pane(button, tab, navigationItem, iconToggleButton);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(root, false);
        M3ThemeRuntime.uninstallThemeStylesheet(scene);
        root.applyCss();
        button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        tab.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        navigationItem.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        iconToggleButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        root.applyCss();

        assertEquals(0.08, lookupRegion(button, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(0.10, lookupRegion(tab, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(0.10, lookupRegion(navigationItem, ".m3-state-layer").getOpacity(), 0.0001);
        assertEquals(0.08, lookupRegion(iconToggleButton, ".m3-state-layer").getOpacity(), 0.0001);
    }

    /// Verifies that controls expose their default styles through user-agent stylesheets.
    @Test
    void controlsExposeUserAgentStylesheets() {
        assertUserAgentStylesheet(new M3Button(), "/styles/controls/button.css");
        assertUserAgentStylesheet(new M3ButtonGroup(), "/styles/controls/button-group.css");
        assertUserAgentStylesheet(new M3SplitButton(), "/styles/controls/split-button.css");
        assertUserAgentStylesheet(new M3FabMenu(), "/styles/controls/fab-menu.css");
        assertUserAgentStylesheet(new M3Toolbar(), "/styles/controls/toolbar.css");
        assertUserAgentStylesheet(new M3IconButton(), "/styles/controls/button.css");
        assertUserAgentStylesheet(new M3MenuButton(), "/styles/controls/button.css");
        assertUserAgentStylesheet(new M3IconToggleButton(), "/styles/controls/icon-toggle-button.css");
        assertUserAgentStylesheet(new M3IconToggleButtonGroup(), "/styles/controls/icon-toggle-button.css");
        assertUserAgentStylesheet(new M3FloatingActionButton(), "/styles/controls/floating-action-button.css");
        assertUserAgentStylesheet(new M3TextField(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3PasswordField(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3TextArea(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3TextInputLayout(), "/styles/controls/text-field.css");
        assertUserAgentStylesheet(new M3DatePicker(), "/styles/controls/date-picker.css");
        assertUserAgentStylesheet(new M3DatePickerField(), "/styles/controls/picker-field.css");
        assertUserAgentStylesheet(new M3DateRangePicker(), "/styles/controls/date-picker.css");
        assertUserAgentStylesheet(new M3DateRangePickerField(), "/styles/controls/picker-field.css");
        assertUserAgentStylesheet(new M3TimePicker(), "/styles/controls/time-picker.css");
        assertUserAgentStylesheet(new M3TimePickerField(), "/styles/controls/picker-field.css");
        assertUserAgentStylesheet(new M3Avatar(), "/styles/controls/avatar.css");
        assertUserAgentStylesheet(new M3DisclosureIcon(), "/styles/controls/disclosure-icon.css");
        assertUserAgentStylesheet(new M3Icon(), "/styles/controls/icon.css");
        assertUserAgentStylesheet(new M3SVGIcon(), "/styles/controls/svg-icon.css");
        assertUserAgentStylesheet(new M3Text(), "/styles/controls/text.css");
        assertUserAgentStylesheet(new M3Surface(), "/styles/controls/surface.css");
        assertUserAgentStylesheet(new M3FormPane(), "/styles/controls/form.css");
        assertUserAgentStylesheet(formSection(), "/styles/controls/form.css");
        assertUserAgentStylesheet(new M3FormRow(), "/styles/controls/form.css");
        assertUserAgentStylesheet(new M3ValidationSummary(), "/styles/controls/validation-summary.css");
        assertUserAgentStylesheet(new M3BadgedBox(), "/styles/controls/badge.css");
        assertUserAgentStylesheet(new M3Menu(), "/styles/controls/menu.css");
        assertUserAgentStylesheet(new M3MenuItem(), "/styles/controls/menu-item.css");
        assertUserAgentStylesheet(new M3SubMenuItem(), "/styles/controls/menu-item.css");
        assertUserAgentStylesheet(new M3MenuSectionHeader(), "/styles/controls/menu.css");
        assertUserAgentStylesheet(new M3SearchBar(), "/styles/controls/search.css");
        assertUserAgentStylesheet(searchView(), "/styles/controls/search.css");
        assertUserAgentStylesheet(sideSheet(), "/styles/controls/sheet.css");
        assertUserAgentStylesheet(bottomSheet(), "/styles/controls/sheet.css");
        assertUserAgentStylesheet(new M3Scrim(), "/styles/controls/scrim.css");
        assertUserAgentStylesheet(new M3CheckBox(), "/styles/controls/selection.css");
        assertUserAgentStylesheet(new M3RadioButton(), "/styles/controls/selection.css");
        assertUserAgentStylesheet(new M3Switch(), "/styles/controls/selection.css");
        assertUserAgentStylesheet(new M3Slider(), "/styles/controls/slider.css");
        assertUserAgentStylesheet(new M3AssistChip(), "/styles/controls/chip.css");
        assertUserAgentStylesheet(new M3ChipGroup(), "/styles/controls/chip.css");
        assertUserAgentStylesheet(new M3SegmentedButton(), "/styles/controls/segmented-button.css");
        assertUserAgentStylesheet(new M3SegmentedButtonGroup(), "/styles/controls/segmented-button.css");
        assertUserAgentStylesheet(new M3Tab(), "/styles/controls/tab.css");
        assertUserAgentStylesheet(new M3TabBar(), "/styles/controls/tab.css");
        assertUserAgentStylesheet(new M3ProgressBar(), "/styles/controls/progress.css");
        assertUserAgentStylesheet(new M3ProgressIndicator(), "/styles/controls/progress.css");
        assertUserAgentStylesheet(new M3LoadingIndicator(), "/styles/controls/loading-indicator.css");
        assertUserAgentStylesheet(new M3Divider(), "/styles/controls/divider.css");
        assertUserAgentStylesheet(new M3Badge(), "/styles/controls/badge.css");
        assertUserAgentStylesheet(topAppBar(), "/styles/controls/top-app-bar.css");
        assertUserAgentStylesheet(bottomAppBar(), "/styles/controls/bottom-app-bar.css");
        assertUserAgentStylesheet(new M3NavigationBar(), "/styles/controls/navigation-bar.css");
        assertUserAgentStylesheet(new M3NavigationRail(), "/styles/controls/navigation-rail.css");
        assertUserAgentStylesheet(new M3NavigationDrawer(), "/styles/controls/navigation-drawer.css");
        assertUserAgentStylesheet(new M3NavigationDrawerGroup(), "/styles/controls/navigation-drawer-group.css");
        assertUserAgentStylesheet(new M3NavigationItem(), "/styles/controls/navigation-bar.css");
        assertUserAgentStylesheet(new M3ListPane(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3ListView<>(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3ListItem(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3ListSectionHeader(), "/styles/controls/list-item.css");
        assertUserAgentStylesheet(new M3Card(), "/styles/controls/card.css");
        assertUserAgentStylesheet(new M3DialogPane(), "/styles/controls/dialog.css");
        assertUserAgentStylesheet(new M3Carousel(), "/styles/controls/carousel.css");
        assertUserAgentStylesheet(banner(), "/styles/controls/banner.css");
    }

    /// Verifies that a node user-agent stylesheet has the expected bundled suffix.
    private static void assertUserAgentStylesheet(Object node, String suffix) {
        try {
            Object stylesheet = node.getClass().getMethod("getUserAgentStylesheet").invoke(node);
            assertTrue(stylesheet instanceof String);
            assertTrue(((String) stylesheet).endsWith(suffix));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /// Returns whether the standalone control scene has reached a renderable fallback-styled state.
    private static boolean standaloneControlSceneReady(AtomicReference<@Nullable Stage> stageReference) {
        @Nullable Stage stage = stageReference.get();
        if (stage == null || !stage.isShowing() || stage.getScene() == null) {
            return false;
        }

        Parent root = stage.getScene().getRoot();
        root.applyCss();
        root.layout();
        return hasRenderableBounds(root);
    }

    /// Returns whether standalone tooltip popups have reached a renderable fallback-styled state.
    private static boolean standaloneTooltipPopupsReady(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable M3Tooltip> plainTooltipReference,
            AtomicReference<@Nullable M3RichTooltip> richTooltipReference
    ) {
        @Nullable Stage stage = stageReference.get();
        @Nullable M3Tooltip plainTooltip = plainTooltipReference.get();
        @Nullable M3RichTooltip richTooltip = richTooltipReference.get();
        return stage != null
                && stage.isShowing()
                && stage.getScene() != null
                && tooltipPopupReady(plainTooltip, "m3-tooltip")
                && tooltipPopupReady(richTooltip, "m3-rich-tooltip-container");
    }

    /// Returns whether a tooltip popup has visible styled content.
    private static boolean tooltipPopupReady(@Nullable M3Tooltip tooltip, String contentStyleClass) {
        if (tooltip == null || !tooltip.isShowing() || tooltip.getScene() == null || tooltip.getSkin() == null) {
            return false;
        }

        Node skinNode = tooltip.getSkin().getNode();
        skinNode.applyCss();
        if (skinNode instanceof Parent parent) {
            parent.layout();
            Node content = parent.lookup("." + contentStyleClass);
            return content != null && hasRenderableBounds(content);
        }
        return hasRenderableBounds(skinNode);
    }

    /// Asserts that a standalone tooltip popup has visible styled content.
    private static void assertStandaloneTooltipPopupReady(@Nullable M3Tooltip tooltip, String contentStyleClass) {
        assertTrue(tooltipPopupReady(tooltip, contentStyleClass), () -> "Tooltip popup did not render " + contentStyleClass);
    }

    /// Closes standalone tooltip popup test content.
    private static void closeStandaloneTooltipScene(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable M3Tooltip> plainTooltipReference,
            AtomicReference<@Nullable M3RichTooltip> richTooltipReference
    ) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            @Nullable M3Tooltip plainTooltip = plainTooltipReference.get();
            if (plainTooltip != null) {
                plainTooltip.hide();
            }
            @Nullable M3RichTooltip richTooltip = richTooltipReference.get();
            if (richTooltip != null) {
                richTooltip.hide();
            }
            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Returns whether a picker control has completed CSS and skin creation after a popup presentation.
    private static boolean pickerControlReady(Control picker) {
        if (picker.getScene() == null) {
            return false;
        }
        picker.applyCss();
        picker.layout();
        return picker.getSkin() != null;
    }

    /// Returns whether a standalone internal icon has resolved a renderable fallback token fill.
    private static boolean standaloneInternalIconReady(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable M3InternalIcon> iconReference
    ) {
        @Nullable Stage stage = stageReference.get();
        @Nullable M3InternalIcon icon = iconReference.get();
        if (stage == null || icon == null || !stage.isShowing() || stage.getScene() == null) {
            return false;
        }

        Parent root = stage.getScene().getRoot();
        root.applyCss();
        root.layout();
        return hasRenderableBounds(icon) && icon.getPath().getFill() != null;
    }

    /// Verifies one generic picker field popup reaches a renderable fallback-styled state in isolation.
    private static <T, P extends Control & M3Localized> void assertStandalonePickerPopupReady(
            M3PickerField<T, P> field
    ) throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<String> stateReference = new AtomicReference<>("owner stage not created");
        AtomicBoolean showRequested = new AtomicBoolean();
        P picker = field.getPicker();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable Stage stage = stageReference.get();
                        @Nullable Bounds screenBounds = field.localToScreen(field.getBoundsInLocal());
                        stateReference.set("stageShowing=" + (stage != null && stage.isShowing())
                                + ", screenBounds=" + screenBounds
                                + ", showRequested=" + showRequested.get()
                                + ", fieldShowing=" + field.isShowing()
                                + ", pickerScene=" + picker.getScene()
                                + ", pickerSkin=" + picker.getSkin());
                        if (stage == null || !stage.isShowing() || screenBounds == null) {
                            return false;
                        }
                        if (showRequested.compareAndSet(false, true)) {
                            field.showPicker();
                            return false;
                        }
                        return field.isShowing() && pickerControlReady(picker);
                    },
                    STANDALONE_FALLBACK_STYLE_STABLE_PULSES,
                    stateReference::get,
                    () -> {
                        Stage stage = new Stage();
                        stageReference.set(stage);
                        showStandalonePickerOwner(stage, field);
                    },
                    () -> assertTrue(field.isShowing())
            );
        } finally {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                try {
                    field.hidePicker();
                } finally {
                    @Nullable Stage stage = stageReference.get();
                    if (stage != null) {
                        stage.close();
                    }
                }
            });
        }
    }

    /// Verifies one date-range picker popup reaches a renderable fallback-styled state in isolation.
    private static void assertStandalonePickerPopupReady(
            M3DateRangePickerField field
    ) throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<String> stateReference = new AtomicReference<>("owner stage not created");
        AtomicBoolean showRequested = new AtomicBoolean();
        M3DateRangePicker picker = field.getPicker();
        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        @Nullable Stage stage = stageReference.get();
                        @Nullable Bounds screenBounds = field.localToScreen(field.getBoundsInLocal());
                        stateReference.set("stageShowing=" + (stage != null && stage.isShowing())
                                + ", screenBounds=" + screenBounds
                                + ", showRequested=" + showRequested.get()
                                + ", fieldShowing=" + field.isShowing()
                                + ", pickerScene=" + picker.getScene()
                                + ", pickerSkin=" + picker.getSkin());
                        if (stage == null || !stage.isShowing() || screenBounds == null) {
                            return false;
                        }
                        if (showRequested.compareAndSet(false, true)) {
                            field.showPicker();
                            return false;
                        }
                        return field.isShowing() && pickerControlReady(picker);
                    },
                    STANDALONE_FALLBACK_STYLE_STABLE_PULSES,
                    stateReference::get,
                    () -> {
                        Stage stage = new Stage();
                        stageReference.set(stage);
                        showStandalonePickerOwner(stage, field);
                    },
                    () -> assertTrue(field.isShowing())
            );
        } finally {
            FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
                try {
                    field.hidePicker();
                } finally {
                    @Nullable Stage stage = stageReference.get();
                    if (stage != null) {
                        stage.close();
                    }
                }
            });
        }
    }

    /// Shows a standalone owner window for one picker field.
    private static void showStandalonePickerOwner(Stage stage, Control field) {
        VBox root = new VBox(field);
        stage.setScene(new Scene(root, 560.0, 360.0));
        stage.show();
        root.applyCss();
        root.layout();
    }

    /// Verifies one Region control against generated theme CSS layout metrics.
    private static void assertGeneratedThemeCssPreservesBoundRegionMetrics(Region region) {
        Pane root = new Pane(region);
        Scene scene = new Scene(root);

        assertBoundRegionMetricsPreserved(region, () -> {
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
        }, () -> assertSame(scene, region.getScene()));
    }
}
