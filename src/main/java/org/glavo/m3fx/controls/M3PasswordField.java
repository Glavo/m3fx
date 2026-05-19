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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 password field.
///
/// This control keeps JavaFX's `PasswordField` editing and masking implementation while exposing Material state and
/// token APIs through [M3TextInput].
@NotNullByDefault
public class M3PasswordField extends PasswordField implements M3TextInput {
    /// The base style class for m3fx password fields.
    public static final String STYLE_CLASS = "m3-password-field";

    /// Shared Material text input state and token plumbing.
    private final M3TextInputSupport<M3PasswordField> support = new M3TextInputSupport<>(
            this,
            M3TextInputSupport.DEFAULT_FIELD_CONTAINER_HEIGHT,
            M3TextInputSupport.DEFAULT_FIELD_VERTICAL_PADDING,
            StyleableProperties.CONTAINER_HEIGHT,
            StyleableProperties.CONTAINER_SHAPE,
            StyleableProperties.HORIZONTAL_PADDING,
            null
    );

    /// Creates an empty filled password field.
    public M3PasswordField() {
        initialize();
    }

    /// Creates a filled password field with initial text.
    public M3PasswordField(String text) {
        setText(Objects.requireNonNull(text, "text"));
        initialize();
    }

    /// Creates a password field with initial text and the requested variant.
    public static M3PasswordField withVariant(String text, M3TextInputVariant variant) {
        M3PasswordField passwordField = new M3PasswordField(text);
        passwordField.setVariant(variant);
        return passwordField;
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
        setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
    }

    /// CSS metadata for m3fx password field component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3PasswordField, Number> CONTAINER_HEIGHT = createSizeCssMetaData(
                "-m3-container-height",
                M3TextInputSupport.DEFAULT_FIELD_CONTAINER_HEIGHT,
                M3PasswordField::containerHeightProperty
        );

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3PasswordField, Number> CONTAINER_SHAPE = createSizeCssMetaData(
                "-m3-container-shape",
                M3TextInputSupport.DEFAULT_CONTAINER_SHAPE,
                M3PasswordField::containerShapeProperty
        );

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3PasswordField, Number> HORIZONTAL_PADDING = createSizeCssMetaData(
                "-m3-horizontal-padding",
                M3TextInputSupport.DEFAULT_HORIZONTAL_PADDING,
                M3PasswordField::horizontalPaddingProperty
        );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(TextField.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents utility class instantiation.
        private StyleableProperties() {
        }

        /// Creates CSS metadata for a password field size token.
        private static CssMetaData<M3PasswordField, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyProvider provider
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3PasswordField control) {
                    return M3Css.isSettable(provider.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3PasswordField control) {
                    return provider.property(control);
                }
            };
        }
    }

    /// Provides a styleable double property for a password field.
    @FunctionalInterface
    @NotNullByDefault
    private interface StyleablePropertyProvider {
        /// Returns the styleable property for a password field.
        StyleableDoubleProperty property(M3PasswordField control);
    }
}
