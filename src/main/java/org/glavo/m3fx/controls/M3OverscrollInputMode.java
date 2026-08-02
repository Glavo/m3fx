// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;

/// Selects which user scroll inputs may be decorated by an [M3OverscrollEffect].
///
/// The modes use JavaFX scroll-event semantics rather than inferring a physical device. In particular, an indirect
/// gesture is continuous only when the platform emits `SCROLL_STARTED` and `SCROLL_FINISHED` around its movement
/// events. This commonly distinguishes precision touchpad gestures from isolated mouse-wheel events, but the exact
/// event sequence remains platform-dependent.
@NotNullByDefault
public enum M3OverscrollInputMode {
    /// Applies overscroll only to direct manipulation reported by [javafx.scene.input.ScrollEvent#isDirect()].
    ///
    /// JavaFX normally reports touchscreen scrolling as direct input. Indirect touchpads and mouse wheels do not
    /// receive an overscroll effect in this mode.
    DIRECT,

    /// Applies overscroll to direct manipulation and lifecycle-delimited indirect scroll gestures.
    ///
    /// Isolated indirect events, such as ordinary mouse-wheel notches on platforms that do not report a surrounding
    /// gesture lifecycle, retain bounded scrolling without an edge effect. This is the default mode.
    CONTINUOUS,

    /// Applies overscroll to every owned scroll event, including isolated indirect wheel input.
    ALL;

    /// Returns whether one event may be decorated under this mode.
    ///
    /// @param direct whether JavaFX identifies the event as direct input
    /// @param continuousGesture whether the event belongs to a lifecycle-delimited scroll gesture
    /// @return `true` when the configured effect may decorate the event
    boolean accepts(boolean direct, boolean continuousGesture) {
        return switch (this) {
            case DIRECT -> direct;
            case CONTINUOUS -> direct || continuousGesture;
            case ALL -> true;
        };
    }
}
