// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the observable lifecycle state of an [M3AnimatedVisibility] region.
///
/// The state reports the content's current visual lifecycle rather than merely repeating the target value of
/// [M3AnimatedVisibility#showingProperty()]. During an interrupted transition it changes immediately to the state
/// corresponding to the newest target. The stable states indicate whether the content is presented.
@NotNullByDefault
public enum M3VisibilityState {
    /// The content is not presented and contributes no content size to the region.
    HIDDEN,

    /// The content is moving toward its fully visible visual and layout state.
    ENTERING,

    /// The content has reached its fully visible visual state.
    VISIBLE,

    /// The content is moving toward its hidden visual and layout state.
    EXITING
}
