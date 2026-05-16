package org.glavo.m3fx.internal.tokens;

import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Default immutable implementation of {@link M3ComponentTokens}.
///
/// @param filledButton tokens used by filled buttons
/// @param tonalButton tokens used by filled tonal buttons
/// @param outlinedButton tokens used by outlined buttons
/// @param textButton tokens used by text buttons
/// @param elevatedButton tokens used by elevated buttons
/// @param iconButton tokens used by icon buttons
/// @param floatingActionButton tokens used by floating action buttons
/// @param segmentedButton tokens used by segmented buttons
/// @param field tokens used by text input controls
/// @param selection tokens used by selection controls
/// @param slider tokens used by sliders
/// @param chip tokens used by chips
/// @param progress tokens used by progress controls
/// @param card tokens used by cards
/// @param dialog tokens used by dialogs
/// @param snackbar tokens used by snackbar controls
/// @param divider tokens used by dividers
/// @param badge tokens used by badges
/// @param listItem tokens used by list items
@NotNullByDefault
public record M3ComponentTokensImpl(
        M3ComponentTokens.ButtonTokens filledButton,
        M3ComponentTokens.ButtonTokens tonalButton,
        M3ComponentTokens.ButtonTokens outlinedButton,
        M3ComponentTokens.ButtonTokens textButton,
        M3ComponentTokens.ButtonTokens elevatedButton,
        M3ComponentTokens.ButtonTokens iconButton,
        M3ComponentTokens.FabTokens floatingActionButton,
        M3ComponentTokens.ButtonTokens segmentedButton,
        M3ComponentTokens.FieldTokens field,
        M3ComponentTokens.SelectionTokens selection,
        M3ComponentTokens.SliderTokens slider,
        M3ComponentTokens.ChipTokens chip,
        M3ComponentTokens.ProgressTokens progress,
        M3ComponentTokens.CardTokens card,
        M3ComponentTokens.DialogTokens dialog,
        M3ComponentTokens.SnackbarTokens snackbar,
        M3ComponentTokens.DividerTokens divider,
        M3ComponentTokens.BadgeTokens badge,
        M3ComponentTokens.ListItemTokens listItem
) implements M3ComponentTokens {
    /// Creates component token implementation.
    public M3ComponentTokensImpl {
        Objects.requireNonNull(filledButton, "filledButton");
        Objects.requireNonNull(tonalButton, "tonalButton");
        Objects.requireNonNull(outlinedButton, "outlinedButton");
        Objects.requireNonNull(textButton, "textButton");
        Objects.requireNonNull(elevatedButton, "elevatedButton");
        Objects.requireNonNull(iconButton, "iconButton");
        Objects.requireNonNull(floatingActionButton, "floatingActionButton");
        Objects.requireNonNull(segmentedButton, "segmentedButton");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(selection, "selection");
        Objects.requireNonNull(slider, "slider");
        Objects.requireNonNull(chip, "chip");
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(dialog, "dialog");
        Objects.requireNonNull(snackbar, "snackbar");
        Objects.requireNonNull(divider, "divider");
        Objects.requireNonNull(badge, "badge");
        Objects.requireNonNull(listItem, "listItem");
    }
}
