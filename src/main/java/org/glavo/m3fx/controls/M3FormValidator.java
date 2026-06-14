// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/// Coordinates validation across multiple [M3TextInputLayout] controls.
///
/// `M3FormValidator` keeps a validation-ordered list of text input layouts, runs each layout's validator,
/// tracks invalid layouts, and exposes read-only aggregate state for submit buttons and error summaries.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
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

    // The first invalid input layout in registration order.
    private final ReadOnlyObjectWrapper<@Nullable M3TextInputLayout> firstInvalidInput =
            new ReadOnlyObjectWrapper<>(this, "firstInvalidInput");

    // Whether all registered input layouts are valid.
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid", true);

    // Whether at least one registered input layout has active validation.
    private final ReadOnlyBooleanWrapper validationActive = new ReadOnlyBooleanWrapper(this, "validationActive");

    // The number of currently invalid input layouts.
    private final ReadOnlyIntegerWrapper invalidInputCount = new ReadOnlyIntegerWrapper(this, "invalidInputCount");

    /// Updates group state when one registered layout changes its validator-produced error text.
    private final ChangeListener<String> validationErrorTextListener =
            (observable, oldValue, newValue) -> refreshInvalidInputs();

    /// Updates group state when one registered layout activates or clears validation.
    private final ChangeListener<Boolean> validationActiveListener =
            (observable, oldValue, newValue) -> refreshInvalidInputs();

    /// Creates an empty form validator.
    public M3FormValidator() {
    }

    /// Creates a form validator with the supplied input layouts.
    ///
    /// @param inputs the input layouts to validate in order
    public M3FormValidator(M3TextInputLayout... inputs) {
        addInputs(inputs);
    }

    /// Returns the registered input layouts in validation order.
    ///
    /// @return the registered input layouts in validation order
    public @UnmodifiableView ObservableList<M3TextInputLayout> getInputs() {
        return inputsView;
    }

    /// Adds one input layout to the end of the validation order.
    ///
    /// @param input the input layout to add
    public void addInput(M3TextInputLayout input) {
        M3TextInputLayout validatedInput = Objects.requireNonNull(input, "input");
        validateNewInput(validatedInput);
        installInput(validatedInput);
        inputs.add(validatedInput);
        refreshInvalidInputs();
    }

    /// Adds input layouts to the end of the validation order.
    ///
    /// @param inputs the input layouts to add
    public void addInputs(M3TextInputLayout... inputs) {
        List<M3TextInputLayout> validatedInputs = validatedNewInputs(inputs);
        installInputs(validatedInputs);
        this.inputs.addAll(validatedInputs);
        refreshInvalidInputs();
    }

    /// Replaces the registered input layouts with the supplied layouts.
    ///
    /// @param inputs the replacement input layouts
    public void setInputs(M3TextInputLayout... inputs) {
        setInputs(validatedInputs(inputs));
    }

    /// Replaces the registered input layouts with the supplied layouts.
    ///
    /// @param inputs the replacement input layouts
    public void setInputs(Collection<? extends M3TextInputLayout> inputs) {
        List<M3TextInputLayout> validatedInputs = validatedInputs(inputs);
        uninstallInputs(this.inputs);
        this.inputs.setAll(validatedInputs);
        installInputs(validatedInputs);
        refreshInvalidInputs();
    }

    /// Removes one input layout from the validation order.
    ///
    /// @param input the input layout to remove
    /// @return `true` when the input layout was registered and removed
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
    ///
    /// @return the currently invalid input layouts in validation order
    public @UnmodifiableView ObservableList<M3TextInputLayout> getInvalidInputs() {
        return invalidInputsView;
    }

    /// Returns the first invalid input layout in validation order.
    ///
    /// @return the first invalid input layout in validation order, or `null` when all inputs are valid
    public @Nullable M3TextInputLayout getFirstInvalidInput() {
        return firstInvalidInput.get();
    }

    /// Returns the first invalid input layout property.
    ///
    /// @return the first invalid input layout property
    public ReadOnlyObjectProperty<@Nullable M3TextInputLayout> firstInvalidInputProperty() {
        return firstInvalidInput.getReadOnlyProperty();
    }

    /// Returns whether all registered input layouts are currently valid.
    ///
    /// @return `true` when all registered input layouts are currently valid
    public boolean isValid() {
        return valid.get();
    }

    /// Returns the valid state property.
    ///
    /// @return the valid state property
    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    /// Returns whether at least one registered input layout has active validation.
    ///
    /// @return `true` when at least one registered input layout has active validation
    public boolean isValidationActive() {
        return validationActive.get();
    }

    /// Returns the group validation-active state property.
    ///
    /// @return the group validation-active state property
    public ReadOnlyBooleanProperty validationActiveProperty() {
        return validationActive.getReadOnlyProperty();
    }

    /// Returns the number of currently invalid input layouts.
    ///
    /// @return the number of currently invalid input layouts
    public int getInvalidInputCount() {
        return invalidInputCount.get();
    }

    /// Returns the invalid input count property.
    ///
    /// @return the invalid input count property
    public ReadOnlyIntegerProperty invalidInputCountProperty() {
        return invalidInputCount.getReadOnlyProperty();
    }

    /// Runs validation on all registered input layouts and returns whether all are valid.
    ///
    /// @return `true` when all registered input layouts validate successfully
    public boolean validate() {
        boolean valid = true;
        for (M3TextInputLayout input : inputs) {
            valid &= input.validate();
        }
        refreshInvalidInputs();
        return valid;
    }

    /// Runs validation on one registered input layout and returns whether it is valid.
    ///
    /// @param input the registered input layout to validate
    /// @return `true` when the input layout validates successfully
    public boolean validateInput(M3TextInputLayout input) {
        boolean valid = registeredInput(input).validate();
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

    /// Clears validator-produced error state on one registered input layout.
    ///
    /// @param input the registered input layout whose validation state is cleared
    public void clearValidation(M3TextInputLayout input) {
        registeredInput(input).clearValidation();
        refreshInvalidInputs();
    }

    /// Requests focus on the first invalid input layout and returns whether one existed.
    ///
    /// @return `true` when an invalid input existed and focus was requested
    public boolean focusFirstInvalidInput() {
        for (M3TextInputLayout invalidInput : invalidInputs) {
            @Nullable Node focusTarget = invalidInputFocusTarget(invalidInput);
            if (focusTarget != null) {
                M3Accessible.showItem(focusTarget);
                return true;
            }
        }
        return false;
    }

    /// Runs validation, focuses the first invalid input layout, and returns whether all inputs are valid.
    ///
    /// @return `true` when all registered input layouts validate successfully
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
        input.validationActiveProperty().addListener(validationActiveListener);
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
        input.validationActiveProperty().removeListener(validationActiveListener);
    }

    /// Rebuilds the invalid input list and read-only state properties.
    private void refreshInvalidInputs() {
        ArrayList<M3TextInputLayout> invalidInputs = new ArrayList<>();
        boolean validationActive = false;
        for (M3TextInputLayout input : inputs) {
            validationActive |= input.isValidationActive();
            if (input.isValidationError()) {
                invalidInputs.add(input);
            }
        }

        this.invalidInputs.setAll(invalidInputs);
        firstInvalidInput.set(invalidInputs.isEmpty() ? null : invalidInputs.get(0));
        this.validationActive.set(validationActive);
        invalidInputCount.set(invalidInputs.size());
        valid.set(invalidInputs.isEmpty());
    }

    /// Returns a registered input layout or throws when the input is not managed by this validator.
    private M3TextInputLayout registeredInput(M3TextInputLayout input) {
        M3TextInputLayout validatedInput = Objects.requireNonNull(input, "input");
        if (!inputs.contains(validatedInput)) {
            throw new IllegalArgumentException("input is not registered");
        }
        return validatedInput;
    }

    /// Returns the preferred reachable focus target for one invalid input.
    private static @Nullable Node invalidInputFocusTarget(M3TextInputLayout invalidInput) {
        @Nullable TextInputControl textInput = invalidInput.getInput();
        @Nullable Node textInputFocusTarget = M3Accessible.structuralFocusTarget(textInput);
        return textInputFocusTarget != null ? textInputFocusTarget : M3Accessible.structuralFocusTarget(invalidInput);
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
