// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3StateLayerTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds immutable [M3StateLayerTokens] by replacing named state-layer values.
///
/// Replacement methods retain the supplied values. [build] requires opacity values in `[0.0, 1.0]`, a finite
/// non-negative focus-indicator thickness, and finite focus-indicator offsets. It throws
/// [IllegalArgumentException] when a value violates those constraints. A builder can be reused after building.
///
/// See [Material Design interaction states](https://m3.material.io/foundations/interaction/states/overview).
@NotNullByDefault
public final class M3StateLayerTokensBuilder {
    /// The current hoverOpacity token value.
    private double hoverOpacity;

    /// The current focusOpacity token value.
    private double focusOpacity;

    /// The current pressedOpacity token value.
    private double pressedOpacity;

    /// The current draggedOpacity token value.
    private double draggedOpacity;

    /// The current disabledContainerOpacity token value.
    private double disabledContainerOpacity;

    /// The current disabledContentOpacity token value.
    private double disabledContentOpacity;

    /// The current focusIndicatorThickness token value.
    private double focusIndicatorThickness;

    /// The current focusIndicatorOuterOffset token value.
    private double focusIndicatorOuterOffset;

    /// The current focusIndicatorInnerOffset token value.
    private double focusIndicatorInnerOffset;

    /// Creates a builder initialized from an existing token set.
    ///
    /// @param tokens the token set to copy
    M3StateLayerTokensBuilder(M3StateLayerTokens tokens) {
        M3StateLayerTokens source = Objects.requireNonNull(tokens, "tokens");
        hoverOpacity = source.hoverOpacity();
        focusOpacity = source.focusOpacity();
        pressedOpacity = source.pressedOpacity();
        draggedOpacity = source.draggedOpacity();
        disabledContainerOpacity = source.disabledContainerOpacity();
        disabledContentOpacity = source.disabledContentOpacity();
        focusIndicatorThickness = source.focusIndicatorThickness();
        focusIndicatorOuterOffset = source.focusIndicatorOuterOffset();
        focusIndicatorInnerOffset = source.focusIndicatorInnerOffset();
    }

    /// Replaces the hoverOpacity token value.
    ///
    /// @param hoverOpacity the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder hoverOpacity(double hoverOpacity) {
        this.hoverOpacity = hoverOpacity;
        return this;
    }

    /// Replaces the focusOpacity token value.
    ///
    /// @param focusOpacity the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder focusOpacity(double focusOpacity) {
        this.focusOpacity = focusOpacity;
        return this;
    }

    /// Replaces the pressedOpacity token value.
    ///
    /// @param pressedOpacity the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder pressedOpacity(double pressedOpacity) {
        this.pressedOpacity = pressedOpacity;
        return this;
    }

    /// Replaces the draggedOpacity token value.
    ///
    /// @param draggedOpacity the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder draggedOpacity(double draggedOpacity) {
        this.draggedOpacity = draggedOpacity;
        return this;
    }

    /// Replaces the disabledContainerOpacity token value.
    ///
    /// @param disabledContainerOpacity the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder disabledContainerOpacity(double disabledContainerOpacity) {
        this.disabledContainerOpacity = disabledContainerOpacity;
        return this;
    }

    /// Replaces the disabledContentOpacity token value.
    ///
    /// @param disabledContentOpacity the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder disabledContentOpacity(double disabledContentOpacity) {
        this.disabledContentOpacity = disabledContentOpacity;
        return this;
    }

    /// Replaces the focusIndicatorThickness token value.
    ///
    /// @param focusIndicatorThickness the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder focusIndicatorThickness(double focusIndicatorThickness) {
        this.focusIndicatorThickness = focusIndicatorThickness;
        return this;
    }

    /// Replaces the focusIndicatorOuterOffset token value.
    ///
    /// @param focusIndicatorOuterOffset the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder focusIndicatorOuterOffset(double focusIndicatorOuterOffset) {
        this.focusIndicatorOuterOffset = focusIndicatorOuterOffset;
        return this;
    }

    /// Replaces the focusIndicatorInnerOffset token value.
    ///
    /// @param focusIndicatorInnerOffset the replacement value
    /// @return this builder
    public M3StateLayerTokensBuilder focusIndicatorInnerOffset(double focusIndicatorInnerOffset) {
        this.focusIndicatorInnerOffset = focusIndicatorInnerOffset;
        return this;
    }

    /// Creates an immutable token set from the current builder state.
    ///
    /// @return the built token set
    /// @throws IllegalArgumentException if any value violates the documented state-layer constraints
    public M3StateLayerTokens build() {
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
}
