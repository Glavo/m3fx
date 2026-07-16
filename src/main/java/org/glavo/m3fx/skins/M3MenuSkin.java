// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.beans.InvalidationListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuItem;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3Menu].
@NotNullByDefault
public final class M3MenuSkin extends M3ItemContainerSkinBase<M3Menu, VBox, Node> {
    /// The pseudo-class applied to the first direct menu item.
    private static final PseudoClass FIRST_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("first-menu-item");

    /// The pseudo-class applied to the last direct menu item.
    private static final PseudoClass LAST_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("last-menu-item");

    /// Recomputes visual group boundaries when an item enters or leaves layout.
    private final InvalidationListener itemVisibilityListener = observable -> updateItemStructurePseudoClasses();

    /// Updates direct menu item structural pseudo-classes after item list changes.
    private final ListChangeListener<Node> itemStructureListener = change -> {
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                if (removed instanceof M3MenuItem item) {
                    item.visibleProperty().removeListener(itemVisibilityListener);
                    item.managedProperty().removeListener(itemVisibilityListener);
                    setItemStructurePseudoClasses(item, false, false);
                }
            }
            for (Node added : change.getAddedSubList()) {
                if (added instanceof M3MenuItem item) {
                    item.visibleProperty().addListener(itemVisibilityListener);
                    item.managedProperty().addListener(itemVisibilityListener);
                }
            }
        }
        updateItemStructurePseudoClasses();
    };

    /// Creates a menu skin.
    ///
    /// @param control the skinned menu
    public M3MenuSkin(M3Menu control) {
        super(control, control.getItems(), new VBox());
        getContainer().getStyleClass().add(M3Menu.CONTAINER_STYLE_CLASS);
        control.getItems().addListener(itemStructureListener);
        for (Node child : control.getItems()) {
            if (child instanceof M3MenuItem item) {
                item.visibleProperty().addListener(itemVisibilityListener);
                item.managedProperty().addListener(itemVisibilityListener);
            }
        }
        updateItemStructurePseudoClasses();
    }

    /// Removes item structure listeners before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemStructureListener);
        for (Node child : getSkinnable().getItems()) {
            if (child instanceof M3MenuItem item) {
                item.visibleProperty().removeListener(itemVisibilityListener);
                item.managedProperty().removeListener(itemVisibilityListener);
            }
        }
        clearItemStructurePseudoClasses();
        super.dispose();
    }

    /// Updates first and last pseudo-classes for each contiguous visible menu-item group.
    private void updateItemStructurePseudoClasses() {
        clearItemStructurePseudoClasses();

        @Nullable M3MenuItem firstItem = null;
        @Nullable M3MenuItem previousItem = null;
        for (Node child : getContainer().getChildren()) {
            if (!child.isVisible() || !child.isManaged()) {
                continue;
            }
            if (child instanceof M3MenuItem item) {
                if (firstItem == null) {
                    firstItem = item;
                }
                previousItem = item;
            } else if (firstItem != null) {
                setItemStructurePseudoClasses(firstItem, true, firstItem == previousItem);
                if (previousItem != firstItem) {
                    setItemStructurePseudoClasses(previousItem, false, true);
                }
                firstItem = null;
                previousItem = null;
            }
        }

        if (firstItem != null) {
            setItemStructurePseudoClasses(firstItem, true, firstItem == previousItem);
            if (previousItem != firstItem) {
                setItemStructurePseudoClasses(previousItem, false, true);
            }
        }
    }

    /// Clears structural pseudo-classes from direct menu items.
    private void clearItemStructurePseudoClasses() {
        for (Node child : getContainer().getChildren()) {
            if (child instanceof M3MenuItem item) {
                setItemStructurePseudoClasses(item, false, false);
            }
        }
    }

    /// Sets structural pseudo-classes on one menu item.
    private static void setItemStructurePseudoClasses(M3MenuItem item, boolean first, boolean last) {
        item.pseudoClassStateChanged(FIRST_ITEM_PSEUDO_CLASS, first);
        item.pseudoClassStateChanged(LAST_ITEM_PSEUDO_CLASS, last);
    }
}
