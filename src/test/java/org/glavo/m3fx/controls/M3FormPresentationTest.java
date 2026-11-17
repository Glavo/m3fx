// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.glavo.m3fx.M3TestControls.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies form layout, field state, and validation presentation.
@NotNullByDefault
final class M3FormPresentationTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that form sections mirror text alignment when orientation changes at runtime.
    @Test
    void formSectionMirrorsTextAlignmentWhenOrientationChanges() {
        M3FormSection section = formSection("Account", new Label("Content"));
        section.setSupportingText("Profile settings");
        section.setPrefWidth(360.0);
        Pane root = new Pane(section);
        Scene scene = new Scene(root, 380.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        section.resize(360.0, 100.0);
        section.layout();

        assertFormSectionTextAlignment(
                section,
                Pos.TOP_LEFT,
                Pos.CENTER_LEFT,
                TextAlignment.LEFT,
                NodeOrientation.LEFT_TO_RIGHT
        );

        section.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        section.layout();

        assertFormSectionTextAlignment(
                section,
                Pos.TOP_LEFT,
                Pos.CENTER_RIGHT,
                TextAlignment.RIGHT,
                NodeOrientation.RIGHT_TO_LEFT
        );

        section.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        root.applyCss();
        section.layout();

        assertFormSectionTextAlignment(
                section,
                Pos.TOP_LEFT,
                Pos.CENTER_LEFT,
                TextAlignment.LEFT,
                NodeOrientation.LEFT_TO_RIGHT
        );
    }

    /// Verifies that form rows mirror text alignment when orientation changes at runtime.
    @Test
    void formRowMirrorsTextAlignmentWhenOrientationChanges() {
        M3FormRow row = new M3FormRow("Label", "Helper", new Label("Content"), new Label("Trailing"));
        row.setPrefWidth(480.0);
        Pane root = new Pane(row);
        Scene scene = new Scene(root, 520.0, 120.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        row.resize(480.0, 72.0);
        row.layout();

        assertFormRowTextAlignment(row, Pos.CENTER_LEFT, Pos.CENTER_RIGHT, TextAlignment.LEFT);

        row.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.applyCss();
        row.layout();

        assertFormRowTextAlignment(row, Pos.CENTER_LEFT, Pos.CENTER_RIGHT, TextAlignment.RIGHT);

        row.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        root.applyCss();
        row.layout();

        assertFormRowTextAlignment(row, Pos.CENTER_LEFT, Pos.CENTER_RIGHT, TextAlignment.LEFT);
    }

    /// Verifies that form rows mirror content and trailing slots in right-to-left layouts.
    @Test
    void formRowMirrorsLogicalSlotsForRightToLeft() {
        Label content = new Label("Content");
        Label trailing = new Label("Trailing");
        M3FormRow formRow = new M3FormRow("Label", "Helper", content, trailing);
        formRow.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Pane root = new Pane(formRow);
        Scene scene = new Scene(root, 520.0, 96.0);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();
        formRow.resize(480.0, 72.0);
        formRow.layout();

        Node labelColumn = Objects.requireNonNull(formRow.lookup("." + "m3-form-row-text-column"));
        assertFormRowTextAlignment(formRow, Pos.CENTER_LEFT, Pos.CENTER_RIGHT, TextAlignment.RIGHT);
        Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
        Bounds trailingBounds = trailing.localToScene(trailing.getBoundsInLocal());
        Bounds labelBounds = labelColumn.localToScene(labelColumn.getBoundsInLocal());

        assertTrue(labelBounds.getMinX() > contentBounds.getMaxX(),
                () -> "labelBounds=" + labelBounds + ", contentBounds=" + contentBounds);
        assertTrue(contentBounds.getMinX() > trailingBounds.getMaxX(),
                () -> "contentBounds=" + contentBounds + ", trailingBounds=" + trailingBounds);
    }

    /// Verifies that form containers support directional focus without stealing text editing keys.
    @Test
    void formContainersSupportDirectionalKeyboardTraversalWithoutStealingTextInputKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button paneFirst = new M3Button("Pane first");
            M3Button rowContent = new M3Button("Row content");
            M3Button rowTrailing = new M3Button("Row trailing");
            M3FormRow row = new M3FormRow("Row", "Helper", rowContent, rowTrailing);

            M3Button sectionFirst = new M3Button("Section first");
            M3Button hidden = new M3Button("Hidden");
            M3Button disabled = new M3Button("Disabled");
            M3Button sectionSecond = new M3Button("Section second");
            hidden.setVisible(false);
            disabled.setDisable(true);
            M3FormSection section = formSection("Section", sectionFirst, hidden, disabled, sectionSecond);

            M3TextField editor = new M3TextField("M3FX");
            M3Button inputTrailing = new M3Button("Input trailing");
            M3FormRow inputRow = new M3FormRow("Input", "Editor row", editor, inputTrailing);

            M3Button paneLast = new M3Button("Pane last");
            M3Button outside = new M3Button("Outside");
            M3FormPane formPane = formPane(paneFirst, row, section, inputRow, paneLast);
            VBox root = new VBox(8.0, formPane, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 680.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                rowContent.requestFocus();
                assertTrue(rowContent.isFocused());
                rowContent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(rowTrailing.isFocused());
                rowTrailing.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(rowTrailing.isFocused());

                row.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                rowContent.requestFocus();
                rowContent.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
                assertTrue(rowTrailing.isFocused());
                rowTrailing.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(rowContent.isFocused());

                sectionFirst.requestFocus();
                sectionFirst.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(sectionSecond.isFocused(), () -> "focused=" + scene.getFocusOwner());
                sectionSecond.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(editor.isFocused(), () -> "focused=" + scene.getFocusOwner());

                editor.positionCaret(1);
                editor.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
                assertTrue(editor.isFocused());
                assertFalse(inputTrailing.isFocused());
                assertFalse(paneLast.isFocused());

                inputTrailing.requestFocus();
                inputTrailing.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(paneLast.isFocused());
                paneLast.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(inputTrailing.isFocused());

                outside.requestFocus();
                formPane.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
                assertTrue(paneFirst.isFocused());
                paneFirst.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
                assertTrue(paneFirst.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that validation summaries mirror text alignment when orientation changes at runtime.
    @Test
    void validationSummaryMirrorsTextAlignmentWhenOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField field = new M3TextField();
            M3TextInputLayout layout = new M3TextInputLayout(field);
            layout.setLabelText("Display name");
            layout.setSupportingText("Required");
            layout.setValidator(M3TextInputValidators.required("Display name is required"));
            M3FormValidator validator = new M3FormValidator(layout);
            assertFalse(validator.validate());

            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setPrefWidth(360.0);
            Pane root = new Pane(summary);
            Scene scene = new Scene(root, 380.0, 120.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            summary.resize(360.0, 100.0);
            summary.layout();

            Label title = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-title")
            );
            Label itemLabel = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-item-label")
            );
            Label itemError = assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-item-error")
            );
            assertEquals(Pos.CENTER_LEFT, title.getAlignment());
            assertEquals(Pos.CENTER_LEFT, itemLabel.getAlignment());
            assertEquals(Pos.CENTER_LEFT, itemError.getAlignment());
            assertEquals(TextAlignment.LEFT, title.getTextAlignment());
            assertEquals(TextAlignment.LEFT, itemLabel.getTextAlignment());
            assertEquals(TextAlignment.LEFT, itemError.getTextAlignment());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, title.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, itemLabel.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, itemError.getEffectiveNodeOrientation());

            summary.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            summary.layout();

            title = assertInstanceOf(Label.class, summary.lookup("." + "m3-validation-summary-title"));
            itemLabel = assertInstanceOf(Label.class, summary.lookup("." + "m3-validation-summary-item-label"));
            itemError = assertInstanceOf(Label.class, summary.lookup("." + "m3-validation-summary-item-error"));
            assertEquals(Pos.CENTER_RIGHT, title.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, itemLabel.getAlignment());
            assertEquals(Pos.CENTER_RIGHT, itemError.getAlignment());
            assertEquals(TextAlignment.RIGHT, title.getTextAlignment());
            assertEquals(TextAlignment.RIGHT, itemLabel.getTextAlignment());
            assertEquals(TextAlignment.RIGHT, itemError.getTextAlignment());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, title.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, itemLabel.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, itemError.getEffectiveNodeOrientation());

            summary.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            root.applyCss();
            summary.layout();

            title = assertInstanceOf(Label.class, summary.lookup("." + "m3-validation-summary-title"));
            itemLabel = assertInstanceOf(Label.class, summary.lookup("." + "m3-validation-summary-item-label"));
            itemError = assertInstanceOf(Label.class, summary.lookup("." + "m3-validation-summary-item-error"));
            assertEquals(Pos.CENTER_LEFT, title.getAlignment());
            assertEquals(Pos.CENTER_LEFT, itemLabel.getAlignment());
            assertEquals(Pos.CENTER_LEFT, itemError.getAlignment());
            assertEquals(TextAlignment.LEFT, title.getTextAlignment());
            assertEquals(TextAlignment.LEFT, itemLabel.getTextAlignment());
            assertEquals(TextAlignment.LEFT, itemError.getTextAlignment());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, title.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, itemLabel.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, itemError.getEffectiveNodeOrientation());
        });
    }

    /// Verifies that validation summaries render invalid input rows in snapshots.
    @Tier2Test
    @Test
    void validationSummarySnapshotRendersInvalidInputRows() {
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

            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            summary.setPrefWidth(420.0);
            assertFalse(validator.validate());

            VBox root = new VBox(summary);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 500.0, 190.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(500.0, 190.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, summary, Color.WHITE, 0.04);
            assertEquals(2, summary.lookupAll("." + "m3-validation-summary-item").size());
            assertEquals("Display name", assertInstanceOf(
                    Label.class,
                    summary.lookup("." + "m3-validation-summary-item-label")
            ).getText());
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-validation-summary.png"
            ));
        });
    }

    /// Verifies that validation summaries route accessibility focus to the current invalid input.
    @Test
    void validationSummaryRoutesAccessibleFocusAcrossInvalidInputs() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField nameField = new M3TextField();
            M3TextInputLayout nameLayout = new M3TextInputLayout(nameField);
            nameLayout.setLabelText("Name");
            nameLayout.setSupportingText("Required");
            nameLayout.setValidator(M3TextInputValidators.required("Name is required"));

            M3TextField emailField = new M3TextField();
            M3TextInputLayout emailLayout = new M3TextInputLayout(emailField);
            emailLayout.setLabelText("Email");
            emailLayout.setSupportingText("Required");
            emailLayout.setValidator(M3TextInputValidators.required("Email is required"));

            M3Button outside = new M3Button("Outside");
            M3FormValidator validator = new M3FormValidator(nameLayout, emailLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            assertFalse(validator.validate());

            VBox root = new VBox(8.0, nameLayout, emailLayout, summary, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 520.0, 320.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(2, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(nameLayout, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(emailLayout, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                VBox items = assertInstanceOf(
                        VBox.class,
                        summary.lookup("." + "m3-validation-summary-items")
                );
                Node nameSummaryItem = items.getChildren().get(0);
                Node emailSummaryItem = items.getChildren().get(1);

                nameSummaryItem.requestFocus();
                assertTrue(nameSummaryItem.isFocused());

                nameSummaryItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(emailSummaryItem.isFocused());

                emailSummaryItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));

                assertTrue(emailSummaryItem.isFocused());

                emailSummaryItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));

                assertTrue(nameSummaryItem.isFocused());

                nameSummaryItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(emailSummaryItem.isFocused());

                emailSummaryItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

                assertTrue(nameSummaryItem.isFocused());

                nameSummaryItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE));

                assertTrue(nameField.isFocused());
                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, emailLayout);

                assertTrue(emailField.isFocused());
                assertSame(emailField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, List.of(nameField));

                assertTrue(nameField.isFocused());
                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                emailSummaryItem.requestFocus();
                assertTrue(emailSummaryItem.isFocused());

                emailSummaryItem.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));

                assertTrue(emailField.isFocused());
                assertSame(emailField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 1);

                assertTrue(emailField.isFocused());
                assertSame(emailField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                emailField.setText("support@example.com");

                assertEquals(1, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(nameLayout, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                outside.requestFocus();

                assertTrue(outside.isFocused());
                assertSame(nameField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                nameField.setText("Ada");

                assertEquals(0, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertNull(summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that form validators focus the first reachable invalid input.
    @Test
    void formValidatorFocusSkipsInvalidInputsHiddenByAncestors() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField hiddenField = new M3TextField();
            M3TextInputLayout hiddenLayout = new M3TextInputLayout(hiddenField);
            hiddenLayout.setLabelText("Hidden");
            hiddenLayout.setSupportingText("Required");
            hiddenLayout.setValidator(M3TextInputValidators.required("Hidden is required"));
            Pane hiddenAncestor = new Pane(hiddenLayout);
            hiddenAncestor.setVisible(false);

            M3TextField visibleField = new M3TextField();
            M3TextInputLayout visibleLayout = new M3TextInputLayout(visibleField);
            visibleLayout.setLabelText("Visible");
            visibleLayout.setSupportingText("Required");
            visibleLayout.setValidator(M3TextInputValidators.required("Visible is required"));

            M3Button outside = new M3Button("Outside");
            M3FormValidator validator = new M3FormValidator(hiddenLayout, visibleLayout);
            VBox root = new VBox(8.0, hiddenAncestor, visibleLayout, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 520.0, 280.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertFalse(validator.validate());
                assertTrue(validator.focusFirstInvalidInput());
                assertTrue(visibleField.isFocused());
                assertFalse(hiddenField.isFocused());

                outside.requestFocus();
                visibleLayout.setVisible(false);

                assertFalse(validator.focusFirstInvalidInput());
                assertTrue(outside.isFocused());
                assertFalse(hiddenField.isFocused());
                assertFalse(visibleField.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that validation summary accessibility skips invalid inputs hidden by ancestors.
    @Test
    void validationSummarySkipsInvalidInputsHiddenByAncestors() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField hiddenField = new M3TextField();
            M3TextInputLayout hiddenLayout = new M3TextInputLayout(hiddenField);
            hiddenLayout.setLabelText("Hidden");
            hiddenLayout.setSupportingText("Required");
            hiddenLayout.setValidator(M3TextInputValidators.required("Hidden is required"));
            Pane hiddenAncestor = new Pane(hiddenLayout);
            hiddenAncestor.setVisible(false);

            M3TextField visibleField = new M3TextField();
            M3TextInputLayout visibleLayout = new M3TextInputLayout(visibleField);
            visibleLayout.setLabelText("Visible");
            visibleLayout.setSupportingText("Required");
            visibleLayout.setValidator(M3TextInputValidators.required("Visible is required"));

            M3Button outside = new M3Button("Outside");
            M3FormValidator validator = new M3FormValidator(hiddenLayout, visibleLayout);
            M3ValidationSummary summary = new M3ValidationSummary(validator);
            VBox root = new VBox(8.0, hiddenAncestor, visibleLayout, summary, outside);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 520.0, 360.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                assertFalse(validator.validate());
                root.applyCss();
                root.layout();

                PseudoClass empty = PseudoClass.getPseudoClass("empty");
                assertEquals(2, summary.getInvalidInputCount());
                assertEquals(1, summary.getVisibleInvalidInputCount());
                assertFalse(summary.getPseudoClassStates().contains(empty));
                assertFalse(summary.isInvalidInputShown(hiddenLayout));
                assertFalse(summary.isInvalidInputReachable(hiddenLayout));
                assertTrue(summary.isInvalidInputShown(visibleLayout));
                assertTrue(summary.isInvalidInputReachable(visibleLayout));
                assertEquals(1, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertSame(visibleLayout, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertNull(summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertSame(visibleField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                VBox items = assertInstanceOf(
                        VBox.class,
                        summary.lookup("." + "m3-validation-summary-items")
                );
                assertEquals(1, items.getChildren().size());
                Label itemLabel = assertInstanceOf(
                        Label.class,
                        items.getChildren().get(0).lookup("." + "m3-validation-summary-item-label")
                );
                assertEquals("Visible", itemLabel.getText());

                outside.requestFocus();
                assertFalse(summary.focusInput(hiddenLayout));

                assertTrue(outside.isFocused());
                assertFalse(hiddenField.isFocused());

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, hiddenLayout);

                assertTrue(outside.isFocused());
                assertFalse(hiddenField.isFocused());

                summary.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);

                assertTrue(visibleField.isFocused());
                assertSame(visibleField, summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                hiddenAncestor.setVisible(true);
                root.applyCss();
                root.layout();

                assertTrue(summary.isInvalidInputShown(hiddenLayout));
                assertTrue(summary.isInvalidInputReachable(hiddenLayout));
                assertEquals(2, summary.getVisibleInvalidInputCount());
                assertEquals(2, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertEquals(2, summary.lookupAll("." + "m3-validation-summary-item").size());

                visibleLayout.setDisable(true);
                root.applyCss();
                root.layout();

                assertFalse(summary.isInvalidInputShown(visibleLayout));
                assertFalse(summary.isInvalidInputReachable(visibleLayout));
                assertEquals(1, summary.getVisibleInvalidInputCount());
                assertEquals(1, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                items = assertInstanceOf(
                        VBox.class,
                        summary.lookup("." + "m3-validation-summary-items")
                );
                assertEquals(1, items.getChildren().size());
                itemLabel = assertInstanceOf(
                        Label.class,
                        items.getChildren().get(0).lookup("." + "m3-validation-summary-item-label")
                );
                assertEquals("Hidden", itemLabel.getText());

                hiddenAncestor.setVisible(false);
                root.applyCss();
                root.layout();

                assertFalse(summary.isShowingSummary());
                assertEquals(0, summary.getVisibleInvalidInputCount());
                assertTrue(summary.getPseudoClassStates().contains(empty));
                assertTrue(summary.lookupAll("." + "m3-validation-summary-item").isEmpty());
                assertEquals(0, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

                hiddenAncestor.setVisible(true);
                visibleLayout.setDisable(false);
                root.applyCss();
                root.layout();

                assertTrue(summary.isShowingSummary());
                assertEquals(2, summary.getVisibleInvalidInputCount());
                assertFalse(summary.getPseudoClassStates().contains(empty));
                assertEquals(2, summary.lookupAll("." + "m3-validation-summary-item").size());

                outside.requestFocus();
                summary.setVisible(false);
                root.applyCss();
                root.layout();

                assertEquals(0, summary.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertNull(summary.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertFalse(summary.focusInput(visibleLayout));
                assertTrue(outside.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies the resolved logical text alignment of a form row.
    private static void assertFormRowTextAlignment(
            M3FormRow row,
            Pos startAlignment,
            Pos endAlignment,
            TextAlignment textAlignment
    ) {
        VBox textColumn = assertInstanceOf(VBox.class, row.lookup("." + "m3-form-row-text-column"));
        Label label = assertInstanceOf(Label.class, row.lookup("." + "m3-form-row-label"));
        Label supporting = assertInstanceOf(Label.class, row.lookup("." + "m3-form-row-supporting-text"));
        StackPane contentSlot = assertInstanceOf(StackPane.class, row.lookup("." + "m3-form-row-content"));
        StackPane trailingSlot = assertInstanceOf(StackPane.class, row.lookup("." + "m3-form-row-trailing"));

        assertEquals(startAlignment, textColumn.getAlignment());
        assertEquals(startAlignment, label.getAlignment());
        assertEquals(startAlignment, supporting.getAlignment());
        assertEquals(textAlignment, label.getTextAlignment());
        assertEquals(textAlignment, supporting.getTextAlignment());
        assertEquals(startAlignment, contentSlot.getAlignment());
        assertEquals(endAlignment, trailingSlot.getAlignment());
    }

    /// Verifies the resolved logical text alignment of a form section.
    private static void assertFormSectionTextAlignment(
            M3FormSection section,
            Pos topAlignment,
            Pos centerAlignment,
            TextAlignment textAlignment,
            NodeOrientation nodeOrientation
    ) {
        VBox header = assertInstanceOf(VBox.class, section.lookup("." + "m3-form-section-header"));
        VBox content = assertInstanceOf(VBox.class, section.lookup("." + "m3-form-section-content"));
        Label title = assertInstanceOf(Label.class, section.lookup("." + "m3-form-section-title"));
        Label supporting = assertInstanceOf(
                Label.class,
                section.lookup("." + "m3-form-section-supporting-text")
        );

        assertEquals(topAlignment, header.getAlignment());
        assertEquals(topAlignment, content.getAlignment());
        assertEquals(nodeOrientation, header.getEffectiveNodeOrientation());
        assertEquals(nodeOrientation, content.getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, title.getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, supporting.getEffectiveNodeOrientation());
        assertEquals(centerAlignment, title.getAlignment());
        assertEquals(centerAlignment, supporting.getAlignment());
        assertEquals(textAlignment, title.getTextAlignment());
        assertEquals(textAlignment, supporting.getTextAlignment());
    }
}
