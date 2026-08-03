// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Translate;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3TooltipRegistry;
import org.glavo.m3fx.skins.M3TreeViewSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies Material tree-view state, selection presentation, rendering, directionality, and accessibility.
@NotNullByDefault
final class M3TreeViewTest {
    /// The Material one-line list height used by default tree rows.
    private static final double MATERIAL_ONE_LINE_HEIGHT = 56.0;

    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies defaults, property ownership, validation, and mutually exclusive selection style classes.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void exposesStableStateProperties() {
        FxTestUtils.runOnFxThread(() -> {
            TreeItem<String> rootItem = new TreeItem<>("Root");
            M3TreeView<String> treeView = new M3TreeView<>(rootItem);

            assertSame(rootItem, treeView.getRoot());
            assertTrue(treeView.isShowRoot());
            assertEquals(SelectionMode.SINGLE, treeView.getSelectionModel().getSelectionMode());
            assertEquals(M3TreeViewSelectionStyle.HIGHLIGHT, treeView.getSelectionStyle());
            assertSame(treeView, treeView.selectionStyleProperty().getBean());
            assertEquals(AccessibleRole.TREE_VIEW, treeView.getAccessibleRole());
            assertTrue(treeView.isFocusTraversable());
            assertTrue(treeView.getStyleClass().contains("m3-tree-view"));
            assertTrue(treeView.getStyleClass().contains("m3-highlight-tree-selection"));
            assertTrue(treeView.getCellFactory().call(treeView) instanceof M3TreeCell<?>);

            treeView.setSelectionStyle(M3TreeViewSelectionStyle.CHECKBOX);
            assertTrue(treeView.getStyleClass().contains("m3-checkbox-tree-selection"));
            assertFalse(treeView.getStyleClass().contains("m3-highlight-tree-selection"));

            treeView.selectionStyleProperty().set(null);
            assertEquals(M3TreeViewSelectionStyle.HIGHLIGHT, treeView.getSelectionStyle());
            assertThrows(NullPointerException.class, () -> treeView.setSelectionStyle(null));

            M3TreeView<String> emptyTree = new M3TreeView<>();
            assertNull(emptyTree.getRoot());
        });
    }

    /// Verifies that expansion, visible-row indexing, and single and multiple selection retain JavaFX semantics.
    @Test
    void preservesTreeAndSelectionModels() {
        FxTestUtils.runOnFxThread(() -> {
            TreeItem<String> rootItem = hierarchy();
            M3TreeView<String> treeView = new M3TreeView<>(rootItem);

            assertEquals(1, treeView.getExpandedItemCount());
            rootItem.setExpanded(true);
            assertEquals(4, treeView.getExpandedItemCount());
            assertSame(rootItem.getChildren().get(1), treeView.getTreeItem(2));

            MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
            selectionModel.select(2);
            assertSame(rootItem.getChildren().get(1), selectionModel.getSelectedItem());

            selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
            selectionModel.selectIndices(1, 3);
            assertEquals(List.of(1, 2, 3), List.copyOf(selectionModel.getSelectedIndices()));

            treeView.setShowRoot(false);
            assertEquals(3, treeView.getExpandedItemCount());
            assertSame(rootItem.getChildren().get(0), treeView.getTreeItem(0));
        });
    }

    /// Verifies the Material one-line row height without replacing an application cell factory.
    @Test
    void usesMaterialOneLineRowHeight() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3TreeView<String> treeView = new M3TreeView<>(hierarchy());
            treeView.getRoot().setExpanded(true);
            treeView.setPrefSize(320.0, 260.0);
            StackPane root = themedRoot(treeView, 360.0, 280.0);
            layout(root, 360.0, 280.0);

            assertEquals(MATERIAL_ONE_LINE_HEIGHT, treeView.getFixedCellSize(), 0.001);
            for (M3TreeCell<?> cell : visibleCells(treeView)) {
                assertEquals(MATERIAL_ONE_LINE_HEIGHT, cell.getHeight(), 0.001);
            }

            treeView.setCellFactory(view -> new NamedTreeCell());
            layout(root, 360.0, 280.0);
            assertTrue(visibleCells(treeView).stream().allMatch(NamedTreeCell.class::isInstance));
        }));
    }

    /// Verifies highlight and checkbox selection presentation against the inherited selection model.
    @Test
    void switchesSelectionPresentationWithoutChangingSelection() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3TreeView<String> treeView = new M3TreeView<>(hierarchy());
            treeView.getRoot().setExpanded(true);
            treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            treeView.getSelectionModel().selectIndices(1, 3);
            treeView.setPrefSize(360.0, 260.0);
            StackPane root = themedRoot(treeView, 400.0, 280.0);
            layout(root, 400.0, 280.0);

            List<Integer> selectedBefore = List.copyOf(treeView.getSelectionModel().getSelectedIndices());
            assertTrue(visibleCells(treeView).stream()
                    .filter(TreeCell::isSelected)
                    .allMatch(cell -> cell.getSelectionCheckBox().getParent() == null));

            treeView.setSelectionStyle(M3TreeViewSelectionStyle.CHECKBOX);
            layout(root, 400.0, 280.0);
            assertEquals(selectedBefore, List.copyOf(treeView.getSelectionModel().getSelectedIndices()));
            for (M3TreeCell<?> cell : visibleCells(treeView)) {
                assertTrue(cell.getSelectionCheckBox().getParent() != null);
                assertEquals(cell.isSelected(), cell.getSelectionCheckBox().isSelected());
            }

            M3TreeCell<?> unselectedCell = visibleCells(treeView).stream()
                    .filter(cell -> cell.getIndex() == 2)
                    .findFirst()
                    .orElseThrow();
            unselectedCell.getSelectionCheckBox().fire();
            assertTrue(treeView.getSelectionModel().isSelected(2));
            assertTrue(unselectedCell.getSelectionCheckBox().isSelected());
            unselectedCell.getSelectionCheckBox().fire();
            assertFalse(treeView.getSelectionModel().isSelected(2));
            assertFalse(unselectedCell.getSelectionCheckBox().isSelected());
        }));
    }

    /// Verifies default text, TreeItem graphics, cell reuse clearing, and ellipsis-only full-text help.
    @Test
    void defaultCellRendersTreeItemContentAndTruncationHelp() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            String longText = "A deliberately long project folder name that cannot fit in the compact tree";
            Label graphic = new Label("G");
            TreeItem<String> rootItem = new TreeItem<>(longText, graphic);
            rootItem.getChildren().add(new TreeItem<>("Child"));
            M3TreeView<String> treeView = new M3TreeView<>(rootItem);
            treeView.setPrefSize(180.0, 120.0);
            treeView.setMaxWidth(180.0);
            StackPane root = themedRoot(treeView, 1240.0, 140.0);
            layout(root, 1240.0, 140.0);

            M3TreeCell<?> cell = visibleCells(treeView).get(0);
            assertEquals(longText, cell.getText());
            assertSame(graphic, cell.getGraphic());
            assertEquals(AccessibleRole.TREE_ITEM, cell.getAccessibleRole());
            assertEquals(longText, cell.getFullTextTooltip().getText());
            assertEquals(longText, cell.getAccessibleHelp());
            assertTrue(M3TooltipRegistry.installation(cell) != null);

            treeView.setPrefWidth(1200.0);
            treeView.setMaxWidth(1200.0);
            layout(root, 1240.0, 140.0);
            cell = visibleCells(treeView).get(0);
            assertNull(cell.getFullTextTooltip().getText());
            assertNull(cell.getAccessibleHelp());
            assertNull(M3TooltipRegistry.installation(cell));

            M3TreeCell<String> reusableCell = new M3TreeCell<>();
            reusableCell.updateItem("Visible", false);
            assertEquals("Visible", reusableCell.getText());
            reusableCell.updateItem(null, true);
            assertNull(reusableCell.getText());
            assertNull(reusableCell.getGraphic());
            assertNull(reusableCell.getFullTextTooltip().getText());
        }));
    }

    /// Verifies Material selected surfaces, shared scrollbar styling, and the TreeView skin lifecycle.
    @Test
    void stylesMaterialRowsAndPreservesStockSkinLifecycle() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3TreeView<String> treeView = new M3TreeView<>(hierarchy());
            treeView.getRoot().setExpanded(true);
            treeView.getSelectionModel().select(1);
            treeView.setPrefSize(320.0, 220.0);
            StackPane root = themedRoot(treeView, 360.0, 240.0);
            layout(root, 360.0, 240.0);

            assertTrue(treeView.getSkin() instanceof M3TreeViewSkin<?>);
            List<ScrollBar> scrollBars = treeView.lookupAll(".scroll-bar").stream()
                    .filter(ScrollBar.class::isInstance)
                    .map(ScrollBar.class::cast)
                    .toList();
            assertEquals(2, scrollBars.size());
            assertTrue(scrollBars.stream()
                    .allMatch(scrollBar -> scrollBar.getStyleClass().contains("m3-scroll-bar")));
            M3TreeCell<?> selectedCell = visibleCells(treeView).stream()
                    .filter(TreeCell::isSelected)
                    .findFirst()
                    .orElseThrow();
            assertFalse(selectedCell.getBackground().getFills().isEmpty());

            Object originalSkin = treeView.getSkin();
            TreeViewSkin<String> replacementSkin = new TreeViewSkin<>(treeView);
            treeView.setSkin(replacementSkin);
            layout(root, 360.0, 240.0);
            assertNotSame(originalSkin, treeView.getSkin());
            assertSame(replacementSkin, treeView.getSkin());
            assertEquals(1, treeView.lookupAll(".virtual-flow").size());
        }));
    }

    /// Verifies fixed disclosure columns, Material indicator state, and sibling content alignment.
    @Test
    void alignsAndAnimatesMaterialDisclosureIndicators() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            TreeItem<String> rootItem = new TreeItem<>("Workspace");
            TreeItem<String> branchItem = new TreeItem<>("Branch");
            branchItem.getChildren().add(new TreeItem<>("Nested"));
            rootItem.getChildren().addAll(List.of(branchItem, new TreeItem<>("Leaf")));
            rootItem.setExpanded(true);

            M3TreeView<String> treeView = new M3TreeView<>(rootItem);
            treeView.setPrefSize(360.0, 220.0);
            StackPane root = themedRoot(treeView, 400.0, 240.0);
            layout(root, 400.0, 240.0);

            List<M3TreeCell<?>> cells = visibleCells(treeView);
            M3TreeCell<?> rootCell = cells.get(0);
            M3TreeCell<?> branchCell = cells.get(1);
            M3TreeCell<?> leafCell = cells.get(2);
            Node rootDisclosure = rootCell.getDisclosureNode();
            Node branchDisclosure = branchCell.getDisclosureNode();
            Node rootArrow = rootDisclosure.lookup(".m3-disclosure-icon-shape");
            Node branchArrow = branchDisclosure.lookup(".m3-disclosure-icon-shape");

            assertEquals(40.0, rootDisclosure.getLayoutBounds().getWidth(), 0.001);
            assertEquals(40.0, branchDisclosure.getLayoutBounds().getWidth(), 0.001);
            assertEquals(24.0, centerX(branchDisclosure) - centerX(rootDisclosure), 0.001);
            assertEquals(centerY(rootCell), centerY(rootArrow), 0.001);
            assertEquals(centerY(branchCell), centerY(branchArrow), 0.001);
            assertEquals(minX(branchCell.lookup(".text")), minX(leafCell.lookup(".text")), 0.001);

            Node branchIndicator = branchDisclosure.lookup(".m3-disclosure-icon");
            assertTrue(branchIndicator != null);
            assertFalse(branchIndicator.getPseudoClassStates().contains(
                    javafx.css.PseudoClass.getPseudoClass("expanded")
            ));
            branchItem.setExpanded(true);
            assertTrue(branchIndicator.getPseudoClassStates().contains(
                    javafx.css.PseudoClass.getPseudoClass("expanded")
            ));
        }));
    }

    /// Verifies that expanding a tree branch produces a real intermediate Material rotation frame.
    @Test
    void animatesDisclosureRotationBetweenCollapsedAndExpandedStates() throws InterruptedException {
        AtomicReference<@Nullable M3TreeView<String>> treeReference = new AtomicReference<>();
        AtomicReference<@Nullable TreeItem<String>> branchReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> {
                    @Nullable Node arrow = visibleDisclosureArrow(treeReference.get(), branchReference.get());
                    return arrow != null && arrow.getRotate() > -89.0 && arrow.getRotate() < -1.0;
                },
                () -> {
                    @Nullable Node arrow = visibleDisclosureArrow(treeReference.get(), branchReference.get());
                    return "Timed out waiting for disclosure rotation; current rotation="
                            + (arrow == null ? "unavailable" : arrow.getRotate());
                },
                () -> {
                    M3MotionSettings.setGlobalReducedMotionRequested(false);
                    TreeItem<String> rootItem = new TreeItem<>("Workspace");
                    TreeItem<String> branchItem = new TreeItem<>("Branch");
                    branchItem.getChildren().add(new TreeItem<>("Nested"));
                    rootItem.getChildren().add(branchItem);
                    rootItem.setExpanded(true);

                    M3TreeView<String> treeView = new M3TreeView<>(rootItem);
                    treeView.setPrefSize(360.0, 180.0);
                    StackPane root = themedRoot(treeView, 400.0, 200.0);
                    layout(root, 400.0, 200.0);

                    M3TreeCell<?> branchCell = visibleCells(treeView).get(1);
                    Node arrow = branchCell.getDisclosureNode().lookup(".m3-disclosure-icon-shape");
                    assertEquals(-90.0, arrow.getRotate(), 0.001);
                    treeReference.set(treeView);
                    branchReference.set(branchItem);
                    branchItem.setExpanded(true);
                },
                () -> {
                    Node arrow = java.util.Objects.requireNonNull(
                            visibleDisclosureArrow(treeReference.get(), branchReference.get()),
                            "disclosure arrow"
                    );
                    assertTrue(arrow.getRotate() > -89.0 && arrow.getRotate() < -1.0);
                }
        );
    }

    /// Verifies that expansion moves newly revealed descendants without overlapping following rows.
    @Test
    void animatesRowsWhileExpandingABranch() throws InterruptedException {
        AtomicReference<@Nullable M3TreeView<String>> treeReference = new AtomicReference<>();
        AtomicReference<@Nullable TreeItem<String>> nestedReference = new AtomicReference<>();
        AtomicReference<@Nullable TreeItem<String>> siblingReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> {
                    @Nullable M3TreeView<String> treeView = treeReference.get();
                    @Nullable TreeItem<String> nestedItem = nestedReference.get();
                    @Nullable TreeItem<String> siblingItem = siblingReference.get();
                    if (treeView == null || nestedItem == null || siblingItem == null) {
                        return false;
                    }
                    treeView.getParent().layout();
                    @Nullable M3TreeCell<?> nestedCell = visibleCell(treeView, nestedItem);
                    @Nullable M3TreeCell<?> siblingCell = visibleCell(treeView, siblingItem);
                    return hasActiveRowMotion(nestedCell, -1.0)
                            && siblingCell != null
                            && hasMotionStyle(siblingCell)
                            && Math.abs(rowMotionOffset(siblingCell)) <= 0.001;
                },
                () -> {
                    M3MotionSettings.setGlobalReducedMotionRequested(false);
                    TreeItem<String> rootItem = new TreeItem<>("Workspace");
                    TreeItem<String> branchItem = new TreeItem<>("Branch");
                    TreeItem<String> nestedItem = new TreeItem<>("Nested");
                    TreeItem<String> siblingItem = new TreeItem<>("Sibling");
                    branchItem.getChildren().add(nestedItem);
                    rootItem.getChildren().addAll(List.of(branchItem, siblingItem));
                    rootItem.setExpanded(true);

                    M3TreeView<String> treeView = new M3TreeView<>(rootItem);
                    treeView.setPrefSize(360.0, 240.0);
                    StackPane root = themedRoot(treeView, 400.0, 260.0);
                    layout(root, 400.0, 260.0);
                    treeReference.set(treeView);
                    nestedReference.set(nestedItem);
                    siblingReference.set(siblingItem);
                    branchItem.setExpanded(true);
                },
                () -> {
                    M3TreeView<String> treeView = java.util.Objects.requireNonNull(treeReference.get(), "tree view");
                    M3TreeCell<?> nestedCell = java.util.Objects.requireNonNull(
                            visibleCell(treeView, java.util.Objects.requireNonNull(nestedReference.get(), "nested item")),
                            "nested cell"
                    );
                    M3TreeCell<?> siblingCell = java.util.Objects.requireNonNull(
                            visibleCell(treeView, java.util.Objects.requireNonNull(siblingReference.get(), "sibling item")),
                            "sibling cell"
                    );
                    assertTrue(rowMotionOffset(nestedCell) < -1.0);
                    assertEquals(0.0, rowMotionOffset(siblingCell), 0.001);
                    M3MotionSettings.setReducedMotionRequested(treeView, true);
                    assertFalse(hasMotionStyle(nestedCell));
                    assertFalse(hasMotionStyle(siblingCell));
                    assertEquals(0.0, rowMotionOffset(nestedCell), 0.001);
                    assertEquals(0.0, rowMotionOffset(siblingCell), 0.001);
                }
        );
    }

    /// Verifies that collapse animates following rows upward without leaving a reused-cell motion surface.
    @Test
    void animatesRowsWhileCollapsingABranch() throws InterruptedException {
        AtomicReference<@Nullable M3TreeView<String>> treeReference = new AtomicReference<>();
        AtomicReference<@Nullable TreeItem<String>> siblingReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> {
                    @Nullable M3TreeView<String> treeView = treeReference.get();
                    @Nullable TreeItem<String> siblingItem = siblingReference.get();
                    if (treeView == null || siblingItem == null) {
                        return false;
                    }
                    treeView.getParent().layout();
                    return hasActiveRowMotion(visibleCell(treeView, siblingItem), 1.0);
                },
                () -> {
                    M3MotionSettings.setGlobalReducedMotionRequested(false);
                    TreeItem<String> rootItem = new TreeItem<>("Workspace");
                    TreeItem<String> branchItem = new TreeItem<>("Branch");
                    TreeItem<String> siblingItem = new TreeItem<>("Sibling");
                    branchItem.getChildren().addAll(List.of(
                            new TreeItem<>("Nested one"),
                            new TreeItem<>("Nested two")
                    ));
                    branchItem.setExpanded(true);
                    rootItem.getChildren().addAll(List.of(branchItem, siblingItem));
                    rootItem.setExpanded(true);

                    M3TreeView<String> treeView = new M3TreeView<>(rootItem);
                    treeView.setPrefSize(360.0, 300.0);
                    StackPane root = themedRoot(treeView, 400.0, 320.0);
                    layout(root, 400.0, 320.0);
                    treeReference.set(treeView);
                    siblingReference.set(siblingItem);
                    branchItem.setExpanded(false);
                },
                () -> {
                    M3TreeView<String> treeView = java.util.Objects.requireNonNull(treeReference.get(), "tree view");
                    M3TreeCell<?> siblingCell = java.util.Objects.requireNonNull(
                            visibleCell(treeView, java.util.Objects.requireNonNull(siblingReference.get(), "sibling item")),
                            "sibling cell"
                    );
                    assertTrue(rowMotionOffset(siblingCell) > 1.0);
                    assertTrue(hasMotionStyle(siblingCell));
                    M3MotionSettings.setReducedMotionRequested(treeView, true);
                    assertFalse(hasMotionStyle(siblingCell));
                    assertEquals(0.0, rowMotionOffset(siblingCell), 0.001);
                }
        );
    }

    /// Verifies stock arrow-key expansion and that the disclosure affordance follows logical leading in RTL.
    @Test
    void supportsKeyboardExpansionAndRightToLeftLayout() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            TreeItem<String> rootItem = hierarchy();
            M3TreeView<String> treeView = new M3TreeView<>(rootItem);
            treeView.setPrefSize(320.0, 180.0);
            StackPane root = themedRoot(treeView, 360.0, 200.0);
            layout(root, 360.0, 200.0);
            treeView.getSelectionModel().select(0);
            treeView.getFocusModel().focus(0);

            treeView.fireEvent(keyEvent(KeyCode.RIGHT));
            assertTrue(rootItem.isExpanded());
            treeView.fireEvent(keyEvent(KeyCode.DOWN));
            assertEquals(1, treeView.getSelectionModel().getSelectedIndex());
            treeView.fireEvent(keyEvent(KeyCode.LEFT));
            assertEquals(0, treeView.getSelectionModel().getSelectedIndex());
            treeView.fireEvent(keyEvent(KeyCode.LEFT));
            assertFalse(rootItem.isExpanded());

            M3TreeCell<?> rootCell = visibleCells(treeView).get(0);
            Node disclosure = rootCell.getDisclosureNode();
            Node text = rootCell.lookup(".text");
            assertTrue(centerX(disclosure) < centerX(text));

            treeView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout(root, 360.0, 200.0);
            rootCell = visibleCells(treeView).get(0);
            disclosure = rootCell.getDisclosureNode();
            text = rootCell.lookup(".text");
            assertTrue(centerX(disclosure) > centerX(text));

            treeView.getSelectionModel().select(0);
            treeView.getFocusModel().focus(0);
            treeView.fireEvent(keyEvent(KeyCode.LEFT));
            assertTrue(rootItem.isExpanded(), "RTL Left should expand toward the nested content");
            treeView.fireEvent(keyEvent(KeyCode.RIGHT));
            assertFalse(rootItem.isExpanded(), "RTL Right should collapse toward the parent level");
        }));
    }

    /// Creates a representative two-level hierarchy.
    ///
    /// @return a collapsed root with three children
    private static TreeItem<String> hierarchy() {
        TreeItem<String> root = new TreeItem<>("Workspace");
        root.getChildren().addAll(List.of(
                new TreeItem<>("Applications"),
                new TreeItem<>("Libraries"),
                new TreeItem<>("Documentation")
        ));
        return root;
    }

    /// Creates a themed scene root containing the supplied tree view.
    ///
    /// @param treeView the tree view to present
    /// @param width    the scene width
    /// @param height   the scene height
    /// @return the scene root
    private static StackPane themedRoot(M3TreeView<?> treeView, double width, double height) {
        StackPane root = new StackPane(treeView);
        Scene scene = new Scene(root, width, height);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        return root;
    }

    /// Applies CSS and performs one deterministic layout pass.
    ///
    /// @param root   the scene root
    /// @param width  the root width
    /// @param height the root height
    private static void layout(StackPane root, double width, double height) {
        root.applyCss();
        root.resize(width, height);
        root.layout();
    }

    /// Returns non-empty Material cells ordered by their visible row index.
    ///
    /// @param treeView the owning tree view
    /// @return the ordered visible cells
    private static List<M3TreeCell<?>> visibleCells(M3TreeView<?> treeView) {
        ArrayList<M3TreeCell<?>> cells = new ArrayList<>();
        for (Node node : treeView.lookupAll(".m3-tree-cell")) {
            if (node instanceof M3TreeCell<?> cell && !cell.isEmpty()) {
                cells.add(cell);
            }
        }
        cells.sort(Comparator.comparingInt(TreeCell::getIndex));
        return List.copyOf(cells);
    }

    /// Returns the currently visible cell representing an item by identity.
    ///
    /// @param treeView the owning tree view
    /// @param item     the logical item to locate
    /// @return the materialized cell, or `null` when the item is outside the viewport
    private static @Nullable M3TreeCell<?> visibleCell(M3TreeView<?> treeView, TreeItem<?> item) {
        for (M3TreeCell<?> cell : visibleCells(treeView)) {
            if (cell.getTreeItem() == item) {
                return cell;
            }
        }
        return null;
    }

    /// Returns the disclosure arrow for a currently visible logical item.
    ///
    /// @param treeView the owning tree view, or `null`
    /// @param item     the logical item, or `null`
    /// @return the disclosure arrow, or `null` when the item is not materialized
    private static @Nullable Node visibleDisclosureArrow(
            @Nullable M3TreeView<?> treeView,
            @Nullable TreeItem<?> item
    ) {
        if (treeView == null || item == null) {
            return null;
        }
        @Nullable M3TreeCell<?> cell = visibleCell(treeView, item);
        return cell == null ? null : cell.getDisclosureNode().lookup(".m3-disclosure-icon-shape");
    }

    /// Returns whether a cell carries the temporary branch-motion style and exceeds a signed offset threshold.
    ///
    /// @param cell               the cell to inspect, or `null`
    /// @param signedMinMagnitude a positive threshold for downward motion or a negative threshold for upward motion
    /// @return `true` when the expected intermediate motion frame is present
    private static boolean hasActiveRowMotion(
            @Nullable M3TreeCell<?> cell,
            double signedMinMagnitude
    ) {
        if (cell == null || !hasMotionStyle(cell)) {
            return false;
        }
        double offset = rowMotionOffset(cell);
        return signedMinMagnitude < 0.0 ? offset < signedMinMagnitude : offset > signedMinMagnitude;
    }

    /// Returns whether a cell carries the temporary branch-motion style class.
    ///
    /// @param cell the cell to inspect
    /// @return `true` while branch motion owns the cell's transient presentation
    private static boolean hasMotionStyle(M3TreeCell<?> cell) {
        return cell.getStyleClass().contains("m3-tree-row-motion");
    }

    /// Returns the largest vertical translation currently applied to a row by absolute magnitude.
    ///
    /// @param cell the cell to inspect
    /// @return the signed vertical translation, or zero when no translation is present
    private static double rowMotionOffset(M3TreeCell<?> cell) {
        double result = 0.0;
        for (javafx.scene.transform.Transform transform : cell.getTransforms()) {
            if (transform instanceof Translate translation && Math.abs(translation.getY()) > Math.abs(result)) {
                result = translation.getY();
            }
        }
        return result;
    }

    /// Returns a node's horizontal center in scene coordinates.
    ///
    /// @param node the node to measure
    /// @return the scene-coordinate center x value
    private static double centerX(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return bounds.getCenterX();
    }

    /// Returns a node's vertical center in scene coordinates.
    ///
    /// @param node the node to measure
    /// @return the scene-coordinate center y value
    private static double centerY(Node node) {
        return node.localToScene(node.getBoundsInLocal()).getCenterY();
    }

    /// Returns a node's leading horizontal edge in scene coordinates.
    ///
    /// @param node the node to measure
    /// @return the scene-coordinate minimum x value
    private static double minX(Node node) {
        return node.localToScene(node.getBoundsInLocal()).getMinX();
    }

    /// Creates an unmodified key-pressed event.
    ///
    /// @param code the key code
    /// @return the key event
    private static KeyEvent keyEvent(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /// A custom cell type used to verify that Material styling preserves application factories.
    @NotNullByDefault
    private static final class NamedTreeCell extends M3TreeCell<String> {
        /// Creates an empty named cell.
        private NamedTreeCell() {
            getStyleClass().add("named-tree-cell");
        }
    }
}
