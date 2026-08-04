// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.FocusModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3TreeCell;
import org.glavo.m3fx.controls.M3TreeView;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusVisibleTracker;
import org.glavo.m3fx.internal.animation.M3DoubleTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;

/// The default virtualized skin for [M3TreeView].
///
/// The skin preserves JavaFX tree navigation and virtualization while applying the same standalone Material
/// scrollbar styling used by [M3ListViewSkin]. Branch changes reveal or remove a clipped subtree in sync with
/// following-row movement, matching expandable navigation drawer groups. Snapshot presentation keeps exiting
/// virtualized rows visible until their clipped region closes without delaying the public expanded state.
/// Following-row motion uses private transforms, so application-owned translation properties remain unchanged.
///
/// @param <T> the tree-item value type
@NotNullByDefault
public final class M3TreeViewSkin<T> extends TreeViewSkin<T> {
    /// The temporary style class applied while virtualized rows are being reassigned for branch motion.
    private static final String ROW_MOTION_STYLE_CLASS = "m3-tree-row-motion";

    /// The scale used to retain sharp text in unavoidable collapse snapshots.
    private static final double SNAPSHOT_RENDER_SCALE = 2.0;

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

    /// Real entering rows whose vector content is revealed by private per-cell clips.
    private final ArrayList<RevealedRow<T>> revealedRows = new ArrayList<>();

    /// The last stable mapping from reusable cells to the logical items whose pixels they render.
    private final IdentityHashMap<TreeCell<T>, TreeItem<T>> renderedCellItems = new IdentityHashMap<>();

    /// The unmanaged overlay that presents clipped subtree snapshots above the virtual flow.
    private final Pane rowMotionOverlay = new Pane();

    /// The snapshot container translated with the subtree reveal progress.
    private final Pane rowMotionContent = new Pane();

    /// The rectangular reveal clip whose height tracks the subtree's visible fraction.
    private final Rectangle rowMotionClip = new Rectangle();

    /// Whether the active row motion reveals rather than removes a subtree.
    private boolean rowMotionExpanding;

    /// The overlay-local top edge of the animated subtree viewport.
    private double rowMotionSubtreeTop;

    /// The fully expanded height represented by the animated subtree viewport.
    private double rowMotionSubtreeHeight;

    /// The latest content-area x coordinate supplied by the inherited skin layout.
    private double contentX;

    /// The latest content-area y coordinate supplied by the inherited skin layout.
    private double contentY;

    /// The latest content-area width supplied by the inherited skin layout.
    private double contentWidth;

    /// The latest content-area height supplied by the inherited skin layout.
    private double contentHeight;

    /// Receives expanded-count events from the current root and all of its descendants.
    private final EventHandler<TreeItem.TreeModificationEvent<T>> expandedItemCountListener =
            this::handleExpandedItemCountChange;

    /// Rebinds the bubbling expanded-count listener when the tree root changes.
    private final ChangeListener<@Nullable TreeItem<T>> rootListener =
            (observable, oldRoot, newRoot) -> updateObservedRoot(oldRoot, newRoot);

    /// Tracks keyboard-visible focus on the composite tree for its logical focused row.
    private final M3FocusVisibleTracker focusVisibleTracker =
            new M3FocusVisibleTracker(getSkinnable(), this::refreshFocusedCellFeedback, null);

    /// Refreshes logical row focus when the tree's focus eligibility changes.
    private final InvalidationListener focusEligibilityListener = observable -> {
        focusVisibleTracker.refresh();
        refreshFocusedCellFeedback();
    };

    /// Refreshes logical row focus when the focused visible index changes.
    private final InvalidationListener focusedIndexListener = observable -> refreshFocusedCellFeedback();

    /// Moves focused-index observation when an application replaces the inherited focus model.
    private final ChangeListener<@Nullable FocusModel<TreeItem<T>>> focusModelListener =
            (observable, oldModel, newModel) -> updateObservedFocusModel(oldModel, newModel);

    /// The branch change waiting for the virtual flow's next normal layout pass, or `null`.
    private @Nullable PendingRowMotion<T> pendingRowMotion;

    /// Whether this skin has released its listeners and must ignore deferred work.
    private boolean disposed;

    /// Creates a Material tree-view skin.
    ///
    /// @param control the skinned Material tree view
    public M3TreeViewSkin(M3TreeView<T> control) {
        super(control);
        rowMotionOverlay.setManaged(false);
        rowMotionOverlay.setMouseTransparent(true);
        rowMotionOverlay.setVisible(false);
        rowMotionOverlay.getStyleClass().add("m3-tree-subtree-motion-overlay");
        rowMotionContent.setManaged(false);
        rowMotionContent.setMouseTransparent(true);
        rowMotionContent.getStyleClass().add("m3-tree-subtree-motion-content");
        rowMotionOverlay.setClip(rowMotionClip);
        rowMotionOverlay.getChildren().add(rowMotionContent);
        getChildren().add(rowMotionOverlay);
        materialFlow().setLayoutCompletion(this::handleVirtualFlowLayoutCompleted);
        rowMotionAnimation.setOnFinished(event -> clearRowMotion());
        control.rootProperty().addListener(rootListener);
        control.focusedProperty().addListener(focusEligibilityListener);
        control.disabledProperty().addListener(focusEligibilityListener);
        control.focusModelProperty().addListener(focusModelListener);
        updateObservedFocusModel(null, control.getFocusModel());
        focusVisibleTracker.install();
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
        renderedCellItems.clear();
        M3TreeView<T> control = materialTreeView();
        control.rootProperty().removeListener(rootListener);
        control.focusedProperty().removeListener(focusEligibilityListener);
        control.disabledProperty().removeListener(focusEligibilityListener);
        control.focusModelProperty().removeListener(focusModelListener);
        updateObservedFocusModel(control.getFocusModel(), null);
        focusVisibleTracker.uninstall();
        updateObservedRoot(control.getRoot(), null);
        rowMotionAnimation.setOnFinished(null);
        clearRowMotion();
        rowMotionOverlay.setClip(null);
        rowMotionOverlay.getChildren().clear();
        getChildren().remove(rowMotionOverlay);
        materialFlow().setLayoutCompletion(null);
        super.dispose();
    }

    /// Refreshes stable row mappings and logical focus after one virtual-flow layout.
    private void handleVirtualFlowLayoutCompleted() {
        updateRenderedCellItemsIfSynchronized();
        refreshFocusedCellFeedback();
    }

    /// Moves focused-index observation between inherited focus-model instances.
    ///
    /// @param oldModel the previously observed focus model, or `null`
    /// @param newModel the focus model to observe, or `null`
    private void updateObservedFocusModel(
            @Nullable FocusModel<TreeItem<T>> oldModel,
            @Nullable FocusModel<TreeItem<T>> newModel
    ) {
        if (oldModel != null) {
            oldModel.focusedIndexProperty().removeListener(focusedIndexListener);
        }
        if (newModel != null && !disposed) {
            newModel.focusedIndexProperty().addListener(focusedIndexListener);
        }
        refreshFocusedCellFeedback();
    }

    /// Delegates composite keyboard focus to the currently focused materialized row.
    private void refreshFocusedCellFeedback() {
        if (disposed) {
            return;
        }

        M3TreeView<T> treeView = materialTreeView();
        @Nullable FocusModel<TreeItem<T>> focusModel = treeView.getFocusModel();
        int focusedIndex = focusModel == null ? -1 : focusModel.getFocusedIndex();
        boolean focusVisible = treeView.isFocused()
                && !treeView.isDisabled()
                && treeView.getPseudoClassStates().contains(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS);
        for (TreeCell<T> cell : materialFlow().materializedCells()) {
            if (cell instanceof M3TreeCell<?> && cell.getSkin() instanceof M3TreeCellSkin<?> materialSkin) {
                boolean cellFocusVisible = focusVisible && !cell.isEmpty() && cell.getIndex() == focusedIndex;
                cell.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, cellFocusVisible);
                materialSkin.setLogicalFocusVisible(cellFocusVisible);
            }
        }
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
        contentX = x;
        contentY = y;
        contentWidth = width;
        contentHeight = height;
        rowMotionOverlay.resizeRelocate(x, y, width, height);
        rowMotionContent.resizeRelocate(0.0, 0.0, width, height);
        rowMotionOverlay.toFront();
        @Nullable PendingRowMotion<T> pending = pendingRowMotion;
        if (pending == null) {
            updateRenderedCellItemsIfSynchronized();
            refreshFocusedCellFeedback();
            updateRowMotionProgress();
            return;
        }
        if (pending.expanding() && !areMaterializedItemsSynchronized()) {
            getVirtualFlow().requestLayout();
            getSkinnable().requestLayout();
            return;
        }
        pendingRowMotion = null;
        prepareRowMotion(
                pending.precedingPositions(),
                pending.changedBranch(),
                pending.expanding(),
                pending.collapsingSnapshots()
        );
        updateRenderedCellItemsIfSynchronized();
    }

    /// Moves expanded-count observation from one root hierarchy to another.
    ///
    /// @param oldRoot the previously observed root, or `null`
    /// @param newRoot the root to observe, or `null`
    private void updateObservedRoot(@Nullable TreeItem<T> oldRoot, @Nullable TreeItem<T> newRoot) {
        if (oldRoot != null) {
            oldRoot.removeEventFilter(TreeItem.<T>expandedItemCountChangeEvent(), expandedItemCountListener);
        }
        if (oldRoot != newRoot) {
            renderedCellItems.clear();
        }
        if (newRoot != null && !disposed) {
            newRoot.addEventFilter(TreeItem.<T>expandedItemCountChangeEvent(), expandedItemCountListener);
        }
    }

    /// Captures the rendered row positions before JavaFX lays out a changed expanded-item count.
    ///
    /// @param event the bubbling branch expansion or collapse event
    private void handleExpandedItemCountChange(TreeItem.TreeModificationEvent<T> event) {
        if (disposed || !event.wasExpanded() && !event.wasCollapsed()) {
            return;
        }

        clearRowMotion();
        IdentityHashMap<TreeItem<T>, Double> precedingPositions = captureRenderedRowPositions();
        markVisibleCellsForMotion();
        TreeItem<T> changedBranch = event.getTreeItem();
        List<RowSnapshot> collapsingSnapshots = event.wasCollapsed()
                ? captureDescendantSnapshots(changedBranch)
                : List.of();
        pendingRowMotion = new PendingRowMotion<>(
                precedingPositions,
                changedBranch,
                event.wasExpanded(),
                collapsingSnapshots
        );
        getSkinnable().requestLayout();
    }

    /// Captures the current scene-coordinate top edge for each materialized logical row.
    ///
    /// @return an identity map from visible tree items to their rendered vertical positions
    private IdentityHashMap<TreeItem<T>, Double> captureRenderedRowPositions() {
        IdentityHashMap<TreeItem<T>, Double> positions = new IdentityHashMap<>();
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = renderedCellItems.get(cell);
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
            boolean expanding,
            List<RowSnapshot> collapsingSnapshots
    ) {
        if (disposed) {
            return;
        }
        M3TreeView<T> control = materialTreeView();
        markVisibleCellsForMotion();

        ArrayList<RowPlacement<T>> placements = new ArrayList<>();
        ArrayList<TreeCell<T>> enteringCells = new ArrayList<>();
        double collapseOffset = 0.0;
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (!isRenderedCell(cell, item)) {
                continue;
            }

            @Nullable Double precedingPosition = precedingPositions.get(item);
            if (precedingPosition != null) {
                double initialOffset = precedingPosition - sceneTop(cell);
                if (Math.abs(initialOffset) <= POSITION_VISIBILITY_THRESHOLD) {
                    continue;
                }
                Translate translation = new Translate(0.0, initialOffset);
                cell.getTransforms().add(0, translation);
                placements.add(new RowPlacement<>(cell, item, translation, initialOffset));
                if (!expanding) {
                    collapseOffset = Math.max(collapseOffset, initialOffset);
                }
            } else if (expanding && isDescendantOf(item, changedBranch)) {
                enteringCells.add(cell);
            }
        }

        if (!expanding && collapseOffset > POSITION_VISIBILITY_THRESHOLD) {
            int branchRow = control.getRow(changedBranch);
            for (TreeCell<T> cell : materializedCells()) {
                @Nullable TreeItem<T> item = cell.getTreeItem();
                if (!isRenderedCell(cell, item)
                        || cell.getIndex() <= branchRow
                        || precedingPositions.containsKey(item)
                        || hasPlacementForCell(placements, cell)) {
                    continue;
                }
                Translate translation = new Translate(0.0, collapseOffset);
                cell.getTransforms().add(0, translation);
                placements.add(new RowPlacement<>(cell, item, translation, collapseOffset));
            }
        }

        List<RowSnapshot> snapshots = expanding ? List.of() : collapsingSnapshots;
        double subtreeHeight = subtreeHeight(placements, enteringCells, snapshots);
        @Nullable Double branchBottom = branchBottom(changedBranch);
        if (subtreeHeight <= POSITION_VISIBILITY_THRESHOLD || branchBottom == null) {
            clearRowMotion();
            return;
        }

        rowPlacements.addAll(placements);
        if (expanding && !installRevealedRows(enteringCells)) {
            clearRowMotion();
            return;
        }
        installRowMotionSnapshots(snapshots);
        rowMotionExpanding = expanding;
        rowMotionSubtreeTop = branchBottom;
        rowMotionSubtreeHeight = subtreeHeight;
        rowMotionOverlay.setVisible(!expanding && !snapshots.isEmpty());
        rowMotionProgress.set(0.0);
        updateRowMotionProgress();
        rowMotionAnimation.configure(M3Animation.defaultSpatial(control), 1.0);
        M3Animation.playFromStart(control, rowMotionAnimation);
    }

    /// Applies normalized progress to every private row transform.
    private void updateRowMotionProgress() {
        double progress = rowMotionProgress.get();
        double remaining = 1.0 - rowMotionProgress.get();
        for (int index = 0; index < rowPlacements.size(); index++) {
            RowPlacement<T> placement = rowPlacements.get(index);
            if (placement.cell().getTreeItem() == placement.item()) {
                placement.translation().setY(placement.initialOffset() * remaining);
            } else {
                placement.translation().setY(0.0);
            }
        }

        double revealBoundary = rowMotionSubtreeTop + rowMotionSubtreeHeight * progress;
        for (int index = 0; index < revealedRows.size(); index++) {
            RevealedRow<T> revealed = revealedRows.get(index);
            if (revealed.cell().getTreeItem() != revealed.item()) {
                revealed.clip().setHeight(0.0);
                continue;
            }
            revealed.clip().setWidth(revealed.cell().getWidth());
            revealed.clip().setHeight(Math.max(0.0, Math.min(
                    revealed.cell().getHeight(),
                    revealBoundary - revealed.baseTop()
            )));
        }

        if (!rowMotionOverlay.isVisible()) {
            return;
        }
        double visibleFraction = rowMotionExpanding ? progress : remaining;
        rowMotionClip.setX(0.0);
        rowMotionClip.setY(rowMotionSubtreeTop);
        rowMotionClip.setWidth(contentWidth);
        rowMotionClip.setHeight(Math.min(
                Math.max(0.0, contentHeight - rowMotionSubtreeTop),
                rowMotionSubtreeHeight * visibleFraction
        ));
        rowMotionContent.setOpacity(1.0);
        rowMotionContent.setTranslateY(0.0);
    }

    /// Stops active motion and removes every transform and temporary style owned by this skin.
    private void clearRowMotion() {
        rowMotionAnimation.stop();
        for (int index = 0; index < rowPlacements.size(); index++) {
            RowPlacement<T> placement = rowPlacements.get(index);
            placement.cell().getTransforms().remove(placement.translation());
        }
        rowPlacements.clear();
        for (int index = 0; index < revealedRows.size(); index++) {
            RevealedRow<T> revealed = revealedRows.get(index);
            if (!revealed.cell().clipProperty().isBound() && revealed.cell().getClip() == revealed.clip()) {
                revealed.cell().setClip(revealed.originalClip());
            }
        }
        revealedRows.clear();
        for (int index = 0; index < styledMotionCells.size(); index++) {
            TreeCell<T> cell = styledMotionCells.get(index);
            cell.getStyleClass().remove(ROW_MOTION_STYLE_CLASS);
            cell.applyCss();
        }
        styledMotionCells.clear();
        rowMotionOverlay.setVisible(false);
        rowMotionContent.getChildren().clear();
        rowMotionContent.setOpacity(1.0);
        rowMotionContent.setTranslateY(0.0);
        rowMotionClip.setHeight(0.0);
        rowMotionSubtreeHeight = 0.0;
    }

    /// Marks every currently rendered cell so stale hover and focus surfaces cannot flash during reuse.
    private void markVisibleCellsForMotion() {
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (!isRenderedCell(cell, item) || styledMotionCells.contains(cell)) {
                continue;
            }
            cell.getStyleClass().add(ROW_MOTION_STYLE_CLASS);
            cell.applyCss();
            styledMotionCells.add(cell);
        }
    }

    /// Captures the currently rendered descendants of a branch before collapse removes their virtual cells.
    ///
    /// @param branch the collapsing branch
    /// @return immutable snapshot descriptors ordered by virtual-flow cell order
    private List<RowSnapshot> captureDescendantSnapshots(TreeItem<T> branch) {
        ArrayList<TreeCell<T>> descendants = new ArrayList<>();
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = renderedCellItems.get(cell);
            if (isRenderedCell(cell, item) && isDescendantOf(item, branch)) {
                descendants.add(cell);
            }
        }
        return captureSnapshots(descendants);
    }

    /// Captures transparent images and scene positions for rendered cells.
    ///
    /// @param cells the cells to capture
    /// @return immutable snapshot descriptors
    private static List<RowSnapshot> captureSnapshots(List<? extends TreeCell<?>> cells) {
        if (cells.isEmpty()) {
            return List.of();
        }
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setTransform(new Scale(SNAPSHOT_RENDER_SCALE, SNAPSHOT_RENDER_SCALE));
        ArrayList<RowSnapshot> snapshots = new ArrayList<>(cells.size());
        for (TreeCell<?> cell : cells) {
            cell.applyCss();
            cell.layout();
            Bounds bounds = cell.localToScene(cell.getBoundsInLocal());
            WritableImage image = cell.snapshot(parameters, null);
            snapshots.add(new RowSnapshot(
                    image,
                    bounds.getMinX(),
                    bounds.getMinY(),
                    bounds.getWidth(),
                    bounds.getHeight()
            ));
        }
        return List.copyOf(snapshots);
    }

    /// Installs private clips that reveal real entering rows without rasterizing or moving their text.
    ///
    /// @param cells the entering rows to reveal
    /// @return `true` when every row supports temporary clipping
    private boolean installRevealedRows(List<TreeCell<T>> cells) {
        M3TreeView<T> control = materialTreeView();
        for (TreeCell<T> cell : cells) {
            if (cell.clipProperty().isBound()) {
                return false;
            }
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (item == null) {
                continue;
            }
            Bounds bounds = cell.localToScene(cell.getBoundsInLocal());
            Point2D local = control.sceneToLocal(bounds.getMinX(), bounds.getMinY());
            Rectangle clip = new Rectangle(cell.getWidth(), 0.0);
            @Nullable Node originalClip = cell.getClip();
            cell.setClip(clip);
            revealedRows.add(new RevealedRow<>(
                    cell,
                    item,
                    clip,
                    originalClip,
                    local.getY() - contentY
            ));
        }
        return !revealedRows.isEmpty();
    }

    /// Mounts row snapshot images at their current tree-local positions.
    ///
    /// @param snapshots the row snapshot descriptors to present
    private void installRowMotionSnapshots(List<RowSnapshot> snapshots) {
        M3TreeView<T> control = materialTreeView();
        ArrayList<ImageView> views = new ArrayList<>(snapshots.size());
        for (RowSnapshot snapshot : snapshots) {
            Point2D local = control.sceneToLocal(snapshot.sceneX(), snapshot.sceneY());
            ImageView view = new ImageView(snapshot.image());
            view.setManaged(false);
            view.setMouseTransparent(true);
            view.setFitWidth(snapshot.width());
            view.setFitHeight(snapshot.height());
            view.setPreserveRatio(false);
            view.setSmooth(true);
            view.relocate(local.getX() - contentX, local.getY() - contentY);
            views.add(view);
        }
        rowMotionContent.getChildren().setAll(views);
    }

    /// Returns the expanded subtree height represented by row deltas or visible snapshots.
    ///
    /// @param placements    following-row placements that encode the full structural delta
    /// @param enteringCells real entering rows used when no following row is materialized
    /// @param snapshots     visible subtree snapshots used when no following row is materialized
    /// @return a non-negative subtree height in logical pixels
    private static double subtreeHeight(
            List<? extends RowPlacement<?>> placements,
            List<? extends TreeCell<?>> enteringCells,
            List<RowSnapshot> snapshots
    ) {
        double height = 0.0;
        for (RowPlacement<?> placement : placements) {
            height = Math.max(height, Math.abs(placement.initialOffset()));
        }
        if (height > POSITION_VISIBILITY_THRESHOLD) {
            return height;
        }

        if (!enteringCells.isEmpty()) {
            double minimumY = Double.POSITIVE_INFINITY;
            double maximumY = Double.NEGATIVE_INFINITY;
            for (TreeCell<?> cell : enteringCells) {
                Bounds bounds = cell.localToScene(cell.getBoundsInLocal());
                minimumY = Math.min(minimumY, bounds.getMinY());
                maximumY = Math.max(maximumY, bounds.getMaxY());
            }
            height = Math.max(0.0, maximumY - minimumY);
            if (height > POSITION_VISIBILITY_THRESHOLD) {
                return height;
            }
        }

        if (snapshots.isEmpty()) {
            return 0.0;
        }
        double minimumY = Double.POSITIVE_INFINITY;
        double maximumY = Double.NEGATIVE_INFINITY;
        for (RowSnapshot snapshot : snapshots) {
            minimumY = Math.min(minimumY, snapshot.sceneY());
            maximumY = Math.max(maximumY, snapshot.sceneY() + snapshot.height());
        }
        return Math.max(0.0, maximumY - minimumY);
    }

    /// Returns the changed branch row's bottom edge in overlay-local coordinates.
    ///
    /// @param branch the changed branch
    /// @return the local bottom edge, or `null` when the branch row is outside the viewport
    private @Nullable Double branchBottom(TreeItem<T> branch) {
        M3TreeView<T> control = materialTreeView();
        for (TreeCell<T> cell : materializedCells()) {
            if (cell.getTreeItem() != branch || !isRenderedCell(cell, branch)) {
                continue;
            }
            Bounds bounds = cell.localToScene(cell.getBoundsInLocal());
            Point2D local = control.sceneToLocal(bounds.getMinX(), bounds.getMaxY());
            return local.getY() - contentY;
        }
        return null;
    }

    /// Returns the active cells currently owned by the Material virtual flow.
    ///
    /// @return a stable copy of the flow's active-cell list
    private List<TreeCell<T>> materializedCells() {
        return materialFlow().materializedCells();
    }

    /// Returns the Material virtual-flow subtype created by this skin.
    ///
    /// @return the Material tree virtual flow
    private TreeViewVirtualFlow<T> materialFlow() {
        @SuppressWarnings("unchecked")
        TreeViewVirtualFlow<T> flow = (TreeViewVirtualFlow<T>) getVirtualFlow();
        return flow;
    }

    /// Returns whether every rendered cell's value has caught up with its logical tree item after flow reuse.
    ///
    /// JavaFX may expose the new [TreeCell#getTreeItem()] before invoking the cell's item update in the same pulse.
    /// Snapshot-based expansion waits for both channels to agree so old row pixels cannot enter the overlay.
    ///
    /// @return `true` when materialized cell values represent their current logical items
    private boolean areMaterializedItemsSynchronized() {
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (isRenderedCell(cell, item) && !Objects.equals(cell.getItem(), item.getValue())) {
                return false;
            }
        }
        return true;
    }

    /// Refreshes the stable rendered-item cache only after cell values match their current logical assignments.
    private void updateRenderedCellItemsIfSynchronized() {
        if (!areMaterializedItemsSynchronized()) {
            return;
        }
        renderedCellItems.clear();
        for (TreeCell<T> cell : materializedCells()) {
            @Nullable TreeItem<T> item = cell.getTreeItem();
            if (isRenderedCell(cell, item)) {
                renderedCellItems.put(cell, item);
            }
        }
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

    /// Returns whether a materialized cell already has a private placement for the current motion run.
    ///
    /// @param placements the placements prepared so far
    /// @param cell       the cell to inspect
    /// @return `true` when the cell already participates in following-row motion
    private static boolean hasPlacementForCell(
            List<? extends RowPlacement<?>> placements,
            TreeCell<?> cell
    ) {
        for (RowPlacement<?> placement : placements) {
            if (placement.cell() == cell) {
                return true;
            }
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

    /// Associates a real entering row with the private reveal geometry owned by this skin.
    ///
    /// @param cell         the real virtualized row
    /// @param item         the logical item represented when reveal began
    /// @param clip         the private per-row reveal clip
    /// @param originalClip the application clip to restore, or `null`
    /// @param baseTop      the row's untransformed overlay-local top edge
    /// @param <T>          the tree-item value type
    @NotNullByDefault
    private record RevealedRow<T>(
            TreeCell<T> cell,
            TreeItem<T> item,
            Rectangle clip,
            @Nullable Node originalClip,
            double baseTop
    ) {
    }

    /// Retains one expanded-item count change until the virtual flow completes its normal layout.
    ///
    /// @param precedingPositions positions captured before the change, keyed by logical-item identity
    /// @param changedBranch      the branch whose expanded state changed
    /// @param expanding          whether the change reveals descendants
    /// @param collapsingSnapshots snapshots of descendants removed by a collapse
    /// @param <T>                the tree-item value type
    @NotNullByDefault
    private record PendingRowMotion<T>(
            IdentityHashMap<TreeItem<T>, Double> precedingPositions,
            TreeItem<T> changedBranch,
            boolean expanding,
            List<RowSnapshot> collapsingSnapshots
    ) {
    }

    /// Stores one transparent row image and its scene-coordinate origin.
    ///
    /// @param image   the captured row pixels
    /// @param sceneX  the snapshot's scene-coordinate x origin
    /// @param sceneY  the snapshot's scene-coordinate y origin
    /// @param width   the logical row width
    /// @param height  the logical row height
    @NotNullByDefault
    private record RowSnapshot(
            WritableImage image,
            double sceneX,
            double sceneY,
            double width,
            double height
    ) {
    }

    /// A virtual flow that exposes its protected scrollbars and active cells to the enclosing skin.
    @NotNullByDefault
    private static final class TreeViewVirtualFlow<T> extends VirtualFlow<TreeCell<T>> {
        /// The callback invoked after active cells finish a virtual-flow layout, or `null`.
        private @Nullable Runnable layoutCompletion;

        /// Creates a flow and applies the shared standalone scrollbar style.
        private TreeViewVirtualFlow() {
            M3ScrollPane.style(getHbar());
            M3ScrollPane.style(getVbar());
        }

        /// Runs the inherited virtual layout and then reports stable cell assignments to the enclosing skin.
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            @Nullable Runnable completion = layoutCompletion;
            if (completion != null) {
                completion.run();
            }
        }

        /// Replaces the callback invoked after virtual-flow layout.
        ///
        /// @param completion the callback, or `null` during disposal
        private void setLayoutCompletion(@Nullable Runnable completion) {
            layoutCompletion = completion;
        }

        /// Returns a stable copy of the cells currently maintained in the flow sheet.
        ///
        /// @return the active materialized cells
        private List<TreeCell<T>> materializedCells() {
            return List.copyOf(getCells());
        }
    }
}
