// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Scale;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3PresentationActivity;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/// Renders bounded overscroll as a resistant stretch of the scroll pane content.
///
/// The effect consumes opposite input before logical scrolling so an active stretch is relaxed naturally. Delta
/// left after the bounded scroll is converted to a nonlinear scale capped by [#maximumStretchProperty()]. Releasing
/// the gesture uses the pane's default spatial motion specification and honors inherited reduced-motion settings.
///
/// Rendering is isolated in a dedicated [Scale] appended to the content's transform list. Existing transform and
/// scale properties are not changed. The transform and geometry listeners exist only while a stretch is visible and
/// are removed when the effect settles or is detached.
///
/// If a scroll callback detaches this effect or starts another scroll or release operation on it, the earlier
/// operation still invokes its bounded-scroll callback exactly once but does not apply a remaining pull afterward.
/// Its result retains input consumed before and by that callback. Changes made by content or transform listeners
/// are reconciled before the outermost rendering update returns normally.
@NotNullByDefault
public final class M3StretchOverscrollEffect extends M3OverscrollEffect {
    /// The default maximum scale increase on either axis.
    public static final double DEFAULT_MAXIMUM_STRETCH = 0.10;

    /// The default viewport fraction controlling pull resistance.
    public static final double DEFAULT_RESISTANCE = 0.55;

    /// The minimum meaningful pixel pull.
    private static final double PULL_EPSILON = 0.000001;

    /// The maximum retained raw pull expressed as a multiple of the viewport length.
    private static final double MAXIMUM_RAW_PULL_VIEWPORTS = 4.0;

    /// The maximum scale increase before its writable property is requested.
    private double maximumStretchValue = DEFAULT_MAXIMUM_STRETCH;

    /// The lazily created maximum-stretch property.
    private @Nullable DoubleProperty maximumStretch;

    /// The resistance before its writable property is requested.
    private double resistanceValue = DEFAULT_RESISTANCE;

    /// The lazily created resistance property.
    private @Nullable DoubleProperty resistance;

    /// Whether this effect currently has a pull or release animation.
    private boolean inProgressValue;

    /// The lazily created observable in-progress property.
    private @Nullable ReadOnlyBooleanWrapper inProgress;

    /// The resources owned by the current visible stretch, or `null` while no renderer is installed.
    private @Nullable StretchRenderer renderer;

    /// The release transition created after the first released pull.
    private @Nullable StretchReleaseTransition releaseTransition;

    /// Changes whenever a scroll, release, or attachment lifecycle operation supersedes earlier work.
    private long operationVersion;

    /// Whether the current attachment accepts visual state changes.
    private boolean attached;

    /// The raw horizontal pull retained in input pixels.
    private double horizontalPull;

    /// The raw vertical pull retained in input pixels.
    private double verticalPull;

    /// Creates a stretch overscroll effect with the default maximum and resistance.
    public M3StretchOverscrollEffect() {
    }

    /// Returns the maximum scale increase applied to either axis.
    ///
    /// @return the finite value in the range `(0.0, 0.5]`
    public double getMaximumStretch() {
        @Nullable DoubleProperty property = maximumStretch;
        return property == null ? maximumStretchValue : property.get();
    }

    /// Sets the maximum scale increase applied to either axis.
    ///
    /// For example, `0.1` permits a scale of at most `1.1` on a pulled axis.
    ///
    /// @param value the finite value in the range `(0.0, 0.5]`
    /// @throws IllegalArgumentException if `value` is outside the permitted range
    public void setMaximumStretch(double value) {
        validateMaximumStretch(value);
        @Nullable DoubleProperty property = maximumStretch;
        if (property == null) {
            if (maximumStretchValue != value) {
                maximumStretchValue = value;
                updateTransform();
            }
        } else {
            property.set(value);
        }
    }

    /// Returns the property controlling the maximum scale increase.
    ///
    /// @return the writable maximum-stretch property
    public DoubleProperty maximumStretchProperty() {
        @Nullable DoubleProperty property = maximumStretch;
        if (property == null) {
            property = new DoublePropertyBase(maximumStretchValue) {
                @Override
                public void set(double value) {
                    validateMaximumStretch(value);
                    super.set(value);
                }

                @Override
                protected void invalidated() {
                    validateMaximumStretch(get());
                    maximumStretchValue = get();
                    updateTransform();
                }

                @Override
                public Object getBean() {
                    return M3StretchOverscrollEffect.this;
                }

                @Override
                public String getName() {
                    return "maximumStretch";
                }
            };
            maximumStretch = property;
        }
        return property;
    }

    /// Returns the pull resistance as a fraction of the current viewport length.
    ///
    /// Larger values require more input distance to produce the same visible stretch.
    ///
    /// @return the finite, positive resistance
    public double getResistance() {
        @Nullable DoubleProperty property = resistance;
        return property == null ? resistanceValue : property.get();
    }

    /// Sets the pull resistance as a fraction of the current viewport length.
    ///
    /// @param value the finite, positive resistance
    /// @throws IllegalArgumentException if `value` is not finite and positive
    public void setResistance(double value) {
        validateResistance(value);
        @Nullable DoubleProperty property = resistance;
        if (property == null) {
            if (resistanceValue != value) {
                resistanceValue = value;
                updateTransform();
            }
        } else {
            property.set(value);
        }
    }

    /// Returns the property controlling pull resistance.
    ///
    /// @return the writable resistance property
    public DoubleProperty resistanceProperty() {
        @Nullable DoubleProperty property = resistance;
        if (property == null) {
            property = new DoublePropertyBase(resistanceValue) {
                @Override
                public void set(double value) {
                    validateResistance(value);
                    super.set(value);
                }

                @Override
                protected void invalidated() {
                    validateResistance(get());
                    resistanceValue = get();
                    updateTransform();
                }

                @Override
                public Object getBean() {
                    return M3StretchOverscrollEffect.this;
                }

                @Override
                public String getName() {
                    return "resistance";
                }
            };
            resistance = property;
        }
        return property;
    }

    /// Returns whether this effect has an active pull or release animation.
    ///
    /// @return `true` while the effect is in progress
    @Override
    public boolean isInProgress() {
        @Nullable ReadOnlyBooleanWrapper property = inProgress;
        return property == null ? inProgressValue : property.get();
    }

    /// Returns the read-only in-progress property.
    ///
    /// @return the observable in-progress state
    public ReadOnlyBooleanProperty inProgressProperty() {
        @Nullable ReadOnlyBooleanWrapper property = inProgress;
        if (property == null) {
            property = new ReadOnlyBooleanWrapper(this, "inProgress", inProgressValue);
            inProgress = property;
        }
        return property.getReadOnlyProperty();
    }

    /// Applies pre-scroll relaxation, bounded scrolling, and post-scroll pull on one axis.
    ///
    /// @param orientation   the axis to which the delta applies
    /// @param delta         the finite pixel delta available on that axis
    /// @param event         the event that produced the delta
    /// @param performScroll the bounded logical scroll operation
    /// @return the delta consumed by scrolling and this effect
    @Override
    protected double onApplyToScroll(
            Orientation orientation,
            double delta,
            ScrollEvent event,
            DoubleUnaryOperator performScroll
    ) {
        long version = ++operationVersion;
        if (!attached) {
            return performScroll.applyAsDouble(delta);
        }
        @Nullable StretchReleaseTransition activeRelease = releaseTransition;
        if (activeRelease != null) {
            activeRelease.stop();
        }
        double consumedBeforeScroll = consumeOppositePull(orientation, delta);
        double availableToScroll = delta - consumedBeforeScroll;
        double consumedByScroll = performScroll.applyAsDouble(availableToScroll);
        if (version != operationVersion || !attached) {
            return consumedBeforeScroll + consumedByScroll;
        }
        double remaining = availableToScroll - consumedByScroll;

        if (Math.abs(remaining) > PULL_EPSILON && canRender(orientation)) {
            addPull(orientation, remaining);
            return delta;
        }
        if (Math.abs(consumedByScroll) > PULL_EPSILON && pull(orientation) != 0.0) {
            onRelease();
        } else {
            refreshInProgress();
        }
        return consumedBeforeScroll + consumedByScroll;
    }

    /// Animates both pulled axes to their resting scale.
    @Override
    protected void onRelease() {
        long version = ++operationVersion;
        if (!attached) {
            return;
        }
        if (!hasPull()) {
            @Nullable StretchReleaseTransition activeRelease = releaseTransition;
            if (activeRelease != null) {
                activeRelease.stop();
            }
            finishRelease();
            return;
        }

        M3ScrollPane scrollPane = getScrollPane();
        M3MotionSpec spec = M3Animation.defaultSpatial(scrollPane);
        StretchReleaseTransition transition = releaseTransition();
        transition.configure(spec, horizontalPull, verticalPull);
        setInProgress(true);
        if (version == operationVersion && attached) {
            M3Animation.playFromStart(scrollPane, transition);
        }
    }

    /// Starts a new attachment without retaining rendering resources from a previous owner.
    @Override
    protected void onAttached(M3ScrollPane scrollPane) {
        operationVersion++;
        attached = true;
    }

    /// Resets any retained rendering when this effect is detached.
    ///
    /// @param scrollPane the scroll pane being detached
    @Override
    protected void onDetached(M3ScrollPane scrollPane) {
        operationVersion++;
        attached = false;
        @Nullable StretchReleaseTransition activeRelease = releaseTransition;
        if (activeRelease != null) {
            activeRelease.stop();
        }
        horizontalPull = 0.0;
        verticalPull = 0.0;
        deactivateRenderer();
        setInProgress(false);
    }

    /// Consumes input that opposes the current pull before logical scrolling begins.
    ///
    /// @param orientation the affected axis
    /// @param delta       the available pixel delta
    /// @return the delta consumed while relaxing the pull
    private double consumeOppositePull(Orientation orientation, double delta) {
        double currentPull = pull(orientation);
        if (Math.abs(currentPull) <= PULL_EPSILON
                || Math.abs(delta) <= PULL_EPSILON
                || Math.signum(currentPull) == Math.signum(delta)) {
            return 0.0;
        }

        double consumed = Math.copySign(Math.min(Math.abs(currentPull), Math.abs(delta)), delta);
        setPull(orientation, currentPull + consumed);
        return consumed;
    }

    /// Adds unconsumed input to one axis with a finite raw-pull cap.
    ///
    /// @param orientation the affected axis
    /// @param delta       the unconsumed pixel delta
    private void addPull(Orientation orientation, double delta) {
        double viewportLength = viewportLength(orientation);
        double currentPull = pull(orientation);
        if (currentPull != 0.0 && Math.signum(currentPull) != Math.signum(delta)) {
            currentPull = 0.0;
        }
        double maximumPull = viewportLength * MAXIMUM_RAW_PULL_VIEWPORTS;
        setPull(orientation, clamp(currentPull + delta, -maximumPull, maximumPull));
    }

    /// Returns whether the effect has content and usable viewport geometry on one axis.
    ///
    /// @param orientation the axis to inspect
    /// @return `true` when a stretch can be rendered
    private boolean canRender(Orientation orientation) {
        M3ScrollPane scrollPane = getScrollPane();
        return scrollPane.getContent() != null && viewportLength(orientation) > PULL_EPSILON;
    }

    /// Returns the current viewport length on one axis.
    ///
    /// @param orientation the axis to inspect
    /// @return the non-negative viewport length
    private double viewportLength(Orientation orientation) {
        Bounds viewport = getScrollPane().getViewportBounds();
        return Math.max(0.0, orientation == Orientation.HORIZONTAL ? viewport.getWidth() : viewport.getHeight());
    }

    /// Returns the retained raw pull on one axis.
    ///
    /// @param orientation the axis to inspect
    /// @return the signed pull in pixels
    private double pull(Orientation orientation) {
        return orientation == Orientation.HORIZONTAL ? horizontalPull : verticalPull;
    }

    /// Sets the raw pull on one axis and refreshes rendering.
    ///
    /// @param orientation the affected axis
    /// @param value       the finite signed pull in pixels
    private void setPull(Orientation orientation, double value) {
        double settledValue = Math.abs(value) <= PULL_EPSILON ? 0.0 : value;
        if (orientation == Orientation.HORIZONTAL) {
            horizontalPull = settledValue;
        } else {
            verticalPull = settledValue;
        }
        refreshRenderer();
    }

    /// Sets both raw pulls during release animation.
    ///
    /// @param horizontal the horizontal pull in pixels
    /// @param vertical   the vertical pull in pixels
    private void setPulls(double horizontal, double vertical) {
        horizontalPull = Math.abs(horizontal) <= PULL_EPSILON ? 0.0 : horizontal;
        verticalPull = Math.abs(vertical) <= PULL_EPSILON ? 0.0 : vertical;
        refreshRenderer();
    }

    /// Returns whether either axis retains a meaningful pull.
    private boolean hasPull() {
        return horizontalPull != 0.0 || verticalPull != 0.0;
    }

    /// Activates or removes the transient renderer to match the current pull.
    private void refreshRenderer() {
        if (hasPull() && attached) {
            @Nullable StretchRenderer current = renderer;
            if (current == null) {
                current = new StretchRenderer(getScrollPane());
                renderer = current;
                current.start();
            } else {
                current.refresh();
            }
        } else {
            deactivateRenderer();
        }
        refreshInProgress();
    }

    /// Removes the transient renderer and every active-only geometry listener.
    private void deactivateRenderer() {
        @Nullable StretchRenderer previous = renderer;
        renderer = null;
        if (previous != null) {
            previous.dispose();
        }
    }

    /// Recomputes scale factors and fixed-edge pivots from current geometry.
    private void updateTransform() {
        @Nullable StretchRenderer current = renderer;
        if (current != null) {
            current.refresh();
        }
    }

    /// Converts raw pull pixels to a nonlinear normalized scale increase.
    ///
    /// @param pull           the signed raw pull in pixels
    /// @param viewportLength the viewport length on the same axis
    /// @return the non-negative scale increase
    private double stretchForPull(double pull, double viewportLength) {
        if (Math.abs(pull) <= PULL_EPSILON || viewportLength <= PULL_EPSILON) {
            return 0.0;
        }
        double normalizedDistance = Math.abs(pull) / (viewportLength * getResistance());
        return getMaximumStretch() * -Math.expm1(-normalizedDistance);
    }

    /// Updates the observable progress state from pull and animation state.
    private void refreshInProgress() {
        @Nullable StretchReleaseTransition transition = releaseTransition;
        setInProgress(hasPull() || transition != null && transition.getStatus() != Animation.Status.STOPPED);
    }

    /// Completes a release and removes idle rendering resources.
    private void finishRelease() {
        long version = ++operationVersion;
        horizontalPull = 0.0;
        verticalPull = 0.0;
        deactivateRenderer();
        if (version == operationVersion) {
            setInProgress(false);
        }
    }

    /// Returns the release transition, creating it after the first released pull.
    ///
    /// @return the reusable release transition
    private StretchReleaseTransition releaseTransition() {
        @Nullable StretchReleaseTransition transition = releaseTransition;
        if (transition == null) {
            transition = new StretchReleaseTransition();
            transition.setOnFinished(event -> finishRelease());
            releaseTransition = transition;
        }
        return transition;
    }

    /// Updates the primitive and optional observable in-progress state.
    ///
    /// @param value whether the effect is active
    private void setInProgress(boolean value) {
        inProgressValue = value;
        @Nullable ReadOnlyBooleanWrapper property = inProgress;
        if (property != null) {
            property.set(value);
        }
    }

    /// Validates a maximum-stretch property value.
    ///
    /// @param value the candidate scale increase
    private static void validateMaximumStretch(double value) {
        if (!Double.isFinite(value) || value <= 0.0 || value > 0.5) {
            throw new IllegalArgumentException("maximumStretch must be finite and in the range (0.0, 0.5]");
        }
    }

    /// Validates a resistance property value.
    ///
    /// @param value the candidate viewport fraction
    private static void validateResistance(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException("resistance must be finite and positive");
        }
    }

    /// Returns a value clamped to an inclusive range.
    ///
    /// @param value        the candidate value
    /// @param minimumValue the inclusive lower bound
    /// @param maximumValue the inclusive upper bound
    /// @return the clamped value
    private static double clamp(double value, double minimumValue, double maximumValue) {
        return Math.max(minimumValue, Math.min(maximumValue, value));
    }

    /// Owns the transform and observations for one uninterrupted period of visible stretching.
    @NotNullByDefault
    private final class StretchRenderer {
        /// The pane whose content and viewport supply this renderer's geometry.
        private final M3ScrollPane scrollPane;

        /// The transform owned exclusively by this renderer.
        private final Scale scale = new Scale();

        /// Reconciles content and geometry against their current values after invalidation.
        private final InvalidationListener invalidated = observable -> refresh();

        /// Observes presentation activity until this renderer is disposed.
        private final M3MotionSettingsObserver presentationObserver;

        /// The node carrying the transform, or `null` while no content is installed.
        private @Nullable Node content;

        /// Whether this renderer has permanently released its resources.
        private boolean disposed;

        /// Whether content or transform updates are currently being dispatched.
        private boolean refreshing;

        /// Whether a callback invalidated the current update's content or geometry.
        private boolean refreshRequested;

        /// Creates unpublished rendering resources without registering listeners.
        private StretchRenderer(M3ScrollPane scrollPane) {
            this.scrollPane = scrollPane;
            presentationObserver = new M3MotionSettingsObserver(
                    scrollPane, this::refreshPresentationActivity, false);
        }

        /// Installs observation after this renderer has been published as the active owner.
        private void start() {
            scrollPane.contentProperty().addListener(invalidated);
            scrollPane.viewportBoundsProperty().addListener(invalidated);
            refresh();
            if (!disposed) {
                presentationObserver.start();
            }
        }

        /// Reconciles the latest content and geometry without continuing superseded transform writes.
        private void refresh() {
            if (disposed) {
                return;
            }
            refreshRequested = true;
            if (refreshing) {
                return;
            }
            refreshing = true;
            try {
                while (!disposed && refreshRequested) {
                    refreshRequested = false;
                    @Nullable Node next = scrollPane.getContent();
                    if (content != next) {
                        @Nullable Node previous = content;
                        content = null;
                        if (previous != null) {
                            previous.layoutBoundsProperty().removeListener(invalidated);
                            previous.getTransforms().remove(scale);
                        }
                        if (disposed || refreshRequested) {
                            continue;
                        }
                        content = next;
                        if (next != null) {
                            next.layoutBoundsProperty().addListener(invalidated);
                            if (!next.getTransforms().contains(scale)) {
                                next.getTransforms().add(scale);
                            }
                        }
                        if (disposed || refreshRequested) {
                            continue;
                        }
                    }
                    if (next == null) {
                        continue;
                    }

                    Bounds bounds = next.getLayoutBounds();
                    Bounds viewport = scrollPane.getViewportBounds();
                    double horizontalStretch = stretchForPull(horizontalPull, viewport.getWidth());
                    double verticalStretch = stretchForPull(verticalPull, viewport.getHeight());
                    // Every property write can detach this renderer or request a newer geometry pass.
                    scale.setPivotX(horizontalPull < 0.0 ? bounds.getMaxX() : bounds.getMinX());
                    if (disposed || refreshRequested) {
                        continue;
                    }
                    scale.setPivotY(verticalPull < 0.0 ? bounds.getMaxY() : bounds.getMinY());
                    if (disposed || refreshRequested) {
                        continue;
                    }
                    scale.setX(1.0 + horizontalStretch);
                    if (disposed || refreshRequested) {
                        continue;
                    }
                    scale.setY(1.0 + verticalStretch);
                }
            } finally {
                refreshing = false;
            }
        }

        /// Settles the active pull when this renderer's pane can no longer present it.
        private void refreshPresentationActivity() {
            if (disposed) {
                return;
            }
            @Nullable Scene scene = scrollPane.getScene();
            @Nullable Window window = scene == null ? null : scene.getWindow();
            if (M3PresentationActivity.isTreeVisible(scrollPane)
                    && (window == null || M3PresentationActivity.isRenderActive(window))) {
                return;
            }
            @Nullable StretchReleaseTransition transition = releaseTransition;
            if (transition != null) {
                transition.stop();
            }
            finishRelease();
        }

        /// Retires ownership before removing resources that can invoke application listeners.
        private void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            scrollPane.contentProperty().removeListener(invalidated);
            scrollPane.viewportBoundsProperty().removeListener(invalidated);
            presentationObserver.dispose();
            @Nullable Node previous = content;
            content = null;
            if (previous != null) {
                previous.layoutBoundsProperty().removeListener(invalidated);
                previous.getTransforms().remove(scale);
            }
            scale.setX(1.0);
            scale.setY(1.0);
        }
    }

    /// Reusable two-axis transition that relaxes the retained raw pull.
    @NotNullByDefault
    private final class StretchReleaseTransition extends M3FiniteTransition {
        /// The horizontal pull at the beginning of the current release.
        private double startHorizontalPull;

        /// The vertical pull at the beginning of the current release.
        private double startVerticalPull;

        /// Creates a reusable release transition.
        private StretchReleaseTransition() {
        }

        /// Configures a release from the supplied pull values.
        ///
        /// @param spec       the resolved spatial motion specification
        /// @param horizontal the starting horizontal pull
        /// @param vertical   the starting vertical pull
        private void configure(M3MotionSpec spec, double horizontal, double vertical) {
            Objects.requireNonNull(spec, "spec");
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            startHorizontalPull = horizontal;
            startVerticalPull = vertical;
        }

        /// Interpolates both pulls without permitting a spatial overshoot to cross the resting state.
        ///
        /// @param fraction the eased transition fraction
        @Override
        protected void interpolate(double fraction) {
            double remaining = 1.0 - clamp(fraction, 0.0, 1.0);
            setPulls(startHorizontalPull * remaining, startVerticalPull * remaining);
        }
    }
}
