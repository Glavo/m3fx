// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.animation.Interpolator;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Identifies a named Material Design 3 motion easing curve.
@NotNullByDefault
public enum M3MotionEasing {
    /// Uses a linear animation curve.
    LINEAR("linear", M3Motion.LINEAR),

    /// Uses the standard Material Design 3 easing curve.
    STANDARD("standard", M3Motion.STANDARD),

    /// Uses the standard accelerate Material Design 3 easing curve.
    STANDARD_ACCELERATE("standard-accelerate", M3Motion.STANDARD_ACCELERATE),

    /// Uses the standard decelerate Material Design 3 easing curve.
    STANDARD_DECELERATE("standard-decelerate", M3Motion.STANDARD_DECELERATE),

    /// Uses the emphasized Material Design 3 easing curve.
    EMPHASIZED("emphasized", M3Motion.EMPHASIZED),

    /// Uses the emphasized accelerate Material Design 3 easing curve.
    EMPHASIZED_ACCELERATE("emphasized-accelerate", M3Motion.EMPHASIZED_ACCELERATE),

    /// Uses the emphasized decelerate Material Design 3 easing curve.
    EMPHASIZED_DECELERATE("emphasized-decelerate", M3Motion.EMPHASIZED_DECELERATE);

    /// The stable CSS token name for this easing curve.
    private final String tokenName;

    /// The JavaFX interpolator used by this easing curve.
    private final Interpolator interpolator;

    /// Creates a named easing curve.
    M3MotionEasing(String tokenName, Interpolator interpolator) {
        this.tokenName = Objects.requireNonNull(tokenName, "tokenName");
        this.interpolator = Objects.requireNonNull(interpolator, "interpolator");
    }

    /// Returns the stable CSS token name for this easing curve.
    public String tokenName() {
        return tokenName;
    }

    /// Returns the JavaFX interpolator used by this easing curve.
    public Interpolator interpolator() {
        return interpolator;
    }
}
