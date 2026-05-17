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

/// A Material Design 3 chip.
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

    /// The chip variant property.
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

    /// The styleable container height token.
    private StyleableDoubleProperty containerHeight;

    /// The styleable container shape token.
    private StyleableDoubleProperty containerShape;

    /// The styleable horizontal padding token.
    private StyleableDoubleProperty horizontalPadding;

    /// The selected state property.
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
        }
    };

    /// Creates an empty assist chip.
    public M3Chip() {
        this("", null);
    }

    /// Creates an assist chip with text.
    public M3Chip(String text) {
        this(text, null);
    }

    /// Creates an assist chip with text and graphic content.
    public M3Chip(String text, @Nullable Node graphic) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize();
    }

    /// Creates a chip with text and the requested variant.
    public static M3Chip withVariant(String text, M3ChipVariant variant) {
        return withVariant(text, null, variant, false);
    }

    /// Creates a chip with text, the requested variant, and selected state.
    public static M3Chip withVariant(String text, M3ChipVariant variant, boolean selected) {
        return withVariant(text, null, variant, selected);
    }

    /// Creates a chip with text, graphic content, and the requested variant.
    public static M3Chip withVariant(String text, @Nullable Node graphic, M3ChipVariant variant) {
        return withVariant(text, graphic, variant, false);
    }

    /// Creates a chip with text, graphic content, the requested variant, and selected state.
    public static M3Chip withVariant(
            String text,
            @Nullable Node graphic,
            M3ChipVariant variant,
            boolean selected
    ) {
        M3Chip chip = new M3Chip(text, graphic);
        chip.setVariant(variant);
        chip.setSelected(selected);
        return chip;
    }

    /// Returns whether this chip is selected.
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this chip is selected.
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the selected state property.
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// Returns the chip variant.
    public final M3ChipVariant getVariant() {
        return variant.get();
    }

    /// Sets the chip variant.
    public final void setVariant(M3ChipVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the chip variant property.
    public final ObjectProperty<M3ChipVariant> variantProperty() {
        return variant;
    }

    /// Returns the preferred container height token.
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the preferred container height token property.
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = new StyleableDoubleProperty(DEFAULT_CONTAINER_HEIGHT) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "containerHeight");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Chip.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Chip, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_HEIGHT;
                }
            };
        }
        return containerHeight;
    }

    /// Returns the container shape radius token.
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = new StyleableDoubleProperty(DEFAULT_CONTAINER_SHAPE) {
                /// Validates updated shape tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "containerShape");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Chip.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Chip, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
        }
        return containerShape;
    }

    /// Returns the horizontal content padding token.
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = new StyleableDoubleProperty(DEFAULT_HORIZONTAL_PADDING) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "horizontalPadding");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Chip.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "horizontalPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Chip, Number> getCssMetaData() {
                    return StyleableProperties.HORIZONTAL_PADDING;
                }
            };
        }
        return horizontalPadding;
    }

    /// Returns the CSS metadata for this control class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
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
        setFocusTraversable(true);
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
