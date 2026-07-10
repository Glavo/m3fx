// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.DoubleProperty;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// Reusable finite transition for one writable double property.
@NotNullByDefault
final class M3DoubleTransition extends M3FiniteTransition {
    /// The property whose value is animated.
    private final DoubleProperty property;

    /// The value at the beginning of the current transition.
    private double startValue;

    /// The value at the end of the current transition.
    private double targetValue;

    /// Creates a transition for a writable double property.
    M3DoubleTransition(DoubleProperty property) {
        this.property = property;
    }

    /// Reconfigures this transition from the property's current value.
    void configure(M3MotionSpec spec, double targetValue) {
        stop();
        setCycleDuration(spec.duration());
        setInterpolator(spec.interpolator());
        startValue = property.get();
        this.targetValue = targetValue;
    }

    /// Applies the eased property value for the current pulse.
    @Override
    protected void interpolate(double fraction) {
        property.set(startValue + (targetValue - startValue) * fraction);
    }
}
