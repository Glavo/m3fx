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
    private static final String FALLBACK_STYLESHEET_LISTENER_KEY =
            M3ControlStyles.class.getName() + ".fallbackStylesheetListener";

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

        FallbackStylesheetInstallation installation = new FallbackStylesheetInstallation(node);
        node.getProperties().put(FALLBACK_STYLESHEET_LISTENER_KEY, installation);
        installation.install();
    }

    /// Tracks the scene root that needs standalone fallback token declarations for one control.
    @NotNullByDefault
    private static final class FallbackStylesheetInstallation {
        /// The control that requested standalone fallback token support.
        private final Node node;

        /// The listener that follows the control between scenes.
        private final ChangeListener<@Nullable Scene> sceneListener =
                (observable, oldScene, newScene) -> updateObservedScene(newScene);

        /// The listener that follows replacement roots inside the current scene.
        private final ChangeListener<Parent> rootListener =
                (observable, oldRoot, newRoot) -> applyFallbackStylesheet();

        /// The scene whose root listener is currently installed.
        private @Nullable Scene observedScene;

        /// Creates a fallback stylesheet installation for the requested node.
        private FallbackStylesheetInstallation(Node node) {
            this.node = node;
        }

        /// Starts listening to the node and applies fallback styles to its current scene when present.
        private void install() {
            node.sceneProperty().addListener(sceneListener);
            updateObservedScene(node.getScene());
        }

        /// Moves the root listener from the previous scene to the new scene.
        private void updateObservedScene(@Nullable Scene scene) {
            if (observedScene != null) {
                observedScene.rootProperty().removeListener(rootListener);
            }

            observedScene = scene;
            if (scene != null) {
                scene.rootProperty().addListener(rootListener);
                applyFallbackStylesheet();
            }
        }

        /// Applies fallback styles when the node still belongs to the observed scene root.
        private void applyFallbackStylesheet() {
            @Nullable Scene scene = observedScene;
            if (scene != null && node.getScene() == scene && isInsideSceneRoot(scene)) {
                addFallbackStylesheet(scene);
            }
        }

        /// Returns whether the node is contained by the current scene root.
        private boolean isInsideSceneRoot(Scene scene) {
            Parent root = scene.getRoot();
            @Nullable Node current = node;
            while (current != null) {
                if (current == root) {
                    return true;
                }
                current = current.getParent();
            }
            return false;
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
