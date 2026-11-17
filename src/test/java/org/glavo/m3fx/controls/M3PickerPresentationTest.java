// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.testing.Tier2Test;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.glavo.m3fx.controls.ControlMotionTestUtils.*;
import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies picker fields, popup presentation, and locale-dependent geometry.
@NotNullByDefault
final class M3PickerPresentationTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that calendar navigation buttons use vector chevron glyphs.
    @Test
    void datePickerNavigationButtonsUseInternalVectorIcons() {
        M3DatePicker datePicker = new M3DatePicker();
        M3DateRangePicker dateRangePicker = new M3DateRangePicker();

        applyCss(datePicker);
        applyCss(dateRangePicker);

        assertNavigationGlyphs(datePicker);
        assertNavigationGlyphs(dateRangePicker);

        datePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        dateRangePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        applyCss(datePicker);
        applyCss(dateRangePicker);

        assertNavigationGlyphs(datePicker);
        assertNavigationGlyphs(dateRangePicker);
    }

    /// Verifies that RTL picker fields keep editor ink away from the trailing open button in a shown window.
    @Test
    void rightToLeftPickerFieldSeparatesEditorInkAndOpenButtonInWindow() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 27));
            field.getEditor().setVariant(M3TextInputVariant.OUTLINED);
            field.getInputLayout().setLabelText("RTL date");
            field.getInputLayout().setSupportingText("RTL project date");
            field.getInputLayout().setCharacterCounterVisible(true);
            field.getInputLayout().setCharacterLimit(10);
            field.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            field.setPrefWidth(360.0);

            StackPane root = new StackPane(field);
            root.setAlignment(Pos.TOP_LEFT);
            root.setStyle("-fx-background-color: rgb(248, 240, 249); -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 430.0, 150.0);
            Stage stage = new Stage();

            try {
                stage.setScene(scene);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                FxTestUtils.setCssAnimationsEnabled(root, false);
                stage.show();
                field.getEditor().requestFocus();
                field.getEditor().positionCaret(field.getEditor().getText().length());
                root.applyCss();
                root.resize(430.0, 150.0);
                root.layout();
                field.getInputLayout().layout();

                M3TextInputLayout layout = field.getInputLayout();
                M3TextField editor = field.getEditor();
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, layout.getEffectiveNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, editor.getEffectiveNodeOrientation());
                assertTextInputLayoutUsesLogicalGeometry(layout, true);
                assertEquals(48.0, editor.getPadding().getRight(), 0.0001);
                assertEquals(16.0, editor.getPadding().getLeft(), 0.0001);

                Text label = assertInstanceOf(
                        Text.class,
                        layout.lookup("." + "m3-text-input-label")
                );
                Text inputText = renderedTextNode(editor, "2026-06-27");
                Node openButton = assertInstanceOf(
                        Node.class,
                        layout.lookup("." + M3PickerField.OPEN_BUTTON_STYLE_CLASS)
                );

                Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
                Bounds openButtonBounds = openButton.localToScene(openButton.getBoundsInLocal());
                WritableImage image = snapshotImageOnFxThread(root);
                Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, inputText);

                assertTrue(inkBounds.getMinX() >= openButtonBounds.getMaxX() + 3.0,
                        () -> "RTL picker editor ink overlaps open button: ink="
                                + inkBounds + ", openButton=" + openButtonBounds
                                + ", editorPadding=" + editor.getPadding());
                assertTrue(labelBounds.getMinX() >= openButtonBounds.getMaxX() + 1.0,
                        () -> "RTL picker floating label overlaps open button: label="
                                + labelBounds + ", openButton=" + openButtonBounds);
                assertSnapshotNodeContainsContrast(image, inputText, Color.WHITE, 0.04);
                writeVisualSnapshot(image, java.nio.file.Path.of(
                        "build",
                        "reports",
                        "m3fx-visual",
                        "visual-picker-field-rtl-open-button-separation.png"
                ));
            } finally {
                FxTestUtils.setCssAnimationsEnabled(root, true);
                stage.close();
            }
        });
    }

    /// Verifies that an already open picker-field popup follows runtime scene theme changes.
    @Test
    void pickerFieldPopupReinheritsRuntimeSceneThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField field = new M3DatePickerField(LocalDate.of(2026, 6, 15));
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#2d6b27"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();
            try {
                Pane root = new Pane(field);
                Scene scene = new Scene(root, 520.0, 420.0);
                M3ThemeManager.install(scene, baselineTheme);
                stage.setScene(scene);
                stage.show();
                field.resizeRelocate(24.0, 24.0, 260.0, 64.0);
                root.applyCss();
                root.layout();

                field.showPicker();
                Parent popupSurface = Objects.requireNonNull(pickerPopupSurface(field), "popupSurface");

                assertSame(baselineTheme, M3ThemeManager.getTheme(popupSurface));

                M3ThemeManager.install(scene, expressiveDarkTheme);

                assertSame(expressiveDarkTheme, M3ThemeManager.getTheme(popupSurface));
                assertTrue(popupSurface.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupSurface.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(popupSurface.getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)
                ));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that an already open date-range picker-field popup follows runtime scene theme changes.
    @Test
    void dateRangePickerFieldPopupReinheritsRuntimeSceneThemeChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 6, 15),
                    LocalDate.of(2026, 6, 20)
            );
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                    Color.web("#8c4a5f"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.DARK
            );
            Stage stage = new Stage();
            try {
                Pane root = new Pane(field);
                Scene scene = new Scene(root, 680.0, 480.0);
                M3ThemeManager.install(scene, baselineTheme);
                stage.setScene(scene);
                stage.show();
                field.resizeRelocate(24.0, 24.0, 520.0, 72.0);
                root.applyCss();
                root.layout();

                field.showPicker();
                Parent popupSurface = Objects.requireNonNull(pickerPopupSurface(field), "popupSurface");

                assertSame(baselineTheme, M3ThemeManager.getTheme(popupSurface));

                M3ThemeManager.install(scene, expressiveDarkTheme);

                assertSame(expressiveDarkTheme, M3ThemeManager.getTheme(popupSurface));
                assertTrue(popupSurface.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
                assertTrue(popupSurface.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
                assertTrue(popupSurface.getStylesheets().contains(
                        M3ThemeRuntime.themeStylesheetUrl(expressiveDarkTheme)
                ));
            } finally {
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that picker skins stop exposing cells hidden by ancestor containers.
    @Test
    void pickerAccessibleCellsSkipAncestorHiddenTargets() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));
            timePicker.setUse24HourClock(true);
            timePicker.setMinuteStep(15);

            VBox hiddenAncestor = new VBox(12.0, datePicker, dateRangePicker, timePicker);
            Scene scene = new Scene(hiddenAncestor, 620.0, 760.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            hiddenAncestor.applyCss();
            hiddenAncestor.layout();

            assertTrue((int) datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT) > 0);
            assertTrue((int) dateRangePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT) > 0);
            assertTrue((int) timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT) > 0);

            hiddenAncestor.setVisible(false);

            assertEquals(0, datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertEquals(0, dateRangePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertEquals(0, timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertNull(datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertNull(dateRangePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertNull(timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
        });
    }

    /// Verifies that hidden picker field owners do not route accessibility focus or open popups.
    @Test
    void pickerFieldsSkipAccessibleFocusWhenOwnerHiddenByAncestor() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 18));
            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            M3Button outside = new M3Button("Outside");
            VBox hiddenOwner = new VBox(8.0, dateField, rangeField);
            VBox root = new VBox(8.0, outside, hiddenOwner);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 560.0, 260.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                outside.requestFocus();
                hiddenOwner.setVisible(false);
                root.applyCss();
                root.layout();

                dateField.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                dateField.executeAccessibleAction(AccessibleAction.EXPAND);
                dateField.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(outside.isFocused());
                assertFalse(dateField.getEditor().isFocused());
                assertFalse(dateField.isShowing());

                rangeField.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                rangeField.executeAccessibleAction(AccessibleAction.EXPAND);
                rangeField.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(outside.isFocused());
                assertFalse(rangeField.getStartEditor().isFocused());
                assertFalse(rangeField.getEndEditor().isFocused());
                assertFalse(rangeField.isShowing());
            } finally {
                dateField.hidePicker();
                rangeField.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that picker skins propagate right-to-left orientation into their rendered layout nodes.
    @Test
    void pickerSkinsPropagateRightToLeftOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            datePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3DateRangePicker dateRangePicker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            dateRangePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));
            timePicker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            timePicker.setPrefSize(608.0, 304.0);

            M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 18));
            dateField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            dateField.getPresets().setAll(M3DatePresets.common(LocalDate.of(2026, 5, 18)));
            M3DateRangePickerField rangeField = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2026, 5, 16)
            );
            rangeField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            rangeField.getPresets().setAll(M3DateRangePresets.common(LocalDate.of(2026, 5, 18), rangeField.getPicker().getFirstDayOfWeek()));
            M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
            timeField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            timeField.getPresets().setAll(M3TimePresets.common(LocalTime.of(10, 30)));

            Pane root = new Pane(datePicker, dateRangePicker, timePicker, dateField, rangeField, timeField);
            Scene scene = new Scene(root, 1320.0, 760.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(1320.0, 760.0);
            datePicker.resizeRelocate(20.0, 20.0, 340.0, 320.0);
            dateRangePicker.resizeRelocate(390.0, 20.0, 340.0, 320.0);
            timePicker.resizeRelocate(20.0, 360.0, 608.0, 304.0);
            dateField.resizeRelocate(760.0, 360.0, 420.0, 96.0);
            rangeField.resizeRelocate(760.0, 460.0, 540.0, 96.0);
            timeField.resizeRelocate(760.0, 560.0, 420.0, 96.0);
            root.layout();
            datePicker.layout();
            dateRangePicker.layout();
            timePicker.layout();

            HBox dateHeader = assertInstanceOf(
                    HBox.class,
                    datePicker.lookup("." + "m3-date-picker-header")
            );
            HBox dateWeekdays = assertInstanceOf(
                    HBox.class,
                    datePicker.lookup("." + "m3-date-picker-weekday-row")
            );
            HBox rangeHeader = assertInstanceOf(
                    HBox.class,
                    dateRangePicker.lookup("." + "m3-date-picker-header")
            );
            HBox timeDisplay = assertInstanceOf(
                    HBox.class,
                    timePicker.lookup("." + "m3-time-picker-display")
            );

            assertEquals(NodeOrientation.RIGHT_TO_LEFT, dateHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, dateWeekdays.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, rangeHeader.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, timeDisplay.getEffectiveNodeOrientation());
            assertEquals(Pos.CENTER_LEFT, dateHeader.getAlignment());
            assertEquals(Pos.CENTER_LEFT, dateWeekdays.getAlignment());
            assertEquals(Pos.CENTER_LEFT, rangeHeader.getAlignment());
            assertEquals(Pos.CENTER_LEFT, timeDisplay.getAlignment());
            HBox dateMonthSection = assertInstanceOf(HBox.class, dateHeader.getChildren().get(0));
            M3IconButton previousMonthButton = assertInstanceOf(
                    M3IconButton.class,
                    dateMonthSection.getChildren().get(0)
            );
            M3IconButton nextMonthButton = assertInstanceOf(
                    M3IconButton.class,
                    dateMonthSection.getChildren().get(2)
            );
            assertEquals(
                    M3InternalIcon.Glyph.CHEVRON_RIGHT,
                    assertInstanceOf(M3InternalIcon.class, previousMonthButton.getGraphic()).getGlyph()
            );
            assertEquals(
                    M3InternalIcon.Glyph.CHEVRON_LEFT,
                    assertInstanceOf(M3InternalIcon.class, nextMonthButton.getGraphic()).getGlyph()
            );
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, dateField.getPicker().getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, rangeField.getPicker().getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, timeField.getPicker().getEffectiveNodeOrientation());
            assertPickerFieldPresetContentUsesLogicalStart(
                    assertInstanceOf(Node.class, dateField.getPicker().getParent()),
                    "m3-date-picker-field-preset-list"
            );
            assertPickerFieldPresetContentUsesLogicalStart(
                    assertInstanceOf(Node.class, rangeField.getPicker().getParent()),
                    "m3-date-range-picker-field-preset-list"
            );
            assertPickerFieldPresetContentUsesLogicalStart(
                    assertInstanceOf(Node.class, timeField.getPicker().getParent()),
                    "m3-time-picker-field-preset-list"
            );

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, dateRangePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, timePicker, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-pickers-rtl.png"
            ));
        });
    }

    /// Verifies that date range dialog presets render beside the picker.
    @Tier2Test
    @Test
    void dateRangePresetDialogSnapshotRendersPresetColumn() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerDialog dialog = new M3DateRangePickerDialog();
            dialog.getPresets().setAll(M3DateRangePresets.common(
                    LocalDate.of(2026, 5, 19),
                    dialog.getPicker().getFirstDayOfWeek()
            ));
            M3DialogPane pane = dialog.getDialogPane();
            pane.setPrefWidth(660.0);

            StackPane root = new StackPane(pane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 720.0, 600.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(720.0, 600.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, pane, Color.WHITE, 0.04);
            assertEquals(6, pane.lookupAll("." + "m3-date-range-picker-dialog-preset-button").size());
            assertSnapshotNodeContainsContrast(
                    image,
                    assertInstanceOf(Node.class, pane.lookup("." + "m3-date-range-picker-dialog-preset-list")),
                    Color.WHITE,
                    0.04
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-range-presets.png"
            ));
        });
    }

    /// Verifies that single-date dialog presets render beside the picker.
    @Tier2Test
    @Test
    void datePresetDialogSnapshotRendersPresetColumn() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerDialog dialog = new M3DatePickerDialog();
            dialog.getPresets().setAll(M3DatePresets.common(LocalDate.of(2026, 5, 19)));
            M3DialogPane pane = dialog.getDialogPane();
            pane.setPrefWidth(620.0);

            StackPane root = new StackPane(pane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 680.0, 600.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(680.0, 600.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, pane, Color.WHITE, 0.04);
            assertEquals(5, pane.lookupAll("." + "m3-date-picker-dialog-preset-button").size());
            assertSnapshotNodeContainsContrast(
                    image,
                    assertInstanceOf(Node.class, pane.lookup("." + "m3-date-picker-dialog-preset-list")),
                    Color.WHITE,
                    0.04
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-presets.png"
            ));
        });
    }

    /// Verifies that time dialog presets render in a wrapping row above the picker.
    @Tier2Test
    @Test
    void timePresetDialogSnapshotRendersPresetRow() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePickerDialog dialog = new M3TimePickerDialog(LocalTime.of(10, 30));
            dialog.getPicker().setUse24HourClock(true);
            dialog.getPicker().setMinuteStep(15);
            dialog.getPresets().setAll(M3TimePresets.common(LocalTime.of(10, 30)));
            M3DialogPane pane = dialog.getDialogPane();
            pane.setPrefWidth(720.0);

            StackPane root = new StackPane(pane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 780.0, 620.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(780.0, 620.0);
            root.layout();

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, pane, Color.WHITE, 0.04);
            assertEquals(5, pane.lookupAll("." + "m3-time-picker-dialog-preset-button").size());
            assertTimePickerDialogActionRow(pane, NodeOrientation.LEFT_TO_RIGHT);
            assertSnapshotNodeContainsContrast(
                    image,
                    assertInstanceOf(Node.class, pane.lookup("." + "m3-time-picker-dialog-preset-list")),
                    Color.WHITE,
                    0.04
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-time-presets.png"
            ));
        });
    }

    /// Verifies that picker dialog preset containers follow right-to-left layout direction.
    @Test
    void pickerPresetDialogsPropagateRightToLeftOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerDialog dateDialog = new M3DatePickerDialog(LocalDate.of(2026, 5, 19));
            dateDialog.getPresets().setAll(M3DatePresets.common(LocalDate.of(2026, 5, 19)));
            M3DialogPane datePane = dateDialog.getDialogPane();
            datePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3DateRangePickerDialog rangeDialog = new M3DateRangePickerDialog(
                    LocalDate.of(2026, 5, 19),
                    LocalDate.of(2026, 5, 24)
            );
            rangeDialog.getPresets().setAll(M3DateRangePresets.common(LocalDate.of(2026, 5, 19), rangeDialog.getPicker().getFirstDayOfWeek()));
            M3DialogPane rangePane = rangeDialog.getDialogPane();
            rangePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            M3TimePickerDialog timeDialog = new M3TimePickerDialog(LocalTime.of(10, 30));
            timeDialog.getPicker().setUse24HourClock(true);
            timeDialog.getPicker().setMinuteStep(15);
            timeDialog.getPresets().setAll(M3TimePresets.common(LocalTime.of(10, 30)));
            M3DialogPane timePane = timeDialog.getDialogPane();
            timePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Pane root = new Pane(datePane, rangePane, timePane);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 1420.0, 1240.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(1420.0, 1240.0);
            datePane.resizeRelocate(20.0, 20.0, 620.0, 560.0);
            rangePane.resizeRelocate(680.0, 20.0, 660.0, 560.0);
            timePane.resizeRelocate(20.0, 620.0, 720.0, 560.0);
            root.layout();
            datePane.layout();
            rangePane.layout();
            timePane.layout();

            assertPickerPresetMirrorsRightToLeft(
                    datePane,
                    "m3-date-picker-dialog-preset-content",
                    "m3-date-picker-dialog-preset-list",
                    dateDialog.getPicker()
            );
            assertPickerPresetMirrorsRightToLeft(
                    rangePane,
                    "m3-date-range-picker-dialog-preset-content",
                    "m3-date-range-picker-dialog-preset-list",
                    rangeDialog.getPicker()
            );
            HBox timePresetContent = assertInstanceOf(
                    HBox.class,
                    timePane.lookup("." + "m3-time-picker-dialog-preset-content")
            );
            VBox timePresetList = assertInstanceOf(
                    VBox.class,
                    timePane.lookup("." + "m3-time-picker-dialog-preset-list")
            );
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, timePresetContent.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, timePresetList.getEffectiveNodeOrientation());
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, timeDialog.getPicker().getEffectiveNodeOrientation());
            assertEquals(Pos.TOP_LEFT, timePresetContent.getAlignment());
            assertEquals(Pos.TOP_LEFT, timePresetList.getAlignment());
            Bounds timePresetBounds = timePresetList.localToScene(timePresetList.getBoundsInLocal());
            Bounds timePickerBounds = timeDialog.getPicker().localToScene(
                    timeDialog.getPicker().getBoundsInLocal()
            );
            assertTrue(
                    timePickerBounds.getMaxX() < timePresetBounds.getMinX(),
                    () -> "timePresetBounds=" + timePresetBounds + ", timePickerBounds=" + timePickerBounds
            );
            assertTimePickerDialogActionRow(timePane, NodeOrientation.RIGHT_TO_LEFT);

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, datePane, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, rangePane, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, timePane, Color.WHITE, 0.04);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-picker-dialog-presets-rtl.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies that date range field presets render beside the popup picker.
    @Tier2Test
    @Test
    void dateRangePickerFieldPresetPopupSnapshotRendersPresetColumn() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DateRangePickerField> fieldReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> dateRangeFieldPresetPopupReady(fieldReference),
                    () -> {
                        M3DateRangePickerField field = new M3DateRangePickerField(
                                LocalDate.of(2026, 5, 19),
                                LocalDate.of(2026, 5, 25)
                        );
                        field.setAnimationsEnabled(false);
                        field.getPresets().setAll(M3DateRangePresets.common(LocalDate.of(2026, 5, 19), field.getPicker().getFirstDayOfWeek()));
                        field.setPrefWidth(680.0);

                        Pane root = new Pane(field);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 760.0, 180.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                        root.layout();

                        stageReference.set(stage);
                        fieldReference.set(field);
                        field.showPicker();
                    }, () -> {
                        M3DateRangePickerField field = Objects.requireNonNull(fieldReference.get());
                        assertTrue(field.isShowing());
                        Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                        Node popupRoot = assertInstanceOf(Node.class, presetContent.getParent());
                        popupRoot.applyCss();
                        if (popupRoot instanceof Region region) {
                            region.layout();
                        }

                        WritableImage image = snapshotImageOnFxThread(popupRoot);
                        assertEquals(
                                6,
                                presetContent.lookupAll("." + "m3-date-range-picker-field-preset-button").size()
                        );
                        assertNodeSnapshotContainsContrast(
                                assertInstanceOf(Node.class, presetContent.lookup(
                                        "." + "m3-date-range-picker-field-preset-list"
                                )),
                                Color.WHITE,
                                0.04
                        );
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-date-range-field-presets.png"
                        ));
                    });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3DateRangePickerField field = fieldReference.get();
                if (field != null) {
                    field.hidePicker();
                    field.setAnimationsEnabled(true);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that date range field preset popups mirror preset columns in right-to-left layout.
    @Tier2Test
    @Test
    void dateRangePickerFieldPresetPopupMirrorsRightToLeft() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DateRangePickerField> fieldReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> dateRangeFieldPresetPopupReady(fieldReference),
                    () -> {
                        M3DateRangePickerField field = new M3DateRangePickerField(
                                LocalDate.of(2026, 5, 19),
                                LocalDate.of(2026, 5, 25)
                        );
                        field.setAnimationsEnabled(false);
                        field.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                        field.getPresets().setAll(M3DateRangePresets.common(LocalDate.of(2026, 5, 19), field.getPicker().getFirstDayOfWeek()));
                        field.setPrefWidth(680.0);

                        Pane root = new Pane(field);
                        Stage stage = new Stage();
                        Scene scene = new Scene(root, 760.0, 180.0);
                        M3ThemeManager.install(scene, M3Theme.defaultTheme());
                        stage.setScene(scene);
                        stage.show();
                        root.applyCss();
                        field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                        root.layout();

                        stageReference.set(stage);
                        fieldReference.set(field);
                        field.showPicker();
                    }, () -> {
                        M3DateRangePickerField field = Objects.requireNonNull(fieldReference.get());
                        assertTrue(field.isShowing());
                        Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                        Node popupRoot = assertInstanceOf(Node.class, presetContent.getParent());
                        popupRoot.applyCss();
                        if (popupRoot instanceof Region region) {
                            region.layout();
                        }

                        assertPickerPresetMirrorsRightToLeft(
                                popupRoot,
                                "m3-date-range-picker-field-preset-content",
                                "m3-date-range-picker-field-preset-list",
                                field.getPicker()
                        );

                        WritableImage image = snapshotImageOnFxThread(popupRoot);
                        assertNodeSnapshotContainsContrast(presetContent, Color.WHITE, 0.04);
                        writeVisualSnapshot(image, java.nio.file.Path.of(
                                "build",
                                "reports",
                                "m3fx-visual",
                                "visual-date-range-field-presets-rtl.png"
                        ));
                        assertRenderedTextNodesStayInsideLayout(popupRoot);
                        assertFixedTargetControlsKeepCenteredContent(popupRoot);
                    });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3DateRangePickerField field = fieldReference.get();
                if (field != null) {
                    field.hidePicker();
                    field.setAnimationsEnabled(true);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that open picker field preset popups reflow when inherited node orientation changes at runtime.
    @Tier2Test
    @Test
    void pickerFieldPresetPopupsReflowAfterRuntimeInheritedOrientationChanges() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Pane> rootReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DatePickerField> dateFieldReference = new AtomicReference<>();
        AtomicReference<@Nullable M3TimePickerField> timeFieldReference = new AtomicReference<>();
        AtomicReference<@Nullable M3DateRangePickerField> rangeFieldReference = new AtomicReference<>();

        try {
            FxTestUtils.runOnFxThread(() -> {
                M3DatePickerField dateField = new M3DatePickerField(LocalDate.of(2026, 5, 19));
                dateField.setAnimationsEnabled(false);
                dateField.getPresets().setAll(M3DatePresets.common(LocalDate.of(2026, 5, 19)));
                dateField.setPrefWidth(420.0);

                M3TimePickerField timeField = new M3TimePickerField(LocalTime.of(10, 30));
                timeField.setAnimationsEnabled(false);
                timeField.getPicker().setUse24HourClock(true);
                timeField.getPicker().setMinuteStep(15);
                timeField.getPresets().setAll(M3TimePresets.common(LocalTime.of(10, 30)));
                timeField.setPrefWidth(420.0);

                M3DateRangePickerField rangeField = new M3DateRangePickerField(
                        LocalDate.of(2026, 5, 19),
                        LocalDate.of(2026, 5, 25)
                );
                rangeField.setAnimationsEnabled(false);
                rangeField.getPresets().setAll(M3DateRangePresets.common(
                        LocalDate.of(2026, 5, 19),
                        rangeField.getPicker().getFirstDayOfWeek()
                ));
                rangeField.setPrefWidth(680.0);

                Pane root = new Pane(dateField, timeField, rangeField);
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                Stage stage = new Stage();
                Scene scene = new Scene(root, 780.0, 420.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                dateField.resizeRelocate(24.0, 24.0, 420.0, 96.0);
                timeField.resizeRelocate(24.0, 132.0, 420.0, 96.0);
                rangeField.resizeRelocate(24.0, 240.0, 680.0, 96.0);
                root.layout();

                stageReference.set(stage);
                rootReference.set(root);
                dateFieldReference.set(dateField);
                timeFieldReference.set(timeField);
                rangeFieldReference.set(rangeField);
            });

            FxTestUtils.runOnFxThreadWhen(
                    () -> pickerFieldPresetPopupReady(
                            dateFieldReference.get(),
                            "m3-date-picker-field-preset-button",
                            "m3-date-picker-field-preset-list"
                    ),
                    () -> {
                        Pane root = Objects.requireNonNull(rootReference.get());
                        M3DatePickerField dateField = Objects.requireNonNull(dateFieldReference.get());
                        root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                        dateField.getEditor().requestFocus();
                        dateField.showPicker();
                    },
                    () -> assertPickerFieldPresetPopupReflowsAfterRuntimeOrientationChange(
                            Objects.requireNonNull(rootReference.get()),
                            Objects.requireNonNull(dateFieldReference.get()),
                            "m3-date-picker-field-preset-content",
                            "m3-date-picker-field-preset-list",
                            "m3-date-picker-field-preset-button",
                            Objects.requireNonNull(dateFieldReference.get()).getEditor()
                    )
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> pickerFieldPresetPopupReady(
                            timeFieldReference.get(),
                            "m3-time-picker-field-preset-button",
                            "m3-time-picker-field-preset-list"
                    ),
                    () -> {
                        Pane root = Objects.requireNonNull(rootReference.get());
                        M3TimePickerField timeField = Objects.requireNonNull(timeFieldReference.get());
                        root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                        timeField.getEditor().requestFocus();
                        timeField.showPicker();
                    },
                    () -> assertPickerFieldPresetPopupReflowsAfterRuntimeOrientationChange(
                            Objects.requireNonNull(rootReference.get()),
                            Objects.requireNonNull(timeFieldReference.get()),
                            "m3-time-picker-field-preset-content",
                            "m3-time-picker-field-preset-list",
                            "m3-time-picker-field-preset-button",
                            Objects.requireNonNull(timeFieldReference.get()).getEditor()
                    )
            );

            FxTestUtils.runOnFxThreadWhen(
                    () -> dateRangeFieldPresetPopupReady(rangeFieldReference),
                    () -> {
                        Pane root = Objects.requireNonNull(rootReference.get());
                        M3DateRangePickerField rangeField = Objects.requireNonNull(rangeFieldReference.get());
                        root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                        rangeField.getEndEditor().requestFocus();
                        rangeField.showPicker();
                    },
                    () -> assertDateRangePickerFieldPresetPopupReflowsAfterRuntimeOrientationChange(
                            Objects.requireNonNull(rootReference.get()),
                            Objects.requireNonNull(rangeFieldReference.get())
                    )
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable M3DatePickerField dateField = dateFieldReference.get();
                if (dateField != null) {
                    dateField.hidePicker();
                    dateField.setAnimationsEnabled(true);
                }
                @Nullable M3TimePickerField timeField = timeFieldReference.get();
                if (timeField != null) {
                    timeField.hidePicker();
                    timeField.setAnimationsEnabled(true);
                }
                @Nullable M3DateRangePickerField rangeField = rangeFieldReference.get();
                if (rangeField != null) {
                    rangeField.hidePicker();
                    rangeField.setAnimationsEnabled(true);
                }
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that date range picker fields route popup accessibility focus through real popup children.
    @Test
    void dateRangePickerFieldRoutesPopupAccessibilityFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePickerField field = new M3DateRangePickerField(
                    LocalDate.of(2026, 5, 19),
                    LocalDate.of(2026, 5, 25)
            );
            field.getPresets().setAll(M3DateRangePresets.common(LocalDate.of(2026, 5, 19), field.getPicker().getFirstDayOfWeek()));
            field.setPrefWidth(680.0);

            M3Button outsideFocus = new M3Button("Outside");
            Pane root = new Pane(outsideFocus, field);
            Stage stage = new Stage();
            try {
                field.setAnimationsEnabled(false);
                Scene scene = new Scene(root, 760.0, 220.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                outsideFocus.resizeRelocate(24.0, 132.0, 120.0, 40.0);
                field.resizeRelocate(24.0, 24.0, 680.0, 96.0);
                root.applyCss();
                root.layout();

                outsideFocus.requestFocus();
                field.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(field.getStartEditor().isFocused());
                assertSame(field.getStartEditor(), field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.getEndEditor().requestFocus();
                assertTrue(field.getEndEditor().isFocused());
                field.executeAccessibleAction(AccessibleAction.EXPAND);

                assertTrue(field.isShowing());
                Node pickerFocusNode = assertInstanceOf(
                        Node.class,
                        field.getPicker().queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE)
                );

                field.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(pickerFocusNode.isFocused());
                assertSame(pickerFocusNode, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                Node presetContent = assertInstanceOf(Node.class, field.getPicker().getParent());
                Node popupRoot = assertInstanceOf(Node.class, presetContent.getParent());
                popupRoot.applyCss();
                if (popupRoot instanceof Region region) {
                    region.layout();
                }
                Node presetButton = assertInstanceOf(
                        Node.class,
                        presetContent.lookup("." + "m3-date-range-picker-field-preset-button")
                );

                presetButton.requestFocus();

                assertTrue(presetButton.isFocused());
                assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                field.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(presetButton.isFocused());
                assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                presetButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

                assertFalse(field.isShowing());
                assertTrue(field.getEndEditor().isFocused());
                assertSame(field.getEndEditor(), field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                field.setAnimationsEnabled(true);
                field.hidePicker();
                stage.close();
            }
        });
    }

    /// Verifies that date pickers expose rendered day cells to accessibility clients.
    @Test
    void datePickerExposesAccessibleDayCellsAndActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 19));
            datePicker.setDisplayedMonth(YearMonth.of(2026, 5));
            datePicker.setFirstDayOfWeek(DayOfWeek.MONDAY);
            datePicker.setShowAdjacentMonthDays(false);

            Pane root = new Pane(datePicker);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 420.0, 360.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(31, datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                Node firstDay = assertInstanceOf(Node.class,
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(LocalDate.of(2026, 5, 1), firstDay.getUserData());
                assertNull(datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 31));

                Node selectedDay = assertInstanceOf(Node.class,
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(LocalDate.of(2026, 5, 19), selectedDay.getUserData());
                assertEquals(java.util.List.of(LocalDate.of(2026, 5, 19)),
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

                datePicker.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(selectedDay.isFocused());

                datePicker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 14);
                Node shownDay = assertInstanceOf(Node.class,
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 14));
                assertEquals(LocalDate.of(2026, 5, 15), shownDay.getUserData());
                assertTrue(shownDay.isFocused());

                datePicker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalDate.of(2026, 5, 21));

                assertEquals(LocalDate.of(2026, 5, 21), datePicker.getValue());
                assertEquals(java.util.List.of(LocalDate.of(2026, 5, 21)),
                        datePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that date picker day labels stay centered inside fixed day cells.
    @Test
    void datePickerCentersDayCellContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            datePicker.setDisplayedMonth(YearMonth.of(2026, 5));
            datePicker.setFirstDayOfWeek(DayOfWeek.MONDAY);
            Pane root = new Pane(datePicker);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 440.0, 460.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            double pickerWidth = 360.0;
            double pickerHeight = Math.ceil(datePicker.prefHeight(pickerWidth));
            root.resize(440.0, pickerHeight + 40.0);
            datePicker.relocate(20.0, 20.0);
            datePicker.resize(pickerWidth, pickerHeight);
            root.layout();
            datePicker.layout();

            for (LocalDate date : java.util.List.of(
                    LocalDate.of(2026, 5, 8),
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 21)
            )) {
                ButtonBase dayCell = dateCellForDate(datePicker, date);
                Node textNode = Objects.requireNonNull(dayCell.lookup(".text"));

                assertEquals(Pos.CENTER, dayCell.getAlignment());
                assertNodeCentersAligned(dayCell, textNode, 0.75);
            }

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, datePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(
                    image,
                    dateCellForDate(datePicker, LocalDate.of(2026, 5, 18)),
                    Color.WHITE,
                    0.08
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-picker-centering.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies that time picker cell labels stay centered inside fixed time cells.
    @Test
    void timePickerCentersCellContent() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(9, 30));
            timePicker.setUse24HourClock(true);
            timePicker.setMinuteStep(15);
            Pane root = new Pane(timePicker);
            root.setStyle("-fx-background-color: white; " + visualTestColors());
            Scene scene = new Scene(root, 560.0, 420.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            double pickerWidth = 480.0;
            double pickerHeight = Math.ceil(timePicker.prefHeight(pickerWidth));
            root.resize(560.0, pickerHeight + 40.0);
            timePicker.relocate(20.0, 20.0);
            timePicker.resize(pickerWidth, pickerHeight);
            root.layout();
            timePicker.layout();

            for (LocalTime time : java.util.List.of(
                    LocalTime.of(0, 30),
                    LocalTime.of(9, 30),
                    LocalTime.of(21, 30)
            )) {
                ButtonBase timeCell = timeCellForTime(timePicker, time);
                Node textNode = Objects.requireNonNull(timeCell.lookup(".text"));

                assertEquals(Pos.CENTER, timeCell.getAlignment());
                assertNodeCentersAligned(timeCell, textNode, 0.75);
            }

            assertInstanceOf(
                    ButtonBase.class,
                    timePicker.lookup("." + "m3-time-picker-minute-display")
            ).fire();
            root.layout();
            timePicker.layout();

            for (LocalTime time : java.util.List.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(9, 15),
                    LocalTime.of(9, 30),
                    LocalTime.of(9, 45)
            )) {
                ButtonBase timeCell = timeCellForTime(timePicker, time);
                Node textNode = Objects.requireNonNull(timeCell.lookup(".text"));

                assertEquals(Pos.CENTER, timeCell.getAlignment());
                assertNodeCentersAligned(timeCell, textNode, 0.75);
            }

            WritableImage image = snapshotImageOnFxThread(root);
            assertSnapshotNodeContainsContrast(image, timePicker, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(
                    image,
                    timeCellForTime(timePicker, LocalTime.of(9, 30)),
                    Color.WHITE,
                    0.08
            );
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-time-picker-centering.png"
            ));
            assertMaterialTargetGeometryIsStable(root);
        });
    }

    /// Verifies that time pickers expose rendered selectable cells to accessibility clients.
    @Test
    void timePickerExposesAccessibleCellsAndActions() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker timePicker = new M3TimePicker(LocalTime.of(9, 30));
            timePicker.setUse24HourClock(true);
            timePicker.setMinuteStep(15);

            Pane root = new Pane(timePicker);
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(root, 520.0, 360.0));
                M3ThemeManager.install(stage.getScene(), M3Theme.defaultTheme());
                stage.show();
                root.applyCss();
                root.layout();

                assertEquals(24, timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
                Node firstHour = assertInstanceOf(Node.class,
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
                assertEquals(LocalTime.of(0, 30), firstHour.getUserData());
                assertNull(timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 24));

                Node selectedTime = assertInstanceOf(Node.class,
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
                assertEquals(LocalTime.of(9, 30), selectedTime.getUserData());
                assertEquals(java.util.List.of(LocalTime.of(9, 30)),
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

                timePicker.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
                assertTrue(selectedTime.isFocused());

                timePicker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, 21);
                Node shownHour = assertInstanceOf(Node.class,
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 21));
                assertEquals(LocalTime.of(21, 30), shownHour.getUserData());
                assertTrue(shownHour.isFocused());

                timePicker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalTime.of(14, 45, 12));

                assertEquals(LocalTime.of(14, 45), timePicker.getValue());
                assertEquals(java.util.List.of(LocalTime.of(14, 45)),
                        timePicker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies mode-switch and text-action placement in a time picker dialog action row.
    private static void assertTimePickerDialogActionRow(M3DialogPane pane, NodeOrientation orientation) {
        Node modeButton = assertInstanceOf(
                Node.class,
                pane.lookup("." + "m3-time-picker-mode-button")
        );
        Node cancelButton = Objects.requireNonNull(pane.getCancelAction(), "cancel action");
        Node okButton = Objects.requireNonNull(pane.getDefaultAction(), "default action");
        Bounds modeBounds = modeButton.localToScene(modeButton.getBoundsInLocal());
        Bounds cancelBounds = cancelButton.localToScene(cancelButton.getBoundsInLocal());
        Bounds okBounds = okButton.localToScene(okButton.getBoundsInLocal());

        assertEquals(cancelBounds.getCenterY(), modeBounds.getCenterY(), 1.0);
        assertEquals(okBounds.getCenterY(), modeBounds.getCenterY(), 1.0);
        if (orientation == NodeOrientation.RIGHT_TO_LEFT) {
            assertTrue(Math.max(cancelBounds.getMaxX(), okBounds.getMaxX()) < modeBounds.getMinX());
        } else {
            assertTrue(modeBounds.getMaxX() < Math.min(cancelBounds.getMinX(), okBounds.getMinX()));
        }
    }

    /// Verifies that a picker preset column is on the right-to-left logical start side.
    private static void assertPickerPresetMirrorsRightToLeft(
            Node root,
            String presetContentStyleClass,
            String presetListStyleClass,
            Node picker
    ) {
        assertPickerPresetUsesOrientation(root, presetContentStyleClass, presetListStyleClass, picker,
                NodeOrientation.RIGHT_TO_LEFT);
    }

    /// Verifies that a picker preset column is on the current logical start side.
    private static void assertPickerPresetUsesOrientation(
            Node root,
            String presetContentStyleClass,
            String presetListStyleClass,
            Node picker,
            NodeOrientation orientation
    ) {
        HBox presetContent = assertInstanceOf(HBox.class, root.lookup("." + presetContentStyleClass));
        VBox presetList = assertInstanceOf(VBox.class, root.lookup("." + presetListStyleClass));

        assertEquals(orientation, root.getEffectiveNodeOrientation());
        assertEquals(orientation, presetContent.getEffectiveNodeOrientation());
        assertEquals(orientation, presetList.getEffectiveNodeOrientation());
        assertEquals(orientation, picker.getEffectiveNodeOrientation());
        Pos expectedAlignment = Pos.TOP_LEFT;
        assertEquals(expectedAlignment, presetContent.getAlignment());
        assertEquals(expectedAlignment, presetList.getAlignment());

        Bounds pickerBounds = picker.localToScene(picker.getBoundsInLocal());
        Bounds presetListBounds = presetList.localToScene(presetList.getBoundsInLocal());
        if (orientation == NodeOrientation.RIGHT_TO_LEFT) {
            assertTrue(presetListBounds.getMinX() > pickerBounds.getMaxX(),
                    () -> "presetListBounds=" + presetListBounds + ", pickerBounds=" + pickerBounds);
        } else {
            assertTrue(presetListBounds.getMaxX() < pickerBounds.getMinX(),
                    () -> "presetListBounds=" + presetListBounds + ", pickerBounds=" + pickerBounds);
        }
    }

    /// Verifies that a single-value picker field popup follows inherited runtime orientation changes.
    private static void assertPickerFieldPresetPopupReflowsAfterRuntimeOrientationChange(
            Parent ownerRoot,
            M3PickerField<?, ?> field,
            String presetContentStyleClass,
            String presetListStyleClass,
            String presetButtonStyleClass,
            Node expectedClosedFocusNode
    ) {
        Parent popupRoot = Objects.requireNonNull(pickerFieldPresetPopupRootOrNull(field), "picker field preset popup root");
        Node presetButton = assertInstanceOf(Node.class, popupRoot.lookup("." + presetButtonStyleClass));

        ownerRoot.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        applyOwnerAndPopupLayout(ownerRoot, popupRoot);
        assertPickerPresetUsesOrientation(
                popupRoot,
                presetContentStyleClass,
                presetListStyleClass,
                field.getPicker(),
                NodeOrientation.LEFT_TO_RIGHT
        );

        presetButton.requestFocus();
        assertTrue(presetButton.isFocused());
        assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        ownerRoot.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        applyOwnerAndPopupLayout(ownerRoot, popupRoot);
        assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertPickerPresetUsesOrientation(
                popupRoot,
                presetContentStyleClass,
                presetListStyleClass,
                field.getPicker(),
                NodeOrientation.RIGHT_TO_LEFT
        );

        ownerRoot.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        applyOwnerAndPopupLayout(ownerRoot, popupRoot);
        assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertPickerPresetUsesOrientation(
                popupRoot,
                presetContentStyleClass,
                presetListStyleClass,
                field.getPicker(),
                NodeOrientation.LEFT_TO_RIGHT
        );

        presetButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

        assertFalse(field.isShowing());
        assertTrue(expectedClosedFocusNode.isFocused());
        assertSame(expectedClosedFocusNode, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Verifies that a date range picker field popup follows inherited runtime orientation changes.
    private static void assertDateRangePickerFieldPresetPopupReflowsAfterRuntimeOrientationChange(
            Parent ownerRoot,
            M3DateRangePickerField field
    ) {
        Parent presetContent = Objects.requireNonNull(field.getPicker().getParent(), "preset content");
        Parent popupRoot = Objects.requireNonNull(presetContent.getParent(), "date range picker field preset popup root");
        Node presetButton = assertInstanceOf(
                Node.class,
                popupRoot.lookup("." + "m3-date-range-picker-field-preset-button")
        );

        ownerRoot.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        applyOwnerAndPopupLayout(ownerRoot, popupRoot);
        assertPickerPresetUsesOrientation(
                popupRoot,
                "m3-date-range-picker-field-preset-content",
                "m3-date-range-picker-field-preset-list",
                field.getPicker(),
                NodeOrientation.LEFT_TO_RIGHT
        );

        presetButton.requestFocus();
        assertTrue(presetButton.isFocused());
        assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

        ownerRoot.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        applyOwnerAndPopupLayout(ownerRoot, popupRoot);
        assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertPickerPresetUsesOrientation(
                popupRoot,
                "m3-date-range-picker-field-preset-content",
                "m3-date-range-picker-field-preset-list",
                field.getPicker(),
                NodeOrientation.RIGHT_TO_LEFT
        );

        ownerRoot.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        applyOwnerAndPopupLayout(ownerRoot, popupRoot);
        assertSame(presetButton, field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
        assertPickerPresetUsesOrientation(
                popupRoot,
                "m3-date-range-picker-field-preset-content",
                "m3-date-range-picker-field-preset-list",
                field.getPicker(),
                NodeOrientation.LEFT_TO_RIGHT
        );

        presetButton.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));

        assertFalse(field.isShowing());
        assertTrue(field.getEndEditor().isFocused());
        assertSame(field.getEndEditor(), field.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
    }

    /// Applies CSS and layout to an owner scene root and one popup root.
    private static void applyOwnerAndPopupLayout(Parent ownerRoot, Node popupRoot) {
        ownerRoot.applyCss();
        ownerRoot.layout();
        popupRoot.applyCss();
        if (popupRoot instanceof Parent popupParent) {
            popupParent.layout();
        }
    }

    /// Returns whether a single-value picker field popup has rendered its preset column.
    private static boolean pickerFieldPresetPopupReady(
            @Nullable M3PickerField<?, ?> field,
            String presetButtonStyleClass,
            String presetListStyleClass
    ) {
        if (field == null || !field.isShowing()) {
            return false;
        }

        @Nullable Parent popupRoot = pickerFieldPresetPopupRootOrNull(field);
        if (popupRoot == null || popupRoot.getScene() == null) {
            return false;
        }

        popupRoot.applyCss();
        popupRoot.layout();
        return !popupRoot.lookupAll("." + presetButtonStyleClass).isEmpty()
                && popupRoot.lookup("." + presetListStyleClass) != null
                && hasRenderableBounds(field.getPicker());
    }

    /// Returns the popup root for a single-value picker field preset popup, or `null` before it is attached.
    private static @Nullable Parent pickerFieldPresetPopupRootOrNull(M3PickerField<?, ?> field) {
        @Nullable Parent presetContent = field.getPicker().getParent();
        return presetContent == null ? null : presetContent.getParent();
    }

    /// Returns whether a date range picker field popup has rendered its preset column.
    private static boolean dateRangeFieldPresetPopupReady(
            AtomicReference<@Nullable M3DateRangePickerField> fieldReference
    ) {
        @Nullable M3DateRangePickerField field = fieldReference.get();
        if (field == null || !field.isShowing()) {
            return false;
        }

        @Nullable Parent presetContent = field.getPicker().getParent();
        if (presetContent == null) {
            return false;
        }

        @Nullable Parent popupRoot = presetContent.getParent();
        if (popupRoot == null || popupRoot.getScene() == null) {
            return false;
        }

        popupRoot.applyCss();
        if (popupRoot instanceof Region region) {
            region.layout();
        }

        return presetContent.lookupAll("." + "m3-date-range-picker-field-preset-button").size() == 6
                && presetContent.lookup("." + "m3-date-range-picker-field-preset-list") != null;
    }

    /// Verifies that hidden picker field preset content uses the orientation-aware logical start alignment.
    private static void assertPickerFieldPresetContentUsesLogicalStart(Node presetContentNode, String presetListStyleClass) {
        HBox presetContent = assertInstanceOf(HBox.class, presetContentNode);
        VBox presetList = assertInstanceOf(VBox.class, presetContent.lookup("." + presetListStyleClass));

        assertEquals(NodeOrientation.RIGHT_TO_LEFT, presetContent.getEffectiveNodeOrientation());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, presetList.getEffectiveNodeOrientation());
        assertEquals(Pos.TOP_LEFT, presetContent.getAlignment());
        assertEquals(Pos.TOP_LEFT, presetList.getAlignment());
    }

    /// Returns a date picker day cell for the supplied date.
    private static ButtonBase dateCellForDate(M3DatePicker picker, LocalDate date) {
        for (Node node : picker.lookupAll("." + "m3-date-picker-day-cell")) {
            if (node instanceof ButtonBase button && date.equals(button.getUserData())) {
                return button;
            }
        }
        throw new AssertionError("No date cell found for " + date);
    }

    /// Returns a time picker cell for the supplied time.
    private static ButtonBase timeCellForTime(M3TimePicker picker, LocalTime time) {
        for (Node node : picker.lookupAll("." + "m3-time-picker-cell")) {
            if (node instanceof ButtonBase button && time.equals(button.getUserData())) {
                return button;
            }
        }
        throw new AssertionError("No time cell found for " + time);
    }

    /// Verifies that a node's own snapshot contains pixels that contrast with a reference color.
    private static void assertNodeSnapshotContainsContrast(Node node, Color reference, double minimumDistance) {
        WritableImage image = snapshotImageOnFxThread(node);
        assertSnapshotAreaContainsContrast(
                image,
                0,
                0,
                (int) image.getWidth(),
                (int) image.getHeight(),
                reference,
                minimumDistance,
                node + " snapshot"
        );
    }

    /// Verifies that a picker exposes both logical navigation chevrons.
    private static void assertNavigationGlyphs(Control picker) {
        Set<M3InternalIcon.Glyph> glyphs = new HashSet<>();
        for (Node node : picker.lookupAll("." + "m3-date-picker-navigation-button")) {
            M3IconButton button = assertInstanceOf(M3IconButton.class, node);
            M3InternalIcon icon = assertInstanceOf(M3InternalIcon.class, button.getGraphic());
            glyphs.add(icon.getGlyph());
        }
        assertEquals(
                Set.of(M3InternalIcon.Glyph.CHEVRON_LEFT, M3InternalIcon.Glyph.CHEVRON_RIGHT),
                glyphs,
                () -> "Navigation glyphs for " + picker
        );
    }
}
