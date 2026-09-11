// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.StyleableDoubleProperty;
import org.glavo.m3fx.internal.M3Css;
import org.jetbrains.annotations.NotNullByDefault;

/// Defines icon sizing shared by M3FX icon graphic controls.
///
/// Implementations may use any rendering model while allowing containing controls to apply component icon-size
/// tokens without depending on a particular font or vector representation.
@NotNullByDefault
interface M3IconGraphic {
    /// Returns the observable, bindable, styleable icon-size property.
    ///
    /// The property represents both rendered dimensions in logical pixels and accepts only finite, non-negative
    /// values. Each implementation initializes it from its current semantic size.
    ///
    /// @return the icon-size property
    /// @implSpec The property must be created by [M3Css#nonNegativeStyleableDoubleProperty] so containing controls
    /// can apply and release temporary token values through [M3Css#setControlToken(StyleableDoubleProperty, double)].
    StyleableDoubleProperty iconSizeProperty();
}
