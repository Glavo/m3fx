// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/// Observes runtime M3FX motion settings and window activity while an owner node is attached to a scene.
///
/// `M3MotionSettingsObserver` registers shared scene listeners only while the owner participates in a scene graph,
/// preventing detached controls from being retained by [M3MotionSettings]. The supplied action is invoked immediately
/// after attachment, after every relevant settings change, and when the scene's window attachment or showing state
/// changes. Notifications raised off the JavaFX application thread are coalesced and dispatched on that thread.
@NotNullByDefault
public final class M3MotionSettingsObserver {
    /// Opaque scene property key for the shared motion-settings dispatcher.
    private static final Object SCENE_OBSERVER_KEY = new Object();

    /// Empty observer array reused as the snapshot array type token.
    private static final M3MotionSettingsObserver[] EMPTY_OBSERVERS = new M3MotionSettingsObserver[0];

    /// The node whose scene attachment controls listener lifetime.
    private final Node owner;

    /// The action invoked when motion settings may affect the owner.
    private final Runnable refreshAction;

    /// The listener that updates global listener registration when the owner moves between scenes.
    private final InvalidationListener sceneListener;

    /// The scene dispatcher currently responsible for this observer.
    private @Nullable SceneObserver registeredSceneObserver;

    /// Whether this observer has been disposed.
    private volatile boolean disposed;

    /// Creates an observer for one owner node.
    ///
    /// @param owner the node whose scene attachment controls listener lifetime
    /// @param refreshAction the action invoked when motion settings may affect the owner
    public M3MotionSettingsObserver(Node owner, Runnable refreshAction) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.refreshAction = Objects.requireNonNull(refreshAction, "refreshAction");
        this.sceneListener = observable -> updateRegistration();
        owner.sceneProperty().addListener(sceneListener);
        updateRegistration();
    }

    /// Refreshes the owner after the shared scene dispatcher reaches the JavaFX application thread.
    private void refresh() {
        SceneObserver currentObserver = registeredSceneObserver;
        if (!disposed
                && currentObserver != null
                && owner.getScene() == currentObserver.scene) {
            refreshAction.run();
        }
    }

    /// Stops observing scene and runtime motion setting changes.
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        owner.sceneProperty().removeListener(sceneListener);
        unregisterSceneObserver();
    }

    /// Updates whether the observer is registered for global motion setting changes.
    private void updateRegistration() {
        if (disposed) {
            return;
        }
        @Nullable Scene scene = owner.getScene();
        SceneObserver currentObserver = registeredSceneObserver;
        if (currentObserver != null && currentObserver.scene == scene) {
            return;
        }

        boolean wasRegistered = currentObserver != null;
        unregisterSceneObserver();
        if (scene != null) {
            SceneObserver nextObserver = sceneObserver(scene);
            nextObserver.add(this);
            registeredSceneObserver = nextObserver;
            refresh();
        } else if (wasRegistered) {
            refreshAction.run();
        }
    }

    /// Removes this observer from its current scene dispatcher and tears down an empty dispatcher.
    private void unregisterSceneObserver() {
        SceneObserver currentObserver = registeredSceneObserver;
        if (currentObserver == null) {
            return;
        }

        registeredSceneObserver = null;
        if (currentObserver.remove(this)
                && currentObserver.scene.getProperties().get(SCENE_OBSERVER_KEY) == currentObserver) {
            currentObserver.scene.getProperties().remove(SCENE_OBSERVER_KEY);
        }
    }

    /// Returns the dispatcher owned by a scene, creating it when needed.
    ///
    /// @param scene the scene that owns the dispatcher
    /// @return the existing or created dispatcher
    private static SceneObserver sceneObserver(Scene scene) {
        Object value = scene.getProperties().get(SCENE_OBSERVER_KEY);
        if (value instanceof SceneObserver observer) {
            return observer;
        }

        SceneObserver observer = new SceneObserver(scene);
        scene.getProperties().put(SCENE_OBSERVER_KEY, observer);
        return observer;
    }

    /// Dispatches one global motion-settings notification to all observers in a scene.
    @NotNullByDefault
    private static final class SceneObserver {
        /// The scene that owns this dispatcher.
        private final Scene scene;

        /// Observers currently attached to nodes in the scene.
        private final ArrayList<M3MotionSettingsObserver> observers = new ArrayList<>();

        /// Receives global and node-local motion-settings revisions.
        private final InvalidationListener settingsListener = observable -> requestRefresh();

        /// Receives changes to the window that presents the scene.
        private final ChangeListener<@Nullable Window> windowListener =
                (observable, oldWindow, newWindow) -> updateWindow(newWindow, true);

        /// Receives showing-state changes from the current scene window.
        private final InvalidationListener windowShowingListener = observable -> requestRefresh();

        /// The window whose showing state is currently observed.
        private @Nullable Window observedWindow;

        /// Whether an off-thread settings notification already scheduled one FX-thread refresh.
        private volatile boolean refreshPending;

        /// Creates a dispatcher owned by one scene.
        private SceneObserver(Scene scene) {
            this.scene = scene;
        }

        /// Adds an observer and installs the single global listener when needed.
        private void add(M3MotionSettingsObserver observer) {
            boolean wasEmpty = observers.isEmpty();
            observers.add(observer);
            if (wasEmpty) {
                M3MotionSettings.addSettingsChangeListener(settingsListener);
                scene.windowProperty().addListener(windowListener);
                updateWindow(scene.getWindow(), false);
            }
        }

        /// Removes an observer and returns whether the dispatcher became empty.
        private boolean remove(M3MotionSettingsObserver observer) {
            int index = -1;
            for (int i = 0; i < observers.size(); i++) {
                if (observers.get(i) == observer) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                return false;
            }

            observers.remove(index);
            if (!observers.isEmpty()) {
                return false;
            }
            M3MotionSettings.removeSettingsChangeListener(settingsListener);
            scene.windowProperty().removeListener(windowListener);
            updateWindow(null, false);
            return true;
        }

        /// Reattaches the shared showing-state listener to the scene's current window.
        private void updateWindow(@Nullable Window window, boolean refresh) {
            if (observedWindow == window) {
                if (refresh) {
                    requestRefresh();
                }
                return;
            }

            if (observedWindow != null) {
                observedWindow.showingProperty().removeListener(windowShowingListener);
            }
            observedWindow = window;
            if (window != null) {
                window.showingProperty().addListener(windowShowingListener);
            }
            if (refresh) {
                requestRefresh();
            }
        }

        /// Coalesces off-thread notifications into one JavaFX application-thread dispatch per scene.
        private void requestRefresh() {
            if (Platform.isFxApplicationThread()) {
                refreshObservers();
                return;
            }

            synchronized (this) {
                if (refreshPending) {
                    return;
                }
                refreshPending = true;
            }
            Platform.runLater(() -> {
                refreshPending = false;
                refreshObservers();
            });
        }

        /// Refreshes a stable observer snapshot so callbacks may detach nodes safely.
        private void refreshObservers() {
            M3MotionSettingsObserver[] snapshot = observers.toArray(EMPTY_OBSERVERS);
            for (M3MotionSettingsObserver observer : snapshot) {
                observer.refresh();
            }
        }
    }
}
