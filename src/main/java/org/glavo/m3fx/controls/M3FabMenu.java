// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3FabMenuSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 floating action button menu.
///
/// `M3FabMenu` expands a primary [M3FloatingActionButton] into a vertical set of related action nodes. It
/// manages expanded state, keyboard dismissal, accessible child traversal, and Material expand and collapse
/// motion for the action items.
///
/// See [Material Design floating action buttons](https://m3.material.io/components/floating-action-button/overview).
@NotNullByDefault
public class M3FabMenu extends Control {
    /// The base style class for M3FX floating action button menus.
    public static final String STYLE_CLASS = "m3-fab-menu";

    /// The style class applied to the action item container.
    public static final String ACTIONS_STYLE_CLASS = "m3-fab-menu-actions";

    /// The style class applied to each action item node.
    public static final String ACTION_STYLE_CLASS = "m3-fab-menu-action";

    /// The style class applied to the menu toggle floating action button.
    public static final String TOGGLE_STYLE_CLASS = "m3-fab-menu-toggle";

    /// The default spacing between expanded action items.
    private static final double DEFAULT_ACTION_SPACING = 12.0;

    /// The offset used when action buttons enter or exit.
    private static final double ACTION_TRANSITION_OFFSET_Y = 16.0;

    /// The scale used when action buttons enter or exit.
    private static final double ACTION_TRANSITION_SCALE = 0.86;

    /// The action item container.
    private final VBox actions = new VBox();

    // The styleable spacing between expanded action items.
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// The toggle floating action button.
    private final M3FloatingActionButton toggleButton;

    // Whether the action items are currently expanded.
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded") {
        /// Applies expanded state when changed.
        @Override
        protected void invalidated() {
            setExpandedState(get());
        }
    };

    /// The currently running expand or collapse animation.
    private @Nullable Animation animation;

    /// Handles detached action item activation before the default skin attaches the item.
    private final EventHandler<ActionEvent> actionItemActionHandler = this::handleActionItemAction;

    /// Updates item styles and visibility when action items change.
    private final ListChangeListener<Node> actionsListener = change -> {
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                clearActionStyle(removed);
            }
            for (Node added : change.getAddedSubList()) {
                installAction(added);
            }
        }
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    };

    /// Creates a floating action button menu with a default toggle button.
    public M3FabMenu() {
        this(createDefaultToggleButton());
    }

    /// Creates a floating action button menu with default toggle button and action items.
    ///
    /// @param items the action items shown when the menu is expanded
    public M3FabMenu(Node... items) {
        this(createDefaultToggleButton(), items);
    }

    /// Creates a floating action button menu with a custom toggle button and action items.
    ///
    /// @param toggleButton the floating action button used to expand or collapse the menu
    /// @param items the action items shown when the menu is expanded
    /// @return a floating action button menu with the supplied toggle button
    public static M3FabMenu withToggleButton(M3FloatingActionButton toggleButton, Node... items) {
        return new M3FabMenu(toggleButton, items);
    }

    /// Creates a floating action button menu with the resolved toggle button and action items.
    private M3FabMenu(M3FloatingActionButton toggleButton, Node... items) {
        this.toggleButton = Objects.requireNonNull(toggleButton, "toggleButton");
        initialize();
        addItems(items);
    }

    /// Returns the toggle floating action button.
    ///
    /// @return the toggle floating action button
    public final M3FloatingActionButton getToggleButton() {
        return toggleButton;
    }

    /// Returns the action item container used by the default skin.
    ///
    /// @return the action item container used by the default skin
    public final VBox getActionsContainer() {
        return actions;
    }

    /// Returns the spacing between expanded action items.
    ///
    /// @return the action item spacing in pixels
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing between expanded action items.
    ///
    /// @param actionSpacing the action item spacing in pixels
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the action item spacing property.
    ///
    /// @return the styleable action item spacing property
    public final StyleableDoubleProperty actionSpacingProperty() {
        if (actionSpacing == null) {
            actionSpacing = new StyleableDoubleProperty(DEFAULT_ACTION_SPACING) {
                /// Validates updated action item spacing values.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "actionSpacing");
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3FabMenu.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "actionSpacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3FabMenu, Number> getCssMetaData() {
                    return StyleableProperties.ACTION_SPACING;
                }
            };
        }
        return actionSpacing;
    }

    /// Returns the mutable action item list.
    ///
    /// @return the mutable action item list
    public final ObservableList<Node> getItems() {
        return actions.getChildren();
    }

    /// Adds one action item.
    ///
    /// @param item the action item to add
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds action items.
    ///
    /// @param items the action items to add
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all action items.
    ///
    /// @param items the replacement action items
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all action items.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns whether action items are currently expanded.
    ///
    /// @return `true` when action items are currently expanded
    public final boolean isExpanded() {
        return expanded.get();
    }

    /// Sets whether action items are currently expanded.
    ///
    /// @param expanded whether action items are currently expanded
    public final void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    /// Returns the expanded state property.
    ///
    /// @return the expanded state property
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    /// Expands the action items.
    public final void show() {
        setExpanded(true);
    }

    /// Collapses the action items.
    public final void hide() {
        setExpanded(false);
    }

    /// Toggles the action item expanded state.
    public final void toggle() {
        setExpanded(!isExpanded());
    }

    /// Returns the user-agent stylesheet for M3FX floating action button menus.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("fab-menu.css");
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

    /// Returns accessibility attributes for the menu and action items.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isExpanded();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> accessibleFocusNode();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for expanding, collapsing, and toggling the menu.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> toggle();
            case EXPAND -> show();
            case SHOW_ITEM -> {
                show();
                M3Accessible.showItem(getItems(), parameters);
            }
            case COLLAPSE -> hide();
            case REQUEST_FOCUS -> M3Accessible.showItem(accessibleFocusNode());
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds style classes, child structure, and default action behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        M3ControlStyles.add(actions, ACTIONS_STYLE_CLASS);
        M3ControlStyles.add(toggleButton, TOGGLE_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        actions.getChildren().addListener(actionsListener);
        addEventHandler(ActionEvent.ACTION, this::handleActionItemAction);
        toggleButton.addEventHandler(ActionEvent.ACTION, event -> toggle());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        applyCollapsedState();
    }

    /// Collapses the menu after an action item fires.
    private void handleActionItemAction(ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (isExpanded() && event.getSource() instanceof Node node && getItems().contains(node)) {
            hide();
        }
    }

    /// Applies keyboard focus navigation across visible action items and the toggle button.
    private void handleNavigationKeyPressed(KeyEvent event) {
        List<Node> targets = focusTargets();
        if (targets.isEmpty()) {
            return;
        }

        int currentIndex = focusedTargetIndex(targets);
        @Nullable Node target = switch (event.getCode()) {
            case UP -> targets.get(Math.floorMod(currentIndex - 1, targets.size()));
            case DOWN -> targets.get(Math.floorMod(currentIndex + 1, targets.size()));
            case HOME -> targets.get(0);
            case END -> targets.get(targets.size() - 1);
            default -> null;
        };
        if (target == null) {
            return;
        }

        if (target.isFocusTraversable()) {
            target.requestFocus();
        }
        event.consume();
    }

    /// Returns the currently focusable action items followed by the toggle button.
    private List<Node> focusTargets() {
        ArrayList<Node> targets = new ArrayList<>();
        for (Node item : getItems()) {
            if (item.isVisible() && item.isFocusTraversable() && !item.isDisabled()) {
                targets.add(item);
            }
        }
        if (toggleButton.isVisible() && toggleButton.isFocusTraversable() && !toggleButton.isDisabled()) {
            targets.add(toggleButton);
        }
        return targets;
    }

    /// Returns the focused target index, using the toggle button as the default anchor.
    private int focusedTargetIndex(List<Node> targets) {
        for (int index = 0; index < targets.size(); index++) {
            if (targets.get(index).isFocused()) {
                return index;
            }
        }
        int toggleIndex = targets.indexOf(toggleButton);
        return Math.max(toggleIndex, 0);
    }

    /// Applies expanded state, using animation only after the control is attached to a scene.
    private void setExpandedState(boolean expanded) {
        stopAnimation();
        if (getScene() == null) {
            if (expanded) {
                applyExpandedState();
            } else {
                applyCollapsedState();
            }
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            return;
        }

        if (expanded) {
            playExpandAnimation();
        } else {
            playCollapseAnimation();
        }
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    }

    /// Returns the current focusable menu target for accessibility clients.
    private @Nullable Node accessibleFocusNode() {
        List<Node> targets = focusTargets();
        if (targets.isEmpty()) {
            return null;
        }

        int currentIndex = focusedTargetIndex(targets);
        return M3Accessible.focusTarget(targets.get(currentIndex));
    }

    /// Plays the action item expand animation.
    private void playExpandAnimation() {
        prepareActionsForExpandedLayout();
        ParallelTransition transition = new ParallelTransition();
        M3MotionSpec spec = M3Animation.defaultSpatial(this);
        int index = 0;
        int itemCount = getItems().size();
        for (Node item : getItems()) {
            item.setOpacity(0.0);
            item.setScaleX(ACTION_TRANSITION_SCALE);
            item.setScaleY(ACTION_TRANSITION_SCALE);
            item.setTranslateY(ACTION_TRANSITION_OFFSET_Y * Math.max(1, itemCount - index));
            transition.getChildren().add(new Timeline(new KeyFrame(
                    spec.duration(),
                    new KeyValue(item.opacityProperty(), 1.0, spec.interpolator()),
                    new KeyValue(item.scaleXProperty(), 1.0, spec.interpolator()),
                    new KeyValue(item.scaleYProperty(), 1.0, spec.interpolator()),
                    new KeyValue(item.translateYProperty(), 0.0, spec.interpolator())
            )));
            index++;
        }
        animation = transition;
        M3Animation.playFromStart(this, transition);
    }

    /// Plays the action item collapse animation.
    private void playCollapseAnimation() {
        ParallelTransition transition = new ParallelTransition();
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        int index = 0;
        int itemCount = getItems().size();
        for (Node item : getItems()) {
            transition.getChildren().add(new Timeline(new KeyFrame(
                    spec.duration(),
                    new KeyValue(item.opacityProperty(), 0.0, spec.interpolator()),
                    new KeyValue(item.scaleXProperty(), ACTION_TRANSITION_SCALE, spec.interpolator()),
                    new KeyValue(item.scaleYProperty(), ACTION_TRANSITION_SCALE, spec.interpolator()),
                    new KeyValue(
                            item.translateYProperty(),
                            ACTION_TRANSITION_OFFSET_Y * Math.max(1, itemCount - index),
                            spec.interpolator()
                    )
            )));
            index++;
        }
        transition.setOnFinished(event -> {
            if (!isExpanded()) {
                applyCollapsedState();
            }
        });
        animation = transition;
        M3Animation.playFromStart(this, transition);
    }

    /// Stops the current expand or collapse animation.
    private void stopAnimation() {
        Animation currentAnimation = animation;
        if (currentAnimation != null) {
            currentAnimation.stop();
            animation = null;
        }
    }

    /// Prepares action items to participate in layout while expanded.
    private void prepareActionsForExpandedLayout() {
        actions.setVisible(true);
        actions.setManaged(true);
        for (Node item : getItems()) {
            item.setVisible(true);
            item.setManaged(true);
        }
    }

    /// Applies the final expanded item state.
    private void applyExpandedState() {
        prepareActionsForExpandedLayout();
        for (Node item : getItems()) {
            item.setOpacity(1.0);
            item.setScaleX(1.0);
            item.setScaleY(1.0);
            item.setTranslateY(0.0);
        }
    }

    /// Applies the final collapsed item state.
    private void applyCollapsedState() {
        actions.setVisible(false);
        actions.setManaged(false);
        for (Node item : getItems()) {
            prepareCollapsedAction(item);
        }
    }

    /// Adds style classes and current visibility to an action item.
    private void installAction(Node item) {
        M3ControlStyles.add(item, ACTION_STYLE_CLASS);
        item.addEventHandler(ActionEvent.ACTION, actionItemActionHandler);
        if (isExpanded()) {
            item.setVisible(true);
            item.setManaged(true);
            item.setOpacity(1.0);
            item.setScaleX(1.0);
            item.setScaleY(1.0);
            item.setTranslateY(0.0);
        } else {
            prepareCollapsedAction(item);
        }
    }

    /// Applies collapsed transforms to an action item.
    private static void prepareCollapsedAction(Node item) {
        item.setVisible(false);
        item.setManaged(false);
        item.setOpacity(0.0);
        item.setScaleX(ACTION_TRANSITION_SCALE);
        item.setScaleY(ACTION_TRANSITION_SCALE);
        item.setTranslateY(ACTION_TRANSITION_OFFSET_Y);
    }

    /// Removes menu-specific style classes and transient transforms from an action item.
    private void clearActionStyle(Node item) {
        item.getStyleClass().remove(ACTION_STYLE_CLASS);
        item.removeEventHandler(ActionEvent.ACTION, actionItemActionHandler);
        item.setVisible(true);
        item.setManaged(true);
        item.setOpacity(1.0);
        item.setScaleX(1.0);
        item.setScaleY(1.0);
        item.setTranslateY(0.0);
    }

    /// Creates the default menu toggle floating action button.
    private static M3FloatingActionButton createDefaultToggleButton() {
        return M3FloatingActionButton.withGraphic(
                new M3Icon("+"),
                M3FloatingActionButtonVariant.PRIMARY,
                M3FloatingActionButtonSize.REGULAR
        );
    }

    /// Creates the default Material Design 3 floating action button menu skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FabMenuSkin(this);
    }

    /// Validates an action item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }

    /// CSS metadata for FAB menu layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for expanded action item spacing.
        private static final CssMetaData<M3FabMenu, Number> ACTION_SPACING =
                new CssMetaData<>("-m3-fab-menu-action-spacing", SizeConverter.getInstance(), DEFAULT_ACTION_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FabMenu control) {
                        return M3Css.isSettable(control.actionSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FabMenu control) {
                        return control.actionSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ACTION_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
