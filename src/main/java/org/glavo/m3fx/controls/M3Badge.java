// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BadgeSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 badge.
@NotNullByDefault
public class M3Badge extends Control {
    /// The base style class for m3fx badges.
    public static final String STYLE_CLASS = "m3-badge";

    /// The default small dot badge size.
    private static final double DEFAULT_SMALL_SIZE = 6.0;

    /// The default large badge height.
    private static final double DEFAULT_LARGE_HEIGHT = 16.0;

    /// The default minimum width for text badges.
    private static final double DEFAULT_LARGE_MIN_WIDTH = 16.0;

    /// The default text badge container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 8.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 4.0;

    /// The badge text property.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /// The maximum display text length before the badge uses an overflow suffix.
    private final IntegerProperty maxCharacterCount = new SimpleIntegerProperty(this, "maxCharacterCount", 3) {
        /// Validates assigned maximum character counts.
        @Override
        protected void invalidated() {
            if (get() < 1) {
                set(1);
            }
        }
    };

    /// The styleable small dot badge size token.
    private StyleableDoubleProperty smallSize;

    /// The styleable large badge height token.
    private StyleableDoubleProperty largeHeight;

    /// The styleable large badge minimum width token.
    private StyleableDoubleProperty largeMinWidth;

    /// The styleable container shape token.
    private StyleableDoubleProperty containerShape;

    /// The styleable horizontal padding token.
    private StyleableDoubleProperty horizontalPadding;

    /// Creates a small dot badge.
    public M3Badge() {
        this("");
    }

    /// Creates a badge with text.
    public M3Badge(String text) {
        M3ControlStyles.add(this, STYLE_CLASS);
        setText(text);
    }

    /// Returns the badge text.
    public final String getText() {
        return text.get();
    }

    /// Sets the badge text.
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the badge text property.
    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the maximum display text length before an overflow suffix is used.
    public final int getMaxCharacterCount() {
        return maxCharacterCount.get();
    }

    /// Sets the maximum display text length before an overflow suffix is used.
    public final void setMaxCharacterCount(int maxCharacterCount) {
        if (maxCharacterCount < 1) {
            throw new IllegalArgumentException("maxCharacterCount must be positive");
        }
        this.maxCharacterCount.set(maxCharacterCount);
    }

    /// Returns the maximum display text length property.
    public final IntegerProperty maxCharacterCountProperty() {
        return maxCharacterCount;
    }

    /// Returns the text rendered by this badge after overflow handling.
    public final String getDisplayText() {
        String value = getText();
        int maximum = getMaxCharacterCount();
        if (value.isEmpty() || value.length() <= maximum) {
            return value;
        }
        return value.substring(0, maximum) + "+";
    }

    /// Returns the small dot badge size token.
    public final double getSmallSize() {
        return smallSize == null ? DEFAULT_SMALL_SIZE : smallSize.get();
    }

    /// Sets the small dot badge size token.
    public final void setSmallSize(double smallSize) {
        smallSizeProperty().set(M3Css.nonNegative(smallSize, "smallSize"));
    }

    /// Returns the small dot badge size token property.
    public final StyleableDoubleProperty smallSizeProperty() {
        if (smallSize == null) {
            smallSize = new StyleableDoubleProperty(DEFAULT_SMALL_SIZE) {
                /// Validates updated size tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "smallSize"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Badge.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "smallSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Badge, Number> getCssMetaData() {
                    return StyleableProperties.SMALL_SIZE;
                }
            };
        }
        return smallSize;
    }

    /// Returns the large badge height token.
    public final double getLargeHeight() {
        return largeHeight == null ? DEFAULT_LARGE_HEIGHT : largeHeight.get();
    }

    /// Sets the large badge height token.
    public final void setLargeHeight(double largeHeight) {
        largeHeightProperty().set(M3Css.nonNegative(largeHeight, "largeHeight"));
    }

    /// Returns the large badge height token property.
    public final StyleableDoubleProperty largeHeightProperty() {
        if (largeHeight == null) {
            largeHeight = new StyleableDoubleProperty(DEFAULT_LARGE_HEIGHT) {
                /// Validates updated size tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "largeHeight"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Badge.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "largeHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Badge, Number> getCssMetaData() {
                    return StyleableProperties.LARGE_HEIGHT;
                }
            };
        }
        return largeHeight;
    }

    /// Returns the large badge minimum width token.
    public final double getLargeMinWidth() {
        return largeMinWidth == null ? DEFAULT_LARGE_MIN_WIDTH : largeMinWidth.get();
    }

    /// Sets the large badge minimum width token.
    public final void setLargeMinWidth(double largeMinWidth) {
        largeMinWidthProperty().set(M3Css.nonNegative(largeMinWidth, "largeMinWidth"));
    }

    /// Returns the large badge minimum width token property.
    public final StyleableDoubleProperty largeMinWidthProperty() {
        if (largeMinWidth == null) {
            largeMinWidth = new StyleableDoubleProperty(DEFAULT_LARGE_MIN_WIDTH) {
                /// Validates updated size tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "largeMinWidth"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Badge.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "largeMinWidth";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Badge, Number> getCssMetaData() {
                    return StyleableProperties.LARGE_MIN_WIDTH;
                }
            };
        }
        return largeMinWidth;
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
                    set(M3Css.nonNegative(get(), "containerShape"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Badge.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Badge, Number> getCssMetaData() {
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
                /// Validates updated padding tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "horizontalPadding"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Badge.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "horizontalPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Badge, Number> getCssMetaData() {
                    return StyleableProperties.HORIZONTAL_PADDING;
                }
            };
        }
        return horizontalPadding;
    }

    /// Creates the default badge skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BadgeSkin(this);
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

    /// Returns the user-agent stylesheet for m3fx badges.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("badge.css");
    }

    /// CSS metadata for m3fx badge component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the small size token.
        private static final CssMetaData<M3Badge, Number> SMALL_SIZE =
                new CssMetaData<>("-m3-small-size", SizeConverter.getInstance(), DEFAULT_SMALL_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.smallSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.smallSizeProperty();
                    }
                };

        /// CSS metadata for the large height token.
        private static final CssMetaData<M3Badge, Number> LARGE_HEIGHT =
                new CssMetaData<>("-m3-large-height", SizeConverter.getInstance(), DEFAULT_LARGE_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.largeHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.largeHeightProperty();
                    }
                };

        /// CSS metadata for the large minimum width token.
        private static final CssMetaData<M3Badge, Number> LARGE_MIN_WIDTH =
                new CssMetaData<>("-m3-large-min-width", SizeConverter.getInstance(), DEFAULT_LARGE_MIN_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.largeMinWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.largeMinWidthProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Badge, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3Badge, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SMALL_SIZE);
            styleables.add(LARGE_HEIGHT);
            styleables.add(LARGE_MIN_WIDTH);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
