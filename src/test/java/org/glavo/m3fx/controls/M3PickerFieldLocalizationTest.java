// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.localization.M3Localized;
import org.glavo.m3fx.localization.M3Localizer;
import org.glavo.m3fx.skins.M3TextInputLayoutSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies picker-field localization bindings, application overrides, and adornment skin lifetime.
@NotNullByDefault
final class M3PickerFieldLocalizationTest {
    /// Starts JavaFX before creating controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Owned layouts and pickers follow explicit bindings even before presentation.
    @Test
    void ownedControlsBindToTheirFieldLocalizer() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField date = new M3DatePickerField();
            M3TimePickerField time = new M3TimePickerField();
            M3DateRangePickerField range = new M3DateRangePickerField();
            SimpleObjectProperty<M3Localizer> language = new SimpleObjectProperty<>(M3Localizer.getDefault());
            date.localizerProperty().bind(language);
            time.localizerProperty().bind(language);
            range.localizerProperty().bind(language);
            for (M3Localizer localizer : List.of(new TaggedLocalizer("first"), new TaggedLocalizer("second"))) {
                language.set(localizer);
                for (M3Localized owned : List.<M3Localized>of(date.getInputLayout(), date.getPicker(),
                        time.getInputLayout(), time.getPicker(), range.getStartInputLayout(),
                        range.getEndInputLayout(), range.getPicker())) {
                    assertTrue(owned.localizerProperty().isBound());
                    assertSame(localizer, owned.getLocalizer());
                }
            }
        });
    }

    /// Popup visibility does not create or remove the owned picker's localization binding.
    @Test
    void ownedPickerBindingSurvivesPopupPresentation() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            M3DatePickerField date = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            M3TimePickerField time = new M3TimePickerField(LocalTime.NOON);
            M3DateRangePickerField range = new M3DateRangePickerField();
            VBox root = new VBox(date, time, range);
            Scene scene = new Scene(root, 720, 400);
            Stage stage = new Stage();
            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                for (M3PickerField<?, ?> field : List.of(date, time)) {
                    field.setLocalizer(first);
                    assertSame(first, field.getPicker().getLocalizer());
                    field.showPicker();
                    assertTrue(field.isShowing());
                    field.setLocalizer(second);
                    assertSame(second, field.getPicker().getLocalizer());
                    assertTrue(field.getPicker().localizerProperty().isBound());
                    field.hidePicker();
                    assertFalse(field.isShowing());
                    field.setLocalizer(first);
                    assertSame(first, field.getPicker().getLocalizer());
                    field.showPicker();
                    assertTrue(field.isShowing());
                    assertSame(first, field.getPicker().getLocalizer());
                    field.hidePicker();
                }
                range.setLocalizer(first);
                range.showPicker();
                assertTrue(range.isShowing());
                range.setLocalizer(second);
                assertSame(second, range.getPicker().getLocalizer());
                range.hidePicker();
                assertFalse(range.isShowing());
                range.setLocalizer(first);
                assertSame(first, range.getPicker().getLocalizer());
                assertTrue(range.getPicker().localizerProperty().isBound());
            } finally {
                date.hidePicker();
                time.hidePicker();
                range.hidePicker();
                stage.close();
            }
        });
    }

    /// A language replacement during a text notification leaves every default on the latest localizer.
    @Test
    void reentrantLanguageChangeRefreshesAllRangeFieldDefaults() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField();
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            field.labelTextProperty().addListener((observable, oldValue, newValue) -> {
                if (field.getLocalizer() == first) {
                    field.setLocalizer(second);
                }
            });

            field.setLocalizer(first);

            assertSame(second, field.getLocalizer());
            assertSame(second, field.getPicker().getLocalizer());
            assertEquals(second.getDateRangeText(), field.getLabelText());
            assertEquals(second.getStartDateText(), field.getStartLabelText());
            assertEquals(second.getEndDateText(), field.getEndLabelText());
            assertEquals(second.getInvalidDateRangeText(), field.getInvalidTextErrorText());
            assertEquals(second.getDateRangeOutOfRangeText(), field.getRangeErrorText());
        });
    }

    /// Application language changes refresh errors without changing raw edits, formatters, or parent independence.
    @Test
    void pickerFieldsFollowBindingsAndPreserveEditing() {
        FxTestUtils.runOnFxThread(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            VBox left = new VBox();
            VBox right = new VBox();
            M3DatePickerField date = new M3DatePickerField(LocalDate.of(2026, 5, 19));
            M3TimePickerField time = new M3TimePickerField(LocalTime.of(14, 35));
            SimpleObjectProperty<M3Localizer> language = new SimpleObjectProperty<>(first);
            date.localizerProperty().bind(language);
            time.localizerProperty().bind(language);
            left.getChildren().addAll(date, time);
            assertEquals("2026-05-19", date.getText());
            assertEquals("14:35", time.getText());
            DateTimeFormatter dateFormatter = date.getFormatter();
            DateTimeFormatter timeFormatter = time.getFormatter();
            date.setText("unfinished date");
            time.setText("unfinished time");
            assertFalse(date.commitEditorText());
            assertFalse(time.commitEditorText());
            date.getEditor().selectRange(2, 6);
            time.getEditor().selectRange(3, 7);
            assertEquals(first.getInvalidDateText(), date.getValidationErrorText());
            assertEquals(first.getInvalidTimeText(), time.getValidationErrorText());
            assertEquals(date.getInvalidTextErrorText(), date.invalidTextErrorTextProperty().get());
            assertEquals(time.getInvalidTextErrorText(), time.invalidTextErrorTextProperty().get());
            assertEquals(first.getOpenDatePickerText(), date.getInputLayout().getTrailing().getAccessibleText());
            assertEquals(first.getOpenTimePickerText(), time.getInputLayout().getTrailing().getAccessibleText());

            left.getChildren().clear();
            right.getChildren().addAll(date, time);
            assertEquals(first.getInvalidDateText(), date.getValidationErrorText());
            assertEquals(first.getInvalidTimeText(), time.getValidationErrorText());
            language.set(second);
            assertEquals(second.getInvalidDateText(), date.getValidationErrorText());
            assertEquals(second.getInvalidTimeText(), time.getValidationErrorText());
            assertEquals(second.getInvalidDateText(), date.invalidTextErrorTextProperty().get());
            assertEquals(second.getInvalidTimeText(), time.invalidTextErrorTextProperty().get());
            assertEquals("unfinished date", date.getText());
            assertEquals("unfinished time", time.getText());
            assertEquals(2, date.getEditor().getAnchor());
            assertEquals(6, date.getEditor().getCaretPosition());
            assertEquals(3, time.getEditor().getAnchor());
            assertEquals(7, time.getEditor().getCaretPosition());
            assertEquals(LocalDate.of(2026, 5, 19), date.getValue());
            assertEquals(LocalTime.of(14, 35), time.getValue());
            assertSame(dateFormatter, date.getFormatter());
            assertSame(timeFormatter, time.getFormatter());

            date.localizerProperty().unbind();
            date.setLocalizer(first);
            assertEquals(first.getInvalidDateText(), date.getValidationErrorText());
            assertEquals(second.getInvalidTimeText(), time.getValidationErrorText());
            date.localizerProperty().bind(language);
            assertEquals(second.getInvalidDateText(), date.getValidationErrorText());
        });
    }

    /// Explicit default-valued overrides and bindings survive language changes and unbinding.
    @Test
    void pickerOverridesAndFormattersRemainApplicationOwned() {
        FxTestUtils.runOnFxThread(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            M3DatePickerField date = new M3DatePickerField();
            M3TimePickerField time = new M3TimePickerField();
            SimpleObjectProperty<M3Localizer> language = new SimpleObjectProperty<>(first);
            date.localizerProperty().bind(language);
            time.localizerProperty().bind(language);
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
            language.set(second);
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
            language.set(first);
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
            field.setLocalizer(first);
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
            field.setLocalizer(second);
            assertEquals(first.getStartDateText(), field.getStartInputLayout().getLabelText());
            assertEquals(compositeLabel, field.getLabelText());
            assertEquals("custom end", field.getEndInputLayout().getLabelText());
            assertEquals(second.getInvalidDateRangeText(), field.getValidationErrorText());
            assertEquals(field.getInvalidTextErrorText(), field.invalidTextErrorTextProperty().get());
            assertEquals(second.getOpenStartDateRangePickerText(),
                    field.getStartInputLayout().getTrailing().getAccessibleText());
            assertEquals("invalid", field.getStartText());
            field.setInvalidTextErrorText(field.getInvalidTextErrorText());
            field.setLocalizer(first);
            assertEquals(second.getInvalidDateRangeText(), field.getValidationErrorText());
            assertEquals(field.getInvalidTextErrorText(), field.invalidTextErrorTextProperty().get());

            field.setStartText("2026-05-20");
            field.setEndText("2026-05-19");
            assertFalse(field.commitEditorText());
            assertEquals(first.getDateRangeOutOfRangeText(), field.getValidationErrorText());
            field.rangeErrorTextProperty().set(field.getRangeErrorText());
            field.setLocalizer(second);
            assertEquals(first.getDateRangeOutOfRangeText(), field.getValidationErrorText());
            assertEquals(field.getRangeErrorText(), field.rangeErrorTextProperty().get());
            SimpleStringProperty customRange = new SimpleStringProperty("custom range");
            field.rangeErrorTextProperty().bind(customRange);
            field.setLocalizer(first);
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

    /// Disposed skins stop receiving updates and replacement skins use the current localizer.
    @Test
    void clearButtonLocalizationEndsWithSkinLifetime() {
        FxTestUtils.runOnFxThread(() -> {
            TaggedLocalizer first = new TaggedLocalizer("first");
            TaggedLocalizer second = new TaggedLocalizer("second");
            M3TextField editor = new M3TextField();
            editor.setText("text");
            M3TextInputLayout layout = new M3TextInputLayout(editor);
            layout.setClearButtonEnabled(true);
            layout.setLocalizer(first);
            M3TextInputLayoutSkin skin = new M3TextInputLayoutSkin(layout);
            layout.setSkin(skin);
            Node oldButton = assertInstanceOf(Node.class, skin.clearButton());
            assertEquals(first.getClearActionText(), oldButton.getAccessibleText());
            oldButton.setAccessibleText("temporary internal text");
            layout.setLocalizer(second);
            assertEquals(second.getClearActionText(), oldButton.getAccessibleText());
            layout.setSkin(null);
            layout.setLocalizer(first);
            assertEquals(second.getClearActionText(), oldButton.getAccessibleText());
            M3TextInputLayoutSkin replacement = new M3TextInputLayoutSkin(layout);
            layout.setSkin(replacement);
            Node newButton = assertInstanceOf(Node.class, replacement.clearButton());
            assertEquals(first.getClearActionText(), newButton.getAccessibleText());
            SimpleStringProperty customText = new SimpleStringProperty("custom clear");
            newButton.accessibleTextProperty().bind(customText);
            layout.setLocalizer(second);
            assertEquals("custom clear", newButton.getAccessibleText());
            newButton.accessibleTextProperty().unbind();
            layout.setLocalizer(first);
            assertEquals(first.getClearActionText(), newButton.getAccessibleText());
            layout.setSkin(null);
        });
    }

    /// Provides distinct messages without depending on translations supplied by the default implementation.
    @NotNullByDefault
    private static final class TaggedLocalizer extends M3Localizer {
        /// Identifies the localization configuration in assertions.
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
