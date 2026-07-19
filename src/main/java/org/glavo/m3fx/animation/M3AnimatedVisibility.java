// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Presents or removes one content node with an interruptible Material visibility transition.
///
/// The content is hosted in a private layout wrapper, so the transition does not take ownership of the content
/// node's opacity, scale, or translation properties. Showing content reserves its normal layout size immediately;
/// after an exit transition completes, this region reports zero content size. Sibling repositioning caused by that
/// size change can be animated independently with [M3LayoutTransition].
///
/// A transition that is interrupted by a change to [#showingProperty()] continues from the current visual values.
/// If reduced motion is requested for this node before or during playback, the current state settles
/// synchronously. A transition also settles if this node leaves the scene in which that run started. Changing the
/// inherited [Node#visibleProperty()] or [Node#managedProperty()] is independent of the showing state and may prevent
/// this region from being rendered or laid out.
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

    /// The scale applied after content has fully exited.
    private static final double DEFAULT_HIDDEN_SCALE = 0.96;

    /// The private wrapper whose visual properties are owned by this transition.
    private final StackPane contentHolder = new StackPane();

    /// The reusable transition for the holder's opacity and scale channels.
    private final M3NodeTransition visibilityTransition = new M3NodeTransition(contentHolder);

    /// Whether this region currently contributes its content size to layout calculations.
    private boolean layoutPresent = true;

    /// The content displayed by this region, or `null` when it is empty.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// Returns the content displayed by this region.
    ///
    /// @return the current content, or `null` if this region is empty
    public @Nullable Node getContent() {
        return content.get();
    }

    /// Replaces the content displayed by this region.
    ///
    /// The replacement is immediate and does not itself run a content-change transition. The node must not be this
    /// region, one of its ancestors, or a node that cannot legally be reparented into this region.
    ///
    /// @param content the new content, or `null` to make this region empty
    /// @throws IllegalArgumentException if adding the node would create a scene-graph cycle or otherwise violate
    ///                                  JavaFX child-list constraints
    public void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable content property.
    ///
    /// @return the content property
    public ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Whether the content's target state is shown.
    private final BooleanProperty showing = new SimpleBooleanProperty(this, "showing", true);

    /// Returns whether the content's target state is shown.
    ///
    /// @return `true` when the content is showing or entering
    public boolean isShowing() {
        return showing.get();
    }

    /// Sets whether the content should be shown.
    ///
    /// Changing this value starts an enter or exit transition when the node is attached to a scene and motion is
    /// enabled. Repeating the current value has no effect.
    ///
    /// @param showing whether the content should be shown
    public void setShowing(boolean showing) {
        this.showing.set(showing);
    }

    /// Returns the observable showing-state property.
    ///
    /// @return the showing-state property
    public BooleanProperty showingProperty() {
        return showing;
    }

    /// The scale used by the fully hidden visual state.
    private final DoubleProperty hiddenScale =
            new SimpleDoubleProperty(this, "hiddenScale", DEFAULT_HIDDEN_SCALE) {
                /// Validates and applies a hidden-state scale change.
                @Override
                protected void invalidated() {
                    validateHiddenScale(get());
                    if (!isShowing()) {
                        if (isTransitioning()) {
                            animateToShowingState(false);
                        } else {
                            contentHolder.setScaleX(get());
                            contentHolder.setScaleY(get());
                        }
                    }
                }
            };

    /// Returns the scale used by the fully hidden visual state.
    ///
    /// @return the finite, positive hidden-state scale
    public double getHiddenScale() {
        return hiddenScale.get();
    }

    /// Sets the scale used by the fully hidden visual state.
    ///
    /// A value below `1.0` makes content grow as it enters; a value above `1.0` makes it shrink. The value affects
    /// only the private content wrapper and never changes the content node's own scale properties.
    ///
    /// @param hiddenScale the finite, positive hidden-state scale
    /// @throws IllegalArgumentException if the value is not finite and greater than zero
    public void setHiddenScale(double hiddenScale) {
        this.hiddenScale.set(validateHiddenScale(hiddenScale));
    }

    /// Returns the observable hidden-state scale property.
    ///
    /// The default value is `0.96`. Values must be finite and greater than zero, including values supplied through
    /// a binding.
    ///
    /// @return the hidden-state scale property
    public DoubleProperty hiddenScaleProperty() {
        return hiddenScale;
    }

    /// The explicit visibility motion specification, or `null` to inherit the default spatial role.
    private final ObjectProperty<@Nullable M3MotionSpec> motionSpec =
            new SimpleObjectProperty<>(this, "motionSpec");

    /// Returns the explicit motion specification used for enter and exit transitions.
    ///
    /// @return the explicit specification, or `null` when the active theme's default spatial role is used
    public @Nullable M3MotionSpec getMotionSpec() {
        return motionSpec.get();
    }

    /// Sets the motion specification used for subsequent enter and exit transitions.
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

    /// Whether an enter or exit transition is currently running.
    private final ReadOnlyBooleanWrapper transitioning =
            new ReadOnlyBooleanWrapper(this, "transitioning", false);

    /// Returns whether an enter or exit transition is currently running.
    ///
    /// @return `true` while the visual state is moving toward the showing state
    public boolean isTransitioning() {
        return transitioning.get();
    }

    /// Returns the read-only transition-status property.
    ///
    /// @return the transition-status property
    public ReadOnlyBooleanProperty transitioningProperty() {
        return transitioning.getReadOnlyProperty();
    }

    /// Creates an empty animated-visibility region whose content is initially shown.
    public M3AnimatedVisibility() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        setPickOnBounds(false);
        contentHolder.setAlignment(Pos.CENTER);
        getChildren().add(contentHolder);

        content.addListener((observable, oldContent, newContent) -> updateContent(newContent));
        showing.addListener((observable, wasShowing, isNowShowing) -> animateToShowingState(isNowShowing));
        visibilityTransition.setOnFinished(event -> completeVisibilityTransition());
    }

    /// Creates an animated-visibility region for content that is initially shown.
    ///
    /// @param content the initial content
    /// @throws NullPointerException     if `content` is `null`
    /// @throws IllegalArgumentException if the node cannot legally be added to this region
    public M3AnimatedVisibility(Node content) {
        this();
        setContent(java.util.Objects.requireNonNull(content, "content"));
    }

    /// Immediately applies the current showing state without running a transition.
    ///
    /// An active transition is stopped. If the target is hidden, the content ceases to contribute to this region's
    /// preferred and minimum size before this method returns. Repeating this method is idempotent.
    public void snapToCurrentState() {
        visibilityTransition.stop();
        transitioning.set(false);

        boolean shown = isShowing();
        layoutPresent = shown;
        contentHolder.setManaged(shown);
        contentHolder.setVisible(shown);
        contentHolder.setOpacity(shown ? 1.0 : 0.0);
        double scale = shown ? 1.0 : getHiddenScale();
        contentHolder.setScaleX(scale);
        contentHolder.setScaleY(scale);
        requestContainerLayout();
    }

    /// Returns the baseline offset of the hosted content.
    ///
    /// @return the content baseline relative to this region, or [Node#BASELINE_OFFSET_SAME_AS_HEIGHT]
    ///         when the content has no baseline
    @Override
    public double getBaselineOffset() {
        if (!layoutPresent) {
            return Node.BASELINE_OFFSET_SAME_AS_HEIGHT;
        }
        double baseline = contentHolder.getBaselineOffset();
        return baseline == Node.BASELINE_OFFSET_SAME_AS_HEIGHT
                ? baseline
                : snappedTopInset() + baseline;
    }

    /// Returns the minimum width required by the visible or transitioning content.
    @Override
    protected double computeMinWidth(double height) {
        return horizontalInsets() + (layoutPresent ? contentHolder.minWidth(contentHeight(height)) : 0.0);
    }

    /// Returns the minimum height required by the visible or transitioning content.
    @Override
    protected double computeMinHeight(double width) {
        return verticalInsets() + (layoutPresent ? contentHolder.minHeight(contentWidth(width)) : 0.0);
    }

    /// Returns the preferred width required by the visible or transitioning content.
    @Override
    protected double computePrefWidth(double height) {
        return horizontalInsets() + (layoutPresent ? contentHolder.prefWidth(contentHeight(height)) : 0.0);
    }

    /// Returns the preferred height required by the visible or transitioning content.
    @Override
    protected double computePrefHeight(double width) {
        return verticalInsets() + (layoutPresent ? contentHolder.prefHeight(contentWidth(width)) : 0.0);
    }

    /// Returns the maximum width accepted by the hosted content.
    @Override
    protected double computeMaxWidth(double height) {
        return layoutPresent
                ? addInsetsWithoutOverflow(contentHolder.maxWidth(contentHeight(height)), horizontalInsets())
                : horizontalInsets();
    }

    /// Returns the maximum height accepted by the hosted content.
    @Override
    protected double computeMaxHeight(double width) {
        return layoutPresent
                ? addInsetsWithoutOverflow(contentHolder.maxHeight(contentWidth(width)), verticalInsets())
                : verticalInsets();
    }

    /// Lays out the private content wrapper inside this region's snapped insets.
    @Override
    protected void layoutChildren() {
        if (!layoutPresent) {
            return;
        }
        double left = snappedLeftInset();
        double top = snappedTopInset();
        contentHolder.resizeRelocate(
                left,
                top,
                Math.max(0.0, getWidth() - left - snappedRightInset()),
                Math.max(0.0, getHeight() - top - snappedBottomInset())
        );
    }

    /// Replaces the holder's only child and refreshes layout metrics.
    private void updateContent(@Nullable Node content) {
        if (content == null) {
            contentHolder.getChildren().clear();
        } else {
            contentHolder.getChildren().setAll(content);
        }
        requestContainerLayout();
    }

    /// Starts or synchronously settles a transition toward one showing state.
    private void animateToShowingState(boolean shown) {
        if (shown) {
            layoutPresent = true;
            contentHolder.setManaged(true);
            contentHolder.setVisible(true);
            requestContainerLayout();
        }

        if (getScene() == null || getContent() == null) {
            snapToCurrentState();
            return;
        }

        transitioning.set(true);
        @Nullable M3MotionSpec explicitSpec = getMotionSpec();
        M3MotionSpec resolvedSpec = explicitSpec == null ? M3Animation.defaultSpatial(this) : explicitSpec;
        double targetScale = shown ? 1.0 : getHiddenScale();
        visibilityTransition.configure(
                resolvedSpec,
                shown ? 1.0 : 0.0,
                targetScale,
                targetScale,
                0.0,
                0.0
        );
        M3Animation.playFromStart(this, visibilityTransition);
    }

    /// Applies layout participation after the current visual transition reaches its target.
    private void completeVisibilityTransition() {
        transitioning.set(false);
        if (!isShowing()) {
            layoutPresent = false;
            contentHolder.setManaged(false);
            contentHolder.setVisible(false);
            requestContainerLayout();
        }
    }

    /// Requests layout from this region and its parent after participation changes.
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

    /// Converts an outer height constraint to the content-holder constraint.
    private double contentHeight(double height) {
        return height < 0.0 ? height : Math.max(0.0, height - verticalInsets());
    }

    /// Converts an outer width constraint to the content-holder constraint.
    private double contentWidth(double width) {
        return width < 0.0 ? width : Math.max(0.0, width - horizontalInsets());
    }

    /// Adds insets to a maximum size without overflowing JavaFX's unbounded-size sentinel.
    private static double addInsetsWithoutOverflow(double contentSize, double insets) {
        return contentSize >= Double.MAX_VALUE - insets ? Double.MAX_VALUE : contentSize + insets;
    }

    /// Validates a hidden-state scale.
    private static double validateHiddenScale(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException("hiddenScale must be finite and greater than zero");
        }
        return value;
    }
}
