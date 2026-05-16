// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.controls.M3SegmentedButton;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3SegmentedButton].
@NotNullByDefault
public class M3SegmentedButtonSkin extends M3LabeledButtonSkinBase<M3SegmentedButton> {
    /// Creates a segmented button skin.
    public M3SegmentedButtonSkin(M3SegmentedButton control) {
        super(control);
    }
}
