// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextInputControl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/// Coordinates validation across multiple [M3TextInputLayout] controls.
@NotNullByDefault
public final class M3FormValidator {
    /// The registered text input layouts.
    private final ObservableList<M3TextInputLayout> inputs = FXCollections.observableArrayList();

    /// The unmodifiable registered input list exposed to callers.
    private final @UnmodifiableView ObservableList<M3TextInputLayout> inputsView =
            FXCollections.unmodifiableObservableList(inputs);

    /// The currently invalid input layouts.
    private final ObservableList<M3TextInputLayout> invalidInputs = FXCollections.observableArrayList();

    /// The unmodifiable invalid input list exposed to callers.
    private final @UnmodifiableView ObservableList<M3TextInputLayout> invalidInputsView =
            FXCollections.unmodifiableObservableList(invalidInputs);

    /// The first invalid input layout in registration order.
    private final ReadOnlyObjectWrapper<@Nullable M3TextInputLayout> firstInvalidInput =
            new ReadOnlyObjectWrapper<>(this, "firstInvalidInput");

    /// Whether all registered input layouts are valid.
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid", true);

    /// Updates group state when one registered layout changes its validator-produced error text.
    private final ChangeListener<String> validationErrorTextListener =
            (observable, oldValue, newValue) -> refreshInvalidInputs();

    /// Creates an empty form validator.
    public M3FormValidator() {
    }

    /// Creates a form validator with the supplied input layouts.
    public M3FormValidator(M3TextInputLayout... inputs) {
        addInputs(inputs);
    }

    /// Returns the registered input layouts in validation order.
    public @UnmodifiableView ObservableList<M3TextInputLayout> getInputs() {
        return inputsView;
    }

    /// Adds one input layout to the end of the validation order.
    public void addInput(M3TextInputLayout input) {
        M3TextInputLayout validatedInput = Objects.requireNonNull(input, "input");
        validateNewInput(validatedInput);
        installInput(validatedInput);
        inputs.add(validatedInput);
        refreshInvalidInputs();
    }

    /// Adds input layouts to the end of the validation order.
    public void addInputs(M3TextInputLayout... inputs) {
        List<M3TextInputLayout> validatedInputs = validatedNewInputs(inputs);
        installInputs(validatedInputs);
        this.inputs.addAll(validatedInputs);
        refreshInvalidInputs();
    }

    /// Replaces the registered input layouts with the supplied layouts.
    public void setInputs(M3TextInputLayout... inputs) {
        setInputs(validatedInputs(inputs));
    }

    /// Replaces the registered input layouts with the supplied layouts.
    public void setInputs(Collection<? extends M3TextInputLayout> inputs) {
        List<M3TextInputLayout> validatedInputs = validatedInputs(inputs);
        uninstallInputs(this.inputs);
        this.inputs.setAll(validatedInputs);
        installInputs(validatedInputs);
        refreshInvalidInputs();
    }

    /// Removes one input layout from the validation order.
    public boolean removeInput(M3TextInputLayout input) {
        M3TextInputLayout validatedInput = Objects.requireNonNull(input, "input");
        boolean removed = inputs.remove(validatedInput);
        if (removed) {
            uninstallInput(validatedInput);
            refreshInvalidInputs();
        }
        return removed;
    }

    /// Removes all registered input layouts.
    public void clearInputs() {
        uninstallInputs(inputs);
        inputs.clear();
        refreshInvalidInputs();
    }

    /// Returns the currently invalid input layouts in validation order.
    public @UnmodifiableView ObservableList<M3TextInputLayout> getInvalidInputs() {
        return invalidInputsView;
    }

    /// Returns the first invalid input layout in validation order.
    public @Nullable M3TextInputLayout getFirstInvalidInput() {
        return firstInvalidInput.get();
    }

    /// Returns the first invalid input layout property.
    public ReadOnlyObjectProperty<@Nullable M3TextInputLayout> firstInvalidInputProperty() {
        return firstInvalidInput.getReadOnlyProperty();
    }

    /// Returns whether all registered input layouts are currently valid.
    public boolean isValid() {
        return valid.get();
    }

    /// Returns the valid state property.
    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    /// Runs validation on all registered input layouts and returns whether all are valid.
    public boolean validate() {
        boolean valid = true;
        for (M3TextInputLayout input : inputs) {
            valid &= input.validate();
        }
        refreshInvalidInputs();
        return valid;
    }

    /// Clears validator-produced error state on all registered input layouts.
    public void clearValidation() {
        for (M3TextInputLayout input : inputs) {
            input.clearValidation();
        }
        refreshInvalidInputs();
    }

    /// Requests focus on the first invalid input layout and returns whether one existed.
    public boolean focusFirstInvalidInput() {
        @Nullable M3TextInputLayout invalidInput = getFirstInvalidInput();
        if (invalidInput == null) {
            return false;
        }

        @Nullable TextInputControl textInput = invalidInput.getInput();
        if (textInput != null && !textInput.isDisabled()) {
            textInput.requestFocus();
        } else if (!invalidInput.isDisabled()) {
            invalidInput.requestFocus();
        }
        return true;
    }

    /// Runs validation, focuses the first invalid input layout, and returns whether all inputs are valid.
    public boolean validateAndFocusFirstInvalidInput() {
        boolean valid = validate();
        if (!valid) {
            focusFirstInvalidInput();
        }
        return valid;
    }

    /// Installs validation listeners on a group of input layouts.
    private void installInputs(Collection<M3TextInputLayout> inputs) {
        for (M3TextInputLayout input : inputs) {
            installInput(input);
        }
    }

    /// Installs validation listeners on one input layout.
    private void installInput(M3TextInputLayout input) {
        input.validationErrorTextProperty().addListener(validationErrorTextListener);
    }

    /// Removes validation listeners from a group of input layouts.
    private void uninstallInputs(Collection<M3TextInputLayout> inputs) {
        for (M3TextInputLayout input : inputs) {
            uninstallInput(input);
        }
    }

    /// Removes validation listeners from one input layout.
    private void uninstallInput(M3TextInputLayout input) {
        input.validationErrorTextProperty().removeListener(validationErrorTextListener);
    }

    /// Rebuilds the invalid input list and read-only state properties.
    private void refreshInvalidInputs() {
        ArrayList<M3TextInputLayout> invalidInputs = new ArrayList<>();
        for (M3TextInputLayout input : inputs) {
            if (input.isValidationError()) {
                invalidInputs.add(input);
            }
        }

        this.invalidInputs.setAll(invalidInputs);
        firstInvalidInput.set(invalidInputs.isEmpty() ? null : invalidInputs.get(0));
        valid.set(invalidInputs.isEmpty());
    }

    /// Returns a validated copy of input layout varargs.
    private List<M3TextInputLayout> validatedInputs(M3TextInputLayout... inputs) {
        Objects.requireNonNull(inputs, "inputs");
        ArrayList<M3TextInputLayout> validatedInputs = new ArrayList<>(inputs.length);
        for (M3TextInputLayout input : inputs) {
            validatedInputs.add(Objects.requireNonNull(input, "input"));
        }
        validateDistinctInputs(validatedInputs);
        return validatedInputs;
    }

    /// Returns a validated copy of new input layout varargs.
    private List<M3TextInputLayout> validatedNewInputs(M3TextInputLayout... inputs) {
        List<M3TextInputLayout> validatedInputs = validatedInputs(inputs);
        validateNewInputs(validatedInputs);
        return validatedInputs;
    }

    /// Returns a validated copy of input layout collection values.
    private List<M3TextInputLayout> validatedInputs(Collection<? extends M3TextInputLayout> inputs) {
        Objects.requireNonNull(inputs, "inputs");
        ArrayList<M3TextInputLayout> validatedInputs = new ArrayList<>(inputs.size());
        for (M3TextInputLayout input : inputs) {
            validatedInputs.add(Objects.requireNonNull(input, "input"));
        }
        validateDistinctInputs(validatedInputs);
        return validatedInputs;
    }

    /// Validates that one input layout is not already registered.
    private void validateNewInput(M3TextInputLayout input) {
        if (inputs.contains(input)) {
            throw new IllegalArgumentException("input is already registered");
        }
    }

    /// Validates that input layouts are not already registered.
    private void validateNewInputs(List<M3TextInputLayout> inputs) {
        for (M3TextInputLayout input : inputs) {
            validateNewInput(input);
        }
    }

    /// Validates that input layouts contain no duplicates.
    private static void validateDistinctInputs(List<M3TextInputLayout> inputs) {
        for (int i = 0; i < inputs.size(); i++) {
            M3TextInputLayout input = inputs.get(i);
            for (int j = i + 1; j < inputs.size(); j++) {
                if (input == inputs.get(j)) {
                    throw new IllegalArgumentException("inputs must not contain duplicates");
                }
            }
        }
    }
}
