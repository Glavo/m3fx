// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3SplitButton;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the SplitButtons component showcase page.
@NotNullByDefault
final class SplitButtonsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SplitButtonsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the split button component page.
    Node createContent() {
        M3SplitButton tonal = createSplitButton("Create", M3ButtonVariant.TONAL);
        M3SplitButton outlined = createSplitButton("Export", M3ButtonVariant.OUTLINED);
        M3SplitButton filled = createSplitButton("Publish", M3ButtonVariant.FILLED);
        M3SplitButton elevated = createSplitButton("Save", M3ButtonVariant.ELEVATED);
        tonal.setGraphic(createSplitButtonGraphic("create", M3ButtonVariant.TONAL, M3IconSize.SMALL));
        outlined.setGraphic(createSplitButtonGraphic("share", M3ButtonVariant.OUTLINED, M3IconSize.SMALL));
        filled.setGraphic(createSplitButtonGraphic("send", M3ButtonVariant.FILLED, M3IconSize.SMALL));
        elevated.setGraphic(createSplitButtonGraphic("save", M3ButtonVariant.ELEVATED, M3IconSize.SMALL));

        M3SplitButton disabled = createSplitButton("Disabled", M3ButtonVariant.TONAL);
        disabled.setDisable(true);

        M3SplitButton extraSmall = createSplitButton("XS", M3ButtonVariant.TONAL);
        M3SplitButton small = createSplitButton("Small", M3ButtonVariant.TONAL);
        M3SplitButton medium = createSplitButton("Medium", M3ButtonVariant.TONAL);
        M3SplitButton large = createSplitButton("Large", M3ButtonVariant.TONAL);
        M3SplitButton extraLarge = createSplitButton("XL", M3ButtonVariant.TONAL);
        extraSmall.setSize(M3ButtonSize.EXTRA_SMALL);
        small.setSize(M3ButtonSize.SMALL);
        medium.setSize(M3ButtonSize.MEDIUM);
        large.setSize(M3ButtonSize.LARGE);
        extraLarge.setSize(M3ButtonSize.EXTRA_LARGE);
        extraSmall.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.SMALL));
        small.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.SMALL));
        medium.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.MEDIUM));
        large.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.LARGE));
        extraLarge.setGraphic(createSplitButtonGraphic("edit", M3ButtonVariant.TONAL, M3IconSize.EXTRA_LARGE));

        return createGallery(
                createShowcaseGroup("Color Roles", tonal, outlined, filled, elevated),
                createShowcaseGroup("Disabled", disabled),
                createShowcaseGroup("Size Scale", extraSmall, small, medium, large, extraLarge)
        );
    }

    /// Creates a split-button leading icon whose color follows the button variant.
    private static StackPane createSplitButtonGraphic(
            String iconName,
            M3ButtonVariant variant,
            M3IconSize size
    ) {
        SVGPath icon = switch (variant) {
            case FILLED -> DemoIcons.onPrimary(iconName);
            case TONAL -> DemoIcons.onSecondaryContainer(iconName);
            case OUTLINED, TEXT, ELEVATED -> DemoIcons.primary(iconName);
        };
        return createIconViewport(
                icon,
                defaultIconGlyphSize(size),
                "demo-split-button-leading-icon"
        );
    }
}
