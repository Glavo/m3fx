// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies popup theme synchronization, positioning, and motion lifecycle.
@NotNullByDefault
final class M3PopupIntegrationTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that popup context synchronizers mirror owner scene stylesheet list mutations.
    @Test
    void popupContextSynchronizerTracksRuntimeStylesheetChanges() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane root = new Pane(owner);
            Pane popupRoot = new Pane();
            String firstStylesheet = M3Stylesheets.controlStylesheet("button.css");
            String secondStylesheet = M3Stylesheets.controlStylesheet("text-field.css");
            String controlStylesheet = M3Stylesheets.controlStylesheet("menu.css");
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(
                    owner,
                    popupRoot,
                    controlStylesheet
            );
            M3Theme theme = M3Theme.defaultTheme();
            Scene scene = new Scene(root, 420.0, 220.0);
            try {
                M3ThemeManager.install(scene, theme);
                scene.getStylesheets().add(firstStylesheet);

                synchronizer.start();

                assertEquals(M3Stylesheets.fallbackStylesheet(), popupRoot.getStylesheets().get(0));
                assertTrue(popupRoot.getStylesheets().contains(firstStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(controlStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));

                AtomicInteger stylesheetChanges = new AtomicInteger();
                AtomicInteger styleClassChanges = new AtomicInteger();
                popupRoot.getStylesheets().addListener(
                        (ListChangeListener<String>) change -> stylesheetChanges.incrementAndGet()
                );
                popupRoot.getStyleClass().addListener(
                        (ListChangeListener<String>) change -> styleClassChanges.incrementAndGet()
                );

                synchronizer.sync();

                assertEquals(0, stylesheetChanges.get());
                assertEquals(0, styleClassChanges.get());

                scene.getStylesheets().remove(firstStylesheet);
                scene.getStylesheets().add(secondStylesheet);
                synchronizer.sync();

                assertFalse(popupRoot.getStylesheets().contains(firstStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(secondStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(controlStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));

                M3ThemeManager.uninstall(scene);

                assertFalse(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));
                assertTrue(popupRoot.getStylesheets().contains(secondStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(controlStylesheet));
            } finally {
                synchronizer.stop();
            }
        });
    }

    /// Verifies that popup context observation can restart without retaining stopped listeners or registering duplicates.
    @Test
    void popupContextSynchronizerRestartsCleanly() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane root = new Pane(owner);
            Pane popupRoot = new Pane();
            Scene scene = new Scene(root, 420.0, 220.0);
            AtomicInteger stylesheetReads = new AtomicInteger();
            String firstStylesheet = M3Stylesheets.controlStylesheet("button.css");
            String secondStylesheet = M3Stylesheets.controlStylesheet("text-field.css");
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(
                    owner,
                    popupRoot,
                    () -> {
                        stylesheetReads.incrementAndGet();
                        return scene.getStylesheets();
                    },
                    () -> null
            );

            synchronizer.start();
            int readsAfterStart = stylesheetReads.get();
            assertEquals(1, readsAfterStart);
            synchronizer.stop();

            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            scene.getStylesheets().add(firstStylesheet);

            assertEquals(readsAfterStart, stylesheetReads.get());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getEffectiveNodeOrientation());
            assertFalse(popupRoot.getStylesheets().contains(firstStylesheet));

            synchronizer.start();
            int readsAfterRestart = stylesheetReads.get();

            assertEquals(readsAfterStart + 1, readsAfterRestart);
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, popupRoot.getEffectiveNodeOrientation());
            assertTrue(popupRoot.getStylesheets().contains(firstStylesheet));

            scene.getStylesheets().add(secondStylesheet);
            synchronizer.sync();

            assertEquals(readsAfterRestart + 1, stylesheetReads.get());
            assertTrue(popupRoot.getStylesheets().contains(secondStylesheet));

            synchronizer.stop();
        });
    }

    /// Verifies that one stylesheet mutation burst schedules only one popup synchronization pass.
    @Test
    void popupContextSynchronizerCoalescesStylesheetMutationBurst() {
        AtomicInteger stylesheetReads = new AtomicInteger();
        AtomicReference<@Nullable M3PopupContextSynchronizer> synchronizerReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> popupRootReference = new AtomicReference<>();
        String firstStylesheet = M3Stylesheets.controlStylesheet("button.css");
        String secondStylesheet = M3Stylesheets.controlStylesheet("text-field.css");

        try {
            FxTestUtils.runOnFxThread(() -> {
                Label owner = new Label("Owner");
                Pane root = new Pane(owner);
                Pane popupRoot = new Pane();
                Scene scene = new Scene(root, 420.0, 220.0);
                scene.getStylesheets().add(firstStylesheet);
                M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(
                        owner,
                        popupRoot,
                        () -> {
                            stylesheetReads.incrementAndGet();
                            return scene.getStylesheets();
                        },
                        () -> null
                );
                synchronizerReference.set(synchronizer);
                popupRootReference.set(popupRoot);
                synchronizer.start();
                stylesheetReads.set(0);

                scene.getStylesheets().remove(firstStylesheet);
                scene.getStylesheets().add(secondStylesheet);

                assertEquals(0, stylesheetReads.get());
            });

            FxTestUtils.runOnFxThread(() -> {
                Pane popupRoot = Objects.requireNonNull(popupRootReference.get(), "popupRoot");
                assertEquals(1, stylesheetReads.get());
                assertFalse(popupRoot.getStylesheets().contains(firstStylesheet));
                assertTrue(popupRoot.getStylesheets().contains(secondStylesheet));
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3PopupContextSynchronizer synchronizer = synchronizerReference.get();
                if (synchronizer != null) {
                    synchronizer.stop();
                }
            });
        }
    }

    /// Verifies a failed initial popup synchronization removes every listener and can be retried cleanly.
    @Test
    void popupContextSynchronizerRollsBackFailedStart() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button owner = new M3Button("Owner");
            Pane root = new Pane(owner);
            M3Scrim popupRoot = new M3Scrim();
            Scene scene = new Scene(root, 420.0, 220.0);
            AtomicInteger stylesheetReads = new AtomicInteger();
            AtomicBoolean failSynchronization = new AtomicBoolean(true);
            String stylesheet = M3Stylesheets.controlStylesheet("button.css");
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(
                    owner,
                    popupRoot,
                    () -> {
                        stylesheetReads.incrementAndGet();
                        if (failSynchronization.get()) {
                            throw new IllegalStateException("stylesheet lookup failed");
                        }
                        return scene.getStylesheets();
                    },
                    () -> null
            );

            try {
                assertThrows(IllegalStateException.class, synchronizer::start);
                int failedStartReads = stylesheetReads.get();

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                scene.getStylesheets().add(stylesheet);
                FxTestUtils.setCssAnimationsEnabled(root, false);

                assertEquals(failedStartReads, stylesheetReads.get());
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getEffectiveNodeOrientation());
                assertTrue(popupRoot.isAnimationsEnabled());
                assertFalse(popupRoot.getStylesheets().contains(stylesheet));

                failSynchronization.set(false);
                synchronizer.start();

                assertEquals(NodeOrientation.RIGHT_TO_LEFT, popupRoot.getEffectiveNodeOrientation());
                assertFalse(popupRoot.isAnimationsEnabled());
                assertTrue(popupRoot.getStylesheets().contains(stylesheet));
            } finally {
                synchronizer.stop();
                FxTestUtils.setCssAnimationsEnabled(root, true);
            }
        });
    }

    /// Verifies that popup context synchronizers mirror owner effective orientation changes.
    @Test
    void popupContextSynchronizerTracksRuntimeOwnerOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane root = new Pane(owner);
            Pane popupRoot = new Pane();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            new Scene(root, 420.0, 220.0);

            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            synchronizer.start();

            assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getEffectiveNodeOrientation());

            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, popupRoot.getNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, popupRoot.getEffectiveNodeOrientation());

            owner.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

            assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getEffectiveNodeOrientation());

            synchronizer.stop();
            owner.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getNodeOrientation());
        });
    }

    /// Verifies that popup context synchronizers leave bound popup orientation properties under owner control.
    @Test
    void popupContextSynchronizerSkipsBoundPopupOrientationProperty() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane root = new Pane(owner);
            Pane popupRoot = new Pane();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            new Scene(root, 420.0, 220.0);

            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            popupRoot.nodeOrientationProperty().bind(root.effectiveNodeOrientationProperty());
            try {
                synchronizer.start();

                assertTrue(popupRoot.nodeOrientationProperty().isBound());
                assertEquals(NodeOrientation.LEFT_TO_RIGHT, popupRoot.getEffectiveNodeOrientation());

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

                assertTrue(popupRoot.nodeOrientationProperty().isBound());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, popupRoot.getEffectiveNodeOrientation());
            } finally {
                synchronizer.stop();
                popupRoot.nodeOrientationProperty().unbind();
            }
        });
    }

    /// Verifies that popup context synchronizers mirror inherited local motion setting changes.
    @Test
    void popupContextSynchronizerTracksRuntimeLocalMotionSettings() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button owner = new M3Button("Owner");
            Pane root = new Pane(owner);
            M3Scrim popupRoot = new M3Scrim();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            new Scene(root, 420.0, 220.0);

            try {
                FxTestUtils.setCssAnimationsEnabled(root, false);
                synchronizer.start();

                assertFalse(popupRoot.isAnimationsEnabled());
                assertTrue(M3MotionSettings.shouldReduceMotion(popupRoot));

                FxTestUtils.setCssAnimationsEnabled(owner, true);

                assertTrue(popupRoot.isAnimationsEnabled());
                assertFalse(M3MotionSettings.shouldReduceMotion(popupRoot));

                synchronizer.stop();
                FxTestUtils.setCssAnimationsEnabled(owner, false);

                assertTrue(popupRoot.isAnimationsEnabled());
                assertFalse(M3MotionSettings.shouldReduceMotion(popupRoot));
            } finally {
                synchronizer.stop();
                FxTestUtils.setCssAnimationsEnabled(owner, true);
                FxTestUtils.setCssAnimationsEnabled(root, true);
            }
        });
    }

    /// Verifies that popup context synchronizers mirror reduced motion and theme motion tokens.
    @Test
    void popupContextSynchronizerTracksReducedMotionAndThemeMotion() {
        FxTestUtils.runOnFxThread(() -> {
            boolean previousReducedMotionRequested = M3MotionSettings.isGlobalReducedMotionRequested();
            M3Button owner = new M3Button("Owner");
            Pane root = new Pane(owner);
            M3Scrim popupRoot = new M3Scrim();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            M3Theme standardTheme = M3Theme.defaultTheme();
            M3Theme expressiveTheme = M3Theme.fromSeed(
                    M3Theme.DEFAULT_SEED_COLOR,
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );
            new Scene(root, 420.0, 220.0);

            try {
                M3MotionSettings.setGlobalReducedMotionRequested(false);
                M3ThemeManager.install(root, standardTheme);
                synchronizer.start();

                assertTrue(popupRoot.isAnimationsEnabled());
                assertSame(standardTheme, M3ThemeManager.getTheme(popupRoot));
                assertSame(standardTheme.tokens().motionTokens().scheme(), M3Animation.motionScheme(popupRoot));
                assertSame(standardTheme.tokens().motionTokens().behavior(), M3Animation.motionBehavior(popupRoot));

                M3MotionSettings.setGlobalReducedMotionRequested(true);
                M3ThemeManager.install(root, expressiveTheme);

                assertTrue(popupRoot.isAnimationsEnabled());
                assertTrue(M3MotionSettings.shouldReduceMotion(popupRoot));
                assertSame(expressiveTheme, M3ThemeManager.getTheme(popupRoot));
                assertSame(expressiveTheme.tokens().motionTokens().scheme(), M3Animation.motionScheme(popupRoot));
                assertSame(expressiveTheme.tokens().motionTokens().behavior(), M3Animation.motionBehavior(popupRoot));

                synchronizer.stop();
                M3MotionSettings.setGlobalReducedMotionRequested(false);
                M3ThemeManager.install(root, standardTheme);

                assertTrue(popupRoot.isAnimationsEnabled());
                assertFalse(M3MotionSettings.shouldReduceMotion(popupRoot));
                assertSame(expressiveTheme, M3ThemeManager.getTheme(popupRoot));
            } finally {
                synchronizer.stop();
                M3MotionSettings.setGlobalReducedMotionRequested(previousReducedMotionRequested);
                M3ThemeManager.uninstall(root);
            }
        });
    }

    /// Verifies that motion changes update popup overrides without rebuilding stylesheet and theme context.
    @Test
    void popupContextSynchronizerUpdatesOnlyRelevantMotionContext() {
        FxTestUtils.runOnFxThread(() -> {
            boolean previousReducedMotionRequested = M3MotionSettings.isGlobalReducedMotionRequested();
            M3Button unrelated = new M3Button();
            M3Button owner = new M3Button("Owner");
            Pane root = new Pane(owner);
            M3Scrim popupRoot = new M3Scrim();
            Scene scene = new Scene(root, 420.0, 220.0);
            AtomicInteger syncCount = new AtomicInteger();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(
                    owner,
                    popupRoot,
                    () -> {
                        syncCount.incrementAndGet();
                        return scene.getStylesheets();
                    },
                    () -> null
            );

            try {
                M3MotionSettings.setGlobalReducedMotionRequested(false);
                synchronizer.start();
                syncCount.set(0);

                unrelated.setAnimationsEnabled(false);

                assertEquals(0, syncCount.get());
                assertFalse(M3MotionSettings.shouldReduceMotion(popupRoot));

                FxTestUtils.setCssAnimationsEnabled(root, false);

                assertEquals(0, syncCount.get());
                assertFalse(popupRoot.isAnimationsEnabled());
                assertTrue(M3MotionSettings.shouldReduceMotion(popupRoot));
            } finally {
                synchronizer.stop();
                unrelated.setAnimationsEnabled(true);
                FxTestUtils.setCssAnimationsEnabled(root, true);
                M3MotionSettings.setGlobalReducedMotionRequested(previousReducedMotionRequested);
            }
        });
    }

    /// Verifies that generated theme rules keep higher priority than popup control stylesheets.
    @Test
    void popupContextSynchronizerKeepsThemeStylesheetAfterPopupControlStylesheets() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane ownerContainer = new Pane(owner);
            Pane root = new Pane(ownerContainer);
            Pane popupRoot = new Pane();
            String controlStylesheet = M3Stylesheets.controlStylesheet("menu.css");
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(
                    owner,
                    popupRoot,
                    controlStylesheet
            );
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Scene scene = new Scene(root, 420.0, 220.0);
            try {
                M3ThemeManager.install(scene, baselineTheme);
                synchronizer.start();

                String baselineThemeStylesheet = M3ThemeRuntime.themeStylesheetUrl(baselineTheme);
                List<String> baselineStylesheets = popupRoot.getStylesheets();
                assertTrue(baselineStylesheets.indexOf(controlStylesheet)
                        < baselineStylesheets.lastIndexOf(baselineThemeStylesheet));
                assertEquals(baselineStylesheets.size() - 1, baselineStylesheets.lastIndexOf(baselineThemeStylesheet));

                M3ThemeManager.install(scene, expressiveDarkTheme);

                String expressiveThemeStylesheet = M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme);
                List<String> expressiveStylesheets = popupRoot.getStylesheets();
                assertFalse(expressiveStylesheets.contains(baselineThemeStylesheet));
                assertTrue(expressiveStylesheets.indexOf(controlStylesheet)
                        < expressiveStylesheets.lastIndexOf(expressiveThemeStylesheet));
                assertEquals(expressiveStylesheets.size() - 1, expressiveStylesheets.lastIndexOf(expressiveThemeStylesheet));

                M3ThemeManager.uninstall(scene);
                M3ThemeManager.install(ownerContainer, baselineTheme);

                List<String> localStylesheets = popupRoot.getStylesheets();
                assertTrue(localStylesheets.indexOf(controlStylesheet)
                        < localStylesheets.lastIndexOf(baselineThemeStylesheet));
                assertEquals(localStylesheets.size() - 1, localStylesheets.lastIndexOf(baselineThemeStylesheet));
            } finally {
                synchronizer.stop();
            }
        });
    }

    /// Verifies that popup theme synchronizers follow scene root replacement.
    @Test
    void popupContextSynchronizerReinheritsRuntimeSceneRootReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane ownerContainer = new Pane(owner);
            Pane popupRoot = new Pane();
            popupRoot.setStyle("-fx-padding: 7px;");
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Pane firstRoot = new Pane(ownerContainer);
            Scene scene = new Scene(firstRoot, 420.0, 220.0);

            M3ThemeManager.install(scene, baselineTheme);
            firstRoot.applyCss();
            firstRoot.layout();

            try {
                synchronizer.start();

                assertSame(baselineTheme, M3ThemeManager.getTheme(popupRoot));
                assertEquals(
                        "-fx-padding: 7px;",
                        popupRoot.getStyle()
                );
                assertTrue(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(baselineTheme)));

                Pane secondRoot = new Pane();
                firstRoot.getChildren().remove(ownerContainer);
                secondRoot.getChildren().add(ownerContainer);
                scene.setRoot(secondRoot);

                assertSame(baselineTheme, M3ThemeManager.getTheme(popupRoot));

                M3ThemeManager.install(scene, expressiveDarkTheme);

                assertSame(expressiveDarkTheme, M3ThemeManager.getTheme(popupRoot));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)));

                M3ThemeManager.uninstall(scene);

                assertNull(M3ThemeManager.getTheme(popupRoot));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

                Pane unthemedRoot = new Pane();
                secondRoot.getChildren().remove(ownerContainer);
                unthemedRoot.getChildren().add(ownerContainer);
                scene.setRoot(unthemedRoot);

                assertNull(M3ThemeManager.getTheme(popupRoot));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
            } finally {
                synchronizer.stop();
            }
        });
    }

    /// Verifies that popup theme synchronizers follow owner subtree reparenting between local themes.
    @Test
    void popupContextSynchronizerReinheritsRuntimeLocalThemeAfterReparenting() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane ownerContainer = new Pane(owner);
            Pane popupRoot = new Pane();
            popupRoot.setStyle("-fx-padding: 7px;");
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            try {
                Pane firstLocalRoot = new Pane(ownerContainer);
                Pane secondLocalRoot = new Pane();
                Pane root = new Pane(firstLocalRoot, secondLocalRoot);
                M3ThemeManager.install(firstLocalRoot, baselineTheme);
                M3ThemeManager.install(secondLocalRoot, expressiveDarkTheme);
                new Scene(root, 420.0, 220.0);
                root.applyCss();
                root.layout();

                synchronizer.start();

                assertSame(baselineTheme, M3ThemeManager.getTheme(popupRoot));

                firstLocalRoot.getChildren().remove(ownerContainer);
                secondLocalRoot.getChildren().add(ownerContainer);

                assertSame(expressiveDarkTheme, M3ThemeManager.getTheme(popupRoot));
                assertEquals("-fx-padding: 7px;", popupRoot.getStyle());
                assertTrue(popupRoot.getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)
                ));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

                secondLocalRoot.getChildren().remove(ownerContainer);
                root.getChildren().add(ownerContainer);

                assertNull(M3ThemeManager.getTheme(popupRoot));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertFalse(popupRoot.getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)
                ));
                assertEquals("-fx-padding: 7px;", popupRoot.getStyle());
            } finally {
                synchronizer.stop();
            }
        });
    }

    /// Verifies that popup theme synchronizers observe local themes installed after the popup is already open.
    @Test
    void popupContextSynchronizerTracksRuntimeLocalThemeInstallation() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane ownerContainer = new Pane(owner);
            Pane root = new Pane(ownerContainer);
            Pane popupRoot = new Pane();
            popupRoot.setStyle("-fx-padding: 9px;");
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            M3Theme localTheme = M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            new Scene(root, 420.0, 220.0);

            root.applyCss();
            root.layout();

            try {
                synchronizer.start();

                assertNull(M3ThemeManager.getTheme(popupRoot));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertEquals("-fx-padding: 9px;", popupRoot.getStyle());

                M3ThemeManager.install(ownerContainer, localTheme);

                assertSame(localTheme, M3ThemeManager.getTheme(popupRoot));
                assertEquals("-fx-padding: 9px;", popupRoot.getStyle());
                assertTrue(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(localTheme)));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

                M3ThemeManager.uninstall(ownerContainer);

                assertNull(M3ThemeManager.getTheme(popupRoot));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertFalse(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(localTheme)));
                assertEquals("-fx-padding: 9px;", popupRoot.getStyle());
            } finally {
                synchronizer.stop();
            }
        });
    }

    /// Verifies that stopped popup theme synchronizers no longer observe owner context changes.
    @Test
    void popupContextSynchronizerStopsRuntimeContextObservation() {
        FxTestUtils.runOnFxThread(() -> {
            Label owner = new Label("Owner");
            Pane ownerContainer = new Pane(owner);
            Pane root = new Pane(ownerContainer);
            Pane popupRoot = new Pane();
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popupRoot);
            M3Theme sceneTheme = M3Theme.defaultTheme();
            M3Theme localTheme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Scene scene = new Scene(root, 420.0, 220.0);

            M3ThemeManager.install(scene, sceneTheme);
            root.applyCss();
            root.layout();

            synchronizer.start();
            assertSame(sceneTheme, M3ThemeManager.getTheme(popupRoot));

            synchronizer.stop();
            M3ThemeManager.install(ownerContainer, localTheme);
            M3ThemeManager.install(scene, localTheme);

            assertSame(sceneTheme, M3ThemeManager.getTheme(popupRoot));
            assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
            assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
            assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
            assertFalse(popupRoot.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(localTheme)));
        });
    }

    /// Verifies that hidden popup owners do not open popups or route accessibility focus into hidden content.
    @Test
    void popupOwnersSkipAccessibleFocusAndRevealWhenHiddenByAncestor() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem buttonTarget = new M3MenuItem("Button target");
            M3MenuButton menuButton = new M3MenuButton("More", buttonTarget);

            M3MenuItem subTarget = new M3MenuItem("Sub target");
            M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", subTarget);

            M3MenuItem splitTarget = new M3MenuItem("Split target");
            M3SplitButton splitButton = splitButton("Create", splitTarget);

            M3FloatingActionButton fabAction = new M3FloatingActionButton("A");
            M3FabMenu fabMenu = new M3FabMenu();
            fabMenu.getItems().add(fabAction);

            M3Button outside = new M3Button("Outside");
            VBox hiddenOwner = new VBox(8.0, menuButton, subMenuItem, splitButton, fabMenu);
            VBox root = new VBox(8.0, outside, hiddenOwner);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 520.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                hiddenOwner.setVisible(false);
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                menuButton.executeAccessibleAction(AccessibleAction.EXPAND);
                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, buttonTarget);

                assertTrue(outside.isFocused());
                assertFalse(menuButton.isShowing());
                assertFalse(buttonTarget.isFocused());

                subMenuItem.showSubMenu();
                subMenuItem.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                subMenuItem.executeAccessibleAction(AccessibleAction.EXPAND);
                subMenuItem.executeAccessibleAction(AccessibleAction.SHOW_ITEM, subTarget);

                assertTrue(outside.isFocused());
                assertFalse(subMenuItem.isSubMenuShowing());
                assertFalse(subTarget.isFocused());

                splitButton.showMenu();
                splitButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                splitButton.executeAccessibleAction(AccessibleAction.EXPAND);
                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, splitTarget);

                assertTrue(outside.isFocused());
                assertFalse(splitButton.isShowing());
                assertFalse(splitButtonActionButton(splitButton).isFocused());
                assertFalse(splitButtonMenuButton(splitButton).isFocused());
                assertFalse(splitTarget.isFocused());

                fabMenu.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                fabMenu.executeAccessibleAction(AccessibleAction.EXPAND);
                fabMenu.executeAccessibleAction(AccessibleAction.SHOW_ITEM, fabAction);

                assertTrue(outside.isFocused());
                assertFalse(fabMenu.isExpanded());
                assertFalse(fabMenu.getToggleButton().isFocused());
                assertFalse(fabAction.isFocused());
            } finally {
                menuButton.hideMenu();
                subMenuItem.hideSubMenu();
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that hidden overlay owners do not reveal surfaces or route accessibility focus into hidden content.
    @Test
    void overlayOwnersSkipAccessibleRevealAndFocusWhenHiddenByAncestor() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button sideAction = new M3Button("Side action");
            M3SideSheet sideSheet = sideSheet("Details", new Label("Side content"), sideAction);
            sideSheet.hide();

            M3Button bottomAction = new M3Button("Bottom action");
            M3BottomSheet bottomSheet = bottomSheet("Queue", new Label("Bottom content"), bottomAction);
            bottomSheet.hide();

            M3Scrim scrim = new M3Scrim();
            scrim.hide();

            M3Button outside = new M3Button("Outside");
            VBox hiddenOwner = new VBox(8.0, sideSheet, bottomSheet, scrim);
            M3Surface ownerSurface = surface(hiddenOwner);
            VBox root = new VBox(8.0, outside, ownerSurface);
            Stage stage = new Stage();
            try {
                FxTestUtils.setCssAnimationsEnabled(root, false);
                Scene scene = new Scene(root, 620.0, 520.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                hiddenOwner.setVisible(false);
                root.applyCss();
                root.layout();

                sideSheet.executeAccessibleAction(AccessibleAction.EXPAND);
                sideSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, sideAction);
                sideSheet.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertFalse(sideSheet.isShown());
                assertFalse(sideAction.isFocused());
                assertTrue(outside.isFocused());

                bottomSheet.executeAccessibleAction(AccessibleAction.EXPAND);
                bottomSheet.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bottomAction);
                bottomSheet.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertFalse(bottomSheet.isShown());
                assertFalse(bottomAction.isFocused());
                assertTrue(outside.isFocused());

                scrim.executeAccessibleAction(AccessibleAction.EXPAND);
                scrim.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                scrim.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertFalse(scrim.isShown());
                assertFalse(scrim.isFocused());
                assertNull(scrim.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertTrue(outside.isFocused());

                assertTrue(outside.isFocused());
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that nested popup focus changes propagate through menu and split-button owners.
    @Test
    void nestedPopupMenuFocusPropagatesThroughOwners() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3MenuItem html = new M3MenuItem("HTML");
            M3SubMenuItem recent = new M3SubMenuItem("Recent", pdf, html);
            M3SubMenuItem export = new M3SubMenuItem("Export", recent);
            M3MenuButton menuButton = new M3MenuButton("More", export);

            M3MenuItem splitPdf = new M3MenuItem("PDF");
            M3MenuItem splitHtml = new M3MenuItem("HTML");
            M3SubMenuItem splitRecent = new M3SubMenuItem("Recent", splitPdf, splitHtml);
            M3SubMenuItem splitExport = new M3SubMenuItem("Export", splitRecent);
            M3SplitButton splitButton = splitButton("Create", splitExport);
            Stage stage = new Stage();
            try {
                menuButton.setAnimationsEnabled(false);
                export.setAnimationsEnabled(false);
                recent.setAnimationsEnabled(false);
                splitButton.setAnimationsEnabled(false);
                splitExport.setAnimationsEnabled(false);
                splitRecent.setAnimationsEnabled(false);

                Pane root = new Pane(menuButton, splitButton);
                stage.setScene(new Scene(root, 360.0, 220.0));
                stage.show();
                root.applyCss();
                menuButton.resizeRelocate(24.0, 24.0, 120.0, 40.0);
                splitButton.resizeRelocate(24.0, 96.0, 160.0, 40.0);
                root.layout();

                menuButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, html);

                assertTrue(menuButton.isShowing());
                assertTrue(export.isSubMenuShowing());
                assertTrue(recent.isSubMenuShowing());
                assertTrue(html.isFocused());
                assertSame(html, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                pdf.requestFocus();

                assertTrue(pdf.isFocused());
                assertSame(pdf, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                recent.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertTrue(export.isSubMenuShowing());
                assertFalse(recent.isSubMenuShowing());
                assertTrue(recent.isFocused());
                assertSame(recent, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                export.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertSame(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertSame(menuButton, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                splitButton.executeAccessibleAction(AccessibleAction.SHOW_ITEM, splitHtml);

                assertTrue(splitButton.isShowing());
                assertTrue(splitExport.isSubMenuShowing());
                assertTrue(splitRecent.isSubMenuShowing());
                assertTrue(splitHtml.isFocused());
                assertSame(splitHtml, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                splitPdf.requestFocus();

                assertTrue(splitPdf.isFocused());
                assertSame(splitPdf, splitButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.setAnimationsEnabled(true);
                export.setAnimationsEnabled(true);
                recent.setAnimationsEnabled(true);
                splitButton.setAnimationsEnabled(true);
                splitExport.setAnimationsEnabled(true);
                splitRecent.setAnimationsEnabled(true);
                menuButton.hideMenu();
                splitButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that structural containers delegate explicit accessibility reveal requests into popup-backed children.
    @Test
    void structuralContainersRouteNestedPopupAccessibleTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem formTarget = new M3MenuItem("Form archive");
            M3SubMenuItem formSubMenu = new M3SubMenuItem("Form move", formTarget);
            M3MenuButton formMenuButton = new M3MenuButton("Form menu", formSubMenu);
            M3FormRow formRow = new M3FormRow("Destination", "", formMenuButton, new M3Button("Edit"));
            M3FormSection formSection = formSection("Routing", formRow);
            M3FormPane formPane = formPane(formSection);

            M3MenuItem surfaceTarget = new M3MenuItem("Surface archive");
            M3SubMenuItem surfaceSubMenu = new M3SubMenuItem("Surface move", surfaceTarget);
            M3MenuButton surfaceMenuButton = new M3MenuButton("Surface menu", surfaceSubMenu);
            M3Surface surface = surface(surfaceMenuButton);

            M3MenuItem badgedTarget = new M3MenuItem("Badged archive");
            M3SubMenuItem badgedSubMenu = new M3SubMenuItem("Badged move", badgedTarget);
            M3MenuButton badgedMenuButton = new M3MenuButton("Badged menu", badgedSubMenu);
            M3BadgedBox badgedBox = new M3BadgedBox(badgedMenuButton, new M3Badge("2"));

            M3MenuItem bannerTarget = new M3MenuItem("Banner archive");
            M3SubMenuItem bannerSubMenu = new M3SubMenuItem("Banner move", bannerTarget);
            M3MenuButton bannerMenuButton = new M3MenuButton("Banner menu", bannerSubMenu);
            M3Banner banner = createBanner("Offline", new M3Icon("!"), bannerMenuButton);

            List<Node> popupOwners = List.of(
                    formMenuButton,
                    formSubMenu,
                    surfaceMenuButton,
                    surfaceSubMenu,
                    badgedMenuButton,
                    badgedSubMenu,
                    bannerMenuButton,
                    bannerSubMenu
            );
            Stage stage = new Stage();
            try {
                for (Node popupOwner : popupOwners) {
                    assertInstanceOf(M3Animated.class, popupOwner).setAnimationsEnabled(false);
                }

                VBox root = new VBox(12.0, formPane, surface, badgedBox, banner);
                Scene scene = new Scene(root, 560.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                formPane.executeAccessibleAction(AccessibleAction.SHOW_ITEM, List.of(formTarget));
                assertNestedPopupAccessibleFocus(formPane, formMenuButton, formSubMenu, formTarget);
                formSubMenu.hideSubMenu();
                formMenuButton.hideMenu();

                surface.executeAccessibleAction(AccessibleAction.SHOW_ITEM, surfaceTarget);
                assertNestedPopupAccessibleFocus(surface, surfaceMenuButton, surfaceSubMenu, surfaceTarget);
                surfaceSubMenu.hideSubMenu();
                surfaceMenuButton.hideMenu();

                badgedBox.executeAccessibleAction(AccessibleAction.SHOW_ITEM, badgedTarget);
                assertNestedPopupAccessibleFocus(badgedBox, badgedMenuButton, badgedSubMenu, badgedTarget);
                badgedSubMenu.hideSubMenu();
                badgedMenuButton.hideMenu();

                banner.executeAccessibleAction(AccessibleAction.SHOW_ITEM, bannerTarget);
                assertNestedPopupAccessibleFocus(banner, bannerMenuButton, bannerSubMenu, bannerTarget);
            } finally {
                bannerSubMenu.hideSubMenu();
                bannerMenuButton.hideMenu();
                badgedSubMenu.hideSubMenu();
                badgedMenuButton.hideMenu();
                surfaceSubMenu.hideSubMenu();
                surfaceMenuButton.hideMenu();
                formSubMenu.hideSubMenu();
                formMenuButton.hideMenu();
                for (Node popupOwner : popupOwners) {
                    assertInstanceOf(M3Animated.class, popupOwner).setAnimationsEnabled(true);
                }
                stage.close();
            }
        });
    }

    /// Verifies that rich tooltip popup action focus opened from a menu item is exposed through owner containers.
    @Tier2Test
    @Test
    void menuItemRichTooltipPopupActionFocusRoutesThroughOwnerStack() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Surface> surfaceReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuItem> detailsReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> actionReference = new AtomicReference<>();

        FxTestUtils.runOnFxThreadWhen(
                () -> tooltipShowingWithFocus(tooltipReference, detailsReference),
                () -> {
                    @Nullable M3MenuItem details = detailsReference.get();
                    @Nullable M3RichTooltip tooltip = tooltipReference.get();
                    boolean focusVisible = details != null && details.getPseudoClassStates().contains(
                            PseudoClass.getPseudoClass("focus-visible")
                    );
                    return "menu tooltip did not open with its owner focused: detailsFocused="
                            + (details != null && details.isFocused())
                            + ", focusVisible="
                            + focusVisible
                            + ", tooltipShowing="
                            + (tooltip != null && tooltip.isShowing());
                },
                () -> {
                    Stage stage = new Stage();
                    M3MenuItem details = new M3MenuItem("Details");
                    M3MenuButton menuButton = new M3MenuButton("More", details);
                    M3Button action = createButton("Open details", M3ButtonVariant.TEXT);
                    M3RichTooltip tooltip = installRichTooltip(
                            details,
                            "Details",
                            "Actions in a menu-owned tooltip remain reachable.",
                            action
                    );
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.setShowDuration(Duration.INDEFINITE);
                    details.setAnimationsEnabled(false);
                    M3Surface surface = surface(menuButton);
                    Scene scene = new Scene(surface, 360.0, 220.0);
                    M3ThemeManager.install(scene, M3Theme.defaultTheme());
                    stage.setScene(scene);
                    stage.show();
                    surface.applyCss();
                    surface.layout();

                    menuButton.showMenu();
                    menuButton.getMenu().applyCss();
                    menuButton.getMenu().layout();
                    details.requestFocus();
                    Bounds detailsBounds = Objects.requireNonNull(
                            details.localToScreen(details.getBoundsInLocal()),
                            "details bounds"
                    );
                    tooltip.show(details, detailsBounds.getMinX(), detailsBounds.getMaxY() + 8.0);

                    stageReference.set(stage);
                    surfaceReference.set(surface);
                    menuButtonReference.set(menuButton);
                    detailsReference.set(details);
                    tooltipReference.set(tooltip);
                    actionReference.set(action);
                },
                () -> {
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    M3Surface surface = Objects.requireNonNull(surfaceReference.get(), "surface");
                    M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                    M3MenuItem details = Objects.requireNonNull(detailsReference.get(), "details");
                    M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                    M3Button action = Objects.requireNonNull(actionReference.get(), "action");
                    try {
                        assertTrue(menuButton.isShowing());
                        assertTrue(tooltip.isShowing());
                        assertTrue(tooltip.focusFirstInteractiveTarget());
                        assertTrue(action.isFocused());
                        assertSame(action, menuButton.getMenu().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                        assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                        assertSame(action, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                        menuButton.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                        assertTrue(action.isFocused());
                        assertSame(action, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                        assertSame(action, surface.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                        action.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                        assertFalse(tooltip.isShowing());
                        assertTrue(details.isFocused());
                        assertSame(details, menuButton.getMenu().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                        assertSame(details, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                    } finally {
                        tooltip.hide();
                        menuButton.hideMenu();
                        details.setAnimationsEnabled(true);
                        stage.close();
                    }
                }
        );
    }

    /// Verifies that accessible popup collapse actions restore focus to the owning popup control.
    @Test
    void popupAccessibleCollapseRestoresOwnerFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem pdf = new M3MenuItem("PDF");
            M3SubMenuItem export = new M3SubMenuItem("Export", pdf);
            M3MenuButton menuButton = new M3MenuButton("More", export);
            Stage stage = new Stage();
            try {
                menuButton.setAnimationsEnabled(false);
                export.setAnimationsEnabled(false);
                Pane root = new Pane(menuButton);
                stage.setScene(new Scene(root, 280.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                menuButton.showMenu();
                assertTrue(export.showSubMenuAndFocusFirstItem());
                assertTrue(pdf.isFocused());

                export.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertTrue(menuButton.isShowing());
                assertFalse(export.isSubMenuShowing());
                assertTrue(export.isFocused());
                assertEquals(export, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                menuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);

                assertFalse(menuButton.isShowing());
                assertTrue(menuButton.isFocused());
                assertEquals(menuButton, menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                menuButton.setAnimationsEnabled(true);
                export.setAnimationsEnabled(true);
                menuButton.hideMenu();
                stage.close();
            }
        });
    }

    /// Verifies that repeated dismissal calls retain an earlier popup focus-restoration request.
    @Tier2Test
    @Test
    void repeatedPopupDismissalPreservesRequestedFocusRestoration() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable VBox> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DatePickerField> dateFieldReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DateRangePickerField> rangeFieldReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> {
                M3SubMenuItem subMenuItem = new M3SubMenuItem("Export", new M3MenuItem("PDF"));
                M3MenuButton menuButton = new M3MenuButton("More", subMenuItem);
                M3DatePickerField dateField = new M3DatePickerField();
                M3DateRangePickerField rangeField = new M3DateRangePickerField();
                VBox root = new VBox(12.0, menuButton, dateField, rangeField);
                Stage stage = new Stage();

                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.setScene(new Scene(root, 520.0, 360.0));
                stage.show();
                root.applyCss();
                root.layout();

                stageReference.set(stage);
                rootReference.set(root);
                menuButtonReference.set(menuButton);
                subMenuItemReference.set(subMenuItem);
                dateFieldReference.set(dateField);
                rangeFieldReference.set(rangeField);
            });

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3MenuButton menuButton = menuButtonReference.get();
                        @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
                        return menuButton != null
                                && subMenuItem != null
                                && menuButton.isShowing()
                                && !subMenuItem.isSubMenuShowing()
                                && subMenuItem.isFocused();
                    },
                    () -> {
                        M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                        M3SubMenuItem subMenuItem =
                                Objects.requireNonNull(subMenuItemReference.get(), "subMenuItem");
                        menuButton.showMenu();
                        subMenuItem.showSubMenu();
                        subMenuItem.executeAccessibleAction(AccessibleAction.COLLAPSE);
                        subMenuItem.hideSubMenu();
                    },
                    () -> {
                        M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                        M3SubMenuItem subMenuItem =
                                Objects.requireNonNull(subMenuItemReference.get(), "subMenuItem");
                        assertTrue(menuButton.isShowing());
                        assertFalse(subMenuItem.isSubMenuShowing());
                        assertTrue(subMenuItem.isFocused());
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3MenuButton menuButton = menuButtonReference.get();
                        return menuButton != null && !menuButton.isShowing() && menuButton.isFocused();
                    },
                    () -> {
                        M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                        menuButton.executeAccessibleAction(AccessibleAction.COLLAPSE);
                        menuButton.hideMenu();
                    },
                    () -> {
                        M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                        assertFalse(menuButton.isShowing());
                        assertTrue(menuButton.isFocused());
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3DatePickerField dateField = dateFieldReference.get();
                        return dateField != null && !dateField.isShowing() && dateField.getEditor().isFocused();
                    },
                    () -> {
                        M3DatePickerField dateField =
                                Objects.requireNonNull(dateFieldReference.get(), "dateField");
                        dateField.showPicker();
                        dateField.executeAccessibleAction(AccessibleAction.COLLAPSE);
                        dateField.hidePicker();
                    },
                    () -> {
                        M3DatePickerField dateField =
                                Objects.requireNonNull(dateFieldReference.get(), "dateField");
                        assertFalse(dateField.isShowing());
                        assertTrue(dateField.getEditor().isFocused());
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3DateRangePickerField rangeField = rangeFieldReference.get();
                        return rangeField != null
                                && !rangeField.isShowing()
                                && rangeField.getEndEditor().isFocused();
                    },
                    () -> {
                        M3DateRangePickerField rangeField =
                                Objects.requireNonNull(rangeFieldReference.get(), "rangeField");
                        rangeField.getEndEditor().requestFocus();
                        rangeField.showPicker();
                        rangeField.executeAccessibleAction(AccessibleAction.COLLAPSE);
                        rangeField.hidePicker();
                    },
                    () -> {
                        M3DateRangePickerField rangeField =
                                Objects.requireNonNull(rangeFieldReference.get(), "rangeField");
                        assertFalse(rangeField.isShowing());
                        assertTrue(rangeField.getEndEditor().isFocused());
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3MenuButton menuButton = menuButtonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }
                @Nullable M3DatePickerField dateField = dateFieldReference.get();
                if (dateField != null) {
                    dateField.hidePicker();
                }
                @Nullable M3DateRangePickerField rangeField = rangeFieldReference.get();
                if (rangeField != null) {
                    rangeField.hidePicker();
                }
                @Nullable VBox root = rootReference.get();
                if (root != null) {
                    FxTestUtils.setCssAnimationsEnabled(root, true);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that detached popup surface animations settle and inherit updated motion settings.
    @Tier2Test
    @Test
    void popupSurfaceTransitionsSettleWhenMotionIsDisabled() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable VBox> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DatePickerField> dateFieldReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DateRangePickerField> rangeFieldReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> showPopupMotionTestWindow(
                    stageReference,
                    rootReference,
                    menuButtonReference,
                    subMenuItemReference,
                    dateFieldReference,
                    rangeFieldReference
            ));
            FxTestUtils.runOnFxThreadWhen(
                    () -> menuPopupSurfaceIsAnimating(menuButtonReference),
                    () -> {
                        FxTestUtils.setCssAnimationsEnabled(Objects.requireNonNull(rootReference.get(), "root"), true);
                        Objects.requireNonNull(menuButtonReference.get(), "menuButton").showMenu();
                    },
                    () -> {
                        VBox root = Objects.requireNonNull(rootReference.get(), "root");
                        M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                        assertPopupSurfaceIntermediate(menuButton.getMenu(), "menu popup");

                        FxTestUtils.setCssAnimationsEnabled(root, false);

                        assertPopupSurfaceSettled(menuButton.getMenu(), "menu popup");
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> subMenuPopupSurfaceIsAnimating(subMenuItemReference),
                    () -> {
                        FxTestUtils.setCssAnimationsEnabled(Objects.requireNonNull(rootReference.get(), "root"), true);
                        Objects.requireNonNull(subMenuItemReference.get(), "subMenuItem").showSubMenu();
                    },
                    () -> {
                        VBox root = Objects.requireNonNull(rootReference.get(), "root");
                        M3SubMenuItem subMenuItem =
                                Objects.requireNonNull(subMenuItemReference.get(), "subMenuItem");
                        assertPopupSurfaceIntermediate(subMenuItem.getSubMenu(), "submenu popup");

                        FxTestUtils.setCssAnimationsEnabled(root, false);

                        assertPopupSurfaceSettled(subMenuItem.getSubMenu(), "submenu popup");
                        subMenuItem.hideSubMenu();
                        Objects.requireNonNull(menuButtonReference.get(), "menuButton").hideMenu();
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> datePickerPopupSurfaceIsAnimating(dateFieldReference),
                    () -> {
                        FxTestUtils.setCssAnimationsEnabled(Objects.requireNonNull(rootReference.get(), "root"), true);
                        Objects.requireNonNull(dateFieldReference.get(), "dateField").showPicker();
                    },
                    () -> {
                        VBox root = Objects.requireNonNull(rootReference.get(), "root");
                        M3DatePickerField dateField = Objects.requireNonNull(dateFieldReference.get(), "dateField");
                        Parent popupSurface = Objects.requireNonNull(
                                pickerPopupSurface(dateField),
                                "datePopupSurface"
                        );
                        assertPopupSurfaceIntermediate(popupSurface, "date picker popup");

                        FxTestUtils.setCssAnimationsEnabled(root, false);

                        assertPopupSurfaceSettled(popupSurface, "date picker popup");
                        dateField.hidePicker();
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> rangePickerPopupSurfaceIsAnimating(rangeFieldReference),
                    () -> {
                        FxTestUtils.setCssAnimationsEnabled(Objects.requireNonNull(rootReference.get(), "root"), true);
                        Objects.requireNonNull(rangeFieldReference.get(), "rangeField").showPicker();
                    },
                    () -> {
                        VBox root = Objects.requireNonNull(rootReference.get(), "root");
                        M3DateRangePickerField rangeField =
                                Objects.requireNonNull(rangeFieldReference.get(), "rangeField");
                        Parent popupSurface = Objects.requireNonNull(
                                pickerPopupSurface(rangeField),
                                "rangePopupSurface"
                        );
                        assertPopupSurfaceIntermediate(popupSurface, "date range picker popup");

                        FxTestUtils.setCssAnimationsEnabled(root, false);

                        assertPopupSurfaceSettled(popupSurface, "date range picker popup");
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> closePopupMotionTestWindow(
                    stageReference,
                    rootReference,
                    menuButtonReference,
                    subMenuItemReference,
                    dateFieldReference,
                    rangeFieldReference
            ));
        }
    }

    /// Verifies that dark expressive popup roots inherit theme mode classes and render visible content.
    @Tier2Test
    @Test
    void darkExpressivePopupVisualSnapshotInheritsThemeContext() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Theme> themeReference = new AtomicReference<>();
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Button> ownerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3Tooltip> tooltipReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> menuPopupSurfaceHasSettled(menuButtonReference),
                    () -> {
                        Stage stage = new Stage();
                        M3Theme theme = M3Theme.fromSeed(
                                Color.web("#006a6a"),
                                M3Profile.EXPRESSIVE_2025,
                                Brightness.DARK
                        );
                        M3MenuItem open = new M3MenuItem("Open");
                        M3MenuItem save = new M3MenuItem("Save");
                        M3MenuButton menuButton = new M3MenuButton("More", open, save);
                        M3Button owner = new M3Button("Owner");
                        M3Tooltip tooltip = new M3Tooltip("Dark expressive tooltip");

                        VBox root = new VBox(16.0, menuButton, owner);
                        root.setStyle("-fx-background-color: -m3-color-surface; -fx-padding: 24px;");
                        FxTestUtils.setCssAnimationsEnabled(root, false);
                        Scene scene = new Scene(root, 360.0, 180.0);
                        M3ThemeManager.install(scene, theme);
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.resize(360.0, 180.0);
                        root.layout();

                        stageReference.set(stage);
                        themeReference.set(theme);
                        menuButtonReference.set(menuButton);
                        ownerReference.set(owner);
                        tooltipReference.set(tooltip);
                        menuButton.showMenu();
                    },
                    () -> {
                        Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                        M3Theme theme = Objects.requireNonNull(themeReference.get(), "theme");
                        M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menuButton");
                        M3Button owner = Objects.requireNonNull(ownerReference.get(), "owner");
                        M3Tooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "tooltip");
                        M3Menu menu = menuButton.getMenu();

                        assertPopupSurfaceSettled(menu, "dark expressive menu popup");
                        resizeToPreferredSize(menu);
                        assertTrue(menu.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                        assertTrue(menu.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                        assertSame(theme, M3ThemeManager.getTheme(menu));
                        assertTrue(menu.getStylesheets().contains(M3ThemeRuntime.themeStylesheetUrl(theme)));

                        WritableImage menuImage = snapshotImageOnFxThread(menu);
                        assertSnapshotHasColorVariety(menuImage, 4);
                        assertSnapshotNodeContainsContrast(menuImage, menu, Color.BLACK, 0.08);
                        writeVisualSnapshot(menuImage, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-dark-expressive-menu-popup.png"
                        ));
                        assertRenderedTextNodesStayInsideLayout(menu);
                        assertFixedTargetControlsKeepCenteredContent(menu);

                        tooltip.setTheme(theme);
                        tooltip.show(owner, stage.getX() + 48.0, stage.getY() + 128.0);
                        Parent tooltipRoot = tooltip.getScene().getRoot();
                        tooltipRoot.applyCss();
                        if (tooltipRoot instanceof Region region) {
                            resizeToPreferredSize(region);
                        } else {
                            tooltipRoot.layout();
                        }
                        assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                        assertTrue(tooltip.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                        assertSame(theme, tooltip.getTheme());
                        assertTrue(tooltip.getStyle().isEmpty());
                        assertTooltipThemeStylesheet(tooltip, theme);

                        WritableImage tooltipImage = snapshotImageOnFxThread(tooltipRoot);
                        assertSnapshotHasColorVariety(tooltipImage, 2);
                        assertSnapshotNodeContainsContrast(tooltipImage, tooltipRoot, Color.BLACK, 0.08);
                        writeVisualSnapshot(tooltipImage, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-dark-expressive-tooltip.png"
                        ));
                        assertRenderedTextNodesStayInsideLayout(tooltipRoot);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3Tooltip tooltip = tooltipReference.get();
                if (tooltip != null) {
                    tooltip.hide();
                }

                @Nullable M3MenuButton menuButton = menuButtonReference.get();
                if (menuButton != null) {
                    menuButton.hideMenu();
                }

                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that real Material dialog overlays inherit dark expressive host theme tokens.
    @Tier2Test
    @Test
    void darkExpressiveDialogOverlayInheritsHostThemeContext() throws InterruptedException {
        AtomicReference<@Nullable Stage> ownerStageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3OverlayPane> overlayPaneReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DialogHandle> dialogHandleReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> dialogOverlayReady(dialogHandleReference),
                    2,
                    () -> {
                        Stage ownerStage = new Stage();
                        M3Theme theme = M3Theme.fromSeed(
                                Color.web("#006a6a"),
                                M3Profile.EXPRESSIVE_2025,
                                Brightness.DARK
                        );
                        StackPane content = new StackPane(new M3Button("Open dialog"));
                        content.setStyle("-fx-background-color: -m3-color-surface; -fx-padding: 24px;");
                        M3OverlayPane overlayPane = new M3OverlayPane();
                        overlayPane.setContent(content);
                        Scene scene = new Scene(overlayPane, 360.0, 180.0);
                        M3ThemeManager.install(scene, theme);
                        FxTestUtils.setCssAnimationsEnabled(overlayPane, false);
                        ownerStage.setScene(scene);
                        ownerStage.show();
                        overlayPane.applyCss();
                        overlayPane.resize(360.0, 180.0);
                        overlayPane.layout();

                        M3Dialog dialog = new M3Dialog();
                        dialog.getDialogPane().setHeaderText("Dark expressive dialog");
                        dialog.getDialogPane().setContentText("Dark overlay body");
                        M3Button cancelAction = new M3Button("Cancel", M3ButtonVariant.TEXT);
                        cancelAction.setCancelButton(true);
                        M3Button okAction = new M3Button("OK", M3ButtonVariant.TEXT);
                        okAction.setDefaultButton(true);
                        dialog.getDialogPane().getActions().setAll(cancelAction, okAction);
                        ownerStageReference.set(ownerStage);
                        overlayPaneReference.set(overlayPane);
                        dialogHandleReference.set(overlayPane.showDialog(dialog));
                    },
                    () -> {
                        M3DialogHandle dialogHandle =
                                Objects.requireNonNull(dialogHandleReference.get(), "dialog handle");
                        M3Dialog dialog = dialogHandle.getDialog();
                        M3OverlayPane overlayPane =
                                Objects.requireNonNull(overlayPaneReference.get(), "overlayPane");
                        M3DialogPane pane = dialog.getDialogPane();
                        pane.applyCss();
                        pane.layout();

                        assertTrue(dialogHandle.isShowing());
                        assertSame(
                                overlayPane,
                                Objects.requireNonNull(overlayPane.getScene(), "overlay scene").getRoot()
                        );
                        assertTrue(M3Accessible.containsNode(overlayPane, pane));
                        Label header = assertInstanceOf(
                                Label.class,
                                pane.lookup(".header-panel .label"),
                                "dialog overlay header label"
                        );
                        Label content = assertInstanceOf(
                                Label.class,
                                pane.lookup(".content-label"),
                                "dialog overlay content label"
                        );
                        assertDarkThemeLabelFill(header, "dialog overlay header label");
                        assertDarkThemeLabelFill(content, "dialog overlay content label");

                        WritableImage image = snapshotImageOnFxThread(pane);
                        assertSnapshotHasColorVariety(image, 4);
                        assertSnapshotNodeContainsContrast(image, pane, Color.BLACK, 0.08);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-dark-expressive-dialog-overlay.png"
                        ));
                        assertMaterialTargetGeometryIsStable(
                                Objects.requireNonNull(pane.getScene(), "dialog scene").getRoot()
                        );

                        assertTrue(dialogHandle.requestClose());

                        assertFalse(dialogHandle.isShowing());
                        assertNull(pane.getParent());
                        assertSame(
                                overlayPane,
                                Objects.requireNonNull(overlayPane.getScene(), "overlay scene").getRoot()
                        );
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3DialogHandle dialogHandle = dialogHandleReference.get();
                if (dialogHandle != null) {
                    dialogHandle.requestClose();
                }

                @Nullable Stage ownerStage = ownerStageReference.get();
                if (ownerStage != null) {
                    ownerStage.close();
                }
            });
        }
    }

    /// Shows the window used to verify detached popup surface motion.
    private static void showPopupMotionTestWindow(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable VBox> rootReference,
            AtomicReference<@Nullable M3MenuButton> menuButtonReference,
            AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference,
            AtomicReference<@Nullable M3DatePickerField> dateFieldReference,
            AtomicReference<@Nullable M3DateRangePickerField> rangeFieldReference
    ) {
        Stage stage = new Stage();
        M3SubMenuItem subMenuItem = new M3SubMenuItem("Move to", new M3MenuItem("Archive"));
        M3MenuButton menuButton = new M3MenuButton("Open menu", new M3MenuItem("Duplicate"), subMenuItem);
        M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 18));
        M3DateRangePickerField rangeField = new M3DateRangePickerField(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 25)
        );
        VBox root = new VBox(16.0, menuButton, dateField, rangeField);
        Scene scene = new Scene(root, 820.0, 620.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(root, true);
        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.resize(820.0, 620.0);
        root.layout();

        stageReference.set(stage);
        rootReference.set(root);
        menuButtonReference.set(menuButton);
        subMenuItemReference.set(subMenuItem);
        dateFieldReference.set(dateField);
        rangeFieldReference.set(rangeField);
    }

    /// Closes the detached popup surface motion test window and clears local motion overrides.
    private static void closePopupMotionTestWindow(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable VBox> rootReference,
            AtomicReference<@Nullable M3MenuButton> menuButtonReference,
            AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference,
            AtomicReference<@Nullable M3DatePickerField> dateFieldReference,
            AtomicReference<@Nullable M3DateRangePickerField> rangeFieldReference
    ) {
        @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
        if (subMenuItem != null) {
            subMenuItem.hideSubMenu();
        }

        @Nullable M3MenuButton menuButton = menuButtonReference.get();
        if (menuButton != null) {
            menuButton.hideMenu();
        }

        @Nullable M3DatePickerField dateField = dateFieldReference.get();
        if (dateField != null) {
            dateField.hidePicker();
        }

        @Nullable M3DateRangePickerField rangeField = rangeFieldReference.get();
        if (rangeField != null) {
            rangeField.hidePicker();
        }

        @Nullable VBox root = rootReference.get();
        if (root != null) {
            FxTestUtils.setCssAnimationsEnabled(root, true);
            FxTestUtils.clearMotionScheme(root);
        }

        @Nullable Stage stage = stageReference.get();
        if (stage != null) {
            stage.close();
        }
    }

    /// Returns whether the menu popup surface is rendering a visible enter-transition frame.
    private static boolean menuPopupSurfaceIsAnimating(
            AtomicReference<@Nullable M3MenuButton> menuButtonReference
    ) {
        @Nullable M3MenuButton menuButton = menuButtonReference.get();
        return menuButton != null
                && menuButton.isShowing()
                && popupSurfaceIsAnimating(menuButton.getMenu());
    }

    /// Returns whether the menu popup surface has reached its final identity transform.
    private static boolean menuPopupSurfaceHasSettled(
            AtomicReference<@Nullable M3MenuButton> menuButtonReference
    ) {
        @Nullable M3MenuButton menuButton = menuButtonReference.get();
        return menuButton != null
                && menuButton.isShowing()
                && popupSurfaceIsSettled(menuButton.getMenu());
    }

    /// Returns whether a dialog overlay pane has reached a renderable styled state.
    private static boolean dialogOverlayReady(
            AtomicReference<@Nullable M3DialogHandle> dialogHandleReference
    ) {
        @Nullable M3DialogHandle dialogHandle = dialogHandleReference.get();
        if (dialogHandle == null || !dialogHandle.isShowing()) {
            return false;
        }

        M3DialogPane pane = dialogHandle.getDialog().getDialogPane();
        pane.applyCss();
        pane.layout();
        @Nullable Scene scene = pane.getScene();
        return scene != null
                && scene.getWindow() != null
                && scene.getWindow().isShowing()
                && hasRenderableBounds(pane)
                && pane.getOpacity() >= 0.999
                && pane.lookup(".header-panel .label") instanceof Label
                && pane.lookup(".content-label") instanceof Label
                && pane.getActions().stream().anyMatch(M3Button::isDefaultButton);
    }

    /// Returns whether the submenu popup surface is rendering a visible enter-transition frame.
    private static boolean subMenuPopupSurfaceIsAnimating(
            AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference
    ) {
        @Nullable M3SubMenuItem subMenuItem = subMenuItemReference.get();
        return subMenuItem != null
                && subMenuItem.isSubMenuShowing()
                && popupSurfaceIsAnimating(subMenuItem.getSubMenu());
    }

    /// Returns whether the date picker popup surface is rendering a visible enter-transition frame.
    private static boolean datePickerPopupSurfaceIsAnimating(
            AtomicReference<@Nullable M3DatePickerField> dateFieldReference
    ) {
        @Nullable M3DatePickerField dateField = dateFieldReference.get();
        @Nullable Parent popupSurface = dateField == null ? null : pickerPopupSurface(dateField);
        return popupSurface != null && popupSurfaceIsAnimating(popupSurface);
    }

    /// Returns whether the date range picker popup surface is rendering a visible enter-transition frame.
    private static boolean rangePickerPopupSurfaceIsAnimating(
            AtomicReference<@Nullable M3DateRangePickerField> rangeFieldReference
    ) {
        @Nullable M3DateRangePickerField rangeField = rangeFieldReference.get();
        @Nullable Parent popupSurface = rangeField == null ? null : pickerPopupSurface(rangeField);
        return popupSurface != null && popupSurfaceIsAnimating(popupSurface);
    }

    /// Returns whether a popup surface is in a visible enter-transition frame.
    private static boolean popupSurfaceIsAnimating(Node surface) {
        surface.applyCss();
        if (surface instanceof Parent parent) {
            parent.layout();
        }

        return surface.getScene() != null
                && surface.getOpacity() > 0.0
                && (surface.getOpacity() < 1.0
                || surface.getScaleX() < 1.0
                || surface.getScaleY() < 1.0
                || Math.abs(surface.getTranslateX()) > 0.0001
                || Math.abs(surface.getTranslateY()) > 0.0001);
    }

    /// Verifies that a popup surface is visibly mid-transition.
    private static void assertPopupSurfaceIntermediate(Node surface, String description) {
        surface.applyCss();
        if (surface instanceof Parent parent) {
            parent.layout();
        }

        assertTrue(surface.getScene() != null, () -> description + " should be attached to a scene");
        assertBetween(surface.getOpacity(), 0.0, 1.0, description + " opacity");
        assertTrue(surface.getScaleX() >= 0.96 && surface.getScaleX() <= 1.0,
                () -> description + " scaleX=" + surface.getScaleX());
        assertTrue(surface.getScaleY() >= 0.96 && surface.getScaleY() <= 1.0,
                () -> description + " scaleY=" + surface.getScaleY());
        assertTrue(
                Math.abs(surface.getTranslateX()) > 0.0001 || Math.abs(surface.getTranslateY()) > 0.0001,
                () -> description + " translateX=" + surface.getTranslateX()
                        + ", translateY=" + surface.getTranslateY()
        );
    }

    /// Resizes a region to its preferred size before taking a detached popup snapshot.
    private static void resizeToPreferredSize(Region region) {
        double width = Math.ceil(region.prefWidth(-1.0));
        double height = Math.ceil(region.prefHeight(width));
        region.resize(width, height);
        region.applyCss();
        region.layout();
    }
}
