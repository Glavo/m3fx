// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies scrim opacity validation before direct assignments and while reading bound values.
@NotNullByDefault
final class M3ScrimOpacityTest {
    /// Starts JavaFX before constructing scrims.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Rejects invalid property writes without changing logical or rendered opacity or notifying listeners.
    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -0.1, 1.1})
    void propertyRejectsInvalidValuesBeforeAssignment(double invalidValue) {
        FxTestUtils.runOnFxThread(() -> {
            M3Scrim scrim = new M3Scrim();
            scrim.setVisibleOpacity(0.4);
            AtomicInteger changes = new AtomicInteger();
            scrim.visibleOpacityProperty().addListener(observable -> changes.incrementAndGet());

            assertThrows(IllegalArgumentException.class, () -> scrim.visibleOpacityProperty().set(invalidValue));
            assertEquals(0.4, scrim.getVisibleOpacity());
            assertEquals(0.4, scrim.getOpacity());
            assertEquals(0, changes.get());
            scrim.hide();
            scrim.show();
            assertEquals(0.4, scrim.getOpacity());
        });
    }

    /// Rejects reads of an invalid bound value and resumes normal rendering when its source is corrected.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void invalidBindingDoesNotExposeUncheckedOpacityAndCanRecover(boolean unbind) {
        FxTestUtils.runOnFxThread(() -> {
            M3Scrim scrim = new M3Scrim();
            SimpleDoubleProperty source = new SimpleDoubleProperty(0.4);
            scrim.visibleOpacityProperty().bind(source);
            List<Throwable> failures = new ArrayList<>();
            Thread thread = Thread.currentThread();
            Thread.@Nullable UncaughtExceptionHandler previousHandler = thread.getUncaughtExceptionHandler();
            thread.setUncaughtExceptionHandler((ignored, failure) -> failures.add(failure));
            try {
                source.set(1.1);
                assertTrue(scrim.visibleOpacityProperty().isBound());
                assertThrows(IllegalArgumentException.class, scrim::getVisibleOpacity);
                assertThrows(IllegalArgumentException.class, () -> scrim.visibleOpacityProperty().get());
                assertEquals(0.4, scrim.getOpacity());
                assertFalse(failures.isEmpty());
                assertTrue(failures.stream().allMatch(IllegalArgumentException.class::isInstance), failures::toString);

                if (unbind) {
                    scrim.visibleOpacityProperty().unbind();
                    scrim.setVisibleOpacity(0.6);
                } else {
                    source.set(0.6);
                }
                assertEquals(0.6, scrim.getVisibleOpacity());
                assertEquals(0.6, scrim.getOpacity());
            } finally {
                scrim.visibleOpacityProperty().unbind();
                thread.setUncaughtExceptionHandler(previousHandler);
            }
        });
    }
}
