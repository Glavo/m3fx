// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3BottomAppBarSkin;
import org.glavo.m3fx.skins.M3ProgressBarSkin;
import org.glavo.m3fx.skins.M3TopAppBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies bar height constraints without inferring ownership from numeric equality.
@NotNullByDefault
final class M3BarLayoutSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains an explicit preferred height even when it equals the computed default.
    @ParameterizedTest
    @ValueSource(strings = {"top", "bottom", "progress"})
    void explicitHeightEqualToDefaultRemainsExplicit(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            BarCase fixture = createCase(kind);
            Control control = fixture.control();
            double originalHeight = control.prefHeight(-1);
            control.setPrefHeight(originalHeight);
            fixture.changeSize().run();
            assertEquals(originalHeight, control.prefHeight(-1));
        });
    }

    /// Leaves the last bound value in effect after unbinding until the application resets it.
    @ParameterizedTest
    @ValueSource(strings = {"top", "bottom", "progress"})
    void unbindingDoesNotTransferHeightOwnershipToTokens(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            BarCase fixture = createCase(kind);
            Control control = fixture.control();
            SimpleDoubleProperty height = new SimpleDoubleProperty(90.0);
            control.prefHeightProperty().bind(height);
            fixture.changeSize().run();
            control.prefHeightProperty().unbind();
            height.set(120.0);
            fixture.changeSize().run();
            assertEquals(90.0, control.prefHeight(-1));
        });
    }

    /// Uses the same total height before and after skin installation, including custom padding.
    @ParameterizedTest
    @ValueSource(strings = {"top", "bottom", "progress"})
    void computedHeightIsIndependentOfSkinInstallation(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            BarCase fixture = createCase(kind);
            Control control = fixture.control();
            control.setPadding(new Insets(3.0, 4.0, 5.0, 6.0));
            control.setMinHeight(20.0);
            control.setPrefHeight(30.0);
            control.setMaxHeight(40.0);
            fixture.changeSize().run();
            control.setMinHeight(Region.USE_COMPUTED_SIZE);
            control.setPrefHeight(Region.USE_COMPUTED_SIZE);
            control.setMaxHeight(Region.USE_COMPUTED_SIZE);
            assertHeight(fixture);
            fixture.installSkin().run();
            assertHeight(fixture);
            control.setSkin(null);
        });
    }

    /// Checks all computed height constraints against the token-derived total height.
    private static void assertHeight(BarCase fixture) {
        assertEquals(fixture.expectedHeight(), fixture.control().minHeight(-1));
        assertEquals(fixture.expectedHeight(), fixture.control().prefHeight(-1));
        assertEquals(fixture.expectedHeight(), fixture.control().maxHeight(-1));
    }

    /// Creates repeatable token changes that finish at a known height.
    private static BarCase createCase(String kind) {
        return switch (kind) {
            case "top" -> {
                M3TopAppBar bar = new M3TopAppBar();
                SimpleDoubleProperty collapseProgress = new SimpleDoubleProperty(0.0);
                bar.collapseProgressProperty().bind(collapseProgress);
                yield new BarCase(bar, () -> {
                    bar.setVariant(M3TopAppBarVariant.MEDIUM_FLEXIBLE);
                    bar.setSubtitle("Subtitle");
                    bar.setContainerHeight(64.0);
                    bar.setMediumFlexibleSubtitleContainerHeight(136.0);
                    collapseProgress.set(0.0);
                    collapseProgress.set(0.5);
                }, () -> bar.setSkin(new M3TopAppBarSkin(bar)), 100.0);
            }
            case "bottom" -> {
                M3BottomAppBar bar = new M3BottomAppBar();
                yield new BarCase(bar, () -> {
                    bar.setContainerHeight(80.0);
                    bar.setContainerHeight(86.0);
                }, () -> bar.setSkin(new M3BottomAppBarSkin(bar)), 86.0);
            }
            case "progress" -> {
                M3ProgressBar bar = new M3ProgressBar();
                yield new BarCase(bar, () -> {
                    bar.setTrackThickness(4.0);
                    bar.setWaveAmplitude(0.0);
                    bar.setWaveAmplitude(3.0);
                }, () -> bar.setSkin(new M3ProgressBarSkin(bar)), 10.0);
            }
            default -> throw new IllegalArgumentException("Unknown bar kind: " + kind);
        };
    }

    /// Supplies a bar, its token changes, and its default skin installation.
    ///
    /// @param control the bar under test
    /// @param changeSize the repeatable token changes
    /// @param installSkin installs the bar's default skin
    /// @param expectedHeight the resulting computed total height
    @NotNullByDefault
    private record BarCase(Control control, Runnable changeSize, Runnable installSkin, double expectedHeight) {
    }
}
