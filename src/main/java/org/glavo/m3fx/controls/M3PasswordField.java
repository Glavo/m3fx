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
import javafx.scene.AccessibleRole;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 password field.
@NotNullByDefault
public class M3PasswordField extends PasswordField implements M3TextInput {
    /// The base style class for m3fx password fields.
    public static final String STYLE_CLASS = "m3-password-field";

    /// The pseudo-class used while the field is in an error state.
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

    /// The default field container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 56.0;

    /// The default field container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 4.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The visual variant property.
    private final ObjectProperty<M3TextInputVariant> variant = new SimpleObjectProperty<>(this, "variant", M3TextInputVariant.FILLED) {
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

    /// Whether this field should render its error state.
    private final BooleanProperty error = new SimpleBooleanProperty(this, "error") {
        /// Updates the error pseudo-class when the property changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(ERROR_PSEUDO_CLASS, get());
        }
    };

    /// The styleable container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    /// The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    /// The styleable horizontal padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

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
    public final M3TextInputVariant getVariant() {
        return variant.get();
    }

    /// Sets the text input variant.
    public final void setVariant(M3TextInputVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the text input variant property.
    public final ObjectProperty<M3TextInputVariant> variantProperty() {
        return variant;
    }

    /// Returns whether this field renders its error state.
    public final boolean isError() {
        return error.get();
    }

    /// Sets whether this field renders its error state.
    public final void setError(boolean error) {
        this.error.set(error);
    }

    /// Returns the error state property.
    public final BooleanProperty errorProperty() {
        return error;
    }

    /// Returns the preferred container height token.
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the preferred container height token property.
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = new StyleableDoubleProperty(DEFAULT_CONTAINER_HEIGHT) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "containerHeight");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3PasswordField.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3PasswordField, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_HEIGHT;
                }
            };
        }
        return containerHeight;
    }

    /// Returns the container shape radius token.
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
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
                    return M3PasswordField.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3PasswordField, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
        }
        return containerShape;
    }

    /// Returns the horizontal content padding token.
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = new StyleableDoubleProperty(DEFAULT_HORIZONTAL_PADDING) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "horizontalPadding");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3PasswordField.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "horizontalPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3PasswordField, Number> getCssMetaData() {
                    return StyleableProperties.HORIZONTAL_PADDING;
                }
            };
        }
        return horizontalPadding;
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
        updateVariantStyle();
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3TextInputVariant.FILLED.getStyleClass(),
                M3TextInputVariant.OUTLINED.getStyleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double padding = getHorizontalPadding();
        setMinHeight(height);
        setPrefHeight(height);
        setPadding(new Insets(8.0, padding, 8.0, padding));
    }

    /// CSS metadata for m3fx password field component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3PasswordField, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3PasswordField control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3PasswordField control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3PasswordField, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3PasswordField control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3PasswordField control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3PasswordField, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3PasswordField control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3PasswordField control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(TextField.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
