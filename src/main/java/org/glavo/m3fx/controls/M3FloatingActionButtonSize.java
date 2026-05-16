package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the size variant of an m3fx floating action button.
@NotNullByDefault
public enum M3FloatingActionButtonSize {
    /// A compact floating action button.
    SMALL("m3-small-fab"),

    /// The default floating action button size.
    REGULAR("m3-regular-fab"),

    /// A prominent floating action button.
    LARGE("m3-large-fab");

    /// The JavaFX style class used by this size.
    private final String styleClass;

    /// Creates a floating action button size.
    M3FloatingActionButtonSize(String styleClass) {
        this.styleClass = styleClass;
    }

    /// Returns the JavaFX style class used by this size.
    public String getStyleClass() {
        return styleClass;
    }
}
