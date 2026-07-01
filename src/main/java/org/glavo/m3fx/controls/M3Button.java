// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 button used to invoke a command.
///
/// `M3Button` is built on JavaFX [ButtonBase] so it participates in the standard action-event, mnemonic,
/// focus, accessibility, and default/cancel button mechanisms. The control adds Material variants through
/// [M3ButtonVariant] and exposes token-backed sizing properties for the container height, shape, and horizontal
/// padding.
///
/// The default skin renders Material state layers, ripple feedback, focus indication, and variant-specific
/// elevation. Use filled, tonal, outlined, text, or elevated variants according to the action emphasis described
/// in the [Material Design buttons](https://m3.material.io/components/buttons/overview) guidance.
@NotNullByDefault
public class M3Button extends ButtonBase {
    /// The base style class for all m3fx buttons.
    public static final String STYLE_CLASS = "m3-button";

    /// The pseudo-class used when this button is the default action.
    private static final PseudoClass DEFAULT_PSEUDO_CLASS = PseudoClass.getPseudoClass("default");

    /// The pseudo-class used when this button is the cancel action.
    private static final PseudoClass CANCEL_PSEUDO_CLASS = PseudoClass.getPseudoClass("cancel");

    /// The default button container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 40.0;

    /// The default button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 999.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 24.0;

    // The button variant property.
    private final ObjectProperty<M3ButtonVariant> variant = new SimpleObjectProperty<>(this, "variant", M3ButtonVariant.FILLED) {
        /// Updates variant style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(M3ButtonVariant.FILLED);
                return;
            }
            updateVariantStyle();
        }
    };

    // The styleable container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable horizontal padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    // Whether this button is the default action in its containing context.
    private @Nullable BooleanProperty defaultButton;

    // Whether this button is the cancel action in its containing context.
    private @Nullable BooleanProperty cancelButton;

    /// Creates an empty filled button.
    public M3Button() {
        this("");
    }

    /// Creates a filled button with text.
    ///
    /// @param text the text displayed by the button
    public M3Button(String text) {
        super(text);
        initialize();
    }

    /// Creates a filled button with text and graphic content.
    ///
    /// @param text the text displayed by the button
    /// @param graphic the optional graphic displayed with the text
    public M3Button(String text, @Nullable Node graphic) {
        super(text, graphic);
        initialize();
    }

    /// Creates a button with text and the requested variant.
    ///
    /// @param text the text displayed by the button
    /// @param variant the Material button variant
    public M3Button(String text, M3ButtonVariant variant) {
        this(text, null, variant, null);
    }

    /// Creates a button with text, graphic content, and the requested variant.
    ///
    /// @param text the text displayed by the button
    /// @param graphic the optional graphic displayed with the text
    /// @param variant the Material button variant
    public M3Button(String text, @Nullable Node graphic, M3ButtonVariant variant) {
        this(text, graphic, variant, null);
    }

    /// Creates a button with text, the requested variant, and an action handler.
    ///
    /// @param text the text displayed by the button
    /// @param variant the Material button variant
    /// @param onAction the action handler invoked when the button fires, or `null` for no handler
    public M3Button(
            String text,
            M3ButtonVariant variant,
            @Nullable EventHandler<ActionEvent> onAction
    ) {
        this(text, null, variant, onAction);
    }

    /// Creates a button with text, graphic content, the requested variant, and an action handler.
    ///
    /// @param text the text displayed by the button
    /// @param graphic the optional graphic displayed with the text
    /// @param variant the Material button variant
    /// @param onAction the action handler invoked when the button fires, or `null` for no handler
    public M3Button(
            String text,
            @Nullable Node graphic,
            M3ButtonVariant variant,
            @Nullable EventHandler<ActionEvent> onAction
    ) {
        this(text, graphic);
        setVariant(variant);
        setOnAction(onAction);
    }

    /// Returns the button variant.
    ///
    /// @return the Material button variant
    public final M3ButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the button variant.
    ///
    /// @param variant the Material button variant
    public final void setVariant(M3ButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the button variant property.
    ///
    /// @return the button variant property
    public final ObjectProperty<M3ButtonVariant> variantProperty() {
        return variant;
    }

    /// Sets whether this button is the default action in its containing context.
    ///
    /// @param defaultButton whether this button should be treated as the default action
    public final void setDefaultButton(boolean defaultButton) {
        defaultButtonProperty().set(defaultButton);
    }

    /// Returns whether this button is the default action in its containing context.
    ///
    /// @return `true` when this button is the default action
    public final boolean isDefaultButton() {
        return defaultButton != null && defaultButton.get();
    }

    /// Returns the default button state property.
    ///
    /// @return the default button state property
    public final BooleanProperty defaultButtonProperty() {
        if (defaultButton == null) {
            defaultButton = new BooleanPropertyBase(false) {
                /// Updates the default pseudo-class.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(DEFAULT_PSEUDO_CLASS, get());
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Button.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "defaultButton";
                }
            };
        }
        return defaultButton;
    }

    /// Sets whether this button is the cancel action in its containing context.
    ///
    /// @param cancelButton whether this button should be treated as the cancel action
    public final void setCancelButton(boolean cancelButton) {
        cancelButtonProperty().set(cancelButton);
    }

    /// Returns whether this button is the cancel action in its containing context.
    ///
    /// @return `true` when this button is the cancel action
    public final boolean isCancelButton() {
        return cancelButton != null && cancelButton.get();
    }

    /// Returns the cancel button state property.
    ///
    /// @return the cancel button state property
    public final BooleanProperty cancelButtonProperty() {
        if (cancelButton == null) {
            cancelButton = new BooleanPropertyBase(false) {
                /// Updates the cancel pseudo-class.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(CANCEL_PSEUDO_CLASS, get());
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Button.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "cancelButton";
                }
            };
        }
        return cancelButton;
    }

    /// Returns the preferred container height token.
    ///
    /// @return the preferred button container height in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred button container height in pixels
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the preferred container height token property.
    ///
    /// @return the preferred container height property
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    this,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT,
                    this::updateMetrics
            );
        }
        return containerHeight;
    }

    /// Returns the container shape radius token.
    ///
    /// @return the button container corner radius in pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the button container corner radius in pixels
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
    ///
    /// @return the container shape property
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    this,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE,
                    this::requestLayout
            );
        }
        return containerShape;
    }

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in pixels
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    ///
    /// @return the horizontal content padding property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    this,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING,
                    this::updateMetrics
            );
        }
        return horizontalPadding;
    }

    /// Fires this button's action handler.
    @Override
    public void fire() {
        if (!isDisabled()) {
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default animated Material Design 3 button skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ButtonSkin(this);
    }

    /// Returns the user-agent stylesheet for m3fx buttons.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("button.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        setPickOnBounds(true);
        updateVariantStyle();
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3ButtonVariant.FILLED.getStyleClass(),
                M3ButtonVariant.TONAL.getStyleClass(),
                M3ButtonVariant.OUTLINED.getStyleClass(),
                M3ButtonVariant.TEXT.getStyleClass(),
                M3ButtonVariant.ELEVATED.getStyleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double padding = getHorizontalPadding();
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setPaddingIfUnbound(this, new Insets(0.0, padding, 0.0, padding));
    }

    /// CSS metadata for m3fx button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3Button, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Button control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Button control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Button, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Button control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Button control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3Button, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Button control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Button control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
