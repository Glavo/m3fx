// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.controls.M3TimePickerDialog;
import org.glavo.m3fx.controls.M3TimePickerField;
import org.glavo.m3fx.controls.M3TimePresets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.Objects;

/// Builds the TimePickers component showcase page.
@NotNullByDefault
final class TimePickersDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    TimePickersDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the time picker component page.
    Node createContent() {
        M3TimePickerField field = new M3TimePickerField(LocalTime.of(10, 30));
        field.setLabelText("Start time");
        field.setSupportingText("Editable 24-hour time with popup picker");
        field.setVariant(M3TextInputVariant.OUTLINED);
        field.getPicker().setUse24HourClock(true);
        field.getPicker().setMinuteStep(15);
        field.getPresets().setAll(M3TimePresets.common(LocalTime.of(10, 30)));
        field.setPrefWidth(320.0);
        field.setMaxWidth(320.0);

        M3TimePickerField boundedField = new M3TimePickerField(LocalTime.of(9, 30));
        boundedField.setLabelText("Office hours");
        boundedField.setSupportingText("Limited to 09:00 through 17:30");
        boundedField.setVariant(M3TextInputVariant.FILLED);
        boundedField.getPicker().setMinTime(LocalTime.of(9, 0));
        boundedField.getPicker().setMaxTime(LocalTime.of(17, 30));
        boundedField.getPicker().setMinuteStep(30);
        boundedField.getPresets().setAll(M3TimePresets.morning(), M3TimePresets.noon(), M3TimePresets.afternoon());
        boundedField.setPrefWidth(320.0);
        boundedField.setMaxWidth(320.0);

        M3TimePicker twelveHour = new M3TimePicker(LocalTime.of(10, 30));

        M3TimePicker twentyFourHour = new M3TimePicker(LocalTime.of(14, 45));
        twentyFourHour.setUse24HourClock(true);
        twentyFourHour.setMinuteStep(15);

        M3TimePicker bounded = new M3TimePicker(LocalTime.of(9, 30));
        bounded.setMinTime(LocalTime.of(9, 0));
        bounded.setMaxTime(LocalTime.of(17, 30));

        M3Button dialogButton = new M3Button("Open time dialog", M3ButtonVariant.FILLED);
        dialogButton.setOnAction(event -> showTimePickerDialog(LocalTime.of(10, 30)));

        return createGallery(
                createShowcaseGroup("Fields", field, boundedField),
                createShowcaseGroup("Dialog", dialogButton),
                createShowcaseGroup("12 Hour", twelveHour),
                createShowcaseGroup("24 Hour", twentyFourHour),
                createShowcaseGroup("Bounded Range", bounded)
        );
    }

    /// Opens a time picker dialog and reports the accepted time.
    ///
    /// @param initialTime the initially selected time
    private void showTimePickerDialog(LocalTime initialTime) {
        M3TimePickerDialog dialog = new M3TimePickerDialog(initialTime);
        dialog.getPicker().setUse24HourClock(true);
        dialog.getPicker().setMinuteStep(15);
        dialog.getPresets().setAll(M3TimePresets.common(initialTime));
        M3Button confirmAction = Objects.requireNonNull(
                dialog.getDialogPane().getDefaultAction(),
                "time dialog default action"
        );
        dialog.setOnHidden(event -> {
            if (event.getAction() == confirmAction) {
                @Nullable LocalTime value = dialog.getValue();
                if (value != null) {
                    context.showSnackbar("Selected time " + value);
                }
            }
        });
        context.showDialog(dialog);
    }
}
