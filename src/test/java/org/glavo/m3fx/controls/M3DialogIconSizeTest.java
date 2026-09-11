// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies dialog icon token updates without acquiring application ownership of the graphic size.
@NotNullByDefault
final class M3DialogIconSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Keeps successive Java token updates effective before and after CSS initialization.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void successiveTokenUpdatesReachGraphic(boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            M3IconGraphic icon = createIcon(svg);
            M3DialogPane pane = new M3DialogPane();
            pane.setIconSize(32.0);
            pane.setGraphic((Node) icon);
            assertEquals(32.0, icon.iconSizeProperty().get());
            pane.setIconSize(40.0);
            assertEquals(40.0, icon.iconSizeProperty().get());
            StackPane root = attach(pane);
            assertEquals(40.0, icon.iconSizeProperty().get());
            pane.setIconSize(48.0);
            root.applyCss();
            assertEquals(48.0, icon.iconSizeProperty().get());
        });
    }

    /// Keeps repeated stylesheet token changes effective on an attached graphic.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void successiveCssTokenUpdatesReachGraphic(boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            M3IconGraphic icon = createIcon(svg);
            M3DialogPane pane = new M3DialogPane();
            pane.setGraphic((Node) icon);
            pane.setStyle("-m3-dialog-icon-size: 32px;");
            StackPane root = attach(pane);
            assertEquals(32.0, icon.iconSizeProperty().get());
            pane.setStyle("-m3-dialog-icon-size: 40px;");
            root.applyCss();
            assertEquals(40.0, icon.iconSizeProperty().get());
        });
    }

    /// Preserves explicit equal-valued assignments made before or after dialog management begins.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void explicitGraphicSizeTakesPrecedence(boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            M3IconGraphic icon = createIcon(svg);
            M3DialogPane pane = new M3DialogPane();
            icon.iconSizeProperty().set(icon.iconSizeProperty().get());
            pane.setGraphic((Node) icon);
            pane.setIconSize(32.0);
            assertEquals(24.0, icon.iconSizeProperty().get());
            M3IconGraphic replacement = createIcon(svg);
            pane.setGraphic((Node) replacement);
            replacement.iconSizeProperty().set(replacement.iconSizeProperty().get());
            pane.setIconSize(40.0);
            attach(pane);
            assertEquals(32.0, replacement.iconSizeProperty().get());
        });
    }

    /// Leaves a bound graphic size under the application's control.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void boundGraphicSizeTakesPrecedence(boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            M3IconGraphic icon = createIcon(svg);
            SimpleDoubleProperty size = new SimpleDoubleProperty(28.0);
            icon.iconSizeProperty().bind(size);
            M3DialogPane pane = new M3DialogPane();
            pane.setGraphic((Node) icon);
            pane.setIconSize(40.0);
            attach(pane);
            assertEquals(28.0, icon.iconSizeProperty().get());
            size.set(36.0);
            assertEquals(36.0, icon.iconSizeProperty().get());
        });
    }

    /// Uses the dialog token consistently instead of allowing graphic CSS to replace container sizing.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void dialogTokenTakesPrecedenceOverGraphicCss(boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            M3IconGraphic icon = createIcon(svg);
            ((Node) icon).setStyle("-m3-icon-size: 28px;");
            M3DialogPane pane = new M3DialogPane();
            pane.setGraphic((Node) icon);
            StackPane root = attach(pane);
            assertEquals(24.0, icon.iconSizeProperty().get());
            pane.setIconSize(40.0);
            assertEquals(40.0, icon.iconSizeProperty().get());
            root.applyCss();
            assertEquals(40.0, icon.iconSizeProperty().get());
            pane.setGraphic(null);
            root.getChildren().setAll((Node) icon);
            root.applyCss();
            assertEquals(28.0, icon.iconSizeProperty().get());
        });
    }

    /// Creates either supported graphic representation with its default size.
    private static M3IconGraphic createIcon(boolean svg) {
        return svg ? new M3SVGIcon("M0 0 H24 V24 H0 Z") : new M3Icon("info");
    }

    /// Attaches the dialog to a themed scene and resolves its user-agent and application styles.
    private static StackPane attach(M3DialogPane pane) {
        StackPane root = new StackPane(pane);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.assertNoCssWarnings(root::applyCss);
        return root;
    }
}
