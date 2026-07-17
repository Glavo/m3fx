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
import javafx.scene.Node;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 icon button for compact icon-only actions.
///
/// `M3IconButton` keeps the Material button action behavior while sizing its container around a graphic,
/// usually an [M3Icon]. It uses the standard icon-button treatment by default and participates in the same
/// state-layer, ripple, focus, and accessibility behavior as other buttons.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
@NotNullByDefault
public final class M3IconButton extends M3ButtonBase {
    /// The base style class for M3FX icon buttons.
    public static final String STYLE_CLASS = "m3-icon-button";

    /// The default icon button width role.
    private static final M3IconButtonWidth DEFAULT_WIDTH = M3IconButtonWidth.DEFAULT;

    /// The default icon button container width.
    private static final double DEFAULT_CONTAINER_WIDTH = 40.0;

    /// The icon button width role property.
    private final ObjectProperty<M3IconButtonWidth> widthRole =
            new SimpleObjectProperty<>(this, "widthRole", DEFAULT_WIDTH) {
                /// Updates width style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_WIDTH);
                        return;
                    }
                    updateWidthStyle();
                }
            };

    /// The styleable visual container width token.
    private @Nullable StyleableDoubleProperty containerWidth;

    /// Creates an icon button without a graphic.
    public M3IconButton() {
        this(null);
    }

    /// Creates an icon button with a graphic.
    ///
    /// @param graphic the graphic displayed by the icon button, or `null`
    public M3IconButton(@Nullable Node graphic) {
        super("", graphic);
        M3ControlStyles.add(this, STYLE_CLASS);
        setVariant(M3ButtonVariant.TEXT);
        initializeIconMetrics();
    }

    /// Returns the icon button width role.
    ///
    /// @return the icon button width role
    public final M3IconButtonWidth getWidthRole() {
        return widthRole.get();
    }

    /// Sets the icon button width role.
    ///
    /// @param widthRole the icon button width role
    /// @throws NullPointerException if any required argument is `null`
    public final void setWidthRole(M3IconButtonWidth widthRole) {
        this.widthRole.set(Objects.requireNonNull(widthRole, "widthRole"));
    }

    public final ObjectProperty<M3IconButtonWidth> widthRoleProperty() {
        return widthRole;
    }

    /// Returns the preferred visual container width token.
    ///
    /// @return the preferred visual container width in pixels
    public final double getContainerWidth() {
        return containerWidth == null ? DEFAULT_CONTAINER_WIDTH : containerWidth.get();
    }

    /// Sets the preferred visual container width token.
    ///
    /// @param containerWidth the preferred visual container width in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerWidth(double containerWidth) {
        containerWidthProperty().set(M3Css.nonNegative(containerWidth, "containerWidth"));
    }

    public final StyleableDoubleProperty containerWidthProperty() {
        if (containerWidth == null) {
            containerWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_WIDTH,
                    this,
                    "containerWidth",
                    StyleableProperties.CONTAINER_WIDTH,
                    this::updateIconMetrics
            );
        }
        return containerWidth;
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

    /// Keeps icon buttons sized when container size tokens change.
    private void initializeIconMetrics() {
        containerHeightProperty().addListener(observable -> updateIconMetrics());
        containerWidthProperty().addListener(observable -> updateIconMetrics());
        sizeProperty().addListener(observable -> updateIconSizeStyle());
        buttonShapeProperty().addListener(observable -> updateIconShapeStyle());
        updateIconSizeStyle();
        updateWidthStyle();
        updateIconShapeStyle();
        updateIconMetrics();
    }

    /// Applies the current container size tokens to layout metrics.
    private void updateIconMetrics() {
        double width = getContainerWidth();
        double height = getContainerHeight();
        M3Css.setMinWidthIfUnbound(this, width);
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefWidthIfUnbound(this, width);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setMaxWidthIfUnbound(this, width);
        M3Css.setMaxHeightIfUnbound(this, height);
    }

    /// Applies the current width style class.
    private void updateWidthStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getWidthRole().styleClass(),
                M3IconButtonWidth.NARROW.styleClass(),
                M3IconButtonWidth.DEFAULT.styleClass(),
                M3IconButtonWidth.WIDE.styleClass()
        );
    }

    /// Applies the icon-button-specific class for the shared Material size.
    private void updateIconSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                iconSizeStyleClass(getSize()),
                iconSizeStyleClass(M3ButtonSize.EXTRA_SMALL),
                iconSizeStyleClass(M3ButtonSize.SMALL),
                iconSizeStyleClass(M3ButtonSize.MEDIUM),
                iconSizeStyleClass(M3ButtonSize.LARGE),
                iconSizeStyleClass(M3ButtonSize.EXTRA_LARGE)
        );
    }

    /// Applies the icon-button-specific class for the shared Material shape.
    private void updateIconShapeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                iconShapeStyleClass(getButtonShape()),
                iconShapeStyleClass(M3ButtonShape.ROUND),
                iconShapeStyleClass(M3ButtonShape.SQUARE)
        );
    }

    /// Returns the icon-button style class for a shared Material size.
    ///
    /// @param size the Material button size
    /// @return the icon-button size style class
    private static String iconSizeStyleClass(M3ButtonSize size) {
        return "m3-icon-button-" + size.cssSuffix();
    }

    /// Returns the icon-button style class for a shared Material shape.
    ///
    /// @param shape the Material button shape
    /// @return the icon-button shape style class
    private static String iconShapeStyleClass(M3ButtonShape shape) {
        return "m3-icon-button-" + shape.cssSuffix();
    }

    /// CSS metadata for M3FX icon button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the visual container width token.
        private static final CssMetaData<M3IconButton, Number> CONTAINER_WIDTH =
                new CssMetaData<>("-m3-container-width", SizeConverter.getInstance(), DEFAULT_CONTAINER_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconButton control) {
                        return M3Css.isSettable(control.containerWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconButton control) {
                        return control.containerWidthProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(M3ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_WIDTH);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
