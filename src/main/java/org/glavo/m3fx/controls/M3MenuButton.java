// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

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

    /// The menu displayed by this button.
    private final M3Menu menu = new M3Menu();

    /// The popup window used to host the menu.
    private final Popup popup = new Popup();

    /// Whether this menu button popup is currently showing.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// Creates an empty menu button.
    public M3MenuButton() {
        this("");
    }

    /// Creates a menu button with text.
    public M3MenuButton(String text) {
        super(text);
        initialize();
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

    /// Selects a menu item that belongs to this button's menu.
    public final void select(M3MenuItem item) {
        menu.select(item);
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
        popup.show(this, bounds.getMinX(), bounds.getMaxY() + MENU_OFFSET_Y);
        showing.set(true);
    }

    /// Hides the menu popup.
    public final void hideMenu() {
        popup.hide();
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
        popup.setOnHidden(event -> showing.set(false));
        menu.addEventHandler(javafx.event.ActionEvent.ACTION, event -> hideMenu());
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
