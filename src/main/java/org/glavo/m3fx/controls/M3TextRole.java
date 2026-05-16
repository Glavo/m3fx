// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 typography role used by [M3Text].
@NotNullByDefault
public enum M3TextRole {
    /// Display large text used for the largest presentation text.
    DISPLAY_LARGE("m3-display-large-text"),

    /// Headline medium text used for prominent section headings.
    HEADLINE_MEDIUM("m3-headline-medium-text"),

    /// Title large text used for medium emphasis titles.
    TITLE_LARGE("m3-title-large-text"),

    /// Label large text used for buttons and compact labels.
    LABEL_LARGE("m3-label-large-text"),

    /// Body large text used for primary body copy.
    BODY_LARGE("m3-body-large-text"),

    /// Body medium text used for secondary body copy.
    BODY_MEDIUM("m3-body-medium-text");

    /// The style class applied for this typography role.
    private final String styleClass;

    /// Creates a typography role.
    M3TextRole(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class for this typography role.
    public String getStyleClass() {
        return styleClass;
    }
}
