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
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3CardSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 card container.
///
/// `M3Card` groups related content in a filled, elevated, or outlined container. It can be passive content or an
/// actionable surface when an action handler is installed. The control exposes token-backed container shape,
/// content padding, outline width, and variant state so cards can participate in the same theme and density
/// system as other M3FX controls.
///
/// See [Material Design cards](https://m3.material.io/components/cards/overview).
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

    // The card content node property.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content") {
        /// Updates accessibility semantics when content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleContentChanged();
        }
    };

    /// Notifies accessibility clients when focus moves between the card and nested content.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    // The action handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates accessibility semantics when action behavior changes.
                @Override
                protected void invalidated() {
                    updateActionAccessibility();
                }
            };

    // The card variant property.
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

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable content padding token.
    private @Nullable StyleableDoubleProperty contentPadding;

    // The styleable outline width token.
    private @Nullable StyleableDoubleProperty outlineWidth;

    /// Whether focus traversal was enabled automatically because the card became actionable.
    private boolean actionFocusTraversableApplied;

    /// The focus traversal value that was active before the card became actionable.
    private boolean focusTraversableBeforeAction;

    /// Creates an empty filled card.
    public M3Card() {
        this(null);
    }

    /// Creates a filled card with content.
    ///
    /// @param content the card content node, or `null` for no content
    public M3Card(@Nullable Node content) {
        M3ControlStyles.add(this, STYLE_CLASS);
        setFocusTraversable(false);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
        setContent(content);
        updateVariantStyle();
        updateActionAccessibility();
    }

    /// Creates a card with content and a variant.
    ///
    /// @param content the card content node, or `null` for no content
    /// @param variant the Material card variant
    public M3Card(@Nullable Node content, M3CardVariant variant) {
        this(content);
        setVariant(variant);
    }

    /// Creates a card with content, variant, and action handler.
    ///
    /// @param content the card content node, or `null` for no content
    /// @param variant the Material card variant
    /// @param onAction the action handler invoked when the card fires, or `null` for a passive card
    public M3Card(
            @Nullable Node content,
            M3CardVariant variant,
            @Nullable EventHandler<ActionEvent> onAction
    ) {
        this(content, variant);
        setOnAction(onAction);
    }

    /// Returns the card content node.
    ///
    /// @return the card content node, or `null` when no content is set
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the card content node.
    ///
    /// @param content the card content node, or `null` to clear it
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the card content property.
    ///
    /// @return the card content property
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the action handler.
    ///
    /// @return the action handler, or `null` when this card is passive
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    ///
    /// @param onAction the action handler invoked when the card fires, or `null` for a passive card
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action handler property.
    ///
    /// @return the action handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns the card variant.
    ///
    /// @return the Material card variant
    public final M3CardVariant getVariant() {
        return variant.get();
    }

    /// Sets the card variant.
    ///
    /// @param variant the Material card variant
    public final void setVariant(M3CardVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the card variant property.
    ///
    /// @return the card variant property
    public final ObjectProperty<M3CardVariant> variantProperty() {
        return variant;
    }

    /// Returns the card container shape radius token.
    ///
    /// @return the card container corner radius in pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the card container shape radius token.
    ///
    /// @param containerShape the card container corner radius in pixels
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the card container shape radius token property.
    ///
    /// @return the card container shape property
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

    /// Returns the card content padding token.
    ///
    /// @return the card content padding in pixels
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the card content padding token.
    ///
    /// @param contentPadding the card content padding in pixels
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the card content padding token property.
    ///
    /// @return the card content padding property
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_PADDING,
                    this,
                    "contentPadding",
                    StyleableProperties.CONTENT_PADDING,
                    this::requestLayout
            );
        }
        return contentPadding;
    }

    /// Returns the outlined card border width token.
    ///
    /// @return the outlined card border width in pixels
    public final double getOutlineWidth() {
        return outlineWidth == null ? DEFAULT_OUTLINE_WIDTH : outlineWidth.get();
    }

    /// Sets the outlined card border width token.
    ///
    /// @param outlineWidth the outlined card border width in pixels
    public final void setOutlineWidth(double outlineWidth) {
        outlineWidthProperty().set(M3Css.nonNegative(outlineWidth, "outlineWidth"));
    }

    /// Returns the outlined card border width token property.
    ///
    /// @return the outlined card border width property
    public final StyleableDoubleProperty outlineWidthProperty() {
        if (outlineWidth == null) {
            outlineWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_OUTLINE_WIDTH,
                    this,
                    "outlineWidth",
                    StyleableProperties.OUTLINE_WIDTH,
                    this::requestLayout
            );
        }
        return outlineWidth;
    }

    /// Creates the default card skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3CardSkin(this);
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

    /// Executes assistive-technology actions supported by this card.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> fire();
            case REQUEST_FOCUS -> M3Accessible.showItem(accessibleFocusNode());
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns accessibility attributes for the card content.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case CONTENTS -> getContent();
            case ITEM_COUNT -> M3Accessible.itemCount(getContent(), (Node) null, (Node) null);
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getContent(), (Node) null, (Node) null, parameters);
            case FOCUS_NODE -> accessibleFocusNode();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Returns the current card or nested content accessibility focus node.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node currentTarget = M3Accessible.currentFocusTarget(this, getContent(), (Node) null);
        if (currentTarget != null) {
            return currentTarget;
        }
        if (getOnAction() != null) {
            @Nullable Node cardTarget = M3Accessible.focusTarget(this);
            if (cardTarget != null) {
                return cardTarget;
            }
        }
        return M3Accessible.firstFocusTarget(getContent(), (Node) null);
    }

    /// Handles keyboard traversal between the actionable card surface and nested content.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3FocusTraversal.focusOwnerInsideTextInput(this)) {
            return;
        }

        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                navigationTargets(),
                true,
                true,
                -1,
                false
        );
    }

    /// Returns reachable keyboard navigation targets in logical card order.
    private @Unmodifiable List<Node> navigationTargets() {
        List<Node> targets = new ArrayList<>();
        if (getOnAction() != null) {
            @Nullable Node cardTarget = M3Accessible.focusTarget(this);
            if (cardTarget != null) {
                targets.add(cardTarget);
            }
        }
        @Nullable Node contentTarget = M3Accessible.accessibleFocusTarget(getContent());
        if (contentTarget != null && contentTarget != this) {
            targets.add(contentTarget);
        }
        return List.copyOf(targets);
    }

    /// Focuses the current card/content target, or an explicitly requested content target.
    private void showAccessibleItem(Object... parameters) {
        if (parameters.length == 0) {
            M3Accessible.showItem(accessibleFocusNode());
        } else {
            M3Accessible.showCurrentOrItem(this, getContent(), (Node) null, parameters);
        }
    }

    /// Notifies accessibility clients that the card content item changed.
    private void notifyAccessibleContentChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
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

    /// Updates accessibility role and traversal from the card action state.
    private void updateActionAccessibility() {
        boolean actionable = getOnAction() != null;
        setAccessibleRole(actionable ? AccessibleRole.BUTTON : AccessibleRole.PARENT);
        if (actionable) {
            if (!actionFocusTraversableApplied) {
                focusTraversableBeforeAction = isFocusTraversable();
                setFocusTraversable(true);
                actionFocusTraversableApplied = true;
            }
        } else if (actionFocusTraversableApplied) {
            setFocusTraversable(focusTraversableBeforeAction);
            actionFocusTraversableApplied = false;
        }
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
