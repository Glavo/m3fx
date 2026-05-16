// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ColorTokens;
import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.glavo.m3fx.tokens.M3ElevationTokens;
import org.glavo.m3fx.tokens.M3MotionTokens;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3ShapeTokens;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.m3fx.tokens.M3TypographyTokens;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of {@link M3TokenSet}.
///
/// @param profile the profile that produced this token set
/// @param colorTokens the color tokens
/// @param typographyTokens the typography tokens
/// @param shapeTokens the shape tokens
/// @param elevationTokens the elevation tokens
/// @param motionTokens the motion tokens
/// @param stateLayerTokens the state layer tokens
/// @param componentTokens the component tokens
@NotNullByDefault
public record M3TokenSetImpl(
        M3Profile profile,
        M3ColorTokens colorTokens,
        M3TypographyTokens typographyTokens,
        M3ShapeTokens shapeTokens,
        M3ElevationTokens elevationTokens,
        M3MotionTokens motionTokens,
        M3StateLayerTokens stateLayerTokens,
        M3ComponentTokens componentTokens
) implements M3TokenSet {
    /// Creates a token set implementation.
    public M3TokenSetImpl {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(colorTokens, "colorTokens");
        Objects.requireNonNull(typographyTokens, "typographyTokens");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(elevationTokens, "elevationTokens");
        Objects.requireNonNull(motionTokens, "motionTokens");
        Objects.requireNonNull(stateLayerTokens, "stateLayerTokens");
        Objects.requireNonNull(componentTokens, "componentTokens");
    }
}
