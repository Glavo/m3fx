// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3LocalizedStringProperty;
import org.glavo.m3fx.localization.M3Localizer;
import org.glavo.m3fx.skins.M3NumberFieldSkin;
import org.glavo.m3fx.skins.M3ValidationSummarySkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies form localization, explicit property ownership, pending edits, and summary skin lifecycle.
@NotNullByDefault
final class M3FormLocalizationTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Shared defaults publish changes until an explicit same-value assignment takes ownership.
    @Test
    void sharedPropertyPublishesDefaultsAndPreservesSameValueAssignments() {
        M3LocalizedStringProperty property = new M3LocalizedStringProperty(this, "message", "Default");
        assertSame(this, property.getBean());
        assertEquals("message", property.getName());
        AtomicInteger changes = new AtomicInteger();
        property.addListener((observable, oldValue, newValue) -> changes.incrementAndGet());
        property.updateDefault("Localized");
        assertEquals("Localized", property.get());
        assertEquals(1, changes.get());
        property.set(property.get());
        property.updateDefault("Another locale");
        assertEquals("Localized", property.get());
        assertEquals(1, changes.get());
    }

    /// Null arguments fail before acquiring ownership, and bound null values cannot escape through getters.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void sharedPropertyRejectsNullWithoutTakingDefaultOwnership() {
        assertThrows(NullPointerException.class, () -> new M3LocalizedStringProperty(this, "message", null));
        M3LocalizedStringProperty property = new M3LocalizedStringProperty(this, "message", "Default");
        assertThrows(NullPointerException.class, () -> property.set(null));
        assertThrows(NullPointerException.class, () -> property.updateDefault(null));
        assertThrows(NullPointerException.class, () -> property.bind(null));
        SimpleStringProperty source = new SimpleStringProperty();
        assertThrows(NullPointerException.class, () -> property.bind(source));
        assertFalse(property.isBound());
        property.updateDefault("Localized");
        assertEquals("Localized", property.get());

        source.set("Application");
        property.bind(source);
        source.set(null);
        assertThrows(NullPointerException.class, property::get);
        source.set("Recovered");
        assertEquals("Recovered", property.get());
        property.unbind();
        assertThrows(NullPointerException.class, () -> property.updateDefault(null));
        property.updateDefault("Ignored");
        assertEquals("Recovered", property.get());
    }

    /// A same-value unidirectional binding owns the value through failed assignments and unbinding.
    @Test
    void sharedPropertyBindingRetainsApplicationOwnership() {
        M3LocalizedStringProperty property = new M3LocalizedStringProperty(this, "message", "Default");
        SimpleStringProperty source = new SimpleStringProperty("Default");
        property.bind(source);
        assertThrows(RuntimeException.class, () -> property.set("Rejected"));
        property.updateDefault("Ignored while bound");
        assertTrue(property.isBound());
        assertEquals("Default", property.get());
        property.unbind();
        property.updateDefault("Ignored after unbinding");
        assertEquals("Default", property.get());
        source.set("Detached");
        assertEquals("Default", property.get());
        property.set("Application");
        assertEquals("Application", property.get());
    }

    /// Same-value bidirectional binding retains ownership while allowing application writes from both sides.
    @Test
    void sharedPropertyBidirectionalBindingPreservesBothDirections() {
        M3LocalizedStringProperty property = new M3LocalizedStringProperty(this, "message", "Default");
        SimpleStringProperty source = new SimpleStringProperty("Default");
        property.bindBidirectional(source);
        property.updateDefault("Ignored while linked");
        assertEquals("Default", property.get());
        assertEquals("Default", source.get());
        source.set("From source");
        assertEquals("From source", property.get());
        property.set("From property");
        assertEquals("From property", source.get());
        property.unbindBidirectional(source);
        property.updateDefault("Ignored after unlinking");
        source.set("Detached");
        assertEquals("From property", property.get());
    }

    /// Defaults follow subtree installations, nearest overrides, removal, and reparenting without a skin.
    @Test
    void defaultsFollowInheritedContextWithoutSkins() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = new M3NumberField();
            M3ValidationSummary summary = new M3ValidationSummary();
            M3Localizer fallback = M3Localizer.getEffectiveLocalizer(field);
            Pane firstRoot = new Pane(field, summary);
            Pane secondRoot = new Pane();
            TestLocalizer first = new TestLocalizer("First");
            TestLocalizer second = new TestLocalizer("Second");
            M3Localizer.install(firstRoot, first);
            M3Localizer.install(secondRoot, second);
            assertDefaults(first, field, summary);

            M3Localizer.install(field, second);
            assertEquals(second.getInvalidNumberText(), field.getInvalidTextErrorText());
            assertEquals(first.getValidationSummaryTitleText(), summary.getTitleText());
            M3Localizer.uninstall(field);
            assertDefaults(first, field, summary);

            firstRoot.getChildren().clear();
            secondRoot.getChildren().addAll(field, summary);
            assertDefaults(second, field, summary);
            M3Localizer.uninstall(secondRoot);
            assertDefaults(fallback, field, summary);
        });
    }

    /// Setter and property assignments equal to old English defaults remain explicit overrides.
    @Test
    void sameDefaultAssignmentsAreNeverReplaced() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = new M3NumberField();
            M3ValidationSummary summary = new M3ValidationSummary();
            M3Localizer english = M3Localizer.getEffectiveLocalizer(field);
            field.setInvalidTextErrorText(english.getInvalidNumberText());
            field.rangeErrorTextProperty().set(english.getNumberOutOfRangeText());
            field.setDecrementAccessibleText(english.getDecrementText());
            field.incrementAccessibleTextProperty().set(english.getIncrementText());
            summary.setTitleText(english.getValidationSummaryTitleText());
            summary.emptyTextProperty().set(english.getNoValidationIssuesText());
            Pane root = new Pane(field, summary);

            M3Localizer.install(root, new TestLocalizer("Changed"));
            assertDefaults(english, field, summary);
            assertEquals(field.getInvalidTextErrorText(), field.invalidTextErrorTextProperty().get());
            assertEquals(field.getRangeErrorText(), field.rangeErrorTextProperty().get());
            assertEquals(field.getDecrementAccessibleText(), field.decrementAccessibleTextProperty().get());
            assertEquals(field.getIncrementAccessibleText(), field.incrementAccessibleTextProperty().get());
            assertEquals(summary.getTitleText(), summary.titleTextProperty().get());
            assertEquals(summary.getEmptyText(), summary.emptyTextProperty().get());
        });
    }

    /// Unidirectional and bidirectional bindings retain ownership even after being removed.
    @Test
    void bindingsKeepApplicationValuesAcrossLocaleChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = new M3NumberField();
            M3ValidationSummary summary = new M3ValidationSummary();
            M3DateRangePickerField range = new M3DateRangePickerField();
            Pane root = new Pane(field, summary, range);
            @Unmodifiable List<StringProperty> properties = List.of(field.invalidTextErrorTextProperty(),
                    field.rangeErrorTextProperty(), field.decrementAccessibleTextProperty(),
                    field.incrementAccessibleTextProperty(), summary.titleTextProperty(), summary.emptyTextProperty(),
                    range.labelTextProperty(), range.startLabelTextProperty(), range.endLabelTextProperty());
            @Unmodifiable List<String> defaults = properties.stream().map(StringProperty::get).toList();
            for (StringProperty property : properties) {
                property.bind(new SimpleStringProperty(property.get()));
            }
            M3Localizer.install(root, new TestLocalizer("Same bound values"));
            for (int index = 0; index < properties.size(); index++) {
                assertEquals(defaults.get(index), properties.get(index).get());
            }
            SimpleStringProperty source = new SimpleStringProperty("Application message");
            for (StringProperty property : properties) {
                property.bind(source);
            }
            M3Localizer.install(root, new TestLocalizer("Bound"));
            for (StringProperty property : properties) {
                assertTrue(property.isBound());
                assertEquals(source.get(), property.get());
            }
            source.set("Updated message");
            for (StringProperty property : properties) {
                assertEquals(source.get(), property.get());
                property.unbind();
            }
            M3Localizer.install(root, new TestLocalizer("Unbound"));
            for (StringProperty property : properties) {
                assertEquals(source.get(), property.get());
            }

            M3NumberField bidirectionalField = new M3NumberField();
            M3ValidationSummary bidirectionalSummary = new M3ValidationSummary();
            root.getChildren().addAll(bidirectionalField, bidirectionalSummary);
            SimpleStringProperty action = new SimpleStringProperty(bidirectionalField.getIncrementAccessibleText());
            SimpleStringProperty title = new SimpleStringProperty(bidirectionalSummary.getTitleText());
            String oldAction = action.get();
            String oldTitle = title.get();
            bidirectionalField.incrementAccessibleTextProperty().bindBidirectional(action);
            bidirectionalSummary.titleTextProperty().bindBidirectional(title);
            M3Localizer.install(root, new TestLocalizer("Bidirectional"));
            assertEquals(oldAction, action.get());
            assertEquals(oldAction, bidirectionalField.getIncrementAccessibleText());
            assertEquals(oldTitle, title.get());
            assertEquals(oldTitle, bidirectionalSummary.getTitleText());
            action.set("Application action");
            title.set("Application title");
            assertEquals(action.get(), bidirectionalField.getIncrementAccessibleText());
            assertEquals(title.get(), bidirectionalSummary.getTitleText());
            bidirectionalField.incrementAccessibleTextProperty().unbindBidirectional(action);
            bidirectionalSummary.titleTextProperty().unbindBidirectional(title);
        });
    }

    /// Active numeric errors refresh while raw text, selection, committed value, and formatters are retained.
    @Test
    void numberMessagesRefreshWithoutCommittingOrReformatting() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = new M3NumberField(0, 10, 4);
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            SimpleObjectProperty<NumberFormat> formatterSource = new SimpleObjectProperty<>(formatter);
            field.formatterProperty().bind(formatterSource);
            field.setCommitBehavior(M3NumberFieldCommitBehavior.VALIDATE);
            field.setErrorText("Application error");
            field.setText("unfinished-");
            field.getEditor().selectRange(2, 6);
            assertFalse(field.commitEditorText());
            AtomicInteger textChanges = new AtomicInteger();
            field.textProperty().addListener((observable, oldValue, newValue) -> textChanges.incrementAndGet());
            TestLocalizer first = new TestLocalizer("First");
            M3Localizer.install(field, first);
            assertEquals(first.getInvalidNumberText(), field.getValidationErrorText());
            assertEquals("Application error", field.getErrorText());
            assertEquals("unfinished-", field.getText());
            assertEquals(2, field.getEditor().getAnchor());
            assertEquals(6, field.getEditor().getCaretPosition());
            assertEquals(4.0, field.getValue());
            assertEquals(0, textChanges.get());
            assertSame(formatter, field.getFormatter());
            assertTrue(field.formatterProperty().isBound());

            field.setText("11");
            assertFalse(field.commitEditorText());
            TestLocalizer second = new TestLocalizer("Second");
            M3Localizer.install(field, second);
            assertEquals(second.getNumberOutOfRangeText(), field.getValidationErrorText());
            assertEquals("11", field.getText());
            assertEquals(4.0, field.getValue());
            field.clearValidation();
            field.setText("-");
            M3Localizer.install(field, first);
            assertFalse(field.isValidationActive());
            assertEquals("-", field.getText());

            M3NumberField defaultFormatterField = new M3NumberField(1.5);
            NumberFormat captured = defaultFormatterField.getFormatter();
            String originalText = defaultFormatterField.getText();
            M3Localizer.install(defaultFormatterField, new M3Localizer(Locale.GERMANY));
            assertSame(captured, defaultFormatterField.getFormatter());
            assertEquals(originalText, defaultFormatterField.getText());
        });
    }

    /// Stepper accessibility remains localized across skin replacement and honors explicit action labels.
    @Test
    void stepperActionsFollowLocaleAcrossSkinReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = new M3NumberField();
            field.setSkin(new M3NumberFieldSkin(field, field.getInputLayout()));
            TestLocalizer first = new TestLocalizer("First");
            M3Localizer.install(field, first);
            Node stepper = Objects.requireNonNull(field.getInputLayout().getTrailing());
            Node decrement = Objects.requireNonNull(stepper.lookup(".m3-number-field-decrement-button"));
            Node increment = Objects.requireNonNull(stepper.lookup(".m3-number-field-increment-button"));
            assertEquals(first.getDecrementText(), decrement.getAccessibleText());
            assertEquals(first.getIncrementText(), increment.getAccessibleText());
            field.setIncrementAccessibleText("Application increment");
            field.setSkin(null);
            TestLocalizer second = new TestLocalizer("Second");
            M3Localizer.install(field, second);
            field.setSkin(new M3NumberFieldSkin(field, field.getInputLayout()));
            assertEquals(second.getDecrementText(), decrement.getAccessibleText());
            assertEquals("Application increment", increment.getAccessibleText());
            field.setSkin(null);
        });
    }

    /// Summary fallback labels refresh independently of title overrides and preserve cached row identity.
    @Test
    void summaryFallbackRowsFollowLocaleAndDisposeObservation() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout input = new M3TextInputLayout(new M3TextField());
            input.setValidator((control, text) -> " ");
            M3FormValidator validator = new M3FormValidator(input);
            assertFalse(validator.validate());
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setTitleText("Application title");
            summary.setEmptyText("Application empty state");
            summary.setSkin(new M3ValidationSummarySkin(summary));
            Pane root = new Pane(summary, input);
            TestLocalizer first = new TestLocalizer("First");
            M3Localizer.install(root, first);
            Node row = Objects.requireNonNull(summary.lookup(".m3-validation-summary-item"));
            Label label = label(summary, ".m3-validation-summary-item-label");
            Label error = label(summary, ".m3-validation-summary-item-error");
            assertEquals(first.getFieldText(), label.getText());
            assertEquals(first.getInvalidValueText(), error.getText());
            assertEquals(first.getFieldText() + ": " + first.getInvalidValueText(), row.getAccessibleText());

            TestLocalizer second = new TestLocalizer("Second");
            M3Localizer.install(root, second);
            assertSame(row, summary.lookup(".m3-validation-summary-item"));
            assertEquals(second.getFieldText(), label.getText());
            assertEquals(second.getInvalidValueText(), error.getText());
            assertEquals("Application title", label(summary, ".m3-validation-summary-title").getText());
            input.setLabelText("Application field");
            input.setValidator((control, text) -> "Application validation");
            assertFalse(input.validate());
            M3Localizer.install(root, first);
            assertEquals("Application field", label.getText());
            assertEquals("Application validation", error.getText());

            input.setLabelText("");
            input.setValidator((control, text) -> " ");
            assertFalse(input.validate());
            assertEquals(first.getFieldText(), label.getText());
            summary.setSkin(null);
            M3Localizer.install(root, second);
            assertEquals(first.getFieldText(), label.getText());
            summary.setSkin(new M3ValidationSummarySkin(summary));
            assertEquals(second.getFieldText(), label(summary, ".m3-validation-summary-item-label").getText());
            summary.setSkin(null);
        });
    }

    /// Valid-state labels and accessibility update with defaults without starting form validation.
    @Test
    void summaryEmptyStateRefreshesWithoutStartingValidation() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout input = new M3TextInputLayout(new M3TextField());
            M3FormValidator validator = new M3FormValidator(input);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setShowWhenValid(true);
            summary.setSkin(new M3ValidationSummarySkin(summary));
            for (TestLocalizer localizer : List.of(new TestLocalizer("First"), new TestLocalizer("Second"))) {
                M3Localizer.install(summary, localizer);
                assertEquals(localizer.getValidationSummaryTitleText(), label(summary, ".m3-validation-summary-title").getText());
                assertEquals(localizer.getNoValidationIssuesText(), label(summary, ".m3-validation-summary-empty-text").getText());
                assertEquals(localizer.getValidationSummaryTitleText() + " " + localizer.getNoValidationIssuesText(),
                        summary.getAccessibleText());
                assertFalse(validator.isValidationActive());
                assertFalse(input.isValidationActive());
            }
            summary.setSkin(null);
        });
    }

    /// Checks that all public default text getters reflect the expected localizer.
    private static void assertDefaults(M3Localizer localizer, M3NumberField field, M3ValidationSummary summary) {
        assertEquals(localizer.getInvalidNumberText(), field.getInvalidTextErrorText());
        assertEquals(localizer.getNumberOutOfRangeText(), field.getRangeErrorText());
        assertEquals(localizer.getDecrementText(), field.getDecrementAccessibleText());
        assertEquals(localizer.getIncrementText(), field.getIncrementAccessibleText());
        assertEquals(localizer.getValidationSummaryTitleText(), summary.getTitleText());
        assertEquals(localizer.getNoValidationIssuesText(), summary.getEmptyText());
    }

    /// Looks up a rendered label and fails clearly when its skin content is missing.
    private static Label label(Node node, String selector) {
        return assertInstanceOf(Label.class, node.lookup(selector));
    }

    /// Supplies distinct messages so tests do not depend on the machine's default locale.
    @NotNullByDefault
    private static final class TestLocalizer extends M3Localizer {
        /// Identifies this immutable message set.
        private final String prefix;

        /// Creates a localizer with distinguishable messages and a non-default formatting locale.
        private TestLocalizer(String prefix) {
            super(Locale.GERMANY);
            this.prefix = prefix;
        }

        /// Returns the test invalid-number message.
        @Override
        public String getInvalidNumberText() { return prefix + " invalid number"; }

        /// Returns the test range message.
        @Override
        public String getNumberOutOfRangeText() { return prefix + " number range"; }

        /// Returns the test decrement action.
        @Override
        public String getDecrementText() { return prefix + " decrement"; }

        /// Returns the test increment action.
        @Override
        public String getIncrementText() { return prefix + " increment"; }

        /// Returns the test summary title.
        @Override
        public String getValidationSummaryTitleText() { return prefix + " summary"; }

        /// Returns the test valid-state message.
        @Override
        public String getNoValidationIssuesText() { return prefix + " valid"; }

        /// Returns the test fallback field label.
        @Override
        public String getFieldText() { return prefix + " field"; }

        /// Returns the test fallback validation message.
        @Override
        public String getInvalidValueText() { return prefix + " invalid value"; }
    }
}
