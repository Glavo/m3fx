// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 button used to invoke a command.
///
/// The button supports the Material emphasis variants, size scale, shape roles, state layers, ripple feedback,
/// focus indication, and elevation behavior provided by [M3ButtonBase].
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/overview).
@NotNullByDefault
public final class M3Button extends M3ButtonBase {
    /// Creates an empty filled button.
    public M3Button() {
        super();
    }

    /// Creates a filled button with text.
    ///
    /// @param text the text displayed by the button
    public M3Button(String text) {
        super(text);
    }

    /// Creates a filled button with text and graphic content.
    ///
    /// @param text the text displayed by the button
    /// @param graphic the optional graphic displayed with the text
    public M3Button(String text, @Nullable Node graphic) {
        super(text, graphic);
    }

    /// Creates a button with text and the requested variant.
    ///
    /// @param text the text displayed by the button
    /// @param variant the Material button variant
    public M3Button(String text, M3ButtonVariant variant) {
        super(text, variant);
    }

    /// Creates a button with text, graphic content, and the requested variant.
    ///
    /// @param text the text displayed by the button
    /// @param graphic the optional graphic displayed with the text
    /// @param variant the Material button variant
    public M3Button(String text, @Nullable Node graphic, M3ButtonVariant variant) {
        super(text, graphic, variant);
    }
}
