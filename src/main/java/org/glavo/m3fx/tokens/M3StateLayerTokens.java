// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3StateLayerTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

/// Holds Material Design 3 interaction state tokens.
///
/// State tokens define the opacity used when controls render hover, focus, pressed, dragged, and disabled
/// feedback. They also expose the keyboard focus indicator metrics referenced by component tokens. Skins combine
/// these values with the active color roles so interaction feedback remains theme-aware.
///
/// See [Material Design interaction states](https://m3.material.io/foundations/interaction/states/overview).
@NotNullByDefault
public sealed interface M3StateLayerTokens permits M3StateLayerTokensImpl {
    /// Returns the hover state layer opacity.
    ///
    /// @return the hover state layer opacity
    double hoverOpacity();

    /// Returns the focus state layer opacity.
    ///
    /// @return the focus state layer opacity
    double focusOpacity();

    /// Returns the pressed state layer opacity.
    ///
    /// @return the pressed state layer opacity
    double pressedOpacity();

    /// Returns the dragged state layer opacity.
    ///
    /// @return the dragged state layer opacity
    double draggedOpacity();

    /// Returns the disabled container opacity.
    ///
    /// @return the disabled container opacity
    double disabledContainerOpacity();

    /// Returns the disabled content opacity.
    ///
    /// @return the disabled content opacity
    double disabledContentOpacity();

    /// Returns the keyboard focus indicator thickness.
    ///
    /// @return the keyboard focus indicator thickness in pixels
    double focusIndicatorThickness();

    /// Returns the keyboard focus indicator outer offset.
    ///
    /// @return the keyboard focus indicator outer offset in pixels
    double focusIndicatorOuterOffset();

    /// Returns the keyboard focus indicator inner offset.
    ///
    /// @return the keyboard focus indicator inner offset in pixels
    double focusIndicatorInnerOffset();

    /// Creates state layer opacity tokens.
    ///
    /// @param hoverOpacity the hover state layer opacity
    /// @param focusOpacity the focus state layer opacity
    /// @param pressedOpacity the pressed state layer opacity
    /// @param draggedOpacity the dragged state layer opacity
    /// @param disabledContainerOpacity the disabled container opacity
    /// @param disabledContentOpacity the disabled content opacity
    /// @return the created state layer token set
    static M3StateLayerTokens create(
            double hoverOpacity,
            double focusOpacity,
            double pressedOpacity,
            double draggedOpacity,
            double disabledContainerOpacity,
            double disabledContentOpacity
    ) {
        return create(
                hoverOpacity,
                focusOpacity,
                pressedOpacity,
                draggedOpacity,
                disabledContainerOpacity,
                disabledContentOpacity,
                3.0,
                2.0,
                -2.0
        );
    }

    /// Creates state tokens.
    ///
    /// @param hoverOpacity the hover state layer opacity
    /// @param focusOpacity the focus state layer opacity
    /// @param pressedOpacity the pressed state layer opacity
    /// @param draggedOpacity the dragged state layer opacity
    /// @param disabledContainerOpacity the disabled container opacity
    /// @param disabledContentOpacity the disabled content opacity
    /// @param focusIndicatorThickness the keyboard focus indicator thickness in pixels
    /// @param focusIndicatorOuterOffset the keyboard focus indicator outer offset in pixels
    /// @param focusIndicatorInnerOffset the keyboard focus indicator inner offset in pixels
    /// @return the created state token set
    static M3StateLayerTokens create(
            double hoverOpacity,
            double focusOpacity,
            double pressedOpacity,
            double draggedOpacity,
            double disabledContainerOpacity,
            double disabledContentOpacity,
            double focusIndicatorThickness,
            double focusIndicatorOuterOffset,
            double focusIndicatorInnerOffset
    ) {
        return new M3StateLayerTokensImpl(
                hoverOpacity,
                focusOpacity,
                pressedOpacity,
                draggedOpacity,
                disabledContainerOpacity,
                disabledContentOpacity,
                focusIndicatorThickness,
                focusIndicatorOuterOffset,
                focusIndicatorInnerOffset
        );
    }

    /// Returns baseline Material Design 3 state layer tokens.
    ///
    /// @return baseline Material Design 3 state layer tokens
    static M3StateLayerTokens baseline() {
        return create(0.08, 0.10, 0.10, 0.16, 0.12, 0.38);
    }

    /// Converts the state tokens into root-level JavaFX CSS declarations.
    ///
    /// @return root-level JavaFX CSS declarations for this state layer token set
    default String toStyleDeclarations() {
        return "-m3-state-hover-opacity: " + M3TokenCss.format(hoverOpacity()) + "; "
                + "-m3-state-focus-opacity: " + M3TokenCss.format(focusOpacity()) + "; "
                + "-m3-state-pressed-opacity: " + M3TokenCss.format(pressedOpacity()) + "; "
                + "-m3-state-dragged-opacity: " + M3TokenCss.format(draggedOpacity()) + "; "
                + "-m3-state-disabled-container-opacity: " + M3TokenCss.format(disabledContainerOpacity()) + "; "
                + "-m3-state-disabled-content-opacity: " + M3TokenCss.format(disabledContentOpacity()) + "; "
                + "-m3-state-focus-indicator-color: -m3-color-secondary; "
                + "-m3-state-focus-indicator-thickness: " + M3TokenCss.pixels(focusIndicatorThickness()) + "; "
                + "-m3-state-focus-indicator-outer-offset: " + M3TokenCss.pixels(focusIndicatorOuterOffset()) + "; "
                + "-m3-state-focus-indicator-inner-offset: " + M3TokenCss.pixels(focusIndicatorInnerOffset()) + ";";
    }

    /// Converts state layer tokens into JavaFX CSS rules for M3FX controls.
    ///
    /// @return JavaFX CSS rules for controls that render state layers or disabled opacity
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendOpacityRule(builder, hoverStateSelectors(), hoverOpacity());
        appendOpacityRule(builder, focusStateSelectors(), focusOpacity());
        appendOpacityRule(builder, pressedStateSelectors(), pressedOpacity());
        appendOpacityRule(builder, disabledStateSelectors(), disabledContentOpacity());
        return builder.toString().stripTrailing();
    }

    /// Returns selectors for controls that expose hover state layer feedback.
    private static String hoverStateSelectors() {
        return ".m3-button:hover .m3-state-layer, .m3-chip:hover .m3-state-layer, "
                + ".m3-icon-button:hover .m3-state-layer, .m3-icon-toggle-button:hover .m3-state-layer, "
                + ".m3-fab:hover .m3-state-layer, "
                + ".m3-segmented-button:hover .m3-state-layer, .m3-checkbox:hover .m3-state-layer, "
                + ".m3-radio-button:hover .m3-state-layer, .m3-switch:hover .m3-state-layer, "
                + ".m3-slider:hover .m3-state-layer, .m3-tab:hover .m3-state-layer, "
                + ".m3-navigation-item:hover .m3-state-layer, "
                + ".m3-list-item:hover .m3-state-layer, "
                + ".m3-validation-summary-item:hover .m3-state-layer, "
                + ".m3-card:hover .m3-state-layer";
    }

    /// Returns selectors for controls that expose focus state layer feedback.
    private static String focusStateSelectors() {
        return ".m3-button:focus-visible .m3-state-layer, .m3-chip:focus-visible .m3-state-layer, "
                + ".m3-icon-button:focus-visible .m3-state-layer, "
                + ".m3-icon-toggle-button:focus-visible .m3-state-layer, .m3-fab:focus-visible .m3-state-layer, "
                + ".m3-segmented-button:focus-visible .m3-state-layer, .m3-checkbox:focus-visible .m3-state-layer, "
                + ".m3-radio-button:focus-visible .m3-state-layer, .m3-switch:focus-visible .m3-state-layer, "
                + ".m3-slider:focus-visible .m3-state-layer, .m3-tab:focus-visible .m3-state-layer, "
                + ".m3-navigation-item:focus-visible .m3-state-layer, "
                + ".m3-list-item:focus-visible .m3-state-layer, "
                + ".m3-validation-summary-item:focus-visible .m3-state-layer, "
                + ".m3-card:focus-visible .m3-state-layer";
    }

    /// Returns selectors for controls that expose pressed state layer feedback.
    private static String pressedStateSelectors() {
        return ".m3-button:pressed .m3-state-layer, .m3-chip:pressed .m3-state-layer, "
                + ".m3-icon-button:pressed .m3-state-layer, .m3-icon-toggle-button:pressed .m3-state-layer, "
                + ".m3-fab:pressed .m3-state-layer, "
                + ".m3-segmented-button:pressed .m3-state-layer, .m3-checkbox:pressed .m3-state-layer, "
                + ".m3-radio-button:pressed .m3-state-layer, .m3-switch:pressed .m3-state-layer, "
                + ".m3-slider:pressed .m3-state-layer, .m3-tab:pressed .m3-state-layer, "
                + ".m3-navigation-item:pressed .m3-state-layer, "
                + ".m3-list-item:pressed .m3-state-layer, "
                + ".m3-validation-summary-item:pressed .m3-state-layer, "
                + ".m3-card:pressed .m3-state-layer";
    }

    /// Returns selectors for controls that expose disabled content opacity.
    private static String disabledStateSelectors() {
        return ".m3-button:disabled, .m3-chip:disabled, .m3-icon-button:disabled, "
                + ".m3-fab:disabled, .m3-icon-toggle-button:disabled, "
                + ".m3-segmented-button:disabled, .m3-text-field:disabled, "
                + ".m3-password-field:disabled, "
                + ".m3-slider:disabled, .m3-tab:disabled, .m3-progress-bar:disabled, .m3-progress-indicator:disabled, "
                + ".m3-navigation-item:disabled, .m3-list-item:disabled, .m3-card:disabled, "
                + ".m3-dialog-pane:disabled, .m3-snackbar:disabled, "
                + ".m3-divider:disabled, .m3-badge:disabled";
    }

    /// Appends an opacity CSS rule.
    private static void appendOpacityRule(StringBuilder builder, String selector, double opacity) {
        builder.append(selector)
                .append(" {\n    -fx-opacity: ")
                .append(M3TokenCss.format(opacity))
                .append(";\n}\n\n");
    }

}
