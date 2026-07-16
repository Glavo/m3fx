// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 assist chip for contextual actions.
///
/// Assist chips are command surfaces and fire action events without retaining selected state.
///
/// See [Material Design assist chips](https://m3.material.io/components/chips/specs#assist-chip).
@NotNullByDefault
public final class M3AssistChip extends M3Chip {
    /// The style class identifying assist chips.
    public static final String STYLE_CLASS = "m3-assist-chip";

    /// Creates an empty assist chip.
    public M3AssistChip() {
        this("", null);
    }

    /// Creates an assist chip with text.
    ///
    /// @param text the text displayed by the chip
    public M3AssistChip(String text) {
        this(text, null);
    }

    /// Creates an assist chip with text and graphic content.
    ///
    /// @param text the text displayed by the chip
    /// @param graphic the optional graphic displayed with the text
    public M3AssistChip(String text, @Nullable Node graphic) {
        super(text, graphic, STYLE_CLASS);
    }
}
