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
import javafx.geometry.Insets;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 surface container for arbitrary content.
@NotNullByDefault
public class M3Surface extends StackPane {
    /// The base style class for M3FX surfaces.
    public static final String STYLE_CLASS = "m3-surface";

    /// The default surface container shape.
    private static final double DEFAULT_CONTAINER_SHAPE = 12.0;

    /// The default surface content padding.
    private static final double DEFAULT_CONTENT_PADDING = 16.0;

    /// The surface color variant property.
    private final ObjectProperty<M3SurfaceVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3SurfaceVariant.CONTAINER) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SurfaceVariant.CONTAINER);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// The surface elevation property.
    private final ObjectProperty<M3SurfaceElevation> elevation =
            new SimpleObjectProperty<>(this, "elevation", M3SurfaceElevation.LEVEL0) {
                /// Updates elevation style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SurfaceElevation.LEVEL0);
                        return;
                    }
                    updateElevationStyle();
                }
            };

    /// The styleable container shape token.
    private StyleableDoubleProperty containerShape;

    /// The styleable content padding token.
    private StyleableDoubleProperty contentPadding;

    /// Creates an empty surface.
    public M3Surface() {
        initialize();
    }

    /// Creates a surface with content nodes.
    public M3Surface(Node... children) {
        initialize();
        getChildren().addAll(children);
    }

    /// Returns the surface color variant.
    public final M3SurfaceVariant getVariant() {
        return variant.get();
    }

    /// Sets the surface color variant.
    public final void setVariant(M3SurfaceVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the surface color variant property.
    public final ObjectProperty<M3SurfaceVariant> variantProperty() {
        return variant;
    }

    /// Returns the surface elevation level.
    public final M3SurfaceElevation getElevation() {
        return elevation.get();
    }

    /// Sets the surface elevation level.
    public final void setElevation(M3SurfaceElevation elevation) {
        this.elevation.set(Objects.requireNonNull(elevation, "elevation"));
    }

    /// Returns the surface elevation property.
    public final ObjectProperty<M3SurfaceElevation> elevationProperty() {
        return elevation;
    }

    /// Returns the surface container shape token.
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the surface container shape token.
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the surface container shape token property.
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
                    return M3Surface.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Surface, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
        }
        return containerShape;
    }

    /// Returns the surface content padding token.
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the surface content padding token.
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the surface content padding token property.
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = new StyleableDoubleProperty(DEFAULT_CONTENT_PADDING) {
                /// Applies updated padding tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "contentPadding");
                    updatePadding();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Surface.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "contentPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Surface, Number> getCssMetaData() {
                    return StyleableProperties.CONTENT_PADDING;
                }
            };
        }
        return contentPadding;
    }

    /// Returns the user-agent stylesheet for M3FX surfaces.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("surface.css");
    }

    /// Returns the CSS metadata for this node class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this node.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /// Initializes style classes and default metrics.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        updateVariantStyle();
        updateElevationStyle();
        updatePadding();
    }

    /// Applies the current color variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3SurfaceVariant.SURFACE.getStyleClass(),
                M3SurfaceVariant.CONTAINER_LOWEST.getStyleClass(),
                M3SurfaceVariant.CONTAINER_LOW.getStyleClass(),
                M3SurfaceVariant.CONTAINER.getStyleClass(),
                M3SurfaceVariant.CONTAINER_HIGH.getStyleClass(),
                M3SurfaceVariant.CONTAINER_HIGHEST.getStyleClass(),
                M3SurfaceVariant.PRIMARY_CONTAINER.getStyleClass(),
                M3SurfaceVariant.SECONDARY_CONTAINER.getStyleClass(),
                M3SurfaceVariant.TERTIARY_CONTAINER.getStyleClass()
        );
    }

    /// Applies the current elevation style class.
    private void updateElevationStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getElevation().getStyleClass(),
                M3SurfaceElevation.LEVEL0.getStyleClass(),
                M3SurfaceElevation.LEVEL1.getStyleClass(),
                M3SurfaceElevation.LEVEL2.getStyleClass(),
                M3SurfaceElevation.LEVEL3.getStyleClass(),
                M3SurfaceElevation.LEVEL4.getStyleClass(),
                M3SurfaceElevation.LEVEL5.getStyleClass()
        );
    }

    /// Applies content padding from tokens.
    private void updatePadding() {
        double padding = getContentPadding();
        setPadding(new Insets(padding));
    }

    /// CSS metadata for M3FX surface tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Surface, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Surface control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Surface control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3Surface, Number> CONTENT_PADDING =
                new CssMetaData<>("-m3-content-padding", SizeConverter.getInstance(), DEFAULT_CONTENT_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Surface control) {
                        return M3Css.isSettable(control.contentPaddingProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Surface control) {
                        return control.contentPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(StackPane.getClassCssMetaData());
            styleables.add(CONTAINER_SHAPE);
            styleables.add(CONTENT_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
