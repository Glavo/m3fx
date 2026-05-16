// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;

/// A bounded Material Design 3 state layer with ripple animation support.
@NotNullByDefault
final class M3StateLayer extends Pane {
    /// The class applied to state layer containers.
    static final String STYLE_CLASS = "m3-state-layer-container";

    /// The class applied to persistent state layer overlays.
    static final String OVERLAY_STYLE_CLASS = "m3-state-layer";

    /// The class applied to animated ripple nodes.
    static final String RIPPLE_STYLE_CLASS = "m3-ripple";

    /// The opacity used by the animated ripple at the start of a press.
    private static final double RIPPLE_START_OPACITY = 0.18;

    /// The duration used by the ripple expansion.
    private static final Duration RIPPLE_DURATION = Duration.millis(360.0);

    /// The persistent overlay node controlled by CSS pseudo-class rules.
    private final Region overlay = new Region();

    /// The animated bounded ripple node.
    private final Region ripple = new Region();

    /// The clip that bounds overlay and ripple visuals to the component shape.
    private final Rectangle clip = new Rectangle();

    /// The ripple animation timeline.
    private final Timeline rippleAnimation = new Timeline();

    /// Creates a state layer.
    M3StateLayer() {
        getStyleClass().add(STYLE_CLASS);
        overlay.getStyleClass().add(OVERLAY_STYLE_CLASS);
        ripple.getStyleClass().add(RIPPLE_STYLE_CLASS);
        setMouseTransparent(true);
        setManaged(false);
        overlay.setMouseTransparent(true);
        ripple.setMouseTransparent(true);
        ripple.setOpacity(0.0);
        getChildren().addAll(overlay, ripple);
        setClip(clip);
    }

    /// Lays out the state layer within the skinnable component.
    void layoutLayer(double x, double y, double width, double height, double shapeRadius) {
        resizeRelocate(x, y, width, height);
        overlay.resizeRelocate(0.0, 0.0, width, height);
        clip.setWidth(width);
        clip.setHeight(height);
        clip.setArcWidth(shapeRadius * 2.0);
        clip.setArcHeight(shapeRadius * 2.0);
    }

    /// Plays a bounded ripple from a point in this state layer's coordinate space.
    void playRipple(double x, double y) {
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
        rippleAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(ripple.scaleXProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(ripple.scaleYProperty(), 0.0, Interpolator.EASE_OUT),
                        new KeyValue(ripple.opacityProperty(), RIPPLE_START_OPACITY, Interpolator.EASE_OUT)
                ),
                new KeyFrame(
                        RIPPLE_DURATION,
                        new KeyValue(ripple.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(ripple.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                        new KeyValue(ripple.opacityProperty(), 0.0, Interpolator.EASE_OUT)
                )
        );
        rippleAnimation.playFromStart();
    }

    /// Plays a bounded ripple from the layer center.
    void playCenteredRipple() {
        playRipple(getWidth() / 2.0, getHeight() / 2.0);
    }

    /// Stops ripple animation and clears transient ripple state.
    void reset() {
        rippleAnimation.stop();
        ripple.setOpacity(0.0);
        ripple.setScaleX(0.0);
        ripple.setScaleY(0.0);
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
}
