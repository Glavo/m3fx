package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 button.
@NotNullByDefault
public class M3Button extends Button {
    /// The base style class for all m3fx buttons.
    public static final String STYLE_CLASS = "m3-button";

    /// The button variant property.
    private final ObjectProperty<M3ButtonVariant> variant = new SimpleObjectProperty<>(this, "variant", M3ButtonVariant.FILLED) {
        /// Updates variant style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(M3ButtonVariant.FILLED);
                return;
            }
            updateVariantStyle();
        }
    };

    /// Creates an empty filled button.
    public M3Button() {
        this("");
    }

    /// Creates a filled button with text.
    public M3Button(String text) {
        super(text);
        initialize();
    }

    /// Creates a filled button with text and graphic content.
    public M3Button(String text, @Nullable Node graphic) {
        super(text, graphic);
        initialize();
    }

    /// Returns the button variant.
    public final M3ButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the button variant.
    public final void setVariant(M3ButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the button variant property.
    public final ObjectProperty<M3ButtonVariant> variantProperty() {
        return variant;
    }

    /// Creates the default animated Material Design 3 button skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ButtonSkin(this);
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
                M3ButtonVariant.FILLED.getStyleClass(),
                M3ButtonVariant.TONAL.getStyleClass(),
                M3ButtonVariant.OUTLINED.getStyleClass(),
                M3ButtonVariant.TEXT.getStyleClass(),
                M3ButtonVariant.ELEVATED.getStyleClass()
        );
    }
}
