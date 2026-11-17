// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.skins.M3AdaptiveScaffoldSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that scaffold height queries use the widths assigned to responsive regions.
@NotNullByDefault
final class M3AdaptiveScaffoldSizingTest {
    /// Starts JavaFX before creating controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Measures each pane within its share of the space remaining after rails, margins, and spacing.
    @ParameterizedTest
    @CsvSource({
            "SINGLE,false,0,820,0,102", "SINGLE,true,0,820,0,102",
            "SPLIT_LEADING,false,290,510,0,183", "SPLIT_LEADING,true,290,510,0,183",
            "SPLIT_TRAILING,false,0,290,510,183", "SPLIT_TRAILING,true,0,290,510,183",
            "FIXED_LEADING,false,200,600,0,238", "FIXED_LEADING,true,200,600,0,238",
            "FIXED_TRAILING,false,0,650,150,298", "FIXED_TRAILING,true,0,650,150,298",
            "THREE_PANE,false,200,430,150,298", "THREE_PANE,true,200,430,150,298"
    })
    void measuresAllocatedPaneWidths(M3PaneLayout layout, boolean minimum, double leadingWidth,
                                    double mainWidth, double trailingWidth, double expectedHeight) {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(layout);
            Pane root = new Pane(scaffold);
            scaffold.setManaged(false);
            new Scene(root, 1060, 500);
            root.applyCss();

            assertEquals(expectedHeight, minimum ? scaffold.minHeight(1060) : scaffold.prefHeight(1060));
            scaffold.resize(1060, expectedHeight);
            root.layout();

            for (M3PaneRole role : M3PaneRole.values()) {
                if (!scaffold.isPaneVisible(role)) {
                    continue;
                }
                Region pane = (Region) Objects.requireNonNull(switch (role) {
                    case LEADING -> scaffold.getLeadingPane();
                    case MAIN -> scaffold.getMainPane();
                    case TRAILING -> scaffold.getTrailingPane();
                });
                double expectedWidth = switch (role) {
                    case LEADING -> leadingWidth;
                    case MAIN -> mainWidth;
                    case TRAILING -> trailingWidth;
                };
                assertEquals(expectedWidth, pane.getWidth());
                assertTrue(pane.getHeight() >= pane.prefHeight(pane.getWidth()));
            }
            scaffold.setSkin(null);
        });
    }

    /// Preserves unconstrained height measurement and the platform's content-bias sizing rules.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void measuresUnconstrainedPaneHeight(boolean minimum) {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.SINGLE);
            ((Region) Objects.requireNonNull(scaffold.getMainPane())).setMaxWidth(600);
            ResponsivePane referencePane = new ResponsivePane();
            referencePane.setMaxWidth(600);
            StackPane reference = new StackPane(referencePane);
            double bodyHeight = minimum ? reference.minHeight(-1) : reference.prefHeight(-1);
            assertEquals(58 + bodyHeight, minimum ? scaffold.minHeight(-1) : scaffold.prefHeight(-1));
            scaffold.setSkin(null);
        });
    }

    /// Includes minimum-width constraints when splitting or shrinking fixed side panes.
    @ParameterizedTest
    @CsvSource({
            "SPLIT_LEADING,148", "FIXED_LEADING,418", "THREE_PANE,958"
    })
    void measuresWidthsClampedByPaneMinimums(M3PaneLayout layout, double expectedHeight) {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(layout);
            if (layout == M3PaneLayout.SPLIT_LEADING) {
                ((Region) Objects.requireNonNull(scaffold.getLeadingPane())).setMinWidth(400);
            } else {
                ((Region) Objects.requireNonNull(scaffold.getMainPane())).setMinWidth(700);
            }
            assertEquals(expectedHeight, scaffold.minHeight(1060));
            assertEquals(expectedHeight, scaffold.prefHeight(1060));
            scaffold.setSkin(null);
        });
    }

    /// Measures a responsive rail at its own preferred width rather than the full body width.
    @Test
    void measuresRailAtAllocatedWidth() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.SINGLE);
            ResponsivePane rail = new ResponsivePane();
            rail.setPrefWidth(80);
            scaffold.setNavigationRail(rail);
            assertEquals(508, scaffold.minHeight(1060));
            assertEquals(508, scaffold.prefHeight(1060));
            scaffold.setSkin(null);
        });
    }

    /// Uses the snapped widths that the control and its skin assign during layout.
    @Test
    void measuresSnappedPaneWidth() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.SINGLE);
            Pane root = new Pane(scaffold);
            scaffold.setManaged(false);
            new Scene(root, 1060, 500);
            root.applyCss();
            assertEquals(102, scaffold.prefHeight(1058.1));
            scaffold.resize(1058.1, 102);
            root.layout();
            assertEquals(819, ((Region) Objects.requireNonNull(scaffold.getMainPane())).getWidth());
            scaffold.setSkin(null);
        });
    }

    /// Lets a parent determine height after assigning the scaffold's available width.
    @Test
    void parentResolvesWidthBeforeHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.SINGLE);
            VBox root = new VBox(scaffold);
            new Scene(root, 1060, 500);
            root.applyCss();
            root.resize(1060, 500);
            root.layout();

            assertEquals(Orientation.HORIZONTAL, scaffold.getContentBias());
            assertEquals(1060, scaffold.getWidth());
            assertEquals(102, scaffold.getHeight());
            scaffold.setSkin(null);
        });
    }

    /// Measures the topology for a proposed width without publishing it as the assigned breakpoint.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void adaptiveQueryUsesProposedWidthWithoutChangingState(boolean minimum) {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.ADAPTIVE);
            scaffold.setBreakpointOverride(null);
            assertEquals(M3Breakpoint.COMPACT, scaffold.getBreakpoint());
            assertEquals(M3PaneLayout.SINGLE, scaffold.getEffectivePaneLayout());
            assertEquals(183, minimum ? scaffold.minHeight(1060) : scaffold.prefHeight(1060));
            assertEquals(M3Breakpoint.COMPACT, scaffold.getBreakpoint());
            assertEquals(M3PaneLayout.SINGLE, scaffold.getEffectivePaneLayout());
            assertEquals(0, scaffold.getWidth());
            scaffold.setSkin(null);
        });
    }

    /// Allocates responsive height on the first layout and when crossing breakpoints in either direction.
    @ParameterizedTest
    @CsvSource({"0,1060,183", "500,1060,183", "1060,500,197"})
    void parentRemeasuresAcrossAdaptiveBreakpoints(double initialWidth, double width, double expectedHeight) {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.ADAPTIVE);
            scaffold.setBreakpointOverride(null);
            VBox root = new VBox(scaffold);
            new Scene(root, width, 500);
            root.applyCss();
            if (initialWidth > 0) {
                root.resize(initialWidth, 500);
                root.layout();
            }
            root.resize(width, 500);
            root.layout();

            assertEquals(width, scaffold.getWidth());
            assertEquals(expectedHeight, scaffold.getHeight());
            assertEquals(M3Breakpoint.forWidth(width), scaffold.getBreakpoint());
            scaffold.setSkin(null);
        });
    }

    /// Resolves automatic margins, spacing, and navigation at the query width without changing presented slots.
    @Test
    void queriesAutomaticMetricsAndNavigationWithoutChangingVisibility() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.ADAPTIVE);
            scaffold.setBreakpointOverride(null);
            scaffold.setContentMargin(Region.USE_COMPUTED_SIZE);
            scaffold.setPaneSpacing(Region.USE_COMPUTED_SIZE);
            scaffold.setNavigationLayout(M3NavigationLayout.ADAPTIVE);
            Region navigationBar = fixedRegion(0, 50);
            scaffold.setNavigationBar(navigationBar);
            VBox root = new VBox(scaffold);
            new Scene(root, 500, 500);
            root.applyCss();
            root.resize(500, 500);
            root.layout();

            assertEquals(212, scaffold.getHeight());
            Parent barSlot = Objects.requireNonNull(navigationBar.getParent());
            Parent railSlot = Objects.requireNonNull(Objects.requireNonNull(scaffold.getNavigationRail()).getParent());
            AtomicInteger updates = new AtomicInteger();
            InvalidationListener listener = observable -> updates.incrementAndGet();
            scaffold.breakpointProperty().addListener(listener);
            scaffold.effectivePaneLayoutProperty().addListener(listener);
            scaffold.effectiveNavigationLayoutProperty().addListener(listener);
            scaffold.visiblePaneCountProperty().addListener(listener);
            barSlot.visibleProperty().addListener(listener);
            railSlot.visibleProperty().addListener(listener);
            assertEquals(185, scaffold.minHeight(1060));
            assertEquals(185, scaffold.prefHeight(1060));
            assertEquals(212, scaffold.prefHeight(500));
            assertEquals(M3Breakpoint.COMPACT, scaffold.getBreakpoint());
            assertEquals(M3NavigationLayout.BAR, scaffold.getEffectiveNavigationLayout());
            assertTrue(barSlot.isVisible());
            assertFalse(railSlot.isVisible());
            assertEquals(0, updates.get());
            scaffold.setSkin(null);
        });
    }

    /// Honors an explicit breakpoint override even when a height query proposes a wider breakpoint.
    @Test
    void queryHonorsBreakpointOverride() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(M3PaneLayout.ADAPTIVE);
            scaffold.setBreakpointOverride(M3Breakpoint.COMPACT);
            assertEquals(102, scaffold.minHeight(1060));
            assertEquals(102, scaffold.prefHeight(1060));
            assertEquals(M3Breakpoint.COMPACT, scaffold.getBreakpoint());
            scaffold.setSkin(null);
        });
    }

    /// Uses side-pane defaults from the proposed breakpoint without changing current effective-width getters.
    @ParameterizedTest
    @CsvSource({"FIXED_LEADING,146", "FIXED_TRAILING,146", "THREE_PANE,175"})
    void queriesFixedPaneDefaultsAtProposedBreakpoint(M3PaneLayout layout, double expectedHeight) {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffold(layout);
            scaffold.setBreakpointOverride(null);
            scaffold.setFixedLeadingPaneWidth(Region.USE_COMPUTED_SIZE);
            scaffold.setFixedTrailingPaneWidth(Region.USE_COMPUTED_SIZE);
            assertEquals(360, scaffold.getEffectiveFixedLeadingPaneWidth());
            assertEquals(expectedHeight, scaffold.minHeight(1400));
            assertEquals(expectedHeight, scaffold.prefHeight(1400));
            assertEquals(360, scaffold.getEffectiveFixedLeadingPaneWidth());
            assertEquals(M3Breakpoint.COMPACT, scaffold.getBreakpoint());
            scaffold.setSkin(null);
        });
    }

    /// Creates a scaffold with responsive panes and independently sized bars and rails.
    private static M3AdaptiveScaffold scaffold(M3PaneLayout layout) {
        M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
        scaffold.setBreakpointOverride(M3Breakpoint.EXPANDED);
        scaffold.setPaneLayout(layout);
        scaffold.setNavigationLayout(M3NavigationLayout.RAIL);
        scaffold.setPadding(new Insets(5, 7, 11, 13));
        scaffold.setSafetyInsets(new Insets(3, 17, 9, 23));
        scaffold.setContentMargin(20);
        scaffold.setPaneSpacing(20);
        scaffold.setSplitPosition(0.4);
        scaffold.setFixedLeadingPaneWidth(200);
        scaffold.setFixedTrailingPaneWidth(150);
        scaffold.setLeadingPane(new ResponsivePane());
        scaffold.setMainPane(new ResponsivePane());
        scaffold.setTrailingPane(new ResponsivePane());
        scaffold.setNavigationRail(fixedRegion(80, 0));
        scaffold.setTrailingRail(fixedRegion(60, 0));
        scaffold.setTopBar(fixedRegion(0, 20));
        scaffold.setBottomBar(fixedRegion(0, 10));
        M3MotionSettings.setReducedMotionRequested(scaffold, true);
        scaffold.setSkin(new M3AdaptiveScaffoldSkin(scaffold));
        return scaffold;
    }

    /// Creates a region whose requested dimensions do not depend on the opposite axis.
    private static Region fixedRegion(double width, double height) {
        Region region = new Region();
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return region;
    }

    /// Requires more height as its available width decreases, as wrapped content does.
    @NotNullByDefault
    private static final class ResponsivePane extends Region {
        /// Creates a pane that can shrink horizontally and fill its slot.
        private ResponsivePane() {
            setMinWidth(0);
            setPrefWidth(300);
        }

        /// Requires width to be resolved before height.
        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        /// Returns a natural height for unconstrained queries and the required height otherwise.
        @Override
        protected double computeMinHeight(double width) {
            return width < 0 ? 50 : 36000 / Math.max(1, width);
        }

        /// Uses the same height requirement for preferred sizing.
        @Override
        protected double computePrefHeight(double width) {
            return computeMinHeight(width);
        }
    }
}
