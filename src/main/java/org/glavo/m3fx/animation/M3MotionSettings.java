// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Provides the application-wide reduced-motion preference for M3FX components.
///
/// The global property requests reduced motion for the complete application. Individual components expose
/// [M3Animated#animationsEnabledProperty()], whose CSS property `-m3-animations-enabled` supports inheritance.
/// A component cannot override the global request by enabling its own animations.
///
/// Controls use these settings for state-layer fades, ripple release, popup entrance and exit, smooth scrolling,
/// and progress motion. Requesting reduced motion makes finite transitions settle immediately,
/// while indeterminate activity indicators keep simple linear movement so loading and progress states remain
/// visibly active without playing the full Material motion treatment. Motion curves, durations, and interaction
/// timings are supplied by the active theme rather than this accessibility setting. See
/// [Material motion](https://m3.material.io/styles/motion/overview) and
/// [Material Design](https://m3.material.io/).
///
/// The global property is writable and may be observed or bound like any other JavaFX [BooleanProperty]. Calling
/// [setGlobalReducedMotionRequested] delegates to that property; if it is unidirectionally bound, direct writes
/// follow the failure behavior defined by [BooleanProperty].
///
/// Methods that inspect or mutate a live [Node] follow JavaFX scene-graph threading rules. The global property
/// should likewise be changed on the JavaFX Application Thread while controls are observing it.
@NotNullByDefault
public final class M3MotionSettings {
    /// Prevents instantiation.
    private M3MotionSettings() {
    }

    /// The application-wide reduced-motion request.
    ///
    /// @defaultValue `false`
    private static final BooleanProperty globalReducedMotionRequest =
            new SimpleBooleanProperty(M3MotionSettings.class, "globalReducedMotionRequested", false);

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
    /// If the global property is bound, this method has the same direct-write behavior as
    /// [BooleanProperty#set(boolean)].
    ///
    /// @param requested whether reduced motion should be requested application-wide
    public static void setGlobalReducedMotionRequested(boolean requested) {
        globalReducedMotionRequest.set(requested);
    }

    /// Returns the singleton application-wide reduced-motion request property.
    ///
    /// The returned property is live and writable. Its initial value is `false`.
    ///
    /// @return the writable application-wide reduced-motion request property
    public static BooleanProperty globalReducedMotionRequestedProperty() {
        return globalReducedMotionRequest;
    }

    /// Returns whether a node should use reduced motion under the application and component settings.
    ///
    /// For an [M3Animated] component, the current value of its animation property is combined with the global
    /// request. Other nodes use only the global request. This method does not walk the parent chain or apply CSS;
    /// inherited CSS changes become observable after the normal CSS pass.
    ///
    /// @param node the animation owner
    /// @return `true` when finite motion should settle immediately for the node
    /// @throws NullPointerException if `node` is `null`
    public static boolean shouldReduceMotion(Node node) {
        Objects.requireNonNull(node, "node");
        return isGlobalReducedMotionRequested()
                || node instanceof M3Animated animated && !animated.isAnimationsEnabled();
    }
}
