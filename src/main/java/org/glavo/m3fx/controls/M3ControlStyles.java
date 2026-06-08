// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

/// Provides shared style-class helpers for m3fx controls.
@NotNullByDefault
final class M3ControlStyles {
    /// The standard JavaFX scene root style class used by fallback token declarations.
    private static final String ROOT_STYLE_CLASS = "root";

    /// The node property key used to mark fallback stylesheet listener installation.
    private static final String FALLBACK_STYLESHEET_LISTENER_KEY =
            M3ControlStyles.class.getName() + ".fallbackStylesheetListener";

    /// Prevents utility class instantiation.
    private M3ControlStyles() {
    }

    /// Adds a style class if it is not already present.
    static void add(Styleable node, String styleClass) {
        List<String> styleClasses = node.getStyleClass();
        if (!styleClasses.contains(styleClass)) {
            styleClasses.add(styleClass);
        }
        installFallbackStylesheet(node);
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

    /// Installs fallback token stylesheets for controls used without an application theme.
    private static void installFallbackStylesheet(Styleable styleable) {
        if (!(styleable instanceof Node node)
                || node.getProperties().containsKey(FALLBACK_STYLESHEET_LISTENER_KEY)) {
            return;
        }

        node.getProperties().put(FALLBACK_STYLESHEET_LISTENER_KEY, Boolean.TRUE);
        node.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                addFallbackStylesheet(newScene);
            }
        });

        Scene scene = node.getScene();
        if (scene != null) {
            addFallbackStylesheet(scene);
        }
    }

    /// Adds the fallback token stylesheet to the scene at the lowest application stylesheet priority.
    private static void addFallbackStylesheet(Scene scene) {
        List<String> styleClasses = scene.getRoot().getStyleClass();
        if (!styleClasses.contains(ROOT_STYLE_CLASS)) {
            styleClasses.add(ROOT_STYLE_CLASS);
        }
        String stylesheet = M3Stylesheets.fallbackStylesheet();
        List<String> stylesheets = scene.getStylesheets();
        if (!stylesheets.contains(stylesheet)) {
            stylesheets.add(0, stylesheet);
        }
    }
}
