// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Runs the HMCL-inspired Material 3 launcher demonstration.
@NotNullByDefault
public final class HMCLM3DemoApp extends Application {
    /// Initial scene width in logical pixels.
    private static final double INITIAL_WIDTH = 1_080.0;

    /// Initial scene height in logical pixels.
    private static final double INITIAL_HEIGHT = 720.0;

    /// Minimum scene width in logical pixels.
    private static final double MIN_WIDTH = 720.0;

    /// Minimum scene height in logical pixels.
    private static final double MIN_HEIGHT = 480.0;

    /// The active scene after startup, or `null` before creation.
    private @Nullable Scene scene;

    /// Creates an application instance.
    public HMCLM3DemoApp() {
    }

    /// Creates the adaptive HMCL shell and Material theme.
    ///
    /// @param stage the JavaFX primary stage
    @Override
    public void start(Stage stage) {
        HMCLDemoStrings strings = new HMCLDemoStrings();
        HMCLDemoState state = new HMCLDemoState(strings);
        M3OverlayPane root = new M3OverlayPane();
        root.getStyleClass().add("hmcl-demo-root");
        root.setContent(new HMCLDemoShell(root, strings, state));

        Scene activeScene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
        activeScene.getStylesheets().add(Objects.requireNonNull(
                HMCLM3DemoApp.class.getResource("hmcl-md3-demo.css"),
                "HMCL demo stylesheet"
        ).toExternalForm());
        scene = activeScene;

        Runnable applyTheme = this::applyTheme;
        state.themeColorProperty().addListener((observable, oldColor, newColor) -> applyTheme.run());
        state.brightnessProperty().addListener((observable, oldBrightness, newBrightness) -> applyTheme.run());
        state.profileProperty().addListener((observable, oldProfile, newProfile) -> applyTheme.run());
        state.animationDisabledProperty().addListener((observable, oldValue, newValue) ->
                M3MotionSettings.setGlobalReducedMotionRequested(Boolean.TRUE.equals(newValue)));
        M3MotionSettings.setGlobalReducedMotionRequested(state.isAnimationDisabled());
        applyTheme.run();

        stage.setScene(activeScene);
        stage.setTitle(strings.get("app.title"));
        stage.getIcons().add(HMCLDemoAssets.image("img/icon.png"));
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setWidth(INITIAL_WIDTH);
        stage.setHeight(INITIAL_HEIGHT);
        strings.localeProperty().addListener(
                (observable, oldLocale, newLocale) -> stage.setTitle(strings.get("app.title"))
        );
        stage.show();
    }

    /// Installs the Material theme for the current appearance settings.
    private void applyTheme() {
        @Nullable Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }
        // Recover theme inputs from the shell state by reading the scene graph root content.
        if (!(activeScene.getRoot() instanceof M3OverlayPane overlay)
                || !(overlay.getContent() instanceof HMCLDemoShell shell)) {
            return;
        }
        HMCLDemoState state = shell.state();
        M3ThemeManager.install(activeScene, M3Theme.fromSeed(
                state.getThemeColor(),
                state.getProfile(),
                resolveBrightness(state.getBrightness()),
                M3Density.standard()
        ));
    }

    /// Maps demo brightness settings onto MonetFX brightness values.
    ///
    /// @param brightness the demo brightness mode
    /// @return the MonetFX brightness
    private static Brightness resolveBrightness(HMCLDemoState.Brightness brightness) {
        return switch (brightness) {
            case DARK -> Brightness.DARK;
            case LIGHT, SYSTEM -> Brightness.LIGHT;
        };
    }
}
