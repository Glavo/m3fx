package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 segmented button.
@NotNullByDefault
public class M3SegmentedButton extends ToggleButton {
    /// The base style class for m3fx segmented buttons.
    public static final String STYLE_CLASS = "m3-segmented-button";

    /// The default segmented button container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 40.0;

    /// The default segmented button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 999.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 12.0;

    /// The styleable container height token.
    private StyleableDoubleProperty containerHeight;

    /// The styleable container shape token.
    private StyleableDoubleProperty containerShape;

    /// The styleable horizontal padding token.
    private StyleableDoubleProperty horizontalPadding;

    /// Creates an empty segmented button.
    public M3SegmentedButton() {
        this("");
    }

    /// Creates a segmented button with text.
    public M3SegmentedButton(String text) {
        super(text);
        initialize();
    }

    /// Creates a segmented button with text and graphic content.
    public M3SegmentedButton(String text, @Nullable Node graphic) {
        super(text, graphic);
        initialize();
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
                    return M3SegmentedButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3SegmentedButton, Number> getCssMetaData() {
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
                    return M3SegmentedButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3SegmentedButton, Number> getCssMetaData() {
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
                    return M3SegmentedButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "horizontalPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3SegmentedButton, Number> getCssMetaData() {
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

    /// Returns the user-agent stylesheet for m3fx segmented buttons.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("segmented-button.css");
    }

    /// Adds base style classes and applies token metrics.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double padding = getHorizontalPadding();
        setMinHeight(height);
        setPrefHeight(height);
        setPadding(new Insets(0.0, padding, 0.0, padding));
    }

    /// CSS metadata for m3fx segmented button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3SegmentedButton, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SegmentedButton control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SegmentedButton control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3SegmentedButton, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SegmentedButton control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SegmentedButton control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3SegmentedButton, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SegmentedButton control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SegmentedButton control) {
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
