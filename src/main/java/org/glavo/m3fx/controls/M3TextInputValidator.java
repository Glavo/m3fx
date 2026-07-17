// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.control.TextInputControl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Validates text entered into an [M3TextInputLayout].
///
/// Validation is synchronous and is invoked on the JavaFX Application Thread. Returning `null` or an empty string
/// accepts the value; returning non-empty text rejects it and supplies the message rendered by the layout.
/// Implementations should avoid blocking work and should not modify the input while validation is in progress.
/// An exception thrown by a validator is propagated to the caller of the validation operation.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@FunctionalInterface
@NotNullByDefault
public interface M3TextInputValidator {
    /// Returns an error message for invalid input, or `null` when the input is valid.
    ///
    /// The layout supplies its current input and a non-null snapshot of the input text. The validator must not retain
    /// either value as an ownership claim. This method may be invoked repeatedly as the user edits after validation
    /// has become active.
    ///
    /// @param input the text input control being validated
    /// @param text the current text value to validate
    /// @return a non-empty error message for invalid input, or `null` or an empty string when the input is valid
    @Nullable String validate(TextInputControl input, String text);
}
