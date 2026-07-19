// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.binding.Bindings;
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
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TextSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A token-aware text label driven by the Material Design 3 type scale.
///
/// `M3Text` is an M3FX utility control rather than a standalone Material component. It renders text with an
/// [M3TextRole], token-backed font family, size, line height, and weight so custom layouts can use the same
/// typography model as built-in controls. The inherited `font` and `lineSpacing` properties are bound to the
/// typography token properties; applications customize typography through the token properties rather than the
/// inherited setters.
///
/// See [Material Design typography](https://m3.material.io/styles/typography/overview) and
/// [Material Design](https://m3.material.io/).
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

    /// Creates an empty body-large text label.
    public M3Text() {
        this("");
    }

    /// Creates a body-large text label.
    ///
    /// @param text the initial text content
    public M3Text(String text) {
        initialize();
        setText(text);
    }

    /// Creates a text label with a typography role.
    ///
    /// @param text the initial text content
    /// @param role the typography role used to resolve type scale tokens
    /// @throws NullPointerException if `role` is `null`
    public M3Text(String text, M3TextRole role) {
        initialize();
        setText(text);
        setRole(role);
    }

    /// The semantic Material typography role.
    ///
    /// Assigning `null` through the property restores [M3TextRole#BODY_LARGE].
    ///
    /// @defaultValue `BODY_LARGE`
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

    /// Returns the typography role.
    ///
    /// @return the typography role used by this text label
    public final M3TextRole getRole() {
        return role.get();
    }

    /// Sets the typography role.
    ///
    /// @param role the typography role used to resolve type scale tokens
    /// @throws NullPointerException if `role` is `null`
    public final void setRole(M3TextRole role) {
        this.role.set(Objects.requireNonNull(role, "role"));
    }

    /// Returns the `role` property.
    ///
    /// The returned property is observable and bindable. Its default value is `BODY_LARGE`.
    ///
    /// @return the `role` property
    public final ObjectProperty<M3TextRole> roleProperty() {
        return role;
    }

    /// The styleable typography font-family token.
    ///
    /// Assigning `null` through the property restores `System`.
    ///
    /// @defaultValue `"System"`
    private @Nullable StyleableObjectProperty<@Nullable String> typographyFontFamily;

    /// Returns the typography font family token.
    ///
    /// @return the resolved typography font family
    public final String getTypographyFontFamily() {
        return typographyFontFamily == null
                ? DEFAULT_TYPOGRAPHY_FONT_FAMILY
                : Objects.requireNonNullElse(typographyFontFamily.get(), DEFAULT_TYPOGRAPHY_FONT_FAMILY);
    }

    /// Sets the typography font family token.
    ///
    /// @param typographyFontFamily the font family used by this text label
    /// @throws NullPointerException if `typographyFontFamily` is `null`
    public final void setTypographyFontFamily(String typographyFontFamily) {
        typographyFontFamilyProperty().set(Objects.requireNonNull(typographyFontFamily, "typographyFontFamily"));
    }

    /// Returns the `typographyFontFamily` property.
    ///
    /// The returned property is observable, bindable, and styleable. Its default value is `"System"`. A directly
    /// assigned `null` is replaced with that default. If a binding source provides `null`, the property remains
    /// `null` while [#getTypographyFontFamily()] reports the default family.
    ///
    /// @return the `typographyFontFamily` property
    public final StyleableObjectProperty<@Nullable String> typographyFontFamilyProperty() {
        if (typographyFontFamily == null) {
            typographyFontFamily = M3Css.styleableObjectProperty(
                    DEFAULT_TYPOGRAPHY_FONT_FAMILY,
                    this,
                    "typographyFontFamily",
                    StyleableProperties.TYPOGRAPHY_FONT_FAMILY,
                    () -> {
                        if (typographyFontFamilyProperty().get() == null
                                && !typographyFontFamilyProperty().isBound()) {
                            typographyFontFamilyProperty().set(DEFAULT_TYPOGRAPHY_FONT_FAMILY);
                        }
                    }
            );
        }
        return typographyFontFamily;
    }

    /// The styleable typography font size in logical pixels.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty typographyFontSize;

    /// Returns the typography font size token.
    ///
    /// @return the resolved typography font size in pixels
    public final double getTypographyFontSize() {
        return typographyFontSize == null ? DEFAULT_TYPOGRAPHY_FONT_SIZE : typographyFontSize.get();
    }

    /// Sets the typography font size token.
    ///
    /// @param typographyFontSize the font size in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setTypographyFontSize(double typographyFontSize) {
        typographyFontSizeProperty().set(M3Css.nonNegative(typographyFontSize, "typographyFontSize"));
    }

    /// Returns the `typographyFontSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `16.0` logical pixels.
    ///
    /// @return the `typographyFontSize` property
    public final StyleableDoubleProperty typographyFontSizeProperty() {
        if (typographyFontSize == null) {
            typographyFontSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TYPOGRAPHY_FONT_SIZE,
                    this,
                    "typographyFontSize",
                    StyleableProperties.TYPOGRAPHY_FONT_SIZE,
                    this::requestLayout
            );
        }
        return typographyFontSize;
    }

    /// The styleable typography line height in logical pixels.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty typographyLineHeight;

    /// Returns the typography line height token.
    ///
    /// @return the resolved typography line height in pixels
    public final double getTypographyLineHeight() {
        return typographyLineHeight == null ? DEFAULT_TYPOGRAPHY_LINE_HEIGHT : typographyLineHeight.get();
    }

    /// Sets the typography line height token.
    ///
    /// @param typographyLineHeight the line height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setTypographyLineHeight(double typographyLineHeight) {
        typographyLineHeightProperty().set(M3Css.nonNegative(typographyLineHeight, "typographyLineHeight"));
    }

    /// Returns the `typographyLineHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `24.0` logical pixels.
    ///
    /// @return the `typographyLineHeight` property
    public final StyleableDoubleProperty typographyLineHeightProperty() {
        if (typographyLineHeight == null) {
            typographyLineHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TYPOGRAPHY_LINE_HEIGHT,
                    this,
                    "typographyLineHeight",
                    StyleableProperties.TYPOGRAPHY_LINE_HEIGHT,
                    this::requestLayout
            );
        }
        return typographyLineHeight;
    }

    /// The styleable typography font weight.
    ///
    /// Assigning `null` through the property restores [FontWeight#NORMAL].
    ///
    /// @defaultValue `NORMAL`
    private @Nullable StyleableObjectProperty<@Nullable FontWeight> typographyFontWeight;

    /// Returns the typography font weight token.
    ///
    /// @return the resolved typography font weight as a numeric CSS weight
    public final double getTypographyFontWeight() {
        return getTypographyFontWeightValue().getWeight();
    }

    /// Sets the typography font weight token.
    ///
    /// @param typographyFontWeight the numeric CSS font weight, from `1` through `1000`
    /// @throws IllegalArgumentException if `typographyFontWeight` is not finite, less than `1`, or greater than
    ///                                  `1000`
    public final void setTypographyFontWeight(double typographyFontWeight) {
        typographyFontWeightProperty().set(validateFontWeight(typographyFontWeight));
    }

    /// Returns the `typographyFontWeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. Its default value is `NORMAL`. A directly
    /// assigned `null` is replaced with that default. If a binding source provides `null`, the property remains
    /// `null` while [#getTypographyFontWeight()] reports the default weight.
    ///
    /// @return the `typographyFontWeight` property
    public final StyleableObjectProperty<@Nullable FontWeight> typographyFontWeightProperty() {
        if (typographyFontWeight == null) {
            typographyFontWeight = M3Css.styleableObjectProperty(
                    DEFAULT_TYPOGRAPHY_FONT_WEIGHT,
                    this,
                    "typographyFontWeight",
                    StyleableProperties.TYPOGRAPHY_FONT_WEIGHT,
                    () -> {
                        if (typographyFontWeightProperty().get() == null
                                && !typographyFontWeightProperty().isBound()) {
                            typographyFontWeightProperty().set(DEFAULT_TYPOGRAPHY_FONT_WEIGHT);
                        }
                    }
            );
        }
        return typographyFontWeight;
    }

    /// Returns the user-agent stylesheet for M3FX text labels.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("text.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3Text`
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
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
        setFocusTraversable(false);
        updateRoleStyle();
        bindTypographyProperties();
    }

    /// Applies the style class for the selected typography role.
    private void updateRoleStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getRole().styleClass(),
                M3TextRole.DISPLAY_LARGE.styleClass(),
                M3TextRole.DISPLAY_MEDIUM.styleClass(),
                M3TextRole.DISPLAY_SMALL.styleClass(),
                M3TextRole.HEADLINE_LARGE.styleClass(),
                M3TextRole.HEADLINE_MEDIUM.styleClass(),
                M3TextRole.HEADLINE_SMALL.styleClass(),
                M3TextRole.TITLE_LARGE.styleClass(),
                M3TextRole.TITLE_MEDIUM.styleClass(),
                M3TextRole.TITLE_SMALL.styleClass(),
                M3TextRole.LABEL_LARGE.styleClass(),
                M3TextRole.LABEL_MEDIUM.styleClass(),
                M3TextRole.LABEL_SMALL.styleClass(),
                M3TextRole.BODY_LARGE.styleClass(),
                M3TextRole.BODY_MEDIUM.styleClass(),
                M3TextRole.BODY_SMALL.styleClass()
        );
    }

    /// Binds inherited text metrics to the styleable Material typography token properties.
    private void bindTypographyProperties() {
        fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(
                        getTypographyFontFamily(),
                        getTypographyFontWeightValue(),
                        getTypographyFontSize()
                ),
                typographyFontFamilyProperty(),
                typographyFontSizeProperty(),
                typographyFontWeightProperty()
        ));
        lineSpacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0.0, getTypographyLineHeight() - getTypographyFontSize()),
                typographyLineHeightProperty(),
                typographyFontSizeProperty()
        ));
    }

    /// Returns the resolved typography font weight token.
    private FontWeight getTypographyFontWeightValue() {
        return typographyFontWeight == null
                ? DEFAULT_TYPOGRAPHY_FONT_WEIGHT
                : Objects.requireNonNullElse(typographyFontWeight.get(), DEFAULT_TYPOGRAPHY_FONT_WEIGHT);
    }

    /// Validates a font weight token.
    private static FontWeight validateFontWeight(double value) {
        if (!Double.isFinite(value) || value < 1.0 || value > 1000.0) {
            throw new IllegalArgumentException("typographyFontWeight must be finite and between 1 and 1000");
        }
        return FontWeight.findByWeight((int) Math.round(value));
    }

    /// CSS metadata for M3FX text typography tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the typography font family token.
        private static final CssMetaData<M3Text, @Nullable String> TYPOGRAPHY_FONT_FAMILY =
                new CssMetaData<>(
                        "-m3-typography-font-family",
                        StringConverter.getInstance(),
                        DEFAULT_TYPOGRAPHY_FONT_FAMILY
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Text control) {
                        return M3Css.isSettable(control.typographyFontFamilyProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<@Nullable String> getStyleableProperty(M3Text control) {
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
        private static final CssMetaData<M3Text, @Nullable FontWeight> TYPOGRAPHY_FONT_WEIGHT =
                new CssMetaData<>(
                        "-m3-typography-font-weight",
                        FontConverter.FontWeightConverter.getInstance(),
                        DEFAULT_TYPOGRAPHY_FONT_WEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Text control) {
                        return M3Css.isSettable(control.typographyFontWeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<@Nullable FontWeight> getStyleableProperty(M3Text control) {
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
