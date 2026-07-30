// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents or hides one content node with an interruptible Material visibility transition.
///
/// The [showing property][#showingProperty()] is the requested target. The read-only [state property][#stateProperty()]
/// reports whether content is hidden, entering, visible, or exiting. The [content property][#contentProperty()]
/// continues to identify the configured node while it is hidden, so the same node may be shown again.
///
/// This region does not modify the content node's own opacity, scale, translation, or transform list. By default,
/// preferred and minimum size animate between the content's measured size and zero, and drawing is clipped to the
/// current region bounds. The enter, exit, and size transforms are independently configurable.
/// Set [#fitToWidthProperty()] or [#fitToHeightProperty()] when a constraining parent must fit retained content to
/// an assigned dimension instead of preserving its independent preferred size.
/// Replacing [#getContent()] is immediate and is not treated as an animated content transformation; use
/// [M3AnimatedContent] when old and new content should coexist during replacement.
///
/// Reversing [#showingProperty()] during playback continues from the current visual values. Reduced motion and a
/// change in the node's presentation context settle at the newest target. Changing [Node#visibleProperty()] or
/// [Node#managedProperty()] is independent of this lifecycle and may prevent the region from being rendered or laid
/// out.
///
/// This class is a layout container rather than a Material component and does not install a user-agent stylesheet.
/// Its public properties and animation-control methods must be accessed on the JavaFX Application Thread once the
/// node is attached to a showing scene.
///
/// See [Material Design motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public final class M3AnimatedVisibility extends Region {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-animated-visibility";

    /// The default alignment of visible and exiting content.
    private static final Pos DEFAULT_ALIGNMENT = Pos.CENTER;

    /// The default enter effects used by visibility changes.
    private static final M3EnterTransition DEFAULT_ENTER_TRANSITION =
            M3EnterTransition.fade(0.0).and(M3EnterTransition.scale(0.96));

    /// The default exit effects used by visibility changes.
    private static final M3ExitTransition DEFAULT_EXIT_TRANSITION =
            M3ExitTransition.fade(0.0).and(M3ExitTransition.scale(0.96));

    /// The default animated and clipped size behavior.
    private static final M3SizeTransform DEFAULT_SIZE_TRANSFORM = new M3SizeTransform(true, null);

    /// The child region that applies visual effects, clipping, and animated size.
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

    /// Returns the content configured for this region.
    ///
    /// @return the current content, or `null` if this region is empty
    public @Nullable Node getContent() {
        return content.get();
    }

    /// Replaces the content configured for this region.
///
/// Replacement is immediate. If the region is showing, the new node becomes fully visible without an enter
    /// transition; if it is hidden, the node remains hidden until [#setShowing(boolean)] requests entry. The node
    /// must not be this region or one of its ancestors, and it must satisfy the JavaFX single-parent rule.
    ///
    /// @param content the new content, or `null` to make this region empty
    /// @throws IllegalArgumentException if adding the node would create a scene-graph cycle or otherwise violate
    ///                                  JavaFX child-list constraints
    public void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable content property.
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

    /// The effects used while content enters.
    private final ObjectProperty<@Nullable M3EnterTransition> enterTransition =
            new SimpleObjectProperty<>(this, "enterTransition", DEFAULT_ENTER_TRANSITION) {
                /// Restores the default after a direct null assignment and updates active motion otherwise.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_ENTER_TRANSITION);
                    } else {
                        configureMotion();
                    }
                }
            };

    /// Returns the effects used while content enters.
    ///
    /// @return the non-null immutable enter transition
    public M3EnterTransition getEnterTransition() {
        return Objects.requireNonNull(enterTransition.get(), "enterTransition");
    }

    /// Sets the effects used by subsequent and active enter transitions.
    ///
    /// @param transition the immutable enter transition
    /// @throws NullPointerException if `transition` is `null`
    public void setEnterTransition(M3EnterTransition transition) {
        enterTransition.set(Objects.requireNonNull(transition, "transition"));
    }

    /// Returns the observable enter-transition property.
    ///
    /// A `null` value assigned directly through the property is replaced with the default fade-and-scale transition.
    ///
    /// @return the enter-transition property
    public ObjectProperty<@Nullable M3EnterTransition> enterTransitionProperty() {
        return enterTransition;
    }

    /// The effects used while content exits.
    private final ObjectProperty<@Nullable M3ExitTransition> exitTransition =
            new SimpleObjectProperty<>(this, "exitTransition", DEFAULT_EXIT_TRANSITION) {
                /// Restores the default after a direct null assignment and updates active motion otherwise.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_EXIT_TRANSITION);
                    } else {
                        configureMotion();
                    }
                }
            };

    /// Returns the effects used while content exits.
    ///
    /// @return the non-null immutable exit transition
    public M3ExitTransition getExitTransition() {
        return Objects.requireNonNull(exitTransition.get(), "exitTransition");
    }

    /// Sets the effects used by subsequent and active exit transitions.
    ///
    /// @param transition the immutable exit transition
    /// @throws NullPointerException if `transition` is `null`
    public void setExitTransition(M3ExitTransition transition) {
        exitTransition.set(Objects.requireNonNull(transition, "transition"));
    }

    /// Returns the observable exit-transition property.
    ///
    /// A `null` value assigned directly through the property is replaced with the default fade-and-scale transition.
    ///
    /// @return the exit-transition property
    public ObjectProperty<@Nullable M3ExitTransition> exitTransitionProperty() {
        return exitTransition;
    }

    /// The alignment of content within this region.
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

    /// Returns the alignment of content within this region.
    ///
    /// @return the non-null content alignment
    public Pos getAlignment() {
        return Objects.requireNonNull(alignment.get(), "alignment");
    }

    /// Sets the alignment of content within this region.
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

    /// Whether a positive assigned width constrains content measurement.
    ///
    /// When `false`, the default, this region measures visible content at its independent preferred width. When
    /// `true`, a positive assigned width constrains content measurement and allows ordinary resizable content
    /// to reflow. This is useful inside a width-constraining parent such as [javafx.scene.control.ScrollPane].
    private final BooleanProperty fitToWidth = new SimpleBooleanProperty(this, "fitToWidth", false) {
        /// Synchronizes the retained-content engine after the outer measurement contract changes.
        @Override
        protected void invalidated() {
            animatedContent.setFitToWidth(get());
            requestLayout();
        }
    };

    /// Returns whether content is fitted to this region's assigned width.
    ///
    /// @return `true` when a positive assigned width constrains visible content
    public boolean isFitToWidth() {
        return fitToWidth.get();
    }

    /// Sets whether content is fitted to this region's assigned width.
    ///
    /// Changing this value requests a target remeasurement. It does not alter the configured enter or exit effects
    /// and may retarget an active size transition when the measured content height changes.
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

    /// Whether a positive assigned height constrains content measurement.
    ///
    /// When `false`, the default, this region measures visible content at its independent preferred height. When
    /// `true`, a positive assigned height constrains content measurement and allows ordinary resizable content to fit
    /// a height-constraining parent.
    private final BooleanProperty fitToHeight = new SimpleBooleanProperty(this, "fitToHeight", false) {
        /// Synchronizes the retained-content engine after the outer measurement contract changes.
        @Override
        protected void invalidated() {
            animatedContent.setFitToHeight(get());
            requestLayout();
        }
    };

    /// Returns whether content is fitted to this region's assigned height.
    ///
    /// @return `true` when a positive assigned height constrains retained content
    public boolean isFitToHeight() {
        return fitToHeight.get();
    }

    /// Sets whether content is fitted to this region's assigned height.
    ///
    /// Changing this value requests a target remeasurement. It does not alter the configured enter or exit effects
    /// and may retarget an active size transition when the measured content height changes.
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

    /// The animated size and clipping behavior, or `null` for synchronous un-clipped size changes.
    private final ObjectProperty<@Nullable M3SizeTransform> sizeTransform =
            new SimpleObjectProperty<>(this, "sizeTransform", DEFAULT_SIZE_TRANSFORM) {
                /// Updates active and subsequent visibility motion after the size behavior changes.
                @Override
                protected void invalidated() {
                    configureMotion();
                }
            };

    /// Returns the size behavior used by visibility transitions.
    ///
    /// @return the immutable size transform, or `null` when size changes synchronously without clipping
    public @Nullable M3SizeTransform getSizeTransform() {
        return sizeTransform.get();
    }

    /// Sets the size behavior used by subsequent and active visibility transitions.
    ///
    /// Passing `null` applies the target size synchronously without stopping active enter or exit effects.
    ///
    /// @param sizeTransform the immutable size behavior, or `null` to disable it
    public void setSizeTransform(@Nullable M3SizeTransform sizeTransform) {
        this.sizeTransform.set(sizeTransform);
    }

    /// Returns the observable size-transform property.
    ///
    /// @return the size-transform property, whose value may be `null`
    public ObjectProperty<@Nullable M3SizeTransform> sizeTransformProperty() {
        return sizeTransform;
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
        configureMotion();
        getChildren().add(animatedContent);

        // Preserve a size invalidation raised while this wrapper is already laying out the retained-content engine.
        animatedContent.needsLayoutProperty().addListener((observable, wasNeeded, isNeeded) -> {
            if (isNeeded) {
                requestLayout();
            }
        });
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
    /// @throws IllegalArgumentException if the node would create a scene-graph cycle or violate the JavaFX
    ///                                  single-parent rule
    public M3AnimatedVisibility(Node content) {
        this();
        setContent(Objects.requireNonNull(content, "content"));
    }

    /// Completes an active visibility or size transition at its current target.
    ///
    /// This method has no effect when no transition is active. Completion is synchronous; after an exit,
    /// [#getState()] returns [M3VisibilityState#HIDDEN] before this method returns.
    public void finish() {
        if (animatedContent.isTransitioning()) {
            animatedContent.finish();
        } else if (lifecycleTransitionActive) {
            settleLifecycleState();
        }
    }

    /// Immediately applies the current showing target without running a transition.
    ///
    /// An active transition is stopped. Hidden content contributes zero content size before this method returns.
    /// Shown content uses neutral opacity, scale, and translation. Repeated calls are idempotent.
    public void snapToCurrentState() {
        lifecycleTransitionActive = false;
        @Nullable Node target = isShowing() ? getContent() : null;
        animatedContent.setContent(target);
        animatedContent.snapToCurrentState();
        transitioning.set(false);
        state.set(target == null ? M3VisibilityState.HIDDEN : M3VisibilityState.VISIBLE);
        requestLayout();
    }

    /// Returns the baseline offset of content at its current alignment.
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

    /// Lays out content inside this region's snapped insets.
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
        requestLayout();
    }

    /// Starts, reverses, or synchronously settles toward one showing target.
    private void animateToShowingState(boolean shown) {
        @Nullable Node retainedContent = getContent();
        if (retainedContent == null) {
            snapToCurrentState();
            return;
        }

        lifecycleTransitionActive = true;
        state.set(shown ? M3VisibilityState.ENTERING : M3VisibilityState.EXITING);
        animatedContent.setContent(shown ? retainedContent : null);

        if (!animatedContent.isTransitioning()) {
            settleLifecycleState();
        }
    }

    /// Applies the current enter, exit, and size configuration to the retained-content engine.
    private void configureMotion() {
        M3ContentTransform transform = new M3ContentTransform(
                getEnterTransition(),
                getExitTransition(),
                getSizeTransform(),
                0.0
        );
        if (!transform.equals(animatedContent.getContentTransform())) {
            animatedContent.setContentTransform(transform);
        }
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
        requestLayout();
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

}
