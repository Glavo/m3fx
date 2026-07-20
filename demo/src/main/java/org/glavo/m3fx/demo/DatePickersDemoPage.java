// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerDialog;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3DatePresets;
import org.glavo.m3fx.controls.M3DateRange;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.controls.M3DateRangePickerDialog;
import org.glavo.m3fx.controls.M3DateRangePickerField;
import org.glavo.m3fx.controls.M3DateRangePresets;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/// Builds the DatePickers component showcase page.
@NotNullByDefault
final class DatePickersDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    DatePickersDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the date picker component page.
    Node createContent() {
        LocalDate today = LocalDate.now();
        M3DatePickerField field = new M3DatePickerField(today);
        field.setLabelText("Event date");
        field.setSupportingText("Editable ISO date with popup calendar");
        field.setVariant(M3TextInputVariant.OUTLINED);
        field.getPresets().setAll(M3DatePresets.common(today));
        field.setPrefWidth(320.0);
        field.setMaxWidth(320.0);

        M3DatePickerField boundedField = new M3DatePickerField(today.plusDays(2));
        boundedField.setLabelText("Booking date");
        boundedField.setSupportingText("Limited to the next two weeks");
        boundedField.setVariant(M3TextInputVariant.FILLED);
        boundedField.getPicker().setMinDate(today);
        boundedField.getPicker().setMaxDate(today.plusDays(14));
        boundedField.getPresets().setAll(
                M3DatePresets.today(today),
                M3DatePresets.tomorrow(today),
                M3DatePresets.daysFrom(today, 7)
        );
        boundedField.setPrefWidth(320.0);
        boundedField.setMaxWidth(320.0);

        M3DateRangePickerField rangeField = new M3DateRangePickerField(today.plusDays(2), today.plusDays(8));
        rangeField.setStartLabelText("Start date");
        rangeField.setEndLabelText("End date");
        rangeField.setStartSupportingText("Editable range start");
        rangeField.setEndSupportingText("Editable range end");
        rangeField.setStartVariant(M3TextInputVariant.OUTLINED);
        rangeField.setEndVariant(M3TextInputVariant.OUTLINED);
        rangeField.getPicker().setMinDate(today.minusDays(7));
        rangeField.getPicker().setMaxDate(today.plusDays(30));
        rangeField.getPresets().setAll(M3DateRangePresets.common(today, rangeField.getPicker().getFirstDayOfWeek()));
        rangeField.setPrefWidth(680.0);
        rangeField.setMaxWidth(680.0);

        M3DatePicker selected = new M3DatePicker(today);

        M3DatePicker range = new M3DatePicker(today.plusDays(4));
        range.setMinDate(today.minusDays(3));
        range.setMaxDate(today.plusDays(18));

        M3DateRangePicker dateRange = new M3DateRangePicker(today.plusDays(2), today.plusDays(8));
        dateRange.setMinDate(today.minusDays(7));
        dateRange.setMaxDate(today.plusDays(30));

        M3DatePicker monthOnly = new M3DatePicker();
        monthOnly.setDisplayedMonth(YearMonth.from(today.plusMonths(1)));
        monthOnly.setShowAdjacentMonthDays(false);

        M3Button dateDialogButton = new M3Button("Open date dialog", M3ButtonVariant.FILLED);
        dateDialogButton.setOnAction(event -> showDatePickerDialog(today));
        M3Button rangeDialogButton = new M3Button("Open range dialog", M3ButtonVariant.TONAL);
        rangeDialogButton.setOnAction(event -> showDateRangePickerDialog(today.plusDays(2), today.plusDays(8)));
        M3Button presetRangeDialogButton = new M3Button("Open preset range dialog", M3ButtonVariant.OUTLINED);
        presetRangeDialogButton.setOnAction(event -> showPresetDateRangePickerDialog(today));

        return createGallery(
                createShowcaseGroup("Fields", field, boundedField),
                createShowcaseGroup("Range Field", rangeField),
                createShowcaseGroup("Dialogs", dateDialogButton, rangeDialogButton, presetRangeDialogButton),
                createShowcaseGroup("Selected Date", selected),
                createShowcaseGroup("Bounded Range", range, dateRange),
                createShowcaseGroup("Month Only", monthOnly)
        );
    }

    /// Opens a date picker dialog and reports the accepted date.
    ///
    /// @param initialDate the initially selected date
    private void showDatePickerDialog(LocalDate initialDate) {
        M3DatePickerDialog dialog = new M3DatePickerDialog(initialDate);
        dialog.getPresets().setAll(M3DatePresets.common(initialDate));
        M3Button confirmAction = Objects.requireNonNull(
                dialog.getDialogPane().getDefaultAction(),
                "date dialog default action"
        );
        dialog.setOnHidden(event -> {
            if (event.getAction() == confirmAction) {
                @Nullable LocalDate value = dialog.getValue();
                if (value != null) {
                    context.showSnackbar("Selected date " + value);
                }
            }
        });
        context.showDialog(dialog);
    }

    /// Opens a date range picker dialog and reports the accepted range.
    ///
    /// @param startDate the initial range start
    /// @param endDate   the initial range end
    private void showDateRangePickerDialog(LocalDate startDate, LocalDate endDate) {
        M3DateRangePickerDialog dialog = new M3DateRangePickerDialog(startDate, endDate);
        M3Button confirmAction = Objects.requireNonNull(
                dialog.getDialogPane().getDefaultAction(),
                "date range dialog default action"
        );
        dialog.setOnHidden(event -> {
            if (event.getAction() == confirmAction) {
                @Nullable M3DateRange range = dialog.getPicker().getRange();
                if (range != null) {
                    context.showSnackbar("Selected range " + range.startDate() + " to " + range.endDate());
                }
            }
        });
        context.showDialog(dialog);
    }

    /// Opens a date range picker dialog with common range presets.
    ///
    /// @param anchorDate the date used to derive the initial presets and bounds
    private void showPresetDateRangePickerDialog(LocalDate anchorDate) {
        M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
        dialog.getPicker().setMinDate(anchorDate.minusMonths(1));
        dialog.getPicker().setMaxDate(anchorDate.plusMonths(3));
        dialog.getPresets().setAll(
                M3DateRangePresets.common(anchorDate, dialog.getPicker().getFirstDayOfWeek())
        );
        M3Button confirmAction = Objects.requireNonNull(
                dialog.getDialogPane().getDefaultAction(),
                "date range dialog default action"
        );
        dialog.setOnHidden(event -> {
            if (event.getAction() == confirmAction) {
                @Nullable M3DateRange range = dialog.getPicker().getRange();
                if (range != null) {
                    context.showSnackbar(
                            "Selected preset range " + range.startDate() + " to " + range.endDate()
                    );
                }
            }
        });
        context.showDialog(dialog);
    }
}
