// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// A Material Design 3 split button with a primary action and an attached menu action.
@NotNullByDefault
public class M3SplitButton extends HBox {
    /// The base style class for M3FX split buttons.
    public static final String STYLE_CLASS = "m3-split-button";

    /// The style class applied to the primary action button.
    public static final String ACTION_BUTTON_STYLE_CLASS = "m3-split-button-action";

    /// The style class applied to the menu button.
    public static final String MENU_BUTTON_STYLE_CLASS = "m3-split-button-menu";

    /// The default spacing that lets adjacent button borders overlap.
    private static final double DEFAULT_SPACING = -1.0;

    /// The minimum width used for the menu side of the split button.
    private static final double DEFAULT_MENU_BUTTON_WIDTH = 48.0;

    /// The text used for the default menu indicator.
    private static final String MENU_INDICATOR_TEXT = "v";

    /// The primary action button.
    private final M3Button actionButton = new M3Button();

    /// The attached menu button.
    private final M3MenuButton menuButton = new M3MenuButton(MENU_INDICATOR_TEXT);

    /// The visual variant applied to both split button parts.
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
    public M3SplitButton(String text) {
        initialize();
        setText(text);
    }

    /// Creates a split button with primary action text and menu items.
    public M3SplitButton(String text, Node... items) {
        this(text);
        addItems(items);
    }

    /// Creates a split button with text, variant, and menu items.
    public static M3SplitButton withVariant(String text, M3ButtonVariant variant, Node... items) {
        M3SplitButton button = new M3SplitButton(text, items);
        button.setVariant(variant);
        return button;
    }

    /// Creates a split button with text, variant, action handler, and menu items.
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
    public final M3Button getActionButton() {
        return actionButton;
    }

    /// Returns the attached menu button.
    public final M3MenuButton getMenuButton() {
        return menuButton;
    }

    /// Returns the primary action text.
    public final String getText() {
        return actionButton.getText();
    }

    /// Sets the primary action text.
    public final void setText(String text) {
        actionButton.setText(Objects.requireNonNull(text, "text"));
    }

    /// Returns the primary action text property.
    public final StringProperty textProperty() {
        return actionButton.textProperty();
    }

    /// Returns the primary action graphic.
    public final @Nullable Node getGraphic() {
        return actionButton.getGraphic();
    }

    /// Sets the primary action graphic.
    public final void setGraphic(@Nullable Node graphic) {
        actionButton.setGraphic(graphic);
    }

    /// Returns the primary action graphic property.
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return actionButton.graphicProperty();
    }

    /// Returns the primary action handler.
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return actionButton.getOnAction();
    }

    /// Sets the primary action handler.
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        actionButton.setOnAction(onAction);
    }

    /// Returns the primary action handler property.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return actionButton.onActionProperty();
    }

    /// Fires the primary action.
    public final void fire() {
        actionButton.fire();
    }

    /// Returns the button variant shared by both split button parts.
    public final M3ButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the button variant shared by both split button parts.
    public final void setVariant(M3ButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the shared button variant property.
    public final ObjectProperty<M3ButtonVariant> variantProperty() {
        return variant;
    }

    /// Returns the menu displayed by the menu side.
    public final M3Menu getMenu() {
        return menuButton.getMenu();
    }

    /// Returns the mutable item list shown by the menu side.
    public final ObservableList<Node> getItems() {
        return menuButton.getItems();
    }

    /// Adds one menu item node.
    public final void addItem(Node item) {
        menuButton.addItem(item);
    }

    /// Adds menu item nodes.
    public final void addItems(Node... items) {
        menuButton.addItems(items);
    }

    /// Replaces all menu item nodes.
    public final void setItems(Node... items) {
        menuButton.setItems(items);
    }

    /// Removes all menu item nodes.
    public final void clearItems() {
        menuButton.clearItems();
    }

    /// Returns the menu item selection mode.
    public final M3MenuSelectionMode getSelectionMode() {
        return menuButton.getSelectionMode();
    }

    /// Sets the menu item selection mode.
    public final void setSelectionMode(M3MenuSelectionMode selectionMode) {
        menuButton.setSelectionMode(selectionMode);
    }

    /// Returns the menu item selection mode property.
    public final ObjectProperty<M3MenuSelectionMode> selectionModeProperty() {
        return menuButton.selectionModeProperty();
    }

    /// Returns whether the menu allows all selectable items to be unselected.
    public final boolean isAllowEmptySelection() {
        return menuButton.isAllowEmptySelection();
    }

    /// Sets whether the menu allows all selectable items to be unselected.
    public final void setAllowEmptySelection(boolean allowEmptySelection) {
        menuButton.setAllowEmptySelection(allowEmptySelection);
    }

    /// Returns the empty-selection policy property for the menu.
    public final BooleanProperty allowEmptySelectionProperty() {
        return menuButton.allowEmptySelectionProperty();
    }

    /// Returns the selected menu items in child order.
    public final @UnmodifiableView ObservableList<M3MenuItem> getSelectedItems() {
        return menuButton.getSelectedItems();
    }

    /// Returns the first selected menu item in child order.
    public final @Nullable M3MenuItem getSelectedItem() {
        return menuButton.getSelectedItem();
    }

    /// Returns the first selected menu item property.
    public final ReadOnlyObjectProperty<@Nullable M3MenuItem> selectedItemProperty() {
        return menuButton.selectedItemProperty();
    }

    /// Returns the child index of the first selected menu item, or `-1` when no item is selected.
    public final int getSelectedIndex() {
        return menuButton.getSelectedIndex();
    }

    /// Selects a menu item that belongs to this split button's menu.
    public final void select(M3MenuItem item) {
        menuButton.select(item);
    }

    /// Selects the menu item at the given child index.
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
    public final boolean isShowing() {
        return menuButton.isShowing();
    }

    /// Returns the read-only showing state property.
    public final ReadOnlyBooleanProperty showingProperty() {
        return menuButton.showingProperty();
    }

    /// Returns the user-agent stylesheet for M3FX split buttons.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("split-button.css");
    }

    /// Returns accessibility attributes for the split button and its attached menu.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case ITEM_COUNT -> getChildren().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getChildren(), parameters);
            case SUBMENU -> getMenu();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for the primary action and attached menu.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> fire();
            case SHOW_MENU, EXPAND -> showMenu();
            case COLLAPSE -> hideMenu();
            case SHOW_ITEM -> M3Accessible.showItem(getChildren(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes, child buttons, and popup state forwarding.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        M3ControlStyles.add(actionButton, ACTION_BUTTON_STYLE_CLASS);
        M3ControlStyles.add(menuButton, MENU_BUTTON_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setSpacing(DEFAULT_SPACING);
        menuButton.setMinWidth(DEFAULT_MENU_BUTTON_WIDTH);
        menuButton.setPrefWidth(DEFAULT_MENU_BUTTON_WIDTH);
        menuButton.setHorizontalPadding(0.0);
        actionButton.addEventHandler(ActionEvent.ACTION, event -> hideMenu());
        menuButton.showingProperty().addListener((observable, oldValue, newValue) ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED));
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getChildren().setAll(actionButton, menuButton);
        updateVariant();
    }

    /// Applies keyboard focus navigation across the two split button parts.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeyFocus(
                event,
                getChildren(),
                M3SelectionNavigation.focused(getChildren(), M3Button.class),
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
}
