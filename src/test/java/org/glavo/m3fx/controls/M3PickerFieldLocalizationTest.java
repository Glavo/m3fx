// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.localization.M3Localizer;
import org.glavo.m3fx.skins.M3TextInputLayoutSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Verifies scoped picker-field defaults, application overrides, and adornment skin lifetime.
@NotNullByDefault
final class M3PickerFieldLocalizationTest {
    /// Starts JavaFX before creating controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// A malformed translation for one control does not prevent unrelated control defaults from updating.
    @Test
    void translationFailureIsIsolatedAndCanBeRecovered() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField date = new M3DatePickerField();
            M3TimePickerField time = new M3TimePickerField();
            VBox root = new VBox(date, time);
            M3Localizer broken = new M3Localizer(Locale.GERMANY) {
                /// Models a failing application translation lookup.
                @Override
                public String getInvalidDateText() {
                    throw new IllegalArgumentException("missing date translation");
                }

                /// Supplies an independent translation that remains usable.
                @Override
                public String getInvalidTimeText() {
                    return "Invalid translated time";
                }
            };
            assertThrows(IllegalArgumentException.class, () -> M3Localizer.install(root, broken));
            assertEquals("Invalid translated time", time.getInvalidTextErrorText());
            assertSame(broken, M3Localizer.getEffectiveLocalizer(date));
            TaggedLocalizer recovered = new TaggedLocalizer("recovered");
            M3Localizer.install(root, recovered);
            assertEquals(recovered.getInvalidDateText(), date.getInvalidTextErrorText());
            assertEquals(recovered.getInvalidTimeText(), time.getInvalidTextErrorText());
        });
    }

    /// Reparenting and nearer installations refresh active errors without changing raw text or formatters.
    @Test
    void pickerFieldsFollowScopeAndPreserveEditing() {
        FxTestUtils.runOnFxThread(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            VBox left = new VBox();
            VBox right = new VBox();
            M3Localizer.install(left, first);
            M3Localizer.install(right, second);
            M3DatePickerField date = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            M3TimePickerField time = new M3TimePickerField(LocalTime.of(14, 35));
            left.getChildren().addAll(date, time);
            assertEquals("2026-05-19", date.getText());
            assertEquals("14:35", time.getText());
            DateTimeFormatter dateFormatter = date.getFormatter();
            DateTimeFormatter timeFormatter = time.getFormatter();
            date.setText("unfinished date");
            time.setText("unfinished time");
            assertFalse(date.commitEditorText());
            assertFalse(time.commitEditorText());
            assertEquals(first.getInvalidDateText(), date.getValidationErrorText());
            assertEquals(first.getInvalidTimeText(), time.getValidationErrorText());
            assertEquals(date.getInvalidTextErrorText(), date.invalidTextErrorTextProperty().get());
            assertEquals(time.getInvalidTextErrorText(), time.invalidTextErrorTextProperty().get());
            assertEquals(first.getOpenDatePickerText(), date.getInputLayout().getTrailing().getAccessibleText());
            assertEquals(first.getOpenTimePickerText(), time.getInputLayout().getTrailing().getAccessibleText());

            left.getChildren().clear();
            right.getChildren().addAll(date, time);
            assertEquals(second.getInvalidDateText(), date.getValidationErrorText());
            assertEquals(second.getInvalidTimeText(), time.getValidationErrorText());
            assertEquals(second.getInvalidDateText(), date.invalidTextErrorTextProperty().get());
            assertEquals(second.getInvalidTimeText(), time.invalidTextErrorTextProperty().get());
            assertEquals("unfinished date", date.getText());
            assertEquals("unfinished time", time.getText());
            assertSame(dateFormatter, date.getFormatter());
            assertSame(timeFormatter, time.getFormatter());

            M3Localizer.install(date, first);
            assertEquals(first.getInvalidDateText(), date.getValidationErrorText());
            M3Localizer.uninstall(date);
            assertEquals(second.getInvalidDateText(), date.getValidationErrorText());
        });
    }

    /// Explicit default-valued overrides and bindings survive scope replacement and unbinding.
    @Test
    void pickerOverridesAndFormattersRemainApplicationOwned() {
        FxTestUtils.runOnFxThread(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            M3DatePickerField date = new M3DatePickerField();
            M3TimePickerField time = new M3TimePickerField();
            VBox root = new VBox(date, time);
            M3Localizer.install(root, first);
            assertEquals(first.getInvalidDateText(), date.invalidTextErrorTextProperty().get());
            assertEquals(first.getDateOutOfRangeText(), date.rangeErrorTextProperty().get());
            date.setInvalidTextErrorText(date.getInvalidTextErrorText());
            SimpleStringProperty customError = new SimpleStringProperty(time.getInvalidTextErrorText());
            time.invalidTextErrorTextProperty().bind(customError);
            date.setText("invalid");
            time.setText("invalid");
            date.validate();
            time.validate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            date.setFormatter(formatter);
            date.setText("invalid");
            date.validate();
            M3Localizer.install(root, second);
            assertEquals(first.getInvalidDateText(), date.getValidationErrorText());
            assertEquals(first.getInvalidTimeText(), time.getValidationErrorText());
            assertEquals(date.getInvalidTextErrorText(), date.invalidTextErrorTextProperty().get());
            assertEquals(time.getInvalidTextErrorText(), time.invalidTextErrorTextProperty().get());
            assertSame(formatter, date.getFormatter());
            customError.set("updated parse error");
            assertEquals("updated parse error", time.getValidationErrorText());
            time.invalidTextErrorTextProperty().unbind();

            date.getPicker().setMinDate(LocalDate.of(2026, 6, 1));
            date.setText("19/05/2026");
            assertFalse(date.commitEditorText());
            assertEquals(second.getDateOutOfRangeText(), date.getValidationErrorText());
            SimpleStringProperty customRange = new SimpleStringProperty("custom range error");
            date.rangeErrorTextProperty().bind(customRange);
            M3Localizer.install(root, first);
            assertEquals("custom range error", date.getValidationErrorText());
            assertEquals("updated parse error", time.getValidationErrorText());
            assertEquals(date.getRangeErrorText(), date.rangeErrorTextProperty().get());
            customRange.set("updated range error");
            assertEquals("updated range error", date.getValidationErrorText());
            time.getPicker().setMinTime(LocalTime.of(9, 0));
            time.setText("08:30");
            assertFalse(time.commitEditorText());
            assertEquals(first.getTimeOutOfRangeText(), time.getValidationErrorText());
        });
    }

    /// Range labels follow defaults until explicitly assigned or bound, while validation remains localized.
    @Test
    void rangeLabelsAndErrorsRespectOverrides() {
        FxTestUtils.runOnFxThread(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            M3DateRangePickerField field = new M3DateRangePickerField();
            VBox root = new VBox(field);
            M3Localizer.install(root, first);
            assertEquals(first.getDateRangeText(), field.getLabelText());
            assertEquals(first.getStartDateText(), field.getStartLabelText());
            assertEquals(first.getStartDateText(), field.getStartInputLayout().getLabelText());
            assertEquals(first.getEndDateText(), field.getEndInputLayout().getLabelText());
            assertEquals(first.getOpenStartDateRangePickerText(),
                    field.getStartInputLayout().getTrailing().getAccessibleText());
            assertEquals(first.getOpenEndDateRangePickerText(),
                    field.getEndInputLayout().getTrailing().getAccessibleText());
            field.setStartLabelText(field.getStartLabelText());
            field.labelTextProperty().set(field.getLabelText());
            String compositeLabel = field.getLabelText();
            SimpleStringProperty endLabel = new SimpleStringProperty("custom end");
            field.endLabelTextProperty().bind(endLabel);
            field.setStartText("invalid");
            assertFalse(field.commitEditorText());
            M3Localizer.install(root, second);
            assertEquals(first.getStartDateText(), field.getStartInputLayout().getLabelText());
            assertEquals(compositeLabel, field.getLabelText());
            assertEquals("custom end", field.getEndInputLayout().getLabelText());
            assertEquals(second.getInvalidDateRangeText(), field.getValidationErrorText());
            assertEquals(field.getInvalidTextErrorText(), field.invalidTextErrorTextProperty().get());
            assertEquals(second.getOpenStartDateRangePickerText(),
                    field.getStartInputLayout().getTrailing().getAccessibleText());
            assertEquals("invalid", field.getStartText());
            field.setInvalidTextErrorText(field.getInvalidTextErrorText());
            M3Localizer.install(root, first);
            assertEquals(second.getInvalidDateRangeText(), field.getValidationErrorText());
            assertEquals(field.getInvalidTextErrorText(), field.invalidTextErrorTextProperty().get());

            field.setStartText("2026-05-20");
            field.setEndText("2026-05-19");
            assertFalse(field.commitEditorText());
            assertEquals(first.getDateRangeOutOfRangeText(), field.getValidationErrorText());
            field.rangeErrorTextProperty().set(field.getRangeErrorText());
            M3Localizer.install(root, second);
            assertEquals(first.getDateRangeOutOfRangeText(), field.getValidationErrorText());
            assertEquals(field.getRangeErrorText(), field.rangeErrorTextProperty().get());
            SimpleStringProperty customRange = new SimpleStringProperty("custom range");
            field.rangeErrorTextProperty().bind(customRange);
            M3Localizer.install(root, first);
            assertEquals("custom range", field.getValidationErrorText());
            customRange.set("updated range");
            assertEquals("updated range", field.getValidationErrorText());
            assertEquals(field.getRangeErrorText(), field.rangeErrorTextProperty().get());
        });
    }

    /// Both setters and writable properties retain the non-null error-message contract.
    @Test
    void errorMessagePropertiesRejectNull() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField date = new M3DatePickerField();
            M3TimePickerField time = new M3TimePickerField();
            M3DateRangePickerField range = new M3DateRangePickerField();
            assertThrows(NullPointerException.class, () -> date.setInvalidTextErrorText(null));
            assertThrows(NullPointerException.class, () -> date.setRangeErrorText(null));
            assertThrows(NullPointerException.class, () -> time.setInvalidTextErrorText(null));
            assertThrows(NullPointerException.class, () -> time.setRangeErrorText(null));
            assertThrows(NullPointerException.class, () -> range.setInvalidTextErrorText(null));
            assertThrows(NullPointerException.class, () -> range.setRangeErrorText(null));
            assertThrows(NullPointerException.class, () -> date.invalidTextErrorTextProperty().set(null));
            assertThrows(NullPointerException.class, () -> date.rangeErrorTextProperty().set(null));
            assertThrows(NullPointerException.class, () -> time.invalidTextErrorTextProperty().set(null));
            assertThrows(NullPointerException.class, () -> time.rangeErrorTextProperty().set(null));
            assertThrows(NullPointerException.class, () -> range.invalidTextErrorTextProperty().set(null));
            assertThrows(NullPointerException.class, () -> range.rangeErrorTextProperty().set(null));
            assertEquals(date.getInvalidTextErrorText(), date.invalidTextErrorTextProperty().get());
            assertEquals(time.getRangeErrorText(), time.rangeErrorTextProperty().get());
            assertEquals(range.getRangeErrorText(), range.rangeErrorTextProperty().get());
        });
    }

    /// Disposed skins stop receiving updates and replacement skins resolve the current scope.
    @Test
    void clearButtonLocalizationEndsWithSkinLifetime() {
        FxTestUtils.runOnFxThread(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            M3TextField editor = new M3TextField();
            editor.setText("text");
            M3TextInputLayout layout = new M3TextInputLayout(editor);
            layout.setClearButtonEnabled(true);
            VBox root = new VBox(layout);
            M3Localizer.install(root, first);
            M3TextInputLayoutSkin skin = new M3TextInputLayoutSkin(layout);
            layout.setSkin(skin);
            Node oldButton = assertInstanceOf(Node.class, skin.clearButton());
            assertEquals(first.getClearActionText(), oldButton.getAccessibleText());
            oldButton.setAccessibleText("temporary internal text");
            M3Localizer.install(root, second);
            assertEquals(second.getClearActionText(), oldButton.getAccessibleText());
            layout.setSkin(null);
            M3Localizer.install(root, first);
            assertEquals(second.getClearActionText(), oldButton.getAccessibleText());
            M3TextInputLayoutSkin replacement = new M3TextInputLayoutSkin(layout);
            layout.setSkin(replacement);
            Node newButton = assertInstanceOf(Node.class, replacement.clearButton());
            assertEquals(first.getClearActionText(), newButton.getAccessibleText());
            SimpleStringProperty customText = new SimpleStringProperty("custom clear");
            newButton.accessibleTextProperty().bind(customText);
            M3Localizer.install(root, second);
            assertEquals("custom clear", newButton.getAccessibleText());
            newButton.accessibleTextProperty().unbind();
            M3Localizer.install(root, first);
            assertEquals(first.getClearActionText(), newButton.getAccessibleText());
            layout.setSkin(null);
        });
    }

    /// Provides distinct messages without depending on translations supplied by the default implementation.
    @NotNullByDefault
    private static final class TaggedLocalizer extends M3Localizer {
        /// Identifies the localization scope in assertions.
        private final String tag;

        /// Creates one stable set of tagged English messages.
        private TaggedLocalizer(String tag) {
            super(Locale.ROOT);
            this.tag = tag;
        }

        /// Returns the tagged clear action.
        @Override
        public String getClearActionText() { return tag + " clear"; }

        /// Returns the tagged date-picker action.
        @Override
        public String getOpenDatePickerText() { return tag + " open date"; }

        /// Returns the tagged time-picker action.
        @Override
        public String getOpenTimePickerText() { return tag + " open time"; }

        /// Returns the tagged date parse error.
        @Override
        public String getInvalidDateText() { return tag + " invalid date"; }

        /// Returns the tagged time parse error.
        @Override
        public String getInvalidTimeText() { return tag + " invalid time"; }

        /// Returns the tagged selectable-date error.
        @Override
        public String getDateOutOfRangeText() { return tag + " date outside range"; }

        /// Returns the tagged selectable-time error.
        @Override
        public String getTimeOutOfRangeText() { return tag + " time outside range"; }

        /// Returns the tagged composite range label.
        @Override
        public String getDateRangeText() { return tag + " date range"; }

        /// Returns the tagged start-editor opening action.
        @Override
        public String getOpenStartDateRangePickerText() { return tag + " open start"; }

        /// Returns the tagged end-editor opening action.
        @Override
        public String getOpenEndDateRangePickerText() { return tag + " open end"; }

        /// Returns the tagged start label.
        @Override
        public String getStartDateText() { return tag + " start"; }

        /// Returns the tagged end label.
        @Override
        public String getEndDateText() { return tag + " end"; }

        /// Returns the tagged range parse error.
        @Override
        public String getInvalidDateRangeText() { return tag + " invalid range"; }

        /// Returns the tagged selectable-range error.
        @Override
        public String getDateRangeOutOfRangeText() { return tag + " range outside bounds"; }
    }
}
