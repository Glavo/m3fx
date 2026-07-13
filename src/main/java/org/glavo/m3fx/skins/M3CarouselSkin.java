// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Carousel].
@NotNullByDefault
public final class M3CarouselSkin extends SkinBase<M3Carousel> {
    /// The default maximum preferred viewport width.
    private static final double DEFAULT_MAX_PREF_WIDTH = M3CarouselTrack.DEFAULT_MAX_PREF_WIDTH;

    /// The internal horizontal item track.
    private final M3CarouselTrack track = new M3CarouselTrack(getSkinnable());

    /// The internal viewport used to scroll the item track.
    private final ScrollPane viewport = new ScrollPane(track);

    /// The reusable selected-item scroll transition.
    private final M3DoubleTransition scrollAnimation = new M3DoubleTransition(viewport.hvalueProperty());

    /// Delay after user scrolling before a snapping layout selects its nearest focal item.
    private final PauseTransition scrollSettleDelay = new PauseTransition(Duration.millis(120.0));

    /// Whether the skin is directly assigning the viewport scroll value.
    private boolean settingScrollValue;

    /// Observes viewport movement and schedules snap settling for contained layouts.
    private final ChangeListener<Number> viewportHValueListener = (observable, oldValue, newValue) -> {
        if (settingScrollValue
                || scrollAnimation.getStatus() == Animation.Status.RUNNING
                || !getSkinnable().getCarouselLayout().usesSnapScrolling()) {
            return;
        }
        scrollSettleDelay.playFromStart();
    };

    /// Mirrors public item changes into the internal track.
    private final ListChangeListener<Node> itemsListener = change -> {
        updateItems();
        requestSelectedScroll(false);
    };

    /// Animates focal widths and scrolls the selected item into view after selection changes.
    private final ChangeListener<Number> selectedIndexListener = (observable, oldValue, newValue) -> {
        track.animateSelection(oldValue.intValue(), newValue.intValue());
        requestSelectedScroll(true);
    };

    /// Refreshes geometry when the public layout strategy changes.
    private final ChangeListener<M3CarouselLayout> carouselLayoutListener =
            (observable, oldValue, newValue) -> {
                track.refreshLayoutStrategy();
                requestSelectedScroll(false);
            };

    /// Refreshes physical placement when effective node orientation changes.
    private final ChangeListener<javafx.geometry.NodeOrientation> orientationListener =
            (observable, oldValue, newValue) -> {
                track.requestLayout();
                requestSelectedScroll(false);
            };

    /// Supplies final viewport width to contained arrangement solving.
    private final ChangeListener<Bounds> viewportBoundsListener =
            (observable, oldValue, newValue) -> {
                track.setViewportWidth(newValue.getWidth());
                requestSelectedScroll(false);
            };

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
        getChildren().setAll(viewport);
        control.getItems().addListener(itemsListener);
        control.selectedIndexProperty().addListener(selectedIndexListener);
        control.carouselLayoutProperty().addListener(carouselLayoutListener);
        control.effectiveNodeOrientationProperty().addListener(orientationListener);
        viewport.viewportBoundsProperty().addListener(viewportBoundsListener);
        viewport.hvalueProperty().addListener(viewportHValueListener);
        scrollSettleDelay.setOnFinished(event -> settleToNearestItem());
        updateItems();
    }

    /// Removes listeners, animations, and child references before disposal.
    @Override
    public void dispose() {
        motionSettingsObserver.dispose();
        scrollSettleDelay.stop();
        scrollSettleDelay.setOnFinished(null);
        stopScrollAnimation();
        track.dispose();
        M3ScrollPanes.disableSmoothScrolling(viewport);
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().selectedIndexProperty().removeListener(selectedIndexListener);
        getSkinnable().carouselLayoutProperty().removeListener(carouselLayoutListener);
        getSkinnable().effectiveNodeOrientationProperty().removeListener(orientationListener);
        viewport.viewportBoundsProperty().removeListener(viewportBoundsListener);
        viewport.hvalueProperty().removeListener(viewportHValueListener);
        track.getChildren().clear();
        viewport.setContent(null);
        getChildren().remove(viewport);
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
        track.setViewportWidth(width);
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
        track.refreshItems();
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
        int selectedIndex = getSkinnable().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= track.getChildren().size()) {
            return false;
        }

        double viewportWidth = viewport.getViewportBounds().getWidth();
        if (viewportWidth <= 0.0) {
            return true;
        }

        animateOrSetHValue(track.targetHValue(selectedIndex, viewportWidth), animated);
        return false;
    }

    /// Animates or directly sets the viewport horizontal value.
    private void animateOrSetHValue(double targetHValue, boolean animated) {
        stopScrollAnimation();
        if (!animated || getSkinnable().getScene() == null) {
            settingScrollValue = true;
            try {
                viewport.setHvalue(targetHValue);
            } finally {
                settingScrollValue = false;
            }
            return;
        }

        M3MotionSpec spec = M3Animation.defaultSpatial(getSkinnable());
        scrollAnimation.configure(spec, targetHValue);
        M3Animation.playFromStart(getSkinnable(), scrollAnimation);
    }

    /// Selects and aligns the item nearest the current snapping keyline.
    private void settleToNearestItem() {
        M3Carousel carousel = getSkinnable();
        if (!carousel.getCarouselLayout().usesSnapScrolling()) {
            return;
        }

        double viewportWidth = viewport.getViewportBounds().getWidth();
        if (viewportWidth <= 0.0) {
            return;
        }

        int nearestIndex = -1;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (int index = 0; index < carousel.getItems().size(); index++) {
            Node item = carousel.getItems().get(index);
            if (!item.isVisible() || item.isDisabled() || !item.isManaged()) {
                continue;
            }
            double target = track.targetHValue(index, viewportWidth);
            double distance = Math.abs(target - viewport.getHvalue());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestIndex = index;
            }
        }

        if (nearestIndex < 0) {
            return;
        }
        if (nearestIndex != carousel.getSelectedIndex()) {
            carousel.selectIndex(nearestIndex);
        } else {
            animateOrSetHValue(track.targetHValue(nearestIndex, viewportWidth), true);
        }
    }

    /// Settles a running scroll animation if the carousel now resolves reduced motion.
    private void refreshMotionSettings() {
        track.refreshMotionSettings();
        M3Animation.finishRunningAnimationsIfDisabled(getSkinnable(), scrollAnimation);
        requestSelectedScroll(false);
    }

    /// Stops the current scroll animation.
    private void stopScrollAnimation() {
        scrollAnimation.stop();
    }


}
