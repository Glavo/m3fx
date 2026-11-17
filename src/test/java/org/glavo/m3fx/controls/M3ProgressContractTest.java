// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.shape.M3ShapeMorph;
import org.glavo.m3fx.skins.M3LoadingIndicatorSkin;
import org.glavo.m3fx.skins.M3ProgressBarSkin;
import org.glavo.m3fx.skins.M3ProgressIndicatorSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies progress and loading indicator geometry, motion, and lifecycle.
@NotNullByDefault
final class M3ProgressContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// The pulse count used after a circular progress indicator has crossed a real rendered cycle seam.
    private static final int PROGRESS_INDICATOR_CYCLE_STABLE_PULSES = 2;

    /// The largest accepted visible circular progress angle delta across the cycle seam.
    private static final double MAXIMUM_PROGRESS_INDICATOR_CYCLE_SEAM_DELTA_DEGREES = 150.0;

    /// The square snapshot size used by loading indicator morph geometry tests.
    private static final double LOADING_INDICATOR_TEST_FRAME_SIZE = 140.0;

    /// The active shape size used by loading indicator morph geometry tests.
    private static final double LOADING_INDICATOR_TEST_INDICATOR_SIZE = 89.0;

    /// The active shape center used by loading indicator morph geometry tests.
    private static final double LOADING_INDICATOR_TEST_CENTER = LOADING_INDICATOR_TEST_FRAME_SIZE / 2.0;

    /// The rotation added by each loading indicator morph segment.
    private static final double LOADING_INDICATOR_QUARTER_ROTATION = 0.25;

    /// The largest active-shape scale added during loading indicator morphing.
    private static final double LOADING_INDICATOR_MORPH_SCALE_AMPLITUDE = 0.12;

    /// Verifies that progress component token properties are styleable from CSS.
    @Test
    void progressTokensAreStyleable() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        progressBar.setStyle("-m3-track-thickness: 6px; "
                + "-m3-track-shape: 18px; "
                + "-m3-wave-amplitude: 3px; "
                + "-m3-wavelength: 36px; "
                + "-m3-indeterminate-wavelength: 19px; "
                + "-m3-track-gap: 7px; "
                + "-m3-stop-size: 8px;");

        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        progressIndicator.setStyle("-m3-track-thickness: 6px; "
                + "-m3-indicator-size: 72px; "
                + "-m3-wave-indicator-size: 80px; "
                + "-m3-wave-amplitude: 2px; "
                + "-m3-wavelength: 18px; "
                + "-m3-track-gap: 5px;");

        applyCss(progressBar);
        applyCss(progressIndicator);

        assertEquals(6.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(18.0, progressBar.getTrackShape(), 0.0001);
        assertEquals(3.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(36.0, progressBar.getWavelength(), 0.0001);
        assertEquals(19.0, progressBar.getIndeterminateWavelength(), 0.0001);
        assertEquals(7.0, progressBar.getTrackGap(), 0.0001);
        assertEquals(8.0, progressBar.getStopSize(), 0.0001);
        assertEquals(12.0, progressBar.prefHeight(-1.0), 0.0001);
        assertEquals(6.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(72.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(80.0, progressIndicator.getWaveIndicatorSize(), 0.0001);
        assertEquals(2.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(18.0, progressIndicator.getWavelength(), 0.0001);
        assertEquals(5.0, progressIndicator.getTrackGap(), 0.0001);
        assertEquals(80.0, progressIndicator.prefWidth(-1.0), 0.0001);
        assertEquals(80.0, progressIndicator.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that fallback progress and loading indicator tokens match Material Design defaults.
    @Test
    void progressAndLoadingFallbackTokensMatchMaterialDefaults() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();

        applyCss(progressBar);
        applyCss(progressIndicator);
        applyCss(loadingIndicator);

        assertEquals(0.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(40.0, progressBar.getWavelength(), 0.0001);
        assertEquals(20.0, progressBar.getIndeterminateWavelength(), 0.0001);
        assertEquals(40.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(48.0, progressIndicator.getWaveIndicatorSize(), 0.0001);
        assertEquals(40.0, progressIndicator.prefWidth(-1.0), 0.0001);
        assertEquals(40.0, progressIndicator.prefHeight(-1.0), 0.0001);
        assertEquals(48.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(38.0, loadingIndicator.getIndicatorSize(), 0.0001);
        assertEquals(48.0, loadingIndicator.prefWidth(-1.0), 0.0001);
        assertEquals(48.0, loadingIndicator.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that loading indicator token properties are styleable from CSS.
    @Test
    void loadingIndicatorTokensAreStyleable() {
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        loadingIndicator.setStyle("-m3-container-size: 72px; "
                + "-m3-indicator-size: 48px;");

        applyCss(loadingIndicator);

        assertEquals(72.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(48.0, loadingIndicator.getIndicatorSize(), 0.0001);
        assertEquals(72.0, loadingIndicator.prefWidth(-1.0), 0.0001);
        assertEquals(72.0, loadingIndicator.prefHeight(-1.0), 0.0001);
    }

    /// Verifies that the Expressive profile retains Flat progress defaults while publishing Wavy metrics.
    @Test
    void expressiveProgressTokensKeepFlatDefaults() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        Pane root = new Pane(progressBar, progressIndicator, loadingIndicator);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();

        assertEquals(4.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(0.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(40.0, progressBar.getWavelength(), 0.0001);
        assertEquals(20.0, progressBar.getIndeterminateWavelength(), 0.0001);
        assertEquals(4.0, progressBar.getTrackGap(), 0.0001);
        assertEquals(4.0, progressBar.getStopSize(), 0.0001);
        assertEquals(4.0, progressBar.prefHeight(-1.0), 0.0001);
        assertEquals(4.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(40.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(48.0, progressIndicator.getWaveIndicatorSize(), 0.0001);
        assertEquals(0.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(15.0, progressIndicator.getWavelength(), 0.0001);
        assertEquals(4.0, progressIndicator.getTrackGap(), 0.0001);
        assertEquals(40.0, progressIndicator.prefWidth(-1.0), 0.0001);
        assertEquals(40.0, progressIndicator.prefHeight(-1.0), 0.0001);
        assertEquals(48.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(38.0, loadingIndicator.getIndicatorSize(), 0.0001);
    }

    /// Verifies that m3fx progress controls create Material Design 3 skins.
    @Test
    void progressControlsCreateMaterialSkins() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();

        applyCss(progressBar);
        applyCss(progressIndicator);
        applyCss(loadingIndicator);

        assertInstanceOf(M3ProgressBarSkin.class, progressBar.getSkin());
        assertInstanceOf(M3ProgressIndicatorSkin.class, progressIndicator.getSkin());
        assertInstanceOf(M3LoadingIndicatorSkin.class, loadingIndicator.getSkin());
    }

    /// Verifies that indeterminate progress animations respond to runtime motion setting changes.
    @Tier2Test
    @Test
    void indeterminateProgressAnimationsRefreshWhenMotionSettingsChange() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> indeterminateProgressMotionAdvanced(sceneReference, sampleReference),
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(true);
                        sceneReference.set(scene);
                        sampleReference.set(sampleIndeterminateProgressMotion(scene));
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        assertIndeterminateProgressMotionAdvanced(scene, sampleReference, "enabled motion");
                        FxTestUtils.setCssAnimationsEnabled(scene.root, false);
                        sampleReference.set(sampleIndeterminateProgressMotion(scene));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> indeterminateProgressMotionAdvanced(sceneReference, sampleReference),
                    () -> {
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        assertTrue(M3MotionSettings.shouldReduceMotion(scene.progressBar));
                        assertIndeterminateProgressMotionAdvanced(scene, sampleReference, "reduced motion");
                        FxTestUtils.setCssAnimationsEnabled(scene.root, true);
                        sampleReference.set(sampleIndeterminateProgressMotion(scene));
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> indeterminateProgressMotionAdvanced(sceneReference, sampleReference),
                    () -> {
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        assertFalse(M3MotionSettings.shouldReduceMotion(scene.progressBar));
                        assertIndeterminateProgressMotionAdvanced(scene, sampleReference, "restored motion");
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that the loading indicator restarts each indeterminate morph segment continuously.
    @Tier2Test
    @Test
    void loadingIndicatorIndeterminateMorphSegmentsKeepRunning() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        Set<String> frameSignatures = new HashSet<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> loadingIndicatorObservedDistinctFrames(sceneReference, frameSignatures, 6),
                    2,
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(true);
                        sceneReference.set(scene);
                        loadingIndicatorObservedDistinctFrames(sceneReference, frameSignatures, 1);
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        assertFalse(M3MotionSettings.shouldReduceMotion(scene.loadingIndicator));
                        assertTrue(scene.loadingIndicatorPath.isVisible());
                        assertTrue(scene.loadingIndicatorPath.getElements().size() > 24);
                        assertTrue(frameSignatures.size() >= 6, () -> "frameSignatures=" + frameSignatures);
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that the loading indicator morph reaches an observable scaled frame with local motion settings.
    @Tier2Test
    @Test
    void loadingIndicatorIndeterminateMorphUsesMotionSchemeTiming() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> initialAreaReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> loadingIndicatorBoundsExpandedPastInitial(sceneReference, initialAreaReference, 1.04),
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(true);
                        sceneReference.set(scene);
                        initialAreaReference.set(loadingIndicatorPathBoundsArea(scene.loadingIndicatorPath));
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        double initialArea = Objects.requireNonNull(initialAreaReference.get(), "initialArea");
                        double currentArea = loadingIndicatorPathBoundsArea(scene.loadingIndicatorPath);

                        assertFalse(M3MotionSettings.shouldReduceMotion(scene.loadingIndicator));
                        assertTrue(currentArea > initialArea * 1.04,
                                () -> "initialArea=" + initialArea + ", currentArea=" + currentArea);
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that runtime motion behavior changes reconfigure an already running loading indicator loop.
    @Tier2Test
    @Test
    void loadingIndicatorMotionBehaviorChangesRefreshRunningLoop() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> initialAreaReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> loadingIndicatorBoundsExpandedPastInitial(sceneReference, initialAreaReference, 1.04),
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(
                                true,
                                loadingIndicatorTimingBehavior(Duration.millis(600_000.0), Duration.millis(600_000.0))
                        );
                        sceneReference.set(scene);
                        initialAreaReference.set(loadingIndicatorPathBoundsArea(scene.loadingIndicatorPath));
                        FxTestUtils.setMotionBehavior(scene.root, observableIndeterminateProgressBehavior());
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        double initialArea = Objects.requireNonNull(initialAreaReference.get(), "initialArea");
                        double currentArea = loadingIndicatorPathBoundsArea(scene.loadingIndicatorPath);

                        assertFalse(M3MotionSettings.shouldReduceMotion(scene.loadingIndicator));
                        assertTrue(currentArea > initialArea * 1.04,
                                () -> "initialArea=" + initialArea + ", currentArea=" + currentArea);
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that progress controls expose accessible values and loading remains indeterminate.
    @Test
    void progressControlsExposeAccessibleValues() {
        M3ProgressBar progressBar = new M3ProgressBar(0.42);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.65);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        @Nullable AccessibleAttribute valueStringAttribute = M3Accessible.attribute("VALUE_STRING");

        assertEquals(false, progressBar.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(0.0, progressBar.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(1.0, progressBar.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(0.42, (Double) progressBar.queryAccessibleAttribute(AccessibleAttribute.VALUE), 0.0001);
        if (valueStringAttribute != null) {
            assertEquals("42%", progressBar.queryAccessibleAttribute(valueStringAttribute));
        }
        assertEquals(Orientation.HORIZONTAL, progressBar.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));

        assertEquals(false, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(0.0, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(1.0, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(0.65, (Double) progressIndicator.queryAccessibleAttribute(AccessibleAttribute.VALUE), 0.0001);
        if (valueStringAttribute != null) {
            assertEquals("65%", progressIndicator.queryAccessibleAttribute(valueStringAttribute));
        }

        assertEquals(true, loadingIndicator.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));

        progressBar.setProgress(-10.0);
        progressIndicator.setProgress(Double.NaN);

        assertEquals(true, progressBar.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(M3ProgressBar.INDETERMINATE_PROGRESS,
                (Double) progressBar.queryAccessibleAttribute(AccessibleAttribute.VALUE),
                0.0001);
        if (valueStringAttribute != null) {
            assertEquals("Indeterminate", progressBar.queryAccessibleAttribute(valueStringAttribute));
        }
        assertEquals(true, progressIndicator.queryAccessibleAttribute(AccessibleAttribute.INDETERMINATE));
        assertEquals(M3ProgressIndicator.INDETERMINATE_PROGRESS,
                (Double) progressIndicator.queryAccessibleAttribute(AccessibleAttribute.VALUE),
                0.0001);
        if (valueStringAttribute != null) {
            assertEquals("Indeterminate", progressIndicator.queryAccessibleAttribute(valueStringAttribute));
        }
    }

    /// Verifies that the loading indicator skin lays out one visible morphing shape.
    @Test
    void loadingIndicatorSkinLaysOutSingleMorphingShape() {
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        Pane root = new Pane(loadingIndicator);
        Scene scene = new Scene(root, 120.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        loadingIndicator.resize(72.0, 72.0);
        loadingIndicator.layout();

        Set<Node> indicators = loadingIndicator.lookupAll(".m3-loading-indicator-indicator");
        assertEquals(1, indicators.size());
        Path indicator = assertInstanceOf(Path.class, indicators.iterator().next());
        assertTrue(indicator.isVisible());
        assertTrue(indicator.getElements().size() > 24);
    }

    /// Verifies that disabled loading indicators apply Material content opacity exactly once.
    @Test
    void loadingIndicatorDisabledOpacityIsNotCompounded() {
        M3LoadingIndicator defaultIndicator = new M3LoadingIndicator();
        M3LoadingIndicator containedIndicator = new M3LoadingIndicator();
        containedIndicator.setVariant(M3LoadingIndicatorVariant.CONTAINED);
        defaultIndicator.setDisable(true);
        containedIndicator.setDisable(true);

        HBox root = new HBox(defaultIndicator, containedIndicator);
        Scene scene = new Scene(root, 160.0, 80.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        defaultIndicator.resize(48.0, 48.0);
        containedIndicator.resize(48.0, 48.0);
        defaultIndicator.layout();
        containedIndicator.layout();

        assertEquals(0.38, defaultIndicator.getOpacity(), 0.0001);
        assertEquals(0.38, containedIndicator.getOpacity(), 0.0001);
        assertEquals(1.0,
                defaultIndicator.lookup(".m3-loading-indicator-indicator").getOpacity(),
                0.0001);
        assertEquals(1.0,
                containedIndicator.lookup(".m3-loading-indicator-indicator").getOpacity(),
                0.0001);
        assertEquals(1.0,
                containedIndicator.lookup(".m3-loading-indicator-container").getOpacity(),
                0.0001);
    }

    /// Verifies that loading indicator morph frames reuse their path elements.
    @Tier2Test
    @Test
    void loadingIndicatorMorphFramesReusePathElements() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable List<PathElement>> elementsReference = new AtomicReference<>();
        Set<String> frameSignatures = new HashSet<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> loadingIndicatorObservedDistinctFrames(sceneReference, frameSignatures, 4),
                    2,
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(true);
                        sceneReference.set(scene);
                        elementsReference.set(List.copyOf(scene.loadingIndicatorPath.getElements()));
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        List<PathElement> elements = Objects.requireNonNull(elementsReference.get(), "elements");

                        assertEquals(elements.size(), scene.loadingIndicatorPath.getElements().size());
                        for (int i = 0; i < elements.size(); i++) {
                            assertSame(elements.get(i), scene.loadingIndicatorPath.getElements().get(i));
                        }
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that sampled indeterminate loading indicator frames stay centered.
    @Tier2Test
    @Test
    void loadingIndicatorIndeterminateMorphFramesStayCentered() {
        FxTestUtils.runOnFxThread(() -> {
            LoadingIndicatorFrameScene frameScene = new LoadingIndicatorFrameScene();

            Set<String> boundsSignatures = new HashSet<>();
            int frameSize = (int) LOADING_INDICATOR_TEST_FRAME_SIZE;
            BufferedImage strip = new BufferedImage(frameSize * 7, frameSize, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = strip.createGraphics();
            try {
                for (int segment = 0; segment < 7; segment++) {
                    int segmentIndex = segment;
                    writeLoadingIndicatorMorphFrame(frameScene, segment, 0.5, 0.0);
                    Bounds bounds = frameScene.indicator.getBoundsInLocal();
                    WritableImage frame = snapshotImageOnFxThread(frameScene.root);
                    Point2D centroid = contrastingPixelCentroid(frame, frameScene.indicator, Color.WHITE, 0.04);
                    assertEquals(LOADING_INDICATOR_TEST_CENTER, centroid.getX(), 2.5,
                            () -> "loading indicator rendered centroid is horizontally unstable: segment="
                                    + segmentIndex + ", centroid=" + centroid);
                    assertEquals(LOADING_INDICATOR_TEST_CENTER, centroid.getY(), 2.5,
                            () -> "loading indicator rendered centroid is vertically unstable: segment="
                                    + segmentIndex + ", centroid=" + centroid);
                    boundsSignatures.add(Math.round(bounds.getWidth()) + "x" + Math.round(bounds.getHeight()));
                    graphics.drawImage(toBufferedImage(frame), segment * frameSize, 0, null);
                }
            } finally {
                graphics.dispose();
            }
            assertTrue(boundsSignatures.size() >= 4);
            writeVisualSnapshot(strip, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "visual",
                    "visual-loading-indicator-compose-loop.png"
            ));
        });
    }

    /// Verifies that indeterminate loading indicator morphs scale the active shape between segment endpoints.
    @Test
    void loadingIndicatorIndeterminateMorphScalesWithinSegment() {
        FxTestUtils.runOnFxThread(() -> {
            LoadingIndicatorFrameScene frameScene = new LoadingIndicatorFrameScene();

            writeLoadingIndicatorMorphFrame(frameScene, 0, 0.0, 0.0);
            Bounds startBounds = frameScene.indicator.getBoundsInLocal();

            writeLoadingIndicatorMorphFrame(frameScene, 0, 0.5, 0.0);
            Bounds middleBounds = frameScene.indicator.getBoundsInLocal();
            WritableImage middleFrame = snapshotImageOnFxThread(frameScene.root);
            Point2D middleCentroid = contrastingPixelCentroid(
                    middleFrame,
                    frameScene.indicator,
                    Color.WHITE,
                    0.04
            );

            writeLoadingIndicatorMorphFrame(frameScene, 0, 1.0, 0.0);
            Bounds endBounds = frameScene.indicator.getBoundsInLocal();

            assertEquals(LOADING_INDICATOR_TEST_CENTER, middleCentroid.getX(), 2.5,
                    () -> "loading indicator scaled midpoint is horizontally unstable: centroid="
                            + middleCentroid);
            assertEquals(LOADING_INDICATOR_TEST_CENTER, middleCentroid.getY(), 2.5,
                    () -> "loading indicator scaled midpoint is vertically unstable: centroid="
                            + middleCentroid);
            assertTrue(middleBounds.getWidth() > Math.max(startBounds.getWidth(), endBounds.getWidth()) * 1.04,
                    () -> "start=" + startBounds + ", middle=" + middleBounds + ", end=" + endBounds);
            assertTrue(middleBounds.getHeight() > Math.max(startBounds.getHeight(), endBounds.getHeight()) * 1.04,
                    () -> "start=" + startBounds + ", middle=" + middleBounds + ", end=" + endBounds);
        });
    }

    /// Verifies that settled morph segments keep rotating through the global rotation phase.
    @Test
    void loadingIndicatorIndeterminateMorphTargetKeepsRotating() {
        FxTestUtils.runOnFxThread(() -> {
            LoadingIndicatorFrameScene frameScene = new LoadingIndicatorFrameScene();
            writeLoadingIndicatorMorphFrame(frameScene, 0, 1.0, 0.0);
            Point2D initialPoint = firstPathPoint(frameScene.indicator);

            writeLoadingIndicatorMorphFrame(frameScene, 0, 1.0, 0.03);
            Point2D rotatedPoint = firstPathPoint(frameScene.indicator);

            assertTrue(initialPoint.distance(rotatedPoint) > 4.0,
                    () -> "initialPoint=" + initialPoint + ", rotatedPoint=" + rotatedPoint);
        });
    }

    /// Verifies that disabled full motion keeps a basic loading indicator rotation without morphing.
    @Tier2Test
    @Test
    void loadingIndicatorUsesBasicRotationWhenAnimationsDisabled() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> loadingIndicatorMotionAdvanced(sceneReference, sampleReference),
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(false);
                        sceneReference.set(scene);
                        sampleReference.set(sampleIndeterminateProgressMotion(scene));
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        assertTrue(M3MotionSettings.shouldReduceMotion(scene.loadingIndicator));
                        assertLoadingIndicatorMotionAdvanced(scene, sampleReference, "reduced motion");
                        assertLoadingIndicatorReducedMotionKeepsStableInk(scene, sampleReference);
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that the contained loading indicator aligns the active shape with its container.
    @Test
    void containedLoadingIndicatorCentersShapeInContainer() {
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        loadingIndicator.setVariant(M3LoadingIndicatorVariant.CONTAINED);
        loadingIndicator.setStyle("-m3-container-size: 72px; -m3-indicator-size: 54px;");
        Pane root = new Pane(loadingIndicator);
        Scene scene = new Scene(root, 120.0, 90.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        loadingIndicator.resize(72.0, 72.0);
        loadingIndicator.layout();

        Region container = assertInstanceOf(
                Region.class,
                loadingIndicator.lookup(".m3-loading-indicator-container")
        );
        Path indicator = assertInstanceOf(
                Path.class,
                loadingIndicator.lookup(".m3-loading-indicator-indicator")
        );
        Bounds containerBounds = container.localToScene(container.getBoundsInLocal());
        Bounds indicatorBounds = indicator.localToScene(indicator.getBoundsInLocal());
        assertEquals(containerBounds.getCenterX(), indicatorBounds.getCenterX(), 0.75);
        assertEquals(containerBounds.getCenterY(), indicatorBounds.getCenterY(), 0.75);
        assertTrue(containerBounds.contains(indicatorBounds));
    }

    /// Verifies that the progress bar skin lays out determinate progress without Modena internals.
    @Test
    void progressBarSkinLaysOutDeterminateProgress() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 240.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressBar.resize(200.0, 16.0);
        progressBar.layout();

        Rectangle track = progressBarPrimaryTrack(progressBar);
        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
        javafx.scene.shape.Circle stop = assertInstanceOf(
                javafx.scene.shape.Circle.class,
                lookupShape(progressBar, ".m3-progress-stop")
        );
        assertEquals(106.0, track.getX(), 0.0001);
        assertEquals(84.0, track.getWidth(), 0.0001);
        assertEquals(100.0, bar.getWidth(), 0.0001);
        assertEquals(4.0, track.getArcWidth(), 0.0001);
        assertEquals(4.0, track.getArcHeight(), 0.0001);
        assertEquals(4.0, bar.getArcWidth(), 0.0001);
        assertEquals(4.0, bar.getArcHeight(), 0.0001);
        assertEquals(6.0, track.getX() - bar.getBoundsInParent().getMaxX(), 0.0001);
        assertEquals(198.0, stop.getCenterX(), 0.0001);
        assertEquals(2.0, stop.getRadius(), 0.0001);
    }

    /// Verifies that linear progress bars mirror their physical fill direction in right-to-left layouts.
    @Test
    void progressBarSkinMirrorsLinearProgressForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3ProgressBar determinateBar = new M3ProgressBar(0.25);
            determinateBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3ProgressBar indeterminateBar = new M3ProgressBar();
            indeterminateBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3ProgressBar wavyBar = new M3ProgressBar(0.5);
            wavyBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            wavyBar.setStyle("-m3-wave-amplitude: 3px; "
                    + "-m3-wavelength: 40px; "
                    + "-m3-track-gap: 4px; "
                    + "-m3-stop-size: 4px;");
            Pane root = new Pane(determinateBar, indeterminateBar, wavyBar);
            Scene scene = new Scene(root, 280.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            determinateBar.resize(200.0, 16.0);
            indeterminateBar.resize(200.0, 16.0);
            wavyBar.resize(240.0, 16.0);
            determinateBar.layout();
            indeterminateBar.layout();
            wavyBar.layout();

            Rectangle determinateIndicator = (Rectangle) lookupShape(determinateBar, ".bar");
            assertEquals(50.0, determinateIndicator.getWidth(), 0.0001);
            assertEquals(0.0, determinateIndicator.getX(), 0.0001);
            Bounds determinateBounds = determinateBar.localToScene(determinateBar.getBoundsInLocal());
            Bounds determinateIndicatorBounds = determinateIndicator.localToScene(determinateIndicator.getBoundsInLocal());
            assertEquals(determinateBounds.getMaxX(), determinateIndicatorBounds.getMaxX(), 0.0001);

            Rectangle indeterminateIndicator = (Rectangle) lookupShape(indeterminateBar, ".bar");
            double indeterminateWidth = indeterminateIndicator.getWidth();
            double leftToRightX = indeterminateIndicator.getX();
            assertTrue(indeterminateWidth > 0.0);
            assertTrue(indeterminateWidth <= 200.0);
            assertTrue(leftToRightX >= 0.0);
            assertTrue(leftToRightX + indeterminateWidth <= 200.0);
            Bounds indeterminateBounds = indeterminateBar.localToScene(indeterminateBar.getBoundsInLocal());
            Bounds indeterminateIndicatorBounds = indeterminateIndicator.localToScene(indeterminateIndicator.getBoundsInLocal());
            assertEquals(
                    indeterminateBounds.getMinX() + 200.0 - (leftToRightX + indeterminateWidth),
                    indeterminateIndicatorBounds.getMinX(),
                    0.0001
            );

            Path wave = assertInstanceOf(Path.class, lookupShape(wavyBar, ".m3-progress-bar-wave"));
            Point2D waveStart = firstPathPoint(wave);
            Point2D waveEnd = lastPathPoint(wave);
            assertEquals(0.0, waveStart.getX(), 0.0001);
            assertEquals(120.0, waveEnd.getX(), 0.0001);
            Bounds wavyBounds = wavyBar.localToScene(wavyBar.getBoundsInLocal());
            Bounds waveBounds = wave.localToScene(wave.getBoundsInLocal());
            assertEquals(wavyBounds.getMaxX(), waveBounds.getMaxX(), wavyBar.getTrackThickness());

            Rectangle track = progressBarPrimaryTrack(wavyBar);
            javafx.scene.shape.Circle stop = assertInstanceOf(
                    javafx.scene.shape.Circle.class,
                    lookupShape(wavyBar, ".m3-progress-stop")
            );
            assertEquals(126.0, track.getX(), 0.0001);
            assertEquals(104.0, track.getWidth(), 0.0001);
            assertEquals(238.0, stop.getCenterX(), 0.0001);
            assertEquals(2.0, stop.getRadius(), 0.0001);
            Bounds trackBounds = track.localToScene(track.getBoundsInLocal());
            Bounds stopBounds = stop.localToScene(stop.getBoundsInLocal());
            assertEquals(wavyBounds.getMinX() + 10.0, trackBounds.getMinX(), 0.0001);
            assertEquals(wavyBounds.getMinX(), stopBounds.getMinX(), 0.0001);
        });
    }

    /// Verifies that inherited runtime direction changes relayout a previously laid-out progress bar.
    @Test
    void progressBarSkinRelayoutsWhenInheritedDirectionChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3ProgressBar progressBar = new M3ProgressBar(0.25);
            Pane root = new Pane(progressBar);
            Scene scene = new Scene(root, 240.0, 40.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            progressBar.resize(200.0, 16.0);
            progressBar.layout();
            Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, progressBar.getEffectiveNodeOrientation());
            assertEquals(0.0, bar.getX(), 0.0001);
            Bounds controlBounds = progressBar.localToScene(progressBar.getBoundsInLocal());
            Bounds barBounds = bar.localToScene(bar.getBoundsInLocal());
            assertEquals(controlBounds.getMinX(), barBounds.getMinX(), 0.0001);

            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, progressBar.getEffectiveNodeOrientation());
            controlBounds = progressBar.localToScene(progressBar.getBoundsInLocal());
            barBounds = bar.localToScene(bar.getBoundsInLocal());
            assertEquals(0.0, bar.getX(), 0.0001);
            assertEquals(controlBounds.getMaxX(), barBounds.getMaxX(), 0.0001);

            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, progressBar.getEffectiveNodeOrientation());
            controlBounds = progressBar.localToScene(progressBar.getBoundsInLocal());
            barBounds = bar.localToScene(bar.getBoundsInLocal());
            assertEquals(0.0, bar.getX(), 0.0001);
            assertEquals(controlBounds.getMinX(), barBounds.getMinX(), 0.0001);
        });
    }

    /// Verifies that progress bars mirror when attached to an already mirrored parent after detached layout.
    @Test
    void progressBarSkinRelayoutsWhenAttachedToRightToLeftParent() {
        FxTestUtils.runOnFxThread(() -> {
            M3ProgressBar progressBar = new M3ProgressBar(0.25);
            Pane detachedRoot = new Pane(progressBar);
            Scene detachedScene = new Scene(detachedRoot, 240.0, 40.0);
            M3ThemeManager.install(detachedScene, M3Theme.defaultTheme());
            detachedRoot.applyCss();
            progressBar.resize(200.0, 16.0);
            progressBar.layout();
            Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, progressBar.getEffectiveNodeOrientation());
            assertEquals(0.0, bar.getX(), 0.0001);

            Pane rightToLeftRoot = new Pane();
            rightToLeftRoot.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            Scene rightToLeftScene = new Scene(rightToLeftRoot, 240.0, 40.0);
            M3ThemeManager.install(rightToLeftScene, M3Theme.defaultTheme());
            detachedRoot.getChildren().clear();
            rightToLeftRoot.getChildren().add(progressBar);
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, progressBar.getEffectiveNodeOrientation());
            assertEquals(0.0, bar.getX(), 0.0001);
            Bounds controlBounds = progressBar.localToScene(progressBar.getBoundsInLocal());
            Bounds barBounds = bar.localToScene(bar.getBoundsInLocal());
            assertEquals(controlBounds.getMaxX(), barBounds.getMaxX(), 0.0001);
        });
    }

    /// Verifies that the progress bar skin clips indeterminate progress inside the track.
    @Test
    void progressBarSkinClipsIndeterminateSegment() {
        M3ProgressBar progressBar = new M3ProgressBar();
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 240.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressBar.resize(200.0, 16.0);
        progressBar.layout();

        Pane container = assertInstanceOf(Pane.class, progressBar.lookup(".m3-progress-bar-container"));
        Rectangle clip = assertInstanceOf(Rectangle.class, container.getClip());
        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
        assertEquals(200.0, clip.getWidth(), 0.0001);
        assertEquals(4.0, clip.getHeight(), 0.0001);
        assertEquals(4.0, clip.getArcWidth(), 0.0001);
        assertEquals(4.0, clip.getArcHeight(), 0.0001);
        assertTrue(bar.getWidth() >= 24.0);
        assertTrue(bar.getBoundsInParent().getMaxX() > 0.0);
    }

    /// Verifies that determinate progress bar value changes are animated.
    @Tier2Test
    @Test
    void progressBarSkinAnimatesDeterminateProgressChanges() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ProgressBar> progressBarReference = new AtomicReference<>();
        AtomicReference<@Nullable Rectangle> barReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> progressBarWidthSettled(progressBarReference, barReference, 180.0),
                    () -> {
                        M3ProgressBar progressBar = new M3ProgressBar(0.1);
                        progressBar.setManaged(false);
                        Pane root = new Pane(progressBar);
                        Scene scene = new Scene(root, 240.0, 40.0);
                        Stage stage = new Stage();

                        stageReference.set(stage);
                        rootReference.set(root);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        progressBar.resize(200.0, 16.0);
                        progressBar.layout();

                        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
                        assertEquals(20.0, bar.getWidth(), 0.0001);

                        progressBarReference.set(progressBar);
                        barReference.set(bar);
                        progressBar.setProgress(0.9);
                        progressBar.layout();

                        assertTrue(bar.getWidth() < 180.0);
                    },
                    () -> {
                        M3ProgressBar progressBar =
                                Objects.requireNonNull(progressBarReference.get(), "progressBar");
                        Rectangle bar = Objects.requireNonNull(barReference.get(), "bar");
                        progressBar.layout();

                        assertEquals(180.0, bar.getWidth(), 0.0001);
                    }
            );
        } finally {
            closeMotionTestScene(stageReference, rootReference);
        }
    }

    /// Verifies that indeterminate progress bar segments move over time.
    @Tier2Test
    @Test
    void progressBarSkinAnimatesIndeterminateProgress() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ProgressBar> progressBarReference = new AtomicReference<>();
        AtomicReference<@Nullable Rectangle> barReference = new AtomicReference<>();
        AtomicReference<@Nullable Rectangle> secondaryBarReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> initialX = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3ProgressBar progressBar = progressBarReference.get();
                        @Nullable Rectangle bar = barReference.get();
                        @Nullable Rectangle secondaryBar = secondaryBarReference.get();
                        @Nullable Double x = initialX.get();
                        if (progressBar == null || bar == null || secondaryBar == null || x == null) {
                            return false;
                        }

                        progressBar.layout();
                        return Math.abs(bar.getX() - x) > 0.1
                                && bar.isVisible()
                                && secondaryBar.isVisible()
                                && secondaryBar.getWidth() > 0.0;
                    },
                    () -> {
                        M3ProgressBar progressBar = new M3ProgressBar();
                        progressBar.setManaged(false);
                        Pane root = new Pane(progressBar);
                        Scene scene = new Scene(root, 240.0, 40.0);
                        Stage stage = new Stage();

                        stageReference.set(stage);
                        rootReference.set(root);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        progressBar.resize(200.0, 16.0);
                        progressBar.layout();

                        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
                        progressBarReference.set(progressBar);
                        barReference.set(bar);
                        secondaryBarReference.set((Rectangle) lookupShape(
                                progressBar,
                                ".m3-progress-bar-secondary-bar"
                        ));
                        initialX.set(bar.getX());
                    },
                    () -> {
                        M3ProgressBar progressBar =
                                Objects.requireNonNull(progressBarReference.get(), "progressBar");
                        Rectangle bar = Objects.requireNonNull(barReference.get(), "bar");
                        Rectangle secondaryBar = Objects.requireNonNull(
                                secondaryBarReference.get(),
                                "secondaryBar"
                        );
                        double x = Objects.requireNonNull(initialX.get(), "initialX");
                        progressBar.layout();

                        assertTrue(Math.abs(bar.getX() - x) > 0.1);
                        assertTrue(bar.isVisible());
                        assertTrue(secondaryBar.isVisible());
                        assertTrue(secondaryBar.getWidth() > 0.0);
                    }
            );
        } finally {
            closeMotionTestScene(stageReference, rootReference);
        }
    }

    /// Verifies that disabled full motion keeps a basic indeterminate progress bar loop.
    @Tier2Test
    @Test
    void progressBarUsesBasicIndeterminateLoopWhenAnimationsDisabled() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> progressBarMotionAdvanced(sceneReference, sampleReference),
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(false);
                        sceneReference.set(scene);
                        sampleReference.set(sampleIndeterminateProgressMotion(scene));
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        assertTrue(M3MotionSettings.shouldReduceMotion(scene.progressBar));
                        assertProgressBarMotionAdvanced(scene, sampleReference, "reduced motion");
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that an explicitly wavy progress bar renders a separated active path, track, and stop indicator.
    @Test
    void configuredWavyProgressBarSkinDrawsIndicatorAndStop() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        progressBar.setWaveAmplitude(3.0);
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 280.0, 60.0);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();
        progressBar.resize(240.0, 16.0);
        progressBar.layout();

        Rectangle bar = (Rectangle) lookupShape(progressBar, ".bar");
        Path wave = assertInstanceOf(Path.class, lookupShape(progressBar, ".m3-progress-bar-wave"));
        Rectangle track = (Rectangle) lookupShape(progressBar, ".track");
        javafx.scene.shape.Circle stop =
                assertInstanceOf(javafx.scene.shape.Circle.class, lookupShape(progressBar, ".m3-progress-stop"));

        assertFalse(bar.isVisible());
        assertTrue(wave.isVisible());
        assertTrue(wave.getElements().size() > 8);
        assertTrue(wave.getBoundsInLocal().getHeight() > progressBar.getTrackThickness());
        assertTrue(track.getX() > 120.0);
        assertTrue(stop.isVisible());
        assertEquals(2.0, stop.getRadius(), 0.0001);
    }

    /// Verifies that expressive indeterminate progress bars keep the track outside the moving wave.
    @Tier2Test
    @Test
    void expressiveProgressBarSkinSeparatesIndeterminateWaveFromTrack() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ProgressBar> progressBarReference = new AtomicReference<>();
        AtomicReference<@Nullable Path> waveReference = new AtomicReference<>();
        AtomicReference<@Nullable Rectangle> leadingTrackReference = new AtomicReference<>();
        AtomicReference<@Nullable Rectangle> trailingTrackReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> expressiveProgressBarIndeterminateWaveSeparated(
                            progressBarReference,
                            waveReference,
                            leadingTrackReference,
                            trailingTrackReference
                    ),
                    2,
                    () -> {
                        M3ProgressBar progressBar = new M3ProgressBar();
                        progressBar.setStyle("-m3-wave-amplitude: 3px; "
                                + "-m3-wavelength: 40px; "
                                + "-m3-track-gap: 4px; "
                                + "-m3-stop-size: 4px;");
                        progressBar.setManaged(false);
                        Pane root = new Pane(progressBar);
                        Scene scene = new Scene(root, 280.0, 60.0);
                        Stage stage = new Stage();

                        FxTestUtils.setCssAnimationsEnabled(root, true);
                        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                        FxTestUtils.setMotionBehavior(root, observableIndeterminateProgressBehavior());
                        M3ThemeManager.install(scene, M3Theme.fromSeed(
                                Color.web("#006a6a"),
                                M3Profile.EXPRESSIVE_2025,
                                Brightness.LIGHT
                        ));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.resize(280.0, 60.0);
                        progressBar.resizeRelocate(20.0, 22.0, 240.0, 16.0);
                        root.layout();
                        progressBar.layout();

                        stageReference.set(stage);
                        rootReference.set(root);
                        progressBarReference.set(progressBar);
                        waveReference.set(assertInstanceOf(
                                Path.class,
                                lookupShape(progressBar, ".m3-progress-bar-wave")
                        ));
                        leadingTrackReference.set(progressBarPrimaryTrack(progressBar));
                        trailingTrackReference.set((Rectangle) lookupShape(
                                progressBar,
                                ".m3-progress-bar-secondary-track"
                        ));
                    },
                    () -> assertExpressiveProgressBarIndeterminateWaveSeparated(
                            Objects.requireNonNull(progressBarReference.get(), "progressBar"),
                            Objects.requireNonNull(waveReference.get(), "wave"),
                            Objects.requireNonNull(leadingTrackReference.get(), "leadingTrack"),
                            Objects.requireNonNull(trailingTrackReference.get(), "trailingTrack")
                    )
            );
        } finally {
            closeExpressiveProgressBarIndeterminateWaveScene(stageReference, rootReference);
        }
    }

    /// Verifies that expressive progress animations update sampled paths without rebuilding elements.
    @Tier2Test
    @Test
    void expressiveProgressWavePathsReuseElementsDuringAnimation() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ProgressBar> progressBarReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ProgressIndicator> progressIndicatorReference = new AtomicReference<>();
        AtomicReference<@Nullable Path> barWaveReference = new AtomicReference<>();
        AtomicReference<@Nullable Path> circularTrackReference = new AtomicReference<>();
        AtomicReference<@Nullable Path> circularWaveReference = new AtomicReference<>();
        AtomicReference<@Nullable List<PathElement>> barElementsReference = new AtomicReference<>();
        AtomicReference<@Nullable List<PathElement>> circularTrackElementsReference = new AtomicReference<>();
        AtomicReference<@Nullable List<PathElement>> circularElementsReference = new AtomicReference<>();
        AtomicReference<@Nullable Point2D> initialBarPointReference = new AtomicReference<>();
        AtomicReference<@Nullable Point2D> initialCircularPointReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> expressiveProgressWavePathsMovedWithReusedElements(
                            rootReference,
                            progressBarReference,
                            progressIndicatorReference,
                            barWaveReference,
                            circularTrackReference,
                            circularWaveReference,
                            barElementsReference,
                            circularTrackElementsReference,
                            circularElementsReference,
                            initialBarPointReference,
                            initialCircularPointReference
                    ),
                    2,
                    () -> {
                        M3ProgressBar progressBar = new M3ProgressBar();
                        progressBar.setStyle("-m3-wave-amplitude: 3px; "
                                + "-m3-wavelength: 40px; "
                                + "-m3-track-gap: 4px; "
                                + "-m3-stop-size: 4px;");
                        progressBar.setManaged(false);

                        M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
                        progressIndicator.setStyle("-m3-track-thickness: 4px; "
                                + "-m3-indicator-size: 48px; "
                                + "-m3-wave-amplitude: 1.6px; "
                                + "-m3-wavelength: 15px; "
                                + "-m3-track-gap: 4px;");
                        progressIndicator.setManaged(false);

                        Pane root = new Pane(progressBar, progressIndicator);
                        Scene scene = new Scene(root, 320.0, 120.0);
                        Stage stage = new Stage();

                        FxTestUtils.setCssAnimationsEnabled(root, true);
                        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                        FxTestUtils.setMotionBehavior(root, observableIndeterminateProgressBehavior());
                        M3ThemeManager.install(scene, M3Theme.fromSeed(
                                Color.web("#006a6a"),
                                M3Profile.EXPRESSIVE_2025,
                                Brightness.LIGHT
                        ));
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.resize(320.0, 120.0);
                        progressBar.resizeRelocate(24.0, 28.0, 240.0, 16.0);
                        progressIndicator.resizeRelocate(24.0, 56.0, 64.0, 64.0);
                        root.layout();
                        progressBar.layout();
                        progressIndicator.layout();

                        Path barWave = assertInstanceOf(
                                Path.class,
                                lookupShape(progressBar, ".m3-progress-bar-wave")
                        );
                        Path circularTrack = assertInstanceOf(
                                Path.class,
                                lookupShape(progressIndicator, ".m3-progress-indicator-track-wave")
                        );
                        Path circularWave = assertInstanceOf(
                                Path.class,
                                lookupShape(progressIndicator, ".m3-progress-indicator-wave")
                        );

                        stageReference.set(stage);
                        rootReference.set(root);
                        progressBarReference.set(progressBar);
                        progressIndicatorReference.set(progressIndicator);
                        barWaveReference.set(barWave);
                        circularTrackReference.set(circularTrack);
                        circularWaveReference.set(circularWave);
                        barElementsReference.set(List.copyOf(barWave.getElements()));
                        circularTrackElementsReference.set(List.copyOf(circularTrack.getElements()));
                        circularElementsReference.set(List.copyOf(circularWave.getElements()));
                        initialBarPointReference.set(firstPathPoint(barWave));
                        initialCircularPointReference.set(firstPathPoint(circularWave));
                    },
                    () -> {
                        Path barWave = Objects.requireNonNull(barWaveReference.get(), "barWave");
                        Path circularTrack = Objects.requireNonNull(circularTrackReference.get(), "circularTrack");
                        Path circularWave = Objects.requireNonNull(circularWaveReference.get(), "circularWave");
                        List<PathElement> barElements = Objects.requireNonNull(
                                barElementsReference.get(),
                                "barElements"
                        );
                        List<PathElement> circularTrackElements = Objects.requireNonNull(
                                circularTrackElementsReference.get(),
                                "circularTrackElements"
                        );
                        List<PathElement> circularElements = Objects.requireNonNull(
                                circularElementsReference.get(),
                                "circularElements"
                        );
                        Point2D initialBarPoint = Objects.requireNonNull(
                                initialBarPointReference.get(),
                                "initialBarPoint"
                        );
                        Point2D initialCircularPoint = Objects.requireNonNull(
                                initialCircularPointReference.get(),
                                "initialCircularPoint"
                        );

                        assertSamePathElements(barWave, barElements, "linear expressive wave");
                        assertSamePathElements(circularTrack, circularTrackElements, "circular expressive track");
                        assertSamePathElements(circularWave, circularElements, "circular expressive wave");
                        assertTrue(firstPathPoint(barWave).distance(initialBarPoint) > 0.1,
                                () -> "linear wave did not advance: initial=" + initialBarPoint
                                        + ", current=" + firstPathPoint(barWave));
                        assertTrue(firstPathPoint(circularWave).distance(initialCircularPoint) > 0.1,
                                () -> "circular wave did not advance: initial=" + initialCircularPoint
                                        + ", current=" + firstPathPoint(circularWave));
                    }
            );
        } finally {
            closeExpressiveProgressBarIndeterminateWaveScene(stageReference, rootReference);
        }
    }

    /// Verifies that progress bar subnodes keep Material colors.
    @Test
    void progressBarStateStylesPreserveMaterialColors() {
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        Pane root = new Pane(progressBar);
        Scene scene = new Scene(root, 240.0, 40.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        progressBar.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        root.applyCss();

        Shape track = progressBarPrimaryTrack(progressBar);
        Shape bar = lookupShape(progressBar, ".bar");
        assertEquals(Color.rgb(7, 8, 9), track.getFill());
        assertTrue(isTransparent(track.getStroke()));
        assertEquals(Color.rgb(1, 2, 3), bar.getFill());
        assertTrue(isTransparent(bar.getStroke()));
    }

    /// Verifies that progress indicator subnodes keep Material colors and determinate geometry.
    @Test
    void progressIndicatorStateStylesPreserveMaterialColors() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.25);
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 80.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        progressIndicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        root.applyCss();
        progressIndicator.resize(48.0, 48.0);
        progressIndicator.layout();

        Shape track = lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        assertEquals(Color.rgb(7, 8, 9), track.getStroke());
        assertEquals(Color.rgb(1, 2, 3), indicator.getStroke());
        assertEquals(4.0, track.getStrokeWidth(), 0.0001);
        assertEquals(4.0, indicator.getStrokeWidth(), 0.0001);
        assertEquals(-90.0, indicator.getLength(), 0.0001);
    }

    /// Verifies that circular progress indicators can render at an explicitly allocated size.
    @Test
    void progressIndicatorSkinUsesAllocatedSize() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 80.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressIndicator.resize(64.0, 64.0);
        progressIndicator.layout();

        Arc track = (Arc) lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        assertEquals(30.0, track.getRadiusX(), 0.0001);
        assertEquals(30.0, track.getRadiusY(), 0.0001);
        assertEquals(30.0, indicator.getRadiusX(), 0.0001);
        assertEquals(30.0, indicator.getRadiusY(), 0.0001);
    }

    /// Verifies that indeterminate circular progress leaves a gapped inactive track around the active arc.
    @Test
    void progressIndicatorSkinShowsGappedTrackWhenIndeterminate() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 80.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        progressIndicator.resize(48.0, 48.0);
        progressIndicator.layout();

        Shape track = lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        Arc trackArc = assertInstanceOf(Arc.class, track);
        assertTrue(trackArc.isVisible());
        assertTrue(trackArc.getLength() < 0.0);
        assertTrue(Math.abs(trackArc.getLength()) < 360.0);
        assertTrue(indicator.getLength() < 0.0);
    }

    /// Verifies that determinate circular progress value changes are animated.
    @Tier2Test
    @Test
    void progressIndicatorSkinAnimatesDeterminateProgressChanges() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ProgressIndicator> progressIndicatorReference = new AtomicReference<>();
        AtomicReference<@Nullable Arc> indicatorReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> progressIndicatorLengthSettled(progressIndicatorReference, indicatorReference, -324.0),
                    () -> {
                        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.1);
                        progressIndicator.setManaged(false);
                        Pane root = new Pane(progressIndicator);
                        Scene scene = new Scene(root, 80.0, 80.0);
                        Stage stage = new Stage();

                        stageReference.set(stage);
                        rootReference.set(root);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        progressIndicator.resize(48.0, 48.0);
                        progressIndicator.layout();

                        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
                        assertEquals(-36.0, indicator.getLength(), 0.0001);

                        progressIndicatorReference.set(progressIndicator);
                        indicatorReference.set(indicator);
                        progressIndicator.setProgress(0.9);
                        progressIndicator.layout();

                        assertTrue(indicator.getLength() > -324.0);
                    },
                    () -> {
                        M3ProgressIndicator progressIndicator =
                                Objects.requireNonNull(progressIndicatorReference.get(), "progressIndicator");
                        Arc indicator = Objects.requireNonNull(indicatorReference.get(), "indicator");
                        progressIndicator.layout();

                        assertEquals(-324.0, indicator.getLength(), 0.0001);
                    }
            );
        } finally {
            closeMotionTestScene(stageReference, rootReference);
        }
    }

    /// Verifies that indeterminate circular progress rotates without oversized sweeps.
    @Tier2Test
    @Test
    void progressIndicatorSkinAnimatesIndeterminateProgress() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3ProgressIndicator> progressIndicatorReference = new AtomicReference<>();
        AtomicReference<@Nullable Arc> indicatorReference = new AtomicReference<>();
        AtomicReference<@Nullable Double> initialStartAngle = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable M3ProgressIndicator progressIndicator = progressIndicatorReference.get();
                        @Nullable Arc indicator = indicatorReference.get();
                        @Nullable Double startAngle = initialStartAngle.get();
                        if (progressIndicator == null || indicator == null || startAngle == null) {
                            return false;
                        }

                        progressIndicator.layout();
                        return Math.abs(indicator.getStartAngle() - startAngle) > 0.1;
                    },
                    () -> {
                        M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
                        progressIndicator.setManaged(false);
                        Pane root = new Pane(progressIndicator);
                        Scene scene = new Scene(root, 80.0, 80.0);
                        Stage stage = new Stage();

                        stageReference.set(stage);
                        rootReference.set(root);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        progressIndicator.resize(48.0, 48.0);
                        progressIndicator.layout();

                        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
                        progressIndicatorReference.set(progressIndicator);
                        indicatorReference.set(indicator);
                        initialStartAngle.set(indicator.getStartAngle());
                    },
                    () -> {
                        M3ProgressIndicator progressIndicator =
                                Objects.requireNonNull(progressIndicatorReference.get(), "progressIndicator");
                        Arc indicator = Objects.requireNonNull(indicatorReference.get(), "indicator");
                        double startAngle = Objects.requireNonNull(initialStartAngle.get(), "initialStartAngle");
                        progressIndicator.layout();

                        assertTrue(Math.abs(indicator.getStartAngle() - startAngle) > 0.1);
                        assertTrue(indicator.getLength() <= -36.0);
                        assertTrue(indicator.getLength() >= -313.2);
                    }
            );
        } finally {
            closeMotionTestScene(stageReference, rootReference);
        }
    }

    /// Verifies that disabled full motion keeps a basic indeterminate circular progress loop.
    @Tier2Test
    @Test
    void progressIndicatorUsesBasicIndeterminateLoopWhenAnimationsDisabled() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> progressIndicatorMotionAdvanced(sceneReference, sampleReference),
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(false);
                        sceneReference.set(scene);
                        sampleReference.set(sampleIndeterminateProgressMotion(scene));
                    },
                    () -> {
                        IndeterminateProgressMotionScene scene =
                                Objects.requireNonNull(sceneReference.get(), "scene");
                        assertTrue(M3MotionSettings.shouldReduceMotion(scene.progressIndicator));
                        assertProgressIndicatorMotionAdvanced(scene, sampleReference, "reduced motion");
                        assertEquals(-72.0, scene.progressIndicatorArc.getLength(), 0.0001);
                    }
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that indeterminate circular progress uses a seamless phase cycle.
    @Tier2Test
    @Test
    void progressIndicatorIndeterminateCycleHasNoPhaseJump() throws InterruptedException {
        AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference = new AtomicReference<>();
        ProgressIndicatorCycleObservation observation = new ProgressIndicatorCycleObservation();

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> progressIndicatorObservedSeamlessCycle(sceneReference, observation),
                    PROGRESS_INDICATOR_CYCLE_STABLE_PULSES,
                    () -> {
                        IndeterminateProgressMotionScene scene = showIndeterminateProgressMotionScene(true);
                        sceneReference.set(scene);
                        progressIndicatorObservedSeamlessCycle(sceneReference, observation);
                    },
                    () -> assertProgressIndicatorCycleObservation(observation)
            );
        } finally {
            closeIndeterminateProgressMotionScene(sceneReference);
        }
    }

    /// Verifies that explicitly wavy circular progress uses paths instead of the Flat arc geometry.
    @Test
    void configuredWavyProgressIndicatorSkinDrawsIndicatorAndTrack() {
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        progressIndicator.setWaveAmplitude(1.6);
        Pane root = new Pane(progressIndicator);
        Scene scene = new Scene(root, 96.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        ));
        root.applyCss();
        double indicatorSize = progressIndicator.prefWidth(-1.0);
        assertEquals(48.0, indicatorSize, 0.0001);
        progressIndicator.resize(indicatorSize, indicatorSize);
        progressIndicator.layout();

        Arc track = (Arc) lookupShape(progressIndicator, ".track");
        Arc indicator = (Arc) lookupShape(progressIndicator, ".indicator");
        Path waveTrack = assertInstanceOf(
                Path.class,
                lookupShape(progressIndicator, ".m3-progress-indicator-track-wave")
        );
        Path waveIndicator = assertInstanceOf(
                Path.class,
                lookupShape(progressIndicator, ".m3-progress-indicator-wave")
        );

        assertFalse(track.isVisible());
        assertFalse(indicator.isVisible());
        assertTrue(waveTrack.isVisible());
        assertTrue(waveIndicator.isVisible());
        assertTrue(waveTrack.getElements().size() > 8);
        assertTrue(waveIndicator.getElements().size() > 8);
        assertTrue(waveIndicator.getBoundsInLocal().getHeight() > 0.0);

        Point2D activeStart = firstPathPoint(waveIndicator);
        Point2D activeEnd = lastPathPoint(waveIndicator);
        Point2D trackStart = firstPathPoint(waveTrack);
        Point2D trackEnd = lastPathPoint(waveTrack);
        double minimumCenterlineGap = progressIndicator.getTrackGap()
                + progressIndicator.getTrackThickness() / 2.0;
        assertTrue(activeEnd.distance(trackStart) > minimumCenterlineGap,
                () -> "activeEnd=" + activeEnd + ", trackStart=" + trackStart);
        assertTrue(activeStart.distance(trackEnd) > minimumCenterlineGap,
                () -> "activeStart=" + activeStart + ", trackEnd=" + trackEnd);
    }

    /// Verifies that determinate progress snapshots show separated fill, track, and rounded caps.
    @Tier2Test
    @Test
    void progressBarSnapshotRendersFillTrackAndRoundedCaps() {
        FxTestUtils.runOnFxThread(() -> {
            M3ProgressBar progressBar = new M3ProgressBar(0.5);
            progressBar.setPrefSize(200.0, 32.0);
            progressBar.setStyle("-m3-track-thickness: 8px; -m3-track-shape: 999px;");
            FlowPane root = new FlowPane(progressBar);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 240.0, 48.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(240.0, 48.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            Shape trackShape = lookupShape(progressBar, ".track");
            Shape barShape = lookupShape(progressBar, ".bar");
            var trackBounds = trackShape.localToScene(trackShape.getBoundsInLocal());
            var barBounds = barShape.localToScene(barShape.getBoundsInLocal());
            int trackCenterY = (int) Math.round((trackBounds.getMinY() + trackBounds.getMaxY()) / 2.0);
            int fillX = (int) Math.round((barBounds.getMinX() + barBounds.getMaxX()) / 2.0);
            int trackX = (int) Math.round(trackBounds.getMaxX() - 12.0);
            Color fill = averageSnapshotColor(
                    image, fillX - 4, trackCenterY - 2, fillX + 5, trackCenterY + 3
            );
            Color track = averageSnapshotColor(
                    image, trackX - 4, trackCenterY - 2, trackX + 5, trackCenterY + 3
            );
            Color roundedCorner = averageSnapshotColor(
                    image,
                    (int) Math.round(barBounds.getMinX()) - 1,
                    (int) Math.round(barBounds.getMinY()) - 1,
                    (int) Math.round(barBounds.getMinX()) + 4,
                    (int) Math.round(barBounds.getMinY()) + 4
            );

            assertTrue(fill.getOpacity() > 0.8, () -> "fill=" + fill);
            assertTrue(track.getOpacity() > 0.8, () -> "track=" + track);
            assertTrue(colorDistance(fill, track) > 0.1, () -> "fill=" + fill + ", track=" + track);
            assertTrue(roundedCorner.getOpacity() < 0.4
                            || colorDistance(fill, roundedCorner) > 0.1,
                    () -> "fill=" + fill + ", roundedCorner=" + roundedCorner);
        });
    }

    /// Verifies that circular progress snapshots show distinct track and indicator arc pixels.
    @Tier2Test
    @Test
    void progressIndicatorSnapshotRendersTrackAndArcPixels() {
        FxTestUtils.runOnFxThread(() -> {
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
            progressIndicator.setPrefSize(72.0, 72.0);
            progressIndicator.setStyle("-m3-track-thickness: 8px; -m3-indicator-size: 72px;");
            FlowPane root = new FlowPane(progressIndicator);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 104.0, 96.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(104.0, 96.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            Arc arc = (Arc) lookupShape(progressIndicator, ".indicator");
            Arc track = (Arc) lookupShape(progressIndicator, ".track");
            var arcCenter = arc.localToScene(arc.getCenterX(), arc.getCenterY());
            int centerX = (int) Math.round(arcCenter.getX());
            int centerY = (int) Math.round(arcCenter.getY());
            int radius = (int) Math.round(track.getRadiusX());
            Color arcPixel = image.getPixelReader().getColor(centerX, centerY - radius);
            Color trackPixel = image.getPixelReader().getColor(centerX - radius, centerY);

            assertTrue(arcPixel.getOpacity() > 0.4, () -> "arcPixel=" + arcPixel);
            assertTrue(trackPixel.getOpacity() > 0.4, () -> "trackPixel=" + trackPixel);
            assertTrue(colorDistance(arcPixel, trackPixel) > 0.1,
                    () -> "arcPixel=" + arcPixel + ", trackPixel=" + trackPixel);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-progress-indicator-arc.png"
            ));
        });
    }

    /// Verifies that Expressive progress remains flat by default and renders wavy geometry when configured.
    @Test
    void expressiveProgressDefaultsToFlatAndSupportsWavyConfiguration() {
        FxTestUtils.runOnFxThread(() -> {
            M3ProgressBar determinateBar = new M3ProgressBar(0.55);
            determinateBar.setPrefWidth(260.0);
            M3ProgressBar indeterminateBar = new M3ProgressBar();
            indeterminateBar.setPrefWidth(260.0);
            M3ProgressIndicator determinateIndicator = new M3ProgressIndicator(0.62);
            M3ProgressIndicator indeterminateIndicator = new M3ProgressIndicator();
            FlowPane root = new FlowPane(24.0, 24.0, determinateBar, indeterminateBar,
                    determinateIndicator, indeterminateIndicator);
            root.setStyle("-fx-background-color: white; -fx-padding: 24px; " + visualTestColors());
            Scene scene = new Scene(root, 640.0, 176.0);

            M3ThemeManager.install(scene, M3Theme.fromSeed(
                    Color.web("#006a6a"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            ));
            root.applyCss();
            root.resize(640.0, 176.0);
            root.layout();

            assertFalse(lookupShape(determinateBar, ".m3-progress-bar-wave").isVisible());
            assertFalse(lookupShape(indeterminateBar, ".m3-progress-bar-wave").isVisible());
            assertFalse(lookupShape(determinateIndicator, ".m3-progress-indicator-wave").isVisible());
            assertFalse(lookupShape(indeterminateIndicator, ".m3-progress-indicator-wave").isVisible());
            assertTrue(lookupShape(determinateBar, ".bar").isVisible());
            assertTrue(lookupShape(determinateIndicator, ".indicator").isVisible());
            assertEquals(40.0, determinateIndicator.getIndicatorSize(), 0.0001);
            assertEquals(48.0, determinateIndicator.getWaveIndicatorSize(), 0.0001);

            String linearWaveStyle = "-m3-wave-amplitude: 3px; "
                    + "-m3-wavelength: 40px; "
                    + "-m3-indeterminate-wavelength: 20px;";
            String circularWaveStyle = "-m3-wave-amplitude: 1.6px; "
                    + "-m3-wave-indicator-size: 48px; "
                    + "-m3-wavelength: 15px;";
            determinateBar.setStyle(linearWaveStyle);
            indeterminateBar.setStyle(linearWaveStyle);
            determinateIndicator.setStyle(circularWaveStyle);
            indeterminateIndicator.setStyle(circularWaveStyle);
            root.applyCss();
            root.layout();

            assertEquals(20.0, indeterminateBar.getIndeterminateWavelength(), 0.0001);
            assertTrue(lookupShape(determinateBar, ".m3-progress-bar-wave").isVisible());
            assertTrue(lookupShape(indeterminateBar, ".m3-progress-bar-wave").isVisible());
            assertTrue(lookupShape(determinateIndicator, ".m3-progress-indicator-wave").isVisible());
            assertTrue(lookupShape(indeterminateIndicator, ".m3-progress-indicator-wave").isVisible());
            assertEquals(48.0, determinateIndicator.getWidth(), 0.0001);
            assertEquals(48.0, determinateIndicator.getHeight(), 0.0001);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotHasColorVariety(image, 6);
            assertSnapshotNodeContainsContrast(image, determinateBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateBar, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, determinateIndicator, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, indeterminateIndicator, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-expressive-progress-indicators.png"
            ));
        });
    }

    /// Creates motion behavior with progress and loading indicator cycles that are observable in pulse tests.
    private static M3MotionBehavior observableIndeterminateProgressBehavior() {
        return M3MotionBehavior.builder()
                .linearProgressIndeterminateCycleDuration(OBSERVABLE_TEST_MOTION_DURATION)
                .circularProgressIndeterminateCycleDuration(OBSERVABLE_TEST_MOTION_DURATION)
                .loadingIndicatorMorphInterval(OBSERVABLE_TEST_MOTION_DURATION)
                .loadingIndicatorGlobalRotationDuration(OBSERVABLE_TEST_MOTION_DURATION)
                .build();
    }

    /// Creates motion behavior that only overrides loading indicator morph and rotation timings.
    private static M3MotionBehavior loadingIndicatorTimingBehavior(
            Duration loadingIndicatorMorphInterval,
            Duration loadingIndicatorGlobalRotationDuration
    ) {
        return M3MotionBehavior.builder()
                .loadingIndicatorMorphInterval(Objects.requireNonNull(
                        loadingIndicatorMorphInterval,
                        "loadingIndicatorMorphInterval"
                ))
                .loadingIndicatorGlobalRotationDuration(Objects.requireNonNull(
                        loadingIndicatorGlobalRotationDuration,
                        "loadingIndicatorGlobalRotationDuration"
                ))
                .build();
    }

    /// Returns whether a determinate progress bar has reached the expected rendered bar width.
    private static boolean progressBarWidthSettled(
            AtomicReference<@Nullable M3ProgressBar> progressBarReference,
            AtomicReference<@Nullable Rectangle> barReference,
            double expectedWidth
    ) {
        @Nullable M3ProgressBar progressBar = progressBarReference.get();
        @Nullable Rectangle bar = barReference.get();
        if (progressBar == null || bar == null) {
            return false;
        }

        progressBar.layout();
        return Math.abs(bar.getWidth() - expectedWidth) < 0.0001;
    }

    /// Returns whether a determinate circular progress indicator has reached the expected arc length.
    private static boolean progressIndicatorLengthSettled(
            AtomicReference<@Nullable M3ProgressIndicator> progressIndicatorReference,
            AtomicReference<@Nullable Arc> indicatorReference,
            double expectedLength
    ) {
        @Nullable M3ProgressIndicator progressIndicator = progressIndicatorReference.get();
        @Nullable Arc indicator = indicatorReference.get();
        if (progressIndicator == null || indicator == null) {
            return false;
        }

        progressIndicator.layout();
        return Math.abs(indicator.getLength() - expectedLength) < 0.0001;
    }

    /// Holds a real-window progress and loading indicator scene used by pulse-driven animation tests.
    private static final class IndeterminateProgressMotionScene {
        /// The stage that owns the tested scene graph.
        private final Stage stage;

        /// The root node that carries local animation settings.
        private final VBox root;

        /// The indeterminate linear progress bar under test.
        private final M3ProgressBar progressBar;

        /// The rendered indeterminate linear bar segment.
        private final Rectangle progressBarSegment;

        /// The indeterminate circular progress indicator under test.
        private final M3ProgressIndicator progressIndicator;

        /// The rendered circular progress arc.
        private final Arc progressIndicatorArc;

        /// The indeterminate loading indicator under test.
        private final M3LoadingIndicator loadingIndicator;

        /// The rendered loading indicator path.
        private final Path loadingIndicatorPath;

        /// Creates a holder for the sampled progress and loading indicator scene.
        private IndeterminateProgressMotionScene(
                Stage stage,
                VBox root,
                M3ProgressBar progressBar,
                Rectangle progressBarSegment,
                M3ProgressIndicator progressIndicator,
                Arc progressIndicatorArc,
                M3LoadingIndicator loadingIndicator,
                Path loadingIndicatorPath
        ) {
            this.stage = stage;
            this.root = root;
            this.progressBar = progressBar;
            this.progressBarSegment = progressBarSegment;
            this.progressIndicator = progressIndicator;
            this.progressIndicatorArc = progressIndicatorArc;
            this.loadingIndicator = loadingIndicator;
            this.loadingIndicatorPath = loadingIndicatorPath;
        }
    }

    /// Captures rendered animation state from the indeterminate progress scene.
    private static final class IndeterminateProgressMotionSample {
        /// The current horizontal position of the linear progress segment.
        private final double progressBarX;

        /// The current circular progress arc start angle.
        private final double progressIndicatorStartAngle;

        /// The current first rendered point of the loading indicator path.
        private final Point2D loadingIndicatorPoint;

        /// The current rendered loading indicator ink area.
        private final double loadingIndicatorInkArea;

        /// The current loading indicator path element count.
        private final int loadingIndicatorElementCount;

        /// Creates a rendered-state sample for progress and loading indicator motion.
        private IndeterminateProgressMotionSample(
                double progressBarX,
                double progressIndicatorStartAngle,
                Point2D loadingIndicatorPoint,
                double loadingIndicatorInkArea,
                int loadingIndicatorElementCount
        ) {
            this.progressBarX = progressBarX;
            this.progressIndicatorStartAngle = progressIndicatorStartAngle;
            this.loadingIndicatorPoint = loadingIndicatorPoint;
            this.loadingIndicatorInkArea = loadingIndicatorInkArea;
            this.loadingIndicatorElementCount = loadingIndicatorElementCount;
        }
    }

    /// Holds a standalone shape-morph scene used by loading indicator geometry tests.
    private static final class LoadingIndicatorFrameScene {
        /// The root snapshot node.
        private final Pane root;

        /// The active loading indicator path.
        private final Path indicator;

        /// The reusable shape morph scratch storage.
        private final M3ShapeMorph.Scratch scratch = new M3ShapeMorph.Scratch();

        /// Creates a standalone loading indicator frame scene.
        private LoadingIndicatorFrameScene() {
            indicator = new Path();
            indicator.setManaged(false);
            indicator.setFill(Color.BLACK);
            root = new Pane(indicator);
            root.setStyle("-fx-background-color: white;");
            new Scene(root, LOADING_INDICATOR_TEST_FRAME_SIZE, LOADING_INDICATOR_TEST_FRAME_SIZE);
            root.resize(LOADING_INDICATOR_TEST_FRAME_SIZE, LOADING_INDICATOR_TEST_FRAME_SIZE);
            root.applyCss();
            root.layout();
        }
    }

    /// Records real rendered circular progress phase samples across an indeterminate cycle seam.
    private static final class ProgressIndicatorCycleObservation {
        /// Coarse normalized angle buckets reached by the rendered arc.
        private final Set<Integer> angleBuckets = new HashSet<>();

        /// The number of rendered samples that have been observed.
        private int sampleCount;

        /// The number of raw animation phase wraps observed across the cycle seam.
        private int wrapCount;

        /// The largest normalized visual angle delta observed across a raw cycle wrap.
        private double maximumSeamVisualDelta;

        /// The raw start angle from the previous rendered sample.
        private double lastRawStartAngle;

        /// The normalized visual start angle from the previous rendered sample.
        private double lastNormalizedStartAngle;

        /// Whether the previous-angle fields contain a sample.
        private boolean hasLastStartAngle;

        /// Records one rendered start-angle sample.
        ///
        /// @param rawStartAngle the arc start angle reported by the rendered progress indicator
        private void observe(double rawStartAngle) {
            double normalizedStartAngle = rawStartAngle % 360.0;
            if (normalizedStartAngle < 0.0) {
                normalizedStartAngle += 360.0;
            }
            angleBuckets.add((int) Math.floor(normalizedStartAngle / 45.0));
            sampleCount++;

            if (hasLastStartAngle) {
                double rawDelta = rawStartAngle - lastRawStartAngle;
                if (rawDelta > 180.0) {
                    wrapCount++;
                    double angleDifference = Math.abs(normalizedStartAngle - lastNormalizedStartAngle);
                    maximumSeamVisualDelta = Math.max(
                            maximumSeamVisualDelta,
                            Math.min(angleDifference, 360.0 - angleDifference)
                    );
                }
            }

            lastRawStartAngle = rawStartAngle;
            lastNormalizedStartAngle = normalizedStartAngle;
            hasLastStartAngle = true;
        }
    }

    /// Shows a real-window progress and loading indicator scene with the requested animation setting.
    private static IndeterminateProgressMotionScene showIndeterminateProgressMotionScene(boolean animationsEnabled) {
        return showIndeterminateProgressMotionScene(animationsEnabled, observableIndeterminateProgressBehavior());
    }

    /// Shows a real-window progress and loading indicator scene with the requested animation and timing settings.
    private static IndeterminateProgressMotionScene showIndeterminateProgressMotionScene(
            boolean animationsEnabled,
            M3MotionBehavior motionBehavior
    ) {
        M3ProgressBar progressBar = new M3ProgressBar();
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        VBox root = new VBox(16.0, progressBar, progressIndicator, loadingIndicator);
        Scene scene = new Scene(root, 280.0, 260.0);
        Stage stage = new Stage();

        progressBar.setPrefSize(220.0, 16.0);
        progressIndicator.setPrefSize(64.0, 64.0);
        loadingIndicator.setStyle("-m3-container-size: 112px; -m3-indicator-size: 89px;");
        loadingIndicator.setPrefSize(112.0, 112.0);
        root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
        FxTestUtils.setCssAnimationsEnabled(root, animationsEnabled);
        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
        FxTestUtils.setMotionBehavior(root, Objects.requireNonNull(motionBehavior, "motionBehavior"));
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.resize(280.0, 260.0);
        root.layout();

        return new IndeterminateProgressMotionScene(
                stage,
                root,
                progressBar,
                (Rectangle) lookupShape(progressBar, ".bar"),
                progressIndicator,
                (Arc) lookupShape(progressIndicator, ".indicator"),
                loadingIndicator,
                assertInstanceOf(Path.class, loadingIndicator.lookup(".m3-loading-indicator-indicator"))
        );
    }

    /// Captures the current rendered state of progress and loading indicator motion.
    private static IndeterminateProgressMotionSample sampleIndeterminateProgressMotion(
            IndeterminateProgressMotionScene scene
    ) {
        scene.root.applyCss();
        scene.root.layout();
        WritableImage image = snapshotImageOnFxThread(scene.root);
        Color background = sampledNodeBackgroundColor(image, scene.loadingIndicator);
        return new IndeterminateProgressMotionSample(
                scene.progressBarSegment.getX(),
                scene.progressIndicatorArc.getStartAngle(),
                firstPathPoint(scene.loadingIndicatorPath),
                contrastingPixelArea(image, scene.loadingIndicatorPath, background, 0.04),
                scene.loadingIndicatorPath.getElements().size()
        );
    }

    /// Writes one loading indicator indeterminate morph frame to a standalone path.
    private static void writeLoadingIndicatorMorphFrame(
            LoadingIndicatorFrameScene frameScene,
            int segment,
            double segmentProgress,
            double globalRotation
    ) {
        M3ShapeMorph.Sequence sequence = M3ShapeMorph.loadingIndicatorIndeterminate();
        double progress = clampToUnit(segmentProgress);
        double morphRotationTarget = ((segment + 1.0) * LOADING_INDICATOR_QUARTER_ROTATION) % 1.0;
        if (morphRotationTarget < 0.0) {
            morphRotationTarget += 1.0;
        }
        double morphScaleEnvelope = Math.sin(Math.PI * progress);
        double morphScale = 1.0
                + LOADING_INDICATOR_MORPH_SCALE_AMPLITUDE * morphScaleEnvelope * morphScaleEnvelope;
        sequence.morphAt(segment).writeTo(
                frameScene.indicator,
                progress,
                LOADING_INDICATOR_TEST_CENTER,
                LOADING_INDICATOR_TEST_CENTER,
                LOADING_INDICATOR_TEST_INDICATOR_SIZE,
                sequence.scaleFactor(),
                morphScale,
                progress * LOADING_INDICATOR_QUARTER_ROTATION + morphRotationTarget + globalRotation,
                frameScene.scratch
        );
        frameScene.root.layout();
    }

    /// Returns whether circular progress has crossed a rendered indeterminate cycle seam without a visual jump.
    private static boolean progressIndicatorObservedSeamlessCycle(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference,
            ProgressIndicatorCycleObservation observation
    ) {
        @Nullable IndeterminateProgressMotionScene scene = sceneReference.get();
        if (scene == null) {
            return false;
        }

        scene.root.applyCss();
        scene.root.layout();
        observation.observe(scene.progressIndicatorArc.getStartAngle());
        return observation.wrapCount >= 1
                && observation.sampleCount >= 8
                && observation.angleBuckets.size() >= 6;
    }

    /// Verifies the captured circular progress cycle samples.
    private static void assertProgressIndicatorCycleObservation(ProgressIndicatorCycleObservation observation) {
        assertTrue(observation.wrapCount >= 1, () -> "wrapCount=" + observation.wrapCount);
        assertTrue(observation.sampleCount >= 8, () -> "sampleCount=" + observation.sampleCount);
        assertTrue(observation.angleBuckets.size() >= 6, () -> "angleBuckets=" + observation.angleBuckets);
        assertTrue(
                observation.maximumSeamVisualDelta <= MAXIMUM_PROGRESS_INDICATOR_CYCLE_SEAM_DELTA_DEGREES,
                () -> "maximumSeamVisualDelta=" + observation.maximumSeamVisualDelta
                        + ", limit=" + MAXIMUM_PROGRESS_INDICATOR_CYCLE_SEAM_DELTA_DEGREES
        );
    }

    /// Returns whether a loading indicator has rendered the requested number of distinct real-pulse frames.
    private static boolean loadingIndicatorObservedDistinctFrames(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference,
            Set<String> frameSignatures,
            int minimumFrameCount
    ) {
        @Nullable IndeterminateProgressMotionScene scene = sceneReference.get();
        if (scene == null) {
            return false;
        }

        scene.root.applyCss();
        scene.root.layout();
        frameSignatures.add(loadingIndicatorPathFrameSignature(scene.loadingIndicatorPath));
        return frameSignatures.size() >= minimumFrameCount;
    }

    /// Returns whether the loading indicator path bounds have expanded past the sampled initial area.
    private static boolean loadingIndicatorBoundsExpandedPastInitial(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference,
            AtomicReference<@Nullable Double> initialAreaReference,
            double areaRatio
    ) {
        @Nullable IndeterminateProgressMotionScene scene = sceneReference.get();
        @Nullable Double initialArea = initialAreaReference.get();
        if (scene == null || initialArea == null) {
            return false;
        }

        scene.root.applyCss();
        scene.root.layout();
        return loadingIndicatorPathBoundsArea(scene.loadingIndicatorPath) > initialArea * areaRatio;
    }

    /// Returns a coarse signature for a rendered loading indicator frame.
    private static String loadingIndicatorPathFrameSignature(Path path) {
        Bounds bounds = path.getBoundsInLocal();
        Point2D firstPoint = firstPathPoint(path);
        return Math.round(bounds.getWidth() * 2.0)
                + "x" + Math.round(bounds.getHeight() * 2.0)
                + "@" + Math.round(firstPoint.getX() * 2.0)
                + "," + Math.round(firstPoint.getY() * 2.0);
    }

    /// Returns the current local bounds area for a loading indicator path.
    private static double loadingIndicatorPathBoundsArea(Path path) {
        Bounds bounds = path.getBoundsInLocal();
        return bounds.getWidth() * bounds.getHeight();
    }

    /// Returns whether an expressive indeterminate progress bar has a visible moving wave separated from both tracks.
    private static boolean expressiveProgressBarIndeterminateWaveSeparated(
            AtomicReference<@Nullable M3ProgressBar> progressBarReference,
            AtomicReference<@Nullable Path> waveReference,
            AtomicReference<@Nullable Rectangle> leadingTrackReference,
            AtomicReference<@Nullable Rectangle> trailingTrackReference
    ) {
        @Nullable M3ProgressBar progressBar = progressBarReference.get();
        @Nullable Path wave = waveReference.get();
        @Nullable Rectangle leadingTrack = leadingTrackReference.get();
        @Nullable Rectangle trailingTrack = trailingTrackReference.get();
        if (progressBar == null || wave == null || leadingTrack == null || trailingTrack == null) {
            return false;
        }

        progressBar.applyCss();
        progressBar.layout();
        if (!wave.isVisible()
                || !leadingTrack.isVisible()
                || !trailingTrack.isVisible()
                || wave.getElements().isEmpty()) {
            return false;
        }

        Point2D waveStart = firstPathPoint(wave);
        Point2D waveEnd = lastPathPoint(wave);
        double gap = progressBar.getTrackGap();
        return waveStart.getX() > gap + 2.0
                && waveEnd.getX() < progressBar.getWidth() - gap - 2.0
                && waveEnd.getX() > waveStart.getX() + 8.0
                && leadingTrack.getX() + leadingTrack.getWidth() <= waveStart.getX() - gap + 0.0001
                && trailingTrack.getX() >= waveEnd.getX() + gap - 0.0001;
    }

    /// Verifies that an expressive indeterminate progress bar keeps the animated wave clear of both track pieces.
    private static void assertExpressiveProgressBarIndeterminateWaveSeparated(
            M3ProgressBar progressBar,
            Path wave,
            Rectangle leadingTrack,
            Rectangle trailingTrack
    ) {
        progressBar.applyCss();
        progressBar.layout();

        Point2D waveStart = firstPathPoint(wave);
        Point2D waveEnd = lastPathPoint(wave);
        double gap = progressBar.getTrackGap();
        assertTrue(wave.isVisible());
        assertTrue(leadingTrack.isVisible());
        assertTrue(trailingTrack.isVisible());
        assertTrue(waveStart.getX() > gap + 2.0,
                () -> "waveStart=" + waveStart + ", gap=" + gap);
        assertTrue(waveEnd.getX() < progressBar.getWidth() - gap - 2.0,
                () -> "waveEnd=" + waveEnd + ", width=" + progressBar.getWidth() + ", gap=" + gap);
        assertTrue(waveEnd.getX() > waveStart.getX() + 8.0,
                () -> "waveStart=" + waveStart + ", waveEnd=" + waveEnd);
        assertTrue(leadingTrack.getX() + leadingTrack.getWidth() <= waveStart.getX() - gap + 0.0001,
                () -> "leadingTrack=" + leadingTrack.getBoundsInParent() + ", waveStart=" + waveStart);
        assertTrue(trailingTrack.getX() >= waveEnd.getX() + gap - 0.0001,
                () -> "trailingTrack=" + trailingTrack.getBoundsInParent() + ", waveEnd=" + waveEnd);
    }

    /// Closes an expressive progress bar scene and clears local motion overrides.
    private static void closeExpressiveProgressBarIndeterminateWaveScene(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Pane> rootReference
    ) {
        FxTestUtils.runOnFxThread(() -> {
            @Nullable Pane root = rootReference.getAndSet(null);
            if (root != null) {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                FxTestUtils.clearMotionScheme(root);
                FxTestUtils.clearMotionBehavior(root);
            }

            @Nullable Stage stage = stageReference.getAndSet(null);
            if (stage != null) {
                stage.close();
            }
        });
    }

    /// Returns whether all indeterminate progress and loading indicators advanced past the stored sample.
    private static boolean indeterminateProgressMotionAdvanced(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference
    ) {
        return progressBarMotionAdvanced(sceneReference, sampleReference)
                && progressIndicatorMotionAdvanced(sceneReference, sampleReference)
                && loadingIndicatorMotionAdvanced(sceneReference, sampleReference);
    }

    /// Returns whether the linear progress segment advanced past the stored sample.
    private static boolean progressBarMotionAdvanced(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference
    ) {
        @Nullable IndeterminateProgressMotionScene scene = sceneReference.get();
        @Nullable IndeterminateProgressMotionSample sample = sampleReference.get();
        if (scene == null || sample == null) {
            return false;
        }

        scene.root.layout();
        return Math.abs(scene.progressBarSegment.getX() - sample.progressBarX) > 0.1;
    }

    /// Returns whether the circular progress arc advanced past the stored sample.
    private static boolean progressIndicatorMotionAdvanced(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference
    ) {
        @Nullable IndeterminateProgressMotionScene scene = sceneReference.get();
        @Nullable IndeterminateProgressMotionSample sample = sampleReference.get();
        if (scene == null || sample == null) {
            return false;
        }

        scene.root.layout();
        return Math.abs(scene.progressIndicatorArc.getStartAngle() - sample.progressIndicatorStartAngle) > 0.1;
    }

    /// Returns whether the loading indicator path advanced past the stored sample.
    private static boolean loadingIndicatorMotionAdvanced(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference
    ) {
        @Nullable IndeterminateProgressMotionScene scene = sceneReference.get();
        @Nullable IndeterminateProgressMotionSample sample = sampleReference.get();
        if (scene == null || sample == null) {
            return false;
        }

        scene.root.layout();
        return firstPathPoint(scene.loadingIndicatorPath).distance(sample.loadingIndicatorPoint) > 0.25;
    }

    /// Verifies that all indeterminate progress and loading indicators advanced past the stored sample.
    private static void assertIndeterminateProgressMotionAdvanced(
            IndeterminateProgressMotionScene scene,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference,
            String description
    ) {
        assertProgressBarMotionAdvanced(scene, sampleReference, description);
        assertProgressIndicatorMotionAdvanced(scene, sampleReference, description);
        assertLoadingIndicatorMotionAdvanced(scene, sampleReference, description);
    }

    /// Verifies that the linear progress segment advanced past the stored sample.
    private static void assertProgressBarMotionAdvanced(
            IndeterminateProgressMotionScene scene,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference,
            String description
    ) {
        IndeterminateProgressMotionSample sample = Objects.requireNonNull(sampleReference.get(), "sample");
        scene.root.layout();
        assertTrue(
                Math.abs(scene.progressBarSegment.getX() - sample.progressBarX) > 0.1,
                () -> description + " progressBarX=" + scene.progressBarSegment.getX()
                        + ", sample=" + sample.progressBarX
        );
    }

    /// Verifies that the circular progress arc advanced past the stored sample.
    private static void assertProgressIndicatorMotionAdvanced(
            IndeterminateProgressMotionScene scene,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference,
            String description
    ) {
        IndeterminateProgressMotionSample sample = Objects.requireNonNull(sampleReference.get(), "sample");
        scene.root.layout();
        assertTrue(
                Math.abs(scene.progressIndicatorArc.getStartAngle() - sample.progressIndicatorStartAngle) > 0.1,
                () -> description + " progressIndicatorStartAngle=" + scene.progressIndicatorArc.getStartAngle()
                        + ", sample=" + sample.progressIndicatorStartAngle
        );
    }

    /// Verifies that the loading indicator path advanced past the stored sample.
    private static void assertLoadingIndicatorMotionAdvanced(
            IndeterminateProgressMotionScene scene,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference,
            String description
    ) {
        IndeterminateProgressMotionSample sample = Objects.requireNonNull(sampleReference.get(), "sample");
        scene.root.layout();
        Point2D currentPoint = firstPathPoint(scene.loadingIndicatorPath);
        assertTrue(
                currentPoint.distance(sample.loadingIndicatorPoint) > 0.25,
                () -> description + " loadingPoint=" + currentPoint + ", sample=" + sample.loadingIndicatorPoint
        );
    }

    /// Verifies that reduced loading-indicator motion keeps the same visible shape while rotating.
    private static void assertLoadingIndicatorReducedMotionKeepsStableInk(
            IndeterminateProgressMotionScene scene,
            AtomicReference<@Nullable IndeterminateProgressMotionSample> sampleReference
    ) {
        IndeterminateProgressMotionSample sample = Objects.requireNonNull(sampleReference.get(), "sample");
        scene.root.applyCss();
        scene.root.layout();

        WritableImage image = snapshotImageOnFxThread(scene.root);
        Color background = sampledNodeBackgroundColor(image, scene.loadingIndicator);
        double currentInkArea = contrastingPixelArea(image, scene.loadingIndicatorPath, background, 0.04);
        double maximumAreaDelta = Math.max(36.0, sample.loadingIndicatorInkArea * 0.08);

        assertEquals(sample.loadingIndicatorElementCount, scene.loadingIndicatorPath.getElements().size());
        assertEquals(sample.loadingIndicatorInkArea, currentInkArea, maximumAreaDelta,
                () -> "reduced motion should rotate without morphing or scaling: initialArea="
                        + sample.loadingIndicatorInkArea + ", currentArea=" + currentInkArea);
    }

    /// Closes an indeterminate progress motion scene if one was created.
    private static void closeIndeterminateProgressMotionScene(
            AtomicReference<@Nullable IndeterminateProgressMotionScene> sceneReference
    ) {
        @Nullable IndeterminateProgressMotionScene scene = sceneReference.getAndSet(null);
        if (scene == null) {
            return;
        }

        FxTestUtils.runOnFxThread(() -> {
            FxTestUtils.setCssAnimationsEnabled(scene.root, true);
            FxTestUtils.clearMotionScheme(scene.root);
            FxTestUtils.clearMotionBehavior(scene.root);
            scene.stage.close();
        });
    }

    /// Returns the primary progress bar track, excluding the secondary expressive track.
    private static Rectangle progressBarPrimaryTrack(M3ProgressBar progressBar) {
        for (Node node : progressBar.lookupAll(".track")) {
            if (node instanceof Rectangle rectangle
                    && !rectangle.getStyleClass().contains("m3-progress-bar-secondary-track")) {
                return rectangle;
            }
        }
        throw new AssertionError("Missing primary progress bar track below " + progressBar);
    }

    /// Returns whether the supplied path still uses the same sampled path elements.
    private static boolean pathElementsAreSame(Path path, List<PathElement> elements) {
        if (path.getElements().size() != elements.size()) {
            return false;
        }
        for (int i = 0; i < elements.size(); i++) {
            if (path.getElements().get(i) != elements.get(i)) {
                return false;
            }
        }
        return true;
    }

    /// Returns whether animated expressive progress paths have advanced while keeping path elements stable.
    private static boolean expressiveProgressWavePathsMovedWithReusedElements(
            AtomicReference<@Nullable Pane> rootReference,
            AtomicReference<@Nullable M3ProgressBar> progressBarReference,
            AtomicReference<@Nullable M3ProgressIndicator> progressIndicatorReference,
            AtomicReference<@Nullable Path> barWaveReference,
            AtomicReference<@Nullable Path> circularTrackReference,
            AtomicReference<@Nullable Path> circularWaveReference,
            AtomicReference<@Nullable List<PathElement>> barElementsReference,
            AtomicReference<@Nullable List<PathElement>> circularTrackElementsReference,
            AtomicReference<@Nullable List<PathElement>> circularElementsReference,
            AtomicReference<@Nullable Point2D> initialBarPointReference,
            AtomicReference<@Nullable Point2D> initialCircularPointReference
    ) {
        @Nullable Pane root = rootReference.get();
        @Nullable M3ProgressBar progressBar = progressBarReference.get();
        @Nullable M3ProgressIndicator progressIndicator = progressIndicatorReference.get();
        @Nullable Path barWave = barWaveReference.get();
        @Nullable Path circularTrack = circularTrackReference.get();
        @Nullable Path circularWave = circularWaveReference.get();
        @Nullable List<PathElement> barElements = barElementsReference.get();
        @Nullable List<PathElement> circularTrackElements = circularTrackElementsReference.get();
        @Nullable List<PathElement> circularElements = circularElementsReference.get();
        @Nullable Point2D initialBarPoint = initialBarPointReference.get();
        @Nullable Point2D initialCircularPoint = initialCircularPointReference.get();
        if (root == null
                || progressBar == null
                || progressIndicator == null
                || barWave == null
                || circularTrack == null
                || circularWave == null
                || barElements == null
                || circularTrackElements == null
                || circularElements == null
                || initialBarPoint == null
                || initialCircularPoint == null) {
            return false;
        }

        root.applyCss();
        root.layout();
        progressBar.layout();
        progressIndicator.layout();
        return barWave.isVisible()
                && circularTrack.isVisible()
                && circularWave.isVisible()
                && pathElementsAreSame(barWave, barElements)
                && pathElementsAreSame(circularTrack, circularTrackElements)
                && pathElementsAreSame(circularWave, circularElements)
                && firstPathPoint(barWave).distance(initialBarPoint) > 0.1
                && firstPathPoint(circularWave).distance(initialCircularPoint) > 0.1;
    }

    /// Returns the first point in a path made from move, line, and cubic elements.
    private static Point2D firstPathPoint(Path path) {
        assertFalse(path.getElements().isEmpty());
        return pathElementPoint(path.getElements().get(0));
    }

    /// Returns the last point in a path made from move, line, and cubic elements.
    private static Point2D lastPathPoint(Path path) {
        assertFalse(path.getElements().isEmpty());
        return pathElementPoint(path.getElements().get(path.getElements().size() - 1));
    }

    /// Returns the point coordinates carried by a sampled path element.
    private static Point2D pathElementPoint(PathElement element) {
        if (element instanceof MoveTo moveTo) {
            return new Point2D(moveTo.getX(), moveTo.getY());
        }
        if (element instanceof LineTo lineTo) {
            return new Point2D(lineTo.getX(), lineTo.getY());
        }
        if (element instanceof CubicCurveTo cubicCurveTo) {
            return new Point2D(cubicCurveTo.getX(), cubicCurveTo.getY());
        }
        throw new AssertionError("Unsupported path element: " + element);
    }

    /// Returns the number of rendered pixels inside a node that contrast with the reference color.
    private static double contrastingPixelArea(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        double area = 0.0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                double distance = colorDistance(color, reference);
                if (color.getOpacity() > 0.1 && distance >= minimumDistance) {
                    area += Math.min(1.0, color.getOpacity() * distance / minimumDistance);
                }
            }
        }

        assertTrue(area > 0.0, () -> "No contrasting pixels found for " + node);
        return area;
    }
}
