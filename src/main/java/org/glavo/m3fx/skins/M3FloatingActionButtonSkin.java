// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.jetbrains.annotations.NotNullByDefault;

/// The default animated skin for [M3FloatingActionButton].
@NotNullByDefault
public class M3FloatingActionButtonSkin extends M3LabeledButtonSkinBase<M3FloatingActionButton> {
    /// Creates a floating action button skin.
    public M3FloatingActionButtonSkin(M3FloatingActionButton control) {
        super(control);
    }

    /// Returns depth-style pressed scale for floating action buttons.
    @Override
    protected double pressedScale(boolean pressed) {
        return depthPressedScale(pressed);
    }
}
