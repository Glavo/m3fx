// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.StyleableDoubleProperty;
import org.jetbrains.annotations.NotNullByDefault;

/// Common API shared by Material Design 3 single-line, password, and multiline text inputs.
///
/// Implementations expose Material visual state independently from the editable text API inherited from the
/// corresponding JavaFX control. The variant and geometry properties are writable and styleable. Geometry values
/// use JavaFX logical pixels and must be finite and non-negative. Setting [#errorProperty()] changes presentation
/// only; validation and error-message ownership belong to [M3TextInputLayout].
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public interface M3TextInput {
    /// Returns the text input variant.
    ///
    /// @return the text input variant
    M3TextInputVariant getVariant();

    /// Sets the text input variant.
    ///
    /// @param variant the text input variant
    /// @throws NullPointerException if `variant` is `null`
    void setVariant(M3TextInputVariant variant);

    /// Returns the text input variant property.
    ///
    /// @return the writable text input variant property
    ObjectProperty<M3TextInputVariant> variantProperty();

    /// Returns whether this text input renders its error state.
    ///
    /// @return `true` when this text input renders its error state
    boolean isError();

    /// Sets whether this text input renders its error state.
    ///
    /// @param error whether this text input renders its error state
    void setError(boolean error);

    /// Returns the error state property.
    ///
    /// @return the writable error state property
    BooleanProperty errorProperty();

    /// Returns the preferred container height token.
    ///
    /// @return the preferred container height in pixels
    double getContainerHeight();

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred container height in pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or not finite
    void setContainerHeight(double containerHeight);

    /// Returns the preferred container height token property.
    ///
    /// @return the styleable preferred container height property
    StyleableDoubleProperty containerHeightProperty();

    /// Returns the container shape radius token.
    ///
    /// @return the container corner radius in pixels
    double getContainerShape();

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the container corner radius in pixels
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    void setContainerShape(double containerShape);

    /// Returns the container shape radius token property.
    ///
    /// @return the styleable container shape radius property
    StyleableDoubleProperty containerShapeProperty();

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in pixels
    double getHorizontalPadding();

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    void setHorizontalPadding(double horizontalPadding);

    /// Returns the horizontal content padding token property.
    ///
    /// @return the styleable horizontal content padding property
    StyleableDoubleProperty horizontalPaddingProperty();

    /// Returns the vertical content padding token.
    ///
    /// @return the vertical content padding in pixels
    double getVerticalPadding();

    /// Sets the vertical content padding token.
    ///
    /// @param verticalPadding the vertical content padding in pixels
    /// @throws IllegalArgumentException if `verticalPadding` is negative or not finite
    void setVerticalPadding(double verticalPadding);

    /// Returns the vertical content padding token property.
    ///
    /// @return the styleable vertical content padding property
    StyleableDoubleProperty verticalPaddingProperty();
}
