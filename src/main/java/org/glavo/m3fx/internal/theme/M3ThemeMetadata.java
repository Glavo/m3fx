// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import javafx.scene.Parent;
import org.glavo.m3fx.internal.IdentityKey;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Stores and identifies internal M3FX theme metadata on JavaFX roots.
@NotNullByDefault
public final class M3ThemeMetadata {
    /// The JavaFX properties map key used for installed theme metadata.
    private static final IdentityKey THEME_PROPERTY_KEY =
            new IdentityKey("org.glavo.m3fx.theme.M3ThemeManager.theme");

    /// Prevents utility class instantiation.
    private M3ThemeMetadata() {
    }

    /// Stores installed theme metadata on a root.
    ///
    /// @param root  the root receiving theme metadata
    /// @param theme the installed theme
    public static void setTheme(Parent root, M3Theme theme) {
        Parent checkedRoot = Objects.requireNonNull(root, "root");
        M3Theme checkedTheme = Objects.requireNonNull(theme, "theme");
        if (getTheme(checkedRoot) == checkedTheme && hasTheme(checkedRoot)) {
            return;
        }
        checkedRoot.getProperties().put(THEME_PROPERTY_KEY, checkedTheme);
        M3MotionSettingsObserver.motionContextChanged(checkedRoot);
    }

    /// Returns installed theme metadata from a root.
    ///
    /// @param root the root to query
    /// @return the installed theme, or `null` when no theme metadata is present
    public static @Nullable M3Theme getTheme(Parent root) {
        Parent checkedRoot = Objects.requireNonNull(root, "root");
        if (!checkedRoot.hasProperties()) {
            return null;
        }
        Object theme = checkedRoot.getProperties().get(THEME_PROPERTY_KEY);
        return theme instanceof M3Theme materialTheme ? materialTheme : null;
    }

    /// Returns whether a root has installed theme metadata.
    ///
    /// @param root the root to inspect
    /// @return `true` when the root has installed theme metadata
    public static boolean hasTheme(Parent root) {
        Parent checkedRoot = Objects.requireNonNull(root, "root");
        return checkedRoot.hasProperties() && checkedRoot.getProperties().containsKey(THEME_PROPERTY_KEY);
    }

    /// Removes installed theme metadata from a root.
    ///
    /// @param root the root whose theme metadata should be removed
    public static void clearTheme(Parent root) {
        Parent checkedRoot = Objects.requireNonNull(root, "root");
        if (checkedRoot.hasProperties() && checkedRoot.getProperties().containsKey(THEME_PROPERTY_KEY)) {
            checkedRoot.getProperties().remove(THEME_PROPERTY_KEY);
            M3MotionSettingsObserver.motionContextChanged(checkedRoot);
        }
    }

    /// Returns whether a JavaFX properties map key identifies installed theme metadata.
    ///
    /// @param key the properties map key to inspect
    /// @return `true` when the key identifies installed theme metadata
    public static boolean isThemePropertyKey(Object key) {
        return THEME_PROPERTY_KEY == key;
    }
}
