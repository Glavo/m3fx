// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 menu button backed by an M3FX menu popup.
///
/// `M3MenuButton` owns one [M3Menu] and presents it in an auto-hiding popup associated with the button's window.
/// [#getMenu()] always returns the same menu instance; [#getItems()] is a convenience view of that menu's live content
/// list. Firing an enabled button toggles the popup and also delivers the button's action event.
///
/// [#showMenu()] is non-blocking and has no effect until the button belongs to a showing window or when the popup is
/// already visible. The popup hides when an item fires, the user dismisses it, the owner becomes unreachable, or
/// [#hideMenu()] is called. Keyboard dismissal and item activation return focus to the button; a direct [#hideMenu()]
/// call does not request focus.
///
/// ```java
/// M3MenuButton menuButton = new M3MenuButton("File");
/// M3MenuItem closeItem = new M3MenuItem("Close");
/// closeItem.setOnAction(event -> System.out.println("Close"));
/// menuButton.getItems().add(closeItem);
/// ```
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public final class M3MenuButton extends M3ButtonBase {
    /// The default style class for this control.
    private static final String DEFAULT_STYLE_CLASS = "m3-menu-button";

    /// The showing pseudo-class used while the owned menu popup is visible.
    private static final PseudoClass SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("showing");

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

    /// Keeps the detached popup menu synchronized with the owner scene and theme context while visible.
    private final M3PopupContextSynchronizer popupContextSynchronizer =
            new M3PopupContextSynchronizer(this, menu, M3Stylesheets.controlStylesheet("menu.css"));

    /// The reusable menu popup enter and exit animation.
    private final M3NodeTransition popupAnimation = new M3NodeTransition(menu);

    /// Reports popup menu focus changes through this button's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, menu, this::focusNode, this::notifyPopupFocusNodeChanged);

    /// Closes the popup when this button or one of its ancestors becomes unreachable.
    private final M3ReachabilityObserver reachabilityObserver =
            new M3ReachabilityObserver(this, this::hidePopupIfOwnerUnreachable);

    /// The single composite owner notified when this button's reported focus node changes.
    private @Nullable Runnable popupFocusNodeListener;

    /// Whether focus should return to the owner button after the popup hides.
    private boolean focusOwnerOnHidden;

    /// Whether the reusable popup animation is currently closing the menu.
    private boolean hidingPopup;

    /// Creates a menu button with empty text and an empty owned menu.
    public M3MenuButton() {
        this("");
    }

    /// Creates a menu button with the specified text and an empty owned menu.
    ///
    /// @param text the button text
    public M3MenuButton(String text) {
        super(text);
        initialize();
    }

    /// Creates a menu button with text and menu items.
    ///
    /// @param text  the button text
    /// @param items the initial non-null menu content nodes
    /// @throws NullPointerException if `items` or any element of `items` is `null`
    public M3MenuButton(String text, Node... items) {
        this(text);
        getItems().addAll(items);
    }

    /// Whether this menu button popup is currently showing.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing") {
        /// Updates the showing pseudo-class used by owner-specific component styling.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SHOWING_PSEUDO_CLASS, get());
        }
    };

    /// Returns whether the menu popup is currently showing.
    ///
    /// @return `true` when the menu popup is showing
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only observable property that reports popup visibility.
    ///
    /// The property is `false` by default, becomes `true` after the popup is shown successfully, and returns to
    /// `false` when the popup's hidden notification is received.
    ///
    /// @return the read-only showing property
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Returns the menu owned and displayed by this button.
    ///
    /// @return the stable owned menu instance; never `null`
    public final M3Menu getMenu() {
        return menu;
    }

    /// Returns the live mutable content list of the owned menu.
    ///
    /// The returned object is [M3Menu#getItems] from the owned menu; its ordering, null, duplicate-node, observation,
    /// and node-ownership contracts apply unchanged.
    ///
    /// @return the owned menu's live mutable content list
    public final ObservableList<Node> getItems() {
        return menu.getItems();
    }

    /// Sets the composite owner callback for popup-accessible focus changes.
    ///
    /// @param listener the callback to invoke
    final void setPopupFocusNodeListener(Runnable listener) {
        popupFocusNodeListener = Objects.requireNonNull(listener, "listener");
    }

    /// Shows the owned menu in a popup positioned relative to this button.
    ///
    /// This method is non-blocking and idempotent while the popup is visible. It has no effect if the button is not
    /// reachable from a showing window or a popup position cannot be established.
    public final void showMenu() {
        if (!M3Accessible.canReach(this) || popup.isShowing() || !M3PopupWindows.canShow(this)) {
            return;
        }

        boolean popupShown = false;
        popupContextSynchronizer.start();
        try {
            M3Css.setMinWidthIfUnbound(menu, Math.max(getWidth(), menu.minWidth(-1.0)));
            @Nullable M3PopupPositioning.Placement placement =
                    M3PopupPositioning.menuBelowOrAbove(this, menu, MENU_OFFSET_Y);
            if (placement == null) {
                return;
            }
            prepareMenuForShowAnimation();
            menu.setAccessibleFocusNodeListener(this::notifyPopupFocusNodeChanged);
            if (!M3PopupWindows.show(popup, this, placement.x(), placement.y())) {
                return;
            }
            popupShown = true;
        } finally {
            if (!popupShown) {
                menu.setAccessibleFocusNodeListener(null);
                resetMenuAnimationState();
                popupContextSynchronizer.stop();
            }
        }
        popupFocusNotifier.start();
        reachabilityObserver.install();
        showing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyPopupFocusNodeChanged();
        playShowAnimation();
    }

    /// Begins hiding the menu popup if it is visible.
    ///
    /// This method is non-blocking and idempotent while the popup is hidden or already closing. It does not request
    /// focus for the owner button.
    public final void hideMenu() {
        hideMenu(false);
    }

    /// Hides the menu popup and optionally returns focus to the owner button.
    private void hideMenu(boolean focusOwner) {
        menu.hideSubMenusExcept(null);
        if (!popup.isShowing()) {
            return;
        }

        focusOwnerOnHidden |= focusOwner;
        if (hidingPopup && popupAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hidingPopup = true;
        popupAnimation.configure(
                spec,
                0.0,
                MENU_TRANSITION_SCALE,
                MENU_TRANSITION_SCALE,
                menu.getTranslateX(),
                MENU_TRANSITION_OFFSET_Y
        );
        M3Animation.playFromStart(this, popupAnimation);
    }

    /// Toggles the menu popup and fires this button's action event.
    ///
    /// A disabled button performs neither operation.
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
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case SUBMENU -> menu;
            case FOCUS_NODE -> focusNode();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> menu.getSelectionMode() == M3SelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> menu.getSelectedItems();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes menu-related accessibility actions.
    ///
    /// @param action     the accessibility action to execute
    /// @param parameters optional action-specific parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case SHOW_MENU, EXPAND -> showMenu();
            case COLLAPSE -> hideMenu(true);
            case REQUEST_FOCUS -> requestAccessibleFocus();
            case SET_SELECTED_ITEMS -> menu.executeAccessibleAction(action, parameters);
            case SHOW_ITEM -> showAccessibleMenuItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and configures popup behavior.
    private void initialize() {
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.MENU_BUTTON);
        M3Accessible.installAccessibleActionRoute(
                this,
                this::requestAccessibleFocus,
                this::showAccessibleMenuItem,
                this::canShowAccessibleMenuItem
        );
        popup.setAutoHide(true);
        popup.getContent().add(menu);
        popupAnimation.setOnFinished(event -> {
            if (hidingPopup) {
                popup.hide();
            }
        });
        popup.setOnHidden(event -> {
            menu.setAccessibleFocusNodeListener(null);
            popupFocusNotifier.stop();
            reachabilityObserver.uninstall();
            popupContextSynchronizer.stop();
            menu.hideSubMenusExcept(null);
            showing.set(false);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyPopupFocusNodeChanged();
            resetMenuAnimationState();
            boolean restoreFocus = focusOwnerOnHidden;
            focusOwnerOnHidden = false;
            if (restoreFocus && M3Accessible.canReach(this)) {
                M3Accessible.showDirectItem(this, this);
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        menu.addEventHandler(KeyEvent.KEY_PRESSED, this::handleMenuKeyPressed);
        menu.addEventHandler(javafx.event.ActionEvent.ACTION, event -> hideMenu(true));
    }

    /// Hides the popup if its owner button can no longer be reached from its scene.
    private void hidePopupIfOwnerUnreachable() {
        if (popup.isShowing() && !M3Accessible.canReach(this)) {
            hideMenu(false);
        }
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
    final boolean requestAccessibleFocus() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (!isShowing()) {
            if (M3Accessible.showDirectItem(this, this)) {
                notifyPopupFocusNodeChanged();
                return true;
            }
            return false;
        }

        @Nullable Object focusNode = menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && node != this) {
            if (M3Accessible.showItem(this, node)) {
                notifyPopupFocusNodeChanged();
                return true;
            }
            return false;
        }

        if (menu.requestAccessibleFocus()) {
            notifyPopupFocusNodeChanged();
            return true;
        }

        if (M3Accessible.showDirectItem(this, this)) {
            notifyPopupFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns whether this menu button can reveal the supplied menu target without opening the popup.
    final boolean canShowAccessibleMenuItem(@Nullable Object parameter) {
        return parameter != null && !isDisabled() && menu.canShowAccessibleItem(parameter);
    }

    /// Opens the popup menu and focuses the descendant supplied by accessibility parameters.
    final boolean showAccessibleMenuItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (!M3Accessible.canReach(this) || !menu.canShowAccessibleItem(parameters)) {
            return false;
        }
        showMenu();
        if (!popup.isShowing()) {
            return false;
        }
        if (menu.showAccessibleItem(parameters)) {
            notifyPopupFocusNodeChanged();
            return true;
        }
        return false;
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
        if (event.getCode() == KeyCode.ESCAPE && popup.isShowing()) {
            hideMenu(true);
            event.consume();
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
        M3Accessible.notifyFocusNodeChanged(this);
        popupFocusNotifier.refresh();
        @Nullable Runnable listener = popupFocusNodeListener;
        if (listener != null) {
            listener.run();
        }
    }

    /// Applies initial visual state before the popup is shown.
    private void prepareMenuForShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        menu.setOpacity(0.0);
        menu.setScaleX(MENU_TRANSITION_SCALE);
        menu.setScaleY(MENU_TRANSITION_SCALE);
        menu.setTranslateY(MENU_TRANSITION_OFFSET_Y);
    }

    /// Plays the popup menu enter animation.
    private void playShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        popupAnimation.configure(spec, 1.0, 1.0, 1.0, menu.getTranslateX(), 0.0);
        M3Animation.playFromStart(this, popupAnimation);
    }

    /// Resets transient popup menu animation transforms.
    private void resetMenuAnimationState() {
        popupAnimation.stop();
        hidingPopup = false;
        menu.setOpacity(1.0);
        menu.setScaleX(1.0);
        menu.setScaleY(1.0);
        menu.setTranslateY(0.0);
    }

}
