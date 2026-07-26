// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 menu item.
///
/// `M3MenuItem` is the concrete action row used inside [M3Menu]. Calling [#fire()] on an enabled item delivers its
/// action event. A containing menu may additionally change [#selectedProperty()] according to its selection mode.
/// Leading and trailing nodes use the slot and ownership contract defined by [M3ListItemBase].
///
/// A menu item is focus traversable as part of menu keyboard navigation. It is not an [M3ListItem] and cannot be
/// selected through list-only containers.
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public sealed class M3MenuItem extends M3ListItemBase permits M3SubMenuItem {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-menu-item";

    /// The fallback radius used by inner corners at a grouped-menu boundary.
    private static final double DEFAULT_INNER_CORNER_SHAPE = 4.0;

    /// Creates an empty menu item.
    public M3MenuItem() {
        this("");
    }

    /// Creates a menu item with text.
    ///
    /// @param text the item text
    /// @throws NullPointerException if `text` is `null`
    public M3MenuItem(String text) {
        super(text);
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.MENU_ITEM);
    }

    /// Creates a menu item with text and leading content.
    ///
    /// @param text    the item text
    /// @param leading the leading slot node, or `null` for no leading content
    /// @throws NullPointerException if `text` is `null`
    public M3MenuItem(String text, @Nullable Node leading) {
        this(text);
        setLeading(leading);
    }

    /// Creates a menu item with text, leading content, and trailing content.
    ///
    /// @param text     the item text
    /// @param leading  the leading slot node, or `null` for no leading content
    /// @param trailing the trailing slot node, or `null` for no trailing content
    /// @throws NullPointerException if `text` is `null`
    public M3MenuItem(String text, @Nullable Node leading, @Nullable Node trailing) {
        this(text, leading);
        setTrailing(trailing);
    }

    /// The corner radius used at the inner edge of the first or last item in a contiguous menu group.
    ///
    /// Values must be finite and non-negative. The value is ignored for middle items and states that use one
    /// uniform container shape.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty innerCornerShapeValue;

    /// Returns the radius used by the inner corners of a first or last item in a visual menu group.
    ///
    /// The value is ignored for middle items and for states whose container uses one uniform shape, such as a
    /// selected item or an active submenu owner. A theme normally supplies this value from the Material menu-item
    /// inner-corner token.
    ///
    /// @return the non-negative inner-corner radius
    public final double getInnerCornerShape() {
        return innerCornerShapeValue == null ? DEFAULT_INNER_CORNER_SHAPE : innerCornerShapeValue.get();
    }

    /// Sets the radius used by the inner corners of a first or last item in a visual menu group.
    ///
    /// @param innerCornerShape the non-negative inner-corner radius
    /// @throws IllegalArgumentException if `innerCornerShape` is negative or not finite
    public final void setInnerCornerShape(double innerCornerShape) {
        innerCornerShapeProperty().set(M3Css.nonNegative(innerCornerShape, "innerCornerShape"));
    }

    /// Returns the styleable inner-corner radius property.
    ///
    /// @return the styleable inner-corner radius property
    public final StyleableDoubleProperty innerCornerShapeProperty() {
        if (innerCornerShapeValue == null) {
            innerCornerShapeValue = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INNER_CORNER_SHAPE,
                    this,
                    "innerCornerShape",
                    StyleableProperties.INNER_CORNER_SHAPE,
                    this::requestLayout
            );
        }
        return innerCornerShapeValue;
    }

    /// Returns the CSS metadata supported by menu items.
    ///
    /// @return the menu-item CSS metadata
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata supported by this menu item.
    ///
    /// @return the menu-item CSS metadata
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the menu-item stylesheet, which extends the base list-item styles with menu states.
    ///
    /// @return the bundled menu-item stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("menu-item.css");
    }

    /// CSS metadata for menu-item component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// Metadata for the grouped-menu inner-corner radius.
        private static final CssMetaData<M3MenuItem, Number> INNER_CORNER_SHAPE = new CssMetaData<>(
                "-m3-menu-inner-corner-shape",
                SizeConverter.getInstance(),
                DEFAULT_INNER_CORNER_SHAPE
        ) {
            /// Returns whether CSS may assign the property.
            @Override
            public boolean isSettable(M3MenuItem control) {
                return M3Css.isSettable(control.innerCornerShapeProperty());
            }

            /// Returns the styleable property represented by this metadata.
            @Override
            public StyleableProperty<Number> getStyleableProperty(M3MenuItem control) {
                return control.innerCornerShapeProperty();
            }
        };

        /// The immutable CSS metadata list for menu items.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(M3ListItemBase.getClassCssMetaData());
            styleables.add(INNER_CORNER_SHAPE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents instantiation.
        private StyleableProperties() {
        }
    }

}
