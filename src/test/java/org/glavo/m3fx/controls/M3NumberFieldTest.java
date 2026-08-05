// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.skins.M3NumberFieldSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotHasColorVariety;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.snapshotImageOnFxThread;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.visualTestColors;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.writeVisualSnapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies number-field value editing, localization, stepping, accessibility, rendering, and skin lifecycle.
@NotNullByDefault
final class M3NumberFieldTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies stable defaults, exact programmatic values, and numeric configuration validation.
    @Test
    void exposesStableDefaultsAndValidatesConfiguration() {
        M3NumberField field = new M3NumberField(12.5);

        assertEquals(AccessibleRole.SPINNER, field.getAccessibleRole());
        assertEquals(12.5, field.getValue());
        assertFalse(field.getText().isEmpty());
        assertEquals(-Double.MAX_VALUE, field.getMin());
        assertEquals(Double.MAX_VALUE, field.getMax());
        assertEquals(1.0, field.getStep());
        assertEquals(M3NumberFieldCommitBehavior.SNAP, field.getCommitBehavior());
        assertFalse(field.isHideStepper());
        assertFalse(field.isWheelDisabled());
        assertTrue(field.isEditable());
        assertNotNull(field.getFormatter());
        assertTrue(field.getStyleClass().contains("m3-number-field"));

        field.setValue(null);
        assertNull(field.getValue());
        assertEquals("", field.getText());

        assertThrows(IllegalArgumentException.class, () -> field.setValue(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> field.setMin(Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> field.setMax(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> field.setStep(0.0));
        assertThrows(IllegalArgumentException.class, () -> field.increment(-1));
        assertThrows(IllegalArgumentException.class, () -> field.decrement(-1));

        field.setMin(2.0);
        field.setMax(10.0);
        assertThrows(IllegalArgumentException.class, () -> field.setMin(11.0));
        assertThrows(IllegalArgumentException.class, () -> field.setMax(1.0));
    }

    /// Verifies that snap commits clamp to the range and choose an in-range step anchored at the minimum.
    @Test
    void snapCommitClampsAndAlignsToMinimumAnchoredSteps() {
        M3NumberField field = decimalField(2.0, 10.0, 5.0, 3.0);

        field.setText("10");
        assertTrue(field.commitEditorText());
        assertEquals(8.0, field.getValue());
        assertEquals("8", field.getText());

        field.setText("1");
        assertTrue(field.commitEditorText());
        assertEquals(2.0, field.getValue());

        field.setText("6.4");
        assertTrue(field.commitEditorText());
        assertEquals(5.0, field.getValue());

        field.setText("");
        assertTrue(field.commitEditorText());
        assertNull(field.getValue());
    }

    /// Verifies that validate commits preserve invalid text and the previous committed value.
    @Test
    void validateCommitRejectsRangeStepAndParseErrors() {
        M3NumberField field = decimalField(2.0, 11.0, 5.0, 3.0);
        field.setCommitBehavior(M3NumberFieldCommitBehavior.VALIDATE);

        field.setText("8");
        assertTrue(field.commitEditorText());
        assertEquals(8.0, field.getValue());

        field.setText("9");
        assertFalse(field.commitEditorText());
        assertEquals(8.0, field.getValue());
        assertEquals("9", field.getText());
        assertEquals("", field.getErrorText());
        assertEquals(field.getRangeErrorText(), field.getValidationErrorText());
        assertTrue(field.isValidationActive());

        field.setText("not a number");
        assertEquals("", field.getErrorText());
        assertEquals(field.getInvalidTextErrorText(), field.getValidationErrorText());
        assertFalse(field.commitEditorText());
        assertEquals(8.0, field.getValue());
        assertEquals(field.getInvalidTextErrorText(), field.getValidationErrorText());

        field.setText("5 trailing");
        assertFalse(field.commitEditorText());
        assertEquals(8.0, field.getValue());
    }

    /// Verifies that application errors and generated numeric validation have independent ownership.
    @Test
    void preservesExplicitErrorsAcrossValidationAndSuccessfulCommits() {
        M3NumberField field = decimalField(0.0, 10.0, 4.0, 2.0);
        field.setCommitBehavior(M3NumberFieldCommitBehavior.VALIDATE);
        field.setErrorText("Server rejected this value");

        field.setText("5");
        assertFalse(field.commitEditorText());
        assertEquals("Server rejected this value", field.getErrorText());
        assertEquals(field.getRangeErrorText(), field.getValidationErrorText());

        field.setText("6");
        assertTrue(field.commitEditorText());
        assertEquals(6.0, field.getValue());
        assertEquals("Server rejected this value", field.getErrorText());
        assertEquals("", field.getValidationErrorText());

        field.setErrorText(field.getInvalidTextErrorText());
        field.setText("invalid");
        assertFalse(field.commitEditorText());
        field.setText("8");
        field.clearValidation();
        assertEquals(field.getInvalidTextErrorText(), field.getErrorText());
        assertEquals("", field.getValidationErrorText());
    }

    /// Verifies that control actions never write a unidirectionally bound committed-value property.
    @Test
    void treatsUnidirectionallyBoundValuesAsReadOnly() {
        M3NumberField field = decimalField(0.0, 10.0, 4.0, 2.0);
        ObjectProperty<@Nullable Double> source = new SimpleObjectProperty<>(4.0);
        field.valueProperty().bind(source);

        assertFalse(field.getEditor().isEditable());
        field.setText("8");
        assertFalse(field.commitEditorText());
        assertEquals(4.0, field.getValue());
        assertEquals("4", field.getText());

        field.increment();
        field.decrement();
        field.adjustValue(10.0);
        field.executeAccessibleAction(AccessibleAction.SET_VALUE, 8.0);
        assertEquals(4.0, field.getValue());

        source.set(8.0);
        assertEquals("8", field.getText());
        assertEquals(8.0, field.queryAccessibleAttribute(AccessibleAttribute.VALUE));

        source.set(Double.NaN);
        assertTrue(Double.isNaN(field.getValue()));
        assertEquals("8", field.getText());
        assertEquals(8.0, field.queryAccessibleAttribute(AccessibleAttribute.VALUE));
        assertEquals(field.getInvalidTextErrorText(), field.getValidationErrorText());

        source.set(6.0);
        assertEquals("6", field.getText());
        assertEquals("", field.getValidationErrorText());

        field.valueProperty().unbind();
        assertTrue(field.getEditor().isEditable());
        field.increment();
        assertEquals(8.0, field.getValue());
    }

    /// Verifies zero-count and empty-value stepping without committing unrelated raw text.
    @Test
    void definesZeroCountAndEmptyValueStepping() {
        M3NumberField field = decimalField(2.0, 10.0, 5.0, 3.0);
        field.setText("pending");

        field.increment(0);
        field.decrement(0);
        assertEquals(5.0, field.getValue());
        assertEquals("pending", field.getText());
        assertFalse(field.isValidationActive());

        field.setValue(null);
        field.increment();
        assertEquals(2.0, field.getValue());

        field.setValue(null);
        field.decrement();
        assertEquals(2.0, field.getValue());
    }

    /// Verifies that stepping normalizes exact programmatic values outside the configured value scale.
    @Test
    void stepsFromProgrammaticValuesOutsideTheValueScale() {
        M3NumberField field = decimalField(2.0, 11.0, 5.0, 3.0);
        field.setCommitBehavior(M3NumberFieldCommitBehavior.VALIDATE);

        field.setValue(100.0);
        field.decrement();
        assertEquals(11.0, field.getValue());

        field.setValue(6.0);
        field.increment();
        assertEquals(8.0, field.getValue());
    }

    /// Verifies localized grouping and decimal separators through a locale-specific formatter.
    @Test
    void parsesAndFormatsLocalizedNumbers() {
        M3NumberField field = new M3NumberField();
        NumberFormat german = NumberFormat.getNumberInstance(Locale.GERMANY);
        german.setMaximumFractionDigits(2);
        field.setFormatter(german);
        field.setStep(0.5);
        field.setText("1.234,5");

        assertTrue(field.commitEditorText());
        assertEquals(1234.5, field.getValue());
        assertEquals("1.234,5", field.getText());

        field.setValue(9876.25);
        assertEquals("9.876,25", field.getText());
    }

    /// Verifies buttons, keyboard actions, and accessibility actions share range-aware stepping.
    @Test
    void stepsAcrossProgrammaticKeyboardAndAccessibilityPaths() {
        M3NumberField field = decimalField(2.0, 11.0, 5.0, 3.0);

        field.increment();
        assertEquals(8.0, field.getValue());
        field.decrement();
        assertEquals(5.0, field.getValue());

        field.getEditor().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
        assertEquals(8.0, field.getValue());
        field.getEditor().fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(5.0, field.getValue());

        field.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(8.0, field.getValue());
        field.executeAccessibleAction(AccessibleAction.SET_VALUE, 10.0);
        assertEquals(11.0, field.getValue());
        field.executeAccessibleAction(AccessibleAction.BLOCK_DECREMENT);
        assertEquals(2.0, field.getValue());

        assertEquals(2.0, field.queryAccessibleAttribute(AccessibleAttribute.MIN_VALUE));
        assertEquals(11.0, field.queryAccessibleAttribute(AccessibleAttribute.MAX_VALUE));
        assertEquals(2.0, field.queryAccessibleAttribute(AccessibleAttribute.VALUE));
        assertSame(field.getEditor(), field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        @Nullable AccessibleAttribute valueString = M3Accessible.attribute("VALUE_STRING");
        if (valueString != null) {
            assertEquals("2", field.queryAccessibleAttribute(valueString));
        }

        field.setEditable(false);
        field.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(2.0, field.getValue());
    }

    /// Verifies the Material skin, prefix slot, step controls, hiding behavior, and replacement lifecycle.
    @Tier2Test
    @Test
    void rendersMaterialInputLayoutAndStepper() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = decimalField(0.0, 100.0, 24.0, 2.0);
            field.setLabelText("Width");
            field.setSupportingText("Pixels");
            field.setPrefix(new Label("W"));
            field.setPrefWidth(360.0);
            M3Theme theme = iconButtonMetricTheme(34.0, 36.0, 17.0);
            StackPane root = themedRoot(field, 420.0, 140.0, theme);
            layout(root, 420.0, 140.0);

            assertInstanceOf(M3NumberFieldSkin.class, field.getSkin());
            assertEquals(1, field.lookupAll(".m3-number-field-stepper").size());
            assertEquals(2, field.lookupAll(".m3-number-field-step-button").size());
            assertEquals(1, field.lookupAll(".m3-text-input-leading").size());
            assertTrue(field.getWidth() > 0.0);
            assertTrue(field.getHeight() > 0.0);

            M3TextInputLayout inputLayout = field.getInputLayout();
            javafx.scene.layout.HBox stepper = assertInstanceOf(
                    javafx.scene.layout.HBox.class,
                    inputLayout.getTrailing()
            );
            M3IconButton decrementButton = assertInstanceOf(M3IconButton.class, stepper.getChildren().get(0));
            M3IconButton incrementButton = assertInstanceOf(M3IconButton.class, stepper.getChildren().get(1));
            assertEquals(34.0, decrementButton.getContainerHeight(), 0.0001);
            assertEquals(36.0, decrementButton.getContainerWidth(), 0.0001);
            assertEquals(17.0, decrementButton.getContainerShape(), 0.0001);
            assertTrue(decrementButton.getWidth() >= 48.0);
            assertTrue(incrementButton.getWidth() >= 48.0);
            assertTrue(
                    decrementButton.getBoundsInParent().getMaxX()
                            <= incrementButton.getBoundsInParent().getMinX()
            );
            Bounds stepperBounds = inputLayout.sceneToLocal(stepper.localToScene(stepper.getBoundsInLocal()));
            assertTrue(stepperBounds.getMinX() >= 0.0, stepperBounds::toString);
            assertTrue(stepperBounds.getMaxX() <= inputLayout.getWidth(), stepperBounds::toString);
            assertTrue(field.getEditor().getPadding().getRight() >= stepperBounds.getWidth());

            field.setHideStepper(true);
            layout(root, 420.0, 140.0);
            assertTrue(field.lookupAll(".m3-number-field-stepper").isEmpty());

            field.setHideStepper(false);
            FxTestUtils.replaceSkin(field, control -> new M3NumberFieldSkin(control, control.getInputLayout()));
            layout(root, 420.0, 140.0);
            assertInstanceOf(M3NumberFieldSkin.class, field.getSkin());
            assertEquals(2, field.lookupAll(".m3-number-field-step-button").size());
        }));
    }

    /// Verifies that focused wheel input steps the value and honors the wheel-disable property.
    @Tier2Test
    @Test
    void focusedWheelInputCanBeDisabled() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = decimalField(0.0, 10.0, 5.0, 1.0);
            StackPane root = new StackPane(field);
            Scene scene = new Scene(root, 420.0, 140.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                field.getEditor().requestFocus();
                assertTrue(field.getEditor().isFocused());

                field.getEditor().fireEvent(scrollEvent(field.getEditor(), 40.0));
                assertEquals(6.0, field.getValue());

                field.setWheelDisabled(true);
                field.getEditor().fireEvent(scrollEvent(field.getEditor(), 40.0));
                assertEquals(6.0, field.getValue());
            } finally {
                stage.close();
            }
        });
    }

    /// Renders a real-window visual matrix covering filled, outlined, hidden-stepper, localized, and error states.
    @Tier3Test
    @Test
    void rendersNumberFieldVisualMatrix() {
        FxTestUtils.assertNoCssWarnings(() -> FxTestUtils.runOnFxThread(() -> {
            M3NumberField filled = decimalField(0.0, 100.0, 24.0, 2.0);
            filled.setLabelText("Width");
            filled.setSupportingText("Incremented in pairs");

            M3NumberField outlined = decimalField(-20.0, 40.0, 18.5, 0.5);
            outlined.setVariant(M3TextInputVariant.OUTLINED);
            outlined.setLabelText("Temperature");
            outlined.setPrefix(new Label("°C"));

            M3NumberField localized = new M3NumberField(0.375);
            localized.setFormatter(NumberFormat.getPercentInstance(Locale.US));
            localized.setStep(0.05);
            localized.setLabelText("Completion");
            localized.setHideStepper(true);

            M3NumberField invalid = decimalField(0.0, 10.0, 5.0, 2.0);
            invalid.setCommitBehavior(M3NumberFieldCommitBehavior.VALIDATE);
            invalid.setVariant(M3TextInputVariant.OUTLINED);
            invalid.setLabelText("Validated amount");
            invalid.setText("7");
            assertFalse(invalid.commitEditorText());

            for (M3NumberField field : new M3NumberField[]{filled, outlined, localized, invalid}) {
                field.setPrefWidth(360.0);
            }
            VBox root = new VBox(20.0, filled, outlined, localized, invalid);
            root.setStyle("-fx-padding: 24px; -fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 440.0, 430.0);
            Stage stage = new Stage();

            try {
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                for (M3NumberField field : new M3NumberField[]{filled, outlined, invalid}) {
                    for (Node node : field.lookupAll(".m3-number-field-step-button")) {
                        M3IconButton button = assertInstanceOf(M3IconButton.class, node);
                        assertTrue(button.getWidth() >= button.getContainerWidth());
                        assertTrue(button.getHeight() >= button.getContainerHeight());
                    }
                }

                WritableImage image = snapshotImageOnFxThread(root);
                assertSnapshotHasColorVariety(image, 6);
                writeVisualSnapshot(image, Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-number-field-matrix.png"
                ));
            } finally {
                stage.close();
            }
        }));
    }

    /// Creates a decimal number field with explicit scale values.
    ///
    /// @param min   the minimum
    /// @param max   the maximum
    /// @param value the initial value
    /// @param step  the step size
    /// @return the configured field
    private static M3NumberField decimalField(double min, double max, double value, double step) {
        M3NumberField field = new M3NumberField(min, max, value);
        field.setFormatter(NumberFormat.getNumberInstance(Locale.US));
        field.setStep(step);
        return field;
    }

    /// Creates a themed detached root containing one number field.
    ///
    /// @param field  the number field
    /// @param width  the root width
    /// @param height the root height
    /// @return the themed root
    private static StackPane themedRoot(M3NumberField field, double width, double height) {
        return themedRoot(field, width, height, M3Theme.defaultTheme());
    }

    /// Creates a themed detached root containing one number field.
    ///
    /// @param field  the number field
    /// @param width  the root width
    /// @param height the root height
    /// @param theme  the theme installed on the root
    /// @return the themed root
    private static StackPane themedRoot(M3NumberField field, double width, double height, M3Theme theme) {
        StackPane root = new StackPane(field);
        M3ThemeManager.install(root, theme);
        new Scene(root, width, height);
        return root;
    }

    /// Creates a theme whose small icon-button metrics differ from the baseline NumberField CSS values.
    ///
    /// @param height the small icon-button visual container height
    /// @param width  the small icon-button default visual container width
    /// @param shape  the small round icon-button resting shape
    /// @return the customized theme
    private static M3Theme iconButtonMetricTheme(double height, double width, double shape) {
        M3Theme baseTheme = M3Theme.defaultTheme();
        M3ComponentTokens baseComponents = baseTheme.tokens().componentTokens();
        M3ComponentTokens.IconButtonTokens baseIconButtons = baseComponents.iconButton();
        M3ComponentTokens.IconButtonSizeTokens baseSmall = baseIconButtons.small();
        M3ComponentTokens.IconButtonSizeTokens customSmall = new M3ComponentTokens.IconButtonSizeTokens(
                height,
                baseSmall.iconSize(),
                baseSmall.narrowWidth(),
                width,
                baseSmall.wideWidth(),
                shape,
                baseSmall.squareContainerShape(),
                baseSmall.pressedRoundContainerShape(),
                baseSmall.pressedSquareContainerShape(),
                baseSmall.selectedRoundContainerShape(),
                baseSmall.selectedSquareContainerShape(),
                baseSmall.outlineWidth()
        );
        M3ComponentTokens.IconButtonTokens customIconButtons = new M3ComponentTokens.IconButtonTokens(
                baseIconButtons.extraSmall(),
                customSmall,
                baseIconButtons.medium(),
                baseIconButtons.large(),
                baseIconButtons.extraLarge()
        );
        M3ComponentTokens customComponents = M3ComponentTokens.builder(baseComponents)
                .iconButton(customIconButtons)
                .build();
        M3TokenSet customTokens = M3TokenSet.builder(baseTheme.tokens())
                .componentTokens(customComponents)
                .build();
        return M3Theme.fromTokenSet(customTokens);
    }

    /// Applies CSS and lays out a root at a stable size.
    ///
    /// @param root   the root to lay out
    /// @param width  the root width
    /// @param height the root height
    private static void layout(StackPane root, double width, double height) {
        root.applyCss();
        root.resize(width, height);
        root.layout();
        root.applyCss();
        root.layout();
    }

    /// Creates an unmodified key event.
    ///
    /// @param eventType the event type
    /// @param code      the key code
    /// @return the key event
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }

    /// Creates a vertical wheel event targeted at one editor.
    ///
    /// @param target the event target
    /// @param deltaY the vertical wheel delta
    /// @return the scroll event
    private static ScrollEvent scrollEvent(javafx.scene.Node target, double deltaY) {
        return new ScrollEvent(
                target,
                target,
                ScrollEvent.SCROLL,
                40.0,
                40.0,
                40.0,
                40.0,
                false,
                false,
                false,
                false,
                false,
                false,
                0.0,
                deltaY,
                0.0,
                deltaY,
                ScrollEvent.HorizontalTextScrollUnits.NONE,
                0.0,
                ScrollEvent.VerticalTextScrollUnits.NONE,
                0.0,
                0,
                null
        );
    }
}
