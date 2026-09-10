// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/// Maintains the transient normalized prefix used by keyboard type-ahead navigation.
///
/// The reset transition and motion observer are created only after the first accepted key. The observer remains
/// installed only while a prefix is active, so controls that never use type-ahead navigation do not participate in
/// runtime motion dispatch. An active prefix is cleared when an attached owner leaves its scene or when the scene's
/// window becomes hidden. An owner that has not yet entered a scene, or whose visible scene has no window, can still
/// use the inactivity timer for headless interaction.
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

    /// Searches for a target after accepting one printable Unicode code point from a typed key event.
    ///
    /// Shortcut-modified events, non-typed events, whitespace, control characters, unpaired surrogates, and input
    /// containing multiple code points are ignored without changing the prefix or timer. Shift is permitted.
    /// Accepted input is normalized, appended to the current prefix, and restarts the inactivity timer. The finder
    /// receives that prefix; if it returns `null` and a previous prefix existed, the new character replaces the
    /// prefix and the finder is called once more. The last attempted prefix remains active even if no target is
    /// found or the finder throws. Owner lifecycle changes may clear the prefix and cancel the search.
    ///
    /// This method must run on the JavaFX application thread. It does not consume the event or change focus or
    /// selection; the caller applies the returned target according to its control's interaction policy.
    ///
    /// @param event the key event to inspect
    /// @param targetFinder finds a target for a normalized prefix, returning `null` when no target matches
    /// @param <T> the target type
    /// @return the matching target, or `null` when input is ignored, canceled, or unmatched
    /// @throws NullPointerException if `event` or `targetFinder` is `null`
    public <T> @Nullable T find(KeyEvent event, Function<String, @Nullable T> targetFinder) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(targetFinder, "targetFinder");
        if (event.getEventType() != KeyEvent.KEY_TYPED || M3KeyEvents.hasShortcutModifier(event)) {
            return null;
        }

        String character = event.getCharacter();
        if (character.codePointCount(0, character.length()) != 1 || character.isBlank()) {
            return null;
        }
        int codePoint = character.codePointAt(0);
        if (Character.isISOControl(codePoint)
                || codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE) {
            return null;
        }

        String normalizedCharacter = M3SelectionNavigation.normalizeTypeAheadText(character);
        prefix += normalizedCharacter;
        restartResetDelay();
        if (prefix.isEmpty()) {
            return null;
        }
        @Nullable T target = targetFinder.apply(prefix);
        if (target == null && prefix.length() > normalizedCharacter.length()) {
            prefix = normalizedCharacter;
            target = targetFinder.apply(prefix);
        }
        return target;
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
        return !M3PresentationActivity.isTreeVisible(owner)
                || window != null && !M3PresentationActivity.isRenderActive(window);
    }
}
