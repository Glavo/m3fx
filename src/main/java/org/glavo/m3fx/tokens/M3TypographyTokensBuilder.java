// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TypographyTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Builds immutable [M3TypographyTokens] by replacing named type-scale roles.
///
/// A builder starts from a complete type scale. Replacement methods reject `null` before changing the builder and
/// return this builder for method chaining. [build] creates an independent immutable snapshot; later changes do
/// not affect previously built token sets. Builders may be reused but are not thread-safe.
///
/// See [Material Design typography](https://m3.material.io/styles/typography/overview).
@NotNullByDefault
public final class M3TypographyTokensBuilder {
    /// The current displayLarge token value.
    private M3TextStyle displayLarge;

    /// The current displayMedium token value.
    private M3TextStyle displayMedium;

    /// The current displaySmall token value.
    private M3TextStyle displaySmall;

    /// The current headlineLarge token value.
    private M3TextStyle headlineLarge;

    /// The current headlineMedium token value.
    private M3TextStyle headlineMedium;

    /// The current headlineSmall token value.
    private M3TextStyle headlineSmall;

    /// The current titleLarge token value.
    private M3TextStyle titleLarge;

    /// The current titleMedium token value.
    private M3TextStyle titleMedium;

    /// The current titleSmall token value.
    private M3TextStyle titleSmall;

    /// The current labelLarge token value.
    private M3TextStyle labelLarge;

    /// The current labelMedium token value.
    private M3TextStyle labelMedium;

    /// The current labelSmall token value.
    private M3TextStyle labelSmall;

    /// The current bodyLarge token value.
    private M3TextStyle bodyLarge;

    /// The current bodyMedium token value.
    private M3TextStyle bodyMedium;

    /// The current bodySmall token value.
    private M3TextStyle bodySmall;

    /// Creates a builder initialized from an existing token set.
    ///
    /// @param tokens the token set to copy
    M3TypographyTokensBuilder(M3TypographyTokens tokens) {
        M3TypographyTokens source = Objects.requireNonNull(tokens, "tokens");
        displayLarge = source.displayLarge();
        displayMedium = source.displayMedium();
        displaySmall = source.displaySmall();
        headlineLarge = source.headlineLarge();
        headlineMedium = source.headlineMedium();
        headlineSmall = source.headlineSmall();
        titleLarge = source.titleLarge();
        titleMedium = source.titleMedium();
        titleSmall = source.titleSmall();
        labelLarge = source.labelLarge();
        labelMedium = source.labelMedium();
        labelSmall = source.labelSmall();
        bodyLarge = source.bodyLarge();
        bodyMedium = source.bodyMedium();
        bodySmall = source.bodySmall();
    }

    /// Replaces the displayLarge token value.
    ///
    /// @param displayLarge the replacement value
    /// @return this builder
    /// @throws NullPointerException if `displayLarge` is `null`
    public M3TypographyTokensBuilder displayLarge(M3TextStyle displayLarge) {
        this.displayLarge = Objects.requireNonNull(displayLarge, "displayLarge");
        return this;
    }

    /// Replaces the displayMedium token value.
    ///
    /// @param displayMedium the replacement value
    /// @return this builder
    /// @throws NullPointerException if `displayMedium` is `null`
    public M3TypographyTokensBuilder displayMedium(M3TextStyle displayMedium) {
        this.displayMedium = Objects.requireNonNull(displayMedium, "displayMedium");
        return this;
    }

    /// Replaces the displaySmall token value.
    ///
    /// @param displaySmall the replacement value
    /// @return this builder
    /// @throws NullPointerException if `displaySmall` is `null`
    public M3TypographyTokensBuilder displaySmall(M3TextStyle displaySmall) {
        this.displaySmall = Objects.requireNonNull(displaySmall, "displaySmall");
        return this;
    }

    /// Replaces the headlineLarge token value.
    ///
    /// @param headlineLarge the replacement value
    /// @return this builder
    /// @throws NullPointerException if `headlineLarge` is `null`
    public M3TypographyTokensBuilder headlineLarge(M3TextStyle headlineLarge) {
        this.headlineLarge = Objects.requireNonNull(headlineLarge, "headlineLarge");
        return this;
    }

    /// Replaces the headlineMedium token value.
    ///
    /// @param headlineMedium the replacement value
    /// @return this builder
    /// @throws NullPointerException if `headlineMedium` is `null`
    public M3TypographyTokensBuilder headlineMedium(M3TextStyle headlineMedium) {
        this.headlineMedium = Objects.requireNonNull(headlineMedium, "headlineMedium");
        return this;
    }

    /// Replaces the headlineSmall token value.
    ///
    /// @param headlineSmall the replacement value
    /// @return this builder
    /// @throws NullPointerException if `headlineSmall` is `null`
    public M3TypographyTokensBuilder headlineSmall(M3TextStyle headlineSmall) {
        this.headlineSmall = Objects.requireNonNull(headlineSmall, "headlineSmall");
        return this;
    }

    /// Replaces the titleLarge token value.
    ///
    /// @param titleLarge the replacement value
    /// @return this builder
    /// @throws NullPointerException if `titleLarge` is `null`
    public M3TypographyTokensBuilder titleLarge(M3TextStyle titleLarge) {
        this.titleLarge = Objects.requireNonNull(titleLarge, "titleLarge");
        return this;
    }

    /// Replaces the titleMedium token value.
    ///
    /// @param titleMedium the replacement value
    /// @return this builder
    /// @throws NullPointerException if `titleMedium` is `null`
    public M3TypographyTokensBuilder titleMedium(M3TextStyle titleMedium) {
        this.titleMedium = Objects.requireNonNull(titleMedium, "titleMedium");
        return this;
    }

    /// Replaces the titleSmall token value.
    ///
    /// @param titleSmall the replacement value
    /// @return this builder
    /// @throws NullPointerException if `titleSmall` is `null`
    public M3TypographyTokensBuilder titleSmall(M3TextStyle titleSmall) {
        this.titleSmall = Objects.requireNonNull(titleSmall, "titleSmall");
        return this;
    }

    /// Replaces the labelLarge token value.
    ///
    /// @param labelLarge the replacement value
    /// @return this builder
    /// @throws NullPointerException if `labelLarge` is `null`
    public M3TypographyTokensBuilder labelLarge(M3TextStyle labelLarge) {
        this.labelLarge = Objects.requireNonNull(labelLarge, "labelLarge");
        return this;
    }

    /// Replaces the labelMedium token value.
    ///
    /// @param labelMedium the replacement value
    /// @return this builder
    /// @throws NullPointerException if `labelMedium` is `null`
    public M3TypographyTokensBuilder labelMedium(M3TextStyle labelMedium) {
        this.labelMedium = Objects.requireNonNull(labelMedium, "labelMedium");
        return this;
    }

    /// Replaces the labelSmall token value.
    ///
    /// @param labelSmall the replacement value
    /// @return this builder
    /// @throws NullPointerException if `labelSmall` is `null`
    public M3TypographyTokensBuilder labelSmall(M3TextStyle labelSmall) {
        this.labelSmall = Objects.requireNonNull(labelSmall, "labelSmall");
        return this;
    }

    /// Replaces the bodyLarge token value.
    ///
    /// @param bodyLarge the replacement value
    /// @return this builder
    /// @throws NullPointerException if `bodyLarge` is `null`
    public M3TypographyTokensBuilder bodyLarge(M3TextStyle bodyLarge) {
        this.bodyLarge = Objects.requireNonNull(bodyLarge, "bodyLarge");
        return this;
    }

    /// Replaces the bodyMedium token value.
    ///
    /// @param bodyMedium the replacement value
    /// @return this builder
    /// @throws NullPointerException if `bodyMedium` is `null`
    public M3TypographyTokensBuilder bodyMedium(M3TextStyle bodyMedium) {
        this.bodyMedium = Objects.requireNonNull(bodyMedium, "bodyMedium");
        return this;
    }

    /// Replaces the bodySmall token value.
    ///
    /// @param bodySmall the replacement value
    /// @return this builder
    /// @throws NullPointerException if `bodySmall` is `null`
    public M3TypographyTokensBuilder bodySmall(M3TextStyle bodySmall) {
        this.bodySmall = Objects.requireNonNull(bodySmall, "bodySmall");
        return this;
    }

    /// Creates an immutable snapshot of the current type-scale role assignments.
    ///
    /// @return a new immutable typography token set; never `null`
    public M3TypographyTokens build() {
        return new M3TypographyTokensImpl(
                displayLarge,
                displayMedium,
                displaySmall,
                headlineLarge,
                headlineMedium,
                headlineSmall,
                titleLarge,
                titleMedium,
                titleSmall,
                labelLarge,
                labelMedium,
                labelSmall,
                bodyLarge,
                bodyMedium,
                bodySmall
        );
    }
}
