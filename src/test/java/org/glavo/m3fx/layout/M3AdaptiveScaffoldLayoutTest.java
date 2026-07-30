// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies scaffold geometry, logical direction, and stable slot attachment in an offscreen scene graph.
///
/// These tests deliberately create a [Scene] without a [javafx.stage.Stage]. They exercise CSS and skin layout
/// while remaining deterministic Tier 1 tests that do not depend on a native window manager.
@NotNullByDefault
final class M3AdaptiveScaffoldLayoutTest {
    /// Starts the JavaFX toolkit before controls are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that every installed slot remains attached to one stable parent across adaptive transitions.
    @Test
    void keepsSlotNodesAttachedToStableParents() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane topBar = fixedHeightPane(64.0);
            Pane bottomBar = fixedHeightPane(48.0);
            Pane navigationBar = fixedHeightPane(80.0);
            Pane navigationRail = fixedWidthPane(80.0);
            Pane trailingRail = fixedWidthPane(72.0);
            Pane leadingPane = new Pane();
            Pane mainPane = new Pane();
            Pane trailingPane = new Pane();
            mainPane.setUserData("preserved");

            scaffold.setTopBar(topBar);
            scaffold.setBottomBar(bottomBar);
            scaffold.setNavigationBar(navigationBar);
            scaffold.setNavigationRail(navigationRail);
            scaffold.setTrailingRail(trailingRail);
            scaffold.setLeadingPane(leadingPane);
            scaffold.setMainPane(mainPane);
            scaffold.setTrailingPane(trailingPane);

            Pane root = installForLayout(scaffold, 500.0, 700.0);
            Parent topBarParent = requireParent(topBar);
            Parent bottomBarParent = requireParent(bottomBar);
            Parent navigationBarParent = requireParent(navigationBar);
            Parent navigationRailParent = requireParent(navigationRail);
            Parent trailingRailParent = requireParent(trailingRail);
            Parent leadingPaneParent = requireParent(leadingPane);
            Parent mainPaneParent = requireParent(mainPane);
            Parent trailingPaneParent = requireParent(trailingPane);

            layoutScaffold(root, scaffold, 900.0, 700.0);
            scaffold.setActivePane(M3PaneRole.TRAILING);
            layoutScaffold(root, scaffold, 1_700.0, 700.0);
            scaffold.setPaneLayout(M3PaneLayout.THREE_PANE);
            layoutScaffold(root, scaffold, 1_700.0, 700.0);

            assertSame(topBarParent, topBar.getParent());
            assertSame(bottomBarParent, bottomBar.getParent());
            assertSame(navigationBarParent, navigationBar.getParent());
            assertSame(navigationRailParent, navigationRail.getParent());
            assertSame(trailingRailParent, trailingRail.getParent());
            assertSame(leadingPaneParent, leadingPane.getParent());
            assertSame(mainPaneParent, mainPane.getParent());
            assertSame(trailingPaneParent, trailingPane.getParent());
            assertEquals("preserved", mainPane.getUserData());
        });
    }

    /// Verifies safety insets and logical content margins for a single visible pane.
    @Test
    void appliesSafetyInsetsAndContentMarginsToSinglePane() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane mainPane = new Pane();
            scaffold.setMainPane(mainPane);
            scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            scaffold.setContentMargin(24.0);
            scaffold.setSafetyInsets(new Insets(10.0, 20.0, 30.0, 40.0));

            installForLayout(scaffold, 800.0, 500.0);
            Bounds bounds = sceneBounds(mainPane);

            assertEquals(64.0, bounds.getMinX(), 0.001);
            assertEquals(10.0, bounds.getMinY(), 0.001);
            assertEquals(692.0, bounds.getWidth(), 0.001);
            assertEquals(460.0, bounds.getHeight(), 0.001);
        });
    }

    /// Verifies that asymmetric safety insets retain their physical sides under right-to-left orientation.
    @Test
    void keepsSafetyInsetsPhysicalInRightToLeftOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane mainPane = new Pane();
            scaffold.setMainPane(mainPane);
            scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            scaffold.setContentMargin(0.0);
            scaffold.setSafetyInsets(new Insets(10.0, 20.0, 30.0, 40.0));
            scaffold.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            installForLayout(scaffold, 800.0, 500.0);
            Bounds bounds = sceneBounds(mainPane);

            assertEquals(40.0, bounds.getMinX(), 0.001);
            assertEquals(780.0, bounds.getMaxX(), 0.001);
            assertEquals(10.0, bounds.getMinY(), 0.001);
            assertEquals(460.0, bounds.getHeight(), 0.001);
        });
    }

    /// Verifies equal split geometry and logical leading placement in both node orientations.
    @Test
    void mirrorsSplitPanesForRightToLeftOrientation() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane leadingPane = new Pane();
            Pane mainPane = new Pane();
            scaffold.setLeadingPane(leadingPane);
            scaffold.setMainPane(mainPane);
            scaffold.setPaneLayout(M3PaneLayout.SPLIT_LEADING);
            scaffold.setNavigationLayout(M3NavigationLayout.NONE);
            scaffold.setContentMargin(24.0);
            scaffold.setPaneSpacing(20.0);

            Pane root = installForLayout(scaffold, 1_000.0, 600.0);
            Bounds leftToRightLeading = sceneBounds(leadingPane);
            Bounds leftToRightMain = sceneBounds(mainPane);
            assertEquals(24.0, leftToRightLeading.getMinX(), 0.001);
            assertEquals(976.0, leftToRightMain.getMaxX(), 0.001);
            assertEquals(20.0, leftToRightMain.getMinX() - leftToRightLeading.getMaxX(), 0.001);
            assertEquals(500.0, splitCenter(leftToRightLeading, leftToRightMain), 0.001);

            scaffold.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layoutScaffold(root, scaffold, 1_000.0, 600.0);
            Bounds rightToLeftLeading = sceneBounds(leadingPane);
            Bounds rightToLeftMain = sceneBounds(mainPane);
            assertEquals(24.0, rightToLeftMain.getMinX(), 0.001);
            assertEquals(976.0, rightToLeftLeading.getMaxX(), 0.001);
            assertEquals(20.0, rightToLeftLeading.getMinX() - rightToLeftMain.getMaxX(), 0.001);
            assertEquals(500.0, splitCenter(rightToLeftMain, rightToLeftLeading), 0.001);
            assertEquals(leftToRightLeading.getWidth(), rightToLeftLeading.getWidth(), 0.001);
            assertEquals(leftToRightMain.getWidth(), rightToLeftMain.getWidth(), 0.001);
        });
    }

    /// Verifies configurable split positions are logical, mirrored, and constrained by pane minimum widths.
    @Test
    void honorsLogicalSplitPositionAndPaneMinimums() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane leadingPane = new Pane();
            Pane mainPane = new Pane();
            scaffold.setLeadingPane(leadingPane);
            scaffold.setMainPane(mainPane);
            scaffold.setPaneLayout(M3PaneLayout.SPLIT_LEADING);
            scaffold.setNavigationLayout(M3NavigationLayout.NONE);
            scaffold.setContentMargin(0.0);
            scaffold.setPaneSpacing(20.0);
            scaffold.setSplitPosition(0.35);

            Pane root = installForLayout(scaffold, 1_000.0, 600.0);
            Bounds leftToRightLeading = sceneBounds(leadingPane);
            Bounds leftToRightMain = sceneBounds(mainPane);
            assertEquals(350.0, splitCenter(leftToRightLeading, leftToRightMain), 0.001);
            assertEquals(340.0, leftToRightLeading.getWidth(), 0.001);
            assertEquals(640.0, leftToRightMain.getWidth(), 0.001);

            scaffold.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layoutScaffold(root, scaffold, 1_000.0, 600.0);
            Bounds rightToLeftLeading = sceneBounds(leadingPane);
            Bounds rightToLeftMain = sceneBounds(mainPane);
            assertEquals(650.0, splitCenter(rightToLeftMain, rightToLeftLeading), 0.001);
            assertEquals(340.0, rightToLeftLeading.getWidth(), 0.001);
            assertEquals(640.0, rightToLeftMain.getWidth(), 0.001);

            scaffold.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            leadingPane.setMinWidth(420.0);
            scaffold.setSplitPosition(0.1);
            layoutScaffold(root, scaffold, 1_000.0, 600.0);
            Bounds constrainedLeading = sceneBounds(leadingPane);
            Bounds constrainedMain = sceneBounds(mainPane);
            assertEquals(420.0, constrainedLeading.getWidth(), 0.001);
            assertEquals(430.0, splitCenter(constrainedLeading, constrainedMain), 0.001);
        });
    }

    /// Verifies that a leading navigation rail compresses only the logical leading side of a centered split.
    @Test
    void keepsSplitDividerCenteredBesideLeadingRail() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane navigationRail = fixedWidthPane(80.0);
            Pane leadingPane = new Pane();
            Pane mainPane = new Pane();
            scaffold.setNavigationRail(navigationRail);
            scaffold.setNavigationLayout(M3NavigationLayout.RAIL);
            scaffold.setLeadingPane(leadingPane);
            scaffold.setMainPane(mainPane);
            scaffold.setPaneLayout(M3PaneLayout.SPLIT_LEADING);
            scaffold.setContentMargin(24.0);
            scaffold.setPaneSpacing(24.0);

            Pane root = installForLayout(scaffold, 1_000.0, 600.0);
            Bounds leftToRightRail = sceneBounds(navigationRail);
            Bounds leftToRightLeading = sceneBounds(leadingPane);
            Bounds leftToRightMain = sceneBounds(mainPane);
            assertEquals(0.0, leftToRightRail.getMinX(), 0.001);
            assertEquals(80.0, leftToRightRail.getMaxX(), 0.001);
            assertEquals(104.0, leftToRightLeading.getMinX(), 0.001);
            assertEquals(976.0, leftToRightMain.getMaxX(), 0.001);
            assertEquals(500.0, splitCenter(leftToRightLeading, leftToRightMain), 0.001);

            scaffold.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layoutScaffold(root, scaffold, 1_000.0, 600.0);
            Bounds rightToLeftRail = sceneBounds(navigationRail);
            Bounds rightToLeftLeading = sceneBounds(leadingPane);
            Bounds rightToLeftMain = sceneBounds(mainPane);
            assertEquals(920.0, rightToLeftRail.getMinX(), 0.001);
            assertEquals(1_000.0, rightToLeftRail.getMaxX(), 0.001);
            assertEquals(896.0, rightToLeftLeading.getMaxX(), 0.001);
            assertEquals(24.0, rightToLeftMain.getMinX(), 0.001);
            assertEquals(500.0, splitCenter(rightToLeftMain, rightToLeftLeading), 0.001);
        });
    }

    /// Verifies explicit fixed widths, pane spacing, and logical placement for two- and three-pane layouts.
    @Test
    void honorsFixedPaneMetricsInBothOrientations() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane leadingPane = new Pane();
            Pane mainPane = new Pane();
            Pane trailingPane = new Pane();
            scaffold.setLeadingPane(leadingPane);
            scaffold.setMainPane(mainPane);
            scaffold.setTrailingPane(trailingPane);
            scaffold.setNavigationLayout(M3NavigationLayout.NONE);
            scaffold.setContentMargin(24.0);
            scaffold.setPaneSpacing(12.0);
            scaffold.setFixedLeadingPaneWidth(180.0);
            scaffold.setFixedTrailingPaneWidth(200.0);
            scaffold.setPaneLayout(M3PaneLayout.THREE_PANE);

            Pane root = installForLayout(scaffold, 1_000.0, 600.0);
            Bounds leftToRightLeading = sceneBounds(leadingPane);
            Bounds leftToRightMain = sceneBounds(mainPane);
            Bounds leftToRightTrailing = sceneBounds(trailingPane);
            assertEquals(24.0, leftToRightLeading.getMinX(), 0.001);
            assertEquals(180.0, leftToRightLeading.getWidth(), 0.001);
            assertEquals(12.0, leftToRightMain.getMinX() - leftToRightLeading.getMaxX(), 0.001);
            assertEquals(12.0, leftToRightTrailing.getMinX() - leftToRightMain.getMaxX(), 0.001);
            assertEquals(200.0, leftToRightTrailing.getWidth(), 0.001);
            assertEquals(976.0, leftToRightTrailing.getMaxX(), 0.001);

            scaffold.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            layoutScaffold(root, scaffold, 1_000.0, 600.0);
            Bounds rightToLeftLeading = sceneBounds(leadingPane);
            Bounds rightToLeftMain = sceneBounds(mainPane);
            Bounds rightToLeftTrailing = sceneBounds(trailingPane);
            assertEquals(24.0, rightToLeftTrailing.getMinX(), 0.001);
            assertEquals(200.0, rightToLeftTrailing.getWidth(), 0.001);
            assertEquals(12.0, rightToLeftMain.getMinX() - rightToLeftTrailing.getMaxX(), 0.001);
            assertEquals(12.0, rightToLeftLeading.getMinX() - rightToLeftMain.getMaxX(), 0.001);
            assertEquals(180.0, rightToLeftLeading.getWidth(), 0.001);
            assertEquals(976.0, rightToLeftLeading.getMaxX(), 0.001);
        });
    }

    /// Verifies that top, bottom, and navigation bars reserve non-overlapping body space.
    @Test
    void reservesBodySpaceForVerticalBars() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane topBar = fixedHeightPane(64.0);
            Pane bottomBar = fixedHeightPane(48.0);
            Pane navigationBar = fixedHeightPane(80.0);
            Pane mainPane = new Pane();
            scaffold.setTopBar(topBar);
            scaffold.setBottomBar(bottomBar);
            scaffold.setNavigationBar(navigationBar);
            scaffold.setNavigationLayout(M3NavigationLayout.BAR);
            scaffold.setMainPane(mainPane);
            scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            scaffold.setContentMargin(0.0);
            scaffold.setSafetyInsets(new Insets(5.0, 7.0, 11.0, 13.0));

            installForLayout(scaffold, 800.0, 600.0);
            Bounds topBounds = sceneBounds(topBar);
            Bounds mainBounds = sceneBounds(mainPane);
            Bounds bottomBounds = sceneBounds(bottomBar);
            Bounds navigationBounds = sceneBounds(navigationBar);

            assertEquals(5.0, topBounds.getMinY(), 0.001);
            assertEquals(64.0, topBounds.getHeight(), 0.001);
            assertEquals(topBounds.getMaxY(), mainBounds.getMinY(), 0.001);
            assertEquals(mainBounds.getMaxY(), bottomBounds.getMinY(), 0.001);
            assertEquals(48.0, bottomBounds.getHeight(), 0.001);
            assertEquals(bottomBounds.getMaxY(), navigationBounds.getMinY(), 0.001);
            assertEquals(80.0, navigationBounds.getHeight(), 0.001);
            assertEquals(589.0, navigationBounds.getMaxY(), 0.001);
            assertTrue(mainBounds.getHeight() > 0.0);
        });
    }

    /// Installs a scaffold in an offscreen scene and performs its initial CSS and layout pass.
    ///
    /// @param scaffold the scaffold to install
    /// @param width    the assigned scene and scaffold width
    /// @param height   the assigned scene and scaffold height
    /// @return the scene root used for subsequent layout passes
    private static Pane installForLayout(M3AdaptiveScaffold scaffold, double width, double height) {
        Pane root = new Pane(scaffold);
        scaffold.setManaged(false);
        new Scene(root, width, height);
        layoutScaffold(root, scaffold, width, height);
        return root;
    }

    /// Resizes and lays out an installed scaffold without requiring a native window.
    ///
    /// @param root     the scaffold's scene root
    /// @param scaffold the scaffold to lay out
    /// @param width    the assigned root and scaffold width
    /// @param height   the assigned root and scaffold height
    private static void layoutScaffold(
            Pane root,
            M3AdaptiveScaffold scaffold,
            double width,
            double height
    ) {
        root.resize(width, height);
        scaffold.resize(width, height);
        root.applyCss();
        scaffold.applyCss();
        root.layout();
        scaffold.layout();
    }

    /// Creates a pane constrained to one preferred and maximum width.
    ///
    /// @param width the pane width
    /// @return the constrained pane
    private static Pane fixedWidthPane(double width) {
        Pane pane = new Pane();
        pane.setMinWidth(width);
        pane.setPrefWidth(width);
        pane.setMaxWidth(width);
        pane.setMinHeight(0.0);
        pane.setMaxHeight(Double.MAX_VALUE);
        return pane;
    }

    /// Creates a pane constrained to one preferred and maximum height.
    ///
    /// @param height the pane height
    /// @return the constrained pane
    private static Pane fixedHeightPane(double height) {
        Pane pane = new Pane();
        pane.setMinHeight(height);
        pane.setPrefHeight(height);
        pane.setMaxHeight(height);
        pane.setMinWidth(0.0);
        pane.setMaxWidth(Double.MAX_VALUE);
        return pane;
    }

    /// Returns an installed node's scene-space bounds.
    ///
    /// @param node the installed node
    /// @return the node bounds in scene coordinates
    private static Bounds sceneBounds(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        assertNotNull(bounds);
        return bounds;
    }

    /// Returns an installed slot node's parent or fails the current assertion.
    ///
    /// @param node the slot node
    /// @return the node's non-null parent
    private static Parent requireParent(Node node) {
        Parent parent = node.getParent();
        assertNotNull(parent);
        return parent;
    }

    /// Returns the center of the gap between physically ordered pane bounds.
    ///
    /// @param leftBounds  the pane physically on the left
    /// @param rightBounds the pane physically on the right
    /// @return the horizontal center of the pane gap
    private static double splitCenter(Bounds leftBounds, Bounds rightBounds) {
        return (leftBounds.getMaxX() + rightBounds.getMinX()) / 2.0;
    }

}
