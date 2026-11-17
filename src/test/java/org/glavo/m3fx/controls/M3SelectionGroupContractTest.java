// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
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
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.skins.M3ButtonGroupSkin;
import org.glavo.m3fx.skins.M3ChipGroupSkin;
import org.glavo.m3fx.skins.M3IconToggleButtonGroupSkin;
import org.glavo.m3fx.skins.M3SegmentedButtonGroupSkin;
import org.glavo.m3fx.skins.M3SegmentedButtonSkin;
import org.glavo.m3fx.skins.M3TabBarSkin;
import org.glavo.m3fx.skins.M3TabSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies selection, keyboard navigation, and layout in grouped choices.
@NotNullByDefault
final class M3SelectionGroupContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that grouped selection containers create Material Design 3 skins.
    @Test
    void groupedSelectionContainersCreateMaterialSkins() {
        M3ButtonGroup buttonGroup = buttonGroup(new M3Button("A"), new M3Button("B"));
        M3IconToggleButtonGroup iconGroup =
                iconToggleButtonGroup(new M3IconToggleButton("A"), new M3IconToggleButton("B"));
        M3ChipGroup chipGroup = chipGroup(new M3AssistChip("A"), new M3AssistChip("B"));
        M3SegmentedButtonGroup segmentedGroup =
                segmentedButtonGroup(new M3SegmentedButton("A"), new M3SegmentedButton("B"));
        M3TabBar tabBar = tabBar(new M3Tab("A"), new M3Tab("B"));
        Pane root = new Pane(buttonGroup, iconGroup, chipGroup, segmentedGroup, tabBar);
        Scene scene = new Scene(root);

        chipGroup.setPrefWrapLength(240.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertInstanceOf(M3ButtonGroupSkin.class, buttonGroup.getSkin());
        assertInstanceOf(M3IconToggleButtonGroupSkin.class, iconGroup.getSkin());
        assertInstanceOf(M3ChipGroupSkin.class, chipGroup.getSkin());
        assertInstanceOf(M3SegmentedButtonGroupSkin.class, segmentedGroup.getSkin());
        assertInstanceOf(M3TabBarSkin.class, tabBar.getSkin());
        assertTrue(tabBar.lookup("." + "m3-tab-bar-container") instanceof HBox);
        assertEquals(240.0, chipGroup.getPrefWrapLength(), 0.0001);
    }

    /// Verifies that toggle icon button groups keep selection mutually exclusive.
    @Test
    void iconToggleButtonGroupKeepsSelectionExclusive() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButtonGroup group = iconToggleButtonGroup(first, second);

        first.setSelected(true);

        assertEquals(M3SelectionMode.SINGLE, group.getSelectionMode());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertEquals(0, group.getSelectedIndex());
        assertTrue(first.isSelected());
        assertFalse(second.isSelected());

        group.selectIndex(1);

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertEquals(1, group.getSelectedIndex());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertThrows(UnsupportedOperationException.class, () -> group.getSelectedButtons().add(first));

        group.selectPrevious();

        assertEquals(first, group.getSelectedButton());
        assertTrue(first.isSelected());
        assertFalse(second.isSelected());

        group.selectNext();

        assertEquals(second, group.getSelectedButton());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());

        group.clearSelection();

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertEquals(-1, group.getSelectedIndex());
        assertFalse(first.isSelected());
        assertFalse(second.isSelected());
    }

    /// Verifies that toggle icon button groups can use multiple selected button behavior.
    @Test
    void iconToggleButtonGroupCanUseMultipleSelection() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButton third = new M3IconToggleButton("C");
        M3IconToggleButtonGroup group = iconToggleButtonGroup(first, second, third);

        group.setSelectionMode(M3SelectionMode.MULTIPLE);
        first.fire();
        third.fire();

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertTrue(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());

        first.fire();

        assertFalse(first.isSelected());
        assertTrue(third.isSelected());
        assertEquals(third, group.getSelectedButton());
        assertEquals(java.util.List.of(third), group.getSelectedButtons());
    }

    /// Verifies that toggle icon button groups collapse multiple selection when switched to single selection.
    @Test
    void iconToggleButtonGroupCollapsesMultipleSelectionWhenModeChanges() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButton third = new M3IconToggleButton("C");
        M3IconToggleButtonGroup group = iconToggleButtonGroup(first, second, third);

        group.setSelectionMode(M3SelectionMode.MULTIPLE);
        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());

        group.setSelectionMode(M3SelectionMode.SINGLE);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertFalse(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
    }

    /// Verifies that toggle icon button groups can require a selected button.
    @Test
    void iconToggleButtonGroupCanRequireSelection() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButtonGroup group = iconToggleButtonGroup(first, second);

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        group.clearSelection();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        first.fire();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertTrue(second.isSelected());
    }

    /// Verifies that toggle icon button groups clean selection when children are removed.
    @Test
    void iconToggleButtonGroupUpdatesSelectionWhenChildrenChange() {
        M3IconToggleButton first = new M3IconToggleButton("A");
        M3IconToggleButton second = new M3IconToggleButton("B");
        M3IconToggleButtonGroup group = iconToggleButtonGroup(first, second);

        second.setSelected(true);
        group.getItems().remove(second);

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertFalse(second.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());
    }

    /// Verifies that toggle icon button group spacing is styleable from CSS.
    @Test
    void iconToggleButtonGroupSpacingTokenIsStyleable() {
        M3IconToggleButtonGroup group =
                iconToggleButtonGroup(new M3IconToggleButton("A"), new M3IconToggleButton("B"));
        group.setStyle("-m3-icon-toggle-button-group-spacing: 12px;");

        applyCss(group);

        assertEquals(12.0, group.getSpacing(), 0.0001);
    }

    /// Verifies that type-ahead reset delays refresh while printable-key prefixes are buffered.
    @Tier2Test
    @Test
    void typeAheadResetDelayRefreshUsesThemeBehavior() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable VBox> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListPane> listPaneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> listPaneSearchReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListView<String>> listViewReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Menu> menuReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuItem> menuSearchReference = new AtomicReference<>();
        AtomicReference<@Nullable M3NavigationDrawer> drawerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListItem> drawerSearchReference = new AtomicReference<>();
        AtomicLong readyNanos = new AtomicLong();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> readyNanos.get() > 0L && System.nanoTime() >= readyNanos.get(),
                    () -> {
                        M3ListItem listPaneArchive = new M3ListItem("Archive");
                        M3ListItem listPaneAssistant = new M3ListItem("Assistant");
                        M3ListItem listPaneSearch = new M3ListItem("Search");
                        M3ListPane listPane = listPane(listPaneArchive, listPaneAssistant, listPaneSearch);
                        listPane.setSelectionMode(M3SelectionMode.SINGLE);

                        M3ListView<String> listView = listView("Archive", "Assistant", "Search");
                        listView.setSelectionMode(M3SelectionMode.SINGLE);
                        listView.setFixedCellSize(48.0);

                        M3MenuItem menuArchive = new M3MenuItem("Archive");
                        M3MenuItem menuAssistant = new M3MenuItem("Assistant");
                        M3MenuItem menuSearch = new M3MenuItem("Search");
                        M3Menu menu = new M3Menu(menuArchive, menuAssistant, menuSearch);
                        menu.setSelectionMode(M3SelectionMode.SINGLE);

                        M3ListItem drawerArchive = new M3ListItem("Archive");
                        M3ListItem drawerAssistant = new M3ListItem("Assistant");
                        M3ListItem drawerSearch = new M3ListItem("Search");
                        M3NavigationDrawer drawer = navigationDrawer(
                                drawerArchive,
                                drawerAssistant,
                                drawerSearch
                        );

                        VBox root = new VBox(12.0, listPane, listView, menu, drawer);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 420.0, 520.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setMotionBehavior(root, interactionTimingBehavior(
                                Duration.seconds(30.0),
                                Duration.seconds(30.0),
                                Duration.seconds(30.0)
                        ));
                        stage.setScene(scene);
                        stage.show();
                        listView.resize(360.0, 160.0);
                        root.applyCss();
                        root.layout();

                        listPane.requestFocus();
                        listPane.fireEvent(keyTypedEvent("a"));
                        assertTrue(Objects.requireNonNull(
                                listPane.getSelectedItem(),
                                "listPaneSelectedItem"
                        ).getHeadlineText().startsWith("A"));

                        listView.requestFocus();
                        listView.fireEvent(keyTypedEvent("a"));
                        assertTrue(Objects.requireNonNull(
                                listView.getSelectedItem(),
                                "listViewSelectedItem"
                        ).startsWith("A"));

                        menu.requestFocus();
                        menu.fireEvent(keyTypedEvent("a"));
                        assertTrue(Objects.requireNonNull(
                                menu.getSelectedItem(),
                                "menuSelectedItem"
                        ).getHeadlineText().startsWith("A"));

                        drawer.requestFocus();
                        drawer.fireEvent(keyTypedEvent("a"));
                        assertTrue(Objects.requireNonNull(
                                drawer.getSelectedItem(),
                                "drawerSelectedItem"
                        ).getHeadlineText().startsWith("A"));

                        stageReference.set(stage);
                        rootReference.set(root);
                        listPaneReference.set(listPane);
                        listPaneSearchReference.set(listPaneSearch);
                        listViewReference.set(listView);
                        menuReference.set(menu);
                        menuSearchReference.set(menuSearch);
                        drawerReference.set(drawer);
                        drawerSearchReference.set(drawerSearch);

                        FxTestUtils.setMotionBehavior(root, interactionTimingBehavior(
                                Duration.millis(40.0),
                                Duration.seconds(30.0),
                                Duration.seconds(30.0)
                        ));
                        readyNanos.set(System.nanoTime() + 180_000_000L);
                    },
                    () -> {
                        M3ListPane listPane = Objects.requireNonNull(listPaneReference.get(), "listPane");
                        M3ListItem listPaneSearch =
                                Objects.requireNonNull(listPaneSearchReference.get(), "listPaneSearch");
                        M3ListView<String> listView = Objects.requireNonNull(listViewReference.get(), "listView");
                        M3Menu menu = Objects.requireNonNull(menuReference.get(), "menu");
                        M3MenuItem menuSearch = Objects.requireNonNull(menuSearchReference.get(), "menuSearch");
                        M3NavigationDrawer drawer = Objects.requireNonNull(drawerReference.get(), "drawer");
                        M3ListItem drawerSearch = Objects.requireNonNull(drawerSearchReference.get(), "drawerSearch");

                        listPane.fireEvent(keyTypedEvent("s"));
                        assertSame(listPaneSearch, listPane.getSelectedItem());
                        assertTrue(listPaneSearch.isFocused());

                        listView.fireEvent(keyTypedEvent("s"));
                        assertEquals("Search", listView.getSelectedItem());
                        assertEquals(2, listView.getFocusedIndex());

                        menu.fireEvent(keyTypedEvent("s"));
                        assertSame(menuSearch, menu.getSelectedItem());
                        assertTrue(menuSearch.isFocused());

                        drawer.fireEvent(keyTypedEvent("s"));
                        assertSame(drawerSearch, drawer.getSelectedItem());
                        assertTrue(drawerSearch.isFocused());
                    }
            );
        } finally {
            closeMotionTestScene(stageReference, rootReference);
        }
    }

    /// Verifies that chip groups track multiple selected chips in child order.
    @Test
    void chipGroupTracksMultipleSelections() {
        M3SelectableChip first = createFilterChip("First");
        M3SelectableChip second = createFilterChip("Second");
        M3SelectableChip third = createFilterChip("Third");
        M3ChipGroup group = chipGroup(first, second, third);

        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), group.getSelectedChips());
        assertEquals(first, group.getSelectedChip());
        assertEquals(0, group.getSelectedIndex());

        first.setSelected(false);

        assertEquals(java.util.List.of(third), group.getSelectedChips());
        assertEquals(third, group.getSelectedChip());
        assertEquals(2, group.getSelectedIndex());

        group.selectIndex(1);

        assertEquals(java.util.List.of(second, third), group.getSelectedChips());
        assertEquals(second, group.getSelectedChip());
        assertEquals(1, group.getSelectedIndex());

        group.clearSelection();

        assertTrue(group.getSelectedChips().isEmpty());
        assertNull(group.getSelectedChip());
        assertEquals(-1, group.getSelectedIndex());
    }

    /// Verifies that chip groups can enforce single selection.
    @Test
    void chipGroupCanUseSingleSelection() {
        M3SelectableChip first = createFilterChip("First");
        M3SelectableChip second = createFilterChip("Second");
        M3ChipGroup group = chipGroup(first, second);

        group.setSelectionMode(M3SelectionMode.SINGLE);
        first.setSelected(true);
        second.setSelected(true);

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, group.getSelectedChip());
        assertEquals(java.util.List.of(second), group.getSelectedChips());

        group.select(first);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertEquals(first, group.getSelectedChip());

        group.selectNext();

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(second, group.getSelectedChip());

        group.selectPrevious();

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertEquals(first, group.getSelectedChip());
    }

    /// Verifies that chip groups can require a selected chip.
    @Test
    void chipGroupCanRequireSelection() {
        M3SelectableChip first = createFilterChip("First");
        M3SelectableChip second = createFilterChip("Second");
        M3ChipGroup group = chipGroup(first, second);

        group.setSelectionMode(M3SelectionMode.SINGLE);
        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedChip());
        assertTrue(first.isSelected());

        group.clearSelection();

        assertEquals(first, group.getSelectedChip());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, group.getSelectedChip());
        assertTrue(second.isSelected());
    }

    /// Verifies that chip groups update selection when chips are removed.
    @Test
    void chipGroupUpdatesSelectionWhenChildrenChange() {
        M3SelectableChip first = createFilterChip("First");
        M3SelectableChip second = createFilterChip("Second");
        M3ChipGroup group = chipGroup(first, second);

        second.setSelected(true);
        group.getItems().remove(second);

        assertTrue(group.getSelectedChips().isEmpty());
        assertNull(group.getSelectedChip());
        assertFalse(second.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedChip());
        assertTrue(first.isSelected());
    }

    /// Verifies that chip group layout gaps are styleable from CSS.
    @Test
    void chipGroupGapTokensAreStyleable() {
        M3ChipGroup group = chipGroup(new M3AssistChip("First"), new M3AssistChip("Second"));
        group.setStyle("-m3-chip-group-horizontal-gap: 12px; -m3-chip-group-vertical-gap: 14px;");

        applyCss(group);

        assertEquals(12.0, group.getHorizontalGap(), 0.0001);
        assertEquals(14.0, group.getVerticalGap(), 0.0001);
    }

    /// Verifies that segmented button component token properties are styleable from CSS.
    @Test
    void segmentedButtonTokensAreStyleable() {
        M3SegmentedButton button = createSegmentedButton("Week", true);
        button.setStyle("-m3-container-height: 44px; -m3-container-shape: 12px; -m3-horizontal-padding: 18px;");

        applyCss(button);

        assertTrue(button.isSelected());
        assertEquals(44.0, button.getContainerHeight(), 0.0001);
        assertEquals(12.0, button.getContainerShape(), 0.0001);
        assertEquals(18.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(44.0, button.prefHeight(-1.0), 0.0001);
        assertEquals(18.0, button.getPadding().getLeft(), 0.0001);
        assertEquals(18.0, button.getPadding().getRight(), 0.0001);
    }

    /// Verifies that segmented button groups keep a single selected segment.
    @Test
    void segmentedButtonGroupKeepsSingleSelection() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButtonGroup group = segmentedButtonGroup(first, second);

        first.setSelected(true);
        second.setSelected(true);

        assertEquals(M3SelectionMode.SINGLE, group.getSelectionMode());
        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertEquals(1, group.getSelectedIndex());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());

        group.selectIndex(0);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertEquals(0, group.getSelectedIndex());
        assertTrue(first.isSelected());
        assertFalse(second.isSelected());

        group.selectNext();

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertFalse(first.isSelected());
        assertTrue(second.isSelected());

        group.selectPrevious();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());
        assertFalse(second.isSelected());

        first.fire();

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertEquals(-1, group.getSelectedIndex());
        assertFalse(first.isSelected());
    }

    /// Verifies that segmented button groups can use multiple selected segment behavior.
    @Test
    void segmentedButtonGroupCanUseMultipleSelection() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButton third = new M3SegmentedButton("Third");
        M3SegmentedButtonGroup group = segmentedButtonGroup(first, second, third);

        group.setSelectionMode(M3SelectionMode.MULTIPLE);
        first.fire();
        third.fire();

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertTrue(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());
        assertThrows(UnsupportedOperationException.class, () -> group.getSelectedButtons().add(second));

        first.fire();

        assertFalse(first.isSelected());
        assertTrue(third.isSelected());
        assertEquals(third, group.getSelectedButton());
        assertEquals(java.util.List.of(third), group.getSelectedButtons());
    }

    /// Verifies that segmented button groups collapse multiple selection when switched to single selection.
    @Test
    void segmentedButtonGroupCollapsesMultipleSelectionWhenModeChanges() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButton third = new M3SegmentedButton("Third");
        M3SegmentedButtonGroup group = segmentedButtonGroup(first, second, third);

        group.setSelectionMode(M3SelectionMode.MULTIPLE);
        third.setSelected(true);
        first.setSelected(true);

        assertEquals(java.util.List.of(first, third), group.getSelectedButtons());

        group.setSelectionMode(M3SelectionMode.SINGLE);

        assertTrue(first.isSelected());
        assertFalse(second.isSelected());
        assertFalse(third.isSelected());
        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
    }

    /// Verifies that segmented button groups can require a selected segment.
    @Test
    void segmentedButtonGroupCanRequireSelection() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButtonGroup group = segmentedButtonGroup(first, second);

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());

        first.setSelected(true);
        group.clearSelection();

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertFalse(first.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        first.fire();

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());

        second.setSelected(true);
        second.setSelected(false);

        assertEquals(second, group.getSelectedButton());
        assertEquals(java.util.List.of(second), group.getSelectedButtons());
        assertTrue(second.isSelected());
    }

    /// Verifies that segmented button groups update selection when children change.
    @Test
    void segmentedButtonGroupUpdatesSelectionWhenChildrenChange() {
        M3SegmentedButton first = new M3SegmentedButton("First");
        M3SegmentedButton second = new M3SegmentedButton("Second");
        M3SegmentedButtonGroup group = segmentedButtonGroup(first, second);

        second.setSelected(true);
        group.getItems().remove(second);

        assertNull(group.getSelectedButton());
        assertTrue(group.getSelectedButtons().isEmpty());
        assertFalse(second.isSelected());

        group.setAllowEmptySelection(false);

        assertEquals(first, group.getSelectedButton());
        assertEquals(java.util.List.of(first), group.getSelectedButtons());
        assertTrue(first.isSelected());
    }

    /// Verifies that segmented button group spacing is styleable from CSS.
    @Test
    void segmentedButtonGroupSpacingTokenIsStyleable() {
        M3SegmentedButtonGroup group =
                segmentedButtonGroup(new M3SegmentedButton("A"), new M3SegmentedButton("B"));
        group.setStyle("-m3-segmented-button-group-spacing: -2px;");

        applyCss(group);

        assertEquals(-2.0, group.getSpacing(), 0.0001);
    }

    /// Verifies that m3fx segmented buttons create the Material Design 3 skin.
    @Test
    void segmentedButtonCreatesMaterialSkin() {
        M3SegmentedButton button = new M3SegmentedButton("Month");

        applyCss(button);

        assertInstanceOf(M3SegmentedButtonSkin.class, button.getSkin());
    }

    /// Verifies that text-only selected segments expose the default indicator without changing intrinsic width.
    @Test
    void segmentedButtonProvidesStableOptionalSelectionIndicator() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButton button = new M3SegmentedButton("Week");
            button.setSelected(true);
            StackPane root = new StackPane(button);
            Scene scene = new Scene(root, 180.0, 80.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();
            button.layout();

            Region indicator = segmentedButtonSelectionIndicator(button);
            double selectedPrefWidth = button.prefWidth(-1.0);
            assertTrue(button.isSelectionIndicatorEnabled());
            assertEquals(1.0, indicator.getOpacity(), 0.0001);
            assertEquals(18.0, indicator.getWidth(), 0.0001);
            assertEquals(18.0, indicator.getHeight(), 0.0001);

            button.setSelected(false);

            assertEquals(selectedPrefWidth, button.prefWidth(-1.0), 0.0001,
                    "selection indicator must not change intrinsic segment width");
        });
    }

    /// Verifies that applications can suppress the indicator and that selected checks replace supplied graphics.
    @Test
    void segmentedButtonSelectionIndicatorRespectsConfigurationAndReplacesGraphic() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButton indicatorDisabled = new M3SegmentedButton("Day");
            indicatorDisabled.setSelectionIndicatorEnabled(false);
            indicatorDisabled.setSelected(true);

            Region graphic = new Region();
            graphic.setPrefSize(18.0, 18.0);
            M3SegmentedButton graphicButton = new M3SegmentedButton("Month", graphic);
            graphicButton.setSelected(true);

            HBox root = new HBox(indicatorDisabled, graphicButton);
            Scene scene = new Scene(root, 320.0, 80.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();
            indicatorDisabled.layout();
            graphicButton.layout();

            Region disabledIndicator = segmentedButtonSelectionIndicator(indicatorDisabled);
            Region graphicIndicator = segmentedButtonSelectionIndicator(graphicButton);
            Bounds graphicBounds = graphic.getBoundsInParent();
            Bounds indicatorBounds = graphicIndicator.getBoundsInParent();

            assertEquals(0.0, disabledIndicator.getOpacity(), 0.0001);
            assertEquals(1.0, graphicIndicator.getOpacity(), 0.0001);
            assertEquals(graphicBounds.getCenterX(), indicatorBounds.getCenterX(), 0.0001);
            assertEquals(graphicBounds.getCenterY(), indicatorBounds.getCenterY(), 0.0001);
            assertTrue(graphicIndicator.getViewOrder() < graphic.getViewOrder());
            assertSame(graphic, graphicButton.getGraphic());
            assertEquals(0.0, graphic.getOpacity(), 0.0001);
            assertNull(graphicButton.lookup(".m3-segmented-button-selection-indicator-backdrop"));
            assertNotNull(graphicButton.lookup(".m3-segmented-button-selection-indicator-mark"));

            PseudoClass hover = PseudoClass.getPseudoClass("hover");
            graphicButton.pseudoClassStateChanged(hover, true);
            root.applyCss();
            assertEquals(0.0, graphic.getOpacity(), 0.0001);
            assertNull(graphicButton.lookup(".m3-segmented-button-selection-indicator-backdrop"));
            assertNotNull(graphicButton.lookup(".m3-segmented-button-selection-indicator-mark"));

            FxTestUtils.setCssAnimationsEnabled(root, false);
            try {
                graphicButton.setSelected(false);
                root.applyCss();
                assertEquals(1.0, graphic.getOpacity(), 0.0001);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
            }
        });
    }

    /// Verifies that a selected-state indicator inherits the exact center of the graphic it replaces.
    @Test
    void segmentedButtonSelectionIndicatorDoesNotResnapGraphicCenter() {
        FxTestUtils.runOnFxThread(() -> {
            M3SVGIcon graphic = svgTestIcon();
            M3SegmentedButton button = new M3SegmentedButton("Month", graphic);
            StackPane root = new StackPane(button);
            Scene scene = new Scene(root, 181.0, 80.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();
            button.layout();

            assertEquals(18.0, graphic.getIconSize(), 0.0001);
            double graphicCenterBefore = graphic.localToScene(graphic.getBoundsInLocal()).getCenterX();
            FxTestUtils.setCssAnimationsEnabled(root, false);
            try {
                button.setSelected(true);
                root.applyCss();
                root.layout();
                button.layout();

                Region indicator = segmentedButtonSelectionIndicator(button);
                double graphicCenterAfter = graphic.localToScene(graphic.getBoundsInLocal()).getCenterX();
                double indicatorCenter = indicator.localToScene(indicator.getBoundsInLocal()).getCenterX();
                assertEquals(graphicCenterBefore, graphicCenterAfter, 0.0001);
                assertEquals(graphicCenterAfter, indicatorCenter, 0.0001);
                assertEquals(0.0, graphic.getOpacity(), 0.0001);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
            }
        });
    }

    /// Verifies that the built-in selected icon follows logical leading placement in both layout directions.
    @Test
    void segmentedButtonSelectionIndicatorFollowsNodeOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButton button = new M3SegmentedButton("Day");
            button.setSelected(true);
            M3SegmentedButtonGroup group = segmentedButtonGroup(button);
            StackPane root = new StackPane(group);
            Scene scene = new Scene(root, 180.0, 80.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();
            group.layout();
            button.layout();

            Region leftToRightIndicatorNode = segmentedButtonSelectionIndicator(button);
            Node leftToRightLabelNode = Objects.requireNonNull(button.lookup(".text"), "LTR label");
            Bounds leftToRightIndicator = leftToRightIndicatorNode.localToScene(
                    leftToRightIndicatorNode.getBoundsInLocal()
            );
            Bounds leftToRightLabel = leftToRightLabelNode.localToScene(leftToRightLabelNode.getBoundsInLocal());
            assertTrue(leftToRightIndicator.getMaxX() < leftToRightLabel.getMinX(),
                    "LTR indicator should precede its label");

            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            group.layout();
            button.layout();

            Region rightToLeftIndicatorNode = segmentedButtonSelectionIndicator(button);
            Node rightToLeftLabelNode = Objects.requireNonNull(button.lookup(".text"), "RTL label");
            Bounds rightToLeftIndicator = rightToLeftIndicatorNode.localToScene(
                    rightToLeftIndicatorNode.getBoundsInLocal()
            );
            Bounds rightToLeftLabel = rightToLeftLabelNode.localToScene(rightToLeftLabelNode.getBoundsInLocal());
            assertTrue(rightToLeftIndicator.getMinX() > rightToLeftLabel.getMaxX(),
                    () -> "an inherited RTL change should move the indicator after its label physically: orientation="
                            + button.getEffectiveNodeOrientation() + ", indicator=" + rightToLeftIndicator
                            + ", label=" + rightToLeftLabel);

            M3SegmentedButton attachedButton = new M3SegmentedButton("Week");
            attachedButton.setSelected(true);
            M3SegmentedButtonGroup attachedGroup = segmentedButtonGroup(attachedButton);
            root.getChildren().setAll(attachedGroup);
            root.applyCss();
            root.layout();
            attachedGroup.layout();
            attachedButton.layout();

            Region attachedIndicatorNode = segmentedButtonSelectionIndicator(attachedButton);
            Node attachedLabelNode = Objects.requireNonNull(attachedButton.lookup(".text"), "attached RTL label");
            Bounds attachedIndicator = attachedIndicatorNode.localToScene(attachedIndicatorNode.getBoundsInLocal());
            Bounds attachedLabel = attachedLabelNode.localToScene(attachedLabelNode.getBoundsInLocal());
            assertTrue(attachedIndicator.getMinX() > attachedLabel.getMaxX(),
                    "a segment attached directly to an RTL hierarchy should place its indicator after the label");
        });
    }

    /// Verifies that selected segmented button states keep Material colors.
    @Test
    void segmentedButtonStateStylesPreserveSelectedColors() {
        M3SegmentedButton day = new M3SegmentedButton("Day");
        M3SegmentedButton week = new M3SegmentedButton("Week");
        M3SegmentedButton month = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup group = segmentedButtonGroup(day, week, month);
        Pane root = new Pane(group);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        month.setSelected(true);
        applyInteractivePseudoClasses(month);
        root.applyCss();
        root.layout();

        assertLabeledColors(day, Color.TRANSPARENT, Color.rgb(34, 35, 36));
        assertBorderColor(day, Color.rgb(13, 14, 15));
        assertLabeledColors(month, Color.TRANSPARENT, Color.rgb(10, 11, 12));
        assertRegionFill(segmentedButtonSelectionContainer(month), Color.rgb(7, 8, 9));
        assertBorderColor(month, Color.rgb(13, 14, 15));
    }

    /// Verifies that segmented button groups assign segment position style classes.
    @Test
    void segmentedButtonGroupAssignsPositionStyleClasses() {
        M3SegmentedButton first = new M3SegmentedButton("Day");
        M3SegmentedButton second = new M3SegmentedButton("Week");
        M3SegmentedButton third = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup group = segmentedButtonGroup(first, second, third);

        assertTrue(group.getStyleClass().contains("m3-segmented-button-group"));
        assertTrue(first.getStyleClass().contains("m3-segmented-button-first"));
        assertTrue(second.getStyleClass().contains("m3-segmented-button-middle"));
        assertTrue(third.getStyleClass().contains("m3-segmented-button-last"));

        group.getItems().remove(second);

        assertFalse(second.getStyleClass().contains("m3-segmented-button-middle"));
        assertTrue(first.getStyleClass().contains("m3-segmented-button-first"));
        assertTrue(third.getStyleClass().contains("m3-segmented-button-last"));

        group.getItems().remove(first);

        assertFalse(first.getStyleClass().contains("m3-segmented-button-first"));
        assertTrue(third.getStyleClass().contains("m3-segmented-button-single"));
    }

    /// Verifies that segmented button groups keep logical edge style classes for right-to-left painting.
    @Test
    void segmentedButtonGroupKeepsLogicalPositionStyleClassesForRightToLeft() {
        M3SegmentedButton first = new M3SegmentedButton("Day");
        M3SegmentedButton second = new M3SegmentedButton("Week");
        M3SegmentedButton third = new M3SegmentedButton("Month");
        M3SegmentedButtonGroup group = segmentedButtonGroup(first, second, third);

        group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        assertTrue(first.getStyleClass().contains("m3-segmented-button-first"));
        assertTrue(second.getStyleClass().contains("m3-segmented-button-middle"));
        assertTrue(third.getStyleClass().contains("m3-segmented-button-last"));

        group.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        assertTrue(first.getStyleClass().contains("m3-segmented-button-first"));
        assertTrue(third.getStyleClass().contains("m3-segmented-button-last"));
    }

    /// Verifies that segmented button surfaces and state layers follow segment position shapes.
    @Test
    void segmentedButtonGroupUsesPositionSpecificShapes() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup group = segmentedButtonGroup(day, week, month);
            Pane root = new Pane(group);
            Scene scene = new Scene(root, 320.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            month.setSelected(true);
            week.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            root.applyCss();
            root.layout();
            group.layout();
            day.layout();
            week.layout();
            month.layout();
            root.applyCss();

            assertRegionRoundedCorners(day, true, false, false, true);
            assertRegionRoundedCorners(week, false, false, false, false);
            assertRegionRoundedCorners(month, false, true, true, false);
            assertStateLayerRadii(day, 20.0, 0.0, 0.0, 20.0);
            assertStateLayerRadii(week, 0.0, 0.0, 0.0, 0.0);
            assertStateLayerRadii(month, 0.0, 20.0, 20.0, 0.0);
        });
    }

    /// Verifies that segmented button corners and feedback layers keep logical CSS shapes in right-to-left layout.
    @Test
    void segmentedButtonGroupUsesRightToLeftPositionSpecificShapes() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup group = segmentedButtonGroup(day, week, month);
            group.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            Pane root = new Pane(group);
            Scene scene = new Scene(root, 320.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            month.setSelected(true);
            week.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            root.applyCss();
            root.layout();
            group.layout();
            day.layout();
            week.layout();
            month.layout();
            root.applyCss();

            assertRegionRoundedCorners(day, true, false, false, true);
            assertRegionRoundedCorners(week, false, false, false, false);
            assertRegionRoundedCorners(month, false, true, true, false);
            assertStateLayerRadii(day, 20.0, 0.0, 0.0, 20.0);
            assertStateLayerRadii(week, 0.0, 0.0, 0.0, 0.0);
            assertStateLayerRadii(month, 0.0, 20.0, 20.0, 0.0);
            assertRegionRadii(segmentedButtonSelectionContainer(month), 0.0, 19.0, 19.0, 0.0);
        });
    }

    /// Verifies that segmented button selected containers animate between selected states.
    @Tier2Test
    @Test
    void segmentedButtonSelectionContainersAnimateBetweenStates() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Parent> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SegmentedButton> dayReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SegmentedButton> monthReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> {
                M3SegmentedButton day = createSegmentedButton("Day", true);
                M3SegmentedButton week = new M3SegmentedButton("Week");
                M3SegmentedButton month = new M3SegmentedButton("Month");
                M3SegmentedButtonGroup group = segmentedButtonGroup(day, week, month);
                group.setPrefSize(240.0, 40.0);
                FlowPane root = new FlowPane(group);
                root.setStyle("-fx-background-color: white; " + visualTestColors());
                Scene scene = new Scene(root, 280.0, 80.0);
                Stage stage = new Stage();

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, true);
                FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(280.0, 80.0);
                root.layout();

                stageReference.set(stage);
                rootReference.set(root);
                dayReference.set(day);
                monthReference.set(month);
                group.select(month);
            });

            FxTestUtils.runOnFxThreadWhen(
                    () -> segmentedButtonSelectionIsTransitioning(rootReference, dayReference, monthReference),
                    () -> {
                    },
                    () -> {
                        Parent root = Objects.requireNonNull(rootReference.get(), "root");
                        M3SegmentedButton day = Objects.requireNonNull(dayReference.get(), "day");
                        M3SegmentedButton month = Objects.requireNonNull(monthReference.get(), "month");
                        Region outgoingSelection = segmentedButtonSelectionContainer(day);
                        Region incomingSelection = segmentedButtonSelectionContainer(month);
                        Region outgoingIndicator = segmentedButtonSelectionIndicator(day);
                        Region incomingIndicator = segmentedButtonSelectionIndicator(month);

                        assertBetween(outgoingSelection.getOpacity(), 0.0, 1.0, "outgoing segment selection opacity");
                        assertBetween(incomingSelection.getOpacity(), 0.25, 1.0, "incoming segment selection opacity");
                        assertBetween(incomingSelection.getScaleX(), 0.96, 1.0, "incoming segment selection scale");
                        assertBetween(outgoingIndicator.getOpacity(), 0.0, 1.0, "outgoing selection icon opacity");
                        assertBetween(outgoingIndicator.getScaleX(), 0.8, 1.0, "outgoing selection icon scale");
                        assertBetween(incomingIndicator.getOpacity(), 0.0, 1.0, "incoming selection icon opacity");
                        assertBetween(incomingIndicator.getScaleX(), 0.8, 1.0, "incoming selection icon scale");
                        assertRegionRadii(outgoingSelection, 19.0, 0.0, 0.0, 19.0);
                        assertRegionRadii(incomingSelection, 0.0, 19.0, 19.0, 0.0);

                        WritableImage image = snapshotImageOnFxThread(root);
                        assertSnapshotNodeContainsContrast(image, incomingSelection, Color.WHITE, 0.03);
                        assertSnapshotNodeBorderContainsContrast(image, month, Color.WHITE, 0.08);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-segmented-selection-animation-frame.png"
                        ));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> segmentedButtonSelectionHasSettled(rootReference, dayReference, monthReference),
                    () -> {
                    },
                    () -> {
                        M3SegmentedButton day = Objects.requireNonNull(dayReference.get(), "day");
                        M3SegmentedButton month = Objects.requireNonNull(monthReference.get(), "month");
                        Region outgoingSelection = segmentedButtonSelectionContainer(day);
                        Region incomingSelection = segmentedButtonSelectionContainer(month);
                        Region outgoingIndicator = segmentedButtonSelectionIndicator(day);
                        Region incomingIndicator = segmentedButtonSelectionIndicator(month);

                        assertEquals(0.0, outgoingSelection.getOpacity(), 0.0001);
                        assertEquals(1.0, incomingSelection.getOpacity(), 0.0001);
                        assertEquals(0.96, outgoingSelection.getScaleX(), 0.0001);
                        assertEquals(1.0, incomingSelection.getScaleX(), 0.0001);
                        assertEquals(0.0, outgoingIndicator.getOpacity(), 0.0001);
                        assertEquals(0.8, outgoingIndicator.getScaleX(), 0.0001);
                        assertEquals(1.0, incomingIndicator.getOpacity(), 0.0001);
                        assertEquals(1.0, incomingIndicator.getScaleX(), 0.0001);
                    }
            );
        } finally {
            closeObservableMotionWindow(stageReference, rootReference);
        }
    }

    /// Verifies that tab component token properties are styleable from CSS.
    @Test
    void tabTokensAreStyleable() {
        M3Tab tab = createTab("Overview", true);
        tab.setStyle(
                "-m3-container-height: 52px; "
                        + "-m3-tab-min-width: 120px; "
                        + "-m3-horizontal-padding: 20px; "
                        + "-m3-active-indicator-height: 4px; "
                        + "-m3-active-indicator-shape: 2px; "
                        + "-m3-active-indicator-min-width: 28px; "
                        + "-m3-active-indicator-horizontal-inset: 3px;"
        );

        applyCss(tab);

        assertTrue(tab.isSelected());
        assertEquals(52.0, tab.getContainerHeight(), 0.0001);
        assertEquals(120.0, tab.getTabMinWidth(), 0.0001);
        assertEquals(20.0, tab.getHorizontalPadding(), 0.0001);
        assertEquals(4.0, tab.getActiveIndicatorHeight(), 0.0001);
        assertEquals(2.0, tab.getActiveIndicatorShape(), 0.0001);
        assertEquals(28.0, tab.getActiveIndicatorMinWidth(), 0.0001);
        assertEquals(3.0, tab.getActiveIndicatorHorizontalInset(), 0.0001);
        assertEquals(120.0, tab.minWidth(-1.0), 0.0001);
        assertEquals(52.0, tab.prefHeight(-1.0), 0.0001);
        assertInstanceOf(M3TabSkin.class, tab.getSkin());
    }

    /// Verifies that tab bars group tabs and keep a selected tab.
    @Test
    void tabBarGroupsTabsAndKeepsSelection() {
        M3Tab overview = new M3Tab("Overview");
        M3Tab details = new M3Tab("Details");
        M3TabBar tabBar = tabBar(overview, details);

        assertTrue(overview.isSelected());
        assertEquals(overview, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(overview), tabBar.getSelectedTabs());
        assertEquals(0, tabBar.getSelectedIndex());

        tabBar.selectIndex(1);

        assertFalse(overview.isSelected());
        assertTrue(details.isSelected());
        assertEquals(details, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(details), tabBar.getSelectedTabs());
        assertEquals(1, tabBar.getSelectedIndex());

        details.fire();

        assertTrue(details.isSelected());
        assertEquals(details, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(details), tabBar.getSelectedTabs());
        assertThrows(UnsupportedOperationException.class, () -> tabBar.getSelectedTabs().add(overview));

        tabBar.selectNext();

        assertEquals(overview, tabBar.getSelectedTab());
        assertTrue(overview.isSelected());
        assertFalse(details.isSelected());

        tabBar.selectPrevious();

        assertEquals(details, tabBar.getSelectedTab());
        assertFalse(overview.isSelected());
        assertTrue(details.isSelected());

        tabBar.clearSelection();

        assertEquals(details, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(details), tabBar.getSelectedTabs());

        tabBar.setAllowEmptySelection(true);
        tabBar.clearSelection();

        assertNull(tabBar.getSelectedTab());
        assertTrue(tabBar.getSelectedTabs().isEmpty());
        assertEquals(-1, tabBar.getSelectedIndex());
        assertFalse(overview.isSelected());
        assertFalse(details.isSelected());

        tabBar.setAllowEmptySelection(false);

        assertEquals(overview, tabBar.getSelectedTab());
        assertEquals(java.util.List.of(overview), tabBar.getSelectedTabs());
        assertTrue(overview.isSelected());
    }

    /// Verifies fixed and scrollable primary and secondary tab geometry, reveal behavior, and feedback.
    @Test
    void tabSkinLaysOutIndicatorAndRipple() {
        M3Tab primaryTab = new M3Tab("Overview");
        M3Tab secondaryTab = new M3Tab("Specifications");
        M3TabBar primaryBar = tabBar(primaryTab, new M3Tab("Activity"));
        M3TabBar secondaryBar = tabBar(secondaryTab, new M3Tab("Reviews"));
        secondaryBar.setVariant(M3TabBarVariant.SECONDARY);
        M3Tab firstScrollableTab = new M3Tab("Overview");
        M3Tab longScrollableTab = new M3Tab("Technical specifications");
        M3Tab lastScrollableTab = new M3Tab("Connected applications");
        M3TabBar scrollableBar = tabBar(
                firstScrollableTab,
                new M3Tab("Recent activity"),
                longScrollableTab,
                new M3Tab("Storage management"),
                lastScrollableTab
        );
        scrollableBar.setTabLayout(M3TabBarLayout.SCROLLABLE);
        VBox root = new VBox(primaryBar, secondaryBar, scrollableBar);
        Scene scene = new Scene(root, 360.0, 144.0);

        M3Theme theme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, theme);
        root.applyCss();
        root.layout();

        PseudoClass secondaryPseudoClass = PseudoClass.getPseudoClass("secondary");
        assertEquals(M3TabBarVariant.PRIMARY, primaryBar.getVariant());
        assertEquals(M3TabBarVariant.SECONDARY, secondaryBar.getVariant());
        assertFalse(primaryTab.getPseudoClassStates().contains(secondaryPseudoClass));
        assertTrue(secondaryBar.getPseudoClassStates().contains(secondaryPseudoClass));
        assertTrue(secondaryTab.getPseudoClassStates().contains(secondaryPseudoClass));
        assertEquals(
                primaryTab.getWidth(),
                assertInstanceOf(M3Tab.class, primaryBar.getTabs().get(1)).getWidth(),
                0.0001
        );
        assertEquals(
                secondaryTab.getWidth(),
                assertInstanceOf(M3Tab.class, secondaryBar.getTabs().get(1)).getWidth(),
                0.0001
        );
        assertEquals(primaryBar.getWidth() / 2.0, primaryTab.getWidth(), 0.0001);
        assertEquals(secondaryBar.getWidth() / 2.0, secondaryTab.getWidth(), 0.0001);
        assertEquals(M3TabBarLayout.FIXED, primaryBar.getTabLayout());
        assertEquals(M3TabBarLayout.SCROLLABLE, scrollableBar.getTabLayout());
        assertTrue(scrollableBar.getPseudoClassStates().contains(PseudoClass.getPseudoClass("scrollable")));
        assertEquals(52.0, firstScrollableTab.getLayoutX(), 0.75);
        assertTrue(longScrollableTab.getWidth() > firstScrollableTab.getWidth(),
                "scrollable tab widths should follow their labels");
        assertTrue(lastScrollableTab.getLayoutX() >= scrollableBar.getWidth(),
                "the overflowing final tab should begin outside the initial viewport");
        for (Node node : scrollableBar.getTabs()) {
            M3Tab tab = assertInstanceOf(M3Tab.class, node);
            Text label = assertInstanceOf(Text.class, tab.lookup(".text"));
            assertEquals(tab.getText(), label.getText(), () -> "scrollable tab labels should remain complete: text="
                    + tab.getText() + ", width=" + tab.getWidth() + ", prefWidth=" + tab.prefWidth(-1.0)
                    + ", minWidth=" + tab.minWidth(-1.0) + ", padding=" + tab.getPadding());
        }

        Region primaryIndicator = lookupRegion(primaryTab, ".m3-tab-active-indicator");
        Region secondaryIndicator = lookupRegion(secondaryTab, ".m3-tab-active-indicator");
        Text primaryText = assertInstanceOf(Text.class, primaryTab.lookup(".text"));
        double expectedPrimaryIndicatorWidth = Math.max(
                primaryTab.getActiveIndicatorMinWidth(),
                primaryText.getBoundsInParent().getWidth()
                        + primaryTab.getActiveIndicatorHorizontalInset() * 2.0
        );
        assertEquals(expectedPrimaryIndicatorWidth, primaryIndicator.getWidth(), 1.0);
        assertEquals(
                primaryText.getBoundsInParent().getCenterX(),
                primaryIndicator.getBoundsInParent().getCenterX(),
                0.0001
        );
        assertTrue(primaryIndicator.getWidth() < primaryTab.getWidth());
        assertEquals(3.0, primaryIndicator.getHeight(), 0.0001);
        assertEquals(3.0, primaryTab.getActiveIndicatorShape(), 0.0001);
        assertEquals(1.0, primaryIndicator.getOpacity(), 0.0001);
        assertEquals(secondaryTab.getWidth(), secondaryIndicator.getWidth(), 0.0001);
        assertEquals(2.0, secondaryIndicator.getHeight(), 0.0001);
        assertEquals(0.0, secondaryTab.getActiveIndicatorShape(), 0.0001);
        assertEquals(1.0, secondaryIndicator.getOpacity(), 0.0001);
        assertEquals(
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY),
                primaryTab.getTextFill()
        );
        assertEquals(
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE),
                secondaryTab.getTextFill()
        );

        Region primaryDivider = lookupRegion(primaryBar, "." + "m3-tab-bar-divider");
        Region secondaryDivider = lookupRegion(secondaryBar, "." + "m3-tab-bar-divider");
        assertEquals(1.0, primaryDivider.getHeight(), 0.0001);
        assertEquals(1.0, secondaryDivider.getHeight(), 0.0001);
        assertEquals(primaryBar.getWidth(), primaryDivider.getWidth(), 0.0001);
        assertEquals(secondaryBar.getWidth(), secondaryDivider.getWidth(), 0.0001);

        secondaryTab.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 40.0, 24.0, true));
        assertTrue(secondaryTab.isArmed());
        secondaryTab.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 40.0, 24.0, false));

        assertTrue(lookupRegion(secondaryTab, ".m3-ripple").getOpacity() > 0.0);

        secondaryBar.setVariant(M3TabBarVariant.PRIMARY);
        assertFalse(secondaryBar.getPseudoClassStates().contains(secondaryPseudoClass));
        assertFalse(secondaryTab.getPseudoClassStates().contains(secondaryPseudoClass));
        secondaryBar.setVariant(M3TabBarVariant.SECONDARY);
        assertTrue(secondaryBar.getPseudoClassStates().contains(secondaryPseudoClass));
        assertTrue(secondaryTab.getPseudoClassStates().contains(secondaryPseudoClass));

        FxTestUtils.setCssAnimationsEnabled(root, false);
        scrollableBar.select(lastScrollableTab);
        root.layout();
        root.layout();
        assertTrue(lastScrollableTab.getLayoutX() + lastScrollableTab.getWidth() <= scrollableBar.getWidth() + 0.75,
                "selection should reveal the complete final tab");

        scrollableBar.select(firstScrollableTab);
        root.layout();
        root.layout();
        assertEquals(52.0, firstScrollableTab.getLayoutX(), 0.75,
                "returning to the first tab should restore the Material leading offset");
        scrollableBar.fireEvent(scrollEvent(scrollableBar, 0.0, -36.0, false, false));
        root.layout();
        assertEquals(16.0, firstScrollableTab.getLayoutX(), 0.75,
                "vertical wheel movement should browse the horizontal tab track");

        scrollableBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        scrollableBar.select(lastScrollableTab);
        scrollableBar.select(firstScrollableTab);
        root.layout();
        root.layout();
        Bounds firstRtlBounds = firstScrollableTab.localToScene(firstScrollableTab.getBoundsInLocal());
        Bounds scrollableBarBounds = scrollableBar.localToScene(scrollableBar.getBoundsInLocal());
        assertEquals(scrollableBarBounds.getMaxX() - 52.0, firstRtlBounds.getMaxX(), 0.75,
                "RTL scrollable tabs should apply the leading offset from the right edge");
        FxTestUtils.setCssAnimationsEnabled(root, true);
    }

    /// Verifies that selection containers handle keyboard navigation and skip disabled children.
    @Test
    void selectionContainersHandleKeyboardNavigationAndSkipDisabledChildren() {
        M3IconToggleButton iconFirst = new M3IconToggleButton("A");
        M3IconToggleButton iconSecond = new M3IconToggleButton("B");
        M3IconToggleButton iconThird = new M3IconToggleButton("C");
        iconSecond.setDisable(true);
        M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond, iconThird);

        iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(iconFirst, iconGroup.getSelectedButton());
        iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(iconThird, iconGroup.getSelectedButton());

        M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
        M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
        segmentSecond.setDisable(true);
        M3SegmentedButtonGroup segmentedGroup =
                segmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);

        segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());
        segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(segmentThird, segmentedGroup.getSelectedButton());
        segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());

        M3SelectableChip chipFirst = createInputChip("Input");
        M3SelectableChip chipSecond = createFilterChip("Filter");
        M3SelectableChip chipThird = createFilterChip("Assist");
        chipSecond.setDisable(true);
        M3ChipGroup chipGroup = chipGroup(chipFirst, chipSecond, chipThird);
        chipGroup.setSelectionMode(M3SelectionMode.SINGLE);

        chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(chipFirst, chipGroup.getSelectedChip());
        chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(chipThird, chipGroup.getSelectedChip());

        M3Tab tabFirst = new M3Tab("Overview");
        M3Tab tabSecond = new M3Tab("Details");
        M3Tab tabThird = new M3Tab("Activity");
        tabSecond.setDisable(true);
        M3TabBar tabBar = tabBar(tabFirst, tabSecond, tabThird);

        tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(tabThird, tabBar.getSelectedTab());
        tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
        assertEquals(tabThird, tabBar.getSelectedTab());

        M3NavigationItem navFirst = new M3NavigationItem("Home");
        M3NavigationItem navSecond = new M3NavigationItem("Search");
        M3NavigationItem navThird = new M3NavigationItem("Inbox");
        navSecond.setDisable(true);
        M3NavigationBar navigationBar = navigationBar(navFirst, navSecond, navThird);

        navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(navThird, navigationBar.getSelectedItem());

        M3NavigationItem railFirst = new M3NavigationItem("Home");
        M3NavigationItem railSecond = new M3NavigationItem("Search");
        M3NavigationItem railThird = new M3NavigationItem("Inbox");
        railSecond.setDisable(true);
        M3NavigationRail navigationRail = navigationRail(railFirst, railSecond, railThird);
        navigationRail.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(railThird, navigationRail.getSelectedItem());

        M3ListItem drawerFirst = new M3ListItem("Home");
        M3ListItem drawerSecond = new M3ListItem("Search");
        M3ListItem drawerThird = new M3ListItem("Inbox");
        drawerSecond.setDisable(true);
        M3NavigationDrawer navigationDrawer = navigationDrawer(drawerFirst, drawerSecond, drawerThird);

        navigationDrawer.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(drawerThird, navigationDrawer.getSelectedItem());

        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        listFirst.setDisable(true);
        M3ListPane list = listPane(listFirst, listSecond);
        list.setSelectionMode(M3SelectionMode.SINGLE);

        list.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(listSecond, list.getSelectedItem());

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        menuFirst.setDisable(true);
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3SelectionMode.SINGLE);

        menu.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(menuSecond, menu.getSelectedItem());

        M3ListPane listWithoutSelection = listPane(new M3ListItem("Action"));
        KeyEvent listFocusEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);
        assertFalse(M3SelectionNavigation.handleKeyFocus(
                listFocusEvent,
                null,
                listWithoutSelection.getItems(),
                null,
                M3ListItem.class,
                false,
                true
        ));
        assertFalse(listFocusEvent.isConsumed());
        assertNull(listWithoutSelection.getSelectedItem());

        M3Menu menuWithoutSelection = new M3Menu(new M3MenuItem("Action"));
        KeyEvent menuFocusEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);
        assertFalse(M3SelectionNavigation.handleKeyFocus(
                menuFocusEvent,
                null,
                menuWithoutSelection.getItems(),
                null,
                M3MenuItem.class,
                false,
                true
        ));
        assertFalse(menuFocusEvent.isConsumed());
        assertNull(menuWithoutSelection.getSelectedItem());
    }

    /// Verifies that grouped selection containers reject disabled and hidden selection targets.
    @Test
    void groupedSelectionContainersRejectUnreachableSelectionTargets() {
        M3IconToggleButton iconDisabled = new M3IconToggleButton("Disabled");
        M3IconToggleButton iconHidden = new M3IconToggleButton("Hidden");
        M3IconToggleButton iconFirst = new M3IconToggleButton("First");
        M3IconToggleButton iconSecond = new M3IconToggleButton("Second");
        iconDisabled.setDisable(true);
        iconHidden.setVisible(false);
        M3IconToggleButtonGroup iconGroup =
                iconToggleButtonGroup(iconDisabled, iconHidden, iconFirst, iconSecond);
        iconGroup.setSelectionMode(M3SelectionMode.SINGLE);
        iconGroup.setAllowEmptySelection(false);

        assertEquals(iconFirst, iconGroup.getSelectedButton());
        assertThrows(IllegalArgumentException.class, () -> iconGroup.select(iconDisabled));
        assertThrows(IllegalArgumentException.class, () -> iconGroup.select(iconHidden));
        assertThrows(IllegalArgumentException.class, () -> iconGroup.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> iconGroup.selectIndex(1));
        iconDisabled.setSelected(true);
        iconHidden.setSelected(true);
        assertFalse(iconDisabled.isSelected());
        assertFalse(iconHidden.isSelected());
        assertEquals(iconFirst, iconGroup.getSelectedButton());
        iconFirst.setDisable(true);
        assertFalse(iconFirst.isSelected());
        assertEquals(iconSecond, iconGroup.getSelectedButton());

        M3SegmentedButton segmentDisabled = new M3SegmentedButton("Disabled");
        M3SegmentedButton segmentHidden = new M3SegmentedButton("Hidden");
        M3SegmentedButton segmentFirst = new M3SegmentedButton("First");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Second");
        segmentDisabled.setDisable(true);
        segmentHidden.setVisible(false);
        M3SegmentedButtonGroup segmentedGroup =
                segmentedButtonGroup(segmentDisabled, segmentHidden, segmentFirst, segmentSecond);
        segmentedGroup.setSelectionMode(M3SelectionMode.SINGLE);
        segmentedGroup.setAllowEmptySelection(false);

        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());
        assertThrows(IllegalArgumentException.class, () -> segmentedGroup.select(segmentDisabled));
        assertThrows(IllegalArgumentException.class, () -> segmentedGroup.select(segmentHidden));
        assertThrows(IllegalArgumentException.class, () -> segmentedGroup.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> segmentedGroup.selectIndex(1));
        segmentDisabled.setSelected(true);
        segmentHidden.setSelected(true);
        assertFalse(segmentDisabled.isSelected());
        assertFalse(segmentHidden.isSelected());
        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());
        segmentFirst.setDisable(true);
        assertFalse(segmentFirst.isSelected());
        assertEquals(segmentSecond, segmentedGroup.getSelectedButton());

        M3SelectableChip chipDisabled = createFilterChip("Disabled");
        M3SelectableChip chipHidden = createFilterChip("Hidden");
        M3SelectableChip chipFirst = createFilterChip("First");
        M3SelectableChip chipSecond = createFilterChip("Second");
        chipDisabled.setDisable(true);
        chipHidden.setVisible(false);
        M3ChipGroup chipGroup = chipGroup(chipDisabled, chipHidden, chipFirst, chipSecond);
        chipGroup.setSelectionMode(M3SelectionMode.SINGLE);
        chipGroup.setAllowEmptySelection(false);

        assertEquals(chipFirst, chipGroup.getSelectedChip());
        assertThrows(IllegalArgumentException.class, () -> chipGroup.select(chipDisabled));
        assertThrows(IllegalArgumentException.class, () -> chipGroup.select(chipHidden));
        assertThrows(IllegalArgumentException.class, () -> chipGroup.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> chipGroup.selectIndex(1));
        chipDisabled.setSelected(true);
        chipHidden.setSelected(true);
        assertFalse(chipDisabled.isSelected());
        assertFalse(chipHidden.isSelected());
        assertEquals(chipFirst, chipGroup.getSelectedChip());
        chipFirst.setDisable(true);
        assertFalse(chipFirst.isSelected());
        assertEquals(chipSecond, chipGroup.getSelectedChip());

        M3Tab tabDisabled = new M3Tab("Disabled");
        M3Tab tabHidden = new M3Tab("Hidden");
        M3Tab tabFirst = new M3Tab("First");
        M3Tab tabSecond = new M3Tab("Second");
        tabDisabled.setDisable(true);
        tabHidden.setVisible(false);
        M3TabBar tabBar = tabBar(tabDisabled, tabHidden, tabFirst, tabSecond);
        tabBar.setAllowEmptySelection(false);

        assertEquals(tabFirst, tabBar.getSelectedTab());
        assertThrows(IllegalArgumentException.class, () -> tabBar.select(tabDisabled));
        assertThrows(IllegalArgumentException.class, () -> tabBar.select(tabHidden));
        assertThrows(IllegalArgumentException.class, () -> tabBar.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> tabBar.selectIndex(1));
        tabDisabled.setSelected(true);
        tabHidden.setSelected(true);
        assertFalse(tabDisabled.isSelected());
        assertFalse(tabHidden.isSelected());
        assertEquals(tabFirst, tabBar.getSelectedTab());
        tabFirst.setDisable(true);
        assertFalse(tabFirst.isSelected());
        assertEquals(tabSecond, tabBar.getSelectedTab());

        M3NavigationItem barDisabled = new M3NavigationItem("Disabled");
        M3NavigationItem barHidden = new M3NavigationItem("Hidden");
        M3NavigationItem barFirst = new M3NavigationItem("First");
        M3NavigationItem barSecond = new M3NavigationItem("Second");
        barDisabled.setDisable(true);
        barHidden.setVisible(false);
        M3NavigationBar navigationBar = navigationBar(barDisabled, barHidden, barFirst, barSecond);
        navigationBar.setAllowEmptySelection(false);

        assertEquals(barFirst, navigationBar.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> navigationBar.select(barDisabled));
        assertThrows(IllegalArgumentException.class, () -> navigationBar.select(barHidden));
        assertThrows(IllegalArgumentException.class, () -> navigationBar.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> navigationBar.selectIndex(1));
        barDisabled.setSelected(true);
        barHidden.setSelected(true);
        assertFalse(barDisabled.isSelected());
        assertFalse(barHidden.isSelected());
        assertEquals(barFirst, navigationBar.getSelectedItem());
        barFirst.setDisable(true);
        assertFalse(barFirst.isSelected());
        assertEquals(barSecond, navigationBar.getSelectedItem());

        M3NavigationItem railDisabled = new M3NavigationItem("Disabled");
        M3NavigationItem railHidden = new M3NavigationItem("Hidden");
        M3NavigationItem railFirst = new M3NavigationItem("First");
        M3NavigationItem railSecond = new M3NavigationItem("Second");
        railDisabled.setDisable(true);
        railHidden.setVisible(false);
        M3NavigationRail navigationRail = navigationRail(railDisabled, railHidden, railFirst, railSecond);
        navigationRail.setAllowEmptySelection(false);

        assertEquals(railFirst, navigationRail.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> navigationRail.select(railDisabled));
        assertThrows(IllegalArgumentException.class, () -> navigationRail.select(railHidden));
        assertThrows(IllegalArgumentException.class, () -> navigationRail.selectIndex(0));
        assertThrows(IllegalArgumentException.class, () -> navigationRail.selectIndex(1));
        railDisabled.setSelected(true);
        railHidden.setSelected(true);
        assertFalse(railDisabled.isSelected());
        assertFalse(railHidden.isSelected());
        assertEquals(railFirst, navigationRail.getSelectedItem());
        railFirst.setDisable(true);
        assertFalse(railFirst.isSelected());
        assertEquals(railSecond, navigationRail.getSelectedItem());
    }

    /// Verifies that navigation routes retain model selection while interactive groups reject unreachable targets.
    @Test
    void groupedSelectionContainersDistinguishModelSelectionFromReachability() {
        M3IconToggleButton iconFirst = new M3IconToggleButton("First");
        M3IconToggleButton iconSecond = new M3IconToggleButton("Second");
        M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond);
        iconGroup.setSelectionMode(M3SelectionMode.SINGLE);
        iconGroup.setAllowEmptySelection(false);

        M3SegmentedButton segmentFirst = new M3SegmentedButton("First");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Second");
        M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(segmentFirst, segmentSecond);
        segmentedGroup.setSelectionMode(M3SelectionMode.SINGLE);
        segmentedGroup.setAllowEmptySelection(false);

        M3SelectableChip chipFirst = createFilterChip("First");
        M3SelectableChip chipSecond = createFilterChip("Second");
        M3ChipGroup chipGroup = chipGroup(chipFirst, chipSecond);
        chipGroup.setSelectionMode(M3SelectionMode.SINGLE);
        chipGroup.setAllowEmptySelection(false);

        M3Tab tabFirst = new M3Tab("First");
        M3Tab tabSecond = new M3Tab("Second");
        M3TabBar tabBar = tabBar(tabFirst, tabSecond);
        tabBar.setAllowEmptySelection(false);

        M3NavigationItem barFirst = new M3NavigationItem("First");
        M3NavigationItem barSecond = new M3NavigationItem("Second");
        M3NavigationBar navigationBar = navigationBar(barFirst, barSecond);
        navigationBar.setAllowEmptySelection(false);

        M3NavigationItem railFirst = new M3NavigationItem("First");
        M3NavigationItem railSecond = new M3NavigationItem("Second");
        M3NavigationRail navigationRail = navigationRail(railFirst, railSecond);
        navigationRail.setAllowEmptySelection(false);

        M3ListItem listFirst = new M3ListItem("First");
        M3ListItem listSecond = new M3ListItem("Second");
        M3ListPane list = listPane(listFirst, listSecond);
        list.setSelectionMode(M3SelectionMode.SINGLE);
        list.setAllowEmptySelection(false);

        M3MenuItem menuFirst = new M3MenuItem("First");
        M3MenuItem menuSecond = new M3MenuItem("Second");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.setAllowEmptySelection(false);

        M3Carousel carousel = carousel(new Label("First"), new Label("Second"));
        carousel.clearSelection();

        Pane hiddenAncestor = new Pane(
                iconGroup,
                segmentedGroup,
                chipGroup,
                tabBar,
                navigationBar,
                navigationRail,
                list,
                menu,
                carousel
        );
        hiddenAncestor.setVisible(false);

        assertThrows(IllegalArgumentException.class, () -> iconGroup.select(iconSecond));
        assertThrows(IllegalArgumentException.class, () -> segmentedGroup.select(segmentSecond));
        assertThrows(IllegalArgumentException.class, () -> chipGroup.select(chipSecond));
        assertThrows(IllegalArgumentException.class, () -> tabBar.select(tabSecond));
        navigationBar.select(barSecond);
        navigationRail.select(railSecond);
        assertEquals(barSecond, navigationBar.getSelectedItem());
        assertEquals(railSecond, navigationRail.getSelectedItem());
        assertThrows(IllegalArgumentException.class, () -> list.select(listSecond));
        assertThrows(IllegalArgumentException.class, () -> menu.select(menuSecond));
        assertThrows(IllegalArgumentException.class, () -> carousel.select(carousel.getItems().get(1)));
        assertThrows(IllegalArgumentException.class, () -> carousel.selectIndex(1));

        iconGroup.selectNext();
        segmentedGroup.selectNext();
        chipGroup.selectNext();
        tabBar.selectNext();
        navigationBar.selectNext();
        navigationRail.selectNext();
        list.selectNext();
        menu.selectNext();
        carousel.selectFirst();

        assertEquals(iconFirst, iconGroup.getSelectedButton());
        assertEquals(segmentFirst, segmentedGroup.getSelectedButton());
        assertEquals(chipFirst, chipGroup.getSelectedChip());
        assertEquals(tabFirst, tabBar.getSelectedTab());
        assertEquals(barSecond, navigationBar.getSelectedItem());
        assertEquals(railSecond, navigationRail.getSelectedItem());
        assertEquals(listFirst, list.getSelectedItem());
        assertEquals(menuFirst, menu.getSelectedItem());
        assertNull(carousel.getSelectedItem());
    }

    /// Verifies that right-to-left controls mirror horizontal keyboard focus and selection.
    @Test
    void horizontalKeyboardNavigationMirrorsForRightToLeftLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButton groupFirst = new M3IconToggleButton("First");
            M3IconToggleButton groupSecond = new M3IconToggleButton("Second");
            M3IconToggleButton groupThird = new M3IconToggleButton("Third");
            M3ButtonGroup buttonGroup = new M3ButtonGroup();
            buttonGroup.getItems().addAll(groupFirst, groupSecond, groupThird);
            buttonGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButton iconThird = new M3IconToggleButton("C");
            M3IconToggleButtonGroup iconGroup =
                    iconToggleButtonGroup(iconFirst, iconSecond, iconThird);
            iconGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            iconGroup.select(iconSecond);

            M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
            M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segmentedGroup =
                    segmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);
            segmentedGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            segmentedGroup.select(segmentSecond);

            M3SelectableChip chipFirst = createInputChip("Input");
            M3SelectableChip chipSecond = createFilterChip("Filter");
            M3SelectableChip chipThird = createFilterChip("Assist");
            M3ChipGroup chipGroup = chipGroup(chipFirst, chipSecond, chipThird);
            chipGroup.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            chipGroup.setSelectionMode(M3SelectionMode.SINGLE);
            chipGroup.select(chipSecond);

            M3Tab tabFirst = new M3Tab("Overview");
            M3Tab tabSecond = new M3Tab("Details");
            M3Tab tabThird = new M3Tab("Activity");
            M3TabBar tabBar = tabBar(tabFirst, tabSecond, tabThird);
            tabBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            tabBar.select(tabSecond);

            M3NavigationItem navFirst = new M3NavigationItem("Home");
            M3NavigationItem navSecond = new M3NavigationItem("Search");
            M3NavigationItem navThird = new M3NavigationItem("Inbox");
            M3NavigationBar navigationBar = navigationBar(navFirst, navSecond, navThird);
            navigationBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            navigationBar.select(navSecond);

            M3SplitButton splitButton = splitButton("Create", new M3MenuItem("Draft"));
            splitButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Label carouselFirst = new Label("First");
            Label carouselSecond = new Label("Second");
            Label carouselThird = new Label("Third");
            M3Carousel carousel = carousel(carouselFirst, carouselSecond, carouselThird);
            carousel.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            carousel.select(carouselSecond);

            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 15));
            datePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 15),
                    LocalDate.of(2026, 5, 15)
            );
            dateRangePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Stage stage = new Stage();
            try {
                VBox root = new VBox(
                        buttonGroup,
                        iconGroup,
                        segmentedGroup,
                        chipGroup,
                        tabBar,
                        navigationBar,
                        splitButton,
                        carousel,
                        datePicker,
                        dateRangePicker
                );
                Scene scene = new Scene(root, 760.0, 640.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                groupSecond.requestFocus();
                buttonGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(groupFirst.isFocused());
                buttonGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(groupSecond.isFocused());

                iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(iconFirst, iconGroup.getSelectedButton());
                iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(iconSecond, iconGroup.getSelectedButton());

                segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(segmentFirst, segmentedGroup.getSelectedButton());
                segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(segmentSecond, segmentedGroup.getSelectedButton());

                chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(chipFirst, chipGroup.getSelectedChip());
                chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(chipSecond, chipGroup.getSelectedChip());

                tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(tabFirst, tabBar.getSelectedTab());
                tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(tabSecond, tabBar.getSelectedTab());

                navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(navFirst, navigationBar.getSelectedItem());
                navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(navSecond, navigationBar.getSelectedItem());

                splitButtonMenuButton(splitButton).requestFocus();
                splitButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(splitButtonActionButton(splitButton).isFocused());
                splitButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(splitButtonMenuButton(splitButton).isFocused());

                carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertSame(carouselFirst, carousel.getSelectedItem());
                carousel.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertSame(carouselSecond, carousel.getSelectedItem());

                datePicker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(LocalDate.of(2026, 5, 14), datePicker.getValue());
                datePicker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertEquals(LocalDate.of(2026, 5, 15), datePicker.getValue());

                dateRangePicker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(LocalDate.of(2026, 5, 14), dateRangePicker.getStartDate());
                assertNull(dateRangePicker.getEndDate());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that single-selection containers use the focused child as their keyboard navigation anchor.
    @Test
    void singleSelectionContainersUseFocusedChildAsKeyboardAnchor() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButton iconThird = new M3IconToggleButton("C");
            M3IconToggleButtonGroup iconGroup =
                    iconToggleButtonGroup(iconFirst, iconSecond, iconThird);
            iconGroup.select(iconFirst);

            M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
            M3SegmentedButton segmentThird = new M3SegmentedButton("Month");
            M3SegmentedButtonGroup segmentedGroup =
                    segmentedButtonGroup(segmentFirst, segmentSecond, segmentThird);
            segmentedGroup.select(segmentFirst);

            M3SelectableChip chipFirst = createInputChip("Input");
            M3SelectableChip chipSecond = createFilterChip("Filter");
            M3SelectableChip chipThird = createFilterChip("Assist");
            M3ChipGroup chipGroup = chipGroup(chipFirst, chipSecond, chipThird);
            chipGroup.setSelectionMode(M3SelectionMode.SINGLE);
            chipGroup.select(chipFirst);

            M3ListItem listFirst = new M3ListItem("One");
            M3ListItem listSecond = new M3ListItem("Two");
            M3ListItem listThird = new M3ListItem("Three");
            M3ListPane list = listPane(listFirst, listSecond, listThird);
            list.setSelectionMode(M3SelectionMode.SINGLE);
            list.select(listFirst);

            M3Tab tabFirst = new M3Tab("Overview");
            M3Tab tabSecond = new M3Tab("Details");
            M3Tab tabThird = new M3Tab("Activity");
            M3TabBar tabBar = tabBar(tabFirst, tabSecond, tabThird);
            tabBar.select(tabFirst);

            M3NavigationItem barFirst = new M3NavigationItem("Home");
            M3NavigationItem barSecond = new M3NavigationItem("Search");
            M3NavigationItem barThird = new M3NavigationItem("Inbox");
            M3NavigationBar navigationBar = navigationBar(barFirst, barSecond, barThird);
            navigationBar.select(barFirst);

            M3NavigationItem railFirst = new M3NavigationItem("Home");
            M3NavigationItem railSecond = new M3NavigationItem("Search");
            M3NavigationItem railThird = new M3NavigationItem("Inbox");
            M3NavigationRail navigationRail = navigationRail(railFirst, railSecond, railThird);
            navigationRail.select(railFirst);

            Stage stage = new Stage();
            try {
                VBox root = new VBox(iconGroup, segmentedGroup, chipGroup, list, tabBar, navigationBar, navigationRail);
                Scene scene = new Scene(root, 720.0, 520.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                iconSecond.requestFocus();
                iconGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(iconThird, iconGroup.getSelectedButton());

                segmentSecond.requestFocus();
                segmentedGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(segmentThird, segmentedGroup.getSelectedButton());

                chipSecond.requestFocus();
                chipGroup.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(chipThird, chipGroup.getSelectedChip());

                listSecond.requestFocus();
                list.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertEquals(listThird, list.getSelectedItem());

                tabSecond.requestFocus();
                tabBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(tabThird, tabBar.getSelectedTab());

                barSecond.requestFocus();
                navigationBar.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertEquals(barThird, navigationBar.getSelectedItem());

                railSecond.requestFocus();
                navigationRail.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertEquals(railThird, navigationRail.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that selection containers expose a focusable child through accessibility focus routing.
    @Test
    void selectionContainersExposeAccessibleFocusTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3IconToggleButton iconFirst = new M3IconToggleButton("A");
            M3IconToggleButton iconSecond = new M3IconToggleButton("B");
            M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond);
            iconGroup.select(iconSecond);

            M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
            M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
            M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(segmentFirst, segmentSecond);
            segmentedGroup.select(segmentSecond);

            M3SelectableChip chipFirst = createInputChip("Input");
            M3SelectableChip chipSecond = createFilterChip("Filter");
            M3ChipGroup chipGroup = chipGroup(chipFirst, chipSecond);
            chipGroup.setSelectionMode(M3SelectionMode.SINGLE);
            chipGroup.select(chipSecond);

            M3ListItem listFirst = new M3ListItem("One");
            M3ListItem listSecond = new M3ListItem("Two");
            M3ListPane list = listPane(listFirst, listSecond);
            list.setSelectionMode(M3SelectionMode.SINGLE);
            list.select(listSecond);

            M3ListItem passiveListFirst = new M3ListItem("Passive one");
            M3ListItem passiveListSecond = new M3ListItem("Passive two");
            M3ListPane passiveList = listPane(passiveListFirst, passiveListSecond);

            M3Tab tabFirst = new M3Tab("Overview");
            M3Tab tabSecond = new M3Tab("Details");
            M3TabBar tabBar = tabBar(tabFirst, tabSecond);
            tabBar.select(tabSecond);

            M3NavigationItem barFirst = new M3NavigationItem("Home");
            M3NavigationItem barSecond = new M3NavigationItem("Search");
            M3NavigationBar navigationBar = navigationBar(barFirst, barSecond);
            navigationBar.select(barSecond);

            M3NavigationItem railFirst = new M3NavigationItem("Home");
            M3NavigationItem railSecond = new M3NavigationItem("Search");
            M3NavigationRail navigationRail = navigationRail(railFirst, railSecond);
            navigationRail.select(railSecond);

            M3ListItem drawerFirst = new M3ListItem("Inbox");
            M3ListItem drawerSecond = new M3ListItem("Archive");
            M3NavigationDrawer navigationDrawer = navigationDrawer(drawerFirst, drawerSecond);
            navigationDrawer.select(drawerSecond);

            M3Button outsideFocus = new M3Button("Outside");
            Stage stage = new Stage();
            try {
                VBox root = new VBox(
                        outsideFocus,
                        iconGroup,
                        segmentedGroup,
                        chipGroup,
                        list,
                        passiveList,
                        tabBar,
                        navigationBar,
                        navigationRail,
                        navigationDrawer
                );
                Scene scene = new Scene(root, 760.0, 640.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertAccessibleFocus(iconGroup, iconSecond);
                assertAccessibleFocus(segmentedGroup, segmentSecond);
                assertAccessibleFocus(chipGroup, chipSecond);
                assertAccessibleFocus(list, listSecond);
                assertAccessibleFocus(passiveList, passiveListFirst);
                assertAccessibleFocus(tabBar, tabSecond);
                assertAccessibleFocus(navigationBar, barSecond);
                assertAccessibleFocus(navigationRail, railSecond);
                assertAccessibleFocus(navigationDrawer, drawerSecond);

                assertCurrentAccessibleFocus(iconGroup, iconFirst);
                assertCurrentAccessibleFocus(segmentedGroup, segmentFirst);
                assertCurrentAccessibleFocus(chipGroup, chipFirst);
                assertCurrentAccessibleFocus(list, listFirst);
                assertCurrentAccessibleFocus(passiveList, passiveListSecond);
                assertCurrentAccessibleFocus(tabBar, tabFirst);
                assertCurrentAccessibleFocus(navigationBar, barFirst);
                assertCurrentAccessibleFocus(navigationRail, railFirst);
                assertCurrentAccessibleFocus(navigationDrawer, drawerFirst);

                assertAccessibleShowItemFocus(outsideFocus, iconGroup, iconSecond);
                assertAccessibleShowItemFocus(outsideFocus, segmentedGroup, segmentSecond);
                assertAccessibleShowItemFocus(outsideFocus, chipGroup, chipSecond);
                assertAccessibleShowItemFocus(outsideFocus, list, listSecond);
                assertAccessibleShowItemFocus(outsideFocus, passiveList, passiveListFirst);
                assertAccessibleShowItemFocus(outsideFocus, tabBar, tabSecond);
                assertAccessibleShowItemFocus(outsideFocus, navigationBar, barSecond);
                assertAccessibleShowItemFocus(outsideFocus, navigationRail, railSecond);
                assertAccessibleShowItemFocus(outsideFocus, navigationDrawer, drawerSecond);

                iconSecond.setDisable(true);
                assertAccessibleFocus(iconGroup, iconFirst);
                assertAccessibleShowItemFocus(outsideFocus, iconGroup, iconFirst);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that multi-selection containers use arrow keys for focus movement without changing selection.
    @Test
    void multiSelectionContainersKeepSelectionDuringKeyboardFocusNavigation() {
        M3SelectableChip chipFirst = createInputChip("Input", true);
        M3SelectableChip chipSecond = createFilterChip("Filter", false);
        M3ChipGroup chipGroup = chipGroup(chipFirst, chipSecond);
        KeyEvent chipEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

        chipGroup.fireEvent(chipEvent);

        assertEquals(java.util.List.of(chipFirst), chipGroup.getSelectedChips());
        assertFalse(chipSecond.isSelected());

        M3IconToggleButton iconFirst = createIconToggleButton(
                "edit",
                M3IconToggleButtonVariant.STANDARD,
                true
        );
        M3IconToggleButton iconSecond = createIconToggleButton(
                "done",
                M3IconToggleButtonVariant.STANDARD,
                false
        );
        M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond);
        iconGroup.setSelectionMode(M3SelectionMode.MULTIPLE);
        KeyEvent iconEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

        iconGroup.fireEvent(iconEvent);

        assertEquals(java.util.List.of(iconFirst), iconGroup.getSelectedButtons());
        assertFalse(iconSecond.isSelected());

        M3SegmentedButton segmentFirst = createSegmentedButton("Day", true);
        M3SegmentedButton segmentSecond = createSegmentedButton("Week", false);
        M3SegmentedButtonGroup segmentGroup = segmentedButtonGroup(segmentFirst, segmentSecond);
        segmentGroup.setSelectionMode(M3SelectionMode.MULTIPLE);
        KeyEvent segmentEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT);

        segmentGroup.fireEvent(segmentEvent);

        assertEquals(java.util.List.of(segmentFirst), segmentGroup.getSelectedButtons());
        assertFalse(segmentSecond.isSelected());

        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        M3ListPane list = listPane(listFirst, listSecond);
        list.setSelectionMode(M3SelectionMode.MULTIPLE);
        listFirst.setSelected(true);
        KeyEvent listEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);

        list.fireEvent(listEvent);

        assertEquals(java.util.List.of(listFirst), list.getSelectedItems());
        assertFalse(listSecond.isSelected());

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3SelectionMode.MULTIPLE);
        menuFirst.setSelected(true);
        KeyEvent menuEvent = keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN);

        menu.fireEvent(menuEvent);

        assertEquals(java.util.List.of(menuFirst), menu.getSelectedItems());
        assertFalse(menuSecond.isSelected());
    }

    /// Verifies that a hovered selected segment keeps rounded caps without a square behind its check indicator.
    @Tier2Test
    @Test
    void segmentedButtonSnapshotKeepsSelectedEndRounded() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButton day = new M3SegmentedButton("Day");
            M3SegmentedButton week = new M3SegmentedButton("Week");
            M3SegmentedButton month = createSegmentedButton("Month", true);
            month.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            M3SegmentedButtonGroup group = segmentedButtonGroup(day, week, month);
            group.setPrefSize(240.0, 40.0);
            FlowPane root = new FlowPane(group);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 280.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(280.0, 80.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            var monthBounds = month.localToScene(month.getBoundsInLocal());
            int monthRight = (int) Math.round(monthBounds.getMaxX());
            int monthTop = (int) Math.round(monthBounds.getMinY());
            int monthCenterY = (int) Math.round((monthBounds.getMinY() + monthBounds.getMaxY()) / 2.0);
            Color selectedBody = averageSnapshotColor(
                    image, monthRight - 20, monthCenterY - 3, monthRight - 8, monthCenterY + 4
            );
            Color roundedCorner = averageSnapshotColor(
                    image, monthRight - 4, monthTop, monthRight, monthTop + 5
            );

            assertTrue(selectedBody.getOpacity() > 0.4, () -> "selectedBody=" + selectedBody);
            assertSnapshotNodeBorderContainsContrast(image, month, selectedBody, 0.08);
            assertTrue(roundedCorner.getOpacity() < 0.4
                            || colorDistance(selectedBody, roundedCorner) > 0.1,
                    () -> "selectedBody=" + selectedBody + ", roundedCorner=" + roundedCorner);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-segmented-selected-hover.png"
            ));
        });
    }

    /// Verifies that selection containers expose accessible item collections and selected items.
    @Test
    void selectionContainersExposeAccessibleCollections() {
        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        M3ListPane list = listPane(listFirst, new M3Divider(), listSecond);
        list.setSelectionMode(M3SelectionMode.MULTIPLE);
        listFirst.setSelected(true);
        listSecond.setSelected(true);

        assertEquals(3, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(listFirst, list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertNull(list.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, -1));
        assertEquals(true, list.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(list.getSelectedItems(), list.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.select(menuSecond);

        assertEquals(2, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(menuSecond, menu.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, menu.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(menu.getSelectedItems(), menu.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3SelectableChip firstChip = createInputChip("Input", true);
        M3SelectableChip secondChip = createFilterChip("Filter", false);
        M3ChipGroup chipGroup = chipGroup(firstChip, secondChip);

        assertEquals(2, chipGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(firstChip, chipGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(true, chipGroup.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(chipGroup.getSelectedChips(), chipGroup.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3IconToggleButton iconFirst = createIconToggleButton("edit", M3IconToggleButtonVariant.STANDARD, true);
        M3IconToggleButton iconSecond = createIconToggleButton("done", M3IconToggleButtonVariant.STANDARD, false);
        M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond);

        assertEquals(iconFirst, iconGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(false, iconGroup.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(iconGroup.getSelectedButtons(), iconGroup.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3SegmentedButton segmentFirst = createSegmentedButton("Day", true);
        M3SegmentedButton segmentSecond = createSegmentedButton("Week", false);
        M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(segmentFirst, segmentSecond);

        assertEquals(segmentSecond, segmentedGroup.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, segmentedGroup.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(segmentedGroup.getSelectedButtons(),
                segmentedGroup.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3Tab tabFirst = createTab("Overview", true);
        M3Tab tabSecond = createTab("Details", false);
        M3TabBar tabBar = tabBar(tabFirst, tabSecond);

        assertEquals(tabSecond, tabBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, tabBar.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(tabBar.getSelectedTabs(), tabBar.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3NavigationItem navFirst = createNavigationItem("Home", true);
        M3NavigationItem navSecond = createNavigationItem("Search", false);
        M3NavigationBar navigationBar = navigationBar(navFirst, navSecond);
        M3NavigationRail navigationRail = navigationRail(
                createNavigationItem("Home", true),
                createNavigationItem("Search", false)
        );

        assertEquals(navSecond, navigationBar.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, navigationBar.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(navigationBar.getSelectedItems(),
                navigationBar.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
        assertEquals(2, navigationRail.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(navigationRail.getSelectedItems(),
                navigationRail.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        M3ListItem drawerFirst = new M3ListItem("Inbox");
        M3ListItem drawerSecond = new M3ListItem("Archive");
        M3NavigationDrawer navigationDrawer = navigationDrawer(drawerFirst, drawerSecond);
        navigationDrawer.select(drawerSecond);

        assertEquals(drawerSecond, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(false, navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.MULTIPLE_SELECTION));
        assertEquals(navigationDrawer.getSelectedItems(),
                navigationDrawer.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
    }

    /// Verifies that selection containers apply selected items requested by accessibility clients.
    @Test
    void selectionContainersApplyAccessibleSelectionActions() {
        M3ListItem listFirst = new M3ListItem("One");
        M3ListItem listSecond = new M3ListItem("Two");
        M3ListPane list = listPane(listFirst, listSecond);
        list.setSelectionMode(M3SelectionMode.MULTIPLE);

        list.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, java.util.List.of(listSecond));

        assertFalse(listFirst.isSelected());
        assertTrue(listSecond.isSelected());
        assertEquals(java.util.List.of(listSecond), list.getSelectedItems());

        M3MenuItem menuFirst = new M3MenuItem("Open");
        M3MenuItem menuSecond = new M3MenuItem("Save");
        M3Menu menu = new M3Menu(menuFirst, menuSecond);
        menu.setSelectionMode(M3SelectionMode.SINGLE);

        menu.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, menuSecond);

        assertEquals(menuSecond, menu.getSelectedItem());
        assertFalse(menuFirst.isSelected());
        assertTrue(menuSecond.isSelected());

        M3SelectableChip chipFirst = createInputChip("Input");
        M3SelectableChip chipSecond = createFilterChip("Filter");
        M3ChipGroup chipGroup = chipGroup(chipFirst, chipSecond);

        chipGroup.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, chipFirst, chipSecond);

        assertEquals(java.util.List.of(chipFirst, chipSecond), chipGroup.getSelectedChips());

        M3IconToggleButton iconFirst = new M3IconToggleButton("edit");
        M3IconToggleButton iconSecond = new M3IconToggleButton("done");
        M3IconToggleButtonGroup iconGroup = iconToggleButtonGroup(iconFirst, iconSecond);

        iconGroup.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, java.util.List.of(iconSecond));

        assertEquals(iconSecond, iconGroup.getSelectedButton());

        M3SegmentedButton segmentFirst = new M3SegmentedButton("Day");
        M3SegmentedButton segmentSecond = new M3SegmentedButton("Week");
        M3SegmentedButtonGroup segmentedGroup = segmentedButtonGroup(segmentFirst, segmentSecond);
        segmentedGroup.setSelectionMode(M3SelectionMode.MULTIPLE);

        segmentedGroup.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, segmentFirst, segmentSecond);

        assertEquals(java.util.List.of(segmentFirst, segmentSecond), segmentedGroup.getSelectedButtons());

        M3Tab tabFirst = new M3Tab("Overview");
        M3Tab tabSecond = new M3Tab("Details");
        M3TabBar tabBar = tabBar(tabFirst, tabSecond);

        tabBar.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, tabSecond);

        assertEquals(tabSecond, tabBar.getSelectedTab());

        M3NavigationItem navFirst = new M3NavigationItem("Home");
        M3NavigationItem navSecond = new M3NavigationItem("Search");
        M3NavigationBar navigationBar = navigationBar(navFirst, navSecond);

        navigationBar.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, navSecond);

        assertEquals(navSecond, navigationBar.getSelectedItem());

        M3NavigationItem railFirst = new M3NavigationItem("Home");
        M3NavigationItem railSecond = new M3NavigationItem("Search");
        M3NavigationRail navigationRail = navigationRail(railFirst, railSecond);

        navigationRail.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, railSecond);

        assertEquals(railSecond, navigationRail.getSelectedItem());

        M3ListItem drawerFirst = new M3ListItem("Inbox");
        M3ListItem drawerSecond = new M3ListItem("Archive");
        M3NavigationDrawer navigationDrawer = navigationDrawer(drawerFirst, drawerSecond);

        navigationDrawer.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, drawerSecond);

        assertEquals(drawerSecond, navigationDrawer.getSelectedItem());
    }

    /// Verifies that selection actions ignore hidden or disabled descendant targets.
    @Test
    void selectionContainersIgnoreUnreachableDescendantSelectionTargets() {
        M3Button hiddenAction = new M3Button("Hidden action");
        hiddenAction.setVisible(false);
        M3ListItem hiddenItem = new M3ListItem("Hidden row");
        hiddenItem.setTrailing(hiddenAction);

        M3Button disabledAction = new M3Button("Disabled action");
        disabledAction.setDisable(true);
        M3ListItem disabledItem = new M3ListItem("Disabled row");
        disabledItem.setTrailing(disabledAction);

        M3Button visibleAction = new M3Button("Visible action");
        M3ListItem visibleItem = new M3ListItem("Visible row");
        visibleItem.setTrailing(visibleAction);

        M3ListPane list = listPane(hiddenItem, disabledItem, visibleItem);
        list.setSelectionMode(M3SelectionMode.MULTIPLE);

        list.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, hiddenAction, disabledAction);

        assertTrue(list.getSelectedItems().isEmpty());
        assertFalse(hiddenItem.isSelected());
        assertFalse(disabledItem.isSelected());

        hiddenAction.setVisible(true);
        disabledAction.setDisable(false);
        list.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, hiddenAction, disabledAction, visibleAction);

        assertEquals(java.util.List.of(hiddenItem, disabledItem, visibleItem), list.getSelectedItems());
    }

    /// Returns whether a segmented button selection is rendering an intermediate transition.
    private static boolean segmentedButtonSelectionIsTransitioning(
            AtomicReference<@Nullable Parent> rootReference,
            AtomicReference<@Nullable M3SegmentedButton> outgoingReference,
            AtomicReference<@Nullable M3SegmentedButton> incomingReference
    ) {
        @Nullable M3SegmentedButton outgoing = outgoingReference.get();
        @Nullable M3SegmentedButton incoming = incomingReference.get();
        if (outgoing == null || incoming == null || observableRootUnavailable(rootReference)) {
            return false;
        }

        Region outgoingSelection = segmentedButtonSelectionContainer(outgoing);
        Region incomingSelection = segmentedButtonSelectionContainer(incoming);
        Region outgoingIndicator = segmentedButtonSelectionIndicator(outgoing);
        Region incomingIndicator = segmentedButtonSelectionIndicator(incoming);
        return isBetweenExclusive(outgoingSelection.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(incomingSelection.getOpacity(), 0.25, 1.0)
                && isBetweenExclusive(incomingSelection.getScaleX(), 0.96, 1.0)
                && isBetweenExclusive(outgoingIndicator.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(outgoingIndicator.getScaleX(), 0.8, 1.0)
                && isBetweenExclusive(incomingIndicator.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(incomingIndicator.getScaleX(), 0.8, 1.0);
    }

    /// Returns whether a segmented button selection has settled after a selection change.
    private static boolean segmentedButtonSelectionHasSettled(
            AtomicReference<@Nullable Parent> rootReference,
            AtomicReference<@Nullable M3SegmentedButton> outgoingReference,
            AtomicReference<@Nullable M3SegmentedButton> incomingReference
    ) {
        @Nullable M3SegmentedButton outgoing = outgoingReference.get();
        @Nullable M3SegmentedButton incoming = incomingReference.get();
        if (outgoing == null || incoming == null || observableRootUnavailable(rootReference)) {
            return false;
        }

        Region outgoingSelection = segmentedButtonSelectionContainer(outgoing);
        Region incomingSelection = segmentedButtonSelectionContainer(incoming);
        Region outgoingIndicator = segmentedButtonSelectionIndicator(outgoing);
        Region incomingIndicator = segmentedButtonSelectionIndicator(incoming);
        return Math.abs(outgoingSelection.getOpacity()) < 0.0001
                && Math.abs(incomingSelection.getOpacity() - 1.0) < 0.0001
                && Math.abs(outgoingSelection.getScaleX() - 0.96) < 0.0001
                && Math.abs(incomingSelection.getScaleX() - 1.0) < 0.0001
                && Math.abs(outgoingIndicator.getOpacity()) < 0.0001
                && Math.abs(outgoingIndicator.getScaleX() - 0.8) < 0.0001
                && Math.abs(incomingIndicator.getOpacity() - 1.0) < 0.0001
                && Math.abs(incomingIndicator.getScaleX() - 1.0) < 0.0001;
    }

    /// Returns the segmented button built-in selected-state indicator region.
    private static javafx.scene.layout.Region segmentedButtonSelectionIndicator(M3SegmentedButton button) {
        javafx.scene.Node indicator = button.lookup("." + "m3-segmented-button-selection-indicator");
        assertInstanceOf(javafx.scene.layout.Region.class, indicator);
        return (javafx.scene.layout.Region) indicator;
    }

    /// Verifies that a parameterless show-item action focuses the expected accessibility item.
    private static void assertAccessibleShowItemFocus(Node outsideFocusNode, Node control, Node expectedFocusNode) {
        outsideFocusNode.requestFocus();
        assertTrue(outsideFocusNode.isFocused());

        control.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

        assertTrue(expectedFocusNode.isFocused());
        assertSame(expectedFocusNode, control.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Creates an input chip with the requested selected state.
    private static M3InputChip createInputChip(String text, boolean selected) {
        M3InputChip chip = new M3InputChip(text);
        chip.setSelected(selected);
        return chip;
    }

    /// Creates an unselected input chip.
    private static M3InputChip createInputChip(String text) {
        return createInputChip(text, false);
    }
}
