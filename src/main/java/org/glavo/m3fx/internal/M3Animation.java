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
import org.glavo.m3fx.animation.M3MotionSettings;
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
}
