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
/// formatting states. It supports standard, filled, tonal, and outlined variants, token-backed size and shape,
/// JavaFX accessibility toggle attributes, and Material state-layer and ripple feedback.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
@NotNullByDefault
public class M3IconToggleButton extends ButtonBase {
    /// The base style class for M3FX toggle icon buttons.
    public static final String STYLE_CLASS = "m3-icon-toggle-button";

    /// The selected pseudo-class used by toggle icon buttons.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The default toggle icon button container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 40.0;

    /// The default toggle icon button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 999.0;

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

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    /// Creates an empty standard toggle icon button.
    public M3IconToggleButton() {
        this(nullGraphic());
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
            containerHeight = new StyleableDoubleProperty(DEFAULT_CONTAINER_HEIGHT) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "containerHeight"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3IconToggleButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3IconToggleButton, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_HEIGHT;
                }
            };
        }
        return containerHeight;
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
                    set(M3Css.nonNegative(get(), "containerShape"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3IconToggleButton.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3IconToggleButton, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
        }
        return containerShape;
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

    /// Returns the default graphic value.
    private static @Nullable Node nullGraphic() {
        return null;
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);
        updateVariantStyle();
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3IconToggleButtonVariant.STANDARD.getStyleClass(),
                M3IconToggleButtonVariant.FILLED.getStyleClass(),
                M3IconToggleButtonVariant.TONAL.getStyleClass(),
                M3IconToggleButtonVariant.OUTLINED.getStyleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getContainerHeight();
        setMinSize(size, size);
        setPrefSize(size, size);
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

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
