// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipStyle;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Chip].
@NotNullByDefault
public class M3ChipSkin extends M3LabeledButtonSkinBase<M3Chip> {
    /// Creates a chip skin.
    ///
    /// @param control the chip controlled by this skin
    public M3ChipSkin(M3Chip control) {
        super(control);
    }

    /// Returns a pressed scale only for elevated chips that already own elevation.
    @Override
    protected double pressedScale(boolean pressed) {
        if (getSkinnable().getChipStyle() == M3ChipStyle.ELEVATED) {
            return depthPressedScale(pressed);
        }
        return 1.0;
    }
}
