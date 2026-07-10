// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Animates CSS-resolved drop shadow effect changes for an interaction owner.
@NotNullByDefault
final class M3CssEffectTransition {
    /// Handles owner interaction state changes.
    private final ChangeListener<Boolean> interactionStateListener =
            (observable, oldValue, newValue) -> animateEffectFromCss();

    /// The reusable animation for drop shadow transitions.
    private final ShadowTransition animation = new ShadowTransition();

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
        animation.configure(spec, animated, targetShadow);

        animation.setOnFinished(event -> target.setEffect(end));
        M3Animation.playFromStart(owner, animation);
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

    /// Reusable finite transition for the rendered parameters of one drop shadow.
    private static final class ShadowTransition extends M3FiniteTransition {
        /// The drop shadow receiving interpolated values during the current transition.
        private @Nullable DropShadow shadow;

        /// The starting radius.
        private double startRadius;

        /// The target radius.
        private double targetRadius;

        /// The starting spread.
        private double startSpread;

        /// The target spread.
        private double targetSpread;

        /// The starting horizontal offset.
        private double startOffsetX;

        /// The target horizontal offset.
        private double targetOffsetX;

        /// The starting vertical offset.
        private double startOffsetY;

        /// The target vertical offset.
        private double targetOffsetY;

        /// The starting color.
        private Color startColor = Color.TRANSPARENT;

        /// The target color.
        private Color targetColor = Color.TRANSPARENT;

        /// Creates an unconfigured shadow transition.
        private ShadowTransition() {
        }

        /// Reconfigures the transition from the currently rendered shadow values.
        private void configure(M3MotionSpec spec, DropShadow shadow, DropShadow target) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            this.shadow = shadow;
            startRadius = shadow.getRadius();
            targetRadius = target.getRadius();
            startSpread = shadow.getSpread();
            targetSpread = target.getSpread();
            startOffsetX = shadow.getOffsetX();
            targetOffsetX = target.getOffsetX();
            startOffsetY = shadow.getOffsetY();
            targetOffsetY = target.getOffsetY();
            startColor = shadow.getColor();
            targetColor = target.getColor();
        }

        /// Applies the eased shadow parameters for the current pulse.
        @Override
        protected void interpolate(double fraction) {
            DropShadow current = shadow;
            if (current == null) {
                return;
            }

            current.setRadius(interpolate(startRadius, targetRadius, fraction));
            current.setSpread(interpolate(startSpread, targetSpread, fraction));
            current.setOffsetX(interpolate(startOffsetX, targetOffsetX, fraction));
            current.setOffsetY(interpolate(startOffsetY, targetOffsetY, fraction));
            current.setColor(startColor.interpolate(targetColor, fraction));
        }

        /// Interpolates linearly between two scalar values.
        private static double interpolate(double start, double end, double fraction) {
            return start + (end - start) * fraction;
        }
    }
}
