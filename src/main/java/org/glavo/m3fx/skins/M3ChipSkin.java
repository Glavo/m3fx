// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.controls.M3Chip;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Chip].
@NotNullByDefault
public class M3ChipSkin extends M3LabeledButtonSkinBase<M3Chip> {
    /// Creates a chip skin.
    public M3ChipSkin(M3Chip control) {
        super(control);
    }
}
