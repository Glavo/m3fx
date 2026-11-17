// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.imageio.ImageIO;

import static org.glavo.m3fx.controls.ControlLayoutAssertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Provides shared pixel assertions and snapshot reports for control tests.
@NotNullByDefault
final class ControlVisualTestUtils {
    /// Prevents instantiation of this utility class.
    private ControlVisualTestUtils() {
    }

    /// The edge tolerance used when comparing rendered text ink against visual owners.
    static final double TEXT_INK_EDGE_TOLERANCE = 1.0;

    /// The largest accepted vertical antialiasing overflow for rendered text ink.
    static final double TEXT_INK_VERTICAL_OVERFLOW_TOLERANCE = 1.0;

    /// The minimum rendered ink height expected for visible alphanumeric text.
    static final double TEXT_INK_MINIMUM_FONT_HEIGHT_RATIO = 0.42;

    /// The pixel tolerance used by rendered text clipping checks.
    static final double TEXT_INK_HEIGHT_CLIPPING_TOLERANCE = 1.0;

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

    /// Verifies that rendered text nodes do not escape their nearest visual layout boundary.
    static void assertRenderedTextNodesStayInsideLayout(Node root) {
        WritableImage image = snapshotImageOnFxThread(root);
        visitVisibleNodes(root, node -> {
            if (!(node instanceof Text text) || text.getText().isBlank() || !hasRenderableBounds(text)) {
                return;
            }

            Bounds textBounds = text.localToScene(text.getBoundsInLocal());
            Node boundary = nearestVisualBoundary(text, root);
            assertNodeInsideAncestor(boundary, text, 1.0);

            @Nullable Rectangle2D inkBounds = renderedTextInkBounds(image, text);
            if (inkBounds == null) {
                return;
            }

            assertRenderedTextInkHasVerticalRoom(text, textBounds, inkBounds);
            assertRectangleInsideNodeBounds(boundary, inkBounds, TEXT_INK_EDGE_TOLERANCE,
                    "rendered text ink leaves its nearest visual owner");
            assertRenderedTextInkHasExpectedHeight(text, textBounds, inkBounds);
        });
    }

    /// Verifies that rendered text ink does not materially overflow the text layout bounds.
    static void assertRenderedTextInkHasVerticalRoom(
            Text text,
            Bounds textBounds,
            Rectangle2D inkBounds
    ) {
        double topRoom = inkBounds.getMinY() - textBounds.getMinY();
        double bottomRoom = textBounds.getMaxY() - inkBounds.getMaxY();
        assertTrue(topRoom >= -TEXT_INK_VERTICAL_OVERFLOW_TOLERANCE
                        && bottomRoom >= -TEXT_INK_VERTICAL_OVERFLOW_TOLERANCE,
                () -> "rendered text ink materially overflows its vertical layout edge: text="
                        + text.getText() + ", topRoom=" + topRoom + ", bottomRoom=" + bottomRoom
                        + ", textBounds=" + textBounds + ", inkBounds=" + inkBounds);
    }

    /// Verifies that rendered text ink keeps enough height to catch vertical clipping inside layout bounds.
    static void assertRenderedTextInkHasExpectedHeight(
            Text text,
            Bounds textBounds,
            Rectangle2D inkBounds
    ) {
        if (!shouldCheckRenderedTextInkHeight(text)
                || inkBounds.getWidth() < Math.max(24.0, textBounds.getWidth() * 0.35)) {
            return;
        }

        double minimumInkHeight = Math.max(3.0, text.getFont().getSize() * TEXT_INK_MINIMUM_FONT_HEIGHT_RATIO);
        assertTrue(inkBounds.getHeight() + TEXT_INK_HEIGHT_CLIPPING_TOLERANCE >= minimumInkHeight,
                () -> "rendered text ink is too short for its font size and may be clipped: text="
                        + text.getText() + ", fontSize=" + text.getFont().getSize()
                        + ", minimumInkHeight=" + minimumInkHeight + ", inkBounds=" + inkBounds);
    }

    /// Returns whether a text node should participate in rendered-ink height clipping checks.
    static boolean shouldCheckRenderedTextInkHeight(Text text) {
        if (text.getFont().getSize() < 6.0) {
            return false;
        }

        String value = text.getText();
        for (int index = 0; index < value.length(); index++) {
            if (Character.isLetterOrDigit(value.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    /// Returns the first visible text node inside a rendered node subtree.
    static @Nullable Text firstRenderedText(Node root) {
        if (root instanceof Text text && !text.getText().isBlank() && hasRenderableBounds(text)) {
            return text;
        }

        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Text text = firstRenderedText(child);
                if (text != null) {
                    return text;
                }
            }
        }

        return null;
    }

    /// Verifies that a rendered pixel rectangle is contained by a node in scene coordinates.
    static void assertRectangleInsideNodeBounds(
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

    /// Returns a rendered pixel from a node snapshot.
    static Color snapshotPixel(Node node, int x, int y) {
        return FxTestUtils.callOnFxThread(() -> snapshotImageOnFxThread(node).getPixelReader().getColor(x, y));
    }

    /// Returns a rendered pixel from a node-local coordinate in a root snapshot.
    static Color snapshotNodePixel(WritableImage image, Node node, double x, double y) {
        var point = node.localToScene(x, y);
        int pixelX = clampPixelCoordinate((int) Math.round(point.getX()), (int) image.getWidth());
        int pixelY = clampPixelCoordinate((int) Math.round(point.getY()), (int) image.getHeight());
        return image.getPixelReader().getColor(pixelX, pixelY);
    }

    /// Returns a rendered pixel from a scene coordinate in a root snapshot.
    static Color snapshotScenePixel(WritableImage image, double x, double y) {
        int pixelX = clampPixelCoordinate((int) Math.round(x), (int) image.getWidth());
        int pixelY = clampPixelCoordinate((int) Math.round(y), (int) image.getHeight());
        return image.getPixelReader().getColor(pixelX, pixelY);
    }

    /// Clamps a pixel coordinate to a rendered snapshot dimension.
    static int clampPixelCoordinate(int coordinate, int dimension) {
        return Math.max(0, Math.min(dimension - 1, coordinate));
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

    /// Returns the average color of a rendered snapshot area.
    static Color averageSnapshotColor(WritableImage image, int minX, int minY, int maxX, int maxY) {
        int startX = clampPixelCoordinate(Math.min(minX, maxX), (int) image.getWidth());
        int startY = clampPixelCoordinate(Math.min(minY, maxY), (int) image.getHeight());
        int endX = clampPixelCoordinate(Math.max(minX, maxX), (int) image.getWidth());
        int endY = clampPixelCoordinate(Math.max(minY, maxY), (int) image.getHeight());
        double red = 0.0;
        double green = 0.0;
        double blue = 0.0;
        double opacity = 0.0;
        int count = 0;
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                red += color.getRed();
                green += color.getGreen();
                blue += color.getBlue();
                opacity += color.getOpacity();
                count++;
            }
        }
        assertTrue(count > 0, () -> "empty snapshot sample area: minX=" + minX
                + ", minY=" + minY + ", maxX=" + maxX + ", maxY=" + maxY);
        return new Color(red / count, green / count, blue / count, opacity / count);
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
        var bounds = node.localToScene(node.getBoundsInLocal());
        assertSnapshotAreaContainsContrast(
                image,
                (int) Math.floor(bounds.getMinX()),
                (int) Math.floor(bounds.getMinY()),
                (int) Math.ceil(bounds.getMaxX()),
                (int) Math.ceil(bounds.getMaxY()),
                reference,
                minimumDistance,
                node.toString()
        );
    }

    /// Verifies that a node's rendered border band contains pixels that contrast with a reference color.
    static void assertSnapshotNodeBorderContainsContrast(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        var bounds = node.localToScene(node.getBoundsInLocal());
        int minX = (int) Math.floor(bounds.getMinX());
        int minY = (int) Math.floor(bounds.getMinY());
        int maxX = (int) Math.ceil(bounds.getMaxX());
        int maxY = (int) Math.ceil(bounds.getMaxY());
        String description = node + " border";

        if (snapshotAreaContainsContrast(image, minX, minY, maxX, minY + 3, reference, minimumDistance)
                || snapshotAreaContainsContrast(image, minX, maxY - 3, maxX, maxY, reference, minimumDistance)
                || snapshotAreaContainsContrast(image, minX, minY, minX + 3, maxY, reference, minimumDistance)
                || snapshotAreaContainsContrast(image, maxX - 3, minY, maxX, maxY, reference, minimumDistance)) {
            return;
        }

        throw new AssertionError("No contrasting border pixels found for " + description);
    }

    /// Verifies that a node snapshot keeps all contrasting pixels away from its rendered edges.
    static void assertSnapshotNodeEdgesClear(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        var bounds = node.localToScene(node.getBoundsInLocal());
        int minX = (int) Math.floor(bounds.getMinX());
        int minY = (int) Math.floor(bounds.getMinY());
        int maxX = (int) Math.ceil(bounds.getMaxX());
        int maxY = (int) Math.ceil(bounds.getMaxY());
        int edge = Math.max(1, Math.min(3, Math.min(maxX - minX, maxY - minY) / 6));

        boolean touchesTop = snapshotAreaContainsContrast(image, minX, minY, maxX, minY + edge,
                reference, minimumDistance);
        boolean touchesBottom = snapshotAreaContainsContrast(image, minX, maxY - edge, maxX, maxY,
                reference, minimumDistance);
        boolean touchesLeft = snapshotAreaContainsContrast(image, minX, minY, minX + edge, maxY,
                reference, minimumDistance);
        boolean touchesRight = snapshotAreaContainsContrast(image, maxX - edge, minY, maxX, maxY,
                reference, minimumDistance);

        assertFalse(
                touchesTop || touchesBottom || touchesLeft || touchesRight,
                () -> "Contrasting pixels reached the edge for " + node + ", bounds=" + bounds
        );
    }

    /// Verifies that a snapshot area contains pixels that contrast with a reference color.
    static void assertSnapshotAreaContainsContrast(
            WritableImage image,
            int minX,
            int minY,
            int maxX,
            int maxY,
            Color reference,
            double minimumDistance,
            String description
    ) {
        assertTrue(snapshotAreaContainsContrast(image, minX, minY, maxX, maxY, reference, minimumDistance),
                () -> "No contrasting pixels found for " + description);
    }

    /// Returns whether a snapshot area contains pixels that contrast with a reference color.
    static boolean snapshotAreaContainsContrast(
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

    /// Returns the centroid of rendered pixels inside a node that contrast with the reference color.
    static Point2D contrastingPixelCentroid(
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
        double totalWeight = 0.0;
        double weightedX = 0.0;
        double weightedY = 0.0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = image.getPixelReader().getColor(x, y);
                double distance = colorDistance(color, reference);
                if (color.getOpacity() > 0.1 && distance >= minimumDistance) {
                    double weight = color.getOpacity() * Math.min(1.0, distance);
                    totalWeight += weight;
                    weightedX += (x + 0.5) * weight;
                    weightedY += (y + 0.5) * weight;
                }
            }
        }

        assertTrue(totalWeight > 0.0, () -> "No contrasting pixels found for " + node);
        return new Point2D(weightedX / totalWeight, weightedY / totalWeight);
    }

    /// Returns a required rendered text ink bounds inside a snapshot.
    static Rectangle2D requiredRenderedTextInkBounds(WritableImage image, Text text) {
        @Nullable Rectangle2D bounds = renderedTextInkBounds(image, text);
        assertTrue(bounds != null, () -> "No rendered text ink pixels found for " + text.getText());
        return Objects.requireNonNull(bounds);
    }

    /// Returns the rendered text ink bounds inside a snapshot, or `null` when pixel classification is inconclusive.
    static @Nullable Rectangle2D renderedTextInkBounds(WritableImage image, Text text) {
        Paint fillPaint = text.getFill();
        if (fillPaint instanceof Color fill) {
            @Nullable Rectangle2D inkBounds = textFillPixelBounds(image, text, fill, null, 0.24);
            Color background = sampledTextBackgroundColor(image, text);
            inkBounds = unionRectangles(inkBounds, textFillPixelBounds(image, text, fill, background, 0.45));
            return unionRectangles(inkBounds, contrastingPixelBoundsOrNull(image, text, background, 0.04));
        }

        return contrastingPixelBoundsOrNull(image, text, sampledTextBackgroundColor(image, text), 0.04);
    }

    /// Returns the union of two nullable rectangle bounds.
    static @Nullable Rectangle2D unionRectangles(
            @Nullable Rectangle2D first,
            @Nullable Rectangle2D second
    ) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }

        double minX = Math.min(first.getMinX(), second.getMinX());
        double minY = Math.min(first.getMinY(), second.getMinY());
        double maxX = Math.max(first.getMaxX(), second.getMaxX());
        double maxY = Math.max(first.getMaxY(), second.getMaxY());
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    /// Returns pixel bounds for text pixels close to the requested fill color.
    static @Nullable Rectangle2D textFillPixelBounds(
            WritableImage image,
            Text text,
            Color fill,
            @Nullable Color background,
            double maximumFillDistance
    ) {
        Bounds bounds = text.localToScene(text.getBoundsInLocal());
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
                double fillDistance = colorDistance(color, fill);
                if (fillDistance > maximumFillDistance) {
                    continue;
                }
                if (background != null && fillDistance + 0.02 >= colorDistance(color, background)) {
                    continue;
                }

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        if (maxX < minX || maxY < minY) {
            return null;
        }
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Samples a nearby background pixel around a text node before measuring rendered text ink.
    static Color sampledTextBackgroundColor(WritableImage image, Text text) {
        Bounds bounds = text.localToScene(text.getBoundsInLocal());
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int left = Math.max(0, (int) Math.floor(bounds.getMinX()) - 3);
        int right = Math.min(width - 1, (int) Math.ceil(bounds.getMaxX()) + 3);
        int top = Math.max(0, (int) Math.floor(bounds.getMinY()) - 3);
        int bottom = Math.min(height - 1, (int) Math.ceil(bounds.getMaxY()) + 3);
        @Nullable Color textFill = text.getFill() instanceof Color color ? color : null;
        @Nullable Color bestColor = null;
        double bestDistance = -1.0;

        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                if (x >= bounds.getMinX() && x <= bounds.getMaxX()
                        && y >= bounds.getMinY() && y <= bounds.getMaxY()) {
                    continue;
                }

                Color color = image.getPixelReader().getColor(x, y);
                if (textFill == null) {
                    return color;
                }

                double distance = colorDistance(color, textFill);
                if (distance > bestDistance) {
                    bestDistance = distance;
                    bestColor = color;
                }
            }
        }

        return bestColor == null ? sampledNodeBackgroundColor(image, text) : bestColor;
    }

    /// Returns the bounds of rendered pixels inside a node that contrast with its sampled local background.
    static Color sampledNodeBackgroundColor(WritableImage image, Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        int x = clampPixelCoordinate((int) Math.floor(bounds.getMinX()), (int) image.getWidth());
        int y = clampPixelCoordinate((int) Math.floor(bounds.getMinY()), (int) image.getHeight());
        return image.getPixelReader().getColor(x, y);
    }

    /// Returns the bounds of rendered pixels inside a node that contrast with the reference color.
    static Rectangle2D contrastingPixelBounds(
            WritableImage image,
            Node node,
            Color reference,
            double minimumDistance
    ) {
        @Nullable Rectangle2D bounds = contrastingPixelBoundsOrNull(image, node, reference, minimumDistance);
        assertTrue(bounds != null, () -> "No contrasting pixels found for " + node);
        return Objects.requireNonNull(bounds);
    }

    /// Returns the rendered-pixel bounds inside a node that contrast with a reference color, or `null`.
    static @Nullable Rectangle2D contrastingPixelBoundsOrNull(
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

        if (maxX < minX || maxY < minY) {
            return null;
        }
        return new Rectangle2D(minX, minY, maxX - minX + 1.0, maxY - minY + 1.0);
    }

    /// Writes a rendered snapshot to a build report path for manual visual inspection.
    static void writeVisualSnapshot(WritableImage image, java.nio.file.Path path) {
        writeVisualSnapshot(toBufferedImage(image), path);
    }

    /// Writes a desktop image to a build report path for manual visual inspection.
    static void writeVisualSnapshot(BufferedImage image, java.nio.file.Path path) {
        assertVisualSnapshotReportHasContent(image, path);
        try {
            @Nullable java.nio.file.Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ImageIO.write(image, "png", path.toFile());
            writeVisualSnapshotIndex(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Verifies that a visual report image is not empty or a single flat color.
    static void assertVisualSnapshotReportHasContent(BufferedImage image, java.nio.file.Path path) {
        int width = image.getWidth();
        int height = image.getHeight();
        assertTrue(width > 0 && height > 0, () -> "visual snapshot has invalid dimensions: " + path);

        int visiblePixels = 0;
        int differentVisiblePixels = 0;
        int firstVisibleColor = 0;
        boolean hasFirstVisibleColor = false;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                if (((argb >>> 24) & 0xff) <= 16) {
                    continue;
                }

                int colorBucket = argb & 0xf0f0f0f0;
                visiblePixels++;
                if (!hasFirstVisibleColor) {
                    firstVisibleColor = colorBucket;
                    hasFirstVisibleColor = true;
                } else if (colorBucket != firstVisibleColor) {
                    differentVisiblePixels++;
                }
            }
        }

        assertTrue(visiblePixels > 0, () -> "visual snapshot has no visible pixels: " + path);
        assertTrue(differentVisiblePixels > 0,
                () -> "visual snapshot is a single flat color and cannot prove rendered content: " + path);
    }

    /// Converts a JavaFX image snapshot to a desktop image for report output.
    static BufferedImage toBufferedImage(WritableImage image) {
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
    static double colorDistance(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed())
                + Math.abs(first.getGreen() - second.getGreen())
                + Math.abs(first.getBlue() - second.getBlue());
    }

    /// Verifies that a sampled snapshot pixel is close to the expected color.
    static void assertColorNear(Color actual, Color expected, double maximumDistance, String description) {
        assertTrue(colorDistance(actual, expected) <= maximumDistance,
                () -> description + ": actual=" + actual + ", expected=" + expected);
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

    /// Writes a lightweight HTML index for generated core visual snapshots.
    static void writeVisualSnapshotIndex(java.nio.file.Path snapshotPath) throws IOException {
        java.nio.file.Path directory = snapshotPath.getParent();
        if (directory == null) {
            return;
        }

        List<java.nio.file.Path> snapshots;
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
        for (java.nio.file.Path snapshot : snapshots) {
            appendVisualSnapshotFigure(html, snapshot);
        }
        html.append("""
                  </div>
                </body>
                </html>
                """);
        java.nio.file.Path indexPath = directory.resolve("index.html");
        Files.writeString(indexPath, html.toString());
    }

    /// Appends one linked snapshot figure to the visual report index.
    private static void appendVisualSnapshotFigure(StringBuilder html, java.nio.file.Path snapshot) {
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
}
