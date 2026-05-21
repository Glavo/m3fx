// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Applies M3FX Material scroll styling to JavaFX scroll controls.
@NotNullByDefault
public final class M3ScrollPanes {
    /// The node property key used to store the installed smooth scroll state.
    private static final Object SMOOTH_SCROLL_STATE_KEY = new Object();

    /// The default wheel line distance used when a platform reports text-line scroll units.
    private static final double DEFAULT_LINE_SCROLL_PIXELS = 40.0;

    /// The minimum meaningful scroll value difference.
    private static final double EPSILON = 0.000001;

    /// The style class that enables Material styling for a JavaFX [ScrollPane].
    public static final String STYLE_CLASS = "m3-scroll-pane";

    /// The style class that enables Material styling for a standalone JavaFX [ScrollBar].
    public static final String SCROLL_BAR_STYLE_CLASS = "m3-scroll-bar";

    /// Prevents utility class instantiation.
    private M3ScrollPanes() {
    }

    /// Adds the Material scroll style class to a JavaFX scroll pane.
    public static void style(ScrollPane scrollPane) {
        M3ControlStyles.add(Objects.requireNonNull(scrollPane, "scrollPane"), STYLE_CLASS);
    }

    /// Enables Material smooth wheel scrolling for a JavaFX scroll pane.
    public static void enableSmoothScrolling(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        if (!isSmoothScrollingEnabled(scrollPane)) {
            scrollPane.getProperties().put(SMOOTH_SCROLL_STATE_KEY, new SmoothScrollState(scrollPane));
        }
    }

    /// Disables Material smooth wheel scrolling for a JavaFX scroll pane.
    public static void disableSmoothScrolling(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        @Nullable Object state = scrollPane.getProperties().remove(SMOOTH_SCROLL_STATE_KEY);
        if (state instanceof SmoothScrollState smoothScrollState) {
            smoothScrollState.dispose();
        }
    }

    /// Returns whether Material smooth wheel scrolling is enabled for a JavaFX scroll pane.
    public static boolean isSmoothScrollingEnabled(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        return scrollPane.getProperties().get(SMOOTH_SCROLL_STATE_KEY) instanceof SmoothScrollState;
    }

    /// Adds the Material scroll style class to a standalone JavaFX scroll bar.
    public static void style(ScrollBar scrollBar) {
        M3ControlStyles.add(Objects.requireNonNull(scrollBar, "scrollBar"), SCROLL_BAR_STYLE_CLASS);
    }

    /// Handles smooth wheel scrolling for one JavaFX scroll pane.
    private static final class SmoothScrollState {
        /// The scroll pane receiving smooth wheel behavior.
        private final ScrollPane scrollPane;

        /// The scroll event filter installed on the scroll pane.
        private final EventHandler<ScrollEvent> scrollHandler = this::handleScroll;

        /// The currently running scroll animation.
        private @Nullable Timeline animation;

        /// The accumulated horizontal target value.
        private double targetHValue;

        /// The accumulated vertical target value.
        private double targetVValue;

        /// Creates and installs smooth wheel behavior.
        private SmoothScrollState(ScrollPane scrollPane) {
            this.scrollPane = scrollPane;
            targetHValue = scrollPane.getHvalue();
            targetVValue = scrollPane.getVvalue();
            scrollPane.addEventFilter(ScrollEvent.SCROLL, scrollHandler);
        }

        /// Removes smooth wheel behavior and stops any running animation.
        private void dispose() {
            stopAnimation();
            scrollPane.removeEventFilter(ScrollEvent.SCROLL, scrollHandler);
        }

        /// Handles one wheel or trackpad scroll event.
        private void handleScroll(ScrollEvent event) {
            if (event.isDirect() || !isEventForThisScrollPane(event)) {
                return;
            }

            if (animation == null || animation.getStatus() == Animation.Status.STOPPED) {
                targetHValue = scrollPane.getHvalue();
                targetVValue = scrollPane.getVvalue();
            }

            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            double horizontalDelta = scrollDeltaX(event);
            double verticalDelta = scrollDeltaY(event, viewportHeight);
            if (event.isShiftDown() && Math.abs(horizontalDelta) <= EPSILON) {
                horizontalDelta = verticalDelta;
                verticalDelta = 0.0;
            }

            double nextHValue = targetHValue;
            double nextVValue = targetVValue;
            if (Math.abs(horizontalDelta) > EPSILON) {
                nextHValue = scrollTargetValue(
                        targetHValue,
                        horizontalDelta,
                        scrollPane.getHmin(),
                        scrollPane.getHmax(),
                        contentWidth() - viewportWidth
                );
            }
            if (Math.abs(verticalDelta) > EPSILON) {
                nextVValue = scrollTargetValue(
                        targetVValue,
                        verticalDelta,
                        scrollPane.getVmin(),
                        scrollPane.getVmax(),
                        contentHeight() - viewportHeight
                );
            }

            if (close(nextHValue, targetHValue) && close(nextVValue, targetVValue)) {
                return;
            }

            targetHValue = nextHValue;
            targetVValue = nextVValue;
            animateToTarget();
            event.consume();
        }

        /// Returns whether the event target belongs directly to this scroll pane rather than a nested scroll pane.
        private boolean isEventForThisScrollPane(ScrollEvent event) {
            EventTarget target = event.getTarget();
            if (!(target instanceof Node node)) {
                return true;
            }

            @Nullable Node current = node;
            while (current != null && current != scrollPane) {
                if (current instanceof ScrollPane) {
                    return false;
                }
                current = current.getParent();
            }
            return current == scrollPane;
        }

        /// Starts an animation toward the accumulated target values.
        private void animateToTarget() {
            stopAnimation();
            M3MotionSpec spec = M3Animation.defaultSpatial(scrollPane);
            Timeline timeline = new Timeline(new KeyFrame(
                    spec.duration(),
                    new KeyValue(scrollPane.hvalueProperty(), targetHValue, spec.interpolator()),
                    new KeyValue(scrollPane.vvalueProperty(), targetVValue, spec.interpolator())
            ));
            animation = timeline;
            M3Animation.playFromStart(scrollPane, timeline);
        }

        /// Stops the current scroll animation.
        private void stopAnimation() {
            Timeline currentAnimation = animation;
            if (currentAnimation != null) {
                currentAnimation.stop();
                animation = null;
            }
        }

        /// Returns the current content width.
        private double contentWidth() {
            @Nullable Node content = scrollPane.getContent();
            if (content == null) {
                return 0.0;
            }
            Bounds bounds = content.getBoundsInLocal();
            double width = bounds.getWidth();
            if (content instanceof Region region) {
                width = Math.max(width, region.prefWidth(-1.0));
            }
            return width;
        }

        /// Returns the current content height.
        private double contentHeight() {
            @Nullable Node content = scrollPane.getContent();
            if (content == null) {
                return 0.0;
            }
            Bounds bounds = content.getBoundsInLocal();
            double height = bounds.getHeight();
            if (content instanceof Region region) {
                height = Math.max(height, region.prefHeight(-1.0));
            }
            return height;
        }
    }

    /// Converts an event's horizontal scroll amount to pixels.
    private static double scrollDeltaX(ScrollEvent event) {
        return switch (event.getTextDeltaXUnits()) {
            case CHARACTERS -> event.getTextDeltaX() * DEFAULT_LINE_SCROLL_PIXELS;
            case NONE -> event.getDeltaX();
        };
    }

    /// Converts an event's vertical scroll amount to pixels.
    private static double scrollDeltaY(ScrollEvent event, double viewportHeight) {
        return switch (event.getTextDeltaYUnits()) {
            case LINES -> event.getTextDeltaY() * DEFAULT_LINE_SCROLL_PIXELS;
            case PAGES -> event.getTextDeltaY() * viewportHeight;
            case NONE -> event.getDeltaY();
        };
    }

    /// Computes the target normalized scroll value after applying a pixel delta.
    private static double scrollTargetValue(
            double currentValue,
            double scrollDelta,
            double minValue,
            double maxValue,
            double scrollablePixels
    ) {
        if (scrollablePixels <= EPSILON || close(minValue, maxValue)) {
            return currentValue;
        }

        double currentPixels = pixelsForValue(currentValue, minValue, maxValue, scrollablePixels);
        double targetPixels = clamp(currentPixels - scrollDelta, 0.0, scrollablePixels);
        return valueForPixels(targetPixels, minValue, maxValue, scrollablePixels);
    }

    /// Converts a normalized scroll value to content pixels.
    private static double pixelsForValue(double value, double minValue, double maxValue, double scrollablePixels) {
        double clampedValue = clamp(value, minValue, maxValue);
        return (clampedValue - minValue) / (maxValue - minValue) * scrollablePixels;
    }

    /// Converts content pixels to a normalized scroll value.
    private static double valueForPixels(double pixels, double minValue, double maxValue, double scrollablePixels) {
        return minValue + pixels / scrollablePixels * (maxValue - minValue);
    }

    /// Returns a value clamped into the supplied range.
    private static double clamp(double value, double minValue, double maxValue) {
        if (value <= minValue) {
            return minValue;
        }
        return Math.min(value, maxValue);
    }

    /// Returns whether two scroll values are effectively equal.
    private static boolean close(double first, double second) {
        return Math.abs(first - second) <= EPSILON;
    }
}
