// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests explicit token factory methods.
@NotNullByDefault
final class M3TokenFactoryTest {
    /// Verifies that system token groups can be created with explicit values.
    @Test
    void createsExplicitSystemTokens() {
        M3TextStyle displayLarge = M3TextStyle.create("Demo", 60.0, 68.0, 600);
        M3TextStyle headlineMedium = M3TextStyle.create("Demo", 30.0, 38.0, 500);
        M3TextStyle titleLarge = M3TextStyle.create("Demo", 24.0, 32.0, 500);
        M3TextStyle labelLarge = M3TextStyle.create("Demo", 15.0, 22.0, 600);
        M3TextStyle bodyLarge = M3TextStyle.create("Demo", 17.0, 26.0, 400);
        M3TextStyle bodyMedium = M3TextStyle.create("Demo", 15.0, 23.0, 400);

        M3TypographyTokens typography = M3TypographyTokens.create(
                displayLarge,
                headlineMedium,
                titleLarge,
                labelLarge,
                bodyLarge,
                bodyMedium
        );
        M3ShapeTokens shape = M3ShapeTokens.create(2.0, 6.0, 10.0, 18.0, 30.0, 999.0);
        M3ElevationTokens elevation = M3ElevationTokens.create(0.0, 2.0, 4.0, 8.0, 12.0, 16.0);
        M3MotionTokens motion = M3MotionTokens.create(90, 210, 420);
        M3StateLayerTokens stateLayer = M3StateLayerTokens.create(0.05, 0.11, 0.13, 0.17, 0.14, 0.42);

        assertSame(displayLarge, typography.displayLarge());
        assertEquals(30.0, shape.extraLarge(), 0.0001);
        assertEquals(16.0, elevation.level5(), 0.0001);
        assertEquals(210, motion.mediumDuration());
        assertEquals(0.42, stateLayer.disabledContentOpacity(), 0.0001);
        assertTrue(typography.toStyleDeclarations().contains("-m3-typescale-display-large-font-family: \"Demo\""));
        assertTrue(stateLayer.toStyleDeclarations().contains("-m3-state-disabled-content-opacity: 0.42"));
        assertTrue(stateLayer.toControlStyleRules().contains(".m3-button:focus-visible .m3-state-layer"));
        assertTrue(stateLayer.toControlStyleRules().contains(".m3-tab:focus-visible .m3-state-layer"));
        assertTrue(stateLayer.toControlStyleRules().contains(".m3-button:pressed .m3-state-layer"));
        assertTrue(stateLayer.toControlStyleRules().contains("-fx-opacity: 0.13"));
        assertTrue(elevation.toControlStyleRules().contains(".m3-elevated-button:hover"));
        assertTrue(elevation.toControlStyleRules().contains(".m3-fab:hover"));
    }

    /// Verifies that component and token set factories preserve explicit token groups.
    @Test
    void createsExplicitComponentTokensAndTokenSet() {
        ColorScheme colorScheme = ColorScheme.fromSeed(Color.web("#6750a4"));
        M3ColorTokens colorTokens = M3ColorTokens.create(colorScheme);
        M3TypographyTokens typographyTokens = M3TypographyTokens.baseline();
        M3ShapeTokens shapeTokens = M3ShapeTokens.create(3.0, 7.0, 11.0, 17.0, 29.0, 999.0);
        M3ElevationTokens elevationTokens = M3ElevationTokens.create(0.0, 2.0, 5.0, 9.0, 13.0, 18.0);
        M3MotionTokens motionTokens = M3MotionTokens.create(80, 220, 460);
        M3StateLayerTokens stateLayerTokens = M3StateLayerTokens.create(0.07, 0.12, 0.15, 0.18, 0.16, 0.44);
        M3ComponentTokens componentTokens = createComponentTokens();

        M3TokenSet tokenSet = M3TokenSet.create(
                M3Profile.BASELINE_2021,
                colorTokens,
                typographyTokens,
                shapeTokens,
                elevationTokens,
                motionTokens,
                stateLayerTokens,
                componentTokens
        );

        assertSame(colorTokens, tokenSet.colorTokens());
        assertSame(typographyTokens, tokenSet.typographyTokens());
        assertSame(shapeTokens, tokenSet.shapeTokens());
        assertSame(componentTokens, tokenSet.componentTokens());
        assertEquals(51.0, tokenSet.componentTokens().filledButton().height(), 0.0001);
        assertEquals(70.0, tokenSet.componentTokens().floatingActionButton().regularSize(), 0.0001);
        assertEquals(61.0, tokenSet.componentTokens().tab().containerHeight(), 0.0001);
        assertEquals(59.0, tokenSet.componentTokens().topAppBar().containerHeight(), 0.0001);
        assertEquals(62.0, tokenSet.componentTokens().bottomAppBar().containerHeight(), 0.0001);
        assertEquals(72.0, tokenSet.componentTokens().navigationRail().containerWidth(), 0.0001);
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-button-filled-container-height: 51px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-tab-container-height: 61px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-top-app-bar-container-height: 59px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-bottom-app-bar-container-height: 62px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-navigation-rail-container-width: 72px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-navigation-drawer-container-width: 78px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-text-area-container-height: 67px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-menu-item-height: 43px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-search-bar-container-height: 44px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-sheet-side-container-width: 46px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-scrim-container-opacity: 0.31"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-motion-duration-medium: 220ms"));
        assertTrue(tokenSet.toControlStyleRules().contains("-m3-container-height: 51px"));
        assertTrue(tokenSet.toControlStyleRules().contains("-m3-item-width: 68px"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-tab-active-indicator"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-top-app-bar-actions"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-text-area"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-menu .m3-menu-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-search-view .m3-list-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-bottom-sheet .m3-bottom-sheet-drag-handle"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-scrim.m3-scrim"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-bottom-app-bar-actions"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-navigation-rail .m3-navigation-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-navigation-drawer .m3-list-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-slider:pressed .m3-state-layer"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-elevated-card:hover .m3-card-container"));
        assertTrue(tokenSet.toControlStyleRules().contains("-fx-opacity: 0.15"));
    }

    /// Creates component tokens with distinctive values for factory tests.
    private static M3ComponentTokens createComponentTokens() {
        return M3ComponentTokens.create(
                new M3ComponentTokens.ButtonTokens(51.0, 21.0, 17.0),
                new M3ComponentTokens.ButtonTokens(52.0, 22.0, 18.0),
                new M3ComponentTokens.ButtonTokens(53.0, 23.0, 19.0),
                new M3ComponentTokens.ButtonTokens(54.0, 24.0, 20.0),
                new M3ComponentTokens.ButtonTokens(55.0, 25.0, 21.0),
                new M3ComponentTokens.ButtonTokens(56.0, 28.0, 0.0),
                new M3ComponentTokens.FabTokens(50.0, 70.0, 110.0, 14.0, 22.0, 30.0, 13.0, 17.0, 25.0),
                new M3ComponentTokens.ButtonTokens(57.0, 26.0, 14.0),
                new M3ComponentTokens.TabTokens(61.0, 91.0, 15.0, 4.0, 4.0),
                new M3ComponentTokens.FieldTokens(66.0, 8.0, 18.0),
                new M3ComponentTokens.TextAreaTokens(67.0, 9.0, 19.0, 20.0),
                new M3ComponentTokens.MenuTokens(7.0, 8.0, 43.0, 6.0, 13.0, 14.0),
                new M3ComponentTokens.SearchTokens(44.0, 22.0, 15.0, 11.0, 21.0, 9.0, 45.0),
                new M3ComponentTokens.SheetTokens(46.0, 23.0, 47.0, 24.0, 25.0, 26.0, 27.0, 5.0),
                new M3ComponentTokens.ScrimTokens(0.31),
                new M3ComponentTokens.SelectionTokens(42.0, 20.0),
                new M3ComponentTokens.SliderTokens(5.0, 18.0, 24.0, 50.0),
                new M3ComponentTokens.ChipTokens(34.0, 10.0, 15.0),
                new M3ComponentTokens.ProgressTokens(5.0, 18.0, 52.0),
                new M3ComponentTokens.CardTokens(13.0, 18.0, 2.0),
                new M3ComponentTokens.DialogTokens(30.0, 26.0),
                new M3ComponentTokens.SnackbarTokens(9.0, 18.0),
                new M3ComponentTokens.DividerTokens(2.0, 8.0, 12.0),
                new M3ComponentTokens.BadgeTokens(7.0, 19.0, 21.0, 10.0, 5.0),
                new M3ComponentTokens.TopAppBarTokens(59.0, 11.0, 13.0, 6.0),
                new M3ComponentTokens.BottomAppBarTokens(62.0, 12.0, 14.0, 7.0),
                new M3ComponentTokens.NavigationBarTokens(67.0, 68.0, 69.0, 30.0, 15.0, 4.0, 9.0),
                new M3ComponentTokens.NavigationRailTokens(72.0, 73.0, 74.0, 75.0, 31.0, 16.0, 5.0, 17.0, 10.0, 11.0),
                new M3ComponentTokens.NavigationDrawerTokens(78.0, 79.0, 80.0, 81.0, 18.0, 12.0, 14.0, 2.0, 16.0, 5.0),
                new M3ComponentTokens.ListItemTokens(58.0, 74.0, 90.0, 6.0, 18.0, 9.0, 15.0)
        );
    }
}
