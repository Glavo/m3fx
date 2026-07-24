// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/// Runs the HMCL-inspired Material 3 launcher demonstration.
///
/// The stage starts at the classic HMCL size (content 802×492 + 8px shadow padding → 818×508) but may shrink down to
/// the shell minimum and grow freely; list content never drives stage min/pref size.
@NotNullByDefault
public final class HMCLM3DemoApp extends Application {
    /// The JavaFX system property controlling LCD subpixel text rendering.
    private static final String LCD_TEXT_PROPERTY = "prism.lcdtext";

    /// The output scale above which Windows uses grayscale text antialiasing by default.
    private static final double SCALED_OUTPUT_THRESHOLD = 1.0;

    /// The active scene after startup, or `null` before creation.
    private @Nullable Scene scene;

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
        double minWidth = HMCLDemoShell.minWindowWidth();
        double minHeight = HMCLDemoShell.minWindowHeight();
        double prefWidth = HMCLDemoShell.prefWindowWidth();
        double prefHeight = HMCLDemoShell.prefWindowHeight();

        M3OverlayPane root = new M3OverlayPane();
        // Overlay min/pref sizes must not follow page lists; the shell owns window metrics.
        root.setMinSize(minWidth, minHeight);
        root.setPrefSize(prefWidth, prefHeight);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.getStyleClass().add("hmcl-demo-root");
        root.setContent(new HMCLDemoShell(root, strings, state));

        Scene activeScene = new Scene(root, prefWidth, prefHeight);
        activeScene.setFill(Color.TRANSPARENT);
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
        stage.setResizable(true);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setWidth(prefWidth);
        stage.setHeight(prefHeight);
        strings.localeProperty().addListener(
                (observable, oldLocale, newLocale) -> stage.setTitle(strings.get("app.title"))
        );
        stage.show();
        // Keep the initial HMCL-sized window even if later page content reports a larger preferred size.
        stage.setWidth(prefWidth);
        stage.setHeight(prefHeight);
    }

    /// Installs the Material theme for the current appearance settings.
    private void applyTheme() {
        @Nullable Scene activeScene = scene;
        if (activeScene == null) {
            return;
        }
        if (!(activeScene.getRoot() instanceof M3OverlayPane overlay)
                || !(overlay.getContent() instanceof HMCLDemoShell shell)) {
            return;
        }
        HMCLDemoState state = shell.state();
        M3ThemeManager.install(
                activeScene,
                HMCLDemoTheme.create(
                        state.getThemeColor(),
                        state.getProfile(),
                        resolveBrightness(state.getBrightness())
                )
        );
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
