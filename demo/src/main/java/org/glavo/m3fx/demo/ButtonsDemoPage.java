// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonShape;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Buttons component showcase page.
@NotNullByDefault
final class ButtonsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ButtonsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the button component page.
    Node createContent() {
        M3Button disabledFilled = new M3Button("Disabled", M3ButtonVariant.FILLED);
        disabledFilled.setDisable(true);

        M3Button mediumWithIcon = createSizedButton(
                "Create",
                M3ButtonVariant.FILLED,
                M3ButtonSize.MEDIUM,
                M3ButtonShape.ROUND
        );
        mediumWithIcon.setGraphic(createIconViewport(
                DemoIcons.onPrimary("add"),
                defaultButtonGlyphSize(M3ButtonSize.MEDIUM)
        ));

        M3Button largeSquareWithIcon = createSizedButton(
                "Launch",
                M3ButtonVariant.TONAL,
                M3ButtonSize.LARGE,
                M3ButtonShape.SQUARE
        );
        largeSquareWithIcon.setGraphic(createIconViewport(
                DemoIcons.onSecondaryContainer("send"),
                defaultButtonGlyphSize(M3ButtonSize.LARGE)
        ));

        M3Button localColors = new M3Button("Local colors", M3ButtonVariant.FILLED);
        localColors.setContainerColor(Color.web("#006A6A"));
        localColors.setContentColor(Color.web("#FFFFFF"));

        return createGallery(
                createShowcaseGroup(
                        "Button Variants",
                        new M3Button("Filled", M3ButtonVariant.FILLED),
                        new M3Button("Tonal", M3ButtonVariant.TONAL),
                        new M3Button("Outlined", M3ButtonVariant.OUTLINED),
                        new M3Button("Text", M3ButtonVariant.TEXT),
                        new M3Button("Elevated", M3ButtonVariant.ELEVATED),
                        disabledFilled
                ),
                createShowcaseGroup(
                        "Button Sizes",
                        createSizedButton("XS", M3ButtonVariant.FILLED, M3ButtonSize.EXTRA_SMALL, M3ButtonShape.ROUND),
                        createSizedButton("Small", M3ButtonVariant.FILLED, M3ButtonSize.SMALL, M3ButtonShape.ROUND),
                        createSizedButton("Medium", M3ButtonVariant.TONAL, M3ButtonSize.MEDIUM, M3ButtonShape.ROUND),
                        createSizedButton("Large", M3ButtonVariant.TONAL, M3ButtonSize.LARGE, M3ButtonShape.ROUND),
                        createSizedButton("XL", M3ButtonVariant.OUTLINED, M3ButtonSize.EXTRA_LARGE, M3ButtonShape.ROUND)
                ),
                createShowcaseGroup(
                        "Button Shapes",
                        createSizedButton("Round", M3ButtonVariant.FILLED, M3ButtonSize.SMALL, M3ButtonShape.ROUND),
                        createSizedButton("Square", M3ButtonVariant.FILLED, M3ButtonSize.SMALL, M3ButtonShape.SQUARE),
                        createSizedButton("Medium square", M3ButtonVariant.OUTLINED, M3ButtonSize.MEDIUM, M3ButtonShape.SQUARE),
                        createSizedButton("Large square", M3ButtonVariant.TONAL, M3ButtonSize.LARGE, M3ButtonShape.SQUARE)
                ),
                createShowcaseGroup(
                        "Buttons With Icons",
                        mediumWithIcon,
                        largeSquareWithIcon
                ),
                createShowcaseGroup(
                        "Local Colors",
                        localColors
                )
        );
    }

    /// Creates a sample button with explicit Material size and shape roles.
    ///
    /// @param text    the button label
    /// @param variant the button color and elevation variant
    /// @param size    the Material button size
    /// @param shape   the resting Material button shape
    /// @return the configured sample button
    private static M3Button createSizedButton(
            String text,
            M3ButtonVariant variant,
            M3ButtonSize size,
            M3ButtonShape shape
    ) {
        M3Button button = new M3Button(text, variant);
        button.setSize(size);
        button.setButtonShape(shape);
        return button;
    }

    /// Returns the default glyph size for a labeled Material button size.
    ///
    /// @param size the Material button size
    /// @return the labeled button glyph size in pixels
    private static double defaultButtonGlyphSize(M3ButtonSize size) {
        return switch (size) {
            case EXTRA_SMALL, SMALL -> 20.0;
            case MEDIUM -> 24.0;
            case LARGE -> 32.0;
            case EXTRA_LARGE -> 40.0;
        };
    }
}
