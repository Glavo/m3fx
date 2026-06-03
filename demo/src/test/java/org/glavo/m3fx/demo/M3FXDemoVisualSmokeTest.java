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
import javafx.scene.control.ButtonType;
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
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3PickerField;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3SnackbarHost;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3SubMenuItem;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    /// Material documentation URLs expected for demo pages that map to official Material pages.
    private static final @Unmodifiable Map<String, String> EXPECTED_MATERIAL_URLS = Map.ofEntries(
            Map.entry("Components Overview", "https://m3.material.io/components"),
            Map.entry("App Bars", "https://m3.material.io/components/top-app-bar/overview"),
            Map.entry("Badges", "https://m3.material.io/components/badges/overview"),
            Map.entry("Button Groups", "https://m3.material.io/components/button-groups/overview"),
            Map.entry("Buttons", "https://m3.material.io/components/buttons/overview"),
            Map.entry("Extended FABs", "https://m3.material.io/components/extended-fab/overview"),
            Map.entry("FAB Menu", "https://m3.material.io/components/fab-menu/overview"),
            Map.entry("Floating Action Buttons", "https://m3.material.io/components/floating-action-button/overview"),
            Map.entry("Icon Buttons", "https://m3.material.io/components/icon-buttons/overview"),
            Map.entry("Segmented Buttons", "https://m3.material.io/components/segmented-buttons/overview"),
            Map.entry("Split Buttons", "https://m3.material.io/components/split-button/overview"),
            Map.entry("Cards", "https://m3.material.io/components/cards/overview"),
            Map.entry("Carousel", "https://m3.material.io/components/carousel/overview"),
            Map.entry("Checkboxes", "https://m3.material.io/components/checkbox/overview"),
            Map.entry("Chips", "https://m3.material.io/components/chips/overview"),
            Map.entry("Date Pickers", "https://m3.material.io/components/date-pickers/overview"),
            Map.entry("Time Pickers", "https://m3.material.io/components/time-pickers/overview"),
            Map.entry("Dialogs", "https://m3.material.io/components/dialogs/overview"),
            Map.entry("Dividers", "https://m3.material.io/components/divider/overview"),
            Map.entry("Lists", "https://m3.material.io/components/lists/overview"),
            Map.entry("Loading Indicator", "https://m3.material.io/components/loading-indicator/overview"),
            Map.entry("Progress", "https://m3.material.io/components/progress-indicators/overview"),
            Map.entry("Menus", "https://m3.material.io/components/menus/overview"),
            Map.entry("Navigation", "https://m3.material.io/components/navigation-bar/overview"),
            Map.entry("Navigation Drawer", "https://m3.material.io/components/navigation-drawer/overview"),
            Map.entry("Navigation Rail", "https://m3.material.io/components/navigation-rail/overview"),
            Map.entry("Radio Buttons", "https://m3.material.io/components/radio-button/overview"),
            Map.entry("Search", "https://m3.material.io/components/search/overview"),
            Map.entry("Bottom Sheets", "https://m3.material.io/components/bottom-sheets/overview"),
            Map.entry("Side Sheets", "https://m3.material.io/components/side-sheets/overview"),
            Map.entry("Sliders", "https://m3.material.io/components/sliders/overview"),
            Map.entry("Snackbars", "https://m3.material.io/components/snackbar/overview"),
            Map.entry("Switches", "https://m3.material.io/components/switch/overview"),
            Map.entry("Tabs", "https://m3.material.io/components/tabs/overview"),
            Map.entry("Text Fields", "https://m3.material.io/components/text-fields/overview"),
            Map.entry("Toolbars", "https://m3.material.io/components/toolbars/overview"),
            Map.entry("Tooltips", "https://m3.material.io/components/tooltips/overview"),
            Map.entry("Typography", "https://m3.material.io/styles/typography/overview"),
            Map.entry("Icons", "https://m3.material.io/styles/icons/overview")
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

    /// Verifies that demo pages expose stable Material documentation links in their page header.
    @Test
    void demoPagesExposeMaterialDocumentationLinks() {
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
            runOnFxThread(() -> {
                M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");

                assertEquals(EXPECTED_MATERIAL_URLS, app.demoPageMaterialUrlsForTesting());

                app.showPageForTesting("Buttons");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                Node docsLink = Objects.requireNonNull(scene.lookup(".demo-page-doc-link"), "docsLink");
                M3Button docsButton = assertInstanceOf(M3Button.class, docsLink);
                assertEquals("Material docs", docsButton.getText());

                app.showPageForTesting("Forms");
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                assertNull(scene.lookup(".demo-page-doc-link"));
            });
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
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
            verifyButtonRippleReleaseAnimation(appReference, sceneReference);
            verifyTextFieldFocusFeedback(appReference, sceneReference);
            verifySidebarMouseFeedback(appReference, sceneReference);
            verifySidebarRippleReleaseAnimation(appReference, sceneReference);
            verifyIconToggleButtonMouseFeedback(appReference, sceneReference);
            verifyIconToggleButtonRippleReleaseAnimation(appReference, sceneReference);
            verifySwitchSelectionAnimation(appReference, sceneReference);
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

    /// Verifies that popup-backed demo controls expose visible enter and exit motion frames.
    @Test
    void popupDemoAnimationsProduceDistinctFrames() throws InterruptedException {
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
            verifySplitButtonPopupAnimation(appReference, sceneReference);
            verifyDatePickerFieldPopupAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that nested popup menu stacks expose visible motion and sane screen placement.
    @Test
    void nestedMenuPopupStackProducesDistinctFrames() throws InterruptedException {
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
            verifyNestedMenuPopupStackAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that navigation selection and drawer disclosure animations expose visible intermediate frames.
    @Test
    void navigationDemoAnimationsProduceDistinctFrames() throws InterruptedException {
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
            verifyNavigationItemSelectionAnimation(
                    appReference,
                    sceneReference,
                    "Navigation",
                    "Search",
                    "navigation-bar-selection"
            );
            verifyNavigationItemSelectionAnimation(
                    appReference,
                    sceneReference,
                    "Navigation Rail",
                    "Search",
                    "navigation-rail-selection"
            );
            verifySidebarDrawerGroupExpansionAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that sheet visibility changes expose animated intermediate frames in the demo.
    @Test
    void sheetDemoAnimationsProduceDistinctFrames() throws InterruptedException {
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
            verifyBottomSheetVisibilityAnimation(appReference, sceneReference);
            verifySideSheetVisibilityAnimation(appReference, sceneReference);
        } finally {
            runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that overlay surfaces render compactly and expose their expected interactive motion.
    @Test
    void overlayDemoSurfacesRenderAndAnimate() throws InterruptedException {
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
            verifySnackbarHostAnimation(appReference, sceneReference);
            verifyFabMenuExpansionAnimation(appReference, sceneReference);
            verifyRichTooltipInteractiveLifetime(appReference, sceneReference);
            verifyDialogPopupSurface(sceneReference);
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

    /// Verifies that button ripple release remains visible for an intermediate fade-out frame.
    private static void verifyButtonRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Buttons",
                "button-ripple",
                "button",
                root -> firstVisibleButtonWithText(root, "Filled")
        );
    }

    /// Verifies that toggle icon button ripple release remains visible for an intermediate fade-out frame.
    private static void verifyIconToggleButtonRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Icon Buttons",
                "icon-toggle-button-ripple",
                "toggle icon button",
                root -> firstVisibleNodeWithStyle(root, M3IconToggleButton.STYLE_CLASS)
        );
    }

    /// Verifies that sidebar navigation item ripple release remains visible for an intermediate fade-out frame.
    private static void verifySidebarRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        verifyRippleReleaseAnimation(
                appReference,
                sceneReference,
                "Buttons",
                "sidebar-ripple",
                "sidebar item",
                root -> firstVisibleNodeWithStyle(root, "demo-sidebar-child-item")
        );
    }

    /// Verifies that a target's ripple release includes a visible intermediate fade-out frame.
    private static void verifyRippleReleaseAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            String snapshotName,
            String targetName,
            Function<Node, @Nullable Node> targetLookup
    ) throws InterruptedException {
        AtomicReference<@Nullable Node> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> pressedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> releaseReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting(pageTitle);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Node target = Objects.requireNonNull(targetLookup.apply(scene.getRoot()), targetName);
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal ripple target snapshot"),
                    snapshotName,
                    "normal"
            );
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_PRESSED, true);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            pressedReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(pressedReference.get(), "pressed ripple target snapshot"),
                    snapshotName,
                    "pressed"
            );
            Node target = Objects.requireNonNull(targetReference.get(), targetName);
            firePrimaryMouseEvent(target, MouseEvent.MOUSE_RELEASED, false);
        });

        runOnFxThreadAfterDelay(Duration.millis(70.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            releaseReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(releaseReference.get(), "released ripple target snapshot"),
                    snapshotName,
                    "released"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(260.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            settledReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled ripple target snapshot"),
                    snapshotName,
                    "settled"
            );
        });

        Node target = Objects.requireNonNull(targetReference.get(), targetName);
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal ripple target snapshot"),
                Objects.requireNonNull(pressedReference.get(), "pressed ripple target snapshot"),
                targetName + " ripple pressed frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal ripple target snapshot"),
                Objects.requireNonNull(releaseReference.get(), "released ripple target snapshot"),
                targetName + " ripple release intermediate frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(releaseReference.get(), "released ripple target snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled ripple target snapshot"),
                targetName + " ripple release fade-out"
        );
    }

    /// Verifies that switch selection produces visible thumb animation intermediate frames.
    private static void verifySwitchSelectionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Switch> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> intermediateReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(240.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Switches");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3Switch target = Objects.requireNonNull(firstVisibleSwitchWithText(
                    scene.getRoot(),
                    "Off"
            ), "switch");
            M3MotionSettings.setMotionScheme(target, visualSwitchMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal switch snapshot"),
                    "switch-selection",
                    "normal"
            );
            target.fire();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertTrue(target.isSelected());
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            intermediateReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                    "switch-selection",
                    "intermediate"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(520.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            settledReference.set(snapshot(scene));
            writeInteractionSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled switch snapshot"),
                    "switch-selection",
                    "settled"
            );
            M3MotionSettings.clearMotionScheme(Objects.requireNonNull(targetReference.get(), "switch"));
        });

        M3Switch target = Objects.requireNonNull(targetReference.get(), "switch");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal switch snapshot"),
                Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                "switch selection intermediate frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(intermediateReference.get(), "intermediate switch snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled switch snapshot"),
                "switch selection settling frame"
        );
    }

    /// Returns a switch-specific motion scheme that makes real visual intermediate frames observable.
    private static M3MotionScheme visualSwitchMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                M3MotionSpec.create(Duration.millis(600.0), M3MotionEasing.LINEAR),
                standard.defaultSpatial(),
                standard.slowSpatial()
        );
    }

    /// Verifies popup enter and exit motion on the demo split button menu.
    private static void verifySplitButtonPopupAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SplitButton> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> popupRootReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Split Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SplitButton target = Objects.requireNonNull(firstVisibleSplitButtonWithText(
                    scene.getRoot(),
                    "Create"
            ), "split button");
            M3MotionSettings.setMotionScheme(target, visualPopupMotionScheme());
            target.showMenu();
            assertTrue(target.isShowing());

            Node popupRoot = target.getMenu();
            layoutPopupRoot(popupRoot);
            targetReference.set(target);
            popupRootReference.set(popupRoot);
        }, () -> {
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "split button popup");
            layoutPopupRoot(popupRoot);
            openingReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening split button popup snapshot"),
                    "split-button-popup",
                    "opening"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            M3SplitButton target = Objects.requireNonNull(targetReference.get(), "split button");
            assertTrue(target.isShowing());
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "split button popup");
            layoutPopupRoot(popupRoot);
            settledReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                    "split-button-popup",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                    "split button popup"
            );
            target.hideMenu();
        });

        runOnFxThreadAfterDelay(Duration.millis(360.0), () -> {
        }, () -> {
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "split button popup");
            layoutPopupRoot(popupRoot);
            hidingReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding split button popup snapshot"),
                    "split-button-popup",
                    "hiding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(360.0), () -> {
        }, () -> {
            M3SplitButton target = Objects.requireNonNull(targetReference.get(), "split button");
            assertFalse(target.isShowing());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening split button popup snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                "split button popup enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled split button popup snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding split button popup snapshot"),
                "split button popup exit motion"
        );
    }

    /// Verifies nested menu popup enter and exit motion with side-by-side popup placement.
    private static void verifyNestedMenuPopupStackAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3MenuButton> menuButtonReference = new AtomicReference<>();
        AtomicReference<@Nullable M3SubMenuItem> subMenuItemReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> ownerMenuReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Menus");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3MenuButton menuButton = Objects.requireNonNull(firstVisibleMenuButtonWithText(
                    scene.getRoot(),
                    "Open menu"
            ), "menu button");
            M3MotionSettings.setMotionScheme(menuButton, visualPopupMotionScheme());
            menuButton.showMenu();
            assertTrue(menuButton.isShowing());
            menuButtonReference.set(menuButton);
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            layoutPopupRoot(menuButton.getMenu());
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            assertTrue(menuButton.isShowing());
            layoutPopupRoot(menuButton.getMenu());
            ownerMenuReference.set(snapshotNode(menuButton.getMenu()));
            writeAnimationSnapshot(
                    Objects.requireNonNull(ownerMenuReference.get(), "owner menu snapshot"),
                    "nested-menu-owner",
                    "settled"
            );

            M3SubMenuItem subMenuItem = Objects.requireNonNull(firstVisibleSubMenuItemWithText(
                    menuButton.getMenu(),
                    "Move to"
            ), "submenu item");
            M3MotionSettings.setMotionScheme(subMenuItem, visualPopupMotionScheme());
            subMenuItem.showSubMenu();
            assertTrue(subMenuItem.isSubMenuShowing());
            subMenuItemReference.set(subMenuItem);
        });

        runOnFxThreadAfterDelay(Duration.millis(180.0), () -> {
        }, () -> {
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            layoutPopupRoot(subMenuItem.getSubMenu());
            openingReference.set(snapshotNode(subMenuItem.getSubMenu()));
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening submenu snapshot"),
                    "nested-submenu",
                    "opening"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            assertTrue(menuButton.isShowing());
            assertTrue(subMenuItem.isSubMenuShowing());
            layoutPopupRoot(menuButton.getMenu());
            layoutPopupRoot(subMenuItem.getSubMenu());
            settledReference.set(snapshotNode(subMenuItem.getSubMenu()));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                    "nested-submenu",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(ownerMenuReference.get(), "owner menu snapshot"),
                    "nested owner menu"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                    "nested submenu"
            );
            assertPopupStackSideBySide(menuButton.getMenu(), subMenuItem.getSubMenu());
            subMenuItem.hideSubMenu();
        });

        runOnFxThreadAfterDelay(Duration.millis(360.0), () -> {
        }, () -> {
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            layoutPopupRoot(subMenuItem.getSubMenu());
            hidingReference.set(snapshotNode(subMenuItem.getSubMenu()));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding submenu snapshot"),
                    "nested-submenu",
                    "hiding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(360.0), () -> {
        }, () -> {
            M3MenuButton menuButton = Objects.requireNonNull(menuButtonReference.get(), "menu button");
            M3SubMenuItem subMenuItem = Objects.requireNonNull(subMenuItemReference.get(), "submenu item");
            assertFalse(subMenuItem.isSubMenuShowing());
            menuButton.hideMenu();
            M3MotionSettings.clearMotionScheme(subMenuItem);
            M3MotionSettings.clearMotionScheme(menuButton);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening submenu snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                "nested submenu enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled submenu snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding submenu snapshot"),
                "nested submenu exit motion"
        );
    }

    /// Verifies popup enter and exit motion on the demo date picker field.
    private static void verifyDatePickerFieldPopupAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3DatePickerField> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> popupRootReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Date Pickers");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3DatePickerField target = Objects.requireNonNull(firstVisibleDatePickerField(
                    scene.getRoot()
            ), "date picker field");
            M3MotionSettings.setMotionScheme(target, visualPopupMotionScheme());
            target.showPicker();
            assertTrue(target.isShowing());

            Node popupRoot = Objects.requireNonNull(pickerPopupRoot(target), "date picker field popup");
            layoutPopupRoot(popupRoot);
            targetReference.set(target);
            popupRootReference.set(popupRoot);
        }, () -> {
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "date picker field popup");
            layoutPopupRoot(popupRoot);
            openingReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening date picker popup snapshot"),
                    "date-picker-field-popup",
                    "opening"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            M3DatePickerField target = Objects.requireNonNull(targetReference.get(), "date picker field");
            assertTrue(target.isShowing());
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "date picker field popup");
            layoutPopupRoot(popupRoot);
            settledReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                    "date-picker-field-popup",
                    "settled"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                    "date picker field popup"
            );
            target.hidePicker();
        });

        runOnFxThreadAfterDelay(Duration.millis(360.0), () -> {
        }, () -> {
            Node popupRoot = Objects.requireNonNull(popupRootReference.get(), "date picker field popup");
            layoutPopupRoot(popupRoot);
            hidingReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding date picker popup snapshot"),
                    "date-picker-field-popup",
                    "hiding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(360.0), () -> {
        }, () -> {
            M3DatePickerField target = Objects.requireNonNull(targetReference.get(), "date picker field");
            assertFalse(target.isShowing());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening date picker popup snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                "date picker field popup enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled date picker popup snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding date picker popup snapshot"),
                "date picker field popup exit motion"
        );
    }

    /// Returns a popup-specific motion scheme that makes real visual intermediate frames observable.
    private static M3MotionScheme visualPopupMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                M3MotionSpec.create(Duration.millis(600.0), M3MotionEasing.LINEAR),
                standard.defaultSpatial(),
                standard.slowSpatial()
        );
    }

    /// Verifies selected-indicator motion on a demo navigation item.
    private static void verifyNavigationItemSelectionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference,
            String pageTitle,
            String itemText,
            String snapshotName
    ) throws InterruptedException {
        AtomicReference<@Nullable M3NavigationItem> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> normalReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> intermediateReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting(pageTitle);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3NavigationItem target = Objects.requireNonNull(firstVisibleNavigationItemWithText(
                    scene.getRoot(),
                    itemText
            ), "navigation item");
            assertFalse(target.isSelected());
            M3MotionSettings.setMotionScheme(target, visualNavigationMotionScheme());
            targetReference.set(target);
            normalReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(normalReference.get(), "normal navigation snapshot"),
                    snapshotName,
                    "normal"
            );
            target.fire();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertTrue(target.isSelected());
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            intermediateReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                    snapshotName,
                    "intermediate"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(520.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            settledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled navigation snapshot"),
                    snapshotName,
                    "settled"
            );
            M3MotionSettings.clearMotionScheme(Objects.requireNonNull(targetReference.get(), "navigation item"));
        });

        M3NavigationItem target = Objects.requireNonNull(targetReference.get(), "navigation item");
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(normalReference.get(), "normal navigation snapshot"),
                Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                snapshotName + " intermediate frame"
        );
        assertNodeAreaChanged(
                target,
                Objects.requireNonNull(intermediateReference.get(), "intermediate navigation snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled navigation snapshot"),
                snapshotName + " settling frame"
        );
    }

    /// Verifies expand and collapse motion on the demo sidebar's visible drawer group.
    private static void verifySidebarDrawerGroupExpansionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3NavigationDrawerGroup> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Buttons");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3NavigationDrawerGroup target = Objects.requireNonNull(firstVisibleDrawerGroupWithTitle(
                    scene.getRoot(),
                    "Buttons"
            ), "sidebar drawer group");
            M3MotionSettings.setAnimationsEnabled(target, false);
            target.setExpanded(false);
            M3MotionSettings.clearAnimationsEnabled(target);
            M3MotionSettings.setMotionScheme(target, visualNavigationMotionScheme());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            targetReference.set(target);
            collapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsedReference.get(), "collapsed drawer group snapshot"),
                    "sidebar-drawer-group",
                    "collapsed"
            );
            target.setExpanded(true);
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            expandingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                    "sidebar-drawer-group",
                    "expanding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(520.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3NavigationDrawerGroup target = Objects.requireNonNull(targetReference.get(), "sidebar drawer group");
            expandedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                    "sidebar-drawer-group",
                    "expanded"
            );
            assertTrue(target.isExpanded());
            target.setExpanded(false);
        });

        runOnFxThreadAfterDelay(Duration.millis(240.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            collapsingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                    "sidebar-drawer-group",
                    "collapsing"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(520.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3NavigationDrawerGroup target = Objects.requireNonNull(targetReference.get(), "sidebar drawer group");
            settledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled drawer group snapshot"),
                    "sidebar-drawer-group",
                    "settled"
            );
            assertFalse(target.isExpanded());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(collapsedReference.get(), "collapsed drawer group snapshot"),
                Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                "sidebar drawer group expand intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandingReference.get(), "expanding drawer group snapshot"),
                Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                "sidebar drawer group expand settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandedReference.get(), "expanded drawer group snapshot"),
                Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                "sidebar drawer group collapse intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(collapsingReference.get(), "collapsing drawer group snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled drawer group snapshot"),
                "sidebar drawer group collapse settling frame"
        );
    }

    /// Returns a navigation-specific motion scheme that makes selection and disclosure frames observable.
    private static M3MotionScheme visualNavigationMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(Duration.millis(600.0), M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                observableSpec,
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies bottom sheet hide and show motion in the demo page.
    private static void verifyBottomSheetVisibilityAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3BottomSheet> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> shownReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> showingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> resettledReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Bottom Sheets");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3BottomSheet target = Objects.requireNonNull(firstVisibleBottomSheetWithHeadline(
                    scene.getRoot(),
                    "Now playing"
            ), "bottom sheet");
            M3MotionSettings.setMotionScheme(target, visualSheetMotionScheme());
            targetReference.set(target);
            shownReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(shownReference.get(), "shown bottom sheet snapshot"),
                    "bottom-sheet",
                    "shown"
            );
            target.hide();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            hidingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                    "bottom-sheet",
                    "hiding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3BottomSheet target = Objects.requireNonNull(targetReference.get(), "bottom sheet");
            hiddenReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                    "bottom-sheet",
                    "hidden"
            );
            assertFalse(target.isVisible());
            target.show();
        });

        runOnFxThreadAfterDelay(Duration.millis(240.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            showingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                    "bottom-sheet",
                    "showing"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3BottomSheet target = Objects.requireNonNull(targetReference.get(), "bottom sheet");
            resettledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(resettledReference.get(), "resettled bottom sheet snapshot"),
                    "bottom-sheet",
                    "resettled"
            );
            assertTrue(target.isShown());
            assertTrue(target.isVisible());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(shownReference.get(), "shown bottom sheet snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                "bottom sheet hide intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding bottom sheet snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                "bottom sheet hide settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hiddenReference.get(), "hidden bottom sheet snapshot"),
                Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                "bottom sheet show intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(showingReference.get(), "showing bottom sheet snapshot"),
                Objects.requireNonNull(resettledReference.get(), "resettled bottom sheet snapshot"),
                "bottom sheet show settling frame"
        );
    }

    /// Verifies side sheet hide and show motion in the demo page.
    private static void verifySideSheetVisibilityAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SideSheet> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> shownReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> showingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> resettledReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Side Sheets");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SideSheet target = Objects.requireNonNull(firstVisibleSideSheetWithHeadline(
                    scene.getRoot(),
                    "Details"
            ), "side sheet");
            M3MotionSettings.setMotionScheme(target, visualSheetMotionScheme());
            targetReference.set(target);
            shownReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(shownReference.get(), "shown side sheet snapshot"),
                    "side-sheet",
                    "shown"
            );
            target.hide();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            hidingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                    "side-sheet",
                    "hiding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SideSheet target = Objects.requireNonNull(targetReference.get(), "side sheet");
            hiddenReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                    "side-sheet",
                    "hidden"
            );
            assertFalse(target.isVisible());
            target.show();
        });

        runOnFxThreadAfterDelay(Duration.millis(240.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            showingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                    "side-sheet",
                    "showing"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SideSheet target = Objects.requireNonNull(targetReference.get(), "side sheet");
            resettledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(resettledReference.get(), "resettled side sheet snapshot"),
                    "side-sheet",
                    "resettled"
            );
            assertTrue(target.isShown());
            assertTrue(target.isVisible());
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(shownReference.get(), "shown side sheet snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                "side sheet hide intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding side sheet snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                "side sheet hide settling frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hiddenReference.get(), "hidden side sheet snapshot"),
                Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                "side sheet show intermediate frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(showingReference.get(), "showing side sheet snapshot"),
                Objects.requireNonNull(resettledReference.get(), "resettled side sheet snapshot"),
                "side sheet show settling frame"
        );
    }

    /// Returns a sheet-specific motion scheme that makes visibility motion frames observable.
    private static M3MotionScheme visualSheetMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(Duration.millis(600.0), M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                standard.fastEffects(),
                standard.defaultEffects(),
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
        );
    }

    /// Verifies snackbar entrance and dismissal motion on the demo host.
    private static void verifySnackbarHostAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3SnackbarHost> hostReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> openingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> settledReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hidingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> hiddenReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Snackbars");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3SnackbarHost host = Objects.requireNonNull(firstVisibleSnackbarHost(scene.getRoot()), "snackbar host");
            M3MotionSettings.setMotionScheme(host, visualOverlayMotionScheme());
            host.setDisplayDuration(Duration.INDEFINITE);
            host.show("Theme-aware snackbar", "Action", event -> {
            });
            hostReference.set(host);
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            M3Snackbar snackbar = Objects.requireNonNull(host.getSnackbar(), "opening snackbar");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertSnackbarStaysCompact(scene, snackbar);
            openingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(openingReference.get(), "opening snackbar snapshot"),
                    "snackbar-host",
                    "opening"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            M3Snackbar snackbar = Objects.requireNonNull(host.getSnackbar(), "settled snackbar");
            assertTrue(host.isShowing());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertSnackbarStaysCompact(scene, snackbar);
            settledReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                    "snackbar-host",
                    "settled"
            );
            host.dismiss();
        });

        runOnFxThreadAfterDelay(Duration.millis(240.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            assertNotNull(host.getSnackbar(), "hiding snackbar");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            hidingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                    "snackbar-host",
                    "hiding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3SnackbarHost host = Objects.requireNonNull(hostReference.get(), "snackbar host");
            assertFalse(host.isShowing());
            assertNull(host.getSnackbar(), "hidden snackbar");
            hiddenReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(hiddenReference.get(), "hidden snackbar snapshot"),
                    "snackbar-host",
                    "hidden"
            );
            M3MotionSettings.clearMotionScheme(host);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(openingReference.get(), "opening snackbar snapshot"),
                Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                "snackbar host enter motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(settledReference.get(), "settled snackbar snapshot"),
                Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                "snackbar host exit motion"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(hidingReference.get(), "hiding snackbar snapshot"),
                Objects.requireNonNull(hiddenReference.get(), "hidden snackbar snapshot"),
                "snackbar host hidden frame"
        );
    }

    /// Verifies floating action button menu expand and collapse motion.
    private static void verifyFabMenuExpansionAnimation(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3FabMenu> targetReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> expandedReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> collapsingReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> recollapsedReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(160.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("FAB Menu");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3FabMenu target = Objects.requireNonNull(firstVisibleFabMenu(scene.getRoot(), false), "collapsed FAB menu");
            M3MotionSettings.setMotionScheme(target, visualOverlayMotionScheme());
            targetReference.set(target);
            collapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsedReference.get(), "collapsed FAB menu snapshot"),
                    "fab-menu",
                    "collapsed"
            );
            target.show();
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            expandingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                    "fab-menu",
                    "expanding"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3FabMenu target = Objects.requireNonNull(targetReference.get(), "FAB menu");
            assertTrue(target.isExpanded());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertFabMenuActionsStayInsideShowcase(target);
            expandedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                    "fab-menu",
                    "expanded"
            );
            target.hide();
        });

        runOnFxThreadAfterDelay(Duration.millis(240.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            collapsingReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                    "fab-menu",
                    "collapsing"
            );
        });

        runOnFxThreadAfterDelay(Duration.millis(620.0), () -> {
        }, () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3FabMenu target = Objects.requireNonNull(targetReference.get(), "FAB menu");
            assertFalse(target.isExpanded());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            recollapsedReference.set(snapshot(scene));
            writeAnimationSnapshot(
                    Objects.requireNonNull(recollapsedReference.get(), "recollapsed FAB menu snapshot"),
                    "fab-menu",
                    "recollapsed"
            );
            M3MotionSettings.clearMotionScheme(target);
        });

        assertSnapshotChanged(
                Objects.requireNonNull(collapsedReference.get(), "collapsed FAB menu snapshot"),
                Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                "FAB menu expanding frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandingReference.get(), "expanding FAB menu snapshot"),
                Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                "FAB menu expanded frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(expandedReference.get(), "expanded FAB menu snapshot"),
                Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                "FAB menu collapsing frame"
        );
        assertSnapshotChanged(
                Objects.requireNonNull(collapsingReference.get(), "collapsing FAB menu snapshot"),
                Objects.requireNonNull(recollapsedReference.get(), "recollapsed FAB menu snapshot"),
                "FAB menu recollapsed frame"
        );
    }

    /// Verifies that a rich tooltip remains interactive while pointer focus transfers into its popup.
    private static void verifyRichTooltipInteractiveLifetime(
            AtomicReference<@Nullable M3FXDemoApp> appReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Button> ownerReference = new AtomicReference<>();
        AtomicReference<@Nullable M3RichTooltip> tooltipReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> popupRootReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> popupReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(120.0), () -> {
            M3FXDemoApp app = Objects.requireNonNull(appReference.get(), "app");
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            app.showPageForTesting("Tooltips");
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            M3Button owner = Objects.requireNonNull(firstVisibleButtonWithText(
                    scene.getRoot(),
                    "Rich action"
            ), "rich tooltip owner");
            M3Button action = new M3Button("Open");
            M3RichTooltip tooltip = M3RichTooltip.install(
                    owner,
                    "Generated theme",
                    "The tooltip keeps its action surface available while pointer focus moves into the popup.",
                    action
            );
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setHideDelay(Duration.millis(160.0));
            tooltip.setShowDuration(Duration.INDEFINITE);
            ownerReference.set(owner);
            tooltipReference.set(tooltip);
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_ENTERED, false);
        }, () -> {
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "rich tooltip owner");
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertTrue(tooltip.isShowing());
            Node popupRoot = Objects.requireNonNull(tooltip.getScene(), "rich tooltip scene").getRoot();
            layoutPopupRoot(popupRoot);
            assertTooltipNearOwner(owner, popupRoot);
            assertRichTooltipActionInsidePopup(popupRoot);
            popupRootReference.set(popupRoot);
            popupReference.set(snapshotNode(popupRoot));
            writeAnimationSnapshot(
                    Objects.requireNonNull(popupReference.get(), "rich tooltip snapshot"),
                    "rich-tooltip",
                    "shown"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(popupReference.get(), "rich tooltip snapshot"),
                    "rich tooltip"
            );
            firePrimaryMouseEvent(owner, MouseEvent.MOUSE_EXITED, false);
            firePrimaryMouseEvent(popupRoot, MouseEvent.MOUSE_ENTERED, false);
        });

        runOnFxThreadAfterDelay(Duration.millis(260.0), () -> {
        }, () -> {
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertTrue(tooltip.isShowing());
            tooltip.hide();
        });

        runOnFxThreadAfterDelay(Duration.millis(80.0), () -> {
        }, () -> {
            M3Button owner = Objects.requireNonNull(ownerReference.get(), "rich tooltip owner");
            M3RichTooltip tooltip = Objects.requireNonNull(tooltipReference.get(), "rich tooltip");
            assertFalse(tooltip.isShowing());
            M3RichTooltip.uninstall(owner, tooltip);
        });
    }

    /// Verifies that the real dialog window uses a compact Material dialog pane.
    private static void verifyDialogPopupSurface(
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        AtomicReference<@Nullable M3Dialog<ButtonType>> dialogReference = new AtomicReference<>();
        AtomicReference<@Nullable WritableImage> dialogSnapshotReference = new AtomicReference<>();

        runOnFxThreadAfterDelay(Duration.millis(240.0), () -> {
            Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3Dialog<ButtonType> dialog = new M3Dialog<>(
                    "M3FX Demo Dialog",
                    "Dialog title",
                    "The active theme is applied to this dialog pane.",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );
            dialog.initOwner(scene.getRoot());
            dialog.getDialogPane().setPrefWidth(420.0);
            dialog.show();
            dialogReference.set(dialog);
        }, () -> {
            Scene ownerScene = Objects.requireNonNull(sceneReference.get(), "scene");
            M3Dialog<ButtonType> dialog = Objects.requireNonNull(dialogReference.get(), "dialog");
            assertTrue(dialog.isShowing());
            Node dialogPane = dialog.getDialogPane();
            dialogPane.applyCss();
            if (dialogPane instanceof Parent parent) {
                parent.layout();
            }
            assertDialogPaneStaysCompact(ownerScene, dialogPane);
            dialogSnapshotReference.set(snapshotNode(dialogPane));
            writeAnimationSnapshot(
                    Objects.requireNonNull(dialogSnapshotReference.get(), "dialog popup snapshot"),
                    "dialog-popup",
                    "shown"
            );
            assertSnapshotHasVisibleContent(
                    Objects.requireNonNull(dialogSnapshotReference.get(), "dialog popup snapshot"),
                    "dialog popup"
            );
            dialog.close();
        });
    }

    /// Returns an overlay-specific motion scheme that makes popup and surface transitions observable.
    private static M3MotionScheme visualOverlayMotionScheme() {
        M3MotionScheme standard = M3MotionScheme.standard();
        M3MotionSpec observableSpec = M3MotionSpec.create(Duration.millis(600.0), M3MotionEasing.LINEAR);
        return M3MotionScheme.create(
                observableSpec,
                observableSpec,
                standard.slowEffects(),
                observableSpec,
                observableSpec,
                standard.slowSpatial()
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

    /// Captures one standalone node as a writable image.
    private static WritableImage snapshotNode(Node node) {
        Bounds bounds = node.getLayoutBounds();
        int width = Math.max(1, (int) Math.ceil(bounds.getWidth()));
        int height = Math.max(1, (int) Math.ceil(bounds.getHeight()));
        WritableImage image = new WritableImage(width, height);
        node.snapshot(null, image);
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
        assertNavigationBadgesStayCompact(scene, pageTitle);
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

    /// Verifies that navigation badges stay compact and anchored instead of stretching over the selected indicator.
    private static void assertNavigationBadgesStayCompact(Scene scene, String pageTitle) {
        Bounds sceneBounds = scene.getRoot().localToScene(scene.getRoot().getBoundsInLocal());
        visitVisibleNodes(scene.getRoot(), node -> {
            if (!(node instanceof M3NavigationItem item) || !hasRenderableBounds(item)) {
                return;
            }

            @Nullable Node badge = item.lookup(".m3-navigation-item-badge");
            @Nullable Node indicator = item.lookup(".m3-navigation-item-indicator");
            if (badge == null || indicator == null || !hasRenderableBounds(badge) || !hasRenderableBounds(indicator)) {
                return;
            }

            Bounds badgeBounds = badge.localToScene(badge.getBoundsInLocal());
            Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
            if (!isVisibleWithinSceneViewport(badge, badgeBounds, sceneBounds)) {
                return;
            }

            double maximumBadgeWidth = Math.max(24.0, indicatorBounds.getWidth() * 0.65);
            double minimumCenterOffset = Math.max(6.0, indicatorBounds.getWidth() * 0.16);
            double centerOffset = Math.abs(badgeBounds.getCenterX() - indicatorBounds.getCenterX());
            assertTrue(badgeBounds.getWidth() <= maximumBadgeWidth && centerOffset >= minimumCenterOffset,
                    () -> pageTitle + " navigation badge has unsafe indicator geometry: badgeBounds="
                            + badgeBounds + ", indicatorBounds=" + indicatorBounds + ", centerOffset=" + centerOffset);
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
                || node instanceof M3PickerField<?, ?>
                || node instanceof M3RadioButton
                || node instanceof M3SegmentedButton
                || node instanceof M3SplitButton
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

    /// Returns the first visible M3 switch with the requested text.
    private static @Nullable M3Switch firstVisibleSwitchWithText(Node root, String text) {
        if (root instanceof M3Switch switchControl
                && switchControl.isVisible()
                && text.equals(switchControl.getText())
                && hasRenderableBounds(switchControl)) {
            return switchControl;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3Switch result = firstVisibleSwitchWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 split button with the requested text.
    private static @Nullable M3SplitButton firstVisibleSplitButtonWithText(Node root, String text) {
        if (root instanceof M3SplitButton splitButton
                && splitButton.isVisible()
                && text.equals(splitButton.getText())
                && hasRenderableBounds(splitButton)) {
            return splitButton;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SplitButton result = firstVisibleSplitButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 menu button with the requested text.
    private static @Nullable M3MenuButton firstVisibleMenuButtonWithText(Node root, String text) {
        if (root instanceof M3MenuButton menuButton
                && menuButton.isVisible()
                && text.equals(menuButton.getText())
                && hasRenderableBounds(menuButton)) {
            return menuButton;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3MenuButton result = firstVisibleMenuButtonWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 date picker field.
    private static @Nullable M3DatePickerField firstVisibleDatePickerField(Node root) {
        if (root instanceof M3DatePickerField field && field.isVisible() && hasRenderableBounds(field)) {
            return field;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3DatePickerField result = firstVisibleDatePickerField(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 submenu item with the requested text.
    private static @Nullable M3SubMenuItem firstVisibleSubMenuItemWithText(Node root, String text) {
        if (root instanceof M3SubMenuItem subMenuItem
                && subMenuItem.isVisible()
                && text.equals(subMenuItem.getHeadlineText())
                && hasRenderableBounds(subMenuItem)) {
            return subMenuItem;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SubMenuItem result = firstVisibleSubMenuItemWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible M3 navigation item with the requested text.
    private static @Nullable M3NavigationItem firstVisibleNavigationItemWithText(Node root, String text) {
        if (root instanceof M3NavigationItem item
                && item.isVisible()
                && text.equals(item.getText())
                && hasRenderableBounds(item)) {
            return item;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3NavigationItem result = firstVisibleNavigationItemWithText(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible navigation drawer group with the requested title.
    private static @Nullable M3NavigationDrawerGroup firstVisibleDrawerGroupWithTitle(Node root, String title) {
        if (root instanceof M3NavigationDrawerGroup group
                && group.isVisible()
                && title.equals(group.getTitle())
                && hasRenderableBounds(group)) {
            return group;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3NavigationDrawerGroup result = firstVisibleDrawerGroupWithTitle(child, title);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible bottom sheet with the requested headline.
    private static @Nullable M3BottomSheet firstVisibleBottomSheetWithHeadline(Node root, String headline) {
        if (root instanceof M3BottomSheet sheet
                && sheet.isVisible()
                && headline.equals(sheet.getHeadline())
                && hasRenderableBounds(sheet)) {
            return sheet;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3BottomSheet result = firstVisibleBottomSheetWithHeadline(child, headline);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible side sheet with the requested headline.
    private static @Nullable M3SideSheet firstVisibleSideSheetWithHeadline(Node root, String headline) {
        if (root instanceof M3SideSheet sheet
                && sheet.isVisible()
                && headline.equals(sheet.getHeadline())
                && hasRenderableBounds(sheet)) {
            return sheet;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SideSheet result = firstVisibleSideSheetWithHeadline(child, headline);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible snackbar host.
    private static @Nullable M3SnackbarHost firstVisibleSnackbarHost(Node root) {
        if (root instanceof M3SnackbarHost host && host.isVisible() && hasRenderableBounds(host)) {
            return host;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3SnackbarHost result = firstVisibleSnackbarHost(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the first visible FAB menu with the requested expanded state.
    private static @Nullable M3FabMenu firstVisibleFabMenu(Node root, boolean expanded) {
        if (root instanceof M3FabMenu menu
                && menu.isVisible()
                && menu.isExpanded() == expanded
                && hasRenderableBounds(menu)) {
            return menu;
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable M3FabMenu result = firstVisibleFabMenu(child, expanded);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /// Returns the popup root that hosts a picker field's popup picker.
    private static @Nullable Node pickerPopupRoot(M3DatePickerField field) {
        @Nullable Parent parent = field.getPicker().getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains(M3PickerField.POPUP_STYLE_CLASS)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Applies CSS and layout to a popup root before capturing it.
    private static void layoutPopupRoot(Node popupRoot) {
        popupRoot.applyCss();
        if (popupRoot instanceof Parent parent) {
            parent.layout();
        }
    }

    /// Verifies that an owning popup and nested popup are positioned beside each other on screen.
    private static void assertPopupStackSideBySide(Node ownerPopupRoot, Node childPopupRoot) {
        @Nullable Bounds ownerBounds = ownerPopupRoot.localToScreen(ownerPopupRoot.getBoundsInLocal());
        @Nullable Bounds childBounds = childPopupRoot.localToScreen(childPopupRoot.getBoundsInLocal());
        assertNotNull(ownerBounds, "owner popup screen bounds");
        assertNotNull(childBounds, "child popup screen bounds");

        double horizontalOverlap = Math.max(
                0.0,
                Math.min(ownerBounds.getMaxX(), childBounds.getMaxX())
                        - Math.max(ownerBounds.getMinX(), childBounds.getMinX())
        );
        double verticalOverlap = Math.max(
                0.0,
                Math.min(ownerBounds.getMaxY(), childBounds.getMaxY())
                        - Math.max(ownerBounds.getMinY(), childBounds.getMinY())
        );
        double maximumHorizontalOverlap = Math.max(24.0, Math.min(ownerBounds.getWidth(), childBounds.getWidth()) * 0.16);
        double minimumVerticalOverlap = Math.min(ownerBounds.getHeight(), childBounds.getHeight()) * 0.25;
        assertTrue(horizontalOverlap <= maximumHorizontalOverlap && verticalOverlap >= minimumVerticalOverlap,
                () -> "Nested popup stack has unsafe placement: ownerBounds=" + ownerBounds
                        + ", childBounds=" + childBounds + ", horizontalOverlap=" + horizontalOverlap
                        + ", verticalOverlap=" + verticalOverlap);
    }

    /// Verifies that a snackbar occupies its compact message surface instead of the whole overlay.
    private static void assertSnackbarStaysCompact(Scene scene, M3Snackbar snackbar) {
        Bounds bounds = snackbar.localToScene(snackbar.getBoundsInLocal());
        assertTrue(bounds.getWidth() >= 160.0 && bounds.getWidth() <= scene.getWidth() * 0.75,
                () -> "Snackbar width is not compact: bounds=" + bounds + ", sceneWidth=" + scene.getWidth());
        assertTrue(bounds.getHeight() >= 40.0 && bounds.getHeight() <= 96.0,
                () -> "Snackbar height is not compact: bounds=" + bounds);
    }

    /// Verifies that a tooltip popup appears near its owner and stays at tooltip scale.
    private static void assertTooltipNearOwner(Node owner, Node popupRoot) {
        @Nullable Bounds ownerBounds = owner.localToScreen(owner.getBoundsInLocal());
        @Nullable Bounds popupBounds = popupRoot.localToScreen(popupRoot.getBoundsInLocal());
        assertNotNull(ownerBounds, "tooltip owner screen bounds");
        assertNotNull(popupBounds, "tooltip popup screen bounds");
        assertTrue(popupBounds.getMinY() >= ownerBounds.getMaxY() - 2.0,
                () -> "Tooltip popup is not below its owner: ownerBounds=" + ownerBounds
                        + ", popupBounds=" + popupBounds);
        assertTrue(popupBounds.getWidth() <= 420.0 && popupBounds.getHeight() <= 220.0,
                () -> "Tooltip popup is not compact: popupBounds=" + popupBounds);
    }

    /// Verifies that a rich tooltip action button is fully contained by the popup root.
    private static void assertRichTooltipActionInsidePopup(Node popupRoot) {
        M3Button action = Objects.requireNonNull(firstVisibleButtonWithText(popupRoot, "Open"), "rich tooltip action");
        Bounds popupBounds = popupRoot.localToScene(popupRoot.getBoundsInLocal());
        Bounds actionBounds = action.localToScene(action.getBoundsInLocal());
        assertTrue(containsBoundsWithTolerance(popupBounds, actionBounds, CONTROL_EDGE_TOLERANCE),
                () -> "Rich tooltip action is clipped: popupBounds=" + popupBounds
                        + ", actionBounds=" + actionBounds);
        assertTrue(popupBounds.getMaxY() - actionBounds.getMaxY() >= 4.0,
                () -> "Rich tooltip action has no safe bottom padding: popupBounds=" + popupBounds
                        + ", actionBounds=" + actionBounds);
    }

    /// Verifies that a dialog pane remains a compact dialog surface.
    private static void assertDialogPaneStaysCompact(Scene ownerScene, Node dialogPane) {
        Bounds bounds = dialogPane.getBoundsInLocal();
        assertTrue(bounds.getWidth() >= 280.0 && bounds.getWidth() <= ownerScene.getWidth() * 0.70,
                () -> "Dialog pane width is not compact: bounds=" + bounds
                        + ", ownerWidth=" + ownerScene.getWidth());
        assertTrue(bounds.getHeight() >= 120.0 && bounds.getHeight() <= ownerScene.getHeight() * 0.70,
                () -> "Dialog pane height is not compact: bounds=" + bounds
                        + ", ownerHeight=" + ownerScene.getHeight());
    }

    /// Verifies that expanded FAB menu action items remain within the owning demo showcase surface.
    private static void assertFabMenuActionsStayInsideShowcase(M3FabMenu menu) {
        Node showcase = Objects.requireNonNull(
                nearestAncestorWithStyle(menu, "demo-flow"),
                "FAB menu showcase flow"
        );
        Bounds showcaseBounds = showcase.localToScene(showcase.getBoundsInLocal());
        for (Node item : menu.getItems()) {
            if (!item.isVisible() || !hasRenderableBounds(item)) {
                continue;
            }
            Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
            assertTrue(containsBoundsWithTolerance(showcaseBounds, itemBounds, CONTROL_EDGE_TOLERANCE),
                    () -> "FAB menu action item escaped its showcase: showcaseBounds=" + showcaseBounds
                            + ", itemBounds=" + itemBounds);
        }
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

    /// Verifies that two standalone snapshots differ by more than antialiasing noise.
    private static void assertSnapshotChanged(WritableImage before, WritableImage after, String description) {
        int width = (int) Math.min(before.getWidth(), after.getWidth());
        int height = (int) Math.min(before.getHeight(), after.getHeight());
        int changedPixels = 0;
        int totalPixels = Math.max(1, width * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixelDistance(before.getPixelReader().getColor(x, y), after.getPixelReader().getColor(x, y)) > 0.02) {
                    changedPixels++;
                }
            }
        }
        int minimumChangedPixels = Math.max(8, totalPixels / 250);
        int finalChangedPixels = changedPixels;
        assertTrue(finalChangedPixels >= minimumChangedPixels,
                () -> description + " produced too little visual change: changed="
                        + finalChangedPixels + ", minimum=" + minimumChangedPixels
                        + ", before=" + before.getWidth() + "x" + before.getHeight()
                        + ", after=" + after.getWidth() + "x" + after.getHeight());
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

    /// Returns the nearest ancestor with the requested style class.
    private static @Nullable Node nearestAncestorWithStyle(Node node, String styleClass) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent.getStyleClass().contains(styleClass)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
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

    /// Returns an RGBA distance between two pixels.
    private static double pixelDistance(Color first, Color second) {
        return colorDistance(first, second) + Math.abs(first.getOpacity() - second.getOpacity());
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
