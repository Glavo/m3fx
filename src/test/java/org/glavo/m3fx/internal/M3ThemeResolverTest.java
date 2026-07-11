// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/// Tests installed theme resolution for scene-attached and detached nodes.
@NotNullByDefault
final class M3ThemeResolverTest {
    /// Starts the JavaFX toolkit before scene-level theme tests create scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that a scene-installed theme controls scene-attached nodes.
    @Test
    void sceneThemeTakesPrecedenceForAttachedNodes() {
        Pane root = new Pane();
        Pane child = new Pane();
        root.getChildren().add(child);
        Scene scene = new Scene(root);
        M3Theme parentTheme = M3Theme.fromSeed(Color.web("#6750a4"), M3Profile.BASELINE_2021, Brightness.LIGHT);
        M3Theme sceneTheme = M3Theme.fromSeed(Color.web("#006a6a"), M3Profile.EXPRESSIVE_2025, Brightness.DARK);

        M3ThemeManager.install(root, parentTheme);
        M3ThemeManager.install(scene, sceneTheme);

        assertSame(sceneTheme, M3ThemeResolver.findTheme(scene));
        assertSame(sceneTheme, M3ThemeResolver.findTheme(child));
    }

    /// Verifies that detached nodes resolve themes from their parent chain.
    @Test
    void detachedNodeResolvesThemeFromParentChain() {
        Pane root = new Pane();
        Pane child = new Pane();
        root.getChildren().add(child);
        M3Theme theme = M3Theme.defaultTheme();

        assertNull(M3ThemeResolver.findTheme(child));

        M3ThemeManager.install(root, theme);

        assertSame(theme, M3ThemeResolver.findTheme(child));
        assertSame(root, M3ThemeResolver.findThemeRoot(child));
    }

    /// Verifies that scene roots are preferred as popup theme sources when a scene theme is installed.
    @Test
    void sceneRootSuppliesPopupThemeContext() {
        Pane root = new Pane();
        Pane localRoot = new Pane();
        Pane child = new Pane();
        root.getChildren().add(localRoot);
        localRoot.getChildren().add(child);
        Scene scene = new Scene(root);
        M3Theme sceneTheme = M3Theme.defaultTheme();
        M3Theme localTheme = M3Theme.fromSeed(Color.web("#006a6a"), M3Profile.EXPRESSIVE_2025, Brightness.DARK);

        M3ThemeManager.install(scene, sceneTheme);
        M3ThemeManager.install(localRoot, localTheme);

        assertSame(root, M3ThemeResolver.findThemeRoot(child));
    }

    /// Verifies that local parent themes supply popup context when the scene has no installed theme.
    @Test
    void localParentSuppliesPopupThemeContextWithoutSceneTheme() {
        Pane root = new Pane();
        Pane localRoot = new Pane();
        Pane child = new Pane();
        root.getChildren().add(localRoot);
        localRoot.getChildren().add(child);
        Scene scene = new Scene(root);
        M3Theme localTheme = M3Theme.defaultTheme();

        M3ThemeManager.install(localRoot, localTheme);

        assertSame(localRoot, M3ThemeResolver.findThemeRoot(child));
    }


    /// Verifies absent theme and motion queries do not allocate properties on the owner chain.
    @Test
    void absentThemeQueriesDoNotAllocateNodeProperties() {
        Pane root = new Pane();
        Pane child = new Pane();
        Pane nested = new Pane();
        root.getChildren().add(child);
        child.getChildren().add(nested);
        Scene scene = new Scene(root);

        assertFalse(scene.hasProperties());
        assertFalse(root.hasProperties());
        assertFalse(child.hasProperties());
        assertFalse(nested.hasProperties());

        assertNull(M3ThemeResolver.findTheme(nested));
        assertNull(M3ThemeResolver.findThemeRoot(nested));
        M3Animation.defaultEffects(nested);
        M3Animation.motionBehavior(nested);
        M3ThemeManager.uninstall(scene);

        assertFalse(scene.hasProperties());
        assertFalse(root.hasProperties());
        assertFalse(child.hasProperties());
        assertFalse(nested.hasProperties());
    }}
