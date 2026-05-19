// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.jetbrains.annotations.NotNullByDefault;

/// The default Material Design 3 skin for [M3SegmentedButtonGroup].
@NotNullByDefault
public final class M3SegmentedButtonGroupSkin extends M3ItemContainerSkinBase<M3SegmentedButtonGroup, HBox> {
    /// The spacing that lets adjacent segmented button borders overlap.
    private static final double SEGMENT_SPACING = -1.0;

    /// Creates a segmented button group skin.
    public M3SegmentedButtonGroupSkin(M3SegmentedButtonGroup control) {
        super(control, control.getItems(), new HBox());
        getContainer().setAlignment(Pos.CENTER_LEFT);
        getContainer().setSpacing(SEGMENT_SPACING);
    }
}
