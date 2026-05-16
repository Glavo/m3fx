// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.application.Application;
import org.jetbrains.annotations.NotNullByDefault;

/// Launches the M3FX demo without extending [Application].
@NotNullByDefault
public final class M3FXDemoLauncher {
    /// Prevents launcher instantiation.
    private M3FXDemoLauncher() {
    }

    /// Starts the JavaFX demo application.
    public static void main(String[] args) {
        Application.launch(M3FXDemoApp.class, args);
    }
}
