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
/// The global setting requests reduced motion for the complete application. A node may make the same request for
/// its subtree; full motion resumes only after that request is cleared and no ancestor or global setting still
/// requests reduced motion. A descendant cannot override an ancestor's accessibility preference.
///
/// Controls use these settings for state-layer fades, ripple release, popup entrance and exit, smooth scrolling,
/// and progress motion. Requesting reduced motion makes finite transitions settle immediately,
/// while indeterminate activity indicators keep simple linear movement so loading and progress states remain
/// visibly active without playing the full Material motion treatment. Motion curves, durations, and interaction
/// timings are supplied by the active theme rather than this accessibility setting. See
/// [Material motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/).
///
/// Methods that inspect or mutate a live [Node] follow JavaFX scene-graph threading rules and must be called on
/// the JavaFX Application Thread. The global property should likewise be changed on that thread when controls are
/// observing it.
@NotNullByDefault
public final class M3MotionSettings {
    /// The key used to store node-local reduced-motion requests.
    private static final Object REDUCED_MOTION_REQUEST_KEY = new Object();

    /// The application-wide reduced-motion request.
    private static final BooleanProperty globalReducedMotionRequest =
            new SimpleBooleanProperty(M3MotionSettings.class, "globalReducedMotionRequested", false);

    static {
        globalReducedMotionRequest.addListener((observable, oldValue, newValue) -> markSettingsChanged(null));
    }

    /// Prevents instantiation.
    private M3MotionSettings() {
    }

    /// Returns whether the application directly requests reduced motion.
    ///
    /// This value is the application default. Use [shouldReduceMotion] to resolve the effective setting for a node.
    ///
    /// @return `true` when reduced motion is requested application-wide
    public static boolean isGlobalReducedMotionRequested() {
        return globalReducedMotionRequest.get();
    }

    /// Sets whether the application requests reduced motion.
    ///
    /// @param requested whether reduced motion should be requested application-wide
    public static void setGlobalReducedMotionRequested(boolean requested) {
        globalReducedMotionRequest.set(requested);
    }

    /// Returns the application-wide reduced-motion request property.
    ///
    /// @return the writable application-wide reduced-motion request property
    public static BooleanProperty globalReducedMotionRequestedProperty() {
        return globalReducedMotionRequest;
    }

    /// Returns whether a node should use reduced motion after resolving global and inherited requests.
    ///
    /// @param node the node used to resolve inherited motion settings
    /// @return `true` when finite motion should settle immediately for the node
    /// @throws NullPointerException if `node` is `null`
    public static boolean shouldReduceMotion(Node node) {
        Objects.requireNonNull(node, "node");
        if (isGlobalReducedMotionRequested()) {
            return true;
        }

        @Nullable Node current = node;
        while (current != null) {
            if (isReducedMotionRequested(current)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /// Returns whether this node directly requests reduced motion for its subtree.
    ///
    /// @param node the node to query
    /// @return `true` when this node directly requests reduced motion
    /// @throws NullPointerException if `node` is `null`
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
    /// application-wide reduced-motion setting.
    ///
    /// @param node      the node to update
    /// @param requested whether this node should directly request reduced motion
    /// @throws NullPointerException if `node` is `null`
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
