package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

/// Provides shared style-class helpers for m3fx controls.
@NotNullByDefault
final class M3ControlStyles {
    /// Prevents utility class instantiation.
    private M3ControlStyles() {
    }

    /// Adds a style class if it is not already present.
    static void add(Node node, String styleClass) {
        List<String> styleClasses = node.getStyleClass();
        if (!styleClasses.contains(styleClass)) {
            styleClasses.add(styleClass);
        }
    }

    /// Replaces one variant style class with another.
    static void replaceVariant(Node node, String selectedStyleClass, String... variantStyleClasses) {
        List<String> styleClasses = node.getStyleClass();
        for (String styleClass : variantStyleClasses) {
            styleClasses.remove(styleClass);
        }
        if (!styleClasses.contains(selectedStyleClass)) {
            styleClasses.add(selectedStyleClass);
        }
    }
}
