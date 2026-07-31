// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.controls.M3ScrollPanes;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies page viewport ownership during animated Demo navigation.
@NotNullByDefault
@Tier2Test
final class M3FXDemoPageTransitionTest {
    /// Starts the JavaFX toolkit before tests create the Demo window.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that the outgoing page retains its scroll offset while the incoming page starts at the top.
    @Test
    void animatedSwitchKeepsOutgoingScrollPosition() {
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp application = new M3FXDemoApp();
            try {
                application.start(stage);
                stage.setWidth(1280.0);
                stage.setHeight(720.0);
                Scene scene = Objects.requireNonNull(application.activeScene(), "scene");
                application.showPageByTitle("App Bars");
                layout(scene);

                M3AnimatedContent host = pageHost(scene);
                ScrollPane outgoing = currentPageScrollPane(host);
                outgoing.setVvalue(0.75);
                layout(scene);
                assertEquals(0.75, outgoing.getVvalue(), 0.001);
                M3TopAppBar appBar = assertInstanceOf(
                        M3TopAppBar.class,
                        scene.getRoot().lookup(".demo-header"),
                        "Demo app bar"
                );
                assertTrue(appBar.isScrolledUnder());

                DemoPage target = application.demoPages().stream()
                        .filter(page -> page.title().equals("Typography"))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Typography demo page not found"));
                application.showPage(target);
                layout(scene);

                ScrollPane incoming = currentPageScrollPane(host);
                assertTrue(host.isTransitioning());
                assertNotSame(outgoing, incoming);
                assertSame(scene, outgoing.getScene());
                assertTrue(Objects.requireNonNull(
                        host.getContentTransform().sizeTransform(),
                        "Demo page size transform"
                ).clip());
                assertEquals(0.75, outgoing.getVvalue(), 0.001);
                assertEquals(incoming.getVmin(), incoming.getVvalue(), 0.001);
                assertFalse(appBar.isScrolledUnder());
                assertFalse(M3ScrollPanes.isSmoothScrollingEnabled(outgoing));
                assertTrue(M3ScrollPanes.isSmoothScrollingEnabled(incoming));
            } finally {
                stage.close();
            }
        });
    }

    /// Applies CSS and performs one synchronous scene layout pass.
    ///
    /// @param scene the scene to lay out
    private static void layout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    /// Returns the retained Demo page host.
    ///
    /// @param scene the Demo scene
    /// @return the page host
    private static M3AnimatedContent pageHost(Scene scene) {
        return assertInstanceOf(
                M3AnimatedContent.class,
                scene.getRoot().lookup(".demo-page-host"),
                "Demo page host"
        );
    }

    /// Returns the current page-owned scroll pane.
    ///
    /// @param host the retained page host
    /// @return the target page's scroll pane
    private static ScrollPane currentPageScrollPane(M3AnimatedContent host) {
        return assertInstanceOf(ScrollPane.class, host.getContent(), "current Demo page viewport");
    }
}
