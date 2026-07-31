// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.stage.PopupWindow;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Provides guarded popup-window lifecycle operations.
///
/// JavaFX records a popup owner before checking whether the owner tree and root window can present the popup. Calling
/// [PopupWindow#show(Node, double, double)] for an invisible owner or a scene attached to a hidden or iconified stage
/// can therefore leave a non-showing popup retaining its owner. This helper rejects that state before JavaFX mutates
/// the popup and tears down transient owner listeners when a show request is rejected during popup events.
@NotNullByDefault
public final class M3PopupWindows {
    /// Prevents utility class instantiation.
    private M3PopupWindows() {
    }

    /// Returns whether an owner node belongs to a render-active window.
    ///
    /// @param owner the prospective popup owner
    /// @return `true` when JavaFX can present a popup for the owner
    /// @throws NullPointerException if `owner` is `null`
    public static boolean canShow(Node owner) {
        return M3PresentationActivity.isRenderActive(Objects.requireNonNull(owner, "owner"));
    }

    /// Shows a popup only for a render-active owner window and confirms that it remained visible.
    ///
    /// If JavaFX or a popup event handler rejects the show request, this method calls [PopupWindow#hide()] to
    /// release transient JavaFX owner listeners before returning or rethrowing the failure.
    ///
    /// @param popup the popup to show
    /// @param owner the owner node
    /// @param anchorX the screen x coordinate for the popup anchor
    /// @param anchorY the screen y coordinate for the popup anchor
    /// @return `true` when the popup is showing after the request
    /// @throws NullPointerException if `popup` or `owner` is `null`
    public static boolean show(PopupWindow popup, Node owner, double anchorX, double anchorY) {
        Objects.requireNonNull(popup, "popup");
        if (!canShow(owner)) {
            return false;
        }

        try {
            popup.show(owner, anchorX, anchorY);
        } catch (RuntimeException | Error exception) {
            cleanupAfterRejectedShow(popup, exception);
            throw exception;
        }
        if (popup.isShowing()) {
            return true;
        }

        popup.hide();
        return false;
    }

    /// Releases popup owner bookkeeping while preserving an original show failure.
    private static void cleanupAfterRejectedShow(PopupWindow popup, Throwable failure) {
        try {
            popup.hide();
        } catch (RuntimeException | Error cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }
}
