// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.StyleableDoubleProperty;
import org.jetbrains.annotations.NotNullByDefault;

/// Internal sizing contract shared by M3FX icon graphic controls.
///
/// Implementations retain their own rendering model while allowing containing controls to apply component icon-size
/// tokens without depending on a particular font or vector representation.
@NotNullByDefault
interface M3IconGraphic {
    /// The common style class used when an icon participates in a component graphic slot.
    String STYLE_CLASS = "m3-icon-graphic";

    /// Returns the effective icon size.
    ///
    /// @return the icon size in logical pixels
    double getIconSize();

    /// Sets an explicit icon size.
    ///
    /// @param iconSize the icon size in logical pixels
    void setIconSize(double iconSize);

    /// Returns the styleable icon-size property.
    ///
    /// @return the icon-size property
    StyleableDoubleProperty iconSizeProperty();
}
