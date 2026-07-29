// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3DoubleAnimatable;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3ListCell;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3ScrollToEvent;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FocusVisibleTracker;
import org.glavo.m3fx.internal.M3ListViewPresentation;
import org.glavo.m3fx.internal.M3ScrollReveal;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/// The default virtualized skin for [M3ListView].
///
/// The skin materializes only the rows needed by the viewport, styles the virtual flow scroll bars, and coordinates
/// scrolling, logical row focus, and accessibility focus with the control's focused index. Materialized rows remain
/// descendants of the list view and inherit its nearest theme scope through the normal JavaFX CSS cascade.
///
/// @param <T> the item type rendered by the skinned list view
@NotNullByDefault
public final class M3ListViewSkin<T> extends SkinBase<M3ListView<T>>
        implements M3ListViewPresentation {
    /// The fallback row height used before the virtual flow has measured a visible cell.
    private static final double DEFAULT_ROW_HEIGHT = 56.0;

    /// The default wheel line distance used when a platform reports text-line scroll units.
    private static final double DEFAULT_LINE_SCROLL_PIXELS = 40.0;

    /// The minimum meaningful scroll value difference.
    private static final double EPSILON = 0.000001;

    /// Sentinel used when no attached focused row has been reported to accessibility clients.
    private static final int NO_MATERIALIZED_ACCESSIBLE_FOCUS = -2;

    /// The virtualized cell container.
    private final ListViewVirtualFlow<T> flow = new ListViewVirtualFlow<>();

    /// The independent normalized position used while a virtual-flow scroll is animated.
    private final DoubleProperty animatedScrollPosition =
            new SimpleDoubleProperty(this, "animatedScrollPosition");

    /// Animates the virtual flow's normalized scroll position.
    private final M3DoubleAnimatable smoothScrollAnimation =
            new M3DoubleAnimatable(getSkinnable(), animatedScrollPosition, EPSILON);

    /// The animated normalized position already transferred to the virtual flow.
    private double appliedAnimatedScrollPosition;

    /// Whether the active animation should commit its accumulated target when it stops naturally.
    private boolean smoothScrollRunActive;

    /// Transfers each animated position delta to the virtual flow's authoritative pixel offset.
    private final InvalidationListener animatedScrollPositionInvalidation = observable -> {
        double nextPosition = animatedScrollPosition.get();
        double delta = (nextPosition - appliedAnimatedScrollPosition) * estimatedScrollablePixels();
        appliedAnimatedScrollPosition = nextPosition;
        if (Math.abs(delta) > EPSILON && isSceneRefreshable()) {
            flow.scrollPixels(delta);
        }
        if (smoothScrollRunActive
                && smoothScrollAnimation.getStatus() == Animation.Status.STOPPED
                && close(nextPosition, smoothScrollAnimation.getTargetValue())) {
            finishSmoothScrollAnimation();
        }
    };

    /// Updates virtual flow cell count when data items change.
    private final ListChangeListener<T> itemsListener = change -> refreshItemCount();

    /// Rebuilds visible cells when the cell factory changes.
    private final InvalidationListener cellFactoryInvalidation = observable -> rebuildCells();

    /// Reconfigures cell measurement after item height or spacing changes.
    private final InvalidationListener cellMetricsInvalidation = observable -> flow.refreshCellMetrics();

    /// Tracks keyboard-visible focus for virtualized row focus indicators.
    ///
    /// Virtual rows use logical list focus rather than becoming the JavaFX scene focus owner, so this deliberately
    /// uses scene-modality tracking instead of the list view's native focus-visible property.
    private final M3FocusVisibleTracker focusVisibleTracker =
            new M3FocusVisibleTracker(getSkinnable(), this::refreshCells, null);

    /// Updates logical row focus when focus or disabled eligibility changes.
    private final InvalidationListener focusEligibilityInvalidation = observable -> {
        focusVisibleTracker.refresh();
        refreshCells();
    };

    /// Suspends virtualized cell refreshes while the list view is detached and refreshes after reattachment.
    private final ChangeListener<@Nullable Scene> sceneListener =
            (observable, oldScene, newScene) -> handleSceneChanged(newScene);

    /// Handles wheel and trackpad scrolling through Material motion.
    private final EventHandler<ScrollEvent> smoothScrollHandler = this::handleSmoothScroll;

    /// Handles semantic indexed scrolling requests from the skinnable control.
    private final EventHandler<M3ScrollToEvent> scrollToRequestHandler = event -> {
        scrollToIndex(event.getIndex(), event.isAnimated());
        event.consume();
    };

    /// The completion callback attached to the currently running smooth scroll animation.
    private @Nullable Runnable smoothScrollOnFinished;

    /// Completes deferred scroll work after the position animation reaches its target.
    private final ChangeListener<Animation.Status> smoothScrollStatusListener = (observable, oldStatus, newStatus) -> {
        if (newStatus == Animation.Status.STOPPED
                && smoothScrollRunActive
                && close(smoothScrollAnimation.getValue(), smoothScrollAnimation.getTargetValue())) {
            finishSmoothScrollAnimation();
        }
    };

    /// The accumulated target virtual flow position.
    private double smoothScrollTargetPosition;

    /// Whether a focused cell should refresh logical row focus after the next layout pass.
    private boolean focusRequestPending;

    /// Whether a deferred focus retry has already been queued for the next pulse.
    private boolean focusRetryScheduled;

    /// The focused row index whose attached item has been reported as the accessibility focus node.
    private int materializedAccessibleFocusIndex = NO_MATERIALIZED_ACCESSIBLE_FOCUS;

    /// Creates a virtualized list view skin.
    ///
    /// @param control the skinned virtualized list view
    public M3ListViewSkin(M3ListView<T> control) {
        super(control);
        flow.getStyleClass().add("m3-list-view-flow");
        flow.setVertical(true);
        flow.setPannable(true);
        flow.setCellFactory(flow -> createCell(control));
        flow.styleScrollBars();
        animatedScrollPosition.addListener(animatedScrollPositionInvalidation);
        smoothScrollAnimation.statusProperty().addListener(smoothScrollStatusListener);
        flow.fixedCellSizeProperty().bind(Bindings.createDoubleBinding(
                () -> control.getItemSpacing() > 0.0 ? 0.0 : control.getFixedCellSize(),
                control.fixedCellSizeProperty(),
                control.itemSpacingProperty()
        ));
        control.addEventFilter(ScrollEvent.SCROLL, smoothScrollHandler);
        control.addEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, scrollToRequestHandler);
        getChildren().setAll(flow);

        control.getItems().addListener(itemsListener);
        control.cellFactoryProperty().addListener(cellFactoryInvalidation);
        control.itemSpacingProperty().addListener(cellMetricsInvalidation);
        control.fixedCellSizeProperty().addListener(cellMetricsInvalidation);
        control.focusedProperty().addListener(focusEligibilityInvalidation);
        control.disabledProperty().addListener(focusEligibilityInvalidation);
        focusVisibleTracker.install();
        control.sceneProperty().addListener(sceneListener);
        refreshItemCount();
    }

    /// Stops bindings and removes listeners before disposal.
    @Override
    public void dispose() {
        M3ListView<T> listView = getSkinnable();
        stopSmoothScrollAnimation();
        smoothScrollAnimation.statusProperty().removeListener(smoothScrollStatusListener);
        animatedScrollPosition.removeListener(animatedScrollPositionInvalidation);
        listView.removeEventFilter(ScrollEvent.SCROLL, smoothScrollHandler);
        listView.removeEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, scrollToRequestHandler);
        listView.getItems().removeListener(itemsListener);
        listView.cellFactoryProperty().removeListener(cellFactoryInvalidation);
        listView.itemSpacingProperty().removeListener(cellMetricsInvalidation);
        listView.fixedCellSizeProperty().removeListener(cellMetricsInvalidation);
        listView.focusedProperty().removeListener(focusEligibilityInvalidation);
        listView.disabledProperty().removeListener(focusEligibilityInvalidation);
        focusVisibleTracker.uninstall();
        listView.sceneProperty().removeListener(sceneListener);
        flow.fixedCellSizeProperty().unbind();
        flow.releaseCells();
        flow.setCellFactory(null);
        getChildren().remove(flow);
        super.dispose();
    }

    /// Lays out the virtual flow in the available bounds.
    ///
    /// @param x      the layout area's x coordinate
    /// @param y      the layout area's y coordinate
    /// @param width  the layout area's width
    /// @param height the layout area's height
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        flow.resizeRelocate(x, y, width, height);
        if (focusRequestPending) {
            if (!isSceneRefreshable()) {
                focusRequestPending = false;
                return;
            }
            flow.applyCss();
            flow.layout();
        }
        focusVisibleCellIfNeeded();
        notifyAccessibleFocusNodeMaterialized();
        scheduleFocusRetry();
    }

    /// Computes the preferred width from the virtual flow.
    ///
    /// @param height      the height that should be used if width depends on it
    /// @param topInset    the snapped top inset
    /// @param rightInset  the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset   the snapped left inset
    /// @return the preferred width
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + Math.max(240.0, flow.prefWidth(height)) + rightInset;
    }

    /// Computes the preferred height from visible row metrics.
    ///
    /// @param width       the width that should be used if height depends on it
    /// @param topInset    the snapped top inset
    /// @param rightInset  the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset   the snapped left inset
    /// @return the preferred height
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double fixedCellSize = getSkinnable().getFixedCellSize();
        int visibleRows = Math.min(8, Math.max(1, getSkinnable().getItems().size()));
        double rowHeight = fixedCellSize > 0.0 ? fixedCellSize : 56.0;
        double spacing = getSkinnable().getItemSpacing() * Math.max(0, visibleRows - 1);
        return topInset + rowHeight * visibleRows + spacing + bottomInset;
    }

    /// Updates the number of cells owned by the virtual flow.
    private void refreshItemCount() {
        flow.setCellCount(getSkinnable().getItems().size());
        if (isSceneRefreshable()) {
            flow.requestLayout();
        }
    }

    /// Requests visible cell state and layout updates.
    @Override
    public void refreshCells() {
        if (!isSceneRefreshable()) {
            return;
        }
        flow.refreshCells();
    }

    /// Requests visible cell focus updates, optionally animating the scroll into view.
    ///
    /// @param requestNodeFocus whether the focused cell should request keyboard focus
    /// @param animated         whether scrolling the focused cell into view should animate
    @Override
    public void refreshFocus(boolean requestNodeFocus, boolean animated) {
        focusRequestPending |= requestNodeFocus;
        if (!isSceneRefreshable()) {
            return;
        }
        flow.refreshCells();
        int focusedIndex = getSkinnable().getFocusedIndex();
        if (focusedIndex >= 0) {
            if (requestNodeFocus || attachedVisibleItem(focusedIndex) == null) {
                scrollToIndex(focusedIndex, animated);
            }
        } else {
            stopSmoothScrollAnimation();
        }
        getSkinnable().requestLayout();
        focusVisibleCellIfNeeded();
        notifyAccessibleFocusNodeMaterialized();
        scheduleFocusRetry();
    }

    /// Recreates visible cells after the cell factory changes.
    private void rebuildCells() {
        if (!isSceneRefreshable()) {
            return;
        }
        flow.rebuildAllCells();
    }

    /// Creates and validates one reusable virtualized cell through the control factory.
    private static <T> M3ListCell<T> createCell(M3ListView<T> listView) {
        M3ListCell<T> cell = Objects.requireNonNull(
                listView.getCellFactory().call(listView),
                "cellFactory result"
        );
        if (cell.getListView() != listView) {
            throw new IllegalArgumentException("cellFactory must create a cell for the supplied list view");
        }
        return cell;
    }

    /// Updates virtualized row state after the list view enters or leaves a scene.
    private void handleSceneChanged(@Nullable Scene scene) {
        if (scene == null) {
            focusRequestPending = false;
            focusRetryScheduled = false;
            stopSmoothScrollAnimation();
            return;
        }

        flow.refreshCells();
        getSkinnable().requestLayout();
    }

    /// Returns whether the virtual flow may safely refresh cells for the current scene lifecycle state.
    private boolean isSceneRefreshable() {
        @Nullable Scene scene = getSkinnable().getScene();
        if (scene == null) {
            return false;
        }

        @Nullable Window window = scene.getWindow();
        return window == null || window.isShowing();
    }

    /// Returns the rendered list item for a visible or reusable cell index.
    ///
    /// @param index the data item index to query
    /// @return the rendered list item node, or `null` when the index is outside the data list
    @Override
    public @Nullable Node visibleItem(int index) {
        if (index < 0 || index >= getSkinnable().getItems().size()) {
            return null;
        }

        M3ListCell<T> cell = flow.visibleOrReusableCell(index);
        return cell.getListItem();
    }

    /// Returns the rendered list item when the requested index is currently owned by the virtual flow.
    ///
    /// @param index the data item index to query
    /// @return the rendered list item node, or `null` when the row is not currently materialized
    @Override
    public @Nullable Node attachedVisibleItem(int index) {
        if (index < 0 || index >= getSkinnable().getItems().size()) {
            return null;
        }

        @Nullable M3ListCell<T> cell = flow.findVisibleCell(index);
        return cell == null ? null : cell.getListItem();
    }

    /// Returns the data index of the attached visible item that contains the supplied node.
    ///
    /// @param node the node to find in the attached visible rows
    /// @return the containing row index, or `-1` when the node is not in an attached row
    @Override
    public int attachedVisibleItemIndex(Node node) {
        return flow.attachedVisibleItemIndex(node);
    }

    /// Returns the first attached visible list item accepted by the supplied predicate.
    ///
    /// @param predicate the predicate used to select one attached visible list item
    /// @return the matching attached visible list item, or `null` when none matches
    @Override
    public @Nullable Node findAttachedVisibleItem(Predicate<? super Node> predicate) {
        return flow.findAttachedVisibleItem(predicate);
    }

    /// Refreshes the visible cell that owns the focused index when it has been materialized.
    private void focusVisibleCellIfNeeded() {
        if (!focusRequestPending) {
            return;
        }

        int index = getSkinnable().getFocusedIndex();
        if (index < 0) {
            M3ScrollReveal.requestFocusAndReveal(getSkinnable(), getSkinnable());
            focusRequestPending = false;
            return;
        }

        @Nullable M3ListCell<T> cell = flow.findVisibleCell(index);
        if (cell != null && cell.focusCell()) {
            focusRequestPending = false;
        }
    }

    /// Queues a next-pulse focus retry when `VirtualFlow` has not materialized the requested row yet.
    private void scheduleFocusRetry() {
        if (!focusRequestPending || focusRetryScheduled) {
            return;
        }

        focusRetryScheduled = true;
        Platform.runLater(() -> {
            focusRetryScheduled = false;
            if (!focusRequestPending) {
                return;
            }
            if (!isSceneRefreshable()) {
                focusRequestPending = false;
                return;
            }

            getSkinnable().applyCss();
            flow.applyCss();
            flow.layout();
            focusVisibleCellIfNeeded();
            notifyAccessibleFocusNodeMaterialized();
            if (focusRequestPending) {
                getSkinnable().requestLayout();
            }
        });
    }

    /// Handles one indirect wheel or trackpad scroll event.
    private void handleSmoothScroll(ScrollEvent event) {
        if (event.isDirect() || !isEventForThisFlow(event)) {
            return;
        }

        double scrollablePixels = estimatedScrollablePixels();
        if (scrollablePixels <= EPSILON) {
            return;
        }

        if (!smoothScrollAnimation.isRunning()) {
            smoothScrollTargetPosition = flow.getPosition();
        }

        double delta = scrollDeltaY(event, flow.getHeight());
        if (Math.abs(delta) <= EPSILON) {
            return;
        }

        double nextPosition = clamp(smoothScrollTargetPosition - delta / scrollablePixels);
        if (close(nextPosition, smoothScrollTargetPosition)) {
            return;
        }

        smoothScrollTargetPosition = nextPosition;
        if (animationsEnabled()) {
            animateSmoothScroll(null);
        } else {
            stopSmoothScrollAnimation();
            flow.setPosition(smoothScrollTargetPosition);
            flow.requestLayout();
        }
        event.consume();
    }

    /// Returns whether a scroll event belongs directly to this virtual flow rather than a nested scroll container.
    private boolean isEventForThisFlow(ScrollEvent event) {
        EventTarget target = event.getTarget();
        if (!(target instanceof Node node)) {
            return true;
        }
        if (node == getSkinnable()) {
            return true;
        }

        @Nullable Node current = node;
        while (current != null && current != flow) {
            if (current instanceof ScrollPane || current instanceof VirtualFlow<?>) {
                return false;
            }
            current = current.getParent();
        }
        return current == flow;
    }

    /// Scrolls the virtual flow to an item index.
    private void scrollToIndex(int index, boolean animated) {
        if (index < 0 || index >= getSkinnable().getItems().size()) {
            stopSmoothScrollAnimation();
            return;
        }

        double targetPosition = scrollPositionForIndex(index);
        if (!animated
                || !isSceneRefreshable()
                || !animationsEnabled()) {
            stopSmoothScrollAnimation();
            if (Double.isNaN(targetPosition)) {
                flow.scrollTo(index);
            } else {
                flow.setPosition(targetPosition);
            }
            flow.requestLayout();
            layoutFlowAfterImmediateScroll();
            return;
        }

        if (Double.isNaN(targetPosition)) {
            stopSmoothScrollAnimation();
            flow.scrollTo(index);
            flow.requestLayout();
            layoutFlowAfterImmediateScroll();
            return;
        }

        if (close(flow.getPosition(), targetPosition)) {
            flow.setPosition(targetPosition);
            flow.requestLayout();
            layoutFlowAfterImmediateScroll();
            return;
        }

        smoothScrollTargetPosition = targetPosition;
        animateSmoothScroll(() -> finishAnimatedIndexScroll(index));
    }

    /// Finishes focus updates after an animated index scroll reaches its target.
    private void finishAnimatedIndexScroll(int index) {
        flow.requestLayout();
        if (getSkinnable().getFocusedIndex() == index) {
            focusVisibleCellIfNeeded();
            notifyAccessibleFocusNodeMaterialized();
            scheduleFocusRetry();
        }
    }

    /// Materializes target cells immediately after non-animated programmatic scrolls.
    private void layoutFlowAfterImmediateScroll() {
        if (!isSceneRefreshable()) {
            return;
        }

        flow.applyCss();
        flow.layout();
    }

    /// Notifies accessibility clients when the focused virtual row becomes an attached scene node.
    private void notifyAccessibleFocusNodeMaterialized() {
        int focusedIndex = getSkinnable().getFocusedIndex();
        if (focusedIndex < 0) {
            materializedAccessibleFocusIndex = NO_MATERIALIZED_ACCESSIBLE_FOCUS;
            return;
        }

        if (attachedVisibleItem(focusedIndex) == null) {
            materializedAccessibleFocusIndex = NO_MATERIALIZED_ACCESSIBLE_FOCUS;
            return;
        }

        if (materializedAccessibleFocusIndex == focusedIndex) {
            return;
        }

        materializedAccessibleFocusIndex = focusedIndex;
        notifyAccessibleFocusNodeChanged();
    }

    /// Notifies the list view and its ancestors that the exposed accessibility focus node changed.
    private void notifyAccessibleFocusNodeChanged() {
        Node node = getSkinnable();
        node.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            parent.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            parent = parent.getParent();
        }
    }

    /// Animates the virtual flow to the accumulated target position.
    private void animateSmoothScroll(@Nullable Runnable onFinished) {
        smoothScrollOnFinished = null;
        if (!smoothScrollAnimation.isRunning()) {
            appliedAnimatedScrollPosition = flow.getPosition();
            smoothScrollAnimation.snapTo(appliedAnimatedScrollPosition);
        }
        smoothScrollRunActive = false;
        smoothScrollAnimation.animateTo(smoothScrollTargetPosition, M3Animation.defaultSpatial(getSkinnable()));
        smoothScrollRunActive = smoothScrollAnimation.isRunning();
        smoothScrollOnFinished = onFinished;
        if (!smoothScrollRunActive) {
            finishSmoothScrollAnimation();
        }
    }

    /// Returns the current inherited animation setting.
    private boolean animationsEnabled() {
        return M3Animation.areAnimationsEnabled(getSkinnable());
    }

    /// Runs the current smooth-scroll completion callback after the reusable transition finishes.
    private void finishSmoothScrollAnimation() {
        smoothScrollRunActive = false;
        @Nullable Runnable onFinished = smoothScrollOnFinished;
        smoothScrollOnFinished = null;
        flow.setPosition(smoothScrollTargetPosition);
        if (isSceneRefreshable()) {
            flow.requestLayout();
        }
        if (onFinished != null) {
            onFinished.run();
        }
    }

    /// Stops the running smooth scroll animation.
    private void stopSmoothScrollAnimation() {
        smoothScrollRunActive = false;
        smoothScrollOnFinished = null;
        smoothScrollAnimation.stop();
    }

    /// Returns an estimated scrollable content height in pixels.
    private double estimatedScrollablePixels() {
        int itemCount = getSkinnable().getItems().size();
        if (itemCount == 0) {
            return 0.0;
        }

        double rowHeight = estimatedRowHeight();
        double contentHeight = rowHeight * itemCount;
        double viewportHeight = flow.getHeight();
        if (viewportHeight <= 0.0) {
            viewportHeight = getSkinnable().getHeight();
        }
        return Math.max(0.0, contentHeight - viewportHeight);
    }

    /// Returns the best available estimate for one row height.
    private double estimatedRowHeight() {
        double fixedCellSize = getSkinnable().getFixedCellSize();
        if (fixedCellSize > 0.0) {
            return fixedCellSize;
        }

        double averageVisibleCellHeight = flow.averageVisibleCellHeight();
        if (averageVisibleCellHeight > 0.0) {
            return averageVisibleCellHeight;
        }
        return DEFAULT_ROW_HEIGHT;
    }

    /// Returns the estimated normalized virtual flow position that reveals the item at an index.
    private double scrollPositionForIndex(int index) {
        double scrollablePixels = estimatedScrollablePixels();
        if (scrollablePixels <= EPSILON) {
            return 0.0;
        }

        double rowHeight = estimatedRowHeight();
        double viewportHeight = flow.getHeight();
        if (viewportHeight <= 0.0) {
            viewportHeight = getSkinnable().getHeight();
        }
        if (viewportHeight <= 0.0) {
            return Double.NaN;
        }

        double rowStart = rowHeight * index;
        double centerOffset = Math.max(0.0, (viewportHeight - rowHeight) / 2.0);
        double targetPixels = Math.min(Math.max(0.0, rowStart - centerOffset), scrollablePixels);
        return clamp(targetPixels / scrollablePixels);
    }

    /// Converts an event's vertical scroll amount to pixels.
    private static double scrollDeltaY(ScrollEvent event, double viewportHeight) {
        return switch (event.getTextDeltaYUnits()) {
            case LINES -> event.getTextDeltaY() * DEFAULT_LINE_SCROLL_PIXELS;
            case PAGES -> event.getTextDeltaY() * viewportHeight;
            case NONE -> event.getDeltaY();
        };
    }

    /// Returns a value clamped to a virtual flow position.
    private static double clamp(double value) {
        if (value <= 0.0) {
            return 0.0;
        }
        return Math.min(value, 1.0);
    }

    /// Returns whether two scroll values are effectively equal.
    private static boolean close(double first, double second) {
        return Math.abs(first - second) <= EPSILON;
    }

    /// Returns whether one attached node contains another attached node.
    private static boolean containsNode(Node possibleAncestor, Node possibleDescendant) {
        if (possibleAncestor == possibleDescendant) {
            return true;
        }
        if (possibleAncestor instanceof M3ListItem listItem
                && (containsOptionalNode(listItem.getLeading(), possibleDescendant)
                || containsOptionalNode(listItem.getTrailing(), possibleDescendant))) {
            return true;
        }
        if (possibleAncestor instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsNode(child, possibleDescendant)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether one optional logical slot node contains another attached node.
    private static boolean containsOptionalNode(@Nullable Node possibleAncestor, Node possibleDescendant) {
        return possibleAncestor != null && containsNode(possibleAncestor, possibleDescendant);
    }

    /// A private wrapper exposing protected virtual flow hooks to this skin.
    @NotNullByDefault
    private static final class ListViewVirtualFlow<T> extends VirtualFlow<M3ListCell<T>> {
        /// Applies standalone Material styling to both virtual-flow scroll bars.
        private void styleScrollBars() {
            M3ScrollPanes.style(getHbar());
            M3ScrollPanes.style(getVbar());
        }

        /// Invalidates cached variable cell lengths after item stride metrics change.
        private void refreshCellMetrics() {
            reconfigureCells();
            requestLayout();
        }

        /// Requests visible cell relayout and selection refresh.
        private void refreshCells() {
            requestCellLayout();
            requestLayout();
            for (M3ListCell<T> cell : getCells()) {
                cell.refreshSelection();
                cell.refreshFocus();
            }
        }

        /// Returns a current virtual flow cell for the requested index.
        private @Nullable M3ListCell<T> findVisibleCell(int index) {
            for (M3ListCell<T> cell : getCells()) {
                if (!cell.isEmpty() && cell.getIndex() == index && cell.getListItem() != null) {
                    return cell;
                }
            }
            return null;
        }

        /// Returns a visible cell, asking the virtual flow for a reusable cell when needed.
        @SuppressWarnings("ConstantValue")
        private M3ListCell<T> visibleOrReusableCell(int index) {
            M3ListCell<T> cell = getVisibleCell(index);
            if (cell == null) {
                return getCell(index);
            }
            return cell;
        }

        /// Rebuilds the virtual flow cell pile from the current cell factory.
        private void rebuildAllCells() {
            releaseCells();
            recreateCells();
            requestLayout();
        }

        /// Releases application-owned row nodes before cells are rebuilt or the skin is disposed.
        private void releaseCells() {
            for (M3ListCell<T> cell : getCells()) {
                cell.updateIndex(-1);
            }
        }

        /// Returns the average height of attached data cells, or zero before cells are measured.
        private double averageVisibleCellHeight() {
            double totalHeight = 0.0;
            int measuredCellCount = 0;
            for (M3ListCell<T> cell : getCells()) {
                double cellHeight = cell.getHeight();
                if (!cell.isEmpty() && Double.isFinite(cellHeight) && cellHeight > 0.0) {
                    totalHeight += cellHeight;
                    measuredCellCount++;
                }
            }
            return measuredCellCount == 0 ? 0.0 : totalHeight / measuredCellCount;
        }

        /// Returns the index for the attached visible row that contains the supplied node.
        private int attachedVisibleItemIndex(Node node) {
            for (M3ListCell<T> cell : getCells()) {
                if (cell.isEmpty() || cell.getScene() == null) {
                    continue;
                }
                @Nullable Node listItem = cell.getListItem();
                if (listItem != null && containsNode(listItem, node)) {
                    return cell.getIndex();
                }
            }
            return -1;
        }

        /// Returns the first attached visible list item accepted by the supplied predicate.
        private @Nullable Node findAttachedVisibleItem(Predicate<? super Node> predicate) {
            for (M3ListCell<T> cell : getCells()) {
                if (cell.isEmpty() || cell.getScene() == null) {
                    continue;
                }
                @Nullable Node listItem = cell.getListItem();
                if (listItem != null && predicate.test(listItem)) {
                    return listItem;
                }
            }
            return null;
        }
    }
}
