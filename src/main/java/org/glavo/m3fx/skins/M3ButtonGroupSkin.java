// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3ButtonGroup].
@NotNullByDefault
public final class M3ButtonGroupSkin extends M3ItemContainerSkinBase<M3ButtonGroup, HBox> {
    /// Creates a button group skin.
    ///
    /// @param control the button group controlled by this skin
    public M3ButtonGroupSkin(M3ButtonGroup control) {
        super(control, control.getItems(), new HBox());
        getContainer().alignmentProperty().bind(M3NodeLayout.createLogicalStartCenterAlignmentBinding(control));
        getContainer().spacingProperty().bind(control.spacingProperty());
    }

    /// Removes bindings before disposal.
    @Override
    public void dispose() {
        getContainer().alignmentProperty().unbind();
        getContainer().spacingProperty().unbind();
        super.dispose();
    }
}
