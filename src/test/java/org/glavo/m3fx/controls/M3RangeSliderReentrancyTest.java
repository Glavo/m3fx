// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleDoubleProperty;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies range normalization when endpoint listeners update the same slider.
@NotNullByDefault
final class M3RangeSliderReentrancyTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Reconciles a newer minimum written while the lower endpoint is being clamped.
    @Test
    void lowerListenerChangesMinimum() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 10, 90);
            AtomicBoolean changed = new AtomicBoolean();
            slider.lowValueProperty().addListener((observable, oldValue, newValue) -> {
                if (changed.compareAndSet(false, true)) {
                    slider.setMin(95);
                }
            });
            slider.setMin(20);
            assertSelection(slider, 95, 95);
        });
    }

    /// Reconciles a newer maximum written while the upper endpoint is being clamped.
    @Test
    void upperListenerChangesMaximum() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 10, 90);
            AtomicBoolean changed = new AtomicBoolean();
            slider.highValueProperty().addListener((observable, oldValue, newValue) -> {
                if (changed.compareAndSet(false, true)) {
                    slider.setMax(5);
                }
            });
            slider.setMax(80);
            assertSelection(slider, 5, 5);
        });
    }

    /// Preserves an upper-endpoint edit instead of overwriting it with an earlier step calculation.
    @Test
    void lowerListenerChangesUpperEndpoint() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 13, 87);
            slider.lowValueProperty().addListener((observable, oldValue, newValue) -> slider.setHighValue(70));
            slider.setStepSize(10);
            assertSelection(slider, 10, 70);
        });
    }

    /// Applies ordinary upper-endpoint ordering to edits made during lower-endpoint normalization.
    @Test
    void lowerListenerCannotMoveUpperBelowLower() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 10, 90);
            slider.lowValueProperty().addListener((observable, oldValue, newValue) -> slider.setHighValue(-10));
            slider.setMin(20);
            assertSelection(slider, 20, 20);
        });
    }

    /// Applies ordinary lower-endpoint ordering to edits made during upper-endpoint normalization.
    @Test
    void upperListenerCannotMoveLowerAboveUpper() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 10, 90);
            slider.highValueProperty().addListener((observable, oldValue, newValue) -> slider.setLowValue(100));
            slider.setMax(80);
            assertSelection(slider, 80, 80);
        });
    }

    /// Uses a step interval changed by a lower-endpoint listener for both selected values.
    @Test
    void lowerListenerChangesStep() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 13, 87);
            AtomicBoolean changed = new AtomicBoolean();
            slider.lowValueProperty().addListener((observable, oldValue, newValue) -> {
                if (changed.compareAndSet(false, true)) {
                    slider.setStepSize(25);
                }
            });
            slider.setStepSize(10);
            assertSelection(slider, 0, 75);
        });
    }

    /// Observes another range change even when listeners do not read the invalidated endpoint.
    @Test
    void invalidationListenerChangesMinimum() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 10, 90);
            AtomicBoolean changed = new AtomicBoolean();
            slider.lowValueProperty().addListener((InvalidationListener) observable -> {
                if (changed.compareAndSet(false, true)) {
                    slider.setMin(95);
                }
            });
            slider.setMin(20);
            assertSelection(slider, 95, 95);
        });
    }

    /// Preserves a valid upper-endpoint binding installed during a lower-endpoint notification.
    @Test
    void lowerListenerBindsUpperEndpoint() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 13, 87);
            SimpleDoubleProperty upper = new SimpleDoubleProperty(70);
            slider.lowValueProperty().addListener((observable, oldValue, newValue) -> slider.highValueProperty().bind(upper));
            slider.setStepSize(10);
            assertTrue(slider.highValueProperty().isBound());
            assertSelection(slider, 10, 70);
            upper.set(60);
            assertSelection(slider, 10, 60);
        });
    }

    /// Rejects an incompatible binding before writing either endpoint and permits subsequent normalization.
    @Test
    void normalizationRecoversAfterBoundEndpointFailure() {
        FxTestUtils.runOnFxThread(() -> {
            M3RangeSlider slider = new M3RangeSlider(0, 100, 13, 87);
            slider.highValueProperty().bind(new SimpleDoubleProperty(87));
            assertThrows(RuntimeException.class, () -> slider.setStepSize(10));
            assertSelection(slider, 13, 87);
            slider.highValueProperty().unbind();
            slider.setStepSize(0);
            slider.setStepSize(10);
            assertSelection(slider, 10, 90);
        });
    }

    /// Compares both selected endpoints after all synchronous callbacks have completed.
    private static void assertSelection(M3RangeSlider slider, double low, double high) {
        assertEquals(low, slider.getLowValue());
        assertEquals(high, slider.getHighValue());
    }
}
