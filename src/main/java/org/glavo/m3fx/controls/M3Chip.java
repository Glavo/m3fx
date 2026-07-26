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

/// Base class for Material Design 3 compact action and selection controls.
///
/// A chip has inherited text and a logical leading [graphic][ButtonBase#graphicProperty()], plus an optional
/// [trailing graphic][#trailingGraphicProperty()]. Concrete chip classes determine whether activation is a command
/// or a selection transition. All chip activations emit action events after any concrete selection transition.
///
/// The default container treatment is flat. Direct [M3Icon] graphics follow [iconSize][#iconSizeProperty()]; other
/// node types retain their own dimensions. Graphic nodes cannot simultaneously be children of another parent.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public abstract sealed class M3Chip extends ButtonBase
        permits M3AssistChip, M3SelectableChip, M3SuggestionChip {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-chip";

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

    /// Creates a chip with fixed semantic styling.
    ///
    /// @param text              the text displayed by the chip
    /// @param graphic           the optional graphic displayed with the text
    /// @param variantStyleClass the style class identifying the concrete chip kind
    M3Chip(String text, @Nullable Node graphic, String variantStyleClass) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize(Objects.requireNonNull(variantStyleClass, "variantStyleClass"));
    }

    /// The visual container treatment of this chip.
    ///
    /// The default value is [M3ChipStyle#FLAT]. A direct `null` assignment restores the default; bound values must
    /// be non-null.
    ///
    /// @defaultValue [M3ChipStyle#FLAT]
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

    /// Returns the chip container style.
    ///
    /// @return the Material chip container style
    public final M3ChipStyle getChipStyle() {
        return chipStyle.get();
    }

    /// Sets the chip container style.
    ///
    /// @param chipStyle the chip container style
    /// @throws NullPointerException if `chipStyle` is `null`
    public final void setChipStyle(M3ChipStyle chipStyle) {
        this.chipStyle.set(Objects.requireNonNull(chipStyle, "chipStyle"));
    }

    /// Returns the observable property that stores the chip container treatment.
    ///
    /// The property can be observed and bound. Its default value is [M3ChipStyle#FLAT], and a direct `null`
    /// assignment restores that default.
    ///
    /// @return the chip style property
    public final ObjectProperty<M3ChipStyle> chipStyleProperty() {
        return chipStyle;
    }

    /// The preferred chip container height, in logical pixels.
    ///
    /// The default value is `32.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `32.0`
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the preferred container height token.
    ///
    /// @return the preferred chip container height in logical pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred chip container height in logical pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the styleable property that stores the chip container height.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-height`, and accepts finite,
    /// non-negative values. Its default value is `32.0` logical pixels.
    ///
    /// @return the chip container height property
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

    /// The chip container corner radius, in logical pixels.
    ///
    /// The default value is `8.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the container shape radius token.
    ///
    /// @return the chip container corner radius in logical pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the chip container corner radius in logical pixels
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the styleable property that stores the chip corner radius.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-shape`, and accepts finite,
    /// non-negative values. Its default value is `8.0` logical pixels.
    ///
    /// @return the chip corner radius property
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

    /// The horizontal content padding used without a leading graphic, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in logical pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in logical pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the styleable property that stores horizontal padding for chips without graphics.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-horizontal-padding`, and accepts finite,
    /// non-negative values. Its default value is `16.0` logical pixels.
    ///
    /// @return the horizontal padding property
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

    /// The horizontal content padding used when a leading graphic is present, in logical pixels.
    ///
    /// The default value is `8.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty iconHorizontalPadding;

    /// Returns the horizontal content padding token used when a leading graphic is present.
    ///
    /// @return the horizontal content padding in logical pixels for chips with leading graphics
    public final double getIconHorizontalPadding() {
        return iconHorizontalPadding == null ? DEFAULT_ICON_HORIZONTAL_PADDING : iconHorizontalPadding.get();
    }

    /// Sets the horizontal content padding token used when a leading graphic is present.
    ///
    /// @param iconHorizontalPadding the horizontal content padding in logical pixels for chips with leading graphics
    /// @throws IllegalArgumentException if `iconHorizontalPadding` is negative or not finite
    public final void setIconHorizontalPadding(double iconHorizontalPadding) {
        iconHorizontalPaddingProperty().set(M3Css.nonNegative(iconHorizontalPadding, "iconHorizontalPadding"));
    }

    /// Returns the styleable property that stores horizontal padding for chips with graphics.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-icon-horizontal-padding`, and accepts
    /// finite, non-negative values. Its default value is `8.0` logical pixels.
    ///
    /// @return the icon-aware horizontal padding property
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

    /// The requested width and height of direct [M3Icon] graphics, in logical pixels.
    ///
    /// The default value is `18.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `18.0`
    private @Nullable StyleableDoubleProperty iconSize;

    /// Returns the icon size token applied to [M3Icon] graphics.
    ///
    /// @return the icon graphic size in logical pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the icon size token applied to [M3Icon] graphics.
    ///
    /// @param iconSize the icon graphic size in logical pixels
    /// @throws IllegalArgumentException if `iconSize` is negative or not finite
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the styleable property that stores the managed icon size.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-chip-icon-size`, and accepts finite,
    /// non-negative values. Its default value is `18.0` logical pixels.
    ///
    /// @return the managed icon size property
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

    /// The node displayed at the logical trailing edge of the chip.
    ///
    /// The default value is `null`. This slot does not automatically receive independent action semantics; use an
    /// actionable node when a separate trailing action is required.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> trailingGraphic =
            new SimpleObjectProperty<>(this, "trailingGraphic") {
                /// Recomputes content padding and layout when the trailing slot changes.
                @Override
                protected void invalidated() {
                    updateMetrics();
                    requestLayout();
                }
            };

    /// Returns the optional graphic shown at the logical trailing edge of this chip.
    ///
    /// The inherited [ButtonBase#graphicProperty()] is the logical leading slot. The trailing slot accepts any
    /// node, including an [M3IconButton] when an input chip needs a separately actionable remove affordance.
    ///
    /// @return the trailing graphic, or `null` when the trailing slot is empty
    public final @Nullable Node getTrailingGraphic() {
        return trailingGraphic.get();
    }

    /// Sets the optional graphic shown at the logical trailing edge of this chip.
    ///
    /// @param trailingGraphic the trailing graphic, or `null` to clear the slot
    public final void setTrailingGraphic(@Nullable Node trailingGraphic) {
        this.trailingGraphic.set(trailingGraphic);
    }

    /// Returns the observable property that stores the optional trailing graphic.
    ///
    /// The property can be observed and bound. Its default value is `null`; changing it updates content metrics
    /// and layout.
    ///
    /// @return the trailing graphic property
    public final ObjectProperty<@Nullable Node> trailingGraphicProperty() {
        return trailingGraphic;
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

    /// Fires an action event unless this chip is disabled.
    ///
    /// Concrete selectable chips may override this method to update selection before dispatching the event.
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
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
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
        if (getGraphic() instanceof M3IconGraphic icon) {
            icon.setIconSize(getIconSize());
        }
        if (getTrailingGraphic() instanceof M3IconGraphic icon) {
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
