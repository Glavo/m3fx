// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// The base class for popup-control skins whose skinnable is not accepted by JavaFX [javafx.scene.control.SkinBase].
@NotNullByDefault
abstract class M3PopupSkinBase<C extends PopupControl> extends Region implements Skin<C> {
    /// The popup control rendered by this skin.
    private final C control;

    /// Creates a popup skin for the supplied control.
    ///
    /// @param control the popup control rendered by this skin
    M3PopupSkinBase(C control) {
        this.control = Objects.requireNonNull(control, "control");
    }

    /// Returns the popup control rendered by this skin.
    @Override
    public final C getSkinnable() {
        return control;
    }

    /// Returns this region as the node inserted into the popup scene.
    @Override
    public final Node getNode() {
        return this;
    }

    /// Releases resources held by this base skin.
    @Override
    public void dispose() {
    }
}
