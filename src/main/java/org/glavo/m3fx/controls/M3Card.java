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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3CardSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 card container.
@NotNullByDefault
public class M3Card extends Control {
    /// The base style class for m3fx cards.
    public static final String STYLE_CLASS = "m3-card";

    /// The default card container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 12.0;

    /// The default content padding.
    private static final double DEFAULT_CONTENT_PADDING = 16.0;

    /// The default outlined card border width.
    private static final double DEFAULT_OUTLINE_WIDTH = 1.0;

    /// The card content node property.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// The action handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction");

    /// The card variant property.
    private final ObjectProperty<M3CardVariant> variant = new SimpleObjectProperty<>(this, "variant", M3CardVariant.FILLED) {
        /// Updates variant style classes when the property changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(M3CardVariant.FILLED);
                return;
            }
            updateVariantStyle();
        }
    };

    /// The styleable container shape token.
    private StyleableDoubleProperty containerShape;

    /// The styleable content padding token.
    private StyleableDoubleProperty contentPadding;

    /// The styleable outline width token.
    private StyleableDoubleProperty outlineWidth;

    /// Creates an empty filled card.
    public M3Card() {
        this(null);
    }

    /// Creates a filled card with content.
    public M3Card(@Nullable Node content) {
        M3ControlStyles.add(this, STYLE_CLASS);
        setContent(content);
        updateVariantStyle();
    }

    /// Creates a card with content and a variant.
    public M3Card(@Nullable Node content, M3CardVariant variant) {
        this(content);
        setVariant(variant);
    }

    /// Creates a card with content, variant, and action handler.
    public M3Card(
            @Nullable Node content,
            M3CardVariant variant,
            @Nullable EventHandler<ActionEvent> onAction
    ) {
        this(content, variant);
        setOnAction(onAction);
    }

    /// Returns the card content node.
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the card content node.
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the card content property.
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the action handler.
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action handler property.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns the card variant.
    public final M3CardVariant getVariant() {
        return variant.get();
    }

    /// Sets the card variant.
    public final void setVariant(M3CardVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the card variant property.
    public final ObjectProperty<M3CardVariant> variantProperty() {
        return variant;
    }

    /// Returns the card container shape radius token.
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the card container shape radius token.
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the card container shape radius token property.
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
                    return M3Card.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Card, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
        }
        return containerShape;
    }

    /// Returns the card content padding token.
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the card content padding token.
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the card content padding token property.
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = new StyleableDoubleProperty(DEFAULT_CONTENT_PADDING) {
                /// Validates updated padding tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "contentPadding");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Card.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "contentPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Card, Number> getCssMetaData() {
                    return StyleableProperties.CONTENT_PADDING;
                }
            };
        }
        return contentPadding;
    }

    /// Returns the outlined card border width token.
    public final double getOutlineWidth() {
        return outlineWidth == null ? DEFAULT_OUTLINE_WIDTH : outlineWidth.get();
    }

    /// Sets the outlined card border width token.
    public final void setOutlineWidth(double outlineWidth) {
        outlineWidthProperty().set(M3Css.nonNegative(outlineWidth, "outlineWidth"));
    }

    /// Returns the outlined card border width token property.
    public final StyleableDoubleProperty outlineWidthProperty() {
        if (outlineWidth == null) {
            outlineWidth = new StyleableDoubleProperty(DEFAULT_OUTLINE_WIDTH) {
                /// Validates updated outline width tokens.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "outlineWidth");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Card.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "outlineWidth";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Card, Number> getCssMetaData() {
                    return StyleableProperties.OUTLINE_WIDTH;
                }
            };
        }
        return outlineWidth;
    }

    /// Creates the default card skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3CardSkin(this);
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

    /// Returns the user-agent stylesheet for m3fx cards.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("card.css");
    }

    /// Fires this card's action event.
    public final void fire() {
        if (!isDisabled()) {
            ActionEvent event = new ActionEvent(this, this);
            @Nullable EventHandler<ActionEvent> handler = getOnAction();
            if (handler != null) {
                handler.handle(event);
            }
            if (!event.isConsumed()) {
                Event.fireEvent(this, event);
            }
        }
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3CardVariant.ELEVATED.getStyleClass(),
                M3CardVariant.FILLED.getStyleClass(),
                M3CardVariant.OUTLINED.getStyleClass()
        );
    }

    /// CSS metadata for m3fx card component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Card, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Card control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Card control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3Card, Number> CONTENT_PADDING =
                new CssMetaData<>("-m3-content-padding", SizeConverter.getInstance(), DEFAULT_CONTENT_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Card control) {
                        return M3Css.isSettable(control.contentPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Card control) {
                        return control.contentPaddingProperty();
                    }
                };

        /// CSS metadata for the outline width token.
        private static final CssMetaData<M3Card, Number> OUTLINE_WIDTH =
                new CssMetaData<>("-m3-outline-width", SizeConverter.getInstance(), DEFAULT_OUTLINE_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Card control) {
                        return M3Css.isSettable(control.outlineWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Card control) {
                        return control.outlineWidthProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_SHAPE);
            styleables.add(CONTENT_PADDING);
            styleables.add(OUTLINE_WIDTH);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
