// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.animation.M3DelayedScalarChannel;
import org.glavo.m3fx.internal.animation.M3EnterTransitionImpl;
import org.glavo.m3fx.internal.animation.M3ExitTransitionImpl;
import org.glavo.m3fx.internal.animation.M3ScalarChannel;
import org.glavo.m3fx.internal.animation.M3TransitionEffect;
import org.glavo.m3fx.internal.animation.M3TransitionEffectKind;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/// Animates replacement of one JavaFX content node with another.
///
/// The [content property][#contentProperty()] identifies the target content. When it changes, the previous content
/// may remain visible while its exit effects run and the target content performs its enter effects. Only the target
/// content receives input during the transition. Replacing the target while a transition is active retargets the
/// presentation from its current visual state.
///
/// Fade, scale, logical-edge slide, and reveal effects are composed by [M3EnterTransition] and [M3ExitTransition].
/// The effects do not modify an assigned content node's opacity, scale, translation, clip, or transform list. Slide
/// offsets follow this region's [Region#snapToPixelProperty()] so text and other pixel-sensitive content settle on
/// pixel boundaries. [M3SizeTransform] independently controls the animated preferred size and whether drawing is
/// clipped to the animated bounds.
///
/// Callers create and retain content nodes, then assign the desired target through [#setContent(Node)]. A node must
/// be available for use as a child of this region when assigned. Assigning `null` performs an animated removal.
///
/// Enter, exit, and size channels are interruptible. Physical spring specifications retain channel velocity when a
/// target or configuration changes. Reduced motion, presentation detachment or window hiding during a run, and
/// [#snapToCurrentState()] settle synchronously at the target presentation. Animation-control and property mutation
/// methods must be invoked on the JavaFX Application Thread once this node is attached to a showing scene.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3AnimatedContent extends Region {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-animated-content";

    /// The default style class assigned to private content holders.
    private static final String ITEM_STYLE_CLASS = "m3-animated-content-item";

    /// The default alignment of current and outgoing content.
    private static final Pos DEFAULT_ALIGNMENT = Pos.TOP_LEFT;

    /// The visibility threshold used for opacity spring channels.
    private static final double OPACITY_VISIBILITY_THRESHOLD = 1.0e-2;

    /// The visibility threshold used for scale spring channels.
    private static final double SCALE_VISIBILITY_THRESHOLD = 5.0e-4;

    /// The visibility threshold used for translation spring channels, in logical pixels.
    private static final double TRANSLATION_VISIBILITY_THRESHOLD = 1.0e-2;

    /// The visibility threshold used for normalized reveal-edge spring channels.
    private static final double CLIP_VISIBILITY_THRESHOLD = 5.0e-4;

    /// The visibility threshold used for size spring channels, in logical pixels.
    private static final double SIZE_VISIBILITY_THRESHOLD = 5.0e-1;

    /// The private viewport that owns clipping and two permanently attached reusable holders.
    private final Pane viewport = new Pane();

    /// The clip updated to the private viewport bounds during layout.
    private final Rectangle viewportClip = new Rectangle();

    /// The first reusable content holder and its animation channels.
    private final HolderState firstState = new HolderState();

    /// The second reusable content holder and its animation channels.
    private final HolderState secondState = new HolderState();

    /// The single transition that advances both holders and both size channels.
    private final ContentAnimation animation = new ContentAnimation();

    /// The holder containing the target content, or `null` for an empty target.
    private @Nullable HolderState currentState;

    /// The holder containing content that is leaving, or `null` when no content is leaving.
    private @Nullable HolderState outgoingState;

    /// Whether holder invalidations are currently caused by this region's own measurement pass.
    private boolean measuring;

    /// Whether the target content requires a new preferred-size measurement.
    private boolean measurementPending;

    /// The target content width measured independently from the animated container width.
    private double targetContentWidth;

    /// The target content height measured independently from the animated container height.
    private double targetContentHeight;

    /// The content width currently reported to the surrounding layout.
    private double animatedContentWidth;

    /// The content height currently reported to the surrounding layout.
    private double animatedContentHeight;

    /// The width used by the latest fit-to-width target measurement, or `NaN` when unconstrained.
    private double measuredFitWidth = Double.NaN;

    /// The height used by the latest fit-to-height target measurement, or `NaN` when unconstrained.
    private double measuredFitHeight = Double.NaN;

    /// Whether an initial content size has been established.
    private boolean sizeInitialized = true;

    /// The target content node, or `null` when the target state is empty.
    private final ObjectProperty<@Nullable Node> content =
            new SimpleObjectProperty<>(this, "content") {
                /// Starts a replacement transition after the target node changes.
                @Override
                protected void invalidated() {
                    replaceContent(get());
                }
            };

    /// Returns the target content node.
    ///
    /// During a transition the previous content may remain visible until its exit effect completes.
    ///
    /// @return the target content, or `null` for an empty target
    public @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the target content node.
    ///
    /// Replacing a non-null target starts a transition when this region is attached to a scene. Passing `null`
    /// animates removal of the current content. Reassigning the same node has no effect.
    ///
    /// @param content the target content, or `null` for no content
    /// @throws IllegalArgumentException if the node cannot be used as a child of this region
    public void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable target-content property.
    ///
    /// A value assigned through a binding has the same scene-graph constraints as [#setContent(Node)].
    ///
    /// @return the target-content property
    public ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Whether current and incoming content are measured and laid out at this region's assigned width.
    ///
    /// When `false`, the default, this region measures content at its independent preferred width. When `true`, a
    /// positive assigned width constrains current and transitioning content, allowing normal resizable children to
    /// reflow. This is useful when the animated content is hosted by a
    /// width-constraining parent such as [javafx.scene.control.ScrollPane].
    private final BooleanProperty fitToWidth = new SimpleBooleanProperty(this, "fitToWidth", false) {
        /// Re-measures the current target after the holder's width constraint changes.
        @Override
        protected void invalidated() {
            measuredFitWidth = Double.NaN;
            measurementPending = true;
            requestLayout();
        }
    };

    /// Returns whether content is fitted to this region's assigned width.
    ///
    /// @return `true` when current and transitioning content use the assigned width while it is positive
    public boolean isFitToWidth() {
        return fitToWidth.get();
    }

    /// Sets whether content is fitted to this region's assigned width.
    ///
    /// Changing this value requests a target remeasurement. It does not alter the configured enter, exit, or size
    /// transform and may retarget an active size transition when the measured height changes.
    ///
    /// @param fitToWidth whether the assigned width constrains retained content
    public void setFitToWidth(boolean fitToWidth) {
        this.fitToWidth.set(fitToWidth);
    }

    /// Returns the observable fit-to-width property.
    ///
    /// The default value is `false`.
    ///
    /// @return the fit-to-width property
    public BooleanProperty fitToWidthProperty() {
        return fitToWidth;
    }

    /// Whether current and incoming content are measured and laid out at this region's assigned height.
    ///
    /// When `false`, the default, this region measures content at its independent preferred height. When `true`, a
    /// positive assigned height constrains current and transitioning content, allowing full-bleed page roots such as
    /// sidebars in a [javafx.scene.layout.BorderPane] to stretch with the
    /// host. Pair with [#fitToWidthProperty()] when the animated content is the sole child of a stretched shell slot.
    private final BooleanProperty fitToHeight = new SimpleBooleanProperty(this, "fitToHeight", false) {
        /// Re-measures the current target after the holder's height constraint changes.
        @Override
        protected void invalidated() {
            measuredFitHeight = Double.NaN;
            measurementPending = true;
            requestLayout();
        }
    };

    /// Returns whether content is fitted to this region's assigned height.
    ///
    /// @return `true` when current and transitioning content use the assigned height while it is positive
    public boolean isFitToHeight() {
        return fitToHeight.get();
    }

    /// Sets whether content is fitted to this region's assigned height.
    ///
    /// Changing this value requests a target remeasurement. It does not alter the configured enter, exit, or size
    /// transform and may retarget an active size transition when the measured width changes.
    ///
    /// @param fitToHeight whether the assigned height constrains retained content
    public void setFitToHeight(boolean fitToHeight) {
        this.fitToHeight.set(fitToHeight);
    }

    /// Returns the observable fit-to-height property.
    ///
    /// The default value is `false`.
    ///
    /// @return the fit-to-height property
    public BooleanProperty fitToHeightProperty() {
        return fitToHeight;
    }

    /// The alignment shared by current and outgoing content within this region.
    private final ObjectProperty<@Nullable Pos> alignment =
            new SimpleObjectProperty<>(this, "alignment", DEFAULT_ALIGNMENT) {
                /// Restores the default after a direct null assignment and requests layout otherwise.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_ALIGNMENT);
                    } else {
                        requestLayout();
                    }
                }
            };

    /// Returns the alignment of current and outgoing content.
    ///
    /// @return the non-null content alignment
    public Pos getAlignment() {
        return Objects.requireNonNull(alignment.get(), "alignment");
    }

    /// Sets the alignment of current and outgoing content.
    ///
    /// @param alignment the content alignment
    /// @throws NullPointerException if `alignment` is `null`
    public void setAlignment(Pos alignment) {
        this.alignment.set(Objects.requireNonNull(alignment, "alignment"));
    }

    /// Returns the observable content-alignment property.
    ///
    /// The default is [Pos#TOP_LEFT]. A `null` value assigned directly through this property is replaced with that
    /// default.
    ///
    /// @return the content-alignment property
    public ObjectProperty<@Nullable Pos> alignmentProperty() {
        return alignment;
    }

    /// The immutable enter, exit, size, and drawing-order configuration.
    private final ObjectProperty<@Nullable M3ContentTransform> contentTransform =
            new SimpleObjectProperty<>(this, "contentTransform", M3ContentTransform.DEFAULT) {
                /// Restores the default after a direct null assignment and retargets active channels otherwise.
                @Override
                protected void invalidated() {
                    @Nullable M3ContentTransform value = get();
                    if (value == null) {
                        set(M3ContentTransform.DEFAULT);
                        return;
                    }
                    if (value.sizeTransform() == null) {
                        animatedContentWidth = targetContentWidth;
                        animatedContentHeight = targetContentHeight;
                        animation.resetSizeChannels();
                        requestLayout();
                    }
                    updateViewportClip();
                    updateHolderOrder();
                    retargetIfTransitioning();
                }
            };

    /// Returns the transition configuration used for content replacements.
    ///
    /// @return the non-null immutable content transform
    public M3ContentTransform getContentTransform() {
        return Objects.requireNonNull(contentTransform.get(), "contentTransform");
    }

    /// Sets the transition configuration used by subsequent and active replacements.
    ///
    /// Changing this value while a transition is active retargets every affected channel from its currently rendered
    /// value. Spring channels retain velocity after any configured delay has elapsed.
    ///
    /// @param contentTransform the immutable transition configuration
    /// @throws NullPointerException if `contentTransform` is `null`
    public void setContentTransform(M3ContentTransform contentTransform) {
        this.contentTransform.set(Objects.requireNonNull(contentTransform, "contentTransform"));
    }

    /// Returns the observable content-transform property.
    ///
    /// The default is [M3ContentTransform#DEFAULT]. A `null` value assigned directly through the property is replaced
    /// with that default.
    ///
    /// @return the content-transform property
    public ObjectProperty<@Nullable M3ContentTransform> contentTransformProperty() {
        return contentTransform;
    }

    /// Whether an enter, exit, or size channel is moving toward its target.
    private final ReadOnlyBooleanWrapper transitioning =
            new ReadOnlyBooleanWrapper(this, "transitioning", false);

    /// Returns whether a content or size transition is active.
    ///
    /// @return `true` until all active effects have reached their target values
    public boolean isTransitioning() {
        return transitioning.get();
    }

    /// Returns the read-only transition-state property.
    ///
    /// @return the transition-state property
    public ReadOnlyBooleanProperty transitioningProperty() {
        return transitioning.getReadOnlyProperty();
    }

    /// Creates an empty animated-content region.
    public M3AnimatedContent() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        setPickOnBounds(false);

        viewport.setPickOnBounds(false);
        viewportClip.setSmooth(false);
        viewport.getChildren().setAll(firstState.holder, secondState.holder);
        getChildren().add(viewport);
        updateViewportClip();

        animation.setOnFinished(event -> completeTransition());
    }

    /// Creates an animated-content region with initial content.
    ///
    /// Initial content is installed synchronously without an enter transition.
    ///
    /// @param content the initial content
    /// @throws NullPointerException     if `content` is `null`
    /// @throws IllegalArgumentException if the node cannot be used as a child of this region
    public M3AnimatedContent(Node content) {
        this();
        setContent(Objects.requireNonNull(content, "content"));
    }

    /// Completes the active transition at its target state.
    ///
    /// This method has no effect when no transition is active. Completion is synchronous; when the method returns,
    /// only the target content remains visible.
    public void finish() {
        if (animation.getStatus() == Animation.Status.RUNNING) {
            M3Animation.finish(animation);
        } else if (isTransitioning()) {
            snapToCurrentState();
        }
    }

    /// Immediately applies the current target content and target size.
    ///
    /// Any active transition is stopped and the target presentation is applied at its measured size. Previous
    /// content is no longer visible. Repeated calls are idempotent.
    public void snapToCurrentState() {
        animation.stop();
        clearOutgoingState();

        @Nullable HolderState current = currentState;
        if (current != null) {
            current.resetVisuals(1.0, 1.0, 0.0, 0.0);
        }

        measureTargetContent();
        animatedContentWidth = targetContentWidth;
        animatedContentHeight = targetContentHeight;
        sizeInitialized = true;
        animation.resetSizeChannels();
        updateHolderOrder();
        transitioning.set(false);
        requestLayout();
    }

    /// Returns the baseline offset of the target content at its current alignment.
    ///
    /// @return the aligned target-content baseline, or [Node#BASELINE_OFFSET_SAME_AS_HEIGHT] when none is available
    @Override
    public double getBaselineOffset() {
        @Nullable HolderState current = currentState;
        if (current == null) {
            return Node.BASELINE_OFFSET_SAME_AS_HEIGHT;
        }
        double baseline = current.holder.getBaselineOffset();
        if (baseline == Node.BASELINE_OFFSET_SAME_AS_HEIGHT) {
            return baseline;
        }
        double availableHeight = Math.max(0.0, animatedContentHeight);
        double contentHeight = current.holder.getHeight();
        return snappedTopInset()
                + alignedOffset(availableHeight, contentHeight, getAlignment().getVpos())
                + baseline;
    }

    /// Returns the animated minimum width including snapped insets.
    @Override
    protected double computeMinWidth(double height) {
        if (isFitToWidth()) {
            return horizontalInsets();
        }
        return horizontalInsets() + Math.max(0.0, animatedContentWidth);
    }

    /// Returns the animated minimum height including snapped insets.
    @Override
    protected double computeMinHeight(double width) {
        if (isFitToHeight()) {
            return verticalInsets();
        }
        return verticalInsets() + Math.max(0.0, animatedContentHeight);
    }

    /// Returns the animated preferred width including snapped insets.
    @Override
    protected double computePrefWidth(double height) {
        if (isFitToWidth()) {
            return horizontalInsets();
        }
        return horizontalInsets() + Math.max(0.0, animatedContentWidth);
    }

    /// Returns the animated preferred height including snapped insets.
    @Override
    protected double computePrefHeight(double width) {
        if (isFitToHeight()) {
            return verticalInsets();
        }
        return verticalInsets() + Math.max(0.0, animatedContentHeight);
    }

    /// Returns an unbounded maximum width so normal JavaFX parents may stretch this region.
    @Override
    protected double computeMaxWidth(double height) {
        return Double.MAX_VALUE;
    }

    /// Returns an unbounded maximum height so normal JavaFX parents may stretch this region.
    @Override
    protected double computeMaxHeight(double width) {
        return Double.MAX_VALUE;
    }

    /// Measures pending target changes and positions both private holders.
    @Override
    protected void layoutChildren() {
        double left = snappedLeftInset();
        double top = snappedTopInset();
        double width = Math.max(0.0, getWidth() - left - snappedRightInset());
        double height = Math.max(0.0, getHeight() - top - snappedBottomInset());
        if (currentState != null
                && ((isFitToWidth()
                && (Double.isNaN(measuredFitWidth)
                || Math.abs(measuredFitWidth - width) > SIZE_VISIBILITY_THRESHOLD))
                || (isFitToHeight()
                && (Double.isNaN(measuredFitHeight)
                || Math.abs(measuredFitHeight - height) > SIZE_VISIBILITY_THRESHOLD)))) {
            measurementPending = true;
        }
        if (measurementPending) {
            refreshMeasuredSize();
        }
        viewport.resizeRelocate(left, top, width, height);
        viewportClip.setWidth(width);
        viewportClip.setHeight(height);

        layoutHolder(firstState, width, height);
        layoutHolder(secondState, width, height);
    }

    /// Replaces or reverses the retained target content.
    private void replaceContent(@Nullable Node target) {
        @Nullable HolderState previousCurrent = currentState;
        @Nullable HolderState previousOutgoing = outgoingState;

        if (target != null
                && previousOutgoing != null
                && !previousOutgoing.holder.getChildren().isEmpty()
                && previousOutgoing.holder.getChildren().get(0) == target) {
            currentState = previousOutgoing;
            outgoingState = previousCurrent;
        } else if (target == null) {
            currentState = null;
            outgoingState = previousCurrent;
            if (previousOutgoing != null) {
                previousOutgoing.clearContent();
            }
        } else {
            HolderState incoming;
            if (previousCurrent == null) {
                incoming = previousOutgoing == null ? firstState : previousOutgoing;
            } else {
                incoming = otherState(previousCurrent);
            }
            if (incoming == previousOutgoing) {
                incoming.clearContent();
            }
            incoming.installContent(target);
            incoming.prepareForEnter(getContentTransform().targetContentEnter());
            currentState = incoming;
            outgoingState = previousCurrent;
        }

        updateHolderOrder();
        measureTargetContent();

        if (!sizeInitialized || getScene() == null) {
            snapToCurrentState();
            return;
        }

        transitioning.set(true);
        animation.retarget();
    }

    /// Returns the holder state other than the supplied state.
    private HolderState otherState(HolderState state) {
        return state == firstState ? secondState : firstState;
    }

    /// Makes the target holder interactive and applies the configured target-content drawing order.
    ///
    /// The holders remain at stable child-list positions for the lifetime of this region. Drawing order is expressed
    /// through [Node#viewOrderProperty()] so completing a transition does not detach and reinsert rendered content,
    /// which would cause an avoidable CSS and layout pass after the final animation pulse.
    private void updateHolderOrder() {
        @Nullable HolderState current = currentState;
        @Nullable HolderState outgoing = outgoingState;

        firstState.holder.setMouseTransparent(firstState != current);
        secondState.holder.setMouseTransparent(secondState != current);
        firstState.holder.setVisible(firstState == current || firstState == outgoing);
        secondState.holder.setVisible(secondState == current || secondState == outgoing);

        if (current != null && outgoing != null) {
            outgoing.holder.setViewOrder(0.0);
            current.holder.setViewOrder(getContentTransform().targetContentZIndex() >= 0.0 ? -1.0 : 1.0);
        }
    }

    /// Measures the target holder at its independent preferred size or current fit constraints.
    private void measureTargetContent() {
        measurementPending = false;
        @Nullable HolderState current = currentState;
        if (current == null) {
            targetContentWidth = 0.0;
            targetContentHeight = 0.0;
            measuredFitWidth = Double.NaN;
            measuredFitHeight = Double.NaN;
            return;
        }

        boolean constrainedToWidth = isFitToWidth() && getWidth() > 0.0;
        boolean constrainedToHeight = isFitToHeight() && getHeight() > 0.0;
        measuring = true;
        try {
            current.holder.applyCss();
            double width = constrainedToWidth
                    ? Math.max(0.0, getWidth() - snappedLeftInset() - snappedRightInset())
                    : finiteSize(current.holder.prefWidth(-1.0));
            double height = constrainedToHeight
                    ? Math.max(0.0, getHeight() - snappedTopInset() - snappedBottomInset())
                    : finiteSize(current.holder.prefHeight(width));
            if (!constrainedToWidth && constrainedToHeight) {
                width = finiteSize(current.holder.prefWidth(height));
            }
            current.holder.resize(width, height);
            current.holder.layout();
            current.updateClipGeometry();
            targetContentWidth = width;
            targetContentHeight = height;
            measuredFitWidth = constrainedToWidth ? width : Double.NaN;
            measuredFitHeight = constrainedToHeight ? height : Double.NaN;
        } finally {
            measuring = false;
        }
    }

    /// Remeasures content after its own layout contract changes.
    private void refreshMeasuredSize() {
        double oldWidth = targetContentWidth;
        double oldHeight = targetContentHeight;
        measureTargetContent();
        if (Double.compare(oldWidth, targetContentWidth) == 0
                && Double.compare(oldHeight, targetContentHeight) == 0) {
            return;
        }

        if (!sizeInitialized || getScene() == null || getContentTransform().sizeTransform() == null) {
            animatedContentWidth = targetContentWidth;
            animatedContentHeight = targetContentHeight;
            sizeInitialized = true;
            animation.resetSizeChannels();
            requestLayout();
            return;
        }

        transitioning.set(true);
        animation.retarget();
    }

    /// Receives a private holder layout invalidation that may change target size.
    private void holderNeedsLayout(HolderState state, boolean needsLayout) {
        if (needsLayout && !measuring && state == currentState) {
            measurementPending = true;
            requestLayout();
        }
    }

    /// Positions one visible holder at its preferred size inside the viewport.
    private void layoutHolder(HolderState state, double width, double height) {
        if (!state.holder.isVisible()) {
            return;
        }
        double holderWidth = state.holder.getWidth();
        double holderHeight = state.holder.getHeight();
        Pos position = getAlignment();
        state.holder.relocate(
                snapPositionX(alignedOffset(width, holderWidth, position.getHpos())),
                snapPositionY(alignedOffset(height, holderHeight, position.getVpos()))
        );
        state.updateClipGeometry();
    }

    /// Completes lifecycle cleanup after every channel reaches its target.
    private void completeTransition() {
        clearOutgoingState();

        @Nullable HolderState current = currentState;
        if (current != null) {
            current.resetVisuals(1.0, 1.0, 0.0, 0.0);
        }

        animatedContentWidth = targetContentWidth;
        animatedContentHeight = targetContentHeight;
        sizeInitialized = true;
        animation.resetSizeChannels();
        updateHolderOrder();
        transitioning.set(false);
        requestLayout();
    }

    /// Detaches and resets the outgoing holder, when present.
    private void clearOutgoingState() {
        @Nullable HolderState outgoing = outgoingState;
        outgoingState = null;
        if (outgoing != null && outgoing != currentState) {
            outgoing.clearContent();
        }
    }

    /// Retargets active channels after a transition property changes.
    private void retargetIfTransitioning() {
        if (isTransitioning()) {
            animation.retarget();
        }
    }

    /// Installs or removes the clip specified by the current size transform.
    private void updateViewportClip() {
        @Nullable M3SizeTransform sizeTransform = getContentTransform().sizeTransform();
        viewport.setClip(sizeTransform != null && sizeTransform.clip() ? viewportClip : null);
    }

    /// Returns the horizontal snapped inset total.
    private double horizontalInsets() {
        return snappedLeftInset() + snappedRightInset();
    }

    /// Returns the vertical snapped inset total.
    private double verticalInsets() {
        return snappedTopInset() + snappedBottomInset();
    }

    /// Returns a finite non-negative JavaFX layout size.
    private static double finiteSize(double value) {
        if (!Double.isFinite(value)) {
            return value > 0.0 ? Double.MAX_VALUE : 0.0;
        }
        return Math.max(0.0, value);
    }

    /// Restricts one finite normalized reveal coordinate to the unit interval.
    private static double clampUnit(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Computes one aligned coordinate inside an available extent.
    private static double alignedOffset(double available, double size, HPos alignment) {
        return switch (alignment) {
            case CENTER -> (available - size) / 2.0;
            case RIGHT -> available - size;
            default -> 0.0;
        };
    }

    /// Computes one aligned coordinate inside an available extent.
    private static double alignedOffset(double available, double size, VPos alignment) {
        return switch (alignment) {
            case CENTER -> (available - size) / 2.0;
            case BOTTOM -> available - size;
            default -> 0.0;
        };
    }

    /// Returns the effect of one kind, or `null` when that channel is not part of a transition.
    private static @Nullable M3TransitionEffect findEffect(
            List<M3TransitionEffect> effects,
            M3TransitionEffectKind kind
    ) {
        for (M3TransitionEffect effect : effects) {
            if (effect.kind() == kind) {
                return effect;
            }
        }
        return null;
    }

    /// Resolves a slide effect to its physical horizontal translation.
    private double slideOffsetX(M3TransitionEffect effect) {
        M3TransitionEdge edge = Objects.requireNonNull(effect.edge(), "slide edge");
        boolean rightToLeft = getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        return switch (edge) {
            case START -> rightToLeft ? effect.value() : -effect.value();
            case END -> rightToLeft ? -effect.value() : effect.value();
            case TOP, BOTTOM -> 0.0;
        };
    }

    /// Resolves a slide effect to its physical vertical translation.
    private static double slideOffsetY(M3TransitionEffect effect) {
        return switch (Objects.requireNonNull(effect.edge(), "slide edge")) {
            case TOP -> -effect.value();
            case BOTTOM -> effect.value();
            case START, END -> 0.0;
        };
    }

    /// Stores one reusable holder and its fixed visual animation channels.
    @NotNullByDefault
    private final class HolderState {
        /// The private wrapper that owns visual transition properties.
        private final StackPane holder = new StackPane();

        /// The reusable rectangle that clips this holder while a reveal effect is active.
        private final Rectangle revealClip = new Rectangle();

        /// The holder opacity channel.
        private final M3DelayedScalarChannel opacity =
                new M3DelayedScalarChannel(OPACITY_VISIBILITY_THRESHOLD);

        /// The holder uniform-scale channel.
        private final M3DelayedScalarChannel scale =
                new M3DelayedScalarChannel(SCALE_VISIBILITY_THRESHOLD);

        /// The holder horizontal-translation channel.
        private final M3DelayedScalarChannel translateX =
                new M3DelayedScalarChannel(TRANSLATION_VISIBILITY_THRESHOLD);

        /// The holder vertical-translation channel.
        private final M3DelayedScalarChannel translateY =
                new M3DelayedScalarChannel(TRANSLATION_VISIBILITY_THRESHOLD);

        /// The normalized logical minimum-x reveal channel.
        private final M3DelayedScalarChannel clipMinX =
                new M3DelayedScalarChannel(CLIP_VISIBILITY_THRESHOLD);

        /// The normalized minimum-y reveal channel.
        private final M3DelayedScalarChannel clipMinY =
                new M3DelayedScalarChannel(CLIP_VISIBILITY_THRESHOLD);

        /// The normalized logical maximum-x reveal channel.
        private final M3DelayedScalarChannel clipMaxX =
                new M3DelayedScalarChannel(CLIP_VISIBILITY_THRESHOLD);

        /// The normalized maximum-y reveal channel.
        private final M3DelayedScalarChannel clipMaxY =
                new M3DelayedScalarChannel(CLIP_VISIBILITY_THRESHOLD);

        /// The currently rendered normalized logical minimum x-coordinate.
        private double clipMinXValue;

        /// The currently rendered normalized minimum y-coordinate.
        private double clipMinYValue;

        /// The currently rendered normalized logical maximum x-coordinate.
        private double clipMaxXValue = 1.0;

        /// The currently rendered normalized maximum y-coordinate.
        private double clipMaxYValue = 1.0;

        /// Whether the reusable reveal rectangle is installed as the holder clip.
        private boolean clipActive;

        /// Creates an empty inactive holder.
        private HolderState() {
            holder.getStyleClass().add(ITEM_STYLE_CLASS);
            holder.setManaged(false);
            holder.setPickOnBounds(false);
            holder.setVisible(false);
            holder.needsLayoutProperty().addListener(
                    (observable, oldValue, newValue) -> holderNeedsLayout(this, newValue)
            );
            revealClip.setSmooth(false);
            resetVisuals(1.0, 1.0, 0.0, 0.0);
        }

        /// Installs one content node as the holder's only child.
        private void installContent(Node content) {
            holder.getChildren().setAll(Objects.requireNonNull(content, "content"));
            holder.setVisible(true);
            measurementPending = true;
        }

        /// Applies the configured starting values for newly installed incoming content.
        private void prepareForEnter(M3EnterTransition transition) {
            double opacityValue = 1.0;
            double scaleValue = 1.0;
            double translateXValue = 0.0;
            double translateYValue = 0.0;
            @Nullable Rectangle2D clipBounds = null;
            for (M3TransitionEffect effect : ((M3EnterTransitionImpl) transition).effects()) {
                switch (effect.kind()) {
                    case FADE -> opacityValue = effect.value();
                    case SCALE -> scaleValue = effect.value();
                    case SLIDE -> {
                        translateXValue = slideOffsetX(effect);
                        translateYValue = slideOffsetY(effect);
                    }
                    case CLIP -> clipBounds = Objects.requireNonNull(effect.clipBounds(), "clipBounds");
                }
            }
            resetVisuals(opacityValue, scaleValue, translateXValue, translateYValue);
            if (clipBounds != null) {
                resetClip(clipBounds);
            }
        }

        /// Removes content and resets visual state without retaining the previous node.
        private void clearContent() {
            holder.getChildren().clear();
            holder.setVisible(false);
            holder.setMouseTransparent(true);
            holder.setViewOrder(0.0);
            resetVisuals(1.0, 1.0, 0.0, 0.0);
        }

        /// Immediately applies all holder visuals and clears retained velocity.
        private void resetVisuals(
                double opacityValue,
                double scaleValue,
                double translateXValue,
                double translateYValue
        ) {
            holder.setOpacity(opacityValue);
            holder.setScaleX(scaleValue);
            holder.setScaleY(scaleValue);
            holder.setTranslateX(translateXValue);
            holder.setTranslateY(translateYValue);
            opacity.reset(opacityValue);
            scale.reset(scaleValue);
            translateX.reset(translateXValue);
            translateY.reset(translateYValue);
            clearClip();
        }

        /// Clears channel velocity while retaining the holder's current visual values.
        private void resetChannelsToCurrentVisuals() {
            opacity.reset(holder.getOpacity());
            scale.reset(holder.getScaleX());
            translateX.reset(holder.getTranslateX());
            translateY.reset(holder.getTranslateY());
            resetClipChannelsToCurrentValues();
        }

        /// Installs the reveal rectangle at normalized bounds and clears clip-channel velocity.
        private void resetClip(Rectangle2D bounds) {
            resetClip(
                    bounds.getMinX(),
                    bounds.getMinY(),
                    bounds.getMaxX(),
                    bounds.getMaxY()
            );
        }

        /// Installs the reveal rectangle at four normalized logical edges.
        private void resetClip(double minX, double minY, double maxX, double maxY) {
            clipActive = true;
            holder.setClip(revealClip);
            applyClip(minX, minY, maxX, maxY);
            resetClipChannelsToCurrentValues();
        }

        /// Installs a full-bounds reveal rectangle without changing the rendered holder.
        private void ensureClip() {
            if (!clipActive) {
                resetClip(0.0, 0.0, 1.0, 1.0);
            }
        }

        /// Removes the private reveal clip and resets its channels to full bounds.
        private void clearClip() {
            clipActive = false;
            holder.setClip(null);
            clipMinXValue = 0.0;
            clipMinYValue = 0.0;
            clipMaxXValue = 1.0;
            clipMaxYValue = 1.0;
            resetClipChannelsToCurrentValues();
        }

        /// Clears clip-channel velocity while retaining the currently rendered normalized bounds.
        private void resetClipChannelsToCurrentValues() {
            clipMinX.reset(clipMinXValue);
            clipMinY.reset(clipMinYValue);
            clipMaxX.reset(clipMaxXValue);
            clipMaxY.reset(clipMaxYValue);
        }

        /// Applies normalized logical reveal bounds and updates the reusable clip geometry.
        private void applyClip(double minX, double minY, double maxX, double maxY) {
            double boundedMinX = clampUnit(minX);
            double boundedMinY = clampUnit(minY);
            double boundedMaxX = clampUnit(maxX);
            double boundedMaxY = clampUnit(maxY);
            if (boundedMaxX < boundedMinX) {
                boundedMinX = boundedMaxX = (boundedMinX + boundedMaxX) / 2.0;
            }
            if (boundedMaxY < boundedMinY) {
                boundedMinY = boundedMaxY = (boundedMinY + boundedMaxY) / 2.0;
            }
            clipMinXValue = boundedMinX;
            clipMinYValue = boundedMinY;
            clipMaxXValue = boundedMaxX;
            clipMaxYValue = boundedMaxY;
            updateClipGeometry();
        }

        /// Resolves logical horizontal edges and sizes the reusable reveal rectangle.
        private void updateClipGeometry() {
            if (!clipActive) {
                return;
            }
            boolean rightToLeft =
                    M3AnimatedContent.this.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
            double physicalMinX = rightToLeft ? 1.0 - clipMaxXValue : clipMinXValue;
            double physicalMaxX = rightToLeft ? 1.0 - clipMinXValue : clipMaxXValue;
            double width = holder.getWidth();
            double height = holder.getHeight();
            revealClip.setX(physicalMinX * width);
            revealClip.setY(clipMinYValue * height);
            revealClip.setWidth(Math.max(0.0, physicalMaxX - physicalMinX) * width);
            revealClip.setHeight(Math.max(0.0, clipMaxYValue - clipMinYValue) * height);
        }
    }

    /// Shared finite transition for both holders and the animated content size.
    @NotNullByDefault
    private final class ContentAnimation extends M3FiniteTransition {
        /// The animated content-width channel.
        private final M3ScalarChannel width = new M3ScalarChannel(SIZE_VISIBILITY_THRESHOLD);

        /// The animated content-height channel.
        private final M3ScalarChannel height = new M3ScalarChannel(SIZE_VISIBILITY_THRESHOLD);

        /// The duration of the longest active channel, in seconds.
        private double runDurationSeconds;

        /// Reconfigures every active channel from its current rendered state.
        private void retarget() {
            double elapsedSeconds = getStatus() == Animation.Status.RUNNING
                    ? Math.max(0.0, getCurrentTime().toSeconds())
                    : Double.POSITIVE_INFINITY;
            M3ContentTransform transform = getContentTransform();
            M3EnterTransitionImpl enter =
                    (M3EnterTransitionImpl) transform.targetContentEnter();
            M3ExitTransitionImpl exit =
                    (M3ExitTransitionImpl) transform.initialContentExit();

            configureHolder(firstState, elapsedSeconds, enter, exit);
            configureHolder(secondState, elapsedSeconds, enter, exit);

            @Nullable M3SizeTransform sizeTransform = transform.sizeTransform();
            if (sizeTransform != null) {
                @Nullable M3MotionSpec explicitSizeSpec = sizeTransform.motionSpec();
                M3MotionSpec sizeSpec = explicitSizeSpec == null
                        ? M3Animation.defaultSpatial(M3AnimatedContent.this)
                        : explicitSizeSpec;
                width.configure(animatedContentWidth, targetContentWidth, sizeSpec, elapsedSeconds);
                height.configure(animatedContentHeight, targetContentHeight, sizeSpec, elapsedSeconds);
            } else {
                animatedContentWidth = targetContentWidth;
                animatedContentHeight = targetContentHeight;
                resetSizeChannels();
                requestLayout();
            }

            stop();
            runDurationSeconds = Math.max(
                    Math.max(holderDuration(firstState), holderDuration(secondState)),
                    Math.max(width.getDurationSeconds(), height.getDurationSeconds())
            );

            if (runDurationSeconds <= 0.0) {
                setCycleDuration(Duration.ZERO);
                M3Animation.finish(this);
                return;
            }

            setCycleDuration(Duration.seconds(runDurationSeconds));
            setInterpolator(Interpolator.LINEAR);
            M3Animation.playFromStart(M3AnimatedContent.this, this);
        }

        /// Configures one holder according to its current lifecycle role.
        private void configureHolder(
                HolderState state,
                double elapsedSeconds,
                M3EnterTransitionImpl enter,
                M3ExitTransitionImpl exit
        ) {
            if (state == currentState) {
                configureIncoming(state, enter.effects(), elapsedSeconds);
            } else if (state == outgoingState) {
                configureOutgoing(state, exit.effects(), elapsedSeconds);
            } else {
                state.resetChannelsToCurrentVisuals();
            }
        }

        /// Configures channels that return incoming content to its neutral visual state.
        private void configureIncoming(
                HolderState state,
                List<M3TransitionEffect> effects,
                double elapsedSeconds
        ) {
            @Nullable M3TransitionEffect fade = findEffect(effects, M3TransitionEffectKind.FADE);
            if (fade == null) {
                state.holder.setOpacity(1.0);
                state.opacity.reset(1.0);
            } else {
                configureChannel(state.opacity, state.holder.getOpacity(), 1.0, fade, true, elapsedSeconds);
            }

            @Nullable M3TransitionEffect scale = findEffect(effects, M3TransitionEffectKind.SCALE);
            if (scale == null) {
                state.holder.setScaleX(1.0);
                state.holder.setScaleY(1.0);
                state.scale.reset(1.0);
            } else {
                configureChannel(state.scale, state.holder.getScaleX(), 1.0, scale, true, elapsedSeconds);
            }

            @Nullable M3TransitionEffect slide = findEffect(effects, M3TransitionEffectKind.SLIDE);
            if (slide == null) {
                state.holder.setTranslateX(0.0);
                state.holder.setTranslateY(0.0);
                state.translateX.reset(0.0);
                state.translateY.reset(0.0);
            } else {
                configureChannel(
                        state.translateX,
                        state.holder.getTranslateX(),
                        0.0,
                        slide,
                        true,
                        elapsedSeconds
                );
                configureChannel(
                        state.translateY,
                        state.holder.getTranslateY(),
                        0.0,
                        slide,
                        true,
                        elapsedSeconds
                );
            }

            @Nullable M3TransitionEffect clip = findEffect(effects, M3TransitionEffectKind.CLIP);
            if (clip == null) {
                state.clearClip();
            } else {
                state.ensureClip();
                configureClipChannels(
                        state,
                        Objects.requireNonNull(clip.clipBounds(), "clipBounds"),
                        clip,
                        true,
                        elapsedSeconds
                );
            }
        }

        /// Configures channels that move outgoing content toward its requested effects.
        private void configureOutgoing(
                HolderState state,
                List<M3TransitionEffect> effects,
                double elapsedSeconds
        ) {
            @Nullable M3TransitionEffect fade = findEffect(effects, M3TransitionEffectKind.FADE);
            if (fade == null) {
                state.opacity.reset(state.holder.getOpacity());
            } else {
                configureChannel(
                        state.opacity,
                        state.holder.getOpacity(),
                        fade.value(),
                        fade,
                        false,
                        elapsedSeconds
                );
            }

            @Nullable M3TransitionEffect scale = findEffect(effects, M3TransitionEffectKind.SCALE);
            if (scale == null) {
                state.scale.reset(state.holder.getScaleX());
            } else {
                configureChannel(
                        state.scale,
                        state.holder.getScaleX(),
                        scale.value(),
                        scale,
                        false,
                        elapsedSeconds
                );
            }

            @Nullable M3TransitionEffect slide = findEffect(effects, M3TransitionEffectKind.SLIDE);
            if (slide == null) {
                state.translateX.reset(state.holder.getTranslateX());
                state.translateY.reset(state.holder.getTranslateY());
            } else {
                configureChannel(
                        state.translateX,
                        state.holder.getTranslateX(),
                        slideOffsetX(slide),
                        slide,
                        false,
                        elapsedSeconds
                );
                configureChannel(
                        state.translateY,
                        state.holder.getTranslateY(),
                        slideOffsetY(slide),
                        slide,
                        false,
                        elapsedSeconds
                );
            }

            @Nullable M3TransitionEffect clip = findEffect(effects, M3TransitionEffectKind.CLIP);
            if (clip == null) {
                state.resetClipChannelsToCurrentValues();
            } else {
                state.ensureClip();
                configureClipChannels(
                        state,
                        Objects.requireNonNull(clip.clipBounds(), "clipBounds"),
                        clip,
                        false,
                        elapsedSeconds
                );
            }
        }

        /// Configures all normalized reveal edges as one composable clip effect.
        private void configureClipChannels(
                HolderState state,
                Rectangle2D targetBounds,
                M3TransitionEffect effect,
                boolean entering,
                double elapsedSeconds
        ) {
            double targetMinX = entering ? 0.0 : targetBounds.getMinX();
            double targetMinY = entering ? 0.0 : targetBounds.getMinY();
            double targetMaxX = entering ? 1.0 : targetBounds.getMaxX();
            double targetMaxY = entering ? 1.0 : targetBounds.getMaxY();
            configureChannel(
                    state.clipMinX,
                    state.clipMinXValue,
                    targetMinX,
                    effect,
                    entering,
                    elapsedSeconds
            );
            configureChannel(
                    state.clipMinY,
                    state.clipMinYValue,
                    targetMinY,
                    effect,
                    entering,
                    elapsedSeconds
            );
            configureChannel(
                    state.clipMaxX,
                    state.clipMaxXValue,
                    targetMaxX,
                    effect,
                    entering,
                    elapsedSeconds
            );
            configureChannel(
                    state.clipMaxY,
                    state.clipMaxYValue,
                    targetMaxY,
                    effect,
                    entering,
                    elapsedSeconds
            );
        }

        /// Configures one delayed scalar effect with an explicit or semantic motion specification.
        private void configureChannel(
                M3DelayedScalarChannel channel,
                double currentValue,
                double targetValue,
                M3TransitionEffect effect,
                boolean entering,
                double elapsedSeconds
        ) {
            channel.configure(
                    currentValue,
                    targetValue,
                    resolveEffectSpec(effect, entering),
                    effect.delay().toSeconds(),
                    elapsedSeconds
            );
        }

        /// Returns the longest visual channel duration owned by one holder.
        private double holderDuration(HolderState state) {
            double transformDuration = Math.max(
                    Math.max(state.opacity.getDurationSeconds(), state.scale.getDurationSeconds()),
                    Math.max(state.translateX.getDurationSeconds(), state.translateY.getDurationSeconds())
            );
            double clipDuration = Math.max(
                    Math.max(state.clipMinX.getDurationSeconds(), state.clipMinY.getDurationSeconds()),
                    Math.max(state.clipMaxX.getDurationSeconds(), state.clipMaxY.getDurationSeconds())
            );
            return Math.max(transformDuration, clipDuration);
        }

        /// Applies one shared elapsed time to all fixed channels.
        @Override
        protected void interpolate(double fraction) {
            double elapsedSeconds = Math.max(0.0, fraction) * runDurationSeconds;
            applyHolder(firstState, elapsedSeconds);
            applyHolder(secondState, elapsedSeconds);

            double newWidth = Math.max(0.0, width.valueAt(elapsedSeconds));
            double newHeight = Math.max(0.0, height.valueAt(elapsedSeconds));
            if (Double.compare(animatedContentWidth, newWidth) != 0
                    || Double.compare(animatedContentHeight, newHeight) != 0) {
                animatedContentWidth = newWidth;
                animatedContentHeight = newHeight;
                requestLayout();
            }
        }

        /// Applies current visual values to one holder.
        private void applyHolder(HolderState state, double elapsedSeconds) {
            state.holder.setOpacity(Math.max(0.0, Math.min(1.0, state.opacity.valueAt(elapsedSeconds))));
            double scaleValue = state.scale.valueAt(elapsedSeconds);
            state.holder.setScaleX(scaleValue);
            state.holder.setScaleY(scaleValue);
            state.holder.setTranslateX(snapPositionX(state.translateX.valueAt(elapsedSeconds)));
            state.holder.setTranslateY(snapPositionY(state.translateY.valueAt(elapsedSeconds)));
            if (state.clipActive) {
                state.applyClip(
                        state.clipMinX.valueAt(elapsedSeconds),
                        state.clipMinY.valueAt(elapsedSeconds),
                        state.clipMaxX.valueAt(elapsedSeconds),
                        state.clipMaxY.valueAt(elapsedSeconds)
                );
            }
        }

        /// Resets size channel history to the currently rendered dimensions.
        private void resetSizeChannels() {
            width.reset(animatedContentWidth);
            height.reset(animatedContentHeight);
        }

        /// Resolves an effect's explicit spec or its semantic default.
        private M3MotionSpec resolveEffectSpec(M3TransitionEffect effect, boolean entering) {
            @Nullable M3MotionSpec explicit = effect.motionSpec();
            if (explicit != null) {
                return explicit;
            }
            if (effect.kind() == M3TransitionEffectKind.FADE) {
                return entering
                        ? M3Animation.defaultEffects(M3AnimatedContent.this)
                        : M3Animation.fastEffects(M3AnimatedContent.this);
            }
            return entering
                    ? M3Animation.defaultSpatial(M3AnimatedContent.this)
                    : M3Animation.fastSpatial(M3AnimatedContent.this);
        }
    }

}
