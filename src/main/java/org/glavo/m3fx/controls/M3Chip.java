// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ChipSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 chip for compact actions, filters, inputs, or suggestions.
///
/// `M3Chip` is a selectable [ButtonBase] with Material chip variants, leading and trailing graphic slots,
/// token-backed height, shape, padding, and action dispatch. Selection state is available for filter and input
/// chips, while assist and suggestion chips can be used as command surfaces.
///
/// See [Material Design chips](https://m3.material.io/components/chips/overview).
@NotNullByDefault
public class M3Chip extends ButtonBase {
    /// The base style class for m3fx chips.
    public static final String STYLE_CLASS = "m3-chip";

    /// The selected pseudo-class used by chips.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The default chip container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 32.0;

    /// The default chip container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 8.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    // The chip variant property.
    private final ObjectProperty<M3ChipVariant> variant = new SimpleObjectProperty<>(this, "variant", M3ChipVariant.ASSIST) {
        /// Updates variant style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(M3ChipVariant.ASSIST);
                return;
            }
            updateVariantStyle();
        }
    };

    // The styleable container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable horizontal padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    // The selected state property.
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
            notifyAccessibleAttributeChanged(AccessibleAttribute.TOGGLE_STATE);
        }
    };

    /// Creates an empty assist chip.
    public M3Chip() {
        this("", null);
    }

    /// Creates an assist chip with text.
    ///
    /// @param text the text displayed by the chip
    public M3Chip(String text) {
        this(text, null);
    }

    /// Creates an assist chip with text and graphic content.
    ///
    /// @param text the text displayed by the chip
    /// @param graphic the optional graphic displayed with the text
    public M3Chip(String text, @Nullable Node graphic) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize();
    }

    /// Returns whether this chip is selected.
    ///
    /// @return `true` when this chip is selected
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this chip is selected.
    ///
    /// @param selected whether this chip should be selected
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the selected state property.
    ///
    /// @return the selected state property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// Returns the chip variant.
    ///
    /// @return the Material chip variant
    public final M3ChipVariant getVariant() {
        return variant.get();
    }

    /// Sets the chip variant.
    ///
    /// @param variant the Material chip variant
    public final void setVariant(M3ChipVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the chip variant property.
    ///
    /// @return the chip variant property
    public final ObjectProperty<M3ChipVariant> variantProperty() {
        return variant;
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
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the preferred container height token property.
    ///
    /// @return the preferred container height property
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
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
    ///
    /// @return the container shape property
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
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    ///
    /// @return the horizontal content padding property
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

    /// Returns accessibility attributes for the chip selection state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case TOGGLE_STATE -> isSelected()
                    ? AccessibleAttribute.ToggleState.CHECKED
                    : AccessibleAttribute.ToggleState.UNCHECKED;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Toggles and fires this chip.
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(!isSelected());
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default Material Design 3 chip skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ChipSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx chips.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("chip.css");
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
        setFocusTraversable(true);
        setPickOnBounds(true);
        updateVariantStyle();
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3ChipVariant.ASSIST.getStyleClass(),
                M3ChipVariant.FILTER.getStyleClass(),
                M3ChipVariant.INPUT.getStyleClass(),
                M3ChipVariant.SUGGESTION.getStyleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double padding = getHorizontalPadding();
        setMinHeight(height);
        setPrefHeight(height);
        setPadding(new Insets(0.0, padding, 0.0, padding));
    }

    /// CSS metadata for m3fx chip component tokens.
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

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
