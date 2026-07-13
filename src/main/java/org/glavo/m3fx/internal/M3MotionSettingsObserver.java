// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/// Observes runtime M3FX motion settings and window activity while an owner node is attached to a scene.
///
/// Observers that share an owner also share one owner coordinator and one scene-property listener. Scene dispatchers
/// register coordinators rather than individual callbacks, so controls with several animated features do not multiply
/// scene and window observation overhead. Notifications raised off the JavaFX application thread are coalesced and
/// dispatched on that thread. Callback dispatch does not allocate snapshots and remains stable when callbacks dispose
/// subscriptions or move their owner to another scene.
@NotNullByDefault
public final class M3MotionSettingsObserver {
    /// Opaque owner property key for the shared owner coordinator.
    private static final Object OWNER_OBSERVER_KEY = new Object();

    /// Opaque scene property key for the shared motion-settings dispatcher.
    private static final Object SCENE_OBSERVER_KEY = new Object();

    /// Internal listeners notified when global settings or one local settings subtree changes.
    private static final CopyOnWriteArrayList<Consumer<@Nullable Node>> SETTINGS_CHANGE_LISTENERS =
            new CopyOnWriteArrayList<>();

    /// Empty nullable observer storage reused before the first owner subscription.
    private static final @Nullable M3MotionSettingsObserver[] EMPTY_OBSERVERS =
            new M3MotionSettingsObserver[0];

    /// Empty nullable owner storage reused before the first scene registration.
    private static final @Nullable OwnerObserver[] EMPTY_OWNERS = new OwnerObserver[0];

    /// The shared coordinator for this observer's owner.
    private final OwnerObserver ownerObserver;

    /// The action invoked when motion settings may affect the owner.
    private final Runnable refreshAction;

    /// Whether this observer has been disposed.
    private volatile boolean disposed;

    /// Creates an observer for one owner node.
    ///
    /// @param owner         the node whose scene attachment controls listener lifetime
    /// @param refreshAction the action invoked when motion settings may affect the owner
    public M3MotionSettingsObserver(Node owner, Runnable refreshAction) {
        Objects.requireNonNull(owner, "owner");
        this.refreshAction = Objects.requireNonNull(refreshAction, "refreshAction");
        this.ownerObserver = ownerObserver(owner);
        try {
            ownerObserver.add(this);
        } catch (RuntimeException | Error exception) {
            disposed = true;
            ownerObserver.remove(this);
            throw exception;
        }
    }

    /// Stops observing scene and runtime motion setting changes.
    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        ownerObserver.remove(this);
    }

    /// Invokes this observer's callback unless it has been disposed.
    private void refresh() {
        if (!disposed) {
            refreshAction.run();
        }
    }

    /// Returns the coordinator owned by a node, creating it when needed.
    ///
    /// @param owner the node that owns the coordinator
    /// @return the existing or created owner coordinator
    private static OwnerObserver ownerObserver(Node owner) {
        Object value = owner.getProperties().get(OWNER_OBSERVER_KEY);
        if (value instanceof OwnerObserver observer) {
            return observer;
        }

        OwnerObserver observer = new OwnerObserver(owner);
        owner.getProperties().put(OWNER_OBSERVER_KEY, observer);
        return observer;
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

    /// Notifies internal motion observers after the public settings revision has advanced.
    ///
    /// @param source the root of the affected subtree, or `null` for a global change
    public static void settingsChanged(@Nullable Node source) {
        for (Consumer<@Nullable Node> listener : SETTINGS_CHANGE_LISTENERS) {
            listener.accept(source);
        }
    }

    /// Registers one internal settings listener.
    static void addSettingsChangeListener(Consumer<@Nullable Node> listener) {
        SETTINGS_CHANGE_LISTENERS.add(Objects.requireNonNull(listener, "listener"));
    }

    /// Removes one internal settings listener.
    static void removeSettingsChangeListener(Consumer<@Nullable Node> listener) {
        SETTINGS_CHANGE_LISTENERS.remove(Objects.requireNonNull(listener, "listener"));
    }

    /// Shares owner lifecycle observation across all subscriptions attached to one node.
    @NotNullByDefault
    private static final class OwnerObserver {
        /// The node whose scene attachment controls this coordinator.
        private final Node owner;

        /// The scene dispatcher currently responsible for this owner.
        private @Nullable SceneObserver registeredSceneObserver;

        /// Re-registers this coordinator when the owner moves between scenes.
        private final InvalidationListener sceneListener = observable -> updateRegistration();

        /// Refreshes inherited settings when the owner moves between parents without leaving its scene.
        private final InvalidationListener parentListener = observable -> refreshAfterParentChange();

        /// Nullable observer slots; removals during dispatch leave temporary tombstones.
        private @Nullable M3MotionSettingsObserver[] observers = EMPTY_OBSERVERS;

        /// The number of occupied observer slots, including temporary tombstones.
        private int observerSlots;

        /// The number of live observers.
        private int observerCount;

        /// The number of active callback dispatches.
        private int dispatchDepth;

        /// Whether a nested notification requested another pass.
        private boolean refreshRequested;

        /// Changes whenever this owner transfers between scene dispatchers.
        private long registrationVersion;

        /// Creates a coordinator for one owner node.
        ///
        /// @param owner the node whose subscriptions are coordinated
        private OwnerObserver(Node owner) {
            this.owner = owner;
        }

        /// Adds one subscription and installs owner lifecycle observation when it is the first.
        ///
        /// @param observer the subscription to add
        private void add(M3MotionSettingsObserver observer) {
            boolean wasEmpty = observerCount == 0;
            append(observer);
            if (wasEmpty) {
                owner.sceneProperty().addListener(sceneListener);
                owner.parentProperty().addListener(parentListener);
                updateRegistration();
                return;
            }

            SceneObserver sceneObserver = registeredSceneObserver;
            if (sceneObserver != null && owner.getScene() == sceneObserver.scene) {
                observer.refresh();
            }
        }

        /// Removes one subscription and tears down this coordinator when it becomes empty.
        ///
        /// @param observer the subscription to remove
        private void remove(M3MotionSettingsObserver observer) {
            int index = indexOf(observer);
            if (index < 0) {
                return;
            }

            if (dispatchDepth == 0) {
                int moved = observerSlots - index - 1;
                if (moved > 0) {
                    System.arraycopy(observers, index + 1, observers, index, moved);
                }
                observers[--observerSlots] = null;
            } else {
                observers[index] = null;
            }
            observerCount--;

            if (observerCount == 0) {
                refreshRequested = false;
                unregisterSceneObserver();
                owner.sceneProperty().removeListener(sceneListener);
                owner.parentProperty().removeListener(parentListener);
                if (owner.getProperties().get(OWNER_OBSERVER_KEY) == this) {
                    owner.getProperties().remove(OWNER_OBSERVER_KEY);
                }
            } else if (dispatchDepth == 0) {
                compactObservers();
            }
        }

        /// Appends one observer, growing compact storage only during registration changes.
        ///
        /// @param observer the observer to append
        private void append(M3MotionSettingsObserver observer) {
            if (observerSlots == observers.length) {
                int currentCapacity = observers.length;
                int nextCapacity = currentCapacity == 0 ? 4 : currentCapacity + (currentCapacity >> 1);
                observers = Arrays.copyOf(observers, nextCapacity);
            }
            observers[observerSlots++] = observer;
            observerCount++;
        }

        /// Returns the identity index of one observer, or -1 when it is absent.
        ///
        /// @param observer the observer to find
        /// @return its slot index, or -1
        private int indexOf(M3MotionSettingsObserver observer) {
            for (int index = 0; index < observerSlots; index++) {
                if (observers[index] == observer) {
                    return index;
                }
            }
            return -1;
        }

        /// Updates scene registration after owner attachment changes.
        private void updateRegistration() {
            @Nullable Scene scene = owner.getScene();
            SceneObserver currentObserver = registeredSceneObserver;
            if (currentObserver == null ? scene == null : currentObserver.scene == scene) {
                return;
            }

            registrationVersion++;
            unregisterSceneObserver();
            if (scene != null) {
                SceneObserver nextObserver = sceneObserver(scene);
                nextObserver.add(this);
                registeredSceneObserver = nextObserver;
            }
            requestRefresh();
        }

        /// Refreshes inherited settings only when a parent change leaves the owner in its registered scene.
        private void refreshAfterParentChange() {
            SceneObserver sceneObserver = registeredSceneObserver;
            if (owner.getParent() != null
                    && sceneObserver != null
                    && owner.getScene() == sceneObserver.scene) {
                requestRefresh();
            }
        }

        /// Removes this owner from its current scene and releases an empty scene dispatcher.
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

        /// Refreshes this owner's subscriptions when called by its current scene dispatcher.
        ///
        /// @param source the scene dispatcher requesting the refresh
        private void refreshFrom(SceneObserver source) {
            if (registeredSceneObserver == source && owner.getScene() == source.scene) {
                requestRefresh();
            }
        }

        /// Dispatches callbacks without allocating snapshots and coalesces nested refresh requests.
        private void requestRefresh() {
            if (observerCount == 0) {
                return;
            }
            if (dispatchDepth != 0) {
                refreshRequested = true;
                return;
            }

            do {
                refreshRequested = false;
                long dispatchRegistrationVersion = registrationVersion;
                int dispatchLimit = observerSlots;
                dispatchDepth++;
                try {
                    for (int index = 0; index < dispatchLimit; index++) {
                        @Nullable M3MotionSettingsObserver observer = observers[index];
                        if (observer != null) {
                            observer.refresh();
                        }
                        if (registrationVersion != dispatchRegistrationVersion) {
                            break;
                        }
                    }
                } finally {
                    dispatchDepth--;
                    compactObservers();
                }
            } while (refreshRequested && observerCount != 0);
        }

        /// Compacts observer tombstones after the outermost dispatch.
        private void compactObservers() {
            if (dispatchDepth != 0) {
                return;
            }

            if (observerSlots != observerCount) {
                int destination = 0;
                for (int source = 0; source < observerSlots; source++) {
                    @Nullable M3MotionSettingsObserver observer = observers[source];
                    if (observer != null) {
                        observers[destination++] = observer;
                    }
                }
                Arrays.fill(observers, destination, observerSlots, null);
                observerSlots = destination;
            }

            int capacity = observers.length;
            if (observerCount != 0 && capacity > 8 && observerCount <= (capacity >> 2)) {
                int targetCapacity = Math.max(4, observerCount + (observerCount >> 1));
                observers = Arrays.copyOf(observers, targetCapacity);
            }
        }
    }

    /// Dispatches global settings and window lifecycle notifications once per scene.
    @NotNullByDefault
    private static final class SceneObserver {
        /// The scene that owns this dispatcher.
        private final Scene scene;

        /// Nullable owner slots; removals during dispatch leave temporary tombstones.
        private @Nullable OwnerObserver[] owners = EMPTY_OWNERS;

        /// The number of occupied owner slots, including temporary tombstones.
        private int ownerSlots;

        /// The number of live owner coordinators.
        private int ownerCount;

        /// Receives global and node-local motion-settings revisions.
        private final Consumer<@Nullable Node> settingsListener = this::requestRefresh;

        /// Receives changes to the window that presents the scene.
        private final ChangeListener<@Nullable Window> windowListener =
                (observable, oldWindow, newWindow) -> updateWindow(newWindow, true);

        /// Receives showing-state changes from the current scene window.
        private final InvalidationListener windowShowingListener = observable -> requestRefresh(null);

        /// The window whose showing state is currently observed.
        private @Nullable Window observedWindow;

        /// The number of active owner dispatches.
        private int dispatchDepth;

        /// Whether an off-thread settings notification already scheduled one FX-thread refresh.
        private volatile boolean refreshPending;

        /// The common local source retained while one off-thread refresh is pending.
        private @Nullable Node pendingSource;

        /// Whether pending changes require refreshing every owner in this scene.
        private boolean refreshAllPending;

        /// Creates a dispatcher owned by one scene.
        ///
        /// @param scene the scene that owns the dispatcher
        private SceneObserver(Scene scene) {
            this.scene = scene;
        }

        /// Adds one owner coordinator and installs shared settings listeners when it is the first.
        ///
        /// @param ownerObserver the owner coordinator to add
        private void add(OwnerObserver ownerObserver) {
            if (ownerSlots == owners.length) {
                int currentCapacity = owners.length;
                int nextCapacity = currentCapacity == 0 ? 8 : currentCapacity + (currentCapacity >> 1);
                owners = Arrays.copyOf(owners, nextCapacity);
            }
            owners[ownerSlots++] = ownerObserver;
            if (ownerCount++ == 0) {
                addSettingsChangeListener(settingsListener);
                scene.windowProperty().addListener(windowListener);
                updateWindow(scene.getWindow(), false);
            }
        }

        /// Removes one owner coordinator and returns whether this dispatcher became empty.
        ///
        /// @param ownerObserver the owner coordinator to remove
        /// @return true when no owner coordinators remain
        private boolean remove(OwnerObserver ownerObserver) {
            int index = indexOf(ownerObserver);
            if (index < 0) {
                return false;
            }

            if (dispatchDepth == 0) {
                int moved = ownerSlots - index - 1;
                if (moved > 0) {
                    System.arraycopy(owners, index + 1, owners, index, moved);
                }
                owners[--ownerSlots] = null;
            } else {
                owners[index] = null;
            }

            if (--ownerCount != 0) {
                if (dispatchDepth == 0) {
                    compactOwners();
                }
                return false;
            }

            removeSettingsChangeListener(settingsListener);
            scene.windowProperty().removeListener(windowListener);
            updateWindow(null, false);
            return true;
        }

        /// Returns the identity index of one owner coordinator, or -1 when it is absent.
        ///
        /// @param ownerObserver the owner coordinator to find
        /// @return its slot index, or -1
        private int indexOf(OwnerObserver ownerObserver) {
            for (int index = 0; index < ownerSlots; index++) {
                if (owners[index] == ownerObserver) {
                    return index;
                }
            }
            return -1;
        }

        /// Reattaches the shared showing-state listener to the scene's current window.
        ///
        /// @param window  the new window, or null when detached
        /// @param refresh whether subscribers should be refreshed
        private void updateWindow(@Nullable Window window, boolean refresh) {
            if (observedWindow == window) {
                if (refresh) {
                    requestRefresh(null);
                }
                return;
            }

            Window currentWindow = observedWindow;
            if (currentWindow != null) {
                currentWindow.showingProperty().removeListener(windowShowingListener);
            }
            observedWindow = window;
            if (window != null) {
                window.showingProperty().addListener(windowShowingListener);
            }
            if (refresh) {
                requestRefresh(null);
            }
        }

        /// Coalesces off-thread notifications into one JavaFX application-thread dispatch per scene.
        private void requestRefresh(@Nullable Node source) {
            if (Platform.isFxApplicationThread()) {
                refreshOwners(source);
                return;
            }

            synchronized (this) {
                if (refreshPending) {
                    if (!refreshAllPending && pendingSource != source) {
                        pendingSource = null;
                        refreshAllPending = true;
                    }
                    return;
                }
                refreshPending = true;
                pendingSource = source;
                refreshAllPending = source == null;
            }
            Platform.runLater(() -> {
                @Nullable Node sourceToRefresh;
                synchronized (this) {
                    refreshPending = false;
                    sourceToRefresh = refreshAllPending ? null : pendingSource;
                    pendingSource = null;
                    refreshAllPending = false;
                }
                refreshOwners(sourceToRefresh);
            });
        }

        /// Refreshes affected owner coordinators without allocating a per-notification snapshot.
        private void refreshOwners(@Nullable Node source) {
            if (source != null && source.getScene() != scene) {
                return;
            }
            int dispatchLimit = ownerSlots;
            dispatchDepth++;
            try {
                for (int index = 0; index < dispatchLimit; index++) {
                    @Nullable OwnerObserver ownerObserver = owners[index];
                    if (ownerObserver != null && (source == null || contains(source, ownerObserver.owner))) {
                        ownerObserver.refreshFrom(this);
                    }
                }
            } finally {
                dispatchDepth--;
                compactOwners();
            }
        }

        /// Returns whether one owner belongs to the subtree rooted at the changed settings node.
        private static boolean contains(Node source, Node owner) {
            @Nullable Node current = owner;
            while (current != null) {
                if (current == source) {
                    return true;
                }
                current = current.getParent();
            }
            return false;
        }

        /// Compacts owner tombstones after the outermost dispatch.
        private void compactOwners() {
            if (dispatchDepth != 0) {
                return;
            }

            if (ownerSlots != ownerCount) {
                int destination = 0;
                for (int source = 0; source < ownerSlots; source++) {
                    @Nullable OwnerObserver ownerObserver = owners[source];
                    if (ownerObserver != null) {
                        owners[destination++] = ownerObserver;
                    }
                }
                Arrays.fill(owners, destination, ownerSlots, null);
                ownerSlots = destination;
            }

            int capacity = owners.length;
            if (ownerCount != 0 && capacity > 16 && ownerCount <= (capacity >> 2)) {
                int targetCapacity = Math.max(8, ownerCount + (ownerCount >> 1));
                owners = Arrays.copyOf(owners, targetCapacity);
            }
        }
    }
}
