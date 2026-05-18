// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.control.skin.LabeledSkinBase;
import org.glavo.m3fx.controls.M3Icon;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3Icon].
@NotNullByDefault
public final class M3IconSkin extends LabeledSkinBase<M3Icon> {
    /// Creates an icon skin.
    public M3IconSkin(M3Icon control) {
        super(control);
    }
}
