// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.control.skin.LabeledSkinBase;
import org.glavo.m3fx.controls.M3Text;
import org.jetbrains.annotations.NotNullByDefault;

/// The default labeled skin for [M3Text].
@NotNullByDefault
public final class M3TextSkin extends LabeledSkinBase<M3Text> {
    /// Creates a text skin.
    ///
    /// @param control the text control rendered by this skin
    public M3TextSkin(M3Text control) {
        super(control);
    }
}
