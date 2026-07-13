// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3NavigationRail].
@NotNullByDefault
public final class M3NavigationRailSkin extends SkinBase<M3NavigationRail> {
    /// The leading inset used by header content in an expanded rail.
    private static final double EXPANDED_HEADER_INSET = 16.0;

    /// The internal vertical item container.
    private final NavigationContainer container = new NavigationContainer();

    /// The currently installed optional header node.
    private @Nullable Node header;

    /// The current width transition progress from collapsed zero to expanded one.
    private final DoubleProperty expansionProgress = new SimpleDoubleProperty(this, "expansionProgress") {
        /// Requests a new rail layout for the animated width.
        @Override
        protected void invalidated() {
            getSkinnable().requestLayout();
        }
    };

    /// The finite expanded-width transition.
    private final M3DoubleTransition expansionAnimation = new M3DoubleTransition(expansionProgress);

    /// Settles the width transition when runtime motion is disabled.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    () -> M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), expansionAnimation)
            );

    /// Mirrors public item changes into the skin container.
    private final ListChangeListener<Node> itemsListener = change -> updateItems();

    /// Requests item relayout when the row spacing token changes.
    private final InvalidationListener itemSpacingListener = observable -> container.requestLayout();

    /// Requests rail relayout when header spacing changes.
    private final InvalidationListener headerSpacingListener = observable -> getSkinnable().requestLayout();

    /// Mirrors the optional public header into the skin.
    private final ChangeListener<@Nullable Node> headerListener =
            (observable, oldValue, newValue) -> updateHeader(newValue);

    /// Animates between collapsed and expanded rail widths.
    private final ChangeListener<Boolean> expandedListener =
            (observable, oldValue, newValue) -> updateExpandedState(newValue, getSkinnable().getScene() != null);

    /// Creates a navigation rail skin.
    ///
    /// @param control the skinned navigation rail
    public M3NavigationRailSkin(M3NavigationRail control) {
        super(control);
        expansionAnimation.setOnFinished(event -> finishExpansionAnimation());
        container.setManaged(false);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        getChildren().setAll(container);
        control.getItems().addListener(itemsListener);
        control.itemSpacingProperty().addListener(itemSpacingListener);
        control.headerSpacingProperty().addListener(headerSpacingListener);
        control.headerProperty().addListener(headerListener);
        control.expandedProperty().addListener(expandedListener);
        updateHeader(control.getHeader());
        updateItems();
        updateExpandedState(control.isExpanded(), false);
    }

    /// Removes listeners, animations, and child references before disposal.
    @Override
    public void dispose() {
        M3NavigationRail control = getSkinnable();
        expansionAnimation.stop();
        expansionAnimation.setOnFinished(null);
        motionSettingsObserver.dispose();
        control.getItems().removeListener(itemsListener);
        control.itemSpacingProperty().removeListener(itemSpacingListener);
        control.headerSpacingProperty().removeListener(headerSpacingListener);
        control.headerProperty().removeListener(headerListener);
        control.expandedProperty().removeListener(expandedListener);
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        header = null;
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width from the current collapsed-to-expanded transition.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + animatedContentWidth(leftInset, rightInset) + rightInset;
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
        return topInset + contentHeight(width, true) + bottomInset;
    }

    /// Computes the preferred width from the current collapsed-to-expanded transition.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + animatedContentWidth(leftInset, rightInset) + rightInset;
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
        return topInset + contentHeight(width, false) + bottomInset;
    }

    /// Computes the maximum width from the current collapsed-to-expanded transition.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + animatedContentWidth(leftInset, rightInset) + rightInset;
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
        return topInset + contentHeight(width, false) + bottomInset;
    }

    /// Lays out the item container across the animated rail width.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        @Nullable Node currentHeader = header;
        if (currentHeader == null || !currentHeader.isVisible()) {
            container.resizeRelocate(x, y, width, height);
            return;
        }

        double headerWidth = Math.min(width, currentHeader.prefWidth(-1.0));
        double headerHeight = currentHeader.prefHeight(headerWidth);
        double collapsedX = (width - headerWidth) / 2.0;
        double expandedX = getSkinnable().getEffectiveNodeOrientation() == javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
                ? width - headerWidth - EXPANDED_HEADER_INSET
                : EXPANDED_HEADER_INSET;
        double headerX = collapsedX + (expandedX - collapsedX) * expansionProgress.get();
        if (currentHeader.isResizable()) {
            currentHeader.resizeRelocate(
                    snapPositionX(x + headerX),
                    snapPositionY(y),
                    snapSizeX(headerWidth),
                    snapSizeY(headerHeight)
            );
        } else {
            javafx.geometry.Bounds bounds = currentHeader.getLayoutBounds();
            currentHeader.relocate(
                    snapPositionX(x + headerX + (headerWidth - bounds.getWidth()) / 2.0 - bounds.getMinX()),
                    snapPositionY(y + (headerHeight - bounds.getHeight()) / 2.0 - bounds.getMinY())
            );
        }

        double itemY = y + headerHeight + getSkinnable().getHeaderSpacing();
        container.resizeRelocate(x, snapPositionY(itemY), width, Math.max(0.0, y + height - itemY));
    }

    /// Returns the preferred or minimum height of the header and destination stack.
    private double contentHeight(double width, boolean minimum) {
        double itemHeight = minimum ? container.minHeight(width) : container.prefHeight(width);
        @Nullable Node currentHeader = header;
        if (currentHeader == null || !currentHeader.isVisible()) {
            return itemHeight;
        }
        double headerWidth = Math.min(width, currentHeader.prefWidth(-1.0));
        double headerHeight = minimum
                ? currentHeader.minHeight(headerWidth)
                : currentHeader.prefHeight(headerWidth);
        return headerHeight + getSkinnable().getHeaderSpacing() + itemHeight;
    }

    /// Returns the animated content width after accounting for control insets.
    private double animatedContentWidth(double leftInset, double rightInset) {
        M3NavigationRail control = getSkinnable();
        double collapsed = Math.max(0.0, control.getCollapsedContainerWidth() - leftInset - rightInset);
        double expanded = Math.max(
                collapsed,
                control.getExpandedContainerWidth() - leftInset - rightInset
        );
        return collapsed + (expanded - collapsed) * expansionProgress.get();
    }

    /// Applies an expanded-state target, optionally using Material spatial motion.
    private void updateExpandedState(boolean expanded, boolean animate) {
        expansionAnimation.stop();
        double target = expanded ? 1.0 : 0.0;
        if (!animate || Double.compare(expansionProgress.get(), target) == 0) {
            expansionProgress.set(target);
            applyItemLayout(expanded ? M3NavigationItemLayout.HORIZONTAL : M3NavigationItemLayout.VERTICAL);
            return;
        }

        // Keep horizontal rows during both directions so labels do not jump before the width transition settles.
        applyItemLayout(M3NavigationItemLayout.HORIZONTAL);

        M3MotionSpec spec = expanded
                ? M3Animation.defaultSpatial(getSkinnable())
                : M3Animation.fastSpatial(getSkinnable());
        expansionAnimation.configure(spec, target);
        M3Animation.playFromStart(getSkinnable(), expansionAnimation);
    }

    /// Applies the final item arrangement after an expansion transition settles.
    private void finishExpansionAnimation() {
        applyItemLayout(getSkinnable().isExpanded()
                ? M3NavigationItemLayout.HORIZONTAL
                : M3NavigationItemLayout.VERTICAL);
    }

    /// Applies one item layout to every navigation destination without allocating intermediate collections.
    private void applyItemLayout(M3NavigationItemLayout layout) {
        for (Node child : getSkinnable().getItems()) {
            if (child instanceof M3NavigationItem item) {
                item.setItemLayout(layout);
            }
        }
    }

    /// Installs the optional rail header directly in the skin's layout layer.
    private void updateHeader(@Nullable Node newHeader) {
        if (header == newHeader) {
            return;
        }
        if (header != null) {
            getChildren().remove(header);
        }
        header = newHeader;
        if (newHeader != null) {
            getChildren().add(newHeader);
        }
        getSkinnable().requestLayout();
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        container.getChildren().setAll(getSkinnable().getItems());
        container.requestLayout();
        getSkinnable().requestLayout();
    }

    /// Returns the total minimum or preferred height of managed rail items and their spacing.
    private double itemHeightSum(double width, boolean minimum) {
        double height = 0.0;
        int itemCount = 0;
        for (Node child : container.getChildren()) {
            if (!child.isManaged()) {
                continue;
            }
            height += minimum ? child.minHeight(width) : child.prefHeight(width);
            itemCount++;
        }
        if (itemCount > 1) {
            height += getSkinnable().getItemSpacing() * (itemCount - 1);
        }
        return height;
    }

    /// Lays out every managed rail item at the current available row width.
    private void layoutItems(double width) {
        double currentY = 0.0;
        double spacing = getSkinnable().getItemSpacing();
        for (Node child : container.getChildren()) {
            if (!child.isManaged()) {
                continue;
            }
            double childHeight = child.prefHeight(width);
            if (child.isResizable()) {
                child.resizeRelocate(0.0, currentY, width, childHeight);
            } else {
                container.layoutChildInArea(child, currentY, width, childHeight);
            }
            currentY += childHeight + spacing;
        }
    }

    /// Pane that applies full-row sizing without retaining fixed child minimum widths.
    @NotNullByDefault
    private final class NavigationContainer extends Pane {
        /// Computes the minimum row-stack height.
        @Override
        protected double computeMinHeight(double width) {
            return itemHeightSum(width, true);
        }

        /// Computes the preferred row-stack height.
        @Override
        protected double computePrefHeight(double width) {
            return itemHeightSum(width, false);
        }

        /// Computes the maximum row-stack height.
        @Override
        protected double computeMaxHeight(double width) {
            return itemHeightSum(width, false);
        }

        /// Lays out a non-resizable child in one full-width row.
        private void layoutChildInArea(Node child, double y, double width, double height) {
            layoutInArea(child, 0.0, y, width, height, 0.0, HPos.CENTER, VPos.CENTER);
        }

        /// Applies current full-width row geometry.
        @Override
        protected void layoutChildren() {
            layoutItems(getWidth());
        }
    }
}