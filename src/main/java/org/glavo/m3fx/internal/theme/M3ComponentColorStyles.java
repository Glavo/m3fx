// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3ButtonColors;
import org.glavo.m3fx.controls.M3CardColors;
import org.glavo.m3fx.controls.M3SurfaceColors;
import org.glavo.m3fx.internal.IdentityKey;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/// Generates and owns component-local stylesheets for explicit color properties.
///
/// The generated stylesheet is attached to the component itself, giving it branch-local author-style scope without
/// rewriting the application's inline style or installing a complete local theme. Empty override values remove all
/// managed state. Generated URLs are weakly cached so repeated immutable color values can share their encoded CSS
/// while unused application color combinations remain collectible.
@NotNullByDefault
public final class M3ComponentColorStyles {
    /// Property key for a button color installation.
    private static final IdentityKey BUTTON_COLORS_KEY =
            new IdentityKey(M3ComponentColorStyles.class.getName() + ".buttonColors");

    /// Property key for a card color installation.
    private static final IdentityKey CARD_COLORS_KEY =
            new IdentityKey(M3ComponentColorStyles.class.getName() + ".cardColors");

    /// Property key for a surface color installation.
    private static final IdentityKey SURFACE_COLORS_KEY =
            new IdentityKey(M3ComponentColorStyles.class.getName() + ".surfaceColors");

    /// Property key shared by the two icon implementations.
    private static final IdentityKey ICON_TINT_KEY =
            new IdentityKey(M3ComponentColorStyles.class.getName() + ".iconTint");

    /// Style class that scopes explicit button colors.
    private static final String BUTTON_COLORS_STYLE_CLASS = "m3-custom-button-colors";

    /// Style class that scopes explicit card colors.
    private static final String CARD_COLORS_STYLE_CLASS = "m3-custom-card-colors";

    /// Style class that scopes explicit surface colors.
    private static final String SURFACE_COLORS_STYLE_CLASS = "m3-custom-surface-colors";

    /// Style class that scopes an explicit icon tint.
    private static final String ICON_TINT_STYLE_CLASS = "m3-custom-icon-tint";

    /// Encoded stylesheet URLs keyed weakly by their complete CSS text.
    private static final Map<String, String> STYLESHEET_URLS = new WeakHashMap<>();

    /// Prevents utility class instantiation.
    private M3ComponentColorStyles() {
    }

    /// Applies optional explicit colors to an M3FX button root.
    ///
    /// @param button the button receiving branch-local colors
    /// @param colors the color overrides, or `null` to remove them
    /// @throws NullPointerException if `button` is `null`
    public static void applyButtonColors(Parent button, @Nullable M3ButtonColors colors) {
        Objects.requireNonNull(button, "button");
        apply(button, BUTTON_COLORS_KEY, BUTTON_COLORS_STYLE_CLASS, buttonStylesheet(colors));
    }

    /// Applies optional explicit colors to an M3FX card root.
    ///
    /// @param card   the card receiving branch-local colors
    /// @param colors the color overrides, or `null` to remove them
    /// @throws NullPointerException if `card` is `null`
    public static void applyCardColors(Parent card, @Nullable M3CardColors colors) {
        Objects.requireNonNull(card, "card");
        apply(card, CARD_COLORS_KEY, CARD_COLORS_STYLE_CLASS, cardStylesheet(colors));
    }

    /// Applies optional explicit colors to an M3FX surface root.
    ///
    /// @param surface the surface receiving branch-local colors
    /// @param colors  the color overrides, or `null` to remove them
    /// @throws NullPointerException if `surface` is `null`
    public static void applySurfaceColors(Parent surface, @Nullable M3SurfaceColors colors) {
        Objects.requireNonNull(surface, "surface");
        apply(surface, SURFACE_COLORS_KEY, SURFACE_COLORS_STYLE_CLASS, surfaceStylesheet(colors));
    }

    /// Applies an optional explicit tint to an icon root.
    ///
    /// @param icon the icon receiving the branch-local tint
    /// @param tint the explicit tint, or `null` to restore semantic color resolution
    /// @throws NullPointerException if `icon` is `null`
    public static void applyIconTint(Parent icon, @Nullable Color tint) {
        Objects.requireNonNull(icon, "icon");
        @Nullable String stylesheet = tint == null
                ? null
                : ".m3-custom-icon-tint { -fx-text-fill: " + cssColor(tint) + "; }";
        apply(icon, ICON_TINT_KEY, ICON_TINT_STYLE_CLASS, stylesheet);
    }

    /// Creates the optional button override stylesheet.
    ///
    /// @return complete CSS, or `null` when no component is overridden
    private static @Nullable String buttonStylesheet(@Nullable M3ButtonColors colors) {
        if (colors == null || allNull(
                colors.containerColor(),
                colors.contentColor(),
                colors.disabledContainerColor(),
                colors.disabledContentColor()
        )) {
            return null;
        }

        StringBuilder css = new StringBuilder(768);
        @Nullable Color containerColor = colors.containerColor();
        @Nullable Color contentColor = colors.contentColor();
        if (containerColor != null || contentColor != null) {
            css.append(".m3-button.m3-custom-button-colors,")
                    .append(".m3-button.m3-custom-button-colors:hover,")
                    .append(".m3-button.m3-custom-button-colors:focused,")
                    .append(".m3-button.m3-custom-button-colors:armed,")
                    .append(".m3-button.m3-custom-button-colors:pressed {");
            if (containerColor != null) {
                css.append("-fx-background-color:").append(cssColor(containerColor)).append(';');
            }
            appendContentColor(css, contentColor);
            css.append('}');
        }
        if (contentColor != null) {
            css.append(".m3-button.m3-custom-button-colors > .m3-state-layer-container .m3-state-layer,")
                    .append(".m3-button.m3-custom-button-colors > .m3-state-layer-container .m3-ripple {")
                    .append("-fx-background-color:").append(cssColor(contentColor)).append(";")
                    .append('}');
        }

        @Nullable Color disabledContainerColor = colors.disabledContainerColor();
        @Nullable Color disabledContentColor = colors.disabledContentColor();
        if (disabledContainerColor != null || disabledContentColor != null) {
            css.append(".m3-button.m3-custom-button-colors:disabled {");
            if (disabledContainerColor != null) {
                css.append("-fx-background-color:").append(cssColor(disabledContainerColor)).append(';');
            }
            appendContentColor(css, disabledContentColor);
            css.append('}');
        }
        if (disabledContentColor != null) {
            css.append(".m3-button.m3-custom-button-colors:disabled > * {")
                    .append("-fx-opacity:1;")
                    .append('}');
        }
        return css.toString();
    }

    /// Creates the optional card override stylesheet.
    ///
    /// @return complete CSS, or `null` when no component is overridden
    private static @Nullable String cardStylesheet(@Nullable M3CardColors colors) {
        if (colors == null || allNull(
                colors.containerColor(),
                colors.contentColor(),
                colors.disabledContainerColor(),
                colors.disabledContentColor()
        )) {
            return null;
        }

        StringBuilder css = new StringBuilder(640);
        @Nullable Color contentColor = colors.contentColor();
        if (contentColor != null) {
            appendScopedContentRule(css, ".m3-card.m3-custom-card-colors", contentColor);
            css.append(".m3-card.m3-custom-card-colors > .m3-card-container > .m3-state-layer-container .m3-state-layer,")
                    .append(".m3-card.m3-custom-card-colors > .m3-card-container > .m3-state-layer-container .m3-ripple {")
                    .append("-fx-background-color:").append(cssColor(contentColor)).append(";")
                    .append('}');
        }
        @Nullable Color containerColor = colors.containerColor();
        if (containerColor != null) {
            css.append(".m3-card.m3-custom-card-colors > .m3-card-container {")
                    .append("-fx-background-color:").append(cssColor(containerColor)).append(";")
                    .append('}');
        }

        @Nullable Color disabledContentColor = colors.disabledContentColor();
        if (disabledContentColor != null) {
            appendScopedContentRule(css, ".m3-card.m3-custom-card-colors:disabled", disabledContentColor);
        }
        @Nullable Color disabledContainerColor = colors.disabledContainerColor();
        if (disabledContainerColor != null) {
            css.append(".m3-card.m3-custom-card-colors:disabled > .m3-card-container {")
                    .append("-fx-background-color:").append(cssColor(disabledContainerColor)).append(";")
                    .append('}');
        }
        return css.toString();
    }

    /// Creates the optional surface override stylesheet.
    ///
    /// @return complete CSS, or `null` when no component is overridden
    private static @Nullable String surfaceStylesheet(@Nullable M3SurfaceColors colors) {
        if (colors == null || allNull(colors.containerColor(), colors.contentColor())) {
            return null;
        }

        StringBuilder css = new StringBuilder(320);
        @Nullable Color contentColor = colors.contentColor();
        if (contentColor != null) {
            appendScopedContentRule(css, ".m3-surface.m3-custom-surface-colors", contentColor);
        }
        @Nullable Color containerColor = colors.containerColor();
        if (containerColor != null) {
            css.append(".m3-surface.m3-custom-surface-colors > .m3-surface-container {")
                    .append("-fx-background-color:").append(cssColor(containerColor)).append(";")
                    .append('}');
        }
        return css.toString();
    }

    /// Appends the shared content-color lookups for a scoped container.
    private static void appendScopedContentRule(StringBuilder css, String selector, Color color) {
        String value = cssColor(color);
        css.append(selector).append(" {")
                .append("-fx-text-fill:").append(value).append(';')
                .append("-m3-color-on-surface:").append(value).append(';')
                .append("-m3-color-on-surface-variant:").append(value).append(';')
                .append('}');
    }

    /// Appends declarations shared by button text, icons, disclosure graphics, and interaction overlays.
    private static void appendContentColor(StringBuilder css, @Nullable Color color) {
        if (color == null) {
            return;
        }
        String value = cssColor(color);
        css.append("-fx-text-fill:").append(value).append(';')
                .append("-m3-button-icon-color:").append(value).append(';')
                .append("-m3-disclosure-icon-color:").append(value).append(';');
    }

    /// Returns whether every supplied color is absent.
    private static boolean allNull(@Nullable Color... colors) {
        for (@Nullable Color color : colors) {
            if (color != null) {
                return false;
            }
        }
        return true;
    }

    /// Serializes a JavaFX color as an exact CSS RGBA function.
    private static String cssColor(Color color) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return "rgba(" + red + ',' + green + ',' + blue + ',' + color.getOpacity() + ')';
    }

    /// Installs, replaces, or removes one managed branch-local stylesheet.
    private static void apply(
            Parent root,
            IdentityKey propertyKey,
            String styleClass,
            @Nullable String stylesheet
    ) {
        @Nullable Installation previous = installation(root, propertyKey);
        if (stylesheet == null || stylesheet.isEmpty()) {
            uninstall(root, propertyKey, styleClass, previous);
            return;
        }

        String stylesheetUrl = stylesheetUrl(stylesheet);
        if (previous != null && previous.stylesheetUrl().equals(stylesheetUrl)) {
            Installation current = ensureInstalled(root, styleClass, previous);
            if (current != previous) {
                root.getProperties().put(propertyKey, current);
            }
            return;
        }

        boolean ownsStyleClass = previous != null
                ? previous.ownsStyleClass()
                : !root.getStyleClass().contains(styleClass);
        if (previous != null && previous.ownsStylesheet()) {
            root.getStylesheets().remove(previous.stylesheetUrl());
        }
        if (!root.getStyleClass().contains(styleClass)) {
            root.getStyleClass().add(styleClass);
        }

        ObservableList<String> stylesheets = root.getStylesheets();
        boolean ownsStylesheet = !stylesheets.contains(stylesheetUrl);
        if (ownsStylesheet) {
            stylesheets.add(stylesheetUrl);
        }
        root.getProperties().put(
                propertyKey,
                new Installation(stylesheetUrl, ownsStyleClass, ownsStylesheet)
        );
    }

    /// Restores a managed installation that an application removed while its property remained set.
    ///
    /// @return the original installation when it was intact, or an updated installation that owns restored entries
    private static Installation ensureInstalled(Parent root, String styleClass, Installation installation) {
        boolean ownsStyleClass = installation.ownsStyleClass();
        if (!root.getStyleClass().contains(styleClass)) {
            root.getStyleClass().add(styleClass);
            ownsStyleClass = true;
        }
        boolean ownsStylesheet = installation.ownsStylesheet();
        if (!root.getStylesheets().contains(installation.stylesheetUrl())) {
            root.getStylesheets().add(installation.stylesheetUrl());
            ownsStylesheet = true;
        }
        return ownsStyleClass == installation.ownsStyleClass() && ownsStylesheet == installation.ownsStylesheet()
                ? installation
                : new Installation(installation.stylesheetUrl(), ownsStyleClass, ownsStylesheet);
    }

    /// Removes only entries introduced by the managed installation.
    private static void uninstall(
            Parent root,
            IdentityKey propertyKey,
            String styleClass,
            @Nullable Installation installation
    ) {
        if (installation == null) {
            return;
        }
        root.getProperties().remove(propertyKey);
        if (installation.ownsStylesheet()) {
            root.getStylesheets().remove(installation.stylesheetUrl());
        }
        if (installation.ownsStyleClass()) {
            root.getStyleClass().remove(styleClass);
        }
    }

    /// Returns the managed installation stored on a root, if present.
    private static @Nullable Installation installation(Parent root, IdentityKey propertyKey) {
        Object value = root.getProperties().get(propertyKey);
        return value instanceof Installation installation ? installation : null;
    }

    /// Returns a cached self-contained URL for generated CSS.
    private static String stylesheetUrl(String stylesheet) {
        synchronized (STYLESHEET_URLS) {
            return STYLESHEET_URLS.computeIfAbsent(stylesheet, M3StylesheetUrls::create);
        }
    }

    /// Tracks ownership of one generated stylesheet and its scoping style class.
    ///
    /// @param stylesheetUrl  the self-contained generated stylesheet URL
    /// @param ownsStyleClass whether M3FX added the scoping style class
    /// @param ownsStylesheet whether M3FX added the stylesheet-list entry
    private record Installation(String stylesheetUrl, boolean ownsStyleClass, boolean ownsStylesheet) {
        /// Returns the generated stylesheet URL.
        @Override
        public String stylesheetUrl() {
            return stylesheetUrl;
        }

        /// Returns whether M3FX owns the scoping style class.
        @Override
        public boolean ownsStyleClass() {
            return ownsStyleClass;
        }

        /// Returns whether M3FX owns the stylesheet entry.
        @Override
        public boolean ownsStylesheet() {
            return ownsStylesheet;
        }
    }
}
