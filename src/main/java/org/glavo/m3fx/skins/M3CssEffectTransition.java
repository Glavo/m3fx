// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3FocusVisibleTracker;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Animates CSS-resolved drop-shadow changes for an interaction owner.
///
/// The transition observes interaction state on the owner and applies the interpolated effect to the target. Only
/// user-agent and author styles that resolve to a [DropShadow], or to no effect, are interpolated; an application
/// effect set at user origin remains under application control.
@NotNullByDefault
final class M3CssEffectTransition {
    /// The pseudo-class used by controls that expose an explicit armed state.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The pseudo-class used while a draggable component is represented as dragged.
    private static final PseudoClass DRAGGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("dragged");

    /// Handles owner interaction state changes.
    private final ChangeListener<Boolean> interactionStateListener =
            (observable, oldValue, newValue) -> animateEffectFromCss();

    /// The reusable animation for drop shadow transitions.
    private final ShadowTransition animation = new ShadowTransition();

    /// The lazily created shadow mutated by every animated elevation transition.
    private @Nullable DropShadow animatedShadow;

    /// The CSS origin retained while the reusable shadow temporarily represents the resolved effect.
    private StyleOrigin animationStyleOrigin = StyleOrigin.USER_AGENT;

    /// The node whose pseudo-class states drive target effect resolution.
    private final Node owner;

    /// The node that receives the animated effect.
    private final Node target;

    /// Handles interaction pseudo-classes that do not have a dedicated JavaFX observable property.
    private final SetChangeListener<PseudoClass> pseudoClassStateListener = change -> {
        @Nullable PseudoClass added = change.getElementAdded();
        @Nullable PseudoClass removed = change.getElementRemoved();
        if (M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS.equals(added)
                || M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS.equals(removed)
                || ARMED_PSEUDO_CLASS.equals(added)
                || ARMED_PSEUDO_CLASS.equals(removed)
                || DRAGGED_PSEUDO_CLASS.equals(added)
                || DRAGGED_PSEUDO_CLASS.equals(removed)) {
            animateEffectFromCss();
        }
    };

    /// Creates an effect transition without installing listeners.
    ///
    /// @param owner  the node whose interaction state determines the CSS target
    /// @param target the node whose effect is rendered
    M3CssEffectTransition(Node owner, Node target) {
        this.owner = owner;
        this.target = target;
    }

    /// Installs interaction listeners.
    ///
    /// This method is intended to be paired with one later call to [#uninstall()].
    void install() {
        owner.getPseudoClassStates().addListener(pseudoClassStateListener);
        owner.hoverProperty().addListener(interactionStateListener);
        owner.focusedProperty().addListener(interactionStateListener);
        owner.pressedProperty().addListener(interactionStateListener);
        owner.disabledProperty().addListener(interactionStateListener);
    }

    /// Uninstalls interaction listeners, stops active animation, and applies the exact target effect.
    void uninstall() {
        owner.hoverProperty().removeListener(interactionStateListener);
        owner.focusedProperty().removeListener(interactionStateListener);
        owner.pressedProperty().removeListener(interactionStateListener);
        owner.disabledProperty().removeListener(interactionStateListener);
        owner.getPseudoClassStates().removeListener(pseudoClassStateListener);
        animation.stop();
        settleAnimation();
        animation.setOnFinished(null);
    }

    /// Returns whether the effect transition is currently running.
    boolean isRunning() {
        return animation.getStatus() == Animation.Status.RUNNING;
    }

    /// Animates from the current target effect to the CSS-resolved target effect.
    void animateEffectFromCss() {
        boolean supportedStart = animation.captureStart(target.getEffect());
        animation.stop();
        owner.applyCss();
        if (owner != target) {
            target.applyCss();
        }
        Effect resolvedEffect = target.getEffect();
        StyleableProperty<@Nullable Effect> effectProperty = styleableEffectProperty();
        @Nullable StyleOrigin resolvedOrigin = effectProperty.getStyleOrigin();
        StyleOrigin targetOrigin = resolvedOrigin == null ? StyleOrigin.USER_AGENT : resolvedOrigin;

        if (targetOrigin == StyleOrigin.USER) {
            return;
        }

        if (owner.isDisabled()) {
            return;
        }

        @Nullable DropShadow end = resolvedEffect instanceof DropShadow shadow ? shadow : null;
        boolean supportedEnd = resolvedEffect == null || end != null;

        if (!supportedStart
                || !supportedEnd
                || animation.matchesTarget(end)
                || !M3Animation.areAnimationsEnabled(owner)) {
            return;
        }

        DropShadow animated = reusableAnimatedShadow();
        animationStyleOrigin = targetOrigin;
        animation.configure(M3Animation.fastEffects(owner), animated, end);
        effectProperty.applyStyle(targetOrigin, animated);
        M3Animation.playFromStart(owner, animation);
    }

    /// Returns the single mutable shadow used after this transition first needs animation.
    private DropShadow reusableAnimatedShadow() {
        DropShadow current = animatedShadow;
        if (current == null) {
            current = new DropShadow();
            animatedShadow = current;
            animation.setOnFinished(event -> settleAnimation());
        }
        return current;
    }

    /// Applies the exact target state when an animation completes or its owning skin is disposed.
    private void settleAnimation() {
        DropShadow current = animatedShadow;
        if (current == null || target.getEffect() != current) {
            return;
        }
        animation.applyTarget();
        if (!animation.hasTargetShadow()) {
            StyleableProperty<@Nullable Effect> effectProperty = styleableEffectProperty();
            effectProperty.applyStyle(animationStyleOrigin, null);
        }
    }

    /// Returns JavaFX's styleable effect property with its actual nullable value contract.
    @SuppressWarnings("unchecked")
    private StyleableProperty<@Nullable Effect> styleableEffectProperty() {
        return (StyleableProperty<@Nullable Effect>) target.effectProperty();
    }

    /// Returns a fully transparent color with the same hue as the reference color.
    private static Color transparent(Color color) {
        if (color.getRed() == 0.0 && color.getGreen() == 0.0 && color.getBlue() == 0.0) {
            return Color.TRANSPARENT;
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.0);
    }

    /// Reusable finite transition for the rendered parameters of one drop shadow.
    private static final class ShadowTransition extends M3FiniteTransition {
        /// The drop shadow receiving interpolated values during the current transition.
        private @Nullable DropShadow shadow;

        /// Whether the captured starting effect is a drop shadow rather than no effect.
        private boolean startPresent;

        /// Whether the configured target effect is a drop shadow rather than no effect.
        private boolean targetPresent;

        /// The starting blur algorithm.
        private BlurType startBlurType = BlurType.THREE_PASS_BOX;

        /// The target blur algorithm.
        private BlurType targetBlurType = BlurType.THREE_PASS_BOX;

        /// The starting input effect.
        private @Nullable Effect startInput;

        /// The target input effect.
        private @Nullable Effect targetInput;

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

        /// Whether color interpolation is needed during each pulse.
        private boolean animateColor;

        /// Creates an unconfigured shadow transition.
        private ShadowTransition() {
        }

        /// Captures the currently rendered effect before CSS resolves the next interaction state.
        private boolean captureStart(@Nullable Effect effect) {
            if (effect == null) {
                startPresent = false;
                return true;
            }
            if (!(effect instanceof DropShadow start)) {
                return false;
            }

            startPresent = true;
            startBlurType = start.getBlurType();
            startInput = start.getInput();
            startRadius = start.getRadius();
            startSpread = start.getSpread();
            startOffsetX = start.getOffsetX();
            startOffsetY = start.getOffsetY();
            startColor = start.getColor();
            return true;
        }

        /// Returns whether the captured start already equals the supplied target shadow.
        private boolean matchesTarget(@Nullable DropShadow target) {
            if (target == null) {
                return !startPresent;
            }
            return startPresent
                    && startBlurType == target.getBlurType()
                    && startInput == target.getInput()
                    && Double.compare(startRadius, target.getRadius()) == 0
                    && Double.compare(startSpread, target.getSpread()) == 0
                    && Double.compare(startOffsetX, target.getOffsetX()) == 0
                    && Double.compare(startOffsetY, target.getOffsetY()) == 0
                    && startColor.equals(target.getColor());
        }

        /// Reconfigures the transition from the captured start to the resolved target shadow.
        private void configure(M3MotionSpec spec, DropShadow shadow, @Nullable DropShadow target) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            this.shadow = shadow;
            targetPresent = target != null;
            if (target != null) {
                targetBlurType = target.getBlurType();
                targetInput = target.getInput();
                targetRadius = target.getRadius();
                targetSpread = target.getSpread();
                targetOffsetX = target.getOffsetX();
                targetOffsetY = target.getOffsetY();
                targetColor = target.getColor();
                if (!startPresent) {
                    startBlurType = targetBlurType;
                    startInput = targetInput;
                    startRadius = 0.0;
                    startSpread = targetSpread;
                    startOffsetX = targetOffsetX;
                    startOffsetY = 0.0;
                    startColor = transparent(targetColor);
                }
            } else {
                targetBlurType = startBlurType;
                targetInput = startInput;
                targetRadius = 0.0;
                targetSpread = startSpread;
                targetOffsetX = startOffsetX;
                targetOffsetY = 0.0;
                targetColor = transparent(startColor);
            }

            shadow.setBlurType(targetPresent ? targetBlurType : startBlurType);
            shadow.setInput(targetPresent ? targetInput : startInput);
            shadow.setRadius(startRadius);
            shadow.setSpread(startSpread);
            shadow.setOffsetX(startOffsetX);
            shadow.setOffsetY(startOffsetY);
            shadow.setColor(startColor);
            animateColor = !startColor.equals(targetColor);
        }

        /// Returns whether the configured target retains a visible shadow effect.
        private boolean hasTargetShadow() {
            return targetPresent;
        }

        /// Applies the exact configured target values to the reusable rendered shadow.
        private void applyTarget() {
            DropShadow current = shadow;
            if (current == null) {
                return;
            }
            current.setBlurType(targetBlurType);
            current.setInput(targetInput);
            current.setRadius(targetRadius);
            current.setSpread(targetSpread);
            current.setOffsetX(targetOffsetX);
            current.setOffsetY(targetOffsetY);
            current.setColor(targetColor);
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
            if (animateColor) {
                current.setColor(startColor.interpolate(targetColor, fraction));
            }
        }

        /// Interpolates linearly between two scalar values.
        private static double interpolate(double start, double end, double fraction) {
            return start + (end - start) * fraction;
        }
    }
}
