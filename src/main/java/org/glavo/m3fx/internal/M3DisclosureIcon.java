// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.skins.M3DisclosureIconSkin;
import org.jetbrains.annotations.NotNullByDefault;

/// A compact Material Design disclosure indicator for expandable rows and destinations.
///
/// See [Material Design navigation drawer](https://m3.material.io/components/navigation-drawer/overview).
@NotNullByDefault
public final class M3DisclosureIcon extends Control {
    /// The base style class for M3FX disclosure icons.
    public static final String STYLE_CLASS = "m3-disclosure-icon";

    /// The expanded pseudo-class used by disclosure icons.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    // Whether the disclosure target is expanded.
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded") {
        /// Updates expanded pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, get());
        }
    };

    /// Creates a collapsed disclosure icon.
    public M3DisclosureIcon() {
        this(false);
    }

    /// Creates a disclosure icon with the supplied expanded state.
    ///
    /// @param expanded whether the disclosure target starts expanded
    public M3DisclosureIcon(boolean expanded) {
        getStyleClass().add(STYLE_CLASS);
        setFocusTraversable(false);
        setExpanded(expanded);
    }

    /// Returns whether the disclosure target is expanded.
    ///
    /// @return `true` when the disclosure target is expanded
    public final boolean isExpanded() {
        return expanded.get();
    }

    /// Sets whether the disclosure target is expanded.
    ///
    /// @param expanded whether the disclosure target is expanded
    public final void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    /// Returns the expanded state property.
    ///
    /// @return the expanded state property
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    /// Returns the user-agent stylesheet for M3FX disclosure icons.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("disclosure-icon.css");
    }

    /// Creates the default Material Design 3 disclosure icon skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DisclosureIconSkin(this);
    }
}
