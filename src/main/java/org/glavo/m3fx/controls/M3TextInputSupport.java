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
import javafx.geometry.Insets;
import javafx.scene.control.TextInputControl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// Provides shared state and token plumbing for Material text input controls.
@NotNullByDefault
final class M3TextInputSupport<C extends TextInputControl & M3TextInput> {
    /// The default single-line text input container height.
    static final double DEFAULT_FIELD_CONTAINER_HEIGHT = 56.0;

    /// The default multiline text input container height.
    static final double DEFAULT_AREA_CONTAINER_HEIGHT = 112.0;

    /// The default text input container shape radius.
    static final double DEFAULT_CONTAINER_SHAPE = 4.0;

    /// The default horizontal content padding.
    static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default single-line vertical content padding.
    static final double DEFAULT_FIELD_VERTICAL_PADDING = 8.0;

    /// The default multiline vertical content padding.
    static final double DEFAULT_AREA_VERTICAL_PADDING = 16.0;

    /// The pseudo-class used while an input renders its error state.
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

    /// The owning JavaFX text input control.
    private final C control;

    /// The default container height for the owning control kind.
    private final double defaultContainerHeight;

    /// The default vertical padding for the owning control kind.
    private final double defaultVerticalPadding;

    /// The CSS metadata for the container height token.
    private final CssMetaData<C, Number> containerHeightCssMetaData;

    /// The CSS metadata for the container shape token.
    private final CssMetaData<C, Number> containerShapeCssMetaData;

    /// The CSS metadata for the horizontal padding token.
    private final CssMetaData<C, Number> horizontalPaddingCssMetaData;

    /// The CSS metadata for the optional vertical padding token.
    private final @Nullable CssMetaData<C, Number> verticalPaddingCssMetaData;

    // The visual variant property.
    private final ObjectProperty<M3TextInputVariant> variant;

    /// Whether the owning input should render its error state.
    private final BooleanProperty error;

    // The styleable container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable horizontal padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    // The styleable vertical padding token.
    private @Nullable StyleableDoubleProperty verticalPadding;

    /// Creates shared text input state for a concrete JavaFX text input control.
    M3TextInputSupport(
            C control,
            double defaultContainerHeight,
            double defaultVerticalPadding,
            CssMetaData<C, Number> containerHeightCssMetaData,
            CssMetaData<C, Number> containerShapeCssMetaData,
            CssMetaData<C, Number> horizontalPaddingCssMetaData,
            @Nullable CssMetaData<C, Number> verticalPaddingCssMetaData
    ) {
        this.control = Objects.requireNonNull(control, "control");
        this.defaultContainerHeight = defaultContainerHeight;
        this.defaultVerticalPadding = defaultVerticalPadding;
        this.containerHeightCssMetaData = Objects.requireNonNull(
                containerHeightCssMetaData,
                "containerHeightCssMetaData"
        );
        this.containerShapeCssMetaData = Objects.requireNonNull(
                containerShapeCssMetaData,
                "containerShapeCssMetaData"
        );
        this.horizontalPaddingCssMetaData = Objects.requireNonNull(
                horizontalPaddingCssMetaData,
                "horizontalPaddingCssMetaData"
        );
        this.verticalPaddingCssMetaData = verticalPaddingCssMetaData;
        this.variant = new SimpleObjectProperty<>(control, "variant", M3TextInputVariant.FILLED) {
            /// Updates variant style classes when the property changes.
            @Override
            protected void invalidated() {
                if (get() == null) {
                    set(M3TextInputVariant.FILLED);
                    return;
                }
                updateVariantStyle();
            }
        };
        this.error = new SimpleBooleanProperty(control, "error") {
            /// Updates the error pseudo-class when the property changes.
            @Override
            protected void invalidated() {
                control.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, get());
            }
        };
    }

    /// Creates CSS metadata for a non-negative text input size token.
    static <C extends TextInputControl & M3TextInput> CssMetaData<C, Number> createSizeCssMetaData(
            String property,
            double initialValue,
            StyleablePropertyProvider<C> provider
    ) {
        return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
            /// Returns whether this property can be set by CSS.
            @Override
            public boolean isSettable(C control) {
                return M3Css.isSettable(provider.property(control));
            }

            /// Returns the styleable property for a control.
            @Override
            public StyleableProperty<Number> getStyleableProperty(C control) {
                return provider.property(control);
            }
        };
    }

    /// Combines JavaFX text input CSS metadata with three Material text input token entries.
    static <C extends TextInputControl & M3TextInput> List<CssMetaData<? extends Styleable, ?>> cssMetaData(
            List<CssMetaData<? extends Styleable, ?>> baseCssMetaData,
            CssMetaData<C, Number> containerHeightCssMetaData,
            CssMetaData<C, Number> containerShapeCssMetaData,
            CssMetaData<C, Number> horizontalPaddingCssMetaData
    ) {
        List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(baseCssMetaData);
        styleables.add(containerHeightCssMetaData);
        styleables.add(containerShapeCssMetaData);
        styleables.add(horizontalPaddingCssMetaData);
        return Collections.unmodifiableList(styleables);
    }

    /// Combines JavaFX text input CSS metadata with four Material text input token entries.
    static <C extends TextInputControl & M3TextInput> List<CssMetaData<? extends Styleable, ?>> cssMetaData(
            List<CssMetaData<? extends Styleable, ?>> baseCssMetaData,
            CssMetaData<C, Number> containerHeightCssMetaData,
            CssMetaData<C, Number> containerShapeCssMetaData,
            CssMetaData<C, Number> horizontalPaddingCssMetaData,
            CssMetaData<C, Number> verticalPaddingCssMetaData
    ) {
        List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(cssMetaData(
                baseCssMetaData,
                containerHeightCssMetaData,
                containerShapeCssMetaData,
                horizontalPaddingCssMetaData
        ));
        styleables.add(verticalPaddingCssMetaData);
        return Collections.unmodifiableList(styleables);
    }

    /// Adds base styles and applies default text input metrics.
    void initialize(String styleClass) {
        M3ControlStyles.add(control, styleClass);
        updateVariantStyle();
        updateMetrics();
    }

    /// Returns the text input variant.
    M3TextInputVariant getVariant() {
        return variant.get();
    }

    /// Sets the text input variant.
    void setVariant(M3TextInputVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the text input variant property.
    ObjectProperty<M3TextInputVariant> variantProperty() {
        return variant;
    }

    /// Returns whether this input renders its error state.
    boolean isError() {
        return error.get();
    }

    /// Sets whether this input renders its error state.
    void setError(boolean error) {
        this.error.set(error);
    }

    /// Returns the error state property.
    BooleanProperty errorProperty() {
        return error;
    }

    /// Returns the preferred container height token.
    double getContainerHeight() {
        return containerHeight == null ? defaultContainerHeight : containerHeight.get();
    }

    /// Sets the preferred container height token.
    void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the preferred container height token property.
    StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = createStyleableDoubleProperty(
                    defaultContainerHeight,
                    "containerHeight",
                    containerHeightCssMetaData,
                    true
            );
        }
        return containerHeight;
    }

    /// Returns the container shape radius token.
    double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
    StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = createStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    "containerShape",
                    containerShapeCssMetaData,
                    false
            );
        }
        return containerShape;
    }

    /// Returns the horizontal content padding token.
    double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = createStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    "horizontalPadding",
                    horizontalPaddingCssMetaData,
                    true
            );
        }
        return horizontalPadding;
    }

    /// Returns the vertical content padding token.
    double getVerticalPadding() {
        return verticalPadding == null ? defaultVerticalPadding : verticalPadding.get();
    }

    /// Sets the vertical content padding token.
    void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the vertical content padding token property.
    StyleableDoubleProperty verticalPaddingProperty() {
        CssMetaData<C, Number> cssMetaData = verticalPaddingCssMetaData;
        if (cssMetaData == null) {
            throw new UnsupportedOperationException("verticalPadding is not styleable for this input");
        }
        if (verticalPadding == null) {
            verticalPadding = createStyleableDoubleProperty(
                    defaultVerticalPadding,
                    "verticalPadding",
                    cssMetaData,
                    true
            );
        }
        return verticalPadding;
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                control,
                getVariant().getStyleClass(),
                M3TextInputVariant.FILLED.getStyleClass(),
                M3TextInputVariant.OUTLINED.getStyleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double horizontalPadding = getHorizontalPadding();
        double verticalPadding = getVerticalPadding();
        M3Css.setMinHeightIfUnbound(control, height);
        M3Css.setPrefHeightIfUnbound(control, height);
        M3Css.setPaddingIfUnbound(
                control,
                new Insets(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding)
        );
    }

    /// Creates a non-negative styleable double property.
    private StyleableDoubleProperty createStyleableDoubleProperty(
            double initialValue,
            String name,
            CssMetaData<C, Number> cssMetaData,
            boolean updateMetrics
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                initialValue,
                control,
                name,
                cssMetaData,
                updateMetrics ? this::updateMetrics : control::requestLayout
        );
    }

    /// Provides a styleable double property for a text input control.
    @FunctionalInterface
    @NotNullByDefault
    interface StyleablePropertyProvider<C extends TextInputControl & M3TextInput> {
        /// Returns the styleable property for a text input control.
        StyleableDoubleProperty property(C control);
    }
}
