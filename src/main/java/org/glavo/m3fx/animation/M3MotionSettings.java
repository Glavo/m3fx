// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/// Runtime settings for Material Design 3 motion in M3FX controls.
///
/// The global settings are used when no node-specific override is present. Node overrides inherit through the
/// JavaFX parent chain, so an application can disable motion for a whole subtree, re-enable it for one control,
/// or replace the motion scheme used by one feature area without rewriting control skins.
///
/// Controls use these settings for state-layer fades, ripple release, popup entrance and exit, smooth scrolling,
/// and progress motion. Disabling animations requests reduced motion: finite transitions settle immediately,
/// while indeterminate activity indicators keep simple linear movement so loading and progress states remain
/// visibly active without playing the full Material motion treatment. The defaults follow the Material Design 3
/// motion guidance; see [Material motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3MotionSettings {
    /// The key used to store nullable node-local animation overrides.
    private static final Object ANIMATIONS_ENABLED_KEY = new Object();

    /// The key used to store nullable node-local motion scheme overrides.
    private static final Object MOTION_SCHEME_KEY = new Object();

    /// The key used to store nullable node-local motion behavior overrides.
    private static final Object MOTION_BEHAVIOR_KEY = new Object();

    // The global animation switch used when a node has no inherited override.
    private static final BooleanProperty animationsEnabled =
            new SimpleBooleanProperty(M3MotionSettings.class, "animationsEnabled", true);

    // The global motion scheme used when a node has no inherited override and no installed theme.
    private static final ObjectProperty<M3MotionScheme> motionScheme =
            new SimpleObjectProperty<>(M3MotionSettings.class, "motionScheme", M3MotionScheme.standard());

    // The global motion behavior used when a node has no inherited override and no installed theme.
    private static final ObjectProperty<M3MotionBehavior> motionBehavior =
            new SimpleObjectProperty<>(M3MotionSettings.class, "motionBehavior", M3MotionBehavior.standard());

    /// The revision incremented whenever any global or node-local motion setting changes.
    private static final ReadOnlyLongWrapper settingsRevision =
            new ReadOnlyLongWrapper(M3MotionSettings.class, "settingsRevision");

    /// The read-only view of [settingsRevision].
    private static final ReadOnlyLongProperty readOnlySettingsRevision = settingsRevision.getReadOnlyProperty();

    /// Listeners notified whenever global or node-local motion settings change.
    private static final CopyOnWriteArrayList<InvalidationListener> settingsChangeListeners =
            new CopyOnWriteArrayList<>();

    static {
        animationsEnabled.addListener(observable -> markSettingsChanged());
        motionScheme.addListener(observable -> markSettingsChanged());
        motionBehavior.addListener(observable -> markSettingsChanged());
    }

    /// Prevents instantiation.
    private M3MotionSettings() {
    }

    /// Returns whether full Material motion is globally enabled.
    ///
    /// @return `true` when global full-motion animations are enabled
    public static boolean areAnimationsEnabled() {
        return animationsEnabled.get();
    }

    /// Sets whether full Material motion is globally enabled.
    ///
    /// @param enabled whether global full-motion animations should be enabled
    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled.set(enabled);
    }

    /// Returns the global full-motion animation switch property.
    ///
    /// @return the writable global full-motion animation switch property
    public static BooleanProperty animationsEnabledProperty() {
        return animationsEnabled;
    }

    /// Returns the global motion scheme.
    ///
    /// @return the global motion scheme
    public static M3MotionScheme getMotionScheme() {
        return motionScheme.get();
    }

    /// Sets the global motion scheme.
    ///
    /// @param scheme the global motion scheme
    public static void setMotionScheme(M3MotionScheme scheme) {
        motionScheme.set(Objects.requireNonNull(scheme, "scheme"));
    }

    /// Returns the global motion scheme property.
    ///
    /// @return the writable global motion scheme property
    public static ObjectProperty<M3MotionScheme> motionSchemeProperty() {
        return motionScheme;
    }

    /// Returns the global motion behavior.
    ///
    /// @return the global motion behavior
    public static M3MotionBehavior getMotionBehavior() {
        return motionBehavior.get();
    }

    /// Sets the global motion behavior.
    ///
    /// @param behavior the global motion behavior
    public static void setMotionBehavior(M3MotionBehavior behavior) {
        motionBehavior.set(Objects.requireNonNull(behavior, "behavior"));
    }

    /// Returns the global motion behavior property.
    ///
    /// @return the writable global motion behavior property
    public static ObjectProperty<M3MotionBehavior> motionBehaviorProperty() {
        return motionBehavior;
    }

    /// Returns a read-only revision property that changes when any global or node-local motion setting changes.
    ///
    /// @return the read-only settings revision property
    public static ReadOnlyLongProperty revisionProperty() {
        return readOnlySettingsRevision;
    }

    /// Adds a listener that is called whenever global or node-local motion settings change.
    ///
    /// The listener is notified synchronously after [revisionProperty] increments. Unlike a JavaFX
    /// invalidation listener attached directly to the property, this listener is called for every settings change
    /// even when the property value has not been read between changes.
    ///
    /// @param listener the listener to add
    public static void addSettingsChangeListener(InvalidationListener listener) {
        settingsChangeListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /// Removes a listener previously added with [addSettingsChangeListener].
    ///
    /// @param listener the listener to remove
    public static void removeSettingsChangeListener(InvalidationListener listener) {
        settingsChangeListeners.remove(Objects.requireNonNull(listener, "listener"));
    }

    /// Returns whether full Material motion is enabled for a node after resolving inherited overrides.
    ///
    /// @param node the node used to resolve inherited motion settings
    /// @return `true` when full-motion animations are enabled for the node
    public static boolean areAnimationsEnabled(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Boolean override = findInheritedAnimationsEnabled(node);
        return override == null ? areAnimationsEnabled() : override;
    }

    /// Returns the node-local full-motion animation override, or `null` when the node inherits its setting.
    ///
    /// @param node the node to query
    /// @return the node-local full-motion animation override, or `null` when the node inherits its setting
    public static @Nullable Boolean getAnimationsEnabled(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Object value = node.getProperties().get(ANIMATIONS_ENABLED_KEY);
        return value instanceof Boolean booleanValue ? booleanValue : null;
    }

    /// Sets the node-local full-motion animation override, or clears it when `null` is supplied.
    ///
    /// @param node the node to update
    /// @param enabled the node-local full-motion animation override, or `null` to inherit
    public static void setAnimationsEnabled(Node node, @Nullable Boolean enabled) {
        Objects.requireNonNull(node, "node");
        @Nullable Boolean previous = getAnimationsEnabled(node);
        if (Objects.equals(previous, enabled)) {
            return;
        }
        if (enabled == null) {
            node.getProperties().remove(ANIMATIONS_ENABLED_KEY);
        } else {
            node.getProperties().put(ANIMATIONS_ENABLED_KEY, enabled);
        }
        markSettingsChanged();
    }

    /// Clears the node-local full-motion animation override so the node inherits its setting.
    ///
    /// @param node the node whose local full-motion animation override should be cleared
    public static void clearAnimationsEnabled(Node node) {
        setAnimationsEnabled(node, null);
    }

    /// Returns the node-local motion scheme override, or `null` when the node inherits its setting.
    ///
    /// @param node the node to query
    /// @return the node-local motion scheme override, or `null` when the node inherits its setting
    public static @Nullable M3MotionScheme getMotionScheme(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Object value = node.getProperties().get(MOTION_SCHEME_KEY);
        return value instanceof M3MotionScheme scheme ? scheme : null;
    }

    /// Sets the node-local motion scheme override, or clears it when `null` is supplied.
    ///
    /// @param node the node to update
    /// @param scheme the node-local motion scheme override, or `null` to inherit
    public static void setMotionScheme(Node node, @Nullable M3MotionScheme scheme) {
        Objects.requireNonNull(node, "node");
        @Nullable M3MotionScheme previous = getMotionScheme(node);
        if (Objects.equals(previous, scheme)) {
            return;
        }
        if (scheme == null) {
            node.getProperties().remove(MOTION_SCHEME_KEY);
        } else {
            node.getProperties().put(MOTION_SCHEME_KEY, scheme);
        }
        markSettingsChanged();
    }

    /// Clears the node-local motion scheme override so the node inherits its setting.
    ///
    /// @param node the node whose local motion scheme override should be cleared
    public static void clearMotionScheme(Node node) {
        setMotionScheme(node, null);
    }

    /// Returns the node-local motion behavior override, or `null` when the node inherits its setting.
    ///
    /// @param node the node to query
    /// @return the node-local motion behavior override, or `null` when the node inherits its setting
    public static @Nullable M3MotionBehavior getMotionBehavior(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Object value = node.getProperties().get(MOTION_BEHAVIOR_KEY);
        return value instanceof M3MotionBehavior behavior ? behavior : null;
    }

    /// Sets the node-local motion behavior override, or clears it when `null` is supplied.
    ///
    /// @param node the node to update
    /// @param behavior the node-local motion behavior override, or `null` to inherit
    public static void setMotionBehavior(Node node, @Nullable M3MotionBehavior behavior) {
        Objects.requireNonNull(node, "node");
        @Nullable M3MotionBehavior previous = getMotionBehavior(node);
        if (Objects.equals(previous, behavior)) {
            return;
        }
        if (behavior == null) {
            node.getProperties().remove(MOTION_BEHAVIOR_KEY);
        } else {
            node.getProperties().put(MOTION_BEHAVIOR_KEY, behavior);
        }
        markSettingsChanged();
    }

    /// Clears the node-local motion behavior override so the node inherits its setting.
    ///
    /// @param node the node whose local motion behavior override should be cleared
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

    /// Increments the global settings revision.
    private static void markSettingsChanged() {
        settingsRevision.set(settingsRevision.get() + 1L);
        for (InvalidationListener listener : settingsChangeListeners) {
            listener.invalidated(readOnlySettingsRevision);
        }
    }
}
