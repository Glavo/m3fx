// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3FabMenuSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 floating action button menu.
///
/// The menu displays a toggle [M3FloatingActionButton] while collapsed and a close button plus an ordered column of
/// action nodes while expanded. Activating the toggle expands the menu; activating the close button or a direct
/// action item collapses it. Escape also collapses an expanded menu. Up and Down traverse the visible actions and
/// activator.
///
/// A custom toggle button and all item nodes become children of this control and must not be kept in another parent.
/// Expansion transfers focus from the toggle to the close button; collapsing while focus is in the menu restores
/// focus to the toggle when reachable. Motion follows the effective M3FX motion settings.
///
/// ```java
/// private M3FabMenu createFabMenu() {
///     M3FabMenu menu = new M3FabMenu(new M3FloatingActionButton("Create"));
///     M3FloatingActionButton document = new M3FloatingActionButton("Document");
///     M3FloatingActionButton folder = new M3FloatingActionButton("Folder");
///     document.setOnAction(event -> System.out.println("Document"));
///     folder.setOnAction(event -> System.out.println("Folder"));
///     menu.getItems().addAll(document, folder);
///     return menu;
/// }
/// ```
///
/// See [Material Design FAB menus](https://m3.material.io/components/fab-menu/overview).
@NotNullByDefault
public final class M3FabMenu extends Control {
    /// The base style class for M3FX floating action button menus.
    public static final String STYLE_CLASS = "m3-fab-menu";

    /// The style class applied to the action item container.
    public static final String ACTIONS_STYLE_CLASS = "m3-fab-menu-actions";

    /// The style class applied to each action item node.
    public static final String ACTION_STYLE_CLASS = "m3-fab-menu-action";

    /// The style class applied to the menu toggle floating action button.
    public static final String TOGGLE_STYLE_CLASS = "m3-fab-menu-toggle";
    /// The style class applied to the menu close floating action button.
    public static final String CLOSE_STYLE_CLASS = "m3-fab-menu-close";

    /// The expanded pseudo-class.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");


    /// The default spacing between expanded action items.
    private static final double DEFAULT_ACTION_SPACING = 4.0;

    /// The default spacing between the last action and close button.
    private static final double DEFAULT_CLOSE_SPACING = 8.0;

    /// The offset used when action buttons enter or exit.
    private static final double ACTION_TRANSITION_OFFSET_Y = 16.0;

    /// The scale used when action buttons enter or exit.
    private static final double ACTION_TRANSITION_SCALE = 0.86;

    /// Creates a collapsed menu with a default regular primary-container toggle button and no action items.
    public M3FabMenu() {
        this(createDefaultToggleButton());
    }

    /// Creates a collapsed menu using the specified toggle button.
    ///
    /// The button becomes owned by this control. Its existing action handlers remain installed; the menu also
    /// installs an action handler that expands the menu.
    ///
    /// @param toggleButton the floating action button used to expand or collapse the menu
    /// @throws NullPointerException if `toggleButton` is `null`
    public M3FabMenu(M3FloatingActionButton toggleButton) {
        this.toggleButton = Objects.requireNonNull(toggleButton, "toggleButton");
        initialize();
    }

    /// The spacing between expanded action items in logical pixels.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// Returns the spacing between expanded action items.
    ///
    /// @return the action item spacing in logical pixels
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing between expanded action items.
    ///
    /// @param actionSpacing the action item spacing in logical pixels
    /// @throws IllegalArgumentException if `actionSpacing` is negative or not finite
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the observable, bindable, styleable action-item spacing property.
    ///
    /// The property defaults to `4.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the action-item spacing property
    public final StyleableDoubleProperty actionSpacingProperty() {
        if (actionSpacing == null) {
            actionSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTION_SPACING,
                    this,
                    "actionSpacing",
                    StyleableProperties.ACTION_SPACING,
                    this::requestLayout
            );
        }
        return actionSpacing;
    }

    /// The spacing between the last action and close button in logical pixels.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty closeSpacing;

    /// Returns the spacing between the last expanded action and close button.
    ///
    /// @return the close button spacing in logical pixels
    public final double getCloseSpacing() {
        return closeSpacing == null ? DEFAULT_CLOSE_SPACING : closeSpacing.get();
    }

    /// Sets the spacing between the last expanded action and close button.
    ///
    /// @param closeSpacing the close button spacing in logical pixels
    /// @throws IllegalArgumentException if `closeSpacing` is negative or not finite
    public final void setCloseSpacing(double closeSpacing) {
        closeSpacingProperty().set(M3Css.nonNegative(closeSpacing, "closeSpacing"));
    }

    /// Returns the observable, bindable, styleable close-button spacing property.
    ///
    /// The property defaults to `8.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the close-button spacing property
    public final StyleableDoubleProperty closeSpacingProperty() {
        if (closeSpacing == null) {
            closeSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CLOSE_SPACING,
                    this,
                    "closeSpacing",
                    StyleableProperties.CLOSE_SPACING,
                    this::requestLayout
            );
        }
        return closeSpacing;
    }

    /// Whether the action items are expanded.
    ///
    /// Changing this property updates visibility and focus immediately and animates the visual transition when the
    /// control is attached to a scene and motion is enabled.
    ///
    /// @defaultValue `false`
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded") {
        /// Applies expanded state when changed.
        @Override
        protected void invalidated() {
            setExpandedState(get());
        }
    };

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

    /// Returns the observable, bindable expanded-state property.
    ///
    /// The property defaults to `false`. Changing it updates child visibility, focus, accessibility state, and the
    /// animated visual transition.
    ///
    /// @return the expanded-state property
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    /// The action item container.
    private final VBox actions = new VBox();

    /// The toggle floating action button owned by this menu.
    private final M3FloatingActionButton toggleButton;

    /// The close floating action button shown while expanded.
    private final M3FloatingActionButton closeButton = createCloseButton();

    /// The reusable expand and collapse animation for every action item.
    private final ActionItemsTransition animation = new ActionItemsTransition();

    /// Collapses the menu when an action item is activated.
    private final EventHandler<ActionEvent> actionItemActionHandler = this::handleActionItemAction;

    /// Handles keyboard navigation from the menu root and action items.
    private final EventHandler<KeyEvent> navigationKeyHandler = this::handleNavigationKeyPressed;

    /// Updates item styles and visibility when action items change.
    private final ListChangeListener<Node> actionsListener = change -> {
        boolean interrupted = animation.getStatus() == Animation.Status.RUNNING;
        stopAnimation();
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                clearActionStyle(removed);
            }
            for (Node added : change.getAddedSubList()) {
                installAction(added);
            }
        }
        if (interrupted) {
            if (isExpanded()) {
                applyExpandedState();
            } else {
                applyCollapsedState();
            }
        }
        notifyAccessibleItemsChanged();
    };

    /// Notifies accessibility clients when focus moves between visible FAB menu targets.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    /// Returns the toggle floating action button.
    ///
    /// @return the toggle floating action button
    final M3FloatingActionButton getToggleButton() {
        return toggleButton;
    }

    /// Returns the internal close floating action button.
    ///
    /// @return the close floating action button
    final M3FloatingActionButton getCloseButton() {
        return closeButton;
    }

    /// Returns the live, mutable list of direct action item nodes in display order.
    ///
    /// The list initially is empty and enforces JavaFX parent-child constraints, including non-null elements and no
    /// duplicate node instances. Items are parented by this control while present. An action event whose source is a
    /// direct item collapses the menu without consuming that event.
    ///
    /// @return the live, mutable action item list
    public final ObservableList<Node> getItems() {
        return actions.getChildren();
    }

    /// Expands the action items.
    ///
    /// Calling this method while expanded has no effect.
    public final void show() {
        setExpanded(true);
    }

    /// Collapses the action items.
    ///
    /// Calling this method while collapsed has no effect.
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
    ///
    /// @throws NullPointerException if `attribute` is `null`
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
            case FIRE -> {
                if (M3Accessible.canReach(this)) {
                    toggle();
                }
            }
            case EXPAND -> {
                if (M3Accessible.canReach(this)) {
                    show();
                }
            }
            case SHOW_ITEM -> showAccessibleItem(parameters);
            case COLLAPSE -> hide();
            case REQUEST_FOCUS -> focusAccessibleNode();
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds style classes, child structure, and default action behavior.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        M3ControlStyles.add(actions, ACTIONS_STYLE_CLASS);
        M3ControlStyles.add(toggleButton, TOGGLE_STYLE_CLASS);
        M3ControlStyles.add(closeButton, CLOSE_STYLE_CLASS);
        updateCloseButtonVariant();
        toggleButton.variantProperty().addListener(observable -> updateCloseButtonVariant());
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        actions.getChildren().addListener(actionsListener);
        addEventHandler(ActionEvent.ACTION, this::handleActionItemAction);
        toggleButton.addEventHandler(ActionEvent.ACTION, event -> show());
        closeButton.addEventHandler(ActionEvent.ACTION, event -> hide());
        addEventHandler(KeyEvent.KEY_PRESSED, navigationKeyHandler);
        applyCollapsedState();
        focusNotifier.start();
    }

    /// Pairs the close button's solid color role with the entry button family.
    private void updateCloseButtonVariant() {
        M3InternalIcon closeIcon = (M3InternalIcon) closeButton.getGraphic();
        switch (toggleButton.getVariant()) {
            case SURFACE, PRIMARY_CONTAINER, PRIMARY -> {
                closeButton.setVariant(M3FloatingActionButtonVariant.PRIMARY);
                closeIcon.setColorRole(M3InternalIcon.ColorRole.ON_PRIMARY);
            }
            case SECONDARY_CONTAINER, SECONDARY -> {
                closeButton.setVariant(M3FloatingActionButtonVariant.SECONDARY);
                closeIcon.setColorRole(M3InternalIcon.ColorRole.ON_SECONDARY);
            }
            case TERTIARY_CONTAINER, TERTIARY -> {
                closeButton.setVariant(M3FloatingActionButtonVariant.TERTIARY);
                closeIcon.setColorRole(M3InternalIcon.ColorRole.ON_TERTIARY);
            }
        }
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
        if (event.getCode() == KeyCode.ESCAPE && isExpanded()) {
            hide();
            event.consume();
            return;
        }

        List<Node> targets = navigationTargets();
        if (M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                targets,
                false,
                true,
                Math.max(targets.indexOf(isExpanded() ? closeButton : toggleButton), 0)
        )) {
            notifyFocusNodeChanged();
        }
    }

    /// Returns the currently focusable actions followed by the visible activator button.
    private List<Node> navigationTargets() {
        return M3FocusTraversal.focusTargets(getItems(), isExpanded() ? closeButton : toggleButton);
    }

    /// Applies expanded state, using animation only after the control is attached to a scene.
    private void setExpandedState(boolean expanded) {
        boolean restoreToggleFocus = !expanded && (isFocusInsideActionItems() || closeButton.isFocused());
        boolean transferToggleFocus = expanded && toggleButton.isFocused();
        stopAnimation();
        pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, expanded);
        if (getScene() == null) {
            if (expanded) {
                applyExpandedState();
            } else {
                applyCollapsedState();
            }
            if (transferToggleFocus) {
                M3Accessible.showDirectItem(this, closeButton);
            }
            restoreToggleFocusAfterCollapse(restoreToggleFocus);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
            return;
        }

        if (expanded) {
            playExpandAnimation();
        } else {
            playCollapseAnimation();
        }
        if (transferToggleFocus) {
            M3Accessible.showDirectItem(this, closeButton);
        }
        restoreToggleFocusAfterCollapse(restoreToggleFocus);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
    }

    /// Returns the current focusable menu target for accessibility clients.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node externalTarget = activeExternalActionFocusNode();
        if (externalTarget != null) {
            return externalTarget;
        }

        List<Node> targets = navigationTargets();
        if (targets.isEmpty()) {
            return null;
        }

        int currentIndex = M3FocusTraversal.focusedTargetIndex(this, targets);
        if (currentIndex < 0) {
            currentIndex = targets.indexOf(isExpanded() ? closeButton : toggleButton);
        }
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        return M3Accessible.focusTarget(targets.get(currentIndex));
    }

    /// Requests focus on the current action or toggle target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (M3Accessible.showItem(this, accessibleFocusNode())) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Shows the action menu and focuses the requested action, preserving a currently focused action by default.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested action target
    final boolean showAccessibleItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (parameters.length > 0 && M3Accessible.containsUnrevealableActionNodeTarget(getItems(), parameters)) {
            return false;
        }
        @Nullable Node target;
        if (parameters.length == 0) {
            target = activeExternalActionFocusNode();
            if (target == null) {
                target = currentActionFocusNode();
            }
        } else {
            target = M3Accessible.actionItem(getItems(), parameters);
        }
        boolean hasNestedTarget = parameters.length > 0
                && target == null
                && containsNestedAccessibleActionTarget(parameters);
        if (parameters.length > 0 && target == null && !hasNestedTarget) {
            return false;
        }
        show();
        if (parameters.length == 0 && target == null) {
            target = M3Accessible.actionItem(getItems());
        }
        boolean shown = parameters.length > 0 && M3Accessible.showAccessibleActionTarget(this, getItems(), parameters);
        if (!shown && target != null) {
            shown = M3Accessible.showItem(this, target);
        } else if (!shown && hasNestedTarget) {
            shown = M3Accessible.showAccessibleActionTarget(this, getItems(), parameters);
        }
        if (shown) {
            notifyFocusNodeChanged();
        }
        return shown;
    }


    /// Returns whether an action item exposes the requested nested accessibility target.
    private boolean containsNestedAccessibleActionTarget(Object... parameters) {
        for (Node item : getItems()) {
            if (M3Accessible.containsAccessibleActionTarget(item, parameters)) {
                return true;
            }
        }
        return false;
    }

    /// Returns the active focus target exposed by action-owned external popup content.
    private @Nullable Node activeExternalActionFocusNode() {
        for (Node item : getItems()) {
            @Nullable Node target = M3Accessible.activeExternalFocusTarget(this, item);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the focused action item target, ignoring the menu toggle button.
    private @Nullable Node currentActionFocusNode() {
        if (getScene() == null) {
            return null;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        if (focusOwner == null) {
            return null;
        }

        for (Node item : getItems()) {
            if (M3Accessible.containsNode(item, focusOwner)) {
                @Nullable Node focusTarget = M3Accessible.focusTarget(item);
                return focusTarget == null ? null : M3Accessible.canReach(focusOwner) ? focusOwner : focusTarget;
            }
        }
        return null;
    }

    /// Returns whether keyboard focus is currently inside one of the expanded action items.
    private boolean isFocusInsideActionItems() {
        if (getScene() == null) {
            return false;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        if (focusOwner == null) {
            return false;
        }

        for (Node item : getItems()) {
            if (M3Accessible.containsNode(item, focusOwner)) {
                return true;
            }
        }
        return false;
    }

    /// Moves focus back to the toggle button after collapsing hidden action items.
    private void restoreToggleFocusAfterCollapse(boolean restoreToggleFocus) {
        if (restoreToggleFocus) {
            M3Accessible.showDirectItem(this, toggleButton);
        }
    }

    /// Notifies accessibility clients that visible FAB menu targets changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyFocusNodeChanged();
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Plays the action item expand animation.
    private void playExpandAnimation() {
        prepareActionsForExpandedLayout();
        animation.configure(M3Animation.defaultSpatial(this), true);
        playConfiguredAnimation();
    }

    /// Plays the action item collapse animation.
    private void playCollapseAnimation() {
        animation.configure(M3Animation.fastSpatial(this), false);
        playConfiguredAnimation();
    }

    /// Starts the configured action transition using the resolved motion setting.
    private void playConfiguredAnimation() {
        M3Animation.playFromStart(this, animation);
    }

    /// Stops the current expand or collapse animation and releases its item references.
    private void stopAnimation() {
        animation.stop();
        animation.clearTargets();
    }

    /// Applies changed runtime motion settings to the active expand or collapse animation.
    private void refreshMotionSettings() {
        if (!M3Animation.areAnimationsEnabled(this) || !isShowingWindow()) {
            M3Animation.finishIfRunning(animation);
        }
    }

    /// Returns whether this menu is attached to a currently showing window.
    private boolean isShowingWindow() {
        @Nullable Scene scene = getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window != null && window.isShowing();
    }

    /// Prepares action items to participate in layout while expanded.
    private void prepareActionsForExpandedLayout() {
        actions.setVisible(true);
        actions.setManaged(true);
        int index = 0;
        int itemCount = getItems().size();
        for (Node item : getItems()) {
            if (Double.compare(item.getOpacity(), 0.0) == 0) {
                item.setScaleX(ACTION_TRANSITION_SCALE);
                item.setScaleY(ACTION_TRANSITION_SCALE);
                item.setTranslateY(collapsedOffset(itemCount, index));
            }
            item.setVisible(true);
            item.setManaged(true);
            index++;
        }
        requestMenuLayout();
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
        toggleButton.setOpacity(0.0);
        toggleButton.setScaleX(ACTION_TRANSITION_SCALE);
        toggleButton.setScaleY(ACTION_TRANSITION_SCALE);
        toggleButton.setVisible(false);
        toggleButton.setMouseTransparent(true);
        closeButton.setVisible(true);
        closeButton.setOpacity(1.0);
        closeButton.setScaleX(1.0);
        closeButton.setScaleY(1.0);
        closeButton.setMouseTransparent(false);
    }

    /// Applies the final collapsed item state.
    private void applyCollapsedState() {
        actions.setVisible(false);
        actions.setManaged(false);
        int index = 0;
        int itemCount = getItems().size();
        for (Node item : getItems()) {
            prepareCollapsedAction(item, collapsedOffset(itemCount, index++));
        }
        closeButton.setOpacity(0.0);
        closeButton.setScaleX(ACTION_TRANSITION_SCALE);
        closeButton.setScaleY(ACTION_TRANSITION_SCALE);
        closeButton.setVisible(false);
        closeButton.setMouseTransparent(true);
        toggleButton.setVisible(true);
        toggleButton.setOpacity(1.0);
        toggleButton.setScaleX(1.0);
        toggleButton.setScaleY(1.0);
        toggleButton.setMouseTransparent(false);
        requestMenuLayout();
    }

    /// Adds style classes and current visibility to an action item.
    private void installAction(Node item) {
        M3ControlStyles.add(item, ACTION_STYLE_CLASS);
        item.addEventHandler(ActionEvent.ACTION, actionItemActionHandler);
        item.addEventFilter(KeyEvent.KEY_PRESSED, navigationKeyHandler);
        if (isExpanded()) {
            item.setVisible(true);
            item.setManaged(true);
            item.setOpacity(1.0);
            item.setScaleX(1.0);
            item.setScaleY(1.0);
            item.setTranslateY(0.0);
        } else {
            prepareCollapsedAction(item, collapsedOffset(getItems().size(), getItems().indexOf(item)));
        }
        requestMenuLayout();
    }

    /// Requests this menu and its layout parent to recompute bounds after action visibility changes.
    private void requestMenuLayout() {
        actions.requestLayout();
        requestLayout();
        @Nullable Parent parent = getParent();
        if (parent != null) {
            parent.requestLayout();
        }
    }

    /// Applies collapsed transforms to an action item.
    private static void prepareCollapsedAction(Node item, double translateY) {
        item.setVisible(false);
        item.setManaged(false);
        item.setOpacity(0.0);
        item.setScaleX(ACTION_TRANSITION_SCALE);
        item.setScaleY(ACTION_TRANSITION_SCALE);
        item.setTranslateY(translateY);
    }

    /// Returns the collapsed vertical offset for one action item.
    private static double collapsedOffset(int itemCount, int index) {
        return ACTION_TRANSITION_OFFSET_Y * Math.max(1, itemCount - index);
    }

    /// Applies the final state after the reusable action animation completes.
    private void handleActionAnimationFinished() {
        animation.clearTargets();
        if (isExpanded()) {
            applyExpandedState();
        } else {
            applyCollapsedState();
        }
    }

    /// Reuses primitive start-value storage for all FAB menu action transitions.
    @NotNullByDefault
    private final class ActionItemsTransition extends M3FiniteTransition {
        /// Nodes participating in the current transition.
        private @Nullable Node[] targets = new Node[0];

        /// Starting opacity values parallel to [#targets].
        private double[] startOpacities = new double[0];

        /// Starting horizontal scale values parallel to [#targets].
        private double[] startScaleX = new double[0];

        /// Starting vertical scale values parallel to [#targets].
        private double[] startScaleY = new double[0];

        /// Starting vertical translations parallel to [#targets].
        private double[] startTranslateY = new double[0];

        /// Starting opacity of the entry button.
        private double startToggleOpacity;

        /// Starting horizontal scale of the entry button.
        private double startToggleScaleX;

        /// Starting vertical scale of the entry button.
        private double startToggleScaleY;

        /// Starting opacity of the close button.
        private double startCloseOpacity;

        /// Starting horizontal scale of the close button.
        private double startCloseScaleX;

        /// Starting vertical scale of the close button.
        private double startCloseScaleY;

        /// The number of populated target slots.
        private int targetCount;

        /// Whether the configured transition expands rather than collapses actions.
        private boolean expanding;

        /// Creates a reusable action transition and installs its stable completion handler.
        private ActionItemsTransition() {
            setOnFinished(event -> handleActionAnimationFinished());
        }

        /// Captures current item transforms and configures their target state.
        private void configure(M3MotionSpec spec, boolean expanding) {
            stop();
            clearTargets();
            ObservableList<Node> items = getItems();
            ensureCapacity(items.size());
            targetCount = items.size();
            this.expanding = expanding;
            toggleButton.setVisible(true);
            closeButton.setVisible(true);
            toggleButton.setMouseTransparent(true);
            closeButton.setMouseTransparent(true);
            startToggleOpacity = toggleButton.getOpacity();
            startToggleScaleX = toggleButton.getScaleX();
            startToggleScaleY = toggleButton.getScaleY();
            startCloseOpacity = closeButton.getOpacity();
            startCloseScaleX = closeButton.getScaleX();
            startCloseScaleY = closeButton.getScaleY();
            for (int index = 0; index < targetCount; index++) {
                Node item = items.get(index);
                targets[index] = item;
                startOpacities[index] = item.getOpacity();
                startScaleX[index] = item.getScaleX();
                startScaleY[index] = item.getScaleY();
                startTranslateY[index] = item.getTranslateY();
            }
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
        }

        /// Grows transition storage only when an item list exceeds the retained capacity.
        private void ensureCapacity(int requiredCapacity) {
            if (targets.length >= requiredCapacity) {
                return;
            }
            int currentCapacity = targets.length;
            int nextCapacity = Math.max(requiredCapacity, currentCapacity == 0 ? 4 : currentCapacity * 2);
            targets = Arrays.copyOf(targets, nextCapacity);
            startOpacities = Arrays.copyOf(startOpacities, nextCapacity);
            startScaleX = Arrays.copyOf(startScaleX, nextCapacity);
            startScaleY = Arrays.copyOf(startScaleY, nextCapacity);
            startTranslateY = Arrays.copyOf(startTranslateY, nextCapacity);
        }

        /// Releases action node references after completion or interruption.
        private void clearTargets() {
            Arrays.fill(targets, 0, targetCount, null);
            targetCount = 0;
        }

        /// Applies one eased frame without allocating writable values or animation nodes.
        @Override
        protected void interpolate(double fraction) {
            double targetOpacity = expanding ? 1.0 : 0.0;
            double targetScale = expanding ? 1.0 : ACTION_TRANSITION_SCALE;
            for (int index = 0; index < targetCount; index++) {
                Node item = Objects.requireNonNull(targets[index], "target");
                double targetTranslateY = expanding ? 0.0 : collapsedOffset(targetCount, index);
                item.setOpacity(interpolate(startOpacities[index], targetOpacity, fraction));
                item.setScaleX(interpolate(startScaleX[index], targetScale, fraction));
                item.setScaleY(interpolate(startScaleY[index], targetScale, fraction));
                item.setTranslateY(interpolate(startTranslateY[index], targetTranslateY, fraction));
            }
            double targetToggleOpacity = expanding ? 0.0 : 1.0;
            double targetToggleScale = expanding ? ACTION_TRANSITION_SCALE : 1.0;
            double targetCloseOpacity = expanding ? 1.0 : 0.0;
            double targetCloseScale = expanding ? 1.0 : ACTION_TRANSITION_SCALE;
            toggleButton.setOpacity(interpolate(startToggleOpacity, targetToggleOpacity, fraction));
            toggleButton.setScaleX(interpolate(startToggleScaleX, targetToggleScale, fraction));
            toggleButton.setScaleY(interpolate(startToggleScaleY, targetToggleScale, fraction));
            closeButton.setOpacity(interpolate(startCloseOpacity, targetCloseOpacity, fraction));
            closeButton.setScaleX(interpolate(startCloseScaleX, targetCloseScale, fraction));
            closeButton.setScaleY(interpolate(startCloseScaleY, targetCloseScale, fraction));
        }

        /// Interpolates one primitive channel.
        private static double interpolate(double start, double end, double fraction) {
            return start + (end - start) * fraction;
        }
    }

    /// Removes menu-specific style classes and transient transforms from an action item.
    private void clearActionStyle(Node item) {
        item.getStyleClass().remove(ACTION_STYLE_CLASS);
        item.removeEventHandler(ActionEvent.ACTION, actionItemActionHandler);
        item.removeEventFilter(KeyEvent.KEY_PRESSED, navigationKeyHandler);
        item.setVisible(true);
        item.setManaged(true);
        item.setOpacity(1.0);
        item.setScaleX(1.0);
        item.setScaleY(1.0);
        item.setTranslateY(0.0);
    }

    /// Creates the default menu toggle floating action button.
    private static M3FloatingActionButton createDefaultToggleButton() {
        M3FloatingActionButton button = new M3FloatingActionButton(new M3InternalIcon(
                M3InternalIcon.Glyph.ADD,
                M3InternalIcon.ColorRole.ON_PRIMARY_CONTAINER
        ));
        button.setVariant(M3FloatingActionButtonVariant.PRIMARY_CONTAINER);
        button.setSize(M3FloatingActionButtonSize.REGULAR);
        return button;
    }

    /// Creates the 56-logical-pixel solid close button shown by an expanded menu.
    private static M3FloatingActionButton createCloseButton() {
        M3FloatingActionButton button = new M3FloatingActionButton(new M3InternalIcon(
                M3InternalIcon.Glyph.CLOSE,
                M3InternalIcon.ColorRole.ON_PRIMARY_CONTAINER,
                20.0
        ));
        button.setVariant(M3FloatingActionButtonVariant.PRIMARY);
        button.setSize(M3FloatingActionButtonSize.REGULAR);
        return button;
    }

    /// Creates the default Material Design 3 floating action button menu skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3FabMenuSkin(this, actions, toggleButton, closeButton);
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


        /// CSS metadata for spacing before the close button.
        private static final CssMetaData<M3FabMenu, Number> CLOSE_SPACING =
                new CssMetaData<>("-m3-fab-menu-close-spacing", SizeConverter.getInstance(), DEFAULT_CLOSE_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3FabMenu control) {
                        return M3Css.isSettable(control.closeSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3FabMenu control) {
                        return control.closeSpacingProperty();
                    }
                };
        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ACTION_SPACING);
            styleables.add(CLOSE_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
