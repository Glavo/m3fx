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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
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
    public static boolean areAnimationsEnabled(Node owner) {
        return M3MotionSettings.areAnimationsEnabled(Objects.requireNonNull(owner, "owner"));
    }

    /// Returns the semantic motion scheme for an owner node.
    public static M3MotionScheme motionScheme(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable M3MotionScheme override = findMotionSchemeOverride(owner);
        if (override != null) {
            return override;
        }

        @Nullable M3Theme theme = findTheme(owner);
        return theme == null ? M3MotionSettings.getMotionScheme() : theme.tokens().motionTokens().scheme();
    }

    /// Returns the fast effects motion spec for an owner node.
    public static M3MotionSpec fastEffects(Node owner) {
        return motionScheme(owner).fastEffects();
    }

    /// Returns the default effects motion spec for an owner node.
    public static M3MotionSpec defaultEffects(Node owner) {
        return motionScheme(owner).defaultEffects();
    }

    /// Returns the slow effects motion spec for an owner node.
    public static M3MotionSpec slowEffects(Node owner) {
        return motionScheme(owner).slowEffects();
    }

    /// Returns the fast spatial motion spec for an owner node.
    public static M3MotionSpec fastSpatial(Node owner) {
        return motionScheme(owner).fastSpatial();
    }

    /// Returns the default spatial motion spec for an owner node.
    public static M3MotionSpec defaultSpatial(Node owner) {
        return motionScheme(owner).defaultSpatial();
    }

    /// Returns the slow spatial motion spec for an owner node.
    public static M3MotionSpec slowSpatial(Node owner) {
        return motionScheme(owner).slowSpatial();
    }

    /// Copies the resolved motion settings from a scene control into a detached popup root.
    public static void copyResolvedMotionSettings(Node source, Node target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        M3MotionSettings.setAnimationsEnabled(target, M3MotionSettings.areAnimationsEnabled(source));
        M3MotionSettings.setMotionScheme(target, motionScheme(source));
    }

    /// Plays an animation from the beginning or finishes it immediately when animations are disabled.
    public static void playFromStart(Node owner, Animation animation) {
        Objects.requireNonNull(animation, "animation");
        if (areAnimationsEnabled(owner)) {
            animation.playFromStart();
        } else {
            finish(animation);
        }
    }

    /// Plays an animation from its current time or finishes it immediately when animations are disabled.
    public static void play(Node owner, Animation animation) {
        Objects.requireNonNull(animation, "animation");
        if (areAnimationsEnabled(owner)) {
            animation.play();
        } else {
            finish(animation);
        }
    }

    /// Finishes an animation synchronously and invokes its completion handlers.
    public static void finish(Animation animation) {
        Objects.requireNonNull(animation, "animation");
        animation.stop();
        if (animation instanceof Timeline timeline) {
            finishTimeline(timeline);
        } else if (animation instanceof ParallelTransition parallelTransition) {
            for (Animation child : parallelTransition.getChildren()) {
                finish(child);
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

    /// Finds the theme that controls an owner node, when one was installed through [M3ThemeManager].
    private static @Nullable M3Theme findTheme(Node owner) {
        @Nullable Scene scene = owner.getScene();
        if (scene != null) {
            @Nullable M3Theme sceneTheme = M3ThemeManager.getTheme(scene);
            if (sceneTheme != null) {
                return sceneTheme;
            }
        }

        @Nullable Node current = owner;
        while (current != null) {
            if (current instanceof Parent parent) {
                @Nullable M3Theme parentTheme = M3ThemeManager.getTheme(parent);
                if (parentTheme != null) {
                    return parentTheme;
                }
            }
            current = current.getParent();
        }
        return null;
    }
}
