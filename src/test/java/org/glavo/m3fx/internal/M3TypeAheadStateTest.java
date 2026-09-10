// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies shared type-ahead input filtering, prefix retries, and search failure state.
@NotNullByDefault
final class M3TypeAheadStateTest {
    /// Starts JavaFX for the prefix inactivity timer.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains successful prefixes and retries an unmatched extension with only the latest character.
    @Test
    void failedExtensionRetriesLatestCharacter() {
        FxTestUtils.runOnFxThread(() -> {
            M3TypeAheadState state = new M3TypeAheadState(new Pane());
            try {
                List<String> attempts = new ArrayList<>();
                Function<String, @Nullable String> finder = prefix -> {
                    attempts.add(prefix);
                    return List.of("a", "ab", "b").contains(prefix) ? prefix : null;
                };

                assertEquals("a", state.find(typed("a"), finder));
                assertEquals("ab", state.find(typed("b"), finder));
                KeyEvent repeated = typed("b");
                assertEquals("b", state.find(repeated, finder));

                assertEquals(List.of("a", "ab", "abb", "b"), attempts);
                assertEquals("b", state.getPrefix());
                assertFalse(repeated.isConsumed());
            } finally {
                state.clear();
            }
        });
    }

    /// Leaves the active prefix untouched when input belongs to shortcuts or is not one printable code point.
    @Test
    void ignoredEventsDoNotRunSearchOrChangePrefix() {
        FxTestUtils.runOnFxThread(() -> {
            M3TypeAheadState state = new M3TypeAheadState(new Pane());
            try {
                state.find(typed("a"), prefix -> prefix);
                List<String> attempts = new ArrayList<>();
                for (KeyEvent event : List.of(
                        typed(""), typed("ab"), typed(" "), typed("\n"), typed("\uD801"), typed("\uDC00"),
                        new KeyEvent(KeyEvent.KEY_TYPED, "b", "", KeyCode.UNDEFINED, false, true, false, false),
                        new KeyEvent(KeyEvent.KEY_TYPED, "b", "", KeyCode.UNDEFINED, false, false, true, false),
                        new KeyEvent(KeyEvent.KEY_TYPED, "b", "", KeyCode.UNDEFINED, false, false, false, true),
                        new KeyEvent(KeyEvent.KEY_PRESSED, "b", "", KeyCode.B, false, false, false, false))) {
                    assertNull(state.find(event, prefix -> {
                        attempts.add(prefix);
                        return prefix;
                    }));
                    assertEquals("a", state.getPrefix());
                    assertFalse(event.isConsumed());
                }
                assertTrue(attempts.isEmpty());
            } finally {
                state.clear();
            }
        });
    }

    /// Accepts Shift and treats a supplementary Unicode letter as one character.
    @Test
    void acceptsShiftAndSupplementaryCharacters() {
        FxTestUtils.runOnFxThread(() -> {
            M3TypeAheadState state = new M3TypeAheadState(new Pane());
            try {
                assertEquals("s", state.find(new KeyEvent(
                        KeyEvent.KEY_TYPED, "S", "", KeyCode.UNDEFINED, true, false, false, false), prefix -> prefix));
                state.clear();
                assertEquals("\uD801\uDC28", state.find(typed("\uD801\uDC00"), prefix -> prefix));
            } finally {
                state.clear();
            }
        });
    }

    /// Does not retry the same search merely because one character expands when normalized.
    @Test
    void expandingFirstCharacterIsSearchedOnce() {
        FxTestUtils.runOnFxThread(() -> {
            M3TypeAheadState state = new M3TypeAheadState(new Pane());
            try {
                List<String> attempts = new ArrayList<>();
                assertNull(state.find(typed("\u0130"), prefix -> {
                    attempts.add(prefix);
                    return null;
                }));
                assertEquals(List.of("i\u0307"), attempts);
                assertEquals("i\u0307", state.getPrefix());
            } finally {
                state.clear();
            }
        });
    }

    /// Preserves the attempted prefix after a lookup failure and allows explicit clearing to recover.
    @Test
    void searchFailureRetainsPrefixUntilCleared() {
        FxTestUtils.runOnFxThread(() -> {
            M3TypeAheadState state = new M3TypeAheadState(new Pane());
            try {
                state.find(typed("a"), prefix -> prefix);
                assertThrows(IllegalStateException.class, () -> state.find(typed("b"), prefix -> {
                    throw new IllegalStateException("Search failed");
                }));
                assertEquals("ab", state.getPrefix());
                state.clear();
                assertEquals("c", state.find(typed("c"), prefix -> prefix));
            } finally {
                state.clear();
            }
        });
    }

    /// Preserves ordinary case-insensitive matching while supporting expanding lowercase mappings.
    @Test
    void matchesNormalizedUnicodePrefixes() {
        assertTrue(M3SelectionNavigation.matchesTypeAheadPrefix("  Settings", "se"));
        assertFalse(M3SelectionNavigation.matchesTypeAheadPrefix("Archive", "se"));
        assertTrue(M3SelectionNavigation.matchesTypeAheadPrefix("\u0130tem", "i\u0307"));
        assertFalse(M3SelectionNavigation.matchesTypeAheadPrefix("Item", "i\u0307"));
        assertTrue(M3SelectionNavigation.matchesTypeAheadPrefix("\u03A3", "\u03C2"));
        assertTrue(M3SelectionNavigation.matchesTypeAheadPrefix("\uD801\uDC00 item", "\uD801\uDC28"));
    }

    /// Creates an unmodified typed event without assigning a focus or selection target.
    private static KeyEvent typed(String character) {
        return new KeyEvent(KeyEvent.KEY_TYPED, character, "", KeyCode.UNDEFINED, false, false, false, false);
    }
}
