// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3AnimatedVisibility;
import org.glavo.m3fx.controls.M3NavigationBar;
import org.glavo.m3fx.controls.M3NavigationItemLayout;
import org.glavo.m3fx.testing.Tier3Test;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that every demo page remains horizontally usable across responsive window widths.
///
/// The checks run in a real JavaFX window because ScrollPane sizing, FlowPane wrapping, and logical right-to-left
/// positioning differ from detached-node measurement. This suite validates the page shell rather than individual
/// control visuals; the visual matrix retains responsibility for control-specific rendering assertions.
@NotNullByDefault
@Tier3Test
final class M3FXDemoResponsiveContentTest {
    /// The number of stable JavaFX pulses required after a window resize.
    private static final int STABLE_PULSES = 3;

    /// The tolerance used for scene-coordinate horizontal containment checks.
    private static final double HORIZONTAL_TOLERANCE = 1.5;

    /// The minimum width required by the adaptive four-destination navigation bar's horizontal arrangement.
    private static final double ADAPTIVE_NAVIGATION_HORIZONTAL_WIDTH = 656.0;

    /// The stage widths used to cover compact, medium, expanded, and wide demo panes.
    private static final @Unmodifiable List<Double> LEFT_TO_RIGHT_WIDTHS = List.of(480.0, 720.0, 960.0, 1280.0);

    /// The stage widths that exercise compact and persistent-navigation right-to-left page layouts.
    private static final @Unmodifiable List<Double> RIGHT_TO_LEFT_WIDTHS = List.of(480.0, 1280.0);

    /// Starts the JavaFX toolkit before the responsive layout tests create native windows.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that every page fits the page viewport in each left-to-right responsive width.
    @Test
    void allPagesRemainHorizontallyContainedAcrossLeftToRightWidths() throws InterruptedException {
        DemoWindow window = openDemoWindow();
        try {
            for (double width : LEFT_TO_RIGHT_WIDTHS) {
                verifyResponsiveWidth(window, width, NodeOrientation.LEFT_TO_RIGHT);
            }
        } finally {
            closeDemoWindow(window);
        }
    }

    /// Verifies that page headers and showcase flows retain their logical layout in right-to-left windows.
    @Test
    void allPagesRemainHorizontallyContainedAcrossRightToLeftWidths() throws InterruptedException {
        DemoWindow window = openDemoWindow();
        try {
            for (double width : RIGHT_TO_LEFT_WIDTHS) {
                verifyResponsiveWidth(window, width, NodeOrientation.RIGHT_TO_LEFT);
            }
        } finally {
            closeDemoWindow(window);
        }
    }

    /// Opens one configured demo window for responsive content verification.
    ///
    /// @return the showing test window and its application instance
    /// @throws InterruptedException if the test thread is interrupted while waiting for JavaFX work
    private static DemoWindow openDemoWindow() throws InterruptedException {
        AtomicReference<@Nullable DemoWindow> windowReference = new AtomicReference<>();
        FxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            M3FXDemoApp application = new M3FXDemoApp();
            application.start(stage);
            stage.setWidth(1280.0);
            stage.setHeight(860.0);
            application.configurePresentation(M3Profile.BASELINE_2021, Brightness.LIGHT, false);
            windowReference.set(new DemoWindow(stage, application));
        });
        return Objects.requireNonNull(windowReference.get(), "demo window");
    }

    /// Closes one test window after its responsive checks finish.
    ///
    /// @param window the window to close
    /// @throws InterruptedException if the test thread is interrupted while waiting for JavaFX work
    private static void closeDemoWindow(DemoWindow window) throws InterruptedException {
        Objects.requireNonNull(window, "window");
        FxTestUtils.runOnFxThread(window.stage()::close);
    }

    /// Resizes a demo window and validates every registered page after the shell settles.
    ///
    /// @param window      the showing demo window
    /// @param width       the requested stage width in logical pixels
    /// @param orientation the global layout direction to verify
    /// @throws InterruptedException if the test thread is interrupted while awaiting stable JavaFX pulses
    private static void verifyResponsiveWidth(
            DemoWindow window,
            double width,
            NodeOrientation orientation
    ) throws InterruptedException {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(orientation, "orientation");
        FxTestUtils.runOnFxThreadWhenStable(
                () -> pageViewportIsReady(window.application()),
                STABLE_PULSES,
                () -> {
                    window.stage().setWidth(width);
                    Scene scene = requireScene(window.application());
                    scene.getRoot().setNodeOrientation(orientation);
                    layout(scene);
                },
                () -> {
                    for (String title : window.application().demoPageTitles()) {
                        window.application().showPageByTitle(title);
                        Scene scene = requireScene(window.application());
                        settlePageTransition(scene);
                        assertPageFitsViewport(scene, title, width, orientation);
                    }
                }
        );
    }

    /// Returns whether the current page ScrollPane has a laid-out viewport.
    ///
    /// @param application the active demo application
    /// @return whether the responsive page viewport is ready for inspection
    private static boolean pageViewportIsReady(M3FXDemoApp application) {
        Scene scene = requireScene(application);
        layout(scene);
        @Nullable ScrollPane pageScrollPane = findStyledNode(scene.getRoot(), "demo-scroll-pane", ScrollPane.class);
        return pageScrollPane != null
                && pageScrollPane.getViewportBounds().getWidth() > 0.0
                && pageScrollPane.getViewportBounds().getHeight() > 0.0;
    }

    /// Settles the current page replacement before inspecting its final layout bounds.
    ///
    /// @param scene the active demo scene
    private static void settlePageTransition(Scene scene) {
        M3AnimatedContent host = requireStyledNode(scene.getRoot(), "demo-page-host", M3AnimatedContent.class);
        host.finish();
        layout(scene);
    }

    /// Verifies the viewport, header, and showcase-flow bounds of one rendered demo page.
    ///
    /// @param scene       the scene containing the rendered page
    /// @param pageTitle   the inspected page title
    /// @param stageWidth  the current requested stage width
    /// @param orientation the active global layout direction
    private static void assertPageFitsViewport(
            Scene scene,
            String pageTitle,
            double stageWidth,
            NodeOrientation orientation
    ) {
        ScrollPane pageScrollPane = requireStyledNode(scene.getRoot(), "demo-scroll-pane", ScrollPane.class);
        Node page = requireStyledNode(scene.getRoot(), "demo-page", Node.class);
        Bounds viewportBounds = pageScrollPane.localToScene(pageScrollPane.getViewportBounds());
        Bounds pageBounds = page.localToScene(page.getLayoutBounds());
        String context = pageTitle + " at " + (int) stageWidth + "px " + orientation;

        assertWithinHorizontalBounds(pageBounds, viewportBounds, context + " page root");
        assertFalse(pageScrollPane.getHbarPolicy() == ScrollPane.ScrollBarPolicy.AS_NEEDED,
                context + " must not rely on an outer horizontal page scrollbar");

        DemoPageHeader header = requireStyledNode(scene.getRoot(), "demo-page-header", DemoPageHeader.class);
        assertHeaderFitsViewport(header, viewportBounds, orientation, context);
        assertShowcaseFlowsFitViewport(page, viewportBounds, context);
        assertMotionLayout(page, pageTitle, context);
        assertAdaptiveNavigationLayout(page, pageTitle, context);
    }

    /// Verifies that the page header remains visible and uses logical action placement.
    ///
    /// @param header      the rendered responsive page header
    /// @param viewport    the page viewport bounds in scene coordinates
    /// @param orientation the active layout direction
    /// @param context     the assertion context
    private static void assertHeaderFitsViewport(
            DemoPageHeader header,
            Bounds viewport,
            NodeOrientation orientation,
            String context
    ) {
        Node heading = requireStyledNode(header, "demo-page-heading", Node.class);
        Node documentationAction = requireStyledNode(header, "demo-page-doc-link", Node.class);
        Bounds headingBounds = heading.localToScene(heading.getBoundsInLocal());
        Bounds actionBounds = documentationAction.localToScene(documentationAction.getBoundsInLocal());
        assertWithinHorizontalBounds(headingBounds, viewport, context + " heading");
        assertWithinHorizontalBounds(actionBounds, viewport, context + " documentation action");

        boolean verticallyOverlapping = headingBounds.getMinY() < actionBounds.getMaxY() - HORIZONTAL_TOLERANCE
                && actionBounds.getMinY() < headingBounds.getMaxY() - HORIZONTAL_TOLERANCE;
        if (!verticallyOverlapping) {
            return;
        }

        boolean separate = headingBounds.getMaxX() <= actionBounds.getMinX() + HORIZONTAL_TOLERANCE
                || actionBounds.getMaxX() <= headingBounds.getMinX() + HORIZONTAL_TOLERANCE;
        assertTrue(separate, context + " header children must not overlap");
        if (orientation == NodeOrientation.RIGHT_TO_LEFT) {
            assertTrue(actionBounds.getCenterX() <= headingBounds.getCenterX(),
                    context + " documentation action should remain at logical end in RTL");
        } else {
            assertTrue(actionBounds.getCenterX() >= headingBounds.getCenterX(),
                    context + " documentation action should remain at logical end in LTR");
        }
    }

    /// Verifies that direct samples in responsive showcase flows stay within their allocated surface.
    ///
    /// @param page     the rendered page root
    /// @param viewport the outer page viewport in scene coordinates
    /// @param context  the assertion context
    private static void assertShowcaseFlowsFitViewport(Node page, Bounds viewport, String context) {
        for (FlowPane flow : responsiveFlows(page)) {
            Bounds flowBounds = flow.localToScene(flow.getLayoutBounds());
            assertWithinHorizontalBounds(flowBounds, viewport, context + " showcase flow");
            for (Node child : flow.getChildrenUnmodifiable()) {
                if (!child.isManaged() || !child.isVisible()) {
                    continue;
                }
                Bounds childBounds = child.localToScene(child.getLayoutBounds());
                assertWithinHorizontalBounds(childBounds, flowBounds, context + " showcase sample " + child);
            }
        }
    }

    /// Verifies that retained Motion examples receive a usable fitted size in their first stable layout.
    ///
    /// @param page      the current page root
    /// @param pageTitle the current page title
    /// @param context   the assertion context
    private static void assertMotionLayout(Node page, String pageTitle, String context) {
        if (!pageTitle.equals("Motion")) {
            return;
        }

        List<M3AnimatedVisibility> visibilities = page.lookupAll(".m3-animated-visibility").stream()
                .filter(M3AnimatedVisibility.class::isInstance)
                .map(M3AnimatedVisibility.class::cast)
                .toList();
        assertTrue(visibilities.size() == 1,
                context + " Motion page should contain one animated-visibility example");
        M3AnimatedVisibility visibility = visibilities.get(0);
        Bounds bounds = visibility.getLayoutBounds();
        assertTrue(bounds.getWidth() > 0.5 && bounds.getHeight() > 0.5,
                () -> context + " animated visibility has no first-stable-layout size: bounds=" + bounds
                        + ", prefHeight=" + visibility.prefHeight(visibility.getWidth()));
    }
    /// Verifies the navigation showcase selects the layout that fits its allocated bar width.
    ///
    /// @param page      the current page root
    /// @param pageTitle the current page title
    /// @param context   the assertion context
    private static void assertAdaptiveNavigationLayout(Node page, String pageTitle, String context) {
        if (!pageTitle.equals("Navigation")) {
            return;
        }

        List<M3NavigationBar> bars = page.lookupAll("." + "m3-navigation-bar").stream()
                .filter(M3NavigationBar.class::isInstance)
                .map(M3NavigationBar.class::cast)
                .toList();
        assertTrue(bars.size() >= 2, context + " navigation page should render the compact and adaptive bars");

        M3NavigationBar adaptiveBar = bars.get(1);
        M3NavigationItemLayout expectedLayout = adaptiveBar.getWidth() >= ADAPTIVE_NAVIGATION_HORIZONTAL_WIDTH
                ? M3NavigationItemLayout.HORIZONTAL
                : M3NavigationItemLayout.VERTICAL;
        assertTrue(adaptiveBar.getItemLayout() == expectedLayout,
                context + " adaptive navigation bar should use the layout matching its allocated width");
    }

    /// Returns all page flows whose direct children are expected to wrap inside the page surface.
    ///
    /// @param page the page subtree to inspect
    /// @return the responsive showcase and action flows
    private static @Unmodifiable List<FlowPane> responsiveFlows(Node page) {
        List<FlowPane> flows = new ArrayList<>();
        for (Node node : page.lookupAll(".demo-flow")) {
            if (node instanceof FlowPane flow) {
                flows.add(flow);
            }
        }
        for (Node node : page.lookupAll(".demo-action-row")) {
            if (node instanceof FlowPane flow && !flows.contains(flow)) {
                flows.add(flow);
            }
        }
        return List.copyOf(flows);
    }

    /// Verifies that one node's horizontal scene bounds are contained by an outer scene region.
    ///
    /// @param candidate the bounds to inspect
    /// @param container the containing bounds
    /// @param context   the assertion context
    private static void assertWithinHorizontalBounds(Bounds candidate, Bounds container, String context) {
        assertTrue(candidate.getMinX() >= container.getMinX() - HORIZONTAL_TOLERANCE,
                () -> context + " starts outside container: candidate=" + candidate + ", container=" + container);
        assertTrue(candidate.getMaxX() <= container.getMaxX() + HORIZONTAL_TOLERANCE,
                () -> context + " ends outside container: candidate=" + candidate + ", container=" + container);
    }

    /// Applies CSS and one synchronous layout pass to the scene root.
    ///
    /// @param scene the scene to lay out
    private static void layout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    /// Returns the current application scene.
    ///
    /// @param application the active demo application
    /// @return the active scene
    private static Scene requireScene(M3FXDemoApp application) {
        return Objects.requireNonNull(application.activeScene(), "demo scene");
    }

    /// Returns one styled node of the requested type.
    ///
    /// @param root       the subtree root
    /// @param styleClass the required style class
    /// @param type       the required node type
    /// @param <T>        the node type
    /// @return the matching node
    /// @throws AssertionError if no matching node is visible
    private static <T extends Node> T requireStyledNode(Node root, String styleClass, Class<T> type) {
        @Nullable T node = findStyledNode(root, styleClass, type);
        if (node == null) {
            throw new AssertionError("missing " + styleClass);
        }
        return node;
    }

    /// Returns one visible styled node of the requested type, or `null` when none exists.
    ///
    /// @param root       the subtree root
    /// @param styleClass the required style class
    /// @param type       the required node type
    /// @param <T>        the node type
    /// @return the matching node, or `null` when absent
    private static <T extends Node> @Nullable T findStyledNode(Node root, String styleClass, Class<T> type) {
        if (type.isInstance(root) && root.isVisible() && root.getStyleClass().contains(styleClass)) {
            return type.cast(root);
        }
        for (Node node : root.lookupAll("." + styleClass)) {
            if (type.isInstance(node) && node.isVisible()) {
                return type.cast(node);
            }
        }
        return null;
    }

    /// Holds the showing native window and its associated demo application.
    ///
    /// @param stage       the showing JavaFX stage
    /// @param application the application that owns the stage scene
    @NotNullByDefault
    private record DemoWindow(Stage stage, M3FXDemoApp application) {
    }
}
