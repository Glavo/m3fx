// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3Tooltip;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.glavo.monetfx.ColorRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that tooltip theme resources remain local to their rendered content.
@NotNullByDefault
final class M3TooltipSkinTest {
    /// Starts JavaFX before constructing popup skins.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Local popup preferences can override their owner without overriding the global constraint.
    @Test
    void nativePopupResolvesOwnerAndExplicitMotionSettings() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            M3Button owner = new M3Button("Owner");
            Pane parent = new Pane(owner);
            Stage stage = new Stage();
            M3Tooltip tooltip = new M3Tooltip("Motion settings");
            try {
                stage.setScene(new Scene(parent, 300.0, 160.0));
                stage.show();
                owner.setAnimationsEnabled(false);
                tooltip.show(owner, stage.getX() + 20.0, stage.getY() + 50.0);
                tooltip.getScene().getRoot().applyCss();
                M3TooltipSkin skin = (M3TooltipSkin) tooltip.getSkin();
                assertFalse(tooltip.isAnimationsEnabled());
                assertFalse(skin.isAnimationsEnabled());
                assertEquals(1.0, skin.getOpacity());

                owner.setAnimationsEnabled(true);
                tooltip.getScene().getRoot().applyCss();
                assertTrue(tooltip.isAnimationsEnabled());
                owner.setAnimationsEnabled(false);
                tooltip.getScene().getRoot().applyCss();
                assertFalse(tooltip.isAnimationsEnabled());

                SimpleBooleanProperty enabled = new SimpleBooleanProperty(true);
                tooltip.animationsEnabledProperty().bind(enabled);
                assertTrue(skin.isAnimationsEnabled());
                assertFalse(M3MotionSettings.shouldReduceMotion(skin));
                M3MotionSettings.setGlobalReducedMotionRequested(true);
                assertTrue(tooltip.isAnimationsEnabled());
                assertTrue(M3MotionSettings.shouldReduceMotion(skin));
                enabled.set(false);
                assertFalse(skin.isAnimationsEnabled());
            } finally {
                tooltip.animationsEnabledProperty().unbind();
                tooltip.setAnimationsEnabled(false);
                tooltip.hide();
                stage.close();
            }
        }));
    }

    /// Rejects unavailable owners without changing an existing popup or stopping its context synchronization.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void invalidOwnerPreservesCurrentPresentationAndContext(boolean attachedToHiddenWindow) {
        FxTestUtils.runOnFxThread(() -> {
            M3Button owner = new M3Button("Owner");
            Pane parent = new Pane(owner);
            Pane invalidOwner = new Pane();
            Stage stage = new Stage();
            Stage hiddenStage = new Stage();
            M3Tooltip tooltip = new M3Tooltip("Retained context");
            try {
                if (attachedToHiddenWindow) {
                    hiddenStage.setScene(new Scene(invalidOwner));
                }
                tooltip.show(invalidOwner, 0.0, 0.0);
                assertFalse(tooltip.isShowing());
                stage.setScene(new Scene(parent, 300.0, 160.0));
                stage.show();
                owner.setAnimationsEnabled(false);
                tooltip.show(owner, stage.getX() + 20.0, stage.getY() + 50.0);
                Parent popupRoot = tooltip.getScene().getRoot();
                popupRoot.applyCss();
                assertTrue(tooltip.isShowing());
                assertFalse(tooltip.isAnimationsEnabled());
                double anchorX = tooltip.getAnchorX();
                double anchorY = tooltip.getAnchorY();

                tooltip.show(invalidOwner, 0.0, 0.0);
                assertTrue(tooltip.isShowing());
                assertSame(owner, tooltip.getOwnerNode());
                assertSame(popupRoot, tooltip.getScene().getRoot());
                assertEquals(anchorX, tooltip.getAnchorX());
                assertEquals(anchorY, tooltip.getAnchorY());
                assertFalse(tooltip.isAnimationsEnabled());
                owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, popupRoot.getNodeOrientation());
                owner.setAnimationsEnabled(true);
                popupRoot.applyCss();
                assertTrue(tooltip.isAnimationsEnabled());
                owner.setAnimationsEnabled(false);
                popupRoot.applyCss();
                assertFalse(tooltip.isAnimationsEnabled());
            } finally {
                tooltip.setAnimationsEnabled(false);
                tooltip.hide();
                stage.close();
                hiddenStage.close();
            }
        });
    }

    /// Native popup CSS inherits through an ordinary JavaFX owner with its own unrelated style.
    @Test
    void nativePopupInheritsCssThroughOrdinaryOwner() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            owner.setStyle("-fx-opacity: 0.9;");
            Pane parent = new Pane(owner);
            parent.setStyle("-m3-animations-enabled: false;");
            Stage stage = new Stage();
            M3Tooltip tooltip = new M3Tooltip("CSS motion");
            try {
                Scene scene = new Scene(parent, 300.0, 160.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                tooltip.show(owner, stage.getX() + 20.0, stage.getY() + 50.0);
                tooltip.getScene().getRoot().applyCss();
                assertFalse(tooltip.isAnimationsEnabled());
                tooltip.setStyle("-m3-animations-enabled: true;");
                tooltip.getScene().getRoot().applyCss();
                assertTrue(tooltip.isAnimationsEnabled());
                tooltip.setStyle("");
                tooltip.getScene().getRoot().applyCss();
                assertFalse(tooltip.isAnimationsEnabled());
            } finally {
                tooltip.setAnimationsEnabled(false);
                tooltip.hide();
                stage.close();
            }
        });
    }

    /// Does not install tooltip token declarations into the containing scene.
    @Test
    void themeStylesheetRemainsLocal() {
        FxTestUtils.runOnFxThread(() -> {
            M3Tooltip tooltip = new M3Tooltip("Local theme");
            tooltip.setTheme(M3Theme.defaultTheme());
            M3TooltipSkin skin = new M3TooltipSkin(tooltip);
            try {
                Scene scene = new Scene(new Pane(skin));
                assertEquals(List.of(), scene.getStylesheets());
                assertTrue(content(skin).getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(M3Theme.defaultTheme())));
            } finally {
                skin.dispose();
            }
        });
    }

    /// Leaves a matching application-owned scene stylesheet in place during cleanup.
    @Test
    void disposalPreservesSceneStylesheet() {
        FxTestUtils.runOnFxThread(() -> {
            M3Tooltip tooltip = new M3Tooltip("Borrowed scene stylesheet");
            tooltip.setTheme(M3Theme.defaultTheme());
            M3TooltipSkin skin = new M3TooltipSkin(tooltip);
            Pane parent = new Pane();
            Scene scene = new Scene(parent);
            String stylesheet = M3ThemeRuntime.themeStylesheetUrl(M3Theme.defaultTheme());
            scene.getStylesheets().add(stylesheet);
            parent.getChildren().add(skin);
            skin.dispose();
            assertEquals(List.of(stylesheet), scene.getStylesheets());
        });
    }

    /// Retains a matching application-owned content stylesheet after clearing the theme and disposing the skin.
    @Test
    void clearingThemePreservesContentStylesheet() {
        FxTestUtils.runOnFxThread(() -> {
            M3Tooltip tooltip = new M3Tooltip("Borrowed content stylesheet");
            M3TooltipSkin skin = new M3TooltipSkin(tooltip);
            new Scene(new Pane(skin));
            Label content = content(skin);
            String stylesheet = M3ThemeRuntime.themeStylesheetUrl(M3Theme.defaultTheme());
            content.getStylesheets().add(stylesheet);
            @Unmodifiable List<String> original = List.copyOf(content.getStylesheets());
            try {
                tooltip.setTheme(M3Theme.defaultTheme());
                tooltip.setTheme(null);
                assertEquals(original, content.getStylesheets());
            } finally {
                skin.dispose();
            }
            assertEquals(original, content.getStylesheets());
        });
    }

    /// Retains local declarations across scene detachment and removes owned declarations on disposal.
    @Test
    void detachmentRetainsThemeUntilDisposal() {
        FxTestUtils.runOnFxThread(() -> {
            M3Tooltip tooltip = new M3Tooltip("Detached content");
            tooltip.setTheme(M3Theme.defaultTheme());
            M3TooltipSkin skin = new M3TooltipSkin(tooltip);
            Pane parent = new Pane(skin);
            new Scene(parent);
            Label content = content(skin);
            String stylesheet = M3ThemeRuntime.themeStylesheetUrl(M3Theme.defaultTheme());
            try {
                parent.getChildren().clear();
                assertTrue(content.getStylesheets().contains(stylesheet));
            } finally {
                skin.dispose();
            }
            assertFalse(content.getStylesheets().contains(stylesheet));
        });
    }

    /// Applies runtime theme changes for both node-owned and window-owned native popups.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nativePopupUsesCurrentTheme(boolean nodeOwner) {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            Label owner = new Label("Owner");
            Pane parent = new Pane(owner);
            M3Tooltip tooltip = new M3Tooltip("Theme changes");
            tooltip.setAnimationsEnabled(false);
            try {
                stage.setScene(new Scene(parent, 300.0, 160.0));
                stage.show();
                tooltip.setTheme(M3Theme.defaultTheme());
                if (nodeOwner) {
                    tooltip.show(owner, stage.getX() + 20.0, stage.getY() + 50.0);
                } else {
                    tooltip.show(stage, stage.getX() + 20.0, stage.getY() + 50.0);
                }
                M3TooltipSkin skin = (M3TooltipSkin) tooltip.getSkin();
                Label content = content(skin);
                for (Brightness brightness : List.of(Brightness.DARK, Brightness.LIGHT)) {
                    M3Theme theme = M3Theme.fromSeed(Color.TEAL, M3Profile.EXPRESSIVE_2025, brightness);
                    tooltip.setTheme(theme);
                    tooltip.getScene().getRoot().applyCss();
                    assertEquals(theme.colorScheme().getColor(ColorRole.INVERSE_SURFACE),
                            content.getBackground().getFills().get(0).getFill());
                    assertEquals(theme.colorScheme().getColor(ColorRole.INVERSE_ON_SURFACE), content.getTextFill());
                    assertEquals(6.0, content.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius());
                }
                tooltip.setTheme(null);
                tooltip.getScene().getRoot().applyCss();
                assertEquals(4.0, content.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius());
            } finally {
                stage.close();
                assertFalse(tooltip.isShowing(), "Closing the owner must synchronously close its tooltip");
                tooltip.hide();
            }
        });
    }

    /// Renders inherited local theme changes after the queued popup context synchronization has completed.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void inheritedThemeUpdatesRenderedColors(boolean reparent) {
        AtomicReference<@Nullable InheritedThemeFixture> reference = new AtomicReference<>();
        M3Theme dark = M3Theme.fromSeed(Color.TEAL, M3Profile.EXPRESSIVE_2025, Brightness.DARK);
        try {
            FxTestUtils.runOnFxThread(() -> {
                Stage stage = new Stage();
                Label owner = new Label("Owner");
                Pane localRoot = new Pane(owner);
                Pane replacementRoot = new Pane();
                Pane root = new Pane(localRoot, replacementRoot);
                M3ThemeManager.install(localRoot, M3Theme.defaultTheme());
                M3ThemeManager.install(replacementRoot, dark);
                M3Tooltip tooltip = new M3Tooltip("Inherited theme");
                tooltip.setAnimationsEnabled(false);
                reference.set(new InheritedThemeFixture(stage, owner, tooltip));
                M3Tooltip.install(owner, tooltip);
                stage.setScene(new Scene(root, 300.0, 160.0));
                stage.show();
                tooltip.show(owner, stage.getX() + 20.0, stage.getY() + 50.0);
                if (reparent) {
                    localRoot.getChildren().remove(owner);
                    replacementRoot.getChildren().add(owner);
                } else {
                    M3ThemeManager.install(localRoot, dark);
                }
            });
            FxTestUtils.runOnFxThread(() -> {
                M3Tooltip tooltip = Objects.requireNonNull(reference.get()).tooltip();
                tooltip.getScene().getRoot().applyCss();
                Label content = content((M3TooltipSkin) tooltip.getSkin());
                assertEquals(dark, tooltip.getTheme());
                assertEquals(dark, M3ThemeRuntime.getTheme(content));
                assertTrue(content.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(dark)));
                assertTrue(content.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertEquals(dark.colorScheme().getColor(ColorRole.INVERSE_SURFACE),
                        content.getBackground().getFills().get(0).getFill(), content.getStyleClass().toString());
                assertEquals(dark.colorScheme().getColor(ColorRole.INVERSE_ON_SURFACE), content.getTextFill());
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable InheritedThemeFixture fixture = reference.get();
                if (fixture != null) {
                    M3Tooltip.uninstall(fixture.owner(), fixture.tooltip());
                    fixture.stage().close();
                    fixture.tooltip().hide();
                }
            });
        }
    }

    /// Keeps application inline styles above generated theme declarations.
    @Test
    void applicationStylesOverrideTheme() {
        FxTestUtils.runOnFxThread(() -> {
            M3Tooltip tooltip = new M3Tooltip("Application CSS");
            tooltip.setTheme(M3Theme.defaultTheme());
            M3TooltipSkin skin = new M3TooltipSkin(tooltip);
            Pane parent = new Pane(skin);
            new Scene(parent);
            Label content = content(skin);
            try {
                tooltip.setStyle("-fx-background-radius: 19px; -fx-text-fill: red;");
                tooltip.setTheme(M3Theme.fromSeed(Color.TEAL, M3Profile.EXPRESSIVE_2025, Brightness.DARK));
                parent.applyCss();
                assertEquals(Color.RED, content.getTextFill());
                assertEquals(19.0, content.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius());
            } finally {
                skin.dispose();
            }
        });
    }

    /// Returns the skin's rendered label without relying on CSS installation.
    private static Label content(M3TooltipSkin skin) {
        return (Label) skin.getChildrenUnmodifiable().get(0);
    }

    /// Retains the native resources needed between popup synchronization turns.
    ///
    /// @param stage the owner window
    /// @param owner the installed tooltip target
    /// @param tooltip the displayed popup
    @NotNullByDefault
    private record InheritedThemeFixture(Stage stage, Label owner, M3Tooltip tooltip) {
    }
}
