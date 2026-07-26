// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorArea;
import org.glavo.m3fx.controls.M3ColorChannel;
import org.glavo.m3fx.controls.M3ColorField;
import org.glavo.m3fx.controls.M3ColorPicker;
import org.glavo.m3fx.controls.M3ColorPlane;
import org.glavo.m3fx.controls.M3ColorSlider;
import org.glavo.m3fx.controls.M3ColorSpace;
import org.glavo.m3fx.controls.M3ColorSwatch;
import org.glavo.m3fx.controls.M3ColorSwatchPicker;
import org.glavo.m3fx.controls.M3ColorSwatchRounding;
import org.glavo.m3fx.controls.M3ColorSwatchSize;
import org.glavo.m3fx.controls.M3ColorWheel;
import org.glavo.m3fx.controls.M3HsbColor;
import org.glavo.m3fx.controls.M3HslColor;
import org.glavo.m3fx.controls.M3RgbColor;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/// Builds the color-control showcase page.
@NotNullByDefault
final class ColorPickersDemoPage extends DemoPageSupport {
    /// The shared initial color used by interactive primitive examples.
    private static final M3Color INITIAL_COLOR = new M3HsbColor(268.0, 0.62, 0.76);

    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ColorPickersDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the color-picker showcase.
    ///
    /// @return the complete page content
    Node createContent() {
        M3ColorPicker picker = new M3ColorPicker(INITIAL_COLOR);
        picker.setShowColorWheel(true);
        picker.getPresets().setAll(createPalette());
        configureResponsiveWidth(picker, 540.0);

        M3ColorArea hsbArea = new M3ColorArea(INITIAL_COLOR);
        hsbArea.setPlane(M3ColorPlane.HSB_SATURATION_BRIGHTNESS);
        hsbArea.setPrefSize(260.0, 172.0);

        M3ColorArea hslArea = new M3ColorArea(INITIAL_COLOR.toColorSpace(
                M3ColorSpace.HSL
        ));
        hslArea.setPlane(M3ColorPlane.HSL_SATURATION_LIGHTNESS);
        hslArea.setPrefSize(260.0, 172.0);

        M3ColorArea rgbArea = new M3ColorArea(INITIAL_COLOR.toColorSpace(
                M3ColorSpace.RGB
        ));
        rgbArea.setPlane(M3ColorPlane.RGB_RED_GREEN);
        rgbArea.setPrefSize(260.0, 172.0);

        M3ColorWheel wheel = new M3ColorWheel(INITIAL_COLOR);
        wheel.setPrefSize(176.0, 176.0);

        M3ColorSlider hue = createSlider(M3ColorChannel.HUE);
        M3ColorSlider saturation = createSlider(M3ColorChannel.SATURATION);
        M3ColorSlider brightness = createSlider(M3ColorChannel.BRIGHTNESS);
        M3ColorSlider alpha = createSlider(M3ColorChannel.ALPHA);

        M3ColorSlider verticalHue = createSlider(M3ColorChannel.HUE);
        verticalHue.setOrientation(Orientation.VERTICAL);
        verticalHue.setPrefSize(48.0, 188.0);

        M3ColorField opaqueField = new M3ColorField(new M3RgbColor(0.22, 0.47, 0.82));
        M3ColorField alphaField = new M3ColorField(new M3HslColor(284.0, 0.58, 0.48, 0.56));
        alphaField.setIncludeAlpha(true);
        M3ColorField disabledField = new M3ColorField(new M3RgbColor(0.45, 0.45, 0.45));
        disabledField.setDisable(true);

        M3ColorSwatchPicker swatchPicker = new M3ColorSwatchPicker();
        swatchPicker.getItems().setAll(createPalette());
        swatchPicker.select(4);
        swatchPicker.setColumnCount(8);

        VBox swatchSizes = new VBox(12.0);
        swatchSizes.setAlignment(Pos.CENTER_LEFT);
        for (M3ColorSwatchSize size : M3ColorSwatchSize.values()) {
            HBox row = new HBox(12.0);
            row.setAlignment(Pos.CENTER_LEFT);
            M3ColorSwatch rounded = new M3ColorSwatch(INITIAL_COLOR);
            rounded.setSize(size);
            M3ColorSwatch square = new M3ColorSwatch(new M3HslColor(148.0, 0.52, 0.43, 0.62));
            square.setSize(size);
            square.setRounding(M3ColorSwatchRounding.NONE);
            M3ColorSwatch full = new M3ColorSwatch(new M3HsbColor(34.0, 0.84, 0.95));
            full.setSize(size);
            full.setRounding(M3ColorSwatchRounding.FULL);
            row.getChildren().addAll(rounded, square, full);
            swatchSizes.getChildren().add(row);
        }

        M3ColorSwatch noColor = new M3ColorSwatch();
        M3ColorSwatch translucent = new M3ColorSwatch(new M3RgbColor(0.16, 0.55, 0.76, 0.38));
        M3ColorSwatch named = new M3ColorSwatch(new M3HslColor(350.0, 0.74, 0.50));
        named.setColorName("Brand red");

        M3Text selectedValue = new M3Text("", M3TextRole.BODY_LARGE);
        updateSelectedValue(selectedValue, swatchPicker.getSelectedColor());
        swatchPicker.selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                updateSelectedValue(selectedValue, swatchPicker.getSelectedColor()));

        return createGallery(
                createFullWidthShowcaseGroup("Complete Picker", picker),
                createShowcaseGroup(
                        "Color Areas",
                        labeled("HSB saturation and brightness", hsbArea),
                        labeled("HSL saturation and lightness", hslArea),
                        labeled("RGB red and green", rgbArea)
                ),
                createShowcaseGroup(
                        "Hue Controls",
                        labeled("Wheel", wheel),
                        labeled("Vertical slider", verticalHue)
                ),
                createFullWidthShowcaseGroup(
                        "Channel Sliders",
                        labeled("Hue", hue),
                        labeled("Saturation", saturation),
                        labeled("Brightness", brightness),
                        labeled("Alpha", alpha)
                ),
                createShowcaseGroup(
                        "Hexadecimal Fields",
                        labeled("Opaque", opaqueField),
                        labeled("With alpha", alphaField),
                        labeled("Disabled", disabledField)
                ),
                createFullWidthShowcaseGroup("Swatch Picker", swatchPicker, selectedValue),
                createShowcaseGroup("Swatch Sizes And Shapes", swatchSizes),
                createShowcaseGroup("Transparency And No Color", translucent, noColor, named)
        );
    }

    /// Creates a horizontal channel slider initialized to the shared sample color.
    ///
    /// @param channel the channel represented by the slider
    /// @return the configured slider
    private static M3ColorSlider createSlider(M3ColorChannel channel) {
        M3ColorSlider slider = new M3ColorSlider(channel);
        slider.setValue(INITIAL_COLOR);
        configureResponsiveWidth(slider, 420.0);
        return slider;
    }

    /// Places a concise sample label above one control.
    ///
    /// @param labelText the label presented above the sample
    /// @param sample    the sample node
    /// @return the labeled sample container
    private static VBox labeled(String labelText, Node sample) {
        M3Text label = new M3Text(labelText, M3TextRole.LABEL_MEDIUM);
        VBox container = new VBox(8.0, label, sample);
        container.setMinWidth(0.0);
        return container;
    }

    /// Creates the sample preset palette.
    ///
    /// @return a new palette array
    private static M3Color[] createPalette() {
        return new M3Color[]{
                new M3HsbColor(0.0, 0.74, 0.83),
                new M3HsbColor(28.0, 0.82, 0.94),
                new M3HsbColor(52.0, 0.72, 0.96),
                new M3HsbColor(126.0, 0.59, 0.67),
                new M3HsbColor(174.0, 0.67, 0.68),
                new M3HsbColor(216.0, 0.68, 0.86),
                new M3HsbColor(267.0, 0.56, 0.76),
                new M3HsbColor(326.0, 0.60, 0.82)
        };
    }

    /// Updates the textual representation of the selected palette color.
    ///
    /// @param text the text node to update
    /// @param color the selected color, or `null` when selection is empty
    private static void updateSelectedValue(M3Text text, @Nullable M3Color color) {
        text.setText(color == null ? "No preset selected" : formatHex(color));
    }

    /// Formats one rendered color as an opaque hexadecimal RGB value.
    ///
    /// @param color the color to format
    /// @return the uppercase `#RRGGBB` representation
    private static String formatHex(M3Color color) {
        javafx.scene.paint.Color rendered = color.toFxColor();
        return String.format(
                Locale.ROOT,
                "#%02X%02X%02X",
                Math.round(rendered.getRed() * 255.0),
                Math.round(rendered.getGreen() * 255.0),
                Math.round(rendered.getBlue() * 255.0)
        );
    }
}
