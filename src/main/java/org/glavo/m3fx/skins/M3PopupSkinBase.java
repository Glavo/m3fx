// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// The base class for M3FX skins whose skinnable is a JavaFX [PopupControl].
///
/// A popup control is not a [javafx.scene.control.Control], so its skin cannot extend
/// [javafx.scene.control.SkinBase]. This region supplies the popup scene node while retaining the skinnable reference
/// required by [Skin]. Subclasses remain responsible for releasing listeners and bindings from [dispose()].
@NotNullByDefault
abstract class M3PopupSkinBase<C extends PopupControl> extends Region implements Skin<C> {
    /// The popup control rendered by this skin.
    private final C control;

    /// Creates a popup skin for the supplied control.
    ///
    /// @param control the popup control rendered by this skin
    /// @throws NullPointerException if `control` is `null`
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
    ///
    /// The base implementation has no resources to release. Subclasses that override this method should release
    /// their listeners and bindings before invoking `super.dispose()`.
    @Override
    public void dispose() {
    }
}
