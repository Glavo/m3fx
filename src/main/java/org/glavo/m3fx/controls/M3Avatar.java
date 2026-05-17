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
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 avatar for initials, icons, or small images.
@NotNullByDefault
public class M3Avatar extends StackPane {
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
    private StyleableDoubleProperty containerSize;

    /// The label used when this avatar has no graphic node.
    private final Label textLabel = new Label();

    /// Creates an empty avatar.
    public M3Avatar() {
        this("");
    }

    /// Creates an avatar with text.
    public M3Avatar(String text) {
        initialize();
        setText(text);
    }

    /// Creates an avatar with a graphic node.
    public M3Avatar(@Nullable Node graphic) {
        initialize();
        setGraphic(graphic);
    }

    /// Creates an avatar with text and the requested color variant.
    public static M3Avatar withVariant(String text, M3AvatarVariant variant) {
        M3Avatar avatar = new M3Avatar(text);
        avatar.setVariant(variant);
        return avatar;
    }

    /// Creates an avatar with a graphic node and the requested color variant.
    public static M3Avatar withVariant(@Nullable Node graphic, M3AvatarVariant variant) {
        M3Avatar avatar = new M3Avatar(graphic);
        avatar.setVariant(variant);
        return avatar;
    }

    /// Returns the avatar text.
    public final String getText() {
        return text.get();
    }

    /// Sets the avatar text.
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the avatar text property.
    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the optional graphic node.
    public final @Nullable Node getGraphic() {
        return graphic.get();
    }

    /// Sets the optional graphic node.
    public final void setGraphic(@Nullable Node graphic) {
        this.graphic.set(graphic);
    }

    /// Returns the optional graphic node property.
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

    /// Returns the avatar color variant.
    public final M3AvatarVariant getVariant() {
        return variant.get();
    }

    /// Sets the avatar color variant.
    public final void setVariant(M3AvatarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the avatar color variant property.
    public final ObjectProperty<M3AvatarVariant> variantProperty() {
        return variant;
    }

    /// Returns the avatar container size token.
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the avatar container size token.
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the avatar container size token property.
    public final StyleableDoubleProperty containerSizeProperty() {
        if (containerSize == null) {
            containerSize = new StyleableDoubleProperty(DEFAULT_CONTAINER_SIZE) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "containerSize"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Avatar.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerSize";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Avatar, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SIZE;
                }
            };
        }
        return containerSize;
    }

    /// Returns the CSS metadata for this node class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this node.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX avatars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("avatar.css");
    }

    /// Initializes style classes, child nodes, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        setAlignment(Pos.CENTER);
        textLabel.getStyleClass().add(LABEL_STYLE_CLASS);
        textLabel.textProperty().bind(text);
        graphic.addListener((observable, oldValue, newValue) -> updateContent(newValue));
        updateVariantStyle();
        updateContent(getGraphic());
        updateMetrics();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3AvatarVariant.PRIMARY.getStyleClass(),
                M3AvatarVariant.SECONDARY.getStyleClass(),
                M3AvatarVariant.TERTIARY.getStyleClass(),
                M3AvatarVariant.SURFACE.getStyleClass()
        );
    }

    /// Updates the avatar content shown in the center slot.
    private void updateContent(@Nullable Node node) {
        if (node == null) {
            getChildren().setAll(textLabel);
        } else {
            getChildren().setAll(node);
        }
    }

    /// Applies size-related component tokens to layout metrics.
    private void updateMetrics() {
        double size = getContainerSize();
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
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
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(StackPane.getClassCssMetaData());
            styleables.add(CONTAINER_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
