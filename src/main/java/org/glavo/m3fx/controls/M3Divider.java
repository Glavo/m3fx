// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3DividerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 divider.
///
/// A divider is noninteractive and not focus traversable. Horizontal dividers use logical start and end insets that
/// follow node orientation; vertical dividers use start at the top and end at the bottom. Thickness and insets are
/// styleable logical-pixel values, and CSS cannot replace a bound styleable property.
///
/// See [Material Design dividers](https://m3.material.io/components/divider/overview).
@NotNullByDefault
public final class M3Divider extends Control {
    /// The base style class for M3FX dividers.
    public static final String STYLE_CLASS = "m3-divider";

    /// The default divider thickness.
    private static final double DEFAULT_THICKNESS = 1.0;

    /// The default leading inset.
    private static final double DEFAULT_INSET_START = 0.0;

    /// The default trailing inset.
    private static final double DEFAULT_INSET_END = 0.0;

    /// The divider orientation.
    ///
    /// A direct `null` assignment restores [Orientation#HORIZONTAL].
    ///
    /// @defaultValue [Orientation#HORIZONTAL]
    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL) {
        /// Restores the default orientation when a null value is assigned.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(Orientation.HORIZONTAL);
            }
        }
    };

    /// The visible divider thickness in logical pixels.
    ///
    /// @defaultValue `1.0`
    private @Nullable StyleableDoubleProperty thickness;

    /// The logical leading inset in logical pixels.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty insetStart;

    /// The logical trailing inset in logical pixels.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty insetEnd;

    /// Creates a horizontal divider.
    public M3Divider() {
        this(Orientation.HORIZONTAL);
    }

    /// Creates a divider with the requested orientation.
    ///
    /// @param orientation the divider orientation
    public M3Divider(Orientation orientation) {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.NODE);
        setFocusTraversable(false);
        setOrientation(orientation);
    }

    /// Returns the divider orientation.
    ///
    /// @return the divider orientation
    public final Orientation getOrientation() {
        return orientation.get();
    }

    /// Sets the divider orientation.
    ///
    /// @param orientation the divider orientation
    /// @throws NullPointerException if `orientation` is `null`
    public final void setOrientation(Orientation orientation) {
        this.orientation.set(Objects.requireNonNull(orientation, "orientation"));
    }

    public final ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    /// Returns the divider thickness in logical pixels.
    ///
    /// @return the divider thickness token
    public final double getThickness() {
        return thickness == null ? DEFAULT_THICKNESS : thickness.get();
    }

    /// Sets the divider thickness in logical pixels.
    ///
    /// @param thickness the divider thickness token
    /// @throws IllegalArgumentException if `thickness` is negative or not finite
    public final void setThickness(double thickness) {
        thicknessProperty().set(M3Css.nonNegative(thickness, "thickness"));
    }

    public final StyleableDoubleProperty thicknessProperty() {
        if (thickness == null) {
            thickness = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_THICKNESS,
                    this,
                    "thickness",
                    StyleableProperties.THICKNESS,
                    this::requestLayout
            );
        }
        return thickness;
    }

    /// Returns the logical leading inset in logical pixels.
    ///
    /// @return the leading inset token
    public final double getInsetStart() {
        return insetStart == null ? DEFAULT_INSET_START : insetStart.get();
    }

    /// Sets the logical leading inset in logical pixels.
    ///
    /// @param insetStart the leading inset token
    /// @throws IllegalArgumentException if `insetStart` is negative or not finite
    public final void setInsetStart(double insetStart) {
        insetStartProperty().set(M3Css.nonNegative(insetStart, "insetStart"));
    }

    public final StyleableDoubleProperty insetStartProperty() {
        if (insetStart == null) {
            insetStart = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INSET_START,
                    this,
                    "insetStart",
                    StyleableProperties.INSET_START,
                    this::requestLayout
            );
        }
        return insetStart;
    }

    /// Returns the logical trailing inset in logical pixels.
    ///
    /// @return the trailing inset token
    public final double getInsetEnd() {
        return insetEnd == null ? DEFAULT_INSET_END : insetEnd.get();
    }

    /// Sets the logical trailing inset in logical pixels.
    ///
    /// @param insetEnd the trailing inset token
    /// @throws IllegalArgumentException if `insetEnd` is negative or not finite
    public final void setInsetEnd(double insetEnd) {
        insetEndProperty().set(M3Css.nonNegative(insetEnd, "insetEnd"));
    }

    public final StyleableDoubleProperty insetEndProperty() {
        if (insetEnd == null) {
            insetEnd = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INSET_END,
                    this,
                    "insetEnd",
                    StyleableProperties.INSET_END,
                    this::requestLayout
            );
        }
        return insetEnd;
    }

    /// Creates the default divider skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DividerSkin(this);
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

    /// Returns the user-agent stylesheet for M3FX dividers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("divider.css");
    }

    /// CSS metadata for M3FX divider component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the thickness token.
        private static final CssMetaData<M3Divider, Number> THICKNESS =
                new CssMetaData<>("-m3-thickness", SizeConverter.getInstance(), DEFAULT_THICKNESS) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Divider control) {
                        return M3Css.isSettable(control.thicknessProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Divider control) {
                        return control.thicknessProperty();
                    }
                };

        /// CSS metadata for the leading inset token.
        private static final CssMetaData<M3Divider, Number> INSET_START =
                new CssMetaData<>("-m3-inset-start", SizeConverter.getInstance(), DEFAULT_INSET_START) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Divider control) {
                        return M3Css.isSettable(control.insetStartProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Divider control) {
                        return control.insetStartProperty();
                    }
                };

        /// CSS metadata for the trailing inset token.
        private static final CssMetaData<M3Divider, Number> INSET_END =
                new CssMetaData<>("-m3-inset-end", SizeConverter.getInstance(), DEFAULT_INSET_END) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Divider control) {
                        return M3Css.isSettable(control.insetEndProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Divider control) {
                        return control.insetEndProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(THICKNESS);
            styleables.add(INSET_START);
            styleables.add(INSET_END);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
