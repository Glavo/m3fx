package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual and semantic variant of an m3fx chip.
@NotNullByDefault
public enum M3ChipVariant {
    /// An assist chip.
    ASSIST("m3-assist-chip"),

    /// A filter chip.
    FILTER("m3-filter-chip"),

    /// An input chip.
    INPUT("m3-input-chip"),

    /// A suggestion chip.
    SUGGESTION("m3-suggestion-chip");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a chip variant.
    M3ChipVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    public String getStyleClass() {
        return styleClass;
    }
}
