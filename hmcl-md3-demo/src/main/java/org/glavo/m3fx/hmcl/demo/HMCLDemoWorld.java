// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// One in-memory saved world.
///
/// @param id stable identifier
/// @param name world name
/// @param gameMode survival/creative/etc.
/// @param lastPlayed last-played label
@NotNullByDefault
public record HMCLDemoWorld(String id, String name, String gameMode, String lastPlayed) {
}
