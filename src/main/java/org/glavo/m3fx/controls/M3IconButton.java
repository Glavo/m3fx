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
import javafx.scene.Node;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 icon button for compact icon-only actions.
///
/// An icon button presents one compact action using graphic content, normally an [M3Icon]. It inherits action
/// dispatch, keyboard activation, focus traversal, and disabled-state behavior from [M3ButtonBase]. The default
/// button has no graphic, uses the text-button color treatment, the small Material button size, the default width
/// role, and a `40.0` logical-pixel visual container. The default control occupies a `48.0` by `48.0`
/// logical-pixel interaction target, with the visual container centered inside it. Larger visual containers expand
/// the interaction target as needed.
///
/// The graphic is a JavaFX node and therefore may have only one parent. Supplying `null` leaves the button empty.
/// Applications should provide accessible text or accessible help when the graphic alone does not convey the
/// action to assistive technologies.
///
/// See [Material Design icon buttons](https://m3.material.io/components/icon-buttons/overview).
@NotNullByDefault
public final class M3IconButton extends M3ButtonBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-icon-button";

    /// The default icon button width role.
    private static final M3IconButtonWidth DEFAULT_WIDTH = M3IconButtonWidth.DEFAULT;

    /// The default icon button container width.
    private static final double DEFAULT_CONTAINER_WIDTH = 40.0;

    /// The minimum interaction-target dimension recommended by Material accessibility guidance.
    private static final double MINIMUM_INTERACTION_TARGET_SIZE = 48.0;

    /// Creates an icon button with no graphic and the default icon-button metrics.
    public M3IconButton() {
        this(null);
    }

    /// Creates an icon button with a graphic.
    ///
    /// @param graphic the graphic displayed by the icon button, or `null`
    public M3IconButton(@Nullable Node graphic) {
        super("", graphic);
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setVariant(M3ButtonVariant.TEXT);
        initializeIconMetrics();
    }

    /// The semantic width role used with the active button size.
    ///
    /// A direct assignment of `null` is replaced with the default role.
    ///
    /// @defaultValue [M3IconButtonWidth#DEFAULT]
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

    /// Returns the icon button width role.
    ///
    /// @return the icon button width role
    public final M3IconButtonWidth getWidthRole() {
        return widthRole.get();
    }

    /// Sets the icon button width role.
    ///
    /// @param widthRole the icon button width role
    /// @throws NullPointerException if `widthRole` is `null`
    public final void setWidthRole(M3IconButtonWidth widthRole) {
        this.widthRole.set(Objects.requireNonNull(widthRole, "widthRole"));
    }

    /// Returns the observable, bindable icon-button width-role property.
    ///
    /// The property defaults to [M3IconButtonWidth#DEFAULT]. A `null` value assigned directly through the property
    /// is replaced with that default.
    ///
    /// @return the icon-button width-role property
    public final ObjectProperty<M3IconButtonWidth> widthRoleProperty() {
        return widthRole;
    }

    /// The preferred visual container width in logical pixels.
    ///
    /// The value must be finite and non-negative. The unbound minimum, preferred, and maximum control widths are
    /// updated to the greater of this value and `48.0`; an application binding on those inherited size properties
    /// remains authoritative.
    ///
    /// @defaultValue `40.0`
    private @Nullable StyleableDoubleProperty containerWidth;

    /// Returns the preferred visual container width token.
    ///
    /// @return the preferred visual container width in logical pixels
    public final double getContainerWidth() {
        return containerWidth == null ? DEFAULT_CONTAINER_WIDTH : containerWidth.get();
    }

    /// Sets the preferred visual container width token.
    ///
    /// @param containerWidth the preferred visual container width in logical pixels
    /// @throws IllegalArgumentException if `containerWidth` is negative or not finite
    public final void setContainerWidth(double containerWidth) {
        containerWidthProperty().set(M3Css.nonNegative(containerWidth, "containerWidth"));
    }

    /// Returns the observable, bindable, styleable preferred container-width property.
    ///
    /// The property defaults to `40.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the preferred container-width property
    public final StyleableDoubleProperty containerWidthProperty() {
        if (containerWidth == null) {
            containerWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_WIDTH,
                    this,
                    "containerWidth",
                    StyleableProperties.CONTAINER_WIDTH,
                    this::updateIconMetrics
            );
        }
        return containerWidth;
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

    /// Keeps icon buttons sized when container size tokens change.
    private void initializeIconMetrics() {
        containerHeightProperty().addListener(observable -> updateIconMetrics());
        containerWidthProperty().addListener(observable -> updateIconMetrics());
        sizeProperty().addListener(observable -> updateIconSizeStyle());
        buttonShapeProperty().addListener(observable -> updateIconShapeStyle());
        updateIconSizeStyle();
        updateWidthStyle();
        updateIconShapeStyle();
        updateIconMetrics();
    }

    /// Applies visual container tokens while preserving the minimum interaction target.
    private void updateIconMetrics() {
        double width = Math.max(MINIMUM_INTERACTION_TARGET_SIZE, getContainerWidth());
        double height = Math.max(MINIMUM_INTERACTION_TARGET_SIZE, getContainerHeight());
        M3Css.setMinWidthIfUnbound(this, width);
        M3Css.setPrefWidthIfUnbound(this, width);
        M3Css.setMaxWidthIfUnbound(this, width);
        M3Css.setMaxHeightIfUnbound(this, height);
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

    /// Applies the icon-button-specific class for the shared Material size.
    private void updateIconSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                iconSizeStyleClass(getSize()),
                iconSizeStyleClass(M3ButtonSize.EXTRA_SMALL),
                iconSizeStyleClass(M3ButtonSize.SMALL),
                iconSizeStyleClass(M3ButtonSize.MEDIUM),
                iconSizeStyleClass(M3ButtonSize.LARGE),
                iconSizeStyleClass(M3ButtonSize.EXTRA_LARGE)
        );
    }

    /// Applies the icon-button-specific class for the shared Material shape.
    private void updateIconShapeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                iconShapeStyleClass(getButtonShape()),
                iconShapeStyleClass(M3ButtonShape.ROUND),
                iconShapeStyleClass(M3ButtonShape.SQUARE)
        );
    }

    /// Returns the icon-button style class for a shared Material size.
    ///
    /// @param size the Material button size
    /// @return the icon-button size style class
    private static String iconSizeStyleClass(M3ButtonSize size) {
        return "m3-icon-button-" + size.cssSuffix();
    }

    /// Returns the icon-button style class for a shared Material shape.
    ///
    /// @param shape the Material button shape
    /// @return the icon-button shape style class
    private static String iconShapeStyleClass(M3ButtonShape shape) {
        return "m3-icon-button-" + shape.cssSuffix();
    }

    /// CSS metadata for M3FX icon button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the visual container width token.
        private static final CssMetaData<M3IconButton, Number> CONTAINER_WIDTH =
                new CssMetaData<>("-m3-container-width", SizeConverter.getInstance(), DEFAULT_CONTAINER_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3IconButton control) {
                        return M3Css.isSettable(control.containerWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3IconButton control) {
                        return control.containerWidthProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(M3ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_WIDTH);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
