// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3TopAppBar;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies page viewport ownership during Demo navigation and presentation changes.
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
                assertFalse(M3ScrollPane.isSmoothScrollingEnabled(outgoing));
                assertTrue(M3ScrollPane.isSmoothScrollingEnabled(incoming));
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that changing the animation preference updates motion in place without recreating the active page.
    @Test
    void animationPreferencePreservesCurrentPageViewport() {
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
                ScrollPane viewport = currentPageScrollPane(host);
                Node pageContent = Objects.requireNonNull(viewport.getContent(), "page content");
                viewport.setVvalue(0.75);
                layout(scene);
                assertEquals(0.75, viewport.getVvalue(), 0.001);

                M3IconButton settingsButton = assertInstanceOf(
                        M3IconButton.class,
                        scene.getRoot().lookup(".demo-settings-button"),
                        "Demo settings button"
                );
                settingsButton.fire();
                layout(scene);

                M3Switch animationsSwitch = assertInstanceOf(
                        M3Switch.class,
                        scene.getRoot().lookup(".demo-animations-switch"),
                        "Demo animations switch"
                );
                assertTrue(animationsSwitch.isSelected());

                animationsSwitch.fire();
                layout(scene);
                assertFalse(animationsSwitch.isSelected());
                assertTrue(M3MotionSettings.shouldReduceMotion(scene.getRoot()));
                assertSame(viewport, currentPageScrollPane(host));
                assertSame(pageContent, viewport.getContent());
                assertEquals(0.75, viewport.getVvalue(), 0.001);

                animationsSwitch.fire();
                layout(scene);
                assertTrue(animationsSwitch.isSelected());
                assertFalse(M3MotionSettings.shouldReduceMotion(scene.getRoot()));
                assertSame(viewport, currentPageScrollPane(host));
                assertSame(pageContent, viewport.getContent());
                assertEquals(0.75, viewport.getVvalue(), 0.001);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies that a short Demo window scrolls settings content while retaining the dialog action row.
    @Test
    void settingsDialogKeepsActionsVisibleInShortWindows() {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp application = new M3FXDemoApp();
            application.start(stage);
            stage.setWidth(400.0);
            stage.setHeight(580.0);
            stageReference.set(stage);
            sceneReference.set(Objects.requireNonNull(application.activeScene(), "scene"));
        });

        try {
            FxTestUtils.runOnFxThread(() -> {
                Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                layout(scene);

                M3IconButton settingsButton = assertInstanceOf(
                        M3IconButton.class,
                        scene.getRoot().lookup(".demo-settings-button"),
                        "Demo settings button"
                );
                settingsButton.fire();
                layout(scene);

                M3DialogPane dialogPane = assertInstanceOf(
                        M3DialogPane.class,
                        scene.getRoot().lookup(".m3-dialog-pane"),
                        "Demo settings dialog"
                );
                M3ScrollPane settingsViewport = assertInstanceOf(
                        M3ScrollPane.class,
                        scene.getRoot().lookup(".demo-settings-scroll-pane"),
                        "Demo settings viewport"
                );
                Node settingsContent = Objects.requireNonNull(settingsViewport.getContent(), "settings content");
                Node viewport = Objects.requireNonNull(settingsViewport.lookup(".viewport"), "scroll viewport");
                M3Button done = assertInstanceOf(M3Button.class, dialogPane.getActions().get(0), "Done action");

                Bounds paneBounds = dialogPane.localToScene(dialogPane.getBoundsInLocal());
                Bounds doneBounds = done.localToScene(done.getBoundsInLocal());
                assertTrue(paneBounds.contains(doneBounds), "Done action must remain inside the dialog surface");
                assertTrue(
                        settingsContent.getBoundsInParent().getHeight() > viewport.getBoundsInParent().getHeight(),
                        () -> "Short windows must constrain settings to a scrollable viewport: content="
                                + settingsContent.getBoundsInParent().getHeight()
                                + ", viewport=" + viewport.getBoundsInParent().getHeight()
                                + ", scrollPane=" + settingsViewport.getHeight()
                );

                settingsViewport.setVvalue(settingsViewport.getVmax());
                layout(scene);
                M3Switch darkTheme = assertInstanceOf(
                        M3Switch.class,
                        settingsContent.lookup(".demo-brightness-switch"),
                        "Dark theme switch"
                );
                Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
                Bounds darkThemeBounds = darkTheme.localToScene(darkTheme.getBoundsInLocal());
                assertTrue(
                        darkThemeBounds.getMinY() >= viewportBounds.getMinY() - 1.0
                                && darkThemeBounds.getMaxY() <= viewportBounds.getMaxY() + 1.0,
                        () -> "Dark theme switch must scroll fully into view: switch=" + darkThemeBounds
                                + ", viewport=" + viewportBounds
                );
                assertTrue(
                        paneBounds.contains(done.localToScene(done.getBoundsInLocal())),
                        "Scrolling settings must not move the Done action"
                );
            });
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
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
