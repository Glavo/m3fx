// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3Animated;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/// Observes an owner's animation property and, while attached, its motion context and presentation activity.
///
/// Observers that share an owner also share one owner coordinator and one tree-visible observation. The native JavaFX
/// property is preferred; the public parent chain is observed when that property is inaccessible. Scene dispatchers
/// register coordinators rather than individual callbacks, so controls with several animated features do not multiply
/// ancestor, scene, or window observation overhead. Notifications raised off the JavaFX application thread are
/// coalesced and dispatched on that thread. Callback dispatch does not allocate snapshots and remains stable when
/// callbacks dispose subscriptions or move their owner to another scene.
/// Application-wide motion settings are not observed while the presenting window is hidden or iconified; restoring
/// the presentation refreshes subscribers against the latest setting.
/// Runtime callback failures are rethrown after notifying the remaining subscribers, with further failures suppressed.
///
/// Construction and lifecycle methods must be invoked on the JavaFX application thread. A disposed observer cannot
/// be restarted. Calling [stop] or [dispose] more than once has no effect.
@NotNullByDefault
public final class M3MotionSettingsObserver {
    /// Opaque owner property key for the shared owner coordinator.
    private static final IdentityKey OWNER_OBSERVER_KEY =
            new IdentityKey(M3MotionSettingsObserver.class.getName() + ".ownerObserver");

    /// Opaque scene property key for the shared motion-settings dispatcher.
    private static final IdentityKey SCENE_OBSERVER_KEY =
            new IdentityKey(M3MotionSettingsObserver.class.getName() + ".sceneObserver");

    /// Empty nullable node storage reused before the first ancestor-chain observation.
    private static final @Nullable Node[] EMPTY_NODES = new Node[0];

    /// The node whose attachment controls observation lifetime.
    private final Node owner;

    /// The shared coordinator used while this observer is active.
    private @Nullable OwnerObserver ownerObserver;

    /// The action invoked when motion settings or presentation activity may affect the owner.
    private final Runnable refreshAction;

    /// Whether this observer has been disposed.
    private volatile boolean disposed;

    /// Whether this observer is currently registered with its owner coordinator.
    private volatile boolean observing;

    /// Creates an observer for one owner node.
    ///
    /// @param owner         the node whose scene attachment controls listener lifetime
    /// @param refreshAction the action invoked when motion settings or presentation activity may affect the owner
    /// @throws NullPointerException if `owner` or `refreshAction` is `null`
    public M3MotionSettingsObserver(Node owner, Runnable refreshAction) {
        this(owner, refreshAction, true);
    }

    /// Creates an observer for one owner node with explicit initial activation.
    ///
    /// An inactive observer does not allocate owner or scene observation state until [start] is called.
    ///
    /// @param owner              the node whose scene attachment controls listener lifetime
    /// @param refreshAction      the action invoked when motion settings or presentation activity may affect the owner
    /// @param observeImmediately whether observation should start during construction
    /// @throws NullPointerException if `owner` or `refreshAction` is `null`
    public M3MotionSettingsObserver(Node owner, Runnable refreshAction, boolean observeImmediately) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.refreshAction = Objects.requireNonNull(refreshAction, "refreshAction");
        if (observeImmediately) {
            start();
        }
    }

    /// Starts observation when it is not already active.
    ///
    /// Calling this method while observation is active has no effect.
    ///
    /// @throws IllegalStateException if this observer has been disposed
    public void start() {
        if (disposed) {
            throw new IllegalStateException("motion settings observer is disposed");
        }
        if (observing) {
            return;
        }

        OwnerObserver coordinator = ownerObserver(owner);
        ownerObserver = coordinator;
        observing = true;
        try {
            coordinator.add(this);
        } catch (RuntimeException | Error exception) {
            observing = false;
            coordinator.remove(this);
            ownerObserver = null;
            throw exception;
        }
    }

    /// Pauses observation without disposing this observer.
    ///
    /// Calling this method while observation is inactive has no effect. A stopped observer may be restarted with
    /// [start] until it is disposed.
    public void stop() {
        if (!observing) {
            return;
        }

        observing = false;
        @Nullable OwnerObserver coordinator = ownerObserver;
        ownerObserver = null;
        if (coordinator != null) {
            coordinator.remove(this);
        }
    }

    /// Permanently stops observing ancestor, scene, window, and runtime motion-setting changes.
    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        stop();
    }

    /// Invokes this observer's callback unless it has been disposed.
    private void refresh() {
        if (!disposed && observing) {
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

    /// Notifies internal motion observers after one subtree's theme motion tokens change.
    ///
    /// Global reduced-motion changes are delivered by the global property's weak per-scene listeners instead.
    /// Detached subtrees have no active scene dispatcher and require no notification.
    ///
    /// @param source the root of the affected subtree
    public static void motionContextChanged(Node source) {
        Objects.requireNonNull(source, "source");
        @Nullable Scene scene = source.getScene();
        if (scene == null || !scene.hasProperties()) {
            return;
        }
        @Nullable Object value = scene.getProperties().get(SCENE_OBSERVER_KEY);
        if (value instanceof SceneObserver observer) {
            observer.requestRefresh(source);
        }
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

        /// Receives local Java or CSS animation-property changes without observing ancestor settings.
        private final ChangeListener<Boolean> animationsEnabledListener =
                (observable, oldValue, newValue) -> requestRefresh();

        /// Refreshes presentation activity when the owner or one of its ancestors changes visibility.
        private final InvalidationListener visibilityListener = observable -> refreshAfterVisibilityChange();

        /// Rebuilds presentation observation when any link in the current ancestor chain changes.
        private final InvalidationListener parentListener = observable -> refreshAfterParentChange();

        /// The native JavaFX tree-visible property currently observed, or `null` while using the public fallback.
        private @Nullable ObservableBooleanValue observedTreeVisibleProperty;

        /// Nullable node slots containing the owner followed by its currently observed ancestors.
        private @Nullable Node[] observedNodes = EMPTY_NODES;

        /// The number of occupied slots in the observed ancestor chain.
        private int observedNodeCount;

        /// The most recently observed effective tree-visible state.
        private boolean treeVisible;

        /// Active subscriptions, with stable slots during callback dispatch.
        private final M3ObserverList<M3MotionSettingsObserver> observers = new M3ObserverList<>();

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
            boolean wasEmpty = observers.isEmpty();
            observers.add(observer);
            if (wasEmpty) {
                owner.sceneProperty().addListener(sceneListener);
                if (owner instanceof M3Animated animated) {
                    animated.animationsEnabledProperty().addListener(animationsEnabledListener);
                }
                installTreeVisibilityObservation();
                updateRegistration();
                return;
            }

            @Nullable SceneObserver sceneObserver = registeredSceneObserver;
            if (sceneObserver != null && owner.getScene() == sceneObserver.scene) {
                observer.refresh();
            }
        }

        /// Removes one subscription and tears down this coordinator when it becomes empty.
        ///
        /// @param observer the subscription to remove
        private void remove(M3MotionSettingsObserver observer) {
            if (!observers.remove(observer)) {
                return;
            }
            if (observers.isEmpty()) {
                refreshRequested = false;
                unregisterSceneObserver();
                owner.sceneProperty().removeListener(sceneListener);
                if (owner instanceof M3Animated animated) {
                    animated.animationsEnabledProperty().removeListener(animationsEnabledListener);
                }
                releaseTreeVisibilityObservation();
                if (owner.getProperties().get(OWNER_OBSERVER_KEY) == this) {
                    owner.getProperties().remove(OWNER_OBSERVER_KEY);
                }
            }
        }

        /// Updates scene registration after owner attachment changes.
        private void updateRegistration() {
            @Nullable Scene scene = owner.getScene();
            @Nullable SceneObserver currentObserver = registeredSceneObserver;
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

        /// Refreshes subscriptions when effective tree visibility changes.
        private void refreshAfterVisibilityChange() {
            boolean currentTreeVisible = M3PresentationActivity.isTreeVisible(owner);
            if (treeVisible == currentTreeVisible) {
                return;
            }

            treeVisible = currentTreeVisible;
            @Nullable SceneObserver sceneObserver = registeredSceneObserver;
            if (sceneObserver != null && owner.getScene() == sceneObserver.scene) {
                requestRefresh();
            }
        }

        /// Rebuilds ancestor observation and refreshes inherited settings after a parent link changes.
        private void refreshAfterParentChange() {
            @Nullable ObservableBooleanValue property = observedTreeVisibleProperty;
            if (property == null) {
                rebuildObservedChain();
            } else {
                treeVisible = property.get();
            }
            @Nullable SceneObserver sceneObserver = registeredSceneObserver;
            if (sceneObserver != null
                    && owner.getScene() == sceneObserver.scene
                    && (owner.getParent() != null || sceneObserver.scene.getRoot() == owner)) {
                requestRefresh();
            }
        }

        /// Installs native tree-visible observation or a public ancestor-chain fallback.
        private void installTreeVisibilityObservation() {
            @Nullable ObservableBooleanValue property = M3TreeVisibility.treeVisibleProperty(owner);
            observedTreeVisibleProperty = property;
            if (property == null) {
                rebuildObservedChain();
                return;
            }

            property.addListener(visibilityListener);
            owner.parentProperty().addListener(parentListener);
            treeVisible = property.get();
        }

        /// Removes native or fallback tree-visible observation and releases fallback storage.
        private void releaseTreeVisibilityObservation() {
            @Nullable ObservableBooleanValue property = observedTreeVisibleProperty;
            observedTreeVisibleProperty = null;
            if (property != null) {
                property.removeListener(visibilityListener);
                owner.parentProperty().removeListener(parentListener);
            }
            releaseObservedChain();
        }

        /// Rebuilds visible and parent-property observation from the owner through its current public parent root.
        private void rebuildObservedChain() {
            removeObservedChain();
            @Nullable Node current = owner;
            while (current != null) {
                ensureObservedNodeCapacity();
                observedNodes[observedNodeCount++] = current;
                current.visibleProperty().addListener(visibilityListener);
                current.parentProperty().addListener(parentListener);
                current = current.getParent();
            }
            treeVisible = M3PresentationActivity.isTreeVisible(owner);
        }

        /// Grows ancestor storage before one node is appended.
        private void ensureObservedNodeCapacity() {
            if (observedNodeCount < observedNodes.length) {
                return;
            }
            int currentCapacity = observedNodes.length;
            int nextCapacity = currentCapacity == 0 ? 8 : currentCapacity + (currentCapacity >> 1);
            observedNodes = Arrays.copyOf(observedNodes, nextCapacity);
        }

        /// Removes listeners from the currently observed ancestor chain while retaining reusable storage.
        private void removeObservedChain() {
            for (int index = 0; index < observedNodeCount; index++) {
                @Nullable Node node = observedNodes[index];
                if (node != null) {
                    node.visibleProperty().removeListener(visibilityListener);
                    node.parentProperty().removeListener(parentListener);
                }
            }
            Arrays.fill(observedNodes, 0, observedNodeCount, null);
            observedNodeCount = 0;
        }

        /// Removes ancestor listeners and releases reusable node storage.
        private void releaseObservedChain() {
            removeObservedChain();
            observedNodes = EMPTY_NODES;
        }

        /// Removes this owner from its current scene and releases an empty scene dispatcher.
        private void unregisterSceneObserver() {
            @Nullable SceneObserver currentObserver = registeredSceneObserver;
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
            if (observers.isEmpty()) {
                return;
            }
            if (observers.isDispatching()) {
                refreshRequested = true;
                return;
            }

            @Nullable RuntimeException failure = null;
            do {
                refreshRequested = false;
                long dispatchRegistrationVersion = registrationVersion;
                int dispatchLimit = observers.beginDispatch();
                try {
                    for (int index = 0; index < dispatchLimit; index++) {
                        @Nullable M3MotionSettingsObserver observer = observers.get(index);
                        if (observer != null) {
                            try {
                                observer.refresh();
                            } catch (RuntimeException exception) {
                                if (failure == null) {
                                    failure = exception;
                                } else if (failure != exception) {
                                    failure.addSuppressed(exception);
                                }
                            }
                        }
                        if (registrationVersion != dispatchRegistrationVersion) {
                            break;
                        }
                    }
                } finally {
                    observers.endDispatch();
                }
            } while (refreshRequested && !observers.isEmpty());
            if (failure != null) {
                throw failure;
            }
        }
    }

    /// Dispatches global settings and window lifecycle notifications once per scene.
    @NotNullByDefault
    private static final class SceneObserver {
        /// The scene that owns this dispatcher.
        private final Scene scene;

        /// Active owners, with stable slots during callback dispatch.
        private final M3ObserverList<OwnerObserver> owners = new M3ObserverList<>();

        /// Receives global reduced-motion changes without retaining this scene from the static property.
        private final ChangeListener<Boolean> globalSettingsListener =
                (observable, oldValue, newValue) -> requestRefresh(null);

        /// Weak wrapper registered with the application-wide reduced-motion property.
        private final WeakChangeListener<Boolean> weakGlobalSettingsListener =
                new WeakChangeListener<>(globalSettingsListener);

        /// Receives changes to the window that presents the scene.
        private final ChangeListener<@Nullable Window> windowListener =
                (observable, oldWindow, newWindow) -> updateWindow(newWindow, true);

        /// Receives showing-state changes from the current scene window.
        private final InvalidationListener windowShowingListener = observable -> handleWindowShowingChanged();

        /// Receives iconification changes from the stage that presents the current scene window.
        private final InvalidationListener stageIconifiedListener = observable -> handleStageIconifiedChanged();

        /// The window whose showing state is currently observed.
        private @Nullable Window observedWindow;

        /// The stage whose iconification state controls the current window's presentation.
        private @Nullable Stage observedStage;

        /// Whether the global reduced-motion property currently contains this dispatcher's weak listener.
        private boolean globalSettingsObserved;

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
            boolean wasEmpty = owners.isEmpty();
            owners.add(ownerObserver);
            if (wasEmpty) {
                scene.windowProperty().addListener(windowListener);
                updateWindow(scene.getWindow(), false);
            }
        }

        /// Removes one owner coordinator and returns whether this dispatcher became empty.
        ///
        /// @param ownerObserver the owner coordinator to remove
        /// @return true when no owner coordinators remain
        private boolean remove(OwnerObserver ownerObserver) {
            if (!owners.remove(ownerObserver) || !owners.isEmpty()) {
                return false;
            }

            scene.windowProperty().removeListener(windowListener);
            updateWindow(null, false);
            return true;
        }

        /// Reattaches shared render-activity listeners to the scene's current window.
        ///
        /// @param window  the new window, or null when detached
        /// @param refresh whether subscribers should be refreshed
        private void updateWindow(@Nullable Window window, boolean refresh) {
            if (observedWindow == window) {
                updateGlobalSettingsObservation();
                if (refresh) {
                    requestRefresh(null);
                }
                return;
            }

            @Nullable Window currentWindow = observedWindow;
            if (currentWindow != null) {
                currentWindow.showingProperty().removeListener(windowShowingListener);
            }
            @Nullable Stage currentStage = observedStage;
            if (currentStage != null) {
                currentStage.iconifiedProperty().removeListener(stageIconifiedListener);
            }
            observedWindow = window;
            observedStage = M3PresentationActivity.presentationStage(window);
            if (window != null) {
                window.showingProperty().addListener(windowShowingListener);
            }
            @Nullable Stage stage = observedStage;
            if (stage != null) {
                stage.iconifiedProperty().addListener(stageIconifiedListener);
            }
            updateGlobalSettingsObservation();
            if (refresh) {
                requestRefresh(null);
            }
        }

        /// Updates global setting observation for the current presentation lifecycle.
        private void updateGlobalSettingsObservation() {
            boolean shouldObserve = !owners.isEmpty()
                    && (observedWindow == null
                    || observedWindow.isShowing() && (observedStage == null || !observedStage.isIconified()));
            if (globalSettingsObserved == shouldObserve) {
                return;
            }
            globalSettingsObserved = shouldObserve;
            if (shouldObserve) {
                M3MotionSettings.globalReducedMotionRequestedProperty().addListener(weakGlobalSettingsListener);
            } else {
                M3MotionSettings.globalReducedMotionRequestedProperty().removeListener(weakGlobalSettingsListener);
            }
        }

        /// Refreshes presentation state and global setting observation after a window shows or hides.
        private void handleWindowShowingChanged() {
            updateGlobalSettingsObservation();
            requestRefresh(null);
        }

        /// Refreshes presentation state and global setting observation after the presenting stage is iconified.
        private void handleStageIconifiedChanged() {
            updateGlobalSettingsObservation();
            requestRefresh(null);
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
            @Nullable RuntimeException failure = null;
            int dispatchLimit = owners.beginDispatch();
            try {
                for (int index = 0; index < dispatchLimit; index++) {
                    @Nullable OwnerObserver ownerObserver = owners.get(index);
                    if (ownerObserver != null && (source == null || contains(source, ownerObserver.owner))) {
                        try {
                            ownerObserver.refreshFrom(this);
                        } catch (RuntimeException exception) {
                            if (failure == null) {
                                failure = exception;
                            } else if (failure != exception) {
                                failure.addSuppressed(exception);
                            }
                        }
                    }
                }
            } finally {
                owners.endDispatch();
            }
            if (failure != null) {
                throw failure;
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
    }
}
