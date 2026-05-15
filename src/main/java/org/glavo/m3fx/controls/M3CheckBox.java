package org.glavo.m3fx.controls;

import javafx.scene.control.CheckBox;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 checkbox.
@NotNullByDefault
public class M3CheckBox extends CheckBox {
    /// The base style class for m3fx checkboxes.
    public static final String STYLE_CLASS = "m3-checkbox";

    /// Creates an empty checkbox.
    public M3CheckBox() {
        initialize();
    }

    /// Creates a checkbox with text.
    public M3CheckBox(String text) {
        super(text);
        initialize();
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
