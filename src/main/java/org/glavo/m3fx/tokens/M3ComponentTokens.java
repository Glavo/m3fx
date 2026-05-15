package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Holds component-level Material Design 3 tokens used by m3fx controls.
@NotNullByDefault
public record M3ComponentTokens(
        /// Tokens used by filled buttons.
        ButtonTokens filledButton,

        /// Tokens used by filled tonal buttons.
        ButtonTokens tonalButton,

        /// Tokens used by outlined buttons.
        ButtonTokens outlinedButton,

        /// Tokens used by text buttons.
        ButtonTokens textButton,

        /// Tokens used by elevated buttons.
        ButtonTokens elevatedButton,

        /// Tokens used by icon buttons.
        ButtonTokens iconButton,

        /// Tokens used by text input controls.
        FieldTokens field,

        /// Tokens used by selection controls.
        SelectionTokens selection,

        /// Tokens used by progress controls.
        ProgressTokens progress,

        /// Tokens used by cards.
        CardTokens card,

        /// Tokens used by dialogs.
        DialogTokens dialog,

        /// Tokens used by snackbar controls.
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
    @NotNullByDefault
    public record ButtonTokens(
            /// The preferred button height.
            double height,

            /// The button container radius.
            double containerShape,

            /// The horizontal content padding.
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
    @NotNullByDefault
    public record FieldTokens(
            /// The preferred field height.
            double height,

            /// The field container radius.
            double containerShape,

            /// The horizontal content padding.
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
    @NotNullByDefault
    public record SelectionTokens(
            /// The preferred touch target size.
            double touchTargetSize,

            /// The switch track radius.
            double trackShape
    ) {
        /// Creates selection tokens.
        public SelectionTokens {
            validateNonNegative(touchTargetSize, "touchTargetSize");
            validateNonNegative(trackShape, "trackShape");
        }
    }

    /// Tokens shared by progress indicators.
    @NotNullByDefault
    public record ProgressTokens(
            /// The default track thickness.
            double thickness,

            /// The progress indicator radius.
            double shape
    ) {
        /// Creates progress tokens.
        public ProgressTokens {
            validateNonNegative(thickness, "thickness");
            validateNonNegative(shape, "shape");
        }
    }

    /// Tokens used by cards.
    @NotNullByDefault
    public record CardTokens(
            /// The card container radius.
            double containerShape,

            /// The outlined card border width.
            double outlineWidth
    ) {
        /// Creates card tokens.
        public CardTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(outlineWidth, "outlineWidth");
        }
    }

    /// Tokens used by dialogs.
    @NotNullByDefault
    public record DialogTokens(
            /// The dialog container radius.
            double containerShape,

            /// The dialog content padding.
            double contentPadding
    ) {
        /// Creates dialog tokens.
        public DialogTokens {
            validateNonNegative(containerShape, "containerShape");
            validateNonNegative(contentPadding, "contentPadding");
        }
    }

    /// Tokens used by snackbar controls.
    @NotNullByDefault
    public record SnackbarTokens(
            /// The snackbar container radius.
            double containerShape,

            /// The snackbar content padding.
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
