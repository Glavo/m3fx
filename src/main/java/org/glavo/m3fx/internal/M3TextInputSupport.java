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
import javafx.geometry.Insets;
import javafx.scene.control.TextInputControl;
import org.glavo.m3fx.controls.M3TextInput;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
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

    /// The immutable defaults and CSS metadata shared by this concrete control class.
    private final Metadata<C> metadata;

    /// Creates shared text input state using a concrete control class's metadata.
    ///
    /// @param control the control whose state and geometry are managed
    /// @param metadata the defaults and CSS metadata for that control class
    /// @throws NullPointerException if either argument is `null`
    public M3TextInputSupport(C control, Metadata<C> metadata) {
        this.control = Objects.requireNonNull(control, "control");
        this.metadata = Objects.requireNonNull(metadata, "metadata");
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
    /// The default comes from the concrete control class metadata. Values must be finite and
    /// non-negative.
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the preferred container height token.
    ///
    /// @return the preferred container height in logical pixels
    public double getContainerHeight() {
        return containerHeight == null ? metadata.defaultContainerHeight : containerHeight.get();
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
            containerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    metadata.defaultContainerHeight,
                    control,
                    "containerHeight",
                    metadata.containerHeight,
                    control::requestLayout
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
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    control,
                    "containerShape",
                    metadata.containerShape,
                    control::requestLayout
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
            horizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    control,
                    "horizontalPadding",
                    metadata.horizontalPadding,
                    this::updatePadding
            );
        }
        return horizontalPadding;
    }

    /// The vertical content padding, in logical pixels.
    ///
    /// The default comes from the concrete control class metadata. Values must be finite and
    /// non-negative.
    private @Nullable StyleableDoubleProperty verticalPadding;

    /// Returns the vertical content padding token.
    ///
    /// @return the vertical content padding in logical pixels
    public double getVerticalPadding() {
        return verticalPadding == null ? metadata.defaultVerticalPadding : verticalPadding.get();
    }

    /// Sets the vertical content padding token.
    ///
    /// @param verticalPadding the vertical content padding in logical pixels
    /// @throws IllegalArgumentException if `verticalPadding` is negative or not finite
    public void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the styleable property that stores the vertical content padding.
    ///
    /// @return the vertical padding property, in logical pixels
    public StyleableDoubleProperty verticalPaddingProperty() {
        if (verticalPadding == null) {
            verticalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    metadata.defaultVerticalPadding,
                    control,
                    "verticalPadding",
                    metadata.verticalPadding,
                    this::updatePadding
            );
        }
        return verticalPadding;
    }

    /// Installs the base style class and applies the current variant and geometry to the owning control.
    ///
    /// This method is intended to be called once while the control is initialized.
    ///
    /// @param styleClass the base style class to install
    public void initialize(String styleClass) {
        M3ControlStyles.initialize(control, styleClass);
        updateVariantStyle();
        updatePadding();
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

    /// Applies content padding independently of application height constraints.
    private void updatePadding() {
        double horizontalPadding = getHorizontalPadding();
        double verticalPadding = getVerticalPadding();
        M3Css.setPaddingIfUnbound(
                control,
                new Insets(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding)
        );
    }

    /// Holds immutable defaults and the four geometry-token declarations for one concrete text input class.
    ///
    /// Each control class shares one instance. Instance properties remain owned by individual controls; metadata
    /// resolves them through [M3TextInput] without retaining control instances.
    ///
    /// @param <C> the concrete JavaFX text input type
    @NotNullByDefault
    public static final class Metadata<C extends TextInputControl & M3TextInput> {
        /// The default container height in logical pixels.
        private final double defaultContainerHeight;

        /// The default vertical padding in logical pixels.
        private final double defaultVerticalPadding;

        /// CSS metadata for the container-height token.
        private final CssMetaData<C, Number> containerHeight;

        /// CSS metadata for the container-shape token.
        private final CssMetaData<C, Number> containerShape;

        /// CSS metadata for the horizontal-padding token.
        private final CssMetaData<C, Number> horizontalPadding;

        /// CSS metadata for the vertical-padding token.
        private final CssMetaData<C, Number> verticalPadding;

        /// The immutable snapshot of inherited and Material metadata in declaration order.
        private final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> styleables;

        /// Creates the common Material token declarations after the inherited JavaFX metadata.
        ///
        /// @param base the inherited metadata, copied without modification
        /// @param defaultContainerHeight the finite, non-negative container height in logical pixels
        /// @param defaultVerticalPadding the finite, non-negative vertical padding in logical pixels
        /// @throws NullPointerException if `base` or any of its entries is `null`
        /// @throws IllegalArgumentException if either default is negative or not finite
        public Metadata(List<CssMetaData<? extends Styleable, ?>> base,
                        double defaultContainerHeight, double defaultVerticalPadding) {
            this.defaultContainerHeight = M3Css.nonNegative(defaultContainerHeight, "defaultContainerHeight");
            this.defaultVerticalPadding = M3Css.nonNegative(defaultVerticalPadding, "defaultVerticalPadding");
            containerHeight = M3Css.sizeMetaData("-m3-container-height", defaultContainerHeight,
                    M3TextInput::containerHeightProperty);
            containerShape = M3Css.sizeMetaData("-m3-container-shape", DEFAULT_CONTAINER_SHAPE,
                    M3TextInput::containerShapeProperty);
            horizontalPadding = M3Css.sizeMetaData("-m3-horizontal-padding", DEFAULT_HORIZONTAL_PADDING,
                    M3TextInput::horizontalPaddingProperty);
            verticalPadding = M3Css.sizeMetaData("-m3-vertical-padding", defaultVerticalPadding,
                    M3TextInput::verticalPaddingProperty);
            List<CssMetaData<? extends Styleable, ?>> entries = new ArrayList<>(base);
            entries.add(containerHeight);
            entries.add(containerShape);
            entries.add(horizontalPadding);
            entries.add(verticalPadding);
            styleables = List.copyOf(entries);
        }

        /// Returns inherited metadata followed by height, shape, horizontal padding, and vertical padding.
        ///
        /// @return the immutable metadata snapshot shared by this control class
        public @Unmodifiable List<CssMetaData<? extends Styleable, ?>> styleables() {
            return styleables;
        }

    }
}
