// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Runs the HMCL-inspired Material 3 launcher demonstration.
///
/// The application is an offline presentation prototype. It never reads a real HMCL configuration or game
/// directory and keeps every interaction in deterministic in-memory state.
@NotNullByDefault
public final class HMCLM3DemoApp extends Application {
    /// The initial scene width in logical pixels.
    private static final double INITIAL_WIDTH = 1_180.0;

    /// The initial scene height in logical pixels.
    private static final double INITIAL_HEIGHT = 800.0;

    /// The minimum supported window width in logical pixels.
    private static final double MINIMUM_WIDTH = 420.0;

    /// The minimum supported window height in logical pixels.
    private static final double MINIMUM_HEIGHT = 560.0;

    /// Creates an application instance.
    public HMCLM3DemoApp() {
    }

    /// Creates the localized demo state, adaptive shell, and expressive Material theme.
    ///
    /// @param stage the JavaFX primary stage
    @Override
    public void start(Stage stage) {
        HMCLDemoStrings strings = new HMCLDemoStrings();
        HMCLDemoState state = new HMCLDemoState(strings);
        M3OverlayPane root = new M3OverlayPane();
        root.setContent(new HMCLDemoShell(root, strings, state));

        Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(
                HMCLM3DemoApp.class.getResource("hmcl-md3-demo.css"),
                "HMCL demo stylesheet"
        ).toExternalForm());

        Runnable applyTheme = () -> M3ThemeManager.install(scene, M3Theme.fromSeed(
                state.getThemeColor(),
                M3Profile.EXPRESSIVE_2025,
                state.getBrightness() == HMCLDemoState.Brightness.DARK ? Brightness.DARK : Brightness.LIGHT,
                M3Density.standard()
        ));
        state.themeColorProperty().addListener((observable, oldColor, newColor) -> applyTheme.run());
        state.brightnessProperty().addListener((observable, oldBrightness, newBrightness) -> applyTheme.run());
        applyTheme.run();

        stage.setScene(scene);
        stage.setTitle(strings.get("app.title"));
        stage.getIcons().add(HMCLDemoAssets.image("img/icon.png"));
        stage.setMinWidth(MINIMUM_WIDTH);
        stage.setMinHeight(MINIMUM_HEIGHT);
        strings.localeProperty().addListener(
                (observable, oldLocale, newLocale) -> stage.setTitle(strings.get("app.title"))
        );
        stage.show();
    }
}
