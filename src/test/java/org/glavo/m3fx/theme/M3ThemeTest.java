// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.theme;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.m3fx.tokens.M3TokenSet;
import org.glavo.monetfx.Brightness;
import org.glavo.monetfx.ColorScheme;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for m3fx theme creation and installation.
@NotNullByDefault
final class M3ThemeTest {
    /// Starts the JavaFX toolkit before tests create scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    /// Verifies that the baseline profile creates a complete token set.
    @Test
    void createsBaselineTokenSet() {
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));

        assertEquals(M3Profile.BASELINE_2021, theme.profile());
        assertSame(theme.colorScheme(), theme.tokens().colorTokens().colorScheme());
        assertEquals(M3MotionEasing.STANDARD, theme.tokens().motionTokens().defaultEffects().easing());
        assertTrue(theme.toRootStyleDeclarations().contains("-monet-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-color-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-motion-default-effects-easing: standard"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-typescale-label-large-font-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-typescale-display-medium-font-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-typescale-body-small-line-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-button-filled-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-fab-regular-container-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-segmented-button-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-tab-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-slider-track-thickness"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-chip-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-progress-indicator-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-card-content-padding"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-divider-thickness"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-badge-small-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-bottom-app-bar-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-rail-container-width"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-drawer-container-width"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-item-one-line-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-section-header-height"));
        assertTrue(theme.toControlStyleRules().contains(".m3-filled-button"));
        assertTrue(theme.toControlStyleRules().contains(".m3-display-medium-text"));
        assertTrue(theme.toControlStyleRules().contains(".m3-body-small-text"));
        assertTrue(theme.toControlStyleRules().contains(".m3-regular-fab"));
        assertTrue(theme.toControlStyleRules().contains(".m3-segmented-button"));
        assertTrue(theme.toControlStyleRules().contains(".m3-segmented-button-first"));
        assertTrue(theme.toControlStyleRules().contains(".m3-tab-active-indicator"));
        assertTrue(theme.toControlStyleRules().contains(".m3-checkbox:hover"));
        assertTrue(theme.toControlStyleRules().contains(".m3-slider:pressed"));
        assertTrue(theme.toControlStyleRules().contains(".m3-list-item:disabled"));
        assertTrue(theme.toControlStyleRules().contains(".m3-slider:focus-visible .m3-state-layer"));
        assertTrue(theme.toControlStyleRules().contains(".m3-card:focus-visible .m3-state-layer"));
        assertTrue(theme.toControlStyleRules().contains(".m3-dialog-pane"));
        assertTrue(theme.toControlStyleRules().contains(".m3-badge"));
        assertTrue(theme.toControlStyleRules().contains(".m3-top-app-bar"));
        assertTrue(theme.toControlStyleRules().contains(".m3-bottom-app-bar"));
        assertTrue(theme.toControlStyleRules().contains(".m3-navigation-rail"));
        assertTrue(theme.toControlStyleRules().contains(".m3-navigation-drawer"));
        assertTrue(theme.toControlStyleRules().contains(".m3-list-item"));
        assertTrue(theme.toControlStyleRules().contains(".m3-list-section-header"));
        assertTrue(theme.toControlStyleRules().contains("-fx-opacity: 0.08"));
        assertTrue(theme.toControlStyleRules().contains("-fx-opacity: 0.1"));
        assertTrue(theme.toControlStyleRules().contains("-fx-opacity: 0.38"));
        assertTrue(theme.toControlStyleRules().contains(".m3-elevated-card .m3-card-container"));
        assertTrue(theme.toControlStyleRules().contains(".m3-elevated-card:hover .m3-card-container"));
        assertTrue(theme.toControlStyleRules().contains(".m3-fab:hover"));
        assertTrue(theme.toControlStyleRules().contains(".m3-surface-elevation-level5"));
        assertNotNull(theme.tokens().componentTokens().filledButton());
        assertNotNull(theme.tokens().componentTokens().floatingActionButton());
        assertNotNull(theme.tokens().componentTokens().segmentedButton());
        assertNotNull(theme.tokens().componentTokens().tab());
        assertNotNull(theme.tokens().componentTokens().slider());
        assertNotNull(theme.tokens().componentTokens().chip());
        assertNotNull(theme.tokens().componentTokens().divider());
        assertNotNull(theme.tokens().componentTokens().badge());
        assertNotNull(theme.tokens().componentTokens().topAppBar());
        assertNotNull(theme.tokens().componentTokens().bottomAppBar());
        assertNotNull(theme.tokens().componentTokens().navigationBar());
        assertNotNull(theme.tokens().componentTokens().navigationRail());
        assertNotNull(theme.tokens().componentTokens().navigationDrawer());
        assertNotNull(theme.tokens().componentTokens().listItem());
    }

    /// Verifies that the expressive profile creates a complete token set.
    @Test
    void createsExpressiveTokenSet() {
        M3Theme theme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK,
                M3Density.standard()
        );

        assertEquals(M3Profile.EXPRESSIVE_2025, theme.profile());
        assertSame(theme.colorScheme(), theme.tokens().colorTokens().colorScheme());
        assertEquals(M3MotionEasing.EMPHASIZED, theme.tokens().motionTokens().defaultEffects().easing());
        assertEquals(400.0, theme.tokens().motionTokens().defaultSpatial().duration().toMillis(), 0.0001);
        assertEquals(150.0, theme.tokens().motionTokens().behavior().subMenuHoverOpenDelay().toMillis(), 0.0001);
        assertTrue(theme.toRootStyleDeclarations().contains("-monet-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-color-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-motion-default-effects-easing: emphasized"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-motion-default-spatial-duration: 400ms"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-motion-sub-menu-hover-open-delay: 150ms"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-button-filled-container-height: 48px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-button-filled-horizontal-padding: 28px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-button-text-horizontal-padding: 16px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-fab-regular-container-size: 64px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-fab-regular-horizontal-padding: 18px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-segmented-button-container-height: 48px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-segmented-button-horizontal-padding: 16px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-tab-container-height: 56px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-tab-horizontal-padding: 20px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-tab-active-indicator-height: 4px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-field-horizontal-padding: 20px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-text-area-vertical-padding: 20px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-selection-touch-target-size: 48px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-slider-track-thickness: 6px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-slider-thumb-size: 24px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-slider-touch-target-size: 56px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-chip-container-height: 36px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-chip-horizontal-padding: 18px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-menu-container-padding: 10px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-menu-item-container-shape: 10px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-search-bar-horizontal-padding: 20px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-search-view-result-padding: 12px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-sheet-content-padding: 28px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-sheet-drag-handle-width: 36px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-card-container-shape: 24px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-card-content-padding: 20px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-dialog-container-shape: 32px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-dialog-content-padding: 28px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-snackbar-container-shape: 16px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-snackbar-content-padding: 18px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-badge-small-size: 8px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-container-height: 72px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-medium-container-height: 120px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-large-container-height: 160px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-horizontal-padding: 24px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-medium-bottom-padding: 24px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-large-bottom-padding: 32px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-top-app-bar-action-spacing: 12px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-bottom-app-bar-container-height: 88px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-bottom-app-bar-horizontal-padding: 24px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-bottom-app-bar-action-spacing: 12px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-rail-container-width: 112px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-drawer-container-width: 384px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-bar-horizontal-padding: 12px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-rail-item-spacing: 12px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-drawer-container-padding: 16px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-navigation-drawer-group-child-item-horizontal-padding: 40px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-item-one-line-height: 64px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-item-horizontal-padding: 20px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-item-content-spacing: 20px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-section-header-height: 56px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-section-header-horizontal-padding: 20px"));
        assertTrue(theme.toControlStyleRules().contains("-m3-container-height: 48px"));
        assertTrue(theme.toControlStyleRules().contains("-m3-content-spacing: 6px"));
        assertTrue(theme.toControlStyleRules().contains("-m3-horizontal-padding: 40px"));
        assertTrue(theme.toControlStyleRules().contains("-fx-padding: 10px"));
        assertTrue(theme.toControlStyleRules().contains("-fx-padding: 0 20px"));
        assertTrue(theme.toControlStyleRules().contains("-fx-padding: 28px"));
        assertTrue(theme.toControlStyleRules().contains("-fx-background-radius: 999px"));
        assertNotNull(theme.tokens().componentTokens().filledButton());
        assertNotNull(theme.tokens().componentTokens().floatingActionButton());
        assertNotNull(theme.tokens().componentTokens().segmentedButton());
        assertNotNull(theme.tokens().componentTokens().tab());
        assertNotNull(theme.tokens().componentTokens().slider());
        assertNotNull(theme.tokens().componentTokens().chip());
        assertNotNull(theme.tokens().componentTokens().divider());
        assertNotNull(theme.tokens().componentTokens().badge());
        assertNotNull(theme.tokens().componentTokens().topAppBar());
        assertNotNull(theme.tokens().componentTokens().bottomAppBar());
        assertNotNull(theme.tokens().componentTokens().navigationBar());
        assertNotNull(theme.tokens().componentTokens().navigationRail());
        assertNotNull(theme.tokens().componentTokens().navigationDrawer());
        assertNotNull(theme.tokens().componentTokens().listItem());
        assertEquals(28.0, theme.tokens().componentTokens().filledButton().horizontalPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().textButton().horizontalPadding(), 0.0001);
        assertEquals(18.0, theme.tokens().componentTokens().floatingActionButton().regularHorizontalPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().segmentedButton().horizontalPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().tab().horizontalPadding(), 0.0001);
        assertEquals(4.0, theme.tokens().componentTokens().tab().activeIndicatorHeight(), 0.0001);
        assertEquals(18.0, theme.tokens().componentTokens().chip().horizontalPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().field().horizontalPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().textArea().verticalPadding(), 0.0001);
        assertEquals(48.0, theme.tokens().componentTokens().selection().touchTargetSize(), 0.0001);
        assertEquals(6.0, theme.tokens().componentTokens().slider().trackThickness(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().slider().thumbSize(), 0.0001);
        assertEquals(56.0, theme.tokens().componentTokens().slider().touchTargetSize(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().navigationBar().horizontalPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().navigationRail().verticalPadding(), 0.0001);
        assertEquals(40.0, theme.tokens().componentTokens().navigationDrawer().groupChildItemHorizontalPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().listItem().horizontalPadding(), 0.0001);
        assertEquals(10.0, theme.tokens().componentTokens().menu().containerPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().menu().itemContentSpacing(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().search().barHorizontalPadding(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().search().viewResultPadding(), 0.0001);
        assertEquals(28.0, theme.tokens().componentTokens().sheet().contentPadding(), 0.0001);
        assertEquals(36.0, theme.tokens().componentTokens().sheet().dragHandleWidth(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().card().containerShape(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().card().contentPadding(), 0.0001);
        assertEquals(28.0, theme.tokens().componentTokens().dialog().contentPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().snackbar().containerShape(), 0.0001);
        assertEquals(18.0, theme.tokens().componentTokens().snackbar().contentPadding(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().topAppBar().horizontalPadding(), 0.0001);
        assertEquals(120.0, theme.tokens().componentTokens().topAppBar().mediumContainerHeight(), 0.0001);
        assertEquals(160.0, theme.tokens().componentTokens().topAppBar().largeContainerHeight(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().topAppBar().mediumBottomPadding(), 0.0001);
        assertEquals(32.0, theme.tokens().componentTokens().topAppBar().largeBottomPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().topAppBar().contentSpacing(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().bottomAppBar().actionSpacing(), 0.0001);
    }

    /// Verifies that convenience theme factories use the expected defaults.
    @Test
    void createsThemesFromConvenienceFactories() {
        Color seedColor = Color.web("#386a20");
        ColorScheme colorScheme = ColorScheme.fromSeed(seedColor);

        M3Theme darkBaselineTheme = M3Theme.fromSeed(seedColor, Brightness.DARK);
        M3Theme expressiveTheme = M3Theme.fromSeed(seedColor, M3Profile.EXPRESSIVE_2025, Brightness.LIGHT);
        M3Theme colorSchemeTheme = M3Theme.fromColorScheme(colorScheme);
        M3Theme expressiveColorSchemeTheme = M3Theme.fromColorScheme(M3Profile.EXPRESSIVE_2025, colorScheme);

        assertEquals(M3Profile.BASELINE_2021, darkBaselineTheme.profile());
        assertEquals(Brightness.DARK, darkBaselineTheme.brightness());
        assertEquals(0.0, darkBaselineTheme.density().scale(), 0.0001);
        assertEquals(M3Profile.EXPRESSIVE_2025, expressiveTheme.profile());
        assertEquals(Brightness.LIGHT, expressiveTheme.brightness());
        assertEquals(0.0, expressiveTheme.density().scale(), 0.0001);
        assertEquals(M3Profile.BASELINE_2021, colorSchemeTheme.profile());
        assertEquals(Brightness.LIGHT, colorSchemeTheme.brightness());
        assertSame(colorScheme, colorSchemeTheme.colorScheme());
        assertEquals(M3Profile.EXPRESSIVE_2025, expressiveColorSchemeTheme.profile());
        assertEquals(Brightness.LIGHT, expressiveColorSchemeTheme.brightness());
        assertSame(colorScheme, expressiveColorSchemeTheme.colorScheme());
    }

    /// Verifies that a theme can reuse an explicit token set.
    @Test
    void createsThemeFromExplicitTokenSet() {
        ColorScheme colorScheme = ColorScheme.fromSeed(Color.web("#6750a4"));
        M3Density density = M3Density.of(2.0);
        M3TokenSet tokenSet = M3TokenSet.create(M3Profile.BASELINE_2021, colorScheme, density);

        M3Theme theme = M3Theme.fromTokenSet(M3Profile.BASELINE_2021, colorScheme, density, tokenSet);

        assertSame(tokenSet, theme.tokens());
        assertSame(colorScheme, theme.colorScheme());
        assertSame(density, theme.density());
        assertEquals(48.0, theme.tokens().componentTokens().filledButton().height(), 0.0001);
    }

    /// Verifies that installing a theme on a scene is idempotent.
    @Test
    void installsThemeOnSceneOnce() {
        Pane root = new Pane();
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        M3ThemeManager.install(scene, theme);
        M3ThemeManager.install(scene, theme);

        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertSame(theme, root.getProperties().get(M3ThemeManager.THEME_PROPERTY_KEY));
        assertTrue(root.getStyle().contains("-m3-color-primary"));
        assertEquals(2, scene.getStylesheets().size());
        assertEquals(M3ThemeManager.stylesheetUrl(), scene.getStylesheets().get(0));
        assertTrue(M3ThemeManager.stylesheetUrl().endsWith("/styles/base.css"));
    }

    /// Verifies that root profile and brightness style classes track theme reinstallations.
    @Test
    void updatesRootThemeStyleClassesOnReinstall() {
        Pane root = new Pane();
        Scene scene = new Scene(root);
        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3Theme expressiveDarkTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK
        );

        M3ThemeManager.install(scene, baselineTheme);

        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

        M3ThemeManager.install(scene, expressiveDarkTheme);

        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));

        M3ThemeManager.uninstall(scene);

        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
    }

    /// Verifies that detached popup roots can inherit the installed theme context.
    @Test
    void copiesThemeContextToDetachedRoots() {
        Pane root = new Pane();
        Pane popupRoot = new Pane();
        popupRoot.getStyleClass().add("popup-root");
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK
        );

        M3ThemeManager.install(scene, theme);
        M3ThemeManager.copyThemeContext(root, popupRoot);

        assertTrue(popupRoot.getStyleClass().contains("popup-root"));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertEquals(root.getStyle(), popupRoot.getStyle());
        assertSame(theme, popupRoot.getProperties().get(M3ThemeManager.THEME_PROPERTY_KEY));

        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, baselineTheme);
        M3ThemeManager.copyThemeContext(root, popupRoot);

        assertTrue(popupRoot.getStyleClass().contains("popup-root"));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertSame(baselineTheme, popupRoot.getProperties().get(M3ThemeManager.THEME_PROPERTY_KEY));
    }

    /// Verifies that installed themes can be queried from scenes and roots.
    @Test
    void returnsInstalledTheme() {
        Pane root = new Pane();
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        assertNull(M3ThemeManager.getTheme(root));
        assertNull(M3ThemeManager.getTheme(scene));

        M3ThemeManager.install(scene, theme);

        assertSame(theme, M3ThemeManager.getTheme(root));
        assertSame(theme, M3ThemeManager.getTheme(scene));

        M3ThemeManager.uninstall(scene);

        assertNull(M3ThemeManager.getTheme(root));
        assertNull(M3ThemeManager.getTheme(scene));
    }

    /// Verifies that generated theme stylesheets can be addressed directly.
    @Test
    void exposesGeneratedThemeStylesheetUrl() throws Exception {
        M3Theme theme = M3Theme.defaultTheme();

        String stylesheet = M3ThemeManager.themeStylesheetUrl(theme);
        String repeatedStylesheet = M3ThemeManager.themeStylesheetUrl(theme);
        String stylesheetContent = Files.readString(Path.of(URI.create(stylesheet)));

        assertEquals(stylesheet, repeatedStylesheet);
        assertTrue(stylesheet.startsWith("file:"));
        assertTrue(stylesheet.endsWith(".css"));
        assertTrue(stylesheetContent.contains(".m3-root"));
        assertTrue(stylesheetContent.contains("-m3-color-primary"));
        assertTrue(stylesheetContent.contains(".m3-filled-button"));
        assertTrue(stylesheetContent.contains(".m3-elevated-card .m3-card-container"));
    }

    /// Verifies that generated theme stylesheets can be installed independently.
    @Test
    void installsGeneratedThemeStylesheetIndependently() {
        Pane root = new Pane();
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        M3ThemeManager.installThemeStylesheet(scene, theme);
        M3ThemeManager.installThemeStylesheet(scene, theme);

        assertEquals(1, scene.getStylesheets().size());
        assertEquals(M3ThemeManager.themeStylesheetUrl(theme), scene.getStylesheets().get(0));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertFalse(root.getProperties().containsKey(M3ThemeManager.THEME_PROPERTY_KEY));

        M3ThemeManager.uninstallThemeStylesheet(scene);
        M3ThemeManager.uninstallThemeStylesheet(scene);

        assertEquals(0, scene.getStylesheets().size());
    }

    /// Verifies that installed theme stylesheets keep application styles later in the cascade.
    @Test
    void installsThemeStylesheetsBeforeApplicationStylesheets() throws Exception {
        M3NavigationDrawer navigationDrawer = new M3NavigationDrawer();
        navigationDrawer.getStyleClass().add("test-sidebar-drawer");
        Pane root = new Pane(navigationDrawer);
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();
        String applicationStylesheet = temporaryStylesheet("""
                .m3-navigation-drawer.test-sidebar-drawer {
                    -fx-min-width: 320px;
                    -fx-pref-width: 320px;
                    -fx-max-width: 320px;
                }
                """);

        scene.getStylesheets().add(applicationStylesheet);
        M3ThemeManager.install(scene, theme);
        root.applyCss();

        assertEquals(M3ThemeManager.stylesheetUrl(), scene.getStylesheets().get(0));
        assertEquals(M3ThemeManager.themeStylesheetUrl(theme), scene.getStylesheets().get(1));
        assertEquals(applicationStylesheet, scene.getStylesheets().get(2));
        assertEquals(320.0, navigationDrawer.getMinWidth(), 0.0001);
        assertEquals(320.0, navigationDrawer.getPrefWidth(), 0.0001);
        assertEquals(320.0, navigationDrawer.getMaxWidth(), 0.0001);
    }

    /// Verifies that application styles can pin icon button metrics across theme reinstallations.
    @Test
    void preservesApplicationIconButtonMetricsAcrossThemeReinstall() throws Exception {
        M3IconButton button = new M3IconButton();
        button.getStyleClass().add("test-seed-button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);
        String applicationStylesheet = temporaryStylesheet("""
                .m3-icon-button.test-seed-button {
                    -m3-container-height: 32px;
                    -m3-container-shape: 999px;
                    -m3-horizontal-padding: 0px;
                    -fx-min-width: 32px;
                    -fx-min-height: 32px;
                    -fx-pref-width: 32px;
                    -fx-pref-height: 32px;
                    -fx-max-width: 32px;
                    -fx-max-height: 32px;
                    -fx-background-radius: 999px;
                    -fx-border-radius: 999px;
                }
                """);

        scene.getStylesheets().add(applicationStylesheet);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertSeedButtonMetrics(button);

        M3ThemeManager.install(scene, M3Theme.fromSeed(Color.web("#006a6a")));
        root.applyCss();

        assertEquals(M3ThemeManager.stylesheetUrl(), scene.getStylesheets().get(0));
        assertEquals(M3ThemeManager.themeStylesheetUrl(M3Theme.fromSeed(Color.web("#006a6a"))), scene.getStylesheets().get(1));
        assertEquals(applicationStylesheet, scene.getStylesheets().get(2));
        assertSeedButtonMetrics(button);
    }

    /// Verifies that theme manager installation can be reverted.
    @Test
    void uninstallsThemeFromScene() {
        Pane root = new Pane();
        root.getStyleClass().add("app-root");
        root.setStyle("-fx-padding: 4px;");
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        M3ThemeManager.install(scene, theme);
        assertEquals(2, scene.getStylesheets().size());
        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(root.getStyle().contains("-m3-color-primary"));

        M3ThemeManager.uninstall(scene);
        M3ThemeManager.uninstall(scene);

        assertTrue(root.getStyleClass().contains("app-root"));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertFalse(root.getProperties().containsKey(M3ThemeManager.THEME_PROPERTY_KEY));
        assertEquals("-fx-padding: 4px;", root.getStyle());
        assertFalse(scene.getStylesheets().contains(M3ThemeManager.stylesheetUrl()));
        assertEquals(0, scene.getStylesheets().size());
    }

    /// Verifies that generated component stylesheets apply theme tokens to controls.
    @Test
    void installsGeneratedComponentStylesheet() {
        M3Button button = new M3Button("Button");
        M3TextField textField = new M3TextField();
        M3TextArea textArea = new M3TextArea();
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        M3Menu menu = new M3Menu(new M3MenuItem("Open"));
        M3SearchBar searchBar = new M3SearchBar();
        M3SearchView searchView = new M3SearchView();
        searchView.getResults().add(new M3ListItem("Result"));
        M3ListSectionHeader listSectionHeader = new M3ListSectionHeader("Results");
        M3SideSheet sideSheet = new M3SideSheet();
        M3BottomSheet bottomSheet = new M3BottomSheet();
        M3Card card = new M3Card();
        M3DialogPane dialogPane = new M3DialogPane();
        M3Snackbar snackbar = new M3Snackbar("Saved");
        M3TopAppBar topAppBar = new M3TopAppBar("Inbox");
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        M3Scrim scrim = new M3Scrim();
        M3Avatar avatar = new M3Avatar("A");
        M3Text displayText = new M3Text("Display", M3TextRole.DISPLAY_LARGE);
        M3Chip chip = new M3Chip("Chip");
        M3FloatingActionButton fab = new M3FloatingActionButton();
        fab.setSize(M3FloatingActionButtonSize.LARGE);
        M3SegmentedButton segmentedButton = new M3SegmentedButton("Week");
        M3Tab tab = M3Tab.withSelected("Overview", true);
        Pane root = new Pane(
                button,
                textField,
                textArea,
                checkBox,
                slider,
                menu,
                searchBar,
                searchView,
                listSectionHeader,
                sideSheet,
                bottomSheet,
                card,
                dialogPane,
                snackbar,
                topAppBar,
                bottomAppBar,
                scrim,
                avatar,
                displayText,
                chip,
                fab,
                segmentedButton,
                tab
        );
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(48.0, button.getContainerHeight(), 0.0001);
        assertEquals(28.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, textField.getContainerHeight(), 0.0001);
        assertEquals(20.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(128.0, textArea.getContainerHeight(), 0.0001);
        assertEquals(20.0, textArea.getHorizontalPadding(), 0.0001);
        assertEquals(20.0, textArea.getVerticalPadding(), 0.0001);
        assertEquals(48.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(6.0, slider.getTrackThickness(), 0.0001);
        assertEquals(24.0, slider.getThumbSize(), 0.0001);
        assertEquals(56.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(56.0, ((M3MenuItem) menu.getItems().get(0)).getOneLineHeight(), 0.0001);
        assertEquals(10.0, menu.getPadding().getTop(), 0.0001);
        assertEquals(16.0, ((M3MenuItem) menu.getItems().get(0)).getHorizontalPadding(), 0.0001);
        assertEquals(16.0, ((M3MenuItem) menu.getItems().get(0)).getContentSpacing(), 0.0001);
        assertEquals(64.0, searchBar.getPrefHeight(), 0.0001);
        assertEquals(20.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(64.0, ((M3ListItem) searchView.getResults().get(0)).getOneLineHeight(), 0.0001);
        assertEquals(20.0, ((M3ListItem) searchView.getResults().get(0)).getHorizontalPadding(), 0.0001);
        assertEquals(56.0, listSectionHeader.prefHeight(-1.0), 0.0001);
        assertEquals(20.0, listSectionHeader.getPadding().getLeft(), 0.0001);
        assertEquals(384.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(360.0, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(
                28.0,
                ((Region) sideSheet.lookup("." + M3SideSheet.CONTENT_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(24.0, card.getContainerShape(), 0.0001);
        assertEquals(20.0, card.getContentPadding(), 0.0001);
        assertEquals(32.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(28.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getTop(), 0.0001);
        assertEquals(16.0, snackbar.getContainerShape(), 0.0001);
        assertEquals(18.0, snackbar.getContentPadding(), 0.0001);
        assertEquals(72.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(24.0, topAppBar.getPadding().getLeft(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(120.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(24.0, topAppBar.getPadding().getBottom(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(160.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(32.0, topAppBar.getPadding().getBottom(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.SMALL);
        root.applyCss();
        assertEquals(88.0, bottomAppBar.getPrefHeight(), 0.0001);
        assertEquals(24.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(0.32, scrim.getOpacity(), 0.0001);
        assertEquals(44.0, avatar.getContainerSize(), 0.0001);
        assertEquals(64.0, displayText.getTypographyFontSize(), 0.0001);
        assertEquals(72.0, displayText.getTypographyLineHeight(), 0.0001);
        assertEquals(36.0, chip.getContainerHeight(), 0.0001);
        assertEquals(18.0, chip.getHorizontalPadding(), 0.0001);
        assertEquals(104.0, fab.getContainerSize(), 0.0001);
        assertEquals(28.0, fab.getHorizontalPadding(), 0.0001);
        assertEquals(48.0, segmentedButton.getContainerHeight(), 0.0001);
        assertEquals(16.0, segmentedButton.getHorizontalPadding(), 0.0001);
        assertEquals(56.0, tab.getContainerHeight(), 0.0001);
        assertEquals(20.0, tab.getHorizontalPadding(), 0.0001);
        assertEquals(4.0, tab.getActiveIndicatorHeight(), 0.0001);

        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, baselineTheme);
        root.applyCss();

        assertEquals(40.0, button.getContainerHeight(), 0.0001);
        assertEquals(24.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(56.0, textField.getContainerHeight(), 0.0001);
        assertEquals(16.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(112.0, textArea.getContainerHeight(), 0.0001);
        assertEquals(16.0, textArea.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, textArea.getVerticalPadding(), 0.0001);
        assertEquals(40.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(4.0, slider.getTrackThickness(), 0.0001);
        assertEquals(20.0, slider.getThumbSize(), 0.0001);
        assertEquals(48.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(48.0, ((M3MenuItem) menu.getItems().get(0)).getOneLineHeight(), 0.0001);
        assertEquals(56.0, searchBar.getPrefHeight(), 0.0001);
        assertEquals(56.0, ((M3ListItem) searchView.getResults().get(0)).getOneLineHeight(), 0.0001);
        assertEquals(48.0, listSectionHeader.prefHeight(-1.0), 0.0001);
        assertEquals(16.0, listSectionHeader.getPadding().getLeft(), 0.0001);
        assertEquals(360.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(320.0, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(12.0, card.getContainerShape(), 0.0001);
        assertEquals(16.0, card.getContentPadding(), 0.0001);
        assertEquals(28.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(24.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(4.0, snackbar.getContainerShape(), 0.0001);
        assertEquals(16.0, snackbar.getContentPadding(), 0.0001);
        assertEquals(64.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, topAppBar.getPadding().getLeft(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(112.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(20.0, topAppBar.getPadding().getBottom(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(152.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(28.0, topAppBar.getPadding().getBottom(), 0.0001);
        assertEquals(80.0, bottomAppBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(0.32, scrim.getOpacity(), 0.0001);
        assertEquals(40.0, avatar.getContainerSize(), 0.0001);
        assertEquals(57.0, displayText.getTypographyFontSize(), 0.0001);
        assertEquals(64.0, displayText.getTypographyLineHeight(), 0.0001);
        assertEquals(32.0, chip.getContainerHeight(), 0.0001);
        assertEquals(16.0, chip.getHorizontalPadding(), 0.0001);
        assertEquals(96.0, fab.getContainerSize(), 0.0001);
        assertEquals(24.0, fab.getHorizontalPadding(), 0.0001);
        assertEquals(40.0, segmentedButton.getContainerHeight(), 0.0001);
        assertEquals(12.0, segmentedButton.getHorizontalPadding(), 0.0001);
        assertEquals(48.0, tab.getContainerHeight(), 0.0001);
        assertEquals(16.0, tab.getHorizontalPadding(), 0.0001);
        assertEquals(3.0, tab.getActiveIndicatorHeight(), 0.0001);
        assertEquals(2, scene.getStylesheets().size());
    }

    /// Verifies that generated component stylesheets apply utility component tokens.
    @Test
    void generatedComponentStylesheetAppliesUtilityTokens() {
        M3Divider divider = new M3Divider();
        M3Badge badge = new M3Badge("12");
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        Pane root = new Pane(divider, badge, progressBar, progressIndicator);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(1.0, divider.getThickness(), 0.0001);
        assertEquals(0.0, divider.getInsetStart(), 0.0001);
        assertEquals(0.0, divider.getInsetEnd(), 0.0001);
        assertEquals(8.0, badge.getSmallSize(), 0.0001);
        assertEquals(18.0, badge.getLargeHeight(), 0.0001);
        assertEquals(18.0, badge.getLargeMinWidth(), 0.0001);
        assertEquals(9.0, badge.getContainerShape(), 0.0001);
        assertEquals(3.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(40.0, progressBar.getWavelength(), 0.0001);
        assertEquals(2.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(15.0, progressIndicator.getWavelength(), 0.0001);
    }

    /// Verifies that generated component stylesheets apply list item tokens.
    @Test
    void generatedComponentStylesheetAppliesListItemTokens() {
        M3ListItem listItem = new M3ListItem("Headline");
        M3ListSectionHeader sectionHeader = new M3ListSectionHeader("Pinned");
        Pane root = new Pane(listItem, sectionHeader);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(64.0, listItem.getOneLineHeight(), 0.0001);
        assertEquals(80.0, listItem.getTwoLineHeight(), 0.0001);
        assertEquals(96.0, listItem.getThreeLineHeight(), 0.0001);
        assertEquals(10.0, listItem.getContainerShape(), 0.0001);
        assertEquals(20.0, listItem.getHorizontalPadding(), 0.0001);
        assertEquals(10.0, listItem.getVerticalPadding(), 0.0001);
        assertEquals(20.0, listItem.getContentSpacing(), 0.0001);
        assertEquals(56.0, sectionHeader.prefHeight(-1.0), 0.0001);
        assertEquals(20.0, sectionHeader.getPadding().getLeft(), 0.0001);
    }

    /// Verifies that generated component stylesheets apply container tokens.
    @Test
    void generatedComponentStylesheetAppliesContainerTokens() {
        M3Card card = new M3Card();
        M3DialogPane dialogPane = new M3DialogPane();
        Pane root = new Pane(card, dialogPane);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(24.0, card.getContainerShape(), 0.0001);
        assertEquals(20.0, card.getContentPadding(), 0.0001);
        assertEquals(1.0, card.getOutlineWidth(), 0.0001);
        assertEquals(32.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(28.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(28.0, dialogPane.getPadding().getTop(), 0.0001);
    }

    /// Writes an application stylesheet for stylesheet cascade tests.
    private static String temporaryStylesheet(String content) throws Exception {
        Path path = Files.createTempFile("m3fx-test-", ".css");
        Files.writeString(path, content);
        path.toFile().deleteOnExit();
        return path.toUri().toString();
    }

    /// Verifies that an icon button keeps the fixed swatch metrics from application CSS.
    private static void assertSeedButtonMetrics(M3IconButton button) {
        assertEquals(32.0, button.getContainerHeight(), 0.0001);
        assertEquals(999.0, button.getContainerShape(), 0.0001);
        assertEquals(32.0, button.getMinWidth(), 0.0001);
        assertEquals(32.0, button.getMinHeight(), 0.0001);
        assertEquals(32.0, button.getPrefWidth(), 0.0001);
        assertEquals(32.0, button.getPrefHeight(), 0.0001);
        assertEquals(32.0, button.getMaxWidth(), 0.0001);
        assertEquals(32.0, button.getMaxHeight(), 0.0001);
    }
}
