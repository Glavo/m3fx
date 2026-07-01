// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import javafx.util.Callback;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.skins.M3ListViewCellSkin;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A reusable virtualized cell used by [M3ListView].
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
///
/// @param <T> the item type rendered by this cell
@NotNullByDefault
public class M3ListViewCell<T> extends IndexedCell<T> {
    /// The base style class for M3FX list view cells.
    public static final String STYLE_CLASS = "m3-list-view-cell";

    /// The property key that stores a row item style before copied theme declarations are applied.
    private static final String BASE_STYLE_PROPERTY_KEY = M3ListViewCell.class.getName() + ".baseStyle";

    /// The pseudo-class used when this virtualized row owns logical keyboard focus.
    private static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// The owning virtualized list view.
    private final M3ListView<T> listView;

    /// The rendered list item currently owned by this cell.
    private @Nullable M3ListItem listItem;

    /// Routes list item actions back into the list view selection policy.
    private final EventHandler<ActionEvent> itemActionHandler = this::handleItemAction;

    /// Creates a reusable list view cell.
    ///
    /// @param listView the owning virtualized list view
    public M3ListViewCell(M3ListView<T> listView) {
        this.listView = Objects.requireNonNull(listView, "listView");
        M3ControlStyles.add(this, STYLE_CLASS);
        M3PopupStyles.addFallbackRootStyleClass(this);
        M3PopupStyles.addStylesheet(this, M3Stylesheets.fallbackStylesheet());
        setAccessibleRole(AccessibleRole.LIST_ITEM);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setText(null);
    }

    /// Returns the owning virtualized list view.
    ///
    /// @return the owning virtualized list view
    public final M3ListView<T> getListView() {
        return listView;
    }

    /// Returns the rendered list item currently owned by this cell.
    ///
    /// @return the rendered list item, or `null` when this cell is empty
    public final @Nullable M3ListItem getListItem() {
        return listItem;
    }

    /// Updates this cell's index and selected state.
    ///
    /// @param index the virtualized row index
    @Override
    public void updateIndex(int index) {
        super.updateIndex(index);
        if (index < 0 || index >= getListView().getItems().size()) {
            updateItem(null, true);
        } else {
            updateItem(getListView().getItems().get(index), false);
        }
        refreshSelection();
    }

    /// Updates the rendered list item for the current virtualized item.
    ///
    /// @param item the data item assigned to this cell, or `null` for an empty cell
    /// @param empty whether this cell is empty
    @Override
    protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            clearContent();
            return;
        }
        if (getScene() == null || !isListViewSceneShowing()) {
            return;
        }

        M3ListItem itemNode = createListItem(item);
        setListItem(itemNode);
        setGraphic(itemNode);
        setText(null);
        refreshListItemWidth();
        refreshSelection();
    }

    /// Lays out the cell and keeps the rendered list item width in sync.
    @Override
    protected void layoutChildren() {
        refreshListItemWidth();
        super.layoutChildren();
    }

    /// Returns accessibility attributes for this virtualized cell.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> getListView().isIndexSelected(getIndex());
            case INDEX -> getIndex();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates the default skin that lays out this virtualized cell's rendered list item.
    ///
    /// @return the default virtualized cell skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ListViewCellSkin<>(this);
    }

    /// Creates or delegates creation of the visual list item for one data item.
    private M3ListItem createListItem(@Nullable T item) {
        @Nullable Callback<? super T, ? extends M3ListItem> factory = getListView().getCellFactory();
        M3ListItem itemNode = factory == null
                ? new M3ListItem(String.valueOf(item))
                : Objects.requireNonNull(factory.call(item), "cellFactory result");
        itemNode.setFocusTraversable(false);
        if (!itemNode.minWidthProperty().isBound() && Double.compare(itemNode.getMinWidth(), 0.0) != 0) {
            itemNode.setMinWidth(0.0);
        }
        if (!itemNode.maxWidthProperty().isBound()
                && Double.compare(itemNode.getMaxWidth(), Double.MAX_VALUE) != 0) {
            itemNode.setMaxWidth(Double.MAX_VALUE);
        }
        copyThemeContext(itemNode);
        return itemNode;
    }

    /// Copies the current list view theme context into a virtualized row for early CSS passes.
    private void copyThemeContext(M3ListItem itemNode) {
        preserveBaseStyle(itemNode);
        @Nullable Parent themeRoot = M3ThemeResolver.findThemeRoot(getListView());
        if (themeRoot != null) {
            M3ThemeManager.copyThemeContext(themeRoot, itemNode);
        } else {
            clearThemeContext(itemNode);
        }
    }

    /// Preserves the row item style that existed before copied theme declarations were applied.
    private static void preserveBaseStyle(M3ListItem itemNode) {
        if (!itemNode.getProperties().containsKey(BASE_STYLE_PROPERTY_KEY)) {
            itemNode.getProperties().put(BASE_STYLE_PROPERTY_KEY, itemNode.getStyle());
        }
    }

    /// Clears copied theme metadata and restores the row item base style.
    private static void clearThemeContext(M3ListItem itemNode) {
        M3ThemeManager.clearThemeStyleClasses(itemNode);
        itemNode.getProperties().remove(M3ThemeManager.THEME_PROPERTY_KEY);
        Object baseStyleValue = itemNode.getProperties().get(BASE_STYLE_PROPERTY_KEY);
        itemNode.setStyle(baseStyleValue instanceof String baseStyle ? baseStyle : "");
    }

    /// Returns whether the owning list view is in a scene whose window is still visible.
    private boolean isListViewSceneShowing() {
        @Nullable Scene scene = getListView().getScene();
        if (scene == null) {
            return false;
        }

        @Nullable Window window = scene.getWindow();
        return window == null || window.isShowing();
    }

    /// Replaces the rendered list item and updates event handlers.
    private void setListItem(@Nullable M3ListItem listItem) {
        if (this.listItem != null) {
            this.listItem.removeEventHandler(ActionEvent.ACTION, itemActionHandler);
        }
        this.listItem = listItem;
        if (listItem != null) {
            listItem.addEventHandler(ActionEvent.ACTION, itemActionHandler);
        }
    }

    /// Clears rendered content while the cell is empty or detached from the scene.
    private void clearContent() {
        setListItem(null);
        setGraphic(null);
        setText(null);
    }

    /// Updates the list item's selected state from the owning view.
    public final void refreshSelection() {
        boolean selected = !isEmpty() && getListView().isIndexSelected(getIndex());
        updateSelected(selected);
        if (listItem != null) {
            listItem.setSelected(selected);
        }
    }

    /// Reapplies the current list view theme context to the rendered row item.
    public final void refreshThemeContext() {
        if (listItem != null) {
            copyThemeContext(listItem);
            listItem.applyCss();
            listItem.layout();
        }
    }

    /// Requests list focus when this cell owns logical row focus.
    ///
    /// @return `true` when the owning list view accepted focus
    public final boolean focusCell() {
        if (listItem == null || isEmpty() || !getListView().isIndexFocused(getIndex())) {
            return false;
        }

        M3Accessible.showDirectItem(getListView(), getListView());
        refreshFocus();
        return getListView().isFocused();
    }

    /// Updates logical focus pseudo-class state for this virtualized row.
    public final void refreshFocus() {
        boolean focusVisible = !isEmpty()
                && getListView().isIndexFocused(getIndex())
                && getListView().getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS);
        pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
        if (listItem != null) {
            listItem.pseudoClassStateChanged(FOCUS_VISIBLE_PSEUDO_CLASS, focusVisible);
        }
    }

    /// Stretches the rendered list item to the current cell width.
    private void refreshListItemWidth() {
        if (listItem != null) {
            double width = getWidth();
            if (width > 0.0
                    && !listItem.prefWidthProperty().isBound()
                    && Double.compare(listItem.getPrefWidth(), width) != 0) {
                listItem.setPrefWidth(width);
            }
        }
    }

    /// Applies the list view selection policy when the rendered item fires.
    private void handleItemAction(ActionEvent event) {
        int index = getIndex();
        if (index >= 0 && index < getListView().getItems().size()) {
            getListView().focusIndex(index);
            getListView().activateIndex(index);
        }
        event.consume();
    }
}
