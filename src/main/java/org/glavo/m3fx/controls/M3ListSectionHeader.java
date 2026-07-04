// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

/// A non-interactive Material Design 3 list section header.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public class M3ListSectionHeader extends M3Text {
    /// The base style class for M3FX list section headers.
    public static final String STYLE_CLASS = "m3-list-section-header";

    /// Creates an empty list section header.
    public M3ListSectionHeader() {
        this("");
    }

    /// Creates a list section header with text.
    ///
    /// @param text the header text
    public M3ListSectionHeader(String text) {
        super(text, M3TextRole.LABEL_LARGE);
        M3ControlStyles.add(this, STYLE_CLASS);
        setFocusTraversable(false);
    }

    /// Returns the user-agent stylesheet for M3FX list section headers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("list-item.css");
    }

}
