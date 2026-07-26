// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.glavo.m3fx.internal.M3Css;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Creates JavaFX properties with validation for direct assignments used by color controls.
///
/// These properties do not alter JavaFX binding lifecycle. A caller that binds one of them must ensure that the
/// source observable always supplies values accepted by the corresponding setter.
@NotNullByDefault
final class M3ColorProperties {
    /// Prevents utility class instantiation.
    private M3ColorProperties() {
    }

    /// Creates a non-null object property without additional invalidation work.
    ///
    /// @param bean         the property owner
    /// @param name         the property name
    /// @param initialValue the initial non-null value
    /// @param <T>          the property value type
    /// @return the constrained object property
    /// @throws NullPointerException if an argument is `null`
    static <T> ObjectProperty<T> nonNullObjectProperty(Object bean, String name, T initialValue) {
        return nonNullObjectProperty(bean, name, initialValue, () -> {
        });
    }

    /// Creates a non-null object property.
    ///
    /// @param bean         the property owner
    /// @param name         the property name
    /// @param initialValue the initial non-null value
    /// @param invalidation the callback invoked after the property becomes invalid
    /// @param <T>          the property value type
    /// @return the constrained object property
    /// @throws NullPointerException if an argument is `null`
    static <T> ObjectProperty<T> nonNullObjectProperty(
            Object bean,
            String name,
            T initialValue,
            Runnable invalidation
    ) {
        Object owner = Objects.requireNonNull(bean, "bean");
        String propertyName = Objects.requireNonNull(name, "name");
        T initial = Objects.requireNonNull(initialValue, "initialValue");
        Runnable callback = Objects.requireNonNull(invalidation, "invalidation");
        return new SimpleObjectProperty<>(owner, propertyName, initial) {
            /// Rejects a direct null assignment before changing the property.
            ///
            /// @param value the new non-null value
            @Override
            public void set(T value) {
                super.set(Objects.requireNonNull(value, getName()));
            }

            /// Invokes the owner-provided invalidation callback.
            @Override
            protected void invalidated() {
                callback.run();
            }
        };
    }

    /// Creates a finite, non-negative double property.
    ///
    /// @param bean         the property owner
    /// @param name         the property name
    /// @param initialValue the initial finite, non-negative value
    /// @param invalidation the callback invoked after the property becomes invalid
    /// @return the constrained double property
    /// @throws NullPointerException if an object argument is `null`
    /// @throws IllegalArgumentException if `initialValue` is negative or not finite
    static DoubleProperty nonNegativeDoubleProperty(
            Object bean,
            String name,
            double initialValue,
            Runnable invalidation
    ) {
        Object owner = Objects.requireNonNull(bean, "bean");
        String propertyName = Objects.requireNonNull(name, "name");
        double initial = M3Css.nonNegative(initialValue, propertyName);
        Runnable callback = Objects.requireNonNull(invalidation, "invalidation");
        return new SimpleDoubleProperty(owner, propertyName, initial) {
            /// Rejects an invalid direct assignment before changing the property.
            ///
            /// @param value the new finite, non-negative value
            @Override
            public void set(double value) {
                super.set(M3Css.nonNegative(value, getName()));
            }

            /// Invokes the owner-provided invalidation callback.
            @Override
            protected void invalidated() {
                callback.run();
            }
        };
    }

    /// Creates a positive integer property.
    ///
    /// @param bean         the property owner
    /// @param name         the property name
    /// @param initialValue the initial positive value
    /// @param invalidation the callback invoked after the property becomes invalid
    /// @return the constrained integer property
    /// @throws NullPointerException if an object argument is `null`
    /// @throws IllegalArgumentException if `initialValue` is not positive
    static IntegerProperty positiveIntegerProperty(
            Object bean,
            String name,
            int initialValue,
            Runnable invalidation
    ) {
        Object owner = Objects.requireNonNull(bean, "bean");
        String propertyName = Objects.requireNonNull(name, "name");
        int initial = requirePositive(initialValue, propertyName);
        Runnable callback = Objects.requireNonNull(invalidation, "invalidation");
        return new SimpleIntegerProperty(owner, propertyName, initial) {
            /// Rejects a non-positive direct assignment before changing the property.
            ///
            /// @param value the new positive value
            @Override
            public void set(int value) {
                super.set(requirePositive(value, getName()));
            }

            /// Invokes the owner-provided invalidation callback.
            @Override
            protected void invalidated() {
                callback.run();
            }
        };
    }

    /// Returns a positive integer.
    ///
    /// @param value the value to validate
    /// @param name  the property name used in the exception message
    /// @return the validated positive value
    /// @throws IllegalArgumentException if `value` is not positive
    private static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive: " + value);
        }
        return value;
    }
}
