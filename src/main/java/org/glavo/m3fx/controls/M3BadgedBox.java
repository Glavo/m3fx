// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A container that overlays a Material Design 3 badge on content.
@NotNullByDefault
public class M3BadgedBox extends StackPane {
    /// The base style class for M3FX badged boxes.
    public static final String STYLE_CLASS = "m3-badged-box";

    /// The optional content node property.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// The optional badge property.
    private final ObjectProperty<@Nullable M3Badge> badge = new SimpleObjectProperty<>(this, "badge");

    /// Creates an empty badged box.
    public M3BadgedBox() {
        this(null, null);
    }

    /// Creates a badged box with content.
    public M3BadgedBox(@Nullable Node content) {
        this(content, null);
    }

    /// Creates a badged box with content and badge.
    public M3BadgedBox(@Nullable Node content, @Nullable M3Badge badge) {
        initialize();
        setContent(content);
        setBadge(badge);
    }

    /// Returns the optional content node.
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the optional content node.
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the optional content node property.
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the optional badge.
    public final @Nullable M3Badge getBadge() {
        return badge.get();
    }

    /// Sets the optional badge.
    public final void setBadge(@Nullable M3Badge badge) {
        this.badge.set(badge);
    }

    /// Returns the optional badge property.
    public final ObjectProperty<@Nullable M3Badge> badgeProperty() {
        return badge;
    }

    /// Returns the user-agent stylesheet for M3FX badge containers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("badge.css");
    }

    /// Initializes style classes and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        content.addListener(observable -> updateChildren());
        badge.addListener(observable -> updateChildren());
        updateChildren();
    }

    /// Updates child nodes and badge alignment.
    private void updateChildren() {
        @Nullable Node contentNode = getContent();
        @Nullable M3Badge badgeNode = getBadge();
        if (contentNode == null && badgeNode == null) {
            getChildren().clear();
        } else if (contentNode == null) {
            StackPane.setAlignment(badgeNode, Pos.TOP_RIGHT);
            getChildren().setAll(badgeNode);
        } else if (badgeNode == null) {
            getChildren().setAll(contentNode);
        } else {
            StackPane.setAlignment(badgeNode, Pos.TOP_RIGHT);
            getChildren().setAll(contentNode, badgeNode);
        }
    }
}
