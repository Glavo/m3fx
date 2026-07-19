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
import javafx.beans.value.WeakChangeListener;
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
/// Input layouts are registered in validation order through a live mutable list. [#validate()] invokes every
/// registered layout synchronously in that order and publishes aggregate invalid state after the pass completes.
/// Validator exceptions are propagated. Removing an input stops observing it but does not clear that layout's own
/// validation state.
///
/// Aggregate properties also track validation subsequently triggered by an input layout, such as focus-loss or
/// text-change validation. [#validProperty()] reports whether the current validator-produced error set is empty; it
/// does not imply that validation has been activated. The invalid-input view is live, ordered, and read-only.
///
/// ```java
/// private M3FormValidator configureValidation(
///         M3FormPane form,
///         M3Button submit,
///         M3TextInputLayout email,
///         M3TextInputLayout password) {
///     M3FormValidator validator = new M3FormValidator(email, password);
///     submit.setOnAction(event -> {
///         if (validator.validateAndFocusFirstInvalidInput(form)) {
///             System.out.println("Form accepted");
///         }
///     });
///     return validator;
/// }
/// ```
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public final class M3FormValidator {
    /// Creates a validator with no registered inputs and no active validation.
    public M3FormValidator() {
    }

    /// Creates a validator with the specified input layouts in array order.
    ///
    /// @param inputs the input layouts to validate in order
    /// @throws NullPointerException     if `inputs` or an element of `inputs` is `null`
    /// @throws IllegalArgumentException if `inputs` contains the same layout instance more than once
    public M3FormValidator(M3TextInputLayout... inputs) {
        this.inputs.addAll(inputs);
    }

    /// The first invalid registered input, or `null` when none is currently invalid.
    private final ReadOnlyObjectWrapper<@Nullable M3TextInputLayout> firstInvalidInput =
            new ReadOnlyObjectWrapper<>(this, "firstInvalidInput");

    /// Returns the first invalid input layout in validation order.
    ///
    /// @return the first invalid input layout in validation order, or `null` when all inputs are valid
    public @Nullable M3TextInputLayout getFirstInvalidInput() {
        return firstInvalidInput.get();
    }

    /// Returns the observable read-only first-invalid-input property.
    ///
    /// The property initially contains `null`. It can be used as a binding source but cannot be set or bound as a
    /// writable target.
    ///
    /// @return the first-invalid-input property
    public ReadOnlyObjectProperty<@Nullable M3TextInputLayout> firstInvalidInputProperty() {
        return firstInvalidInput.getReadOnlyProperty();
    }

    /// Whether no registered input currently contributes a validator-produced error.
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid", true);

    /// Returns whether all registered input layouts are currently valid.
    ///
    /// @return `true` when all registered input layouts are currently valid
    public boolean isValid() {
        return valid.get();
    }

    /// Returns the observable read-only aggregate-validity property.
    ///
    /// The property initially is `true`. It can be used as a binding source but cannot be set or bound as a
    /// writable target. A `true` value does not imply that validation has run.
    ///
    /// @return the aggregate-validity property
    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    /// Whether at least one registered input currently has active validation.
    private final ReadOnlyBooleanWrapper validationActive = new ReadOnlyBooleanWrapper(this, "validationActive");

    /// Returns whether at least one registered input layout has active validation.
    ///
    /// @return `true` when at least one registered input layout has active validation
    public boolean isValidationActive() {
        return validationActive.get();
    }

    /// Returns the observable read-only aggregate validation-active property.
    ///
    /// The property initially is `false`. It can be used as a binding source but cannot be set or bound as a
    /// writable target.
    ///
    /// @return the aggregate validation-active property
    public ReadOnlyBooleanProperty validationActiveProperty() {
        return validationActive.getReadOnlyProperty();
    }

    /// The number of registered inputs currently contributing validator-produced errors.
    private final ReadOnlyIntegerWrapper invalidInputCount = new ReadOnlyIntegerWrapper(this, "invalidInputCount");

    /// Returns the number of currently invalid input layouts.
    ///
    /// @return the number of currently invalid input layouts
    public int getInvalidInputCount() {
        return invalidInputCount.get();
    }

    /// Returns the observable read-only invalid-input-count property.
    ///
    /// The property initially is `0` and remains in the inclusive range from `0` through [#getInputs()] size. It
    /// can be used as a binding source but cannot be set or bound as a writable target.
    ///
    /// @return the invalid-input-count property
    public ReadOnlyIntegerProperty invalidInputCountProperty() {
        return invalidInputCount.getReadOnlyProperty();
    }

    /// The live, mutable list of registered text input layouts in validation order.
    ///
    /// The list initially is empty. It rejects `null` and duplicate layout instances. Mutations are observable and
    /// immediately update listener ownership and aggregate state; layouts are not reparented.
    private final ObservableList<M3TextInputLayout> inputs = new InputList();

    /// The currently invalid registered inputs in validation order.
    private final ObservableList<M3TextInputLayout> invalidInputs = FXCollections.observableArrayList();

    /// The unmodifiable invalid input list exposed to callers.
    private final @UnmodifiableView ObservableList<M3TextInputLayout> invalidInputsView =
            FXCollections.unmodifiableObservableList(invalidInputs);

    /// Updates group state when one registered layout changes its validator-produced error text.
    private final ChangeListener<String> validationErrorTextListener =
            (observable, oldValue, newValue) -> {
                if (oldValue.isEmpty() != newValue.isEmpty()) {
                    requestAggregateRefresh(true, false);
                }
            };

    /// Weak wrapper that prevents registered inputs from retaining an otherwise unreachable validator.
    private final WeakChangeListener<String> weakValidationErrorTextListener =
            new WeakChangeListener<>(validationErrorTextListener);

    /// Updates group state when one registered layout activates or clears validation.
    private final ChangeListener<Boolean> validationActiveListener =
            (observable, oldValue, newValue) -> requestAggregateRefresh(false, true);

    /// Weak wrapper that prevents registered inputs from retaining an otherwise unreachable validator.
    private final WeakChangeListener<Boolean> weakValidationActiveListener =
            new WeakChangeListener<>(validationActiveListener);

    /// The nesting depth of aggregate operations that defer form-state refreshes.
    private int aggregateUpdateDepth;

    /// Whether a deferred operation changed invalid-input membership.
    private boolean invalidInputsRefreshPending;

    /// Whether a deferred operation changed aggregate validation activation.
    private boolean validationActiveRefreshPending;

    /// Returns the mutable registered input list in validation order.
    ///
    /// Mutating this list installs and removes validation observation, rejects `null` and duplicate layout
    /// instances, and refreshes aggregate state. The list does not take scene-graph ownership of its elements.
    ///
    /// @return the registered input layouts in validation order
    public ObservableList<M3TextInputLayout> getInputs() {
        return inputs;
    }

    /// Returns an unmodifiable live view of the currently invalid inputs in validation order.
    ///
    /// The view changes when registered input validation state or registration order changes. Mutation operations
    /// are unsupported.
    ///
    /// @return the unmodifiable live invalid-input view
    public @UnmodifiableView ObservableList<M3TextInputLayout> getInvalidInputs() {
        return invalidInputsView;
    }

    /// Runs validation on every registered input and returns whether all accept their current values.
    ///
    /// Inputs are evaluated synchronously in registration order, including inputs after an earlier failure.
    /// Aggregate properties are refreshed once after the pass. Exceptions from an input validator are propagated.
    /// Calling this method with no registered inputs returns `true`.
    ///
    /// @return `true` if every registered input validates successfully
    public boolean validate() {
        boolean valid = true;
        beginAggregateUpdate();
        try {
            for (M3TextInputLayout input : inputs) {
                valid &= input.validate();
            }
        } finally {
            endAggregateUpdate();
        }
        return valid;
    }

    /// Runs validation on one registered input layout and returns whether it is valid.
    ///
    /// @param input the registered input layout to validate
    /// @return `true` when the input layout validates successfully
    /// @throws NullPointerException     if `input` is `null`
    /// @throws IllegalArgumentException if `input` is not registered with this validator
    public boolean validateInput(M3TextInputLayout input) {
        beginAggregateUpdate();
        try {
            return registeredInput(input).validate();
        } finally {
            endAggregateUpdate();
        }
    }

    /// Clears validator-produced error state on all registered inputs in registration order.
    ///
    /// Validators and explicit error text are not changed. Aggregate state is published after every input has been
    /// cleared. Calling this method with no registered inputs has no effect.
    public void clearValidation() {
        beginAggregateUpdate();
        try {
            for (M3TextInputLayout input : inputs) {
                input.clearValidation();
            }
        } finally {
            endAggregateUpdate();
        }
    }

    /// Clears validator-produced error state on one registered input layout.
    ///
    /// @param input the registered input layout whose validation state is cleared
    /// @throws NullPointerException     if `input` is `null`
    /// @throws IllegalArgumentException if `input` is not registered with this validator
    public void clearValidation(M3TextInputLayout input) {
        beginAggregateUpdate();
        try {
            registeredInput(input).clearValidation();
        } finally {
            endAggregateUpdate();
        }
    }

    /// Requests focus on the first reachable invalid input in registration order.
    ///
    /// @return `true` when a reachable invalid input accepted keyboard focus
    public boolean focusFirstInvalidInput() {
        return focusFirstInvalidInputWithOwner(null);
    }

    /// Requests focus on the first reachable invalid input and asks the supplied owner hierarchy to reveal it.
    ///
    /// @param owner the node whose enclosing scroll pane should reveal the focused invalid input
    /// @return `true` when a reachable invalid input accepted keyboard focus
    /// @throws NullPointerException if `owner` is `null`
    public boolean focusFirstInvalidInput(Node owner) {
        return focusFirstInvalidInputWithOwner(Objects.requireNonNull(owner, "owner"));
    }

    /// Runs validation and, on failure, attempts to focus the first reachable invalid input.
    ///
    /// @return `true` when all registered input layouts validate successfully
    public boolean validateAndFocusFirstInvalidInput() {
        boolean valid = validate();
        if (!valid) {
            focusFirstInvalidInput();
        }
        return valid;
    }

    /// Runs validation and, on failure, attempts to reveal and focus the first reachable invalid input.
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
        input.validationErrorTextProperty().addListener(weakValidationErrorTextListener);
        input.validationActiveProperty().addListener(weakValidationActiveListener);
    }

    /// Removes validation listeners from one input layout.
    private void uninstallInput(M3TextInputLayout input) {
        input.validationErrorTextProperty().removeListener(weakValidationErrorTextListener);
        input.validationActiveProperty().removeListener(weakValidationActiveListener);
    }

    /// Begins an operation that coalesces aggregate form-state refreshes.
    private void beginAggregateUpdate() {
        aggregateUpdateDepth++;
    }

    /// Ends an aggregate operation and publishes one consolidated state refresh.
    private void endAggregateUpdate() {
        aggregateUpdateDepth--;
        if (aggregateUpdateDepth != 0) {
            return;
        }

        boolean refreshInvalidInputs = invalidInputsRefreshPending;
        boolean refreshValidationActive = validationActiveRefreshPending;
        invalidInputsRefreshPending = false;
        validationActiveRefreshPending = false;
        refreshAggregateState(refreshInvalidInputs, refreshValidationActive);
    }

    /// Requests an immediate or deferred refresh of selected aggregate state.
    private void requestAggregateRefresh(boolean refreshInvalidInputs, boolean refreshValidationActive) {
        if (aggregateUpdateDepth > 0) {
            invalidInputsRefreshPending |= refreshInvalidInputs;
            validationActiveRefreshPending |= refreshValidationActive;
            return;
        }
        refreshAggregateState(refreshInvalidInputs, refreshValidationActive);
    }

    /// Refreshes invalid membership and validation activation without allocating for stable membership.
    private void refreshAggregateState(boolean refreshInvalidInputs, boolean refreshValidationActive) {
        if (!refreshInvalidInputs && !refreshValidationActive) {
            return;
        }

        int invalidCount = 0;
        @Nullable M3TextInputLayout firstInvalid = null;
        boolean invalidMembershipMatches = true;
        boolean anyValidationActive = false;
        for (M3TextInputLayout input : inputs) {
            if (refreshValidationActive) {
                anyValidationActive |= input.isValidationActive();
            }
            if (refreshInvalidInputs && input.isValidationError()) {
                if (firstInvalid == null) {
                    firstInvalid = input;
                }
                if (invalidCount >= invalidInputs.size() || invalidInputs.get(invalidCount) != input) {
                    invalidMembershipMatches = false;
                }
                invalidCount++;
            }
        }

        if (refreshInvalidInputs) {
            invalidMembershipMatches &= invalidCount == invalidInputs.size();
            if (!invalidMembershipMatches) {
                ArrayList<M3TextInputLayout> refreshedInvalidInputs = new ArrayList<>(invalidCount);
                for (M3TextInputLayout input : inputs) {
                    if (input.isValidationError()) {
                        refreshedInvalidInputs.add(input);
                    }
                }
                invalidInputs.setAll(refreshedInvalidInputs);
            }
            firstInvalidInput.set(firstInvalid);
            invalidInputCount.set(invalidCount);
            valid.set(invalidCount == 0);
        }
        if (refreshValidationActive) {
            validationActive.set(anyValidationActive);
        }
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
            List<M3TextInputLayout> copy = validatedAddCopy(inputs);
            beginAggregateUpdate();
            try {
                return super.addAll(copy);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Adds all inputs at an index after validating the full mutation.
        @Override
        public boolean addAll(int index, Collection<? extends M3TextInputLayout> inputs) {
            List<M3TextInputLayout> copy = validatedAddCopy(inputs);
            beginAggregateUpdate();
            try {
                return super.addAll(index, copy);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Adds all inputs after validating the full mutation.
        @Override
        public boolean addAll(M3TextInputLayout... inputs) {
            List<M3TextInputLayout> copy = validatedAddCopy(inputs);
            beginAggregateUpdate();
            try {
                return super.addAll(copy);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Replaces all inputs after validating the full replacement.
        @Override
        public boolean setAll(Collection<? extends M3TextInputLayout> inputs) {
            List<M3TextInputLayout> copy = validatedReplacementCopy(inputs);
            beginAggregateUpdate();
            try {
                return super.setAll(copy);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Replaces all inputs after validating the full replacement.
        @Override
        public boolean setAll(M3TextInputLayout... inputs) {
            List<M3TextInputLayout> copy = validatedReplacementCopy(inputs);
            beginAggregateUpdate();
            try {
                return super.setAll(copy);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Removes all matching inputs with one aggregate state refresh.
        @Override
        public boolean removeAll(Collection<?> inputs) {
            beginAggregateUpdate();
            try {
                return super.removeAll(inputs);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Removes all supplied inputs with one aggregate state refresh.
        @Override
        public boolean removeAll(M3TextInputLayout... inputs) {
            beginAggregateUpdate();
            try {
                return super.removeAll(inputs);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Retains matching inputs with one aggregate state refresh.
        @Override
        public boolean retainAll(Collection<?> inputs) {
            beginAggregateUpdate();
            try {
                return super.retainAll(inputs);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Retains supplied inputs with one aggregate state refresh.
        @Override
        public boolean retainAll(M3TextInputLayout... inputs) {
            beginAggregateUpdate();
            try {
                return super.retainAll(inputs);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Removes an input range with one aggregate state refresh.
        @Override
        public void remove(int from, int to) {
            beginAggregateUpdate();
            try {
                super.remove(from, to);
            } finally {
                endAggregateUpdate();
            }
        }

        /// Adds one input and installs its validation listeners.
        @Override
        protected void doAdd(int index, M3TextInputLayout input) {
            M3TextInputLayout validatedInput = requireNewInput(input);
            backingList.add(index, validatedInput);
            installInput(validatedInput);
            requestAggregateRefresh(true, true);
        }

        /// Replaces one input and updates validation listeners.
        @Override
        protected M3TextInputLayout doSet(int index, M3TextInputLayout input) {
            M3TextInputLayout validatedInput = requireReplacementInput(index, input);
            M3TextInputLayout oldInput = backingList.set(index, validatedInput);
            if (oldInput != validatedInput) {
                uninstallInput(oldInput);
                installInput(validatedInput);
                requestAggregateRefresh(true, true);
            }
            return oldInput;
        }

        /// Removes one input and uninstalls its validation listeners.
        @Override
        protected M3TextInputLayout doRemove(int index) {
            M3TextInputLayout oldInput = backingList.remove(index);
            uninstallInput(oldInput);
            requestAggregateRefresh(true, true);
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
