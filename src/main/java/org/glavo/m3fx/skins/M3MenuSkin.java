// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Menu;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3Menu].
@NotNullByDefault
public final class M3MenuSkin extends M3ItemContainerSkinBase<M3Menu, VBox> {
    /// Creates a menu skin.
    ///
    /// @param control the skinned menu
    public M3MenuSkin(M3Menu control) {
        super(control, control.getItems(), new VBox());
        getContainer().setSpacing(0.0);
    }
}
