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
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.skins.M3TimePickerSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;

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
        runOnFxThread(() -> {
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

    /// Verifies 24-hour mode, minute-step rendering, and bounded range disabled states.
    @Test
    void timePickerSupportsTwentyFourHourModeAndRanges() {
        runOnFxThread(() -> {
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
        });
    }

    /// Verifies keyboard navigation changes the selected time without relying on a skin internals.
    @Test
    void timePickerHandlesKeyboardNavigation() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
        picker.setMinuteStep(15);

        picker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(LocalTime.of(11, 30), picker.getValue());

        picker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
        assertEquals(LocalTime.of(11, 45), picker.getValue());

        picker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertEquals(LocalTime.of(10, 45), picker.getValue());

        picker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.DOWN));
        assertEquals(LocalTime.of(10, 30), picker.getValue());
    }

    /// Verifies that horizontal keyboard navigation follows visual order in right-to-left layouts.
    @Test
    void timePickerMirrorsHorizontalKeyboardNavigationInRightToLeftLayouts() {
        M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 30));
        picker.setMinuteStep(15);
        picker.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        picker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.RIGHT));
        assertEquals(LocalTime.of(9, 30), picker.getValue());

        picker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertEquals(LocalTime.of(10, 30), picker.getValue());

        picker.fireEvent(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.UP));
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

    /// Verifies that default accessibility focus actions preserve the focused visible time cell.
    @Test
    void timePickerAccessibleFocusPreservesFocusedVisibleCell() {
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
            boolean previousAnimationsEnabled = M3MotionSettings.areAnimationsEnabled();
            M3MotionSettings.setAnimationsEnabled(false);
            try {
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
            } finally {
                M3MotionSettings.setAnimationsEnabled(previousAnimationsEnabled);
            }
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

    /// Runs a task on the FX application thread and propagates failures.
    private static void runOnFxThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }

        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                failure.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        Throwable exception = failure.get();
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (exception instanceof Error error) {
            throw error;
        }
        if (exception != null) {
            throw new AssertionError(exception);
        }
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

    /// Creates a key event for control behavior tests.
    private static KeyEvent keyEvent(EventType<KeyEvent> eventType, KeyCode code) {
        return new KeyEvent(eventType, "", "", code, false, false, false, false);
    }
}
