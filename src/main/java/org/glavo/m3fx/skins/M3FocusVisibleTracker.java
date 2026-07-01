// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/// Tracks focus-visible changes for Material state feedback.
@NotNullByDefault
final class M3FocusVisibleTracker {
    /// Resolves JavaFX native focus-visible support when it is available at runtime.
    private static final @Nullable MethodHandle FOCUS_VISIBLE_PROPERTY_HANDLE = focusVisiblePropertyHandle();

    /// The pseudo-class used by m3fx CSS for keyboard-visible focus feedback.
    static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// Scene-level fallback input trackers keyed weakly by scene.
    private static final Map<Scene, SceneInputTracker> SCENE_INPUT_TRACKERS = new WeakHashMap<>();

    /// The owner node whose focus-visible state is tracked.
    private final Node owner;

    /// Called after focus visibility changes.
    private final Runnable invalidation;

    /// Handles owner focus changes.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> updateFocusVisible();

    /// Handles owner scene changes in fallback mode.
    private final ChangeListener<@Nullable Scene> sceneListener = (observable, oldScene, newScene) -> {
        detachFromFallbackScene();
        attachToFallbackScene(newScene);
        updateFocusVisible();
    };

    /// Handles native JavaFX focus-visible changes.
    private final ChangeListener<Boolean> nativeFocusVisibleListener;

    /// The native JavaFX focus-visible property, or `null` when the runtime does not expose it.
    private final @Nullable ReadOnlyBooleanProperty nativeFocusVisibleProperty;

    /// The scene currently registered for fallback input tracking.
    private @Nullable Scene fallbackScene;

    /// Whether this tracker is currently installed.
    private boolean installed;

    /// Creates a focus-visible tracker.
    M3FocusVisibleTracker(Node owner, Runnable invalidation) {
        this(owner, invalidation, nativeFocusVisibleProperty(owner));
    }

    /// Creates a focus-visible tracker with an explicit native focus-visible property.
    M3FocusVisibleTracker(
            Node owner,
            Runnable invalidation,
            @Nullable ReadOnlyBooleanProperty nativeFocusVisibleProperty
    ) {
        this.owner = owner;
        this.invalidation = invalidation;
        this.nativeFocusVisibleListener = (observable, oldValue, newValue) -> invalidation.run();
        this.nativeFocusVisibleProperty = nativeFocusVisibleProperty;
    }

    /// Installs native focus-visible or fallback event listeners.
    void install() {
        if (installed) {
            return;
        }

        installed = true;
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        if (nativeProperty != null) {
            nativeProperty.addListener(nativeFocusVisibleListener);
            return;
        }

        owner.sceneProperty().addListener(sceneListener);
        owner.focusedProperty().addListener(focusedListener);
        attachToFallbackScene(owner.getScene());
        updateFocusVisible(false);
    }

    /// Uninstalls native focus-visible or fallback event listeners.
    void uninstall() {
        if (!installed) {
            return;
        }

        installed = false;
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        if (nativeProperty != null) {
            nativeProperty.removeListener(nativeFocusVisibleListener);
            return;
        }

        detachFromFallbackScene();
        owner.sceneProperty().removeListener(sceneListener);
        owner.focusedProperty().removeListener(focusedListener);
        owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, false);
    }

    /// Registers this tracker with one fallback scene input tracker.
    private void attachToFallbackScene(@Nullable Scene scene) {
        if (scene == null) {
            return;
        }

        fallbackScene = scene;
        fallbackTracker(scene).add(this);
    }

    /// Unregisters this tracker from its current fallback scene input tracker.
    private void detachFromFallbackScene() {
        Scene scene = fallbackScene;
        if (scene == null) {
            return;
        }

        fallbackScene = null;
        @Nullable SceneInputTracker tracker = SCENE_INPUT_TRACKERS.get(scene);
        if (tracker != null && tracker.remove(this)) {
            SCENE_INPUT_TRACKERS.remove(scene);
        }
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible() {
        updateFocusVisible(true);
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible(boolean notify) {
        boolean focusVisible = owner.isFocused() && isFallbackKeyboardInteraction();
        owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
        if (notify) {
            invalidation.run();
        }
    }

    /// Returns whether this tracker's fallback scene most recently received keyboard input.
    private boolean isFallbackKeyboardInteraction() {
        Scene scene = fallbackScene;
        if (scene == null) {
            return false;
        }

        @Nullable SceneInputTracker tracker = SCENE_INPUT_TRACKERS.get(scene);
        return tracker != null && tracker.isKeyboardInteraction();
    }

    /// Returns or creates the fallback input tracker for one scene.
    private static SceneInputTracker fallbackTracker(Scene scene) {
        SceneInputTracker tracker = SCENE_INPUT_TRACKERS.get(scene);
        if (tracker == null) {
            tracker = new SceneInputTracker(scene);
            SCENE_INPUT_TRACKERS.put(scene, tracker);
        }
        return tracker;
    }

    /// Returns a method handle for JavaFX native focus-visible support when the runtime exposes it.
    private static @Nullable MethodHandle focusVisiblePropertyHandle() {
        try {
            return MethodHandles.publicLookup().findVirtual(
                    Node.class,
                    "focusVisibleProperty",
                    MethodType.methodType(ReadOnlyBooleanProperty.class)
            );
        } catch (IllegalAccessException | NoSuchMethodException ignored) {
            return null;
        }
    }

    /// Returns the native JavaFX focus-visible property for the owner when it is available.
    private static @Nullable ReadOnlyBooleanProperty nativeFocusVisibleProperty(Node owner) {
        MethodHandle handle = FOCUS_VISIBLE_PROPERTY_HANDLE;
        if (handle == null) {
            return null;
        }

        try {
            return (ReadOnlyBooleanProperty) handle.invoke(owner);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /// Tracks fallback input modality for one scene.
    @NotNullByDefault
    private static final class SceneInputTracker {
        /// The scene whose input events are tracked.
        private final WeakReference<Scene> sceneReference;

        /// The installed focus-visible trackers in this scene.
        private final Set<M3FocusVisibleTracker> trackers =
                Collections.newSetFromMap(new IdentityHashMap<>());

        /// Handles key presses as keyboard interaction.
        private final EventHandler<KeyEvent> keyPressedHandler = event -> updateModality(true);

        /// Handles mouse presses as pointer interaction.
        private final EventHandler<MouseEvent> mousePressedHandler = event -> updateModality(false);

        /// Whether the latest fallback interaction in this scene was keyboard-driven.
        private boolean keyboardInteraction;

        /// Creates a scene-level fallback input tracker.
        private SceneInputTracker(Scene scene) {
            this.sceneReference = new WeakReference<>(scene);
        }

        /// Adds one focus-visible tracker to this scene.
        private void add(M3FocusVisibleTracker tracker) {
            @Nullable Scene scene = sceneReference.get();
            if (scene == null) {
                return;
            }

            if (trackers.isEmpty()) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            }
            trackers.add(tracker);
        }

        /// Returns whether this scene most recently received keyboard input.
        private boolean isKeyboardInteraction() {
            return keyboardInteraction;
        }

        /// Updates this scene's fallback modality and all registered focus-visible owners.
        private void updateModality(boolean keyboard) {
            keyboardInteraction = keyboard;
            for (M3FocusVisibleTracker tracker : new ArrayList<>(trackers)) {
                tracker.updateFocusVisible();
            }
        }

        /// Removes one focus-visible tracker and returns whether this scene tracker is empty.
        private boolean remove(M3FocusVisibleTracker tracker) {
            trackers.remove(tracker);
            if (!trackers.isEmpty()) {
                return false;
            }

            @Nullable Scene scene = sceneReference.get();
            if (scene != null) {
                scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            }
            return true;
        }
    }
}
