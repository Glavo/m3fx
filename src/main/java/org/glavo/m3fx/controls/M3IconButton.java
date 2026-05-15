package org.glavo.m3fx.controls;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 icon button.
@NotNullByDefault
public class M3IconButton extends M3Button {
    /// The base style class for m3fx icon buttons.
    public static final String STYLE_CLASS = "m3-icon-button";

    /// Creates an icon button without a graphic.
    public M3IconButton() {
        this(nullGraphic());
    }

    /// Creates an icon button with a graphic.
    public M3IconButton(@Nullable Node graphic) {
        super("", graphic);
        M3ControlStyles.add(this, STYLE_CLASS);
        setVariant(M3ButtonVariant.TEXT);
    }

    /// Returns the default graphic value.
    private static @Nullable Node nullGraphic() {
        return null;
    }
}
