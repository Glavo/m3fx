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
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3IconToggleButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 toggle icon button.
///
/// `M3IconToggleButton` is a selectable [ButtonBase] for icon-only choices such as favorite, visibility, or
/// formatting states. It supports standard, filled, tonal, and outlined variants, token-backed size, width,
/// shape, JavaFX accessibility toggle attributes, and Material state-layer and ripple feedback.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
@NotNullByDefault
public class M3IconToggleButton extends ButtonBase {
    /// The base style class for M3FX toggle icon buttons.
    public static final String STYLE_CLASS = "m3-icon-toggle-button";

    /// The pseudo-class applied to an M3FX icon used directly as a button graphic.
    private static final PseudoClass BUTTON_GRAPHIC_PSEUDO_CLASS = PseudoClass.getPseudoClass("button-graphic");

    /// The selected pseudo-class used by toggle icon buttons.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The default toggle icon button size.
    private static final M3ButtonSize DEFAULT_SIZE = M3ButtonSize.SMALL;

    /// The default toggle icon button width role.
    private static final M3IconButtonWidth DEFAULT_WIDTH = M3IconButtonWidth.DEFAULT;

    /// The default toggle icon button shape.
    private static final M3ButtonShape DEFAULT_SHAPE = M3ButtonShape.ROUND;

    /// The default toggle icon button container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 40.0;

    /// The default toggle icon button container width.
    private static final double DEFAULT_CONTAINER_WIDTH = 40.0;

    /// The default toggle icon button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 999.0;

    /// The default toggle icon button glyph size.
    private static final double DEFAULT_ICON_SIZE = 24.0;

    // The toggle icon button variant property.
    private final ObjectProperty<M3IconToggleButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3IconToggleButtonVariant.STANDARD) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3IconToggleButtonVariant.STANDARD);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    // The toggle icon button size property.
    private final ObjectProperty<M3ButtonSize> size =
            new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE) {
                /// Updates size style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_SIZE);
                        return;
                    }
                    updateSizeStyle();
                }
            };

    // The toggle icon button width role property.
    private final ObjectProperty<M3IconButtonWidth> widthRole =
            new SimpleObjectProperty<>(this, "widthRole", DEFAULT_WIDTH) {
                /// Updates width style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_WIDTH);
                        return;
                    }
                    updateWidthStyle();
                }
            };

    // The toggle icon button shape property.
    private final ObjectProperty<M3ButtonShape> buttonShape =
            new SimpleObjectProperty<>(this, "buttonShape", DEFAULT_SHAPE) {
                /// Updates shape style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_SHAPE);
                        return;
                    }
                    updateShapeStyle();
                }
            };

    // The selected state property.
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
            notifyAccessibleAttributeChanged(AccessibleAttribute.TOGGLE_STATE);
        }
    };

    // The styleable container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    // The styleable container width token.
    private @Nullable StyleableDoubleProperty containerWidth;

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable icon glyph size token.
    private @Nullable StyleableDoubleProperty iconSize;

    /// The direct M3FX icon whose embedded color and size are managed by this button.
    private @Nullable M3Icon managedIconGraphic;

    /// Creates an empty standard toggle icon button.
    public M3IconToggleButton() {
        this((Node) null);
    }

    /// Creates a standard toggle icon button with graphic content.
    ///
    /// @param graphic the graphic displayed by the toggle icon button, or `null`
    public M3IconToggleButton(@Nullable Node graphic) {
        super("", graphic);
        initialize();
    }

    /// Creates a standard toggle icon button with an M3FX icon label.
    ///
    /// @param iconText the glyph text rendered by the icon
    public M3IconToggleButton(String iconText) {
        this(new M3Icon(iconText));
    }

    /// Creates a standard toggle icon button with an M3FX icon label, size, and color variant.
    ///
    /// @param iconText the glyph text rendered by the icon
    /// @param iconSize the icon size role
    /// @param iconVariant the icon color variant
    public M3IconToggleButton(String iconText, M3IconSize iconSize, M3IconVariant iconVariant) {
        this(new M3Icon(iconText, iconSize, iconVariant));
    }

    /// Returns the toggle icon button variant.
    ///
    /// @return the toggle icon button variant
    public final M3IconToggleButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the toggle icon button variant.
    ///
    /// @param variant the toggle icon button variant
    public final void setVariant(M3IconToggleButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the toggle icon button variant property.
    ///
    /// @return the toggle icon button variant property
    public final ObjectProperty<M3IconToggleButtonVariant> variantProperty() {
        return variant;
    }

    /// Returns the toggle icon button size.
    ///
    /// @return the toggle icon button size
    public final M3ButtonSize getSize() {
        return size.get();
    }

    /// Sets the toggle icon button size.
    ///
    /// @param size the toggle icon button size
    public final void setSize(M3ButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the toggle icon button size property.
    ///
    /// @return the toggle icon button size property
    public final ObjectProperty<M3ButtonSize> sizeProperty() {
        return size;
    }

    /// Returns the toggle icon button width role.
    ///
    /// @return the toggle icon button width role
    public final M3IconButtonWidth getWidthRole() {
        return widthRole.get();
    }

    /// Sets the toggle icon button width role.
    ///
    /// @param widthRole the toggle icon button width role
    public final void setWidthRole(M3IconButtonWidth widthRole) {
        this.widthRole.set(Objects.requireNonNull(widthRole, "widthRole"));
    }

    /// Returns the toggle icon button width role property.
    ///
    /// @return the toggle icon button width role property
    public final ObjectProperty<M3IconButtonWidth> widthRoleProperty() {
        return widthRole;
    }

    /// Returns the toggle icon button shape.
    ///
    /// @return the toggle icon button shape
    public final M3ButtonShape getButtonShape() {
        return buttonShape.get();
    }

    /// Sets the toggle icon button shape.
    ///
    /// @param shape the toggle icon button shape
    public final void setButtonShape(M3ButtonShape shape) {
        this.buttonShape.set(Objects.requireNonNull(shape, "shape"));
    }

    /// Returns the toggle icon button shape property.
    ///
    /// @return the toggle icon button shape property
    public final ObjectProperty<M3ButtonShape> buttonShapeProperty() {
        return buttonShape;
    }

    /// Returns whether this toggle icon button is selected.
    ///
    /// @return `true` when this toggle icon button is selected
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this toggle icon button is selected.
    ///
    /// @param selected whether this toggle icon button is selected
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the selected state property.
    ///
    /// @return the selected state property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// Returns the preferred container height token.
    ///
    /// @return the preferred container height token
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred container height token
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the preferred container height token property.
    ///
    /// @return the preferred container height token property
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

    /// Returns the preferred container width token.
    ///
    /// @return the preferred container width token
    public final double getContainerWidth() {
        return containerWidth == null ? DEFAULT_CONTAINER_WIDTH : containerWidth.get();
    }

    /// Sets the preferred container width token.
    ///
    /// @param containerWidth the preferred container width token
    public final void setContainerWidth(double containerWidth) {
        containerWidthProperty().set(M3Css.nonNegative(containerWidth, "containerWidth"));
    }

    /// Returns the preferred container width token property.
    ///
    /// @return the preferred container width token property
    public final StyleableDoubleProperty containerWidthProperty() {
        if (containerWidth == null) {
            containerWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_WIDTH,
                    this,
                    "containerWidth",
                    StyleableProperties.CONTAINER_WIDTH,
                    this::updateMetrics
            );
        }
        return containerWidth;
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

    /// Returns the icon glyph size token.
    ///
    /// @return the icon glyph size in pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the icon glyph size token.
    ///
    /// @param iconSize the icon glyph size in pixels
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the icon glyph size token property.
    ///
    /// @return the icon glyph size property
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::updateMetrics
            );
        }
        return iconSize;
    }

    /// Toggles and fires this icon button.
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(!isSelected());
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default animated Material Design 3 toggle icon button skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3IconToggleButtonSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX toggle icon buttons.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("icon-toggle-button.css");
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

    /// Returns accessibility attributes for the toggle selection state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case TOGGLE_STATE -> isSelected()
                    ? AccessibleAttribute.ToggleState.CHECKED
                    : AccessibleAttribute.ToggleState.UNCHECKED;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);
        setPickOnBounds(true);
        graphicProperty().addListener(observable -> updateM3IconGraphicSize());
        updateVariantStyle();
        updateSizeStyle();
        updateWidthStyle();
        updateShapeStyle();
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3IconToggleButtonVariant.STANDARD.styleClass(),
                M3IconToggleButtonVariant.FILLED.styleClass(),
                M3IconToggleButtonVariant.TONAL.styleClass(),
                M3IconToggleButtonVariant.OUTLINED.styleClass()
        );
    }

    /// Applies the current size style class.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                iconButtonSizeStyleClass(getSize()),
                iconButtonSizeStyleClass(M3ButtonSize.EXTRA_SMALL),
                iconButtonSizeStyleClass(M3ButtonSize.SMALL),
                iconButtonSizeStyleClass(M3ButtonSize.MEDIUM),
                iconButtonSizeStyleClass(M3ButtonSize.LARGE),
                iconButtonSizeStyleClass(M3ButtonSize.EXTRA_LARGE)
        );
    }

    /// Applies the current width style class.
    private void updateWidthStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getWidthRole().styleClass(),
                M3IconButtonWidth.NARROW.styleClass(),
                M3IconButtonWidth.DEFAULT.styleClass(),
                M3IconButtonWidth.WIDE.styleClass()
        );
    }

    /// Applies the current shape style class.
    private void updateShapeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                iconButtonShapeStyleClass(getButtonShape()),
                iconButtonShapeStyleClass(M3ButtonShape.ROUND),
                iconButtonShapeStyleClass(M3ButtonShape.SQUARE)
        );
    }

    /// Returns the shared icon-button size style class for a Material size.
    ///
    /// @param size the Material button size
    /// @return the icon-button size style class
    private static String iconButtonSizeStyleClass(M3ButtonSize size) {
        return "m3-icon-button-" + size.cssSuffix();
    }

    /// Returns the shared icon-button shape style class for a Material shape.
    ///
    /// @param shape the Material button shape
    /// @return the icon-button shape style class
    private static String iconButtonShapeStyleClass(M3ButtonShape shape) {
        return "m3-icon-button-" + shape.cssSuffix();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double width = getContainerWidth();
        double height = getContainerHeight();
        M3Css.setMinWidthIfUnbound(this, width);
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefWidthIfUnbound(this, width);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setMaxWidthIfUnbound(this, width);
        M3Css.setMaxHeightIfUnbound(this, height);
        updateM3IconGraphicSize();
    }

    /// Applies the resolved icon button size token to direct M3FX icon graphics.
    private void updateM3IconGraphicSize() {
        @Nullable M3Icon currentIcon = getGraphic() instanceof M3Icon icon ? icon : null;
        if (managedIconGraphic != currentIcon) {
            if (managedIconGraphic != null) {
                managedIconGraphic.pseudoClassStateChanged(BUTTON_GRAPHIC_PSEUDO_CLASS, false);
            }
            managedIconGraphic = currentIcon;
            if (currentIcon != null) {
                currentIcon.pseudoClassStateChanged(BUTTON_GRAPHIC_PSEUDO_CLASS, true);
            }
        }
        if (currentIcon != null) {
            currentIcon.setIconSize(getIconSize());
        }
    }

    /// CSS metadata for M3FX toggle icon button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3IconToggleButton, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconToggleButton control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconToggleButton control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the container width token.
        private static final CssMetaData<M3IconToggleButton, Number> CONTAINER_WIDTH =
                new CssMetaData<>("-m3-container-width", SizeConverter.getInstance(), DEFAULT_CONTAINER_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconToggleButton control) {
                        return M3Css.isSettable(control.containerWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconToggleButton control) {
                        return control.containerWidthProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3IconToggleButton, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconToggleButton control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconToggleButton control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the icon glyph size token.
        private static final CssMetaData<M3IconToggleButton, Number> ICON_SIZE =
                new CssMetaData<>("-m3-icon-button-icon-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconToggleButton control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconToggleButton control) {
                        return control.iconSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_WIDTH);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(ICON_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
