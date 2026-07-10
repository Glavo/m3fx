// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Carousel].
@NotNullByDefault
public final class M3CarouselSkin extends SkinBase<M3Carousel> {
    /// The default maximum preferred viewport width.
    private static final double DEFAULT_MAX_PREF_WIDTH = 480.0;

    /// The internal horizontal item track.
    private final HBox track = new HBox();

    /// The internal viewport used to scroll the item track.
    private final ScrollPane viewport = new ScrollPane(track);

    /// The reusable selected-item scroll transition.
    private final M3DoubleTransition scrollAnimation = new M3DoubleTransition(viewport.hvalueProperty());

    /// Mirrors public item changes into the internal track.
    private final ListChangeListener<Node> itemsListener = change -> {
        updateItems();
        requestSelectedScroll(false);
    };

    /// Scrolls the selected item into view after selection changes.
    private final ChangeListener<Number> selectedIndexListener =
            (observable, oldValue, newValue) -> requestSelectedScroll(true);

    /// Settles running selected-item scroll transitions when runtime motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(
                    getSkinnable(),
                    this::refreshMotionSettings
            );

    /// Whether scrolling should be retried after the next layout pass.
    private boolean pendingSelectedScroll;

    /// Whether the pending scroll request should be animated.
    private boolean pendingSelectedScrollAnimated;

    /// Creates a carousel skin.
    ///
    /// @param control the carousel controlled by this skin
    public M3CarouselSkin(M3Carousel control) {
        super(control);
        installViewport();
        getChildren().add(viewport);
        control.getItems().addListener(itemsListener);
        control.selectedIndexProperty().addListener(selectedIndexListener);
        updateItems();
    }

    /// Removes listeners, animations, and child references before disposal.
    @Override
    public void dispose() {
        motionSettingsObserver.dispose();
        stopScrollAnimation();
        M3ScrollPanes.disableSmoothScrolling(viewport);
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().selectedIndexProperty().removeListener(selectedIndexListener);
        track.getChildren().clear();
        super.dispose();
    }

    /// Scrolls the selected carousel item into the viewport.
    ///
    /// @param animated whether the viewport scroll should animate
    public void scrollSelectedItemIntoView(boolean animated) {
        if (deferSelectedItemScrollIfNeeded(animated)) {
            requestSelectedScroll(animated);
        }
    }

    /// Computes the minimum width from the viewport.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + Math.min(DEFAULT_MAX_PREF_WIDTH, viewport.minWidth(height)) + rightInset;
    }

    /// Computes the minimum height from the viewport.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + viewport.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the item track without letting long carousels expand unbounded.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        double contentWidth = track.prefWidth(height);
        return leftInset + Math.min(Math.max(0.0, contentWidth), DEFAULT_MAX_PREF_WIDTH) + rightInset;
    }

    /// Computes the preferred height from the item track.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + viewport.prefHeight(width) + bottomInset;
    }

    /// Lays out the viewport in the control bounds and completes any pending selected-item scroll.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        viewport.resizeRelocate(x, y, width, height);
        if (pendingSelectedScroll) {
            boolean animated = pendingSelectedScrollAnimated;
            pendingSelectedScroll = false;
            pendingSelectedScrollAnimated = false;
            if (deferSelectedItemScrollIfNeeded(animated)) {
                requestSelectedScroll(animated);
            }
        }
    }

    /// Initializes viewport style classes and scrolling policies.
    private void installViewport() {
        viewport.getStyleClass().add(M3Carousel.VIEWPORT_STYLE_CLASS);
        M3ScrollPanes.style(viewport);
        M3ScrollPanes.enableSmoothScrolling(viewport);
        track.getStyleClass().add(M3Carousel.TRACK_STYLE_CLASS);
        viewport.setManaged(false);
        viewport.setFitToHeight(true);
        viewport.setPannable(true);
        viewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewport.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    /// Mirrors the public item list into the internal track.
    private void updateItems() {
        track.getChildren().setAll(getSkinnable().getItems());
        getSkinnable().requestLayout();
    }

    /// Schedules selected item scrolling for the next layout pass.
    private void requestSelectedScroll(boolean animated) {
        pendingSelectedScroll = true;
        pendingSelectedScrollAnimated = pendingSelectedScrollAnimated || animated;
        getSkinnable().requestLayout();
    }

    /// Scrolls the selected item immediately and returns whether geometry requires deferring.
    private boolean deferSelectedItemScrollIfNeeded(boolean animated) {
        @Nullable Node item = getSkinnable().getSelectedItem();
        if (item == null || !track.getChildren().contains(item)) {
            return false;
        }

        double viewportWidth = viewport.getViewportBounds().getWidth();
        double contentWidth = track.getBoundsInLocal().getWidth();
        if (viewportWidth <= 0.0) {
            return true;
        }
        if (contentWidth <= viewportWidth) {
            animateOrSetHValue(0.0, false);
            return false;
        }

        Bounds itemBounds = item.getBoundsInParent();
        double targetPixel = itemBounds.getMinX() - (viewportWidth - itemBounds.getWidth()) / 2.0;
        double maxPixel = contentWidth - viewportWidth;
        double targetHValue = clamp(targetPixel / maxPixel);
        animateOrSetHValue(targetHValue, animated);
        return false;
    }

    /// Animates or directly sets the viewport horizontal value.
    private void animateOrSetHValue(double targetHValue, boolean animated) {
        stopScrollAnimation();
        if (!animated || getSkinnable().getScene() == null) {
            viewport.setHvalue(targetHValue);
            return;
        }

        M3MotionSpec spec = M3Animation.defaultSpatial(getSkinnable());
        scrollAnimation.configure(spec, targetHValue);
        M3Animation.playFromStart(getSkinnable(), scrollAnimation);
    }

    /// Settles a running scroll animation if the carousel now resolves reduced motion.
    private void refreshMotionSettings() {
        M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), scrollAnimation);
    }

    /// Stops the current scroll animation.
    private void stopScrollAnimation() {
        scrollAnimation.stop();
    }

    /// Clamps a normalized scroll value to the supported range.
    private static double clamp(double value) {
        if (value <= 0.0) {
            return 0.0;
        }
        return Math.min(value, 1.0);
    }
}
