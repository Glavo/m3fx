// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3DatePickerSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotAreaChanged;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotHasColorVariety;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotNodeContainsContrast;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.snapshotImageOnFxThread;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.visualTestColors;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.writeVisualSnapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3DatePicker] API behavior, skin layout, and visual rendering.
@NotNullByDefault
final class M3DatePickerTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that value, displayed month, and range properties stay consistent.
    @Test
    void datePickerPropertiesKeepDisplayedMonthAndRange() {
        LocalDate selectedDate = LocalDate.of(2026, 5, 18);
        M3DatePicker picker = new M3DatePicker(selectedDate);

        assertEquals(selectedDate, picker.getValue());
        assertEquals(YearMonth.of(2026, 5), picker.getDisplayedMonth());

        picker.setMinDate(LocalDate.of(2026, 5, 10));
        picker.setMaxDate(LocalDate.of(2026, 5, 25));
        assertThrows(IllegalArgumentException.class, () -> picker.setValue(LocalDate.of(2026, 5, 8)));
        assertThrows(IllegalArgumentException.class, () -> picker.setMinDate(LocalDate.of(2026, 5, 26)));

        picker.setMaxDate(LocalDate.of(2026, 5, 17));
        assertNull(picker.getValue());
    }

    /// Verifies that date presets update selection and visible month.
    @Test
    void datePickerAppliesPreset() {
        LocalDate anchor = LocalDate.of(2026, 5, 19);
        M3DatePicker picker = new M3DatePicker();
        M3DatePreset preset = M3DatePresets.daysFrom(anchor, 7);

        picker.applyPreset(preset);

        assertEquals(anchor.plusDays(7), picker.getValue());
        assertEquals(YearMonth.from(anchor), picker.getDisplayedMonth());
        assertThrows(IllegalArgumentException.class, () -> {
            picker.setMaxDate(anchor.plusDays(3));
            picker.applyPreset(preset);
        });
    }

    /// Verifies that the skin creates reusable cells and selects dates from cell actions.
    @Test
    void datePickerSkinBuildsCalendarCellsAndSelectsDate() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            picker.setFirstDayOfWeek(DayOfWeek.MONDAY);
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 420.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(360.0, 340.0);
            picker.layout();

            assertInstanceOf(M3DatePickerSkin.class, picker.getSkin());
            assertEquals(42, picker.lookupAll("." + "m3-date-picker-day-cell").size());
            M3MenuButton yearButton = assertInstanceOf(
                    M3MenuButton.class,
                    picker.lookup("." + "m3-date-picker-year-menu-button")
            );
            assertEquals("2026", yearButton.getText());
            M3MenuButton monthButton = assertInstanceOf(
                    M3MenuButton.class,
                    picker.lookup("." + "m3-date-picker-month-menu-button")
            );
            assertEquals(12, monthButton.getItems().size());
            assertFalse(monthButton.getText().contains("..."));

            ButtonBase targetCell = dayCellForDate(picker, LocalDate.of(2026, 5, 20));
            assertNull(
                    targetCell.lookup(".m3-state-layer"),
                    "inactive day cells should not allocate Material interaction nodes"
            );
            targetCell.fire();

            assertEquals(LocalDate.of(2026, 5, 20), picker.getValue());
            assertTrue(targetCell.getStyleClass().contains("m3-date-picker-selected-day"));
            assertEquals(48.0, targetCell.getWidth(), 0.5);
            targetCell.arm();
            targetCell.applyCss();
            root.layout();
            Node stateLayer = Objects.requireNonNull(targetCell.lookup(".m3-state-layer"), "state layer");
            assertEquals(40.0, stateLayer.getBoundsInParent().getWidth(), 0.5);
            assertEquals(40.0, stateLayer.getBoundsInParent().getHeight(), 0.5);
            targetCell.disarm();

            M3MenuItem january = assertInstanceOf(M3MenuItem.class, monthButton.getItems().get(0));
            january.fire();
            assertEquals(YearMonth.of(2026, 1), picker.getDisplayedMonth());
        });
    }

    /// Verifies that same-month selection and bound changes preserve reusable cell date mappings.
    @Test
    void datePickerSkinAvoidsDateRemappingForStateOnlyChanges() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 420.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(360.0, 340.0);
            picker.layout();

            List<ButtonBase> cells = picker.lookupAll("." + "m3-date-picker-day-cell")
                    .stream()
                    .map(node -> assertInstanceOf(ButtonBase.class, node))
                    .toList();
            List<Object> dateReferences = cells.stream().map(Node::getUserData).toList();

            picker.setValue(LocalDate.of(2026, 5, 20));
            picker.setMinDate(LocalDate.of(2026, 5, 5));
            picker.setMaxDate(LocalDate.of(2026, 5, 28));

            for (int index = 0; index < cells.size(); index++) {
                assertSame(dateReferences.get(index), cells.get(index).getUserData());
            }
            assertTrue(dayCellForDate(picker, LocalDate.of(2026, 5, 20))
                    .getStyleClass().contains("m3-date-picker-selected-day"));
            assertTrue(dayCellForDate(picker, LocalDate.of(2026, 5, 4)).isDisabled());

            Object previousFirstDate = cells.get(0).getUserData();
            picker.setMaxDate(null);
            picker.setMinDate(null);
            picker.setDisplayedMonth(YearMonth.of(2026, 7));

            assertNotSame(previousFirstDate, cells.get(0).getUserData());
            assertEquals(YearMonth.of(2026, 7), YearMonth.from((LocalDate) cells.get(20).getUserData()));
        });
    }

    /// Verifies that replacing a date picker skin detaches selection listeners from the retired cell grid.
    @Test
    void datePickerSkinReplacementDetachesRetiredGrid() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 420.0, 360.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(360.0, 340.0);
            picker.layout();
            ButtonBase retiredSelectedCell = dayCellForDate(picker, LocalDate.of(2026, 5, 18));
            ButtonBase retiredTargetCell = dayCellForDate(picker, LocalDate.of(2026, 5, 20));

            FxTestUtils.replaceSkin(picker, M3DatePickerSkin::new);
            picker.setValue(LocalDate.of(2026, 5, 20));
            root.applyCss();
            picker.layout();

            assertNull(retiredSelectedCell.getScene());
            assertNull(retiredTargetCell.getScene());
            assertTrue(retiredSelectedCell.getStyleClass().contains("m3-date-picker-selected-day"));
            assertFalse(retiredTargetCell.getStyleClass().contains("m3-date-picker-selected-day"));
        });
    }

    /// Verifies that hidden adjacent month days stay allocated but not visible.
    @Test
    void datePickerCanHideAdjacentMonthDays() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker picker = new M3DatePicker();
            picker.setDisplayedMonth(YearMonth.of(2026, 2));
            picker.setShowAdjacentMonthDays(false);
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 420.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(360.0, 340.0);
            picker.layout();

            long hiddenOutsideCells = picker.lookupAll("." + "m3-date-picker-day-cell")
                    .stream()
                    .filter(node -> node.getUserData() instanceof LocalDate date
                            && !YearMonth.from(date).equals(YearMonth.of(2026, 2))
                            && !node.isVisible())
                    .count();

            assertTrue(hiddenOutsideCells > 0);
        });
    }

    /// Verifies that accessibility adjustment actions mirror keyboard date navigation.
    @Test
    void datePickerAccessibleActionsAdjustDateSelection() {
        M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));

        picker.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(LocalDate.of(2026, 5, 19), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.DECREMENT);
        assertEquals(LocalDate.of(2026, 5, 18), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.BLOCK_INCREMENT);
        assertEquals(LocalDate.of(2026, 6, 18), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.BLOCK_DECREMENT);
        assertEquals(LocalDate.of(2026, 5, 18), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, List.of(LocalDate.of(2026, 5, 25)));
        assertEquals(LocalDate.of(2026, 5, 25), picker.getValue());
        assertEquals(List.of(LocalDate.of(2026, 5, 25)),
                picker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        picker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 6, 2));
        assertEquals(YearMonth.of(2026, 6), picker.getDisplayedMonth());
    }

    /// Verifies that accessibility reveal and selection ignore dates outside selectable bounds.
    @Test
    void datePickerAccessibleActionsIgnoreDisabledDateValues() {
        M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));
        picker.setMinDate(LocalDate.of(2026, 5, 1));
        picker.setMaxDate(LocalDate.of(2026, 5, 31));
        picker.setDisplayedMonth(YearMonth.of(2026, 5));

        picker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 6, 2));

        assertEquals(YearMonth.of(2026, 5), picker.getDisplayedMonth());
        assertEquals(LocalDate.of(2026, 5, 18), picker.getValue());

        picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, LocalDate.of(2026, 6, 2));

        assertEquals(LocalDate.of(2026, 5, 18), picker.getValue());
    }

    /// Verifies that composite owners skip date pickers whose value route rejects an out-of-range target.
    @Tier2Test
    @Test
    void datePickerAccessibleRouteRejectsOutOfRangeTargetsInCompositeOwners() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker first = new M3DatePicker(LocalDate.of(2026, 5, 18));
            first.setMinDate(LocalDate.of(2026, 5, 1));
            first.setMaxDate(LocalDate.of(2026, 5, 31));
            M3DatePicker second = new M3DatePicker(LocalDate.of(2026, 6, 2));
            second.setMinDate(LocalDate.of(2026, 6, 1));
            second.setMaxDate(LocalDate.of(2026, 6, 30));
            M3FormPane form = new M3FormPane();
            form.getItems().setAll(first, second);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(form);
                Scene scene = new Scene(root, 720.0, 480.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                form.resizeRelocate(24.0, 24.0, 640.0, 400.0);
                root.layout();
                form.layout();

                form.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 6, 2));

                Node focusOwner = scene.getFocusOwner();
                assertTrue(focusOwner != null && M3Accessible.containsNode(second, focusOwner));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that accessibility selection ignores unreachable rendered day cell nodes.
    @Tier2Test
    @Test
    void datePickerAccessibleSelectionIgnoresUnreachableDayCells() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            Stage stage = new Stage();
            try {
                Pane root = new Pane(picker);
                Scene scene = new Scene(root, 420.0, 360.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                ButtonBase targetCell = dayCellForDate(picker, LocalDate.of(2026, 5, 25));
                targetCell.setVisible(false);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, targetCell);

                assertEquals(LocalDate.of(2026, 5, 18), picker.getValue());

                targetCell.setVisible(true);
                targetCell.setDisable(true);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, targetCell);

                assertEquals(LocalDate.of(2026, 5, 18), picker.getValue());

                targetCell.setDisable(false);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, targetCell);

                assertEquals(LocalDate.of(2026, 5, 25), picker.getValue());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that default accessibility focus actions preserve the focused visible day cell.
    @Tier2Test
    @Test
    void datePickerAccessibleFocusPreservesFocusedVisibleCell() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            picker.setFirstDayOfWeek(DayOfWeek.MONDAY);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(picker);
                Scene scene = new Scene(root, 420.0, 360.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                picker.resize(360.0, 340.0);
                root.layout();
                picker.layout();

                ButtonBase focusedCell = dayCellForDate(picker, LocalDate.of(2026, 5, 20));
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

    /// Verifies that the date picker renders a non-empty Material-colored calendar surface.
    @Tier2Test
    @Test
    void datePickerSnapshotRendersCalendarSurface() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker selected = new M3DatePicker(LocalDate.of(2026, 5, 18));
            M3DatePicker bounded = new M3DatePicker(LocalDate.of(2026, 5, 22));
            bounded.setMinDate(LocalDate.of(2026, 5, 12));
            bounded.setMaxDate(LocalDate.of(2026, 5, 28));

            HBox row = new HBox(20.0, selected, bounded);
            row.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(row, 760.0, 420.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            row.applyCss();
            row.resize(760.0, 420.0);
            row.layout();

            WritableImage image = snapshotImageOnFxThread(row);
            assertSnapshotHasColorVariety(image, 8);
            assertSnapshotNodeContainsContrast(image, selected, Color.WHITE, 0.04);
            assertSnapshotNodeContainsContrast(image, dayCellForDate(selected, LocalDate.of(2026, 5, 18)), Color.WHITE, 0.08);
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-picker.png"
            ));
        });
    }

    /// Verifies that an armed day cell shows the Material state layer.
    @Test
    void datePickerDayCellShowsArmedStateLayer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DatePicker picker = new M3DatePicker(LocalDate.of(2026, 5, 18));
            Pane root = new Pane(picker);
            root.setStyle("-fx-background-color: white; -fx-padding: 20px; " + visualTestColors());
            Scene scene = new Scene(root, 420.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(360.0, 340.0);
            root.resize(420.0, 360.0);
            root.layout();

            ButtonBase targetCell = dayCellForDate(picker, LocalDate.of(2026, 5, 20));
            WritableImage normalImage = snapshotImageOnFxThread(root);

            targetCell.arm();
            targetCell.applyCss();
            targetCell.requestLayout();
            root.layout();

            Node stateLayer = targetCell.lookup(".m3-state-layer");
            assertTrue(stateLayer.getOpacity() >= 0.09);

            WritableImage armedImage = snapshotImageOnFxThread(root);
            assertSnapshotAreaChanged(normalImage, armedImage, targetCell, 16);
            targetCell.disarm();
        });
    }

    /// Returns a day cell by date.
    private static ButtonBase dayCellForDate(M3DatePicker picker, LocalDate date) {
        for (Node node : picker.lookupAll("." + "m3-date-picker-day-cell")) {
            if (node instanceof ButtonBase button && date.equals(button.getUserData())) {
                return button;
            }
        }
        throw new AssertionError("No date cell found for " + date);
    }

}