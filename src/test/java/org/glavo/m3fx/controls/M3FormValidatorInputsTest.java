// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies atomic registration-list updates and validation observation across reordered or replaced inputs.
@NotNullByDefault
final class M3FormValidatorInputsTest {
    /// Starts JavaFX before constructing form inputs.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Sorts registrations without introducing temporary duplicates or losing validation observation.
    @Test
    void sortingReordersInvalidInputsInOneChange() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = invalidInput("First");
            M3TextInputLayout second = invalidInput("Second");
            M3FormValidator validator = new M3FormValidator(second, first);
            AtomicInteger changes = new AtomicInteger();
            validator.getInvalidInputs().addListener((ListChangeListener<M3FormInput>) change -> changes.incrementAndGet());

            validator.getInputs().sort(Comparator.comparing(M3FormInput::getLabelText));

            assertEquals(List.of(first, second), validator.getInputs());
            assertEquals(List.of(first, second), validator.getInvalidInputs());
            assertSame(first, validator.getFirstInvalidInput());
            assertEquals(1, changes.get());
            first.clearValidation();
            assertEquals(List.of(second), validator.getInvalidInputs());
        });
    }

    /// Accepts a replacement that swaps two existing registrations as one complete candidate list.
    @Test
    void replaceAllCanReorderExistingInputs() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = invalidInput("First");
            M3TextInputLayout second = invalidInput("Second");
            M3FormValidator validator = new M3FormValidator(first, second);
            validator.getInputs().replaceAll(input -> input == first ? second : first);
            assertEquals(List.of(second, first), validator.getInputs());
            assertEquals(List.of(second, first), validator.getInvalidInputs());
            second.clearValidation();
            assertEquals(List.of(first), validator.getInvalidInputs());
        });
    }

    /// Rejects a duplicate replacement before changing registrations or their observers.
    @Test
    void invalidReplacementLeavesRegistrationUnchanged() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = invalidInput("First");
            M3TextInputLayout second = invalidInput("Second");
            M3TextInputLayout replacement = invalidInput("Replacement");
            M3FormValidator validator = new M3FormValidator(first, second);
            AtomicInteger changes = new AtomicInteger();
            validator.getInputs().addListener((ListChangeListener<M3FormInput>) change -> changes.incrementAndGet());

            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().replaceAll(input -> replacement));

            assertEquals(List.of(first, second), validator.getInputs());
            assertEquals(List.of(first, second), validator.getInvalidInputs());
            assertEquals(0, changes.get());
            first.clearValidation();
            assertEquals(List.of(second), validator.getInvalidInputs());
        });
    }

    /// Preserves the original registrations if an operator fails after producing an earlier replacement.
    @Test
    void failingReplacementLeavesRegistrationUnchanged() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = invalidInput("First");
            M3TextInputLayout second = invalidInput("Second");
            M3TextInputLayout replacement = invalidInput("Replacement");
            M3FormValidator validator = new M3FormValidator(first, second);

            assertThrows(IllegalStateException.class, () -> validator.getInputs().replaceAll(input -> {
                if (input == second) {
                    throw new IllegalStateException("Replacement failed");
                }
                return replacement;
            }));

            assertEquals(List.of(first, second), validator.getInputs());
            assertEquals(List.of(first, second), validator.getInvalidInputs());
        });
    }

    /// Publishes one aggregate replacement and releases observation of the removed registrations.
    @Test
    void replacementUpdatesObserverOwnershipInOneChange() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = invalidInput("First");
            M3TextInputLayout second = invalidInput("Second");
            M3TextInputLayout replacementFirst = invalidInput("Replacement first");
            M3TextInputLayout replacementSecond = invalidInput("Replacement second");
            M3FormValidator validator = new M3FormValidator(first, second);
            AtomicInteger changes = new AtomicInteger();
            validator.getInvalidInputs().addListener((ListChangeListener<M3FormInput>) change -> changes.incrementAndGet());

            validator.getInputs().replaceAll(input -> input == first ? replacementFirst : replacementSecond);

            assertEquals(1, changes.get());
            assertEquals(List.of(replacementFirst, replacementSecond), validator.getInvalidInputs());
            first.clearValidation();
            second.clearValidation();
            first.validate();
            second.validate();
            assertEquals(1, changes.get());
            replacementFirst.clearValidation();
            assertEquals(List.of(replacementSecond), validator.getInvalidInputs());
        });
    }

    /// Refreshes aggregate state when registrations are removed through a list view.
    @Test
    void subListRemovalUpdatesAggregateState() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout first = invalidInput("First");
            M3TextInputLayout second = invalidInput("Second");
            M3FormValidator validator = new M3FormValidator(first, second);
            validator.getInputs().subList(0, 1).clear();
            assertEquals(List.of(second), validator.getInvalidInputs());
            assertSame(second, validator.getFirstInvalidInput());
        });
    }

    /// Creates an input with active validator-produced error state.
    private static M3TextInputLayout invalidInput(String label) {
        M3TextInputLayout input = new M3TextInputLayout(new M3TextField());
        input.setLabelText(label);
        input.setValidator((control, text) -> "Required");
        assertFalse(input.validate());
        return input;
    }
}
