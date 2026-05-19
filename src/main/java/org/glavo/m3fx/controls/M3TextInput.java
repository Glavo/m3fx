// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.StyleableDoubleProperty;
import org.jetbrains.annotations.NotNullByDefault;

/// Common API shared by Material Design 3 single-line, password, and multiline text inputs.
@NotNullByDefault
public interface M3TextInput {
    /// Returns the text input variant.
    M3TextInputVariant getVariant();

    /// Sets the text input variant.
    void setVariant(M3TextInputVariant variant);

    /// Returns the text input variant property.
    ObjectProperty<M3TextInputVariant> variantProperty();

    /// Returns whether this text input renders its error state.
    boolean isError();

    /// Sets whether this text input renders its error state.
    void setError(boolean error);

    /// Returns the error state property.
    BooleanProperty errorProperty();

    /// Returns the preferred container height token.
    double getContainerHeight();

    /// Sets the preferred container height token.
    void setContainerHeight(double containerHeight);

    /// Returns the preferred container height token property.
    StyleableDoubleProperty containerHeightProperty();

    /// Returns the container shape radius token.
    double getContainerShape();

    /// Sets the container shape radius token.
    void setContainerShape(double containerShape);

    /// Returns the container shape radius token property.
    StyleableDoubleProperty containerShapeProperty();

    /// Returns the horizontal content padding token.
    double getHorizontalPadding();

    /// Sets the horizontal content padding token.
    void setHorizontalPadding(double horizontalPadding);

    /// Returns the horizontal content padding token property.
    StyleableDoubleProperty horizontalPaddingProperty();
}
