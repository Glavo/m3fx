package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.jetbrains.annotations.NotNullByDefault;

/// Default immutable implementation of {@link M3StateLayerTokens}.
///
/// @param hoverOpacity the hover state layer opacity
/// @param focusOpacity the focus state layer opacity
/// @param pressedOpacity the pressed state layer opacity
/// @param draggedOpacity the dragged state layer opacity
/// @param disabledContainerOpacity the disabled container opacity
/// @param disabledContentOpacity the disabled content opacity
@NotNullByDefault
public record M3StateLayerTokensImpl(
        double hoverOpacity,
        double focusOpacity,
        double pressedOpacity,
        double draggedOpacity,
        double disabledContainerOpacity,
        double disabledContentOpacity
) implements M3StateLayerTokens {
    /// Creates state layer tokens.
    public M3StateLayerTokensImpl {
        validate(hoverOpacity, "hoverOpacity");
        validate(focusOpacity, "focusOpacity");
        validate(pressedOpacity, "pressedOpacity");
        validate(draggedOpacity, "draggedOpacity");
        validate(disabledContainerOpacity, "disabledContainerOpacity");
        validate(disabledContentOpacity, "disabledContentOpacity");
    }

    /// Validates an opacity token.
    private static void validate(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
        }
    }
}
