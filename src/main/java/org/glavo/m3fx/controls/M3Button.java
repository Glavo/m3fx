// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 button used to invoke a command.
///
/// `M3Button` has the standard JavaFX button action, keyboard, mnemonic, focus, default-button, and cancel-button
/// contracts supplied by [M3ButtonBase]. Its variant controls visual emphasis without changing action semantics.
/// Unless specified otherwise, constructors create a filled, small, round button that is enabled, is neither the
/// default nor cancel button, and has no action handler.
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/overview).
@NotNullByDefault
public final class M3Button extends M3ButtonBase {
    /// The style class identifying ordinary M3FX command buttons.
    public static final String STYLE_CLASS = "m3-button";

    /// Creates a filled button with empty text and no graphic.
    public M3Button() {
        this("", null, M3ButtonVariant.FILLED);
    }

    /// Creates a filled button with the specified text and no graphic.
    ///
    /// @param text the text displayed by the button
    public M3Button(String text) {
        this(text, null, M3ButtonVariant.FILLED);
    }

    /// Creates a filled button with the specified text and graphic.
    ///
    /// @param text    the text displayed by the button
    /// @param graphic the graphic displayed with the text, or `null` for no graphic
    public M3Button(String text, @Nullable Node graphic) {
        this(text, graphic, M3ButtonVariant.FILLED);
    }

    /// Creates a button with the specified text and variant and no graphic.
    ///
    /// @param text    the text displayed by the button
    /// @param variant the Material button variant
    /// @throws NullPointerException if `variant` is `null`
    public M3Button(String text, M3ButtonVariant variant) {
        this(text, null, variant);
    }

    /// Creates a button with the specified text, graphic, and variant.
    ///
    /// @param text    the text displayed by the button
    /// @param graphic the graphic displayed with the text, or `null` for no graphic
    /// @param variant the Material button variant
    /// @throws NullPointerException if `variant` is `null`
    public M3Button(String text, @Nullable Node graphic, M3ButtonVariant variant) {
        super(text, graphic, variant);
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
