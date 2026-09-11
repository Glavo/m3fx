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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Coordinates validation across multiple [M3FormInput] controls.
///
/// Inputs are registered in validation order through a live mutable list. [#validate()] captures that order for
/// each synchronous pass and publishes aggregate invalid state after the pass completes.
/// Validator exceptions are propagated. Removing an input stops observing it but does not clear that input's own
/// validation state.
/// Registration and single-input validation operations identify inputs by instance, independently of `equals`.
///
/// Aggregate properties also track validation subsequently triggered by an input, such as focus-loss or
/// text-change validation. [#validProperty()] reports whether the current validator-produced error set is empty; it
/// does not imply that validation has been activated. The invalid-input view is live, ordered, and read-only.
/// Aggregate notifications are separate. Changes made by their listeners are reconciled before the outer update
/// returns successfully; listeners may observe intermediate aggregate values while publication is in progress.
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
        inputs.addListener(this::inputsChanged);
    }

    /// Creates a validator with the specified inputs in array order.
    ///
    /// @param inputs the form inputs to validate in order
    /// @throws NullPointerException     if `inputs` or an element of `inputs` is `null`
    /// @throws IllegalArgumentException if `inputs` contains the same input instance more than once
    public M3FormValidator(M3FormInput... inputs) {
        this();
        this.inputs.addAll(inputs);
    }

    /// The first invalid registered input, or `null` when none is currently invalid.
    private final ReadOnlyObjectWrapper<@Nullable M3FormInput> firstInvalidInput =
            new ReadOnlyObjectWrapper<>(this, "firstInvalidInput");

    /// Returns the first invalid input in validation order.
    ///
    /// @return the first invalid input in validation order, or `null` when all inputs are valid
    public @Nullable M3FormInput getFirstInvalidInput() {
        return firstInvalidInput.get();
    }

    /// Returns the observable read-only first-invalid-input property.
    ///
    /// The property initially contains `null`. It can be used as a binding source but cannot be set or bound as a
    /// writable target.
    ///
    /// @return the first-invalid-input property
    public ReadOnlyObjectProperty<@Nullable M3FormInput> firstInvalidInputProperty() {
        return firstInvalidInput.getReadOnlyProperty();
    }

    /// Whether no registered input currently contributes a validator-produced error.
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid", true);

    /// Returns whether all registered inputs are currently valid.
    ///
    /// @return `true` when all registered inputs are currently valid
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

    /// Returns whether at least one registered input has active validation.
    ///
    /// @return `true` when at least one registered input has active validation
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

    /// Returns the number of currently invalid inputs.
    ///
    /// @return the number of currently invalid inputs
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

    /// The live, mutable list of registered form inputs in validation order.
    ///
    /// The list initially is empty. It rejects `null` and duplicate input instances. Mutations are observable and
    /// immediately update listener ownership and aggregate state; semantic nodes are not reparented.
    private final ObservableList<M3FormInput> inputs = M3ObservableLists.identityDistinctElementList("input");

    /// The currently invalid registered inputs in validation order.
    private final ObservableList<M3FormInput> invalidInputs = FXCollections.observableArrayList();

    /// The unmodifiable invalid input list exposed to callers.
    private final @UnmodifiableView ObservableList<M3FormInput> invalidInputsView =
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
    /// Mutating this list installs and removes validation observation, rejects `null` and duplicate input
    /// instances, and refreshes aggregate state. Distinct instances may compare equal. The list does not take
    /// scene-graph ownership of its elements; its ordinary search and removal operations follow [List] equality.
    /// Direct bulk additions and replacements validate their complete candidate list before changing registrations.
    /// Sorting preserves input instances and reports their new registration order as a permutation.
    ///
    /// @return the registered inputs in validation order
    public ObservableList<M3FormInput> getInputs() {
        return inputs;
    }

    /// Returns an unmodifiable live view of the currently invalid inputs in validation order.
    ///
    /// The view changes when registered input validation state or registration order changes. Mutation operations
    /// are unsupported.
    ///
    /// @return the unmodifiable live invalid-input view
    public @UnmodifiableView ObservableList<M3FormInput> getInvalidInputs() {
        return invalidInputsView;
    }

    /// Runs validation on every registered input and returns whether all accept their current values.
    ///
    /// Each pass captures the registered input instances and their order when it starts. An input is skipped if it
    /// is no longer registered when its turn starts. New inputs are deferred to the next pass, and reordering the
    /// registered list does not change the current pass's order. Inputs after a validation failure are still evaluated.
    ///
    /// Aggregate publication is deferred until the pass finishes. If a validator throws, the exception is propagated
    /// and later inputs are not evaluated; state changes already made are retained and reflected by the aggregates.
    /// Calling this method with no registered inputs returns `true`.
    ///
    /// @return `true` if every input invoked by this pass validates successfully
    public boolean validate() {
        @Unmodifiable List<M3FormInput> batch = List.copyOf(inputs);
        boolean valid = true;
        beginAggregateUpdate();
        try {
            for (M3FormInput input : batch) {
                if (inputIdentityIndex(input) >= 0) {
                    valid &= input.validate();
                }
            }
        } finally {
            endAggregateUpdate();
        }
        return valid;
    }

    /// Runs validation on one registered input and returns whether it is valid.
    ///
    /// @param input the registered input to validate
    /// @return `true` when the input validates successfully
    /// @throws NullPointerException     if `input` is `null`
    /// @throws IllegalArgumentException if `input` is not registered with this validator
    public boolean validateInput(M3FormInput input) {
        beginAggregateUpdate();
        try {
            return registeredInput(input).validate();
        } finally {
            endAggregateUpdate();
        }
    }

    /// Clears validator-produced error state on all registered inputs in registration order.
    ///
    /// Uses the same registration snapshot rules as [#validate()]. Validators and explicit error text are not changed.
    /// Aggregate publication is deferred until the pass finishes. If clearing an input throws, later inputs are not
    /// cleared; state changes already made are retained. Calling this method with no registered inputs has no effect.
    public void clearValidation() {
        @Unmodifiable List<M3FormInput> batch = List.copyOf(inputs);
        beginAggregateUpdate();
        try {
            for (M3FormInput input : batch) {
                if (inputIdentityIndex(input) >= 0) {
                    input.clearValidation();
                }
            }
        } finally {
            endAggregateUpdate();
        }
    }

    /// Clears validator-produced error state on one registered input.
    ///
    /// @param input the registered input whose validation state is cleared
    /// @throws NullPointerException     if `input` is `null`
    /// @throws IllegalArgumentException if `input` is not registered with this validator
    public void clearValidation(M3FormInput input) {
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
    /// @return `true` when all registered inputs validate successfully
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
    /// @return `true` when all registered inputs validate successfully
    public boolean validateAndFocusFirstInvalidInput(Node owner) {
        boolean valid = validate();
        if (!valid) {
            focusFirstInvalidInput(owner);
        }
        return valid;
    }

    /// Requests focus on the first reachable invalid input with an optional reveal owner.
    private boolean focusFirstInvalidInputWithOwner(@Nullable Node owner) {
        for (M3FormInput invalidInput : invalidInputs) {
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

    /// Updates observation and aggregate state once for each complete registration-list change.
    private void inputsChanged(ListChangeListener.Change<? extends M3FormInput> change) {
        beginAggregateUpdate();
        try {
            while (change.next()) {
                if (!change.wasPermutated() && !change.wasUpdated()) {
                    for (M3FormInput input : change.getRemoved()) {
                        uninstallInput(input);
                    }
                    for (M3FormInput input : change.getAddedSubList()) {
                        installInput(input);
                    }
                }
            }
            requestAggregateRefresh(true, true);
        } finally {
            endAggregateUpdate();
        }
    }

    /// Installs validation listeners on one input.
    private void installInput(M3FormInput input) {
        input.validationErrorTextProperty().addListener(weakValidationErrorTextListener);
        input.validationActiveProperty().addListener(weakValidationActiveListener);
    }

    /// Removes validation listeners from one input.
    private void uninstallInput(M3FormInput input) {
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
        requestAggregateRefresh(false, false);
    }

    /// Requests an immediate or deferred refresh of selected aggregate state.
    private void requestAggregateRefresh(boolean refreshInvalidInputs, boolean refreshValidationActive) {
        invalidInputsRefreshPending |= refreshInvalidInputs;
        validationActiveRefreshPending |= refreshValidationActive;
        if (aggregateUpdateDepth > 0) {
            return;
        }

        aggregateUpdateDepth++;
        try {
            while (isAggregateRefreshPending()) {
                boolean updateInvalidInputs = invalidInputsRefreshPending;
                boolean updateValidationActive = validationActiveRefreshPending;
                invalidInputsRefreshPending = false;
                validationActiveRefreshPending = false;
                refreshAggregateState(updateInvalidInputs, updateValidationActive);
                if (isAggregateRefreshPending()) {
                    // A callback interrupted publication; recompute every part of that unfinished snapshot.
                    invalidInputsRefreshPending |= updateInvalidInputs;
                    validationActiveRefreshPending |= updateValidationActive;
                }
            }
        } finally {
            aggregateUpdateDepth--;
        }
    }

    /// Returns whether observed changes require another aggregate snapshot.
    private boolean isAggregateRefreshPending() {
        return invalidInputsRefreshPending || validationActiveRefreshPending;
    }

    /// Refreshes invalid membership and validation activation without allocating for stable membership.
    private void refreshAggregateState(boolean refreshInvalidInputs, boolean refreshValidationActive) {
        if (!refreshInvalidInputs && !refreshValidationActive) {
            return;
        }

        int invalidCount = 0;
        @Nullable M3FormInput firstInvalid = null;
        boolean invalidMembershipMatches = true;
        boolean anyValidationActive = false;
        for (M3FormInput input : inputs) {
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
                ArrayList<M3FormInput> refreshedInvalidInputs = new ArrayList<>(invalidCount);
                for (M3FormInput input : inputs) {
                    if (input.isValidationError()) {
                        refreshedInvalidInputs.add(input);
                    }
                }
                invalidInputs.setAll(refreshedInvalidInputs);
            }
            if (isAggregateRefreshPending()) {
                return;
            }
            firstInvalidInput.set(firstInvalid);
            if (isAggregateRefreshPending()) {
                return;
            }
            invalidInputCount.set(invalidCount);
            if (isAggregateRefreshPending()) {
                return;
            }
            valid.set(invalidCount == 0);
        }
        if (refreshValidationActive && !isAggregateRefreshPending()) {
            validationActive.set(anyValidationActive);
        }
    }

    /// Returns a registered input or throws when the input is not managed by this validator.
    private M3FormInput registeredInput(M3FormInput input) {
        M3FormInput validatedInput = Objects.requireNonNull(input, "input");
        if (inputIdentityIndex(validatedInput) < 0) {
            throw new IllegalArgumentException("input is not registered");
        }
        return validatedInput;
    }

    /// Returns the registration index of this exact input instance, or `-1` when it is absent.
    private int inputIdentityIndex(M3FormInput input) {
        for (int index = 0; index < inputs.size(); index++) {
            if (inputs.get(index) == input) {
                return index;
            }
        }
        return -1;
    }

    /// Returns the preferred reachable focus target for one invalid input.
    private static @Nullable Node invalidInputFocusTarget(M3FormInput invalidInput) {
        @Nullable Node preferredTarget = M3Accessible.structuralFocusTarget(invalidInput.getValidationFocusTarget());
        return preferredTarget != null
                ? preferredTarget
                : M3Accessible.structuralFocusTarget(invalidInput.getValidationNode());
    }

}
