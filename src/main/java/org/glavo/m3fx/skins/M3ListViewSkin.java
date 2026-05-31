// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;
import javafx.util.Callback;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ListView;
import org.glavo.m3fx.controls.M3ListViewCell;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default virtualized skin for [M3ListView].
///
/// @param <T> the item type rendered by the skinned list view
@NotNullByDefault
public final class M3ListViewSkin<T> extends SkinBase<M3ListView<T>> {
    /// The fallback row height used before the virtual flow has measured a visible cell.
    private static final double DEFAULT_ROW_HEIGHT = 56.0;

    /// The default wheel line distance used when a platform reports text-line scroll units.
    private static final double DEFAULT_LINE_SCROLL_PIXELS = 40.0;

    /// The minimum meaningful scroll value difference.
    private static final double EPSILON = 0.000001;

    /// The virtualized cell container.
    private final ListViewVirtualFlow<T> flow = new ListViewVirtualFlow<>();

    /// Updates virtual flow cell count when data items change.
    private final ListChangeListener<T> itemsListener = change -> refreshItemCount();

    /// Rebuilds visible cells when the cell factory changes.
    private final InvalidationListener cellFactoryInvalidation = observable -> rebuildCells();

    /// Updates visible cells when selection changes.
    private final ListChangeListener<Integer> selectedIndicesListener = change -> refreshCells();

    /// Updates logical focused-row visuals when the list view focus owner state changes.
    private final InvalidationListener focusedInvalidation = observable -> refreshCells();

    /// Handles wheel and trackpad scrolling through Material motion.
    private final EventHandler<ScrollEvent> smoothScrollHandler = this::handleSmoothScroll;

    /// Updates running smooth scroll when global or node-local motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(getSkinnable(), this::refreshMotionSettings);

    // The currently running virtual flow scroll animation.
    private @Nullable Timeline smoothScrollAnimation;

    /// The completion callback attached to the currently running smooth scroll animation.
    private @Nullable Runnable smoothScrollOnFinished;

    /// The accumulated target virtual flow position.
    private double smoothScrollTargetPosition;

    /// Whether a focused cell should refresh logical row focus after the next layout pass.
    private boolean focusRequestPending;

    /// Whether a deferred focus retry has already been queued for the next pulse.
    private boolean focusRetryScheduled;

    /// Creates a virtualized list view skin.
    ///
    /// @param control the skinned virtualized list view
    public M3ListViewSkin(M3ListView<T> control) {
        super(control);
        flow.getStyleClass().add("m3-list-view-flow");
        flow.setVertical(true);
        flow.setPannable(true);
        flow.setCellFactory(createCellFactory(control));
        flow.fixedCellSizeProperty().bind(control.fixedCellSizeProperty());
        control.addEventFilter(ScrollEvent.SCROLL, smoothScrollHandler);
        getChildren().add(flow);

        control.getItems().addListener(itemsListener);
        control.cellFactoryProperty().addListener(cellFactoryInvalidation);
        control.getSelectedIndices().addListener(selectedIndicesListener);
        control.focusedProperty().addListener(focusedInvalidation);
        refreshItemCount();
    }

    /// Stops bindings and removes listeners before disposal.
    @Override
    public void dispose() {
        M3ListView<T> listView = getSkinnable();
        motionSettingsObserver.dispose();
        stopSmoothScrollAnimation();
        listView.removeEventFilter(ScrollEvent.SCROLL, smoothScrollHandler);
        listView.getItems().removeListener(itemsListener);
        listView.cellFactoryProperty().removeListener(cellFactoryInvalidation);
        listView.getSelectedIndices().removeListener(selectedIndicesListener);
        listView.focusedProperty().removeListener(focusedInvalidation);
        flow.fixedCellSizeProperty().unbind();
        flow.setCellFactory(null);
        super.dispose();
    }

    /// Lays out the virtual flow in the available bounds.
    ///
    /// @param x the layout area's x coordinate
    /// @param y the layout area's y coordinate
    /// @param width the layout area's width
    /// @param height the layout area's height
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        flow.resizeRelocate(x, y, width, height);
        if (focusRequestPending) {
            flow.applyCss();
            flow.layout();
        }
        focusVisibleCellIfNeeded();
        scheduleFocusRetry();
    }

    /// Computes the preferred width from the virtual flow.
    ///
    /// @param height the height that should be used if width depends on it
    /// @param topInset the snapped top inset
    /// @param rightInset the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset the snapped left inset
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
    /// @param width the width that should be used if height depends on it
    /// @param topInset the snapped top inset
    /// @param rightInset the snapped right inset
    /// @param bottomInset the snapped bottom inset
    /// @param leftInset the snapped left inset
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
        double visibleRows = Math.min(8.0, Math.max(1.0, getSkinnable().getItems().size()));
        double rowHeight = fixedCellSize > 0.0 ? fixedCellSize : 56.0;
        return topInset + rowHeight * visibleRows + bottomInset;
    }

    /// Updates the number of cells owned by the virtual flow.
    public void refreshItemCount() {
        flow.setCellCount(getSkinnable().getItems().size());
        flow.refreshCells();
    }

    /// Requests visible cell state and layout updates.
    public void refreshCells() {
        flow.refreshCells();
    }

    /// Requests visible cell focus updates, optionally animating the scroll into view.
    ///
    /// @param requestNodeFocus whether the focused cell should request keyboard focus
    /// @param animated whether scrolling the focused cell into view should animate
    public void refreshFocus(boolean requestNodeFocus, boolean animated) {
        focusRequestPending |= requestNodeFocus;
        flow.refreshCells();
        int focusedIndex = getSkinnable().getFocusedIndex();
        if (focusedIndex >= 0) {
            scrollToIndex(focusedIndex, animated);
        } else {
            stopSmoothScrollAnimation();
        }
        getSkinnable().requestLayout();
        focusVisibleCellIfNeeded();
        scheduleFocusRetry();
    }

    /// Recreates visible cells after the cell factory changes.
    public void rebuildCells() {
        flow.rebuildAllCells();
    }

    /// Scrolls the virtual flow to the supplied index.
    ///
    /// @param index the data item index to reveal
    public void scrollTo(int index) {
        scrollTo(index, true);
    }

    /// Scrolls the virtual flow to the supplied index, optionally animating the position change.
    ///
    /// @param index the data item index to reveal
    /// @param animated whether the scroll should animate when animations are enabled
    public void scrollTo(int index, boolean animated) {
        scrollToIndex(index, animated);
    }

    /// Returns the rendered list item for a visible or reusable cell index.
    ///
    /// @param index the data item index to query
    /// @return the rendered list item node, or `null` when the index is outside the data list
    public @Nullable Node getVisibleItem(int index) {
        if (index < 0 || index >= getSkinnable().getItems().size()) {
            return null;
        }

        M3ListViewCell<T> cell = flow.visibleOrReusableCell(index);
        return cell.getListItem();
    }

    /// Returns the rendered list item only when the requested index is currently attached to the scene.
    ///
    /// @param index the data item index to query
    /// @return the attached rendered list item node, or `null` when the row is not currently materialized
    public @Nullable Node getAttachedVisibleItem(int index) {
        if (index < 0 || index >= getSkinnable().getItems().size()) {
            return null;
        }

        @Nullable M3ListViewCell<T> cell = flow.findVisibleCell(index);
        return cell == null ? null : cell.getListItem();
    }

    /// Creates the virtual flow cell factory.
    private static <T> Callback<VirtualFlow<M3ListViewCell<T>>, M3ListViewCell<T>> createCellFactory(
            M3ListView<T> listView
    ) {
        return flow -> new M3ListViewCell<>(listView);
    }

    /// Refreshes the visible cell that owns the focused index when it has been materialized.
    private void focusVisibleCellIfNeeded() {
        if (!focusRequestPending) {
            return;
        }

        int index = getSkinnable().getFocusedIndex();
        if (index < 0) {
            getSkinnable().requestFocus();
            focusRequestPending = false;
            return;
        }

        @Nullable M3ListViewCell<T> cell = flow.findVisibleCell(index);
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

            getSkinnable().applyCss();
            flow.applyCss();
            flow.layout();
            focusVisibleCellIfNeeded();
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

        if (smoothScrollAnimation == null || smoothScrollAnimation.getStatus() == Animation.Status.STOPPED) {
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
        if (M3Animation.areAnimationsEnabled(getSkinnable())) {
            animateSmoothScroll(null);
        } else {
            stopSmoothScrollAnimation();
            flow.setPosition(smoothScrollTargetPosition);
            flow.requestLayout();
        }
        event.consume();
    }

    /// Applies changed animation settings to the current smooth scroll operation.
    private void refreshMotionSettings() {
        Timeline animation = smoothScrollAnimation;
        if (animation == null || animation.getStatus() != Animation.Status.RUNNING) {
            return;
        }

        if (!M3Animation.areAnimationsEnabled(getSkinnable())) {
            @Nullable Runnable onFinished = smoothScrollOnFinished;
            animation.stop();
            smoothScrollAnimation = null;
            smoothScrollOnFinished = null;
            flow.setPosition(smoothScrollTargetPosition);
            flow.requestLayout();
            if (onFinished != null) {
                onFinished.run();
            }
        } else {
            animateSmoothScroll(smoothScrollOnFinished);
        }
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
                || getSkinnable().getScene() == null
                || !M3Animation.areAnimationsEnabled(getSkinnable())
                || Double.isNaN(targetPosition)) {
            stopSmoothScrollAnimation();
            flow.scrollTo(index);
            flow.requestLayout();
            return;
        }

        if (close(flow.getPosition(), targetPosition)) {
            flow.scrollTo(index);
            flow.requestLayout();
            return;
        }

        smoothScrollTargetPosition = targetPosition;
        animateSmoothScroll(() -> finishAnimatedIndexScroll(index));
    }

    /// Finishes focus updates after an animated index scroll reaches its target.
    private void finishAnimatedIndexScroll(int index) {
        smoothScrollAnimation = null;
        flow.requestLayout();
        if (getSkinnable().getFocusedIndex() == index) {
            focusVisibleCellIfNeeded();
            scheduleFocusRetry();
        }
    }

    /// Animates the virtual flow to the accumulated target position.
    private void animateSmoothScroll(@Nullable Runnable onFinished) {
        stopSmoothScrollAnimation();
        M3MotionSpec spec = M3Animation.defaultSpatial(getSkinnable());
        Timeline timeline = new Timeline(new KeyFrame(
                spec.duration(),
                new KeyValue(flow.positionProperty(), smoothScrollTargetPosition, spec.interpolator())
        ));
        if (onFinished != null) {
            timeline.setOnFinished(event -> onFinished.run());
        }
        smoothScrollAnimation = timeline;
        smoothScrollOnFinished = onFinished;
        M3Animation.playFromStart(getSkinnable(), timeline);
    }

    /// Stops the running smooth scroll animation.
    private void stopSmoothScrollAnimation() {
        Timeline animation = smoothScrollAnimation;
        if (animation != null) {
            animation.stop();
            smoothScrollAnimation = null;
            smoothScrollOnFinished = null;
        }
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

        double visibleCellHeight = flow.visibleCellHeight();
        if (visibleCellHeight > 0.0) {
            return visibleCellHeight;
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

    /// A public-API wrapper exposing protected virtual flow refresh hooks to this skin.
    @NotNullByDefault
    private static final class ListViewVirtualFlow<T> extends VirtualFlow<M3ListViewCell<T>> {
        /// Requests visible cell relayout and selection refresh.
        private void refreshCells() {
            requestCellLayout();
            requestLayout();
            for (M3ListViewCell<T> cell : getCells()) {
                cell.refreshSelection();
                cell.refreshFocus();
            }
        }

        /// Returns a currently attached cell for the requested index.
        private @Nullable M3ListViewCell<T> findVisibleCell(int index) {
            for (M3ListViewCell<T> cell : getCells()) {
                if (!cell.isEmpty() && cell.getIndex() == index && cell.getScene() != null) {
                    return cell;
                }
            }
            return null;
        }

        /// Returns a visible cell, asking the virtual flow for a reusable cell when needed.
        @SuppressWarnings("ConstantValue")
        private M3ListViewCell<T> visibleOrReusableCell(int index) {
            M3ListViewCell<T> cell = getVisibleCell(index);
            if (cell == null) {
                return getCell(index);
            }
            return cell;
        }

        /// Rebuilds the virtual flow cell pile from the current cell factory.
        private void rebuildAllCells() {
            recreateCells();
            requestLayout();
        }

        /// Returns the height of a currently attached cell, or zero before cells are measured.
        private double visibleCellHeight() {
            for (M3ListViewCell<T> cell : getCells()) {
                if (!cell.isEmpty() && cell.getScene() != null && cell.getHeight() > 0.0) {
                    return cell.getHeight();
                }
            }
            return 0.0;
        }
    }
}
