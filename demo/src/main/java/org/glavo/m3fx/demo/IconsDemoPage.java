// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonVariant;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3SVGIcon;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Icons component showcase page.
@NotNullByDefault
final class IconsDemoPage extends DemoPageSupport {
    /// A Material Symbols add path using the coordinates published by the official SVG asset.
    private static final String MATERIAL_SYMBOL_ADD_PATH =
            "M440-120v-320H120v-80h320v-320h80v320h320v80H520v320h-80Z";

    /// The viewport published with the Material Symbols add path.
    private static final Rectangle2D MATERIAL_SYMBOL_VIEW_BOX =
            new Rectangle2D(0.0, -960.0, 960.0, 960.0);

    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    IconsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the icon component page.
    Node createContent() {
        Node disabledIcon = createDemoIcon("notifications", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT);
        disabledIcon.setDisable(true);
        M3IconButton svgIconButton = new M3IconButton(
                createMaterialSymbolIcon(M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT)
        );
        M3IconToggleButton svgToggleButton = new M3IconToggleButton(
                createDemoIcon("favorite", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT)
        );
        svgToggleButton.setVariant(M3IconToggleButtonVariant.TONAL);
        svgToggleButton.setSelected(true);

        M3Icon localGlyph = new M3Icon("favorite", M3IconSize.MEDIUM, M3IconVariant.PRIMARY);
        localGlyph.setTint(Color.web("#006A6A"));
        M3SVGIcon localSvg = createDemoIcon("favorite", M3IconSize.MEDIUM, M3IconVariant.PRIMARY);
        localSvg.setTint(Color.web("#9C4146"));

        return createGallery(
                createShowcaseGroup(
                        "Sizes",
                        createDemoIcon("search", M3IconSize.SMALL, M3IconVariant.PRIMARY),
                        createDemoIcon("search", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                        createDemoIcon("search", M3IconSize.LARGE, M3IconVariant.PRIMARY),
                        createDemoIcon("search", M3IconSize.EXTRA_LARGE, M3IconVariant.PRIMARY)
                ),
                createShowcaseGroup(
                        "Color Variants",
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.PRIMARY),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.SECONDARY),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.TERTIARY),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.ERROR),
                        createDemoIcon("star", M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE),
                        disabledIcon
                ),
                createShowcaseGroup(
                        "Source Viewports",
                        createDemoIcon("search", M3IconSize.LARGE, M3IconVariant.PRIMARY),
                        createMaterialSymbolIcon(M3IconSize.LARGE, M3IconVariant.PRIMARY)
                ),
                createShowcaseGroup(
                        "Button Usage",
                        createIconButton("info"),
                        svgIconButton,
                        svgToggleButton,
                        createFab("add", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.SMALL),
                        createFab("spark", M3FloatingActionButtonVariant.TERTIARY, M3FloatingActionButtonSize.REGULAR)
                ),
                createShowcaseGroup(
                        "Local Colors",
                        localGlyph,
                        localSvg
                )
        );
    }

    /// Creates an SVG icon from an official Material Symbols 960-unit source viewport.
    ///
    /// @param size    the semantic rendered size
    /// @param variant the semantic color role
    /// @return the configured Material Symbols icon
    private static M3SVGIcon createMaterialSymbolIcon(M3IconSize size, M3IconVariant variant) {
        M3SVGIcon icon = new M3SVGIcon(MATERIAL_SYMBOL_ADD_PATH, MATERIAL_SYMBOL_VIEW_BOX);
        icon.setSize(size);
        icon.setVariant(variant);
        icon.getProperties().put(DemoIcons.ICON_NAME_PROPERTY, "add");
        icon.getStyleClass().add("demo-sample-icon");
        icon.setMouseTransparent(true);
        return icon;
    }
}
