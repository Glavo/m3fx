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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.theme.M3ComponentColorStyles;
import org.glavo.m3fx.skins.M3ButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// Base class for Material Design 3 command buttons.
///
/// This class extends JavaFX [ButtonBase] with Material emphasis, size, and resting-shape properties. Activating a
/// concrete button by pointer, keyboard, mnemonic, or [fire][#fire()] emits an [ActionEvent]. The action is not
/// emitted while the control is disabled.
///
/// A direct [M3Icon] graphic follows the button's icon-size property; other graphic nodes retain their own sizing.
/// The default and cancel properties identify semantic roles for a containing control, such as [M3DialogPane];
/// setting either property alone does not install a scene-wide keyboard accelerator.
///
/// New buttons are filled, small, round, focus traversable, mnemonic-parsing, and neither default nor cancel.
/// Their token-backed geometry can be changed through the styleable properties or CSS. The container, content, and
/// disabled color properties provide optional, type-safe overrides for one button while absent values remain
/// connected to the active theme.
@NotNullByDefault
public abstract sealed class M3ButtonBase extends ButtonBase
        permits M3Button, M3IconButton, M3MenuButton {
    /// The base style class for all M3FX buttons.
    public static final String STYLE_CLASS = "m3-button";

    /// The pseudo-class applied to an M3FX icon used directly as a button graphic.
    private static final PseudoClass BUTTON_GRAPHIC_PSEUDO_CLASS = PseudoClass.getPseudoClass("button-graphic");

    /// The pseudo-class used when this button is the default action.
    private static final PseudoClass DEFAULT_PSEUDO_CLASS = PseudoClass.getPseudoClass("default");

    /// The pseudo-class used when this button is the cancel action.
    private static final PseudoClass CANCEL_PSEUDO_CLASS = PseudoClass.getPseudoClass("cancel");

    /// The default Material button size.
    private static final M3ButtonSize DEFAULT_SIZE = M3ButtonSize.SMALL;

    /// The default Material button shape.
    private static final M3ButtonShape DEFAULT_BUTTON_SHAPE = M3ButtonShape.ROUND;

    /// The default button container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 40.0;

    /// The default button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 999.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 24.0;

    /// The default icon glyph size.
    private static final double DEFAULT_ICON_SIZE = 20.0;

    /// Creates a filled, small, round button with empty text and no graphic.
    protected M3ButtonBase() {
        this("");
    }

    /// Creates a filled, small, round button with the specified text and no graphic.
    ///
    /// @param text the text displayed by the button
    protected M3ButtonBase(String text) {
        super(text);
        initialize();
    }

    /// Creates a filled, small, round button with the specified text and graphic.
    ///
    /// @param text    the text displayed by the button
    /// @param graphic the graphic displayed with the text, or `null` for no graphic
    protected M3ButtonBase(String text, @Nullable Node graphic) {
        super(text, graphic);
        initialize();
    }

    /// Creates a button with text and the requested variant.
    ///
    /// @param text    the text displayed by the button
    /// @param variant the Material button variant
    /// @throws NullPointerException if `variant` is `null`
    protected M3ButtonBase(String text, M3ButtonVariant variant) {
        this(text);
        setVariant(variant);
    }

    /// Creates a button with text, graphic content, and the requested variant.
    ///
    /// @param text    the text displayed by the button
    /// @param graphic the graphic displayed with the text, or `null` for no graphic
    /// @param variant the Material button variant
    /// @throws NullPointerException if `variant` is `null`
    protected M3ButtonBase(String text, @Nullable Node graphic, M3ButtonVariant variant) {
        this(text, graphic);
        setVariant(variant);
    }

    /// The visual emphasis variant of this button.
    ///
    /// The default value is [M3ButtonVariant#FILLED]. A direct `null` assignment restores the default; bound values
    /// must be non-null.
    ///
    /// @defaultValue [M3ButtonVariant#FILLED]
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

    /// Returns the button variant.
    ///
    /// @return the Material button variant
    public final M3ButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the button variant.
    ///
    /// @param variant the Material button variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3ButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the observable property that stores the button variant.
    ///
    /// The property can be observed and bound. Its default value is [M3ButtonVariant#FILLED], and a direct `null`
    /// assignment restores that default.
    ///
    /// @return the button variant property
    public final ObjectProperty<M3ButtonVariant> variantProperty() {
        return variant;
    }

    /// The Material size that selects the button's geometry and typography roles.
    ///
    /// The default value is [M3ButtonSize#SMALL]. A direct `null` assignment restores the default; bound values must
    /// be non-null.
    ///
    /// @defaultValue [M3ButtonSize#SMALL]
    private final ObjectProperty<M3ButtonSize> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE) {
        /// Updates size and typography style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(DEFAULT_SIZE);
                return;
            }
            updateSizeStyle();
            updateTypographyStyle();
            requestLayout();
        }
    };

    /// Returns the Material button size.
    ///
    /// @return the size that selects container, padding, icon, typography, outline, and shape tokens
    public final M3ButtonSize getSize() {
        return size.get();
    }

    /// Sets the Material button size.
    ///
    /// Extra-small, medium, large, and extra-large are Material Expressive sizes. Baseline themes retain the
    /// baseline small-button treatment by default while still rendering explicitly requested larger sizes.
    ///
    /// @param size the Material button size
    /// @throws NullPointerException if `size` is `null`
    public final void setSize(M3ButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the observable property that stores the Material button size.
    ///
    /// The property can be observed and bound. Its default value is [M3ButtonSize#SMALL], and a direct `null`
    /// assignment restores that default.
    ///
    /// @return the button size property
    public final ObjectProperty<M3ButtonSize> sizeProperty() {
        return size;
    }

    /// The resting container shape of this button.
    ///
    /// The default value is [M3ButtonShape#ROUND]. Interaction states may temporarily use another shape without
    /// changing this property. A direct `null` assignment restores the default; bound values must be non-null.
    ///
    /// @defaultValue [M3ButtonShape#ROUND]
    private final ObjectProperty<M3ButtonShape> buttonShape =
            new SimpleObjectProperty<>(this, "buttonShape", DEFAULT_BUTTON_SHAPE) {
                /// Updates the resting shape style class when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_BUTTON_SHAPE);
                        return;
                    }
                    updateButtonShapeStyle();
                    requestLayout();
                }
            };

    /// Returns the resting Material button shape.
    ///
    /// @return the resting round or rounded-square shape role
    public final M3ButtonShape getButtonShape() {
        return buttonShape.get();
    }

    /// Sets the resting Material button shape.
    ///
    /// @param buttonShape the resting round or rounded-square shape role
    /// @throws NullPointerException if `buttonShape` is `null`
    public final void setButtonShape(M3ButtonShape buttonShape) {
        this.buttonShape.set(Objects.requireNonNull(buttonShape, "buttonShape"));
    }

    /// Returns the observable property that stores the resting button shape.
    ///
    /// The property can be observed and bound. Its default value is [M3ButtonShape#ROUND], and a direct `null`
    /// assignment restores that default.
    ///
    /// @return the resting button shape property
    public final ObjectProperty<M3ButtonShape> buttonShapeProperty() {
        return buttonShape;
    }

    /// The explicit container color for this button.
    ///
    /// A `null` value leaves the container under the control of the button variant, active theme, and application
    /// stylesheets. A non-null value remains effective across variant and theme changes.
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable Color> containerColor;

    /// Returns the explicit container color.
    ///
    /// @return the container color, or `null` to use normal color resolution
    public final @Nullable Color getContainerColor() {
        return containerColor == null ? null : containerColor.get();
    }

    /// Sets the explicit container color.
    ///
    /// @param color the container color, or `null` to use normal color resolution
    public final void setContainerColor(@Nullable Color color) {
        if (containerColor != null || color != null) {
            containerColorProperty().set(color);
        }
    }

    /// Returns the observable property that stores the explicit container color.
    ///
    /// The property's default value is `null`.
    ///
    /// @return the nullable container-color property
    public final ObjectProperty<@Nullable Color> containerColorProperty() {
        if (containerColor == null) {
            containerColor = new SimpleObjectProperty<>(this, "containerColor") {
                /// Rebuilds the branch-local declarations when the color changes.
                @Override
                protected void invalidated() {
                    updateLocalColors();
                }
            };
        }
        return containerColor;
    }

    /// The explicit content and interaction-layer color for this button.
    ///
    /// The value colors text, icon graphics, disclosure graphics, state layers, and ripples. A `null` value leaves
    /// those roles under normal variant, theme, and CSS resolution.
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable Color> contentColor;

    /// Returns the explicit content color.
    ///
    /// @return the content color, or `null` to use normal color resolution
    public final @Nullable Color getContentColor() {
        return contentColor == null ? null : contentColor.get();
    }

    /// Sets the explicit content color.
    ///
    /// @param color the content color, or `null` to use normal color resolution
    public final void setContentColor(@Nullable Color color) {
        if (contentColor != null || color != null) {
            contentColorProperty().set(color);
        }
    }

    /// Returns the observable property that stores the explicit content color.
    ///
    /// The property's default value is `null`.
    ///
    /// @return the nullable content-color property
    public final ObjectProperty<@Nullable Color> contentColorProperty() {
        if (contentColor == null) {
            contentColor = new SimpleObjectProperty<>(this, "contentColor") {
                /// Rebuilds the branch-local declarations when the color changes.
                @Override
                protected void invalidated() {
                    updateLocalColors();
                }
            };
        }
        return contentColor;
    }

    /// The explicit disabled container color for this button.
    ///
    /// A non-null value replaces the disabled container treatment and is interpreted as the final rendered color,
    /// including its opacity. A `null` value retains normal disabled-state cascading.
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable Color> disabledContainerColor;

    /// Returns the explicit disabled container color.
    ///
    /// @return the disabled container color, or `null` to use normal color resolution
    public final @Nullable Color getDisabledContainerColor() {
        return disabledContainerColor == null ? null : disabledContainerColor.get();
    }

    /// Sets the explicit disabled container color.
    ///
    /// @param color the final disabled container color, or `null` to use normal color resolution
    public final void setDisabledContainerColor(@Nullable Color color) {
        if (disabledContainerColor != null || color != null) {
            disabledContainerColorProperty().set(color);
        }
    }

    /// Returns the observable property that stores the explicit disabled container color.
    ///
    /// The property's default value is `null`.
    ///
    /// @return the nullable disabled-container-color property
    public final ObjectProperty<@Nullable Color> disabledContainerColorProperty() {
        if (disabledContainerColor == null) {
            disabledContainerColor = new SimpleObjectProperty<>(this, "disabledContainerColor") {
                /// Rebuilds the branch-local declarations when the color changes.
                @Override
                protected void invalidated() {
                    updateLocalColors();
                }
            };
        }
        return disabledContainerColor;
    }

    /// The explicit disabled content color for this button.
    ///
    /// A non-null value replaces the disabled text and icon treatment and is interpreted as the final rendered
    /// color, including its opacity. A `null` value retains normal disabled-state cascading.
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable Color> disabledContentColor;

    /// Returns the explicit disabled content color.
    ///
    /// @return the disabled content color, or `null` to use normal color resolution
    public final @Nullable Color getDisabledContentColor() {
        return disabledContentColor == null ? null : disabledContentColor.get();
    }

    /// Sets the explicit disabled content color.
    ///
    /// @param color the final disabled content color, or `null` to use normal color resolution
    public final void setDisabledContentColor(@Nullable Color color) {
        if (disabledContentColor != null || color != null) {
            disabledContentColorProperty().set(color);
        }
    }

    /// Returns the observable property that stores the explicit disabled content color.
    ///
    /// The property's default value is `null`.
    ///
    /// @return the nullable disabled-content-color property
    public final ObjectProperty<@Nullable Color> disabledContentColorProperty() {
        if (disabledContentColor == null) {
            disabledContentColor = new SimpleObjectProperty<>(this, "disabledContentColor") {
                /// Rebuilds the branch-local declarations when the color changes.
                @Override
                protected void invalidated() {
                    updateLocalColors();
                }
            };
        }
        return disabledContentColor;
    }

    /// Rebuilds the optional component-local color declarations from the current property values.
    private void updateLocalColors() {
        M3ComponentColorStyles.applyButtonColors(
                this,
                getContainerColor(),
                getContentColor(),
                getDisabledContainerColor(),
                getDisabledContentColor()
        );
    }

    /// The preferred button container height, in logical pixels.
    ///
    /// The default value is `40.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `40.0`
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the preferred container height token.
    ///
    /// @return the preferred button container height in logical pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred button container height in logical pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the styleable property that stores the button container height.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-height`, and accepts finite,
    /// non-negative values. Its default value is `40.0` logical pixels.
    ///
    /// @return the button container height property
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

    /// The button container corner radius, in logical pixels.
    ///
    /// The default value is `999.0`, producing a fully rounded container for normal button dimensions. Values
    /// must be finite and non-negative.
    ///
    /// @defaultValue `999.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the container shape radius token.
    ///
    /// @return the button container corner radius in logical pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the button container corner radius in logical pixels
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the styleable property that stores the button corner radius.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-shape`, and accepts finite,
    /// non-negative values. Its default value is `999.0` logical pixels.
    ///
    /// @return the button corner radius property
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

    /// The horizontal padding on each side of button content, in logical pixels.
    ///
    /// The default value is `24.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in logical pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in logical pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the styleable property that stores the horizontal content padding.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-horizontal-padding`, and accepts finite,
    /// non-negative values. Its default value is `24.0` logical pixels.
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

    /// The requested width and height of a direct M3FX icon graphic, in logical pixels.
    ///
    /// The default value is `20.0`. Values must be finite and non-negative. Other graphics are not resized by this
    /// property.
    ///
    /// @defaultValue `20.0`
    private @Nullable StyleableDoubleProperty iconSize;

    /// Returns the icon glyph size token.
    ///
    /// Direct [M3Icon] graphics are resized to this value after CSS resolves the active size tokens. Other
    /// graphic nodes retain their application-controlled dimensions.
    ///
    /// @return the icon glyph size in logical pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the icon glyph size token.
    ///
    /// @param iconSize the icon glyph size in logical pixels
    /// @throws IllegalArgumentException if `iconSize` is negative or not finite
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the styleable property that stores the managed icon size.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-button-icon-size`, and accepts finite,
    /// non-negative values. Its default value is `20.0` logical pixels.
    ///
    /// @return the managed icon size property
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::updateM3IconGraphicSize
            );
        }
        return iconSize;
    }

    /// Whether this button is marked as the default action in its containing context.
    ///
    /// The default value is `false`. A containing control may use this role for keyboard activation and styling.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty defaultButton;

    /// Returns whether this button is the default action in its containing context.
    ///
    /// @return `true` when this button is the default action
    public final boolean isDefaultButton() {
        return defaultButton != null && defaultButton.get();
    }

    /// Sets whether this button is the default action in its containing context.
    ///
    /// @param defaultButton whether this button should be treated as the default action
    public final void setDefaultButton(boolean defaultButton) {
        defaultButtonProperty().set(defaultButton);
    }

    /// Returns the observable property that marks this button as a default action.
    ///
    /// The property can be observed and bound. Its default value is `false`; changes update the `default`
    /// pseudo-class.
    ///
    /// @return the default-button property
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
                    return M3ButtonBase.this;
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

    /// Whether this button is marked as the cancel action in its containing context.
    ///
    /// The default value is `false`. A containing control may use this role for keyboard activation and styling.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty cancelButton;

    /// Returns whether this button is the cancel action in its containing context.
    ///
    /// @return `true` when this button is the cancel action
    public final boolean isCancelButton() {
        return cancelButton != null && cancelButton.get();
    }

    /// Sets whether this button is the cancel action in its containing context.
    ///
    /// @param cancelButton whether this button should be treated as the cancel action
    public final void setCancelButton(boolean cancelButton) {
        cancelButtonProperty().set(cancelButton);
    }

    /// Returns the observable property that marks this button as a cancel action.
    ///
    /// The property can be observed and bound. Its default value is `false`; changes update the `cancel`
    /// pseudo-class.
    ///
    /// @return the cancel-button property
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
                    return M3ButtonBase.this;
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

    /// The direct M3FX icon whose embedded color and size are managed by this button.
    private @Nullable Node managedIconGraphic;

    /// Fires an action event unless this button is disabled.
    ///
    /// The event uses this button as both source and target. It is dispatched through the normal JavaFX event
    /// chain, so registered handlers and parent event handlers may observe it.
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

    /// Returns the user-agent stylesheet for M3FX buttons.
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
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        setPickOnBounds(true);
        graphicProperty().addListener(observable -> updateM3IconGraphicSize());
        updateVariantStyle();
        updateSizeStyle();
        updateButtonShapeStyle();
        updateTypographyStyle();
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3ButtonVariant.FILLED.styleClass(),
                M3ButtonVariant.TONAL.styleClass(),
                M3ButtonVariant.OUTLINED.styleClass(),
                M3ButtonVariant.TEXT.styleClass(),
                M3ButtonVariant.ELEVATED.styleClass()
        );
    }

    /// Applies the active Material size style class.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                sizeStyleClass(getSize()),
                sizeStyleClass(M3ButtonSize.EXTRA_SMALL),
                sizeStyleClass(M3ButtonSize.SMALL),
                sizeStyleClass(M3ButtonSize.MEDIUM),
                sizeStyleClass(M3ButtonSize.LARGE),
                sizeStyleClass(M3ButtonSize.EXTRA_LARGE)
        );
    }

    /// Applies the active resting shape style class.
    private void updateButtonShapeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                shapeStyleClass(getButtonShape()),
                shapeStyleClass(M3ButtonShape.ROUND),
                shapeStyleClass(M3ButtonShape.SQUARE)
        );
    }

    /// Applies the Material typography role selected by the active size.
    private void updateTypographyStyle() {
        M3TextRole role = switch (getSize()) {
            case EXTRA_SMALL, SMALL -> M3TextRole.LABEL_LARGE;
            case MEDIUM -> M3TextRole.TITLE_MEDIUM;
            case LARGE -> M3TextRole.HEADLINE_SMALL;
            case EXTRA_LARGE -> M3TextRole.HEADLINE_LARGE;
        };
        M3ControlStyles.replaceVariant(
                this,
                role.styleClass(),
                M3TextRole.LABEL_LARGE.styleClass(),
                M3TextRole.TITLE_MEDIUM.styleClass(),
                M3TextRole.HEADLINE_SMALL.styleClass(),
                M3TextRole.HEADLINE_LARGE.styleClass()
        );
    }

    /// Returns the control style class for one Material button size.
    ///
    /// @param size the Material button size
    /// @return the button size style class
    static String sizeStyleClass(M3ButtonSize size) {
        return "m3-button-" + size.cssSuffix();
    }

    /// Returns the control style class for one Material button shape.
    ///
    /// @param shape the Material button shape
    /// @return the button shape style class
    static String shapeStyleClass(M3ButtonShape shape) {
        return "m3-button-" + shape.cssSuffix();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double padding = getHorizontalPadding();
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setPaddingIfUnbound(this, new Insets(0.0, padding, 0.0, padding));
        updateM3IconGraphicSize();
    }

    /// Applies the resolved button icon token to a direct M3FX icon graphic.
    private void updateM3IconGraphicSize() {
        @Nullable Node graphic = getGraphic();
        @Nullable M3IconGraphic currentIcon =
                graphic instanceof M3IconGraphic icon ? icon : null;
        @Nullable Node currentIconNode = currentIcon == null ? null : graphic;
        if (managedIconGraphic != currentIconNode) {
            if (managedIconGraphic != null) {
                managedIconGraphic.pseudoClassStateChanged(BUTTON_GRAPHIC_PSEUDO_CLASS, false);
            }
            managedIconGraphic = currentIconNode;
            if (currentIconNode != null) {
                currentIconNode.pseudoClassStateChanged(BUTTON_GRAPHIC_PSEUDO_CLASS, true);
            }
        }
        if (currentIcon != null) {
            currentIcon.setIconSize(getIconSize());
        }
    }

    /// CSS metadata for M3FX button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3ButtonBase, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ButtonBase control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ButtonBase control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3ButtonBase, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ButtonBase control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ButtonBase control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3ButtonBase, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ButtonBase control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ButtonBase control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// CSS metadata for the icon glyph size token.
        private static final CssMetaData<M3ButtonBase, Number> ICON_SIZE =
                new CssMetaData<>("-m3-button-icon-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3ButtonBase control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3ButtonBase control) {
                        return control.iconSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(ICON_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
