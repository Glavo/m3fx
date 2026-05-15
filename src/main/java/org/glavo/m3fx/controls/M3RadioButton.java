package org.glavo.m3fx.controls;

import javafx.scene.control.RadioButton;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 radio button.
@NotNullByDefault
public class M3RadioButton extends RadioButton {
    /// The base style class for m3fx radio buttons.
    public static final String STYLE_CLASS = "m3-radio-button";

    /// Creates an empty radio button.
    public M3RadioButton() {
        initialize();
    }

    /// Creates a radio button with text.
    public M3RadioButton(String text) {
        super(text);
        initialize();
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
