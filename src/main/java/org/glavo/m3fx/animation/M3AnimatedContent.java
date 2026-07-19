// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.animation.M3ScalarChannel;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Animates replacement of one retained JavaFX content node with another.
///
/// The [content property][#contentProperty()] identifies the target content. When it changes, this region keeps the
/// previous and target nodes in private holders until their exit and enter effects have completed. The target is
/// drawn above the previous content and is the only interactive node during the transition. The previous node is
/// detached when the transition completes. At most two content nodes are retained, including when targets change
/// repeatedly before an earlier transition finishes.
///
/// Visual effects are applied to the private holders, leaving each content node's opacity, scale, translation, and
/// transform list under caller ownership. Preferred and minimum size move from the current rendered size to the
/// target content size when [#sizeAnimationEnabledProperty()] is `true`. Content is clipped to the region by default.
/// A target that is already the outgoing node reverses naturally from its current visual state instead of being
/// reparented or reset.
///
/// This class follows the retained-mode semantics of JavaFX rather than accepting a state-to-content composition
/// callback. Callers create and retain their nodes, then assign the desired target through [#setContent(Node)]. A
/// node assigned as content must be unparented, already hosted by this region, or otherwise legal to add to a JavaFX
/// scene graph. Assigning `null` performs an animated removal.
///
/// Enter, exit, and size channels are interruptible. Physical spring specifications retain channel velocity when a
/// target or configuration changes. Reduced motion, scene detachment during a run, and [#snapToCurrentState()]
/// settle synchronously and release outgoing content. Animation-control and property mutation methods must be
/// invoked on the JavaFX Application Thread once this node is attached to a showing scene.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3AnimatedContent extends Region {
    /// The default style class assigned to animated-content regions.
    private static final String DEFAULT_STYLE_CLASS = "m3-animated-content";

    /// The default style class assigned to private content holders.
    private static final String ITEM_STYLE_CLASS = "m3-animated-content-item";

    /// The default alignment of current and outgoing content.
    private static final Pos DEFAULT_ALIGNMENT = Pos.TOP_LEFT;

    /// The scale from which newly assigned content enters.
    private static final double DEFAULT_ENTER_SCALE = 0.92;

    /// The scale reached by content as it exits.
    private static final double DEFAULT_EXIT_SCALE = 1.0;

    /// The visibility threshold used for opacity spring channels.
    private static final double OPACITY_VISIBILITY_THRESHOLD = 1.0e-2;

    /// The visibility threshold used for scale spring channels.
    private static final double SCALE_VISIBILITY_THRESHOLD = 5.0e-4;

    /// The visibility threshold used for size spring channels, in logical pixels.
    private static final double SIZE_VISIBILITY_THRESHOLD = 5.0e-1;

    /// The private viewport that owns clipping and the two reusable holders.
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
    /// During a transition the previous node may remain attached internally until its exit effect completes.
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
    /// @throws IllegalArgumentException if the node cannot legally be added to this region's private scene graph
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

    /// The initial scale applied to newly assigned target content.
    private final DoubleProperty enterScale =
            new SimpleDoubleProperty(this, "enterScale", DEFAULT_ENTER_SCALE) {
                /// Validates and retargets after the enter scale changes.
                @Override
                protected void invalidated() {
                    validateScale(get(), "enterScale");
                    retargetIfTransitioning();
                }
            };

    /// Returns the initial scale applied to newly assigned target content.
    ///
    /// @return the finite, positive enter scale
    public double getEnterScale() {
        return enterScale.get();
    }

    /// Sets the initial scale applied to newly assigned target content.
    ///
    /// @param enterScale the finite, positive enter scale
    /// @throws IllegalArgumentException if the value is not finite and greater than zero
    public void setEnterScale(double enterScale) {
        this.enterScale.set(validateScale(enterScale, "enterScale"));
    }

    /// Returns the observable enter-scale property.
    ///
    /// The default value is `0.92`. Values supplied through a binding must be finite and greater than zero.
    ///
    /// @return the enter-scale property
    public DoubleProperty enterScaleProperty() {
        return enterScale;
    }

    /// The final scale reached by outgoing content.
    private final DoubleProperty exitScale =
            new SimpleDoubleProperty(this, "exitScale", DEFAULT_EXIT_SCALE) {
                /// Validates and retargets after the exit scale changes.
                @Override
                protected void invalidated() {
                    validateScale(get(), "exitScale");
                    retargetIfTransitioning();
                }
            };

    /// Returns the final scale reached by outgoing content.
    ///
    /// @return the finite, positive exit scale
    public double getExitScale() {
        return exitScale.get();
    }

    /// Sets the final scale reached by outgoing content.
    ///
    /// @param exitScale the finite, positive exit scale
    /// @throws IllegalArgumentException if the value is not finite and greater than zero
    public void setExitScale(double exitScale) {
        this.exitScale.set(validateScale(exitScale, "exitScale"));
    }

    /// Returns the observable exit-scale property.
    ///
    /// The default value is `1.0`. Values supplied through a binding must be finite and greater than zero.
    ///
    /// @return the exit-scale property
    public DoubleProperty exitScaleProperty() {
        return exitScale;
    }

    /// The explicit target-content enter specification, or `null` for the theme default effects role.
    private final ObjectProperty<@Nullable M3MotionSpec> enterMotionSpec =
            new SimpleObjectProperty<>(this, "enterMotionSpec") {
                /// Retargets an active transition after the explicit specification changes.
                @Override
                protected void invalidated() {
                    retargetIfTransitioning();
                }
            };

    /// Returns the explicit target-content enter specification.
    ///
    /// @return the explicit specification, or `null` for the active theme's default effects role
    public @Nullable M3MotionSpec getEnterMotionSpec() {
        return enterMotionSpec.get();
    }

    /// Sets the target-content enter specification used by subsequent and active transitions.
    ///
    /// @param motionSpec the explicit specification, or `null` to resolve the active theme
    public void setEnterMotionSpec(@Nullable M3MotionSpec motionSpec) {
        enterMotionSpec.set(motionSpec);
    }

    /// Returns the observable explicit enter-specification property.
    ///
    /// @return the enter-specification property, whose value may be `null`
    public ObjectProperty<@Nullable M3MotionSpec> enterMotionSpecProperty() {
        return enterMotionSpec;
    }

    /// The explicit outgoing-content specification, or `null` for the theme fast effects role.
    private final ObjectProperty<@Nullable M3MotionSpec> exitMotionSpec =
            new SimpleObjectProperty<>(this, "exitMotionSpec") {
                /// Retargets an active transition after the explicit specification changes.
                @Override
                protected void invalidated() {
                    retargetIfTransitioning();
                }
            };

    /// Returns the explicit outgoing-content specification.
    ///
    /// @return the explicit specification, or `null` for the active theme's fast effects role
    public @Nullable M3MotionSpec getExitMotionSpec() {
        return exitMotionSpec.get();
    }

    /// Sets the outgoing-content specification used by subsequent and active transitions.
    ///
    /// @param motionSpec the explicit specification, or `null` to resolve the active theme
    public void setExitMotionSpec(@Nullable M3MotionSpec motionSpec) {
        exitMotionSpec.set(motionSpec);
    }

    /// Returns the observable explicit exit-specification property.
    ///
    /// @return the exit-specification property, whose value may be `null`
    public ObjectProperty<@Nullable M3MotionSpec> exitMotionSpecProperty() {
        return exitMotionSpec;
    }

    /// The explicit container-size specification, or `null` for the theme default spatial role.
    private final ObjectProperty<@Nullable M3MotionSpec> sizeMotionSpec =
            new SimpleObjectProperty<>(this, "sizeMotionSpec") {
                /// Retargets an active transition after the explicit specification changes.
                @Override
                protected void invalidated() {
                    retargetIfTransitioning();
                }
            };

    /// Returns the explicit container-size specification.
    ///
    /// @return the explicit specification, or `null` for the active theme's default spatial role
    public @Nullable M3MotionSpec getSizeMotionSpec() {
        return sizeMotionSpec.get();
    }

    /// Sets the container-size specification used by subsequent and active transitions.
    ///
    /// @param motionSpec the explicit specification, or `null` to resolve the active theme
    public void setSizeMotionSpec(@Nullable M3MotionSpec motionSpec) {
        sizeMotionSpec.set(motionSpec);
    }

    /// Returns the observable explicit size-specification property.
    ///
    /// @return the size-specification property, whose value may be `null`
    public ObjectProperty<@Nullable M3MotionSpec> sizeMotionSpecProperty() {
        return sizeMotionSpec;
    }

    /// Whether preferred and minimum size move toward the target content size.
    private final BooleanProperty sizeAnimationEnabled =
            new SimpleBooleanProperty(this, "sizeAnimationEnabled", true) {
                /// Retargets or settles size channels after the policy changes.
                @Override
                protected void invalidated() {
                    if (!get()) {
                        animatedContentWidth = targetContentWidth;
                        animatedContentHeight = targetContentHeight;
                        animation.resetSizeChannels();
                        requestLayout();
                    }
                    retargetIfTransitioning();
                }
            };

    /// Returns whether content-size changes are animated.
    ///
    /// @return `true` when preferred and minimum size move toward the target size
    public boolean isSizeAnimationEnabled() {
        return sizeAnimationEnabled.get();
    }

    /// Sets whether content-size changes should be animated.
    ///
    /// Disabling this property while a transition is active applies the target size synchronously without stopping
    /// the enter or exit effects.
    ///
    /// @param enabled whether size changes should be animated
    public void setSizeAnimationEnabled(boolean enabled) {
        sizeAnimationEnabled.set(enabled);
    }

    /// Returns the observable size-animation policy property.
    ///
    /// @return the size-animation policy property, initially `true`
    public BooleanProperty sizeAnimationEnabledProperty() {
        return sizeAnimationEnabled;
    }

    /// Whether drawing is clipped to the private viewport.
    private final BooleanProperty clipContent =
            new SimpleBooleanProperty(this, "clipContent", true) {
                /// Installs or removes the clip after the policy changes.
                @Override
                protected void invalidated() {
                    updateViewportClip();
                }
            };

    /// Returns whether content drawing is clipped to this region.
    ///
    /// @return `true` when current and outgoing content are clipped
    public boolean isClipContent() {
        return clipContent.get();
    }

    /// Sets whether content drawing should be clipped to this region.
    ///
    /// This setting affects drawing and picking outside the region; it does not change preferred-size calculation.
    ///
    /// @param clipContent whether content should be clipped
    public void setClipContent(boolean clipContent) {
        this.clipContent.set(clipContent);
    }

    /// Returns the observable content-clipping property.
    ///
    /// @return the content-clipping property, initially `true`
    public BooleanProperty clipContentProperty() {
        return clipContent;
    }

    /// Whether an enter, exit, or size channel is moving toward its target.
    private final ReadOnlyBooleanWrapper transitioning =
            new ReadOnlyBooleanWrapper(this, "transitioning", false);

    /// Returns whether a content or size transition is active.
    ///
    /// @return `true` until all active channels have reached their targets and outgoing content has been released
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
    /// @throws IllegalArgumentException if the node cannot legally be added to this region's private scene graph
    public M3AnimatedContent(Node content) {
        this();
        setContent(Objects.requireNonNull(content, "content"));
    }

    /// Completes the active transition at its target state.
    ///
    /// This method has no effect when no transition is active. Completion is synchronous and releases outgoing
    /// content before returning.
    public void finish() {
        if (animation.getStatus() == Animation.Status.RUNNING) {
            M3Animation.finish(animation);
        } else if (isTransitioning()) {
            snapToCurrentState();
        }
    }

    /// Immediately applies the current target content and target size.
    ///
    /// Any active transition is stopped, outgoing content is detached, and the target holder returns to opacity and
    /// scale `1.0`. Repeated calls are idempotent.
    public void snapToCurrentState() {
        animation.stop();
        clearOutgoingState();

        @Nullable HolderState current = currentState;
        if (current != null) {
            current.holder.setOpacity(1.0);
            current.holder.setScaleX(1.0);
            current.holder.setScaleY(1.0);
            current.resetChannelsToCurrentVisuals();
        }

        measureTargetContent();
        animatedContentWidth = targetContentWidth;
        animatedContentHeight = targetContentHeight;
        sizeInitialized = true;
        animation.resetSizeChannels();
        updateHolderOrder();
        transitioning.set(false);
        requestContainerLayout();
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
        return horizontalInsets() + Math.max(0.0, animatedContentWidth);
    }

    /// Returns the animated minimum height including snapped insets.
    @Override
    protected double computeMinHeight(double width) {
        return verticalInsets() + Math.max(0.0, animatedContentHeight);
    }

    /// Returns the animated preferred width including snapped insets.
    @Override
    protected double computePrefWidth(double height) {
        return horizontalInsets() + Math.max(0.0, animatedContentWidth);
    }

    /// Returns the animated preferred height including snapped insets.
    @Override
    protected double computePrefHeight(double width) {
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
        if (measurementPending) {
            refreshMeasuredSize();
        }

        double left = snappedLeftInset();
        double top = snappedTopInset();
        double width = Math.max(0.0, getWidth() - left - snappedRightInset());
        double height = Math.max(0.0, getHeight() - top - snappedBottomInset());
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

        if (target != null && previousOutgoing != null && previousOutgoing.content() == target) {
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
            incoming.resetVisuals(0.0, getEnterScale());
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

    /// Makes the target holder interactive and orders it above outgoing content.
    private void updateHolderOrder() {
        @Nullable HolderState current = currentState;
        @Nullable HolderState outgoing = outgoingState;

        firstState.holder.setMouseTransparent(firstState != current);
        secondState.holder.setMouseTransparent(secondState != current);
        firstState.holder.setVisible(firstState == current || firstState == outgoing);
        secondState.holder.setVisible(secondState == current || secondState == outgoing);

        if (current != null && outgoing != null) {
            viewport.getChildren().setAll(outgoing.holder, current.holder);
        } else if (current == firstState || outgoing == firstState) {
            viewport.getChildren().setAll(secondState.holder, firstState.holder);
        } else {
            viewport.getChildren().setAll(firstState.holder, secondState.holder);
        }
    }

    /// Measures the target holder at its independent preferred size.
    private void measureTargetContent() {
        measurementPending = false;
        @Nullable HolderState current = currentState;
        if (current == null) {
            targetContentWidth = 0.0;
            targetContentHeight = 0.0;
            return;
        }

        measuring = true;
        try {
            current.holder.applyCss();
            double width = finiteSize(current.holder.prefWidth(-1.0));
            double height = finiteSize(current.holder.prefHeight(width));
            current.holder.resize(width, height);
            current.holder.layout();
            targetContentWidth = width;
            targetContentHeight = height;
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

        if (!sizeInitialized || getScene() == null || !isSizeAnimationEnabled()) {
            animatedContentWidth = targetContentWidth;
            animatedContentHeight = targetContentHeight;
            sizeInitialized = true;
            animation.resetSizeChannels();
            requestContainerLayout();
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
                alignedOffset(width, holderWidth, position.getHpos()),
                alignedOffset(height, holderHeight, position.getVpos())
        );
    }

    /// Completes lifecycle cleanup after every channel reaches its target.
    private void completeTransition() {
        clearOutgoingState();

        @Nullable HolderState current = currentState;
        if (current != null) {
            current.holder.setOpacity(1.0);
            current.holder.setScaleX(1.0);
            current.holder.setScaleY(1.0);
            current.resetChannelsToCurrentVisuals();
        }

        animatedContentWidth = targetContentWidth;
        animatedContentHeight = targetContentHeight;
        sizeInitialized = true;
        animation.resetSizeChannels();
        updateHolderOrder();
        transitioning.set(false);
        requestContainerLayout();
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

    /// Installs or removes the clip owned by the private viewport.
    private void updateViewportClip() {
        viewport.setClip(isClipContent() ? viewportClip : null);
    }

    /// Requests layout from this region and its parent after animated dimensions change.
    private void requestContainerLayout() {
        requestLayout();
        @Nullable javafx.scene.Parent parent = getParent();
        if (parent != null) {
            parent.requestLayout();
        }
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

    /// Validates a content scale.
    private static double validateScale(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and greater than zero");
        }
        return value;
    }

    /// Stores one reusable holder and its fixed visual animation channels.
    @NotNullByDefault
    private final class HolderState {
        /// The private wrapper that owns visual transition properties.
        private final StackPane holder = new StackPane();

        /// The holder opacity channel.
        private final M3ScalarChannel opacity = new M3ScalarChannel(OPACITY_VISIBILITY_THRESHOLD);

        /// The holder uniform-scale channel.
        private final M3ScalarChannel scale = new M3ScalarChannel(SCALE_VISIBILITY_THRESHOLD);

        /// Creates an empty inactive holder.
        private HolderState() {
            holder.getStyleClass().add(ITEM_STYLE_CLASS);
            holder.setManaged(false);
            holder.setPickOnBounds(false);
            holder.setVisible(false);
            holder.needsLayoutProperty().addListener(
                    (observable, oldValue, newValue) -> holderNeedsLayout(this, newValue)
            );
            resetVisuals(1.0, 1.0);
        }

        /// Returns the holder's content, or `null` when empty.
        private @Nullable Node content() {
            return holder.getChildren().isEmpty() ? null : holder.getChildren().get(0);
        }

        /// Installs one content node as the holder's only child.
        private void installContent(Node content) {
            holder.getChildren().setAll(Objects.requireNonNull(content, "content"));
            holder.setVisible(true);
            measurementPending = true;
        }

        /// Removes content and resets visual state without retaining the previous node.
        private void clearContent() {
            holder.getChildren().clear();
            holder.setVisible(false);
            holder.setMouseTransparent(true);
            resetVisuals(1.0, 1.0);
        }

        /// Immediately applies opacity and uniform scale and clears retained velocity.
        private void resetVisuals(double opacityValue, double scaleValue) {
            holder.setOpacity(opacityValue);
            holder.setScaleX(scaleValue);
            holder.setScaleY(scaleValue);
            opacity.reset(opacityValue);
            scale.reset(scaleValue);
        }

        /// Clears channel velocity while retaining the holder's current visual values.
        private void resetChannelsToCurrentVisuals() {
            opacity.reset(holder.getOpacity());
            scale.reset(holder.getScaleX());
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

            M3MotionSpec enterSpec = resolveEnterSpec();
            M3MotionSpec exitSpec = resolveExitSpec();
            M3MotionSpec sizeSpec = resolveSizeSpec();

            configureHolder(firstState, elapsedSeconds, enterSpec, exitSpec);
            configureHolder(secondState, elapsedSeconds, enterSpec, exitSpec);

            if (isSizeAnimationEnabled()) {
                width.configure(animatedContentWidth, targetContentWidth, sizeSpec, elapsedSeconds);
                height.configure(animatedContentHeight, targetContentHeight, sizeSpec, elapsedSeconds);
            } else {
                animatedContentWidth = targetContentWidth;
                animatedContentHeight = targetContentHeight;
                resetSizeChannels();
                requestContainerLayout();
            }

            stop();
            runDurationSeconds = Math.max(
                    Math.max(firstState.opacity.getDurationSeconds(), firstState.scale.getDurationSeconds()),
                    Math.max(
                            Math.max(secondState.opacity.getDurationSeconds(), secondState.scale.getDurationSeconds()),
                            Math.max(width.getDurationSeconds(), height.getDurationSeconds())
                    )
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
                M3MotionSpec enterSpec,
                M3MotionSpec exitSpec
        ) {
            if (state == currentState) {
                state.opacity.configure(state.holder.getOpacity(), 1.0, enterSpec, elapsedSeconds);
                state.scale.configure(state.holder.getScaleX(), 1.0, enterSpec, elapsedSeconds);
            } else if (state == outgoingState) {
                state.opacity.configure(state.holder.getOpacity(), 0.0, exitSpec, elapsedSeconds);
                state.scale.configure(state.holder.getScaleX(), getExitScale(), exitSpec, elapsedSeconds);
            } else {
                state.resetChannelsToCurrentVisuals();
            }
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
                requestContainerLayout();
            }
        }

        /// Applies the current opacity and scale values to one holder.
        private void applyHolder(HolderState state, double elapsedSeconds) {
            state.holder.setOpacity(Math.max(0.0, Math.min(1.0, state.opacity.valueAt(elapsedSeconds))));
            double scaleValue = state.scale.valueAt(elapsedSeconds);
            state.holder.setScaleX(scaleValue);
            state.holder.setScaleY(scaleValue);
        }

        /// Resets size channel history to the currently rendered dimensions.
        private void resetSizeChannels() {
            width.reset(animatedContentWidth);
            height.reset(animatedContentHeight);
        }

        /// Resolves the explicit or theme-derived enter specification.
        private M3MotionSpec resolveEnterSpec() {
            @Nullable M3MotionSpec explicit = getEnterMotionSpec();
            return explicit == null ? M3Animation.defaultEffects(M3AnimatedContent.this) : explicit;
        }

        /// Resolves the explicit or theme-derived exit specification.
        private M3MotionSpec resolveExitSpec() {
            @Nullable M3MotionSpec explicit = getExitMotionSpec();
            return explicit == null ? M3Animation.fastEffects(M3AnimatedContent.this) : explicit;
        }

        /// Resolves the explicit or theme-derived size specification.
        private M3MotionSpec resolveSizeSpec() {
            @Nullable M3MotionSpec explicit = getSizeMotionSpec();
            return explicit == null ? M3Animation.defaultSpatial(M3AnimatedContent.this) : explicit;
        }
    }

}
