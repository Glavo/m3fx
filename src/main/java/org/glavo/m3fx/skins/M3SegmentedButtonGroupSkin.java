// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SegmentedButtonGroup].
///
/// The skin presents the group's observable button list as one logical horizontal sequence. Alignment follows node
/// orientation and spacing is bound to the group property, while each button skin remains responsible for edge shape
/// and selection treatment.
@NotNullByDefault
public final class M3SegmentedButtonGroupSkin
        extends M3ItemContainerSkinBase<M3SegmentedButtonGroup, HBox, M3SegmentedButton> {
    /// Creates a segmented button group skin.
    ///
    /// @param control the segmented button group controlled by this skin
    public M3SegmentedButtonGroupSkin(M3SegmentedButtonGroup control) {
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
