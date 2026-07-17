// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3TokenSetImpl;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Builds an immutable [M3TokenSet] from profile defaults and explicit token-group overrides.
///
/// A builder starts with a complete baseline or expressive token set. Setters replace whole semantic token groups,
/// which avoids positional factory methods with many unrelated arguments and keeps custom themes internally
/// consistent. Unless [componentTokens(M3ComponentTokens)] is called, component tokens are derived at build time
/// from the current profile, shape tokens, and density.
///
/// Replacement token groups must not be `null`; replacement methods throw [NullPointerException] when that
/// contract is violated. A builder can be reused after [build].
///
/// See [Material Design styles](https://m3.material.io/styles).
@NotNullByDefault
public final class M3TokenSetBuilder {
    /// The immutable profile for the token set being built.
    private final M3Profile profile;

    /// The immutable density for the token set being built.
    private final M3Density density;

    /// The current color token group.
    private M3ColorTokens colorTokens;

    /// The current typography token group.
    private M3TypographyTokens typographyTokens;

    /// The current shape token group.
    private M3ShapeTokens shapeTokens;

    /// The current elevation token group.
    private M3ElevationTokens elevationTokens;

    /// The current motion token group.
    private M3MotionTokens motionTokens;

    /// The current state-layer token group.
    private M3StateLayerTokens stateLayerTokens;

    /// An explicit component-token override, or `null` to derive component tokens when building.
    private @Nullable M3ComponentTokens componentTokens;

    /// Creates a builder initialized with complete defaults for a profile.
    ///
    /// @param profile the Material token profile
    /// @param colorScheme the MonetFX color scheme
    /// @param density the density used for component metrics
    M3TokenSetBuilder(M3Profile profile, ColorScheme colorScheme, M3Density density) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.density = Objects.requireNonNull(density, "density");
        colorTokens = M3ColorTokens.fromColorScheme(Objects.requireNonNull(colorScheme, "colorScheme"));
        boolean expressive = profile == M3Profile.EXPRESSIVE_2025;
        typographyTokens = expressive ? M3TypographyTokens.expressive() : M3TypographyTokens.baseline();
        shapeTokens = expressive ? M3ShapeTokens.expressive() : M3ShapeTokens.baseline();
        elevationTokens = M3ElevationTokens.baseline();
        motionTokens = expressive ? M3MotionTokens.expressive() : M3MotionTokens.baseline();
        stateLayerTokens = M3StateLayerTokens.baseline();
    }

    /// Creates a builder initialized with every token group from an existing token set.
    ///
    /// @param tokenSet the token set to copy
    M3TokenSetBuilder(M3TokenSet tokenSet) {
        M3TokenSet source = Objects.requireNonNull(tokenSet, "tokenSet");
        profile = source.profile();
        density = source.density();
        colorTokens = source.colorTokens();
        typographyTokens = source.typographyTokens();
        shapeTokens = source.shapeTokens();
        elevationTokens = source.elevationTokens();
        motionTokens = source.motionTokens();
        stateLayerTokens = source.stateLayerTokens();
        componentTokens = source.componentTokens();
    }

    /// Returns the immutable profile for the token set being built.
    ///
    /// @return the Material token profile
    public M3Profile profile() {
        return profile;
    }

    /// Returns the immutable density for the token set being built.
    ///
    /// @return the component density
    public M3Density density() {
        return density;
    }

    /// Replaces the color token group.
    ///
    /// @param colorTokens the replacement color tokens
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3TokenSetBuilder colorTokens(M3ColorTokens colorTokens) {
        this.colorTokens = Objects.requireNonNull(colorTokens, "colorTokens");
        return this;
    }

    /// Replaces the typography token group.
    ///
    /// @param typographyTokens the replacement typography tokens
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3TokenSetBuilder typographyTokens(M3TypographyTokens typographyTokens) {
        this.typographyTokens = Objects.requireNonNull(typographyTokens, "typographyTokens");
        return this;
    }

    /// Replaces the shape token group.
    ///
    /// Derived component tokens will use this group unless an explicit component-token override is present.
    ///
    /// @param shapeTokens the replacement shape tokens
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3TokenSetBuilder shapeTokens(M3ShapeTokens shapeTokens) {
        this.shapeTokens = Objects.requireNonNull(shapeTokens, "shapeTokens");
        return this;
    }

    /// Replaces the elevation token group.
    ///
    /// @param elevationTokens the replacement elevation tokens
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3TokenSetBuilder elevationTokens(M3ElevationTokens elevationTokens) {
        this.elevationTokens = Objects.requireNonNull(elevationTokens, "elevationTokens");
        return this;
    }

    /// Replaces the motion token group.
    ///
    /// @param motionTokens the replacement motion tokens
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3TokenSetBuilder motionTokens(M3MotionTokens motionTokens) {
        this.motionTokens = Objects.requireNonNull(motionTokens, "motionTokens");
        return this;
    }

    /// Replaces the state-layer token group.
    ///
    /// @param stateLayerTokens the replacement state-layer tokens
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3TokenSetBuilder stateLayerTokens(M3StateLayerTokens stateLayerTokens) {
        this.stateLayerTokens = Objects.requireNonNull(stateLayerTokens, "stateLayerTokens");
        return this;
    }

    /// Replaces the component token group.
    ///
    /// @param componentTokens the replacement component tokens
    /// @return this builder
    /// @throws NullPointerException if any required argument is `null`
    public M3TokenSetBuilder componentTokens(M3ComponentTokens componentTokens) {
        this.componentTokens = Objects.requireNonNull(componentTokens, "componentTokens");
        return this;
    }

    /// Clears an explicit component-token override so it is derived at build time.
    ///
    /// @return this builder
    public M3TokenSetBuilder deriveComponentTokens() {
        componentTokens = null;
        return this;
    }

    /// Creates an immutable token set from the current builder state.
    ///
    /// @return the built token set
    public M3TokenSet build() {
        M3ComponentTokens resolvedComponentTokens = componentTokens == null
                ? M3ComponentTokens.builder(profile, shapeTokens, density).build()
                : componentTokens;
        return new M3TokenSetImpl(
                profile,
                density,
                colorTokens,
                typographyTokens,
                shapeTokens,
                elevationTokens,
                motionTokens,
                stateLayerTokens,
                resolvedComponentTokens
        );
    }
}
