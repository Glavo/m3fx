// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

/// Internal layout helpers that resolve logical edges from a node's effective orientation.
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

    /// Returns the center alignment for the node's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the visual start-side center alignment for the current effective orientation
    public static Pos logicalStartCenterAlignment(Node node) {
        return isRightToLeft(node) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT;
    }

    /// Returns the top alignment for the node's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the visual top-start alignment for the current effective orientation
    public static Pos logicalStartTopAlignment(Node node) {
        return isRightToLeft(node) ? Pos.TOP_RIGHT : Pos.TOP_LEFT;
    }

    /// Returns the top alignment for the node's logical end edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the visual top-end alignment for the current effective orientation
    public static Pos logicalEndTopAlignment(Node node) {
        return isRightToLeft(node) ? Pos.TOP_LEFT : Pos.TOP_RIGHT;
    }

    /// Returns the center alignment for the node's logical end edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the visual end-side center alignment for the current effective orientation
    public static Pos logicalEndCenterAlignment(Node node) {
        return isRightToLeft(node) ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT;
    }

    /// Resolves a horizontal alignment as logical start or end for the node's effective orientation.
    ///
    /// @param node the node that owns the effective orientation
    /// @param alignment the alignment to resolve
    /// @return the physical alignment matching the current effective orientation
    public static Pos logicalAlignment(Node node, Pos alignment) {
        if (!isRightToLeft(node)) {
            return alignment;
        }
        return switch (alignment) {
            case TOP_LEFT -> Pos.TOP_RIGHT;
            case TOP_RIGHT -> Pos.TOP_LEFT;
            case CENTER_LEFT -> Pos.CENTER_RIGHT;
            case CENTER_RIGHT -> Pos.CENTER_LEFT;
            case BOTTOM_LEFT -> Pos.BOTTOM_RIGHT;
            case BOTTOM_RIGHT -> Pos.BOTTOM_LEFT;
            case BASELINE_LEFT -> Pos.BASELINE_RIGHT;
            case BASELINE_RIGHT -> Pos.BASELINE_LEFT;
            default -> alignment;
        };
    }

    /// Returns the horizontal alignment for the node's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return the visual start-side horizontal alignment for the current effective orientation
    public static HPos logicalStartHorizontalAlignment(Node node) {
        return isRightToLeft(node) ? HPos.RIGHT : HPos.LEFT;
    }

    /// Returns physical insets from logical leading and trailing edge values.
    ///
    /// @param node the node that owns the effective orientation
    /// @param top the top inset
    /// @param leading the logical leading-edge inset
    /// @param bottom the bottom inset
    /// @param trailing the logical trailing-edge inset
    /// @return physical insets matching the node's current effective orientation
    public static Insets logicalInsets(Node node, double top, double leading, double bottom, double trailing) {
        if (isRightToLeft(node)) {
            return new Insets(top, leading, bottom, trailing);
        }
        return new Insets(top, trailing, bottom, leading);
    }

    /// Creates a center alignment binding that follows the node's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return a binding that resolves to the visual start side for the current effective orientation
    public static ObjectBinding<Pos> createLogicalStartCenterAlignmentBinding(Node node) {
        return Bindings.createObjectBinding(
                () -> logicalStartCenterAlignment(node),
                node.effectiveNodeOrientationProperty()
        );
    }

    /// Creates a top alignment binding that follows the node's logical start edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return a binding that resolves to the visual top-start side for the current effective orientation
    public static ObjectBinding<Pos> createLogicalStartTopAlignmentBinding(Node node) {
        return Bindings.createObjectBinding(
                () -> logicalStartTopAlignment(node),
                node.effectiveNodeOrientationProperty()
        );
    }

    /// Creates a center alignment binding that follows the node's logical end edge.
    ///
    /// @param node the node that owns the effective orientation
    /// @return a binding that resolves to the visual end side for the current effective orientation
    public static ObjectBinding<Pos> createLogicalEndCenterAlignmentBinding(Node node) {
        return Bindings.createObjectBinding(
                () -> logicalEndCenterAlignment(node),
                node.effectiveNodeOrientationProperty()
        );
    }
}