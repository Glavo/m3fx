package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an m3fx text input control.
@NotNullByDefault
public enum M3TextInputVariant {
    /// A filled text input.
    FILLED("m3-filled-field"),

    /// An outlined text input.
    OUTLINED("m3-outlined-field");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a text input variant.
    M3TextInputVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    public String getStyleClass() {
        return styleClass;
    }
}
