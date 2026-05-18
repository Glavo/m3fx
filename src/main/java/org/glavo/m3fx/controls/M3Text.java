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
import javafx.scene.AccessibleRole;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TextSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 text label driven by typography tokens.
@NotNullByDefault
public class M3Text extends Labeled {
    /// The base style class for M3FX text labels.
    public static final String STYLE_CLASS = "m3-text";

    /// The default typography font family.
    private static final String DEFAULT_TYPOGRAPHY_FONT_FAMILY = "System";

    /// The default typography font size.
    private static final double DEFAULT_TYPOGRAPHY_FONT_SIZE = 16.0;

    /// The default typography line height.
    private static final double DEFAULT_TYPOGRAPHY_LINE_HEIGHT = 24.0;

    /// The default typography font weight.
    private static final FontWeight DEFAULT_TYPOGRAPHY_FONT_WEIGHT = FontWeight.NORMAL;

    /// The typography role property.
    private final ObjectProperty<M3TextRole> role =
            new SimpleObjectProperty<>(this, "role", M3TextRole.BODY_LARGE) {
                /// Updates typography role style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TextRole.BODY_LARGE);
                        return;
                    }
                    updateRoleStyle();
                }
            };

    /// The styleable typography font family token.
    private StyleableObjectProperty<String> typographyFontFamily;

    /// The styleable typography font size token.
    private StyleableDoubleProperty typographyFontSize;

    /// The styleable typography line height token.
    private StyleableDoubleProperty typographyLineHeight;

    /// The styleable typography font weight token.
    private StyleableObjectProperty<FontWeight> typographyFontWeight;

    /// Creates an empty body-large text label.
    public M3Text() {
        this("");
    }

    /// Creates a body-large text label.
    public M3Text(String text) {
        initialize();
        setText(text);
    }

    /// Creates a text label with a typography role.
    public M3Text(String text, M3TextRole role) {
        initialize();
        setText(text);
        setRole(role);
    }

    /// Returns the typography role.
    public final M3TextRole getRole() {
        return role.get();
    }

    /// Sets the typography role.
    public final void setRole(M3TextRole role) {
        this.role.set(Objects.requireNonNull(role, "role"));
    }

    /// Returns the typography role property.
    public final ObjectProperty<M3TextRole> roleProperty() {
        return role;
    }

    /// Returns the typography font family token.
    public final String getTypographyFontFamily() {
        return typographyFontFamily == null ? DEFAULT_TYPOGRAPHY_FONT_FAMILY : typographyFontFamily.get();
    }

    /// Sets the typography font family token.
    public final void setTypographyFontFamily(String typographyFontFamily) {
        typographyFontFamilyProperty().set(Objects.requireNonNull(typographyFontFamily, "typographyFontFamily"));
    }

    /// Returns the typography font family token property.
    public final StyleableObjectProperty<String> typographyFontFamilyProperty() {
        if (typographyFontFamily == null) {
            typographyFontFamily = new StyleableObjectProperty<>(DEFAULT_TYPOGRAPHY_FONT_FAMILY) {
                /// Applies updated font family tokens.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_TYPOGRAPHY_FONT_FAMILY);
                        return;
                    }
                    updateFont();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Text.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "typographyFontFamily";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Text, String> getCssMetaData() {
                    return StyleableProperties.TYPOGRAPHY_FONT_FAMILY;
                }
            };
        }
        return typographyFontFamily;
    }

    /// Returns the typography font size token.
    public final double getTypographyFontSize() {
        return typographyFontSize == null ? DEFAULT_TYPOGRAPHY_FONT_SIZE : typographyFontSize.get();
    }

    /// Sets the typography font size token.
    public final void setTypographyFontSize(double typographyFontSize) {
        typographyFontSizeProperty().set(M3Css.nonNegative(typographyFontSize, "typographyFontSize"));
    }

    /// Returns the typography font size token property.
    public final StyleableDoubleProperty typographyFontSizeProperty() {
        if (typographyFontSize == null) {
            typographyFontSize = new StyleableDoubleProperty(DEFAULT_TYPOGRAPHY_FONT_SIZE) {
                /// Applies updated font size tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "typographyFontSize");
                    updateFont();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Text.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "typographyFontSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Text, Number> getCssMetaData() {
                    return StyleableProperties.TYPOGRAPHY_FONT_SIZE;
                }
            };
        }
        return typographyFontSize;
    }

    /// Returns the typography line height token.
    public final double getTypographyLineHeight() {
        return typographyLineHeight == null ? DEFAULT_TYPOGRAPHY_LINE_HEIGHT : typographyLineHeight.get();
    }

    /// Sets the typography line height token.
    public final void setTypographyLineHeight(double typographyLineHeight) {
        typographyLineHeightProperty().set(M3Css.nonNegative(typographyLineHeight, "typographyLineHeight"));
    }

    /// Returns the typography line height token property.
    public final StyleableDoubleProperty typographyLineHeightProperty() {
        if (typographyLineHeight == null) {
            typographyLineHeight = new StyleableDoubleProperty(DEFAULT_TYPOGRAPHY_LINE_HEIGHT) {
                /// Applies updated line height tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "typographyLineHeight");
                    updateFont();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Text.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "typographyLineHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Text, Number> getCssMetaData() {
                    return StyleableProperties.TYPOGRAPHY_LINE_HEIGHT;
                }
            };
        }
        return typographyLineHeight;
    }

    /// Returns the typography font weight token.
    public final double getTypographyFontWeight() {
        return getTypographyFontWeightValue().getWeight();
    }

    /// Sets the typography font weight token.
    public final void setTypographyFontWeight(double typographyFontWeight) {
        typographyFontWeightProperty().set(validateFontWeight(typographyFontWeight));
    }

    /// Returns the typography font weight token property.
    public final StyleableObjectProperty<FontWeight> typographyFontWeightProperty() {
        if (typographyFontWeight == null) {
            typographyFontWeight = new StyleableObjectProperty<>(DEFAULT_TYPOGRAPHY_FONT_WEIGHT) {
                /// Applies updated font weight tokens.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_TYPOGRAPHY_FONT_WEIGHT);
                        return;
                    }
                    updateFont();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Text.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "typographyFontWeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Text, FontWeight> getCssMetaData() {
                    return StyleableProperties.TYPOGRAPHY_FONT_WEIGHT;
                }
            };
        }
        return typographyFontWeight;
    }

    /// Returns the user-agent stylesheet for M3FX text labels.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("text.css");
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

    /// Creates the default Material Design 3 text skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TextSkin(this);
    }

    /// Initializes style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
        updateRoleStyle();
        updateFont();
    }

    /// Applies the style class for the selected typography role.
    private void updateRoleStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getRole().getStyleClass(),
                M3TextRole.DISPLAY_LARGE.getStyleClass(),
                M3TextRole.DISPLAY_MEDIUM.getStyleClass(),
                M3TextRole.DISPLAY_SMALL.getStyleClass(),
                M3TextRole.HEADLINE_LARGE.getStyleClass(),
                M3TextRole.HEADLINE_MEDIUM.getStyleClass(),
                M3TextRole.HEADLINE_SMALL.getStyleClass(),
                M3TextRole.TITLE_LARGE.getStyleClass(),
                M3TextRole.TITLE_MEDIUM.getStyleClass(),
                M3TextRole.TITLE_SMALL.getStyleClass(),
                M3TextRole.LABEL_LARGE.getStyleClass(),
                M3TextRole.LABEL_MEDIUM.getStyleClass(),
                M3TextRole.LABEL_SMALL.getStyleClass(),
                M3TextRole.BODY_LARGE.getStyleClass(),
                M3TextRole.BODY_MEDIUM.getStyleClass(),
                M3TextRole.BODY_SMALL.getStyleClass()
        );
    }

    /// Applies resolved typography font tokens to the inherited font property.
    private void updateFont() {
        double fontSize = getTypographyFontSize();
        setFont(Font.font(
                getTypographyFontFamily(),
                getTypographyFontWeightValue(),
                fontSize
        ));
        setLineSpacing(Math.max(0.0, getTypographyLineHeight() - fontSize));
    }

    /// Returns the resolved typography font weight token.
    private FontWeight getTypographyFontWeightValue() {
        return typographyFontWeight == null ? DEFAULT_TYPOGRAPHY_FONT_WEIGHT : typographyFontWeight.get();
    }

    /// Validates a font weight token.
    private static FontWeight validateFontWeight(double value) {
        if (value < 1.0 || value > 1000.0) {
            throw new IllegalArgumentException("typographyFontWeight must be between 1 and 1000");
        }
        return FontWeight.findByWeight((int) Math.round(value));
    }

    /// CSS metadata for M3FX text typography tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the typography font family token.
        private static final CssMetaData<M3Text, String> TYPOGRAPHY_FONT_FAMILY =
                new CssMetaData<>(
                        "-m3-typography-font-family",
                        StringConverter.getInstance(),
                        DEFAULT_TYPOGRAPHY_FONT_FAMILY
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Text control) {
                        return !control.typographyFontFamilyProperty().isBound();
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<String> getStyleableProperty(M3Text control) {
                        return control.typographyFontFamilyProperty();
                    }
                };

        /// CSS metadata for the typography font size token.
        private static final CssMetaData<M3Text, Number> TYPOGRAPHY_FONT_SIZE =
                new CssMetaData<>(
                        "-m3-typography-font-size",
                        SizeConverter.getInstance(),
                        DEFAULT_TYPOGRAPHY_FONT_SIZE
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Text control) {
                        return M3Css.isSettable(control.typographyFontSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Text control) {
                        return control.typographyFontSizeProperty();
                    }
                };

        /// CSS metadata for the typography font weight token.
        private static final CssMetaData<M3Text, FontWeight> TYPOGRAPHY_FONT_WEIGHT =
                new CssMetaData<>(
                        "-m3-typography-font-weight",
                        FontConverter.FontWeightConverter.getInstance(),
                        DEFAULT_TYPOGRAPHY_FONT_WEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Text control) {
                        return !control.typographyFontWeightProperty().isBound();
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<FontWeight> getStyleableProperty(M3Text control) {
                        return control.typographyFontWeightProperty();
                    }
                };

        /// CSS metadata for the typography line height token.
        private static final CssMetaData<M3Text, Number> TYPOGRAPHY_LINE_HEIGHT =
                new CssMetaData<>(
                        "-m3-typography-line-height",
                        SizeConverter.getInstance(),
                        DEFAULT_TYPOGRAPHY_LINE_HEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Text control) {
                        return M3Css.isSettable(control.typographyLineHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Text control) {
                        return control.typographyLineHeightProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Labeled.getClassCssMetaData());
            styleables.add(TYPOGRAPHY_FONT_FAMILY);
            styleables.add(TYPOGRAPHY_FONT_SIZE);
            styleables.add(TYPOGRAPHY_LINE_HEIGHT);
            styleables.add(TYPOGRAPHY_FONT_WEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
