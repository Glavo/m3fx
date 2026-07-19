// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.DoubleProperty;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// Reusable finite transition for one writable double property.
///
/// Each call to [#configure(M3MotionSpec, double)] captures the property's current value, allowing one transition
/// instance to be retargeted without replacing the property or allocating key frames.
@NotNullByDefault
final class M3DoubleTransition extends M3FiniteTransition {
    /// The property whose value is animated.
    private final DoubleProperty property;

    /// The value at the beginning of the current transition.
    private double startValue;

    /// The value at the end of the current transition.
    private double targetValue;

    /// Creates a transition for a writable double property.
    ///
    /// @param property the property updated by each animation pulse
    M3DoubleTransition(DoubleProperty property) {
        this.property = property;
    }

    /// Reconfigures this transition from the property's current value.
    ///
    /// @param spec        the duration and interpolator for the next run
    /// @param targetValue the value to apply at the end of the next run
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
