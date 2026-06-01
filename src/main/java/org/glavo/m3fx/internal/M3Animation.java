// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/// Internal helpers for honoring the M3FX motion settings around JavaFX animations.
@NotNullByDefault
public final class M3Animation {
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
    /// @param owner the node whose local, theme, or global motion scheme should be resolved
    /// @return the resolved motion scheme
    public static M3MotionScheme motionScheme(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable M3MotionScheme override = findMotionSchemeOverride(owner);
        if (override != null) {
            return override;
        }

        @Nullable M3Theme theme = M3ThemeResolver.findTheme(owner);
        return theme == null ? M3MotionSettings.getMotionScheme() : theme.tokens().motionTokens().scheme();
    }

    /// Returns the semantic motion behavior for an owner node.
    ///
    /// @param owner the node whose local, theme, or global motion behavior should be resolved
    /// @return the resolved motion behavior
    public static M3MotionBehavior motionBehavior(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable M3MotionBehavior override = findMotionBehaviorOverride(owner);
        if (override != null) {
            return override;
        }

        @Nullable M3Theme theme = M3ThemeResolver.findTheme(owner);
        return theme == null ? M3MotionSettings.getMotionBehavior() : theme.tokens().motionTokens().behavior();
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

    /// Copies the resolved motion settings from a scene control into a detached popup root.
    ///
    /// @param source the node whose resolved motion settings should be copied
    /// @param target the detached node that should receive equivalent local motion settings
    public static void copyResolvedMotionSettings(Node source, Node target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        M3MotionSettings.setAnimationsEnabled(target, M3MotionSettings.areAnimationsEnabled(source));
        M3MotionSettings.setMotionScheme(target, motionScheme(source));
        M3MotionSettings.setMotionBehavior(target, motionBehavior(source));
    }

    /// Plays an animation from the beginning or finishes it immediately when animations are disabled.
    ///
    /// @param owner the node whose animation settings should be honored
    /// @param animation the animation to play or finish
    public static void playFromStart(Node owner, Animation animation) {
        Objects.requireNonNull(animation, "animation");
        if (areAnimationsEnabled(owner)) {
            animation.playFromStart();
        } else {
            finish(animation);
        }
    }

    /// Plays an animation from its current time or finishes it immediately when animations are disabled.
    ///
    /// @param owner the node whose animation settings should be honored
    /// @param animation the animation to play or finish
    public static void play(Node owner, Animation animation) {
        Objects.requireNonNull(animation, "animation");
        if (areAnimationsEnabled(owner)) {
            animation.play();
        } else {
            finish(animation);
        }
    }

    /// Finishes an animation synchronously and invokes its completion handlers.
    ///
    /// @param animation the animation to settle at its final state
    public static void finish(Animation animation) {
        finish(animation, true);
    }

    /// Finishes an animation only when it is currently running.
    ///
    /// @param animation the animation to inspect
    /// @return `true` when the animation was running and has been finished
    public static boolean finishIfRunning(Animation animation) {
        Objects.requireNonNull(animation, "animation");
        if (animation.getStatus() != Animation.Status.RUNNING) {
            return false;
        }
        finish(animation);
        return true;
    }

    /// Finishes running animations when the owner currently resolves animations as disabled.
    ///
    /// This is used by finite component-state transitions that already honor disabled motion when starting,
    /// but also need to settle when an application disables motion while the transition is in flight.
    ///
    /// @param owner the node whose inherited animation switch should be resolved
    /// @param animations the animations to settle when disabled
    public static void finishRunningAnimationsIfDisabled(Node owner, Animation... animations) {
        Objects.requireNonNull(animations, "animations");
        if (areAnimationsEnabled(owner)) {
            return;
        }
        for (Animation animation : animations) {
            finishIfRunning(animation);
        }
    }

    /// Finishes an animation, optionally skipping `stop()` for animations embedded in a parent transition.
    ///
    /// @param animation the animation to settle at its final state
    /// @param stop whether to stop the animation before applying final values
    private static void finish(Animation animation, boolean stop) {
        Objects.requireNonNull(animation, "animation");
        if (stop) {
            animation.stop();
        }
        if (animation instanceof Timeline timeline) {
            finishTimeline(timeline);
        } else if (animation instanceof ParallelTransition parallelTransition) {
            for (Animation child : parallelTransition.getChildren()) {
                finish(child, false);
            }
        } else {
            Duration totalDuration = animation.getTotalDuration();
            if (isFinite(totalDuration)) {
                animation.jumpTo(totalDuration);
            }
        }
        invokeOnFinished(animation.getOnFinished());
    }

    /// Finishes a timeline by applying its final key values and final key-frame callbacks.
    private static void finishTimeline(Timeline timeline) {
        List<KeyFrame> keyFrames = new ArrayList<>(timeline.getKeyFrames());
        keyFrames.sort(Comparator.comparingDouble(frame -> frame.getTime().toMillis()));
        @Nullable Duration finalTime = null;
        for (KeyFrame keyFrame : keyFrames) {
            Duration time = keyFrame.getTime();
            if (isFinite(time) && (finalTime == null || time.greaterThan(finalTime))) {
                finalTime = time;
            }
            for (KeyValue keyValue : keyFrame.getValues()) {
                applyKeyValue(keyValue);
            }
        }

        if (finalTime == null) {
            return;
        }

        for (KeyFrame keyFrame : keyFrames) {
            if (keyFrame.getTime().equals(finalTime)) {
                invokeOnFinished(keyFrame.getOnFinished());
            }
        }
    }

    /// Applies one key value to its target writable value.
    @SuppressWarnings("unchecked")
    private static void applyKeyValue(KeyValue keyValue) {
        WritableValue<Object> target = (WritableValue<Object>) keyValue.getTarget();
        target.setValue(keyValue.getEndValue());
    }

    /// Invokes an animation completion handler when one is present.
    private static void invokeOnFinished(@Nullable EventHandler<ActionEvent> handler) {
        if (handler != null) {
            handler.handle(new ActionEvent());
        }
    }

    /// Returns whether a duration can be used as a concrete animation endpoint.
    private static boolean isFinite(Duration duration) {
        return !duration.isUnknown() && !duration.isIndefinite();
    }

    /// Finds the nearest node-local motion scheme override.
    private static @Nullable M3MotionScheme findMotionSchemeOverride(Node owner) {
        @Nullable Node current = owner;
        while (current != null) {
            @Nullable M3MotionScheme override = M3MotionSettings.getMotionScheme(current);
            if (override != null) {
                return override;
            }
            current = current.getParent();
        }
        return null;
    }

    /// Finds the nearest node-local motion behavior override.
    private static @Nullable M3MotionBehavior findMotionBehaviorOverride(Node owner) {
        @Nullable Node current = owner;
        while (current != null) {
            @Nullable M3MotionBehavior override = M3MotionSettings.getMotionBehavior(current);
            if (override != null) {
                return override;
            }
            current = current.getParent();
        }
        return null;
    }

}
