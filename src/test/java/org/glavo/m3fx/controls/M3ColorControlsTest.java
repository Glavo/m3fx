// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.paint.Color;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3ColorMath;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies color-model invariants and the public contracts shared by the color-selection controls.
@NotNullByDefault
final class M3ColorControlsTest {
    /// The tolerance used for color-space round trips.
    private static final double COLOR_TOLERANCE = 1e-9;

    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies canonical RGB, HSL, and HSB conversions with alpha preservation.
    @Test
    void colorSpacesConvertWithoutChangingRenderedColor() {
        M3Color[] colors = {
                new M3RgbColor(0.17, 0.48, 0.83, 0.64),
                new M3HslColor(132.0, 0.71, 0.38, 0.42),
                new M3HsbColor(287.0, 0.58, 0.77, 0.91)
        };

        for (M3Color source : colors) {
            for (M3ColorSpace target : M3ColorSpace.values()) {
                M3Color converted = source.toColorSpace(target);
                assertEquals(target, converted.getColorSpace());
                assertEquals(source.getAlpha(), converted.getAlpha(), COLOR_TOLERANCE);
                assertRenderedColorEquals(source.toFxColor(), converted.toFxColor());
                List<M3ColorChannel> channels = target.getChannels();
                assertEquals(
                        toArgb(converted.toFxColor()),
                        M3ColorMath.toArgb(
                                target,
                                converted.getChannel(channels.get(0)),
                                converted.getChannel(channels.get(1)),
                                converted.getChannel(channels.get(2)),
                                converted.getAlpha()
                        )
                );
            }
        }

        assertRenderedColorEquals(Color.RED, new M3HsbColor(0.0, 1.0, 1.0).toFxColor());
        assertRenderedColorEquals(Color.color(0.0, 0.5, 0.0), new M3HslColor(120.0, 1.0, 0.25).toFxColor());
    }

    /// Verifies that rendered-color equivalence uses one deterministic, transitive canonical key.
    @Test
    void renderedColorEquivalenceUsesCanonicalKeys() {
        M3Color rgbRed = new M3RgbColor(1.0, 0.0, 0.0);
        M3Color hslRed = new M3HslColor(0.0, 1.0, 0.5);
        M3Color hsbRed = new M3HsbColor(360.0, 1.0, 1.0);
        assertTrue(rgbRed.isEquivalentTo(hslRed));
        assertTrue(hslRed.isEquivalentTo(hsbRed));
        assertTrue(rgbRed.isEquivalentTo(hsbRed));
        assertEquals(M3ColorMath.canonicalRgbaKey(rgbRed), M3ColorMath.canonicalRgbaKey(hsbRed));

        M3Color nearBlack = new M3RgbColor(0.49 / 65535.0, 0.0, 0.0);
        M3Color distinguishableBlack = new M3RgbColor(0.51 / 65535.0, 0.0, 0.0);
        assertTrue(new M3RgbColor(0.0, 0.0, 0.0).isEquivalentTo(nearBlack));
        assertFalse(nearBlack.isEquivalentTo(distinguishableBlack));
    }

    /// Verifies that achromatic HSL and HSB values retain latent hue during subsequent edits.
    @Test
    void editingColorSpacesRetainLatentHue() {
        M3HsbColor black = new M3HsbColor(287.0, 0.0, 0.0);
        M3HsbColor revealed = black
                .withChannel(M3ColorChannel.BRIGHTNESS, 0.72)
                .withChannel(M3ColorChannel.SATURATION, 0.81);
        assertEquals(287.0, revealed.hue());
        assertTrue(revealed.isEquivalentTo(new M3HsbColor(287.0, 0.81, 0.72)));

        M3HslColor gray = new M3HslColor(42.0, 0.0, 0.5);
        M3HslColor saturated = gray.withChannel(M3ColorChannel.SATURATION, 0.65);
        assertEquals(42.0, saturated.hue());
    }

    /// Verifies hexadecimal parsing, canonical formatting, and unsupported input rejection.
    @Test
    void hexadecimalCodecSupportsSpectrumFieldForms() {
        M3RgbColor shortRgba = Objects.requireNonNull(M3ColorMath.parseHex("#369C"));
        assertEquals(0x33 / 255.0, shortRgba.red(), COLOR_TOLERANCE);
        assertEquals(0x66 / 255.0, shortRgba.green(), COLOR_TOLERANCE);
        assertEquals(0x99 / 255.0, shortRgba.blue(), COLOR_TOLERANCE);
        assertEquals(0xCC / 255.0, shortRgba.alpha(), COLOR_TOLERANCE);

        M3RgbColor longRgba = Objects.requireNonNull(M3ColorMath.parseHex("336699cc"));
        assertEquals("#336699CC", M3ColorMath.formatHex(longRgba, true));
        assertEquals("#336699", M3ColorMath.formatHex(longRgba, false));
        assertNull(M3ColorMath.parseHex("#12"));
        assertNull(M3ColorMath.parseHex("#GGGGGG"));
    }

    /// Verifies channel ranges and area-plane structural constraints.
    @Test
    void channelsAndPlanesEnforceTheirDomains() {
        assertEquals(180.0, M3ColorChannel.HUE.fromPosition(0.5));
        assertEquals(1.0, M3ColorChannel.ALPHA.constrain(2.0));
        assertEquals(0.0, M3ColorChannel.SATURATION.constrain(-1.0));
        assertThrows(IllegalArgumentException.class, () -> M3ColorChannel.RED.constrain(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new M3RgbColor(-0.1, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new M3HslColor(361.0, 0.5, 0.5));

        assertEquals(
                M3ColorChannel.HUE,
                M3ColorPlane.HSB_SATURATION_BRIGHTNESS.fixedChannel()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new M3ColorPlane(M3ColorSpace.HSB, M3ColorChannel.HUE, M3ColorChannel.ALPHA)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new M3ColorPlane(M3ColorSpace.RGB, M3ColorChannel.RED, M3ColorChannel.HUE)
        );
    }

    /// Verifies picker selection behavior across equivalent colors and list mutations.
    @Test
    void swatchPickerKeepsSelectionStableAcrossListChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorSwatchPicker picker = new M3ColorSwatchPicker();
            M3Color redRgb = new M3RgbColor(1.0, 0.0, 0.0);
            M3Color green = new M3HsbColor(120.0, 1.0, 1.0);
            M3Color blue = new M3HslColor(240.0, 1.0, 0.5);
            picker.getItems().setAll(redRgb, green, blue);

            picker.selectColor(new M3HsbColor(0.0, 1.0, 1.0));
            assertEquals(0, picker.getSelectedIndex());
            assertSame(redRgb, picker.getSelectedColor());
            assertSame(redRgb, picker.selectedColorProperty().get());

            picker.select(2);
            picker.setAllowEmptySelection(true);
            picker.select(2);
            assertEquals(2, picker.getSelectedIndex());
            picker.getItems().add(0, new M3RgbColor(0.0, 0.0, 0.0));
            assertEquals(3, picker.getSelectedIndex());
            assertSame(blue, picker.getSelectedColor());

            picker.getItems().remove(0);
            assertEquals(2, picker.getSelectedIndex());
            assertSame(blue, picker.getSelectedColor());

            picker.getItems().remove(2);
            assertEquals(-1, picker.getSelectedIndex());
            assertNull(picker.getSelectedColor());
        });
    }

    /// Verifies canonical color uniqueness and atomic bulk mutations.
    @Test
    void paletteListsRejectEquivalentColorsWithoutPartialMutation() {
        FxTestUtils.runOnFxThread(() -> {
            M3Color red = new M3RgbColor(1.0, 0.0, 0.0);
            M3Color green = new M3HsbColor(120.0, 1.0, 1.0);
            M3Color blue = new M3HslColor(240.0, 1.0, 0.5);
            M3ColorSwatchPicker picker = new M3ColorSwatchPicker();
            picker.getItems().setAll(red, green, blue);
            picker.select(0);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> picker.getItems().add(new M3HsbColor(0.0, 1.0, 1.0))
            );
            assertIterableEquals(List.of(red, green, blue), picker.getItems());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> picker.getItems().addAll(
                            new M3RgbColor(1.0, 1.0, 0.0),
                            new M3HslColor(120.0, 1.0, 0.5)
                    )
            );
            assertIterableEquals(List.of(red, green, blue), picker.getItems());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> picker.getItems().replaceAll(color -> red)
            );
            assertIterableEquals(List.of(red, green, blue), picker.getItems());

            M3Color replacementRed = new M3HslColor(0.0, 1.0, 0.5);
            picker.getItems().set(0, replacementRed);
            assertSame(replacementRed, picker.getSelectedColor());

            M3ColorPicker composed = new M3ColorPicker();
            composed.getPresets().setAll(red, green);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> composed.getPresets().setAll(red, new M3HsbColor(0.0, 1.0, 1.0))
            );
            assertIterableEquals(List.of(red, green), composed.getPresets());
        });
    }

    /// Verifies defaults, JavaFX property ownership, and standalone stylesheet availability.
    @Test
    void colorControlsExposeOwnedPropertiesAndStylesheets() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorArea area = new M3ColorArea();
            M3ColorSlider slider = new M3ColorSlider();
            M3ColorWheel wheel = new M3ColorWheel();
            M3ColorField field = new M3ColorField();
            M3ColorSwatch swatch = new M3ColorSwatch();
            M3ColorSwatchPicker swatchPicker = new M3ColorSwatchPicker();
            M3ColorPicker picker = new M3ColorPicker();

            assertSame(area, area.valueProperty().getBean());
            assertSame(area, area.planeProperty().getBean());
            assertSame(slider, slider.valueProperty().getBean());
            assertSame(slider, slider.channelProperty().getBean());
            assertSame(wheel, wheel.valueProperty().getBean());
            assertSame(field, field.valueProperty().getBean());
            assertSame(field, field.textProperty().getBean());
            assertSame(field, field.invalidProperty().getBean());
            assertSame(swatch, swatch.colorProperty().getBean());
            assertSame(swatchPicker, swatchPicker.selectedIndexProperty().getBean());
            assertSame(swatchPicker, swatchPicker.selectedColorProperty().getBean());
            assertSame(swatchPicker, swatchPicker.roundingProperty().getBean());
            assertSame(picker, picker.valueProperty().getBean());
            assertSame(picker, picker.planeProperty().getBean());

            assertTrue(area.isFocusTraversable());
            assertTrue(slider.isFocusTraversable());
            assertTrue(wheel.isFocusTraversable());
            assertFalse(field.isFocusTraversable());
            assertFalse(swatch.isFocusTraversable());
            assertFalse(picker.isFocusTraversable());

            assertStylesheet(area);
            assertStylesheet(slider);
            assertStylesheet(wheel);
            assertStylesheet(field);
            assertStylesheet(swatch);
            assertStylesheet(swatchPicker);
            assertStylesheet(picker);

            assertThrows(NullPointerException.class, () -> picker.setValue(null));
            assertThrows(NullPointerException.class, () -> area.setPlane(null));
            assertThrows(IllegalArgumentException.class, () -> swatchPicker.setColumnCount(0));
            assertEquals(M3ColorSwatchRounding.NONE, swatchPicker.getRounding());
            assertThrows(NullPointerException.class, () -> swatchPicker.setRounding(null));
        });
    }

    /// Verifies that color-field editing and validation remain control-owned without a skin.
    @Test
    void colorFieldOwnsTransientEditingState() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorField field = new M3ColorField(new M3HslColor(210.0, 0.50, 0.40, 0.60));
            field.setIncludeAlpha(true);

            field.setText("#33669980");
            assertTrue(field.commit());
            assertEquals(M3ColorSpace.HSL, field.getValue().getColorSpace());
            assertEquals(128.0 / 255.0, field.getValue().getAlpha(), 1.0 / 255.0);
            assertFalse(field.isInvalid());

            M3Color committed = field.getValue();
            field.setText("#NOT-A-COLOR");
            assertFalse(field.commit());
            assertSame(committed, field.getValue());
            assertTrue(field.isInvalid());

            field.setText("#33669980");
            assertFalse(field.isInvalid());
            field.setText("invalid");
            field.cancelEdit();
            assertEquals("#33669980", field.getText());
            assertFalse(field.isInvalid());
        });
    }

    /// Verifies that a bound committed value rejects edits without partially changing field state.
    @Test
    void colorFieldCommitIsAtomicWhenValueIsBound() {
        FxTestUtils.runOnFxThread(() -> {
            M3Color initial = new M3HsbColor(30.0, 0.50, 0.80);
            SimpleObjectProperty<M3Color> source = new SimpleObjectProperty<>(initial);
            M3ColorField field = new M3ColorField();
            field.valueProperty().bind(source);
            field.setText("  #336699  ");

            assertThrows(RuntimeException.class, field::commit);
            assertSame(initial, field.getValue());
            assertEquals("  #336699  ", field.getText());
            assertFalse(field.isInvalid());
        });
    }

    /// Verifies component-picker defaults and live preset semantics.
    @Test
    void composedPickerUsesIndependentVisibilityPoliciesAndLivePresets() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorPicker picker = new M3ColorPicker(new M3HslColor(220.0, 0.5, 0.4, 0.7));
            assertTrue(picker.isShowAlpha());
            assertFalse(picker.isShowColorWheel());
            assertTrue(picker.isShowColorField());
            assertTrue(picker.isShowPresets());
            assertTrue(picker.getPresets().isEmpty());

            M3Color preset = new M3HsbColor(32.0, 0.7, 0.9);
            picker.getPresets().add(preset);
            assertSame(preset, picker.getPresets().get(0));
            assertThrows(NullPointerException.class, () -> picker.getPresets().add(null));

            picker.setShowAlpha(false);
            picker.setShowColorWheel(true);
            picker.setShowColorField(false);
            picker.setShowPresets(false);
            assertFalse(picker.isShowAlpha());
            assertTrue(picker.isShowColorWheel());
            assertFalse(picker.isShowColorField());
            assertFalse(picker.isShowPresets());
        });
    }

    /// Verifies that slider-role controls expose complete values and adjustment actions to assistive technology.
    @Test
    void sliderAndWheelExposeAccessibleRangesAndActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3ColorSlider slider = new M3ColorSlider(M3ColorChannel.SATURATION);
            slider.setValue(new M3HsbColor(240.0, 0.50, 0.75));
            assertEquals(0.0, slider.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
            assertEquals(1.0, slider.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
            assertEquals(0.50, slider.queryAccessibleAttribute(AccessibleAttribute.VALUE));
            assertEquals(Orientation.HORIZONTAL, slider.queryAccessibleAttribute(AccessibleAttribute.ORIENTATION));

            slider.executeAccessibleAction(AccessibleAction.INCREMENT);
            assertEquals(0.51, slider.getValue().getChannel(M3ColorChannel.SATURATION), COLOR_TOLERANCE);
            slider.executeAccessibleAction(AccessibleAction.SET_VALUE, 0.80);
            assertEquals(0.80, slider.getValue().getChannel(M3ColorChannel.SATURATION), COLOR_TOLERANCE);

            M3ColorWheel wheel = new M3ColorWheel(new M3HslColor(359.0, 0.70, 0.45));
            assertEquals(0.0, wheel.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
            assertEquals(360.0, wheel.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
            assertEquals(359.0, wheel.queryAccessibleAttribute(AccessibleAttribute.VALUE));
            wheel.executeAccessibleAction(AccessibleAction.INCREMENT);
            assertEquals(0.0, wheel.getValue().getChannel(M3ColorChannel.HUE), COLOR_TOLERANCE);
            assertEquals(M3ColorSpace.HSL, wheel.getValue().getColorSpace());
            wheel.executeAccessibleAction(AccessibleAction.SET_VALUE, 315.0);
            assertEquals(315.0, wheel.getValue().getChannel(M3ColorChannel.HUE), COLOR_TOLERANCE);
        });
    }

    /// Verifies one control stylesheet URL is non-empty.
    private static void assertStylesheet(javafx.scene.control.Control control) {
        String stylesheet = control.getUserAgentStylesheet();
        assertNotNull(stylesheet);
        assertFalse(stylesheet.isBlank());
    }

    /// Verifies every rendered RGBA component within the shared tolerance.
    private static void assertRenderedColorEquals(Color expected, Color actual) {
        assertEquals(expected.getRed(), actual.getRed(), COLOR_TOLERANCE);
        assertEquals(expected.getGreen(), actual.getGreen(), COLOR_TOLERANCE);
        assertEquals(expected.getBlue(), actual.getBlue(), COLOR_TOLERANCE);
        assertEquals(expected.getOpacity(), actual.getOpacity(), COLOR_TOLERANCE);
    }

    /// Packs a JavaFX color with the same nearest-byte rounding used by the raster renderer.
    ///
    /// @param color the color to pack
    /// @return the non-premultiplied ARGB pixel
    private static int toArgb(Color color) {
        return Math.round((float) (color.getOpacity() * 255.0)) << 24
                | Math.round((float) (color.getRed() * 255.0)) << 16
                | Math.round((float) (color.getGreen() * 255.0)) << 8
                | Math.round((float) (color.getBlue() * 255.0));
    }
}
