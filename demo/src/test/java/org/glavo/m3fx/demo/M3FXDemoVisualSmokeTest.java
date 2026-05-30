// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Visual smoke tests for the demo application's real JavaFX window hierarchy.
@NotNullByDefault
final class M3FXDemoVisualSmokeTest {
    /// Representative pages rendered under the dark expressive theme combination.
    private static final @Unmodifiable List<String> DARK_EXPRESSIVE_VISUAL_PAGES = List.of(
            "Buttons",
            "Button Groups",
            "Text Fields",
            "Date Pickers",
            "Loading Indicator",
            "Progress",
            "Menus",
            "Navigation Drawer"
    );

    /// Demo pages that are sensitive to right-to-left mirroring in real window layouts.
    private static final @Unmodifiable List<String> RTL_VISUAL_PAGES = List.of(
            "Button Groups",
            "Icon Buttons",
            "Text Fields",
            "Date Pickers",
            "Menus",
            "Navigation Drawer"
    );

    /// Fixed-target controls whose visible glyph content should stay centered.
    private static final @Unmodifiable Set<String> CENTERED_TARGET_STYLE_CLASSES = Set.of(
            M3DatePicker.DAY_CELL_STYLE_CLASS,
            M3FloatingActionButton.STYLE_CLASS,
            M3IconButton.STYLE_CLASS,
            M3IconToggleButton.STYLE_CLASS,
            M3SegmentedButton.STYLE_CLASS
    );

    /// The edge tolerance used when comparing text bounds against scene and viewport bounds.
    private static final double TEXT_EDGE_TOLERANCE = 1.0;

    /// The edge tolerance used when comparing visible control bounds against scene and viewport bounds.
    private static final double CONTROL_EDGE_TOLERANCE = 2.0;

    /// The minimum safe vertical room for single-line input text inside its editable area.
    private static final double INPUT_TEXT_MINIMUM_VERTICAL_ROOM = 4.0;

    /// The lowest acceptable vertical center ratio for single-line input text.
    private static final double INPUT_TEXT_MINIMUM_CENTER_RATIO = 0.33;

    /// The highest acceptable vertical center ratio for single-line input text.
    private static final double INPUT_TEXT_MAXIMUM_CENTER_RATIO = 0.70;

    /// Starts the JavaFX toolkit before creating the demo stage.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    /// Verifies that every registered demo page renders visible content without obvious clipping or off-center glyphs.
    @Test
    void allDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertTrue(app.demoPageTitlesForTesting().size() > 20);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            @Unmodifiable List<String> pageTitles =
                    Objects.requireNonNull(appReference.get(), "app").demoPageTitlesForTesting();
            for (String pageTitle : pageTitles) {
                runOnFxThreadAfterDelay(Duration.millis(80.0), () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    app.showPageForTesting(pageTitle);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertCurrentPageTitle(scene, pageTitle);

                    WritableImage image = snapshot(scene);
                    writeVisualSnapshot(image, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "demo-" + snapshotFileName(pageTitle) + ".png"
                    ));
                    assertSnapshotHasVisibleContent(image, pageTitle);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                });
            }
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that representative demo pages stay readable under the dark expressive theme combination.
    @Test
    void darkExpressiveDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(900.0);
            app.setThemeModeForTesting(M3Profile.EXPRESSIVE_2025, Brightness.DARK);

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertTrue(app.demoPageTitlesForTesting().containsAll(DARK_EXPRESSIVE_VISUAL_PAGES));
            assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
            assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            for (String pageTitle : DARK_EXPRESSIVE_VISUAL_PAGES) {
                runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    app.showPageForTesting(pageTitle);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertCurrentPageTitle(scene, pageTitle);
                    assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                    assertTrue(scene.getRoot().getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

                    WritableImage image = snapshot(scene);
                    writeVisualSnapshot(image, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "demo-dark-expressive-" + snapshotFileName(pageTitle) + ".png"
                    ));
                    assertSnapshotHasVisibleContent(image, pageTitle);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                });
            }
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that representative demo pages keep visible content valid when the scene is mirrored for RTL locales.
    @Test
    void rightToLeftDemoPagesRenderWithoutClippedTextOrOffCenterFixedTargets() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1366.0);
            stage.setHeight(900.0);

            Scene scene = Objects.requireNonNull(app.sceneForTesting(), "scene");
            assertNotNull(scene);
            assertTrue(app.demoPageTitlesForTesting().containsAll(RTL_VISUAL_PAGES));
            scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(scene);
        });

        try {
            for (String pageTitle : RTL_VISUAL_PAGES) {
                runOnFxThreadAfterDelay(Duration.millis(80.0), () -> {
                    M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    app.showPageForTesting(pageTitle);
                    scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                }, () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertEquals(NodeOrientation.RIGHT_TO_LEFT, scene.getRoot().getEffectiveNodeOrientation());
                    assertCurrentPageTitle(scene, pageTitle);

                    WritableImage image = snapshot(scene);
                    writeVisualSnapshot(image, Path.of(
                            "build",
                            "reports",
                            "m3fx-demo-visual",
                            "demo-rtl-" + snapshotFileName(pageTitle) + ".png"
                    ));
                    assertSnapshotHasVisibleContent(image, pageTitle);
                    assertDemoPageVisualGeometry(scene, pageTitle);
                });
            }
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that real mouse and focus interactions produce visible feedback in the demo window.
    @Test
    void interactiveDemoStatesProduceVisibleFeedback() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            verifyButtonMouseFeedback(appReference, sceneReference);
            verifyTextFieldFocusFeedback(appReference, sceneReference);
            verifySidebarMouseFeedback(appReference, sceneReference);
            verifyIconToggleButtonMouseFeedback(appReference, sceneReference);
            verifyDisabledAnimationInteractionFeedback(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that animated progress indicators visibly advance between real rendered frames.
    @Test
    void progressDemoAnimationsProduceDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> pageReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> firstFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> secondFrameReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThreadAfterDelay(Duration.millis(180.0), () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Progress");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                pageReference.set(Objects.requireNonNull(firstVisibleNodeWithStyle(
                        scene.getRoot(),
                        "demo-page"
                ), "demo page"));
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                firstFrameReference.set(snapshot(scene));
                writeAnimationSnapshot(
                        Objects.requireNonNull(firstFrameReference.get(), "first progress frame"),
                        "progress",
                        "frame-a"
                );
            });

            runOnFxThreadAfterDelay(Duration.millis(520.0), () -> {
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                secondFrameReference.set(snapshot(scene));
                writeAnimationSnapshot(
                        Objects.requireNonNull(secondFrameReference.get(), "second progress frame"),
                        "progress",
                        "frame-b"
                );
            });

            assertNodeAreaChanged(
                    Objects.requireNonNull(pageReference.get(), "progress page"),
                    Objects.requireNonNull(firstFrameReference.get(), "first progress frame"),
                    Objects.requireNonNull(secondFrameReference.get(), "second progress frame"),
                    "progress animation frames"
            );
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that animated loading indicators visibly morph between real rendered frames.
    @Test
    void loadingIndicatorDemoAnimationProducesDistinctFrames() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3FXDemoApp> appReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> indicatorReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> firstFrameReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> secondFrameReference = new AtomicReference<>();

        runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(900.0);

            stageReference.set(stage);
            appReference.set(app);
            sceneReference.set(Objects.requireNonNull(app.sceneForTesting(), "scene"));
        });

        try {
            runOnFxThreadAfterDelay(Duration.millis(180.0), () -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                app.showPageForTesting("Loading Indicator");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                indicatorReference.set(Objects.requireNonNull(firstVisibleNodeWithStyle(
                        scene.getRoot(),
                        M3LoadingIndicator.STYLE_CLASS
                ), "loading indicator"));
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                firstFrameReference.set(snapshot(scene));
                writeAnimationSnapshot(
                        Objects.requireNonNull(firstFrameReference.get(), "first loading indicator frame"),
                        "loading-indicator",
                        "frame-a"
                );
            });

            runOnFxThreadAfterDelay(Duration.millis(520.0), () -> {
            }, () -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                secondFrameReference.set(snapshot(scene));
                writeAnimationSnapshot(
                        Objects.requireNonNull(secondFrameReference.get(), "second loading indicator frame"),
                        "loading-indicator",
                        "frame-b"
                );
            });

            assertNodeAreaChanged(
                    Objects.requireNonNull(indicatorReference.get(), "loading indicator"),
                    Objects.requireNonNull(firstFrameReference.get(), "first loading indicator frame"),
                    Objects.requireNonNull(secondFrameReference.get(), "second loading indicator frame"),
                    "loading indicator animation frames"
            );
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies hover and pressed feedback on a regular demo button.
    private static void verifyButtonMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Filled"
            ), "button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal button snapshot"),
                    "button",
                    "normal"
            );
            applyPseudoState(target, "hover");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            hoverReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                    "button",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            pressedReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed button snapshot"),
                    "button",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearPseudoState(target, "hover");
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(normalReference.get(), "normal button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                "button hover"
        );
        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(hoverReference.get(), "hover button snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed button snapshot"),
                "button pressed"
        );
    }

    /// Verifies focus feedback on a populated text field in the demo page.
    private static void verifyTextFieldFocusFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> focusedReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Text Fields");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    M3TextField.STYLE_CLASS
            ), "text field");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal text field snapshot"),
                    "text-field",
                    "normal"
            );
            target.requestFocus();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            focusedReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(focusedReference.get(), "focused text field snapshot"),
                    "text-field",
                    "focused"
            );
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "text field"),
                Objects.requireNonNull(normalReference.get(), "normal text field snapshot"),
                Objects.requireNonNull(focusedReference.get(), "focused text field snapshot"),
                "text field focus"
        );
    }

    /// Verifies hover and pressed feedback on a sidebar destination row.
    private static void verifySidebarMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    "demo-sidebar-child-item"
            ), "sidebar item");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal sidebar snapshot"),
                    "sidebar",
                    "normal"
            );
            applyPseudoState(target, "hover");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            hoverReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                    "sidebar",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "sidebar item");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            pressedReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed sidebar snapshot"),
                    "sidebar",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "sidebar item");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearPseudoState(target, "hover");
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "sidebar item"),
                Objects.requireNonNull(normalReference.get(), "normal sidebar snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                "sidebar hover"
        );
        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "sidebar item"),
                Objects.requireNonNull(hoverReference.get(), "hover sidebar snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed sidebar snapshot"),
                "sidebar pressed"
        );
    }

    /// Verifies hover and pressed feedback on a toggle icon button in the demo page.
    private static void verifyIconToggleButtonMouseFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Icon Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleNodeWithStyle(
                    scene.getRoot(),
                    M3IconToggleButton.STYLE_CLASS
            ), "toggle icon button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal toggle icon button snapshot"),
                    "icon-toggle-button",
                    "normal"
            );
            applyPseudoState(target, "hover");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            hoverReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                    "icon-toggle-button",
                    "hover"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        });

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            pressedReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed toggle icon button snapshot"),
                    "icon-toggle-button",
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
            clearPseudoState(target, "hover");
        });

        Node target = Objects.requireNonNull(targetReference.get(), "toggle icon button");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal toggle icon button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                "toggle icon button hover"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(hoverReference.get(), "hover toggle icon button snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed toggle icon button snapshot"),
                "toggle icon button pressed"
        );
    }

    /// Verifies that disabled animations still apply interaction states immediately.
    private static void verifyDisabledAnimationInteractionFeedback(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hoverReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(80.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3MotionSettings.setAnimationsEnabled(scene.getRoot(), false);
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Filled"
            ), "button");
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            applyPseudoState(target, "hover");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            hoverReference.set(snapshot(scene));
            M3MotionSettings.clearAnimationsEnabled(scene.getRoot());
            Node target = Objects.requireNonNull(targetReference.get(), "button");
            clearPseudoState(target, "hover");
        });

        assertNodeAreaChanged(
                Objects.requireNonNull(targetReference.get(), "button"),
                Objects.requireNonNull(normalReference.get(), "normal no-motion button snapshot"),
                Objects.requireNonNull(hoverReference.get(), "hover no-motion button snapshot"),
                "button hover with animations disabled"
        );
    }

    /// Verifies that the requested page title is visible in the content area.
    private static void assertCurrentPageTitle(Scene scene, String pageTitle) {
        @Nullable Label title = null;
        for (Node node : scene.getRoot().lookupAll(".demo-page-title")) {
            if (node instanceof Label label && label.isVisible()) {
                title = label;
                break;
            }
        }
        assertNotNull(title, () -> "No visible page title for " + pageTitle);
        assertEquals(pageTitle, title.getText());
    }

    /// Captures the current scene root as a writable image.
    private static WritableImage snapshot(Scene scene) {
        int width = Math.max(1, (int) Math.ceil(scene.getWidth()));
        int height = Math.max(1, (int) Math.ceil(scene.getHeight()));
        WritableImage image = new WritableImage(width, height);
        scene.getRoot().snapshot(null, image);
        return image;
    }

    /// Verifies that a snapshot contains enough non-background pixels to be useful as a visual artifact.
    private static void assertSnapshotHasVisibleContent(WritableImage image, String pageTitle) {
        int contrastingPixels = 0;
        Color background = image.getPixelReader().getColor(8, 8);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, background) > 0.08) {
                    contrastingPixels++;
                }
            }
        }
        int visiblePixels = contrastingPixels;
        assertTrue(visiblePixels > image.getWidth() * image.getHeight() * 0.02,
                () -> pageTitle + " snapshot has too little visible content: " + visiblePixels);
    }

    /// Verifies the shared page-level geometry checks for a rendered demo page.
    private static void assertDemoPageVisualGeometry(Scene scene, String pageTitle) {
        assertVisibleTextInsideScene(scene, pageTitle);
        assertVisibleMaterialControlsInsideScene(scene, pageTitle);
        assertFixedTargetGlyphsCentered(scene.getRoot(), pageTitle);
        assertSingleLineTextInputsHaveVerticalRoom(scene, pageTitle);
        assertSelectionIndicatorsCentered(scene, pageTitle);
    }

    /// Verifies that visible text nodes intersecting the scene are not clipped by the scene viewport.
    private static void assertVisibleTextInsideScene(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof Text text) || text.getText().isBlank() || !hasRenderableBounds(text)) {
                return;
            }

            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            if (!sceneBounds.intersects(textBounds) || !sceneBounds.contains(textBounds.getCenterX(), textBounds.getCenterY())) {
                return;
            }
            @Nullable Node scrollViewport = nearestScrollViewport(text);
            if (scrollViewport != null) {
                Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
                if (!viewportBounds.intersects(textBounds)
                        || !viewportBounds.contains(textBounds.getCenterX(), textBounds.getCenterY())) {
                    return;
                }
                assertTrue(containsHorizontalBoundsWithTolerance(viewportBounds, textBounds, TEXT_EDGE_TOLERANCE),
                        () -> pageTitle + " visible text leaves its scroll viewport horizontally: text="
                                + text.getText() + ", bounds=" + textBounds + ", viewport=" + viewportBounds);
                if (touchesVerticalViewportEdge(textBounds, viewportBounds, TEXT_EDGE_TOLERANCE)) {
                    return;
                }
                assertTrue(containsBoundsWithTolerance(viewportBounds, textBounds, TEXT_EDGE_TOLERANCE),
                        () -> pageTitle + " visible text leaves its scroll viewport: text="
                                + text.getText() + ", bounds=" + textBounds + ", viewport=" + viewportBounds);
                return;
            }

            assertTrue(containsBoundsWithTolerance(sceneBounds, textBounds, TEXT_EDGE_TOLERANCE),
                    () -> pageTitle + " visible text leaves the scene viewport: text="
                            + text.getText() + ", bounds=" + textBounds + ", scene=" + sceneBounds);
        });
    }

    /// Verifies that visible Material controls stay inside the visible scene and scroll viewport.
    private static void assertVisibleMaterialControlsInsideScene(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!isPageLevelMaterialControl(node) || !hasRenderableBounds(node)) {
                return;
            }

            Bounds controlBounds = node.localToScene(node.getBoundsInLocal());
            if (!isVisibleWithinSceneViewport(node, controlBounds, sceneBounds)) {
                return;
            }

            @Nullable Node scrollViewport = nearestScrollViewport(node);
            if (scrollViewport != null) {
                Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
                assertTrue(containsHorizontalBoundsWithTolerance(viewportBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                        () -> pageTitle + " visible control leaves its scroll viewport horizontally: node="
                                + node + ", bounds=" + controlBounds + ", viewport=" + viewportBounds);
                if (touchesVerticalViewportEdge(controlBounds, viewportBounds, CONTROL_EDGE_TOLERANCE)) {
                    return;
                }
                assertTrue(containsBoundsWithTolerance(viewportBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                        () -> pageTitle + " visible control leaves its scroll viewport: node="
                                + node + ", bounds=" + controlBounds + ", viewport=" + viewportBounds);
                return;
            }

            assertTrue(containsBoundsWithTolerance(sceneBounds, controlBounds, CONTROL_EDGE_TOLERANCE),
                    () -> pageTitle + " visible control leaves the scene viewport: node="
                            + node + ", bounds=" + controlBounds + ", scene=" + sceneBounds);
        });
    }

    /// Returns the nearest scroll pane viewport that clips a node, or `null` when the node is not inside one.
    private static @Nullable Node nearestScrollViewport(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane scrollPane) {
                return scrollPane.lookup(".viewport");
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Verifies that fixed-size Material targets keep their glyph text centered.
    private static void assertFixedTargetGlyphsCentered(Node root, String pageTitle) {
        visitVisibleNodes(root, node -> {
            if (!isCenteredTarget(node) || !hasRenderableBounds(node)) {
                return;
            }

            @Nullable Text text = firstVisibleText(node);
            if (text == null || !hasRenderableBounds(text)) {
                return;
            }

            Bounds targetBounds = node.localToScene(node.getBoundsInLocal());
            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            double dx = Math.abs(targetBounds.getCenterX() - textBounds.getCenterX());
            double dy = Math.abs(targetBounds.getCenterY() - textBounds.getCenterY());
            assertTrue(dx <= 3.0 && dy <= 3.5,
                    () -> pageTitle + " fixed target glyph is off-center: target="
                            + node + ", text=" + text.getText() + ", dx=" + dx + ", dy=" + dy
                            + ", targetBounds=" + targetBounds + ", textBounds=" + textBounds);
        });
    }

    /// Verifies that single-line text input glyphs have visible vertical room inside their field containers.
    private static void assertSingleLineTextInputsHaveVerticalRoom(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3TextInputLayout layout) || !hasRenderableBounds(layout)) {
                return;
            }

            TextInputControl input = layout.getInput();
            if (input == null || input instanceof M3TextArea || !input.isVisible() || !hasRenderableBounds(input)) {
                return;
            }

            @Nullable Text text = firstVisibleText(input);
            if (text == null || !hasRenderableBounds(text)) {
                return;
            }

            Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            if (!isVisibleWithinSceneViewport(text, textBounds, sceneBounds)) {
                return;
            }

            double topRoom = textBounds.getMinY() - inputBounds.getMinY();
            double bottomRoom = inputBounds.getMaxY() - textBounds.getMaxY();
            double centerRatio = (textBounds.getCenterY() - inputBounds.getMinY()) / inputBounds.getHeight();
            assertTrue(topRoom >= INPUT_TEXT_MINIMUM_VERTICAL_ROOM
                            && bottomRoom >= INPUT_TEXT_MINIMUM_VERTICAL_ROOM
                            && centerRatio >= INPUT_TEXT_MINIMUM_CENTER_RATIO
                            && centerRatio <= INPUT_TEXT_MAXIMUM_CENTER_RATIO,
                    () -> pageTitle + " text input glyph has unsafe vertical geometry: text="
                            + text.getText() + ", topRoom=" + topRoom + ", bottomRoom=" + bottomRoom
                            + ", centerRatio=" + centerRatio + ", inputBounds=" + inputBounds
                            + ", textBounds=" + textBounds);
        });
    }

    /// Verifies that selection-control indicators keep their active pieces centered in real rendered geometry.
    private static void assertSelectionIndicatorsCentered(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (node instanceof M3RadioButton radioButton && hasRenderableBounds(radioButton)) {
                assertNestedIndicatorCentered(
                        radioButton,
                        ".m3-radio-ring",
                        ".m3-radio-dot",
                        0.75,
                        sceneBounds,
                        pageTitle,
                        "radio dot"
                );
            } else if (node instanceof M3Switch switchControl && hasRenderableBounds(switchControl)) {
                assertSwitchThumbInsideTrack(
                        switchControl,
                        sceneBounds,
                        pageTitle
                );
            }
        });
    }

    /// Verifies that a nested visual indicator shares the same rendered center as its container.
    private static void assertNestedIndicatorCentered(
            Node root,
            String containerStyleClass,
            String indicatorStyleClass,
            double tolerance,
            Bounds sceneBounds,
            String pageTitle,
            String description
    ) {
        @Nullable Node container = root.lookup(containerStyleClass);
        @Nullable Node indicator = root.lookup(indicatorStyleClass);
        if (container == null || indicator == null || !hasRenderableBounds(container) || !hasRenderableBounds(indicator)) {
            return;
        }
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        if (!isVisibleWithinSceneViewport(container, containerBounds, sceneBounds)) {
            return;
        }

        double dx = Math.abs(containerBounds.getCenterX() - indicatorBounds.getCenterX());
        double dy = Math.abs(containerBounds.getCenterY() - indicatorBounds.getCenterY());
        assertTrue(dx <= tolerance && dy <= tolerance,
                () -> pageTitle + " " + description + " is off-center: dx=" + dx + ", dy=" + dy
                        + ", containerBounds=" + containerBounds + ", indicatorBounds=" + indicatorBounds);
    }

    /// Verifies that a switch thumb stays vertically centered and inside its track.
    private static void assertSwitchThumbInsideTrack(Node root, Bounds sceneBounds, String pageTitle) {
        @Nullable Node track = root.lookup(".m3-switch-track");
        @Nullable Node thumb = root.lookup(".m3-switch-thumb");
        if (track == null || thumb == null || !hasRenderableBounds(track) || !hasRenderableBounds(thumb)) {
            return;
        }
        Bounds trackBounds = track.localToScene(track.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        if (!isVisibleWithinSceneViewport(track, trackBounds, sceneBounds)) {
            return;
        }

        double dy = Math.abs(trackBounds.getCenterY() - thumbBounds.getCenterY());
        assertTrue(dy <= 1.0
                        && thumbBounds.getMinX() >= trackBounds.getMinX() - 0.75
                        && thumbBounds.getMaxX() <= trackBounds.getMaxX() + 0.75,
                () -> pageTitle + " switch thumb has unsafe geometry: dy=" + dy
                        + ", trackBounds=" + trackBounds + ", thumbBounds=" + thumbBounds);
    }

    /// Returns whether a node should have centered glyph content.
    private static boolean isCenteredTarget(Node node) {
        for (String styleClass : CENTERED_TARGET_STYLE_CLASSES) {
            if (node.getStyleClass().contains(styleClass)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a node is a demo-level Material control whose visible bounds should fit the viewport.
    private static boolean isPageLevelMaterialControl(Node node) {
        return node instanceof M3Button
                || node instanceof M3DatePicker
                || node instanceof M3FloatingActionButton
                || node instanceof M3IconButton
                || node instanceof M3IconToggleButton
                || node instanceof M3LoadingIndicator
                || node instanceof M3RadioButton
                || node instanceof M3SegmentedButton
                || node instanceof M3Switch
                || node instanceof M3TextField
                || node instanceof M3TextInputLayout;
    }

    /// Returns the first visible descendant with the requested style class.
    private static @Nullable Node firstVisibleNodeWithStyle(Node root, String styleClass) {
        if (root.isVisible() && root.getStyleClass().contains(styleClass) && hasRenderableBounds(root)) {
            return root;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node result = firstVisibleNodeWithStyle(child, styleClass);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns whether a node bounds intersects the scene and any active scroll viewport.
    private static boolean isVisibleWithinSceneViewport(Node node, Bounds nodeBounds, Bounds sceneBounds) {
        if (!sceneBounds.intersects(nodeBounds)
                || !sceneBounds.contains(nodeBounds.getCenterX(), nodeBounds.getCenterY())) {
            return false;
        }
        @Nullable Node scrollViewport = nearestScrollViewport(node);
        if (scrollViewport == null) {
            return true;
        }
        Bounds viewportBounds = scrollViewport.localToScene(scrollViewport.getBoundsInLocal());
        return viewportBounds.intersects(nodeBounds)
                && viewportBounds.contains(nodeBounds.getCenterX(), nodeBounds.getCenterY());
    }

    /// Returns the first visible M3 button with the requested text.
    private static @Nullable M3Button firstVisibleButtonWithText(Node root, String text) {
        if (root instanceof M3Button button
                && button.isVisible()
                && text.equals(button.getText())
                && hasRenderableBounds(button)) {
            return button;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3Button result = firstVisibleButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Verifies that an interaction visibly changes the snapshot region occupied by a node.
    private static void assertNodeAreaChanged(Node node, WritableImage before, WritableImage after, String description) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int minX = clampPixel(Math.floor(bounds.getMinX()), before.getWidth());
        int minY = clampPixel(Math.floor(bounds.getMinY()), before.getHeight());
        int maxX = clampPixel(Math.ceil(bounds.getMaxX()), before.getWidth());
        int maxY = clampPixel(Math.ceil(bounds.getMaxY()), before.getHeight());
        int changedPixels = 0;
        int totalPixels = Math.max(1, (maxX - minX) * (maxY - minY));
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (colorDistance(before.getPixelReader().getColor(x, y), after.getPixelReader().getColor(x, y)) > 0.02) {
                    changedPixels++;
                }
            }
        }
        int minimumChangedPixels = Math.max(8, totalPixels / 250);
        int finalChangedPixels = changedPixels;
        assertTrue(finalChangedPixels >= minimumChangedPixels,
                () -> description + " produced too little visual change: changed="
                        + finalChangedPixels + ", minimum=" + minimumChangedPixels + ", bounds=" + bounds);
    }

    /// Writes a named interaction-state visual snapshot to the build report directory.
    private static void writeInteractionSnapshot(WritableImage image, String targetName, String stateName) {
        writeVisualSnapshot(image, Path.of(
                "build",
                "reports",
                "m3fx-demo-visual",
                "interaction-" + targetName + "-" + stateName + ".png"
        ));
    }

    /// Writes a named animation-frame visual snapshot to the build report directory.
    private static void writeAnimationSnapshot(WritableImage image, String targetName, String stateName) {
        writeVisualSnapshot(image, Path.of(
                "build",
                "reports",
                "m3fx-demo-visual",
                "animation-" + targetName + "-" + stateName + ".png"
        ));
    }

    /// Clamps a floating point coordinate to a valid snapshot pixel coordinate.
    private static int clampPixel(double coordinate, double size) {
        return Math.max(0, Math.min((int) Math.ceil(size), (int) coordinate));
    }

    /// Applies a JavaFX pseudo-class to a node before rendering an interaction state.
    private static void applyPseudoState(Node node, String pseudoClass) {
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass(pseudoClass), true);
    }

    /// Clears a JavaFX pseudo-class from a node after rendering an interaction state.
    private static void clearPseudoState(Node node, String pseudoClass) {
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass(pseudoClass), false);
    }

    /// Fires a primary-button mouse event at the center of a node.
    private static void firePrimaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            boolean primaryButtonDown
    ) {
        Bounds bounds = node.getBoundsInLocal();
        double x = bounds.getMinX() + bounds.getWidth() / 2.0;
        double y = bounds.getMinY() + bounds.getHeight() / 2.0;
        node.fireEvent(new MouseEvent(
                eventType,
                x,
                y,
                x,
                y,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(node, x, y)
        ));
    }

    /// Returns the first visible text node below a target node.
    private static @Nullable Text firstVisibleText(Node node) {
        if (node instanceof Text text && !text.getText().isBlank() && hasRenderableBounds(text)) {
            return text;
        }
        if (node instanceof M3Icon icon) {
            @Nullable Node iconText = icon.lookup(".text");
            if (iconText instanceof Text text && !text.getText().isBlank() && hasRenderableBounds(text)) {
                return text;
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Text text = firstVisibleText(child);
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    /// Returns whether a node has visible non-empty bounds.
    private static boolean hasRenderableBounds(Node node) {
        Bounds bounds = node.getBoundsInLocal();
        return bounds.getWidth() > 0.5 && bounds.getHeight() > 0.5;
    }

    /// Returns whether `outer` fully contains `inner` after applying a small edge tolerance.
    private static boolean containsBoundsWithTolerance(Bounds outer, Bounds inner, double tolerance) {
        return inner.getMinX() >= outer.getMinX() - tolerance
                && inner.getMinY() >= outer.getMinY() - tolerance
                && inner.getMaxX() <= outer.getMaxX() + tolerance
                && inner.getMaxY() <= outer.getMaxY() + tolerance;
    }

    /// Returns whether `outer` contains the horizontal span of `inner` after applying a small edge tolerance.
    private static boolean containsHorizontalBoundsWithTolerance(Bounds outer, Bounds inner, double tolerance) {
        return inner.getMinX() >= outer.getMinX() - tolerance
                && inner.getMaxX() <= outer.getMaxX() + tolerance;
    }

    /// Returns whether a node bounds touches a scroll viewport edge where partial vertical visibility is expected.
    private static boolean touchesVerticalViewportEdge(Bounds inner, Bounds viewport, double tolerance) {
        return inner.getMinY() < viewport.getMinY() + tolerance
                || inner.getMaxY() > viewport.getMaxY() - tolerance;
    }

    /// Visits visible descendants in a rendered hierarchy.
    private static void visitVisibleNodes(Node node, Consumer<Node> visitor) {
        if (!node.isVisible()) {
            return;
        }

        visitor.accept(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                visitVisibleNodes(child, visitor);
            }
        }
    }

    /// Writes a visual snapshot to the build report directory.
    private static void writeVisualSnapshot(WritableImage image, Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ImageIO.write(toBufferedImage(image), "png", path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Converts a JavaFX image into a desktop image.
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        return bufferedImage;
    }

    /// Returns a filesystem-safe snapshot file name.
    private static String snapshotFileName(String pageTitle) {
        return pageTitle.toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    /// Returns a simple RGB distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }

    /// Runs a task on the JavaFX application thread and propagates failures.
    private static void runOnFxThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }

        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                failure.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }

        @Nullable Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
    }

    /// Runs setup on the FX thread and verifies the result after a JavaFX delay.
    private static void runOnFxThreadAfterDelay(
            Duration delay,
            Runnable setup,
            Runnable verification
    ) throws InterruptedException {
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setup.run();
                PauseTransition pause = new PauseTransition(delay);
                pause.setOnFinished(event -> {
                    try {
                        verification.run();
                    } catch (Throwable e) {
                        failure.set(e);
                    } finally {
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Throwable e) {
                failure.set(e);
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        @Nullable Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
    }
}
