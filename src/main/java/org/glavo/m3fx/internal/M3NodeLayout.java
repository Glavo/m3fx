// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

/// Internal direction helpers for controls that implement bidirectional layout.
///
/// JavaFX automatically mirrors children of a right-to-left parent. The physical-alignment methods in this class
/// are therefore only for internal nodes deliberately isolated with `NodeOrientation.LEFT_TO_RIGHT`; ordinary
/// children must keep their alignment and padding in local left-to-right coordinates.
@NotNullByDefault
public final class M3NodeLayout {
    /// Prevents utility class instantiation.
    private M3NodeLayout() {
    }

    /// Returns whether the node currently resolves to right-to-left layout.
    ///
    /// @param node the node that owns the effective orientation
    /// @return `true` when the node's effective orientation is `NodeOrientation.RIGHT_TO_LEFT`
    public static boolean isRightToLeft(Node node) {
        return node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
    }

    /// Returns the physical center alignment for an isolated node at the owner's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the physical start-side center alignment for the current effective orientation
    public static Pos physicalStartCenterAlignment(Node node) {
        return isRightToLeft(node) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT;
    }

    /// Returns the physical top alignment for an isolated node at the owner's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the physical top-start alignment for the current effective orientation
    public static Pos physicalStartTopAlignment(Node node) {
        return isRightToLeft(node) ? Pos.TOP_RIGHT : Pos.TOP_LEFT;
    }

    /// Returns the physical top alignment for an isolated node at the owner's logical end edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the physical top-end alignment for the current effective orientation
    public static Pos physicalEndTopAlignment(Node node) {
        return isRightToLeft(node) ? Pos.TOP_LEFT : Pos.TOP_RIGHT;
    }

    /// Creates a physical center-alignment binding for an isolated node at the owner's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return a binding that resolves to the physical start side for the current effective orientation
    public static ObjectBinding<Pos> createPhysicalStartCenterAlignmentBinding(Node node) {
        return Bindings.createObjectBinding(
                () -> physicalStartCenterAlignment(node),
                node.effectiveNodeOrientationProperty()
        );
    }

    /// Creates a physical top-alignment binding for an isolated node at the owner's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return a binding that resolves to the physical top-start side for the current effective orientation
    public static ObjectBinding<Pos> createPhysicalStartTopAlignmentBinding(Node node) {
        return Bindings.createObjectBinding(
                () -> physicalStartTopAlignment(node),
                node.effectiveNodeOrientationProperty()
        );
    }
}