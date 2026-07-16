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
/// Installing on a [Scene] adds the M3FX base and generated theme stylesheets and themes the scene root.
/// Installing on a [Parent] applies the same token declarations directly to that subtree, which allows local
/// theme scopes without replacing the scene-wide theme. Reinstalling replaces the previous M3FX theme while
/// [uninstall] restores the root state captured before installation.
///
/// See [Material Design theming](https://m3.material.io/styles) and [Material Design](https://m3.material.io/).
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

    /// Prevents utility class instantiation.
    private M3ThemeManager() {
    }

    /// Installs a theme on a scene and its root.
    ///
    /// @param scene the scene to theme
    /// @param theme the Material theme to install
    public static void install(Scene scene, M3Theme theme) {
        M3ThemeRuntime.install(scene, theme);
    }

    /// Installs a theme on one parent subtree.
    ///
    /// @param root the root of the local theme scope
    /// @param theme the Material theme to install
    public static void install(Parent root, M3Theme theme) {
        M3ThemeRuntime.install(root, theme);
    }

    /// Returns the theme installed on a scene root.
    ///
    /// @param scene the scene to inspect
    /// @return the installed theme, or `null` when the scene has no M3FX theme
    public static @Nullable M3Theme getTheme(Scene scene) {
        return M3ThemeRuntime.getTheme(scene);
    }

    /// Returns the theme installed directly on a parent.
    ///
    /// @param root the local theme root to inspect
    /// @return the installed theme, or `null` when the parent has no direct M3FX theme
    public static @Nullable M3Theme getTheme(Parent root) {
        return M3ThemeRuntime.getTheme(root);
    }

    /// Removes M3FX theme state and stylesheets from a scene.
    ///
    /// @param scene the scene from which to remove the theme
    public static void uninstall(Scene scene) {
        M3ThemeRuntime.uninstall(scene);
    }

    /// Removes the directly installed M3FX theme from a parent.
    ///
    /// @param root the local theme root from which to remove the theme
    public static void uninstall(Parent root) {
        M3ThemeRuntime.uninstall(root);
    }
}
