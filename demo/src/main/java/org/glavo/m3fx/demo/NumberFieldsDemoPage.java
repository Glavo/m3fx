// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.glavo.m3fx.controls.M3NumberField;
import org.glavo.m3fx.controls.M3NumberFieldCommitBehavior;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.text.NumberFormat;
import java.util.Locale;

/// Builds the Spectrum-inspired NumberField extension showcase page.
@NotNullByDefault
final class NumberFieldsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    NumberFieldsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the number-field showcase.
    ///
    /// @return the complete page content
    Node createContent() {
        M3NumberField filled = numberField(0.0, 1000.0, 240.0, 20.0, "Width");
        filled.setSupportingText("Use the buttons, arrow keys, or focused mouse wheel");

        M3NumberField outlined = numberField(-20.0, 40.0, 18.5, 0.5, "Temperature");
        outlined.setVariant(M3TextInputVariant.OUTLINED);
        outlined.setPrefix(new Label("°C"));
        outlined.setSupportingText("A non-interactive prefix shares the input container");

        M3NumberField validate = numberField(2.0, 200.0, 101.0, 3.0, "Validated amount");
        validate.setCommitBehavior(M3NumberFieldCommitBehavior.VALIDATE);
        validate.setSupportingText("Valid values start at 2 and advance by 3");

        M3NumberField percent = new M3NumberField(0.375);
        percent.setFormatter(NumberFormat.getPercentInstance(Locale.getDefault(Locale.Category.FORMAT)));
        percent.setStep(0.05);
        percent.setMin(0.0);
        percent.setMax(1.0);
        percent.setLabelText("Completion");
        percent.setSupportingText("Formatted with the current locale");
        percent.setHideStepper(true);
        configureResponsiveWidth(percent, 360.0);

        M3NumberField disabled = numberField(0.0, 1000.0, 400.0, 100.0, "Disabled");
        disabled.setDisable(true);

        M3NumberField readOnly = numberField(0.0, 1000.0, 700.0, 100.0, "Read only");
        readOnly.setEditable(false);

        return createGallery(
                createShowcaseGroup("Snap Commit", filled, outlined),
                createShowcaseGroup("Validate Commit", validate),
                createShowcaseGroup("Localized And Compact", percent),
                createShowcaseGroup("Unavailable", disabled, readOnly)
        );
    }

    /// Creates a configured number field with US decimal formatting for stable demonstration values.
    ///
    /// @param min       the inclusive minimum
    /// @param max       the inclusive maximum
    /// @param value     the initial value
    /// @param step      the step size
    /// @param labelText the floating label text
    /// @return the configured number field
    private static M3NumberField numberField(
            double min,
            double max,
            double value,
            double step,
            String labelText
    ) {
        M3NumberField field = new M3NumberField(min, max, value);
        field.setFormatter(NumberFormat.getNumberInstance(Locale.US));
        field.setStep(step);
        field.setLabelText(labelText);
        configureResponsiveWidth(field, 360.0);
        return field;
    }
}
