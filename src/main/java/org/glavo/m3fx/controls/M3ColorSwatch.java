// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ColorMath;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ColorSwatchSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Provides a passive preview of a color value.
///
/// A partially transparent color is shown over a checkerboard. A `null` or fully transparent color is represented
/// by a no-color diagonal. This control does not select, activate, or modify its color and is not focus traversable
/// by default. Use [M3ColorSwatchPicker] when selectable swatches are required.
///
/// The accessible description is taken from [#colorNameProperty()] when that property contains a non-blank string.
/// Otherwise it is `"No color"` for no color or a fully transparent color, and an uppercase hexadecimal
/// description for any other color.
///
/// This is an M3FX color-selection extension. Material Design 3 does not define a corresponding standard component.
@NotNullByDefault
public final class M3ColorSwatch extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-color-swatch";

    /// Creates a passive, medium-sized swatch with no color.
    public M3ColorSwatch() {
        initialize();
    }

    /// Creates a passive, medium-sized swatch for a color.
    ///
    /// @param color the displayed color, or `null` for no color
    public M3ColorSwatch(@Nullable M3Color color) {
        this();
        setColor(color);
    }

    /// The displayed color, or `null` to display the no-color representation.
    ///
    /// Changing this property updates the rendered preview and the generated accessible description.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3Color> color =
            new SimpleObjectProperty<>(this, "color") {
                /// Requests painting and accessibility updates.
                @Override
                protected void invalidated() {
                    updateAccessibleText();
                    requestLayout();
                }
            };

    /// Returns the displayed color.
    ///
    /// @return the displayed color, or `null`
    public @Nullable M3Color getColor() {
        return color.get();
    }

    /// Sets the displayed color.
    ///
    /// @param color the color to display, or `null` for no color
    /// @throws RuntimeException if [#colorProperty()] is unidirectionally bound
    public void setColor(@Nullable M3Color color) {
        this.color.set(color);
    }

    /// Returns the property containing the displayed color.
    ///
    /// This property permits `null`. A unidirectional binding may supply either a color or `null`.
    ///
    /// @return the displayed-color property
    public ObjectProperty<@Nullable M3Color> colorProperty() {
        return color;
    }

    /// The optional human-readable name exposed to accessibility clients.
    ///
    /// A `null` or blank value selects the generated description documented by this class. A non-blank value is
    /// exposed unchanged.
    ///
    /// @defaultValue `null`
    private final StringProperty colorName = new SimpleStringProperty(this, "colorName") {
        /// Updates accessible text when the explicit name changes.
        @Override
        protected void invalidated() {
            updateAccessibleText();
        }
    };

    /// Returns the explicit accessible color name.
    ///
    /// @return the color name, or `null`
    public @Nullable String getColorName() {
        return colorName.get();
    }

    /// Sets the explicit accessible color name.
    ///
    /// @param colorName the color name, or `null` to use a generated description
    /// @throws RuntimeException if [#colorNameProperty()] is unidirectionally bound
    public void setColorName(@Nullable String colorName) {
        this.colorName.set(colorName);
    }

    /// Returns the property containing the explicit accessible color name.
    ///
    /// This property permits `null` and blank strings.
    ///
    /// @return the color-name property
    public StringProperty colorNameProperty() {
        return colorName;
    }

    /// The nominal square content size of the swatch.
    ///
    /// @defaultValue [M3ColorSwatchSize#MEDIUM]
    private final ObjectProperty<M3ColorSwatchSize> size = M3ColorProperties.nonNullObjectProperty(
            this,
            "size",
            M3ColorSwatchSize.MEDIUM,
            () -> {
                updateSizeStyle();
                requestLayout();
            }
    );

    /// Returns the nominal swatch size.
    ///
    /// @return the non-null size
    public M3ColorSwatchSize getSize() {
        return size.get();
    }

    /// Sets the nominal swatch size.
    ///
    /// @param size the non-null size
    /// @throws NullPointerException if `size` is `null`
    /// @throws RuntimeException if [#sizeProperty()] is unidirectionally bound
    public void setSize(M3ColorSwatchSize size) {
        this.size.set(size);
    }

    /// Returns the property containing the nominal swatch size.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the swatch-size property
    public ObjectProperty<M3ColorSwatchSize> sizeProperty() {
        return size;
    }

    /// The swatch corner treatment.
    ///
    /// @defaultValue [M3ColorSwatchRounding#DEFAULT]
    private final ObjectProperty<M3ColorSwatchRounding> rounding = M3ColorProperties.nonNullObjectProperty(
            this,
            "rounding",
            M3ColorSwatchRounding.DEFAULT,
            this::updateRoundingStyle
    );

    /// Returns the swatch corner treatment.
    ///
    /// @return the non-null corner treatment
    public M3ColorSwatchRounding getRounding() {
        return rounding.get();
    }

    /// Sets the swatch corner treatment.
    ///
    /// @param rounding the non-null corner treatment
    /// @throws NullPointerException if `rounding` is `null`
    /// @throws RuntimeException if [#roundingProperty()] is unidirectionally bound
    public void setRounding(M3ColorSwatchRounding rounding) {
        this.rounding.set(rounding);
    }

    /// Returns the property containing the corner treatment.
    ///
    /// A unidirectional binding must supply non-null values.
    ///
    /// @return the corner-treatment property
    public ObjectProperty<M3ColorSwatchRounding> roundingProperty() {
        return rounding;
    }

    /// Creates the default visual representation of this control.
    ///
    /// @return the non-null default skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ColorSwatchSkin(this);
    }

    /// Returns the user-agent stylesheet for color swatches.
    ///
    /// @return the color-swatch stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("color-swatch.css");
    }

    /// Initializes style and accessibility state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        setFocusTraversable(false);
        updateSizeStyle();
        updateRoundingStyle();
        updateAccessibleText();
    }

    /// Applies the current size style class.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                sizeStyleClass(getSize()),
                sizeStyleClass(M3ColorSwatchSize.EXTRA_SMALL),
                sizeStyleClass(M3ColorSwatchSize.SMALL),
                sizeStyleClass(M3ColorSwatchSize.MEDIUM),
                sizeStyleClass(M3ColorSwatchSize.LARGE)
        );
    }

    /// Applies the current corner-treatment style class.
    private void updateRoundingStyle() {
        M3ControlStyles.replaceVariant(
                this,
                roundingStyleClass(getRounding()),
                roundingStyleClass(M3ColorSwatchRounding.DEFAULT),
                roundingStyleClass(M3ColorSwatchRounding.NONE),
                roundingStyleClass(M3ColorSwatchRounding.FULL)
        );
    }

    /// Returns the style class associated with a swatch size.
    private static String sizeStyleClass(M3ColorSwatchSize size) {
        return switch (size) {
            case EXTRA_SMALL -> "m3-extra-small-color-swatch";
            case SMALL -> "m3-small-color-swatch";
            case MEDIUM -> "m3-medium-color-swatch";
            case LARGE -> "m3-large-color-swatch";
        };
    }

    /// Returns the style class associated with a corner treatment.
    private static String roundingStyleClass(M3ColorSwatchRounding rounding) {
        return switch (rounding) {
            case DEFAULT -> "m3-default-color-swatch-rounding";
            case NONE -> "m3-no-color-swatch-rounding";
            case FULL -> "m3-full-color-swatch-rounding";
        };
    }

    /// Updates the text exposed to accessibility clients.
    private void updateAccessibleText() {
        @Nullable String explicitName = getColorName();
        if (explicitName != null && !explicitName.isBlank()) {
            setAccessibleText(explicitName);
            return;
        }
        @Nullable M3Color currentColor = getColor();
        setAccessibleText(currentColor == null || currentColor.getAlpha() == 0.0
                ? "No color"
                : M3ColorMath.describe(currentColor));
    }
}
