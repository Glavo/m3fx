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

    /// Opaque node property key for notifiers registered under one physical focus subtree.
    private static final Object NODE_NOTIFIERS_KEY = new Object();

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
        try {
            updateScene(sceneOwner.getScene());
        } catch (RuntimeException | Error exception) {
            started = false;
            sceneOwner.sceneProperty().removeListener(sceneListener);
            unregisterSceneDispatcher();
            lastFocusNode = null;
            throw exception;
        }
    }

    /// Stops listening and clears cached focus state.
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        sceneOwner.sceneProperty().removeListener(sceneListener);
        unregisterSceneDispatcher();
        lastFocusNode = null;
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

    /// Dispatches scene focus-owner transitions by visiting only affected ancestor paths.
    @NotNullByDefault
    private static final class SceneFocusDispatcher {
        /// The scene that owns this dispatcher.
        private final Scene scene;

        /// The number of notifiers currently registered in node properties for this scene.
        private int notifierCount;

        /// Observes the scene's focus owner once for all registered notifiers.
        private final ChangeListener<@Nullable Node> focusOwnerListener =
                (observable, oldFocusOwner, newFocusOwner) -> dispatch(oldFocusOwner, newFocusOwner);

        /// Creates a dispatcher owned by one scene.
        private SceneFocusDispatcher(Scene scene) {
            this.scene = scene;
        }

        /// Adds a notifier and installs the scene listener when the first notifier arrives.
        private void add(M3AccessibleFocusNotifier notifier) {
            Node notifierOwner = notifier.sceneOwner;
            Object value = notifierOwner.hasProperties()
                    ? notifierOwner.getProperties().get(NODE_NOTIFIERS_KEY)
                    : null;
            if (value instanceof M3AccessibleFocusNotifier current) {
                if (current == notifier) {
                    return;
                }
                notifierOwner.getProperties().put(
                        NODE_NOTIFIERS_KEY,
                        new M3AccessibleFocusNotifier[]{current, notifier}
                );
            } else if (value instanceof M3AccessibleFocusNotifier[] currentNotifiers) {
                for (M3AccessibleFocusNotifier current : currentNotifiers) {
                    if (current == notifier) {
                        return;
                    }
                }
                M3AccessibleFocusNotifier[] expanded = Arrays.copyOf(
                        currentNotifiers,
                        currentNotifiers.length + 1
                );
                expanded[currentNotifiers.length] = notifier;
                notifierOwner.getProperties().put(NODE_NOTIFIERS_KEY, expanded);
            } else if (value == null) {
                notifierOwner.getProperties().put(NODE_NOTIFIERS_KEY, notifier);
            } else {
                throw new IllegalStateException("Unexpected accessible focus notifier node property");
            }

            if (notifierCount++ == 0) {
                scene.focusOwnerProperty().addListener(focusOwnerListener);
            }
        }

        /// Removes a notifier and returns whether the dispatcher became empty.
        private boolean remove(M3AccessibleFocusNotifier notifier) {
            Node notifierOwner = notifier.sceneOwner;
            if (!notifierOwner.hasProperties()) {
                return false;
            }

            Object value = notifierOwner.getProperties().get(NODE_NOTIFIERS_KEY);
            if (value == notifier) {
                notifierOwner.getProperties().remove(NODE_NOTIFIERS_KEY);
            } else if (value instanceof M3AccessibleFocusNotifier[] currentNotifiers) {
                int index = -1;
                for (int i = 0; i < currentNotifiers.length; i++) {
                    if (currentNotifiers[i] == notifier) {
                        index = i;
                        break;
                    }
                }
                if (index < 0) {
                    return false;
                }

                if (currentNotifiers.length == 2) {
                    notifierOwner.getProperties().put(
                            NODE_NOTIFIERS_KEY,
                            currentNotifiers[index == 0 ? 1 : 0]
                    );
                } else {
                    M3AccessibleFocusNotifier[] reduced =
                            new M3AccessibleFocusNotifier[currentNotifiers.length - 1];
                    System.arraycopy(currentNotifiers, 0, reduced, 0, index);
                    System.arraycopy(
                            currentNotifiers,
                            index + 1,
                            reduced,
                            index,
                            reduced.length - index
                    );
                    notifierOwner.getProperties().put(NODE_NOTIFIERS_KEY, reduced);
                }
            } else {
                return false;
            }

            notifierCount--;
            if (notifierCount == 0) {
                scene.focusOwnerProperty().removeListener(focusOwnerListener);
                return true;
            }
            return false;
        }

        /// Dispatches one focus transition only through affected physical ancestor paths.
        private void dispatch(@Nullable Node oldFocusOwner, @Nullable Node newFocusOwner) {
            @Nullable Node commonAncestor = lowestCommonAncestor(oldFocusOwner, newFocusOwner);
            dispatchPath(oldFocusOwner, commonAncestor);
            dispatchPath(newFocusOwner, commonAncestor);
            dispatchPath(commonAncestor, null);
        }

        /// Dispatches notifiers registered from one node up to, but excluding, the supplied ancestor.
        private static void dispatchPath(@Nullable Node start, @Nullable Node endExclusive) {
            @Nullable Node current = start;
            while (current != endExclusive && current != null) {
                @Nullable Node parent = current.getParent();
                dispatchNode(current);
                current = parent;
            }
        }

        /// Dispatches every notifier registered directly on one physical subtree root.
        private static void dispatchNode(Node node) {
            if (!node.hasProperties()) {
                return;
            }

            Object value = node.getProperties().get(NODE_NOTIFIERS_KEY);
            if (value instanceof M3AccessibleFocusNotifier notifier) {
                notifier.notifyIfFocusNodeChanged();
            } else if (value instanceof M3AccessibleFocusNotifier[] notifiers) {
                for (M3AccessibleFocusNotifier notifier : notifiers) {
                    notifier.notifyIfFocusNodeChanged();
                }
            }
        }

        /// Returns the lowest physical ancestor shared by two nodes without allocating path collections.
        private static @Nullable Node lowestCommonAncestor(@Nullable Node first, @Nullable Node second) {
            int firstDepth = depth(first);
            int secondDepth = depth(second);
            while (firstDepth > secondDepth && first != null) {
                first = first.getParent();
                firstDepth--;
            }
            while (secondDepth > firstDepth && second != null) {
                second = second.getParent();
                secondDepth--;
            }
            while (first != second) {
                first = first == null ? null : first.getParent();
                second = second == null ? null : second.getParent();
            }
            return first;
        }

        /// Returns the number of nodes in one physical parent path.
        private static int depth(@Nullable Node node) {
            int depth = 0;
            @Nullable Node current = node;
            while (current != null) {
                depth++;
                current = current.getParent();
            }
            return depth;
        }
    }
}