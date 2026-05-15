package org.glavo.m3fx.controls;

import javafx.scene.control.Slider;
import org.jetbrains.annotations.NotNullByDefault;

/// A Material Design 3 slider.
@NotNullByDefault
public class M3Slider extends Slider {
    /// The base style class for m3fx sliders.
    public static final String STYLE_CLASS = "m3-slider";

    /// Creates a slider with the JavaFX default range.
    public M3Slider() {
        initialize();
    }

    /// Creates a slider with a range and initial value.
    public M3Slider(double min, double max, double value) {
        super(min, max, value);
        initialize();
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
