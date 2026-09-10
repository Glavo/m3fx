// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Transform;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that SVG viewport mapping retains finite transforms at numeric boundaries.
@NotNullByDefault
final class M3SVGIconSkinTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Applies the requested artwork direction once in either parent orientation.
    @Test
    void rtlMirroringIsAppliedExactlyOnce() {
        FxTestUtils.runOnFxThread(() -> {
            for (NodeOrientation orientation : List.of(NodeOrientation.LEFT_TO_RIGHT, NodeOrientation.RIGHT_TO_LEFT)) {
                M3SVGIcon icon = createIcon();
                icon.setManaged(false);
                icon.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                Pane root = new Pane(icon);
                root.setNodeOrientation(orientation);
                new Scene(root, 80.0, 40.0);
                icon.layout();
                SVGPath path = path(icon);

                assertTrue(path.localToScene(0.0, 0.0).getX() < path.localToScene(24.0, 0.0).getX(),
                        "Non-directional artwork must retain its source direction");
                icon.setAutoMirrored(true);
                icon.layout();
                assertTrue(path.localToScene(0.0, 0.0).getX() > path.localToScene(24.0, 0.0).getX(),
                        "Directional artwork must be mirrored exactly once");

                icon.setNodeOrientation(NodeOrientation.INHERIT);
                icon.layout();
                assertEquals(orientation == NodeOrientation.RIGHT_TO_LEFT,
                        path.localToScene(0.0, 0.0).getX() > path.localToScene(24.0, 0.0).getX());
                icon.setAutoMirrored(false);
                icon.layout();
                assertTrue(path.localToScene(0.0, 0.0).getX() < path.localToScene(24.0, 0.0).getX());
            }
        });
    }

    /// Mirrors a finite viewport whose maximum source coordinate overflows ordinary addition.
    @Test
    void mirroredViewportAvoidsIntermediateOverflow() {
        FxTestUtils.runOnFxThread(() -> {
            M3SVGIcon icon = createIcon();
            icon.setAutoMirrored(true);
            icon.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            icon.setViewBox(new Rectangle2D(Double.MAX_VALUE, 0.0, Double.MAX_VALUE, Double.MAX_VALUE));
            icon.layout();

            SVGPath path = path(icon);
            Transform transform = path.getTransforms().get(0);
            assertTrue(path.isVisible());
            assertEquals(48.0, transform.getTx(), 1.0e-12);
            assertTrue(Double.isFinite(transform.getMxx()));
            assertTrue(transform.getMxx() < 0.0);
        });
    }

    /// Hides mappings that cannot be represented and recovers after a usable viewport is restored.
    @Test
    void unrepresentableViewportDoesNotPublishNonfiniteTransform() {
        FxTestUtils.runOnFxThread(() -> {
            M3SVGIcon icon = createIcon();
            icon.layout();
            SVGPath path = path(icon);
            Transform transform = path.getTransforms().get(0);
            for (Rectangle2D viewBox : List.of(
                    new Rectangle2D(0.0, 0.0, Double.MIN_VALUE, Double.MIN_VALUE),
                    new Rectangle2D(Double.MAX_VALUE, 0.0, 1.0, 1.0))) {
                icon.setViewBox(viewBox);
                icon.layout();
                assertFalse(path.isVisible());
                assertTrue(Double.isFinite(transform.getMxx()));
                assertTrue(Double.isFinite(transform.getMyy()));
                assertTrue(Double.isFinite(transform.getTx()));
                assertTrue(Double.isFinite(transform.getTy()));
            }

            icon.setViewBox(new Rectangle2D(0.0, 0.0, 24.0, 24.0));
            icon.layout();
            assertTrue(path.isVisible());
            assertEquals(1.0, transform.getMxx());
        });
    }

    /// Creates an icon with a default skin and a 24-pixel layout size.
    private static M3SVGIcon createIcon() {
        M3SVGIcon icon = new M3SVGIcon("M0 0H24V24H0Z");
        icon.setIconSize(24.0);
        icon.setSkin(new M3SVGIconSkin(icon));
        icon.resize(24.0, 24.0);
        return icon;
    }

    /// Returns the path displayed by the icon's default skin.
    private static SVGPath path(M3SVGIcon icon) {
        return (SVGPath) icon.getChildrenUnmodifiable().get(0);
    }
}
