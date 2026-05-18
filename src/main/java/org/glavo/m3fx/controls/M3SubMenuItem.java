// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 menu item that opens a nested menu.
@NotNullByDefault
public class M3SubMenuItem extends M3MenuItem {
    /// The base style class for M3FX submenu items.
    public static final String STYLE_CLASS = "m3-sub-menu-item";

    /// The style class applied to the default submenu indicator.
    public static final String INDICATOR_STYLE_CLASS = "m3-sub-menu-indicator";

    /// The horizontal overlap used when a submenu opens beside its owner item.
    private static final double SUB_MENU_OFFSET_X = -1.0;

    /// The duration used when the submenu popup enters.
    private static final Duration SUB_MENU_SHOW_DURATION = M3Motion.SHORT3;

    /// The duration used when the submenu popup exits.
    private static final Duration SUB_MENU_HIDE_DURATION = M3Motion.SHORT2;

    /// The initial popup menu scale used for enter and exit motion.
    private static final double SUB_MENU_TRANSITION_SCALE = 0.96;

    /// The initial horizontal popup menu offset used for enter and exit motion.
    private static final double SUB_MENU_TRANSITION_OFFSET_X = -6.0;

    /// The submenu displayed by this item.
    private final M3Menu subMenu = new M3Menu();

    /// The popup window used to host the submenu.
    private final Popup popup = new Popup();

    /// Whether this submenu item popup is currently showing.
    private final ReadOnlyBooleanWrapper subMenuShowing = new ReadOnlyBooleanWrapper(this, "subMenuShowing");

    /// The submenu popup enter animation.
    private final Timeline showAnimation = new Timeline();

    /// The submenu popup exit animation.
    private final Timeline hideAnimation = new Timeline();

    /// Whether an action from the submenu is being forwarded to this item's parent menu.
    private boolean forwardingSubMenuAction = false;

    /// Creates an empty submenu item.
    public M3SubMenuItem() {
        this("");
    }

    /// Creates a submenu item with text.
    public M3SubMenuItem(String text) {
        super(text);
        initialize();
    }

    /// Creates a submenu item with text and submenu content.
    public M3SubMenuItem(String text, Node... items) {
        this(text);
        addItems(items);
    }

    /// Returns the submenu displayed by this item.
    public final M3Menu getSubMenu() {
        return subMenu;
    }

    /// Returns the mutable item list shown by this item's submenu.
    public final ObservableList<Node> getItems() {
        return subMenu.getItems();
    }

    /// Adds one submenu item node.
    public final void addItem(Node item) {
        getItems().add(Objects.requireNonNull(item, "item"));
    }

    /// Adds submenu item nodes.
    public final void addItems(Node... items) {
        validateItems(items);
        getItems().addAll(items);
    }

    /// Replaces all submenu item nodes.
    public final void setItems(Node... items) {
        validateItems(items);
        getItems().setAll(items);
    }

    /// Removes all submenu item nodes.
    public final void clearItems() {
        getItems().clear();
    }

    /// Returns whether the submenu popup is currently showing.
    public final boolean isSubMenuShowing() {
        return subMenuShowing.get();
    }

    /// Returns the read-only submenu showing state property.
    public final ReadOnlyBooleanProperty subMenuShowingProperty() {
        return subMenuShowing.getReadOnlyProperty();
    }

    /// Shows the submenu popup beside this item.
    public final void showSubMenu() {
        if (isDisabled() || popup.isShowing()) {
            return;
        }

        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null) {
            return;
        }

        hideSiblingSubMenus();
        prepareSubMenuForPopup(scene);
        Bounds bounds = localToScreen(getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        subMenu.setMinWidth(Math.max(getWidth(), subMenu.minWidth(-1.0)));
        prepareSubMenuForShowAnimation();
        popup.show(this, bounds.getMaxX() + SUB_MENU_OFFSET_X, bounds.getMinY());
        subMenuShowing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        playShowAnimation();
    }

    /// Hides the submenu popup.
    public final void hideSubMenu() {
        subMenu.hideSubMenusExcept(null);
        if (!popup.isShowing()) {
            return;
        }

        showAnimation.stop();
        if (hideAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        hideAnimation.getKeyFrames().setAll(new KeyFrame(
                SUB_MENU_HIDE_DURATION,
                event -> popup.hide(),
                new KeyValue(subMenu.opacityProperty(), 0.0, M3Motion.STANDARD_ACCELERATE),
                new KeyValue(subMenu.scaleXProperty(), SUB_MENU_TRANSITION_SCALE, M3Motion.STANDARD_ACCELERATE),
                new KeyValue(subMenu.scaleYProperty(), SUB_MENU_TRANSITION_SCALE, M3Motion.STANDARD_ACCELERATE),
                new KeyValue(subMenu.translateXProperty(), SUB_MENU_TRANSITION_OFFSET_X, M3Motion.STANDARD_ACCELERATE)
        ));
        hideAnimation.playFromStart();
    }

    /// Returns accessibility attributes for submenu content and expanded state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isSubMenuShowing();
            case SUBMENU -> subMenu;
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case MULTIPLE_SELECTION -> subMenu.getSelectionMode() == M3MenuSelectionMode.MULTIPLE;
            case SELECTED_ITEMS -> subMenu.getSelectedItems();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes submenu-related accessibility actions.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SHOW_MENU, EXPAND -> showSubMenu();
            case COLLAPSE -> hideSubMenu();
            case SET_SELECTED_ITEMS -> subMenu.executeAccessibleAction(action, parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes and configures submenu popup behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        if (getTrailing() == null) {
            setTrailing(createDefaultIndicator());
        }
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
        popup.setAutoHide(true);
        popup.getContent().add(subMenu);
        popup.setOnHidden(event -> {
            subMenuShowing.set(false);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            resetSubMenuAnimationState();
        });
        addEventFilter(ActionEvent.ACTION, this::handleOwnActionEvent);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        subMenu.addEventHandler(KeyEvent.KEY_PRESSED, this::handleSubMenuKeyPressed);
        subMenu.addEventHandler(ActionEvent.ACTION, this::handleSubMenuAction);
    }

    /// Handles this item's own action event by opening the submenu.
    private void handleOwnActionEvent(ActionEvent event) {
        if (forwardingSubMenuAction) {
            return;
        }
        showSubMenu();
        event.consume();
    }

    /// Handles keyboard actions on the submenu item.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case RIGHT, ENTER, SPACE -> {
                if (showSubMenuAndFocusFirstItem()) {
                    event.consume();
                }
            }
            case LEFT, ESCAPE -> {
                if (popup.isShowing()) {
                    hideSubMenu();
                    requestFocus();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Handles keyboard dismissal while focus is inside the submenu.
    private void handleSubMenuKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT, ESCAPE -> {
                if (popup.isShowing()) {
                    hideSubMenu();
                    requestFocus();
                    event.consume();
                }
            }
            default -> {
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

    /// Shows the submenu and focuses its first enabled visible item.
    private boolean showSubMenuAndFocusFirstItem() {
        boolean showingBefore = popup.isShowing();
        showSubMenu();
        if (!popup.isShowing() && !showingBefore) {
            return false;
        }

        @Nullable M3MenuItem firstItem = M3SelectionNavigation.first(subMenu.getItems(), M3MenuItem.class);
        if (firstItem != null) {
            firstItem.requestFocus();
        }
        return true;
    }

    /// Applies initial visual state before the submenu is shown.
    private void prepareSubMenuForShowAnimation() {
        hideAnimation.stop();
        subMenu.setOpacity(0.0);
        subMenu.setScaleX(SUB_MENU_TRANSITION_SCALE);
        subMenu.setScaleY(SUB_MENU_TRANSITION_SCALE);
        subMenu.setTranslateX(SUB_MENU_TRANSITION_OFFSET_X);
    }

    /// Plays the submenu popup enter animation.
    private void playShowAnimation() {
        showAnimation.stop();
        showAnimation.getKeyFrames().setAll(new KeyFrame(
                SUB_MENU_SHOW_DURATION,
                new KeyValue(subMenu.opacityProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(subMenu.scaleXProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(subMenu.scaleYProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(subMenu.translateXProperty(), 0.0, M3Motion.STANDARD_DECELERATE)
        ));
        showAnimation.playFromStart();
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

    /// Copies scene styles and theme declarations into the popup-hosted submenu.
    private void prepareSubMenuForPopup(Scene scene) {
        subMenu.getStylesheets().setAll(scene.getStylesheets());
        String menuStylesheet = M3Stylesheets.controlStylesheet("menu.css");
        if (!subMenu.getStylesheets().contains(menuStylesheet)) {
            subMenu.getStylesheets().add(menuStylesheet);
        }

        Parent root = scene.getRoot();
        @Nullable String rootStyle = root == null ? null : root.getStyle();
        subMenu.setStyle(rootStyle == null ? "" : rootStyle);
        subMenu.applyCss();
    }

    /// Hides sibling submenu popups owned by the same parent menu.
    private void hideSiblingSubMenus() {
        if (getParent() instanceof M3Menu parentMenu) {
            parentMenu.hideSubMenusExcept(this);
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
