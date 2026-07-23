// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.application.Application;
import org.jetbrains.annotations.NotNullByDefault;

/// Launches the HMCL Material Design 3 demonstration application.
///
/// This indirection prevents the Java launcher from treating the entry point itself as a JavaFX
/// [Application] subclass.
@NotNullByDefault
public final class HMCLM3DemoLauncher {
    /// Prevents launcher instantiation.
    private HMCLM3DemoLauncher() {
    }

    /// Starts the HMCL Material Design 3 demonstration application.
    ///
    /// @param args command-line arguments forwarded to JavaFX
    public static void main(String[] args) {
        Application.launch(HMCLM3DemoApp.class, args);
    }
}
