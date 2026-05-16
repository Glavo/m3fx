// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.theme;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/// Installs m3fx themes and stylesheets into JavaFX scenes.
@NotNullByDefault
public final class M3ThemeManager {
    /// The style class applied to themed roots.
    public static final String ROOT_STYLE_CLASS = "m3-root";

    /// The property key that stores the applied theme.
    public static final String THEME_PROPERTY_KEY = M3ThemeManager.class.getName() + ".theme";

    /// The property key that stores the root style before m3fx theme declarations were added.
    private static final String BASE_STYLE_PROPERTY_KEY = M3ThemeManager.class.getName() + ".baseStyle";

    /// The directory name used for generated theme stylesheets.
    private static final String THEME_STYLESHEET_DIRECTORY = "m3fx-theme-stylesheets";

    /// The map from scenes to their generated theme stylesheet URL.
    private static final Map<Scene, String> THEME_STYLESHEETS = Collections.synchronizedMap(new WeakHashMap<>());

    /// Prevents utility class instantiation.
    private M3ThemeManager() {
    }

    /// Installs a theme on a scene and adds the base m3fx stylesheet.
    public static void install(Scene scene, M3Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        install(scene.getRoot(), theme);
        installStylesheet(scene);
        installThemeStylesheet(scene, theme);
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

    /// Adds the base m3fx stylesheet to a scene if it is not already present.
    public static void installStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        String stylesheet = stylesheetUrl();
        List<String> stylesheets = scene.getStylesheets();
        if (!stylesheets.contains(stylesheet)) {
            stylesheets.add(stylesheet);
        }
    }

    /// Adds the generated component token stylesheet to a scene.
    public static void installThemeStylesheet(Scene scene, M3Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        String stylesheet = themeStylesheetUrl(theme);
        List<String> stylesheets = scene.getStylesheets();
        String previousStylesheet = THEME_STYLESHEETS.put(scene, stylesheet);
        if (previousStylesheet != null && !previousStylesheet.equals(stylesheet)) {
            stylesheets.remove(previousStylesheet);
        }
        if (!stylesheets.contains(stylesheet)) {
            stylesheets.add(stylesheet);
        }
    }

    /// Sets the base m3fx stylesheet as the application user-agent stylesheet.
    public static void installUserAgentStylesheet() {
        Application.setUserAgentStylesheet(stylesheetUrl());
    }

    /// Returns the base m3fx stylesheet URL.
    public static String stylesheetUrl() {
        return M3Stylesheets.baseStylesheet();
    }

    /// Returns a file URL for a generated component token stylesheet.
    private static String themeStylesheetUrl(M3Theme theme) {
        String stylesheet = theme.toControlStyleRules();
        String digest = sha256(stylesheet);
        Path directory = Path.of(System.getProperty("java.io.tmpdir"), THEME_STYLESHEET_DIRECTORY);
        Path file = directory.resolve("m3fx-theme-" + digest + ".css");

        try {
            Files.createDirectories(directory);
            if (!Files.exists(file)) {
                Files.writeString(file, stylesheet, StandardCharsets.UTF_8);
                file.toFile().deleteOnExit();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write generated m3fx theme stylesheet", e);
        }

        return file.toUri().toString();
    }

    /// Computes the SHA-256 digest for generated stylesheet content.
    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(Character.forDigit((item >> 4) & 0x0f, 16));
                builder.append(Character.forDigit(item & 0x0f, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing SHA-256 message digest", e);
        }
    }

    /// Merges existing root style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }
}
