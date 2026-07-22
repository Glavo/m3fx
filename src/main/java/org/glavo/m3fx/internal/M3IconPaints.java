// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Stores paint channels shared privately by the font and SVG icon implementations.
///
/// Semantic paint is populated by each icon's user-agent stylesheet. Inherited paint is supplied by a containing
/// M3FX component for the duration of icon ownership. Both channels live on the icon node so skins can consume them
/// without adding implementation accessors to the exported control API.
@NotNullByDefault
public final class M3IconPaints {
    /// Node-property key for the styleable semantic paint.
    private static final IdentityKey SEMANTIC_PAINT_KEY =
            new IdentityKey(M3IconPaints.class.getName() + ".semanticPaint");

    /// Node-property key for the inherited component paint.
    private static final IdentityKey INHERITED_PAINT_KEY =
            new IdentityKey(M3IconPaints.class.getName() + ".inheritedPaint");

    /// Prevents utility class instantiation.
    private M3IconPaints() {
    }

    /// Initializes the styleable semantic paint channel for an icon.
    ///
    /// Repeated calls preserve the existing property and do not replace its metadata or value.
    ///
    /// @param icon        the icon node that owns the channel
    /// @param cssMetaData the CSS metadata exposed by the icon class
    /// @throws NullPointerException if `icon` or `cssMetaData` is `null`
    public static void initializeSemanticPaint(
            Control icon,
            CssMetaData<? extends Styleable, @Nullable Paint> cssMetaData
    ) {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(cssMetaData, "cssMetaData");
        Object existing = icon.getProperties().get(SEMANTIC_PAINT_KEY);
        if (existing instanceof StyleableObjectProperty<?>) {
            return;
        }
        StyleableObjectProperty<@Nullable Paint> property = M3Css.styleableObjectProperty(
                Color.BLACK,
                icon,
                "semanticIconPaint",
                cssMetaData,
                icon::requestLayout
        );
        icon.getProperties().put(SEMANTIC_PAINT_KEY, property);
    }

    /// Returns an icon's initialized semantic paint property.
    ///
    /// @param icon the icon node
    /// @return the stable semantic paint property
    /// @throws NullPointerException  if `icon` is `null`
    /// @throws IllegalStateException if the icon implementation has not initialized its paint channel
    public static StyleableObjectProperty<@Nullable Paint> semanticPaintProperty(Node icon) {
        Objects.requireNonNull(icon, "icon");
        Object value = icon.getProperties().get(SEMANTIC_PAINT_KEY);
        if (!(value instanceof StyleableObjectProperty<?> property)) {
            throw new IllegalStateException("icon semantic paint has not been initialized");
        }
        @SuppressWarnings("unchecked")
        StyleableObjectProperty<@Nullable Paint> paintProperty =
                (StyleableObjectProperty<@Nullable Paint>) property;
        return paintProperty;
    }

    /// Returns whether a node exposes the M3FX icon paint channels.
    ///
    /// This query does not allocate either channel. Containers use it before supplying inherited paint so arbitrary
    /// graphic nodes do not acquire unused implementation properties.
    ///
    /// @param node the node to inspect
    /// @return `true` if the node initialized an M3FX semantic paint channel
    /// @throws NullPointerException if `node` is `null`
    public static boolean supportsInheritedPaint(Node node) {
        Objects.requireNonNull(node, "node");
        return node.hasProperties()
                && node.getProperties().get(SEMANTIC_PAINT_KEY) instanceof StyleableObjectProperty<?>;
    }

    /// Returns the lazily allocated paint supplied by a containing component.
    ///
    /// @param icon the icon node
    /// @return the stable inherited paint property
    /// @throws NullPointerException if `icon` is `null`
    public static ObjectProperty<@Nullable Paint> inheritedPaintProperty(Node icon) {
        Objects.requireNonNull(icon, "icon");
        Object existing = icon.getProperties().get(INHERITED_PAINT_KEY);
        if (existing instanceof ObjectProperty<?> property) {
            @SuppressWarnings("unchecked")
            ObjectProperty<@Nullable Paint> paintProperty = (ObjectProperty<@Nullable Paint>) property;
            return paintProperty;
        }
        ObjectProperty<@Nullable Paint> property = new SimpleObjectProperty<>(icon, "inheritedIconPaint");
        icon.getProperties().put(INHERITED_PAINT_KEY, property);
        return property;
    }

    /// Sets the paint inherited from the icon's current containing component.
    ///
    /// @param icon  the icon node
    /// @param paint the inherited paint, or `null` when no component owns the icon color
    /// @throws NullPointerException if `icon` is `null`
    public static void setInheritedPaint(Node icon, @Nullable Paint paint) {
        inheritedPaintProperty(icon).set(paint);
    }
}
