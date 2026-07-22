// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

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

/// Tracks focus-visible state for Material interaction feedback.
///
/// Native JavaFX focus-visible state is used when exposed by the running JavaFX version. M3FX traversal explicitly
/// marks focus that it moves without the platform traversal engine. JavaFX 17 does not expose the native property,
/// so trackers on the compatibility baseline also derive keyboard focus from a shared scene-level input-modality
/// observer. [#install()] and [#uninstall()] are idempotent and must delimit the tracker's active lifetime.
@NotNullByDefault
public final class M3FocusVisibleTracker {
    /// Resolves post-JavaFX-17 native focus-visible support when it is available at runtime.
    private static final @Nullable MethodHandle FOCUS_VISIBLE_PROPERTY_HANDLE = focusVisiblePropertyHandle();

    /// The pseudo-class used by M3FX CSS for keyboard-visible focus feedback.
    public static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// Opaque scene property key for the shared input tracker.
    private static final IdentityKey SCENE_INPUT_TRACKER_KEY =
            new IdentityKey(M3FocusVisibleTracker.class.getName() + ".sceneInputTracker");

    /// Opaque owner property key for installed focus-visible trackers.
    private static final IdentityKey OWNER_TRACKERS_KEY =
            new IdentityKey(M3FocusVisibleTracker.class.getName() + ".ownerTrackers");

    /// The owner node whose focus-visible state is tracked.
    private final Node owner;

    /// Called after an installed tracker reevaluates focus-visible state.
    private final Runnable invalidation;

    /// Handles native JavaFX focus-visible changes when native support is available.
    private final @Nullable ChangeListener<Boolean> nativeFocusVisibleListener;

    /// The native JavaFX focus-visible property, or `null` when the runtime does not expose it.
    private final @Nullable ReadOnlyBooleanProperty nativeFocusVisibleProperty;

    /// Whether this tracker is currently installed.
    private boolean installed;

    /// Whether this tracker currently contributes keyboard-visible focus to its owner.
    private boolean focusVisible;

    /// Whether M3FX custom keyboard traversal explicitly focused this owner.
    private boolean keyboardTraversalFocusVisible;

    /// Handles owner focus changes for both native and fallback focus-visible tracking.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> {
        if (!newValue) {
            keyboardTraversalFocusVisible = false;
        }
        updateFocusVisible();
    };

    /// Handles owner eligibility changes that may invalidate visible focus feedback.
    private final ChangeListener<Boolean> eligibilityListener =
            (observable, oldValue, newValue) -> updateFocusVisible();

    /// The shared scene input observation used for pointer clearing and JavaFX 17 fallback modality.
    private @Nullable SceneObservation sceneObservation;

    /// Creates a focus-visible tracker using the focus-visible capability available at runtime.
    ///
    /// @param owner        the node whose focus-visible pseudo-class is maintained
    /// @param invalidation the callback invoked after installed state is reevaluated
    public M3FocusVisibleTracker(Node owner, Runnable invalidation) {
        this(owner, invalidation, nativeFocusVisibleProperty(owner));
    }

    /// Creates a focus-visible tracker with an explicit native focus-visible property.
    ///
    /// @param owner                      the node whose focus-visible pseudo-class is maintained
    /// @param invalidation               the callback invoked after installed state is reevaluated
    /// @param nativeFocusVisibleProperty the native focus-visible property, or `null` to use fallback tracking
    public M3FocusVisibleTracker(
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

    /// Installs native focus-visible and shared scene-input listeners.
    public void install() {
        if (installed) {
            return;
        }

        installed = true;
        boolean initiallyFocusVisible = owner.getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS);
        addOwnerTracker(owner, this);
        owner.focusedProperty().addListener(focusedListener);
        owner.disabledProperty().addListener(eligibilityListener);
        owner.visibleProperty().addListener(eligibilityListener);
        SceneObservation observation = new SceneObservation();
        sceneObservation = observation;
        observation.install();
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        ChangeListener<Boolean> nativeListener = nativeFocusVisibleListener;
        if (nativeProperty != null && nativeListener != null) {
            nativeProperty.addListener(nativeListener);
        }
        updateFocusVisible(false);
        if (nativeFocusVisibleProperty == null && initiallyFocusVisible && owner.isFocused()) {
            focusVisible = true;
            applyOwnerFocusVisibleState(owner);
        }
    }

    /// Uninstalls native focus-visible and shared scene-input listeners.
    public void uninstall() {
        if (!installed) {
            return;
        }

        owner.focusedProperty().removeListener(focusedListener);
        owner.disabledProperty().removeListener(eligibilityListener);
        owner.visibleProperty().removeListener(eligibilityListener);
        ReadOnlyBooleanProperty nativeProperty = nativeFocusVisibleProperty;
        ChangeListener<Boolean> nativeListener = nativeFocusVisibleListener;
        if (nativeProperty != null && nativeListener != null) {
            nativeProperty.removeListener(nativeListener);
        }
        SceneObservation observation = sceneObservation;
        sceneObservation = null;
        if (observation != null) {
            observation.uninstall();
        }
        installed = false;
        focusVisible = false;
        keyboardTraversalFocusVisible = false;
        removeOwnerTracker(owner, this);
        applyOwnerFocusVisibleState(owner);
    }

    /// Reevaluates state after owner eligibility changes outside the focus properties.
    public void refresh() {
        if (installed) {
            updateFocusVisible();
        }
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible() {
        updateFocusVisible(true);
    }

    /// Updates the focus-visible pseudo-class from the current modality and focus state.
    private void updateFocusVisible(boolean notify) {
        focusVisible = owner.isFocused()
                && !owner.isDisabled()
                && owner.isVisible()
                && (keyboardTraversalFocusVisible || isNativeFocusVisible() || isFallbackKeyboardInteraction());
        applyOwnerFocusVisibleState(owner);
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
        SceneObservation observation = sceneObservation;
        return nativeFocusVisibleProperty == null
                && observation != null
                && observation.isKeyboardInteraction();
    }

    /// Marks the installed trackers on a focused node as originating from M3FX keyboard traversal.
    ///
    /// The marker remains active while the node keeps focus and no pointer press reaches it. Calling this method for
    /// a node without an installed tracker has no effect.
    ///
    /// @param owner the current scene focus owner, or `null` when no node accepted focus
    static void markKeyboardTraversalFocus(@Nullable Node owner) {
        if (owner == null || !owner.hasProperties()) {
            return;
        }

        Object value = owner.getProperties().get(OWNER_TRACKERS_KEY);
        if (value instanceof M3FocusVisibleTracker tracker) {
            tracker.markKeyboardTraversalFocus();
        } else if (value instanceof M3FocusVisibleTracker[] trackers) {
            for (M3FocusVisibleTracker tracker : trackers) {
                tracker.markKeyboardTraversalFocus();
            }
        }
    }

    /// Marks this installed tracker as keyboard-visible when its owner currently holds focus.
    private void markKeyboardTraversalFocus() {
        if (!installed || !owner.isFocused()) {
            return;
        }
        keyboardTraversalFocusVisible = true;
        updateFocusVisible();
    }

    /// Returns or creates the shared input tracker for one scene.
    private static SceneInputTracker inputTracker(Scene scene) {
        @Nullable SceneInputTracker tracker = sceneInputTracker(scene);
        if (tracker == null) {
            tracker = new SceneInputTracker(scene);
            scene.getProperties().put(SCENE_INPUT_TRACKER_KEY, tracker);
        }
        return tracker;
    }

    /// Returns the shared input tracker currently owned by a scene.
    private static @Nullable SceneInputTracker sceneInputTracker(Scene scene) {
        Object value = scene.getProperties().get(SCENE_INPUT_TRACKER_KEY);
        return value instanceof SceneInputTracker tracker ? tracker : null;
    }

    /// Returns a method handle for the focus-visible property introduced after JavaFX 17.
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

    /// Owns the listeners and registration needed to share one input tracker per scene.
    @NotNullByDefault
    private final class SceneObservation {
        /// Handles owner scene changes.
        private final ChangeListener<@Nullable Scene> sceneListener = (observable, oldScene, newScene) -> {
            detachFromScene();
            attachToScene(newScene);
            updateFocusVisible();
        };

        /// The scene currently registered for shared input tracking.
        private @Nullable Scene scene;

        /// Installs the owner listeners and scene registration.
        private void install() {
            owner.sceneProperty().addListener(sceneListener);
            attachToScene(owner.getScene());
        }

        /// Removes all owner listeners and scene registration.
        private void uninstall() {
            detachFromScene();
            owner.sceneProperty().removeListener(sceneListener);
        }

        /// Registers the outer tracker with one shared scene input tracker.
        private void attachToScene(@Nullable Scene newScene) {
            if (newScene == null) {
                return;
            }

            scene = newScene;
            inputTracker(newScene).add();
        }

        /// Unregisters the outer tracker from its current scene input tracker.
        private void detachFromScene() {
            Scene currentScene = scene;
            if (currentScene == null) {
                return;
            }

            scene = null;
            @Nullable SceneInputTracker tracker = sceneInputTracker(currentScene);
            if (tracker != null && tracker.remove()) {
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

    /// Tracks input modality and pointer clearing for one scene.
    @NotNullByDefault
    private static final class SceneInputTracker {
        /// The scene whose input events are tracked.
        private final Scene scene;

        /// Handles key presses as keyboard interaction.
        private final EventHandler<KeyEvent> keyPressedHandler = event -> updateModality(true);

        /// Handles mouse presses as pointer interaction.
        private final EventHandler<MouseEvent> mousePressedHandler = event -> updateModality(false);

        /// Whether the latest interaction in this scene was keyboard-driven.
        private boolean keyboardInteraction;

        /// The number of focus-visible trackers registered in this scene.
        private int trackerCount;

        /// Creates a scene-level input tracker.
        private SceneInputTracker(Scene scene) {
            this.scene = scene;
        }

        /// Adds one focus-visible tracker to this scene.
        private void add() {
            if (trackerCount++ == 0) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            }
        }

        /// Updates this scene's modality and all registered trackers for its focus owner.
        private void updateModality(boolean keyboard) {
            keyboardInteraction = keyboard;
            @Nullable Node focusOwner = scene.getFocusOwner();
            if (focusOwner == null || !focusOwner.hasProperties()) {
                return;
            }

            Object value = focusOwner.getProperties().get(OWNER_TRACKERS_KEY);
            if (value instanceof M3FocusVisibleTracker tracker) {
                tracker.updateSceneInput(keyboard);
            } else if (value instanceof M3FocusVisibleTracker[] trackers) {
                for (M3FocusVisibleTracker tracker : trackers) {
                    tracker.updateSceneInput(keyboard);
                }
            }
        }

        /// Removes one focus-visible tracker and returns whether this scene tracker is empty.
        private boolean remove() {
            if (--trackerCount != 0) {
                return false;
            }

            scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            return true;
        }

    }

    /// Reevaluates fallback modality or clears explicit traversal focus after scene input.
    private void updateSceneInput(boolean keyboard) {
        boolean traversalFocusCleared = !keyboard && keyboardTraversalFocusVisible;
        if (traversalFocusCleared) {
            keyboardTraversalFocusVisible = false;
        }
        if (nativeFocusVisibleProperty == null || traversalFocusCleared) {
            updateFocusVisible();
        }
    }

    /// Applies the combined contribution from every installed tracker on an owner.
    private static void applyOwnerFocusVisibleState(Node owner) {
        Object value = owner.hasProperties() ? owner.getProperties().get(OWNER_TRACKERS_KEY) : null;
        boolean focusVisible = value instanceof M3FocusVisibleTracker tracker
                ? tracker.installed && tracker.focusVisible
                : value instanceof M3FocusVisibleTracker[] trackers && anyFocusVisible(trackers);
        owner.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
    }

    /// Returns whether an owner registry contains an installed focus-visible contributor.
    private static boolean anyFocusVisible(M3FocusVisibleTracker[] trackers) {
        for (M3FocusVisibleTracker tracker : trackers) {
            if (tracker.installed && tracker.focusVisible) {
                return true;
            }
        }
        return false;
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
    private static void removeOwnerTracker(Node owner, M3FocusVisibleTracker tracker) {
        if (!owner.hasProperties()) {
            return;
        }

        Object value = owner.getProperties().get(OWNER_TRACKERS_KEY);
        if (value == tracker) {
            owner.getProperties().remove(OWNER_TRACKERS_KEY);
            return;
        }
        if (!(value instanceof M3FocusVisibleTracker[] trackers)) {
            return;
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
            return;
        }
    }
}
