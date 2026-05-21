// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

/// A non-interactive Material Design 3 menu section header.
@NotNullByDefault
public class M3MenuSectionHeader extends M3Text {
    /// The base style class for M3FX menu section headers.
    public static final String STYLE_CLASS = "m3-menu-section-header";

    /// Creates an empty menu section header.
    public M3MenuSectionHeader() {
        this("");
    }

    /// Creates a menu section header with text.
    ///
    /// @param text the header text
    public M3MenuSectionHeader(String text) {
        super(text, M3TextRole.LABEL_LARGE);
        initialize();
    }

    /// Returns the user-agent stylesheet for M3FX menu section headers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("menu.css");
    }

    /// Adds base style classes and keeps the header outside keyboard traversal.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setFocusTraversable(false);
    }
}
