// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.theme;

import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Installs, queries, and removes Material Design 3 themes on JavaFX scene graphs.
///
/// Installing on a [Scene] themes its current root and makes the M3FX stylesheets available to the complete scene.
/// If the scene root is later replaced, the previous root is restored and the installed theme is applied to the
/// replacement root. Installing directly on a [Parent] creates a local theme scope whose declarations take
/// precedence for that parent and its descendants; it does not replace the theme installed on the containing
/// scene.
///
/// Installation is replaceable and idempotent. Installing another theme on the same target replaces the previous
/// M3FX theme rather than accumulating theme state. [uninstall] restores the root style and managed style-class
/// membership captured by the corresponding installation. Applications should therefore avoid replacing a themed
/// root's complete inline style while the installation is active if that change must survive uninstallation.
///
/// These methods mutate JavaFX scene-graph state. They must be called on the JavaFX Application Thread once the
/// affected scene graph is live, following the same threading rules as [Scene] and [Parent].
///
/// The following example installs a light theme generated from an application seed color:
///
/// ```java
/// import javafx.application.Application;
/// import javafx.scene.Scene;
/// import javafx.scene.layout.StackPane;
/// import javafx.scene.paint.Color;
/// import javafx.stage.Stage;
/// import org.glavo.m3fx.theme.M3Theme;
/// import org.glavo.m3fx.theme.M3ThemeManager;
///
/// public final class ThemedApplication extends Application {
///     @Override
///     public void start(Stage stage) {
///         Scene scene = new Scene(new StackPane(), 800, 600);
///         M3Theme theme = M3Theme.fromSeed(Color.web("#006A6A"));
///         M3ThemeManager.install(scene, theme);
///         stage.setScene(scene);
///         stage.show();
///     }
/// }
/// ```
///
/// See [Material Design theming](https://m3.material.io/styles) and [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3ThemeManager {
    /// The style class maintained on every root with an installed M3FX theme.
    public static final String ROOT_STYLE_CLASS = "m3-root";

    /// The style class maintained on roots using the baseline Material Design 3 profile.
    public static final String BASELINE_PROFILE_STYLE_CLASS = "m3-profile-baseline";

    /// The style class maintained on roots using the Material Design 3 Expressive profile.
    public static final String EXPRESSIVE_PROFILE_STYLE_CLASS = "m3-profile-expressive";

    /// The style class maintained on roots using a light color scheme.
    public static final String LIGHT_BRIGHTNESS_STYLE_CLASS = "m3-light";

    /// The style class maintained on roots using a dark color scheme.
    public static final String DARK_BRIGHTNESS_STYLE_CLASS = "m3-dark";

    /// Prevents utility class instantiation.
    private M3ThemeManager() {
    }

    /// Installs or replaces the theme on a scene and its current root.
    ///
    /// The installation remains associated with the scene when its root changes. Repeated installation with the
    /// same theme has no additional effect.
    ///
    /// @param scene the scene to theme
    /// @param theme the Material theme to install
    /// @throws NullPointerException if `scene` or `theme` is `null`
    /// @throws IllegalStateException if the theme stylesheet cannot be made available to JavaFX
    public static void install(Scene scene, M3Theme theme) {
        M3ThemeRuntime.install(scene, theme);
    }

    /// Installs or replaces a local theme on one parent subtree.
    ///
    /// A local theme takes precedence over inherited theme declarations for the subtree rooted at `root`.
    /// Reinstalling replaces the previous local theme. The installation remains attached to `root` if that
    /// parent is moved elsewhere in the scene graph.
    ///
    /// @param root the root of the local theme scope
    /// @param theme the Material theme to install
    /// @throws NullPointerException if `root` or `theme` is `null`
    public static void install(Parent root, M3Theme theme) {
        M3ThemeRuntime.install(root, theme);
    }

    /// Returns the theme currently associated with a scene's root.
    ///
    /// This method reports the root's direct theme context. It does not search ancestor windows or other scenes.
    ///
    /// @param scene the scene to inspect
    /// @return the theme associated with the current root, or `null` if none is present
    /// @throws NullPointerException if `scene` is `null`
    public static @Nullable M3Theme getTheme(Scene scene) {
        return M3ThemeRuntime.getTheme(scene);
    }

    /// Returns the theme installed directly on a parent.
    ///
    /// This method does not resolve a theme inherited from an ancestor.
    ///
    /// @param root the local theme root to inspect
    /// @return the installed theme, or `null` when the parent has no direct M3FX theme
    /// @throws NullPointerException if `root` is `null`
    public static @Nullable M3Theme getTheme(Parent root) {
        return M3ThemeRuntime.getTheme(root);
    }

    /// Removes the scene installation and restores the root state captured when it was installed.
    ///
    /// The M3FX stylesheets associated with the scene installation are removed. Calling this method when no scene
    /// theme is installed has no effect beyond clearing directly installed M3FX state from the current root.
    ///
    /// @param scene the scene from which to remove the theme
    /// @throws NullPointerException if `scene` is `null`
    public static void uninstall(Scene scene) {
        M3ThemeRuntime.uninstall(scene);
    }

    /// Removes the directly installed M3FX theme from a parent.
    ///
    /// This method is idempotent. The parent's captured pre-installation style is restored and descendants then
    /// resolve any theme inherited from an enclosing scope.
    ///
    /// @param root the local theme root from which to remove the theme
    /// @throws NullPointerException if `root` is `null`
    public static void uninstall(Parent root) {
        M3ThemeRuntime.uninstall(root);
    }
}
