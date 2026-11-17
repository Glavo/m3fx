// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3TextInputLayoutSkin;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlStyleAssertions.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies text input state, floating labels, editor geometry, and validation.
@NotNullByDefault
final class M3TextInputContractTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// The lowest acceptable rendered ink center ratio for outlined fields with a floating label.
    private static final double OUTLINED_FLOATING_INK_MINIMUM_CENTER_RATIO = 0.46;

    /// The highest acceptable rendered ink center ratio for outlined fields with a floating label.
    private static final double OUTLINED_FLOATING_INK_MAXIMUM_CENTER_RATIO = 0.70;

    /// The lowest acceptable rendered ink center ratio for filled fields with a floating label.
    private static final double FILLED_FLOATING_INK_MINIMUM_CENTER_RATIO = 0.48;

    /// The highest acceptable rendered ink center ratio for filled fields with a floating label.
    private static final double FILLED_FLOATING_INK_MAXIMUM_CENTER_RATIO = 0.72;

    /// The tolerance used when checking centered single-line text input ink.
    private static final double SINGLE_LINE_TEXT_INPUT_INK_CENTER_TOLERANCE = 3.0;

    /// The edge tolerance used when comparing rendered text area ink against JavaFX content regions.
    private static final double TEXT_AREA_CONTENT_INK_EDGE_TOLERANCE = 2.0;

    /// The overlap tolerance used when comparing rendered text ink against text input adornment slots.
    private static final double TEXT_INPUT_ADORNMENT_OVERLAP_TOLERANCE = 1.0;

    /// Verifies that text field component token properties are styleable from CSS.
    @Test
    void textFieldTokensAreStyleable() {
        M3TextField textField = createTextField("Content", M3TextInputVariant.OUTLINED);
        textField.setStyle("-m3-container-height: 64px; -m3-container-shape: 12px; -m3-horizontal-padding: 22px;");

        applyCss(textField);

        assertEquals("Content", textField.getText());
        assertEquals(M3TextInputVariant.OUTLINED, textField.getVariant());
        assertEquals(64.0, textField.getContainerHeight(), 0.0001);
        assertEquals(12.0, textField.getContainerShape(), 0.0001);
        assertEquals(22.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, textField.prefHeight(-1.0), 0.0001);
        assertEquals(22.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(22.0, textField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that password field component token properties are styleable from CSS.
    @Test
    void passwordFieldTokensAreStyleable() {
        M3PasswordField passwordField = createPasswordField("secret", M3TextInputVariant.OUTLINED);
        passwordField.setStyle("-m3-container-height: 60px; -m3-container-shape: 10px; -m3-horizontal-padding: 20px;");

        applyCss(passwordField);

        assertEquals("secret", passwordField.getText());
        assertEquals(M3TextInputVariant.OUTLINED, passwordField.getVariant());
        assertEquals(60.0, passwordField.getContainerHeight(), 0.0001);
        assertEquals(10.0, passwordField.getContainerShape(), 0.0001);
        assertEquals(20.0, passwordField.getHorizontalPadding(), 0.0001);
        assertEquals(60.0, passwordField.prefHeight(-1.0), 0.0001);
        assertEquals(20.0, passwordField.getPadding().getLeft(), 0.0001);
        assertEquals(20.0, passwordField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that text area component token properties are styleable from CSS.
    @Test
    void textAreaTokensAreStyleable() {
        M3TextArea textArea = createTextArea("Notes", M3TextInputVariant.OUTLINED);
        textArea.setStyle(
                "-m3-container-height: 140px; "
                        + "-m3-container-shape: 12px; "
                        + "-m3-horizontal-padding: 22px; "
                        + "-m3-vertical-padding: 18px;"
        );

        applyCss(textArea);

        assertEquals("Notes", textArea.getText());
        assertEquals(M3TextInputVariant.OUTLINED, textArea.getVariant());
        assertEquals(140.0, textArea.getContainerHeight(), 0.0001);
        assertEquals(12.0, textArea.getContainerShape(), 0.0001);
        assertEquals(22.0, textArea.getHorizontalPadding(), 0.0001);
        assertEquals(18.0, textArea.getVerticalPadding(), 0.0001);
        assertEquals(140.0, textArea.prefHeight(-1.0), 0.0001);
        assertEquals(22.0, textArea.getPadding().getLeft(), 0.0001);
        assertEquals(22.0, textArea.getPadding().getRight(), 0.0001);
        assertEquals(18.0, textArea.getPadding().getTop(), 0.0001);
        assertEquals(18.0, textArea.getPadding().getBottom(), 0.0001);
    }

    /// Verifies that text inputs expose an error pseudo-class state.
    @Test
    void textInputsExposeErrorState() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField();
        M3PasswordField passwordField = new M3PasswordField();
        M3TextArea textArea = new M3TextArea();

        assertInstanceOf(M3TextInput.class, textField);
        assertInstanceOf(M3TextInput.class, passwordField);
        assertInstanceOf(M3TextInput.class, textArea);
        assertFalse(textField.isError());
        assertFalse(passwordField.isError());
        assertFalse(textArea.isError());

        textField.setError(true);
        passwordField.setError(true);
        textArea.setError(true);

        assertTrue(textField.isError());
        assertTrue(passwordField.isError());
        assertTrue(textArea.isError());
        assertTrue(textField.getPseudoClassStates().contains(error));
        assertTrue(passwordField.getPseudoClassStates().contains(error));
        assertTrue(textArea.getPseudoClassStates().contains(error));

        textField.errorProperty().set(false);
        passwordField.errorProperty().set(false);
        textArea.errorProperty().set(false);

        assertFalse(textField.getPseudoClassStates().contains(error));
        assertFalse(passwordField.getPseudoClassStates().contains(error));
        assertFalse(textArea.getPseudoClassStates().contains(error));
    }

    /// Verifies that the shared text input API exposes common metric tokens.
    @Test
    void textInputInterfaceExposesSharedMetricTokens() {
        M3TextInput[] inputs = {
                new M3TextField(),
                new M3PasswordField(),
                new M3TextArea()
        };

        for (M3TextInput input : inputs) {
            input.setContainerHeight(72.0);
            input.setContainerShape(14.0);
            input.setHorizontalPadding(24.0);
            input.setVerticalPadding(10.0);

            TextInputControl control = assertInstanceOf(TextInputControl.class, input);
            assertEquals(72.0, input.getContainerHeight(), 0.0001);
            assertEquals(14.0, input.getContainerShape(), 0.0001);
            assertEquals(24.0, input.getHorizontalPadding(), 0.0001);
            assertEquals(10.0, input.getVerticalPadding(), 0.0001);
            assertEquals(72.0, control.prefHeight(-1.0), 0.0001);
            assertEquals(24.0, control.getPadding().getLeft(), 0.0001);
            assertEquals(24.0, control.getPadding().getRight(), 0.0001);
            assertEquals(10.0, control.getPadding().getTop(), 0.0001);
            assertEquals(10.0, control.getPadding().getBottom(), 0.0001);
            assertThrows(IllegalArgumentException.class, () -> input.setContainerHeight(-1.0));
            assertThrows(IllegalArgumentException.class, () -> input.setContainerShape(-1.0));
            assertThrows(IllegalArgumentException.class, () -> input.setHorizontalPadding(-1.0));
            assertThrows(IllegalArgumentException.class, () -> input.setVerticalPadding(-1.0));
        }
    }

    /// Verifies that text input metric tokens preserve application-bound JavaFX layout properties.
    @Test
    void textInputMetricTokensPreserveBoundLayoutProperties() {
        M3TextInput[] inputs = {
                new M3TextField(),
                new M3PasswordField(),
                new M3TextArea()
        };

        for (M3TextInput input : inputs) {
            TextInputControl control = assertInstanceOf(TextInputControl.class, input);
            SimpleDoubleProperty applicationMinHeight = new SimpleDoubleProperty(31.0);
            SimpleDoubleProperty applicationPrefHeight = new SimpleDoubleProperty(41.0);
            SimpleObjectProperty<Insets> applicationPadding =
                    new SimpleObjectProperty<>(new Insets(1.0, 2.0, 3.0, 4.0));
            control.minHeightProperty().bind(applicationMinHeight);
            control.prefHeightProperty().bind(applicationPrefHeight);
            control.paddingProperty().bind(applicationPadding);

            input.setContainerHeight(72.0);
            input.setHorizontalPadding(24.0);
            input.setVerticalPadding(10.0);

            assertEquals(72.0, input.getContainerHeight(), 0.0001);
            assertEquals(24.0, input.getHorizontalPadding(), 0.0001);
            assertEquals(10.0, input.getVerticalPadding(), 0.0001);
            assertEquals(31.0, control.getMinHeight(), 0.0001);
            assertEquals(41.0, control.getPrefHeight(), 0.0001);
            assertEquals(new Insets(1.0, 2.0, 3.0, 4.0), control.getPadding());

            applicationMinHeight.set(35.0);
            applicationPrefHeight.set(45.0);
            applicationPadding.set(new Insets(5.0, 6.0, 7.0, 8.0));

            assertEquals(35.0, control.getMinHeight(), 0.0001);
            assertEquals(45.0, control.getPrefHeight(), 0.0001);
            assertEquals(new Insets(5.0, 6.0, 7.0, 8.0), control.getPadding());

            control.minHeightProperty().unbind();
            control.prefHeightProperty().unbind();
            control.paddingProperty().unbind();
            input.setContainerHeight(80.0);
            input.setHorizontalPadding(30.0);
            input.setVerticalPadding(12.0);

            assertEquals(35.0, control.minHeight(-1.0), 0.0001);
            assertEquals(45.0, control.prefHeight(-1.0), 0.0001);
            assertEquals(new Insets(12.0, 30.0, 12.0, 30.0), control.getPadding());
        }
    }

    /// Verifies that text input controls expose their shared Material metric tokens through CSS metadata.
    @Test
    void textInputCssMetaDataExposeSharedMetricTokens() {
        assertTextInputCssMetaData(M3TextField.getClassCssMetaData());
        assertTextInputCssMetaData(M3PasswordField.getClassCssMetaData());
        assertTextInputCssMetaData(M3TextArea.getClassCssMetaData());
    }

    /// Verifies that text input layouts expose supporting text, counters, and wrapped input state.
    @Test
    void textInputLayoutDisplaysSupportingTextAndCounter() {
        M3TextField textField = new M3TextField("abc");
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setSupportingText("Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(5);

        applyCss(layout);

        assertInstanceOf(M3TextInputLayoutSkin.class, layout.getSkin());
        assertEquals(textField, layout.getInput());
        assertEquals(textField, layout.getTextInput());
        assertEquals(3, layout.getCharacterCount());
        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertTrue(textField.getStyleClass().contains("m3-text-input-layout-input"));

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + "m3-text-input-supporting-text")
        );
        Label counter = assertInstanceOf(Label.class, layout.lookup("." + "m3-text-input-counter"));
        assertEquals("Helper text", supportingText.getText());
        assertEquals("3 / 5", counter.getText());
        assertTrue(supportingText.isManaged());
        assertTrue(counter.isManaged());

        textField.setDisable(true);

        assertTrue(assertInstanceOf(
                Node.class,
                layout.lookup("." + "m3-text-input-supporting-row")
        ).isDisable());
    }

    /// Verifies that text input layouts expose labels and update floating label state.
    @Test
    void textInputLayoutDisplaysFloatingLabel() {
        PseudoClass floating = PseudoClass.getPseudoClass("floating");
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setLabelText("Email");
        layout.setSupportingText("Helper text");
        AtomicInteger floatingChanges = new AtomicInteger();
        layout.labelFloatingProperty().addListener(observable -> floatingChanges.incrementAndGet());

        applyCss(layout);

        Text label = assertInstanceOf(Text.class, layout.lookup("." + "m3-text-input-label"));
        assertEquals("Email", layout.getLabelText());
        assertEquals("Email", label.getText());
        assertEquals("Email Helper text", layout.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertFalse(layout.isLabelFloating());
        assertFalse(label.getPseudoClassStates().contains(floating));
        assertEquals(20.0, textField.getPadding().getTop(), 0.0001);
        assertTrue(layout.getStyleClass().contains(M3TextInputVariant.FILLED.styleClass()));

        textField.setText("support@example.com");

        assertTrue(layout.isLabelFloating());
        assertTrue(label.getPseudoClassStates().contains(floating));
        assertEquals(20.0, textField.getPadding().getTop(), 0.0001);

        textField.setVariant(M3TextInputVariant.OUTLINED);

        assertTrue(layout.getStyleClass().contains(M3TextInputVariant.OUTLINED.styleClass()));

        textField.clear();

        assertFalse(layout.isLabelFloating());
        assertEquals(11.0, textField.getPadding().getTop(), 0.0001);
        assertEquals(2, floatingChanges.get());
    }

    /// Verifies that filled text input labels and text keep stable vertical placement in a shown window.
    @Test
    void filledTextInputLayoutKeepsFloatingLabelAndTextAlignedInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("support@example.com", M3TextInputVariant.FILLED);
            textField.setPrefWidth(340.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Email address");
            layout.setLabelText("Filled with text");
            layout.setLeading(new M3Icon("E"));
            layout.setClearButtonEnabled(true);
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(32);
            layout.setPrefWidth(340.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 420.0, 130.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.resize(420.0, 130.0);
                root.layout();

                Region inputContainer = lookupRegion(layout, "." + "m3-text-input-container");
                Text label = assertInstanceOf(
                        Text.class,
                        layout.lookup("." + "m3-text-input-label")
                );
                Text inputText = renderedTextNode(textField, "support@example.com");

                Bounds containerBounds = inputContainer.localToScene(inputContainer.getLayoutBounds());
                Bounds labelBounds = label.localToScene(label.getLayoutBounds());
                Bounds textBounds = inputText.localToScene(inputText.getLayoutBounds());

                assertEquals(containerBounds.getMinY() + 4.0, labelBounds.getMinY(), 1.0);
                assertTrue(labelBounds.getMaxY() + 0.5 <= textBounds.getMinY(),
                        () -> "labelBounds=" + labelBounds + ", textBounds=" + textBounds);
                assertTrue(textBounds.getMaxY() <= containerBounds.getMaxY() - 6.0,
                        () -> "textBounds=" + textBounds + ", containerBounds=" + containerBounds);

                WritableImage image = snapshotImageOnFxThread(root);
                assertSnapshotNodeContainsContrast(image, label, Color.WHITE, 0.04);
                assertSnapshotNodeContainsContrast(image, inputText, Color.WHITE, 0.04);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-text-field-filled-label-alignment.png"
                ));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that a focused outlined text input layout keeps text visually centered in a shown window.
    @Test
    void focusedOutlinedTextInputLayoutKeepsTextCenteredInWindowAfterEdits() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField);

            layout.setSupportingText("Project name");
            layout.setLabelText("Outlined with text");
            layout.setLeading(new M3Icon("T"));
            layout.setClearButtonEnabled(true);
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 150.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                root.applyCss();
                root.resize(430.0, 150.0);
                root.layout();

                textField.requestFocus();
                assertTrue(textField.isFocused());
                textField.setText("M3FX");
                textField.positionCaret(textField.getText().length());

                root.applyCss();
                root.layout();
                layout.layout();

                Region inputContainer = lookupRegion(layout, "." + "m3-text-input-container");
                Text label = assertInstanceOf(
                        Text.class,
                        layout.lookup("." + "m3-text-input-label")
                );
                Text inputText = renderedTextNode(textField, "M3FX");
                Path outline = assertInstanceOf(Path.class, layout.lookup("." + "m3-text-input-outline"));

                Bounds fieldBounds = textField.localToScene(textField.getBoundsInLocal());
                Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
                Bounds inputTextBounds = inputText.localToScene(inputText.getBoundsInLocal());
                Bounds containerBounds = inputContainer.localToScene(inputContainer.getBoundsInLocal());

                assertTrue(layout.isLabelFloating());
                assertTrue(outlineNotchGap(outline) >= labelBounds.getWidth() - 1.0,
                        () -> "outline gap is narrower than the floating label: gap="
                                + outlineNotchGap(outline) + ", label=" + labelBounds);
                assertTrue(labelBounds.getMaxY() < inputTextBounds.getMinY(),
                        () -> "labelBounds=" + labelBounds + ", inputTextBounds=" + inputTextBounds);
                assertTrue(inputTextBounds.getMaxY() <= containerBounds.getMaxY() - 6.0,
                        () -> "inputTextBounds=" + inputTextBounds + ", containerBounds=" + containerBounds);
                assertSingleLineTextInputsKeepInkAligned(root);

                WritableImage image = snapshotImageOnFxThread(root);
                Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, inputText);
                double fieldCenterY = (fieldBounds.getMinY() + fieldBounds.getMaxY()) / 2.0;
                double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
                assertEquals(fieldCenterY, inkCenterY, 4.0,
                        () -> "outlined input rendered ink is vertically misaligned: field="
                                + fieldBounds + ", ink=" + inkBounds);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-text-field-focused-outlined-alignment.png"
                ));

                textInputClearButton(layout).fire();

                assertEquals("", textField.getText());
                assertTrue(textField.isFocused());
                assertTrue(layout.isLabelFloating());
                assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                assertEquals(textField, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
                assertEquals(16.0, textField.getPadding().getRight(), 0.0001);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that focused outlined text areas do not expose the JavaFX inner content background.
    @Test
    void focusedOutlinedTextAreaKeepsTransparentContentBackgroundInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextArea textArea = createTextArea(
                    "Material text areas share field colors\nbut keep multi-line height tokens.",
                    M3TextInputVariant.OUTLINED
            );
            textArea.setPrefSize(500.0, 112.0);

            M3TextInputLayout layout = new M3TextInputLayout(textArea);

            layout.setSupportingText("Outlined multi-line input");
            layout.setLabelText("Outlined text area");
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(96);
            layout.setPrefWidth(540.0);

            Color surface = Color.rgb(248, 240, 249);
            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 610.0, 190.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                textArea.requestFocus();
                root.applyCss();
                root.resize(610.0, 190.0);
                root.layout();
                layout.layout();

                assertTrue(textArea.isFocused());
                WritableImage image = snapshotImageOnFxThread(root);
                Region content = lookupRegion(textArea, ".content");

                assertTextAreaContentBackgroundMatches(
                        image,
                        content,
                        surface,
                        0.035,
                        "focused outlined text area content background"
                );
                assertTextAreaTextInkInsideContentArea(
                        image,
                        textArea,
                        content,
                        "focused outlined text area"
                );
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-text-area-focused-outlined-background.png"
                ));
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that multiline RTL inputs do not receive the single-line text field edge correction.
    @Test
    void rightToLeftOutlinedTextAreaKeepsApplicationTranslationInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextArea textArea = createTextArea("M3FX RTL\nSecond line", M3TextInputVariant.OUTLINED);
            textArea.setTranslateX(6.0);
            textArea.setPrefSize(420.0, 128.0);

            M3TextInputLayout layout = new M3TextInputLayout(textArea);

            layout.setSupportingText("RTL multiline supporting text");
            layout.setLabelText("RTL outlined area");
            layout.setLeading(visualIcon("text"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(64);
            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout.setPrefWidth(460.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 540.0, 230.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                textArea.requestFocus();
                root.applyCss();
                root.resize(540.0, 230.0);
                root.layout();
                layout.layout();

                assertTrue(textArea.isFocused());
                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(6.0, textArea.getTranslateX(), 0.0001);
                assertEquals(16.0, textArea.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textArea.getPadding().getRight(), 0.0001);
                assertEquals(16.0, textArea.getPadding().getTop(), 0.0001);
                assertEquals(16.0, textArea.getPadding().getBottom(), 0.0001);
                WritableImage image = snapshotImageOnFxThread(root);
                Region content = lookupRegion(textArea, ".content");
                assertTextAreaTextInkInsideContentArea(image, textArea, content, "RTL outlined text area");

                textArea.setPadding(new Insets(12.0, 70.0, 18.0, 22.0));
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(6.0, textArea.getTranslateX(), 0.0001);
                assertEquals(22.0, textArea.getPadding().getLeft(), 0.0001);
                assertEquals(70.0, textArea.getPadding().getRight(), 0.0001);
                assertEquals(12.0, textArea.getPadding().getTop(), 0.0001);
                assertEquals(18.0, textArea.getPadding().getBottom(), 0.0001);
                image = snapshotImageOnFxThread(root);
                assertTextAreaTextInkInsideContentArea(image, textArea, content, "custom padded RTL outlined text area");

                layout.setInput(null);
                root.applyCss();
                root.layout();
                layout.layout();

                assertEquals(6.0, textArea.getTranslateX(), 0.0001);
                assertEquals(22.0, textArea.getPadding().getLeft(), 0.0001);
                assertEquals(70.0, textArea.getPadding().getRight(), 0.0001);
                assertEquals(12.0, textArea.getPadding().getTop(), 0.0001);
                assertEquals(18.0, textArea.getPadding().getBottom(), 0.0001);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that RTL outlined inputs keep floating labels, rendered text ink, and actions in separate slots.
    @Test
    void rightToLeftOutlinedTextInputSeparatesLabelTextAndTrailingActionInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX RTL", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField);

            layout.setSupportingText("RTL project name");
            layout.setLabelText("RTL outlined");
            layout.setLeading(visualIcon("text"));
            layout.setClearButtonEnabled(true);
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 150.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                textField.requestFocus();
                textField.positionCaret(textField.getText().length());
                root.applyCss();
                root.resize(430.0, 150.0);
                root.layout();
                layout.layout();

                assertTrue(textField.isFocused());
                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);
                assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);

                Text label = assertInstanceOf(
                        Text.class,
                        layout.lookup("." + "m3-text-input-label")
                );
                Text inputText = renderedTextNode(textField, "M3FX RTL");
                StackPane leadingSlot = assertInstanceOf(
                        StackPane.class,
                        layout.lookup("." + "m3-text-input-leading")
                );
                StackPane trailingSlot = assertInstanceOf(
                        StackPane.class,
                        layout.lookup("." + "m3-text-input-trailing")
                );
                Node leadingContent = leadingSlot.getChildren().get(0);
                Node trailingContent = trailingSlot.getChildren().get(0);

                Node leadingGraphic = assertInstanceOf(Parent.class, leadingContent).getChildrenUnmodifiable().get(0);
                Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
                Bounds textBounds = inputText.localToScene(inputText.getBoundsInLocal());
                Bounds leadingBounds = leadingGraphic.localToScene(leadingGraphic.getBoundsInLocal());
                Bounds trailingBounds = trailingContent.localToScene(trailingContent.getBoundsInLocal());
                WritableImage image = snapshotImageOnFxThread(root);
                Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, inputText);

                assertTrue(inkBounds.getMinX() >= trailingBounds.getMaxX() + 3.0,
                        () -> "RTL input rendered ink overlaps trailing action: ink="
                                + inkBounds + ", trailing=" + trailingBounds);
                assertTrue(inkBounds.getMaxX() <= leadingBounds.getMinX() - 3.0,
                        () -> "RTL input rendered ink overlaps leading adornment: ink="
                                + inkBounds + ", leading=" + leadingBounds);
                assertTrue(labelBounds.getMinX() >= trailingBounds.getMaxX() + 1.0,
                        () -> "RTL floating label overlaps trailing action: label="
                                + labelBounds + ", trailing=" + trailingBounds);
                assertTrue(labelBounds.getMaxX() <= leadingBounds.getMinX() - 1.0,
                        () -> "RTL floating label overlaps leading adornment: label="
                                + labelBounds + ", leading=" + leadingBounds);
                assertTrue(labelBounds.getMaxY() < textBounds.getMinY(),
                        () -> "RTL floating label overlaps input text vertically: label="
                                + labelBounds + ", text=" + textBounds);

                assertSnapshotNodeContainsContrast(image, inputText, Color.WHITE, 0.04);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-text-field-rtl-outlined-action-separation.png"
                ));
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that asymmetric RTL input adornments reserve the physical edge that owns the leading slot.
    @Test
    void rightToLeftOutlinedTextInputReservesLeadingOnlyAdornmentSpaceInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX RTL", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField);

            layout.setSupportingText("RTL project name");
            layout.setLabelText("RTL outlined");
            layout.setLeading(visualIcon("text"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 150.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                textField.requestFocus();
                root.applyCss();
                root.resize(430.0, 150.0);
                root.layout();
                layout.layout();

                assertTrue(textField.isFocused());
                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);

                Text label = assertInstanceOf(
                        Text.class,
                        layout.lookup("." + "m3-text-input-label")
                );
                Text inputText = renderedTextNode(textField, "M3FX RTL");
                StackPane leadingSlot = assertInstanceOf(
                        StackPane.class,
                        layout.lookup("." + "m3-text-input-leading")
                );
                Node leadingContent = leadingSlot.getChildren().get(0);
                Node leadingGraphic = assertInstanceOf(Parent.class, leadingContent).getChildrenUnmodifiable().get(0);
                Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
                Bounds leadingBounds = leadingGraphic.localToScene(leadingGraphic.getBoundsInLocal());
                WritableImage image = snapshotImageOnFxThread(root);
                Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, inputText);

                assertTrue(inkBounds.getMaxX() <= leadingBounds.getMinX() - 3.0,
                        () -> "RTL leading-only input ink overlaps the leading adornment: ink="
                                + inkBounds + ", leading=" + leadingBounds
                                + ", padding=" + textField.getPadding()
                                + ", translateX=" + textField.getTranslateX()
                                + ", orientation=" + textField.getEffectiveNodeOrientation());
                assertTrue(labelBounds.getMaxX() <= leadingBounds.getMinX() - 1.0,
                        () -> "RTL leading-only floating label overlaps the leading adornment: label="
                                + labelBounds + ", leading=" + leadingBounds
                                + ", padding=" + textField.getPadding()
                                + ", translateX=" + textField.getTranslateX()
                                + ", orientation=" + textField.getEffectiveNodeOrientation());
                assertSnapshotNodeContainsContrast(image, inputText, Color.WHITE, 0.04);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-text-field-rtl-leading-only-separation.png"
                ));
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that bound application translation is not overwritten by layout-owned RTL correction.
    @Test
    void rightToLeftOutlinedTextInputPreservesBoundApplicationTranslationInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX RTL", M3TextInputVariant.OUTLINED);
            SimpleDoubleProperty applicationTranslate = new SimpleDoubleProperty(9.0);
            textField.translateXProperty().bind(applicationTranslate);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField);

            layout.setSupportingText("RTL project name");
            layout.setLabelText("RTL outlined");
            layout.setLeading(visualIcon("text"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 150.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                textField.requestFocus();
                root.applyCss();
                root.resize(430.0, 150.0);
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(9.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                applicationTranslate.set(13.0);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(13.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                layout.setInput(null);
                root.applyCss();
                root.layout();
                layout.layout();

                assertEquals(13.0, textField.getTranslateX(), 0.0001);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                textField.translateXProperty().unbind();
                stage.close();
            }
        });
    }

    /// Verifies that bound application padding is not overwritten by layout-owned adornment padding.
    @Test
    void rightToLeftOutlinedTextInputPreservesBoundApplicationPaddingInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX RTL", M3TextInputVariant.OUTLINED);
            SimpleObjectProperty<Insets> applicationPadding = new SimpleObjectProperty<>(
                    new Insets(10.0, 64.0, 14.0, 24.0)
            );
            textField.paddingProperty().bind(applicationPadding);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField);

            layout.setSupportingText("RTL project name");
            layout.setLabelText("RTL outlined");
            layout.setLeading(visualIcon("text"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 150.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                textField.requestFocus();
                root.applyCss();
                root.resize(430.0, 150.0);
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(40.0, textField.getTranslateX(), 0.0001);
                assertEquals(24.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(64.0, textField.getPadding().getRight(), 0.0001);
                assertEquals(10.0, textField.getPadding().getTop(), 0.0001);
                assertEquals(14.0, textField.getPadding().getBottom(), 0.0001);

                applicationPadding.set(new Insets(8.0, 80.0, 16.0, 32.0));
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(48.0, textField.getTranslateX(), 0.0001);
                assertEquals(32.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(80.0, textField.getPadding().getRight(), 0.0001);
                assertEquals(8.0, textField.getPadding().getTop(), 0.0001);
                assertEquals(16.0, textField.getPadding().getBottom(), 0.0001);

                layout.setInput(null);
                root.applyCss();
                root.layout();
                layout.layout();

                assertEquals(0.0, textField.getTranslateX(), 0.0001);
                assertEquals(32.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(80.0, textField.getPadding().getRight(), 0.0001);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                textField.paddingProperty().unbind();
                stage.close();
            }
        });
    }

    /// Verifies that asymmetric RTL input edge correction is applied and cleared during runtime direction changes.
    @Test
    void rightToLeftOutlinedTextInputLeadingOnlyOffsetTracksRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX RTL", M3TextInputVariant.OUTLINED);
            textField.setTranslateX(4.0);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField);

            layout.setSupportingText("RTL project name");
            layout.setLabelText("RTL outlined");
            layout.setLeading(visualIcon("text"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 150.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                textField.requestFocus();
                root.applyCss();
                root.resize(430.0, 150.0);
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, false);
                assertEquals(4.0, textField.getTranslateX(), 0.0001);
                assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(16.0, textField.getPadding().getRight(), 0.0001);

                layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(36.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                Text inputText = renderedTextNode(textField, "M3FX RTL");
                StackPane leadingSlot = assertInstanceOf(
                        StackPane.class,
                        layout.lookup("." + "m3-text-input-leading")
                );
                Node leadingContent = leadingSlot.getChildren().get(0);
                Node leadingGraphic = assertInstanceOf(Parent.class, leadingContent).getChildrenUnmodifiable().get(0);
                Bounds leadingBounds = leadingGraphic.localToScene(leadingGraphic.getBoundsInLocal());
                WritableImage image = snapshotImageOnFxThread(root);
                Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, inputText);
                assertTrue(inkBounds.getMaxX() <= leadingBounds.getMinX() - 3.0,
                        "runtime RTL leading-only input ink overlaps the leading adornment: ink="
                                + inkBounds + ", leading=" + leadingBounds
                                + ", padding=" + textField.getPadding()
                                + ", translateX=" + textField.getTranslateX());

                textField.setTranslateX(12.0);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(44.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                layout.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, false);
                assertEquals(12.0, textField.getTranslateX(), 0.0001);
                assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(16.0, textField.getPadding().getRight(), 0.0001);

                layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(44.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                layout.setLeading(null);
                root.applyCss();
                root.layout();
                layout.layout();

                assertEquals(12.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(16.0, textField.getPadding().getRight(), 0.0001);

                layout.setLeading(visualIcon("text"));
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(44.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                M3IconButton trailingButton = createIconButton("V");
                layout.setTrailing(trailingButton);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(12.0, textField.getTranslateX(), 0.0001);
                assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                layout.setTrailing(null);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(44.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                textField.setVariant(M3TextInputVariant.FILLED);
                root.applyCss();
                root.layout();
                layout.layout();

                assertEquals(12.0, textField.getTranslateX(), 0.0001);
                assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                textField.setVariant(M3TextInputVariant.OUTLINED);
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(44.0, textField.getTranslateX(), 0.0001);
                assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

                textField.setPadding(new Insets(10.0, 64.0, 14.0, 24.0));
                root.applyCss();
                root.layout();
                layout.layout();

                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(52.0, textField.getTranslateX(), 0.0001);
                assertEquals(24.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(64.0, textField.getPadding().getRight(), 0.0001);
                assertEquals(11.0, textField.getPadding().getTop(), 0.0001);
                assertEquals(14.0, textField.getPadding().getBottom(), 0.0001);
                Bounds leadingSlotBounds = leadingSlot.localToScene(leadingSlot.getBoundsInLocal());
                image = snapshotImageOnFxThread(root);
                inkBounds = requiredRenderedTextInkBounds(image, inputText);
                assertTrue(inkBounds.getMaxX() <= leadingSlotBounds.getMinX() - 3.0,
                        "custom RTL padding input ink overlaps the leading slot: ink="
                                + inkBounds + ", leadingSlot=" + leadingSlotBounds
                                + ", padding=" + textField.getPadding()
                                + ", translateX=" + textField.getTranslateX());

                M3TextField replacement = createTextField("Replacement", M3TextInputVariant.OUTLINED);
                replacement.setTranslateX(7.0);
                replacement.setPrefWidth(360.0);
                layout.setInput(replacement);
                root.applyCss();
                root.layout();
                layout.layout();

                assertEquals(12.0, textField.getTranslateX(), 0.0001);
                assertEquals(24.0, textField.getPadding().getLeft(), 0.0001);
                assertEquals(64.0, textField.getPadding().getRight(), 0.0001);
                assertEquals(10.0, textField.getPadding().getTop(), 0.0001);
                assertEquals(14.0, textField.getPadding().getBottom(), 0.0001);
                assertEquals(39.0, replacement.getTranslateX(), 0.0001);
                assertEquals(16.0, replacement.getPadding().getLeft(), 0.0001);
                assertEquals(48.0, replacement.getPadding().getRight(), 0.0001);

                layout.setInput(null);
                root.applyCss();
                root.layout();
                layout.layout();

                assertEquals(7.0, replacement.getTranslateX(), 0.0001);
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that text input layout presentation changes are observable in real JavaFX pulses.
    @Tier2Test
    @Test
    void textInputLayoutAnimatesLabelClearButtonAndSupportingRow() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3TextInputLayout> layoutReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> textInputLayoutFloatingPresentationIsAnimating(layoutReference),
                    () -> showObservableTextInputLayout(stageReference, rootReference, layoutReference, false),
                    () -> {
                        M3TextInputLayout layout = Objects.requireNonNull(layoutReference.get(), "layout");

                        assertTextInputLayoutFloatingPresentationIntermediate(layout);
                        Path outline = assertInstanceOf(
                                Path.class,
                                layout.lookup("." + "m3-text-input-outline")
                        );
                        double notchGap = outlineNotchGap(outline);
                        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");

                        input.setText("alpha2");
                        layout.layout();

                        assertEquals(notchGap, outlineNotchGap(outline), 0.0001);
                        layout.setSupportingText("Helper text");
                    }
            );
            FxTestUtils.runOnFxThreadWhen(
                    () -> textInputLayoutSupportingRowPresentationIsAnimating(layoutReference),
                    () -> {
                    },
                    () -> assertTextInputLayoutSupportingRowPresentationIntermediate(
                            Objects.requireNonNull(layoutReference.get(), "layout")
                    )
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Pane root = rootReference.get();
                if (root != null) {
                    FxTestUtils.setCssAnimationsEnabled(root, true);
                    FxTestUtils.clearMotionScheme(root);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that runtime motion changes settle active text input presentation animations.
    @Tier2Test
    @Test
    void textInputLayoutSettlesRunningPresentationAnimationsWhenMotionIsDisabled() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3TextInputLayout> layoutReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> textInputLayoutFullPresentationIsAnimating(layoutReference),
                    () -> showObservableTextInputLayout(stageReference, rootReference, layoutReference, true),
                    () -> {
                        Pane root = Objects.requireNonNull(rootReference.get(), "root");
                        M3TextInputLayout layout = Objects.requireNonNull(layoutReference.get(), "layout");

                        assertTextInputLayoutFloatingPresentationIntermediate(layout);
                        assertTextInputLayoutSupportingRowPresentationIntermediate(layout);

                        FxTestUtils.setCssAnimationsEnabled(root, false);
                        root.applyCss();
                        root.layout();
                        layout.layout();

                        assertTextInputLayoutPresentationSettled(layout);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Pane root = rootReference.get();
                if (root != null) {
                    FxTestUtils.setCssAnimationsEnabled(root, true);
                    FxTestUtils.clearMotionScheme(root);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that text input layouts manage leading and trailing adornment slots.
    @Test
    void textInputLayoutManagesLeadingAndTrailingAdornments() {
        M3TextField textField = new M3TextField("abc");
        M3Icon leading = new M3Icon("S");
        M3IconButton trailing = new M3IconButton(new M3Icon("C"));
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setSupportingText("Helper text");
        layout.setLeading(leading);
        layout.setTrailing(trailing);

        applyCss(layout);

        assertEquals(leading, layout.getLeading());
        assertEquals(trailing, layout.getTrailing());
        assertEquals(3, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(leading, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(textField, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(trailing, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
        assertEquals(leading, assertInstanceOf(
                StackPane.class,
                layout.lookup("." + "m3-text-input-leading")
        ).getChildren().get(0));
        assertEquals(trailing, assertInstanceOf(
                StackPane.class,
                layout.lookup("." + "m3-text-input-trailing")
        ).getChildren().get(0));
        assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

        layout.setTrailing(null);

        assertNull(layout.getTrailing());
        assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(48.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, textField.getPadding().getRight(), 0.0001);

        layout.setLeading(null);

        assertNull(layout.getLeading());
        assertEquals(1, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(16.0, textField.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, textField.getPadding().getRight(), 0.0001);

        M3TextField widePaddingField = new M3TextField("wide");
        widePaddingField.setStyle("-m3-horizontal-padding: 64px;");
        M3TextInputLayout widePaddingLayout = new M3TextInputLayout(widePaddingField);
        widePaddingLayout.setLeading(new M3Icon("W"));

        applyCss(widePaddingLayout);

        assertEquals(64.0, widePaddingField.getPadding().getLeft(), 0.0001);
        assertEquals(64.0, widePaddingField.getPadding().getRight(), 0.0001);
    }

    /// Verifies that text input layouts route accessibility focus through input and adornment targets.
    @Test
    void textInputLayoutRoutesAccessibleFocusAcrossInputAndAdornments() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField("abc");
            M3Button leading = new M3Button("Lead");
            M3IconButton trailing = createIconButton("T");
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Helper text");
            layout.setLeading(leading);
            layout.setTrailing(trailing);

            Pane root = new Pane(layout);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 160.0));
                stage.show();
                root.applyCss();
                root.layout();

                assertSame(textField, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                leading.requestFocus();
                assertTrue(leading.isFocused());
                assertSame(leading, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                layout.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(leading.isFocused());

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(leading.isFocused());
                assertSame(leading, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, trailing);
                assertTrue(trailing.isFocused());
                assertSame(trailing, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                layout.setTrailing(null);
                assertTrue(textField.isFocused());
                assertSame(textField, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                layout.setClearButtonEnabled(true);
                M3IconButton clearButton = textInputClearButton(layout);
                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, clearButton);
                assertTrue(clearButton.isFocused());
                assertSame(clearButton, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                layout.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(clearButton.isFocused());

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM);
                assertTrue(clearButton.isFocused());
                assertSame(clearButton, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                clearButton.fire();

                assertEquals("", textField.getText());
                assertTrue(textField.isFocused());
                assertSame(textField, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 0);
                assertTrue(leading.isFocused());

                layout.setLeading(null);
                assertTrue(textField.isFocused());
                assertSame(textField, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that text input layouts traverse slots without stealing editor navigation keys.
    @Test
    void textInputLayoutKeyboardTraversalFollowsDirectionWithoutStealingEditorKeys() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField("abc");
            M3Button leading = new M3Button("Lead");
            M3IconButton trailing = createIconButton("T");
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Helper text");
            layout.setLeading(leading);
            layout.setTrailing(trailing);

            Pane root = new Pane(layout);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 180.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                layout.resizeRelocate(24.0, 24.0, 440.0, 96.0);
                root.layout();

                leading.requestFocus();
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(textField.isFocused());

                textField.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(textField.isFocused());

                trailing.requestFocus();
                trailing.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(textField.isFocused());

                trailing.requestFocus();
                trailing.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(leading.isFocused());

                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(trailing.isFocused());

                trailing.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.HOME));

                assertTrue(leading.isFocused());

                trailing.setDisable(true);
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(textField.isFocused());
                assertFalse(trailing.isFocused());

                trailing.setDisable(false);
                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                leading.requestFocus();
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(trailing.isFocused());

                trailing.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(leading.isFocused());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that built-in clear buttons participate in text input layout slot traversal.
    @Test
    void textInputLayoutKeyboardTraversalIncludesClearButtonSlot() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField("abc");
            M3Button leading = new M3Button("Lead");
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Helper text");
            layout.setLeading(leading);
            layout.setClearButtonEnabled(true);

            Pane root = new Pane(layout);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 180.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                layout.resizeRelocate(24.0, 24.0, 440.0, 96.0);
                root.layout();
                M3IconButton clearButton = textInputClearButton(layout);

                leading.requestFocus();
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.END));

                assertTrue(clearButton.isFocused());
                assertSame(clearButton, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                clearButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));

                assertTrue(textField.isFocused());

                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                root.applyCss();
                root.layout();

                leading.requestFocus();
                leading.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));

                assertTrue(clearButton.isFocused());

                clearButton.fire();

                assertEquals("", textField.getText());
                assertTrue(textField.isFocused());
                assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that text input layouts expose focused descendants inside compound adornment slots.
    @Test
    void textInputLayoutRoutesAccessibleFocusToDescendantAdornments() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField("abc");
            M3Button leadingPrimary = new M3Button("Leading primary");
            M3Button leadingSecondary = new M3Button("Leading secondary");
            M3Button trailingPrimary = new M3Button("Trailing primary");
            M3Button trailingSecondary = new M3Button("Trailing secondary");
            HBox leadingGroup = new HBox(leadingPrimary, leadingSecondary);
            HBox trailingGroup = new HBox(trailingPrimary, trailingSecondary);
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Helper text");
            layout.setLeading(leadingGroup);
            layout.setTrailing(trailingGroup);

            Pane root = new Pane(layout);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 560.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, leadingSecondary);

                assertTrue(leadingSecondary.isFocused());
                assertSame(leadingSecondary, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                trailingPrimary.requestFocus();

                assertTrue(trailingPrimary.isFocused());
                assertSame(trailingPrimary, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, trailingSecondary);

                assertTrue(trailingSecondary.isFocused());
                assertSame(trailingSecondary, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that text input layouts route explicit reveal actions into popup-backed adornments.
    @Test
    void textInputLayoutRoutesAccessibleFocusThroughPopupAdornments() {
        FxTestUtils.runOnFxThread(() -> {
            M3MenuItem leadingTarget = new M3MenuItem("Leading target");
            M3SubMenuItem leadingSubMenu = new M3SubMenuItem("Leading submenu", leadingTarget);
            M3MenuButton leadingMenuButton = new M3MenuButton("Leading menu", leadingSubMenu);

            M3MenuItem trailingTarget = new M3MenuItem("Trailing target");
            M3SubMenuItem trailingSubMenu = new M3SubMenuItem("Trailing submenu", trailingTarget);
            M3MenuButton trailingMenuButton = new M3MenuButton("Trailing menu", trailingSubMenu);

            M3TextField textField = new M3TextField("abc");
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Helper text");
            layout.setLeading(leadingMenuButton);
            layout.setTrailing(trailingMenuButton);
            leadingMenuButton.setAnimationsEnabled(false);
            leadingSubMenu.setAnimationsEnabled(false);
            trailingMenuButton.setAnimationsEnabled(false);
            trailingSubMenu.setAnimationsEnabled(false);

            Pane root = new Pane(layout);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 640.0, 180.0));
                stage.show();
                root.applyCss();
                root.layout();

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, leadingTarget);
                assertNestedPopupAccessibleFocus(layout, leadingMenuButton, leadingSubMenu, leadingTarget);

                layout.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(leadingTarget.isFocused());
                assertSame(leadingTarget, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                leadingSubMenu.hideSubMenu();
                leadingMenuButton.hideMenu();

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, List.of(trailingTarget));
                assertNestedPopupAccessibleFocus(layout, trailingMenuButton, trailingSubMenu, trailingTarget);
            } finally {
                trailingSubMenu.hideSubMenu();
                trailingMenuButton.hideMenu();
                leadingSubMenu.hideSubMenu();
                leadingMenuButton.hideMenu();
                trailingSubMenu.setAnimationsEnabled(true);
                trailingMenuButton.setAnimationsEnabled(true);
                leadingSubMenu.setAnimationsEnabled(true);
                leadingMenuButton.setAnimationsEnabled(true);
                stage.close();
            }
        });
    }

    /// Verifies that hidden text input layout owners do not route accessibility focus into children.
    @Test
    void textInputLayoutSkipsAccessibleFocusWhenOwnerHiddenByAncestor() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField("abc");
            M3IconButton trailing = createIconButton("T");
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Helper text");
            layout.setTrailing(trailing);

            M3Button outside = new M3Button("Outside");
            Pane hiddenOwner = new Pane(layout);
            VBox root = new VBox(8.0, outside, hiddenOwner);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 460.0, 200.0));
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                hiddenOwner.setVisible(false);
                root.applyCss();
                root.layout();

                layout.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(outside.isFocused());
                assertFalse(textField.isFocused());
                assertFalse(trailing.isFocused());

                layout.executeAccessibleAction(AccessibleAction.SHOW_ITEM, trailing);

                assertTrue(outside.isFocused());
                assertFalse(textField.isFocused());
                assertFalse(trailing.isFocused());
                assertSame(textField, layout.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that text input layouts mirror logical adornments and floating label geometry in right-to-left mode.
    @Test
    void textInputLayoutMirrorsAdornmentsAndFloatingLabelForRightToLeft() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Project name");
            layout.setLabelText("Outlined with text");
            layout.setLeading(new M3Icon("T"));
            layout.setTrailing(createIconButton("V"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(430.0, 140.0);
            root.layout();
            layout.layout();

            Text label = assertInstanceOf(Text.class, layout.lookup("." + "m3-text-input-label"));
            assertTextInputLayoutUsesLogicalGeometry(layout, true);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, label, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-text-field-rtl.png"
            ));
        });
    }

    /// Verifies that text input layouts reflow logical geometry after runtime orientation changes.
    @Test
    void textInputLayoutReflowsAdornmentsAndFloatingLabelAfterRuntimeOrientationChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setSupportingText("Project name");
            layout.setLabelText("Outlined with text");
            layout.setLeading(new M3Icon("T"));
            layout.setTrailing(createIconButton("V"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setPrefWidth(360.0);

            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(430.0, 140.0);
            root.layout();
            layout.layout();

            assertTextInputLayoutUsesLogicalGeometry(layout, false);

            layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.applyCss();
            root.layout();
            layout.layout();

            assertTextInputLayoutUsesLogicalGeometry(layout, true);

            layout.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            root.applyCss();
            root.layout();
            layout.layout();

            assertTextInputLayoutUsesLogicalGeometry(layout, false);
        });
    }

    /// Verifies that text input trailing icon buttons keep square state layers and visible ripples.
    @Tier2Test
    @Test
    void textInputLayoutTrailingIconButtonKeepsSquareRipple() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable StackPane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3IconButton> trailingButtonReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> trailingIconRippleIsVisible(trailingButtonReference),
                    () -> {
                        M3PasswordField passwordField = createPasswordField("Hello", M3TextInputVariant.OUTLINED);
                        passwordField.setPrefWidth(320.0);
                        M3IconButton trailingButton = createIconButton("V");

                        M3TextInputLayout layout =
                                new M3TextInputLayout(passwordField);
                        layout.setSupportingText("At least 8 characters");
                        layout.setTrailing(trailingButton);
                        layout.setPrefWidth(320.0);

                        StackPane root = new StackPane(layout);
                        root.setAlignment(Pos.TOP_LEFT);
                        root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; "
                                + visualTestColors());
                        Scene scene = new Scene(root, 390.0, 140.0);
                        Stage stage = new Stage();

                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        FxTestUtils.setCssAnimationsEnabled(root, true);
                        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        root.resize(390.0, 140.0);
                        root.layout();

                        Region stateLayer = lookupRegion(trailingButton, ".m3-state-layer-container");
                        assertEquals(48.0, trailingButton.getWidth(), 0.0001);
                        assertEquals(48.0, trailingButton.getHeight(), 0.0001);
                        assertEquals(40.0, stateLayer.getWidth(), 0.0001);
                        assertEquals(40.0, stateLayer.getHeight(), 0.0001);

                        stageReference.set(stage);
                        rootReference.set(root);
                        trailingButtonReference.set(trailingButton);
                        trailingButton.fireEvent(primaryMouseEvent(
                                MouseEvent.MOUSE_PRESSED,
                                trailingButton.getWidth() / 2.0,
                                trailingButton.getHeight() / 2.0,
                                true
                        ));
                    },
                    () -> {
                        StackPane root = Objects.requireNonNull(rootReference.get(), "root");
                        M3IconButton trailingButton =
                                Objects.requireNonNull(trailingButtonReference.get(), "trailingButton");
                        Region ripple = lookupRegion(trailingButton, ".m3-ripple");

                        assertTrue(ripple.getOpacity() > 0.0, () -> "ripple opacity=" + ripple.getOpacity());
                        assertTrue(ripple.getScaleX() > 0.0, () -> "ripple scaleX=" + ripple.getScaleX());
                        assertTrue(ripple.getScaleY() > 0.0, () -> "ripple scaleY=" + ripple.getScaleY());
                        writeVisualSnapshot(snapshotImageOnFxThread(root), java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-text-field-trailing-ripple.png"
                        ));

                        trailingButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 20.0, 20.0, false));
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3IconButton trailingButton = trailingButtonReference.get();
                if (trailingButton != null && trailingButton.isArmed()) {
                    trailingButton.fireEvent(primaryMouseEvent(MouseEvent.MOUSE_RELEASED, 20.0, 20.0, false));
                }
                @Nullable StackPane root = rootReference.get();
                if (root != null) {
                    FxTestUtils.setCssAnimationsEnabled(root, true);
                    FxTestUtils.clearMotionScheme(root);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that text input layouts apply error state from error text and character overflow.
    @Test
    void textInputLayoutAppliesErrorStateFromTextAndCharacterLimit() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField("abcdef");
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setSupportingText("Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(4);

        applyCss(layout);

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + "m3-text-input-supporting-text")
        );
        Label counter = assertInstanceOf(Label.class, layout.lookup("." + "m3-text-input-counter"));
        assertTrue(layout.isCharacterLimitExceeded());
        assertTrue(textField.isError());
        assertTrue(counter.getPseudoClassStates().contains(error));
        assertEquals("Helper text", supportingText.getText());
        assertEquals("6 / 4", counter.getText());

        layout.setErrorText("Too long");

        assertTrue(textField.isError());
        assertTrue(supportingText.getPseudoClassStates().contains(error));
        assertEquals("Too long", supportingText.getText());
        assertEquals("Helper text Too long 6 / 4",
                layout.queryAccessibleAttribute(AccessibleAttribute.TEXT));

        layout.setCharacterLimit(10);
        layout.setErrorText("");

        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertFalse(supportingText.getPseudoClassStates().contains(error));
        assertFalse(counter.getPseudoClassStates().contains(error));
        assertEquals("Helper text", supportingText.getText());
        assertEquals("6 / 10", counter.getText());
    }

    /// Verifies that text input layouts run configured validators and refresh active validation on edits.
    @Test
    void textInputLayoutValidatesWithConfiguredValidator() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setLabelText("Email");
        layout.setSupportingText("Helper text");
        layout.setValidator((input, text) -> text.isBlank()
                ? "Email is required"
                : text.contains("@") ? null : "Use an email address");

        applyCss(layout);

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + "m3-text-input-supporting-text")
        );
        assertTrue(layout.isValidateOnFocusLost());
        assertTrue(layout.isValidateOnTextChange());
        assertFalse(layout.isValidationActive());
        assertFalse(layout.isValidationError());
        assertEquals("", layout.getValidationErrorText());
        assertEquals("Helper text", supportingText.getText());
        assertFalse(textField.isError());

        assertFalse(layout.validate());

        assertTrue(layout.isValidationActive());
        assertTrue(layout.validationActiveProperty().get());
        assertTrue(layout.isValidationError());
        assertEquals("Email is required", layout.getValidationErrorText());
        assertEquals("Email is required", layout.validationErrorTextProperty().get());
        assertEquals("Email Helper text Email is required",
                layout.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals("Email is required", supportingText.getText());
        assertTrue(supportingText.getPseudoClassStates().contains(error));
        assertTrue(textField.isError());

        textField.setText("support");

        assertEquals("Use an email address", layout.getValidationErrorText());
        assertEquals("Use an email address", supportingText.getText());
        assertTrue(textField.isError());

        textField.setText("support@example.com");

        assertFalse(layout.isValidationError());
        assertEquals("", layout.getValidationErrorText());
        assertEquals("Helper text", supportingText.getText());
        assertFalse(supportingText.getPseudoClassStates().contains(error));
        assertFalse(textField.isError());

        layout.clearValidation();

        assertFalse(layout.isValidationActive());
        assertFalse(layout.isValidationError());
        assertEquals("Email Helper text", layout.queryAccessibleAttribute(AccessibleAttribute.TEXT));
    }

    /// Verifies that visible supporting rows do not replay entry motion while validation refreshes during edits.
    @Test
    void textInputLayoutDoesNotFlickerVisibleValidationFeedbackOnEdits() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField();
            textField.setPrefWidth(280.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setLabelText("Email");
            layout.setSupportingText("Helper text");
            layout.setValidator((input, text) -> text.contains("@") ? null : "Use an email address");
            layout.setPrefWidth(280.0);

            StackPane root = new StackPane(layout);
            Scene scene = new Scene(root, 360.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(360.0, 120.0);
            root.layout();

            HBox supportingRow = assertInstanceOf(
                    HBox.class,
                    layout.lookup("." + "m3-text-input-supporting-row")
            );

            assertFalse(layout.validate());
            assertEquals("Use an email address", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);

            textField.setText("support");

            assertEquals("Use an email address", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);

            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(32);
            assertVisibleSupportingRowIsStable(supportingRow);

            textField.setText("supportx");

            assertEquals("Use an email address", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);

            textField.setText("support@example.com");

            assertEquals("", layout.getValidationErrorText());
            assertVisibleSupportingRowIsStable(supportingRow);
        });
    }

    /// Verifies that stable floating-label geometry is not rewritten for every entered character.
    @Test
    void textInputLayoutRetainsStableGeometryObjectsDuringTyping() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField("a");
            textField.setVariant(M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(280.0);
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setLabelText("Project name");
            layout.setSupportingText("Supporting text");
            layout.setPrefWidth(280.0);

            StackPane root = new StackPane(layout);
            Scene scene = new Scene(root, 360.0, 120.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(360.0, 120.0);
            root.layout();

            Text label = assertInstanceOf(
                    Text.class,
                    layout.lookup("." + "m3-text-input-label")
            );
            Insets inputPadding = textField.getPadding();
            var labelFont = label.getFont();
            AtomicInteger inputPaddingChanges = new AtomicInteger();
            AtomicInteger labelFontChanges = new AtomicInteger();
            textField.paddingProperty().addListener(observable -> inputPaddingChanges.incrementAndGet());
            label.fontProperty().addListener(observable -> labelFontChanges.incrementAndGet());

            for (int length = 2; length <= 64; length++) {
                textField.setText("x".repeat(length));
            }
            root.layout();

            assertSame(inputPadding, textField.getPadding());
            assertSame(labelFont, label.getFont());
            assertEquals(1.0, renderedScaleX(label), 0.0001);
            assertEquals(0, inputPaddingChanges.get());
            assertEquals(0, labelFontChanges.get());
            assertTrue(layout.isLabelFloating());
        });
    }

    /// Verifies that reusable text input validators cover common validation rules.
    @Test
    void textInputValidatorsProvideReusableRules() {
        M3TextField textField = new M3TextField();
        M3TextInputValidator validator = M3TextInputValidators.all(
                M3TextInputValidators.required("Required"),
                M3TextInputValidators.lengthBetween(3, 5, "Too short", "Too long"),
                M3TextInputValidators.pattern(Pattern.compile("[a-z]+"), "Letters only")
        );

        assertNull(M3TextInputValidators.none().validate(textField, ""));
        assertEquals("Required", validator.validate(textField, ""));
        assertEquals("Too short", validator.validate(textField, "ab"));
        assertEquals("Too long", validator.validate(textField, "abcdef"));
        assertEquals("Letters only", validator.validate(textField, "abc1"));
        assertNull(validator.validate(textField, "abc"));
        assertEquals("Predicate failed", M3TextInputValidators.predicate(
                (input, text) -> text.equals(input.getText()),
                "Predicate failed"
        ).validate(textField, "other"));
        assertThrows(IllegalArgumentException.class, () ->
                M3TextInputValidators.lengthBetween(4, 3, "Too short", "Too long"));
        assertThrows(IllegalArgumentException.class, () ->
                M3TextInputValidators.minLength(-1, "Too short"));
        assertThrows(NullPointerException.class, () ->
                M3TextInputValidators.all(M3TextInputValidators.none(), null));
    }

    /// Verifies that text input layouts evaluate an additional validation pipeline.
    @Test
    void textInputLayoutRunsAdditionalValidators() {
        PseudoClass error = PseudoClass.getPseudoClass("error");
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setLabelText("Email");
        layout.setSupportingText("Helper text");
        M3TextInputValidator emailValidator = M3TextInputValidators.pattern(
                Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                "Use an email address"
        );
        M3TextInputValidator maxLengthValidator = M3TextInputValidators.maxLength(12, "Email is too long");

        layout.setValidator(M3TextInputValidators.required("Email is required"));
        layout.getValidators().add(emailValidator);
        layout.getValidators().add(maxLengthValidator);

        applyCss(layout);

        Label supportingText = assertInstanceOf(
                Label.class,
                layout.lookup("." + "m3-text-input-supporting-text")
        );
        assertEquals(java.util.List.of(emailValidator, maxLengthValidator), layout.getValidators());

        assertFalse(layout.validate());

        assertEquals("Email is required", layout.getValidationErrorText());
        assertEquals("Email is required", supportingText.getText());
        assertTrue(supportingText.getPseudoClassStates().contains(error));
        assertTrue(textField.isError());

        textField.setText("support");

        assertEquals("Use an email address", layout.getValidationErrorText());

        textField.setText("support@example.com");

        assertEquals("Email is too long", layout.getValidationErrorText());

        layout.getValidators().remove(maxLengthValidator);

        assertEquals("", layout.getValidationErrorText());
        assertFalse(textField.isError());
        assertEquals("Helper text", supportingText.getText());

        layout.getValidators().setAll(maxLengthValidator);

        assertEquals("Email is too long", layout.getValidationErrorText());

        layout.getValidators().clear();

        assertEquals("", layout.getValidationErrorText());
        assertTrue(layout.getValidators().isEmpty());
    }

    /// Verifies that text input layouts can keep validation errors stable while editing.
    @Test
    void textInputLayoutCanDisableTextChangeValidationRefresh() {
        M3TextField textField = new M3TextField();
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setLabelText("Email");
        layout.setSupportingText("Helper text");
        layout.setValidateOnTextChange(false);
        layout.setValidator((input, text) -> text.contains("@") ? null : "Use an email address");

        applyCss(layout);

        assertFalse(layout.validate());
        assertEquals("Use an email address", layout.getValidationErrorText());

        textField.setText("support@example.com");

        assertEquals("Use an email address", layout.getValidationErrorText());
        assertTrue(textField.isError());

        assertTrue(layout.validate());

        assertEquals("", layout.getValidationErrorText());
        assertFalse(textField.isError());
    }

    /// Verifies that focus-loss validation activates configured validators.
    @Test
    void textInputLayoutValidatesOnFocusLoss() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = new M3TextField();
            M3Button nextButton = new M3Button("Next");
            M3TextInputLayout layout = new M3TextInputLayout(textField);
            layout.setLabelText("Email");
            layout.setSupportingText("Helper text");
            layout.setValidator((input, text) -> text.isBlank() ? "Email is required" : null);

            Pane root = new Pane(layout, nextButton);
            Scene scene = new Scene(root, 420.0, 160.0);
            Stage stage = new Stage();
            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.show();
                root.applyCss();

                textField.requestFocus();
                assertTrue(textField.isFocused());
                assertFalse(layout.isValidationActive());

                nextButton.requestFocus();

                assertFalse(textField.isFocused());
                assertTrue(layout.isValidationActive());
                assertEquals("Email is required", layout.getValidationErrorText());
                assertTrue(textField.isError());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that text input layouts can enforce character limits and expose a clear button.
    @Test
    void textInputLayoutEnforcesCharacterLimitAndClearButton() {
        M3TextField textField = new M3TextField("abcdef");
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setSupportingText("Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(4);
        layout.setCharacterLimitEnforced(true);
        layout.setClearButtonEnabled(true);

        applyCss(layout);

        Label counter = assertInstanceOf(Label.class, layout.lookup("." + "m3-text-input-counter"));
        StackPane trailingSlot = assertInstanceOf(
                StackPane.class,
                layout.lookup("." + "m3-text-input-trailing")
        );
        M3IconButton clearButton = textInputClearButton(layout);

        assertEquals("abcd", textField.getText());
        assertEquals(4, layout.getCharacterCount());
        assertTrue(layout.isCharacterLimitEnforced());
        assertTrue(layout.isClearButtonEnabled());
        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertEquals("4 / 4", counter.getText());
        assertTrue(clearButton.getStyleClass().contains("m3-text-input-clear-button"));
        assertEquals("Clear text", clearButton.getAccessibleText());
        assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(textField, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        assertEquals(clearButton, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(clearButton, trailingSlot.getChildren().get(0));
        assertEquals(48.0, textField.getPadding().getRight(), 0.0001);

        clearButton.fire();

        assertEquals("", textField.getText());
        assertEquals(1, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertTrue(trailingSlot.getChildren().isEmpty());
        assertEquals(16.0, textField.getPadding().getRight(), 0.0001);
        assertEquals("0 / 4", counter.getText());

        textField.setText("123456");

        assertEquals("1234", textField.getText());
        assertFalse(textField.isError());
        assertEquals("4 / 4", counter.getText());

        M3Icon customTrailing = new M3Icon("T");
        layout.setTrailing(customTrailing);

        assertEquals(2, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        assertEquals(customTrailing, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
        assertEquals(customTrailing, trailingSlot.getChildren().get(0));
    }

    /// Verifies that character limits preserve application-bound input text.
    @Test
    void textInputLayoutPreservesBoundTextWhenCharacterLimitIsEnforced() {
        M3TextField textField = new M3TextField();
        SimpleStringProperty applicationText = new SimpleStringProperty("abcdef");
        textField.textProperty().bind(applicationText);

        M3TextInputLayout layout = new M3TextInputLayout(textField);

        layout.setSupportingText("Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(4);
        layout.setCharacterLimitEnforced(true);
        layout.setClearButtonEnabled(true);

        applyCss(layout);

        Label counter = assertInstanceOf(Label.class, layout.lookup("." + "m3-text-input-counter"));
        StackPane trailingSlot = assertInstanceOf(
                StackPane.class,
                layout.lookup("." + "m3-text-input-trailing")
        );
        assertEquals("abcdef", textField.getText());
        assertEquals("abcdef", applicationText.get());
        assertEquals(6, layout.getCharacterCount());
        assertTrue(layout.isCharacterLimitExceeded());
        assertTrue(textField.isError());
        assertEquals("6 / 4", counter.getText());
        assertTrue(trailingSlot.getChildren().isEmpty());
        assertEquals(1, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        applicationText.set("abc");

        assertEquals("abc", textField.getText());
        assertEquals(3, layout.getCharacterCount());
        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertEquals("3 / 4", counter.getText());
        assertTrue(trailingSlot.getChildren().isEmpty());

        textField.textProperty().unbind();
        textField.setText("abcdef");

        assertEquals("abcd", textField.getText());
        assertEquals(4, layout.getCharacterCount());
        assertFalse(layout.isCharacterLimitExceeded());
        assertEquals("4 / 4", counter.getText());
        M3IconButton clearButton = textInputClearButton(layout);
        assertEquals(clearButton, trailingSlot.getChildren().get(0));

        textField.setEditable(false);
        assertTrue(trailingSlot.getChildren().isEmpty());
        assertEquals(1, layout.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));

        textField.setEditable(true);
        assertEquals(clearButton, trailingSlot.getChildren().get(0));
        clearButton.fire();
        assertEquals("", textField.getText());
    }

    /// Verifies that layout-owned error states preserve application-bound input error properties.
    @Test
    void textInputLayoutPreservesBoundInputErrorState() {
        M3TextField textField = new M3TextField("abcdef");
        SimpleBooleanProperty applicationError = new SimpleBooleanProperty(false);
        textField.errorProperty().bind(applicationError);

        M3TextInputLayout layout = new M3TextInputLayout(textField);

        layout.setSupportingText("Helper text");
        layout.setCharacterCounterVisible(true);
        layout.setCharacterLimit(4);

        applyCss(layout);

        Label counter = assertInstanceOf(Label.class, layout.lookup("." + "m3-text-input-counter"));

        assertEquals(6, layout.getCharacterCount());
        assertTrue(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertEquals("6 / 4", counter.getText());

        applicationError.set(true);
        layout.setCharacterLimit(3);

        assertTrue(layout.isCharacterLimitExceeded());
        assertTrue(textField.isError());
        assertEquals("6 / 3", counter.getText());

        textField.errorProperty().unbind();
        layout.setCharacterLimit(4);

        assertTrue(textField.isError());

        textField.errorProperty().bind(applicationError);
        applicationError.set(false);
        layout.setInput(null);

        assertFalse(textField.isError());
        assertEquals(0, layout.getCharacterCount());

        textField.errorProperty().unbind();
        layout.setInput(textField);

        assertTrue(layout.isCharacterLimitExceeded());
        assertTrue(textField.isError());

        textField.setText("abc");

        assertFalse(layout.isCharacterLimitExceeded());
        assertFalse(textField.isError());
        assertEquals("3 / 4", counter.getText());
    }

    /// Verifies that text input layouts reject unsupported input nodes and invalid limits.
    @Test
    void textInputLayoutValidatesInputAndCharacterLimit() {
        M3TextInputLayout layout = new M3TextInputLayout();

        assertThrows(IllegalArgumentException.class, () -> layout.setInput(new javafx.scene.control.TextField()));
        assertThrows(IllegalArgumentException.class, () -> layout.setCharacterLimit(-2));
    }

    /// Verifies that text input error styles resolve to the Material error color token.
    @Test
    void textInputErrorStylesUseErrorColor() {
        FxTestUtils.runOnFxThread(() -> {
            Color errorColor = Color.rgb(186, 26, 26);
            M3TextField filledField = new M3TextField("Filled error");
            filledField.setError(true);
            filledField.setPrefWidth(180.0);
            M3TextField outlinedField = createTextField("Outlined error", M3TextInputVariant.OUTLINED);
            outlinedField.setError(true);
            outlinedField.setPrefWidth(190.0);
            M3PasswordField passwordField = createPasswordField("secret", M3TextInputVariant.OUTLINED);
            passwordField.setError(true);
            passwordField.setPrefWidth(160.0);
            M3TextArea textArea = createTextArea("Multiline\nerror", M3TextInputVariant.FILLED);
            textArea.setError(true);
            textArea.setPrefSize(240.0, 96.0);

            FlowPane row = new FlowPane(16.0, 16.0, filledField, outlinedField, passwordField, textArea);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 840.0, 240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(840.0, 240.0);
            row.layout();

            assertBorderBottomColor(filledField, errorColor);
            assertBorderColor(outlinedField, errorColor);
            assertBorderColor(passwordField, errorColor);
            assertBorderBottomColor(textArea, errorColor);

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeBorderContainsContrast(image, filledField, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, outlinedField, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, passwordField, Color.WHITE, 0.08);
            assertSnapshotNodeBorderContainsContrast(image, textArea, Color.WHITE, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-input-errors.png"
            ));
        });
    }

    /// Verifies that focused text input states keep Material field colors.
    @Test
    void textInputStateStylesPreserveVariantColors() {
        M3TextField filledField = new M3TextField();
        M3PasswordField outlinedField = new M3PasswordField();
        outlinedField.setVariant(M3TextInputVariant.OUTLINED);
        Pane root = new Pane(filledField, outlinedField);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.setStyle(root.getStyle() + " " + buttonStateTestColors());
        filledField.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        outlinedField.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        root.applyCss();

        assertRegionFill(filledField, Color.rgb(37, 38, 39));
        assertBorderBottomColor(filledField, Color.rgb(1, 2, 3));
        assertEquals(2.0, filledField.getBorder().getStrokes().get(0).getWidths().getBottom(), 0.0001);
        assertRegionFill(outlinedField, Color.TRANSPARENT);
        assertBorderColor(outlinedField, Color.rgb(1, 2, 3));
        assertEquals(3.0, outlinedField.getBorder().getStrokes().get(0).getWidths().getTop(), 0.0001);
    }

    /// Verifies that disabled text inputs keep separate Material container, outline, and content opacities.
    @Test
    void disabledTextInputStatesUseMaterialComponentOpacities() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField standaloneFilled = createTextField("Filled value", M3TextInputVariant.FILLED);
            standaloneFilled.setDisable(true);
            M3TextField standaloneOutlined = createTextField("Outlined value", M3TextInputVariant.OUTLINED);
            standaloneOutlined.setDisable(true);

            M3TextField wrappedFilled = createTextField("Wrapped filled", M3TextInputVariant.FILLED);
            M3TextInputLayout filledLayout = new M3TextInputLayout(wrappedFilled);
            filledLayout.setSupportingText("Disabled supporting text");
            filledLayout.setLabelText("Disabled filled");
            M3Icon leading = new M3Icon("L");
            filledLayout.setLeading(leading);
            wrappedFilled.setDisable(true);

            M3TextField wrappedOutlined = createTextField("Wrapped outlined", M3TextInputVariant.OUTLINED);
            M3TextInputLayout outlinedLayout =
                    new M3TextInputLayout(wrappedOutlined);
            outlinedLayout.setSupportingText("Disabled outlined supporting text");
            outlinedLayout.setLabelText("Disabled outlined");
            M3Icon trailingIcon = new M3Icon("T");
            M3IconButton trailingButton = new M3IconButton(trailingIcon);
            outlinedLayout.setTrailing(trailingButton);
            wrappedOutlined.setDisable(true);

            VBox root = new VBox(12.0, standaloneFilled, standaloneOutlined, filledLayout, outlinedLayout);
            Scene scene = new Scene(root, 420.0, 360.0);
            M3Theme theme = M3Theme.defaultTheme();
            M3ThemeManager.install(scene, theme);
            root.applyCss();
            root.layout();

            Color onSurface = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE);
            Color disabledFilledContainer = withOpacity(onSurface, 0.04);
            Color disabledOutline = withOpacity(onSurface, 0.12);
            Color disabledContent = withOpacity(onSurface, 0.38);

            assertEquals(1.0, standaloneFilled.getOpacity(), 0.0001);
            assertRegionFill(standaloneFilled, disabledFilledContainer);
            assertBorderBottomColor(standaloneFilled, disabledContent);
            assertEquals(1.0,
                    standaloneFilled.getBorder().getStrokes().get(0).getWidths().getBottom(),
                    0.0001);

            assertEquals(1.0, standaloneOutlined.getOpacity(), 0.0001);
            assertRegionFill(standaloneOutlined, Color.TRANSPARENT);
            assertBorderColor(standaloneOutlined, disabledOutline);
            assertEquals(1.0,
                    standaloneOutlined.getBorder().getStrokes().get(0).getWidths().getTop(),
                    0.0001);

            PseudoClass inputDisabled = PseudoClass.getPseudoClass("input-disabled");
            assertTrue(filledLayout.getPseudoClassStates().contains(inputDisabled));
            assertTrue(outlinedLayout.getPseudoClassStates().contains(inputDisabled));
            assertRegionFill(
                    lookupRegion(filledLayout, "." + "m3-text-input-container"),
                    Color.TRANSPARENT
            );
            assertRegionFill(wrappedFilled, disabledFilledContainer);
            assertBorderBottomColor(wrappedFilled, disabledContent);

            Path outline = assertInstanceOf(
                    Path.class,
                    outlinedLayout.lookup("." + "m3-text-input-outline")
            );
            assertEquals(onSurface, outline.getStroke());
            assertEquals(0.12, outline.getOpacity(), 0.0001);

            Text outlinedLabel = assertInstanceOf(
                    Text.class,
                    outlinedLayout.lookup("." + "m3-text-input-label")
            );
            assertEquals(disabledContent, outlinedLabel.getFill());
            assertEquals(1.0, outlinedLabel.getOpacity(), 0.0001);

            Region leadingSlot = lookupRegion(
                    filledLayout,
                    "." + "m3-text-input-leading"
            );
            assertEquals(0.38, leadingSlot.getOpacity(), 0.0001);
            assertEquals(1.0, leading.getOpacity(), 0.0001);
            Region trailingSlot = lookupRegion(
                    outlinedLayout,
                    "." + "m3-text-input-trailing"
            );
            assertEquals(0.38, trailingSlot.getOpacity(), 0.0001);
            assertEquals(1.0, trailingButton.getOpacity(), 0.0001);
            assertEquals(1.0, trailingIcon.getOpacity(), 0.0001);
        });
    }

    /// Verifies Material text-input colors across enabled, hovered, focused, and error states.
    @Test
    void textInputLayoutUsesMaterialStateColors() {
        FxTestUtils.runOnFxThread(() -> {
            PseudoClass hover = PseudoClass.getPseudoClass("hover");

            M3TextField directFilled = createTextField("Filled", M3TextInputVariant.FILLED);
            directFilled.setPrefWidth(260.0);
            M3TextField directOutlined = createTextField("Outlined", M3TextInputVariant.OUTLINED);
            directOutlined.setPrefWidth(260.0);

            M3TextField filledField = createTextField("M3FX", M3TextInputVariant.FILLED);
            filledField.setPrefWidth(260.0);
            M3TextInputLayout filledLayout = new M3TextInputLayout(filledField);
            filledLayout.setSupportingText("Supporting text");
            filledLayout.setLabelText("Filled label");
            filledLayout.setPrefWidth(260.0);

            M3TextField focusedField = createTextField("M3FX", M3TextInputVariant.OUTLINED);
            focusedField.setPrefWidth(260.0);
            M3TextInputLayout focusedLayout = new M3TextInputLayout(focusedField);
            focusedLayout.setSupportingText("Project name");
            focusedLayout.setLabelText("Focused label");
            focusedLayout.setPrefWidth(260.0);

            M3TextField errorField = createTextField("Invalid", M3TextInputVariant.OUTLINED);
            errorField.setPrefWidth(260.0);
            M3Icon errorTrailing = new M3Icon("error");
            M3TextInputLayout errorLayout = new M3TextInputLayout(errorField);
            errorLayout.setSupportingText("Helper text");
            errorLayout.setLabelText("Error label");
            errorLayout.setErrorText("Use a valid value");
            errorLayout.setTrailing(errorTrailing);
            errorLayout.setPrefWidth(260.0);

            VBox root = new VBox(16.0, directFilled, directOutlined, filledLayout, focusedLayout, errorLayout);
            root.setFocusTraversable(true);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px;");
            Scene scene = new Scene(root, 360.0, 430.0);
            Stage stage = new Stage();
            M3Theme theme = M3Theme.defaultTheme();
            Color primary = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.PRIMARY);
            Color error = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ERROR);
            Color onSurface = theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_SURFACE);
            Color onErrorContainer =
                    theme.colorScheme().getColor(org.glavo.monetfx.ColorRole.ON_ERROR_CONTAINER);
            Color unquantizedHoverContainer = theme.colorScheme()
                    .getColor(org.glavo.monetfx.ColorRole.SURFACE_CONTAINER_HIGHEST)
                    .interpolate(onSurface, theme.tokens().stateLayerTokens().hoverOpacity());
            Color hoverContainer = Color.rgb(
                    (int) Math.round(unquantizedHoverContainer.getRed() * 255.0),
                    (int) Math.round(unquantizedHoverContainer.getGreen() * 255.0),
                    (int) Math.round(unquantizedHoverContainer.getBlue() * 255.0)
            );
            try {
                M3ThemeManager.install(scene, theme);
                stage.setScene(scene);
                stage.show();
                root.requestFocus();

                directFilled.pseudoClassStateChanged(hover, true);
                directOutlined.pseudoClassStateChanged(hover, true);
                filledLayout.pseudoClassStateChanged(hover, true);
                filledField.pseudoClassStateChanged(hover, true);
                focusedLayout.pseudoClassStateChanged(hover, true);
                focusedField.pseudoClassStateChanged(hover, true);
                errorLayout.pseudoClassStateChanged(hover, true);
                errorField.pseudoClassStateChanged(hover, true);
                root.applyCss();
                root.layout();

                assertRegionFill(directFilled, hoverContainer);
                assertBorderBottomColor(directFilled, onSurface);
                assertEquals(onSurface, firstBorderTopPaint(directOutlined));
                assertRegionFill(
                        lookupRegion(filledLayout, "." + "m3-text-input-container"),
                        hoverContainer
                );

                Path hoveredOutline = assertInstanceOf(
                        Path.class,
                        focusedLayout.lookup("." + "m3-text-input-outline")
                );
                Text hoveredLabel = assertInstanceOf(
                        Text.class,
                        focusedLayout.lookup("." + "m3-text-input-label")
                );
                assertEquals(onSurface, hoveredOutline.getStroke());
                assertEquals(onSurface, hoveredLabel.getFill());

                Path hoveredErrorOutline = assertInstanceOf(
                        Path.class,
                        errorLayout.lookup("." + "m3-text-input-outline")
                );
                Text errorLabel = assertInstanceOf(
                        Text.class,
                        errorLayout.lookup("." + "m3-text-input-label")
                );
                assertEquals(onErrorContainer, hoveredErrorOutline.getStroke());
                assertEquals(onErrorContainer, errorLabel.getFill());
                Region errorTrailingSlot = lookupRegion(
                        errorLayout,
                        "." + "m3-text-input-trailing"
                );
                assertTrue(errorTrailingSlot.getPseudoClassStates().contains(PseudoClass.getPseudoClass("error")));
                assertEquals(onErrorContainer, iconFill(errorTrailing));

                directFilled.pseudoClassStateChanged(hover, false);
                directOutlined.pseudoClassStateChanged(hover, false);
                filledLayout.pseudoClassStateChanged(hover, false);
                filledField.pseudoClassStateChanged(hover, false);
                focusedLayout.pseudoClassStateChanged(hover, false);
                focusedField.pseudoClassStateChanged(hover, false);
                errorLayout.pseudoClassStateChanged(hover, false);
                errorField.pseudoClassStateChanged(hover, false);

                focusedField.requestFocus();
                root.applyCss();
                root.layout();

                assertTrue(focusedField.isFocused(), "focused text field should own focus");
                Text focusedLabel = assertInstanceOf(
                        Text.class,
                        focusedLayout.lookup("." + "m3-text-input-label")
                );
                assertTrue(focusedLabel.getPseudoClassStates().contains(PseudoClass.getPseudoClass("focused")));
                assertEquals(primary, focusedLabel.getFill());
                assertEquals(error, errorLabel.getFill());
                assertEquals(error, iconFill(errorTrailing));

                errorField.requestFocus();
                root.applyCss();
                root.layout();

                assertTrue(errorField.isFocused(), "error text field should own focus");
                assertTrue(errorLabel.getPseudoClassStates().contains(PseudoClass.getPseudoClass("focused")));
                assertEquals(error, errorLabel.getFill());
                assertEquals(error, iconFill(errorTrailing));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that inputs render filled, outlined, password, and multiline visual variants.
    @Tier2Test
    @Test
    void inputSnapshotRendersFilledOutlinedPasswordAndTextAreaControls() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField filledField = new M3TextField("Filled text");
            filledField.setPrefWidth(180.0);
            M3TextField outlinedField = createTextField("Outlined text", M3TextInputVariant.OUTLINED);
            outlinedField.setPrefWidth(190.0);
            M3PasswordField passwordField = createPasswordField("secret", M3TextInputVariant.OUTLINED);
            passwordField.setPrefWidth(160.0);
            M3TextArea textArea = createTextArea("Multiline\ncontent", M3TextInputVariant.FILLED);
            textArea.setPrefSize(240.0, 96.0);

            FlowPane row = new FlowPane(16.0, 16.0, filledField, outlinedField, passwordField, textArea);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 840.0, 180.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(840.0, 180.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, filledField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, outlinedField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, passwordField, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, textArea, Color.WHITE, 0.04);
            Region textAreaContent = lookupRegion(textArea, ".content");
            assertTextAreaContentBackgroundMatches(
                    image,
                    textAreaContent,
                    Color.rgb(230, 224, 233),
                    0.035,
                    "filled text area content background"
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-inputs.png"
            ));
            assertRenderedTextNodesStayInsideLayout(row);
            assertSingleLineTextInputsKeepInkAligned(row);
            assertFixedTargetControlsKeepCenteredContent(row);
        });
    }

    /// Verifies that text input layouts render supporting text, validation, errors, and character counters.
    @Tier2Test
    @Test
    void inputLayoutSnapshotRendersSupportingErrorAndCounterText() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField supportingField = new M3TextField("Alpha");
            supportingField.setPrefWidth(220.0);
            M3TextInputLayout supportingLayout = new M3TextInputLayout(supportingField);
            supportingLayout.setSupportingText("Supporting text");
            supportingLayout.setLabelText("Project");
            supportingLayout.setLeading(visualIcon("work"));
            supportingLayout.setPrefWidth(220.0);

            M3TextField counterField = createTextField("support@example.com", M3TextInputVariant.OUTLINED);
            counterField.setPrefWidth(320.0);
            M3TextInputLayout counterLayout = new M3TextInputLayout(counterField);
            counterLayout.setSupportingText("Email address");
            counterLayout.setLabelText("Email");
            counterLayout.setLeading(visualIcon("mail"));
            counterLayout.setClearButtonEnabled(true);
            counterLayout.setCharacterCounterVisible(true);
            counterLayout.setCharacterLimit(32);
            counterLayout.setPrefWidth(320.0);

            M3TextField errorField = createTextField("abcdef", M3TextInputVariant.OUTLINED);
            errorField.setPrefWidth(220.0);
            M3TextInputLayout errorLayout = new M3TextInputLayout(errorField);
            errorLayout.setSupportingText("Helper text");
            errorLayout.setLabelText("Code");
            errorLayout.setLeading(visualIcon("warning"));
            errorLayout.setCharacterCounterVisible(true);
            errorLayout.setCharacterLimit(4);
            errorLayout.setErrorText("Too long");
            errorLayout.setPrefWidth(220.0);

            M3TextField enforcedField = createTextField("Too many characters", M3TextInputVariant.OUTLINED);
            enforcedField.setPrefWidth(260.0);
            M3TextInputLayout enforcedLayout = new M3TextInputLayout(enforcedField);
            enforcedLayout.setSupportingText("Limit enforced");
            enforcedLayout.setLabelText("Limited");
            enforcedLayout.setCharacterCounterVisible(true);
            enforcedLayout.setCharacterLimit(8);
            enforcedLayout.setCharacterLimitEnforced(true);
            enforcedLayout.setPrefWidth(260.0);

            M3TextField validatedField = createTextField("support", M3TextInputVariant.OUTLINED);
            validatedField.setPrefWidth(260.0);
            M3TextInputLayout validatedLayout = new M3TextInputLayout(validatedField);
            validatedLayout.setSupportingText("Email format");
            validatedLayout.setLabelText("Validated");
            validatedLayout.setValidator((input, text) -> text.contains("@") ? null : "Use an email address");
            validatedLayout.validate();
            validatedLayout.setPrefWidth(260.0);

            FlowPane row = new FlowPane(
                    18.0,
                    18.0,
                    supportingLayout,
                    counterLayout,
                    errorLayout,
                    enforcedLayout,
                    validatedLayout
            );
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 1220.0, 220.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(1220.0, 220.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, supportingLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, counterLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, errorLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, enforcedLayout, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, validatedLayout, Color.WHITE, 0.04);
            assertTrue(errorField.isError());
            assertFalse(enforcedField.isError());
            assertTrue(validatedField.isError());
            assertEquals("Too many", enforcedField.getText());
            assertTrue(counterLayout.isLabelFloating());
            assertEquals(textInputClearButton(counterLayout), counterLayout.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));
            Text floatingCounterLabel = assertInstanceOf(
                    Text.class,
                    counterLayout.lookup("." + "m3-text-input-label")
            );
            var counterFieldBounds = counterField.localToScene(counterField.getBoundsInLocal());
            var floatingCounterLabelBounds = floatingCounterLabel.localToScene(floatingCounterLabel.getBoundsInLocal());
            assertTrue(
                    floatingCounterLabelBounds.getMinX() > counterFieldBounds.getMinX() + 32.0,
                    () -> "floating label background starts too far left: field="
                            + counterFieldBounds + ", label=" + floatingCounterLabelBounds
            );
            assertSnapshotAreaContainsContrast(
                    image,
                    (int) Math.floor(counterFieldBounds.getMinX() + 4.0),
                    (int) Math.floor(counterFieldBounds.getMinY()),
                    (int) Math.floor(floatingCounterLabelBounds.getMinX() - 2.0),
                    (int) Math.ceil(counterFieldBounds.getMinY() + 3.0),
                    Color.WHITE,
                    0.04,
                    "outlined text field leading top outline before floating label notch"
            );
            assertEquals("Too long", assertInstanceOf(
                    Label.class,
                    errorLayout.lookup("." + "m3-text-input-supporting-text")
            ).getText());
            assertEquals("Use an email address", assertInstanceOf(
                    Label.class,
                    validatedLayout.lookup("." + "m3-text-input-supporting-text")
            ).getText());
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-input-layouts.png"
            ));
            assertRenderedTextNodesStayInsideLayout(row);
            assertSingleLineTextInputsKeepInkAligned(row);
            assertTextAreasKeepInkInsideContentArea(row);
            assertVisualIconSlotsUseVectorGraphics(row);
            assertFixedTargetControlsKeepCenteredContent(row);
        });
    }

    /// Verifies that right-to-left text input layouts keep rendered ink, slots, counters, and notches stable.
    @Tier2Test
    @Test
    void inputLayoutSnapshotRendersRightToLeftStatesWithStableTextInk() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField filledField = new M3TextField("Filled value");
            filledField.setPrefWidth(240.0);
            M3TextInputLayout filledLayout = new M3TextInputLayout(filledField);
            filledLayout.setSupportingText("Filled supporting text");
            filledLayout.setLabelText("Filled label");
            filledLayout.setLeading(visualIcon("mail"));
            filledLayout.setClearButtonEnabled(true);
            filledLayout.setCharacterCounterVisible(true);
            filledLayout.setCharacterLimit(24);
            filledLayout.setPrefWidth(300.0);

            M3TextField outlinedField = createTextField("M3FX", M3TextInputVariant.OUTLINED);
            outlinedField.setPrefWidth(300.0);
            M3TextInputLayout outlinedLayout = new M3TextInputLayout(outlinedField);
            outlinedLayout.setSupportingText("Project name");
            outlinedLayout.setLabelText("Outlined with text");
            outlinedLayout.setLeading(visualIcon("text"));
            outlinedLayout.setClearButtonEnabled(true);
            outlinedLayout.setCharacterCounterVisible(true);
            outlinedLayout.setCharacterLimit(24);
            outlinedLayout.setPrefWidth(360.0);

            M3PasswordField passwordField = createPasswordField("secret", M3TextInputVariant.OUTLINED);
            passwordField.setPrefWidth(240.0);
            M3TextInputLayout passwordLayout = new M3TextInputLayout(passwordField);
            passwordLayout.setSupportingText("At least 8 characters");
            passwordLayout.setLabelText("Password");
            passwordLayout.setLeading(visualIcon("bookmark"));
            passwordLayout.setTrailing(new M3IconButton(visualIcon("info")));
            passwordLayout.setCharacterCounterVisible(true);
            passwordLayout.setCharacterLimit(32);
            passwordLayout.setPrefWidth(320.0);

            M3TextField errorField = createTextField("abcdef", M3TextInputVariant.OUTLINED);
            errorField.setPrefWidth(220.0);
            M3TextInputLayout errorLayout = new M3TextInputLayout(errorField);
            errorLayout.setSupportingText("Helper text");
            errorLayout.setLabelText("Error code");
            errorLayout.setLeading(visualIcon("warning"));
            errorLayout.setCharacterCounterVisible(true);
            errorLayout.setCharacterLimit(4);
            errorLayout.setErrorText("Too long");
            errorLayout.setPrefWidth(300.0);

            M3TextArea area = createTextArea("Multiline\ncontent", M3TextInputVariant.OUTLINED);
            area.setPrefSize(280.0, 104.0);
            M3TextInputLayout areaLayout = new M3TextInputLayout(area);
            areaLayout.setSupportingText("Detailed notes");
            areaLayout.setLabelText("Text area");
            areaLayout.setLeading(visualIcon("info"));
            areaLayout.setCharacterCounterVisible(true);
            areaLayout.setCharacterLimit(80);
            areaLayout.setPrefWidth(340.0);

            FlowPane row = new FlowPane(
                    18.0,
                    18.0,
                    filledLayout,
                    outlinedLayout,
                    passwordLayout,
                    errorLayout,
                    areaLayout
            );
            row.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            row.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 1060.0, 300.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(1060.0, 300.0);
            row.layout();

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, filledLayout.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, outlinedLayout.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, passwordLayout.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, errorLayout.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, areaLayout.getEffectiveNodeOrientation());
            assertTextInputLayoutUsesLogicalGeometry(outlinedLayout, true);
            assertTextInputLayoutUsesLogicalGeometry(passwordLayout, true);
            assertTextInputLayoutUsesLogicalGeometry(errorLayout, true);
            assertTextInputLayoutUsesLogicalGeometry(areaLayout, true);

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotNodeContainsContrast(image, filledLayout, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, outlinedField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, passwordField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, errorField, Color.WHITE, 0.04);
            assertSnapshotNodeBorderContainsContrast(image, area, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-input-layouts-rtl.png"
            ));
            assertRenderedTextNodesStayInsideLayout(row);
            assertSingleLineTextInputsKeepInkAligned(row);
            assertTextAreasKeepInkInsideContentArea(row);
            assertVisualIconSlotsUseVectorGraphics(row);
            assertFixedTargetControlsKeepCenteredContent(row);
        });
    }

    /// Verifies that a focused right-to-left outlined text area keeps content, notch, and adornments stable.
    @Tier2Test
    @Test
    void focusedRightToLeftOutlinedTextAreaSnapshotKeepsContentAndAdornmentGeometry() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextArea textArea = createTextArea(
                    "Material text areas share field colors\nbut keep multi-line height tokens.",
                    M3TextInputVariant.OUTLINED
            );
            textArea.setPrefSize(360.0, 168.0);

            M3TextInputLayout layout = new M3TextInputLayout(textArea);

            layout.setSupportingText("Outlined multi-line input");
            layout.setLabelText("RTL outlined text area");
            layout.setLeading(visualIcon("text"));
            layout.setTrailing(new M3IconButton(visualIcon("info")));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(96);
            layout.setPrefWidth(440.0);

            Color surface = Color.rgb(248, 240, 249);
            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 28px; " + visualTestColors());

            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 520.0, 280.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.resize(520.0, 280.0);
                root.layout();

                textArea.requestFocus();
                root.applyCss();
                root.layout();

                assertTrue(textArea.isFocused());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, layout.getEffectiveNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, textArea.getEffectiveNodeOrientation());
                assertTextInputLayoutUsesLogicalGeometry(layout, true);

                WritableImage image = snapshotImageOnFxThread(root);
                Region content = lookupRegion(textArea, ".content");
                assertTextAreaContentBackgroundMatches(
                        image,
                        content,
                        surface,
                        0.035,
                        "focused RTL outlined text area content background"
                );
                assertTextAreaTextInkInsideContentArea(
                        image,
                        textArea,
                        content,
                        "focused RTL outlined text area"
                );
                assertSnapshotNodeContainsContrast(image, layout, surface, 0.04);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-input-layouts-rtl-focused-text-area.png"
                ));
                assertRenderedTextNodesStayInsideLayout(root);
                assertVisualIconSlotsUseVectorGraphics(root);
                assertFixedTargetControlsKeepCenteredContent(root);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that outlined text input layouts cut a real outline notch instead of painting a label mask.
    @Tier2Test
    @Test
    void outlinedInputLayoutSnapshotRendersNotchedOutlineWithoutLabelMask() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField textField = createTextField("M3FX", M3TextInputVariant.OUTLINED);
            textField.setPrefWidth(360.0);

            M3TextInputLayout layout = new M3TextInputLayout(textField);

            layout.setSupportingText("Project name");
            layout.setLabelText("Outlined with text");
            layout.setLeading(visualIcon("text"));
            layout.setCharacterCounterVisible(true);
            layout.setCharacterLimit(24);
            layout.setPrefWidth(360.0);

            Color surface = Color.rgb(248, 240, 249);
            StackPane root = new StackPane(layout);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 140.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(430.0, 140.0);
            root.layout();

            assertVisualIconSlotsUseVectorGraphics(root);

            WritableImage image = snapshotImageOnFxThread(root);
            Text label = assertInstanceOf(Text.class, layout.lookup("." + "m3-text-input-label"));
            Path outline = assertInstanceOf(Path.class, layout.lookup("." + "m3-text-input-outline"));
            Text inputText = renderedTextNode(textField, "M3FX");
            var labelBounds = label.localToScene(label.getBoundsInLocal());
            var fieldBounds = textField.localToScene(textField.getBoundsInLocal());
            var inputTextBounds = inputText.localToScene(inputText.getBoundsInLocal());
            Rectangle2D inputInkBounds = requiredRenderedTextInkBounds(image, inputText);
            double fieldCenterY = (fieldBounds.getMinY() + fieldBounds.getMaxY()) / 2.0;
            double inputTextCenterY = inputTextBounds.getCenterY();

            assertEquals(11.0, textField.getPadding().getTop(), 0.0001);
            assertEquals(fieldCenterY, inputTextCenterY, 4.0,
                    () -> "outlined input line box should stay vertically centered: field="
                            + fieldBounds + ", text=" + inputTextBounds + ", ink=" + inputInkBounds);
            assertNull(label.getClip());
            assertTrue(outlineNotchGap(outline) >= labelBounds.getWidth() - 1.0,
                    () -> "outline gap is narrower than the floating label: gap="
                            + outlineNotchGap(outline) + ", label=" + labelBounds);
            assertTrue(labelBounds.getMinY() < fieldBounds.getMinY(),
                    () -> "floating label should start above the outline top: field="
                            + fieldBounds + ", label=" + labelBounds);
            assertTrue(labelBounds.getMaxY() > fieldBounds.getMinY(),
                    () -> "floating label should straddle the outline top: field="
                            + fieldBounds + ", label=" + labelBounds);
            assertTrue(Math.abs(labelBounds.getCenterY() - fieldBounds.getMinY()) < 6.0,
                    () -> "floating label should be centered around the outline top: field="
                            + fieldBounds + ", label=" + labelBounds);

            Color leadingNotchPixel = averageSnapshotColor(
                    image,
                    (int) Math.floor(labelBounds.getMinX() - 3.0),
                    (int) Math.floor(fieldBounds.getMinY()),
                    (int) Math.floor(labelBounds.getMinX() - 1.0),
                    (int) Math.floor(fieldBounds.getMinY() + 3.0)
            );
            assertColorNear(leadingNotchPixel, surface, 0.05, "outlined notch leading clearance");
            assertSnapshotAreaContainsContrast(
                    image,
                    (int) Math.floor(fieldBounds.getMinX() + 4.0),
                    (int) Math.floor(fieldBounds.getMinY()),
                    (int) Math.floor(labelBounds.getMinX() - 2.0),
                    (int) Math.ceil(fieldBounds.getMinY() + 3.0),
                    surface,
                    0.04,
                    "outlined text field top outline before floating label notch"
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-outlined-input-notch.png"
            ));
            assertSingleLineTextInputsKeepInkAligned(root);
        });
    }

    /// Shows an outlined text input layout configured for observable presentation animations.
    private static void showObservableTextInputLayout(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Pane> rootReference,
            AtomicReference<@Nullable M3TextInputLayout> layoutReference,
            boolean showSupportingText
    ) {
        M3TextField textField = createTextField("", M3TextInputVariant.OUTLINED);
        textField.setPrefWidth(260.0);
        M3TextInputLayout layout = new M3TextInputLayout(textField);
        layout.setLabelText("Email");
        layout.setClearButtonEnabled(true);
        layout.setPrefWidth(260.0);

        Pane root = new Pane(layout);
        layout.resizeRelocate(20.0, 20.0, 260.0, 96.0);
        Scene scene = new Scene(root, 320.0, 140.0);
        Stage stage = new Stage();

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.setCssAnimationsEnabled(root, true);
        FxTestUtils.setMotionScheme(root, observableTestMotionScheme());
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();

        Path outline = assertInstanceOf(Path.class, layout.lookup("." + "m3-text-input-outline"));
        assertEquals(0.0, outlineNotchGap(outline), 0.0001);

        stageReference.set(stage);
        rootReference.set(root);
        layoutReference.set(layout);
        textField.setText("alpha");
        if (showSupportingText) {
            layout.setSupportingText("Helper text");
        }
    }

    /// Returns whether a text input layout is rendering a floating-label intermediate frame.
    private static boolean textInputLayoutFloatingPresentationIsAnimating(
            AtomicReference<@Nullable M3TextInputLayout> layoutReference
    ) {
        @Nullable M3TextInputLayout layout = layoutReference.get();
        if (layout == null) {
            return false;
        }

        layout.applyCss();
        layout.layout();
        @Nullable Node labelNode = layout.lookup("." + "m3-text-input-label");
        @Nullable Node outlineNode = layout.lookup("." + "m3-text-input-outline");
        if (!(labelNode instanceof Text label) || !(outlineNode instanceof Path outline)) {
            return false;
        }

        M3IconButton clearButton = textInputClearButton(layout);
        double expandedScale = textInputLayoutExpandedLabelScale(layout, label);
        double lowerScale = Math.min(1.0, expandedScale);
        double upperScale = Math.max(1.0, expandedScale);
        return Double.compare(label.getOpacity(), 1.0) == 0
                && isBetweenExclusive(renderedScaleX(label), lowerScale, upperScale)
                && outlineNotchGap(outline) > 0.5
                && isBetweenExclusive(clearButton.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(clearButton.getScaleX(), 0.86, 1.0)
                && isBetweenExclusive(clearButton.getScaleY(), 0.86, 1.0);
    }

    /// Returns whether a text input layout is rendering a supporting-row intermediate frame.
    private static boolean textInputLayoutSupportingRowPresentationIsAnimating(
            AtomicReference<@Nullable M3TextInputLayout> layoutReference
    ) {
        @Nullable M3TextInputLayout layout = layoutReference.get();
        if (layout == null) {
            return false;
        }

        layout.applyCss();
        layout.layout();
        @Nullable Node supportingRowNode = layout.lookup("." + "m3-text-input-supporting-row");
        if (!(supportingRowNode instanceof HBox supportingRow)) {
            return false;
        }

        return isBetweenExclusive(supportingRow.getOpacity(), 0.0, 1.0)
                && isBetweenExclusive(Math.abs(supportingRow.getTranslateY()), 0.0, 4.0);
    }

    /// Returns whether all text input presentation channels are rendering intermediate frames.
    private static boolean textInputLayoutFullPresentationIsAnimating(
            AtomicReference<@Nullable M3TextInputLayout> layoutReference
    ) {
        return textInputLayoutFloatingPresentationIsAnimating(layoutReference)
                && textInputLayoutSupportingRowPresentationIsAnimating(layoutReference);
    }

    /// Verifies a floating-label and clear-button intermediate frame on a text input layout.
    private static void assertTextInputLayoutFloatingPresentationIntermediate(M3TextInputLayout layout) {
        Text label = assertInstanceOf(Text.class, layout.lookup("." + "m3-text-input-label"));
        Path outline = assertInstanceOf(Path.class, layout.lookup("." + "m3-text-input-outline"));
        M3IconButton clearButton = textInputClearButton(layout);
        double expandedScale = textInputLayoutExpandedLabelScale(layout, label);

        assertEquals(1.0, label.getOpacity(), 0.0001);
        assertBetween(
                renderedScaleX(label),
                Math.min(1.0, expandedScale),
                Math.max(1.0, expandedScale),
                "floating label scale"
        );
        assertTrue(outlineNotchGap(outline) > 0.5, () -> "outlineNotchGap=" + outlineNotchGap(outline));
        assertBetween(clearButton.getOpacity(), 0.0, 1.0, "clear button opacity");
        assertBetween(clearButton.getScaleX(), 0.86, 1.0, "clear button scaleX");
        assertBetween(clearButton.getScaleY(), 0.86, 1.0, "clear button scaleY");
    }

    /// Verifies a supporting-row intermediate frame on a text input layout.
    private static void assertTextInputLayoutSupportingRowPresentationIntermediate(M3TextInputLayout layout) {
        HBox supportingRow = assertInstanceOf(
                HBox.class,
                layout.lookup("." + "m3-text-input-supporting-row")
        );

        assertBetween(supportingRow.getOpacity(), 0.0, 1.0, "supporting row opacity");
        assertBetween(Math.abs(supportingRow.getTranslateY()), 0.0, 4.0, "supporting row translateY");
    }

    /// Verifies that text input presentation state is settled after motion is disabled.
    private static void assertTextInputLayoutPresentationSettled(M3TextInputLayout layout) {
        Text label = assertInstanceOf(Text.class, layout.lookup("." + "m3-text-input-label"));
        Path outline = assertInstanceOf(Path.class, layout.lookup("." + "m3-text-input-outline"));
        M3IconButton clearButton = textInputClearButton(layout);
        HBox supportingRow = assertInstanceOf(
                HBox.class,
                layout.lookup("." + "m3-text-input-supporting-row")
        );

        assertEquals(1.0, label.getOpacity(), 0.0001);
        assertEquals(1.0, renderedScaleX(label), 0.0001);
        assertTrue(outlineNotchGap(outline) > 0.5, () -> "outlineNotchGap=" + outlineNotchGap(outline));
        assertEquals(1.0, clearButton.getOpacity(), 0.0001);
        assertEquals(1.0, clearButton.getScaleX(), 0.0001);
        assertEquals(1.0, clearButton.getScaleY(), 0.0001);
        assertEquals(1.0, supportingRow.getOpacity(), 0.0001);
        assertEquals(0.0, supportingRow.getTranslateY(), 0.0001);
    }

    /// Returns whether a trailing icon button is rendering a visible ripple frame.
    private static boolean trailingIconRippleIsVisible(AtomicReference<@Nullable M3IconButton> buttonReference) {
        @Nullable M3IconButton button = buttonReference.get();
        if (button == null) {
            return false;
        }

        button.applyCss();
        button.layout();
        @Nullable Node rippleNode = button.lookup(".m3-ripple");
        if (!(rippleNode instanceof Region ripple)) {
            return false;
        }

        return ripple.getOpacity() > 0.0
                && ripple.getScaleX() > 0.0
                && ripple.getScaleY() > 0.0;
    }

    /// Returns the horizontal scale represented by a node's rendered local-to-parent transform.
    private static double renderedScaleX(Node node) {
        var transform = node.getLocalToParentTransform();
        return Math.hypot(transform.getMxx(), transform.getMyx());
    }

    /// Returns the resting label scale derived from the current computed input and label typography.
    private static double textInputLayoutExpandedLabelScale(M3TextInputLayout layout, Text label) {
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");
        return input.getFont().getSize() / label.getFont().getSize();
    }

    /// Verifies the first bottom border stroke color for a region.
    private static void assertBorderBottomColor(Region region, Color expectedColor) {
        assertEquals(1, region.getBorder().getStrokes().size());
        assertEquals(expectedColor, region.getBorder().getStrokes().get(0).getBottomStroke());
    }

    /// Verifies that an already visible supporting row is not in an entry-transition frame.
    private static void assertVisibleSupportingRowIsStable(HBox supportingRow) {
        assertTrue(supportingRow.isVisible());
        assertTrue(supportingRow.isManaged());
        assertEquals(1.0, supportingRow.getOpacity(), 0.0001);
        assertEquals(0.0, supportingRow.getTranslateY(), 0.0001);
    }

    /// Verifies that single-line text inputs keep entered text ink in the active content slot.
    private static void assertSingleLineTextInputsKeepInkAligned(Node root) {
        WritableImage image = snapshotImageOnFxThread(root);
        visitVisibleNodes(root, node -> {
            if (!(node instanceof TextInputControl input)
                    || input instanceof M3TextArea
                    || !(input instanceof M3TextInput textInput)
                    || input.getText().isBlank()
                    || !hasRenderableBounds(input)) {
                return;
            }

            @Nullable Text text = firstRenderedText(input);
            assertTrue(text != null, () -> "single-line text input has no rendered text node: input=" + input);
            Text renderedText = Objects.requireNonNull(text);
            Bounds inputBounds = input.localToScene(input.getBoundsInLocal());
            Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, renderedText);
            assertRectangleInsideNodeBounds(input, inkBounds, TEXT_INK_EDGE_TOLERANCE,
                    "single-line text input rendered ink leaves the editable field");
            Bounds textBounds = renderedText.localToScene(renderedText.getBoundsInLocal());
            double textCenterY = textBounds.getCenterY();
            @Nullable M3TextInputLayout layout = nearestTextInputLayout(input);
            if (layout != null) {
                assertTextInputInkAvoidsAdornmentSlot(
                        layout,
                        "m3-text-input-leading",
                        inkBounds,
                        "single-line text input leading adornment slot"
                );
                assertTextInputInkAvoidsAdornmentSlot(
                        layout,
                        "m3-text-input-trailing",
                        inkBounds,
                        "single-line text input trailing adornment slot"
                );
            }
            if (layout != null && layout.isLabelFloating() && textInput.getVariant() == M3TextInputVariant.FILLED) {
                double centerRatio = (textCenterY - inputBounds.getMinY()) / inputBounds.getHeight();
                assertTrue(centerRatio >= FILLED_FLOATING_INK_MINIMUM_CENTER_RATIO
                                && centerRatio <= FILLED_FLOATING_INK_MAXIMUM_CENTER_RATIO,
                        () -> "filled floating-label input rendered text ink is outside its content slot: input="
                                + inputBounds + ", text=" + textBounds + ", ink=" + inkBounds + ", centerRatio=" + centerRatio);
                return;
            }
            if (layout != null && layout.isLabelFloating() && textInput.getVariant() == M3TextInputVariant.OUTLINED) {
                double centerRatio = (textCenterY - inputBounds.getMinY()) / inputBounds.getHeight();
                assertTrue(centerRatio >= OUTLINED_FLOATING_INK_MINIMUM_CENTER_RATIO
                                && centerRatio <= OUTLINED_FLOATING_INK_MAXIMUM_CENTER_RATIO,
                        () -> "outlined floating-label input rendered text ink is outside its content slot: input="
                                + inputBounds + ", text=" + textBounds + ", ink=" + inkBounds + ", centerRatio=" + centerRatio);
                return;
            }

            double inputCenterY = (inputBounds.getMinY() + inputBounds.getMaxY()) / 2.0;
            assertEquals(inputCenterY, textCenterY, SINGLE_LINE_TEXT_INPUT_INK_CENTER_TOLERANCE,
                    () -> "single-line input rendered text ink is vertically misaligned: input="
                            + inputBounds + ", text=" + textBounds + ", ink=" + inkBounds + ", textInput=" + input);
        });
    }

    /// Verifies that visible text areas keep rendered text ink inside their multiline content region.
    private static void assertTextAreasKeepInkInsideContentArea(Node root) {
        WritableImage image = snapshotImageOnFxThread(root);
        visitVisibleNodes(root, node -> {
            if (node instanceof M3TextArea textArea && !textArea.getText().isBlank() && hasRenderableBounds(textArea)) {
                Region content = lookupRegion(textArea, ".content");
                assertTextAreaTextInkInsideContentArea(image, textArea, content, "visible text area");
            }
        });
    }

    /// Verifies that one text area's rendered text ink stays inside JavaFX's multiline content region.
    private static void assertTextAreaTextInkInsideContentArea(
            WritableImage image,
            M3TextArea textArea,
            Region content,
            String description
    ) {
        java.util.ArrayList<Rectangle2D> inkBoundsList = new java.util.ArrayList<>();
        visitVisibleNodes(textArea, node -> {
            if (!(node instanceof Text text) || text.getText().isBlank() || !hasRenderableBounds(text)) {
                return;
            }

            @Nullable Rectangle2D inkBounds = renderedTextInkBounds(image, text);
            if (inkBounds == null) {
                return;
            }

            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            assertRenderedTextInkHasVerticalRoom(text, textBounds, inkBounds);
            assertRenderedTextInkHasExpectedHeight(text, textBounds, inkBounds);
            assertRectangleInsideNodeBounds(
                    content,
                    inkBounds,
                    TEXT_AREA_CONTENT_INK_EDGE_TOLERANCE,
                    description + " rendered ink leaves the multiline content area"
            );
            inkBoundsList.add(inkBounds);
        });

        assertFalse(inkBoundsList.isEmpty(),
                () -> description + " has no rendered text ink: text=" + textArea.getText());
    }

    /// Verifies that rendered text ink does not overlap a visible Material adornment slot.
    private static void assertTextInputInkAvoidsAdornmentSlot(
            M3TextInputLayout layout,
            String slotStyleClass,
            Rectangle2D inkBounds,
            String description
    ) {
        @Nullable Node slot = layout.lookup("." + slotStyleClass);
        if (slot == null || !slot.isVisible() || !hasRenderableBounds(slot)) {
            return;
        }

        Bounds slotBounds = slot.localToScene(slot.getBoundsInLocal());
        double verticalOverlap = Math.min(inkBounds.getMaxY(), slotBounds.getMaxY())
                - Math.max(inkBounds.getMinY(), slotBounds.getMinY());
        if (verticalOverlap <= TEXT_INPUT_ADORNMENT_OVERLAP_TOLERANCE) {
            return;
        }

        double horizontalOverlap = Math.min(inkBounds.getMaxX(), slotBounds.getMaxX())
                - Math.max(inkBounds.getMinX(), slotBounds.getMinX());
        assertTrue(horizontalOverlap <= TEXT_INPUT_ADORNMENT_OVERLAP_TOLERANCE,
                () -> description + " overlaps rendered text ink: overlap=" + horizontalOverlap
                        + ", inkBounds=" + inkBounds + ", slotBounds=" + slotBounds
                        + ", input=" + textInputDebugState(layout.getInput()));
    }

    /// Returns diagnostic state for a text input layout input.
    private static String textInputDebugState(@Nullable TextInputControl input) {
        if (input == null) {
            return "null";
        }

        String alignment = input instanceof TextField textField ? String.valueOf(textField.getAlignment()) : "n/a";
        return input.getClass().getSimpleName()
                + "[text=" + input.getText()
                + ", nodeOrientation=" + input.getNodeOrientation()
                + ", effectiveNodeOrientation=" + input.getEffectiveNodeOrientation()
                + ", alignment=" + alignment
                + ", padding=" + input.getPadding() + "]";
    }

    /// Verifies that the lower content area of a text area uses the Material container background.
    private static void assertTextAreaContentBackgroundMatches(
            WritableImage image,
            Region content,
            Color expected,
            double maximumDistance,
            String description
    ) {
        Bounds bounds = content.localToScene(content.getBoundsInLocal());
        double minX = Math.max(0.0, bounds.getMinX() + 8.0);
        double maxX = Math.min(image.getWidth() - 1.0, bounds.getMaxX() - 8.0);
        double minY = Math.max(0.0, bounds.getMinY() + Math.max(24.0, bounds.getHeight() * 0.45));
        double maxY = Math.min(image.getHeight() - 1.0, bounds.getMaxY() - 8.0);

        assertTrue(minX <= maxX && minY <= maxY,
                () -> description + " has no sampleable content area: bounds=" + bounds);

        int mismatches = 0;
        int whiteLeaks = 0;
        int ignoredInkPixels = 0;
        double worstDistance = 0.0;
        Color worstColor = expected;
        int worstX = -1;
        int worstY = -1;

        for (int yIndex = 0; yIndex < 3; yIndex++) {
            double y = minY + (maxY - minY) * yIndex / 2.0;
            for (int xIndex = 0; xIndex < 5; xIndex++) {
                double x = minX + (maxX - minX) * xIndex / 4.0;
                int pixelX = (int) Math.round(x);
                int pixelY = (int) Math.round(y);
                Color color = image.getPixelReader().getColor(pixelX, pixelY);
                double distance = colorDistance(color, expected);
                if (distance > worstDistance) {
                    worstDistance = distance;
                    worstColor = color;
                    worstX = pixelX;
                    worstY = pixelY;
                }
                if (colorDistance(color, Color.WHITE) <= 0.02) {
                    whiteLeaks++;
                } else if (distance > maximumDistance) {
                    if (color.getBrightness() < 0.72) {
                        ignoredInkPixels++;
                    } else {
                        mismatches++;
                    }
                }
            }
        }

        String mismatchMessage = description + " sampled non-container pixels: mismatches=" + mismatches
                + ", worstColor=" + worstColor
                + ", ignoredInkPixels=" + ignoredInkPixels
                + ", worstDistance=" + worstDistance
                + ", worstPixel=(" + worstX + ", " + worstY + ")"
                + ", expected=" + expected
                + ", bounds=" + bounds;
        String whiteLeakMessage = description + " exposes JavaFX white content background: whiteLeaks=" + whiteLeaks
                + ", worstColor=" + worstColor
                + ", bounds=" + bounds;
        assertEquals(0, mismatches, mismatchMessage);
        assertEquals(0, whiteLeaks, whiteLeakMessage);
    }

    /// Verifies that one text input CSS metadata list exposes the expected Material metric tokens.
    private static void assertTextInputCssMetaData(List<CssMetaData<? extends Styleable, ?>> cssMetaData) {
        assertCssProperty(cssMetaData, "-m3-container-height");
        assertCssProperty(cssMetaData, "-m3-container-shape");
        assertCssProperty(cssMetaData, "-m3-horizontal-padding");
        assertCssProperty(cssMetaData, "-m3-vertical-padding");
    }
}
