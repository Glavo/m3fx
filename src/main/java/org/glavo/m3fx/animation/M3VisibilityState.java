// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes the observable lifecycle state of an [M3AnimatedVisibility] region.
///
/// The state reports the content's current visual lifecycle rather than merely repeating the target value of
/// [M3AnimatedVisibility#showingProperty()]. During an interrupted transition it changes immediately to the state
/// corresponding to the newest target. The stable states indicate whether the content is attached to the private
/// scene graph owned by the region.
@NotNullByDefault
public enum M3VisibilityState {
    /// The content is detached and contributes no content size to the region.
    HIDDEN,

    /// The content is attached and moving toward its fully visible visual and layout state.
    ENTERING,

    /// The content is attached and has reached its fully visible visual state.
    VISIBLE,

    /// The content remains attached while moving toward its hidden visual and layout state.
    EXITING
}
