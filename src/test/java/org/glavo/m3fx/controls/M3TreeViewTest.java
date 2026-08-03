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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies Material tree-view state, inherited behavior, rendering, directionality, and accessibility.
@NotNullByDefault
final class M3TreeViewTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies defaults, property ownership, validation, and mutually exclusive style classes.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void exposesStableStateProperties() {
        FxTestUtils.runOnFxThread(() -> {
            TreeItem<String> rootItem = new TreeItem<>("Root");
            M3TreeView<String> treeView = new M3TreeView<>(rootItem);

            assertSame(rootItem, treeView.getRoot());
            assertTrue(treeView.isShowRoot());
            assertEquals(SelectionMode.SINGLE, treeView.getSelectionModel().getSelectionMode());
            assertEquals(M3TreeViewSize.MEDIUM, treeView.getSize());
            assertEquals(M3TreeViewStyle.STANDARD, treeView.getTreeStyle());
            assertSame(treeView, treeView.sizeProperty().getBean());
            assertSame(treeView, treeView.treeStyleProperty().getBean());
            assertEquals(AccessibleRole.TREE_VIEW, treeView.getAccessibleRole());
            assertTrue(treeView.isFocusTraversable());
            assertTrue(treeView.getStyleClass().contains("m3-tree-view"));
            assertTrue(treeView.getStyleClass().contains("m3-medium-tree-view"));
            assertTrue(treeView.getStyleClass().contains("m3-standard-tree-view"));
            assertTrue(treeView.getCellFactory().call(treeView) instanceof M3TreeCell<?>);

            treeView.setSize(M3TreeViewSize.EXTRA_LARGE);
            assertTrue(treeView.getStyleClass().contains("m3-extra-large-tree-view"));
            assertFalse(treeView.getStyleClass().contains("m3-medium-tree-view"));
            treeView.setTreeStyle(M3TreeViewStyle.DETACHED);
            assertTrue(treeView.getStyleClass().contains("m3-detached-tree-view"));
            assertFalse(treeView.getStyleClass().contains("m3-standard-tree-view"));

            treeView.sizeProperty().set(null);
            treeView.treeStyleProperty().set(null);
            assertEquals(M3TreeViewSize.MEDIUM, treeView.getSize());
            assertEquals(M3TreeViewStyle.STANDARD, treeView.getTreeStyle());
            assertThrows(NullPointerException.class, () -> treeView.setSize(null));
            assertThrows(NullPointerException.class, () -> treeView.setTreeStyle(null));

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

    /// Verifies that each size role resolves the documented fixed row height without replacing a custom factory.
    @Test
    void sizeRolesControlVirtualizedRowHeight() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3TreeView<String> treeView = new M3TreeView<>(hierarchy());
            treeView.getRoot().setExpanded(true);
            treeView.setPrefSize(320.0, 260.0);
            StackPane root = themedRoot(treeView, 360.0, 280.0);

            for (M3TreeViewSize size : M3TreeViewSize.values()) {
                treeView.setSize(size);
                layout(root, 360.0, 280.0);
                for (M3TreeCell<?> cell : visibleCells(treeView)) {
                    assertEquals(size.getRowHeight(), cell.getHeight(), 0.001, () -> size + " row height");
                }
            }

            treeView.setCellFactory(view -> new NamedTreeCell());
            treeView.setSize(M3TreeViewSize.SMALL);
            layout(root, 360.0, 280.0);
            assertTrue(visibleCells(treeView).stream().allMatch(NamedTreeCell.class::isInstance));
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

            treeView.setPrefWidth(1200.0);
            treeView.setMaxWidth(1200.0);
            layout(root, 1240.0, 140.0);
            assertTrue(treeView.getWidth() > 1000.0, () -> "expanded tree width: " + treeView.getWidth());
            cell = visibleCells(treeView).get(0);
            assertNull(cell.getFullTextTooltip().getText());
            assertNull(cell.getAccessibleHelp());

            M3TreeCell<String> reusableCell = new M3TreeCell<>();
            reusableCell.updateItem("Visible", false);
            assertEquals("Visible", reusableCell.getText());
            reusableCell.updateItem(null, true);
            assertNull(reusableCell.getText());
            assertNull(reusableCell.getGraphic());
            assertNull(reusableCell.getFullTextTooltip().getText());

            String nestedLongText =
                    "A deliberately long generated-resources directory whose complete name appears in a tooltip";
            TreeItem<String> nestedRootItem = new TreeItem<>("M3FX workspace");
            nestedRootItem.getChildren().add(new TreeItem<>(nestedLongText));
            nestedRootItem.setExpanded(true);
            M3TreeView<String> nestedTreeView = new M3TreeView<>(nestedRootItem);
            nestedTreeView.setPrefSize(400.0, 120.0);
            nestedTreeView.setMaxWidth(400.0);
            StackPane nestedRoot = themedRoot(nestedTreeView, 440.0, 140.0);
            layout(nestedRoot, 440.0, 140.0);
            M3TreeCell<?> nestedCell = visibleCells(nestedTreeView).stream()
                    .filter(candidate -> nestedLongText.equals(candidate.getText()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(nestedLongText, nestedCell.getFullTextTooltip().getText());
        }));
    }

    /// Verifies Material row surfaces, selected colors, detached insets, and stock skin lifecycle.
    @Test
    void stylesStandardAndDetachedRows() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3TreeView<String> treeView = new M3TreeView<>(hierarchy());
            treeView.getRoot().setExpanded(true);
            treeView.setPrefSize(320.0, 220.0);
            StackPane root = themedRoot(treeView, 360.0, 240.0);
            layout(root, 360.0, 240.0);

            assertTrue(treeView.getSkin() instanceof TreeViewSkin<?>);
            M3TreeCell<?> standardCell = visibleCells(treeView).get(0);
            assertTrue(standardCell.getBackground().getFills().isEmpty()
                    || standardCell.getBackground().getFills().get(0).getInsets().getLeft() == 0.0);

            treeView.setTreeStyle(M3TreeViewStyle.DETACHED);
            treeView.getSelectionModel().select(1);
            layout(root, 360.0, 240.0);
            M3TreeCell<?> detachedCell = visibleCells(treeView).stream()
                    .filter(cell -> cell.getIndex() == 0)
                    .findFirst()
                    .orElseThrow();
            M3TreeCell<?> selectedCell = visibleCells(treeView).stream()
                    .filter(TreeCell::isSelected)
                    .findFirst()
                    .orElseThrow();
            assertFalse(detachedCell.getBackground().getFills().isEmpty());
            assertEquals(8.0, detachedCell.getBackground().getFills().get(0).getInsets().getLeft(), 0.001);
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
            Node disclosure = rootCell.lookup(".tree-disclosure-node");
            Node text = rootCell.lookup(".text");
            assertTrue(centerX(disclosure) < centerX(text));

            treeView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout(root, 360.0, 200.0);
            rootCell = visibleCells(treeView).get(0);
            disclosure = rootCell.lookup(".tree-disclosure-node");
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

    /// Returns a node's horizontal center in scene coordinates.
    ///
    /// @param node the node to measure
    /// @return the scene-coordinate center x value
    private static double centerX(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return bounds.getCenterX();
    }

    /// Creates an unmodified key-pressed event.
    ///
    /// @param code the key code
    /// @return the key event
    private static KeyEvent keyEvent(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /// A custom cell type used to verify that size updates preserve application factories.
    @NotNullByDefault
    private static final class NamedTreeCell extends M3TreeCell<String> {
        /// Creates an empty named cell.
        private NamedTreeCell() {
            getStyleClass().add("named-tree-cell");
        }
    }
}
