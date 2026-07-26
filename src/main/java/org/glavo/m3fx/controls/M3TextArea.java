// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import org.glavo.m3fx.internal.M3TextInputSupport;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// A Material Design 3 multi-line text area.
///
/// This control keeps JavaFX's [TextArea] editing, wrapping, scrolling, selection, clipboard, and IME behavior
/// while exposing Material state and token APIs through [M3TextInput]. Use it directly for a Material-styled
/// multi-line editor, or place it inside [M3TextInputLayout] to add a label, supporting text, error text,
/// character counter, and adornment slots.
///
/// A new text area uses the filled variant, is not in error state, wraps text, and applies M3FX styling to its scroll
/// bars. Text, selection, editing, preferred row count, and scroll position retain the inherited [TextArea]
/// contracts.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public final class M3TextArea extends TextArea implements M3TextInput {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-text-area";

    /// Styles the native text-area viewport after JavaFX installs or replaces its skin.
    private final InvalidationListener skinInvalidation = observable -> styleInternalScrollPane();

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

    /// Creates an empty, wrapping, filled text area that is not in error state.
    public M3TextArea() {
        initialize();
    }

    /// Creates a wrapping, filled text area with initial text and no error state.
    ///
    /// As with [TextArea], a `null` value is represented as empty content by the inherited text property.
    ///
    /// @param text the initial text content
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

    /// Returns the observable, bindable text input variant property.
    ///
    /// The default value is [M3TextInputVariant#FILLED].
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

    /// Returns the observable, bindable error-state property.
    ///
    /// The default value is `false`.
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

    /// Returns the observable, bindable, and styleable preferred-container-height property.
    ///
    /// The default value is `112.0` logical pixels. Values must be finite and non-negative.
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

    /// Returns the observable, bindable, and styleable container-shape-radius property.
    ///
    /// The default value is `4.0` logical pixels. Values must be finite and non-negative.
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

    /// Returns the observable, bindable, and styleable horizontal-content-padding property.
    ///
    /// The default value is `16.0` logical pixels. Values must be finite and non-negative.
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

    /// Returns the observable, bindable, and styleable vertical-content-padding property.
    ///
    /// The default value is `16.0` logical pixels. Values must be finite and non-negative.
    @Override
    public final StyleableDoubleProperty verticalPaddingProperty() {
        return support.verticalPaddingProperty();
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class and its superclasses
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
        support.initialize(DEFAULT_STYLE_CLASS);
        skinProperty().addListener(skinInvalidation);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setWrapText(true);
    }

    /// Installs Material scrollbar styling on the ScrollPane owned by the current JavaFX text-area skin.
    private void styleInternalScrollPane() {
        @Nullable Node node = lookup(".scroll-pane");
        if (node instanceof ScrollPane scrollPane) {
            M3ScrollPanes.style(scrollPane);
        }
    }

    /// CSS metadata for M3FX text area component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3TextArea, Number> CONTAINER_HEIGHT =
                M3TextInputSupport.createSizeCssMetaData(
                        "-m3-container-height",
                        M3TextInputSupport.DEFAULT_AREA_CONTAINER_HEIGHT,
                        M3TextArea::containerHeightProperty
                );

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3TextArea, Number> CONTAINER_SHAPE =
                M3TextInputSupport.createSizeCssMetaData(
                        "-m3-container-shape",
                        M3TextInputSupport.DEFAULT_CONTAINER_SHAPE,
                        M3TextArea::containerShapeProperty
                );

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3TextArea, Number> HORIZONTAL_PADDING =
                M3TextInputSupport.createSizeCssMetaData(
                        "-m3-horizontal-padding",
                        M3TextInputSupport.DEFAULT_HORIZONTAL_PADDING,
                        M3TextArea::horizontalPaddingProperty
                );

        /// CSS metadata for the vertical padding token.
        private static final CssMetaData<M3TextArea, Number> VERTICAL_PADDING =
                M3TextInputSupport.createSizeCssMetaData(
                        "-m3-vertical-padding",
                        M3TextInputSupport.DEFAULT_AREA_VERTICAL_PADDING,
                        M3TextArea::verticalPaddingProperty
                );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = M3TextInputSupport.cssMetaData(
                TextArea.getClassCssMetaData(),
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
