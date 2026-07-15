// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3ListViewCell;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies accessibility roles and keyboard traversal defaults shared by public M3FX controls.
@NotNullByDefault
final class M3ControlAccessibilityTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that custom controls expose stable accessibility roles.
    @Test
    void controlsExposeAccessibilityRoles() {
        M3Badge badge = new M3Badge(1234);
        M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
        M3Card passiveCard = new M3Card(new Label("Card"));
        M3Card actionCard = new M3Card(new Label("Action"));
        actionCard.setOnAction(event -> {
        });

        assertEquals(AccessibleRole.BUTTON, new M3Button().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3ButtonGroup().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3SplitButton().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3FabMenu().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3Toolbar().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3IconButton().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3FloatingActionButton().getAccessibleRole());
        assertEquals(AccessibleRole.CHECK_BOX, new M3CheckBox().getAccessibleRole());
        assertEquals(AccessibleRole.RADIO_BUTTON, new M3RadioButton().getAccessibleRole());
        assertEquals(AccessibleRole.CHECK_BOX, new M3Switch().getAccessibleRole());
        assertEquals(AccessibleRole.SLIDER, new M3Slider().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3DatePicker().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3DateRangePicker().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3TimePicker().getAccessibleRole());
        assertEquals(AccessibleRole.COMBO_BOX, new M3DatePickerField().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3DateRangePickerField().getAccessibleRole());
        assertEquals(AccessibleRole.COMBO_BOX, new M3TimePickerField().getAccessibleRole());
        assertEquals(AccessibleRole.PROGRESS_INDICATOR, new M3ProgressBar().getAccessibleRole());
        assertEquals(AccessibleRole.PROGRESS_INDICATOR, new M3ProgressIndicator().getAccessibleRole());
        assertEquals(AccessibleRole.PROGRESS_INDICATOR, new M3LoadingIndicator().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT_FIELD, new M3TextField().getAccessibleRole());
        assertEquals(AccessibleRole.PASSWORD_FIELD, new M3PasswordField().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT_AREA, new M3TextArea().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3TextInputLayout().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3Text("Text").getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3Icon("info").getAccessibleRole());
        assertEquals(AccessibleRole.IMAGE_VIEW, new M3Avatar("A").getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3BadgedBox().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, badge.getAccessibleRole());
        assertEquals("123+", badge.getAccessibleText());
        badge.setMaxCharacterCount(2);
        assertEquals("12+", badge.getAccessibleText());
        assertEquals(AccessibleRole.NODE, new M3Divider().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3Surface().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3FormPane().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3FormSection().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3FormRow().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3ValidationSummary().getAccessibleRole());
        assertEquals(AccessibleRole.DIALOG, new M3DialogPane().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, passiveCard.getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, actionCard.getAccessibleRole());
        assertTrue(actionCard.isFocusTraversable());
        assertEquals(AccessibleRole.PARENT, new M3Banner().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, snackbar.getAccessibleRole());
        assertEquals("Saved Undo", snackbar.getAccessibleText());
        assertEquals(AccessibleRole.PARENT, new M3SnackbarHost().getAccessibleRole());
        assertEquals(AccessibleRole.DIALOG, new M3SideSheet().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3BottomSheet().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3Scrim().getAccessibleRole());
        assertEquals("Dismiss", new M3Scrim().getAccessibleText());
        assertEquals(AccessibleRole.LIST_VIEW, new M3Carousel().getAccessibleRole());
        assertEquals(AccessibleRole.MENU, new M3Menu().getAccessibleRole());
        assertEquals(AccessibleRole.MENU_BUTTON, new M3MenuButton().getAccessibleRole());
        assertEquals(AccessibleRole.MENU_ITEM, new M3MenuItem().getAccessibleRole());
        assertEquals(AccessibleRole.MENU_ITEM, new M3SubMenuItem().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3MenuSectionHeader().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3SearchBar().getAccessibleRole());
        assertEquals(AccessibleRole.PARENT, new M3SearchView().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3ListPane().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3ListView<>().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_ITEM, new M3ListViewCell<>(new M3ListView<>()).getAccessibleRole());
        assertEquals(AccessibleRole.LIST_ITEM, new M3ListItem().getAccessibleRole());
        assertEquals(AccessibleRole.TEXT, new M3ListSectionHeader().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3ChipGroup().getAccessibleRole());
        M3Chip assistChip = new M3Chip();
        assertEquals(AccessibleRole.BUTTON, assistChip.getAccessibleRole());
        assistChip.setVariant(M3ChipVariant.FILTER);
        assertEquals(AccessibleRole.TOGGLE_BUTTON, assistChip.getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3IconToggleButtonGroup().getAccessibleRole());
        assertEquals(AccessibleRole.TOGGLE_BUTTON, new M3IconToggleButton().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3SegmentedButtonGroup().getAccessibleRole());
        assertEquals(AccessibleRole.TOGGLE_BUTTON, new M3SegmentedButton().getAccessibleRole());
        assertEquals(AccessibleRole.TAB_PANE, new M3TabBar().getAccessibleRole());
        assertEquals(AccessibleRole.TAB_ITEM, new M3Tab().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3TopAppBar().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3BottomAppBar().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3NavigationBar().getAccessibleRole());
        assertEquals(AccessibleRole.TOOL_BAR, new M3NavigationRail().getAccessibleRole());
        assertEquals(AccessibleRole.LIST_VIEW, new M3NavigationDrawer().getAccessibleRole());
        assertEquals(AccessibleRole.NODE, new M3NavigationDrawerGroup().getAccessibleRole());
        assertEquals(AccessibleRole.BUTTON, new M3NavigationItem().getAccessibleRole());
    }

    /// Verifies that custom controls expose stable keyboard traversal defaults.
    @Test
    void controlsExposeFocusTraversalDefaults() {
        M3Card passiveCard = new M3Card(new Label("Card"));
        M3Card actionCard = new M3Card(new Label("Action"));
        actionCard.setOnAction(event -> {
        });
        Label carouselItem = new Label("Carousel item");
        M3Carousel carousel = new M3Carousel();
        carousel.getItems().add(carouselItem);

        List<Node> focusableControls = List.of(
                new M3Button(),
                new M3IconButton(),
                new M3MenuButton(),
                new M3FloatingActionButton(),
                new M3CheckBox(),
                new M3RadioButton(),
                new M3Switch(),
                new M3Slider(),
                new M3DatePicker(),
                new M3DateRangePicker(),
                new M3TimePicker(),
                new M3TextField(),
                new M3PasswordField(),
                new M3TextArea(),
                new M3Scrim(),
                new M3SearchBar(),
                new M3MenuItem(),
                new M3SubMenuItem(),
                new M3ListItem(),
                new M3ListView<>(),
                new M3Chip(),
                new M3IconToggleButton(),
                new M3SegmentedButton(),
                new M3Tab(),
                new M3NavigationItem(),
                actionCard
        );
        for (Node control : focusableControls) {
            assertTrue(control.isFocusTraversable(),
                    () -> control.getClass().getSimpleName() + " should participate in keyboard traversal");
        }

        List<Node> structuralControls = List.of(
                new M3ButtonGroup(),
                new M3SplitButton(),
                carousel,
                new M3FabMenu(),
                new M3Toolbar(),
                new M3DatePickerField(),
                new M3DateRangePickerField(),
                new M3TimePickerField(),
                new M3ProgressBar(),
                new M3ProgressIndicator(),
                new M3LoadingIndicator(),
                new M3TextInputLayout(),
                new M3Text(),
                new M3Icon(),
                new M3Avatar(),
                new M3BadgedBox(),
                new M3Badge(),
                new M3Divider(),
                new M3Surface(),
                new M3FormPane(),
                new M3FormSection(),
                new M3FormRow(),
                new M3ValidationSummary(),
                new M3DialogPane(),
                passiveCard,
                new M3Banner(),
                new M3Snackbar(),
                new M3SnackbarHost(),
                new M3SideSheet(),
                new M3BottomSheet(),
                new M3Menu(),
                new M3MenuSectionHeader(),
                new M3SearchView(),
                new M3ListPane(),
                new M3ListSectionHeader(),
                new M3ChipGroup(),
                new M3IconToggleButtonGroup(),
                new M3SegmentedButtonGroup(),
                new M3TabBar(),
                new M3TopAppBar(),
                new M3BottomAppBar(),
                new M3NavigationBar(),
                new M3NavigationRail(),
                new M3NavigationDrawer(),
                new M3NavigationDrawerGroup()
        );
        for (Node control : structuralControls) {
            assertFalse(control.isFocusTraversable(),
                    () -> control.getClass().getSimpleName() + " should expose child focus instead of root traversal");
        }
        assertTrue(carouselItem.isFocusTraversable(), "Carousel items should participate in keyboard traversal");
    }

    /// Verifies that accessible fire actions invoke public action handlers.
    @Test
    void actionControlsFireFromAccessibleAction() {
        assertAccessibleFireInvokesAction(new M3Button("Button"));
        assertAccessibleFireInvokesAction(new M3IconButton(new M3Icon("A")));
        assertAccessibleFireInvokesAction(new M3FloatingActionButton("A"));
        assertAccessibleFireInvokesListItemAction(new M3MenuItem("Menu item"));
        assertAccessibleFireInvokesAction(new M3NavigationItem("Navigation item"));
        assertAccessibleFireInvokesCardAction(new M3Card(new Label("Action")));

        M3Scrim scrim = new M3Scrim();
        assertAccessibleFireInvokesAction(scrim, scrim::setOnAction);

        M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
        assertAccessibleFireInvokesAction(snackbar, snackbar::setOnAction);

        M3SearchBar searchBar = new M3SearchBar();
        assertAccessibleFireInvokesAction(searchBar, searchBar::setOnAction);

        M3SearchView searchView = new M3SearchView();
        assertAccessibleFireInvokesAction(searchView, searchView::setOnAction);
    }

    /// Verifies that selectable controls expose selected and toggle-state changes through accessible fire.
    @Test
    void selectionControlsToggleFromAccessibleFireAction() {
        M3CheckBox checkBox = new M3CheckBox("Checkbox");
        assertAccessibleFireToggles(checkBox, checkBox::isSelected);

        M3Switch switchControl = new M3Switch("Switch");
        assertAccessibleFireToggles(switchControl, switchControl::isSelected);

        M3RadioButton radioButton = new M3RadioButton("Radio");
        assertAccessibleFireSelects(radioButton, radioButton::isSelected);

        M3IconToggleButton iconToggleButton = new M3IconToggleButton("A");
        assertAccessibleFireToggles(iconToggleButton, iconToggleButton::isSelected);

        M3Chip chip = new M3Chip("Chip");
        chip.setVariant(M3ChipVariant.FILTER);
        assertAccessibleFireToggles(chip, chip::isSelected);

        M3SegmentedButton segmentedButton = new M3SegmentedButton("Segment");
        assertAccessibleFireToggles(segmentedButton, segmentedButton::isSelected);
    }

    /// Verifies that accessible fire follows the Material checkbox three-state cycle.
    @Test
    void indeterminateCheckboxCyclesFromAccessibleFireAction() {
        M3CheckBox checkBox = new M3CheckBox("Checkbox");
        checkBox.setAllowIndeterminate(true);

        assertToggleState(checkBox, false, false, AccessibleAttribute.ToggleState.UNCHECKED);
        checkBox.executeAccessibleAction(AccessibleAction.FIRE);
        assertToggleState(checkBox, false, true, AccessibleAttribute.ToggleState.INDETERMINATE);
        checkBox.executeAccessibleAction(AccessibleAction.FIRE);
        assertToggleState(checkBox, true, false, AccessibleAttribute.ToggleState.CHECKED);
        checkBox.executeAccessibleAction(AccessibleAction.FIRE);
        assertToggleState(checkBox, false, false, AccessibleAttribute.ToggleState.UNCHECKED);
    }

    /// Verifies that disabled action controls ignore accessible fire actions.
    @Test
    void disabledActionControlsIgnoreAccessibleFireAction() {
        M3Button button = new M3Button("Button");
        assertDisabledAccessibleFireDoesNotInvokeAction(button, button::setOnAction);

        M3IconButton iconButton = new M3IconButton(new M3Icon("A"));
        assertDisabledAccessibleFireDoesNotInvokeAction(iconButton, iconButton::setOnAction);

        M3FloatingActionButton floatingActionButton = new M3FloatingActionButton("A");
        assertDisabledAccessibleFireDoesNotInvokeAction(floatingActionButton, floatingActionButton::setOnAction);

        M3MenuItem menuItem = new M3MenuItem("Menu item");
        assertDisabledAccessibleFireDoesNotInvokeAction(menuItem, menuItem::setOnAction);

        M3NavigationItem navigationItem = new M3NavigationItem("Navigation item");
        assertDisabledAccessibleFireDoesNotInvokeAction(navigationItem, navigationItem::setOnAction);

        M3Card card = new M3Card(new Label("Action"));
        assertDisabledAccessibleFireDoesNotInvokeAction(card, card::setOnAction);

        M3Scrim scrim = new M3Scrim();
        assertDisabledAccessibleFireDoesNotInvokeAction(scrim, scrim::setOnAction);

        M3Snackbar snackbar = new M3Snackbar("Saved", "Undo");
        assertDisabledAccessibleFireDoesNotInvokeAction(snackbar, snackbar::setOnAction);

        M3SearchBar searchBar = new M3SearchBar();
        assertDisabledAccessibleFireDoesNotInvokeAction(searchBar, searchBar::setOnAction);

        M3SearchView searchView = new M3SearchView();
        assertDisabledAccessibleFireDoesNotInvokeAction(searchView, searchView::setOnAction);
    }

    /// Verifies that disabled selectable controls ignore accessible fire actions.
    @Test
    void disabledSelectionControlsIgnoreAccessibleFireAction() {
        M3CheckBox checkBox = new M3CheckBox("Checkbox");
        assertDisabledAccessibleFireDoesNotToggle(checkBox, checkBox::isSelected);

        M3Switch switchControl = new M3Switch("Switch");
        assertDisabledAccessibleFireDoesNotToggle(switchControl, switchControl::isSelected);

        M3RadioButton radioButton = new M3RadioButton("Radio");
        assertDisabledAccessibleFireDoesNotToggle(radioButton, radioButton::isSelected);

        M3IconToggleButton iconToggleButton = new M3IconToggleButton("A");
        assertDisabledAccessibleFireDoesNotToggle(iconToggleButton, iconToggleButton::isSelected);

        M3Chip chip = new M3Chip("Chip");
        chip.setVariant(M3ChipVariant.FILTER);
        assertDisabledAccessibleFireDoesNotToggle(chip, chip::isSelected);

        M3SegmentedButton segmentedButton = new M3SegmentedButton("Segment");
        assertDisabledAccessibleFireDoesNotToggle(segmentedButton, segmentedButton::isSelected);
    }

    /// Verifies that disabled aggregate controls ignore state-changing accessibility actions.
    @Test
    void disabledAggregateControlsIgnoreAccessibleStateActions() {
        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3MenuSelectionMode.SINGLE);
        menu.select(menuFirst);
        menu.setDisable(true);
        menu.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, menuSecond);
        assertEquals(List.of(menuFirst), menu.getSelectedItems());

        M3ListItem listFirst = new M3ListItem("Inbox");
        M3ListItem listSecond = new M3ListItem("Archive");
        M3ListPane listPane = new M3ListPane();
        listPane.setSelectionMode(M3ListSelectionMode.SINGLE);
        listPane.getItems().addAll(listFirst, listSecond);
        listPane.select(listFirst);
        listPane.setDisable(true);
        listPane.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, listSecond);
        assertEquals(List.of(listFirst), listPane.getSelectedItems());

        M3NavigationItem navFirst = new M3NavigationItem("Inbox");
        M3NavigationItem navSecond = new M3NavigationItem("Archive");
        M3NavigationBar navigationBar = new M3NavigationBar();
        navigationBar.getItems().addAll(navFirst, navSecond);
        navigationBar.select(navFirst);
        navigationBar.setDisable(true);
        navigationBar.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, navSecond);
        assertEquals(List.of(navFirst), navigationBar.getSelectedItems());

        M3Tab tabFirst = new M3Tab("Overview");
        M3Tab tabSecond = new M3Tab("Details");
        M3TabBar tabBar = new M3TabBar();
        tabBar.getTabs().addAll(tabFirst, tabSecond);
        tabBar.select(tabFirst);
        tabBar.setDisable(true);
        tabBar.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, tabSecond);
        assertEquals(List.of(tabFirst), tabBar.getSelectedTabs());

        M3Slider slider = new M3Slider();
        slider.setValue(20.0);
        slider.setDisable(true);
        slider.executeAccessibleAction(AccessibleAction.INCREMENT);
        slider.executeAccessibleAction(AccessibleAction.SET_VALUE, 80.0);
        assertEquals(20.0, slider.getValue(), 0.0001);

        LocalDate date = LocalDate.of(2026, 5, 18);
        M3DatePicker datePicker = new M3DatePicker(date);
        datePicker.setDisable(true);
        datePicker.executeAccessibleAction(AccessibleAction.INCREMENT);
        datePicker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalDate.of(2026, 5, 19));
        assertEquals(date, datePicker.getValue());

        M3SearchView searchView = new M3SearchView();
        searchView.setText("M3FX");
        searchView.setActive(false);
        searchView.setDisable(true);
        searchView.executeAccessibleAction(AccessibleAction.SET_TEXT, "Changed");
        searchView.executeAccessibleAction(AccessibleAction.EXPAND);
        assertEquals("M3FX", searchView.getText());
        assertFalse(searchView.isActive());
    }

    /// Verifies that disabled picker and search controls ignore state-changing accessibility actions.
    @Test
    void disabledPickerAndSearchControlsIgnoreAccessibleStateActions() {
        LocalDate start = LocalDate.of(2026, 5, 18);
        LocalDate end = LocalDate.of(2026, 5, 22);
        M3DateRangePicker rangePicker = new M3DateRangePicker(start, end);
        rangePicker.setDisable(true);
        rangePicker.executeAccessibleAction(AccessibleAction.INCREMENT);
        rangePicker.executeAccessibleAction(
                AccessibleAction.SET_SELECTED_ITEMS,
                List.of(LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 24))
        );
        assertEquals(start, rangePicker.getStartDate());
        assertEquals(end, rangePicker.getEndDate());

        LocalTime time = LocalTime.of(10, 30);
        M3TimePicker timePicker = new M3TimePicker(time);
        timePicker.setMinuteStep(15);
        timePicker.setDisable(true);
        timePicker.executeAccessibleAction(AccessibleAction.INCREMENT);
        timePicker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalTime.of(12, 45));
        assertEquals(time, timePicker.getValue());

        M3DatePickerField dateField = new M3DatePickerField(start);
        dateField.setDisable(true);
        dateField.executeAccessibleAction(AccessibleAction.EXPAND);
        dateField.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalDate.of(2026, 5, 24));
        assertEquals(start, dateField.getValue());
        assertFalse(dateField.isShowing());

        M3TimePickerField timeField = new M3TimePickerField(time);
        timeField.setDisable(true);
        timeField.executeAccessibleAction(AccessibleAction.EXPAND);
        timeField.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalTime.of(12, 45));
        assertEquals(time, timeField.getValue());
        assertFalse(timeField.isShowing());

        M3DateRangePickerField rangeField = new M3DateRangePickerField(start, end);
        rangeField.setDisable(true);
        rangeField.executeAccessibleAction(AccessibleAction.EXPAND);
        rangeField.executeAccessibleAction(
                AccessibleAction.SET_SELECTED_ITEMS,
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 24)
        );
        assertEquals(start, rangeField.getStartDate());
        assertEquals(end, rangeField.getEndDate());
        assertFalse(rangeField.isShowing());

        M3SearchBar searchBar = new M3SearchBar();
        searchBar.setText("M3FX");
        searchBar.setActive(false);
        searchBar.setDisable(true);
        searchBar.executeAccessibleAction(AccessibleAction.SET_TEXT, "Changed");
        searchBar.executeAccessibleAction(AccessibleAction.EXPAND);
        assertEquals("M3FX", searchBar.getText());
        assertFalse(searchBar.isActive());
    }

    /// Verifies that disabled overlay hosts ignore state-changing accessibility actions.
    @Test
    void disabledOverlayControlsIgnoreAccessibleStateActions() {
        M3FabMenu fabMenu = new M3FabMenu();
        fabMenu.setDisable(true);
        fabMenu.executeAccessibleAction(AccessibleAction.EXPAND);
        fabMenu.executeAccessibleAction(AccessibleAction.FIRE);
        assertFalse(fabMenu.isExpanded());
        fabMenu.setExpanded(true);
        fabMenu.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertTrue(fabMenu.isExpanded());

        M3Scrim scrim = new M3Scrim();
        AtomicInteger scrimFireCount = new AtomicInteger();
        scrim.setOnAction(event -> scrimFireCount.incrementAndGet());
        scrim.setShown(false);
        scrim.setDisable(true);
        scrim.executeAccessibleAction(AccessibleAction.EXPAND);
        scrim.executeAccessibleAction(AccessibleAction.FIRE);
        assertFalse(scrim.isShown());
        assertEquals(0, scrimFireCount.get());
        scrim.setShown(true);
        scrim.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertTrue(scrim.isShown());

        M3BottomSheet bottomSheet = new M3BottomSheet("Bottom sheet");
        bottomSheet.setShown(false);
        bottomSheet.setDisable(true);
        bottomSheet.executeAccessibleAction(AccessibleAction.EXPAND);
        assertFalse(bottomSheet.isShown());
        bottomSheet.setShown(true);
        bottomSheet.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertTrue(bottomSheet.isShown());

        M3SideSheet sideSheet = new M3SideSheet("Side sheet");
        sideSheet.setShown(false);
        sideSheet.setDisable(true);
        sideSheet.executeAccessibleAction(AccessibleAction.EXPAND);
        assertFalse(sideSheet.isShown());
        sideSheet.setShown(true);
        sideSheet.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertTrue(sideSheet.isShown());

        M3SnackbarHost host = new M3SnackbarHost();
        host.setDisplayDuration(Duration.ZERO);
        host.show(new M3Snackbar("Visible"));
        host.setDisable(true);
        host.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertTrue(host.isShowing());
        host.dismissAll();
    }

    /// Verifies that popup-backed controls expose expanded state through accessibility actions.
    @Test
    void popupControlsExposeExpandedAccessibleStateActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuButton menuButton = new M3MenuButton("Menu", new M3MenuItem("Open"));
            M3SplitButton splitButton = new M3SplitButton("Split");
            splitButton.getItems().add(new M3MenuItem("More"));
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
            HBox root = new HBox(12.0, menuButton, splitButton, subMenuItem);
            Stage stage = new Stage();

            try {
                M3MotionSettings.setReducedMotionRequested(root, true);
                stage.setScene(new Scene(root, 520.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                assertAccessibleExpandedActions(menuButton, "menu button");
                assertAccessibleExpandedActions(splitButton, "split button");
                assertAccessibleExpandedActions(subMenuItem, "submenu item");
                assertDisabledAccessibleExpandedActions(menuButton, "menu button");
                assertDisabledAccessibleExpandedActions(splitButton, "split button");
                assertDisabledAccessibleExpandedActions(subMenuItem, "submenu item");
            } finally {
                subMenuItem.hideSubMenu();
                splitButton.hideMenu();
                menuButton.hideMenu();
                M3MotionSettings.setReducedMotionRequested(root, false);
                stage.close();
            }
        });
    }

    /// Verifies popup-backed controls forward accessible selection only while enabled.
    @Test
    void popupControlsExposeSelectedItemsAccessibleStateActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem menuFirst = new M3MenuItem("Open");
            M3MenuItem menuSecond = new M3MenuItem("Save");
            M3MenuButton menuButton = new M3MenuButton("Menu", menuFirst, menuSecond);
            menuButton.getMenu().setSelectionMode(M3MenuSelectionMode.SINGLE);

            M3MenuItem splitFirst = new M3MenuItem("Copy");
            M3MenuItem splitSecond = new M3MenuItem("Paste");
            M3SplitButton splitButton = new M3SplitButton("Split");
            splitButton.getItems().addAll(splitFirst, splitSecond);
            splitButton.getMenu().setSelectionMode(M3MenuSelectionMode.MULTIPLE);

            M3MenuItem subFirst = new M3MenuItem("Archive");
            M3MenuItem subSecond = new M3MenuItem("Trash");
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", subFirst, subSecond);
            subMenuItem.getSubMenu().setSelectionMode(M3MenuSelectionMode.MULTIPLE);

            HBox root = new HBox(12.0, menuButton, splitButton, subMenuItem);
            Stage stage = new Stage();

            try {
                M3MotionSettings.setReducedMotionRequested(root, true);
                stage.setScene(new Scene(root, 560.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                assertAccessibleSelectedItems(menuButton, List.of(menuFirst), menuFirst);
                assertAccessibleSelectedItems(splitButton, List.of(splitFirst, splitSecond), splitFirst, splitSecond);
                assertAccessibleSelectedItems(subMenuItem, List.of(subFirst, subSecond), subFirst, subSecond);

                assertDisabledAccessibleSelectionIgnored(menuButton, menuSecond, List.of(menuFirst));
                assertDisabledAccessibleSelectionIgnored(splitButton, splitSecond, List.of(splitFirst, splitSecond));
                assertDisabledAccessibleSelectionIgnored(subMenuItem, subSecond, List.of(subFirst, subSecond));
            } finally {
                subMenuItem.hideSubMenu();
                splitButton.hideMenu();
                menuButton.hideMenu();
                M3MotionSettings.setReducedMotionRequested(root, false);
                stage.close();
            }
        });
    }

    /// Verifies that drawer disclosure groups respect disabled state for accessible state actions.
    @Test
    void navigationDrawerGroupsRespectDisabledAccessibleStateActions() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup("Group");
        group.executeAccessibleAction(AccessibleAction.FIRE);
        assertTrue(group.isExpanded(), "Navigation drawer group should expand from FIRE");
        group.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertFalse(group.isExpanded(), "Navigation drawer group should collapse from COLLAPSE");
        group.executeAccessibleAction(AccessibleAction.EXPAND);
        assertTrue(group.isExpanded(), "Navigation drawer group should expand from EXPAND");

        group.setDisable(true);
        group.executeAccessibleAction(AccessibleAction.FIRE);
        assertTrue(group.isExpanded(), "Disabled navigation drawer group should ignore FIRE");
        group.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertTrue(group.isExpanded(), "Disabled navigation drawer group should ignore COLLAPSE");
        group.setExpanded(false);
        group.executeAccessibleAction(AccessibleAction.FIRE);
        assertFalse(group.isExpanded(), "Disabled navigation drawer group should ignore FIRE while collapsed");
        group.executeAccessibleAction(AccessibleAction.EXPAND);
        assertFalse(group.isExpanded(), "Disabled navigation drawer group should ignore EXPAND");
    }

    /// Verifies popup accessibility actions update expanded state for one reachable control.
    private static void assertAccessibleExpandedActions(Node control, String description) {
        assertEquals(false, control.queryAccessibleAttribute(AccessibleAttribute.EXPANDED),
                () -> description + " should start collapsed");
        control.executeAccessibleAction(AccessibleAction.SHOW_MENU);
        assertEquals(true, control.queryAccessibleAttribute(AccessibleAttribute.EXPANDED),
                () -> description + " should expand from SHOW_MENU");
        control.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertEquals(false, control.queryAccessibleAttribute(AccessibleAttribute.EXPANDED),
                () -> description + " should collapse from COLLAPSE");
        control.executeAccessibleAction(AccessibleAction.EXPAND);
        assertEquals(true, control.queryAccessibleAttribute(AccessibleAttribute.EXPANDED),
                () -> description + " should expand from EXPAND");
        control.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertEquals(false, control.queryAccessibleAttribute(AccessibleAttribute.EXPANDED),
                () -> description + " should finish collapsed");
    }

    /// Verifies disabled popup controls ignore expanded-state accessibility actions.
    private static void assertDisabledAccessibleExpandedActions(Node control, String description) {
        control.setDisable(true);
        control.executeAccessibleAction(AccessibleAction.SHOW_MENU);
        assertEquals(false, control.queryAccessibleAttribute(AccessibleAttribute.EXPANDED),
                () -> "Disabled " + description + " should ignore SHOW_MENU");
        control.executeAccessibleAction(AccessibleAction.EXPAND);
        assertEquals(false, control.queryAccessibleAttribute(AccessibleAttribute.EXPANDED),
                () -> "Disabled " + description + " should ignore EXPAND");
        control.setDisable(false);
    }

    /// Verifies one popup owner forwards accessible selected items to its attached menu.
    private static void assertAccessibleSelectedItems(
            Node control,
            List<M3MenuItem> expectedSelection,
            M3MenuItem... selectedItems
    ) {
        control.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, (Object[]) selectedItems);
        assertEquals(expectedSelection, control.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS),
                () -> control.getClass().getSimpleName() + " selected items should follow accessibility selection");
    }

    /// Verifies disabled popup owners do not forward accessible selection changes.
    private static void assertDisabledAccessibleSelectionIgnored(
            Node control,
            M3MenuItem requestedItem,
            List<M3MenuItem> expectedSelection
    ) {
        control.setDisable(true);
        control.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, requestedItem);
        assertEquals(expectedSelection, control.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS),
                () -> "Disabled " + control.getClass().getSimpleName() + " should ignore accessible selection");
        control.setDisable(false);
    }

    /// Verifies a button-style control fires its handler from the accessibility fire action.
    private static void assertAccessibleFireInvokesAction(ButtonBase control) {
        AtomicInteger fireCount = new AtomicInteger();
        control.setOnAction(event -> fireCount.incrementAndGet());
        assertAccessibleFireInvokesAction(control, fireCount);
    }

    /// Verifies a list item fires its handler from the accessibility fire action.
    private static void assertAccessibleFireInvokesListItemAction(M3ListItem control) {
        AtomicInteger fireCount = new AtomicInteger();
        control.setOnAction(event -> fireCount.incrementAndGet());
        assertAccessibleFireInvokesAction(control, fireCount);
    }

    /// Verifies an action card fires its handler from the accessibility fire action.
    private static void assertAccessibleFireInvokesCardAction(M3Card control) {
        AtomicInteger fireCount = new AtomicInteger();
        control.setOnAction(event -> fireCount.incrementAndGet());
        assertAccessibleFireInvokesAction(control, fireCount);
    }

    /// Verifies an action-bearing node fires the action count from accessibility fire.
    private static void assertAccessibleFireInvokesAction(Node control, AtomicInteger fireCount) {
        control.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, fireCount.get(),
                () -> control.getClass().getSimpleName() + " should invoke its action handler from FIRE");
    }

    /// Verifies an action-bearing node fires its handler from the accessibility fire action.
    private static void assertAccessibleFireInvokesAction(
            Node control,
            Consumer<EventHandler<ActionEvent>> actionSetter
    ) {
        AtomicInteger fireCount = new AtomicInteger();
        actionSetter.accept(event -> fireCount.incrementAndGet());
        assertAccessibleFireInvokesAction(control, fireCount);
    }

    /// Verifies a disabled action-bearing node ignores accessibility fire.
    private static void assertDisabledAccessibleFireDoesNotInvokeAction(
            Node control,
            Consumer<EventHandler<ActionEvent>> actionSetter
    ) {
        AtomicInteger fireCount = new AtomicInteger();
        actionSetter.accept(event -> fireCount.incrementAndGet());
        control.setDisable(true);
        control.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(0, fireCount.get(),
                () -> control.getClass().getSimpleName() + " should ignore FIRE while disabled");
    }

    /// Verifies an accessible fire action toggles a two-state selectable control.
    private static void assertAccessibleFireToggles(Node control, BooleanSupplier selected) {
        assertToggleState(control, false, AccessibleAttribute.ToggleState.UNCHECKED);
        control.executeAccessibleAction(AccessibleAction.FIRE);
        assertTrue(selected.getAsBoolean(),
                () -> control.getClass().getSimpleName() + " should become selected after FIRE");
        assertToggleState(control, true, AccessibleAttribute.ToggleState.CHECKED);
        control.executeAccessibleAction(AccessibleAction.FIRE);
        assertFalse(selected.getAsBoolean(),
                () -> control.getClass().getSimpleName() + " should become unselected after a second FIRE");
        assertToggleState(control, false, AccessibleAttribute.ToggleState.UNCHECKED);
    }

    /// Verifies an accessible fire action selects a radio-style control.
    private static void assertAccessibleFireSelects(Node control, BooleanSupplier selected) {
        assertToggleState(control, false, AccessibleAttribute.ToggleState.UNCHECKED);
        control.executeAccessibleAction(AccessibleAction.FIRE);
        assertTrue(selected.getAsBoolean(),
                () -> control.getClass().getSimpleName() + " should become selected after FIRE");
        assertToggleState(control, true, AccessibleAttribute.ToggleState.CHECKED);
        control.executeAccessibleAction(AccessibleAction.FIRE);
        assertTrue(selected.getAsBoolean(),
                () -> control.getClass().getSimpleName() + " should remain selected after repeated FIRE");
        assertToggleState(control, true, AccessibleAttribute.ToggleState.CHECKED);
    }

    /// Verifies a disabled selectable node does not change selection from accessibility fire.
    private static void assertDisabledAccessibleFireDoesNotToggle(Node control, BooleanSupplier selected) {
        control.setDisable(true);
        assertToggleState(control, false, AccessibleAttribute.ToggleState.UNCHECKED);
        control.executeAccessibleAction(AccessibleAction.FIRE);
        assertFalse(selected.getAsBoolean(),
                () -> control.getClass().getSimpleName() + " should ignore FIRE while disabled");
        assertToggleState(control, false, AccessibleAttribute.ToggleState.UNCHECKED);
    }

    /// Verifies a selectable control exposes the expected selected and toggle-state attributes.
    private static void assertToggleState(
            Node control,
            boolean selected,
            AccessibleAttribute.ToggleState toggleState
    ) {
        assertEquals(selected, control.queryAccessibleAttribute(AccessibleAttribute.SELECTED),
                () -> control.getClass().getSimpleName() + " selected accessibility state is wrong");
        assertEquals(toggleState, control.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE),
                () -> control.getClass().getSimpleName() + " toggle accessibility state is wrong");
    }

    /// Verifies a checkbox exposes the expected selected, indeterminate, and toggle-state attributes.
    private static void assertToggleState(
            M3CheckBox checkBox,
            boolean selected,
            boolean indeterminate,
            AccessibleAttribute.ToggleState toggleState
    ) {
        assertEquals(selected, checkBox.queryAccessibleAttribute(AccessibleAttribute.SELECTED),
                "Checkbox selected accessibility state is wrong");
        assertEquals(indeterminate, checkBox.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE),
                "Checkbox indeterminate accessibility state is wrong");
        assertEquals(toggleState, checkBox.queryAccessibleAttribute(AccessibleAttribute.TOGGLE_STATE),
                "Checkbox toggle accessibility state is wrong");
    }
}
