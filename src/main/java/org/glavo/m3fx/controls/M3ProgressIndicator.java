package org.glavo.m3fx.controls;

import javafx.scene.control.ProgressIndicator;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 circular progress indicator.
@NotNullByDefault
public class M3ProgressIndicator extends ProgressIndicator {
    /// The base style class for m3fx progress indicators.
    public static final String STYLE_CLASS = "m3-progress-indicator";

    /// Creates an indeterminate progress indicator.
    public M3ProgressIndicator() {
        initialize();
    }

    /// Creates a progress indicator with an initial progress value.
    public M3ProgressIndicator(double progress) {
        super(progress);
        initialize();
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
