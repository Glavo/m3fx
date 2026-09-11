// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies aggregate form state when its observers change registration or validation during publication.
@NotNullByDefault
final class M3FormValidatorReentrancyTest {
    /// Starts JavaFX before constructing form controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Uses entry-time order, skips removed registrations, and defers new registrations to the next pass.
    @ParameterizedTest
    @CsvSource({"remove,false", "add,false", "reorder,false", "remove,true", "add,true", "reorder,true"})
    void batchCallbacksCanChangeRegistration(String mutation, boolean clear) {
        FxTestUtils.runOnFxThread(() -> {
            List<String> visits = new ArrayList<>();
            M3TextInputLayout first = input("first", visits);
            M3TextInputLayout second = input("second", visits);
            M3TextInputLayout third = input("third", visits);
            M3TextInputLayout added = input("added", visits);
            if (clear) {
                for (M3TextInputLayout input : List.of(first, second, third, added)) {
                    input.validate();
                    input.validationActiveProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue) {
                            visits.add(input.getLabelText());
                        }
                    });
                }
                visits.clear();
            }
            M3FormValidator validator = new M3FormValidator(first, second, third);
            AtomicBoolean changed = new AtomicBoolean();
            Runnable changeRegistration = () -> {
                if (changed.compareAndSet(false, true)) {
                    switch (mutation) {
                        case "remove" -> validator.getInputs().remove(second);
                        case "add" -> validator.getInputs().add(added);
                        case "reorder" -> validator.getInputs().setAll(third, second, first);
                        default -> throw new AssertionError(mutation);
                    }
                }
            };
            if (clear) {
                first.validationActiveProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        changeRegistration.run();
                    }
                });
                validator.clearValidation();
            } else {
                first.setValidator((control, text) -> {
                    visits.add("first");
                    changeRegistration.run();
                    return null;
                });
                assertTrue(validator.validate());
            }
            assertEquals(mutation.equals("remove") ? List.of("first", "third") : List.of("first", "second", "third"), visits);
            assertEquals(clear, added.isValidationActive());
            if (mutation.equals("remove")) {
                assertEquals(clear, second.isValidationActive());
            }
            if (mutation.equals("add")) {
                visits.clear();
                if (clear) {
                    validator.clearValidation();
                    assertEquals(List.of("added"), visits);
                } else {
                    assertTrue(validator.validate());
                    assertEquals(List.of("first", "second", "third", "added"), visits);
                }
            }
        });
    }

    /// Creates an input whose successful validation records its place in the batch.
    private static M3TextInputLayout input(String label, List<String> visits) {
        M3TextInputLayout input = new M3TextInputLayout(new M3TextField());
        input.setLabelText(label);
        input.setValidator((control, text) -> {
            visits.add(label);
            return null;
        });
        return input;
    }

    /// Reconciles all aggregates after a publication callback removes or clears the newly invalid input.
    @ParameterizedTest
    @CsvSource({"list,true", "first,true", "count,true", "valid,true", "active,true",
            "list,false", "first,false", "count,false", "valid,false", "active,false"})
    void aggregateListenersCanRemoveOrClearInputs(String observed, boolean remove) {
        FxTestUtils.runOnFxThread(() -> {
            M3TextInputLayout input = new M3TextInputLayout(new M3TextField());
            input.setValidator((control, text) -> "Required");
            assertFalse(input.validate());
            M3FormValidator validator = new M3FormValidator();
            AtomicBoolean changed = new AtomicBoolean();
            Runnable changeState = () -> {
                if (changed.compareAndSet(false, true)) {
                    if (remove) {
                        validator.getInputs().clear();
                    } else {
                        input.clearValidation();
                    }
                }
            };
            switch (observed) {
                case "list" -> validator.getInvalidInputs().addListener(
                        (ListChangeListener<M3FormInput>) change -> changeState.run());
                case "first" -> validator.firstInvalidInputProperty().addListener(
                        (observable, oldValue, newValue) -> changeState.run());
                case "count" -> validator.invalidInputCountProperty().addListener(
                        (observable, oldValue, newValue) -> changeState.run());
                case "valid" -> validator.validProperty().addListener(
                        (observable, oldValue, newValue) -> changeState.run());
                case "active" -> validator.validationActiveProperty().addListener(
                        (observable, oldValue, newValue) -> changeState.run());
                default -> throw new AssertionError(observed);
            }

            validator.getInputs().add(input);

            assertTrue(changed.get());
            assertEquals(remove ? 0 : 1, validator.getInputs().size());
            assertTrue(validator.getInvalidInputs().isEmpty());
            assertNull(validator.getFirstInvalidInput());
            assertEquals(0, validator.getInvalidInputCount());
            assertTrue(validator.isValid());
            assertFalse(validator.isValidationActive());
        });
    }
}
