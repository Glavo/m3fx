// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Menu;
import org.glavo.m3fx.controls.M3MenuItem;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3Menu].
@NotNullByDefault
public final class M3MenuSkin extends M3ItemContainerSkinBase<M3Menu, VBox> {
    /// The pseudo-class applied to the first direct menu item.
    private static final PseudoClass FIRST_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("first-menu-item");

    /// The pseudo-class applied to the last direct menu item.
    private static final PseudoClass LAST_ITEM_PSEUDO_CLASS = PseudoClass.getPseudoClass("last-menu-item");

    /// Updates direct menu item structural pseudo-classes after item list changes.
    private final ListChangeListener<Node> itemStructureListener = change -> {
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                if (removed instanceof M3MenuItem item) {
                    setItemStructurePseudoClasses(item, false, false);
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
        updateItemStructurePseudoClasses();
    }

    /// Removes item structure listeners before disposal.
    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemStructureListener);
        clearItemStructurePseudoClasses();
        super.dispose();
    }

    /// Updates first and last pseudo-classes on direct menu items.
    private void updateItemStructurePseudoClasses() {
        @Nullable M3MenuItem firstItem = null;
        @Nullable M3MenuItem lastItem = null;
        for (Node child : getContainer().getChildren()) {
            if (child instanceof M3MenuItem item) {
                if (firstItem == null) {
                    firstItem = item;
                }
                lastItem = item;
            }
        }

        for (Node child : getContainer().getChildren()) {
            if (child instanceof M3MenuItem item) {
                setItemStructurePseudoClasses(item, item == firstItem, item == lastItem);
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
