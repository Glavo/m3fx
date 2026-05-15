package org.glavo.m3fx.controls;

import javafx.scene.control.ProgressBar;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 linear progress indicator.
@NotNullByDefault
public class M3ProgressBar extends ProgressBar {
    /// The base style class for m3fx progress bars.
    public static final String STYLE_CLASS = "m3-progress-bar";

    /// Creates an indeterminate progress bar.
    public M3ProgressBar() {
        initialize();
    }

    /// Creates a progress bar with an initial progress value.
    public M3ProgressBar(double progress) {
        super(progress);
        initialize();
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
