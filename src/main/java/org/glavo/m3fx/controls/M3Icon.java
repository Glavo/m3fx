// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.FontConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.theme.M3ComponentColorStyles;
import org.glavo.m3fx.skins.M3IconSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A token-aware font glyph for Material Design 3 controls.
///
/// `M3Icon` is an M3FX utility control rather than a standalone interactive component. Its
/// [glyph][#glyphProperty()] contains a character or symbol-font ligature, while [#sizeProperty()],
/// [#variantProperty()], and the styleable font token properties determine its metrics and color role. An explicit
/// [tint][#tintProperty()] overrides that role and any containing component color until cleared. The default icon is
/// empty, medium sized, uses the system font at medium weight, and uses the on-surface-variant color role.
///
/// The control renders exactly one glyph and does not provide label layout, graphics, mnemonic parsing, text
/// wrapping, or action behavior. It is centered and not focus traversable by default. Place it in an action-owning
/// control such as [M3IconButton], [M3NavigationItem], or [M3MenuItem] when the icon is interactive. Applications
/// that use a dedicated symbol font may set [#iconFontFamilyProperty()] on an individual icon or override the
/// corresponding CSS property for a subtree.
///
/// See [Material Design icons](https://m3.material.io/styles/icons/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3Icon extends Control implements M3IconGraphic {
    /// The base style class for M3FX icons.
    public static final String STYLE_CLASS = "m3-icon";

    /// The default icon font family token.
    private static final String DEFAULT_ICON_FONT_FAMILY = "System";

    /// The default icon size token.
    private static final double DEFAULT_ICON_SIZE = M3IconSize.MEDIUM.defaultSize();

    /// The default icon font weight token.
    private static final FontWeight DEFAULT_ICON_FONT_WEIGHT = FontWeight.MEDIUM;

    /// The layout line-box multiplier used to keep fallback font glyphs from being clipped.
    private static final double ICON_LINE_BOX_SCALE = 1.5;

    /// Creates an empty icon with the default medium size and on-surface-variant color role.
    public M3Icon() {
        this("");
    }

    /// Creates a medium icon containing a glyph or symbol-font ligature.
    ///
    /// @param glyph the glyph rendered by this icon
    /// @throws NullPointerException if `glyph` is `null`
    public M3Icon(String glyph) {
        initialize();
        setGlyph(glyph);
    }

    /// Creates an icon with a glyph, size role, and color variant.
    ///
    /// @param glyph   the glyph rendered by this icon
    /// @param size    the icon size role
    /// @param variant the icon color variant
    /// @throws NullPointerException if `glyph`, `size`, or `variant` is `null`
    public M3Icon(String glyph, M3IconSize size, M3IconVariant variant) {
        initialize();
        setGlyph(glyph);
        setSize(size);
        setVariant(variant);
    }

    /// The glyph or symbol-font ligature rendered by this icon.
    ///
    /// @defaultValue `""`
    private final StringProperty glyphValue = new SimpleStringProperty(this, "glyph", "");

    /// Returns the glyph or symbol-font ligature rendered by this icon.
    ///
    /// @return the current non-null glyph
    public String getGlyph() {
        return glyphValue.get();
    }

    /// Sets the glyph or symbol-font ligature rendered by this icon.
    ///
    /// @param glyph the new glyph
    /// @throws NullPointerException if `glyph` is `null`
    public void setGlyph(String glyph) {
        glyphValue.set(Objects.requireNonNull(glyph, "glyph"));
    }

    /// Returns the observable, bindable property containing the glyph rendered by this icon.
    ///
    /// The property defaults to an empty string and must contain a non-null value. Use an empty string for an icon
    /// with no visible glyph.
    ///
    /// @return the glyph property
    public StringProperty glyphProperty() {
        return glyphValue;
    }

    /// The semantic icon size role.
    ///
    /// A direct assignment of `null` is replaced with the default role. The role supplies the effective size until
    /// [#iconSizeProperty()] is explicitly initialized or styled.
    ///
    /// @defaultValue [M3IconSize#MEDIUM]
    private final ObjectProperty<M3IconSize> sizeValue =
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

    /// Returns the icon size role.
    ///
    /// @return the icon size role
    public M3IconSize getSize() {
        return sizeValue.get();
    }

    /// Sets the icon size role.
    ///
    /// @param size the icon size role
    /// @throws NullPointerException if `size` is `null`
    public void setSize(M3IconSize size) {
        sizeValue.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the observable, bindable property containing the semantic icon size role.
    ///
    /// The property defaults to [M3IconSize#MEDIUM]. A `null` value assigned directly through the property is
    /// replaced with that default.
    ///
    /// @return the icon size-role property
    public ObjectProperty<M3IconSize> sizeProperty() {
        return sizeValue;
    }

    /// The Material color role used to paint the glyph.
    ///
    /// A direct assignment of `null` is replaced with the default variant.
    ///
    /// @defaultValue [M3IconVariant#ON_SURFACE_VARIANT]
    private final ObjectProperty<M3IconVariant> variantValue =
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

    /// Returns the icon color variant.
    ///
    /// @return the icon color variant
    public M3IconVariant getVariant() {
        return variantValue.get();
    }

    /// Sets the icon color variant.
    ///
    /// @param variant the icon color variant
    /// @throws NullPointerException if `variant` is `null`
    public void setVariant(M3IconVariant variant) {
        variantValue.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the observable, bindable property containing the icon color variant.
    ///
    /// The property defaults to [M3IconVariant#ON_SURFACE_VARIANT]. A `null` value assigned directly through the
    /// property is replaced with that default.
    ///
    /// @return the icon color-variant property
    public ObjectProperty<M3IconVariant> variantProperty() {
        return variantValue;
    }

    /// The explicit tint used to paint the glyph, or `null` to use semantic color resolution.
    ///
    /// A non-null tint takes precedence over [#variantProperty()] and over the color supplied by a containing M3FX
    /// component. Clearing the tint restores those normal rules without changing the semantic variant.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Color> tintValue =
            new SimpleObjectProperty<>(this, "tint") {
                /// Updates the branch-local tint declaration.
                @Override
                protected void invalidated() {
                    M3ComponentColorStyles.applyIconTint(M3Icon.this, get());
                }
            };

    /// Returns the explicit icon tint.
    ///
    /// @return the tint, or `null` when semantic color resolution is active
    public @Nullable Color getTint() {
        return tintValue.get();
    }

    /// Sets the explicit icon tint.
    ///
    /// @param tint the tint to apply, or `null` to restore semantic color resolution
    public void setTint(@Nullable Color tint) {
        tintValue.set(tint);
    }

    /// Returns the observable property that stores the explicit icon tint.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the nullable tint property
    public ObjectProperty<@Nullable Color> tintProperty() {
        return tintValue;
    }

    /// The resolved immutable font used by the glyph node.
    private final ReadOnlyObjectWrapper<Font> resolvedIconFont = new ReadOnlyObjectWrapper<>(
            this,
            "iconFont",
            Font.font(DEFAULT_ICON_FONT_FAMILY, DEFAULT_ICON_FONT_WEIGHT, DEFAULT_ICON_SIZE)
    );

    /// Returns the resolved font currently used to render the glyph.
    ///
    /// The returned font reflects [#iconFontFamilyProperty()], [#iconSizeProperty()], and
    /// [#iconFontWeightProperty()]. It is derived state and cannot be replaced independently of those token
    /// properties.
    ///
    /// @return the resolved icon font
    public Font getIconFont() {
        return resolvedIconFont.get();
    }

    /// Returns the observable read-only property containing the resolved icon font.
    ///
    /// The property is a binding source but cannot be set or bound as a writable target. Its initial value uses the
    /// system font, medium weight, and the default medium icon size.
    ///
    /// @return the resolved icon-font property
    public ReadOnlyObjectProperty<Font> iconFontProperty() {
        return resolvedIconFont.getReadOnlyProperty();
    }

    /// The styleable font family used to resolve the glyph.
    ///
    /// The property does not retain a directly assigned `null` value.
    ///
    /// @defaultValue `System`
    private @Nullable StyleableObjectProperty<@Nullable String> iconFontFamilyValue;

    /// Returns the icon font family token.
    ///
    /// @return the icon font family token
    public String getIconFontFamily() {
        return iconFontFamilyValue == null
                ? DEFAULT_ICON_FONT_FAMILY
                : Objects.requireNonNullElse(iconFontFamilyValue.get(), DEFAULT_ICON_FONT_FAMILY);
    }

    /// Sets the icon font family token.
    ///
    /// @param iconFontFamily the icon font family token
    /// @throws NullPointerException if `iconFontFamily` is `null`
    public void setIconFontFamily(String iconFontFamily) {
        iconFontFamilyProperty().set(Objects.requireNonNull(iconFontFamily, "iconFontFamily"));
    }

    /// Returns the observable, bindable, styleable property containing the icon font family token.
    ///
    /// The property defaults to `System`. A `null` value assigned directly through the property is replaced with
    /// that default. If a binding source provides `null`, the property remains `null` while the effective family
    /// reported by [#getIconFontFamily()] falls back to `System`. CSS cannot set the property while it is bound.
    ///
    /// @return the icon font-family property
    public StyleableObjectProperty<@Nullable String> iconFontFamilyProperty() {
        if (iconFontFamilyValue == null) {
            iconFontFamilyValue = M3Css.styleableObjectProperty(
                    DEFAULT_ICON_FONT_FAMILY,
                    this,
                    "iconFontFamily",
                    StyleableProperties.ICON_FONT_FAMILY,
                    () -> {
                        if (iconFontFamilyProperty().get() == null) {
                            if (!iconFontFamilyProperty().isBound()) {
                                iconFontFamilyProperty().set(DEFAULT_ICON_FONT_FAMILY);
                                return;
                            }
                        }
                        updateIconFont();
                    }
            );
        }
        return iconFontFamilyValue;
    }

    /// The styleable glyph size in logical pixels.
    ///
    /// The value must be finite and non-negative. Before this property is initialized, the effective value is
    /// supplied by [#sizeProperty()]; once initialized, this property is the explicit size override.
    private @Nullable StyleableDoubleProperty iconSizeValue;

    /// Returns the effective glyph size.
    ///
    /// @return the glyph size in logical pixels
    public double getIconSize() {
        return iconSizeValue == null ? getSize().defaultSize() : iconSizeValue.get();
    }

    /// Sets an explicit glyph size, overriding the size supplied by [#sizeProperty()].
    ///
    /// @param iconSize the glyph size in logical pixels
    /// @throws IllegalArgumentException if `iconSize` is negative or not finite
    public void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the observable, bindable, styleable property containing the effective glyph size.
    ///
    /// On first access, the property is initialized from the current [#sizeProperty()] role. It then accepts only
    /// finite, non-negative logical-pixel values and acts as an explicit size override. CSS cannot set the property
    /// while it is bound.
    ///
    /// @return the icon-size property
    public StyleableDoubleProperty iconSizeProperty() {
        if (iconSizeValue == null) {
            iconSizeValue = M3Css.nonNegativeStyleableDoubleProperty(
                    getSize().defaultSize(),
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    () -> {
                        updateIconFont();
                        updateMetrics();
                    }
            );
        }
        return iconSizeValue;
    }

    /// The styleable glyph font weight.
    ///
    /// Values assigned through [#setIconFontWeight(double)] are rounded to the nearest supported JavaFX font
    /// weight. The property does not retain a directly assigned `null` value.
    ///
    /// @defaultValue `500`
    private @Nullable StyleableObjectProperty<@Nullable FontWeight> iconFontWeightValue;

    /// Returns the effective numeric font weight.
    ///
    /// @return the icon font weight token
    public double getIconFontWeight() {
        return getIconFontWeightValue().getWeight();
    }

    /// Sets the numeric font weight used to resolve the glyph font.
    ///
    /// @param iconFontWeight the font weight in the inclusive range `1.0` through `1000.0`
    /// @throws IllegalArgumentException if `iconFontWeight` is not finite, less than `1.0`, or greater than `1000.0`
    public void setIconFontWeight(double iconFontWeight) {
        iconFontWeightProperty().set(validateFontWeight(iconFontWeight));
    }

    /// Returns the observable, bindable, styleable property containing the resolved JavaFX font weight.
    ///
    /// The property defaults to [FontWeight#MEDIUM]. A `null` value assigned directly through the property is
    /// replaced with that default. If a binding source provides `null`, the property remains `null` while the
    /// effective weight reported by [#getIconFontWeight()] falls back to [FontWeight#MEDIUM]. CSS cannot set the
    /// property while it is bound.
    ///
    /// @return the icon font-weight property
    public StyleableObjectProperty<@Nullable FontWeight> iconFontWeightProperty() {
        if (iconFontWeightValue == null) {
            iconFontWeightValue = M3Css.styleableObjectProperty(
                    DEFAULT_ICON_FONT_WEIGHT,
                    this,
                    "iconFontWeight",
                    StyleableProperties.ICON_FONT_WEIGHT,
                    () -> {
                        if (iconFontWeightProperty().get() == null) {
                            if (!iconFontWeightProperty().isBound()) {
                                iconFontWeightProperty().set(DEFAULT_ICON_FONT_WEIGHT);
                                return;
                            }
                        }
                        updateIconFont();
                    }
            );
        }
        return iconFontWeightValue;
    }

    /// Returns the user-agent stylesheet for M3FX icons.
    ///
    /// @return the icon user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("icon.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the immutable CSS metadata list
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default Material Design 3 icon skin.
    ///
    /// @return a new icon skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3IconSkin(this);
    }

    /// Initializes style classes, accessibility, and resolved token state.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        getStyleClass().add(M3IconGraphic.STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
        setFocusTraversable(false);
        updateSizeStyle();
        updateVariantStyle();
        updateIconFont();
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
        if (iconSizeValue == null) {
            updateIconFont();
            updateMetrics();
        }
    }

    /// Applies the style class for the selected icon color role.
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

    /// Updates the derived font consumed by the icon skin.
    private void updateIconFont() {
        Font resolvedFont = Font.font(
                getIconFontFamily(),
                getIconFontWeightValue(),
                getIconSize()
        );
        if (!resolvedFont.equals(resolvedIconFont.get())) {
            resolvedIconFont.set(resolvedFont);
        }
    }

    /// Returns the resolved icon font weight token.
    private FontWeight getIconFontWeightValue() {
        return iconFontWeightValue == null
                ? DEFAULT_ICON_FONT_WEIGHT
                : Objects.requireNonNullElse(iconFontWeightValue.get(), DEFAULT_ICON_FONT_WEIGHT);
    }

    /// Applies the effective icon size to the control line box.
    private void updateMetrics() {
        double lineBoxSize = Math.ceil(getIconSize() * ICON_LINE_BOX_SCALE);
        setMinSize(lineBoxSize, lineBoxSize);
        setPrefSize(lineBoxSize, lineBoxSize);
    }

    /// Validates a numeric font weight token.
    ///
    /// @param value the numeric font weight
    /// @return the nearest JavaFX font weight
    /// @throws IllegalArgumentException if the value is not finite or is outside the inclusive range `1.0` through
    ///                                  `1000.0`
    private static FontWeight validateFontWeight(double value) {
        if (!Double.isFinite(value) || value < 1.0 || value > 1000.0) {
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
                    ///
                    /// @param control the target icon
                    /// @return whether CSS may assign the property
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return M3Css.isSettable(control.iconFontFamilyProperty());
                    }

                    /// Returns the styleable property for an icon.
                    ///
                    /// @param control the target icon
                    /// @return the icon font-family property
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
                    ///
                    /// @param control the target icon
                    /// @return whether CSS may assign the property
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for an icon.
                    ///
                    /// @param control the target icon
                    /// @return the icon-size property
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Icon control) {
                        return control.iconSizeProperty();
                    }
                };

        /// CSS metadata for the icon font-weight token.
        private static final CssMetaData<M3Icon, @Nullable FontWeight> ICON_FONT_WEIGHT =
                new CssMetaData<>(
                        "-m3-icon-font-weight",
                        FontConverter.FontWeightConverter.getInstance(),
                        DEFAULT_ICON_FONT_WEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    ///
                    /// @param control the target icon
                    /// @return whether CSS may assign the property
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return M3Css.isSettable(control.iconFontWeightProperty());
                    }

                    /// Returns the styleable property for an icon.
                    ///
                    /// @param control the target icon
                    /// @return the icon font-weight property
                    @Override
                    public StyleableProperty<@Nullable FontWeight> getStyleableProperty(M3Icon control) {
                        return control.iconFontWeightProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ICON_FONT_FAMILY);
            styleables.add(ICON_SIZE);
            styleables.add(ICON_FONT_WEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents utility class instantiation.
        private StyleableProperties() {
        }
    }
}
