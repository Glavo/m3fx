// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SplitButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// A Material Design 3 split button with a primary action and an attached menu action.
///
/// `M3SplitButton` combines an [M3Button] for the primary command with an [M3MenuButton] that reveals related
/// secondary commands. The control keeps both parts visually joined, forwards the configured button variant, and
/// exposes the menu items through the embedded menu button.
///
/// See [Material Design buttons](https://m3.material.io/components/buttons/overview).
@NotNullByDefault
public class M3SplitButton extends Control {
    /// The base style class for M3FX split buttons.
    public static final String STYLE_CLASS = "m3-split-button";

    /// The style class applied to the primary action button.
    public static final String ACTION_BUTTON_STYLE_CLASS = "m3-split-button-action";

    /// The style class applied to the menu button.
    public static final String MENU_BUTTON_STYLE_CLASS = "m3-split-button-menu";

    /// The minimum width used for the menu side of the split button.
    private static final double DEFAULT_MENU_BUTTON_WIDTH = 48.0;

    /// The text used for the default menu indicator.
    private static final String MENU_INDICATOR_TEXT = "v";

    /// The primary action button.
    private final M3Button actionButton = new M3Button();

    /// The attached menu button.
    private final M3MenuButton menuButton = new M3MenuButton(MENU_INDICATOR_TEXT);

    /// The focusable button parts exposed to accessibility and keyboard navigation.
    private final ObservableList<Node> buttonParts = FXCollections.observableArrayList();

    // Backing property for the public shared button variant API.
    private final ObjectProperty<M3ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3ButtonVariant.TONAL) {
                /// Updates both child buttons when the variant changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3ButtonVariant.TONAL);
                        return;
                    }
                    updateVariant();
                }
            };

    /// Creates an empty split button.
    public M3SplitButton() {
        this("");
    }

    /// Creates a split button with primary action text.
    ///
    /// @param text the primary action text
    public M3SplitButton(String text) {
        initialize();
        setText(text);
    }

    /// Creates a split button with primary action text and menu items.
    ///
    /// @param text the primary action text
    /// @param items the menu item nodes shown by the menu side
    public M3SplitButton(String text, Node... items) {
        this(text);
        addItems(items);
    }

    /// Creates a split button with text, variant, and menu items.
    ///
    /// @param text the primary action text
    /// @param variant the button variant shared by both split button parts
    /// @param items the menu item nodes shown by the menu side
    /// @return the created split button
    public static M3SplitButton withVariant(String text, M3ButtonVariant variant, Node... items) {
        M3SplitButton button = new M3SplitButton(text, items);
        button.setVariant(variant);
        return button;
    }

    /// Creates a split button with text, variant, action handler, and menu items.
    ///
    /// @param text the primary action text
    /// @param variant the button variant shared by both split button parts
    /// @param onAction the primary action handler, or `null` for none
    /// @param items the menu item nodes shown by the menu side
    /// @return the created split button
    public static M3SplitButton withVariant(
            String text,
            M3ButtonVariant variant,
            @Nullable EventHandler<ActionEvent> onAction,
            Node... items
    ) {
        M3SplitButton button = withVariant(text, variant, items);
        button.setOnAction(onAction);
        return button;
    }

    /// Returns the primary action button.
    ///
    /// @return the primary action button
    public final M3Button getActionButton() {
        return actionButton;
    }

    /// Returns the attached menu button.
    ///
    /// @return the attached menu button
    public final M3MenuButton getMenuButton() {
        return menuButton;
    }

    /// Returns the primary action text.
    ///
    /// @return the primary action text
    public final String getText() {
        return actionButton.getText();
    }

    /// Sets the primary action text.
    ///
    /// @param text the primary action text
    public final void setText(String text) {
        actionButton.setText(Objects.requireNonNull(text, "text"));
    }

    /// Returns the primary action text property.
    ///
    /// @return the primary action text property
    public final StringProperty textProperty() {
        return actionButton.textProperty();
    }

    /// Returns the primary action graphic.
    ///
    /// @return the primary action graphic, or `null` if none is set
    public final @Nullable Node getGraphic() {
        return actionButton.getGraphic();
    }

    /// Sets the primary action graphic.
    ///
    /// @param graphic the primary action graphic, or `null` to clear it
    public final void setGraphic(@Nullable Node graphic) {
        actionButton.setGraphic(graphic);
    }

    /// Returns the primary action graphic property.
    ///
    /// @return the primary action graphic property
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return actionButton.graphicProperty();
    }

    /// Returns the primary action handler.
    ///
    /// @return the primary action handler, or `null` if none is set
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return actionButton.getOnAction();
    }

    /// Sets the primary action handler.
    ///
    /// @param onAction the primary action handler, or `null` to clear it
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        actionButton.setOnAction(onAction);
    }

    /// Returns the primary action handler property.
    ///
    /// @return the primary action handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return actionButton.onActionProperty();
    }

    /// Fires the primary action.
    public final void fire() {
        actionButton.fire();
    }

    /// Returns the button variant shared by both split button parts.
    ///
    /// @return the button variant shared by both split button parts
    public final M3ButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the button variant shared by both split button parts.
    ///
    /// @param variant the button variant shared by both split button parts
    public final void setVariant(M3ButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the shared button variant property.
    ///
    /// @return the shared button variant property
    public final ObjectProperty<M3ButtonVariant> variantProperty() {
        return variant;
    }

    /// Returns the menu displayed by the menu side.
    ///
    /// @return the menu displayed by the menu side
    public final M3Menu getMenu() {
        return menuButton.getMenu();
    }

    /// Returns the mutable item list shown by the menu side.
    ///
    /// @return the mutable item list shown by the menu side
    public final ObservableList<Node> getItems() {
        return menuButton.getItems();
    }

    /// Adds one menu item node.
    ///
    /// @param item the menu item node to add
    public final void addItem(Node item) {
        menuButton.addItem(item);
    }

    /// Adds menu item nodes.
    ///
    /// @param items the menu item nodes to add
    public final void addItems(Node... items) {
        menuButton.addItems(items);
    }

    /// Replaces all menu item nodes.
    ///
    /// @param items the replacement menu item nodes
    public final void setItems(Node... items) {
        menuButton.setItems(items);
    }

    /// Removes all menu item nodes.
    public final void clearItems() {
        menuButton.clearItems();
    }

    /// Returns the menu item selection mode.
    ///
    /// @return the menu item selection mode
    public final M3MenuSelectionMode getSelectionMode() {
        return menuButton.getSelectionMode();
    }

    /// Sets the menu item selection mode.
    ///
    /// @param selectionMode the menu item selection mode
    public final void setSelectionMode(M3MenuSelectionMode selectionMode) {
        menuButton.setSelectionMode(selectionMode);
    }

    /// Returns the menu item selection mode property.
    ///
    /// @return the menu item selection mode property
    public final ObjectProperty<M3MenuSelectionMode> selectionModeProperty() {
        return menuButton.selectionModeProperty();
    }

    /// Returns whether the menu allows all selectable items to be unselected.
    ///
    /// @return `true` if the menu allows all selectable items to be unselected
    public final boolean isAllowEmptySelection() {
        return menuButton.isAllowEmptySelection();
    }

    /// Sets whether the menu allows all selectable items to be unselected.
    ///
    /// @param allowEmptySelection whether the menu allows all selectable items to be unselected
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        menuButton.setAllowEmptySelection(allowEmptySelection);
    }

    /// Returns the empty-selection policy property for the menu.
    ///
    /// @return the empty-selection policy property for the menu
    public final BooleanProperty allowEmptySelectionProperty() {
        return menuButton.allowEmptySelectionProperty();
    }

    /// Returns the selected menu items in child order.
    ///
    /// @return the selected menu items in child order
    public final @UnmodifiableView ObservableList<M3MenuItem> getSelectedItems() {
        return menuButton.getSelectedItems();
    }

    /// Returns the first selected menu item in child order.
    ///
    /// @return the first selected menu item in child order, or `null` when selection is empty
    public final @Nullable M3MenuItem getSelectedItem() {
        return menuButton.getSelectedItem();
    }

    /// Returns the first selected menu item property.
    ///
    /// @return the read-only first selected menu item property
    public final ReadOnlyObjectProperty<@Nullable M3MenuItem> selectedItemProperty() {
        return menuButton.selectedItemProperty();
    }

    /// Returns the child index of the first selected menu item, or `-1` when no item is selected.
    ///
    /// @return the child index of the first selected menu item, or `-1` when no item is selected
    public final int getSelectedIndex() {
        return menuButton.getSelectedIndex();
    }

    /// Selects a menu item that belongs to this split button's menu.
    ///
    /// @param item the menu item to select
    public final void select(M3MenuItem item) {
        menuButton.select(item);
    }

    /// Selects the menu item at the given child index.
    ///
    /// @param index the child index to select
    public final void selectIndex(int index) {
        menuButton.selectIndex(index);
    }

    /// Clears this split button's menu selection when empty selection is allowed.
    public final void clearSelection() {
        menuButton.clearSelection();
    }

    /// Shows the attached menu.
    public final void showMenu() {
        menuButton.showMenu();
    }

    /// Hides the attached menu.
    public final void hideMenu() {
        menuButton.hideMenu();
    }

    /// Returns whether the attached menu is currently showing.
    ///
    /// @return `true` if the attached menu is currently showing
    public final boolean isShowing() {
        return menuButton.isShowing();
    }

    /// Returns the read-only showing state property.
    ///
    /// @return the read-only showing state property
    public final ReadOnlyBooleanProperty showingProperty() {
        return menuButton.showingProperty();
    }

    /// Returns the user-agent stylesheet for M3FX split buttons.
    ///
    /// @return the split button user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("split-button.css");
    }

    /// Returns accessibility attributes for the split button and its attached menu.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case ITEM_COUNT -> buttonParts.size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(buttonParts, parameters);
            case FOCUS_NODE -> M3Accessible.firstFocusTarget(buttonParts);
            case SUBMENU -> getMenu();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for the primary action and attached menu.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> fire();
            case SHOW_MENU, EXPAND -> showMenu();
            case COLLAPSE -> hideMenu();
            case REQUEST_FOCUS -> M3Accessible.showItem(M3Accessible.firstFocusTarget(buttonParts));
            case SHOW_ITEM -> M3Accessible.showItem(buttonParts, parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes, child buttons, and popup state forwarding.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        M3ControlStyles.add(actionButton, ACTION_BUTTON_STYLE_CLASS);
        M3ControlStyles.add(menuButton, MENU_BUTTON_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        buttonParts.setAll(actionButton, menuButton);
        menuButton.setMinWidth(DEFAULT_MENU_BUTTON_WIDTH);
        menuButton.setPrefWidth(DEFAULT_MENU_BUTTON_WIDTH);
        menuButton.setHorizontalPadding(0.0);
        actionButton.addEventHandler(ActionEvent.ACTION, event -> hideMenu());
        menuButton.showingProperty().addListener((observable, oldValue, newValue) ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED));
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        updateVariant();
    }

    /// Applies keyboard focus navigation across the two split button parts.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeyFocus(
                event,
                buttonParts,
                M3SelectionNavigation.focused(buttonParts, M3Button.class),
                M3Button.class,
                true,
                false
        );
    }

    /// Applies the configured variant to both child buttons.
    private void updateVariant() {
        M3ButtonVariant currentVariant = getVariant();
        actionButton.setVariant(currentVariant);
        menuButton.setVariant(currentVariant);
    }

    /// Creates the default Material Design 3 split button skin.
    ///
    /// @return the default Material Design 3 split button skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SplitButtonSkin(this);
    }
}
