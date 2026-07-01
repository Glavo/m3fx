// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.FlowPane;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3ChipGroup].
@NotNullByDefault
public final class M3ChipGroupSkin extends M3ItemContainerSkinBase<M3ChipGroup, FlowPane> {
    /// Creates a chip group skin.
    ///
    /// @param control the chip group controlled by this skin
    public M3ChipGroupSkin(M3ChipGroup control) {
        super(control, control.getItems(), new FlowPane());
        getContainer().alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        getContainer().hgapProperty().bind(control.horizontalGapProperty());
        getContainer().vgapProperty().bind(control.verticalGapProperty());
        getContainer().prefWrapLengthProperty().bind(control.prefWrapLengthProperty());
    }

    /// Removes bindings before disposal.
    @Override
    public void dispose() {
        getContainer().alignmentProperty().unbind();
        getContainer().hgapProperty().unbind();
        getContainer().vgapProperty().unbind();
        getContainer().prefWrapLengthProperty().unbind();
        super.dispose();
    }
}
