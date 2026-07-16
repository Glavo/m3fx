// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.theme;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import org.glavo.m3fx.internal.theme.M3ThemeCssCompiler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
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

    /// The property key that stores the root style before M3FX theme declarations were added.
    private static final String BASE_STYLE_PROPERTY_KEY = M3ThemeManager.class.getName() + ".baseStyle";

    /// The process-local directory used for generated theme stylesheets.
    private static final Path THEME_STYLESHEET_DIRECTORY = Path.of(
            System.getProperty("java.io.tmpdir"),
            "m3fx-theme-stylesheets",
            Long.toString(ProcessHandle.current().pid())
    );

    /// Opaque scene property key for the generated theme stylesheet URL.
    private static final Object THEME_STYLESHEET_KEY = new Object();

    /// Opaque scene property key for the active scene-root theme installation.
    private static final Object SCENE_THEME_INSTALLATION_KEY = new Object();

    /// Generated stylesheet URLs keyed by immutable theme values.
    private static final Map<M3Theme, String> GENERATED_THEME_STYLESHEETS =
            Collections.synchronizedMap(new WeakHashMap<>());

    static {
        Thread cleanupThread = new Thread(
                M3ThemeManager::deleteGeneratedThemeStylesheets,
                "M3FX theme stylesheet cleanup"
        );
        cleanupThread.setContextClassLoader(null);
        Runtime.getRuntime().addShutdownHook(cleanupThread);
    }

    /// Prevents utility class instantiation.
    private M3ThemeManager() {
    }

    /// Installs a theme on a scene and adds the base M3FX stylesheet.
    public static void install(Scene scene, M3Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        installSceneTheme(scene, theme);
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
        root.setStyle(mergeStyles(baseStyle, M3ThemeCssCompiler.rootStyleDeclarations(theme)));
        M3ThemeMetadata.setTheme(root, theme);
    }

    /// Returns the theme installed on a root node, or null when no M3FX theme is installed.
    public static @Nullable M3Theme getTheme(Parent root) {
        return M3ThemeMetadata.getTheme(root);
    }

    /// Adds the base M3FX stylesheet to a scene if it is not already present.
    public static void installStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        String stylesheet = stylesheetUrl();
        List<String> stylesheets = scene.getStylesheets();
        int fallbackStylesheetIndex = stylesheets.indexOf(M3Stylesheets.fallbackStylesheet());
        moveOrAdd(stylesheets, stylesheet, fallbackStylesheetIndex >= 0 ? fallbackStylesheetIndex + 1 : 0);
    }

    /// Adds the generated theme stylesheet to a scene.
    public static void installThemeStylesheet(Scene scene, M3Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        String stylesheet = themeStylesheetUrl(theme);
        List<String> stylesheets = scene.getStylesheets();
        Object previousValue = scene.getProperties().get(THEME_STYLESHEET_KEY);
        @Nullable String previousStylesheet = previousValue instanceof String value ? value : null;
        if (!stylesheet.equals(previousStylesheet)) {
            scene.getProperties().put(THEME_STYLESHEET_KEY, stylesheet);
            if (previousStylesheet != null) {
                stylesheets.remove(previousStylesheet);
            }
        }
        int baseStylesheetIndex = stylesheets.indexOf(stylesheetUrl());
        int stylesheetIndex = baseStylesheetIndex >= 0
                ? baseStylesheetIndex + 1
                : stylesheets.indexOf(M3Stylesheets.fallbackStylesheet()) + 1;
        moveOrAdd(stylesheets, stylesheet, stylesheetIndex);
    }

    /// Removes M3FX theme state and stylesheets from a scene.
    public static void uninstall(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        if (!uninstallSceneTheme(scene)) {
            uninstall(scene.getRoot());
        }
        uninstallThemeStylesheet(scene);
        uninstallStylesheet(scene);
    }

    /// Removes M3FX theme tokens from a root node.
    public static void uninstall(Parent root) {
        Objects.requireNonNull(root, "root");

        clearThemeStyleClasses(root);
        @Nullable Object baseStyleValue = root.hasProperties()
                ? root.getProperties().remove(BASE_STYLE_PROPERTY_KEY)
                : null;
        if (baseStyleValue instanceof String baseStyle) {
            root.setStyle(baseStyle);
        } else if (M3ThemeMetadata.hasTheme(root)) {
            root.setStyle("");
        }
        M3ThemeMetadata.clearTheme(root);
    }

    /// Removes the base M3FX stylesheet from a scene.
    public static void uninstallStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        scene.getStylesheets().remove(stylesheetUrl());
    }

    /// Copies the installed theme context from a scene root to a detached root such as popup content.
    public static void copyThemeContext(Parent sourceRoot, Parent targetRoot) {
        Objects.requireNonNull(sourceRoot, "sourceRoot");
        Objects.requireNonNull(targetRoot, "targetRoot");

        @Nullable M3Theme theme = M3ThemeMetadata.getTheme(sourceRoot);
        if (theme != null) {
            applyThemeStyleClasses(targetRoot, theme);
            M3ThemeMetadata.setTheme(targetRoot, theme);
        } else {
            List<String> sourceStyleClasses = sourceRoot.getStyleClass();
            setStyleClassPresent(
                    targetRoot,
                    ROOT_STYLE_CLASS,
                    sourceStyleClasses.contains(ROOT_STYLE_CLASS)
            );
            setStyleClassPresent(
                    targetRoot,
                    BASELINE_PROFILE_STYLE_CLASS,
                    sourceStyleClasses.contains(BASELINE_PROFILE_STYLE_CLASS)
            );
            setStyleClassPresent(
                    targetRoot,
                    EXPRESSIVE_PROFILE_STYLE_CLASS,
                    sourceStyleClasses.contains(EXPRESSIVE_PROFILE_STYLE_CLASS)
            );
            setStyleClassPresent(
                    targetRoot,
                    LIGHT_BRIGHTNESS_STYLE_CLASS,
                    sourceStyleClasses.contains(LIGHT_BRIGHTNESS_STYLE_CLASS)
            );
            setStyleClassPresent(
                    targetRoot,
                    DARK_BRIGHTNESS_STYLE_CLASS,
                    sourceStyleClasses.contains(DARK_BRIGHTNESS_STYLE_CLASS)
            );
            M3ThemeMetadata.clearTheme(targetRoot);
        }

        String sourceStyle = sourceRoot.getStyle();
        if (!Objects.equals(targetRoot.getStyle(), sourceStyle)) {
            targetRoot.setStyle(sourceStyle);
        }
    }

    /// Applies root, profile, and brightness style classes for a theme without mutating inline token styles.
    public static void applyThemeStyleClasses(Styleable root, M3Theme theme) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(theme, "theme");

        String profileStyleClass = switch (theme.profile()) {
            case BASELINE_2021 -> BASELINE_PROFILE_STYLE_CLASS;
            case EXPRESSIVE_2025 -> EXPRESSIVE_PROFILE_STYLE_CLASS;
        };
        String brightnessStyleClass = switch (theme.brightness()) {
            case LIGHT -> LIGHT_BRIGHTNESS_STYLE_CLASS;
            case DARK -> DARK_BRIGHTNESS_STYLE_CLASS;
        };

        setStyleClassPresent(root, ROOT_STYLE_CLASS, true);
        setStyleClassPresent(
                root,
                BASELINE_PROFILE_STYLE_CLASS,
                BASELINE_PROFILE_STYLE_CLASS.equals(profileStyleClass)
        );
        setStyleClassPresent(
                root,
                EXPRESSIVE_PROFILE_STYLE_CLASS,
                EXPRESSIVE_PROFILE_STYLE_CLASS.equals(profileStyleClass)
        );
        setStyleClassPresent(
                root,
                LIGHT_BRIGHTNESS_STYLE_CLASS,
                LIGHT_BRIGHTNESS_STYLE_CLASS.equals(brightnessStyleClass)
        );
        setStyleClassPresent(
                root,
                DARK_BRIGHTNESS_STYLE_CLASS,
                DARK_BRIGHTNESS_STYLE_CLASS.equals(brightnessStyleClass)
        );
    }

    /// Removes root, profile, and brightness style classes managed by M3FX.
    public static void clearThemeStyleClasses(Styleable root) {
        Objects.requireNonNull(root, "root");

        setStyleClassPresent(root, ROOT_STYLE_CLASS, false);
        setStyleClassPresent(root, BASELINE_PROFILE_STYLE_CLASS, false);
        setStyleClassPresent(root, EXPRESSIVE_PROFILE_STYLE_CLASS, false);
        setStyleClassPresent(root, LIGHT_BRIGHTNESS_STYLE_CLASS, false);
        setStyleClassPresent(root, DARK_BRIGHTNESS_STYLE_CLASS, false);
    }

    /// Removes the generated theme stylesheet tracked for a scene.
    public static void uninstallThemeStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        @Nullable Object value = scene.hasProperties()
                ? scene.getProperties().remove(THEME_STYLESHEET_KEY)
                : null;
        @Nullable String stylesheet = value instanceof String url ? url : null;
        if (stylesheet != null) {
            scene.getStylesheets().remove(stylesheet);
        }
    }

    /// Sets the base M3FX stylesheet as the application user-agent stylesheet.
    public static void installUserAgentStylesheet() {
        Application.setUserAgentStylesheet(stylesheetUrl());
    }

    /// Returns the base M3FX stylesheet URL.
    public static String stylesheetUrl() {
        return M3Stylesheets.baseStylesheet();
    }

    /// Returns a file URL for a generated theme stylesheet.
    public static String themeStylesheetUrl(M3Theme theme) {
        Objects.requireNonNull(theme, "theme");

        synchronized (GENERATED_THEME_STYLESHEETS) {
            @Nullable String cachedUrl = GENERATED_THEME_STYLESHEETS.get(theme);
            if (cachedUrl != null) {
                return cachedUrl;
            }

            String stylesheet = themeStylesheet(theme);
            String digest = sha256(stylesheet);
            Path file = THEME_STYLESHEET_DIRECTORY.resolve("m3fx-theme-" + digest + ".css");

            try {
                Files.createDirectories(THEME_STYLESHEET_DIRECTORY);
                if (!Files.exists(file)) {
                    Files.writeString(file, stylesheet, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write generated M3FX theme stylesheet", e);
            }

            String url = file.toUri().toString();
            GENERATED_THEME_STYLESHEETS.put(theme, url);
            return url;
        }
    }

    /// Deletes this process's generated theme stylesheets without retaining one shutdown entry per theme.
    private static void deleteGeneratedThemeStylesheets() {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(THEME_STYLESHEET_DIRECTORY)) {
            for (Path file : files) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }

        try {
            Files.deleteIfExists(THEME_STYLESHEET_DIRECTORY);
        } catch (IOException ignored) {
        }
    }

    /// Creates the complete generated stylesheet for a theme.
    private static String themeStylesheet(M3Theme theme) {
        return "." + ROOT_STYLE_CLASS + " { "
                + M3ThemeCssCompiler.rootStyleDeclarations(theme)
                + " }\n\n"
                + M3ThemeCssCompiler.controlStyleRules(theme);
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

    /// Installs a scene-owned theme and observes scene root replacement.
    private static void installSceneTheme(Scene scene, M3Theme theme) {
        Object value = scene.getProperties().get(SCENE_THEME_INSTALLATION_KEY);
        @Nullable SceneThemeInstallation installation =
                value instanceof SceneThemeInstallation current ? current : null;
        if (installation == null) {
            installation = new SceneThemeInstallation(scene);
            scene.getProperties().put(SCENE_THEME_INSTALLATION_KEY, installation);
        }
        installation.install(scene, theme);
    }

    /// Stops scene-root theme observation for a scene.
    private static boolean uninstallSceneTheme(Scene scene) {
        @Nullable Object value = scene.hasProperties()
                ? scene.getProperties().remove(SCENE_THEME_INSTALLATION_KEY)
                : null;
        @Nullable SceneThemeInstallation installation =
                value instanceof SceneThemeInstallation current ? current : null;
        if (installation != null) {
            installation.dispose(scene);
            return true;
        }
        return false;
    }

    /// Merges existing root style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }

    /// Adds or removes one managed style class only when its desired state differs.
    private static void setStyleClassPresent(Styleable root, String styleClass, boolean present) {
        List<String> styleClasses = root.getStyleClass();
        boolean currentlyPresent = styleClasses.contains(styleClass);
        if (currentlyPresent == present) {
            return;
        }
        if (present) {
            styleClasses.add(styleClass);
        } else {
            styleClasses.remove(styleClass);
        }
    }

    /// Maintains scene-level theme declarations across root replacement.
    @NotNullByDefault
    private static final class SceneThemeInstallation {
        /// Handles scene root replacement.
        private final ChangeListener<Parent> rootListener = this::handleRootChanged;

        /// The root state captured before applying the scene theme.
        private @Nullable RootThemeSnapshot snapshot;

        /// The current theme applied by this scene installation.
        private @Nullable M3Theme theme;

        /// Creates a scene theme installation.
        private SceneThemeInstallation(Scene scene) {
            scene.rootProperty().addListener(rootListener);
        }

        /// Applies or replaces the scene-owned theme.
        private void install(Scene scene, M3Theme theme) {
            Parent root = scene.getRoot();
            @Nullable RootThemeSnapshot currentSnapshot = snapshot;
            if (this.theme == theme && currentSnapshot != null && currentSnapshot.references(root)) {
                return;
            }

            this.theme = theme;
            if (currentSnapshot != null && currentSnapshot.references(root)) {
                applyTheme(root, theme, currentSnapshot.baseStyle);
            } else {
                installRoot(root, theme);
            }
        }

        /// Removes the scene root listener.
        private void dispose(Scene scene) {
            scene.rootProperty().removeListener(rootListener);
            restoreSnapshot();
            theme = null;
        }

        /// Moves the installed theme from the previous scene root to the new one.
        private void handleRootChanged(
                ObservableValue<? extends Parent> observable,
                Parent oldRoot,
                Parent newRoot
        ) {
            restoreSnapshot();
            @Nullable M3Theme currentTheme = theme;
            if (currentTheme != null) {
                installRoot(newRoot, currentTheme);
            }
        }

        /// Applies the scene theme to one root after saving its previous theme state.
        private void installRoot(Parent root, M3Theme theme) {
            restoreSnapshot();
            RootThemeSnapshot nextSnapshot = new RootThemeSnapshot(root);
            snapshot = nextSnapshot;
            applyTheme(root, theme, nextSnapshot.baseStyle);
        }

        /// Applies one theme over the root style captured before scene theme installation.
        private static void applyTheme(Parent root, M3Theme theme, String baseStyle) {
            applyThemeStyleClasses(root, theme);
            String themedStyle = mergeStyles(baseStyle, M3ThemeCssCompiler.rootStyleDeclarations(theme));
            if (!Objects.equals(root.getStyle(), themedStyle)) {
                root.setStyle(themedStyle);
            }
            M3ThemeMetadata.setTheme(root, theme);
        }

        /// Restores the root state that existed before the scene theme was applied.
        private void restoreSnapshot() {
            @Nullable RootThemeSnapshot currentSnapshot = snapshot;
            if (currentSnapshot != null) {
                currentSnapshot.restore();
                snapshot = null;
            }
        }
    }

    /// Captures root theme state before a scene-level theme temporarily overrides it.
    @NotNullByDefault
    private static final class RootThemeSnapshot {
        /// A weak reference to the root whose state was captured.
        private final WeakReference<Parent> rootReference;

        /// The inline style before scene-level theme declarations were added.
        private final String baseStyle;

        /// Whether the root had explicit theme metadata before the scene override.
        private final boolean hadTheme;

        /// The root theme metadata before the scene override.
        private final @Nullable M3Theme theme;

        /// Whether the root had the managed root style class.
        private final boolean hadRootStyleClass;

        /// Whether the root had the managed baseline profile style class.
        private final boolean hadBaselineProfileStyleClass;

        /// Whether the root had the managed expressive profile style class.
        private final boolean hadExpressiveProfileStyleClass;

        /// Whether the root had the managed light brightness style class.
        private final boolean hadLightBrightnessStyleClass;

        /// Whether the root had the managed dark brightness style class.
        private final boolean hadDarkBrightnessStyleClass;

        /// Captures current root theme state.
        private RootThemeSnapshot(Parent root) {
            rootReference = new WeakReference<>(root);
            baseStyle = root.getStyle();
            theme = M3ThemeMetadata.getTheme(root);
            hadTheme = theme != null;
            hadRootStyleClass = root.getStyleClass().contains(ROOT_STYLE_CLASS);
            hadBaselineProfileStyleClass = root.getStyleClass().contains(BASELINE_PROFILE_STYLE_CLASS);
            hadExpressiveProfileStyleClass = root.getStyleClass().contains(EXPRESSIVE_PROFILE_STYLE_CLASS);
            hadLightBrightnessStyleClass = root.getStyleClass().contains(LIGHT_BRIGHTNESS_STYLE_CLASS);
            hadDarkBrightnessStyleClass = root.getStyleClass().contains(DARK_BRIGHTNESS_STYLE_CLASS);
        }

        /// Returns whether this snapshot belongs to the supplied root.
        private boolean references(Parent root) {
            return rootReference.get() == root;
        }

        /// Restores the captured theme state to the root.
        private void restore() {
            @Nullable Parent root = rootReference.get();
            if (root == null) {
                return;
            }
            if (!Objects.equals(root.getStyle(), baseStyle)) {
                root.setStyle(baseStyle);
            }
            setStyleClassPresent(root, ROOT_STYLE_CLASS, hadRootStyleClass);
            setStyleClassPresent(root, BASELINE_PROFILE_STYLE_CLASS, hadBaselineProfileStyleClass);
            setStyleClassPresent(root, EXPRESSIVE_PROFILE_STYLE_CLASS, hadExpressiveProfileStyleClass);
            setStyleClassPresent(root, LIGHT_BRIGHTNESS_STYLE_CLASS, hadLightBrightnessStyleClass);
            setStyleClassPresent(root, DARK_BRIGHTNESS_STYLE_CLASS, hadDarkBrightnessStyleClass);
            if (hadTheme && theme != null) {
                M3ThemeMetadata.setTheme(root, theme);
            } else {
                M3ThemeMetadata.clearTheme(root);
            }
        }
    }
}
