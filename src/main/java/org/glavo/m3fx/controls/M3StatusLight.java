// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3StatusLightSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// Presents a colored status indicator followed by a descriptive text label.
///
/// `M3StatusLight` is a passive M3FX extension inspired by Adobe Spectrum status lights. Material Design 3 does
/// not define a corresponding component. The [variant][#variantProperty()] communicates common semantic states,
/// while [indicatorColor][#indicatorColorProperty()] permits an application-defined category color. Color is
/// always accompanied by [text][#textProperty()] and must not be used as the sole status description.
///
/// The control is not focus traversable and provides no action behavior. Empty text is permitted for JavaFX
/// property and FXML compatibility, but applications should supply a clear label before presenting the control.
/// Four [size roles][M3StatusLightSize] scale the indicator, spacing, and label typography together.
///
/// See [Spectrum Web Components status lights](https://opensource.adobe.com/spectrum-web-components/components/status-light/).
@NotNullByDefault
public final class M3StatusLight extends Control {
    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-status-light";

    /// The fallback indicator paint before CSS resolves a semantic variant.
    private static final Paint DEFAULT_INDICATOR_COLOR = Color.GRAY;

    /// Creates an empty neutral status light with the medium size role.
    public M3StatusLight() {
        this("");
    }

    /// Creates a neutral status light with the specified text and medium size role.
    ///
    /// @param text the descriptive status text
    /// @throws NullPointerException if `text` is `null`
    public M3StatusLight(String text) {
        initialize();
        setText(text);
    }

    /// Creates a status light with the specified text and semantic variant.
    ///
    /// @param text the descriptive status text
    /// @param variant the semantic status variant
    /// @throws NullPointerException if `text` or `variant` is `null`
    public M3StatusLight(String text, M3StatusLightVariant variant) {
        initialize();
        setText(text);
        setVariant(variant);
    }

    /// The descriptive status text.
    ///
    /// The default value is the empty string. [#setText(String)] rejects `null`; a `null` supplied directly to the
    /// property or by a binding is exposed and rendered as an empty string.
    ///
    /// @defaultValue `""`
    private final StringProperty text = new SimpleStringProperty(this, "text", "") {
        /// Updates rendering and accessibility after the text changes.
        @Override
        protected void invalidated() {
            updateAccessibleText();
            requestLayout();
        }
    };

    /// Returns the descriptive status text.
    ///
    /// @return the status text, or the empty string while the property contains `null`
    public String getText() {
        return Objects.requireNonNullElse(text.get(), "");
    }

    /// Sets the descriptive status text.
    ///
    /// @param text the status text
    /// @throws NullPointerException if `text` is `null`
    public void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the property containing the descriptive status text.
    ///
    /// Applications should keep this property non-empty so the indicator color is never the only status signal.
    ///
    /// @return the status-text property
    public StringProperty textProperty() {
        return text;
    }

    /// The semantic meaning conveyed by the indicator.
    ///
    /// A direct `null` assignment restores [M3StatusLightVariant#NEUTRAL]. Bound values must be non-null.
    ///
    /// @defaultValue [M3StatusLightVariant#NEUTRAL]
    private final ObjectProperty<M3StatusLightVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3StatusLightVariant.NEUTRAL) {
                /// Restores the default or updates semantic styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3StatusLightVariant.NEUTRAL);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// Returns the semantic status variant.
    ///
    /// @return the non-null semantic variant
    public M3StatusLightVariant getVariant() {
        return variant.get();
    }

    /// Sets the semantic status variant.
    ///
    /// @param variant the semantic variant
    /// @throws NullPointerException if `variant` is `null`
    public void setVariant(M3StatusLightVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the property containing the semantic status variant.
    ///
    /// A direct `null` assignment restores [M3StatusLightVariant#NEUTRAL]. A unidirectional binding must supply
    /// non-null values.
    ///
    /// @return the semantic-variant property
    public ObjectProperty<M3StatusLightVariant> variantProperty() {
        return variant;
    }

    /// The nominal size role applied to the indicator and label.
    ///
    /// A direct `null` assignment restores [M3StatusLightSize#MEDIUM]. Bound values must be non-null.
    ///
    /// @defaultValue [M3StatusLightSize#MEDIUM]
    private final ObjectProperty<M3StatusLightSize> size =
            new SimpleObjectProperty<>(this, "size", M3StatusLightSize.MEDIUM) {
                /// Restores the default or updates size styling after assignment.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3StatusLightSize.MEDIUM);
                        return;
                    }
                    updateSizeStyle();
                    requestLayout();
                }
            };

    /// Returns the nominal status-light size.
    ///
    /// @return the non-null size role
    public M3StatusLightSize getSize() {
        return size.get();
    }

    /// Sets the nominal status-light size.
    ///
    /// @param size the size role
    /// @throws NullPointerException if `size` is `null`
    public void setSize(M3StatusLightSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the property containing the nominal status-light size.
    ///
    /// A direct `null` assignment restores [M3StatusLightSize#MEDIUM]. A unidirectional binding must supply
    /// non-null values.
    ///
    /// @return the status-light-size property
    public ObjectProperty<M3StatusLightSize> sizeProperty() {
        return size;
    }

    /// The paint used by the circular indicator.
    ///
    /// CSS exposes this property as `-m3-indicator-color`. Before CSS is applied, the effective value is gray.
    /// Semantic variant styles normally supply the value; an explicit application assignment overrides them.
    private @Nullable StyleableObjectProperty<@Nullable Paint> indicatorColor;

    /// Returns the effective indicator paint.
    ///
    /// @return the non-null indicator paint
    public Paint getIndicatorColor() {
        return indicatorColor == null
                ? DEFAULT_INDICATOR_COLOR
                : Objects.requireNonNullElse(indicatorColor.get(), DEFAULT_INDICATOR_COLOR);
    }

    /// Sets an explicit indicator paint.
    ///
    /// This is intended for non-semantic categories. The text must still identify the category independently of
    /// color.
    ///
    /// @param indicatorColor the non-null indicator paint
    /// @throws NullPointerException if `indicatorColor` is `null`
    public void setIndicatorColor(Paint indicatorColor) {
        indicatorColorProperty().set(Objects.requireNonNull(indicatorColor, "indicatorColor"));
    }

    /// Returns the styleable property containing the indicator paint.
    ///
    /// If a binding supplies `null`, rendering falls back to gray until it supplies a non-null value. CSS cannot
    /// set this property while it is bound or after application code assigns it explicitly.
    ///
    /// @return the indicator-color property
    public StyleableObjectProperty<@Nullable Paint> indicatorColorProperty() {
        if (indicatorColor == null) {
            indicatorColor = M3Css.styleableObjectProperty(
                    DEFAULT_INDICATOR_COLOR,
                    this,
                    "indicatorColor",
                    StyleableProperties.INDICATOR_COLOR,
                    this::requestLayout
            );
        }
        return indicatorColor;
    }

    /// Creates the default retained status-light skin.
    ///
    /// @return the default status-light skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3StatusLightSkin(this);
    }

    /// Returns the user-agent stylesheet for status lights.
    ///
    /// @return the status-light stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("status-light.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list
    public static @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata supported by this control.
    ///
    /// @return the immutable CSS metadata list
    @Override
    public @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Initializes style, accessibility, and semantic state.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
        setFocusTraversable(false);
        updateVariantStyle();
        updateSizeStyle();
        updateAccessibleText();
    }

    /// Applies the style class for the current semantic variant.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3StatusLightVariant.NEUTRAL.styleClass(),
                M3StatusLightVariant.POSITIVE.styleClass(),
                M3StatusLightVariant.NEGATIVE.styleClass(),
                M3StatusLightVariant.NOTICE.styleClass(),
                M3StatusLightVariant.INFO.styleClass()
        );
    }

    /// Applies the style class for the current size role.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().styleClass(),
                M3StatusLightSize.SMALL.styleClass(),
                M3StatusLightSize.MEDIUM.styleClass(),
                M3StatusLightSize.LARGE.styleClass(),
                M3StatusLightSize.EXTRA_LARGE.styleClass()
        );
    }

    /// Updates the text exposed to assistive technologies.
    private void updateAccessibleText() {
        setAccessibleText(getText());
    }

    /// CSS metadata for status-light component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the indicator paint.
        private static final CssMetaData<M3StatusLight, @Nullable Paint> INDICATOR_COLOR =
                new CssMetaData<>(
                        "-m3-indicator-color",
                        PaintConverter.getInstance(),
                        DEFAULT_INDICATOR_COLOR
                ) {
                    /// Returns whether CSS may assign the indicator paint.
                    @Override
                    public boolean isSettable(M3StatusLight control) {
                        return M3Css.isSettable(control.indicatorColorProperty());
                    }

                    /// Returns the styleable indicator-paint property.
                    @Override
                    public StyleableProperty<@Nullable Paint> getStyleableProperty(M3StatusLight control) {
                        return control.indicatorColorProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(INDICATOR_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents instantiation.
        private StyleableProperties() {
        }
    }
}
