// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Verifies shared JavaFX CSS value serialization.
@NotNullByDefault
final class M3CssTest {
    /// Verifies compact, locale-independent pixel values without losing non-integral precision.
    @Test
    void formatsPixelValues() {
        assertEquals("0px", M3Css.pixels(-0.0));
        assertEquals("24px", M3Css.pixels(24.0));
        assertEquals("-2.5px", M3Css.pixels(-2.5));
        assertEquals("0.3333333333333333px", M3Css.pixels(1.0 / 3.0));
        assertEquals("-9223372036854775808px", M3Css.pixels(-0x1.0p63));
        assertEquals("9.223372036854776E18px", M3Css.pixels(0x1.0p63));
        assertEquals("1.0E20px", M3Css.pixels(1.0E20));
    }

    /// Verifies that non-finite values are rejected before invalid CSS is constructed.
    @Test
    void rejectsNonFinitePixelValues() {
        assertThrows(IllegalArgumentException.class, () -> M3Css.pixels(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> M3Css.pixels(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> M3Css.pixels(Double.NEGATIVE_INFINITY));
    }
}
