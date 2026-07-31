// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Maintains the transient normalized prefix used by keyboard type-ahead navigation.
///
/// The reset transition and motion observer are created only after the first accepted key. The observer remains
/// installed only while a prefix is active, so controls that never use type-ahead navigation do not participate in
/// runtime motion dispatch. An active prefix is cleared when an attached owner leaves its scene or when the scene's
/// window becomes hidden. An owner that has not yet entered a scene, or whose scene has no window, can still use the
/// inactivity timer for headless interaction.
@NotNullByDefault
public final class M3TypeAheadState {
    /// The control that supplies inherited motion behavior.
    private final Node owner;

    /// The current normalized search prefix.
    private String prefix = "";

    /// The inactivity timer retained only while a prefix is active.
    private @Nullable PauseTransition resetDelay;

    /// The observer installed only while a prefix is active.
    private @Nullable M3MotionSettingsObserver motionSettingsObserver;

    /// Whether the current reset delay has observed the owner in a scene.
    private boolean resetDelayObservedScene;

    /// Creates an inactive type-ahead state for one control.
    ///
    /// @param owner the control that owns the type-ahead interaction
    public M3TypeAheadState(Node owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    /// Returns the current normalized search prefix.
    ///
    /// @return the current prefix, or the empty string when inactive
    public String getPrefix() {
        return prefix;
    }

    /// Returns the number of UTF-16 code units in the current prefix.
    ///
    /// @return the current prefix length
    public int length() {
        return prefix.length();
    }

    /// Appends normalized text and restarts the inactivity timer.
    ///
    /// @param text the non-null normalized text to append
    public void append(String text) {
        Objects.requireNonNull(text, "text");
        if (text.isEmpty()) {
            return;
        }
        prefix += text;
        restartResetDelay();
    }

    /// Replaces the active prefix and restarts the inactivity timer.
    ///
    /// Supplying an empty string clears the interaction and releases its active motion observer.
    ///
    /// @param text the normalized replacement prefix
    public void replace(String text) {
        Objects.requireNonNull(text, "text");
        if (text.isEmpty()) {
            clear();
            return;
        }
        prefix = text;
        restartResetDelay();
    }

    /// Clears the prefix, stops its inactivity timer, and releases active motion observation.
    public void clear() {
        prefix = "";
        resetDelayObservedScene = false;
        @Nullable PauseTransition delay = resetDelay;
        if (delay != null) {
            resetDelay = null;
            delay.stop();
            delay.setOnFinished(null);
        }
        @Nullable M3MotionSettingsObserver observer = motionSettingsObserver;
        if (observer != null) {
            motionSettingsObserver = null;
            observer.dispose();
        }
    }

    /// Creates the inactivity timer on first use and starts it with the current motion behavior.
    private void restartResetDelay() {
        @Nullable PauseTransition delay = resetDelay;
        if (delay == null) {
            delay = new PauseTransition();
            delay.setOnFinished(event -> clear());
            resetDelay = delay;
        }
        if (owner.getScene() != null) {
            resetDelayObservedScene = true;
        }
        delay.setDuration(M3Animation.motionBehavior(owner).typeAheadResetDelay());
        if (motionSettingsObserver == null) {
            M3MotionSettingsObserver observer =
                    new M3MotionSettingsObserver(owner, this::refreshMotionSettings, false);
            motionSettingsObserver = observer;
            observer.start();
        }
        if (resetDelay == delay && !prefix.isEmpty()) {
            delay.playFromStart();
        }
    }

    /// Applies runtime motion-behavior changes to an active reset timer.
    private void refreshMotionSettings() {
        if (ownerCannotRetainResetDelay()) {
            clear();
            return;
        }
        @Nullable PauseTransition delay = resetDelay;
        if (prefix.isEmpty() || delay == null) {
            return;
        }
        M3Animation.updatePauseDuration(
                delay,
                M3Animation.motionBehavior(owner).typeAheadResetDelay(),
                true
        );
    }

    /// Returns whether the active delay must be discarded for the owner's current presentation lifecycle.
    ///
    /// A null scene is considered unavailable only after this delay has observed a non-null scene. This distinction
    /// preserves type-ahead behavior for controls exercised without a scene while still cancelling work when a live
    /// control is detached.
    private boolean ownerCannotRetainResetDelay() {
        @Nullable Scene scene = owner.getScene();
        if (scene == null) {
            return resetDelayObservedScene;
        }

        resetDelayObservedScene = true;
        @Nullable Window window = scene.getWindow();
        return window != null && !M3WindowActivity.isRenderActive(window);
    }
}
