// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3StateLayerTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Defines immutable Material Design 3 interaction-state values.
///
/// State-layer and disabled opacities are finite values in `[0.0, 1.0]`. Focus-indicator lengths use JavaFX
/// logical pixels; thickness is finite and non-negative, while offsets may be negative. Controls combine these
/// semantic values with their active color roles.
///
/// See [Material Design interaction states](https://m3.material.io/foundations/interaction/states/overview).
@NotNullByDefault
public sealed interface M3StateLayerTokens permits M3StateLayerTokensImpl {
    /// Returns the hover state layer opacity.
    ///
    /// @return the hover state layer opacity in `[0.0, 1.0]`
    double hoverOpacity();

    /// Returns the focus state layer opacity.
    ///
    /// @return the focus state layer opacity in `[0.0, 1.0]`
    double focusOpacity();

    /// Returns the pressed state layer opacity.
    ///
    /// @return the pressed state layer opacity in `[0.0, 1.0]`
    double pressedOpacity();

    /// Returns the dragged state layer opacity.
    ///
    /// @return the dragged state layer opacity in `[0.0, 1.0]`
    double draggedOpacity();

    /// Returns the disabled container opacity.
    ///
    /// @return the disabled container opacity in `[0.0, 1.0]`
    double disabledContainerOpacity();

    /// Returns the disabled content opacity.
    ///
    /// @return the disabled content opacity in `[0.0, 1.0]`
    double disabledContentOpacity();

    /// Returns the keyboard focus indicator thickness.
    ///
    /// @return the finite, non-negative keyboard focus indicator thickness in logical pixels
    double focusIndicatorThickness();

    /// Returns the keyboard focus indicator outer offset.
    ///
    /// @return the finite keyboard focus indicator outer offset in logical pixels
    double focusIndicatorOuterOffset();

    /// Returns the keyboard focus indicator inner offset.
    ///
    /// @return the finite keyboard focus indicator inner offset in logical pixels
    double focusIndicatorInnerOffset();

    /// Creates a builder initialized with all values from [baseline].
    ///
    /// @return a mutable state-layer-token builder
    static M3StateLayerTokensBuilder builder() {
        return new M3StateLayerTokensBuilder(baseline());
    }

    /// Creates a builder initialized from an existing state-layer token set.
    ///
    /// @param tokens the state-layer tokens to copy
    /// @return a mutable state-layer-token builder
    /// @throws NullPointerException if `tokens` is `null`
    static M3StateLayerTokensBuilder builder(M3StateLayerTokens tokens) {
        return new M3StateLayerTokensBuilder(tokens);
    }

    /// Creates state layer opacity tokens.
    ///
    /// @param hoverOpacity the hover state layer opacity
    /// @param focusOpacity the focus state layer opacity
    /// @param pressedOpacity the pressed state layer opacity
    /// @param draggedOpacity the dragged state layer opacity
    /// @param disabledContainerOpacity the disabled container opacity
    /// @param disabledContentOpacity the disabled content opacity
    /// @return the created state layer token set
    private static M3StateLayerTokens create(
            double hoverOpacity,
            double focusOpacity,
            double pressedOpacity,
            double draggedOpacity,
            double disabledContainerOpacity,
            double disabledContentOpacity
    ) {
        return create(
                hoverOpacity,
                focusOpacity,
                pressedOpacity,
                draggedOpacity,
                disabledContainerOpacity,
                disabledContentOpacity,
                3.0,
                2.0,
                -2.0
        );
    }

    /// Creates state tokens.
    ///
    /// @param hoverOpacity the hover state layer opacity
    /// @param focusOpacity the focus state layer opacity
    /// @param pressedOpacity the pressed state layer opacity
    /// @param draggedOpacity the dragged state layer opacity
    /// @param disabledContainerOpacity the disabled container opacity
    /// @param disabledContentOpacity the disabled content opacity
    /// @param focusIndicatorThickness the keyboard focus indicator thickness in pixels
    /// @param focusIndicatorOuterOffset the keyboard focus indicator outer offset in pixels
    /// @param focusIndicatorInnerOffset the keyboard focus indicator inner offset in pixels
    /// @return the created state token set
    private static M3StateLayerTokens create(
            double hoverOpacity,
            double focusOpacity,
            double pressedOpacity,
            double draggedOpacity,
            double disabledContainerOpacity,
            double disabledContentOpacity,
            double focusIndicatorThickness,
            double focusIndicatorOuterOffset,
            double focusIndicatorInnerOffset
    ) {
        return new M3StateLayerTokensImpl(
                hoverOpacity,
                focusOpacity,
                pressedOpacity,
                draggedOpacity,
                disabledContainerOpacity,
                disabledContentOpacity,
                focusIndicatorThickness,
                focusIndicatorOuterOffset,
                focusIndicatorInnerOffset
        );
    }

    /// Returns baseline Material Design 3 state layer tokens.
    ///
    /// @return baseline Material Design 3 state layer tokens
    static M3StateLayerTokens baseline() {
        return create(0.08, 0.10, 0.10, 0.16, 0.12, 0.38);
    }

}
