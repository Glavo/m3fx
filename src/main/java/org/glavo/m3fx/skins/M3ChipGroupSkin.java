// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3ChipGroup].
@NotNullByDefault
public final class M3ChipGroupSkin extends M3ItemContainerSkinBase<M3ChipGroup, FlowPane> {
    /// The default horizontal gap between chips.
    private static final double DEFAULT_HORIZONTAL_GAP = 8.0;

    /// The default vertical gap between wrapped chip rows.
    private static final double DEFAULT_VERTICAL_GAP = 8.0;

    /// Creates a chip group skin.
    public M3ChipGroupSkin(M3ChipGroup control) {
        super(control, control.getItems(), new FlowPane());
        getContainer().setAlignment(Pos.CENTER_LEFT);
        getContainer().setHgap(DEFAULT_HORIZONTAL_GAP);
        getContainer().setVgap(DEFAULT_VERTICAL_GAP);
        getContainer().prefWrapLengthProperty().bind(control.prefWrapLengthProperty());
    }

    /// Removes bindings before disposal.
    @Override
    public void dispose() {
        getContainer().prefWrapLengthProperty().unbind();
        super.dispose();
    }
}
