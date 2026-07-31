// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Resolves whether a JavaFX window can currently produce user-visible rendered output.
///
/// Render activity requires the window and every popup owner to be showing. The presentation [Stage] must additionally
/// not be iconified. Window focus is not required because an unfocused window remains visible and should continue
/// rendering. This state is intended for pulse-driven visual work and must not be used as a substitute for scene-graph
/// visibility or input reachability.
@NotNullByDefault
public final class M3WindowActivity {
    /// Prevents utility class instantiation.
    private M3WindowActivity() {
    }

    /// Returns whether the window can currently produce visible rendered output.
    ///
    /// A popup inherits the render activity of its owner-window chain because it cannot be independently iconified
    /// through the JavaFX API.
    ///
    /// @param window the window to inspect, or `null`
    /// @return `true` when the window and its owners are showing and the presentation stage is not iconified
    public static boolean isRenderActive(@Nullable Window window) {
        @Nullable Window current = window;
        while (current != null) {
            if (!current.isShowing()) {
                return false;
            }
            if (current instanceof Stage stage) {
                return !stage.isIconified();
            }
            if (current instanceof PopupWindow popup) {
                current = popup.getOwnerWindow();
            } else {
                return true;
            }
        }
        return false;
    }

    /// Returns whether the node belongs to a render-active window.
    ///
    /// @param owner the node whose scene and window should be inspected
    /// @return `true` when the node is attached to a scene presented by a render-active window
    /// @throws NullPointerException if `owner` is `null`
    public static boolean isRenderActive(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Scene scene = owner.getScene();
        return scene != null && isRenderActive(scene.getWindow());
    }

    /// Returns the stage that ultimately presents a window, following popup ownership when necessary.
    ///
    /// @param window the window whose presentation stage should be resolved, or `null`
    /// @return the presenting stage, or `null` when the window has no stage owner
    static @Nullable Stage presentationStage(@Nullable Window window) {
        @Nullable Window current = window;
        while (current instanceof PopupWindow popup) {
            current = popup.getOwnerWindow();
        }
        return current instanceof Stage stage ? stage : null;
    }
}
