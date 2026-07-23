// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// One loader/component installer slot on an instance.
///
/// @param id installer family id
/// @param name display name
/// @param installedVersion installed version, or `null` when absent
@NotNullByDefault
public record HMCLDemoInstaller(String id, String name, @Nullable String installedVersion) {
    /// Returns whether an version is installed.
    public boolean isInstalled() {
        return installedVersion != null && !installedVersion.isBlank();
    }

    /// Returns a copy with a different installed version.
    public HMCLDemoInstaller withVersion(@Nullable String version) {
        return new HMCLDemoInstaller(id, name, version);
    }
}
