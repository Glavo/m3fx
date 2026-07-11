// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Maintains the transient normalized prefix used by keyboard type-ahead navigation.
///
/// The reset transition and motion observer are created only after the first accepted key. The observer remains
/// installed only while a prefix is active, so controls that never use type-ahead navigation do not participate in
/// runtime motion dispatch.
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
        if (motionSettingsObserver == null) {
            motionSettingsObserver = new M3MotionSettingsObserver(owner, this::refreshMotionSettings);
        }
        delay.setDuration(M3Animation.motionBehavior(owner).typeAheadResetDelay());
        delay.playFromStart();
    }

    /// Applies runtime motion-behavior changes to an active reset timer.
    private void refreshMotionSettings() {
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
}
