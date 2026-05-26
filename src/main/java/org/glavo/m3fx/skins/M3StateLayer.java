// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A bounded Material Design 3 state layer with ripple animation support.
@NotNullByDefault
final class M3StateLayer extends Pane {
    /// The pseudo-class used by button-like controls while their armed state is active.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The pseudo-class used by JavaFX while a node is hovered.
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("hover");

    /// The pseudo-class used by JavaFX while a node has keyboard-visible focus.
    private static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// The pseudo-class used by JavaFX while a node is pressed.
    private static final PseudoClass PRESSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pressed");

    /// The state layer opacity used while a node is hovered.
    private static final double HOVER_OVERLAY_OPACITY = 0.08;

    /// The state layer opacity used while a node has focus-visible feedback.
    private static final double FOCUS_OVERLAY_OPACITY = 0.10;

    /// The class applied to state layer containers.
    static final String STYLE_CLASS = "m3-state-layer-container";

    /// The class applied to persistent state layer overlays.
    static final String OVERLAY_STYLE_CLASS = "m3-state-layer";

    /// The class applied to animated ripple nodes.
    static final String RIPPLE_STYLE_CLASS = "m3-ripple";

    /// The opacity used by the animated ripple at the start of a press.
    private static final double RIPPLE_START_OPACITY = 0.18;

    /// The state layer opacity used while a button-like control is armed.
    private static final double ARMED_OVERLAY_OPACITY = 0.10;

    /// The persistent overlay node controlled by CSS pseudo-class rules.
    private final Region overlay = new Region();

    /// The animated bounded ripple node.
    private final Region ripple = new Region();

    /// The clip that bounds overlay and ripple visuals to the component shape.
    private final Path clip = new Path();

    /// The ripple expansion and release animation timeline.
    private final Timeline rippleAnimation = new Timeline();

    /// The overlay opacity animation timeline.
    private final Timeline overlayOpacityAnimation = new Timeline();

    /// The control whose interaction states drive this layer.
    private @Nullable Node stateOwner;

    /// Handles interaction state changes that should animate owner-state opacity.
    private final ChangeListener<Boolean> interactionStateListener =
            (observable, oldValue, newValue) -> animateOverlayOpacityFromOwnerState();

    /// Handles owner pseudo-class changes that should animate owner-state opacity.
    private final SetChangeListener<PseudoClass> pseudoClassStateListener =
            change -> animateOverlayOpacityFromOwnerState();

    /// Handles button armed changes that should expose the CSS armed pseudo-class.
    private final ChangeListener<Boolean> buttonArmedStateListener = (observable, oldValue, newValue) -> {
        Node owner = stateOwner;
        if (owner != null) {
            owner.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, newValue);
        }
        animateOverlayOpacityFromOwnerState();
    };

    /// Tracks keyboard-visible focus state for the owner.
    private @Nullable M3FocusVisibleTracker focusVisibleTracker;

    /// The radius currently applied to the overlay background.
    private double overlayTopLeftRadius = Double.NaN;

    /// The top-right radius currently applied to the overlay background.
    private double overlayTopRightRadius = Double.NaN;

    /// The bottom-right radius currently applied to the overlay background.
    private double overlayBottomRightRadius = Double.NaN;

    /// The bottom-left radius currently applied to the overlay background.
    private double overlayBottomLeftRadius = Double.NaN;

    /// Creates a state layer.
    M3StateLayer() {
        getStyleClass().add(STYLE_CLASS);
        overlay.getStyleClass().add(OVERLAY_STYLE_CLASS);
        ripple.getStyleClass().add(RIPPLE_STYLE_CLASS);
        setMouseTransparent(true);
        setManaged(false);
        overlay.setManaged(false);
        ripple.setManaged(false);
        overlay.setMouseTransparent(true);
        ripple.setMouseTransparent(true);
        overlay.setOpacity(0.0);
        ripple.setOpacity(0.0);
        clip.setFill(Color.BLACK);
        getChildren().addAll(overlay, ripple);
        setClip(clip);
    }

    /// Installs opacity transitions driven by the owner node's interaction states.
    void installStateTransitions(Node owner) {
        if (stateOwner == owner) {
            return;
        }
        uninstallStateTransitions();
        stateOwner = owner;
        focusVisibleTracker = new M3FocusVisibleTracker(owner, this::animateOverlayOpacityFromOwnerState);
        focusVisibleTracker.install();
        owner.hoverProperty().addListener(interactionStateListener);
        owner.focusedProperty().addListener(interactionStateListener);
        owner.pressedProperty().addListener(interactionStateListener);
        owner.disabledProperty().addListener(interactionStateListener);
        owner.getPseudoClassStates().addListener(pseudoClassStateListener);
        if (owner instanceof ButtonBase button) {
            owner.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, button.isArmed());
            button.armedProperty().addListener(buttonArmedStateListener);
        }
    }

    /// Removes opacity transition listeners from the current owner.
    void uninstallStateTransitions() {
        Node owner = stateOwner;
        if (owner == null) {
            return;
        }

        owner.hoverProperty().removeListener(interactionStateListener);
        owner.focusedProperty().removeListener(interactionStateListener);
        owner.pressedProperty().removeListener(interactionStateListener);
        owner.disabledProperty().removeListener(interactionStateListener);
        owner.getPseudoClassStates().removeListener(pseudoClassStateListener);
        if (owner instanceof ButtonBase button) {
            button.armedProperty().removeListener(buttonArmedStateListener);
            owner.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, false);
        }
        M3FocusVisibleTracker tracker = focusVisibleTracker;
        if (tracker != null) {
            tracker.uninstall();
            focusVisibleTracker = null;
        }
        stateOwner = null;
        overlayOpacityAnimation.stop();
    }

    /// Lays out the state layer within the skinnable component.
    void layoutLayer(double x, double y, double width, double height, double shapeRadius) {
        layoutLayer(x, y, width, height, shapeRadius, shapeRadius, shapeRadius, shapeRadius);
    }

    /// Lays out the state layer with independent corner radii.
    void layoutLayer(
            double x,
            double y,
            double width,
            double height,
            double topLeftRadius,
            double topRightRadius,
            double bottomRightRadius,
            double bottomLeftRadius
    ) {
        double topLeft = resolvedShapeRadius(width, height, topLeftRadius);
        double topRight = resolvedShapeRadius(width, height, topRightRadius);
        double bottomRight = resolvedShapeRadius(width, height, bottomRightRadius);
        double bottomLeft = resolvedShapeRadius(width, height, bottomLeftRadius);
        resizeRelocate(x, y, width, height);
        overlay.resizeRelocate(0.0, 0.0, width, height);
        updateOverlayShape(topLeft, topRight, bottomRight, bottomLeft);
        updateClip(width, height, topLeft, topRight, bottomRight, bottomLeft);
    }

    /// Plays a bounded ripple from a point in this state layer's coordinate space.
    void playRipple(double x, double y) {
        Node owner = animationOwner();
        if (!M3Animation.areAnimationsEnabled(owner)) {
            rippleAnimation.stop();
            ripple.setOpacity(0.0);
            ripple.setScaleX(0.0);
            ripple.setScaleY(0.0);
            return;
        }

        double width = getWidth();
        double height = getHeight();
        if (width <= 0.0 || height <= 0.0) {
            return;
        }

        double diameter = rippleDiameter(x, y, width, height);
        rippleAnimation.stop();
        ripple.resizeRelocate(x - diameter / 2.0, y - diameter / 2.0, diameter, diameter);
        ripple.setScaleX(0.0);
        ripple.setScaleY(0.0);
        ripple.setOpacity(RIPPLE_START_OPACITY);
        M3MotionSpec rippleSpec = M3Animation.defaultSpatial(owner);
        rippleAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(ripple.scaleXProperty(), 0.0, rippleSpec.interpolator()),
                        new KeyValue(ripple.scaleYProperty(), 0.0, rippleSpec.interpolator()),
                        new KeyValue(ripple.opacityProperty(), RIPPLE_START_OPACITY, rippleSpec.interpolator())
                ),
                new KeyFrame(
                        rippleSpec.duration(),
                        new KeyValue(ripple.scaleXProperty(), 1.0, rippleSpec.interpolator()),
                        new KeyValue(ripple.scaleYProperty(), 1.0, rippleSpec.interpolator()),
                        new KeyValue(ripple.opacityProperty(), RIPPLE_START_OPACITY, rippleSpec.interpolator())
                )
        );
        M3Animation.playFromStart(owner, rippleAnimation);
    }

    /// Plays a bounded ripple from the layer center.
    void playCenteredRipple() {
        playRipple(getWidth() / 2.0, getHeight() / 2.0);
    }

    /// Releases the active ripple and fades it out.
    void releaseRipple() {
        Node owner = animationOwner();
        if (!M3Animation.areAnimationsEnabled(owner)) {
            rippleAnimation.stop();
            ripple.setOpacity(0.0);
            ripple.setScaleX(1.0);
            ripple.setScaleY(1.0);
            return;
        }

        double startOpacity = ripple.getOpacity();
        if (startOpacity <= 0.0) {
            return;
        }

        double startScaleX = ripple.getScaleX();
        double startScaleY = ripple.getScaleY();
        rippleAnimation.stop();
        ripple.setOpacity(startOpacity);
        ripple.setScaleX(startScaleX);
        ripple.setScaleY(startScaleY);

        M3MotionSpec expansionSpec = M3Animation.defaultSpatial(owner);
        M3MotionSpec fadeSpec = M3Animation.fastEffects(owner);
        Duration remainingExpansion = remainingRippleExpansionDuration(
                expansionSpec.duration(),
                Math.max(startScaleX, startScaleY)
        );
        Duration fadeEnd = Duration.millis(remainingExpansion.toMillis() + fadeSpec.duration().toMillis());
        if (remainingExpansion.greaterThan(Duration.ZERO)) {
            rippleAnimation.getKeyFrames().setAll(
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(ripple.scaleXProperty(), startScaleX, expansionSpec.interpolator()),
                            new KeyValue(ripple.scaleYProperty(), startScaleY, expansionSpec.interpolator()),
                            new KeyValue(ripple.opacityProperty(), startOpacity, fadeSpec.interpolator())
                    ),
                    new KeyFrame(
                            remainingExpansion,
                            new KeyValue(ripple.scaleXProperty(), 1.0, expansionSpec.interpolator()),
                            new KeyValue(ripple.scaleYProperty(), 1.0, expansionSpec.interpolator()),
                            new KeyValue(ripple.opacityProperty(), startOpacity, fadeSpec.interpolator())
                    ),
                    new KeyFrame(
                            fadeEnd,
                            new KeyValue(ripple.scaleXProperty(), 1.0, expansionSpec.interpolator()),
                            new KeyValue(ripple.scaleYProperty(), 1.0, expansionSpec.interpolator()),
                            new KeyValue(ripple.opacityProperty(), 0.0, fadeSpec.interpolator())
                    )
            );
        } else {
            rippleAnimation.getKeyFrames().setAll(
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(ripple.scaleXProperty(), startScaleX, fadeSpec.interpolator()),
                            new KeyValue(ripple.scaleYProperty(), startScaleY, fadeSpec.interpolator()),
                            new KeyValue(ripple.opacityProperty(), startOpacity, fadeSpec.interpolator())
                    ),
                    new KeyFrame(
                            fadeSpec.duration(),
                            new KeyValue(ripple.scaleXProperty(), 1.0, fadeSpec.interpolator()),
                            new KeyValue(ripple.scaleYProperty(), 1.0, fadeSpec.interpolator()),
                            new KeyValue(ripple.opacityProperty(), 0.0, fadeSpec.interpolator())
                    )
            );
        }
        M3Animation.playFromStart(owner, rippleAnimation);
    }

    /// Stops ripple animation and clears transient ripple state.
    void reset() {
        overlayOpacityAnimation.stop();
        overlay.setOpacity(0.0);
        rippleAnimation.stop();
        ripple.setOpacity(0.0);
        ripple.setScaleX(0.0);
        ripple.setScaleY(0.0);
    }

    /// Returns whether the overlay opacity is currently animating.
    boolean isOverlayOpacityAnimationRunning() {
        return overlayOpacityAnimation.getStatus() == Animation.Status.RUNNING;
    }

    /// Returns whether the ripple is currently animating.
    boolean isRippleAnimationRunning() {
        return rippleAnimation.getStatus() == Animation.Status.RUNNING;
    }

    /// Animates from the current overlay opacity to the opacity resolved from the owner state.
    void animateOverlayOpacityFromOwnerState() {
        Node owner = stateOwner;
        if (owner == null) {
            return;
        }

        double startOpacity = overlay.getOpacity();
        overlayOpacityAnimation.stop();
        double targetOpacity = resolvedOverlayOpacity(owner);
        overlay.setOpacity(startOpacity);

        if (Double.compare(startOpacity, targetOpacity) == 0) {
            overlay.setOpacity(targetOpacity);
            return;
        }

        if (!M3Animation.areAnimationsEnabled(owner)) {
            overlay.setOpacity(targetOpacity);
            return;
        }

        M3MotionSpec opacitySpec = M3Animation.fastEffects(owner);
        overlayOpacityAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(overlay.opacityProperty(), startOpacity, opacitySpec.interpolator())
                ),
                new KeyFrame(
                        opacitySpec.duration(),
                        new KeyValue(overlay.opacityProperty(), targetOpacity, opacitySpec.interpolator())
                )
        );
        M3Animation.playFromStart(owner, overlayOpacityAnimation);
    }

    /// Returns the target overlay opacity for the owner interaction state.
    private double resolvedOverlayOpacity(Node owner) {
        if (owner.isDisabled()) {
            return 0.0;
        }
        if (isPressedLike(owner)) {
            return ARMED_OVERLAY_OPACITY;
        }
        if (owner.getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS)) {
            return FOCUS_OVERLAY_OPACITY;
        }
        if (owner.isHover() || owner.getPseudoClassStates().contains(HOVER_PSEUDO_CLASS)) {
            return HOVER_OVERLAY_OPACITY;
        }
        return 0.0;
    }

    /// Returns whether the owner should show pressed-state feedback.
    private static boolean isPressedLike(Node owner) {
        if (owner.isPressed() || owner.getPseudoClassStates().contains(PRESSED_PSEUDO_CLASS)) {
            return true;
        }
        return owner instanceof ButtonBase button
                && (button.isArmed() || owner.getPseudoClassStates().contains(ARMED_PSEUDO_CLASS));
    }

    /// Returns the node whose motion setting controls this state layer.
    private Node animationOwner() {
        @Nullable Node owner = stateOwner;
        return owner == null ? this : owner;
    }

    /// Computes the ripple diameter needed to cover this layer from an origin point.
    private static double rippleDiameter(double x, double y, double width, double height) {
        double left = x;
        double right = width - x;
        double top = y;
        double bottom = height - y;
        double radius = Math.hypot(Math.max(left, right), Math.max(top, bottom));
        return radius * 2.0;
    }

    /// Returns the remaining expansion duration for a ripple released before it reaches full size.
    private static Duration remainingRippleExpansionDuration(Duration fullDuration, double currentScale) {
        double clampedScale = Math.max(0.0, Math.min(1.0, currentScale));
        return Duration.millis(fullDuration.toMillis() * (1.0 - clampedScale));
    }

    /// Resolves a token radius to a radius that can be represented within the current bounds.
    private static double resolvedShapeRadius(double width, double height, double shapeRadius) {
        double maximumRadius = Math.max(0.0, Math.min(width, height) / 2.0);
        return Math.min(Math.max(0.0, shapeRadius), maximumRadius);
    }

    /// Updates the overlay background radius when the resolved shape changes.
    private void updateOverlayShape(double topLeft, double topRight, double bottomRight, double bottomLeft) {
        if (Double.compare(overlayTopLeftRadius, topLeft) == 0
                && Double.compare(overlayTopRightRadius, topRight) == 0
                && Double.compare(overlayBottomRightRadius, bottomRight) == 0
                && Double.compare(overlayBottomLeftRadius, bottomLeft) == 0) {
            return;
        }

        overlayTopLeftRadius = topLeft;
        overlayTopRightRadius = topRight;
        overlayBottomRightRadius = bottomRight;
        overlayBottomLeftRadius = bottomLeft;
        overlay.setStyle("-fx-background-radius: "
                + formatPixels(topLeft) + " "
                + formatPixels(topRight) + " "
                + formatPixels(bottomRight) + " "
                + formatPixels(bottomLeft) + ";");
    }

    /// Updates the clip path to match the resolved rounded rectangle shape.
    private void updateClip(double width, double height, double topLeft, double topRight, double bottomRight, double bottomLeft) {
        clip.getElements().setAll(
                new MoveTo(topLeft, 0.0),
                new LineTo(width - topRight, 0.0),
                arcTo(topRight, width, topRight),
                new LineTo(width, height - bottomRight),
                arcTo(bottomRight, width - bottomRight, height),
                new LineTo(bottomLeft, height),
                arcTo(bottomLeft, 0.0, height - bottomLeft),
                new LineTo(0.0, topLeft),
                arcTo(topLeft, topLeft, 0.0),
                new ClosePath()
        );
    }

    /// Creates a corner arc or a zero-length line for square corners.
    private static PathElement arcTo(double radius, double x, double y) {
        if (radius <= 0.0) {
            return new LineTo(x, y);
        }

        ArcTo arc = new ArcTo();
        arc.setRadiusX(radius);
        arc.setRadiusY(radius);
        arc.setX(x);
        arc.setY(y);
        arc.setSweepFlag(true);
        return arc;
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        return Double.toString(value) + "px";
    }
}
