// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests property-derived form input queries and instance-based registration of custom inputs.
@NotNullByDefault
final class M3FormInputContractTest {
    /// Starts JavaFX before creating the test inputs.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Derives all default queries from the input's observable properties.
    @Test
    void defaultQueriesFollowValidationAndPresentationProperties() {
        FxTestUtils.runOnFxThread(() -> {
            EqualInput input = newInput();
            assertFalse(input.isValidationActive());
            assertFalse(input.isValidationError());
            assertEquals("", input.getValidationErrorText());
            input.labelTextProperty().set("Name");
            assertEquals("Name", input.getLabelText());
            assertSame(input.layout().getInput(), input.getValidationFocusTarget());
            assertFalse(input.validate());
            assertTrue(input.isValidationActive());
            assertTrue(input.isValidationError());
            assertEquals("Required", input.getValidationErrorText());
            input.clearValidation();
            assertFalse(input.isValidationActive());
            assertFalse(input.isValidationError());
            input.layout().setInput(null);
            assertNull(input.getValidationFocusTarget());
        });
    }

    /// Accepts equal but distinct inputs and rejects actions on an unregistered equal instance.
    @Test
    void registrationUsesIdentityForEveryMutationAndSingleInputAction() {
        FxTestUtils.runOnFxThread(() -> {
            EqualInput first = newInput();
            EqualInput second = newInput();
            EqualInput unregistered = newInput();
            M3FormValidator validator = new M3FormValidator(first);
            validator.getInputs().add(second);
            assertEquals(2, validator.getInputs().size());
            assertThrows(IllegalArgumentException.class, () -> validator.validateInput(unregistered));
            assertThrows(IllegalArgumentException.class, () -> validator.clearValidation(unregistered));
            assertFalse(unregistered.isValidationActive());
            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().add(first));
            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().set(1, first));
            assertThrows(IllegalArgumentException.class, () -> validator.getInputs().setAll(first, first));
            assertSame(second, validator.getInputs().get(1));

            validator.getInputs().setAll(second, first);
            assertFalse(validator.validate());
            assertEquals(2, validator.getInvalidInputCount());
            assertSame(second, validator.getFirstInvalidInput());
            validator.clearValidation(first);
            assertTrue(second.isValidationError());
            assertFalse(first.isValidationError());
            validator.getInputs().clear();
            validator.getInputs().addAll(first, second);
            assertSame(first, validator.getInputs().get(0));
            assertSame(second, validator.getInputs().get(1));
        });
    }

    /// Creates a custom input whose empty editor fails validation.
    private static EqualInput newInput() {
        M3TextInputLayout layout = new M3TextInputLayout(new M3TextField());
        layout.setValidator(M3TextInputValidators.required("Required"));
        return new EqualInput(layout);
    }

    /// Adapts an editor while deliberately comparing equal to other adapter instances.
    ///
    /// @param layout the independently owned validation state
    @NotNullByDefault
    private record EqualInput(M3TextInputLayout layout) implements M3FormInput {
        /// Validates the wrapped input.
        @Override
        public boolean validate() {
            return layout.validate();
        }

        /// Clears the wrapped input's validation state.
        @Override
        public void clearValidation() {
            layout.clearValidation();
        }

        /// Returns the wrapped input's error property.
        @Override
        public ReadOnlyStringProperty validationErrorTextProperty() {
            return layout.validationErrorTextProperty();
        }

        /// Returns the wrapped input's activation property.
        @Override
        public ReadOnlyBooleanProperty validationActiveProperty() {
            return layout.validationActiveProperty();
        }

        /// Returns the wrapped input's label property.
        @Override
        public StringProperty labelTextProperty() {
            return layout.labelTextProperty();
        }

        /// Returns the independently owned semantic root.
        @Override
        public Node getValidationNode() {
            return layout;
        }

        /// Returns the wrapped input's observable focus target.
        @Override
        public ObservableValue<? extends @Nullable Node> validationFocusTargetProperty() {
            return layout.validationFocusTargetProperty();
        }

        /// Compares equal to every instance of this test adapter.
        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof EqualInput;
        }

        /// Returns the same hash for all equal adapters.
        @Override
        public int hashCode() {
            return 0;
        }
    }
}
