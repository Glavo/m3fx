// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.localization;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3Breadcrumbs;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3DatePickerDialog;
import org.glavo.m3fx.controls.M3DatePickerField;
import org.glavo.m3fx.controls.M3DateRangePicker;
import org.glavo.m3fx.controls.M3DateRangePickerDialog;
import org.glavo.m3fx.controls.M3DateRangePickerField;
import org.glavo.m3fx.controls.M3Dialog;
import org.glavo.m3fx.controls.M3NumberField;
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3ProgressIndicator;
import org.glavo.m3fx.controls.M3RangeSlider;
import org.glavo.m3fx.controls.M3Scrim;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.controls.M3TimePickerDialog;
import org.glavo.m3fx.controls.M3TimePickerField;
import org.glavo.m3fx.controls.M3ValidationSummary;
import org.glavo.m3fx.internal.M3LocalizerObserver;
import org.glavo.m3fx.internal.M3LocalizerProperty;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies per-component localization properties, explicit bindings, and direct observation lifetime.
@NotNullByDefault
final class M3LocalizationTest {
    /// Starts JavaFX before constructing controls.
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
        assertSame(M3Localizer.getDefault(), M3Localizer.getDefault());
        assertThrows(NullPointerException.class, () -> new M3Localizer(null));
        assertThrows(NullPointerException.class, () -> localizer.formatMonth(null, TextStyle.FULL));
    }

    /// Allows subclasses to override only messages they translate.
    @Test
    void partialOverridesRetainEnglishFallback() {
        M3Localizer localizer = new PartialLocalizer();
        assertEquals("Text entfernen", localizer.getClearActionText());
        assertEquals("Dismiss", localizer.getDismissText());
    }

    /// Every component exposes one stable non-null property with setter and getter agreement.
    @Test
    @SuppressWarnings("DataFlowIssue")
    void componentPropertiesDefaultAndRejectNull() {
        FxTestUtils.runOnFxThread(() -> {
            for (M3Localized component : createComponents()) {
                ObjectProperty<M3Localizer> property = component.localizerProperty();
                assertSame(property, component.localizerProperty());
                assertSame(M3Localizer.getDefault(), component.getLocalizer());
                assertSame(component.getLocalizer(), property.get());
                assertFalse(property.isBound());
                assertThrows(NullPointerException.class, () -> component.setLocalizer(null));
                assertThrows(NullPointerException.class, () -> property.set(null));
                assertThrows(NullPointerException.class, () -> property.bind(null));
                ObjectProperty<@Nullable M3Localizer> absent = new SimpleObjectProperty<>();
                assertThrows(NullPointerException.class, () -> property.bind(absent));
                assertFalse(property.isBound());
                assertSame(M3Localizer.getDefault(), property.get());

                M3Localizer first = new M3Localizer(Locale.US);
                M3Localizer second = new M3Localizer(Locale.FRANCE);
                component.setLocalizer(first);
                assertSame(first, property.get());
                property.set(second);
                assertSame(second, component.getLocalizer());
                component.setLocalizer(M3Localizer.getDefault());
                assertSame(M3Localizer.getDefault(), property.get());
            }
        });
    }

    /// A binding source that violates the non-null contract cannot expose null through the property getter.
    @Test
    void localizerPropertyRejectsNullBoundReads() {
        M3LocalizerProperty property = new M3LocalizerProperty(this);
        ObjectProperty<@Nullable M3Localizer> source = new SimpleObjectProperty<>(M3Localizer.getDefault());
        property.bind(source);
        source.set(null);
        assertThrows(NullPointerException.class, property::get);
        M3Localizer replacement = new M3Localizer(Locale.FRANCE);
        source.set(replacement);
        assertSame(replacement, property.get());
        property.unbind();
        assertSame(replacement, property.get());
    }

    /// Explicit application bindings share replacements while unbound components retain their own value.
    @Test
    void sharedLanguageBindingSupportsDirectOverridesAndRebinding() {
        FxTestUtils.runOnFxThread(() -> {
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.GERMANY);
            ObjectProperty<M3Localizer> language = new SimpleObjectProperty<>(first);
            @Unmodifiable List<M3Localized> components = createComponents();
            for (M3Localized component : components) {
                component.localizerProperty().bind(language);
                assertTrue(component.localizerProperty().isBound());
                assertSame(first, component.getLocalizer());
                assertThrows(RuntimeException.class, () -> component.setLocalizer(second));
            }
            language.set(second);
            for (M3Localized component : components) {
                assertSame(second, component.getLocalizer());
                component.localizerProperty().unbind();
                assertFalse(component.localizerProperty().isBound());
                assertSame(second, component.getLocalizer());
            }
            language.set(first);
            for (M3Localized component : components) {
                assertSame(second, component.getLocalizer());
                component.setLocalizer(M3Localizer.getDefault());
                assertSame(M3Localizer.getDefault(), component.getLocalizer());
                component.localizerProperty().bind(language);
                assertSame(first, component.getLocalizer());
            }
        });
    }

    /// Observation works on a non-Node dialog and stops independently for each subscription.
    @Test
    void directObserversDeliverImmediatelyAndCloseIndependently() {
        FxTestUtils.runOnFxThread(() -> {
            M3Dialog dialog = new M3Dialog();
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.US);
            List<M3Localizer> firstChanges = new ArrayList<>();
            List<M3Localizer> secondChanges = new ArrayList<>();
            try (M3LocalizerObserver firstObserver = new M3LocalizerObserver(dialog, firstChanges::add);
                 M3LocalizerObserver secondObserver = new M3LocalizerObserver(dialog, secondChanges::add)) {
                assertEquals(List.of(M3Localizer.getDefault()), firstChanges);
                assertEquals(firstChanges, secondChanges);
                dialog.setLocalizer(first);
                dialog.setLocalizer(first);
                assertEquals(List.of(M3Localizer.getDefault(), first), firstChanges);
                firstObserver.close();
                firstObserver.close();
                dialog.localizerProperty().set(second);
                assertEquals(List.of(M3Localizer.getDefault(), first), firstChanges);
                assertEquals(List.of(M3Localizer.getDefault(), first, second), secondChanges);
            }
            dialog.setLocalizer(M3Localizer.getDefault());
            assertEquals(2, firstChanges.size());
            assertEquals(3, secondChanges.size());
        });
    }

    /// Distinct instances refresh observers and defaults even when application equality considers them equal.
    @Test
    void equalLocalizersStillRefreshDirectAndBoundProperties() {
        FxTestUtils.runOnFxThread(() -> {
            M3DatePickerDialog dialog = new M3DatePickerDialog();
            EqualLocalizer first = new EqualLocalizer("First date");
            EqualLocalizer second = new EqualLocalizer("Second date");
            assertEquals(first, second);
            assertNotSame(first, second);
            List<M3Localizer> changes = new ArrayList<>();
            try (M3LocalizerObserver observer = new M3LocalizerObserver(dialog, changes::add)) {
                dialog.setLocalizer(first);
                dialog.setLocalizer(second);
                assertEquals(3, changes.size());
                assertSame(first, changes.get(1));
                assertSame(second, changes.get(2));
                assertEquals("Second date", dialog.getDialogPane().getHeaderText());

                ObjectProperty<M3Localizer> language = new SimpleObjectProperty<>(first);
                dialog.localizerProperty().bind(language);
                assertEquals("First date", dialog.getDialogPane().getHeaderText());
                language.set(second);
                assertEquals(5, changes.size());
                assertSame(second, changes.get(4));
                assertSame(second, dialog.getPicker().getLocalizer());
                assertEquals("Second date", dialog.getDialogPane().getHeaderText());
            }
        });
    }

    /// Failed initial callbacks rethrow the failure and remove their direct property subscription.
    @Test
    void failedInitialCallbackDoesNotRetainSubscription() {
        FxTestUtils.runOnFxThread(() -> {
            M3Dialog dialog = new M3Dialog();
            AtomicInteger calls = new AtomicInteger();
            IllegalArgumentException failure = new IllegalArgumentException("translation failure");
            assertSame(failure, assertThrows(IllegalArgumentException.class,
                    () -> new M3LocalizerObserver(dialog, ignored -> {
                        calls.incrementAndGet();
                        throw failure;
                    })));
            dialog.setLocalizer(new M3Localizer(Locale.FRANCE));
            assertEquals(1, calls.get());
        });
    }

    /// Parent localizers and visibility do not affect independent values or explicit application bindings.
    @Test
    void reparentingAndDetachedControlsKeepTheirLanguage() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField field = new M3NumberField();
            Pane branch = new Pane(field);
            M3OverlayPane firstRoot = new M3OverlayPane();
            M3OverlayPane secondRoot = new M3OverlayPane();
            firstRoot.setContent(branch);
            new Scene(firstRoot);
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.FRANCE);
            firstRoot.setLocalizer(first);
            secondRoot.setLocalizer(second);
            assertSame(M3Localizer.getDefault(), field.getLocalizer());
            ObjectProperty<M3Localizer> language = new SimpleObjectProperty<>(first);
            field.localizerProperty().bind(language);
            List<M3Localizer> changes = new ArrayList<>();
            try (M3LocalizerObserver observer = new M3LocalizerObserver(field, changes::add)) {
                firstRoot.setVisible(false);
                firstRoot.setContent(null);
                assertSame(first, field.getLocalizer());
                language.set(second);
                assertSame(second, field.getLocalizer());
                secondRoot.setContent(branch);
                secondRoot.setLocalizer(first);
                assertSame(second, field.getLocalizer());
                assertEquals(List.of(first, second), changes);
            }
        });
    }

    /// Popup presentation synchronization does not assign language to application-owned content.
    @Test
    void popupContextDoesNotInheritOwnerLanguage() {
        FxTestUtils.runOnFxThread(() -> {
            M3NumberField owner = new M3NumberField();
            new Scene(new Pane(owner));
            M3NumberField popupField = new M3NumberField();
            Pane popup = new Pane(popupField);
            M3Localizer first = new M3Localizer(Locale.US);
            M3Localizer second = new M3Localizer(Locale.GERMANY);
            owner.setLocalizer(first);
            M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(owner, popup);
            try {
                synchronizer.start();
                assertSame(M3Localizer.getDefault(), popupField.getLocalizer());
                owner.setLocalizer(second);
                assertSame(M3Localizer.getDefault(), popupField.getLocalizer());
                popupField.localizerProperty().bind(owner.localizerProperty());
                assertSame(second, popupField.getLocalizer());
                synchronizer.stop();
                owner.setLocalizer(first);
                assertSame(first, popupField.getLocalizer());
                popupField.localizerProperty().unbind();
                popupField.setLocalizer(second);
                synchronizer.start();
                assertSame(second, popupField.getLocalizer());
            } finally {
                synchronizer.stop();
            }
        });
    }

    /// Creates independent controls and dialogs covered by the public localization contract.
    private static @Unmodifiable List<M3Localized> createComponents() {
        return List.of(new M3TextInputLayout(), new M3NumberField(), new M3ValidationSummary(), new M3OverlayPane(),
                new M3BottomSheet(), new M3Breadcrumbs(), new M3RangeSlider(), new M3Scrim(), new M3SearchView(),
                new M3ProgressBar(), new M3ProgressIndicator(),
                new M3DatePicker(), new M3DateRangePicker(), new M3TimePicker(), new M3DatePickerField(),
                new M3DateRangePickerField(), new M3TimePickerField(), new M3Dialog(),
                new M3DatePickerDialog(), new M3DateRangePickerDialog(), new M3TimePickerDialog());
    }

    /// Models equal application configurations with different translated resources.
    @NotNullByDefault
    private static final class EqualLocalizer extends M3Localizer {
        /// The title supplied by this particular resource instance.
        private final String title;

        /// Creates an equal configuration with a distinct title.
        private EqualLocalizer(String title) {
            super(Locale.US);
            this.title = title;
        }

        /// Returns the instance-specific date title.
        @Override
        public String getSelectDateText() {
            return title;
        }

        /// Treats every instance of this test configuration as equivalent.
        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof EqualLocalizer;
        }

        /// Matches the configuration equality contract.
        @Override
        public int hashCode() {
            return 1;
        }
    }

    /// Translates one message while retaining inherited English fallbacks.
    @NotNullByDefault
    private static final class PartialLocalizer extends M3Localizer {
        /// Uses German regional formatting.
        private PartialLocalizer() {
            super(Locale.GERMAN);
        }

        /// Supplies an application-owned translation.
        @Override
        public String getClearActionText() {
            return "Text entfernen";
        }
    }
}
