// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 input chip representing user-provided input or an entity.
///
/// Input chips represent user-provided values or entities such as recipients, tags, or search terms. The inherited
/// [M3SelectableChip#selectedProperty()] records persistent selection; firing an enabled chip toggles that state and
/// delivers an action event. The inherited [M3Chip#trailingGraphicProperty()] may contain a separate remove action.
///
/// The empty constructor creates an unselected chip with empty text and no graphic. Graphic nodes may have only
/// one parent. A separately actionable trailing control should own its own action rather than relying on the
/// chip's action event.
///
/// See [Material Design input chips](https://m3.material.io/components/chips/specs#input-chip).
@NotNullByDefault
public final class M3InputChip extends M3SelectableChip {
    /// The style class identifying input chips.
    public static final String STYLE_CLASS = "m3-input-chip";

    /// Creates an empty input chip.
    public M3InputChip() {
        this("", null);
    }

    /// Creates an input chip with text.
    ///
    /// @param text the text displayed by the chip
    /// @throws NullPointerException if `text` is `null`
    public M3InputChip(String text) {
        this(text, null);
    }

    /// Creates an input chip with text and graphic content.
    ///
    /// @param text    the text displayed by the chip
    /// @param graphic the optional leading graphic displayed with the text, or `null`
    /// @throws NullPointerException if `text` is `null`
    public M3InputChip(String text, @Nullable Node graphic) {
        super(text, graphic, STYLE_CLASS);
    }
}
