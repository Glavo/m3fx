// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.theme.M3Theme;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Internal helpers for resolving the installed M3FX theme that controls a node.
@NotNullByDefault
public final class M3ThemeResolver {
    /// Prevents instantiation.
    private M3ThemeResolver() {
    }

    /// Finds the nearest installed theme for an owner node.
    ///
    /// The scene root is checked first so scene-level installation remains authoritative. If the
    /// owner is detached or the scene has no installed theme, the owner and its parent chain are
    /// checked for directly installed theme metadata.
    ///
    /// @param owner the node whose theme should be resolved
    /// @return the nearest installed theme, or `null` when no theme controls the node
    public static @Nullable M3Theme findTheme(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Scene scene = owner.getScene();
        if (scene != null) {
            @Nullable M3Theme sceneTheme = findTheme(scene);
            if (sceneTheme != null) {
                return sceneTheme;
            }
        }

        @Nullable Node current = owner;
        while (current != null) {
            if (current instanceof Parent parent) {
                @Nullable M3Theme parentTheme = M3ThemeMetadata.getTheme(parent);
                if (parentTheme != null) {
                    return parentTheme;
                }
            }
            current = current.getParent();
        }
        return null;
    }

    /// Finds the root whose installed theme context should be copied into detached popup content.
    ///
    /// Scene-level installation is preferred so popup content receives the same root declarations as the owner
    /// scene. If the scene has no installed theme, the owner and parent chain are searched for a locally installed
    /// theme root.
    ///
    /// @param owner the node that owns the popup content
    /// @return the root that supplies theme declarations, or `null` when no theme root is available
    public static @Nullable Parent findThemeRoot(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Scene scene = owner.getScene();
        if (scene != null && findTheme(scene) != null) {
            return scene.getRoot();
        }

        @Nullable Node current = owner;
        while (current != null) {
            if (current instanceof Parent parent && M3ThemeMetadata.getTheme(parent) != null) {
                return parent;
            }
            current = current.getParent();
        }
        return null;
    }

    /// Finds the theme installed on a scene root.
    ///
    /// @param scene the scene whose root theme should be resolved
    /// @return the installed scene theme, or `null` when the scene has no theme metadata
    public static @Nullable M3Theme findTheme(Scene scene) {
        Objects.requireNonNull(scene, "scene");
        return M3ThemeMetadata.getTheme(scene.getRoot());
    }
}
