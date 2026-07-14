// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Runtime settings for Material Design 3 motion in M3FX controls.
///
/// The global setting controls the application default. A node may request reduced motion for its complete subtree;
/// full motion resumes only after that request is cleared and no ancestor or global setting still requests reduced
/// motion. A descendant cannot override an ancestor's accessibility preference.
///
/// Controls use these settings for state-layer fades, ripple release, popup entrance and exit, smooth scrolling,
/// and progress motion. Disabling animations requests reduced motion: finite transitions settle immediately,
/// while indeterminate activity indicators keep simple linear movement so loading and progress states remain
/// visibly active without playing the full Material motion treatment. Motion curves, durations, and interaction
/// timings are supplied by the active theme rather than this accessibility setting. See
/// [Material motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3MotionSettings {
    /// The key used to store node-local reduced-motion requests.
    private static final Object REDUCED_MOTION_REQUEST_KEY = new Object();

    /// The global full-motion switch.
    private static final BooleanProperty globalAnimationsEnabled =
            new SimpleBooleanProperty(M3MotionSettings.class, "animationsEnabled", true);

    static {
        globalAnimationsEnabled.addListener((observable, oldValue, newValue) -> markSettingsChanged(null));
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
            if (isReducedMotionRequested(current)) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

    /// Returns whether this node directly requests reduced motion for its subtree.
    ///
    /// @param node the node to query
    /// @return `true` when this node directly requests reduced motion
    public static boolean isReducedMotionRequested(Node node) {
        Objects.requireNonNull(node, "node");
        if (!node.hasProperties()) {
            return false;
        }
        return Boolean.TRUE.equals(node.getProperties().get(REDUCED_MOTION_REQUEST_KEY));
    }

    /// Sets whether this node requests reduced motion for itself and its descendants.
    ///
    /// Clearing the request restores inherited behavior but cannot override a request made by an ancestor or the
    /// global animation setting.
    ///
    /// @param node      the node to update
    /// @param requested whether this node should directly request reduced motion
    public static void setReducedMotionRequested(Node node, boolean requested) {
        Objects.requireNonNull(node, "node");
        if (isReducedMotionRequested(node) == requested) {
            return;
        }
        if (requested) {
            node.getProperties().put(REDUCED_MOTION_REQUEST_KEY, Boolean.TRUE);
        } else if (node.hasProperties()) {
            node.getProperties().remove(REDUCED_MOTION_REQUEST_KEY);
        }
        markSettingsChanged(node);
    }

    /// Notifies internal observers of a changed reduced-motion context.
    private static void markSettingsChanged(@Nullable Node source) {
        M3MotionSettingsObserver.reducedMotionChanged(source);
    }
}
