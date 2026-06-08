// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests state layer animation behavior.
@NotNullByDefault
final class M3StateLayerTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that owner-state hover opacity is reached through an animation.
    @Test
    void stateLayerAnimatesOwnerStateHoverOpacity() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3StateLayer stateLayer = new M3StateLayer();
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
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3MotionSettings.setAnimationsEnabled(owner, false);
            M3StateLayer stateLayer = new M3StateLayer();
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
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3StateLayer stateLayer = new M3StateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            M3MotionSettings.setAnimationsEnabled(owner, true);
            try {
                Region overlay = lookupRegion(stateLayer, ".m3-state-layer");
                owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
                stateLayer.animateOverlayOpacityFromOwnerState();

                assertEquals(0.0, overlay.getOpacity(), 0.0001);
                assertTrue(stateLayer.isOverlayOpacityAnimationRunning());

                M3MotionSettings.setAnimationsEnabled(owner, false);

                assertTrue(overlay.getOpacity() > 0.0);
                assertFalse(stateLayer.isOverlayOpacityAnimationRunning());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(owner);
                stateLayer.uninstallStateTransitions();
            }
        });
    }

    /// Verifies that installed theme tokens control runtime state layer opacity.
    @Test
    void stateLayerUsesInstalledThemeStateTokens() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3MotionSettings.setAnimationsEnabled(owner, false);
            M3StateLayer stateLayer = new M3StateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);
            M3Theme baseTheme = M3Theme.defaultTheme();
            M3TokenSet baseTokens = baseTheme.tokens();
            M3Theme tokenTheme = M3Theme.fromTokenSet(
                    baseTheme.profile(),
                    baseTheme.colorScheme(),
                    M3Density.standard(),
                    M3TokenSet.create(
                            baseTokens.profile(),
                            baseTokens.colorTokens(),
                            baseTokens.typographyTokens(),
                            baseTokens.shapeTokens(),
                            baseTokens.elevationTokens(),
                            baseTokens.motionTokens(),
                            M3StateLayerTokens.create(0.21, 0.22, 0.23, 0.24, 0.25, 0.26),
                            baseTokens.componentTokens()
                    )
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

    /// Verifies that disabled motion suppresses transient ripple animation.
    @Test
    void disabledMotionSuppressesRippleAnimation() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            M3MotionSettings.setAnimationsEnabled(owner, false);
            M3StateLayer stateLayer = new M3StateLayer();
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
            Pane owner = new Pane();
            M3StateLayer stateLayer = new M3StateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            M3MotionSettings.setAnimationsEnabled(owner, true);
            try {
                stateLayer.playRipple(20.0, 20.0);

                Region ripple = lookupRegion(stateLayer, ".m3-ripple");
                assertTrue(ripple.getOpacity() > 0.0);
                assertTrue(stateLayer.isRippleAnimationRunning());

                M3MotionSettings.setAnimationsEnabled(owner, false);

                assertEquals(0.0, ripple.getOpacity(), 0.0001);
                assertEquals(0.0, ripple.getScaleX(), 0.0001);
                assertEquals(0.0, ripple.getScaleY(), 0.0001);
                assertFalse(stateLayer.isRippleAnimationRunning());
            } finally {
                M3MotionSettings.clearAnimationsEnabled(owner);
                stateLayer.uninstallStateTransitions();
            }
        });
    }

    /// Verifies that plain focused state does not show a persistent state layer.
    @Test
    void stateLayerUsesFocusVisibleInsteadOfPlainFocus() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3MotionSettings.setAnimationsEnabled(owner, false);
            M3StateLayer stateLayer = new M3StateLayer();
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

    /// Verifies that ripples remain visible until explicitly released.
    @Test
    void rippleHoldsUntilReleaseThenFades() {
        FxTestUtils.runOnFxThread(() -> {
            Pane root = new Pane();
            M3StateLayer stateLayer = new M3StateLayer();
            root.getChildren().add(stateLayer);
            Scene scene = new Scene(root, 100.0, 40.0);

            root.applyCss();
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            stateLayer.playRipple(20.0, 20.0);

            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            Timeline expansionAnimation = reflectedTimeline(stateLayer, "rippleAnimation");
            expansionAnimation.jumpTo(expansionAnimation.getTotalDuration());

            assertTrue(ripple.getOpacity() > 0.1);
            assertEquals(1.0, ripple.getScaleX(), 0.0001);
            assertEquals(1.0, ripple.getScaleY(), 0.0001);

            stateLayer.releaseRipple();
            Timeline releaseAnimation = reflectedTimeline(stateLayer, "rippleAnimation");
            assertTrue(ripple.getOpacity() > 0.1);
            assertTrue(stateLayer.isRippleAnimationRunning());

            releaseAnimation.jumpTo(releaseAnimation.getTotalDuration().divide(2.0));

            assertTrue(ripple.getOpacity() > 0.0);

            releaseAnimation.jumpTo(releaseAnimation.getTotalDuration());

            assertEquals(0.0, ripple.getOpacity(), 0.0001);
        });
    }

    /// Verifies that an early release lets the ripple expand while it fades.
    @Test
    void rippleReleasedBeforeExpansionCompletesStillExpandsWhileFading() {
        FxTestUtils.runOnFxThread(() -> {
            Pane root = new Pane();
            M3StateLayer stateLayer = new M3StateLayer();
            root.getChildren().add(stateLayer);
            Scene scene = new Scene(root, 100.0, 40.0);

            root.applyCss();
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            stateLayer.playRipple(12.0, 20.0);

            Region ripple = lookupRegion(stateLayer, ".m3-ripple");
            double startScale = Math.max(ripple.getScaleX(), ripple.getScaleY());
            double startOpacity = ripple.getOpacity();
            stateLayer.releaseRipple();
            Timeline releaseAnimation = reflectedTimeline(stateLayer, "rippleAnimation");

            assertTrue(ripple.getOpacity() > 0.1);
            assertTrue(stateLayer.isRippleAnimationRunning());

            releaseAnimation.jumpTo(releaseAnimation.getTotalDuration().divide(2.0));

            assertTrue(Math.max(ripple.getScaleX(), ripple.getScaleY()) > startScale);
            assertTrue(ripple.getOpacity() > 0.0);
            assertTrue(ripple.getOpacity() < startOpacity);

            releaseAnimation.jumpTo(releaseAnimation.getTotalDuration());

            assertEquals(0.0, ripple.getOpacity(), 0.0001);
            assertEquals(1.0, ripple.getScaleX(), 0.0001);
            assertEquals(1.0, ripple.getScaleY(), 0.0001);
        });
    }

    /// Returns a region looked up below a node.
    private static Region lookupRegion(Pane node, String selector) {
        javafx.scene.Node child = node.lookup(selector);
        assertInstanceOf(Region.class, child);
        return (Region) child;
    }

    /// Returns a private timeline field from a state layer.
    private static Timeline reflectedTimeline(M3StateLayer stateLayer, String fieldName) {
        try {
            java.lang.reflect.Field field = M3StateLayer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return assertInstanceOf(Timeline.class, field.get(stateLayer));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
