// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.theme;

import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionEasing;
import org.glavo.m3fx.controls.M3Avatar;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3Banner;
import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3ButtonSize;
import org.glavo.m3fx.controls.M3ButtonGroupVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3Carousel;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3FormRow;
import org.glavo.m3fx.controls.M3FormSection;
import org.glavo.m3fx.controls.M3FormValidator;
import org.glavo.m3fx.controls.M3FabMenu;
import org.glavo.m3fx.controls.M3Icon;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3ListPane;
import org.glavo.m3fx.controls.M3ListSectionHeader;
import org.glavo.m3fx.controls.M3ListStyle;
import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuButton;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.controls.M3NavigationRail;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.controls.M3RichTooltip;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SideSheet;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3Tab;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputValidators;
import org.glavo.m3fx.controls.M3Toolbar;
import org.glavo.m3fx.controls.M3ValidationSummary;
import org.glavo.m3fx.controls.M3TextRole;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.internal.theme.M3ThemeCssCompiler;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.controls.M3TopAppBarVariant;
import org.glavo.m3fx.tokens.M3ComponentTokens;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.glavo.m3fx.M3TestControls.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        FxTestUtils.startToolkit();
    }

    /// Verifies that the baseline profile creates a complete token set.
    @Test
    void createsBaselineTokenSet() {
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));

        assertEquals(M3Profile.BASELINE_2021, theme.profile());
        assertSame(theme.colorScheme(), theme.tokens().colorTokens().colorScheme());
        assertEquals(M3MotionEasing.STANDARD, theme.tokens().motionTokens().defaultEffects().easing());
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-monet-primary"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-color-primary"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-motion-default-effects-easing: standard"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-typescale-label-large-font-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-typescale-display-medium-font-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-typescale-body-small-line-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-typescale-body-medium-tracking: 0.25px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-shape-corner-extra-extra-large: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-state-focus-indicator-thickness: 3px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-state-disabled-container-color: rgba(29,27,32,0.12)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-state-disabled-content-color: rgba(29,27,32,0.38)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-button-disabled-container-color: rgba(29,27,32,0.1)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-list-item-disabled-state-layer-color: rgba(29,27,32,0.1)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-filled-card-disabled-container-color: rgba(231,224,235,0.38)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-elevated-card-disabled-container-color: rgba(253,247,255,0.38)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-outlined-card-disabled-outline-color: rgba(122,117,127,0.12)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-text-field-disabled-container-color: rgba(29,27,32,0.04)"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-text-field-hover-container-color"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-text-input-trailing-icon-color"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-button-filled-container-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-split-button-menu-width"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-fab-regular-container-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-segmented-button-container-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-tab-container-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-track-thickness"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-container-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-icon-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-outline-width"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-progress-indicator-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-progress-wave-indicator-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-progress-linear-indeterminate-wavelength"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-loading-indicator-container-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-loading-indicator-indicator-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-card-content-padding"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-divider-thickness"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-badge-small-size"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-container-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-bottom-app-bar-container-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-rail-collapsed-container-width"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-drawer-container-width"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-list-item-one-line-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-list-section-header-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-form-row-min-height"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-validation-summary-container-shape"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-surface-content-padding"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-track-horizontal-padding"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-track-vertical-padding"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-item-spacing"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-item-shape"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-small-item-min-width"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-small-item-max-width"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-large-item-max-width"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-container-color: -m3-color-surface-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-selected-item-container-color: -m3-color-secondary-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-selected-item-content-color: -m3-color-on-secondary-container"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-filled-button"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(
                ".m3-button-group.m3-button-group-small.m3-connected-button-group "
                        + ".m3-grouped-button.m3-button-group-first"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-menu-button.m3-split-button-menu"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-large-icon"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-display-medium-text"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-body-small-text"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-regular-fab"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-segmented-button"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-segmented-button-first"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-tab-active-indicator"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-checkbox:hover"));
        assertFalse(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-slider:pressed .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-loading-indicator"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-list-item:disabled"));
        assertFalse(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-slider:focus-visible .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(
                ".m3-card:actionable:focus-visible .m3-state-layer"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-card:dragged .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-dialog-pane"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-badge"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-top-app-bar"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-bottom-app-bar"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-navigation-rail"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-navigation-drawer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-list-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-list-section-header"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-form-row"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-validation-summary-item:hover .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-validation-summary-item:focus-visible .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-validation-summary-item:pressed .m3-state-layer"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-carousel-track"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-surface"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-fx-opacity: 0.08"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-fx-opacity: 0.1"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-elevated-card .m3-card-container"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(
                ".m3-elevated-card:actionable:hover .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(
                ".m3-filled-card:actionable:hover .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(
                ".m3-outlined-card:actionable:hover .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(
                ".m3-filled-card:actionable:focus-visible .m3-card-container"
        ));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-fab:hover"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-surface-elevation-level5"));
        assertNotNull(theme.tokens().componentTokens().filledButton());
        assertNotNull(theme.tokens().componentTokens().floatingActionButton());
        assertNotNull(theme.tokens().componentTokens().icon());
        assertNotNull(theme.tokens().componentTokens().buttonGroup());
        assertNotNull(theme.tokens().componentTokens().segmentedButton());
        assertNotNull(theme.tokens().componentTokens().tab());
        assertNotNull(theme.tokens().componentTokens().slider());
        assertNotNull(theme.tokens().componentTokens().chip());
        assertNotNull(theme.tokens().componentTokens().loadingIndicator());
        assertNotNull(theme.tokens().componentTokens().divider());
        assertNotNull(theme.tokens().componentTokens().badge());
        assertNotNull(theme.tokens().componentTokens().topAppBar());
        assertNotNull(theme.tokens().componentTokens().bottomAppBar());
        assertNotNull(theme.tokens().componentTokens().navigationBar());
        assertNotNull(theme.tokens().componentTokens().navigationRail());
        assertNotNull(theme.tokens().componentTokens().navigationDrawer());
        assertNotNull(theme.tokens().componentTokens().listItem());
        assertNotNull(theme.tokens().componentTokens().form());
        assertNotNull(theme.tokens().componentTokens().validationSummary());
        assertNotNull(theme.tokens().componentTokens().surface());
        assertNotNull(theme.tokens().componentTokens().carousel());
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
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-monet-primary"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-color-primary"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-motion-default-effects-easing: emphasized"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-motion-default-spatial-duration: 400ms"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-motion-sub-menu-hover-open-delay: 150ms"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-button-filled-container-height: 40px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-button-filled-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-button-text-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-icon-small-size: 20px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-icon-large-size: 36px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-fab-regular-container-size: 56px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-fab-regular-leading-space: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-fab-medium-container-size: 80px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-fab-large-container-size: 96px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-segmented-button-container-height: 40px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-segmented-button-horizontal-padding: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-tab-container-height: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-tab-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-tab-active-indicator-height: 3px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-secondary-tab-active-indicator-height: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-tab-active-indicator-min-width: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-tab-active-indicator-horizontal-inset: 2px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-field-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-text-area-vertical-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-selection-touch-target-size: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-selection-state-layer-size: 40px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-checkbox-container-size: 18px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-checkbox-selected-mark-width: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-checkbox-selected-mark-height: 10px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-checkbox-indeterminate-mark-width: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-checkbox-indeterminate-mark-height: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-radio-container-size: 20px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-radio-selected-dot-size: 10px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-touch-target-size: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-track-width: 52px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-track-height: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-state-layer-size: 40px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-unselected-handle-size: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-with-icon-handle-size: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-selected-handle-size: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-pressed-handle-size: 28px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-switch-icon-size: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-track-thickness: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-thumb-size: 44px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-thumb-width: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-focused-thumb-width: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-pressed-thumb-width: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-thumb-track-gap: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-slider-touch-target-size: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-container-height: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-icon-horizontal-padding: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-icon-size: 18px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-avatar-size: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-chip-group-horizontal-gap: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-container-shape: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-container-padding: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-item-height: 44px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-item-container-shape: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-selected-item-container-shape: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-first-item-container-shape: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-last-item-container-shape: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-item-content-spacing: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-item-spacing: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-container-color: -m3-color-surface-container-low"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-selected-item-container-color: -m3-color-tertiary-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-selected-item-content-color: -m3-color-on-tertiary-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-vibrant-container-color: -m3-color-tertiary-container"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-menu-vibrant-selected-item-container-color: -m3-color-tertiary"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-bar-container-height: 56px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-bar-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-bar-content-spacing: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-contained-bar-horizontal-padding: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-contained-bar-content-spacing: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-divided-bar-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-bar-trailing-actions-gap: 0px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-horizontal-padding: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-bar-results-gap: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-results-shape: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-docked-bottom-padding: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-full-screen-bottom-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-min-width: 360px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-max-width: 720px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-docked-min-height: 240px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-search-view-full-screen-divided-header-height: 72px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-sheet-side-container-width: 256px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-sheet-side-container-max-width: 400px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-sheet-bottom-container-max-width: 640px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-sheet-content-padding: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-sheet-drag-handle-width: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-card-container-shape: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-card-content-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-dialog-container-shape: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-dialog-content-padding: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-dialog-container-min-width: 280px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-dialog-container-max-width: 560px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-dialog-action-spacing: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-dialog-icon-size: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-snackbar-container-shape: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-snackbar-content-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-snackbar-container-min-width: 344px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-snackbar-container-max-width: 672px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-snackbar-single-line-container-height: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-snackbar-two-line-container-height: 68px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-snackbar-action-container-height: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-banner-container-min-height: 80px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-banner-horizontal-padding: 24px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-tooltip-plain-container-shape: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-tooltip-rich-pref-width: 320px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-picker-field-popup-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-date-picker-day-cell-size: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-time-picker-dial-size: 256px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-time-picker-dial-handle-size: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-time-picker-input-field-height: 72px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-badge-small-size: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-container-height: 64px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-medium-container-height: 112px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-large-container-height: 152px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-top-app-bar-medium-flexible-container-height: 112px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-top-app-bar-medium-flexible-subtitle-container-height: 136px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-top-app-bar-large-flexible-container-height: 120px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-top-app-bar-large-flexible-subtitle-container-height: 152px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-edge-padding: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-medium-bottom-padding: 20px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-large-bottom-padding: 28px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-flexible-bottom-padding: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-content-spacing: 0px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-top-app-bar-action-spacing: 0px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-bottom-app-bar-container-height: 80px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-bottom-app-bar-horizontal-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-bottom-app-bar-action-spacing: 0px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-navigation-rail-collapsed-container-width: 96px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-navigation-rail-narrow-collapsed-container-width: 80px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-navigation-rail-expanded-minimum-container-width: 220px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains(
                "-m3-navigation-rail-expanded-maximum-container-width: 360px"
        ));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-drawer-container-width: 360px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-bar-horizontal-padding: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-bar-item-spacing: 6px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-rail-item-spacing: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-drawer-container-padding: 12px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-navigation-drawer-group-child-item-horizontal-padding: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-list-item-one-line-height: 64px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-list-item-horizontal-padding: 20px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-list-item-content-spacing: 20px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-list-section-header-height: 56px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-list-section-header-horizontal-padding: 20px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-picker-field-popup-shape: 32px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-form-row-min-height: 64px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-validation-summary-content-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-split-button-spacing: 2px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-split-button-menu-width: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-icon-toggle-button-group-spacing: 10px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-fab-menu-action-spacing: 4px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-fab-menu-close-spacing: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-loading-indicator-container-size: 48px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-loading-indicator-indicator-size: 38px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-surface-content-padding: 16px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-item-spacing: 8px"));
        assertTrue(M3ThemeCssCompiler.rootStyleDeclarations(theme).contains("-m3-carousel-item-shape: 32px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-container-height: 48px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-content-spacing: 4px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-horizontal-padding: 32px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-fx-padding: 8px 10px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-menu .m3-menu-item.m3-menu-item:first-menu-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains(".m3-menu .m3-menu-item.m3-menu-item:last-menu-item"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-fx-padding: 0 24px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-fx-padding: 24px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-selected-mark-width: 12px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-selected-dot-size: 10px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-fx-background-radius: 999px"));
        assertNotNull(theme.tokens().componentTokens().filledButton());
        assertNotNull(theme.tokens().componentTokens().floatingActionButton());
        assertNotNull(theme.tokens().componentTokens().icon());
        assertNotNull(theme.tokens().componentTokens().buttonGroup());
        assertNotNull(theme.tokens().componentTokens().segmentedButton());
        assertNotNull(theme.tokens().componentTokens().tab());
        assertNotNull(theme.tokens().componentTokens().slider());
        assertNotNull(theme.tokens().componentTokens().chip());
        assertNotNull(theme.tokens().componentTokens().loadingIndicator());
        assertNotNull(theme.tokens().componentTokens().divider());
        assertNotNull(theme.tokens().componentTokens().badge());
        assertNotNull(theme.tokens().componentTokens().topAppBar());
        assertNotNull(theme.tokens().componentTokens().bottomAppBar());
        assertNotNull(theme.tokens().componentTokens().navigationBar());
        assertNotNull(theme.tokens().componentTokens().navigationRail());
        assertNotNull(theme.tokens().componentTokens().navigationDrawer());
        assertNotNull(theme.tokens().componentTokens().listItem());
        assertNotNull(theme.tokens().componentTokens().pickerField());
        assertNotNull(theme.tokens().componentTokens().datePicker());
        assertNotNull(theme.tokens().componentTokens().timePicker());
        assertNotNull(theme.tokens().componentTokens().form());
        assertNotNull(theme.tokens().componentTokens().validationSummary());
        assertEquals(16.0, theme.tokens().componentTokens().filledButton().horizontalPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().textButton().horizontalPadding(), 0.0001);
        assertEquals(36.0, theme.tokens().componentTokens().icon().largeSize(), 0.0001);
        M3ComponentTokens.SplitButtonSizeTokens smallSplitButton =
                theme.tokens().componentTokens().splitButton().small();
        assertEquals(
                48.0,
                smallSplitButton.menuLeadingSpace()
                        + smallSplitButton.menuIconSize()
                        + smallSplitButton.menuTrailingSpace(),
                0.0001
        );
        M3ComponentTokens.ButtonGroupSizeTokens smallButtonGroup =
                theme.tokens().componentTokens().buttonGroup().small();
        assertEquals(2.0, smallButtonGroup.connectedSpacing(), 0.0001);
        assertEquals(12.0, smallButtonGroup.standardSpacing(), 0.0001);
        assertEquals(0.15, smallButtonGroup.standardPressedWidthMultiplier(), 0.0001);
        assertEquals(10.0, smallButtonGroup.connectedInnerCorner(), 0.0001);
        assertEquals(-1.0, theme.tokens().componentTokens().buttonGroup().segmentedGroupSpacing(), 0.0001);
        assertEquals(10.0, theme.tokens().componentTokens().buttonGroup().iconToggleGroupSpacing(), 0.0001);
        M3ComponentTokens.FabTokens expressiveFab =
                theme.tokens().componentTokens().floatingActionButton();
        assertEquals(56.0, expressiveFab.regular().containerSize(), 0.0001);
        assertEquals(80.0, expressiveFab.medium().containerSize(), 0.0001);
        assertEquals(96.0, expressiveFab.large().containerSize(), 0.0001);
        assertEquals(24.0, expressiveFab.regular().iconSize(), 0.0001);
        assertEquals(28.0, expressiveFab.medium().iconSize(), 0.0001);
        assertEquals(36.0, expressiveFab.large().iconSize(), 0.0001);
        assertEquals(16.0, expressiveFab.regular().leadingSpace(), 0.0001);
        assertEquals(8.0, expressiveFab.regular().iconLabelSpace(), 0.0001);
        assertEquals(16.0, expressiveFab.regular().trailingSpace(), 0.0001);
        assertEquals(4.0, theme.tokens().componentTokens().floatingActionButton().menuActionSpacing(), 0.0001);
        assertEquals(8.0, theme.tokens().componentTokens().floatingActionButton().menuCloseSpacing(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().segmentedButton().horizontalPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().tab().horizontalPadding(), 0.0001);
        assertEquals(3.0, theme.tokens().componentTokens().tab().activeIndicatorHeight(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().chip().horizontalPadding(), 0.0001);
        assertEquals(8.0, theme.tokens().componentTokens().chip().iconHorizontalPadding(), 0.0001);
        assertEquals(18.0, theme.tokens().componentTokens().chip().iconSize(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().chip().avatarSize(), 0.0001);
        assertEquals(8.0, theme.tokens().componentTokens().chip().groupHorizontalGap(), 0.0001);
        M3ComponentTokens.ProgressTokens expressiveProgress = theme.tokens().componentTokens().progress();
        assertEquals(40.0, expressiveProgress.indicatorSize(), 0.0001);
        assertEquals(48.0, expressiveProgress.waveIndicatorSize(), 0.0001);
        assertEquals(3.0, expressiveProgress.linearWaveAmplitude(), 0.0001);
        assertEquals(40.0, expressiveProgress.linearWavelength(), 0.0001);
        assertEquals(20.0, expressiveProgress.linearIndeterminateWavelength(), 0.0001);
        assertEquals(1.6, expressiveProgress.circularWaveAmplitude(), 0.0001);
        assertEquals(15.0, expressiveProgress.circularWavelength(), 0.0001);
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-wave-amplitude: 0px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-wave-indicator-size: 48px"));
        assertTrue(M3ThemeCssCompiler.controlStyleRules(theme).contains("-m3-indeterminate-wavelength: 20px"));
        assertEquals(48.0, theme.tokens().componentTokens().loadingIndicator().containerSize(), 0.0001);
        assertEquals(38.0, theme.tokens().componentTokens().loadingIndicator().indicatorSize(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().field().horizontalPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().textArea().verticalPadding(), 0.0001);
        assertEquals(48.0, theme.tokens().componentTokens().selection().touchTargetSize(), 0.0001);
        assertEquals(40.0, theme.tokens().componentTokens().selection().stateLayerSize(), 0.0001);
        assertEquals(18.0, theme.tokens().componentTokens().selection().checkboxContainerSize(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().selection().checkboxSelectedMarkWidth(), 0.0001);
        assertEquals(10.0, theme.tokens().componentTokens().selection().checkboxSelectedMarkHeight(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().selection().checkboxIndeterminateMarkWidth(), 0.0001);
        assertEquals(2.0, theme.tokens().componentTokens().selection().checkboxIndeterminateMarkHeight(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().selection().radioContainerSize(), 0.0001);
        assertEquals(10.0, theme.tokens().componentTokens().selection().radioSelectedDotSize(), 0.0001);
        assertEquals(48.0, theme.tokens().componentTokens().selection().switchTouchTargetSize(), 0.0001);
        assertEquals(52.0, theme.tokens().componentTokens().selection().switchTrackWidth(), 0.0001);
        assertEquals(32.0, theme.tokens().componentTokens().selection().switchTrackHeight(), 0.0001);
        assertEquals(40.0, theme.tokens().componentTokens().selection().switchStateLayerSize(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().selection().switchUnselectedHandleSize(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().selection().switchWithIconHandleSize(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().selection().switchSelectedHandleSize(), 0.0001);
        assertEquals(28.0, theme.tokens().componentTokens().selection().switchPressedHandleSize(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().selection().switchIconSize(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().slider().sizing().extraSmall().trackThickness(), 0.0001);
        assertEquals(44.0, theme.tokens().componentTokens().slider().sizing().extraSmall().thumbSize(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().slider().sizing().small().trackThickness(), 0.0001);
        assertEquals(40.0, theme.tokens().componentTokens().slider().sizing().medium().trackThickness(), 0.0001);
        assertEquals(56.0, theme.tokens().componentTokens().slider().sizing().large().trackThickness(), 0.0001);
        assertEquals(96.0, theme.tokens().componentTokens().slider().sizing().extraLarge().trackThickness(), 0.0001);
        assertEquals(4.0, theme.tokens().componentTokens().slider().thumbWidth(), 0.0001);
        assertEquals(2.0, theme.tokens().componentTokens().slider().focusedThumbWidth(), 0.0001);
        assertEquals(2.0, theme.tokens().componentTokens().slider().pressedThumbWidth(), 0.0001);
        assertEquals(6.0, theme.tokens().componentTokens().slider().thumbTrackGap(), 0.0001);
        assertEquals(48.0, theme.tokens().componentTokens().slider().touchTargetSize(), 0.0001);
        assertEquals(8.0, theme.tokens().componentTokens().navigationBar().horizontalPadding(), 0.0001);
        assertEquals(96.0, theme.tokens().componentTokens().navigationRail().collapsedContainerWidth(), 0.0001);
        assertEquals(80.0, theme.tokens().componentTokens().navigationRail().narrowCollapsedContainerWidth(), 0.0001);
        assertEquals(44.0, theme.tokens().componentTokens().navigationRail().collapsedTopPadding(), 0.0001);
        assertEquals(0.0, theme.tokens().componentTokens().navigationRail().collapsedBottomPadding(), 0.0001);
        assertEquals(220.0, theme.tokens().componentTokens().navigationRail().expandedMinimumContainerWidth(), 0.0001);
        assertEquals(280.0, theme.tokens().componentTokens().navigationRail().expandedContainerWidth(), 0.0001);
        assertEquals(360.0, theme.tokens().componentTokens().navigationRail().expandedMaximumContainerWidth(), 0.0001);
        assertEquals(44.0, theme.tokens().componentTokens().navigationRail().expandedTopPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().navigationRail().expandedBottomPadding(), 0.0001);
        assertEquals(40.0, theme.tokens().componentTokens().navigationRail().headerSpacing(), 0.0001);
        assertEquals(
                theme.tokens().shapeTokens().large(),
                theme.tokens().componentTokens().navigationRail().modalContainerShape(),
                0.0001
        );
        assertEquals(32.0, theme.tokens().componentTokens().navigationDrawer().groupChildItemHorizontalPadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().listItem().horizontalPadding(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().menu().containerShape(), 0.0001);
        assertEquals(2.0, theme.tokens().componentTokens().menu().containerPadding(), 0.0001);
        assertEquals(44.0, theme.tokens().componentTokens().menu().itemHeight(), 0.0001);
        assertEquals(6.0, theme.tokens().componentTokens().menu().itemContainerShape(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().menu().selectedItemContainerShape(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().menu().firstItemContainerShape(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().menu().lastItemContainerShape(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().menu().itemContentSpacing(), 0.0001);
        assertEquals(56.0, theme.tokens().componentTokens().search().barHeight(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().search().barHorizontalPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().search().barContentSpacing(), 0.0001);
        assertEquals(4.0, theme.tokens().componentTokens().search().containedBarHorizontalPadding(), 0.0001);
        assertEquals(4.0, theme.tokens().componentTokens().search().containedBarContentSpacing(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().search().dividedBarHorizontalPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().search().dividedBarContentSpacing(), 0.0001);
        assertEquals(0.0, theme.tokens().componentTokens().search().barTrailingActionsGap(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().search().viewHorizontalPadding(), 0.0001);
        assertEquals(2.0, theme.tokens().componentTokens().search().viewBarResultsGap(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().search().viewResultsShape(), 0.0001);
        assertEquals(4.0, theme.tokens().componentTokens().search().viewDockedBottomPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().search().viewFullScreenBottomPadding(), 0.0001);
        assertEquals(360.0, theme.tokens().componentTokens().search().viewMinWidth(), 0.0001);
        assertEquals(720.0, theme.tokens().componentTokens().search().viewMaxWidth(), 0.0001);
        assertEquals(240.0, theme.tokens().componentTokens().search().viewDockedMinHeight(), 0.0001);
        assertEquals(72.0, theme.tokens().componentTokens().search().viewFullScreenDividedHeaderHeight(), 0.0001);
        assertEquals(32.0, theme.tokens().componentTokens().pickerField().popupShape(), 0.0001);
        assertEquals(64.0, theme.tokens().componentTokens().form().rowMinHeight(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().validationSummary().contentPadding(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().surface().containerShape(), 0.0001);
        assertEquals(8.0, theme.tokens().componentTokens().carousel().itemSpacing(), 0.0001);
        assertEquals(32.0, theme.tokens().componentTokens().carousel().itemShape(), 0.0001);
        assertEquals(40.0, theme.tokens().componentTokens().carousel().smallItemMinWidth(), 0.0001);
        assertEquals(56.0, theme.tokens().componentTokens().carousel().smallItemMaxWidth(), 0.0001);
        assertEquals(320.0, theme.tokens().componentTokens().carousel().largeItemMaxWidth(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().sheet().contentPadding(), 0.0001);
        assertEquals(32.0, theme.tokens().componentTokens().sheet().dragHandleWidth(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().card().containerShape(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().card().contentPadding(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().dialog().contentPadding(), 0.0001);
        assertEquals(280.0, theme.tokens().componentTokens().dialog().containerMinWidth(), 0.0001);
        assertEquals(560.0, theme.tokens().componentTokens().dialog().containerMaxWidth(), 0.0001);
        assertEquals(8.0, theme.tokens().componentTokens().dialog().actionSpacing(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().dialog().iconSize(), 0.0001);
        assertEquals(6.0, theme.tokens().componentTokens().snackbar().containerShape(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().snackbar().contentPadding(), 0.0001);
        assertEquals(344.0, theme.tokens().componentTokens().snackbar().containerMinWidth(), 0.0001);
        assertEquals(672.0, theme.tokens().componentTokens().snackbar().containerMaxWidth(), 0.0001);
        assertEquals(48.0, theme.tokens().componentTokens().snackbar().singleLineContainerHeight(), 0.0001);
        assertEquals(68.0, theme.tokens().componentTokens().snackbar().twoLineContainerHeight(), 0.0001);
        assertEquals(32.0, theme.tokens().componentTokens().snackbar().actionContainerHeight(), 0.0001);
        assertEquals(80.0, theme.tokens().componentTokens().banner().containerMinHeight(), 0.0001);
        assertEquals(24.0, theme.tokens().componentTokens().banner().horizontalPadding(), 0.0001);
        assertEquals(6.0, theme.tokens().componentTokens().tooltip().plainContainerShape(), 0.0001);
        assertEquals(320.0, theme.tokens().componentTokens().tooltip().richPreferredWidth(), 0.0001);
        assertEquals(16.0, theme.tokens().componentTokens().topAppBar().horizontalPadding(), 0.0001);
        assertEquals(112.0, theme.tokens().componentTokens().topAppBar().mediumContainerHeight(), 0.0001);
        assertEquals(152.0, theme.tokens().componentTokens().topAppBar().largeContainerHeight(), 0.0001);
        assertEquals(112.0, theme.tokens().componentTokens().topAppBar().mediumFlexibleContainerHeight(), 0.0001);
        assertEquals(136.0,
                theme.tokens().componentTokens().topAppBar().mediumFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(120.0, theme.tokens().componentTokens().topAppBar().largeFlexibleContainerHeight(), 0.0001);
        assertEquals(152.0,
                theme.tokens().componentTokens().topAppBar().largeFlexibleSubtitleContainerHeight(), 0.0001);
        assertEquals(4.0, theme.tokens().componentTokens().topAppBar().edgePadding(), 0.0001);
        assertEquals(20.0, theme.tokens().componentTokens().topAppBar().mediumBottomPadding(), 0.0001);
        assertEquals(28.0, theme.tokens().componentTokens().topAppBar().largeBottomPadding(), 0.0001);
        assertEquals(12.0, theme.tokens().componentTokens().topAppBar().flexibleBottomPadding(), 0.0001);
        assertEquals(0.0, theme.tokens().componentTokens().topAppBar().contentSpacing(), 0.0001);
        assertEquals(0.0, theme.tokens().componentTokens().topAppBar().actionSpacing(), 0.0001);
        assertEquals(0.0, theme.tokens().componentTokens().bottomAppBar().actionSpacing(), 0.0001);
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
        M3TokenSet tokenSet = M3TokenSet.builder(M3Profile.BASELINE_2021, colorScheme, density).build();

        M3Theme theme = M3Theme.fromTokenSet(tokenSet);

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

        AtomicInteger mutations = new AtomicInteger();
        root.getStyleClass().addListener(
                (ListChangeListener<String>) change -> mutations.incrementAndGet()
        );
        root.getProperties().addListener(
                (MapChangeListener<Object, Object>) change -> mutations.incrementAndGet()
        );
        scene.getStylesheets().addListener(
                (ListChangeListener<String>) change -> mutations.incrementAndGet()
        );
        scene.getProperties().addListener(
                (MapChangeListener<Object, Object>) change -> mutations.incrementAndGet()
        );

        M3ThemeManager.install(scene, theme);

        assertEquals(0, mutations.get());
        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertSame(theme, M3ThemeManager.getTheme(root));
        assertTrue(root.getStyle().isEmpty());
        assertEquals(2, scene.getStylesheets().size());
        assertEquals(M3ThemeRuntime.stylesheetUrl(), scene.getStylesheets().get(0));
        assertTrue(M3ThemeRuntime.stylesheetUrl().endsWith("/styles/base.css"));
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

        AtomicInteger themeMetadataChanges = new AtomicInteger();
        root.getProperties().addListener(
                (MapChangeListener<Object, Object>) change -> themeMetadataChanges.incrementAndGet()
        );

        M3ThemeManager.install(scene, expressiveDarkTheme);

        assertEquals(1, themeMetadataChanges.get());
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
        root.setStyle("-fx-background-color: red;");
        popupRoot.setStyle("-fx-padding: 3px;");
        popupRoot.getStyleClass().add("popup-root");
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK
        );

        M3ThemeManager.install(scene, theme);
        M3ThemeRuntime.copyThemeContext(root, popupRoot);

        assertTrue(popupRoot.getStyleClass().contains("popup-root"));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertEquals("-fx-padding: 3px;", popupRoot.getStyle());
        assertSame(theme, M3ThemeManager.getTheme(popupRoot));

        AtomicInteger styleClassChanges = new AtomicInteger();
        AtomicInteger themeMetadataChanges = new AtomicInteger();
        popupRoot.getStyleClass().addListener(
                (ListChangeListener<String>) change -> styleClassChanges.incrementAndGet()
        );
        popupRoot.getProperties().addListener(
                (MapChangeListener<Object, Object>) change -> themeMetadataChanges.incrementAndGet()
        );

        M3ThemeRuntime.copyThemeContext(root, popupRoot);

        assertEquals(0, styleClassChanges.get());
        assertEquals(0, themeMetadataChanges.get());

        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, baselineTheme);
        M3ThemeRuntime.copyThemeContext(root, popupRoot);

        assertTrue(popupRoot.getStyleClass().contains("popup-root"));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(popupRoot.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(popupRoot.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertSame(baselineTheme, M3ThemeManager.getTheme(popupRoot));
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
    void exposesGeneratedThemeStylesheetUrl() {
        M3Theme theme = M3Theme.defaultTheme();

        String stylesheet = M3ThemeRuntime.themeStylesheetUrl(theme);
        String repeatedStylesheet = M3ThemeRuntime.themeStylesheetUrl(theme);

        assertEquals(stylesheet, repeatedStylesheet);
        assertTrue(stylesheet.startsWith("data:") || stylesheet.startsWith("m3fx-css:"));
        assertFalse(stylesheet.startsWith("file:"));
    }

    /// Verifies that generated theme stylesheets can be installed independently.
    @Test
    void installsGeneratedThemeStylesheetIndependently() {
        Pane root = new Pane();
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        M3ThemeRuntime.installThemeStylesheet(scene, theme);
        M3ThemeRuntime.installThemeStylesheet(scene, theme);

        assertEquals(1, scene.getStylesheets().size());
        assertEquals(M3ThemeRuntime.themeStylesheetUrl(theme), scene.getStylesheets().get(0));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertNull(M3ThemeManager.getTheme(root));

        M3ThemeRuntime.uninstallThemeStylesheet(scene);
        M3ThemeRuntime.uninstallThemeStylesheet(scene);

        assertEquals(0, scene.getStylesheets().size());
    }

    /// Verifies that a local theme owns only its generated stylesheet and preserves application stylesheet order.
    @Test
    void managesGeneratedStylesheetForLocalThemeScope() {
        Pane root = new Pane();
        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT
        );
        String applicationStylesheet = "file:/application.css";
        root.getStylesheets().add(applicationStylesheet);

        M3ThemeManager.install(root, baselineTheme);

        String baselineStylesheet = M3ThemeRuntime.themeStylesheetUrl(baselineTheme);
        assertEquals(2, root.getStylesheets().size());
        assertEquals(baselineStylesheet, root.getStylesheets().get(0));
        assertEquals(applicationStylesheet, root.getStylesheets().get(1));

        M3ThemeManager.install(root, expressiveTheme);

        String expressiveStylesheet = M3ThemeRuntime.themeStylesheetUrl(expressiveTheme);
        assertEquals(2, root.getStylesheets().size());
        assertEquals(expressiveStylesheet, root.getStylesheets().get(0));
        assertEquals(applicationStylesheet, root.getStylesheets().get(1));
        assertFalse(root.getStylesheets().contains(baselineStylesheet));

        M3ThemeManager.uninstall(root);

        assertEquals(1, root.getStylesheets().size());
        assertEquals(applicationStylesheet, root.getStylesheets().get(0));
        assertNull(M3ThemeManager.getTheme(root));
    }

    /// Verifies that uninstalling a local theme does not remove a matching application-owned stylesheet.
    @Test
    void preservesPreexistingLocalThemeStylesheet() {
        Pane root = new Pane();
        M3Theme theme = M3Theme.defaultTheme();
        String stylesheet = M3ThemeRuntime.themeStylesheetUrl(theme);
        root.getStylesheets().add(stylesheet);

        M3ThemeManager.install(root, theme);
        M3ThemeManager.uninstall(root);

        assertEquals(1, root.getStylesheets().size());
        assertEquals(stylesheet, root.getStylesheets().get(0));
    }

    /// Verifies that a local theme restores semantic classes and metadata that it did not own.
    @Test
    void restoresPreexistingLocalThemeContext() {
        Pane source = new Pane();
        Pane root = new Pane();
        M3Theme previousTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK
        );
        M3Theme replacementTheme = M3Theme.defaultTheme();
        M3ThemeManager.install(source, previousTheme);
        M3ThemeRuntime.copyThemeContext(source, root);

        M3ThemeManager.install(root, replacementTheme);

        assertSame(replacementTheme, M3ThemeManager.getTheme(root));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));

        M3ThemeManager.uninstall(root);

        assertSame(previousTheme, M3ThemeManager.getTheme(root));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.EXPRESSIVE_PROFILE_STYLE_CLASS));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.DARK_BRIGHTNESS_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.BASELINE_PROFILE_STYLE_CLASS));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.LIGHT_BRIGHTNESS_STYLE_CLASS));

        M3ThemeManager.uninstall(source);
    }

    /// Verifies that a scene installation temporarily overrides and then restores a local root installation.
    @Test
    void restoresLocalRootThemeAfterSceneThemeUninstall() {
        Pane root = new Pane();
        M3Theme localTheme = M3Theme.defaultTheme();
        M3Theme sceneTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK
        );
        M3ThemeManager.install(root, localTheme);
        String localStylesheet = M3ThemeRuntime.themeStylesheetUrl(localTheme);
        Scene scene = new Scene(root);

        M3ThemeManager.install(scene, sceneTheme);

        assertSame(sceneTheme, M3ThemeManager.getTheme(root));
        assertFalse(root.getStylesheets().contains(localStylesheet));

        M3ThemeManager.uninstall(scene);

        assertSame(localTheme, M3ThemeManager.getTheme(root));
        assertEquals(localStylesheet, root.getStylesheets().get(0));

        M3ThemeManager.uninstall(root);
    }

    /// Verifies that scene ownership coordinates local theme updates on the same root.
    @Test
    void sceneThemeCoordinatesLocalThemeMutationOnManagedRoot() {
        Pane root = new Pane();
        Scene scene = new Scene(root);
        M3Theme sceneTheme = M3Theme.defaultTheme();
        M3Theme localTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK
        );
        M3ThemeManager.install(scene, sceneTheme);

        M3ThemeManager.install(root, localTheme);
        assertSame(sceneTheme, M3ThemeManager.getTheme(root));

        M3ThemeManager.uninstall(scene);
        assertSame(localTheme, M3ThemeManager.getTheme(root));

        M3ThemeManager.install(scene, sceneTheme);
        M3ThemeManager.uninstall(root);
        assertSame(sceneTheme, M3ThemeManager.getTheme(root));

        M3ThemeManager.uninstall(scene);
        assertNull(M3ThemeManager.getTheme(root));
    }

    /// Verifies that nearest local component tokens win in both profile directions.
    @Test
    void localThemeComponentTokensOverrideSceneProfile() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Button");
            button.setSize(M3ButtonSize.SMALL);
            Pane localRoot = new Pane(button);
            Pane sceneRoot = new Pane(localRoot);
            Scene scene = new Scene(sceneRoot);
            M3Theme baselineTheme = M3Theme.defaultTheme();
            M3Theme expressiveTheme = M3Theme.fromSeed(
                    Color.web("#6750a4"),
                    M3Profile.EXPRESSIVE_2025,
                    Brightness.LIGHT
            );

            M3ThemeManager.install(scene, baselineTheme);
            M3ThemeManager.install(localRoot, expressiveTheme);
            button.arm();
            sceneRoot.applyCss();

            assertEquals(
                    expressiveTheme.tokens().componentTokens().buttonSizing().small()
                            .pressedRoundContainerShape(),
                    button.getContainerShape(),
                    0.0001
            );

            M3ThemeManager.install(scene, expressiveTheme);
            M3ThemeManager.install(localRoot, baselineTheme);
            sceneRoot.applyCss();

            assertEquals(
                    baselineTheme.tokens().componentTokens().buttonSizing().small()
                            .pressedRoundContainerShape(),
                    button.getContainerShape(),
                    0.0001
            );

            M3ThemeManager.uninstall(localRoot);
            sceneRoot.applyCss();

            assertEquals(
                    expressiveTheme.tokens().componentTokens().buttonSizing().small()
                            .pressedRoundContainerShape(),
                    button.getContainerShape(),
                    0.0001
            );

            button.disarm();
            M3ThemeManager.uninstall(scene);
        });
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

        assertTrue(scene.getStylesheets().get(0).endsWith("/styles/fallback.css"));
        assertEquals(M3ThemeRuntime.stylesheetUrl(), scene.getStylesheets().get(1));
        assertEquals(M3ThemeRuntime.themeStylesheetUrl(theme), scene.getStylesheets().get(2));
        assertEquals(applicationStylesheet, scene.getStylesheets().get(3));
        assertEquals(320.0, navigationDrawer.getMinWidth(), 0.0001);
        assertEquals(320.0, navigationDrawer.getPrefWidth(), 0.0001);
        assertEquals(320.0, navigationDrawer.getMaxWidth(), 0.0001);
    }

    /// Verifies that scene and local installations leave generated token declarations in the CSS cascade.
    @Test
    void applicationCssOverridesSceneAndLocalThemeTokens() throws Exception {
        String applicationStylesheet = temporaryStylesheet("""
                .m3-root.test-scene-token-override {
                    -m3-color-primary: #010203;
                    -fx-background-color: -m3-color-primary;
                }

                .m3-root.test-local-token-override {
                    -m3-color-primary: #040506;
                    -fx-background-color: -m3-color-primary;
                }
                """);

        FxTestUtils.runOnFxThread(() -> {
            Pane localRoot = new Pane();
            localRoot.getStyleClass().add("test-local-token-override");
            localRoot.getStylesheets().add(applicationStylesheet);
            Pane sceneRoot = new Pane(localRoot);
            sceneRoot.getStyleClass().add("test-scene-token-override");
            Scene scene = new Scene(sceneRoot);
            scene.getStylesheets().add(applicationStylesheet);
            M3Theme theme = M3Theme.defaultTheme();

            M3ThemeManager.install(scene, theme);
            M3ThemeManager.install(localRoot, theme);
            sceneRoot.applyCss();

            assertEquals(
                    Color.web("#010203"),
                    sceneRoot.getBackground().getFills().get(0).getFill()
            );
            assertEquals(
                    Color.web("#040506"),
                    localRoot.getBackground().getFills().get(0).getFill()
            );
            assertTrue(sceneRoot.getStyle().isEmpty());
            assertTrue(localRoot.getStyle().isEmpty());

            M3ThemeManager.uninstall(localRoot);
            M3ThemeManager.uninstall(scene);
        });
    }

    /// Verifies that transient item style classes do not allocate hidden scene infrastructure on application nodes.
    @Test
    void carouselItemStylingDoesNotAllocateNodeProperties() {
        M3Carousel carousel = new M3Carousel();
        Pane firstItem = new Pane();
        Pane secondItem = new Pane();

        assertFalse(firstItem.hasProperties());
        assertFalse(secondItem.hasProperties());

        carousel.getItems().addAll(firstItem, secondItem);
        carousel.setSelectedIndex(1);

        assertFalse(firstItem.hasProperties());
        assertFalse(secondItem.hasProperties());

        carousel.getItems().remove(firstItem);
        carousel.clearSelection();

        assertFalse(firstItem.hasProperties());
        assertFalse(secondItem.hasProperties());
    }

    /// Verifies that standalone non-Control components still install fallback tokens for their scene.
    @Test
    void standaloneRegionComponentInstallsFallbackStylesheet() {
        M3Scrim scrim = new M3Scrim();
        Pane root = new Pane(scrim);
        Scene scene = new Scene(root);

        root.applyCss();
        root.layout();

        assertTrue(scene.getStylesheets().get(0).endsWith("/styles/fallback.css"));
        assertTrue(root.getStyleClass().contains("root"));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertNull(M3ThemeManager.getTheme(root));
    }

    /// Verifies that standalone controls install fallback token styles on a matching scene root.
    @Test
    void standaloneControlFallbackStylesheetAppliesToSceneRoot() {
        M3ListItem listItem = new M3ListItem("List item");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root);

        root.applyCss();
        root.layout();

        assertTrue(scene.getStylesheets().get(0).endsWith("/styles/fallback.css"));
        assertTrue(root.getStyleClass().contains("root"));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertNull(M3ThemeManager.getTheme(root));

        root.getChildren().clear();
        Pane replacementRoot = new Pane(listItem);
        scene.setRoot(replacementRoot);
        replacementRoot.applyCss();
        replacementRoot.layout();

        assertTrue(replacementRoot.getStyleClass().contains("root"));
        assertEquals(1L, scene.getStylesheets().stream()
                .filter(stylesheet -> stylesheet.endsWith("/styles/fallback.css"))
                .count());
    }

    /// Verifies that installing and uninstalling a theme preserves standalone fallback root styling.
    @Test
    void themeInstallationPreservesStandaloneFallbackRootStyleClass() {
        M3ListItem listItem = new M3ListItem("List item");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        root.applyCss();
        M3ThemeManager.install(scene, theme);

        assertTrue(scene.getStylesheets().get(0).endsWith("/styles/fallback.css"));
        assertTrue(root.getStyleClass().contains("root"));
        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));

        M3ThemeManager.uninstall(scene);

        assertTrue(root.getStyleClass().contains("root"));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertNull(M3ThemeManager.getTheme(root));
        assertTrue(scene.getStylesheets().get(0).endsWith("/styles/fallback.css"));
    }

    /// Verifies that application styles can pin icon button metrics across theme reinstallations.
    @Test
    void preservesApplicationIconButtonMetricsAcrossThemeReinstall() throws Exception {
        M3IconButton button = new M3IconButton();
        button.getStyleClass().add("test-seed-button");
        Pane root = new Pane(button);
        Scene scene = new Scene(root);
        String applicationStylesheet = temporaryStylesheet("""
                .m3-button-base.m3-icon-button.test-seed-button {
                    -m3-container-height: 32px;
                    -m3-container-width: 32px;
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

        assertTrue(scene.getStylesheets().get(0).endsWith("/styles/fallback.css"));
        assertEquals(M3ThemeRuntime.stylesheetUrl(), scene.getStylesheets().get(1));
        assertEquals(M3ThemeRuntime.themeStylesheetUrl(M3Theme.fromSeed(Color.web("#006a6a"))), scene.getStylesheets().get(2));
        assertEquals(applicationStylesheet, scene.getStylesheets().get(3));
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
        int initialPropertyCount = scene.getProperties().size();

        M3ThemeManager.install(scene, theme);
        assertEquals(2, scene.getStylesheets().size());
        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertEquals("-fx-padding: 4px;", root.getStyle());

        M3ThemeManager.uninstall(scene);
        M3ThemeManager.uninstall(scene);

        assertTrue(root.getStyleClass().contains("app-root"));
        assertFalse(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertNull(M3ThemeManager.getTheme(root));
        assertEquals("-fx-padding: 4px;", root.getStyle());
        assertFalse(scene.getStylesheets().contains(M3ThemeRuntime.stylesheetUrl()));
        assertEquals(0, scene.getStylesheets().size());
        assertEquals(initialPropertyCount, scene.getProperties().size());
    }

    /// Verifies that generated component stylesheets apply theme tokens to controls.
    @Test
    void installsGeneratedComponentStylesheet() {
        M3Button button = new M3Button("Button");
        M3Button groupedButton = new M3Button("Grouped");
        M3ButtonGroup buttonGroup = buttonGroup(groupedButton);
        M3ButtonGroup standardButtonGroup = buttonGroup(new M3Button("Standard A"), new M3Button("Standard B"));
        standardButtonGroup.setVariant(M3ButtonGroupVariant.STANDARD);
        standardButtonGroup.setSize(M3ButtonSize.MEDIUM);
        M3IconToggleButtonGroup iconToggleButtonGroup =
                iconToggleButtonGroup(new M3IconToggleButton("A"), new M3IconToggleButton("B"));
        M3SegmentedButtonGroup segmentedButtonGroup =
                segmentedButtonGroup(new M3SegmentedButton("A"), new M3SegmentedButton("B"));
        M3SplitButton splitButton = new M3SplitButton("Split");
        splitButton.getItems().add(new M3MenuItem("Action"));
        M3FabMenu fabMenu = new M3FabMenu(new M3FloatingActionButton(new M3Icon("A")));
        M3TextField textField = new M3TextField();
        M3TextArea textArea = new M3TextArea();
        M3CheckBox checkBox = new M3CheckBox("Check");
        M3RadioButton radioButton = new M3RadioButton("Radio");
        M3Switch switchControl = new M3Switch("Switch");
        M3Slider slider = new M3Slider(0.0, 100.0, 50.0);
        M3Menu menu = new M3Menu(
                new M3MenuItem("Open"),
                new M3MenuItem("Save"),
                new M3MenuItem("Close")
        );
        M3SearchBar searchBar = new M3SearchBar();
        M3SearchView searchView = new M3SearchView();
        searchView.getResults().add(new M3ListItem("Result"));
        M3DatePicker datePicker = new M3DatePicker(LocalDate.of(2026, 5, 18));
        M3TimePicker timePicker = new M3TimePicker(LocalTime.of(10, 30));
        M3DatePickerField datePickerField = new M3DatePickerField(LocalDate.of(2026, 5, 18));
        M3FormPane formPane = new M3FormPane();
        M3FormSection formSection = formSection("Account", new M3TextField());
        M3FormRow formRow = new M3FormRow("Name", new M3TextField());
        M3TextInputLayout invalidInput = new M3TextInputLayout(new M3TextField(), "Project", "Required");
        invalidInput.setValidator(M3TextInputValidators.required("Project is required"));
        M3FormValidator validationValidator = new M3FormValidator(invalidInput);
        M3ValidationSummary validationSummary = new M3ValidationSummary(validationValidator);
        validationSummary.setShowWhenValid(true);
        validationValidator.validate();
        M3ListSectionHeader listSectionHeader = new M3ListSectionHeader("Results");
        M3SideSheet sideSheet = new M3SideSheet();
        M3BottomSheet bottomSheet = new M3BottomSheet();
        M3Card card = new M3Card();
        M3DialogPane dialogPane = new M3DialogPane();
        M3OverlayPane snackbarOverlay = new M3OverlayPane();
        M3Snackbar snackbar = new M3Snackbar("Saved");
        snackbar.setActionText("Undo");
        snackbar.setAction(() -> {
        });
        snackbarOverlay.showSnackbar(snackbar);
        M3Banner banner = new M3Banner("Message");
        M3TopAppBar topAppBar = new M3TopAppBar("Inbox");
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        M3Scrim scrim = new M3Scrim();
        M3Avatar avatar = new M3Avatar("A");
        M3Text displayText = new M3Text("Display", M3TextRole.DISPLAY_LARGE);
        M3Surface surface = new M3Surface();
        M3Button carouselFirst = new M3Button("First");
        M3Button carouselSecond = new M3Button("Second");
        M3Carousel carousel = carousel(carouselFirst, carouselSecond);
        carousel.select(carouselSecond);
        M3Icon icon = new M3Icon("I", M3IconSize.LARGE, M3IconVariant.PRIMARY);
        M3Chip chip = new M3AssistChip("Chip");
        M3ChipGroup chipGroup = chipGroup(new M3AssistChip("First"), new M3AssistChip("Second"));
        M3FloatingActionButton fab = new M3FloatingActionButton();
        fab.setSize(M3FloatingActionButtonSize.LARGE);
        M3SegmentedButton segmentedButton = new M3SegmentedButton("Week");
        M3Tab tab = new M3Tab("Overview");
        tab.setSelected(true);
        Pane root = new Pane(
                button,
                buttonGroup,
                standardButtonGroup,
                iconToggleButtonGroup,
                segmentedButtonGroup,
                splitButton,
                fabMenu,
                textField,
                textArea,
                checkBox,
                radioButton,
                switchControl,
                slider,
                menu,
                searchBar,
                searchView,
                datePicker,
                timePicker,
                datePickerField,
                formPane,
                formSection,
                formRow,
                validationSummary,
                listSectionHeader,
                sideSheet,
                bottomSheet,
                card,
                dialogPane,
                snackbarOverlay,
                banner,
                topAppBar,
                bottomAppBar,
                scrim,
                avatar,
                displayText,
                surface,
                carousel,
                icon,
                chip,
                chipGroup,
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
        root.layout();
        root.applyCss();
        Region snackbarSurface = assertInstanceOf(
                Region.class,
                snackbarOverlay.lookup(".m3-snackbar-container")
        );
        M3Button snackbarAction = assertInstanceOf(
                M3Button.class,
                snackbarOverlay.lookup(".m3-snackbar-action")
        );

        assertEquals(40.0, button.getContainerHeight(), 0.0001);
        assertEquals(16.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(M3ButtonGroupVariant.CONNECTED, buttonGroup.getVariant());
        assertEquals(M3ButtonSize.SMALL, buttonGroup.getSize());
        assertEquals(2.0, buttonGroup.getSpacing(), 0.0001);
        assertEquals(8.0, standardButtonGroup.getSpacing(), 0.0001);
        assertEquals(10.0, iconToggleButtonGroup.getSpacing(), 0.0001);
        assertEquals(-1.0, segmentedButtonGroup.getSpacing(), 0.0001);
        assertEquals(4.0, fabMenu.getActionSpacing(), 0.0001);
        assertEquals(8.0, fabMenu.getCloseSpacing(), 0.0001);
        assertEquals(16.0, groupedButton.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, splitButtonActionButton(splitButton).getHorizontalPadding(), 0.0001);
        assertEquals(48.0, splitButtonMenuButton(splitButton).getMinWidth(), 0.0001);
        assertEquals(48.0, splitButtonMenuButton(splitButton).getPrefWidth(), 0.0001);
        assertEquals(56.0, textField.getContainerHeight(), 0.0001);
        assertEquals(16.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(112.0, textArea.getContainerHeight(), 0.0001);
        assertEquals(16.0, textArea.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, textArea.getVerticalPadding(), 0.0001);
        assertEquals(48.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(40.0, checkBox.getStateLayerSize(), 0.0001);
        assertEquals(18.0, checkBox.getContainerSize(), 0.0001);
        assertEquals(12.0, checkBox.getSelectedMarkWidth(), 0.0001);
        assertEquals(10.0, checkBox.getSelectedMarkHeight(), 0.0001);
        assertEquals(12.0, checkBox.getIndeterminateMarkWidth(), 0.0001);
        assertEquals(2.0, checkBox.getIndeterminateMarkHeight(), 0.0001);
        assertEquals(48.0, radioButton.getTouchTargetSize(), 0.0001);
        assertEquals(40.0, radioButton.getStateLayerSize(), 0.0001);
        assertEquals(20.0, radioButton.getContainerSize(), 0.0001);
        assertEquals(10.0, radioButton.getSelectedDotSize(), 0.0001);
        assertEquals(48.0, switchControl.getTouchTargetSize(), 0.0001);
        assertEquals(52.0, switchControl.getTrackWidth(), 0.0001);
        assertEquals(32.0, switchControl.getTrackHeight(), 0.0001);
        assertEquals(40.0, switchControl.getStateLayerSize(), 0.0001);
        assertEquals(16.0, switchControl.getUnselectedHandleSize(), 0.0001);
        assertEquals(24.0, switchControl.getSelectedHandleSize(), 0.0001);
        assertEquals(28.0, switchControl.getPressedHandleSize(), 0.0001);
        assertEquals(16.0, slider.getTrackThickness(), 0.0001);
        assertEquals(44.0, slider.getThumbSize(), 0.0001);
        assertEquals(4.0, slider.getThumbWidth(), 0.0001);
        assertEquals(48.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(16.0, ((Region) slider.lookup(".track")).prefHeight(-1.0), 0.0001);
        M3MenuItem firstMenuItem = (M3MenuItem) menu.getItems().get(0);
        M3MenuItem middleMenuItem = (M3MenuItem) menu.getItems().get(1);
        M3MenuItem lastMenuItem = (M3MenuItem) menu.getItems().get(2);
        assertEquals(44.0, firstMenuItem.getOneLineHeight(), 0.0001);
        assertEquals(2.0, menu.getPadding().getTop(), 0.0001);
        assertEquals(16.0, firstMenuItem.getContainerShape(), 0.0001);
        assertEquals(6.0, firstMenuItem.getInnerCornerShape(), 0.0001);
        assertEquals(6.0, middleMenuItem.getContainerShape(), 0.0001);
        assertEquals(6.0, middleMenuItem.getInnerCornerShape(), 0.0001);
        assertEquals(16.0, lastMenuItem.getContainerShape(), 0.0001);
        assertEquals(6.0, lastMenuItem.getInnerCornerShape(), 0.0001);
        assertEquals(16.0, firstMenuItem.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, firstMenuItem.getContentSpacing(), 0.0001);
        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.select(middleMenuItem);
        root.applyCss();
        assertEquals(16.0, middleMenuItem.getContainerShape(), 0.0001);
        assertEquals(56.0, searchBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, searchBar.getPadding().getLeft(), 0.0001);
        assertEquals(0.0, ((HBox) searchBar.lookup("." + M3SearchBar.TRAILING_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(12.0, searchView.getPadding().getLeft(), 0.0001);
        assertEquals(12.0, searchView.getPadding().getRight(), 0.0001);
        M3ListItem themedSearchResult = (M3ListItem) searchView.getResults().get(0);
        assertEquals(64.0, themedSearchResult.getOneLineHeight(), 0.0001);
        assertEquals(0.0, themedSearchResult.getContainerShape(), 0.0001);
        assertEquals(20.0, themedSearchResult.getHorizontalPadding(), 0.0001);
        assertEquals(0.0, ((Region) datePicker.lookup("." + M3DatePicker.CONTAINER_STYLE_CLASS)).getPadding().getTop(), 0.0001);
        assertEquals(40.0, datePicker.lookup("." + M3DatePicker.NAVIGATION_BUTTON_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(48.0, datePicker.lookup("." + M3DatePicker.DAY_CELL_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(48.0, datePicker.lookup("." + M3DatePicker.DAY_CELL_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(24.0, ((Region) timePicker.lookup("." + M3TimePicker.CONTAINER_STYLE_CLASS)).getPadding().getTop(), 0.0001);
        assertEquals(96.0, timePicker.lookup("." + M3TimePicker.HOUR_DISPLAY_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(80.0, timePicker.lookup("." + M3TimePicker.MINUTE_DISPLAY_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(256.0, timePicker.lookup("." + M3TimePicker.DIAL_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(256.0, timePicker.lookup("." + M3TimePicker.DIAL_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(52.0, timePicker.lookup("." + M3TimePicker.PERIOD_ROW_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(80.0, timePicker.lookup("." + M3TimePicker.PERIOD_ROW_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(40.0, datePickerField.lookup(".m3-picker-field-open-button").prefWidth(-1.0), 0.0001);
        assertEquals(40.0, datePickerField.lookup(".m3-picker-field-open-button").prefHeight(-1.0), 0.0001);
        assertEquals(16.0, formPane.getRowSpacing(), 0.0001);
        assertEquals(12.0, formSection.getContentSpacing(), 0.0001);
        assertEquals(180.0, formRow.getLabelWidth(), 0.0001);
        assertEquals(24.0, formRow.getColumnSpacing(), 0.0001);
        assertEquals(64.0, formRow.getRowMinHeight(), 0.0001);
        assertEquals(16.0, validationSummary.getPadding().getTop(), 0.0001);
        assertEquals(
                4.0,
                ((javafx.scene.layout.VBox) validationSummary.lookup(
                        "." + M3ValidationSummary.ITEMS_STYLE_CLASS
                )).getSpacing(),
                0.0001
        );
        assertEquals(
                8.0,
                ((Region) validationSummary.lookup("." + M3ValidationSummary.ITEM_STYLE_CLASS)).getPadding().getTop(),
                0.0001
        );
        assertEquals(
                10.0,
                ((Region) validationSummary.lookup("." + M3ValidationSummary.ITEM_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(56.0, listSectionHeader.prefHeight(-1.0), 0.0001);
        assertEquals(20.0, listSectionHeader.getPadding().getLeft(), 0.0001);
        assertEquals(256.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(400.0, sideSheet.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(640.0, bottomSheet.getMaxWidth(), 0.0001);
        assertEquals(
                24.0,
                ((Region) sideSheet.lookup("." + M3SideSheet.HEADER_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                ((Region) sideSheet.lookup("." + M3SideSheet.CONTENT_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                ((Region) bottomSheet.lookup("." + M3BottomSheet.HEADER_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                ((Region) bottomSheet.lookup("." + M3BottomSheet.CONTENT_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(32.0, ((Region) bottomSheet.lookup("." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS)).prefWidth(-1.0), 0.0001);
        assertEquals(4.0, ((Region) bottomSheet.lookup("." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS)).prefHeight(-1.0), 0.0001);
        assertEquals(16.0, card.getContainerShape(), 0.0001);
        assertEquals(16.0, card.getContentPadding(), 0.0001);
        assertEquals(32.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(24.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(280.0, dialogPane.getContainerMinWidth(), 0.0001);
        assertEquals(560.0, dialogPane.getContainerMaxWidth(), 0.0001);
        assertEquals(304.0, dialogPane.getMinWidth(), 0.0001);
        assertEquals(584.0, dialogPane.getMaxWidth(), 0.0001);
        assertEquals(36.0, dialogPane.getPadding().getTop(), 0.0001);
        assertEquals(16.0, snackbarSurface.getPadding().getLeft(), 0.0001);
        assertEquals(
                6.0,
                snackbarSurface.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(),
                0.0001
        );
        assertEquals(32.0, snackbarAction.getContainerHeight(), 0.0001);
        assertEquals(80.0, banner.getMinHeight(), 0.0001);
        assertEquals(24.0, banner.getPadding().getLeft(), 0.0001);
        assertEquals(64.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Insets.EMPTY, topAppBar.getPadding());
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(112.0, topAppBar.getPrefHeight(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(152.0, topAppBar.getPrefHeight(), 0.0001);
        topAppBar.setVariant(M3TopAppBarVariant.SMALL);
        root.applyCss();
        assertEquals(80.0, bottomAppBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(0.32, scrim.getOpacity(), 0.0001);
        assertEquals(40.0, avatar.getContainerSize(), 0.0001);
        assertEquals(64.0, displayText.getTypographyFontSize(), 0.0001);
        assertEquals(72.0, displayText.getTypographyLineHeight(), 0.0001);
        assertEquals(16.0, surface.getContainerShape(), 0.0001);
        assertEquals(16.0, surface.getContentPadding(), 0.0001);
        assertEquals(8.0, ((Region) carousel.lookup("." + M3Carousel.TRACK_STYLE_CLASS)).getPadding().getTop(), 0.0001);
        assertEquals(16.0, ((Region) carousel.lookup("." + M3Carousel.TRACK_STYLE_CLASS)).getPadding().getLeft(), 0.0001);
        assertEquals(8.0, ((javafx.scene.layout.HBox) carousel.lookup("." + M3Carousel.TRACK_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(1.0, carouselFirst.getOpacity(), 0.0001);
        assertEquals(1.0, carouselSecond.getOpacity(), 0.0001);
        assertNull(carouselSecond.getEffect());
        assertEquals(36.0, icon.getIconSize(), 0.0001);
        assertEquals(32.0, chip.getContainerHeight(), 0.0001);
        assertEquals(16.0, chip.getHorizontalPadding(), 0.0001);
        assertEquals(8.0, chip.getIconHorizontalPadding(), 0.0001);
        assertEquals(8.0, chipGroup.getHorizontalGap(), 0.0001);
        assertEquals(8.0, chipGroup.getVerticalGap(), 0.0001);
        assertEquals(96.0, fab.getContainerSize(), 0.0001);
        assertEquals(28.0, fab.getHorizontalPadding(), 0.0001);
        assertEquals(40.0, segmentedButton.getContainerHeight(), 0.0001);
        assertEquals(12.0, segmentedButton.getHorizontalPadding(), 0.0001);
        assertEquals(48.0, tab.getContainerHeight(), 0.0001);
        assertEquals(16.0, tab.getHorizontalPadding(), 0.0001);
        assertEquals(3.0, tab.getActiveIndicatorHeight(), 0.0001);

        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, baselineTheme);
        root.applyCss();

        assertEquals(40.0, button.getContainerHeight(), 0.0001);
        assertEquals(24.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(2.0, buttonGroup.getSpacing(), 0.0001);
        assertEquals(8.0, iconToggleButtonGroup.getSpacing(), 0.0001);
        assertEquals(-1.0, segmentedButtonGroup.getSpacing(), 0.0001);
        assertEquals(4.0, fabMenu.getActionSpacing(), 0.0001);
        assertEquals(8.0, fabMenu.getCloseSpacing(), 0.0001);
        assertEquals(24.0, groupedButton.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, splitButtonActionButton(splitButton).getPadding().getLeft(), 0.0001);
        assertEquals(12.0, splitButtonActionButton(splitButton).getPadding().getRight(), 0.0001);
        assertEquals(48.0, splitButtonMenuButton(splitButton).getMinWidth(), 0.0001);
        assertEquals(48.0, splitButtonMenuButton(splitButton).getPrefWidth(), 0.0001);
        assertEquals(56.0, textField.getContainerHeight(), 0.0001);
        assertEquals(16.0, textField.getHorizontalPadding(), 0.0001);
        assertEquals(112.0, textArea.getContainerHeight(), 0.0001);
        assertEquals(16.0, textArea.getHorizontalPadding(), 0.0001);
        assertEquals(16.0, textArea.getVerticalPadding(), 0.0001);
        assertEquals(48.0, checkBox.getTouchTargetSize(), 0.0001);
        assertEquals(40.0, checkBox.getStateLayerSize(), 0.0001);
        assertEquals(18.0, checkBox.getContainerSize(), 0.0001);
        assertEquals(12.0, checkBox.getSelectedMarkWidth(), 0.0001);
        assertEquals(10.0, checkBox.getSelectedMarkHeight(), 0.0001);
        assertEquals(12.0, checkBox.getIndeterminateMarkWidth(), 0.0001);
        assertEquals(2.0, checkBox.getIndeterminateMarkHeight(), 0.0001);
        assertEquals(48.0, radioButton.getTouchTargetSize(), 0.0001);
        assertEquals(40.0, radioButton.getStateLayerSize(), 0.0001);
        assertEquals(20.0, radioButton.getContainerSize(), 0.0001);
        assertEquals(10.0, radioButton.getSelectedDotSize(), 0.0001);
        assertEquals(48.0, switchControl.getTouchTargetSize(), 0.0001);
        assertEquals(52.0, switchControl.getTrackWidth(), 0.0001);
        assertEquals(32.0, switchControl.getTrackHeight(), 0.0001);
        assertEquals(40.0, switchControl.getStateLayerSize(), 0.0001);
        assertEquals(16.0, switchControl.getUnselectedHandleSize(), 0.0001);
        assertEquals(24.0, switchControl.getSelectedHandleSize(), 0.0001);
        assertEquals(28.0, switchControl.getPressedHandleSize(), 0.0001);
        assertEquals(16.0, slider.getTrackThickness(), 0.0001);
        assertEquals(44.0, slider.getThumbSize(), 0.0001);
        assertEquals(4.0, slider.getThumbWidth(), 0.0001);
        assertEquals(48.0, slider.getTouchTargetSize(), 0.0001);
        assertEquals(16.0, ((Region) slider.lookup(".track")).prefHeight(-1.0), 0.0001);
        assertEquals(48.0, ((M3MenuItem) menu.getItems().get(0)).getOneLineHeight(), 0.0001);
        assertEquals(56.0, searchBar.getPrefHeight(), 0.0001);
        assertEquals(0.0, ((HBox) searchBar.lookup("." + M3SearchBar.TRAILING_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(56.0, ((M3ListItem) searchView.getResults().get(0)).getOneLineHeight(), 0.0001);
        assertEquals(0.0, ((M3ListItem) searchView.getResults().get(0)).getContainerShape(), 0.0001);
        assertEquals(0.0, ((Region) datePicker.lookup("." + M3DatePicker.CONTAINER_STYLE_CLASS)).getPadding().getTop(), 0.0001);
        assertEquals(40.0, datePicker.lookup("." + M3DatePicker.NAVIGATION_BUTTON_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(48.0, datePicker.lookup("." + M3DatePicker.DAY_CELL_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(48.0, datePicker.lookup("." + M3DatePicker.DAY_CELL_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(24.0, ((Region) timePicker.lookup("." + M3TimePicker.CONTAINER_STYLE_CLASS)).getPadding().getTop(), 0.0001);
        assertEquals(96.0, timePicker.lookup("." + M3TimePicker.HOUR_DISPLAY_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(80.0, timePicker.lookup("." + M3TimePicker.MINUTE_DISPLAY_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(256.0, timePicker.lookup("." + M3TimePicker.DIAL_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(256.0, timePicker.lookup("." + M3TimePicker.DIAL_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(52.0, timePicker.lookup("." + M3TimePicker.PERIOD_ROW_STYLE_CLASS).prefWidth(-1.0), 0.0001);
        assertEquals(80.0, timePicker.lookup("." + M3TimePicker.PERIOD_ROW_STYLE_CLASS).prefHeight(-1.0), 0.0001);
        assertEquals(40.0, datePickerField.lookup(".m3-picker-field-open-button").prefWidth(-1.0), 0.0001);
        assertEquals(40.0, datePickerField.lookup(".m3-picker-field-open-button").prefHeight(-1.0), 0.0001);
        assertEquals(16.0, formPane.getRowSpacing(), 0.0001);
        assertEquals(12.0, formSection.getContentSpacing(), 0.0001);
        assertEquals(180.0, formRow.getLabelWidth(), 0.0001);
        assertEquals(24.0, formRow.getColumnSpacing(), 0.0001);
        assertEquals(64.0, formRow.getRowMinHeight(), 0.0001);
        assertEquals(16.0, validationSummary.getPadding().getTop(), 0.0001);
        assertEquals(
                4.0,
                ((javafx.scene.layout.VBox) validationSummary.lookup(
                        "." + M3ValidationSummary.ITEMS_STYLE_CLASS
                )).getSpacing(),
                0.0001
        );
        assertEquals(
                8.0,
                ((Region) validationSummary.lookup("." + M3ValidationSummary.ITEM_STYLE_CLASS)).getPadding().getTop(),
                0.0001
        );
        assertEquals(
                10.0,
                ((Region) validationSummary.lookup("." + M3ValidationSummary.ITEM_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(48.0, listSectionHeader.prefHeight(-1.0), 0.0001);
        assertEquals(16.0, listSectionHeader.getPadding().getLeft(), 0.0001);
        assertEquals(256.0, sideSheet.getPrefWidth(), 0.0001);
        assertEquals(400.0, sideSheet.getMaxWidth(), 0.0001);
        assertEquals(Region.USE_COMPUTED_SIZE, bottomSheet.getPrefHeight(), 0.0001);
        assertEquals(640.0, bottomSheet.getMaxWidth(), 0.0001);
        assertEquals(
                24.0,
                ((Region) sideSheet.lookup("." + M3SideSheet.HEADER_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                ((Region) sideSheet.lookup("." + M3SideSheet.CONTENT_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                ((Region) bottomSheet.lookup("." + M3BottomSheet.HEADER_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(
                24.0,
                ((Region) bottomSheet.lookup("." + M3BottomSheet.CONTENT_STYLE_CLASS)).getPadding().getLeft(),
                0.0001
        );
        assertEquals(32.0, ((Region) bottomSheet.lookup("." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS)).prefWidth(-1.0), 0.0001);
        assertEquals(4.0, ((Region) bottomSheet.lookup("." + M3BottomSheet.DRAG_HANDLE_STYLE_CLASS)).prefHeight(-1.0), 0.0001);
        assertEquals(12.0, card.getContainerShape(), 0.0001);
        assertEquals(16.0, card.getContentPadding(), 0.0001);
        assertEquals(28.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(24.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(280.0, dialogPane.getContainerMinWidth(), 0.0001);
        assertEquals(560.0, dialogPane.getContainerMaxWidth(), 0.0001);
        assertEquals(16.0, snackbarSurface.getPadding().getLeft(), 0.0001);
        assertEquals(
                4.0,
                snackbarSurface.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius(),
                0.0001
        );
        assertEquals(32.0, snackbarAction.getContainerHeight(), 0.0001);
        assertEquals(80.0, banner.getMinHeight(), 0.0001);
        assertEquals(24.0, banner.getPadding().getLeft(), 0.0001);
        assertEquals(64.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Insets.EMPTY, topAppBar.getPadding());
        topAppBar.setVariant(M3TopAppBarVariant.MEDIUM);
        root.applyCss();
        assertEquals(112.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Insets.EMPTY, topAppBar.getPadding());
        topAppBar.setVariant(M3TopAppBarVariant.LARGE);
        root.applyCss();
        assertEquals(152.0, topAppBar.getPrefHeight(), 0.0001);
        assertEquals(Insets.EMPTY, topAppBar.getPadding());
        assertEquals(80.0, bottomAppBar.getPrefHeight(), 0.0001);
        assertEquals(16.0, bottomAppBar.getPadding().getLeft(), 0.0001);
        assertEquals(0.32, scrim.getOpacity(), 0.0001);
        assertEquals(40.0, avatar.getContainerSize(), 0.0001);
        assertEquals(57.0, displayText.getTypographyFontSize(), 0.0001);
        assertEquals(64.0, displayText.getTypographyLineHeight(), 0.0001);
        assertEquals(12.0, surface.getContainerShape(), 0.0001);
        assertEquals(16.0, surface.getContentPadding(), 0.0001);
        assertEquals(8.0, ((Region) carousel.lookup("." + M3Carousel.TRACK_STYLE_CLASS)).getPadding().getTop(), 0.0001);
        assertEquals(16.0, ((Region) carousel.lookup("." + M3Carousel.TRACK_STYLE_CLASS)).getPadding().getLeft(), 0.0001);
        assertEquals(8.0, ((javafx.scene.layout.HBox) carousel.lookup("." + M3Carousel.TRACK_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(1.0, carouselFirst.getOpacity(), 0.0001);
        assertNull(carouselSecond.getEffect());
        assertEquals(32.0, icon.getIconSize(), 0.0001);
        assertEquals(32.0, chip.getContainerHeight(), 0.0001);
        assertEquals(16.0, chip.getHorizontalPadding(), 0.0001);
        assertEquals(8.0, chip.getIconHorizontalPadding(), 0.0001);
        assertEquals(8.0, chipGroup.getHorizontalGap(), 0.0001);
        assertEquals(8.0, chipGroup.getVerticalGap(), 0.0001);
        assertEquals(96.0, fab.getContainerSize(), 0.0001);
        assertEquals(28.0, fab.getHorizontalPadding(), 0.0001);
        assertEquals(40.0, segmentedButton.getContainerHeight(), 0.0001);
        assertEquals(12.0, segmentedButton.getHorizontalPadding(), 0.0001);
        assertEquals(48.0, tab.getContainerHeight(), 0.0001);
        assertEquals(16.0, tab.getHorizontalPadding(), 0.0001);
        assertEquals(3.0, tab.getActiveIndicatorHeight(), 0.0001);
        assertEquals(3, scene.getStylesheets().size());
    }

    /// Verifies that generated component stylesheets apply utility component tokens.
    @Test
    void generatedComponentStylesheetAppliesUtilityTokens() {
        M3Divider divider = new M3Divider();
        M3Badge badge = new M3Badge("12");
        M3ProgressBar progressBar = new M3ProgressBar(0.5);
        M3ProgressIndicator progressIndicator = new M3ProgressIndicator(0.5);
        M3LoadingIndicator loadingIndicator = new M3LoadingIndicator();
        Pane root = new Pane(divider, badge, progressBar, progressIndicator, loadingIndicator);
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
        assertEquals(6.0, badge.getSmallSize(), 0.0001);
        assertEquals(16.0, badge.getLargeHeight(), 0.0001);
        assertEquals(16.0, badge.getLargeMinWidth(), 0.0001);
        assertEquals(8.0, badge.getContainerShape(), 0.0001);
        assertEquals(4.0, progressBar.getTrackThickness(), 0.0001);
        assertEquals(0.0, progressBar.getWaveAmplitude(), 0.0001);
        assertEquals(40.0, progressBar.getWavelength(), 0.0001);
        assertEquals(20.0, progressBar.getIndeterminateWavelength(), 0.0001);
        assertEquals(4.0, progressBar.getTrackGap(), 0.0001);
        assertEquals(4.0, progressBar.getStopSize(), 0.0001);
        assertEquals(4.0, ((Rectangle) progressBar.lookup(".track")).getArcWidth(), 0.0001);
        assertEquals(4.0, ((Rectangle) progressBar.lookup(".bar")).getArcHeight(), 0.0001);
        assertEquals(4.0, progressIndicator.getTrackThickness(), 0.0001);
        assertEquals(40.0, progressIndicator.getIndicatorSize(), 0.0001);
        assertEquals(48.0, progressIndicator.getWaveIndicatorSize(), 0.0001);
        assertEquals(0.0, progressIndicator.getWaveAmplitude(), 0.0001);
        assertEquals(15.0, progressIndicator.getWavelength(), 0.0001);
        assertEquals(4.0, progressIndicator.getTrackGap(), 0.0001);
        assertEquals(48.0, loadingIndicator.getContainerSize(), 0.0001);
        assertEquals(38.0, loadingIndicator.getIndicatorSize(), 0.0001);
        assertEquals(48.0, loadingIndicator.getPrefWidth(), 0.0001);
    }

    /// Verifies that generated component stylesheets apply internal layout tokens.
    @Test
    void generatedComponentStylesheetAppliesInternalLayoutTokens() {
        M3Banner banner = new M3Banner("Message");
        banner.getActions().add(new M3Button("Action"));
        M3Button tooltipAction = new M3Button("Action");
        M3RichTooltip richTooltip = new M3RichTooltip("Title", "Supporting");
        richTooltip.getActions().add(tooltipAction);
        VBox richTooltipContainer = assertInstanceOf(VBox.class, richTooltip.getGraphic());
        M3NavigationItem barHome = new M3NavigationItem("Home", new M3Icon("H"));
        M3NavigationItem barSearch = new M3NavigationItem("Search", new M3Icon("S"));
        M3NavigationBar navigationBar = navigationBar(barHome, barSearch);
        M3NavigationItem railHome = new M3NavigationItem("Home", new M3Icon("H"));
        M3NavigationItem railSearch = new M3NavigationItem("Search", new M3Icon("S"));
        M3NavigationRail navigationRail = navigationRail(railHome, railSearch);
        M3ListItem drawerHome = new M3ListItem("Home");
        M3NavigationDrawerGroup drawerGroup = new M3NavigationDrawerGroup("Group");
        M3ListItem drawerChild = new M3ListItem("Child");
        drawerGroup.getItems().add(drawerChild);
        drawerGroup.setExpanded(true);
        M3NavigationDrawer navigationDrawer = navigationDrawer(drawerHome, drawerGroup);
        M3Toolbar toolbar = toolbar(new M3IconButton(new M3Icon("B")), new M3IconButton(new M3Icon("I")));
        Pane root = new Pane(banner, richTooltipContainer, navigationBar, navigationRail, navigationDrawer, toolbar);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(80.0, banner.getMinHeight(), 0.0001);
        assertEquals(16.0, banner.getPadding().getTop(), 0.0001);
        assertEquals(24.0, banner.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, ((HBox) banner.lookup("." + M3Banner.CONTAINER_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(8.0, ((HBox) banner.lookup("." + M3Banner.ACTIONS_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(12.0, richTooltipContainer.getPadding().getTop(), 0.0001);
        assertEquals(16.0, richTooltipContainer.getPadding().getLeft(), 0.0001);
        assertEquals(8.0, richTooltipContainer.getPadding().getBottom(), 0.0001);
        assertEquals(8.0, richTooltipContainer.getSpacing(), 0.0001);
        assertEquals(320.0, richTooltipContainer.getPrefWidth(), 0.0001);
        assertEquals(8.0, ((HBox) richTooltipContainer.lookup("." + M3RichTooltip.ACTIONS_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(32.0, tooltipAction.getContainerHeight(), 0.0001);
        assertEquals(12.0, tooltipAction.getHorizontalPadding(), 0.0001);
        assertEquals(64.0, navigationBar.getPrefHeight(), 0.0001);
        assertEquals(8.0, navigationBar.getPadding().getLeft(), 0.0001);
        assertEquals(64.0, barHome.getContainerHeight(), 0.0001);
        assertEquals(80.0, barHome.getItemWidth(), 0.0001);
        assertEquals(56.0, barHome.getIndicatorWidth(), 0.0001);
        assertEquals(32.0, barHome.getIndicatorHeight(), 0.0001);
        assertEquals(4.0, barHome.getContentSpacing(), 0.0001);
        assertEquals(96.0, navigationRail.getCollapsedContainerWidth(), 0.0001);
        assertEquals(44.0, navigationRail.getPadding().getTop(), 0.0001);
        assertEquals(0.0, navigationRail.getPadding().getLeft(), 0.0001);
        assertEquals(4.0, navigationRail.getItemSpacing(), 0.0001);
        assertEquals(64.0, railHome.getContainerHeight(), 0.0001);
        assertEquals(80.0, railHome.getItemWidth(), 0.0001);
        assertEquals(56.0, railHome.getIndicatorWidth(), 0.0001);
        assertEquals(32.0, railHome.getIndicatorHeight(), 0.0001);
        assertEquals(4.0, railHome.getContentSpacing(), 0.0001);

        navigationRail.setExpanded(true);
        root.applyCss();
        assertEquals(44.0, navigationRail.getPadding().getTop(), 0.0001);
        assertEquals(20.0, navigationRail.getPadding().getBottom(), 0.0001);
        assertEquals(0.0, navigationRail.getPadding().getLeft(), 0.0001);
        assertEquals(0.0, navigationRail.getItemSpacing(), 0.0001);
        assertEquals(64.0, railHome.getContainerHeight(), 0.0001);
        assertEquals(56.0, railHome.getIndicatorHeight(), 0.0001);
        assertEquals(8.0, railHome.getContentSpacing(), 0.0001);

        navigationRail.setExpanded(false);
        root.applyCss();
        assertEquals(360.0, navigationDrawer.getPrefWidth(), 0.0001);
        assertEquals(12.0, navigationDrawer.getPadding().getTop(), 0.0001);
        assertEquals(0.0, navigationDrawer.getItemSpacing(), 0.0001);
        assertEquals(56.0, drawerHome.getOneLineHeight(), 0.0001);
        assertEquals(16.0, drawerHome.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, drawerHome.getContentSpacing(), 0.0001);
        assertEquals(56.0, drawerChild.getOneLineHeight(), 0.0001);
        assertEquals(32.0, drawerChild.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, drawerChild.getContentSpacing(), 0.0001);
        assertEquals(64.0, toolbar.getContainerHeight(), 0.0001);
        assertEquals(64.0, toolbar.getContainerWidth(), 0.0001);
        assertEquals(48.0, toolbar.getItemSlotSize(), 0.0001);
        assertEquals(8.0, toolbar.getContentPadding(), 0.0001);
        assertEquals(4.0, toolbar.getItemSpacing(), 0.0001);

        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        root.applyCss();

        assertEquals(80.0, banner.getMinHeight(), 0.0001);
        assertEquals(16.0, banner.getPadding().getTop(), 0.0001);
        assertEquals(24.0, banner.getPadding().getLeft(), 0.0001);
        assertEquals(16.0, ((HBox) banner.lookup("." + M3Banner.CONTAINER_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(8.0, ((HBox) banner.lookup("." + M3Banner.ACTIONS_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(12.0, richTooltipContainer.getPadding().getTop(), 0.0001);
        assertEquals(16.0, richTooltipContainer.getPadding().getLeft(), 0.0001);
        assertEquals(8.0, richTooltipContainer.getPadding().getBottom(), 0.0001);
        assertEquals(8.0, richTooltipContainer.getSpacing(), 0.0001);
        assertEquals(320.0, richTooltipContainer.getPrefWidth(), 0.0001);
        assertEquals(8.0, ((HBox) richTooltipContainer.lookup("." + M3RichTooltip.ACTIONS_STYLE_CLASS)).getSpacing(), 0.0001);
        assertEquals(32.0, tooltipAction.getContainerHeight(), 0.0001);
        assertEquals(12.0, tooltipAction.getHorizontalPadding(), 0.0001);
        assertEquals(80.0, navigationBar.getPrefHeight(), 0.0001);
        assertEquals(8.0, navigationBar.getPadding().getLeft(), 0.0001);
        assertEquals(80.0, barHome.getContainerHeight(), 0.0001);
        assertEquals(80.0, barHome.getItemWidth(), 0.0001);
        assertEquals(64.0, barHome.getIndicatorWidth(), 0.0001);
        assertEquals(32.0, barHome.getIndicatorHeight(), 0.0001);
        assertEquals(4.0, barHome.getContentSpacing(), 0.0001);
        assertEquals(80.0, navigationRail.getCollapsedContainerWidth(), 0.0001);
        assertEquals(16.0, navigationRail.getPadding().getTop(), 0.0001);
        assertEquals(0.0, navigationRail.getPadding().getLeft(), 0.0001);
        assertEquals(8.0, navigationRail.getItemSpacing(), 0.0001);
        assertEquals(80.0, railHome.getContainerHeight(), 0.0001);
        assertEquals(80.0, railHome.getItemWidth(), 0.0001);
        assertEquals(56.0, railHome.getIndicatorWidth(), 0.0001);
        assertEquals(32.0, railHome.getIndicatorHeight(), 0.0001);
        assertEquals(4.0, railHome.getContentSpacing(), 0.0001);
        assertEquals(360.0, navigationDrawer.getPrefWidth(), 0.0001);
        assertEquals(12.0, navigationDrawer.getPadding().getTop(), 0.0001);
        assertEquals(0.0, navigationDrawer.getItemSpacing(), 0.0001);
        assertEquals(56.0, drawerHome.getOneLineHeight(), 0.0001);
        assertEquals(16.0, drawerHome.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, drawerHome.getContentSpacing(), 0.0001);
        assertEquals(56.0, drawerChild.getOneLineHeight(), 0.0001);
        assertEquals(32.0, drawerChild.getHorizontalPadding(), 0.0001);
        assertEquals(12.0, drawerChild.getContentSpacing(), 0.0001);
        assertEquals(64.0, toolbar.getContainerHeight(), 0.0001);
        assertEquals(64.0, toolbar.getContainerWidth(), 0.0001);
        assertEquals(48.0, toolbar.getItemSlotSize(), 0.0001);
        assertEquals(8.0, toolbar.getContentPadding(), 0.0001);
        assertEquals(4.0, toolbar.getItemSpacing(), 0.0001);
        assertEquals(32.0, toolbar.getDockedMaxItemSpacing(), 0.0001);
    }

    /// Verifies that generated component stylesheets apply list item tokens.
    @Test
    void generatedComponentStylesheetAppliesListItemTokens() {
        M3ListItem standardItem = new M3ListItem("Standard headline");
        M3ListItem segmentedItem = new M3ListItem("Segmented headline");
        M3ListPane segmentedList = new M3ListPane();
        segmentedList.setListStyle(M3ListStyle.SEGMENTED);
        segmentedList.getItems().add(segmentedItem);
        M3ListSectionHeader sectionHeader = new M3ListSectionHeader("Pinned");
        Pane root = new Pane(standardItem, segmentedList, sectionHeader);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(64.0, standardItem.getOneLineHeight(), 0.0001);
        assertEquals(80.0, standardItem.getTwoLineHeight(), 0.0001);
        assertEquals(96.0, standardItem.getThreeLineHeight(), 0.0001);
        assertEquals(0.0, standardItem.getContainerShape(), 0.0001);
        assertEquals(20.0, standardItem.getHorizontalPadding(), 0.0001);
        assertEquals(10.0, standardItem.getVerticalPadding(), 0.0001);
        assertEquals(20.0, standardItem.getContentSpacing(), 0.0001);
        assertEquals(2.0, segmentedList.getItemSpacing(), 0.0001);
        assertEquals(
                expressiveTheme.tokens().shapeTokens().extraSmall(),
                segmentedItem.getContainerShape(),
                0.0001
        );
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

        assertEquals(16.0, card.getContainerShape(), 0.0001);
        assertEquals(16.0, card.getContentPadding(), 0.0001);
        assertEquals(1.0, card.getOutlineWidth(), 0.0001);
        assertEquals(32.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(24.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(280.0, dialogPane.getContainerMinWidth(), 0.0001);
        assertEquals(560.0, dialogPane.getContainerMaxWidth(), 0.0001);
        assertEquals(304.0, dialogPane.getMinWidth(), 0.0001);
        assertEquals(584.0, dialogPane.getMaxWidth(), 0.0001);
        assertEquals(36.0, dialogPane.getPadding().getTop(), 0.0001);
    }

    /// Writes an application stylesheet for stylesheet cascade tests.
    private static String temporaryStylesheet(String content) throws Exception {
        Path path = Files.createTempFile("m3fx-test-", ".css");
        Files.writeString(path, content);
        path.toFile().deleteOnExit();
        return path.toUri().toString();
    }

    /// Returns the primary action part exposed by a split button.
    private static M3Button splitButtonActionButton(M3SplitButton splitButton) {
        return splitButtonPart(splitButton, M3Button.class, 0);
    }

    /// Returns the menu part exposed by a split button.
    private static M3MenuButton splitButtonMenuButton(M3SplitButton splitButton) {
        return splitButtonPart(splitButton, M3MenuButton.class, 1);
    }

    /// Returns one typed part exposed by a split button.
    private static <T extends Node> T splitButtonPart(M3SplitButton splitButton, Class<T> type, int index) {
        return assertInstanceOf(type, splitButton.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index));
    }

    /// Verifies that an icon button keeps the fixed swatch metrics from application CSS.
    private static void assertSeedButtonMetrics(M3IconButton button) {
        assertEquals(32.0, button.getContainerHeight(), 0.0001);
        assertEquals(32.0, button.getContainerWidth(), 0.0001);
        assertEquals(999.0, button.getContainerShape(), 0.0001);
        assertEquals(0.0, button.getHorizontalPadding(), 0.0001);
        assertEquals(32.0, button.getMinWidth(), 0.0001);
        assertEquals(32.0, button.getMinHeight(), 0.0001);
        assertEquals(32.0, button.getPrefWidth(), 0.0001);
        assertEquals(32.0, button.getPrefHeight(), 0.0001);
        assertEquals(32.0, button.getMaxWidth(), 0.0001);
        assertEquals(32.0, button.getMaxHeight(), 0.0001);
    }

}
