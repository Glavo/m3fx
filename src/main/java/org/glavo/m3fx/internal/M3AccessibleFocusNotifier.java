// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ChangeListener;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

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
    /// The node whose scene focus owner should be observed.
    private final Node sceneOwner;

    /// Supplies the accessibility focus node currently exposed by the owner.
    private final Supplier<@Nullable Node> focusNodeSupplier;

    /// Runs when the focused accessibility child changes.
    private final Runnable focusNodeChangedNotifier;

    /// Observes scene-owner changes so the focus owner listener follows the attached scene.
    private final ChangeListener<@Nullable Scene> sceneListener =
            (observable, oldScene, newScene) -> updateScene(newScene);

    /// Observes scene focus owner changes.
    private final ChangeListener<@Nullable Node> focusOwnerListener =
            (observable, oldFocusOwner, newFocusOwner) -> notifyIfFocusNodeChanged();

    /// The scene that currently has `focusOwnerListener` installed.
    private @Nullable Scene scene;

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
        if (scene == newScene) {
            refresh();
            return;
        }
        if (scene != null) {
            scene.focusOwnerProperty().removeListener(focusOwnerListener);
        }
        scene = newScene;
        if (scene != null) {
            scene.focusOwnerProperty().addListener(focusOwnerListener);
        }
        refresh();
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
}
