// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3NavigationDrawer;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3NavigationDrawer].
@NotNullByDefault
public final class M3NavigationDrawerSkin extends M3ItemContainerSkinBase<M3NavigationDrawer, VBox> {
    /// Creates a navigation drawer skin.
    ///
    /// @param control the skinned navigation drawer
    public M3NavigationDrawerSkin(M3NavigationDrawer control) {
        super(control, control.getItems(), new VBox());
        getContainer().spacingProperty().bind(control.itemSpacingProperty());
    }

    /// Removes the item-spacing binding before disposal.
    @Override
    public void dispose() {
        getContainer().spacingProperty().unbind();
        super.dispose();
    }
}
