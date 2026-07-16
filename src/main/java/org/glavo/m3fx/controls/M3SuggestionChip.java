// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 suggestion chip offering a contextually relevant response or action.
///
/// Suggestion chips are command surfaces and fire action events without retaining selected state.
///
/// See [Material Design suggestion chips](https://m3.material.io/components/chips/specs#suggestion-chip).
@NotNullByDefault
public final class M3SuggestionChip extends M3Chip {
    /// The style class identifying suggestion chips.
    public static final String STYLE_CLASS = "m3-suggestion-chip";

    /// Creates an empty suggestion chip.
    public M3SuggestionChip() {
        this("", null);
    }

    /// Creates a suggestion chip with text.
    ///
    /// @param text the text displayed by the chip
    public M3SuggestionChip(String text) {
        this(text, null);
    }

    /// Creates a suggestion chip with text and graphic content.
    ///
    /// @param text the text displayed by the chip
    /// @param graphic the optional graphic displayed with the text
    public M3SuggestionChip(String text, @Nullable Node graphic) {
        super(text, graphic, STYLE_CLASS);
    }
}
