// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.control.TextInputControl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Validates text entered into an [M3TextInputLayout].
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@FunctionalInterface
@NotNullByDefault
public interface M3TextInputValidator {
    /// Returns an error message for invalid input, or null when the input is valid.
    ///
    /// @param input the text input control being validated
    /// @param text the current text value to validate
    /// @return an error message for invalid input, or `null` when the input is valid
    @Nullable String validate(TextInputControl input, String text);
}
