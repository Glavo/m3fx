// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.glavo.m3fx.internal.tokens.M3ComponentTokensImpl;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds component-level Material Design 3 tokens used by m3fx controls.
@NotNullByDefault
public sealed interface M3ComponentTokens permits M3ComponentTokensImpl {
    /// Returns tokens used by filled buttons.
    ButtonTokens filledButton();

    /// Returns tokens used by filled tonal buttons.
    ButtonTokens tonalButton();

    /// Returns tokens used by outlined buttons.
    ButtonTokens outlinedButton();

    /// Returns tokens used by text buttons.
    ButtonTokens textButton();

    /// Returns tokens used by elevated buttons.
    ButtonTokens elevatedButton();

    /// Returns tokens used by icon buttons.
    ButtonTokens iconButton();

    /// Returns tokens used by floating action buttons.
    FabTokens floatingActionButton();

    /// Returns tokens used by segmented buttons.
    ButtonTokens segmentedButton();

    /// Returns tokens used by text input controls.
    FieldTokens field();

    /// Returns tokens used by selection controls.
    SelectionTokens selection();

    /// Returns tokens used by sliders.
    SliderTokens slider();

    /// Returns tokens used by chips.
    ChipTokens chip();

    /// Returns tokens used by progress controls.
    ProgressTokens progress();

    /// Returns tokens used by cards.
    CardTokens card();

    /// Returns tokens used by dialogs.
    DialogTokens dialog();

    /// Returns tokens used by snackbar controls.
    SnackbarTokens snackbar();

    /// Returns tokens used by dividers.
    DividerTokens divider();

    /// Returns tokens used by badges.
    BadgeTokens badge();

    /// Returns tokens used by list items.
    ListItemTokens listItem();

    /// Creates component tokens from explicit component token values.
    static M3ComponentTokens create(
            ButtonTokens filledButton,
            ButtonTokens tonalButton,
            ButtonTokens outlinedButton,
            ButtonTokens textButton,
            ButtonTokens elevatedButton,
            ButtonTokens iconButton,
            FabTokens floatingActionButton,
            ButtonTokens segmentedButton,
            FieldTokens field,
            SelectionTokens selection,
            SliderTokens slider,
            ChipTokens chip,
            ProgressTokens progress,
            CardTokens card,
            DialogTokens dialog,
            SnackbarTokens snackbar,
            DividerTokens divider,
            BadgeTokens badge,
            ListItemTokens listItem
    ) {
        return new M3ComponentTokensImpl(
                filledButton,
                tonalButton,
                outlinedButton,
                textButton,
                elevatedButton,
                iconButton,
                floatingActionButton,
                segmentedButton,
                field,
                selection,
                slider,
                chip,
                progress,
                card,
                dialog,
                snackbar,
                divider,
                badge,
                listItem
        );
    }

    /// Creates component tokens for a profile.
    static M3ComponentTokens create(M3Profile profile, M3ShapeTokens shapeTokens, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(density, "density");

        double buttonHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double iconButtonSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double fabSmallSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double fabRegularSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double fabLargeSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 104.0 : 96.0);
        double segmentedButtonHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double fieldHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double chipHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 36.0 : 32.0);
        double badgeSmallSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 8.0 : 6.0);
        double badgeLargeHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 18.0 : 16.0);
        double listItemOneLineHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double listItemTwoLineHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 80.0 : 72.0);
        double listItemThreeLineHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 96.0 : 88.0);

        return create(
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 12.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(iconButtonSize, shapeTokens.full(), 0.0),
                new FabTokens(
                        fabSmallSize,
                        fabRegularSize,
                        fabLargeSize,
                        shapeTokens.medium(),
                        shapeTokens.large(),
                        shapeTokens.extraLarge(),
                        12.0,
                        16.0,
                        24.0
                ),
                new ButtonTokens(segmentedButtonHeight, shapeTokens.full(), 12.0),
                new FieldTokens(fieldHeight, shapeTokens.extraSmall(), 16.0),
                new SelectionTokens(density.apply(40.0), shapeTokens.full()),
                new SliderTokens(4.0, shapeTokens.full(), 20.0, density.apply(48.0)),
                new ChipTokens(chipHeight, shapeTokens.small(), 16.0),
                new ProgressTokens(4.0, shapeTokens.full(), 48.0),
                new CardTokens(shapeTokens.medium(), 16.0, 1.0),
                new DialogTokens(shapeTokens.extraLarge(), 24.0),
                new SnackbarTokens(shapeTokens.extraSmall(), 16.0),
                new DividerTokens(1.0, 0.0, 0.0),
                new BadgeTokens(badgeSmallSize, badgeLargeHeight, badgeLargeHeight, badgeLargeHeight / 2.0, 4.0),
                new ListItemTokens(
                        listItemOneLineHeight,
                        listItemTwoLineHeight,
                        listItemThreeLineHeight,
                        profile == M3Profile.EXPRESSIVE_2025 ? shapeTokens.small() : 0.0,
                        16.0,
                        8.0,
                        16.0
                )
        );
    }

    /// Converts component tokens into inline JavaFX CSS declarations.
    default String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        append(builder, "button-filled", filledButton());
        append(builder, "button-tonal", tonalButton());
        append(builder, "button-outlined", outlinedButton());
        append(builder, "button-text", textButton());
        append(builder, "button-elevated", elevatedButton());
        append(builder, "button-icon", iconButton());
        append(builder, floatingActionButton());
        append(builder, "segmented-button", segmentedButton());
        append(builder, field());
        append(builder, selection());
        append(builder, slider());
        append(builder, chip());
        append(builder, progress());
        append(builder, card());
        append(builder, dialog());
        append(builder, snackbar());
        append(builder, divider());
        append(builder, badge());
        append(builder, listItem());
        return builder.toString().trim();
    }

    /// Converts component tokens into JavaFX CSS rules for m3fx controls.
    default String toControlStyleRules() {
        StringBuilder builder = new StringBuilder();
        appendButtonRule(builder, ".m3-filled-button", filledButton());
        appendButtonRule(builder, ".m3-tonal-button", tonalButton());
        appendButtonRule(builder, ".m3-outlined-button", outlinedButton());
        appendButtonRule(builder, ".m3-text-button", textButton());
        appendButtonRule(builder, ".m3-elevated-button", elevatedButton());
        appendButtonRule(builder, ".m3-icon-button", iconButton());
        appendFabRule(
                builder,
                ".m3-small-fab",
                floatingActionButton().smallSize(),
                floatingActionButton().smallShape(),
                floatingActionButton().smallHorizontalPadding()
        );
        appendFabRule(
                builder,
                ".m3-regular-fab",
                floatingActionButton().regularSize(),
                floatingActionButton().regularShape(),
                floatingActionButton().regularHorizontalPadding()
        );
        appendFabRule(
                builder,
                ".m3-large-fab",
                floatingActionButton().largeSize(),
                floatingActionButton().largeShape(),
                floatingActionButton().largeHorizontalPadding()
        );
        appendButtonRule(builder, ".m3-segmented-button", segmentedButton());
        appendSegmentedButtonPositionRules(builder, segmentedButton());
        appendFieldRule(builder, ".m3-text-field, .m3-password-field", field());
        appendFilledFieldRule(builder, ".m3-filled-field", field());
        appendOutlinedFieldRule(builder, ".m3-outlined-field", field());
        appendSelectionRule(builder, ".m3-checkbox, .m3-radio-button, .m3-switch", selection());
        appendSwitchRule(builder, ".m3-switch", selection());
        appendSwitchBoxRule(builder, ".m3-switch .box", selection());
        appendSliderRule(builder, ".m3-slider", slider());
        appendSliderTrackRule(builder, ".m3-slider .track", slider());
        appendSliderThumbRule(builder, ".m3-slider .thumb", slider());
        appendChipRule(builder, ".m3-chip", chip());
        appendProgressBarRule(builder, ".m3-progress-bar", progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .track", progress());
        appendProgressBarTrackRule(builder, ".m3-progress-bar .bar", progress());
        appendProgressIndicatorRule(builder, ".m3-progress-indicator", progress());
        appendCardRule(builder, ".m3-card", card());
        appendDialogRule(builder, ".m3-dialog-pane", dialog());
        appendSnackbarRule(builder, ".m3-snackbar", snackbar());
        appendDividerRule(builder, ".m3-divider", divider());
        appendBadgeRule(builder, ".m3-badge", badge());
        appendListItemRule(builder, ".m3-list-item", listItem());
        return builder.toString().stripTrailing();
    }

    /// Appends button token declarations.
    private static void append(StringBuilder builder, String prefix, ButtonTokens tokens) {
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
    }

    /// Appends floating action button token declarations.
    private static void append(StringBuilder builder, FabTokens tokens) {
        M3TokenCss.append(builder, "-m3-fab-small-container-size", M3TokenCss.pixels(tokens.smallSize()));
        M3TokenCss.append(builder, "-m3-fab-regular-container-size", M3TokenCss.pixels(tokens.regularSize()));
        M3TokenCss.append(builder, "-m3-fab-large-container-size", M3TokenCss.pixels(tokens.largeSize()));
        M3TokenCss.append(builder, "-m3-fab-small-container-shape", M3TokenCss.pixels(tokens.smallShape()));
        M3TokenCss.append(builder, "-m3-fab-regular-container-shape", M3TokenCss.pixels(tokens.regularShape()));
        M3TokenCss.append(builder, "-m3-fab-large-container-shape", M3TokenCss.pixels(tokens.largeShape()));
        M3TokenCss.append(builder, "-m3-fab-small-horizontal-padding", M3TokenCss.pixels(tokens.smallHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-fab-regular-horizontal-padding", M3TokenCss.pixels(tokens.regularHorizontalPadding()));
        M3TokenCss.append(builder, "-m3-fab-large-horizontal-padding", M3TokenCss.pixels(tokens.largeHorizontalPadding()));
    }

    /// Appends field token declarations.
    private static void append(StringBuilder builder, FieldTokens tokens) {
        M3TokenCss.append(builder, "-m3-field-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-field-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-field-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
    }

    /// Appends selection token declarations.
    private static void append(StringBuilder builder, SelectionTokens tokens) {
        M3TokenCss.append(builder, "-m3-selection-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        M3TokenCss.append(builder, "-m3-selection-track-shape", M3TokenCss.pixels(tokens.trackShape()));
    }

    /// Appends slider token declarations.
    private static void append(StringBuilder builder, SliderTokens tokens) {
        M3TokenCss.append(builder, "-m3-slider-track-thickness", M3TokenCss.pixels(tokens.trackThickness()));
        M3TokenCss.append(builder, "-m3-slider-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        M3TokenCss.append(builder, "-m3-slider-thumb-size", M3TokenCss.pixels(tokens.thumbSize()));
        M3TokenCss.append(builder, "-m3-slider-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
    }

    /// Appends chip token declarations.
    private static void append(StringBuilder builder, ChipTokens tokens) {
        M3TokenCss.append(builder, "-m3-chip-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-chip-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-chip-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
    }

    /// Appends progress token declarations.
    private static void append(StringBuilder builder, ProgressTokens tokens) {
        M3TokenCss.append(builder, "-m3-progress-thickness", M3TokenCss.pixels(tokens.thickness()));
        M3TokenCss.append(builder, "-m3-progress-shape", M3TokenCss.pixels(tokens.shape()));
        M3TokenCss.append(builder, "-m3-progress-indicator-size", M3TokenCss.pixels(tokens.indicatorSize()));
    }

    /// Appends card token declarations.
    private static void append(StringBuilder builder, CardTokens tokens) {
        M3TokenCss.append(builder, "-m3-card-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-card-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        M3TokenCss.append(builder, "-m3-card-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
    }

    /// Appends dialog token declarations.
    private static void append(StringBuilder builder, DialogTokens tokens) {
        M3TokenCss.append(builder, "-m3-dialog-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-dialog-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
    }

    /// Appends snackbar token declarations.
    private static void append(StringBuilder builder, SnackbarTokens tokens) {
        M3TokenCss.append(builder, "-m3-snackbar-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-snackbar-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
    }

    /// Appends divider token declarations.
    private static void append(StringBuilder builder, DividerTokens tokens) {
        M3TokenCss.append(builder, "-m3-divider-thickness", M3TokenCss.pixels(tokens.thickness()));
        M3TokenCss.append(builder, "-m3-divider-inset-start", M3TokenCss.pixels(tokens.insetStart()));
        M3TokenCss.append(builder, "-m3-divider-inset-end", M3TokenCss.pixels(tokens.insetEnd()));
    }

    /// Appends badge token declarations.
    private static void append(StringBuilder builder, BadgeTokens tokens) {
        M3TokenCss.append(builder, "-m3-badge-small-size", M3TokenCss.pixels(tokens.smallSize()));
        M3TokenCss.append(builder, "-m3-badge-large-height", M3TokenCss.pixels(tokens.largeHeight()));
        M3TokenCss.append(builder, "-m3-badge-large-min-width", M3TokenCss.pixels(tokens.largeMinWidth()));
        M3TokenCss.append(builder, "-m3-badge-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-badge-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
    }

    /// Appends list item token declarations.
    private static void append(StringBuilder builder, ListItemTokens tokens) {
        M3TokenCss.append(builder, "-m3-list-item-one-line-height", M3TokenCss.pixels(tokens.oneLineHeight()));
        M3TokenCss.append(builder, "-m3-list-item-two-line-height", M3TokenCss.pixels(tokens.twoLineHeight()));
        M3TokenCss.append(builder, "-m3-list-item-three-line-height", M3TokenCss.pixels(tokens.threeLineHeight()));
        M3TokenCss.append(builder, "-m3-list-item-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-list-item-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        M3TokenCss.append(builder, "-m3-list-item-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        M3TokenCss.append(builder, "-m3-list-item-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
    }

    /// Appends a button token CSS rule.
    private static void appendButtonRule(StringBuilder builder, String selector, ButtonTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a floating action button token CSS rule.
    private static void appendFabRule(
            StringBuilder builder,
            String selector,
            double size,
            double shape,
            double horizontalPadding
    ) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-size", M3TokenCss.pixels(size));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(shape));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(horizontalPadding));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(shape));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(shape));
        endRule(builder);
    }

    /// Appends segmented button position shape CSS rules.
    private static void appendSegmentedButtonPositionRules(StringBuilder builder, ButtonTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        appendSegmentedButtonPositionRule(builder, ".m3-segmented-button-single", radius, radius, radius, radius);
        appendSegmentedButtonPositionRule(builder, ".m3-segmented-button-first", radius, "0", "0", radius);
        appendSegmentedButtonPositionRule(builder, ".m3-segmented-button-middle", "0", "0", "0", "0");
        appendSegmentedButtonPositionRule(builder, ".m3-segmented-button-last", "0", radius, radius, "0");
    }

    /// Appends a segmented button position shape CSS rule.
    private static void appendSegmentedButtonPositionRule(
            StringBuilder builder,
            String selector,
            String topLeft,
            String topRight,
            String bottomRight,
            String bottomLeft
    ) {
        String radii = topLeft + " " + topRight + " " + bottomRight + " " + bottomLeft;
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radii);
        appendDeclaration(builder, "-fx-border-radius", radii);
        endRule(builder);
    }

    /// Appends a field token CSS rule.
    private static void appendFieldRule(StringBuilder builder, String selector, FieldTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a filled field shape CSS rule.
    private static void appendFilledFieldRule(StringBuilder builder, String selector, FieldTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius + " " + radius + " 0 0");
        endRule(builder);
    }

    /// Appends an outlined field shape CSS rule.
    private static void appendOutlinedFieldRule(StringBuilder builder, String selector, FieldTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.containerShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a selection token CSS rule.
    private static void appendSelectionRule(StringBuilder builder, String selector, SelectionTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        endRule(builder);
    }

    /// Appends a switch token CSS rule.
    private static void appendSwitchRule(StringBuilder builder, String selector, SelectionTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        endRule(builder);
    }

    /// Appends a switch box shape CSS rule.
    private static void appendSwitchBoxRule(StringBuilder builder, String selector, SelectionTokens tokens) {
        String radius = M3TokenCss.pixels(tokens.trackShape());
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", radius);
        appendDeclaration(builder, "-fx-border-radius", radius);
        endRule(builder);
    }

    /// Appends a slider token CSS rule.
    private static void appendSliderRule(StringBuilder builder, String selector, SliderTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-track-thickness", M3TokenCss.pixels(tokens.trackThickness()));
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-m3-thumb-size", M3TokenCss.pixels(tokens.thumbSize()));
        appendDeclaration(builder, "-m3-touch-target-size", M3TokenCss.pixels(tokens.touchTargetSize()));
        endRule(builder);
    }

    /// Appends a slider track visual CSS rule.
    private static void appendSliderTrackRule(StringBuilder builder, String selector, SliderTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.trackThickness()));
        endRule(builder);
    }

    /// Appends a slider thumb visual CSS rule.
    private static void appendSliderThumbRule(StringBuilder builder, String selector, SliderTokens tokens) {
        String thumbInset = M3TokenCss.pixels(tokens.thumbSize() / 2.0);
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.trackShape()));
        appendDeclaration(builder, "-fx-padding", thumbInset);
        endRule(builder);
    }

    /// Appends a chip token CSS rule.
    private static void appendChipRule(StringBuilder builder, String selector, ChipTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-height", M3TokenCss.pixels(tokens.height()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-border-radius", M3TokenCss.pixels(tokens.containerShape()));
        endRule(builder);
    }

    /// Appends a progress bar token CSS rule.
    private static void appendProgressBarRule(StringBuilder builder, String selector, ProgressTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-track-thickness", M3TokenCss.pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-track-shape", M3TokenCss.pixels(tokens.shape()));
        endRule(builder);
    }

    /// Appends a progress bar track visual CSS rule.
    private static void appendProgressBarTrackRule(StringBuilder builder, String selector, ProgressTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.shape()));
        appendDeclaration(builder, "-fx-pref-height", M3TokenCss.pixels(tokens.thickness()));
        endRule(builder);
    }

    /// Appends a progress indicator token CSS rule.
    private static void appendProgressIndicatorRule(StringBuilder builder, String selector, ProgressTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-indicator-size", M3TokenCss.pixels(tokens.indicatorSize()));
        endRule(builder);
    }

    /// Appends a dialog pane token CSS rule.
    private static void appendDialogRule(StringBuilder builder, String selector, DialogTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-fx-background-radius", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-fx-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a card token CSS rule.
    private static void appendCardRule(StringBuilder builder, String selector, CardTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        appendDeclaration(builder, "-m3-outline-width", M3TokenCss.pixels(tokens.outlineWidth()));
        endRule(builder);
    }

    /// Appends a snackbar token CSS rule.
    private static void appendSnackbarRule(StringBuilder builder, String selector, SnackbarTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-content-padding", M3TokenCss.pixels(tokens.contentPadding()));
        endRule(builder);
    }

    /// Appends a divider token CSS rule.
    private static void appendDividerRule(StringBuilder builder, String selector, DividerTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-thickness", M3TokenCss.pixels(tokens.thickness()));
        appendDeclaration(builder, "-m3-inset-start", M3TokenCss.pixels(tokens.insetStart()));
        appendDeclaration(builder, "-m3-inset-end", M3TokenCss.pixels(tokens.insetEnd()));
        endRule(builder);
    }

    /// Appends a badge token CSS rule.
    private static void appendBadgeRule(StringBuilder builder, String selector, BadgeTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-small-size", M3TokenCss.pixels(tokens.smallSize()));
        appendDeclaration(builder, "-m3-large-height", M3TokenCss.pixels(tokens.largeHeight()));
        appendDeclaration(builder, "-m3-large-min-width", M3TokenCss.pixels(tokens.largeMinWidth()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        endRule(builder);
    }

    /// Appends a list item token CSS rule.
    private static void appendListItemRule(StringBuilder builder, String selector, ListItemTokens tokens) {
        beginRule(builder, selector);
        appendDeclaration(builder, "-m3-one-line-height", M3TokenCss.pixels(tokens.oneLineHeight()));
        appendDeclaration(builder, "-m3-two-line-height", M3TokenCss.pixels(tokens.twoLineHeight()));
        appendDeclaration(builder, "-m3-three-line-height", M3TokenCss.pixels(tokens.threeLineHeight()));
        appendDeclaration(builder, "-m3-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        appendDeclaration(builder, "-m3-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
        appendDeclaration(builder, "-m3-vertical-padding", M3TokenCss.pixels(tokens.verticalPadding()));
        appendDeclaration(builder, "-m3-content-spacing", M3TokenCss.pixels(tokens.contentSpacing()));
        endRule(builder);
    }

    /// Appends a CSS rule header.
    private static void beginRule(StringBuilder builder, String selector) {
        builder.append(selector).append(" {\n");
    }

    /// Appends a CSS declaration line.
    private static void appendDeclaration(StringBuilder builder, String name, String value) {
        builder.append("    ").append(name).append(": ").append(value).append(";\n");
    }

    /// Appends a CSS rule footer.
    private static void endRule(StringBuilder builder) {
        builder.append("}\n\n");
    }

    /// Tokens shared by button variants.
    ///
    /// @param height the preferred button height
    /// @param containerShape the button container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    public record ButtonTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates button tokens.
        public ButtonTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens shared by floating action button sizes.
    ///
    /// @param smallSize the small floating action button square size
    /// @param regularSize the regular floating action button square size
    /// @param largeSize the large floating action button square size
    /// @param smallShape the small floating action button corner radius
    /// @param regularShape the regular floating action button corner radius
    /// @param largeShape the large floating action button corner radius
    /// @param smallHorizontalPadding the horizontal padding for small extended floating action buttons
    /// @param regularHorizontalPadding the horizontal padding for regular extended floating action buttons
    /// @param largeHorizontalPadding the horizontal padding for large extended floating action buttons
    @NotNullByDefault
    public record FabTokens(
            double smallSize,
            double regularSize,
            double largeSize,
            double smallShape,
            double regularShape,
            double largeShape,
            double smallHorizontalPadding,
            double regularHorizontalPadding,
            double largeHorizontalPadding
    ) {
        /// Creates floating action button tokens.
        public FabTokens {
            validateNonNegative(smallSize, "smallSize");
            validateNonNegative(regularSize, "regularSize");
            validateNonNegative(largeSize, "largeSize");
            validateNonNegative(smallShape, "smallShape");
            validateNonNegative(regularShape, "regularShape");
            validateNonNegative(largeShape, "largeShape");
            validateNonNegative(smallHorizontalPadding, "smallHorizontalPadding");
            validateNonNegative(regularHorizontalPadding, "regularHorizontalPadding");
            validateNonNegative(largeHorizontalPadding, "largeHorizontalPadding");
        }
    }

    /// Tokens shared by text input controls.
    ///
    /// @param height the preferred field height
    /// @param containerShape the field container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    public record FieldTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates field tokens.
        public FieldTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens shared by selection controls.
    ///
    /// @param touchTargetSize the preferred touch target size
    /// @param trackShape the switch track radius
    @NotNullByDefault
    public record SelectionTokens(
            double touchTargetSize,
            double trackShape
    ) {
        /// Creates selection tokens.
        public SelectionTokens {
            validateNonNegative(touchTargetSize, "touchTargetSize");
            validateNonNegative(trackShape, "trackShape");
        }
    }

    /// Tokens shared by sliders.
    ///
    /// @param trackThickness the slider track thickness
    /// @param trackShape the slider track radius
    /// @param thumbSize the slider thumb size
    /// @param touchTargetSize the preferred slider touch target size
    @NotNullByDefault
    public record SliderTokens(
            double trackThickness,
            double trackShape,
            double thumbSize,
            double touchTargetSize
    ) {
        /// Creates slider tokens.
        public SliderTokens {
            validateNonNegative(trackThickness, "trackThickness");
            validateNonNegative(trackShape, "trackShape");
            validateNonNegative(thumbSize, "thumbSize");
            validateNonNegative(touchTargetSize, "touchTargetSize");
        }
    }

    /// Tokens shared by chip variants.
    ///
    /// @param height the preferred chip height
    /// @param containerShape the chip container radius
    /// @param horizontalPadding the horizontal content padding
    @NotNullByDefault
    public record ChipTokens(
            double height,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates chip tokens.
        public ChipTokens {
            validateNonNegative(height, "height");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens shared by progress indicators.
    ///
    /// @param thickness the default track thickness
    /// @param shape the progress indicator radius
    /// @param indicatorSize the circular indicator size
    @NotNullByDefault
    public record ProgressTokens(
            double thickness,
            double shape,
            double indicatorSize
    ) {
        /// Creates progress tokens.
        public ProgressTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(shape, "shape");
            validateNonNegative(indicatorSize, "indicatorSize");
        }
    }

    /// Tokens used by cards.
    ///
    /// @param containerShape the card container radius
    /// @param contentPadding the card content padding
    /// @param outlineWidth the outlined card border width
    @NotNullByDefault
    public record CardTokens(
            double containerShape,
            double contentPadding,
            double outlineWidth
    ) {
        /// Creates card tokens.
        public CardTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }

    /// Tokens used by dialogs.
    ///
    /// @param containerShape the dialog container radius
    /// @param contentPadding the dialog content padding
    @NotNullByDefault
    public record DialogTokens(
            double containerShape,
            double contentPadding
    ) {
        /// Creates dialog tokens.
        public DialogTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
        }
    }

    /// Tokens used by snackbar controls.
    ///
    /// @param containerShape the snackbar container radius
    /// @param contentPadding the snackbar content padding
    @NotNullByDefault
    public record SnackbarTokens(
            double containerShape,
            double contentPadding
    ) {
        /// Creates snackbar tokens.
        public SnackbarTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
        }
    }

    /// Tokens used by dividers.
    ///
    /// @param thickness the divider line thickness
    /// @param insetStart the leading inset before the divider line
    /// @param insetEnd the trailing inset after the divider line
    @NotNullByDefault
    public record DividerTokens(
            double thickness,
            double insetStart,
            double insetEnd
    ) {
        /// Creates divider tokens.
        public DividerTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(insetStart, "insetStart");
            validateNonNegative(insetEnd, "insetEnd");
        }
    }

    /// Tokens used by badges.
    ///
    /// @param smallSize the dot badge size
    /// @param largeHeight the text badge height
    /// @param largeMinWidth the text badge minimum width
    /// @param containerShape the text badge container radius
    /// @param horizontalPadding the text badge horizontal padding
    @NotNullByDefault
    public record BadgeTokens(
            double smallSize,
            double largeHeight,
            double largeMinWidth,
            double containerShape,
            double horizontalPadding
    ) {
        /// Creates badge tokens.
        public BadgeTokens {
            validateNonNegative(smallSize, "smallSize");
            validateNonNegative(largeHeight, "largeHeight");
            validateNonNegative(largeMinWidth, "largeMinWidth");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
        }
    }

    /// Tokens used by list items.
    ///
    /// @param oneLineHeight the preferred one-line item height
    /// @param twoLineHeight the preferred two-line item height
    /// @param threeLineHeight the preferred three-line item height
    /// @param containerShape the list item container radius
    /// @param horizontalPadding the horizontal content padding
    /// @param verticalPadding the vertical content padding
    /// @param contentSpacing the spacing between content regions
    @NotNullByDefault
    public record ListItemTokens(
            double oneLineHeight,
            double twoLineHeight,
            double threeLineHeight,
            double containerShape,
            double horizontalPadding,
            double verticalPadding,
            double contentSpacing
    ) {
        /// Creates list item tokens.
        public ListItemTokens {
            validateNonNegative(oneLineHeight, "oneLineHeight");
            validateNonNegative(twoLineHeight, "twoLineHeight");
            validateNonNegative(threeLineHeight, "threeLineHeight");
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(horizontalPadding, "horizontalPadding");
            validateNonNegative(verticalPadding, "verticalPadding");
            validateNonNegative(contentSpacing, "contentSpacing");
        }
    }

    /// Validates a non-negative component token.
    private static void validateNonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
