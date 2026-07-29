// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.glavo.m3fx.animation.M3SizeTransform;
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

    /// Horizontal offset used by HMCL decorator title-bar `NavBarAnimations` (±50 logical pixels).
    private static final double TITLE_NAV_DISTANCE = 50.0;

    /// Clips retained pages and title content to their assigned animated host bounds.
    private static final M3SizeTransform CLIPPED_SIZE_TRANSFORM = new M3SizeTransform(true, null);

    /// Prevents utility-class instantiation.
    private HMCLDemoTransitions() {
    }

    /// Returns the title-bar transform for navigating deeper (HMCL `NavBarAnimations.NEXT`).
    ///
    /// @return the transform
    static M3ContentTransform titleNext() {
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(20.0))
                .and(M3EnterTransition.slideFrom(M3TransitionEdge.END, TITLE_NAV_DISTANCE));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(M3TransitionEdge.START, TITLE_NAV_DISTANCE));
        return new M3ContentTransform(enter, exit, CLIPPED_SIZE_TRANSFORM, 0.0);
    }

    /// Returns the title-bar transform for navigating back (HMCL `NavBarAnimations.PREVIOUS`).
    ///
    /// @return the transform
    static M3ContentTransform titlePrevious() {
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(20.0))
                .and(M3EnterTransition.slideFrom(M3TransitionEdge.START, TITLE_NAV_DISTANCE));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(M3TransitionEdge.END, TITLE_NAV_DISTANCE));
        return new M3ContentTransform(enter, exit, CLIPPED_SIZE_TRANSFORM, 0.0);
    }

    /// Returns the title-bar fade transform used for section or neutral title updates.
    ///
    /// @return the transform
    static M3ContentTransform titleFade() {
        return new M3ContentTransform(
                M3EnterTransition.fade(0.0).withDelay(Duration.millis(20.0)),
                M3ExitTransition.fade(0.0),
                CLIPPED_SIZE_TRANSFORM,
                0.0
        );
    }

    /// Returns the ordinary shell navigation transform (home ↔ primary destinations).
    ///
    /// Host-level motion is fade only. HMCL `ContainerAnimations.NAVIGATION` does **not** pan the whole page; it
    /// fades while optionally offsetting a `DecoratorAnimatedPage` left/center pair by ±30px. The shell applies that
    /// split on [javafx.scene.layout.BorderPane] pages separately so the content area never slides as one solid block.
    ///
    /// @return the transform
    static M3ContentTransform navigation() {
        return new M3ContentTransform(
                M3EnterTransition.fade(0.0).withDelay(Duration.millis(40.0)),
                M3ExitTransition.fade(0.0),
                CLIPPED_SIZE_TRANSFORM,
                0.0
        );
    }

    /// Returns the reverse of [#navigation] used when returning toward home.
    ///
    /// Same host fade as [#navigation]; directionality for the left/center split is applied by the shell.
    ///
    /// @return the transform
    static M3ContentTransform navigationBack() {
        return navigation();
    }

    /// Horizontal offset applied to left/center panes during shell navigation (HMCL `NAVIGATION`).
    ///
    /// @return distance in logical pixels
    static double navigationSplitDistance() {
        return NAVIGATION_DISTANCE;
    }

    /// Returns a hierarchical forward navigation transform.
    ///
    /// @return the transform
    static M3ContentTransform forward() {
        return slide(M3TransitionEdge.END, M3TransitionEdge.START);
    }

    /// Returns a hierarchical backward navigation transform.
    ///
    /// @return the transform
    static M3ContentTransform backward() {
        return slide(M3TransitionEdge.START, M3TransitionEdge.END);
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
                CLIPPED_SIZE_TRANSFORM,
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
                CLIPPED_SIZE_TRANSFORM,
                0.0
        );
    }

    /// Builds a shared-axis slide transform.
    ///
    /// @param enterEdge the edge from which the next page enters
    /// @param exitEdge the edge toward which the previous page exits
    /// @return the transform
    private static M3ContentTransform slide(
            M3TransitionEdge enterEdge,
            M3TransitionEdge exitEdge
    ) {
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(30.0))
                .and(M3EnterTransition.slideFrom(enterEdge, PAGE_ENTER_DISTANCE));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(exitEdge, PAGE_EXIT_DISTANCE));
        return new M3ContentTransform(enter, exit, null, 0.0);
    }
}
