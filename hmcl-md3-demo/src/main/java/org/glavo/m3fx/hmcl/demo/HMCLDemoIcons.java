// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Rectangle2D;
import javafx.scene.shape.FillRule;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.jetbrains.annotations.NotNullByDefault;

/// Creates the small Material-style vector icon set used by the HMCL demo.
///
/// The application-local paths keep navigation and common operations independent of HMCL's JFoenix-era icon
/// registry and avoid adding an icon dependency to the M3FX library.
@NotNullByDefault
final class HMCLDemoIcons {
    /// The shared Material icon coordinate system.
    private static final Rectangle2D VIEW_BOX = new Rectangle2D(0.0, 0.0, 24.0, 24.0);

    /// The home icon path.
    static final String HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";

    /// The installed instances icon path.
    static final String INSTANCES =
            "M4 5h2v2H4zm4 0h12v2H8zM4 11h2v2H4zm4 0h12v2H8zM4 17h2v2H4zm4 0h12v2H8z";

    /// The discover and download icon path.
    static final String DISCOVER = "M11 3h2v9l3.5-3.5 1.4 1.4L12 15.8 6.1 9.9l1.4-1.4L11 12zM4 19h16v2H4z";

    /// The accounts icon path.
    static final String ACCOUNTS =
            "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm0 2c-4.4 0-8 2.2-8 5v1h16v-1c0-2.8-3.6-5-8-5z";

    /// The settings icon path.
    static final String SETTINGS =
            "M19.4 13.5c.1-.5.1-1 .1-1.5s0-1-.1-1.5l2.1-1.6-2-3.5-2.5 1a7 7 0 0 0-2.6-1.5L14 2h-4l-.4 2.9A7 7 0 0 0 7 6.4l-2.5-1-2 3.5 2.1 1.6a9.2 9.2 0 0 0 0 3L2.5 15.1l2 3.5 2.5-1a7 7 0 0 0 2.6 1.5L10 22h4l.4-2.9a7 7 0 0 0 2.6-1.5l2.5 1 2-3.5zM12 15.5A3.5 3.5 0 1 1 12 8a3.5 3.5 0 0 1 0 7.5z";

    /// The back arrow icon path.
    static final String BACK = "M20 11H7.8l5.6-5.6L12 4l-8 8 8 8 1.4-1.4L7.8 13H20z";

    /// The menu icon path.
    static final String MENU = "M3 6h18v2H3zm0 5h18v2H3zm0 5h18v2H3z";

    /// The palette icon path.
    static final String PALETTE =
            "M12 3a9 9 0 0 0 0 18h1.5a1.5 1.5 0 0 0 0-3H12a1.5 1.5 0 0 1 0-3h1.8A7.2 7.2 0 0 0 21 7.8C21 5.1 16.9 3 12 3zM6.5 13A1.5 1.5 0 1 1 6.5 10a1.5 1.5 0 0 1 0 3zm2-4A1.5 1.5 0 1 1 8.5 6a1.5 1.5 0 0 1 0 3zm4-1A1.5 1.5 0 1 1 12.5 5a1.5 1.5 0 0 1 0 3zm4 2A1.5 1.5 0 1 1 16.5 7a1.5 1.5 0 0 1 0 3z";

    /// The add icon path.
    static final String ADD = "M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z";

    /// The play icon path.
    static final String PLAY = "M8 5v14l11-7z";

    /// The search icon path.
    static final String SEARCH =
            "M9.5 3a6.5 6.5 0 1 1 0 13 6.5 6.5 0 0 1 0-13zm0 2a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0-9zm5.4 8.5L22 20.6 20.6 22l-7.1-7.1z";

    /// Prevents utility-class instantiation.
    private HMCLDemoIcons() {
    }

    /// Creates a semantic-color SVG icon.
    ///
    /// @param path the SVG path content
    /// @return the configured icon
    static M3SVGIcon create(String path) {
        M3SVGIcon icon = new M3SVGIcon(path, VIEW_BOX);
        icon.setFillRule(FillRule.EVEN_ODD);
        icon.setMouseTransparent(true);
        icon.getStyleClass().add("hmcl-demo-icon");
        return icon;
    }

    /// Creates a directional icon that mirrors under right-to-left orientation.
    ///
    /// @param path the SVG path content
    /// @return the configured directional icon
    static M3SVGIcon directional(String path) {
        M3SVGIcon icon = create(path);
        icon.setAutoMirrored(true);
        return icon;
    }
}
