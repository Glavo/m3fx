// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal helpers for honoring the M3FX motion settings around JavaFX animations.
@NotNullByDefault
public final class M3Animation {
    /// The fallback motion scheme used when no M3FX theme is installed.
    private static final M3MotionScheme FALLBACK_MOTION_SCHEME = M3MotionScheme.standard();

    /// The fallback interaction timings used when no M3FX theme is installed.
    private static final M3MotionBehavior FALLBACK_MOTION_BEHAVIOR = M3MotionBehavior.standard();

    /// Prevents instantiation.
    private M3Animation() {
    }

    /// Returns whether animations are enabled for the supplied owner.
    ///
    /// @param owner the node whose local, inherited, or global motion settings should be resolved
    /// @return `true` when animations should play for the owner
    public static boolean areAnimationsEnabled(Node owner) {
        return M3MotionSettings.areAnimationsEnabled(Objects.requireNonNull(owner, "owner"));
    }

    /// Returns whether an owner should use reduced motion behavior.
    ///
    /// Reduced motion disables finite visual transitions, shape morphs, easing-based state changes, and entrance or
    /// exit motion. Activity indicators may still run simple linear motion so indeterminate progress remains visibly
    /// alive without playing the full Material motion treatment.
    ///
    /// @param owner the node whose local, inherited, or global motion settings should be resolved
    /// @return `true` when finite transitions should be settled and activity indicators should use reduced motion
    public static boolean shouldReduceMotion(Node owner) {
        return !areAnimationsEnabled(owner);
    }

    /// Returns the semantic motion scheme for an owner node.
    ///
    /// @param owner the node whose theme motion scheme should be resolved
    /// @return the resolved motion scheme
    public static M3MotionScheme motionScheme(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable M3Theme theme = M3ThemeResolver.findTheme(owner);
        return theme == null ? FALLBACK_MOTION_SCHEME : theme.tokens().motionTokens().scheme();
    }

    /// Returns the semantic motion behavior for an owner node.
    ///
    /// @param owner the node whose theme motion behavior should be resolved
    /// @return the resolved motion behavior
    public static M3MotionBehavior motionBehavior(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable M3Theme theme = M3ThemeResolver.findTheme(owner);
        return theme == null ? FALLBACK_MOTION_BEHAVIOR : theme.tokens().motionTokens().behavior();
    }

    /// Returns the fast effects motion spec for an owner node.
    ///
    /// @param owner the node whose motion scheme should be resolved
    /// @return the resolved fast effects motion spec
    public static M3MotionSpec fastEffects(Node owner) {
        return motionScheme(owner).fastEffects();
    }

    /// Returns the default effects motion spec for an owner node.
    ///
    /// @param owner the node whose motion scheme should be resolved
    /// @return the resolved default effects motion spec
    public static M3MotionSpec defaultEffects(Node owner) {
        return motionScheme(owner).defaultEffects();
    }

    /// Returns the slow effects motion spec for an owner node.
    ///
    /// @param owner the node whose motion scheme should be resolved
    /// @return the resolved slow effects motion spec
    public static M3MotionSpec slowEffects(Node owner) {
        return motionScheme(owner).slowEffects();
    }

    /// Returns the fast spatial motion spec for an owner node.
    ///
    /// @param owner the node whose motion scheme should be resolved
    /// @return the resolved fast spatial motion spec
    public static M3MotionSpec fastSpatial(Node owner) {
        return motionScheme(owner).fastSpatial();
    }

    /// Returns the default spatial motion spec for an owner node.
    ///
    /// @param owner the node whose motion scheme should be resolved
    /// @return the resolved default spatial motion spec
    public static M3MotionSpec defaultSpatial(Node owner) {
        return motionScheme(owner).defaultSpatial();
    }

    /// Returns the slow spatial motion spec for an owner node.
    ///
    /// @param owner the node whose motion scheme should be resolved
    /// @return the resolved slow spatial motion spec
    public static M3MotionSpec slowSpatial(Node owner) {
        return motionScheme(owner).slowSpatial();
    }

    /// Copies the resolved reduced-motion request from a scene control into a detached popup root.
    ///
    /// @param source the node whose resolved motion settings should be copied
    /// @param target the detached node that should receive the equivalent reduced-motion request
    public static void copyResolvedMotionSettings(Node source, Node target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        if (M3MotionSettings.areAnimationsEnabled(source)) {
            M3MotionSettings.setReducedMotionRequested(target, false);
        } else {
            M3MotionSettings.setReducedMotionRequested(target, true);
        }
    }

    /// Plays a finite transition from the beginning or finishes it immediately when animations are disabled.
    ///
    /// @param owner      the node whose animation settings should be honored
    /// @param transition the transition to play or finish
    public static void playFromStart(Node owner, M3FiniteTransition transition) {
        Objects.requireNonNull(transition, "transition");
        if (areAnimationsEnabled(owner)) {
            transition.playFromStart();
        } else {
            finish(transition);
        }
    }

    /// Finishes a finite transition synchronously and invokes its completion handler.
    ///
    /// @param transition the transition to settle at its final state
    public static void finish(M3FiniteTransition transition) {
        Objects.requireNonNull(transition, "transition");
        transition.stop();
        transition.applyEndValues();
        invokeOnFinished(transition.getOnFinished());
    }

    /// Finishes a finite transition only when it is currently running.
    ///
    /// @param transition the transition to inspect
    public static void finishIfRunning(M3FiniteTransition transition) {
        Objects.requireNonNull(transition, "transition");
        if (transition.getStatus() == Animation.Status.RUNNING) {
            finish(transition);
        }
    }

    /// Finishes running finite transitions when the owner currently resolves animations as disabled.
    ///
    /// This is used by component-state transitions that already honor disabled motion when starting,
    /// but also need to settle when an application disables motion while the transition is in flight.
    ///
    /// @param owner       the node whose inherited animation switch should be resolved
    /// @param transitions the transitions to settle when disabled
    public static void finishRunningAnimationsIfDisabled(Node owner, M3FiniteTransition... transitions) {
        Objects.requireNonNull(transitions, "transitions");
        if (areAnimationsEnabled(owner)) {
            return;
        }
        for (M3FiniteTransition transition : transitions) {
            finishIfRunning(transition);
        }
    }

    /// Updates the duration of a pause transition and restarts it when requested.
    ///
    /// This helper is used for motion-behavior timings such as type-ahead reset and submenu hover delays.
    /// JavaFX animations should not have timing properties changed while they are running, so the transition is
    /// stopped before its duration is replaced. Running transitions are restarted only when the caller still has
    /// an active interaction that should keep the timer alive.
    ///
    /// @param transition       the pause transition to update
    /// @param duration         the new transition duration
    /// @param restartIfRunning whether a running transition should restart after the duration changes
    public static void updatePauseDuration(
            PauseTransition transition,
            Duration duration,
            boolean restartIfRunning
    ) {
        Objects.requireNonNull(transition, "transition");
        Objects.requireNonNull(duration, "duration");

        boolean running = transition.getStatus() == Animation.Status.RUNNING;
        if (running) {
            transition.stop();
        }

        transition.setDuration(duration);

        if (running && restartIfRunning) {
            transition.playFromStart();
        }
    }

    /// Invokes an animation completion handler when one is present.
    private static void invokeOnFinished(@Nullable EventHandler<ActionEvent> handler) {
        if (handler != null) {
            handler.handle(new ActionEvent());
        }
    }

}
