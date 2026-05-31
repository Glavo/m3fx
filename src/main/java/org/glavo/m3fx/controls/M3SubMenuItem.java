// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.NodeOrientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 menu item that opens a nested menu.
///
/// `M3SubMenuItem` extends [M3MenuItem] with a child [M3Menu] and popup positioning behavior. It can open its
/// submenu from pointer hover, keyboard navigation, or explicit API calls, and it inherits theme context for the
/// nested popup surface.
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public class M3SubMenuItem extends M3MenuItem {
    /// The base style class for M3FX submenu items.
    public static final String STYLE_CLASS = "m3-sub-menu-item";

    /// The style class applied to the default submenu indicator.
    public static final String INDICATOR_STYLE_CLASS = "m3-sub-menu-indicator";

    /// The horizontal overlap used when a submenu opens beside its owner item.
    private static final double SUB_MENU_OFFSET_X = -1.0;

    /// The initial popup menu scale used for enter and exit motion.
    private static final double SUB_MENU_TRANSITION_SCALE = 0.96;

    /// The initial horizontal popup menu offset used for enter and exit motion.
    private static final double SUB_MENU_TRANSITION_OFFSET_X = -6.0;

    /// The submenu displayed by this item.
    private final M3Menu subMenu = new M3Menu();

    /// The default submenu indicator used when no custom trailing content is set.
    private final M3Icon defaultIndicator = createDefaultIndicator();

    /// The menu that directly owns this submenu item.
    private @Nullable M3Menu ownerMenu;

    /// The popup window used to host the submenu.
    private final Popup popup = new Popup();

    // Backing property for the public read-only submenu showing state API.
    private final ReadOnlyBooleanWrapper subMenuShowing = new ReadOnlyBooleanWrapper(this, "subMenuShowing");

    /// The submenu popup enter animation.
    private final Timeline showAnimation = new Timeline();

    /// The submenu popup exit animation.
    private final Timeline hideAnimation = new Timeline();

    /// Observes runtime motion settings while this item is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Reports popup submenu focus changes through this item's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, subMenu, this::focusNode, this::notifyFocusNodeChanged);

    /// Notifies popup owners when this item's reported focus node changes.
    private final List<Runnable> focusNodeListeners = new ArrayList<>();

    /// The pointer-hover open delay.
    private final PauseTransition hoverOpenDelay = new PauseTransition();

    /// The pointer-exit close delay.
    private final PauseTransition hoverCloseDelay = new PauseTransition();

    /// Updates the default indicator glyph when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateDefaultIndicatorDirection();

    /// Whether an action from the submenu is being forwarded to this item's parent menu.
    private boolean forwardingSubMenuAction = false;

    /// Whether focus should return to this item after the submenu popup hides.
    private boolean focusOwnerOnHidden = false;

    /// Whether the pointer is currently over this menu item.
    private boolean pointerInsideOwner = false;

    /// Whether the pointer is currently over this item's submenu popup.
    private boolean pointerInsideSubMenu = false;

    /// The horizontal transition offset used by the current popup side.
    private double currentTransitionOffsetX = SUB_MENU_TRANSITION_OFFSET_X;

    /// Creates an empty submenu item.
    public M3SubMenuItem() {
        this("");
    }

    /// Creates a submenu item with text.
    ///
    /// @param text the submenu item text
    public M3SubMenuItem(String text) {
        super(text);
        initialize();
    }

    /// Creates a submenu item with text and submenu content.
    ///
    /// @param text the submenu item text
    /// @param items the submenu item nodes
    public M3SubMenuItem(String text, Node... items) {
        this(text);
        addItems(items);
    }

    /// Returns the submenu displayed by this item.
    ///
    /// @return the submenu displayed by this item
    public final M3Menu getSubMenu() {
        return subMenu;
    }

    /// Returns the mutable item list shown by this item's submenu.
    ///
    /// @return the mutable item list shown by this item's submenu
    public final ObservableList<Node> getItems() {
        return subMenu.getItems();
    }

    /// Adds one submenu item node.
    ///
    /// @param item the submenu item node to add
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds submenu item nodes.
    ///
    /// @param items the submenu item nodes to add
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all submenu item nodes.
    ///
    /// @param items the replacement submenu item nodes
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all submenu item nodes.
    public final void clearItems() {
        getItems().clear();
    }

    /// Sets the menu that directly owns this submenu item.
    final void setOwnerMenu(@Nullable M3Menu ownerMenu) {
        this.ownerMenu = ownerMenu;
    }

    /// Returns whether the submenu popup is currently showing.
    ///
    /// @return `true` if the submenu popup is currently showing
    public final boolean isSubMenuShowing() {
        return subMenuShowing.get();
    }

    /// Returns the read-only submenu showing state property.
    ///
    /// @return the read-only submenu showing state property
    public final ReadOnlyBooleanProperty subMenuShowingProperty() {
        return subMenuShowing.getReadOnlyProperty();
    }

    /// Shows the submenu popup beside this item.
    public final void showSubMenu() {
        if (isDisabled()) {
            return;
        }
        hoverOpenDelay.stop();
        hoverCloseDelay.stop();
        if (popup.isShowing()) {
            hideAnimation.stop();
            subMenuShowing.set(true);
            playShowAnimation();
            return;
        }

        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null) {
            return;
        }

        hideSiblingSubMenus();
        prepareSubMenuForPopup(scene);
        subMenu.setMinWidth(Math.max(getWidth(), subMenu.minWidth(-1.0)));
        @Nullable M3PopupPositioning.Placement placement =
                M3PopupPositioning.subMenuBeside(this, subMenu, SUB_MENU_OFFSET_X);
        if (placement == null) {
            return;
        }
        currentTransitionOffsetX = placement.opensToLeft()
                ? -SUB_MENU_TRANSITION_OFFSET_X
                : SUB_MENU_TRANSITION_OFFSET_X;
        prepareSubMenuForShowAnimation();
        popup.show(this, placement.x(), placement.y());
        subMenuShowing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
        playShowAnimation();
    }

    /// Hides the submenu popup.
    public final void hideSubMenu() {
        hideSubMenu(false);
    }

    /// Adds a listener that runs when this submenu item's accessible focus node changes.
    ///
    /// @param listener the listener to add
    final void addAccessibleFocusNodeListener(Runnable listener) {
        Objects.requireNonNull(listener, "listener");
        if (!focusNodeListeners.contains(listener)) {
            focusNodeListeners.add(listener);
        }
    }

    /// Removes an accessible focus-node listener.
    ///
    /// @param listener the listener to remove
    final void removeAccessibleFocusNodeListener(Runnable listener) {
        focusNodeListeners.remove(Objects.requireNonNull(listener, "listener"));
    }

    /// Hides the submenu popup and optionally returns focus to this item.
    private void hideSubMenu(boolean focusOwner) {
        hoverOpenDelay.stop();
        hoverCloseDelay.stop();
        subMenu.hideSubMenusExcept(null);
        focusOwnerOnHidden = focusOwner;
        if (!popup.isShowing()) {
            if (focusOwner) {
                requestFocus();
            }
            focusOwnerOnHidden = false;
            return;
        }

        showAnimation.stop();
        if (hideAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hideAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                event -> popup.hide(),
                new KeyValue(subMenu.opacityProperty(), 0.0, spec.interpolator()),
                new KeyValue(subMenu.scaleXProperty(), SUB_MENU_TRANSITION_SCALE, spec.interpolator()),
                new KeyValue(subMenu.scaleYProperty(), SUB_MENU_TRANSITION_SCALE, spec.interpolator()),
                new KeyValue(subMenu.translateXProperty(), currentTransitionOffsetX, spec.interpolator())
        ));
        M3Animation.playFromStart(this, hideAnimation);
    }

    /// Returns accessibility attributes for submenu content and expanded state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isSubMenuShowing();
            case SUBMENU -> subMenu;
            case FOCUS_NODE -> focusNode();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> subMenu.getSelectionMode() == M3MenuSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> subMenu.getSelectedItems();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes submenu-related accessibility actions.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SHOW_MENU, EXPAND -> showSubMenu();
            case COLLAPSE -> hideSubMenu(true);
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SET_SELECTED_ITEMS -> subMenu.executeAccessibleAction(action, parameters);
            case SHOW_ITEM -> {
                showSubMenu();
                subMenu.executeAccessibleAction(action, parameters);
                notifyFocusNodeChanged();
            }
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and configures submenu popup behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        if (getTrailing() == null) {
            setTrailing(defaultIndicator);
        }
        updateDefaultIndicatorDirection();
        effectiveNodeOrientationProperty().addListener(nodeOrientationInvalidation);
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setSelected(false);
            }
        });
        disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                hideSubMenu();
            }
        });
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                hideSubMenu();
            }
        });
        hoverOpenDelay.setOnFinished(event -> {
            if (pointerInsideOwner) {
                showSubMenu();
            }
        });
        hoverCloseDelay.setOnFinished(event -> {
            if (!pointerInsideOwner && !pointerInsideSubMenu) {
                hideSubMenu();
            }
        });
        popup.setAutoHide(true);
        popup.getContent().add(subMenu);
        popup.setOnHidden(event -> {
            pointerInsideOwner = false;
            pointerInsideSubMenu = false;
            subMenuShowing.set(false);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
            resetSubMenuAnimationState();
            if (focusOwnerOnHidden) {
                focusOwnerOnHidden = false;
                requestFocus();
            }
        });
        addEventFilter(ActionEvent.ACTION, this::handleOwnActionEvent);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        addEventHandler(MouseEvent.MOUSE_ENTERED, this::handleMouseEntered);
        addEventHandler(MouseEvent.MOUSE_EXITED, this::handleMouseExited);
        subMenu.addEventHandler(KeyEvent.KEY_PRESSED, this::handleSubMenuKeyPressed);
        subMenu.addEventHandler(ActionEvent.ACTION, this::handleSubMenuAction);
        subMenu.addEventHandler(MouseEvent.MOUSE_ENTERED, this::handleSubMenuMouseEntered);
        subMenu.addEventHandler(MouseEvent.MOUSE_EXITED, this::handleSubMenuMouseExited);
        subMenu.addAccessibleFocusNodeListener(this::notifyFocusNodeChanged);
        popupFocusNotifier.start();
    }

    /// Returns the current submenu focus node for accessibility clients.
    private Node focusNode() {
        if (!isSubMenuShowing()) {
            return this;
        }
        @Nullable Object focusNode = subMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node ? node : this;
    }

    /// Requests focus for this item or the currently reachable submenu focus node.
    private void focusAccessibleNode() {
        if (!isSubMenuShowing()) {
            requestFocus();
            notifyFocusNodeChanged();
            return;
        }

        @Nullable Object focusNode = subMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && node != this) {
            M3Accessible.showItem(node);
            notifyFocusNodeChanged();
            return;
        }

        subMenu.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
        @Nullable Object nextFocusNode = subMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (!(nextFocusNode instanceof Node node) || node == this) {
            requestFocus();
        }
        notifyFocusNodeChanged();
    }

    /// Handles this item's own action event by opening the submenu.
    private void handleOwnActionEvent(ActionEvent event) {
        if (forwardingSubMenuAction) {
            return;
        }
        showSubMenu();
        event.consume();
    }

    /// Schedules submenu opening when the pointer enters this item.
    private void handleMouseEntered(MouseEvent event) {
        if (isDisabled()) {
            return;
        }
        pointerInsideOwner = true;
        hoverCloseDelay.stop();
        if (!popup.isShowing()) {
            hoverOpenDelay.setDuration(M3Animation.motionBehavior(this).subMenuHoverOpenDelay());
            hoverOpenDelay.playFromStart();
        }
    }

    /// Schedules submenu closing when the pointer exits this item.
    private void handleMouseExited(MouseEvent event) {
        pointerInsideOwner = false;
        hoverOpenDelay.stop();
        scheduleHoverClose();
    }

    /// Cancels submenu closing while the pointer is inside the submenu popup.
    private void handleSubMenuMouseEntered(MouseEvent event) {
        pointerInsideSubMenu = true;
        hoverCloseDelay.stop();
    }

    /// Schedules submenu closing when the pointer exits the submenu popup.
    private void handleSubMenuMouseExited(MouseEvent event) {
        pointerInsideSubMenu = false;
        scheduleHoverClose();
    }

    /// Handles keyboard actions on the submenu item.
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.ENTER || code == KeyCode.SPACE || isOpenSubMenuKey(code)) {
            if (showSubMenuAndFocusFirstItem()) {
                event.consume();
            }
        } else if (code == KeyCode.ESCAPE || isCloseSubMenuKey(code)) {
            if (popup.isShowing()) {
                hideSubMenu(true);
                event.consume();
            }
        }
    }

    /// Handles keyboard dismissal while focus is inside the submenu.
    private void handleSubMenuKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.ESCAPE || isCloseSubMenuKey(code)) {
            if (popup.isShowing()) {
                hideSubMenu(true);
                event.consume();
            }
        }
    }

    /// Forwards submenu item actions so an owning popup menu can close.
    private void handleSubMenuAction(ActionEvent event) {
        hideSubMenu();
        forwardingSubMenuAction = true;
        try {
            Event.fireEvent(this, new ActionEvent(event.getSource(), this));
        } finally {
            forwardingSubMenuAction = false;
        }
    }

    /// Returns whether this item is currently forwarding an action from its submenu.
    final boolean isForwardingSubMenuAction() {
        return forwardingSubMenuAction;
    }

    /// Shows the submenu and focuses its first enabled visible item.
    final boolean showSubMenuAndFocusFirstItem() {
        boolean showingBefore = popup.isShowing();
        showSubMenu();
        if (!popup.isShowing() && !showingBefore) {
            return false;
        }

        subMenu.focusFirstItem();
        notifyFocusNodeChanged();
        return true;
    }

    /// Notifies clients and popup owners that the current accessible focus node changed.
    private void notifyFocusNodeChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        popupFocusNotifier.refresh();
        for (Runnable listener : List.copyOf(focusNodeListeners)) {
            listener.run();
        }
    }

    /// Applies initial visual state before the submenu is shown.
    private void prepareSubMenuForShowAnimation() {
        hideAnimation.stop();
        subMenu.setOpacity(0.0);
        subMenu.setScaleX(SUB_MENU_TRANSITION_SCALE);
        subMenu.setScaleY(SUB_MENU_TRANSITION_SCALE);
        subMenu.setTranslateX(currentTransitionOffsetX);
    }

    /// Plays the submenu popup enter animation.
    private void playShowAnimation() {
        showAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        showAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(subMenu.opacityProperty(), 1.0, spec.interpolator()),
                new KeyValue(subMenu.scaleXProperty(), 1.0, spec.interpolator()),
                new KeyValue(subMenu.scaleYProperty(), 1.0, spec.interpolator()),
                new KeyValue(subMenu.translateXProperty(), 0.0, spec.interpolator())
        ));
        M3Animation.playFromStart(this, showAnimation);
    }

    /// Resets transient submenu animation transforms.
    private void resetSubMenuAnimationState() {
        showAnimation.stop();
        hideAnimation.stop();
        subMenu.setOpacity(1.0);
        subMenu.setScaleX(1.0);
        subMenu.setScaleY(1.0);
        subMenu.setTranslateX(0.0);
    }

    /// Applies changed runtime motion settings to active submenu popup animations.
    private void refreshMotionSettings() {
        if (popup.isShowing()) {
            M3Animation.copyResolvedMotionSettings(this, subMenu);
        }
        M3Animation.finishRunningAnimationsIfDisabled(this, showAnimation, hideAnimation);
    }

    /// Returns whether a key opens the submenu for the current node orientation.
    private boolean isOpenSubMenuKey(KeyCode keyCode) {
        return keyCode == (isRightToLeft() ? KeyCode.LEFT : KeyCode.RIGHT);
    }

    /// Returns whether a key closes the submenu for the current node orientation.
    private boolean isCloseSubMenuKey(KeyCode keyCode) {
        return keyCode == (isRightToLeft() ? KeyCode.RIGHT : KeyCode.LEFT);
    }

    /// Returns whether this item is rendered in right-to-left orientation.
    private boolean isRightToLeft() {
        return getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
    }

    /// Updates the default indicator to point toward the submenu opening side.
    private void updateDefaultIndicatorDirection() {
        defaultIndicator.setText(isRightToLeft() ? "<" : ">");
    }

    /// Starts the pointer-exit close delay when the submenu is open.
    private void scheduleHoverClose() {
        if (popup.isShowing()) {
            hoverCloseDelay.setDuration(M3Animation.motionBehavior(this).subMenuHoverCloseDelay());
            hoverCloseDelay.playFromStart();
        }
    }

    /// Copies scene styles and theme declarations into the popup-hosted submenu.
    private void prepareSubMenuForPopup(Scene scene) {
        copyPopupStylesheets(scene);
        String menuStylesheet = M3Stylesheets.controlStylesheet("menu.css");
        if (!subMenu.getStylesheets().contains(menuStylesheet)) {
            subMenu.getStylesheets().add(menuStylesheet);
        }

        M3ThemeManager.copyThemeContext(popupThemeSource(scene), subMenu);
        M3Animation.copyResolvedMotionSettings(this, subMenu);
        subMenu.setNodeOrientation(getEffectiveNodeOrientation());
        subMenu.applyCss();
    }

    /// Copies stylesheets from the owning popup menu when this item is already inside a popup branch.
    private void copyPopupStylesheets(Scene scene) {
        if (ownerMenu != null && !ownerMenu.getStylesheets().isEmpty()) {
            subMenu.getStylesheets().setAll(ownerMenu.getStylesheets());
        } else {
            subMenu.getStylesheets().setAll(scene.getStylesheets());
        }
    }

    /// Returns the root that should supply looked-up theme tokens for the submenu popup.
    private Parent popupThemeSource(Scene scene) {
        @Nullable Parent ownerMenuThemeRoot = ownerMenu == null ? null : M3ThemeResolver.findThemeRoot(ownerMenu);
        if (ownerMenuThemeRoot != null) {
            return ownerMenuThemeRoot;
        }
        @Nullable Parent itemThemeRoot = M3ThemeResolver.findThemeRoot(this);
        if (itemThemeRoot != null) {
            return itemThemeRoot;
        }
        return scene.getRoot();
    }

    /// Hides sibling submenu popups owned by the same parent menu.
    private void hideSiblingSubMenus() {
        if (ownerMenu != null) {
            ownerMenu.hideSubMenusExcept(this);
        }
    }

    /// Creates the default trailing submenu indicator.
    private static M3Icon createDefaultIndicator() {
        M3Icon indicator = new M3Icon(">", M3IconSize.SMALL, M3IconVariant.ON_SURFACE_VARIANT);
        M3ControlStyles.add(indicator, INDICATOR_STYLE_CLASS);
        return indicator;
    }

    /// Validates a submenu item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }
}
