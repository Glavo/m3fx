// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 menu item.
///
/// `M3MenuItem` specializes [M3ListItem] for use inside [M3Menu]. It provides menu-item accessibility semantics,
/// optional leading and trailing slots, action dispatch, and selection state when the containing menu uses a
/// selectable mode.
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public class M3MenuItem extends M3ListItem {
    /// The base style class for m3fx menu items.
    public static final String STYLE_CLASS = "m3-menu-item";

    /// Creates an empty menu item.
    public M3MenuItem() {
        this("");
    }

    /// Creates a menu item with text.
    public M3MenuItem(String text) {
        super(text);
        initialize();
    }

    /// Creates a menu item with text and leading content.
    public M3MenuItem(String text, @Nullable Node leading) {
        this(text);
        setLeading(leading);
    }

    /// Creates a menu item with text, leading content, and trailing content.
    public M3MenuItem(String text, @Nullable Node leading, @Nullable Node trailing) {
        this(text, leading);
        setTrailing(trailing);
    }

    /// Creates a menu item with text, leading content, trailing content, and an action handler.
    public M3MenuItem(
            String text,
            @Nullable Node leading,
            @Nullable Node trailing,
            @Nullable EventHandler<ActionEvent> onAction
    ) {
        this(text, leading, trailing);
        setOnAction(onAction);
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.MENU_ITEM);
    }
}
