// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/// Provides access to the rows currently rendered by a virtualized list presentation.
///
/// This contract separates list behavior and accessibility from the concrete skin and virtual-flow
/// implementation. Returned nodes are presentation instances whose item association may change when cells are
/// reused.
///
/// @implSpec Implementations must return the nodes used by the active presentation. Query methods must not create
/// cells or scene-graph snapshots solely to answer a request.
@NotNullByDefault
public interface M3ListViewPresentation {
    /// Refreshes the state of materialized rows.
    void refreshCells();

    /// Refreshes focused-row state and reveals the focused row when one exists.
    ///
    /// @param requestNodeFocus whether the materialized row should receive keyboard focus
    /// @param animated         whether revealing the row should animate when motion is available
    void refreshFocus(boolean requestNodeFocus, boolean animated);

    /// Returns a rendered row for the specified index.
    ///
    /// The returned row may be reusable without currently being attached to the scene graph.
    ///
    /// @param index the data item index
    /// @return the rendered row, or `null` when no row is available
    @Nullable Node visibleItem(int index);

    /// Returns the attached rendered row for the specified index.
    ///
    /// @param index the data item index
    /// @return the attached row, or `null` when the row is not materialized
    @Nullable Node attachedVisibleItem(int index);

    /// Returns the data index of the attached row containing a node.
    ///
    /// @param node the node to locate
    /// @return the containing data index, or `-1` when the node is not in an attached row
    int attachedVisibleItemIndex(Node node);

    /// Returns the first attached row accepted by a predicate.
    ///
    /// @param predicate the row predicate
    /// @return the first accepted row, or `null` when none is accepted
    @Nullable Node findAttachedVisibleItem(Predicate<? super Node> predicate);
}
