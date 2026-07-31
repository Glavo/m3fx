// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.internal.animation.M3DoubleTransition;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CarouselLayout;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3ScrollToEvent;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Carousel].
@NotNullByDefault
public final class M3CarouselSkin extends SkinBase<M3Carousel> {
    /// The internal carousel viewport style class.
    private static final String VIEWPORT_STYLE_CLASS = "m3-carousel-viewport";

    /// The internal carousel track style class.
    private static final String TRACK_STYLE_CLASS = "m3-carousel-track";

    /// The default maximum preferred viewport width.
    private static final double DEFAULT_MAX_PREF_WIDTH = M3CarouselTrack.DEFAULT_MAX_PREF_WIDTH;

    /// The internal item track.
    private final M3CarouselTrack track = new M3CarouselTrack(getSkinnable());

    /// The internal viewport used to scroll the item track.
    private final M3ScrollPane viewport = new M3ScrollPane(track);

    /// The reusable selected-item horizontal scroll transition.
    private final M3DoubleTransition horizontalScrollAnimation =
            new M3DoubleTransition(
                    viewport.hvalueProperty(),
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
                    0.0,
                    1.0
            );

    /// The reusable selected-item vertical scroll transition.
    private final M3DoubleTransition verticalScrollAnimation =
            new M3DoubleTransition(
                    viewport.vvalueProperty(),
                    M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
                    0.0,
                    1.0
            );

    /// Refreshes keyline geometry and active scrolling when inherited reduced-motion settings change.
    private final M3MotionSettingsObserver motionSettingsObserver;

    /// Delay after user scrolling before a snapping layout selects its nearest focal item.
    private final PauseTransition scrollSettleDelay = new PauseTransition(Duration.millis(120.0));

    /// Whether the skin is directly assigning the viewport scroll value.
    private boolean settingScrollValue;

    /// Whether viewport movement currently originates from direct pointer or wheel interaction.
    private boolean viewportInteractionActive;

    /// Whether the latest viewport value still needs to be reflected in the keyline arrangement.
    private boolean viewportTrackingDirty;

    /// Latest normalized viewport value received during direct interaction.
    private double pendingViewportValue;

    /// Coalesces high-frequency viewport changes to one keyline update per JavaFX pulse.
    private final AnimationTimer viewportTrackingTimer = new AnimationTimer() {
        /// Applies the latest interaction position outside the ScrollPane value-listener call stack.
        @Override
        public void handle(long now) {
            applyPendingViewportPosition();
        }
    };

    /// Marks wheel and trackpad gestures as direct viewport interaction before smooth scrolling consumes them.
    private final EventHandler<ScrollEvent> viewportScrollInteractionHandler = event -> {
        beginViewportInteraction();
        if (viewportInteractionActive) {
            scrollSettleDelay.playFromStart();
        }
    };

    /// Marks a panning drag as direct viewport interaction before the ScrollPane skin updates its value.
    private final EventHandler<MouseEvent> viewportDragInteractionHandler = event -> beginViewportInteraction();

    /// Starts settling after a panning gesture releases the pointer.
    private final EventHandler<MouseEvent> viewportReleaseInteractionHandler = event -> {
        if (viewportInteractionActive) {
            scrollSettleDelay.playFromStart();
        }
    };

    /// Handles semantic item-reveal requests from the skinnable control.
    private final EventHandler<M3ScrollToEvent> scrollToRequestHandler = event -> {
        scrollItemIntoView(event.getIndex(), event.isAnimated());
        event.consume();
    };

    /// Cancels direct viewport ownership when the carousel cannot continue receiving gesture events.
    private final InvalidationListener interactionEligibilityInvalidation = observable -> {
        M3Carousel carousel = getSkinnable();
        if (carousel.isDisabled() || carousel.getScene() == null) {
            cancelViewportInteraction();
        }
    };

    /// Observes viewport movement and schedules snap settling for contained layouts.
    private final ChangeListener<Number> viewportValueListener = (observable, oldValue, newValue) -> {
        boolean vertical = getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN;
        if ((vertical && observable != viewport.vvalueProperty())
                || (!vertical && observable != viewport.hvalueProperty())) {
            return;
        }
        if (settingScrollValue
                || !viewportInteractionActive
                || horizontalScrollAnimation.getStatus() == Animation.Status.RUNNING
                || verticalScrollAnimation.getStatus() == Animation.Status.RUNNING
                || !getSkinnable().getCarouselLayout().usesSnapScrolling()) {
            return;
        }
        pendingViewportValue = newValue.doubleValue();
        viewportTrackingDirty = true;
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
                cancelViewportInteraction();
                stopScrollAnimation();
                configureViewportAxis();
                track.refreshLayoutStrategy();
                requestSelectedScroll(false);
            };

    /// Refreshes physical placement when effective node orientation changes.
    private final ChangeListener<javafx.geometry.NodeOrientation> orientationListener =
            (observable, oldValue, newValue) -> {
                track.requestLayout();
                requestSelectedScroll(false);
            };

    /// Supplies final viewport dimensions to arrangement solving and full-screen pagination.
    private final ChangeListener<Bounds> viewportBoundsListener =
            (observable, oldValue, newValue) -> {
                track.setViewportSize(newValue.getWidth(), newValue.getHeight());
                requestSelectedScroll(false);
            };

    /// Whether scrolling should be retried after the next layout pass.
    private boolean pendingItemScroll;

    /// The item index retained for the pending scroll request.
    private int pendingScrollIndex = -1;

    /// Whether the pending scroll request should be animated.
    private boolean pendingItemScrollAnimated;

    /// Creates a carousel skin.
    ///
    /// @param control the carousel controlled by this skin
    public M3CarouselSkin(M3Carousel control) {
        super(control);
        motionSettingsObserver = new M3MotionSettingsObserver(control, this::refreshMotionSettings);
        installViewport();
        getChildren().setAll(viewport);
        control.getItems().addListener(itemsListener);
        control.selectedIndexProperty().addListener(selectedIndexListener);
        control.carouselLayoutProperty().addListener(carouselLayoutListener);
        control.effectiveNodeOrientationProperty().addListener(orientationListener);
        control.disabledProperty().addListener(interactionEligibilityInvalidation);
        control.sceneProperty().addListener(interactionEligibilityInvalidation);
        control.addEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, scrollToRequestHandler);
        viewport.viewportBoundsProperty().addListener(viewportBoundsListener);
        viewport.hvalueProperty().addListener(viewportValueListener);
        viewport.vvalueProperty().addListener(viewportValueListener);
        scrollSettleDelay.setOnFinished(event -> settleToNearestItem());
        updateItems();
    }

    /// Removes listeners, animations, and child references before disposal.
    @Override
    public void dispose() {
        cancelViewportInteraction();
        scrollSettleDelay.stop();
        scrollSettleDelay.setOnFinished(null);
        stopScrollAnimation();
        motionSettingsObserver.dispose();
        track.dispose();
        M3ScrollPanes.disableSmoothScrolling(viewport);
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().selectedIndexProperty().removeListener(selectedIndexListener);
        getSkinnable().carouselLayoutProperty().removeListener(carouselLayoutListener);
        getSkinnable().effectiveNodeOrientationProperty().removeListener(orientationListener);
        getSkinnable().disabledProperty().removeListener(interactionEligibilityInvalidation);
        getSkinnable().sceneProperty().removeListener(interactionEligibilityInvalidation);
        getSkinnable().removeEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, scrollToRequestHandler);
        viewport.viewportBoundsProperty().removeListener(viewportBoundsListener);
        viewport.hvalueProperty().removeListener(viewportValueListener);
        viewport.vvalueProperty().removeListener(viewportValueListener);
        viewport.removeEventFilter(ScrollEvent.SCROLL, viewportScrollInteractionHandler);
        viewport.removeEventFilter(MouseEvent.MOUSE_DRAGGED, viewportDragInteractionHandler);
        viewport.removeEventFilter(MouseEvent.MOUSE_RELEASED, viewportReleaseInteractionHandler);
        viewport.setContent(null);
        getChildren().remove(viewport);
        super.dispose();
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
        if (getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN) {
            return topInset
                    + track.preferredViewportHeight(Math.max(0.0, width - leftInset - rightInset))
                    + bottomInset;
        }
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
        if (getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN) {
            return topInset
                    + track.preferredViewportHeight(Math.max(0.0, width - leftInset - rightInset))
                    + bottomInset;
        }
        return topInset + viewport.prefHeight(width) + bottomInset;
    }

    /// Lays out the viewport in the control bounds and completes any pending selected-item scroll.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        track.setViewportSize(width, height);
        viewport.resizeRelocate(x, y, width, height);
        if (pendingItemScroll) {
            int index = pendingScrollIndex;
            boolean animated = pendingItemScrollAnimated;
            pendingItemScroll = false;
            pendingScrollIndex = -1;
            pendingItemScrollAnimated = false;
            if (deferItemScrollIfNeeded(index, animated)) {
                requestItemScroll(index, animated);
            }
        }
    }

    /// Initializes viewport style classes and scrolling policies.
    private void installViewport() {
        viewport.getStyleClass().add(VIEWPORT_STYLE_CLASS);
        viewport.addEventFilter(ScrollEvent.SCROLL, viewportScrollInteractionHandler);
        viewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, viewportDragInteractionHandler);
        viewport.addEventFilter(MouseEvent.MOUSE_RELEASED, viewportReleaseInteractionHandler);
        track.getStyleClass().add(TRACK_STYLE_CLASS);
        viewport.setManaged(false);
        viewport.setPannable(true);
        viewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewport.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        configureViewportAxis();
    }

    /// Configures the ScrollPane for horizontal keylines or vertical full-screen pages.
    private void configureViewportAxis() {
        boolean vertical = getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN;
        viewport.setFitToWidth(vertical);
        viewport.setFitToHeight(!vertical);
        settingScrollValue = true;
        try {
            if (vertical) {
                viewport.setHvalue(0.0);
            } else {
                viewport.setVvalue(0.0);
            }
        } finally {
            settingScrollValue = false;
        }
    }

    /// Mirrors the public item list into the internal track.
    private void updateItems() {
        cancelViewportInteraction();
        stopScrollAnimation();
        track.setItems(getSkinnable().getItems());
        getSkinnable().requestLayout();
    }

    /// Settles animations and recomputes keylines after the effective motion policy changes.
    private void refreshMotionSettings() {
        cancelViewportInteraction();
        stopScrollAnimation();
        track.refreshLayoutStrategy();
        pendingItemScroll = false;
        pendingScrollIndex = -1;
        pendingItemScrollAnimated = false;
        if (deferItemScrollIfNeeded(getSkinnable().getSelectedIndex(), false)) {
            requestSelectedScroll(false);
        }
    }

    /// Schedules selected item scrolling for the next layout pass.
    private void requestSelectedScroll(boolean animated) {
        requestItemScroll(getSkinnable().getSelectedIndex(), animated);
    }

    /// Schedules one item index for scrolling during the next layout pass.
    private void requestItemScroll(int index, boolean animated) {
        if (index < 0 || index >= getSkinnable().getItems().size()) {
            return;
        }
        if (viewportInteractionActive) {
            return;
        }
        boolean sameRequest = pendingItemScroll && pendingScrollIndex == index;
        pendingItemScroll = true;
        pendingScrollIndex = index;
        pendingItemScrollAnimated = sameRequest ? pendingItemScrollAnimated || animated : animated;
        getSkinnable().requestLayout();
    }

    /// Applies one indexed scroll request immediately or retains it until viewport geometry becomes available.
    private void scrollItemIntoView(int index, boolean animated) {
        if (deferItemScrollIfNeeded(index, animated)) {
            requestItemScroll(index, animated);
        }
    }

    /// Scrolls one item immediately and returns whether geometry requires deferring.
    private boolean deferItemScrollIfNeeded(int index, boolean animated) {
        if (index < 0 || index >= getSkinnable().getItems().size()) {
            return false;
        }

        boolean vertical = getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN;
        double viewportExtent = vertical
                ? viewport.getViewportBounds().getHeight()
                : viewport.getViewportBounds().getWidth();
        if (viewportExtent <= 0.0) {
            return true;
        }

        animateOrSetScrollValue(track.targetScrollValue(index, viewportExtent), animated);
        return false;
    }

    /// Animates or directly sets the viewport value for the active scrolling axis.
    private void animateOrSetScrollValue(double targetValue, boolean animated) {
        boolean vertical = getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN;
        M3DoubleTransition animation = vertical ? verticalScrollAnimation : horizontalScrollAnimation;
        if (vertical) {
            horizontalScrollAnimation.stop();
        } else {
            verticalScrollAnimation.stop();
        }
        if (!animated || getSkinnable().getScene() == null) {
            animation.stop();
            settingScrollValue = true;
            try {
                if (vertical) {
                    viewport.setVvalue(targetValue);
                } else {
                    viewport.setHvalue(targetValue);
                }
            } finally {
                settingScrollValue = false;
            }
            return;
        }

        M3MotionSpec spec = M3Animation.defaultSpatial(getSkinnable());
        animation.configure(spec, targetValue);
        M3Animation.playFromStart(getSkinnable(), animation);
    }

    /// Marks subsequent viewport value changes as direct interaction-driven movement.
    private void beginViewportInteraction() {
        if (!getSkinnable().getCarouselLayout().usesSnapScrolling()) {
            return;
        }
        stopScrollAnimation();
        scrollSettleDelay.stop();
        pendingItemScroll = false;
        pendingScrollIndex = -1;
        pendingItemScrollAnimated = false;
        if (!viewportInteractionActive) {
            viewportInteractionActive = true;
            pendingViewportValue = getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN
                    ? viewport.getVvalue()
                    : viewport.getHvalue();
            viewportTrackingDirty = false;
            viewportTrackingTimer.start();
        }
    }

    /// Applies the latest direct viewport position at most once during the current JavaFX pulse.
    private void applyPendingViewportPosition() {
        if (!viewportInteractionActive || !viewportTrackingDirty) {
            return;
        }
        viewportTrackingDirty = false;
        double viewportExtent = getSkinnable().getCarouselLayout() == M3CarouselLayout.FULL_SCREEN
                ? viewport.getViewportBounds().getHeight()
                : viewport.getViewportBounds().getWidth();
        track.followViewportPosition(pendingViewportValue, viewportExtent);
    }

    /// Stops interaction tracking without selecting or scrolling an item.
    private void cancelViewportInteraction() {
        viewportInteractionActive = false;
        viewportTrackingDirty = false;
        viewportTrackingTimer.stop();
        scrollSettleDelay.stop();
    }

    /// Selects and aligns the item nearest the current snapping keyline.
    private void settleToNearestItem() {
        applyPendingViewportPosition();
        viewportInteractionActive = false;
        viewportTrackingTimer.stop();
        M3Carousel carousel = getSkinnable();
        if (!carousel.getCarouselLayout().usesSnapScrolling()) {
            return;
        }

        boolean vertical = carousel.getCarouselLayout() == M3CarouselLayout.FULL_SCREEN;
        double viewportExtent = vertical
                ? viewport.getViewportBounds().getHeight()
                : viewport.getViewportBounds().getWidth();
        if (viewportExtent <= 0.0) {
            return;
        }

        double viewportValue = vertical ? viewport.getVvalue() : viewport.getHvalue();
        int nearestIndex = track.nearestSelectableIndex(viewportValue, viewportExtent);

        if (nearestIndex < 0) {
            return;
        }
        if (nearestIndex != carousel.getSelectedIndex()) {
            carousel.selectIndex(nearestIndex);
        } else {
            animateOrSetScrollValue(track.targetScrollValue(nearestIndex, viewportExtent), true);
        }
    }

    /// Stops the current scroll animation.
    private void stopScrollAnimation() {
        horizontalScrollAnimation.stop();
        verticalScrollAnimation.stop();
    }

}
