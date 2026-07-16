// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of [M3Theme].
///
/// @param tokens the complete token set for this theme
@NotNullByDefault
public record M3ThemeImpl(M3TokenSet tokens) implements M3Theme {
    /// Creates a theme implementation.
    public M3ThemeImpl {
        Objects.requireNonNull(tokens, "tokens");
    }
}
