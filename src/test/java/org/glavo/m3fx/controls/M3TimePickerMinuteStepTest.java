// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.AccessibleAction;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies that invalid minute steps cannot corrupt time-picker adjustments or escape through property reads.
@NotNullByDefault
final class M3TimePickerMinuteStepTest {
    /// Starts JavaFX before constructing time pickers.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves the previous step and selection when a property assignment is rejected.
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 7, 31, 60, Integer.MAX_VALUE})
    void propertyRejectsInvalidStepBeforeAssignment(int invalidStep) {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 17));
            AtomicInteger changes = new AtomicInteger();
            picker.minuteStepProperty().addListener(observable -> changes.incrementAndGet());

            assertThrows(IllegalArgumentException.class, () -> picker.minuteStepProperty().set(invalidStep));
            assertEquals(5, picker.getMinuteStep());
            assertEquals(0, changes.get());
            assertEquals(LocalTime.of(10, 17), picker.getValue());
            picker.executeAccessibleAction(AccessibleAction.INCREMENT);
            assertEquals(LocalTime.of(10, 22), picker.getValue());
        });
    }

    /// Rejects invalid bound steps before arithmetic and supports correcting or removing the binding.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void invalidBindingRejectsReadsAndAdjustmentsUntilCorrected(boolean unbind) {
        FxTestUtils.runOnFxThread(() -> {
            M3TimePicker picker = new M3TimePicker(LocalTime.of(10, 17));
            SimpleIntegerProperty source = new SimpleIntegerProperty(5);
            picker.minuteStepProperty().bind(source);
            List<Throwable> failures = new ArrayList<>();
            Thread thread = Thread.currentThread();
            Thread.@Nullable UncaughtExceptionHandler previousHandler = thread.getUncaughtExceptionHandler();
            thread.setUncaughtExceptionHandler((ignored, failure) -> failures.add(failure));
            try {
                source.set(0);
                assertTrue(picker.minuteStepProperty().isBound());
                assertThrows(IllegalArgumentException.class, picker::getMinuteStep);
                assertThrows(IllegalArgumentException.class, () -> picker.minuteStepProperty().get());
                assertThrows(IllegalArgumentException.class, () ->
                        picker.executeAccessibleAction(AccessibleAction.INCREMENT));
                assertEquals(LocalTime.of(10, 17), picker.getValue());
                assertFalse(failures.isEmpty());
                assertTrue(failures.stream().allMatch(IllegalArgumentException.class::isInstance), failures::toString);

                if (unbind) {
                    picker.minuteStepProperty().unbind();
                    picker.setMinuteStep(10);
                } else {
                    source.set(10);
                }
                assertEquals(10, picker.getMinuteStep());
                assertEquals(LocalTime.of(10, 17), picker.getValue());
                picker.executeAccessibleAction(AccessibleAction.INCREMENT);
                assertEquals(LocalTime.of(10, 27), picker.getValue());
            } finally {
                picker.minuteStepProperty().unbind();
                thread.setUncaughtExceptionHandler(previousHandler);
            }
        });
    }
}
