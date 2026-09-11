// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Verifies minimal style-class mutations and validation before observable changes.
@NotNullByDefault
final class M3ControlStylesTest {
    /// Starts JavaFX before constructing styled nodes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains the selected class in place without notifying listeners when the variant is already correct.
    @Test
    void unchangedVariantDoesNotMutateClasses() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.getStyleClass().setAll("custom-before", "selected", "custom-after");
            AtomicInteger changes = new AtomicInteger();
            node.getStyleClass().addListener((ListChangeListener<String>) change -> changes.incrementAndGet());
            M3ControlStyles.replaceVariant(node, "selected", "other", "selected");
            M3ControlStyles.replaceVariant(node, "selected", "other", "selected");
            assertEquals(List.of("custom-before", "selected", "custom-after"), node.getStyleClass());
            assertEquals(0, changes.get());
        });
    }

    /// Preserves caller-supplied selected occurrences while removing the unselected variant.
    @Test
    void selectedOccurrencesRemainUntouched() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.getStyleClass().setAll("selected", "custom", "selected", "other");
            M3ControlStyles.replaceVariant(node, "selected", "selected", "other");
            assertEquals(List.of("selected", "custom", "selected"), node.getStyleClass());
        });
    }

    /// Keeps unrelated class ordering and appends a newly selected variant.
    @Test
    void changedVariantPreservesUnrelatedClasses() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.getStyleClass().setAll("custom-before", "old", "custom-after");
            M3ControlStyles.replaceVariant(node, "new", "old", "new");
            assertEquals(List.of("custom-before", "custom-after", "new"), node.getStyleClass());
        });
    }

    /// Rejects a null selected class before removing the previous variant.
    @Test
    void nullSelectionLeavesClassesUnchanged() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.getStyleClass().setAll("custom", "old");
            assertThrows(NullPointerException.class, () -> M3ControlStyles.replaceVariant(node, null, "old"));
            assertEquals(List.of("custom", "old"), node.getStyleClass());
        });
    }

    /// Validates the complete variant array before applying removals.
    @Test
    void nullVariantLeavesClassesUnchanged() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            node.getStyleClass().setAll("custom", "old");
            assertThrows(NullPointerException.class, () -> M3ControlStyles.replaceVariant(node, "new", "old", null));
            assertEquals(List.of("custom", "old"), node.getStyleClass());
        });
    }

    /// Does not insert a null class through the common add operation.
    @Test
    void nullAddedClassLeavesListUnchanged() {
        FxTestUtils.runOnFxThread(() -> {
            Pane node = new Pane();
            assertThrows(NullPointerException.class, () -> M3ControlStyles.add(node, null));
            assertEquals(List.of(), node.getStyleClass());
        });
    }
}
