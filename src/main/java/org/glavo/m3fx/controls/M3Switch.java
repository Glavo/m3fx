package org.glavo.m3fx.controls;

import javafx.scene.control.CheckBox;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 switch.
@NotNullByDefault
public class M3Switch extends CheckBox {
    /// The base style class for m3fx switches.
    public static final String STYLE_CLASS = "m3-switch";

    /// Creates an empty switch.
    public M3Switch() {
        initialize();
    }

    /// Creates a switch with text.
    public M3Switch(String text) {
        super(text);
        initialize();
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
