// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.FontConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3IconSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A token-aware icon label for Material Design 3 controls.
///
/// `M3Icon` is an M3FX utility control rather than a standalone interactive component. Its [textProperty]
/// contains the glyph text to render, while [sizeProperty], [variantProperty], and the styleable font properties
/// select the Material metrics and color role. The default icon is empty, medium sized, uses the system font at
/// medium weight, and uses the on-surface-variant color role.
///
/// An icon is centered and is not focus traversable by default. Place it in an action-owning control such as
/// [M3IconButton], [M3NavigationItem], or [M3MenuItem] when the icon is interactive. Applications that use a
/// dedicated symbol font may set [iconFontFamilyProperty] on an individual icon or override the corresponding
/// CSS property for a subtree.
///
/// See [Material Design icons](https://m3.material.io/styles/icons/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3Icon extends Labeled {
    /// The base style class for M3FX icon labels.
    public static final String STYLE_CLASS = "m3-icon";

    /// The default icon font family token.
    private static final String DEFAULT_ICON_FONT_FAMILY = "System";

    /// The default icon size token.
    private static final double DEFAULT_ICON_SIZE = M3IconSize.MEDIUM.defaultSize();

    /// The default icon font weight token.
    private static final FontWeight DEFAULT_ICON_FONT_WEIGHT = FontWeight.MEDIUM;

    /// The layout line box multiplier used to keep fallback font glyphs from being clipped.
    private static final double ICON_LINE_BOX_SCALE = 1.5;

    /// The semantic icon size role.
    ///
    /// A direct assignment of `null` is replaced with the default role. The role supplies the effective size until
    /// [iconSizeProperty] is explicitly initialized or styled.
    ///
    /// @defaultValue [M3IconSize#MEDIUM]
    private final ObjectProperty<M3IconSize> size =
            new SimpleObjectProperty<>(this, "size", M3IconSize.MEDIUM) {
                /// Updates icon size style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3IconSize.MEDIUM);
                        return;
                    }
                    updateSizeStyle();
                }
            };

    /// The Material color role used to paint the glyph.
    ///
    /// A direct assignment of `null` is replaced with the default variant.
    ///
    /// @defaultValue [M3IconVariant#ON_SURFACE_VARIANT]
    private final ObjectProperty<M3IconVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3IconVariant.ON_SURFACE_VARIANT) {
                /// Updates icon color style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3IconVariant.ON_SURFACE_VARIANT);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// The styleable font family used to resolve the glyph.
    ///
    /// The property does not retain a directly assigned `null` value.
    ///
    /// @defaultValue `System`
    private @Nullable StyleableObjectProperty<@Nullable String> iconFontFamily;

    /// The styleable glyph size in logical pixels.
    ///
    /// The value must be finite and non-negative. Before this property is initialized, the effective value is
    /// supplied by [sizeProperty]; once initialized, this property is the explicit size override.
    private @Nullable StyleableDoubleProperty iconSize;

    /// The styleable glyph font weight.
    ///
    /// Values assigned through [setIconFontWeight] are rounded to the nearest supported JavaFX font weight. The
    /// property does not retain a directly assigned `null` value.
    ///
    /// @defaultValue `500`
    private @Nullable StyleableObjectProperty<@Nullable FontWeight> iconFontWeight;

    /// Creates an empty icon with the default medium size and on-surface-variant color role.
    public M3Icon() {
        this("");
    }

    /// Creates a medium icon with text content.
    ///
    /// @param text the glyph text rendered by this icon
    /// @throws NullPointerException if `text` is `null`
    public M3Icon(String text) {
        initialize();
        setText(Objects.requireNonNull(text, "text"));
    }

    /// Creates an icon with text content, size, and color variant.
    ///
    /// @param text the glyph text rendered by this icon
    /// @param size the icon size role
    /// @param variant the icon color variant
    /// @throws NullPointerException if `text`, `size`, or `variant` is `null`
    public M3Icon(String text, M3IconSize size, M3IconVariant variant) {
        initialize();
        setText(Objects.requireNonNull(text, "text"));
        setSize(size);
        setVariant(variant);
    }

    /// Returns the icon size role.
    ///
    /// @return the icon size role
    public final M3IconSize getSize() {
        return size.get();
    }

    /// Sets the icon size role.
    ///
    /// @param size the icon size role
    /// @throws NullPointerException if `size` is `null`
    public final void setSize(M3IconSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    public final ObjectProperty<M3IconSize> sizeProperty() {
        return size;
    }

    /// Returns the icon color variant.
    ///
    /// @return the icon color variant
    public final M3IconVariant getVariant() {
        return variant.get();
    }

    /// Sets the icon color variant.
    ///
    /// @param variant the icon color variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3IconVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    public final ObjectProperty<M3IconVariant> variantProperty() {
        return variant;
    }

    /// Returns the icon font family token.
    ///
    /// @return the icon font family token
    public final String getIconFontFamily() {
        return iconFontFamily == null
                ? DEFAULT_ICON_FONT_FAMILY
                : Objects.requireNonNullElse(iconFontFamily.get(), DEFAULT_ICON_FONT_FAMILY);
    }

    /// Sets the icon font family token.
    ///
    /// @param iconFontFamily the icon font family token
    /// @throws NullPointerException if `iconFontFamily` is `null`
    public final void setIconFontFamily(String iconFontFamily) {
        iconFontFamilyProperty().set(Objects.requireNonNull(iconFontFamily, "iconFontFamily"));
    }

    public final StyleableObjectProperty<@Nullable String> iconFontFamilyProperty() {
        if (iconFontFamily == null) {
            iconFontFamily = M3Css.styleableObjectProperty(
                    DEFAULT_ICON_FONT_FAMILY,
                    this,
                    "iconFontFamily",
                    StyleableProperties.ICON_FONT_FAMILY,
                    () -> {
                        if (iconFontFamilyProperty().get() == null) {
                            iconFontFamilyProperty().set(DEFAULT_ICON_FONT_FAMILY);
                            return;
                        }
                        updateFont();
                    }
            );
        }
        return iconFontFamily;
    }

    /// Returns the effective glyph size.
    ///
    /// @return the glyph size in logical pixels
    public final double getIconSize() {
        return iconSize == null ? getSize().defaultSize() : iconSize.get();
    }

    /// Sets an explicit glyph size, overriding the size supplied by [sizeProperty].
    ///
    /// @param iconSize the glyph size in logical pixels
    /// @throws IllegalArgumentException if `iconSize` is negative or not finite
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    () -> {
                        updateFont();
                        updateMetrics();
                    }
            );
        }
        return iconSize;
    }

    /// Returns the effective numeric font weight.
    ///
    /// @return the icon font weight token
    public final double getIconFontWeight() {
        return getIconFontWeightValue().getWeight();
    }

    /// Sets the numeric font weight used to resolve the glyph font.
    ///
    /// @param iconFontWeight the font weight in the inclusive range `1.0` through `1000.0`
    /// @throws IllegalArgumentException if `iconFontWeight` is less than `1.0` or greater than `1000.0`
    public final void setIconFontWeight(double iconFontWeight) {
        iconFontWeightProperty().set(validateFontWeight(iconFontWeight));
    }

    public final StyleableObjectProperty<@Nullable FontWeight> iconFontWeightProperty() {
        if (iconFontWeight == null) {
            iconFontWeight = M3Css.styleableObjectProperty(
                    DEFAULT_ICON_FONT_WEIGHT,
                    this,
                    "iconFontWeight",
                    StyleableProperties.ICON_FONT_WEIGHT,
                    () -> {
                        if (iconFontWeightProperty().get() == null) {
                            iconFontWeightProperty().set(DEFAULT_ICON_FONT_WEIGHT);
                            return;
                        }
                        updateFont();
                    }
            );
        }
        return iconFontWeight;
    }

    /// Returns the user-agent stylesheet for M3FX icon labels.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("icon.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default Material Design 3 icon skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3IconSkin(this);
    }

    /// Initializes style classes, layout, and font defaults.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
        setFocusTraversable(false);
        setAlignment(Pos.CENTER);
        updateSizeStyle();
        updateVariantStyle();
        updateFont();
        updateMetrics();
    }

    /// Applies the style class for the selected icon size.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().styleClass(),
                M3IconSize.SMALL.styleClass(),
                M3IconSize.MEDIUM.styleClass(),
                M3IconSize.LARGE.styleClass(),
                M3IconSize.EXTRA_LARGE.styleClass()
        );
        if (iconSize == null) {
            updateFont();
            updateMetrics();
        }
    }

    /// Applies the style class for the selected icon color variant.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3IconVariant.PRIMARY.styleClass(),
                M3IconVariant.SECONDARY.styleClass(),
                M3IconVariant.TERTIARY.styleClass(),
                M3IconVariant.ERROR.styleClass(),
                M3IconVariant.ON_SURFACE.styleClass(),
                M3IconVariant.ON_SURFACE_VARIANT.styleClass(),
                M3IconVariant.INVERSE_ON_SURFACE.styleClass()
        );
    }

    /// Applies resolved icon font tokens to the inherited font property.
    private void updateFont() {
        setFont(Font.font(
                getIconFontFamily(),
                getIconFontWeightValue(),
                getIconSize()
        ));
    }

    /// Returns the resolved icon font weight token.
    private FontWeight getIconFontWeightValue() {
        return iconFontWeight == null
                ? DEFAULT_ICON_FONT_WEIGHT
                : Objects.requireNonNullElse(iconFontWeight.get(), DEFAULT_ICON_FONT_WEIGHT);
    }

    /// Applies icon size tokens to layout metrics.
    private void updateMetrics() {
        double size = getIconSize();
        double lineBoxSize = Math.ceil(size * ICON_LINE_BOX_SCALE);
        setMinSize(lineBoxSize, lineBoxSize);
        setPrefSize(lineBoxSize, lineBoxSize);
    }

    /// Validates a font weight token.
    private static FontWeight validateFontWeight(double value) {
        if (value < 1.0 || value > 1000.0) {
            throw new IllegalArgumentException("iconFontWeight must be between 1 and 1000");
        }
        return FontWeight.findByWeight((int) Math.round(value));
    }

    /// CSS metadata for M3FX icon tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the icon font family token.
        private static final CssMetaData<M3Icon, @Nullable String> ICON_FONT_FAMILY =
                new CssMetaData<>(
                        "-m3-icon-font-family",
                        StringConverter.getInstance(),
                        DEFAULT_ICON_FONT_FAMILY
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return M3Css.isSettable(control.iconFontFamilyProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<@Nullable String> getStyleableProperty(M3Icon control) {
                        return control.iconFontFamilyProperty();
                    }
                };

        /// CSS metadata for the icon size token.
        private static final CssMetaData<M3Icon, Number> ICON_SIZE =
                new CssMetaData<>(
                        "-m3-icon-size",
                        SizeConverter.getInstance(),
                        DEFAULT_ICON_SIZE
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Icon control) {
                        return control.iconSizeProperty();
                    }
                };

        /// CSS metadata for the icon font weight token.
        private static final CssMetaData<M3Icon, @Nullable FontWeight> ICON_FONT_WEIGHT =
                new CssMetaData<>(
                        "-m3-icon-font-weight",
                        FontConverter.FontWeightConverter.getInstance(),
                        DEFAULT_ICON_FONT_WEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return M3Css.isSettable(control.iconFontWeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<@Nullable FontWeight> getStyleableProperty(M3Icon control) {
                        return control.iconFontWeightProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Labeled.getClassCssMetaData());
            styleables.add(ICON_FONT_FAMILY);
            styleables.add(ICON_SIZE);
            styleables.add(ICON_FONT_WEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
