// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNullByDefault;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Provides shared visual snapshot assertions for control tests.
@NotNullByDefault
final class ControlVisualTestUtils {
    /// Prevents instantiation of this utility class.
    private ControlVisualTestUtils() {
    }

    /// Returns high-contrast color tokens used by snapshot-based visual tests.
    static String visualTestColors() {
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
    static WritableImage snapshotImageOnFxThread(Node node) {
        WritableImage image = new WritableImage(
                (int) Math.ceil(node.getLayoutBounds().getWidth()),
                (int) Math.ceil(node.getLayoutBounds().getHeight())
        );
        node.snapshot(null, image);
        return image;
    }

    /// Verifies that a rendered snapshot contains enough distinct visible colors.
    static void assertSnapshotHasColorVariety(WritableImage image, int minimumColorCount) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                int argb = image.getPixelReader().getArgb(x, y);
                if (((argb >>> 24) & 0xff) > 16) {
                    colors.add(argb & 0xf0f0f0f0);
                }
            }
        }

        assertTrue(colors.size() >= minimumColorCount,
                () -> "snapshotColorCount=" + colors.size() + ", minimum=" + minimumColorCount);
    }

    /// Verifies that a node's rendered bounds contain pixels that contrast with a reference color.
    static void assertSnapshotNodeContainsContrast(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
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
    static void assertSnapshotAreaChanged(
            WritableImage before,
            WritableImage after,
            Node node,
            int minimumChangedPixels
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
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

    /// Verifies that a fixed cell text node is visually centered by rendered ink, not only by layout bounds.
    static void assertCellTextInkCentered(
            WritableImage image,
            ButtonBase cell,
            double tolerance,
            String description
    ) {
        Text text = assertInstanceOf(Text.class, cell.lookup(".text"));
        Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
        Rectangle2D textInkBounds = contrastingPixelBounds(image, text, sampledNodeBackgroundColor(image, cell), 0.04);
        Point2D cellCenter = new Point2D(
                (cellBounds.getMinX() + cellBounds.getMaxX()) / 2.0,
                (cellBounds.getMinY() + cellBounds.getMaxY()) / 2.0
        );
        Point2D inkCenter = new Point2D(
                textInkBounds.getMinX() + textInkBounds.getWidth() / 2.0,
                textInkBounds.getMinY() + textInkBounds.getHeight() / 2.0
        );

        assertEquals(cellCenter.getX(), inkCenter.getX(), tolerance,
                () -> description + " text ink is horizontally off-center: cell="
                        + cell + ", cellBounds=" + cellBounds + ", inkBounds=" + textInkBounds);
        assertEquals(cellCenter.getY(), inkCenter.getY(), tolerance,
                () -> description + " text ink is vertically off-center: cell="
                        + cell + ", cellBounds=" + cellBounds + ", inkBounds=" + textInkBounds);
        assertRectangleInsideNodeBounds(cell, textInkBounds, 1.0, description + " text ink leaves cell bounds");
    }

    /// Writes a rendered snapshot to a build report path for manual visual inspection.
    static void writeVisualSnapshot(WritableImage image, Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ImageIO.write(toBufferedImage(image), "png", path.toFile());
            writeVisualSnapshotIndex(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Writes a lightweight HTML index for generated core visual snapshots.
    static void writeVisualSnapshotIndex(Path snapshotPath) throws IOException {
        Path directory = snapshotPath.getParent();
        if (directory == null) {
            return;
        }

        List<Path> snapshots;
        try (var stream = Files.list(directory)) {
            snapshots = stream
                    .filter(path -> path.getFileName().toString().endsWith(".png"))
                    .sorted()
                    .toList();
        }

        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>M3FX Core Visual Snapshots</title>
                  <style>
                    body { margin: 24px; font-family: system-ui, sans-serif; background: #fdf8ff; color: #1d1b20; }
                    h1 { margin: 0 0 16px; font-size: 28px; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
                    figure { margin: 0; border: 1px solid #cac4d0; border-radius: 8px; padding: 12px; background: #fff7ff; }
                    img { display: block; width: 100%; height: auto; border-radius: 4px; background: #f7f2fa; }
                    figcaption { margin-top: 8px; font-size: 13px; word-break: break-all; }
                  </style>
                </head>
                <body>
                  <h1>M3FX Core Visual Snapshots</h1>
                  <div class="grid">
                """);
        for (Path snapshot : snapshots) {
            appendVisualSnapshotFigure(html, snapshot);
        }
        html.append("""
                  </div>
                </body>
                </html>
                """);
        Path indexPath = directory.resolve("index.html");
        Files.writeString(indexPath, html.toString());
    }

    /// Appends one linked snapshot figure to the visual report index.
    private static void appendVisualSnapshotFigure(StringBuilder html, Path snapshot) {
        String fileName = snapshot.getFileName().toString();
        html.append("    <figure><a href=\"")
                .append(escapeHtml(fileName))
                .append("\"><img src=\"")
                .append(escapeHtml(fileName))
                .append("\" alt=\"")
                .append(escapeHtml(fileName))
                .append("\"></a><figcaption>")
                .append(escapeHtml(fileName))
                .append("</figcaption></figure>\n");
    }

    /// Escapes text for HTML content and attributes.
    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /// Verifies that a rendered pixel rectangle is contained by a node in scene coordinates.
    private static void assertRectangleInsideNodeBounds(
            Node node,
            Rectangle2D rectangle,
            double tolerance,
            String description
    ) {
        Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(rectangle.getMinX() >= nodeBounds.getMinX() - tolerance
                        && rectangle.getMaxX() <= nodeBounds.getMaxX() + tolerance
                        && rectangle.getMinY() >= nodeBounds.getMinY() - tolerance
                        && rectangle.getMaxY() <= nodeBounds.getMaxY() + tolerance,
                () -> description + ": node=" + node
                        + ", nodeBounds=" + nodeBounds
                        + ", rectangle=" + rectangle);
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

    /// Returns the bounds of rendered pixels inside a node that contrast with the reference color.
    private static Rectangle2D contrastingPixelBounds(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int startX = Math.max(0, (int) Math.floor(bounds.getMinX()));
        int startY = Math.max(0, (int) Math.floor(bounds.getMinY()));
        int endX = Math.min((int) image.getWidth(), (int) Math.ceil(bounds.getMaxX()));
        int endY = Math.min((int) image.getHeight(), (int) Math.ceil(bounds.getMaxY()));
        int minX = endX;
        int minY = endY;
        int maxX = startX - 1;
        int maxY = startY - 1;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                if (color.getOpacity() > 0.1 && colorDistance(color, reference) >= minimumDistance) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        assertTrue(maxX >= minX && maxY >= minY, () -> "No contrasting pixels found for " + node);
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Samples a rendered background color near the local origin of a node.
    private static Color sampledNodeBackgroundColor(WritableImage image, Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int x = Math.max(0, Math.min((int) image.getWidth() - 1, (int) Math.floor(bounds.getMinX())));
        int y = Math.max(0, Math.min((int) image.getHeight() - 1, (int) Math.floor(bounds.getMinY())));
        return image.getPixelReader().getColor(x, y);
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

    /// Returns a simple RGB distance between two colors.
    private static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }
}
