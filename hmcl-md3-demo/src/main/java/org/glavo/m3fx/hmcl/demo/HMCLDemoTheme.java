// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.scene.paint.Color;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3TextStyle;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.m3fx.tokens.M3TypographyTokens;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the compact desktop theme used by the HMCL MD3 demo.
///
/// List geometry and body type roles are product density choices for an ~800×500 launcher window. Component defaults
/// still come from M3FX tokens; this class only replaces the groups that the demo intentionally densifies.
@NotNullByDefault
final class HMCLDemoTheme {
    /// Preferred one-line list/setting row height for the demo.
    private static final double LIST_ONE_LINE_HEIGHT = 52.0;

    /// Preferred two-line list/setting row height for the demo.
    private static final double LIST_TWO_LINE_HEIGHT = 64.0;

    /// Preferred three-line list/setting row height for the demo.
    private static final double LIST_THREE_LINE_HEIGHT = 76.0;

    /// Horizontal padding inside list rows.
    private static final double LIST_HORIZONTAL_PADDING = 16.0;

    /// Vertical padding inside list rows.
    private static final double LIST_VERTICAL_PADDING = 8.0;

    /// Spacing between list-row content regions.
    private static final double LIST_CONTENT_SPACING = 12.0;

    /// Prevents utility-class instantiation.
    private HMCLDemoTheme() {
    }

    /// Creates the demo theme for the supplied appearance settings.
    ///
    /// @param seedColor  the dynamic-color seed
    /// @param profile    the Material profile
    /// @param brightness the light/dark scheme
    /// @return an immutable theme with compact desktop list and body type tokens
    static M3Theme create(Color seedColor, M3Profile profile, Brightness brightness) {
        M3Theme baseline = M3Theme.fromSeed(seedColor, profile, brightness, M3Density.standard());
        M3TokenSet tokens = baseline.tokens();

        M3ComponentTokens.ListItemTokens listItem = tokens.componentTokens().listItem();
        M3ComponentTokens.ListItemTokens compactListItem = new M3ComponentTokens.ListItemTokens(
                LIST_ONE_LINE_HEIGHT,
                LIST_TWO_LINE_HEIGHT,
                LIST_THREE_LINE_HEIGHT,
                listItem.containerShape(),
                LIST_HORIZONTAL_PADDING,
                LIST_VERTICAL_PADDING,
                LIST_CONTENT_SPACING,
                listItem.segmentedGap(),
                listItem.segmentedContainerShape(),
                listItem.segmentedHoverContainerShape(),
                listItem.segmentedActiveContainerShape(),
                listItem.segmentedDisabledContainerShape(),
                listItem.sectionHeaderHeight(),
                listItem.sectionHeaderHorizontalPadding()
        );

        M3ComponentTokens componentTokens = M3ComponentTokens.builder(tokens.componentTokens())
                .listItem(compactListItem)
                .build();

        M3TypographyTokens typography = densifyBodyType(tokens.typographyTokens());

        return M3Theme.fromTokenSet(
                M3TokenSet.builder(tokens)
                        .componentTokens(componentTokens)
                        .typographyTokens(typography)
                        .build()
        );
    }

    /// Shrinks body and label roles used by list rows and compact chrome while preserving the larger display scale.
    private static M3TypographyTokens densifyBodyType(M3TypographyTokens source) {
        String family = source.bodyLarge().fontFamily();
        return M3TypographyTokens.builder(source)
                .titleSmall(M3TextStyle.of(family, 14.0, 20.0, 600, 0.10))
                .labelLarge(M3TextStyle.of(family, 14.0, 20.0, 500, 0.10))
                .labelMedium(M3TextStyle.of(family, 12.0, 16.0, 500, 0.50))
                .labelSmall(M3TextStyle.of(family, 11.0, 16.0, 500, 0.50))
                .bodyLarge(M3TextStyle.of(family, 14.0, 20.0, 500, 0.15))
                .bodyMedium(M3TextStyle.of(family, 12.0, 16.0, 400, 0.25))
                .bodySmall(M3TextStyle.of(family, 12.0, 16.0, 400, 0.40))
                .build();
    }
}
