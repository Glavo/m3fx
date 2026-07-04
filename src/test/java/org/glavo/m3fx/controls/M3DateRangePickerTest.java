// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.css.PseudoClass;
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
import org.glavo.m3fx.skins.M3DateRangePickerSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotAreaChanged;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotHasColorVariety;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.assertSnapshotNodeContainsContrast;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.snapshotImageOnFxThread;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.visualTestColors;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.writeVisualSnapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests [M3DateRangePicker] API behavior, skin layout, and visual rendering.
@NotNullByDefault
final class M3DateRangePickerTest {
    /// The pseudo-class used to verify pressed-like day cell feedback in visual tests.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The tolerance used when comparing rendered cell text ink centers.
    private static final double CELL_TEXT_INK_CENTER_TOLERANCE = 1.5;

    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies selected range properties, range ordering, and selectable bounds.
    @Test
    void dateRangePickerPropertiesValidateRangeAndBounds() {
        M3DateRangePicker picker = new M3DateRangePicker(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 22)
        );

        assertEquals(LocalDate.of(2026, 5, 18), picker.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 22), picker.getEndDate());
        assertEquals(YearMonth.of(2026, 5), picker.getDisplayedMonth());
        assertTrue(picker.isRangeComplete());
        assertTrue(picker.isDateInSelectedRange(LocalDate.of(2026, 5, 20)));

        assertThrows(IllegalArgumentException.class, () ->
                picker.setRange(LocalDate.of(2026, 5, 22), LocalDate.of(2026, 5, 18)));
        assertThrows(IllegalArgumentException.class, () ->
                new M3DateRangePicker().setEndDate(LocalDate.of(2026, 5, 18)));

        picker.setMinDate(LocalDate.of(2026, 5, 19));
        assertNull(picker.getStartDate());
        assertNull(picker.getEndDate());
    }

    /// Verifies that incremental selection starts, completes, and restarts the selected range.
    @Test
    void dateRangePickerSelectDateBuildsNormalizedRange() {
        M3DateRangePicker picker = new M3DateRangePicker();

        picker.selectDate(LocalDate.of(2026, 5, 20));
        assertEquals(LocalDate.of(2026, 5, 20), picker.getStartDate());
        assertNull(picker.getEndDate());

        picker.selectDate(LocalDate.of(2026, 5, 18));
        assertEquals(LocalDate.of(2026, 5, 18), picker.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 20), picker.getEndDate());

        picker.selectDate(LocalDate.of(2026, 5, 25));
        assertEquals(LocalDate.of(2026, 5, 25), picker.getStartDate());
        assertNull(picker.getEndDate());
    }

    /// Verifies that the range picker skin marks range start, middle, and end day cells.
    @Test
    void dateRangePickerSkinBuildsCalendarCellsAndRangeStates() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePicker picker = new M3DateRangePicker();
            picker.setDisplayedMonth(YearMonth.of(2026, 5));
            picker.setFirstDayOfWeek(DayOfWeek.MONDAY);
            Pane root = new Pane(picker);
            Scene scene = new Scene(root, 420.0, 360.0);

            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            picker.resize(360.0, 340.0);
            picker.layout();

            assertInstanceOf(M3DateRangePickerSkin.class, picker.getSkin());
            assertEquals(42, picker.lookupAll("." + M3DatePicker.DAY_CELL_STYLE_CLASS).size());

            ButtonBase startCell = dayCellForDate(picker, LocalDate.of(2026, 5, 18));
            ButtonBase middleCell = dayCellForDate(picker, LocalDate.of(2026, 5, 20));
            ButtonBase endCell = dayCellForDate(picker, LocalDate.of(2026, 5, 22));
            startCell.fire();
            endCell.fire();

            assertEquals(LocalDate.of(2026, 5, 18), picker.getStartDate());
            assertEquals(LocalDate.of(2026, 5, 22), picker.getEndDate());
            assertTrue(startCell.getStyleClass().contains(M3DateRangePicker.RANGE_START_DAY_STYLE_CLASS));
            assertTrue(middleCell.getStyleClass().contains(M3DateRangePicker.RANGE_MIDDLE_DAY_STYLE_CLASS));
            assertTrue(endCell.getStyleClass().contains(M3DateRangePicker.RANGE_END_DAY_STYLE_CLASS));
        });
    }

    /// Verifies that accessibility actions can adjust and replace the selected date range.
    @Test
    void dateRangePickerAccessibleActionsAdjustRangeSelection() {
        M3DateRangePicker picker = new M3DateRangePicker();
        picker.setDisplayedMonth(YearMonth.of(2026, 1));

        picker.executeAccessibleAction(AccessibleAction.INCREMENT);
        assertEquals(LocalDate.of(2026, 1, 2), picker.getStartDate());
        assertNull(picker.getEndDate());

        picker.executeAccessibleAction(AccessibleAction.DECREMENT);
        assertEquals(LocalDate.of(2026, 1, 1), picker.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 2), picker.getEndDate());

        picker.executeAccessibleAction(
                AccessibleAction.SET_SELECTED_ITEMS,
                List.of(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 10))
        );
        assertEquals(LocalDate.of(2026, 1, 10), picker.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 12), picker.getEndDate());
        assertEquals(List.of(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 12)),
                picker.queryAccessibleAttribute(AccessibleAttribute.SELECTED_ITEMS));

        picker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 2, 5));
        assertEquals(YearMonth.of(2026, 2), picker.getDisplayedMonth());
    }

    /// Verifies that accessibility reveal and selection ignore range dates outside selectable bounds.
    @Test
    void dateRangePickerAccessibleActionsIgnoreDisabledDateValues() {
        M3DateRangePicker picker = new M3DateRangePicker(
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12)
        );
        picker.setMinDate(LocalDate.of(2026, 5, 1));
        picker.setMaxDate(LocalDate.of(2026, 5, 31));
        picker.setDisplayedMonth(YearMonth.of(2026, 5));

        picker.executeAccessibleAction(AccessibleAction.SHOW_ITEM, LocalDate.of(2026, 6, 2));

        assertEquals(YearMonth.of(2026, 5), picker.getDisplayedMonth());
        assertEquals(LocalDate.of(2026, 5, 10), picker.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 12), picker.getEndDate());

        picker.executeAccessibleAction(
                AccessibleAction.SET_SELECTED_ITEMS,
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 4)
        );

        assertEquals(LocalDate.of(2026, 5, 10), picker.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 12), picker.getEndDate());
    }

    /// Verifies that composite owners skip range pickers whose value route rejects an out-of-range target.
    @Test
    void dateRangePickerAccessibleRouteRejectsOutOfRangeTargetsInCompositeOwners() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePicker first = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 10),
                    LocalDate.of(2026, 5, 12)
            );
            first.setMinDate(LocalDate.of(2026, 5, 1));
            first.setMaxDate(LocalDate.of(2026, 5, 31));
            M3DateRangePicker second = new M3DateRangePicker(
                    LocalDate.of(2026, 6, 2),
                    LocalDate.of(2026, 6, 4)
            );
            second.setMinDate(LocalDate.of(2026, 6, 1));
            second.setMaxDate(LocalDate.of(2026, 6, 30));
            M3FormPane form = new M3FormPane();
            form.getItems().setAll(first, second);
            Stage stage = new Stage();
            try {
                Pane root = new Pane(form);
                Scene scene = new Scene(root, 720.0, 620.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                form.resizeRelocate(24.0, 24.0, 640.0, 540.0);
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

    /// Verifies that accessibility selection ignores unreachable rendered range day cell nodes.
    @Test
    void dateRangePickerAccessibleSelectionIgnoresUnreachableDayCells() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePicker picker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 10),
                    LocalDate.of(2026, 5, 12)
            );
            Stage stage = new Stage();
            try {
                Pane root = new Pane(picker);
                Scene scene = new Scene(root, 420.0, 360.0);

                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                ButtonBase startCell = dayCellForDate(picker, LocalDate.of(2026, 5, 18));
                ButtonBase endCell = dayCellForDate(picker, LocalDate.of(2026, 5, 22));
                startCell.setVisible(false);
                endCell.setDisable(true);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, startCell, endCell);

                assertEquals(LocalDate.of(2026, 5, 10), picker.getStartDate());
                assertEquals(LocalDate.of(2026, 5, 12), picker.getEndDate());

                startCell.setVisible(true);
                endCell.setDisable(false);
                picker.executeAccessibleAction(AccessibleAction.SET_SELECTED_ITEMS, startCell, endCell);

                assertEquals(LocalDate.of(2026, 5, 18), picker.getStartDate());
                assertEquals(LocalDate.of(2026, 5, 22), picker.getEndDate());
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that default accessibility focus actions preserve the focused visible day cell.
    @Test
    void dateRangePickerAccessibleFocusPreservesFocusedVisibleCell() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePicker picker = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 22)
            );
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

    /// Verifies that the date range picker renders selected endpoint and in-range states.
    @Test
    void dateRangePickerSnapshotRendersRangeStates() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePicker selected = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 18),
                    LocalDate.of(2026, 5, 22)
            );
            M3DateRangePicker bounded = new M3DateRangePicker(
                    LocalDate.of(2026, 5, 24),
                    LocalDate.of(2026, 5, 28)
            );
            bounded.setMinDate(LocalDate.of(2026, 5, 12));
            bounded.setMaxDate(LocalDate.of(2026, 6, 4));

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
            assertSnapshotNodeContainsContrast(
                    image,
                    dayCellForDate(selected, LocalDate.of(2026, 5, 20)),
                    Color.WHITE,
                    0.08
            );
            assertCellTextInkCentered(image, dayCellForDate(selected, LocalDate.of(2026, 5, 18)));
            assertCellTextInkCentered(image, dayCellForDate(selected, LocalDate.of(2026, 5, 20)));
            assertCellTextInkCentered(image, dayCellForDate(selected, LocalDate.of(2026, 5, 22)));
            assertCellTextInkCentered(image, dayCellForDate(bounded, LocalDate.of(2026, 5, 24)));
            assertCellTextInkCentered(image, dayCellForDate(bounded, LocalDate.of(2026, 5, 28)));
            writeVisualSnapshot(image, java.nio.file.Path.of(
                    "build",
                    "reports",
                    "m3fx-visual",
                    "visual-date-range-picker.png"
            ));
        });
    }

    /// Verifies that an armed range-picker day cell shows the Material state layer.
    @Test
    void dateRangePickerDayCellShowsArmedStateLayer() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DateRangePicker picker = new M3DateRangePicker();
            picker.setDisplayedMonth(YearMonth.of(2026, 5));
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

    /// Returns a day cell by date.
    private static ButtonBase dayCellForDate(M3DateRangePicker picker, LocalDate date) {
        for (Node node : picker.lookupAll("." + M3DatePicker.DAY_CELL_STYLE_CLASS)) {
            if (node instanceof ButtonBase button && date.equals(button.getUserData())) {
                return button;
            }
        }
        throw new AssertionError("No date cell found for " + date);
    }

    /// Verifies that a fixed day-cell text node is visually centered by rendered ink, not only by layout bounds.
    private static void assertCellTextInkCentered(WritableImage image, ButtonBase cell) {
        ControlVisualTestUtils.assertCellTextInkCentered(
                image,
                cell,
                CELL_TEXT_INK_CENTER_TOLERANCE,
                "date range cell"
        );
    }
}