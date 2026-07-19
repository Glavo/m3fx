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
    /// The owner and its parent chain are searched from nearest to farthest. A local theme therefore
    /// overrides a theme installed on the containing scene, while an attached node still reaches the
    /// scene root when no closer theme scope exists.
    ///
    /// @param owner the node whose theme should be resolved
    /// @return the nearest installed theme, or `null` when no theme controls the node
    public static @Nullable M3Theme findTheme(Node owner) {
        Objects.requireNonNull(owner, "owner");
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
        @Nullable Parent sceneRoot = sceneRoot(owner);
        return sceneRoot == null ? null : M3ThemeMetadata.getTheme(sceneRoot);
    }

    /// Finds the root whose installed theme context should be copied into detached popup content.
    ///
    /// The owner and its parent chain are searched from nearest to farthest. Returning the nearest local scope is
    /// essential for detached popup content because the popup must copy the theme that visibly controls its owner,
    /// not an enclosing scene default hidden by that local scope.
    ///
    /// @param owner the node that owns the popup content
    /// @return the root that supplies theme declarations, or `null` when no theme root is available
    public static @Nullable Parent findThemeRoot(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node current = owner;
        while (current != null) {
            if (current instanceof Parent parent && M3ThemeMetadata.getTheme(parent) != null) {
                return parent;
            }
            current = current.getParent();
        }
        @Nullable Parent sceneRoot = sceneRoot(owner);
        return sceneRoot != null && M3ThemeMetadata.getTheme(sceneRoot) != null ? sceneRoot : null;
    }

    /// Returns the containing scene root when it is outside the owner's ordinary parent chain.
    ///
    /// Nodes rendered inside a [javafx.scene.SubScene] retain the containing [Scene] but their public parent chain
    /// stops at the SubScene root. Falling back to the Scene root after the nearest-scope search keeps local SubScene
    /// themes authoritative while still inheriting the scene default.
    private static @Nullable Parent sceneRoot(Node owner) {
        @Nullable Scene scene = owner.getScene();
        return scene == null ? null : scene.getRoot();
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
