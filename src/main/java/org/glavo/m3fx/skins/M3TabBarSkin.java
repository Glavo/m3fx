// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3TabBar;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3TabBar].
@NotNullByDefault
public final class M3TabBarSkin extends M3ItemContainerSkinBase<M3TabBar, HBox> {
    /// Creates a tab bar skin.
    public M3TabBarSkin(M3TabBar control) {
        super(control, control.getTabs(), new HBox());
        getContainer().getStyleClass().add(M3TabBar.CONTAINER_STYLE_CLASS);
        getContainer().alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
    }

    /// Removes bindings before disposal.
    @Override
    public void dispose() {
        getContainer().alignmentProperty().unbind();
        super.dispose();
    }
}
