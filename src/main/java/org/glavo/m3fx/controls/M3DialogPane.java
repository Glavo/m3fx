package org.glavo.m3fx.controls;

import javafx.scene.control.DialogPane;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 dialog pane.
@NotNullByDefault
public class M3DialogPane extends DialogPane {
    /// The base style class for m3fx dialog panes.
    public static final String STYLE_CLASS = "m3-dialog-pane";

    /// Creates a dialog pane.
    public M3DialogPane() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
