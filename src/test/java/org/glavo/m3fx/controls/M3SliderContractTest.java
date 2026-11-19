// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.skins.M3RangeSliderSkin;
import org.glavo.m3fx.skins.M3SliderSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies slider values, input direction, geometry, and interaction feedback.
@NotNullByDefault
final class M3SliderContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that slider component token properties are styleable from CSS.
    @Test
    void sliderTokensAreStyleable() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setStyle(
                "-m3-track-thickness: 8px; "
                        + "-m3-track-shape: 12px; "
                        + "-m3-stop-indicator-size: 6px; "
                        + "-m3-stop-indicator-trailing-space: 7px; "
                        + "-m3-thumb-size: 52px; "
                        + "-m3-thumb-width: 8px; "
                        + "-m3-thumb-track-gap: 10px; "
                        + "-m3-touch-target-size: 56px; "
                        + "-m3-slider-icon-size: 18px; "
                        + "-m3-slider-icon-padding: 9px; "
                        + "-m3-value-indicator-bottom-space: 14px;"
        );

        applyCss(slider);

        assertEquals(8.0, slider.getTrackThickness(), 0.0001);
        assertEquals(12.0, slider.getTrackShape(), 0.0001);
        assertEquals(6.0, slider.getStopIndicatorSize(), 0.0001);
        assertEquals(7.0, slider.getStopIndicatorTrailingSpace(), 0.0001);
        assertEquals(52.0, slider.getThumbSize(), 0.0001);
        assertEquals(8.0, slider.getThumbWidth(), 0.0001);
        assertEquals(10.0, slider.getThumbTrackGap(), 0.0001);
        assertEquals(56.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(56.0, slider.prefHeight(-1.0), 0.0001);
    }

    /// Verifies the complete Material slider size scale, inset-icon geometry, and interaction handle widths.
    @Tier2Test
    @Test
    void sliderSizeScaleRendersPublishedGeometryAndTrackGraphics() {
        M3SliderSize[] sizes = M3SliderSize.values();
        double[] trackHeights = {16.0, 24.0, 40.0, 56.0, 96.0};
        double[] trackShapes = {8.0, 8.0, 12.0, 16.0, 28.0};
        double[] handleHeights = {44.0, 44.0, 52.0, 68.0, 108.0};
        double[] iconSizes = {0.0, 0.0, 24.0, 24.0, 32.0};
        double[] iconPaddings = {0.0, 0.0, 6.0, 6.0, 8.0};
        List<M3Slider> sliders = new ArrayList<>(sizes.length);
        List<Region> activeGraphics = new ArrayList<>(sizes.length);
        List<Region> inactiveGraphics = new ArrayList<>(sizes.length);
        VBox root = new VBox(8.0);

        for (M3SliderSize size : sizes) {
            M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
            Region activeGraphic = new Region();
            Region inactiveGraphic = new Region();
            slider.setSize(size);
            slider.setActiveTrackGraphic(activeGraphic);
            slider.setInactiveTrackGraphic(inactiveGraphic);
            slider.setPrefWidth(280.0);
            sliders.add(slider);
            activeGraphics.add(activeGraphic);
            inactiveGraphics.add(inactiveGraphic);
            root.getChildren().add(slider);
        }

        Scene scene = new Scene(root, 360.0, 520.0);
        M3Theme theme = M3Theme.fromSeed(
                Color.web("#6750a4"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        );
        M3ThemeManager.install(scene, theme);
        root.resize(360.0, 520.0);
        root.applyCss();
        root.layout();

        for (int index = 0; index < sizes.length; index++) {
            M3Slider slider = sliders.get(index);
            slider.layout();
            Region track = lookupRegion(slider, ".track");
            Region thumb = lookupRegion(slider, ".thumb");
            Region activeSlot = lookupRegion(slider, ".m3-slider-active-track-graphic-slot");
            Region inactiveSlot = lookupRegion(slider, ".m3-slider-inactive-track-graphic-slot");

            assertTrue(slider.getStyleClass().contains(M3Slider.sizeStyleClass(sizes[index])));
            assertEquals(trackHeights[index], slider.getTrackThickness(), 0.0001);
            assertEquals(trackShapes[index], slider.getTrackShape(), 0.0001);
            assertEquals(handleHeights[index], slider.getThumbSize(), 0.0001);
            assertEquals(iconSizes[index], slider.getIconSize(), 0.0001);
            assertEquals(iconPaddings[index], slider.getIconPadding(), 0.0001);
            assertEquals(trackHeights[index], track.getHeight(), 0.0001);
            assertEquals(handleHeights[index], thumb.getHeight(), 0.0001);
            assertTrue(slider.getHeight() + 0.0001 >= handleHeights[index]);

            boolean supportsInsetIcons = iconSizes[index] > 0.0;
            assertEquals(supportsInsetIcons, activeSlot.isVisible());
            assertFalse(inactiveSlot.isVisible(), "Only one inset-icon variant may be visible at a time");
            if (supportsInsetIcons) {
                assertEquals(iconSizes[index], activeSlot.getWidth(), 0.0001);
                assertEquals(iconSizes[index], activeSlot.getHeight(), 0.0001);
                assertRegionFill(
                        activeGraphics.get(index),
                        theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_PRIMARY)
                );
                assertRegionFill(
                        inactiveGraphics.get(index),
                        theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SECONDARY_CONTAINER)
                );
            }
        }

        M3Slider medium = sliders.get(M3SliderSize.MEDIUM.ordinal());
        assertEquals(Cursor.DEFAULT, medium.getCursor());
        Region mediumActiveSlot = lookupRegion(medium, ".m3-slider-active-track-graphic-slot");
        Region mediumInactiveSlot = lookupRegion(medium, ".m3-slider-inactive-track-graphic-slot");
        FxTestUtils.setCssAnimationsEnabled(root, false);
        medium.setValue(0.0);
        root.applyCss();
        root.layout();
        medium.layout();
        assertFalse(mediumActiveSlot.isVisible(), "The active segment cannot contain an icon at minimum value");
        assertTrue(mediumInactiveSlot.isVisible(), "The inset icon should move to the inactive track");
        assertEquals(medium.getIconSize(), mediumInactiveSlot.getWidth(), 0.0001);
        assertEquals(medium.getIconSize(), mediumInactiveSlot.getHeight(), 0.0001);

        medium.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
        root.applyCss();
        medium.layout();
        assertEquals(2.0, medium.getThumbWidth(), 0.0001);
        Region mediumThumb = lookupRegion(medium, ".thumb");
        Region focusIndicator = lookupRegion(medium, ".m3-focus-indicator");
        Region stateOverlay = lookupRegion(medium, ".m3-state-layer");
        assertEquals(2.0, mediumThumb.getWidth(), 0.0001);
        assertTrue(focusIndicator.isVisible());
        assertTrue(focusIndicator.getWidth() < medium.getTouchTargetSize(),
                "Slider focus should outline the handle rather than the circular touch target");
        assertTrue(focusIndicator.getHeight() > mediumThumb.getHeight());
        assertEquals(0.0, stateOverlay.getOpacity(), 0.0001,
                "Current slider tokens do not render the deprecated circular state layer");

        medium.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), false);
        root.applyCss();
        medium.layout();
        assertEquals(4.0, medium.getThumbWidth(), 0.0001);
        assertFalse(focusIndicator.isVisible());
        FxTestUtils.setCssAnimationsEnabled(root, true);
    }

    /// Verifies value-indicator content, state timing, Material colors, and mounted-graphic ownership cleanup.
    @Test
    void sliderValueIndicatorFollowsDirectManipulationAndSkinLifetime() {
        M3Slider slider = new M3Slider(0.0, 100.0, 37.0);
        Region activeGraphic = new Region();
        Region inactiveGraphic = new Region();
        slider.setSize(M3SliderSize.MEDIUM);
        slider.setActiveTrackGraphic(activeGraphic);
        slider.setInactiveTrackGraphic(inactiveGraphic);
        slider.setShowValueIndicator(true);
        slider.setLabelFormatter(new StringConverter<>() {
            /// Formats a slider value as a whole-number percentage.
            @Override
            public String toString(Double value) {
                return Math.round(value) + "%";
            }

            /// Parses a percentage string for the bidirectional converter contract.
            @Override
            public Double fromString(String text) {
                return Double.valueOf(text.replace("%", ""));
            }
        });
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 320.0, 160.0);
        M3Theme theme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, theme);
        root.applyCss();
        slider.resize(280.0, slider.prefHeight(-1.0));
        slider.layout();

        Label indicator = (Label) slider.lookup(".m3-slider-value-indicator");
        Region thumb = lookupRegion(slider, ".thumb");
        assertNotNull(indicator);
        assertFalse(indicator.isVisible());
        assertTrue(slider.getHeight() >= 104.0);

        slider.fireEvent(primaryMouseEvent(
                slider,
                MouseEvent.MOUSE_PRESSED,
                140.0,
                slider.getHeight() - 24.0,
                true
        ));
        root.applyCss();
        slider.layout();

        assertTrue(indicator.isVisible());
        assertEquals("50%", indicator.getText());
        assertTrue(indicator.getBoundsInParent().getMaxY() + slider.getValueIndicatorBottomSpace()
                <= thumb.getBoundsInParent().getMinY() + 0.0001);
        assertRegionFill(indicator, theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.INVERSE_SURFACE));
        assertEquals(
                theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.INVERSE_ON_SURFACE),
                indicator.getTextFill()
        );
        @Nullable AccessibleAttribute valueStringAttribute = M3Accessible.attribute("VALUE_STRING");
        if (valueStringAttribute != null) {
            assertEquals("50%", slider.queryAccessibleAttribute(valueStringAttribute));
        }

        slider.fireEvent(primaryMouseEvent(
                slider,
                MouseEvent.MOUSE_RELEASED,
                140.0,
                slider.getHeight() - 24.0,
                false
        ));
        assertFalse(indicator.isVisible());

        Skin<?> skin = slider.getSkin();
        assertNotNull(skin);
        assertTrue(activeGraphic.getStyleClass().contains("m3-slider-active-track-graphic"));
        assertTrue(inactiveGraphic.getStyleClass().contains("m3-slider-inactive-track-graphic"));
        skin.dispose();
        assertFalse(activeGraphic.getStyleClass().contains("m3-slider-active-track-graphic"));
        assertFalse(inactiveGraphic.getStyleClass().contains("m3-slider-inactive-track-graphic"));
        assertNull(activeGraphic.getParent());
        assertNull(inactiveGraphic.getParent());
    }

    /// Both slider types retain the label during exit and reverse without jumping on a second press.
    @Tier2Test
    @Test
    void valueIndicatorsAnimateAcrossPressAndRelease() throws InterruptedException {
        Stage stage = FxTestUtils.callOnFxThread(Stage::new);
        try {
            var sliders = FxTestUtils.callOnFxThread(() -> {
                var scalar = new M3Slider();
                var range = new M3RangeSlider();
                scalar.setShowValueIndicator(true);
                range.setShowValueIndicator(true);
                stage.setScene(new Scene(new VBox(scalar, range), 320, 240));
                stage.show();
                return List.<Control>of(scalar, range);
            });
            for (Control slider : sliders) {
                var label = FxTestUtils.callOnFxThread(() -> (Label) slider.lookup(".m3-slider-value-indicator"));
                Runnable press = () -> slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_PRESSED, 100, 70, true));
                Runnable release = () -> slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_RELEASED, 100, 70, false));
                FxTestUtils.runOnFxThreadWhen(() -> label.getOpacity() > 0 && label.getOpacity() < 1,
                        press, () -> assertTrue(label.isVisible() && label.getScaleX() < 1));
                FxTestUtils.runOnFxThreadWhen(() -> label.getOpacity() == 1 && label.getScaleX() == 1,
                        () -> {}, () -> {});
                FxTestUtils.runOnFxThreadWhen(() -> label.getOpacity() > 0 && label.getOpacity() < 1,
                        release, () -> {
                            assertTrue(label.isVisible());
                            double opacity = label.getOpacity();
                            press.run();
                            assertEquals(opacity, label.getOpacity(), 0.001);
                        });
                FxTestUtils.runOnFxThreadWhen(() -> label.getOpacity() == 1 && label.getScaleX() == 1,
                        () -> {}, release);
                FxTestUtils.runOnFxThreadWhen(() -> !label.isVisible(), () -> {}, () -> assertEquals(0, label.getOpacity()));
            }
        } finally {
            FxTestUtils.runOnFxThread(stage::close);
        }
    }

    /// Verifies that slider metric tokens do not overwrite explicit preferred sizes.
    @Test
    void sliderMetricTokensPreserveExplicitPreferredSize() {
        M3Slider slider = new M3Slider(0.0, 100.0, 48.0);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setPrefSize(56.0, 180.0);
        slider.setStyle(
                "-m3-track-thickness: 6px; "
                        + "-m3-thumb-size: 44px; "
                        + "-m3-thumb-width: 6px; "
                        + "-m3-thumb-track-gap: 8px; "
                        + "-m3-touch-target-size: 60px;"
        );
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 120.0, 240.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(slider.getPrefWidth(), slider.getPrefHeight());
        slider.layout();

        assertEquals(56.0, slider.getPrefWidth(), 0.0001);
        assertEquals(180.0, slider.getPrefHeight(), 0.0001);
        assertVerticalSliderTrackUsesExplicitHeight(slider);

        slider.setStyle(
                "-m3-track-thickness: 8px; "
                        + "-m3-thumb-size: 52px; "
                        + "-m3-thumb-width: 8px; "
                        + "-m3-thumb-track-gap: 10px; "
                        + "-m3-touch-target-size: 64px;"
        );
        root.applyCss();
        slider.resize(slider.getPrefWidth(), slider.getPrefHeight());
        slider.layout();

        assertEquals(56.0, slider.getPrefWidth(), 0.0001);
        assertEquals(180.0, slider.getPrefHeight(), 0.0001);
        assertVerticalSliderTrackUsesExplicitHeight(slider);
    }

    /// Verifies that slider track shape tokens reach the rendered track regions.
    @Test
    void sliderSkinAppliesTrackShapeToken() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setStyle("-m3-track-shape: 12px;");
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 260.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();
        root.applyCss();

        Region track = lookupRegion(slider, ".track");
        Region activeTrack = lookupRegion(slider, ".active-track");
        assertRegionRadii(track, 0.0, 12.0, 12.0, 0.0);
        assertRegionRadii(activeTrack, 12.0, 0.0, 0.0, 12.0);
    }

    /// Verifies that focused slider states keep Material track and thumb colors.
    @Test
    void sliderStateStylesPreserveMaterialColors() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        applyInteractivePseudoClasses(slider);
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        Region track = lookupRegion(slider, ".track");
        Region activeTrack = lookupRegion(slider, ".active-track");
        Region stopIndicator = lookupRegion(slider, ".stop-indicator");
        Region thumb = lookupRegion(slider, ".thumb");
        assertRegionFill(track, Color.rgb(7, 8, 9));
        assertRegionFill(activeTrack, Color.rgb(1, 2, 3));
        assertRegionFill(stopIndicator, Color.rgb(10, 11, 12));
        assertNoBorder(track);
        assertRegionFill(thumb, Color.rgb(1, 2, 3));
        assertNoBorder(thumb);
    }

    /// Verifies that horizontal sliders mirror value geometry and arrow-key semantics in right-to-left mode.
    @Test
    void sliderMirrorsHorizontalValueAndKeysForRightToLeft() {
        M3Slider slider = new M3Slider(0.0, 100.0, 25.0);
        slider.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 280.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        Region track = lookupRegion(slider, ".track");
        Region thumb = lookupRegion(slider, ".thumb");
        Bounds trackBounds = track.localToScene(track.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        double trackCenter = trackBounds.getCenterX();
        double thumbCenter = thumbBounds.getCenterX();
        assertTrue(thumbCenter > trackCenter, () -> "thumbCenter=" + thumbCenter + ", trackCenter=" + trackCenter);

        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

        assertEquals(35.0, slider.getValue(), 0.0001);

        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertEquals(25.0, slider.getValue(), 0.0001);

        Bounds sliderSceneBounds = slider.localToScene(slider.getBoundsInLocal());
        double sceneCenterY = sliderSceneBounds.getCenterY();
        double lowValueSceneX = sliderSceneBounds.getMaxX() - slider.getThumbWidth() / 2.0;
        slider.fireEvent(primaryMouseEventAtScenePoint(
                slider,
                MouseEvent.MOUSE_PRESSED,
                lowValueSceneX,
                sceneCenterY,
                true
        ));
        slider.fireEvent(primaryMouseEventAtScenePoint(
                slider,
                MouseEvent.MOUSE_RELEASED,
                lowValueSceneX,
                sceneCenterY,
                false
        ));

        assertEquals(0.0, slider.getValue(), 0.0001);
        assertFalse(slider.isValueChanging());

        double highValueSceneX = sliderSceneBounds.getMinX() + slider.getThumbWidth() / 2.0;
        slider.fireEvent(primaryMouseEventAtScenePoint(
                slider,
                MouseEvent.MOUSE_PRESSED,
                highValueSceneX,
                sceneCenterY,
                true
        ));
        slider.fireEvent(primaryMouseEventAtScenePoint(
                slider,
                MouseEvent.MOUSE_RELEASED,
                highValueSceneX,
                sceneCenterY,
                false
        ));

        assertEquals(100.0, slider.getValue(), 0.0001);
        assertFalse(slider.isValueChanging());
    }

    /// Verifies that slider active and inactive tracks leave Material handle gaps in every direction.
    @Test
    void sliderTrackSegmentsFollowValueDirectionOrientationAndHandleGap() {
        M3Slider leftToRight = new M3Slider(0.0, 100.0, 25.0);
        M3Slider rightToLeft = new M3Slider(0.0, 100.0, 25.0);
        M3Slider vertical = new M3Slider(0.0, 100.0, 25.0);
        rightToLeft.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        vertical.setOrientation(Orientation.VERTICAL);
        Pane root = new Pane(leftToRight, rightToLeft, vertical);
        Scene scene = new Scene(root, 640.0, 220.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        leftToRight.resize(220.0, 48.0);
        rightToLeft.resize(220.0, 48.0);
        vertical.resize(56.0, 180.0);
        leftToRight.layout();
        rightToLeft.layout();
        vertical.layout();

        assertHorizontalSliderSegments(leftToRight, false);
        assertHorizontalSliderSegments(rightToLeft, true);
        assertVerticalSliderSegments(vertical);
    }

    /// Verifies that slider stop indicators disappear when no inactive track remains or their size is zero.
    @Test
    void sliderStopIndicatorVisibilityFollowsInactiveTrackAndSizeToken() {
        M3Slider maximum = new M3Slider(0.0, 100.0, 100.0);
        M3Slider hiddenByToken = new M3Slider(0.0, 100.0, 50.0);
        hiddenByToken.setStopIndicatorSize(0.0);
        Pane root = new Pane(maximum, hiddenByToken);
        Scene scene = new Scene(root, 500.0, 100.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        maximum.resizeRelocate(0.0, 0.0, 220.0, 48.0);
        hiddenByToken.resizeRelocate(240.0, 0.0, 220.0, 48.0);
        maximum.layout();
        hiddenByToken.layout();

        assertFalse(lookupRegion(maximum, ".stop-indicator").isVisible());
        assertFalse(lookupRegion(hiddenByToken, ".stop-indicator").isVisible());
    }

    /// Verifies that centered sliders render an active range from the midpoint and inactive end stops on both sides.
    @Test
    void sliderCenteredTrackUsesMidpointAcrossDirections() {
        M3Slider leftToRight = new M3Slider(-100.0, 100.0, -50.0);
        M3Slider rightToLeft = new M3Slider(-100.0, 100.0, -50.0);
        M3Slider vertical = new M3Slider(-100.0, 100.0, 50.0);
        leftToRight.setCentered(true);
        rightToLeft.setCentered(true);
        rightToLeft.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        vertical.setCentered(true);
        vertical.setOrientation(Orientation.VERTICAL);
        Pane root = new Pane(leftToRight, rightToLeft, vertical);
        Scene scene = new Scene(root, 640.0, 240.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        leftToRight.resizeRelocate(0.0, 0.0, 220.0, 48.0);
        rightToLeft.resizeRelocate(240.0, 0.0, 220.0, 48.0);
        vertical.resizeRelocate(500.0, 0.0, 56.0, 180.0);
        leftToRight.layout();
        rightToLeft.layout();
        vertical.layout();

        assertTrue(leftToRight.getPseudoClassStates().contains(PseudoClass.getPseudoClass("centered")));
        assertCenteredHorizontalSliderSegments(leftToRight, false);
        assertCenteredHorizontalSliderSegments(rightToLeft, true);
        assertCenteredVerticalSliderSegments(vertical);
    }

    /// Verifies that centered sliders keep a full handle gap when the value is very close to the midpoint.
    @Test
    void sliderCenteredTrackCollapsesShortActiveSegmentWithoutNegativeGeometry() {
        M3Slider slider = new M3Slider(-100.0, 100.0, 2.0);
        slider.setCentered(true);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 260.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        Region activeTrack = lookupRegion(slider, ".active-track");
        List<Region> inactiveTracks = visibleSliderRegions(slider, ".track");
        inactiveTracks.sort((first, second) -> Double.compare(
                first.getBoundsInParent().getMinX(),
                second.getBoundsInParent().getMinX()
        ));
        assertEquals(0.0, activeTrack.getWidth(), 0.0001);
        assertEquals(2, inactiveTracks.size());
        assertEquals(slider.getThumbWidth() + slider.getThumbTrackGap() * 2.0,
                inactiveTracks.get(1).getBoundsInParent().getMinX()
                        - inactiveTracks.get(0).getBoundsInParent().getMaxX(),
                0.0001);
    }

    /// Verifies that discrete sliders pool visible selectable-value stops and omit stops hidden by the handle.
    @Test
    void sliderDiscreteStopsFollowStepsActiveRangeAndDensityGuard() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setStepSize(10.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 260.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();
        root.applyCss();

        List<Region> visibleStops = visibleSliderRegions(slider, ".step-indicator");
        assertEquals(9, visibleStops.size());
        assertEquals(5, visibleStops.stream()
                .filter(region -> region.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")))
                .count());
        assertEquals(4, visibleStops.stream()
                .filter(region -> !region.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")))
                .count());

        slider.setStepSize(1.0);
        slider.layout();

        assertTrue(visibleSliderRegions(slider, ".step-indicator").isEmpty());
    }

    /// Verifies that disabled slider parts use the distinct MD3 active, inactive, handle, and stop opacities.
    @Test
    void sliderDisabledPartsUseMaterialOpacityRoles() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        slider.setDisable(true);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 260.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();
        root.applyCss();

        assertEquals(1.0, slider.getOpacity(), 0.0001);
        assertEquals(Cursor.DEFAULT, slider.getCursor());
        assertEquals(0.12, lookupRegion(slider, ".track").getOpacity(), 0.0001);
        assertEquals(0.38, lookupRegion(slider, ".active-track").getOpacity(), 0.0001);
        assertEquals(0.38, lookupRegion(slider, ".thumb").getOpacity(), 0.0001);
        assertEquals(0.38, lookupRegion(slider, ".stop-indicator").getOpacity(), 0.0001);
    }

    /// Verifies range-slider values, Material geometry, state colors, input, accessibility, RTL, and skin cleanup.
    @Test
    void rangeSliderImplementsMaterialGeometryInteractionAccessibilityAndLifetime() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider range = new M3RangeSlider(0.0, 100.0, 25.0, 75.0);
            range.setSize(M3SliderSize.MEDIUM);
            range.setAnimationsEnabled(false);
            range.setStepSize(5.0);
            range.setShowValueIndicator(true);

            M3RangeSlider rightToLeft = new M3RangeSlider(0.0, 100.0, 20.0, 80.0);

            M3RangeSlider vertical = new M3RangeSlider(0.0, 100.0, 20.0, 80.0);
            vertical.setOrientation(Orientation.VERTICAL);
            vertical.setSize(M3SliderSize.SMALL);

            M3RangeSlider disabled = new M3RangeSlider(0.0, 100.0, 35.0, 85.0);
            disabled.setDisable(true);

            Pane root = new Pane(range, rightToLeft, vertical, disabled);
            Scene scene = new Scene(root, 520.0, 360.0);
            M3Theme theme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );
            M3ThemeManager.install(scene, theme);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            try {
                root.applyCss();
                range.resizeRelocate(20.0, 20.0, 320.0, range.prefHeight(-1.0));
                rightToLeft.resizeRelocate(20.0, 145.0, 320.0, rightToLeft.prefHeight(-1.0));
                disabled.resizeRelocate(20.0, 220.0, 320.0, disabled.prefHeight(-1.0));
                vertical.resizeRelocate(400.0, 20.0, vertical.prefWidth(-1.0), 260.0);
                root.layout();
                range.layout();
                rightToLeft.layout();
                vertical.layout();
                disabled.layout();
                root.applyCss();

                range.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                vertical.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                disabled.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                rightToLeft.layout();

                assertInstanceOf(M3RangeSliderSkin.class, range.getSkin());
                assertEquals(AccessibleRole.PARENT, range.getAccessibleRole());
                assertFalse(range.isFocusTraversable());
                assertTrue(range.getStyleClass().contains("m3-slider"));
                assertTrue(range.getStyleClass().contains("m3-range-slider"));
                assertTrue(range.getStyleClass().contains(M3Slider.sizeStyleClass(M3SliderSize.MEDIUM)));
                assertEquals(40.0, range.getTrackThickness(), 0.0001);
                assertEquals(12.0, range.getTrackShape(), 0.0001);
                assertEquals(52.0, range.getThumbSize(), 0.0001);
                assertEquals(4.0, range.getThumbWidth(), 0.0001);
                assertEquals(2.0, range.getFocusedThumbWidth(), 0.0001);
                assertEquals(2.0, range.getPressedThumbWidth(), 0.0001);
                assertEquals(4.0, range.getStopIndicatorTrailingSpace(), 0.0001);

                Pane lowThumb = assertInstanceOf(Pane.class, range.lookup(".range-low-thumb"));
                Pane highThumb = assertInstanceOf(Pane.class, range.lookup(".range-high-thumb"));
                Region lowHandle = assertInstanceOf(Region.class, lowThumb.lookup(".thumb"));
                Region highHandle = assertInstanceOf(Region.class, highThumb.lookup(".thumb"));
                Region leadingTrack = lookupRegion(range, ".range-leading-track");
                Region activeTrack = lookupRegion(range, ".range-active-track");
                Region trailingTrack = lookupRegion(range, ".range-trailing-track");

                double lowCenter = lowThumb.getBoundsInParent().getCenterX();
                double highCenter = highThumb.getBoundsInParent().getCenterX();
                assertTrue(lowCenter < highCenter);
                double lowGapExtent = lowHandle.getWidth() / 2.0 + range.getThumbTrackGap();
                double highGapExtent = highHandle.getWidth() / 2.0 + range.getThumbTrackGap();
                assertEquals(lowGapExtent,
                        lowCenter - leadingTrack.getBoundsInParent().getMaxX(),
                        0.0001);
                assertEquals(lowGapExtent,
                        activeTrack.getBoundsInParent().getMinX() - lowCenter,
                        0.0001);
                assertEquals(highGapExtent,
                        highCenter - activeTrack.getBoundsInParent().getMaxX(),
                        0.0001);
                assertEquals(highGapExtent,
                        trailingTrack.getBoundsInParent().getMinX() - highCenter,
                        0.0001);
                assertEquals(40.0, leadingTrack.getHeight(), 0.0001);
                assertEquals(52.0, lowHandle.getHeight(), 0.0001,
                        () -> "lowThumb=" + lowThumb.getBoundsInParent()
                                + ", lowHandle=" + lowHandle.getBoundsInParent()
                                + ", children=" + lowThumb.getChildren());
                assertEquals(4.0, lowHandle.getWidth(), 0.0001);
                assertEquals(4.0, highHandle.getWidth(), 0.0001);
                assertRegionRadii(leadingTrack, 12.0, 0.0, 0.0, 12.0);
                assertRegionRadii(activeTrack, 0.0, 0.0, 0.0, 0.0);
                assertRegionRadii(trailingTrack, 0.0, 12.0, 12.0, 0.0);
                assertRegionFill(activeTrack, theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY));
                assertRegionFill(leadingTrack,
                        theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.SECONDARY_CONTAINER));
                assertRegionFill(lowHandle, theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY));

                List<Region> endStops = range.lookupAll(".stop-indicator").stream()
                        .filter(Node::isVisible)
                        .map(node -> assertInstanceOf(Region.class, node))
                        .filter(region -> !region.getStyleClass().contains("step-indicator"))
                        .sorted((first, second) -> Double.compare(
                                first.getBoundsInParent().getMinX(),
                                second.getBoundsInParent().getMinX()
                        ))
                        .toList();
                assertEquals(2, endStops.size());
                double stopCenterOffset = range.getThumbWidth() / 2.0
                        + range.getStopIndicatorTrailingSpace()
                        + range.getStopIndicatorSize() / 2.0;
                assertEquals(stopCenterOffset, endStops.get(0).getBoundsInParent().getCenterX(), 0.0001);
                assertEquals(
                        range.getWidth() - stopCenterOffset,
                        endStops.get(1).getBoundsInParent().getCenterX(),
                        0.0001
                );

                List<Region> stops = range.lookupAll(".step-indicator").stream()
                        .filter(Node::isVisible)
                        .map(node -> assertInstanceOf(Region.class, node))
                        .toList();
                assertEquals(17, stops.size());
                assertEquals(9, stops.stream()
                        .filter(stop -> stop.getPseudoClassStates().contains(PseudoClass.getPseudoClass("active")))
                        .count());

                assertEquals(AccessibleRole.SLIDER, lowThumb.getAccessibleRole());
                assertEquals(0.0, lowThumb.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
                assertEquals(75.0, lowThumb.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
                assertEquals(25.0, lowThumb.queryAccessibleAttribute(AccessibleAttribute.VALUE));
                assertEquals(25.0, highThumb.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
                assertEquals(100.0, highThumb.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
                assertEquals(75.0, highThumb.queryAccessibleAttribute(AccessibleAttribute.VALUE));
                assertEquals(Orientation.HORIZONTAL,
                        lowThumb.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));

                lowThumb.requestFocus();
                assertTrue(lowThumb.isFocused());
                lowThumb.fireEvent(targetedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT, lowThumb));
                assertEquals(30.0, range.getLowValue(), 0.0001);
                lowThumb.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB));
                assertTrue(highThumb.isFocused(), "Tab should move from the lower handle to the upper handle");
                highThumb.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.TAB, true));
                assertTrue(lowThumb.isFocused(), "Shift+Tab should return to the lower handle");
                range.setAnimationsEnabled(false);
                lowThumb.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), true);
                root.applyCss();
                range.layout();
                assertEquals(2.0, lowHandle.getWidth(), 0.0001);
                Region lowFocusIndicator = lookupRegion(lowThumb, ".m3-focus-indicator");
                Region lowStateOverlay = lookupRegion(lowThumb, ".m3-state-layer");
                assertTrue(lowFocusIndicator.isVisible());
                assertTrue(lowFocusIndicator.getWidth() < range.getTouchTargetSize(),
                        "Range-slider focus should outline its handle instead of the circular touch target");
                assertTrue(lowFocusIndicator.getHeight() > lowHandle.getHeight());
                assertEquals(0.0, lowStateOverlay.getOpacity(), 0.0001);
                lowThumb.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-visible"), false);
                root.applyCss();
                assertFalse(lowFocusIndicator.isVisible());
                range.setAnimationsEnabled(true);

                Label valueIndicator = assertInstanceOf(
                        Label.class,
                        range.lookup(".m3-slider-value-indicator")
                );
                double pressX = highThumb.getBoundsInParent().getCenterX();
                double pressY = highThumb.getBoundsInParent().getCenterY();
                range.fireEvent(primaryMouseEvent(range, MouseEvent.MOUSE_PRESSED, pressX, pressY, true));
                range.layout();
                assertTrue(range.isHighValueChanging());
                assertFalse(range.isLowValueChanging());
                assertTrue(valueIndicator.isVisible());
                assertEquals(2.0, highHandle.getWidth(), 0.0001);
                assertEquals(
                        highHandle.getWidth() / 2.0 + range.getThumbTrackGap(),
                        highThumb.getBoundsInParent().getCenterX()
                                - activeTrack.getBoundsInParent().getMaxX(),
                        0.0001
                );
                assertEquals(
                        highHandle.getWidth() / 2.0 + range.getThumbTrackGap(),
                        trailingTrack.getBoundsInParent().getMinX()
                                - highThumb.getBoundsInParent().getCenterX(),
                        0.0001
                );

                double targetX = range.getThumbWidth() / 2.0
                        + (range.getWidth() - range.getThumbWidth()) * 0.9;
                range.fireEvent(primaryMouseEvent(
                        range,
                        MouseEvent.MOUSE_DRAGGED,
                        targetX,
                        pressY,
                        true
                ));
                assertEquals(90.0, range.getHighValue(), 0.0001);
                range.fireEvent(primaryMouseEvent(
                        range,
                        MouseEvent.MOUSE_RELEASED,
                        targetX,
                        pressY,
                        false
                ));
                assertFalse(range.isHighValueChanging());
                assertFalse(valueIndicator.isVisible());

                double highThumbLocalX = highThumb.getWidth() / 2.0;
                double highThumbLocalY = highThumb.getHeight() / 2.0;
                highThumb.fireEvent(primaryMouseEvent(
                        highThumb,
                        MouseEvent.MOUSE_PRESSED,
                        highThumbLocalX,
                        highThumbLocalY,
                        true
                ));
                highThumb.fireEvent(primaryMouseEvent(
                        highThumb,
                        MouseEvent.MOUSE_RELEASED,
                        highThumbLocalX,
                        highThumbLocalY,
                        false
                ));
                assertEquals(90.0, range.getHighValue(), 0.0001,
                        "Clicking a handle must use coordinates local to the range slider");
                assertEquals(30.0, range.getLowValue(), 0.0001,
                        "Clicking the upper handle must not move the lower handle");

                highThumb.executeAccessibleAction(AccessibleAction.DECREMENT);
                assertEquals(85.0, range.getHighValue(), 0.0001);
                highThumb.executeAccessibleAction(AccessibleAction.SET_VALUE, 65.0);
                assertEquals(65.0, range.getHighValue(), 0.0001);
                lowThumb.executeAccessibleAction(AccessibleAction.SET_VALUE, 80.0);
                assertEquals(65.0, range.getLowValue(), 0.0001);

                Pane rtlLowThumb = assertInstanceOf(Pane.class, rightToLeft.lookup(".range-low-thumb"));
                Pane rtlHighThumb = assertInstanceOf(Pane.class, rightToLeft.lookup(".range-high-thumb"));
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, rightToLeft.getEffectiveNodeOrientation());
                assertTrue(rtlLowThumb.localToScene(rtlLowThumb.getBoundsInLocal()).getCenterX()
                        > rtlHighThumb.localToScene(rtlHighThumb.getBoundsInLocal()).getCenterX());
                Bounds rtlSceneBounds = rightToLeft.localToScene(rightToLeft.getBoundsInLocal());
                double rtlRightEdgeSceneX = rtlSceneBounds.getMaxX() - 8.0;
                double rtlSceneCenterY = rtlSceneBounds.getCenterY();
                rightToLeft.fireEvent(primaryMouseEventAtScenePoint(
                        rightToLeft,
                        MouseEvent.MOUSE_PRESSED,
                        rtlRightEdgeSceneX,
                        rtlSceneCenterY,
                        true
                ));
                assertTrue(rightToLeft.isLowValueChanging());
                assertFalse(rightToLeft.isHighValueChanging());
                assertTrue(rightToLeft.getLowValue() < 5.0,
                        () -> "lowValue=" + rightToLeft.getLowValue());
                rightToLeft.fireEvent(primaryMouseEventAtScenePoint(
                        rightToLeft,
                        MouseEvent.MOUSE_RELEASED,
                        rtlRightEdgeSceneX,
                        rtlSceneCenterY,
                        false
                ));
                rightToLeft.setLowValue(20.0);

                rtlLowThumb.requestFocus();
                assertTrue(rtlLowThumb.isFocused());
                rtlLowThumb.fireEvent(targetedKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT, rtlLowThumb));
                assertEquals(30.0, rightToLeft.getLowValue(), 0.0001);
                double rtlLowLocalX = rtlLowThumb.getBoundsInLocal().getCenterX();
                double rtlLowLocalY = rtlLowThumb.getBoundsInLocal().getCenterY();
                rtlLowThumb.fireEvent(primaryMouseEvent(
                        rtlLowThumb,
                        MouseEvent.MOUSE_PRESSED,
                        rtlLowLocalX,
                        rtlLowLocalY,
                        true
                ));
                assertTrue(rightToLeft.isLowValueChanging());
                assertFalse(rightToLeft.isHighValueChanging());
                rtlLowThumb.fireEvent(primaryMouseEvent(
                        rtlLowThumb,
                        MouseEvent.MOUSE_RELEASED,
                        rtlLowLocalX,
                        rtlLowLocalY,
                        false
                ));
                assertEquals(30.0, rightToLeft.getLowValue(), 0.0001);
                assertEquals(80.0, rightToLeft.getHighValue(), 0.0001);

                Pane verticalLowThumb = assertInstanceOf(Pane.class, vertical.lookup(".range-low-thumb"));
                Pane verticalHighThumb = assertInstanceOf(Pane.class, vertical.lookup(".range-high-thumb"));
                assertTrue(verticalLowThumb.getBoundsInParent().getCenterY()
                        > verticalHighThumb.getBoundsInParent().getCenterY());
                assertEquals(24.0, lookupRegion(vertical, ".range-active-track").getWidth(), 0.0001);

                assertEquals(1.0, disabled.getOpacity(), 0.0001);
                assertEquals(0.12, lookupRegion(disabled, ".range-leading-track").getOpacity(), 0.0001);
                assertEquals(0.38, lookupRegion(disabled, ".range-active-track").getOpacity(), 0.0001);
                assertEquals(0.38, lookupRegion(disabled, ".thumb").getOpacity(), 0.0001);
                assertEquals(0.38, lookupRegion(disabled, ".stop-indicator").getOpacity(), 0.0001);

                M3RangeSliderSkin skin = assertInstanceOf(M3RangeSliderSkin.class, range.getSkin());
                skin.dispose();
                assertNull(lowThumb.getParent());
                assertNull(highThumb.getParent());
                assertFalse(range.isLowValueChanging());
                assertFalse(range.isHighValueChanging());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that slider snapshots show distinct rendered track and thumb pixels.
    @Tier2Test
    @Test
    void sliderSnapshotRendersTrackAndThumbPixels() {
        FxTestUtils.runOnFxThread(() -> {
            M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
            slider.setPrefSize(220.0, 56.0);
            FlowPane root = new FlowPane(slider);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 260.0, 80.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(260.0, 80.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            var sliderBounds = slider.getBoundsInParent();
            int sliderX = (int) Math.round(sliderBounds.getMinX());
            int sliderY = (int) Math.round(sliderBounds.getMinY());
            Color activeTrack = averageSnapshotColor(
                    image, sliderX + 20, sliderY + 26, sliderX + 29, sliderY + 31
            );
            Color inactiveTrack = averageSnapshotColor(
                    image, sliderX + 186, sliderY + 26, sliderX + 195, sliderY + 31
            );
            Color thumb = averageSnapshotColor(
                    image, sliderX + 105, sliderY + 23, sliderX + 116, sliderY + 34
            );
            Color emptyTouchTarget = averageSnapshotColor(
                    image, sliderX + 105, sliderY + 2, sliderX + 116, sliderY + 8
            );

            assertTrue(colorDistance(inactiveTrack, thumb) > 0.1,
                    () -> "inactiveTrack=" + inactiveTrack + ", thumb=" + thumb);
            assertTrue(colorDistance(activeTrack, inactiveTrack) > 0.1,
                    () -> "activeTrack=" + activeTrack + ", inactiveTrack=" + inactiveTrack);
            assertTrue(emptyTouchTarget.getOpacity() < 0.1
                    || colorDistance(emptyTouchTarget, thumb) > 0.1);
        });
    }

    /// Verifies that m3fx sliders create the Material Design 3 slider skin.
    @Test
    void sliderCreatesMaterialSkin() {
        M3Slider slider = new M3Slider();

        applyCss(slider);

        assertInstanceOf(M3SliderSkin.class, slider.getSkin());
    }

    /// Verifies that sliders expose accessible value, range, orientation, and value adjustment actions.
    @Test
    void sliderExposesAccessibleValuesAndActions() {
        M3Slider slider = new M3Slider(0.0, 100.0, 40.0);
        slider.setBlockIncrement(10.0);
        @Nullable AccessibleAttribute valueStringAttribute = M3Accessible.attribute("VALUE_STRING");

        assertEquals(0.0, slider.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(100.0, slider.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(40.0, (Double) slider.queryAccessibleAttribute(AccessibleAttribute.VALUE), 0.0001);
        if (valueStringAttribute != null) {
            assertEquals("40", slider.queryAccessibleAttribute(valueStringAttribute));
        }
        assertEquals(Orientation.HORIZONTAL, slider.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));

        slider.executeAccessibleAction(AccessibleAction.SET_VALUE, 75.0);
        assertEquals(75.0, slider.getValue(), 0.0001);
        slider.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(85.0, slider.getValue(), 0.0001);
        slider.executeAccessibleAction(AccessibleAction.BLOCK_DECREMENT);
        assertEquals(75.0, slider.getValue(), 0.0001);
        slider.executeAccessibleAction(AccessibleAction.SET_VALUE, 120.0);
        assertEquals(100.0, slider.getValue(), 0.0001);

        slider.setOrientation(Orientation.VERTICAL);

        assertEquals(Orientation.VERTICAL, slider.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));
    }

    /// Verifies that discrete sliders snap programmatic, keyboard, and accessibility value updates.
    @Test
    void sliderSnapsDiscreteValuesAcrossInputPaths() {
        M3Slider slider = new M3Slider(0.0, 100.0, 0.0);
        slider.setStepSize(10.0);
        slider.setBlockIncrement(25.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        slider.setValue(44.0);
        assertEquals(40.0, slider.getValue(), 0.0001);

        slider.adjustValue(46.0);
        assertEquals(50.0, slider.getValue(), 0.0001);

        slider.executeAccessibleAction(AccessibleAction.SET_VALUE, 73.0);
        assertEquals(70.0, slider.getValue(), 0.0001);

        slider.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(80.0, slider.getValue(), 0.0001);

        slider.executeAccessibleAction(AccessibleAction.BLOCK_INCREMENT);
        assertEquals(100.0, slider.getValue(), 0.0001);

        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertEquals(90.0, slider.getValue(), 0.0001);
    }

    /// Verifies that slider track layout remains bounded by the slider control.
    @Test
    void sliderTrackStaysInsideControlBoundsInFlowLayout() {
        M3CheckBox checkBox = new M3CheckBox("Checkbox");
        M3Slider slider = new M3Slider(0.0, 100.0, 64.0);
        slider.setPrefWidth(220.0);
        M3SegmentedButtonGroup segmentedButtons = segmentedButtonGroup(
                new M3SegmentedButton("Day"),
                new M3SegmentedButton("Week"),
                new M3SegmentedButton("Month")
        );
        FlowPane flow = new FlowPane(16.0, 16.0);
        flow.getChildren().addAll(checkBox, slider, segmentedButtons);
        Scene scene = new Scene(flow, 900.0, 160.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        flow.resize(900.0, 160.0);
        flow.applyCss();
        flow.layout();

        Node track = slider.lookup(".track");
        assertInstanceOf(Node.class, track);
        assertTrue(slider.getWidth() <= 220.0 + 0.0001);
        assertTrue(track.getBoundsInParent().getMinX() >= -0.0001);
        assertTrue(track.getBoundsInParent().getMaxX() <= slider.getWidth() + 0.0001);
    }

    /// Verifies that the slider skin uses Material handle geometry and centers feedback on the handle.
    @Test
    void sliderSkinUsesMaterialHandleGeometry() {
        FxTestUtils.runOnFxThread(() -> {
            M3Slider horizontal = new M3Slider(0.0, 100.0, 25.0);
            M3Slider vertical = new M3Slider(0.0, 100.0, 75.0);
            vertical.setOrientation(Orientation.VERTICAL);
            Pane root = new Pane(horizontal, vertical);
            Scene scene = new Scene(root, 320.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            horizontal.resize(220.0, 48.0);
            vertical.resize(48.0, 180.0);
            horizontal.layout();
            vertical.layout();

            Region horizontalTrack = lookupRegion(horizontal, ".track");
            Region horizontalThumb = lookupRegion(horizontal, ".thumb");
            Region horizontalStateLayer = lookupRegion(horizontal, ".m3-state-layer-container");
            assertEquals(16.0, horizontalTrack.getHeight(), 0.0001);
            assertEquals(4.0, horizontalThumb.getWidth(), 0.0001);
            assertEquals(44.0, horizontalThumb.getHeight(), 0.0001);
            assertEquals(horizontalTrack.getLayoutY() + horizontalTrack.getHeight() / 2.0,
                    horizontalThumb.getLayoutY() + horizontalThumb.getHeight() / 2.0,
                    0.0001);
            assertEquals(horizontalThumb.getLayoutX() + horizontalThumb.getWidth() / 2.0,
                    horizontalStateLayer.getLayoutX() + horizontalStateLayer.getWidth() / 2.0,
                    0.0001);
            assertEquals(horizontalThumb.getLayoutY() + horizontalThumb.getHeight() / 2.0,
                    horizontalStateLayer.getLayoutY() + horizontalStateLayer.getHeight() / 2.0,
                    0.0001);

            Region verticalTrack = lookupRegion(vertical, ".track");
            Region verticalThumb = lookupRegion(vertical, ".thumb");
            Region verticalStateLayer = lookupRegion(vertical, ".m3-state-layer-container");
            assertEquals(16.0, verticalTrack.getWidth(), 0.0001);
            assertEquals(44.0, verticalThumb.getWidth(), 0.0001);
            assertEquals(4.0, verticalThumb.getHeight(), 0.0001);
            assertEquals(verticalTrack.getLayoutX() + verticalTrack.getWidth() / 2.0,
                    verticalThumb.getLayoutX() + verticalThumb.getWidth() / 2.0,
                    0.0001);
            assertEquals(verticalThumb.getLayoutX() + verticalThumb.getWidth() / 2.0,
                    verticalStateLayer.getLayoutX() + verticalStateLayer.getWidth() / 2.0,
                    0.0001);
            assertEquals(verticalThumb.getLayoutY() + verticalThumb.getHeight() / 2.0,
                    verticalStateLayer.getLayoutY() + verticalStateLayer.getHeight() / 2.0,
                    0.0001);
        });
    }

    /// Verifies that the slider skin updates values from pointer and keyboard input.
    @Test
    void sliderSkinHandlesPointerAndKeyboardInput() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_PRESSED, 210.0, 24.0, true));
        assertTrue(slider.getValue() > 95.0);
        slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_RELEASED, 10.0, 24.0, false));
        assertTrue(slider.getValue() < 5.0);

        slider.setValue(50.0);
        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(slider.getValue() > 50.0);
        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertEquals(50.0, slider.getValue(), 0.0001);

        M3Slider rightToLeft = new M3Slider(0.0, 100.0, 50.0);
        rightToLeft.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane rightToLeftRoot = new Pane(rightToLeft);
        Scene rightToLeftScene = new Scene(rightToLeftRoot, 240.0, 80.0);

        M3ThemeManager.install(rightToLeftScene, M3Theme.defaultTheme());
        rightToLeftRoot.applyCss();
        rightToLeft.resize(220.0, 48.0);
        rightToLeft.layout();

        Bounds rightToLeftSceneBounds = rightToLeft.localToScene(rightToLeft.getBoundsInLocal());
        double rightToLeftSceneCenterY = rightToLeftSceneBounds.getCenterY();
        double rightEdgeSceneX = rightToLeftSceneBounds.getMaxX() - 10.0;
        rightToLeft.fireEvent(primaryMouseEventAtScenePoint(
                rightToLeft,
                MouseEvent.MOUSE_PRESSED,
                rightEdgeSceneX,
                rightToLeftSceneCenterY,
                true
        ));
        assertTrue(rightToLeft.getValue() < 5.0, () -> "value=" + rightToLeft.getValue());
        double leftEdgeSceneX = rightToLeftSceneBounds.getMinX() + 10.0;
        rightToLeft.fireEvent(primaryMouseEventAtScenePoint(
                rightToLeft,
                MouseEvent.MOUSE_RELEASED,
                leftEdgeSceneX,
                rightToLeftSceneCenterY,
                false
        ));
        assertTrue(rightToLeft.getValue() > 95.0, () -> "value=" + rightToLeft.getValue());

        rightToLeft.setValue(50.0);
        rightToLeft.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertTrue(rightToLeft.getValue() < 50.0);
    }

    /// Verifies that drag interactions snap the displayed slider position without animation lag.
    @Test
    void sliderSkinSnapsDisplayedPositionWhileDragging() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        Region thumb = lookupRegion(slider, ".thumb");
        slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_PRESSED, 210.0, 24.0, true));
        slider.layout();
        assertTrue(thumb.getLayoutX() > 190.0);

        slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_DRAGGED, 10.0, 24.0, true));
        slider.layout();
        assertTrue(thumb.getLayoutX() < 10.0);
    }

    /// Verifies that disabled slider skins ignore pointer and keyboard input.
    @Test
    void sliderSkinIgnoresInputWhileDisabled() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();
        slider.setValueChanging(true);

        slider.setDisable(true);
        slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_PRESSED, 210.0, 24.0, true));
        slider.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

        assertEquals(50.0, slider.getValue(), 0.0001);
        assertFalse(slider.isValueChanging());
        assertNull(slider.lookup(".m3-ripple"));
    }

    /// Verifies that disposed slider skins no longer receive disabled-state changes.
    @Test
    void sliderSkinRemovesDisabledListenerWhenDisposed() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);

        applyCss(slider);

        Skin<?> skin = slider.getSkin();
        skin.dispose();
        slider.setValueChanging(true);
        slider.setDisable(true);

        assertTrue(slider.isValueChanging());
    }

    /// Verifies that pressing a slider does not display a state layer or ripple.
    @Test
    void sliderPressDoesNotShowStateLayerOrRipple() {
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        Pane root = new Pane(slider);
        Scene scene = new Scene(root, 240.0, 80.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(220.0, 48.0);
        slider.layout();

        assertInstanceOf(Region.class, slider.lookup(".m3-state-layer"));
        slider.fireEvent(primaryMouseEvent(slider, MouseEvent.MOUSE_PRESSED, 110.0, 24.0, true));

        assertEquals(0.0, lookupRegion(slider, ".m3-state-layer").getOpacity(), 0.0001);
        assertNull(slider.lookup(".m3-ripple"));
    }

    /// Verifies a horizontal slider's rendered active and inactive segments around the handle gap.
    private static void assertHorizontalSliderSegments(M3Slider slider, boolean rightToLeft) {
        Region inactiveTrack = lookupRegion(slider, ".track");
        Region activeTrack = lookupRegion(slider, ".active-track");
        Region stopIndicator = lookupRegion(slider, ".stop-indicator");
        Region thumb = lookupRegion(slider, ".thumb");
        Bounds sliderBounds = slider.localToScene(slider.getBoundsInLocal());
        Bounds inactiveBounds = inactiveTrack.localToScene(inactiveTrack.getBoundsInLocal());
        Bounds activeBounds = activeTrack.localToScene(activeTrack.getBoundsInLocal());
        Bounds stopIndicatorBounds = stopIndicator.localToScene(stopIndicator.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        double trackStart = sliderBounds.getMinX() + slider.getThumbWidth() / 2.0;
        double trackEnd = sliderBounds.getMaxX() - slider.getThumbWidth() / 2.0;
        double trackLength = trackEnd - trackStart;
        double logicalPosition = normalizedSliderValue(slider);
        double visualPosition = rightToLeft ? 1.0 - logicalPosition : logicalPosition;
        double thumbCenter = trackStart + trackLength * visualPosition;
        double gapExtent = thumbBounds.getWidth() / 2.0 + slider.getThumbTrackGap();
        double leadingGapEdge = Math.max(trackStart, thumbCenter - gapExtent);
        double trailingGapEdge = Math.min(trackEnd, thumbCenter + gapExtent);

        assertEquals(thumbCenter, thumbBounds.getCenterX(), 0.0001);
        assertEquals(slider.getThumbWidth(), thumbBounds.getWidth(), 0.0001);
        if (rightToLeft) {
            assertEquals(trackStart, inactiveBounds.getMinX(), 0.0001);
            assertEquals(leadingGapEdge, inactiveBounds.getMaxX(), 0.0001);
            assertEquals(trailingGapEdge, activeBounds.getMinX(), 0.0001);
            assertEquals(trackEnd, activeBounds.getMaxX(), 0.0001);
        } else {
            assertEquals(trackStart, activeBounds.getMinX(), 0.0001);
            assertEquals(leadingGapEdge, activeBounds.getMaxX(), 0.0001);
            assertEquals(trailingGapEdge, inactiveBounds.getMinX(), 0.0001);
            assertEquals(trackEnd, inactiveBounds.getMaxX(), 0.0001);
        }
        double segmentGap = rightToLeft
                ? activeBounds.getMinX() - inactiveBounds.getMaxX()
                : inactiveBounds.getMinX() - activeBounds.getMaxX();
        assertEquals(thumbBounds.getWidth() + slider.getThumbTrackGap() * 2.0, segmentGap, 0.0001);
        double stopRadius = slider.getStopIndicatorSize() / 2.0;
        double expectedStopCenter = rightToLeft
                ? trackStart + slider.getStopIndicatorTrailingSpace() + stopRadius
                : trackEnd - slider.getStopIndicatorTrailingSpace() - stopRadius;
        assertTrue(stopIndicator.isVisible());
        assertEquals(expectedStopCenter, stopIndicatorBounds.getCenterX(), 0.0001);
        assertEquals(slider.getStopIndicatorSize(), stopIndicatorBounds.getWidth(), 0.0001);
        assertEquals(slider.getStopIndicatorSize(), stopIndicatorBounds.getHeight(), 0.0001);
    }

    /// Verifies a vertical slider's rendered active and inactive segments around the handle gap.
    private static void assertVerticalSliderSegments(M3Slider slider) {
        Region inactiveTrack = lookupRegion(slider, ".track");
        Region activeTrack = lookupRegion(slider, ".active-track");
        Region stopIndicator = lookupRegion(slider, ".stop-indicator");
        Region thumb = lookupRegion(slider, ".thumb");
        Bounds sliderBounds = slider.localToScene(slider.getBoundsInLocal());
        Bounds inactiveBounds = inactiveTrack.localToScene(inactiveTrack.getBoundsInLocal());
        Bounds activeBounds = activeTrack.localToScene(activeTrack.getBoundsInLocal());
        Bounds stopIndicatorBounds = stopIndicator.localToScene(stopIndicator.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        double trackStart = sliderBounds.getMinY() + slider.getThumbWidth() / 2.0;
        double trackEnd = sliderBounds.getMaxY() - slider.getThumbWidth() / 2.0;
        double trackLength = trackEnd - trackStart;
        double thumbCenter = trackEnd - trackLength * normalizedSliderValue(slider);
        double gapExtent = thumbBounds.getHeight() / 2.0 + slider.getThumbTrackGap();
        double upperGapEdge = Math.max(trackStart, thumbCenter - gapExtent);
        double lowerGapEdge = Math.min(trackEnd, thumbCenter + gapExtent);

        assertEquals(thumbCenter, thumbBounds.getCenterY(), 0.0001);
        assertEquals(slider.getThumbWidth(), thumbBounds.getHeight(), 0.0001);
        assertEquals(trackStart, inactiveBounds.getMinY(), 0.0001);
        assertEquals(upperGapEdge, inactiveBounds.getMaxY(), 0.0001);
        assertEquals(lowerGapEdge, activeBounds.getMinY(), 0.0001);
        assertEquals(trackEnd, activeBounds.getMaxY(), 0.0001);
        assertEquals(thumbBounds.getHeight() + slider.getThumbTrackGap() * 2.0,
                activeBounds.getMinY() - inactiveBounds.getMaxY(),
                0.0001);
        double stopRadius = slider.getStopIndicatorSize() / 2.0;
        assertTrue(stopIndicator.isVisible());
        assertEquals(
                trackStart + slider.getStopIndicatorTrailingSpace() + stopRadius,
                stopIndicatorBounds.getCenterY(),
                0.0001
        );
        assertEquals(slider.getStopIndicatorSize(), stopIndicatorBounds.getWidth(), 0.0001);
        assertEquals(slider.getStopIndicatorSize(), stopIndicatorBounds.getHeight(), 0.0001);
    }

    /// Verifies centered horizontal slider segments and end stops in rendered scene coordinates.
    private static void assertCenteredHorizontalSliderSegments(M3Slider slider, boolean rightToLeft) {
        List<Region> inactiveTracks = visibleSliderRegions(slider, ".track");
        List<Region> stopIndicators = visibleSliderRegions(slider, ".stop-indicator");
        inactiveTracks.sort((first, second) -> Double.compare(
                first.localToScene(first.getBoundsInLocal()).getMinX(),
                second.localToScene(second.getBoundsInLocal()).getMinX()
        ));
        stopIndicators.sort((first, second) -> Double.compare(
                first.localToScene(first.getBoundsInLocal()).getMinX(),
                second.localToScene(second.getBoundsInLocal()).getMinX()
        ));
        assertEquals(2, inactiveTracks.size());
        assertEquals(2, stopIndicators.size());

        Region activeTrack = lookupRegion(slider, ".active-track");
        Region thumb = lookupRegion(slider, ".thumb");
        Bounds sliderBounds = slider.localToScene(slider.getBoundsInLocal());
        Bounds leadingInactive = inactiveTracks.get(0).localToScene(inactiveTracks.get(0).getBoundsInLocal());
        Bounds trailingInactive = inactiveTracks.get(1).localToScene(inactiveTracks.get(1).getBoundsInLocal());
        Bounds activeBounds = activeTrack.localToScene(activeTrack.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        double trackStart = sliderBounds.getMinX() + slider.getThumbWidth() / 2.0;
        double trackEnd = sliderBounds.getMaxX() - slider.getThumbWidth() / 2.0;
        double trackCenter = (trackStart + trackEnd) / 2.0;
        double logicalPosition = normalizedSliderValue(slider);
        double visualPosition = rightToLeft ? 1.0 - logicalPosition : logicalPosition;
        double thumbCenter = trackStart + (trackEnd - trackStart) * visualPosition;
        double gapExtent = thumbBounds.getWidth() / 2.0 + slider.getThumbTrackGap();
        double activeStart = thumbCenter < trackCenter
                ? thumbCenter + gapExtent
                : trackCenter;
        double activeEnd = thumbCenter < trackCenter
                ? trackCenter
                : thumbCenter - gapExtent;

        assertEquals(thumbCenter, thumbBounds.getCenterX(), 0.0001);
        assertEquals(trackStart, leadingInactive.getMinX(), 0.0001);
        assertEquals(Math.min(trackCenter, thumbCenter - gapExtent),
                leadingInactive.getMaxX(), 0.0001);
        assertEquals(activeStart, activeBounds.getMinX(), 0.0001);
        assertEquals(activeEnd, activeBounds.getMaxX(), 0.0001);
        assertEquals(Math.max(trackCenter, thumbCenter + gapExtent),
                trailingInactive.getMinX(), 0.0001);
        assertEquals(trackEnd, trailingInactive.getMaxX(), 0.0001);
        assertRegionRadii(activeTrack, 0.0, 0.0, 0.0, 0.0);

        double stopRadius = slider.getStopIndicatorSize() / 2.0;
        double stopCenterOffset = slider.getStopIndicatorTrailingSpace() + stopRadius;
        Bounds leadingStop = stopIndicators.get(0).localToScene(stopIndicators.get(0).getBoundsInLocal());
        Bounds trailingStop = stopIndicators.get(1).localToScene(stopIndicators.get(1).getBoundsInLocal());
        assertEquals(trackStart + stopCenterOffset, leadingStop.getCenterX(), 0.0001);
        assertEquals(trackEnd - stopCenterOffset, trailingStop.getCenterX(), 0.0001);
    }

    /// Verifies centered vertical slider segments and end stops in rendered scene coordinates.
    private static void assertCenteredVerticalSliderSegments(M3Slider slider) {
        List<Region> inactiveTracks = visibleSliderRegions(slider, ".track");
        List<Region> stopIndicators = visibleSliderRegions(slider, ".stop-indicator");
        inactiveTracks.sort((first, second) -> Double.compare(
                first.localToScene(first.getBoundsInLocal()).getMinY(),
                second.localToScene(second.getBoundsInLocal()).getMinY()
        ));
        stopIndicators.sort((first, second) -> Double.compare(
                first.localToScene(first.getBoundsInLocal()).getMinY(),
                second.localToScene(second.getBoundsInLocal()).getMinY()
        ));
        assertEquals(2, inactiveTracks.size());
        assertEquals(2, stopIndicators.size());

        Region activeTrack = lookupRegion(slider, ".active-track");
        Region thumb = lookupRegion(slider, ".thumb");
        Bounds sliderBounds = slider.localToScene(slider.getBoundsInLocal());
        Bounds upperInactive = inactiveTracks.get(0).localToScene(inactiveTracks.get(0).getBoundsInLocal());
        Bounds lowerInactive = inactiveTracks.get(1).localToScene(inactiveTracks.get(1).getBoundsInLocal());
        Bounds activeBounds = activeTrack.localToScene(activeTrack.getBoundsInLocal());
        Bounds thumbBounds = thumb.localToScene(thumb.getBoundsInLocal());
        double trackStart = sliderBounds.getMinY() + slider.getThumbWidth() / 2.0;
        double trackEnd = sliderBounds.getMaxY() - slider.getThumbWidth() / 2.0;
        double trackCenter = (trackStart + trackEnd) / 2.0;
        double thumbCenter = trackEnd - (trackEnd - trackStart) * normalizedSliderValue(slider);
        double gapExtent = thumbBounds.getHeight() / 2.0 + slider.getThumbTrackGap();
        double activeStart = thumbCenter < trackCenter
                ? thumbCenter + gapExtent
                : trackCenter;
        double activeEnd = thumbCenter < trackCenter
                ? trackCenter
                : thumbCenter - gapExtent;

        assertEquals(thumbCenter, thumbBounds.getCenterY(), 0.0001);
        assertEquals(trackStart, upperInactive.getMinY(), 0.0001);
        assertEquals(Math.min(trackCenter, thumbCenter - gapExtent),
                upperInactive.getMaxY(), 0.0001);
        assertEquals(activeStart, activeBounds.getMinY(), 0.0001);
        assertEquals(activeEnd, activeBounds.getMaxY(), 0.0001);
        assertEquals(Math.max(trackCenter, thumbCenter + gapExtent),
                lowerInactive.getMinY(), 0.0001);
        assertEquals(trackEnd, lowerInactive.getMaxY(), 0.0001);

        double stopRadius = slider.getStopIndicatorSize() / 2.0;
        double stopCenterOffset = slider.getStopIndicatorTrailingSpace() + stopRadius;
        Bounds upperStop = stopIndicators.get(0).localToScene(stopIndicators.get(0).getBoundsInLocal());
        Bounds lowerStop = stopIndicators.get(1).localToScene(stopIndicators.get(1).getBoundsInLocal());
        assertEquals(trackStart + stopCenterOffset, upperStop.getCenterY(), 0.0001);
        assertEquals(trackEnd - stopCenterOffset, lowerStop.getCenterY(), 0.0001);
    }

    /// Returns visible slider regions matching a CSS selector.
    private static List<Region> visibleSliderRegions(M3Slider slider, String selector) {
        List<Region> regions = new ArrayList<>();
        for (Node node : slider.lookupAll(selector)) {
            if (node instanceof Region region && region.isVisible()) {
                regions.add(region);
            }
        }
        return regions;
    }

    /// Returns the slider value normalized to the supported range.
    private static double normalizedSliderValue(M3Slider slider) {
        double min = slider.getMin();
        double max = slider.getMax();
        if (max <= min) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (slider.getValue() - min) / (max - min)));
    }

    /// Verifies that vertical slider track segments use the caller-provided preferred height.
    private static void assertVerticalSliderTrackUsesExplicitHeight(M3Slider slider) {
        Region inactiveTrack = lookupRegion(slider, ".track");
        Region activeTrack = lookupRegion(slider, ".active-track");
        double coveredHeight = inactiveTrack.getBoundsInParent().getHeight()
                + activeTrack.getBoundsInParent().getHeight()
                + slider.getThumbWidth()
                + slider.getThumbTrackGap() * 2.0;
        assertTrue(coveredHeight >= 140.0, () -> "vertical slider track is too short: " + coveredHeight);
    }

    /// Creates a primary mouse event at one point expressed in scene coordinates.
    private static MouseEvent primaryMouseEventAtScenePoint(
            Node node,
            EventType<MouseEvent> eventType,
            double sceneX,
            double sceneY,
            boolean primaryButtonDown
    ) {
        Point2D localPoint = node.sceneToLocal(sceneX, sceneY);
        return primaryMouseEvent(node, eventType, localPoint.getX(), localPoint.getY(), primaryButtonDown);
    }
}
