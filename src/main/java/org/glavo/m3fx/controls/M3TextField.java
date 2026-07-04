// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TextField;
import org.glavo.m3fx.internal.M3TextInputSupport;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

/// A Material Design 3 single-line text field.
///
/// This control keeps JavaFX's [TextField] editing implementation for caret movement, selection, clipboard,
/// undo, redo, and IME behavior while exposing Material state and token APIs through [M3TextInput].
/// Use it directly when a plain single-line field is enough, or place it inside [M3TextInputLayout] when the
/// field needs a floating label, supporting text, character counter, clear button, or adornment slots.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public class M3TextField extends TextField implements M3TextInput {
    /// The base style class for M3FX text fields.
    public static final String STYLE_CLASS = "m3-text-field";

    /// Shared Material text input state and token plumbing.
    private final M3TextInputSupport<M3TextField> support = new M3TextInputSupport<>(
            this,
            M3TextInputSupport.DEFAULT_FIELD_CONTAINER_HEIGHT,
            M3TextInputSupport.DEFAULT_FIELD_VERTICAL_PADDING,
            StyleableProperties.CONTAINER_HEIGHT,
            StyleableProperties.CONTAINER_SHAPE,
            StyleableProperties.HORIZONTAL_PADDING,
            StyleableProperties.VERTICAL_PADDING
    );

    /// Creates an empty filled text field.
    public M3TextField() {
        initialize();
    }

    /// Creates a filled text field with initial text.
    public M3TextField(String text) {
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

    /// Returns whether this field renders its error state.
    @Override
    public final boolean isError() {
        return support.isError();
    }

    /// Sets whether this field renders its error state.
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
    @Override
    public final double getVerticalPadding() {
        return support.getVerticalPadding();
    }

    /// Sets the vertical content padding token.
    @Override
    public final void setVerticalPadding(double verticalPadding) {
        support.setVerticalPadding(verticalPadding);
    }

    /// Returns the vertical content padding token property.
    @Override
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

    /// Returns the user-agent stylesheet for M3FX text input controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("text-field.css");
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        support.initialize(STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT_FIELD);
    }

    /// CSS metadata for M3FX text field component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3TextField, Number> CONTAINER_HEIGHT =
                M3TextInputSupport.createSizeCssMetaData(
                "-m3-container-height",
                M3TextInputSupport.DEFAULT_FIELD_CONTAINER_HEIGHT,
                M3TextField::containerHeightProperty
        );

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3TextField, Number> CONTAINER_SHAPE =
                M3TextInputSupport.createSizeCssMetaData(
                "-m3-container-shape",
                M3TextInputSupport.DEFAULT_CONTAINER_SHAPE,
                M3TextField::containerShapeProperty
        );

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3TextField, Number> HORIZONTAL_PADDING =
                M3TextInputSupport.createSizeCssMetaData(
                "-m3-horizontal-padding",
                M3TextInputSupport.DEFAULT_HORIZONTAL_PADDING,
                M3TextField::horizontalPaddingProperty
        );

        /// CSS metadata for the vertical padding token.
        private static final CssMetaData<M3TextField, Number> VERTICAL_PADDING =
                M3TextInputSupport.createSizeCssMetaData(
                "-m3-vertical-padding",
                M3TextInputSupport.DEFAULT_FIELD_VERTICAL_PADDING,
                M3TextField::verticalPaddingProperty
        );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = M3TextInputSupport.cssMetaData(
                TextField.getClassCssMetaData(),
                CONTAINER_HEIGHT,
                CONTAINER_SHAPE,
                HORIZONTAL_PADDING,
                VERTICAL_PADDING
        );

        /// Prevents utility class instantiation.
        private StyleableProperties() {
        }

    }
}
