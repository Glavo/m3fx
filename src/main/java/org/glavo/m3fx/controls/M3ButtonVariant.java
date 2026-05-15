package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the visual variant of an m3fx button.
@NotNullByDefault
public enum M3ButtonVariant {
    /// A high-emphasis filled button.
    FILLED("m3-filled-button"),

    /// A medium-emphasis filled tonal button.
    TONAL("m3-tonal-button"),

    /// An outlined button.
    OUTLINED("m3-outlined-button"),

    /// A low-emphasis text button.
    TEXT("m3-text-button"),

    /// An elevated button.
    ELEVATED("m3-elevated-button");

    /// The JavaFX style class used by this variant.
    private final String styleClass;

    /// Creates a button variant.
    M3ButtonVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this variant.
    public String getStyleClass() {
        return styleClass;
    }
}
