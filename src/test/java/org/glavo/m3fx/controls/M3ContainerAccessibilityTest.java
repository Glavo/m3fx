// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies accessibility roles, focus targets, and actions in composite controls.
@NotNullByDefault
final class M3ContainerAccessibilityTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that custom actionable surfaces respond to accessibility fire actions.
    @Test
    void actionableSurfacesExecuteAccessibleFire() {
        AtomicInteger cardActions = new AtomicInteger();
        M3Card card = new M3Card(new Label("Card"));
        card.setOnAction(event -> cardActions.incrementAndGet());

        card.executeAccessibleAction(AccessibleAction.FIRE);
        card.setDisable(true);
        card.executeAccessibleAction(AccessibleAction.FIRE);

        assertEquals(1, cardActions.get());

        AtomicInteger scrimActions = new AtomicInteger();
        M3Scrim scrim = new M3Scrim();
        scrim.setOnAction(event -> scrimActions.incrementAndGet());

        scrim.executeAccessibleAction(AccessibleAction.FIRE);
        scrim.setDisable(true);
        scrim.executeAccessibleAction(AccessibleAction.FIRE);

        assertEquals(1, scrimActions.get());

    }

    /// Verifies that composite controls can focus indexed children through accessibility show-item actions.
    @Test
    void compositeControlsSupportAccessibleShowItemActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3ButtonGroup group = buttonGroup(first, second);
            M3Button listTrailing = new M3Button("More");
            M3ListItem listItem = new M3ListItem("Row");
            listItem.setTrailing(listTrailing);
            M3ListPane listPane = listPane(listItem);
            Stage stage = new Stage();
            try {
                VBox root = new VBox(group, listPane);
                stage.setScene(new Scene(root, 240.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(first.isFocused());

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(second.isFocused());

                group.executeAccessibleAction(AccessibleAction.SHOW_ITEM, first);
                assertTrue(first.isFocused());

                listPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(listItem.isFocused());

                listPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, listTrailing);
                assertTrue(listTrailing.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that indexed show-item parameters can still reveal nested targets inside the indexed item.
    @Test
    void indexedContainersDelegateShowItemIntoNestedTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstTrailing = new M3Button("First trailing");
            M3ListItem firstItem = new M3ListItem("First row");
            firstItem.setTrailing(firstTrailing);
            M3Button secondTrailing = new M3Button("Second trailing");
            M3ListItem secondItem = new M3ListItem("Second row");
            secondItem.setTrailing(secondTrailing);
            M3ListPane list = listPane(firstItem, secondItem);

            M3Button firstCarouselAction = new M3Button("First carousel action");
            HBox firstCarouselItem = new HBox(firstCarouselAction);
            M3Button secondCarouselAction = new M3Button("Second carousel action");
            HBox secondCarouselItem = new HBox(secondCarouselAction);
            M3Button hiddenCarouselAction = new M3Button("Hidden carousel action");
            hiddenCarouselAction.setVisible(false);
            HBox hiddenCarouselItem = new HBox(hiddenCarouselAction);
            M3Carousel carousel = carousel(firstCarouselItem, secondCarouselItem, hiddenCarouselItem);
            carousel.setAnimatedScroll(false);

            M3Button firstDrawerTrailing = new M3Button("First drawer trailing");
            M3ListItem firstDrawerItem = new M3ListItem("First drawer row");
            firstDrawerItem.setTrailing(firstDrawerTrailing);
            M3Button secondDrawerTrailing = new M3Button("Second drawer trailing");
            M3ListItem secondDrawerItem = new M3ListItem("Second drawer row");
            secondDrawerItem.setTrailing(secondDrawerTrailing);
            M3NavigationDrawerGroup drawerGroup = new M3NavigationDrawerGroup("Drawer group");
            drawerGroup.getItems().add(firstDrawerItem);
            drawerGroup.getItems().add(secondDrawerItem);
            drawerGroup.setExpanded(false);

            Stage stage = new Stage();
            try {
                VBox root = new VBox(list, carousel, drawerGroup);
                stage.setScene(new Scene(root, 520.0, 360.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                carousel.resizeRelocate(0.0, 0.0, 480.0, 120.0);
                root.applyCss();
                root.layout();

                list.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1, secondTrailing);

                assertTrue(secondTrailing.isFocused());
                assertSame(secondTrailing, secondItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(secondTrailing, list.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                assertFalse(drawerGroup.isExpanded());
                drawerGroup.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 2, secondDrawerTrailing);

                assertTrue(drawerGroup.isExpanded());
                assertTrue(secondDrawerTrailing.isFocused());
                assertSame(secondDrawerTrailing,
                        secondDrawerItem.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(secondDrawerTrailing, drawerGroup.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                carousel.select(firstCarouselItem);
                assertSame(firstCarouselItem, carousel.getSelectedItem());

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 2, hiddenCarouselAction);
                root.layout();

                assertSame(firstCarouselItem, carousel.getSelectedItem());
                assertFalse(hiddenCarouselAction.isFocused());

                carousel.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1, secondCarouselAction);
                root.layout();

                assertSame(secondCarouselItem, carousel.getSelectedItem());
                assertTrue(secondCarouselAction.isFocused());
                assertSame(secondCarouselAction, carousel.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that structural indexed containers focus or reveal requested accessibility items.
    @Test
    void structuralIndexedContainersSupportAccessibleShowItemActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button navigation = new M3Button("Navigation");
            M3Button topAction = new M3Button("Top action");
            M3TopAppBar topAppBar = topAppBar("Inbox", navigation, topAction);

            M3Button bottomAction = new M3Button("Bottom action");
            M3Button floatingAction = new M3Button("Floating action");
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    bottomAction
            );

            M3Button bannerAction = new M3Button("Dismiss");
            M3Banner banner = createBanner("Network unavailable", new M3Icon("!"), bannerAction);
            M3Button surfaceAction = new M3Button("Surface action");
            M3Surface surface = surface(surfaceAction);
            M3Button badgedContent = new M3Button("Messages");
            M3BadgedBox badgedBox = new M3BadgedBox(badgedContent, new M3Badge("3"));

            M3TextField textField = new M3TextField();
            M3Button leading = new M3Button("Leading");
            M3TextInputLayout inputLayout = new M3TextInputLayout(textField);
            inputLayout.setSupportingText("Helper text");
            inputLayout.setLeading(leading);

            M3Button rowContentPrimary = new M3Button("Row primary");
            M3Button rowContentSecondary = new M3Button("Row secondary");
            M3Button rowTrailingPrimary = new M3Button("Row trailing primary");
            M3Button rowTrailingSecondary = new M3Button("Row trailing secondary");
            M3FormRow formRow = new M3FormRow(
                    "Row",
                    "",
                    new HBox(rowContentPrimary, rowContentSecondary),
                    new HBox(rowTrailingPrimary, rowTrailingSecondary)
            );

            M3ListItem firstResult = new M3ListItem("First result");
            M3ListItem secondResult = new M3ListItem("Second result");
            M3SearchView searchView = searchView("Search", firstResult, secondResult);
            M3Button fabAction = new M3Button("Create note");
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(fabAction);

            VBox root = new VBox(
                    topAppBar,
                    bottomAppBar,
                    banner,
                    surface,
                    badgedBox,
                    inputLayout,
                    formRow,
                    searchView,
                    fabMenu
            );
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 560.0, 560.0));
                stage.show();

                topAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(navigation.isFocused());

                topAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, topAction);
                assertTrue(topAction.isFocused());

                topAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(topAction.isFocused());

                bottomAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(bottomAction.isFocused());

                bottomAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, floatingAction);
                assertTrue(floatingAction.isFocused());

                bottomAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(floatingAction.isFocused());

                banner.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(bannerAction.isFocused());

                banner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bannerAction);
                assertTrue(bannerAction.isFocused());

                banner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(bannerAction.isFocused());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(surfaceAction.isFocused());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(surfaceAction.isFocused());

                badgedBox.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(badgedContent.isFocused());

                badgedBox.executeAccessibleAction(AccessibleAction.SHOW_ITEM, badgedContent);
                assertTrue(badgedContent.isFocused());

                badgedBox.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(badgedContent.isFocused());

                inputLayout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(textField.isFocused());

                formRow.executeAccessibleAction(AccessibleAction.SHOW_ITEM, rowContentSecondary);
                assertTrue(rowContentSecondary.isFocused());

                formRow.executeAccessibleAction(AccessibleAction.SHOW_ITEM, rowTrailingSecondary);
                assertTrue(rowTrailingSecondary.isFocused());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);
                assertTrue(searchView.isActive());
                assertTrue(secondResult.isFocused());

                fabMenu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(fabMenu.isExpanded());
                assertTrue(fabAction.isFocused());
            } finally {
                stage.close();
            }
        });

    }

    /// Verifies that indexed structural containers expose default accessibility focus targets.
    @Test
    void structuralIndexedContainersExposeAccessibleFocusTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button groupFirst = new M3Button("First");
            M3Button groupSecond = new M3Button("Second");
            M3ButtonGroup buttonGroup = buttonGroup(groupFirst, groupSecond);

            M3Button badgedContent = new M3Button("Inbox");
            M3BadgedBox badgedBox = new M3BadgedBox(badgedContent, new M3Badge("4"));

            M3Button bannerAction = new M3Button("Dismiss");
            M3Banner banner = createBanner("Offline", new M3Icon("!"), bannerAction);

            M3Button topNavigation = new M3Button("Menu");
            M3Button topAction = new M3Button("Search");
            M3TopAppBar topAppBar = topAppBar("Inbox", topNavigation, topAction);

            M3Button bottomAction = new M3Button("Archive");
            M3Button floatingAction = new M3Button("Create");
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    floatingAction,
                    bottomAction
            );

            M3Button surfaceAction = new M3Button("Surface action");
            M3Surface surface = surface(surfaceAction);

            M3SplitButton splitButton = splitButton("Create", new M3MenuItem("Draft"));

            M3Button formItem = new M3Button("Form item");
            M3FormPane formPane = formPane(formItem);

            M3Button sectionItem = new M3Button("Section item");
            M3FormSection formSection = formSection("Section", sectionItem);

            M3Button rowContent = new M3Button("Row content");
            M3Button rowTrailing = new M3Button("Row trailing");
            M3FormRow formRow = new M3FormRow("Row", "Helper", rowContent, rowTrailing);

            M3FabMenu fabMenu = new M3FabMenu(new M3FloatingActionButton(new M3Icon("+")));

            M3Button carouselFirst = new M3Button("One");
            M3Button carouselSecond = new M3Button("Two");
            M3Carousel carousel = carousel(carouselFirst, carouselSecond);
            carousel.select(carouselSecond);

            M3TextField invalidField = new M3TextField();
            M3TextInputLayout invalidLayout = new M3TextInputLayout(invalidField);
            invalidLayout.setSupportingText("Name");
            invalidLayout.setValidator(M3TextInputValidators.required("Required"));
            M3TextField secondInvalidField = new M3TextField();
            M3TextInputLayout secondInvalidLayout = new M3TextInputLayout(secondInvalidField);
            secondInvalidLayout.setSupportingText("Email");
            secondInvalidLayout.setValidator(M3TextInputValidators.required("Required"));
            M3FormValidator validator = new M3FormValidator(invalidLayout, secondInvalidLayout);
            M3ValidationSummary validationSummary = new M3ValidationSummary(validator);
            assertFalse(validator.validate());

            VBox root = new VBox(
                    buttonGroup,
                    badgedBox,
                    banner,
                    topAppBar,
                    bottomAppBar,
                    surface,
                    splitButton,
                    formPane,
                    formSection,
                    formRow,
                    fabMenu,
                    carousel,
                    invalidLayout,
                    secondInvalidLayout,
                    validationSummary
            );
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 760.0, 920.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertAccessibleFocus(buttonGroup, groupFirst);
                assertAccessibleFocus(badgedBox, badgedContent);
                assertAccessibleFocus(banner, bannerAction);
                assertAccessibleFocus(topAppBar, topNavigation);
                assertAccessibleFocus(bottomAppBar, bottomAction);
                assertAccessibleFocus(surface, surfaceAction);
                assertAccessibleFocus(splitButton, splitButtonActionButton(splitButton));
                assertAccessibleFocus(formPane, formItem);
                assertAccessibleFocus(formSection, sectionItem);
                assertAccessibleFocus(formRow, rowContent);
                assertAccessibleFocus(fabMenu, fabMenu.getToggleButton());
                assertAccessibleFocus(carousel, carouselSecond);
                assertAccessibleFocus(validationSummary, invalidField);
                assertCurrentAccessibleFocus(validationSummary, secondInvalidField);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that actionable containers ignore activation keys while a nested text input owns focus.
    @Test
    void actionableContainersIgnoreEmbeddedTextInputActivationKeys() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicInteger cardActions = new AtomicInteger();
            M3TextField cardEditor = new M3TextField("Card text");
            M3Card card = new M3Card(cardEditor, M3CardVariant.FILLED);
            card.setOnAction(event -> cardActions.incrementAndGet());
            AtomicInteger listActions = new AtomicInteger();
            M3TextField listEditor = new M3TextField("List text");
            M3ListItem listItem = new M3ListItem("Editable row");
            listItem.setTrailingMedia(listEditor, M3ListItemSlotSize.WIDE_THUMBNAIL);
            listItem.setOnAction(event -> listActions.incrementAndGet());

            VBox root = new VBox(12.0, card, listItem);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 520.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                cardEditor.requestFocus();
                assertTrue(cardEditor.isFocused());
                card.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                card.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
                card.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
                assertEquals(0, cardActions.get());

                listEditor.requestFocus();
                assertTrue(listEditor.isFocused());
                listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
                listItem.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
                assertEquals(0, listActions.get());

                card.requestFocus();
                assertTrue(card.isFocused());
                card.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                assertEquals(1, cardActions.get());

                listItem.requestFocus();
                assertTrue(listItem.isFocused());
                listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                listItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
                listItem.fireEvent(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE));
                assertEquals(2, listActions.get());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that structural containers support directional focus without stealing text input keys.
    @Test
    void structuralContainersSupportDirectionalKeyboardTraversalWithoutStealingTextInputKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button surfaceFirst = new M3Button("Surface first");
            M3Button hiddenSurfaceTarget = new M3Button("Hidden");
            M3Button disabledSurfaceTarget = new M3Button("Disabled");
            M3Button surfaceSecond = new M3Button("Surface second");
            M3TextField surfaceEditor = new M3TextField("M3FX");
            M3Button surfaceLast = new M3Button("Surface last");
            hiddenSurfaceTarget.setVisible(false);
            disabledSurfaceTarget.setDisable(true);
            M3Surface surface = surface(
                    surfaceFirst,
                    hiddenSurfaceTarget,
                    disabledSurfaceTarget,
                    surfaceSecond,
                    surfaceEditor,
                    surfaceLast
            );

            M3Button badgedContent = new M3Button("Inbox");
            M3Badge focusableBadge = new M3Badge("7");
            focusableBadge.setFocusTraversable(true);
            M3BadgedBox badgedBox = new M3BadgedBox(badgedContent, focusableBadge);

            M3Button cardContent = new M3Button("Card details");
            M3Card actionCard = new M3Card(cardContent, M3CardVariant.FILLED);
            actionCard.setOnAction(event -> {
            });
            M3TextField cardEditor = new M3TextField("Card text");
            M3Card passiveInputCard = new M3Card(cardEditor);
            M3Button outside = new M3Button("Outside");
            VBox root = new VBox(8.0, surface, badgedBox, actionCard, passiveInputCard, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 680.0, 520.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                surface.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(surfaceFirst.isFocused());
                surfaceFirst.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(surfaceSecond.isFocused());
                assertFalse(hiddenSurfaceTarget.isFocused());
                assertFalse(disabledSurfaceTarget.isFocused());
                surfaceSecond.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(surfaceEditor.isFocused());
                surfaceEditor.positionCaret(1);
                surfaceEditor.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(surfaceEditor.isFocused());
                assertFalse(surfaceLast.isFocused());

                surface.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                surfaceFirst.requestFocus();
                surfaceFirst.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(surfaceSecond.isFocused());
                surfaceSecond.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(surfaceFirst.isFocused());

                badgedContent.requestFocus();
                badgedContent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(focusableBadge.isFocused());
                focusableBadge.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(focusableBadge.isFocused());
                badgedBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                badgedContent.requestFocus();
                badgedContent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(focusableBadge.isFocused());
                focusableBadge.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(badgedContent.isFocused());

                actionCard.requestFocus();
                assertTrue(actionCard.isFocused());
                actionCard.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(cardContent.isFocused());
                cardContent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(actionCard.isFocused(), () -> "focused=" + scene.getFocusOwner());
                actionCard.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                actionCard.requestFocus();
                actionCard.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(cardContent.isFocused());
                cardContent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(actionCard.isFocused(), () -> "focused=" + scene.getFocusOwner());

                cardEditor.requestFocus();
                cardEditor.positionCaret(1);
                cardEditor.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(cardEditor.isFocused());
                assertFalse(actionCard.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that custom selectable controls expose accessibility selection state.
    @Test
    void selectableControlsExposeAccessibleSelectionState() {
        M3SelectableChip chip = createFilterChip("Filter", true);
        M3IconToggleButton iconToggleButton = createIconToggleButton(
                "star",
                M3IconToggleButtonVariant.TONAL,
                true
        );
        M3SegmentedButton segmentedButton = createSegmentedButton("Day", true);
        M3Tab tab = createTab("Overview", true);
        M3NavigationItem navigationItem = createNavigationItem("Home", true);

        assertEquals(true, chip.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertAggregateToggleState(chip, true);
        chip.setSelected(false);
        assertEquals(false, chip.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertAggregateToggleState(chip, false);

        assertEquals(true, iconToggleButton.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertAggregateToggleState(iconToggleButton, true);
        assertEquals(true, segmentedButton.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertAggregateToggleState(segmentedButton, true);
        assertEquals(true, tab.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(true, navigationItem.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
    }

    /// Verifies that list and menu items expose accessible text, selection, position, and fire actions.
    @Test
    void listAndMenuItemsExposeAccessibleStateAndActions() {
        M3ListItem listItem = new M3ListItem("Headline");
        listItem.setOverlineText("Overline");
        listItem.setSupportingText("Supporting");
        listItem.setSelected(true);
        AtomicInteger listActions = new AtomicInteger();
        listItem.setOnAction(event -> listActions.incrementAndGet());
        M3ListPane list = listPane(new M3Divider(), listItem);

        applyCss(list);

        assertEquals("Overline Headline Supporting", listItem.getAccessibleText());
        assertEquals(true, listItem.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(1, listItem.queryAccessibleAttribute(AccessibleAttribute.INDEX));
        listItem.executeAccessibleAction(AccessibleAction.FIRE);
        listItem.setDisable(true);
        listItem.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, listActions.get());

        M3MenuItem menuItem = new M3MenuItem("Open");
        menuItem.setSelected(true);
        AtomicInteger menuActions = new AtomicInteger();
        menuItem.setOnAction(event -> menuActions.incrementAndGet());
        new M3Menu(new M3Divider(), menuItem);

        assertEquals("Open", menuItem.getAccessibleText());
        assertEquals(true, menuItem.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
        assertEquals(1, menuItem.queryAccessibleAttribute(AccessibleAttribute.INDEX));
        menuItem.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, menuActions.get());

        assertEquals(listItem, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
    }

    /// Verifies that structural containers expose indexed accessibility content.
    @Test
    void structuralContainersExposeAccessibleCollections() {
        Label surfaceContent = new Label("Surface");
        Label surfaceExtra = new Label("Extra");
        M3Surface surface = surface(surfaceContent);

        assertEquals(surfaceContent, surface.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(1, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(surfaceContent, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        surface.getContent().add(surfaceExtra);
        assertNull(surface.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(2, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(surfaceExtra, surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertNull(surface.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, -1));

        Label badgeContent = new Label("Inbox");
        M3Badge badge = new M3Badge("3");
        M3BadgedBox badgedBox = new M3BadgedBox(badgeContent, badge);

        assertEquals(badgeContent, badgedBox.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(2, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(badgeContent, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(badge, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        badgedBox.setContent(null);
        assertNull(badgedBox.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
        assertEquals(1, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(badge, badgedBox.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        Label navigation = new Label("Menu");
        Label search = new Label("Search");
        Label more = new Label("More");
        M3TopAppBar topAppBar = topAppBar("Inbox", navigation, search, more);

        assertEquals("Inbox", topAppBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals("Inbox", topAppBar.getAccessibleText());
        assertEquals(3, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(navigation, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(search, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(more, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
        topAppBar.setTitle("Archive");
        topAppBar.setNavigation(null);
        assertEquals("Archive", topAppBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals("Archive", topAppBar.getAccessibleText());
        assertEquals(2, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(search, topAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

        Label bottomAction = new Label("Search");
        Label floatingAction = new Label("Create");
        M3BottomAppBar bottomAppBar = bottomAppBar(
                M3BottomAppBarFloatingActionAlignment.END,
                floatingAction,
                bottomAction
        );

        assertEquals(2, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(bottomAction, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(floatingAction, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        bottomAppBar.setFloatingAction(null);
        assertEquals(1, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertNull(bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
    }

    /// Verifies that slot and container controls report the currently focused accessible child.
    @Test
    void slotContainersReportCurrentFocusedAccessibleChild() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button topNavigation = new M3Button("Menu");
            M3Button topSearch = new M3Button("Search");
            M3Button topAccount = new M3Button("Account");
            M3TopAppBar topAppBar = topAppBar("Inbox", topNavigation, topSearch, topAccount);

            M3Button bottomSearch = new M3Button("Search");
            M3Button bottomMore = new M3Button("More");
            M3Button bottomCreate = new M3Button("Create");
            M3BottomAppBar bottomAppBar = bottomAppBar(
                    M3BottomAppBarFloatingActionAlignment.END,
                    bottomCreate,
                    bottomSearch,
                    bottomMore
            );

            M3Button bannerDismiss = new M3Button("Dismiss");
            M3Button bannerAction = new M3Button("Action");
            M3Banner banner = banner("Message", bannerDismiss, bannerAction);

            M3Button surfacePrimary = new M3Button("Primary");
            M3Button surfaceSecondary = new M3Button("Secondary");
            M3Surface surface = surface(surfacePrimary, surfaceSecondary);

            M3Button badgedContent = new M3Button("Inbox");
            M3Badge badge = new M3Badge("3");
            badge.setFocusTraversable(true);
            M3BadgedBox badgedBox = new M3BadgedBox(badgedContent, badge);

            M3Button rowContent = new M3Button("Row content");
            M3Button rowTrailing = new M3Button("Row trailing");
            M3FormRow formRow = new M3FormRow("Name", "", rowContent, rowTrailing);

            M3Button paneFirst = new M3Button("Pane first");
            M3Button paneSecond = new M3Button("Pane second");
            M3FormPane formPane = formPane(paneFirst, paneSecond);

            M3Button sectionFirst = new M3Button("Section first");
            M3Button sectionSecond = new M3Button("Section second");
            M3FormSection formSection = formSection("Settings", sectionFirst, sectionSecond);

            M3Button groupFirst = new M3Button("Group first");
            M3Button groupSecond = new M3Button("Group second");
            M3ButtonGroup buttonGroup = buttonGroup(groupFirst, groupSecond);

            M3SplitButton splitButton = new M3SplitButton("Create");

            M3NavigationDrawerGroup drawerGroup = new M3NavigationDrawerGroup("Group");
            M3ListItem drawerChild = new M3ListItem("Child");
            drawerGroup.getItems().add(drawerChild);
            drawerGroup.setExpanded(true);

            M3Button sideContent = new M3Button("Side content");
            M3Button sideAction = new M3Button("Side action");
            M3SideSheet sideSheet = sideSheet("Side", sideContent, sideAction);

            M3Button bottomContent = new M3Button("Bottom content");
            M3Button bottomAction = new M3Button("Bottom action");
            M3BottomSheet bottomSheet = bottomSheet("Bottom", bottomContent, bottomAction);

            VBox root = new VBox(
                    8.0,
                    topAppBar,
                    bottomAppBar,
                    banner,
                    surface,
                    badgedBox,
                    formRow,
                    formPane,
                    formSection,
                    buttonGroup,
                    splitButton,
                    drawerGroup,
                    sideSheet,
                    bottomSheet
            );
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 760.0, 960.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertCurrentAccessibleFocus(topAppBar, topAccount);
                assertCurrentAccessibleFocus(bottomAppBar, bottomCreate);
                assertCurrentAccessibleFocus(banner, bannerAction);
                assertCurrentAccessibleFocus(surface, surfaceSecondary);
                assertCurrentAccessibleFocus(badgedBox, badge);
                assertCurrentAccessibleFocus(formRow, rowTrailing);
                assertCurrentAccessibleFocus(formPane, paneSecond);
                assertCurrentAccessibleFocus(formSection, sectionSecond);
                assertCurrentAccessibleFocus(buttonGroup, groupSecond);
                assertCurrentAccessibleFocus(splitButton, splitButtonMenuButton(splitButton));
                assertCurrentAccessibleFocus(drawerGroup, drawerChild);
                assertCurrentAccessibleFocus(sideSheet, sideAction);
                assertCurrentAccessibleFocus(bottomSheet, bottomAction);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that hidden ancestor chains are excluded from structural accessibility focus routing.
    @Test
    void structuralContainersSkipFocusTargetsInsideHiddenAncestors() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button outside = new M3Button("Outside");
            M3Button hiddenTarget = new M3Button("Hidden target");
            M3Button visibleTarget = new M3Button("Visible target");
            Pane hiddenContainer = new Pane(hiddenTarget);
            hiddenContainer.setVisible(false);
            M3Surface surface = surface(hiddenContainer, visibleTarget);
            VBox root = new VBox(8.0, outside, surface);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 420.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertFalse(M3Accessible.canReach(hiddenTarget));
                assertFalse(M3Accessible.isEffectivelyReachable(hiddenTarget));
                assertTrue(M3Accessible.canReach(visibleTarget));
                assertSame(visibleTarget, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                outside.requestFocus();
                assertTrue(outside.isFocused());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenTarget);

                assertTrue(outside.isFocused());
                assertFalse(hiddenTarget.isFocused());
                assertSame(visibleTarget, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                surface.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(visibleTarget.isFocused());
                assertSame(visibleTarget, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that shared keyboard navigation skips candidates hidden by an ancestor.
    @Test
    void selectionNavigationSkipsNodesInsideHiddenAncestors() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button hiddenTarget = new M3Button("Hidden");
            M3Button visibleTarget = new M3Button("Visible");
            Pane hiddenContainer = new Pane(hiddenTarget);
            hiddenContainer.setVisible(false);
            Pane root = new Pane(hiddenContainer, visibleTarget);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 360.0, 160.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                javafx.collections.ObservableList<Node> candidates =
                        javafx.collections.FXCollections.observableArrayList(hiddenTarget, visibleTarget);

                assertNull(M3SelectionNavigation.focused(candidates, M3Button.class));
                assertSame(visibleTarget, M3SelectionNavigation.first(candidates, M3Button.class));
                assertSame(visibleTarget, M3SelectionNavigation.next(candidates, null, M3Button.class));
                assertSame(visibleTarget, M3SelectionNavigation.previous(candidates, null, M3Button.class));

                KeyEvent event = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

                assertTrue(M3SelectionNavigation.handleKeyFocus(
                        event,
                        null,
                        candidates,
                        null,
                        M3Button.class,
                        true,
                        false
                ));
                assertTrue(event.isConsumed());
                assertTrue(visibleTarget.isFocused());
                assertFalse(hiddenTarget.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that parent containers use child controls' exposed focus nodes as default focus targets.
    @Test
    void parentContainersReportNestedCompositeDefaultFocusNodes() {
        FxTestUtils.runOnFxThread(() -> {
            M3ButtonGroup buttonGroup = buttonGroup(new M3Button("Archive"), new M3Button("Share"));

            M3SearchView searchView = searchView("Search", new M3Button("Result"));

            M3Surface surface = surface(buttonGroup, searchView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(surface, 640.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                surface.applyCss();
                surface.layout();

                assertSame(
                        buttonGroup.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE),
                        surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );

                buttonGroup.setVisible(false);
                surface.applyCss();
                surface.layout();

                assertSame(
                        searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE),
                        surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that parent containers reveal child controls through their exposed focus nodes.
    @Test
    void parentContainersShowNestedCompositeAccessibleFocusTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button archive = new M3Button("Archive");
            M3Button share = new M3Button("Share");
            M3ButtonGroup buttonGroup = buttonGroup(archive, share);

            M3SearchView searchView = searchView("Search", new M3Button("Result"));

            M3Surface surface = surface(buttonGroup, searchView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(surface, 640.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                surface.applyCss();
                surface.layout();

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, buttonGroup);

                assertTrue(archive.isFocused());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);

                Node searchFocusNode = assertInstanceOf(
                        Node.class,
                        searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );
                assertTrue(searchFocusNode.isFocused());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, searchView);

                assertTrue(searchFocusNode.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that parent containers delegate indexed reveal requests into inactive nested search results.
    @Test
    void parentContainersShowNestedSearchResultByIndex() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button firstResult = new M3Button("First result");
            M3Button secondResult = new M3Button("Second result");
            M3SearchView searchView = searchView("Search", firstResult, secondResult);
            M3Surface surface = surface(searchView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(surface, 640.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                surface.applyCss();
                surface.layout();

                searchView.deactivate();
                surface.applyCss();
                surface.layout();

                assertFalse(searchView.isActive());
                assertFalse(firstResult.isFocused());
                assertFalse(secondResult.isFocused());

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);

                assertTrue(searchView.isActive());
                assertTrue(secondResult.isFocused());
                assertSame(secondResult, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(secondResult, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that parent containers delegate indexed reveal requests into hidden sheet owners.
    @Test
    void parentContainersShowNestedHiddenSheetItemByIndex() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button indexedSideContent = new M3Button("Side content");
            M3Button indexedSideAction = new M3Button("Side action");
            M3SideSheet indexedSideSheet = sideSheet("Details", indexedSideContent, indexedSideAction);
            M3Surface indexedSideSurface = surface(indexedSideSheet);

            M3Button directSideContent = new M3Button("Direct side content");
            M3Button directSideAction = new M3Button("Direct side action");
            M3SideSheet directSideSheet = sideSheet("Direct details", directSideContent, directSideAction);
            M3Surface directSideSurface = surface(directSideSheet);

            M3Button directBottomContent = new M3Button("Direct bottom content");
            M3Button directBottomAction = new M3Button("Direct bottom action");
            M3BottomSheet directBottomSheet =
                    bottomSheet("Direct queue", directBottomContent, directBottomAction);
            M3Surface directBottomSurface = surface(directBottomSheet);

            M3Button formRowContent = new M3Button("Form row content");
            M3Button formRowAction = new M3Button("Form row action");
            M3SideSheet formRowSheet = sideSheet("Form details", formRowContent, formRowAction);
            M3FormRow formRow = new M3FormRow("Form row", formRowSheet);

            M3Button bottomBarContent = new M3Button("Bottom bar content");
            M3Button bottomBarAction = new M3Button("Bottom bar action");
            M3SideSheet bottomBarSheet = sideSheet("Bottom bar details", bottomBarContent, bottomBarAction);
            M3BottomAppBar bottomAppBar = bottomAppBar(bottomBarSheet);

            M3Button topBarContent = new M3Button("Top bar content");
            M3Button topBarAction = new M3Button("Top bar action");
            M3SideSheet topBarSheet = sideSheet("Top bar details", topBarContent, topBarAction);
            M3TopAppBar topAppBar = topAppBar("Top bar", (Node) null, topBarSheet);

            M3Button searchBarContent = new M3Button("Search bar content");
            M3Button searchBarAction = new M3Button("Search bar action");
            M3SideSheet searchBarSheet = sideSheet("Search bar details", searchBarContent, searchBarAction);
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.getTrailingActions().add(searchBarSheet);

            Stage stage = new Stage();
            try {
                indexedSideSheet.hide();
                directSideSheet.hide();
                directBottomSheet.hide();
                formRowSheet.hide();
                bottomBarSheet.hide();
                topBarSheet.hide();
                searchBarSheet.hide();
                VBox root = new VBox(
                        indexedSideSurface,
                        directSideSurface,
                        directBottomSurface,
                        formRow,
                        bottomAppBar,
                        topAppBar,
                        searchBar
                );
                Scene scene = new Scene(root, 720.0, 720.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertFalse(indexedSideSheet.isShown());
                assertFalse(directSideSheet.isShown());
                assertFalse(directBottomSheet.isShown());
                assertFalse(formRowSheet.isShown());
                assertFalse(bottomBarSheet.isShown());
                assertFalse(topBarSheet.isShown());
                assertFalse(searchBarSheet.isShown());

                indexedSideSurface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);

                assertTrue(indexedSideSheet.isShown());
                assertTrue(indexedSideAction.isFocused());
                assertSame(indexedSideAction, indexedSideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(indexedSideAction, indexedSideSurface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                indexedSideSheet.hide();
                root.applyCss();
                root.layout();

                indexedSideSurface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, indexedSideAction);

                assertTrue(indexedSideSheet.isShown());
                assertTrue(indexedSideAction.isFocused());
                assertSame(indexedSideAction, indexedSideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(indexedSideAction, indexedSideSurface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                directSideSurface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, directSideAction);

                assertTrue(directSideSheet.isShown());
                assertTrue(directSideAction.isFocused());
                assertSame(directSideAction, directSideSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(directSideAction, directSideSurface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                directBottomSurface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, directBottomContent);

                assertTrue(directBottomSheet.isShown());
                assertTrue(directBottomContent.isFocused());
                assertSame(directBottomContent, directBottomSheet.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertSame(
                        directBottomContent,
                        directBottomSurface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );

                formRow.executeAccessibleAction(AccessibleAction.SHOW_ITEM, formRowAction);

                assertTrue(formRowSheet.isShown());
                assertTrue(formRowAction.isFocused());
                assertSame(formRowAction, formRow.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                bottomAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomBarAction);

                assertTrue(bottomBarSheet.isShown());
                assertTrue(bottomBarAction.isFocused());
                assertSame(bottomBarAction, bottomAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                topAppBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, topBarAction);

                assertTrue(topBarSheet.isShown());
                assertTrue(topBarAction.isFocused());
                assertSame(topBarAction, topAppBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, searchBarAction);

                assertTrue(searchBarSheet.isShown());
                assertTrue(searchBarAction.isFocused());
                assertSame(searchBarAction, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that composite controls actively notify when their accessible focus child changes.
    @Test
    void accessibleFocusNotifierReportsSceneFocusChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Surface surface = surface(first, second);
            M3Button outside = new M3Button("Outside");
            AtomicInteger notifications = new AtomicInteger();
            M3AccessibleFocusNotifier notifier = new M3AccessibleFocusNotifier(
                    surface,
                    () -> M3Accessible.currentFocusTarget(surface, surface.getContent()),
                    notifications::incrementAndGet
            );
            Stage stage = new Stage();
            try {
                notifier.start();
                VBox root = new VBox(8.0, surface, outside);
                Scene scene = new Scene(root, 320.0, 160.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                notifier.refresh();
                notifications.set(0);

                first.requestFocus();

                assertTrue(first.isFocused());
                assertEquals(1, notifications.get());

                first.requestFocus();

                assertEquals(1, notifications.get());

                second.requestFocus();

                assertTrue(second.isFocused());
                assertEquals(2, notifications.get());

                outside.requestFocus();

                assertTrue(outside.isFocused());
                assertEquals(3, notifications.get());

                notifier.stop();
                first.requestFocus();

                assertTrue(first.isFocused());
                assertEquals(3, notifications.get());
            } finally {
                notifier.stop();
                stage.close();
            }
        });
    }

    /// Verifies that accessible focus notifications can observe popup-hosted content for a separate owner.
    @Test
    void accessibleFocusNotifierReportsSeparateSceneFocusChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button owner = new M3Button("Owner");
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3Surface popupSurface = surface(first, second);
            AtomicInteger notifications = new AtomicInteger();
            M3AccessibleFocusNotifier notifier = new M3AccessibleFocusNotifier(
                    owner,
                    popupSurface,
                    () -> M3Accessible.currentFocusTarget(popupSurface, popupSurface.getContent()),
                    notifications::incrementAndGet
            );
            Stage stage = new Stage();
            javafx.stage.Popup popup = new javafx.stage.Popup();
            try {
                notifier.start();
                VBox root = new VBox(8.0, owner);
                Scene scene = new Scene(root, 320.0, 160.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                popup.getContent().add(popupSurface);
                popup.show(stage);
                root.applyCss();
                root.layout();
                popupSurface.applyCss();
                popupSurface.layout();
                notifier.refresh();
                notifications.set(0);

                first.requestFocus();

                assertTrue(first.isFocused());
                assertEquals(1, notifications.get());

                second.requestFocus();

                assertTrue(second.isFocused());
                assertEquals(2, notifications.get());

                notifier.stop();
                first.requestFocus();

                assertTrue(first.isFocused());
                assertEquals(2, notifications.get());

                notifier.start();
                notifier.refresh();
                second.requestFocus();

                assertTrue(second.isFocused());
                assertEquals(3, notifications.get());
            } finally {
                notifier.stop();
                popup.hide();
                stage.close();
            }
        });
    }
}
