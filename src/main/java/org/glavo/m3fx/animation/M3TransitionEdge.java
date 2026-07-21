// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies the logical edge from which content enters or toward which content exits.
///
/// [#START] and [#END] follow the effective [javafx.geometry.NodeOrientation] of the
/// [M3AnimatedContent] that executes the transition. [#TOP] and [#BOTTOM] are independent of layout direction.
/// Distances measured from an edge are expressed in JavaFX logical pixels.
@NotNullByDefault
public enum M3TransitionEdge {
    /// The leading horizontal edge: left in left-to-right layout and right in right-to-left layout.
    START,

    /// The trailing horizontal edge: right in left-to-right layout and left in right-to-left layout.
    END,

    /// The top edge.
    TOP,

    /// The bottom edge.
    BOTTOM
}
