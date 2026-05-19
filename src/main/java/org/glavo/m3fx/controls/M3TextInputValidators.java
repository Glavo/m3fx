// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.control.TextInputControl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

/// Provides reusable validators for [M3TextInputLayout].
@NotNullByDefault
public final class M3TextInputValidators {
    /// Prevents utility class instantiation.
    private M3TextInputValidators() {
    }

    /// Returns a validator that always accepts the current value.
    public static M3TextInputValidator none() {
        return (input, text) -> null;
    }

    /// Returns a validator that rejects blank text.
    public static M3TextInputValidator required(String errorText) {
        String message = Objects.requireNonNull(errorText, "errorText");
        return (input, text) -> text.isBlank() ? message : null;
    }

    /// Returns a validator that rejects text shorter than the requested minimum length.
    public static M3TextInputValidator minLength(int minLength, String errorText) {
        int minimum = nonNegative(minLength, "minLength");
        String message = Objects.requireNonNull(errorText, "errorText");
        return (input, text) -> text.length() < minimum ? message : null;
    }

    /// Returns a validator that rejects text longer than the requested maximum length.
    public static M3TextInputValidator maxLength(int maxLength, String errorText) {
        int maximum = nonNegative(maxLength, "maxLength");
        String message = Objects.requireNonNull(errorText, "errorText");
        return (input, text) -> text.length() > maximum ? message : null;
    }

    /// Returns a validator that rejects text outside the inclusive length range.
    public static M3TextInputValidator lengthBetween(
            int minLength,
            int maxLength,
            String tooShortErrorText,
            String tooLongErrorText
    ) {
        int minimum = nonNegative(minLength, "minLength");
        int maximum = nonNegative(maxLength, "maxLength");
        if (minimum > maximum) {
            throw new IllegalArgumentException("minLength must be less than or equal to maxLength");
        }

        String tooShortMessage = Objects.requireNonNull(tooShortErrorText, "tooShortErrorText");
        String tooLongMessage = Objects.requireNonNull(tooLongErrorText, "tooLongErrorText");
        return (input, text) -> {
            if (text.length() < minimum) {
                return tooShortMessage;
            }
            return text.length() > maximum ? tooLongMessage : null;
        };
    }

    /// Returns a validator that accepts only text matching the supplied pattern.
    public static M3TextInputValidator pattern(Pattern pattern, String errorText) {
        Pattern validatedPattern = Objects.requireNonNull(pattern, "pattern");
        String message = Objects.requireNonNull(errorText, "errorText");
        return (input, text) -> validatedPattern.matcher(text).matches() ? null : message;
    }

    /// Returns a validator backed by a boolean predicate.
    public static M3TextInputValidator predicate(
            BiPredicate<? super TextInputControl, ? super String> predicate,
            String errorText
    ) {
        BiPredicate<? super TextInputControl, ? super String> validatedPredicate =
                Objects.requireNonNull(predicate, "predicate");
        String message = Objects.requireNonNull(errorText, "errorText");
        return (input, text) -> validatedPredicate.test(input, text) ? null : message;
    }

    /// Returns a validator that evaluates validators in order and reports the first error.
    public static M3TextInputValidator all(M3TextInputValidator... validators) {
        M3TextInputValidator[] validatedValidators = validatedValidators(validators);
        return (input, text) -> firstError(input, text, validatedValidators);
    }

    /// Returns the first error produced by the supplied validators.
    static @Nullable String firstError(
            TextInputControl input,
            String text,
            Iterable<? extends M3TextInputValidator> validators
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(validators, "validators");

        for (M3TextInputValidator validator : validators) {
            @Nullable String errorText = Objects.requireNonNull(validator, "validator").validate(input, text);
            if (isError(errorText)) {
                return errorText;
            }
        }
        return null;
    }

    /// Returns the first error produced by the supplied validators.
    private static @Nullable String firstError(
            TextInputControl input,
            String text,
            M3TextInputValidator[] validators
    ) {
        for (M3TextInputValidator validator : validators) {
            @Nullable String errorText = validator.validate(input, text);
            if (isError(errorText)) {
                return errorText;
            }
        }
        return null;
    }

    /// Returns whether a validation message represents an error.
    private static boolean isError(@Nullable String errorText) {
        return errorText != null && !errorText.isEmpty();
    }

    /// Returns a validated copy of a validator array.
    private static M3TextInputValidator[] validatedValidators(M3TextInputValidator... validators) {
        Objects.requireNonNull(validators, "validators");
        M3TextInputValidator[] copy = validators.clone();
        for (M3TextInputValidator validator : copy) {
            Objects.requireNonNull(validator, "validator");
        }
        return copy;
    }

    /// Returns a non-negative integer value.
    private static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
        return value;
    }
}
