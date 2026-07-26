// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BadgedBoxSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A container that overlays a badge on another node.
///
/// The [content][#contentProperty()] and [badge][#badgeProperty()] are independent optional scene-graph nodes.
/// The badge is positioned relative to the content according to [badgeAlignment][#badgeAlignmentProperty()] and
/// then translated by the X and Y offsets. Horizontal alignment follows the effective node orientation.
///
/// A badged box is non-focus-traversable; focus remains with focusable content or badge children. Nodes assigned
/// to either slot cannot simultaneously be children of another parent. The no-argument constructor creates an
/// empty box with top-end alignment and zero offsets.
///
/// See [Material Design badges](https://m3.material.io/components/badges/overview).
@NotNullByDefault
public final class M3BadgedBox extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-badged-box";

    /// Creates an empty badged box with top-end alignment and zero offsets.
    public M3BadgedBox() {
        this(null, null);
    }

    /// Creates a badged box with the specified content and no badge.
    ///
    /// @param content the content node, or `null` for no content
    public M3BadgedBox(@Nullable Node content) {
        this(content, null);
    }

    /// Creates a badged box with the specified content and badge.
    ///
    /// @param content the content node, or `null` for no content
    /// @param badge   the badge node, or `null` for no badge
    public M3BadgedBox(@Nullable Node content, @Nullable M3Badge badge) {
        initialize();
        setContent(content);
        setBadge(badge);
    }

    /// The node over which the badge is positioned.
    ///
    /// The default value is `null`. Replacing or clearing the content updates layout and accessibility children.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// Returns the optional content node.
    ///
    /// @return the content node, or `null` when no content is set
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the optional content node.
    ///
    /// @param content the content node, or `null` to clear it
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable property that stores the optional content node.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the content property
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// The badge positioned over the content.
    ///
    /// The default value is `null`. Replacing or clearing the badge updates layout and accessibility children.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3Badge> badge = new SimpleObjectProperty<>(this, "badge");

    /// Returns the optional badge.
    ///
    /// @return the badge node, or `null` when no badge is set
    public final @Nullable M3Badge getBadge() {
        return badge.get();
    }

    /// Sets the optional badge.
    ///
    /// @param badge the badge node, or `null` to clear it
    public final void setBadge(@Nullable M3Badge badge) {
        this.badge.set(badge);
    }

    /// Returns the observable property that stores the optional badge.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the badge property
    public final ObjectProperty<@Nullable M3Badge> badgeProperty() {
        return badge;
    }

    /// The alignment of the badge within this container.
    ///
    /// The default value is [Pos#TOP_RIGHT]. Horizontal left and right positions are interpreted as logical start
    /// and end and therefore mirror with the effective node orientation. A direct `null` assignment restores the
    /// default; bound values must be non-null.
    ///
    /// @defaultValue [Pos#TOP_RIGHT]
    private final ObjectProperty<Pos> badgeAlignment = new SimpleObjectProperty<>(this, "badgeAlignment", Pos.TOP_RIGHT) {
        /// Restores the default badge alignment when the property is set to null.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(Pos.TOP_RIGHT);
                return;
            }
            requestLayout();
        }
    };

    /// Returns the badge alignment inside this container.
    ///
    /// Horizontal left and right alignments are interpreted as logical start and end and are resolved against the
    /// control's effective node orientation.
    ///
    /// @return the badge alignment
    public final Pos getBadgeAlignment() {
        return badgeAlignment.get();
    }

    /// Sets the badge alignment inside this container.
    ///
    /// Horizontal left and right alignments are interpreted as logical start and end and are resolved against the
    /// control's effective node orientation.
    ///
    /// @param badgeAlignment the badge alignment
    /// @throws NullPointerException if `badgeAlignment` is `null`
    public final void setBadgeAlignment(Pos badgeAlignment) {
        this.badgeAlignment.set(Objects.requireNonNull(badgeAlignment, "badgeAlignment"));
    }

    /// Returns the observable property that stores the badge alignment.
    ///
    /// The property can be observed and bound. Its default value is [Pos#TOP_RIGHT], and a direct `null`
    /// assignment restores that default.
    ///
    /// @return the badge alignment property
    public final ObjectProperty<Pos> badgeAlignmentProperty() {
        return badgeAlignment;
    }

    /// The horizontal translation applied after badge alignment, in logical pixels.
    ///
    /// The default value is `0.0`. Positive values move the badge toward increasing local X coordinates.
    ///
    /// @defaultValue `0.0`
    private final DoubleProperty badgeOffsetX = new SimpleDoubleProperty(this, "badgeOffsetX") {
        /// Updates badge placement after the offset changes.
        @Override
        protected void invalidated() {
            requestLayout();
        }
    };

    /// Returns the horizontal badge translation after alignment is applied.
    ///
    /// @return the horizontal badge offset in logical pixels
    public final double getBadgeOffsetX() {
        return badgeOffsetX.get();
    }

    /// Sets the horizontal badge translation after alignment is applied.
    ///
    /// @param badgeOffsetX the horizontal badge offset in logical pixels
    public final void setBadgeOffsetX(double badgeOffsetX) {
        this.badgeOffsetX.set(badgeOffsetX);
    }

    /// Returns the observable property that stores the horizontal badge offset.
    ///
    /// The property can be observed and bound. Its default value is `0.0` logical pixels.
    ///
    /// @return the horizontal badge offset property
    public final DoubleProperty badgeOffsetXProperty() {
        return badgeOffsetX;
    }

    /// The vertical translation applied after badge alignment, in logical pixels.
    ///
    /// The default value is `0.0`. Positive values move the badge toward increasing local Y coordinates.
    ///
    /// @defaultValue `0.0`
    private final DoubleProperty badgeOffsetY = new SimpleDoubleProperty(this, "badgeOffsetY") {
        /// Updates badge placement after the offset changes.
        @Override
        protected void invalidated() {
            requestLayout();
        }
    };

    /// Returns the vertical badge translation after alignment is applied.
    ///
    /// @return the vertical badge offset in logical pixels
    public final double getBadgeOffsetY() {
        return badgeOffsetY.get();
    }

    /// Sets the vertical badge translation after alignment is applied.
    ///
    /// @param badgeOffsetY the vertical badge offset in logical pixels
    public final void setBadgeOffsetY(double badgeOffsetY) {
        this.badgeOffsetY.set(badgeOffsetY);
    }

    /// Returns the observable property that stores the vertical badge offset.
    ///
    /// The property can be observed and bound. Its default value is `0.0` logical pixels.
    ///
    /// @return the vertical badge offset property
    public final DoubleProperty badgeOffsetYProperty() {
        return badgeOffsetY;
    }

    /// Notifies accessibility clients when focus moves between content and badge children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(
                    this,
                    getContent(),
                    getBadge()
            ));

    /// Returns the user-agent stylesheet for M3FX badge containers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("badge.css");
    }

    /// Creates the default Material Design 3 badged box skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BadgedBoxSkin(this);
    }

    /// Returns accessibility attributes for content and badge children.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case CONTENTS -> getContent();
            case ITEM_COUNT -> accessibleItemCount();
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getContent(), getBadge());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for content and badge children.
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleItem();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Requests focus on the current or first accessibility item.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem() {
        if (M3Accessible.showCurrentOrItem(this, getContent(), getBadge())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showCurrentOrItem(this, getContent(), getBadge(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the container focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Initializes style classes and property listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        content.addListener(observable -> handleContentChanged());
        badge.addListener(observable -> handleContentChanged());
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
    }

    /// Handles horizontal keyboard traversal between the content and badge targets.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(getContent(), getBadge()),
                true,
                false,
                -1,
                false
        );
    }

    /// Notifies accessibility clients that content or badge children changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the number of indexed content and badge nodes exposed to accessibility clients.
    private int accessibleItemCount() {
        return (getContent() == null ? 0 : 1) + (getBadge() == null ? 0 : 1);
    }

    /// Returns the indexed content or badge node requested by an accessibility client.
    private @Nullable Node accessibleItemAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }

        @Nullable Node contentNode = getContent();
        if (contentNode != null) {
            if (index == 0) {
                return contentNode;
            }
            index--;
        }

        return index == 0 ? getBadge() : null;
    }

    /// Requests layout and notifies accessibility clients after content or badge changes.
    private void handleContentChanged() {
        requestLayout();
        notifyAccessibleItemsChanged();
    }
}
