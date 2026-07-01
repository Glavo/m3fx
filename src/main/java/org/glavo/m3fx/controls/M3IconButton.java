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
public class M3IconButton extends M3Button {
    /// The base style class for M3FX icon buttons.
    public static final String STYLE_CLASS = "m3-icon-button";

    /// The default icon button size.
    private static final M3IconButtonSize DEFAULT_SIZE = M3IconButtonSize.SMALL;

    /// The default icon button width role.
    private static final M3IconButtonWidth DEFAULT_WIDTH = M3IconButtonWidth.DEFAULT;

    /// The default icon button shape.
    private static final M3IconButtonShape DEFAULT_SHAPE = M3IconButtonShape.ROUND;

    /// The default icon button container width.
    private static final double DEFAULT_CONTAINER_WIDTH = 40.0;

    /// The default icon button glyph size.
    private static final double DEFAULT_ICON_SIZE = 24.0;

    // The icon button size property.
    private final ObjectProperty<M3IconButtonSize> size =
            new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE) {
                /// Updates size style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_SIZE);
                        return;
                    }
                    updateSizeStyle();
                }
            };

    // The icon button width role property.
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

    // The icon button shape property.
    private final ObjectProperty<M3IconButtonShape> iconButtonShape =
            new SimpleObjectProperty<>(this, "iconButtonShape", DEFAULT_SHAPE) {
                /// Updates shape style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_SHAPE);
                        return;
                    }
                    updateShapeStyle();
                }
            };

    // The styleable visual container width token.
    private @Nullable StyleableDoubleProperty containerWidth;

    // The styleable icon glyph size token.
    private @Nullable StyleableDoubleProperty iconSize;

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

    /// Returns the icon button size.
    ///
    /// @return the icon button size
    public final M3IconButtonSize getSize() {
        return size.get();
    }

    /// Sets the icon button size.
    ///
    /// @param size the icon button size
    public final void setSize(M3IconButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the icon button size property.
    ///
    /// @return the icon button size property
    public final ObjectProperty<M3IconButtonSize> sizeProperty() {
        return size;
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
    public final void setWidthRole(M3IconButtonWidth widthRole) {
        this.widthRole.set(Objects.requireNonNull(widthRole, "widthRole"));
    }

    /// Returns the icon button width role property.
    ///
    /// @return the icon button width role property
    public final ObjectProperty<M3IconButtonWidth> widthRoleProperty() {
        return widthRole;
    }

    /// Returns the icon button shape.
    ///
    /// @return the icon button shape
    public final M3IconButtonShape getIconButtonShape() {
        return iconButtonShape.get();
    }

    /// Sets the icon button shape.
    ///
    /// @param shape the icon button shape
    public final void setIconButtonShape(M3IconButtonShape shape) {
        this.iconButtonShape.set(Objects.requireNonNull(shape, "shape"));
    }

    /// Returns the icon button shape property.
    ///
    /// @return the icon button shape property
    public final ObjectProperty<M3IconButtonShape> iconButtonShapeProperty() {
        return iconButtonShape;
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
    public final void setContainerWidth(double containerWidth) {
        containerWidthProperty().set(M3Css.nonNegative(containerWidth, "containerWidth"));
    }

    /// Returns the preferred visual container width token property.
    ///
    /// @return the preferred visual container width property
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

    /// Returns the icon glyph size token.
    ///
    /// @return the icon glyph size in pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the icon glyph size token.
    ///
    /// @param iconSize the icon glyph size in pixels
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the icon glyph size token property.
    ///
    /// @return the icon glyph size property
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::updateIconMetrics
            );
        }
        return iconSize;
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
        iconSizeProperty().addListener(observable -> updateM3IconGraphicSize());
        graphicProperty().addListener(observable -> updateM3IconGraphicSize());
        updateSizeStyle();
        updateWidthStyle();
        updateShapeStyle();
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
        updateM3IconGraphicSize();
    }

    /// Applies the resolved icon button size token to direct M3FX icon graphics.
    private void updateM3IconGraphicSize() {
        if (getGraphic() instanceof M3Icon icon) {
            icon.setIconSize(getIconSize());
        }
    }

    /// Applies the current size style class.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().getStyleClass(),
                M3IconButtonSize.EXTRA_SMALL.getStyleClass(),
                M3IconButtonSize.SMALL.getStyleClass(),
                M3IconButtonSize.MEDIUM.getStyleClass(),
                M3IconButtonSize.LARGE.getStyleClass(),
                M3IconButtonSize.EXTRA_LARGE.getStyleClass()
        );
    }

    /// Applies the current width style class.
    private void updateWidthStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getWidthRole().getStyleClass(),
                M3IconButtonWidth.NARROW.getStyleClass(),
                M3IconButtonWidth.DEFAULT.getStyleClass(),
                M3IconButtonWidth.WIDE.getStyleClass()
        );
    }

    /// Applies the current shape style class.
    private void updateShapeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getIconButtonShape().getStyleClass(),
                M3IconButtonShape.ROUND.getStyleClass(),
                M3IconButtonShape.SQUARE.getStyleClass()
        );
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

        /// CSS metadata for the icon glyph size token.
        private static final CssMetaData<M3IconButton, Number> ICON_SIZE =
                new CssMetaData<>("-m3-icon-button-icon-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconButton control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconButton control) {
                        return control.iconSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(M3Button.getClassCssMetaData());
            styleables.add(CONTAINER_WIDTH);
            styleables.add(ICON_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
