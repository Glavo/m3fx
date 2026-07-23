// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// One discovered Java runtime entry.
///
/// @param id stable identifier
/// @param name display name
/// @param version version label
/// @param path filesystem path label
/// @param architecture architecture label
@NotNullByDefault
public record HMCLDemoJavaRuntime(
        String id,
        String name,
        String version,
        String path,
        String architecture
) {
}
