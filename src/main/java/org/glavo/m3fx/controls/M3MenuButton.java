// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 menu button backed by an M3FX menu popup.
///
/// `M3MenuButton` behaves like an [M3Button] that owns an [M3Menu] and a popup window. It manages menu show and
/// hide state, theme propagation for popup content, focus return, keyboard dismissal, and Material popup motion.
/// Add menu content through [getItems] or operate directly on [getMenu].
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public class M3MenuButton extends M3Button {
    /// The base style class for m3fx menu buttons.
    public static final String STYLE_CLASS = "m3-menu-button";

    /// The vertical gap between the button and popup menu.
    private static final double MENU_OFFSET_Y = 4.0;

    /// The initial popup menu scale used for enter and exit motion.
    private static final double MENU_TRANSITION_SCALE = 0.96;

    /// The initial vertical popup menu offset used for enter and exit motion.
    private static final double MENU_TRANSITION_OFFSET_Y = -6.0;

    /// The menu displayed by this button.
    private final M3Menu menu = new M3Menu();

    /// The popup window used to host the menu.
    private final Popup popup = new Popup();

    // Whether this menu button popup is currently showing.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The menu popup enter animation.
    private final Timeline showAnimation = new Timeline();

    /// The menu popup exit animation.
    private final Timeline hideAnimation = new Timeline();

    /// Observes runtime motion settings while this button is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Reports popup menu focus changes through this button's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, menu, this::focusNode, this::notifyPopupFocusNodeChanged);

    /// Notifies composite owners when this button's reported focus node changes.
    private final List<Runnable> popupFocusNodeListeners = new ArrayList<>();

    /// Whether focus should return to the owner button after the popup hides.
    private boolean focusOwnerOnHidden;

    /// Creates an empty menu button.
    public M3MenuButton() {
        this("");
    }

    /// Creates a menu button with text.
    ///
    /// @param text the button text
    public M3MenuButton(String text) {
        super(text);
        initialize();
    }

    /// Creates a menu button with text and menu items.
    ///
    /// @param text the button text
    /// @param items the initial non-null menu content nodes
    public M3MenuButton(String text, Node... items) {
        this(text);
        addItems(items);
    }

    /// Returns the menu displayed by this button.
    ///
    /// @return the menu displayed in this button's popup
    public final M3Menu getMenu() {
        return menu;
    }

    /// Returns the mutable item list shown by this button's menu.
    ///
    /// @return the mutable menu content list
    public final ObservableList<Node> getItems() {
        return menu.getItems();
    }

    /// Adds one menu item node.
    ///
    /// @param item the non-null node to append to the menu
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds menu item nodes.
    ///
    /// @param items the non-null nodes to append to the menu
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all menu item nodes.
    ///
    /// @param items the non-null nodes that replace the current menu content
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all menu item nodes.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns the menu item selection mode used by this button's menu.
    ///
    /// @return the active menu selection mode
    public final M3MenuSelectionMode getSelectionMode() {
        return menu.getSelectionMode();
    }

    /// Sets the menu item selection mode used by this button's menu.
    ///
    /// @param selectionMode the active menu selection mode
    public final void setSelectionMode(M3MenuSelectionMode selectionMode) {
        menu.setSelectionMode(selectionMode);
    }

    /// Returns the menu item selection mode property.
    ///
    /// @return the writable menu selection mode property
    public final ObjectProperty<M3MenuSelectionMode> selectionModeProperty() {
        return menu.selectionModeProperty();
    }

    /// Returns whether this button's menu allows all selectable items to be unselected.
    ///
    /// @return `true` when all selectable menu items may be unselected
    public final boolean isAllowEmptySelection() {
        return menu.isAllowEmptySelection();
    }

    /// Sets whether this button's menu allows all selectable items to be unselected.
    ///
    /// @param allowEmptySelection whether all selectable menu items may be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        menu.setAllowEmptySelection(allowEmptySelection);
    }

    /// Returns the empty-selection policy property for this button's menu.
    ///
    /// @return the writable empty-selection policy property
    public final BooleanProperty allowEmptySelectionProperty() {
        return menu.allowEmptySelectionProperty();
    }

    /// Returns the selected menu items in child order.
    ///
    /// @return an unmodifiable observable view of selected menu items
    public final @UnmodifiableView ObservableList<M3MenuItem> getSelectedItems() {
        return menu.getSelectedItems();
    }

    /// Returns the first selected menu item in child order.
    ///
    /// @return the first selected menu item, or `null` when selection is empty
    public final @Nullable M3MenuItem getSelectedItem() {
        return menu.getSelectedItem();
    }

    /// Returns the first selected menu item property.
    ///
    /// @return the read-only first selected menu item property
    public final ReadOnlyObjectProperty<@Nullable M3MenuItem> selectedItemProperty() {
        return menu.selectedItemProperty();
    }

    /// Returns the child index of the first selected menu item, or `-1` when no item is selected.
    ///
    /// @return the child index of the first selected menu item, or `-1` when no item is selected
    public final int getSelectedIndex() {
        return menu.getSelectedIndex();
    }

    /// Selects a menu item that belongs to this button's menu.
    ///
    /// @param item the selectable menu item to select
    public final void select(M3MenuItem item) {
        menu.select(item);
    }

    /// Selects the menu item at the given child index.
    ///
    /// @param index the child index of the selectable menu item
    public final void selectIndex(int index) {
        menu.selectIndex(index);
    }

    /// Selects the first menu item in this button's menu when one exists.
    public final void selectFirst() {
        menu.selectFirst();
    }

    /// Selects the last menu item in this button's menu when one exists.
    public final void selectLast() {
        menu.selectLast();
    }

    /// Selects the next menu item after the current selected item, wrapping at the end.
    public final void selectNext() {
        menu.selectNext();
    }

    /// Selects the previous menu item before the current selected item, wrapping at the start.
    public final void selectPrevious() {
        menu.selectPrevious();
    }

    /// Clears this button's menu selection when empty selection is allowed.
    public final void clearSelection() {
        menu.clearSelection();
    }

    /// Returns whether the menu popup is currently showing.
    ///
    /// @return `true` when the menu popup is showing
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only showing state property.
    ///
    /// @return the read-only showing state property
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Adds a listener that runs when this button's popup-accessible focus node changes.
    ///
    /// @param listener the listener to add
    final void addPopupFocusNodeListener(Runnable listener) {
        Objects.requireNonNull(listener, "listener");
        if (!popupFocusNodeListeners.contains(listener)) {
            popupFocusNodeListeners.add(listener);
        }
    }

    /// Removes a popup-accessible focus-node listener.
    ///
    /// @param listener the listener to remove
    final void removePopupFocusNodeListener(Runnable listener) {
        popupFocusNodeListeners.remove(Objects.requireNonNull(listener, "listener"));
    }

    /// Shows the menu popup below this button.
    public final void showMenu() {
        if (isDisabled() || popup.isShowing()) {
            return;
        }

        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null) {
            return;
        }

        prepareMenuForPopup(scene);
        menu.setMinWidth(Math.max(getWidth(), menu.minWidth(-1.0)));
        @Nullable M3PopupPositioning.Placement placement =
                M3PopupPositioning.menuBelowOrAbove(this, menu, MENU_OFFSET_Y);
        if (placement == null) {
            return;
        }
        prepareMenuForShowAnimation();
        popup.show(this, placement.x(), placement.y());
        showing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyPopupFocusNodeChanged();
        playShowAnimation();
    }

    /// Hides the menu popup.
    public final void hideMenu() {
        hideMenu(false);
    }

    /// Hides the menu popup and optionally returns focus to the owner button.
    private void hideMenu(boolean focusOwner) {
        menu.hideSubMenusExcept(null);
        if (!popup.isShowing()) {
            return;
        }

        focusOwnerOnHidden = focusOwner;
        showAnimation.stop();
        if (hideAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hideAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                event -> popup.hide(),
                new KeyValue(menu.opacityProperty(), 0.0, spec.interpolator()),
                new KeyValue(menu.scaleXProperty(), MENU_TRANSITION_SCALE, spec.interpolator()),
                new KeyValue(menu.scaleYProperty(), MENU_TRANSITION_SCALE, spec.interpolator()),
                new KeyValue(menu.translateYProperty(), MENU_TRANSITION_OFFSET_Y, spec.interpolator())
        ));
        M3Animation.playFromStart(this, hideAnimation);
    }

    /// Toggles the menu popup when the button fires.
    @Override
    public void fire() {
        if (isDisabled()) {
            return;
        }

        if (popup.isShowing()) {
            hideMenu();
        } else {
            showMenu();
        }
        super.fire();
    }

    /// Returns accessibility attributes for the menu popup.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case SUBMENU -> menu;
            case FOCUS_NODE -> focusNode();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> getSelectionMode() == M3MenuSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> getSelectedItems();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes menu-related accessibility actions.
    ///
    /// @param action the accessibility action to execute
    /// @param parameters optional action-specific parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SHOW_MENU, EXPAND -> showMenu();
            case COLLAPSE -> hideMenu(true);
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SET_SELECTED_ITEMS -> menu.executeAccessibleAction(action, parameters);
            case SHOW_ITEM -> {
                showMenu();
                menu.executeAccessibleAction(action, parameters);
                notifyPopupFocusNodeChanged();
            }
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and configures popup behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.MENU_BUTTON);
        popup.setAutoHide(true);
        popup.getContent().add(menu);
        popup.setOnHidden(event -> {
            menu.hideSubMenusExcept(null);
            showing.set(false);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyPopupFocusNodeChanged();
            resetMenuAnimationState();
            if (focusOwnerOnHidden) {
                focusOwnerOnHidden = false;
                requestFocus();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        menu.addEventHandler(KeyEvent.KEY_PRESSED, this::handleMenuKeyPressed);
        menu.addEventHandler(javafx.event.ActionEvent.ACTION, event -> hideMenu(true));
        menu.addAccessibleFocusNodeListener(this::notifyPopupFocusNodeChanged);
        popupFocusNotifier.start();
    }

    /// Returns the current popup focus node for accessibility clients.
    private Node focusNode() {
        if (!isShowing()) {
            return this;
        }
        @Nullable Object focusNode = menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node ? node : this;
    }

    /// Requests focus for this button or the currently reachable popup menu focus node.
    private void focusAccessibleNode() {
        if (!isShowing()) {
            requestFocus();
            notifyPopupFocusNodeChanged();
            return;
        }

        @Nullable Object focusNode = menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && node != this) {
            M3Accessible.showItem(node);
            notifyPopupFocusNodeChanged();
            return;
        }

        menu.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
        @Nullable Object nextFocusNode = menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (!(nextFocusNode instanceof Node node) || node == this) {
            requestFocus();
        }
        notifyPopupFocusNodeChanged();
    }

    /// Handles keyboard opening and dismissal for the popup menu.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case DOWN -> {
                if (showMenuAndFocusFirstItem()) {
                    event.consume();
                }
            }
            case UP -> {
                if (showMenuAndFocusLastItem()) {
                    event.consume();
                }
            }
            case ESCAPE -> {
                if (popup.isShowing()) {
                    hideMenu(true);
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Handles keyboard dismissal while focus is inside the popup menu.
    private void handleMenuKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (popup.isShowing()) {
                    hideMenu(true);
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Shows the popup menu and focuses the first enabled visible menu item.
    private boolean showMenuAndFocusFirstItem() {
        boolean showingBefore = popup.isShowing();
        showMenu();
        if (!popup.isShowing() && !showingBefore) {
            return false;
        }

        boolean focused = menu.focusFirstItem();
        notifyPopupFocusNodeChanged();
        return focused || popup.isShowing();
    }

    /// Shows the popup menu and focuses the last enabled visible menu item.
    private boolean showMenuAndFocusLastItem() {
        boolean showingBefore = popup.isShowing();
        showMenu();
        if (!popup.isShowing() && !showingBefore) {
            return false;
        }

        boolean focused = menu.focusLastItem();
        notifyPopupFocusNodeChanged();
        return focused || popup.isShowing();
    }

    /// Notifies clients and composite owners that the current popup-accessible focus node changed.
    private void notifyPopupFocusNodeChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        popupFocusNotifier.refresh();
        for (Runnable listener : List.copyOf(popupFocusNodeListeners)) {
            listener.run();
        }
    }

    /// Applies initial visual state before the popup is shown.
    private void prepareMenuForShowAnimation() {
        hideAnimation.stop();
        menu.setOpacity(0.0);
        menu.setScaleX(MENU_TRANSITION_SCALE);
        menu.setScaleY(MENU_TRANSITION_SCALE);
        menu.setTranslateY(MENU_TRANSITION_OFFSET_Y);
    }

    /// Plays the popup menu enter animation.
    private void playShowAnimation() {
        showAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        showAnimation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(menu.opacityProperty(), 1.0, spec.interpolator()),
                new KeyValue(menu.scaleXProperty(), 1.0, spec.interpolator()),
                new KeyValue(menu.scaleYProperty(), 1.0, spec.interpolator()),
                new KeyValue(menu.translateYProperty(), 0.0, spec.interpolator())
        ));
        M3Animation.playFromStart(this, showAnimation);
    }

    /// Resets transient popup menu animation transforms.
    private void resetMenuAnimationState() {
        showAnimation.stop();
        hideAnimation.stop();
        menu.setOpacity(1.0);
        menu.setScaleX(1.0);
        menu.setScaleY(1.0);
        menu.setTranslateY(0.0);
    }

    /// Applies changed runtime motion settings to active popup menu animations.
    private void refreshMotionSettings() {
        if (popup.isShowing()) {
            M3Animation.copyResolvedMotionSettings(this, menu);
        }
        M3Animation.finishRunningAnimationsIfDisabled(this, showAnimation, hideAnimation);
    }

    /// Validates a menu item array.
    private static void validateItems(Node... items) {
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
    }

    /// Copies scene styles and theme declarations into the popup-hosted menu.
    private void prepareMenuForPopup(Scene scene) {
        menu.getStylesheets().setAll(scene.getStylesheets());
        String menuStylesheet = M3Stylesheets.controlStylesheet("menu.css");
        if (!menu.getStylesheets().contains(menuStylesheet)) {
            menu.getStylesheets().add(menuStylesheet);
        }

        @Nullable Parent themeRoot = M3ThemeResolver.findThemeRoot(this);
        if (themeRoot != null) {
            M3ThemeManager.copyThemeContext(themeRoot, menu);
        }
        M3Animation.copyResolvedMotionSettings(this, menu);
        menu.setNodeOrientation(getEffectiveNodeOrientation());
        menu.applyCss();
    }
}
