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
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3NodeLayout;
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
/// The control follows [ButtonBase] action, focus, mnemonic, disabled, text, and graphic semantics. An icon-only
/// button uses a square container; non-blank text creates an extended FAB whose width is determined by its content
/// and logical padding. Graphic nodes are owned through the inherited [graphic][javafx.scene.control.Labeled#graphicProperty()]
/// property and must not be parented elsewhere while installed.
///
/// Variant and size choose semantic Material token families. The styleable geometry properties provide local
/// overrides in logical pixels; CSS cannot replace a bound styleable property.
///
/// Use one floating action button for the most important screen-level action. See
/// [Material Design floating action buttons](https://m3.material.io/components/floating-action-button/overview).
@NotNullByDefault
public final class M3FloatingActionButton extends ButtonBase {
    /// The base style class for M3FX floating action buttons.
    public static final String STYLE_CLASS = "m3-fab";

    /// The default floating action button container size.
    private static final double DEFAULT_CONTAINER_SIZE = 56.0;

    /// The default floating action button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 16.0;

    /// The default horizontal content padding for extended floating action buttons.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default logical trailing content padding for extended floating action buttons.
    private static final double DEFAULT_TRAILING_PADDING = 20.0;

    /// Creates an iconless, unlabeled regular primary-container floating action button.
    public M3FloatingActionButton() {
        this("");
    }

    /// Creates a regular primary-container floating action button with the specified text.
    ///
    /// @param text the text displayed by the floating action button
    public M3FloatingActionButton(String text) {
        super(text);
        initialize();
    }

    /// Creates a regular primary-container floating action button with the specified graphic.
    ///
    /// @param graphic the graphic displayed by the floating action button, or `null`
    public M3FloatingActionButton(@Nullable Node graphic) {
        super("", graphic);
        initialize();
    }

    /// Creates a regular primary-container floating action button with the specified text and graphic.
    ///
    /// @param text    the text displayed by the floating action button
    /// @param graphic the graphic displayed by the floating action button, or `null`
    public M3FloatingActionButton(String text, @Nullable Node graphic) {
        super(text, graphic);
        initialize();
    }

    /// The floating action button color variant.
    ///
    /// A direct `null` assignment restores the default.
    ///
    /// @defaultValue [M3FloatingActionButtonVariant#PRIMARY_CONTAINER]
    private final ObjectProperty<M3FloatingActionButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3FloatingActionButtonVariant.PRIMARY_CONTAINER) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3FloatingActionButtonVariant.PRIMARY_CONTAINER);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// Returns the floating action button color variant.
    ///
    /// @return the floating action button color variant
    public final M3FloatingActionButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the floating action button color variant.
    ///
    /// @param variant the floating action button color variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3FloatingActionButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the observable, bindable floating action button variant property.
    ///
    /// The property defaults to [M3FloatingActionButtonVariant#PRIMARY_CONTAINER]. A `null` value assigned directly
    /// through the property is replaced with that default.
    ///
    /// @return the floating action button variant property
    public final ObjectProperty<M3FloatingActionButtonVariant> variantProperty() {
        return variant;
    }

    /// The floating action button size variant.
    ///
    /// A direct `null` assignment restores the default.
    ///
    /// @defaultValue [M3FloatingActionButtonSize#REGULAR]
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

    /// Returns the floating action button size.
    ///
    /// @return the floating action button size
    public final M3FloatingActionButtonSize getSize() {
        return size.get();
    }

    /// Sets the floating action button size.
    ///
    /// @param size the floating action button size
    /// @throws NullPointerException if `size` is `null`
    public final void setSize(M3FloatingActionButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the observable, bindable floating action button size property.
    ///
    /// The property defaults to [M3FloatingActionButtonSize#REGULAR]. A `null` value assigned directly through the
    /// property is replaced with that default.
    ///
    /// @return the floating action button size property
    public final ObjectProperty<M3FloatingActionButtonSize> sizeProperty() {
        return size;
    }

    /// The preferred square container size in logical pixels.
    ///
    /// @defaultValue `56.0`
    private @Nullable StyleableDoubleProperty containerSize;

    /// Returns the preferred square container size in logical pixels.
    ///
    /// @return the preferred square container size token
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the preferred square container size in logical pixels.
    ///
    /// @param containerSize the preferred square container size token
    /// @throws IllegalArgumentException if `containerSize` is negative or not finite
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the observable, bindable, styleable preferred container size property.
    ///
    /// The property defaults to `56.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the preferred container size property
    public final StyleableDoubleProperty containerSizeProperty() {
        if (containerSize == null) {
            containerSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SIZE,
                    this,
                    "containerSize",
                    StyleableProperties.CONTAINER_SIZE,
                    this::updateMetrics
            );
        }
        return containerSize;
    }

    /// The container corner radius in logical pixels.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the container corner radius in logical pixels.
    ///
    /// @return the container shape radius token
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container corner radius in logical pixels.
    ///
    /// @param containerShape the container shape radius token
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the observable, bindable, styleable container corner-radius property.
    ///
    /// The property defaults to `16.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the container corner-radius property
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

    /// The logical leading content padding for an extended FAB, in logical pixels.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the logical leading content padding for an extended FAB, in logical pixels.
    ///
    /// @return the horizontal content padding token
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the logical leading content padding for an extended FAB, in logical pixels.
    ///
    /// @param horizontalPadding the horizontal content padding token
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the observable, bindable, styleable logical leading-padding property.
    ///
    /// The property defaults to `16.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the logical leading-padding property
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

    /// The logical trailing content padding for an extended FAB, in logical pixels.
    ///
    /// @defaultValue `20.0`
    private @Nullable StyleableDoubleProperty trailingPadding;

    /// Returns the logical trailing content padding token.
    ///
    /// The value is used only when the button has a non-empty label. It follows the effective node orientation,
    /// so a right-to-left button applies the value to its physical left edge.
    ///
    /// @return the logical trailing content padding in logical pixels
    public final double getTrailingPadding() {
        return trailingPadding == null ? DEFAULT_TRAILING_PADDING : trailingPadding.get();
    }

    /// Sets the logical trailing content padding token.
    ///
    /// @param trailingPadding the logical trailing content padding in logical pixels
    /// @throws IllegalArgumentException if `trailingPadding` is negative or not finite
    public final void setTrailingPadding(double trailingPadding) {
        trailingPaddingProperty().set(M3Css.nonNegative(trailingPadding, "trailingPadding"));
    }

    /// Returns the observable, bindable, styleable logical trailing-padding property.
    ///
    /// The property defaults to `20.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the logical trailing-padding property
    public final StyleableDoubleProperty trailingPaddingProperty() {
        if (trailingPadding == null) {
            trailingPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TRAILING_PADDING,
                    this,
                    "trailingPadding",
                    StyleableProperties.TRAILING_PADDING,
                    this::updateMetrics
            );
        }
        return trailingPadding;
    }

    /// Fires an [ActionEvent] from this button unless it is disabled.
    ///
    /// The event is dispatched even when no action handler is installed and may bubble to ancestors.
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

    /// Returns the user-agent stylesheet for M3FX floating action buttons.
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
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        setPickOnBounds(true);
        updateVariantStyle();
        updateSizeStyle();
        textProperty().addListener(observable -> updateMetrics());
        effectiveNodeOrientationProperty().addListener(observable -> updateMetrics());
        updateMetrics();
    }

    /// Applies the current color variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3FloatingActionButtonVariant.SURFACE.styleClass(),
                M3FloatingActionButtonVariant.PRIMARY_CONTAINER.styleClass(),
                M3FloatingActionButtonVariant.SECONDARY_CONTAINER.styleClass(),
                M3FloatingActionButtonVariant.TERTIARY_CONTAINER.styleClass(),
                M3FloatingActionButtonVariant.PRIMARY.styleClass(),
                M3FloatingActionButtonVariant.SECONDARY.styleClass(),
                M3FloatingActionButtonVariant.TERTIARY.styleClass()
        );
    }

    /// Applies the current size style class.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().styleClass(),
                M3FloatingActionButtonSize.SMALL.styleClass(),
                M3FloatingActionButtonSize.REGULAR.styleClass(),
                M3FloatingActionButtonSize.MEDIUM.styleClass(),
                M3FloatingActionButtonSize.LARGE.styleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getContainerSize();
        M3Css.setMinHeightIfUnbound(this, size);
        M3Css.setPrefHeightIfUnbound(this, size);
        @Nullable String text = getText();
        if (text != null && !text.isBlank()) {
            double padding = getHorizontalPadding();
            M3Css.setMinWidthIfUnbound(this, Region.USE_COMPUTED_SIZE);
            M3Css.setPrefWidthIfUnbound(this, Region.USE_COMPUTED_SIZE);
            M3Css.setPaddingIfUnbound(
                    this,
                    M3NodeLayout.logicalInsets(this, 0.0, padding, 0.0, getTrailingPadding())
            );
        } else {
            M3Css.setMinWidthIfUnbound(this, size);
            M3Css.setPrefWidthIfUnbound(this, size);
            M3Css.setPaddingIfUnbound(this, Insets.EMPTY);
        }
    }

    /// CSS metadata for M3FX floating action button component tokens.
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

        /// CSS metadata for the logical trailing padding token.
        private static final CssMetaData<M3FloatingActionButton, Number> TRAILING_PADDING =
                new CssMetaData<>("-m3-trailing-padding", SizeConverter.getInstance(), DEFAULT_TRAILING_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FloatingActionButton control) {
                        return M3Css.isSettable(control.trailingPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FloatingActionButton control) {
                        return control.trailingPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_SIZE);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(TRAILING_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
