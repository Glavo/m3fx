// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.theme.M3Theme;
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

import static org.glavo.m3fx.theme.M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS;
import static org.glavo.m3fx.theme.M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS;
import static org.glavo.m3fx.theme.M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS;
import static org.glavo.m3fx.theme.M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS;
import static org.glavo.m3fx.theme.M3ThemeManager.ROOT_STYLE_CLASS;

/// Implements the internal M3FX theme installation and rendering backend.
///
/// This runtime is the integration point between a JavaFX application and the Material Design 3 token
/// system. Installing a theme on a [Scene] adds the M3FX user-agent stylesheet, writes generated token CSS to a
/// scene stylesheet, and applies root style classes such as light or dark brightness and baseline or expressive
/// profile. Installing on a [Parent] creates a branch-local theme scope by combining root declarations with a
/// generated parent stylesheet. JavaFX gives that stylesheet precedence over scene stylesheets for the parent and
/// its descendants while still allowing later application-owned parent stylesheets to override M3FX defaults.
///
/// Theme installation is reversible. `uninstall` restores managed style classes and theme metadata and removes
/// generated stylesheets tracked by the manager. Application-owned inline styles are never rewritten. Use
/// [copyThemeContext] for popup roots whose scene is
/// created outside the main window so they keep the same [Material Design](https://m3.material.io/) color and
/// typography context as their owner.
@NotNullByDefault
public final class M3ThemeRuntime {
    /// The property key that stores state owned by a local parent theme installation.
    private static final String LOCAL_THEME_INSTALLATION_PROPERTY_KEY =
            M3ThemeRuntime.class.getName() + ".localThemeInstallation";

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

    /// Initial buffer size for a generated theme stylesheet, chosen to avoid most growth without retaining memory.
    private static final int THEME_STYLESHEET_INITIAL_CAPACITY = 64 * 1024;

    /// Generated stylesheet URLs keyed by immutable theme values.
    private static final Map<M3Theme, String> GENERATED_THEME_STYLESHEETS =
            Collections.synchronizedMap(new WeakHashMap<>());

    static {
        Thread cleanupThread = new Thread(
                M3ThemeRuntime::deleteGeneratedThemeStylesheets,
                "M3FX theme stylesheet cleanup"
        );
        cleanupThread.setContextClassLoader(null);
        Runtime.getRuntime().addShutdownHook(cleanupThread);
    }

    /// Prevents utility class instantiation.
    private M3ThemeRuntime() {
    }

    /// Installs a theme on a scene and adds the base M3FX stylesheet.
    ///
    /// Reinstalling replaces the currently managed theme while preserving the root style that preceded the first
    /// installation. The installation follows later replacements of the scene root.
    ///
    /// @param scene the scene that should receive the theme
    /// @param theme the theme to install
    /// @throws NullPointerException if `scene` or `theme` is `null`
    public static void install(Scene scene, M3Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        installSceneTheme(scene, theme);
        installStylesheet(scene);
        installThemeStylesheet(scene, theme);
    }

    /// Returns the theme installed on the scene root.
    ///
    /// @param scene the scene to inspect
    /// @return the installed theme, or `null` when the root has no M3FX theme metadata
    /// @throws NullPointerException if `scene` is `null`
    public static @Nullable M3Theme getTheme(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        return getTheme(scene.getRoot());
    }

    /// Installs theme tokens on a root node.
    ///
    /// Reinstalling a different theme updates only the generated local stylesheet, managed style classes, and
    /// metadata. The root's inline style is not changed. Application stylesheets already present on the parent
    /// remain after the managed stylesheet and may therefore override generated token declarations.
    ///
    /// @param root  the root that should receive theme declarations
    /// @param theme the theme to install
    /// If `root` is also controlled by an active scene installation, the local theme is retained beneath the scene
    /// theme and becomes effective when the scene installation releases that root.
    ///
    /// @throws NullPointerException if `root` or `theme` is `null`
    public static void install(Parent root, M3Theme theme) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(theme, "theme");
        @Nullable SceneThemeInstallation sceneInstallation = activeSceneInstallation(root);
        if (sceneInstallation != null) {
            sceneInstallation.installLocalTheme(root, theme);
            return;
        }

        installLocalTheme(root, theme);
    }

    /// Returns the theme installed on a root node.
    ///
    /// @param root the root to inspect
    /// @return the installed theme, or `null` when no M3FX theme metadata is present
    /// @throws NullPointerException if `root` is `null`
    public static @Nullable M3Theme getTheme(Parent root) {
        return M3ThemeMetadata.getTheme(root);
    }

    /// Adds the base M3FX stylesheet to a scene if it is not already present.
    ///
    /// If already present, the stylesheet is moved to its managed position after the fallback token stylesheet.
    ///
    /// @param scene the scene whose stylesheet list should be updated
    /// @throws NullPointerException if `scene` is `null`
    public static void installStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        String stylesheet = stylesheetUrl();
        List<String> stylesheets = scene.getStylesheets();
        int fallbackStylesheetIndex = stylesheets.indexOf(M3Stylesheets.fallbackStylesheet());
        moveOrAdd(stylesheets, stylesheet, fallbackStylesheetIndex >= 0 ? fallbackStylesheetIndex + 1 : 0);
    }

    /// Adds the generated theme stylesheet to a scene.
    ///
    /// Any previously managed generated theme stylesheet is removed before the replacement is inserted immediately
    /// after the base M3FX stylesheet when possible.
    ///
    /// @param scene the scene whose stylesheet list should be updated
    /// @param theme the theme whose token stylesheet should be installed
    /// @throws NullPointerException  if `scene` or `theme` is `null`
    /// @throws IllegalStateException if the generated stylesheet cannot be written
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
    ///
    /// The root inline style captured by [#install(Scene, M3Theme)] is restored. Calling this method when no managed
    /// scene installation exists still removes M3FX state directly from the current root.
    ///
    /// @param scene the scene to uninstall
    /// @throws NullPointerException if `scene` is `null`
    public static void uninstall(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        if (!uninstallSceneTheme(scene)) {
            uninstall(scene.getRoot());
        }
        uninstallThemeStylesheet(scene);
        uninstallStylesheet(scene);
    }

    /// Removes M3FX theme tokens from a root node.
    ///
    /// The method removes the generated local stylesheet, M3FX-managed profile and brightness classes, and theme
    /// metadata. It does not modify application-owned inline style. Calling it repeatedly has no further effect.
    ///
    /// @param root the root to uninstall
    /// If `root` is also controlled by an active scene installation, only the retained local theme is removed; the
    /// active scene theme remains applied.
    ///
    /// @throws NullPointerException if `root` is `null`
    public static void uninstall(Parent root) {
        Objects.requireNonNull(root, "root");
        @Nullable SceneThemeInstallation sceneInstallation = activeSceneInstallation(root);
        if (sceneInstallation != null) {
            sceneInstallation.uninstallLocalTheme(root);
            return;
        }

        uninstallLocalTheme(root);
    }

    /// Installs or replaces a local theme without consulting scene ownership.
    private static void installLocalTheme(Parent root, M3Theme theme) {
        installLocalThemeStylesheet(root, theme);
        applyThemeStyleClasses(root, theme);
        M3ThemeMetadata.setTheme(root, theme);
    }

    /// Removes a local theme without consulting scene ownership.
    private static void uninstallLocalTheme(Parent root) {
        @Nullable LocalThemeInstallation installation = removeLocalThemeInstallation(root);
        if (installation != null) {
            installation.uninstall(root);
        } else {
            clearThemeStyleClasses(root);
            M3ThemeMetadata.clearTheme(root);
        }
    }

    /// Removes the base M3FX stylesheet from a scene.
    ///
    /// @param scene the scene whose stylesheet list should be updated
    /// @throws NullPointerException if `scene` is `null`
    public static void uninstallStylesheet(Scene scene) {
        Objects.requireNonNull(scene, "scene");

        scene.getStylesheets().remove(stylesheetUrl());
    }

    /// Copies installed theme metadata and managed style classes to another root.
    ///
    /// Arbitrary inline declarations from `sourceRoot` are intentionally not copied. Popup infrastructure adds the
    /// generated stylesheet for the copied [M3Theme], while attached descendants continue to inherit lookup values
    /// from their normal ancestor chain. Excluding unrelated inline declarations prevents visual properties such as
    /// root backgrounds and padding from leaking into popup or virtualized component roots.
    ///
    /// @param sourceRoot the root that supplies theme metadata and managed style classes
    /// @param targetRoot the root that should receive the copied context
    /// @throws NullPointerException if either root is `null`
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

    }

    /// Applies root, profile, and brightness style classes for a theme without mutating inline token styles.
    ///
    /// @param root  the styleable object whose managed classes should be updated
    /// @param theme the theme that selects the profile and brightness classes
    /// @throws NullPointerException if `root` or `theme` is `null`
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
    ///
    /// @param root the styleable object whose managed classes should be removed
    /// @throws NullPointerException if `root` is `null`
    public static void clearThemeStyleClasses(Styleable root) {
        Objects.requireNonNull(root, "root");

        setStyleClassPresent(root, ROOT_STYLE_CLASS, false);
        setStyleClassPresent(root, BASELINE_PROFILE_STYLE_CLASS, false);
        setStyleClassPresent(root, EXPRESSIVE_PROFILE_STYLE_CLASS, false);
        setStyleClassPresent(root, LIGHT_BRIGHTNESS_STYLE_CLASS, false);
        setStyleClassPresent(root, DARK_BRIGHTNESS_STYLE_CLASS, false);
    }

    /// Removes the generated theme stylesheet tracked for a scene.
    ///
    /// Other stylesheet entries, including generated stylesheets not installed through this runtime, are left
    /// unchanged.
    ///
    /// @param scene the scene whose managed theme stylesheet should be removed
    /// @throws NullPointerException if `scene` is `null`
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

    /// Returns the base M3FX stylesheet URL.
    ///
    /// @return the external-form URL of the bundled base stylesheet
    public static String stylesheetUrl() {
        return M3Stylesheets.baseStylesheet();
    }

    /// Returns a file URL for a generated theme stylesheet.
    ///
    /// Equivalent immutable theme values share one process-local generated file while that cache entry remains
    /// reachable.
    ///
    /// @param theme the theme to compile
    /// @return the file URL of the generated stylesheet
    /// @throws NullPointerException  if `theme` is `null`
    /// @throws IllegalStateException if the stylesheet directory or file cannot be created
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
        StringBuilder builder = new StringBuilder(THEME_STYLESHEET_INITIAL_CAPACITY);
        builder.append('.').append(ROOT_STYLE_CLASS).append(" { ");
        M3ThemeCssCompiler.appendRootStyleDeclarations(builder, theme.tokens());
        builder.append("}\n\n");
        M3ThemeCssCompiler.appendControlStyleRules(builder, theme.tokens());
        return builder.toString();
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

    /// Installs the generated stylesheet that gives a local theme complete component-token coverage.
    ///
    /// Parent stylesheets outrank scene stylesheets for descendants of that parent. Keeping the generated theme
    /// stylesheet at the beginning of the local list lets later application-owned parent stylesheets continue to
    /// override M3FX defaults.
    ///
    /// @param root  the local theme root
    /// @param theme the theme whose generated rules should apply to the local branch
    private static void installLocalThemeStylesheet(Parent root, M3Theme theme) {
        String stylesheet = themeStylesheetUrl(theme);
        @Nullable LocalThemeInstallation installation = localThemeInstallation(root);
        @Nullable LocalThemeStylesheet previous = installation == null ? null : installation.stylesheet();
        List<String> stylesheets = root.getStylesheets();

        if (previous != null && previous.owned() && !previous.url().equals(stylesheet)) {
            stylesheets.remove(previous.url());
        }

        if (previous != null && previous.url().equals(stylesheet) && stylesheets.contains(stylesheet)) {
            if (previous.owned()) {
                moveOrAdd(stylesheets, stylesheet, 0);
            }
            return;
        }

        boolean owned = !stylesheets.contains(stylesheet);
        if (owned) {
            stylesheets.add(0, stylesheet);
        }
        LocalThemeStylesheet nextStylesheet = new LocalThemeStylesheet(stylesheet, owned);
        if (installation == null) {
            installation = new LocalThemeInstallation(root, nextStylesheet);
            root.getProperties().put(LOCAL_THEME_INSTALLATION_PROPERTY_KEY, installation);
        } else {
            installation.setStylesheet(nextStylesheet);
        }
    }

    /// Returns the local theme installation associated directly with a parent.
    ///
    /// @param root the parent to inspect
    /// @return the local installation, or `null` when the parent has no directly installed local theme
    private static @Nullable LocalThemeInstallation localThemeInstallation(Parent root) {
        @Nullable Object value = root.hasProperties()
                ? root.getProperties().get(LOCAL_THEME_INSTALLATION_PROPERTY_KEY)
                : null;
        return value instanceof LocalThemeInstallation installation ? installation : null;
    }

    /// Returns the generated stylesheet associated with a directly installed local theme.
    ///
    /// @param root the parent to inspect
    /// @return the local stylesheet metadata, or `null` when no local installation exists
    private static @Nullable LocalThemeStylesheet localThemeStylesheet(Parent root) {
        @Nullable LocalThemeInstallation installation = localThemeInstallation(root);
        return installation == null ? null : installation.stylesheet();
    }

    /// Removes and returns a local theme installation from a parent.
    ///
    /// @param root the parent whose local installation should be removed
    /// @return the removed installation, or `null` when none was present
    private static @Nullable LocalThemeInstallation removeLocalThemeInstallation(Parent root) {
        @Nullable Object value = root.hasProperties()
                ? root.getProperties().remove(LOCAL_THEME_INSTALLATION_PROPERTY_KEY)
                : null;
        return value instanceof LocalThemeInstallation installation ? installation : null;
    }

    /// Captures direct theme metadata and semantic style-class membership for later restoration.
    ///
    /// @param rootStyleClassPresent              whether the root style class was present
    /// @param baselineProfileStyleClassPresent   whether the baseline profile class was present
    /// @param expressiveProfileStyleClassPresent whether the Expressive profile class was present
    /// @param lightBrightnessStyleClassPresent   whether the light brightness class was present
    /// @param darkBrightnessStyleClassPresent    whether the dark brightness class was present
    /// @param theme                              the directly associated theme, or `null` when none existed
    @NotNullByDefault
    private record ThemeContextSnapshot(
            boolean rootStyleClassPresent,
            boolean baselineProfileStyleClassPresent,
            boolean expressiveProfileStyleClassPresent,
            boolean lightBrightnessStyleClassPresent,
            boolean darkBrightnessStyleClassPresent,
            @Nullable M3Theme theme
    ) {
        /// Captures direct theme context from a parent.
        private static ThemeContextSnapshot capture(Parent root) {
            return new ThemeContextSnapshot(
                    root.getStyleClass().contains(ROOT_STYLE_CLASS),
                    root.getStyleClass().contains(BASELINE_PROFILE_STYLE_CLASS),
                    root.getStyleClass().contains(EXPRESSIVE_PROFILE_STYLE_CLASS),
                    root.getStyleClass().contains(LIGHT_BRIGHTNESS_STYLE_CLASS),
                    root.getStyleClass().contains(DARK_BRIGHTNESS_STYLE_CLASS),
                    M3ThemeMetadata.getTheme(root)
            );
        }

        /// Restores this direct theme context to a parent.
        private void restore(Parent root) {
            setStyleClassPresent(root, ROOT_STYLE_CLASS, rootStyleClassPresent);
            setStyleClassPresent(root, BASELINE_PROFILE_STYLE_CLASS, baselineProfileStyleClassPresent);
            setStyleClassPresent(root, EXPRESSIVE_PROFILE_STYLE_CLASS, expressiveProfileStyleClassPresent);
            setStyleClassPresent(root, LIGHT_BRIGHTNESS_STYLE_CLASS, lightBrightnessStyleClassPresent);
            setStyleClassPresent(root, DARK_BRIGHTNESS_STYLE_CLASS, darkBrightnessStyleClassPresent);
            if (theme != null) {
                M3ThemeMetadata.setTheme(root, theme);
            } else {
                M3ThemeMetadata.clearTheme(root);
            }
        }
    }

    /// Retains application-owned theme context while a local theme temporarily replaces it.
    @NotNullByDefault
    private static final class LocalThemeInstallation {
        /// The direct theme context that existed before installation.
        private final ThemeContextSnapshot previousContext;

        /// The generated stylesheet currently associated with this installation.
        private LocalThemeStylesheet stylesheet;

        /// Captures the root context and initial generated stylesheet for a new local installation.
        private LocalThemeInstallation(Parent root, LocalThemeStylesheet stylesheet) {
            previousContext = ThemeContextSnapshot.capture(root);
            this.stylesheet = Objects.requireNonNull(stylesheet, "stylesheet");
        }

        /// Returns the generated stylesheet currently associated with this installation.
        private LocalThemeStylesheet stylesheet() {
            return stylesheet;
        }

        /// Replaces generated stylesheet ownership after a local theme reinstall.
        private void setStylesheet(LocalThemeStylesheet stylesheet) {
            this.stylesheet = Objects.requireNonNull(stylesheet, "stylesheet");
        }

        /// Removes owned stylesheet state and restores the root context captured before installation.
        private void uninstall(Parent root) {
            if (stylesheet.owned()) {
                root.getStylesheets().remove(stylesheet.url());
            }
            previousContext.restore(root);
        }
    }

    /// Describes one generated stylesheet associated with a local parent theme.
    ///
    /// @param url   the generated stylesheet URL
    /// @param owned whether M3FX inserted the URL into the parent stylesheet list
    @NotNullByDefault
    private record LocalThemeStylesheet(String url, boolean owned) {
        /// Creates validated local stylesheet metadata.
        private LocalThemeStylesheet {
            Objects.requireNonNull(url, "url");
        }
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

    /// Returns the scene installation that currently owns the supplied parent as its root.
    private static @Nullable SceneThemeInstallation activeSceneInstallation(Parent root) {
        @Nullable Scene scene = root.getScene();
        if (scene == null || scene.getRoot() != root || !scene.hasProperties()) {
            return null;
        }
        @Nullable Object value = scene.getProperties().get(SCENE_THEME_INSTALLATION_KEY);
        return value instanceof SceneThemeInstallation installation ? installation : null;
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
                applyTheme(root, theme);
            } else {
                installRoot(root, theme);
            }
        }

        /// Installs a local theme beneath the scene override without changing the visible scene theme.
        private void installLocalTheme(Parent root, M3Theme localTheme) {
            updateLocalTheme(root, () -> M3ThemeRuntime.installLocalTheme(root, localTheme));
        }

        /// Removes the local theme beneath the scene override without changing the visible scene theme.
        private void uninstallLocalTheme(Parent root) {
            updateLocalTheme(root, () -> M3ThemeRuntime.uninstallLocalTheme(root));
        }

        /// Updates retained local state, then captures it again beneath the active scene override.
        private void updateLocalTheme(Parent root, Runnable update) {
            @Nullable M3Theme currentTheme = theme;
            @Nullable RootThemeSnapshot currentSnapshot = snapshot;
            if (currentTheme == null || currentSnapshot == null || !currentSnapshot.references(root)) {
                throw new IllegalStateException("Scene theme installation does not control the supplied root");
            }

            restoreSnapshot();
            try {
                update.run();
            } finally {
                installRoot(root, currentTheme);
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
            snapshot = new RootThemeSnapshot(root);
            applyTheme(root, theme);
        }

        /// Applies one theme's metadata and semantic style classes to the scene root.
        private static void applyTheme(Parent root, M3Theme theme) {
            applyThemeStyleClasses(root, theme);
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

        /// The direct theme context that existed before the scene override.
        private final ThemeContextSnapshot previousContext;

        /// The local generated stylesheet temporarily suspended by the scene installation.
        private final @Nullable LocalThemeStylesheet localThemeStylesheet;

        /// The index from which the local generated stylesheet was removed.
        private final int localThemeStylesheetIndex;

        /// Captures current root theme state.
        private RootThemeSnapshot(Parent root) {
            rootReference = new WeakReference<>(root);
            previousContext = ThemeContextSnapshot.capture(root);
            localThemeStylesheet = M3ThemeRuntime.localThemeStylesheet(root);
            if (localThemeStylesheet != null && localThemeStylesheet.owned()) {
                localThemeStylesheetIndex = root.getStylesheets().indexOf(localThemeStylesheet.url());
                if (localThemeStylesheetIndex >= 0) {
                    root.getStylesheets().remove(localThemeStylesheetIndex);
                }
            } else {
                localThemeStylesheetIndex = -1;
            }
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
            previousContext.restore(root);
            if (localThemeStylesheet != null
                    && localThemeStylesheet.owned()
                    && localThemeStylesheetIndex >= 0
                    && !root.getStylesheets().contains(localThemeStylesheet.url())) {
                root.getStylesheets().add(
                        Math.min(localThemeStylesheetIndex, root.getStylesheets().size()),
                        localThemeStylesheet.url()
                );
            }
        }
    }
}
