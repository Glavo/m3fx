// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.animation.M3ScalarChannel;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// The default skin for [M3AdaptiveScaffold].
///
/// The skin keeps one stable container for every public scaffold slot. Breakpoint changes alter the visibility and
/// bounds of those containers without detaching application content, preserving control state across adaptive
/// transitions. Horizontal placement uses logical leading and trailing coordinates and therefore mirrors under
/// right-to-left orientation.
@NotNullByDefault
public final class M3AdaptiveScaffoldSkin extends SkinBase<M3AdaptiveScaffold> {
    /// The logical-pixel offset used by pane enter and exit motion.
    private static final double ENTER_EXIT_OFFSET = 32.0;

    /// The threshold used to settle position and size channels.
    private static final double GEOMETRY_VISIBILITY_THRESHOLD = 0.5;

    /// The threshold used to settle slot opacity channels.
    private static final double OPACITY_VISIBILITY_THRESHOLD = 5.0e-4;

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

    /// Motion state for the top-bar slot.
    private final SlotState topBarState = new SlotState(topBarSlot, MotionEdge.TOP);

    /// Motion state for the contextual bottom-bar slot.
    private final SlotState bottomBarState = new SlotState(bottomBarSlot, MotionEdge.BOTTOM);

    /// Motion state for the bottom-navigation slot.
    private final SlotState navigationBarState = new SlotState(navigationBarSlot, MotionEdge.BOTTOM);

    /// Motion state for the logical leading navigation-rail slot.
    private final SlotState navigationRailState = new SlotState(navigationRailSlot, MotionEdge.LEADING);

    /// Motion state for the logical trailing rail slot.
    private final SlotState trailingRailState = new SlotState(trailingRailSlot, MotionEdge.TRAILING);

    /// Motion state for the logical leading pane slot.
    private final SlotState leadingPaneState = new SlotState(leadingPaneSlot, MotionEdge.LEADING);

    /// Motion state for the main pane slot.
    private final SlotState mainPaneState = new SlotState(mainPaneSlot, MotionEdge.NONE);

    /// Motion state for the logical trailing pane slot.
    private final SlotState trailingPaneState = new SlotState(trailingPaneSlot, MotionEdge.TRAILING);

    /// All reusable slot states in layout order.
    private final SlotState @Unmodifiable [] slotStates = {
            topBarState,
            bottomBarState,
            navigationBarState,
            navigationRailState,
            trailingRailState,
            leadingPaneState,
            mainPaneState,
            trailingPaneState
    };

    /// The shared pulse receiver that advances every slot channel.
    private final ScaffoldAnimation layoutAnimation = new ScaffoldAnimation();

    /// Synchronizes public slot properties with their stable containers.
    private final InvalidationListener slotListener = observable -> updateSlotsAndTargetState();

    /// Coalesces derived adaptive-state changes into the next layout pass.
    private final InvalidationListener stateListener = observable -> invalidateAdaptiveState();

    /// Requests layout after a metric or orientation change.
    private final InvalidationListener layoutListener = observable -> getSkinnable().requestLayout();

    /// Prevents slot relocation from being mistaken for a child-originated metric change.
    private boolean applyingRenderedLayout;

    /// Whether stable slots must bridge child layout requests back to the scaffold.
    private boolean slotLayoutPropagationEnabled;

    /// Retargets a running layout transition after its explicit motion specification changes.
    private final InvalidationListener motionSpecListener = observable -> invalidateRunningTransition();

    /// Whether the next layout pass should animate a resolved adaptive-state change.
    private boolean transitionRequested;

    /// Whether at least one complete target layout has established the initial geometry.
    private boolean initialized;

    /// Whether slot geometry is currently between resolved adaptive states.
    private boolean transitionActive;

    /// Prevents duplicate deferred focus-repair requests.
    private boolean focusRepairPending;

    /// Creates a skin for the supplied scaffold.
    ///
    /// @param control the scaffold controlled by this skin
    public M3AdaptiveScaffoldSkin(M3AdaptiveScaffold control) {
        super(control);
        layoutAnimation.setOnFinished(event -> finishLayoutTransition());
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
        control.effectivePaneLayoutProperty().addListener(stateListener);
        control.effectiveNavigationLayoutProperty().addListener(stateListener);
        control.activePaneProperty().addListener(stateListener);
        control.safetyInsetsProperty().addListener(layoutListener);
        control.contentMarginProperty().addListener(layoutListener);
        control.paneSpacingProperty().addListener(layoutListener);
        control.fixedLeadingPaneWidthProperty().addListener(layoutListener);
        control.fixedTrailingPaneWidthProperty().addListener(layoutListener);
        control.effectiveNodeOrientationProperty().addListener(layoutListener);
        control.layoutMotionSpecProperty().addListener(motionSpecListener);

        synchronizeSlots();
        refreshTargetVisibility();
        applySettledVisibility();
        slotLayoutPropagationEnabled = true;
    }

    /// Removes listeners and releases all application nodes before disposal.
    @Override
    public void dispose() {
        M3AdaptiveScaffold control = getSkinnable();
        slotLayoutPropagationEnabled = false;
        removeSlotListeners(control);
        control.effectivePaneLayoutProperty().removeListener(stateListener);
        control.effectiveNavigationLayoutProperty().removeListener(stateListener);
        control.activePaneProperty().removeListener(stateListener);
        control.safetyInsetsProperty().removeListener(layoutListener);
        control.contentMarginProperty().removeListener(layoutListener);
        control.paneSpacingProperty().removeListener(layoutListener);
        control.fixedLeadingPaneWidthProperty().removeListener(layoutListener);
        control.fixedTrailingPaneWidthProperty().removeListener(layoutListener);
        control.effectiveNodeOrientationProperty().removeListener(layoutListener);
        control.layoutMotionSpecProperty().removeListener(motionSpecListener);

        layoutAnimation.stop();
        layoutAnimation.setOnFinished(null);
        for (SlotState state : slotStates) {
            state.dispose();
        }

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
        beginTargetLayout();
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
        completeTargetLayout();
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

    /// Records snapped target bounds for an effective slot.
    private void layoutSlot(StackPane slot, double x, double y, double width, double height) {
        SlotState state = stateFor(slot);
        if (!state.targetVisible) {
            return;
        }
        state.setTargetBounds(
                snapPositionX(x),
                snapPositionY(y),
                snapSizeX(Math.max(0.0, width)),
                snapSizeY(Math.max(0.0, height))
        );
    }

    /// Clears target-assignment markers before one target layout is computed.
    private void beginTargetLayout() {
        for (SlotState state : slotStates) {
            state.beginTargetLayout();
        }
    }

    /// Commits or animates the target geometry computed by the current layout pass.
    private void completeTargetLayout() {
        boolean targetGeometryChanged = false;
        for (SlotState state : slotStates) {
            state.completeTargetLayout();
            targetGeometryChanged |= state.targetChanged;
        }

        if (!initialized) {
            settleTargetLayout();
            initialized = true;
            transitionRequested = false;
            return;
        }

        boolean shouldRetarget = transitionRequested || transitionActive && targetGeometryChanged;
        transitionRequested = false;
        if (shouldRetarget) {
            prepareTransitionTargets();
            if (canAnimateLayout() && hasTransitionDelta()) {
                transitionActive = true;
                layoutAnimation.retarget(resolveLayoutMotionSpec());
            } else {
                settleTargetLayout();
            }
        } else if (!transitionActive) {
            settleTargetLayout();
        }

        applyRenderedLayout();
    }

    /// Establishes enter and exit geometry while preserving already rendered slot bounds.
    private void prepareTransitionTargets() {
        for (SlotState state : slotStates) {
            state.prepareTransitionTarget();
        }
    }

    /// Returns whether at least one slot differs from its prepared target.
    private boolean hasTransitionDelta() {
        for (SlotState state : slotStates) {
            if (state.hasTransitionDelta()) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether this scaffold can currently render animation pulses.
    private boolean canAnimateLayout() {
        Scene scene = getSkinnable().getScene();
        return scene != null
                && scene.getWindow() != null
                && scene.getWindow().isShowing()
                && M3Animation.areAnimationsEnabled(getSkinnable());
    }

    /// Resolves the explicit or theme-derived adaptive-layout motion specification.
    private M3MotionSpec resolveLayoutMotionSpec() {
        @Nullable M3MotionSpec explicit = getSkinnable().getLayoutMotionSpec();
        return explicit == null ? M3Animation.defaultSpatial(getSkinnable()) : explicit;
    }

    /// Applies every target synchronously and removes transient presentation state.
    private void settleTargetLayout() {
        layoutAnimation.stop();
        transitionActive = false;
        for (SlotState state : slotStates) {
            state.settleAtTarget();
        }
        applyRenderedLayout();
    }

    /// Finalizes visibility and exact geometry after the shared transition settles.
    private void finishLayoutTransition() {
        transitionActive = false;
        for (SlotState state : slotStates) {
            state.settleAtTarget();
        }
        applyRenderedLayout();
        getSkinnable().requestLayout();
    }

    /// Applies current animated geometry to all stable slot containers.
    private void applyRenderedLayout() {
        applyingRenderedLayout = true;
        try {
            for (SlotState state : slotStates) {
                state.applyRenderedGeometry();
            }
        } finally {
            applyingRenderedLayout = false;
        }
    }

    /// Returns the reusable motion state associated with one stable slot.
    private SlotState stateFor(StackPane slot) {
        if (slot == topBarSlot) {
            return topBarState;
        }
        if (slot == bottomBarSlot) {
            return bottomBarState;
        }
        if (slot == navigationBarSlot) {
            return navigationBarState;
        }
        if (slot == navigationRailSlot) {
            return navigationRailState;
        }
        if (slot == trailingRailSlot) {
            return trailingRailState;
        }
        if (slot == leadingPaneSlot) {
            return leadingPaneState;
        }
        if (slot == mainPaneSlot) {
            return mainPaneState;
        }
        if (slot == trailingPaneSlot) {
            return trailingPaneState;
        }
        throw new IllegalArgumentException("slot does not belong to this scaffold skin");
    }

    /// Synchronizes every public slot and refreshes its resolved participation state.
    private void updateSlotsAndTargetState() {
        boolean repairFocus = focusInsideChangedSlot();
        synchronizeSlots();
        repairFocus |= refreshTargetVisibility();
        getSkinnable().requestLayout();
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

    /// Marks an adaptive state change for one coalesced transition in the next layout pass.
    private void invalidateAdaptiveState() {
        transitionRequested |= initialized;
        boolean repairFocus = refreshTargetVisibility();
        getSkinnable().requestLayout();
        if (repairFocus) {
            scheduleFocusRepair();
        }
    }

    /// Retargets active geometry when the caller changes the explicit motion specification.
    private void invalidateRunningTransition() {
        if (transitionActive) {
            transitionRequested = true;
            getSkinnable().requestLayout();
        }
    }

    /// Updates resolved slot participation and reports whether focus left an effective region.
    private boolean refreshTargetVisibility() {
        M3AdaptiveScaffold control = getSkinnable();
        boolean repairFocus = false;
        repairFocus |= setTargetVisible(topBarState, control.getTopBar() != null);
        repairFocus |= setTargetVisible(bottomBarState, control.getBottomBar() != null);
        repairFocus |= setTargetVisible(
                navigationBarState,
                control.getEffectiveNavigationLayout() == M3NavigationLayout.BAR
                        && control.getNavigationBar() != null
        );
        repairFocus |= setTargetVisible(
                navigationRailState,
                control.getEffectiveNavigationLayout() == M3NavigationLayout.RAIL
                        && control.getNavigationRail() != null
        );
        repairFocus |= setTargetVisible(trailingRailState, control.getTrailingRail() != null);
        repairFocus |= setTargetVisible(leadingPaneState, control.isPaneVisible(M3PaneRole.LEADING));
        repairFocus |= setTargetVisible(mainPaneState, control.isPaneVisible(M3PaneRole.MAIN));
        repairFocus |= setTargetVisible(trailingPaneState, control.isPaneVisible(M3PaneRole.TRAILING));
        return repairFocus;
    }

    /// Updates one slot's resolved participation and detects focus hidden by the change.
    private boolean setTargetVisible(SlotState state, boolean visible) {
        if (state.targetVisible == visible) {
            return false;
        }
        boolean focused = state.slot.isVisible()
                && !visible
                && M3FocusTraversal.focusOwnerInside(getSkinnable(), state.slot);
        state.targetVisible = visible;
        state.slot.setMouseTransparent(true);
        transitionRequested |= initialized;
        return focused;
    }

    /// Applies resolved visibility before the first target geometry is known.
    private void applySettledVisibility() {
        for (SlotState state : slotStates) {
            state.slot.setVisible(state.targetVisible);
            state.slot.setMouseTransparent(!state.targetVisible);
        }
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
        return stateFor(slot).targetVisible && M3FocusTraversal.focusOwnerInside(getSkinnable(), slot);
    }

    /// Requests focus on the first reachable descendant of one visible slot.
    private boolean requestFirstFocusTarget(StackPane slot) {
        if (!stateFor(slot).targetVisible) {
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
    private StackPane createSlot(String styleClass) {
        return new ScaffoldSlot(styleClass);
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
    private double visibleMinimumWidth(StackPane slot, double height) {
        return stateFor(slot).targetVisible ? Math.max(0.0, slot.minWidth(height)) : 0.0;
    }

    /// Returns a slot's minimum or preferred width when it is visible.
    private double visibleWidth(StackPane slot, boolean minimum) {
        if (!stateFor(slot).targetVisible) {
            return 0.0;
        }
        double width = minimum ? slot.minWidth(-1.0) : slot.prefWidth(-1.0);
        return Math.max(0.0, width);
    }

    /// Returns a pane slot's minimum or preferred width when it is visible.
    private double visiblePaneWidth(StackPane slot, boolean minimum) {
        return visibleWidth(slot, minimum);
    }

    /// Returns a slot's preferred width when it is visible.
    private double visiblePrefWidth(StackPane slot, double height) {
        return stateFor(slot).targetVisible ? Math.max(0.0, slot.prefWidth(height)) : 0.0;
    }

    /// Returns a slot's preferred height when it is visible.
    private double visiblePrefHeight(StackPane slot, double width) {
        return stateFor(slot).targetVisible ? Math.max(0.0, slot.prefHeight(width)) : 0.0;
    }

    /// Returns a slot's minimum or preferred height when it is visible.
    private double visibleHeight(StackPane slot, double width, boolean minimum) {
        if (!stateFor(slot).targetVisible) {
            return 0.0;
        }
        double height = minimum ? slot.minHeight(width) : slot.prefHeight(width);
        return Math.max(0.0, height);
    }

    /// Hosts one public scaffold node while forwarding its layout requests through the unmanaged skin layer.
    private final class ScaffoldSlot extends StackPane {
        /// Creates an unmanaged stable slot with the supplied style class.
        ///
        /// @param styleClass the slot style class
        private ScaffoldSlot(String styleClass) {
            setManaged(false);
            setFocusTraversable(false);
            getStyleClass().add(styleClass);
        }

        /// Requests local layout and notifies the scaffold when child metrics may have changed.
        @Override
        public void requestLayout() {
            super.requestLayout();
            if (slotLayoutPropagationEnabled && !applyingRenderedLayout) {
                getSkinnable().requestLayout();
            }
        }
    }

    /// Identifies the edge used by one slot's enter and exit motion.
    private enum MotionEdge {
        /// No spatial offset; visibility changes use opacity only.
        NONE(0.0, 0.0),

        /// The logical leading edge, mirrored automatically by JavaFX under RTL orientation.
        LEADING(-ENTER_EXIT_OFFSET, 0.0),

        /// The logical trailing edge, mirrored automatically by JavaFX under RTL orientation.
        TRAILING(ENTER_EXIT_OFFSET, 0.0),

        /// The physical top edge.
        TOP(0.0, -ENTER_EXIT_OFFSET),

        /// The physical bottom edge.
        BOTTOM(0.0, ENTER_EXIT_OFFSET);

        /// The horizontal enter and exit offset.
        private final double deltaX;

        /// The vertical enter and exit offset.
        private final double deltaY;

        /// Creates an edge with its local-coordinate offset.
        ///
        /// @param deltaX the horizontal offset
        /// @param deltaY the vertical offset
        MotionEdge(double deltaX, double deltaY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }
    }

    /// Stores the rendered and target geometry for one stable scaffold slot.
    private final class SlotState {
        /// The stable slot container controlled by this state.
        private final StackPane slot;

        /// The edge used by enter and exit motion.
        private final MotionEdge motionEdge;

        /// The reusable clip that contains children while animated bounds change.
        private final Rectangle clip = new Rectangle();

        /// The reusable horizontal-position channel.
        private final M3ScalarChannel xChannel = new M3ScalarChannel(GEOMETRY_VISIBILITY_THRESHOLD);

        /// The reusable vertical-position channel.
        private final M3ScalarChannel yChannel = new M3ScalarChannel(GEOMETRY_VISIBILITY_THRESHOLD);

        /// The reusable width channel.
        private final M3ScalarChannel widthChannel = new M3ScalarChannel(GEOMETRY_VISIBILITY_THRESHOLD);

        /// The reusable height channel.
        private final M3ScalarChannel heightChannel = new M3ScalarChannel(GEOMETRY_VISIBILITY_THRESHOLD);

        /// The reusable opacity channel.
        private final M3ScalarChannel opacityChannel = new M3ScalarChannel(OPACITY_VISIBILITY_THRESHOLD);

        /// Whether this slot participates in the latest resolved adaptive state.
        private boolean targetVisible;

        /// Whether the current target-layout pass assigned effective bounds.
        private boolean targetAssigned;

        /// Whether this pass changed the target geometry.
        private boolean targetChanged;

        /// The currently rendered horizontal position.
        private double currentX;

        /// The currently rendered vertical position.
        private double currentY;

        /// The currently rendered width.
        private double currentWidth;

        /// The currently rendered height.
        private double currentHeight;

        /// The currently rendered opacity.
        private double currentOpacity;

        /// The target horizontal position.
        private double targetX;

        /// The target vertical position.
        private double targetY;

        /// The target width.
        private double targetWidth;

        /// The target height.
        private double targetHeight;

        /// The target opacity.
        private double targetOpacity;

        /// Creates motion state for a stable slot.
        ///
        /// @param slot       the stable slot container
        /// @param motionEdge the edge used by enter and exit motion
        private SlotState(StackPane slot, MotionEdge motionEdge) {
            this.slot = slot;
            this.motionEdge = motionEdge;
            clip.setSmooth(false);
        }

        /// Clears transient markers before a target-layout pass.
        private void beginTargetLayout() {
            targetAssigned = false;
            targetChanged = false;
        }

        /// Records target bounds produced by the scaffold layout algorithm.
        private void setTargetBounds(double x, double y, double width, double height) {
            targetAssigned = true;
            targetChanged |= Double.compare(targetX, x) != 0
                    || Double.compare(targetY, y) != 0
                    || Double.compare(targetWidth, width) != 0
                    || Double.compare(targetHeight, height) != 0;
            targetX = x;
            targetY = y;
            targetWidth = width;
            targetHeight = height;
        }

        /// Supplies a stable fallback when an effective slot receives no explicit bounds.
        private void completeTargetLayout() {
            if (targetVisible && !targetAssigned) {
                setTargetBounds(currentX, currentY, currentWidth, currentHeight);
            }
        }

        /// Prepares visibility geometry without discarding an interrupted rendered value.
        private void prepareTransitionTarget() {
            if (targetVisible) {
                targetOpacity = 1.0;
                if (!slot.isVisible()) {
                    currentX = targetX + motionEdge.deltaX;
                    currentY = targetY + motionEdge.deltaY;
                    currentWidth = targetWidth;
                    currentHeight = targetHeight;
                    currentOpacity = 0.0;
                    slot.setVisible(true);
                }
            } else {
                if (slot.isVisible() && targetOpacity > OPACITY_VISIBILITY_THRESHOLD) {
                    targetX = currentX + motionEdge.deltaX;
                    targetY = currentY + motionEdge.deltaY;
                    targetWidth = currentWidth;
                    targetHeight = currentHeight;
                    targetChanged = true;
                }
                targetOpacity = 0.0;
            }
            slot.setClip(clip);
            slot.setMouseTransparent(!targetVisible || currentOpacity < 1.0 - OPACITY_VISIBILITY_THRESHOLD);
        }

        /// Returns whether any rendered channel differs visibly from its target.
        private boolean hasTransitionDelta() {
            return Math.abs(currentX - targetX) >= GEOMETRY_VISIBILITY_THRESHOLD
                    || Math.abs(currentY - targetY) >= GEOMETRY_VISIBILITY_THRESHOLD
                    || Math.abs(currentWidth - targetWidth) >= GEOMETRY_VISIBILITY_THRESHOLD
                    || Math.abs(currentHeight - targetHeight) >= GEOMETRY_VISIBILITY_THRESHOLD
                    || Math.abs(currentOpacity - targetOpacity) >= OPACITY_VISIBILITY_THRESHOLD;
        }

        /// Configures all channels from their current rendered values.
        private void configure(M3MotionSpec motionSpec, double previousElapsedSeconds) {
            xChannel.configure(currentX, targetX, motionSpec, previousElapsedSeconds);
            yChannel.configure(currentY, targetY, motionSpec, previousElapsedSeconds);
            widthChannel.configure(currentWidth, targetWidth, motionSpec, previousElapsedSeconds);
            heightChannel.configure(currentHeight, targetHeight, motionSpec, previousElapsedSeconds);
            opacityChannel.configure(currentOpacity, targetOpacity, motionSpec, previousElapsedSeconds);
        }

        /// Returns the longest duration among this slot's channels.
        private double getDurationSeconds() {
            return Math.max(
                    Math.max(xChannel.getDurationSeconds(), yChannel.getDurationSeconds()),
                    Math.max(
                            Math.max(widthChannel.getDurationSeconds(), heightChannel.getDurationSeconds()),
                            opacityChannel.getDurationSeconds()
                    )
            );
        }

        /// Advances every channel to one shared elapsed time.
        private void advance(double elapsedSeconds) {
            currentX = xChannel.valueAt(elapsedSeconds);
            currentY = yChannel.valueAt(elapsedSeconds);
            currentWidth = Math.max(0.0, widthChannel.valueAt(elapsedSeconds));
            currentHeight = Math.max(0.0, heightChannel.valueAt(elapsedSeconds));
            currentOpacity = clamp(opacityChannel.valueAt(elapsedSeconds), 0.0, 1.0);
        }

        /// Resets every channel at the exact latest target.
        private void settleAtTarget() {
            currentX = targetX;
            currentY = targetY;
            currentWidth = targetWidth;
            currentHeight = targetHeight;
            currentOpacity = targetVisible ? 1.0 : 0.0;
            targetOpacity = currentOpacity;
            xChannel.reset(currentX);
            yChannel.reset(currentY);
            widthChannel.reset(currentWidth);
            heightChannel.reset(currentHeight);
            opacityChannel.reset(currentOpacity);
            slot.setClip(null);
            slot.setVisible(targetVisible);
            slot.setMouseTransparent(!targetVisible);
            slot.setOpacity(1.0);
            if (!targetVisible) {
                slot.resizeRelocate(0.0, 0.0, 0.0, 0.0);
            }
        }

        /// Applies current geometry, opacity, clipping, and input participation to the slot.
        private void applyRenderedGeometry() {
            if (!slot.isVisible()) {
                return;
            }
            double renderedWidth = snapSizeX(Math.max(0.0, currentWidth));
            double renderedHeight = snapSizeY(Math.max(0.0, currentHeight));
            slot.resizeRelocate(
                    snapPositionX(currentX),
                    snapPositionY(currentY),
                    renderedWidth,
                    renderedHeight
            );
            slot.setOpacity(clamp(currentOpacity, 0.0, 1.0));
            if (slot.getClip() == clip) {
                clip.setWidth(renderedWidth);
                clip.setHeight(renderedHeight);
            }
            slot.setMouseTransparent(
                    !targetVisible
                            || transitionActive && currentOpacity < 1.0 - OPACITY_VISIBILITY_THRESHOLD
            );
        }

        /// Releases transient presentation state owned by this slot.
        private void dispose() {
            slot.setClip(null);
            slot.setOpacity(1.0);
            slot.setMouseTransparent(false);
        }
    }

    /// Shared finite transition that advances every scaffold geometry channel.
    private final class ScaffoldAnimation extends M3FiniteTransition {
        /// The duration of the longest channel in the current run, in seconds.
        private double runDurationSeconds;

        /// Retargets all slot channels while preserving their current physical velocity.
        private void retarget(M3MotionSpec motionSpec) {
            double previousElapsedSeconds = getStatus() == Animation.Status.RUNNING
                    ? Math.max(0.0, getCurrentTime().toSeconds())
                    : Double.POSITIVE_INFINITY;
            for (SlotState state : slotStates) {
                state.configure(motionSpec, previousElapsedSeconds);
            }

            stop();
            runDurationSeconds = 0.0;
            for (SlotState state : slotStates) {
                runDurationSeconds = Math.max(runDurationSeconds, state.getDurationSeconds());
            }
            if (runDurationSeconds <= 0.0) {
                setCycleDuration(Duration.ZERO);
                M3Animation.finish(this);
                return;
            }

            setCycleDuration(Duration.seconds(runDurationSeconds));
            setInterpolator(Interpolator.LINEAR);
            M3Animation.playFromStart(getSkinnable(), this);
        }

        /// Applies one elapsed time to every slot and requests a single scaffold layout.
        @Override
        protected void interpolate(double fraction) {
            double elapsedSeconds = Math.max(0.0, fraction) * runDurationSeconds;
            for (SlotState state : slotStates) {
                state.advance(elapsedSeconds);
            }
            getSkinnable().requestLayout();
        }
    }

    /// Clamps a value to an inclusive range.
    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
