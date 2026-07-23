// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Rectangle2D;
import javafx.scene.shape.FillRule;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.jetbrains.annotations.NotNullByDefault;

/// Creates the small Material-style vector icon set used by the HMCL demo.
@NotNullByDefault
final class HMCLDemoIcons {
    /// The shared Material icon coordinate system.
    private static final Rectangle2D VIEW_BOX = new Rectangle2D(0.0, 0.0, 24.0, 24.0);

    /// Home.
    static final String HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";

    /// Version / instance list.
    static final String INSTANCES =
            "M4 5h2v2H4zm4 0h12v2H8zM4 11h2v2H4zm4 0h12v2H8zM4 17h2v2H4zm4 0h12v2H8z";

    /// Download.
    static final String DOWNLOAD = "M11 3h2v9l3.5-3.5 1.4 1.4L12 15.8 6.1 9.9l1.4-1.4L11 12zM4 19h16v2H4z";

    /// Accounts.
    static final String ACCOUNTS =
            "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm0 2c-4.4 0-8 2.2-8 5v1h16v-1c0-2.8-3.6-5-8-5z";

    /// Settings.
    static final String SETTINGS =
            "M19.4 13.5c.1-.5.1-1 .1-1.5s0-1-.1-1.5l2.1-1.6-2-3.5-2.5 1a7 7 0 0 0-2.6-1.5L14 2h-4l-.4 2.9A7 7 0 0 0 7 6.4l-2.5-1-2 3.5 2.1 1.6a9.2 9.2 0 0 0 0 3L2.5 15.1l2 3.5 2.5-1a7 7 0 0 0 2.6 1.5L10 22h4l.4-2.9a7 7 0 0 0 2.6-1.5l2.5 1 2-3.5zM12 15.5A3.5 3.5 0 1 1 12 8a3.5 3.5 0 0 1 0 7.5z";

    /// Back.
    static final String BACK = "M20 11H7.8l5.6-5.6L12 4l-8 8 8 8 1.4-1.4L7.8 13H20z";

    /// Add.
    static final String ADD = "M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z";

    /// Refresh.
    static final String REFRESH =
            "M17.7 6.3A7.9 7.9 0 0 0 12 4a8 8 0 0 0-7.8 9.8l-2 .6A10 10 0 0 1 12 2c2.8 0 5.3 1.1 7.1 2.9L22 2v8h-8zM6.3 17.7A7.9 7.9 0 0 0 12 20a8 8 0 0 0 7.8-9.8l2-.6A10 10 0 0 1 12 22c-2.8 0-5.3-1.1-7.1-2.9L2 22v-8h8z";

    /// Help outline (Material "help_outline" glyph).
    static final String HELP =
            "M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zM12 6c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z";

    /// Minimize.
    static final String MINIMIZE = "M5 11h14v2H5z";

    /// Close.
    static final String CLOSE =
            "M6.4 5 12 10.6 17.6 5 19 6.4 13.4 12 19 17.6 17.6 19 12 13.4 6.4 19 5 17.6 10.6 12 5 6.4z";

    /// Chat / feedback.
    static final String CHAT = "M4 4h16v12H7l-3 3zm2 2v8h10.8l1.2 1.2V6z";

    /// Multiplayer / group.
    static final String GROUP =
            "M16 11a4 4 0 1 0-3.9-5H12a4 4 0 1 0-3.9 5A6 6 0 0 0 2 17v2h8v-2a4 4 0 0 1 4-4h0a4 4 0 0 1 4 4v2h4v-2a6 6 0 0 0-6-6z";

    /// Folder.
    static final String FOLDER = "M10 4H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-8z";

    /// Manage / tune.
    static final String MANAGE =
            "M3 17v2h6v-2zm0-6v2h12v-2zm0-6v2h18V5zM19.4 19.6l1.4-1.4-2.1-2.1 1.4-1.4 2.1 2.1 1.4-1.4 1.4 1.4-1.4 1.4 2.1 2.1-1.4 1.4-2.1-2.1-1.4 1.4z";

    /// Extension / mods.
    static final String EXTENSION =
            "M19 3h-4.2A3 3 0 0 0 12 1a3 3 0 0 0-2.8 2H5a2 2 0 0 0-2 2v4.2A3 3 0 0 0 1 12a3 3 0 0 0 2 2.8V19a2 2 0 0 0 2 2h4.2A3 3 0 0 0 12 23a3 3 0 0 0 2.8-2H19a2 2 0 0 0 2-2v-4.2A3 3 0 0 0 23 12a3 3 0 0 0-2-2.8V5a2 2 0 0 0-2-2z";

    /// World / public.
    static final String WORLD =
            "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm6.9 6h-3.2a15.4 15.4 0 0 0-1.4-3.6A8 8 0 0 1 18.9 8zM12 4c.8 1 1.5 2.4 1.9 4H10c.4-1.6 1.1-3 1.9-4zM4.3 14a8 8 0 0 1 0-4h3.6a17.5 17.5 0 0 0 0 4zm.8 2h3.2a15.4 15.4 0 0 0 1.4 3.6A8 8 0 0 1 5.1 16zM8.3 8H5.1a8 8 0 0 1 3.4-3.6A15.4 15.4 0 0 0 8.3 8zM12 20c-.8-1-1.5-2.4-1.9-4h3.8c-.4 1.6-1.1 3-1.9 4zm2.3-6H9.7a15.6 15.6 0 0 1 0-4h4.6a15.6 15.6 0 0 1 0 4zm.4 5.6A15.4 15.4 0 0 0 15.7 16h3.2a8 8 0 0 1-3.4 3.6zM15.7 8a15.4 15.4 0 0 0-1.4-3.6A8 8 0 0 1 18.9 8z";

    /// Image / resource pack.
    static final String IMAGE =
            "M21 19V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2zM8.5 13.5l2.5 3 3.5-4.5 4.5 6H5z";

    /// Info.
    static final String INFO =
            "M11 7h2v2h-2zm0 4h2v6h-2zm1-9a10 10 0 1 0 0 20 10 10 0 0 0 0-20z";

    /// Java / code.
    static final String CODE = "M9.4 16.6 4.8 12l4.6-4.6L8 6l-6 6 6 6zm5.2 0 4.6-4.6-4.6-4.6L16 6l6 6-6 6z";

    /// Play.
    static final String PLAY = "M8 5v14l11-7z";

    /// Arrow drop up used by HMCL's launch menu button.
    static final String ARROW_DROP_UP = "M7 14l5-5 5 5z";

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

    /// Creates the back icon and mirrors it under right-to-left orientation.
    ///
    /// @return the configured back icon
    static M3SVGIcon back() {
        M3SVGIcon icon = create(BACK);
        icon.setAutoMirrored(true);
        return icon;
    }
}
