// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Provides shared helpers for m3fx CSS-backed component tokens.
@NotNullByDefault
final class M3Css {
    /// Prevents utility class instantiation.
    private M3Css() {
    }

    /// Returns whether a styleable property can be set by CSS.
    static boolean isSettable(StyleableDoubleProperty property) {
        return !property.isBound()
                && (!(property instanceof M3StyleableDoubleProperty m3Property) || !m3Property.isUserSet());
    }

    /// Returns whether a styleable object property can be set by CSS.
    static boolean isSettable(StyleableObjectProperty<?> property) {
        return !property.isBound()
                && (!(property instanceof M3StyleableObjectProperty<?> m3Property) || !m3Property.isUserSet());
    }

    /// Creates a styleable non-negative component token property.
    static StyleableDoubleProperty nonNegativeStyleableDoubleProperty(
            double initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, Number> cssMetaData,
            Runnable invalidation
    ) {
        return new M3StyleableDoubleProperty(
                initialValue,
                bean,
                name,
                cssMetaData,
                value -> nonNegative(value, name),
                invalidation
        );
    }

    /// Creates a styleable finite component token property.
    static StyleableDoubleProperty finiteStyleableDoubleProperty(
            double initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, Number> cssMetaData,
            Runnable invalidation
    ) {
        return new M3StyleableDoubleProperty(
                initialValue,
                bean,
                name,
                cssMetaData,
                value -> finite(value, name),
                invalidation
        );
    }

    /// Creates a styleable object component token property.
    static <T> StyleableObjectProperty<@Nullable T> styleableObjectProperty(
            @Nullable T initialValue,
            Object bean,
            String name,
            CssMetaData<? extends Styleable, @Nullable T> cssMetaData,
            Runnable invalidation
    ) {
        return new M3StyleableObjectProperty<>(initialValue, bean, name, cssMetaData, invalidation);
    }

    /// Validates that a CSS size token is not negative.
    static double nonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    /// Validates that a CSS numeric token is finite.
    static double finite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }
}
