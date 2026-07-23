// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.jetbrains.annotations.NotNullByDefault;

/// Shared page and section transitions for the HMCL Material 3 demo.
///
/// Motion follows HMCL `ContainerAnimations` while enter and exit effects are implemented with M3FX primitives.
/// - [#navigation] matches ordinary `Controllers.navigate` (short offset + fade), not a full-width page pan
/// - [#forward] / [#backward] match hierarchical `navigateForward` / stack pop
/// - [#sectionUp] matches in-page tab changes (`SLIDE_UP_FADE_IN`)
@NotNullByDefault
final class HMCLDemoTransitions {
    /// Short horizontal offset used by HMCL `ContainerAnimations.NAVIGATION` (±30 logical pixels).
    private static final double NAVIGATION_DISTANCE = 30.0;

    /// Enter slide distance used by hierarchical shared-axis navigation.
    private static final double PAGE_ENTER_DISTANCE = 48.0;

    /// Exit slide distance used by hierarchical shared-axis navigation.
    private static final double PAGE_EXIT_DISTANCE = 36.0;

    /// Vertical distance used by in-page section transitions.
    private static final double SECTION_ENTER_DISTANCE = 40.0;

    /// Prevents utility-class instantiation.
    private HMCLDemoTransitions() {
    }

    /// Returns the ordinary shell navigation transform (home ↔ primary destinations).
    ///
    /// @return the transform
    static M3ContentTransform navigation() {
        // Mild dual-offset fade: outgoing drifts toward start, incoming arrives from end.
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(40.0))
                .and(M3EnterTransition.slideFrom(M3TransitionEdge.END, NAVIGATION_DISTANCE));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(M3TransitionEdge.START, NAVIGATION_DISTANCE));
        return new M3ContentTransform(enter, exit, null, 0.0);
    }

    /// Returns the reverse of [#navigation] used when returning toward home.
    ///
    /// @return the transform
    static M3ContentTransform navigationBack() {
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(40.0))
                .and(M3EnterTransition.slideFrom(M3TransitionEdge.START, NAVIGATION_DISTANCE));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(M3TransitionEdge.END, NAVIGATION_DISTANCE));
        return new M3ContentTransform(enter, exit, null, 0.0);
    }

    /// Returns a hierarchical forward navigation transform.
    ///
    /// @return the transform
    static M3ContentTransform forward() {
        return slide(M3TransitionEdge.END, M3TransitionEdge.START, PAGE_ENTER_DISTANCE, PAGE_EXIT_DISTANCE);
    }

    /// Returns a hierarchical backward navigation transform.
    ///
    /// @return the transform
    static M3ContentTransform backward() {
        return slide(M3TransitionEdge.START, M3TransitionEdge.END, PAGE_ENTER_DISTANCE, PAGE_EXIT_DISTANCE);
    }

    /// Returns a slide-up fade transform used for in-page section changes.
    ///
    /// @return the transform
    static M3ContentTransform sectionUp() {
        return new M3ContentTransform(
                M3EnterTransition.fade(0.0)
                        .withDelay(Duration.millis(40.0))
                        .and(M3EnterTransition.slideFrom(M3TransitionEdge.BOTTOM, SECTION_ENTER_DISTANCE)),
                M3ExitTransition.fade(0.0),
                null,
                0.0
        );
    }

    /// Returns an immediate snap transform used for first paint and when animation is disabled.
    ///
    /// @return the transform
    static M3ContentTransform none() {
        return new M3ContentTransform(
                M3EnterTransition.fade(0.0),
                M3ExitTransition.fade(0.0),
                null,
                0.0
        );
    }

    /// Builds a shared-axis slide transform.
    ///
    /// @param enterEdge the edge from which the next page enters
    /// @param exitEdge the edge toward which the previous page exits
    /// @param enterDistance enter slide distance
    /// @param exitDistance exit slide distance
    /// @return the transform
    private static M3ContentTransform slide(
            M3TransitionEdge enterEdge,
            M3TransitionEdge exitEdge,
            double enterDistance,
            double exitDistance
    ) {
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(30.0))
                .and(M3EnterTransition.slideFrom(enterEdge, enterDistance));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(exitEdge, exitDistance));
        return new M3ContentTransform(enter, exit, null, 0.0);
    }
}
