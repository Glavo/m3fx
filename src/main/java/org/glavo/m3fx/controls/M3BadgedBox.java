// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A container that overlays a Material Design 3 badge on content.
@NotNullByDefault
public class M3BadgedBox extends StackPane {
    /// The base style class for M3FX badged boxes.
    public static final String STYLE_CLASS = "m3-badged-box";

    /// The optional content node property.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// The optional badge property.
    private final ObjectProperty<@Nullable M3Badge> badge = new SimpleObjectProperty<>(this, "badge");

    /// The badge alignment inside this container.
    private final ObjectProperty<Pos> badgeAlignment = new SimpleObjectProperty<>(this, "badgeAlignment", Pos.TOP_RIGHT) {
        /// Restores the default badge alignment when the property is set to null.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(Pos.TOP_RIGHT);
                return;
            }
            updateBadgePlacement();
        }
    };

    /// The horizontal badge translation after alignment is applied.
    private final DoubleProperty badgeOffsetX = new SimpleDoubleProperty(this, "badgeOffsetX") {
        /// Updates badge placement after the offset changes.
        @Override
        protected void invalidated() {
            updateBadgePlacement();
        }
    };

    /// The vertical badge translation after alignment is applied.
    private final DoubleProperty badgeOffsetY = new SimpleDoubleProperty(this, "badgeOffsetY") {
        /// Updates badge placement after the offset changes.
        @Override
        protected void invalidated() {
            updateBadgePlacement();
        }
    };

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

    /// Returns the badge alignment inside this container.
    public final Pos getBadgeAlignment() {
        return badgeAlignment.get();
    }

    /// Sets the badge alignment inside this container.
    public final void setBadgeAlignment(Pos badgeAlignment) {
        this.badgeAlignment.set(Objects.requireNonNull(badgeAlignment, "badgeAlignment"));
    }

    /// Returns the badge alignment property.
    public final ObjectProperty<Pos> badgeAlignmentProperty() {
        return badgeAlignment;
    }

    /// Returns the horizontal badge translation after alignment is applied.
    public final double getBadgeOffsetX() {
        return badgeOffsetX.get();
    }

    /// Sets the horizontal badge translation after alignment is applied.
    public final void setBadgeOffsetX(double badgeOffsetX) {
        this.badgeOffsetX.set(badgeOffsetX);
    }

    /// Returns the horizontal badge translation property.
    public final DoubleProperty badgeOffsetXProperty() {
        return badgeOffsetX;
    }

    /// Returns the vertical badge translation after alignment is applied.
    public final double getBadgeOffsetY() {
        return badgeOffsetY.get();
    }

    /// Sets the vertical badge translation after alignment is applied.
    public final void setBadgeOffsetY(double badgeOffsetY) {
        this.badgeOffsetY.set(badgeOffsetY);
    }

    /// Returns the vertical badge translation property.
    public final DoubleProperty badgeOffsetYProperty() {
        return badgeOffsetY;
    }

    /// Returns the user-agent stylesheet for M3FX badge containers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("badge.css");
    }

    /// Initializes style classes and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
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
            updateBadgePlacement();
            getChildren().setAll(badgeNode);
        } else if (badgeNode == null) {
            getChildren().setAll(contentNode);
        } else {
            updateBadgePlacement();
            getChildren().setAll(contentNode, badgeNode);
        }
    }

    /// Applies alignment and offset to the current badge node.
    private void updateBadgePlacement() {
        @Nullable M3Badge badgeNode = getBadge();
        if (badgeNode == null) {
            return;
        }

        StackPane.setAlignment(badgeNode, getBadgeAlignment());
        badgeNode.setTranslateX(getBadgeOffsetX());
        badgeNode.setTranslateY(getBadgeOffsetY());
    }
}
