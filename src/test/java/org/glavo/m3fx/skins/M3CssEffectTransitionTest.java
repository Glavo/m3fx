// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests CSS-resolved effect transitions.
@NotNullByDefault
final class M3CssEffectTransitionTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that CSS effect changes are animated from the current effect.
    @Test
    void animatesCssResolvedDropShadow() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            owner.applyCss();
            assertNull(target.getEffect());

            transition.install();
            target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
            transition.animateEffectFromCss();

            DropShadow animated = assertInstanceOf(DropShadow.class, target.getEffect());
            assertEquals(0.0, animated.getRadius(), 0.0001);
            assertTrue(transition.isRunning());

            transition.uninstall();
        });
    }

    /// Verifies that hover transitions resolve CSS after the hover pseudo-class is present.
    @Test
    void hoverPseudoClassStartsOneContinuousShadowTransition() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("effect-owner");
            Region target = new Region();
            target.getStyleClass().add("effect-target");
            owner.getChildren().add(target);
            Scene scene = new Scene(owner, 100.0, 40.0);
            String stylesheet = ".effect-owner .effect-target { -fx-effect: null; }"
                    + ".effect-owner:hover .effect-target {"
                    + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3); }";
            scene.getStylesheets().add("data:text/css;charset=UTF-8;base64,"
                    + Base64.getEncoder().encodeToString(stylesheet.getBytes(StandardCharsets.UTF_8)));
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            owner.applyCss();
            assertNull(target.getEffect());
            transition.install();
            owner.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);

            DropShadow animated = assertInstanceOf(DropShadow.class, target.getEffect());
            assertEquals(0.0, animated.getRadius(), 0.0001);
            assertNull(animated.getInput());
            assertTrue(transition.isRunning());

            transition.uninstall();
        });
    }

    /// Verifies that disabled motion applies CSS effect changes without starting a transition.
    @Test
    void disabledMotionAppliesCssResolvedDropShadowImmediately() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            M3MotionSettings.setReducedMotionRequested(owner, true);
            new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            owner.applyCss();
            transition.install();
            target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
            transition.animateEffectFromCss();

            DropShadow shadow = assertInstanceOf(DropShadow.class, target.getEffect());
            assertEquals(8.0, shadow.getRadius(), 0.0001);
            assertFalse(transition.isRunning());

            transition.uninstall();
        });
    }

    /// Verifies that a running CSS effect transition settles when animations are disabled at runtime.
    @Test
    void runningCssResolvedDropShadowSettlesWhenAnimationsAreDisabledAtRuntime() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            owner.applyCss();
            transition.install();
            M3MotionSettings.setReducedMotionRequested(owner, false);
            try {
                target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
                transition.animateEffectFromCss();

                DropShadow animated = assertInstanceOf(DropShadow.class, target.getEffect());
                assertEquals(0.0, animated.getRadius(), 0.0001);
                assertTrue(transition.isRunning());

                M3MotionSettings.setReducedMotionRequested(owner, true);

                DropShadow settled = assertInstanceOf(DropShadow.class, target.getEffect());
                assertEquals(8.0, settled.getRadius(), 0.0001);
                assertEquals(3.0, settled.getOffsetY(), 0.0001);
                assertFalse(transition.isRunning());
            } finally {
                M3MotionSettings.setReducedMotionRequested(owner, false);
                transition.uninstall();
            }
        });
    }

    /// Verifies that animated shadows retain their CSS origin and reuse one rendered effect across interruptions.
    @Test
    void animatedShadowRetainsCssOriginAndIsReused() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            transition.install();
            M3MotionSettings.setReducedMotionRequested(owner, false);
            try {
                target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
                transition.animateEffectFromCss();
                DropShadow firstAnimated = assertInstanceOf(DropShadow.class, target.getEffect());
                @SuppressWarnings("unchecked")
                StyleableProperty<Effect> effectProperty = (StyleableProperty<Effect>) target.effectProperty();

                assertEquals(StyleOrigin.INLINE, effectProperty.getStyleOrigin());

                target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 12, 0.18, 0, 5);");
                transition.animateEffectFromCss();

                assertSame(firstAnimated, target.getEffect());
                assertEquals(StyleOrigin.INLINE, effectProperty.getStyleOrigin());

                M3MotionSettings.setReducedMotionRequested(owner, true);

                DropShadow settled = assertInstanceOf(DropShadow.class, target.getEffect());
                assertEquals(12.0, settled.getRadius(), 0.0001);
                assertEquals(5.0, settled.getOffsetY(), 0.0001);
                assertEquals(StyleOrigin.INLINE, effectProperty.getStyleOrigin());
            } finally {
                M3MotionSettings.setReducedMotionRequested(owner, false);
                transition.uninstall();
            }
        });
    }

    /// Verifies that uninstalling a running transition leaves the exact CSS target elevation rendered.
    @Test
    void uninstallSettlesRunningTransition() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);

            transition.install();
            target.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.18, 0, 3);");
            transition.animateEffectFromCss();
            transition.uninstall();

            DropShadow settled = assertInstanceOf(DropShadow.class, target.getEffect());
            assertEquals(8.0, settled.getRadius(), 0.0001);
            assertEquals(3.0, settled.getOffsetY(), 0.0001);
            assertFalse(transition.isRunning());
        });
    }

    /// Verifies that non-shadow application effects are preserved rather than interpreted as zero elevation.
    @Test
    void preservesUnsupportedCssEffects() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Region target = new Region();
            owner.getChildren().add(target);
            new Scene(owner, 100.0, 40.0);
            M3CssEffectTransition transition = new M3CssEffectTransition(owner, target);
            GaussianBlur customEffect = new GaussianBlur(4.0);

            transition.install();
            target.setEffect(customEffect);
            transition.animateEffectFromCss();

            assertSame(customEffect, target.getEffect());
            assertFalse(transition.isRunning());

            owner.setDisable(true);

            assertSame(customEffect, target.getEffect());
            assertFalse(transition.isRunning());

            transition.uninstall();
        });
    }
}
