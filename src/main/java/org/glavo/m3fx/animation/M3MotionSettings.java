// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Runtime settings for Material Design 3 motion in M3FX controls.
///
/// The global setting is used when no node-specific override is present. Node overrides inherit through the JavaFX
/// parent chain, so an application can disable motion for a whole subtree or re-enable it for one control.
@NotNullByDefault
public final class M3MotionSettings {
    /// The key used to store nullable node-local animation overrides.
    private static final Object ANIMATIONS_ENABLED_KEY = new Object();

    /// The global animation switch used when a node has no inherited override.
    private static final BooleanProperty animationsEnabled =
            new SimpleBooleanProperty(M3MotionSettings.class, "animationsEnabled", true);

    /// Prevents instantiation.
    private M3MotionSettings() {
    }

    /// Returns whether animations are globally enabled.
    public static boolean areAnimationsEnabled() {
        return animationsEnabled.get();
    }

    /// Sets whether animations are globally enabled.
    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled.set(enabled);
    }

    /// Returns the global animation switch property.
    public static BooleanProperty animationsEnabledProperty() {
        return animationsEnabled;
    }

    /// Returns whether animations are enabled for a node after resolving inherited overrides.
    public static boolean areAnimationsEnabled(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Boolean override = findInheritedAnimationsEnabled(node);
        return override == null ? areAnimationsEnabled() : override;
    }

    /// Returns the node-local animation override, or `null` when the node inherits its setting.
    public static @Nullable Boolean getAnimationsEnabled(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Object value = node.getProperties().get(ANIMATIONS_ENABLED_KEY);
        return value instanceof Boolean booleanValue ? booleanValue : null;
    }

    /// Sets the node-local animation override, or clears it when `null` is supplied.
    public static void setAnimationsEnabled(Node node, @Nullable Boolean enabled) {
        Objects.requireNonNull(node, "node");
        if (enabled == null) {
            node.getProperties().remove(ANIMATIONS_ENABLED_KEY);
        } else {
            node.getProperties().put(ANIMATIONS_ENABLED_KEY, enabled);
        }
    }

    /// Clears the node-local animation override so the node inherits its setting.
    public static void clearAnimationsEnabled(Node node) {
        setAnimationsEnabled(node, null);
    }

    /// Finds the nearest inherited node animation override.
    private static @Nullable Boolean findInheritedAnimationsEnabled(Node node) {
        @Nullable Node current = node;
        while (current != null) {
            @Nullable Boolean value = getAnimationsEnabled(current);
            if (value != null) {
                return value;
            }
            current = current.getParent();
        }
        return null;
    }
}
