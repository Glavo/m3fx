// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Base transition whose final values can be applied synchronously without starting a JavaFX pulse.
///
/// JavaFX does not guarantee that jumping a stopped custom [Transition] to its total duration invokes
/// `interpolate(1)`. M3FX finite transitions expose this operation so reduced-motion paths can settle reliably.
@NotNullByDefault
public abstract class M3FiniteTransition extends Transition {
    /// The owner whose resolved motion setting controls the current run.
    private @Nullable Node motionOwner;

    /// Observes runtime motion changes only while this transition is running.
    private @Nullable M3MotionSettingsObserver motionSettingsObserver;

    /// Creates a finite transition.
    protected M3FiniteTransition() {
        statusProperty().addListener((observable, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.STOPPED) {
                stopMotionObservation();
            }
        });
    }

    /// Plays this transition from the beginning while observing the owner's resolved motion setting.
    ///
    /// The observer is registered immediately before playback and released as soon as the transition stops. Reusing
    /// a transition therefore does not leave an idle control subscribed to scene-wide motion changes.
    ///
    /// @param owner the node whose inherited motion setting controls this run
    final void playFromStart(Node owner) {
        Node checkedOwner = Objects.requireNonNull(owner, "owner");
        if (!M3Animation.areAnimationsEnabled(checkedOwner)) {
            M3Animation.finish(this);
            return;
        }

        startMotionObservation(checkedOwner);
        super.playFromStart();
    }

    /// Applies the configured final values.
    final void applyEndValues() {
        interpolate(1.0);
    }

    /// Starts or transfers the observer used by the current run.
    private void startMotionObservation(Node owner) {
        M3MotionSettingsObserver observer = motionSettingsObserver;
        if (observer == null || motionOwner != owner) {
            if (observer != null) {
                observer.dispose();
            }
            motionOwner = owner;
            observer = new M3MotionSettingsObserver(owner, this::refreshMotionSettings, false);
            motionSettingsObserver = observer;
        }
        observer.start();
    }

    /// Settles this transition if reduced motion is requested during playback.
    private void refreshMotionSettings() {
        @Nullable Node owner = motionOwner;
        if (owner != null
                && getStatus() == Animation.Status.RUNNING
                && !M3Animation.areAnimationsEnabled(owner)) {
            M3Animation.finish(this);
        }
    }

    /// Releases active scene observation while preserving the reusable observer object.
    private void stopMotionObservation() {
        M3MotionSettingsObserver observer = motionSettingsObserver;
        if (observer != null) {
            observer.stop();
        }
    }
}
