// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.glavo.m3fx.controls.ControlTestFixtures.*;
import static org.glavo.m3fx.controls.ControlVisualTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Checks control geometry, content alignment, and layout boundaries.
@NotNullByDefault
final class ControlLayoutAssertions {
    /// Prevents instantiation of this utility class.
    private ControlLayoutAssertions() {
    }

    /// Verifies the resolved logical geometry of a text input layout.
    static void assertTextInputLayoutUsesLogicalGeometry(M3TextInputLayout layout, boolean rightToLeft) {
        layout.applyCss();
        layout.layout();

        Region inputContainer = lookupRegion(layout, "." + "m3-text-input-container");
        Region leadingSlot = lookupRegion(layout, "." + "m3-text-input-leading");
        Region trailingSlot = lookupRegion(layout, "." + "m3-text-input-trailing");
        Text label = assertInstanceOf(Text.class, layout.lookup("." + "m3-text-input-label"));
        Label supportingLabel = assertInstanceOf(
                Label.class,
                layout.lookup("." + "m3-text-input-supporting-text")
        );
        Label counterLabel = assertInstanceOf(Label.class, layout.lookup("." + "m3-text-input-counter"));
        Path outline = assertInstanceOf(Path.class, layout.lookup("." + "m3-text-input-outline"));
        TextInputControl input = Objects.requireNonNull(layout.getInput(), "input");

        Bounds containerBounds = inputContainer.localToScene(inputContainer.getBoundsInLocal());
        Bounds leadingBounds = leadingSlot.localToScene(leadingSlot.getBoundsInLocal());
        Bounds trailingBounds = trailingSlot.localToScene(trailingSlot.getBoundsInLocal());
        Bounds labelBounds = label.localToScene(label.getBoundsInLocal());
        Bounds supportingBounds = supportingLabel.localToScene(supportingLabel.getBoundsInLocal());
        Bounds counterBounds = counterLabel.localToScene(counterLabel.getBoundsInLocal());

        if (rightToLeft) {
            assertTrue(leadingBounds.getMinX() > containerBounds.getCenterX(),
                    () -> describeTextInputLayoutGeometry(
                            containerBounds,
                            leadingBounds,
                            trailingBounds,
                            labelBounds,
                            supportingBounds,
                            counterBounds
                    ));
            if (hasRenderableBounds(trailingSlot)) {
                assertTrue(trailingBounds.getMaxX() < containerBounds.getCenterX(),
                        () -> describeTextInputLayoutGeometry(
                                containerBounds,
                                leadingBounds,
                                trailingBounds,
                                labelBounds,
                                supportingBounds,
                                counterBounds
                        ));
            }
            assertTrue(labelBounds.getCenterX() > containerBounds.getCenterX(),
                    () -> describeTextInputLayoutGeometry(
                            containerBounds,
                            leadingBounds,
                            trailingBounds,
                            labelBounds,
                            supportingBounds,
                            counterBounds
                    ));
            assertTrue(supportingBounds.getCenterX() > counterBounds.getCenterX(),
                    () -> describeTextInputLayoutGeometry(
                            containerBounds,
                            leadingBounds,
                            trailingBounds,
                            labelBounds,
                            supportingBounds,
                            counterBounds
                    ));
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, input.getEffectiveNodeOrientation());
        } else {
            assertTrue(leadingBounds.getMaxX() < containerBounds.getCenterX(),
                    () -> describeTextInputLayoutGeometry(
                            containerBounds,
                            leadingBounds,
                            trailingBounds,
                            labelBounds,
                            supportingBounds,
                            counterBounds
                    ));
            if (hasRenderableBounds(trailingSlot)) {
                assertTrue(trailingBounds.getMinX() > containerBounds.getCenterX(),
                        () -> describeTextInputLayoutGeometry(
                                containerBounds,
                                leadingBounds,
                                trailingBounds,
                                labelBounds,
                                supportingBounds,
                                counterBounds
                        ));
            }
            assertTrue(labelBounds.getCenterX() < containerBounds.getCenterX(),
                    () -> describeTextInputLayoutGeometry(
                            containerBounds,
                            leadingBounds,
                            trailingBounds,
                            labelBounds,
                            supportingBounds,
                            counterBounds
                    ));
            assertTrue(supportingBounds.getCenterX() < counterBounds.getCenterX(),
                    () -> describeTextInputLayoutGeometry(
                            containerBounds,
                            leadingBounds,
                            trailingBounds,
                            labelBounds,
                            supportingBounds,
                            counterBounds
                    ));
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, input.getEffectiveNodeOrientation());
        }

        assertTrue(outlineNotchGap(outline) >= labelBounds.getWidth() - 1.0,
                () -> "outlineNotchGap=" + outlineNotchGap(outline)
                        + ", labelBounds=" + labelBounds
                        + ", rightToLeft=" + rightToLeft);
    }

    /// Describes text input logical layout geometry for assertion failures.
    static String describeTextInputLayoutGeometry(
            Bounds containerBounds,
            Bounds leadingBounds,
            Bounds trailingBounds,
            Bounds labelBounds,
            Bounds supportingBounds,
            Bounds counterBounds
    ) {
        return "containerBounds=" + containerBounds
                + ", leadingBounds=" + leadingBounds
                + ", trailingBounds=" + trailingBounds
                + ", labelBounds=" + labelBounds
                + ", supportingBounds=" + supportingBounds
                + ", counterBounds=" + counterBounds;
    }

    /// Returns the current top outline gap generated for a floating text input label.
    static double outlineNotchGap(Path outline) {
        if (outline.getElements().size() < 3) {
            return 0.0;
        }
        var notchStart = assertInstanceOf(javafx.scene.shape.LineTo.class, outline.getElements().get(1));
        var notchEnd = assertInstanceOf(javafx.scene.shape.MoveTo.class, outline.getElements().get(2));
        return notchEnd.getX() - notchStart.getX();
    }

    /// Verifies that a badged box badge is anchored to logical end and top of its content.
    static void assertBadgedBoxBadgeAnchoredToLogicalEnd(M3BadgedBox badgedBox, boolean rightToLeft) {
        Node content = assertInstanceOf(Node.class, badgedBox.getContent());
        M3Badge badge = assertInstanceOf(M3Badge.class, badgedBox.getBadge());
        Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
        Bounds badgeBounds = badge.localToScene(badge.getBoundsInLocal());

        if (rightToLeft) {
            assertTrue(badgeBounds.getCenterX() < contentBounds.getCenterX(),
                    () -> "badgeBounds=" + badgeBounds + ", contentBounds=" + contentBounds);
        } else {
            assertTrue(badgeBounds.getCenterX() > contentBounds.getCenterX(),
                    () -> "badgeBounds=" + badgeBounds + ", contentBounds=" + contentBounds);
        }
        assertTrue(badgeBounds.getCenterY() < contentBounds.getCenterY(),
                () -> "badgeBounds=" + badgeBounds + ", contentBounds=" + contentBounds);
        assertTrue(rangesOverlap(badgeBounds.getMinX(), badgeBounds.getMaxX(), contentBounds.getMinX(), contentBounds.getMaxX()),
                () -> "badge should overlap content horizontally: badgeBounds="
                        + badgeBounds + ", contentBounds=" + contentBounds);
        assertTrue(rangesOverlap(badgeBounds.getMinY(), badgeBounds.getMaxY(), contentBounds.getMinY(), contentBounds.getMaxY()),
                () -> "badge should overlap content vertically: badgeBounds="
                        + badgeBounds + ", contentBounds=" + contentBounds);
    }

    /// Returns whether two one-dimensional intervals overlap.
    static boolean rangesOverlap(double firstMin, double firstMax, double secondMin, double secondMax) {
        return firstMax > secondMin && firstMin < secondMax;
    }

    /// Verifies that common Material targets keep text, icon, and fixed-cell geometry stable.
    static void assertMaterialTargetGeometryIsStable(Node root) {
        assertRenderedTextNodesStayInsideLayout(root);
        assertFixedTargetControlsKeepCenteredContent(root);
        assertNavigationItemsKeepGraphicContentCentered(root);
    }

    /// Verifies that fixed Material touch targets keep their rendered content centered.
    static void assertFixedTargetControlsKeepCenteredContent(Node root) {
        WritableImage image = snapshotImageOnFxThread(root);
        visitVisibleNodes(root, node -> {
            if (node instanceof M3IconButton button) {
                @Nullable Node graphic = button.getGraphic();
                if (graphic != null && (button.getText() == null || button.getText().isEmpty())) {
                    assertFixedTargetContentCentered(image, button, graphic, 1.0, 1.5);
                }
            } else if (node instanceof M3IconToggleButton button) {
                @Nullable Node graphic = button.getGraphic();
                if (graphic != null) {
                    assertFixedTargetContentCentered(image, button, graphic, 1.0, 1.5);
                }
            } else if (node instanceof M3FloatingActionButton button) {
                @Nullable Node graphic = button.getGraphic();
                if (graphic != null && (button.getText() == null || button.getText().isEmpty())) {
                    assertFixedTargetContentCentered(image, button, graphic, 1.0, 1.5);
                }
            } else if (node instanceof M3Tab tab) {
                @Nullable Node textNode = tab.lookup(".text");
                if (textNode != null && hasRenderableBounds(textNode)) {
                    assertFixedTargetContentVerticallyCentered(image, tab, textNode, 1.0, 2.0);
                }
            } else if (node instanceof M3SegmentedButton button) {
                @Nullable Node textNode = button.lookup(".text");
                if (textNode != null && hasRenderableBounds(textNode)) {
                    assertFixedTargetContentVerticallyCentered(image, button, textNode, 1.0, 2.0);
                }
            } else if (node instanceof ButtonBase button
                    && (button.getStyleClass().contains("m3-date-picker-day-cell")
                    || button.getStyleClass().contains("m3-time-picker-cell"))) {
                @Nullable Node textNode = button.lookup(".text");
                if (textNode != null && hasRenderableBounds(textNode)) {
                    assertFixedTargetContentCentered(image, button, textNode, 1.0, 1.5);
                }
            }
        });
    }

    /// Verifies that navigation item graphic slots stay centered inside their indicator area.
    static void assertNavigationItemsKeepGraphicContentCentered(Node root) {
        visitVisibleNodes(root, node -> {
            if (!(node instanceof M3NavigationItem item) || item.getGraphic() == null) {
                return;
            }

            @Nullable Node iconContainer = item.lookup(".m3-navigation-item-icon-container");
            @Nullable Node graphicContainer = item.lookup(".m3-navigation-item-graphic");
            if (iconContainer != null && graphicContainer != null && hasRenderableBounds(graphicContainer)) {
                assertNodeCentersAligned(iconContainer, graphicContainer, 1.0);
            }
        });
    }

    /// Verifies that both layout bounds and rendered ink stay centered in a fixed Material target.
    static void assertFixedTargetContentCentered(
            WritableImage image,
            Node container,
            Node content,
            double layoutTolerance,
            double inkTolerance
    ) {
        assertNodeCentersAligned(container, content, layoutTolerance);
        @Nullable Text renderedText = firstRenderedText(content);
        if (renderedText == null || containsSvgPath(content)) {
            return;
        }

        Point2D containerCenter = sceneCenter(container);
        Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, renderedText);
        double inkCenterX = inkBounds.getMinX() + inkBounds.getWidth() / 2.0;
        double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
        assertEquals(containerCenter.getX(), inkCenterX, inkTolerance,
                () -> "fixed target rendered content is horizontally off-center: container="
                        + container + ", content=" + content + ", inkBounds=" + inkBounds);
        assertEquals(containerCenter.getY(), inkCenterY, inkTolerance,
                () -> "fixed target rendered content is vertically off-center: container="
                        + container + ", content=" + content + ", inkBounds=" + inkBounds);
    }

    /// Verifies that fixed-height target content keeps its layout and rendered ink vertically centered.
    static void assertFixedTargetContentVerticallyCentered(
            WritableImage image,
            Node container,
            Node content,
            double layoutTolerance,
            double inkTolerance
    ) {
        assertNodeVerticalCentersAligned(container, content, layoutTolerance);
        @Nullable Text renderedText = firstRenderedText(content);
        if (renderedText == null || containsSvgPath(content)) {
            return;
        }

        Point2D containerCenter = sceneCenter(container);
        Rectangle2D inkBounds = requiredRenderedTextInkBounds(image, renderedText);
        double inkCenterY = inkBounds.getMinY() + inkBounds.getHeight() / 2.0;
        assertEquals(containerCenter.getY(), inkCenterY, inkTolerance,
                () -> "fixed target rendered content is vertically off-center: container="
                        + container + ", content=" + content + ", inkBounds=" + inkBounds);
    }

    /// Visits visible nodes in a rendered hierarchy.
    static void visitVisibleNodes(Node node, Consumer<Node> visitor) {
        if (!node.isVisible()) {
            return;
        }

        visitor.accept(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                visitVisibleNodes(child, visitor);
            }
        }
    }

    /// Returns whether a node has non-empty rendered layout bounds.
    static boolean hasRenderableBounds(Node node) {
        Bounds bounds = node.getLayoutBounds();
        return bounds.getWidth() > 0.5 && bounds.getHeight() > 0.5;
    }

    /// Returns the first visible rendered text node with the supplied text.
    static Text renderedTextNode(Node root, String value) {
        AtomicReference<@Nullable Text> result = new AtomicReference<>();
        visitVisibleNodes(root, node -> {
            if (result.get() == null
                    && node instanceof Text text
                    && value.equals(text.getText())
                    && hasRenderableBounds(text)) {
                result.set(text);
            }
        });

        Text text = result.get();
        if (text == null) {
            throw new AssertionError("No rendered text node found for " + value);
        }
        return text;
    }

    /// Returns the nearest ancestor that should constrain visible text.
    static Node nearestVisualBoundary(Node node, Node root) {
        @Nullable Parent parent = node.getParent();
        if (node.getStyleClass().contains("m3-text-input-label")) {
            @Nullable M3TextInputLayout layout = nearestTextInputLayout(node);
            if (layout != null && layout.getParent() != null) {
                // An outlined floating label intentionally straddles both the input container and layout top edge.
                parent = layout.getParent();
            }
        }
        while (parent != null) {
            if (parent == root
                    || parent instanceof Region
                    || parent.getClip() != null) {
                return parent;
            }
            parent = parent.getParent();
        }
        return root;
    }

    /// Returns the nearest text input layout ancestor.
    static @Nullable M3TextInputLayout nearestTextInputLayout(Node node) {
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof M3TextInputLayout layout) {
                return layout;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /// Verifies that a child node stays inside an ancestor boundary.
    static void assertNodeInsideAncestor(Node ancestor, Node child, double tolerance) {
        Bounds ancestorBounds = ancestor.localToScene(ancestor.getLayoutBounds());
        Bounds childBounds = child.localToScene(child.getLayoutBounds());
        assertTrue(
                childBounds.getMinX() >= ancestorBounds.getMinX() - tolerance
                        && childBounds.getMaxX() <= ancestorBounds.getMaxX() + tolerance
                        && childBounds.getMinY() >= ancestorBounds.getMinY() - tolerance
                        && childBounds.getMaxY() <= ancestorBounds.getMaxY() + tolerance,
                () -> "child escaped visual boundary: child=" + child
                        + ", ancestor=" + ancestor
                        + ", childBounds=" + childBounds
                        + ", ancestorBounds=" + ancestorBounds
        );
    }

    /// Verifies that a child node's visual center matches its container center.
    static void assertNodeCentersAligned(Node container, Node child, double tolerance) {
        Point2D containerCenter = sceneCenter(container);
        Point2D childCenter = sceneCenter(child);
        Bounds containerBounds = container.localToScene(container.getLayoutBounds());
        Bounds childBounds = child.localToScene(child.getLayoutBounds());

        assertEquals(containerCenter.getX(), childCenter.getX(), tolerance,
                () -> "containerBounds=" + containerBounds + ", childBounds=" + childBounds);
        assertEquals(containerCenter.getY(), childCenter.getY(), tolerance,
                () -> "containerBounds=" + containerBounds + ", childBounds=" + childBounds);
    }

    /// Verifies that a child node's vertical visual center matches its container center.
    static void assertNodeVerticalCentersAligned(Node container, Node child, double tolerance) {
        Point2D containerCenter = sceneCenter(container);
        Point2D childCenter = sceneCenter(child);
        Bounds containerBounds = container.localToScene(container.getLayoutBounds());
        Bounds childBounds = child.localToScene(child.getLayoutBounds());

        assertEquals(containerCenter.getY(), childCenter.getY(), tolerance,
                () -> "containerBounds=" + containerBounds + ", childBounds=" + childBounds);
    }

    /// Returns the scene-space center of a node's layout bounds.
    static Point2D sceneCenter(Node node) {
        Bounds bounds = node.localToScene(node.getLayoutBounds());
        return new Point2D(
                (bounds.getMinX() + bounds.getMaxX()) / 2.0,
                (bounds.getMinY() + bounds.getMaxY()) / 2.0
        );
    }

    /// Resizes a synthetic row to deterministic dimensions for keyboard page-step tests.
    static void resizeRow(Region row) {
        row.resize(240.0, 40.0);
    }

    /// Verifies that token updates do not overwrite application-bound Region layout properties.
    static void assertBoundRegionMetricsPreserved(Region region, Runnable tokenUpdate, Runnable tokenAssertions) {
        SimpleDoubleProperty minWidth = new SimpleDoubleProperty(31.0);
        SimpleDoubleProperty prefWidth = new SimpleDoubleProperty(41.0);
        SimpleDoubleProperty maxWidth = new SimpleDoubleProperty(91.0);
        SimpleDoubleProperty minHeight = new SimpleDoubleProperty(32.0);
        SimpleDoubleProperty prefHeight = new SimpleDoubleProperty(42.0);
        SimpleDoubleProperty maxHeight = new SimpleDoubleProperty(92.0);
        SimpleObjectProperty<Insets> padding = new SimpleObjectProperty<>(new Insets(1.0, 2.0, 3.0, 4.0));
        region.minWidthProperty().bind(minWidth);
        region.prefWidthProperty().bind(prefWidth);
        region.maxWidthProperty().bind(maxWidth);
        region.minHeightProperty().bind(minHeight);
        region.prefHeightProperty().bind(prefHeight);
        region.maxHeightProperty().bind(maxHeight);
        region.paddingProperty().bind(padding);

        try {
            tokenUpdate.run();
            tokenAssertions.run();

            assertEquals(31.0, region.getMinWidth(), 0.0001);
            assertEquals(41.0, region.getPrefWidth(), 0.0001);
            assertEquals(91.0, region.getMaxWidth(), 0.0001);
            assertEquals(32.0, region.getMinHeight(), 0.0001);
            assertEquals(42.0, region.getPrefHeight(), 0.0001);
            assertEquals(92.0, region.getMaxHeight(), 0.0001);
            assertEquals(new Insets(1.0, 2.0, 3.0, 4.0), region.getPadding());

            minWidth.set(35.0);
            prefWidth.set(45.0);
            maxWidth.set(95.0);
            minHeight.set(36.0);
            prefHeight.set(46.0);
            maxHeight.set(96.0);
            padding.set(new Insets(5.0, 6.0, 7.0, 8.0));

            assertEquals(35.0, region.getMinWidth(), 0.0001);
            assertEquals(45.0, region.getPrefWidth(), 0.0001);
            assertEquals(95.0, region.getMaxWidth(), 0.0001);
            assertEquals(36.0, region.getMinHeight(), 0.0001);
            assertEquals(46.0, region.getPrefHeight(), 0.0001);
            assertEquals(96.0, region.getMaxHeight(), 0.0001);
            assertEquals(new Insets(5.0, 6.0, 7.0, 8.0), region.getPadding());
        } finally {
            region.minWidthProperty().unbind();
            region.prefWidthProperty().unbind();
            region.maxWidthProperty().unbind();
            region.minHeightProperty().unbind();
            region.prefHeightProperty().unbind();
            region.maxHeightProperty().unbind();
            region.paddingProperty().unbind();
        }
    }
}
