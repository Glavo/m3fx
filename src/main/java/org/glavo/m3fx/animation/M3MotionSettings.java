// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Runtime settings for Material Design 3 motion in M3FX controls.
///
/// Global settings provide the fallback values for the scene graph. A node may request reduced motion for its
/// complete subtree; full motion resumes only after that request is cleared and no ancestor or global setting still
/// requests reduced motion. Motion-scheme and behavior overrides use the nearest value in the parent chain because
/// they select tokens rather than weaken an accessibility preference.
///
/// Controls use these settings for state-layer fades, ripple release, popup entrance and exit, smooth scrolling,
/// and progress motion. Disabling animations requests reduced motion: finite transitions settle immediately,
/// while indeterminate activity indicators keep simple linear movement so loading and progress states remain
/// visibly active without playing the full Material motion treatment. The defaults follow the Material Design 3
/// motion guidance; see [Material motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3MotionSettings {
    /// The key used to store node-local reduced-motion requests.
    private static final Object ANIMATIONS_ENABLED_KEY = new Object();

    /// The key used to store nullable node-local motion scheme overrides.
    private static final Object MOTION_SCHEME_KEY = new Object();

    /// The key used to store nullable node-local motion behavior overrides.
    private static final Object MOTION_BEHAVIOR_KEY = new Object();

    /// The global full-motion switch.
    private static final BooleanProperty globalAnimationsEnabled =
            new SimpleBooleanProperty(M3MotionSettings.class, "animationsEnabled", true);

    /// The global motion scheme used when a node has no inherited override and no installed theme.
    private static final ObjectProperty<M3MotionScheme> globalMotionScheme =
            new SimpleObjectProperty<>(M3MotionSettings.class, "motionScheme", M3MotionScheme.standard());

    /// The global motion behavior used when a node has no inherited override and no installed theme.
    private static final ObjectProperty<M3MotionBehavior> globalMotionBehavior =
            new SimpleObjectProperty<>(M3MotionSettings.class, "motionBehavior", M3MotionBehavior.standard());

    /// The revision incremented whenever any global or node-local motion setting changes.
    private static final ReadOnlyLongWrapper settingsRevision =
            new ReadOnlyLongWrapper(M3MotionSettings.class, "settingsRevision");

    /// The read-only view of [settingsRevision].
    private static final ReadOnlyLongProperty readOnlySettingsRevision = settingsRevision.getReadOnlyProperty();

    static {
        globalAnimationsEnabled.addListener((observable, oldValue, newValue) -> markSettingsChanged(null));
        globalMotionScheme.addListener((observable, oldValue, newValue) -> markSettingsChanged(null));
        globalMotionBehavior.addListener((observable, oldValue, newValue) -> markSettingsChanged(null));
    }

    /// Prevents instantiation.
    private M3MotionSettings() {
    }

    /// Returns whether full Material motion is globally enabled.
    ///
    /// @return `true` when global full-motion animations are enabled
    public static boolean areAnimationsEnabled() {
        return globalAnimationsEnabled.get();
    }

    /// Sets whether full Material motion is globally enabled.
    ///
    /// @param enabled whether global full-motion animations should be enabled
    public static void setAnimationsEnabled(boolean enabled) {
        globalAnimationsEnabled.set(enabled);
    }

    /// Returns the global full-motion animation switch property.
    ///
    /// @return the writable global full-motion animation switch property
    public static BooleanProperty animationsEnabledProperty() {
        return globalAnimationsEnabled;
    }

    /// Returns the global motion scheme.
    ///
    /// @return the global motion scheme
    public static M3MotionScheme getMotionScheme() {
        return globalMotionScheme.get();
    }

    /// Sets the global motion scheme.
    ///
    /// @param scheme the global motion scheme
    public static void setMotionScheme(M3MotionScheme scheme) {
        globalMotionScheme.set(Objects.requireNonNull(scheme, "scheme"));
    }

    /// Returns the global motion scheme property.
    ///
    /// @return the writable global motion scheme property
    public static ObjectProperty<M3MotionScheme> motionSchemeProperty() {
        return globalMotionScheme;
    }

    /// Returns the global motion behavior.
    ///
    /// @return the global motion behavior
    public static M3MotionBehavior getMotionBehavior() {
        return globalMotionBehavior.get();
    }

    /// Sets the global motion behavior.
    ///
    /// @param behavior the global motion behavior
    public static void setMotionBehavior(M3MotionBehavior behavior) {
        globalMotionBehavior.set(Objects.requireNonNull(behavior, "behavior"));
    }

    /// Returns the global motion behavior property.
    ///
    /// @return the writable global motion behavior property
    public static ObjectProperty<M3MotionBehavior> motionBehaviorProperty() {
        return globalMotionBehavior;
    }

    /// Returns a read-only revision property that changes when any global or node-local motion setting changes.
    ///
    /// @return the read-only settings revision property
    public static ReadOnlyLongProperty revisionProperty() {
        return readOnlySettingsRevision;
    }

    /// Returns whether full Material motion is enabled for a node after resolving inherited overrides.
    ///
    /// @param node the node used to resolve inherited motion settings
    /// @return `true` when full-motion animations are enabled for the node
    public static boolean areAnimationsEnabled(Node node) {
        Objects.requireNonNull(node, "node");
        if (!areAnimationsEnabled()) {
            return false;
        }

        @Nullable Node current = node;
        while (current != null) {
            if (Boolean.FALSE.equals(getAnimationsEnabled(current))) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

    /// Returns `false` when this node requests reduced motion, or `null` when it inherits its setting.
    ///
    /// @param node the node to query
    /// @return `false` when this node requests reduced motion, or `null` when it inherits its setting
    public static @Nullable Boolean getAnimationsEnabled(Node node) {
        Objects.requireNonNull(node, "node");
        if (!node.hasProperties()) {
            return null;
        }
        @Nullable Object value = node.getProperties().get(ANIMATIONS_ENABLED_KEY);
        return value instanceof Boolean booleanValue ? booleanValue : null;
    }

    /// Sets or clears the node-local reduced-motion request.
    ///
    /// Supplying `false` disables full Material motion for this node and its descendants. Supplying `true` clears
    /// the local request so the node inherits the global and ancestor settings. Consequently a descendant cannot
    /// restore full motion while an ancestor requests reduced motion.
    ///
    /// @param node    the node to update
    /// @param enabled `false` to request reduced motion, or `true` to inherit
    public static void setAnimationsEnabled(Node node, boolean enabled) {
        Objects.requireNonNull(node, "node");
        boolean reducedMotionRequested = Boolean.FALSE.equals(getAnimationsEnabled(node));
        if (reducedMotionRequested == !enabled) {
            return;
        }
        if (enabled) {
            node.getProperties().remove(ANIMATIONS_ENABLED_KEY);
        } else {
            node.getProperties().put(ANIMATIONS_ENABLED_KEY, Boolean.FALSE);
        }
        markSettingsChanged(node);
    }

    /// Clears the node-local reduced-motion request so the node inherits its setting.
    ///
    /// @param node the node whose local reduced-motion request should be cleared
    public static void clearAnimationsEnabled(Node node) {
        setAnimationsEnabled(node, true);
    }

    /// Returns the node-local motion scheme override, or `null` when the node inherits its setting.
    ///
    /// @param node the node to query
    /// @return the node-local motion scheme override, or `null` when the node inherits its setting
    public static @Nullable M3MotionScheme getMotionScheme(Node node) {
        Objects.requireNonNull(node, "node");
        if (!node.hasProperties()) {
            return null;
        }
        @Nullable Object value = node.getProperties().get(MOTION_SCHEME_KEY);
        return value instanceof M3MotionScheme scheme ? scheme : null;
    }

    /// Sets the node-local motion scheme override, or clears it when `null` is supplied.
    ///
    /// @param node   the node to update
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
        markSettingsChanged(node);
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
        if (!node.hasProperties()) {
            return null;
        }
        @Nullable Object value = node.getProperties().get(MOTION_BEHAVIOR_KEY);
        return value instanceof M3MotionBehavior behavior ? behavior : null;
    }

    /// Sets the node-local motion behavior override, or clears it when `null` is supplied.
    ///
    /// @param node     the node to update
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
        markSettingsChanged(node);
    }

    /// Clears the node-local motion behavior override so the node inherits its setting.
    ///
    /// @param node the node whose local motion behavior override should be cleared
    public static void clearMotionBehavior(Node node) {
        setMotionBehavior(node, null);
    }

    /// Increments the settings revision and identifies the affected subtree when one exists.
    private static void markSettingsChanged(@Nullable Node source) {
        settingsRevision.set(settingsRevision.get() + 1L);
        M3MotionSettingsObserver.settingsChanged(source);
    }
}
