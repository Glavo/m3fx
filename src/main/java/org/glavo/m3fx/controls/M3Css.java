// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.StyleableDoubleProperty;
import org.jetbrains.annotations.NotNullByDefault;

/// Provides shared helpers for m3fx CSS-backed component tokens.
@NotNullByDefault
final class M3Css {
    /// Prevents utility class instantiation.
    private M3Css() {
    }

    /// Returns whether a styleable property can be set by CSS.
    static boolean isSettable(StyleableDoubleProperty property) {
        return !property.isBound();
    }

    /// Validates that a CSS size token is not negative.
    static double nonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    /// Validates that a CSS numeric token is finite.
    static double finite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }
}
