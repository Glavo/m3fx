// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Observes runtime M3FX motion setting changes while an owner node is attached to a scene.
///
/// `M3MotionSettingsObserver` is intended for controls that own finite animations directly. It registers a
/// global motion-settings listener only while the owner participates in a scene graph, and calls a supplied
/// refresh action immediately after attachment and after every global or node-local motion setting change.
@NotNullByDefault
public final class M3MotionSettingsObserver {
    /// The node whose scene attachment controls listener lifetime.
    private final Node owner;

    /// The action invoked when motion settings may affect the owner.
    private final Runnable refreshAction;

    /// The listener registered with [M3MotionSettings] while the owner has a scene.
    private final InvalidationListener settingsListener;

    /// The listener that updates global listener registration when the owner moves between scenes.
    private final InvalidationListener sceneListener;

    /// Whether [settingsListener] is currently registered with [M3MotionSettings].
    private boolean settingsListenerRegistered;

    /// Creates an observer for one owner node.
    ///
    /// @param owner the node whose scene attachment controls listener lifetime
    /// @param refreshAction the action invoked when motion settings may affect the owner
    public M3MotionSettingsObserver(Node owner, Runnable refreshAction) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.refreshAction = Objects.requireNonNull(refreshAction, "refreshAction");
        this.settingsListener = observable -> this.refreshAction.run();
        this.sceneListener = observable -> updateRegistration();
        owner.sceneProperty().addListener(sceneListener);
        updateRegistration();
    }

    /// Refreshes the owner immediately.
    private void refresh() {
        refreshAction.run();
    }

    /// Updates whether the observer is registered for global motion setting changes.
    private void updateRegistration() {
        if (owner.getScene() == null) {
            if (settingsListenerRegistered) {
                M3MotionSettings.removeSettingsChangeListener(settingsListener);
                settingsListenerRegistered = false;
            }
            return;
        }

        if (!settingsListenerRegistered) {
            M3MotionSettings.addSettingsChangeListener(settingsListener);
            settingsListenerRegistered = true;
        }
        refresh();
    }
}
