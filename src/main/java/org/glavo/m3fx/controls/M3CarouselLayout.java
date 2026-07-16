// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Defines the Material Design 3 layout strategy used by [M3Carousel].
///
/// Contained layouts dynamically assign large, medium, and small item widths around the selected focal item.
/// Uncontained layouts preserve authored item widths, while the full-screen layout stacks viewport-sized items
/// vertically.
/// See [Material Design carousel layouts](https://m3.material.io/components/carousel/specs).
@NotNullByDefault
public enum M3CarouselLayout {
    /// Shows one or more large items followed by a medium item and a small item.
    MULTI_BROWSE("m3-carousel-multi-browse", true, false, false),

    /// Shows equal authored-width items that may continue beyond the trailing viewport edge.
    UNCONTAINED("m3-carousel-uncontained", false, false, true),

    /// Shows authored-width items with independently chosen aspect ratios.
    UNCONTAINED_MULTI_ASPECT_RATIO(
            "m3-carousel-uncontained-multi-aspect-ratio",
            false,
            false,
            true
    ),

    /// Shows one large focal item and one small preview item at the logical trailing edge.
    HERO("m3-carousel-hero", true, false, false),

    /// Shows one centered large focal item between two small preview items.
    CENTER_ALIGNED_HERO("m3-carousel-center-aligned-hero", true, true, false),

    /// Shows one edge-to-edge item at a time in a vertical feed and always snaps to item boundaries.
    FULL_SCREEN("m3-carousel-full-screen", true, false, false);

    /// The style class applied to carousels using this layout.
    private final String styleClass;

    /// Whether free scrolling should settle on the nearest focal item.
    private final boolean snapScrolling;

    /// Whether the selected focal item is centered in the viewport.
    private final boolean centeredFocalItem;

    /// Whether authored item widths are preserved.
    private final boolean preservesAuthoredWidths;

    /// Creates a carousel layout descriptor.
    ///
    /// @param styleClass              the style class applied to the carousel
    /// @param snapScrolling           whether free scrolling should settle on an item
    /// @param centeredFocalItem       whether the focal item is centered
    /// @param preservesAuthoredWidths whether child preferred widths are preserved
    M3CarouselLayout(
            String styleClass,
            boolean snapScrolling,
            boolean centeredFocalItem,
            boolean preservesAuthoredWidths
    ) {
        this.styleClass = styleClass;
        this.snapScrolling = snapScrolling;
        this.centeredFocalItem = centeredFocalItem;
        this.preservesAuthoredWidths = preservesAuthoredWidths;
    }

    /// Returns the style class applied to this layout.
    ///
    /// @return the layout style class
    String styleClass() {
        return styleClass;
    }

    /// Returns whether free scrolling should settle on the nearest focal item.
    ///
    /// @return `true` when this layout uses snap scrolling
    public boolean usesSnapScrolling() {
        return snapScrolling;
    }

    /// Returns whether the selected focal item is centered in the viewport.
    ///
    /// @return `true` when the selected item is centered
    public boolean centersFocalItem() {
        return centeredFocalItem;
    }

    /// Returns whether this layout preserves authored item widths.
    ///
    /// @return `true` when child preferred widths determine rendered widths
    public boolean preservesAuthoredWidths() {
        return preservesAuthoredWidths;
    }
}