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
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3DateRangePickerSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

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

    /// Returns high-contrast color tokens used by snapshot-based visual tests.
    private static String visualTestColors() {
        return "-m3-color-primary: rgb(84, 50, 185); "
                + "-m3-color-on-primary: white; "
                + "-m3-color-secondary-container: rgb(222, 214, 250); "
                + "-m3-color-on-secondary-container: rgb(40, 27, 92); "
                + "-m3-color-outline: rgb(95, 91, 105); "
                + "-m3-color-surface-container-low: rgb(247, 242, 250); "
                + "-m3-color-surface-container-high: rgb(236, 230, 240); "
                + "-m3-color-surface-container-highest: rgb(228, 221, 234); "
                + "-m3-color-surface-container: rgb(243, 237, 247); "
                + "-m3-color-surface: white; "
                + "-m3-color-outline-variant: rgb(202, 196, 208); "
                + "-m3-color-primary-container: rgb(226, 221, 255); "
                + "-m3-color-on-primary-container: rgb(36, 14, 110); "
                + "-m3-color-tertiary-container: rgb(255, 216, 228); "
                + "-m3-color-on-tertiary-container: rgb(95, 17, 48); "
                + "-m3-color-on-surface: rgb(30, 28, 32); "
                + "-m3-color-on-surface-variant: rgb(73, 69, 79); "
                + "-m3-color-inverse-surface: rgb(49, 48, 51); "
                + "-m3-color-inverse-on-surface: rgb(244, 239, 244); "
                + "-m3-color-inverse-primary: rgb(207, 189, 255); "
                + "-m3-color-error: rgb(186, 26, 26); "
                + "-m3-color-on-error: white; "
                + "-m3-color-error-container: rgb(255, 218, 214); "
                + "-m3-color-on-error-container: rgb(65, 0, 2);";
    }

    /// Returns a rendered image snapshot from a node on the FX thread.
    private static WritableImage snapshotImageOnFxThread(Node node) {
        WritableImage image = new WritableImage(
                (int) Math.ceil(node.getLayoutBounds().getWidth()),
                (int) Math.ceil(node.getLayoutBounds().getHeight())
        );
        node.snapshot(null, image);
        return image;
    }

    /// Verifies that a rendered snapshot contains enough distinct visible colors.
    private static void assertSnapshotHasColorVariety(WritableImage image, int minimumColorCount) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                int argb = image.getPixelReader().getArgb(x, y);
                if (((argb >>> 24) & 0xff) > 16) {
                    colors.add(quantizedArgb(argb));
                }
            }
        }

        assertTrue(colors.size() >= minimumColorCount,
                () -> "snapshotColorCount=" + colors.size() + ", minimum=" + minimumColorCount);
    }

    /// Verifies that a node's rendered bounds contain pixels that contrast with a reference color.
    private static void assertSnapshotNodeContainsContrast(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        var bounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(snapshotAreaContainsContrast(
                image,
                (int) Math.floor(bounds.getMinX()),
                (int) Math.floor(bounds.getMinY()),
                (int) Math.ceil(bounds.getMaxX()),
                (int) Math.ceil(bounds.getMaxY()),
                reference,
                minimumDistance
        ), () -> "No contrasting pixels found for " + node);
    }

    /// Verifies that a node's rendered bounds changed between two snapshots.
    private static void assertSnapshotAreaChanged(
            WritableImage before,
            WritableImage after,
            Node node,
            int minimumChangedPixels
    ) {
        var bounds = node.localToScene(node.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) before.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) before.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        int changedPixels = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                if (before.getPixelReader().getArgb(x, y) != after.getPixelReader().getArgb(x, y)) {
                    changedPixels++;
                }
            }
        }

        int finalChangedPixels = changedPixels;
        assertTrue(finalChangedPixels >= minimumChangedPixels,
                () -> "changedPixels=" + finalChangedPixels + ", minimum=" + minimumChangedPixels);
    }

    /// Returns whether a snapshot area contains pixels that contrast with a reference color.
    private static boolean snapshotAreaContainsContrast(
            WritableImage image,
            int minX,
            int minY,
            int maxX,
            int maxY,
            Color reference,
            double minimumDistance
    ) {
        int startX = Math.max(0, minX);
        int startY = Math.max(0, minY);
        int endX = Math.min((int) image.getWidth(), maxX);
        int endY = Math.min((int) image.getHeight(), maxY);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, reference) >= minimumDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Writes a rendered snapshot to a build report path for manual visual inspection.
    private static void writeVisualSnapshot(WritableImage image, java.nio.file.Path path) {
        try {
            java.nio.file.Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ImageIO.write(toBufferedImage(image), "png", path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Converts a JavaFX image snapshot to a desktop image for report output.
    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        return bufferedImage;
    }

    /// Returns a quantized ARGB value that keeps color variety checks stable across renderers.
    private static int quantizedArgb(int argb) {
        return argb & 0xf0f0f0f0;
    }

    /// Returns a simple RGB distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }
}
