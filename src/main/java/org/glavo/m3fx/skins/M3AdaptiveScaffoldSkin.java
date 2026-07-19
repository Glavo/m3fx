// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.geometry.NodeOrientation;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// The default skin for [M3AdaptiveScaffold].
///
/// The skin keeps one stable container for every public scaffold slot. Breakpoint changes alter the visibility and
/// bounds of those containers without detaching application content, preserving control state across adaptive
/// transitions. Horizontal placement uses logical leading and trailing coordinates and therefore mirrors under
/// right-to-left orientation.
@NotNullByDefault
public final class M3AdaptiveScaffoldSkin extends SkinBase<M3AdaptiveScaffold> {
    /// The stable top-bar container.
    private final StackPane topBarSlot = createSlot("m3-scaffold-top-bar");

    /// The stable contextual bottom-bar container.
    private final StackPane bottomBarSlot = createSlot("m3-scaffold-bottom-bar");

    /// The stable bottom-navigation container.
    private final StackPane navigationBarSlot = createSlot("m3-scaffold-navigation-bar");

    /// The stable logical leading navigation-rail container.
    private final StackPane navigationRailSlot = createSlot("m3-scaffold-navigation-rail");

    /// The stable logical trailing rail container.
    private final StackPane trailingRailSlot = createSlot("m3-scaffold-trailing-rail");

    /// The stable logical leading pane container.
    private final StackPane leadingPaneSlot = createSlot("m3-scaffold-leading-pane");

    /// The stable main pane container.
    private final StackPane mainPaneSlot = createSlot("m3-scaffold-main-pane");

    /// The stable logical trailing pane container.
    private final StackPane trailingPaneSlot = createSlot("m3-scaffold-trailing-pane");

    /// Synchronizes public slot properties with their stable containers.
    private final InvalidationListener slotListener = observable -> updateSlotsAndVisibility();

    /// Updates region participation after adaptive state changes.
    private final InvalidationListener stateListener = observable -> updateVisibility();

    /// Requests layout after a metric or orientation change.
    private final InvalidationListener layoutListener = observable -> getSkinnable().requestLayout();

    /// Prevents duplicate deferred focus-repair requests.
    private boolean focusRepairPending;

    /// Creates a skin for the supplied scaffold.
    ///
    /// @param control the scaffold controlled by this skin
    public M3AdaptiveScaffoldSkin(M3AdaptiveScaffold control) {
        super(control);
        getChildren().setAll(
                leadingPaneSlot,
                mainPaneSlot,
                trailingPaneSlot,
                navigationRailSlot,
                trailingRailSlot,
                bottomBarSlot,
                navigationBarSlot,
                topBarSlot
        );

        addSlotListeners(control);
        control.breakpointProperty().addListener(stateListener);
        control.effectivePaneLayoutProperty().addListener(stateListener);
        control.effectiveNavigationLayoutProperty().addListener(stateListener);
        control.activePaneProperty().addListener(stateListener);
        control.safetyInsetsProperty().addListener(layoutListener);
        control.contentMarginProperty().addListener(layoutListener);
        control.paneSpacingProperty().addListener(layoutListener);
        control.fixedLeadingPaneWidthProperty().addListener(layoutListener);
        control.fixedTrailingPaneWidthProperty().addListener(layoutListener);
        control.effectiveNodeOrientationProperty().addListener(layoutListener);

        synchronizeSlots();
        updateVisibility();
    }

    /// Removes listeners and releases all application nodes before disposal.
    @Override
    public void dispose() {
        M3AdaptiveScaffold control = getSkinnable();
        removeSlotListeners(control);
        control.breakpointProperty().removeListener(stateListener);
        control.effectivePaneLayoutProperty().removeListener(stateListener);
        control.effectiveNavigationLayoutProperty().removeListener(stateListener);
        control.activePaneProperty().removeListener(stateListener);
        control.safetyInsetsProperty().removeListener(layoutListener);
        control.contentMarginProperty().removeListener(layoutListener);
        control.paneSpacingProperty().removeListener(layoutListener);
        control.fixedLeadingPaneWidthProperty().removeListener(layoutListener);
        control.fixedTrailingPaneWidthProperty().removeListener(layoutListener);
        control.effectiveNodeOrientationProperty().removeListener(layoutListener);

        clearSlot(topBarSlot);
        clearSlot(bottomBarSlot);
        clearSlot(navigationBarSlot);
        clearSlot(navigationRailSlot);
        clearSlot(trailingRailSlot);
        clearSlot(leadingPaneSlot);
        clearSlot(mainPaneSlot);
        clearSlot(trailingPaneSlot);
        getChildren().clear();
        super.dispose();
    }

    /// Computes the minimum width of the effective scaffold regions.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3AdaptiveScaffold control = getSkinnable();
        double safety = control.getSafetyInsets().getLeft() + control.getSafetyInsets().getRight();
        double body = bodyWidth(true);
        double bars = Math.max(
                visibleWidth(topBarSlot, true),
                Math.max(visibleWidth(bottomBarSlot, true), visibleWidth(navigationBarSlot, true))
        );
        return leftInset + safety + Math.max(body, bars) + rightInset;
    }

    /// Computes the preferred width of the effective scaffold regions.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        M3AdaptiveScaffold control = getSkinnable();
        double safety = control.getSafetyInsets().getLeft() + control.getSafetyInsets().getRight();
        double body = bodyWidth(false);
        double bars = Math.max(
                visibleWidth(topBarSlot, false),
                Math.max(visibleWidth(bottomBarSlot, false), visibleWidth(navigationBarSlot, false))
        );
        return leftInset + safety + Math.max(body, bars) + rightInset;
    }

    /// Computes the minimum height of the effective scaffold regions.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computeScaffoldHeight(width, topInset, rightInset, bottomInset, leftInset, true);
    }

    /// Computes the preferred height of the effective scaffold regions.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return computeScaffoldHeight(width, topInset, rightInset, bottomInset, leftInset, false);
    }

    /// Lays out bars, rails, and effective panes inside the scaffold.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3AdaptiveScaffold control = getSkinnable();
        boolean rightToLeft = control.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        double logicalStartSafety = rightToLeft
                ? control.getSafetyInsets().getRight()
                : control.getSafetyInsets().getLeft();
        double innerX = x + logicalStartSafety;
        double innerY = y + control.getSafetyInsets().getTop();
        double innerWidth = Math.max(
                0.0,
                width - control.getSafetyInsets().getLeft() - control.getSafetyInsets().getRight()
        );
        double innerHeight = Math.max(
                0.0,
                height - control.getSafetyInsets().getTop() - control.getSafetyInsets().getBottom()
        );

        double topHeight = visiblePrefHeight(topBarSlot, innerWidth);
        double navigationHeight = visiblePrefHeight(navigationBarSlot, innerWidth);
        double bottomHeight = visiblePrefHeight(bottomBarSlot, innerWidth);
        double bodyY = innerY + topHeight;
        double bodyHeight = Math.max(0.0, innerHeight - topHeight - bottomHeight - navigationHeight);

        layoutSlot(topBarSlot, innerX, innerY, innerWidth, topHeight);
        layoutSlot(
                bottomBarSlot,
                innerX,
                bodyY + bodyHeight,
                innerWidth,
                bottomHeight
        );
        layoutSlot(
                navigationBarSlot,
                innerX,
                bodyY + bodyHeight + bottomHeight,
                innerWidth,
                navigationHeight
        );
        layoutBody(innerX, bodyY, innerWidth, bodyHeight);
    }

    /// Computes the minimum or preferred total height.
    private double computeScaffoldHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset,
            boolean minimum
    ) {
        M3AdaptiveScaffold control = getSkinnable();
        double horizontalSafety = control.getSafetyInsets().getLeft() + control.getSafetyInsets().getRight();
        double contentWidth = Math.max(0.0, width - leftInset - rightInset - horizontalSafety);
        double top = visibleHeight(topBarSlot, contentWidth, minimum);
        double bottom = visibleHeight(bottomBarSlot, contentWidth, minimum);
        double navigation = visibleHeight(navigationBarSlot, contentWidth, minimum);
        double body = Math.max(
                visibleHeight(navigationRailSlot, contentWidth, minimum),
                Math.max(
                        visibleHeight(trailingRailSlot, contentWidth, minimum),
                        Math.max(
                                visibleHeight(leadingPaneSlot, contentWidth, minimum),
                                Math.max(
                                        visibleHeight(mainPaneSlot, contentWidth, minimum),
                                        visibleHeight(trailingPaneSlot, contentWidth, minimum)
                                )
                        )
                )
        );
        return topInset
                + control.getSafetyInsets().getTop()
                + top
                + body
                + bottom
                + navigation
                + control.getSafetyInsets().getBottom()
                + bottomInset;
    }

    /// Returns the minimum or preferred width required by the body.
    private double bodyWidth(boolean minimum) {
        M3AdaptiveScaffold control = getSkinnable();
        double railWidth = visibleWidth(navigationRailSlot, minimum) + visibleWidth(trailingRailSlot, minimum);
        double margin = control.getVisiblePaneCount() == 0 ? 0.0 : control.getEffectiveContentMargin() * 2.0;
        double panes = switch (control.getEffectivePaneLayout()) {
            case ADAPTIVE -> 0.0;
            case SINGLE -> visiblePaneWidth(singlePaneSlot(), minimum);
            case SPLIT_LEADING -> pairedPaneWidth(leadingPaneSlot, mainPaneSlot, minimum);
            case SPLIT_TRAILING -> pairedPaneWidth(mainPaneSlot, trailingPaneSlot, minimum);
            case FIXED_LEADING -> fixedPaneWidth(leadingPaneSlot, mainPaneSlot, true, minimum);
            case FIXED_TRAILING -> fixedPaneWidth(mainPaneSlot, trailingPaneSlot, false, minimum);
            case THREE_PANE -> threePaneWidth(minimum);
        };
        return railWidth + margin + panes;
    }

    /// Returns the width required by two visible flexible panes.
    private double pairedPaneWidth(StackPane first, StackPane second, boolean minimum) {
        return visiblePaneWidth(first, minimum)
                + getSkinnable().getEffectivePaneSpacing()
                + visiblePaneWidth(second, minimum);
    }

    /// Returns the width required by one fixed and one flexible pane.
    private double fixedPaneWidth(StackPane first, StackPane second, boolean leadingFixed, boolean minimum) {
        double fixed = leadingFixed
                ? getSkinnable().getEffectiveFixedLeadingPaneWidth()
                : getSkinnable().getEffectiveFixedTrailingPaneWidth();
        StackPane flexible = leadingFixed ? second : first;
        return fixed + getSkinnable().getEffectivePaneSpacing() + visiblePaneWidth(flexible, minimum);
    }

    /// Returns the width required by the three-pane arrangement.
    private double threePaneWidth(boolean minimum) {
        M3AdaptiveScaffold control = getSkinnable();
        return control.getEffectiveFixedLeadingPaneWidth()
                + control.getEffectivePaneSpacing()
                + visiblePaneWidth(mainPaneSlot, minimum)
                + control.getEffectivePaneSpacing()
                + control.getEffectiveFixedTrailingPaneWidth();
    }

    /// Lays out navigation rails and panes in logical horizontal coordinates.
    private void layoutBody(double x, double y, double width, double height) {
        double leadingRailWidth = visiblePrefWidth(navigationRailSlot, height);
        double trailingRailWidth = visiblePrefWidth(trailingRailSlot, height);
        layoutLogical(navigationRailSlot, x, 0.0, y, leadingRailWidth, height);
        layoutLogical(
                trailingRailSlot,
                x,
                Math.max(0.0, width - trailingRailWidth),
                y,
                trailingRailWidth,
                height
        );

        M3AdaptiveScaffold control = getSkinnable();
        double margin = control.getEffectiveContentMargin();
        double paneStart = Math.min(width, leadingRailWidth + margin);
        double paneEnd = Math.max(paneStart, width - trailingRailWidth - margin);
        double paneWidth = Math.max(0.0, paneEnd - paneStart);
        double spacing = Math.min(control.getEffectivePaneSpacing(), paneWidth);

        clearHiddenPaneBounds();
        switch (control.getEffectivePaneLayout()) {
            case ADAPTIVE -> {
            }
            case SINGLE -> layoutLogical(singlePaneSlot(), x, paneStart, y, paneWidth, height);
            case SPLIT_LEADING -> layoutSplit(
                    x,
                    y,
                    width,
                    height,
                    paneStart,
                    paneEnd,
                    spacing,
                    leadingPaneSlot,
                    mainPaneSlot
            );
            case SPLIT_TRAILING -> layoutSplit(
                    x,
                    y,
                    width,
                    height,
                    paneStart,
                    paneEnd,
                    spacing,
                    mainPaneSlot,
                    trailingPaneSlot
            );
            case FIXED_LEADING -> layoutFixed(
                    x,
                    y,
                    height,
                    paneStart,
                    paneEnd,
                    spacing,
                    leadingPaneSlot,
                    mainPaneSlot,
                    control.getEffectiveFixedLeadingPaneWidth(),
                    true
            );
            case FIXED_TRAILING -> layoutFixed(
                    x,
                    y,
                    height,
                    paneStart,
                    paneEnd,
                    spacing,
                    mainPaneSlot,
                    trailingPaneSlot,
                    control.getEffectiveFixedTrailingPaneWidth(),
                    false
            );
            case THREE_PANE -> layoutThreePanes(
                    x,
                    y,
                    height,
                    paneStart,
                    paneEnd,
                    spacing
            );
        }
    }

    /// Lays out two flexible panes around the visual center of the scaffold.
    private void layoutSplit(
            double x,
            double y,
            double width,
            double height,
            double paneStart,
            double paneEnd,
            double spacing,
            StackPane first,
            StackPane second
    ) {
        double available = Math.max(0.0, paneEnd - paneStart - spacing);
        double firstMinimum = Math.min(available, visibleMinimumWidth(first, height));
        double secondMinimum = Math.min(
                Math.max(0.0, available - firstMinimum),
                visibleMinimumWidth(second, height)
        );
        double minimumSplit = paneStart + firstMinimum;
        double maximumSplit = Math.max(minimumSplit, paneEnd - spacing - secondMinimum);
        double centeredSplit = width / 2.0 - spacing / 2.0;
        double split = clamp(centeredSplit, minimumSplit, maximumSplit);

        layoutLogical(first, x, paneStart, y, Math.max(0.0, split - paneStart), height);
        double secondStart = split + spacing;
        layoutLogical(second, x, secondStart, y, Math.max(0.0, paneEnd - secondStart), height);
    }

    /// Lays out a fixed pane adjacent to a flexible pane.
    private void layoutFixed(
            double x,
            double y,
            double height,
            double paneStart,
            double paneEnd,
            double spacing,
            StackPane first,
            StackPane second,
            double requestedFixedWidth,
            boolean firstFixed
    ) {
        double available = Math.max(0.0, paneEnd - paneStart - spacing);
        StackPane flexible = firstFixed ? second : first;
        double flexibleMinimum = Math.min(available, visibleMinimumWidth(flexible, height));
        double fixedWidth = Math.min(requestedFixedWidth, Math.max(0.0, available - flexibleMinimum));
        if (firstFixed) {
            layoutLogical(first, x, paneStart, y, fixedWidth, height);
            double secondStart = paneStart + fixedWidth + spacing;
            layoutLogical(second, x, secondStart, y, Math.max(0.0, paneEnd - secondStart), height);
        } else {
            double secondStart = paneEnd - fixedWidth;
            layoutLogical(first, x, paneStart, y, Math.max(0.0, secondStart - spacing - paneStart), height);
            layoutLogical(second, x, secondStart, y, fixedWidth, height);
        }
    }

    /// Lays out two fixed side panes and one flexible main pane.
    private void layoutThreePanes(
            double x,
            double y,
            double height,
            double paneStart,
            double paneEnd,
            double spacing
    ) {
        M3AdaptiveScaffold control = getSkinnable();
        double paneSpace = Math.max(0.0, paneEnd - paneStart - spacing - spacing);
        double mainMinimum = Math.min(paneSpace, visibleMinimumWidth(mainPaneSlot, height));
        double sideSpace = Math.max(0.0, paneSpace - mainMinimum);
        double leadingWidth = Math.min(control.getEffectiveFixedLeadingPaneWidth(), sideSpace);
        double trailingWidth = Math.min(control.getEffectiveFixedTrailingPaneWidth(), sideSpace);
        double requestedSides = leadingWidth + trailingWidth;
        if (requestedSides > sideSpace && requestedSides > 0.0) {
            double scale = sideSpace / requestedSides;
            leadingWidth *= scale;
            trailingWidth *= scale;
        }
        double mainWidth = Math.max(0.0, paneSpace - leadingWidth - trailingWidth);
        double mainStart = paneStart + leadingWidth + spacing;
        double trailingStart = mainStart + mainWidth + spacing;

        layoutLogical(leadingPaneSlot, x, paneStart, y, leadingWidth, height);
        layoutLogical(mainPaneSlot, x, mainStart, y, mainWidth, height);
        layoutLogical(trailingPaneSlot, x, trailingStart, y, trailingWidth, height);
    }

    /// Lays out a slot in logical horizontal coordinates.
    ///
    /// JavaFX mirrors skin children as part of the control's effective right-to-left orientation. Retaining logical
    /// coordinates here avoids applying that transformation twice.
    private void layoutLogical(
            StackPane slot,
            double x,
            double logicalStart,
            double y,
            double slotWidth,
            double slotHeight
    ) {
        layoutSlot(slot, x + logicalStart, y, slotWidth, slotHeight);
    }

    /// Assigns snapped bounds to a visible slot.
    private void layoutSlot(StackPane slot, double x, double y, double width, double height) {
        if (!slot.isVisible()) {
            return;
        }
        slot.resizeRelocate(
                snapPositionX(x),
                snapPositionY(y),
                snapSizeX(Math.max(0.0, width)),
                snapSizeY(Math.max(0.0, height))
        );
    }

    /// Clears stale bounds from pane slots that are no longer visible.
    private void clearHiddenPaneBounds() {
        clearHiddenBounds(leadingPaneSlot);
        clearHiddenBounds(mainPaneSlot);
        clearHiddenBounds(trailingPaneSlot);
    }

    /// Clears one hidden slot's previous layout bounds.
    private static void clearHiddenBounds(StackPane slot) {
        if (!slot.isVisible()) {
            slot.resizeRelocate(0.0, 0.0, 0.0, 0.0);
        }
    }

    /// Synchronizes every public slot and then refreshes effective visibility.
    private void updateSlotsAndVisibility() {
        boolean repairFocus = focusInsideChangedSlot();
        synchronizeSlots();
        repairFocus |= updateVisibility();
        if (repairFocus) {
            scheduleFocusRepair();
        }
    }

    /// Mirrors the public slot nodes into stable skin containers.
    private void synchronizeSlots() {
        M3AdaptiveScaffold control = getSkinnable();
        synchronizeSlot(topBarSlot, control.getTopBar());
        synchronizeSlot(bottomBarSlot, control.getBottomBar());
        synchronizeSlot(navigationBarSlot, control.getNavigationBar());
        synchronizeSlot(navigationRailSlot, control.getNavigationRail());
        synchronizeSlot(trailingRailSlot, control.getTrailingRail());
        synchronizeSlot(leadingPaneSlot, control.getLeadingPane());
        synchronizeSlot(mainPaneSlot, control.getMainPane());
        synchronizeSlot(trailingPaneSlot, control.getTrailingPane());
    }

    /// Returns whether focus belongs to a slot whose public node is being replaced.
    private boolean focusInsideChangedSlot() {
        M3AdaptiveScaffold control = getSkinnable();
        return slotChangedAndFocused(topBarSlot, control.getTopBar())
                || slotChangedAndFocused(bottomBarSlot, control.getBottomBar())
                || slotChangedAndFocused(navigationBarSlot, control.getNavigationBar())
                || slotChangedAndFocused(navigationRailSlot, control.getNavigationRail())
                || slotChangedAndFocused(trailingRailSlot, control.getTrailingRail())
                || slotChangedAndFocused(leadingPaneSlot, control.getLeadingPane())
                || slotChangedAndFocused(mainPaneSlot, control.getMainPane())
                || slotChangedAndFocused(trailingPaneSlot, control.getTrailingPane());
    }

    /// Returns whether a slot replacement removes the current focus owner.
    private boolean slotChangedAndFocused(StackPane slot, @Nullable Node node) {
        return installedNode(slot) != node && M3FocusTraversal.focusOwnerInside(getSkinnable(), slot);
    }

    /// Updates region visibility and returns whether a focused region became hidden.
    private boolean updateVisibility() {
        M3AdaptiveScaffold control = getSkinnable();
        boolean repairFocus = false;
        repairFocus |= setSlotVisible(topBarSlot, control.getTopBar() != null);
        repairFocus |= setSlotVisible(bottomBarSlot, control.getBottomBar() != null);
        repairFocus |= setSlotVisible(
                navigationBarSlot,
                control.getEffectiveNavigationLayout() == M3NavigationLayout.BAR
                        && control.getNavigationBar() != null
        );
        repairFocus |= setSlotVisible(
                navigationRailSlot,
                control.getEffectiveNavigationLayout() == M3NavigationLayout.RAIL
                        && control.getNavigationRail() != null
        );
        repairFocus |= setSlotVisible(trailingRailSlot, control.getTrailingRail() != null);
        repairFocus |= setSlotVisible(leadingPaneSlot, control.isPaneVisible(M3PaneRole.LEADING));
        repairFocus |= setSlotVisible(mainPaneSlot, control.isPaneVisible(M3PaneRole.MAIN));
        repairFocus |= setSlotVisible(trailingPaneSlot, control.isPaneVisible(M3PaneRole.TRAILING));
        control.requestLayout();
        if (repairFocus) {
            scheduleFocusRepair();
        }
        return repairFocus;
    }

    /// Applies effective visibility and detects focus hidden by the change.
    private boolean setSlotVisible(StackPane slot, boolean visible) {
        boolean focused = slot.isVisible()
                && !visible
                && M3FocusTraversal.focusOwnerInside(getSkinnable(), slot);
        slot.setVisible(visible);
        slot.setMouseTransparent(!visible);
        return focused;
    }

    /// Schedules focus transfer after an adaptive region is hidden or replaced.
    private void scheduleFocusRepair() {
        if (focusRepairPending) {
            return;
        }
        focusRepairPending = true;
        Platform.runLater(() -> {
            focusRepairPending = false;
            M3AdaptiveScaffold control = getSkinnable();
            if (control.getSkin() == this) {
                requestFocusInVisibleRegion();
            }
        });
    }

    /// Requests focus on the first reachable target in the preferred visible region order.
    private void requestFocusInVisibleRegion() {
        M3AdaptiveScaffold control = getSkinnable();
        Scene scene = control.getScene();
        if (scene == null) {
            return;
        }
        Node focusOwner = scene.getFocusOwner();
        if (focusOwner != null) {
            if (!M3FocusTraversal.focusOwnerInside(control, control)) {
                return;
            }
            if (focusInsideVisibleSlot()) {
                return;
            }
        }

        StackPane active = singlePaneSlot();
        if (requestFirstFocusTarget(active)
                || requestFirstFocusTarget(mainPaneSlot)
                || requestFirstFocusTarget(leadingPaneSlot)
                || requestFirstFocusTarget(trailingPaneSlot)
                || requestFirstFocusTarget(navigationRailSlot)
                || requestFirstFocusTarget(navigationBarSlot)
                || requestFirstFocusTarget(topBarSlot)
                || requestFirstFocusTarget(bottomBarSlot)) {
            return;
        }
        requestFirstFocusTarget(trailingRailSlot);
    }

    /// Returns whether the current scene focus owner remains inside an effective slot.
    private boolean focusInsideVisibleSlot() {
        return visibleSlotContainsFocus(topBarSlot)
                || visibleSlotContainsFocus(bottomBarSlot)
                || visibleSlotContainsFocus(navigationBarSlot)
                || visibleSlotContainsFocus(navigationRailSlot)
                || visibleSlotContainsFocus(trailingRailSlot)
                || visibleSlotContainsFocus(leadingPaneSlot)
                || visibleSlotContainsFocus(mainPaneSlot)
                || visibleSlotContainsFocus(trailingPaneSlot);
    }

    /// Returns whether one effective slot contains the current scene focus owner.
    private boolean visibleSlotContainsFocus(StackPane slot) {
        return slot.isVisible() && M3FocusTraversal.focusOwnerInside(getSkinnable(), slot);
    }

    /// Requests focus on the first reachable descendant of one visible slot.
    private static boolean requestFirstFocusTarget(StackPane slot) {
        if (!slot.isVisible()) {
            return false;
        }
        List<Node> targets = M3FocusTraversal.focusTargetsInReachableTree(slot);
        if (targets.isEmpty()) {
            return false;
        }
        targets.get(0).requestFocus();
        return true;
    }

    /// Returns the slot selected by the active pane with the same fallback order as the control.
    private StackPane singlePaneSlot() {
        M3AdaptiveScaffold control = getSkinnable();
        StackPane preferred = switch (control.getActivePane()) {
            case LEADING -> leadingPaneSlot;
            case MAIN -> mainPaneSlot;
            case TRAILING -> trailingPaneSlot;
        };
        if (installedNode(preferred) != null) {
            return preferred;
        }
        if (installedNode(mainPaneSlot) != null) {
            return mainPaneSlot;
        }
        return installedNode(leadingPaneSlot) != null ? leadingPaneSlot : trailingPaneSlot;
    }

    /// Adds listeners to all public slot properties.
    private void addSlotListeners(M3AdaptiveScaffold control) {
        control.topBarProperty().addListener(slotListener);
        control.bottomBarProperty().addListener(slotListener);
        control.navigationBarProperty().addListener(slotListener);
        control.navigationRailProperty().addListener(slotListener);
        control.trailingRailProperty().addListener(slotListener);
        control.leadingPaneProperty().addListener(slotListener);
        control.mainPaneProperty().addListener(slotListener);
        control.trailingPaneProperty().addListener(slotListener);
    }

    /// Removes listeners from all public slot properties.
    private void removeSlotListeners(M3AdaptiveScaffold control) {
        control.topBarProperty().removeListener(slotListener);
        control.bottomBarProperty().removeListener(slotListener);
        control.navigationBarProperty().removeListener(slotListener);
        control.navigationRailProperty().removeListener(slotListener);
        control.trailingRailProperty().removeListener(slotListener);
        control.leadingPaneProperty().removeListener(slotListener);
        control.mainPaneProperty().removeListener(slotListener);
        control.trailingPaneProperty().removeListener(slotListener);
    }

    /// Creates one unmanaged stable slot.
    private static StackPane createSlot(String styleClass) {
        StackPane slot = new StackPane();
        slot.setManaged(false);
        slot.setFocusTraversable(false);
        slot.getStyleClass().add(styleClass);
        return slot;
    }

    /// Installs a node in a stable slot only when the node identity changed.
    private static void synchronizeSlot(StackPane slot, @Nullable Node node) {
        if (installedNode(slot) == node) {
            return;
        }
        if (node == null) {
            slot.getChildren().clear();
        } else {
            slot.getChildren().setAll(node);
        }
    }

    /// Returns the node currently installed in a stable slot.
    private static @Nullable Node installedNode(StackPane slot) {
        return slot.getChildren().isEmpty() ? null : slot.getChildren().get(0);
    }

    /// Removes the node installed in a stable slot.
    private static void clearSlot(StackPane slot) {
        slot.getChildren().clear();
    }

    /// Returns a slot's minimum width when it is visible.
    private static double visibleMinimumWidth(StackPane slot, double height) {
        return slot.isVisible() ? Math.max(0.0, slot.minWidth(height)) : 0.0;
    }

    /// Returns a slot's minimum or preferred width when it is visible.
    private static double visibleWidth(StackPane slot, boolean minimum) {
        if (!slot.isVisible()) {
            return 0.0;
        }
        double width = minimum ? slot.minWidth(-1.0) : slot.prefWidth(-1.0);
        return Math.max(0.0, width);
    }

    /// Returns a pane slot's minimum or preferred width when it is visible.
    private static double visiblePaneWidth(StackPane slot, boolean minimum) {
        return visibleWidth(slot, minimum);
    }

    /// Returns a slot's preferred width when it is visible.
    private static double visiblePrefWidth(StackPane slot, double height) {
        return slot.isVisible() ? Math.max(0.0, slot.prefWidth(height)) : 0.0;
    }

    /// Returns a slot's preferred height when it is visible.
    private static double visiblePrefHeight(StackPane slot, double width) {
        return slot.isVisible() ? Math.max(0.0, slot.prefHeight(width)) : 0.0;
    }

    /// Returns a slot's minimum or preferred height when it is visible.
    private static double visibleHeight(StackPane slot, double width, boolean minimum) {
        if (!slot.isVisible()) {
            return 0.0;
        }
        double height = minimum ? slot.minHeight(width) : slot.prefHeight(width);
        return Math.max(0.0, height);
    }

    /// Clamps a value to an inclusive range.
    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
