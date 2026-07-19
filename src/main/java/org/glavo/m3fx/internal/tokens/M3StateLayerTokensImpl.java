// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.jetbrains.annotations.NotNullByDefault;

/// Default immutable implementation of [M3StateLayerTokens].
///
/// Opacity values are in the inclusive range `[0.0, 1.0]`. Focus indicator metrics are expressed in JavaFX
/// logical pixels; thickness must be non-negative, while offsets may be negative.
///
/// @param hoverOpacity              the hover state layer opacity
/// @param focusOpacity              the focus state layer opacity
/// @param pressedOpacity            the pressed state layer opacity
/// @param draggedOpacity            the dragged state layer opacity
/// @param disabledContainerOpacity  the disabled container opacity
/// @param disabledContentOpacity    the disabled content opacity
/// @param focusIndicatorThickness   the keyboard focus indicator thickness
/// @param focusIndicatorOuterOffset the keyboard focus indicator outer offset
/// @param focusIndicatorInnerOffset the keyboard focus indicator inner offset
@NotNullByDefault
public record M3StateLayerTokensImpl(
        double hoverOpacity,
        double focusOpacity,
        double pressedOpacity,
        double draggedOpacity,
        double disabledContainerOpacity,
        double disabledContentOpacity,
        double focusIndicatorThickness,
        double focusIndicatorOuterOffset,
        double focusIndicatorInnerOffset
) implements M3StateLayerTokens {
    /// Creates state layer tokens.
    ///
    /// @throws IllegalArgumentException if an opacity is outside `[0.0, 1.0]`, a thickness is negative, or any
    ///                                  value is not finite
    public M3StateLayerTokensImpl {
        validateOpacity(hoverOpacity, "hoverOpacity");
        validateOpacity(focusOpacity, "focusOpacity");
        validateOpacity(pressedOpacity, "pressedOpacity");
        validateOpacity(draggedOpacity, "draggedOpacity");
        validateOpacity(disabledContainerOpacity, "disabledContainerOpacity");
        validateOpacity(disabledContentOpacity, "disabledContentOpacity");
        validateNonNegative(focusIndicatorThickness, "focusIndicatorThickness");
        validateFinite(focusIndicatorOuterOffset, "focusIndicatorOuterOffset");
        validateFinite(focusIndicatorInnerOffset, "focusIndicatorInnerOffset");
    }

    /// Validates an opacity token.
    private static void validateOpacity(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be finite and between 0.0 and 1.0");
        }
    }

    /// Validates a non-negative length token.
    private static void validateNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }

    /// Validates a finite length token.
    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
