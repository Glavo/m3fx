// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SegmentedButtonGroup].
@NotNullByDefault
public final class M3SegmentedButtonGroupSkin extends M3ItemContainerSkinBase<M3SegmentedButtonGroup, HBox> {
    /// Creates a segmented button group skin.
    ///
    /// @param control the segmented button group controlled by this skin
    public M3SegmentedButtonGroupSkin(M3SegmentedButtonGroup control) {
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
