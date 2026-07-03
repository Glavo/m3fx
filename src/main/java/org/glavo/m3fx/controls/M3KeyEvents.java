// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNullByDefault;

/// Provides shared keyboard modifier policies for Material navigation containers.
@NotNullByDefault
final class M3KeyEvents {
    /// Prevents utility class instantiation.
    private M3KeyEvents() {
    }

    /// Returns whether a navigation key event should be left to application shortcuts or platform editing behavior.
    static boolean hasNavigationModifier(KeyEvent event) {
        return event.isShiftDown()
                || event.isControlDown()
                || event.isAltDown()
                || event.isMetaDown()
                || event.isShortcutDown();
    }

    /// Returns whether a type-ahead key event should be left to application shortcuts.
    static boolean hasShortcutModifier(KeyEvent event) {
        return event.isControlDown()
                || event.isAltDown()
                || event.isMetaDown()
                || event.isShortcutDown();
    }
}