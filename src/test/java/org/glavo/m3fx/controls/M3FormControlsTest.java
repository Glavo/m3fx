// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3FormPaneSkin;
import org.glavo.m3fx.skins.M3FormRowSkin;
import org.glavo.m3fx.skins.M3FormSectionSkin;
import org.glavo.m3fx.skins.M3TextInputLayoutSkin;
import org.glavo.m3fx.skins.M3ValidationSummarySkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.glavo.m3fx.testing.Tier2Test;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.glavo.m3fx.M3TestControls.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests form helper controls, skins, and accessibility metadata.
@NotNullByDefault
final class M3FormControlsTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Closes real stages opened by focus and scrolling tests.
    @AfterEach
    void closeStages() {
        FxTestUtils.runOnFxThread(() -> {
            for (Window window : java.util.List.copyOf(Window.getWindows())) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }
        });
    }

    /// Verifies that form panes expose items through the skin and accessibility metadata.
    @Test
    void formPaneMirrorsItemsIntoSkin() {
        FxTestUtils.runOnFxThread(() -> {
            M3FormRow first = new M3FormRow("Name", new Label("Content"));
            M3FormRow second = new M3FormRow("Email", new Label("Content"));
            M3FormPane form = formPane(first);
            form.getItems().add(second);
            form.setContentPadding(20.0);
            form.setRowSpacing(10.0);
            form.setStyle("-m3-content-padding: 20px; -m3-row-spacing: 10px;");

            applyCss(form);

            assertInstanceOf(M3FormPaneSkin.class, form.getSkin());
            assertEquals(2, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(first, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertSame(second, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

            Node content = form.lookup("." + "m3-form-pane-content");
            VBox contentBox = assertInstanceOf(VBox.class, content);
            assertEquals(2, contentBox.getChildren().size());
            assertEquals(10.0, contentBox.getSpacing());
        });
    }

    /// Verifies that form sections update title, supporting text, and content slots.
    @Test
    void formSectionMirrorsHeaderAndContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3FormRow row = new M3FormRow("Field", new Label("Value"));
            M3FormSection section = formSection("Account", "Profile fields", row);
            section.setContentSpacing(18.0);
            section.setStyle("-m3-content-spacing: 18px;");

            applyCss(section);

            assertInstanceOf(M3FormSectionSkin.class, section.getSkin());
            assertEquals("Account", section.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(1, section.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(row, section.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

            Label title = assertInstanceOf(Label.class, section.lookup("." + "m3-form-section-title"));
            Label supporting =
                    assertInstanceOf(Label.class, section.lookup("." + "m3-form-section-supporting-text"));
            VBox content = assertInstanceOf(VBox.class, section.lookup("." + "m3-form-section-content"));

            assertEquals("Account", title.getText());
            assertEquals("Profile fields", supporting.getText());
            assertEquals(18.0, content.getSpacing());
            assertEquals(1, content.getChildren().size());
        });
    }

    /// Verifies that form rows update text, slots, metrics, and accessibility metadata.
    @Test
    void formRowMirrorsTextAndSlotsIntoSkin() {
        FxTestUtils.runOnFxThread(() -> {
            Label content = new Label("Content");
            M3Button trailing = new M3Button("Action");
            M3FormRow row = new M3FormRow("Display name", "Visible to collaborators", content, trailing);
            row.setLabelWidth(144.0);
            row.setColumnSpacing(12.0);
            row.setRowMinHeight(72.0);
            row.setStyle("-m3-label-width: 144px; -m3-column-spacing: 12px; -m3-row-min-height: 72px;");

            applyCss(row);

            assertInstanceOf(M3FormRowSkin.class, row.getSkin());
            assertEquals(AccessibleRole.PARENT, row.getAccessibleRole());
            assertEquals("Display name", row.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(2, row.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(content, row.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertSame(content, row.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertSame(trailing, row.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

            Label label = assertInstanceOf(Label.class, row.lookup("." + "m3-form-row-label"));
            Label supporting = assertInstanceOf(Label.class, row.lookup("." + "m3-form-row-supporting-text"));
            VBox textColumn = assertInstanceOf(VBox.class, row.lookup("." + "m3-form-row-text-column"));

            assertEquals("Display name", label.getText());
            assertEquals("Visible to collaborators", supporting.getText());
            assertEquals(144.0, textColumn.getPrefWidth());
            assertNotNull(content.getParent());
            assertNotNull(trailing.getParent());
        });
    }

    /// Verifies that form rows reject a node reused across both slots.
    @Test
    void formRowRejectsDuplicateSlotNode() {
        Label content = new Label("Content");
        M3FormRow row = new M3FormRow();
        row.setContent(content);

        assertThrows(IllegalArgumentException.class, () -> row.setTrailing(content));
    }

    /// Verifies that bound form-row slots validate every value before publishing structural changes.
    @Test
    void formRowValidatesBoundSlotValues() {
        FxTestUtils.runOnFxThread(() -> {
            Label first = new Label("First");
            Label trailing = new Label("Trailing");
            Label replacement = new Label("Replacement");
            SimpleObjectProperty<Node> source = new SimpleObjectProperty<>(first);
            M3FormRow row = new M3FormRow();
            row.contentProperty().bind(source);
            row.setTrailing(trailing);

            assertInstanceOf(
                    IllegalArgumentException.class,
                    captureUncaughtListenerException(() -> source.set(trailing))
            );

            source.set(replacement);
            assertSame(replacement, row.getContent());
            assertSame(trailing, row.getTrailing());
        });
    }

    /// Verifies that text-input bindings reject incompatible controls before detaching the installed input.
    @Test
    void textInputLayoutValidatesBoundInputBeforeStateMigration() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField first = new M3TextField("First");
            M3TextField replacement = new M3TextField("Replacement");
            SimpleObjectProperty<TextInputControl> source = new SimpleObjectProperty<>(first);
            M3TextInputLayout layout = new M3TextInputLayout();
            layout.inputProperty().bind(source);

            assertInstanceOf(
                    IllegalArgumentException.class,
                    captureUncaughtListenerException(() -> source.set(new TextField("Unsupported")))
            );

            first.setText("Still installed");
            assertEquals("Still installed".length(), layout.getCharacterCount());

            source.set(replacement);
            assertSame(replacement, layout.getInput());
            assertEquals("Replacement".length(), layout.getCharacterCount());

            first.setText("Detached");
            assertEquals("Replacement".length(), layout.getCharacterCount());
        });
    }

    /// Verifies that text-input layout slots reject duplicate nodes and scene-graph cycles.
    @Test
    void textInputLayoutRejectsDuplicateAndCyclicSlotNodes() {
        M3TextInputLayout layout = new M3TextInputLayout(new M3TextField());
        Label adornment = new Label("Adornment");
        layout.setLeading(adornment);

        assertThrows(IllegalArgumentException.class, () -> layout.setTrailing(adornment));
        assertThrows(IllegalArgumentException.class, () -> layout.setTrailing(layout));

        VBox ancestor = new VBox(layout);
        assertThrows(IllegalArgumentException.class, () -> layout.setTrailing(ancestor));
    }

    /// Verifies that form helpers expose their split user-agent stylesheet.
    @Test
    void formControlsExposeUserAgentStylesheet() {
        assertTrue(new M3FormPane().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(formSection().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(new M3FormRow().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(new M3ValidationSummary().getUserAgentStylesheet()
                .endsWith("/styles/controls/validation-summary.css"));
    }

    /// Verifies that form validators coordinate text input validation state.
    @Test
    void formValidatorCoordinatesTextInputValidation() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField);
            nameLayout.setLabelText("Display name");
            nameLayout.setSupportingText("Required");
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));

            M3TextField emailField = new M3TextField("support@example.com");
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField);
            emailLayout.setLabelText("Email");
            emailLayout.setSupportingText("Format");
            emailLayout.setValidator(M3TextInputValidators.pattern(
                    Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                    "Enter a valid email address"
            ));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);

            assertEquals(List.of(nameLayout, emailLayout), validator.getInputs());
            assertTrue(validator.isValid());
            assertTrue(validator.validProperty().get());
            assertFalse(validator.isValidationActive());
            assertFalse(validator.validationActiveProperty().get());
            assertEquals(0, validator.getInvalidInputCount());
            assertEquals(0, validator.invalidInputCountProperty().get());
            assertFalse(validator.validate());
            assertFalse(validator.isValid());
            assertFalse(validator.validProperty().get());
            assertTrue(validator.isValidationActive());
            assertTrue(validator.validationActiveProperty().get());
            assertEquals(1, validator.getInvalidInputCount());
            assertEquals(1, validator.invalidInputCountProperty().get());
            assertEquals(List.of(nameLayout), validator.getInvalidInputs());
            assertSame(nameLayout, validator.getFirstInvalidInput());
            assertSame(nameLayout, validator.firstInvalidInputProperty().get());
            assertFalse(validator.focusFirstInvalidInput());

            VBox root = new VBox(nameLayout, emailLayout);
            Stage stage = new Stage();
            Scene scene = new Scene(root, 520.0, 240.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            assertTrue(validator.focusFirstInvalidInput());
            assertTrue(nameField.isFocused());

            nameField.setText("M3FX Project");

            assertTrue(validator.validateAndFocusFirstInvalidInput());
            assertTrue(validator.isValid());
            assertTrue(validator.isValidationActive());
            assertEquals(0, validator.getInvalidInputCount());
            assertTrue(validator.getInvalidInputs().isEmpty());
            assertNull(validator.getFirstInvalidInput());
        });
    }

    /// Verifies that form validators can reveal the first invalid input through an owner scroll pane.
    @Test
    void formValidatorOwnerFocusRevealsInvalidInput() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField field = new M3TextField();
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("Name");
            layout.setSupportingText("Required");
            layout.setValidator(M3TextInputValidators.required("Name is required"));
            M3FormValidator validator = new M3FormValidator(layout);
            Pane spacer = new Pane();
            spacer.setPrefHeight(240.0);
            VBox content = new VBox(spacer, layout);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(320.0, 120.0);

            Stage stage = new Stage();
            Scene scene = new Scene(scrollPane, 320.0, 120.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            scrollPane.applyCss();
            scrollPane.resize(320.0, 120.0);
            scrollPane.layout();
            content.layout();

            assertFalse(validator.validateAndFocusFirstInvalidInput(content));

            assertTrue(field.isFocused());
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Verifies that form validators track external validation updates.
    @Test
    void formValidatorTracksExternalValidationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField field = new M3TextField();
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("Name");
            layout.setSupportingText("Required");
            layout.setValidator(M3TextInputValidators.required("Name is required"));
            M3FormValidator validator = new M3FormValidator(layout);

            assertTrue(validator.isValid());
            assertFalse(validator.isValidationActive());

            assertFalse(layout.validate());
            assertFalse(validator.isValid());
            assertTrue(validator.isValidationActive());
            assertEquals(1, validator.getInvalidInputCount());
            assertEquals(List.of(layout), validator.getInvalidInputs());

            layout.clearValidation();

            assertTrue(validator.isValid());
            assertFalse(validator.isValidationActive());
            assertEquals(0, validator.getInvalidInputCount());
            assertTrue(validator.getInvalidInputs().isEmpty());
        });
    }

    /// Verifies that form validators can validate and clear one registered input.
    @Test
    void formValidatorSupportsSingleInputValidationWorkflow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField);
            nameLayout.setLabelText("Display name");
            nameLayout.setSupportingText("Required");
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));

            M3TextField emailField = new M3TextField("support");
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField);
            emailLayout.setLabelText("Email");
            emailLayout.setSupportingText("Format");
            emailLayout.setValidator(M3TextInputValidators.pattern(
                    Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                    "Enter a valid email address"
            ));

            M3TextInputLayout unregisteredLayout = new M3TextInputLayout(new M3TextField());
            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);

            assertFalse(validator.validateInput(emailLayout));

            assertTrue(validator.isValidationActive());
            assertFalse(nameLayout.isValidationActive());
            assertTrue(emailLayout.isValidationActive());
            assertEquals(List.of(emailLayout), validator.getInvalidInputs());
            assertEquals(1, validator.getInvalidInputCount());
            assertSame(emailLayout, validator.getFirstInvalidInput());

            validator.clearValidation(emailLayout);

            assertFalse(validator.isValidationActive());
            assertTrue(validator.isValid());
            assertEquals(0, validator.getInvalidInputCount());
            assertTrue(validator.getInvalidInputs().isEmpty());

            assertThrows(IllegalArgumentException.class, () -> validator.validateInput(unregisteredLayout));
            assertThrows(IllegalArgumentException.class, () -> validator.clearValidation(unregisteredLayout));
        });
    }

    /// Verifies that form validators reject invalid input list mutations and update list state.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void formValidatorMaintainsDistinctInputs() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = new M3TextInputLayout(new M3TextField());
            M3TextInputLayout second = new M3TextInputLayout(new M3TextField());
            M3TextInputLayout third = new M3TextInputLayout(new M3TextField());
            M3FormValidator validator = new M3FormValidator(first);

            assertThrows(NullPointerException.class, () -> validator.getInputs().add(null));
            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().add(first));
            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().addAll(second, second));
            assertTrue(validator.getInputs().remove(first));
            assertFalse(validator.getInputs().remove(first));
            assertTrue(validator.getInputs().isEmpty());

            validator.getInputs().addAll(first, second);

            assertEquals(List.of(first, second), validator.getInputs());
            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().setAll(first, first));
            assertEquals(List.of(first, second), validator.getInputs());

            validator.getInputs().set(1, third);

            assertEquals(List.of(first, third), validator.getInputs());
            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().set(1, first));
            assertEquals(List.of(first, third), validator.getInputs());

            validator.getInputs().clear();

            assertTrue(validator.getInputs().isEmpty());
            assertFalse(validator.focusFirstInvalidInput());
        });
    }

    /// Verifies that stable invalid input membership does not publish redundant list changes.
    @Test
    void formValidatorSuppressesRedundantInvalidInputListChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField field = new M3TextField("a");
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("Code");
            layout.setSupportingText("Minimum length");
            layout.setValidator((input, text) -> text.length() < 3 ? "Enter " + (3 - text.length()) + " more" : null);
            M3FormValidator validator = new M3FormValidator(layout);
            AtomicInteger changeCount = new AtomicInteger();
            validator.getInvalidInputs().addListener((ListChangeListener<M3TextInputLayout>) change ->
                    changeCount.incrementAndGet());

            assertFalse(validator.validate());
            assertEquals(1, changeCount.get());
            assertEquals(List.of(layout), validator.getInvalidInputs());

            field.setText("ab");

            assertEquals(1, changeCount.get());
            assertEquals(List.of(layout), validator.getInvalidInputs());
            assertEquals("Enter 1 more", layout.getValidationErrorText());

            field.setText("abc");

            assertEquals(2, changeCount.get());
            assertTrue(validator.getInvalidInputs().isEmpty());
        });
    }

    /// Verifies that bulk validation and input mutations publish one consolidated invalid-input change.
    @Test
    void formValidatorCoalescesBulkAggregateChangesAndDetachesRemovedInputs() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = requiredLayout("First");
            M3TextInputLayout second = requiredLayout("Second");
            M3TextInputLayout third = requiredLayout("Third");
            assertFalse(first.validate());
            assertFalse(second.validate());
            assertFalse(third.validate());

            M3FormValidator validator = new M3FormValidator();
            AtomicInteger invalidListChanges = new AtomicInteger();
            validator.getInvalidInputs().addListener((ListChangeListener<M3TextInputLayout>) change ->
                    invalidListChanges.incrementAndGet());

            validator.getInputs().addAll(first, second, third);

            assertEquals(1, invalidListChanges.get());
            assertEquals(List.of(first, second, third), validator.getInvalidInputs());
            assertTrue(validator.isValidationActive());

            validator.clearValidation();

            assertEquals(2, invalidListChanges.get());
            assertTrue(validator.getInvalidInputs().isEmpty());
            assertFalse(validator.isValidationActive());

            assertFalse(validator.validate());

            assertEquals(3, invalidListChanges.get());
            assertEquals(List.of(first, second, third), validator.getInvalidInputs());

            validator.getInputs().remove(0, 2);

            assertEquals(4, invalidListChanges.get());
            assertEquals(List.of(third), validator.getInvalidInputs());
            assertSame(third, validator.getFirstInvalidInput());

            first.clearValidation();
            second.clearValidation();
            assertFalse(first.validate());
            assertFalse(second.validate());

            assertEquals(4, invalidListChanges.get());
            assertEquals(List.of(third), validator.getInvalidInputs());

            third.clearValidation();

            assertEquals(5, invalidListChanges.get());
            assertTrue(validator.getInvalidInputs().isEmpty());
            assertTrue(validator.isValid());
            assertFalse(validator.isValidationActive());
        });
    }

    /// Verifies that validation summaries render validator errors and expose accessibility targets.
    @Test
    void validationSummaryMirrorsValidatorErrorsIntoSkin() {
        FxTestUtils.runOnFxThread(() -> {
            PseudoClass empty = PseudoClass.getPseudoClass("empty");
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField);
            nameLayout.setLabelText("Display name");
            nameLayout.setSupportingText("Required");
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));

            M3TextField emailField = new M3TextField("support");
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField);
            emailLayout.setLabelText("Email");
            emailLayout.setSupportingText("Format");
            emailLayout.setValidator(M3TextInputValidators.pattern(
                    Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                    "Enter a valid email address"
            ));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            VBox root = new VBox(summary, nameLayout, emailLayout);
            Stage stage = new Stage();
            Scene scene = new Scene(root, 640.0, 320.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            assertInstanceOf(M3ValidationSummarySkin.class, summary.getSkin());
            assertTrue(summary.getPseudoClassStates().contains(empty));
            assertFalse(summary.isShowingSummary());

            summary.setShowWhenValid(true);

            assertFalse(summary.getPseudoClassStates().contains(empty));
            assertTrue(summary.isShowingSummary());
            assertEquals("Fix the following fields No validation issues",
                    summary.queryAccessibleAttribute(AccessibleAttribute.TEXT));

            summary.setShowWhenValid(false);

            assertFalse(validator.validate());
            summary.applyCss();

            assertFalse(summary.getPseudoClassStates().contains(empty));
            assertEquals(2, summary.getInvalidInputCount());
            assertSame(nameLayout, summary.getInvalidInput(0));
            assertSame(emailLayout, summary.getInvalidInput(1));
            assertEquals(2, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(nameLayout, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertEquals(2, summary.lookupAll("." + "m3-validation-summary-item").size());
            assertTrue(summary.focusInput(nameLayout));
        });
    }

    /// Verifies that validation summary accessible text follows the rendered visible invalid row state.
    @Test
    void validationSummaryAccessibleTextFollowsVisibleInvalidRows() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField hiddenField = new M3TextField();
            M3TextInputLayout hiddenLayout = new M3TextInputLayout(hiddenField);
            hiddenLayout.setLabelText("Hidden");
            hiddenLayout.setSupportingText("Required");
            hiddenLayout.setValidator(M3TextInputValidators.required("Hidden is required"));
            Pane hiddenAncestor = new Pane(hiddenLayout);
            hiddenAncestor.setVisible(false);

            M3FormValidator validator = new M3FormValidator(hiddenLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setShowWhenValid(true);
            summary.setTitleText("Validation status");
            summary.setEmptyText("All visible fields are valid");

            VBox root = new VBox(summary, hiddenAncestor);
            Stage stage = new Stage();
            Scene scene = new Scene(root, 520.0, 220.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            assertFalse(validator.validate());
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getInvalidInputCount());
            assertEquals(0, summary.getVisibleInvalidInputCount());
            assertTrue(summary.isShowingSummary());
            assertEquals("Validation status All visible fields are valid",
                    summary.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(0, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertTrue(summary.lookupAll("." + "m3-validation-summary-item").isEmpty());
            Label emptyLabel = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-empty-text")
            );
            assertEquals("All visible fields are valid", emptyLabel.getText());

            hiddenAncestor.setVisible(true);
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getVisibleInvalidInputCount());
            assertEquals("Validation status", summary.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(1, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(hiddenLayout, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertEquals(1, summary.lookupAll("." + "m3-validation-summary-item").size());
        });
    }

    /// Verifies that validation summaries track their own visible and enabled ancestor chain.
    @Test
    void validationSummaryTracksOwnAncestorReachability() {
        FxTestUtils.runOnFxThread(() -> {
            PseudoClass empty = PseudoClass.getPseudoClass("empty");
            M3TextField field = new M3TextField();
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("Display name");
            layout.setSupportingText("Required");
            layout.setValidator(M3TextInputValidators.required("Display name is required"));
            M3FormValidator validator = new M3FormValidator(layout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            Pane summaryOwner = new Pane(summary);
            VBox root = new VBox(summaryOwner, layout);
            Stage stage = new Stage();
            Scene scene = new Scene(root, 520.0, 260.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            assertFalse(validator.validate());
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getVisibleInvalidInputCount());
            assertTrue(summary.isShowingSummary());
            assertFalse(summary.getPseudoClassStates().contains(empty));
            assertEquals(1, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertEquals(1, summary.lookupAll("." + "m3-validation-summary-item").size());

            summaryOwner.setVisible(false);
            root.applyCss();
            root.layout();

            assertEquals(0, summary.getVisibleInvalidInputCount());
            assertFalse(summary.isShowingSummary());
            assertTrue(summary.getPseudoClassStates().contains(empty));
            assertEquals(0, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertTrue(summary.lookupAll("." + "m3-validation-summary-item").isEmpty());

            summaryOwner.setVisible(true);
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getVisibleInvalidInputCount());
            assertTrue(summary.isShowingSummary());
            assertEquals(1, summary.lookupAll("." + "m3-validation-summary-item").size());

            summaryOwner.setDisable(true);
            root.applyCss();
            root.layout();

            assertEquals(0, summary.getVisibleInvalidInputCount());
            assertFalse(summary.isShowingSummary());
            assertEquals(0, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

            summaryOwner.setDisable(false);
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getVisibleInvalidInputCount());
            assertEquals(1, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        });
    }

    /// Verifies that validation summary rows refresh when invalid input display text changes.
    @Test
    void validationSummaryRowsTrackInvalidInputTextChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField field = new M3TextField();
            field.setPromptText("Email");
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("");
            layout.setSupportingText("Required");
            layout.setValidator(M3TextInputValidators.required("Email is required"));
            M3FormValidator validator = new M3FormValidator(layout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            VBox root = new VBox(summary, layout);
            Stage stage = new Stage();
            Scene scene = new Scene(root, 520.0, 260.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            assertFalse(validator.validate());
            root.applyCss();
            root.layout();

            Node row = firstValidationSummaryItem(summary);
            Label itemLabel = assertInstanceOf(
                    Label.class,
                    row.lookup("." + "m3-validation-summary-item-label")
            );
            Label itemError = assertInstanceOf(
                    Label.class,
                    row.lookup("." + "m3-validation-summary-item-error")
            );
            assertEquals("Email", itemLabel.getText());
            assertEquals("Email is required", itemError.getText());

            field.setPromptText("Work email");
            root.applyCss();
            root.layout();

            assertSame(row, firstValidationSummaryItem(summary));
            itemLabel = assertInstanceOf(
                    Label.class,
                    row.lookup("." + "m3-validation-summary-item-label")
            );
            assertEquals("Work email", itemLabel.getText());

            layout.setLabelText("Account email");
            root.applyCss();
            root.layout();

            assertSame(row, firstValidationSummaryItem(summary));
            itemLabel = assertInstanceOf(
                    Label.class,
                    row.lookup("." + "m3-validation-summary-item-label")
            );
            assertEquals("Account email", itemLabel.getText());

            layout.setValidator(M3TextInputValidators.required("Account email is required"));
            root.applyCss();
            root.layout();

            assertSame(row, firstValidationSummaryItem(summary));
            itemError = assertInstanceOf(
                    Label.class,
                    row.lookup("." + "m3-validation-summary-item-error")
            );
            assertEquals("Account email is required", itemError.getText());
        });
    }

    /// Verifies that replacing a validation summary skin detaches retired content listeners.
    @Test
    void replacingValidationSummarySkinDetachesRetiredInputListeners() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout layout = requiredLayout("Account");
            M3FormValidator validator = new M3FormValidator(layout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            VBox root = new VBox(summary, layout);
            Scene scene = new Scene(root, 520.0, 260.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            assertFalse(validator.validate());
            root.applyCss();
            root.layout();

            Node retiredRow = firstValidationSummaryItem(summary);
            Label retiredError = assertInstanceOf(
                    Label.class,
                    retiredRow.lookup("." + "m3-validation-summary-item-error")
            );
            assertEquals("Account is required", retiredError.getText());

            FxTestUtils.replaceSkin(summary, M3ValidationSummarySkin::new);
            layout.setValidator(M3TextInputValidators.required("Enter an account"));
            root.applyCss();
            root.layout();

            assertEquals("Account is required", retiredError.getText());
            Node currentRow = firstValidationSummaryItem(summary);
            assertNotSame(retiredRow, currentRow);
            Label currentError = assertInstanceOf(
                    Label.class,
                    currentRow.lookup("." + "m3-validation-summary-item-error")
            );
            assertEquals("Enter an account", currentError.getText());
        });
    }

    /// Verifies that text input presentation is detached when its skin or input is replaced.
    @Test
    void textInputLayoutRestoresReplacedInputAndDetachesRetiredSkin() {
        FxTestUtils.runOnFxThread(() -> {
            Insets originalPadding = new Insets(2.0, 3.0, 4.0, 5.0);
            M3TextField originalInput = new M3TextField("M3FX");
            originalInput.setPadding(originalPadding);
            originalInput.setTranslateY(6.0);

            M3TextInputLayout layout = new M3TextInputLayout(originalInput);
            layout.setLabelText("Original label");
            VBox root = new VBox(layout);
            Scene scene = new Scene(root, 420.0, 160.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.layout();

            assertInstanceOf(M3TextInputLayoutSkin.class, layout.getSkin());
            Text retiredLabel = assertInstanceOf(
                    Text.class,
                    layout.lookup("." + "m3-text-input-label")
            );

            FxTestUtils.replaceSkin(layout, M3TextInputLayoutSkin::new);
            layout.setLabelText("Replacement label");
            root.applyCss();
            root.layout();

            Text currentLabel = assertInstanceOf(
                    Text.class,
                    layout.lookup("." + "m3-text-input-label")
            );
            assertNotSame(retiredLabel, currentLabel);
            assertEquals("Original label", retiredLabel.getText());
            assertEquals("Replacement label", currentLabel.getText());

            M3TextField replacementInput = new M3TextField();
            layout.setInput(replacementInput);
            root.applyCss();
            root.layout();

            assertNull(originalInput.getParent());
            assertEquals(originalPadding, originalInput.getPadding());
            assertEquals(6.0, originalInput.getTranslateY(), 0.0);
            assertFalse(originalInput.getStyleClass().contains("m3-text-input-layout-input"));
            assertSame(replacementInput, layout.getInput());
            assertNotNull(replacementInput.getParent());
            assertTrue(replacementInput.getStyleClass().contains("m3-text-input-layout-input"));
            assertFalse(layout.isLabelFloating());

            originalInput.setText("Detached input update");
            assertFalse(layout.isLabelFloating());
        });
    }

    /// Verifies that validation summaries follow only the active validator and its live input list.
    @Test
    void validationSummaryTracksValidatorReplacementAndInputListChanges() {
        FxTestUtils.runOnFxThread(() -> {
            PseudoClass empty = PseudoClass.getPseudoClass("empty");
            M3TextField firstField = new M3TextField();
            M3TextInputLayout firstLayout = new M3TextInputLayout(firstField);
            firstLayout.setLabelText("First");
            firstLayout.setSupportingText("Required");
            firstLayout.setValidator(M3TextInputValidators.required("First is required"));

            M3TextField secondField = new M3TextField();
            M3TextInputLayout secondLayout = new M3TextInputLayout(secondField);
            secondLayout.setLabelText("Second");
            secondLayout.setSupportingText("Required");
            secondLayout.setValidator(M3TextInputValidators.required("Second is required"));

            M3TextField thirdField = new M3TextField();
            M3TextInputLayout thirdLayout = new M3TextInputLayout(thirdField);
            thirdLayout.setLabelText("Third");
            thirdLayout.setSupportingText("Required");
            thirdLayout.setValidator(M3TextInputValidators.required("Third is required"));

            M3FormValidator firstValidator = new M3FormValidator(firstLayout);
            M3FormValidator secondValidator = new M3FormValidator(secondLayout);
            M3ValidationSummary summary = new M3ValidationSummary(firstValidator);
            VBox root = new VBox(summary, firstLayout, secondLayout, thirdLayout);
            Stage stage = new Stage();
            Scene scene = new Scene(root, 640.0, 360.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            assertFalse(firstValidator.validate());
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getInvalidInputCount());
            assertSame(firstLayout, summary.getInvalidInput(0));
            assertEquals(1, summary.lookupAll("." + "m3-validation-summary-item").size());

            summary.setValidator(secondValidator);
            assertFalse(secondValidator.validate());
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getInvalidInputCount());
            assertSame(secondLayout, summary.getInvalidInput(0));
            assertEquals(1, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

            firstField.setText("No longer invalid");
            assertTrue(firstValidator.validate());
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getInvalidInputCount());
            assertSame(secondLayout, summary.getInvalidInput(0));

            secondValidator.getInputs().add(thirdLayout);
            assertFalse(secondValidator.validate());
            root.applyCss();
            root.layout();

            assertEquals(2, summary.getInvalidInputCount());
            assertSame(secondLayout, summary.getInvalidInput(0));
            assertSame(thirdLayout, summary.getInvalidInput(1));
            assertEquals(2, summary.lookupAll("." + "m3-validation-summary-item").size());

            assertTrue(secondValidator.getInputs().remove(secondLayout));
            root.applyCss();
            root.layout();

            assertEquals(1, summary.getInvalidInputCount());
            assertSame(thirdLayout, summary.getInvalidInput(0));
            assertEquals(1, summary.lookupAll("." + "m3-validation-summary-item").size());

            secondValidator.getInputs().clear();
            root.applyCss();
            root.layout();

            assertEquals(0, summary.getInvalidInputCount());
            assertFalse(summary.isShowingSummary());
            assertTrue(summary.getPseudoClassStates().contains(empty));
        });
    }

    /// Verifies that pointer activation only fires when a validation summary row is released inside its bounds.
    @Test
    void validationSummaryRowsActivateOnlyWhenReleasedInside() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button focusSentinel = new M3Button("Before");
            M3TextField field = new M3TextField();
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("Display name");
            layout.setSupportingText("Required");
            layout.setValidator(M3TextInputValidators.required("Display name is required"));
            M3FormValidator validator = new M3FormValidator(layout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            VBox root = new VBox(focusSentinel, summary, layout);
            Stage stage = new Stage();
            Scene scene = new Scene(root, 520.0, 260.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            assertFalse(validator.validate());
            root.applyCss();
            root.layout();

            Node row = firstValidationSummaryItem(summary);
            Bounds bounds = row.getLayoutBounds();
            double centerX = bounds.getMinX() + bounds.getWidth() / 2.0;
            double centerY = bounds.getMinY() + bounds.getHeight() / 2.0;
            double outsideX = bounds.getMaxX() + 24.0;
            focusSentinel.requestFocus();
            assertTrue(focusSentinel.isFocused());

            row.fireEvent(primaryMouseEvent(row, MouseEvent.MOUSE_PRESSED, centerX, centerY, true));
            row.fireEvent(primaryMouseEvent(row, MouseEvent.MOUSE_RELEASED, outsideX, centerY, false));

            assertFalse(field.isFocused());
            assertTrue(focusSentinel.isFocused());

            row.fireEvent(primaryMouseEvent(row, MouseEvent.MOUSE_PRESSED, centerX, centerY, true));
            row.fireEvent(primaryMouseEvent(row, MouseEvent.MOUSE_RELEASED, centerX, centerY, false));

            assertTrue(field.isFocused());
        });
    }

    /// Verifies that validation summary rows expose Material state layer and ripple feedback.
    @Tier2Test
    @Test
    void validationSummaryRowsExposeMaterialStateLayerFeedback() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3TextField> fieldReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> rowReference = new AtomicReference<>();
        AtomicReference<@Nullable Node> rippleReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable Node ripple = rippleReference.get();
                        return ripple != null && ripple.getOpacity() > 0.0;
                    },
                    () -> "Timed out waiting for validation summary pointer ripple",
                    () -> {
                        M3TextField field = new M3TextField();
                        M3TextInputLayout layout = new M3TextInputLayout(field);
                        layout.setLabelText("Display name");
                        layout.setSupportingText("Required");
                        layout.setValidator(M3TextInputValidators.required("Display name is required"));
                        M3FormValidator validator = new M3FormValidator(layout);
                        M3ValidationSummary summary = new M3ValidationSummary(validator);
                        VBox root = new VBox(summary, layout);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 520.0, 260.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        assertFalse(validator.validate());
                        root.applyCss();
                        root.layout();

                        Node row = firstValidationSummaryItem(summary);
                        Node stateLayer = Objects.requireNonNull(
                                row.lookup(".m3-state-layer-container"),
                                "validation summary item state layer"
                        );
                        Node ripple = Objects.requireNonNull(
                                row.lookup(".m3-ripple"),
                                "validation summary item ripple"
                        );
                        assertNotNull(stateLayer.lookup(".m3-focus-indicator"));

                        stageReference.set(stage);
                        fieldReference.set(field);
                        rowReference.set(row);
                        rippleReference.set(ripple);
                        firePrimaryMouseEvent(row, MouseEvent.MOUSE_PRESSED, true);
                    },
                    () -> {
                        Node row = Objects.requireNonNull(rowReference.get(), "validation summary item");
                        Node ripple = Objects.requireNonNull(rippleReference.get(), "validation summary item ripple");
                        assertTrue(ripple.getOpacity() > 0.0, () -> "ripple opacity=" + ripple.getOpacity());
                        firePrimaryMouseEvent(row, MouseEvent.MOUSE_RELEASED, false);
                        assertTrue(Objects.requireNonNull(fieldReference.get(), "invalid field").isFocused());
                    }
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable Node ripple = rippleReference.get();
                        return ripple != null && ripple.getOpacity() <= 0.001;
                    },
                    () -> "Timed out waiting for validation summary ripple release",
                    () -> {
                    },
                    () -> assertTrue(Objects.requireNonNull(rippleReference.get(), "validation summary item ripple")
                            .getOpacity() <= 0.001)
            );

            FxTestUtils.runOnFxThread(() -> {
                M3TextField field = Objects.requireNonNull(fieldReference.get(), "invalid field");
                Node row = Objects.requireNonNull(rowReference.get(), "validation summary item");
                Node ripple = Objects.requireNonNull(rippleReference.get(), "validation summary item ripple");
                row.requestFocus();
                row.fireEvent(keyPressed(KeyCode.SPACE));

                assertTrue(field.isFocused());
                assertTrue(ripple.getOpacity() > 0.0, () -> "keyboard ripple opacity=" + ripple.getOpacity());
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies keyboard navigation inside validation summary rows reveals the focused row in a scroll pane.
    @Test
    void validationSummaryKeyboardNavigationRevealsFocusedItem() {
        FxTestUtils.runOnFxThread(() -> {
            ArrayList<M3TextInputLayout> layouts = new ArrayList<>();
            for (int index = 0; index < 8; index++) {
                M3TextField field = new M3TextField();
                M3TextInputLayout layout = new M3TextInputLayout(field);
                layout.setLabelText("Field " + index);
                layout.setSupportingText("Required");
                layout.setValidator(M3TextInputValidators.required("Field " + index + " is required"));
                layouts.add(layout);
            }

            M3FormValidator validator = new M3FormValidator(layouts.toArray(M3TextInputLayout[]::new));
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            assertFalse(validator.validate());

            VBox content = new VBox(summary);
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(280.0, 120.0);
            Stage stage = new Stage();
            Scene scene = new Scene(scrollPane, 280.0, 120.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            stage.setScene(scene);
            stage.show();
            scrollPane.applyCss();
            scrollPane.resize(280.0, 120.0);
            scrollPane.layout();
            content.layout();
            summary.layout();

            ArrayList<Node> rows = new ArrayList<>(summary.lookupAll("." + "m3-validation-summary-item"));
            rows.sort(Comparator.comparingDouble(row -> row.getBoundsInParent().getMinY()));
            assertEquals(8, rows.size());
            Node firstRow = rows.get(0);
            Node lastRow = rows.get(rows.size() - 1);

            firstRow.requestFocus();
            firstRow.fireEvent(keyPressed(KeyCode.END));

            assertSame(lastRow, scene.getFocusOwner());
            assertTrue(scrollPane.getVvalue() > 0.0, () -> "vvalue=" + scrollPane.getVvalue());
        });
    }

    /// Creates one empty required input layout for aggregate validation tests.
    private static M3TextInputLayout requiredLayout(String label) {
        M3TextInputLayout layout = new M3TextInputLayout(new M3TextField());
        layout.setLabelText(label);
        layout.setSupportingText("Required");
        layout.setValidator(M3TextInputValidators.required(label + " is required"));
        return layout;
    }

    /// Runs a property mutation and returns an exception reported through JavaFX listener dispatch.
    ///
    /// JavaFX reports exceptions raised by invalidation listeners to the current thread's uncaught-exception
    /// handler instead of propagating them through the source-property setter.
    ///
    /// @param mutation the property mutation to run
    /// @return the exception reported by JavaFX
    private static Throwable captureUncaughtListenerException(Runnable mutation) {
        Thread thread = Thread.currentThread();
        Thread.UncaughtExceptionHandler previousHandler = thread.getUncaughtExceptionHandler();
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        thread.setUncaughtExceptionHandler((ignoredThread, exception) -> failure.set(exception));
        try {
            mutation.run();
        } finally {
            thread.setUncaughtExceptionHandler(previousHandler);
        }
        return Objects.requireNonNull(failure.get(), "listener exception");
    }

    /// Returns the first rendered validation-summary item row.
    private static Node firstValidationSummaryItem(M3ValidationSummary summary) {
        return summary.lookupAll("." + "m3-validation-summary-item")
                .stream()
                .min(Comparator.comparingDouble(row -> row.getBoundsInParent().getMinY()))
                .orElseThrow(() -> new AssertionError("validation summary item"));
    }

    /// Fires a primary-button mouse event at the center of a node's layout bounds.
    private static void firePrimaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            boolean primaryButtonDown
    ) {
        Bounds bounds = node.getLayoutBounds();
        double x = bounds.getMinX() + bounds.getWidth() / 2.0;
        double y = bounds.getMinY() + bounds.getHeight() / 2.0;
        node.fireEvent(primaryMouseEvent(node, eventType, x, y, primaryButtonDown));
    }

    /// Creates a primary-button mouse event at one local point of a node.
    private static MouseEvent primaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                scenePoint.getX(),
                scenePoint.getY(),
                screenX,
                screenY,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(node, scenePoint.getX(), scenePoint.getY())
        );
    }

    /// Creates a key-pressed event for control keyboard behavior tests.
    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(
                KeyEvent.KEY_PRESSED,
                code.getName(),
                code.getName(),
                code,
                false,
                false,
                false,
                false
        );
    }

    /// Applies the M3FX theme to a node and creates its skin.
    private static void applyCss(Node node) {
        Pane root = new Pane(node);
        Scene scene = new Scene(root, 640.0, 320.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }
}
