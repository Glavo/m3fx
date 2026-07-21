// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.testing.Tier3Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the responsive shell and global layout-direction workflow of the M3FX demo.
///
/// These tests use a showing native window because breakpoint transitions, overlay focus containment, and physical
/// leading-edge placement must be observed after JavaFX window layout has settled.
@NotNullByDefault
@Tier3Test
final class M3FXDemoAdaptiveShellTest {
    /// The number of consecutive JavaFX pulses required before a shell state is considered stable.
    private static final int STABLE_PULSES = 3;

    /// The tolerance used for scene-edge placement assertions.
    private static final double EDGE_TOLERANCE = 1.5;

    /// Starts the JavaFX toolkit before the adaptive shell opens native windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies wide and narrow navigation, global direction switching, and modal drawer dismissal as one workflow.
    @Test
    void globalDirectionAndAdaptiveNavigationRemainReachable() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();
        AtomicReference<@Nullable AnimationTimer> viewportMonitorReference = new AtomicReference<>();

        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(1180.0);
            stage.setHeight(820.0);
            stageReference.set(stage);
            sceneReference.set(Objects.requireNonNull(app.activeScene(), "scene"));
        });

        try {
            verifyWideLeftToRightShell(sceneReference);
            switchToRightToLeft(sceneReference);
            resizeToCompactLayout(stageReference, sceneReference);
            showAndDismissRightToLeftModalDrawer(sceneReference, viewportMonitorReference);
            restoreWideLeftToRightShell(stageReference, sceneReference);
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable AnimationTimer viewportMonitor = viewportMonitorReference.getAndSet(null);
                if (viewportMonitor != null) {
                    viewportMonitor.stop();
                }
                Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies that a pointer-opened modal drawer does not leave interaction feedback on its trigger.
    @Test
    void pointerDrawerLifecycleClearsTriggerInteractionFeedback() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp app = new M3FXDemoApp();
            app.start(stage);
            stage.setWidth(480.0);
            stage.setHeight(720.0);
            stageReference.set(stage);
            sceneReference.set(Objects.requireNonNull(app.activeScene(), "scene"));
        });

        try {
            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        layout(scene);
                        return visibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        ) != null;
                    },
                    STABLE_PULSES,
                    () -> {
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        M3IconButton settings = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-settings-button",
                                M3IconButton.class
                        );
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        settings.requestFocus();
                        assertTrue(settings.isFocused());
                        firePrimaryClick(navigation);
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        @Nullable M3Scrim scrim = visibleScrim(scene.getRoot());
                        @Nullable M3IconButton navigation = visibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        @Nullable Node stateLayer = navigation == null
                                ? null
                                : navigation.lookup(".m3-state-layer");
                        return scrim != null
                                && scrim.isShown()
                                && stateLayer != null
                                && stateLayer.getOpacity() <= 0.0001;
                    },
                    STABLE_PULSES,
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        @Nullable M3Scrim scrim = visibleScrim(scene.getRoot());
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        return "Timed out waiting for modal feedback suspension: scrim="
                                + (scrim == null ? "absent" : scrim.isShown())
                                + ", opacity=" + navigation.lookup(".m3-state-layer").getOpacity()
                                + ", focused=" + navigation.isFocused()
                                + ", pressed=" + navigation.isPressed()
                                + ", armed=" + navigation.isArmed()
                                + ", pseudoClasses=" + navigation.getPseudoClassStates();
                    },
                    () -> {
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        assertFalse(navigation.isPressed());
                        assertFalse(navigation.isArmed());
                        assertEquals(0.0, navigation.lookup(".m3-state-layer").getOpacity(), 0.0001);
                        firePrimaryClick(requireVisibleScrim(scene.getRoot()));
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        return visibleScrim(scene.getRoot()) == null
                                && visibleStyledNode(
                                scene.getRoot(),
                                "demo-modal-sidebar-scroll-pane",
                                ScrollPane.class
                        ) == null
                                && navigation.lookup(".m3-state-layer").getOpacity() <= 0.0001;
                    },
                    STABLE_PULSES,
                    () -> {
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        assertTrue(navigation.isFocused(), "modal dismissal should restore its pointer trigger");
                        assertFalse(navigation.getPseudoClassStates().contains(
                                PseudoClass.getPseudoClass("focus-visible")
                        ));
                        assertEquals(0.0, navigation.lookup(".m3-state-layer").getOpacity(), 0.0001);
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        layout(scene);
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        @Nullable Node stateLayer = navigation.lookup(".m3-state-layer");
                        @Nullable Node ripple = navigation.lookup(".m3-ripple");
                        @Nullable Node focusIndicator = navigation.lookup(".m3-focus-indicator");
                        return !navigation.isFocused()
                                && stateLayer != null
                                && stateLayer.getOpacity() <= 0.0001
                                && ripple != null
                                && ripple.getOpacity() <= 0.0001
                                && focusIndicator != null
                                && focusIndicator.getOpacity() <= 0.0001;
                    },
                    STABLE_PULSES,
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        M3IconButton settings = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-settings-button",
                                M3IconButton.class
                        );
                        return "Timed out waiting for navigation feedback to clear after focus transfer: "
                                + "navigationFocused=" + navigation.isFocused()
                                + ", settingsFocused=" + settings.isFocused()
                                + ", stateOpacity=" + navigation.lookup(".m3-state-layer").getOpacity()
                                + ", rippleOpacity=" + navigation.lookup(".m3-ripple").getOpacity()
                                + ", focusIndicatorOpacity="
                                + navigation.lookup(".m3-focus-indicator").getOpacity()
                                + ", pseudoClasses=" + navigation.getPseudoClassStates();
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        M3IconButton settings = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-settings-button",
                                M3IconButton.class
                        );
                        settings.requestFocus();
                    },
                    () -> {
                        Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                        M3IconButton navigation = requireVisibleStyledNode(
                                scene.getRoot(),
                                "demo-navigation-button",
                                M3IconButton.class
                        );
                        assertFalse(navigation.isFocused());
                        assertFalse(navigation.isPressed());
                        assertFalse(navigation.isArmed());
                        assertFalse(navigation.getPseudoClassStates().contains(
                                PseudoClass.getPseudoClass("focus-visible")
                        ));
                        assertEquals(0.0, navigation.lookup(".m3-state-layer").getOpacity(), 0.0001);
                        assertEquals(0.0, navigation.lookup(".m3-ripple").getOpacity(), 0.0001);
                        assertEquals(0.0, navigation.lookup(".m3-focus-indicator").getOpacity(), 0.0001);
                    }
            );
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                @Nullable Stage stage = stageReference.get();
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    /// Verifies the initial expanded shell and its persistent leading drawer.
    ///
    /// @param sceneReference the active-scene holder
    private static void verifyWideLeftToRightShell(
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    layout(scene);
                    @Nullable ScrollPane sidebar =
                            visibleStyledNode(scene.getRoot(), "demo-sidebar-scroll-pane", ScrollPane.class);
                    @Nullable M3IconButton navigation =
                            visibleStyledNode(scene.getRoot(), "demo-navigation-button", M3IconButton.class);
                    return sidebar != null
                            && navigation == null
                            && Math.abs(sidebar.localToScene(sidebar.getBoundsInLocal()).getMinX()) <= EDGE_TOLERANCE;
                },
                STABLE_PULSES,
                () -> {
                },
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertEquals(NodeOrientation.LEFT_TO_RIGHT, scene.getRoot().getEffectiveNodeOrientation());
                    ScrollPane sidebar = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-sidebar-scroll-pane",
                            ScrollPane.class
                    );
                    assertSceneLeadingEdge(sidebar, scene, false, "LTR persistent drawer");
                    assertDrawerItemLogicalStart(sidebar, false, "LTR persistent drawer item");
                    assertNull(visibleStyledNode(
                            scene.getRoot(),
                            "demo-navigation-button",
                            M3IconButton.class
                    ));
                }
        );
    }

    /// Activates the global direction switch and verifies that the persistent drawer mirrors.
    ///
    /// @param sceneReference the active-scene holder
    private static void switchToRightToLeft(
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    layout(scene);
                    @Nullable ScrollPane sidebar =
                            visibleStyledNode(scene.getRoot(), "demo-sidebar-scroll-pane", ScrollPane.class);
                    return scene.getRoot().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT
                            && sidebar != null
                            && Math.abs(sidebar.localToScene(sidebar.getBoundsInLocal()).getMaxX() - scene.getWidth())
                            <= EDGE_TOLERANCE;
                },
                STABLE_PULSES,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    setRightToLeftFromSettings(scene, true);
                },
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertEquals(NodeOrientation.RIGHT_TO_LEFT, scene.getRoot().getEffectiveNodeOrientation());
                    ScrollPane sidebar = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-sidebar-scroll-pane",
                            ScrollPane.class
                    );
                    assertSceneLeadingEdge(sidebar, scene, true, "RTL persistent drawer");
                    assertDrawerItemLogicalStart(sidebar, true, "RTL persistent drawer item");
                }
        );
    }

    /// Resizes the window to the compact breakpoint and verifies the modal navigation entry point.
    ///
    /// @param stageReference the active-stage holder
    /// @param sceneReference the active-scene holder
    private static void resizeToCompactLayout(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    layout(scene);
                    return visibleStyledNode(
                            scene.getRoot(),
                            "demo-navigation-button",
                            M3IconButton.class
                    ) != null
                            && visibleStyledNode(
                            scene.getRoot(),
                            "demo-sidebar-scroll-pane",
                            ScrollPane.class
                    ) == null;
                },
                STABLE_PULSES,
                () -> {
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    stage.setWidth(480.0);
                },
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertNotNull(visibleStyledNode(
                            scene.getRoot(),
                            "demo-navigation-button",
                            M3IconButton.class
                    ));
                    assertNull(visibleStyledNode(
                            scene.getRoot(),
                            "demo-sidebar-scroll-pane",
                            ScrollPane.class
                    ));
                    ScrollPane pageScrollPane = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-scroll-pane",
                            ScrollPane.class
                    );
                    pageScrollPane.setHvalue(pageScrollPane.getHmin());
                    assertEquals(pageScrollPane.getHmin(), pageScrollPane.getHvalue(), 0.0001);
                }
        );
    }

    /// Opens the compact-layout modal drawer, verifies its RTL edge, and dismisses it through the scrim.
    ///
    /// @param sceneReference           the active-scene holder
    /// @param viewportMonitorReference the holder used to stop the per-frame viewport monitor during cleanup
    private static void showAndDismissRightToLeftModalDrawer(
            AtomicReference<@Nullable Scene> sceneReference,
            AtomicReference<@Nullable AnimationTimer> viewportMonitorReference
    ) throws InterruptedException {
        AtomicBoolean horizontalPositionChanged = new AtomicBoolean();
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    layout(scene);
                    @Nullable ScrollPane sidebar = visibleStyledNode(
                            scene.getRoot(),
                            "demo-modal-sidebar-scroll-pane",
                            ScrollPane.class
                    );
                    @Nullable M3Scrim scrim = visibleScrim(scene.getRoot());
                    return sidebar != null
                            && scrim != null
                            && scrim.isShown()
                            && Math.abs(sidebar.getTranslateX()) <= EDGE_TOLERANCE;
                },
                STABLE_PULSES,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    M3IconButton settings = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-settings-button",
                            M3IconButton.class
                    );
                    M3IconButton navigation = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-navigation-button",
                            M3IconButton.class
                    );
                    settings.requestFocus();
                    assertTrue(settings.isFocused());
                    navigation.fire();
                },
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    ScrollPane sidebar = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-modal-sidebar-scroll-pane",
                            ScrollPane.class
                    );
                    assertSceneLeadingEdge(sidebar, scene, true, "RTL modal drawer");
                    assertEquals(NodeOrientation.RIGHT_TO_LEFT, sidebar.getEffectiveNodeOrientation());
                    assertDrawerItemLogicalStart(sidebar, true, "RTL modal drawer item");
                    assertTrue(requireVisibleScrim(scene.getRoot()).isShown());
                    ScrollPane pageScrollPane = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-scroll-pane",
                            ScrollPane.class
                    );
                    assertEquals(
                            pageScrollPane.getHmin(),
                            pageScrollPane.getHvalue(),
                            0.0001,
                            "opening the modal drawer must preserve horizontal page position"
                    );
                    double horizontalPosition = pageScrollPane.getHvalue();
                    AnimationTimer viewportMonitor = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (Math.abs(pageScrollPane.getHvalue() - horizontalPosition) > 0.0001) {
                                horizontalPositionChanged.set(true);
                            }
                        }
                    };
                    viewportMonitorReference.set(viewportMonitor);
                    viewportMonitor.start();
                }
        );

        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    layout(scene);
                    return visibleStyledNode(
                            scene.getRoot(),
                            "demo-modal-sidebar-scroll-pane",
                            ScrollPane.class
                    ) == null
                            && visibleScrim(scene.getRoot()) == null;
                },
                STABLE_PULSES,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    requireVisibleScrim(scene.getRoot()).fire();
                },
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertNull(visibleStyledNode(
                            scene.getRoot(),
                            "demo-modal-sidebar-scroll-pane",
                            ScrollPane.class
                    ));
                    assertNull(visibleScrim(scene.getRoot()));
                    ScrollPane pageScrollPane = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-scroll-pane",
                            ScrollPane.class
                    );
                    assertEquals(
                            pageScrollPane.getHmin(),
                            pageScrollPane.getHvalue(),
                            0.0001,
                            "closing the modal drawer must preserve horizontal page position"
                    );
                    @Nullable AnimationTimer viewportMonitor = viewportMonitorReference.getAndSet(null);
                    if (viewportMonitor != null) {
                        viewportMonitor.stop();
                    }
                    assertFalse(
                            horizontalPositionChanged.get(),
                            "the modal drawer lifecycle must not expose an intermediate horizontal page jump"
                    );
                    M3IconButton navigation = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-navigation-button",
                            M3IconButton.class
                    );
                    M3IconButton settings = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-settings-button",
                            M3IconButton.class
                    );
                    assertTrue(settings.isFocused(), "closing the modal drawer must restore the actual prior focus");
                    assertFalse(navigation.isFocused(), "opening the drawer must not fabricate menu-button focus");
                    assertFalse(
                            navigation.getPseudoClassStates().contains(PseudoClass.getPseudoClass("focus-visible")),
                            "the menu button must not retain keyboard-visible focus feedback"
                    );
                }
        );
    }

    /// Restores LTR and expanded width and verifies the persistent drawer returns without reconstructing navigation.
    ///
    /// @param stageReference the active-stage holder
    /// @param sceneReference the active-scene holder
    private static void restoreWideLeftToRightShell(
            AtomicReference<@Nullable Stage> stageReference,
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
        FxTestUtils.runOnFxThreadWhenStable(
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    layout(scene);
                    @Nullable ScrollPane sidebar =
                            visibleStyledNode(scene.getRoot(), "demo-sidebar-scroll-pane", ScrollPane.class);
                    return scene.getRoot().getEffectiveNodeOrientation() == NodeOrientation.LEFT_TO_RIGHT
                            && sidebar != null
                            && Math.abs(sidebar.localToScene(sidebar.getBoundsInLocal()).getMinX()) <= EDGE_TOLERANCE;
                },
                STABLE_PULSES,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    setRightToLeftFromSettings(scene, false);
                    Stage stage = Objects.requireNonNull(stageReference.get(), "stage");
                    stage.setWidth(1180.0);
                },
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertEquals(NodeOrientation.LEFT_TO_RIGHT, scene.getRoot().getEffectiveNodeOrientation());
                    assertSceneLeadingEdge(
                            requireVisibleStyledNode(
                                    scene.getRoot(),
                                    "demo-sidebar-scroll-pane",
                                    ScrollPane.class
                            ),
                            scene,
                            false,
                            "restored LTR persistent drawer"
                    );
                    assertDrawerItemLogicalStart(
                            requireVisibleStyledNode(
                                    scene.getRoot(),
                                    "demo-sidebar-scroll-pane",
                                    ScrollPane.class
                            ),
                            false,
                            "restored LTR persistent drawer item"
                    );
                }
        );
    }

    /// Changes the global layout direction through the settings dialog and then closes the dialog.
    ///
    /// @param scene       the active demo scene
    /// @param rightToLeft whether the demo should use right-to-left layout
    private static void setRightToLeftFromSettings(Scene scene, boolean rightToLeft) {
        M3IconButton settings = requireVisibleStyledNode(
                scene.getRoot(),
                "demo-settings-button",
                M3IconButton.class
        );
        settings.fire();
        layout(scene);

        M3Switch direction = requireVisibleStyledNode(
                scene.getRoot(),
                "demo-direction-switch",
                M3Switch.class
        );
        if (direction.isSelected() != rightToLeft) {
            direction.fire();
            layout(scene);
        }

        M3Button done = Objects.requireNonNull(
                findNode(scene.getRoot(), M3Button.class, button -> "Done".equals(button.getText())),
                "visible Done button in demo settings"
        );
        done.fire();
        layout(scene);
    }

    /// Applies CSS and performs one synchronous scene-root layout pass.
    ///
    /// @param scene the scene to lay out
    private static void layout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    /// Fires one complete synthetic primary-button click at the center of a node.
    ///
    /// @param node the event target
    private static void firePrimaryClick(Node node) {
        Bounds bounds = node.getBoundsInLocal();
        double x = bounds.getMinX() + bounds.getWidth() / 2.0;
        double y = bounds.getMinY() + bounds.getHeight() / 2.0;
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_PRESSED, x, y, true));
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_RELEASED, x, y, false));
        node.fireEvent(primaryMouseEvent(node, MouseEvent.MOUSE_CLICKED, x, y, false));
    }

    /// Creates a primary-button mouse event at one local point of a node.
    ///
    /// @param node              the event target
    /// @param eventType         the mouse event type
    /// @param x                 the local horizontal coordinate
    /// @param y                 the local vertical coordinate
    /// @param primaryButtonDown whether the primary button is held
    /// @return the mouse event
    private static MouseEvent primaryMouseEvent(
            Node node,
            EventType<MouseEvent> eventType,
            double x,
            double y,
            boolean primaryButtonDown
    ) {
        Point2D scenePoint = node.localToScene(x, y);
        @Nullable Point2D screenPoint = node.localToScreen(x, y);
        double screenX = screenPoint == null ? scenePoint.getX() : screenPoint.getX();
        double screenY = screenPoint == null ? scenePoint.getY() : screenPoint.getY();
        return new MouseEvent(
                eventType,
                x,
                y,
                screenX,
                screenY,
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                primaryButtonDown,
                false,
                false,
                false,
                false,
                false,
                new PickResult(node, scenePoint.getX(), scenePoint.getY())
        );
    }

    /// Verifies that a drawer touches the logical leading edge of the scene.
    ///
    /// @param drawer      the drawer node
    /// @param scene       the containing scene
    /// @param rightToLeft whether the logical leading edge is the physical right edge
    /// @param description the assertion description
    private static void assertSceneLeadingEdge(
            Node drawer,
            Scene scene,
            boolean rightToLeft,
            String description
    ) {
        Bounds bounds = drawer.localToScene(drawer.getBoundsInLocal());
        if (rightToLeft) {
            assertTrue(
                    Math.abs(bounds.getMaxX() - scene.getWidth()) <= EDGE_TOLERANCE,
                    () -> description + " should touch the scene right edge: " + bounds
            );
        } else {
            assertTrue(
                    Math.abs(bounds.getMinX()) <= EDGE_TOLERANCE,
                    () -> description + " should touch the scene left edge: " + bounds
            );
        }
    }

    /// Verifies that a drawer destination aligns its text to the current logical start edge.
    ///
    /// @param drawer      the drawer viewport
    /// @param rightToLeft whether the logical start edge is the physical right edge
    /// @param description the assertion description
    private static void assertDrawerItemLogicalStart(
            ScrollPane drawer,
            boolean rightToLeft,
            String description
    ) {
        M3ListItem item = requireVisibleStyledNode(drawer, "demo-sidebar-top-item", M3ListItem.class);
        VBox textBox = requireVisibleStyledNode(item, "m3-list-item-text", VBox.class);
        Node headline = requireVisibleStyledNode(item, "m3-list-item-headline", Node.class);
        NodeOrientation expectedOrientation = rightToLeft
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT;
        Pos expectedAlignment = Pos.CENTER_LEFT;

        assertEquals(expectedOrientation, item.getEffectiveNodeOrientation(),
                description + " should inherit the drawer direction");
        assertEquals(expectedAlignment, textBox.getAlignment(),
                description + " should align its text column to logical start");

        Bounds itemBounds = item.localToScene(item.getBoundsInLocal());
        Bounds headlineBounds = headline.localToScene(headline.getBoundsInLocal());
        double actualStartInset = rightToLeft
                ? itemBounds.getMaxX() - headlineBounds.getMaxX()
                : headlineBounds.getMinX() - itemBounds.getMinX();
        assertEquals(item.getHorizontalPadding(), actualStartInset, EDGE_TOLERANCE,
                description + " should preserve horizontal padding at logical start");
    }

    /// Returns a visible node with the requested style class and type.
    ///
    /// @param root       the subtree root
    /// @param styleClass the required style class
    /// @param type       the required node type
    /// @param <T>        the node type
    /// @return the first matching visible node, or `null` when absent
    private static <T extends Node> @Nullable T visibleStyledNode(
            Node root,
            String styleClass,
            Class<T> type
    ) {
        return findNode(root, type, node -> node.getStyleClass().contains(styleClass));
    }

    /// Returns the first visible modal scrim in a subtree.
    ///
    /// @param root the subtree root
    /// @return the first visible modal scrim, or `null` when absent
    private static @Nullable M3Scrim visibleScrim(Node root) {
        return findNode(root, M3Scrim.class, node -> true);
    }

    /// Returns a required visible node with the requested style class and type.
    ///
    /// @param root       the subtree root
    /// @param styleClass the required style class
    /// @param type       the required node type
    /// @param <T>        the node type
    /// @return the matching visible node
    private static <T extends Node> T requireVisibleStyledNode(
            Node root,
            String styleClass,
            Class<T> type
    ) {
        return Objects.requireNonNull(
                visibleStyledNode(root, styleClass, type),
                "visible node with style class " + styleClass
        );
    }

    /// Returns the required visible modal scrim.
    ///
    /// @param root the subtree root
    /// @return the visible modal scrim
    private static M3Scrim requireVisibleScrim(Node root) {
        return Objects.requireNonNull(visibleScrim(root), "visible modal scrim");
    }

    /// Traverses a subtree in breadth-first order and returns the first visible matching node.
    ///
    /// @param root      the subtree root
    /// @param type      the required node type
    /// @param predicate the additional node predicate
    /// @param <T>       the node type
    /// @return the first matching visible node, or `null` when absent
    private static <T extends Node> @Nullable T findNode(
            Node root,
            Class<T> type,
            java.util.function.Predicate<? super T> predicate
    ) {
        ArrayDeque<Node> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty()) {
            Node node = pending.removeFirst();
            if (!node.isVisible()) {
                continue;
            }
            if (type.isInstance(node)) {
                T candidate = type.cast(node);
                if (predicate.test(candidate)) {
                    return candidate;
                }
            }
            if (node instanceof Parent parent) {
                pending.addAll(parent.getChildrenUnmodifiable());
            }
        }
        return null;
    }
}
