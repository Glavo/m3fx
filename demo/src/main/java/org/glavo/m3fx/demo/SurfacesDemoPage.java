// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3SurfaceElevation;
import org.glavo.m3fx.controls.M3SurfaceVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Surfaces component showcase page.
@NotNullByDefault
final class SurfacesDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SurfacesDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the surface component page.
    Node createContent() {
        M3Surface surface = createSurface("Surface", M3SurfaceVariant.SURFACE, M3SurfaceElevation.LEVEL0);
        M3Surface container = createSurface("Container", M3SurfaceVariant.CONTAINER, M3SurfaceElevation.LEVEL1);
        M3Surface high = createSurface("High", M3SurfaceVariant.CONTAINER_HIGH, M3SurfaceElevation.LEVEL3);
        M3Surface primary = createSurface("Primary", M3SurfaceVariant.PRIMARY_CONTAINER, M3SurfaceElevation.LEVEL2);
        M3Surface secondary = createSurface("Secondary", M3SurfaceVariant.SECONDARY_CONTAINER, M3SurfaceElevation.LEVEL2);
        M3Surface tertiary = createSurface("Tertiary", M3SurfaceVariant.TERTIARY_CONTAINER, M3SurfaceElevation.LEVEL2);
        M3Surface localColors =
                createSurface("Local container", M3SurfaceVariant.SURFACE, M3SurfaceElevation.LEVEL1);
        localColors.setContainerColor(Color.web("#E8F5E9"));

        return createGallery(
                createShowcaseGroup("Surface Tones", surface, container, high),
                createShowcaseGroup("Container Colors", primary, secondary, tertiary),
                createShowcaseGroup("Local Container Paint", localColors)
        );
    }

    /// Creates a surface sample with initial content nodes.
    private static M3Surface createSurface(Node... children) {
        M3Surface surface = new M3Surface();
        surface.getContent().addAll(children);
        return surface;
    }

    /// Creates a sample surface.
    private static M3Surface createSurface(
            String title,
            M3SurfaceVariant variant,
            M3SurfaceElevation elevation
    ) {
        M3Text label = new M3Text(title, M3TextRole.TITLE_MEDIUM);
        M3Surface surface = createSurface(label);
        surface.setVariant(variant);
        surface.setElevation(elevation);
        surface.setPrefSize(180.0, 96.0);
        return surface;
    }
}
