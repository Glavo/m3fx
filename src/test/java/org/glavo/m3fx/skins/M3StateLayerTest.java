// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
        Platform.setImplicitExit(false);
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

    /// Verifies that keyboard-visible focus renders a bounded Material focus indicator for button-like controls.
    @Test
    void focusVisibleRendersBoundedFocusIndicatorForButtonLikeControls() {
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
                            M3StateLayerTokens.create(0.08, 0.10, 0.10, 0.16, 0.12, 0.38, 4.0, 3.0, -5.0),
                            baseTokens.componentTokens()
                    )
            );

            M3ThemeManager.install(scene, tokenTheme);
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);

            Region focusIndicator = lookupRegion(stateLayer, ".m3-focus-indicator");
            assertEquals(0.0, focusIndicator.getOpacity(), 0.0001);

            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            assertEquals(1.0, focusIndicator.getOpacity(), 0.0001);
            assertEquals(0.0, focusIndicator.getLayoutX(), 0.0001);
            assertEquals(0.0, focusIndicator.getLayoutY(), 0.0001);
            assertEquals(100.0, focusIndicator.getWidth(), 0.0001);
            assertEquals(40.0, focusIndicator.getHeight(), 0.0001);
            assertTrue(focusIndicator.getStyle().contains("-fx-border-insets: 0.0px"));
            assertTrue(focusIndicator.getStyle().contains("-fx-border-width: 4.0px"));
            assertTrue(focusIndicator.getStyle().contains("20.0px"));
            assertFalse(stateLayer.isFocusIndicatorOpacityAnimationRunning());
        });
    }

    /// Verifies that item-style controls render keyboard focus indicators inside their container bounds.
    @Test
    void focusVisibleRendersInnerFocusIndicatorForItemControls() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-list-item");
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
                            M3StateLayerTokens.create(0.08, 0.10, 0.10, 0.16, 0.12, 0.38, 4.0, 3.0, -5.0),
                            baseTokens.componentTokens()
                    )
            );

            M3ThemeManager.install(scene, tokenTheme);
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            Region focusIndicator = lookupRegion(stateLayer, ".m3-focus-indicator");
            assertEquals(1.0, focusIndicator.getOpacity(), 0.0001);
            assertEquals(0.0, focusIndicator.getLayoutX(), 0.0001);
            assertEquals(0.0, focusIndicator.getLayoutY(), 0.0001);
            assertEquals(100.0, focusIndicator.getWidth(), 0.0001);
            assertEquals(40.0, focusIndicator.getHeight(), 0.0001);
            assertTrue(focusIndicator.getStyle().contains("-fx-border-insets: 5.0px"));
            assertTrue(focusIndicator.getStyle().contains("15.0px"));
        });
    }

    /// Verifies that keyboard focus indicator opacity animates when motion is enabled.
    @Test
    void focusIndicatorOpacityAnimatesWithMotionEnabled() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            owner.getStyleClass().add("m3-button");
            M3MotionSettings.setAnimationsEnabled(owner, true);
            M3StateLayer stateLayer = new M3StateLayer();
            owner.getChildren().add(stateLayer);
            Scene scene = new Scene(owner, 100.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            owner.applyCss();
            stateLayer.installStateTransitions(owner);
            stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);

            Region focusIndicator = lookupRegion(stateLayer, ".m3-focus-indicator");
            owner.pseudoClassStateChanged(M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS, true);
            stateLayer.animateOverlayOpacityFromOwnerState();

            assertEquals(0.0, focusIndicator.getOpacity(), 0.0001);
            assertTrue(stateLayer.isFocusIndicatorOpacityAnimationRunning());
            M3MotionSettings.clearAnimationsEnabled(owner);
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
            M3MotionSettings.setAnimationsEnabled(root, false);
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

            button.setSkin(new M3ButtonSkin(button));
            navigationItem.setSkin(new M3NavigationItemSkin(navigationItem));
            checkBox.setSkin(new M3CheckBoxSkin(checkBox));
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
    @Test
    void rippleHoldsUntilReleaseThenFades() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3StateLayer> stateLayerReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> rippleReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> releaseOpacityReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> rippleReachedExpandedFrame(rippleReference),
                    2,
                    () -> showRippleStateLayer(stageReference, stateLayerReference, rippleReference, 20.0, 20.0),
                    () -> {
                        M3StateLayer stateLayer = Objects.requireNonNull(
                                stateLayerReference.get(),
                                "stateLayer"
                        );
                        Region ripple = Objects.requireNonNull(rippleReference.get(), "ripple");
                        double releaseOpacity = ripple.getOpacity();

                        assertTrue(releaseOpacity > 0.1);
                        assertEquals(1.0, ripple.getScaleX(), 0.001);
                        assertEquals(1.0, ripple.getScaleY(), 0.001);

                        releaseOpacityReference.set(releaseOpacity);
                        stateLayer.releaseRipple();

                        assertTrue(ripple.getOpacity() > 0.1);
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
        } finally {
            closeRippleStateLayer(stageReference);
        }
    }

    /// Verifies that an early release lets the ripple expand while it fades.
    @Test
    void rippleReleasedBeforeExpansionCompletesStillExpandsWhileFading() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3StateLayer> stateLayerReference = new AtomicReference<>();
        AtomicReference<@Nullable Region> rippleReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> startScaleReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> startOpacityReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> rippleExpandsWhileFading(rippleReference, startScaleReference, startOpacityReference),
                    () -> {
                        showRippleStateLayer(stageReference, stateLayerReference, rippleReference, 12.0, 20.0);

                        M3StateLayer stateLayer = Objects.requireNonNull(
                                stateLayerReference.get(),
                                "stateLayer"
                        );
                        Region ripple = Objects.requireNonNull(rippleReference.get(), "ripple");

                        startScaleReference.set(rippleScale(ripple));
                        startOpacityReference.set(ripple.getOpacity());
                        stateLayer.releaseRipple();

                        assertTrue(ripple.getOpacity() > 0.1);
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
        } finally {
            closeRippleStateLayer(stageReference);
        }
    }

    /// Returns a region looked up below a node.
    private static Region lookupRegion(javafx.scene.Node node, String selector) {
        javafx.scene.Node child = node.lookup(selector);
        assertInstanceOf(Region.class, child);
        return (Region) child;
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
        javafx.scene.Parent parent = node.getParent();
        while (parent != null) {
            if (parent == ancestor) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /// Shows a real-window state layer and starts a ripple from the supplied origin.
    private static void showRippleStateLayer(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable M3StateLayer> stateLayerReference,
            AtomicReference<@Nullable Region> rippleReference,
            double originX,
            double originY
    ) {
        Pane root = new Pane();
        M3StateLayer stateLayer = new M3StateLayer();
        root.getChildren().add(stateLayer);
        Scene scene = new Scene(root, 100.0, 40.0);
        Stage stage = new Stage();

        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.resize(100.0, 40.0);
        root.layout();
        stateLayer.layoutLayer(0.0, 0.0, 100.0, 40.0, 20.0);
        stateLayer.playRipple(originX, originY);

        stageReference.set(stage);
        stateLayerReference.set(stateLayer);
        rippleReference.set(lookupRegion(stateLayer, ".m3-ripple"));
    }

    /// Closes a state-layer ripple test window.
    private static void closeRippleStateLayer(AtomicReference<@Nullable Stage> stageReference) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable Stage stage = stageReference.get();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Returns whether the ripple reached the expanded press-held frame.
    private static boolean rippleReachedExpandedFrame(AtomicReference<@Nullable Region> rippleReference) {
        @Nullable Region ripple = rippleReference.get();
        return ripple != null
                && ripple.getOpacity() > 0.1
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
