// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

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
    private final M3InternalIcon defaultIndicator = createDefaultIndicator();

    /// The menu that directly owns this submenu item.
    private @Nullable M3Menu ownerMenu;

    /// The popup window used to host the submenu.
    private final Popup popup = new Popup();

    /// Keeps the detached submenu synchronized with its owner menu or owner scene context while visible.
    private final M3PopupContextSynchronizer popupContextSynchronizer =
            new M3PopupContextSynchronizer(
                    this,
                    subMenu,
                    this::popupStylesheetSource,
                    this::popupThemeSource,
                    M3Stylesheets.controlStylesheet("menu.css")
            );

    // Backing property for the public read-only submenu showing state API.
    private final ReadOnlyBooleanWrapper subMenuShowing = new ReadOnlyBooleanWrapper(this, "subMenuShowing");

    /// The reusable submenu popup enter and exit animation.
    private final M3NodeTransition popupAnimation = new M3NodeTransition(subMenu);

    /// Observes runtime motion settings while this item is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Reports popup submenu focus changes through this item's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, subMenu, this::focusNode, this::notifyFocusNodeChanged);

    /// Closes the popup when this item or one of its ancestors becomes unreachable.
    private final M3ReachabilityObserver reachabilityObserver =
            new M3ReachabilityObserver(this, this::hidePopupIfOwnerUnreachable);

    /// The pointer-hover open delay.
    private final PauseTransition hoverOpenDelay = new PauseTransition();

    /// The pointer-exit close delay.
    private final PauseTransition hoverCloseDelay = new PauseTransition();

    /// Updates the indicator glyph when node orientation changes.
    private final InvalidationListener nodeOrientationInvalidation = observable -> updateDefaultIndicatorDirection();

    /// Whether an action from the submenu is being forwarded to this item's parent menu.
    private boolean forwardingSubMenuAction = false;

    /// Whether focus should return to this item after the submenu popup hides.
    private boolean focusOwnerOnHidden = false;

    /// Whether the reusable popup animation is currently closing the submenu.
    private boolean hidingPopup;

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
        getItems().addAll(items);
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
        if (!M3Accessible.canReach(this) || !M3PopupWindows.canShow(this)) {
            return;
        }
        hoverOpenDelay.stop();
        hoverCloseDelay.stop();
        if (popup.isShowing()) {
            subMenuShowing.set(true);
            playShowAnimation();
            return;
        }

        if (ownerMenu != null) {
            ownerMenu.hideSubMenusExcept(this);
        }
        boolean popupShown = false;
        popupContextSynchronizer.start();
        try {
            M3Css.setMinWidthIfUnbound(subMenu, Math.max(getWidth(), subMenu.minWidth(-1.0)));
            @Nullable Bounds anchorBounds = subMenuAnchorBounds();
            if (anchorBounds == null) {
                return;
            }
            @Nullable M3PopupPositioning.Placement placement =
                    M3PopupPositioning.subMenuBeside(anchorBounds, subMenu, SUB_MENU_OFFSET_X, isRightToLeft());
            if (placement == null) {
                return;
            }
            currentTransitionOffsetX = placement.opensToLeft()
                    ? -SUB_MENU_TRANSITION_OFFSET_X
                    : SUB_MENU_TRANSITION_OFFSET_X;
            prepareSubMenuForShowAnimation();
            subMenu.setAccessibleFocusNodeListener(this::notifyFocusNodeChanged);
            if (!M3PopupWindows.show(popup, this, placement.x(), placement.y())) {
                return;
            }
            popupShown = true;
        } finally {
            if (!popupShown) {
                subMenu.setAccessibleFocusNodeListener(null);
                resetSubMenuAnimationState();
                popupContextSynchronizer.stop();
            }
        }
        popupFocusNotifier.start();
        reachabilityObserver.install();
        subMenuShowing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
        playShowAnimation();
    }

    /// Returns the screen bounds used to position the submenu popup.
    private @Nullable Bounds subMenuAnchorBounds() {
        @Nullable Bounds itemBounds = localToScreen(getBoundsInLocal());
        if (itemBounds == null) {
            return null;
        }
        if (ownerMenu == null) {
            return itemBounds;
        }

        @Nullable Bounds ownerMenuBounds = ownerMenu.localToScreen(ownerMenu.getBoundsInLocal());
        if (ownerMenuBounds == null) {
            return itemBounds;
        }
        return new BoundingBox(
                ownerMenuBounds.getMinX(),
                itemBounds.getMinY(),
                ownerMenuBounds.getWidth(),
                itemBounds.getHeight()
        );
    }

    /// Hides the submenu popup.
    public final void hideSubMenu() {
        hideSubMenu(false);
    }

    /// Hides the submenu popup and optionally returns focus to this item.
    private void hideSubMenu(boolean focusOwner) {
        hoverOpenDelay.stop();
        hoverCloseDelay.stop();
        subMenu.hideSubMenusExcept(null);
        focusOwnerOnHidden |= focusOwner;
        if (!popup.isShowing()) {
            if (focusOwner && M3Accessible.canReach(this)) {
                M3Accessible.showDirectItem(this, this);
            }
            focusOwnerOnHidden = false;
            return;
        }

        if (hidingPopup && popupAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hidingPopup = true;
        popupAnimation.configure(
                spec,
                0.0,
                SUB_MENU_TRANSITION_SCALE,
                SUB_MENU_TRANSITION_SCALE,
                currentTransitionOffsetX,
                subMenu.getTranslateY()
        );
        M3Animation.playFromStart(this, popupAnimation);
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
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case SHOW_MENU, EXPAND -> showSubMenu();
            case COLLAPSE -> hideSubMenu(true);
            case REQUEST_FOCUS -> requestAccessibleFocus();
            case SET_SELECTED_ITEMS -> subMenu.executeAccessibleAction(action, parameters);
            case SHOW_ITEM -> showAccessibleSubMenuItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and configures submenu popup behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        M3Accessible.installAccessibleActionRoute(this, this::requestAccessibleFocus, this::showAccessibleSubMenuItem);
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
        popupAnimation.setOnFinished(event -> {
            if (hidingPopup) {
                popup.hide();
            }
        });
        popup.setOnHidden(event -> {
            subMenu.setAccessibleFocusNodeListener(null);
            popupFocusNotifier.stop();
            reachabilityObserver.uninstall();
            popupContextSynchronizer.stop();
            pointerInsideOwner = false;
            pointerInsideSubMenu = false;
            subMenuShowing.set(false);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
            resetSubMenuAnimationState();
            boolean restoreFocus = focusOwnerOnHidden;
            focusOwnerOnHidden = false;
            if (restoreFocus && M3Accessible.canReach(this)) {
                M3Accessible.showDirectItem(this, this);
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
    }

    /// Hides the popup if its owner item can no longer be reached from its scene.
    private void hidePopupIfOwnerUnreachable() {
        if (popup.isShowing() && !M3Accessible.canReach(this)) {
            hideSubMenu(false);
        }
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
    final boolean requestAccessibleFocus() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (!isSubMenuShowing()) {
            if (M3Accessible.showDirectItem(this, this)) {
                notifyFocusNodeChanged();
                return true;
            }
            return false;
        }

        @Nullable Object focusNode = subMenu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && node != this) {
            if (M3Accessible.showItem(this, node)) {
                notifyFocusNodeChanged();
                return true;
            }
            return false;
        }

        if (subMenu.requestAccessibleFocus()) {
            notifyFocusNodeChanged();
            return true;
        }

        if (M3Accessible.showDirectItem(this, this)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Opens the submenu and focuses a descendant supplied by accessibility parameters.
    final boolean showAccessibleSubMenuItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (!M3Accessible.canReach(this) || !subMenu.canShowAccessibleItem(parameters)) {
            return false;
        }
        showSubMenu();
        if (!popup.isShowing()) {
            return false;
        }
        if (subMenu.showAccessibleItem(parameters)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
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
        if (code == KeyCode.ESCAPE) {
            if (popup.isShowing()) {
                hideSubMenu(true);
                event.consume();
            }
            return;
        }
        if (M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }

        if (code == KeyCode.ENTER || code == KeyCode.SPACE || isOpenSubMenuKey(code)) {
            if (showSubMenuAndFocusFirstItem()) {
                event.consume();
            }
        } else if (isCloseSubMenuKey(code)) {
            if (popup.isShowing()) {
                hideSubMenu(true);
                event.consume();
            }
        }
    }

    /// Handles keyboard dismissal while focus is inside the submenu.
    private void handleSubMenuKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.ESCAPE
                || (!M3KeyEvents.hasNavigationModifier(event) && isCloseSubMenuKey(code))) {
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
        M3Accessible.notifyFocusNodeChanged(this);
        popupFocusNotifier.refresh();
        @Nullable M3Menu currentOwner = ownerMenu;
        if (currentOwner != null) {
            currentOwner.notifyDescendantFocusNodeChanged();
        }
    }

    /// Applies initial visual state before the submenu is shown.
    private void prepareSubMenuForShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        subMenu.setOpacity(0.0);
        subMenu.setScaleX(SUB_MENU_TRANSITION_SCALE);
        subMenu.setScaleY(SUB_MENU_TRANSITION_SCALE);
        subMenu.setTranslateX(currentTransitionOffsetX);
    }

    /// Plays the submenu popup enter animation.
    private void playShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        popupAnimation.configure(spec, 1.0, 1.0, 1.0, 0.0, subMenu.getTranslateY());
        M3Animation.playFromStart(this, popupAnimation);
    }

    /// Resets transient submenu animation transforms.
    private void resetSubMenuAnimationState() {
        popupAnimation.stop();
        hidingPopup = false;
        subMenu.setOpacity(1.0);
        subMenu.setScaleX(1.0);
        subMenu.setScaleY(1.0);
        subMenu.setTranslateX(0.0);
    }

    /// Applies changed runtime motion settings to active submenu popup animations.
    private void refreshMotionSettings() {
        M3Animation.finishRunningAnimationsIfDisabled(this, popupAnimation);
        refreshHoverDelays();
    }

    /// Applies changed runtime motion settings to pointer-hover submenu delays.
    private void refreshHoverDelays() {
        M3Animation.updatePauseDuration(
                hoverOpenDelay,
                M3Animation.motionBehavior(this).subMenuHoverOpenDelay(),
                pointerInsideOwner && !popup.isShowing()
        );
        M3Animation.updatePauseDuration(
                hoverCloseDelay,
                M3Animation.motionBehavior(this).subMenuHoverCloseDelay(),
                popup.isShowing() && !pointerInsideOwner && !pointerInsideSubMenu
        );
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
        return M3NodeLayout.isRightToLeft(this);
    }

    /// Updates the default indicator to point toward the submenu opening side.
    private void updateDefaultIndicatorDirection() {
        defaultIndicator.setGlyph(isRightToLeft()
                ? M3InternalIcon.Glyph.CHEVRON_LEFT
                : M3InternalIcon.Glyph.CHEVRON_RIGHT);
    }

    /// Starts the pointer-exit close delay when the submenu is open.
    private void scheduleHoverClose() {
        if (popup.isShowing()) {
            hoverCloseDelay.setDuration(M3Animation.motionBehavior(this).subMenuHoverCloseDelay());
            hoverCloseDelay.playFromStart();
        }
    }

    /// Returns stylesheets from the owning popup menu when this item is already inside a popup branch.
    private @Nullable ObservableList<String> popupStylesheetSource() {
        if (ownerMenu != null && !ownerMenu.getStylesheets().isEmpty()) {
            return ownerMenu.getStylesheets();
        }
        @Nullable Scene scene = getScene();
        return scene == null ? null : scene.getStylesheets();
    }

    /// Returns the root that should supply looked-up theme tokens for the submenu popup.
    private @Nullable Parent popupThemeSource() {
        @Nullable Parent ownerMenuThemeRoot = ownerMenu == null ? null : M3ThemeResolver.findThemeRoot(ownerMenu);
        if (ownerMenuThemeRoot != null) {
            return ownerMenuThemeRoot;
        }
        @Nullable Parent itemThemeRoot = M3ThemeResolver.findThemeRoot(this);
        if (itemThemeRoot != null) {
            return itemThemeRoot;
        }
        @Nullable Scene scene = getScene();
        return scene == null ? null : scene.getRoot();
    }


    /// Creates the default trailing submenu indicator.
    private static M3InternalIcon createDefaultIndicator() {
        M3InternalIcon indicator = new M3InternalIcon(
                M3InternalIcon.Glyph.CHEVRON_RIGHT,
                M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
        );
        M3ControlStyles.add(indicator, INDICATOR_STYLE_CLASS);
        return indicator;
    }

}
