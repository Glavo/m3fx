// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Animates CSS-resolved drop shadow effect changes for an interaction owner.
@NotNullByDefault
final class M3CssEffectTransition {
    /// The duration used when an effect becomes stronger.
    private static final Duration ENTER_DURATION = Duration.millis(120.0);

    /// The duration used when an effect becomes weaker or disappears.
    private static final Duration EXIT_DURATION = Duration.millis(90.0);

    /// Handles owner interaction state changes.
    private final ChangeListener<Boolean> interactionStateListener =
            (observable, oldValue, newValue) -> animateEffectFromCss();

    /// The animation timeline for drop shadow transitions.
    private final Timeline animation = new Timeline();

    /// The node whose pseudo-class states drive target effect resolution.
    private final Node owner;

    /// The node that receives the animated effect.
    private final Node target;

    /// Tracks keyboard-visible focus state for the owner.
    private final M3FocusVisibleTracker focusVisibleTracker;

    /// Creates an effect transition.
    M3CssEffectTransition(Node owner, Node target) {
        this.owner = owner;
        this.target = target;
        this.focusVisibleTracker = new M3FocusVisibleTracker(owner, this::animateEffectFromCss);
    }

    /// Installs interaction listeners.
    void install() {
        focusVisibleTracker.install();
        owner.hoverProperty().addListener(interactionStateListener);
        owner.focusedProperty().addListener(interactionStateListener);
        owner.pressedProperty().addListener(interactionStateListener);
        owner.disabledProperty().addListener(interactionStateListener);
    }

    /// Uninstalls interaction listeners and stops active animation.
    void uninstall() {
        owner.hoverProperty().removeListener(interactionStateListener);
        owner.focusedProperty().removeListener(interactionStateListener);
        owner.pressedProperty().removeListener(interactionStateListener);
        owner.disabledProperty().removeListener(interactionStateListener);
        focusVisibleTracker.uninstall();
        animation.stop();
    }

    /// Returns whether the effect transition is currently running.
    boolean isRunning() {
        return animation.getStatus() == Animation.Status.RUNNING;
    }

    /// Animates from the current target effect to the CSS-resolved target effect.
    void animateEffectFromCss() {
        DropShadow start = copyDropShadow(target.getEffect());
        animation.stop();
        owner.applyCss();
        if (owner != target) {
            target.applyCss();
        }
        DropShadow end = owner.isDisabled() ? null : copyDropShadow(target.getEffect());

        if (sameDropShadow(start, end)) {
            target.setEffect(end);
            return;
        }

        DropShadow animated = start == null ? emptyShadow(end) : start;
        DropShadow targetShadow = end == null ? emptyShadow(start) : end;
        target.setEffect(animated);
        animation.getKeyFrames().setAll(
                keyFrame(Duration.ZERO, animated, animated),
                keyFrame(transitionDuration(start, end), animated, targetShadow)
        );
        animation.setOnFinished(event -> target.setEffect(end));
        animation.playFromStart();
    }

    /// Creates a key frame for the supplied shadow state.
    private static KeyFrame keyFrame(Duration duration, DropShadow animated, DropShadow state) {
        return new KeyFrame(
                duration,
                new KeyValue(animated.radiusProperty(), state.getRadius(), Interpolator.EASE_BOTH),
                new KeyValue(animated.spreadProperty(), state.getSpread(), Interpolator.EASE_BOTH),
                new KeyValue(animated.offsetXProperty(), state.getOffsetX(), Interpolator.EASE_BOTH),
                new KeyValue(animated.offsetYProperty(), state.getOffsetY(), Interpolator.EASE_BOTH),
                new KeyValue(animated.colorProperty(), state.getColor(), Interpolator.EASE_BOTH)
        );
    }

    /// Returns the transition duration for a shadow change.
    private static Duration transitionDuration(@Nullable DropShadow start, @Nullable DropShadow end) {
        double startDepth = shadowDepth(start);
        double endDepth = shadowDepth(end);
        return endDepth > startDepth ? ENTER_DURATION : EXIT_DURATION;
    }

    /// Returns a simple depth metric for comparing shadow strength.
    private static double shadowDepth(@Nullable DropShadow shadow) {
        if (shadow == null) {
            return 0.0;
        }
        return shadow.getRadius() + Math.abs(shadow.getOffsetY()) + Math.abs(shadow.getOffsetX());
    }

    /// Creates a zero-strength shadow that can animate to or from another shadow.
    private static DropShadow emptyShadow(@Nullable DropShadow reference) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(0.0);
        shadow.setSpread(reference == null ? 0.0 : reference.getSpread());
        shadow.setOffsetX(reference == null ? 0.0 : reference.getOffsetX());
        shadow.setOffsetY(0.0);
        shadow.setColor(reference == null ? Color.TRANSPARENT : transparent(reference.getColor()));
        return shadow;
    }

    /// Copies a drop shadow effect, or returns null for unsupported effects.
    private static @Nullable DropShadow copyDropShadow(@Nullable Effect effect) {
        if (!(effect instanceof DropShadow shadow)) {
            return null;
        }

        DropShadow copy = new DropShadow();
        copy.setBlurType(shadow.getBlurType());
        copy.setColor(shadow.getColor());
        copy.setRadius(shadow.getRadius());
        copy.setSpread(shadow.getSpread());
        copy.setOffsetX(shadow.getOffsetX());
        copy.setOffsetY(shadow.getOffsetY());
        copy.setInput(shadow.getInput());
        return copy;
    }

    /// Returns whether two shadows have the same rendered parameters.
    private static boolean sameDropShadow(@Nullable DropShadow first, @Nullable DropShadow second) {
        if (first == null || second == null) {
            return first == second;
        }

        return Double.compare(first.getRadius(), second.getRadius()) == 0
                && Double.compare(first.getSpread(), second.getSpread()) == 0
                && Double.compare(first.getOffsetX(), second.getOffsetX()) == 0
                && Double.compare(first.getOffsetY(), second.getOffsetY()) == 0
                && first.getColor().equals(second.getColor());
    }

    /// Returns a fully transparent color with the same hue as the reference color.
    private static Color transparent(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.0);
    }
}
