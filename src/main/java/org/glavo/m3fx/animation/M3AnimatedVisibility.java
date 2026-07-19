// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents or removes one retained content node with an interruptible Material visibility transition.
///
/// The [showing property][#showingProperty()] is the requested target. The read-only [state property][#stateProperty()]
/// reports whether content is hidden, entering, visible, or exiting. Content remains attached while an exit is in
/// progress and is detached after all visual and size channels complete. The [content property][#contentProperty()]
/// retains the node reference while hidden, so the same node may be shown again without reconstruction.
///
/// Opacity and scale are applied to private holders. This region does not modify the content node's own opacity,
/// scale, translation, or transform list. By default, preferred and minimum size animate between the content's
/// measured size and zero, and drawing is clipped to the current region bounds. Both policies are configurable.
/// Replacing [#getContent()] is immediate and is not treated as an animated content transformation; use
/// [M3AnimatedContent] when old and new content should coexist during replacement.
///
/// Reversing [#showingProperty()] during playback continues from the current visual values and reuses the attached
/// content node. Reduced motion and scene detachment during a run settle synchronously at the newest target.
/// Changing the inherited [Node#visibleProperty()] or [Node#managedProperty()] is independent of this lifecycle and
/// may prevent the region from being rendered or laid out.
///
/// This class is a layout container rather than a Material component and does not install a user-agent stylesheet.
/// Its public properties and animation-control methods must be accessed on the JavaFX Application Thread once the
/// node is attached to a showing scene.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3AnimatedVisibility extends Region {
    /// The default style class assigned to animated-visibility regions.
    private static final String DEFAULT_STYLE_CLASS = "m3-animated-visibility";

    /// The default alignment of visible and exiting content.
    private static final Pos DEFAULT_ALIGNMENT = Pos.CENTER;

    /// The scale applied at the hidden end of an enter or exit transition.
    private static final double DEFAULT_HIDDEN_SCALE = 0.96;

    /// The retained-content engine that owns visual effects, clipping, and animated size.
    private final M3AnimatedContent animatedContent = new M3AnimatedContent();

    /// Whether the current retained-content run represents a visibility lifecycle transition.
    private boolean lifecycleTransitionActive;

    /// The content retained by this region, or `null` when it is empty.
    private final ObjectProperty<@Nullable Node> content =
            new SimpleObjectProperty<>(this, "content") {
                /// Applies content replacement without a replacement animation.
                @Override
                protected void invalidated() {
                    replaceContent(get());
                }
            };

    /// Returns the content retained by this region.
    ///
    /// Hidden content is retained by this property but is detached from this region's private scene graph.
    ///
    /// @return the current content, or `null` if this region is empty
    public @Nullable Node getContent() {
        return content.get();
    }

    /// Replaces the content retained by this region.
    ///
    /// Replacement is immediate. If the region is showing, the new node becomes fully visible without an enter
    /// transition; if it is hidden, the node remains detached until [#setShowing(boolean)] requests entry. The node
    /// must not be this region, one of its ancestors, or a node that cannot legally be reparented into this region.
    ///
    /// @param content the new content, or `null` to make this region empty
    /// @throws IllegalArgumentException if adding the node would create a scene-graph cycle or otherwise violate
    ///                                  JavaFX child-list constraints
    public void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable retained-content property.
    ///
    /// A value assigned through a binding has the same scene-graph constraints as [#setContent(Node)].
    ///
    /// @return the content property, whose value may be `null`
    public ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Whether the content's requested target is shown.
    private final BooleanProperty showing =
            new SimpleBooleanProperty(this, "showing", true) {
                /// Starts or reverses the visibility lifecycle after the target changes.
                @Override
                protected void invalidated() {
                    animateToShowingState(get());
                }
            };

    /// Returns whether the requested target is shown.
    ///
    /// This value becomes `true` at the start of entry and `false` at the start of exit. Use [#getState()] when the
    /// current visual lifecycle state is required.
    ///
    /// @return `true` when shown content is the current target
    public boolean isShowing() {
        return showing.get();
    }

    /// Sets whether the content should be shown.
    ///
    /// Changing this value starts an enter or exit transition when content is available, this node is attached to a
    /// scene, and motion is enabled. Reversing the value during playback preserves current visual progress. Repeating
    /// the current value has no effect.
    ///
    /// @param showing whether the content should be shown
    public void setShowing(boolean showing) {
        this.showing.set(showing);
    }

    /// Returns the observable showing-target property.
    ///
    /// @return the showing-target property, initially `true`
    public BooleanProperty showingProperty() {
        return showing;
    }

    /// The scale used at the hidden end of the visual transition.
    private final DoubleProperty hiddenScale =
            new SimpleDoubleProperty(this, "hiddenScale", DEFAULT_HIDDEN_SCALE) {
                /// Validates and applies a hidden-state scale change.
                @Override
                protected void invalidated() {
                    double value = validateHiddenScale(get());
                    animatedContent.setEnterScale(value);
                    animatedContent.setExitScale(value);
                }
            };

    /// Returns the scale used at the hidden end of the visual transition.
    ///
    /// @return the finite, positive hidden-state scale
    public double getHiddenScale() {
        return hiddenScale.get();
    }

    /// Sets the scale used at the hidden end of the visual transition.
    ///
    /// A value below `1.0` makes content grow as it enters; a value above `1.0` makes it shrink. The value affects
    /// only a private holder and never changes the content node's own scale properties.
    ///
    /// @param hiddenScale the finite, positive hidden-state scale
    /// @throws IllegalArgumentException if the value is not finite and greater than zero
    public void setHiddenScale(double hiddenScale) {
        this.hiddenScale.set(validateHiddenScale(hiddenScale));
    }

    /// Returns the observable hidden-state scale property.
    ///
    /// The default value is `0.96`. Values supplied through a binding must be finite and greater than zero.
    ///
    /// @return the hidden-state scale property
    public DoubleProperty hiddenScaleProperty() {
        return hiddenScale;
    }

    /// The alignment of attached content within this region.
    private final ObjectProperty<@Nullable Pos> alignment =
            new SimpleObjectProperty<>(this, "alignment", DEFAULT_ALIGNMENT) {
                /// Restores the default after a direct null assignment and updates the retained-content engine.
                @Override
                protected void invalidated() {
                    @Nullable Pos value = get();
                    if (value == null) {
                        set(DEFAULT_ALIGNMENT);
                    } else {
                        animatedContent.setAlignment(value);
                    }
                }
            };

    /// Returns the alignment of attached content within this region.
    ///
    /// @return the non-null content alignment
    public Pos getAlignment() {
        return Objects.requireNonNull(alignment.get(), "alignment");
    }

    /// Sets the alignment of attached content within this region.
    ///
    /// @param alignment the content alignment
    /// @throws NullPointerException if `alignment` is `null`
    public void setAlignment(Pos alignment) {
        this.alignment.set(Objects.requireNonNull(alignment, "alignment"));
    }

    /// Returns the observable content-alignment property.
    ///
    /// The default is [Pos#CENTER]. A `null` value assigned directly through this property is replaced with the
    /// default.
    ///
    /// @return the content-alignment property
    public ObjectProperty<@Nullable Pos> alignmentProperty() {
        return alignment;
    }

    /// Whether preferred and minimum size animate between content size and zero.
    private final BooleanProperty sizeAnimationEnabled =
            new SimpleBooleanProperty(this, "sizeAnimationEnabled", true) {
                /// Applies the size-animation policy to the retained-content engine.
                @Override
                protected void invalidated() {
                    animatedContent.setSizeAnimationEnabled(get());
                }
            };

    /// Returns whether visibility changes animate this region's content size.
    ///
    /// @return `true` when preferred and minimum size move toward their target values
    public boolean isSizeAnimationEnabled() {
        return sizeAnimationEnabled.get();
    }

    /// Sets whether visibility changes should animate this region's content size.
    ///
    /// Disabling this property during playback applies the target size synchronously while allowing opacity and
    /// scale effects to finish.
    ///
    /// @param enabled whether content-size changes should be animated
    public void setSizeAnimationEnabled(boolean enabled) {
        sizeAnimationEnabled.set(enabled);
    }

    /// Returns the observable size-animation policy property.
    ///
    /// @return the size-animation policy property, initially `true`
    public BooleanProperty sizeAnimationEnabledProperty() {
        return sizeAnimationEnabled;
    }

    /// Whether attached content is clipped to this region's current bounds.
    private final BooleanProperty clipContent =
            new SimpleBooleanProperty(this, "clipContent", true) {
                /// Applies the clipping policy to the retained-content engine.
                @Override
                protected void invalidated() {
                    animatedContent.setClipContent(get());
                }
            };

    /// Returns whether content drawing is clipped to this region.
    ///
    /// @return `true` when entering and exiting content is clipped
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

    /// The explicit visibility motion specification, or `null` to use the theme default spatial role.
    private final ObjectProperty<@Nullable M3MotionSpec> motionSpec =
            new SimpleObjectProperty<>(this, "motionSpec");

    /// Returns the explicit motion specification used by visibility transitions.
    ///
    /// @return the explicit specification, or `null` when the active theme's default spatial role is used
    public @Nullable M3MotionSpec getMotionSpec() {
        return motionSpec.get();
    }

    /// Sets the motion specification used by subsequent visibility transitions.
    ///
    /// Passing `null` restores semantic resolution through the active theme. Changing this property does not restart
    /// a transition that is already running.
    ///
    /// @param motionSpec the explicit specification, or `null` to use the active theme
    public void setMotionSpec(@Nullable M3MotionSpec motionSpec) {
        this.motionSpec.set(motionSpec);
    }

    /// Returns the observable explicit motion-specification property.
    ///
    /// @return the motion-specification property, whose value may be `null`
    public ObjectProperty<@Nullable M3MotionSpec> motionSpecProperty() {
        return motionSpec;
    }

    /// The current visual lifecycle state.
    private final ReadOnlyObjectWrapper<M3VisibilityState> state =
            new ReadOnlyObjectWrapper<>(this, "state", M3VisibilityState.HIDDEN);

    /// Returns the current visual lifecycle state.
    ///
    /// @return the current non-null lifecycle state
    public M3VisibilityState getState() {
        return state.get();
    }

    /// Returns the read-only visual lifecycle state property.
    ///
    /// @return the lifecycle state property
    public ReadOnlyObjectProperty<M3VisibilityState> stateProperty() {
        return state.getReadOnlyProperty();
    }

    /// Whether an enter, exit, or associated size channel is active.
    private final ReadOnlyBooleanWrapper transitioning =
            new ReadOnlyBooleanWrapper(this, "transitioning", false);

    /// Returns whether a visibility or associated size transition is currently running.
    ///
    /// @return `true` while one or more visual or size channels are moving toward their targets
    public boolean isTransitioning() {
        return transitioning.get();
    }

    /// Returns the read-only transition-status property.
    ///
    /// @return the transition-status property
    public ReadOnlyBooleanProperty transitioningProperty() {
        return transitioning.getReadOnlyProperty();
    }

    /// Creates an empty animated-visibility region whose showing target is initially enabled.
    public M3AnimatedVisibility() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        setPickOnBounds(false);

        animatedContent.setAlignment(DEFAULT_ALIGNMENT);
        animatedContent.setEnterScale(DEFAULT_HIDDEN_SCALE);
        animatedContent.setExitScale(DEFAULT_HIDDEN_SCALE);
        getChildren().add(animatedContent);

        animatedContent.transitioningProperty().addListener((observable, wasTransitioning, isNowTransitioning) -> {
            transitioning.set(isNowTransitioning);
            if (!isNowTransitioning && lifecycleTransitionActive) {
                settleLifecycleState();
            }
        });
    }

    /// Creates an animated-visibility region for content that is initially visible.
    ///
    /// @param content the initial content
    /// @throws NullPointerException     if `content` is `null`
    /// @throws IllegalArgumentException if the node cannot legally be added to this region
    public M3AnimatedVisibility(Node content) {
        this();
        setContent(Objects.requireNonNull(content, "content"));
    }

    /// Completes an active visibility or size transition at its current target.
    ///
    /// This method has no effect when no transition is active. Completion is synchronous; after an exit, the content
    /// node is detached and [#getState()] returns [M3VisibilityState#HIDDEN] before this method returns.
    public void finish() {
        if (animatedContent.isTransitioning()) {
            animatedContent.finish();
        } else if (lifecycleTransitionActive) {
            settleLifecycleState();
        }
    }

    /// Immediately applies the current showing target without running a transition.
    ///
    /// An active transition is stopped. Hidden content is detached and the region reports zero content size before
    /// this method returns. Shown content is attached at full opacity and scale. Repeated calls are idempotent.
    public void snapToCurrentState() {
        lifecycleTransitionActive = false;
        @Nullable Node target = isShowing() ? getContent() : null;
        animatedContent.setContent(target);
        animatedContent.snapToCurrentState();
        transitioning.set(false);
        state.set(target == null ? M3VisibilityState.HIDDEN : M3VisibilityState.VISIBLE);
        requestContainerLayout();
    }

    /// Returns the baseline offset of attached content at its current alignment.
    ///
    /// @return the content baseline relative to this region, or [Node#BASELINE_OFFSET_SAME_AS_HEIGHT] when none is
    ///         available
    @Override
    public double getBaselineOffset() {
        double baseline = animatedContent.getBaselineOffset();
        return baseline == Node.BASELINE_OFFSET_SAME_AS_HEIGHT
                ? baseline
                : snappedTopInset() + baseline;
    }

    /// Returns the animated minimum width including snapped insets.
    @Override
    protected double computeMinWidth(double height) {
        return horizontalInsets() + animatedContent.minWidth(contentHeight(height));
    }

    /// Returns the animated minimum height including snapped insets.
    @Override
    protected double computeMinHeight(double width) {
        return verticalInsets() + animatedContent.minHeight(contentWidth(width));
    }

    /// Returns the animated preferred width including snapped insets.
    @Override
    protected double computePrefWidth(double height) {
        return horizontalInsets() + animatedContent.prefWidth(contentHeight(height));
    }

    /// Returns the animated preferred height including snapped insets.
    @Override
    protected double computePrefHeight(double width) {
        return verticalInsets() + animatedContent.prefHeight(contentWidth(width));
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

    /// Lays out the retained-content engine inside this region's snapped insets.
    @Override
    protected void layoutChildren() {
        double left = snappedLeftInset();
        double top = snappedTopInset();
        animatedContent.resizeRelocate(
                left,
                top,
                Math.max(0.0, getWidth() - left - snappedRightInset()),
                Math.max(0.0, getHeight() - top - snappedBottomInset())
        );
    }

    /// Replaces the retained node without treating replacement as a visibility transition.
    private void replaceContent(@Nullable Node newContent) {
        lifecycleTransitionActive = false;
        animatedContent.setContent(isShowing() ? newContent : null);
        animatedContent.snapToCurrentState();
        transitioning.set(false);
        state.set(isShowing() && newContent != null ? M3VisibilityState.VISIBLE : M3VisibilityState.HIDDEN);
        requestContainerLayout();
    }

    /// Starts, reverses, or synchronously settles toward one showing target.
    private void animateToShowingState(boolean shown) {
        @Nullable Node retainedContent = getContent();
        if (retainedContent == null) {
            snapToCurrentState();
            return;
        }

        configureMotion();
        lifecycleTransitionActive = true;
        state.set(shown ? M3VisibilityState.ENTERING : M3VisibilityState.EXITING);
        animatedContent.setContent(shown ? retainedContent : null);

        if (!animatedContent.isTransitioning()) {
            settleLifecycleState();
        }
    }

    /// Applies the current motion and hidden-scale configuration to the retained-content engine.
    private void configureMotion() {
        double scale = getHiddenScale();
        animatedContent.setEnterScale(scale);
        animatedContent.setExitScale(scale);

        @Nullable M3MotionSpec explicitSpec = getMotionSpec();
        M3MotionSpec resolvedSpec = explicitSpec == null ? M3Animation.defaultSpatial(this) : explicitSpec;
        animatedContent.setEnterMotionSpec(resolvedSpec);
        animatedContent.setExitMotionSpec(resolvedSpec);
        animatedContent.setSizeMotionSpec(resolvedSpec);
    }

    /// Completes lifecycle bookkeeping after all retained-content channels settle.
    private void settleLifecycleState() {
        lifecycleTransitionActive = false;
        @Nullable Node target = isShowing() ? getContent() : null;
        if (animatedContent.getContent() != target) {
            animatedContent.setContent(target);
            animatedContent.snapToCurrentState();
        }
        transitioning.set(animatedContent.isTransitioning());
        state.set(target == null ? M3VisibilityState.HIDDEN : M3VisibilityState.VISIBLE);
        requestContainerLayout();
    }

    /// Requests layout from this region and its parent after the animated content size changes.
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

    /// Converts an outer height constraint to the retained-content constraint.
    private double contentHeight(double height) {
        return height < 0.0 ? height : Math.max(0.0, height - verticalInsets());
    }

    /// Converts an outer width constraint to the retained-content constraint.
    private double contentWidth(double width) {
        return width < 0.0 ? width : Math.max(0.0, width - horizontalInsets());
    }

    /// Validates a hidden-state scale.
    private static double validateHiddenScale(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException("hiddenScale must be finite and greater than zero");
        }
        return value;
    }
}
