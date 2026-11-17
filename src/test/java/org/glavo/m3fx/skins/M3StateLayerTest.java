// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.internal.M3FocusVisibleTracker;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.glavo.m3fx.testing.Tier2Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests state layer animation behavior.
@NotNullByDefault
final class M3StateLayerTest {
    /// The button-family style class used by state-layer CSS fixtures.
    private static final String BUTTON_BASE_STYLE_CLASS = "m3-button-base";

    /// State layers whose listeners and animations belong to the current test.
    private final List<M3StateLayer> stateLayers = new ArrayList<>();

    /// Windows created by the current test, including partially initialized windows.
    private final List<Stage> stages = new ArrayList<>();

    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Stops animations and removes listeners even when setup or an assertion fails.
    @AfterEach
    void releaseResources() {
        FxTestUtils.runOnFxThread(() -> {
            try {
                for (M3StateLayer stateLayer : stateLayers) {
                    stateLayer.cancelRipple();
                    stateLayer.uninstallStateTransitions();
                }
            } finally {
                stages.forEach(Stage::close);
                stages.clear();
                stateLayers.clear();
            }
        });
    }

    /// Inherits feedback through ordinary parents and permits local overrides and live mode changes.
    @Test
    void pressFeedbackUsesCssInheritanceAndLocalOverrides() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            M3StateLayer layer = createStateLayer();
            owner.getChildren().add(layer);
            Pane intermediate = new Pane(owner);
            Pane root = new Pane(intermediate);
            new Scene(root, 200.0, 80.0);
            root.setStyle("-m3-press-feedback: state-layer;");
            layer.installStateTransitions(owner);
            layer.layoutLayer(0, 0, 100, 40, 20);
            FxTestUtils.assertNoCssWarnings(root::applyCss);
            Region overlay = lookupRegion(layer, ".m3-state-layer");

            layer.playRipple(4, 8);
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            assertNull(layer.lookup(".m3-ripple"));
            assertFalse(layer.isRippleAnimationRunning());
            layer.releaseRipple();
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            assertTrue(layer.isOverlayOpacityAnimationRunning());
            layer.cancelRipple();

            owner.setStyle("-m3-press-feedback: ripple;");
            root.applyCss();
            layer.playRipple(4, 8);
            Region ripple = lookupRegion(layer, ".m3-ripple");
            assertTrue(layer.isRippleAnimationRunning());
            assertEquals(0.0, overlay.getOpacity(), 0.0001);

            owner.setStyle("");
            root.applyCss();
            assertFalse(layer.isRippleAnimationRunning());
            assertEquals(0.0, ripple.getOpacity(), 0.0001);
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            layer.releaseRipple();
            assertTrue(layer.isOverlayOpacityAnimationRunning());

            root.setStyle("");
            root.applyCss();
            layer.playRipple(4, 8);
            assertTrue(layer.isRippleAnimationRunning());
        });
    }

    /// Preserves resting, hover, and focus states while clearing cancelled presses and unavailable owners.
    @Test
    void stateLayerPressFeedbackHonorsInteractionLifecycle() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.setAnimationsEnabled(false);
            owner.setStyle("-m3-press-feedback: state-layer;");
            M3StateLayer layer = createStateLayer();
            owner.getChildren().add(layer);
            Pane root = new Pane(owner);
            new Scene(root, 200.0, 80.0);
            layer.installStateTransitions(owner);
            layer.layoutLayer(0, 0, 100, 40, 20);
            root.applyCss();
            Region overlay = lookupRegion(layer, ".m3-state-layer");
            layer.setRestingOverlayOpacity(0.2);
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            assertEquals(0.264, overlay.getOpacity(), 0.0001);
            layer.playCenteredRipple();
            assertEquals(0.28, overlay.getOpacity(), 0.0001);
            layer.releaseRipple();
            assertEquals(0.264, overlay.getOpacity(), 0.0001);
            assertFalse(layer.isOverlayOpacityAnimationRunning());

            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
            layer.playCenteredRipple();
            layer.cancelRipple();
            assertEquals(0.2, overlay.getOpacity(), 0.0001);
            layer.playCenteredRipple();
            owner.setDisable(true);
            assertEquals(0.0, overlay.getOpacity(), 0.0001);
            owner.setDisable(false);
            assertEquals(0.2, overlay.getOpacity(), 0.0001);

            layer.playCenteredRipple();
            layer.setModalInteractionBlocked(true);
            assertEquals(0.2, overlay.getOpacity(), 0.0001);
            layer.setModalInteractionBlocked(false);
            layer.playCenteredRipple();
            root.setVisible(false);
            assertEquals(0.2, overlay.getOpacity(), 0.0001);
            root.setVisible(true);
            layer.setDelegatedFocusVisible(true);
            assertEquals(0.28, overlay.getOpacity(), 0.0001);
            assertTrue(lookupRegion(layer, ".m3-focus-indicator").isVisible());
            layer.setDelegatedFocusVisible(false);
            layer.playCenteredRipple();
            root.getChildren().clear();
            assertEquals(0.2, overlay.getOpacity(), 0.0001);
            layer.reset();
            assertEquals(0.0, overlay.getOpacity(), 0.0001);
            assertNull(layer.lookup(".m3-ripple"));
        });
    }

    /// Routes keyboard presses through different skins without creating ripples in state-layer mode.
    @ParameterizedTest
    @ValueSource(strings = {"button", "icon-toggle", "checkbox"})
    void controlsUseInheritedStateLayerFeedbackForKeyboardActivation(String type) {
        FxTestUtils.runOnFxThread(() -> {
            ButtonBase button = switch (type) {
                case "icon-toggle" -> new M3IconToggleButton();
                case "checkbox" -> new M3CheckBox();
                default -> new M3Button("Action");
            };
            ((M3Animated) button).setAnimationsEnabled(false);
            Pane root = new Pane(button);
            root.setStyle("-m3-press-feedback: state-layer;");
            new Scene(root, 200, 80);
            root.applyCss();
            button.resize(120, 40);
            button.layout();
            M3StateLayer layer = assertInstanceOf(M3StateLayer.class, button.lookup(".m3-state-layer-container"));
            stateLayers.add(layer);
            Region overlay = lookupRegion(layer, ".m3-state-layer");
            int[] actions = {0};
            button.setOnAction(event -> actions[0]++);
            button.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false));
            assertTrue(button.isArmed());
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            assertNull(layer.lookup(".m3-ripple"));
            button.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.SPACE, false, false, false, false));
            assertFalse(button.isArmed());
            assertEquals(1, actions[0]);
            assertEquals(0.0, overlay.getOpacity(), 0.0001);
        });
    }

    /// Stylesheet state changes and reparenting preserve nearest-scope overrides across styled parents.
    @Test
    void pressFeedbackFollowsStylesheetScopes() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Action");
            Pane branch = new Pane(button);
            branch.setStyle("-fx-opacity: 0.9;");
            Pane rippleScope = new Pane();
            rippleScope.setStyle("-m3-press-feedback: ripple;");
            Pane root = new Pane(branch, rippleScope);
            root.getStyleClass().add("press-scope");
            Scene scene = new Scene(root, 300, 100);
            String css = """
                    .press-scope:flat { -m3-press-feedback: state-layer; }
                    .m3-button.local-ripple { -m3-press-feedback: ripple; }
                    """;
            scene.getStylesheets().add("data:text/css;base64,"
                    + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8)));
            PseudoClass flat = PseudoClass.getPseudoClass("flat");
            root.pseudoClassStateChanged(flat, true);
            FxTestUtils.assertNoCssWarnings(root::applyCss);
            button.resize(120, 40);
            button.layout();
            M3StateLayer layer = assertInstanceOf(M3StateLayer.class, button.lookup(".m3-state-layer-container"));
            stateLayers.add(layer);
            Region overlay = lookupRegion(layer, ".m3-state-layer");
            layer.playCenteredRipple();
            assertNull(layer.lookup(".m3-ripple"));
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            layer.releaseRipple();
            assertTrue(layer.isOverlayOpacityAnimationRunning());
            layer.playCenteredRipple();
            assertFalse(layer.isOverlayOpacityAnimationRunning());
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            layer.releaseRipple();
            button.setAnimationsEnabled(false);
            assertFalse(layer.isOverlayOpacityAnimationRunning());
            assertEquals(0.0, overlay.getOpacity(), 0.0001);
            button.setAnimationsEnabled(true);

            button.getStyleClass().add("local-ripple");
            root.applyCss();
            layer.playCenteredRipple();
            assertTrue(layer.isRippleAnimationRunning());
            button.getStyleClass().remove("local-ripple");
            root.applyCss();
            assertFalse(layer.isRippleAnimationRunning());
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            layer.cancelRipple();

            root.getChildren().remove(branch);
            rippleScope.getChildren().add(branch);
            root.applyCss();
            layer.playCenteredRipple();
            assertTrue(layer.isRippleAnimationRunning());
            layer.cancelRipple();
            rippleScope.getChildren().remove(branch);
            root.getChildren().add(branch);
            root.applyCss();
            layer.playCenteredRipple();
            assertFalse(layer.isRippleAnimationRunning());
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
            root.pseudoClassStateChanged(flat, false);
            root.applyCss();
            layer.playCenteredRipple();
            assertTrue(layer.isRippleAnimationRunning());
        });
    }

    /// Verifies that exact circular bounds use a Circle clip while rounded rectangles keep the reusable path clip.
    @Test
    void exactCircleBoundsUseCircleClip() {
        FxTestUtils.runOnFxThread(() -> {
            M3StateLayer stateLayer = createStateLayer();
            stateLayer.layoutLayer(0.0, 0.0, 40.0, 40.0, 999.0);
            Parent clippedContent = assertInstanceOf(Parent.class, stateLayer.getChildrenUnmodifiable().get(0));
            assertInstanceOf(Circle.class, clippedContent.getClip());

            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            Node roundedClip = Objects.requireNonNull(clippedContent.getClip(), "rounded state layer clip");
            assertInstanceOf(Path.class, roundedClip);
        });
    }

    /// Verifies that optional visual nodes are created on first use and inherit previously configured state.
    @Test
    void optionalVisualNodesAreCreatedOnFirstUse() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);
            PseudoClass customState = PseudoClass.getPseudoClass("custom-state");

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            stateLayer.setContentPaint(Color.CORNFLOWERBLUE);
            stateLayer.setContentPseudoClass(customState, true);
            owner.applyCss();

            assertNull(stateLayer.lookup(".m3-ripple"));
            assertNull(stateLayer.lookup(".m3-focus-indicator"));
            assertFalse(stateLayer.isRippleAnimationRunning());
            assertFalse(stateLayer.isFocusIndicatorOpacityAnimationRunning());

            stateLayer.playRipple(20.0, 20.0);

            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            assertEquals(
                    Color.CORNFLOWERBLUE,
                    ripple.getBackground().getFills().get(0).getFill()
            );
            assertTrue(ripple.getPseudoClassStates().contains(customState));

            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            assertInstanceOf(Region.class, stateLayer.lookup(".m3-focus-indicator"));
            stateLayer.uninstallStateTransitions();
        });
    }

    /// Verifies that owner-state hover opacity is reached through an animation.
    @Test
    void stateLayerAnimatesOwnerStateHoverOpacity() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            assertEquals(0.0, overlay.getOpacity(), 0.0001);

            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            assertEquals(0.0, overlay.getOpacity(), 0.0001);
            assertTrue(stateLayer.isOverlayOpacityAnimationRunning());
        });
    }

    /// Verifies that disabled motion applies owner-state hover opacity without starting a transition.
    @Test
    void disabledMotionAppliesOwnerStateHoverOpacityImmediately() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            owner.setAnimationsEnabled(false);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            assertTrue(overlay.getOpacity() > 0.0);
            assertFalse(stateLayer.isOverlayOpacityAnimationRunning());
        });
    }

    /// Verifies that running owner-state opacity animation settles when animations are disabled at runtime.
    @Test
    void stateLayerSettlesOwnerStateOpacityWhenAnimationsAreDisabledAtRuntime() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            owner.setAnimationsEnabled(true);
            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            assertEquals(0.0, overlay.getOpacity(), 0.0001);
            assertTrue(stateLayer.isOverlayOpacityAnimationRunning());

            owner.setAnimationsEnabled(false);

            assertTrue(overlay.getOpacity() > 0.0);
            assertFalse(stateLayer.isOverlayOpacityAnimationRunning());
        });
    }

    /// Verifies hiding the presenting window settles persistent feedback and clears transient ripple work.
    @Test
    @Tier2Test
    void stateLayerSettlesAnimationsWhenWindowHides() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);
            Stage stage = new Stage();
            stages.add(stage);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            owner.setAnimationsEnabled(true);
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            stateLayer.playRipple(20.0, 20.0);
            stateLayer.animateOverlayOpacityFromOwnerState();

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            assertFalse(stateLayer.isOverlayOpacityAnimationRunning());
            assertTrue(stateLayer.isRippleAnimationRunning());
            assertEquals(0.0, overlay.getOpacity(), 0.0001);

            stage.hide();

            assertFalse(stateLayer.isOverlayOpacityAnimationRunning());
            assertFalse(stateLayer.isRippleAnimationRunning());
            assertTrue(overlay.getOpacity() > 0.0);
            assertEquals(0.0, ripple.getOpacity(), 0.0001);
        });
    }

    /// Verifies that installed theme tokens control runtime state layer opacity.
    @Test
    void stateLayerUsesInstalledThemeStateTokens() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            owner.setAnimationsEnabled(false);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);
            M3Theme baseTheme = M3Theme.defaultTheme();
            M3TokenSet baseTokens = baseTheme.tokens();
            M3Theme tokenTheme = M3Theme.fromTokenSet(
                    M3TokenSet.builder(baseTokens)
                            .stateLayerTokens(M3StateLayerTokens.builder(M3StateLayerTokens.baseline())
                                    .hoverOpacity(0.21)
                                    .focusOpacity(0.22)
                                    .pressedOpacity(0.23)
                                    .draggedOpacity(0.24)
                                    .disabledContainerOpacity(0.25)
                                    .disabledContentOpacity(0.26)
                                    .build())
                            .build()
            );

            M3ThemeManager.install(scene, tokenTheme);
            owner.applyCss();
            stateLayer.installStateTransitions(owner);

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
            assertEquals(0.21, overlay.getOpacity(), 0.0001);

            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
            assertEquals(0.22, overlay.getOpacity(), 0.0001);

            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
            assertEquals(0.23, overlay.getOpacity(), 0.0001);
        });
    }

    /// Verifies that a pressed ripple uses one token-controlled feedback layer instead of stacking two opacities.
    @Test
    void pressedRippleUsesThemeOpacityWithoutStackingThePersistentOverlay() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);
            M3Theme baseTheme = M3Theme.defaultTheme();
            M3Theme tokenTheme = M3Theme.fromTokenSet(
                    M3TokenSet.builder(baseTheme.tokens())
                            .stateLayerTokens(M3StateLayerTokens.builder(M3StateLayerTokens.baseline())
                                    .pressedOpacity(0.23)
                                    .build())
                            .build()
            );

            M3ThemeManager.install(scene, tokenTheme);
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
            stateLayer.playRipple(20.0, 20.0);

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            assertEquals(0.0, overlay.getOpacity(), 0.0001);
            assertEquals(0.23, ripple.getOpacity(), 0.0001);

            stateLayer.cancelRipple();
            stateLayer.uninstallStateTransitions();
        });
    }

    /// Verifies that disabled motion suppresses transient ripple animation.
    @Test
    void disabledMotionSuppressesRippleAnimation() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.setAnimationsEnabled(false);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);

            stateLayer.playRipple(20.0, 20.0);

            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            assertEquals(0.0, ripple.getOpacity(), 0.0001);
            assertFalse(stateLayer.isRippleAnimationRunning());
        });
    }

    /// Verifies that a running ripple is cleared when animations are disabled at runtime.
    @Test
    void stateLayerClearsRippleWhenAnimationsAreDisabledAtRuntime() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            new Scene(owner, 100.0, 40.0);
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            owner.setAnimationsEnabled(true);
            stateLayer.playRipple(20.0, 20.0);

            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            assertTrue(ripple.getOpacity() > 0.0);
            assertTrue(stateLayer.isRippleAnimationRunning());

            owner.setAnimationsEnabled(false);

            assertEquals(0.0, ripple.getOpacity(), 0.0001);
            assertEquals(0.0, ripple.getScaleX(), 0.0001);
            assertEquals(0.0, ripple.getScaleY(), 0.0001);
            assertFalse(stateLayer.isRippleAnimationRunning());
        });
    }

    /// Verifies that plain focused state does not show a persistent state layer.
    @Test
    void stateLayerUsesFocusVisibleInsteadOfPlainFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            owner.setAnimationsEnabled(false);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);

            Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
            owner.applyCss();
            assertEquals(0.0, overlay.getOpacity(), 0.0001);

            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            owner.applyCss();
            assertEquals(0.1, overlay.getOpacity(), 0.0001);
        });
    }

    /// Verifies token-driven focus geometry for outer button rings and inner item rings.
    @Tier2Test
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void focusVisibleRendersTokenControlledIndicator(boolean outer) {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.getStyleClass().add(outer ? BUTTON_BASE_STYLE_CLASS : "m3-list-item");
            owner.setAnimationsEnabled(false);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);
            M3Theme baseTheme = M3Theme.defaultTheme();
            M3TokenSet baseTokens = baseTheme.tokens();
            M3Theme tokenTheme = M3Theme.fromTokenSet(
                    M3TokenSet.builder(baseTokens)
                            .stateLayerTokens(M3StateLayerTokens.builder(M3StateLayerTokens.baseline())
                                    .focusIndicatorThickness(4.0)
                                    .focusIndicatorOuterOffset(3.0)
                                    .focusIndicatorInnerOffset(-5.0)
                                    .build())
                            .build()
            );

            M3ThemeManager.install(scene, tokenTheme);
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);

            assertNull(stateLayer.lookup(".m3-focus-indicator"));

            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            Region focusIndicator = lookupRegion(stateLayer, ".m3-focus-indicator");
            assertEquals(1.0, focusIndicator.getOpacity(), 0.0001);
            assertTrue(focusIndicator.isVisible());
            assertEquals(outer ? -7.0 : 0.0, focusIndicator.getLayoutX(), 0.0001);
            assertEquals(outer ? -7.0 : 0.0, focusIndicator.getLayoutY(), 0.0001);
            assertEquals(outer ? 114.0 : 100.0, focusIndicator.getWidth(), 0.0001);
            assertEquals(outer ? 54.0 : 40.0, focusIndicator.getHeight(), 0.0001);
            owner.applyCss();
            BorderStroke border = focusIndicator.getBorder().getStrokes().get(0);
            assertEquals(outer ? Insets.EMPTY : new Insets(5.0), border.getInsets());
            assertEquals(4.0, border.getWidths().getTop(), 0.0001);
            assertEquals(outer ? 27.0 : 15.0, border.getRadii().getTopLeftHorizontalRadius(), 0.0001);
            assertFalse(stateLayer.isFocusIndicatorOpacityAnimationRunning());
        });
    }

    /// Verifies that a control-local CSS token overrides the theme's focus-indicator color.
    @Test
    void focusIndicatorColorUsesInheritedCssToken() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.setStyle(owner.getStyle() + " -m3-state-focus-indicator-color: rgb(1,2,3);");
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            stateLayer.animateOverlayOpacityFromOwnerState();
            owner.applyCss();

            Region focusIndicator = lookupRegion(stateLayer, ".m3-focus-indicator");
            assertEquals(
                    Color.rgb(1, 2, 3),
                    focusIndicator.getBorder().getStrokes().get(0).getTopStroke()
            );
        });
    }

    /// Verifies that keyboard focus indicator opacity animates when motion is enabled.
    @Test
    void focusIndicatorOpacityAnimatesWithMotionEnabled() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane owner = new M3OverlayPane();
            owner.getStyleClass().add(BUTTON_BASE_STYLE_CLASS);
            owner.setAnimationsEnabled(true);
            M3StateLayer stateLayer = createStateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);

            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            Region focusIndicator = lookupRegion(stateLayer, ".m3-focus-indicator");
            assertEquals(0.0, focusIndicator.getOpacity(), 0.0001);
            assertTrue(focusIndicator.isVisible());
            assertTrue(stateLayer.isFocusIndicatorOpacityAnimationRunning());

            owner.setAnimationsEnabled(false);
            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, false);
            stateLayer.animateOverlayOpacityFromOwnerState();

            assertEquals(0.0, focusIndicator.getOpacity(), 0.0001);
            assertFalse(focusIndicator.isVisible());
        });
    }

    /// Verifies that replacing interactive skins detaches their retired shared state layers.
    @Test
    void replacingInteractiveSkinsDetachesRetiredStateLayers() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            M3NavigationItem navigationItem = new M3NavigationItem("Navigation");
            M3CheckBox checkBox = new M3CheckBox("Check box");
            Pane root = new Pane(button, navigationItem, checkBox);
            Scene scene = new Scene(root, 420.0, 180.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            FxTestUtils.setCssAnimationsEnabled(root, false);
            button.resizeRelocate(12.0, 12.0, 120.0, 40.0);
            navigationItem.resizeRelocate(144.0, 12.0, 120.0, 64.0);
            checkBox.resizeRelocate(276.0, 12.0, 120.0, 48.0);
            root.applyCss();
            root.layout();

            Region retiredButtonOverlay = assertInstanceOf(
                    Region.class,
                    button.lookup("." + M3StateLayer.OVERLAY_STYLE_CLASS)
            );
            Region retiredNavigationOverlay = assertInstanceOf(
                    Region.class,
                    navigationItem.lookup("." + M3StateLayer.OVERLAY_STYLE_CLASS)
            );
            Region retiredCheckBoxOverlay = assertInstanceOf(
                    Region.class,
                    checkBox.lookup("." + M3StateLayer.OVERLAY_STYLE_CLASS)
            );

            FxTestUtils.replaceSkin(button, M3ButtonSkin::new);
            FxTestUtils.replaceSkin(navigationItem, M3NavigationItemSkin::new);
            FxTestUtils.replaceSkin(checkBox, M3CheckBoxSkin::new);
            root.applyCss();
            root.layout();

            PseudoClass hover = PseudoClass.getPseudoClass("hover");
            button.pseudoClassStateChanged(hover, true);
            navigationItem.pseudoClassStateChanged(hover, true);
            checkBox.pseudoClassStateChanged(hover, true);

            assertFalse(hasAncestor(retiredButtonOverlay, button));
            assertFalse(hasAncestor(retiredNavigationOverlay, navigationItem));
            assertFalse(hasAncestor(retiredCheckBoxOverlay, checkBox));
            assertEquals(0.0, retiredButtonOverlay.getOpacity(), 0.0001);
            assertEquals(0.0, retiredNavigationOverlay.getOpacity(), 0.0001);
            assertEquals(0.0, retiredCheckBoxOverlay.getOpacity(), 0.0001);
            assertTrue(currentOverlay(button, retiredButtonOverlay).getOpacity() > 0.0);
            assertTrue(currentOverlay(navigationItem, retiredNavigationOverlay).getOpacity() > 0.0);
            assertTrue(currentOverlay(checkBox, retiredCheckBoxOverlay).getOpacity() > 0.0);
        });
    }

    /// Verifies that ripples remain visible until explicitly released.
    @Tier2Test
    @Test
    void rippleHoldsUntilReleaseThenFades() throws InterruptedException {
        AtomicReference<@Nullable M3StateLayer> stateLayerReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> rippleReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseOpacityReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhenStable(
                () -> rippleReachedExpandedFrame(rippleReference),
                2,
                () -> showRippleStateLayer(stateLayerReference, rippleReference, 20.0, 20.0),
                () -> {
                    M3StateLayer stateLayer = Objects.requireNonNull(
                            stateLayerReference.get(),
                            "stateLayer"
                    );
                    Region ripple = Objects.requireNonNull(rippleReference.get(), "ripple");
                    double releaseOpacity = ripple.getOpacity();

                    assertTrue(releaseOpacity >= 0.099);
                    assertEquals(1.0, ripple.getScaleX(), 0.001);
                    assertEquals(1.0, ripple.getScaleY(), 0.001);

                    releaseOpacityReference.set(releaseOpacity);
                    stateLayer.releaseRipple();

                    assertTrue(ripple.getOpacity() >= 0.099);
                    assertTrue(stateLayer.isRippleAnimationRunning());
                }
        );
        FxTestUtils.runOnFxThreadWhen(
                () -> rippleOpacityFadedBelow(rippleReference, releaseOpacityReference),
                () -> {
                },
                () -> assertTrue(
                        Objects.requireNonNull(rippleReference.get(), "ripple").getOpacity() > 0.0
                )
        );
        FxTestUtils.runOnFxThreadWhenStable(
                () -> rippleReleaseFinished(stateLayerReference, rippleReference),
                2,
                () -> {
                },
                () -> {
                    M3StateLayer stateLayer = Objects.requireNonNull(
                            stateLayerReference.get(),
                            "stateLayer"
                    );
                    Region ripple = Objects.requireNonNull(rippleReference.get(), "ripple");

                    assertEquals(0.0, ripple.getOpacity(), 0.0001);
                    assertEquals(1.0, ripple.getScaleX(), 0.001);
                    assertEquals(1.0, ripple.getScaleY(), 0.001);
                    assertFalse(stateLayer.isRippleAnimationRunning());
                }
        );
    }

    /// Verifies that an early release lets the ripple expand while it fades.
    @Tier2Test
    @Test
    void rippleReleasedBeforeExpansionCompletesStillExpandsWhileFading() throws InterruptedException {
        AtomicReference<@Nullable M3StateLayer> stateLayerReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> rippleReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> startScaleReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> startOpacityReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> rippleExpandsWhileFading(rippleReference, startScaleReference, startOpacityReference),
                () -> {
                    showRippleStateLayer(stateLayerReference, rippleReference, 12.0, 20.0);

                    M3StateLayer stateLayer = Objects.requireNonNull(
                            stateLayerReference.get(),
                            "stateLayer"
                    );
                    Region ripple = Objects.requireNonNull(rippleReference.get(), "ripple");

                    startScaleReference.set(rippleScale(ripple));
                    startOpacityReference.set(ripple.getOpacity());
                    stateLayer.releaseRipple();

                    assertTrue(ripple.getOpacity() >= 0.099);
                    assertTrue(stateLayer.isRippleAnimationRunning());
                },
                () -> {
                    Region ripple = Objects.requireNonNull(rippleReference.get(), "ripple");

                    assertTrue(rippleScale(ripple) > Objects.requireNonNull(
                            startScaleReference.get(),
                            "startScale"
                    ));
                    assertTrue(ripple.getOpacity() > 0.0);
                    assertTrue(ripple.getOpacity() < Objects.requireNonNull(
                            startOpacityReference.get(),
                            "startOpacity"
                    ));
                }
        );
        FxTestUtils.runOnFxThreadWhenStable(
                () -> rippleReleaseFinished(stateLayerReference, rippleReference),
                2,
                () -> {
                },
                () -> {
                    M3StateLayer stateLayer = Objects.requireNonNull(
                            stateLayerReference.get(),
                            "stateLayer"
                    );
                    Region ripple = Objects.requireNonNull(rippleReference.get(), "ripple");

                    assertEquals(0.0, ripple.getOpacity(), 0.0001);
                    assertEquals(1.0, ripple.getScaleX(), 0.001);
                    assertEquals(1.0, ripple.getScaleY(), 0.001);
                    assertFalse(stateLayer.isRippleAnimationRunning());
                }
        );
    }

    /// Creates a state layer registered for cleanup before any listeners or animations are installed.
    private M3StateLayer createStateLayer() {
        M3StateLayer stateLayer = new M3StateLayer();
        stateLayers.add(stateLayer);
        return stateLayer;
    }

    /// Returns a region looked up below a node.
    private static Region lookupRegion(javafx.scene.Node node, String selector) {
        return assertInstanceOf(Region.class, node.lookup(selector));
    }

    /// Returns the current state-layer overlay while excluding a retired Skin node.
    private static Region currentOverlay(javafx.scene.Node owner, Region retiredOverlay) {
        for (javafx.scene.Node candidate : owner.lookupAll("." + M3StateLayer.OVERLAY_STYLE_CLASS)) {
            if (candidate != retiredOverlay) {
                return assertInstanceOf(Region.class, candidate);
            }
        }
        throw new AssertionError("Current state-layer overlay is missing");
    }

    /// Returns whether a node remains attached below a supplied ancestor.
    private static boolean hasAncestor(javafx.scene.Node node, javafx.scene.Node ancestor) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent == ancestor) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /// Shows a real-window state layer and starts a ripple from the supplied origin.
    private void showRippleStateLayer(
            AtomicReference<@Nullable M3StateLayer> stateLayerReference,
            AtomicReference<@Nullable Region> rippleReference,
            double originX,
            double originY
    ) {
        Pane root = new Pane();
        M3StateLayer stateLayer = createStateLayer();
        root.getChildren().add(stateLayer);
        Scene scene = new Scene(root, 100.0, 40.0);
        Stage stage = new Stage();
        stages.add(stage);

        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.resize(100.0, 40.0);
        root.layout();
        stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
        stateLayer.playRipple(originX, originY);

        stateLayerReference.set(stateLayer);
        rippleReference.set(lookupRegion(stateLayer, ".m3-ripple"));
    }

    /// Returns whether the ripple reached the expanded press-held frame.
    private static boolean rippleReachedExpandedFrame(AtomicReference<@Nullable Region> rippleReference) {
        @Nullable Region ripple = rippleReference.get();
        return ripple != null
                && ripple.getOpacity() >= 0.099
                && ripple.getScaleX() >= 0.999
                && ripple.getScaleY() >= 0.999;
    }

    /// Returns whether the ripple has visibly faded below the captured release opacity.
    private static boolean rippleOpacityFadedBelow(
            AtomicReference<@Nullable Region> rippleReference,
            AtomicReference<@Nullable Double> baselineOpacityReference
    ) {
        @Nullable Region ripple = rippleReference.get();
        @Nullable Double baselineOpacity = baselineOpacityReference.get();
        if (ripple == null || baselineOpacity == null) {
            return false;
        }

        double opacity = ripple.getOpacity();
        return opacity > 0.0 && opacity < baselineOpacity - 0.001;
    }

    /// Returns whether an early-released ripple is still expanding while it fades.
    private static boolean rippleExpandsWhileFading(
            AtomicReference<@Nullable Region> rippleReference,
            AtomicReference<@Nullable Double> startScaleReference,
            AtomicReference<@Nullable Double> startOpacityReference
    ) {
        @Nullable Region ripple = rippleReference.get();
        @Nullable Double startScale = startScaleReference.get();
        @Nullable Double startOpacity = startOpacityReference.get();
        if (ripple == null || startScale == null || startOpacity == null) {
            return false;
        }

        double opacity = ripple.getOpacity();
        return rippleScale(ripple) > startScale + 0.001
                && opacity > 0.0
                && opacity < startOpacity - 0.001;
    }

    /// Returns whether the ripple finished its release animation.
    private static boolean rippleReleaseFinished(
            AtomicReference<@Nullable M3StateLayer> stateLayerReference,
            AtomicReference<@Nullable Region> rippleReference
    ) {
        @Nullable M3StateLayer stateLayer = stateLayerReference.get();
        @Nullable Region ripple = rippleReference.get();
        return stateLayer != null
                && ripple != null
                && !stateLayer.isRippleAnimationRunning()
                && ripple.getOpacity() <= 0.0001
                && rippleScale(ripple) >= 0.999;
    }

    /// Returns the larger current ripple scale.
    private static double rippleScale(Region ripple) {
        return Math.max(ripple.getScaleX(), ripple.getScaleY());
    }
}
