// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TextArea;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 multi-line text area.
///
/// This control keeps JavaFX's [TextArea] editing, wrapping, scrolling, selection, clipboard, and IME behavior
/// while exposing Material state and token APIs through [M3TextInput]. Use it directly for a Material-styled
/// multi-line editor, or place it inside [M3TextInputLayout] to add a label, supporting text, error text,
/// character counter, and adornment slots.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public class M3TextArea extends TextArea implements M3TextInput {
    /// The base style class for m3fx text areas.
    public static final String STYLE_CLASS = "m3-text-area";

    /// Shared Material text input state and token plumbing.
    private final M3TextInputSupport<M3TextArea> support = new M3TextInputSupport<>(
            this,
            M3TextInputSupport.DEFAULT_AREA_CONTAINER_HEIGHT,
            M3TextInputSupport.DEFAULT_AREA_VERTICAL_PADDING,
            StyleableProperties.CONTAINER_HEIGHT,
            StyleableProperties.CONTAINER_SHAPE,
            StyleableProperties.HORIZONTAL_PADDING,
            StyleableProperties.VERTICAL_PADDING
    );

    /// Creates an empty filled text area.
    public M3TextArea() {
        initialize();
    }

    /// Creates a filled text area with initial text.
    public M3TextArea(String text) {
        super(text);
        initialize();
    }

    /// Returns the text input variant.
    @Override
    public final M3TextInputVariant getVariant() {
        return support.getVariant();
    }

    /// Sets the text input variant.
    @Override
    public final void setVariant(M3TextInputVariant variant) {
        support.setVariant(variant);
    }

    /// Returns the text input variant property.
    @Override
    public final ObjectProperty<M3TextInputVariant> variantProperty() {
        return support.variantProperty();
    }

    /// Returns whether this text area renders its error state.
    @Override
    public final boolean isError() {
        return support.isError();
    }

    /// Sets whether this text area renders its error state.
    @Override
    public final void setError(boolean error) {
        support.setError(error);
    }

    /// Returns the error state property.
    @Override
    public final BooleanProperty errorProperty() {
        return support.errorProperty();
    }

    /// Returns the preferred container height token.
    @Override
    public final double getContainerHeight() {
        return support.getContainerHeight();
    }

    /// Sets the preferred container height token.
    @Override
    public final void setContainerHeight(double containerHeight) {
        support.setContainerHeight(containerHeight);
    }

    /// Returns the preferred container height token property.
    @Override
    public final StyleableDoubleProperty containerHeightProperty() {
        return support.containerHeightProperty();
    }

    /// Returns the container shape radius token.
    @Override
    public final double getContainerShape() {
        return support.getContainerShape();
    }

    /// Sets the container shape radius token.
    @Override
    public final void setContainerShape(double containerShape) {
        support.setContainerShape(containerShape);
    }

    /// Returns the container shape radius token property.
    @Override
    public final StyleableDoubleProperty containerShapeProperty() {
        return support.containerShapeProperty();
    }

    /// Returns the horizontal content padding token.
    @Override
    public final double getHorizontalPadding() {
        return support.getHorizontalPadding();
    }

    /// Sets the horizontal content padding token.
    @Override
    public final void setHorizontalPadding(double horizontalPadding) {
        support.setHorizontalPadding(horizontalPadding);
    }

    /// Returns the horizontal content padding token property.
    @Override
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        return support.horizontalPaddingProperty();
    }

    /// Returns the vertical content padding token.
    public final double getVerticalPadding() {
        return support.getVerticalPadding();
    }

    /// Sets the vertical content padding token.
    public final void setVerticalPadding(double verticalPadding) {
        support.setVerticalPadding(verticalPadding);
    }

    /// Returns the vertical content padding token property.
    public final StyleableDoubleProperty verticalPaddingProperty() {
        return support.verticalPaddingProperty();
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

    /// Returns the user-agent stylesheet for m3fx text input controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("text-field.css");
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        support.initialize(STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setWrapText(true);
    }

    /// CSS metadata for m3fx text area component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3TextArea, Number> CONTAINER_HEIGHT = createSizeCssMetaData(
                "-m3-container-height",
                M3TextInputSupport.DEFAULT_AREA_CONTAINER_HEIGHT,
                M3TextArea::containerHeightProperty
        );

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3TextArea, Number> CONTAINER_SHAPE = createSizeCssMetaData(
                "-m3-container-shape",
                M3TextInputSupport.DEFAULT_CONTAINER_SHAPE,
                M3TextArea::containerShapeProperty
        );

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3TextArea, Number> HORIZONTAL_PADDING = createSizeCssMetaData(
                "-m3-horizontal-padding",
                M3TextInputSupport.DEFAULT_HORIZONTAL_PADDING,
                M3TextArea::horizontalPaddingProperty
        );

        /// CSS metadata for the vertical padding token.
        private static final CssMetaData<M3TextArea, Number> VERTICAL_PADDING = createSizeCssMetaData(
                "-m3-vertical-padding",
                M3TextInputSupport.DEFAULT_AREA_VERTICAL_PADDING,
                M3TextArea::verticalPaddingProperty
        );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(TextArea.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(VERTICAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents utility class instantiation.
        private StyleableProperties() {
        }

        /// Creates CSS metadata for a text area size token.
        private static CssMetaData<M3TextArea, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyProvider provider
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3TextArea control) {
                    return M3Css.isSettable(provider.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3TextArea control) {
                    return provider.property(control);
                }
            };
        }
    }

    /// Provides a styleable double property for a text area.
    @FunctionalInterface
    @NotNullByDefault
    private interface StyleablePropertyProvider {
        /// Returns the styleable property for a text area.
        StyleableDoubleProperty property(M3TextArea control);
    }
}
