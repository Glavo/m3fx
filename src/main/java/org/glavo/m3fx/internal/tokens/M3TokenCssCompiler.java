// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.tokens;

import javafx.scene.paint.Color;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.tokens.*;
import org.glavo.monetfx.ColorRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;
import java.util.Objects;

/// Compiles global Material token groups into the JavaFX CSS consumed by M3FX skins.
@NotNullByDefault
public final class M3TokenCssCompiler {
    /// The CSS prefix used for Monet color roles.
    private static final String MONET_COLOR_PREFIX = "-monet";

    /// The CSS prefix used for Material color roles.
    private static final String MATERIAL_COLOR_PREFIX = "-m3-color";

    /// Prevents utility class instantiation.
    private M3TokenCssCompiler() {
    }

    /// Converts the color tokens into inline JavaFX CSS declarations.
    ///
    /// @param tokens the color token set to compile
    /// @return inline JavaFX CSS declarations for all supported color roles
    /// @throws NullPointerException if `tokens` is `null`
    public static String styleDeclarations(M3ColorTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        for (ColorRole role : tokens.roles()) {
            String color = toRgb(tokens.get(role));
            append(builder, role.getVariableName(MONET_COLOR_PREFIX), color);
            append(builder, role.getVariableName(MATERIAL_COLOR_PREFIX), color);
        }
        return builder.toString().trim();
    }

    /// Converts a color into a JavaFX CSS rgb value.
    ///
    /// @param color the JavaFX color to convert
    /// @return a JavaFX CSS `rgb(r,g,b)` color value
    private static String toRgb(Color color) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return "rgb(" + red + "," + green + "," + blue + ")";
    }

    /// Converts typography tokens into inline JavaFX CSS declarations.
    ///
    /// @param tokens the typography token set to compile
    /// @return inline JavaFX CSS declarations for every Material type scale
    /// @throws NullPointerException if `tokens` is `null`
    public static String styleDeclarations(M3TypographyTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        append(builder, "display-large", tokens.displayLarge());
        append(builder, "display-medium", tokens.displayMedium());
        append(builder, "display-small", tokens.displaySmall());
        append(builder, "headline-large", tokens.headlineLarge());
        append(builder, "headline-medium", tokens.headlineMedium());
        append(builder, "headline-small", tokens.headlineSmall());
        append(builder, "title-large", tokens.titleLarge());
        append(builder, "title-medium", tokens.titleMedium());
        append(builder, "title-small", tokens.titleSmall());
        append(builder, "label-large", tokens.labelLarge());
        append(builder, "label-medium", tokens.labelMedium());
        append(builder, "label-small", tokens.labelSmall());
        append(builder, "body-large", tokens.bodyLarge());
        append(builder, "body-medium", tokens.bodyMedium());
        append(builder, "body-small", tokens.bodySmall());
        return builder.toString().trim();
    }

    /// Converts typography tokens into JavaFX CSS rules for M3FX text controls.
    ///
    /// @param tokens the typography token set to compile
    /// @return JavaFX CSS rules for the M3FX type-scale style classes
    /// @throws NullPointerException if `tokens` is `null`
    public static String controlStyleRules(M3TypographyTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        appendRule(builder, ".m3-display-large-text", tokens.displayLarge());
        appendRule(builder, ".m3-display-medium-text", tokens.displayMedium());
        appendRule(builder, ".m3-display-small-text", tokens.displaySmall());
        appendRule(builder, ".m3-headline-large-text", tokens.headlineLarge());
        appendRule(builder, ".m3-headline-medium-text", tokens.headlineMedium());
        appendRule(builder, ".m3-headline-small-text", tokens.headlineSmall());
        appendRule(builder, ".m3-title-large-text", tokens.titleLarge());
        appendRule(builder, ".m3-title-medium-text", tokens.titleMedium());
        appendRule(builder, ".m3-title-small-text", tokens.titleSmall());
        appendRule(builder, ".m3-label-large-text", tokens.labelLarge());
        appendRule(builder, ".m3-label-medium-text", tokens.labelMedium());
        appendRule(builder, ".m3-label-small-text", tokens.labelSmall());
        appendRule(builder, ".m3-body-large-text", tokens.bodyLarge());
        appendRule(builder, ".m3-body-medium-text", tokens.bodyMedium());
        appendRule(builder, ".m3-body-small-text", tokens.bodySmall());
        return builder.toString().stripTrailing();
    }

    /// Appends declarations for a typography token.
    private static void append(StringBuilder builder, String name, M3TextStyle style) {
        append(builder, "-m3-typescale-" + name + "-font-family", "\"" + style.fontFamily() + "\"");
        append(builder, "-m3-typescale-" + name + "-font-size", pixels(style.size()));
        append(builder, "-m3-typescale-" + name + "-line-height", pixels(style.lineHeight()));
        append(builder, "-m3-typescale-" + name + "-font-weight", Integer.toString(style.weight()));
        append(builder, "-m3-typescale-" + name + "-tracking", pixels(style.tracking()));
    }

    /// Appends a control CSS rule for a typography token.
    private static void appendRule(StringBuilder builder, String selector, M3TextStyle style) {
        builder.append(selector).append(" {\n");
        appendDeclaration(builder, "-m3-typography-font-family", "\"" + style.fontFamily() + "\"");
        appendDeclaration(builder, "-m3-typography-font-size", pixels(style.size()));
        appendDeclaration(builder, "-m3-typography-line-height", pixels(style.lineHeight()));
        appendDeclaration(builder, "-m3-typography-font-weight", Integer.toString(style.weight()));
        appendDeclaration(builder, "-m3-typography-tracking", pixels(style.tracking()));
        appendDeclaration(builder, "-fx-font-family", "\"" + style.fontFamily() + "\"");
        appendDeclaration(builder, "-fx-font-size", pixels(style.size()));
        appendDeclaration(builder, "-fx-font-weight", Integer.toString(style.weight()));
        builder.append("}\n\n");
    }

    /// Appends one declaration inside a control CSS rule.
    private static void appendDeclaration(StringBuilder builder, String name, String value) {
        builder.append("    ").append(name).append(": ").append(value).append(";\n");
    }

    /// Converts shape tokens into inline JavaFX CSS declarations.
    ///
    /// @param tokens the shape token set to compile
    /// @return inline JavaFX CSS declarations for this shape token set
    /// @throws NullPointerException if `tokens` is `null`
    public static String styleDeclarations(M3ShapeTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        append(builder, "-m3-shape-corner-none", pixels(tokens.none()));
        append(builder, "-m3-shape-corner-extra-small", pixels(tokens.extraSmall()));
        append(builder, "-m3-shape-corner-small", pixels(tokens.small()));
        append(builder, "-m3-shape-corner-medium", pixels(tokens.medium()));
        append(builder, "-m3-shape-corner-large", pixels(tokens.large()));
        append(builder, "-m3-shape-corner-large-increased", pixels(tokens.largeIncreased()));
        append(builder, "-m3-shape-corner-extra-large", pixels(tokens.extraLarge()));
        append(builder, "-m3-shape-corner-extra-large-increased", pixels(tokens.extraLargeIncreased()));
        append(builder, "-m3-shape-corner-extra-extra-large", pixels(tokens.extraExtraLarge()));
        append(builder, "-m3-shape-corner-full", pixels(tokens.full()));
        return builder.toString().trim();
    }

    /// Converts elevation tokens into inline JavaFX CSS declarations.
    ///
    /// @param tokens the elevation token set to compile
    /// @return inline JavaFX CSS declarations for these elevation tokens
    /// @throws NullPointerException if `tokens` is `null`
    public static String styleDeclarations(M3ElevationTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        append(builder, "-m3-elevation-level0", pixels(tokens.level0()));
        append(builder, "-m3-elevation-level1", pixels(tokens.level1()));
        append(builder, "-m3-elevation-level2", pixels(tokens.level2()));
        append(builder, "-m3-elevation-level3", pixels(tokens.level3()));
        append(builder, "-m3-elevation-level4", pixels(tokens.level4()));
        append(builder, "-m3-elevation-level5", pixels(tokens.level5()));
        return builder.toString().trim();
    }

    /// Converts elevation tokens into JavaFX CSS rules for M3FX controls.
    ///
    /// @param tokens the elevation token set to compile
    /// @return JavaFX CSS rules for M3FX controls using these elevation tokens
    /// @throws NullPointerException if `tokens` is `null`
    public static String controlStyleRules(M3ElevationTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        appendShadowRule(builder, ".m3-elevated-button", tokens.level3(), tokens.level1());
        appendShadowRule(builder, ".m3-elevated-button:hover", tokens.level4(), tokens.level2());
        appendShadowRule(builder, ".m3-elevated-button:focus-visible, .m3-elevated-button:armed, .m3-elevated-button:pressed", tokens.level3(), tokens.level1());
        appendShadowRule(builder, ".m3-elevated-chip", tokens.level2(), tokens.level1());
        appendShadowRule(builder, ".m3-elevated-chip:hover", tokens.level3(), tokens.level2());
        appendShadowRule(builder, ".m3-elevated-chip:focus-visible, .m3-elevated-chip:armed, .m3-elevated-chip:pressed", tokens.level2(), tokens.level1());
        appendShadowRule(builder, ".m3-fab", tokens.level4(), tokens.level2());
        appendShadowRule(builder, ".m3-fab:hover", tokens.level5(), tokens.level3());
        appendShadowRule(builder, ".m3-fab:focus-visible, .m3-fab:armed, .m3-fab:pressed", tokens.level4(), tokens.level2());
        appendEffectResetRule(builder, ".m3-elevated-button:disabled, .m3-elevated-chip:disabled, .m3-fab:disabled");
        appendShadowRule(builder, ".m3-elevated-card .m3-card-container", tokens.level1(), tokens.level1());
        appendShadowRule(builder, ".m3-elevated-card:actionable:hover .m3-card-container", tokens.level2(), Math.max(tokens.level1(), tokens.level2() - tokens.level1()));
        appendShadowRule(builder, ".m3-elevated-card:actionable:focus-visible .m3-card-container, .m3-elevated-card:actionable:armed .m3-card-container, .m3-elevated-card:actionable:pressed .m3-card-container", tokens.level1(), tokens.level1());
        appendShadowRule(builder, ".m3-elevated-card:dragged .m3-card-container", tokens.level4(), Math.max(tokens.level1(), tokens.level4() - tokens.level3()));
        appendShadowRule(builder, ".m3-filled-card:actionable:hover .m3-card-container", tokens.level1(), tokens.level1());
        appendEffectResetRule(builder, ".m3-filled-card:actionable:focus-visible .m3-card-container, .m3-filled-card:actionable:armed .m3-card-container, .m3-filled-card:actionable:pressed .m3-card-container");
        appendShadowRule(builder, ".m3-filled-card:dragged .m3-card-container", tokens.level3(), Math.max(tokens.level1(), tokens.level3() - tokens.level2()));
        appendShadowRule(builder, ".m3-outlined-card:actionable:hover .m3-card-container", tokens.level1(), tokens.level1());
        appendEffectResetRule(builder, ".m3-outlined-card:actionable:focus-visible .m3-card-container, .m3-outlined-card:actionable:armed .m3-card-container, .m3-outlined-card:actionable:pressed .m3-card-container");
        appendShadowRule(builder, ".m3-outlined-card:dragged .m3-card-container", tokens.level3(), Math.max(tokens.level1(), tokens.level3() - tokens.level2()));
        appendShadowRule(builder, ".m3-card.m3-elevated-card:disabled .m3-card-container", tokens.level1(), tokens.level1());
        appendEffectResetRule(
                builder,
                ".m3-card.m3-filled-card:disabled .m3-card-container, "
                        + ".m3-card.m3-outlined-card:disabled .m3-card-container"
        );
        appendEffectResetRule(builder, ".m3-side-sheet.m3-standard-sheet");
        appendShadowRule(builder, ".m3-side-sheet.m3-modal-sheet", tokens.level1(), tokens.level1());
        appendShadowRule(builder, ".m3-bottom-sheet", tokens.level1(), tokens.level1());
        appendShadowRule(builder, ".m3-surface-elevation-level1 .m3-surface-container", tokens.level1(), tokens.level1());
        appendShadowRule(builder, ".m3-surface-elevation-level2 .m3-surface-container", tokens.level2(), Math.max(tokens.level1(), tokens.level2() - tokens.level1()));
        appendShadowRule(builder, ".m3-surface-elevation-level3 .m3-surface-container", tokens.level3(), Math.max(tokens.level1(), tokens.level3() - tokens.level2()));
        appendShadowRule(builder, ".m3-surface-elevation-level4 .m3-surface-container", tokens.level4(), Math.max(tokens.level1(), tokens.level4() - tokens.level3()));
        appendShadowRule(builder, ".m3-surface-elevation-level5 .m3-surface-container", tokens.level5(), Math.max(tokens.level1(), tokens.level5() - tokens.level4()));
        appendShadowRule(builder, ".m3-menu, .m3-rich-tooltip-container", tokens.level2(), Math.max(tokens.level1(), tokens.level2() - tokens.level1()));
        appendShadowRule(builder, ".m3-top-app-bar:scrolled-under", tokens.level2(), Math.max(tokens.level1(), tokens.level2() - tokens.level1()));
        appendShadowRule(builder, ".m3-toolbar-floating", tokens.level3(), Math.max(tokens.level1(), tokens.level3() - tokens.level2()));
        appendShadowRule(builder, ".m3-dialog-pane, .m3-snackbar-container", tokens.level3(), Math.max(tokens.level1(), tokens.level3() - tokens.level2()));
        return builder.toString().stripTrailing();
    }

    /// Appends an effect reset CSS rule.
    private static void appendEffectResetRule(StringBuilder builder, String selector) {
        builder.append(selector)
                .append(" {\n    -fx-effect: null;\n}\n\n");
    }

    /// Appends a dropshadow CSS rule.
    private static void appendShadowRule(StringBuilder builder, String selector, double radius, double offsetY) {
        builder.append(selector)
                .append(" {\n    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), ")
                .append(format(radius))
                .append(", 0.18, 0, ")
                .append(format(offsetY))
                .append(");\n}\n\n");
    }

    /// Converts motion tokens into inline JavaFX CSS declarations.
    ///
    /// @param tokens the motion token set to compile
    /// @return inline JavaFX CSS declarations for these motion tokens
    /// @throws NullPointerException if `tokens` is `null`
    public static String styleDeclarations(M3MotionTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        appendCoarseDurationStyleDeclarations(builder, tokens);
        appendSchemeStyleDeclarations(builder, tokens);
        appendBehaviorStyleDeclarations(builder, tokens);
        append(builder, "-m3-motion-duration-short1", tokens.short1() + "ms");
        append(builder, "-m3-motion-duration-short2", tokens.short2() + "ms");
        append(builder, "-m3-motion-duration-short3", tokens.short3() + "ms");
        append(builder, "-m3-motion-duration-short4", tokens.short4() + "ms");
        append(builder, "-m3-motion-duration-medium1", tokens.medium1() + "ms");
        append(builder, "-m3-motion-duration-medium2", tokens.medium2() + "ms");
        append(builder, "-m3-motion-duration-medium3", tokens.medium3() + "ms");
        append(builder, "-m3-motion-duration-medium4", tokens.medium4() + "ms");
        append(builder, "-m3-motion-duration-long1", tokens.long1() + "ms");
        append(builder, "-m3-motion-duration-long2", tokens.long2() + "ms");
        append(builder, "-m3-motion-duration-long3", tokens.long3() + "ms");
        append(builder, "-m3-motion-duration-long4", tokens.long4() + "ms");
        append(builder, "-m3-motion-duration-extra-long1", tokens.extraLong1() + "ms");
        append(builder, "-m3-motion-duration-extra-long2", tokens.extraLong2() + "ms");
        append(builder, "-m3-motion-duration-extra-long3", tokens.extraLong3() + "ms");
        append(builder, "-m3-motion-duration-extra-long4", tokens.extraLong4() + "ms");
        return builder.toString().trim();
    }

    /// Appends coarse duration alias declarations.
    private static void appendCoarseDurationStyleDeclarations(StringBuilder builder, M3MotionTokens tokens) {
        append(builder, "-m3-motion-duration-short", tokens.short2() + "ms");
        append(builder, "-m3-motion-duration-medium", tokens.medium1() + "ms");
        append(builder, "-m3-motion-duration-long", tokens.long2() + "ms");
    }

    /// Appends semantic motion scheme declarations.
    private static void appendSchemeStyleDeclarations(StringBuilder builder, M3MotionTokens tokens) {
        appendSpec(builder, "fast-effects", tokens.fastEffects());
        appendSpec(builder, "default-effects", tokens.defaultEffects());
        appendSpec(builder, "slow-effects", tokens.slowEffects());
        appendSpec(builder, "fast-spatial", tokens.fastSpatial());
        appendSpec(builder, "default-spatial", tokens.defaultSpatial());
        appendSpec(builder, "slow-spatial", tokens.slowSpatial());
    }

    /// Appends interaction timing declarations.
    private static void appendBehaviorStyleDeclarations(StringBuilder builder, M3MotionTokens tokens) {
        M3MotionBehavior behavior = tokens.behavior();
        appendDuration(builder, "-m3-motion-tooltip-show-delay", behavior.tooltipShowDelay());
        appendDuration(builder, "-m3-motion-tooltip-hide-delay", behavior.tooltipHideDelay());
        appendDuration(builder, "-m3-motion-tooltip-show-duration", behavior.tooltipShowDuration());
        appendDuration(builder, "-m3-motion-rich-tooltip-show-duration", behavior.richTooltipShowDuration());
        appendDuration(builder, "-m3-motion-sub-menu-hover-open-delay", behavior.subMenuHoverOpenDelay());
        appendDuration(builder, "-m3-motion-sub-menu-hover-close-delay", behavior.subMenuHoverCloseDelay());
        appendDuration(
                builder,
                "-m3-motion-linear-progress-indeterminate-cycle-duration",
                behavior.linearProgressIndeterminateCycleDuration()
        );
        appendDuration(
                builder,
                "-m3-motion-circular-progress-indeterminate-cycle-duration",
                behavior.circularProgressIndeterminateCycleDuration()
        );
    }

    /// Appends declarations for one semantic motion spec.
    private static void appendSpec(StringBuilder builder, String name, M3MotionSpec spec) {
        appendDuration(builder, "-m3-motion-" + name + "-duration", spec.duration());
        append(builder, "-m3-motion-" + name + "-easing", spec.easing().tokenName());
    }

    /// Appends one duration declaration in milliseconds.
    private static void appendDuration(StringBuilder builder, String name, javafx.util.Duration duration) {
        append(builder, name, format(duration.toMillis()) + "ms");
    }

    /// Converts the state tokens into root-level JavaFX CSS declarations.
    ///
    /// @param tokens the state-layer token set to compile
    /// @return root-level JavaFX CSS declarations for this state layer token set
    /// @throws NullPointerException if `tokens` is `null`
    public static String styleDeclarations(M3StateLayerTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        return "-m3-state-hover-opacity: " + format(tokens.hoverOpacity()) + "; "
                + "-m3-state-focus-opacity: " + format(tokens.focusOpacity()) + "; "
                + "-m3-state-pressed-opacity: " + format(tokens.pressedOpacity()) + "; "
                + "-m3-state-dragged-opacity: " + format(tokens.draggedOpacity()) + "; "
                + "-m3-state-disabled-container-opacity: " + format(tokens.disabledContainerOpacity()) + "; "
                + "-m3-state-disabled-content-opacity: " + format(tokens.disabledContentOpacity()) + "; "
                + "-m3-state-focus-indicator-color: -m3-color-secondary; "
                + "-m3-state-focus-indicator-thickness: " + pixels(tokens.focusIndicatorThickness()) + "; "
                + "-m3-state-focus-indicator-outer-offset: " + pixels(tokens.focusIndicatorOuterOffset()) + "; "
                + "-m3-state-focus-indicator-inner-offset: " + pixels(tokens.focusIndicatorInnerOffset()) + ";";
    }

    /// Converts state layer tokens into JavaFX CSS rules for M3FX controls.
    ///
    /// @param tokens the state-layer token set to compile
    /// @return JavaFX CSS rules for controls that render interaction state layers
    /// @throws NullPointerException if `tokens` is `null`
    public static String controlStyleRules(M3StateLayerTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        StringBuilder builder = new StringBuilder();
        appendOpacityRule(builder, hoverStateSelectors(), tokens.hoverOpacity());
        appendOpacityRule(builder, focusStateSelectors(), tokens.focusOpacity());
        appendOpacityRule(builder, pressedStateSelectors(), tokens.pressedOpacity());
        appendOpacityRule(builder, ".m3-card:dragged .m3-state-layer", tokens.draggedOpacity());
        return builder.toString().stripTrailing();
    }

    /// Returns selectors for controls that expose hover state layer feedback.
    private static String hoverStateSelectors() {
        return ".m3-button:hover .m3-state-layer, .m3-chip:hover .m3-state-layer, "
                + ".m3-icon-button:hover .m3-state-layer, .m3-icon-toggle-button:hover .m3-state-layer, "
                + ".m3-fab:hover .m3-state-layer, "
                + ".m3-segmented-button:hover .m3-state-layer, .m3-checkbox:hover .m3-state-layer, "
                + ".m3-radio-button:hover .m3-state-layer, .m3-switch:hover .m3-state-layer, "
                + ".m3-tab:hover .m3-state-layer, "
                + ".m3-navigation-item:hover .m3-state-layer, "
                + ".m3-list-item:hover .m3-state-layer, "
                + ".m3-validation-summary-item:hover .m3-state-layer, "
                + ".m3-search-bar:hover .m3-state-layer, "
                + ".m3-card:actionable:hover .m3-state-layer";
    }

    /// Returns selectors for controls that expose focus state layer feedback.
    private static String focusStateSelectors() {
        return ".m3-button:focus-visible .m3-state-layer, .m3-chip:focus-visible .m3-state-layer, "
                + ".m3-icon-button:focus-visible .m3-state-layer, "
                + ".m3-icon-toggle-button:focus-visible .m3-state-layer, .m3-fab:focus-visible .m3-state-layer, "
                + ".m3-segmented-button:focus-visible .m3-state-layer, .m3-checkbox:focus-visible .m3-state-layer, "
                + ".m3-radio-button:focus-visible .m3-state-layer, .m3-switch:focus-visible .m3-state-layer, "
                + ".m3-tab:focus-visible .m3-state-layer, "
                + ".m3-navigation-item:focus-visible .m3-state-layer, "
                + ".m3-list-item:focus-visible .m3-state-layer, "
                + ".m3-validation-summary-item:focus-visible .m3-state-layer, "
                + ".m3-search-bar:focus-visible .m3-state-layer, "
                + ".m3-card:actionable:focus-visible .m3-state-layer";
    }

    /// Returns selectors for controls that expose pressed state layer feedback.
    private static String pressedStateSelectors() {
        return ".m3-button:pressed .m3-state-layer, .m3-chip:pressed .m3-state-layer, "
                + ".m3-icon-button:pressed .m3-state-layer, .m3-icon-toggle-button:pressed .m3-state-layer, "
                + ".m3-fab:pressed .m3-state-layer, "
                + ".m3-segmented-button:pressed .m3-state-layer, .m3-checkbox:pressed .m3-state-layer, "
                + ".m3-radio-button:pressed .m3-state-layer, .m3-switch:pressed .m3-state-layer, "
                + ".m3-tab:pressed .m3-state-layer, "
                + ".m3-navigation-item:pressed .m3-state-layer, "
                + ".m3-list-item:pressed .m3-state-layer, "
                + ".m3-validation-summary-item:pressed .m3-state-layer, "
                + ".m3-search-bar:pressed .m3-state-layer, "
                + ".m3-card:actionable:armed .m3-state-layer, "
                + ".m3-card:actionable:pressed .m3-state-layer";
    }

    /// Appends an opacity CSS rule.
    private static void appendOpacityRule(StringBuilder builder, String selector, double opacity) {
        builder.append(selector)
                .append(" {\n    -fx-opacity: ")
                .append(format(opacity))
                .append(";\n}\n\n");
    }


    /// Formats a decimal number with stable locale-independent output.
    private static String format(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /// Formats a decimal number as a pixel value.
    private static String pixels(double value) {
        return format(value) + "px";
    }

    /// Appends an inline CSS declaration.
    private static void append(StringBuilder builder, String name, String value) {
        builder.append(name).append(": ").append(value).append("; ");
    }
}
