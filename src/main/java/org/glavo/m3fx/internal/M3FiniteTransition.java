// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Transition;
import org.jetbrains.annotations.NotNullByDefault;

/// Base transition whose final values can be applied synchronously without starting a JavaFX pulse.
///
/// JavaFX does not guarantee that jumping a stopped custom [Transition] to its total duration invokes
/// `interpolate(1)`. M3FX finite transitions expose this operation so reduced-motion paths can settle reliably.
@NotNullByDefault
public abstract class M3FiniteTransition extends Transition {
    /// Creates a finite transition.
    protected M3FiniteTransition() {
    }

    /// Applies the configured final values.
    final void applyEndValues() {
        interpolate(1.0);
    }
}
