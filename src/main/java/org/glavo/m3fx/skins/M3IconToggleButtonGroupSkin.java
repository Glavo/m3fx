// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3IconToggleButtonGroup;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3IconToggleButtonGroup].
@NotNullByDefault
public final class M3IconToggleButtonGroupSkin
        extends M3ItemContainerSkinBase<M3IconToggleButtonGroup, HBox, M3IconToggleButton> {
    /// Creates a toggle icon button group skin.
    ///
    /// @param control the toggle icon button group controlled by this skin
    public M3IconToggleButtonGroupSkin(M3IconToggleButtonGroup control) {
        super(control, control.getItems(), new HBox());
        getContainer().setAlignment(Pos.CENTER_LEFT);
        getContainer().spacingProperty().bind(control.spacingProperty());
    }

    /// Removes bindings before disposal.
    @Override
    public void dispose() {
        getContainer().spacingProperty().unbind();
        super.dispose();
    }
}
