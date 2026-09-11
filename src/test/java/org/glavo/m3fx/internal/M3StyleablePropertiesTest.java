// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.StyleOrigin;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies validation and CSS ownership through public component-token properties.
@NotNullByDefault
final class M3StyleablePropertiesTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Rejects invalid container updates before taking ownership from CSS.
    @Test
    void invalidControlAssignmentsPreserveCssEligibility() {
        FxTestUtils.runOnFxThread(() -> {
            StyleableDoubleProperty property = new M3Button().containerHeightProperty();
            double initialValue = property.get();
            assertThrows(IllegalArgumentException.class, () -> M3Css.setControlToken(property, -1.0));
            assertEquals(initialValue, property.get());
            assertTrue(M3Css.isSettable(property));
        });
    }

    /// Preserves application ownership acquired by a listener during a container update.
    @Test
    void reentrantApplicationAssignmentTakesPrecedenceOverControl() {
        FxTestUtils.runOnFxThread(() -> {
            StyleableDoubleProperty property = new M3Button().containerHeightProperty();
            property.addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 48.0) {
                    property.set(56.0);
                }
            });
            M3Css.setControlToken(property, 48.0);
            M3Css.setControlToken(property, 64.0);
            M3Css.clearControlToken(property);
            assertEquals(56.0, property.get());
            assertFalse(M3Css.isSettable(property));
        });
    }

    /// Resumes CSS after release while preserving later application assignments, including equal values.
    @Test
    void releasingControlTokenPreservesExplicitOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            StyleableDoubleProperty property = new M3Button().containerHeightProperty();
            M3Css.setControlToken(property, 48.0);
            assertFalse(M3Css.isSettable(property));
            M3Css.clearControlToken(property);
            M3Css.clearControlToken(property);
            assertEquals(48.0, property.get());
            assertTrue(M3Css.isSettable(property));
            M3Css.setControlToken(property, 56.0);
            property.set(56.0);
            M3Css.clearControlToken(property);
            M3Css.setControlToken(property, 64.0);
            assertEquals(56.0, property.get());
            assertFalse(M3Css.isSettable(property));
        });
    }

    /// Rejects invalid direct assignments without changing the value, origin, or CSS eligibility.
    @Test
    void invalidNumericAssignmentsPreservePropertyState() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button();
            StyleableDoubleProperty property = button.containerHeightProperty();
            property.applyStyle(StyleOrigin.AUTHOR, 48.0);
            List<Number> changes = new ArrayList<>();
            property.addListener((observable, oldValue, newValue) -> changes.add(newValue));

            for (double invalid : new double[]{-1.0, Double.NaN, Double.POSITIVE_INFINITY}) {
                assertThrows(IllegalArgumentException.class, () -> property.set(invalid));
                assertEquals(48.0, button.getContainerHeight());
                assertEquals(StyleOrigin.AUTHOR, property.getStyleOrigin());
                assertTrue(M3Css.isSettable(property));
            }
            assertTrue(changes.isEmpty());
            property.applyStyle(StyleOrigin.AUTHOR, 56.0);
            assertEquals(List.of(56.0), changes);
        });
    }

    /// Rejects invalid CSS assignments without losing the last accepted CSS value.
    @Test
    void invalidCssAssignmentsPreservePropertyState() {
        FxTestUtils.runOnFxThread(() -> {
            StyleableDoubleProperty property = new M3Button().containerHeightProperty();
            property.applyStyle(StyleOrigin.AUTHOR, 48.0);

            assertThrows(IllegalArgumentException.class, () -> property.applyStyle(StyleOrigin.INLINE, -1.0));

            assertEquals(48.0, property.get());
            assertEquals(StyleOrigin.AUTHOR, property.getStyleOrigin());
            assertTrue(M3Css.isSettable(property));
            property.set(56.0);
            assertFalse(M3Css.isSettable(property));
        });
    }

    /// Preserves finite negative spacing while rejecting non-finite values through the property API.
    @Test
    void finitePropertiesPreserveValidNegativeValues() {
        FxTestUtils.runOnFxThread(() -> {
            M3SegmentedButtonGroup group = new M3SegmentedButtonGroup();
            group.spacingProperty().set(-2.0);

            assertThrows(IllegalArgumentException.class, () -> group.spacingProperty().set(Double.NaN));

            assertEquals(-2.0, group.getSpacing());
            assertFalse(M3Css.isSettable(group.spacingProperty()));
        });
    }

    /// Allows CSS to resume after a failed direct assignment to a temporarily bound numeric property.
    @Test
    void failedBoundNumericAssignmentDoesNotClaimCssOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            StyleableDoubleProperty property = new M3Button().containerHeightProperty();
            property.bind(new SimpleDoubleProperty(48.0));

            assertThrows(RuntimeException.class, () -> property.set(56.0));
            property.unbind();

            assertEquals(48.0, property.get());
            assertTrue(M3Css.isSettable(property));
            property.applyStyle(StyleOrigin.AUTHOR, 64.0);
            assertEquals(64.0, property.get());
        });
    }

    /// Allows CSS to resume after a failed direct assignment to a temporarily bound paint property.
    @Test
    void failedBoundObjectAssignmentDoesNotClaimCssOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            StyleableObjectProperty<@Nullable Paint> property = new M3Button().containerColorProperty();
            property.bind(new SimpleObjectProperty<>(Color.RED));

            assertThrows(RuntimeException.class, () -> property.set(Color.BLUE));
            property.unbind();

            assertEquals(Color.RED, property.get());
            assertTrue(M3Css.isSettable(property));
            property.applyStyle(StyleOrigin.AUTHOR, Color.GREEN);
            assertEquals(Color.GREEN, property.get());
        });
    }

    /// Keeps explicit assignments authoritative even when they repeat the current CSS value.
    @Test
    void unchangedApplicationAssignmentsClaimCssOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button();
            StyleableDoubleProperty height = button.containerHeightProperty();
            StyleableObjectProperty<@Nullable Paint> color = button.containerColorProperty();
            height.applyStyle(StyleOrigin.AUTHOR, 48.0);
            color.applyStyle(StyleOrigin.AUTHOR, Color.RED);

            height.set(48.0);
            color.set(Color.RED);

            assertFalse(M3Css.isSettable(height));
            assertFalse(M3Css.isSettable(color));
        });
    }

    /// Rejects invalid initial values before publishing a token property.
    @Test
    void factoriesValidateInitialNumericValues() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button();
            var metadata = button.containerHeightProperty().getCssMetaData();

            assertThrows(IllegalArgumentException.class, () -> M3Css.nonNegativeStyleableDoubleProperty(
                    -1.0, button, "height", metadata, button::requestLayout));
            assertThrows(IllegalArgumentException.class, () -> M3Css.finiteStyleableDoubleProperty(
                    Double.NaN, button, "height", metadata, button::requestLayout));
        });
    }
}
