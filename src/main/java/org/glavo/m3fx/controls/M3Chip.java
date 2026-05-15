package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ToggleButton;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A Material Design 3 chip.
@NotNullByDefault
public class M3Chip extends ToggleButton {
    /// The base style class for m3fx chips.
    public static final String STYLE_CLASS = "m3-chip";

    /// The chip variant property.
    private final ObjectProperty<M3ChipVariant> variant = new SimpleObjectProperty<>(this, "variant", M3ChipVariant.ASSIST) {
        /// Updates variant style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(M3ChipVariant.ASSIST);
                return;
            }
            updateVariantStyle();
        }
    };

    /// Creates an empty assist chip.
    public M3Chip() {
        this("");
    }

    /// Creates an assist chip with text.
    public M3Chip(String text) {
        super(text);
        initialize();
    }

    /// Returns the chip variant.
    public final M3ChipVariant getVariant() {
        return variant.get();
    }

    /// Sets the chip variant.
    public final void setVariant(M3ChipVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the chip variant property.
    public final ObjectProperty<M3ChipVariant> variantProperty() {
        return variant;
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        updateVariantStyle();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3ChipVariant.ASSIST.getStyleClass(),
                M3ChipVariant.FILTER.getStyleClass(),
                M3ChipVariant.INPUT.getStyleClass(),
                M3ChipVariant.SUGGESTION.getStyleClass()
        );
    }
}
