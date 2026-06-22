// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NavigationDrawerGroup].
@NotNullByDefault
public final class M3NavigationDrawerGroupSkin extends SkinBase<M3NavigationDrawerGroup> {
    /// The spacing between the header row and child row container.
    private static final double ITEM_SPACING = 4.0;

    /// The vertical offset applied to child rows while expanding or collapsing.
    private static final double CHILD_TRANSITION_OFFSET = -6.0;

    /// The empty padding applied to the child row container before child indentation is resolved.
    private static final Insets EMPTY_CHILD_PADDING = Insets.EMPTY;

    /// The clipped viewport for child destination rows.
    private final Pane childViewport = new Pane();

    /// The vertical container holding child destination rows during expansion.
    private final VBox childrenContainer = new VBox();

    /// The clip that reveals child rows as the group expands.
    private final Rectangle childrenClip = new Rectangle();

    // The current child row reveal progress from collapsed `0` to expanded `1`.
    private final DoubleProperty expansionProgress = new SimpleDoubleProperty(this, "expansionProgress") {
        /// Updates child row visibility and layout after reveal progress changes.
        @Override
        protected void invalidated() {
            updateExpansionProgress();
        }
    };

    /// The expansion and collapse animation for child rows.
    private final Timeline expansionAnimation = new Timeline();

    /// Settles running child-row expansion transitions when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    () -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), expansionAnimation)
            );

    /// Mirrors child destination item changes into the skin container.
    private final ListChangeListener<M3ListItem> itemsListener = change -> updateChildItems();

    /// Mirrors expanded-state changes into the child row viewport.
    private final ChangeListener<Boolean> expandedListener =
            (observable, oldValue, newValue) -> setExpandedState(newValue, shouldAnimateExpansion());

    /// Requests layout when the effective node orientation changes at runtime.
    private final ChangeListener<NodeOrientation> orientationListener = (observable, oldValue, newValue) ->
            getSkinnable().requestLayout();

    /// Whether child items are currently mounted in the viewport.
    private boolean childItemsMounted;

    /// Creates a navigation drawer group skin.
    ///
    /// @param control the skinned navigation drawer group
    public M3NavigationDrawerGroupSkin(M3NavigationDrawerGroup control) {
        super(control);
        childViewport.setManaged(false);
        childViewport.setClip(childrenClip);
        childViewport.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        childrenContainer.setManaged(false);
        childrenContainer.setSpacing(ITEM_SPACING);
        childrenContainer.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        childViewport.getChildren().add(childrenContainer);
        getChildren().addAll(control.getHeaderItem(), childViewport);
        control.getItems().addListener(itemsListener);
        control.expandedProperty().addListener(expandedListener);
        control.effectiveNodeOrientationProperty().addListener(orientationListener);
        setExpandedState(control.isExpanded(), false);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        expansionAnimation.stop();
        getSkinnable().getItems().removeListener(itemsListener);
        motionSettingsObserver.dispose();
        getSkinnable().expandedProperty().removeListener(expandedListener);
        getSkinnable().effectiveNodeOrientationProperty().removeListener(orientationListener);
        childViewport.nodeOrientationProperty().unbind();
        childrenContainer.nodeOrientationProperty().unbind();
        childrenContainer.getChildren().clear();
        childViewport.getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the internal item container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + contentWidth(height, (region, dimension) -> region.minWidth(dimension)) + rightInset;
    }

    /// Computes the minimum height from the internal item container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + contentHeight(width, (region, dimension) -> region.minHeight(dimension)) + bottomInset;
    }

    /// Computes the preferred width from the internal item container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + contentWidth(height, (region, dimension) -> region.prefWidth(dimension)) + rightInset;
    }

    /// Computes the preferred height from the internal item container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + contentHeight(width, (region, dimension) -> region.prefHeight(dimension)) + bottomInset;
    }

    /// Computes the maximum width from the internal item container.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + contentWidth(height, (region, dimension) -> region.maxWidth(dimension)) + rightInset;
    }

    /// Computes the maximum height from the internal item container.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + contentHeight(width, (region, dimension) -> region.maxHeight(dimension)) + bottomInset;
    }

    /// Lays out the header and clipped child row viewport.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3ListItem headerItem = getSkinnable().getHeaderItem();
        updateListItemWidth(headerItem, width);
        double headerHeight = headerItem.prefHeight(width);
        headerItem.resizeRelocate(x, y, width, headerHeight);

        if (!childItemsMounted) {
            childViewport.setVisible(false);
            return;
        }

        double childEdgeInset = childEdgeInset();
        updateChildrenContainerPadding(childEdgeInset);
        updateChildItemWidths(width, childEdgeInset);
        double childrenHeight = childrenContainer.prefHeight(width);
        double viewportHeight = childrenHeight * expansionProgress.get();
        if (viewportHeight <= 0.0) {
            childViewport.setVisible(false);
            return;
        }

        childViewport.setVisible(true);
        childViewport.resizeRelocate(x, y + headerHeight + ITEM_SPACING, width, viewportHeight);
        childrenClip.setWidth(width);
        childrenClip.setHeight(viewportHeight);
        childrenContainer.resizeRelocate(0.0, 0.0, width, childrenHeight);
        childrenContainer.layout();
    }

    /// Returns the combined content width from the header and mounted children.
    private double contentWidth(double height, SizeFunction sizeFunction) {
        double headerWidth = sizeFunction.size(getSkinnable().getHeaderItem(), height);
        double childrenWidth = childItemsMounted ? sizeFunction.size(childrenContainer, height) : 0.0;
        return Math.max(headerWidth, childrenWidth);
    }

    /// Returns the combined content height from the header and animated child viewport.
    private double contentHeight(double width, SizeFunction sizeFunction) {
        double headerHeight = sizeFunction.size(getSkinnable().getHeaderItem(), width);
        if (!childItemsMounted) {
            return headerHeight;
        }

        double childrenHeight = sizeFunction.size(childrenContainer, width);
        double progress = expansionProgress.get();
        if (childrenHeight <= 0.0 || progress <= 0.0) {
            return headerHeight;
        }
        return headerHeight + ITEM_SPACING + childrenHeight * progress;
    }

    /// Updates mounted child rows after the public child list changes.
    private void updateChildItems() {
        if (shouldMountChildItems()) {
            mountChildItems();
        } else {
            unmountChildItems();
        }
        getSkinnable().requestLayout();
    }

    /// Applies the expanded state, using animation when the group is attached to a scene.
    private void setExpandedState(boolean expanded, boolean animate) {
        expansionAnimation.stop();
        expansionAnimation.setOnFinished(null);
        if (expanded) {
            mountChildItems();
        }

        double targetProgress = expanded ? 1.0 : 0.0;
        if (!animate || Double.compare(expansionProgress.get(), targetProgress) == 0) {
            expansionProgress.set(targetProgress);
            if (!expanded) {
                unmountChildItems();
            }
            return;
        }

        boolean targetExpanded = expanded;
        M3MotionSpec spec = targetExpanded
                ? M3Animation.defaultSpatial(getSkinnable())
                : M3Animation.fastSpatial(getSkinnable());
        expansionAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(
                        expansionProgress,
                        targetProgress,
                        spec.interpolator()
                )
        ));
        expansionAnimation.setOnFinished(event -> {
            if (!targetExpanded) {
                unmountChildItems();
            }
            expansionAnimation.setOnFinished(null);
        });
        M3Animation.playFromStart(getSkinnable(), expansionAnimation);
    }

    /// Returns whether expanded-state changes should animate.
    private boolean shouldAnimateExpansion() {
        return getSkinnable().getScene() != null;
    }

    /// Returns whether child rows should be kept mounted in the viewport.
    private boolean shouldMountChildItems() {
        return getSkinnable().isExpanded()
                || expansionProgress.get() > 0.0
                || expansionAnimation.getStatus() == Timeline.Status.RUNNING;
    }

    /// Mounts current child rows into the child viewport.
    private void mountChildItems() {
        childItemsMounted = true;
        childrenContainer.getChildren().setAll(getSkinnable().getItems());
        updateExpansionProgress();
    }

    /// Unmounts hidden child rows from the child viewport.
    private void unmountChildItems() {
        childItemsMounted = false;
        childrenContainer.getChildren().clear();
        childViewport.setVisible(false);
        getSkinnable().requestLayout();
    }

    /// Updates child viewport visual state from the current reveal progress.
    private void updateExpansionProgress() {
        double progress = expansionProgress.get();
        childViewport.setVisible(childItemsMounted && progress > 0.0);
        childrenContainer.setOpacity(progress);
        childrenContainer.setTranslateY((1.0 - progress) * CHILD_TRANSITION_OFFSET);
        getSkinnable().requestLayout();
    }

    /// Keeps child list item containers inside the group content area.
    private void updateChildItemWidths(double width, double childEdgeInset) {
        double itemWidth = Math.max(0.0, width - childEdgeInset);
        for (M3ListItem item : getSkinnable().getItems()) {
            updateListItemWidth(item, itemWidth);
        }
    }

    /// Returns the child row edge inset derived from child and header content padding.
    private double childEdgeInset() {
        double headerPadding = getSkinnable().getHeaderItem().getHorizontalPadding();
        double childPadding = 0.0;
        for (M3ListItem item : getSkinnable().getItems()) {
            childPadding = Math.max(childPadding, item.getHorizontalPadding());
        }
        return Math.max(0.0, childPadding - headerPadding);
    }

    /// Updates the child container padding that creates indented selected row geometry.
    private void updateChildrenContainerPadding(double childEdgeInset) {
        Insets padding = childEdgeInset == 0.0
                ? EMPTY_CHILD_PADDING
                : new Insets(0.0, 0.0, 0.0, childEdgeInset);
        if (!padding.equals(childrenContainer.getPadding())) {
            childrenContainer.setPadding(padding);
        }
    }

    /// Keeps one list item container inside the group content area.
    private static void updateListItemWidth(M3ListItem item, double itemWidth) {
        if (Double.compare(item.getMinWidth(), 0.0) != 0) {
            item.setMinWidth(0.0);
        }
        if (Double.compare(item.getMaxWidth(), itemWidth) != 0) {
            item.setMaxWidth(itemWidth);
        }
    }

    /// Computes one dimension for a region.
    @FunctionalInterface
    @NotNullByDefault
    private interface SizeFunction {
        /// Returns the requested size for a region.
        double size(javafx.scene.layout.Region region, double oppositeDimension);
    }
}
