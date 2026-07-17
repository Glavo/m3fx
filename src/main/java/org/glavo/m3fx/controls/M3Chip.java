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
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ChipSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// Base class for Material Design 3 chips.
///
/// This class defines the common text, graphic, trailing-graphic, sizing, shape, and action contracts shared by
/// [M3AssistChip], [M3FilterChip], [M3InputChip], and [M3SuggestionChip]. Use one of those concrete classes so the
/// chip's interaction semantics are fixed for its lifetime. Selectable chips additionally derive from
/// [M3SelectableChip].
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public abstract sealed class M3Chip extends ButtonBase
        permits M3AssistChip, M3SelectableChip, M3SuggestionChip {
    /// The base style class for M3FX chips.
    public static final String STYLE_CLASS = "m3-chip";

    /// The style class applied to the logical leading graphic.
    public static final String LEADING_GRAPHIC_STYLE_CLASS = "m3-chip-leading-graphic";

    /// The style class applied to the logical trailing graphic.
    public static final String TRAILING_GRAPHIC_STYLE_CLASS = "m3-chip-trailing-graphic";

    /// The default chip container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 32.0;

    /// The default chip container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 8.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default horizontal content padding when a leading graphic is present.
    private static final double DEFAULT_ICON_HORIZONTAL_PADDING = 8.0;

    /// The default size for icon graphics.
    private static final double DEFAULT_ICON_SIZE = 18.0;

    /// The chip container style property.
    private final ObjectProperty<M3ChipStyle> chipStyle =
            new SimpleObjectProperty<>(this, "chipStyle", M3ChipStyle.FLAT) {
                /// Updates chip style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3ChipStyle.FLAT);
                        return;
                    }
                    updateChipStyle();
                }
            };

    /// The styleable container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    /// The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    /// The styleable horizontal padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// The styleable horizontal padding token used when a leading graphic is present.
    private @Nullable StyleableDoubleProperty iconHorizontalPadding;

    /// The styleable icon size token.
    private @Nullable StyleableDoubleProperty iconSize;

    /// The optional logical trailing graphic property.
    private final ObjectProperty<@Nullable Node> trailingGraphic =
            new SimpleObjectProperty<>(this, "trailingGraphic") {
                /// Recomputes content padding and layout when the trailing slot changes.
                @Override
                protected void invalidated() {
                    updateMetrics();
                    requestLayout();
                }
            };

    /// Creates a chip with fixed semantic styling.
    ///
    /// @param text the text displayed by the chip
    /// @param graphic the optional graphic displayed with the text
    /// @param variantStyleClass the style class identifying the concrete chip kind
    M3Chip(String text, @Nullable Node graphic, String variantStyleClass) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize(Objects.requireNonNull(variantStyleClass, "variantStyleClass"));
    }

    /// Returns the optional graphic shown at the logical trailing edge of this chip.
    ///
    /// The inherited [ButtonBase#graphicProperty()] is the logical leading slot. The trailing slot accepts any
    /// node, including an [M3IconButton] when an input chip needs a separately actionable remove affordance.
    ///
    /// @return the trailing graphic, or null when the trailing slot is empty
    public final @Nullable Node getTrailingGraphic() {
        return trailingGraphic.get();
    }

    /// Sets the optional graphic shown at the logical trailing edge of this chip.
    ///
    /// @param trailingGraphic the trailing graphic, or null to clear the slot
    public final void setTrailingGraphic(@Nullable Node trailingGraphic) {
        this.trailingGraphic.set(trailingGraphic);
    }

    public final ObjectProperty<@Nullable Node> trailingGraphicProperty() {
        return trailingGraphic;
    }

    /// Returns the chip container style.
    ///
    /// @return the Material chip container style
    public final M3ChipStyle getChipStyle() {
        return chipStyle.get();
    }

    /// Sets the chip container style.
    ///
    /// @param chipStyle the chip container style
    /// @throws NullPointerException if any required argument is `null`
    public final void setChipStyle(M3ChipStyle chipStyle) {
        this.chipStyle.set(Objects.requireNonNull(chipStyle, "chipStyle"));
    }

    public final ObjectProperty<M3ChipStyle> chipStyleProperty() {
        return chipStyle;
    }

    /// Returns the preferred container height token.
    ///
    /// @return the preferred chip container height in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred chip container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    this,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT,
                    this::updateMetrics
            );
        }
        return containerHeight;
    }

    /// Returns the container shape radius token.
    ///
    /// @return the chip container corner radius in pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the chip container corner radius in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    this,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE,
                    this::requestLayout
            );
        }
        return containerShape;
    }

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    this,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING,
                    this::updateMetrics
            );
        }
        return horizontalPadding;
    }

    /// Returns the horizontal content padding token used when a leading graphic is present.
    ///
    /// @return the horizontal content padding in pixels for chips with graphics
    public final double getIconHorizontalPadding() {
        return iconHorizontalPadding == null ? DEFAULT_ICON_HORIZONTAL_PADDING : iconHorizontalPadding.get();
    }

    /// Sets the horizontal content padding token used when a leading graphic is present.
    ///
    /// @param iconHorizontalPadding the horizontal content padding in pixels for chips with graphics
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setIconHorizontalPadding(double iconHorizontalPadding) {
        iconHorizontalPaddingProperty().set(M3Css.nonNegative(iconHorizontalPadding, "iconHorizontalPadding"));
    }

    public final StyleableDoubleProperty iconHorizontalPaddingProperty() {
        if (iconHorizontalPadding == null) {
            iconHorizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_HORIZONTAL_PADDING,
                    this,
                    "iconHorizontalPadding",
                    StyleableProperties.ICON_HORIZONTAL_PADDING,
                    this::updateMetrics
            );
        }
        return iconHorizontalPadding;
    }

    /// Returns the icon size token applied to [M3Icon] graphics.
    ///
    /// @return the icon graphic size in pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the icon size token applied to [M3Icon] graphics.
    ///
    /// @param iconSize the icon graphic size in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::updateGraphicMetrics
            );
        }
        return iconSize;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Fires this chip's action event.
    @Override
    public void fire() {
        if (!isDisabled()) {
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default Material Design 3 chip skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ChipSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX chips.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("chip.css");
    }

    /// Adds base and concrete-kind style classes.
    private void initialize(String variantStyleClass) {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        getStyleClass().add(variantStyleClass);
        setFocusTraversable(true);
        setPickOnBounds(true);
        setAccessibleRole(AccessibleRole.BUTTON);
        graphicProperty().addListener(observable -> updateMetrics());
        updateChipStyle();
        updateMetrics();
    }

    /// Applies the current chip style class.
    private void updateChipStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getChipStyle().styleClass(),
                M3ChipStyle.FLAT.styleClass(),
                M3ChipStyle.ELEVATED.styleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double leadingPadding = getGraphic() == null ? getHorizontalPadding() : getIconHorizontalPadding();
        double trailingPadding = getTrailingGraphic() == null ? getHorizontalPadding() : getIconHorizontalPadding();
        Insets padding = new Insets(0.0, trailingPadding, 0.0, leadingPadding);
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setPaddingIfUnbound(this, padding);
        updateGraphicMetrics();
    }

    /// Applies graphic-specific component tokens to supported graphic nodes.
    private void updateGraphicMetrics() {
        if (getGraphic() instanceof M3Icon icon) {
            icon.setIconSize(getIconSize());
        }
        if (getTrailingGraphic() instanceof M3Icon icon) {
            icon.setIconSize(getIconSize());
        }
    }


    /// CSS metadata for M3FX chip component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3Chip, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Chip control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Chip control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Chip, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Chip control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Chip control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3Chip, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Chip control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Chip control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token used when a leading graphic is present.
        private static final CssMetaData<M3Chip, Number> ICON_HORIZONTAL_PADDING =
                new CssMetaData<>(
                        "-m3-icon-horizontal-padding",
                        SizeConverter.getInstance(),
                        DEFAULT_ICON_HORIZONTAL_PADDING
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Chip control) {
                        return M3Css.isSettable(control.iconHorizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Chip control) {
                        return control.iconHorizontalPaddingProperty();
                    }
                };

        /// CSS metadata for the icon size token applied to [M3Icon] graphics.
        private static final CssMetaData<M3Chip, Number> ICON_SIZE =
                new CssMetaData<>("-m3-chip-icon-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Chip control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Chip control) {
                        return control.iconSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(ICON_HORIZONTAL_PADDING);
            styleables.add(ICON_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
