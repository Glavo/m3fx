// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

import org.glavo.m3fx.controls.M3RangeSlider;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3SliderSize;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Sliders component showcase page.
@NotNullByDefault
final class SlidersDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SlidersDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the slider component page.
    Node createContent() {
        M3RangeSlider continuousRange = new M3RangeSlider(0.0, 100.0, 20.0, 78.0);
        continuousRange.setPrefWidth(300.0);

        M3RangeSlider discreteRange = new M3RangeSlider(0.0, 100.0, 30.0, 70.0);
        discreteRange.setStepSize(10.0);
        discreteRange.setSize(M3SliderSize.SMALL);
        discreteRange.setPrefWidth(300.0);

        M3RangeSlider indicatorRange = new M3RangeSlider(0.0, 100.0, 25.0, 65.0);
        indicatorRange.setShowValueIndicator(true);
        indicatorRange.setPrefWidth(300.0);

        M3RangeSlider disabledRange = new M3RangeSlider(0.0, 100.0, 35.0, 85.0);
        disabledRange.setDisable(true);
        disabledRange.setPrefWidth(300.0);

        M3Slider vertical = createSlider(48.0, false);
        vertical.setOrientation(Orientation.VERTICAL);
        vertical.setPrefSize(56.0, 180.0);

        M3Slider centeredNegative = new M3Slider(-100.0, 100.0, -45.0);
        centeredNegative.setCentered(true);
        centeredNegative.setPrefWidth(260.0);
        M3Slider centeredNeutral = new M3Slider(-100.0, 100.0, 0.0);
        centeredNeutral.setCentered(true);
        centeredNeutral.setPrefWidth(260.0);
        M3Slider centeredPositive = new M3Slider(-100.0, 100.0, 60.0);
        centeredPositive.setCentered(true);
        centeredPositive.setStepSize(20.0);
        centeredPositive.setPrefWidth(260.0);

        M3Slider centeredVertical = new M3Slider(-100.0, 100.0, -40.0);
        centeredVertical.setCentered(true);
        centeredVertical.setOrientation(Orientation.VERTICAL);
        centeredVertical.setPrefSize(56.0, 180.0);

        VBox sizeSamples = new VBox(12.0);
        M3SliderSize[] sizes = M3SliderSize.values();
        String[] sizeLabels = {"XS · 16 dp", "S · 24 dp", "M · 40 dp", "L · 56 dp", "XL · 96 dp"};
        for (int index = 0; index < sizes.length; index++) {
            M3Slider slider = createSlider(20.0 + index * 15.0, false);
            slider.setSize(sizes[index]);
            slider.setPrefWidth(360.0);
            if (index >= M3SliderSize.MEDIUM.ordinal()) {
                SVGPath activeIcon = DemoIcons.onPrimary("visibility");
                SVGPath inactiveIcon = DemoIcons.onSecondaryContainer("visibility");
                if (sizes[index] == M3SliderSize.EXTRA_LARGE) {
                    activeIcon.setScaleX(4.0 / 3.0);
                    activeIcon.setScaleY(4.0 / 3.0);
                    inactiveIcon.setScaleX(4.0 / 3.0);
                    inactiveIcon.setScaleY(4.0 / 3.0);
                }
                slider.setActiveTrackGraphic(activeIcon);
                slider.setInactiveTrackGraphic(inactiveIcon);
            }

            M3Text sizeLabel = new M3Text(sizeLabels[index], M3TextRole.LABEL_LARGE);
            sizeLabel.setMinWidth(88.0);
            sizeLabel.setPrefWidth(88.0);
            HBox row = new HBox(16.0, sizeLabel, slider);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(slider, Priority.ALWAYS);
            sizeSamples.getChildren().add(row);
        }

        M3Slider valueIndicator = createSlider(50.0, false);
        valueIndicator.setStepSize(10.0);
        valueIndicator.setShowValueIndicator(true);

        return createGallery(
                createShowcaseGroup(
                        "Continuous",
                        createSlider(24.0, false),
                        createSlider(64.0, false),
                        createSlider(50.0, true)
                ),
                createShowcaseGroup(
                        "Discrete",
                        createSteppedSlider(30.0, 10.0),
                        createSteppedSlider(70.0, 5.0)
                ),
                createShowcaseGroup(
                        "Centered",
                        centeredNegative,
                        centeredNeutral,
                        centeredPositive
                ),
                createShowcaseGroup(
                        "Range",
                        continuousRange,
                        discreteRange,
                        indicatorRange,
                        disabledRange
                ),
                createFullWidthShowcaseGroup("Expressive Sizes", sizeSamples),
                createShowcaseGroup("Value Indicator", valueIndicator),
                createShowcaseGroup("Vertical", vertical, centeredVertical)
        );
    }

    /// Creates a slider sample.
    private static M3Slider createSlider(double value, boolean disabled) {
        M3Slider slider = new M3Slider(0.0, 100.0, value);
        slider.setPrefWidth(260.0);
        slider.setDisable(disabled);
        return slider;
    }

    /// Creates a discrete slider sample.
    private static M3Slider createSteppedSlider(double value, double stepSize) {
        M3Slider slider = createSlider(value, false);
        slider.setStepSize(stepSize);
        return slider;
    }
}
