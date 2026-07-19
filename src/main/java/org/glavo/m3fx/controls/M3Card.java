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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.theme.M3ComponentColorStyles;
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
/// actionable surface when an action handler is installed. A directly actionable card must not contain buttons,
/// links, or other independently actionable descendants; use a passive card when the content owns its actions.
/// Passive cards are not focus targets and do not display hover, pressed, or ripple feedback.
///
/// The control exposes token-backed container shape, content padding, outline width, and variant state so cards can
/// participate in the same theme and density system as other M3FX controls. [#colorsProperty()] can override selected
/// colors on one card without disconnecting its remaining colors from that system. Applications that implement card
/// reordering can set the dragged property for the duration of the drag gesture to apply the Material dragged state.
///
/// See [Material Design cards](https://m3.material.io/components/cards/overview).
@NotNullByDefault
public final class M3Card extends Control {
    /// The base style class for M3FX cards.
    public static final String STYLE_CLASS = "m3-card";

    /// The default card container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 12.0;

    /// The default content padding.
    private static final double DEFAULT_CONTENT_PADDING = 16.0;

    /// The default outlined card border width.
    private static final double DEFAULT_OUTLINE_WIDTH = 1.0;

    /// The pseudo-class applied while the card has a direct action.
    private static final PseudoClass ACTIONABLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("actionable");

    /// The pseudo-class applied while the card participates in a drag operation.
    private static final PseudoClass DRAGGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("dragged");

    /// Creates an empty filled card.
    public M3Card() {
        this(null);
    }

    /// Creates a filled card with content.
    ///
    /// @param content the card content node, or `null` for no content
    public M3Card(@Nullable Node content) {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        setFocusTraversable(false);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
        setContent(content);
        updateVariantStyle();
        updateActionAccessibility();
    }

    /// Creates a card with content and a variant.
    ///
    /// @param content the card content node, or `null` for no content
    /// @param variant the Material card variant
    /// @throws NullPointerException if `variant` is `null`
    public M3Card(@Nullable Node content, M3CardVariant variant) {
        this(content);
        setVariant(variant);
    }

    /// The single content node displayed by this card.
    ///
    /// The default value is `null`. The node cannot simultaneously be a child of another parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content") {
        /// Updates accessibility semantics when content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleContentChanged();
        }
    };

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

    /// Returns the observable property that stores the optional card content.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the card content property
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// The handler for action events fired directly by this card.
    ///
    /// The default value is `null`, making the card passive. A non-null handler makes the card a focusable,
    /// directly actionable surface. Action events originating from controls inside the content are not forwarded
    /// to this handler.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates accessibility semantics when action behavior changes.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get() == null ? null : M3Card.this::handleOwnAction);
                    updateActionAccessibility();
                }
            };

    /// Returns the action handler.
    ///
    /// @return the action handler, or `null` when this card is passive
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    ///
    /// Installing a handler makes the complete card surface focusable and directly actionable. Such a card should
    /// not contain another actionable control. Set this property to `null` when buttons, links, or other actions are
    /// hosted by the card content.
    ///
    /// @param onAction the action handler invoked when the card fires, or `null` for a passive card
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the observable property that stores the card action handler.
    ///
    /// The property can be observed and bound. Its default value is `null`; a non-null value makes the complete
    /// card surface directly actionable.
    ///
    /// @return the card action handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Whether the card is represented in the Material dragged state.
    ///
    /// The default value is `false`. This state is visual only and does not initiate or manage a JavaFX
    /// drag-and-drop gesture.
    ///
    /// @defaultValue `false`
    private final BooleanProperty draggedState = new SimpleBooleanProperty(this, "dragged", false) {
        /// Updates the dragged pseudo-class after the state changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(DRAGGED_PSEUDO_CLASS, get());
        }
    };

    /// Returns whether this card is currently represented as dragged.
    ///
    /// @return `true` while the Material dragged state is active
    public final boolean isDragged() {
        return draggedState.get();
    }

    /// Sets whether this card is currently represented as dragged.
    ///
    /// This property describes visual interaction state and does not start a JavaFX drag-and-drop gesture.
    /// Applications should set it when their drag operation starts and clear it when that operation finishes or is
    /// cancelled.
    ///
    /// @param dragged whether the Material dragged state is active
    public final void setDragged(boolean dragged) {
        draggedState.set(dragged);
    }

    /// Returns the observable property that stores the Material dragged state.
    ///
    /// The property can be observed and bound. Its default value is `false`; it controls visual state only and
    /// does not initiate a JavaFX drag-and-drop gesture.
    ///
    /// @return the dragged state property
    public final BooleanProperty draggedProperty() {
        return draggedState;
    }

    /// The visual treatment of this card.
    ///
    /// The default value is [M3CardVariant#FILLED]. A direct `null` assignment restores the default; bound values
    /// must be non-null.
    ///
    /// @defaultValue [M3CardVariant#FILLED]
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

    /// Returns the card variant.
    ///
    /// @return the Material card variant
    public final M3CardVariant getVariant() {
        return variant.get();
    }

    /// Sets the card variant.
    ///
    /// @param variant the Material card variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3CardVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the observable property that stores the card variant.
    ///
    /// The property can be observed and bound. Its default value is [M3CardVariant#FILLED], and a direct `null`
    /// assignment restores that default.
    ///
    /// @return the card variant property
    public final ObjectProperty<M3CardVariant> variantProperty() {
        return variant;
    }

    /// The explicit card color overrides, or `null` to use the variant and active theme.
    ///
    /// Non-null components in the immutable value remain effective across variant and theme changes. The default
    /// value is `null`.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3CardColors> colors =
            new SimpleObjectProperty<>(this, "colors") {
                /// Rebuilds the branch-local card color declarations.
                @Override
                protected void invalidated() {
                    M3ComponentColorStyles.applyCardColors(M3Card.this, get());
                }
            };

    /// Returns the explicit card color overrides.
    ///
    /// @return the overrides, or `null` when the variant and active theme determine every card color
    public final @Nullable M3CardColors getColors() {
        return colors.get();
    }

    /// Sets explicit card color overrides.
    ///
    /// Components not replaced by the supplied value continue through the normal CSS cascade. Set the property
    /// itself to `null` to remove all managed overrides.
    ///
    /// @param colors the overrides, or `null` to restore variant and theme color resolution
    public final void setColors(@Nullable M3CardColors colors) {
        this.colors.set(colors);
    }

    /// Returns the observable property that stores explicit card color overrides.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the nullable card-colors property
    public final ObjectProperty<@Nullable M3CardColors> colorsProperty() {
        return colors;
    }

    /// The card corner radius, in logical pixels.
    ///
    /// The default value is `12.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the card container shape radius token.
    ///
    /// @return the card container corner radius in logical pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the card container shape radius token.
    ///
    /// @param containerShape the card container corner radius in logical pixels
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the styleable property that stores the card corner radius.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-shape`, and accepts finite,
    /// non-negative values. Its default value is `12.0` logical pixels.
    ///
    /// @return the card corner radius property
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

    /// The padding on each side of card content, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty contentPadding;

    /// Returns the card content padding token.
    ///
    /// @return the card content padding in logical pixels
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the card content padding token.
    ///
    /// @param contentPadding the card content padding in logical pixels
    /// @throws IllegalArgumentException if `contentPadding` is negative or not finite
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the styleable property that stores the card content padding.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-content-padding`, and accepts finite,
    /// non-negative values. Its default value is `16.0` logical pixels.
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

    /// The outline width used by outlined cards, in logical pixels.
    ///
    /// The default value is `1.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `1.0`
    private @Nullable StyleableDoubleProperty outlineWidth;

    /// Returns the outlined card border width token.
    ///
    /// @return the outlined card border width in logical pixels
    public final double getOutlineWidth() {
        return outlineWidth == null ? DEFAULT_OUTLINE_WIDTH : outlineWidth.get();
    }

    /// Sets the outlined card border width token.
    ///
    /// @param outlineWidth the outlined card border width in logical pixels
    /// @throws IllegalArgumentException if `outlineWidth` is negative or not finite
    public final void setOutlineWidth(double outlineWidth) {
        outlineWidthProperty().set(M3Css.nonNegative(outlineWidth, "outlineWidth"));
    }

    /// Returns the styleable property that stores the outlined-card border width.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-outline-width`, and accepts finite,
    /// non-negative values. Its default value is `1.0` logical pixel.
    ///
    /// @return the outlined-card border width property
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

    /// Notifies accessibility clients when focus moves between the card and nested content.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    /// Whether focus traversal was enabled automatically because the card became actionable.
    private boolean actionFocusTraversableApplied;

    /// The focus traversal value that was active before the card became actionable.
    private boolean focusTraversableBeforeAction;

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

    /// Returns the user-agent stylesheet for M3FX cards.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("card.css");
    }

    /// Fires an action event unless this card is disabled.
    ///
    /// The event is dispatched even when [onAction][#onActionProperty()] is `null`, allowing handlers registered
    /// through the JavaFX event API or on parent nodes to observe it. The event uses this card as both source and
    /// target.
    public final void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Invokes the action property only for events fired by this card rather than nested controls.
    private void handleOwnAction(ActionEvent event) {
        if (event.getTarget() != this) {
            return;
        }
        @Nullable EventHandler<ActionEvent> handler = getOnAction();
        if (handler != null) {
            handler.handle(event);
        }
    }

    /// Executes assistive-technology actions supported by this card.
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case FIRE -> fire();
            case REQUEST_FOCUS -> focusAccessibleNode();
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
        handleNavigationKeyPressed(event, event.getTarget() instanceof Node eventTarget ? eventTarget : null);
    }

    /// Handles keyboard traversal with an explicit event-target fallback.
    private void handleNavigationKeyPressed(KeyEvent event, @Nullable Node eventTarget) {
        if (event.getEventType() != KeyEvent.KEY_PRESSED) {
            return;
        }
        List<Node> targets = navigationTargets();
        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                targets,
                true,
                true,
                -1,
                false,
                eventTarget
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
        for (Node contentTarget : M3FocusTraversal.focusTargetsInReachableTree(getContent())) {
            if (contentTarget != this) {
                targets.add(contentTarget);
            }
        }
        return List.copyOf(targets);
    }

    /// Focuses the current card/content target, or an explicitly requested content target.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested target
    final boolean showAccessibleItem(Object... parameters) {
        boolean shown = parameters.length == 0
                ? M3Accessible.showItem(this, accessibleFocusNode())
                : M3Accessible.showCurrentOrItem(this, getContent(), (Node) null, parameters);
        if (shown) {
            notifyAccessibleFocusChanged();
        }
        return shown;
    }

    /// Requests focus on the current card accessibility focus node.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        if (M3Accessible.showItem(this, accessibleFocusNode())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the card focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Notifies accessibility clients that the card content item changed.
    private void notifyAccessibleContentChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleFocusChanged();
    }

    /// Applies the current variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3CardVariant.ELEVATED.styleClass(),
                M3CardVariant.FILLED.styleClass(),
                M3CardVariant.OUTLINED.styleClass()
        );
    }

    /// Updates accessibility role and traversal from the card action state.
    private void updateActionAccessibility() {
        boolean actionable = getOnAction() != null;
        pseudoClassStateChanged(ACTIONABLE_PSEUDO_CLASS, actionable);
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

    /// CSS metadata for M3FX card component tokens.
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
