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
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3IconSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 icon label driven by size and color tokens.
@NotNullByDefault
public class M3Icon extends Labeled {
    /// The base style class for M3FX icon labels.
    public static final String STYLE_CLASS = "m3-icon";

    /// The default icon font family token.
    private static final String DEFAULT_ICON_FONT_FAMILY = "System";

    /// The default icon size token.
    private static final double DEFAULT_ICON_SIZE = M3IconSize.MEDIUM.getDefaultSize();

    /// The default icon font weight token.
    private static final FontWeight DEFAULT_ICON_FONT_WEIGHT = FontWeight.MEDIUM;

    /// The icon size role property.
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

    /// The icon color variant property.
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

    /// The styleable icon font family token.
    private StyleableObjectProperty<String> iconFontFamily;

    /// The styleable icon size token.
    private StyleableDoubleProperty iconSize;

    /// The styleable icon font weight token.
    private StyleableObjectProperty<FontWeight> iconFontWeight;

    /// Creates an empty medium icon.
    public M3Icon() {
        this("");
    }

    /// Creates a medium icon with text content.
    public M3Icon(String text) {
        initialize();
        setText(Objects.requireNonNull(text, "text"));
    }

    /// Creates an icon with text content, size, and color variant.
    public M3Icon(String text, M3IconSize size, M3IconVariant variant) {
        initialize();
        setText(Objects.requireNonNull(text, "text"));
        setSize(size);
        setVariant(variant);
    }

    /// Returns the icon size role.
    public final M3IconSize getSize() {
        return size.get();
    }

    /// Sets the icon size role.
    public final void setSize(M3IconSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the icon size role property.
    public final ObjectProperty<M3IconSize> sizeProperty() {
        return size;
    }

    /// Returns the icon color variant.
    public final M3IconVariant getVariant() {
        return variant.get();
    }

    /// Sets the icon color variant.
    public final void setVariant(M3IconVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the icon color variant property.
    public final ObjectProperty<M3IconVariant> variantProperty() {
        return variant;
    }

    /// Returns the icon font family token.
    public final String getIconFontFamily() {
        return iconFontFamily == null ? DEFAULT_ICON_FONT_FAMILY : iconFontFamily.get();
    }

    /// Sets the icon font family token.
    public final void setIconFontFamily(String iconFontFamily) {
        iconFontFamilyProperty().set(Objects.requireNonNull(iconFontFamily, "iconFontFamily"));
    }

    /// Returns the icon font family token property.
    public final StyleableObjectProperty<String> iconFontFamilyProperty() {
        if (iconFontFamily == null) {
            iconFontFamily = new StyleableObjectProperty<>(DEFAULT_ICON_FONT_FAMILY) {
                /// Applies updated icon font family tokens.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_ICON_FONT_FAMILY);
                        return;
                    }
                    updateFont();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Icon.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "iconFontFamily";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Icon, String> getCssMetaData() {
                    return StyleableProperties.ICON_FONT_FAMILY;
                }
            };
        }
        return iconFontFamily;
    }

    /// Returns the icon size token.
    public final double getIconSize() {
        return iconSize == null ? getSize().getDefaultSize() : iconSize.get();
    }

    /// Sets the icon size token.
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the icon size token property.
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = new StyleableDoubleProperty(DEFAULT_ICON_SIZE) {
                /// Applies updated icon size tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "iconSize");
                    updateFont();
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Icon.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "iconSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Icon, Number> getCssMetaData() {
                    return StyleableProperties.ICON_SIZE;
                }
            };
        }
        return iconSize;
    }

    /// Returns the icon font weight token.
    public final double getIconFontWeight() {
        return getIconFontWeightValue().getWeight();
    }

    /// Sets the icon font weight token.
    public final void setIconFontWeight(double iconFontWeight) {
        iconFontWeightProperty().set(validateFontWeight(iconFontWeight));
    }

    /// Returns the icon font weight token property.
    public final StyleableObjectProperty<FontWeight> iconFontWeightProperty() {
        if (iconFontWeight == null) {
            iconFontWeight = new StyleableObjectProperty<>(DEFAULT_ICON_FONT_WEIGHT) {
                /// Applies updated icon font weight tokens.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_ICON_FONT_WEIGHT);
                        return;
                    }
                    updateFont();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Icon.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "iconFontWeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Icon, FontWeight> getCssMetaData() {
                    return StyleableProperties.ICON_FONT_WEIGHT;
                }
            };
        }
        return iconFontWeight;
    }

    /// Returns the user-agent stylesheet for M3FX icon labels.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("icon.css");
    }

    /// Returns the CSS metadata for this control class.
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
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
                getSize().getStyleClass(),
                M3IconSize.SMALL.getStyleClass(),
                M3IconSize.MEDIUM.getStyleClass(),
                M3IconSize.LARGE.getStyleClass(),
                M3IconSize.EXTRA_LARGE.getStyleClass()
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
                getVariant().getStyleClass(),
                M3IconVariant.PRIMARY.getStyleClass(),
                M3IconVariant.SECONDARY.getStyleClass(),
                M3IconVariant.TERTIARY.getStyleClass(),
                M3IconVariant.ERROR.getStyleClass(),
                M3IconVariant.ON_SURFACE.getStyleClass(),
                M3IconVariant.ON_SURFACE_VARIANT.getStyleClass(),
                M3IconVariant.INVERSE_ON_SURFACE.getStyleClass()
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
        return iconFontWeight == null ? DEFAULT_ICON_FONT_WEIGHT : iconFontWeight.get();
    }

    /// Applies icon size tokens to layout metrics.
    private void updateMetrics() {
        double size = getIconSize();
        setMinSize(size, size);
        setPrefSize(size, size);
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
        private static final CssMetaData<M3Icon, String> ICON_FONT_FAMILY =
                new CssMetaData<>(
                        "-m3-icon-font-family",
                        StringConverter.getInstance(),
                        DEFAULT_ICON_FONT_FAMILY
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return !control.iconFontFamilyProperty().isBound();
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<String> getStyleableProperty(M3Icon control) {
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
        private static final CssMetaData<M3Icon, FontWeight> ICON_FONT_WEIGHT =
                new CssMetaData<>(
                        "-m3-icon-font-weight",
                        FontConverter.FontWeightConverter.getInstance(),
                        DEFAULT_ICON_FONT_WEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Icon control) {
                        return !control.iconFontWeightProperty().isBound();
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<FontWeight> getStyleableProperty(M3Icon control) {
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
