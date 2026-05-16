// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.controls.M3Button;
import org.jetbrains.annotations.NotNullByDefault;

/// The default animated skin for [M3Button].
@NotNullByDefault
public class M3ButtonSkin extends M3LabeledButtonSkinBase<M3Button> {
    /// Creates a button skin.
    public M3ButtonSkin(M3Button control) {
        super(control);
    }
}
