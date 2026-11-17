// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Button;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests runtime M3FX motion settings.
@NotNullByDefault
final class M3MotionSettingsTest {
    /// Starts JavaFX for CSS inheritance checks.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies the application-wide reduced-motion request.
    @Test
    void globalReducedMotionRequestControlsDefaultState() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane owner = new Pane();
            M3MotionSettings.setGlobalReducedMotionRequested(true);
            assertTrue(M3MotionSettings.isGlobalReducedMotionRequested());
            assertTrue(M3MotionSettings.globalReducedMotionRequestedProperty().get());
            assertTrue(M3MotionSettings.shouldReduceMotion(owner));

            M3MotionSettings.setGlobalReducedMotionRequested(false);
            assertFalse(M3MotionSettings.isGlobalReducedMotionRequested());
            assertFalse(M3MotionSettings.shouldReduceMotion(owner));
        });
    }

    /// Ancestor declarations cross ordinary layout nodes and permit nearer overrides.
    @Test
    void cssSettingsInheritAndAllowOverrides() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            M3Button button = new M3Button("Nested");
            M3Button sibling = new M3Button("Sibling");
            Pane scope = new Pane(new Pane(button));
            scope.getChildren().get(0).setStyle("-fx-opacity: 0.9;");
            Pane root = new Pane(scope, sibling);
            new Scene(root);
            root.setStyle("-m3-animations-enabled: false;");
            root.applyCss();
            assertFalse(button.isAnimationsEnabled());
            assertFalse(sibling.isAnimationsEnabled());
            assertTrue(M3MotionSettings.shouldReduceMotion(button));

            scope.setStyle("-m3-animations-enabled: true;");
            root.applyCss();
            assertTrue(button.isAnimationsEnabled());
            assertFalse(sibling.isAnimationsEnabled());

            button.setStyle("-m3-animations-enabled: false;");
            root.applyCss();
            assertFalse(button.isAnimationsEnabled());
            button.setStyle("");
            root.applyCss();
            assertTrue(button.isAnimationsEnabled());

            scope.setStyle("");
            root.applyCss();
            assertFalse(button.isAnimationsEnabled());
            root.setStyle("");
            root.applyCss();
            assertTrue(button.isAnimationsEnabled());
            assertTrue(sibling.isAnimationsEnabled());
        }));
    }

    /// Bindings override CSS without overriding the application-wide reduced-motion constraint.
    @Test
    void propertyBindingAndGlobalConstraintRemainIndependent() {
        FxTestUtils.runOnFxThread(() -> FxTestUtils.runWithMotionSettingsPreserved(() -> {
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            M3Button button = new M3Button();
            Pane root = new Pane(button);
            new Scene(root);
            root.setStyle("-m3-animations-enabled: false;");
            SimpleBooleanProperty enabled = new SimpleBooleanProperty(true);
            button.animationsEnabledProperty().bind(enabled);
            root.applyCss();
            assertTrue(button.isAnimationsEnabled());
            assertFalse(M3MotionSettings.shouldReduceMotion(button));
            M3MotionSettings.setGlobalReducedMotionRequested(true);
            assertTrue(button.isAnimationsEnabled());
            assertTrue(M3MotionSettings.shouldReduceMotion(button));
            M3MotionSettings.setGlobalReducedMotionRequested(false);
            enabled.set(false);
            assertTrue(M3MotionSettings.shouldReduceMotion(button));
            button.animationsEnabledProperty().unbind();
            button.setAnimationsEnabled(true);
            assertFalse(M3MotionSettings.shouldReduceMotion(button));
        }));
    }

    /// Reparenting recomputes inheritance on the next CSS pass in the same scene.
    @Test
    void cssSettingsFollowReparenting() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button();
            Pane disabled = new Pane(new Pane(button));
            Pane enabled = new Pane();
            Pane root = new Pane(disabled, enabled);
            new Scene(root);
            disabled.setStyle("-m3-animations-enabled: false;");
            root.applyCss();
            assertFalse(button.isAnimationsEnabled());
            Pane branch = (Pane) disabled.getChildren().remove(0);
            enabled.getChildren().add(branch);
            root.applyCss();
            assertTrue(button.isAnimationsEnabled());
        });
    }

    /// Root-only stylesheet rules update descendants when their pseudo-class changes.
    @Test
    void rootPseudoClassControlsInheritedMotion() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button();
            Pane root = new Pane(new Pane(button));
            root.getChildren().get(0).setStyle("-fx-opacity: 0.9;");
            root.getStyleClass().add("motion-scope");
            Scene scene = new Scene(root);
            String css = ".motion-scope:reduced-motion { -m3-animations-enabled: false; }";
            scene.getStylesheets().add("data:text/css;base64,"
                    + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8)));
            PseudoClass reducedMotion = PseudoClass.getPseudoClass("reduced-motion");
            root.applyCss();
            assertTrue(button.isAnimationsEnabled());
            for (int i = 0; i < 3; i++) {
                root.pseudoClassStateChanged(reducedMotion, true);
                root.applyCss();
                assertFalse(button.isAnimationsEnabled());
                root.pseudoClassStateChanged(reducedMotion, false);
                root.applyCss();
                assertTrue(button.isAnimationsEnabled());
            }
        });
    }

    /// Verifies read-only motion queries do not allocate JavaFX node property maps.
    @Test
    void motionQueriesDoNotAllocateNodePropertyMaps() {
        FxTestUtils.runWithMotionSettingsPreserved(() -> {
            Pane root = new Pane();
            Pane child = new Pane();
            Pane nested = new Pane();
            root.getChildren().add(child);
            child.getChildren().add(nested);

            assertFalse(root.hasProperties());
            assertFalse(child.hasProperties());
            assertFalse(nested.hasProperties());

            M3MotionSettings.shouldReduceMotion(nested);
            assertFalse(root.hasProperties());
            assertFalse(child.hasProperties());
            assertFalse(nested.hasProperties());
        });
    }
}
