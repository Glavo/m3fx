package org.glavo.m3fx.theme;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNullByDefault;

import java.net.URL;
import java.util.List;
import java.util.Objects;

/// Installs m3fx themes and stylesheets into JavaFX scenes.
@NotNullByDefault
public final class M3ThemeManager {
    /// The style class applied to themed roots.
    public static final String ROOT_STYLE_CLASS = "m3-root";

    /// The property key that stores the applied theme.
    public static final String THEME_PROPERTY_KEY = M3ThemeManager.class.getName() + ".theme";

    /// The property key that stores the root style before m3fx theme declarations were added.
    private static final String BASE_STYLE_PROPERTY_KEY = M3ThemeManager.class.getName() + ".baseStyle";

    /// The bundled stylesheet resource path.
    private static final String STYLESHEET_RESOURCE = "/org/glavo/m3fx/m3fx.css";

    /// Prevents utility class instantiation.
    private M3ThemeManager() {
    }

    /// Installs a theme on a scene and adds the bundled m3fx stylesheet.
    public static void install(Scene scene, M3Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        install(scene.getRoot(), theme);
        installStylesheet(scene);
    }

    /// Installs theme tokens on a root node.
    public static void install(Parent root, M3Theme theme) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(theme, "theme");

        if (!root.getStyleClass().contains(ROOT_STYLE_CLASS)) {
            root.getStyleClass().add(ROOT_STYLE_CLASS);
        }

        if (!root.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            root.getProperties().put(BASE_STYLE_PROPERTY_KEY, root.getStyle());
        }

        Object baseStyleValue = root.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        String baseStyle = baseStyleValue instanceof String ? (String) baseStyleValue : "";
        root.setStyle(mergeStyles(baseStyle, theme.toRootStyleDeclarations()));
        root.getProperties().put(THEME_PROPERTY_KEY, theme);
    }

    /// Adds the bundled m3fx stylesheet to a scene if it is not already present.
    public static void installStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        String stylesheet = stylesheetUrl();
        List<String> stylesheets = scene.getStylesheets();
        if (!stylesheets.contains(stylesheet)) {
            stylesheets.add(stylesheet);
        }
    }

    /// Sets the bundled m3fx stylesheet as the application user-agent stylesheet.
    public static void installUserAgentStylesheet() {
        Application.setUserAgentStylesheet(stylesheetUrl());
    }

    /// Returns the bundled m3fx stylesheet URL.
    public static String stylesheetUrl() {
        URL url = M3ThemeManager.class.getResource(STYLESHEET_RESOURCE);
        if (url == null) {
            throw new IllegalStateException("Missing stylesheet resource: " + STYLESHEET_RESOURCE);
        }
        return url.toExternalForm();
    }

    /// Merges existing root style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }
}
