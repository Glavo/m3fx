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
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import org.glavo.m3fx.internal.M3Accessible;
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
    private final ObservableList<M3TextInputLayout> inputs = new InputList();

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
        this.inputs.addAll(inputs);
    }

    /// Returns the mutable registered input list in validation order.
    ///
    /// Mutating this list installs and removes validation listeners, rejects `null` layouts, rejects duplicate
    /// layout instances, and refreshes aggregate validation state.
    ///
    /// @return the registered input layouts in validation order
    public ObservableList<M3TextInputLayout> getInputs() {
        return inputs;
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

    /// Requests focus on the first reachable invalid input layout.
    ///
    /// @return `true` when a reachable invalid input accepted keyboard focus
    public boolean focusFirstInvalidInput() {
        return focusFirstInvalidInputWithOwner(null);
    }

    /// Requests focus on the first reachable invalid input layout and reveals it through the supplied owner.
    ///
    /// @param owner the node whose enclosing scroll pane should reveal the focused invalid input
    /// @return `true` when a reachable invalid input accepted keyboard focus
    public boolean focusFirstInvalidInput(Node owner) {
        return focusFirstInvalidInputWithOwner(Objects.requireNonNull(owner, "owner"));
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

    /// Runs validation, focuses the first invalid input layout, reveals it through the owner, and returns validity.
    ///
    /// @param owner the node whose enclosing scroll pane should reveal the focused invalid input
    /// @return `true` when all registered input layouts validate successfully
    public boolean validateAndFocusFirstInvalidInput(Node owner) {
        boolean valid = validate();
        if (!valid) {
            focusFirstInvalidInput(owner);
        }
        return valid;
    }

    /// Requests focus on the first reachable invalid input layout with an optional reveal owner.
    private boolean focusFirstInvalidInputWithOwner(@Nullable Node owner) {
        for (M3TextInputLayout invalidInput : invalidInputs) {
            @Nullable Node focusTarget = invalidInputFocusTarget(invalidInput);
            if (focusTarget != null) {
                boolean focused = owner == null
                        ? M3Accessible.showItem(focusTarget)
                        : M3Accessible.showItem(owner, focusTarget);
                if (focused) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Installs validation listeners on one input layout.
    private void installInput(M3TextInputLayout input) {
        input.validationErrorTextProperty().addListener(validationErrorTextListener);
        input.validationActiveProperty().addListener(validationActiveListener);
    }

    /// Removes validation listeners from one input layout.
    private void uninstallInput(M3TextInputLayout input) {
        input.validationErrorTextProperty().removeListener(validationErrorTextListener);
        input.validationActiveProperty().removeListener(validationActiveListener);
    }

    /// Rebuilds the invalid input list and read-only state properties.
    private void refreshInvalidInputs() {
        ArrayList<M3TextInputLayout> refreshedInvalidInputs = new ArrayList<>();
        boolean validationActive = false;
        for (M3TextInputLayout input : inputs) {
            validationActive |= input.isValidationActive();
            if (input.isValidationError()) {
                refreshedInvalidInputs.add(input);
            }
        }

        if (!sameInputs(invalidInputs, refreshedInvalidInputs)) {
            invalidInputs.setAll(refreshedInvalidInputs);
        }
        firstInvalidInput.set(refreshedInvalidInputs.isEmpty() ? null : refreshedInvalidInputs.get(0));
        this.validationActive.set(validationActive);
        invalidInputCount.set(refreshedInvalidInputs.size());
        valid.set(refreshedInvalidInputs.isEmpty());
    }

    /// Returns whether two input lists contain the same layouts in the same order.
    private static boolean sameInputs(List<M3TextInputLayout> first, List<M3TextInputLayout> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int index = 0; index < first.size(); index++) {
            if (first.get(index) != second.get(index)) {
                return false;
            }
        }
        return true;
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

    /// Mutable input list that keeps validator listeners synchronized with list contents.
    private final class InputList extends ModifiableObservableListBase<M3TextInputLayout> {
        /// The registered inputs.
        private final ArrayList<M3TextInputLayout> backingList = new ArrayList<>();

        /// Returns the input at the requested index.
        @Override
        public M3TextInputLayout get(int index) {
            return backingList.get(index);
        }

        /// Returns the number of registered inputs.
        @Override
        public int size() {
            return backingList.size();
        }

        /// Adds all inputs after validating the full mutation.
        @Override
        public boolean addAll(Collection<? extends M3TextInputLayout> inputs) {
            return super.addAll(validatedAddCopy(inputs));
        }

        /// Adds all inputs at an index after validating the full mutation.
        @Override
        public boolean addAll(int index, Collection<? extends M3TextInputLayout> inputs) {
            return super.addAll(index, validatedAddCopy(inputs));
        }

        /// Adds all inputs after validating the full mutation.
        @Override
        public boolean addAll(M3TextInputLayout... inputs) {
            return super.addAll(validatedAddCopy(inputs));
        }

        /// Replaces all inputs after validating the full replacement.
        @Override
        public boolean setAll(Collection<? extends M3TextInputLayout> inputs) {
            return super.setAll(validatedReplacementCopy(inputs));
        }

        /// Replaces all inputs after validating the full replacement.
        @Override
        public boolean setAll(M3TextInputLayout... inputs) {
            return super.setAll(validatedReplacementCopy(inputs));
        }

        /// Adds one input and installs its validation listeners.
        @Override
        protected void doAdd(int index, M3TextInputLayout input) {
            M3TextInputLayout validatedInput = requireNewInput(input);
            backingList.add(index, validatedInput);
            installInput(validatedInput);
            refreshInvalidInputs();
        }

        /// Replaces one input and updates validation listeners.
        @Override
        protected M3TextInputLayout doSet(int index, M3TextInputLayout input) {
            M3TextInputLayout validatedInput = requireReplacementInput(index, input);
            M3TextInputLayout oldInput = backingList.set(index, validatedInput);
            if (oldInput != validatedInput) {
                uninstallInput(oldInput);
                installInput(validatedInput);
                refreshInvalidInputs();
            }
            return oldInput;
        }

        /// Removes one input and uninstalls its validation listeners.
        @Override
        protected M3TextInputLayout doRemove(int index) {
            M3TextInputLayout oldInput = backingList.remove(index);
            uninstallInput(oldInput);
            refreshInvalidInputs();
            return oldInput;
        }

        /// Returns a validated copy for adding inputs.
        private List<M3TextInputLayout> validatedAddCopy(Collection<? extends M3TextInputLayout> inputs) {
            Objects.requireNonNull(inputs, "inputs");
            ArrayList<M3TextInputLayout> copy = new ArrayList<>(inputs.size());
            for (M3TextInputLayout input : inputs) {
                copy.add(requireNewInput(input));
            }
            validateDistinctInputs(copy);
            return copy;
        }

        /// Returns a validated copy for adding inputs.
        private List<M3TextInputLayout> validatedAddCopy(M3TextInputLayout[] inputs) {
            Objects.requireNonNull(inputs, "inputs");
            ArrayList<M3TextInputLayout> copy = new ArrayList<>(inputs.length);
            for (M3TextInputLayout input : inputs) {
                copy.add(requireNewInput(input));
            }
            validateDistinctInputs(copy);
            return copy;
        }

        /// Returns a validated copy for replacing all inputs.
        private List<M3TextInputLayout> validatedReplacementCopy(Collection<? extends M3TextInputLayout> inputs) {
            Objects.requireNonNull(inputs, "inputs");
            ArrayList<M3TextInputLayout> copy = new ArrayList<>(inputs.size());
            for (M3TextInputLayout input : inputs) {
                copy.add(Objects.requireNonNull(input, "input"));
            }
            validateDistinctInputs(copy);
            return copy;
        }

        /// Returns a validated copy for replacing all inputs.
        private List<M3TextInputLayout> validatedReplacementCopy(M3TextInputLayout[] inputs) {
            Objects.requireNonNull(inputs, "inputs");
            ArrayList<M3TextInputLayout> copy = new ArrayList<>(inputs.length);
            for (M3TextInputLayout input : inputs) {
                copy.add(Objects.requireNonNull(input, "input"));
            }
            validateDistinctInputs(copy);
            return copy;
        }

        /// Returns one non-null input that is not already registered.
        private M3TextInputLayout requireNewInput(M3TextInputLayout input) {
            M3TextInputLayout validatedInput = Objects.requireNonNull(input, "input");
            if (backingList.contains(validatedInput)) {
                throw new IllegalArgumentException("input is already registered");
            }
            return validatedInput;
        }

        /// Returns one non-null input that is not registered at another index.
        private M3TextInputLayout requireReplacementInput(int replacementIndex, M3TextInputLayout input) {
            M3TextInputLayout validatedInput = Objects.requireNonNull(input, "input");
            for (int index = 0; index < backingList.size(); index++) {
                if (index != replacementIndex && backingList.get(index) == validatedInput) {
                    throw new IllegalArgumentException("input is already registered");
                }
            }
            return validatedInput;
        }

        /// Validates that a candidate input list contains no duplicate instances.
        private void validateDistinctInputs(List<M3TextInputLayout> inputs) {
            for (int firstIndex = 0; firstIndex < inputs.size(); firstIndex++) {
                M3TextInputLayout input = inputs.get(firstIndex);
                for (int secondIndex = firstIndex + 1; secondIndex < inputs.size(); secondIndex++) {
                    if (input == inputs.get(secondIndex)) {
                        throw new IllegalArgumentException("inputs must not contain duplicates");
                    }
                }
            }
        }
    }
}
