// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.testing.Tier3Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /// Verifies wide and narrow navigation, global direction switching, and modal drawer dismissal as one workflow.
    @Test
    void globalDirectionAndAdaptiveNavigationRemainReachable() throws InterruptedException {
        AtomicReference<@Nullable Stage> stageReference = new AtomicReference<>();
        AtomicReference<@Nullable Scene> sceneReference = new AtomicReference<>();

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
            resizeToMediumLayout(stageReference, sceneReference);
            showAndDismissRightToLeftModalDrawer(sceneReference);
            restoreWideLeftToRightShell(stageReference, sceneReference);
        } finally {
            FxTestUtils.runOnFxThread(() -> {
                Stage stage = stageReference.get();
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
                            && sidebar.localToScene(sidebar.getBoundsInLocal()).getMinX() <= EDGE_TOLERANCE;
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
                            && sidebar.localToScene(sidebar.getBoundsInLocal()).getMaxX()
                            >= scene.getWidth() - EDGE_TOLERANCE;
                },
                STABLE_PULSES,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    M3Switch direction = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-direction-switch",
                            M3Switch.class
                    );
                    direction.fire();
                },
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    assertEquals(NodeOrientation.RIGHT_TO_LEFT, scene.getRoot().getEffectiveNodeOrientation());
                    M3Switch direction = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-direction-switch",
                            M3Switch.class
                    );
                    assertTrue(direction.isSelected(), "RTL switch should report the active global direction");
                    ScrollPane sidebar = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-sidebar-scroll-pane",
                            ScrollPane.class
                    );
                    assertSceneLeadingEdge(sidebar, scene, true, "RTL persistent drawer");
                }
        );
    }

    /// Resizes the window to the medium breakpoint and verifies the compact navigation entry point.
    ///
    /// @param stageReference the active-stage holder
    /// @param sceneReference the active-scene holder
    private static void resizeToMediumLayout(
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
                    stage.setWidth(720.0);
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
                }
        );
    }

    /// Opens the medium-layout modal drawer, verifies its RTL edge, and dismisses it through the scrim.
    ///
    /// @param sceneReference the active-scene holder
    private static void showAndDismissRightToLeftModalDrawer(
            AtomicReference<@Nullable Scene> sceneReference
    ) throws InterruptedException {
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
                    M3IconButton navigation = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-navigation-button",
                            M3IconButton.class
                    );
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
                    assertTrue(requireVisibleScrim(scene.getRoot()).isShown());
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
                            && sidebar.localToScene(sidebar.getBoundsInLocal()).getMinX() <= EDGE_TOLERANCE;
                },
                STABLE_PULSES,
                () -> {
                    Scene scene = Objects.requireNonNull(sceneReference.get(), "scene");
                    M3Switch direction = requireVisibleStyledNode(
                            scene.getRoot(),
                            "demo-direction-switch",
                            M3Switch.class
                    );
                    direction.fire();
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
                }
        );
    }

    /// Applies CSS and performs one synchronous scene-root layout pass.
    ///
    /// @param scene the scene to lay out
    private static void layout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
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
            if (node.isVisible() && type.isInstance(node)) {
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
