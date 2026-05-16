package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ColorTokens;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of {@link M3ColorTokens}.
///
/// @param colorScheme the MonetFX color scheme used by this token set
@NotNullByDefault
public record M3ColorTokensImpl(ColorScheme colorScheme) implements M3ColorTokens {
    /// Creates color tokens.
    public M3ColorTokensImpl {
        Objects.requireNonNull(colorScheme, "colorScheme");
    }
}
