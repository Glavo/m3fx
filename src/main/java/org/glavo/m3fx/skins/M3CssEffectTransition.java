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
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Animates CSS-resolved drop shadow effect changes for an interaction owner.
@NotNullByDefault
final class M3CssEffectTransition {
    /// Handles owner interaction state changes.
    private final ChangeListener<Boolean> interactionStateListener =
            (observable, oldValue, newValue) -> animateEffectFromCss();

    /// The animation timeline for drop shadow transitions.
    private final Timeline animation = new Timeline();

    /// The node whose pseudo-class states drive target effect resolution.
    private final Node owner;

    /// The node that receives the animated effect.
    private final Node target;

    /// Handles focus-visible pseudo-class changes produced by the shared interaction state layer.
    private final SetChangeListener<PseudoClass> pseudoClassStateListener = change -> {
        if (M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS.equals(change.getElementAdded())
                || M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS.equals(change.getElementRemoved())) {
            animateEffectFromCss();
        }
    };

    /// Observes runtime motion settings while the owner is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver;

    /// Creates an effect transition.
    M3CssEffectTransition(Node owner, Node target) {
        this.owner = owner;
        this.target = target;
        this.motionSettingsObserver = new M3MotionSettingsObserver(owner, this::refreshMotionSettings);
    }

    /// Installs interaction listeners.
    void install() {
        owner.getPseudoClassStates().addListener(pseudoClassStateListener);
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
        owner.getPseudoClassStates().removeListener(pseudoClassStateListener);
        motionSettingsObserver.dispose();
        animation.stop();
    }

    /// Returns whether the effect transition is currently running.
    boolean isRunning() {
        return animation.getStatus() == Animation.Status.RUNNING;
    }

    /// Applies changed animation settings to the current effect transition.
    private void refreshMotionSettings() {
        if (animation.getStatus() == Animation.Status.RUNNING) {
            animateEffectFromCss();
        }
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

        if (!M3Animation.areAnimationsEnabled(owner)) {
            target.setEffect(end);
            return;
        }

        DropShadow animated = start == null ? emptyShadow(end) : start;
        DropShadow targetShadow = end == null ? emptyShadow(start) : end;
        M3MotionSpec spec = M3Animation.fastEffects(owner);
        target.setEffect(animated);
        animation.getKeyFrames().setAll(
                keyFrame(Duration.ZERO, spec, animated, animated),
                keyFrame(spec.duration(), spec, animated, targetShadow)
        );
        animation.setOnFinished(event -> target.setEffect(end));
        M3Animation.playFromStart(owner, animation);
    }

    /// Creates a key frame for the supplied shadow state.
    private static KeyFrame keyFrame(Duration duration, M3MotionSpec spec, DropShadow animated, DropShadow state) {
        return new KeyFrame(
                duration,
                new KeyValue(animated.radiusProperty(), state.getRadius(), spec.interpolator()),
                new KeyValue(animated.spreadProperty(), state.getSpread(), spec.interpolator()),
                new KeyValue(animated.offsetXProperty(), state.getOffsetX(), spec.interpolator()),
                new KeyValue(animated.offsetYProperty(), state.getOffsetY(), spec.interpolator()),
                new KeyValue(animated.colorProperty(), state.getColor(), spec.interpolator())
        );
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
