// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ShapeTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds immutable [M3ShapeTokens] by replacing named token values.
@NotNullByDefault
public final class M3ShapeTokensBuilder {
    /// The current none token value.
    private double none;

    /// The current extraSmall token value.
    private double extraSmall;

    /// The current small token value.
    private double small;

    /// The current medium token value.
    private double medium;

    /// The current large token value.
    private double large;

    /// The current largeIncreased token value.
    private double largeIncreased;

    /// The current extraLarge token value.
    private double extraLarge;

    /// The current extraLargeIncreased token value.
    private double extraLargeIncreased;

    /// The current extraExtraLarge token value.
    private double extraExtraLarge;

    /// The current full token value.
    private double full;

    /// Creates a builder initialized from an existing token set.
    ///
    /// @param tokens the token set to copy
    M3ShapeTokensBuilder(M3ShapeTokens tokens) {
        M3ShapeTokens source = Objects.requireNonNull(tokens, "tokens");
        none = source.none();
        extraSmall = source.extraSmall();
        small = source.small();
        medium = source.medium();
        large = source.large();
        largeIncreased = source.largeIncreased();
        extraLarge = source.extraLarge();
        extraLargeIncreased = source.extraLargeIncreased();
        extraExtraLarge = source.extraExtraLarge();
        full = source.full();
    }

    /// Replaces the none token value.
    ///
    /// @param none the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder none(double none) {
        this.none = none;
        return this;
    }

    /// Replaces the extraSmall token value.
    ///
    /// @param extraSmall the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder extraSmall(double extraSmall) {
        this.extraSmall = extraSmall;
        return this;
    }

    /// Replaces the small token value.
    ///
    /// @param small the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder small(double small) {
        this.small = small;
        return this;
    }

    /// Replaces the medium token value.
    ///
    /// @param medium the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder medium(double medium) {
        this.medium = medium;
        return this;
    }

    /// Replaces the large token value.
    ///
    /// @param large the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder large(double large) {
        this.large = large;
        return this;
    }

    /// Replaces the largeIncreased token value.
    ///
    /// @param largeIncreased the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder largeIncreased(double largeIncreased) {
        this.largeIncreased = largeIncreased;
        return this;
    }

    /// Replaces the extraLarge token value.
    ///
    /// @param extraLarge the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder extraLarge(double extraLarge) {
        this.extraLarge = extraLarge;
        return this;
    }

    /// Replaces the extraLargeIncreased token value.
    ///
    /// @param extraLargeIncreased the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder extraLargeIncreased(double extraLargeIncreased) {
        this.extraLargeIncreased = extraLargeIncreased;
        return this;
    }

    /// Replaces the extraExtraLarge token value.
    ///
    /// @param extraExtraLarge the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder extraExtraLarge(double extraExtraLarge) {
        this.extraExtraLarge = extraExtraLarge;
        return this;
    }

    /// Replaces the full token value.
    ///
    /// @param full the replacement value
    /// @return this builder
    public M3ShapeTokensBuilder full(double full) {
        this.full = full;
        return this;
    }

    /// Creates an immutable token set from the current builder state.
    ///
    /// @return the built token set
    public M3ShapeTokens build() {
        return new M3ShapeTokensImpl(
                none,
                extraSmall,
                small,
                medium,
                large,
                largeIncreased,
                extraLarge,
                extraLargeIncreased,
                extraExtraLarge,
                full
        );
    }
}

