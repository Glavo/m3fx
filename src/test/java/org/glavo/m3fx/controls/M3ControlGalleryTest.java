// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Checks rendered control galleries across themes and profiles.
@NotNullByDefault
@Tier3Test
final class M3ControlGalleryTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that a representative control set renders non-blank visible output.
    @Test
    void visualSmokeSnapshotRendersCoreControlsWithContrast() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3Button tonalButton = createButton("Tonal", M3ButtonVariant.TONAL);
            M3Button outlinedButton = createButton("Outlined", M3ButtonVariant.OUTLINED);
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = createSegmentedButton("Week", true);
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segments = segmentedButtonGroup(day, week, month);
            M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
            slider.setPrefWidth(220.0);
            M3ProgressBar progressBar = new M3ProgressBar(0.62);
            progressBar.setPrefWidth(220.0);
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.72);
            M3CheckBox checkBox = createCheckBox("Check", true);
            M3RadioButton radioButton = createRadioButton("Radio", true);
            M3Switch switchControl = createSwitch("Switch", true);

            FlowPane buttons = new FlowPane(14.0, 14.0, filledButton, tonalButton, outlinedButton, segments);
            FlowPane controls = new FlowPane(18.0, 18.0, slider, progressBar, progressIndicator);
            FlowPane selections = new FlowPane(18.0, 18.0, checkBox, radioButton, switchControl);
            VBox root = new VBox(18.0, buttons, controls, selections);
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 620.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(620.0, 260.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 10);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-smoke.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies that containment, feedback, and navigation controls render visible surfaces.
    @Test
    void containmentFeedbackAndNavigationSnapshotRendersVisibleSurfaces() {
        FxTestUtils.runOnFxThread(() -> {
            M3Avatar avatar = createAvatar("AB", M3AvatarVariant.TERTIARY);
            M3BadgedBox badgedBox = new M3BadgedBox(new M3Avatar("M"), new M3Badge("7"));
            M3ListItem listItem = new M3ListItem("Inbox");
            listItem.setSupportingText("Latest updates");
            listItem.setSelected(true);
            M3ListSectionHeader listHeader = new M3ListSectionHeader("Recent");
            M3ListPane list = listPane(listHeader, listItem, new M3Divider(), new M3ListItem("Archive"));
            list.setPrefWidth(280.0);
            M3Card card = new M3Card(new Label("Elevated card"));
            card.setVariant(M3CardVariant.ELEVATED);
            M3NavigationItem home = createNavigationItem("Home", visualIcon("home"), true);
            M3NavigationItem search = new M3NavigationItem("Search", visualIcon("search"));
            M3NavigationBar navigationBar = navigationBar(home, search);

            FlowPane topRow = new FlowPane(18.0, 18.0, avatar, badgedBox, list, card);
            VBox root = new VBox(18.0, topRow, navigationBar);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 640.0, 420.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(640.0, 420.0);
            root.layout();

            assertVisualIconSlotsUseVectorGraphics(root);

            assertBadgedBoxBadgeAnchoredToLogicalEnd(badgedBox, false);
            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, avatar, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, badgedBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, list, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, listItemContainer(listItem), Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, lookupRegion(card, ".m3-card-container"), Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, lookupRegion(home, ".m3-navigation-item-indicator"),
                    Color.WHITE,
                    0.08);
            assertSnapshotHasColorVariety(image, 14);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-containment-feedback-navigation.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies that every implemented node-backed control family renders in a full visual gallery.
    @Test
    void allImplementedControlFamiliesRenderVisualGallery() {
        FxTestUtils.runOnFxThread(() -> {
            M3Text titleText = new M3Text("M3FX", M3TextRole.DISPLAY_SMALL);
            M3Icon primaryIcon = new M3Icon("A", M3IconSize.LARGE, M3IconVariant.PRIMARY);
            M3DisclosureIcon disclosureIcon = new M3DisclosureIcon(true);
            M3Avatar avatar = createAvatar("AB", M3AvatarVariant.PRIMARY);
            M3Badge badge = new M3Badge("9+");
            M3BadgedBox badgedBox = new M3BadgedBox(
                    visualIcon("mail", 32.0),
                    new M3Badge("3")
            );
            M3Divider horizontalDivider = new M3Divider();
            horizontalDivider.setPrefWidth(220.0);
            M3Divider verticalDivider = new M3Divider(Orientation.VERTICAL);
            verticalDivider.setPrefHeight(48.0);

            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3Button tonalButton = createButton("Tonal", M3ButtonVariant.TONAL);
            M3Button outlinedButton = createButton("Outlined", M3ButtonVariant.OUTLINED);
            M3Button textButton = createButton("Text", M3ButtonVariant.TEXT);
            M3Button elevatedButton = createButton("Elevated", M3ButtonVariant.ELEVATED);
            M3Button disabledButton = createButton("Disabled", M3ButtonVariant.FILLED);
            disabledButton.setDisable(true);
            M3ButtonGroup buttonGroup = buttonGroup(
                    createButton("Edit", M3ButtonVariant.TONAL),
                    createButton("Share", M3ButtonVariant.TONAL),
                    createButton("Done", M3ButtonVariant.TONAL)
            );
            M3SplitButton splitButton = createSplitButton(
                    "Create",
                    M3ButtonVariant.OUTLINED,
                    new M3MenuItem("Copy")
            );

            M3IconButton iconButton = new M3IconButton(visualIcon("info"));
            M3IconToggleButton standardToggle = createIconToggleButton(
                    visualIcon("star"),
                    M3IconToggleButtonVariant.STANDARD,
                    true
            );
            M3IconToggleButton filledToggle = createIconToggleButton(
                    visualIcon("done"),
                    M3IconToggleButtonVariant.FILLED,
                    true
            );
            M3IconToggleButton tonalToggle = createIconToggleButton(
                    visualIcon("text"),
                    M3IconToggleButtonVariant.TONAL,
                    true
            );
            M3IconToggleButton outlinedToggle = createIconToggleButton(
                    visualIcon("bookmark"),
                    M3IconToggleButtonVariant.OUTLINED,
                    true
            );
            M3IconToggleButtonGroup iconToggleGroup = iconToggleButtonGroup(
                    standardToggle,
                    filledToggle,
                    tonalToggle,
                    outlinedToggle
            );

            M3FloatingActionButton smallFab = createGraphicFab(
                    visualIcon("add"),
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.SMALL
            );
            M3FloatingActionButton regularFab = createGraphicFab(
                    visualIcon("add"),
                    M3FloatingActionButtonVariant.SECONDARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3FloatingActionButton largeFab = createFab(
                    "*",
                    null,
                    M3FloatingActionButtonVariant.TERTIARY,
                    M3FloatingActionButtonSize.LARGE
            );
            M3FloatingActionButton extendedFab = createFab(
                    "Create",
                    visualIcon("add"),
                    M3FloatingActionButtonVariant.SURFACE,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().addAll(
                    createGraphicFab(
                            visualIcon("edit"),
                            M3FloatingActionButtonVariant.PRIMARY,
                            M3FloatingActionButtonSize.SMALL
                    ),
                    createGraphicFab(
                            visualIcon("share"),
                            M3FloatingActionButtonVariant.SECONDARY,
                            M3FloatingActionButtonSize.SMALL
                    )
            );
            fabMenu.setExpanded(true);

            M3TextField filledField = new M3TextField("Filled text field");
            filledField.setPrefWidth(190.0);
            M3TextField outlinedField = createTextField("Outlined text field", M3TextInputVariant.OUTLINED);
            outlinedField.setPrefWidth(210.0);
            M3PasswordField passwordField = createPasswordField("password", M3TextInputVariant.OUTLINED);
            passwordField.setPrefWidth(170.0);
            M3TextField errorField = createTextField("Error", M3TextInputVariant.OUTLINED);
            errorField.setError(true);
            errorField.setPrefWidth(150.0);
            M3TextArea textArea = createTextArea("Multiline\ntext area", M3TextInputVariant.FILLED);
            textArea.setPrefSize(260.0, 96.0);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));

            M3CheckBox selectedCheckBox = createCheckBox("Checkbox", true);
            M3CheckBox indeterminateCheckBox = new M3CheckBox("Mixed");
            indeterminateCheckBox.setIndeterminate(true);
            M3RadioButton selectedRadioButton = createRadioButton("Radio", true);
            M3Switch selectedSwitch = createSwitch("Switch", true);
            M3Switch disabledSwitch = createSwitch("Disabled", true);
            disabledSwitch.setDisable(true);
            M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
            slider.setPrefWidth(260.0);

            M3Chip assistChip = new M3AssistChip("Assist");
            M3SelectableChip filterChip = createFilterChip("Filter", true);
            M3Chip inputChip = new M3InputChip("Input", visualIcon("close"));
            M3Chip suggestionChip = new M3SuggestionChip("Suggestion");
            M3ChipGroup chipGroup = chipGroup(assistChip, filterChip, inputChip, suggestionChip);
            M3SegmentedButtonGroup segmentedButtons = segmentedButtonGroup(
                    new M3SegmentedButton("Day"),
                    createSegmentedButton("Week", true),
                    new M3SegmentedButton("Month")
            );
            M3TabBar tabBar = tabBar(
                    createTab("Overview", true),
                    new M3Tab("Details"),
                    new M3Tab("History")
            );

            M3ProgressBar progressBar = new M3ProgressBar(0.62);
            progressBar.setPrefWidth(260.0);
            M3ProgressBar indeterminateProgressBar = new M3ProgressBar();
            indeterminateProgressBar.setPrefWidth(180.0);
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.72);
            M3ProgressIndicator indeterminateProgressIndicator = new M3ProgressIndicator();
            M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();

            M3Surface surface = surface(visualLabel("Surface"));
            surface.setVariant(M3SurfaceVariant.SECONDARY_CONTAINER);
            surface.setElevation(M3SurfaceElevation.LEVEL2);
            surface.setPrefSize(170.0, 80.0);
            M3Card filledCard = new M3Card(visualLabel("Filled card"), M3CardVariant.FILLED);
            filledCard.setPrefSize(150.0, 80.0);
            M3Card elevatedCard = new M3Card(visualLabel("Elevated card"), M3CardVariant.ELEVATED);
            elevatedCard.setPrefSize(150.0, 80.0);
            M3Card outlinedCard = new M3Card(visualLabel("Outlined card"), M3CardVariant.OUTLINED);
            outlinedCard.setPrefSize(150.0, 80.0);
            M3Carousel carousel = carousel(
                    carouselTestItem("One"),
                    carouselTestItem("Two"),
                    carouselTestItem("Three"),
                    carouselTestItem("Four")
            );
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(420.0, 92.0);
            carousel.selectIndex(1);

            M3Banner banner = createBanner(
                    "Banner message with persistent inline feedback.",
                    visualIcon("info"),
                    createButton("Action", M3ButtonVariant.TEXT)
            );
            banner.setPrefWidth(520.0);
            M3Scrim scrim = new M3Scrim();
            scrim.setPrefSize(180.0, 72.0);
            scrim.show();
            M3DialogPane dialogPane = new M3DialogPane();
            dialogPane.setHeaderText("Dialog title");
            dialogPane.setContentText("Dialog content");
            M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
            okAction.setDefaultButton(true);
            M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
            cancelAction.setCancelButton(true);
            dialogPane.getActions().setAll(okAction, cancelAction);
            dialogPane.setPrefWidth(320.0);

            M3ListItem selectedListItem = new M3ListItem("Selected list item");
            selectedListItem.setLeading(visualIcon("inbox"));
            selectedListItem.setTrailing(new M3Badge("2"));
            selectedListItem.setSupportingText("Supporting text");
            selectedListItem.setTrailingSupportingText("Now");
            selectedListItem.setSelected(true);
            M3ListPane list = listPane(
                    new M3ListSectionHeader("Inbox"),
                    selectedListItem,
                    new M3Divider(),
                    new M3ListSectionHeader("Labels"),
                    new M3ListItem("List item")
            );
            list.setPrefWidth(300.0);
            M3MenuItem selectedMenuItem = new M3MenuItem("Selected menu item", visualIcon("done"));
            selectedMenuItem.setSelected(true);
            M3Menu menu = new M3Menu(
                    new M3MenuSectionHeader("Actions"),
                    selectedMenuItem,
                    new M3MenuItem("Menu item")
            );
            menu.setPrefWidth(280.0);
            M3MenuButton menuButton = new M3MenuButton(
                    "Menu",
                    new M3MenuItem("First"),
                    new M3MenuItem("Second")
            );
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setPrefWidth(280.0);
            M3SearchView searchView = searchView(
                    "Search view",
                    new M3ListItem("First result"),
                    new M3ListItem("Second result")
            );
            searchView.setPrefWidth(340.0);

            M3TopAppBar topAppBar = topAppBar(
                    "Top app bar",
                    M3TopAppBarVariant.CENTER_ALIGNED,
                    new M3IconButton(visualIcon("menu")),
                    new M3IconButton(visualIcon("search")),
                    new M3IconButton(visualIcon("more"))
            );
            topAppBar.setPrefWidth(520.0);
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    createGraphicFab(
                            visualIcon("add"),
                            M3FloatingActionButtonVariant.PRIMARY,
                            M3FloatingActionButtonSize.SMALL
                    ),
                    new M3IconButton(visualIcon("home")),
                    new M3IconButton(visualIcon("search"))
            );
            bottomAppBar.setPrefWidth(520.0);

            M3NavigationBar navigationBar = navigationBar(
                    createNavigationItem("Home", visualIcon("home"), true),
                    new M3NavigationItem("Search", visualIcon("search"), new M3Badge("1")),
                    new M3NavigationItem("Profile", visualIcon("person"))
            );
            navigationBar.setPrefWidth(420.0);
            M3NavigationRail navigationRail = navigationRail(
                    createNavigationItem("Home", visualIcon("home"), true),
                    new M3NavigationItem("Search", visualIcon("search")),
                    new M3NavigationItem("Profile", visualIcon("person"))
            );
            M3NavigationDrawer navigationDrawer = navigationDrawer(
                    drawerItem("Inbox", true),
                    drawerItem("Sent", false),
                    drawerItem("Archive", false)
            );
            navigationDrawer.setPrefWidth(280.0);

            M3SideSheet sideSheet = sideSheet(
                    "Side sheet",
                    visualLabel("Side sheet content"),
                    createButton("Action", M3ButtonVariant.TEXT)
            );
            sideSheet.setVariant(M3SheetVariant.MODAL);
            sideSheet.setPrefSize(280.0, 180.0);
            sideSheet.show();
            M3BottomSheet bottomSheet = bottomSheet(
                    "Bottom sheet",
                    visualLabel("Bottom sheet content"),
                    createButton("Done", M3ButtonVariant.TEXT)
            );
            bottomSheet.setPrefSize(360.0, 180.0);
            bottomSheet.show();

            VBox root = new VBox(
                    20.0,
                    visualSection(
                            "Text, Icons, Badges",
                            titleText,
                            primaryIcon,
                            disclosureIcon,
                            avatar,
                            badge,
                            badgedBox,
                            horizontalDivider,
                            verticalDivider
                    ),
                    visualSection(
                            "Buttons",
                            filledButton,
                            tonalButton,
                            outlinedButton,
                            textButton,
                            elevatedButton,
                            disabledButton,
                            buttonGroup,
                            splitButton,
                            iconButton,
                            iconToggleGroup,
                            smallFab,
                            regularFab,
                            largeFab,
                            extendedFab,
                            fabMenu
                    ),
                    visualSection(
                            "Inputs",
                            filledField,
                            outlinedField,
                            passwordField,
                            errorField,
                            textArea,
                            datePicker,
                            dateRangePicker,
                            timePicker
                    ),
                    visualSection(
                            "Selection",
                            selectedCheckBox,
                            indeterminateCheckBox,
                            selectedRadioButton,
                            selectedSwitch,
                            disabledSwitch,
                            slider
                    ),
                    visualSection("Chips, Segments, Tabs", chipGroup, segmentedButtons, tabBar),
                    visualSection(
                            "Progress",
                            progressBar,
                            indeterminateProgressBar,
                            progressIndicator,
                            indeterminateProgressIndicator,
                            loadingIndicator
                    ),
                    visualSection("Surfaces", surface, filledCard, elevatedCard, outlinedCard, carousel),
                    visualSection("Feedback", banner, scrim, dialogPane),
                    visualSection("Lists, Menus, Search", list, menu, menuButton, searchBar, searchView),
                    visualSection("App Bars", topAppBar, bottomAppBar),
                    visualSection("Navigation", navigationBar, navigationRail, navigationDrawer),
                    visualSection("Sheets", sideSheet, bottomSheet)
            );
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 1120.0, 1700.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(1120.0, Math.ceil(root.prefHeight(1120.0)));
            root.layout();

            assertVisualIconSlotsUseVectorGraphics(root);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 28);
            assertSnapshotNodeContainsContrast(image, titleText, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, disclosureIcon, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, avatar, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, badge, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, filledButton, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, outlinedButton, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, buttonGroup, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, splitButton, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, iconToggleGroup, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, fabMenu, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, filledField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, outlinedField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, errorField, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, dateRangePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, timePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, selectedCheckBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, indeterminateCheckBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, selectedRadioButton, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, selectedSwitch, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, disabledSwitch, Color.WHITE, 0.03);
            assertSnapshotNodeContainsContrast(image, slider, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, chipGroup, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, segmentedButtons, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, tabBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, progressBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateProgressBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, progressIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateProgressIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, loadingIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, surface, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, carousel, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(
                    image,
                    lookupRegion(elevatedCard, ".m3-card-container"),
                    Color.WHITE,
                    0.04
            );
            assertSnapshotNodeContainsContrast(image, banner, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, scrim, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, list, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, menu, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, menuButton, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, searchBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, searchView, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, topAppBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, bottomAppBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationRail, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationDrawer, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, sideSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, bottomSheet, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-all-controls.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies that the expressive profile affects real rendered controls, not only generated token text.
    @Test
    void expressiveProfileVisualSnapshotRendersProfileControls() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3IconButton iconButton = new M3IconButton(visualIcon("search"));
            M3IconToggleButton standardToggle = createIconToggleButton(
                    visualIcon("bold"),
                    M3IconToggleButtonVariant.STANDARD,
                    false
            );
            M3IconToggleButton tonalToggle = createIconToggleButton(
                    visualIcon("italic"),
                    M3IconToggleButtonVariant.TONAL,
                    true
            );
            M3IconToggleButton outlinedToggle = createIconToggleButton(
                    visualIcon("underline"),
                    M3IconToggleButtonVariant.OUTLINED,
                    false
            );
            M3IconToggleButtonGroup toggleGroup =
                    iconToggleButtonGroup(standardToggle, tonalToggle, outlinedToggle);
            M3FloatingActionButton regularFab = createGraphicFab(
                    visualIcon("add"),
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = createSegmentedButton("Week", true);
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segments = segmentedButtonGroup(day, week, month);
            M3Tab selectedTab = createTab("Overview", true);
            M3TabBar tabBar = tabBar(selectedTab, new M3Tab("Details"));
            M3TextField textField = createTextField("Outlined text", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(260.0);
            M3TextArea textArea = createTextArea("Multiline\ntext", M3TextInputVariant.FILLED);
            textArea.setPrefSize(300.0, 128.0);
            M3SelectableChip filterChip = createFilterChip("Filter", true);
            M3CheckBox checkBox = new M3CheckBox("Check");
            checkBox.setSelected(true);
            M3Slider slider = new M3Slider(0.0, 100.0, 54.0);
            slider.setPrefWidth(220.0);
            M3MenuItem menuOpen = new M3MenuItem("Open");
            M3MenuItem menuSave = new M3MenuItem("Save");
            M3Menu menu = new M3Menu(menuOpen, menuSave);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setPrefWidth(300.0);
            M3ListItem searchResult = new M3ListItem("Expressive result");
            M3SearchView searchView = searchView("Search", searchResult);
            searchView.setPrefWidth(340.0);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            M3ListItem listItem = new M3ListItem("Expressive list item");
            M3Avatar avatar = new M3Avatar("EX");
            M3Card card = new M3Card(new Label("Expressive card"), M3CardVariant.FILLED);
            card.setPrefSize(220.0, 96.0);
            M3SideSheet sideSheet = sideSheet("Details", new Label("Side sheet content"));
            sideSheet.setPrefHeight(160.0);
            M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Bottom sheet content"));
            bottomSheet.setPrefWidth(360.0);
            bottomSheet.setPrefHeight(180.0);
            M3NavigationItem navigationBarItem = createNavigationItem("Home", visualIcon("home"), true);
            M3NavigationBar navigationBar = navigationBar(
                    navigationBarItem,
                    new M3NavigationItem("Search", visualIcon("search"))
            );
            M3NavigationItem navigationRailItem = createNavigationItem("Home", visualIcon("home"), true);
            M3NavigationRail navigationRail = navigationRail(
                    navigationRailItem,
                    new M3NavigationItem("Search", visualIcon("search"))
            );
            M3ListItem drawerItem = new M3ListItem("Inbox");
            drawerItem.setLeading(visualIcon("inbox"));
            drawerItem.setSelected(true);
            M3NavigationDrawer navigationDrawer = navigationDrawer(drawerItem);
            navigationDrawer.setPrefHeight(120.0);
            M3TopAppBar topAppBar = topAppBar(
                    "Expressive app bar",
                    new M3IconButton(visualIcon("back")),
                    new M3IconButton(visualIcon("person"))
            );
            topAppBar.setPrefWidth(560.0);
            M3TopAppBar mediumTopAppBar = topAppBar(
                    "Medium expressive app bar",
                    M3TopAppBarVariant.MEDIUM,
                    new M3IconButton(visualIcon("back")),
                    new M3IconButton(visualIcon("person"))
            );
            mediumTopAppBar.setPrefWidth(560.0);
            M3Toolbar toolbar = toolbar(
                    new M3IconButton(visualIcon("bold")),
                    new M3IconButton(visualIcon("italic")),
                    new M3IconButton(visualIcon("underline"))
            );
            toolbar.setVariant(M3ToolbarVariant.FLOATING);

            VBox root = new VBox(
                    18.0,
                    visualSection(
                            "Actions",
                            filledButton,
                            iconButton,
                            toggleGroup,
                            regularFab,
                            segments,
                            tabBar
                    ),
                    visualSection(
                            "Inputs",
                            textField,
                            textArea,
                            filterChip,
                            checkBox,
                            slider,
                            menu,
                            searchBar,
                            searchView,
                            datePicker
                    ),
                    visualSection("Surfaces", card),
                    visualSection(
                            "Navigation",
                            listItem,
                            avatar,
                            navigationBar,
                            navigationRail,
                            navigationDrawer,
                            topAppBar,
                            mediumTopAppBar,
                            toolbar,
                            sideSheet,
                            bottomSheet
                    )
            );
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 960.0, 1080.0);
            M3Theme expressiveTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );

            M3ThemeManager.install(scene, expressiveTheme);
            root.applyCss();
            root.resize(960.0, Math.ceil(root.prefHeight(960.0)));
            root.layout();

            assertVisualIconSlotsUseVectorGraphics(root);

            assertTrue(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertTrue(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
            assertEquals(40.0, filledButton.getContainerHeight(), 0.0001);
            assertEquals(16.0, filledButton.getHorizontalPadding(), 0.0001);
            assertEquals(40.0, iconButton.getContainerHeight(), 0.0001);
            assertEquals(40.0, standardToggle.getContainerHeight(), 0.0001);
            assertEquals(56.0, regularFab.getContainerSize(), 0.0001);
            assertEquals(16.0, regularFab.getHorizontalPadding(), 0.0001);
            assertEquals(40.0, day.getContainerHeight(), 0.0001);
            assertEquals(12.0, day.getHorizontalPadding(), 0.0001);
            assertEquals(48.0, selectedTab.getContainerHeight(), 0.0001);
            assertEquals(16.0, selectedTab.getHorizontalPadding(), 0.0001);
            assertEquals(3.0, selectedTab.getActiveIndicatorHeight(), 0.0001);
            assertEquals(56.0, textField.getContainerHeight(), 0.0001);
            assertEquals(16.0, textField.getHorizontalPadding(), 0.0001);
            assertEquals(112.0, textArea.getContainerHeight(), 0.0001);
            assertEquals(16.0, textArea.getHorizontalPadding(), 0.0001);
            assertEquals(16.0, textArea.getVerticalPadding(), 0.0001);
            assertEquals(32.0, filterChip.getContainerHeight(), 0.0001);
            assertEquals(16.0, filterChip.getHorizontalPadding(), 0.0001);
            assertEquals(48.0, checkBox.getTouchTargetSize(), 0.0001);
            assertEquals(16.0, slider.getTrackThickness(), 0.0001);
            assertEquals(4.0, slider.getStopIndicatorSize(), 0.0001);
            assertEquals(44.0, slider.getThumbSize(), 0.0001);
            assertEquals(4.0, slider.getThumbWidth(), 0.0001);
            assertEquals(48.0, slider.getTouchTargetSize(), 0.0001);
            assertEquals(2.0, menu.getPadding().getTop(), 0.0001);
            assertEquals(44.0, menuOpen.getOneLineHeight(), 0.0001);
            assertEquals(16.0, menuOpen.getContainerShape(), 0.0001);
            assertEquals(16.0, menuSave.getContainerShape(), 0.0001);
            assertEquals(16.0, menuOpen.getHorizontalPadding(), 0.0001);
            assertEquals(12.0, menuOpen.getContentSpacing(), 0.0001);
            assertEquals(56.0, searchBar.prefHeight(-1.0), 0.0001);
            assertEquals(56.0, searchBar.getHeight(), 0.0001);
            assertEquals(16.0, searchBar.getPadding().getLeft(), 0.0001);
            assertEquals(4.0, searchView.getPadding().getBottom(), 0.0001);
            assertEquals(64.0, searchResult.getOneLineHeight(), 0.0001);
            assertEquals(0.0, searchResult.getContainerShape(), 0.0001);
            assertEquals(20.0, searchResult.getHorizontalPadding(), 0.0001);
            assertEquals(20.0, searchResult.getContentSpacing(), 0.0001);
            assertEquals(64.0, listItem.getOneLineHeight(), 0.0001);
            assertEquals(40.0, avatar.getContainerSize(), 0.0001);
            assertEquals(16.0, card.getContainerShape(), 0.0001);
            assertEquals(16.0, card.getContentPadding(), 0.0001);
            assertEquals(256.0, sideSheet.getPrefWidth(), 0.0001);
            assertEquals(400.0, sideSheet.getMaxWidth(), 0.0001);
            assertEquals(180.0, bottomSheet.getPrefHeight(), 0.0001);
            assertEquals(640.0, bottomSheet.getMaxWidth(), 0.0001);
            assertEquals(
                    24.0,
                    lookupRegion(sideSheet, "." + "m3-sheet-content").getPadding().getLeft(),
                    0.0001
            );
            assertEquals(
                    32.0,
                    lookupRegion(bottomSheet, "." + "m3-bottom-sheet-drag-handle").getPrefWidth(),
                    0.0001
            );
            assertEquals(64.0, navigationBar.getPrefHeight(), 0.0001);
            assertEquals(80.0, navigationBarItem.getItemWidth(), 0.0001);
            assertEquals(56.0, navigationBarItem.getIndicatorWidth(), 0.0001);
            assertEquals(4.0, navigationBarItem.getContentSpacing(), 0.0001);
            assertEquals(96.0, navigationRail.getCollapsedContainerWidth(), 0.0001);
            assertEquals(80.0, navigationRailItem.getItemWidth(), 0.0001);
            assertEquals(4.0, navigationRailItem.getContentSpacing(), 0.0001);
            assertEquals(360.0, navigationDrawer.getPrefWidth(), 0.0001);
            assertEquals(56.0, drawerItem.getOneLineHeight(), 0.0001);
            assertEquals(999.0, drawerItem.getContainerShape(), 0.0001);
            assertEquals(16.0, drawerItem.getHorizontalPadding(), 0.0001);
            assertEquals(12.0, drawerItem.getContentSpacing(), 0.0001);
            assertEquals(64.0, topAppBar.prefHeight(-1.0), 0.0001);
            assertEquals(112.0, mediumTopAppBar.prefHeight(-1.0), 0.0001);
            assertEquals(20.0, mediumTopAppBar.getMediumBottomPadding(), 0.0001);
            assertEquals(Insets.EMPTY, mediumTopAppBar.getPadding());
            assertEquals(64.0, toolbar.getContainerHeight(), 0.0001);
            assertEquals(64.0, toolbar.getContainerWidth(), 0.0001);
            assertEquals(48.0, toolbar.getItemSlotSize(), 0.0001);
            assertEquals(8.0, toolbar.getContentPadding(), 0.0001);
            assertEquals(4.0, toolbar.getItemSpacing(), 0.0001);
            assertEquals(8.0, toolbar.getPadding().getTop(), 0.0001);
            assertEquals(64.0, toolbar.prefHeight(-1.0), 0.0001);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 18);
            assertSnapshotNodeContainsContrast(image, filledButton, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, toggleGroup, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, menu, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, checkBox, Color.WHITE, 0.08);
            assertSnapshotNodeContainsContrast(image, slider, Color.WHITE, 0.05);
            assertSnapshotNodeContainsContrast(image, searchView, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, card, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, sideSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, bottomSheet, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationRail, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, navigationDrawer, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, topAppBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, mediumTopAppBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, toolbar, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-expressive-profile.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies that actual dark expressive theme tokens render the main control families visibly.
    @Test
    void darkExpressiveVisualSnapshotRendersTokenDrivenControls() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button filledButton = createButton("Filled", M3ButtonVariant.FILLED);
            M3Button tonalButton = createButton("Tonal", M3ButtonVariant.TONAL);
            M3Button outlinedButton = createButton("Outlined", M3ButtonVariant.OUTLINED);
            M3IconToggleButton selectedToggle = createIconToggleButton(
                    visualIcon("done"),
                    M3IconToggleButtonVariant.FILLED,
                    true
            );
            M3FloatingActionButton fab = createGraphicFab(
                    visualIcon("add"),
                    M3FloatingActionButtonVariant.PRIMARY,
                    M3FloatingActionButtonSize.REGULAR
            );
            M3TextField textField = createTextField("Dark field", M3TextInputVariant.OUTLINED);
            textField.setText("M3FX");
            textField.setPrefWidth(260.0);
            M3PasswordField passwordField = new M3PasswordField("Password");
            passwordField.setPrefWidth(260.0);
            M3CheckBox checkBox = new M3CheckBox("Checkbox");
            checkBox.setSelected(true);
            M3CheckBox indeterminateCheckBox = new M3CheckBox("Mixed");
            indeterminateCheckBox.setIndeterminate(true);
            M3RadioButton radioButton = new M3RadioButton("Radio");
            radioButton.setSelected(true);
            M3Switch controlSwitch = new M3Switch("Switch");
            controlSwitch.setSelected(true);
            M3Slider slider = new M3Slider(0.0, 100.0, 48.0);
            slider.setPrefWidth(260.0);
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = createSegmentedButton("Week", true);
            M3SegmentedButtonGroup segments = segmentedButtonGroup(day, week);
            M3SelectableChip filterChip = createFilterChip("Filter", true);
            M3ProgressBar progressBar = new M3ProgressBar(0.58);
            progressBar.setPrefWidth(280.0);
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.7);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 21));
            M3SearchBar searchBar = new M3SearchBar("Search dark");
            searchBar.setPrefWidth(320.0);
            M3NavigationItem home = createNavigationItem("Home", visualIcon("home"), true);
            M3NavigationBar navigationBar = navigationBar(
                    home,
                    new M3NavigationItem("Search", visualIcon("search"))
            );

            VBox root = new VBox(
                    18.0,
                    visualSection("Actions", filledButton, tonalButton, outlinedButton, selectedToggle, fab),
                    visualSection("Inputs", textField, passwordField, searchBar),
                    visualSection(
                            "Selection",
                            checkBox,
                            indeterminateCheckBox,
                            radioButton,
                            controlSwitch,
                            slider,
                            segments,
                            filterChip
                    ),
                    visualSection("Feedback", progressBar, progressIndicator),
                    visualSection("Navigation", datePicker, navigationBar)
            );
            root.setStyle("-fx-background-color: -m3-color-surface; -fx-padding: 24px;");
            Scene scene = new Scene(root, 980.0, 880.0);
            M3Theme theme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );

            M3ThemeManager.install(scene, theme);
            root.applyCss();
            root.resize(980.0, Math.ceil(root.prefHeight(980.0)));
            root.layout();

            assertVisualIconSlotsUseVectorGraphics(root);

            assertTrue(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertTrue(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
            assertEquals(40.0, filledButton.getContainerHeight(), 0.0001);
            assertEquals(56.0, textField.getContainerHeight(), 0.0001);
            assertEquals(56.0, searchBar.getHeight(), 0.0001);
            assertEquals(40.0, selectedToggle.getContainerHeight(), 0.0001);
            assertEquals(56.0, fab.getContainerSize(), 0.0001);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 18);
            assertSnapshotNodeContainsContrast(image, filledButton, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, textField, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, indeterminateCheckBox, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, slider, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, progressBar, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.BLACK, 0.08);
            assertSnapshotNodeContainsContrast(image, navigationBar, Color.BLACK, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-dark-expressive-controls.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Creates a section container used by visual snapshot tests.
    private static VBox visualSection(String title, Node... nodes) {
        M3Text heading = new M3Text(title, M3TextRole.TITLE_MEDIUM);
        FlowPane row = new FlowPane(16.0, 16.0, nodes);
        row.setPrefWrapLength(1010.0);
        row.setStyle("-fx-background-color: -m3-color-surface-container-low; "
                + "-fx-background-radius: 18px; "
                + "-fx-padding: 16px;");
        return new VBox(8.0, heading, row);
    }
}
