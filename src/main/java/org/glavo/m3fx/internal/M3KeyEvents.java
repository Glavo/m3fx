// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNullByDefault;

/// Provides the keyboard-modifier policies shared by Material navigation containers.
@NotNullByDefault
public final class M3KeyEvents {
    /// Prevents utility class instantiation.
    private M3KeyEvents() {
    }

    /// Returns whether a navigation key event should be left to application shortcuts or platform editing behavior.
    ///
    /// Navigation handlers use this result to avoid consuming directional and paging keys when any conventional
    /// modifier is active.
    ///
    /// @param event the key event to inspect
    /// @return `true` when Shift, Control, Alt, Meta, or the platform shortcut modifier is down
    /// @throws NullPointerException if `event` is `null`
    public static boolean hasNavigationModifier(KeyEvent event) {
        return event.isShiftDown()
                || event.isControlDown()
                || event.isAltDown()
                || event.isMetaDown()
                || event.isShortcutDown();
    }

    /// Returns whether a type-ahead key event should be left to application shortcuts.
    ///
    /// Shift is intentionally ignored because it participates in normal character entry.
    ///
    /// @param event the key event to inspect
    /// @return `true` when Control, Alt, Meta, or the platform shortcut modifier is down
    /// @throws NullPointerException if `event` is `null`
    public static boolean hasShortcutModifier(KeyEvent event) {
        return event.isControlDown()
                || event.isAltDown()
                || event.isMetaDown()
                || event.isShortcutDown();
    }
}