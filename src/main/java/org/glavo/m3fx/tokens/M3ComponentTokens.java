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

        return new M3ComponentTokens(
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 12.0),
                new ButtonTokens(buttonHeight, shapeTokens.full(), 24.0),
                new ButtonTokens(iconButtonSize, shapeTokens.full(), 0.0),
                new FieldTokens(fieldHeight, shapeTokens.extraSmall(), 16.0),
                new SelectionTokens(density.apply(40.0), shapeTokens.full()),
                new ProgressTokens(4.0, shapeTokens.full()),
                new CardTokens(shapeTokens.medium(), 1.0),
                new DialogTokens(shapeTokens.extraLarge(), 24.0),
                new SnackbarTokens(shapeTokens.extraSmall(), 16.0)
        );
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

    /// Tokens shared by progress indicators.
    ///
    /// @param thickness the default track thickness
    /// @param shape the progress indicator radius
    @NotNullByDefault
    public record ProgressTokens(
            double thickness,
            double shape
    ) {
        /// Creates progress tokens.
        public ProgressTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(shape, "shape");
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
