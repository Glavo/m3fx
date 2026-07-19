// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.skins.M3DisclosureIconSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A compact Material Design disclosure indicator for expandable rows, destinations, and split-button menus.
///
/// Directional indicators point toward nested content before expanding downward. Vertical indicators point down
/// before expanding upward, matching the split-button menu icon behavior.
///
/// See [Material Design navigation drawer](https://m3.material.io/components/navigation-drawer/overview) and
/// [Material Design split buttons](https://m3.material.io/components/split-button/overview).
@NotNullByDefault
public final class M3DisclosureIcon extends Control {
    /// The base style class for M3FX disclosure icons.
    public static final String STYLE_CLASS = "m3-disclosure-icon";

    /// The expanded pseudo-class used by disclosure icons.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    /// The default disclosure icon viewport size.
    private static final double DEFAULT_ICON_SIZE = 24.0;

    /// Creates a collapsed disclosure icon.
    public M3DisclosureIcon() {
        this(false);
    }

    /// Creates a disclosure icon with the supplied expanded state.
    ///
    /// @param expanded whether the disclosure target starts expanded
    public M3DisclosureIcon(boolean expanded) {
        getStyleClass().add(STYLE_CLASS);
        setFocusTraversable(false);
        setExpanded(expanded);
    }

    /// Whether the disclosure target is expanded.
    ///
    /// The default value is `false`. Changing the property updates the `:expanded` pseudo-class.
    ///
    /// @defaultValue `false`
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded") {
        /// Updates expanded pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, get());
        }
    };

    /// Returns whether the disclosure target is expanded.
    ///
    /// @return `true` when the disclosure target is expanded
    public final boolean isExpanded() {
        return expanded.get();
    }

    /// Sets whether the disclosure target is expanded.
    ///
    /// @param expanded whether the disclosure target is expanded
    public final void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    /// Returns the property that indicates whether the disclosure target is expanded.
    ///
    /// @return the expanded property
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    /// Whether the icon uses down/up expand-collapse directions instead of logical horizontal disclosure.
    ///
    /// The default value is `false`.
    ///
    /// @defaultValue `false`
    private final BooleanProperty vertical = new SimpleBooleanProperty(this, "vertical");

    /// Returns whether this icon points down when collapsed and up when expanded.
    ///
    /// @return `true` for vertical expand-collapse direction
    public final boolean isVertical() {
        return vertical.get();
    }

    /// Sets whether this icon uses vertical expand-collapse direction.
    ///
    /// @param vertical whether the collapsed and expanded directions are down and up
    public final void setVertical(boolean vertical) {
        this.vertical.set(vertical);
    }

    /// Returns the property that selects vertical expand-collapse directions.
    ///
    /// @return the vertical-direction property
    public final BooleanProperty verticalProperty() {
        return vertical;
    }

    /// The disclosure icon viewport size, in logical pixels.
    ///
    /// The default value is `24.0`. Values must be finite and non-negative. This property is exposed to CSS as
    /// `-m3-disclosure-icon-size`.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty iconSize;

    /// Returns the disclosure icon viewport size.
    ///
    /// @return the icon viewport size in logical pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the disclosure icon viewport size.
    ///
    /// @param iconSize the icon viewport size in logical pixels
    /// @throws IllegalArgumentException if `iconSize` is negative or not finite
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the styleable property that stores the disclosure icon viewport size.
    ///
    /// @return the icon size property, in logical pixels
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::requestLayout
            );
        }
        return iconSize;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the disclosure icon CSS metadata
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the disclosure icon CSS metadata
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX disclosure icons.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("disclosure-icon.css");
    }

    /// Creates the default Material Design 3 disclosure icon skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DisclosureIconSkin(this);
    }

    /// CSS metadata for disclosure icon metrics.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the icon viewport size.
        private static final CssMetaData<M3DisclosureIcon, Number> ICON_SIZE =
                new CssMetaData<>(
                        "-m3-disclosure-icon-size",
                        SizeConverter.getInstance(),
                        DEFAULT_ICON_SIZE
                ) {
                    /// Returns whether CSS can set the icon viewport size.
                    @Override
                    public boolean isSettable(M3DisclosureIcon control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable icon viewport size property.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DisclosureIcon control) {
                        return control.iconSizeProperty();
                    }
                };

        /// The immutable disclosure icon CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ICON_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
