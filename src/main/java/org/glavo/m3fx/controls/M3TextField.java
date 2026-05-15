package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A Material Design 3 text field.
@NotNullByDefault
public class M3TextField extends TextField {
    /// The base style class for m3fx text fields.
    public static final String STYLE_CLASS = "m3-text-field";

    /// The visual variant property.
    private final ObjectProperty<M3TextInputVariant> variant = new SimpleObjectProperty<>(this, "variant", M3TextInputVariant.FILLED) {
        /// Updates variant style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(M3TextInputVariant.FILLED);
                return;
            }
            updateVariantStyle();
        }
    };

    /// Creates an empty filled text field.
    public M3TextField() {
        initialize();
    }

    /// Creates a filled text field with initial text.
    public M3TextField(String text) {
        super(text);
        initialize();
    }

    /// Returns the text input variant.
    public final M3TextInputVariant getVariant() {
        return variant.get();
    }

    /// Sets the text input variant.
    public final void setVariant(M3TextInputVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the text input variant property.
    public final ObjectProperty<M3TextInputVariant> variantProperty() {
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
                M3TextInputVariant.FILLED.getStyleClass(),
                M3TextInputVariant.OUTLINED.getStyleClass()
        );
    }
}
