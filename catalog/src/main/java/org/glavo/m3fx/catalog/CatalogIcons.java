// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import org.jetbrains.annotations.NotNullByDefault;

/// Creates the small vector icon set used exclusively by the M3FX Catalog application.
///
/// M3FX intentionally does not bundle a production icon library. Catalog icons are local presentation assets that
/// distinguish component families and exercise graphic slots without adding an icon dependency to the core module.
@NotNullByDefault
final class CatalogIcons {
    /// The home icon path.
    static final String HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";

    /// The touch-action icon path.
    static final String TOUCH_APP =
            "M18.2 8.4c-.5-.2-1-.1-1.4.2l-.8.6V5.5a1.5 1.5 0 0 0-3 0v2.7h-1V3.5a1.5 1.5 0 0 0-3 0v4.7h-1V5.5a1.5 1.5 0 0 0-3 0v8.8l-1.8-1.8a1.5 1.5 0 0 0-2.1 2.1l6.2 6.2c.8.8 1.8 1.2 2.9 1.2h4.3c2.2 0 4-1.8 4-4V9.8c0-.6-.3-1.1-.8-1.4z";

    /// The edit icon path.
    static final String EDIT =
            "M3 17.3V21h3.8L17.9 9.9l-3.8-3.8zM20.7 7c.4-.4.4-1 0-1.4l-2.3-2.3c-.4-.4-1-.4-1.4 0l-1.8 1.8 3.8 3.8z";

    /// The navigation pointer icon path.
    static final String NAVIGATION = "M12 2 4.5 20.3l.7.7 6.8-3 6.8 3 .7-.7z";

    /// The notification icon path.
    static final String NOTIFICATIONS =
            "M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.1-1.6-5.6-4.5-6.3V4c0-.8-.7-1.5-1.5-1.5S10.5 3.2 10.5 4v.7C7.6 5.4 6 7.9 6 11v5l-2 2v1h16v-1z";

    /// The add icon path.
    static final String ADD = "M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z";

    /// The favorite icon path.
    static final String FAVORITE =
            "M12 21.4l-1.4-1.3C5.4 15.4 2 12.3 2 8.5 2 5.4 4.4 3 7.5 3c1.7 0 3.4.8 4.5 2 1.1-1.2 2.8-2 4.5-2C19.6 3 22 5.4 22 8.5c0 3.8-3.4 6.9-8.6 11.6z";

    /// The settings icon path.
    static final String SETTINGS =
            "M19.4 13.5c.1-.5.1-1 .1-1.5s0-1-.1-1.5l2.1-1.6-2-3.5-2.5 1a7 7 0 0 0-2.6-1.5L14 2h-4l-.4 2.9A7 7 0 0 0 7 6.4l-2.5-1-2 3.5 2.1 1.6a9.2 9.2 0 0 0 0 3L2.5 15.1l2 3.5 2.5-1a7 7 0 0 0 2.6 1.5L10 22h4l.4-2.9a7 7 0 0 0 2.6-1.5l2.5 1 2-3.5zM12 15.5A3.5 3.5 0 1 1 12 8a3.5 3.5 0 0 1 0 7.5z";

    /// The back arrow icon path.
    static final String ARROW_BACK = "M20 11H7.8l5.6-5.6L12 4l-8 8 8 8 1.4-1.4L7.8 13H20z";

    /// The forward arrow icon path.
    static final String ARROW_FORWARD = "M12 4l-1.4 1.4 5.6 5.6H4v2h12.2l-5.6 5.6L12 20l8-8z";

    /// The palette icon path.
    static final String PALETTE =
            "M12 3a9 9 0 0 0 0 18h1.5a1.5 1.5 0 0 0 0-3H12a1.5 1.5 0 0 1 0-3h1.8A7.2 7.2 0 0 0 21 7.8C21 5.1 16.9 3 12 3zM6.5 13A1.5 1.5 0 1 1 6.5 10a1.5 1.5 0 0 1 0 3zm2-4A1.5 1.5 0 1 1 8.5 6a1.5 1.5 0 0 1 0 3zm4-1A1.5 1.5 0 1 1 12.5 5a1.5 1.5 0 0 1 0 3zm4 2A1.5 1.5 0 1 1 16.5 7a1.5 1.5 0 0 1 0 3z";

    /// The vertical overflow icon path.
    static final String MORE_VERTICAL =
            "M12 8a2 2 0 1 0 0-4 2 2 0 0 0 0 4zm0 2a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm0 6a2 2 0 1 0 0 4 2 2 0 0 0 0-4z";

    /// The reset icon path.
    static final String RESET = "M12 5V2L8 6l4 4V7a5 5 0 1 1-4.6 3H5.3A7 7 0 1 0 12 5z";

    /// The bottom app bar icon path.
    static final String BOTTOM_APP_BAR = "M3 4h18v10H3zm0 13h18v3H3z";

    /// The bottom sheet icon path.
    static final String BOTTOM_SHEET = "M4 3h16v18H4zm2 2v4h12V5zm0 6v8h12v-8z";

    /// The card icon path.
    static final String CARD = "M4 3h16a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm0 2v14h16V5z";

    /// The carousel icon path.
    static final String CAROUSEL = "M1 6h4v12H1zm6-2h10v16H7zm12 2h4v12h-4z";

    /// The dialog icon path.
    static final String DIALOG = "M5 3h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H8l-5 4V5a2 2 0 0 1 2-2z";

    /// The list icon path.
    static final String LIST =
            "M4 5h2v2H4zm4 0h12v2H8zM4 11h2v2H4zm4 0h12v2H8zM4 17h2v2H4zm4 0h12v2H8z";

    /// The connected button-group icon path.
    static final String BUTTON_GROUP =
            "M3 7h18v10H3zm2 2v6h4V9zm6 0v6h4V9zm6 0v6h2V9z";

    /// The extended floating action button icon path.
    static final String EXTENDED_FAB =
            "M6 5h12a7 7 0 0 1 0 14H6A7 7 0 0 1 6 5zm2 3v3H5v2h3v3h2v-3h3v-2h-3V8z";

    /// The floating action button menu icon path.
    static final String FAB_MENU =
            "M11 3h2v5h-2zM11 16h2v5h-2zM3 11h5v2H3zm13 0h5v2h-5zM10 10h4v4h-4z";

    /// The floating action button icon path.
    static final String FLOATING_ACTION =
            "M12 2a10 10 0 1 1 0 20 10 10 0 0 1 0-20zm-1 5v4H7v2h4v4h2v-4h4v-2h-4V7z";

    /// The floating toolbar icon path.
    static final String TOOLBAR =
            "M3 7h18v10H3zm2 2v6h14V9zM7 10h2v4H7zm4 0h2v4h-2zm4 0h2v4h-2z";

    /// The split button icon path.
    static final String SPLIT_BUTTON =
            "M3 6h18v12H3zm2 2v8h10V8zm12 0v8h2V8zm1 3-2 3h4z";

    /// The checkbox icon path.
    static final String CHECKBOX =
            "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm5.5 13.5L6 12l1.4-1.4 3.1 3.1 6.1-6.1L18 9z";

    /// The chip icon path.
    static final String CHIP =
            "M7 5h10a7 7 0 0 1 0 14H7A7 7 0 0 1 7 5zm0 2a5 5 0 0 0 0 10h10a5 5 0 0 0 0-10z";

    /// The date picker icon path.
    static final String CALENDAR =
            "M7 2h2v2h6V2h2v2h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2zm12 8H5v10h14zM5 6v2h14V6z";

    /// The radio button icon path.
    static final String RADIO =
            "M12 2a10 10 0 1 1 0 20 10 10 0 0 1 0-20zm0 2a8 8 0 1 0 0 16 8 8 0 0 0 0-16zm0 4a4 4 0 1 1 0 8 4 4 0 0 1 0-8z";

    /// The search icon path.
    static final String SEARCH =
            "M9.5 3a6.5 6.5 0 1 1 0 13 6.5 6.5 0 0 1 0-13zm0 2a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0-9zm5.4 8.5L22 20.6 20.6 22l-7.1-7.1z";

    /// The segmented button icon path.
    static final String SEGMENTED_BUTTON =
            "M3 6h18v12H3zm2 2v8h4V8zm6 0v8h4V8zm6 0v8h2V8z";

    /// The slider icon path.
    static final String SLIDER =
            "M4 3h2v8h2v2H6v8H4v-8H2v-2h2zm8 0h2v3h2v2h-2v13h-2V8h-2V6h2zm8 0h2v11h2v2h-2v5h-2v-5h-2v-2h2z";

    /// The switch icon path.
    static final String SWITCH =
            "M7 5h10a7 7 0 0 1 0 14H7A7 7 0 0 1 7 5zm10 2a5 5 0 1 0 0 10 5 5 0 0 0 0-10z";

    /// The text field icon path.
    static final String TEXT_FIELD = "M3 4v3h6.5v13h5V7H21V4zM2 18h5v2H2zm15 0h5v2h-5z";

    /// The time picker icon path.
    static final String TIME =
            "M12 2a10 10 0 1 1 0 20 10 10 0 0 1 0-20zm0 2a8 8 0 1 0 0 16 8 8 0 0 0 0-16zm-1 3h2v5.4l4 2.3-1 1.7-5-2.9z";

    /// The navigation bar icon path.
    static final String NAVIGATION_BAR =
            "M2 15h20v6H2zm3 2v2h2v-2zm6 0v2h2v-2zm6 0v2h2v-2z";

    /// The navigation drawer icon path.
    static final String NAVIGATION_DRAWER = "M3 3h18v18H3zm2 2v14h5V5zm7 0v14h7V5z";

    /// The navigation rail icon path.
    static final String NAVIGATION_RAIL =
            "M4 2h7v20H4zm2 3v2h3V5zm0 5v2h3v-2zm0 5v2h3v-2zM13 4h7v2h-7zm0 5h7v2h-7zm0 5h7v2h-7z";

    /// The tab bar icon path.
    static final String TABS =
            "M3 4h18v16H3zm2 2v4h4V6zm6 0v4h4V6zm6 0v4h2V6zM5 16h4v2H5z";

    /// The top app bar icon path.
    static final String TOP_APP_BAR = "M3 3h18v18H3zm2 2v5h14V5zm0 7v7h14v-7z";

    /// The loading indicator icon path.
    static final String LOADING =
            "M12 2V0L8 4l4 4V6a6 6 0 0 1 5.7 8H20A8 8 0 0 0 12 2zm0 16a6 6 0 0 1-5.7-8H4a8 8 0 0 0 8 12v2l4-4-4-4z";

    /// The progress indicator icon path.
    static final String PROGRESS =
            "M12 2a10 10 0 1 1-7.1 2.9L6.3 6.3A8 8 0 1 0 12 4zm-1 1h2v9h-2z";

    /// The snackbar icon path.
    static final String SNACKBAR =
            "M3 7h18v10H3zm2 2v6h14V9zm1 2h8v2H6zm10 0h2v2h-2z";

    /// The tooltip icon path.
    static final String TOOLTIP =
            "M4 3h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H9l-5 4v-4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm7 4h2v2h-2zm0 4h2v4h-2z";

    /// The typography icon path.
    static final String TYPOGRAPHY = "M4 4v3h6.5v13h3V7H20V4zM3 17h5v3H3zm13 0h5v3h-5z";

    /// Prevents utility class instantiation.
    private CatalogIcons() {
    }

    /// Creates a catalog SVG icon with the default semantic color.
    ///
    /// @param path the SVG path content
    /// @return a mouse-transparent vector icon
    static SVGPath create(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFillRule(FillRule.EVEN_ODD);
        icon.getStyleClass().add("catalog-icon");
        icon.setMouseTransparent(true);
        return icon;
    }
}
