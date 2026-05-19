// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3ButtonGroup].
@NotNullByDefault
public final class M3ButtonGroupSkin extends M3ItemContainerSkinBase<M3ButtonGroup, HBox> {
    /// The spacing that lets adjacent grouped button borders overlap.
    private static final double GROUPED_BUTTON_SPACING = -1.0;

    /// Creates a button group skin.
    public M3ButtonGroupSkin(M3ButtonGroup control) {
        super(control, control.getItems(), new HBox());
        getContainer().setAlignment(Pos.CENTER_LEFT);
        getContainer().setSpacing(GROUPED_BUTTON_SPACING);
    }
}
