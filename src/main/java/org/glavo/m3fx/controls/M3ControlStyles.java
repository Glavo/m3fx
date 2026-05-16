// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.Styleable;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

/// Provides shared style-class helpers for m3fx controls.
@NotNullByDefault
final class M3ControlStyles {
    /// Prevents utility class instantiation.
    private M3ControlStyles() {
    }

    /// Adds a style class if it is not already present.
    static void add(Styleable node, String styleClass) {
        List<String> styleClasses = node.getStyleClass();
        if (!styleClasses.contains(styleClass)) {
            styleClasses.add(styleClass);
        }
    }

    /// Replaces one variant style class with another.
    static void replaceVariant(Styleable node, String selectedStyleClass, String... variantStyleClasses) {
        List<String> styleClasses = node.getStyleClass();
        for (String styleClass : variantStyleClasses) {
            styleClasses.remove(styleClass);
        }
        if (!styleClasses.contains(selectedStyleClass)) {
            styleClasses.add(selectedStyleClass);
        }
    }
}
