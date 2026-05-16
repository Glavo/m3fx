// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A Material Design 3 menu surface.
@NotNullByDefault
public class M3Menu extends VBox {
    /// The base style class for M3FX menus.
    public static final String STYLE_CLASS = "m3-menu";

    /// Creates an empty menu.
    public M3Menu() {
        initialize();
    }

    /// Creates a menu containing the supplied items.
    public M3Menu(Node... items) {
        initialize();
        Objects.requireNonNull(items, "items");
        for (Node item : items) {
            Objects.requireNonNull(item, "item");
        }
        getItems().addAll(items);
    }

    /// Returns the mutable child list used as menu content.
    public final ObservableList<Node> getItems() {
        return getChildren();
    }

    /// Returns the user-agent stylesheet for M3FX menus.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("menu.css");
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
    }
}
