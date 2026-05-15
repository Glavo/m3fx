package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 state layer opacity tokens.
///
/// @param hoverOpacity the hover state layer opacity
/// @param focusOpacity the focus state layer opacity
/// @param pressedOpacity the pressed state layer opacity
/// @param draggedOpacity the dragged state layer opacity
/// @param disabledContainerOpacity the disabled container opacity
/// @param disabledContentOpacity the disabled content opacity
@NotNullByDefault
public record M3StateLayerTokens(
        double hoverOpacity,
        double focusOpacity,
        double pressedOpacity,
        double draggedOpacity,
        double disabledContainerOpacity,
        double disabledContentOpacity
) {
    /// Creates state layer tokens.
    public M3StateLayerTokens {
        validate(hoverOpacity, "hoverOpacity");
        validate(focusOpacity, "focusOpacity");
        validate(pressedOpacity, "pressedOpacity");
        validate(draggedOpacity, "draggedOpacity");
        validate(disabledContainerOpacity, "disabledContainerOpacity");
        validate(disabledContentOpacity, "disabledContentOpacity");
    }

    /// Returns baseline Material Design 3 state layer tokens.
    public static M3StateLayerTokens baseline() {
        return new M3StateLayerTokens(0.08, 0.10, 0.10, 0.16, 0.12, 0.38);
    }

    /// Converts the state tokens into root-level JavaFX CSS declarations.
    public String toStyleDeclarations() {
        return "-m3-state-hover-opacity: " + M3TokenCss.format(hoverOpacity) + "; "
                + "-m3-state-focus-opacity: " + M3TokenCss.format(focusOpacity) + "; "
                + "-m3-state-pressed-opacity: " + M3TokenCss.format(pressedOpacity) + "; "
                + "-m3-state-dragged-opacity: " + M3TokenCss.format(draggedOpacity) + "; "
                + "-m3-state-disabled-container-opacity: " + M3TokenCss.format(disabledContainerOpacity) + "; "
                + "-m3-state-disabled-content-opacity: " + M3TokenCss.format(disabledContentOpacity) + ";";
    }

    /// Validates an opacity token.
    private static void validate(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
        }
    }
}
