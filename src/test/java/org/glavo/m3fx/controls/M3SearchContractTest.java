// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.skins.M3SearchBarSkin;
import org.glavo.m3fx.skins.M3SearchViewSkin;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies search editing, result navigation, and expanded presentation.
@NotNullByDefault
final class M3SearchContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that search bars delegate text and action APIs to their embedded editor.
    @Test
    void searchBarDelegatesTextAndActions() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        Label clear = new Label("Clear");
        Label filter = new Label("Filter");
        AtomicInteger actions = new AtomicInteger();
        searchBar.setOnAction(event -> actions.incrementAndGet());

        searchBar.setText("M3FX");
        searchBar.getTrailingActions().add(clear);
        searchBar.getTrailingActions().setAll(filter);
        searchBar.fire();

        assertEquals("M3FX", searchBar.getText());
        assertEquals("M3FX", searchBarEditor(searchBar).getText());
        assertEquals("Search", searchBar.getPromptText());
        assertEquals(java.util.List.of(filter), searchBar.getTrailingActions());
        assertEquals(1, actions.get());

        searchBar.getTrailingActions().clear();

        assertTrue(searchBar.getTrailingActions().isEmpty());
    }

    /// Verifies that search bars expose active state and clear actions.
    @Test
    void searchBarTracksActiveStateAndClearsText() {
        M3SearchBar searchBar = new M3SearchBar("Search");

        searchBar.setText("M3FX");
        searchBar.activate();

        assertTrue(searchBar.isActive());
        assertTrue(searchBar.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")));

        searchBar.clear();
        searchBar.deactivate();

        assertEquals("", searchBar.getText());
        assertFalse(searchBar.isActive());
        assertFalse(searchBar.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")));
    }

    /// Verifies bounded pointer feedback and delegated editor focus feedback on the complete search container.
    @Tier2Test
    @Test
    void searchBarRendersRippleStateLayerAndEditorFocusIndicator() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        M3IconButton action = createIconButton("C");
        searchBar.getTrailingActions().add(action);
        Pane root = new Pane(searchBar);
        Scene scene = new Scene(root, 420.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        searchBar.resize(360.0, 56.0);
        searchBar.layout();

        Region stateLayerContainer = searchBar.lookupAll(".m3-state-layer-container").stream()
                .filter(Region.class::isInstance)
                .map(Region.class::cast)
                .filter(region -> region.getWidth() > 100.0)
                .findFirst()
                .orElseThrow();
        Region overlay = lookupRegion(stateLayerContainer, ".m3-state-layer");
        assertEquals(360.0, stateLayerContainer.getWidth(), 0.0001);
        assertEquals(56.0, stateLayerContainer.getHeight(), 0.0001);

        assertNull(stateLayerContainer.lookup(".m3-ripple"));
        assertNull(stateLayerContainer.lookup(".m3-focus-indicator"));
        action.fireEvent(primaryMouseEvent(action, MouseEvent.MOUSE_PRESSED,
                action.getWidth() / 2.0, action.getHeight() / 2.0, true));
        assertNull(stateLayerContainer.lookup(".m3-ripple"),
                "trailing action presses must not create a search-container ripple");
        action.fireEvent(primaryMouseEvent(action, MouseEvent.MOUSE_RELEASED,
                action.getWidth() / 2.0, action.getHeight() / 2.0, false));

        searchBar.deactivate();
        action.fireEvent(primaryMouseEvent(action, MouseEvent.MOUSE_CLICKED,
                action.getWidth() / 2.0, action.getHeight() / 2.0, false));
        assertFalse(searchBar.isActive(), "trailing action clicks must not reactivate the search container");

        searchBar.fireEvent(primaryMouseEvent(searchBar, MouseEvent.MOUSE_PRESSED, 180.0, 28.0, true));
        Region ripple = lookupRegion(stateLayerContainer, ".m3-ripple");
        assertTrue(ripple.getOpacity() > 0.0, "search-bar ripple should be visible for the held press");

        searchBar.fireEvent(primaryMouseEvent(searchBar, MouseEvent.MOUSE_RELEASED, 180.0, 28.0, false));
        assertTrue(ripple.getOpacity() > 0.0, "released search-bar ripple should fade instead of disappearing");

        FxTestUtils.setCssAnimationsEnabled(root, false);
        assertEquals(0.0, overlay.getOpacity(), 0.0001,
                "persistent interaction overlays should not stack over a fading ripple");
        searchBar.setDisable(true);
        searchBar.setDisable(false);
        assertEquals(0.0, ripple.getOpacity(), 0.0001,
                "disabling the search bar should clear transient ripple feedback");
        searchBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        assertEquals(0.08, overlay.getOpacity(), 0.0001);

        searchBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
        TextField editor = searchBarEditor(searchBar);
        editor.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        Region focusIndicator = lookupRegion(stateLayerContainer, ".m3-focus-indicator");
        assertEquals(0.10, overlay.getOpacity(), 0.0001);
        assertEquals(1.0, focusIndicator.getOpacity(), 0.0001);

        editor.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), false);
        assertEquals(0.0, overlay.getOpacity(), 0.0001);
        assertEquals(0.0, focusIndicator.getOpacity(), 0.0001);
        FxTestUtils.setCssAnimationsEnabled(root, true);
    }

    /// Verifies that search bars leave active input state from the Escape key.
    @Test
    void searchBarHandlesEscapeAndClearDeactivate() {
        M3SearchBar searchBar = new M3SearchBar("Search");

        searchBar.setText("M3FX");
        searchBar.activate();
        searchBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

        assertEquals("M3FX", searchBar.getText());
        assertFalse(searchBar.isActive());

        searchBar.activate();
        searchBar.clearAndDeactivate();

        assertEquals("", searchBar.getText());
        assertFalse(searchBar.isActive());
    }

    /// Verifies that search bars expose their slots as indexed accessibility items.
    @Test
    void searchBarExposesIndexedAccessibleSlots() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            Label leading = new Label("S");
            M3Button clear = new M3Button("Clear");
            M3Button filter = new M3Button("Filter");
            searchBar.setLeading(leading);
            searchBar.getTrailingActions().setAll(clear, filter);

            Pane root = new Pane(searchBar);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 120.0));
                stage.show();
                root.applyCss();

                assertEquals(searchBarEditor(searchBar), searchBar.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
                assertEquals(4, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertEquals(leading, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(searchBarEditor(searchBar), searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertEquals(clear, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
                assertEquals(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 3));
                assertNull(searchBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 4));

                searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(searchBar.isActive());
                assertTrue(searchBarEditor(searchBar).isFocused());
                assertSame(searchBarEditor(searchBar), searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);

                assertTrue(searchBarEditor(searchBar).isFocused());
                assertSame(searchBarEditor(searchBar), searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, filter);

                assertTrue(filter.isFocused());
                assertSame(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(filter.isFocused());

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(filter.isFocused());
                assertSame(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.COLLAPSE);
                assertFalse(searchBar.isActive());
                searchBar.executeAccessibleAction(AccessibleAction.EXPAND);
                assertTrue(searchBar.isActive());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search bar slots reflow after runtime orientation changes.
    @Test
    void searchBarReflowsSlotsAfterRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            M3Button leading = new M3Button("Menu");
            M3Button clear = new M3Button("Clear");
            M3Button filter = new M3Button("Filter");
            searchBar.setLeading(leading);
            searchBar.getTrailingActions().setAll(clear, filter);
            searchBar.setPrefWidth(360.0);

            StackPane root = new StackPane(searchBar);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 120.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(430.0, 120.0);
                root.layout();
                searchBar.layout();

                assertSearchBarUsesLogicalGeometry(searchBar, false);

                filter.requestFocus();
                assertTrue(filter.isFocused());

                searchBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                searchBar.layout();

                assertSearchBarUsesLogicalGeometry(searchBar, true);
                assertTrue(filter.isFocused());
                assertSame(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.applyCss();
                root.layout();
                searchBar.layout();

                assertSearchBarUsesLogicalGeometry(searchBar, false);
                assertTrue(filter.isFocused());
                assertSame(filter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that active search views propagate runtime orientation changes into search bar and results.
    @Test
    void searchViewReflowsSearchBarAndResultsAfterRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button leading = new M3Button("Menu");
            M3Button filter = new M3Button("Filter");
            M3ListItem firstResult = new M3ListItem("First result");
            M3ListItem secondResult = new M3ListItem("Second result");
            secondResult.setTrailing(new M3Button("Open"));
            M3SearchView searchView = searchView("Search", firstResult, secondResult);
            searchView.setLeading(leading);
            searchView.getTrailingActions().setAll(filter);
            searchView.setPrefWidth(380.0);
            searchView.activate();

            StackPane root = new StackPane(searchView);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 460.0, 260.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(460.0, 260.0);
                root.layout();
                searchView.layout();

                assertSearchViewUsesLogicalGeometry(searchView, false);

                secondResult.requestFocus();
                assertTrue(secondResult.isFocused());

                searchView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                searchView.layout();
                secondResult.layout();

                assertSearchViewUsesLogicalGeometry(searchView, true);
                assertTrue(secondResult.isFocused());
                assertSame(secondResult, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.applyCss();
                root.layout();
                searchView.layout();
                secondResult.layout();

                assertSearchViewUsesLogicalGeometry(searchView, false);
                assertTrue(secondResult.isFocused());
                assertSame(secondResult, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search controls route direct child focus and restore focus when results collapse.
    @Test
    void searchControlsRouteDirectChildFocusAndRestoreCollapseFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            M3Button barFilter = new M3Button("Filter");
            searchBar.getTrailingActions().setAll(barFilter);

            M3Button viewFilter = new M3Button("Filter");
            M3ListItem result = new M3ListItem("Result");
            M3SearchView searchView = searchView("Search", result);
            searchView.getTrailingActions().setAll(viewFilter);

            Pane root = new Pane(searchBar, searchView);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 260.0));
                stage.show();
                root.applyCss();
                root.layout();

                barFilter.requestFocus();
                assertTrue(barFilter.isFocused());
                assertSame(barFilter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(barFilter.isFocused());

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(barFilter.isFocused());
                assertSame(barFilter, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchBar.requestFocus();
                assertTrue(searchBar.isFocused());
                assertSame(searchBar, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                viewFilter.requestFocus();
                assertTrue(viewFilter.isFocused());
                assertSame(viewFilter, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(viewFilter.isFocused());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(viewFilter.isFocused());
                assertSame(viewFilter, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                result.requestFocus();
                assertTrue(result.isFocused());
                assertSame(result, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(result.isFocused());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(result.isFocused());
                assertSame(result, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.COLLAPSE);
                assertFalse(searchView.isActive());
                assertTrue(searchViewSearchBar(searchView).isFocused());
                assertSame(
                        searchViewSearchBar(searchView),
                        searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search controls route focus to descendant targets inside slots and result rows.
    @Test
    void searchControlsRouteDescendantFocusTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            M3Button barPrimary = new M3Button("Primary");
            M3Button barSecondary = new M3Button("Secondary");
            HBox trailingGroup = new HBox(barPrimary, barSecondary);
            searchBar.getTrailingActions().setAll(trailingGroup);

            M3Button resultPrimary = new M3Button("Open");
            M3Button resultSecondary = new M3Button("Preview");
            HBox resultRow = new HBox(resultPrimary, resultSecondary);
            M3SearchView searchView = searchView("Search", resultRow);

            Pane root = new Pane(searchBar, searchView);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 560.0, 260.0));
                stage.show();
                root.applyCss();
                root.layout();

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, barSecondary);

                assertTrue(barSecondary.isFocused());
                assertSame(barSecondary, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                barPrimary.requestFocus();

                assertTrue(barPrimary.isFocused());
                assertSame(barPrimary, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, resultSecondary);

                assertTrue(searchView.isActive());
                assertTrue(resultSecondary.isFocused());
                assertSame(resultSecondary, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                resultPrimary.requestFocus();

                assertTrue(resultPrimary.isFocused());
                assertSame(resultPrimary, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search bars route explicit reveal actions into popup-backed leading and trailing actions.
    @Test
    void searchBarRoutesAccessibleFocusThroughPopupActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem leadingTarget = new M3MenuItem("Leading target");
            M3SubMenuItem leadingSubMenu = new M3SubMenuItem("Leading submenu", leadingTarget);
            M3MenuButton leadingMenuButton = new M3MenuButton("Leading menu", leadingSubMenu);

            M3MenuItem trailingTarget = new M3MenuItem("Trailing target");
            M3SubMenuItem trailingSubMenu = new M3SubMenuItem("Trailing submenu", trailingTarget);
            M3MenuButton trailingMenuButton = new M3MenuButton("Trailing menu", trailingSubMenu);

            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setLeading(leadingMenuButton);
            searchBar.getTrailingActions().setAll(trailingMenuButton);
            leadingMenuButton.setAnimationsEnabled(false);
            leadingSubMenu.setAnimationsEnabled(false);
            trailingMenuButton.setAnimationsEnabled(false);
            trailingSubMenu.setAnimationsEnabled(false);

            Pane root = new Pane(searchBar);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 640.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, leadingTarget);
                assertTrue(searchBar.isActive());
                assertNestedPopupAccessibleFocus(searchBar, leadingMenuButton, leadingSubMenu, leadingTarget);

                searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(leadingTarget.isFocused());
                assertSame(leadingTarget, searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                leadingSubMenu.hideSubMenu();
                leadingMenuButton.hideMenu();

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, List.of(trailingTarget));
                assertNestedPopupAccessibleFocus(searchBar, trailingMenuButton, trailingSubMenu, trailingTarget);
            } finally {
                trailingSubMenu.hideSubMenu();
                trailingMenuButton.hideMenu();
                leadingSubMenu.hideSubMenu();
                leadingMenuButton.hideMenu();
                trailingSubMenu.setAnimationsEnabled(true);
                trailingMenuButton.setAnimationsEnabled(true);
                leadingSubMenu.setAnimationsEnabled(true);
                leadingMenuButton.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that hidden search bar owners do not route accessibility focus or active state into children.
    @Test
    void searchBarSkipsAccessibleFocusWhenOwnerHiddenByAncestor() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            M3Button filter = new M3Button("Filter");
            searchBar.getTrailingActions().setAll(filter);

            M3Button outside = new M3Button("Outside");
            Pane hiddenOwner = new Pane(searchBar);
            VBox root = new VBox(8.0, outside, hiddenOwner);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 460.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                hiddenOwner.setVisible(false);
                root.applyCss();
                root.layout();

                searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(outside.isFocused());
                assertFalse(searchBar.isActive());
                assertFalse(searchBarEditor(searchBar).isFocused());
                assertFalse(filter.isFocused());

                searchBar.executeAccessibleAction(AccessibleAction.SHOW_ITEM, filter);

                assertTrue(outside.isFocused());
                assertFalse(searchBar.isActive());
                assertFalse(filter.isFocused());

                searchBar.executeAccessibleAction(AccessibleAction.EXPAND);

                assertTrue(outside.isFocused());
                assertFalse(searchBar.isActive());
                assertSame(searchBarEditor(searchBar), searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search component token metrics apply through the active theme.
    @Test
    void searchComponentsApplyTokenMetrics() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        M3SearchView searchView = searchView("Search");
        M3ListItem result = new M3ListItem("Result");
        searchView.getResults().add(result);
        Pane root = new Pane(searchBar, searchView);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(56.0, searchBar.prefHeight(-1.0), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getRight(), 0.0001);
        assertInstanceOf(M3SearchBarSkin.class, searchBar.getSkin());
        assertInstanceOf(M3SearchViewSkin.class, searchView.getSkin());
        assertEquals(16.0, ((HBox) lookupRegion(searchBar, "." + "m3-search-bar-content")).getSpacing(), 0.0001);
        assertEquals(2.0, ((VBox) lookupRegion(searchView, "." + "m3-search-view-content")).getSpacing(), 0.0001);
        assertEquals(12.0, searchView.getPadding().getLeft(), 0.0001);
        assertEquals(4.0, searchView.getPadding().getBottom(), 0.0001);
        M3SearchBar embeddedSearchBar = searchViewSearchBar(searchView);
        assertEquals(4.0, embeddedSearchBar.getPadding().getLeft(), 0.0001);
        assertEquals(4.0,
                ((HBox) lookupRegion(embeddedSearchBar, "." + "m3-search-bar-content")).getSpacing(),
                0.0001);
        assertEquals(56.0, result.getOneLineHeight(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(56.0, searchBar.prefHeight(-1.0), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getRight(), 0.0001);
        assertEquals(16.0, ((HBox) lookupRegion(searchBar, "." + "m3-search-bar-content")).getSpacing(), 0.0001);
        assertEquals(12.0, searchView.getPadding().getLeft(), 0.0001);
        assertEquals(12.0, searchView.getPadding().getRight(), 0.0001);
        assertEquals(2.0, ((VBox) lookupRegion(searchView, "." + "m3-search-view-content")).getSpacing(), 0.0001);
        assertEquals(16.0, searchViewResultsContainer(searchView).getBackground()
                .getFills().get(0).getRadii().getTopLeftHorizontalRadius(), 0.0001);
        assertEquals(64.0, result.getOneLineHeight(), 0.0001);
        assertEquals(20.0, result.getHorizontalPadding(), 0.0001);
        assertEquals(20.0, result.getContentSpacing(), 0.0001);
        assertEquals(4.0, searchView.getPadding().getBottom(), 0.0001);
    }

    /// Verifies the four Material search-view style and layout combinations without overriding result-row tokens.
    @Test
    void searchViewAppliesContainedDividedDockedAndFullScreenGeometry() {
        M3ListItem result = new M3ListItem("Result");
        M3SearchView searchView = searchView("Search", result);
        Pane root = new Pane(searchView);
        Scene scene = new Scene(root, 800.0, 520.0);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        M3SearchBar searchBar = searchViewSearchBar(searchView);
        Region divider = lookupRegion(searchView, "." + "m3-search-view-divider");
        assertEquals(M3SearchViewStyle.CONTAINED, searchView.getViewStyle());
        assertEquals(M3SearchViewLayout.DOCKED, searchView.getViewLayout());
        assertTrue(searchView.getPseudoClassStates().contains(PseudoClass.getPseudoClass("contained")));
        assertTrue(searchView.getPseudoClassStates().contains(PseudoClass.getPseudoClass("docked")));
        assertEquals(360.0, searchView.getMinWidth(), 0.0001);
        assertEquals(720.0, searchView.getMaxWidth(), 0.0001);
        assertEquals(240.0, searchView.getMinHeight(), 0.0001);
        assertEquals(12.0, searchView.getPadding().getLeft(), 0.0001);
        assertEquals(4.0, searchView.getPadding().getBottom(), 0.0001);
        assertEquals(4.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(4.0,
                ((HBox) lookupRegion(searchBar, "." + "m3-search-bar-content")).getSpacing(),
                0.0001);
        assertFalse(divider.isManaged());

        searchView.setViewStyle(M3SearchViewStyle.DIVIDED);
        root.applyCss();

        assertTrue(searchView.getPseudoClassStates().contains(PseudoClass.getPseudoClass("divided")));
        assertFalse(searchView.getPseudoClassStates().contains(PseudoClass.getPseudoClass("contained")));
        assertEquals(0.0, searchView.getPadding().getLeft(), 0.0001);
        assertEquals(0.0, ((VBox) lookupRegion(searchView, "." + "m3-search-view-content")).getSpacing(), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(16.0,
                ((HBox) lookupRegion(searchBar, "." + "m3-search-bar-content")).getSpacing(),
                0.0001);
        assertTrue(divider.isVisible());
        assertTrue(divider.isManaged());

        searchView.setViewLayout(M3SearchViewLayout.FULL_SCREEN);
        root.applyCss();

        assertTrue(searchView.getPseudoClassStates().contains(PseudoClass.getPseudoClass("full-screen")));
        assertFalse(searchView.getPseudoClassStates().contains(PseudoClass.getPseudoClass("docked")));
        assertEquals(Region.USE_COMPUTED_SIZE, searchView.getMinWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, searchView.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, searchView.getMinHeight(), 0.0001);
        assertEquals(72.0, searchBar.prefHeight(-1.0), 0.0001);
        assertEquals(0.0, searchView.getBackground().getFills().get(0)
                .getRadii().getTopLeftHorizontalRadius(), 0.0001);

        searchView.setViewStyle(M3SearchViewStyle.CONTAINED);
        root.applyCss();

        assertEquals(56.0, searchBar.prefHeight(-1.0), 0.0001);
        assertEquals(12.0, searchView.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, searchView.getPadding().getBottom(), 0.0001);
        assertFalse(divider.isManaged());
        assertEquals(64.0, result.getOneLineHeight(), 0.0001);
        assertEquals(20.0, result.getHorizontalPadding(), 0.0001);
        assertEquals(20.0, result.getContentSpacing(), 0.0001);
    }

    /// Verifies that search bar theme metric updates preserve application-bound layout properties.
    @Test
    void searchBarMetricTokensPreserveBoundLayoutProperties() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        Pane root = new Pane(searchBar);
        Scene scene = new Scene(root);

        assertBoundRegionMetricsPreserved(searchBar, () -> {
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            assertEquals(16.0,
                    ((HBox) lookupRegion(searchBar, "." + "m3-search-bar-content")).getSpacing(),
                    0.0001);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
        }, () -> assertEquals(16.0,
                ((HBox) lookupRegion(searchBar, "." + "m3-search-bar-content")).getSpacing(),
                0.0001));
    }

    /// Verifies that search view theme metric updates preserve application-bound layout properties.
    @Test
    void searchViewMetricTokensPreserveBoundLayoutProperties() {
        M3ListItem result = new M3ListItem("Result");
        M3SearchView searchView = searchView("Search", result);
        Pane root = new Pane(searchView);
        Scene scene = new Scene(root);

        assertBoundRegionMetricsPreserved(searchView, () -> {
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            assertEquals(2.0,
                    ((VBox) lookupRegion(searchView, "." + "m3-search-view-content")).getSpacing(),
                    0.0001);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
        }, () -> {
            assertEquals(2.0,
                    ((VBox) lookupRegion(searchView, "." + "m3-search-view-content")).getSpacing(),
                    0.0001);
            assertEquals(16.0, searchViewResultsContainer(searchView).getBackground()
                    .getFills().get(0).getRadii().getTopLeftHorizontalRadius(), 0.0001);
        });
    }

    /// Verifies that search views propagate right-to-left orientation to their embedded content.
    @Test
    void searchViewPropagatesRightToLeftOrientationToSearchBarAndResults() {
        M3ListItem result = new M3ListItem("Result");
        M3SearchView searchView = searchView("Search", result);
        searchView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(searchView);
        Scene scene = new Scene(root, 360.0, 140.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        searchView.resize(320.0, 120.0);
        searchView.layout();

        assertEquals(NodeOrientation.RIGHT_TO_LEFT, searchViewSearchBar(searchView).getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, result.getEffectiveNodeOrientation());
    }

    /// Verifies that search bars keep their token height when a flow row contains taller controls.
    @Test
    void searchBarKeepsPreferredHeightInTallFlowRows() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setPrefWidth(260.0);
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            FlowPane root = new FlowPane(16.0, 16.0, searchBar, datePicker);
            Scene scene = new Scene(root, 760.0, 420.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
            root.resize(760.0, 420.0);
            root.layout();

            assertTrue(datePicker.getHeight() > searchBar.prefHeight(-1.0) * 2.0);
            assertEquals(56.0, searchBar.prefHeight(-1.0), 0.0001);
            assertEquals(56.0, searchBar.getHeight(), 0.0001);
        });
    }

    /// Verifies that search views own a search bar and mutable result list.
    @Test
    void searchViewOwnsSearchBarAndResults() {
        M3ListItem result = new M3ListItem("Result");
        M3ListItem replacement = new M3ListItem("Replacement");
        M3SearchView searchView = searchView("Find", result);
        Label leading = new Label("S");
        M3IconButton trailing = createIconButton("C");
        AtomicInteger actions = new AtomicInteger();

        searchView.setLeading(leading);
        searchView.getTrailingActions().add(trailing);
        searchView.getResults().add(replacement);
        searchView.getResults().setAll(result);
        searchView.setOnAction(event -> actions.incrementAndGet());
        searchView.setText("button");
        searchView.fire();

        assertEquals("button", searchView.getText());
        assertEquals("Find", searchView.getPromptText());
        assertEquals(leading, searchView.getLeading());
        assertEquals(leading, searchView.leadingProperty().get());
        assertEquals(trailing, searchView.getTrailingActions().get(0));
        assertEquals(result, searchView.getResults().get(0));
        assertEquals(1, actions.get());

        searchView.clearAndDeactivate();
        searchView.getResults().clear();
        searchView.getTrailingActions().clear();

        assertEquals("", searchView.getText());

        assertFalse(searchView.isActive());
        assertTrue(searchView.getResults().isEmpty());
        assertTrue(searchView.getTrailingActions().isEmpty());
    }

    /// Verifies that search views use active state to show or hide results.
    @Tier2Test
    @Test
    void searchViewActiveStateControlsResultsVisibility() throws InterruptedException {
        M3SearchView searchView = searchView("Find");
        M3ListItem result = new M3ListItem("Result");
        searchView.getResults().add(result);
        applyCss(searchView);

        assertTrue(searchView.isActive());
        assertTrue(searchViewSearchBar(searchView).isActive());

        searchView.deactivate();

        Node results = searchView.lookup("." + "m3-search-view-results");
        assertFalse(searchView.isActive());
        assertTrue(results.isVisible());
        assertTrue(results.isManaged());
        assertTrue(results.getOpacity() <= 1.0);

        FxTestUtils.runOnFxThreadWhen(() -> !results.isVisible()
                && !results.isManaged()
                && Math.abs(results.getOpacity()) < 0.0001
                && results.getTranslateY() < 0.0, () -> {
        }, () -> {
            assertFalse(results.isVisible());
            assertFalse(results.isManaged());
            assertEquals(0.0, results.getOpacity(), 0.0001);
            assertTrue(results.getTranslateY() < 0.0);
        });

        searchView.activate();

        assertTrue(searchView.isActive());
        assertTrue(results.isVisible());
        assertTrue(results.isManaged());
    }

    /// Verifies that search view result transitions settle immediately when animations are disabled at runtime.
    @Tier2Test
    @Test
    void searchViewResultsTransitionSettlesWhenAnimationsAreDisabledAtRuntime() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SearchView> searchViewReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> resultsReference = new AtomicReference<>();

        FxTestUtils.runOnFxThread(() -> {
            M3SearchView searchView = searchView("Find", new M3ListItem("Result"));
            searchView.setPrefSize(320.0, 160.0);

            StackPane root = new StackPane(searchView);
            root.setPrefSize(420.0, 240.0);
            FxTestUtils.setCssAnimationsEnabled(root, true);
            FxTestUtils.setMotionScheme(root, observableTestMotionScheme());

            Scene scene = new Scene(root, 420.0, 240.0);
            Stage stage = new Stage();
            stage.setScene(scene);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.show();
            root.applyCss();
            root.layout();

            Region results = lookupRegion(searchView, "." + "m3-search-view-results");
            assertTrue(searchView.isActive());
            assertTrue(results.isVisible());
            assertTrue(results.isManaged());
            assertEquals(1.0, results.getOpacity(), 0.0001);
            assertEquals(0.0, results.getTranslateY(), 0.0001);

            stageReference.set(stage);
            rootReference.set(root);
            searchViewReference.set(searchView);
            resultsReference.set(results);

            searchView.deactivate();
        });

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> searchViewResultsExitIsIntermediate(resultsReference),
                    () -> "Search view results never reached an intermediate exit transition frame: "
                            + describeSearchViewResults(resultsReference),
                    () -> {
                    },
                    () -> {
                        StackPane root = Objects.requireNonNull(rootReference.get(), "root");
                        M3SearchView searchView = Objects.requireNonNull(searchViewReference.get(), "searchView");
                        Region results = Objects.requireNonNull(resultsReference.get(), "results");

                        FxTestUtils.setCssAnimationsEnabled(root, false);
                        root.applyCss();
                        root.layout();

                        assertTrue(M3MotionSettings.shouldReduceMotion(searchView));
                        assertFalse(searchView.isActive());
                        assertFalse(results.isVisible());
                        assertFalse(results.isManaged());
                        assertEquals(0.0, results.getOpacity(), 0.0001);
                        assertEquals(-8.0, results.getTranslateY(), 0.0001);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable StackPane root = rootReference.get();
                if (root != null) {
                    FxTestUtils.clearMotionScheme(root);
                    FxTestUtils.setCssAnimationsEnabled(root, true);
                }

                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that search bars traverse focus between slots without stealing editor navigation keys.
    @Test
    void searchBarKeyboardTraversalFollowsDirectionWithoutStealingEditorKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button leading = new M3Button("Menu");
            M3Button filter = new M3Button("Filter");
            M3Button disabled = new M3Button("Disabled");
            M3SearchBar searchBar = new M3SearchBar("Search");
            searchBar.setLeading(leading);
            searchBar.getTrailingActions().setAll(filter, disabled);
            disabled.setDisable(true);
            StackPane root = new StackPane(searchBar);
            Scene scene = new Scene(root, 420.0, 140.0);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                searchBar.requestFocus();
                searchBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(leading.isFocused());

                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(searchBarEditor(searchBar).isFocused());

                searchBarEditor(searchBar).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(searchBarEditor(searchBar).isFocused());

                filter.requestFocus();
                filter.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(searchBarEditor(searchBar).isFocused());

                filter.requestFocus();
                filter.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(leading.isFocused());

                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(filter.isFocused());

                filter.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

                assertTrue(leading.isFocused());

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                leading.requestFocus();
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(filter.isFocused());

                filter.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(leading.isFocused());
                assertFalse(disabled.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that embedded search bars keep slot traversal inside search views.
    @Test
    void searchViewSearchBarSlotKeyboardTraversalSurvivesViewEventFiltering() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button leading = new M3Button("Menu");
            M3Button filter = new M3Button("Filter");
            M3ListItem result = new M3ListItem("Result");
            M3SearchView searchView = searchView("Search", result);
            searchView.setLeading(leading);
            searchView.getTrailingActions().setAll(filter);
            StackPane root = new StackPane(searchView);
            Scene scene = new Scene(root, 460.0, 220.0);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                leading.requestFocus();
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(searchViewEditor(searchView).isFocused());
                assertSame(searchViewEditor(searchView), searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                filter.requestFocus();
                filter.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

                assertTrue(leading.isFocused());
                assertSame(leading, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchViewEditor(searchView).requestFocus();
                searchViewEditor(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(result.isFocused());

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                leading.requestFocus();
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(filter.isFocused());
                assertSame(filter, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search views move keyboard focus between the editor and results.
    @Test
    void searchViewKeyboardNavigatesResults() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem first = new M3ListItem("First");
            M3ListItem second = new M3ListItem("Second");
            M3SearchView searchView = searchView("Search", first, second);

            Pane root = new Pane(searchView);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 240.0));
                stage.show();
                root.applyCss();

                searchViewEditor(searchView).requestFocus();
                assertTrue(searchViewEditor(searchView).isFocused());
                assertSame(searchViewEditor(searchView), searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchViewEditor(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(second.isFocused());
                assertSame(second, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(searchViewEditor(searchView).isFocused());
                assertSame(searchViewEditor(searchView), searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, second);
                assertTrue(second.isFocused());
                assertSame(second, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
                assertFalse(searchView.isActive());
                assertTrue(searchViewSearchBar(searchView).isFocused());

                searchViewSearchBar(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
                assertTrue(searchView.isActive());
                assertTrue(searchViewEditor(searchView).isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search view keyboard traversal continues after runtime orientation changes.
    @Test
    void searchViewKeyboardNavigationSurvivesRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button leading = new M3Button("Menu");
            M3Button trailing = new M3Button("Filter");
            M3ListItem first = new M3ListItem("First");
            M3ListItem second = new M3ListItem("Second");
            second.setTrailing(new M3Button("Open"));
            M3SearchView searchView = searchView("Search", first, second);
            searchView.setLeading(leading);
            searchView.getTrailingActions().setAll(trailing);
            searchView.setPrefWidth(380.0);

            StackPane root = new StackPane(searchView);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 460.0, 260.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                searchView.layout();

                searchViewEditor(searchView).requestFocus();
                assertTrue(searchViewEditor(searchView).isFocused());
                searchViewEditor(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                searchView.layout();

                assertSearchViewUsesLogicalGeometry(searchView, true);
                assertTrue(first.isFocused());
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(searchViewEditor(searchView).isFocused());
                assertSame(searchViewEditor(searchView), searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchViewEditor(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(first.isFocused());
                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(second.isFocused());
                assertSame(second, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchView.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.applyCss();
                root.layout();
                searchView.layout();

                assertSearchViewUsesLogicalGeometry(searchView, false);
                assertTrue(second.isFocused());
                second.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search view result navigation skips unreachable results and supports boundary keys.
    @Test
    void searchViewKeyboardNavigationSkipsUnreachableResultsAndSupportsPageKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem first = new M3ListItem("First");
            M3ListItem disabled = new M3ListItem("Disabled");
            M3ListItem hidden = new M3ListItem("Hidden");
            Label staticResult = new Label("Static");
            M3ListItem fourth = new M3ListItem("Fourth");
            M3ListItem fifth = new M3ListItem("Fifth");
            M3ListItem sixth = new M3ListItem("Sixth");
            M3ListItem seventh = new M3ListItem("Seventh");
            disabled.setDisable(true);
            hidden.setVisible(false);
            M3SearchView searchView = searchView(
                    "Search",
                    first,
                    disabled,
                    hidden,
                    staticResult,
                    fourth,
                    fifth,
                    sixth,
                    seventh
            );
            searchView.setManaged(false);
            Pane root = new Pane(searchView);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 260.0));
                stage.show();
                root.applyCss();
                searchView.resizeRelocate(0.0, 0.0, 320.0, 176.0);
                root.layout();
                searchView.layout();
                resizeRow(first);
                resizeRow(disabled);
                resizeRow(hidden);
                resizeRow(fourth);
                resizeRow(fifth);
                resizeRow(sixth);
                resizeRow(seventh);

                searchViewEditor(searchView).requestFocus();
                searchViewEditor(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
                assertTrue(searchViewEditor(searchView).isFocused());

                searchViewEditor(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(first.isFocused());
                assertSame(first, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(fourth.isFocused());
                assertSame(fourth, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                fourth.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
                assertTrue(seventh.isFocused());
                assertSame(seventh, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                seventh.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));
                assertTrue(seventh.isFocused());

                seventh.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));
                assertTrue(fourth.isFocused());

                fourth.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                assertTrue(seventh.isFocused());

                seventh.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
                assertTrue(first.isFocused());

                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));
                assertTrue(first.isFocused());

                first.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(searchViewEditor(searchView).isFocused());

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(first.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search view result navigation skips results and owners hidden by ancestors.
    @Test
    void searchViewSkipsResultsAndOwnersHiddenByAncestors() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button hiddenAction = new M3Button("Hidden action");
            Pane hiddenResult = new Pane(hiddenAction);
            hiddenResult.setVisible(false);
            M3ListItem visibleResult = new M3ListItem("Visible");
            M3SearchView searchView = searchView("Search", hiddenResult, visibleResult);
            M3Button outside = new M3Button("Outside");
            Pane searchOwner = new Pane(searchView);
            VBox root = new VBox(8.0, outside, searchOwner);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 480.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                searchViewEditor(searchView).requestFocus();
                searchViewEditor(searchView).fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(visibleResult.isFocused());
                assertFalse(hiddenAction.isFocused());
                assertSame(visibleResult, searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                searchViewEditor(searchView).requestFocus();
                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenAction);

                assertTrue(searchViewEditor(searchView).isFocused());
                assertFalse(hiddenAction.isFocused());

                outside.requestFocus();
                searchOwner.setVisible(false);
                root.applyCss();
                root.layout();

                searchView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, visibleResult);

                assertTrue(outside.isFocused());
                assertFalse(visibleResult.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that search controls expose accessible text, focus, action, and result state.
    @Test
    void searchControlsExposeAccessibleStateAndActions() {
        M3SearchBar searchBar = new M3SearchBar("Search");
        AtomicInteger searchActions = new AtomicInteger();
        searchBar.setOnAction(event -> searchActions.incrementAndGet());

        searchBar.executeAccessibleAction(AccessibleAction.SET_TEXT, "material");
        assertEquals("material", searchBar.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(searchBarEditor(searchBar), searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        searchBar.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
        assertEquals(true, searchBar.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchBar.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, searchActions.get());

        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3SearchView searchView = searchView("Search", first, second);
        AtomicInteger viewActions = new AtomicInteger();
        searchView.setOnAction(event -> viewActions.incrementAndGet());

        assertEquals(true, searchView.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchView.executeAccessibleAction(AccessibleAction.SET_TEXT, "query");
        assertEquals("query", searchView.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        M3IconButton searchViewFocusNode = assertInstanceOf(
                M3IconButton.class,
                searchView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
        );
        assertEquals("Back", searchViewFocusNode.getAccessibleText());
        assertEquals(2, searchView.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(second, searchView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        searchView.executeAccessibleAction(AccessibleAction.COLLAPSE);
        assertEquals(false, searchView.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchView.executeAccessibleAction(AccessibleAction.EXPAND);
        assertEquals(true, searchView.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));
        searchView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
        searchView.executeAccessibleAction(AccessibleAction.FIRE);
        assertEquals(1, viewActions.get());
    }

    /// Returns the embedded editor exposed for accessibility by a search bar.
    private static TextField searchBarEditor(M3SearchBar searchBar) {
        return assertInstanceOf(TextField.class, searchBar.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
    }

    /// Returns the embedded search bar rendered by a search view.
    private static M3SearchBar searchViewSearchBar(M3SearchView searchView) {
        return firstDescendantOfType(searchView, M3SearchBar.class);
    }

    /// Returns the embedded editor rendered by a search view.
    private static TextField searchViewEditor(M3SearchView searchView) {
        return searchBarEditor(searchViewSearchBar(searchView));
    }

    /// Returns whether search view results are rendering an intermediate exit transition.
    private static boolean searchViewResultsExitIsIntermediate(AtomicReference<@Nullable Region> resultsReference) {
        @Nullable Region results = resultsReference.get();
        if (results == null) {
            return false;
        }

        results.applyCss();
        results.layout();
        return results.isVisible()
                && results.isManaged()
                && isBetweenExclusive(results.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(Math.abs(results.getTranslateY()), 0.0, 8.0);
    }

    /// Describes search view result presentation state for motion timeout diagnostics.
    private static String describeSearchViewResults(AtomicReference<@Nullable Region> resultsReference) {
        @Nullable Region results = resultsReference.get();
        if (results == null) {
            return "results=null";
        }
        return "visible=" + results.isVisible()
                + ", managed=" + results.isManaged()
                + ", opacity=" + results.getOpacity()
                + ", translateY=" + results.getTranslateY();
    }

    /// Verifies the resolved logical slot geometry of a search bar.
    private static void assertSearchBarUsesLogicalGeometry(M3SearchBar searchBar, boolean rightToLeft) {
        searchBar.applyCss();
        searchBar.layout();

        Region leadingSlot = lookupRegion(searchBar, "." + "m3-search-bar-leading");
        Region trailingBox = lookupRegion(searchBar, "." + "m3-search-bar-trailing");
        TextInputControl editor = searchBarEditor(searchBar);
        Bounds leadingBounds = leadingSlot.localToScene(leadingSlot.getBoundsInLocal());
        Bounds editorBounds = editor.localToScene(editor.getBoundsInLocal());
        Bounds trailingBounds = trailingBox.localToScene(trailingBox.getBoundsInLocal());

        if (rightToLeft) {
            assertTrue(trailingBounds.getCenterX() < editorBounds.getCenterX(),
                    () -> describeSearchBarGeometry(leadingBounds, editorBounds, trailingBounds));
            assertTrue(editorBounds.getCenterX() < leadingBounds.getCenterX(),
                    () -> describeSearchBarGeometry(leadingBounds, editorBounds, trailingBounds));
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, searchBar.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, editor.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, trailingBox.getEffectiveNodeOrientation());
        } else {
            assertTrue(leadingBounds.getCenterX() < editorBounds.getCenterX(),
                    () -> describeSearchBarGeometry(leadingBounds, editorBounds, trailingBounds));
            assertTrue(editorBounds.getCenterX() < trailingBounds.getCenterX(),
                    () -> describeSearchBarGeometry(leadingBounds, editorBounds, trailingBounds));
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, searchBar.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, editor.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, trailingBox.getEffectiveNodeOrientation());
        }
    }

    /// Verifies the resolved logical geometry of an active search view.
    private static void assertSearchViewUsesLogicalGeometry(M3SearchView searchView, boolean rightToLeft) {
        searchView.applyCss();
        searchView.layout();

        assertSearchBarUsesLogicalGeometry(searchViewSearchBar(searchView), rightToLeft);
        assertEquals(
                rightToLeft ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT,
                searchViewResultsContainer(searchView).getEffectiveNodeOrientation()
        );

        for (Node result : searchView.getResults()) {
            assertEquals(
                    rightToLeft ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT,
                    result.getEffectiveNodeOrientation()
            );
        }

        M3ListItem secondResult = assertInstanceOf(M3ListItem.class, searchView.getResults().get(1));
        Node trailing = assertInstanceOf(Node.class, secondResult.getTrailing());
        Bounds rowBounds = secondResult.localToScene(secondResult.getBoundsInLocal());
        Bounds trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());

        if (rightToLeft) {
            assertTrue(trailingBounds.getCenterX() < rowBounds.getCenterX(),
                    () -> "rowBounds=" + rowBounds + ", trailingBounds=" + trailingBounds);
        } else {
            assertTrue(trailingBounds.getCenterX() > rowBounds.getCenterX(),
                    () -> "rowBounds=" + rowBounds + ", trailingBounds=" + trailingBounds);
        }
    }

    /// Describes search bar slot geometry for assertion failures.
    private static String describeSearchBarGeometry(
            Bounds leadingBounds,
            Bounds editorBounds,
            Bounds trailingBounds
    ) {
        return "leadingBounds=" + leadingBounds
                + ", editorBounds=" + editorBounds
                + ", trailingBounds=" + trailingBounds;
    }
}
