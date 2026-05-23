// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.skins.M3FormPaneSkin;
import org.glavo.m3fx.skins.M3FormRowSkin;
import org.glavo.m3fx.skins.M3FormSectionSkin;
import org.glavo.m3fx.skins.M3ValidationSummarySkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    /// Verifies that form panes expose items through the skin and accessibility metadata.
    @Test
    void formPaneMirrorsItemsIntoSkin() {
        runOnFxThread(() -> {
            M3FormRow first = new M3FormRow("Name", new Label("Content"));
            M3FormRow second = new M3FormRow("Email", new Label("Content"));
            M3FormPane form = new M3FormPane(first);
            form.addItem(second);
            form.setContentPadding(20.0);
            form.setRowSpacing(10.0);
            form.setStyle("-m3-content-padding: 20px; -m3-row-spacing: 10px;");

            applyCss(form);

            assertInstanceOf(M3FormPaneSkin.class, form.getSkin());
            assertEquals(2, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(first, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertSame(second, form.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));

            Node content = form.lookup("." + M3FormPane.CONTENT_STYLE_CLASS);
            VBox contentBox = assertInstanceOf(VBox.class, content);
            assertEquals(2, contentBox.getChildren().size());
            assertEquals(10.0, contentBox.getSpacing());
        });
    }

    /// Verifies that form sections update title, supporting text, and content slots.
    @Test
    void formSectionMirrorsHeaderAndContent() {
        runOnFxThread(() -> {
            M3FormRow row = new M3FormRow("Field", new Label("Value"));
            M3FormSection section = new M3FormSection("Account", "Profile fields", row);
            section.setContentSpacing(18.0);
            section.setStyle("-m3-content-spacing: 18px;");

            applyCss(section);

            assertInstanceOf(M3FormSectionSkin.class, section.getSkin());
            assertEquals("Account", section.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(1, section.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(row, section.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

            Label title = assertInstanceOf(Label.class, section.lookup("." + M3FormSection.TITLE_STYLE_CLASS));
            Label supporting =
                    assertInstanceOf(Label.class, section.lookup("." + M3FormSection.SUPPORTING_TEXT_STYLE_CLASS));
            VBox content = assertInstanceOf(VBox.class, section.lookup("." + M3FormSection.CONTENT_STYLE_CLASS));

            assertEquals("Account", title.getText());
            assertEquals("Profile fields", supporting.getText());
            assertEquals(18.0, content.getSpacing());
            assertEquals(1, content.getChildren().size());
        });
    }

    /// Verifies that form rows update text, slots, metrics, and accessibility metadata.
    @Test
    void formRowMirrorsTextAndSlotsIntoSkin() {
        runOnFxThread(() -> {
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

            Label label = assertInstanceOf(Label.class, row.lookup("." + M3FormRow.LABEL_STYLE_CLASS));
            Label supporting = assertInstanceOf(Label.class, row.lookup("." + M3FormRow.SUPPORTING_TEXT_STYLE_CLASS));
            VBox textColumn = assertInstanceOf(VBox.class, row.lookup("." + M3FormRow.TEXT_COLUMN_STYLE_CLASS));

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

    /// Verifies that form helpers expose their split user-agent stylesheet.
    @Test
    void formControlsExposeUserAgentStylesheet() {
        assertTrue(new M3FormPane().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(new M3FormSection().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(new M3FormRow().getUserAgentStylesheet().endsWith("/styles/controls/form.css"));
        assertTrue(new M3ValidationSummary().getUserAgentStylesheet()
                .endsWith("/styles/controls/validation-summary.css"));
    }

    /// Verifies that form validators coordinate text input validation state.
    @Test
    void formValidatorCoordinatesTextInputValidation() {
        runOnFxThread(() -> {
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Display name", "Required");
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));

            M3TextField emailField = new M3TextField("support@example.com");
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Format");
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
            assertTrue(validator.focusFirstInvalidInput());

            nameField.setText("M3FX Project");

            assertTrue(validator.validateAndFocusFirstInvalidInput());
            assertTrue(validator.isValid());
            assertTrue(validator.isValidationActive());
            assertEquals(0, validator.getInvalidInputCount());
            assertTrue(validator.getInvalidInputs().isEmpty());
            assertNull(validator.getFirstInvalidInput());
        });
    }

    /// Verifies that form validators track external validation updates.
    @Test
    void formValidatorTracksExternalValidationChanges() {
        runOnFxThread(() -> {
            M3TextField field = new M3TextField();
            M3TextInputLayout layout = new M3TextInputLayout(field, "Name", "Required");
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
        runOnFxThread(() -> {
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Display name", "Required");
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));

            M3TextField emailField = new M3TextField("support");
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Format");
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

    /// Verifies that form validators reject duplicate registrations and update list state.
    @Test
    void formValidatorMaintainsDistinctInputs() {
        runOnFxThread(() -> {
            M3TextInputLayout first = new M3TextInputLayout(new M3TextField());
            M3TextInputLayout second = new M3TextInputLayout(new M3TextField());
            M3FormValidator validator = new M3FormValidator(first);

            assertThrows(IllegalArgumentException.class, () -> validator.addInput(first));
            assertThrows(IllegalArgumentException.class, () -> validator.addInputs(second, second));
            assertTrue(validator.removeInput(first));
            assertFalse(validator.removeInput(first));
            assertTrue(validator.getInputs().isEmpty());
            validator.addInputs(first, second);
            assertEquals(List.of(first, second), validator.getInputs());
            validator.clearInputs();
            assertTrue(validator.getInputs().isEmpty());
            assertFalse(validator.focusFirstInvalidInput());
        });
    }

    /// Verifies that validation summaries render validator errors and expose accessibility targets.
    @Test
    void validationSummaryMirrorsValidatorErrorsIntoSkin() {
        runOnFxThread(() -> {
            PseudoClass empty = PseudoClass.getPseudoClass("empty");
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField, "Display name", "Required");
            nameLayout.setValidator(M3TextInputValidators.required("Display name is required"));

            M3TextField emailField = new M3TextField("support");
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField, "Email", "Format");
            emailLayout.setValidator(M3TextInputValidators.pattern(
                    Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                    "Enter a valid email address"
            ));

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            applyCss(new VBox(summary, nameLayout, emailLayout));

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
            assertEquals(2, summary.lookupAll("." + M3ValidationSummary.ITEM_STYLE_CLASS).size());
            assertTrue(summary.focusInput(nameLayout));
        });
    }

    /// Applies the M3FX theme to a node and creates its skin.
    private static void applyCss(Node node) {
        Pane root = new Pane(node);
        Scene scene = new Scene(root, 640.0, 320.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
    }

    /// Runs one assertion block on the JavaFX application thread.
    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }

        @Nullable Throwable throwable = failure.get();
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        if (throwable != null) {
            throw new AssertionError(throwable);
        }
    }
}
