// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Meter;
import org.glavo.m3fx.controls.M3MeterSize;
import org.glavo.m3fx.controls.M3MeterVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Meters extension showcase page.
@NotNullByDefault
final class MetersDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    MetersDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the meter extension page.
    ///
    /// @return the complete meter showcase
    Node createContent() {
        M3Meter wrapped = meter(
                "Tutorials completed across the current learning pathway",
                0.25,
                "2 of 8",
                M3MeterVariant.POSITIVE,
                M3MeterSize.LARGE,
                false,
                "demo-meter-wrapped"
        );
        wrapped.setPrefWidth(176.0);
        wrapped.setMaxWidth(176.0);

        return createGallery(
                createFullWidthShowcaseGroup(
                        "Semantic Variants",
                        meterStack(
                                meter("Tutorials completed", 0.50, "4 of 8", M3MeterVariant.INFORMATIVE,
                                        M3MeterSize.LARGE, false, "demo-meter-semantic"),
                                meter("Storage remaining", 0.72, "72%", M3MeterVariant.POSITIVE,
                                        M3MeterSize.LARGE, false, "demo-meter-semantic"),
                                meter("Storage used", 0.80, "80%", M3MeterVariant.NOTICE,
                                        M3MeterSize.LARGE, false, "demo-meter-semantic"),
                                meter("Storage used", 0.94, "94%", M3MeterVariant.NEGATIVE,
                                        M3MeterSize.LARGE, false, "demo-meter-semantic")
                        )
                ),
                createShowcaseGroup(
                        "Sizes",
                        meter("Large meter", 0.62, "62%", M3MeterVariant.INFORMATIVE,
                                M3MeterSize.LARGE, false, "demo-meter-size"),
                        meter("Small meter", 0.62, "62%", M3MeterVariant.INFORMATIVE,
                                M3MeterSize.SMALL, false, "demo-meter-size")
                ),
                createFullWidthShowcaseGroup(
                        "Label Placement",
                        meter("Top label", 0.68, "68%", M3MeterVariant.INFORMATIVE,
                                M3MeterSize.LARGE, false, "demo-meter-top-label"),
                        meter("Side label", 0.68, "68%", M3MeterVariant.INFORMATIVE,
                                M3MeterSize.LARGE, true, "demo-meter-side-label")
                ),
                createShowcaseGroup("Text Overflow", wrapped)
        );
    }

    /// Creates a vertical stack of related meter samples.
    ///
    /// @param meters the meters to arrange
    /// @return the configured stack
    private static VBox meterStack(M3Meter... meters) {
        VBox stack = new VBox(18.0, meters);
        stack.setFillWidth(false);
        stack.getStyleClass().add("demo-meter-stack");
        return stack;
    }

    /// Creates one configured meter sample.
    ///
    /// @param label the measured quantity label
    /// @param value the normalized measured value
    /// @param valueText the displayed value text
    /// @param variant the semantic variant
    /// @param size the visual size
    /// @param sideLabel whether labels appear beside the track
    /// @param styleClass the demo style class identifying the sample role
    /// @return the configured meter
    private static M3Meter meter(
            String label,
            double value,
            String valueText,
            M3MeterVariant variant,
            M3MeterSize size,
            boolean sideLabel,
            String styleClass
    ) {
        M3Meter meter = new M3Meter(label, value);
        meter.setValueText(valueText);
        meter.setVariant(variant);
        meter.setSize(size);
        meter.setSideLabel(sideLabel);
        meter.setPrefWidth(sideLabel ? 420.0 : 280.0);
        meter.setMaxWidth(sideLabel ? 520.0 : 360.0);
        meter.getStyleClass().add(styleClass);
        return meter;
    }
}
