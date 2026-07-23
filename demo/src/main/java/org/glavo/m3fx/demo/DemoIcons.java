// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import org.jetbrains.annotations.NotNullByDefault;

/// Demo-only vector icons used by component showcase pages.
///
/// The production library does not ship a Material icon set. The demo uses these small SVG paths so component
/// samples exercise icon slots with real vector graphics instead of text placeholders.
@NotNullByDefault
final class DemoIcons {
    /// The node property key that stores the logical demo icon name.
    static final String ICON_NAME_PROPERTY = "org.glavo.m3fx.demo.iconName";

    /// The base style class applied to every demo SVG icon.
    static final String STYLE_CLASS = "demo-vector-icon";

    /// The style class used for primary-colored demo SVG icons.
    static final String PRIMARY_STYLE_CLASS = "demo-vector-icon-primary";

    /// The style class used for icons drawn on primary containers.
    static final String ON_PRIMARY_STYLE_CLASS = "demo-vector-icon-on-primary";

    /// The style class used for secondary-colored demo SVG icons.
    static final String SECONDARY_STYLE_CLASS = "demo-vector-icon-secondary";

    /// The style class used for icons drawn on secondary containers.
    static final String ON_SECONDARY_CONTAINER_STYLE_CLASS = "demo-vector-icon-on-secondary-container";

    /// The style class used for tertiary-colored demo SVG icons.
    static final String TERTIARY_STYLE_CLASS = "demo-vector-icon-tertiary";

    /// The style class used for on-surface demo SVG icons.
    static final String ON_SURFACE_STYLE_CLASS = "demo-vector-icon-on-surface";

    /// The style class used for on-surface-variant demo SVG icons.
    static final String ON_SURFACE_VARIANT_STYLE_CLASS = "demo-vector-icon-on-surface-variant";

    /// The style class used for inverse-on-surface demo SVG icons.
    static final String INVERSE_ON_SURFACE_STYLE_CLASS = "demo-vector-icon-inverse-on-surface";

    /// The style class used for error-colored demo SVG icons.
    static final String ERROR_STYLE_CLASS = "demo-vector-icon-error";

    /// The style class used for floating action button demo SVG icons.
    static final String FAB_STYLE_CLASS = "demo-fab-icon";

    /// Prevents instantiation.
    private DemoIcons() {
    }

    /// Creates a primary-colored SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath primary(String name) {
        return create(name, PRIMARY_STYLE_CLASS);
    }

    /// Creates an on-primary SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath onPrimary(String name) {
        return create(name, ON_PRIMARY_STYLE_CLASS);
    }

    /// Creates a secondary-colored SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath secondary(String name) {
        return create(name, SECONDARY_STYLE_CLASS);
    }

    /// Creates an on-secondary-container SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath onSecondaryContainer(String name) {
        return create(name, ON_SECONDARY_CONTAINER_STYLE_CLASS);
    }

    /// Creates a tertiary-colored SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath tertiary(String name) {
        return create(name, TERTIARY_STYLE_CLASS);
    }

    /// Creates an on-surface SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath onSurface(String name) {
        return create(name, ON_SURFACE_STYLE_CLASS);
    }

    /// Creates an on-surface-variant SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath onSurfaceVariant(String name) {
        return create(name, ON_SURFACE_VARIANT_STYLE_CLASS);
    }

    /// Creates an inverse-on-surface SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath inverseOnSurface(String name) {
        return create(name, INVERSE_ON_SURFACE_STYLE_CLASS);
    }

    /// Creates an error-colored SVG icon.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath error(String name) {
        return create(name, ERROR_STYLE_CLASS);
    }

    /// Creates an SVG icon for floating action buttons.
    ///
    /// @param name the logical icon name
    /// @return the configured SVG path
    static SVGPath fab(String name) {
        return create(name, FAB_STYLE_CLASS);
    }

    /// Creates a styled SVG path.
    private static SVGPath create(String name, String colorStyleClass) {
        SVGPath icon = new SVGPath();
        icon.setContent(path(name));
        icon.setFillRule(FillRule.EVEN_ODD);
        icon.getStyleClass().addAll(STYLE_CLASS, colorStyleClass);
        icon.getProperties().put(ICON_NAME_PROPERTY, name);
        icon.setMouseTransparent(true);
        return icon;
    }

    /// Returns the material-style SVG path for a logical icon name.
    static String path(String name) {
        return switch (name) {
            case "+", "add" -> "M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z";
            case "*", "spark" -> "M12 2l1.9 6.1L20 10l-6.1 1.9L12 18l-1.9-6.1L4 10l6.1-1.9z";
            case "archive" -> "M20.5 5.5l-1.4-2H4.9l-1.4 2V20h17zM5.9 5h12.2l.7 1H5.2zM18.5 18h-13V8h13zM8 10h8v2H8z";
            case "arrow-back", "back" -> "M20 11H7.8l5.6-5.6L12 4l-8 8 8 8 1.4-1.4L7.8 13H20z";
            case "bold" -> "M15.6 10.8c1-.7 1.7-1.8 1.7-2.8 0-2.3-1.8-4-4-4H7v14h7c2.1 0 3.8-1.7 3.8-3.8 0-1.5-.9-2.8-2.2-3.4zM10 6.5h3c.8 0 1.5.7 1.5 1.5S13.8 9.5 13 9.5h-3zm3.5 9H10v-3h3.5c.8 0 1.5.7 1.5 1.5s-.7 1.5-1.5 1.5z";
            case "bookmark" -> "M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z";
            case "calendar" -> "M19 4h-1V2h-2v2H8V2H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14z";
            case "chevron-right" -> "M8.6 16.6 13.2 12 8.6 7.4 10 6l6 6-6 6z";
            case "check", "done" -> "M9 16.2 4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4z";
            case "close", "clear", "x" -> "M18.3 5.7 12 12l6.3 6.3-1.4 1.4-6.3-6.3-6.3 6.3-1.4-1.4L9.2 12 2.9 5.7l1.4-1.4 6.3 6.3 6.3-6.3z";
            case "create", "edit" -> "M3 17.3V21h3.8L17.9 9.9l-3.8-3.8zM20.7 7c.4-.4.4-1 0-1.4l-2.3-2.3c-.4-.4-1-.4-1.4 0l-1.8 1.8 3.8 3.8z";
            case "dashboard" -> "M3 13h8V3H3zm0 8h8v-6H3zm10 0h8V11h-8zm0-18v6h8V3z";
            case "delete" -> "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6zM8 4l1-1h6l1 1h4v2H4V4z";
            case "email", "mail" -> "M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4-8 5-8-5V6l8 5 8-5z";
            case "error", "warning" -> "M12 2 1 21h22zM13 18h-2v-2h2zm0-4h-2v-4h2z";
            case "favorite" -> "M12 21.4l-1.4-1.3C5.4 15.4 2 12.3 2 8.5 2 5.4 4.4 3 7.5 3c1.7 0 3.4.8 4.5 2 1.1-1.2 2.8-2 4.5-2C19.6 3 22 5.4 22 8.5c0 3.8-3.4 6.9-8.6 11.6z";
            case "format_align_center" -> "M7 15v2h10v-2zm-4 4v2h18v-2zM3 11v2h18v-2zm4-4v2h10V7zM3 3v2h18V3z";
            case "format_align_left" -> "M3 15v2h12v-2zm0 4v2h18v-2zm0-8v2h18v-2zm0-4v2h12V7zm0-4v2h18V3z";
            case "format_align_right" -> "M9 15v2h12v-2zM3 19v2h18v-2zm0-8v2h18v-2zm6-4v2h12V7zM3 3v2h18V3z";            case "folder", "open" -> "M10 4l2 2h8c1.1 0 2 .9 2 2v10c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z";
            case "group" -> "M16 11c1.7 0 3-1.3 3-3s-1.3-3-3-3-3 1.3-3 3 1.3 3 3 3zM8 11c1.7 0 3-1.3 3-3S9.7 5 8 5 5 6.3 5 8s1.3 3 3 3zm0 2c-2.3 0-7 1.2-7 3.5V19h14v-2.5C15 14.2 10.3 13 8 13zm8 0c-.3 0-.7 0-1.1.1 1.2.9 2.1 2 2.1 3.4V19h6v-2.5c0-2.3-4.7-3.5-7-3.5z";            case "home" -> "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
            case "image" -> "M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 11.5l2.5 3 3.5-4.5L19 16H5z";
            case "inbox" -> "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 14h-4c0 1.7-1.3 3-3 3s-3-1.3-3-3H5V5h14z";
            case "info", "i" -> "M11 17h2v-6h-2zm1-14a9 9 0 1 0 0 18 9 9 0 0 0 0-18zm-1 6h2V7h-2z";
            case "italic" -> "M10 4v3h2.2l-3.4 8H6v3h8v-3h-2.2l3.4-8H18V4z";
            case "label" -> "M17.6 5.8C17.2 5.3 16.6 5 16 5H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h11c.6 0 1.2-.3 1.6-.8L22 12z";
            case "lock" -> "M18 8h-1V6c0-2.8-2.2-5-5-5S7 3.2 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.7 1.3-3 3-3s3 1.3 3 3v2H9z";
            case "menu" -> "M3 6h18v2H3zm0 5h18v2H3zm0 5h18v2H3z";
            case "more" -> "M12 8a2 2 0 1 0 0-4 2 2 0 0 0 0 4zm0 2a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm0 6a2 2 0 1 0 0 4 2 2 0 0 0 0-4z";
            case "move" -> "M12 2 6.5 7.5 8 9l3-3v12l-3-3-1.5 1.5L12 22l5.5-5.5L16 15l-3 3V6l3 3 1.5-1.5z";
            case "navigation" -> "M12 2 4.5 20.3l.7.7 6.8-3 6.8 3 .7-.7z";
            case "notifications" -> "M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.1-1.6-5.6-4.5-6.3V4c0-.8-.7-1.5-1.5-1.5S10.5 3.2 10.5 4v.7C7.6 5.4 6 7.9 6 11v5l-2 2v1h16v-1z";
            case "palette" -> "M12 3C7 3 3 6.6 3 11c0 5 4 9 9 9h1c1.1 0 2-.9 2-2 0-.5-.2-1-.5-1.3-.3-.4-.5-.8-.5-1.4 0-1.1.9-2 2-2h2.2c1.5 0 2.8-1.2 2.8-2.8C21 6.6 17 3 12 3zM6.5 12C5.7 12 5 11.3 5 10.5S5.7 9 6.5 9 8 9.7 8 10.5 7.3 12 6.5 12zm3-4C8.7 8 8 7.3 8 6.5S8.7 5 9.5 5s1.5.7 1.5 1.5S10.3 8 9.5 8zm2.5 4c-.8 0-1.5-.7-1.5-1.5S11.2 9 12 9s1.5.7 1.5 1.5S12.8 12 12 12zm3-4c-.8 0-1.5-.7-1.5-1.5S14.2 5 15 5s1.5.7 1.5 1.5S15.8 8 15 8zm2.5 4c-.8 0-1.5-.7-1.5-1.5S16.7 9 17.5 9s1.5.7 1.5 1.5S18.3 12 17.5 12z";
            case "person" -> "M12 12c2.2 0 4-1.8 4-4s-1.8-4-4-4-4 1.8-4 4 1.8 4 4 4zm0 2c-2.7 0-8 1.3-8 4v2h16v-2c0-2.7-5.3-4-8-4z";
            case "reports" -> "M5 9.2h3V19H5zm5-4h3V19h-3zm5 7h3V19h-3z";
            case "save" -> "M17 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V7zm-5 16a3 3 0 1 1 0-6 3 3 0 0 1 0 6zM6 8V5h9v3z";
            case "schedule" -> "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 11h5v-2h-4V6h-2v7z";
            case "search" -> "M9.5 3a6.5 6.5 0 0 1 5.1 10.5l4.4 4.4-1.4 1.4-4.4-4.4A6.5 6.5 0 1 1 9.5 3zm0 2a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0-9z";
            case "send" -> "M2 21l21-9L2 3v7l15 2-15 2z";
            case "settings" -> "M19.4 13.5c.1-.5.1-1 .1-1.5s0-1-.1-1.5l2.1-1.6-2-3.5-2.5 1a7 7 0 0 0-2.6-1.5L14 2h-4l-.4 2.9A7 7 0 0 0 7 6.4l-2.5-1-2 3.5 2.1 1.6a9.2 9.2 0 0 0 0 3L2.5 15.1l2 3.5 2.5-1a7 7 0 0 0 2.6 1.5L10 22h4l.4-2.9a7 7 0 0 0 2.6-1.5l2.5 1 2-3.5zM12 15.5A3.5 3.5 0 1 1 12 8a3.5 3.5 0 0 1 0 7.5z";
            case "share" -> "M18 16.1c-.8 0-1.5.3-2 .9L8.9 12.9c.1-.3.1-.6.1-.9s0-.6-.1-.9L16 7c.5.5 1.2.9 2 .9a3 3 0 1 0-3-3c0 .3 0 .6.1.9L8 9.9A3 3 0 1 0 8 14l7.1 4.2c-.1.3-.1.5-.1.8a3 3 0 1 0 3-2.9z";
            case "star" -> "M12 17.3 18.2 21l-1.6-7L22 9.2l-7.2-.6L12 2 9.2 8.6 2 9.2 7.5 14 5.8 21z";
            case "task" -> "M3 13h2v-2H3zm0 4h2v-2H3zm0-8h2V7H3zm4 4h14v-2H7zm0 4h14v-2H7zM7 7v2h14V7z";
            case "text" -> "M5 4v3h5v13h4V7h5V4z";
            case "tune" -> "M3 17v2h6v-2zm0-6v2h12v-2zm0-6v2h18V5zm14 12v2h4v-2zm-8-6v2h12v-2zm6-6v2h6V5z";
            case "upload" -> "M5 17h14v2H5zm7-14 5.5 5.5H14V15h-4V8.5H6.5z";            case "underline" -> "M12 17c3.3 0 6-2.7 6-6V3h-2.5v8c0 1.9-1.6 3.5-3.5 3.5S8.5 12.9 8.5 11V3H6v8c0 3.3 2.7 6 6 6zM5 19v2h14v-2z";
            case "visibility" -> "M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5c-1.7-4.4-6-7.5-11-7.5zm0 12.5a5 5 0 1 1 0-10 5 5 0 0 1 0 10zm0-2a3 3 0 1 0 0-6 3 3 0 0 0 0 6z";
            case "work" -> "M20 6h-4V4c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6 0h-4V4h4z";
            default -> throw new IllegalArgumentException("Unknown demo icon: " + name);
        };
    }
}
