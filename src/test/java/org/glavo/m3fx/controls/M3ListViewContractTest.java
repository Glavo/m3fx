// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.skins.M3ListViewCellSkin;
import org.glavo.m3fx.skins.M3ListViewSkin;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies virtualized list layout, scrolling, selection, and focus.
@NotNullByDefault
final class M3ListViewContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// The pulse count used to verify detached virtualized focus remains quiescent.
    private static final int DETACHED_LIST_FOCUS_STABLE_PULSES = 2;

    /// The pulse count used after a smooth scroll reaches its target position.
    private static final int SMOOTH_SCROLL_COMPLETION_STABLE_PULSES = 2;

    /// The normalized position tolerance for a rendered virtual-flow scroll target.
    private static final double SMOOTH_SCROLL_POSITION_TOLERANCE = 0.001;

    /// Verifies that virtualized lists support printable-key type-ahead focus and single selection.
    @Test
    void listViewSupportsTypeAheadKeyboardNavigation() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("Archive", "Settings", "Search", "Reports");
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            Pane root = new Pane(listView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 180.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                listView.resize(280.0, 160.0);
                root.applyCss();
                root.layout();
                listView.requestFocus();

                listView.fireEvent(keyTypedEvent("s"));
                listView.fireEvent(keyTypedEvent("e"));

                assertEquals(2, listView.getFocusedIndex());
                assertEquals(2, listView.getSelectedIndex());
                assertEquals("Search", listView.getSelectedItem());

                listView.fireEvent(keyTypedEvent("r"));

                assertEquals(3, listView.getFocusedIndex());
                assertEquals(3, listView.getSelectedIndex());
                assertEquals("Reports", listView.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list type-ahead uses M3ListItem headline text when data items are list items.
    @Test
    void listViewTypeAheadUsesListItemHeadlineText() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListItem overview = new M3ListItem("Overview");
            M3ListItem buttons = new M3ListItem("Buttons");
            M3ListItem disabled = new M3ListItem("Search");
            disabled.setDisable(true);
            M3ListItem sheets = new M3ListItem("Sheets");
            M3ListView<M3ListItem> listView = listView(overview, buttons, disabled, sheets);
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            Pane root = new Pane(listView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                listView.resize(280.0, 180.0);
                root.applyCss();
                root.layout();
                listView.requestFocus();

                listView.fireEvent(keyTypedEvent("s"));

                assertEquals(3, listView.getFocusedIndex());
                assertEquals(sheets, listView.getSelectedItem());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized lists expose the visible focused row as their accessibility focus node.
    @Test
    void listViewReportsVisibleFocusedRowForAccessibility() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("Archive", "Settings", "Search");
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            Pane root = new Pane(listView);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                listView.resize(280.0, 180.0);
                root.applyCss();
                root.layout();

                listView.focusIndex(1);
                root.applyCss();
                root.layout();

                Object focusNode = listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);

                assertInstanceOf(M3ListItem.class, focusNode);
                assertEquals("Settings", ((M3ListItem) focusNode).getHeadlineText());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized segmented gaps augment cell stride without shrinking fixed-height rows.
    @Test
    void listViewSegmentedSpacingPreservesFixedItemHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = new M3ListView<>();
            for (int index = 0; index < 40; index++) {
                listView.getItems().add("Row " + index);
            }
            listView.setListStyle(M3ListStyle.SEGMENTED);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(320.0, 180.0);

            StackPane root = new StackPane(listView);
            Scene scene = new Scene(root, 360.0, 220.0);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(scene, M3Theme.fromSeed(
                        Color.web("#6750a4"),
                        M3Profile.EXPRESSIVE_2025,
                        Brightness.LIGHT
                ));
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(2.0, listView.getItemSpacing(), 0.0001);
                assertEquals(56.0, listView.getFixedCellSize(), 0.0001);
                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                assertEquals(0.0, flow.getFixedCellSize(), 0.0001);

                List<? extends M3ListCell<?>> cells = listView.lookupAll("." + "m3-list-view-cell").stream()
                        .filter(M3ListCell.class::isInstance)
                        .map(node -> (M3ListCell<?>) node)
                        .filter(cell -> !cell.isEmpty())
                        .sorted((firstCell, secondCell) -> Integer.compare(firstCell.getIndex(), secondCell.getIndex()))
                        .toList();
                assertTrue(cells.size() >= 2);
                M3ListCell<?> firstCell = cells.get(0);
                M3ListCell<?> secondCell = cells.get(1);
                M3ListItem firstRow = Objects.requireNonNull(firstCell.getListItem(), "first row");
                M3ListItem secondRow = Objects.requireNonNull(secondCell.getListItem(), "second row");
                assertEquals(58.0, firstCell.getHeight(), 0.0001);
                assertEquals(56.0, firstRow.getHeight(), 0.0001);
                assertEquals(
                        2.0,
                        secondRow.localToScene(secondRow.getBoundsInLocal()).getMinY()
                                - firstRow.localToScene(firstRow.getBoundsInLocal()).getMaxY(),
                        0.0001
                );

                listView.setItemSpacing(6.0);
                root.applyCss();
                root.layout();
                cells = listView.lookupAll("." + "m3-list-view-cell").stream()
                        .filter(M3ListCell.class::isInstance)
                        .map(node -> (M3ListCell<?>) node)
                        .filter(cell -> !cell.isEmpty())
                        .sorted((firstVisible, secondVisible) ->
                                Integer.compare(firstVisible.getIndex(), secondVisible.getIndex()))
                        .toList();
                assertEquals(62.0, cells.get(0).getHeight(), 0.0001);
                assertEquals(56.0, Objects.requireNonNull(cells.get(0).getListItem(), "updated first row").getHeight(),
                        0.0001);

                listView.setFixedCellSize(64.0);
                root.applyCss();
                root.layout();
                cells = listView.lookupAll("." + "m3-list-view-cell").stream()
                        .filter(M3ListCell.class::isInstance)
                        .map(node -> (M3ListCell<?>) node)
                        .filter(cell -> !cell.isEmpty())
                        .sorted((firstVisible, secondVisible) ->
                                Integer.compare(firstVisible.getIndex(), secondVisible.getIndex()))
                        .toList();
                assertEquals(70.0, cells.get(0).getHeight(), 0.0001);
                assertEquals(64.0, Objects.requireNonNull(cells.get(0).getListItem(), "resized first row").getHeight(),
                        0.0001);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views expose data selection policies.
    @Test
    void listViewManagesDataSelectionPolicies() {
        M3ListView<String> listView = listView("First", "Second", "Third");
        listView.setSelectionMode(M3SelectionMode.SINGLE);

        listView.selectIndex(1);

        assertEquals(1, listView.getSelectedIndex());
        assertEquals("Second", listView.getSelectedItem());
        assertEquals(java.util.List.of(1), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Second"), listView.getSelectedItems());
        assertThrows(UnsupportedOperationException.class, () -> listView.getSelectedIndices().add(2));

        listView.setSelectionMode(M3SelectionMode.MULTIPLE);
        listView.setIndexSelected(2, true);

        assertEquals(java.util.List.of(1, 2), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Second", "Third"), listView.getSelectedItems());

        listView.clearSelection(1);

        assertEquals(java.util.List.of(2), listView.getSelectedIndices());
        assertEquals("Third", listView.getSelectedItem());

        listView.setAllowEmptySelection(false);
        listView.clearSelection();

        assertEquals(java.util.List.of(2), listView.getSelectedIndices());
        assertEquals("Third", listView.getSelectedItem());

        listView.getItems().remove("Third");

        assertEquals(java.util.List.of(0), listView.getSelectedIndices());
        assertEquals("First", listView.getSelectedItem());
    }

    /// Verifies that virtualized list multi-selection stays sorted without redundant observable-list changes.
    @Test
    void listViewMaintainsSortedSelectionWithOneChangePerMutation() {
        M3ListView<String> listView = listView("Zero", "One", "Two", "Three", "Four");
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);
        AtomicInteger selectionChangeCount = new AtomicInteger();
        listView.getSelectedIndices().addListener(
                (javafx.collections.ListChangeListener<Integer>) change -> selectionChangeCount.incrementAndGet()
        );

        listView.selectIndex(4);
        listView.selectIndex(1);
        listView.selectIndex(3);
        listView.selectIndex(3);

        assertEquals(java.util.List.of(1, 3, 4), listView.getSelectedIndices());
        assertEquals(java.util.List.of("One", "Three", "Four"), listView.getSelectedItems());
        assertEquals(3, selectionChangeCount.get());

        listView.clearSelection(3);
        listView.clearSelection(3);

        assertEquals(java.util.List.of(1, 4), listView.getSelectedIndices());
        assertEquals(4, selectionChangeCount.get());
    }

    /// Verifies that selection and focus follow surviving rows across backing-list structural changes.
    @Test
    void listViewRemapsSelectionAndFocusAcrossStructuralChanges() {
        M3ListView<String> listView = listView("Alpha", "Beta", "Gamma", "Delta");
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);
        listView.selectIndex(1);
        listView.selectIndex(3);
        listView.focusIndex(2);

        listView.getItems().add(0, "New");

        assertEquals(java.util.List.of(2, 4), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Beta", "Delta"), listView.getSelectedItems());
        assertEquals(3, listView.getFocusedIndex());
        assertEquals("Gamma", listView.getFocusedItem());

        listView.getItems().remove(1);

        assertEquals(java.util.List.of(1, 3), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Beta", "Delta"), listView.getSelectedItems());
        assertEquals(2, listView.getFocusedIndex());
        assertEquals("Gamma", listView.getFocusedItem());

        String replacementBeta = new String(new char[]{'B', 'e', 't', 'a'});
        listView.getItems().set(1, replacementBeta);

        assertSame(replacementBeta, listView.getSelectedItem());
        assertSame(replacementBeta, listView.getSelectedItems().get(0));

        listView.getItems().set(2, "Updated Gamma");

        assertEquals(2, listView.getFocusedIndex());
        assertEquals("Updated Gamma", listView.getFocusedItem());

        listView.getItems().sort(java.util.Comparator.reverseOrder());

        assertEquals(java.util.List.of(2, 3), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Delta", "Beta"), listView.getSelectedItems());
        assertEquals(0, listView.getFocusedIndex());
        assertEquals("Updated Gamma", listView.getFocusedItem());

        listView.getItems().remove(0);

        assertEquals(java.util.List.of(1, 2), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Delta", "Beta"), listView.getSelectedItems());
        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());
    }

    /// Verifies that natural-order sorting emits a stable permutation for duplicate data values.
    @Test
    void listViewTracksStableNaturalOrderPermutations() {
        M3ListView<String> listView = listView("Beta", "Alpha", "Alpha");
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);
        listView.selectIndex(0);
        listView.selectIndex(2);
        listView.focusIndex(1);
        AtomicInteger permutationCount = new AtomicInteger();
        listView.getItems().addListener((javafx.collections.ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                    permutationCount.incrementAndGet();
                }
            }
        });

        listView.getItems().sort(null);

        assertEquals(java.util.List.of("Alpha", "Alpha", "Beta"), listView.getItems());
        assertEquals(1, permutationCount.get());
        assertEquals(java.util.List.of(1, 2), listView.getSelectedIndices());
        assertEquals(java.util.List.of("Alpha", "Beta"), listView.getSelectedItems());
        assertEquals(0, listView.getFocusedIndex());
        assertEquals("Alpha", listView.getFocusedItem());

        listView.getItems().sort(null);

        assertEquals(1, permutationCount.get());
    }

    /// Verifies that variable-height virtual lists keep trailing empty cells bounded and measurable.
    @Test
    void listViewBoundsVariableHeightTrailingCellPool() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            listView.getItems().addAll(0, 1, 2);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText("Row " + value);
                item.setSupportingText(value >= 1 ? "Supporting text" : null);
                item.setOverlineText(value >= 2 ? "Overline" : null);
            }));
            listView.setPrefSize(260.0, 168.0);
            StackPane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                List<? extends M3ListCell<?>> cells = listView.lookupAll("." + "m3-list-view-cell").stream()
                        .map(node -> (M3ListCell<?>) node)
                        .toList();
                long renderedCellCount = cells.stream().filter(cell -> !cell.isEmpty()).count();

                assertEquals(listView.getItems().size(), renderedCellCount);
                assertTrue(cells.size() <= 16, () -> "materializedCellCount=" + cells.size());
                assertTrue(cells.stream()
                        .filter(M3ListCell::isEmpty)
                        .allMatch(cell -> cell.getHeight() > 0.0 && cell.prefHeight(-1.0) > 0.0));
                assertTrue(cells.stream()
                        .filter(cell -> !cell.isEmpty())
                        .allMatch(cell -> cell.getListItem() != null));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtual rows materialize during hidden-window pre-layout before the first show pulse.
    @Test
    void listViewMaterializesRowsDuringHiddenWindowPreLayout() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicInteger factoryCalls = new AtomicInteger();
            M3ListView<String> listView = listView("Archive", "Settings", "Search");
            listView.setCellFactory(view -> {
                factoryCalls.incrementAndGet();
                return listCell(view, M3ListItem::setHeadlineText);
            });
            listView.setPrefSize(260.0, 168.0);
            StackPane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                root.applyCss();
                root.layout();

                List<? extends M3ListCell<?>> renderedCells = listView.lookupAll("." + "m3-list-view-cell").stream()
                        .map(node -> (M3ListCell<?>) node)
                        .filter(cell -> !cell.isEmpty())
                        .toList();

                assertEquals(listView.getItems().size(), renderedCells.size());
                assertTrue(factoryCalls.get() > 0);
                assertTrue(factoryCalls.get() <= 16, () -> "factoryCalls=" + factoryCalls.get());
                assertTrue(renderedCells.stream().allMatch(cell -> cell.getListItem() != null));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views use VirtualFlow-backed cells instead of materializing every item.
    @Tier2Test
    @Test
    void listViewVirtualizesRenderedItems() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicInteger factoryCalls = new AtomicInteger();
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setCellFactory(view -> {
                factoryCalls.incrementAndGet();
                return listCell(view, (item, value) -> {
                    item.setHeadlineText("Row " + value);
                    item.setLeadingIcon(Integer.toString(value % 10));
                });
            });
            listView.setPrefSize(260.0, 168.0);
            Pane root = new Pane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertInstanceOf(M3ListViewSkin.class, listView.getSkin());
                assertTrue(factoryCalls.get() < listView.getItems().size());
                M3ListItem visibleSecond = assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1)
                );
                visibleSecond.fire();

                assertEquals(1, listView.getSelectedIndex());
                assertTrue(visibleSecond.isSelected());

                listView.scrollTo(80);
                root.layout();

                M3ListItem visibleEightieth = assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 80)
                );
                assertEquals("Row 80", visibleEightieth.getHeadlineText());
                assertTrue(factoryCalls.get() < listView.getItems().size());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that variable-height list scrolling estimates content length from all attached data rows.
    @Test
    void listViewVariableHeightSmoothScrollingUsesVisibleAverageHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int index = 0; index < 60; index++) {
                listView.getItems().add(index);
            }
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText("Row " + value);
                item.setSupportingText(value % 3 >= 1 ? "Supporting text" : null);
                item.setOverlineText(value % 3 == 2 ? "Overline" : null);
            }));
            listView.setPrefSize(260.0, 216.0);
            StackPane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 260.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                List<? extends M3ListCell<?>> measuredCells = listView
                        .lookupAll("." + "m3-list-view-cell")
                        .stream()
                        .map(node -> (M3ListCell<?>) node)
                        .filter(cell -> !cell.isEmpty() && cell.getHeight() > 0.0)
                        .toList();
                double averageRowHeight = measuredCells.stream()
                        .mapToDouble(M3ListCell::getHeight)
                        .average()
                        .orElseThrow();
                long distinctRowHeights = measuredCells.stream()
                        .mapToLong(cell -> Math.round(cell.getHeight()))
                        .distinct()
                        .count();
                double scrollablePixels = averageRowHeight * listView.getItems().size() - flow.getHeight();
                double deltaY = -72.0;
                double expectedPosition = clampToUnit(flow.getPosition() - deltaY / scrollablePixels);

                assertTrue(distinctRowHeights > 1L, () -> "measuredCells=" + measuredCells.size());
                assertTrue(scrollablePixels > 0.0, () -> "scrollablePixels=" + scrollablePixels);
                listView.setAnimationsEnabled(false);
                ScrollEvent event = scrollEvent(listView, 0.0, deltaY);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(expectedPosition, flow.getPosition(), 0.000001);
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views animate wheel scrolling through Material motion.
    @Test
    void listViewSmoothScrollingAnimatesWheelScroll() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(true);

                ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(0.0, flow.getPosition(), 0.0001);
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that completed virtualized list wheel scrolling settles at its rendered target position.
    @Tier2Test
    @Test
    void listViewSmoothScrollingSettlesAtTargetAfterCompletion() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListView<Integer>> listViewReference = new AtomicReference<>();
        AtomicReference<@Nullable VirtualFlow<?>> flowReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> targetPositionReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        VirtualFlow<?> flow = Objects.requireNonNull(flowReference.get(), "flow");
                        double targetPosition = Objects.requireNonNull(
                                targetPositionReference.get(),
                                "targetPosition"
                        );
                        return Math.abs(flow.getPosition() - targetPosition)
                                <= SMOOTH_SCROLL_POSITION_TOLERANCE;
                    },
                    SMOOTH_SCROLL_COMPLETION_STABLE_PULSES,
                    () -> "list view smooth scroll did not settle: target="
                            + targetPositionReference.get()
                            + ", actual="
                            + Objects.requireNonNull(flowReference.get(), "flow").getPosition(),
                    () -> {
                        M3ListView<Integer> listView = new M3ListView<>();
                        for (int i = 0; i < 100; i++) {
                            listView.getItems().add(i);
                        }
                        listView.setFixedCellSize(56.0);
                        listView.setPrefSize(260.0, 168.0);
                        listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
                        Pane root = new StackPane(listView);
                        Scene scene = new Scene(root, 300.0, 220.0);
                        Stage stage = new Stage();

                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.layout();

                        VirtualFlow<?> flow = assertInstanceOf(
                                VirtualFlow.class,
                                listView.lookup(".m3-list-view-flow")
                        );
                        listView.setAnimationsEnabled(true);

                        stageReference.set(stage);
                        listViewReference.set(listView);
                        flowReference.set(flow);
                        double targetPosition = expectedListViewWheelTargetPosition(listView, flow, -112.0);
                        targetPositionReference.set(targetPosition);

                        ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                        listView.fireEvent(event);
                        assertTrue(event.isConsumed());
                        assertEquals(0.0, flow.getPosition(), 0.0001);
                    },
                    () -> {
                        VirtualFlow<?> flow = Objects.requireNonNull(flowReference.get(), "flow");
                        double targetPosition = Objects.requireNonNull(
                                targetPositionReference.get(),
                                "targetPosition"
                        );
                        assertEquals(targetPosition, flow.getPosition(), SMOOTH_SCROLL_POSITION_TOLERANCE);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3ListView<Integer> listView = listViewReference.get();
                if (listView != null) {
                    listView.setAnimationsEnabled(true);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that running virtualized list wheel scrolling settles when animations are disabled at runtime.
    @Test
    void listViewSmoothScrollingSettlesWhenAnimationsAreDisabledAtRuntime() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(true);

                ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.setAnimationsEnabled(false);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that hiding the presenting window settles virtualized list scrolling at its accumulated target.
    @Test
    @Tier2Test
    void listViewSmoothScrollingSettlesWhenWindowHides() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int index = 0; index < 100; index++) {
                listView.getItems().add(index);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);
            Stage stage = new Stage();

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();
            listView.setAnimationsEnabled(true);
            try {
                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                stage.hide();

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that wheel events accumulate while virtualized list smooth scrolling is running.
    @Test
    void listViewSmoothScrollingAccumulatesWheelEventsWhileAnimationRuns() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(true);
                double expectedAccumulatedPosition = expectedListViewWheelTargetPosition(listView, flow, -224.0);

                ScrollEvent firstEvent = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(firstEvent);
                ScrollEvent secondEvent = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(secondEvent);

                assertTrue(firstEvent.isConsumed());
                assertTrue(secondEvent.isConsumed());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.setAnimationsEnabled(false);

                assertEquals(expectedAccumulatedPosition, flow.getPosition(), 0.0001);
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that platform text-line units scroll virtualized lists by Material line distances.
    @Test
    void listViewSmoothScrollingUsesTextLineScrollUnits() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(false);
                double expectedPosition = expectedListViewWheelTargetPosition(listView, flow, -120.0);

                ScrollEvent event = scrollEvent(
                        listView,
                        0.0,
                        0.0,
                        false,
                        false,
                        ScrollEvent.HorizontalTextScrollUnits.NONE,
                        0.0,
                        ScrollEvent.VerticalTextScrollUnits.LINES,
                        -3.0
                );
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(expectedPosition, flow.getPosition(), 0.0001);
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that platform page units scroll virtualized lists by the current viewport height.
    @Test
    void listViewSmoothScrollingUsesPageScrollUnits() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(false);
                double expectedPosition = expectedListViewWheelTargetPosition(listView, flow, -flow.getHeight());

                ScrollEvent event = scrollEvent(
                        listView,
                        0.0,
                        0.0,
                        false,
                        false,
                        ScrollEvent.HorizontalTextScrollUnits.NONE,
                        0.0,
                        ScrollEvent.VerticalTextScrollUnits.PAGES,
                        -1.0
                );
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertEquals(expectedPosition, flow.getPosition(), 0.0001);
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that disabled animation settings make virtualized list wheel scrolling finish synchronously.
    @Test
    void listViewSmoothScrollingHonorsDisabledAnimations() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(false);

                ScrollEvent event = scrollEvent(listView, 0.0, -112.0);
                listView.fireEvent(event);

                assertTrue(event.isConsumed());
                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that programmatic virtualized list scrolling uses the animated scroll policy.
    @Test
    void listViewProgrammaticScrollUsesAnimatedPolicy() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );

                assertTrue(listView.isAnimatedScroll());
                listView.setAnimationsEnabled(true);
                listView.scrollTo(80);

                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.setAnimatedScroll(false);
                assertFalse(listView.isAnimatedScroll());
                listView.scrollTo(80);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that running programmatic virtualized list scrolling settles when animations are disabled.
    @Test
    void listViewProgrammaticScrollSettlesWhenAnimationsAreDisabledAtRuntime() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(true);

                listView.scrollTo(80);

                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.setAnimationsEnabled(false);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that disabled animations make programmatic virtualized list scrolling finish synchronously.
    @Test
    void listViewProgrammaticScrollHonorsDisabledAnimations() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(false);

                listView.scrollTo(80);

                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that keyboard focus scrolling also follows the animated scroll policy.
    @Test
    void listViewKeyboardFocusUsesAnimatedScrollPolicy() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                VirtualFlow<?> flow = assertInstanceOf(
                        VirtualFlow.class,
                        listView.lookup(".m3-list-view-flow")
                );
                listView.setAnimationsEnabled(true);

                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertEquals(99, listView.getFocusedIndex());
                assertEquals(99, listView.getSelectedIndex());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.setAnimatedScroll(false);
                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

                assertEquals(0, listView.getFocusedIndex());
                assertEquals(0, listView.getSelectedIndex());
                assertEquals(0.0, flow.getPosition(), 0.0001);

                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertEquals(99, listView.getFocusedIndex());
                assertEquals(99, listView.getSelectedIndex());
                assertTrue(flow.getPosition() > 0.0, () -> "position=" + flow.getPosition());
            } finally {
                listView.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views expose keyboard focus and selection navigation.
    @Test
    void listViewSupportsKeyboardFocusAndSelectionNavigation() {
        M3ListView<String> listView = listView("First", "Second", "Third");
        listView.setSelectionMode(M3SelectionMode.SINGLE);

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(0, listView.getFocusedIndex());
        assertEquals("First", listView.getFocusedItem());
        assertEquals(0, listView.getSelectedIndex());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Second", listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

        assertEquals(2, listView.getFocusedIndex());
        assertEquals("Third", listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Second", listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

        assertEquals(0, listView.getFocusedIndex());
        assertEquals("First", listView.getSelectedItem());

        listView.setSelectionMode(M3SelectionMode.MULTIPLE);
        listView.clearFocus();
        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(1, listView.getFocusedIndex());
        assertEquals(java.util.List.of(0), listView.getSelectedIndices());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));

        assertEquals(java.util.List.of(0, 1), listView.getSelectedIndices());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

        assertEquals(java.util.List.of(0), listView.getSelectedIndices());
    }

    /// Verifies that virtualized list keyboard navigation skips disabled and hidden node data items.
    @Test
    void listViewKeyboardNavigationSkipsDisabledAndHiddenNodeItems() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem disabled = new M3ListItem("Disabled");
        M3ListItem hidden = new M3ListItem("Hidden");
        M3ListItem fourth = new M3ListItem("Fourth");
        disabled.setDisable(true);
        hidden.setVisible(false);

        M3ListView<M3ListItem> listView = listView(first, disabled, hidden, fourth);
        listView.setSelectionMode(M3SelectionMode.SINGLE);

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(0, listView.getFocusedIndex());
        assertSame(first, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(3, listView.getFocusedIndex());
        assertSame(fourth, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

        assertEquals(0, listView.getFocusedIndex());
        assertSame(first, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

        assertEquals(3, listView.getFocusedIndex());
        assertSame(fourth, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));

        assertEquals(0, listView.getFocusedIndex());
        assertSame(first, listView.getSelectedItem());

        listView.clearFocus();
        listView.focusLast();

        assertEquals(3, listView.getFocusedIndex());

        listView.focusNext();

        assertEquals(0, listView.getFocusedIndex());

        listView.focusPrevious();

        assertEquals(3, listView.getFocusedIndex());

        listView.clearSelection();
        listView.selectFirst();

        assertSame(first, listView.getSelectedItem());

        listView.selectPrevious();

        assertSame(fourth, listView.getSelectedItem());

        listView.selectNext();

        assertSame(first, listView.getSelectedItem());
    }

    /// Verifies that virtualized list focus APIs do not leave focus on unreachable node data items.
    @Test
    void listViewFocusApiRejectsDisabledAndHiddenNodeItems() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem disabled = new M3ListItem("Disabled");
        M3ListItem hidden = new M3ListItem("Hidden");
        disabled.setDisable(true);
        hidden.setVisible(false);

        M3ListView<M3ListItem> listView = listView(first, disabled, hidden);

        listView.focusIndex(1);

        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());

        listView.focusIndex(0);
        listView.focusIndex(2);

        assertEquals(0, listView.getFocusedIndex());
        assertSame(first, listView.getFocusedItem());

        M3ListItem replacement = new M3ListItem("Replacement");
        replacement.setDisable(true);

        listView.getItems().set(0, replacement);

        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());
    }

    /// Verifies that virtualized list selection and focus track runtime node item reachability changes.
    @Test
    void listViewSelectionAndFocusTrackNodeItemReachabilityChanges() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem third = new M3ListItem("Third");
        M3ListView<M3ListItem> listView = listView(first, second, third);
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);

        listView.selectIndex(0);
        listView.selectIndex(1);
        listView.selectIndex(2);
        listView.focusIndex(1);

        assertEquals(java.util.List.of(0, 1, 2), listView.getSelectedIndices());
        assertEquals(1, listView.getFocusedIndex());

        second.setVisible(false);

        assertEquals(java.util.List.of(0, 2), listView.getSelectedIndices());
        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());

        listView.selectIndex(1);

        assertEquals(java.util.List.of(0, 2), listView.getSelectedIndices());

        second.setVisible(true);
        listView.selectIndex(1);
        listView.focusIndex(2);

        assertEquals(java.util.List.of(0, 1, 2), listView.getSelectedIndices());
        assertEquals(2, listView.getFocusedIndex());

        third.setDisable(true);

        assertEquals(java.util.List.of(0, 1), listView.getSelectedIndices());
        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());

        listView.setSelectionMode(M3SelectionMode.SINGLE);
        listView.setAllowEmptySelection(false);
        listView.selectIndex(0);
        first.setDisable(true);

        assertEquals(java.util.List.of(1), listView.getSelectedIndices());
        assertSame(second, listView.getSelectedItem());
    }

    /// Verifies that node data items track reachability through changing external ancestor chains.
    @Test
    void listViewTracksExternalNodeItemAncestorReachability() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        StackPane firstParent = new StackPane(first);
        StackPane secondParent = new StackPane();
        M3ListView<M3ListItem> listView = listView(first, second);
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);
        listView.selectIndex(0);
        listView.focusIndex(0);

        firstParent.setVisible(false);

        assertTrue(listView.getSelectedIndices().isEmpty());
        assertEquals(-1, listView.getFocusedIndex());

        firstParent.setVisible(true);
        firstParent.getChildren().remove(first);
        secondParent.getChildren().add(first);
        listView.selectIndex(0);
        listView.focusIndex(0);

        secondParent.setDisable(true);

        assertTrue(listView.getSelectedIndices().isEmpty());
        assertEquals(-1, listView.getFocusedIndex());

        listView.selectIndex(1);
        listView.focusIndex(1);
        listView.getItems().remove(first);
        first.setVisible(false);
        secondParent.setDisable(false);

        assertEquals(java.util.List.of(0), listView.getSelectedIndices());
        assertSame(second, listView.getSelectedItem());
        assertEquals(0, listView.getFocusedIndex());
        assertSame(second, listView.getFocusedItem());
    }

    /// Verifies that shared node and ancestor observers remain active until their last data-item reference is removed.
    @Test
    void listViewReferenceCountsSharedReachabilityObservers() {
        M3ListItem sharedItem = new M3ListItem("Shared");
        M3ListItem sibling = new M3ListItem("Sibling");
        StackPane sharedParent = new StackPane(sharedItem, sibling);
        M3ListView<M3ListItem> listView = listView(sharedItem, sharedItem, sibling);
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);
        listView.selectIndex(1);
        listView.selectIndex(2);

        listView.getItems().remove(0);
        sharedItem.setVisible(false);

        assertEquals(java.util.List.of(1), listView.getSelectedIndices());
        assertSame(sibling, listView.getSelectedItem());

        sharedParent.setDisable(true);

        assertTrue(listView.getSelectedIndices().isEmpty());
    }

    /// Verifies that virtualized list page navigation moves by visible rows without wrapping.
    @Test
    void listViewPageNavigationUsesVisibleRowSteps() {
        M3ListItem first = new M3ListItem("First");
        M3ListItem second = new M3ListItem("Second");
        M3ListItem disabled = new M3ListItem("Disabled");
        M3ListItem fourth = new M3ListItem("Fourth");
        M3ListItem fifth = new M3ListItem("Fifth");
        M3ListItem hidden = new M3ListItem("Hidden");
        M3ListItem seventh = new M3ListItem("Seventh");
        M3ListItem eighth = new M3ListItem("Eighth");
        M3ListItem ninth = new M3ListItem("Ninth");
        disabled.setDisable(true);
        hidden.setVisible(false);

        M3ListView<M3ListItem> listView = listView(
                first,
                second,
                disabled,
                fourth,
                fifth,
                hidden,
                seventh,
                eighth,
                ninth
        );
        listView.setFixedCellSize(56.0);
        listView.setSelectionMode(M3SelectionMode.SINGLE);
        listView.resize(280.0, 168.0);

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));

        assertEquals(0, listView.getFocusedIndex());
        assertSame(first, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));

        assertEquals(4, listView.getFocusedIndex());
        assertSame(fifth, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));

        assertEquals(8, listView.getFocusedIndex());
        assertSame(ninth, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_DOWN));

        assertEquals(8, listView.getFocusedIndex());
        assertSame(ninth, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));

        assertEquals(4, listView.getFocusedIndex());
        assertSame(fifth, listView.getSelectedItem());

        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));
        listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.PAGE_UP));

        assertEquals(0, listView.getFocusedIndex());
        assertSame(first, listView.getSelectedItem());
    }

    /// Verifies that virtualized list view keyboard and accessibility focus scrolls rows into view.
    @Tier2Test
    @Test
    void listViewFocusesVirtualizedRowsFromKeyboardAndAccessibility() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ListView<Integer>> listViewReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(() -> listViewFocusNodeHasHeadline(listViewReference, "Row 99"), () -> {
                M3ListView<Integer> listView = new M3ListView<>();
                for (int i = 0; i < 100; i++) {
                    listView.getItems().add(i);
                }
                listView.setSelectionMode(M3SelectionMode.SINGLE);
                listView.setFixedCellSize(56.0);
                listView.setAnimatedScroll(false);
                listView.setPrefSize(260.0, 168.0);
                listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
                Pane root = new StackPane(listView);
                Scene scene = new Scene(root, 300.0, 220.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                Stage stage = new Stage();
                stageReference.set(stage);
                rootReference.set(root);
                listViewReference.set(listView);

                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                listView.requestFocus();
                listView.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));
                root.layout();
            }, () -> {
                Pane root = Objects.requireNonNull(rootReference.get(), "root");
                M3ListView<Integer> listView = Objects.requireNonNull(listViewReference.get(), "listView");
                root.layout();

                assertEquals(99, listView.getFocusedIndex());
                assertEquals(99, listView.getSelectedIndex());
                assertAccessibleListItemHeadline(listView, "Row 99");
                assertTrue(listView.isFocused());

                listView.clearFocus();

                assertEquals(-1, listView.getFocusedIndex());

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                root.layout();

                assertEquals(99, listView.getFocusedIndex());

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 40);
                root.layout();
            });

            FxTestUtils.runOnFxThreadWhen(() -> listViewFocusNodeHasHeadline(listViewReference, "Row 40"), () -> {
            }, () -> {
                Pane root = Objects.requireNonNull(rootReference.get(), "root");
                M3ListView<Integer> listView = Objects.requireNonNull(listViewReference.get(), "listView");
                root.layout();

                assertEquals(40, listView.getFocusedIndex());
                assertAccessibleListItemHeadline(listView, "Row 40");
                assertTrue(listView.isFocused());
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that virtualized focus work stops cleanly when the list view is detached.
    @Tier2Test
    @Test
    void listViewStopsFocusWorkWhenDetached() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhenStable(() -> {
            @Nullable Stage stage = stageReference.get();
            return stage != null && !stage.isShowing();
        }, DETACHED_LIST_FOCUS_STABLE_PULSES, () -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 100; i++) {
                listView.getItems().add(i);
            }
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);
            Stage stage = new Stage();

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stageReference.set(stage);
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 99);
            stage.close();
        }, () -> assertTrue(stageReference.get() != null));
    }

    /// Verifies that virtualized list view accessibility actions accept rendered row descendants.
    @Tier2Test
    @Test
    void listViewAccessibilityActionsAcceptRenderedRowDescendants() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("Archive", "Settings", "Search");
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(280.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText(value);
                if (!(item.getTrailing() instanceof M3Button)) {
                    item.setTrailing(new M3Button("Action"));
                }
            }));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 320.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                M3ListItem settingsRow = Objects.requireNonNull(assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1)
                ));
                M3Button settingsAction = Objects.requireNonNull(assertInstanceOf(
                        M3Button.class,
                        settingsRow.getTrailing()
                ));

                settingsAction.setVisible(false);
                listView.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, settingsAction);

                assertEquals(-1, listView.getSelectedIndex());
                assertNull(listView.getSelectedItem());

                settingsAction.setVisible(true);
                listView.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, settingsAction);

                assertEquals(1, listView.getSelectedIndex());
                assertEquals("Settings", listView.getSelectedItem());

                settingsRow = Objects.requireNonNull(assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1)
                ));
                settingsAction = Objects.requireNonNull(assertInstanceOf(
                        M3Button.class,
                        settingsRow.getTrailing()
                ));
                settingsRow.applyCss();
                settingsRow.layout();

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, settingsAction);
                root.layout();

                assertEquals(1, listView.getFocusedIndex());
                assertSame(settingsAction, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertEquals(1, listView.getFocusedIndex());
                assertSame(settingsAction, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views delegate explicit reveal requests into visible row popup targets.
    @Test
    void listViewAccessibilityActionsRevealVisibleRowTooltipTargets() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
            AtomicReference<@Nullable M3Button> tooltipActionReference = new AtomicReference<>();
            M3ListView<String> listView = listView("Archive", "Settings", "Search");
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(280.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText(value);
                M3Button ownerAction = item.getTrailing() instanceof M3Button button
                        ? button
                        : new M3Button("Action");
                if ("Settings".equals(value)) {
                    M3Button tooltipAction = new M3Button("Details");
                    M3RichTooltip tooltip = installRichTooltip(
                            ownerAction,
                            "Settings",
                            "Shows row details.",
                            tooltipAction
                    );
                    tooltipReference.set(tooltip);
                    tooltipActionReference.set(tooltipAction);
                }
                item.setTrailing(ownerAction);
            }));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 320.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                M3Button tooltipAction = Objects.requireNonNull(tooltipActionReference.get(), "tooltipAction");

                assertFalse(tooltip.isShowing());

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, tooltipAction);
                root.layout();

                assertEquals(1, listView.getFocusedIndex());
                assertEquals("Settings", listView.getFocusedItem());
                assertTrue(tooltip.isShowing());
                assertTrue(tooltipAction.isFocused());
                assertSame(tooltipAction, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertEquals(1, listView.getFocusedIndex());
                assertSame(tooltipAction, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list views delegate explicit reveal requests into visible row nested menus.
    @Test
    void listViewAccessibilityActionsRevealVisibleRowNestedMenuTargets() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
            AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();
            AtomicReference<@Nullable M3MenuItem> archiveItemReference = new AtomicReference<>();
            M3ListView<String> listView = listView("Archive", "Settings", "Search");
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(280.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText(value);
                M3MenuButton menuButton;
                M3SubMenuItem subMenuItem;
                M3MenuItem archiveItem;
                if (item.getTrailing() instanceof M3MenuButton existingMenuButton
                        && !existingMenuButton.getItems().isEmpty()
                        && existingMenuButton.getItems().get(0) instanceof M3SubMenuItem existingSubMenuItem
                        && !existingSubMenuItem.getItems().isEmpty()) {
                    menuButton = existingMenuButton;
                    subMenuItem = existingSubMenuItem;
                    archiveItem = (M3MenuItem) existingSubMenuItem.getItems().get(0);
                } else {
                    archiveItem = new M3MenuItem("Archive item");
                    subMenuItem = new M3SubMenuItem("Move to", archiveItem);
                    menuButton = new M3MenuButton("Actions", subMenuItem);
                    item.setTrailing(menuButton);
                }
                if ("Settings".equals(value)) {
                    menuButtonReference.set(menuButton);
                    subMenuItemReference.set(subMenuItem);
                    archiveItemReference.set(archiveItem);
                }
            }));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 320.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "subMenuItem");
                M3MenuItem archiveItem = Objects.requireNonNull(archiveItemReference.get(), "archiveItem");

                assertFalse(menuButton.isShowing());
                assertFalse(subMenuItem.isSubMenuShowing());

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, archiveItem);
                root.layout();

                assertEquals(1, listView.getFocusedIndex());
                assertEquals("Settings", listView.getFocusedItem());
                assertTrue(menuButton.isShowing());
                assertTrue(subMenuItem.isSubMenuShowing());
                assertTrue(archiveItem.isFocused());
                assertSame(archiveItem, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertEquals(1, listView.getFocusedIndex());
                assertSame(archiveItem, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that indexed virtualized reveal opens the picker belonging to the requested distant row.
    @Test
    void listViewAccessibilityActionsRevealVirtualizedRowPickerValueTargets() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicReference<@Nullable M3DatePickerField> targetDateFieldReference = new AtomicReference<>();
            LocalDate targetDate = LocalDate.of(2026, 5, 18);
            M3ListView<Integer> listView = new M3ListView<>();
            for (int index = 0; index < 120; index++) {
                listView.getItems().add(index);
            }
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(360.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText("Row " + value);
                M3DatePickerField dateField;
                if (item.getTrailing() instanceof M3DatePickerField existingDateField) {
                    dateField = existingDateField;
                } else {
                    dateField = new M3DatePickerField(LocalDate.of(2026, 5, 1));
                    dateField.getPicker().setDisplayedMonth(YearMonth.of(2026, 5));
                    dateField.setPrefWidth(180.0);
                    item.setTrailing(dateField);
                }
                if (value == 80) {
                    targetDateFieldReference.set(dateField);
                }
            }));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 440.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertNull(targetDateFieldReference.get());

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 80, targetDate);
                root.layout();

                M3DatePickerField targetDateField =
                        Objects.requireNonNull(targetDateFieldReference.get(), "targetDateField");
                @Nullable Node activePopupTarget = M3Accessible.activeExternalFocusTarget(listView, targetDateField);

                assertEquals(80, listView.getFocusedIndex());
                assertEquals(80, listView.getFocusedItem());
                assertTrue(targetDateField.isShowing());
                assertTrue(activePopupTarget instanceof Node);
                assertSame(activePopupTarget, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                listView.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertEquals(80, listView.getFocusedIndex());
                assertSame(activePopupTarget, listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that visible list view rows reject disabled picker value targets without changing list focus.
    @Test
    void listViewAccessibilityActionsRejectVisibleRowDisabledPickerValueTargets() {
        FxTestUtils.runOnFxThread(() -> {
            AtomicReference<@Nullable M3DatePickerField> targetDateFieldReference = new AtomicReference<>();
            M3ListView<Integer> listView = listView(0, 1, 2);
            listView.setSelectionMode(M3SelectionMode.SINGLE);
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(360.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText("Row " + value);
                M3DatePickerField dateField;
                if (item.getTrailing() instanceof M3DatePickerField existingDateField) {
                    dateField = existingDateField;
                } else {
                    dateField = new M3DatePickerField(LocalDate.of(2026, 5, 1));
                    dateField.getPicker().setDisplayedMonth(YearMonth.of(2026, 5));
                    dateField.getPicker().setMaxDate(LocalDate.of(2026, 5, 10));
                    dateField.setPrefWidth(180.0);
                    item.setTrailing(dateField);
                }
                if (value == 1) {
                    targetDateFieldReference.set(dateField);
                }
            }));
            Pane root = new StackPane(listView);
            Scene scene = new Scene(root, 440.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                M3DatePickerField targetDateField =
                        Objects.requireNonNull(targetDateFieldReference.get(), "targetDateField");
                listView.focusIndex(0);
                root.layout();

                assertEquals(0, listView.getFocusedIndex());
                assertFalse(targetDateField.isShowing());

                listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1, LocalDate.of(2026, 5, 18));
                root.layout();

                assertEquals(0, listView.getFocusedIndex());
                assertEquals(0, listView.getFocusedItem());
                assertFalse(targetDateField.isShowing());
                assertFalse(targetDateField.getEditor().isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list view accessibility actions accept data item descendants.
    @Test
    void listViewAccessibilityActionsAcceptDataItemDescendants() {
        M3ListItem first = new M3ListItem("First");
        M3Button firstAction = new M3Button("First action");
        first.setTrailing(firstAction);
        M3ListItem second = new M3ListItem("Second");
        M3Button secondAction = new M3Button("Second action");
        second.setTrailing(secondAction);
        M3ListView<M3ListItem> listView = listView(first, second);
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);

        listView.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, List.of(firstAction, secondAction));

        assertEquals(List.of(0, 1), listView.getSelectedIndices());
        assertEquals(List.of(first, second), listView.getSelectedItems());

        listView.clearFocus();
        listView.executeAccessibleAction(AccessibleAction.SHOW_ITEM, secondAction);

        assertEquals(1, listView.getFocusedIndex());
        assertSame(second, listView.getFocusedItem());
    }

    /// Verifies that virtualized list view selection ignores unreachable data items and descendants.
    @Test
    void listViewAccessibilitySelectionIgnoresUnreachableDataItemTargets() {
        M3ListItem hiddenRow = new M3ListItem("Hidden row");
        hiddenRow.setVisible(false);

        M3ListItem disabledRow = new M3ListItem("Disabled row");
        disabledRow.setDisable(true);

        M3Button hiddenAction = new M3Button("Hidden action");
        hiddenAction.setVisible(false);
        M3ListItem hiddenActionRow = new M3ListItem("Hidden action row");
        hiddenActionRow.setTrailing(hiddenAction);

        M3Button disabledAction = new M3Button("Disabled action");
        disabledAction.setDisable(true);
        M3ListItem disabledActionRow = new M3ListItem("Disabled action row");
        disabledActionRow.setTrailing(disabledAction);

        M3Button visibleAction = new M3Button("Visible action");
        M3ListItem visibleRow = new M3ListItem("Visible row");
        visibleRow.setTrailing(visibleAction);

        M3ListView<M3ListItem> listView = listView(
                hiddenRow,
                disabledRow,
                hiddenActionRow,
                disabledActionRow,
                visibleRow
        );
        listView.setSelectionMode(M3SelectionMode.MULTIPLE);

        listView.executeAccessibleAction(
                AccessibleAction.SET_SELECTED_ITEMS,
                hiddenRow,
                disabledRow,
                hiddenAction,
                disabledAction
        );

        assertTrue(listView.getSelectedItems().isEmpty());
        assertTrue(listView.getSelectedIndices().isEmpty());

        hiddenRow.setVisible(true);
        disabledRow.setDisable(false);
        hiddenAction.setVisible(true);
        disabledAction.setDisable(false);
        listView.executeAccessibleAction(
                AccessibleAction.SET_SELECTED_ITEMS,
                hiddenRow,
                disabledRow,
                hiddenAction,
                disabledAction,
                visibleAction
        );

        assertEquals(java.util.List.of(0, 1, 2, 3, 4), listView.getSelectedIndices());
        assertEquals(java.util.List.of(hiddenRow, disabledRow, hiddenActionRow, disabledActionRow, visibleRow),
                listView.getSelectedItems());
    }

    /// Verifies that focused virtualized list rows update after item changes.
    @Test
    void listViewRefreshesFocusedItemAfterDataChanges() {
        M3ListView<String> listView = listView("First", "Second", "Third");

        listView.focusIndex(1);
        listView.getItems().set(1, "Updated");

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Updated", listView.getFocusedItem());

        listView.getItems().remove(2);

        assertEquals(1, listView.getFocusedIndex());
        assertEquals("Updated", listView.getFocusedItem());

        listView.getItems().remove(1);

        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());
    }

    /// Verifies that focus convenience methods wrap around list data.
    @Test
    void listViewFocusConvenienceMethodsWrapAroundData() {
        M3ListView<String> listView = listView("First", "Second", "Third");

        listView.focusFirst();

        assertEquals(0, listView.getFocusedIndex());

        listView.focusPrevious();

        assertEquals(2, listView.getFocusedIndex());

        listView.focusNext();

        assertEquals(0, listView.getFocusedIndex());

        listView.focusLast();

        assertEquals(2, listView.getFocusedIndex());

        listView.clearFocus();

        assertEquals(-1, listView.getFocusedIndex());
        assertNull(listView.getFocusedItem());
    }

    /// Verifies that virtualized list view row content stays out of direct tab traversal.
    @Test
    void listViewKeepsVirtualRowsOutOfDirectTabTraversal() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 8; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(260.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> item.setHeadlineText("Row " + value)));
            Pane root = new Pane(listView);
            Scene scene = new Scene(root, 300.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                M3ListItem firstItem = Objects.requireNonNull(assertInstanceOf(
                        M3ListItem.class,
                        listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0)
                ));
                assertFalse(firstItem.isFocusTraversable());

                root.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_PRESSED, 1.0, 1.0, true));
                listView.focusIndex(0);
                root.layout();

                PseudoClass focusVisible = PseudoClass.getPseudoClass("focus-visible");
                assertFalse(firstItem.getPseudoClassStates().contains(focusVisible));

                Event.fireEvent(scene, keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                listView.focusIndex(0);
                root.layout();

                assertTrue(firstItem.getPseudoClassStates().contains(focusVisible));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list view cells create skins that render row graphics.
    @Test
    void listViewCellCreatesMaterialSkin() {
        M3ListCell<String> cell = new M3ListCell<>(new M3ListView<>());

        applyCss(cell);

        assertInstanceOf(M3ListViewCellSkin.class, cell.getSkin());
    }

    /// Verifies that virtualized list cells align row graphics to the logical visual start edge.
    @Tier2Test
    @Test
    void listViewCellAlignsReusableRowToVisualStartInRuntimeOrientation() {
        M3ListView<String> listView = listView("First");
        M3ListItem row = new M3ListItem("First");
        SimpleDoubleProperty rowWidth = new SimpleDoubleProperty(120.0);
        row.minWidthProperty().bind(rowWidth);
        row.prefWidthProperty().bind(rowWidth);
        row.maxWidthProperty().bind(rowWidth);
        row.setMinHeight(56.0);
        row.setPrefHeight(56.0);
        row.setMaxHeight(56.0);
        M3ListCell<String> cell = listCell(listView, row);
        Pane root = new Pane(cell);
        Scene scene = new Scene(root, 280.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        cell.updateIndex(0);
        root.layout();
        cell.resize(240.0, 56.0);
        cell.layout();

        assertSame(row, cell.getListItem());
        assertEquals(0.0, row.getLayoutX(), 0.0001);
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, row.getEffectiveNodeOrientation());
        assertEquals(
                cell.localToScene(cell.getBoundsInLocal()).getMinX(),
                row.localToScene(row.getBoundsInLocal()).getMinX(),
                0.0001
        );

        root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        cell.layout();

        assertEquals(0.0, row.getLayoutX(), 0.0001);
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, row.getEffectiveNodeOrientation());
        assertEquals(
                cell.localToScene(cell.getBoundsInLocal()).getMaxX(),
                row.localToScene(row.getBoundsInLocal()).getMaxX(),
                0.0001
        );

        root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        root.applyCss();
        cell.layout();

        assertEquals(0.0, row.getLayoutX(), 0.0001);
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, row.getEffectiveNodeOrientation());
        assertEquals(
                cell.localToScene(cell.getBoundsInLocal()).getMinX(),
                row.localToScene(row.getBoundsInLocal()).getMinX(),
                0.0001
        );
    }

    /// Verifies that cell layout stretches rows without overwriting application-owned width preferences.
    @Test
    void listViewCellPreservesUnboundReusableRowWidthPreference() {
        M3ListView<String> listView = listView("First");
        M3ListItem row = new M3ListItem("First");
        row.setPrefWidth(144.0);
        M3ListCell<String> cell = listCell(listView, row);
        Pane root = new Pane(cell);
        Scene scene = new Scene(root, 320.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        cell.updateIndex(0);
        root.layout();
        cell.resize(280.0, 56.0);
        cell.layout();

        assertSame(row, cell.getListItem());
        assertEquals(144.0, row.getPrefWidth(), 0.0001);
        assertEquals(280.0, row.getWidth(), 0.0001);
    }

    /// Verifies that virtualized list cells preserve application-bound row width constraints.
    @Test
    void listViewCellPreservesBoundReusableRowWidths() {
        M3ListView<String> listView = listView("First");
        M3ListItem row = new M3ListItem("First");
        SimpleDoubleProperty rowMinWidth = new SimpleDoubleProperty(40.0);
        SimpleDoubleProperty rowPrefWidth = new SimpleDoubleProperty(144.0);
        SimpleDoubleProperty rowMaxWidth = new SimpleDoubleProperty(220.0);
        SimpleBooleanProperty rowFocusTraversable = new SimpleBooleanProperty(true);
        SimpleBooleanProperty rowSelected = new SimpleBooleanProperty(true);
        row.minWidthProperty().bind(rowMinWidth);
        row.prefWidthProperty().bind(rowPrefWidth);
        row.maxWidthProperty().bind(rowMaxWidth);
        row.focusTraversableProperty().bind(rowFocusTraversable);
        row.selectedProperty().bind(rowSelected);
        try {
            M3ListCell<String> cell = listCell(listView, row);
            Pane root = new Pane(cell);
            Scene scene = new Scene(root, 320.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            cell.resize(280.0, 56.0);
            cell.updateIndex(0);
            root.layout();
            cell.layout();

            assertSame(row, cell.getListItem());
            assertEquals(40.0, row.getMinWidth(), 0.0001);
            assertEquals(144.0, row.getPrefWidth(), 0.0001);
            assertEquals(220.0, row.getMaxWidth(), 0.0001);
            assertTrue(row.isFocusTraversable());
            assertTrue(row.isSelected());

            rowMinWidth.set(44.0);
            rowPrefWidth.set(168.0);
            rowMaxWidth.set(240.0);
            rowFocusTraversable.set(false);
            rowSelected.set(false);
            cell.resize(300.0, 56.0);
            cell.layout();

            assertEquals(44.0, row.getMinWidth(), 0.0001);
            assertEquals(168.0, row.getPrefWidth(), 0.0001);
            assertEquals(240.0, row.getMaxWidth(), 0.0001);
            assertFalse(row.isFocusTraversable());
            assertFalse(row.isSelected());
        } finally {
            row.minWidthProperty().unbind();
            row.prefWidthProperty().unbind();
            row.maxWidthProperty().unbind();
            row.focusTraversableProperty().unbind();
            row.selectedProperty().unbind();
        }
    }

    /// Verifies that a virtualized cell retains one row while its data item changes.
    @Test
    void listViewCellReusesRowAcrossItemUpdates() {
        M3ListView<String> listView = listView("First", "Second");
        AtomicInteger rowCreations = new AtomicInteger();
        M3ListCell<String> cell = new M3ListCell<>(listView) {
            /// Creates the retained row once for this cell.
            @Override
            protected M3ListItem createListItem() {
                rowCreations.incrementAndGet();
                return new M3ListItem();
            }
        };

        cell.updateIndex(0);
        M3ListItem row = Objects.requireNonNull(cell.getListItem(), "retained row");

        assertEquals("First", row.getHeadlineText());
        assertEquals(1, rowCreations.get());

        cell.updateIndex(1);

        assertSame(row, cell.getListItem());
        assertEquals("Second", row.getHeadlineText());
        assertEquals(1, rowCreations.get());

        cell.updateIndex(-1);

        assertSame(row, cell.getListItem());
        assertNull(cell.getGraphic());
        assertEquals(1, rowCreations.get());
    }

    /// Verifies that visible virtualized rows follow runtime scene theme changes.
    @Test
    void listViewVisibleRowsReinheritRuntimeSceneThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("Alpha", "Beta", "Gamma");
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(320.0, 168.0);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            StackPane root = new StackPane(listView);
            Scene scene = new Scene(root, 360.0, 220.0);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(scene, baselineTheme);
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertInstanceOf(M3ListViewSkin.class, listView.getSkin());
                M3ListItem row = assertInstanceOf(
                        M3ListItem.class,
                        Objects.requireNonNull(
                                listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0),
                                "row"
                        )
                );

                assertSame(baselineTheme, M3ThemeResolver.findTheme(row));
                assertNull(M3ThemeManager.getTheme(row));
                assertFalse(row.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));

                M3ThemeManager.install(scene, expressiveDarkTheme);
                root.applyCss();
                root.layout();

                assertSame(expressiveDarkTheme, M3ThemeResolver.findTheme(row));
                assertNull(M3ThemeManager.getTheme(row));
                assertFalse(row.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that visible virtualized rows follow local parent theme changes and clearing.
    @Test
    void listViewVisibleRowsReinheritRuntimeLocalParentThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = listView("Alpha", "Beta", "Gamma");
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(320.0, 168.0);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Pane localRoot = new Pane(listView);
            StackPane root = new StackPane(localRoot);
            Scene scene = new Scene(root, 380.0, 240.0);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(localRoot, baselineTheme);
                stage.setScene(scene);
                stage.show();
                localRoot.resizeRelocate(0.0, 0.0, 360.0, 220.0);
                listView.resizeRelocate(20.0, 20.0, 320.0, 168.0);
                root.applyCss();
                root.layout();

                assertInstanceOf(M3ListViewSkin.class, listView.getSkin());
                M3ListItem row = assertInstanceOf(
                        M3ListItem.class,
                        Objects.requireNonNull(
                                listView.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0),
                                "row"
                        )
                );

                assertSame(baselineTheme, M3ThemeResolver.findTheme(row));
                assertNull(M3ThemeManager.getTheme(row));
                assertFalse(row.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));

                M3ThemeManager.install(localRoot, expressiveDarkTheme);
                root.applyCss();
                root.layout();

                assertSame(expressiveDarkTheme, M3ThemeResolver.findTheme(row));
                assertNull(M3ThemeManager.getTheme(row));
                assertFalse(row.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));

                M3ThemeManager.uninstall(localRoot);
                root.applyCss();
                root.layout();

                assertNull(M3ThemeResolver.findTheme(row));
                assertNull(M3ThemeManager.getTheme(row));
                assertFalse(row.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(row.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(row.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that virtualized list view rows are actually visible in rendered snapshots.
    @Tier2Test
    @Test
    void listViewSnapshotRendersVisibleRows() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<Integer> listView = new M3ListView<>();
            for (int i = 0; i < 24; i++) {
                listView.getItems().add(i);
            }
            listView.setFixedCellSize(56.0);
            listView.setPrefSize(320.0, 168.0);
            listView.setCellFactory(view -> listCell(view, (item, value) -> {
                item.setHeadlineText("Visible row " + value);
                item.setLeadingIcon(Integer.toString(value % 10));
            }));
            StackPane root = new StackPane(listView);
            Scene scene = new Scene(root, 360.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            WritableImage image = snapshotImageOnFxThread(listView);
            assertSnapshotAreaContainsContrast(
                    image,
                    12,
                    12,
                    240,
                    150,
                    Color.WHITE,
                    0.35,
                    "visible virtualized list rows"
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-list-view-virtualized.png"
            ));
        });
    }

    /// Returns whether a virtualized list exposes a focused row with the supplied headline.
    private static boolean listViewFocusNodeHasHeadline(
            AtomicReference<@Nullable M3ListView<Integer>> listViewReference,
            String headline
    ) {
        @Nullable M3ListView<Integer> listView = listViewReference.get();
        if (listView == null) {
            return false;
        }

        @Nullable Object focusNode = listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof M3ListItem item && headline.equals(item.getHeadlineText());
    }

    /// Asserts that a virtualized list exposes a focused row with the supplied headline.
    private static void assertAccessibleListItemHeadline(M3ListView<?> listView, String headline) {
        M3ListItem item = assertInstanceOf(
                M3ListItem.class,
                listView.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
        );
        assertEquals(headline, item.getHeadlineText());
    }

    /// Returns the expected normalized position after a virtualized list wheel-scroll delta.
    private static double expectedListViewWheelTargetPosition(
            M3ListView<?> listView,
            VirtualFlow<?> flow,
            double deltaY
    ) {
        double rowHeight = listView.getFixedCellSize();
        assertTrue(rowHeight > 0.0, () -> "fixedCellSize=" + rowHeight);
        double viewportHeight = flow.getHeight() > 0.0 ? flow.getHeight() : listView.getHeight();
        double scrollablePixels = Math.max(0.0, rowHeight * listView.getItems().size() - viewportHeight);
        assertTrue(scrollablePixels > 0.0, () -> "scrollablePixels=" + scrollablePixels);
        return clampToUnit(flow.getPosition() - deltaY / scrollablePixels);
    }
}
