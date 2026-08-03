// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.control.TreeCell;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3TreeView;
import org.jetbrains.annotations.NotNullByDefault;

/// The default virtualized skin for [M3TreeView].
///
/// The skin preserves JavaFX tree navigation and virtualization while applying the same standalone Material
/// scrollbar styling used by [M3ListViewSkin].
///
/// @param <T> the tree-item value type
@NotNullByDefault
public final class M3TreeViewSkin<T> extends TreeViewSkin<T> {
    /// Creates a Material tree-view skin.
    ///
    /// @param control the skinned Material tree view
    public M3TreeViewSkin(M3TreeView<T> control) {
        super(control);
    }

    /// Creates the virtual flow whose scrollbars use the shared Material scroll contract.
    ///
    /// @return a Material-styled tree virtual flow
    @Override
    protected VirtualFlow<TreeCell<T>> createVirtualFlow() {
        return new TreeViewVirtualFlow<>();
    }

    /// A virtual flow that exposes its protected scrollbars during construction.
    @NotNullByDefault
    private static final class TreeViewVirtualFlow<T> extends VirtualFlow<TreeCell<T>> {
        /// Creates a flow and applies the shared standalone scrollbar style.
        private TreeViewVirtualFlow() {
            M3ScrollPane.style(getHbar());
            M3ScrollPane.style(getVbar());
        }
    }
}
