// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.controls.M3ButtonBase;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// The default animated skin for [M3ButtonBase].
@NotNullByDefault
public final class M3ButtonSkin extends M3LabeledButtonSkinBase<M3ButtonBase> {
    /// Creates a button skin.
    ///
    /// @param control the button controlled by this skin
    public M3ButtonSkin(M3ButtonBase control) {
        super(control);
    }

    /// Returns a pressed scale only for elevated buttons that already own elevation.
    @Override
    protected double pressedScale(boolean pressed) {
        if (getSkinnable().getVariant() == M3ButtonVariant.ELEVATED) {
            return depthPressedScale(pressed);
        }
        return 1.0;
    }
}
