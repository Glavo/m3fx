// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.tokens;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Tests density-sensitive component token generation.
@NotNullByDefault
final class M3DensityComponentTokensTest {
    /// Verifies the compactness calculation and its finite-input requirement.
    @Test
    void compactsEligibleMetricsWithoutProducingNegativeValues() {
        M3Density compact = M3Density.of(-1.0);
        M3Density minimum = M3Density.of(-4.0);

        assertEquals(36.0, compact.compact(40.0));
        assertEquals(0.0, minimum.compact(8.0));
        assertThrows(IllegalArgumentException.class, () -> compact.compact(Double.NaN));
    }

    /// Verifies that density changes only generated component metrics which support compact layouts.
    @Test
    void preservesFixedVisualMetricsWhileCompactingVerticalLayout() {
        M3ComponentTokens baseline = generatedTokens(M3Density.standard());
        M3ComponentTokens compact = generatedTokens(M3Density.of(-1.0));
        M3ComponentTokens minimum = generatedTokens(M3Density.of(-4.0));

        assertEquals(36.0, compact.filledButton().height());
        assertEquals(36.0, compact.buttonSizing().small().containerHeight());
        assertEquals(4.0, compact.listItem().verticalPadding());
        assertEquals(0.0, minimum.listItem().verticalPadding());
        assertEquals(2.0, minimum.listItem().segmentedGap());
        assertEquals(48.0, minimum.listItem().oneLineHeight());
        assertEquals(56.0, minimum.listItem().twoLineHeight());
        assertEquals(72.0, minimum.listItem().threeLineHeight());
        assertEquals(48.0, minimum.listItem().sectionHeaderHeight());
        assertEquals(48.0, minimum.navigationDrawer().oneLineItemHeight());

        assertEquals(baseline.icon().mediumSize(), minimum.icon().mediumSize());
        assertEquals(baseline.iconButton().small().containerHeight(), minimum.iconButton().small().containerHeight());
        assertEquals(baseline.iconButton().small().outlineWidth(), minimum.iconButton().small().outlineWidth());
        assertEquals(baseline.iconButton().extraLarge().containerHeight(), minimum.iconButton().extraLarge().containerHeight());
        assertEquals(baseline.iconButton().extraLarge().iconSize(), minimum.iconButton().extraLarge().iconSize());
        assertEquals(baseline.chip().outlineWidth(), minimum.chip().outlineWidth());
        assertEquals(baseline.divider().thickness(), minimum.divider().thickness());
        assertEquals(baseline.progress().thickness(), minimum.progress().thickness());
        assertEquals(baseline.progress().linearWaveAmplitude(), minimum.progress().linearWaveAmplitude());
        assertEquals(baseline.badge().largeHeight(), minimum.badge().largeHeight());
        assertEquals(baseline.selection().touchTargetSize(), minimum.selection().touchTargetSize());
        assertEquals(baseline.selection().switchTrackWidth(), minimum.selection().switchTrackWidth());
    }

    /// Creates generated component tokens using the baseline profile and shape scale.
    private static M3ComponentTokens generatedTokens(M3Density density) {
        return M3ComponentTokens.builder(M3Profile.BASELINE_2021, M3ShapeTokens.baseline(), density).build();
    }
}
