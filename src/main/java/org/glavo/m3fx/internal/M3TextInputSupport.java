// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

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
import org.glavo.m3fx.controls.M3TextInput;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// Provides shared state and styleable geometry for one Material text input control.
///
/// The support object owns the variant, error, and geometry properties exposed by its control through [M3TextInput].
/// It does not own the control. Call [#initialize(String)] after construction to install the base style class and
/// apply the initial geometry. Instances are intended for use by one control on the JavaFX Application Thread.
///
/// @param <C> the concrete text input type that owns the shared state
@NotNullByDefault
public final class M3TextInputSupport<C extends TextInputControl & M3TextInput> {
    /// The default single-line text input container height.
    public static final double DEFAULT_FIELD_CONTAINER_HEIGHT = 56.0;

    /// The default multiline text input container height.
    public static final double DEFAULT_AREA_CONTAINER_HEIGHT = 112.0;

    /// The default text input container shape radius.
    public static final double DEFAULT_CONTAINER_SHAPE = 4.0;

    /// The default horizontal content padding.
    public static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default single-line vertical content padding.
    public static final double DEFAULT_FIELD_VERTICAL_PADDING = 8.0;

    /// The default multiline vertical content padding.
    public static final double DEFAULT_AREA_VERTICAL_PADDING = 16.0;

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

    /// Creates shared text input state for a concrete JavaFX text input control.
    ///
    /// The vertical padding metadata may be `null` for controls that do not expose vertical padding to CSS. In
    /// that case [#getVerticalPadding()] still returns the supplied default, while [#setVerticalPadding(double)] and
    /// [#verticalPaddingProperty()] throw [UnsupportedOperationException].
    ///
    /// @param control                      the control whose state and layout are managed
    /// @param defaultContainerHeight       the default container height in logical pixels
    /// @param defaultVerticalPadding       the default vertical padding in logical pixels
    /// @param containerHeightCssMetaData   CSS metadata for the container height
    /// @param containerShapeCssMetaData    CSS metadata for the container corner radius
    /// @param horizontalPaddingCssMetaData CSS metadata for the horizontal content padding
    /// @param verticalPaddingCssMetaData   CSS metadata for the vertical content padding, or `null` when unsupported
    /// @throws NullPointerException if `control` or any non-nullable metadata argument is `null`
    public M3TextInputSupport(
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

    /// The visual variant used by the owning text input.
    ///
    /// The default value is [M3TextInputVariant#FILLED]. A direct `null` assignment restores that default.
    ///
    /// @defaultValue [M3TextInputVariant#FILLED]
    private final ObjectProperty<M3TextInputVariant> variant;

    /// Returns the text input variant.
    ///
    /// @return the current visual variant
    public M3TextInputVariant getVariant() {
        return variant.get();
    }

    /// Sets the text input variant.
    ///
    /// @param variant the visual variant to use
    /// @throws NullPointerException if `variant` is `null`
    public void setVariant(M3TextInputVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the property that stores the text input variant.
    ///
    /// @return the variant property
    public ObjectProperty<M3TextInputVariant> variantProperty() {
        return variant;
    }

    /// Whether the owning input renders its error state.
    ///
    /// The default value is `false`. Changing the property updates the `:error` pseudo-class on the control.
    ///
    /// @defaultValue `false`
    private final BooleanProperty error;

    /// Returns whether this input renders its error state.
    ///
    /// @return `true` when the error state is rendered
    public boolean isError() {
        return error.get();
    }

    /// Sets whether this input renders its error state.
    ///
    /// @param error whether to render the error state
    public void setError(boolean error) {
        this.error.set(error);
    }

    /// Returns the property that controls the input's error state.
    ///
    /// @return the error property
    public BooleanProperty errorProperty() {
        return error;
    }

    /// The preferred container height, in logical pixels.
    ///
    /// The default is the `defaultContainerHeight` supplied at construction. Values must be finite and
    /// non-negative.
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the preferred container height token.
    ///
    /// @return the preferred container height in logical pixels
    public double getContainerHeight() {
        return containerHeight == null ? defaultContainerHeight : containerHeight.get();
    }

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred container height in logical pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or not finite
    public void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the styleable property that stores the preferred container height.
    ///
    /// @return the container height property, in logical pixels
    public StyleableDoubleProperty containerHeightProperty() {
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

    /// The container corner radius, in logical pixels.
    ///
    /// The default value is `4.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the container shape radius token.
    ///
    /// @return the container corner radius in logical pixels
    public double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the container corner radius in logical pixels
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the styleable property that stores the container corner radius.
    ///
    /// @return the container shape property, in logical pixels
    public StyleableDoubleProperty containerShapeProperty() {
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

    /// The horizontal content padding, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in logical pixels
    public double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in logical pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the styleable property that stores the horizontal content padding.
    ///
    /// @return the horizontal padding property, in logical pixels
    public StyleableDoubleProperty horizontalPaddingProperty() {
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

    /// The vertical content padding, in logical pixels.
    ///
    /// The default is the `defaultVerticalPadding` supplied at construction. Values must be finite and
    /// non-negative. The property exists only when vertical padding CSS metadata was supplied.
    private @Nullable StyleableDoubleProperty verticalPadding;

    /// Returns the vertical content padding token.
    ///
    /// This method also returns the configured default for controls that do not expose a vertical padding property.
    ///
    /// @return the vertical content padding in logical pixels
    public double getVerticalPadding() {
        return verticalPadding == null ? defaultVerticalPadding : verticalPadding.get();
    }

    /// Sets the vertical content padding token.
    ///
    /// @param verticalPadding the vertical content padding in logical pixels
    /// @throws IllegalArgumentException      if `verticalPadding` is negative or not finite
    /// @throws UnsupportedOperationException if this support object has no vertical padding CSS metadata
    public void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the styleable property that stores the vertical content padding.
    ///
    /// @return the vertical padding property, in logical pixels
    /// @throws UnsupportedOperationException if this support object has no vertical padding CSS metadata
    public StyleableDoubleProperty verticalPaddingProperty() {
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

    /// Creates CSS metadata for a non-negative text input size token.
    ///
    /// The returned metadata delegates property lookup to `provider` and permits CSS assignment only while the
    /// selected property is not bound.
    ///
    /// @param property     the CSS property name
    /// @param initialValue the initial value in logical pixels
    /// @param provider     the function that selects the property from a control
    /// @param <C>          the supported text input type
    /// @return CSS metadata backed by the supplied property provider
    public static <C extends TextInputControl & M3TextInput> CssMetaData<C, Number> createSizeCssMetaData(
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
    ///
    /// The returned list is an immutable snapshot. The source list is not modified.
    ///
    /// @param baseCssMetaData              the metadata inherited from the JavaFX control
    /// @param containerHeightCssMetaData   metadata for the container height
    /// @param containerShapeCssMetaData    metadata for the container corner radius
    /// @param horizontalPaddingCssMetaData metadata for horizontal content padding
    /// @param <C>                          the supported text input type
    /// @return an immutable metadata list in base, height, shape, and padding order
    /// @throws NullPointerException if `baseCssMetaData` is `null`
    public static <C extends TextInputControl & M3TextInput> List<CssMetaData<? extends Styleable, ?>> cssMetaData(
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
    ///
    /// The returned list is an immutable snapshot. The source list is not modified.
    ///
    /// @param baseCssMetaData              the metadata inherited from the JavaFX control
    /// @param containerHeightCssMetaData   metadata for the container height
    /// @param containerShapeCssMetaData    metadata for the container corner radius
    /// @param horizontalPaddingCssMetaData metadata for horizontal content padding
    /// @param verticalPaddingCssMetaData   metadata for vertical content padding
    /// @param <C>                          the supported text input type
    /// @return an immutable metadata list with vertical padding appended last
    /// @throws NullPointerException if `baseCssMetaData` is `null`
    public static <C extends TextInputControl & M3TextInput> List<CssMetaData<? extends Styleable, ?>> cssMetaData(
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

    /// Installs the base style class and applies the current variant and geometry to the owning control.
    ///
    /// This method is intended to be called once while the control is initialized.
    ///
    /// @param styleClass the base style class to install
    public void initialize(String styleClass) {
        M3ControlStyles.initialize(control, styleClass);
        updateVariantStyle();
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                control,
                variantStyleClass(getVariant()),
                variantStyleClass(M3TextInputVariant.FILLED),
                variantStyleClass(M3TextInputVariant.OUTLINED)
        );
    }

    /// Returns the style class used internally for a text input variant.
    private static String variantStyleClass(M3TextInputVariant variant) {
        return switch (variant) {
            case FILLED -> "m3-filled-field";
            case OUTLINED -> "m3-outlined-field";
        };
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
    ///
    /// @param <C> the concrete text input type accepted by the provider
    @FunctionalInterface
    @NotNullByDefault
    public interface StyleablePropertyProvider<C extends TextInputControl & M3TextInput> {
        /// Returns the styleable property for a text input control.
        ///
        /// @param control the control whose property is requested
        /// @return the control's styleable property
        StyleableDoubleProperty property(C control);
    }
}
