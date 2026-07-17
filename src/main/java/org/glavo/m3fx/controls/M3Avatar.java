// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3AvatarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 avatar for initials, icons, or small images.
///
/// `M3Avatar` displays compact identity or entity imagery using text, a graphic node, or generated initials.
/// The control exposes color variants and token-backed container size so it can be used in lists, app bars,
/// cards, and menus without hard-coded dimensions.
///
/// See [Material Design](https://m3.material.io/) for the visual system that defines the color, shape, and
/// typography roles used by avatars.
@NotNullByDefault
public final class M3Avatar extends Control {
    /// The base style class for M3FX avatars.
    public static final String STYLE_CLASS = "m3-avatar";

    /// The default text label style class.
    public static final String LABEL_STYLE_CLASS = "m3-avatar-label";

    /// The default avatar container size.
    private static final double DEFAULT_CONTAINER_SIZE = 40.0;

    /// The avatar text property.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /// The optional graphic node property.
    private final ObjectProperty<@Nullable Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    /// The avatar color variant property.
    private final ObjectProperty<M3AvatarVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3AvatarVariant.PRIMARY) {
                /// Updates avatar variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3AvatarVariant.PRIMARY);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// The styleable avatar container size token.
    private @Nullable StyleableDoubleProperty containerSize;

    /// Creates an empty avatar.
    public M3Avatar() {
        this("");
    }

    /// Creates an avatar with text.
    ///
    /// @param text the text displayed when no graphic is set
    public M3Avatar(String text) {
        initialize();
        setText(text);
    }

    /// Creates an avatar with a graphic node.
    ///
    /// @param graphic the graphic node displayed by the avatar, or `null` for no graphic
    public M3Avatar(@Nullable Node graphic) {
        initialize();
        setGraphic(graphic);
    }

    /// Returns the avatar text.
    ///
    /// @return the text displayed when no graphic is set
    public final String getText() {
        return text.get();
    }

    /// Sets the avatar text.
    ///
    /// @param text the text displayed when no graphic is set
    /// @throws NullPointerException if any required argument is `null`
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the optional graphic node.
    ///
    /// @return the graphic node displayed by the avatar, or `null` when the avatar uses text
    public final @Nullable Node getGraphic() {
        return graphic.get();
    }

    /// Sets the optional graphic node.
    ///
    /// @param graphic the graphic node displayed by the avatar, or `null` to display text
    public final void setGraphic(@Nullable Node graphic) {
        this.graphic.set(graphic);
    }

    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

    /// Returns the avatar color variant.
    ///
    /// @return the avatar color variant
    public final M3AvatarVariant getVariant() {
        return variant.get();
    }

    /// Sets the avatar color variant.
    ///
    /// @param variant the avatar color variant
    /// @throws NullPointerException if any required argument is `null`
    public final void setVariant(M3AvatarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    public final ObjectProperty<M3AvatarVariant> variantProperty() {
        return variant;
    }

    /// Returns the avatar container size token.
    ///
    /// @return the avatar container size in pixels
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the avatar container size token.
    ///
    /// @param containerSize the avatar container size in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    public final StyleableDoubleProperty containerSizeProperty() {
        if (containerSize == null) {
            containerSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SIZE,
                    this,
                    "containerSize",
                    StyleableProperties.CONTAINER_SIZE,
                    this::requestLayout
            );
        }
        return containerSize;
    }

    /// Returns the CSS metadata for this node class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this node.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX avatars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("avatar.css");
    }

    /// Initializes style classes, child nodes, and property listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        setFocusTraversable(false);
        text.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        graphic.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        updateVariantStyle();
        updateAccessibleText();
        requestLayout();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3AvatarVariant.PRIMARY.styleClass(),
                M3AvatarVariant.SECONDARY.styleClass(),
                M3AvatarVariant.TERTIARY.styleClass(),
                M3AvatarVariant.SURFACE.styleClass()
        );
    }

    /// Updates the text exposed to assistive technologies.
    private void updateAccessibleText() {
        @Nullable Node graphicNode = getGraphic();
        if (graphicNode != null) {
            @Nullable String graphicText = graphicNode.getAccessibleText();
            if (graphicText != null && !graphicText.isBlank()) {
                setAccessibleText(graphicText);
                return;
            }
        }
        setAccessibleText(getText());
    }

    /// Computes the minimum avatar width from the container size token.
    @Override
    protected double computeMinWidth(double height) {
        return getContainerSize();
    }

    /// Computes the minimum avatar height from the container size token.
    @Override
    protected double computeMinHeight(double width) {
        return getContainerSize();
    }

    /// Computes the preferred avatar width from the container size token.
    @Override
    protected double computePrefWidth(double height) {
        return getContainerSize();
    }

    /// Computes the preferred avatar height from the container size token.
    @Override
    protected double computePrefHeight(double width) {
        return getContainerSize();
    }

    /// Computes the maximum avatar width from the container size token.
    @Override
    protected double computeMaxWidth(double height) {
        return getContainerSize();
    }

    /// Computes the maximum avatar height from the container size token.
    @Override
    protected double computeMaxHeight(double width) {
        return getContainerSize();
    }

    /// Creates the default Material Design 3 avatar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3AvatarSkin(this);
    }

    /// CSS metadata for M3FX avatar component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the avatar container size token.
        private static final CssMetaData<M3Avatar, Number> CONTAINER_SIZE =
                new CssMetaData<>("-m3-container-size", SizeConverter.getInstance(), DEFAULT_CONTAINER_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Avatar control) {
                        return M3Css.isSettable(control.containerSizeProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Avatar control) {
                        return control.containerSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
