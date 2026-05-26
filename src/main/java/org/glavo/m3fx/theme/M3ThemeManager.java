// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.theme;

import javafx.application.Application;
import javafx.css.Styleable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

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

/// Installs M3FX themes and stylesheets into JavaFX scenes.
///
/// The manager is the central integration point between a JavaFX application and the Material Design 3 token
/// system. Installing a theme on a [Scene] adds the M3FX user-agent stylesheet, writes generated token CSS to a
/// scene stylesheet, and applies root style classes such as light or dark brightness and baseline or expressive
/// profile. Installing on a [Parent] only writes inline root declarations, which is useful for detached popup
/// content or embedded controls that already share the scene stylesheet.
///
/// Theme installation is reversible. `uninstall` restores the root style captured before installation and
/// removes generated stylesheets tracked by the manager. Use [copyThemeContext] for popup roots whose scene is
/// created outside the main window so they keep the same [Material Design](https://m3.material.io/) color and
/// typography context as their owner.
@NotNullByDefault
public final class M3ThemeManager {
    /// The style class applied to themed roots.
    public static final String ROOT_STYLE_CLASS = "m3-root";

    /// The style class applied to roots using the baseline Material Design 3 profile.
    public static final String BASELINE_PROFILE_STYLE_CLASS = "m3-profile-baseline";

    /// The style class applied to roots using the Material Design 3 Expressive profile.
    public static final String EXPRESSIVE_PROFILE_STYLE_CLASS = "m3-profile-expressive";

    /// The style class applied to roots using a light color scheme.
    public static final String LIGHT_BRIGHTNESS_STYLE_CLASS = "m3-light";

    /// The style class applied to roots using a dark color scheme.
    public static final String DARK_BRIGHTNESS_STYLE_CLASS = "m3-dark";

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

    /// Returns the theme installed on the scene root, or null when no M3FX theme is installed.
    public static @Nullable M3Theme getTheme(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        return getTheme(scene.getRoot());
    }

    /// Installs theme tokens on a root node.
    public static void install(Parent root, M3Theme theme) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(theme, "theme");

        applyThemeStyleClasses(root, theme);

        if (!root.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            root.getProperties().put(BASE_STYLE_PROPERTY_KEY, root.getStyle());
        }

        Object baseStyleValue = root.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        String baseStyle = baseStyleValue instanceof String ? (String) baseStyleValue : "";
        root.setStyle(mergeStyles(baseStyle, theme.toRootStyleDeclarations()));
        root.getProperties().put(THEME_PROPERTY_KEY, theme);
    }

    /// Returns the theme installed on a root node, or null when no M3FX theme is installed.
    public static @Nullable M3Theme getTheme(Parent root) {
        Objects.requireNonNull(root, "root");

        Object theme = root.getProperties().get(THEME_PROPERTY_KEY);
        return theme instanceof M3Theme materialTheme ? materialTheme : null;
    }

    /// Adds the base m3fx stylesheet to a scene if it is not already present.
    public static void installStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        String stylesheet = stylesheetUrl();
        List<String> stylesheets = scene.getStylesheets();
        moveOrAdd(stylesheets, stylesheet, baseStylesheetIndex(stylesheets));
    }

    /// Adds the generated theme stylesheet to a scene.
    public static void installThemeStylesheet(Scene scene, M3Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        String stylesheet = themeStylesheetUrl(theme);
        List<String> stylesheets = scene.getStylesheets();
        String previousStylesheet = THEME_STYLESHEETS.put(scene, stylesheet);
        if (previousStylesheet != null && !previousStylesheet.equals(stylesheet)) {
            stylesheets.remove(previousStylesheet);
        }
        moveOrAdd(stylesheets, stylesheet, themeStylesheetIndex(stylesheets));
    }

    /// Removes m3fx theme state and stylesheets from a scene.
    public static void uninstall(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        uninstall(scene.getRoot());
        uninstallThemeStylesheet(scene);
        uninstallStylesheet(scene);
    }

    /// Removes m3fx theme tokens from a root node.
    public static void uninstall(Parent root) {
        Objects.requireNonNull(root, "root");

        clearThemeStyleClasses(root);
        Object baseStyleValue = root.getProperties().remove(BASE_STYLE_PROPERTY_KEY);
        if (baseStyleValue instanceof String baseStyle) {
            root.setStyle(baseStyle);
        } else if (root.getProperties().containsKey(THEME_PROPERTY_KEY)) {
            root.setStyle("");
        }
        root.getProperties().remove(THEME_PROPERTY_KEY);
    }

    /// Removes the base m3fx stylesheet from a scene.
    public static void uninstallStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        scene.getStylesheets().remove(stylesheetUrl());
    }

    /// Copies the installed theme context from a scene root to a detached root such as popup content.
    public static void copyThemeContext(Parent sourceRoot, Parent targetRoot) {
        Objects.requireNonNull(sourceRoot, "sourceRoot");
        Objects.requireNonNull(targetRoot, "targetRoot");

        clearThemeStyleClasses(targetRoot);

        Object themeValue = sourceRoot.getProperties().get(THEME_PROPERTY_KEY);
        if (themeValue instanceof M3Theme theme) {
            applyThemeStyleClasses(targetRoot, theme);
            targetRoot.getProperties().put(THEME_PROPERTY_KEY, theme);
        } else {
            copyStyleClassIfPresent(sourceRoot, targetRoot, ROOT_STYLE_CLASS);
            copyStyleClassIfPresent(sourceRoot, targetRoot, BASELINE_PROFILE_STYLE_CLASS);
            copyStyleClassIfPresent(sourceRoot, targetRoot, EXPRESSIVE_PROFILE_STYLE_CLASS);
            copyStyleClassIfPresent(sourceRoot, targetRoot, LIGHT_BRIGHTNESS_STYLE_CLASS);
            copyStyleClassIfPresent(sourceRoot, targetRoot, DARK_BRIGHTNESS_STYLE_CLASS);
            targetRoot.getProperties().remove(THEME_PROPERTY_KEY);
        }

        targetRoot.setStyle(sourceRoot.getStyle());
    }

    /// Applies root, profile, and brightness style classes for a theme without mutating inline token styles.
    public static void applyThemeStyleClasses(Styleable root, M3Theme theme) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(theme, "theme");

        if (!root.getStyleClass().contains(ROOT_STYLE_CLASS)) {
            root.getStyleClass().add(ROOT_STYLE_CLASS);
        }
        updateThemeModeStyleClasses(root, theme);
    }

    /// Removes root, profile, and brightness style classes managed by M3FX.
    public static void clearThemeStyleClasses(Styleable root) {
        Objects.requireNonNull(root, "root");

        root.getStyleClass().remove(ROOT_STYLE_CLASS);
        removeThemeModeStyleClasses(root);
    }

    /// Removes the generated theme stylesheet tracked for a scene.
    public static void uninstallThemeStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        String stylesheet = THEME_STYLESHEETS.remove(scene);
        if (stylesheet != null) {
            scene.getStylesheets().remove(stylesheet);
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

    /// Returns a file URL for a generated theme stylesheet.
    public static String themeStylesheetUrl(M3Theme theme) {
        Objects.requireNonNull(theme, "theme");

        String stylesheet = themeStylesheet(theme);
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
            throw new IllegalStateException("Failed to write generated M3FX theme stylesheet", e);
        }

        return file.toUri().toString();
    }

    /// Creates the complete generated stylesheet for a theme.
    private static String themeStylesheet(M3Theme theme) {
        return "." + ROOT_STYLE_CLASS + " { "
                + theme.toRootStyleDeclarations()
                + " }\n\n"
                + theme.toControlStyleRules();
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

    /// Moves an existing stylesheet or adds a new stylesheet at the requested index.
    private static void moveOrAdd(List<String> stylesheets, String stylesheet, int index) {
        int targetIndex = Math.min(Math.max(0, index), stylesheets.size());
        int currentIndex = stylesheets.indexOf(stylesheet);
        if (currentIndex == targetIndex) {
            return;
        }
        if (currentIndex >= 0) {
            stylesheets.remove(currentIndex);
            if (currentIndex < targetIndex) {
                targetIndex--;
            }
        }
        stylesheets.add(Math.min(targetIndex, stylesheets.size()), stylesheet);
    }

    /// Returns the insertion index for the generated theme stylesheet.
    private static int themeStylesheetIndex(List<String> stylesheets) {
        int baseStylesheetIndex = stylesheets.indexOf(stylesheetUrl());
        return baseStylesheetIndex >= 0 ? baseStylesheetIndex + 1 : baseStylesheetIndex(stylesheets);
    }

    /// Returns the insertion index for the base stylesheet.
    private static int baseStylesheetIndex(List<String> stylesheets) {
        int fallbackStylesheetIndex = stylesheets.indexOf(M3Stylesheets.fallbackStylesheet());
        return fallbackStylesheetIndex >= 0 ? fallbackStylesheetIndex + 1 : 0;
    }

    /// Merges existing root style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }

    /// Updates profile and brightness classes on the themed root.
    private static void updateThemeModeStyleClasses(Styleable root, M3Theme theme) {
        removeThemeModeStyleClasses(root);
        root.getStyleClass().add(profileStyleClass(theme.profile()));
        root.getStyleClass().add(brightnessStyleClass(theme.brightness()));
    }

    /// Removes profile and brightness classes from the root.
    private static void removeThemeModeStyleClasses(Styleable root) {
        root.getStyleClass().remove(BASELINE_PROFILE_STYLE_CLASS);
        root.getStyleClass().remove(EXPRESSIVE_PROFILE_STYLE_CLASS);
        root.getStyleClass().remove(LIGHT_BRIGHTNESS_STYLE_CLASS);
        root.getStyleClass().remove(DARK_BRIGHTNESS_STYLE_CLASS);
    }

    /// Returns the root style class for a profile.
    private static String profileStyleClass(M3Profile profile) {
        return switch (profile) {
            case BASELINE_2021 -> BASELINE_PROFILE_STYLE_CLASS;
            case EXPRESSIVE_2025 -> EXPRESSIVE_PROFILE_STYLE_CLASS;
        };
    }

    /// Returns the root style class for a brightness mode.
    private static String brightnessStyleClass(Brightness brightness) {
        return switch (brightness) {
            case LIGHT -> LIGHT_BRIGHTNESS_STYLE_CLASS;
            case DARK -> DARK_BRIGHTNESS_STYLE_CLASS;
        };
    }

    /// Copies a style class from one root to another when it is present.
    private static void copyStyleClassIfPresent(Parent sourceRoot, Parent targetRoot, String styleClass) {
        if (sourceRoot.getStyleClass().contains(styleClass)) {
            targetRoot.getStyleClass().add(styleClass);
        }
    }
}
