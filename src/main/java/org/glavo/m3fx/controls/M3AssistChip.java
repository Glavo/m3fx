// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 chip that performs a contextual action.
///
/// An assist chip has the action semantics of a JavaFX button. Activating it by mouse, keyboard, or
/// [fire][javafx.scene.control.ButtonBase#fire()] emits an action event; it does not retain a selected state.
/// Text, leading and trailing graphics, container treatment, and sizing are inherited from [M3Chip]. The
/// default constructors create a flat, enabled chip with empty text and no graphics.
///
/// See [Material Design assist chips](https://m3.material.io/components/chips/specs#assist-chip).
@NotNullByDefault
public final class M3AssistChip extends M3Chip {
    /// The style class identifying assist chips.
    public static final String STYLE_CLASS = "m3-assist-chip";

    /// Creates a flat assist chip with empty text and no graphics.
    public M3AssistChip() {
        this("", null);
    }

    /// Creates a flat assist chip with the specified text and no graphics.
    ///
    /// @param text the text displayed by the chip
    /// @throws NullPointerException if `text` is `null`
    public M3AssistChip(String text) {
        this(text, null);
    }

    /// Creates a flat assist chip with the specified text and leading graphic.
    ///
    /// @param text the text displayed by the chip
    /// @param graphic the leading graphic, or `null` for no leading graphic
    /// @throws NullPointerException if `text` is `null`
    public M3AssistChip(String text, @Nullable Node graphic) {
        super(text, graphic, STYLE_CLASS);
    }
}
