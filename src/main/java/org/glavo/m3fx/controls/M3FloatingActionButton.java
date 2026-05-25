// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3FloatingActionButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 floating action button for a prominent primary action.
///
/// `M3FloatingActionButton` is built on JavaFX [ButtonBase] and exposes Material color variants, size variants,
/// container shape, container size, and extended-label padding. The skin renders the elevated container, icon or
/// text content, state layers, ripple feedback, focus indication, and expressive shape tokens.
///
/// Use one floating action button for the most important screen-level action. See
/// [Material Design floating action buttons](https://m3.material.io/components/floating-action-button/overview).
@NotNullByDefault
public class M3FloatingActionButton extends ButtonBase {
    /// The base style class for m3fx floating action buttons.
    public static final String STYLE_CLASS = "m3-fab";

    /// The default floating action button container size.
    private static final double DEFAULT_CONTAINER_SIZE = 56.0;

    /// The default floating action button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 16.0;

    /// The default horizontal content padding for extended floating action buttons.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    // The floating action button color variant property.
    private final ObjectProperty<M3FloatingActionButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3FloatingActionButtonVariant.PRIMARY) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3FloatingActionButtonVariant.PRIMARY);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    // The floating action button size property.
    private final ObjectProperty<M3FloatingActionButtonSize> size =
            new SimpleObjectProperty<>(this, "size", M3FloatingActionButtonSize.REGULAR) {
                /// Updates size style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3FloatingActionButtonSize.REGULAR);
                        return;
                    }
                    updateSizeStyle();
                }
            };

    // The styleable container size token.
    private @Nullable StyleableDoubleProperty containerSize;

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable horizontal padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Creates an empty primary floating action button.
    public M3FloatingActionButton() {
        this("");
    }

    /// Creates a primary floating action button with text.
    ///
    /// @param text the text displayed by the floating action button
    public M3FloatingActionButton(String text) {
        super(text);
        initialize();
    }

    /// Creates a primary floating action button with graphic content.
    ///
    /// @param graphic the graphic displayed by the floating action button, or `null`
    public M3FloatingActionButton(@Nullable Node graphic) {
        super("", graphic);
        initialize();
    }

    /// Creates a primary floating action button with text and graphic content.
    ///
    /// @param text the text displayed by the floating action button
    /// @param graphic the graphic displayed by the floating action button, or `null`
    public M3FloatingActionButton(String text, @Nullable Node graphic) {
        super(text, graphic);
        initialize();
    }

    /// Returns the floating action button color variant.
    ///
    /// @return the floating action button color variant
    public final M3FloatingActionButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the floating action button color variant.
    ///
    /// @param variant the floating action button color variant
    public final void setVariant(M3FloatingActionButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the floating action button color variant property.
    ///
    /// @return the floating action button color variant property
    public final ObjectProperty<M3FloatingActionButtonVariant> variantProperty() {
        return variant;
    }

    /// Returns the floating action button size.
    ///
    /// @return the floating action button size
    public final M3FloatingActionButtonSize getSize() {
        return size.get();
    }

    /// Sets the floating action button size.
    ///
    /// @param size the floating action button size
    public final void setSize(M3FloatingActionButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the floating action button size property.
    ///
    /// @return the floating action button size property
    public final ObjectProperty<M3FloatingActionButtonSize> sizeProperty() {
        return size;
    }

    /// Returns the preferred square container size token.
    ///
    /// @return the preferred square container size token
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the preferred square container size token.
    ///
    /// @param containerSize the preferred square container size token
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the preferred square container size token property.
    ///
    /// @return the preferred square container size token property
    public final StyleableDoubleProperty containerSizeProperty() {
        if (containerSize == null) {
            containerSize = new StyleableDoubleProperty(DEFAULT_CONTAINER_SIZE) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "containerSize");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3FloatingActionButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FloatingActionButton, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SIZE;
                }
            };
        }
        return containerSize;
    }

    /// Returns the container shape radius token.
    ///
    /// @return the container shape radius token
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the container shape radius token
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
    ///
    /// @return the container shape radius token property
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
                    return M3FloatingActionButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FloatingActionButton, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
        }
        return containerShape;
    }

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding token
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding token
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    ///
    /// @return the horizontal content padding token property
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
                    return M3FloatingActionButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "horizontalPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FloatingActionButton, Number> getCssMetaData() {
                    return StyleableProperties.HORIZONTAL_PADDING;
                }
            };
        }
        return horizontalPadding;
    }

    /// Fires this floating action button's action handler.
    @Override
    public void fire() {
        if (!isDisabled()) {
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default animated Material Design 3 floating action button skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FloatingActionButtonSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx floating action buttons.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("floating-action-button.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Adds base style classes and applies token-driven metrics.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        updateVariantStyle();
        updateSizeStyle();
        textProperty().addListener(observable -> updateMetrics());
        updateMetrics();
    }

    /// Applies the current color variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3FloatingActionButtonVariant.SURFACE.getStyleClass(),
                M3FloatingActionButtonVariant.PRIMARY.getStyleClass(),
                M3FloatingActionButtonVariant.SECONDARY.getStyleClass(),
                M3FloatingActionButtonVariant.TERTIARY.getStyleClass()
        );
    }

    /// Applies the current size style class.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().getStyleClass(),
                M3FloatingActionButtonSize.SMALL.getStyleClass(),
                M3FloatingActionButtonSize.REGULAR.getStyleClass(),
                M3FloatingActionButtonSize.LARGE.getStyleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getContainerSize();
        setMinHeight(size);
        setPrefHeight(size);
        if (hasText()) {
            double padding = getHorizontalPadding();
            setMinWidth(Region.USE_COMPUTED_SIZE);
            setPrefWidth(Region.USE_COMPUTED_SIZE);
            setPadding(new Insets(0.0, padding, 0.0, padding));
        } else {
            setMinWidth(size);
            setPrefWidth(size);
            setPadding(Insets.EMPTY);
        }
    }

    /// Returns whether the floating action button has visible text content.
    private boolean hasText() {
        @Nullable String text = getText();
        return text != null && !text.isBlank();
    }

    /// CSS metadata for m3fx floating action button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container size token.
        private static final CssMetaData<M3FloatingActionButton, Number> CONTAINER_SIZE =
                new CssMetaData<>("-m3-container-size", SizeConverter.getInstance(), DEFAULT_CONTAINER_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FloatingActionButton control) {
                        return M3Css.isSettable(control.containerSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FloatingActionButton control) {
                        return control.containerSizeProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3FloatingActionButton, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FloatingActionButton control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FloatingActionButton control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3FloatingActionButton, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FloatingActionButton control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FloatingActionButton control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_SIZE);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
