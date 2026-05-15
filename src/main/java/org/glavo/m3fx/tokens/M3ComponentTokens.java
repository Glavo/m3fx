package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds component-level Material Design 3 tokens used by m3fx controls.
///
/// @param filledButton tokens used by filled buttons
/// @param tonalButton tokens used by filled tonal buttons
/// @param outlinedButton tokens used by outlined buttons
/// @param textButton tokens used by text buttons
/// @param elevatedButton tokens used by elevated buttons
/// @param iconButton tokens used by icon buttons
/// @param field tokens used by text input controls
/// @param selection tokens used by selection controls
/// @param slider tokens used by sliders
/// @param chip tokens used by chips
/// @param progress tokens used by progress controls
/// @param card tokens used by cards
/// @param dialog tokens used by dialogs
/// @param snackbar tokens used by snackbar controls
@NotNullByDefault
public record M3ComponentTokens(
        ButtonTokens filledButton,
        ButtonTokens tonalButton,
        ButtonTokens outlinedButton,
        ButtonTokens textButton,
        ButtonTokens elevatedButton,
        ButtonTokens iconButton,
        FieldTokens field,
        SelectionTokens selection,
        SliderTokens slider,
        ChipTokens chip,
        ProgressTokens progress,
        CardTokens card,
        DialogTokens dialog,
        SnackbarTokens snackbar
) {
    /// Creates component tokens.
    public M3ComponentTokens {
        Objects.requireNonNull(filledButton, "filledButton");
        Objects.requireNonNull(tonalButton, "tonalButton");
        Objects.requireNonNull(outlinedButton, "outlinedButton");
        Objects.requireNonNull(textButton, "textButton");
        Objects.requireNonNull(elevatedButton, "elevatedButton");
        Objects.requireNonNull(iconButton, "iconButton");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(selection, "selection");
        Objects.requireNonNull(slider, "slider");
        Objects.requireNonNull(chip, "chip");
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(dialog, "dialog");
        Objects.requireNonNull(snackbar, "snackbar");
    }

    /// Creates component tokens for a profile.
    public static M3ComponentTokens create(M3Profile profile, M3ShapeTokens shapeTokens, M3Density density) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(shapeTokens, "shapeTokens");
        Objects.requireNonNull(density, "density");

        double buttonHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double iconButtonSize = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 48.0 : 40.0);
        double fieldHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 64.0 : 56.0);
        double chipHeight = density.apply(profile == M3Profile.EXPRESSIVE_2025 ? 36.0 : 32.0);

        return new M3ComponentTokens(
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 12.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(iconButtonSize, shapeTokens.full(), 0.0),
                new FieldTokens(fieldHeight, shapeTokens.extraSmall(), 16.0),
                new SelectionTokens(density.apply(40.0), shapeTokens.full()),
                new SliderTokens(4.0, shapeTokens.full(), 20.0, density.apply(48.0)),
                new ChipTokens(chipHeight, shapeTokens.small(), 16.0),
                new ProgressTokens(4.0, shapeTokens.full(), 48.0),
                new CardTokens(shapeTokens.medium(), 1.0),
                new DialogTokens(shapeTokens.extraLarge(), 24.0),
                new SnackbarTokens(shapeTokens.extraSmall(), 16.0)
        );
    }

    /// Converts component tokens into inline JavaFX CSS declarations.
    public String toStyleDeclarations() {
        StringBuilder builder = new StringBuilder();
        append(builder, "button-filled", filledButton);
        append(builder, "button-tonal", tonalButton);
        append(builder, "button-outlined", outlinedButton);
        append(builder, "button-text", textButton);
        append(builder, "button-elevated", elevatedButton);
        append(builder, "button-icon", iconButton);
        append(builder, field);
        append(builder, selection);
        append(builder, slider);
        append(builder, chip);
        append(builder, progress);
        append(builder, card);
        append(builder, dialog);
        append(builder, snackbar);
        return builder.toString().trim();
    }

    /// Appends button token declarations.
    private static void append(StringBuilder builder, String prefix, ButtonTokens tokens) {
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-height", M3TokenCss.pixels(tokens.height()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-container-shape", M3TokenCss.pixels(tokens.containerShape()));
        M3TokenCss.append(builder, "-m3-" + prefix + "-horizontal-padding", M3TokenCss.pixels(tokens.horizontalPadding()));
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
    /// @param outlineWidth the outlined card border width
    @NotNullByDefault
    public record CardTokens(
            double containerShape,
            double outlineWidth
    ) {
        /// Creates card tokens.
        public CardTokens {
            validateNonNegative(containerShape, "containerShape");
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

    /// Validates a non-negative component token.
    private static void validateNonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
