// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3StateLayerTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 state layer opacity tokens.
@NotNullByDefault
public sealed interface M3StateLayerTokens permits M3StateLayerTokensImpl {
    /// Returns the hover state layer opacity.
    double hoverOpacity();

    /// Returns the focus state layer opacity.
    double focusOpacity();

    /// Returns the pressed state layer opacity.
    double pressedOpacity();

    /// Returns the dragged state layer opacity.
    double draggedOpacity();

    /// Returns the disabled container opacity.
    double disabledContainerOpacity();

    /// Returns the disabled content opacity.
    double disabledContentOpacity();

    /// Returns baseline Material Design 3 state layer tokens.
    static M3StateLayerTokens baseline() {
        return new M3StateLayerTokensImpl(0.08, 0.10, 0.10, 0.16, 0.12, 0.38);
    }

    /// Converts the state tokens into root-level JavaFX CSS declarations.
    default String toStyleDeclarations() {
        return "-m3-state-hover-opacity: " + M3TokenCss.format(hoverOpacity()) + "; "
                + "-m3-state-focus-opacity: " + M3TokenCss.format(focusOpacity()) + "; "
                + "-m3-state-pressed-opacity: " + M3TokenCss.format(pressedOpacity()) + "; "
                + "-m3-state-dragged-opacity: " + M3TokenCss.format(draggedOpacity()) + "; "
                + "-m3-state-disabled-container-opacity: " + M3TokenCss.format(disabledContainerOpacity()) + "; "
                + "-m3-state-disabled-content-opacity: " + M3TokenCss.format(disabledContentOpacity()) + ";";
    }

    /// Converts state layer tokens into JavaFX CSS rules for m3fx controls.
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendOpacityRule(
                builder,
                ".m3-button:hover, .m3-chip:hover, .m3-icon-button:hover, .m3-fab:hover, .m3-segmented-button:hover",
                1.0 - hoverOpacity()
        );
        appendOpacityRule(
                builder,
                ".m3-button:focused, .m3-chip:focused, .m3-icon-button:focused, .m3-fab:focused, "
                        + ".m3-segmented-button:focused",
                1.0 - focusOpacity()
        );
        appendOpacityRule(
                builder,
                ".m3-button:pressed, .m3-chip:pressed, .m3-icon-button:pressed, .m3-fab:pressed, "
                        + ".m3-segmented-button:pressed",
                1.0 - pressedOpacity()
        );
        appendOpacityRule(
                builder,
                ".m3-button:disabled, .m3-chip:disabled, .m3-icon-button:disabled, .m3-fab:disabled, "
                        + ".m3-segmented-button:disabled, "
                        + ".m3-text-field:disabled, .m3-password-field:disabled, "
                        + ".m3-checkbox:disabled, .m3-radio-button:disabled, .m3-switch:disabled, .m3-slider:disabled, "
                        + ".m3-progress-bar:disabled, .m3-progress-indicator:disabled",
                disabledContentOpacity()
        );
        return builder.toString().stripTrailing();
    }

    /// Appends an opacity CSS rule.
    private static void appendOpacityRule(StringBuilder builder, String selector, double opacity) {
        builder.append(selector)
                .append(" {\n    -fx-opacity: ")
                .append(M3TokenCss.format(opacity))
                .append(";\n}\n\n");
    }
}
