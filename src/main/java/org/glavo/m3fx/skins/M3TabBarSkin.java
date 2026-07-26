// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.internal.animation.M3DoubleTransition;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.controls.M3TabBarLayout;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// The default Material Design 3 skin for [M3TabBar].
///
/// The skin presents tabs in fixed or scrollable layout, preserves the selected tab's visibility, and mirrors
/// horizontal scrolling for right-to-left orientation. Fixed layout distributes available width across tabs;
/// scrollable layout clips the row and animates offset changes when motion is enabled.
@NotNullByDefault
public final class M3TabBarSkin extends M3ItemContainerSkinBase<M3TabBar, HBox, M3Tab> {
    /// The tab row style class.
    private static final String CONTAINER_STYLE_CLASS = "m3-tab-bar-container";

    /// The divider style class.
    private static final String DIVIDER_STYLE_CLASS = "m3-tab-bar-divider";

    /// The bottom divider rendered behind active indicators.
    private final Region divider = new Region();

    /// The row that lays out and clips fixed or scrollable tabs.
    private final TabRow tabRow;

    /// Creates a tab bar skin.
    ///
    /// @param control the tab bar controlled by this skin
    public M3TabBarSkin(M3TabBar control) {
        super(control, control.getTabs(), new TabRow(control));
        tabRow = (TabRow) getContainer();
        tabRow.getStyleClass().add(CONTAINER_STYLE_CLASS);
        tabRow.setAlignment(Pos.CENTER_LEFT);
        tabRow.install();
        divider.getStyleClass().add(DIVIDER_STYLE_CLASS);
        divider.setManaged(false);
        divider.setMouseTransparent(true);
        getChildren().add(0, divider);
    }

    /// Lays out the divider inside the bottom edge without changing the tab bar's measured height.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        double dividerHeight = snapSizeY(divider.prefHeight(width));
        divider.resizeRelocate(x, y + height - dividerHeight, width, dividerHeight);
    }

    /// Removes listeners, bindings, and animation state before disposal.
    @Override
    public void dispose() {
        tabRow.dispose();
        getChildren().remove(divider);
        super.dispose();
    }

    /// A tab row that uses equal cells in fixed mode and a clipped, content-width track in scrollable mode.
    private static final class TabRow extends HBox {
        /// The logical leading inset required by scrollable Material tabs.
        private static final double SCROLLABLE_LEADING_INSET = 52.0;

        /// The tolerance used when comparing scroll positions.
        private static final double POSITION_EPSILON = 0.01;

        /// The tab bar whose layout and selection this row presents.
        private final M3TabBar control;

        /// The reusable rectangular viewport clip.
        private final Rectangle viewportClip = new Rectangle();

        /// The logical distance from the beginning of the scrollable tab track.
        private final DoubleProperty scrollOffset = new SimpleDoubleProperty(this, "scrollOffset") {
            /// Requests another row layout for the new track position.
            @Override
            protected void invalidated() {
                requestLayout();
            }
        };

        /// The reusable selection-reveal animation.
        private final M3DoubleTransition scrollAnimation = new M3DoubleTransition(
                scrollOffset,
                M3DoubleTransition.PIXEL_VISIBILITY_THRESHOLD,
                0.0,
                Double.POSITIVE_INFINITY
        );

        /// Reacts to tab selection by revealing the selected item.
        private final ChangeListener<@Nullable M3Tab> selectedTabListener =
                (observable, oldTab, newTab) -> requestReveal(newTab, true);

        /// Resets scroll state when the bar switches between fixed and scrollable layout.
        private final ChangeListener<M3TabBarLayout> tabLayoutListener =
                (observable, oldLayout, newLayout) -> refreshLayoutMode();

        /// Reveals a tab when traversal moves focus without changing selection.
        private final InvalidationListener tabFocusListener = observable -> {
            if (observable instanceof ReadOnlyProperty<?> property
                    && property.getBean() instanceof M3Tab tab
                    && tab.isFocused()) {
                requestReveal(tab, true);
            }
        };

        /// Keeps focus listeners synchronized with the public tab list.
        private final ListChangeListener<Node> childrenListener = this::updateTabFocusListeners;

        /// Applies direct wheel and touch-scroll movement to the logical tab track.
        private final EventHandler<ScrollEvent> scrollHandler = this::handleScroll;

        /// The tab to reveal after row geometry is available.
        private @Nullable M3Tab pendingReveal;

        /// Whether the pending reveal should use Material spatial motion.
        private boolean pendingRevealAnimated;

        /// The maximum valid logical scroll offset from the most recent layout.
        private double maximumScrollOffset;

        /// Creates a row for one tab bar.
        ///
        /// @param control the owning tab bar
        private TabRow(M3TabBar control) {
            this.control = control;
            viewportClip.setSmooth(false);
            setClip(viewportClip);
        }

        /// Installs owner listeners after this row has been attached to its skin.
        private void install() {
            control.selectedTabProperty().addListener(selectedTabListener);
            control.tabLayoutProperty().addListener(tabLayoutListener);
            control.getTabs().addListener(childrenListener);
            for (Node child : control.getTabs()) {
                if (child instanceof M3Tab tab) {
                    tab.focusedProperty().addListener(tabFocusListener);
                }
            }
            control.addEventFilter(ScrollEvent.SCROLL, scrollHandler);
            requestReveal(control.getSelectedTab(), false);
        }

        /// Removes owner listeners and releases pending animation state.
        private void dispose() {
            scrollAnimation.stop();
            control.selectedTabProperty().removeListener(selectedTabListener);
            control.tabLayoutProperty().removeListener(tabLayoutListener);
            control.getTabs().removeListener(childrenListener);
            for (Node child : control.getTabs()) {
                if (child instanceof M3Tab tab) {
                    tab.focusedProperty().removeListener(tabFocusListener);
                }
            }
            control.removeEventFilter(ScrollEvent.SCROLL, scrollHandler);
            pendingReveal = null;
            setClip(null);
        }

        /// Installs and removes focus listeners for one public tab-list change.
        private void updateTabFocusListeners(ListChangeListener.Change<? extends Node> change) {
            while (change.next()) {
                for (Node child : change.getRemoved()) {
                    if (child instanceof M3Tab tab) {
                        tab.focusedProperty().removeListener(tabFocusListener);
                    }
                }
                for (Node child : change.getAddedSubList()) {
                    if (child instanceof M3Tab tab) {
                        tab.focusedProperty().addListener(tabFocusListener);
                    }
                }
            }
            requestReveal(control.getSelectedTab(), false);
        }

        /// Computes the minimum row width for the current layout strategy.
        @Override
        protected double computeMinWidth(double height) {
            if (control.getTabLayout() == M3TabBarLayout.FIXED) {
                return computeEqualCellWidth(height, true);
            }

            List<Node> children = getManagedChildren();
            Insets insets = getInsets();
            if (children.isEmpty()) {
                return insets.getLeft() + insets.getRight();
            }
            return insets.getLeft()
                    + SCROLLABLE_LEADING_INSET
                    + childWidth(children.get(0), -1.0, true)
                    + insets.getRight();
        }

        /// Computes the preferred row width for the current layout strategy.
        @Override
        protected double computePrefWidth(double height) {
            if (control.getTabLayout() == M3TabBarLayout.FIXED) {
                return computeEqualCellWidth(height, false);
            }
            return computeScrollableContentWidth();
        }

        /// Caps fixed tabs at their responsive content width while allowing scrollable bars to form a viewport.
        @Override
        protected double computeMaxWidth(double height) {
            return control.getTabLayout() == M3TabBarLayout.FIXED
                    ? computeEqualCellWidth(height, false)
                    : Double.MAX_VALUE;
        }

        /// Lays out managed children according to the current fixed or scrollable strategy.
        @Override
        protected void layoutChildren() {
            viewportClip.setWidth(Math.max(0.0, getWidth()));
            viewportClip.setHeight(Math.max(0.0, getHeight()));
            if (control.getTabLayout() == M3TabBarLayout.SCROLLABLE) {
                layoutScrollableChildren();
            } else {
                layoutFixedChildren();
            }
        }

        /// Lays out managed children in contiguous equal-width cells.
        private void layoutFixedChildren() {
            List<Node> children = getManagedChildren();
            int count = children.size();
            maximumScrollOffset = 0.0;
            if (scrollOffset.get() != 0.0) {
                scrollOffset.set(0.0);
            }
            if (count == 0) {
                return;
            }

            Insets insets = getInsets();
            double left = snapSpaceX(insets.getLeft());
            double top = snapSpaceY(insets.getTop());
            double right = snapSpaceX(insets.getRight());
            double bottom = snapSpaceY(insets.getBottom());
            double spacing = snapSpaceX(getSpacing());
            double availableWidth = Math.max(0.0, getWidth() - left - right - spacing * (count - 1));
            double availableHeight = Math.max(0.0, getHeight() - top - bottom);
            double cellWidth = availableWidth / count;
            double x = left;
            for (int index = 0; index < count; index++) {
                double nextX = index == count - 1 ? getWidth() - right : x + cellWidth;
                layoutChild(children.get(index), x, nextX, top, availableHeight);
                x = nextX + spacing;
            }
        }

        /// Lays out content-width tabs on the clipped logical scroll track.
        private void layoutScrollableChildren() {
            List<Node> children = getManagedChildren();
            Insets insets = getInsets();
            double left = snapSpaceX(insets.getLeft());
            double top = snapSpaceY(insets.getTop());
            double right = snapSpaceX(insets.getRight());
            double bottom = snapSpaceY(insets.getBottom());
            double spacing = snapSpaceX(getSpacing());
            double availableHeight = Math.max(0.0, getHeight() - top - bottom);
            double contentWidth = left + right + SCROLLABLE_LEADING_INSET;
            for (Node child : children) {
                contentWidth += childWidth(child, -1.0, false);
            }
            contentWidth += spacing * Math.max(0, children.size() - 1);
            maximumScrollOffset = Math.max(0.0, contentWidth - getWidth());
            if (scrollOffset.get() > maximumScrollOffset) {
                scrollAnimation.stop();
                scrollOffset.set(maximumScrollOffset);
            }
            revealPendingTab(children, left, right, spacing);

            double x = left + SCROLLABLE_LEADING_INSET - scrollOffset.get();
            for (Node child : children) {
                double nextX = x + childWidth(child, -1.0, false);
                layoutChild(child, x, nextX, top, availableHeight);
                x = nextX + spacing;
            }
        }

        /// Places one child in a horizontal cell without changing application-owned sizing properties.
        private void layoutChild(Node child, double x, double nextX, double top, double availableHeight) {
            double snappedX = snapPositionX(x);
            double snappedNextX = snapPositionX(nextX);
            double layoutWidth = Math.max(0.0, snappedNextX - snappedX);
            if (child.isResizable()) {
                child.resizeRelocate(snappedX, top, layoutWidth, availableHeight);
            } else {
                child.autosize();
                child.relocate(
                        snappedX + (layoutWidth - child.getLayoutBounds().getWidth()) / 2.0,
                        top + (availableHeight - child.getLayoutBounds().getHeight()) / 2.0
                );
            }
        }

        /// Requests that a tab be made fully visible after the next row layout.
        private void requestReveal(@Nullable M3Tab tab, boolean animated) {
            if (tab == null || control.getTabLayout() != M3TabBarLayout.SCROLLABLE || !getChildren().contains(tab)) {
                return;
            }
            pendingReveal = tab;
            pendingRevealAnimated = pendingRevealAnimated || animated;
            requestLayout();
        }

        /// Resolves and applies the pending reveal against current content geometry.
        private void revealPendingTab(
                List<Node> children,
                double left,
                double right,
                double spacing
        ) {
            M3Tab tab = pendingReveal;
            if (tab == null) {
                return;
            }
            pendingReveal = null;
            boolean animated = pendingRevealAnimated;
            pendingRevealAnimated = false;

            double tabStart = left + SCROLLABLE_LEADING_INSET;
            int tabIndex = -1;
            double tabWidth = 0.0;
            for (int index = 0; index < children.size(); index++) {
                Node child = children.get(index);
                double width = childWidth(child, -1.0, false);
                if (child == tab) {
                    tabIndex = index;
                    tabWidth = width;
                    break;
                }
                tabStart += width + spacing;
            }
            if (tabIndex < 0) {
                return;
            }

            double target = scrollOffset.get();
            if (tabIndex == 0) {
                target = 0.0;
            } else if (tabStart - target < left) {
                target = tabStart - left;
            } else {
                double viewportEnd = getWidth() - right;
                if (tabStart + tabWidth - target > viewportEnd) {
                    target = tabStart + tabWidth - viewportEnd;
                }
            }
            animateOrSetOffset(clampToScrollRange(target, maximumScrollOffset), animated);
        }

        /// Applies a direct horizontal scroll event when the track can move in that direction.
        private void handleScroll(ScrollEvent event) {
            if (control.getTabLayout() != M3TabBarLayout.SCROLLABLE || maximumScrollOffset <= 0.0) {
                return;
            }

            double delta;
            if (Math.abs(event.getDeltaX()) > POSITION_EPSILON) {
                delta = M3NodeLayout.isRightToLeft(control) ? event.getDeltaX() : -event.getDeltaX();
            } else if (event.getTextDeltaXUnits() != ScrollEvent.HorizontalTextScrollUnits.NONE
                    && Math.abs(event.getTextDeltaX()) > POSITION_EPSILON) {
                double textDelta = event.getTextDeltaX() * 40.0;
                delta = M3NodeLayout.isRightToLeft(control) ? textDelta : -textDelta;
            } else if (Math.abs(event.getDeltaY()) > POSITION_EPSILON) {
                delta = -event.getDeltaY();
            } else if (event.getTextDeltaYUnits() != ScrollEvent.VerticalTextScrollUnits.NONE
                    && Math.abs(event.getTextDeltaY()) > POSITION_EPSILON) {
                delta = -event.getTextDeltaY()
                        * (event.getTextDeltaYUnits() == ScrollEvent.VerticalTextScrollUnits.PAGES
                        ? getWidth()
                        : 40.0);
            } else {
                return;
            }
            double target = clampToScrollRange(scrollOffset.get() + delta, maximumScrollOffset);
            if (Math.abs(target - scrollOffset.get()) <= POSITION_EPSILON) {
                return;
            }

            pendingReveal = null;
            pendingRevealAnimated = false;
            scrollAnimation.stop();
            scrollOffset.set(target);
            event.consume();
        }

        /// Resets incompatible offset state after the public layout mode changes.
        private void refreshLayoutMode() {
            scrollAnimation.stop();
            maximumScrollOffset = 0.0;
            scrollOffset.set(0.0);
            pendingReveal = null;
            pendingRevealAnimated = false;
            requestReveal(control.getSelectedTab(), false);
            requestLayout();
        }

        /// Animates a programmatic reveal or applies it synchronously when motion is unavailable.
        private void animateOrSetOffset(double target, boolean animated) {
            if (Math.abs(target - scrollOffset.get()) <= POSITION_EPSILON) {
                return;
            }
            if (!animated || control.getScene() == null) {
                scrollAnimation.stop();
                scrollOffset.set(target);
                return;
            }
            scrollAnimation.configure(M3Animation.defaultSpatial(control), target);
            M3Animation.playFromStart(control, scrollAnimation);
        }

        /// Computes a content-derived row width while preserving equal cells.
        private double computeEqualCellWidth(double height, boolean minimum) {
            List<Node> children = getManagedChildren();
            Insets insets = getInsets();
            double availableHeight = contentHeight(height, insets);
            double cellWidth = 0.0;
            for (Node child : children) {
                cellWidth = Math.max(cellWidth, childWidth(child, availableHeight, minimum));
            }
            return insets.getLeft()
                    + insets.getRight()
                    + cellWidth * children.size()
                    + getSpacing() * Math.max(0, children.size() - 1);
        }

        /// Computes the complete width of the unscrolled tab track.
        private double computeScrollableContentWidth() {
            List<Node> children = getManagedChildren();
            Insets insets = getInsets();
            double width = insets.getLeft() + insets.getRight() + SCROLLABLE_LEADING_INSET;
            for (Node child : children) {
                width += childWidth(child, -1.0, false);
            }
            return width + getSpacing() * Math.max(0, children.size() - 1);
        }

        /// Returns available child height after row insets.
        private static double contentHeight(double height, Insets insets) {
            return height < 0.0 ? -1.0 : Math.max(0.0, height - insets.getTop() - insets.getBottom());
        }

        /// Returns one child's bounded minimum or preferred width.
        private double childWidth(Node child, double height, boolean minimum) {
            if (!child.isResizable()) {
                return snapSizeX(child.getLayoutBounds().getWidth());
            }
            double minimumWidth = child.minWidth(height);
            if (minimum) {
                return snapSizeX(minimumWidth);
            }
            return snapSizeX(Math.max(minimumWidth, Math.min(child.prefWidth(height), child.maxWidth(height))));
        }

        /// Constrains a logical offset to its non-negative scroll range.
        private static double clampToScrollRange(double value, double maximum) {
            return Math.max(0.0, Math.min(maximum, value));
        }
    }
}
