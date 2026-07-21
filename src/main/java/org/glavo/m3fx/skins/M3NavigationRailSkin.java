// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.internal.animation.M3DoubleTransition;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3NavigationRail].
///
/// The skin lays out the optional header and navigation destinations in compact or expanded form. Expansion changes
/// animate rail width and use fade-through when destination content changes layout; disabling motion applies the
/// target layout immediately. The item viewport clips intermediate geometry during the transition.
@NotNullByDefault
public final class M3NavigationRailSkin extends SkinBase<M3NavigationRail> {
    /// The visual expanded pseudo-class switched at the fade-through midpoint.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    /// The leading inset used by header content in an expanded rail.
    private static final double EXPANDED_HEADER_INSET = 16.0;

    /// The progress below which collapsed destination content is visible.
    private static final double COLLAPSED_FADE_END = 0.42;

    /// The progress above which expanded destination content is visible.
    private static final double EXPANDED_FADE_START = 0.58;

    /// The internal vertical item container.
    private final NavigationContainer container = new NavigationContainer();

    /// The clipped rail content layer used by immersive hide transitions.
    private final Pane contentLayer = new Pane();

    /// The reusable clip that prevents animated content from painting outside the rail width.
    private final Rectangle contentClip = new Rectangle();

    /// The currently installed optional header node.
    private @Nullable Node header;

    /// The current width transition progress from collapsed zero to expanded one.
    private final DoubleProperty expansionProgress = new SimpleDoubleProperty(this, "expansionProgress") {
        /// Requests a new rail layout for the animated width.
        @Override
        protected void invalidated() {
            updateTransitionVisuals();
            updateHiddenPresentation();
            getSkinnable().requestLayout();
        }
    };

    /// The finite expanded-width transition.
    private final M3DoubleTransition expansionAnimation = new M3DoubleTransition(
            expansionProgress,
            M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
            0.0,
            1.0
    );

    /// Whether destination content is currently using the rail fade-through transition.
    private boolean transitionActive;

    /// The item layout currently applied during a fade-through transition.
    private @Nullable M3NavigationItemLayout transitionItemLayout;

    /// The expansion progress captured when the current transition started.
    private double transitionStartProgress;

    /// The expansion target of the current transition.
    private double transitionTargetProgress;

    /// The top padding captured before the expanded pseudo-class changed its CSS tokens.
    private double transitionStartTopPadding;

    /// The bottom padding captured before the expanded pseudo-class changed its CSS tokens.
    private double transitionStartBottomPadding;

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
            (observable, oldValue, newValue) -> updateExpandedState(newValue, isVisibleInWindow());

    /// Recomputes the collapsed target when immersive hiding is enabled or disabled.
    private final ChangeListener<Boolean> hideWhenCollapsedListener =
            (observable, oldValue, newValue) -> updateExpandedState(
                    getSkinnable().isExpanded(),
                    isVisibleInWindow()
            );

    /// Requests a destination relayout when top or center alignment changes.
    private final ChangeListener<Boolean> itemsCenteredListener =
            (observable, oldValue, newValue) -> container.requestLayout();

    /// Creates a navigation rail skin.
    ///
    /// @param control the skinned navigation rail
    public M3NavigationRailSkin(M3NavigationRail control) {
        super(control);
        expansionAnimation.setOnFinished(event -> finishExpansionAnimation());
        contentLayer.setManaged(false);
        contentLayer.setClip(contentClip);
        contentLayer.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        container.setManaged(false);
        contentLayer.getChildren().add(container);
        getChildren().setAll(contentLayer);
        control.getItems().addListener(itemsListener);
        control.itemSpacingProperty().addListener(itemSpacingListener);
        control.headerSpacingProperty().addListener(headerSpacingListener);
        control.headerProperty().addListener(headerListener);
        control.expandedProperty().addListener(expandedListener);
        control.hideWhenCollapsedProperty().addListener(hideWhenCollapsedListener);
        control.itemsCenteredProperty().addListener(itemsCenteredListener);
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
        clearTransitionVisuals();
        control.getItems().removeListener(itemsListener);
        control.itemSpacingProperty().removeListener(itemSpacingListener);
        control.headerSpacingProperty().removeListener(headerSpacingListener);
        control.headerProperty().removeListener(headerListener);
        control.expandedProperty().removeListener(expandedListener);
        control.hideWhenCollapsedProperty().removeListener(hideWhenCollapsedListener);
        control.itemsCenteredProperty().removeListener(itemsCenteredListener);
        contentLayer.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        contentLayer.getChildren().clear();
        contentLayer.setClip(null);
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
        return leftInset + animatedContentWidth(
                leftInset,
                rightInset,
                getSkinnable().getExpandedMinimumContainerWidth()
        ) + rightInset;
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
        M3NavigationRail control = getSkinnable();
        double minimum = control.getExpandedMinimumContainerWidth();
        double maximum = Math.max(minimum, control.getExpandedMaximumContainerWidth());
        double preferred = Math.max(minimum, Math.min(maximum, control.getExpandedContainerWidth()));
        return leftInset + animatedContentWidth(leftInset, rightInset, preferred) + rightInset;
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
        M3NavigationRail control = getSkinnable();
        return leftInset + animatedContentWidth(
                leftInset,
                rightInset,
                Math.max(
                        control.getExpandedMinimumContainerWidth(),
                        control.getExpandedMaximumContainerWidth()
                )
        ) + rightInset;
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
        if (transitionActive) {
            M3NavigationRail control = getSkinnable();
            double fraction = transitionFraction();
            double targetTopPadding = control.getPadding().getTop();
            double targetBottomPadding = control.getPadding().getBottom();
            double visualTopPadding = interpolate(transitionStartTopPadding, targetTopPadding, fraction);
            double visualBottomPadding = interpolate(transitionStartBottomPadding, targetBottomPadding, fraction);
            y += visualTopPadding - targetTopPadding;
            height += targetTopPadding + targetBottomPadding - visualTopPadding - visualBottomPadding;
        }

        double layerWidth = Math.max(0.0, width);
        double layerHeight = Math.max(0.0, height);
        contentLayer.resizeRelocate(x, y, layerWidth, layerHeight);
        contentClip.setWidth(layerWidth);
        contentClip.setHeight(layerHeight);

        @Nullable Node currentHeader = header;
        if (currentHeader == null || !currentHeader.isVisible()) {
            container.resizeRelocate(0.0, 0.0, layerWidth, layerHeight);
            return;
        }

        double headerWidth = Math.min(layerWidth, currentHeader.prefWidth(-1.0));
        double headerHeight = currentHeader.prefHeight(headerWidth);
        double collapsedX = (layerWidth - headerWidth) / 2.0;
        // The RTL content layer mirrors this local leading-edge coordinate together with the header node.
        double headerX = collapsedX + (EXPANDED_HEADER_INSET - collapsedX) * expansionProgress.get();
        if (currentHeader.isResizable()) {
            currentHeader.resizeRelocate(
                    snapPositionX(headerX),
                    0.0,
                    snapSizeX(headerWidth),
                    snapSizeY(headerHeight)
            );
        } else {
            javafx.geometry.Bounds bounds = currentHeader.getLayoutBounds();
            currentHeader.relocate(
                    snapPositionX(headerX + (headerWidth - bounds.getWidth()) / 2.0 - bounds.getMinX()),
                    snapPositionY((headerHeight - bounds.getHeight()) / 2.0 - bounds.getMinY())
            );
        }

        double itemY = headerHeight + getSkinnable().getHeaderSpacing();
        container.resizeRelocate(
                0.0,
                snapPositionY(itemY),
                layerWidth,
                Math.max(0.0, layerHeight - itemY)
        );
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
    private double animatedContentWidth(double leftInset, double rightInset, double expandedContainerWidth) {
        M3NavigationRail control = getSkinnable();
        double collapsed = control.isHideWhenCollapsed()
                ? 0.0
                : Math.max(0.0, control.getCollapsedContainerWidth() - leftInset - rightInset);
        double expanded = Math.max(
                collapsed,
                expandedContainerWidth - leftInset - rightInset
        );
        return collapsed + (expanded - collapsed) * expansionProgress.get();
    }

    /// Returns whether the rail is attached to a currently visible window that can render animation pulses.
    private boolean isVisibleInWindow() {
        @Nullable javafx.scene.Scene scene = getSkinnable().getScene();
        return scene != null && scene.getWindow() != null && scene.getWindow().isShowing();
    }

    /// Applies an expanded-state target, optionally using Material spatial motion.
    private void updateExpandedState(boolean expanded, boolean animate) {
        double target = expanded ? 1.0 : 0.0;
        if (!animate || Double.compare(expansionProgress.get(), target) == 0) {
            expansionAnimation.stop();
            clearTransitionVisuals();
            expansionProgress.set(target);
            applyItemLayout(expanded ? M3NavigationItemLayout.HORIZONTAL : M3NavigationItemLayout.VERTICAL);
            return;
        }

        beginTransition(target);

        M3MotionSpec spec = expanded
                ? M3Animation.defaultSpatial(getSkinnable())
                : M3Animation.fastSpatial(getSkinnable());
        expansionAnimation.configure(spec, target);
        M3Animation.playFromStart(getSkinnable(), expansionAnimation);
    }

    /// Applies the final item arrangement after an expansion transition settles.
    private void finishExpansionAnimation() {
        clearTransitionVisuals();
        applyItemLayout(getSkinnable().isExpanded()
                ? M3NavigationItemLayout.HORIZONTAL
                : M3NavigationItemLayout.VERTICAL);
    }

    /// Captures transition geometry and enables fade-through item layout switching.
    private void beginTransition(double target) {
        M3NavigationRail control = getSkinnable();
        transitionActive = true;
        updateHiddenPresentation();
        transitionItemLayout = null;
        transitionStartProgress = expansionProgress.get();
        transitionTargetProgress = target;
        transitionStartTopPadding = control.getPadding().getTop();
        transitionStartBottomPadding = control.getPadding().getBottom();
        control.pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, transitionStartProgress >= 0.5);
        updateTransitionVisuals();
    }

    /// Updates destination layout and opacity for the current fade-through frame.
    private void updateTransitionVisuals() {
        if (!transitionActive) {
            return;
        }

        double progress = expansionProgress.get();
        M3NavigationItemLayout layout = progress >= 0.5
                ? M3NavigationItemLayout.HORIZONTAL
                : M3NavigationItemLayout.VERTICAL;
        if (transitionItemLayout != layout) {
            transitionItemLayout = layout;
            getSkinnable().pseudoClassStateChanged(
                    EXPANDED_PSEUDO_CLASS,
                    layout == M3NavigationItemLayout.HORIZONTAL
            );
            applyItemLayout(layout);
        }

        double opacity;
        if (progress <= COLLAPSED_FADE_END) {
            opacity = 1.0 - progress / COLLAPSED_FADE_END;
        } else if (progress >= EXPANDED_FADE_START) {
            opacity = (progress - EXPANDED_FADE_START) / (1.0 - EXPANDED_FADE_START);
        } else {
            opacity = 0.0;
        }
        applyItemTransitionOpacity(opacity);
    }

    /// Restores settled item opacity and clears transient collapse styling.
    private void clearTransitionVisuals() {
        boolean restoreOpacity = transitionActive;
        transitionActive = false;
        transitionItemLayout = null;
        getSkinnable().pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, getSkinnable().isExpanded());
        if (restoreOpacity) {
            applyItemTransitionOpacity(1.0);
        }
        updateHiddenPresentation();
    }

    /// Keeps immersive rail content clipped, non-interactive, and absent from traversal at its hidden endpoint.
    private void updateHiddenPresentation() {
        boolean hideWhenCollapsed = getSkinnable().isHideWhenCollapsed();
        double progress = expansionProgress.get();
        boolean hidden = hideWhenCollapsed && progress <= 0.000001 && !transitionActive;
        contentLayer.setVisible(!hidden);
        contentLayer.setMouseTransparent(hidden);
        contentLayer.setOpacity(hideWhenCollapsed ? Math.max(0.0, Math.min(1.0, progress)) : 1.0);
    }

    /// Applies one opacity to navigation item skin content without changing control opacity.
    private void applyItemTransitionOpacity(double opacity) {
        for (Node child : getSkinnable().getItems()) {
            if (child instanceof M3NavigationItem item
                    && item.getSkin() instanceof M3NavigationItemSkin itemSkin) {
                itemSkin.setRailTransitionOpacity(opacity);
            }
        }
    }

    /// Returns normalized elapsed progress for the current expansion or collapse transition.
    private double transitionFraction() {
        double distance = transitionTargetProgress - transitionStartProgress;
        if (Math.abs(distance) < 0.000001) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, (expansionProgress.get() - transitionStartProgress) / distance));
    }

    /// Interpolates between two scalar layout values.
    private static double interpolate(double start, double end, double fraction) {
        return start + (end - start) * fraction;
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
            contentLayer.getChildren().remove(header);
        }
        header = newHeader;
        if (newHeader != null) {
            contentLayer.getChildren().add(newHeader);
        }
        getSkinnable().requestLayout();
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        container.getChildren().setAll(getSkinnable().getItems());
        if (transitionActive) {
            updateTransitionVisuals();
        }
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
        double spacing = getSkinnable().getItemSpacing();
        double currentY = getSkinnable().isItemsCentered()
                ? Math.max(0.0, (container.getHeight() - itemHeightSum(width, false)) / 2.0)
                : 0.0;
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