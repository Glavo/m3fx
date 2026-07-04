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
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Applies M3FX Material scroll styling and wheel motion to JavaFX scroll controls.
///
/// This utility class styles standard JavaFX [ScrollPane] and [ScrollBar] instances instead of replacing them,
/// keeping application layout and virtualization behavior compatible with the JavaFX controls. Smooth scrolling
/// can be installed per scroll pane and uses the current M3FX motion behavior.
///
/// See [Material Design scrolling behavior](https://m3.material.io/).
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
    ///
    /// @param scrollPane the scroll pane to style
    public static void style(ScrollPane scrollPane) {
        M3ControlStyles.add(Objects.requireNonNull(scrollPane, "scrollPane"), STYLE_CLASS);
    }

    /// Enables Material smooth wheel scrolling for a JavaFX scroll pane.
    ///
    /// @param scrollPane the scroll pane that should receive smooth wheel scrolling
    public static void enableSmoothScrolling(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        if (!isSmoothScrollingEnabled(scrollPane)) {
            scrollPane.getProperties().put(SMOOTH_SCROLL_STATE_KEY, new SmoothScrollState(scrollPane));
        }
    }

    /// Disables Material smooth wheel scrolling for a JavaFX scroll pane.
    ///
    /// @param scrollPane the scroll pane whose smooth wheel scrolling should be removed
    public static void disableSmoothScrolling(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        @Nullable Object state = scrollPane.getProperties().remove(SMOOTH_SCROLL_STATE_KEY);
        if (state instanceof SmoothScrollState smoothScrollState) {
            smoothScrollState.dispose();
        }
    }

    /// Returns whether Material smooth wheel scrolling is enabled for a JavaFX scroll pane.
    ///
    /// @param scrollPane the scroll pane to inspect
    /// @return `true` if smooth wheel scrolling is installed on the scroll pane
    public static boolean isSmoothScrollingEnabled(ScrollPane scrollPane) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        return scrollPane.getProperties().get(SMOOTH_SCROLL_STATE_KEY) instanceof SmoothScrollState;
    }

    /// Adds the Material scroll style class to a standalone JavaFX scroll bar.
    ///
    /// @param scrollBar the scroll bar to style
    public static void style(ScrollBar scrollBar) {
        M3ControlStyles.add(Objects.requireNonNull(scrollBar, "scrollBar"), SCROLL_BAR_STYLE_CLASS);
    }

    /// Returns whether a scroll event target belongs directly to the supplied scroll pane.
    ///
    /// Nested scroll owners keep their wheel input so that virtualized controls and nested scroll panes can scroll
    /// independently inside a styled outer [ScrollPane].
    ///
    /// @param scrollPane the scroll pane that owns the installed smooth scroll behavior
    /// @param target the original scroll event target
    /// @return `true` if the target belongs to `scrollPane` rather than to a nested scroll owner
    static boolean isEventTargetForScrollPane(ScrollPane scrollPane, EventTarget target) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        if (!(target instanceof Node node)) {
            return true;
        }

        @Nullable Node current = node;
        while (current != null && current != scrollPane) {
            if (isNestedScrollOwner(current)) {
                return false;
            }
            current = current.getParent();
        }
        return current == scrollPane;
    }

    /// Returns whether a node owns its own wheel scrolling inside an outer scroll pane.
    private static boolean isNestedScrollOwner(Node node) {
        return node instanceof ScrollPane
                || node instanceof TextArea
                || node instanceof VirtualFlow<?>
                || node instanceof M3ListView<?>
                || node instanceof ListView<?>
                || node instanceof TreeView<?>
                || node instanceof TableView<?>
                || node instanceof TreeTableView<?>;
    }

    /// Handles smooth wheel scrolling for one JavaFX scroll pane.
    @NotNullByDefault
    private static final class SmoothScrollState {
        /// The scroll pane receiving smooth wheel behavior.
        private final ScrollPane scrollPane;

        /// The scroll event filter installed on the scroll pane.
        private final EventHandler<ScrollEvent> scrollHandler = this::handleScroll;

        /// Updates a running smooth scroll when motion settings change.
        private final M3MotionSettingsObserver motionSettingsObserver;

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
            motionSettingsObserver = new M3MotionSettingsObserver(scrollPane, this::refreshMotionSettings);
        }

        /// Removes smooth wheel behavior and stops any running animation.
        private void dispose() {
            motionSettingsObserver.dispose();
            stopAnimation();
            scrollPane.removeEventFilter(ScrollEvent.SCROLL, scrollHandler);
        }

        /// Handles one wheel or trackpad scroll event.
        private void handleScroll(ScrollEvent event) {
            if (event.isDirect() || !isEventTargetForScrollPane(scrollPane, event.getTarget())) {
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
            double horizontalScrollablePixels = contentWidth() - viewportWidth;
            double verticalScrollablePixels = contentHeight() - viewportHeight;
            boolean canScrollHorizontally = canScroll(
                    scrollPane.getHmin(),
                    scrollPane.getHmax(),
                    horizontalScrollablePixels
            );
            boolean canScrollVertically = canScroll(
                    scrollPane.getVmin(),
                    scrollPane.getVmax(),
                    verticalScrollablePixels
            );
            if (event.isShiftDown() && canScrollHorizontally && Math.abs(horizontalDelta) <= EPSILON) {
                horizontalDelta = verticalDelta;
                verticalDelta = 0.0;
            } else if (!canScrollVertically
                    && canScrollHorizontally
                    && Math.abs(horizontalDelta) <= EPSILON
                    && Math.abs(verticalDelta) > EPSILON) {
                horizontalDelta = verticalDelta;
                verticalDelta = 0.0;
            }

            double nextHValue = targetHValue;
            double nextVValue = targetVValue;
            if (canScrollHorizontally && Math.abs(horizontalDelta) > EPSILON) {
                nextHValue = scrollTargetValue(
                        targetHValue,
                        horizontalDelta,
                        scrollPane.getHmin(),
                        scrollPane.getHmax(),
                        horizontalScrollablePixels
                );
            }
            if (canScrollVertically && Math.abs(verticalDelta) > EPSILON) {
                nextVValue = scrollTargetValue(
                        targetVValue,
                        verticalDelta,
                        scrollPane.getVmin(),
                        scrollPane.getVmax(),
                        verticalScrollablePixels
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

        /// Applies changed animation settings to the current smooth scroll operation.
        private void refreshMotionSettings() {
            Timeline currentAnimation = animation;
            if (currentAnimation == null) {
                return;
            }

            if (!M3Animation.areAnimationsEnabled(scrollPane)) {
                scrollPane.setHvalue(targetHValue);
                scrollPane.setVvalue(targetVValue);
                stopAnimation();
            } else if (currentAnimation.getStatus() == Animation.Status.RUNNING) {
                animateToTarget();
            }
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
            timeline.setOnFinished(event -> animation = null);
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
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double preferredWidth = region.prefWidth(viewportHeight > 0.0 ? viewportHeight : -1.0);
                width = Math.max(width, preferredWidth);
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
                double viewportWidth = scrollPane.getViewportBounds().getWidth();
                double preferredHeight = region.prefHeight(viewportWidth > 0.0 ? viewportWidth : -1.0);
                height = Math.max(height, preferredHeight);
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

    /// Returns whether an axis has a meaningful scroll range.
    private static boolean canScroll(double minValue, double maxValue, double scrollablePixels) {
        return scrollablePixels > EPSILON && !close(minValue, maxValue);
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
