// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies that demo-owned animations follow the native window presentation lifecycle.
@NotNullByDefault
@Tier2Test
final class M3FXDemoWindowActivityTest {
    /// Starts the JavaFX toolkit before the test opens a native window.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that page animations pause while the demo window is iconified and resume after restoration.
    @Test
    void pageAnimationsFollowIconification() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp application = new M3FXDemoApp();
            PauseTransition animation = new PauseTransition(Duration.minutes(1.0));

            try {
                application.start(stage);
                application.registerPageAnimation(animation);
                assertEquals(Animation.Status.RUNNING, animation.getStatus());

                stage.setIconified(true);
                assertEquals(Animation.Status.PAUSED, animation.getStatus());

                stage.setIconified(false);
                assertEquals(Animation.Status.RUNNING, animation.getStatus());
            } finally {
                animation.stop();
                stage.close();
            }
        });
    }
}
