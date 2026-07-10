// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ChangeListener;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/// Tracks scene focus owner changes and reports changed accessibility focus nodes.
///
/// `M3AccessibleFocusNotifier` is used by composite controls whose public `FOCUS_NODE` query can point at
/// whichever child currently owns focus. JavaFX accessibility clients only learn about that change when the
/// owner control calls [Node#notifyAccessibleAttributeChanged(AccessibleAttribute)], so this helper centralizes
/// the scene listener and change detection needed by app bars, groups, sheets, popup owners, and other slot
/// containers.
@NotNullByDefault
public final class M3AccessibleFocusNotifier {
    /// Opaque scene property key for the shared focus-owner dispatcher.
    private static final Object SCENE_DISPATCHER_KEY = new Object();

    /// Empty notifier array shared by scene dispatchers with no observers.
    private static final M3AccessibleFocusNotifier[] EMPTY_NOTIFIERS = new M3AccessibleFocusNotifier[0];

    /// The node whose scene focus owner should be observed.
    private final Node sceneOwner;

    /// Supplies the accessibility focus node currently exposed by the owner.
    private final Supplier<@Nullable Node> focusNodeSupplier;

    /// Runs when the focused accessibility child changes.
    private final Runnable focusNodeChangedNotifier;

    /// Observes scene-owner changes so the focus owner listener follows the attached scene.
    private final ChangeListener<@Nullable Scene> sceneListener =
            (observable, oldScene, newScene) -> updateScene(newScene);

    /// The scene dispatcher currently responsible for this notifier.
    private @Nullable SceneFocusDispatcher sceneDispatcher;

    /// The previously reported current focus node.
    private @Nullable Node lastFocusNode;

    /// Whether this notifier is installed on the owner.
    private boolean started;

    /// Creates a notifier for one owner node.
    ///
    /// @param owner the node that owns and observes the `FOCUS_NODE` attribute
    /// @param focusNodeSupplier the supplier that returns the current accessible focus node
    public M3AccessibleFocusNotifier(Node owner, Supplier<@Nullable Node> focusNodeSupplier) {
        this(owner, owner, focusNodeSupplier);
    }

    /// Creates a notifier whose owner and observed scene node differ.
    ///
    /// This overload is intended for popup-backed controls whose public owner remains in the application scene
    /// while popup content receives focus in a separate popup scene.
    ///
    /// @param notificationOwner the node that owns the `FOCUS_NODE` attribute
    /// @param sceneOwner the node whose scene focus owner is observed
    /// @param focusNodeSupplier the supplier that returns the current accessible focus node
    public M3AccessibleFocusNotifier(
            Node notificationOwner,
            Node sceneOwner,
            Supplier<@Nullable Node> focusNodeSupplier
    ) {
        this(
                notificationOwner,
                sceneOwner,
                focusNodeSupplier,
                () -> notificationOwner.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE)
        );
    }

    /// Creates a notifier for one owner node with a custom notification callback.
    public M3AccessibleFocusNotifier(
            Node owner,
            Supplier<@Nullable Node> focusNodeSupplier,
            Runnable focusNodeChangedNotifier
    ) {
        this(owner, owner, focusNodeSupplier, focusNodeChangedNotifier);
    }

    /// Creates a notifier with a custom notification callback and observed scene node.
    public M3AccessibleFocusNotifier(
            Node notificationOwner,
            Node sceneOwner,
            Supplier<@Nullable Node> focusNodeSupplier,
            Runnable focusNodeChangedNotifier
    ) {
        Objects.requireNonNull(notificationOwner, "notificationOwner");
        this.sceneOwner = Objects.requireNonNull(sceneOwner, "sceneOwner");
        this.focusNodeSupplier = Objects.requireNonNull(focusNodeSupplier, "focusNodeSupplier");
        this.focusNodeChangedNotifier = Objects.requireNonNull(focusNodeChangedNotifier, "focusNodeChangedNotifier");
    }

    /// Starts listening to the observed node's current and future scenes.
    public void start() {
        if (started) {
            return;
        }
        started = true;
        sceneOwner.sceneProperty().addListener(sceneListener);
        updateScene(sceneOwner.getScene());
    }

    /// Stops listening and clears cached focus state.
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        sceneOwner.sceneProperty().removeListener(sceneListener);
        updateScene(null);
    }

    /// Refreshes cached focus state after child content changes already notified accessibility clients.
    public void refresh() {
        lastFocusNode = focusNodeSupplier.get();
    }

    /// Reattaches the focus owner listener to a new scene.
    private void updateScene(@Nullable Scene newScene) {
        SceneFocusDispatcher currentDispatcher = sceneDispatcher;
        if (currentDispatcher != null && currentDispatcher.scene == newScene) {
            refresh();
            return;
        }

        unregisterSceneDispatcher();
        if (newScene != null) {
            SceneFocusDispatcher nextDispatcher = sceneDispatcher(newScene);
            nextDispatcher.add(this);
            sceneDispatcher = nextDispatcher;
        }
        refresh();
    }

    /// Removes this notifier from its current dispatcher and tears down an empty dispatcher.
    private void unregisterSceneDispatcher() {
        SceneFocusDispatcher currentDispatcher = sceneDispatcher;
        if (currentDispatcher == null) {
            return;
        }

        sceneDispatcher = null;
        if (currentDispatcher.remove(this)
                && currentDispatcher.scene.getProperties().get(SCENE_DISPATCHER_KEY) == currentDispatcher) {
            currentDispatcher.scene.getProperties().remove(SCENE_DISPATCHER_KEY);
        }
    }

    /// Notifies the owner when the focused accessibility child changes.
    private void notifyIfFocusNodeChanged() {
        @Nullable Node focusNode = focusNodeSupplier.get();
        if (focusNode == lastFocusNode) {
            return;
        }
        lastFocusNode = focusNode;
        focusNodeChangedNotifier.run();
    }

    /// Returns whether a focus-owner transition can change this notifier's reported focus node.
    private boolean observesFocusTransition(@Nullable Node oldFocusOwner, @Nullable Node newFocusOwner) {
        return containsPhysicalNode(sceneOwner, oldFocusOwner) || containsPhysicalNode(sceneOwner, newFocusOwner);
    }

    /// Returns whether one node is the supplied root or a physical descendant of it.
    private static boolean containsPhysicalNode(Node root, @Nullable Node node) {
        @Nullable Node current = node;
        while (current != null) {
            if (current == root) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /// Returns the dispatcher owned by a scene, creating it when needed.
    ///
    /// @param scene the scene that owns the dispatcher
    /// @return the existing or created dispatcher
    private static SceneFocusDispatcher sceneDispatcher(Scene scene) {
        Object value = scene.getProperties().get(SCENE_DISPATCHER_KEY);
        if (value instanceof SceneFocusDispatcher dispatcher) {
            return dispatcher;
        }

        SceneFocusDispatcher dispatcher = new SceneFocusDispatcher(scene);
        scene.getProperties().put(SCENE_DISPATCHER_KEY, dispatcher);
        return dispatcher;
    }

    /// Dispatches scene focus-owner transitions without allocating on the focus hot path.
    @NotNullByDefault
    private static final class SceneFocusDispatcher {
        /// The scene that owns this dispatcher.
        private final Scene scene;

        /// The immutable-by-replacement notifier array used by focus dispatch.
        private M3AccessibleFocusNotifier[] notifiers = EMPTY_NOTIFIERS;

        /// Observes the scene's focus owner once for all registered notifiers.
        private final ChangeListener<@Nullable Node> focusOwnerListener =
                (observable, oldFocusOwner, newFocusOwner) -> dispatch(oldFocusOwner, newFocusOwner);

        /// Creates a dispatcher owned by one scene.
        private SceneFocusDispatcher(Scene scene) {
            this.scene = scene;
        }

        /// Adds a notifier and installs the scene listener when the first notifier arrives.
        private void add(M3AccessibleFocusNotifier notifier) {
            for (M3AccessibleFocusNotifier current : notifiers) {
                if (current == notifier) {
                    return;
                }
            }

            boolean wasEmpty = notifiers.length == 0;
            M3AccessibleFocusNotifier[] expanded = Arrays.copyOf(notifiers, notifiers.length + 1);
            expanded[notifiers.length] = notifier;
            notifiers = expanded;
            if (wasEmpty) {
                scene.focusOwnerProperty().addListener(focusOwnerListener);
            }
        }

        /// Removes a notifier and returns whether the dispatcher became empty.
        private boolean remove(M3AccessibleFocusNotifier notifier) {
            int index = -1;
            for (int i = 0; i < notifiers.length; i++) {
                if (notifiers[i] == notifier) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                return false;
            }

            M3AccessibleFocusNotifier[] reduced = new M3AccessibleFocusNotifier[notifiers.length - 1];
            System.arraycopy(notifiers, 0, reduced, 0, index);
            System.arraycopy(notifiers, index + 1, reduced, index, reduced.length - index);
            notifiers = reduced;
            if (reduced.length == 0) {
                scene.focusOwnerProperty().removeListener(focusOwnerListener);
                return true;
            }
            return false;
        }

        /// Dispatches one focus transition through a stable array snapshot.
        private void dispatch(@Nullable Node oldFocusOwner, @Nullable Node newFocusOwner) {
            M3AccessibleFocusNotifier[] snapshot = notifiers;
            for (M3AccessibleFocusNotifier notifier : snapshot) {
                if (notifier.observesFocusTransition(oldFocusOwner, newFocusOwner)) {
                    notifier.notifyIfFocusNodeChanged();
                }
            }
        }
    }
}
