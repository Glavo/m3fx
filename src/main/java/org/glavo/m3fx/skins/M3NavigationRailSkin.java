// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NavigationRail].
@NotNullByDefault
public final class M3NavigationRailSkin extends SkinBase<M3NavigationRail> {
    /// The internal vertical item container.
    private final VBox container = new VBox();

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

    /// Animates between collapsed and expanded rail widths.
    private final ChangeListener<Boolean> expandedListener =
            (observable, oldValue, newValue) -> updateExpandedState(newValue, getSkinnable().getScene() != null);

    /// Creates a navigation rail skin.
    ///
    /// @param control the skinned navigation rail
    public M3NavigationRailSkin(M3NavigationRail control) {
        super(control);
        container.setManaged(false);
        container.setAlignment(Pos.TOP_CENTER);
        container.spacingProperty().bind(control.itemSpacingProperty());
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        getChildren().setAll(container);
        control.getItems().addListener(itemsListener);
        control.expandedProperty().addListener(expandedListener);
        updateItems();
        updateExpandedState(control.isExpanded(), false);
    }

    /// Removes listeners, animations, and child references before disposal.
    @Override
    public void dispose() {
        M3NavigationRail control = getSkinnable();
        expansionAnimation.stop();
        motionSettingsObserver.dispose();
        control.getItems().removeListener(itemsListener);
        control.expandedProperty().removeListener(expandedListener);
        container.spacingProperty().unbind();
        container.nodeOrientationProperty().unbind();
        container.getChildren().clear();
        getChildren().remove(container);
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
        return topInset + container.minHeight(width) + bottomInset;
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
        return topInset + container.prefHeight(width) + bottomInset;
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
        return topInset + container.maxHeight(width) + bottomInset;
    }

    /// Lays out the item container across the animated rail width.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
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
            return;
        }

        M3MotionSpec spec = expanded
                ? M3Animation.defaultSpatial(getSkinnable())
                : M3Animation.fastSpatial(getSkinnable());
        expansionAnimation.configure(spec, target);
        M3Animation.playFromStart(getSkinnable(), expansionAnimation);
    }

    /// Mirrors the public item list into the internal container.
    private void updateItems() {
        container.getChildren().setAll(getSkinnable().getItems());
        getSkinnable().requestLayout();
    }
}