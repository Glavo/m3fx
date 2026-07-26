// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3DatePickerSkin;
import org.glavo.m3fx.skins.M3DateRangePickerSkin;
import org.glavo.m3fx.skins.M3TimePickerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/// Tests the indexed accessibility presentation shared by picker controls and their default skins.
@NotNullByDefault
final class M3PickerAccessibilityPresentationTest {
    /// Starts the JavaFX toolkit before controls and skins are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that date-picker accessibility follows the rendered month and releases a replaced skin.
    @Test
    void datePickerPresentationTracksRenderedCellsAndSkinReplacement() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePicker picker = new M3DatePicker();
            picker.setDisplayedMonth(YearMonth.of(2026, 5));
            layout(picker, 360.0, 340.0);

            assertEquals(42, itemCount(picker));
            assertAllItemsAreNodes(picker);
            Node firstCell = itemAt(picker, 0);
            assertSame(firstCell, itemAt(picker, 0));
            assertInstanceOf(LocalDate.class, firstCell.getUserData());

            picker.setShowAdjacentMonthDays(false);
            assertEquals(31, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertEquals(LocalDate.of(2026, 5, 1), itemAt(picker, 0).getUserData());
            assertNull(picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 31));

            picker.setDisplayedMonth(YearMonth.of(2026, 6));
            assertEquals(30, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertEquals(LocalDate.of(2026, 6, 1), itemAt(picker, 0).getUserData());

            FxTestUtils.replaceSkin(picker, EmptySkin::new);
            assertEquals(0, itemCount(picker));
            assertNull(picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertNull(firstCell.getScene());

            FxTestUtils.replaceSkin(picker, M3DatePickerSkin::new);
            assertEquals(30, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertEquals(LocalDate.of(2026, 6, 1), itemAt(picker, 0).getUserData());
        });
    }

    /// Verifies that range-picker accessibility never substitutes model dates for missing rendered nodes.
    @Test
    void dateRangePickerPresentationReturnsOnlyRenderedNodes() {
        FxTestUtils.runOnFxThread(() -> {
            M3DateRangePicker picker = new M3DateRangePicker();
            picker.setDisplayedMonth(YearMonth.of(2026, 5));
            layout(picker, 360.0, 340.0);

            assertEquals(42, itemCount(picker));
            assertAllItemsAreNodes(picker);
            Node firstCell = itemAt(picker, 0);
            assertSame(firstCell, itemAt(picker, 0));
            assertInstanceOf(LocalDate.class, firstCell.getUserData());

            picker.setShowAdjacentMonthDays(false);
            assertEquals(31, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertEquals(LocalDate.of(2026, 5, 1), itemAt(picker, 0).getUserData());

            FxTestUtils.replaceSkin(picker, EmptySkin::new);
            assertEquals(0, itemCount(picker));
            assertNull(picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertNull(firstCell.getScene());

            FxTestUtils.replaceSkin(picker, M3DateRangePickerSkin::new);
            assertEquals(31, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertEquals(LocalDate.of(2026, 5, 1), itemAt(picker, 0).getUserData());
        });
    }

    /// Verifies that time-picker accessibility follows Dial/Input and clock-format presentation changes.
    @Test
    void timePickerPresentationTracksModeAndClockFormat() {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
            layout(picker, 328.0, 472.0);

            assertEquals(14, itemCount(picker));
            assertAllItemsAreNodes(picker);
            Node firstCell = itemAt(picker, 0);
            assertSame(firstCell, itemAt(picker, 0));
            assertInstanceOf(LocalTime.class, firstCell.getUserData());

            picker.setInputMode(true);
            assertEquals(2, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertInstanceOf(Node.class, picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 1));
            assertNull(picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 2));

            picker.setUse24HourClock(true);
            assertEquals(0, itemCount(picker));
            assertNull(picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

            picker.setInputMode(false);
            assertEquals(24, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertInstanceOf(LocalTime.class, itemAt(picker, 0).getUserData());

            FxTestUtils.replaceSkin(picker, EmptySkin::new);
            assertEquals(0, itemCount(picker));
            assertNull(picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));
            assertNull(firstCell.getScene());

            FxTestUtils.replaceSkin(picker, M3TimePickerSkin::new);
            assertEquals(24, itemCount(picker));
            assertAllItemsAreNodes(picker);
            assertInstanceOf(Node.class, picker.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 23));
        });
    }

    /// Installs a control in a scene, applies CSS, and lays out its default skin.
    ///
    /// @param control the picker control
    /// @param width   the control width
    /// @param height  the control height
    private static void layout(Control control, double width, double height) {
        Pane root = new Pane(control);
        new Scene(root, width + 48.0, height + 48.0);
        root.applyCss();
        control.resizeRelocate(24.0, 24.0, width, height);
        root.layout();
        control.layout();
    }

    /// Returns the indexed item count exposed by a picker control.
    ///
    /// @param control the picker control
    /// @return the exposed item count
    private static int itemCount(Control control) {
        return (Integer) control.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT);
    }

    /// Returns one indexed accessible item and verifies that it is a node.
    ///
    /// @param control the picker control
    /// @param index   the item index
    /// @return the rendered accessible node
    private static Node itemAt(Control control, int index) {
        return assertInstanceOf(
                Node.class,
                control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index)
        );
    }

    /// Verifies that every indexed accessibility item exposed by a control is a node.
    ///
    /// @param control the picker control
    private static void assertAllItemsAreNodes(Control control) {
        int count = itemCount(control);
        for (int index = 0; index < count; index++) {
            itemAt(control, index);
        }
        assertNull(control.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, count));
    }

    /// A custom skin that deliberately provides no indexed picker presentation.
    ///
    /// @param <C> the skinnable control type
    @NotNullByDefault
    private static final class EmptySkin<C extends Control> extends SkinBase<C> {
        /// Creates an empty skin for the supplied control.
        ///
        /// @param control the skinnable control
        private EmptySkin(C control) {
            super(control);
        }
    }
}
