// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies a Material Design 3 typography role used by [M3Text].
///
/// Text roles correspond to the Material type scale. Controls and applications can use these roles to select
/// typography by intent rather than by hard-coded font size or weight.
///
/// See [Material Design typography](https://m3.material.io/styles/typography/overview).
@NotNullByDefault
public enum M3TextRole {
    /// Display large text used for the largest presentation text.
    DISPLAY_LARGE("m3-display-large-text"),

    /// Display medium text used for prominent presentation text.
    DISPLAY_MEDIUM("m3-display-medium-text"),

    /// Display small text used for compact presentation text.
    DISPLAY_SMALL("m3-display-small-text"),

    /// Headline large text used for prominent section headings.
    HEADLINE_LARGE("m3-headline-large-text"),

    /// Headline medium text used for medium section headings.
    HEADLINE_MEDIUM("m3-headline-medium-text"),

    /// Headline small text used for compact section headings.
    HEADLINE_SMALL("m3-headline-small-text"),

    /// Title large text used for high-emphasis titles.
    TITLE_LARGE("m3-title-large-text"),

    /// Title medium text used for medium-emphasis titles.
    TITLE_MEDIUM("m3-title-medium-text"),

    /// Title small text used for compact titles.
    TITLE_SMALL("m3-title-small-text"),

    /// Label large text used for buttons and prominent labels.
    LABEL_LARGE("m3-label-large-text"),

    /// Label medium text used for medium labels.
    LABEL_MEDIUM("m3-label-medium-text"),

    /// Label small text used for compact labels.
    LABEL_SMALL("m3-label-small-text"),

    /// Body large text used for primary body copy.
    BODY_LARGE("m3-body-large-text"),

    /// Body medium text used for secondary body copy.
    BODY_MEDIUM("m3-body-medium-text"),

    /// Body small text used for compact body copy.
    BODY_SMALL("m3-body-small-text");

    /// The style class applied for this typography role.
    private final String styleClass;

    /// Creates a typography role.
    M3TextRole(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the style class for this typography role.
    ///
    /// @return the style class applied by this typography role
    String styleClass() {
        return styleClass;
    }
}
