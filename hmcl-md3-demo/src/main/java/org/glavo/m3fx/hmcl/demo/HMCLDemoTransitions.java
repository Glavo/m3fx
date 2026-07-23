// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// Shared content transforms for the HMCL Material 3 demo.
///
/// Page and section replacements use M3FX's Material fade-through default rather than HMCL's slide distances.
/// Curves and durations still come from the active theme; reduced-motion requests settle immediately in the
/// animated host.
@NotNullByDefault
final class HMCLDemoTransitions {
    /// Prevents utility-class instantiation.
    private HMCLDemoTransitions() {
    }

    /// Returns the Material fade-through transform used for ordinary page navigation.
    ///
    /// @return the transform
    static M3ContentTransform navigation() {
        return M3ContentTransform.DEFAULT;
    }

    /// Returns the Material fade-through transform used when pushing a secondary route.
    ///
    /// @return the transform
    static M3ContentTransform forward() {
        return M3ContentTransform.DEFAULT;
    }

    /// Returns the Material fade-through transform used when popping a secondary route.
    ///
    /// @return the transform
    static M3ContentTransform backward() {
        return M3ContentTransform.DEFAULT;
    }

    /// Returns the Material fade-through transform used for in-page section changes.
    ///
    /// @return the transform
    static M3ContentTransform sectionUp() {
        return M3ContentTransform.DEFAULT;
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
}
