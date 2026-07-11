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
import java.util.Arrays;

/// Tracks focus-visible changes for Material state feedback.
@NotNullByDefault
final class M3FocusVisibleTracker {
    /// Resolves JavaFX native focus-visible support when it is available at runtime.
    private static final @Nullable MethodHandle FOCUS_VISIBLE_PROPERTY_HANDLE = focusVisiblePropertyHandle();

    /// The pseudo-class used by M3FX CSS for keyboard-visible focus feedback.
    static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// Opaque scene property key for the fallback input tracker.
    private static final Object SCENE_INPUT_TRACKER_KEY = new Object();

    /// Opaque owner property key for fallback focus-visible trackers.
    private static final Object OWNER_TRACKERS_KEY = new Object();

    /// The owner node whose focus-visible state is tracked.
    private final Node owner;

    /// Called after focus visibility changes.
    private final Runnable invalidation;

    /// Handles native JavaFX focus-visible changes when native support is available.
    private final @Nullable ChangeListener<Boolean> nativeFocusVisibleListener;

    /// The native JavaFX focus-visible property, or `null` when the runtime does not expose it.
    private final @Nullable ReadOnlyBooleanProperty nativeFocusVisibleProperty;

    /// The fallback observation, created only on runtimes without native focus-visible support.
    private @Nullable FallbackObservation fallbackObservation;

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
        this.nativeFocusVisibleProperty = nativeFocusVisibleProperty;
        this.nativeFocusVisibleListener = nativeFocusVisibleProperty == null
                ? null
                : (observable, oldValue, newValue) -> updateFocusVisible();
    }

    /// Installs native focus-visible or fallback event listeners.
    void install() {
        if (installed) {
            return;
        }

        installed = true;
        boolean initiallyFocusVisible = owner.getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS);
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        ChangeListener<Boolean> nativeListener = nativeFocusVisibleListener;
        if (nativeProperty != null && nativeListener != null) {
            nativeProperty.addListener(nativeListener);
        } else {
            FallbackObservation observation = new FallbackObservation();
            fallbackObservation = observation;
            observation.install();
        }
        updateFocusVisible(false);
        if (initiallyFocusVisible) {
            owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, true);
        }
    }

    /// Uninstalls native focus-visible or fallback event listeners.
    void uninstall() {
        if (!installed) {
            return;
        }

        installed = false;
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        ChangeListener<Boolean> nativeListener = nativeFocusVisibleListener;
        if (nativeProperty != null && nativeListener != null) {
            nativeProperty.removeListener(nativeListener);
        } else {
            FallbackObservation observation = fallbackObservation;
            fallbackObservation = null;
            if (observation != null) {
                observation.uninstall();
            }
        }
        owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, false);
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible() {
        updateFocusVisible(true);
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible(boolean notify) {
        boolean focusVisible = isNativeFocusVisible() || owner.isFocused() && isFallbackKeyboardInteraction();
        owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
        if (notify) {
            invalidation.run();
        }
    }

    /// Returns whether JavaFX reports native keyboard-visible focus for the owner.
    private boolean isNativeFocusVisible() {
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        return nativeProperty != null && nativeProperty.get();
    }

    /// Returns whether this tracker's fallback scene most recently received keyboard input.
    private boolean isFallbackKeyboardInteraction() {
        FallbackObservation observation = fallbackObservation;
        return observation != null && observation.isKeyboardInteraction();
    }

    /// Returns or creates the fallback input tracker for one scene.
    private static SceneInputTracker fallbackTracker(Scene scene) {
        @Nullable SceneInputTracker tracker = sceneInputTracker(scene);
        if (tracker == null) {
            tracker = new SceneInputTracker(scene);
            scene.getProperties().put(SCENE_INPUT_TRACKER_KEY, tracker);
        }
        return tracker;
    }

    /// Returns the fallback input tracker currently owned by a scene.
    private static @Nullable SceneInputTracker sceneInputTracker(Scene scene) {
        Object value = scene.getProperties().get(SCENE_INPUT_TRACKER_KEY);
        return value instanceof SceneInputTracker tracker ? tracker : null;
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

    /// Owns the listeners and scene registration needed only by fallback focus-visible tracking.
    @NotNullByDefault
    private final class FallbackObservation {
        /// Handles owner focus changes.
        private final ChangeListener<Boolean> focusedListener =
                (observable, oldValue, newValue) -> updateFocusVisible();

        /// Handles owner scene changes.
        private final ChangeListener<@Nullable Scene> sceneListener = (observable, oldScene, newScene) -> {
            detachFromScene();
            attachToScene(newScene);
            updateFocusVisible();
        };

        /// The scene currently registered for fallback input tracking.
        private @Nullable Scene scene;

        /// Installs the fallback owner listeners and scene registration.
        private void install() {
            owner.sceneProperty().addListener(sceneListener);
            owner.focusedProperty().addListener(focusedListener);
            attachToScene(owner.getScene());
        }

        /// Removes all fallback listeners and scene registration.
        private void uninstall() {
            detachFromScene();
            owner.sceneProperty().removeListener(sceneListener);
            owner.focusedProperty().removeListener(focusedListener);
        }

        /// Registers the outer tracker with one scene input tracker.
        private void attachToScene(@Nullable Scene newScene) {
            if (newScene == null) {
                return;
            }

            scene = newScene;
            fallbackTracker(newScene).add(M3FocusVisibleTracker.this);
        }

        /// Unregisters the outer tracker from its current scene input tracker.
        private void detachFromScene() {
            Scene currentScene = scene;
            if (currentScene == null) {
                return;
            }

            scene = null;
            @Nullable SceneInputTracker tracker = sceneInputTracker(currentScene);
            if (tracker != null && tracker.remove(M3FocusVisibleTracker.this)) {
                currentScene.getProperties().remove(SCENE_INPUT_TRACKER_KEY);
            }
        }

        /// Returns whether the current scene most recently received keyboard input.
        private boolean isKeyboardInteraction() {
            Scene currentScene = scene;
            if (currentScene == null) {
                return false;
            }

            @Nullable SceneInputTracker tracker = sceneInputTracker(currentScene);
            return tracker != null && tracker.keyboardInteraction;
        }
    }

    /// Tracks fallback input modality for one scene.
    @NotNullByDefault
    private static final class SceneInputTracker {
        /// The scene whose input events are tracked.
        private final Scene scene;

        /// Handles key presses as keyboard interaction.
        private final EventHandler<KeyEvent> keyPressedHandler = event -> updateModality(true);

        /// Handles mouse presses as pointer interaction.
        private final EventHandler<MouseEvent> mousePressedHandler = event -> updateModality(false);

        /// Whether the latest fallback interaction in this scene was keyboard-driven.
        private boolean keyboardInteraction;

        /// The number of focus-visible trackers registered in this scene.
        private int trackerCount;

        /// Creates a scene-level fallback input tracker.
        private SceneInputTracker(Scene scene) {
            this.scene = scene;
        }

        /// Adds one focus-visible tracker to this scene.
        private void add(M3FocusVisibleTracker tracker) {
            if (trackerCount++ == 0) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            }
            addOwnerTracker(tracker.owner, tracker);
        }

        /// Updates this scene's fallback modality and all registered trackers for its focus owner.
        private void updateModality(boolean keyboard) {
            keyboardInteraction = keyboard;
            @Nullable Node focusOwner = scene.getFocusOwner();
            if (focusOwner == null || !focusOwner.hasProperties()) {
                return;
            }

            Object value = focusOwner.getProperties().get(OWNER_TRACKERS_KEY);
            if (value instanceof M3FocusVisibleTracker tracker) {
                tracker.updateFocusVisible();
            } else if (value instanceof M3FocusVisibleTracker[] trackers) {
                for (M3FocusVisibleTracker tracker : trackers) {
                    tracker.updateFocusVisible();
                }
            }
        }

        /// Removes one focus-visible tracker and returns whether this scene tracker is empty.
        private boolean remove(M3FocusVisibleTracker tracker) {
            if (!removeOwnerTracker(tracker.owner, tracker)) {
                return false;
            }

            if (--trackerCount != 0) {
                return false;
            }

            scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            return true;
        }

        /// Adds one tracker to the compact owner-node registry.
        private static void addOwnerTracker(Node owner, M3FocusVisibleTracker tracker) {
            Object value = owner.getProperties().get(OWNER_TRACKERS_KEY);
            if (value == null) {
                owner.getProperties().put(OWNER_TRACKERS_KEY, tracker);
            } else if (value instanceof M3FocusVisibleTracker current) {
                owner.getProperties().put(OWNER_TRACKERS_KEY, new M3FocusVisibleTracker[]{current, tracker});
            } else if (value instanceof M3FocusVisibleTracker[] trackers) {
                M3FocusVisibleTracker[] expanded = Arrays.copyOf(trackers, trackers.length + 1);
                expanded[trackers.length] = tracker;
                owner.getProperties().put(OWNER_TRACKERS_KEY, expanded);
            }
        }

        /// Removes one tracker from the compact owner-node registry.
        private static boolean removeOwnerTracker(Node owner, M3FocusVisibleTracker tracker) {
            if (!owner.hasProperties()) {
                return false;
            }

            Object value = owner.getProperties().get(OWNER_TRACKERS_KEY);
            if (value == tracker) {
                owner.getProperties().remove(OWNER_TRACKERS_KEY);
                return true;
            }
            if (!(value instanceof M3FocusVisibleTracker[] trackers)) {
                return false;
            }

            for (int index = 0; index < trackers.length; index++) {
                if (trackers[index] != tracker) {
                    continue;
                }

                if (trackers.length == 2) {
                    owner.getProperties().put(OWNER_TRACKERS_KEY, trackers[1 - index]);
                } else {
                    M3FocusVisibleTracker[] compacted = new M3FocusVisibleTracker[trackers.length - 1];
                    System.arraycopy(trackers, 0, compacted, 0, index);
                    System.arraycopy(trackers, index + 1, compacted, index, trackers.length - index - 1);
                    owner.getProperties().put(OWNER_TRACKERS_KEY, compacted);
                }
                return true;
            }
            return false;
        }
    }
}
