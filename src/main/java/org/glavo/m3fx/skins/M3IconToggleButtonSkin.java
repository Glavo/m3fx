// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.controls.M3IconToggleButton;
import org.jetbrains.annotations.NotNullByDefault;

/// The default animated skin for [M3IconToggleButton].
@NotNullByDefault
public class M3IconToggleButtonSkin extends M3LabeledButtonSkinBase<M3IconToggleButton> {
    /// Creates a toggle icon button skin.
    ///
    /// @param control the toggle icon button controlled by this skin
    public M3IconToggleButtonSkin(M3IconToggleButton control) {
        super(control);
    }
}
