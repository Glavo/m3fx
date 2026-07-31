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

/// Resolves whether a JavaFX node or window can currently produce user-visible rendered output.
///
/// Node render activity requires the node and every ancestor to have [Node#visibleProperty()] set to `true`, scene
/// attachment, and a render-active window. A window and every popup owner must be showing, and the presentation
/// [Stage] must not be iconified. Window focus is not required because an unfocused window remains visible.
///
/// Managed state, opacity, clipping, viewport intersection, and disabled state are intentionally excluded. They do not
/// determine whether JavaFX may render the node, and treating them as visibility would break valid animation and
/// presentation behavior such as fades from zero opacity.
///
/// If the JavaFX module does not expose its tree-visible property, node visibility silently falls back to the public
/// [Node] parent chain. That fallback cannot observe an enclosing [javafx.scene.SubScene] across its root boundary.
@NotNullByDefault
public final class M3PresentationActivity {
    /// Prevents utility class instantiation.
    private M3PresentationActivity() {
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

    /// Returns whether the node belongs to a visible tree in a render-active window.
    ///
    /// @param owner the node whose ancestor chain, scene, and window should be inspected
    /// @return `true` when the node is effectively visible and presented by a render-active window
    /// @throws NullPointerException if `owner` is `null`
    public static boolean isRenderActive(Node owner) {
        Objects.requireNonNull(owner, "owner");
        if (!isTreeVisible(owner)) {
            return false;
        }
        @Nullable Scene scene = owner.getScene();
        return scene != null && isRenderActive(scene.getWindow());
    }

    /// Returns the best available indication that the node and its tree ancestors are visible.
    ///
    /// Scene and window attachment are not required.
    ///
    /// @param node the node whose visible ancestor chain should be inspected, or `null`
    /// @return `true` when every ancestor visible to the selected native or fallback implementation is visible
    public static boolean isTreeVisible(@Nullable Node node) {
        return M3TreeVisibility.isTreeVisible(node);
    }

    /// Returns the stage that presents a window, following popup ownership when necessary.
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
