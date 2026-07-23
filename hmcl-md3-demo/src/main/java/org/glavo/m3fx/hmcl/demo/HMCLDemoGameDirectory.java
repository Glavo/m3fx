// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes one dummy Minecraft game directory shown in the instance list sidebar.
///
/// @param id the stable directory identifier
/// @param name the displayed directory name
/// @param path the displayed filesystem path
@NotNullByDefault
public record HMCLDemoGameDirectory(String id, String name, String path) {
}
