package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 state layer opacity tokens.
@NotNullByDefault
public record M3StateLayerTokens(
        /// The hover state layer opacity.
        double hoverOpacity,

        /// The focus state layer opacity.
        double focusOpacity,

        /// The pressed state layer opacity.
        double pressedOpacity,

        /// The dragged state layer opacity.
        double draggedOpacity,

        /// The disabled container opacity.
        double disabledContainerOpacity,

        /// The disabled content opacity.
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
