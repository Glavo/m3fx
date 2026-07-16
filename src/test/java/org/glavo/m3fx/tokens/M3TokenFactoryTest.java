// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.internal.tokens.M3ComponentTokenCssCompiler;
import org.glavo.m3fx.internal.tokens.M3TokenCssCompiler;
import org.glavo.m3fx.internal.theme.M3ThemeCssCompiler;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests explicit token construction and generated token output.
@NotNullByDefault
final class M3TokenFactoryTest {
    /// Verifies that system token groups can be created with explicit values.
    @Test
    void createsExplicitSystemTokens() {
        M3TextStyle displayLarge = M3TextStyle.of("Demo", 60.0, 68.0, 600, -0.2);
        M3TextStyle displayMedium = M3TextStyle.of("Demo", 48.0, 56.0, 600);
        M3TextStyle displaySmall = M3TextStyle.of("Demo", 38.0, 46.0, 600);
        M3TextStyle headlineLarge = M3TextStyle.of("Demo", 34.0, 42.0, 500);
        M3TextStyle headlineMedium = M3TextStyle.of("Demo", 30.0, 38.0, 500);
        M3TextStyle headlineSmall = M3TextStyle.of("Demo", 26.0, 34.0, 500);
        M3TextStyle titleLarge = M3TextStyle.of("Demo", 24.0, 32.0, 500);
        M3TextStyle titleMedium = M3TextStyle.of("Demo", 18.0, 26.0, 500);
        M3TextStyle titleSmall = M3TextStyle.of("Demo", 16.0, 24.0, 500);
        M3TextStyle labelLarge = M3TextStyle.of("Demo", 15.0, 22.0, 600);
        M3TextStyle labelMedium = M3TextStyle.of("Demo", 13.0, 18.0, 600);
        M3TextStyle labelSmall = M3TextStyle.of("Demo", 11.0, 16.0, 600);
        M3TextStyle bodyLarge = M3TextStyle.of("Demo", 17.0, 26.0, 400);
        M3TextStyle bodyMedium = M3TextStyle.of("Demo", 15.0, 23.0, 400);
        M3TextStyle bodySmall = M3TextStyle.of("Demo", 13.0, 18.0, 400);

        M3TypographyTokens typography = M3TypographyTokens.builder(M3TypographyTokens.baseline())
                .displayLarge(displayLarge)
                .displayMedium(displayMedium)
                .displaySmall(displaySmall)
                .headlineLarge(headlineLarge)
                .headlineMedium(headlineMedium)
                .headlineSmall(headlineSmall)
                .titleLarge(titleLarge)
                .titleMedium(titleMedium)
                .titleSmall(titleSmall)
                .labelLarge(labelLarge)
                .labelMedium(labelMedium)
                .labelSmall(labelSmall)
                .bodyLarge(bodyLarge)
                .bodyMedium(bodyMedium)
                .bodySmall(bodySmall)
                .build();
        M3ShapeTokens shape = M3ShapeTokens.builder(M3ShapeTokens.baseline())
                .none(1.0)
                .extraSmall(2.0)
                .small(6.0)
                .medium(10.0)
                .large(18.0)
                .largeIncreased(22.0)
                .extraLarge(30.0)
                .extraLargeIncreased(34.0)
                .extraExtraLarge(48.0)
                .full(999.0)
                .build();
        M3ElevationTokens elevation = M3ElevationTokens.builder()
                .level1(2.0)
                .level2(4.0)
                .level3(8.0)
                .level4(12.0)
                .level5(16.0)
                .build();
        M3MotionTokens motion = M3MotionTokens.builder()
                .shortDurations(90)
                .mediumDurations(210)
                .longDurations(420)
                .extraLongDurations(840)
                .build();
        M3StateLayerTokens stateLayer = M3StateLayerTokens.builder(M3StateLayerTokens.baseline())
                .hoverOpacity(0.05)
                .focusOpacity(0.11)
                .pressedOpacity(0.13)
                .draggedOpacity(0.17)
                .disabledContainerOpacity(0.14)
                .disabledContentOpacity(0.42)
                .focusIndicatorThickness(4.0)
                .focusIndicatorOuterOffset(3.0)
                .focusIndicatorInnerOffset(-3.0)
                .build();

        assertSame(displayLarge, typography.displayLarge());
        assertSame(displayMedium, typography.displayMedium());
        assertSame(bodySmall, typography.bodySmall());
        assertEquals(-0.2, displayLarge.tracking(), 0.0001);
        assertEquals(1.0, shape.none(), 0.0001);
        assertEquals(22.0, shape.largeIncreased(), 0.0001);
        assertEquals(34.0, shape.extraLargeIncreased(), 0.0001);
        assertEquals(48.0, shape.extraExtraLarge(), 0.0001);
        assertEquals(30.0, shape.extraLarge(), 0.0001);
        assertEquals(16.0, elevation.level5(), 0.0001);
        assertEquals(90, motion.short2());
        assertEquals(210, motion.medium1());
        assertEquals(420, motion.long2());
        assertEquals(840, motion.extraLong2());
        assertEquals(M3MotionEasing.STANDARD, motion.defaultEffects().easing());
        assertEquals(Duration.millis(500.0), motion.behavior().tooltipShowDelay());
        assertEquals(0.42, stateLayer.disabledContentOpacity(), 0.0001);
        assertTrue(M3TokenCssCompiler.styleDeclarations(typography).contains("-m3-typescale-display-large-font-family: \"Demo\""));
        assertTrue(M3TokenCssCompiler.styleDeclarations(typography).contains("-m3-typescale-title-small-font-size: 16px"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(typography).contains("-m3-typescale-display-large-tracking: -0.2px"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(typography).contains(".m3-display-large-text"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(typography).contains(".m3-label-small-text"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(typography).contains("-m3-typography-font-size: 60px"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(typography).contains("-m3-typography-line-height: 68px"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(typography).contains("-m3-typography-tracking: -0.2px"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(stateLayer).contains("-m3-state-disabled-content-opacity: 0.42"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(stateLayer).contains("-m3-state-focus-indicator-thickness: 4px"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(stateLayer).contains("-m3-state-focus-indicator-outer-offset: 3px"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(stateLayer).contains("-m3-state-focus-indicator-inner-offset: -3px"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(stateLayer).contains(".m3-button:focus-visible .m3-state-layer"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(stateLayer).contains(".m3-tab:focus-visible .m3-state-layer"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(stateLayer).contains(".m3-icon-toggle-button:focus-visible .m3-state-layer"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(stateLayer).contains(".m3-button:pressed .m3-state-layer"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(stateLayer).contains("-fx-opacity: 0.13"));
        assertFalse(M3TokenCssCompiler.controlStyleRules(stateLayer).contains(":disabled"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(elevation).contains(".m3-elevated-button:hover"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(elevation).contains(".m3-fab:hover"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(elevation).contains(".m3-surface-elevation-level3"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(elevation).contains(".m3-menu, .m3-rich-tooltip-container"));
        assertTrue(M3TokenCssCompiler.controlStyleRules(elevation).contains(".m3-dialog-pane, .m3-snackbar-container"));
    }

    /// Verifies that public token factories reject non-finite values before rendering.
    @Test
    void rejectsNonFiniteTokenValues() {
        assertThrows(IllegalArgumentException.class, () -> M3Density.of(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> M3Density.standard().apply(Double.POSITIVE_INFINITY));
        assertThrows(
                IllegalArgumentException.class,
                () -> M3ElevationTokens.builder().level1(Double.POSITIVE_INFINITY)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> M3ShapeTokens.builder().small(Double.NaN).build()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> M3StateLayerTokens.builder()
                        .hoverOpacity(Double.NaN)
                        .build()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> M3TextStyle.of("Demo", Double.NaN, 16.0, 400)
        );
    }

    /// Verifies that component and token set factories preserve explicit token groups.
    @Test
    void createsExplicitComponentTokensAndTokenSet() {
        ColorScheme colorScheme = ColorScheme.fromSeed(Color.web("#6750a4"));
        M3ColorTokens colorTokens = M3ColorTokens.fromColorScheme(colorScheme);
        M3TypographyTokens typographyTokens = M3TypographyTokens.baseline();
        M3ShapeTokens shapeTokens = M3ShapeTokens.builder(M3ShapeTokens.baseline())
                .extraSmall(3.0)
                .small(7.0)
                .medium(11.0)
                .large(17.0)
                .largeIncreased(17.0)
                .extraLarge(29.0)
                .extraLargeIncreased(29.0)
                .extraExtraLarge(29.0)
                .full(999.0)
                .build();
        M3ElevationTokens elevationTokens = M3ElevationTokens.builder()
                .level1(2.0)
                .level2(5.0)
                .level3(9.0)
                .level4(13.0)
                .level5(18.0)
                .build();
        M3MotionTokens motionTokens = M3MotionTokens.builder()
                .shortDurations(80)
                .mediumDurations(220)
                .longDurations(460)
                .extraLongDurations(920)
                .build();
        M3StateLayerTokens stateLayerTokens = M3StateLayerTokens.builder(M3StateLayerTokens.baseline())
                .hoverOpacity(0.07)
                .focusOpacity(0.12)
                .pressedOpacity(0.15)
                .draggedOpacity(0.18)
                .disabledContainerOpacity(0.16)
                .disabledContentOpacity(0.44)
                .build();
        M3ComponentTokens componentTokens = createComponentTokens();

        M3TokenSet tokenSet = M3TokenSet.builder(
                        M3Profile.BASELINE_2021,
                        colorScheme,
                        M3Density.standard()
                )
                .colorTokens(colorTokens)
                .typographyTokens(typographyTokens)
                .shapeTokens(shapeTokens)
                .elevationTokens(elevationTokens)
                .motionTokens(motionTokens)
                .stateLayerTokens(stateLayerTokens)
                .componentTokens(componentTokens)
                .build();

        assertSame(colorTokens, tokenSet.colorTokens());
        assertSame(typographyTokens, tokenSet.typographyTokens());
        assertSame(shapeTokens, tokenSet.shapeTokens());
        assertSame(componentTokens, tokenSet.componentTokens());
        assertEquals(51.0, tokenSet.componentTokens().filledButton().height(), 0.0001);
        assertEquals(70.0, tokenSet.componentTokens().floatingActionButton().regular().containerSize(), 0.0001);
        assertEquals(33.0, tokenSet.componentTokens().icon().largeSize(), 0.0001);
        M3ComponentTokens.SplitButtonSizeTokens smallSplitButton =
                tokenSet.componentTokens().splitButton().small();
        assertEquals(
                49.0,
                smallSplitButton.menuLeadingSpace()
                        + smallSplitButton.menuIconSize()
                        + smallSplitButton.menuTrailingSpace(),
                0.0001
        );
        assertEquals(15.0, tokenSet.componentTokens().floatingActionButton().menuActionSpacing(), 0.0001);
        assertEquals(32.0, tokenSet.componentTokens().floatingActionButton().menuItem().containerShape(), 0.0001);
        assertEquals(20.0, tokenSet.componentTokens().floatingActionButton().menuCloseButton().iconSize(), 0.0001);
        assertEquals(16.0, tokenSet.componentTokens().floatingActionButton().menuCloseSpacing(), 0.0001);
        assertEquals(10.0, tokenSet.componentTokens().buttonGroup().iconToggleGroupSpacing(), 0.0001);
        assertEquals(61.0, tokenSet.componentTokens().tab().containerHeight(), 0.0001);
        assertEquals(59.0, tokenSet.componentTokens().topAppBar().containerHeight(), 0.0001);
        assertEquals(60.0, tokenSet.componentTokens().topAppBar().mediumContainerHeight(), 0.0001);
        assertEquals(61.0, tokenSet.componentTokens().topAppBar().largeContainerHeight(), 0.0001);
        assertEquals(62.0, tokenSet.componentTokens().topAppBar().mediumFlexibleContainerHeight(), 0.0001);
        assertEquals(63.0, tokenSet.componentTokens().topAppBar().mediumFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(64.0, tokenSet.componentTokens().topAppBar().largeFlexibleContainerHeight(), 0.0001);
        assertEquals(65.0, tokenSet.componentTokens().topAppBar().largeFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(10.0, tokenSet.componentTokens().topAppBar().edgePadding(), 0.0001);
        assertEquals(12.0, tokenSet.componentTokens().topAppBar().mediumBottomPadding(), 0.0001);
        assertEquals(13.0, tokenSet.componentTokens().topAppBar().largeBottomPadding(), 0.0001);
        assertEquals(14.0, tokenSet.componentTokens().topAppBar().flexibleBottomPadding(), 0.0001);
        assertEquals(6.0, tokenSet.componentTokens().topAppBar().actionSpacing(), 0.0001);
        assertEquals(70.0, tokenSet.componentTokens().banner().containerMinHeight(), 0.0001);
        assertEquals(6.0, tokenSet.componentTokens().tooltip().richContainerShape(), 0.0001);
        assertEquals(37.0, tokenSet.componentTokens().pickerField().popupShape(), 0.0001);
        assertEquals(47.0, tokenSet.componentTokens().datePicker().dayCellSize(), 0.0001);
        assertEquals(54.0, tokenSet.componentTokens().timePicker().displayCellHeight(), 0.0001);
        assertEquals(71.0, tokenSet.componentTokens().form().rowMinHeight(), 0.0001);
        assertEquals(14.0, tokenSet.componentTokens().validationSummary().contentPadding(), 0.0001);
        assertEquals(22.0, tokenSet.componentTokens().surface().containerShape(), 0.0001);
        assertEquals(16.0, tokenSet.componentTokens().carousel().trackHorizontalPadding(), 0.0001);
        assertEquals(8.0, tokenSet.componentTokens().carousel().trackVerticalPadding(), 0.0001);
        assertEquals(8.0, tokenSet.componentTokens().carousel().itemSpacing(), 0.0001);
        assertEquals(28.0, tokenSet.componentTokens().carousel().itemShape(), 0.0001);
        assertEquals(40.0, tokenSet.componentTokens().carousel().smallItemMinWidth(), 0.0001);
        assertEquals(56.0, tokenSet.componentTokens().carousel().smallItemMaxWidth(), 0.0001);
        assertEquals(320.0, tokenSet.componentTokens().carousel().largeItemMaxWidth(), 0.0001);
        assertEquals(42.0, tokenSet.componentTokens().selection().touchTargetSize(), 0.0001);
        assertEquals(41.0, tokenSet.componentTokens().selection().stateLayerSize(), 0.0001);
        assertEquals(18.0, tokenSet.componentTokens().selection().checkboxContainerSize(), 0.0001);
        assertEquals(12.0, tokenSet.componentTokens().selection().checkboxSelectedMarkWidth(), 0.0001);
        assertEquals(10.0, tokenSet.componentTokens().selection().checkboxSelectedMarkHeight(), 0.0001);
        assertEquals(13.0, tokenSet.componentTokens().selection().checkboxIndeterminateMarkWidth(), 0.0001);
        assertEquals(3.0, tokenSet.componentTokens().selection().checkboxIndeterminateMarkHeight(), 0.0001);
        assertEquals(20.0, tokenSet.componentTokens().selection().radioContainerSize(), 0.0001);
        assertEquals(11.0, tokenSet.componentTokens().selection().radioSelectedDotSize(), 0.0001);
        assertEquals(48.0, tokenSet.componentTokens().selection().switchTouchTargetSize(), 0.0001);
        assertEquals(52.0, tokenSet.componentTokens().selection().switchTrackWidth(), 0.0001);
        assertEquals(32.0, tokenSet.componentTokens().selection().switchTrackHeight(), 0.0001);
        assertEquals(40.0, tokenSet.componentTokens().selection().switchStateLayerSize(), 0.0001);
        assertEquals(16.0, tokenSet.componentTokens().selection().switchUnselectedHandleSize(), 0.0001);
        assertEquals(25.0, tokenSet.componentTokens().selection().switchWithIconHandleSize(), 0.0001);
        assertEquals(24.0, tokenSet.componentTokens().selection().switchSelectedHandleSize(), 0.0001);
        assertEquals(28.0, tokenSet.componentTokens().selection().switchPressedHandleSize(), 0.0001);
        assertEquals(17.0, tokenSet.componentTokens().selection().switchIconSize(), 0.0001);
        assertEquals(62.0, tokenSet.componentTokens().bottomAppBar().containerHeight(), 0.0001);
        assertEquals(7.0, tokenSet.componentTokens().bottomAppBar().actionSpacing(), 0.0001);
        assertEquals(63.0, tokenSet.componentTokens().toolbar().containerHeight(), 0.0001);
        assertEquals(25.0, tokenSet.componentTokens().toolbar().containerShape(), 0.0001);
        assertEquals(12.0, tokenSet.componentTokens().toolbar().dockedContentPadding(), 0.0001);
        assertEquals(24.0, tokenSet.componentTokens().toolbar().dockedMaxItemSpacing(), 0.0001);
        assertEquals(72.0, tokenSet.componentTokens().navigationRail().collapsedContainerWidth(), 0.0001);
        assertEquals(64.0, tokenSet.componentTokens().navigationRail().narrowCollapsedContainerWidth(), 0.0001);
        assertEquals(220.0, tokenSet.componentTokens().navigationRail().expandedMinimumContainerWidth(), 0.0001);
        assertEquals(280.0, tokenSet.componentTokens().navigationRail().expandedContainerWidth(), 0.0001);
        assertEquals(360.0, tokenSet.componentTokens().navigationRail().expandedMaximumContainerWidth(), 0.0001);
        assertEquals(63.0, tokenSet.componentTokens().loadingIndicator().containerSize(), 0.0001);
        assertEquals(22.0, tokenSet.componentTokens().loadingIndicator().indicatorSize(), 0.0001);
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-state-disabled-container-color: rgba(29,27,32,0.16)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-state-disabled-content-color: rgba(29,27,32,0.44)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-text-field-disabled-container-color: rgba(29,27,32,0.04)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-button-filled-container-height: 51px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-tab-container-height: 61px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-tab-active-indicator-height: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-secondary-tab-active-indicator-height: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-tab-active-indicator-min-width: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-tab-active-indicator-horizontal-inset: 2px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-top-app-bar-container-height: 59px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-top-app-bar-medium-container-height: 60px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-top-app-bar-large-container-height: 61px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-top-app-bar-medium-flexible-container-height: 62px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-top-app-bar-large-flexible-subtitle-container-height: 65px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-top-app-bar-edge-padding: 10px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-top-app-bar-medium-bottom-padding: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-top-app-bar-large-bottom-padding: 13px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-top-app-bar-action-spacing: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-banner-container-min-height: 70px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-tooltip-rich-container-shape: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-picker-field-popup-shape: 37px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-date-picker-day-cell-size: 47px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-time-picker-display-cell-height: 54px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-bottom-app-bar-container-height: 62px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-bottom-app-bar-action-spacing: 7px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-toolbar-container-height: 63px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-toolbar-container-shape: 25px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-toolbar-docked-max-item-spacing: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-navigation-bar-item-spacing: 10px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-navigation-rail-collapsed-container-width: 72px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-navigation-rail-narrow-collapsed-container-width: 64px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-navigation-rail-expanded-container-width: 280px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-navigation-rail-modal-container-shape: 18px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-navigation-drawer-container-width: 78px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-text-area-container-height: 67px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-item-height: 43px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-selected-item-container-shape: 11px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-active-item-container-shape: 17px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-inner-corner-shape: 18px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-first-item-container-shape: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-last-item-container-shape: 13px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-item-spacing: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-container-color: -m3-color-surface-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-selected-item-container-color: -m3-color-secondary-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-menu-vibrant-container-color: -m3-color-tertiary-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-bar-container-height: 44px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-bar-trailing-actions-gap: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-contained-bar-horizontal-padding: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-divided-bar-content-spacing: 17px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-view-horizontal-padding: 10px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-view-bar-results-gap: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-view-results-shape: 18px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-view-docked-bottom-padding: 7px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-search-view-full-screen-divided-header-height: 72px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-selection-touch-target-size: 42px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-selection-state-layer-size: 41px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-checkbox-container-size: 18px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-checkbox-selected-mark-width: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-checkbox-selected-mark-height: 10px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-checkbox-indeterminate-mark-width: 13px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-checkbox-indeterminate-mark-height: 3px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-radio-container-size: 20px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-radio-selected-dot-size: 11px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-touch-target-size: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-track-width: 52px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-track-height: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-state-layer-size: 40px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-unselected-handle-size: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-with-icon-handle-size: 25px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-selected-handle-size: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-pressed-handle-size: 28px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-switch-icon-size: 17px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-slider-thumb-width: 7px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-slider-focused-thumb-width: 3px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-slider-pressed-thumb-width: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-slider-thumb-track-gap: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-slider-stop-indicator-size: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-slider-stop-indicator-trailing-space: 9px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-chip-icon-horizontal-padding: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-chip-element-spacing: 9px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-chip-icon-size: 18px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-chip-avatar-size: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-chip-outline-width: 1px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-progress-indicator-size: 52px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-progress-wave-indicator-size: 54px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-progress-linear-wave-amplitude: 3px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-progress-linear-indeterminate-wavelength: 21px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-progress-circular-wavelength: 16px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-wave-amplitude: 0px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-wave-indicator-size: 54px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-indeterminate-wavelength: 21px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-loading-indicator-container-size: 63px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-loading-indicator-indicator-size: 22px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-list-section-header-height: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-list-section-header-horizontal-padding: 19px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-sheet-side-container-width: 46px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-sheet-side-container-max-width: 83px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-sheet-bottom-container-max-width: 84px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-sheet-header-content-spacing: 13px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-sheet-drag-handle-vertical-padding: 22px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-scrim-container-opacity: 0.31"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-avatar-container-size: 35px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-motion-duration-medium: 220ms"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-motion-duration-short2: 80ms"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-motion-duration-long2: 460ms"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet)
                .contains("-m3-motion-duration-extra-long2: 920ms"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-motion-default-effects-easing: standard"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-motion-default-spatial-duration: 350ms"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-motion-sub-menu-hover-open-delay: 200ms"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-container-height: 51px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-body-large-text"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-item-width: 68px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains("-m3-split-button-menu-width: 49px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-split-button-spacing: -2px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-fx-pref-width: 49px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(tokenSet).contains(
                "-m3-button-group-extra-large-connected-inner-corner: 20px"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(
                ".m3-button-group.m3-button-group-extra-large.m3-connected-button-group"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(
                ".m3-grouped-button.m3-button-group-first:pressed"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-tab-active-indicator"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-medium-container-height: 60px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-large-container-height: 61px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-medium-bottom-padding: 12px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-large-bottom-padding: 13px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-action-spacing: 6px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-text-area"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-menu .m3-menu-container"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-fx-spacing: 16px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-menu .m3-menu-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(":first-menu-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(":last-menu-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(":active"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-menu-inner-corner-shape: 18px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-container-shape: 12px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-container-shape: 13px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-container-shape: 17px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-search-bar .m3-search-bar-trailing"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-fx-spacing: 5px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-search-view.m3-search-view:contained:docked"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-search-view.m3-search-view:divided:full-screen"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-container-shape: 18px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-date-picker-container"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-picker-field-popup"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-form-row-text-column"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-validation-summary-item:hover .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-validation-summary-item:focus-visible .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-validation-summary-item:pressed .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-carousel-track"));
        assertFalse(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-carousel-selected-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-surface"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-side-sheet.m3-side-sheet:detached"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-fx-max-width: 84px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-bottom-sheet .m3-bottom-sheet-drag-handle"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-scrim.m3-scrim"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-wavelength: 41px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-stop-size: 7px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-stop-indicator-size: 6px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-stop-indicator-trailing-space: 9px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-avatar.m3-avatar"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-action-spacing: 7px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-toolbar"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-toolbar-item-slot-size: 49px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-navigation-rail .m3-navigation-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-navigation-drawer .m3-list-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-list-section-header"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-switch"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-chip-icon-size: 18px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-fx-graphic-text-gap: 9px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-fx-border-width: 1px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-state-layer-size: 41px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-container-size: 18px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-selected-dot-size: 11px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-track-width: 52px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-m3-pressed-handle-size: 28px"));
        assertFalse(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(".m3-slider:pressed .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(
                ".m3-elevated-card:actionable:hover .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(
                ".m3-filled-card:actionable:hover .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(
                ".m3-outlined-card:actionable:hover .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains(
                ".m3-outlined-card:actionable:focus-visible .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(tokenSet).contains("-fx-opacity: 0.15"));
    }

    /// Verifies that generated default component token values feed generated control rules.
    @Test
    void componentTokenValuesFeedGeneratedControlRules() {
        M3ComponentTokens componentTokens = createComponentTokensWithUniqueValues();
        List<String> missing = missingComponentTokenControlValues(componentTokens);

        assertTrue(missing.isEmpty(), () -> "Missing component token values in generated control rules: " + missing);
    }

    /// Verifies that generated component custom properties are consumed by production controls or stylesheets.
    @Test
    void componentTokenControlRulesDeclareOnlyConsumedCustomProperties() throws IOException {
        M3ComponentTokens componentTokens = createComponentTokensWithUniqueValues();
        String consumerSources = readTokenConsumerSources();
        List<String> unused = new ArrayList<>();

        for (String declaration : cssDeclarationLines(M3ComponentTokenCssCompiler.controlStyleRules(componentTokens))) {
            String property = cssProperty(declaration);
            if (property.startsWith("-m3-") && !consumerSources.contains(property)) {
                unused.add(declaration);
            }
        }

        assertTrue(unused.isEmpty(), () -> "Unused generated component custom properties: " + unused);
    }

    /// Verifies that explicit motion tokens preserve a supplied semantic scheme.
    @Test
    void createsMotionTokensWithExplicitScheme() {
        M3MotionTokens motion = M3MotionTokens.builder(M3MotionTokens.baseline())
                .short1(10)
                .short2(20)
                .short3(30)
                .short4(40)
                .medium1(50)
                .medium2(60)
                .medium3(70)
                .medium4(80)
                .long1(90)
                .long2(100)
                .long3(110)
                .long4(120)
                .extraLong1(130)
                .extraLong2(140)
                .extraLong3(150)
                .extraLong4(160)
                .scheme(M3MotionScheme.expressive())
                .behavior(M3MotionBehavior.expressive())
                .build();

        assertEquals(20, motion.short2());
        assertEquals(50, motion.medium1());
        assertEquals(100, motion.long2());
        assertEquals(M3MotionEasing.EMPHASIZED, motion.defaultEffects().easing());
        assertEquals(150.0, motion.behavior().subMenuHoverOpenDelay().toMillis(), 0.0001);
        assertEquals(400.0, motion.defaultSpatial().duration().toMillis(), 0.0001);
        assertTrue(M3TokenCssCompiler.styleDeclarations(motion).contains("-m3-motion-default-effects-easing: emphasized"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(motion).contains("-m3-motion-default-spatial-duration: 400ms"));
        assertTrue(M3TokenCssCompiler.styleDeclarations(motion).contains("-m3-motion-sub-menu-hover-open-delay: 150ms"));
    }

    /// Creates component tokens with distinctive values for generated output assertions.
    private static M3ComponentTokens createComponentTokens() {
        return M3ComponentTokens.builder(
                M3Profile.BASELINE_2021,
                M3ShapeTokens.baseline(),
                M3Density.standard()
        )
                .filledButton(new M3ComponentTokens.ButtonTokens(51.0, 21.0, 17.0))
                .tonalButton(new M3ComponentTokens.ButtonTokens(52.0, 22.0, 18.0))
                .outlinedButton(new M3ComponentTokens.ButtonTokens(53.0, 23.0, 19.0))
                .textButton(new M3ComponentTokens.ButtonTokens(54.0, 24.0, 20.0))
                .elevatedButton(new M3ComponentTokens.ButtonTokens(55.0, 25.0, 21.0))
                .buttonSizing(createButtonSizingTokens())
                .iconButton(createIconButtonTokens())
                .floatingActionButton(new M3ComponentTokens.FabTokens(
                                new M3ComponentTokens.FabSizeTokens(50.0, 14.0, 23.0, 13.0, 7.0, 14.0),
                                new M3ComponentTokens.FabSizeTokens(70.0, 22.0, 24.0, 17.0, 9.0, 19.0),
                                new M3ComponentTokens.FabSizeTokens(90.0, 26.0, 28.0, 21.0, 11.0, 23.0),
                                new M3ComponentTokens.FabSizeTokens(110.0, 30.0, 36.0, 25.0, 13.0, 27.0),
                                new M3ComponentTokens.FabSizeTokens(120.0, 32.0, 25.0, 26.0, 8.0, 27.0),
                                new M3ComponentTokens.FabSizeTokens(58.0, 29.0, 20.0, 19.0, 0.0, 19.0),
                                15.0,
                                16.0
                        ))
                .icon(new M3ComponentTokens.IconTokens(19.0, 25.0, 33.0, 41.0))
                .buttonGroup(createButtonGroupTokens())
                .splitButton(createSplitButtonTokens())
                .segmentedButton(new M3ComponentTokens.ButtonTokens(57.0, 26.0, 14.0))
                .tab(new M3ComponentTokens.TabTokens(61.0, 91.0, 15.0, 4.0, 2.0, 4.0, 24.0, 2.0))
                .field(new M3ComponentTokens.FieldTokens(66.0, 8.0, 18.0))
                .textArea(new M3ComponentTokens.TextAreaTokens(67.0, 9.0, 19.0, 20.0))
                .form(new M3ComponentTokens.FormTokens(1.0, 12.0, 11.0, 3.0, 4.0, 190.0, 25.0, 71.0, 5.0))
                .validationSummary(new M3ComponentTokens.ValidationSummaryTokens(10.0, 14.0, 4.0, 7.0, 9.0, 13.0))
                .menu(new M3ComponentTokens.MenuTokens(
                                7.0,
                                8.0,
                                43.0,
                                6.0,
                                11.0,
                                17.0,
                                18.0,
                                12.0,
                                13.0,
                                14.0,
                                15.0,
                                16.0
                        ))
                .search(new M3ComponentTokens.SearchTokens(
                                44.0,
                                22.0,
                                15.0,
                                11.0,
                                4.0,
                                5.0,
                                16.0,
                                17.0,
                                6.0,
                                21.0,
                                10.0,
                                2.0,
                                18.0,
                                7.0,
                                8.0,
                                360.0,
                                720.0,
                                240.0,
                                72.0
                        ))
                .pickerField(new M3ComponentTokens.PickerFieldTokens(46.0, 28.0, 37.0, 18.0, 19.0, 140.0, 7.0, 16.0))
                .datePicker(new M3ComponentTokens.DatePickerTokens(
                                360.0,
                                16.0,
                                28.0,
                                12.0,
                                0.0,
                                64.0,
                                5.0,
                                46.0,
                                23.0,
                                40.0,
                                47.0,
                                24.0,
                                999.0,
                                6.0
                        ))
                .timePicker(new M3ComponentTokens.TimePickerTokens(
                                30.0,
                                18.0,
                                17.0,
                                6.0,
                                19.0,
                                73.0,
                                74.0,
                                54.0,
                                52.0,
                                72.0,
                                216.0,
                                38.0,
                                256.0,
                                48.0,
                                8.0,
                                2.0,
                                96.0,
                                72.0
                        ))
                .sheet(new M3ComponentTokens.SheetTokens(
                                46.0,
                                83.0,
                                23.0,
                                84.0,
                                24.0,
                                25.0,
                                26.0,
                                13.0,
                                22.0,
                                27.0,
                                5.0
                        ))
                .scrim(new M3ComponentTokens.ScrimTokens(0.31))
                .selection(new M3ComponentTokens.SelectionTokens(
                                42.0,
                                41.0,
                                18.0,
                                12.0,
                                10.0,
                                13.0,
                                3.0,
                                20.0,
                                11.0,
                                20.0,
                                48.0,
                                52.0,
                                32.0,
                                40.0,
                                16.0,
                                25.0,
                                24.0,
                                28.0,
                                17.0
                        ))
                .slider(new M3ComponentTokens.SliderTokens(
                                new M3ComponentTokens.SliderSizingTokens(
                                        new M3ComponentTokens.SliderSizeTokens(5.0, 18.0, 24.0, 0.0, 0.0),
                                        new M3ComponentTokens.SliderSizeTokens(6.0, 19.0, 25.0, 0.0, 0.0),
                                        new M3ComponentTokens.SliderSizeTokens(7.0, 20.0, 26.0, 21.0, 9.0),
                                        new M3ComponentTokens.SliderSizeTokens(8.0, 21.0, 27.0, 22.0, 10.0),
                                        new M3ComponentTokens.SliderSizeTokens(9.0, 22.0, 28.0, 23.0, 11.0)
                                ),
                                6.0,
                                9.0,
                                7.0,
                                3.0,
                                2.0,
                                8.0,
                                50.0
                        ))
                .chip(new M3ComponentTokens.ChipTokens(34.0, 10.0, 15.0, 8.0, 9.0, 18.0, 24.0, 12.0, 1.0, 11.0, 13.0))
                .progress(new M3ComponentTokens.ProgressTokens(
                                5.0, 18.0, 52.0, 54.0, 3.0, 41.0, 21.0, 6.0, 7.0, 2.0, 16.0, 5.0
                        ))
                .loadingIndicator(new M3ComponentTokens.LoadingIndicatorTokens(63.0, 22.0))
                .surface(new M3ComponentTokens.SurfaceTokens(22.0, 19.0))
                .carousel(new M3ComponentTokens.CarouselTokens(16.0, 8.0, 8.0, 28.0, 40.0, 56.0, 320.0))
                .card(new M3ComponentTokens.CardTokens(13.0, 18.0, 2.0))
                .dialog(new M3ComponentTokens.DialogTokens(30.0, 26.0, 300.0, 540.0, 12.0, 28.0))
                .snackbar(new M3ComponentTokens.SnackbarTokens(9.0, 18.0, 344.0, 672.0, 48.0, 68.0, 32.0))
                .banner(new M3ComponentTokens.BannerTokens(70.0, 10.0, 20.0, 11.0, 12.0))
                .tooltip(new M3ComponentTokens.TooltipTokens(
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
                        ))
                .divider(new M3ComponentTokens.DividerTokens(2.0, 8.0, 12.0))
                .badge(new M3ComponentTokens.BadgeTokens(7.0, 19.0, 21.0, 10.0, 5.0))
                .avatar(new M3ComponentTokens.AvatarTokens(35.0, 17.0))
                .topAppBar(new M3ComponentTokens.TopAppBarTokens(
                                59.0,
                                60.0,
                                61.0,
                                62.0,
                                63.0,
                                64.0,
                                65.0,
                                10.0,
                                11.0,
                                12.0,
                                13.0,
                                14.0,
                                15.0,
                                6.0
                        ))
                .bottomAppBar(new M3ComponentTokens.BottomAppBarTokens(62.0, 12.0, 14.0, 7.0))
                .toolbar(new M3ComponentTokens.ToolbarTokens(63.0, 64.0, 25.0, 49.0, 8.0, 12.0, 3.0, 24.0))
                .navigationBar(new M3ComponentTokens.NavigationBarTokens(67.0, 68.0, 69.0, 30.0, 15.0, 4.0, 9.0, 10.0))
                .navigationRail(new M3ComponentTokens.NavigationRailTokens(72.0, 64.0, 220.0, 280.0, 360.0, 73.0, 74.0, 75.0, 31.0, 16.0, 5.0, 17.0, 9.0, 10.0, 11.0, 44.0, 20.0, 19.0, 18.0))
                .navigationDrawer(new M3ComponentTokens.NavigationDrawerTokens(
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
                        ))
                .listItem(new M3ComponentTokens.ListItemTokens(58.0, 74.0, 90.0, 6.0, 18.0, 9.0, 15.0, 32.0, 19.0))
                .build();
    }

    /// Creates button size tokens with distinctive values for generated output assertions.
    private static M3ComponentTokens.ButtonSizingTokens createButtonSizingTokens() {
        return new M3ComponentTokens.ButtonSizingTokens(
                new M3ComponentTokens.ButtonSizeTokens(32.0, 20.0, 999.0, 12.0, 8.0, 12.0, 11.0, 8.0, 1.0),
                new M3ComponentTokens.ButtonSizeTokens(40.0, 20.0, 999.0, 12.0, 8.0, 16.0, 15.0, 8.0, 1.0),
                new M3ComponentTokens.ButtonSizeTokens(56.0, 24.0, 999.0, 16.0, 12.0, 24.0, 23.0, 8.0, 1.0),
                new M3ComponentTokens.ButtonSizeTokens(96.0, 32.0, 999.0, 28.0, 16.0, 48.0, 47.0, 12.0, 2.0),
                new M3ComponentTokens.ButtonSizeTokens(136.0, 40.0, 999.0, 28.0, 16.0, 64.0, 63.0, 16.0, 3.0)
        );
    }

    /// Creates button-group tokens with distinctive size values.
    private static M3ComponentTokens.ButtonGroupTokens createButtonGroupTokens() {
        return new M3ComponentTokens.ButtonGroupTokens(
                new M3ComponentTokens.ButtonGroupSizeTokens(32.0, 18.0, 0.15, -2.0, 4.0, 3.0, 16.0),
                new M3ComponentTokens.ButtonGroupSizeTokens(40.0, 11.0, 0.15, -2.0, 6.0, 4.0, 20.0),
                new M3ComponentTokens.ButtonGroupSizeTokens(56.0, 9.0, 0.15, -2.0, 8.0, 5.0, 28.0),
                new M3ComponentTokens.ButtonGroupSizeTokens(96.0, 8.0, 0.15, -2.0, 16.0, 12.0, 48.0),
                new M3ComponentTokens.ButtonGroupSizeTokens(136.0, 7.0, 0.15, -2.0, 20.0, 16.0, 68.0),
                -3.0,
                10.0
        );
    }

    /// Creates split-button tokens with distinctive size values.
    private static M3ComponentTokens.SplitButtonTokens createSplitButtonTokens() {
        M3ComponentTokens.SplitButtonSizeTokens size = new M3ComponentTokens.SplitButtonSizeTokens(
                40.0,
                -2.0,
                6.0,
                7.0,
                8.0,
                24.0,
                22.0,
                29.0,
                3.0,
                10.0,
                10.0,
                20.0
        );
        return new M3ComponentTokens.SplitButtonTokens(size, size, size, size, size);
    }

    /// Creates icon button tokens with distinctive size values.
    private static M3ComponentTokens.IconButtonTokens createIconButtonTokens() {
        return new M3ComponentTokens.IconButtonTokens(
                new M3ComponentTokens.IconButtonSizeTokens(32.0, 20.0, 28.0, 32.0, 40.0, 999.0, 12.0, 8.0, 12.0, 999.0, 1.0),
                new M3ComponentTokens.IconButtonSizeTokens(40.0, 24.0, 32.0, 40.0, 52.0, 999.0, 12.0, 8.0, 12.0, 999.0, 1.0),
                new M3ComponentTokens.IconButtonSizeTokens(56.0, 24.0, 48.0, 56.0, 72.0, 999.0, 16.0, 12.0, 16.0, 999.0, 1.0),
                new M3ComponentTokens.IconButtonSizeTokens(96.0, 32.0, 64.0, 96.0, 128.0, 999.0, 28.0, 16.0, 28.0, 999.0, 2.0),
                new M3ComponentTokens.IconButtonSizeTokens(136.0, 40.0, 104.0, 136.0, 184.0, 999.0, 28.0, 16.0, 28.0, 999.0, 3.0)
        );
    }

    /// Creates component tokens whose record components all have distinctive values.
    private static M3ComponentTokens createComponentTokensWithUniqueValues() {
        UniqueDoubleValues values = new UniqueDoubleValues();
        return M3ComponentTokens.builder(
                M3Profile.BASELINE_2021,
                M3ShapeTokens.baseline(),
                M3Density.standard()
        )
                .filledButton(createRecord(M3ComponentTokens.ButtonTokens.class, values))
                .tonalButton(createRecord(M3ComponentTokens.ButtonTokens.class, values))
                .outlinedButton(createRecord(M3ComponentTokens.ButtonTokens.class, values))
                .textButton(createRecord(M3ComponentTokens.ButtonTokens.class, values))
                .elevatedButton(createRecord(M3ComponentTokens.ButtonTokens.class, values))
                .buttonSizing(createRecord(M3ComponentTokens.ButtonSizingTokens.class, values))
                .iconButton(createRecord(M3ComponentTokens.IconButtonTokens.class, values))
                .floatingActionButton(createRecord(M3ComponentTokens.FabTokens.class, values))
                .icon(createRecord(M3ComponentTokens.IconTokens.class, values))
                .buttonGroup(createRecord(M3ComponentTokens.ButtonGroupTokens.class, values))
                .splitButton(createRecord(M3ComponentTokens.SplitButtonTokens.class, values))
                .segmentedButton(createRecord(M3ComponentTokens.ButtonTokens.class, values))
                .tab(createRecord(M3ComponentTokens.TabTokens.class, values))
                .field(createRecord(M3ComponentTokens.FieldTokens.class, values))
                .textArea(createRecord(M3ComponentTokens.TextAreaTokens.class, values))
                .form(createRecord(M3ComponentTokens.FormTokens.class, values))
                .validationSummary(createRecord(M3ComponentTokens.ValidationSummaryTokens.class, values))
                .menu(createRecord(M3ComponentTokens.MenuTokens.class, values))
                .search(createRecord(M3ComponentTokens.SearchTokens.class, values))
                .pickerField(createRecord(M3ComponentTokens.PickerFieldTokens.class, values))
                .datePicker(createRecord(M3ComponentTokens.DatePickerTokens.class, values))
                .timePicker(createRecord(M3ComponentTokens.TimePickerTokens.class, values))
                .sheet(createRecord(M3ComponentTokens.SheetTokens.class, values))
                .scrim(createRecord(M3ComponentTokens.ScrimTokens.class, values))
                .selection(createRecord(M3ComponentTokens.SelectionTokens.class, values))
                .slider(createRecord(M3ComponentTokens.SliderTokens.class, values))
                .chip(createRecord(M3ComponentTokens.ChipTokens.class, values))
                .progress(createRecord(M3ComponentTokens.ProgressTokens.class, values))
                .loadingIndicator(createRecord(M3ComponentTokens.LoadingIndicatorTokens.class, values))
                .surface(createRecord(M3ComponentTokens.SurfaceTokens.class, values))
                .carousel(createRecord(M3ComponentTokens.CarouselTokens.class, values))
                .card(createRecord(M3ComponentTokens.CardTokens.class, values))
                .dialog(createRecord(M3ComponentTokens.DialogTokens.class, values))
                .snackbar(createRecord(M3ComponentTokens.SnackbarTokens.class, values))
                .banner(createRecord(M3ComponentTokens.BannerTokens.class, values))
                .tooltip(createRecord(M3ComponentTokens.TooltipTokens.class, values))
                .divider(createRecord(M3ComponentTokens.DividerTokens.class, values))
                .badge(createRecord(M3ComponentTokens.BadgeTokens.class, values))
                .avatar(createRecord(M3ComponentTokens.AvatarTokens.class, values))
                .topAppBar(createRecord(M3ComponentTokens.TopAppBarTokens.class, values))
                .bottomAppBar(createRecord(M3ComponentTokens.BottomAppBarTokens.class, values))
                .toolbar(createRecord(M3ComponentTokens.ToolbarTokens.class, values))
                .navigationBar(createRecord(M3ComponentTokens.NavigationBarTokens.class, values))
                .navigationRail(createRecord(M3ComponentTokens.NavigationRailTokens.class, values))
                .navigationDrawer(createRecord(M3ComponentTokens.NavigationDrawerTokens.class, values))
                .listItem(createRecord(M3ComponentTokens.ListItemTokens.class, values))
                .build();
    }

    /// Creates one component token record using unique values for every record component.
    private static <T extends Record> T createRecord(Class<T> type, UniqueDoubleValues values) {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] arguments = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            parameterTypes[i] = components[i].getType();
            if (parameterTypes[i] == double.class) {
                arguments[i] = values.next(components[i].getName());
            } else if (Record.class.isAssignableFrom(parameterTypes[i])) {
                arguments[i] = createRecord(parameterTypes[i].asSubclass(Record.class), values);
            } else {
                throw new AssertionError("Unexpected component in " + type.getName() + ": " + components[i]);
            }
        }

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to create token record " + type.getName(), ex);
        }
    }

    /// Returns default root component token declarations whose values are not present in generated control rules.
    private static List<String> missingComponentTokenControlValues(M3ComponentTokens componentTokens) {
        String controlStyleRules = M3ComponentTokenCssCompiler.controlStyleRules(componentTokens);
        List<String> missing = new ArrayList<>();
        for (String declaration : cssDeclarationLines(M3ComponentTokenCssCompiler.styleDeclarations(componentTokens))) {
            String property = cssProperty(declaration);
            String value = cssValue(declaration);
            boolean optionalProgressConfiguration = property.equals("-m3-progress-linear-wave-amplitude")
                    || property.equals("-m3-progress-circular-wave-amplitude");
            boolean derivedDatePickerGeometry = property.equals("-m3-date-picker-day-state-layer-size");
            if (!optionalProgressConfiguration && !derivedDatePickerGeometry && !controlStyleRules.contains(value)) {
                missing.add(property + ": " + value);
            }
        }
        return missing;
    }

    /// Parses CSS declaration text from generated rule or inline declaration output.
    private static List<String> cssDeclarationLines(String css) {
        List<String> declarations = new ArrayList<>();
        for (String declarationText : css.split(";")) {
            String candidate = declarationText.trim();
            if (candidate.isEmpty()) {
                continue;
            }

            int lineStart = candidate.lastIndexOf('\n') + 1;
            String declaration = candidate.substring(lineStart).trim();
            if (!declaration.isEmpty() && !declaration.equals("}")) {
                declarations.add(declaration);
            }
        }
        return declarations;
    }

    /// Returns the property name from a CSS declaration.
    private static String cssProperty(String declaration) {
        int separatorIndex = declaration.indexOf(':');
        assertTrue(separatorIndex > 0, () -> "Malformed declaration: " + declaration);
        return declaration.substring(0, separatorIndex).trim();
    }

    /// Returns the property value from a CSS declaration.
    private static String cssValue(String declaration) {
        int separatorIndex = declaration.indexOf(':');
        assertTrue(separatorIndex > 0, () -> "Malformed declaration: " + declaration);
        return declaration.substring(separatorIndex + 1).trim();
    }

    /// Reads production Java and stylesheet sources that may consume generated component custom properties.
    private static String readTokenConsumerSources() throws IOException {
        StringBuilder builder = new StringBuilder();
        appendSourceFiles(
                builder,
                Path.of("src", "main", "java", "org", "glavo", "m3fx"),
                Path.of("src", "main", "java", "org", "glavo", "m3fx", "tokens", "M3ComponentTokens.java")
        );
        appendSourceFiles(
                builder,
                Path.of("src", "main", "resources", "org", "glavo", "m3fx", "styles"),
                null
        );
        return builder.toString();
    }

    /// Appends regular source file contents under the root while optionally skipping one file.
    private static void appendSourceFiles(
            StringBuilder builder,
            Path root,
            @org.jetbrains.annotations.Nullable Path excludedFile
    ) throws IOException {
        @org.jetbrains.annotations.Nullable Path excludedAbsolute =
                excludedFile == null ? null : excludedFile.toAbsolutePath().normalize();
        List<Path> sourceFiles;
        try (Stream<Path> files = Files.walk(root)) {
            sourceFiles = files
                    .filter(Files::isRegularFile)
                    .filter(file -> excludedAbsolute == null
                            || !file.toAbsolutePath().normalize().equals(excludedAbsolute))
                    .toList();
        }

        for (Path sourceFile : sourceFiles) {
            builder.append(Files.readString(sourceFile)).append('\n');
        }
    }

    /// Generates stable distinctive double values for component token copy checks.
    private static final class UniqueDoubleValues {
        /// Number of values already produced.
        private int count;

        /// Returns the next distinctive value for the named component.
        private double next(String componentName) {
            count++;
            if (componentName.toLowerCase(Locale.ROOT).contains("opacity")) {
                return 0.001 * count;
            }
            return 1000.137 + count;
        }
    }
}
