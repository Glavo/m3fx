// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Defines the validation and presentation contract for an input managed by an [M3FormValidator].
///
/// A form input owns its validation lifecycle and exposes validator-produced errors separately from application
/// error state. [M3FormValidator] observes these properties but does not take scene-graph ownership of the input's
/// semantic node or focus target.
@NotNullByDefault
public interface M3FormInput {
    /// Runs validation against the input's current editor state.
    ///
    /// @implSpec An implementation must publish its resulting validation-active and validation-error state before
    /// returning. The return value must be equal to `!isValidationError()` after the operation completes.
    ///
    /// @return `true` when the current state is valid
    boolean validate();

    /// Clears validator-produced state without changing the current value or application error state.
    ///
    /// @implSpec An implementation must make [#isValidationActive()] and [#isValidationError()] both return `false`
    /// before this operation completes.
    void clearValidation();

    /// Returns whether validation currently contributes an error to a form validator.
    ///
    /// @implSpec The default implementation returns whether [#getValidationErrorText()] is non-empty.
    ///
    /// @return `true` when [#getValidationErrorText()] is non-empty
    default boolean isValidationError() {
        return !getValidationErrorText().isEmpty();
    }

    /// Returns the current validator-produced error text.
    ///
    /// @implSpec The default implementation returns the current value of [#validationErrorTextProperty()].
    ///
    /// @return the validation error text, or an empty string when validation succeeds or is inactive
    default String getValidationErrorText() {
        return validationErrorTextProperty().get();
    }

    /// Returns the observable read-only validator-produced error property.
    ///
    /// @return the validation error property
    ReadOnlyStringProperty validationErrorTextProperty();

    /// Returns whether validation has run and has not subsequently been cleared.
    ///
    /// @implSpec The default implementation returns the current value of [#validationActiveProperty()].
    ///
    /// @return `true` while validation is active
    default boolean isValidationActive() {
        return validationActiveProperty().get();
    }

    /// Returns the observable read-only validation-active property.
    ///
    /// @return the validation-active property
    ReadOnlyBooleanProperty validationActiveProperty();

    /// Returns the label used to identify this input in validation presentation.
    ///
    /// Blank text permits a presenter to use the focus target's prompt text or a generic fallback.
    ///
    /// @implSpec The default implementation returns the current value of [#labelTextProperty()].
    ///
    /// @return the non-null validation label text
    default String getLabelText() {
        return labelTextProperty().get();
    }

    /// Returns the observable, bindable validation-label property.
    ///
    /// @return the validation-label property
    StringProperty labelTextProperty();

    /// Returns the root node whose visibility, disabled state, and accessibility descendants represent this input.
    ///
    /// The returned node must remain stable for the lifetime of this form input.
    ///
    /// @return the semantic root node
    Node getValidationNode();

    /// Returns the preferred node to focus after validation fails.
    ///
    /// The target may be `null` while the input has no editor. Callers must still verify that a non-null target is
    /// reachable before requesting focus.
    ///
    /// @implSpec The default implementation returns the current value of [#validationFocusTargetProperty()].
    ///
    /// @return the preferred focus target, or `null` when none is installed
    default @Nullable Node getValidationFocusTarget() {
        return validationFocusTargetProperty().getValue();
    }

    /// Returns an observable value for the preferred validation focus target.
    ///
    /// The observable may return a more specific [Node] subtype. Its value is not owned or reparented by form
    /// validation infrastructure. Repeated calls must return the same observable instance.
    ///
    /// @return the observable validation focus target
    ObservableValue<? extends @Nullable Node> validationFocusTargetProperty();
}
