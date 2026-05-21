// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    /// The key used to store nullable node-local motion scheme overrides.
    private static final Object MOTION_SCHEME_KEY = new Object();

    /// The key used to store nullable node-local motion behavior overrides.
    private static final Object MOTION_BEHAVIOR_KEY = new Object();

    /// The global animation switch used when a node has no inherited override.
    private static final BooleanProperty animationsEnabled =
            new SimpleBooleanProperty(M3MotionSettings.class, "animationsEnabled", true);

    /// The global motion scheme used when a node has no inherited override and no installed theme.
    private static final ObjectProperty<M3MotionScheme> motionScheme =
            new SimpleObjectProperty<>(M3MotionSettings.class, "motionScheme", M3MotionScheme.standard());

    /// The global motion behavior used when a node has no inherited override and no installed theme.
    private static final ObjectProperty<M3MotionBehavior> motionBehavior =
            new SimpleObjectProperty<>(M3MotionSettings.class, "motionBehavior", M3MotionBehavior.standard());

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

    /// Returns the global motion scheme.
    public static M3MotionScheme getMotionScheme() {
        return motionScheme.get();
    }

    /// Sets the global motion scheme.
    public static void setMotionScheme(M3MotionScheme scheme) {
        motionScheme.set(Objects.requireNonNull(scheme, "scheme"));
    }

    /// Returns the global motion scheme property.
    public static ObjectProperty<M3MotionScheme> motionSchemeProperty() {
        return motionScheme;
    }

    /// Returns the global motion behavior.
    public static M3MotionBehavior getMotionBehavior() {
        return motionBehavior.get();
    }

    /// Sets the global motion behavior.
    public static void setMotionBehavior(M3MotionBehavior behavior) {
        motionBehavior.set(Objects.requireNonNull(behavior, "behavior"));
    }

    /// Returns the global motion behavior property.
    public static ObjectProperty<M3MotionBehavior> motionBehaviorProperty() {
        return motionBehavior;
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

    /// Returns the node-local motion scheme override, or `null` when the node inherits its setting.
    public static @Nullable M3MotionScheme getMotionScheme(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Object value = node.getProperties().get(MOTION_SCHEME_KEY);
        return value instanceof M3MotionScheme scheme ? scheme : null;
    }

    /// Sets the node-local motion scheme override, or clears it when `null` is supplied.
    public static void setMotionScheme(Node node, @Nullable M3MotionScheme scheme) {
        Objects.requireNonNull(node, "node");
        if (scheme == null) {
            node.getProperties().remove(MOTION_SCHEME_KEY);
        } else {
            node.getProperties().put(MOTION_SCHEME_KEY, scheme);
        }
    }

    /// Clears the node-local motion scheme override so the node inherits its setting.
    public static void clearMotionScheme(Node node) {
        setMotionScheme(node, null);
    }

    /// Returns the node-local motion behavior override, or `null` when the node inherits its setting.
    public static @Nullable M3MotionBehavior getMotionBehavior(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Object value = node.getProperties().get(MOTION_BEHAVIOR_KEY);
        return value instanceof M3MotionBehavior behavior ? behavior : null;
    }

    /// Sets the node-local motion behavior override, or clears it when `null` is supplied.
    public static void setMotionBehavior(Node node, @Nullable M3MotionBehavior behavior) {
        Objects.requireNonNull(node, "node");
        if (behavior == null) {
            node.getProperties().remove(MOTION_BEHAVIOR_KEY);
        } else {
            node.getProperties().put(MOTION_BEHAVIOR_KEY, behavior);
        }
    }

    /// Clears the node-local motion behavior override so the node inherits its setting.
    public static void clearMotionBehavior(Node node) {
        setMotionBehavior(node, null);
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
