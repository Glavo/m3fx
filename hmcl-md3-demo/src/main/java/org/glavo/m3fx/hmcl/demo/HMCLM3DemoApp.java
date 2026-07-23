// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;
import java.util.Objects;

/// Runs the HMCL-inspired Material 3 launcher demonstration.
///
/// Window metrics follow HMCL `Controllers` defaults:
/// content 802×492 plus 8px shadow padding on each side → 818×508.
@NotNullByDefault
public final class HMCLM3DemoApp extends Application {
    /// The JavaFX system property controlling LCD subpixel text rendering.
    private static final String LCD_TEXT_PROPERTY = "prism.lcdtext";

    /// The output scale above which Windows uses grayscale text antialiasing by default.
    private static final double SCALED_OUTPUT_THRESHOLD = 1.0;

    /// HMCL `MIN_WIDTH` including custom decoration shadow extent.
    private static final double WINDOW_WIDTH = 818.0;

    /// HMCL `MIN_HEIGHT` including custom decoration shadow extent.
    private static final double WINDOW_HEIGHT = 508.0;

    /// Creates an application instance.
    public HMCLM3DemoApp() {
    }

    /// Creates the localized HMCL shell and Material theme.
    ///
    /// @param stage the JavaFX primary stage
    @Override
    public void start(Stage stage) {
        configureFontAntialiasing();
        stage.initStyle(StageStyle.TRANSPARENT);

        HMCLDemoStrings strings = new HMCLDemoStrings();
        HMCLDemoState state = new HMCLDemoState(strings);
        M3OverlayPane root = new M3OverlayPane();
        // Overlay min/pref sizes must not follow page lists; the shell owns window metrics.
        root.setMinSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setContent(new HMCLDemoShell(root, strings, state));

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Objects.requireNonNull(
                HMCLM3DemoApp.class.getResource("hmcl-md3-demo.css"),
                "HMCL demo stylesheet"
        ).toExternalForm());

        Runnable applyTheme = () -> M3ThemeManager.install(scene, M3Theme.fromSeed(
                state.getThemeColor(),
                M3Profile.EXPRESSIVE_2025,
                resolveBrightness(state.getBrightness()),
                M3Density.standard()
        ));
        state.themeColorProperty().addListener((observable, oldColor, newColor) -> applyTheme.run());
        state.brightnessProperty().addListener((observable, oldBrightness, newBrightness) -> applyTheme.run());
        applyTheme.run();

        stage.setScene(scene);
        stage.setTitle(strings.get("app.title"));
        stage.getIcons().add(HMCLDemoAssets.image("img/icon.png"));
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        strings.localeProperty().addListener(
                (observable, oldLocale, newLocale) -> stage.setTitle(strings.get("app.title"))
        );
        stage.show();
        // Keep the initial HMCL-sized window even if later page content reports a larger preferred size.
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
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

    /// Disables LCD subpixel antialiasing on scaled Windows primary displays unless explicitly configured.
    private static void configureFontAntialiasing() {
        if (System.getProperty(LCD_TEXT_PROPERTY) != null) {
            return;
        }

        String operatingSystemName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (operatingSystemName.startsWith("windows")
                && Screen.getPrimary().getOutputScaleX() > SCALED_OUTPUT_THRESHOLD) {
            System.setProperty(LCD_TEXT_PROPERTY, Boolean.FALSE.toString());
        }
    }
}
