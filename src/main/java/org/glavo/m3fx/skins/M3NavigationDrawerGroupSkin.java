// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.internal.animation.M3DoubleTransition;

import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NavigationDrawerGroup].
///
/// The skin lays out the group header above an optional child list and animates the child viewport when expanded
/// state changes. Collapsed children are removed from the presented viewport after the transition so they do not
/// remain reachable through picking or focus traversal.
@NotNullByDefault
public final class M3NavigationDrawerGroupSkin extends SkinBase<M3NavigationDrawerGroup> {
    /// The spacing between adjacent navigation drawer destination rows.
    private static final double ITEM_SPACING = 0.0;

    /// The vertical offset applied to child rows while expanding or collapsing.
    private static final double CHILD_TRANSITION_OFFSET = -6.0;

    /// The empty padding applied to the child row container before child indentation is resolved.
    private static final Insets EMPTY_CHILD_PADDING = Insets.EMPTY;

    /// The clipped viewport for child destination rows.
    private final Pane childViewport = new ChildViewport();

    /// The vertical container holding child destination rows during expansion.
    private final VBox childrenContainer = new VBox();

    /// The clip that reveals child rows as the group expands.
    private final Rectangle childrenClip = new Rectangle();

    /// The current child row reveal progress from collapsed `0` to expanded `1`.
    private final DoubleProperty expansionProgress = new SimpleDoubleProperty(this, "expansionProgress") {
        /// Updates child row visibility and layout after reveal progress changes.
        @Override
        protected void invalidated() {
            updateExpansionProgress();
        }
    };

    /// The expansion and collapse animation for child rows.
    private final M3DoubleTransition expansionAnimation = new M3DoubleTransition(
            expansionProgress,
            M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
            0.0,
            1.0
    );

    /// Whether the currently configured expansion animation targets the expanded state.
    private boolean expansionAnimationTargetExpanded;

    /// Mirrors child destination item changes into the skin container.
    private final ListChangeListener<M3ListItem> itemsListener = change -> updateChildItems();

    /// Mirrors expanded-state changes into the child row viewport.
    private final ChangeListener<Boolean> expandedListener =
            (observable, oldValue, newValue) -> setExpandedState(newValue, getSkinnable().getScene() != null);

    /// Whether child items are currently mounted in the viewport.
    private boolean childItemsMounted;

    /// The child edge inset currently applied to the child container.
    private double appliedChildEdgeInset = Double.NaN;

    /// Creates a navigation drawer group skin.
    ///
    /// @param control the skinned navigation drawer group
    public M3NavigationDrawerGroupSkin(M3NavigationDrawerGroup control) {
        super(control);
        childViewport.setClip(childrenClip);
        childrenContainer.setSpacing(ITEM_SPACING);
        expansionAnimation.setOnFinished(event -> finishExpansionAnimation());
        childViewport.getChildren().add(childrenContainer);
        getChildren().setAll(control.getHeaderItem(), childViewport);
        control.getItems().addListener(itemsListener);
        control.expandedProperty().addListener(expandedListener);
        setExpandedState(control.isExpanded(), false);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3NavigationDrawerGroup control = getSkinnable();
        expansionAnimation.stop();
        expansionAnimation.setOnFinished(null);
        control.getItems().removeListener(itemsListener);
        control.expandedProperty().removeListener(expandedListener);
        childrenContainer.getChildren().clear();
        childViewport.getChildren().clear();
        if (control.getSkin() == null || control.getSkin() == this) {
            getChildren().clear();
        }
        super.dispose();
    }

    /// Returns whether the child viewport is currently moving between collapsed and expanded geometry.
    ///
    /// @return `true` while the reusable expansion animation is running
    boolean isExpansionAnimationRunning() {
        return expansionAnimation.getStatus() == Animation.Status.RUNNING;
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
        return leftInset + contentWidth(height, topInset, bottomInset, Region::minWidth) + rightInset;
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
        return topInset + contentHeight(width, leftInset, rightInset, Region::minHeight) + bottomInset;
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
        return leftInset + contentWidth(height, topInset, bottomInset, Region::prefWidth) + rightInset;
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
        return topInset + contentHeight(width, leftInset, rightInset, Region::prefHeight) + bottomInset;
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
        return leftInset + contentWidth(height, topInset, bottomInset, Region::maxWidth) + rightInset;
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
        return topInset + contentHeight(width, leftInset, rightInset, Region::maxHeight) + bottomInset;
    }

    /// Lays out the header and clipped child row viewport.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3ListItem headerItem = getSkinnable().getHeaderItem();
        double headerHeight = headerItem.prefHeight(width);
        headerItem.resizeRelocate(x, y, width, headerHeight);

        if (!childItemsMounted) {
            childViewport.setVisible(false);
            return;
        }

        updateChildrenContainerPadding();
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
    }

    /// Measures header and mounted-child widths within the control's vertical insets.
    /// Negative height constraints remain unconstrained; smaller constrained heights leave zero content space.
    private double contentWidth(double height, double topInset, double bottomInset, SizeFunction sizeFunction) {
        height = height < 0.0 ? height : Math.max(0.0, height - topInset - bottomInset);
        double headerWidth = sizeFunction.size(getSkinnable().getHeaderItem(), height);
        if (!childItemsMounted) {
            return headerWidth;
        }
        updateChildrenContainerPadding();
        return Math.max(headerWidth, sizeFunction.size(childrenContainer, height));
    }

    /// Measures the header and animated child viewport within the control's horizontal insets.
    /// Negative width constraints remain unconstrained; smaller constrained widths leave zero content space.
    private double contentHeight(double width, double leftInset, double rightInset, SizeFunction sizeFunction) {
        width = width < 0.0 ? width : Math.max(0.0, width - leftInset - rightInset);
        double headerHeight = sizeFunction.size(getSkinnable().getHeaderItem(), width);
        if (!childItemsMounted) {
            return headerHeight;
        }

        updateChildrenContainerPadding();
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
        if (expanded) {
            mountChildItems();
        }

        double targetProgress = expanded ? 1.0 : 0.0;
        if (!animate || Double.compare(expansionProgress.get(), targetProgress) == 0) {
            expansionAnimation.stop();
            expansionProgress.set(targetProgress);
            if (!expanded) {
                unmountChildItems();
            }
            return;
        }

        M3MotionSpec spec = expanded
                ? M3Animation.defaultSpatial(getSkinnable())
                : M3Animation.fastSpatial(getSkinnable());
        expansionAnimationTargetExpanded = expanded;
        expansionAnimation.configure(spec, targetProgress);
        M3Animation.playFromStart(getSkinnable(), expansionAnimation);
    }

    /// Unmounts collapsed child rows after the reusable expansion animation completes.
    private void finishExpansionAnimation() {
        if (!expansionAnimationTargetExpanded && !getSkinnable().isExpanded()) {
            unmountChildItems();
        }
    }

    /// Returns whether child rows should be kept mounted in the viewport.
    private boolean shouldMountChildItems() {
        return getSkinnable().isExpanded()
                || expansionProgress.get() > 0.0
                || expansionAnimation.getStatus() == Animation.Status.RUNNING;
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

    /// Updates indentation from current row padding before measurement or layout.
    private void updateChildrenContainerPadding() {
        double headerPadding = getSkinnable().getHeaderItem().getHorizontalPadding();
        double childPadding = 0.0;
        for (M3ListItem item : getSkinnable().getItems()) {
            childPadding = Math.max(childPadding, item.getHorizontalPadding());
        }
        double childEdgeInset = Math.max(0.0, childPadding - headerPadding);
        if (Double.compare(appliedChildEdgeInset, childEdgeInset) == 0) {
            return;
        }
        appliedChildEdgeInset = childEdgeInset;
        Insets padding = childEdgeInset == 0.0
                ? EMPTY_CHILD_PADDING
                : new Insets(0.0, 0.0, 0.0, childEdgeInset);
        childrenContainer.setPadding(padding);
    }

    /// Fits rows to the viewport width while retaining their full height throughout disclosure motion.
    /// Both containers remain managed so row size changes invalidate the enclosing control and its parents.
    @NotNullByDefault
    private final class ChildViewport extends Pane {
        /// Creates the viewport whose clip is maintained by the enclosing skin.
        private ChildViewport() {
        }

        /// Allocates full row height independently of the viewport's partially revealed height.
        @Override
        protected void layoutChildren() {
            double width = getWidth();
            childrenContainer.resizeRelocate(0.0, 0.0, width, childrenContainer.prefHeight(width));
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
