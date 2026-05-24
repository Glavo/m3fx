// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
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
        M3TextStyle displayMedium = M3TextStyle.create("Demo", 48.0, 56.0, 600);
        M3TextStyle displaySmall = M3TextStyle.create("Demo", 38.0, 46.0, 600);
        M3TextStyle headlineLarge = M3TextStyle.create("Demo", 34.0, 42.0, 500);
        M3TextStyle headlineMedium = M3TextStyle.create("Demo", 30.0, 38.0, 500);
        M3TextStyle headlineSmall = M3TextStyle.create("Demo", 26.0, 34.0, 500);
        M3TextStyle titleLarge = M3TextStyle.create("Demo", 24.0, 32.0, 500);
        M3TextStyle titleMedium = M3TextStyle.create("Demo", 18.0, 26.0, 500);
        M3TextStyle titleSmall = M3TextStyle.create("Demo", 16.0, 24.0, 500);
        M3TextStyle labelLarge = M3TextStyle.create("Demo", 15.0, 22.0, 600);
        M3TextStyle labelMedium = M3TextStyle.create("Demo", 13.0, 18.0, 600);
        M3TextStyle labelSmall = M3TextStyle.create("Demo", 11.0, 16.0, 600);
        M3TextStyle bodyLarge = M3TextStyle.create("Demo", 17.0, 26.0, 400);
        M3TextStyle bodyMedium = M3TextStyle.create("Demo", 15.0, 23.0, 400);
        M3TextStyle bodySmall = M3TextStyle.create("Demo", 13.0, 18.0, 400);

        M3TypographyTokens typography = M3TypographyTokens.create(
                displayLarge,
                displayMedium,
                displaySmall,
                headlineLarge,
                headlineMedium,
                headlineSmall,
                titleLarge,
                titleMedium,
                titleSmall,
                labelLarge,
                labelMedium,
                labelSmall,
                bodyLarge,
                bodyMedium,
                bodySmall
        );
        M3ShapeTokens shape = M3ShapeTokens.create(2.0, 6.0, 10.0, 18.0, 30.0, 999.0);
        M3ElevationTokens elevation = M3ElevationTokens.create(0.0, 2.0, 4.0, 8.0, 12.0, 16.0);
        M3MotionTokens motion = M3MotionTokens.create(90, 210, 420);
        M3StateLayerTokens stateLayer = M3StateLayerTokens.create(0.05, 0.11, 0.13, 0.17, 0.14, 0.42);

        assertSame(displayLarge, typography.displayLarge());
        assertSame(displayMedium, typography.displayMedium());
        assertSame(bodySmall, typography.bodySmall());
        assertEquals(30.0, shape.extraLarge(), 0.0001);
        assertEquals(16.0, elevation.level5(), 0.0001);
        assertEquals(90, motion.short2());
        assertEquals(210, motion.mediumDuration());
        assertEquals(420, motion.long2());
        assertEquals(M3MotionEasing.STANDARD, motion.defaultEffects().easing());
        assertEquals(Duration.millis(500.0), motion.behavior().tooltipShowDelay());
        assertEquals(0.42, stateLayer.disabledContentOpacity(), 0.0001);
        assertTrue(typography.toStyleDeclarations().contains("-m3-typescale-display-large-font-family: \"Demo\""));
        assertTrue(typography.toStyleDeclarations().contains("-m3-typescale-title-small-font-size: 16px"));
        assertTrue(typography.toControlStyleRules().contains(".m3-display-large-text"));
        assertTrue(typography.toControlStyleRules().contains(".m3-label-small-text"));
        assertTrue(typography.toControlStyleRules().contains("-m3-typography-font-size: 60px"));
        assertTrue(typography.toControlStyleRules().contains("-m3-typography-line-height: 68px"));
        assertTrue(stateLayer.toStyleDeclarations().contains("-m3-state-disabled-content-opacity: 0.42"));
        assertTrue(stateLayer.toControlStyleRules().contains(".m3-button:focus-visible .m3-state-layer"));
        assertTrue(stateLayer.toControlStyleRules().contains(".m3-tab:focus-visible .m3-state-layer"));
        assertTrue(stateLayer.toControlStyleRules().contains(".m3-icon-toggle-button:focus-visible .m3-state-layer"));
        assertTrue(stateLayer.toControlStyleRules().contains(".m3-button:pressed .m3-state-layer"));
        assertTrue(stateLayer.toControlStyleRules().contains("-fx-opacity: 0.13"));
        assertTrue(elevation.toControlStyleRules().contains(".m3-elevated-button:hover"));
        assertTrue(elevation.toControlStyleRules().contains(".m3-fab:hover"));
        assertTrue(elevation.toControlStyleRules().contains(".m3-surface-elevation-level3"));
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
        assertEquals(60.0, tokenSet.componentTokens().topAppBar().mediumContainerHeight(), 0.0001);
        assertEquals(61.0, tokenSet.componentTokens().topAppBar().largeContainerHeight(), 0.0001);
        assertEquals(12.0, tokenSet.componentTokens().topAppBar().mediumBottomPadding(), 0.0001);
        assertEquals(13.0, tokenSet.componentTokens().topAppBar().largeBottomPadding(), 0.0001);
        assertEquals(70.0, tokenSet.componentTokens().banner().containerMinHeight(), 0.0001);
        assertEquals(6.0, tokenSet.componentTokens().tooltip().richContainerShape(), 0.0001);
        assertEquals(37.0, tokenSet.componentTokens().pickerField().popupShape(), 0.0001);
        assertEquals(47.0, tokenSet.componentTokens().datePicker().dayCellSize(), 0.0001);
        assertEquals(54.0, tokenSet.componentTokens().timePicker().displayCellHeight(), 0.0001);
        assertEquals(71.0, tokenSet.componentTokens().form().rowMinHeight(), 0.0001);
        assertEquals(14.0, tokenSet.componentTokens().validationSummary().contentPadding(), 0.0001);
        assertEquals(22.0, tokenSet.componentTokens().surface().containerShape(), 0.0001);
        assertEquals(0.91, tokenSet.componentTokens().carousel().itemOpacity(), 0.0001);
        assertEquals(62.0, tokenSet.componentTokens().bottomAppBar().containerHeight(), 0.0001);
        assertEquals(72.0, tokenSet.componentTokens().navigationRail().containerWidth(), 0.0001);
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-button-filled-container-height: 51px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-tab-container-height: 61px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-top-app-bar-container-height: 59px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-top-app-bar-medium-container-height: 60px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-top-app-bar-large-container-height: 61px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-top-app-bar-medium-bottom-padding: 12px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-top-app-bar-large-bottom-padding: 13px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-banner-container-min-height: 70px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-tooltip-rich-container-shape: 6px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-picker-field-popup-shape: 37px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-date-picker-day-cell-size: 47px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-time-picker-display-cell-height: 54px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-bottom-app-bar-container-height: 62px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-navigation-rail-container-width: 72px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-navigation-drawer-container-width: 78px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-text-area-container-height: 67px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-menu-item-height: 43px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-search-bar-container-height: 44px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-progress-linear-wave-amplitude: 3px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-progress-circular-wavelength: 16px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-list-section-header-height: 32px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-list-section-header-horizontal-padding: 19px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-sheet-side-container-width: 46px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-scrim-container-opacity: 0.31"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-avatar-container-size: 35px"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-motion-duration-medium: 220ms"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-motion-duration-short2: 80ms"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-motion-duration-long2: 460ms"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-motion-default-effects-easing: standard"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-motion-default-spatial-duration: 350ms"));
        assertTrue(tokenSet.toRootStyleDeclarations().contains("-m3-motion-sub-menu-hover-open-delay: 200ms"));
        assertTrue(tokenSet.toControlStyleRules().contains("-m3-container-height: 51px"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-body-large-text"));
        assertTrue(tokenSet.toControlStyleRules().contains("-m3-item-width: 68px"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-tab-active-indicator"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-top-app-bar-actions"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-text-area"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-menu .m3-menu-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-search-view .m3-list-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-date-picker-container"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-time-picker-container"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-picker-field-popup"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-form-row-text-column"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-validation-summary-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-carousel-selected-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-surface"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-bottom-sheet .m3-bottom-sheet-drag-handle"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-scrim.m3-scrim"));
        assertTrue(tokenSet.toControlStyleRules().contains("-m3-wavelength: 41px"));
        assertTrue(tokenSet.toControlStyleRules().contains("-m3-stop-size: 7px"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-avatar.m3-avatar"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-bottom-app-bar-actions"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-navigation-rail .m3-navigation-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-navigation-drawer .m3-list-item"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-list-section-header"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-slider:pressed .m3-state-layer"));
        assertTrue(tokenSet.toControlStyleRules().contains(".m3-elevated-card:hover .m3-card-container"));
        assertTrue(tokenSet.toControlStyleRules().contains("-fx-opacity: 0.15"));
    }

    /// Verifies that explicit motion tokens preserve a supplied semantic scheme.
    @Test
    void createsMotionTokensWithExplicitScheme() {
        M3MotionTokens motion = M3MotionTokens.create(
                10,
                20,
                30,
                40,
                50,
                60,
                70,
                80,
                90,
                100,
                110,
                120,
                130,
                140,
                150,
                160,
                M3MotionScheme.expressive(),
                M3MotionBehavior.expressive()
        );

        assertEquals(20, motion.shortDuration());
        assertEquals(50, motion.mediumDuration());
        assertEquals(100, motion.longDuration());
        assertEquals(M3MotionEasing.EMPHASIZED, motion.defaultEffects().easing());
        assertEquals(150.0, motion.behavior().subMenuHoverOpenDelay().toMillis(), 0.0001);
        assertEquals(400.0, motion.defaultSpatial().duration().toMillis(), 0.0001);
        assertTrue(motion.toStyleDeclarations().contains("-m3-motion-default-effects-easing: emphasized"));
        assertTrue(motion.toStyleDeclarations().contains("-m3-motion-default-spatial-duration: 400ms"));
        assertTrue(motion.toStyleDeclarations().contains("-m3-motion-sub-menu-hover-open-delay: 150ms"));
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
                new M3ComponentTokens.FormTokens(1.0, 12.0, 11.0, 3.0, 4.0, 190.0, 25.0, 71.0, 5.0),
                new M3ComponentTokens.ValidationSummaryTokens(10.0, 14.0, 4.0, 7.0, 9.0, 13.0),
                new M3ComponentTokens.MenuTokens(7.0, 8.0, 43.0, 6.0, 13.0, 14.0),
                new M3ComponentTokens.SearchTokens(44.0, 22.0, 15.0, 11.0, 21.0, 9.0, 45.0),
                new M3ComponentTokens.PickerFieldTokens(46.0, 28.0, 37.0, 18.0, 19.0, 140.0, 7.0, 16.0),
                new M3ComponentTokens.DatePickerTokens(29.0, 17.0, 13.0, 5.0, 46.0, 23.0, 47.0, 24.0, 6.0),
                new M3ComponentTokens.TimePickerTokens(
                        30.0,
                        18.0,
                        17.0,
                        6.0,
                        19.0,
                        73.0,
                        54.0,
                        9.0,
                        7.0,
                        45.0,
                        42.0,
                        94.0,
                        21.0
                ),
                new M3ComponentTokens.SheetTokens(46.0, 23.0, 47.0, 24.0, 25.0, 26.0, 27.0, 5.0),
                new M3ComponentTokens.ScrimTokens(0.31),
                new M3ComponentTokens.SelectionTokens(42.0, 20.0),
                new M3ComponentTokens.SliderTokens(5.0, 18.0, 24.0, 50.0),
                new M3ComponentTokens.ChipTokens(34.0, 10.0, 15.0),
                new M3ComponentTokens.ProgressTokens(5.0, 18.0, 52.0, 3.0, 41.0, 6.0, 7.0, 2.0, 16.0, 5.0),
                new M3ComponentTokens.SurfaceTokens(22.0, 19.0),
                new M3ComponentTokens.CarouselTokens(5.0, 13.0, 0.91, 11.0, 0.13, 4.0),
                new M3ComponentTokens.CardTokens(13.0, 18.0, 2.0),
                new M3ComponentTokens.DialogTokens(30.0, 26.0),
                new M3ComponentTokens.SnackbarTokens(9.0, 18.0),
                new M3ComponentTokens.BannerTokens(70.0, 10.0, 20.0, 11.0, 12.0),
                new M3ComponentTokens.TooltipTokens(
                        3.0,
                        4.0,
                        5.0,
                        6.0,
                        7.0,
                        8.0,
                        9.0,
                        10.0,
                        320.0,
                        11.0,
                        12.0,
                        13.0
                ),
                new M3ComponentTokens.DividerTokens(2.0, 8.0, 12.0),
                new M3ComponentTokens.BadgeTokens(7.0, 19.0, 21.0, 10.0, 5.0),
                new M3ComponentTokens.AvatarTokens(35.0, 17.0),
                new M3ComponentTokens.TopAppBarTokens(59.0, 60.0, 61.0, 11.0, 12.0, 13.0, 14.0, 6.0),
                new M3ComponentTokens.BottomAppBarTokens(62.0, 12.0, 14.0, 7.0),
                new M3ComponentTokens.NavigationBarTokens(67.0, 68.0, 69.0, 30.0, 15.0, 4.0, 9.0),
                new M3ComponentTokens.NavigationRailTokens(72.0, 73.0, 74.0, 75.0, 31.0, 16.0, 5.0, 17.0, 10.0, 11.0),
                new M3ComponentTokens.NavigationDrawerTokens(
                        78.0,
                        79.0,
                        80.0,
                        81.0,
                        18.0,
                        12.0,
                        14.0,
                        2.0,
                        16.0,
                        5.0,
                        44.0,
                        20.0,
                        24.0
                ),
                new M3ComponentTokens.ListItemTokens(58.0, 74.0, 90.0, 6.0, 18.0, 9.0, 15.0, 32.0, 19.0)
        );
    }
}
