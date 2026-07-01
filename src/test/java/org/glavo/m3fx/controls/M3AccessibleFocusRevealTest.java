// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for accessibility focus reveal and focus restoration behavior.
@NotNullByDefault
final class M3AccessibleFocusRevealTest {
    /// Starts the JavaFX toolkit for focus reveal tests.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes test windows after every focus reveal test.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies direct accessibility focus reports success only when focus moves and reveals the target.
    @Test
    void directAccessibleFocusReportsSuccessAndRevealsTarget() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3Button target = new M3Button("Target");
            VBox content = new VBox(spacer, target);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            target.layout();

            assertTrue(M3Accessible.showDirectItem(content, target));

            assertTrue(target.isFocused());
            assertTargetVisible(scrollPane, content, target);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies direct accessibility focus rejects a detached target instead of reporting a stale success.
    @Test
    void directAccessibleFocusRejectsDetachedTarget() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            M3Button target = new M3Button("Detached");

            assertFalse(M3Accessible.showDirectItem(owner, target));
            assertFalse(target.isFocused());
        });
    }

    /// Verifies generic accessibility focus requests report failure when the action does not move focus.
    @Test
    void genericAccessibleFocusRejectsNoOpRequestAction() {
        FxTestUtils.runOnFxThread(() -> {
            NonFocusingAccessibleNode target = new NonFocusingAccessibleNode();
            new Scene(target, 120.0, 80.0);
            target.applyCss();
            target.layout();

            assertFalse(M3Accessible.requestAccessibleFocus(target));
            assertFalse(target.isFocused());
        });
    }

    /// Verifies owner-aware accessibility focus requests do not reveal targets when focus does not move.
    @Test
    void ownerAccessibleFocusRejectsNoOpRequestActionWithoutReveal() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            NonFocusingAccessibleNode target = new NonFocusingAccessibleNode();
            target.setPrefSize(80.0, 32.0);
            VBox content = new VBox(spacer, target);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            target.layout();

            assertFalse(M3Accessible.requestAccessibleFocus(content, target));
            assertFalse(target.isFocused());
            assertTrue(scrollPane.getVvalue() <= 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a focus-owning control scrolls it into view.
    @Test
    void sliderAccessibleRequestFocusRevealsControl() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3Slider slider = new M3Slider();
            VBox content = new VBox(spacer, slider);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            slider.layout();

            assertTrue(M3Accessible.requestAccessibleFocus(content, slider));

            assertTrue(slider.isFocused());
            assertTargetVisible(scrollPane, content, slider);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a closed menu button scrolls its owner into view.
    @Test
    void menuButtonAccessibleRequestFocusRevealsClosedButton() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3MenuButton menuButton = new M3MenuButton("More", new M3MenuItem("Archive"));
            VBox content = new VBox(spacer, menuButton);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            menuButton.layout();

            menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

            assertTrue(menuButton.isFocused());
            assertTargetVisible(scrollPane, content, menuButton);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a closed submenu item scrolls its owner into view.
    @Test
    void subMenuItemAccessibleRequestFocusRevealsClosedItem() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
            VBox content = new VBox(spacer, subMenuItem);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            subMenuItem.layout();

            subMenuItem.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

            assertTrue(subMenuItem.isFocused());
            assertTargetVisible(scrollPane, content, subMenuItem);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on an empty focusable menu scrolls the menu surface into view.
    @Test
    void menuAccessibleRequestFocusRevealsFallbackSurface() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3Menu menu = new M3Menu();
            menu.setFocusTraversable(true);
            VBox content = new VBox(spacer, menu);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            menu.layout();

            menu.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

            assertTrue(menu.isFocused());
            assertTargetVisible(scrollPane, content, menu);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a carousel selected item scrolls the carousel owner into view.
    @Test
    void carouselAccessibleRequestFocusRevealsSelectedItem() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3Button selectedItem = new M3Button("Selected");
            M3Carousel carousel = new M3Carousel(selectedItem);
            carousel.setAnimatedScroll(false);
            carousel.setPrefSize(180.0, 80.0);
            carousel.select(selectedItem);
            VBox content = new VBox(spacer, carousel);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            carousel.layout();

            carousel.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

            assertTrue(selectedItem.isFocused());
            assertTargetVisible(scrollPane, content, selectedItem);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a date picker scrolls its active focus node into view.
    @Test
    void datePickerAccessibleRequestFocusRevealsActiveNode() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3DatePicker picker = new M3DatePicker();
            VBox content = new VBox(spacer, picker);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(240.0, 120.0);

            Scene scene = show(scrollPane, 240.0, 120.0);
            scrollPane.applyCss();
            scrollPane.resize(240.0, 120.0);
            scrollPane.layout();
            content.layout();
            picker.layout();

            picker.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

            Node focusOwner = scene.getFocusOwner();
            assertNotNull(focusOwner);
            assertTrue(M3Accessible.containsNode(picker, focusOwner));
            assertTargetVisible(scrollPane, content, focusOwner);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a time picker scrolls its active focus node into view.
    @Test
    void timePickerAccessibleRequestFocusRevealsActiveNode() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3TimePicker picker = new M3TimePicker();
            VBox content = new VBox(spacer, picker);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(240.0, 120.0);

            Scene scene = show(scrollPane, 240.0, 120.0);
            scrollPane.applyCss();
            scrollPane.resize(240.0, 120.0);
            scrollPane.layout();
            content.layout();
            picker.layout();

            picker.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

            Node focusOwner = scene.getFocusOwner();
            assertNotNull(focusOwner);
            assertTrue(M3Accessible.containsNode(picker, focusOwner));
            assertTargetVisible(scrollPane, content, focusOwner);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a text input layout scrolls its input into view.
    @Test
    void textInputLayoutAccessibleRequestFocusRevealsInput() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3TextField input = new M3TextField("Project");
            M3TextInputLayout layout = new M3TextInputLayout(input, "Project name");
            VBox content = new VBox(spacer, layout);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(240.0, 120.0);

            show(scrollPane, 240.0, 120.0);
            scrollPane.applyCss();
            scrollPane.resize(240.0, 120.0);
            scrollPane.layout();
            content.layout();
            layout.layout();

            assertTrue(M3Accessible.requestAccessibleFocus(content, layout));

            assertTrue(input.isFocused());
            assertTargetVisible(scrollPane, content, input);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a validation summary scrolls its current invalid input into view.
    @Test
    void validationSummaryAccessibleRequestFocusRevealsInvalidInput() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3TextField input = new M3TextField();
            M3TextInputLayout layout = new M3TextInputLayout(input, "Name", "Required");
            layout.setValidator(M3TextInputValidators.required("Name is required"));
            M3FormValidator validator = new M3FormValidator(layout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            assertFalse(validator.validate());
            VBox content = new VBox(spacer, layout, summary);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(260.0, 140.0);

            show(scrollPane, 260.0, 140.0);
            scrollPane.applyCss();
            scrollPane.resize(260.0, 140.0);
            scrollPane.layout();
            content.layout();
            layout.layout();
            summary.layout();

            assertTrue(M3Accessible.requestAccessibleFocus(content, summary));

            assertTrue(input.isFocused());
            assertTargetVisible(scrollPane, content, input);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies text input clear-button focus restoration scrolls the input back into view.
    @Test
    void textInputLayoutClearTextRestoresAndRevealsInputFocus() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3TextField input = new M3TextField("Search text");
            M3TextInputLayout layout = new M3TextInputLayout(input);
            layout.setClearButtonEnabled(true);
            VBox content = new VBox(spacer, layout);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(240.0, 120.0);

            show(scrollPane, 240.0, 120.0);
            scrollPane.applyCss();
            scrollPane.resize(240.0, 120.0);
            scrollPane.layout();
            content.layout();
            layout.layout();
            layout.getClearButton().requestFocus();

            layout.clearText();

            assertTrue(input.isFocused());
            assertTargetVisible(scrollPane, content, input);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies accessibility focus on a search bar scrolls its editor into view.
    @Test
    void searchBarAccessibleRequestFocusRevealsEditor() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3SearchBar searchBar = new M3SearchBar("Search");
            VBox content = new VBox(spacer, searchBar);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(240.0, 120.0);

            Scene scene = show(scrollPane, 240.0, 120.0);
            scrollPane.applyCss();
            scrollPane.resize(240.0, 120.0);
            scrollPane.layout();
            content.layout();
            searchBar.layout();

            searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

            Node focusOwner = scene.getFocusOwner();
            assertNotNull(focusOwner);
            assertTrue(M3Accessible.containsNode(searchBar, focusOwner));
            assertTargetVisible(scrollPane, content, focusOwner);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies external accessibility focus routes preserve a search bar's current child focus.
    @Test
    void searchBarRouteRequestFocusPreservesCurrentFocusNode() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3Button filter = new M3Button("Filter");
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setTrailingActions(filter);
            VBox content = new VBox(spacer, searchBar);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(260.0, 140.0);

            show(scrollPane, 260.0, 140.0);
            scrollPane.applyCss();
            scrollPane.resize(260.0, 140.0);
            scrollPane.layout();
            content.layout();
            searchBar.layout();

            assertTrue(M3Accessible.showAccessibleActionTarget(content, searchBar, filter));
            assertTrue(filter.isFocused());
            assertSame(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

            assertTrue(M3Accessible.requestAccessibleFocus(content, searchBar));

            assertTrue(filter.isFocused());
            assertSame(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            assertTargetVisible(scrollPane, content, filter);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }
    /// Verifies search view default show-item focus scrolls the embedded editor into view.
    @Test
    void searchViewDefaultShowItemRevealsEditor() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3SearchView searchView = new M3SearchView("Search");
            VBox content = new VBox(spacer, searchView);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(260.0, 140.0);

            Scene scene = show(scrollPane, 260.0, 140.0);
            scrollPane.applyCss();
            scrollPane.resize(260.0, 140.0);
            scrollPane.layout();
            content.layout();
            searchView.layout();

            searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

            Node focusOwner = scene.getFocusOwner();
            assertNotNull(focusOwner);
            assertTrue(M3Accessible.containsNode(searchView, focusOwner));
            assertTargetVisible(scrollPane, content, focusOwner);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies search view explicit result focus scrolls the result into view.
    @Test
    void searchViewShowItemRevealsResult() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3Button result = new M3Button("Result");
            M3SearchView searchView = new M3SearchView("Search", result);
            VBox content = new VBox(spacer, searchView);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(260.0, 140.0);

            show(scrollPane, 260.0, 140.0);
            scrollPane.applyCss();
            scrollPane.resize(260.0, 140.0);
            scrollPane.layout();
            content.layout();
            searchView.layout();

            searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);

            assertTrue(result.isFocused());
            assertTargetVisible(scrollPane, content, result);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies search view collapse restores focus to the search bar container and reveals it.
    @Test
    void searchViewCollapseRestoresAndRevealsSearchBarFocus() {
        FxTestUtils.runOnFxThread(() -> {
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            M3Button result = new M3Button("Result");
            M3SearchView searchView = new M3SearchView("Search", result);
            VBox content = new VBox(spacer, searchView);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(260.0, 140.0);

            Scene scene = show(scrollPane, 260.0, 140.0);
            scrollPane.applyCss();
            scrollPane.resize(260.0, 140.0);
            scrollPane.layout();
            content.layout();
            searchView.layout();

            searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
            assertTrue(result.isFocused());

            searchView.deactivate();

            Node focusOwner = scene.getFocusOwner();
            assertSame(searchView.getSearchBar(), focusOwner);
            assertNotSame(searchView.getEditor(), focusOwner);
            assertTargetVisible(scrollPane, content, searchView.getSearchBar());
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies owner-aware direct accessibility target reveal scrolls the focused descendant into view.
    @Test
    void ownerAccessibleActionTargetRevealScrollsFocusedDescendant() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = wideButton("First");
            M3Button second = wideButton("Second");
            M3Button third = wideButton("Third");
            M3Button fourth = wideButton("Fourth");
            M3Button fifth = wideButton("Fifth");
            M3Button sixth = wideButton("Sixth");
            HBox owner = new HBox(first, second, third, fourth, fifth, sixth);
            HBox content = new HBox(owner);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefSize(180.0, 80.0);

            show(scrollPane, 180.0, 80.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 80.0);
            scrollPane.layout();
            content.layout();
            owner.layout();

            assertTrue(M3Accessible.showAccessibleActionTarget(owner, owner, sixth));

            assertTrue(sixth.isFocused());
            assertTargetHorizontallyVisible(scrollPane, content, sixth);
            assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        });
    }

    /// Verifies owner-aware list accessibility target reveal scrolls the focused item into view.
    @Test
    void ownerAccessibleActionTargetRevealScrollsFocusedListItem() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = wideButton("First");
            M3Button second = wideButton("Second");
            M3Button third = wideButton("Third");
            M3Button fourth = wideButton("Fourth");
            M3Button fifth = wideButton("Fifth");
            M3Button sixth = wideButton("Sixth");
            ObservableList<Node> items = FXCollections.observableArrayList(first, second, third, fourth, fifth, sixth);
            HBox owner = new HBox();
            owner.getChildren().addAll(items);
            HBox content = new HBox(owner);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefSize(180.0, 80.0);

            show(scrollPane, 180.0, 80.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 80.0);
            scrollPane.layout();
            content.layout();
            owner.layout();

            assertTrue(M3Accessible.showAccessibleActionTarget(owner, items, sixth));

            assertTrue(sixth.isFocused());
            assertTargetHorizontallyVisible(scrollPane, content, sixth);
            assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        });
    }

    /// Verifies modal sheets restore focus to the exact node that owned focus before showing.
    @Test
    void modalSheetsRestoreExactOriginalFocusOwner() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = new M3Button("First");
            M3Button second = new M3Button("Second");
            M3ButtonGroup trigger = new M3ButtonGroup(first, second);
            trigger.setFocusTraversable(true);
            M3Button sideAction = new M3Button("Side action");
            M3Button bottomAction = new M3Button("Bottom action");
            M3SideSheet sideSheet = new M3SideSheet("Details", new Label("Side"), sideAction);
            M3BottomSheet bottomSheet = new M3BottomSheet("Queue", new Label("Bottom"), bottomAction);
            Stage stage = new Stage();
            try {
                sideSheet.setVariant(M3SheetVariant.MODAL);
                bottomSheet.setVariant(M3SheetVariant.MODAL);
                sideSheet.hide();
                bottomSheet.hide();

                Scene scene = new Scene(new VBox(trigger, sideSheet, bottomSheet), 480.0, 360.0);
                stage.setScene(scene);
                stage.show();
                scene.getRoot().applyCss();
                scene.getRoot().layout();

                trigger.requestFocus();
                assertSame(trigger, scene.getFocusOwner());
                sideSheet.show();
                sideAction.requestFocus();
                sideSheet.hide();

                assertSame(trigger, scene.getFocusOwner());
                assertNotSame(first, scene.getFocusOwner());

                trigger.requestFocus();
                assertSame(trigger, scene.getFocusOwner());
                bottomSheet.show();
                bottomAction.requestFocus();
                bottomSheet.hide();

                assertSame(trigger, scene.getFocusOwner());
                assertNotSame(first, scene.getFocusOwner());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies explicit accessibility item reveal for a leading slot followed by indexed items.
    @Test
    void leadingListAccessibleShowItemRevealsExplicitTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button leading = wideButton("Leading");
            M3Button first = wideButton("First");
            M3Button second = wideButton("Second");
            M3Button third = wideButton("Third");
            M3Button fourth = wideButton("Fourth");
            M3Button fifth = wideButton("Fifth");
            M3Button sixth = wideButton("Sixth");
            ObservableList<Node> items = FXCollections.observableArrayList(first, second, third, fourth, fifth, sixth);
            HBox owner = new HBox();
            owner.getChildren().add(leading);
            owner.getChildren().addAll(items);
            HBox content = new HBox(owner);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefSize(180.0, 80.0);

            show(scrollPane, 180.0, 80.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 80.0);
            scrollPane.layout();
            content.layout();
            owner.layout();

            assertTrue(M3Accessible.showCurrentOrItem(owner, leading, items, 6));

            assertTrue(sixth.isFocused());
            assertTargetHorizontallyVisible(scrollPane, content, sixth);
            assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        });
    }

    /// Verifies explicit accessibility item reveal for indexed items followed by a trailing slot.
    @Test
    void listTrailingAccessibleShowItemRevealsExplicitTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button first = wideButton("First");
            M3Button second = wideButton("Second");
            M3Button third = wideButton("Third");
            M3Button fourth = wideButton("Fourth");
            M3Button fifth = wideButton("Fifth");
            M3Button sixth = wideButton("Sixth");
            M3Button trailing = wideButton("Trailing");
            ObservableList<Node> items = FXCollections.observableArrayList(first, second, third, fourth, fifth, sixth);
            HBox owner = new HBox();
            owner.getChildren().addAll(items);
            owner.getChildren().add(trailing);
            HBox content = new HBox(owner);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefSize(180.0, 80.0);

            show(scrollPane, 180.0, 80.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 80.0);
            scrollPane.layout();
            content.layout();
            owner.layout();

            assertTrue(M3Accessible.showCurrentOrItem(owner, items, trailing, 6));

            assertTrue(trailing.isFocused());
            assertTargetHorizontallyVisible(scrollPane, content, trailing);
            assertTrue(scrollPane.getHvalue() > 0.0, () -> "hvalue=" + scrollPane.getHvalue());
        });
    }

    /// Verifies direct accessibility focus can target an indexed child outside the owner's parent-child tree.
    @Test
    void directAccessibleFocusUsesIndexedChildOutsideParentTree() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button indexedChild = new M3Button("Indexed");
            IndexedAccessibleOwner owner = new IndexedAccessibleOwner(indexedChild);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, indexedChild);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            indexedChild.layout();

            assertTrue(M3Accessible.showItem(content, owner));

            assertTrue(indexedChild.isFocused());
            assertFalse(owner.isFocused());
            assertTargetVisible(scrollPane, content, indexedChild);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies explicit accessibility reveal can target an indexed child outside the owner's parent-child tree.
    @Test
    void accessibleShowItemTargetsIndexedChildOutsideParentTree() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button indexedChild = new M3Button("Indexed");
            IndexedAccessibleOwner owner = new IndexedAccessibleOwner(indexedChild);
            owner.setFocusTraversable(true);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, indexedChild);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            indexedChild.layout();

            assertTrue(M3Accessible.showAccessibleActionTarget(content, owner, indexedChild));

            assertTrue(indexedChild.isFocused());
            assertFalse(owner.isFocused());
            assertTargetVisible(scrollPane, content, indexedChild);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies installed focus routes handle external accessibility focus before JavaFX action fallback.
    @Test
    void installedAccessibleFocusRouteHandlesExternalTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button routeTarget = new M3Button("Route target");
            RouteAccessibleOwner owner = new RouteAccessibleOwner(routeTarget);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, routeTarget);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            routeTarget.layout();

            assertTrue(M3Accessible.requestAccessibleFocus(content, owner));

            assertTrue(owner.focusRouteCalled);
            assertFalse(owner.fallbackFocusActionCalled);
            assertTrue(routeTarget.isFocused());
            assertTargetVisible(scrollPane, content, routeTarget);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies installed reveal routes handle indexed external targets before JavaFX action fallback.
    @Test
    void installedAccessibleRevealRouteHandlesIndexedExternalTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button routeTarget = new M3Button("Route target");
            RouteAccessibleOwner owner = new RouteAccessibleOwner(routeTarget);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, routeTarget);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            routeTarget.layout();

            assertTrue(M3Accessible.showAccessibleActionTarget(content, owner, routeTarget));

            assertTrue(owner.showRouteCalled);
            assertFalse(owner.fallbackShowActionCalled);
            assertTrue(routeTarget.isFocused());
            assertTargetVisible(scrollPane, content, routeTarget);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies installed reveal route target matchers handle non-node targets before JavaFX action fallback.
    @Test
    void installedAccessibleRevealRouteHandlesNonNodeTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button routeTarget = new M3Button("Route target");
            RouteAccessibleOwner owner = new RouteAccessibleOwner(routeTarget);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, routeTarget);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            routeTarget.layout();

            assertTrue(M3Accessible.showAccessibleActionTarget(content, owner, owner.valueTarget));

            assertTrue(owner.showTargetMatcherCalled);
            assertTrue(owner.showRouteCalled);
            assertFalse(owner.fallbackShowActionCalled);
            assertTrue(routeTarget.isFocused());
            assertTargetVisible(scrollPane, content, routeTarget);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies installed reveal routes handle nested non-node targets before JavaFX action fallback.
    @Test
    void installedAccessibleRevealRouteHandlesNestedNonNodeTarget() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button routeTarget = new M3Button("Route target");
            RouteAccessibleOwner owner = new RouteAccessibleOwner(routeTarget);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, routeTarget);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            routeTarget.layout();

            Object nestedTarget = new Object[] {List.of(owner.valueTarget)};
            assertTrue(M3Accessible.showAccessibleActionTarget(content, owner, nestedTarget));

            assertTrue(owner.showTargetMatcherCalled);
            assertTrue(owner.showRouteCalled);
            assertFalse(owner.fallbackShowActionCalled);
            assertTrue(routeTarget.isFocused());
            assertTargetVisible(scrollPane, content, routeTarget);
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies removing an installed accessibility route restores JavaFX action fallback dispatch.
    @Test
    void installedAccessibleActionRouteCanBeCleared() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button routeTarget = new M3Button("Route target");
            RouteAccessibleOwner owner = new RouteAccessibleOwner(routeTarget);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, routeTarget);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            routeTarget.layout();

            M3Accessible.installAccessibleActionRoute(owner, null, null, null);

            assertFalse(M3Accessible.showAccessibleActionTarget(content, owner, owner.valueTarget));
            assertFalse(owner.showTargetMatcherCalled);
            assertFalse(owner.showRouteCalled);
            assertFalse(owner.fallbackShowActionCalled);
            assertFalse(routeTarget.isFocused());

            assertFalse(M3Accessible.requestAccessibleFocus(content, owner));
            assertFalse(owner.focusRouteCalled);
            assertTrue(owner.fallbackFocusActionCalled);
            assertFalse(routeTarget.isFocused());
        });
    }

    /// Verifies reinstalling an accessibility route replaces the previous handlers atomically.
    @Test
    void installedAccessibleActionRouteCanBeReplaced() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button routeTarget = new M3Button("Route target");
            RouteAccessibleOwner owner = new RouteAccessibleOwner(routeTarget);
            owner.setPrefSize(120.0, 32.0);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(owner, spacer, routeTarget);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(180.0, 96.0);
            boolean[] replacementFocusRouteCalled = {false};
            boolean[] replacementShowRouteCalled = {false};
            boolean[] replacementShowTargetMatcherCalled = {false};

            show(scrollPane, 180.0, 96.0);
            scrollPane.applyCss();
            scrollPane.resize(180.0, 96.0);
            scrollPane.layout();
            content.layout();
            owner.layout();
            routeTarget.layout();

            M3Accessible.installAccessibleActionRoute(owner,
                    () -> {
                        replacementFocusRouteCalled[0] = true;
                        return false;
                    },
                    parameters -> {
                        replacementShowRouteCalled[0] = true;
                        return false;
                    },
                    parameter -> {
                        replacementShowTargetMatcherCalled[0] = true;
                        return parameter == owner.valueTarget;
                    });

            assertFalse(M3Accessible.requestAccessibleFocus(content, owner));
            assertTrue(replacementFocusRouteCalled[0]);
            assertFalse(owner.focusRouteCalled);
            assertFalse(owner.fallbackFocusActionCalled);
            assertFalse(routeTarget.isFocused());

            assertFalse(M3Accessible.showAccessibleActionTarget(content, owner, owner.valueTarget));
            assertTrue(replacementShowTargetMatcherCalled[0]);
            assertTrue(replacementShowRouteCalled[0]);
            assertFalse(owner.showTargetMatcherCalled);
            assertFalse(owner.showRouteCalled);
            assertFalse(owner.fallbackShowActionCalled);
            assertFalse(routeTarget.isFocused());
        });
    }

    /// Verifies accessibility focus helpers report failure when no reachable target can receive focus.
    @Test
    void accessibleShowCurrentOrItemReturnsFalseWithoutReachableTarget() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            ObservableList<Node> items = FXCollections.observableArrayList(new M3Button("Detached"));

            assertFalse(M3Accessible.showCurrentOrItem(owner, items));
            assertFalse(M3Accessible.showCurrentOrItem(owner, items, 1));
        });
    }

    /// Shows the supplied scroll pane in a real JavaFX window and performs an initial layout pass.
    private static Scene show(ScrollPane scrollPane, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(scrollPane, width, height);
        stage.setScene(scene);
        stage.show();
        scrollPane.applyCss();
        scrollPane.layout();
        return scene;
    }

    /// Verifies that the target node is visible within the current vertical viewport.
    private static void assertTargetVisible(ScrollPane scrollPane, VBox content, Node target) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollableHeight = content.getBoundsInLocal().getHeight() - viewportHeight;
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        double fraction = (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange;
        double visibleTop = Math.max(0.0, Math.min(1.0, fraction)) * scrollableHeight;
        double visibleBottom = visibleTop + viewportHeight;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(targetBounds.getMinY() >= visibleTop - 0.5, () -> "targetTop=" + targetBounds.getMinY());
        assertTrue(targetBounds.getMaxY() <= visibleBottom + 0.5, () -> "targetBottom=" + targetBounds.getMaxY());
    }

    /// Verifies that the target node is visible within the current horizontal viewport.
    private static void assertTargetHorizontallyVisible(ScrollPane scrollPane, Node content, Node target) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double scrollableWidth = content.getBoundsInLocal().getWidth() - viewportWidth;
        double valueRange = scrollPane.getHmax() - scrollPane.getHmin();
        double fraction = (scrollPane.getHvalue() - scrollPane.getHmin()) / valueRange;
        double visibleLeft = Math.max(0.0, Math.min(1.0, fraction)) * scrollableWidth;
        double visibleRight = visibleLeft + viewportWidth;
        Bounds targetBounds = content.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        assertTrue(targetBounds.getMinX() >= visibleLeft - 0.5, () -> "targetLeft=" + targetBounds.getMinX());
        assertTrue(targetBounds.getMaxX() <= visibleRight + 0.5, () -> "targetRight=" + targetBounds.getMaxX());
    }

    /// Creates a wide test button for horizontal viewport reveal checks.
    private static M3Button wideButton(String text) {
        M3Button button = new M3Button(text);
        button.setMinWidth(120.0);
        button.setPrefWidth(120.0);
        button.setMaxWidth(120.0);
        return button;
    }

    /// Test node that exposes one indexed accessibility child without owning it as a scene-graph child.
    @NotNullByDefault
    private static final class IndexedAccessibleOwner extends Pane {
        /// The indexed child returned from accessibility queries.
        private final Node indexedChild;

        /// Creates an owner for one indexed child.
        private IndexedAccessibleOwner(Node indexedChild) {
            this.indexedChild = indexedChild;
        }

        /// Returns the indexed child accessibility structure.
        @Override
        public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
            return switch (attribute) {
                case ITEM_COUNT -> 1;
                case ITEM_AT_INDEX -> parameters.length > 0
                        && parameters[0] instanceof Number number
                        && number.intValue() == 0 ? indexedChild : null;
                default -> super.queryAccessibleAttribute(attribute, parameters);
            };
        }
    }

    /// Test node that exposes installed accessibility action routes for an external indexed child.
    @NotNullByDefault
    private static final class RouteAccessibleOwner extends Pane {
        /// The indexed child reached through the installed route.
        private final Node routeTarget;

        /// The non-node value target exposed through the installed route matcher.
        private final Object valueTarget = new Object();

        /// Whether the installed focus route was called.
        private boolean focusRouteCalled;

        /// Whether the installed reveal route was called.
        private boolean showRouteCalled;

        /// Whether JavaFX fallback focus action dispatch was used.
        private boolean fallbackFocusActionCalled;

        /// Whether JavaFX fallback reveal action dispatch was used.
        private boolean fallbackShowActionCalled;

        /// Whether the installed non-node target matcher was called.
        private boolean showTargetMatcherCalled;

        /// Creates an owner for one external route target.
        private RouteAccessibleOwner(Node routeTarget) {
            this.routeTarget = routeTarget;
            M3Accessible.installAccessibleActionRoute(this,
                    this::focusRouteTarget,
                    this::showRouteTarget,
                    this::handlesShowTarget);
        }

        /// Focuses the external route target.
        private boolean focusRouteTarget() {
            focusRouteCalled = true;
            return M3Accessible.showDirectItem(this, routeTarget);
        }

        /// Reveals the external route target.
        private boolean showRouteTarget(Object... parameters) {
            showRouteCalled = true;
            for (Object parameter : parameters) {
                if (containsRouteTarget(parameter)) {
                    return M3Accessible.showDirectItem(this, routeTarget);
                }
            }
            return false;
        }

        /// Returns whether this route owns the supplied non-node target.
        private boolean handlesShowTarget(@Nullable Object parameter) {
            showTargetMatcherCalled = true;
            return containsRouteTarget(parameter);
        }

        /// Returns whether one reveal parameter references this route target.
        private boolean containsRouteTarget(@Nullable Object parameter) {
            if (parameter == routeTarget || parameter == valueTarget) {
                return true;
            }
            if (parameter instanceof Iterable<?> values) {
                for (Object value : values) {
                    if (containsRouteTarget(value)) {
                        return true;
                    }
                }
                return false;
            }
            if (parameter instanceof Object[] values) {
                for (Object value : values) {
                    if (containsRouteTarget(value)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /// Returns the external route target through indexed accessibility queries.
        @Override
        public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
            return switch (attribute) {
                case ITEM_COUNT -> 1;
                case ITEM_AT_INDEX -> parameters.length > 0
                        && parameters[0] instanceof Number number
                        && number.intValue() == 0 ? routeTarget : null;
                default -> super.queryAccessibleAttribute(attribute, parameters);
            };
        }

        /// Records JavaFX accessibility fallback action dispatch.
        @Override
        public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
            switch (action) {
                case REQUEST_FOCUS -> fallbackFocusActionCalled = true;
                case SHOW_ITEM -> fallbackShowActionCalled = true;
                default -> super.executeAccessibleAction(action, parameters);
            }
        }
    }

    /// Test node that exposes focus traversal but ignores accessibility focus actions.
    @NotNullByDefault
    private static final class NonFocusingAccessibleNode extends Pane {
        /// Creates a reachable node whose accessibility focus action is intentionally inert.
        private NonFocusingAccessibleNode() {
            setFocusTraversable(true);
        }

        /// Ignores accessibility focus requests while preserving default handling for other actions.
        @Override
        public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
            if (action == AccessibleAction.REQUEST_FOCUS) {
                return;
            }
            super.executeAccessibleAction(action, parameters);
        }
    }
}
