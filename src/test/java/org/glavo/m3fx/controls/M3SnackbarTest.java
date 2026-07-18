// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the immutable message contract of [M3Snackbar].
@NotNullByDefault
final class M3SnackbarTest {
    /// Verifies the content and affordances exposed by each supported message form.
    @Test
    void messagesExposeImmutableContentAndAffordances() {
        AtomicInteger invocations = new AtomicInteger();
        M3Snackbar.Action action = new M3Snackbar.Action("Undo", invocations::incrementAndGet);
        M3Snackbar plain = new M3Snackbar("Saved");
        M3Snackbar actionable = new M3Snackbar("Deleted", action);
        M3Snackbar dismissible = new M3Snackbar("Offline", null, true);
        M3Snackbar complete = new M3Snackbar("Archived", action, true);

        assertEquals("Saved", plain.text());
        assertNull(plain.action());
        assertFalse(plain.hasAction());
        assertFalse(plain.closeButtonVisible());

        assertEquals("Deleted", actionable.text());
        assertSame(action, actionable.action());
        assertTrue(actionable.hasAction());
        assertFalse(actionable.closeButtonVisible());

        assertFalse(dismissible.hasAction());
        assertTrue(dismissible.closeButtonVisible());
        assertSame(action, complete.action());
        assertTrue(complete.closeButtonVisible());

        assertEquals("Undo", action.text());
        action.handler().run();
        assertEquals(1, invocations.get());
    }

    /// Verifies that required message and action values reject null or blank input.
    @SuppressWarnings("DataFlowIssue")
    @Test
    void requiredValuesRejectNullAndBlankInput() {
        Runnable handler = () -> {
        };

        assertThrows(NullPointerException.class, () -> new M3Snackbar(null));
        assertThrows(IllegalArgumentException.class, () -> new M3Snackbar(""));
        assertThrows(IllegalArgumentException.class, () -> new M3Snackbar(" \t\n"));
        assertThrows(NullPointerException.class, () -> new M3Snackbar("Saved", null));

        assertThrows(NullPointerException.class, () -> new M3Snackbar.Action(null, handler));
        assertThrows(IllegalArgumentException.class, () -> new M3Snackbar.Action("", handler));
        assertThrows(IllegalArgumentException.class, () -> new M3Snackbar.Action("  ", handler));
        assertThrows(NullPointerException.class, () -> new M3Snackbar.Action("Undo", null));
        assertDoesNotThrow(() -> new M3Snackbar("Saved", null, false));
    }
}
