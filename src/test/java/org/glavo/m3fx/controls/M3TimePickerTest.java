// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3TimePickerSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotAreaChanged;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotHasColorVariety;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotNodeContainsContrast;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.snapshotImageOnFxThread;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.visualTestColors;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.writeVisualSnapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3TimePicker] API behavior, skin interaction, and visual rendering.
@NotNullByDefault
final class M3TimePickerTest {
    /// The pseudo-class used to verify pressed-like time cell feedback in visual tests.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The tolerance used when comparing rendered cell text ink centers.
    private static final double CELL_TEXT_INK_CENTER_TOLERANCE = 1.5;

    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies selected time normalization, ranges, and step validation.
    @Test
    void timePickerPropertiesNormalizeAndValidateRange() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15, 42, 12));

        assertEquals(LocalTime.of(10, 15), picker.getValue());

        picker.setMinTime(LocalTime.of(9, 0, 5));
        picker.setMaxTime(LocalTime.of(17, 30, 5));
        assertEquals(LocalTime.of(9, 0), picker.getMinTime());
        assertEquals(LocalTime.of(17, 30), picker.getMaxTime());
        assertThrows(IllegalArgumentException.class, () -> picker.setValue(LocalTime.of(8, 45)));
        assertThrows(IllegalArgumentException.class, () -> picker.setMinuteStep(7));
        assertThrows(IllegalArgumentException.class, () -> picker.setMinTime(LocalTime.of(18, 0)));

        picker.setMaxTime(LocalTime.of(9, 30));
        assertNull(picker.getValue());
    }

    /// Verifies that the skin creates selectable cells and routes actions into the value property.
    @Test
    void timePickerSkinSelectsHourMinuteAndPeriod() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15));
            picker.setMinuteStep(15);
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 520.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(460.0, 320.0);
            picker.layout();

            assertInstanceOf(M3TimePickerSkin.class, picker.getSkin());
            assertEquals(12, picker.lookupAll("." + M3TimePicker.HOUR_CELL_STYLE_CLASS).size());
            assertEquals(4, picker.lookupAll("." + M3TimePicker.MINUTE_CELL_STYLE_CLASS).size());

            cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "11").fire();
            assertEquals(LocalTime.of(11, 15), picker.getValue());

            cellByText(picker, M3TimePicker.MINUTE_CELL_STYLE_CLASS, "30").fire();
            assertEquals(LocalTime.of(11, 30), picker.getValue());

            cellByText(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS, "PM").fire();
            assertEquals(LocalTime.of(23, 30), picker.getValue());
        });
    }

    /// Verifies that value changes reuse time cells and retain equivalent minute candidates.
    @Test
    void timePickerSkinReusesCellsAcrossValueChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15));
            picker.setMinuteStep(15);
            picker.setMinTime(LocalTime.of(9, 0));
            picker.setMaxTime(LocalTime.of(13, 30));
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 520.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(460.0, 320.0);
            picker.layout();

            ButtonBase hourCell = cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "10");
            ButtonBase minuteCell = cellByText(picker, M3TimePicker.MINUTE_CELL_STYLE_CLASS, "15");
            ButtonBase periodCell = cellByText(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS, "AM");
            Object minuteCandidate = minuteCell.getUserData();

            assertTrue(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "8").isDisabled());
            assertFalse(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "9").isDisabled());

            picker.setMinTime(LocalTime.of(10, 0));

            assertTrue(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "9").isDisabled());
            assertFalse(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "10").isDisabled());

            picker.setValue(LocalTime.of(10, 30));

            assertSame(minuteCandidate, minuteCell.getUserData());

            picker.setValue(LocalTime.of(11, 30));

            assertSame(hourCell, cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "10"));
            assertSame(minuteCell, cellByText(picker, M3TimePicker.MINUTE_CELL_STYLE_CLASS, "15"));
            assertSame(periodCell, cellByText(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS, "AM"));

            picker.setValue(LocalTime.of(13, 30));

            assertSame(hourCell, cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "10"));
            assertFalse(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "1").isDisabled());
            assertTrue(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "2").isDisabled());
            assertFalse(periodCell.getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));
            assertTrue(cellByText(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS, "PM")
                    .getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));
        });
    }

    /// Verifies that replacing a time picker skin detaches the retired skin from control changes.
    @Test
    void replacingTimePickerSkinDetachesRetiredListeners() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 15));
            picker.setMinuteStep(15);
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 520.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(460.0, 320.0);
            picker.layout();

            ButtonBase retiredSelectedCell = cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "10");
            ButtonBase retiredTargetCell = cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "11");
            assertTrue(retiredSelectedCell.getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));
            assertFalse(retiredTargetCell.getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));

            picker.setSkin(new M3TimePickerSkin(picker));
            picker.setValue(LocalTime.of(11, 30));

            assertTrue(retiredSelectedCell.getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));
            assertFalse(retiredTargetCell.getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));
        });
    }

    /// Verifies 24-hour mode, minute-step rendering, and bounded range disabled states.
    @Test
    void timePickerSupportsTwentyFourHourModeAndRanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(14, 45));
            picker.setUse24HourClock(true);
            picker.setMinuteStep(15);
            picker.setMinTime(LocalTime.of(9, 0));
            picker.setMaxTime(LocalTime.of(17, 30));
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 620.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(560.0, 320.0);
            picker.layout();

            assertEquals(24, picker.lookupAll("." + M3TimePicker.HOUR_CELL_STYLE_CLASS).size());
            assertEquals(4, picker.lookupAll("." + M3TimePicker.MINUTE_CELL_STYLE_CLASS).size());
            assertTrue(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "08").isDisabled());
            assertFalse(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "09").isDisabled());
            assertTrue(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "18").isDisabled());

            picker.setValue(LocalTime.of(14, 30));
            picker.setMinuteStep(10);

            assertEquals(6, picker.lookupAll("." + M3TimePicker.MINUTE_CELL_STYLE_CLASS).size());
            assertTrue(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "18").isDisabled());

            picker.setUse24HourClock(false);

            assertEquals(12, picker.lookupAll("." + M3TimePicker.HOUR_CELL_STYLE_CLASS).size());
            assertEquals(2, picker.lookupAll("." + M3TimePicker.PERIOD_CELL_STYLE_CLASS).size());
            assertTrue(cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "2")
                    .getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));
            assertTrue(cellByText(picker, M3TimePicker.PERIOD_CELL_STYLE_CLASS, "PM")
                    .getStyleClass().contains(M3TimePicker.SELECTED_CELL_STYLE_CLASS));
        });
    }

    /// Verifies keyboard navigation changes the selected time without relying on a skin internals.
    @Test
    void timePickerHandlesKeyboardNavigation() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
        picker.setMinuteStep(15);

        picker.fireEvent(keyEvent(KeyCode.RIGHT));
        assertEquals(LocalTime.of(11, 30), picker.getValue());

        picker.fireEvent(keyEvent(KeyCode.UP));
        assertEquals(LocalTime.of(11, 45), picker.getValue());

        picker.fireEvent(keyEvent(KeyCode.LEFT));
        assertEquals(LocalTime.of(10, 45), picker.getValue());

        picker.fireEvent(keyEvent(KeyCode.DOWN));
        assertEquals(LocalTime.of(10, 30), picker.getValue());
    }

    /// Verifies modified time picker navigation keys are left to application shortcuts.
    @Test
    void timePickerModifiedNavigationKeysAreIgnored() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
        picker.setMinuteStep(15);

        picker.fireEvent(modifiedKeyEvent(KeyCode.RIGHT, false, true));
        picker.fireEvent(modifiedKeyEvent(KeyCode.UP, true, false));

        assertEquals(LocalTime.of(10, 30), picker.getValue());
    }

    /// Verifies that horizontal keyboard navigation follows visual order in right-to-left layouts.
    @Test
    void timePickerMirrorsHorizontalKeyboardNavigationInRightToLeftLayouts() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
        picker.setMinuteStep(15);
        picker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        picker.fireEvent(keyEvent(KeyCode.RIGHT));
        assertEquals(LocalTime.of(9, 30), picker.getValue());

        picker.fireEvent(keyEvent(KeyCode.LEFT));
        assertEquals(LocalTime.of(10, 30), picker.getValue());

        picker.fireEvent(keyEvent(KeyCode.UP));
        assertEquals(LocalTime.of(10, 45), picker.getValue());
    }

    /// Verifies that accessibility adjustment actions mirror time picker keyboard navigation.
    @Test
    void timePickerAccessibleActionsAdjustTimeSelection() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
        picker.setMinuteStep(15);

        picker.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(LocalTime.of(10, 45), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.DECREMENT);
        assertEquals(LocalTime.of(10, 30), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.BLOCK_INCREMENT);
        assertEquals(LocalTime.of(11, 30), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.BLOCK_DECREMENT);
        assertEquals(LocalTime.of(10, 30), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, List.of(LocalTime.of(12, 45)));
        assertEquals(LocalTime.of(12, 45), picker.getValue());
        assertEquals(List.of(LocalTime.of(12, 45)),
                picker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));
    }

    /// Verifies that accessibility reveal and selection ignore times outside selectable bounds.
    @Test
    void timePickerAccessibleActionsIgnoreDisabledTimeValues() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            picker.setMinuteStep(15);
            picker.setMinTime(LocalTime.of(9, 0));
            picker.setMaxTime(LocalTime.of(12, 45));
            Stage stage = new Stage();
            try {
                Pane root = new Pane(picker);
                Scene scene = new Scene(root, 520.0, 360.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                picker.resize(460.0, 320.0);
                root.layout();
                picker.layout();

                ButtonBase focusedCell = cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "10");
                focusedCell.requestFocus();

                picker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalTime.of(14, 0));

                assertTrue(focusedCell.isFocused());
                assertEquals(LocalTime.of(10, 30), picker.getValue());

                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalTime.of(14, 0));

                assertTrue(focusedCell.isFocused());
                assertEquals(LocalTime.of(10, 30), picker.getValue());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that composite owners skip time pickers whose value route rejects an out-of-range target.
    @Test
    void timePickerAccessibleRouteRejectsOutOfRangeTargetsInCompositeOwners() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker first = new M3TimePicker(LocalTime.of(10, 30));
            first.setMinTime(LocalTime.of(9, 0));
            first.setMaxTime(LocalTime.of(12, 0));
            M3TimePicker second = new M3TimePicker(LocalTime.of(18, 0));
            second.setMinTime(LocalTime.of(17, 0));
            second.setMaxTime(LocalTime.of(20, 0));
            M3FormPane form = new M3FormPane();
            form.getItems().setAll(first, second);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(form);
                Scene scene = new Scene(root, 760.0, 540.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                form.resizeRelocate(24.0, 24.0, 700.0, 460.0);
                root.layout();
                form.layout();

                form.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalTime.of(18, 0));

                Node focusOwner = scene.getFocusOwner();
                assertTrue(focusOwner != null && M3Accessible.containsNode(second, focusOwner));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that accessibility selection ignores unreachable rendered time cell nodes.
    @Test
    void timePickerAccessibleSelectionIgnoresUnreachableTimeCells() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            picker.setMinuteStep(15);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(picker);
                Scene scene = new Scene(root, 520.0, 360.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                ButtonBase targetCell = cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "11");
                targetCell.setVisible(false);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, targetCell);

                assertEquals(LocalTime.of(10, 30), picker.getValue());

                targetCell.setVisible(true);
                targetCell.setDisable(true);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, targetCell);

                assertEquals(LocalTime.of(10, 30), picker.getValue());

                targetCell.setDisable(false);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, targetCell);

                assertEquals(LocalTime.of(11, 30), picker.getValue());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that default accessibility focus actions preserve the focused visible time cell.
    @Test
    void timePickerAccessibleFocusPreservesFocusedVisibleCell() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            picker.setMinuteStep(15);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(picker);
                Scene scene = new Scene(root, 520.0, 360.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                picker.resize(460.0, 320.0);
                root.layout();
                picker.layout();

                ButtonBase focusedCell = cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "11");
                focusedCell.requestFocus();

                assertTrue(focusedCell.isFocused());
                assertSame(focusedCell, picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));

                picker.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);

                assertTrue(focusedCell.isFocused());

                picker.executeAccessibleAction(AccessibleAction.SHOW_ITEM);

                assertTrue(focusedCell.isFocused());
                assertSame(focusedCell, picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that time pickers render selected, 24-hour, and disabled range states.
    @Test
    void timePickerSnapshotRendersSelectionAndRangeStates() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker twelveHour = new M3TimePicker(LocalTime.of(10, 30));
            M3TimePicker twentyFourHour = new M3TimePicker(LocalTime.of(14, 45));
            twentyFourHour.setUse24HourClock(true);
            twentyFourHour.setMinuteStep(15);
            twentyFourHour.setMinTime(LocalTime.of(9, 0));
            twentyFourHour.setMaxTime(LocalTime.of(17, 30));

            HBox row = new HBox(20.0, twelveHour, twentyFourHour);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 1120.0, 420.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(1120.0, 420.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotHasColorVariety(image, 8);
            assertSnapshotNodeContainsContrast(image, twelveHour, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, twentyFourHour, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(
                    image,
                    cellByText(twelveHour, M3TimePicker.MINUTE_CELL_STYLE_CLASS, "30"),
                    Color.WHITE,
                    0.08
            );
            assertCellTextInkCentered(image, cellByText(twelveHour, M3TimePicker.HOUR_CELL_STYLE_CLASS, "10"));
            assertCellTextInkCentered(image, cellByText(twelveHour, M3TimePicker.MINUTE_CELL_STYLE_CLASS, "30"));
            assertCellTextInkCentered(image, cellByText(twentyFourHour, M3TimePicker.HOUR_CELL_STYLE_CLASS, "14"));
            assertCellTextInkCentered(image, cellByText(twentyFourHour, M3TimePicker.HOUR_CELL_STYLE_CLASS, "18"));
            assertCellTextInkCentered(image, cellByText(twentyFourHour, M3TimePicker.MINUTE_CELL_STYLE_CLASS, "45"));
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-time-picker.png"
            ));
        });
    }

    /// Verifies that an armed time cell shows the Material state layer.
    @Test
    void timePickerCellShowsArmedStateLayer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            picker.setMinuteStep(15);
            Pane root = new Pane(picker);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 560.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(500.0, 320.0);
            root.resize(560.0, 360.0);
            root.layout();

            ButtonBase targetCell = cellByText(picker, M3TimePicker.HOUR_CELL_STYLE_CLASS, "11");
            WritableImage normalImage = snapshotImageOnFxThread(root);

            targetCell.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, true);
            assertTrue(targetCell.getPseudoClassStates().contains(ARMED_PSEUDO_CLASS));
            targetCell.applyCss();
            targetCell.requestLayout();
            root.layout();

            Node stateLayer = targetCell.lookup(".m3-state-layer");
            assertTrue(stateLayer.getOpacity() >= 0.09);

            WritableImage armedImage = snapshotImageOnFxThread(root);
            assertSnapshotAreaChanged(normalImage, armedImage, targetCell, 16);
        });
    }

    /// Returns a time cell by style class and visible text.
    private static ButtonBase cellByText(M3TimePicker picker, String styleClass, String text) {
        for (Node node : picker.lookupAll("." + styleClass)) {
            if (node instanceof ButtonBase button && button.getText().equals(text)) {
                return button;
            }
        }
        throw new AssertionError("No time cell found for " + styleClass + " with text " + text);
    }

    /// Verifies that a fixed time-cell text node is visually centered by rendered ink, not only by layout bounds.
    private static void assertCellTextInkCentered(WritableImage image, ButtonBase cell) {
        ControlVisualTestUtils.assertCellTextInkCentered(
                image,
                cell,
                CELL_TEXT_INK_CENTER_TOLERANCE,
                "time cell"
        );
    }

    /// Creates a modified key event for control behavior tests.
    private static KeyEvent modifiedKeyEvent(
            KeyCode code,
            boolean shiftDown,
            boolean controlDown
    ) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shiftDown, controlDown, false, false);
    }

    /// Creates a key event for control behavior tests.
    private static KeyEvent keyEvent(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }
}
