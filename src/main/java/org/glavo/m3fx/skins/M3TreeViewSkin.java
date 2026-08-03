// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.transform.Translate;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3TreeView;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.animation.M3DoubleTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/// The default virtualized skin for [M3TreeView].
///
/// The skin preserves JavaFX tree navigation and virtualization while applying the same standalone Material
/// scrollbar styling used by [M3ListViewSkin]. Cells newly revealed by expansion enter from the branch edge;
/// collapsing a branch animates the following visible rows from their preceding positions. The motion is applied
/// through private transforms, so application-owned translation properties remain unchanged.
///
/// @param <T> the tree-item value type
@NotNullByDefault
public final class M3TreeViewSkin<T> extends TreeViewSkin<T> {
    /// The temporary style class applied while virtualized rows are being reassigned for branch motion.
    private static final String ROW_MOTION_STYLE_CLASS = "m3-tree-row-motion";

    /// The maximum vertical entry offset for a newly revealed row, in logical pixels.
    private static final double REVEALED_ROW_OFFSET = 12.0;

    /// The minimum position difference that warrants a rendered transform, in logical pixels.
    private static final double POSITION_VISIBILITY_THRESHOLD = 0.5;

    /// The current normalized branch-motion progress.
    private final DoubleProperty rowMotionProgress = new SimpleDoubleProperty(this, "rowMotionProgress", 1.0) {
        /// Applies the new progress to every retained row placement.
        @Override
        protected void invalidated() {
            updateRowMotionProgress();
        }
    };

    /// The reusable transition that advances [#rowMotionProgress].
    private final M3DoubleTransition rowMotionAnimation = new M3DoubleTransition(
            rowMotionProgress,
            M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
            0.0,
            1.0
    );

    /// The visible logical rows participating in the current motion run.
    private final ArrayList<RowPlacement<T>> rowPlacements = new ArrayList<>();

    /// Cells carrying the temporary motion style, including cells without a position delta.
    private final ArrayList<TreeCell<T>> styledMotionCells = new ArrayList<>();

    /// Receives expanded-count events from the current root and all of its descendants.
    private final EventHandler<TreeItem.TreeModificationEvent<T>> expandedItemCountListener =
            this::handleExpandedItemCountChange;

    /// Rebinds the bubbling expanded-count listener when the tree root changes.
    private final ChangeListener<@Nullable TreeItem<T>> rootListener =
            (observable, oldRoot, newRoot) -> updateObservedRoot(oldRoot, newRoot);

    /// The branch change waiting for the virtual flow's next normal layout pass, or `null`.
    private @Nullable PendingRowMotion<T> pendingRowMotion;

    /// Whether this skin has released its listeners and must ignore deferred work.
    private boolean disposed;

    /// Creates a Material tree-view skin.
    ///
    /// @param control the skinned Material tree view
    public M3TreeViewSkin(M3TreeView<T> control) {
        super(control);
        rowMotionAnimation.setOnFinished(event -> clearRowMotion());
        control.rootProperty().addListener(rootListener);
        updateObservedRoot(null, control.getRoot());
    }

    /// Removes root observation and private row transforms before disposal.
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        pendingRowMotion = null;
        M3TreeView<T> control = materialTreeView();
        control.rootProperty().removeListener(rootListener);
        updateObservedRoot(control.getRoot(), null);
        rowMotionAnimation.setOnFinished(null);
        clearRowMotion();
        super.dispose();
    }

    /// Creates the virtual flow whose scrollbars use the shared Material scroll contract.
    ///
    /// @return a Material-styled tree virtual flow
    @Override
    protected VirtualFlow<TreeCell<T>> createVirtualFlow() {
        return new TreeViewVirtualFlow<>();
    }

    /// Lays out the virtual flow and then derives row transforms from the completed logical-item reassignment.
    ///
    /// @param x      the content area's x coordinate
    /// @param y      the content area's y coordinate
    /// @param width  the content area's width
    /// @param height the content area's height
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        @Nullable PendingRowMotion<T> pending = pendingRowMotion;
        if (pending == null) {
            return;
        }
        pendingRowMotion = null;
        prepareRowMotion(pending.precedingPositions(), pending.changedBranch(), pending.expanding());
    }

    /// Moves expanded-count observation from one root hierarchy to another.
    ///
    /// @param oldRoot the previously observed root, or `null`
    /// @param newRoot the root to observe, or `null`
    private void updateObservedRoot(@Nullable TreeItem<T> oldRoot, @Nullable TreeItem<T> newRoot) {
        if (oldRoot != null) {
            oldRoot.removeEventHandler(TreeItem.<T>expandedItemCountChangeEvent(), expandedItemCountListener);
        }
        if (newRoot != null && !disposed) {
            newRoot.addEventHandler(TreeItem.<T>expandedItemCountChangeEvent(), expandedItemCountListener);
        }
    }

    /// Captures the rendered row positions before JavaFX lays out a changed expanded-item count.
    ///
    /// @param event the bubbling branch expansion or collapse event
    private void handleExpandedItemCountChange(TreeItem.TreeModificationEvent<T> event) {
        if (disposed || !event.wasExpanded() && !event.wasCollapsed()) {
            return;
        }

        IdentityHashMap<TreeItem<T>, Double> precedingPositions = captureRenderedRowPositions();
        clearRowMotion();
        markVisibleCellsForMotion();
        pendingRowMotion = new PendingRowMotion<>(precedingPositions, event.getTreeItem(), event.wasExpanded());
        getSkinnable().requestLayout();
    }

    /// Captures the current scene-coordinate top edge for each materialized logical row.
    ///
    /// @return an identity map from visible tree items to their rendered vertical positions
    private IdentityHashMap<TreeItem<T>, Double> captureRenderedRowPositions() {
        IdentityHashMap<TreeItem<T>, Double> positions = new IdentityHashMap<>();
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (isRenderedCell(cell, item)) {
                positions.put(item, sceneTop(cell));
            }
        }
        return positions;
    }

    /// Maps reusable cells back to logical items after layout and starts row motion.
    ///
    /// @param precedingPositions scene positions captured before the expanded-item count changed
    /// @param changedBranch      the branch whose state changed
    /// @param expanding          whether descendants were revealed rather than removed
    private void prepareRowMotion(
            IdentityHashMap<TreeItem<T>, Double> precedingPositions,
            TreeItem<T> changedBranch,
            boolean expanding
    ) {
        if (disposed) {
            return;
        }
        M3TreeView<T> control = materialTreeView();
        markVisibleCellsForMotion();

        ArrayList<RowPlacement<T>> placements = new ArrayList<>();
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (!isRenderedCell(cell, item)) {
                continue;
            }

            @Nullable Double precedingPosition = precedingPositions.get(item);
            double initialOffset;
            if (precedingPosition != null) {
                if (expanding) {
                    continue;
                }
                initialOffset = precedingPosition - sceneTop(cell);
            } else if (expanding && isDescendantOf(item, changedBranch)) {
                initialOffset = -Math.min(REVEALED_ROW_OFFSET, cell.getHeight() * 0.25);
            } else {
                continue;
            }

            if (Math.abs(initialOffset) <= POSITION_VISIBILITY_THRESHOLD) {
                continue;
            }
            Translate translation = new Translate(0.0, initialOffset);
            cell.getTransforms().add(0, translation);
            placements.add(new RowPlacement<>(cell, item, translation, initialOffset));
        }

        if (placements.isEmpty()) {
            clearRowMotion();
            return;
        }
        rowPlacements.addAll(placements);
        rowMotionProgress.set(0.0);
        rowMotionAnimation.configure(M3Animation.defaultSpatial(control), 1.0);
        M3Animation.playFromStart(control, rowMotionAnimation);
    }

    /// Applies normalized progress to every private row transform.
    private void updateRowMotionProgress() {
        double remaining = 1.0 - rowMotionProgress.get();
        for (int index = 0; index < rowPlacements.size(); index++) {
            RowPlacement<T> placement = rowPlacements.get(index);
            if (placement.cell().getTreeItem() == placement.item()) {
                placement.translation().setY(placement.initialOffset() * remaining);
            } else {
                placement.translation().setY(0.0);
            }
        }
    }

    /// Stops active motion and removes every transform and temporary style owned by this skin.
    private void clearRowMotion() {
        rowMotionAnimation.stop();
        for (int index = 0; index < rowPlacements.size(); index++) {
            RowPlacement<T> placement = rowPlacements.get(index);
            placement.cell().getTransforms().remove(placement.translation());
        }
        rowPlacements.clear();
        for (int index = 0; index < styledMotionCells.size(); index++) {
            styledMotionCells.get(index).getStyleClass().remove(ROW_MOTION_STYLE_CLASS);
        }
        styledMotionCells.clear();
    }

    /// Marks every currently rendered cell so stale hover and focus surfaces cannot flash during reuse.
    private void markVisibleCellsForMotion() {
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (!isRenderedCell(cell, item) || styledMotionCells.contains(cell)) {
                continue;
            }
            cell.getStyleClass().add(ROW_MOTION_STYLE_CLASS);
            styledMotionCells.add(cell);
        }
    }

    /// Returns the active cells currently owned by the Material virtual flow.
    ///
    /// @return a stable copy of the flow's active-cell list
    private List<TreeCell<T>> materializedCells() {
        @SuppressWarnings("unchecked")
        TreeViewVirtualFlow<T> flow = (TreeViewVirtualFlow<T>) getVirtualFlow();
        return flow.materializedCells();
    }

    /// Returns the Material subtype supplied to this skin's constructor.
    ///
    /// @return the skinned Material tree view
    @SuppressWarnings("unchecked")
    private M3TreeView<T> materialTreeView() {
        return (M3TreeView<T>) getSkinnable();
    }

    /// Returns whether a virtual cell currently represents a visible logical item.
    ///
    /// @param cell the cell to inspect
    /// @param item the cell's current tree item, or `null`
    /// @return `true` when the cell and item can participate in motion
    private static boolean isRenderedCell(TreeCell<?> cell, @Nullable TreeItem<?> item) {
        return item != null && !cell.isEmpty() && cell.isVisible() && cell.getScene() != null;
    }

    /// Returns the scene-coordinate top edge of a rendered cell.
    ///
    /// @param cell the rendered cell to measure
    /// @return the cell's minimum scene y coordinate
    private static double sceneTop(TreeCell<?> cell) {
        Bounds sceneBounds = cell.localToScene(cell.getBoundsInLocal());
        return sceneBounds.getMinY();
    }

    /// Returns whether one item belongs to the changed branch's descendant hierarchy.
    ///
    /// @param item   the item to inspect
    /// @param branch the possible ancestor
    /// @return `true` when `branch` is an ancestor of `item`
    private static boolean isDescendantOf(TreeItem<?> item, TreeItem<?> branch) {
        @Nullable TreeItem<?> current = item.getParent();
        while (current != null) {
            if (current == branch) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /// Associates one reusable cell with the logical item and private transform captured for a motion run.
    ///
    /// @param cell          the virtualized cell
    /// @param item          the logical item represented when the run began
    /// @param translation   the private transform applied by this skin
    /// @param initialOffset the transform's starting vertical offset
    /// @param <T>           the tree-item value type
    @NotNullByDefault
    private record RowPlacement<T>(
            TreeCell<T> cell,
            TreeItem<T> item,
            Translate translation,
            double initialOffset
    ) {
    }

    /// Retains one expanded-item count change until the virtual flow completes its normal layout.
    ///
    /// @param precedingPositions positions captured before the change, keyed by logical-item identity
    /// @param changedBranch      the branch whose expanded state changed
    /// @param expanding          whether the change reveals descendants
    /// @param <T>                the tree-item value type
    @NotNullByDefault
    private record PendingRowMotion<T>(
            IdentityHashMap<TreeItem<T>, Double> precedingPositions,
            TreeItem<T> changedBranch,
            boolean expanding
    ) {
    }

    /// A virtual flow that exposes its protected scrollbars and active cells to the enclosing skin.
    @NotNullByDefault
    private static final class TreeViewVirtualFlow<T> extends VirtualFlow<TreeCell<T>> {
        /// Creates a flow and applies the shared standalone scrollbar style.
        private TreeViewVirtualFlow() {
            M3ScrollPane.style(getHbar());
            M3ScrollPane.style(getVbar());
        }

        /// Returns a stable copy of the cells currently maintained in the flow sheet.
        ///
        /// @return the active materialized cells
        private List<TreeCell<T>> materializedCells() {
            return List.copyOf(getCells());
        }
    }
}
