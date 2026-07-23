// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.util.Duration;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.jetbrains.annotations.NotNullByDefault;

/// Shared page and section transitions modeled on HMCL `ContainerAnimations`.
@NotNullByDefault
final class HMCLDemoTransitions {
    /// Enter slide distance used by primary navigation.
    private static final double PAGE_ENTER_DISTANCE = 36.0;

    /// Exit slide distance used by primary navigation.
    private static final double PAGE_EXIT_DISTANCE = 24.0;

    /// Enter delay used by fade-through section changes.
    private static final Duration SECTION_ENTER_DELAY = Duration.millis(40.0);

    /// Prevents utility-class instantiation.
    private HMCLDemoTransitions() {
    }

    /// Returns a forward navigation transform (content enters from the logical end).
    ///
    /// @return the transform
    static M3ContentTransform forward() {
        return slide(M3TransitionEdge.END, M3TransitionEdge.START);
    }

    /// Returns a backward navigation transform (content enters from the logical start).
    ///
    /// @return the transform
    static M3ContentTransform backward() {
        return slide(M3TransitionEdge.START, M3TransitionEdge.END);
    }

    /// Returns a fade-through transform used for left-pane tab changes.
    ///
    /// @return the transform
    static M3ContentTransform sectionFade() {
        return new M3ContentTransform(
                M3EnterTransition.fade(0.0)
                        .withDelay(SECTION_ENTER_DELAY)
                        .and(M3EnterTransition.scale(0.98)),
                M3ExitTransition.fade(0.0),
                null,
                0.0
        );
    }

    /// Builds a shared-axis slide transform.
    ///
    /// @param enterEdge the edge from which the next page enters
    /// @param exitEdge the edge toward which the previous page exits
    /// @return the transform
    private static M3ContentTransform slide(M3TransitionEdge enterEdge, M3TransitionEdge exitEdge) {
        M3EnterTransition enter = M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(30.0))
                .and(M3EnterTransition.slideFrom(enterEdge, PAGE_ENTER_DISTANCE));
        M3ExitTransition exit = M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(exitEdge, PAGE_EXIT_DISTANCE));
        return new M3ContentTransform(enter, exit, null, 0.0);
    }
}
