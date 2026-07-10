// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ChangeListener;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Provides shared style-class helpers for M3FX controls.
@NotNullByDefault
public final class M3ControlStyles {
    /// The standard JavaFX scene root style class used by fallback token declarations.
    private static final String ROOT_STYLE_CLASS = "root";

    /// The node property key used to mark fallback stylesheet listener installation.
    private static final Object FALLBACK_STYLESHEET_LISTENER_KEY = new Object();

    /// The scene property key used to retain the single fallback stylesheet installation for that scene.
    private static final Object FALLBACK_STYLESHEET_INSTALLATION_KEY = new Object();

    /// The shared listener that installs fallback tokens when any marked control enters a scene.
    private static final ChangeListener<@Nullable Scene> FALLBACK_SCENE_LISTENER =
            (observable, oldScene, newScene) -> {
                if (newScene != null) {
                    installFallbackStylesheet(newScene);
                }
            };

    /// Prevents utility class instantiation.
    private M3ControlStyles() {
    }

    /// Adds a style class if it is not already present.
    public static void add(Styleable node, String styleClass) {
        List<String> styleClasses = node.getStyleClass();
        if (!styleClasses.contains(styleClass)) {
            styleClasses.add(styleClass);
        }
        installFallbackStylesheet(node);
    }

    /// Replaces one variant style class with another.
    public static void replaceVariant(Styleable node, String selectedStyleClass, String... variantStyleClasses) {
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
        node.sceneProperty().addListener(FALLBACK_SCENE_LISTENER);
        @Nullable Scene scene = node.getScene();
        if (scene != null) {
            installFallbackStylesheet(scene);
        }
    }

    /// Tracks fallback token declarations and root replacement once for an entire scene.
    @NotNullByDefault
    private static final class FallbackStylesheetInstallation {
        /// The scene that owns this installation.
        private final Scene scene;

        /// The listener that applies fallback declarations to replacement roots.
        private final ChangeListener<Parent> rootListener =
                (observable, oldRoot, newRoot) -> apply();

        /// Creates a fallback stylesheet installation for one scene.
        private FallbackStylesheetInstallation(Scene scene) {
            this.scene = scene;
        }

        /// Starts following root replacement and applies the fallback declarations.
        private void install() {
            scene.rootProperty().addListener(rootListener);
            apply();
        }

        /// Applies fallback styles to the current scene and root idempotently.
        private void apply() {
            Parent root = scene.getRoot();
            List<String> styleClasses = root.getStyleClass();
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

    /// Installs or reapplies scene-level fallback token declarations.
    private static void installFallbackStylesheet(Scene scene) {
        Object value = scene.getProperties().get(FALLBACK_STYLESHEET_INSTALLATION_KEY);
        if (value instanceof FallbackStylesheetInstallation installation) {
            installation.apply();
            return;
        }

        FallbackStylesheetInstallation installation = new FallbackStylesheetInstallation(scene);
        scene.getProperties().put(FALLBACK_STYLESHEET_INSTALLATION_KEY, installation);
        installation.install();
    }
}
