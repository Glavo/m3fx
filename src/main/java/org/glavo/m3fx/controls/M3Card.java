package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.skins.M3CardSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 card container.
@NotNullByDefault
public class M3Card extends Control {
    /// The base style class for m3fx cards.
    public static final String STYLE_CLASS = "m3-card";

    /// The card content node property.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// The card variant property.
    private final ObjectProperty<M3CardVariant> variant = new SimpleObjectProperty<>(this, "variant", M3CardVariant.FILLED) {
        /// Updates variant style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(M3CardVariant.FILLED);
                return;
            }
            updateVariantStyle();
        }
    };

    /// Creates an empty filled card.
    public M3Card() {
        this(null);
    }

    /// Creates a filled card with content.
    public M3Card(@Nullable Node content) {
        M3ControlStyles.add(this, STYLE_CLASS);
        setContent(content);
        updateVariantStyle();
    }

    /// Returns the card content node.
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the card content node.
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the card content property.
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the card variant.
    public final M3CardVariant getVariant() {
        return variant.get();
    }

    /// Sets the card variant.
    public final void setVariant(M3CardVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the card variant property.
    public final ObjectProperty<M3CardVariant> variantProperty() {
        return variant;
    }

    /// Creates the default card skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3CardSkin(this);
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3CardVariant.ELEVATED.getStyleClass(),
                M3CardVariant.FILLED.getStyleClass(),
                M3CardVariant.OUTLINED.getStyleClass()
        );
    }
}
