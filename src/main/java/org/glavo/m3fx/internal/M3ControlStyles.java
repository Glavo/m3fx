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

/// Provides the common style-class and fallback-token setup used by M3FX controls.
///
/// These methods modify the supplied node directly. They do not install an [org.glavo.m3fx.theme.M3Theme]; when a
/// control is attached to an otherwise unthemed scene, they instead ensure that the bundled fallback token
/// declarations are available to that scene.
@NotNullByDefault
public final class M3ControlStyles {
    /// The standard JavaFX scene root style class used by fallback token declarations.
    private static final String ROOT_STYLE_CLASS = "root";

    /// The node property key used to mark repeatable fallback stylesheet listener installation.
    private static final IdentityKey FALLBACK_STYLESHEET_LISTENER_KEY =
            new IdentityKey(M3ControlStyles.class.getName() + ".fallbackStylesheetListener");

    /// The scene property key used to retain the single fallback stylesheet installation for that scene.
    private static final IdentityKey FALLBACK_STYLESHEET_INSTALLATION_KEY =
            new IdentityKey(M3ControlStyles.class.getName() + ".fallbackStylesheetInstallation");

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

    /// Initializes a component root with its base style class and standalone fallback styling.
    ///
    /// This method assumes that it is called once by the component constructor. Repeated calls add another scene
    /// listener even though the style class itself is not duplicated. Use [#initializeOnce(Styleable, String)] for
    /// externally owned nodes that may be configured repeatedly.
    ///
    /// @param node       the component root to initialize
    /// @param styleClass the base style class to add
    /// @throws NullPointerException if `node` or `styleClass` is `null`
    public static void initialize(Styleable node, String styleClass) {
        add(node, styleClass);
        if (node instanceof Node sceneNode) {
            observeFallbackStylesheet(sceneNode);
        }
    }

    /// Initializes an externally owned component idempotently with fallback styling.
    ///
    /// Unlike [#initialize(Styleable, String)], this method may be called repeatedly for the same node. The
    /// installation marker uses the node properties map, so M3FX controls should use the property-map-allocation-free
    /// constructor path instead.
    ///
    /// @param node       the externally owned component root to initialize
    /// @param styleClass the base style class to add
    /// @throws NullPointerException if `node` or `styleClass` is `null`
    public static void initializeOnce(Styleable node, String styleClass) {
        add(node, styleClass);
        if (node instanceof Node sceneNode
                && !sceneNode.getProperties().containsKey(FALLBACK_STYLESHEET_LISTENER_KEY)) {
            sceneNode.getProperties().put(FALLBACK_STYLESHEET_LISTENER_KEY, Boolean.TRUE);
            observeFallbackStylesheet(sceneNode);
        }
    }

    /// Adds a style class without installing scene-level component infrastructure.
    ///
    /// No change is made when the node already contains the style class.
    ///
    /// @param node       the styleable object to update
    /// @param styleClass the style class to add
    /// @throws NullPointerException if `node` or `styleClass` is `null`
    public static void add(Styleable node, String styleClass) {
        List<String> styleClasses = node.getStyleClass();
        if (!styleClasses.contains(styleClass)) {
            styleClasses.add(styleClass);
        }
    }

    /// Replaces the known variant style classes with the selected style class.
    ///
    /// One occurrence of each known variant is removed before `selectedStyleClass` is added if absent. Existing
    /// duplicate style-class entries are not normalized.
    ///
    /// @param node                the styleable object to update
    /// @param selectedStyleClass  the variant style class that should remain selected
    /// @param variantStyleClasses all mutually exclusive variant style classes
    /// @throws NullPointerException if any argument is `null`
    public static void replaceVariant(Styleable node, String selectedStyleClass, String... variantStyleClasses) {
        List<String> styleClasses = node.getStyleClass();
        for (String styleClass : variantStyleClasses) {
            styleClasses.remove(styleClass);
        }
        if (!styleClasses.contains(selectedStyleClass)) {
            styleClasses.add(selectedStyleClass);
        }
    }

    /// Observes scene changes and installs fallback token stylesheets for controls used without an application theme.
    private static void observeFallbackStylesheet(Node node) {
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
