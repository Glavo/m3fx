// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;

/// Launches the M3FX component catalog from an executable JAR or module launcher.
///
/// This indirection prevents the Java launcher from treating the entry point itself as a JavaFX
/// [javafx.application.Application] subclass.
@NotNullByDefault
public final class M3FXCatalogLauncher {
    /// Prevents launcher instantiation.
    private M3FXCatalogLauncher() {
    }

    /// Starts the M3FX component catalog.
    ///
    /// @param args command-line arguments forwarded to the catalog application
    public static void main(String[] args) {
        M3FXCatalogApp.main(args);
    }
}
