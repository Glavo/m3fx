// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.localization;

import javafx.scene.Scene;
import javafx.collections.MapChangeListener;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.M3LocalizerObserver;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies localization inheritance, replacement, popup propagation, and observation lifetime.
@NotNullByDefault
final class M3LocalizationTest {
    /// Starts JavaFX before constructing scene graphs.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Separates default English messages from locale-dependent calendar formatting.
    @Test
    void englishDefaultsAndRegionalFormatting() {
        M3Localizer localizer = new M3Localizer(Locale.GERMANY);
        assertSame(Locale.GERMANY, localizer.getLocale());
        assertEquals("Clear text", localizer.getClearActionText());
        assertEquals("Cancel", localizer.getCancelText());
        assertEquals("Januar", localizer.formatMonth(Month.JANUARY, TextStyle.FULL_STANDALONE));
        assertEquals(DayOfWeek.MONDAY, localizer.getFirstDayOfWeek());
        assertEquals("04", localizer.formatMinute(4));
        assertFalse(localizer.formatDate(LocalDate.of(2026, 1, 5)).isBlank());
        assertThrows(NullPointerException.class, () -> new M3Localizer(null));
        assertThrows(NullPointerException.class, () -> localizer.formatMonth(null, TextStyle.FULL));
        assertThrows(NullPointerException.class, () -> M3Localizer.install(new Pane(), null));
    }

    /// Allows subclasses to override only messages they translate.
    @Test
    void partialOverridesRetainEnglishFallback() {
        M3Localizer localizer = new M3Localizer(Locale.GERMAN) {
            /// Supplies an application-owned translation.
            @Override
            public String getClearActionText() {
                return "Text entfernen";
            }
        };
        assertEquals("Text entfernen", localizer.getClearActionText());
        assertEquals("Dismiss", localizer.getDismissText());
    }

    /// Resolves nearest installations, avoids same-instance refreshes, and restores inherited values.
    @Test
    void replacementAndNestedOverrides() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane branch = new Pane(leaf);
            Pane root = new Pane(branch);
            M3Localizer fallback = M3Localizer.getEffectiveLocalizer(leaf);
            M3Localizer english = new M3Localizer(Locale.US);
            M3Localizer german = new M3Localizer(Locale.GERMANY);
            List<M3Localizer> changes = new ArrayList<>();
            try (M3LocalizerObserver observer = new M3LocalizerObserver(leaf, changes::add)) {
                M3Localizer.install(root, english);
                M3Localizer.install(branch, german);
                M3Localizer.install(root, german);
                M3Localizer.install(branch, german);
                assertEquals(List.of(fallback, english, german), changes);
                M3Localizer.install(root, english);
                M3Localizer.uninstall(branch);
                assertSame(english, M3Localizer.getEffectiveLocalizer(leaf));
                M3Localizer.uninstall(root);
                assertEquals(List.of(fallback, english, german, english, fallback), changes);
            }
            assertTrue(root.getProperties().isEmpty());
            assertTrue(branch.getProperties().isEmpty());
            assertTrue(leaf.getProperties().isEmpty());
        });
    }

    /// Reconnects shared ancestor paths when an entire unobserved intermediate branch moves.
    @Test
    void reparentingReleasesOldAncestors() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane branch = new Pane(leaf);
            Pane first = new Pane(branch);
            Pane second = new Pane();
            M3Localizer english = new M3Localizer(Locale.US);
            M3Localizer german = new M3Localizer(Locale.GERMANY);
            M3Localizer.install(first, english);
            M3Localizer.install(second, german);
            AtomicReference<M3Localizer> current = new AtomicReference<>();
            try (M3LocalizerObserver observer = new M3LocalizerObserver(leaf, current::set)) {
                first.getChildren().clear();
                second.getChildren().add(branch);
                assertSame(german, current.get());
                assertEquals(1, first.getProperties().size());
                M3Localizer.install(first, new M3Localizer(Locale.FRANCE));
                assertSame(german, current.get());
            }
            assertEquals(1, second.getProperties().size());
            assertTrue(branch.getProperties().isEmpty());
            assertTrue(leaf.getProperties().isEmpty());
        });
    }

    /// Shares node metadata across consumers and removes it only after the last subscription closes.
    @Test
    void subscriptionsShareAndReleaseTheirContext() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane root = new Pane(leaf);
            M3LocalizerObserver first = new M3LocalizerObserver(leaf, ignored -> {});
            int entries = leaf.getProperties().size();
            M3LocalizerObserver second = new M3LocalizerObserver(leaf, ignored -> {});
            assertEquals(entries, leaf.getProperties().size());
            first.close();
            first.close();
            assertFalse(root.getProperties().isEmpty());
            second.close();
            assertTrue(root.getProperties().isEmpty());
            assertTrue(leaf.getProperties().isEmpty());
        });
    }

    /// Does not retain a parent dependency if application metadata listeners dispose the connecting consumer.
    @Test
    void metadataCallbackCanCloseAnObserverDuringReparenting() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane first = new Pane(leaf);
            Pane second = new Pane();
            M3LocalizerObserver observer = new M3LocalizerObserver(leaf, ignored -> {});
            second.getProperties().addListener((MapChangeListener<Object, Object>) change -> {
                if (change.wasAdded()) {
                    observer.close();
                }
            });
            first.getChildren().clear();
            second.getChildren().add(leaf);
            observer.close();
            assertTrue(first.getProperties().isEmpty());
            assertTrue(second.getProperties().isEmpty());
            assertTrue(leaf.getProperties().isEmpty());
        });
    }

    /// Preserves the latest value when a subscriber replaces the localizer during notification.
    @Test
    void reentrantReplacementDeliversCurrentValue() {
        FxTestUtils.runOnFxThread(() -> {
            Pane root = new Pane();
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.GERMANY);
            AtomicReference<M3Localizer> current = new AtomicReference<>();
            try (M3LocalizerObserver redirect = new M3LocalizerObserver(root, value -> {
                if (value == first) {
                    M3Localizer.install(root, second);
                }
            }); M3LocalizerObserver observer = new M3LocalizerObserver(root, current::set)) {
                M3Localizer.install(root, first);
                assertSame(second, current.get());
                assertSame(second, M3Localizer.getEffectiveLocalizer(root));
            }
        });
    }

    /// Closing and adding subscribers during replacement must not deliver an obsolete outer-pass value.
    @Test
    void subscriptionChangesDuringReplacementUseLatestValue() {
        FxTestUtils.runOnFxThread(() -> {
            Pane root = new Pane();
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.GERMANY);
            List<M3LocalizerObserver> subscriptions = new ArrayList<>();
            List<M3Localizer> delivered = new ArrayList<>();
            try {
                subscriptions.add(new M3LocalizerObserver(root, value -> {
                    if (value == first) {
                        subscriptions.get(1).close();
                        subscriptions.add(new M3LocalizerObserver(root, delivered::add));
                        M3Localizer.install(root, second);
                    }
                }));
                subscriptions.add(new M3LocalizerObserver(root, value -> {
                    assertNotSame(first, value);
                    assertNotSame(second, value);
                }));
                M3Localizer.install(root, first);
                assertEquals(List.of(first, second), delivered);
            } finally {
                subscriptions.forEach(M3LocalizerObserver::close);
            }
            M3Localizer.uninstall(root);
            assertTrue(root.getProperties().isEmpty());
        });
    }

    /// Localization remains synchronous even when the subtree has no visible presentation.
    @Test
    void invisibleAndDetachedSubtreesReceiveReplacements() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane branch = new Pane(leaf);
            Pane root = new Pane(branch);
            new Scene(root);
            root.setVisible(false);
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.GERMANY);
            AtomicReference<M3Localizer> current = new AtomicReference<>();
            try (M3LocalizerObserver observer = new M3LocalizerObserver(leaf, current::set)) {
                M3Localizer.install(root, first);
                assertSame(first, current.get());
                root.getChildren().clear();
                M3Localizer.install(branch, second);
                assertSame(second, current.get());
                M3Localizer.uninstall(branch);
                root.getChildren().add(branch);
                assertSame(first, current.get());
            }
            assertTrue(branch.getProperties().isEmpty());
            assertTrue(leaf.getProperties().isEmpty());
        });
    }

    /// Uses identity rather than application-defined equality to recognize a replacement instance.
    @Test
    void equalInstancesStillReplaceTranslations() {
        FxTestUtils.runOnFxThread(() -> {
            Pane root = new Pane();
            Pane popup = new Pane();
            EqualLocalizer first = new EqualLocalizer();
            EqualLocalizer second = new EqualLocalizer();
            AtomicReference<M3Localizer> current = new AtomicReference<>();
            AtomicReference<M3Localizer> inherited = new AtomicReference<>();
            try (M3LocalizerObserver observer = new M3LocalizerObserver(root, current::set);
                 M3LocalizerObserver popupObserver = new M3LocalizerObserver(popup, inherited::set)) {
                M3Localizer.install(root, first);
                M3Localizer.install(root, second);
                assertSame(second, current.get());
                M3LocalizerObserver.inherit(popup, first);
                M3LocalizerObserver.inherit(popup, second);
                assertSame(second, inherited.get());
            }
        });
    }

    /// A failed translation refresh does not sever a newly restored ancestor subscription.
    @Test
    void failedRefreshStillObservesSubsequentAncestorChanges() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane root = new Pane(leaf);
            M3Localizer direct = new M3Localizer(Locale.US);
            M3Localizer rejected = new M3Localizer(Locale.FRANCE);
            M3Localizer replacement = new M3Localizer(Locale.GERMANY);
            M3Localizer.install(leaf, direct);
            M3Localizer.install(root, rejected);
            AtomicReference<M3Localizer> current = new AtomicReference<>();
            try (M3LocalizerObserver observer = new M3LocalizerObserver(leaf, value -> {
                if (value == rejected) {
                    throw new IllegalArgumentException("translation failure");
                }
                current.set(value);
            })) {
                assertThrows(IllegalArgumentException.class, () -> M3Localizer.uninstall(leaf));
                M3Localizer.install(root, replacement);
                assertSame(replacement, current.get());
            }
            assertTrue(leaf.getProperties().isEmpty());
            assertEquals(1, root.getProperties().size());
        });
    }

    /// Runtime failures are reported only after siblings and later branches receive the replacement.
    @Test
    void failingCallbacksDoNotBlockOtherBranches() {
        FxTestUtils.runOnFxThread(() -> {
            Pane first = new Pane();
            Pane second = new Pane();
            Pane third = new Pane();
            Pane root = new Pane(first, second, third);
            M3Localizer rejected = new M3Localizer(Locale.GERMANY);
            M3Localizer recovered = new M3Localizer(Locale.US);
            RuntimeException primaryFailure = new IllegalArgumentException("first translation failed");
            RuntimeException secondaryFailure = new IllegalStateException("second translation failed");
            AtomicReference<M3Localizer> firstValue = new AtomicReference<>();
            AtomicReference<M3Localizer> lastValue = new AtomicReference<>();
            try (M3LocalizerObserver failingFirst = new M3LocalizerObserver(first, value -> {
                if (value == rejected) {
                    throw primaryFailure;
                }
            }); M3LocalizerObserver healthyFirst = new M3LocalizerObserver(first, firstValue::set);
                 M3LocalizerObserver failingSecond = new M3LocalizerObserver(second, value -> {
                     if (value == rejected) {
                         throw secondaryFailure;
                     }
                 }); M3LocalizerObserver failingThird = new M3LocalizerObserver(third, value -> {
                     if (value == rejected) {
                         throw primaryFailure;
                     }
                 }); M3LocalizerObserver healthyLast = new M3LocalizerObserver(third, lastValue::set)) {
                assertSame(primaryFailure, assertThrows(RuntimeException.class,
                        () -> M3Localizer.install(root, rejected)));
                assertArrayEquals(new Throwable[]{secondaryFailure}, primaryFailure.getSuppressed());
                assertSame(rejected, firstValue.get());
                assertSame(rejected, lastValue.get());
                assertSame(rejected, M3Localizer.getEffectiveLocalizer(third));
                M3Localizer.install(root, recovered);
                assertSame(recovered, firstValue.get());
                assertSame(recovered, lastValue.get());
            }
            M3Localizer.uninstall(root);
            assertTrue(root.getProperties().isEmpty());
            assertTrue(first.getProperties().isEmpty());
            assertTrue(second.getProperties().isEmpty());
            assertTrue(third.getProperties().isEmpty());
        });
    }

    /// A failing callback cannot revert consumers to a value superseded by a nested replacement.
    @Test
    void failureAfterNestedReplacementPreservesLatestValue() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane root = new Pane(leaf);
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.GERMANY);
            List<M3Localizer> delivered = new ArrayList<>();
            try (M3LocalizerObserver redirect = new M3LocalizerObserver(root, value -> {
                if (value == first) {
                    M3Localizer.install(root, second);
                    throw new IllegalStateException("replacement callback failed");
                }
            }); M3LocalizerObserver observer = new M3LocalizerObserver(leaf, delivered::add)) {
                delivered.clear();
                assertThrows(IllegalStateException.class, () -> M3Localizer.install(root, first));
                assertEquals(List.of(second), delivered);
                assertSame(second, M3Localizer.getEffectiveLocalizer(leaf));
            }
            assertTrue(leaf.getProperties().isEmpty());
        });
    }

    /// Models an application localizer with value equality rather than identity equality.
    @NotNullByDefault
    private static final class EqualLocalizer extends M3Localizer {
        /// Creates another equivalent application configuration.
        private EqualLocalizer() {
            super(Locale.US);
        }

        /// Treats all instances as equivalent for application configuration comparisons.
        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable Object other) {
            return other instanceof EqualLocalizer;
        }

        /// Matches the value-equality contract.
        @Override
        public int hashCode() {
            return 1;
        }
    }

    /// Rolls back subscription state when the initial application callback fails.
    @Test
    void failedInitialCallbackDoesNotRetainAncestorContexts() {
        FxTestUtils.runOnFxThread(() -> {
            Pane leaf = new Pane();
            Pane root = new Pane(leaf);
            assertThrows(IllegalArgumentException.class, () -> new M3LocalizerObserver(leaf, ignored -> {
                throw new IllegalArgumentException("translation failure");
            }));
            assertTrue(leaf.getProperties().isEmpty());
            assertTrue(root.getProperties().isEmpty());
        });
    }

    /// Keeps detached popup content current without overwriting its own explicit localization.
    @Test
    void popupPropagationRespectsLocalOverridesAndStopsWithPresentation() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            new Scene(owner);
            Pane nested = new Pane();
            Pane popup = new Pane(nested);
            M3Localizer english = new M3Localizer(Locale.US);
            M3Localizer german = new M3Localizer(Locale.GERMANY);
            M3Localizer french = new M3Localizer(Locale.FRANCE);
            M3Localizer.install(owner, english);
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popup);
            try {
                synchronizer.start();
                assertSame(english, M3Localizer.getEffectiveLocalizer(nested));
                M3Localizer.install(owner, german);
                assertSame(german, M3Localizer.getEffectiveLocalizer(nested));
                M3Localizer.install(popup, french);
                M3Localizer.install(owner, english);
                assertSame(french, M3Localizer.getEffectiveLocalizer(nested));
                M3Localizer.uninstall(popup);
                assertSame(english, M3Localizer.getEffectiveLocalizer(nested));
                synchronizer.stop();
                M3Localizer.install(owner, german);
                assertSame(english, M3Localizer.getEffectiveLocalizer(nested));
                synchronizer.start();
                assertSame(german, M3Localizer.getEffectiveLocalizer(nested));
            } finally {
                synchronizer.stop();
            }
        });
    }
}
