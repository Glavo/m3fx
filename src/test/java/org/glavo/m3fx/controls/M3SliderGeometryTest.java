// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies slider rendering and pointer mapping when a finite range has an overflowing span.
@NotNullByDefault
final class M3SliderGeometryTest {
    /// Starts JavaFX before creating skinned controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Places handles at the correct relative position in every supported axis direction.
    @ParameterizedTest
    @CsvSource({"false,false,false", "true,false,false", "false,false,true", "true,false,true",
            "false,true,false", "true,true,false"})
    void handlePositionsRemainFinite(boolean range, boolean vertical, boolean rtl) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Control slider = createSlider(range, vertical, rtl);
            try {
                if (range) {
                    assertHandlePosition(slider, ".range-low-thumb", 0.25, vertical);
                    assertHandlePosition(slider, ".range-high-thumb", 0.75, vertical);
                } else {
                    assertHandlePosition(slider, ".thumb", 0.5, vertical);
                }
            } finally {
                slider.setSkin(null);
            }
        });
    }

    /// Converts a track press to the requested finite value instead of an infinite endpoint.
    @ParameterizedTest
    @CsvSource({"false,false,false", "true,false,false", "false,false,true", "true,false,true",
            "false,true,false", "true,true,false"})
    void trackPressSelectsInteriorValue(boolean range, boolean vertical, boolean rtl) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Control slider = createSlider(range, vertical, rtl);
            try {
                if (slider instanceof M3RangeSlider ranged) {
                    ranged.setLowValue(-Double.MAX_VALUE);
                    ranged.setHighValue(Double.MAX_VALUE);
                } else {
                    ((M3Slider) slider).setValue(-Double.MAX_VALUE);
                }
                slider.layout();
                double axis = axisCoordinate(slider, 0.25, vertical);
                double x = vertical ? slider.getWidth() / 2.0 : axis;
                double y = vertical ? axis : slider.getHeight() / 2.0;
                Point2D point = slider.localToScene(x, y);
                slider.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED, point.getX(), point.getY(),
                        point.getX(), point.getY(), MouseButton.PRIMARY, 1,
                        false, false, false, false, true, false, false, false, false, false,
                        new PickResult(slider, point.getX(), point.getY())));
                double value = slider instanceof M3RangeSlider ranged ? ranged.getLowValue() : ((M3Slider) slider).getValue();
                assertEquals(-0.5, value / Double.MAX_VALUE, 1.0e-12);
            } finally {
                slider.setSkin(null);
            }
        });
    }

    /// Shows sparse discrete stops even when subtracting the bounds overflows.
    @ParameterizedTest
    @CsvSource({"false,false,false", "true,false,false", "false,false,true", "true,false,true",
            "false,true,false", "true,true,false"})
    void sparseStepIndicatorsRemainVisible(boolean range, boolean vertical, boolean rtl) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Control slider = createSlider(range, vertical, rtl);
            try {
                if (slider instanceof M3RangeSlider ranged) {
                    ranged.setLowValue(-Double.MAX_VALUE);
                    ranged.setHighValue(Double.MAX_VALUE);
                    ranged.setStepSize(Double.MAX_VALUE / 2.0);
                } else {
                    M3Slider scalar = (M3Slider) slider;
                    scalar.setValue(-Double.MAX_VALUE);
                    scalar.setStepSize(Double.MAX_VALUE / 2.0);
                }
                slider.layout();
                assertEquals(3L, slider.lookupAll(".step-indicator").stream().filter(Node::isVisible).count());
            } finally {
                slider.setSkin(null);
            }
        });
    }

    /// Hides crowded stops when multiplying the track length by the step would overflow.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void denseStepIndicatorsRespectPixelSpacing(boolean range) {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Control slider = createSlider(range, false, false);
            try {
                if (slider instanceof M3RangeSlider ranged) {
                    ranged.setMin(0.0);
                    ranged.setLowValue(0.0);
                    ranged.setHighValue(Double.MAX_VALUE);
                    ranged.setStepSize(Double.MAX_VALUE / 128.0);
                } else {
                    M3Slider scalar = (M3Slider) slider;
                    scalar.setMin(0.0);
                    scalar.setValue(0.0);
                    scalar.setStepSize(Double.MAX_VALUE / 128.0);
                }
                slider.layout();
                assertEquals(0L, slider.lookupAll(".step-indicator").stream().filter(Node::isVisible).count());
            } finally {
                slider.setSkin(null);
            }
        });
    }

    /// Creates a themed slider whose finite endpoints span more than the maximum finite double.
    private static Control createSlider(boolean range, boolean vertical, boolean rtl) {
        Control slider;
        Orientation orientation = vertical ? Orientation.VERTICAL : Orientation.HORIZONTAL;
        if (range) {
            M3RangeSlider ranged = new M3RangeSlider(-Double.MAX_VALUE, Double.MAX_VALUE,
                    -Double.MAX_VALUE / 2.0, Double.MAX_VALUE / 2.0);
            ranged.setOrientation(orientation);
            ranged.setShowValueIndicator(false);
            slider = ranged;
        } else {
            M3Slider scalar = new M3Slider(-Double.MAX_VALUE, Double.MAX_VALUE, 0.0);
            scalar.setOrientation(orientation);
            scalar.setShowValueIndicator(false);
            slider = scalar;
        }
        slider.setNodeOrientation(rtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        Pane root = new Pane(slider);
        M3ThemeManager.install(new Scene(root), M3Theme.defaultTheme());
        root.applyCss();
        slider.resize(vertical ? 64.0 : 320.0, vertical ? 320.0 : 64.0);
        slider.layout();
        return slider;
    }

    /// Compares a rendered handle center with its expected coordinate in slider space.
    private static void assertHandlePosition(Control slider, String selector, double fraction,
                                             boolean vertical) {
        Node thumb = Objects.requireNonNull(slider.lookup(selector), selector);
        Bounds bounds = slider.sceneToLocal(thumb.localToScene(thumb.getBoundsInLocal()));
        double center = vertical ? (bounds.getMinY() + bounds.getMaxY()) / 2.0 : (bounds.getMinX() + bounds.getMaxX()) / 2.0;
        assertEquals(axisCoordinate(slider, fraction, vertical), center, 1.0e-6);
    }

    /// Converts a normalized position to a coordinate along the inset track.
    private static double axisCoordinate(Control slider, double fraction, boolean vertical) {
        double thumbWidth = slider instanceof M3RangeSlider ranged ? ranged.getThumbWidth() : ((M3Slider) slider).getThumbWidth();
        double length = vertical ? slider.getHeight() : slider.getWidth();
        // JavaFX mirrors the node's local coordinate system for RTL; only the vertical axis is reversed here.
        double directed = vertical ? 1.0 - fraction : fraction;
        return thumbWidth / 2.0 + (length - thumbWidth) * directed;
    }
}
