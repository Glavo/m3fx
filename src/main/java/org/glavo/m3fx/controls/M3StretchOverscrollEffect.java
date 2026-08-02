// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
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

    /// The scale transform created after the first visible pull.
    private @Nullable Scale contentScale;

    /// The release transition created after the first released pull.
    private @Nullable StretchReleaseTransition releaseTransition;

    /// The content listener created after the first visible pull.
    private @Nullable ChangeListener<@Nullable Node> contentListener;

    /// The geometry listener created after the first visible pull.
    private @Nullable InvalidationListener geometryListener;

    /// The presentation observer created after the first visible pull.
    private @Nullable M3MotionSettingsObserver presentationObserver;

    /// The content currently carrying [contentScale], or `null` while none is rendered.
    private @Nullable Node renderedContent;

    /// The raw horizontal pull retained in input pixels.
    private double horizontalPull;

    /// The raw vertical pull retained in input pixels.
    private double verticalPull;

    /// Whether active-only content and viewport listeners are installed.
    private boolean rendererActive;

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
        @Nullable StretchReleaseTransition activeRelease = releaseTransition;
        if (activeRelease != null) {
            activeRelease.stop();
        }
        double consumedBeforeScroll = consumeOppositePull(orientation, delta);
        double availableToScroll = delta - consumedBeforeScroll;
        double consumedByScroll = performScroll.applyAsDouble(availableToScroll);
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
        M3Animation.playFromStart(scrollPane, transition);
    }

    /// Resets any retained rendering when this effect is detached.
    ///
    /// @param scrollPane the scroll pane being detached
    @Override
    protected void onDetached(M3ScrollPane scrollPane) {
        @Nullable StretchReleaseTransition activeRelease = releaseTransition;
        if (activeRelease != null) {
            activeRelease.stop();
        }
        horizontalPull = 0.0;
        verticalPull = 0.0;
        deactivateRenderer();
        @Nullable M3MotionSettingsObserver observer = presentationObserver;
        if (observer != null) {
            observer.dispose();
            presentationObserver = null;
        }
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
        if (hasPull()) {
            activateRenderer();
            updateTransform();
        } else {
            deactivateRenderer();
        }
        refreshInProgress();
    }

    /// Installs active-only listeners and attaches the dedicated content transform.
    private void activateRenderer() {
        if (rendererActive) {
            if (renderedContent == null) {
                setRenderedContent(getScrollPane().getContent());
            }
            return;
        }

        rendererActive = true;
        M3ScrollPane scrollPane = getScrollPane();
        contentScale();
        scrollPane.contentProperty().addListener(contentListener());
        scrollPane.viewportBoundsProperty().addListener(geometryListener());
        setRenderedContent(scrollPane.getContent());
        presentationObserver().start();
    }

    /// Removes the transient renderer and every active-only geometry listener.
    private void deactivateRenderer() {
        if (!rendererActive) {
            return;
        }

        M3ScrollPane scrollPane = getScrollPane();
        ChangeListener<@Nullable Node> activeContentListener =
                Objects.requireNonNull(contentListener, "contentListener");
        InvalidationListener activeGeometryListener =
                Objects.requireNonNull(geometryListener, "geometryListener");
        scrollPane.contentProperty().removeListener(activeContentListener);
        scrollPane.viewportBoundsProperty().removeListener(activeGeometryListener);
        @Nullable M3MotionSettingsObserver observer = presentationObserver;
        if (observer != null) {
            observer.stop();
        }
        setRenderedContent(null);
        Scale scale = Objects.requireNonNull(contentScale, "contentScale");
        scale.setX(1.0);
        scale.setY(1.0);
        rendererActive = false;
    }

    /// Moves the dedicated scale transform to the current content node.
    ///
    /// @param content the content that should render the stretch, or `null`
    private void setRenderedContent(@Nullable Node content) {
        Scale scale = Objects.requireNonNull(contentScale, "contentScale");
        InvalidationListener activeGeometryListener =
                Objects.requireNonNull(geometryListener, "geometryListener");
        @Nullable Node previousContent = renderedContent;
        if (previousContent == content) {
            if (content != null && !content.getTransforms().contains(scale)) {
                content.getTransforms().add(scale);
            }
            updateTransform();
            return;
        }

        if (previousContent != null) {
            previousContent.layoutBoundsProperty().removeListener(activeGeometryListener);
            previousContent.getTransforms().remove(scale);
        }
        renderedContent = content;
        if (content != null) {
            content.layoutBoundsProperty().addListener(activeGeometryListener);
            if (!content.getTransforms().contains(scale)) {
                content.getTransforms().add(scale);
            }
        }
        updateTransform();
    }

    /// Recomputes scale factors and fixed-edge pivots from current geometry.
    private void updateTransform() {
        @Nullable Node content = renderedContent;
        if (!rendererActive || content == null) {
            return;
        }

        Bounds bounds = content.getLayoutBounds();
        double horizontalStretch = stretchForPull(horizontalPull, viewportLength(Orientation.HORIZONTAL));
        double verticalStretch = stretchForPull(verticalPull, viewportLength(Orientation.VERTICAL));
        Scale scale = Objects.requireNonNull(contentScale, "contentScale");
        scale.setPivotX(horizontalPull < 0.0 ? bounds.getMaxX() : bounds.getMinX());
        scale.setPivotY(verticalPull < 0.0 ? bounds.getMaxY() : bounds.getMinY());
        scale.setX(1.0 + horizontalStretch);
        scale.setY(1.0 + verticalStretch);
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
        horizontalPull = 0.0;
        verticalPull = 0.0;
        deactivateRenderer();
        setInProgress(false);
    }

    /// Returns the dedicated content scale, creating it after the first visible pull.
    ///
    /// @return the reusable content scale
    private Scale contentScale() {
        @Nullable Scale scale = contentScale;
        if (scale == null) {
            scale = new Scale();
            contentScale = scale;
        }
        return scale;
    }

    /// Returns the content listener, creating it after the first visible pull.
    ///
    /// @return the reusable content listener
    private ChangeListener<@Nullable Node> contentListener() {
        @Nullable ChangeListener<@Nullable Node> listener = contentListener;
        if (listener == null) {
            listener = (observable, oldContent, newContent) -> setRenderedContent(newContent);
            contentListener = listener;
        }
        return listener;
    }

    /// Returns the geometry listener, creating it after the first visible pull.
    ///
    /// @return the reusable geometry listener
    private InvalidationListener geometryListener() {
        @Nullable InvalidationListener listener = geometryListener;
        if (listener == null) {
            listener = observable -> updateTransform();
            geometryListener = listener;
        }
        return listener;
    }

    /// Returns the presentation observer, creating it after the first visible pull.
    ///
    /// @return the reusable presentation observer
    private M3MotionSettingsObserver presentationObserver() {
        @Nullable M3MotionSettingsObserver observer = presentationObserver;
        if (observer == null) {
            observer = new M3MotionSettingsObserver(getScrollPane(), this::refreshPresentationActivity, false);
            presentationObserver = observer;
        }
        return observer;
    }

    /// Settles a retained pull when its owner can no longer render it.
    private void refreshPresentationActivity() {
        if (!rendererActive) {
            return;
        }
        M3ScrollPane scrollPane = getScrollPane();
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
