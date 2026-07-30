// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionScheme;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies adaptive state resolution and property contracts without opening a JavaFX window.
@NotNullByDefault
final class M3AdaptiveScaffoldStateTest {
    /// Starts the JavaFX toolkit before controls are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies the initial style, requested policy, and effective empty-scaffold state.
    @Test
    void exposesStableDefaults() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();

            assertTrue(scaffold.getStyleClass().contains("m3-adaptive-scaffold"));
            assertFalse(scaffold.isFocusTraversable());
            assertSame(M3PaneLayout.ADAPTIVE, scaffold.getPaneLayout());
            assertSame(M3PaneRole.MAIN, scaffold.getActivePane());
            assertSame(M3NavigationLayout.ADAPTIVE, scaffold.getNavigationLayout());
            assertNull(scaffold.getLayoutMotionSpec());
            assertNull(scaffold.layoutMotionSpecProperty().get());
            assertNull(scaffold.getBreakpointOverride());
            assertNull(scaffold.breakpointOverrideProperty().get());
            assertSame(M3Breakpoint.COMPACT, scaffold.getBreakpoint());
            assertSame(M3Breakpoint.COMPACT, scaffold.breakpointProperty().get());
            assertSame(M3PaneLayout.SINGLE, scaffold.getEffectivePaneLayout());
            assertSame(M3NavigationLayout.NONE, scaffold.getEffectiveNavigationLayout());
            assertEquals(0, scaffold.getVisiblePaneCount());
            assertEquals(0, scaffold.visiblePaneCountProperty().get());
            assertEquals(Region.USE_COMPUTED_SIZE, scaffold.getContentMargin());
            assertEquals(Region.USE_COMPUTED_SIZE, scaffold.getPaneSpacing());
            assertEquals(0.5, scaffold.getSplitPosition());
            assertEquals(0.5, scaffold.splitPositionProperty().get());
            assertEquals(Region.USE_COMPUTED_SIZE, scaffold.getFixedLeadingPaneWidth());
            assertEquals(Region.USE_COMPUTED_SIZE, scaffold.getFixedTrailingPaneWidth());
        });
    }

    /// Verifies default restoration for direct null assignments and default interpretation of bound null values.
    @Test
    void handlesNullablePropertySourcesWithoutExposingNullFromGetters() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();

            scaffold.paneLayoutProperty().set(null);
            scaffold.activePaneProperty().set(null);
            scaffold.navigationLayoutProperty().set(null);
            scaffold.safetyInsetsProperty().set(null);
            assertSame(M3PaneLayout.ADAPTIVE, scaffold.paneLayoutProperty().get());
            assertSame(M3PaneRole.MAIN, scaffold.activePaneProperty().get());
            assertSame(M3NavigationLayout.ADAPTIVE, scaffold.navigationLayoutProperty().get());
            assertSame(Insets.EMPTY, scaffold.safetyInsetsProperty().get());

            SimpleObjectProperty<@Nullable M3PaneLayout> paneLayoutSource = new SimpleObjectProperty<>();
            SimpleObjectProperty<@Nullable M3PaneRole> activePaneSource = new SimpleObjectProperty<>();
            SimpleObjectProperty<@Nullable M3NavigationLayout> navigationLayoutSource = new SimpleObjectProperty<>();
            SimpleObjectProperty<@Nullable Insets> safetyInsetsSource = new SimpleObjectProperty<>();
            scaffold.paneLayoutProperty().bind(paneLayoutSource);
            scaffold.activePaneProperty().bind(activePaneSource);
            scaffold.navigationLayoutProperty().bind(navigationLayoutSource);
            scaffold.safetyInsetsProperty().bind(safetyInsetsSource);

            assertNull(scaffold.paneLayoutProperty().get());
            assertNull(scaffold.activePaneProperty().get());
            assertNull(scaffold.navigationLayoutProperty().get());
            assertNull(scaffold.safetyInsetsProperty().get());
            assertSame(M3PaneLayout.ADAPTIVE, scaffold.getPaneLayout());
            assertSame(M3PaneRole.MAIN, scaffold.getActivePane());
            assertSame(M3NavigationLayout.ADAPTIVE, scaffold.getNavigationLayout());
            assertSame(Insets.EMPTY, scaffold.getSafetyInsets());
        });
    }

    /// Verifies local motion-specification assignment and restoration of theme-derived motion.
    @Test
    void exposesConfigurableLayoutMotion() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            M3MotionSpec motionSpec = M3MotionScheme.expressive().slowSpatial();

            scaffold.setLayoutMotionSpec(motionSpec);
            assertSame(motionSpec, scaffold.getLayoutMotionSpec());
            assertSame(motionSpec, scaffold.layoutMotionSpecProperty().get());

            scaffold.setLayoutMotionSpec(null);
            assertNull(scaffold.getLayoutMotionSpec());
            assertNull(scaffold.layoutMotionSpecProperty().get());
        });
    }

    /// Verifies that an override supersedes assigned width until it is cleared.
    @Test
    void breakpointOverrideSupersedesAssignedWidth() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            scaffold.resize(900.0, 600.0);
            assertSame(M3Breakpoint.EXPANDED, scaffold.getBreakpoint());

            scaffold.setBreakpointOverride(M3Breakpoint.COMPACT);
            assertSame(M3Breakpoint.COMPACT, scaffold.getBreakpoint());
            scaffold.resize(1_700.0, 600.0);
            assertSame(M3Breakpoint.COMPACT, scaffold.getBreakpoint());

            scaffold.setBreakpointOverride(M3Breakpoint.LARGE);
            assertSame(M3Breakpoint.LARGE, scaffold.getBreakpoint());
            scaffold.setBreakpointOverride(null);
            assertSame(M3Breakpoint.EXTRA_LARGE, scaffold.getBreakpoint());
        });
    }

    /// Verifies automatic one- and two-pane selection across breakpoints and active-pane changes.
    @Test
    void resolvesAdaptivePaneLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffoldWithThreePanes();

            scaffold.resize(599.0, 600.0);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, true, false);

            scaffold.setActivePane(M3PaneRole.TRAILING);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, false, true);

            scaffold.resize(600.0, 600.0);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, false, true);

            scaffold.resize(840.0, 600.0);
            assertPaneState(scaffold, M3PaneLayout.SPLIT_TRAILING, false, true, true);

            scaffold.setActivePane(M3PaneRole.LEADING);
            assertPaneState(scaffold, M3PaneLayout.SPLIT_LEADING, true, true, false);

            scaffold.resize(1_600.0, 600.0);
            assertPaneState(scaffold, M3PaneLayout.SPLIT_LEADING, true, true, false);
        });
    }

    /// Verifies deterministic fallback when a single-pane layout's preferred slot is empty.
    @Test
    void singlePaneFallsBackToAvailableSlots() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane leading = new Pane();
            Pane main = new Pane();
            Pane trailing = new Pane();
            scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            scaffold.setActivePane(M3PaneRole.TRAILING);
            scaffold.setLeadingPane(leading);
            scaffold.setMainPane(main);

            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, true, false);

            scaffold.setMainPane(null);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, true, false, false);

            scaffold.setLeadingPane(null);
            scaffold.setTrailingPane(trailing);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, false, true);
        });
    }

    /// Verifies every explicit pane policy and the visible-pane total when all slots are populated.
    @Test
    void honorsExplicitPaneLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffoldWithThreePanes();
            scaffold.resize(320.0, 600.0);

            scaffold.setPaneLayout(M3PaneLayout.SINGLE);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, true, false);

            scaffold.setPaneLayout(M3PaneLayout.SPLIT_LEADING);
            assertPaneState(scaffold, M3PaneLayout.SPLIT_LEADING, true, true, false);

            scaffold.setPaneLayout(M3PaneLayout.SPLIT_TRAILING);
            assertPaneState(scaffold, M3PaneLayout.SPLIT_TRAILING, false, true, true);

            scaffold.setPaneLayout(M3PaneLayout.FIXED_LEADING);
            assertPaneState(scaffold, M3PaneLayout.FIXED_LEADING, true, true, false);

            scaffold.setPaneLayout(M3PaneLayout.FIXED_TRAILING);
            assertPaneState(scaffold, M3PaneLayout.FIXED_TRAILING, false, true, true);

            scaffold.setPaneLayout(M3PaneLayout.THREE_PANE);
            assertPaneState(scaffold, M3PaneLayout.THREE_PANE, true, true, true);
        });
    }

    /// Verifies that explicit multi-pane requests do not reserve blank regions for empty slots.
    @Test
    void collapsesExplicitLayoutsAroundEmptySlots() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane leading = new Pane();
            Pane main = new Pane();
            Pane trailing = new Pane();
            scaffold.setMainPane(main);

            scaffold.setPaneLayout(M3PaneLayout.SPLIT_LEADING);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, true, false);

            scaffold.setLeadingPane(leading);
            assertPaneState(scaffold, M3PaneLayout.SPLIT_LEADING, true, true, false);

            scaffold.setPaneLayout(M3PaneLayout.THREE_PANE);
            assertPaneState(scaffold, M3PaneLayout.FIXED_LEADING, true, true, false);

            scaffold.setTrailingPane(trailing);
            scaffold.setLeadingPane(null);
            assertPaneState(scaffold, M3PaneLayout.FIXED_TRAILING, false, true, true);

            scaffold.setMainPane(null);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, false, false, true);

            scaffold.setLeadingPane(leading);
            scaffold.setPaneLayout(M3PaneLayout.ADAPTIVE);
            scaffold.setBreakpointOverride(M3Breakpoint.EXTRA_LARGE);
            assertPaneState(scaffold, M3PaneLayout.SINGLE, true, false, false);
        });
    }

    /// Verifies adaptive navigation preferences, fallback to the available slot, and explicit policies.
    @Test
    void resolvesAdaptiveAndExplicitNavigationLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffoldWithThreePanes();
            Pane navigationBar = new Pane();
            Pane navigationRail = new Pane();
            scaffold.setNavigationBar(navigationBar);
            scaffold.setNavigationRail(navigationRail);

            scaffold.resize(500.0, 600.0);
            assertSame(M3NavigationLayout.BAR, scaffold.getEffectiveNavigationLayout());

            scaffold.resize(700.0, 600.0);
            assertSame(M3NavigationLayout.RAIL, scaffold.getEffectiveNavigationLayout());

            scaffold.setPaneLayout(M3PaneLayout.SPLIT_LEADING);
            assertSame(M3NavigationLayout.BAR, scaffold.getEffectiveNavigationLayout());

            scaffold.resize(900.0, 600.0);
            assertSame(M3NavigationLayout.RAIL, scaffold.getEffectiveNavigationLayout());

            scaffold.setNavigationRail(null);
            assertSame(M3NavigationLayout.BAR, scaffold.getEffectiveNavigationLayout());
            scaffold.setNavigationLayout(M3NavigationLayout.RAIL);
            assertSame(M3NavigationLayout.NONE, scaffold.getEffectiveNavigationLayout());
            scaffold.setNavigationLayout(M3NavigationLayout.BAR);
            assertSame(M3NavigationLayout.BAR, scaffold.getEffectiveNavigationLayout());

            scaffold.setNavigationBar(null);
            assertSame(M3NavigationLayout.NONE, scaffold.getEffectiveNavigationLayout());
            scaffold.setNavigationLayout(M3NavigationLayout.ADAPTIVE);
            assertSame(M3NavigationLayout.NONE, scaffold.getEffectiveNavigationLayout());

            scaffold.setNavigationRail(navigationRail);
            scaffold.resize(500.0, 600.0);
            assertSame(M3NavigationLayout.RAIL, scaffold.getEffectiveNavigationLayout());
            scaffold.setNavigationLayout(M3NavigationLayout.NONE);
            assertSame(M3NavigationLayout.NONE, scaffold.getEffectiveNavigationLayout());
        });
    }

    /// Verifies automatic metric values, fixed-pane defaults, and explicit overrides.
    @Test
    void resolvesAutomaticAndExplicitMetrics() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();

            assertAutomaticMetrics(scaffold, M3Breakpoint.COMPACT, 16.0, 0.0, 360.0);
            assertAutomaticMetrics(scaffold, M3Breakpoint.MEDIUM, 24.0, 24.0, 360.0);
            assertAutomaticMetrics(scaffold, M3Breakpoint.EXPANDED, 24.0, 24.0, 360.0);
            assertAutomaticMetrics(scaffold, M3Breakpoint.LARGE, 24.0, 24.0, 412.0);
            assertAutomaticMetrics(scaffold, M3Breakpoint.EXTRA_LARGE, 24.0, 24.0, 412.0);

            scaffold.setLeadingPane(new Pane());
            scaffold.setMainPane(new Pane());
            scaffold.setTrailingPane(new Pane());
            scaffold.setPaneLayout(M3PaneLayout.THREE_PANE);
            assertEquals(400.0, scaffold.getEffectiveFixedTrailingPaneWidth());

            scaffold.setContentMargin(32.0);
            scaffold.setPaneSpacing(12.0);
            scaffold.setFixedLeadingPaneWidth(280.0);
            scaffold.setFixedTrailingPaneWidth(300.0);
            assertEquals(32.0, scaffold.getEffectiveContentMargin());
            assertEquals(12.0, scaffold.getEffectivePaneSpacing());
            assertEquals(280.0, scaffold.getEffectiveFixedLeadingPaneWidth());
            assertEquals(300.0, scaffold.getEffectiveFixedTrailingPaneWidth());

            scaffold.setContentMargin(Region.USE_COMPUTED_SIZE);
            scaffold.setPaneSpacing(Region.USE_COMPUTED_SIZE);
            scaffold.setFixedLeadingPaneWidth(Region.USE_COMPUTED_SIZE);
            scaffold.setFixedTrailingPaneWidth(Region.USE_COMPUTED_SIZE);
            assertEquals(24.0, scaffold.getEffectiveContentMargin());
            assertEquals(24.0, scaffold.getEffectivePaneSpacing());
            assertEquals(412.0, scaffold.getEffectiveFixedLeadingPaneWidth());
            assertEquals(400.0, scaffold.getEffectiveFixedTrailingPaneWidth());
        });
    }

    /// Verifies direct setter validation and deferred validation of values supplied through property binding.
    @Test
    void rejectsInvalidMetrics() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            assertRejectsInvalidMetric(scaffold::setContentMargin);
            assertRejectsInvalidMetric(scaffold::setPaneSpacing);
            assertRejectsInvalidMetric(scaffold::setFixedLeadingPaneWidth);
            assertRejectsInvalidMetric(scaffold::setFixedTrailingPaneWidth);

            SimpleDoubleProperty source = new SimpleDoubleProperty(24.0);
            scaffold.contentMarginProperty().bind(source);
            source.set(Double.NaN);
            assertThrows(IllegalArgumentException.class, scaffold::getEffectiveContentMargin);
            scaffold.contentMarginProperty().unbind();
        });
    }

    /// Verifies that split position follows the JavaFX property contract and rejects invalid direct or bound values.
    @Test
    void exposesBindableValidatedSplitPosition() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            scaffold.setSplitPosition(0.35);
            assertEquals(0.35, scaffold.getSplitPosition());
            assertSame(scaffold, scaffold.splitPositionProperty().getBean());
            assertEquals("splitPosition", scaffold.splitPositionProperty().getName());

            assertThrows(IllegalArgumentException.class, () -> scaffold.setSplitPosition(-0.01));
            assertThrows(IllegalArgumentException.class, () -> scaffold.setSplitPosition(1.01));
            assertThrows(IllegalArgumentException.class, () -> scaffold.setSplitPosition(Double.NaN));
            assertThrows(IllegalArgumentException.class, () -> scaffold.setSplitPosition(Double.POSITIVE_INFINITY));
            assertEquals(0.35, scaffold.getSplitPosition());

            SimpleDoubleProperty source = new SimpleDoubleProperty(0.7);
            scaffold.splitPositionProperty().bind(source);
            assertEquals(0.7, scaffold.getSplitPosition());
            assertInstanceOf(
                    IllegalArgumentException.class,
                    captureUncaughtListenerException(() -> source.set(1.1))
            );
            assertThrows(IllegalArgumentException.class, scaffold::getSplitPosition);
            scaffold.splitPositionProperty().unbind();
            scaffold.setSplitPosition(0.5);
        });
    }

    /// Verifies that each effective state has exactly one pseudo-class from each applicable category.
    @Test
    void maintainsExclusiveAdaptivePseudoClasses() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = scaffoldWithThreePanes();
            scaffold.setNavigationBar(new Pane());
            scaffold.setNavigationRail(new Pane());

            assertBreakpointPseudoClass(scaffold, M3Breakpoint.COMPACT, "compact");
            assertBreakpointPseudoClass(scaffold, M3Breakpoint.MEDIUM, "medium");
            assertBreakpointPseudoClass(scaffold, M3Breakpoint.EXPANDED, "expanded");
            assertBreakpointPseudoClass(scaffold, M3Breakpoint.LARGE, "large");
            assertBreakpointPseudoClass(scaffold, M3Breakpoint.EXTRA_LARGE, "extra-large");

            assertPanePseudoClass(scaffold, M3PaneLayout.SINGLE, "single-pane");
            assertPanePseudoClass(scaffold, M3PaneLayout.SPLIT_LEADING, "split-leading");
            assertPanePseudoClass(scaffold, M3PaneLayout.SPLIT_TRAILING, "split-trailing");
            assertPanePseudoClass(scaffold, M3PaneLayout.FIXED_LEADING, "fixed-leading");
            assertPanePseudoClass(scaffold, M3PaneLayout.FIXED_TRAILING, "fixed-trailing");
            assertPanePseudoClass(scaffold, M3PaneLayout.THREE_PANE, "three-pane");

            scaffold.setNavigationLayout(M3NavigationLayout.BAR);
            assertPseudoClassCategory(scaffold, "navigation-bar", "navigation-bar", "navigation-rail");
            scaffold.setNavigationLayout(M3NavigationLayout.RAIL);
            assertPseudoClassCategory(scaffold, "navigation-rail", "navigation-bar", "navigation-rail");
            scaffold.setNavigationLayout(M3NavigationLayout.NONE);
            assertPseudoClassCategory(scaffold, null, "navigation-bar", "navigation-rail");
        });
    }

    /// Verifies that slot properties retain their exact node instances while adaptive state changes.
    @Test
    void slotPropertiesPreserveNodeIdentityAndState() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane topBar = new Pane();
            Pane bottomBar = new Pane();
            Pane navigationBar = new Pane();
            Pane navigationRail = new Pane();
            Pane trailingRail = new Pane();
            Pane leadingPane = new Pane();
            Pane mainPane = new Pane();
            Pane trailingPane = new Pane();
            mainPane.setUserData("retained-state");

            scaffold.setTopBar(topBar);
            scaffold.setBottomBar(bottomBar);
            scaffold.setNavigationBar(navigationBar);
            scaffold.setNavigationRail(navigationRail);
            scaffold.setTrailingRail(trailingRail);
            scaffold.setLeadingPane(leadingPane);
            scaffold.setMainPane(mainPane);
            scaffold.setTrailingPane(trailingPane);

            scaffold.setBreakpointOverride(M3Breakpoint.COMPACT);
            scaffold.setActivePane(M3PaneRole.TRAILING);
            scaffold.setBreakpointOverride(M3Breakpoint.EXTRA_LARGE);
            scaffold.setPaneLayout(M3PaneLayout.THREE_PANE);

            assertSame(topBar, scaffold.getTopBar());
            assertSame(topBar, scaffold.topBarProperty().get());
            assertSame(bottomBar, scaffold.getBottomBar());
            assertSame(bottomBar, scaffold.bottomBarProperty().get());
            assertSame(navigationBar, scaffold.getNavigationBar());
            assertSame(navigationBar, scaffold.navigationBarProperty().get());
            assertSame(navigationRail, scaffold.getNavigationRail());
            assertSame(navigationRail, scaffold.navigationRailProperty().get());
            assertSame(trailingRail, scaffold.getTrailingRail());
            assertSame(trailingRail, scaffold.trailingRailProperty().get());
            assertSame(leadingPane, scaffold.getLeadingPane());
            assertSame(leadingPane, scaffold.leadingPaneProperty().get());
            assertSame(mainPane, scaffold.getMainPane());
            assertSame(mainPane, scaffold.mainPaneProperty().get());
            assertSame(trailingPane, scaffold.getTrailingPane());
            assertSame(trailingPane, scaffold.trailingPaneProperty().get());
            assertEquals("retained-state", mainPane.getUserData());
        });
    }

    /// Verifies that invalid direct slot assignments fail before replacing the accepted value.
    @Test
    void rejectsDuplicateParentedAndCyclicSlotNodesAtomically() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane acceptedMain = new Pane();
            Pane duplicate = new Pane();
            Pane parented = new Pane();
            Pane externalParent = new Pane(parented);
            Pane ancestor = new Pane(scaffold);

            scaffold.setMainPane(acceptedMain);
            scaffold.setLeadingPane(duplicate);

            assertThrows(IllegalArgumentException.class, () -> scaffold.trailingPaneProperty().set(duplicate));
            assertNull(scaffold.getTrailingPane());
            assertSame(duplicate, scaffold.getLeadingPane());

            assertThrows(IllegalArgumentException.class, () -> scaffold.setMainPane(parented));
            assertSame(acceptedMain, scaffold.getMainPane());
            assertSame(externalParent, parented.getParent());

            assertThrows(IllegalArgumentException.class, () -> scaffold.setMainPane(scaffold));
            assertSame(acceptedMain, scaffold.getMainPane());

            assertThrows(IllegalArgumentException.class, () -> scaffold.setMainPane(ancestor));
            assertSame(acceptedMain, scaffold.getMainPane());
            assertSame(ancestor, scaffold.getParent());

            scaffold.setMainPane(acceptedMain);
            assertSame(acceptedMain, scaffold.getMainPane());
        });
    }

    /// Verifies that a bound slot rejects an ineligible source value before publishing adaptive-state changes.
    @Test
    void validatesEveryBoundSlotValue() {
        FxTestUtils.runOnFxThread(() -> {
            M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
            Pane leading = new Pane();
            Pane initialMain = new Pane();
            Pane replacementMain = new Pane();
            SimpleObjectProperty<@Nullable Node> source =
                    new SimpleObjectProperty<>(initialMain);

            scaffold.setLeadingPane(leading);
            scaffold.mainPaneProperty().bind(source);
            assertSame(initialMain, scaffold.getMainPane());

            assertInstanceOf(
                    IllegalArgumentException.class,
                    captureUncaughtListenerException(() -> source.set(leading))
            );

            source.set(replacementMain);
            assertSame(replacementMain, scaffold.getMainPane());
        });
    }

    /// Creates a scaffold with all three content slots populated.
    ///
    /// @return a scaffold with leading, main, and trailing panes
    private static M3AdaptiveScaffold scaffoldWithThreePanes() {
        M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
        scaffold.setLeadingPane(new Pane());
        scaffold.setMainPane(new Pane());
        scaffold.setTrailingPane(new Pane());
        return scaffold;
    }

    /// Verifies the effective layout, pane visibility, and derived visible-pane total.
    ///
    /// @param scaffold        the scaffold under test
    /// @param layout          the expected effective layout
    /// @param leadingVisible  whether the leading pane must be visible
    /// @param mainVisible     whether the main pane must be visible
    /// @param trailingVisible whether the trailing pane must be visible
    private static void assertPaneState(
            M3AdaptiveScaffold scaffold,
            M3PaneLayout layout,
            boolean leadingVisible,
            boolean mainVisible,
            boolean trailingVisible
    ) {
        assertSame(layout, scaffold.getEffectivePaneLayout());
        assertEquals(leadingVisible, scaffold.isPaneVisible(M3PaneRole.LEADING));
        assertEquals(mainVisible, scaffold.isPaneVisible(M3PaneRole.MAIN));
        assertEquals(trailingVisible, scaffold.isPaneVisible(M3PaneRole.TRAILING));
        int expectedCount = (leadingVisible ? 1 : 0) + (mainVisible ? 1 : 0) + (trailingVisible ? 1 : 0);
        assertEquals(expectedCount, scaffold.getVisiblePaneCount());
    }

    /// Verifies automatic metrics for one forced breakpoint.
    ///
    /// @param scaffold       the scaffold under test
    /// @param breakpoint     the forced breakpoint
    /// @param contentMargin  the expected content margin
    /// @param paneSpacing    the expected pane spacing
    /// @param fixedPaneWidth the expected fixed leading and trailing width
    private static void assertAutomaticMetrics(
            M3AdaptiveScaffold scaffold,
            M3Breakpoint breakpoint,
            double contentMargin,
            double paneSpacing,
            double fixedPaneWidth
    ) {
        scaffold.setBreakpointOverride(breakpoint);
        assertEquals(contentMargin, scaffold.getEffectiveContentMargin());
        assertEquals(paneSpacing, scaffold.getEffectivePaneSpacing());
        assertEquals(fixedPaneWidth, scaffold.getEffectiveFixedLeadingPaneWidth());
        assertEquals(fixedPaneWidth, scaffold.getEffectiveFixedTrailingPaneWidth());
    }

    /// Verifies rejection of every unsupported metric representation.
    ///
    /// @param setter the metric setter under test
    private static void assertRejectsInvalidMetric(DoubleConsumer setter) {
        assertThrows(IllegalArgumentException.class, () -> setter.accept(Math.nextDown(Region.USE_COMPUTED_SIZE)));
        assertThrows(IllegalArgumentException.class, () -> setter.accept(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> setter.accept(Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> setter.accept(Double.POSITIVE_INFINITY));
    }

    /// Runs a property mutation and returns an exception reported through JavaFX listener dispatch.
    ///
    /// @param mutation the property mutation to run
    /// @return the exception reported by JavaFX
    private static Throwable captureUncaughtListenerException(Runnable mutation) {
        Thread thread = Thread.currentThread();
        Thread.UncaughtExceptionHandler previousHandler = thread.getUncaughtExceptionHandler();
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        thread.setUncaughtExceptionHandler((ignoredThread, exception) -> failure.set(exception));
        try {
            mutation.run();
        } finally {
            thread.setUncaughtExceptionHandler(previousHandler);
        }
        return Objects.requireNonNull(failure.get(), "listener exception");
    }

    /// Forces one breakpoint and verifies the mutually exclusive breakpoint pseudo-classes.
    ///
    /// @param scaffold            the scaffold under test
    /// @param breakpoint          the forced breakpoint
    /// @param expectedPseudoClass the expected active pseudo-class
    private static void assertBreakpointPseudoClass(
            M3AdaptiveScaffold scaffold,
            M3Breakpoint breakpoint,
            String expectedPseudoClass
    ) {
        scaffold.setBreakpointOverride(breakpoint);
        assertPseudoClassCategory(
                scaffold,
                expectedPseudoClass,
                "compact",
                "medium",
                "expanded",
                "large",
                "extra-large"
        );
    }

    /// Forces one pane policy and verifies the mutually exclusive pane-layout pseudo-classes.
    ///
    /// @param scaffold            the scaffold under test
    /// @param paneLayout          the requested and expected effective pane layout
    /// @param expectedPseudoClass the expected active pseudo-class
    private static void assertPanePseudoClass(
            M3AdaptiveScaffold scaffold,
            M3PaneLayout paneLayout,
            String expectedPseudoClass
    ) {
        scaffold.setPaneLayout(paneLayout);
        assertPseudoClassCategory(
                scaffold,
                expectedPseudoClass,
                "single-pane",
                "split-leading",
                "split-trailing",
                "fixed-leading",
                "fixed-trailing",
                "three-pane"
        );
    }

    /// Verifies that exactly the expected pseudo-class is active within one state category.
    ///
    /// @param scaffold              the scaffold under test
    /// @param expectedPseudoClass   the expected class name, or `null` when the category must be inactive
    /// @param categoryPseudoClasses all pseudo-class names in the category
    private static void assertPseudoClassCategory(
            M3AdaptiveScaffold scaffold,
            @Nullable String expectedPseudoClass,
            String... categoryPseudoClasses
    ) {
        for (String name : categoryPseudoClasses) {
            assertEquals(
                    name.equals(expectedPseudoClass),
                    scaffold.getPseudoClassStates().contains(PseudoClass.getPseudoClass(name)),
                    "Unexpected pseudo-class state for " + name
            );
        }
    }
}
