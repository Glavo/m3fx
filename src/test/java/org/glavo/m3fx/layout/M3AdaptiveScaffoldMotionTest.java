// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.animation.M3MotionSettings;
import org.glavo.m3fx.testing.Tier2Test;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies interruptible adaptive scaffold motion in a presenting JavaFX window.
@NotNullByDefault
final class M3AdaptiveScaffoldMotionTest {
    /// Starts the JavaFX toolkit before a stage is created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that a pane remains attached, becomes non-interactive, and animates out while main content reflows.
    @Tier2Test
    @Test
    void animatesPaneExitThroughStableSlotGeometry() throws InterruptedException {
        AtomicReference<@Nullable ScaffoldFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> isExitIntermediateFrame(fixtureReference.get()),
                    () -> {
                        ScaffoldFixture fixture = createFixture();
                        fixtureReference.set(fixture);
                        fixture.scaffold().setPaneLayout(M3PaneLayout.SINGLE);
                    },
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertTrue(fixture.leadingSlot().isVisible());
                        assertTrue(fixture.leadingSlot().isMouseTransparent());
                        assertSame(fixture.leadingSlot(), fixture.leadingPane().getParent());
                        assertTrue(fixture.mainSlot().getWidth() > fixture.initialMainWidth());
                        assertTrue(fixture.mainSlot().getWidth() < fixture.scaffold().getWidth());
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        return !fixture.leadingSlot().isVisible()
                                && Math.abs(fixture.mainSlot().getWidth() - fixture.scaffold().getWidth()) < 1.0;
                    },
                    2,
                    () -> {
                    },
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertFalse(fixture.leadingSlot().isVisible());
                        assertSame(fixture.leadingSlot(), fixture.leadingPane().getParent());
                        assertEquals(fixture.scaffold().getWidth(), fixture.mainSlot().getWidth(), 1.0);
                    }
            );
        } finally {
            closeFixture(fixtureReference.get());
        }
    }

    /// Verifies that reversing an exit continues from rendered geometry and restores the original pane arrangement.
    @Tier2Test
    @Test
    void reversesInterruptedPaneExitWithoutReparentingContent() throws InterruptedException {
        AtomicReference<@Nullable ScaffoldFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> isExitIntermediateFrame(fixtureReference.get()),
                    () -> {
                        ScaffoldFixture fixture = createFixture();
                        fixtureReference.set(fixture);
                        fixture.scaffold().setPaneLayout(M3PaneLayout.SINGLE);
                    },
                    () -> Objects.requireNonNull(fixtureReference.get(), "fixture")
                            .scaffold()
                            .setPaneLayout(M3PaneLayout.FIXED_LEADING)
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        return fixture.leadingSlot().isVisible()
                                && fixture.leadingSlot().getOpacity() >= 0.999
                                && Math.abs(fixture.mainSlot().getWidth() - fixture.initialMainWidth()) < 1.0;
                    },
                    2,
                    () -> {
                    },
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertTrue(fixture.leadingSlot().isVisible());
                        assertFalse(fixture.leadingSlot().isMouseTransparent());
                        assertSame(fixture.leadingSlot(), fixture.leadingPane().getParent());
                        assertEquals(fixture.initialMainWidth(), fixture.mainSlot().getWidth(), 1.0);
                    }
            );
        } finally {
            closeFixture(fixtureReference.get());
        }
    }

    /// Verifies that a topology change animates bounds even when every participating pane remains visible.
    @Tier2Test
    @Test
    void animatesGeometryOnlyTopologyChanges() throws InterruptedException {
        AtomicReference<@Nullable ScaffoldFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> {
                        @Nullable ScaffoldFixture fixture = fixtureReference.get();
                        if (fixture == null) {
                            return false;
                        }
                        double leadingWidth = fixture.leadingSlot().getWidth();
                        return leadingWidth > fixture.initialLeadingWidth() + 1.0
                                && leadingWidth < fixture.scaffold().getWidth() / 2.0 - 1.0;
                    },
                    () -> {
                        ScaffoldFixture fixture = createFixture();
                        fixtureReference.set(fixture);
                        fixture.scaffold().setPaneLayout(M3PaneLayout.SPLIT_LEADING);
                    },
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertTrue(fixture.leadingSlot().isVisible());
                        assertTrue(fixture.mainSlot().isVisible());
                        assertSame(fixture.leadingSlot(), fixture.leadingPane().getParent());
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        return Math.abs(fixture.leadingSlot().getWidth() - fixture.mainSlot().getWidth()) < 1.0;
                    },
                    2,
                    () -> {
                    },
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertEquals(fixture.scaffold().getWidth() / 2.0, fixture.leadingSlot().getWidth(), 1.0);
                        assertEquals(fixture.scaffold().getWidth() / 2.0, fixture.mainSlot().getWidth(), 1.0);
                    }
            );
        } finally {
            closeFixture(fixtureReference.get());
        }
    }

    /// Verifies that a local reduced-motion request applies the latest adaptive geometry synchronously.
    @Tier2Test
    @Test
    void honorsReducedMotionForAdaptiveGeometry() {
        AtomicReference<@Nullable ScaffoldFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThread(() -> {
                ScaffoldFixture fixture = createFixture();
                fixtureReference.set(fixture);
                M3MotionSettings.setReducedMotionRequested(fixture.scaffold(), true);
                fixture.scaffold().setPaneLayout(M3PaneLayout.SINGLE);
                fixture.scaffold().layout();

                assertFalse(fixture.leadingSlot().isVisible());
                assertEquals(fixture.scaffold().getWidth(), fixture.mainSlot().getWidth(), 1.0);
            });
        } finally {
            closeFixture(fixtureReference.get());
        }
    }

    /// Verifies that enabling reduced motion during a run settles every slot at the latest target.
    @Tier2Test
    @Test
    void settlesRunningGeometryWhenReducedMotionIsRequested() throws InterruptedException {
        AtomicReference<@Nullable ScaffoldFixture> fixtureReference = new AtomicReference<>();
        try {
            FxTestUtils.runOnFxThreadWhen(
                    () -> isExitIntermediateFrame(fixtureReference.get()),
                    () -> {
                        ScaffoldFixture fixture = createFixture();
                        fixtureReference.set(fixture);
                        fixture.scaffold().setPaneLayout(M3PaneLayout.SINGLE);
                    },
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        M3MotionSettings.setReducedMotionRequested(fixture.scaffold(), true);
                    }
            );

            FxTestUtils.runOnFxThreadWhenStable(
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        return !fixture.leadingSlot().isVisible()
                                && Math.abs(fixture.mainSlot().getWidth() - fixture.scaffold().getWidth()) < 1.0;
                    },
                    2,
                    () -> {
                    },
                    () -> {
                        ScaffoldFixture fixture = Objects.requireNonNull(fixtureReference.get(), "fixture");
                        assertFalse(fixture.leadingSlot().isVisible());
                        assertEquals(fixture.scaffold().getWidth(), fixture.mainSlot().getWidth(), 1.0);
                    }
            );
        } finally {
            closeFixture(fixtureReference.get());
        }
    }

    /// Returns whether a fixture currently displays a measurable pane-exit intermediate frame.
    ///
    /// @param fixture the fixture, or `null` before setup completes
    /// @return `true` when both opacity and main-pane width are between their endpoints
    private static boolean isExitIntermediateFrame(@Nullable ScaffoldFixture fixture) {
        if (fixture == null) {
            return false;
        }
        double opacity = fixture.leadingSlot().getOpacity();
        double mainWidth = fixture.mainSlot().getWidth();
        return fixture.leadingSlot().isVisible()
                && opacity > 0.05
                && opacity < 0.95
                && mainWidth > fixture.initialMainWidth() + 1.0
                && mainWidth < fixture.scaffold().getWidth() - 1.0;
    }

    /// Creates and shows a fixed-leading scaffold with stable lookup-accessible slot containers.
    ///
    /// @return the presenting fixture
    private static ScaffoldFixture createFixture() {
        Pane leadingPane = new Pane();
        Pane mainPane = new Pane();
        M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
        scaffold.setContentMargin(0.0);
        scaffold.setPaneSpacing(0.0);
        scaffold.setFixedLeadingPaneWidth(280.0);
        scaffold.setLeadingPane(leadingPane);
        scaffold.setMainPane(mainPane);
        scaffold.setPaneLayout(M3PaneLayout.FIXED_LEADING);

        StackPane root = new StackPane(scaffold);
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 1_000.0, 600.0));
        stage.show();
        root.applyCss();
        root.layout();

        StackPane leadingSlot = (StackPane) Objects.requireNonNull(
                scaffold.lookup(".m3-scaffold-leading-pane"),
                "leading slot"
        );
        StackPane mainSlot = (StackPane) Objects.requireNonNull(
                scaffold.lookup(".m3-scaffold-main-pane"),
                "main slot"
        );
        return new ScaffoldFixture(
                stage,
                scaffold,
                leadingPane,
                leadingSlot,
                mainSlot,
                leadingSlot.getWidth(),
                mainSlot.getWidth()
        );
    }

    /// Closes a fixture and clears its local reduced-motion request.
    ///
    /// @param fixture the fixture to close, or `null` when setup failed
    private static void closeFixture(@Nullable ScaffoldFixture fixture) {
        if (fixture == null) {
            return;
        }
        FxTestUtils.runOnFxThread(() -> {
            M3MotionSettings.setReducedMotionRequested(fixture.scaffold(), false);
            fixture.stage().hide();
        });
    }

    /// Retains the stage, scaffold, stable slots, and initial geometry used by one motion test.
    ///
    /// @param stage               the presenting stage
    /// @param scaffold            the adaptive scaffold under test
    /// @param leadingPane         the application node installed in the leading slot
    /// @param leadingSlot         the skin's stable leading slot
    /// @param mainSlot            the skin's stable main slot
    /// @param initialLeadingWidth the leading-slot width in fixed-leading layout
    /// @param initialMainWidth    the main-slot width in fixed-leading layout
    private record ScaffoldFixture(
            Stage stage,
            M3AdaptiveScaffold scaffold,
            Pane leadingPane,
            StackPane leadingSlot,
            StackPane mainSlot,
            double initialLeadingWidth,
            double initialMainWidth
    ) {
    }
}
