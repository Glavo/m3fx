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
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// A Material Design 3 menu button backed by an m3fx menu popup.
@NotNullByDefault
public class M3MenuButton extends M3Button {
    /// The base style class for m3fx menu buttons.
    public static final String STYLE_CLASS = "m3-menu-button";

    /// The vertical gap between the button and popup menu.
    private static final double MENU_OFFSET_Y = 4.0;

    /// The duration used when the menu popup enters.
    private static final Duration MENU_SHOW_DURATION = M3Motion.SHORT3;

    /// The duration used when the menu popup exits.
    private static final Duration MENU_HIDE_DURATION = M3Motion.SHORT2;

    /// The initial popup menu scale used for enter and exit motion.
    private static final double MENU_TRANSITION_SCALE = 0.96;

    /// The initial vertical popup menu offset used for enter and exit motion.
    private static final double MENU_TRANSITION_OFFSET_Y = -6.0;

    /// The menu displayed by this button.
    private final M3Menu menu = new M3Menu();

    /// The popup window used to host the menu.
    private final Popup popup = new Popup();

    /// Whether this menu button popup is currently showing.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The menu popup enter animation.
    private final Timeline showAnimation = new Timeline();

    /// The menu popup exit animation.
    private final Timeline hideAnimation = new Timeline();

    /// Creates an empty menu button.
    public M3MenuButton() {
        this("");
    }

    /// Creates a menu button with text.
    public M3MenuButton(String text) {
        super(text);
        initialize();
    }

    /// Creates a menu button with text and menu items.
    public M3MenuButton(String text, Node... items) {
        this(text);
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
        getItems().addAll(items);
    }

    /// Returns the menu displayed by this button.
    public final M3Menu getMenu() {
        return menu;
    }

    /// Returns the mutable item list shown by this button's menu.
    public final ObservableList<Node> getItems() {
        return menu.getItems();
    }

    /// Returns the menu item selection mode used by this button's menu.
    public final M3MenuSelectionMode getSelectionMode() {
        return menu.getSelectionMode();
    }

    /// Sets the menu item selection mode used by this button's menu.
    public final void setSelectionMode(M3MenuSelectionMode selectionMode) {
        menu.setSelectionMode(selectionMode);
    }

    /// Returns the menu item selection mode property.
    public final ObjectProperty<M3MenuSelectionMode> selectionModeProperty() {
        return menu.selectionModeProperty();
    }

    /// Returns whether this button's menu allows all selectable items to be unselected.
    public final boolean isAllowEmptySelection() {
        return menu.isAllowEmptySelection();
    }

    /// Sets whether this button's menu allows all selectable items to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        menu.setAllowEmptySelection(allowEmptySelection);
    }

    /// Returns the empty-selection policy property for this button's menu.
    public final BooleanProperty allowEmptySelectionProperty() {
        return menu.allowEmptySelectionProperty();
    }

    /// Returns the selected menu items in child order.
    public final @UnmodifiableView ObservableList<M3MenuItem> getSelectedItems() {
        return menu.getSelectedItems();
    }

    /// Returns the first selected menu item in child order.
    public final @Nullable M3MenuItem getSelectedItem() {
        return menu.getSelectedItem();
    }

    /// Returns the first selected menu item property.
    public final ReadOnlyObjectProperty<@Nullable M3MenuItem> selectedItemProperty() {
        return menu.selectedItemProperty();
    }

    /// Returns the child index of the first selected menu item, or `-1` when no item is selected.
    public final int getSelectedIndex() {
        return menu.getSelectedIndex();
    }

    /// Selects a menu item that belongs to this button's menu.
    public final void select(M3MenuItem item) {
        menu.select(item);
    }

    /// Selects the menu item at the given child index.
    public final void selectIndex(int index) {
        menu.selectIndex(index);
    }

    /// Selects the first menu item in this button's menu when one exists.
    public final void selectFirst() {
        menu.selectFirst();
    }

    /// Clears this button's menu selection when empty selection is allowed.
    public final void clearSelection() {
        menu.clearSelection();
    }

    /// Returns whether the menu popup is currently showing.
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only showing state property.
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
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
        Bounds bounds = localToScreen(getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        menu.setMinWidth(Math.max(getWidth(), menu.minWidth(-1.0)));
        prepareMenuForShowAnimation();
        popup.show(this, bounds.getMinX(), bounds.getMaxY() + MENU_OFFSET_Y);
        showing.set(true);
        playShowAnimation();
    }

    /// Hides the menu popup.
    public final void hideMenu() {
        if (!popup.isShowing()) {
            return;
        }

        showAnimation.stop();
        if (hideAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        hideAnimation.getKeyFrames().setAll(new KeyFrame(
                MENU_HIDE_DURATION,
                event -> popup.hide(),
                new KeyValue(menu.opacityProperty(), 0.0, M3Motion.STANDARD_ACCELERATE),
                new KeyValue(menu.scaleXProperty(), MENU_TRANSITION_SCALE, M3Motion.STANDARD_ACCELERATE),
                new KeyValue(menu.scaleYProperty(), MENU_TRANSITION_SCALE, M3Motion.STANDARD_ACCELERATE),
                new KeyValue(menu.translateYProperty(), MENU_TRANSITION_OFFSET_Y, M3Motion.STANDARD_ACCELERATE)
        ));
        hideAnimation.playFromStart();
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

    /// Adds base style classes and configures popup behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        popup.setAutoHide(true);
        popup.getContent().add(menu);
        popup.setOnHidden(event -> {
            showing.set(false);
            resetMenuAnimationState();
        });
        menu.addEventHandler(javafx.event.ActionEvent.ACTION, event -> hideMenu());
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
        showAnimation.getKeyFrames().setAll(new KeyFrame(
                MENU_SHOW_DURATION,
                new KeyValue(menu.opacityProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(menu.scaleXProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(menu.scaleYProperty(), 1.0, M3Motion.STANDARD_DECELERATE),
                new KeyValue(menu.translateYProperty(), 0.0, M3Motion.STANDARD_DECELERATE)
        ));
        showAnimation.playFromStart();
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

    /// Copies scene styles and theme declarations into the popup-hosted menu.
    private void prepareMenuForPopup(Scene scene) {
        menu.getStylesheets().setAll(scene.getStylesheets());
        String menuStylesheet = M3Stylesheets.controlStylesheet("menu.css");
        if (!menu.getStylesheets().contains(menuStylesheet)) {
            menu.getStylesheets().add(menuStylesheet);
        }

        Parent root = scene.getRoot();
        @Nullable String rootStyle = root == null ? null : root.getStyle();
        menu.setStyle(rootStyle == null ? "" : rootStyle);
        menu.applyCss();
    }
}
